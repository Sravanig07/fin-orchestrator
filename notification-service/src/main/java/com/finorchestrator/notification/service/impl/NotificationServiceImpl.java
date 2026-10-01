package com.finorchestrator.notification.service.impl;

import com.finorchestrator.common.exception.ResourceNotFoundException;
import com.finorchestrator.notification.dto.CreateReminderRequest;
import com.finorchestrator.notification.dto.NotificationDto;
import com.finorchestrator.notification.entity.Notification;
import com.finorchestrator.notification.repository.NotificationRepository;
import com.finorchestrator.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> getNotifications(Long userId, Boolean unreadOnly) {
        List<Notification> list = Boolean.TRUE.equals(unreadOnly)
                ? notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, false)
                : notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return list.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public NotificationDto createReminder(Long userId, CreateReminderRequest request) {
        log.info("Creating reminder for userId: {}, title: {}", userId, request.getTitle());

        Notification notif = Notification.builder()
                .userId(userId)
                .title(request.getTitle())
                .message(request.getMessage())
                .type("REMINDER")
                .priority(request.getPriority() != null ? request.getPriority().toUpperCase() : "NORMAL")
                .triggerTime(request.getTriggerTime() != null ? request.getTriggerTime() : Instant.now())
                .isRead(false)
                .build();

        notif = notificationRepository.save(notif);
        return mapToDto(notif);
    }

    @Override
    @Transactional
    public void cancelReminder(Long id, Long userId) {
        Notification notif = notificationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification/Reminder", id));
        notificationRepository.delete(notif);
        log.info("Cancelled reminder id: {} for userId: {}", id, userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long id, Long userId) {
        Notification notif = notificationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", id));
        notif.setIsRead(true);
        notif.setReadAt(Instant.now());
        notificationRepository.save(notif);
    }

    @Override
    @Transactional
    public NotificationDto createNotification(Long userId, String title, String message, String type, String priority) {
        Notification notif = Notification.builder()
                .userId(userId)
                .title(title)
                .message(message)
                .type(type)
                .priority(priority != null ? priority : "NORMAL")
                .triggerTime(Instant.now())
                .isRead(false)
                .build();

        notif = notificationRepository.save(notif);
        return mapToDto(notif);
    }

    private NotificationDto mapToDto(Notification n) {
        return NotificationDto.builder()
                .id(n.getId())
                .userId(n.getUserId())
                .title(n.getTitle())
                .message(n.getMessage())
                .type(n.getType())
                .priority(n.getPriority())
                .isRead(n.getIsRead())
                .readAt(n.getReadAt())
                .triggerTime(n.getTriggerTime())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
