package com.palpal.dealightbe.domain.item.presentation;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.palpal.dealightbe.domain.item.application.ItemService;
import com.palpal.dealightbe.domain.item.application.dto.request.ItemReq;
import com.palpal.dealightbe.domain.item.application.dto.response.ItemRes;
import com.palpal.dealightbe.domain.item.domain.Item;
import com.palpal.dealightbe.domain.store.domain.Store;
import com.palpal.dealightbe.global.error.ErrorCode;
import com.palpal.dealightbe.global.error.exception.BusinessException;

import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = ItemController.class)
@AutoConfigureRestDocs
class ItemControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@MockBean
	ItemService itemService;

	private Store store;
	private Item item;

	@BeforeEach
	void setUp() {
		store = Store.builder()
			.name("동네분식")
			.storePhoneNumber("000-0000")
			.telephone("0000-0000")
			.openTime(LocalDateTime.now())
			.closeTime(LocalDateTime.now().plusHours(6))
			.dayOff("000")
			.build();

		item = Item.builder()
			.name("떡볶이")
			.stock(2)
			.discountPrice(4000)
			.originalPrice(4500)
			.description("기본 떡볶이 입니다.")
			.information("통신사 할인 불가능 합니다.")
			.store(store)
			.build();
	}

	@DisplayName("상품 등록 성공 테스트")
	@Test
	public void itemCreateSuccessTest() throws Exception {
		//given
		ItemReq itemReq = new ItemReq(item.getName(), item.getStock(), item.getDiscountPrice(), item.getOriginalPrice(), item.getDescription(), item.getInformation(), item.getImage());
		ItemRes itemRes = ItemRes.from(item);

		Long memberId = 1L;

		when(itemService.create(itemReq, memberId)).thenReturn(itemRes);

		//when
		//then
		mockMvc.perform(RestDocumentationRequestBuilders.post("/api/items")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(itemReq))
				.param("memberId", memberId.toString()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value(itemRes.name()))
			.andExpect(jsonPath("$.stock").value(itemRes.stock()))
			.andExpect(jsonPath("$.discountPrice").value(itemRes.discountPrice()))
			.andExpect(jsonPath("$.originalPrice").value(itemRes.originalPrice()))
			.andExpect(jsonPath("$.description").value(itemRes.description()))
			.andExpect(jsonPath("$.information").value(itemRes.information()))
			.andExpect(jsonPath("$.image").value(itemRes.image()))
			.andDo(print())
			.andDo(document("item-create",
				Preprocessors.preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestFields(
					fieldWithPath("name").description("상품 이름"),
					fieldWithPath("stock").description("재고 수"),
					fieldWithPath("discountPrice").description("할인가"),
					fieldWithPath("originalPrice").description("원가"),
					fieldWithPath("description").description("상세 설명"),
					fieldWithPath("information").description("안내 사항"),
					fieldWithPath("image").description("상품 이미지")
				),
				responseFields(
					fieldWithPath("itemId").description("상품 ID"),
					fieldWithPath("storeId").description("업체 ID"),
					fieldWithPath("name").description("상품 이름"),
					subsectionWithPath("stock").description("재고 수"),
					fieldWithPath("discountPrice").description("할인가"),
					fieldWithPath("originalPrice").description("원가"),
					fieldWithPath("description").description("상세 설명"),
					fieldWithPath("information").description("안내 사항"),
					fieldWithPath("image").description("상품 이미지")
				)
			));
	}

	@DisplayName("상품 등록 실패 테스트 - 입력되지 않은 상품 이름")
	@Test
	public void itemCreateFailureTest_invalidName() throws Exception {
		//given
		Item item2 = Item.builder()
			.name("")
			.stock(2)
			.discountPrice(4000)
			.originalPrice(4500)
			.description("기본 떡볶이 입니다.")
			.information("통신사 할인 불가능 합니다.")
			.store(store)
			.build();

		ItemReq itemReq = new ItemReq(item2.getName(), item2.getStock(), item2.getDiscountPrice(), item2.getOriginalPrice(), item2.getDescription(), item2.getInformation(), item2.getImage());
		ItemRes itemRes = ItemRes.from(item2);

		Long memberId = 1L;

		when(itemService.create(itemReq, memberId)).thenReturn(itemRes);

		//when
		//then
		mockMvc.perform(RestDocumentationRequestBuilders.post("/api/items")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(itemReq))
				.param("memberId", memberId.toString()))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.timestamp").isNotEmpty())
			.andExpect(jsonPath("$.code").value("C001"))
			.andExpect(jsonPath("$.errors[0].field").value("name"))
			.andExpect(jsonPath("$.errors[0].value").value(""))
			.andExpect(jsonPath("$.errors[0].reason").isNotEmpty())
			.andExpect(jsonPath("$.errors[1].field").value("name"))
			.andExpect(jsonPath("$.errors[1].value").value(""))
			.andExpect(jsonPath("$.errors[1].reason").isNotEmpty())
			.andExpect(jsonPath("$.message").value("잘못된 값을 입력하셨습니다."))
			.andDo(print())
			.andDo(document("item-create-fail-invalid-name",
				Preprocessors.preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestFields(
					fieldWithPath("name").description("상품 이름"),
					fieldWithPath("stock").description("재고 수"),
					fieldWithPath("discountPrice").description("할인가"),
					fieldWithPath("originalPrice").description("원가"),
					fieldWithPath("description").description("상세 설명"),
					fieldWithPath("information").description("안내 사항"),
					fieldWithPath("image").description("상품 이미지")
				),
				responseFields(
					fieldWithPath("timestamp").type(STRING).description("예외 시간"),
					fieldWithPath("code").type(STRING).description("예외 코드"),
					fieldWithPath("errors[]").type(ARRAY).description("오류 목록"),
					fieldWithPath("errors[].field").type(STRING).description("오류 필드"),
					fieldWithPath("errors[].value").type(STRING).description("오류 값"),
					fieldWithPath("errors[].reason").type(STRING).description("오류 사유"),
					fieldWithPath("message").type(STRING).description("오류 메시지")
				)
			));
	}

	@DisplayName("상품 등록 실패 테스트 - 할인가가 원가보다 큰 경우")
	@Test
	public void itemCreateFailureTest_invalidDiscountPrice() throws Exception {
		//given
		ItemReq itemReq = new ItemReq("떡볶이", 2, 4500, 4000, "기본 떡볶이 입니다.", "통신사 할인 불가능 합니다.", null);

		Long memberId = 1L;

		when(itemService.create(any(), anyLong())).thenThrow(new BusinessException(ErrorCode.INVALID_ITEM_DISCOUNT_PRICE));

		//when
		//then
		mockMvc.perform(RestDocumentationRequestBuilders.post("/api/items")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(itemReq))
				.param("memberId", memberId.toString()))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.timestamp").isNotEmpty())
			.andExpect(jsonPath("$.code").value("I001"))
			.andExpect(jsonPath("$.errors").isEmpty())
			.andExpect(jsonPath("$.message").value("상품 할인가는 원가보다 클 수 없습니다."))
			.andDo(print())
			.andDo(document("item-create-fail-invalid-discount-price",
				Preprocessors.preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestFields(
					fieldWithPath("name").description("상품 이름"),
					fieldWithPath("stock").description("재고 수"),
					fieldWithPath("discountPrice").description("할인가"),
					fieldWithPath("originalPrice").description("원가"),
					fieldWithPath("description").description("상세 설명"),
					fieldWithPath("information").description("안내 사항"),
					fieldWithPath("image").description("상품 이미지")
				),
				responseFields(
					fieldWithPath("timestamp").type(STRING).description("예외 시간"),
					fieldWithPath("code").type(STRING).description("예외 코드"),
					fieldWithPath("errors[]").type(ARRAY).description("오류 목록"),
					fieldWithPath("message").type(STRING).description("오류 메시지")
				)
			));
	}
}
