package com.extractor.adapter.out;

import com.extractor.adapter.utils.FileUtil;
import com.extractor.application.port.ExtractPort;
import com.extractor.domain.model.HwpxDocument;
import com.extractor.domain.model.PdfDocument;
import com.extractor.domain.vo.document.OriginalDocumentVo;
import com.extractor.domain.vo.hwpx.HwpxImageVo;
import com.extractor.domain.vo.hwpx.HwpxSectionVo;
import com.extractor.global.enums.FileExtension;
import com.extractor.global.utils.StringUtil;
import com.extractor.global.utils.XmlUtil;
import kr.dogfoot.hwp2hwpx.Hwp2Hwpx;
import kr.dogfoot.hwplib.object.HWPFile;
import kr.dogfoot.hwplib.reader.HWPReader;
import kr.dogfoot.hwpxlib.object.HWPXFile;
import kr.dogfoot.hwpxlib.writer.HWPXWriter;
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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExtractAdapter implements ExtractPort {

    @Value("${env.snf-path}")
    private String SNF_PATH;

    /**
     * 한글 문서 추출
     * @param originalDocumentVo 원본 문서 정보
     */
    @Override
    public HwpxDocument extractHwpxDocumentPort(OriginalDocumentVo originalDocumentVo) {

        // HWP 변환
        if (FileExtension.HWP.isEquals(originalDocumentVo.getExtension())) {
            try {
                HWPFile fromFile = HWPReader.fromFile(originalDocumentVo.getFullPath().toString());
                HWPXFile toFile = Hwp2Hwpx.toHWPX(fromFile);
                HWPXWriter.toFilepath(toFile, originalDocumentVo.getFullPath().toString());
            } catch (Exception e) {
                throw new RuntimeException("convert hwp to hwpx error");
            }
        }

        // 문서 ID 생성
        String docId = StringUtil.generateRandomId();

        // 한글 파일 압축 해제
        File zipFile = originalDocumentVo.getFullPath().toFile();
        Path unZipPath = Paths.get(zipFile.getParent(), docId);

        if (!zipFile.exists()) throw new RuntimeException("not exists zip file");
        if (unZipPath.toFile().exists()) throw new RuntimeException("already exists unzip target directory");

        Path decompressionPath = FileUtil.decompression(zipFile, unZipPath.toFile());

        // metadata 추출
        String metaData = FileUtil.readFile(decompressionPath.resolve("Contents/content.hpf"));

        // XML DOM 파싱
        Document document = XmlUtil.parseXml(metaData);
        Element root = document.getDocumentElement();
        NodeList items = root.getElementsByTagName("opf:item");

        // 데이터 저장
        List<HwpxSectionVo> sections = new ArrayList<>();
        List<HwpxImageVo> images = new ArrayList<>();

        for (int itemIndex = 0; itemIndex < items.getLength(); itemIndex++) {
            Node item = items.item(itemIndex);

            String id = item.getAttributes().getNamedItem("id").getTextContent();
            String filePath = item.getAttributes().getNamedItem("href").getTextContent();
            String mediaType = item.getAttributes().getNamedItem("media-type").getTextContent();

            if (FileExtension.XML.isEquals(mediaType) && id.startsWith("section")) {
                File xmlFile = decompressionPath.resolve(filePath).toFile();

                if (xmlFile.exists()) {
                    String content = FileUtil.readFile(xmlFile.toPath())
                            .replace("<hp:lineBreak/>", "\n");

                    sections.add(HwpxSectionVo.builder()
                            .id(id)
                            .content(content)
                            .build());
                }
            } else if (mediaType.startsWith("image/")) {
                File imageFile = decompressionPath.resolve(filePath).toFile();

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
                .docId(docId)
                .name(originalDocumentVo.getOriginalFileName())
                .path(originalDocumentVo.getPath())
                .sections(sections)
                .images(images)
                .unZipPath(unZipPath)
                .build();
    }

    /**
     * PDF 문서 추출
     * @param originalDocumentVo 원본 문서 정보
     */
    @Override
    public PdfDocument extractPdfDocumentPort(OriginalDocumentVo originalDocumentVo) {

        // 문서 ID 생성
        String docId = StringUtil.generateRandomId();

        StringBuilder contentBuilder = new StringBuilder();

        try {
            String os = System.getProperty("os.name").toLowerCase();

            String[] cmd;
            if (os.contains("windows")) {
                cmd = new String[]{"cmd.exe", "/c", SNF_PATH, "-NO_WITHPAGE", "-C", "cp949", originalDocumentVo.getFullPath().toString()};
            } else if(os.contains("mac")) {
                cmd = new String[]{SNF_PATH, "-NO_WITHPAGE", "-C", "utf8", originalDocumentVo.getFullPath().toString()};
            } else {
                cmd = new String[]{SNF_PATH, "-NO_WITHPAGE", "-C", "utf8", originalDocumentVo.getFullPath().toString()};
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

        return PdfDocument.builder()
                .docId(docId)
                .name(originalDocumentVo.getOriginalFileName())
                .path(originalDocumentVo.getPath())
                .content(contentBuilder.toString())
                .build();
    }
}