package com.document.extractor.domain.model;

import com.document.extractor.application.enums.UpdateState;
import com.document.global.utils.HtmlUtil;
import com.document.global.utils.StringUtil;
import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

@Slf4j
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

    @Builder.Default
    private UpdateState updateState = UpdateState.STAY;

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
     * 버전 및 정렬 필드 업데이트
     *
     * @param version 버전
     * @param sortOrder 정렬 필드
     */
    public void update(Long version, Integer sortOrder) {
        this.version = version;
        this.sortOrder = sortOrder;
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
                String splitContent = this.content.substring(start, end);

                chunks.add(Chunk.builder()
                        .passageId(this.passageId)
                        .version(this.version)
                        .title(this.title)
                        .subTitle(this.subTitle)
                        .thirdTitle(this.thirdTitle)
                        .content(splitContent)
                        .compactContent(splitContent)
                        .tokenSize(splitContent.length())
                        .compactTokenSize(splitContent.length())
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

    /**
     * 패시지 DIFF 비교
     * @param previousPassages 이전 버전 패시지 목록
     * @param currentPassages  현재 버전 패시지 목록
     */
    public static void compareDiff(List<Passage> previousPassages, List<Passage> currentPassages) {
        // 이전 버전 패시지 변경 감지
        Patch<Passage> patches = DiffUtils.diff(previousPassages, currentPassages);

        // 변경 사항이 있는 경우
        if (!patches.getDeltas().isEmpty()) {
            for (AbstractDelta<Passage> delta : patches.getDeltas()) {
                List<Passage> sourcePassages = delta.getSource().getLines();
                List<Passage> targetPassages = delta.getTarget().getLines();

                switch (delta.getType()) {
                    case INSERT -> targetPassages.forEach(passage -> passage.updateState = UpdateState.INSERT);
                    case CHANGE -> {
                        // 원본 패시지 Iterator
                        Iterator<Passage> sourcePassagesIterator = sourcePassages.iterator();
                        // 코사인 유사도 판별 후, 최적의 패시지 매핑
                        while (sourcePassagesIterator.hasNext()) {
                            Passage sourcePassage = sourcePassagesIterator.next();
                            // 코사인 유사도 임계값 0.6
                            double maxScore = 0.6D;
                            Passage mappingPassage = null;

                            for (Passage targetPassage : targetPassages) {
                                double  score = sourcePassage.cosineSimilarity(targetPassage);
                                // 최적의 패시지 확인
                                if (maxScore <= score) {
                                    mappingPassage = targetPassage;
                                    maxScore = score;
                                }
                            }

                            // 매핑 된 경우
                            if (mappingPassage != null) {
                                // 수정 상태로 변경
                                mappingPassage.updateState = UpdateState.CHANGE;
                                // 부모 패시지 ID 지정
                                mappingPassage.setParentSortOrder(sourcePassage.getSortOrder());
                                // 목록 삭제
                                targetPassages.remove(mappingPassage);
                                // 수정 상태로 변경
                                sourcePassage.updateState = UpdateState.CHANGE;
                                sourcePassagesIterator.remove();
                            }
                        }

                        // 남은 sourcePassages 처리 (삭제)
                        for (Passage sourcePassage : sourcePassages) {
                            sourcePassage.updateState = UpdateState.DELETE;
                        }
                        // 남은 targetPassages 처리 (추가)
                        for (Passage targetPassage : targetPassages) {
                            targetPassage.updateState = UpdateState.INSERT;
                        }
                    }
                    case DELETE -> sourcePassages.forEach(passage -> passage.updateState = UpdateState.DELETE);
                }
            }
        }
    }
}
