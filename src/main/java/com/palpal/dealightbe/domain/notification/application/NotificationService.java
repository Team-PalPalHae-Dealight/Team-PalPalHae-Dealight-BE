package com.palpal.dealightbe.domain.notification.application;

import static com.palpal.dealightbe.domain.notification.util.NotificationUtil.extractTimestampFromEventId;
import static com.palpal.dealightbe.domain.notification.util.NotificationUtil.getEmitterId;
import static com.palpal.dealightbe.domain.notification.util.NotificationUtil.getEventId;
import static com.palpal.dealightbe.domain.notification.util.NotificationUtil.getNotificationId;
import static com.palpal.dealightbe.global.error.ErrorCode.SSE_STREAM_ERROR;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.palpal.dealightbe.domain.member.domain.Member;
import com.palpal.dealightbe.domain.member.domain.MemberRepository;
import com.palpal.dealightbe.domain.member.domain.RoleType;
import com.palpal.dealightbe.domain.notification.application.dto.response.NotificationRes;
import com.palpal.dealightbe.domain.notification.application.dto.response.NotificationsRes;
import com.palpal.dealightbe.domain.notification.domain.EmitterRepository;
import com.palpal.dealightbe.domain.notification.domain.Notification;
import com.palpal.dealightbe.domain.notification.domain.NotificationRepository;
import com.palpal.dealightbe.domain.order.domain.Order;
import com.palpal.dealightbe.domain.order.domain.OrderStatus;
import com.palpal.dealightbe.domain.store.domain.Store;
import com.palpal.dealightbe.global.error.ErrorCode;
import com.palpal.dealightbe.global.error.exception.BusinessException;
import com.palpal.dealightbe.global.error.exception.EntityNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
	private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;
	private final EmitterRepository emitterRepository;
	private final NotificationRepository notificationRepository;
	private final MemberRepository memberRepository;

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

		// 알림 대상에 따라 SseEmitter에 이벤트 전송
		if (orderStatus == OrderStatus.RECEIVED || orderStatus == OrderStatus.CANCELED) {
			// Store용 Notification 객체 생성 및 저장
			Notification notification = createNotification(null, store, order, message);
			notificationRepository.save(notification);
			sendNotification(store.getId(), notification, "store");
			return;
		}

		if (orderStatus == OrderStatus.CONFIRMED || orderStatus == OrderStatus.COMPLETED) {
			// Member용 Notification 객체 생성 및 저장
			Notification notification = createNotification(member, null, order, message);
			notificationRepository.save(notification);
			sendNotification(member.getId(), notification, "member");
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
				log.warn("DELETE:DELETE:NOT_FOUND_MEMBER_BY_ID : {}", memberId);
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

	@Transactional
	public void readNotification(Long id) {
		Notification notification = notificationRepository.findById(id)
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_NOTIFICATION_BY_ID : {}", id);
				return new EntityNotFoundException(ErrorCode.NOT_FOUND_NOTIFICATION);
			});

		notification.markAsRead();
	}
}
