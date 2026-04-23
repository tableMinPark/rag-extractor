package com.genai.embed.config;

import com.genai.embed.config.properties.CollectionProperty;
import com.genai.embed.config.properties.EmbedProperty;
import com.genai.embed.config.properties.IndexerProperty;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration("embedWebClientConfig")
public class WebClientConfig {

    @Bean(name = "collectionWebClient")
    public WebClient collectionWebClient(CollectionProperty property) {
        return buildWebClient(property.getConnectTimeout(), property.getResponseTimeout(),
                property.getReadTimeout(), property.getWriteTimeout());
    }

    @Bean(name = "indexerWebClient")
    public WebClient indexerWebClient(IndexerProperty property) {
        return buildWebClient(property.getConnectTimeout(), property.getResponseTimeout(),
                property.getReadTimeout(), property.getWriteTimeout());
    }

    @Bean(name = "embedWebClient")
    public WebClient embedWebClient(EmbedProperty property) {
        return buildWebClient(property.getConnectTimeout(), property.getResponseTimeout(),
                property.getReadTimeout(), property.getWriteTimeout());
    }

    private WebClient buildWebClient(int connectTimeout, int responseTimeout, int readTimeout, int writeTimeout) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
                .responseTimeout(Duration.ofMillis(responseTimeout))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(writeTimeout, TimeUnit.MILLISECONDS)));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(100 * 1024 * 1024))
                        .build())
                .build();
    }
}
