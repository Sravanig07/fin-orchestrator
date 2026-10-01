package com.finorchestrator.gateway.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

@RestController
public class GatewayProxyController {

    private static final Logger log = LoggerFactory.getLogger(GatewayProxyController.class);

    private final Map<String, String> serviceRouteMap;
    private final RestTemplate restTemplate;

    public GatewayProxyController(
            @Value("${services.user-service:http://localhost:8081}") String userServiceUrl,
            @Value("${services.finance-service:http://localhost:8082}") String financeServiceUrl,
            @Value("${services.contract-service:http://localhost:8083}") String contractServiceUrl,
            @Value("${services.document-service:http://localhost:8084}") String documentServiceUrl,
            @Value("${services.risk-service:http://localhost:8085}") String riskServiceUrl,
            @Value("${services.notification-service:http://localhost:8086}") String notificationServiceUrl,
            @Value("${services.audit-service:http://localhost:8087}") String auditServiceUrl,
            @Value("${services.ai-orchestrator:http://localhost:8088}") String orchestratorServiceUrl) {

        this.serviceRouteMap = Map.of(
                "auth", userServiceUrl,
                "users", userServiceUrl,
                "finance", financeServiceUrl,
                "contracts", contractServiceUrl,
                "documents", documentServiceUrl,
                "risk", riskServiceUrl,
                "notifications", notificationServiceUrl,
                "audit", auditServiceUrl,
                "orchestrator", orchestratorServiceUrl
        );

        this.restTemplate = new RestTemplate();
        // Do not throw on 4xx/5xx from downstream; pass downstream response directly back to client
        this.restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) {
                return false;
            }
        });
    }

    @RequestMapping("/api/v1/**")
    public ResponseEntity<byte[]> proxy(HttpServletRequest request, HttpMethod method) throws IOException {
        String uri = request.getRequestURI(); // e.g. /api/v1/finance/accounts
        String queryString = request.getQueryString();

        // Extract segment after /api/v1/
        String pathWithoutPrefix = uri.substring("/api/v1/".length());
        int slashIdx = pathWithoutPrefix.indexOf('/');
        String serviceKey = slashIdx != -1 ? pathWithoutPrefix.substring(0, slashIdx) : pathWithoutPrefix;

        String targetBaseUrl = serviceRouteMap.get(serviceKey);
        if (targetBaseUrl == null) {
            log.warn("No downstream service registered for key: {}", serviceKey);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(String.format("{\"error\": \"No route found for %s\"}", serviceKey).getBytes());
        }

        String targetUrl = targetBaseUrl + uri + (queryString != null ? "?" + queryString : "");
        log.debug("Routing [{}] {} -> {}", method, uri, targetUrl);

        // Copy incoming headers
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            if (!name.equalsIgnoreCase(HttpHeaders.HOST) && !name.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH)) {
                headers.addAll(name, Collections.list(request.getHeaders(name)));
            }
        }

        byte[] body = StreamUtils.copyToByteArray(request.getInputStream());
        HttpEntity<byte[]> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(URI.create(targetUrl), method, entity, byte[].class);

            HttpHeaders responseHeaders = new HttpHeaders();
            response.getHeaders().forEach((headerName, headerValues) -> {
                if (!headerName.equalsIgnoreCase(HttpHeaders.TRANSFER_ENCODING)) {
                    responseHeaders.addAll(headerName, headerValues);
                }
            });

            return new ResponseEntity<>(response.getBody(), responseHeaders, response.getStatusCode());
        } catch (Exception e) {
            log.error("Failed to forward request to {}: {}", targetUrl, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(String.format("{\"error\": \"Downstream service unavailable: %s\"}", e.getMessage()).getBytes());
        }
    }
}
