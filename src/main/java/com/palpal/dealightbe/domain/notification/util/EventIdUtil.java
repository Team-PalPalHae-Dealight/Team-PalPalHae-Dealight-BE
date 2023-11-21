package com.palpal.dealightbe.domain.notification.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventIdUtil {

	public static long extractTimestampFromEventId(String eventId) {
		String[] parts = eventId.split("_");
		return Long.parseLong(parts[parts.length - 1]);
	}
}
