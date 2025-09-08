//package com.extractor.domain.model.law;
//
//import com.extractor.domain.model.PassageDocument;
//import com.extractor.domain.model.pattern.DocumentLine;
//import lombok.Builder;
//
//import java.util.List;
//import java.util.Map;
//
//public class LawPassageDocument extends PassageDocument {
//
//    private List<LawContent> lawContents;
//
//    private Map<Long, LawLink> lawLinkMap;
//
//    public LawPassageDocument(String docId, int depthSize, List<LawContent> lawContents) {
//        super(docId, -1, depthSize, "", new String[depthSize], "", 0, new String[depthSize][]);
//        this.lawContents = lawContents;
//    }
//
//    @Builder
//    public LawPassageDocument(String docId, int depthSize, List<DocumentLine> lines, int depth, String[][] titleBuffers) {
//        super(docId, depth, depthSize, "", new String[depthSize], "", 0, deepCopyTitleBuffers(titleBuffers));
//        this.lines = lines;
//    }
//}
