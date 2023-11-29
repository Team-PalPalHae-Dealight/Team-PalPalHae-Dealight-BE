package com.palpal.dealightbe.domain.notification.presentation;

import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Pageable;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.web.servlet.MockMvc;

import com.palpal.dealightbe.config.SecurityConfig;
import com.palpal.dealightbe.domain.notification.application.NotificationService;
import com.palpal.dealightbe.domain.notification.application.dto.response.NotificationRes;
import com.palpal.dealightbe.domain.notification.application.dto.response.NotificationsRes;

@WebMvcTest(value = NotificationController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class,
	OAuth2ClientAutoConfiguration.class}, excludeFilters = {
	@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)})
@AutoConfigureRestDocs
class NotificationControllerTest {

	@Autowired
	MockMvc mockMvc;

	@MockBean
	NotificationService notificationService;

	@Test
	@DisplayName("알림 상태 읽음으로 수정 성공")
	void readNotificationTest() throws Exception {
		// Given
		Long notificationId = 1L;
		doNothing().when(notificationService).readNotification(notificationId);

		// When & Then
		mockMvc.perform(patch("/api/notifications/{id}", notificationId)
				.header("Authorization", "Bearer some_access_token"))
			.andExpect(status().isNoContent())
			.andDo(print())
			.andDo(document("notifications/read-notification",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				pathParameters(
					parameterWithName("id").description("읽음으로 표시할 알림의 ID")
				),
				requestHeaders(
					headerWithName("Authorization").description("토큰")
				)
			));
	}

	@Test
	@DisplayName("알림 조회 성공")
	void getNotificationsTest() throws Exception {

		// Given
		List<NotificationRes> notificationResList = List.of(
			new NotificationRes(1L, "알림 내용", LocalDateTime.now(), false, "event1")
		);
		NotificationsRes notificationsRes = new NotificationsRes(notificationResList, false);

		given(notificationService.findAllByProviderId(any(), any(Pageable.class)))
			.willReturn(notificationsRes);

		System.out.println("notificationService 테스트 이름: " + notificationService.getClass().getName());

		// When & Then
		mockMvc.perform(
				RestDocumentationRequestBuilders.get("/api/notifications")
					.param("page", "0")
					.param("size", "10")
					.param("sort", "createdAt")
					.header("Authorization", "Bearer {ACCESS_TOKEN}")
					.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("notifications/get-notifications",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestParameters(
					parameterWithName("page").description("요청된 알림의 페이지 번호"),
					parameterWithName("size").description("페이지 당 알림의 수"),
					parameterWithName("sort").description("요청된 알림의 정렬 기준")
				),
				requestHeaders(
					headerWithName("Authorization").description("토큰")
				),
				responseFields(
					fieldWithPath("notificationsRes[].id").description("알림의 ID"),
					fieldWithPath("notificationsRes[].content").description("알림의 내용"),
					fieldWithPath("notificationsRes[].createdAt").description("알림 생성 날짜"),
					fieldWithPath("notificationsRes[].isRead").description("알림의 읽음 상태"),
					fieldWithPath("notificationsRes[].eventId").description("알림의 이벤트 ID"),
					fieldWithPath("hasNext").description("다음 페이지 존재 여부")
				)
			));
	}
}
