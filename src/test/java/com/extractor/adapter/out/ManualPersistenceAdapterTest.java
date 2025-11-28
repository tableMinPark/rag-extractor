package com.extractor.adapter.out;

import com.extractor.application.port.ManualReadPort;
import com.extractor.global.enums.ExtractType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class ManualPersistenceAdapterTest {

    private final ManualReadPort manualReadPort;

    public ManualPersistenceAdapterTest(@Autowired ManualReadPort manualReadPort) {
        this.manualReadPort = manualReadPort;
    }

    @Test
    void getManualDocumentsTest() {
        manualReadPort.getManualsPort(3714L, ExtractType.MARK_DOWN);
    }
}