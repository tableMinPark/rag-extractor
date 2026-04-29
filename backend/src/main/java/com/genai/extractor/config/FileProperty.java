package com.genai.extractor.config;

import lombok.Getter;
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
}
