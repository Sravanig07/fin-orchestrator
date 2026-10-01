package com.finorchestrator.notification.controller;

import com.finorchestrator.common.dto.ApiResponse;
import com.finorchestrator.common.security.SecurityHeaders;
import com.finorchestrator.common.security.UserContext;
import com.finorchestrator.notification.dto.CreateReminderRequest;
import com.finorchestrator.notification.dto.NotificationDto;
import com.finorchestrator.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationDto>>> getNotifications(
            @RequestHeader(value = SecurityHeaders.USER_ID_HEADER, required = false) Long headerUserId,
            @RequestParam(value = "unreadOnly", required = false) Boolean unreadOnly) {
        Long userId = resolveUserId(headerUserId);
        List<NotificationDto> list = notificationService.getNotifications(userId, unreadOnly);
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @PostMapping("/reminders")
    public ResponseEntity<ApiResponse<NotificationDto>> createReminder(
            @RequestHeader(value = SecurityHeaders.USER_ID_HEADER, required = false) Long headerUserId,
            @Valid @RequestBody CreateReminderRequest request) {
        Long userId = resolveUserId(headerUserId);
        NotificationDto created = notificationService.createReminder(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Reminder created successfully", created));
    }

    @DeleteMapping("/reminders/{id}")
    public ResponseEntity<ApiResponse<Void>> cancelReminder(
            @PathVariable("id") Long id,
            @RequestHeader(value = SecurityHeaders.USER_ID_HEADER, required = false) Long headerUserId) {
        Long userId = resolveUserId(headerUserId);
        notificationService.cancelReminder(id, userId);
        return ResponseEntity.ok(ApiResponse.ok("Reminder cancelled successfully", null));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable("id") Long id,
            @RequestHeader(value = SecurityHeaders.USER_ID_HEADER, required = false) Long headerUserId) {
        Long userId = resolveUserId(headerUserId);
        notificationService.markAsRead(id, userId);
        return ResponseEntity.ok(ApiResponse.ok("Marked as read", null));
    }

    private Long resolveUserId(Long headerUserId) {
        Long userId = headerUserId != null ? headerUserId : UserContext.getUserId();
        return userId != null ? userId : 1L;
    }
}
