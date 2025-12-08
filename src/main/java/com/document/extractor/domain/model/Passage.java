package com.document.extractor.domain.model;

import com.document.extractor.application.enums.UpdateState;
import com.document.global.utils.HtmlUtil;
import com.document.global.utils.StringUtil;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ToString
@Builder
@Getter
@AllArgsConstructor
public class Passage {

    private final Long passageId;

    private Long sourceId;

    private Long version;

    private String title;

    private final String subTitle;

    private final String thirdTitle;

    private final String content;

    private final String subContent;

    private final Integer tokenSize;

    private final LocalDateTime sysCreateDt;

    private final LocalDateTime sysModifyDt;

    @Setter
    @Builder.Default
    private UpdateState updateState = UpdateState.STAY;

    @Setter
    private Integer sortOrder;

    @Setter
    private Integer parentSortOrder;

    /**
     * 대상 문서 정보 연결
     *
     * @param sourceId 대상 문서 ID
     * @param title    제목
     */
    public void connectSource(Long sourceId, String title) {
        this.sourceId = sourceId;
        this.title = title;
    }

    /**
     * 버전 업데이트
     *
     * @param version 버전
     */
    public void updateVersion(Long version) {
        this.version = version;
    }

    /**
     * 토큰 수 기준 청킹 처리
     *
     * @param tokenSize   청킹 사이즈
     * @param overlapSize 오버랩 사이즈
     */
    public List<Chunk> chunking(int tokenSize, int overlapSize) {

        List<Chunk> chunks = new ArrayList<>();

        boolean isContainsTable = HtmlUtil.isContainsTableHtml(this.content);

        // 표가 없고 토큰 수 초과
        if (!isContainsTable && (0 < tokenSize && tokenSize < this.content.length())) {
            int step = tokenSize - overlapSize;

            for (int start = 0; start < this.content.length(); start += step) {
                int end = Math.min(this.content.length(), start + tokenSize);
                String content = this.content.substring(start, end);

                chunks.add(Chunk.builder()
                        .passageId(this.passageId)
                        .version(this.version)
                        .title(this.title)
                        .subTitle(this.subTitle)
                        .thirdTitle(this.thirdTitle)
                        .content(content)
                        .compactContent(content)
                        .tokenSize(content.length())
                        .compactTokenSize(content.length())
                        .subContent(this.subContent)
                        .sysCreateDt(this.sysCreateDt)
                        .sysModifyDt(this.sysModifyDt)
                        .build());
            }
        } else {
            // 표 마크 다운 변환
            String compactContent = HtmlUtil.convertTableHtmlToMarkdown(this.content);
            chunks.add(Chunk.builder()
                    .passageId(this.passageId)
                    .version(this.version)
                    .title(this.title)
                    .subTitle(this.subTitle)
                    .thirdTitle(this.thirdTitle)
                    .content(this.content)
                    .compactContent(compactContent)
                    .tokenSize(this.content.length())
                    .compactTokenSize(compactContent.length())
                    .subContent(this.subContent)
                    .sysCreateDt(this.sysCreateDt)
                    .sysModifyDt(this.sysModifyDt)
                    .build());
        }

        return chunks;
    }

    /**
     * 코사인 유사도 계산
     *
     * @param passage 비교 패시지
     * @return 코사인 유사도 스코어
     */
    public double cosineSimilarity(Passage passage) {
        return StringUtil.cosineSimilarity(passage.getContent(), this.content);
    }

    /**
     * DIFF 연산을 위한 equals 재정의
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Passage passage = (Passage) o;
        return Objects.equals(content, passage.content);
    }

    /**
     * DIFF 연산을 위한 hashCode 재정의
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(content);
    }
}
