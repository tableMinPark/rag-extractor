package com.document.extractor.adapter.out;

import com.document.extractor.adapter.out.entity.CommonCodeEntity;
import com.document.extractor.adapter.out.repository.CommonCodeRepository;
import com.document.extractor.application.port.CommonCodePersistencePort;
import com.document.extractor.domain.model.CommonCode;
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
        return commonCodeRepository.findByCodeGroup(codeGroup).stream()
                .map(CommonCodeEntity::toDomain)
                .toList();
    }
}
