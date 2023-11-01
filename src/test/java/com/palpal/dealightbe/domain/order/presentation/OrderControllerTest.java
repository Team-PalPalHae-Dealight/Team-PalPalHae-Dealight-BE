package com.palpal.dealightbe.domain.order.presentation;

import static com.palpal.dealightbe.domain.order.domain.OrderStatus.RECEIVED;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.time.LocalTime;
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
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.palpal.dealightbe.config.SecurityConfig;
import com.palpal.dealightbe.domain.auth.filter.JwtAuthenticationFilter;
import com.palpal.dealightbe.domain.order.application.OrderService;
import com.palpal.dealightbe.domain.order.application.dto.request.OrderCreateReq;
import com.palpal.dealightbe.domain.order.application.dto.request.OrderProductReq;
import com.palpal.dealightbe.domain.order.application.dto.request.OrderProductsReq;
import com.palpal.dealightbe.domain.order.application.dto.response.OrderProductRes;
import com.palpal.dealightbe.domain.order.application.dto.response.OrderProductsRes;
import com.palpal.dealightbe.domain.order.application.dto.response.OrderRes;

@AutoConfigureRestDocs
@WebMvcTest(value = OrderController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class,
	OAuth2ClientAutoConfiguration.class}, excludeFilters = {
	@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)})
public class OrderControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private OrderService orderService;

	@Nested
	@DisplayName("<주문 생성>")
	class createTest {
		String createApiPath = "/orders";

		LocalDateTime createdAt = LocalDateTime.now();

		OrderProductsRes productsRes = new OrderProductsRes(List.of(new OrderProductRes(
			1L, "달콤한 도넛", 5, 10000, 15000,
			"https://team-08-image-bucket.s3.ap-northeast-2.amazonaws.com/donut"
		)));

		OrderRes orderRes = new OrderRes(
			1L,
			1L,
			1L,
			"GS25",
			"도착할 때까지 상품 냉장고에 보관 부탁드려요",
			LocalTime.of(12, 30),
			productsRes,
			10000,
			createdAt,
			RECEIVED.getText()
		);

		@Test
		@DisplayName("성공 - 신규 주문을 등록한다")
		void create_success() throws Exception {
			// given
			OrderCreateReq orderCreateReq = new OrderCreateReq(
				new OrderProductsReq(List.of(new OrderProductReq(1L, 3))),
				1L,
				"도착할 때까지 상품 냉장고에 보관 부탁드려요",
				LocalTime.of(12, 30),
				10000
			);

			OrderProductRes productRes = productsRes.orderProducts().get(0);

			given(orderService.create(any(OrderCreateReq.class), anyLong()))
				.willReturn(orderRes);

			// when
			// then
			mockMvc.perform(
					post(createApiPath + "/{memberProviderId}", 1)
						.with(csrf())
						.with(user("username").roles("MEMBER"))
						.content(objectMapper.writeValueAsString(orderCreateReq))
						.contentType(MediaType.APPLICATION_JSON)
				)
				.andDo(print())
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.orderId").value(1L))
				.andExpect(jsonPath("$.storeId").value(1L))
				.andExpect(jsonPath("$.memberId").value(1L))
				.andExpect(jsonPath("$.storeName").value("GS25"))
				.andExpect(jsonPath("$.demand").value(orderCreateReq.demand()))
				.andExpect(jsonPath("$.orderProductsRes.orderProducts[0].itemId").value(productRes.itemId()))
				.andExpect(jsonPath("$.orderProductsRes.orderProducts[0].name").value(productRes.name()))
				.andExpect(jsonPath("$.orderProductsRes.orderProducts[0].stock").value(productRes.stock()))
				.andExpect(
					jsonPath("$.orderProductsRes.orderProducts[0].discountPrice").value(productRes.discountPrice()))
				.andExpect(
					jsonPath("$.orderProductsRes.orderProducts[0].originalPrice").value(productRes.originalPrice()))
				.andExpect(jsonPath("$.orderProductsRes.orderProducts[0].image").value(productRes.image()))
				.andExpect(jsonPath("$.totalPrice").value(orderCreateReq.totalPrice()))
				.andExpect(jsonPath("$.createdAt").value(String.valueOf(createdAt)))
				.andExpect(jsonPath("$.status").value(RECEIVED.getText()))
				.andDo(document(
					"order-create-success",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					pathParameters(parameterWithName("memberProviderId").description("고객 카카오 토큰")),
					requestFields(
						fieldWithPath("orderProductsReq.orderProducts[]").type(JsonFieldType.ARRAY)
							.description("주문한 상품 정보 목록"),
						fieldWithPath("orderProductsReq.orderProducts[].itemId").type(JsonFieldType.NUMBER)
							.description("상품의 아이디"),
						fieldWithPath("orderProductsReq.orderProducts[].quantity").type(JsonFieldType.NUMBER)
							.description("상품의 수량"),

						fieldWithPath("storeId").type(JsonFieldType.NUMBER).description("상품을 구매한 업체 아이디"),
						fieldWithPath("demand").type(JsonFieldType.STRING).description("상품 구매시 작성한 고객의 요청 사항"),
						fieldWithPath("arrivalTime").type(JsonFieldType.STRING).description("고객의 도착 예정 시간"),
						fieldWithPath("totalPrice").type(JsonFieldType.NUMBER).description("주문 총 금액")
					),
					responseFields(
						fieldWithPath("orderId").type(JsonFieldType.NUMBER).description("등록된 주문의 아이디"),
						fieldWithPath("storeId").type(JsonFieldType.NUMBER).description("주문이 이루어진 업체 아이디"),
						fieldWithPath("memberId").type(JsonFieldType.NUMBER).description("고객 아이디"),
						fieldWithPath("storeName").type(JsonFieldType.STRING).description("업체 이름"),
						fieldWithPath("demand").type(JsonFieldType.STRING).description("고객의 요구 사항"),
						fieldWithPath("arrivalTime").type(JsonFieldType.STRING).description("고객의 도착 예정 시간"),

						fieldWithPath("orderProductsRes.orderProducts[]").type(JsonFieldType.ARRAY)
							.description("주문한 상품 목록"),
						fieldWithPath("orderProductsRes.orderProducts[].itemId").type(JsonFieldType.NUMBER)
							.description("상품 아이디"),
						fieldWithPath("orderProductsRes.orderProducts[].name").type(JsonFieldType.STRING)
							.description("상품명"),
						fieldWithPath("orderProductsRes.orderProducts[].stock").type(JsonFieldType.NUMBER)
							.description("상품 재고"),
						fieldWithPath("orderProductsRes.orderProducts[].discountPrice").type(JsonFieldType.NUMBER)
							.description("상품의 할인된 금액"),
						fieldWithPath("orderProductsRes.orderProducts[].originalPrice").type(JsonFieldType.NUMBER)
							.description("상품 원가"),
						fieldWithPath("orderProductsRes.orderProducts[].image").type(JsonFieldType.STRING)
							.description("상품 이미지"),

						fieldWithPath("totalPrice").type(JsonFieldType.NUMBER).description("총 금액"),
						fieldWithPath("createdAt").type(JsonFieldType.STRING).description("주문 완료 일자 및 시간"),
						fieldWithPath("status").type(JsonFieldType.STRING).description("현재 주문 상태")
					)));
		}

		@Test
		@DisplayName("실패 - 주문시 도착 예정 시간을 입력하지 않은 경우 예외가 발생한다.")
		void create_fail_arrival_time() throws Exception {
			// given
			OrderCreateReq orderCreateReq = new OrderCreateReq(
				new OrderProductsReq(List.of(new OrderProductReq(1L, 3))),
				1L,
				"도착할 때까지 상품 냉장고에 보관 부탁드려요",
				null,
				10000
			);

			given(orderService.create(any(OrderCreateReq.class), anyLong()))
				.willReturn(orderRes);

			// when
			// then
			mockMvc.perform(
					post(createApiPath + "/{memberProviderId}", 1)
						.with(csrf())
						.with(user("username").roles("MEMBER"))
						.content(objectMapper.writeValueAsString(orderCreateReq))
						.contentType(MediaType.APPLICATION_JSON)
				)
				.andDo(print())
				.andExpect(status().is4xxClientError())
				.andExpect(result -> {
					assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException);
				});
		}

		@Test
		@DisplayName("실패 - 주문시 업체 아이디를 입력하지 않은 경우 예외가 발생한다.")
		void create_fail_storeId() throws Exception {
			// given
			OrderCreateReq orderCreateReq = new OrderCreateReq(
				new OrderProductsReq(List.of(new OrderProductReq(1L, 3))),
				null,
				"도착할 때까지 상품 냉장고에 보관 부탁드려요",
				LocalTime.of(12, 30),
				10000
			);

			given(orderService.create(any(OrderCreateReq.class), anyLong()))
				.willReturn(orderRes);

			// when
			// then
			mockMvc.perform(
					post(createApiPath + "/{memberProviderId}", 1)
						.with(csrf())
						.with(user("username").roles("MEMBER"))
						.content(objectMapper.writeValueAsString(orderCreateReq))
						.contentType(MediaType.APPLICATION_JSON)
				)
				.andDo(print())
				.andExpect(status().is4xxClientError())
				.andExpect(result -> {
					assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException);
				});
		}

		@Test
		@DisplayName("실패 - 주문시 상품이 0개인 경우 않은 경우 예외가 발생한다.")
		void create_fail_totalPrice_zero() throws Exception {
			// given
			OrderCreateReq orderCreateReq = new OrderCreateReq(
				new OrderProductsReq(List.of(new OrderProductReq(1L, 3))),
				1L,
				"도착할 때까지 상품 냉장고에 보관 부탁드려요",
				LocalTime.of(12, 30),
				0
			);

			given(orderService.create(any(OrderCreateReq.class), anyLong()))
				.willReturn(orderRes);

			// when
			// then
			mockMvc.perform(
					post(createApiPath + "/{memberProviderId}", 1)
						.with(csrf())
						.with(user("username").roles("MEMBER"))
						.content(objectMapper.writeValueAsString(orderCreateReq))
						.contentType(MediaType.APPLICATION_JSON)
				)
				.andDo(print())
				.andExpect(status().is4xxClientError())
				.andExpect(result -> {
					assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException);
				});
		}
	}
}
