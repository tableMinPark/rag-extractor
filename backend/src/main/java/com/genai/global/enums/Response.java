package com.genai.global.enums;

import com.genai.global.dto.ResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Getter
@AllArgsConstructor
public enum Response {

    // 인증
    LOGIN_SUCCESS(HttpStatus.UNAUTHORIZED, 0, "로그인에 실패했습니다.", ""),
    LOGIN_FAIL(HttpStatus.UNAUTHORIZED, 1, "로그인에 실패했습니다.", ""),

    // 공통
    INVALID_METHOD_PARAMETER(HttpStatus.BAD_REQUEST, 1000, "적절하지 않은 파라미터 입니다.", ""),
    INVALID_REQUEST_BODY(HttpStatus.BAD_REQUEST, 1001, "적절하지 않은 요청 바디 입니다..", ""),
    NOT_FOUND(HttpStatus.NOT_FOUND, 1002, "리소스를 찾을 수 없습니다.", ""),
    INVALID_CONNECTION(HttpStatus.INTERNAL_SERVER_ERROR, 1003, "서버간 통신이 원할 하지 않습니다.", ""),
    INVALID_SOURCE_TYPE(HttpStatus.INTERNAL_SERVER_ERROR, 1004, "적절하지 않은 대상 문서 타입입니다.", ""),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 1005, "내부 서버 에러가 발생했습니다.", ""),

    // 추출
    EXTRACT_FILE_SUCCESS(HttpStatus.OK, 1100, "파일 내용 추출에 성공했습니다.", ""),
    EXTRACT_TEXT_SUCCESS(HttpStatus.OK, 1101, "파일 텍스트 추출에 성공했습니다.", ""),

    // 대상 문서
    CREATE_FILE_SOURCE_SUCCESS(HttpStatus.OK, 1200, "파일 대상 문서 등록에 성공했습니다.", ""),
    CREATE_REPO_SOURCE_SUCCESS(HttpStatus.OK, 1201, "원격 대상 문서 등록에 성공했습니다.", ""),
    GET_SOURCE_SUCCESS(HttpStatus.OK, 1202, "대상 문서 조회에 성공했습니다.", ""),
    GET_SOURCES_SUCCESS(HttpStatus.OK, 1203, "대상 문서 목록 조회에 성공했습니다.", ""),
    GET_SOURCE_CATEGORIES_SUCCESS(HttpStatus.OK, 1204, "대상 문서 카테고리 목록 조회에 성공했습니다.", ""),
    GET_SOURCE_TOTAL_COUNT_SUCCESS(HttpStatus.OK, 1205, "총 대상 문서 수 조회에 성공했습니다.", ""),
    DELETE_SOURCE_SUCCESS(HttpStatus.OK, 1206, "대상 문서 삭제에 성공했습니다.", ""),
    UPDATE_IS_BATCH_SUCCESS(HttpStatus.OK, 1207, "대상 문서 배치 여부 수정에 성공했습니다.", ""),

    // 패시지
    GET_PASSAGE_SUCCESS(HttpStatus.OK, 1301, "패시지 조회에 성공했습니다.", ""),
    GET_PASSAGES_SUCCESS(HttpStatus.OK, 1302, "패시지 목록 조회에 성공했습니다.", ""),
    GET_PASSAGE_TOTAL_COUNT_SUCCESS(HttpStatus.OK, 1303, "총 청크 수 조회에 성공했습니다.", ""),

    // 청크
    CHUNK_FILES_SUCCESS(HttpStatus.OK, 1400, "파일 문서 청킹에 성공했습니다.", ""),
    CHUNK_REPOS_SUCCESS(HttpStatus.OK, 1401, "원격 문서 청킹에 성공했습니다.", ""),
    CHUNK_SOURCE_SUCCESS(HttpStatus.OK, 1402, "대상 문서 청킹에 성공했습니다.", ""),
    CREATE_CHUNK_SUCCESS(HttpStatus.OK, 1403, "청크 등록에 성공했습니다.", ""),
    GET_CHUNK_SUCCESS(HttpStatus.OK, 1404, "청크 조회에 성공했습니다.", ""),
    GET_CHUNKS_SUCCESS(HttpStatus.OK, 1405, "청크 목록 조회에 성공했습니다.", ""),
    UPDATE_CHUNK_SUCCESS(HttpStatus.OK, 1406, "청크 수정에 성공했습니다.", ""),
    DELETE_CHUNK_SUCCESS(HttpStatus.OK, 1407, "청크 삭제에 성공했습니다.", ""),
    GET_CHUNK_TOTAL_COUNT_SUCCESS(HttpStatus.OK, 1408, "총 청크 수 조회에 성공했습니다.", ""),

    // 배치
    BATCH_CHUNK_SUCCESS(HttpStatus.OK, 1500, "대상 문서 청킹 배치 처리에 성공했습니다.", ""),

    // 색인
    EMBED_SUCCESS(HttpStatus.OK, 1600, "색인 처리에 성공했습니다.", ""),
    DELETE_EMBED_SUCCESS(HttpStatus.OK, 1601, "색인 삭제에 성공했습니다.", ""),

    // 검색
    KEYWORD_SEARCH_SUCCESS(HttpStatus.OK, 1700, "키워드 검색에 성공했습니다.", ""),
    VECTOR_SEARCH_SUCCESS(HttpStatus.OK, 1701, "벡터 검색에 성공했습니다.", ""),
    SEARCH_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 1702, "검색 중 오류가 발생했습니다.", ""),
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
