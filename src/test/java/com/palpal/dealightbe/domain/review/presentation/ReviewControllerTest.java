package com.palpal.dealightbe.domain.review.presentation;

import static com.palpal.dealightbe.global.error.ErrorCode.ALREADY_EXIST_REVIEW;
import static com.palpal.dealightbe.global.error.ErrorCode.ILLEGAL_REVIEW_REQUEST;
import static com.palpal.dealightbe.global.error.ErrorCode.UNAUTHORIZED_REQUEST;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
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
import com.palpal.dealightbe.domain.review.application.dto.response.ReviewRes;
import com.palpal.dealightbe.domain.review.application.dto.response.StoreReviewRes;
import com.palpal.dealightbe.domain.review.application.dto.response.StoreReviewsRes;
import com.palpal.dealightbe.global.error.exception.BusinessException;

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
						.header("Authorization", "Bearer {ACCESS_TOKEN}")
						.content(objectMapper.writeValueAsString(emptyReq))
						.contentType(APPLICATION_JSON)
						.param("id", "2")
				)
				.andDo(print())
				.andExpect(status().is4xxClientError())
				.andExpect(result -> {
					assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException);
				})
				.andDo(document("review/review-create-fail-empty",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestHeaders(
						headerWithName("Authorization").description("Access Token")
					),
					responseFields(
						fieldWithPath("timestamp").type(STRING).description("예외 발생 시간"),
						fieldWithPath("code").type(STRING).description("오류 코드"),
						fieldWithPath("errors").type(ARRAY).description("오류 목록"),
						fieldWithPath("errors[].field").type(STRING).description("잘못 입력된 필드"),
						fieldWithPath("errors[].value").type(STRING).description("입력된 값"),
						fieldWithPath("errors[].reason").type(STRING).description("원인"),
						fieldWithPath("message").type(STRING).description("오류 메시지")
					)
				));
		}

		@Test
		@DisplayName("실패 - 완료된 주문에 대해서만 리뷰를 작성할 수 있다")
		void create_fail_not_completed() throws Exception {
			// given
			given(reviewService.create(any(), eq(reviewCreateReq), any()))
				.willThrow(new BusinessException(ILLEGAL_REVIEW_REQUEST));

			// when
			// then
			mockMvc.perform(
					post(createApiPath, 1)
						.with(csrf().asHeader())
						.with(user("username").roles("MEMBER"))
						.header("Authorization", "Bearer {ACCESS_TOKEN}")
						.content(objectMapper.writeValueAsString(reviewCreateReq))
						.contentType(APPLICATION_JSON)
						.param("id", "2")
				)
				.andDo(print())
				.andExpect(status().is4xxClientError())
				.andExpect(result -> {
					assertTrue(result.getResolvedException() instanceof BusinessException);
				})
				.andDo(document("review/review-create-fail-not-completed",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestHeaders(
						headerWithName("Authorization").description("Access Token")
					),
					responseFields(
						fieldWithPath("timestamp").type(STRING).description("예외 발생 시간"),
						fieldWithPath("code").type(STRING).description("오류 코드"),
						fieldWithPath("errors").type(ARRAY).description("오류 목록"),
						fieldWithPath("message").type(STRING).description("오류 메시지")
					)
				));
		}

		@Test
		@DisplayName("실패 - 이미 리뷰가 작성된 주문에 대해 추가로 작성할 수 없다.")
		void create_fail_already_exists() throws Exception {
			// given
			given(reviewService.create(eq(1L), any(ReviewCreateReq.class), any()))
				.willThrow(new BusinessException(ALREADY_EXIST_REVIEW));

			// when
			// then
			mockMvc.perform(
					post(createApiPath)
						.with(csrf().asHeader())
						.with(user("username").roles("MEMBER"))
						.header("Authorization", "Bearer {ACCESS_TOKEN}")
						.content(objectMapper.writeValueAsString(reviewCreateReq))
						.contentType(APPLICATION_JSON)
						.param("id", String.valueOf(1L))
				)
				.andDo(print())
				.andExpect(status().is4xxClientError())
				.andExpect(result -> {
					assertTrue(result.getResolvedException() instanceof BusinessException);
				})
				.andDo(document("review/review-create-fail-already-exists",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestHeaders(
						headerWithName("Authorization").description("Access Token")
					),
					responseFields(
						fieldWithPath("timestamp").type(STRING).description("예외 발생 시간"),
						fieldWithPath("code").type(STRING).description("오류 코드"),
						fieldWithPath("errors").type(ARRAY).description("오류 목록"),
						fieldWithPath("message").type(STRING).description("오류 메시지")
					)
				));
		}

		@Nested
		@DisplayName("<업체별 리뷰 조회>")
		class findByStoreIdTest {
			String findByStoreIdPath = "/api/reviews/stores";

			Long storeId = 1L;

			StoreReviewsRes storeReviewsRes = new StoreReviewsRes(storeId,
				List.of(new StoreReviewRes("사장님이 친절해요", 2), new StoreReviewRes("가격이 저렴해요", 4)));

			@Test
			@DisplayName("성공 - 고객이 업체별 리뷰 통계를 확인한다.")
			void findByStoreId_success() throws Exception {
				// given
				given(reviewService.findByStoreId(anyLong()))
					.willReturn(storeReviewsRes);

				// when
				// then
				mockMvc.perform(
						get(findByStoreIdPath + "/{id}", "1")
							.with(user("tester"))
					)
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.storeId").value(storeId))
					.andExpect(jsonPath("$.reviews[0].content").value(storeReviewsRes.reviews().get(0).content()))
					.andExpect(jsonPath("$.reviews[0].count").value(storeReviewsRes.reviews().get(0).count()))
					.andExpect(jsonPath("$.reviews[1].content").value(storeReviewsRes.reviews().get(1).content()))
					.andExpect(jsonPath("$.reviews[1].count").value(storeReviewsRes.reviews().get(1).count()))
					.andDo(document("review/review-find-by-store-id-success",
							preprocessRequest(prettyPrint()),
							preprocessResponse(prettyPrint()),
							pathParameters(
								parameterWithName("id").description("업체 아이디")
							),
							responseFields(
								fieldWithPath("storeId").type(JsonFieldType.NUMBER).description("업체 아이디"),
								fieldWithPath("reviews[]").type(JsonFieldType.ARRAY).description("각 리뷰별 메시지와 개수"),
								fieldWithPath("reviews[].content").type(JsonFieldType.STRING).description("리뷰 메시지"),
								fieldWithPath("reviews[].count").type(JsonFieldType.NUMBER).description("개수")
							)
						)
					);
			}

			@Test
			@DisplayName("성공 - 업체가 본인의 리뷰 통계를 조회한다.")
			void findByStoreOwnerProviderId_success() throws Exception {
				// given
				given(reviewService.findByStoreOwnerProviderId(any()))
					.willReturn(storeReviewsRes);

				// when
				// then
				mockMvc.perform(
						get(findByStoreIdPath)
							.with(user("username").roles("STORE"))
							.header("Authorization", "Bearer {ACCESS_TOKEN}")
					)
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.storeId").value(storeId))
					.andExpect(jsonPath("$.reviews[0].content").value(storeReviewsRes.reviews().get(0).content()))
					.andExpect(jsonPath("$.reviews[0].count").value(storeReviewsRes.reviews().get(0).count()))
					.andExpect(jsonPath("$.reviews[1].content").value(storeReviewsRes.reviews().get(1).content()))
					.andExpect(jsonPath("$.reviews[1].count").value(storeReviewsRes.reviews().get(1).count()))
					.andDo(document("review/review-find-by-token-success",
							preprocessRequest(prettyPrint()),
							preprocessResponse(prettyPrint()),
							requestHeaders(
								headerWithName("Authorization").description("Access Token")
							),
							responseFields(
								fieldWithPath("storeId").type(JsonFieldType.NUMBER).description("업체 아이디"),
								fieldWithPath("reviews[]").type(JsonFieldType.ARRAY).description("각 리뷰별 메시지와 개수"),
								fieldWithPath("reviews[].content").type(JsonFieldType.STRING).description("리뷰 메시지"),
								fieldWithPath("reviews[].count").type(JsonFieldType.NUMBER).description("개수")
							)
						)
					);
			}

			@Test
			@DisplayName("실패 - 업체 당사자만 리뷰를 조회할 수 있다")
			void findByStoreId_fail_unauthorized() throws Exception {
				// given
				given(reviewService.findByStoreOwnerProviderId(any()))
					.willThrow(new BusinessException(UNAUTHORIZED_REQUEST));

				// when
				// then
				mockMvc.perform(
						get(findByStoreIdPath)
							.with(user("username").roles("STORE"))
							.header("Authorization", "Bearer {ACCESS_TOKEN}")
					)
					.andDo(print())
					.andExpect(status().is4xxClientError())
					.andExpect(result -> {
						assertTrue(result.getResolvedException() instanceof BusinessException);
					})
					.andDo(document("review/review-find-by-token-fail-unauthorized",
						preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint()),
						requestHeaders(
							headerWithName("Authorization").description("Access Token")
						),
						responseFields(
							fieldWithPath("timestamp").type(STRING).description("예외 발생 시간"),
							fieldWithPath("code").type(STRING).description("오류 코드"),
							fieldWithPath("errors").type(ARRAY).description("오류 목록"),
							fieldWithPath("message").type(STRING).description("오류 메시지")
						)
					));
			}
		}

		@Nested
		@DisplayName("<주문별 리뷰 조회>")
		class findByOrderIdTest {
			String findByOrderIdPath = "/api/reviews/orders";

			ReviewRes reviewRes = new ReviewRes(List.of("사장님이 친절해요", "가격이 저렴해요"));

			@Test
			@DisplayName("성공 - 주문에 작성된 리뷰를 조회한다.")
			void findByOrderId_success() throws Exception {
				// given
				given(reviewService.findByOrderId(anyLong(), any()))
					.willReturn(reviewRes);

				// when
				// then
				mockMvc.perform(
						get(findByOrderIdPath)
							.with(csrf().asHeader())
							.with(user("username").roles("MEMBER"))
							.header("Authorization", "Bearer {ACCESS_TOKEN}")
							.contentType(APPLICATION_JSON)
							.param("id", "1")
					)
					.andDo(print())
					.andExpect(status().isOk())

					.andExpect(jsonPath("$.messages[0]").value(reviewRes.messages().get(0)))
					.andExpect(jsonPath("$.messages[1]").value(reviewRes.messages().get(1)))
					.andDo(document("review/review-find-by-order-id-success",
							preprocessRequest(prettyPrint()),
							preprocessResponse(prettyPrint()),
							requestHeaders(
								headerWithName("Authorization").description("Access Token")
							),
							requestParameters(
								parameterWithName("id").description("주문 아이디")
							),
							responseFields(
								fieldWithPath("messages[]").type(JsonFieldType.ARRAY).description("해당 주문에 작성된 리뷰 메시지")
							)
						)
					);
			}

			@Test
			@DisplayName("실패 - 주문한 고객만 리뷰를 조회할 수 있습니다.")
			void findByOrderId_fail_unauthorized() throws Exception {
				// given
				given(reviewService.findByOrderId(anyLong(), any()))
					.willThrow(new BusinessException(UNAUTHORIZED_REQUEST));

				// when
				// then
				mockMvc.perform(
						get(findByOrderIdPath)
							.with(csrf().asHeader())
							.with(user("username").roles("MEMBER"))
							.header("Authorization", "Bearer {ACCESS_TOKEN}")
							.contentType(APPLICATION_JSON)
							.param("id", "1")
					)
					.andDo(print())
					.andExpect(status().is4xxClientError())
					.andExpect(result -> {
						assertTrue(result.getResolvedException() instanceof BusinessException);
					})
					.andDo(document("review/review-find-by-order-fail-unauthorized",
						preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint()),
						requestHeaders(
							headerWithName("Authorization").description("Access Token")
						),
						responseFields(
							fieldWithPath("timestamp").type(STRING).description("예외 발생 시간"),
							fieldWithPath("code").type(STRING).description("오류 코드"),
							fieldWithPath("errors").type(ARRAY).description("오류 목록"),
							fieldWithPath("message").type(STRING).description("오류 메시지")
						)
					));
			}

		}

		@Test
		@DisplayName("<리뷰 선택지 항목 조회>")
		void getReviewContentsTest() throws Exception {
			String path = "/api/reviews/contents";

			mockMvc.perform(get(path)
					.with(csrf().asHeader())
					.with(user("username").roles("MEMBER"))
					.header("Authorization", "Bearer {ACCESS_TOKEN}")
				)
				.andDo(document("review/review-contents-success",
						preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint()),
						requestHeaders(
							headerWithName("Authorization").description("Access Token")
						),
						responseFields(
							fieldWithPath("contents[]").type(JsonFieldType.ARRAY).description("사용자에게 주어지는 선택 가능 항목들")
						)
					)
				)
				.andExpect(status().isOk())
			;
		}
	}
}
