package com.palpal.dealightbe.domain.notification.presentation;

import static com.palpal.dealightbe.domain.member.domain.RoleType.ROLE_MEMBER;
import static com.palpal.dealightbe.domain.member.domain.RoleType.ROLE_STORE;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.palpal.dealightbe.domain.notification.application.NotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

	private final NotificationService notificationService;

	@GetMapping(value = "/subscribe/member/{memberId}", produces = "text/event-stream")
	public SseEmitter subscribeMember(@PathVariable Long memberId,
		@RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") String lastEventId) {

		return notificationService.subscribe(memberId, ROLE_MEMBER, lastEventId);
	}

	@GetMapping(value = "/subscribe/store/{storeId}", produces = "text/event-stream")
	public SseEmitter subscribeStore(@PathVariable Long storeId,
		@RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") String lastEventId) {

		return notificationService.subscribe(storeId, ROLE_STORE, lastEventId);
	}
}
