package com.finorchestrator.common.security;

public final class UserContext {
    private static final ThreadLocal<Long> currentUserId = new ThreadLocal<>();
    private static final ThreadLocal<String> currentUserEmail = new ThreadLocal<>();
    private static final ThreadLocal<String> currentUserRole = new ThreadLocal<>();
    private static final ThreadLocal<String> currentCorrelationId = new ThreadLocal<>();

    private UserContext() {}

    public static void setContext(Long userId, String email, String role, String correlationId) {
        currentUserId.set(userId);
        currentUserEmail.set(email);
        currentUserRole.set(role);
        currentCorrelationId.set(correlationId);
    }

    public static Long getUserId() {
        return currentUserId.get();
    }

    public static String getUserEmail() {
        return currentUserEmail.get();
    }

    public static String getUserRole() {
        return currentUserRole.get();
    }

    public static String getCorrelationId() {
        return currentCorrelationId.get();
    }

    public static void clear() {
        currentUserId.remove();
        currentUserEmail.remove();
        currentUserRole.remove();
        currentCorrelationId.remove();
    }
}
