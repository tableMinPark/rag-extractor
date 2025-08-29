package com.extractor.domain.model;

import com.extractor.domain.vo.pattern.ChunkPatternVo;
import com.extractor.domain.vo.pattern.PatternVo;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@ToString
@Getter
public class ExtractDocument {

    private String docId;

    private String name;

    private Path path;

    private List<DocumentLine> lines;

    private List<PassageDocument> passages;

    public ExtractDocument(String docId, String name, Path path) {
        this.docId = docId;
        this.name = name;
        this.path = path;
        this.lines = new ArrayList<>();
        this.passages = new ArrayList<>();
    }

    /**
     * 문자 데이터 등록
     */
    public void addText(String text) {
        if (!text.trim().isBlank()) {
            this.lines.add(DocumentLine.builder()
                    .type(DocumentLine.LineType.TEXT)
                    .content(text)
                    .build());
        }
    }

    /**
     * 표 데이터 등록
     */
    public void addTable(String table) {
        if (!table.trim().isBlank()) {
            this.lines.add(DocumentLine.builder()
                    .type(DocumentLine.LineType.TABLE)
                    .content(table)
                    .build());
        }
    }

    /**
     * 이미지 데이터 등록
     */
    public void addImage(String text) {
        if (!text.trim().isBlank()) {
            this.lines.add(DocumentLine.builder()
                    .type(DocumentLine.LineType.IMAGE)
                    .content(text)
                    .build());
        }
    }

    /**
     * 패시지 분류
     * @param chunkPatternVo 분류 패턴
     */
    public void selectPassage(ChunkPatternVo chunkPatternVo) {

        this.passages = new ArrayList<>();

        List<PatternVo> patterns = chunkPatternVo.getPatterns();
        List<PatternVo> stopPatterns = chunkPatternVo.getStopPatterns();
        String[][] titleBuffers = new String[chunkPatternVo.getDepthSize()][patterns.size()];
        StringBuilder contentBuffer = new StringBuilder();

        for (String[] titleBuffer : titleBuffers) Arrays.fill(titleBuffer, "");

        for (DocumentLine line : this.lines) {
            switch (line.getType()) {
                case DocumentLine.LineType.TEXT -> {
                    // 중단 조건 확인
                    if (isStopPattern(line, stopPatterns)) break;

                    boolean isMatched = false;
                    for (int patternIndex = 0; patternIndex < patterns.size(); patternIndex++) {
                        PatternVo patternVo = patterns.get(patternIndex);

                        String prefix = patternVo.getPrefix();
                        Pattern pattern = Pattern.compile(prefix, Pattern.MULTILINE);
                        Matcher matcher = pattern.matcher(line.getContent());

                        if (matcher.find()) {
                            // 본문 버퍼 플러시
                            if (!contentBuffer.toString().trim().isBlank()) {
                                // 목록 저장
                                PassageDocument passageDocument = PassageDocument.of(
                                        this.docId, this.passages.size(), chunkPatternVo.getDepthSize(), titleBuffers, contentBuffer);

                                if (!passageDocument.getFullTitle().isBlank()) {
                                    this.passages.add(passageDocument);
                                }

                                // 본문 버퍼 초기화
                                contentBuffer = new StringBuilder();
                            }

                            // 타이틀 버퍼 정리
                            for (int depth = patternVo.getDepth(); depth < titleBuffers.length; depth++) {
                                int childPatternIndex = depth == patternVo.getDepth() ? patternIndex : 0;

                                while (childPatternIndex < titleBuffers[depth].length) {
                                    titleBuffers[depth][childPatternIndex] = "";
                                    childPatternIndex++;
                                }
                            }

                            // 타이틀 저장
                            String title = matcher.group().trim();
                            titleBuffers[patternVo.getDepth()][patternIndex] = title;
                            String content = line.getContent().replaceFirst(prefix, "").trim();

                            // 타이틀 라인에 본문 내용이 있는 경우
                            if (!content.isBlank()) contentBuffer.append("\n").append(content);

                            isMatched = true;
                            line.setPrefix(prefix);
                            break;
                        }
                    }

                    if (!isMatched) contentBuffer.append("\n").append(line.getContent());
                }
                case DocumentLine.LineType.TABLE, DocumentLine.LineType.IMAGE -> contentBuffer.append("\n").append(line.getContent());
            }
        }

        // 본문 버퍼 플러시
        if (!contentBuffer.toString().trim().isBlank()) {
            // 목록 저장
            PassageDocument passageDocument = PassageDocument.of(this.docId, this.passages.size(), chunkPatternVo.getDepthSize(), titleBuffers, contentBuffer);
            if (!passageDocument.getFullTitle().isBlank()) {
                this.passages.add(passageDocument);
            }
        }
    }

    /**
     * 탐색 중단 패턴 확인
     * @param line 문서 라인
     * @param stopPatterns 중단 패턴 목록
     * @return 중단 여부
     */
    private static boolean isStopPattern(DocumentLine line, List<PatternVo> stopPatterns) {
        for (PatternVo patternVo : stopPatterns) {
            String prefix = patternVo.getPrefix();
            Pattern pattern = Pattern.compile(prefix, Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(line.getContent());

            if (matcher.find()) return true;
        }

        return false;
    }
}
