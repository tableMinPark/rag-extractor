package com.document.extractor.application.service;

import com.document.extractor.application.command.GetMemberCommand;
import com.document.extractor.application.port.MemberPersistencePort;
import com.document.extractor.application.usecase.MemberUseCase;
import com.document.extractor.domain.model.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService implements MemberUseCase {

    private final MemberPersistencePort memberPersistencePort;

    @Transactional(readOnly = true)
    @Override
    public Member getMemberUseCase(GetMemberCommand command) {
        return memberPersistencePort.getMemberPort(command.getName());
    }
}
