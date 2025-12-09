package com.document.extractor.adapter.out;

import com.document.extractor.adapter.propery.RepoProperty;
import com.document.extractor.application.exception.InvalidConnectionException;
import com.document.extractor.application.port.DocumentReadPort;
import com.document.extractor.domain.model.Document;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * DB 기반 법령 정보 조회 어댑터
 */
@Service
@RequiredArgsConstructor
public class DocumentReadAdapter implements DocumentReadPort {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final RepoProperty repoProperty;

    /**
     * 원격 문서 조회
     *
     * @param repoType        원격 문서 타입
     * @param repoId          원격 문서 ID
     * @param extractTypeCode 표 추출 타입 코드
     */
    @Override
    public Document getRepoDocumentPort(String repoType, String repoId, String extractTypeCode) {

        String uri = String.format(repoProperty.getUrl(), repoType, repoId, extractTypeCode);

        ResponseEntity<String> responseEntity = webClient.get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(response -> response
                        .bodyToMono(String.class)
                        .map(body -> new ResponseEntity<>(body, response.statusCode())))
                .block();

        // 응답 체크
        if (responseEntity == null || !responseEntity.getStatusCode().is2xxSuccessful() || responseEntity.getBody() == null) {
            throw new InvalidConnectionException("원격 문서 조회 서버");
        }

        try {
            String body = responseEntity.getBody();

            // 역직렬화
            Document responseBody = objectMapper.readValue(body, Document.class);

            // 응답 바디 체크
            if (responseBody == null) {
                throw new InvalidConnectionException("원격 문서 조회 서버");
            }

            return responseBody;

        } catch (JsonProcessingException e) {
            throw new InvalidConnectionException("원격 문서 조회 서버");
        }
    }

    /**
     * 원격 문서 조회
     *
     * @param uri 원격 문서 URI
     */
    @Override
    public Document getRepoDocumentPort(String uri) {

        ResponseEntity<String> responseEntity = webClient.get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(response -> response
                        .bodyToMono(String.class)
                        .map(body -> new ResponseEntity<>(body, response.statusCode())))
                .block();

        // 응답 체크
        if (responseEntity == null || !responseEntity.getStatusCode().is2xxSuccessful() || responseEntity.getBody() == null) {
            throw new InvalidConnectionException("원격 문서 조회 서버");
        }

        try {
            String body = responseEntity.getBody();

            // 역직렬화
            Document responseBody = objectMapper.readValue(body, Document.class);

            // 응답 바디 체크
            if (responseBody == null) {
                throw new InvalidConnectionException("원격 문서 조회 서버");
            }

            return responseBody;

        } catch (JsonProcessingException e) {
            throw new InvalidConnectionException("원격 문서 조회 서버");
        }
    }
}
