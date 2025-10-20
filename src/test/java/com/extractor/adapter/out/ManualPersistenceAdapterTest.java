package com.extractor.adapter.out;

import com.extractor.adapter.out.repository.ManualDocumentRepository;
import com.extractor.application.port.ManualPersistencePort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
class ManualPersistenceAdapterTest {

    private final ManualPersistencePort manualPersistencePort;

    public ManualPersistenceAdapterTest(@Autowired ManualPersistencePort manualPersistencePort) {
        this.manualPersistencePort = manualPersistencePort;
    }

    @Test
    void getManualDocumentsTest() {
        manualPersistencePort.getManualDocumentsPort(3714L);
    }
}