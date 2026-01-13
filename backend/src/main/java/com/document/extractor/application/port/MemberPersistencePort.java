package com.document.extractor.application.port;

import com.document.extractor.domain.model.Member;

public interface MemberPersistencePort {

    Member getMemberPort(String name);
}
