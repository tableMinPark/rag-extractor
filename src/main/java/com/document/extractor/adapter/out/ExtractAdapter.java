package com.document.extractor.adapter.out;

import com.document.extractor.adapter.propery.FileProperty;
import com.document.extractor.application.exception.InvalidConnectionException;
import com.document.extractor.application.exception.NotFoundException;
import com.document.extractor.application.port.ExtractPort;
import com.document.extractor.domain.model.*;
import com.document.extractor.domain.vo.HwpxImageVo;
import com.document.extractor.domain.vo.HwpxSectionVo;
import com.document.global.utils.FileUtil;
import com.document.global.utils.StringUtil;
import com.document.global.utils.XmlUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.dogfoot.hwp2hwpx.Hwp2Hwpx;
import kr.dogfoot.hwplib.object.HWPFile;
import kr.dogfoot.hwplib.reader.HWPReader;
import kr.dogfoot.hwpxlib.object.HWPXFile;
import kr.dogfoot.hwpxlib.writer.HWPXWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
@RequiredArgsConstructor
public class ExtractAdapter implements ExtractPort {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final FileProperty fileProperty;

    @Value("${env.custom.law}")
    private String lawUri;

    @Value("${env.custom.law-history}")
    private String lawHistoryUri;

    @Value("${env.custom.law-quotation}")
    private String lawQuotationUri;

    @Value("${env.custom.law-content}")
    private String lawContentUri;

    @Value("${env.custom.law-quotation-content}")
    private String lawQuotationContentUri;

    @Value("${env.custom.manual}")
    private String manualUri;

    /**
     * 문서 추출
     *
     * @param fileDetail  원본 문서 정보
     * @param extractTypeCode 표 추출 타입 코드
     * @return 문서
     */
    @Override
    public Document extractFilePort(FileDetail fileDetail, String extractTypeCode) {

        // 한글 제외 다른 확장자 파일 추출
        if (!fileDetail.getExt().contains("hwp") && !fileDetail.getExt().contains("hwpx")) {
            return PdfDocument.builder()
                    .name(fileDetail.getOriginFileName())
                    .content(FileUtil.readFile(fileProperty.getReadBinary(), fileDetail.getUrl()).trim())
                    .build();
        }

        // 사용 파일 경로
        String tempFileName = StringUtil.generateRandomId();
        Path fullFilePath = Paths.get(fileDetail.getUrl());
        Path unZipDirPath = Paths.get(fileProperty.getFileStorePath(), fileProperty.getTempDir(), tempFileName);
        Path zipFilePath = Paths.get(fileProperty.getFileStorePath(), fileProperty.getTempDir(), tempFileName + ".zip");

        // HWP 파일 변환
        if (fileDetail.getExt().equals("hwp")) {
            try {
                HWPFile fromFile = HWPReader.fromFile(fullFilePath.toString());
                HWPXFile toFile = Hwp2Hwpx.toHWPX(fromFile);
                HWPXWriter.toFilepath(toFile, zipFilePath.toString());
            } catch (Exception e) {
                // 변환 실패
                return PdfDocument.builder()
                        .name(fileDetail.getOriginFileName())
                        .content(FileUtil.readFile(fileProperty.getReadBinary(), fileDetail.getUrl()).trim())
                        .convertError(true)
                        .build();
            }
        }
        // 원본 문서 복사
        else {
            FileUtil.copyFile(fullFilePath.toString(), zipFilePath.toString());
        }

        // 압축 파일 존재 여부 확인
        if (!zipFilePath.toFile().exists()) {
            throw new RuntimeException("not exists zip file");
        }

        // 압축 해제
        FileUtil.decompression(zipFilePath.toString(), unZipDirPath.toString());

        // metadata 추출
        Path metaDataPath = unZipDirPath.resolve("Contents").resolve("content.hpf");
        String metaData = FileUtil.read(metaDataPath.toString());

        // XML DOM 파싱
        Element root = XmlUtil.parseXml(metaData).getDocumentElement();
        NodeList items = root.getElementsByTagName("opf:item");

        // 데이터 저장
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
                            .replaceAll("<hp:lineBreak/>", "\n")                        // 개행 태그 개행 문자로 치환
                            .replaceAll("\\s[a-zA-Z_-]+=\"[^\"]*[<>][^\"]*\"", "");     // XML 속성 내에 "<", ">" 가 있는 경우 속성 제거

                    sections.add(HwpxSectionVo.builder()
                            .id(resourceId)
                            .content(content)
                            .build());
                }
            } else if (mediaType.startsWith("image/")) {
                File imageFile = unZipDirPath.resolve(resourceFilePath).toFile();

                // TODO: Image -> Text 추출 (OCR)
                String content = "<img id=\"" + resourceId + "\"/>";

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

        // 압축 파일 삭제
        FileUtil.deleteFile(zipFilePath.toString());

        // 압축 해제 디렉토리 삭제
        FileUtil.deleteDirectory(unZipDirPath.toString());

        return HwpxDocument.builder()
                .name(StringUtil.removeExtension(fileDetail.getOriginFileName()))
                .extractTypeCode(extractTypeCode)
                .sections(sections)
                .images(images)
                .build();
    }

    /**
     * 문서 텍스트 추출
     *
     * @param fileDetail 원본 문서 정보
     */
    @Override
    public String extractTextPort(FileDetail fileDetail) {
        return FileUtil.readFile(fileProperty.getReadBinary(), fileDetail.getUrl()).trim();
    }

    /**
     * 법령 문서 추출
     *
     * @param lawId 법령 ID
     * @return 추출 결과
     */
    @Override
    public ExtractDocument extractLawPort(String lawId) {
        try {
            String uri = String.format(lawUri, lawId);
            ResponseEntity<String> responseEntity = webClient.get()
                    .uri(uri)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchangeToMono(response -> response
                            .bodyToMono(String.class)
                            .map(body -> new ResponseEntity<>(body, response.statusCode())))
                    .block();

            // 응답 체크
            if (responseEntity == null || !responseEntity.getStatusCode().is2xxSuccessful() || responseEntity.getBody() == null) {
                throw new InvalidConnectionException("원격 법령 문서 조회 서버");
            }

            return objectMapper.readValue(responseEntity.getBody(), new TypeReference<ExtractDocument>() {
            });

        } catch (JsonProcessingException e) {
            throw new NotFoundException("원격 법령 문서");
        }
    }

    /**
     * 법령 이력 추출
     *
     * @param lawId 법령 ID
     * @return 추출 결과
     */
    @Override
    public ExtractDocument extractLawHistoryPort(String lawId) {
        try {
            ResponseEntity<String> responseEntity = webClient.get()
                    .uri(String.format(lawHistoryUri, lawId))
                    .accept(MediaType.APPLICATION_JSON)
                    .exchangeToMono(response -> response
                            .bodyToMono(String.class)
                            .map(b -> new ResponseEntity<>(b, response.statusCode())))
                    .block();

            // 응답 체크
            if (responseEntity == null || !responseEntity.getStatusCode().is2xxSuccessful() || responseEntity.getBody() == null) {
                throw new InvalidConnectionException("원격 문서 조회 서버");
            }

            return objectMapper.readValue(responseEntity.getBody(), new TypeReference<ExtractDocument>() {
            });

        } catch (JsonProcessingException e) {
            throw new NotFoundException("원격 법령 문서");
        }
    }

    /**
     * 법령 본문 추출
     *
     * @param lawId      법령 ID
     * @param lawHistory 법령 이력 코드
     * @return 추출 결과
     */
    @Override
    public ExtractDocument extractLawContentPort(String lawId, String lawHistory) {
        try {
            ResponseEntity<String> responseEntity = webClient.get()
                    .uri(String.format(lawContentUri, lawId, lawHistory))
                    .accept(MediaType.APPLICATION_JSON)
                    .exchangeToMono(response -> response
                            .bodyToMono(String.class)
                            .map(b -> new ResponseEntity<>(b, response.statusCode())))
                    .block();

            // 응답 체크
            if (responseEntity == null || !responseEntity.getStatusCode().is2xxSuccessful() || responseEntity.getBody() == null) {
                throw new InvalidConnectionException("원격 법령 문서 조회 서버");
            }

            return objectMapper.readValue(responseEntity.getBody(), new TypeReference<ExtractDocument>() {
            });
        } catch (JsonProcessingException e) {
            throw new NotFoundException("원격 법령 문서");
        }
    }

    /**
     * 법령 본문 추출
     *
     * @param lawId        법령 ID
     * @param lawHistory   법령 이력 코드
     * @param lawContentId 본문 ID
     * @return 추출 결과
     */
    @Override
    public ExtractDocument extractLawContentPort(String lawId, String lawHistory, String lawContentId) {
        try {
            ResponseEntity<String> responseEntity = webClient.get()
                    .uri(String.format(lawQuotationContentUri, lawId, lawHistory, lawContentId))
                    .accept(MediaType.APPLICATION_JSON)
                    .exchangeToMono(response -> response
                            .bodyToMono(String.class)
                            .map(b -> new ResponseEntity<>(b, response.statusCode())))
                    .block();

            // 응답 체크
            if (responseEntity == null || !responseEntity.getStatusCode().is2xxSuccessful() || responseEntity.getBody() == null) {
                throw new InvalidConnectionException("원격 법령 문서 조회 서버");
            }

            return objectMapper.readValue(responseEntity.getBody(), new TypeReference<ExtractDocument>() {
            });
        } catch (JsonProcessingException e) {
            throw new NotFoundException("원격 법령 문서");
        }
    }

    /**
     * 법령 연결 정보 추출
     *
     * @param lawId        법령 ID
     * @param lawHistory   법령 이력 코드
     * @return 추출 결과
     */
    @Override
    public ExtractDocument extractLawQuotationPort(String lawId, String lawHistory) {
        try {
            ResponseEntity<String> responseEntity = webClient.get()
                    .uri(String.format(lawQuotationUri, lawId, lawHistory))
                    .accept(MediaType.APPLICATION_JSON)
                    .exchangeToMono(response -> response
                            .bodyToMono(String.class)
                            .map(b -> new ResponseEntity<>(b, response.statusCode())))
                    .block();

            // 응답 체크
            if (responseEntity == null || !responseEntity.getStatusCode().is2xxSuccessful() || responseEntity.getBody() == null) {
                throw new InvalidConnectionException("원격 법령 문서 조회 서버");
            }

            return objectMapper.readValue(responseEntity.getBody(), new TypeReference<ExtractDocument>() {
            });
        } catch (JsonProcessingException e) {
            throw new NotFoundException("원격 법령 문서");
        }
    }

    /**
     * 메뉴얼 문서 추출
     *
     * @param manualId 메뉴얼 ID
     * @return 추출 결과
     */
    @Override
    public ExtractDocument extractManualPort(String manualId) {
        try {
            String uri = String.format(manualUri, manualId);
            ResponseEntity<String> responseEntity = webClient.get()
                    .uri(uri)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchangeToMono(response -> response
                            .bodyToMono(String.class)
                            .map(body -> new ResponseEntity<>(body, response.statusCode())))
                    .block();

            // 응답 체크
            if (responseEntity == null || !responseEntity.getStatusCode().is2xxSuccessful() || responseEntity.getBody() == null) {
                throw new InvalidConnectionException("원격 메뉴얼 문서 조회 서버");
            }

            return objectMapper.readValue(responseEntity.getBody(), new TypeReference<ExtractDocument>() {
            });

        } catch (JsonProcessingException e) {
            throw new NotFoundException("원격 메뉴얼 문서");
        }
    }
}