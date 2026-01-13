package com.document.extractor.adapter.out;

import com.document.extractor.adapter.out.repository.MemberRepository;
import com.document.extractor.application.exception.NotFoundException;
import com.document.extractor.application.port.MemberPersistencePort;
import com.document.extractor.domain.model.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberPersistenceAdapter implements MemberPersistencePort {

    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    @Override
    public Member getMemberPort(String name) {
        return memberRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException("회원 정보"))
                .toDomain();
    }
}
