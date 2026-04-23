package com.genai.extractor.adapter.out;

import com.genai.common.repository.entity.CommonCodeEntity;
import com.genai.common.repository.CommonCodeRepository;
import com.genai.extractor.application.port.CommonCodePersistencePort;
import com.genai.extractor.domain.model.CommonCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommonCodePersistenceAdapter implements CommonCodePersistencePort {

    private final CommonCodeRepository commonCodeRepository;

    /**
     * 그룹 코드 기준 공통 코드 목록 조회
     *
     * @param codeGroup 그룹 코드
     * @return 공통 코드 목록
     */
    @Override
    public List<CommonCode> getComnCodesByCodeGroupPort(String codeGroup) {
        return commonCodeRepository.findComnCodeByCodeGroupOrderBySortOrder(codeGroup).stream()
                .map(CommonCodeEntity::toDomain)
                .toList();
    }
}
