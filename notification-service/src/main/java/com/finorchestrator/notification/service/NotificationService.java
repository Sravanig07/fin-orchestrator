package com.finorchestrator.notification.service;

import com.finorchestrator.notification.dto.CreateReminderRequest;
import com.finorchestrator.notification.dto.NotificationDto;

import java.util.List;

public interface NotificationService {
    List<NotificationDto> getNotifications(Long userId, Boolean unreadOnly);
    NotificationDto createReminder(Long userId, CreateReminderRequest request);
    void cancelReminder(Long id, Long userId);
    void markAsRead(Long id, Long userId);
    NotificationDto createNotification(Long userId, String title, String message, String type, String priority);
}
