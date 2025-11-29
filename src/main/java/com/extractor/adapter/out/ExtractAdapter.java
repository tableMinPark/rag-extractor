package com.extractor.adapter.out;

import com.extractor.adapter.propery.FileProperty;
import com.extractor.application.port.ExtractPort;
import com.extractor.domain.model.Document;
import com.extractor.domain.model.FileDetail;
import com.extractor.domain.model.HwpxDocument;
import com.extractor.domain.model.PdfDocument;
import com.extractor.domain.vo.HwpxImageVo;
import com.extractor.domain.vo.HwpxSectionVo;
import com.extractor.global.utils.FileUtil;
import com.extractor.global.utils.StringUtil;
import com.extractor.global.utils.XmlUtil;
import kr.dogfoot.hwp2hwpx.Hwp2Hwpx;
import kr.dogfoot.hwplib.object.HWPFile;
import kr.dogfoot.hwplib.reader.HWPReader;
import kr.dogfoot.hwpxlib.object.HWPXFile;
import kr.dogfoot.hwpxlib.writer.HWPXWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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

    private final FileProperty fileProperty;
    private final FileUtil fileUtil;

    /**
     * 문서 추출
     *
     * @param fileDetail 원본 문서 정보
     * @return 문서
     */
    @Override
    public Document extractFilePort(FileDetail fileDetail) {

        // 한글 제외 다른 확장자 파일 추출
        if (!fileDetail.getExt().contains("hwp") && !fileDetail.getExt().contains("hwpx")) {
            return PdfDocument.builder()
                    .name(fileDetail.getOriginalFileName())
                    .content(fileUtil.readFile(Paths.get(fileDetail.getUrl())).trim())
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
                        .name(fileDetail.getOriginalFileName())
                        .content(fileUtil.readFile(Paths.get(fileDetail.getUrl())).trim())
                        .build();
            }
        }
        // 원본 문서 복사
        else {
            fileUtil.copyFile(fullFilePath, zipFilePath);
        }

        // 압축 파일 존재 여부 확인
        if (!zipFilePath.toFile().exists()) {
            throw new RuntimeException("not exists zip file");
        }

        // 압축 해제
        fileUtil.decompression(zipFilePath.toFile(), unZipDirPath.toFile());

        // metadata 추출
        String metaData = fileUtil.read(unZipDirPath.resolve("Contents").resolve("content.hpf"));

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
                    String content = fileUtil.read(xmlFile.toPath())
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
        fileUtil.deleteFile(zipFilePath);

        // 압축 해제 디렉토리 삭제
        fileUtil.deleteDirectory(unZipDirPath);

        return HwpxDocument.builder()
                .name(fileDetail.getOriginalFileName())
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
        return fileUtil.readFile(Paths.get(fileDetail.getUrl())).trim();
    }
}