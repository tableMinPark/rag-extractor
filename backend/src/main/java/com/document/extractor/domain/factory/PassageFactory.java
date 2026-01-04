package com.document.extractor.domain.factory;

import com.document.extractor.application.enums.SelectType;
import com.document.extractor.domain.model.*;
import com.document.extractor.domain.vo.PassageOptionVo;
import lombok.Builder;
import lombok.ToString;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

@ToString
public class PassageFactory {

    private final String content;

    private final String subContent;

    /**
     * 패시징 처리 변수
     */
    private final int depth;

    private final DocumentContent[][] titleBuffer;

    private final List<DocumentContent> documentContents;

    private final int contentTokenSize;

    private final int subContentTokenSize;

    private final int totalTokenSize;

    @Builder
    public PassageFactory(int depth, DocumentContent[][] titleBuffer, List<DocumentContent> documentContents, String content, String subContent) {
        this.depth = depth;
        this.titleBuffer = titleBuffer;
        this.documentContents = documentContents;
        this.content = content == null ? this.generateContent() : content;
        this.subContent = subContent == null ? this.generateSubContent() : subContent;
        this.contentTokenSize = this.content.length();
        this.subContentTokenSize = this.subContent.length();
        this.totalTokenSize = this.contentTokenSize + this.subContentTokenSize;
    }

    private static PassageFactory init(DocumentContent[][] titleBuffer, List<DocumentContent> documentContents) {
        return PassageFactory.builder()
                .depth(-1)
                .titleBuffer(copyTitleBuffer(titleBuffer))
                .documentContents(documentContents)
                .build();
    }

    private static PassageFactory nextStep(int depth, DocumentContent[][] titleBuffer, List<DocumentContent> documentContents) {
        return PassageFactory.builder()
                .depth(depth)
                .titleBuffer(copyTitleBuffer(titleBuffer))
                .documentContents(documentContents)
                .build();
    }

    /**
     * 패시징 (토큰)
     */
    public static List<Passage> passaging(List<DocumentContent> documentContents, PassageOptionVo passageOptionVo, int tokenSize) {

        DocumentContent[][] titleBuffer = new DocumentContent[passageOptionVo.getDepthSize()][];

        // 타이틀 버퍼 초기화
        for (int depth = 0; depth < passageOptionVo.getDepthSize(); depth++) {
            int prefixSize = passageOptionVo.getPatterns().get(depth).getSourcePrefixes().size();
            titleBuffer[depth] = new DocumentContent[prefixSize];
            Arrays.fill(titleBuffer[depth], null);
        }

        return passagingByToken(PassageFactory.init(titleBuffer, documentContents), Math.max(tokenSize, 1000));
    }

    /**
     * 패시징 (패턴)
     */
    public static List<Passage> passaging(List<DocumentContent> documentContents, PassageOptionVo passageOptionVo) {

        DocumentContent[][] titleBuffer = new DocumentContent[passageOptionVo.getDepthSize()][];

        // 타이틀 버퍼 초기화
        for (int depth = 0; depth < passageOptionVo.getDepthSize(); depth++) {
            int prefixSize = passageOptionVo.getPatterns().get(depth).getSourcePrefixes().size();
            titleBuffer[depth] = new DocumentContent[prefixSize];
            Arrays.fill(titleBuffer[depth], null);
        }

        // 본문 -> 타이틀 추출
        List<String> prefixes = new ArrayList<>();
        passageOptionVo.getPatterns().forEach(patternVo -> prefixes.addAll(patternVo.getSourcePrefixes().stream()
                .filter(SourcePrefix::getIsTitle)
                .map(SourcePrefix::getPrefix)
                .toList()));

        List<DocumentContent> filterDocumentContents = new ArrayList<>();
        for (DocumentContent documentContent : documentContents) {
            boolean isStop = false;
            for (SourceStopPattern stopPattern : passageOptionVo.getStopPatterns()) {
                Pattern pattern = Pattern.compile(stopPattern.getPrefix(), Pattern.MULTILINE);
                Matcher matcher = pattern.matcher(documentContent.getCompareText());

                if (matcher.find()) {
                    isStop = true;
                    break;
                }
            }
            if (isStop) break;
            else {
                documentContent.extractTitle(prefixes);
                filterDocumentContents.add(documentContent);
            }
        }

        return passaging(titleBuffer, PassageFactory.init(titleBuffer, filterDocumentContents), passageOptionVo);
    }

    /**
     * 청킹 프로 세스 (토큰)
     *
     * @param passageFactory 부모 청크
     * @param tokenSize      토큰 사이즈
     * @return 문서 청크 목록
     */
    private static List<Passage> passagingByToken(PassageFactory passageFactory, int tokenSize) {
        // 토큰 기준 청킹
        return IntStream.iterate(0, i -> i + tokenSize)
                .limit((passageFactory.content.length() + tokenSize - 1) / tokenSize)
                .mapToObj(i -> {
                    String content = passageFactory.content.substring(i, Math.min(passageFactory.content.length(), i + tokenSize));

                    boolean isSubContentMatch = false;
                    StringBuilder subContentBuilder = new StringBuilder();
                    for (DocumentContent documentContent : passageFactory.documentContents) {
                        for (DocumentContent subDocumentContent : documentContent.getSubDocumentContents()) {
                            if (content.contains(subDocumentContent.getCompareText())) {
                                subContentBuilder.append("\n")
                                        .append(subDocumentContent.getTitle().trim())
                                        .append(subDocumentContent.getTitle().trim().isBlank() ? " " : "")
                                        .append(subDocumentContent.getContext().trim());

                                isSubContentMatch = true;
                                break;
                            }
                        }

                        if (isSubContentMatch) break;
                    }

                    return Passage.builder()
                            .subTitle(passageFactory.getSubTitle())
                            .thirdTitle(passageFactory.getThirdTitle())
                            .content(content)
                            .subContent(subContentBuilder.toString().trim())
                            .tokenSize(content.length())
                            .build();
                })
                .toList();
    }

    /**
     * 청킹 재귀 프로 세스 (패턴)
     *
     * @param passageFactory 부모 청크
     * @param passageOptionVo  청킹 옵션
     * @return 문서 청크 목록
     */
    private static List<Passage> passaging(DocumentContent[][] titleBuffer, PassageFactory passageFactory, PassageOptionVo passageOptionVo) {

        List<SourcePattern> patternVos = passageOptionVo.getPatterns();
        int nextDepth = passageFactory.depth + 1;
        int contentTokenSize = passageFactory.contentTokenSize;
        int depthMaxTokenSize = patternVos.size() > nextDepth ? patternVos.get(nextDepth).getTokenSize() : 0;

        List<Passage> passages = new ArrayList<>();

        // content 가 빈 경우
        if (passageFactory.documentContents.isEmpty()) {
            return Collections.emptyList();
        }
        // 1개 남은 경우 (재귀 종료)
        else if (passageFactory.documentContents.size() == 1) {
            passages.add(Passage.builder()
                    .subTitle(passageFactory.getSubTitle())
                    .thirdTitle(passageFactory.getThirdTitle())
                    .content(passageFactory.content)
                    .subContent(passageFactory.subContent)
                    .tokenSize(passageFactory.content.length())
                    .build());
        }
        // 깊이 초과 (재귀 종료)
        else if (passageOptionVo.getDepthSize() <= nextDepth) {
            if (SelectType.NONE.equals(passageOptionVo.getSelectType())) {
                // 1개씩 content 1개씩 재귀 호출
                for (DocumentContent documentContent : passageFactory.documentContents) {
                    passages.addAll(passaging(copyTitleBuffer(titleBuffer), PassageFactory.nextStep(nextDepth, titleBuffer, List.of(documentContent)), passageOptionVo));
                }
            } else {
                // 하나의 content 로 저장
                passages.add(Passage.builder()
                        .subTitle(passageFactory.getSubTitle())
                        .thirdTitle(passageFactory.getThirdTitle())
                        .content(passageFactory.content)
                        .subContent(passageFactory.subContent)
                        .tokenSize(passageFactory.content.length())
                        .build());
            }
        }
        // DEPTH 기준 토큰 수 충족 (재귀 종료)
        else if (0 < depthMaxTokenSize && contentTokenSize <= depthMaxTokenSize) {
            passages.add(Passage.builder()
                    .subTitle(passageFactory.getSubTitle())
                    .thirdTitle(passageFactory.getThirdTitle())
                    .content(passageFactory.content)
                    .subContent(passageFactory.subContent)
                    .tokenSize(passageFactory.content.length())
                    .build());
        }
        else {
            int head = -1;
            SourcePattern patternVo = passageOptionVo.getPatterns().get(nextDepth);

            for (int contentIndex = 0; contentIndex < passageFactory.documentContents.size(); contentIndex++) {
                DocumentContent documentContent = passageFactory.documentContents.get(contentIndex);

                for (int prefixIndex = 0; prefixIndex < patternVo.getSourcePrefixes().size(); prefixIndex++) {
                    SourcePrefix prefix = patternVo.getSourcePrefixes().get(prefixIndex);

                    // 조건 확인
                    Pattern pattern = Pattern.compile(prefix.getPrefix(), Pattern.MULTILINE);
                    Matcher matcher = pattern.matcher(documentContent.getCompareText());

                    // 일치 or 정규식 부합한 경우
                    if (matcher.find()) {
                        // 재귀
                        passages.addAll(
                                passaging(
                                        copyTitleBuffer(titleBuffer),
                                        PassageFactory.nextStep(nextDepth, titleBuffer, passageFactory.documentContents.subList(Math.max(head, 0), contentIndex)),
                                        passageOptionVo
                                )
                        );

                        // 헤드 인덱스 변경
                        head = contentIndex;

                        // 타이틀 버퍼 정리
                        clearTitleBuffer(titleBuffer, passageOptionVo.getDepthSize(), nextDepth, prefixIndex);

                        // 타이틀 지정
                        if (prefix.getIsTitle()) {
                            titleBuffer[nextDepth][prefixIndex] = documentContent;
                        }
                        break;
                    }
                }
            }

            passages.addAll(
                    passaging(
                            copyTitleBuffer(titleBuffer),
                            PassageFactory.nextStep(nextDepth, titleBuffer, passageFactory.documentContents.subList(Math.max(head, 0), passageFactory.documentContents.size())),
                            passageOptionVo
                    )
            );
        }

        // 본문 공백 필터링 후 반환
        return passages.stream()
                .filter(c -> !c.getContent().isBlank())
                .toList();
    }

    /**
     * 중제목 조회
     *
     * @return 중제목
     */
    private String getSubTitle() {
        String subTitle = documentContents.size() == 1 ? documentContents.getFirst().getTitle() : "";

        String[] titles = this.getTitles();

        if (titles.length > 0) {
            subTitle = titles[0];
        }

        return subTitle;
    }

    /**
     * 소제목 조회
     *
     * @return 소제목
     */
    private String getThirdTitle() {
        String thirdTitle = "";

        String[] titles = this.getTitles();

        if (titles.length > 1) {
            thirdTitle = String.join("\n", Arrays.stream(titles)
                            .toList()
                            .subList(1, titles.length)
                            .stream()
                            .filter(s -> !s.isBlank()).toList())
                    .trim();
        }

        return thirdTitle;
    }

    /**
     * 타이틀 배열 조회
     *
     * @return 타이틀 배열
     */
    private String[] getTitles() {
        String[] titles = new String[this.titleBuffer.length];
        Arrays.fill(titles, "");

        for (int depth = 0; depth < this.titleBuffer.length; depth++) {
            Queue<DocumentContent> documentContentQueue = new ArrayDeque<>();

            for (DocumentContent documentContent : this.titleBuffer[depth]) {
                if (documentContent != null) {
                    documentContentQueue.offer(documentContent);
                }
            }

            StringBuilder titleBuilder = new StringBuilder();
            while (!documentContentQueue.isEmpty()) {
                DocumentContent documentContent = documentContentQueue.poll();

                if (!titleBuilder.isEmpty()) {
                    titleBuilder.append(" | ");
                }

                if (documentContentQueue.isEmpty()) {
                    titleBuilder.append(documentContent.getTitle());
                } else {
                    titleBuilder.append(documentContent.getSimpleTitle());
                }
            }

            // 타이틀 추가
            titles[depth] = titleBuilder.toString().trim();
        }

        return titles;
    }

    /**
     * 본문 생성
     *
     * @return 본문 문자열
     */
    private String generateContent() {

        StringBuilder contentBuilder = new StringBuilder();

        for (DocumentContent documentContent : this.documentContents) {
            contentBuilder.append("\n")
                    .append(documentContent.getContext().trim());
        }

        return contentBuilder.toString().trim();
    }

    /**
     * 부가 본문 생성
     *
     * @return 부가 본문 문자열
     */
    private String generateSubContent() {

        StringBuilder subContentBuilder = new StringBuilder();

        for (DocumentContent documentContent : this.documentContents) {
            for (DocumentContent subDocumentContent : documentContent.getSubDocumentContents()) {
                subContentBuilder.append("\n")
                        .append(subDocumentContent.getTitle())
                        .append(!subDocumentContent.getTitle().isBlank() ? " " : "")
                        .append(subDocumentContent.getContext().trim());
            }
        }

        return subContentBuilder.toString().trim();
    }

    /**
     * 타이틀 버퍼 정리
     *
     * @param prefixIndex 현재 prefixIndex
     */
    private static void clearTitleBuffer(DocumentContent[][] titleBuffer, int depthSize, int nowDepth, int prefixIndex) {

        for (int depth = nowDepth; depth < depthSize; depth++) {
            int nowPrefixIndex = depth != nowDepth ? 0 : prefixIndex;

            while (nowPrefixIndex < titleBuffer[depth].length) {
                titleBuffer[depth][nowPrefixIndex] = null;
                nowPrefixIndex++;
            }
        }
    }

    /**
     * 타이틀 버퍼 복사
     *
     * @return 복사 배열
     */
    private static DocumentContent[][] copyTitleBuffer(DocumentContent[][] titleBuffer) {

        DocumentContent[][] newTitleBuffer = new DocumentContent[titleBuffer.length][];

        for (int depth = 0; depth < titleBuffer.length; depth++) {
            newTitleBuffer[depth] = new DocumentContent[titleBuffer[depth].length];

            for (int titleIndex = 0; titleIndex < titleBuffer[depth].length; titleIndex++) {
                if (titleBuffer[depth][titleIndex] != null) {
                    newTitleBuffer[depth][titleIndex] = titleBuffer[depth][titleIndex];
                }
            }
        }

        return newTitleBuffer;
    }
}
