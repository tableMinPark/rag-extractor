package com.extractor.adapter.out;

import com.extractor.adapter.utils.FileUtil;
import com.extractor.application.port.ExtractPort;
import com.extractor.domain.model.HwpxDocument;
import com.extractor.domain.vo.document.OriginalDocumentVo;
import com.extractor.domain.vo.hwpx.HwpxImageVo;
import com.extractor.domain.vo.hwpx.HwpxSectionVo;
import com.extractor.global.utils.StringUtil;
import com.extractor.global.utils.XmlUtil;
import kr.dogfoot.hwp2hwpx.Hwp2Hwpx;
import kr.dogfoot.hwplib.object.HWPFile;
import kr.dogfoot.hwplib.reader.HWPReader;
import kr.dogfoot.hwpxlib.object.HWPXFile;
import kr.dogfoot.hwpxlib.writer.HWPXWriter;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExtractAdapter implements ExtractPort {

    /**
     * 한글 문서 XML 트리 추출
     *
     * @param originalDocumentVo 한글 문서 경로
     */
    @Override
    public HwpxDocument extractHwpxDocumentPort(OriginalDocumentVo originalDocumentVo) {

        if ("hwp".equals(originalDocumentVo.getExtension())) {
            try {
                HWPFile fromFile = HWPReader.fromFile(originalDocumentVo.getFullPath().toString());
                HWPXFile toFile = Hwp2Hwpx.toHWPX(fromFile);
                HWPXWriter.toFilepath(toFile, originalDocumentVo.getFullPath().toString());
            } catch (Exception e) {
                throw new RuntimeException("convert hwp to hwpx error");
            }
        }

        String docId = StringUtil.generateRandomId();
        List<HwpxSectionVo> sections = new ArrayList<>();
        List<HwpxImageVo> images = new ArrayList<>();

        // 한글 파일 압축 해제
        File zipFile = originalDocumentVo.getFullPath().toFile();
        Path unZipPath = Paths.get(zipFile.getParent(), docId);

        if (!zipFile.exists() || unZipPath.toFile().exists()) {
            throw new RuntimeException("not exists file");
        }

        Path decompressionPath = FileUtil.decompression(zipFile, unZipPath.toFile());

        // metadata 추출
        String metaData = FileUtil.readFile(decompressionPath.resolve("Contents/content.hpf"));

        // XML DOM 파싱
        Document document = XmlUtil.parseXml(metaData);
        Element root = document.getDocumentElement();
        NodeList items = root.getElementsByTagName("opf:item");

        for (int itemIndex = 0; itemIndex < items.getLength(); itemIndex++) {
            Node item = items.item(itemIndex);

            String id = item.getAttributes().getNamedItem("id").getTextContent();
            String filePath = item.getAttributes().getNamedItem("href").getTextContent();
            String mediaType = item.getAttributes().getNamedItem("media-type").getTextContent();

            if ("application/xml".equals(mediaType) && id.startsWith("section")) {
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
                            .extension(mediaType.replace("image/", ""))
                            .build());
                }
            }
        }

        return HwpxDocument.builder()
                .docId(docId)
                .name(originalDocumentVo.getOriginalFileName())
                .sections(sections)
                .images(images)
                .path(originalDocumentVo.getPath())
                .unZipPath(unZipPath)
                .build();
    }
}