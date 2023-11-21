package com.palpal.dealightbe.domain.notification.util;

public final class EventIdUtil {

	private EventIdUtil() {
	}

	public static long extractTimestampFromEventId(String eventId) {
		String[] parts = eventId.split("_");
		return Long.parseLong(parts[parts.length - 1]);
	}
}
