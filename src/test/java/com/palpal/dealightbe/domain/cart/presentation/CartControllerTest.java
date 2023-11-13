package com.palpal.dealightbe.domain.cart.presentation;

import static com.palpal.dealightbe.global.error.ErrorCode.ANOTHER_STORE_ITEM_ALREADY_EXISTS_IN_THE_CART;
import static com.palpal.dealightbe.global.error.ErrorCode.INVALID_ATTEMPT_TO_ADD_OWN_STORE_ITEM_TO_CART;
import static com.palpal.dealightbe.global.error.ErrorCode.NOT_FOUND_ITEM;
import static org.mockito.ArgumentMatchers.any;
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
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalTime;
import java.util.Collections;

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
import com.palpal.dealightbe.domain.cart.application.dto.response.CartRes;
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
	private Store store2;
	private Item item;
	private Item item2;
	private Address address;
	private Address address2;
	private Cart cart;
	private Cart cart2;

	@BeforeEach
	void setUp() {
		LocalTime openTime = LocalTime.of(13, 0);
		LocalTime closeTime = LocalTime.of(20, 0);

		String storeDefaultImageUrl = "https://fake-image.com/store.png";

		address = Address.builder()
			.name("서울특별시 성북구 안암로 145")
			.xCoordinate(127.0324773)
			.yCoordinate(37.5893876)
			.build();

		store = Store.builder()
			.name("동네분식")
			.storeNumber("0000000")
			.telephone("00000000")
			.openTime(openTime)
			.closeTime(closeTime)
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


		address2 = Address.builder()
			.name("서울특별시 종로구 이화동 대학로8길 1")
			.xCoordinate(127.0028245)
			.yCoordinate(37.5805009)
			.build();

		store2 = Store.builder()
			.name("먼분식")
			.storeNumber("0000000")
			.telephone("00000000")
			.openTime(LocalTime.of(17, 0))
			.closeTime(LocalTime.of(23, 30))
			.dayOff(Collections.singleton(DayOff.MON))
			.address(address2)
			.build();

		store2.updateImage(storeDefaultImageUrl);

		item2 = Item.builder()
			.name("김밥")
			.stock(3)
			.discountPrice(4000)
			.originalPrice(4500)
			.description("김밥 입니다.")
			.information("통신사 할인 불가능 합니다.")
			.image("https://fake-image.com/item2.png")
			.store(store2)
			.build();

		cart2 = Cart.builder()
			.itemId(2L)
			.storeId(2L)
			.memberProviderId(2L)
			.itemName(item2.getName())
			.stock(item2.getStock())
			.discountPrice(item2.getDiscountPrice())
			.itemImage(item2.getImage())
			.storeName(store2.getName())
			.storeCloseTime(store2.getCloseTime())
			.build();
	}

	@DisplayName("장바구니 담기 성공 테스트 (타 업체 상품 존재 시 예외 처리 api)")
	@Test
	void checkAndAddItem() throws Exception {
		//given
		Long itemId = 1L;

		CartRes cartRes = new CartRes(123456789L, cart.getItemId(), cart.getStoreId(), cart.getMemberProviderId(), cart.getItemName(), cart.getStock(), cart.getDiscountPrice(), cart.getItemImage(), cart.getQuantity(), cart.getStoreName(), cart.getStoreCloseTime());

		when(cartService.checkAndAddItem(any(), any())).thenReturn(cartRes);

		//when
		//then
		mockMvc.perform(RestDocumentationRequestBuilders.post("/api/orders/carts")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer {ACCESS_TOKEN}")
				.param("id", String.valueOf(itemId)))
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
			.andDo(print())
			.andDo(document("cart/cart-check-and-add-item",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				requestParameters(
					parameterWithName("id").description("상품 ID")
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
					fieldWithPath("storeCloseTime").type(STRING).description("마감 시간")
				)
			));
	}

	@DisplayName("장바구니 담기 실패 테스트 (타 업체 상품 존재 시 예외 처리 api) - 본인이 등록한 업체의 상품 담기 불가")
	@Test
	void checkAndAddItem_invalidAttemptOwnStoreItem() throws Exception {
		//given
		Long itemId = 1L;

		doThrow(new BusinessException(INVALID_ATTEMPT_TO_ADD_OWN_STORE_ITEM_TO_CART)).when(
			cartService).checkAndAddItem(any(), any());

		//when
		//then
		mockMvc.perform(RestDocumentationRequestBuilders.post("/api/orders/carts")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer {ACCESS_TOKEN}")
				.param("id", String.valueOf(itemId)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.timestamp").isNotEmpty())
			.andExpect(jsonPath("$.code").value("CT004"))
			.andExpect(jsonPath("$.errors").isEmpty())
			.andExpect(jsonPath("$.message").value("본인이 등록한 업체의 상품은 장바구니에 담을 수 없습니다."))
			.andDo(print())
			.andDo(document("cart/cart-check-and-add-item-invalid-attempt-own-store-item",
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

	@DisplayName("장바구니 담기 실패 테스트 (타 업체 상품 존재 시 예외 처리 api) - 타 업체 상품 존재")
	@Test
	void checkAndAddItem_anotherStoreItemExists() throws Exception {
		//given
		Long itemId = 1L;

		doThrow(new BusinessException(ANOTHER_STORE_ITEM_ALREADY_EXISTS_IN_THE_CART)).when(
			cartService).checkAndAddItem(any(), any());

		//when
		//then
		mockMvc.perform(RestDocumentationRequestBuilders.post("/api/orders/carts")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer {ACCESS_TOKEN}")
				.param("id", String.valueOf(itemId)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.timestamp").isNotEmpty())
			.andExpect(jsonPath("$.code").value("CT003"))
			.andExpect(jsonPath("$.errors").isEmpty())
			.andExpect(jsonPath("$.message").value("이미 다른 업체의 상품이 장바구니에 담겨 있습니다."))
			.andDo(print())
			.andDo(document("cart/cart-check-and-add-item-another-store-item-exists",
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

	@DisplayName("장바구니 담기 실패 테스트 (타 업체 상품 존재 시 예외 처리 api) - 상품이 존재하지 않는 경우")
	@Test
	void checkAndAddItem_itemNotFound() throws Exception {
		//given
		Long itemId = 1L;

		doThrow(new EntityNotFoundException(NOT_FOUND_ITEM)).when(
			cartService).checkAndAddItem(any(), any());

		//when
		//then
		mockMvc.perform(RestDocumentationRequestBuilders.post("/api/orders/carts")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer {ACCESS_TOKEN}")
				.param("id", String.valueOf(itemId)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.timestamp").isNotEmpty())
			.andExpect(jsonPath("$.code").value("I002"))
			.andExpect(jsonPath("$.errors").isEmpty())
			.andExpect(jsonPath("$.message").value("상품이 존재하지 않습니다."))
			.andDo(print())
			.andDo(document("cart/cart-check-and-add-item-item-not-found",
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

	@Test
	void clearAndAddItem() {
	}
}
