package com.palpal.dealightbe.domain.notification.domain;

import static com.palpal.dealightbe.domain.notification.util.EventIdUtil.extractTimestampFromEventId;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Repository
public class EmitterRepository {

	public final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
	private final Map<String, Notification> eventCache = new ConcurrentHashMap<>();

	public SseEmitter save(String id, SseEmitter sseEmitter) {
		emitters.put(id, sseEmitter);
		return sseEmitter;
	}

	public void saveEventCache(String id, Notification event) {
		eventCache.put(id, event);
	}

	public Map<String, SseEmitter> findAllStartWithById(String id) {
		return emitters.entrySet().stream()
			.filter(entry -> entry.getKey().startsWith(id))
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	public Map<String, Notification> findAllEventCacheAfterTimestamp(String prefix, long lastEventTimestamp) {
		return eventCache.entrySet().stream()
			.filter(entry -> entry.getKey().startsWith(prefix))
			.filter(entry -> extractTimestampFromEventId(entry.getKey()) > lastEventTimestamp)
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	public void deleteAllStartWithId(String id) {
		emitters.entrySet()
			.removeIf(entry -> entry.getKey().startsWith(id));
	}

	public void deleteById(String id) {
		emitters.remove(id);
	}

	public void deleteAllEventCacheStartWithId(String id) {
		eventCache.keySet().removeIf(key -> key.startsWith(id));
	}
}
