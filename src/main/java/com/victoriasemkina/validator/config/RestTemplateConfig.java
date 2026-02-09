package com.victoriasemkina.validator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        // Увеличиваем таймауты для больших ответов
        factory.setConnectTimeout(10000);  // 10 сек на подключение
        factory.setReadTimeout(30000);     // 30 сек на чтение

        RestTemplate restTemplate = new RestTemplate(factory);
        log.info("RestTemplate configured with timeouts: connect=10s, read=30s");
        return restTemplate;
    }
}