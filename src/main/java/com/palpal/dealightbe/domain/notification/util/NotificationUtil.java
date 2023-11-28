package com.palpal.dealightbe.domain.notification.util;

import com.palpal.dealightbe.domain.member.domain.RoleType;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NotificationUtil {

	private static final String REDIS_CHANNEL_PREFIX = "notification:";

	public static long extractTimestampFromEventId(String eventId) {
		String[] parts = eventId.split("_");
		return Long.parseLong(parts[parts.length - 1]);
	}

	public static String getEmitterId(Long id, RoleType userType) {
		return userType.getRole() + "_" + id + "_" + System.currentTimeMillis();
	}

	public static String getEventId(Long id, String userType) {
		return userType + "_" + id + "_" + System.currentTimeMillis();
	}

	public static String getNotificationId(Long id, RoleType userType) {
		return userType + "_" + id;
	}

	public static String getChannelId(Long id, String userType) {
		return REDIS_CHANNEL_PREFIX + userType + "_" + id;
	}
}
