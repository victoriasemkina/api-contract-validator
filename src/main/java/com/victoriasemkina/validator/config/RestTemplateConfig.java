package com.victoriasemkina.validator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class RestTemplateConfig {

    @Value("${validator.http.connect-timeout:10000}")
    private int connectTimeout;

    @Value("${validator.http.read-timeout:30000}")
    private int readTimeout;

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);

        RestTemplate restTemplate = new RestTemplate(factory);
        log.info("RestTemplate configured: connect={}ms, read={}ms", connectTimeout, readTimeout);
        return restTemplate;
    }
}