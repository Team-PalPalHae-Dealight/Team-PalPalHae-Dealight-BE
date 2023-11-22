package com.palpal.dealightbe.domain.notification.application;

import static com.palpal.dealightbe.domain.notification.util.EventIdUtil.extractTimestampFromEventId;
import static com.palpal.dealightbe.global.error.ErrorCode.SSE_STREAM_ERROR;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.palpal.dealightbe.domain.member.domain.Member;
import com.palpal.dealightbe.domain.member.domain.RoleType;
import com.palpal.dealightbe.domain.notification.application.dto.response.NotificationRes;
import com.palpal.dealightbe.domain.notification.domain.EmitterRepository;
import com.palpal.dealightbe.domain.notification.domain.Notification;
import com.palpal.dealightbe.domain.notification.domain.NotificationRepository;
import com.palpal.dealightbe.domain.order.domain.Order;
import com.palpal.dealightbe.domain.order.domain.OrderStatus;
import com.palpal.dealightbe.domain.store.domain.Store;
import com.palpal.dealightbe.global.error.exception.BusinessException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
	private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;
	private final EmitterRepository emitterRepository;
	private final NotificationRepository notificationRepository;

	private String getEmitterId(Long id, RoleType userType) {
		return userType.getRole() + "_" + id + "_" + System.currentTimeMillis();
	}

	private String getEventId(Long id, String userType) {
		return userType + "_" + id + "_" + System.currentTimeMillis();
	}

	public SseEmitter subscribe(Long id, RoleType userType, String lastEventId) {

		String emitterId = getEmitterId(id, userType);

		SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

		emitter.onCompletion(() -> emitterRepository.deleteById(emitterId));
		emitter.onTimeout(() -> emitterRepository.deleteById(emitterId));

		emitterRepository.save(emitterId, emitter);
		String eventId = getEventId(id, userType.getRole());

		sendEventToEmitter(emitter, emitterId, eventId, "EventStream Created. [" + userType + "Id=" + id + "]");

		if (!lastEventId.isEmpty()) {
			resendMissedEvents(id, userType.getRole(), emitterId, lastEventId, emitter);
		}

		return emitter;
	}

	private void resendMissedEvents(Long id, String userType, String emitterId, String lastEventId,
		SseEmitter emitter) {

		long lastEventTimestamp = extractTimestampFromEventId(lastEventId);
		Map<String, Notification> events = emitterRepository.findAllEventCacheAfterTimestamp(userType + "_" + id,
			lastEventTimestamp);
		for (Map.Entry<String, Notification> eventEntry : events.entrySet()) {
			String eventId = eventEntry.getKey();
			Notification notification = eventEntry.getValue();
			sendEventToEmitter(emitter, emitterId, eventId, NotificationRes.from(notification));
		}
	}

	private void sendEventToEmitter(SseEmitter emitter, String emitterId, String eventId, Object data) {

		try {
			if (data instanceof NotificationRes) {
				emitter.send(SseEmitter.event()
					.id(eventId)
					.name("orderNotification")
					.data(data));
			} else {
				String jsonData = "{\"message\":\"" + data.toString() + "\"}";
				emitter.send(SseEmitter.event().id(eventId).name("orderNotification").data(jsonData));
			}
		} catch (IOException exception) {
			emitterRepository.deleteById(emitterId);
			throw new BusinessException(SSE_STREAM_ERROR);
		}
	}

	private void sendNotification(Long id, Notification notification, String userType) {
		String emitterKeyPrefix = userType + "_" + id;

		Map<String, SseEmitter> emitters = emitterRepository.findAllStartWithById(emitterKeyPrefix);

		String eventId = getEventId(id, userType);
		emitterRepository.saveEventCache(eventId, notification);

		emitters.forEach((emitterId, emitter) -> {
			sendEventToEmitter(emitter, emitterId, eventId, NotificationRes.from(notification));
		});
	}

	@Transactional
	public void send(Member member, Store store, Order order, OrderStatus orderStatus) {
		// Notification 메시지 생성
		String message = Notification.createMessage(orderStatus, order);

		// Notification 저장
		Notification notification = createNotification(member, store, order, message);
		notificationRepository.save(notification);

		// 알림 대상에 따라 SseEmitter에 이벤트 전송
		switch (orderStatus) {
			case RECEIVED ->
				// member가 주문을 했으므로, store에게 알림을 보냄
				sendNotification(store.getId(), notification, "store");
			case CONFIRMED ->
				// store가 주문을 확인했으므로, member에게 알림을 보냄
				sendNotification(member.getId(), notification, "member");
			case COMPLETED -> {
				// 주문이 완료되었으므로, member와 store 모두에게 알림을 보냄
				sendNotification(member.getId(), notification, "member");
				sendNotification(store.getId(), notification, "store");
			}
			case CANCELED -> {
				// 주문이 취소되었으므로, member와 store 모두에게 알림을 보냄
				sendNotification(store.getId(), notification, "store");
				sendNotification(member.getId(), notification, "member");
			}
		}
	}

	private Notification createNotification(Member member, Store store, Order order, String content) {
		return Notification.builder()
			.member(member)
			.store(store)
			.order(order)
			.content(content)
			.build();
	}

	public void deleteAll(Long memberId) {
		Member member = memberRepository.findMemberByProviderId(memberId)
			.orElseThrow(() -> {
				log.warn("PATCH:UPDATE:NOT_FOUND_MEMBER_BY_ID : {}", memberId);
				return new EntityNotFoundException(ErrorCode.NOT_FOUND_MEMBER);
			});

		RoleType role = member.getMemberRoles().get(0).getRole().getType();
		String id = getNotificationId(memberId, role);
		emitterRepository.deleteAllStartWithId(id);
		emitterRepository.deleteAllEventCacheStartWithId(id);
	}

	public NotificationsRes findAllByMemberId(Long memberId) {
		List<Notification> responses = new ArrayList<>(
			notificationRepository.findAllByMemberIdAndIsReadFalse(memberId));

		return NotificationsRes.of(responses);
	}

}
