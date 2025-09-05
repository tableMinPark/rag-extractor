package com.extractor.adapter.out;

import com.extractor.adapter.utils.FileUtil;
import com.extractor.application.port.ExtractPort;
import com.extractor.domain.model.HwpxDocument;
import com.extractor.domain.model.OriginalDocument;
import com.extractor.domain.model.PdfDocument;
import com.extractor.domain.vo.hwpx.HwpxImageVo;
import com.extractor.domain.vo.hwpx.HwpxSectionVo;
import com.extractor.global.enums.FileExtension;
import com.extractor.global.utils.XmlUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExtractAdapter implements ExtractPort {

    @Value("${env.snf-path}")
    private String SNF_PATH;

    /**
     * 한글 문서 추출
     * @param originalDocument 원본 문서 정보
     */
    @Override
    public HwpxDocument extractHwpxDocumentPort(OriginalDocument originalDocument) {

        // 데이터 저장
        List<HwpxSectionVo> sections = new ArrayList<>();
        List<HwpxImageVo> images = new ArrayList<>();

            // metadata 추출
            String metaData = FileUtil.readFile(originalDocument.getPath().resolve("Contents/content.hpf"));

            // XML DOM 파싱
            Document document = XmlUtil.parseXml(metaData);
            Element root = document.getDocumentElement();
            NodeList items = root.getElementsByTagName("opf:item");

            for (int itemIndex = 0; itemIndex < items.getLength(); itemIndex++) {
                Node item = items.item(itemIndex);

                String id = item.getAttributes().getNamedItem("id").getTextContent();
                String filePath = item.getAttributes().getNamedItem("href").getTextContent();
                String mediaType = item.getAttributes().getNamedItem("media-type").getTextContent();

                if (mediaType.endsWith(FileExtension.XML.getSimpleExtension()) && id.startsWith("section")) {
                    File xmlFile = originalDocument.getPath().resolve(filePath).toFile();

                    if (xmlFile.exists()) {
                        String content = FileUtil.readFile(xmlFile.toPath())
                                .replace("<hp:lineBreak/>", "\n");

                        sections.add(HwpxSectionVo.builder()
                                .id(id)
                                .content(content)
                                .build());
                    }
                } else if (mediaType.startsWith("image/")) {
                    File imageFile = originalDocument.getPath().resolve(filePath).toFile();

                    if (imageFile.exists()) {
                        images.add(HwpxImageVo.builder()
                                .id(id)
                                .path(imageFile.toPath())
                                .extension(mediaType)
                                .build());
                    }
                }
            }

        return HwpxDocument.builder()
                .docId(originalDocument.getDocId())
                .name(originalDocument.getOriginalFileName())
                .path(originalDocument.getPath())
                .sections(sections)
                .images(images)
                .build();
    }

    /**
     * PDF 문서 추출
     * @param originalDocument 원본 문서 정보
     */
    @Override
    public PdfDocument extractPdfDocumentPort(OriginalDocument originalDocument) {
        return PdfDocument.builder()
                .docId(originalDocument.getDocId())
                .name(originalDocument.getOriginalFileName())
                .path(originalDocument.getPath())
                .content(this.extractWithSnf(originalDocument.getFullPath()))
                .build();
    }

    /**
     * 문서 텍스트 추출
     * @param originalDocument 원본 문서 정보
     */
    @Override
    public String extractDocumentPort(OriginalDocument originalDocument) {
        return this.extractWithSnf(originalDocument.getFullPath()).trim();
    }

    /**
     * SNF 텍스트 추출
     * @param path 파일 경로
     * @return 추출 문자열
     */
    private String extractWithSnf(Path path) {
        StringBuilder contentBuilder = new StringBuilder();

        try {
            String os = System.getProperty("os.name").toLowerCase();

            String[] cmd;
            if (os.contains("windows")) {
                cmd = new String[]{"cmd.exe", "/c", SNF_PATH, "-NO_WITHPAGE", "-C", "utf8", path.toString()};
            } else if (os.contains("mac")) {
                cmd = new String[]{SNF_PATH, "-NO_WITHPAGE", "-C", "utf8", path.toString()};
            } else {
                cmd = new String[]{SNF_PATH, "-NO_WITHPAGE", "-C", "utf8", path.toString()};
            }

            Process process = Runtime.getRuntime().exec(cmd);

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                contentBuilder.append(line).append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException("snf extract error");
        }

        return contentBuilder.toString();
    }
}