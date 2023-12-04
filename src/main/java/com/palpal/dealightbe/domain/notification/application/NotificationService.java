package com.palpal.dealightbe.domain.notification.application;

import static com.palpal.dealightbe.domain.notification.util.NotificationUtil.extractTimestampFromEventId;
import static com.palpal.dealightbe.domain.notification.util.NotificationUtil.getChannelId;
import static com.palpal.dealightbe.domain.notification.util.NotificationUtil.getEmitterId;
import static com.palpal.dealightbe.domain.notification.util.NotificationUtil.getEventId;
import static com.palpal.dealightbe.domain.notification.util.NotificationUtil.getNotificationId;
import static com.palpal.dealightbe.global.error.ErrorCode.SSE_STREAM_ERROR;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.palpal.dealightbe.domain.member.domain.Member;
import com.palpal.dealightbe.domain.member.domain.MemberRepository;
import com.palpal.dealightbe.domain.member.domain.RoleType;
import com.palpal.dealightbe.domain.notification.application.dto.NotificationUserInfo;
import com.palpal.dealightbe.domain.notification.application.dto.response.NotificationRes;
import com.palpal.dealightbe.domain.notification.application.dto.response.NotificationsRes;
import com.palpal.dealightbe.domain.notification.domain.EmitterRepository;
import com.palpal.dealightbe.domain.notification.domain.Notification;
import com.palpal.dealightbe.domain.notification.domain.NotificationRepository;
import com.palpal.dealightbe.domain.order.domain.Order;
import com.palpal.dealightbe.domain.order.domain.OrderStatus;
import com.palpal.dealightbe.domain.store.domain.Store;
import com.palpal.dealightbe.domain.store.domain.StoreRepository;
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

	private static final String DEFAULT_SCHEDULING_TIME = "0 0 2 ? * TUE";  //매주 화요일 새벽 2시에 진행
	private final NotificationRepository notificationRepository;
	private final MemberRepository memberRepository;
	private final StoreRepository storeRepository;
	private final EmitterRepository emitterRepository;

	private final StringRedisTemplate stringRedisTemplate;

	private final RedisMessageListenerContainer redisMessageListenerContainer;
	private final ObjectMapper objectMapper;

	public SseEmitter subscribe(Long providerId, String lastEventId) {

		NotificationUserInfo notificationUserInfo = findNotificationRoleByProviderId(providerId);

		Long id = notificationUserInfo.id();
		RoleType userType = notificationUserInfo.role();

		String emitterId = getEmitterId(id, userType);
		String eventId = getEventId(id, userType.getRole());

		SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
		emitterRepository.save(emitterId, emitter);

		sendEventToEmitter(emitter, emitterId, eventId, "EventStream Created. [" + userType + "Id=" + id + "]");

		if (!lastEventId.isEmpty()) {
			resendMissedEvents(id, userType.getRole(), emitterId, lastEventId, emitter);
		}

		final MessageListener messageListener = (message, pattern) -> {
			final NotificationRes notificationResponse = serialize(message);
			sendEventToEmitter(emitter, emitterId, notificationResponse.eventId(), notificationResponse);
		};

		String channelId = getChannelId(id, userType.getRole());

		this.redisMessageListenerContainer.addMessageListener(messageListener, ChannelTopic.of(channelId));
		checkEmitterStatus(emitter, emitterId, messageListener);
		return emitter;
	}

	private NotificationUserInfo findNotificationRoleByProviderId(Long providerId) {

		Member member = memberRepository.findMemberWithRolesAndRoleByProviderId(providerId)
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_MEMBER_BY_ID : {}", providerId);
				return new EntityNotFoundException(ErrorCode.NOT_FOUND_MEMBER);
			});

		Long id = member.getId();
		RoleType userType = member.getMemberRoles().get(0).getRole().getType();

		if (userType == RoleType.ROLE_STORE) {
			Store store = storeRepository.findByMemberProviderId(providerId)
				.orElseThrow(() -> {
					log.warn("GET:READ:NOT_FOUND_STORE_BY_PROVIDER_ID : {}", providerId);
					return new EntityNotFoundException(ErrorCode.NOT_FOUND_STORE);
				});
			id = store.getId();
			return new NotificationUserInfo(id, RoleType.ROLE_STORE);
		}

		return new NotificationUserInfo(id, RoleType.ROLE_MEMBER);
	}

	private NotificationRes serialize(final Message message) {
		try {
			String json = new String(message.getBody(), StandardCharsets.UTF_8);
			log.info("JSON received: {}", json);
			return objectMapper.readValue(json, NotificationRes.class);
		} catch (IOException e) {
			throw new BusinessException(ErrorCode.INVALID_REDIS_MESSAGE_FORMAT);
		}
	}

	private void checkEmitterStatus(final SseEmitter emitter, final String emitterId,
		final MessageListener messageListener) {
		emitter.onCompletion(() -> {
			emitterRepository.deleteById(emitterId);
			this.redisMessageListenerContainer.removeMessageListener(messageListener);
		});
		emitter.onTimeout(() -> {
			emitterRepository.deleteById(emitterId);
			this.redisMessageListenerContainer.removeMessageListener(messageListener);
		});
	}

	private void resendMissedEvents(Long id, String userType, String emitterId, String lastEventId,
		SseEmitter emitter) {

		long lastEventTimestamp = extractTimestampFromEventId(lastEventId);
		Map<String, Notification> events = emitterRepository.findAllEventCacheAfterTimestamp(userType + "_" + id,
			lastEventTimestamp);
		for (Map.Entry<String, Notification> eventEntry : events.entrySet()) {
			String eventId = eventEntry.getKey();
			Notification notification = eventEntry.getValue();
			sendEventToEmitter(emitter, emitterId, eventId, NotificationRes.from(notification, eventId));
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

		String channelId = getChannelId(id, userType);
		String eventId = getEventId(id, userType);

		NotificationRes notificationRes = NotificationRes.from(notification, eventId);
		emitterRepository.saveEventCache(eventId, notification);

		try {
			String notificationJson = objectMapper.writeValueAsString(notificationRes);

			log.info("Generated JSON for Redis: {}", notificationJson);

			stringRedisTemplate.convertAndSend(channelId, notificationJson);

			log.info("Notification sent to Redis channel {}: {}", channelId, notificationJson);
		} catch (JsonProcessingException e) {
			log.error("Failed to serialize NotificationRes", e);
			throw new BusinessException(ErrorCode.JSON_PARSING_ERROR);
		}
	}

	@Transactional
	public void send(Member member, Store store, Order order, OrderStatus orderStatus) {

		String message = Notification.createMessage(orderStatus, order);

		if (orderStatus == OrderStatus.CONFIRMED || orderStatus == OrderStatus.CANCELED) {
			// Store용 Notification 객체 생성 및 저장
			Notification notification = createNotification(null, store, order, message);
			notificationRepository.save(notification);
			sendNotification(store.getId(), notification, "store");
			return;
		}

		if (orderStatus == OrderStatus.RECEIVED || orderStatus == OrderStatus.COMPLETED) {
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

	public void deleteAll(Long providerId) {

		NotificationUserInfo notificationUserInfo = findNotificationRoleByProviderId(providerId);
		Long id = notificationUserInfo.id();
		RoleType role = notificationUserInfo.role();

		String notificationId = getNotificationId(id, role);
		emitterRepository.deleteAllStartWithId(notificationId);
		emitterRepository.deleteAllEventCacheStartWithId(notificationId);
	}

	public NotificationsRes findAllByProviderId(Long providerId, Pageable pageable) {
		NotificationUserInfo notificationUserInfo = findNotificationRoleByProviderId(providerId);
		Long id = notificationUserInfo.id();
		RoleType role = notificationUserInfo.role();

		Slice<Notification> notifications;

		if (role == RoleType.ROLE_STORE) {
			notifications = notificationRepository.findAllByStoreIdAndIsReadFalse(id, pageable);
		} else if (role == RoleType.ROLE_MEMBER) {
			notifications = notificationRepository.findAllByMemberIdAndIsReadFalse(id, pageable);
		} else {
			notifications = new SliceImpl<>(Collections.emptyList());
		}

		return NotificationsRes.from(notifications);
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
