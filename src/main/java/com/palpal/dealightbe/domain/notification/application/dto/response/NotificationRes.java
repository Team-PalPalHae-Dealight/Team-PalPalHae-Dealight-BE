package com.palpal.dealightbe.domain.notification.application.dto.response;

import java.time.LocalDateTime;

import com.palpal.dealightbe.domain.notification.domain.Notification;

public record NotificationRes(
	Long id,
	String content,
	LocalDateTime createdAt,
	boolean isRead
) {
	public static NotificationRes from(Notification notification) {
		return new NotificationRes(
			notification.getId(),
			notification.getContent(),
			notification.getCreatedAt(),
			notification.isRead()
		);
	}
}
