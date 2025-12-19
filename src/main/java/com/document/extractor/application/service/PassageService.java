package com.document.extractor.application.service;

import com.document.extractor.application.command.GetPassageCommand;
import com.document.extractor.application.command.GetPassagesCommand;
import com.document.extractor.application.port.PassagePersistencePort;
import com.document.extractor.application.usecase.PassageUseCase;
import com.document.extractor.application.vo.PassageVo;
import com.document.extractor.application.wrapper.PageWrapper;
import com.document.extractor.domain.model.Passage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PassageService implements PassageUseCase {

    private final PassagePersistencePort passagePersistencePort;

    /**
     * 패시지 조회
     *
     * @param command 패시지 조회 Command
     * @return 패시지
     */
    @Transactional(readOnly = true)
    @Override
    public PassageVo getPassageUseCase(GetPassageCommand command) {
        return PassageVo.of(passagePersistencePort.getPassagePort(command.getPassageId()));
    }

    /**
     * 패시지 목록 조회
     *
     * @param command 패시지 목록 조회 Command
     * @return 패시지 목록
     */
    @Transactional(readOnly = true)
    @Override
    public PageWrapper<PassageVo> getPassagesUseCase(GetPassagesCommand command) {

        PageWrapper<Passage> passagePageWrapper = passagePersistencePort.getLatestPassagesPort(command.getPage(), command.getSize(), command.getSourceId());

        return PageWrapper.<PassageVo>builder()
                .data(passagePageWrapper.getData().stream().map(PassageVo::of).toList())
                .isLast(passagePageWrapper.isLast())
                .page(passagePageWrapper.getPage())
                .size(passagePageWrapper.getSize())
                .totalCount(passagePageWrapper.getTotalCount())
                .totalPages(passagePageWrapper.getTotalPages())
                .build();
    }
}