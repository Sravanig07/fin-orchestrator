package com.finorchestrator.common.interceptor;

import com.finorchestrator.common.security.SecurityHeaders;
import com.finorchestrator.common.security.UserContext;
import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.UUID;

@Component
public class CorrelationIdClientInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        String correlationId = MDC.get("correlationId");
        if (!StringUtils.hasText(correlationId)) {
            correlationId = UserContext.getCorrelationId();
        }
        if (!StringUtils.hasText(correlationId)) {
            correlationId = UUID.randomUUID().toString();
        }

        request.getHeaders().add(SecurityHeaders.CORRELATION_ID_HEADER, correlationId);

        if (UserContext.getUserId() != null) {
            request.getHeaders().add(SecurityHeaders.USER_ID_HEADER, String.valueOf(UserContext.getUserId()));
        }
        if (StringUtils.hasText(UserContext.getUserEmail())) {
            request.getHeaders().add(SecurityHeaders.USER_EMAIL_HEADER, UserContext.getUserEmail());
        }
        if (StringUtils.hasText(UserContext.getUserRole())) {
            request.getHeaders().add(SecurityHeaders.USER_ROLE_HEADER, UserContext.getUserRole());
        }

        return execution.execute(request, body);
    }
}
