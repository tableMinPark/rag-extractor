package com.genai.extractor.service;

import com.genai.extractor.config.FileProperty;
import com.genai.extractor.service.domain.model.Document;
import com.genai.extractor.service.domain.model.HwpxDocument;
import com.genai.extractor.service.domain.model.PdfDocument;
import com.genai.extractor.repository.entity.FileDetailEntity;
import com.genai.extractor.service.vo.HwpxImageVo;
import com.genai.extractor.service.vo.HwpxSectionVo;
import com.genai.common.exception.InvalidConnectionException;
import com.genai.common.utils.FileUtil;
import com.genai.common.utils.PdfUtil;
import com.genai.common.utils.StringUtil;
import com.genai.common.utils.XmlUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.dogfoot.hwp2hwpx.Hwp2Hwpx;
import kr.dogfoot.hwplib.object.HWPFile;
import kr.dogfoot.hwplib.reader.HWPReader;
import kr.dogfoot.hwpxlib.object.HWPXFile;
import kr.dogfoot.hwpxlib.writer.HWPXWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ExtractService {

    private final FileProperty fileProperty;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public ExtractService(FileProperty fileProperty,
                          @Qualifier("webClient") WebClient webClient,
                          ObjectMapper objectMapper) {
        this.fileProperty = fileProperty;
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }

    public Document extractFile(FileDetailEntity fileDetail, String extractTypeCode) {
        if (!fileDetail.getExt().contains("hwp") && !fileDetail.getExt().contains("hwpx")) {
            return PdfDocument.builder()
                    .name(fileDetail.getOriginFileName())
                    .extractTypeCode(extractTypeCode)
                    .sections(PdfUtil.extractByPage(fileDetail.getUrl()))
                    .build();
        }

        String tempFileName = StringUtil.generateRandomId();
        Path fullFilePath = Paths.get(fileDetail.getUrl());
        Path unZipDirPath = Paths.get(fileProperty.getFileStorePath(), fileProperty.getTempDir(), tempFileName);
        Path zipFilePath = Paths.get(fileProperty.getFileStorePath(), fileProperty.getTempDir(), tempFileName + ".zip");

        if (fileDetail.getExt().equals("hwp")) {
            try {
                HWPFile fromFile = HWPReader.fromFile(fullFilePath.toString());
                HWPXFile toFile = Hwp2Hwpx.toHWPX(fromFile);
                HWPXWriter.toFilepath(toFile, zipFilePath.toString());
            } catch (Exception e) {
                throw new RuntimeException("not support hwp file");
            }
        } else {
            FileUtil.copyFile(fullFilePath.toString(), zipFilePath.toString());
        }

        if (!zipFilePath.toFile().exists()) {
            throw new RuntimeException("not exists zip file");
        }

        FileUtil.decompression(zipFilePath.toString(), unZipDirPath.toString());

        Path metaDataPath = unZipDirPath.resolve("Contents").resolve("content.hpf");
        String metaData = FileUtil.read(metaDataPath.toString());

        Element root = XmlUtil.parseXml(metaData).getDocumentElement();
        NodeList items = root.getElementsByTagName("opf:item");

        List<HwpxSectionVo> sections = new ArrayList<>();
        Map<String, HwpxImageVo> images = new HashMap<>();

        for (int itemIndex = 0; itemIndex < items.getLength(); itemIndex++) {
            Node item = items.item(itemIndex);

            String resourceId = item.getAttributes().getNamedItem("id").getTextContent();
            String resourceFilePath = item.getAttributes().getNamedItem("href").getTextContent();
            String mediaType = item.getAttributes().getNamedItem("media-type").getTextContent();

            if (mediaType.endsWith("xml") && resourceId.startsWith("section")) {
                File xmlFile = unZipDirPath.resolve(resourceFilePath).toFile();

                if (xmlFile.exists()) {
                    String content = FileUtil.read(xmlFile.toPath().toString())
                            .replaceAll("<hp:lineBreak/>", "\n")
                            .replaceAll("\\s[a-zA-Z_-]+=\"[^\"]*[<>][^\"]*\"", "");

                    sections.add(HwpxSectionVo.builder()
                            .id(resourceId)
                            .content(content)
                            .build());
                }
            } else if (mediaType.startsWith("image/")) {
                File imageFile = unZipDirPath.resolve(resourceFilePath).toFile();

                // TODO: Image -> Text 추출 (OCR)
                String content = "";

                if (imageFile.exists()) {
                    images.put(resourceId, HwpxImageVo.builder()
                            .id(resourceId)
                            .content(content)
                            .path(imageFile.toPath())
                            .ext(mediaType)
                            .build());
                }
            }
        }

        FileUtil.deleteFile(zipFilePath.toString());
        FileUtil.deleteDirectory(unZipDirPath.toString());

        return HwpxDocument.builder()
                .name(StringUtil.removeExtension(fileDetail.getOriginFileName()))
                .extractTypeCode(extractTypeCode)
                .sections(sections)
                .images(images)
                .build();
    }

    public String extractText(FileDetailEntity fileDetail) {
        return this.extractFile(fileDetail, "html").getContent();
    }

    public Document readRepoDocument(String uri, String extractTypeCode) {
        ResponseEntity<String> responseEntity = webClient.get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(response -> response
                        .bodyToMono(String.class)
                        .map(body -> new ResponseEntity<>(body, response.statusCode())))
                .block();

        if (responseEntity == null || !responseEntity.getStatusCode().is2xxSuccessful() || responseEntity.getBody() == null) {
            throw new InvalidConnectionException("원격 문서 조회 서버");
        }

        try {
            Document responseBody = objectMapper.readValue(responseEntity.getBody(), Document.class);

            if (responseBody == null) {
                throw new InvalidConnectionException("원격 문서 조회 서버");
            }

            return responseBody;

        } catch (JsonProcessingException e) {
            throw new InvalidConnectionException("원격 문서 조회 서버");
        }
    }
}
