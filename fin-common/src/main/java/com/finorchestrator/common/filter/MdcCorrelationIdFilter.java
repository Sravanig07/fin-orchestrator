package com.finorchestrator.common.filter;

import com.finorchestrator.common.security.SecurityHeaders;
import com.finorchestrator.common.security.UserContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MdcCorrelationIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String correlationId = request.getHeader(SecurityHeaders.CORRELATION_ID_HEADER);
        if (!StringUtils.hasText(correlationId)) {
            correlationId = UUID.randomUUID().toString();
        }

        MDC.put("correlationId", correlationId);
        response.setHeader(SecurityHeaders.CORRELATION_ID_HEADER, correlationId);

        String userIdHeader = request.getHeader(SecurityHeaders.USER_ID_HEADER);
        String userEmailHeader = request.getHeader(SecurityHeaders.USER_EMAIL_HEADER);
        String userRoleHeader = request.getHeader(SecurityHeaders.USER_ROLE_HEADER);

        Long userId = null;
        if (StringUtils.hasText(userIdHeader)) {
            try {
                userId = Long.parseLong(userIdHeader);
            } catch (NumberFormatException ignored) {}
        }

        UserContext.setContext(userId, userEmailHeader, userRoleHeader, correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
            MDC.remove("correlationId");
        }
    }
}
