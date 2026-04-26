package com.genai.search.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "engine.reranker")
public class RerankerProperty {

    private int connectTimeout;
    private int responseTimeout;
    private int readTimeout;
    private int writeTimeout;
    private String host;
    private int port;
    private String path;

    public String getUrl() {
        StringBuilder url = new StringBuilder();
        if (!host.startsWith("http")) url.append("http://");
        url.append(host).append(":").append(port);
        url.append(path.startsWith("/") ? "" : "/").append(path);
        return url.toString().trim();
    }
}
