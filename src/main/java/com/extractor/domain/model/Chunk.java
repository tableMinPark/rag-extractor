package com.extractor.domain.model;

import com.extractor.domain.vo.PatternVo;
import com.extractor.domain.vo.PrefixVo;
import com.extractor.global.enums.ChunkType;
import lombok.Getter;
import lombok.ToString;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ToString
@Getter
public class Chunk {

    private final int depth;

    private final DocumentContent[][] titleBuffer;

    private final List<DocumentContent> documentContents;

    private final String content;

    private final String subContent;

    private final int contentTokenSize;

    private final int subContentTokenSize;

    private final int totalTokenSize;

    public Chunk(DocumentContent[][] titleBuffer, List<DocumentContent> documentContents) {
        this(-1, titleBuffer, documentContents);
    }

    public Chunk(int depth, DocumentContent[][] titleBuffer, List<DocumentContent> documentContents) {
        this.depth = depth;
        this.titleBuffer = copyTitleBuffer(titleBuffer);
        this.documentContents = documentContents;
        this.content = this.generateContent();
        this.subContent = this.generateSubContent();
        this.contentTokenSize = this.content.length();
        this.subContentTokenSize = this.subContent.length();
        this.totalTokenSize = this.contentTokenSize + this.subContentTokenSize;
    }

    private Chunk(int depth, DocumentContent[][] titleBuffer, String content, String subContent) {
        this.depth = depth;
        this.titleBuffer = copyTitleBuffer(titleBuffer);
        this.documentContents = Collections.emptyList();
        this.content = content;
        this.subContent = subContent;
        this.contentTokenSize = this.content.length();
        this.subContentTokenSize = this.subContent.length();
        this.totalTokenSize = this.contentTokenSize + this.subContentTokenSize;
    }

    /**
     * 청킹
     */
    public static List<Chunk> chunking(List<DocumentContent> documentContents, ChunkOption chunkOption) {

        DocumentContent[][] titleBuffer = new DocumentContent[chunkOption.getDepthSize()][];

        // 타이틀 버퍼 초기화
        for (int depth = 0; depth < chunkOption.getDepthSize(); depth++) {
            int prefixSize = chunkOption.getPatterns().get(depth).getPrefixes().size();
            titleBuffer[depth] = new DocumentContent[prefixSize];
            Arrays.fill(titleBuffer[depth], null);
        }

        // 토큰 수 기반 청킹
        if (ChunkType.TOKEN.equals(chunkOption.getType())) {
            return chunking(new Chunk(titleBuffer, documentContents), chunkOption);
        }
        // 타이틀 추출
        else if (ChunkType.REGEX.equals(chunkOption.getType())) {
            List<String> prefixes = new ArrayList<>();
            chunkOption.getPatterns().forEach(patternVo -> prefixes.addAll(patternVo.getPrefixes().stream().map(PrefixVo::getPrefix).toList()));
            documentContents.forEach(documentContent -> documentContent.extractTitle(prefixes));
        }

        return chunking(titleBuffer, new Chunk(titleBuffer, documentContents), chunkOption);
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
     * 타이틀 배열 조회
     *
     * @return 타이틀 배열
     */
    public String[] getTitles() {
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
     * 청킹 프로 세스 (토큰)
     * @param chunk 부모 청크
     * @param chunkOption 청킹 옵션
     * @return 문서 청크 목록
     */
    private static List<Chunk> chunking(Chunk chunk, ChunkOption chunkOption) {

        List<Chunk> chunks = new ArrayList<>();
        Queue<String> contextQueue = new ArrayDeque<>();

        contextQueue.offer(chunk.getContent());

        // chunkOption.maxTokenSize 크기 기준 문자열 분리
        if (!contextQueue.isEmpty()) {
            int queueSize = contextQueue.size();

            while (!contextQueue.isEmpty() && queueSize > 0) {
                String context = contextQueue.poll();

                if (context.isBlank()) continue;
                else if (context.length() <= chunkOption.getMaxTokenSize()) {
                    contextQueue.offer(context);
                } else {
                    for (int contextIndex = 0; contextIndex < context.length(); contextIndex += chunkOption.getMaxTokenSize()) {
                        contextQueue.offer(context.substring(contextIndex, Math.min(contextIndex + chunkOption.getMaxTokenSize(), context.length())));
                    }
                }

                queueSize--;
            }
        }

        // 분리 문자열 저장 (오버랩)
        if (!contextQueue.isEmpty()) {
            String preContext = "";
            String currentContext = "";
            String nextContext = "";

            while (!contextQueue.isEmpty()) {
                preContext = currentContext;
                currentContext = contextQueue.poll();
                nextContext = "";

                if (!contextQueue.isEmpty()) {
                    nextContext = contextQueue.peek();
                }

                int emptyContextSize = Math.max(0, chunkOption.getMaxTokenSize() - currentContext.length());
                int overlapSize = chunkOption.getOverlapSize() + Math.max(0, emptyContextSize / 2);

                String headOverlap = preContext.substring(Math.max(0, preContext.length() - overlapSize));
                String tailOverlap = nextContext.substring(0, Math.min(overlapSize, nextContext.length()));

                boolean isSubContentMatch = false;
                StringBuilder subContentBuilder = new StringBuilder();
                for (DocumentContent documentContent : chunk.getDocumentContents()) {
                    for (DocumentContent subDocumentContent : documentContent.getSubDocumentContents()) {
                        if (currentContext.contains(subDocumentContent.getCompareText())) {
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

                chunks.add(new Chunk(-1, chunk.getTitleBuffer(), headOverlap + currentContext + tailOverlap, subContentBuilder.toString().trim()));
            }
        }

        return chunks;
    }

    /**
      청킹 재귀 프로 세스 (패턴)
     *
     * @param chunk 부모 청크
     * @param chunkOption 청킹 옵션
     * @return 문서 청크 목록
     */
    private static List<Chunk> chunking(DocumentContent[][] titleBuffer, Chunk chunk, ChunkOption chunkOption) {

        int nextDepth = chunk.getDepth() + 1;
        int tokenSize = chunk.getContentTokenSize();

        List<Chunk> chunks = new ArrayList<>();

        // 깊이 초과
        if (nextDepth >= chunkOption.getDepthSize()) {
            // 전체 토큰 수 충족 (재귀 종료)
            if (0 <= chunkOption.getMaxTokenSize() && chunkOption.getMaxTokenSize() <= tokenSize) {
                chunks.add(chunk);
            }
            // documentContent 가 1개 초과인 경우 (재귀 실행/토큰 기준 분리)
            else if (chunk.getDocumentContents().size() > 1) {
                int mid = chunk.getDocumentContents().size() / 2;

                chunks.addAll(chunking(
                        copyTitleBuffer(titleBuffer),
                        new Chunk(nextDepth, titleBuffer, chunk.getDocumentContents().subList(0, mid)),
                        chunkOption));
                chunks.addAll(chunking(
                        copyTitleBuffer(titleBuffer),
                        new Chunk(nextDepth, titleBuffer, chunk.getDocumentContents().subList(mid, chunk.getDocumentContents().size())),
                        chunkOption));
            }
            // 최대 토큰 수가 음수인 경우 (재귀 종료)
            else if (chunk.getDocumentContents().size() == 1 && chunkOption.getMaxTokenSize() <= 0) {
                chunks.add(chunk);
            }
            // documentContent 가 1개인 경우 (재귀 종료)
            else if (chunk.getDocumentContents().size() == 1) {
                DocumentContent documentContent = chunk.getDocumentContents().getFirst();
                Queue<String> contextQueue = new ArrayDeque<>();

                contextQueue.offer(documentContent.getContext());

                // TODO: 개행 기준 마지막 분리 중지
//                // 개행 두번 기준 문자열 분리
//                if (!contextQueue.isEmpty()) {
//                    int queueSize = contextQueue.size();
//                    while (!contextQueue.isEmpty() && queueSize > 0) {
//                        String context = contextQueue.poll().trim();
//
//                        for (String splitContext : context.split("\n\n")) {
//                            contextQueue.offer(splitContext + "\n\n");
//                        }
//
//                        queueSize--;
//                    }
//                }
//
//                // 개행 한번 기준 문자열 분리
//                if (!contextQueue.isEmpty()) {
//                    int queueSize = contextQueue.size();
//
//                    while (!contextQueue.isEmpty() && queueSize > 0) {
//                        String context = contextQueue.poll().trim();
//
//                        if (context.isBlank()) continue;
//                        else if (context.length() <= chunkOption.getMaxTokenSize()) {
//                            contextQueue.offer(context);
//                        } else {
//                            for (String splitContext : context.split("\n")) {
//                                contextQueue.offer(splitContext + "\n");
//                            }
//                        }
//
//                        queueSize--;
//                    }
//                }

                // chunkOption.maxTokenSize 크기 기준 문자열 분리
                if (!contextQueue.isEmpty()) {
                    int queueSize = contextQueue.size();

                    while (!contextQueue.isEmpty() && queueSize > 0) {
                        String context = contextQueue.poll();

                        if (context.isBlank()) continue;
                        else if (context.length() <= chunkOption.getMaxTokenSize()) {
                            contextQueue.offer(context);
                        } else {
                            for (int contextIndex = 0; contextIndex < context.length(); contextIndex += chunkOption.getMaxTokenSize()) {
                                contextQueue.offer(context.substring(contextIndex, Math.min(contextIndex + chunkOption.getMaxTokenSize(), context.length())));
                            }
                        }

                        queueSize--;
                    }
                }

                // 분리 문자열 저장 (오버랩)
                if (!contextQueue.isEmpty()) {
                    String preContext = "";
                    String currentContext = "";
                    String nextContext = "";

                    while (!contextQueue.isEmpty()) {
                        preContext = currentContext;
                        currentContext = contextQueue.poll();
                        nextContext = "";

                        if (!contextQueue.isEmpty()) {
                            nextContext = contextQueue.peek();
                        }

                        int emptyContextSize = Math.max(0, chunkOption.getMaxTokenSize() - currentContext.length());
                        int overlapSize = chunkOption.getOverlapSize() + Math.max(0, emptyContextSize / 2);

                        String headOverlap = preContext.substring(Math.max(0, preContext.length() - overlapSize));
                        String tailOverlap = nextContext.substring(0, Math.min(overlapSize, nextContext.length()));

                        StringBuilder subContentBuilder = new StringBuilder();
                        for (DocumentContent subDocumentContent : documentContent.getSubDocumentContents()) {
                            if (currentContext.contains(subDocumentContent.getCompareText())) {
                                subContentBuilder.append("\n")
                                        .append(subDocumentContent.getTitle().trim())
                                        .append(subDocumentContent.getTitle().trim().isBlank() ? " " : "")
                                        .append(subDocumentContent.getContext().trim());
                            }
                        }

                        chunks.add(new Chunk(nextDepth, titleBuffer, headOverlap + currentContext + tailOverlap, subContentBuilder.toString().trim()));
                    }
                }
            }
        }
        // 깊이 별 최대 토큰 수 충족 (재귀 종료)
        else if (tokenSize <= chunkOption.getPatterns().get(nextDepth).getTokenSize()) {
            chunks.add(chunk);
        }
        // 토큰 수 초과 (재귀 실행 / DocumentContent 기준 분리)
        else {
            int head = -1;
            PatternVo patternVo = chunkOption.getPatterns().get(nextDepth);

            for (int contentIndex = 0; contentIndex < chunk.getDocumentContents().size(); contentIndex++) {
                DocumentContent documentContent = chunk.getDocumentContents().get(contentIndex);

                for (int prefixIndex = 0; prefixIndex < patternVo.getPrefixes().size(); prefixIndex++) {
                    PrefixVo prefix = patternVo.getPrefixes().get(prefixIndex);

                    // 조건 확인
                    boolean isMatch;

                    // 완전 일치 조건 확인
                    if (ChunkType.EQUALS.equals(chunkOption.getType())) {
                        isMatch = prefix.getPrefix().equals(documentContent.getCompareText());
                    }
                    // 정규식 부합 조건 확인
                    else {
                        Pattern pattern = Pattern.compile(prefix.getPrefix(), Pattern.MULTILINE);
                        Matcher matcher = pattern.matcher(documentContent.getCompareText());
                        isMatch = matcher.find();
                    }

                    // 일치 or 정규식 부합한 경우
                    if (isMatch) {
                        // 재귀
                        chunks.addAll(chunking(
                                copyTitleBuffer(titleBuffer),
                                new Chunk(nextDepth, titleBuffer, chunk.getDocumentContents().subList(Math.max(head, 0), contentIndex)),
                                chunkOption));

                        // 헤드 인덱스 변경
                        head = contentIndex;

                        // 타이틀 버퍼 정리
                        clearTitleBuffer(titleBuffer, chunkOption.getDepthSize(), nextDepth, prefixIndex);

                        // 타이틀 지정
                        if (!prefix.getIsDeleting()) {
                            titleBuffer[nextDepth][prefixIndex] = documentContent;
                        }
                        break;
                    }
                }
            }

            chunks.addAll(chunking(
                    copyTitleBuffer(titleBuffer),
                    new Chunk(nextDepth, titleBuffer, chunk.getDocumentContents().subList(Math.max(head, 0), chunk.getDocumentContents().size())),
                    chunkOption));
        }

        // 본문 공백 필터링 후 반환
        return chunks.stream()
                .filter(c -> !c.getContent().isBlank())
                .toList();
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
