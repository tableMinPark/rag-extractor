package com.document.extractor.application.usecase;

import com.document.extractor.application.command.GetMemberCommand;
import com.document.extractor.domain.model.Member;

public interface MemberUseCase {

    Member getMemberUseCase(GetMemberCommand command);
}
