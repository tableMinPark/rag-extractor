package com.genai.extractor.application.service;

import com.genai.extractor.application.command.GetPassageCommand;
import com.genai.extractor.application.command.GetPassagesCommand;
import com.genai.extractor.application.port.PassagePersistencePort;
import com.genai.extractor.application.usecase.PassageUseCase;
import com.genai.extractor.application.vo.PassageVo;
import com.genai.global.wrapper.PageWrapper;
import com.genai.extractor.domain.model.Passage;
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
                .content(passagePageWrapper.getContent().stream().map(PassageVo::of).toList())
                .isLast(passagePageWrapper.isLast())
                .pageNo(passagePageWrapper.getPageNo())
                .pageSize(passagePageWrapper.getPageSize())
                .totalCount(passagePageWrapper.getTotalCount())
                .totalPages(passagePageWrapper.getTotalPages())
                .build();
    }

    /**
     * 총 패시지 수 조회
     *
     * @return 총 패시지 수
     */
    @Transactional(readOnly = true)
    @Override
    public long getPassageTotalCountUseCase() {
        return passagePersistencePort.getPassageTotalCountPort();
    }
}