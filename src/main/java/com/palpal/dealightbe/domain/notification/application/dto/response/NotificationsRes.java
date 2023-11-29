package com.palpal.dealightbe.domain.notification.application.dto.response;

import java.util.List;

import org.springframework.data.domain.Slice;

import com.palpal.dealightbe.domain.notification.domain.Notification;

public record NotificationsRes(
	List<NotificationRes> notificationsRes,

	boolean hasNext
) {

	public static NotificationsRes from(Slice<Notification> notifications) {
		List<NotificationRes> notificationResList = notifications.stream()
			.map(NotificationRes::from)
			.toList();

		return new NotificationsRes(notificationResList, notifications.hasNext());
	}
}
