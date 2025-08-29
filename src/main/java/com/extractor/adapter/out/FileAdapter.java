package com.extractor.adapter.out;

import com.extractor.adapter.utils.FileUtil;
import com.extractor.application.port.FilePort;
import com.extractor.domain.model.OriginalDocument;
import com.extractor.domain.vo.document.OriginalDocumentVo;
import com.extractor.global.enums.FileExtension;
import com.extractor.global.utils.StringUtil;
import kr.dogfoot.hwp2hwpx.Hwp2Hwpx;
import kr.dogfoot.hwplib.object.HWPFile;
import kr.dogfoot.hwplib.reader.HWPReader;
import kr.dogfoot.hwpxlib.object.HWPXFile;
import kr.dogfoot.hwpxlib.writer.HWPXWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileAdapter implements FilePort {

    @Value("${env.upload-path}")
    private String UPLOAD_PATH;

    /**
     * 파일 업로드
     * @param originalDocumentVo 원본 문서 Vo
     * @return 원본 문서 데이터
     */
    @Override
    public OriginalDocument uploadFilePort(OriginalDocumentVo originalDocumentVo) {

        String originalFileName = originalDocumentVo.getOriginalFileName();
        String extension = originalDocumentVo.getExtension();
        String fileName = StringUtil.generateRandomId();
        Path path = Paths.get(UPLOAD_PATH);
        Path fullPath = path.resolve(fileName);

        if (!path.toFile().exists()) {
            path.toFile().mkdirs();
        }

        try {
            Files.write(fullPath, originalDocumentVo.getData());
        } catch (IOException e) {
            throw new RuntimeException("upload file error");
        }

        if (FileExtension.HWP.isEquals(extension)) {
            try {
                HWPFile fromFile = HWPReader.fromFile(fullPath.toString());
                HWPXFile toFile = Hwp2Hwpx.toHWPX(fromFile);
                HWPXWriter.toFilepath(toFile, fullPath.toString());
                extension = FileExtension.HWPX.getExtension();
            } catch (Exception e) {
                throw new RuntimeException("convert hwp to hwpx error");
            }
        }

        return OriginalDocument.builder()
                .fileName(fileName)
                .originalFileName(originalFileName)
                .path(path)
                .fullPath(fullPath)
                .extension(extension)
                .build();
    }

    /**
     * 파일 정리
     * @param originalDocument 원본 문서 도메인 객체
     */
    @Override
    public void clearFilePort(OriginalDocument originalDocument) {
        // 파일 삭제
        FileUtil.deleteFile(originalDocument.getFullPath());
    }
}
