package com.palpal.dealightbe.domain.order.presentation;

import static com.palpal.dealightbe.domain.order.domain.OrderStatus.RECEIVED;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
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
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.palpal.dealightbe.domain.order.application.OrderService;
import com.palpal.dealightbe.domain.order.application.dto.request.OrderCreateReq;
import com.palpal.dealightbe.domain.order.application.dto.request.OrderProductReq;
import com.palpal.dealightbe.domain.order.application.dto.request.OrderProductsReq;
import com.palpal.dealightbe.domain.order.application.dto.request.OrderStatusUpdateReq;
import com.palpal.dealightbe.domain.order.application.dto.response.OrderProductRes;
import com.palpal.dealightbe.domain.order.application.dto.response.OrderProductsRes;
import com.palpal.dealightbe.domain.order.application.dto.response.OrderRes;
import com.palpal.dealightbe.domain.order.application.dto.response.OrderStatusUpdateRes;
import com.palpal.dealightbe.domain.order.application.dto.response.OrdersRes;

@AutoConfigureRestDocs
@WebMvcTest(
	value = OrderController.class,
	excludeAutoConfiguration = {SecurityAutoConfiguration.class, OAuth2ClientAutoConfiguration.class}
)
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
		String createApiPath = "/api/orders";

		LocalDateTime createdAt = LocalDateTime.now();

		OrderCreateReq orderCreateReq = new OrderCreateReq(
			new OrderProductsReq(List.of(new OrderProductReq(1L, 3))),
			1L, "도착할 때까지 상품 냉장고에 보관 부탁드려요", LocalTime.of(12, 30), 10000);

		OrderProductsRes productsRes = new OrderProductsRes(List.of(new OrderProductRes(1L, "달콤한 도넛", 5, 10000, 15000,
			"https://team-08-bucket.s3.ap-northeast-2.amazonaws.com/donut")));

		OrderRes orderRes = new OrderRes(1L, 1L, 1L, "GS25", "도착할 때까지 상품 냉장고에 보관 부탁드려요", LocalTime.of(12, 30),
			productsRes, 10000, createdAt, RECEIVED.getText());

		@Test
		@DisplayName("성공 - 신규 주문을 등록한다")
		void create_success() throws Exception {
			// given
			OrderProductRes productRes = productsRes.orderProducts().get(0);

			given(orderService.create(any(), any()))
				.willReturn(orderRes);

			// when
			// then
			mockMvc.perform(
					post(createApiPath)
						.with(csrf())
						.with(user("username").roles("MEMBER"))
						.header("Authorization", "Bearer {ACCESS_TOKEN}")
						.content(objectMapper.writeValueAsString(orderCreateReq))
						.contentType(APPLICATION_JSON)
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
				.andExpect(jsonPath("$.status").value(RECEIVED.getText()))
				.andDo(document("order/order-create-success", preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestHeaders(
						headerWithName("Authorization").description("Access Token")
					),
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
						fieldWithPath("status").type(JsonFieldType.STRING).description("현재 주문 상태"))));
		}

		@Test
		@DisplayName("실패 - 주문시 도착 예정 시간을 입력하지 않은 경우 예외가 발생한다.")
		void create_fail_arrival_time() throws Exception {
			// given
			OrderCreateReq orderCreateReq = new OrderCreateReq(
				new OrderProductsReq(List.of(new OrderProductReq(1L, 3))), 1L, "도착할 때까지 상품 냉장고에 보관 부탁드려요", null, 10000);

			given(orderService.create(any(OrderCreateReq.class), anyLong()))
				.willReturn(orderRes);

			// when
			// then
			mockMvc.perform(
					post(createApiPath)
						.with(csrf())
						.with(user("username").roles("MEMBER"))
						.header("Authorization", "Bearer {ACCESS_TOKEN}")
						.content(objectMapper.writeValueAsString(orderCreateReq))
						.contentType(APPLICATION_JSON)
				)
				.andDo(print())
				.andExpect(status().is4xxClientError())
				.andExpect(result -> {
					assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException);
				})
				.andDo(print())
				.andDo(document("order/order-create-fail-arrival-time",
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
		@DisplayName("실패 - 주문시 업체 아이디를 입력하지 않은 경우 예외가 발생한다.")
		void create_fail_storeId() throws Exception {
			// given
			OrderCreateReq orderCreateReq = new OrderCreateReq(
				new OrderProductsReq(List.of(new OrderProductReq(1L, 3))), null, "도착할 때까지 상품 냉장고에 보관 부탁드려요",
				LocalTime.of(12, 30), 10000);

			given(orderService.create(any(OrderCreateReq.class), anyLong())).willReturn(orderRes);

			// when
			// then
			mockMvc.perform(
					post(createApiPath)
						.with(csrf())
						.with(user("username").roles("MEMBER"))
						.header("Authorization", "Bearer {ACCESS_TOKEN}")
						.content(objectMapper.writeValueAsString(orderCreateReq))
						.contentType(APPLICATION_JSON)
				)
				.andDo(print())
				.andExpect(status().is4xxClientError())
				.andExpect(result -> {
					assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException);
				})
				.andDo(document("order/order-create-fail-store-id",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestHeaders(
						headerWithName("Authorization").description("Access Token")
					),
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
		@DisplayName("실패 - 주문시 상품이 0개인 경우 않은 경우 예외가 발생한다.")
		void create_fail_totalPrice_zero() throws Exception {
			// given
			OrderCreateReq orderCreateReq = new OrderCreateReq(
				new OrderProductsReq(List.of(new OrderProductReq(1L, 3))), 1L, "도착할 때까지 상품 냉장고에 보관 부탁드려요",
				LocalTime.of(12, 30), 0);

			given(orderService.create(any(OrderCreateReq.class), anyLong()))
				.willReturn(orderRes);

			// when
			// then
			mockMvc.perform(
					post(createApiPath)
						.with(csrf())
						.with(user("username").roles("MEMBER"))
						.header("Authorization", "Bearer {ACCESS_TOKEN}")
						.content(objectMapper.writeValueAsString(orderCreateReq))
						.contentType(APPLICATION_JSON)
				)
				.andDo(print())
				.andExpect(status().is4xxClientError())
				.andExpect(result -> {
					assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException);
				})
				.andDo(document("order/order-create-item",
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
	}

	@Nested
	@DisplayName("<주문 상태 변경>")
	class updateStatusTest {
		String updateStatusApiPath = "/api/orders/{orderId}";

		OrderStatusUpdateRes orderStatusUpdateRes = new OrderStatusUpdateRes(1L, "RECEIVED");
		OrderStatusUpdateReq orderStatusUpdateReq = new OrderStatusUpdateReq("RECEIVED");

		@Test
		@DisplayName("성공 - 주문 상태를 변경한다")
		void updateStatus_success() throws Exception {
			// given
			long orderId = 1L;
			long memberProviderId = 1L;

			given(orderService.updateStatus(any(), any(), any()))
				.willReturn(orderStatusUpdateRes);

			// when
			// then
			mockMvc.perform(
					patch(updateStatusApiPath, orderId, memberProviderId)
						.with(csrf())
						.with(user("username").roles("MEMBER"))
						.header("Authorization", "Bearer {ACCESS_TOKEN}")
						.content(objectMapper.writeValueAsString(orderStatusUpdateReq))
						.contentType(APPLICATION_JSON)
				)
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.orderId").value(orderId))
				.andExpect(jsonPath("$.status").value(orderStatusUpdateRes.status()))
				.andDo(
					document("order/order-status-update-success",
						preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint()),
						pathParameters(
							parameterWithName("orderId").description("상태 변경을 하고자 하는 주문의 아이디")
						),
						requestHeaders(
							headerWithName("Authorization").description("Access Token")
						),
						requestFields(
							fieldWithPath("status").type(JsonFieldType.STRING)
								.description("변경 후의 주문 상태(CONFIRMED, COMPLETED, CANCELED)")
						),
						responseFields(
							fieldWithPath("orderId").type(JsonFieldType.NUMBER).description("상태가 변경된 주문의 아이디"),
							fieldWithPath("status").type(JsonFieldType.STRING).description("변경 완료된 후의 주문 상태")
						)
					));
		}

		@Test
		@DisplayName("실패 - 변경 후의 주문 상태를 입력하지 않은 경우 예외가 발생한다.")
		void updateStatus_fail() throws Exception {
			// given
			long orderId = 1L;
			long memberProviderId = 1L;

			OrderStatusUpdateReq invalidOrderStatusUpdateReq = new OrderStatusUpdateReq(null);

			given(orderService.updateStatus(anyLong(), any(OrderStatusUpdateReq.class), anyLong())).willReturn(
				orderStatusUpdateRes);

			// when
			// then
			mockMvc.perform(
					patch(updateStatusApiPath, orderId, memberProviderId)
						.with(csrf())
						.with(user("username").roles("MEMBER"))
						.header("Authorization", "Bearer {ACCESS_TOKEN}")
						.content(objectMapper.writeValueAsString(invalidOrderStatusUpdateReq))
						.contentType(APPLICATION_JSON)
				)
				.andDo(print())
				.andExpect(status().is4xxClientError())
				.andExpect(result -> {
					assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException);
				})
				.andDo(document("order/order-create-fail-status",
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
	}

	@Nested
	@DisplayName("<주문 상세 정보 조회>")
	class detailedInfoTest {
		String findByIdApiPath = "/api/orders/{orderId}";

		LocalDateTime createdAt = LocalDateTime.now();

		OrderCreateReq orderCreateReq = new OrderCreateReq(
			new OrderProductsReq(List.of(new OrderProductReq(1L, 3))), 1L, "도착할 때까지 상품 냉장고에 보관 부탁드려요",
			LocalTime.of(12, 30), 10000);

		OrderProductsRes productsRes = new OrderProductsRes(List.of(new OrderProductRes(1L, "달콤한 도넛", 5, 10000, 15000,
			"https://team-08-bucket.s3.ap-northeast-2.amazonaws.com/donut")));

		OrderRes orderRes = new OrderRes(1L, 1L, 1L, "GS25", "도착할 때까지 상품 냉장고에 보관 부탁드려요", LocalTime.of(12, 30),
			productsRes, 10000, createdAt, RECEIVED.getText());

		OrderProductRes productRes = productsRes.orderProducts().get(0);

		@Test
		@DisplayName("성공 - 주문 상세 정보를 조회한다")
		void findById_success() throws Exception {
			// given
			long orderId = 1L;

			given(orderService.findById(any(), any()))
				.willReturn(orderRes);

			// when
			// then
			mockMvc.perform(
					get(findByIdApiPath, orderId)
						.with(csrf())
						.with(user("username").roles("MEMBER"))
						.header("Authorization", "Bearer {ACCESS_TOKEN}")
				)
				.andDo(print())
				.andExpect(status().isOk())
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
				.andExpect(jsonPath("$.status").value(RECEIVED.getText()))
				.andDo(document("order/order-find-by-id-success", preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					pathParameters(
						parameterWithName("orderId").description("상세 조회 하고자 하는 주문의 아이디")
					),
					requestHeaders(
						headerWithName("Authorization").description("Access Token")
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
						fieldWithPath("status").type(JsonFieldType.STRING).description("현재 주문 상태"))));
		}
	}

	@Nested
	@DisplayName("<주문 목록 조회 - 업체>")
	class findByStoreIdTest {
		String findByStoreIdPath = "/api/orders/stores";

		LocalDateTime createdAt = LocalDateTime.now();

		OrderProductsRes productsRes = new OrderProductsRes(List.of(new OrderProductRes(1L, "달콤한 도넛", 5, 10000, 15000,
			"https://team-08-bucket.s3.ap-northeast-2.amazonaws.com/donut")));

		OrderProductRes productRes = productsRes.orderProducts().get(0);

		OrderRes orderRes = new OrderRes(1L, 1L, 1L, "GS25", "도착할 때까지 상품 냉장고에 보관 부탁드려요", LocalTime.of(12, 30),
			productsRes, 10000, createdAt, RECEIVED.getText());

		OrdersRes ordersRes = new OrdersRes(List.of(orderRes), false);

		@Test
		@DisplayName("성공 - 업체의 주문 목록을 조회한다")
		void findByStoreId_success() throws Exception {
			// given
			given(orderService.findAllByStoreId(any(), any(), any(), any()))
				.willReturn(ordersRes);

			// when
			// then
			mockMvc.perform(
					get(findByStoreIdPath, 1)
						.param("id", "1")
						.with(user("username").roles("MEMBER"))
						.header("Authorization", "Bearer {ACCESS_TOKEN}")
				)
				.andExpect(status().isOk())

				.andExpect(jsonPath("$.orders[0].orderId").value(1L))
				.andExpect(jsonPath("$.orders[0].storeId").value(1L))
				.andExpect(jsonPath("$.orders[0].memberId").value(1L))
				.andExpect(jsonPath("$.orders[0].storeName").value("GS25"))
				.andExpect(jsonPath("$.orders[0].demand").value(orderRes.demand()))
				.andExpect(jsonPath("$.orders[0].orderProductsRes.orderProducts[0].itemId").value(productRes.itemId()))
				.andExpect(jsonPath("$.orders[0].orderProductsRes.orderProducts[0].name").value(productRes.name()))
				.andExpect(jsonPath("$.orders[0].orderProductsRes.orderProducts[0].stock").value(productRes.stock()))
				.andExpect(jsonPath("$.orders[0].orderProductsRes.orderProducts[0].discountPrice")
					.value(productRes.discountPrice()))
				.andExpect(jsonPath("$.orders[0].orderProductsRes.orderProducts[0].originalPrice")
					.value(productRes.originalPrice()))
				.andExpect(jsonPath("$.orders[0].orderProductsRes.orderProducts[0].image").value(productRes.image()))
				.andExpect(jsonPath("$.orders[0].totalPrice").value(orderRes.totalPrice()))
				.andExpect(jsonPath("$.orders[0].status").value(RECEIVED.getText()))
				.andExpect(jsonPath("$.hasNext").value(false))
				.andDo(
					document("order/order-find-by-store-id-success",
						preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint()),
						requestHeaders(
							headerWithName("Authorization").description("Access Token")
						),
						requestParameters(
							parameterWithName("id").description("업체의 아이디"),
							parameterWithName("status")
								.description("주문 목록 중 보고자 하는 주문의 상태 - RECEIVED, CONFIRMED, COMPLETED, CANCELED")
								.optional(),
							parameterWithName("page").description("데이터 조회 시작 위치(==offset)").optional(),
							parameterWithName("size").description("한 번에 조회할 데이터 개수(초기값:10)").optional()
						),
						responseFields(
							fieldWithPath("orders").type(JsonFieldType.ARRAY).description("주문 목록"),
							fieldWithPath("orders[].orderId").type(JsonFieldType.NUMBER).description("등록된 주문의 아이디"),
							fieldWithPath("orders[].storeId").type(JsonFieldType.NUMBER).description("주문이 이루어진 업체 아이디"),
							fieldWithPath("orders[].memberId").type(JsonFieldType.NUMBER).description("고객 아이디"),
							fieldWithPath("orders[].storeName").type(JsonFieldType.STRING).description("업체 이름"),
							fieldWithPath("orders[].demand").type(JsonFieldType.STRING).description("고객의 요구 사항"),
							fieldWithPath("orders[].arrivalTime").type(JsonFieldType.STRING)
								.description("고객의 도착 예정 시간"),
							fieldWithPath("orders[].orderProductsRes.orderProducts[]").type(JsonFieldType.ARRAY)
								.description("주문한 상품 목록"),
							fieldWithPath("orders[].orderProductsRes.orderProducts[].itemId").type(JsonFieldType.NUMBER)
								.description("상품 아이디"),
							fieldWithPath("orders[].orderProductsRes.orderProducts[].name").type(JsonFieldType.STRING)
								.description("상품명"),
							fieldWithPath("orders[].orderProductsRes.orderProducts[].stock").type(JsonFieldType.NUMBER)
								.description("상품 재고"),
							fieldWithPath("orders[].orderProductsRes.orderProducts[].discountPrice")
								.type(JsonFieldType.NUMBER)
								.description("상품의 할인된 금액"),
							fieldWithPath("orders[].orderProductsRes.orderProducts[].originalPrice")
								.type(JsonFieldType.NUMBER)
								.description("상품 원가"),
							fieldWithPath("orders[].orderProductsRes.orderProducts[].image").type(JsonFieldType.STRING)
								.description("상품 이미지"),
							fieldWithPath("orders[].totalPrice").type(JsonFieldType.NUMBER).description("총 금액"),
							fieldWithPath("orders[].createdAt").type(JsonFieldType.STRING).description("주문 완료 일자 및 시간"),
							fieldWithPath("orders[].status").type(JsonFieldType.STRING).description("현재 주문 상태"),
							fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN).description("다음 데이터 존재 여부")))
				);
		}
	}

	@Nested
	@DisplayName("<주문 목록 조회 - 고객>")
	class findByMemberProviderIdTest {
		String findByMemberProviderIdPath = "/api/orders";

		LocalDateTime createdAt = LocalDateTime.now();

		OrderProductsRes productsRes = new OrderProductsRes(List.of(new OrderProductRes(1L, "달콤한 도넛", 5, 10000, 15000,
			"https://team-08-bucket.s3.ap-northeast-2.amazonaws.com/donut")));

		OrderProductRes productRes = productsRes.orderProducts().get(0);

		OrderRes orderRes = new OrderRes(1L, 1L, 1L, "GS25", "도착할 때까지 상품 냉장고에 보관 부탁드려요", LocalTime.of(12, 30),
			productsRes, 10000, createdAt, RECEIVED.getText());

		OrdersRes ordersRes = new OrdersRes(List.of(orderRes), false);

		@Test
		@DisplayName("성공 - 업체의 주문 목록을 조회한다")
		void findByStoreId_success() throws Exception {
			// given
			given(orderService.findAllByMemberProviderId(any(), any(), any()))
				.willReturn(ordersRes);

			// when
			// then
			mockMvc.perform(
					get(findByMemberProviderIdPath)
						.with(user("username").roles("MEMBER"))
						.header("Authorization", "Bearer {ACCESS_TOKEN}")
				)
				.andExpect(status().isOk())

				.andExpect(jsonPath("$.orders[0].orderId").value(1L))
				.andExpect(jsonPath("$.orders[0].storeId").value(1L))
				.andExpect(jsonPath("$.orders[0].memberId").value(1L))
				.andExpect(jsonPath("$.orders[0].storeName").value("GS25"))
				.andExpect(jsonPath("$.orders[0].demand").value(orderRes.demand()))
				.andExpect(jsonPath("$.orders[0].orderProductsRes.orderProducts[0].itemId").value(productRes.itemId()))
				.andExpect(jsonPath("$.orders[0].orderProductsRes.orderProducts[0].name").value(productRes.name()))
				.andExpect(jsonPath("$.orders[0].orderProductsRes.orderProducts[0].stock").value(productRes.stock()))
				.andExpect(jsonPath("$.orders[0].orderProductsRes.orderProducts[0].discountPrice")
					.value(productRes.discountPrice()))
				.andExpect(jsonPath("$.orders[0].orderProductsRes.orderProducts[0].originalPrice")
					.value(productRes.originalPrice()))
				.andExpect(jsonPath("$.orders[0].orderProductsRes.orderProducts[0].image").value(productRes.image()))
				.andExpect(jsonPath("$.orders[0].totalPrice").value(orderRes.totalPrice()))
				.andExpect(jsonPath("$.orders[0].status").value(RECEIVED.getText()))
				.andExpect(jsonPath("$.hasNext").value(false))
				.andDo(
					document("order/order-find-by-member-success",
						preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint()),
						requestHeaders(
							headerWithName("Authorization").description("Access Token")
						),
						requestParameters(
							parameterWithName("status")
								.description("주문 목록 중 보고자 하는 주문의 상태 - RECEIVED, CONFIRMED, COMPLETED, CANCELED")
								.optional(),
							parameterWithName("page").description("데이터 조회 시작 위치(==offset)").optional(),
							parameterWithName("size").description("한 번에 조회할 데이터 개수(초기값:10)").optional()
						),
						responseFields(
							fieldWithPath("orders").type(JsonFieldType.ARRAY).description("주문 목록"),
							fieldWithPath("orders[].orderId").type(JsonFieldType.NUMBER).description("등록된 주문의 아이디"),
							fieldWithPath("orders[].storeId").type(JsonFieldType.NUMBER).description("주문이 이루어진 업체 아이디"),
							fieldWithPath("orders[].memberId").type(JsonFieldType.NUMBER).description("고객 아이디"),
							fieldWithPath("orders[].storeName").type(JsonFieldType.STRING).description("업체 이름"),
							fieldWithPath("orders[].demand").type(JsonFieldType.STRING).description("고객의 요구 사항"),
							fieldWithPath("orders[].arrivalTime").type(JsonFieldType.STRING)
								.description("고객의 도착 예정 시간"),
							fieldWithPath("orders[].orderProductsRes.orderProducts[]").type(JsonFieldType.ARRAY)
								.description("주문한 상품 목록"),
							fieldWithPath("orders[].orderProductsRes.orderProducts[].itemId").type(JsonFieldType.NUMBER)
								.description("상품 아이디"),
							fieldWithPath("orders[].orderProductsRes.orderProducts[].name").type(JsonFieldType.STRING)
								.description("상품명"),
							fieldWithPath("orders[].orderProductsRes.orderProducts[].stock").type(JsonFieldType.NUMBER)
								.description("상품 재고"),
							fieldWithPath("orders[].orderProductsRes.orderProducts[].discountPrice")
								.type(JsonFieldType.NUMBER)
								.description("상품의 할인된 금액"),
							fieldWithPath("orders[].orderProductsRes.orderProducts[].originalPrice")
								.type(JsonFieldType.NUMBER)
								.description("상품 원가"),
							fieldWithPath("orders[].orderProductsRes.orderProducts[].image").type(JsonFieldType.STRING)
								.description("상품 이미지"),
							fieldWithPath("orders[].totalPrice").type(JsonFieldType.NUMBER).description("총 금액"),
							fieldWithPath("orders[].createdAt").type(JsonFieldType.STRING).description("주문 완료 일자 및 시간"),
							fieldWithPath("orders[].status").type(JsonFieldType.STRING).description("현재 주문 상태"),
							fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN).description("다음 데이터 존재 여부")))
				);
		}
	}
}
