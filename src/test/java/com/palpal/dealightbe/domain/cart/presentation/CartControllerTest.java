package com.palpal.dealightbe.domain.cart.presentation;

import static com.palpal.dealightbe.global.error.ErrorCode.ANOTHER_STORE_ITEM_ALREADY_EXISTS_IN_THE_CART;
import static com.palpal.dealightbe.global.error.ErrorCode.EXCEEDED_CART_ITEM_SIZE;
import static com.palpal.dealightbe.global.error.ErrorCode.INVALID_ATTEMPT_TO_ADD_OWN_STORE_ITEM_TO_CART;
import static com.palpal.dealightbe.global.error.ErrorCode.INVALID_CART_QUANTITY;
import static com.palpal.dealightbe.global.error.ErrorCode.NOT_FOUND_CART_ITEM;
import static com.palpal.dealightbe.global.error.ErrorCode.NOT_FOUND_ITEM;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.palpal.dealightbe.config.SecurityConfig;
import com.palpal.dealightbe.domain.address.domain.Address;
import com.palpal.dealightbe.domain.cart.application.CartService;
import com.palpal.dealightbe.domain.cart.application.dto.request.CartReq;
import com.palpal.dealightbe.domain.cart.application.dto.request.CartsReq;
import com.palpal.dealightbe.domain.cart.application.dto.response.CartRes;
import com.palpal.dealightbe.domain.cart.application.dto.response.CartsRes;
import com.palpal.dealightbe.domain.cart.domain.Cart;
import com.palpal.dealightbe.domain.item.domain.Item;
import com.palpal.dealightbe.domain.store.domain.DayOff;
import com.palpal.dealightbe.domain.store.domain.Store;
import com.palpal.dealightbe.global.error.exception.BusinessException;
import com.palpal.dealightbe.global.error.exception.EntityNotFoundException;

@WebMvcTest(value = CartController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class,
	OAuth2ClientAutoConfiguration.class}, excludeFilters = {
	@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)})
@AutoConfigureRestDocs
class CartControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@MockBean
	CartService cartService;

	private Store store;
	private Item item;
	private Item item2;
	private Address address;
	private Cart cart;
	private Cart cart2;

	@BeforeEach
	void setUp() {
		String storeDefaultImageUrl = "https://fake-image.com/store.png";

		address = Address.builder()
			.name("서울특별시 종로구 이화동 대학로8길 1")
			.xCoordinate(127.0028245)
			.yCoordinate(37.5805009)
			.build();

		store = Store.builder()
			.name("먼분식")
			.storeNumber("0000000")
			.telephone("00000000")
			.openTime(LocalTime.of(17, 0))
			.closeTime(LocalTime.of(23, 30))
			.dayOff(Collections.singleton(DayOff.MON))
			.address(address)
			.build();

		store.updateImage(storeDefaultImageUrl);

		item = Item.builder()
			.name("떡볶이")
			.stock(2)
			.discountPrice(3000)
			.originalPrice(4500)
			.description("기본 떡볶이 입니다.")
			.information("통신사 할인 불가능 합니다.")
			.image("https://fake-image.com/item1.png")
			.store(store)
			.build();

		item2 = Item.builder()
			.name("김밥")
			.stock(3)
			.discountPrice(4000)
			.originalPrice(4500)
			.description("김밥 입니다.")
			.information("통신사 할인 불가능 합니다.")
			.image("https://fake-image.com/item2.png")
			.store(store)
			.build();

		cart = Cart.builder()
			.itemId(1L)
			.storeId(1L)
			.memberProviderId(1L)
			.itemName(item.getName())
			.stock(item.getStock())
			.discountPrice(item.getDiscountPrice())
			.itemImage(item.getImage())
			.storeName(store.getName())
			.storeCloseTime(store.getCloseTime())
			.build();

		cart2 = Cart.builder()
			.itemId(2L)
			.storeId(1L)
			.memberProviderId(1L)
			.itemName(item2.getName())
			.stock(item2.getStock())
			.discountPrice(item2.getDiscountPrice())
			.itemImage(item2.getImage())
			.storeName(store.getName())
			.storeCloseTime(store.getCloseTime())
			.build();
	}

	@DisplayName("장바구니 담기 성공 테스트")
	@Test
	void addItemSuccessTest() throws Exception {
		//given
		Long itemId = 1L;

		CartRes cartRes = new CartRes(123456789L, cart.getItemId(), cart.getStoreId(), cart.getMemberProviderId(), cart.getItemName(), cart.getStock(), cart.getDiscountPrice(), cart.getItemImage(), cart.getQuantity(), cart.getStoreName(), cart.getStoreCloseTime(), cart.getExpirationDateTime());

		when(cartService.addItem(any(), any(), any())).thenReturn(cartRes);

		//when
		//then
		mockMvc.perform(RestDocumentationRequestBuilders.post("/api/carts/items")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer {ACCESS_TOKEN}")
				.param("id", String.valueOf(itemId))
				.param("type", "check"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.cartId").value(cartRes.cartId()))
			.andExpect(jsonPath("$.itemId").value(cartRes.itemId()))
			.andExpect(jsonPath("$.storeId").value(cartRes.storeId()))
			.andExpect(jsonPath("$.memberProviderId").value(cartRes.memberProviderId()))
			.andExpect(jsonPath("$.itemName").value(cartRes.itemName()))
			.andExpect(jsonPath("$.stock").value(cartRes.stock()))
			.andExpect(jsonPath("$.discountPrice").value(cartRes.discountPrice()))
			.andExpect(jsonPath("$.itemImage").value(cartRes.itemImage()))
			.andExpect(jsonPath("$.quantity").value(cartRes.quantity()))
			.andExpect(jsonPath("$.storeName").value(cartRes.storeName()))
			.andExpect(jsonPath("$.storeCloseTime").value(cartRes.storeCloseTime().toString()))
			.andExpect(jsonPath("$.expirationDateTime").value(cartRes.expirationDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
			.andDo(print())
			.andDo(document("cart/cart-add-item",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				requestParameters(
					parameterWithName("id").description("상품 ID"),
					parameterWithName("type").description("장바구니 담기 타입")
				),
				responseFields(
					fieldWithPath("cartId").type(NUMBER).description("장바구니 ID"),
					fieldWithPath("itemId").type(NUMBER).description("상품 ID"),
					fieldWithPath("storeId").type(NUMBER).description("업체 ID"),
					fieldWithPath("memberProviderId").type(NUMBER).description("회원 provider ID"),
					fieldWithPath("itemName").type(STRING).description("상품 이름"),
					fieldWithPath("stock").type(NUMBER).description("재고 수"),
					fieldWithPath("discountPrice").type(NUMBER).description("할인가"),
					fieldWithPath("itemImage").type(STRING).description("상품 이미지 경로"),
					fieldWithPath("quantity").type(NUMBER).description("장바구니에 담은 개수"),
					fieldWithPath("storeName").type(STRING).description("상호명"),
					fieldWithPath("storeCloseTime").type(STRING).description("마감 시간"),
					fieldWithPath("expirationDateTime").type(STRING).description("장바구니 만료 시점")
				)
			));
	}

	@DisplayName("장바구니 담기 실패 테스트 - 유효하지 않은 장바구니 담기 타입")
	@Test
	void addItemFailureTest_invalidCartAdditionType() throws Exception {
		//given
		Long itemId = 1L;

		//when
		//then
		mockMvc.perform(RestDocumentationRequestBuilders.post("/api/carts/items")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer {ACCESS_TOKEN}")
				.param("id", String.valueOf(itemId))
				.param("type", "failure"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.timestamp").isNotEmpty())
			.andExpect(jsonPath("$.code").value("CT006"))
			.andExpect(jsonPath("$.errors").isEmpty())
			.andExpect(jsonPath("$.message").value("유효하지 않은 장바구니 담기 타입 입니다."))
			.andDo(print())
			.andDo(document("cart/cart-add-item-invalid-cart-addition-type",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				requestParameters(
					parameterWithName("id").description("상품 ID"),
					parameterWithName("type").description("장바구니 담기 타입")
				),
				responseFields(
					fieldWithPath("timestamp").type(STRING).description("예외 시간"),
					fieldWithPath("code").type(STRING).description("예외 코드"),
					fieldWithPath("errors[]").type(ARRAY).description("오류 목록"),
					fieldWithPath("message").type(STRING).description("오류 메시지")
				)
			));
	}

	@DisplayName("장바구니 담기 실패 테스트 - 본인이 등록한 업체의 상품 담기 불가")
	@Test
	void addItemFailureTest_invalidAttemptOwnStoreItem() throws Exception {
		//given
		Long itemId = 1L;

		doThrow(new BusinessException(INVALID_ATTEMPT_TO_ADD_OWN_STORE_ITEM_TO_CART)).when(
			cartService).addItem(any(), any(), any());

		//when
		//then
		mockMvc.perform(RestDocumentationRequestBuilders.post("/api/carts/items")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer {ACCESS_TOKEN}")
				.param("id", String.valueOf(itemId))
				.param("type", "check"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.timestamp").isNotEmpty())
			.andExpect(jsonPath("$.code").value("CT004"))
			.andExpect(jsonPath("$.errors").isEmpty())
			.andExpect(jsonPath("$.message").value("본인이 등록한 업체의 상품은 장바구니에 담을 수 없습니다."))
			.andDo(print())
			.andDo(document("cart/cart-add-item-invalid-attempt-own-store-item",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				requestParameters(
					parameterWithName("id").description("상품 ID"),
					parameterWithName("type").description("장바구니 담기 타입")
				),
				responseFields(
					fieldWithPath("timestamp").type(STRING).description("예외 시간"),
					fieldWithPath("code").type(STRING).description("예외 코드"),
					fieldWithPath("errors[]").type(ARRAY).description("오류 목록"),
					fieldWithPath("message").type(STRING).description("오류 메시지")
				)
			));
	}

	@DisplayName("장바구니 담기 실패 테스트 - 타 업체 상품이 존재하는 경우")
	@Test
	void addItemFailureTest_anotherStoreItemExists() throws Exception {
		//given
		Long itemId = 1L;

		doThrow(new BusinessException(ANOTHER_STORE_ITEM_ALREADY_EXISTS_IN_THE_CART)).when(
			cartService).addItem(any(), any(), any());

		//when
		//then
		mockMvc.perform(RestDocumentationRequestBuilders.post("/api/carts/items")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer {ACCESS_TOKEN}")
				.param("id", String.valueOf(itemId))
				.param("type", "check"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.timestamp").isNotEmpty())
			.andExpect(jsonPath("$.code").value("CT003"))
			.andExpect(jsonPath("$.errors").isEmpty())
			.andExpect(jsonPath("$.message").value("이미 다른 업체의 상품이 장바구니에 담겨 있습니다."))
			.andDo(print())
			.andDo(document("cart/cart-add-item-another-store-item-exists",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				requestParameters(
					parameterWithName("id").description("상품 ID"),
					parameterWithName("type").description("장바구니 담기 타입")
				),
				responseFields(
					fieldWithPath("timestamp").type(STRING).description("예외 시간"),
					fieldWithPath("code").type(STRING).description("예외 코드"),
					fieldWithPath("errors[]").type(ARRAY).description("오류 목록"),
					fieldWithPath("message").type(STRING).description("오류 메시지")
				)
			));
	}

	@DisplayName("장바구니 담기 실패 테스트 - 최대 5개 종류를 초과하여 장바구니 담기를 시도하는 경우")
	@Test
	void addItemFailureTest_exceededCartItemSize() throws Exception {
		//given
		Long itemId = 1L;

		doThrow(new BusinessException(EXCEEDED_CART_ITEM_SIZE)).when(
			cartService).addItem(any(), any(), any());

		//when
		//then
		mockMvc.perform(RestDocumentationRequestBuilders.post("/api/carts/items")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer {ACCESS_TOKEN}")
				.param("id", String.valueOf(itemId))
				.param("type", "check"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.timestamp").isNotEmpty())
			.andExpect(jsonPath("$.code").value("CT005"))
			.andExpect(jsonPath("$.errors").isEmpty())
			.andExpect(jsonPath("$.message").value("최대 5가지 종류의 상품까지만 장바구니에 담을 수 있습니다."))
			.andDo(print())
			.andDo(document("cart/cart-add-item-exceeded-cart-item-size",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				requestParameters(
					parameterWithName("id").description("상품 ID"),
					parameterWithName("type").description("장바구니 담기 타입")
				),
				responseFields(
					fieldWithPath("timestamp").type(STRING).description("예외 시간"),
					fieldWithPath("code").type(STRING).description("예외 코드"),
					fieldWithPath("errors[]").type(ARRAY).description("오류 목록"),
					fieldWithPath("message").type(STRING).description("오류 메시지")
				)
			));
	}

	@DisplayName("장바구니 담기 실패 테스트 - 상품이 존재하지 않는 경우")
	@Test
	void addItemFailureTest_itemNotFound() throws Exception {
		//given
		Long itemId = 1L;

		doThrow(new EntityNotFoundException(NOT_FOUND_ITEM)).when(
			cartService).addItem(any(), any(), any());

		//when
		//then
		mockMvc.perform(RestDocumentationRequestBuilders.post("/api/carts/items")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer {ACCESS_TOKEN}")
				.param("id", String.valueOf(itemId))
				.param("type", "check"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.timestamp").isNotEmpty())
			.andExpect(jsonPath("$.code").value("I002"))
			.andExpect(jsonPath("$.errors").isEmpty())
			.andExpect(jsonPath("$.message").value("상품이 존재하지 않습니다."))
			.andDo(print())
			.andDo(document("cart/cart-add-item-item-not-found",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				requestParameters(
					parameterWithName("id").description("상품 ID"),
					parameterWithName("type").description("장바구니 담기 타입")
				),
				responseFields(
					fieldWithPath("timestamp").type(STRING).description("예외 시간"),
					fieldWithPath("code").type(STRING).description("예외 코드"),
					fieldWithPath("errors[]").type(ARRAY).description("오류 목록"),
					fieldWithPath("message").type(STRING).description("오류 메시지")
				)
			));
	}

	@DisplayName("장바구니 조회 성공 테스트")
	@Test
	void findAllByProviderIdSuccessTest() throws Exception {
		//given
		CartRes cartRes1 = new CartRes(123456789L, cart.getItemId(), cart.getStoreId(), cart.getMemberProviderId(), cart.getItemName(), cart.getStock(), cart.getDiscountPrice(), cart.getItemImage(), cart.getQuantity(), cart.getStoreName(), cart.getStoreCloseTime(), cart.getExpirationDateTime());
		CartRes cartRes2 = new CartRes(123456790L, cart2.getItemId(), cart2.getStoreId(), cart2.getMemberProviderId(), cart2.getItemName(), cart2.getStock(), cart2.getDiscountPrice(), cart2.getItemImage(), cart2.getQuantity(), cart2.getStoreName(), cart2.getStoreCloseTime(), cart2.getExpirationDateTime());

		List<CartRes> cartResList = List.of(cartRes1, cartRes2);
		CartsRes cartsRes = new CartsRes(cartResList);

		when(cartService.findAllByProviderId(any())).thenReturn(cartsRes);

		//when
		//then
		mockMvc.perform(RestDocumentationRequestBuilders.get("/api/carts")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer {ACCESS_TOKEN}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.carts[0].cartId").value(cartRes1.cartId()))
			.andExpect(jsonPath("$.carts[0].itemId").value(cartRes1.itemId()))
			.andExpect(jsonPath("$.carts[0].storeId").value(cartRes1.storeId()))
			.andExpect(jsonPath("$.carts[0].memberProviderId").value(cartRes1.memberProviderId()))
			.andExpect(jsonPath("$.carts[0].itemName").value(cartRes1.itemName()))
			.andExpect(jsonPath("$.carts[0].stock").value(cartRes1.stock()))
			.andExpect(jsonPath("$.carts[0].discountPrice").value(cartRes1.discountPrice()))
			.andExpect(jsonPath("$.carts[0].itemImage").value(cartRes1.itemImage()))
			.andExpect(jsonPath("$.carts[0].quantity").value(cartRes1.quantity()))
			.andExpect(jsonPath("$.carts[0].storeName").value(cartRes1.storeName()))
			.andExpect(jsonPath("$.carts[0].storeCloseTime").value(cartRes1.storeCloseTime().toString()))
			.andExpect(jsonPath("$.carts[0].expirationDateTime").value(cartRes1.expirationDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
			.andDo(print())
			.andDo(document("cart/cart-find-all-by-provider-id",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				responseFields(
					fieldWithPath("carts").type(ARRAY).description("장바구니 배열"),
					fieldWithPath("carts[0].cartId").type(NUMBER).description("장바구니 ID"),
					fieldWithPath("carts[0].itemId").type(NUMBER).description("상품 ID"),
					fieldWithPath("carts[0].storeId").type(NUMBER).description("업체 ID"),
					fieldWithPath("carts[0].memberProviderId").type(NUMBER).description("회원 provider ID"),
					fieldWithPath("carts[0].itemName").type(STRING).description("상품 이름"),
					fieldWithPath("carts[0].stock").type(NUMBER).description("재고 수"),
					fieldWithPath("carts[0].discountPrice").type(NUMBER).description("할인가"),
					fieldWithPath("carts[0].itemImage").type(STRING).description("상품 이미지 경로"),
					fieldWithPath("carts[0].quantity").type(NUMBER).description("장바구니에 담은 개수"),
					fieldWithPath("carts[0].storeName").type(STRING).description("상호명"),
					fieldWithPath("carts[0].storeCloseTime").type(STRING).description("마감 시간"),
					fieldWithPath("carts[0].expirationDateTime").type(STRING).description("장바구니 만료 시점")
				)
			));
	}

	@DisplayName("장바구니 수정 성공 테스트")
	@Test
	void updateSuccessTest() throws Exception {
		//given
		CartReq cartReq = new CartReq(1L, 2);
		CartReq cartReq2 = new CartReq(2L, 3);

		CartsReq cartsReq = new CartsReq(List.of(cartReq, cartReq2));

		CartRes cartRes = new CartRes(123456789L, cart.getItemId(), cart.getStoreId(), cart.getMemberProviderId(), cart.getItemName(), cart.getStock(), cart.getDiscountPrice(), cart.getItemImage(), cartReq.quantity(), cart.getStoreName(), cart.getStoreCloseTime(), cart.getExpirationDateTime());
		CartRes cartRes2 = new CartRes(123456789L, cart2.getItemId(), cart2.getStoreId(), cart2.getMemberProviderId(), cart2.getItemName(), cart2.getStock(), cart2.getDiscountPrice(), cart2.getItemImage(), cartReq2.quantity(), cart2.getStoreName(), cart2.getStoreCloseTime(), cart2.getExpirationDateTime());

		CartsRes cartsRes = new CartsRes(List.of(cartRes, cartRes2));
		when(cartService.update(any(), any())).thenReturn(cartsRes);

		//when
		//then
		mockMvc.perform(RestDocumentationRequestBuilders.patch("/api/carts")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer {ACCESS_TOKEN}")
				.content(objectMapper.writeValueAsString(cartsReq)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.carts[0].cartId").value(cartRes.cartId()))
			.andExpect(jsonPath("$.carts[0].itemId").value(cartRes.itemId()))
			.andExpect(jsonPath("$.carts[0].storeId").value(cartRes.storeId()))
			.andExpect(jsonPath("$.carts[0].memberProviderId").value(cartRes.memberProviderId()))
			.andExpect(jsonPath("$.carts[0].itemName").value(cartRes.itemName()))
			.andExpect(jsonPath("$.carts[0].stock").value(cartRes.stock()))
			.andExpect(jsonPath("$.carts[0].discountPrice").value(cartRes.discountPrice()))
			.andExpect(jsonPath("$.carts[0].itemImage").value(cartRes.itemImage()))
			.andExpect(jsonPath("$.carts[0].quantity").value(cartRes.quantity()))
			.andExpect(jsonPath("$.carts[0].storeName").value(cartRes.storeName()))
			.andExpect(jsonPath("$.carts[0].storeCloseTime").value(cartRes.storeCloseTime().toString()))
			.andExpect(jsonPath("$.carts[0].expirationDateTime").value(cartRes.expirationDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
			.andDo(print())
			.andDo(document("cart/cart-update",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				requestFields(
					fieldWithPath("carts[0].itemId").description("상품 ID"),
					fieldWithPath("carts[0].quantity").description("장바구니에 담은 개수")
				),
				responseFields(
					fieldWithPath("carts").type(ARRAY).description("장바구니 배열"),
					fieldWithPath("carts[0].cartId").type(NUMBER).description("장바구니 ID"),
					fieldWithPath("carts[0].itemId").type(NUMBER).description("상품 ID"),
					fieldWithPath("carts[0].storeId").type(NUMBER).description("업체 ID"),
					fieldWithPath("carts[0].memberProviderId").type(NUMBER).description("회원 provider ID"),
					fieldWithPath("carts[0].itemName").type(STRING).description("상품 이름"),
					fieldWithPath("carts[0].stock").type(NUMBER).description("재고 수"),
					fieldWithPath("carts[0].discountPrice").type(NUMBER).description("할인가"),
					fieldWithPath("carts[0].itemImage").type(STRING).description("상품 이미지 경로"),
					fieldWithPath("carts[0].quantity").type(NUMBER).description("장바구니에 담은 개수"),
					fieldWithPath("carts[0].storeName").type(STRING).description("상호명"),
					fieldWithPath("carts[0].storeCloseTime").type(STRING).description("마감 시간"),
					fieldWithPath("carts[0].expirationDateTime").type(STRING).description("장바구니 만료 시점")
				)
			));
	}

	@DisplayName("장바구니 수정 실패 테스트 - 장바구니의 최소, 최대 수량 조건에 맞지 않는 경우")
	@Test
	void updateFailureTest_invalidCartQuantity() throws Exception {
		//given
		CartReq cartReq = new CartReq(1L, 0);
		CartReq cartReq2 = new CartReq(2L, 100);

		CartsReq cartsReq = new CartsReq(List.of(cartReq, cartReq2));

		doThrow(new BusinessException(INVALID_CART_QUANTITY)).when(
			cartService).update(any(), any());

		//when
		//then
		mockMvc.perform(RestDocumentationRequestBuilders.patch("/api/carts")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer {ACCESS_TOKEN}")
				.content(objectMapper.writeValueAsString(cartsReq)))
			.andExpect(jsonPath("$.timestamp").isNotEmpty())
			.andExpect(jsonPath("$.code").value("CT002"))
			.andExpect(jsonPath("$.errors").isEmpty())
			.andExpect(jsonPath("$.message").value("상품 당 최소 1개에서 최대 재고 수량까지만 장바구니에 담을 수 있습니다."))
			.andDo(print())
			.andDo(document("cart/cart-update-invalid-cart-quantity",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				requestFields(
					fieldWithPath("carts[0].itemId").description("상품 ID"),
					fieldWithPath("carts[0].quantity").description("장바구니에 담은 개수")
				),
				responseFields(
					fieldWithPath("timestamp").type(STRING).description("예외 시간"),
					fieldWithPath("code").type(STRING).description("예외 코드"),
					fieldWithPath("errors[]").type(ARRAY).description("오류 목록"),
					fieldWithPath("message").type(STRING).description("오류 메시지")
				)
			));
	}

	@DisplayName("장바구니 수정 실패 테스트 - 장바구니의 상품이 존재하지 않는 경우")
	@Test
	void updateFailureTest_notFoundCartItem() throws Exception {
		//given
		CartReq cartReq = new CartReq(1L, 2);
		CartReq cartReq2 = new CartReq(2L, 3);

		CartsReq cartsReq = new CartsReq(List.of(cartReq, cartReq2));

		doThrow(new EntityNotFoundException(NOT_FOUND_CART_ITEM)).when(
			cartService).update(any(), any());

		//when
		//then
		mockMvc.perform(RestDocumentationRequestBuilders.patch("/api/carts")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer {ACCESS_TOKEN}")
				.content(objectMapper.writeValueAsString(cartsReq)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.timestamp").isNotEmpty())
			.andExpect(jsonPath("$.code").value("CT001"))
			.andExpect(jsonPath("$.errors").isEmpty())
			.andExpect(jsonPath("$.message").value("장바구니에 상품이 존재하지 않습니다."))
			.andDo(print())
			.andDo(document("cart/cart-update-not-found-cart-item",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				requestFields(
					fieldWithPath("carts[0].itemId").description("상품 ID"),
					fieldWithPath("carts[0].quantity").description("장바구니에 담은 개수")
				),
				responseFields(
					fieldWithPath("timestamp").type(STRING).description("예외 시간"),
					fieldWithPath("code").type(STRING).description("예외 코드"),
					fieldWithPath("errors[]").type(ARRAY).description("오류 목록"),
					fieldWithPath("message").type(STRING).description("오류 메시지")
				)
			));
	}

	@DisplayName("장바구니 요소 삭제 성공 테스트")
	@Test
	void deleteOneSuccessTest() throws Exception {
		//given
		Long itemId = 1L;

		doNothing().when(cartService).deleteOne(any(), any());

		//when
		//then
		mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/carts/items")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer {ACCESS_TOKEN}")
				.param("id", String.valueOf(itemId)))
			.andExpect(status().isNoContent())
			.andDo(print())
			.andDo(document("cart/cart-delete-one",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				requestParameters(
					parameterWithName("id").description("상품 ID")
				)
			));
	}

	@DisplayName("장바구니 요소 삭제 실패 테스트 - 장바구니의 상품이 존재하지 않는 경우")
	@Test
	void deleteOneFailureTest_notFoundCartItem() throws Exception {
		//given
		Long itemId = 1L;

		doThrow(new EntityNotFoundException(NOT_FOUND_CART_ITEM)).when(
			cartService).deleteOne(any(), any());

		//when
		//then
		mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/carts/items")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer {ACCESS_TOKEN}")
				.param("id", String.valueOf(itemId)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.timestamp").isNotEmpty())
			.andExpect(jsonPath("$.code").value("CT001"))
			.andExpect(jsonPath("$.errors").isEmpty())
			.andExpect(jsonPath("$.message").value("장바구니에 상품이 존재하지 않습니다."))
			.andDo(print())
			.andDo(document("cart/cart-delete-one-not-found-cart-item",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				requestParameters(
					parameterWithName("id").description("상품 ID")
				),
				responseFields(
					fieldWithPath("timestamp").type(STRING).description("예외 시간"),
					fieldWithPath("code").type(STRING).description("예외 코드"),
					fieldWithPath("errors[]").type(ARRAY).description("오류 목록"),
					fieldWithPath("message").type(STRING).description("오류 메시지")
				)
			));
	}

	@DisplayName("장바구니 초기화 성공 테스트")
	@Test
	void deleteAllSuccessTest() throws Exception {
		//given
		doNothing().when(cartService).deleteAll(any());

		//when
		//then
		mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/carts")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer {ACCESS_TOKEN}"))
			.andExpect(status().isNoContent())
			.andDo(print())
			.andDo(document("cart/cart-delete-all",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				)
			));
	}
}
