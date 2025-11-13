package com.extractor.adapter.out;

import com.extractor.adapter.out.dto.law.etc.BuchickDto;
import com.extractor.adapter.out.dto.law.etc.HangDto;
import com.extractor.adapter.out.dto.law.mapping.LawDetailMappingDto;
import com.extractor.adapter.out.dto.law.response.GetLawDetailsResponse;
import com.extractor.adapter.out.enums.JoMunType;
import com.extractor.application.exception.NotFoundDocumentException;
import com.extractor.application.port.LawReadPort;
import com.extractor.domain.model.LawDocument;
import com.extractor.domain.vo.LawContentVo;
import com.extractor.global.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 법제처 API 기반 법령 정보 조회 어댑터
 */
@Slf4j
@Service
public class LawApiReadAdapter implements LawReadPort {

    private final RestTemplate restTemplate;

    @Value("${env.law.oc}")
    private String LAW_OC;

    @Autowired
    public LawApiReadAdapter(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder
                .rootUri("http://www.law.go.kr")
                .build();
    }

    /**
     * 법령 문서 조회
     *
     * @param lawId 법령 ID
     */
    @Override
    public LawDocument getLawDocumentsPort(Long lawId) {

        StringBuilder parameterBuilder = new StringBuilder();

        parameterBuilder
                .append("OC=").append(LAW_OC).append("&")
                .append("target=").append("law").append("&")
                .append("type=").append("JSON").append("&")
                .append("ID=").append(String.format("%06d", lawId));

        String url = String.format("/DRF/lawService.do?%s", parameterBuilder);

        ResponseEntity<GetLawDetailsResponse> responseEntity = restTemplate
                .exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});

        if (responseEntity.getBody() == null) {
            throw new NotFoundDocumentException();
        }

        LawDetailMappingDto lawDetailMappingDto = responseEntity.getBody().getResult();

        List<LawContentVo> lawContentVos = new ArrayList<>();

        // 조문
        AtomicInteger atomicInteger = new AtomicInteger(0);
        lawDetailMappingDto.getJoMunMappingDto().getData().forEach(joMunMappingData -> {
            StringBuilder contentBuilder = new StringBuilder(replaceContent(StringUtil.concat(joMunMappingData.getContents())));

            HangDto.toList(joMunMappingData.getHangMappingDtos()).forEach(hangDto -> {
                contentBuilder.append(hangDto.getContent());
                hangDto.getHos().forEach(hoDto -> {
                    contentBuilder.append(hoDto.getContent());
                    hoDto.getMocks().forEach(mockDto -> {
                        contentBuilder.append(mockDto.getContent());
                    });
                });
            });

            String type = joMunMappingData.getType();
            String content = contentBuilder.toString().trim();
            JoMunType joMunType = JoMunType.getType(type, content);

            String simpleTitle = "";
            String title = "";
            if (JoMunType.JO.equals(joMunType)) {
                if (joMunMappingData.getSecondsKey() == null) {
                    simpleTitle += "제" + joMunMappingData.getNum() + "조";
                } else {
                    simpleTitle += "제" + joMunMappingData.getNum() + "조의" + joMunMappingData.getSecondsKey();
                }
                title = simpleTitle + (joMunMappingData.getTitle() != null ? "(" + joMunMappingData.getTitle() + ")" : "");
            } else {
                Matcher matcher = Pattern.compile(joMunType.getPattern()).matcher(content);
                if (matcher.find()) simpleTitle = matcher.group(0);
                title = content;
            }

            lawContentVos.add(LawContentVo.builder()
                    .lawContentId((long) lawContentVos.size())
                    .lawId(lawId)
                    .version(Integer.parseInt(joMunMappingData.getActiveDate().format(DateTimeFormatter.ofPattern("yyyyMMdd"))))
                    .contentType(joMunType.name())
                    .categoryCode(joMunType.name())
                    .arrange(atomicInteger.getAndIncrement())
                    .simpleTitle(simpleTitle)
                    .title(title)
                    .content(content.replaceFirst(Pattern.quote(title), "").trim())
                    .lawLinkVos(Collections.emptyList())
                    .build());
        });

        // 부칙
        atomicInteger.set(0);
        BuchickDto.toList(lawDetailMappingDto.getBuchickMappingDto()).forEach(buchickDto -> {
            String content = replaceContent(buchickDto.getContent());

            String simpleTitle = "";
            String title = "";

            Matcher matcher = Pattern.compile("부(\\s+)?칙\\s<제\\d+호,\\d{4}.\\d{2}.\\d{2}>").matcher(content);
            if (matcher.find()) {
                simpleTitle  = title = matcher.group(0);
            }

            lawContentVos.add(LawContentVo.builder()
                    .lawContentId((long) lawContentVos.size())
                    .lawId(lawId)
                    .version(Integer.parseInt(buchickDto.getPromulgationDate().format(DateTimeFormatter.ofPattern("yyyyMMdd"))))
                    .contentType("BUCHICK")
                    .categoryCode("BUCHICK")
                    .arrange(atomicInteger.getAndIncrement())
                    .simpleTitle(simpleTitle)
                    .title(title)
                    .content(content.replaceFirst(Pattern.quote(title), "").trim())
                    .lawLinkVos(Collections.emptyList())
                    .build());
        });

        return LawDocument.builder()
                .lawId(Long.parseLong(lawDetailMappingDto.getBasicInfoMappingDto().getLawId()))
                .lawName(lawDetailMappingDto.getBasicInfoMappingDto().getName())
                .lawContents(lawContentVos)
                .build();
    }

    private static String replaceContent(String content) {
        return content
                .replaceAll("삭\\s?제\\s?<\\d{4}.\\d{1,2}.\\d{1,2}>", "")
                .replaceAll("<개\\s?정\\s?(,?\\s?\\d{4}.\\d{1,2}.\\d{1,2})+>", "")
                .replaceAll("<신\\s?설\\s?(,?\\s?\\d{4}.\\d{1,2}.\\d{1,2})+>", "")
                .replaceAll("<신\\s?설\\s?(,?\\s?\\d{4}.\\d{1,2}.\\d{1,2})+>", "")
                ;
    }
}
