package com.palpal.dealightbe.domain.notification.presentation;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.palpal.dealightbe.domain.notification.application.NotificationService;
import com.palpal.dealightbe.domain.notification.application.dto.response.NotificationsRes;
import com.palpal.dealightbe.global.aop.ProviderId;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

	private static final String DEFAULT_PAGING_SIZE = "10";
	private static final String DEFAULT_SORTING = "createdAt";
	private final NotificationService notificationService;

	@GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	@ProviderId
	public SseEmitter subscribe(Long providerId,
		@RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") String lastEventId) {

		return notificationService.subscribe(providerId, lastEventId);
	}

	@GetMapping
	@ProviderId
	public ResponseEntity<NotificationsRes> getNotifications(
		Long providerId,
		@RequestParam(value = "page", defaultValue = "0") int page,
		@RequestParam(value = "size", defaultValue = DEFAULT_PAGING_SIZE) int size) {

		page = Math.max(page - 1, 0);
		Pageable pageable = PageRequest.of(page, size, Sort.by(DEFAULT_SORTING).descending());

		NotificationsRes notificationsRes = notificationService.findAllByProviderId(providerId, pageable);
		return ResponseEntity.ok(notificationsRes);
	}

	@PatchMapping("/{id}")
	public ResponseEntity<Void> readNotification(@PathVariable Long id) {
		notificationService.readNotification(id);
		return ResponseEntity.noContent().build();
	}
}
