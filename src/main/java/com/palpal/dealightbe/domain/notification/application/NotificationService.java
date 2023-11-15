package com.palpal.dealightbe.domain.notification.application;

import static com.palpal.dealightbe.global.error.ErrorCode.SSE_STREAM_ERROR;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.palpal.dealightbe.domain.member.domain.Member;
import com.palpal.dealightbe.domain.member.domain.RoleType;
import com.palpal.dealightbe.domain.notification.domain.EmitterRepository;
import com.palpal.dealightbe.domain.notification.domain.Notification;
import com.palpal.dealightbe.domain.notification.domain.NotificationRepository;
import com.palpal.dealightbe.domain.order.domain.Order;
import com.palpal.dealightbe.domain.order.domain.OrderStatus;
import com.palpal.dealightbe.domain.store.domain.Store;
import com.palpal.dealightbe.global.error.exception.BusinessException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {
	private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;

	private final EmitterRepository emitterRepository;
	private final NotificationRepository notificationRepository;

	private static String getEmitterId(Long id, RoleType userType) {
		return userType.getRole() + "_" + id + "_" + System.currentTimeMillis();
	}

	public SseEmitter subscribe(Long id, RoleType userType, String lastEventId) {

		String emitterId = getEmitterId(id, userType);

		SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
		emitterRepository.save(emitterId, emitter);

		emitter.onCompletion(() -> emitterRepository.deleteById(emitterId));
		emitter.onTimeout(() -> emitterRepository.deleteById(emitterId));
		sendEventToEmitter(emitter, emitterId, "EventStream Created. [" + userType + "Id=" + id + "]");

		if (!lastEventId.isEmpty()) {
			resendMissedEvents(id, userType.getRole(), lastEventId, emitter);
		}

		return emitter;
	}

	private void resendMissedEvents(Long id, String userType, String lastEventId, SseEmitter emitter) {
		Map<String, String> events = emitterRepository.findAllEventCacheStartWithId(userType + "_" + id);
		events.entrySet().stream()
			.filter(entry -> lastEventId.compareTo(entry.getKey()) < 0)
			.forEach(entry -> sendEventToEmitter(emitter, entry.getKey(), entry.getValue()));
	}

	private void sendEventToEmitter(SseEmitter emitter, String emitterId, String data) {
		try {
			emitter.send(SseEmitter.event().id(emitterId).name("sse").data(data));
		} catch (IOException exception) {
			emitterRepository.deleteById(emitterId);
			throw new BusinessException(SSE_STREAM_ERROR);
		}
	}

	@Transactional
	public void send(Member member, Store store, Order order, OrderStatus orderStatus) {
		// Notification 메시지 생성
		String message = Notification.createMessage(orderStatus, order);

		// 알림 대상에 따라 SseEmitter에 이벤트 전송
		switch (orderStatus) {
			case RECEIVED:
				// member가 주문을 했으므로, store에게 알림을 보냄
				sendNotification(store.getId(), message, "STORE");
				break;
			case CONFIRMED:
				// store가 주문을 확인했으므로, member에게 알림을 보냄
				sendNotification(member.getId(), message, "MEMBER");
				break;
			case COMPLETED:
				// 주문이 완료되었으므로, member와 store 모두에게 알림을 보냄
				sendNotification(member.getId(), message, "MEMBER");
				sendNotification(store.getId(), message, "STORE");
				break;
			case CANCELED:
				// 주문이 취소되었으므로, store에게 알림을 보냄
				sendNotification(store.getId(), message, "STORE");
				break;
		}

		// Notification 저장
		Notification notification = createNotification(member, store, order, orderStatus);
		notificationRepository.save(notification);
	}

	private void sendNotification(Long id, String message, String userType) {
		// userType과 id를 기반으로 emitter를 찾고 이벤트 전송
		String emitterKeyPrefix = userType + "_" + id;
		Map<String, SseEmitter> emitters = emitterRepository.findAllStartWithById(emitterKeyPrefix);
		emitters.forEach((key, emitter) -> {
			emitterRepository.saveEventCache(key, message);
			sendEventToEmitter(emitter, key, message);
		});
	}

	private Notification createNotification(Member member, Store store, Order order, OrderStatus type) {
		return Notification.builder()
			.member(member)
			.store(store)
			.order(order)
			.type(type)
			.build();
	}
}
