package com.cryptomining.platform.service;

import com.cryptomining.platform.dto.NotificationDto;
import com.cryptomining.platform.entity.Notification;
import com.cryptomining.platform.entity.User;
import com.cryptomining.platform.exception.ResourceNotFoundException;
import com.cryptomining.platform.repository.NotificationRepository;
import com.cryptomining.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public List<NotificationDto> getNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
            .map(this::toDto).toList();
    }

    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        if (!notification.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Notification not found");
        }
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.findByUserIdAndIsReadFalse(userId)
            .forEach(n -> { n.setIsRead(true); notificationRepository.save(n); });
    }

    @Transactional
    public NotificationDto createNotification(Long userId, String title, String message,
            Notification.NotificationType type) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Notification notification = notificationRepository.save(Notification.builder()
            .user(user)
            .title(title)
            .message(message)
            .type(type)
            .build());
        return toDto(notification);
    }

    private NotificationDto toDto(Notification n) {
        return NotificationDto.builder()
            .id(n.getId())
            .title(n.getTitle())
            .message(n.getMessage())
            .type(n.getType().name())
            .isRead(n.getIsRead())
            .createdAt(n.getCreatedAt())
            .build();
    }
}
