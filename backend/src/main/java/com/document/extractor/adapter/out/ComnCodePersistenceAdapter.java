package com.document.extractor.adapter.out;

import com.document.extractor.adapter.out.entity.ComnCodeEntity;
import com.document.extractor.adapter.out.repository.ComnCodeRepository;
import com.document.extractor.application.port.ComnCodePersistencePort;
import com.document.extractor.domain.model.ComnCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ComnCodePersistenceAdapter implements ComnCodePersistencePort {

    private final ComnCodeRepository comnCodeRepository;

    /**
     * 그룹 코드 기준 공통 코드 목록 조회
     *
     * @param codeGroup 그룹 코드
     * @return 공통 코드 목록
     */
    @Override
    public List<ComnCode> getComnCodesByCodeGroupPort(String codeGroup) {
        return comnCodeRepository.findByCodeGroup(codeGroup).stream()
                .map(ComnCodeEntity::toDomain)
                .toList();
    }
}
