package com.palpal.dealightbe.domain.notification.application.dto.response;

import java.util.List;

import com.palpal.dealightbe.domain.notification.domain.Notification;

public record NotificationsRes(
	List<NotificationRes> notificationsRes
) {
	public static NotificationsRes of(List<Notification> notificationList) {
		return new NotificationsRes(
			notificationList.stream()
				.map(NotificationRes::from)
				.toList()
		);
	}
}
