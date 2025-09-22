package com.extractor.adapter.out;

import com.extractor.application.port.FilePort;
import com.extractor.domain.model.FileDocument;
import com.extractor.domain.vo.FileDocumentVo;
import com.extractor.global.enums.FileExtension;
import com.extractor.global.utils.FileUtil;
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
     *
     * @param fileDocumentVo 원본 문서 Vo
     * @return 원본 문서 데이터
     */
    @Override
    public FileDocument uploadFilePort(FileDocumentVo fileDocumentVo) {

        String fileId = StringUtil.generateRandomId();
        String originalFileName = fileDocumentVo.getOriginalFileName();
        Path path = Paths.get(UPLOAD_PATH);

        FileExtension extension = fileDocumentVo.getExtension();
        String fileName = fileId + "." + extension.getSimpleExtension();
        Path fullPath = path.resolve(fileName);

        if (!path.toFile().exists()) {
            FileUtil.mkdirs(path);
        }

        try {
            Files.write(fullPath, fileDocumentVo.getData());
        } catch (IOException e) {
            throw new RuntimeException("upload file error");
        }

        // HWP 파일 변환
        if (FileExtension.HWP.equals(extension)) {
            try {
                // HWP 파일 읽기
                HWPFile fromFile = HWPReader.fromFile(fullPath.toString());
                HWPXFile toFile = Hwp2Hwpx.toHWPX(fromFile);

                // HWP 파일 삭제
                FileUtil.deleteFile(fullPath);

                // 파일 메타 데이터 변경
                extension = FileExtension.HWPX;
                fileName = fileId + "." + extension.getSimpleExtension();
                fullPath = path.resolve(fileName);

                // HWPX 파일 쓰기
                HWPXWriter.toFilepath(toFile, fullPath.toString());

            } catch (Exception e) {
                extension = FileExtension.PDF;
                fileName = fileId + "." + extension.getSimpleExtension();

                // 파일명 PDF 로 변경
                FileUtil.moveFile(fullPath, path.resolve(fileName));
                fullPath = path.resolve(fileName);
            }
        }

        // HWPX 파일 압축 해제
        if (FileExtension.HWPX.equals(extension)) {
            // 압축 해제 경로
            Path unZipPath = path.resolve(fileId);

            // 압축 파일 존재 여부 확인
            if (!fullPath.toFile().exists()) {
                throw new RuntimeException("not exists zip file");
            }

            // 압축 해제 및 path 변경
            path = FileUtil.decompression(fullPath.toFile(), unZipPath.toFile());

            // HWPX 파일 이동
            fullPath = FileUtil.moveFile(fullPath, unZipPath.resolve(fileName));

        }

        return FileDocument.builder()
                .originalFileName(originalFileName)
                .path(path)
                .fullPath(fullPath)
                .extension(extension)
                .build();
    }

    /**
     * 파일 정리
     *
     * @param fileDocument 원본 문서 도메인 객체
     */
    @Override
    public void clearFilePort(FileDocument fileDocument) {
        // 파일 삭제
        FileUtil.deleteFile(fileDocument.getFullPath());

        // HWPX 압축 폴더 삭제
        if (FileExtension.HWPX.equals(fileDocument.getExtension())) {
            FileUtil.deleteDirectory(fileDocument.getPath());
        }
    }
}