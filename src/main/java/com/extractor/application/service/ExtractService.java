package com.extractor.application.service;

import com.extractor.application.port.ExtractPort;
import com.extractor.application.port.FilePort;
import com.extractor.application.usecase.ExtractUseCase;
import com.extractor.application.vo.ExtractContentVo;
import com.extractor.application.vo.ExtractVo;
import com.extractor.application.vo.FileVo;
import com.extractor.domain.model.Document;
import com.extractor.domain.model.FileDocument;
import com.extractor.global.enums.ExtractType;
import com.extractor.global.enums.FileExtension;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExtractService implements ExtractUseCase {

    private final ExtractPort extractPort;
    private final FilePort filePort;

    /**
     * 한글 문서 추출
     *
     * @param fileVo 원본 문서 정보
     */
    @Override
    public ExtractVo extractHwpxDocumentUseCase(FileVo fileVo, ExtractType extractType) {

        // 파일 업로드
        FileDocument fileDocument = filePort.uploadFilePort(fileVo);

        Document document;
        if (FileExtension.PDF.equals(fileDocument.getExtension())) {
            document = extractPort.extractPdfPort(fileDocument);
        } else {
            document = extractPort.extractHwpxPort(fileDocument, extractType);
        }

        try {
            return ExtractVo.builder()
                    .name(document.getName())
                    .extension(document.getExtension())
                    .extractContents(document.getDocumentContents().stream()
                            .map(line -> ExtractContentVo.builder()
                                    .type(line.getType().name())
                                    .content(line.getContext())
                                    .build())
                            .toList())
                    .build();
        } finally {
            // 파일 삭제
            filePort.removeFilePort(fileDocument);
        }
    }

    /**
     * PDf 문서 추출
     *
     * @param fileVo 원본 문서 정보
     */
    @Override
    public ExtractVo extractPdfDocumentUseCase(FileVo fileVo) {

        // 파일 업로드
        FileDocument fileDocument = filePort.uploadFilePort(fileVo);

        try {
            Document document = extractPort.extractPdfPort(fileDocument);
            return ExtractVo.builder()
                    .name(document.getName())
                    .extension(document.getExtension())
                    .extractContents(document.getDocumentContents().stream()
                            .map(line -> ExtractContentVo.builder()
                                    .type(line.getType().name())
                                    .content(line.getContext())
                                    .build())
                            .toList())
                    .build();
        } finally {
            // 파일 삭제
            filePort.removeFilePort(fileDocument);
        }
    }

    /**
     * 문서 텍스트 추출
     *
     * @param fileVo 원본 문서 정보
     */
    @Override
    public String extractDocumentUseCase(FileVo fileVo) {

        // 파일 업로드
        FileDocument fileDocument = filePort.uploadFilePort(fileVo);

        try {
            return extractPort.extractTextPort(fileDocument);
        } finally {
            // 파일 삭제
            filePort.removeFilePort(fileDocument);
        }
    }
}
