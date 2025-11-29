package com.extractor.adapter.propery;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "env.file")
public class FileProperty {

    private String fileStorePath;

    private String tempDir;

    private SnfPath snfPath;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SnfPath {
        private String mac;
        private String windows;
        private String linux;
    }
}
