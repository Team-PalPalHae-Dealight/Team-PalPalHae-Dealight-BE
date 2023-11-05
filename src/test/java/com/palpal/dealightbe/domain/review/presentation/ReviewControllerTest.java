package com.palpal.dealightbe.domain.review.presentation;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.palpal.dealightbe.domain.review.application.ReviewService;
import com.palpal.dealightbe.domain.review.application.dto.request.ReviewCreateReq;
import com.palpal.dealightbe.domain.review.application.dto.response.ReviewCreateRes;

@AutoConfigureRestDocs
@WebMvcTest(
	value = ReviewController.class,
	excludeAutoConfiguration = {SecurityAutoConfiguration.class, OAuth2ClientAutoConfiguration.class}
)
class ReviewControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private ReviewService reviewService;

	@Nested
	@DisplayName("<리뷰 생성>")
	class createTest {
		String createApiPath = "/api/reviews/orders";

		ReviewCreateReq reviewCreateReq = new ReviewCreateReq(List.of("사장님이 친절해요", "가격이 저렴해요"));

		ReviewCreateRes reviewCreateRes = new ReviewCreateRes(List.of(1L, 2L));

		@Test
		@DisplayName("성공 - 리뷰를 등록한다")
		void create_success() throws Exception {
			// given
			given(reviewService.create(anyLong(), any(ReviewCreateReq.class), any()))
				.willReturn(reviewCreateRes);

			// when
			// then
			mockMvc.perform(
					post(createApiPath, 1L)
						.with(csrf().asHeader())
						.with(user("username").roles("MEMBER"))
						.header("Authorization", "Bearer {ACCESS_TOKEN}")
						.content(objectMapper.writeValueAsString(reviewCreateReq))
						.contentType(APPLICATION_JSON)
						.param("id", "2")
				)
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.ids[0]").value(reviewCreateRes.ids().get(0)))
				.andExpect(jsonPath("$.ids[1]").value(reviewCreateRes.ids().get(1)))
				.andDo(document("review/review-create-success",
						preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint()),
						// pathParameters(
						// 	parameterWithName("memberProviderId").description("고객 카카오 토큰")
						// ),
						requestHeaders(
							headerWithName("Authorization").description("Access Token")
						),
						requestParameters(
							parameterWithName("id").description("리뷰 작성하고자 하는 주문 아이디")
						),
						requestFields(
							fieldWithPath("messages[]").type(JsonFieldType.ARRAY).description("선택한 리뷰 메시지")
						),
						responseFields(
							fieldWithPath("ids[]").type(JsonFieldType.ARRAY).description("등록된 리뷰 아이디들")
						)
					)
				);
		}

		@Test
		@DisplayName("실패 - 리뷰를 1개 이상 입력하지 않은 경우 예외가 발생한다")
		void create_fail() throws Exception {
			// given
			ReviewCreateReq emptyReq = new ReviewCreateReq(List.of());

			// when
			// then
			mockMvc.perform(
					post(createApiPath, 1)
						.with(csrf().asHeader())
						.with(user("username").roles("MEMBER"))
						.content(objectMapper.writeValueAsString(emptyReq))
						.contentType(APPLICATION_JSON)
						.param("id", "2")
				)
				.andDo(print())
				.andExpect(status().is4xxClientError())
				.andExpect(result -> {
					assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException);
				});
		}

	}
}
