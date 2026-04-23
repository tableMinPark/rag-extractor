package com.genai.embed.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "engine.collection")
public class CollectionProperty {

    private int connectTimeout;
    private int responseTimeout;
    private int readTimeout;
    private int writeTimeout;
    private String host;
    private int port;

    public String getUrl() {
        StringBuilder url = new StringBuilder();
        if (!host.startsWith("http")) url.append("http://");
        url.append(host).append(":").append(port);
        return url.toString().trim();
    }
}
