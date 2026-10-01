package com.finorchestrator.common.config;

import com.finorchestrator.common.filter.MdcCorrelationIdFilter;
import com.finorchestrator.common.interceptor.CorrelationIdClientInterceptor;
import com.finorchestrator.common.security.JwtUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@AutoConfiguration
public class CommonAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MdcCorrelationIdFilter mdcCorrelationIdFilter() {
        return new MdcCorrelationIdFilter();
    }

    @Bean
    public FilterRegistrationBean<MdcCorrelationIdFilter> mdcCorrelationIdFilterRegistration(MdcCorrelationIdFilter filter) {
        FilterRegistrationBean<MdcCorrelationIdFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.addUrlPatterns("/*");
        return registration;
    }

    @Bean
    @ConditionalOnMissingBean
    public CorrelationIdClientInterceptor correlationIdClientInterceptor() {
        return new CorrelationIdClientInterceptor();
    }

    @Bean
    @ConditionalOnMissingBean
    public RestTemplate restTemplate(CorrelationIdClientInterceptor interceptor) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(Collections.singletonList(interceptor));
        return restTemplate;
    }

    @Bean
    @ConditionalOnMissingBean
    public RestClient restClient(CorrelationIdClientInterceptor interceptor) {
        return RestClient.builder()
                .requestInterceptor(interceptor)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtUtils jwtUtils() {
        return new JwtUtils("SuperSecretFinOrchestratorKeyForJwtGeneration2026!#$", 86400000);
    }
}
