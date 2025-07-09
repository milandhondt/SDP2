package dto;

import java.time.LocalDateTime;

public record NotificationDTO(int id, LocalDateTime time, String message, boolean isRead) {
}
