package com.finorchestrator.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finorchestrator.common.dto.ApiResponse;
import com.finorchestrator.common.dto.ErrorResponse;
import com.finorchestrator.common.security.JwtUtils;
import com.finorchestrator.common.security.SecurityHeaders;
import com.finorchestrator.gateway.service.RateLimiterService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final RateLimiterService rateLimiterService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/actuator"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // 1. Rate Limiting Check
        String clientIp = getClientIp(request);
        if (!rateLimiterService.isAllowed(clientIp)) {
            sendError(response, HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMIT_EXCEEDED", "Too many requests. Please try again later.");
            return;
        }

        // 2. Check if path is public
        boolean isPublic = PUBLIC_PATHS.stream().anyMatch(path::startsWith);
        if (isPublic || request.getMethod().equalsIgnoreCase("OPTIONS")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extract and Validate Token
        String authHeader = request.getHeader("Authorization");
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            sendError(response, HttpStatus.UNAUTHORIZED, "MISSING_TOKEN", "Authorization token is missing or malformed");
            return;
        }

        String token = authHeader.substring(7).trim();
        if (!jwtUtils.validateToken(token)) {
            sendError(response, HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "Token is invalid or has expired");
            return;
        }

        Long userId = jwtUtils.extractUserId(token);
        String email = jwtUtils.extractEmail(token);
        String role = jwtUtils.extractRole(token);

        // 4. Wrap request to inject authentication headers for downstream services
        HeaderMapRequestWrapper requestWrapper = new HeaderMapRequestWrapper(request);
        if (userId != null) {
            requestWrapper.addHeader(SecurityHeaders.USER_ID_HEADER, String.valueOf(userId));
        }
        if (email != null) {
            requestWrapper.addHeader(SecurityHeaders.USER_EMAIL_HEADER, email);
        }
        if (role != null) {
            requestWrapper.addHeader(SecurityHeaders.USER_ROLE_HEADER, role);
        }

        filterChain.doFilter(requestWrapper, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }

    private void sendError(HttpServletResponse response, HttpStatus status, String code, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse error = ErrorResponse.builder()
                .errorCode(code)
                .message(message)
                .correlationId(MDC.get("correlationId"))
                .timestamp(Instant.now())
                .build();

        ApiResponse<Void> apiResponse = ApiResponse.failure(message, error);
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }

    public static class HeaderMapRequestWrapper extends HttpServletRequestWrapper {
        private final Map<String, String> customHeaders = new HashMap<>();

        public HeaderMapRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        public void addHeader(String name, String value) {
            customHeaders.put(name, value);
        }

        @Override
        public String getHeader(String name) {
            String headerValue = customHeaders.get(name);
            if (headerValue != null) {
                return headerValue;
            }
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            Set<String> set = new HashSet<>(customHeaders.keySet());
            Enumeration<String> e = ((HttpServletRequest) getRequest()).getHeaderNames();
            while (e.hasMoreElements()) {
                set.add(e.nextElement());
            }
            return Collections.enumeration(set);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            String value = customHeaders.get(name);
            if (value != null) {
                return Collections.enumeration(Collections.singletonList(value));
            }
            return super.getHeaders(name);
        }
    }
}
