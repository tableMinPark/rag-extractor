package com.document.extractor.adapter.in.enums;

import com.document.extractor.adapter.in.dto.response.ResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Getter
@AllArgsConstructor
public enum Response {

    // 공통
    INVALID_METHOD_PARAMETER(HttpStatus.BAD_REQUEST, 1000, "적절하지 않은 파라미터 입니다.", ""),
    INVALID_REQUEST_BODY(HttpStatus.BAD_REQUEST, 1001, "적절하지 않은 요청 바디 입니다..", ""),
    NOT_FOUND(HttpStatus.NOT_FOUND, 1002, "리소스를 찾을 수 없습니다.", ""),
    INVALID_CONNECTION(HttpStatus.INTERNAL_SERVER_ERROR, 1003, "서버간 통신이 원할 하지 않습니다.", ""),
    INVALID_SOURCE_TYPE(HttpStatus.INTERNAL_SERVER_ERROR, 1004, "적절하지 않은 대상 문서 타입입니다.", ""),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 1005, "내부 서버 에러가 발생했습니다.", ""),

    // 대상 문서 관리
    CREATE_FILE_SOURCE_SUCCESS(HttpStatus.OK, 1100, "파일 대상 문서 등록에 성공했습니다.", ""),
    CREATE_REPO_SOURCE_SUCCESS(HttpStatus.OK, 1101, "원격 대상 문서 등록에 성공했습니다.", ""),
    GET_SOURCE_SUCCESS(HttpStatus.OK, 1102, "대상 문서 조회에 성공했습니다.", ""),
    GET_SOURCES_SUCCESS(HttpStatus.OK, 1103, "대상 문서 목록 조회에 성공했습니다.", ""),

    // 추출
    EXTRACT_FILE_SUCCESS(HttpStatus.OK, 1200, "파일 내용 추출에 성공했습니다.", ""),
    EXTRACT_TEXT_SUCCESS(HttpStatus.OK, 1201, "파일 텍스트 추출에 성공했습니다.", ""),

    // 청크
    CHUNK_FILES_SUCCESS(HttpStatus.OK, 1300, "파일 문서 청킹에 성공했습니다.", ""),
    CHUNK_REPOS_SUCCESS(HttpStatus.OK, 1301, "원격 문서 청킹에 성공했습니다.", ""),
    CHUNK_SOURCE_SUCCESS(HttpStatus.OK, 1302, "대상 문서 청킹에 성공했습니다.", ""),
    GET_PASSAGE_SUCCESS(HttpStatus.OK, 1303, "패시지 조회에 성공했습니다.", ""),
    GET_PASSAGES_SUCCESS(HttpStatus.OK, 1304, "패시지 목록 조회에 성공했습니다.", ""),
    CREATE_CHUNK_SUCCESS(HttpStatus.OK, 1305, "청크 등록에 성공했습니다.", ""),
    GET_CHUNK_SUCCESS(HttpStatus.OK, 1306, "청크 조회에 성공했습니다.", ""),
    GET_CHUNKS_SUCCESS(HttpStatus.OK, 1307, "청크 목록 조회에 성공했습니다.", ""),
    UPDATE_CHUNK_SUCCESS(HttpStatus.OK, 1308, "청크 수정에 성공했습니다.", ""),
    DELETE_CHUNK_SUCCESS(HttpStatus.OK, 1309, "청크 삭제에 성공했습니다.", ""),

    // 청킹 배치
    CHUNK_BATCH_SUCCESS(HttpStatus.OK, 1400, "대상 문서 청킹 배치 처리에 성공했습니다.", ""),
    CHUNK_BATCHES_SUCCESS(HttpStatus.OK, 1401, "대상 문서 다중 청킹 배치 처리에 성공했습니다.", ""),
    ;

    private final HttpStatus statusCode;
    private final int code;
    private final String message;
    private final String status;

    public String setStatus(HttpStatus httpStatus, String status) {
        if (httpStatus != HttpStatus.OK) {
            if (status == null || status.isBlank()) {
                return "error";
            } else {
                return status;
            }
        }
        return "success";
    }

    public <T> ResponseDto<Map<String, Object>> toResponseDto() {
        return ResponseDto.<Map<String, Object>>builder()
                .code(this.code)
                .message(this.message)
                .result(Collections.emptyMap())
                .status(setStatus(this.statusCode, this.status))
                .build();
    }

    public <T> ResponseDto<T> toResponseDto(T result) {
        return ResponseDto.<T>builder()
                .code(this.code)
                .message(this.message)
                .result(result)
                .status(setStatus(this.statusCode, this.status))
                .build();
    }

    public <T> ResponseDto<T> toResponseDto(String customMessage, T result) {
        return ResponseDto.<T>builder()
                .code(this.code)
                .message(Optional.ofNullable(customMessage).orElse(this.message))
                .result(result)
                .status(setStatus(this.statusCode, this.status))
                .build();
    }
}
