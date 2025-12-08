package com.document.global.utils;

import com.document.extractor.application.enums.UpdateState;
import com.document.extractor.domain.model.Passage;
import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

class StringUtilTest {

    private static final Logger log = LoggerFactory.getLogger(StringUtilTest.class);

    @Test
    void compareLines() {
        List<Passage> previousPassages = List.of(Passage.builder()
                .content("1조 테스트")
                .build(), Passage.builder()
                .content("2조 테스트")
                .build(), Passage.builder()
                .content("3조 테스트")
                .build(), Passage.builder()
                .content("4조 테스트")
                .build());
        List<Passage> passages = List.of(Passage.builder()
                .content("1조 테스트")
                .build(), Passage.builder()
                .content("2조 테스트_수정")
                .build(), Passage.builder()
                .content("2조의1 테스트")
                .build(), Passage.builder()
                .content("3조 테스트")
                .build(), Passage.builder()
                .content("5조 테스트")
                .build());
        Patch<Passage> patches = DiffUtils.diff(previousPassages, passages);

        for (AbstractDelta<Passage> delta : patches.getDeltas()) {
            List<Passage> sourcePassages = delta.getSource().getLines();
            List<Passage> targetPassages = delta.getTarget().getLines();

            switch (delta.getType()) {
                case CHANGE, INSERT, DELETE -> {
                    sourcePassages.forEach(passage -> passage.setUpdateState(UpdateState.CHANGE));
                    targetPassages.forEach(passage -> passage.setUpdateState(UpdateState.CHANGE));
                }
            }
        }

        StringBuilder prePassageBuilder = new StringBuilder();
        for (Passage passage : previousPassages) {
            prePassageBuilder.append(passage.getUpdateState().name()).append(" | ").append(passage.getContent()).append("\n");
        }

        StringBuilder passageBuilder = new StringBuilder();
        for (Passage passage : passages) {
            passageBuilder.append(passage.getUpdateState().name()).append(" | ").append(passage.getContent()).append("\n");
        }
        log.info("\n##### PRE PASSAGES #####\n{}", prePassageBuilder.toString());
        log.info("\n##### NOW PASSAGES #####\n{}", passageBuilder.toString());
    }

    @Test
    void diff() {
        System.out.println(StringUtil.cosineSimilarity("패시지 2", "패시지 2 수정"));
        System.out.println(StringUtil.cosineSimilarity("패시지 2", "패시지 2-1"));
    }
}