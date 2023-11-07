package com.palpal.dealightbe.domain.item.presentation;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.palpal.dealightbe.config.SecurityConfig;
import com.palpal.dealightbe.domain.address.application.dto.response.AddressRes;
import com.palpal.dealightbe.domain.address.domain.Address;
import com.palpal.dealightbe.domain.image.application.dto.request.ImageUploadReq;
import com.palpal.dealightbe.domain.item.application.ItemService;
import com.palpal.dealightbe.domain.item.application.dto.request.ItemReq;
import com.palpal.dealightbe.domain.item.application.dto.response.ItemRes;
import com.palpal.dealightbe.domain.item.application.dto.response.ItemsRes;
import com.palpal.dealightbe.domain.item.domain.Item;
import com.palpal.dealightbe.domain.store.application.dto.response.StoreInfoRes;
import com.palpal.dealightbe.domain.store.domain.DayOff;
import com.palpal.dealightbe.domain.store.domain.Store;
import com.palpal.dealightbe.global.error.ErrorCode;
import com.palpal.dealightbe.global.error.exception.BusinessException;
import com.palpal.dealightbe.global.error.exception.EntityNotFoundException;

import static com.palpal.dealightbe.global.error.ErrorCode.DUPLICATED_ITEM_NAME;
import static com.palpal.dealightbe.global.error.ErrorCode.NOT_FOUND_ITEM;
import static com.palpal.dealightbe.global.error.ErrorCode.STORE_HAS_NO_ITEM;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.JsonFieldType.NULL;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = ItemController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class,
	OAuth2ClientAutoConfiguration.class}, excludeFilters = {
	@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)})
@AutoConfigureRestDocs
class ItemControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@MockBean
	ItemService itemService;

	private Store store;
	private Store store2;
	private Item item;
	private Item item2;
	private Address address;
	private Address address2;

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
	}

	@DisplayName("상품 등록 성공 테스트")
	@Test
	public void itemCreateSuccessTest() throws Exception {
		//given
		StoreInfoRes storeInfoRes = new StoreInfoRes(store.getStoreNumber(), store.getName(), store.getTelephone(), store.getAddress().getName(), store.getOpenTime(), store.getCloseTime(), store.getDayOffs(), store.getStoreStatus(), store.getImage());
		AddressRes addressRes = new AddressRes(address.getName(), address.getXCoordinate(), address.getYCoordinate());

		ItemReq itemReq = new ItemReq(item.getName(), item.getStock(), item.getDiscountPrice(), item.getOriginalPrice(),
			item.getDescription(), item.getInformation());
		ItemRes itemRes = new ItemRes(1L, 1L, item.getName(), item.getStock(), item.getDiscountPrice(), item.getOriginalPrice(), item.getDescription(), item.getInformation(), item.getImage(), storeInfoRes, addressRes);

		MockMultipartFile file = new MockMultipartFile("image", "test.jpg", "image/jpeg", "file".getBytes());
		MockMultipartFile request = new MockMultipartFile("itemReq", "itemReq",
			"application/json",
			objectMapper.writeValueAsString(itemReq).getBytes());

		Long memberId = 1L;

		when(itemService.create(any(), any(), any())).thenReturn(itemRes);

		//when
		//then
		mockMvc.perform(RestDocumentationRequestBuilders.multipart("/api/items")
				.file(file)
				.file(request)
				.contentType(MediaType.MULTIPART_FORM_DATA)
				.accept(MediaType.APPLICATION_JSON)
				.param("memberId", memberId.toString()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.itemId").value(itemRes.itemId()))
			.andExpect(jsonPath("$.storeId").value(itemRes.storeId()))
			.andExpect(jsonPath("$.name").value(itemRes.name()))
			.andExpect(jsonPath("$.stock").value(itemRes.stock()))
			.andExpect(jsonPath("$.discountPrice").value(itemRes.discountPrice()))
			.andExpect(jsonPath("$.originalPrice").value(itemRes.originalPrice()))
			.andExpect(jsonPath("$.description").value(itemRes.description()))
			.andExpect(jsonPath("$.information").value(itemRes.information()))
			.andExpect(jsonPath("$.image").value(itemRes.image()))
			.andExpect(jsonPath("$.storeInfoRes.storeNumber").value(storeInfoRes.storeNumber()))
			.andExpect(jsonPath("$.storeInfoRes.name").value(storeInfoRes.name()))
			.andExpect(jsonPath("$.storeInfoRes.telephone").value(storeInfoRes.telephone()))
			.andExpect(jsonPath("$.storeInfoRes.addressName").value(storeInfoRes.addressName()))
			.andExpect(jsonPath("$.storeInfoRes.openTime").value(storeInfoRes.openTime().toString()))
			.andExpect(jsonPath("$.storeInfoRes.closeTime").value(storeInfoRes.closeTime().toString()))
			.andExpect(jsonPath("$.storeInfoRes.dayOff").value(storeInfoRes.dayOff().stream().map(DayOff::getName).collect(Collectors.toList())))
			.andExpect(jsonPath("$.storeInfoRes.storeStatus").value(storeInfoRes.storeStatus().getName()))
			.andExpect(jsonPath("$.storeInfoRes.image").value(storeInfoRes.image()))
			.andExpect(jsonPath("$.storeAddressRes.name").value(addressRes.name()))
			.andExpect(jsonPath("$.storeAddressRes.xCoordinate").value(addressRes.xCoordinate()))
			.andExpect(jsonPath("$.storeAddressRes.yCoordinate").value(addressRes.yCoordinate()))
			.andDo(print())
			.andDo(document("item/item-create",
				Preprocessors.preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestParameters(parameterWithName("memberId").description("고객 ID")
				),
				requestParts(
					partWithName("image").description("상품 이미지"),
					partWithName("itemReq").description("상품 등록 내용")
				),
				responseFields(
					fieldWithPath("itemId").type(NUMBER).description("상품 ID"),
					fieldWithPath("storeId").type(NUMBER).description("업체 ID"),
					fieldWithPath("name").type(STRING).description("상품 이름"),
					fieldWithPath("stock").type(NUMBER).description("재고 수"),
					fieldWithPath("discountPrice").type(NUMBER).description("할인가"),
					fieldWithPath("originalPrice").type(NUMBER).description("원가"),
					fieldWithPath("description").type(STRING).description("상세 설명"),
					fieldWithPath("information").type(STRING).description("안내 사항"),
					fieldWithPath("image").type(STRING).description("상품 이미지 경로"),
					fieldWithPath("storeInfoRes.storeNumber").type(STRING).description("사업자 등록 번호"),
					fieldWithPath("storeInfoRes.name").type(STRING).description("상호명"),
					fieldWithPath("storeInfoRes.telephone").type(STRING).description("업체 전화번호"),
					fieldWithPath("storeInfoRes.addressName").type(STRING).description("업체 주소"),
					fieldWithPath("storeInfoRes.openTime").type(STRING).description("오픈 시간"),
					fieldWithPath("storeInfoRes.closeTime").type(STRING).description("마감 시간"),
					fieldWithPath("storeInfoRes.dayOff").type(ARRAY).description("휴무일"),
					fieldWithPath("storeInfoRes.storeStatus").type(STRING).description("영업 유무"),
					fieldWithPath("storeInfoRes.image").type(STRING).description("업체 이미지 경로"),
					fieldWithPath("storeAddressRes.name").type(STRING).description("업체 주소 URL"),
					fieldWithPath("storeAddressRes.xCoordinate").type(NUMBER).description("업체 주소 경도"),
					fieldWithPath("storeAddressRes.yCoordinate").type(NUMBER).description("업체 주소 위도")
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

		StoreInfoRes storeInfoRes = new StoreInfoRes(store.getStoreNumber(), store.getName(), store.getTelephone(), store.getAddress().getName(), store.getOpenTime(), store.getCloseTime(), store.getDayOffs(), store.getStoreStatus(), store.getImage());
		AddressRes addressRes = new AddressRes(address.getName(), address.getXCoordinate(), address.getYCoordinate());

		ItemReq itemReq = new ItemReq(item2.getName(), item2.getStock(), item2.getDiscountPrice(),
			item2.getOriginalPrice(), item2.getDescription(), item2.getInformation());
		ItemRes itemRes = new ItemRes(1L, 1L, item2.getName(), item2.getStock(), item2.getDiscountPrice(), item2.getOriginalPrice(), item2.getDescription(), item2.getInformation(), item2.getImage(), storeInfoRes, addressRes);

		MockMultipartFile file = new MockMultipartFile("image", "test.jpg", "image/jpeg", "file".getBytes());
		MockMultipartFile request = new MockMultipartFile("itemReq", "itemReq",
			"application/json",
			objectMapper.writeValueAsString(itemReq).getBytes());

		Long memberId = 1L;

		when(itemService.create(any(), any(), any())).thenReturn(itemRes);

		//when
		//then
		mockMvc.perform(RestDocumentationRequestBuilders.multipart("/api/items")
				.file(file)
				.file(request)
				.contentType(MediaType.MULTIPART_FORM_DATA)
				.accept(MediaType.APPLICATION_JSON)
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
			.andDo(document("item/item-create-fail-invalid-name",
				Preprocessors.preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestParameters(parameterWithName("memberId").description("고객 ID")),
				requestParts(
					partWithName("image").description("상품 이미지"),
					partWithName("itemReq").description("상품 등록 내용")
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
		ItemReq itemReq = new ItemReq("떡볶이", 2, 4500, 4000, "기본 떡볶이 입니다.", "통신사 할인 불가능 합니다.");

		Long memberId = 1L;

		MockMultipartFile file = new MockMultipartFile("image", "test.jpg", "image/jpeg", "file".getBytes());
		MockMultipartFile request = new MockMultipartFile("itemReq", "itemReq",
			"application/json",
			objectMapper.writeValueAsString(itemReq).getBytes());

		when(itemService.create(any(), anyLong(), any())).thenThrow(
			new BusinessException(ErrorCode.INVALID_ITEM_DISCOUNT_PRICE));

		//when
		//then
		mockMvc.perform(RestDocumentationRequestBuilders.multipart("/api/items")
				.file(file)
				.file(request)
				.contentType(MediaType.MULTIPART_FORM_DATA)
				.accept(MediaType.APPLICATION_JSON)
				.param("memberId", memberId.toString()))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.timestamp").isNotEmpty())
			.andExpect(jsonPath("$.code").value("I001"))
			.andExpect(jsonPath("$.errors").isEmpty())
			.andExpect(jsonPath("$.message").value("상품 할인가는 원가보다 클 수 없습니다."))
			.andDo(print())
			.andDo(document("item/item-create-fail-invalid-discount-price",
				Preprocessors.preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestParameters(parameterWithName("memberId").description("고객 ID")),
				requestParts(
					partWithName("image").description("상품 이미지"),
					partWithName("itemReq").description("상품 등록 내용")
				),
				responseFields(
					fieldWithPath("timestamp").type(STRING).description("예외 시간"),
					fieldWithPath("code").type(STRING).description("예외 코드"),
					fieldWithPath("errors[]").type(ARRAY).description("오류 목록"),
					fieldWithPath("message").type(STRING).description("오류 메시지")
				)
			));
	}

	@DisplayName("상품 등록 실패 테스트 - 이미 등록된 상품인 경우(이름 중복)")
	@Test
	public void itemCreateFailureTest_duplicatedItemName() throws Exception {
		//given
		ItemReq itemReq = new ItemReq("떡볶이", 2, 4500, 4000, "기본 떡볶이 입니다.", "통신사 할인 불가능 합니다.");

		Long memberId = 1L;

		MockMultipartFile file = new MockMultipartFile("image", "test.jpg", "image/jpeg", "file".getBytes());
		MockMultipartFile request = new MockMultipartFile("itemReq", "itemReq",
			"application/json",
			objectMapper.writeValueAsString(itemReq).getBytes());

		doThrow(new BusinessException(DUPLICATED_ITEM_NAME)).when(
			itemService).create(any(), any(), any());

		//when
		//then
		mockMvc.perform(RestDocumentationRequestBuilders.multipart("/api/items")
				.file(file)
				.file(request)
				.contentType(MediaType.MULTIPART_FORM_DATA)
				.accept(MediaType.APPLICATION_JSON)
				.param("memberId", memberId.toString()))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.timestamp").isNotEmpty())
			.andExpect(jsonPath("$.code").value("I003"))
			.andExpect(jsonPath("$.errors").isEmpty())
			.andExpect(jsonPath("$.message").value("동일한 이름을 가진 상품이 이미 등록되어 있습니다."))
			.andDo(print())
			.andDo(document("item/item-create-fail-duplicated-item-name",
				Preprocessors.preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestParameters(parameterWithName("memberId").description("고객 ID")),
				requestParts(
					partWithName("image").description("상품 이미지"),
					partWithName("itemReq").description("상품 등록 내용")
				),
				responseFields(
					fieldWithPath("timestamp").type(STRING).description("예외 시간"),
					fieldWithPath("code").type(STRING).description("예외 코드"),
					fieldWithPath("errors[]").type(ARRAY).description("오류 목록"),
					fieldWithPath("message").type(STRING).description("오류 메시지")
				)
			));
	}

	@DisplayName("상품 상세 정보 조회(단건) 성공 테스트")
	@Test
	public void itemFindByIdSuccessTest() throws Exception {
		//given
		Long itemId = 1L;

		StoreInfoRes storeInfoRes = new StoreInfoRes(store.getStoreNumber(), store.getName(), store.getTelephone(), store.getAddress().getName(), store.getOpenTime(), store.getCloseTime(), store.getDayOffs(), store.getStoreStatus(), store.getImage());
		AddressRes addressRes = new AddressRes(address.getName(), address.getXCoordinate(), address.getYCoordinate());

		ItemRes itemRes = new ItemRes(itemId, 1L, item.getName(), item.getStock(), item.getDiscountPrice(), item.getOriginalPrice(), item.getDescription(), item.getInformation(), item.getImage(), storeInfoRes, addressRes);

		when(itemService.findById(any())).thenReturn(itemRes);

		//when
		//then
		mockMvc.perform(RestDocumentationRequestBuilders.get("/api/items/{id}", itemId)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.itemId").value(itemRes.itemId()))
			.andExpect(jsonPath("$.storeId").value(itemRes.storeId()))
			.andExpect(jsonPath("$.name").value(itemRes.name()))
			.andExpect(jsonPath("$.stock").value(itemRes.stock()))
			.andExpect(jsonPath("$.discountPrice").value(itemRes.discountPrice()))
			.andExpect(jsonPath("$.originalPrice").value(itemRes.originalPrice()))
			.andExpect(jsonPath("$.description").value(itemRes.description()))
			.andExpect(jsonPath("$.information").value(itemRes.information()))
			.andExpect(jsonPath("$.image").value(itemRes.image()))
			.andExpect(jsonPath("$.storeInfoRes.storeNumber").value(storeInfoRes.storeNumber()))
			.andExpect(jsonPath("$.storeInfoRes.name").value(storeInfoRes.name()))
			.andExpect(jsonPath("$.storeInfoRes.telephone").value(storeInfoRes.telephone()))
			.andExpect(jsonPath("$.storeInfoRes.addressName").value(storeInfoRes.addressName()))
			.andExpect(jsonPath("$.storeInfoRes.openTime").value(storeInfoRes.openTime().toString()))
			.andExpect(jsonPath("$.storeInfoRes.closeTime").value(storeInfoRes.closeTime().toString()))
			.andExpect(jsonPath("$.storeInfoRes.dayOff").value(storeInfoRes.dayOff().stream().map(DayOff::getName).collect(Collectors.toList())))
			.andExpect(jsonPath("$.storeInfoRes.storeStatus").value(storeInfoRes.storeStatus().getName()))
			.andExpect(jsonPath("$.storeInfoRes.image").value(storeInfoRes.image()))
			.andExpect(jsonPath("$.storeAddressRes.name").value(addressRes.name()))
			.andExpect(jsonPath("$.storeAddressRes.xCoordinate").value(addressRes.xCoordinate()))
			.andExpect(jsonPath("$.storeAddressRes.yCoordinate").value(addressRes.yCoordinate()))
			.andDo(print())
			.andDo(document("item/item-find-by-id",
				Preprocessors.preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				pathParameters(parameterWithName("id").description("상품 ID")),
				responseFields(
					fieldWithPath("itemId").type(NUMBER).description("상품 ID"),
					fieldWithPath("storeId").type(NUMBER).description("업체 ID"),
					fieldWithPath("name").type(STRING).description("상품 이름"),
					fieldWithPath("stock").type(NUMBER).description("재고 수"),
					fieldWithPath("discountPrice").type(NUMBER).description("할인가"),
					fieldWithPath("originalPrice").type(NUMBER).description("원가"),
					fieldWithPath("description").type(STRING).description("상세 설명"),
					fieldWithPath("information").type(STRING).description("안내 사항"),
					fieldWithPath("image").type(STRING).description("상품 이미지 경로"),
					fieldWithPath("storeInfoRes.storeNumber").type(STRING).description("사업자 등록 번호"),
					fieldWithPath("storeInfoRes.name").type(STRING).description("상호명"),
					fieldWithPath("storeInfoRes.telephone").type(STRING).description("업체 전화번호"),
					fieldWithPath("storeInfoRes.addressName").type(STRING).description("업체 주소"),
					fieldWithPath("storeInfoRes.openTime").type(STRING).description("오픈 시간"),
					fieldWithPath("storeInfoRes.closeTime").type(STRING).description("마감 시간"),
					fieldWithPath("storeInfoRes.dayOff").type(ARRAY).description("휴무일"),
					fieldWithPath("storeInfoRes.storeStatus").type(STRING).description("영업 유무"),
					fieldWithPath("storeInfoRes.image").type(STRING).description("업체 이미지 경로"),
					fieldWithPath("storeAddressRes.name").type(STRING).description("업체 주소 URL"),
					fieldWithPath("storeAddressRes.xCoordinate").type(NUMBER).description("업체 주소 경도"),
					fieldWithPath("storeAddressRes.yCoordinate").type(NUMBER).description("업체 주소 위도")
				)
			));
	}

	@DisplayName("상품 상세 정보 조회(단건) 실패 테스트 - 상품이 존재하지 않는 경우")
	@Test
	public void itemFindByIdFailureTest_notFoundItem() throws Exception {
		//given
		Long itemId = 1L;
		doThrow(new EntityNotFoundException(NOT_FOUND_ITEM)).when(
			itemService).findById(any());

		//when
		//then
		mockMvc.perform(RestDocumentationRequestBuilders.get("/api/items/{id}", itemId)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound())
			.andDo(print())
			.andDo(document("item/item-find-by-id-fail-not-found-item",
				Preprocessors.preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				pathParameters(parameterWithName("id").description("상품 ID")),
				responseFields(
					fieldWithPath("timestamp").type(STRING).description("예외 시간"),
					fieldWithPath("code").type(STRING).description("예외 코드"),
					fieldWithPath("errors[]").type(ARRAY).description("오류 목록"),
					fieldWithPath("message").type(STRING).description("오류 메시지")
				)
			));
	}

	@DisplayName("상품 목록 조회(업체 시점) 성공 테스트")
	@Test
	public void itemFindAllForStoreSuccessTest() throws Exception {
		//given
		Item item3 = Item.builder()
			.name("치즈김밥")
			.stock(4)
			.discountPrice(4000)
			.originalPrice(4500)
			.description("치즈김밥 입니다.")
			.information("통신사 할인 불가능 합니다.")
			.image("https://fake-image.com/item3.png")
			.store(store)
			.build();

		Long memberId = 1L;

		int size = 5;
		int page = 0;
		PageRequest pageRequest = PageRequest.of(page, size);

		StoreInfoRes storeInfoRes = new StoreInfoRes(store.getStoreNumber(), store.getName(), store.getTelephone(), store.getAddress().getName(), store.getOpenTime(), store.getCloseTime(), store.getDayOffs(), store.getStoreStatus(), store.getImage());
		AddressRes addressRes = new AddressRes(address.getName(), address.getXCoordinate(), address.getYCoordinate());

		ItemRes itemRes = new ItemRes(1L, 1L, item.getName(), item.getStock(), item.getDiscountPrice(), item.getOriginalPrice(), item.getDescription(), item.getInformation(), item.getImage(), storeInfoRes, addressRes);
		ItemRes itemRes3 = new ItemRes(2L, 1L, item3.getName(), item3.getStock(), item3.getDiscountPrice(), item3.getOriginalPrice(), item3.getDescription(), item3.getInformation(), item3.getImage(), storeInfoRes, addressRes);
		List<ItemRes> itemResList = List.of(itemRes, itemRes3);
		ItemsRes itemsRes = new ItemsRes(itemResList);

		when(itemService.findAllForStore(any(), eq(pageRequest))).thenReturn(itemsRes);

		//when
		//then
		mockMvc.perform(RestDocumentationRequestBuilders.get("/api/items/stores")
				.contentType(MediaType.APPLICATION_JSON)
				.param("memberId", memberId.toString())
				.param("size", String.valueOf(size))
				.param("page", String.valueOf(page)))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("item/item-find-All-for-store",
				Preprocessors.preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestParameters(
					List.of(parameterWithName("memberId").description("고객 ID"),
						parameterWithName("size").description("한 페이지 당 상품 목록 개수"),
						parameterWithName("page").description("페이지 번호")
					)),
				responseFields(
					fieldWithPath("itemResponses").type(ARRAY).description("상품 목록"),
					fieldWithPath("itemResponses[0].itemId").type(NUMBER).description("상품 ID"),
					fieldWithPath("itemResponses[0].storeId").type(NUMBER).description("업체 ID"),
					fieldWithPath("itemResponses[0].name").description("상품 이름"),
					fieldWithPath("itemResponses[0].stock").type(NUMBER).description("재고 수"),
					fieldWithPath("itemResponses[0].discountPrice").type(NUMBER).description("할인가"),
					fieldWithPath("itemResponses[0].originalPrice").type(NUMBER).description("원가"),
					fieldWithPath("itemResponses[0].description").type(STRING).description("상세 설명"),
					fieldWithPath("itemResponses[0].information").type(STRING).description("안내 사항"),
					fieldWithPath("itemResponses[0].image").type(STRING).description("상품 이미지 경로"),
					fieldWithPath("itemResponses[0].storeInfoRes.storeNumber").type(STRING).description("사업자 등록 번호"),
					fieldWithPath("itemResponses[0].storeInfoRes.name").type(STRING).description("상호명"),
					fieldWithPath("itemResponses[0].storeInfoRes.telephone").type(STRING).description("업체 전화번호"),
					fieldWithPath("itemResponses[0].storeInfoRes.addressName").type(STRING).description("업체 주소"),
					fieldWithPath("itemResponses[0].storeInfoRes.openTime").type(STRING).description("오픈 시간"),
					fieldWithPath("itemResponses[0].storeInfoRes.closeTime").type(STRING).description("마감 시간"),
					fieldWithPath("itemResponses[0].storeInfoRes.dayOff").type(ARRAY).description("휴무일"),
					fieldWithPath("itemResponses[0].storeInfoRes.storeStatus").type(STRING).description("영업 유무"),
					fieldWithPath("itemResponses[0].storeInfoRes.image").type(STRING).description("업체 이미지 경로"),
					fieldWithPath("itemResponses[0].storeAddressRes.name").type(STRING).description("업체 주소 URL"),
					fieldWithPath("itemResponses[0].storeAddressRes.xCoordinate").type(NUMBER).description("업체 주소 경도"),
					fieldWithPath("itemResponses[0].storeAddressRes.yCoordinate").type(NUMBER).description("업체 주소 위도")
				)
			));
	}

	@DisplayName("상품 목록 조회(고객 시점) - 마감순 성공 테스트")
	@Test
	public void itemFindAllForMemberSuccessSortByDeadlineTest() throws Exception {
		//given
		double xCoordinate = 127.0221068;
		double yCoordinate = 37.5912999;
		String sortBy = "deadline";

		int size = 5;
		int page = 0;
		PageRequest pageRequest = PageRequest.of(page, size);

		StoreInfoRes storeInfoRes = new StoreInfoRes(store.getStoreNumber(), store.getName(), store.getTelephone(), store.getAddress().getName(), store.getOpenTime(), store.getCloseTime(), store.getDayOffs(), store.getStoreStatus(), store.getImage());
		AddressRes addressRes = new AddressRes(address.getName(), address.getXCoordinate(), address.getYCoordinate());

		StoreInfoRes storeInfoRes2 = new StoreInfoRes(store2.getStoreNumber(), store2.getName(), store2.getTelephone(), store2.getAddress().getName(), store2.getOpenTime(), store2.getCloseTime(), store2.getDayOffs(), store2.getStoreStatus(), store2.getImage());
		AddressRes addressRes2 = new AddressRes(address2.getName(), address2.getXCoordinate(), address2.getYCoordinate());

		ItemRes itemRes = new ItemRes(1L, 1L, item.getName(), item.getStock(), item.getDiscountPrice(), item.getOriginalPrice(), item.getDescription(), item.getInformation(), item.getImage(), storeInfoRes, addressRes);
		ItemRes itemRes2 = new ItemRes(2L, 2L, item2.getName(), item2.getStock(), item2.getDiscountPrice(), item2.getOriginalPrice(), item2.getDescription(), item2.getInformation(), item2.getImage(), storeInfoRes2, addressRes2);
		List<ItemRes> itemResList = List.of(itemRes, itemRes2);
		ItemsRes itemsRes = new ItemsRes(itemResList);

		when(itemService.findAllForMember(anyDouble(), anyDouble(), eq(sortBy), eq(pageRequest))).thenReturn(itemsRes);

		//when
		//then
		mockMvc.perform(RestDocumentationRequestBuilders.get("/api/items/members")
				.contentType(MediaType.APPLICATION_JSON)
				.param("x-coordinate", String.valueOf(xCoordinate))
				.param("y-coordinate", String.valueOf(yCoordinate))
				.param("sort-by", sortBy)
				.param("size", String.valueOf(size))
				.param("page", String.valueOf(page)))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("item/item-find-All-for-member-sort-by-deadline",
				Preprocessors.preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestParameters(
					List.of(parameterWithName("x-coordinate").description("경도"),
						parameterWithName("y-coordinate").description("위도"),
						parameterWithName("sort-by").description("정렬 기준(마감순)"),
						parameterWithName("size").description("한 페이지 당 상품 목록 개수"),
						parameterWithName("page").description("페이지 번호")
					)),
				responseFields(
					fieldWithPath("itemResponses").type(ARRAY).description("상품 목록"),
					fieldWithPath("itemResponses[0].itemId").type(NUMBER).description("상품 ID"),
					fieldWithPath("itemResponses[0].storeId").type(NUMBER).description("업체 ID"),
					fieldWithPath("itemResponses[0].name").description("상품 이름"),
					fieldWithPath("itemResponses[0].stock").type(NUMBER).description("재고 수"),
					fieldWithPath("itemResponses[0].discountPrice").type(NUMBER).description("할인가"),
					fieldWithPath("itemResponses[0].originalPrice").type(NUMBER).description("원가"),
					fieldWithPath("itemResponses[0].description").type(STRING).description("상세 설명"),
					fieldWithPath("itemResponses[0].information").type(STRING).description("안내 사항"),
					fieldWithPath("itemResponses[0].image").type(STRING).description("상품 이미지 경로"),
					fieldWithPath("itemResponses[0].storeInfoRes.storeNumber").type(STRING).description("사업자 등록 번호"),
					fieldWithPath("itemResponses[0].storeInfoRes.name").type(STRING).description("상호명"),
					fieldWithPath("itemResponses[0].storeInfoRes.telephone").type(STRING).description("업체 전화번호"),
					fieldWithPath("itemResponses[0].storeInfoRes.addressName").type(STRING).description("업체 주소"),
					fieldWithPath("itemResponses[0].storeInfoRes.openTime").type(STRING).description("오픈 시간"),
					fieldWithPath("itemResponses[0].storeInfoRes.closeTime").type(STRING).description("마감 시간"),
					fieldWithPath("itemResponses[0].storeInfoRes.dayOff").type(ARRAY).description("휴무일"),
					fieldWithPath("itemResponses[0].storeInfoRes.storeStatus").type(STRING).description("영업 유무"),
					fieldWithPath("itemResponses[0].storeInfoRes.image").type(STRING).description("업체 이미지 경로"),
					fieldWithPath("itemResponses[0].storeAddressRes.name").type(STRING).description("업체 주소 URL"),
					fieldWithPath("itemResponses[0].storeAddressRes.xCoordinate").type(NUMBER).description("업체 주소 경도"),
					fieldWithPath("itemResponses[0].storeAddressRes.yCoordinate").type(NUMBER).description("업체 주소 위도")
				)
			));
	}

	@DisplayName("상품 목록 조회(고객 시점) - 할인율순 성공 테스트")
	@Test
	public void itemFindAllForMemberSuccessSortByDiscountRateTest() throws Exception {
		//given
		double xCoordinate = 127.0221068;
		double yCoordinate = 37.5912999;
		String sortBy = "discount-rate";

		int size = 5;
		int page = 0;
		PageRequest pageRequest = PageRequest.of(page, size);

		StoreInfoRes storeInfoRes = new StoreInfoRes(store.getStoreNumber(), store.getName(), store.getTelephone(), store.getAddress().getName(), store.getOpenTime(), store.getCloseTime(), store.getDayOffs(), store.getStoreStatus(), store.getImage());
		AddressRes addressRes = new AddressRes(address.getName(), address.getXCoordinate(), address.getYCoordinate());

		StoreInfoRes storeInfoRes2 = new StoreInfoRes(store2.getStoreNumber(), store2.getName(), store2.getTelephone(), store2.getAddress().getName(), store2.getOpenTime(), store2.getCloseTime(), store2.getDayOffs(), store2.getStoreStatus(), store2.getImage());
		AddressRes addressRes2 = new AddressRes(address2.getName(), address2.getXCoordinate(), address2.getYCoordinate());

		ItemRes itemRes = new ItemRes(1L, 1L, item.getName(), item.getStock(), item.getDiscountPrice(), item.getOriginalPrice(), item.getDescription(), item.getInformation(), item.getImage(), storeInfoRes, addressRes);
		ItemRes itemRes2 = new ItemRes(2L, 2L, item2.getName(), item2.getStock(), item2.getDiscountPrice(), item2.getOriginalPrice(), item2.getDescription(), item2.getInformation(), item2.getImage(), storeInfoRes2, addressRes2);
		List<ItemRes> itemResList = List.of(itemRes, itemRes2);
		ItemsRes itemsRes = new ItemsRes(itemResList);

		when(itemService.findAllForMember(anyDouble(), anyDouble(), eq(sortBy), eq(pageRequest))).thenReturn(itemsRes);

		//when
		//then
		mockMvc.perform(RestDocumentationRequestBuilders.get("/api/items/members")
				.contentType(MediaType.APPLICATION_JSON)
				.param("x-coordinate", String.valueOf(xCoordinate))
				.param("y-coordinate", String.valueOf(yCoordinate))
				.param("sort-by", sortBy)
				.param("size", String.valueOf(size))
				.param("page", String.valueOf(page)))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("item/item-find-All-for-member-sort-by-discount-rate",
				Preprocessors.preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestParameters(
					List.of(parameterWithName("x-coordinate").description("경도"),
						parameterWithName("y-coordinate").description("위도"),
						parameterWithName("sort-by").description("정렬 기준(할인율순)"),
						parameterWithName("size").description("한 페이지 당 상품 목록 개수"),
						parameterWithName("page").description("페이지 번호")
					)),
				responseFields(
					fieldWithPath("itemResponses").type(ARRAY).description("상품 목록"),
					fieldWithPath("itemResponses[0].itemId").type(NUMBER).description("상품 ID"),
					fieldWithPath("itemResponses[0].storeId").type(NUMBER).description("업체 ID"),
					fieldWithPath("itemResponses[0].name").description("상품 이름"),
					fieldWithPath("itemResponses[0].stock").type(NUMBER).description("재고 수"),
					fieldWithPath("itemResponses[0].discountPrice").type(NUMBER).description("할인가"),
					fieldWithPath("itemResponses[0].originalPrice").type(NUMBER).description("원가"),
					fieldWithPath("itemResponses[0].description").type(STRING).description("상세 설명"),
					fieldWithPath("itemResponses[0].information").type(STRING).description("안내 사항"),
					fieldWithPath("itemResponses[0].image").type(STRING).description("상품 이미지 경로"),
					fieldWithPath("itemResponses[0].storeInfoRes.storeNumber").type(STRING).description("사업자 등록 번호"),
					fieldWithPath("itemResponses[0].storeInfoRes.name").type(STRING).description("상호명"),
					fieldWithPath("itemResponses[0].storeInfoRes.telephone").type(STRING).description("업체 전화번호"),
					fieldWithPath("itemResponses[0].storeInfoRes.addressName").type(STRING).description("업체 주소"),
					fieldWithPath("itemResponses[0].storeInfoRes.openTime").type(STRING).description("오픈 시간"),
					fieldWithPath("itemResponses[0].storeInfoRes.closeTime").type(STRING).description("마감 시간"),
					fieldWithPath("itemResponses[0].storeInfoRes.dayOff").type(ARRAY).description("휴무일"),
					fieldWithPath("itemResponses[0].storeInfoRes.storeStatus").type(STRING).description("영업 유무"),
					fieldWithPath("itemResponses[0].storeInfoRes.image").type(STRING).description("업체 이미지 경로"),
					fieldWithPath("itemResponses[0].storeAddressRes.name").type(STRING).description("업체 주소 URL"),
					fieldWithPath("itemResponses[0].storeAddressRes.xCoordinate").type(NUMBER).description("업체 주소 경도"),
					fieldWithPath("itemResponses[0].storeAddressRes.yCoordinate").type(NUMBER).description("업체 주소 위도")
				)
			));
	}

	@DisplayName("상품 목록 조회(고객 시점) - 거리순 성공 테스트")
	@Test
	public void itemFindAllForMemberSuccessSortByDistanceRateTest() throws Exception {
		//given
		double xCoordinate = 127.0221068;
		double yCoordinate = 37.5912999;
		String sortBy = "distance";

		int size = 5;
		int page = 0;
		PageRequest pageRequest = PageRequest.of(page, size);

		StoreInfoRes storeInfoRes = new StoreInfoRes(store.getStoreNumber(), store.getName(), store.getTelephone(), store.getAddress().getName(), store.getOpenTime(), store.getCloseTime(), store.getDayOffs(), store.getStoreStatus(), store.getImage());
		AddressRes addressRes = new AddressRes(address.getName(), address.getXCoordinate(), address.getYCoordinate());

		StoreInfoRes storeInfoRes2 = new StoreInfoRes(store2.getStoreNumber(), store2.getName(), store2.getTelephone(), store2.getAddress().getName(), store2.getOpenTime(), store2.getCloseTime(), store2.getDayOffs(), store2.getStoreStatus(), store2.getImage());
		AddressRes addressRes2 = new AddressRes(address2.getName(), address2.getXCoordinate(), address2.getYCoordinate());

		ItemRes itemRes = new ItemRes(1L, 1L, item.getName(), item.getStock(), item.getDiscountPrice(), item.getOriginalPrice(), item.getDescription(), item.getInformation(), item.getImage(), storeInfoRes, addressRes);
		ItemRes itemRes2 = new ItemRes(2L, 2L, item2.getName(), item2.getStock(), item2.getDiscountPrice(), item2.getOriginalPrice(), item2.getDescription(), item2.getInformation(), item2.getImage(), storeInfoRes2, addressRes2);
		List<ItemRes> itemResList = List.of(itemRes, itemRes2);
		ItemsRes itemsRes = new ItemsRes(itemResList);

		when(itemService.findAllForMember(anyDouble(), anyDouble(), eq(sortBy), eq(pageRequest))).thenReturn(itemsRes);

		//when
		//then
		mockMvc.perform(RestDocumentationRequestBuilders.get("/api/items/members")
				.contentType(MediaType.APPLICATION_JSON)
				.param("x-coordinate", String.valueOf(xCoordinate))
				.param("y-coordinate", String.valueOf(yCoordinate))
				.param("sort-by", sortBy)
				.param("size", String.valueOf(size))
				.param("page", String.valueOf(page)))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("item/item-find-All-for-member-sort-by-distance",
				Preprocessors.preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestParameters(
					List.of(parameterWithName("x-coordinate").description("경도"),
						parameterWithName("y-coordinate").description("위도"),
						parameterWithName("sort-by").description("정렬 기준(거리순)"),
						parameterWithName("size").description("한 페이지 당 상품 목록 개수"),
						parameterWithName("page").description("페이지 번호")
					)),
				responseFields(
					fieldWithPath("itemResponses").type(ARRAY).description("상품 목록"),
					fieldWithPath("itemResponses[0].itemId").type(NUMBER).description("상품 ID"),
					fieldWithPath("itemResponses[0].storeId").type(NUMBER).description("업체 ID"),
					fieldWithPath("itemResponses[0].name").description("상품 이름"),
					fieldWithPath("itemResponses[0].stock").type(NUMBER).description("재고 수"),
					fieldWithPath("itemResponses[0].discountPrice").type(NUMBER).description("할인가"),
					fieldWithPath("itemResponses[0].originalPrice").type(NUMBER).description("원가"),
					fieldWithPath("itemResponses[0].description").type(STRING).description("상세 설명"),
					fieldWithPath("itemResponses[0].information").type(STRING).description("안내 사항"),
					fieldWithPath("itemResponses[0].image").type(STRING).description("상품 이미지 경로"),
					fieldWithPath("itemResponses[0].storeInfoRes.storeNumber").type(STRING).description("사업자 등록 번호"),
					fieldWithPath("itemResponses[0].storeInfoRes.name").type(STRING).description("상호명"),
					fieldWithPath("itemResponses[0].storeInfoRes.telephone").type(STRING).description("업체 전화번호"),
					fieldWithPath("itemResponses[0].storeInfoRes.addressName").type(STRING).description("업체 주소"),
					fieldWithPath("itemResponses[0].storeInfoRes.openTime").type(STRING).description("오픈 시간"),
					fieldWithPath("itemResponses[0].storeInfoRes.closeTime").type(STRING).description("마감 시간"),
					fieldWithPath("itemResponses[0].storeInfoRes.dayOff").type(ARRAY).description("휴무일"),
					fieldWithPath("itemResponses[0].storeInfoRes.storeStatus").type(STRING).description("영업 유무"),
					fieldWithPath("itemResponses[0].storeInfoRes.image").type(STRING).description("업체 이미지 경로"),
					fieldWithPath("itemResponses[0].storeAddressRes.name").type(STRING).description("업체 주소 URL"),
					fieldWithPath("itemResponses[0].storeAddressRes.xCoordinate").type(NUMBER).description("업체 주소 경도"),
					fieldWithPath("itemResponses[0].storeAddressRes.yCoordinate").type(NUMBER).description("업체 주소 위도")
				)
			));
	}

//	@DisplayName("상품 수정 성공 테스트")
//	@Test
//	public void itemUpdateSuccessTest() throws Exception {
//		//given
//		Long itemId = 1L;
//		Long memberId = 1L;
//
//		ItemReq itemReq = new ItemReq("치즈 떡볶이", 4, 9900, 14000, "치즈 떡볶이 입니다.", "통신사 할인 가능 합니다.", null);
//		ItemRes itemRes = new ItemRes(itemId, 1L, itemReq.name(), itemReq.stock(), itemReq.discountPrice(),
//			itemReq.originalPrice(), itemReq.description(), itemReq.information(), itemReq.image());
//
//		when(itemService.update(any(), any(), any())).thenReturn(itemRes);
//
//		//when
//		//then
//		mockMvc.perform(RestDocumentationRequestBuilders.patch("/api/items/{id}", itemId)
//				.contentType(MediaType.APPLICATION_JSON)
//				.content(objectMapper.writeValueAsString(itemReq))
//				.param("memberId", memberId.toString()))
//			.andExpect(status().isOk())
//			.andExpect(jsonPath("$.name").value(itemRes.name()))
//			.andExpect(jsonPath("$.stock").value(itemRes.stock()))
//			.andExpect(jsonPath("$.discountPrice").value(itemRes.discountPrice()))
//			.andExpect(jsonPath("$.originalPrice").value(itemRes.originalPrice()))
//			.andExpect(jsonPath("$.description").value(itemRes.description()))
//			.andExpect(jsonPath("$.information").value(itemRes.information()))
//			.andExpect(jsonPath("$.image").value(itemRes.image()))
//			.andDo(print())
//			.andDo(document("item/item-update",
//				Preprocessors.preprocessRequest(prettyPrint()),
//				preprocessResponse(prettyPrint()),
//				pathParameters(parameterWithName("id").description("상품 ID")),
//				requestParameters(parameterWithName("memberId").description("고객 ID")),
//				requestFields(
//					fieldWithPath("name").description("상품 이름"),
//					fieldWithPath("stock").description("재고 수"),
//					fieldWithPath("discountPrice").description("할인가"),
//					fieldWithPath("originalPrice").description("원가"),
//					fieldWithPath("description").description("상세 설명"),
//					fieldWithPath("information").description("안내 사항"),
//					fieldWithPath("image").description("상품 이미지")
//				),
//				responseFields(
//					fieldWithPath("itemId").type(NUMBER).description("상품 ID"),
//					fieldWithPath("storeId").type(NUMBER).description("업체 ID"),
//					fieldWithPath("name").type(STRING).description("상품 이름"),
//					subsectionWithPath("stock").type(NUMBER).description("재고 수"),
//					fieldWithPath("discountPrice").type(NUMBER).description("할인가"),
//					fieldWithPath("originalPrice").type(NUMBER).description("원가"),
//					fieldWithPath("description").type(STRING).description("상세 설명"),
//					fieldWithPath("information").type(STRING).description("안내 사항"),
//					fieldWithPath("image").type(NULL).description("상품 이미지")
//				)
//			));
//	}
//
//	@DisplayName("상품 수정 실패 테스트 - 입력되지 않은 상품 이름")
//	@Test
//	public void itemUpdateFailureTest_invalidName() throws Exception {
//		//given
//		Long itemId = 1L;
//		Long memberId = 1L;
//
//		Item item2 = Item.builder()
//			.name("")
//			.stock(4)
//			.discountPrice(9900)
//			.originalPrice(14500)
//			.description("치즈 떡볶이 입니다.")
//			.information("통신사 할인 가능 합니다.")
//			.store(store)
//			.build();
//
//		ItemReq itemReq = new ItemReq(item2.getName(), item2.getStock(), item2.getDiscountPrice(),
//			item2.getOriginalPrice(), item2.getDescription(), item2.getInformation(), item2.getImage());
//		ItemRes itemRes = new ItemRes(itemId, 1L, itemReq.name(), itemReq.stock(), itemReq.discountPrice(),
//			itemReq.originalPrice(), itemReq.description(), itemReq.information(), itemReq.image());
//
//		when(itemService.update(any(), any(), any())).thenReturn(itemRes);
//
//		//when
//		//then
//		mockMvc.perform(RestDocumentationRequestBuilders.patch("/api/items/{id}", itemId)
//				.contentType(MediaType.APPLICATION_JSON)
//				.content(objectMapper.writeValueAsString(itemReq))
//				.param("memberId", memberId.toString()))
//			.andExpect(status().isBadRequest())
//			.andExpect(jsonPath("$.timestamp").isNotEmpty())
//			.andExpect(jsonPath("$.code").value("C001"))
//			.andExpect(jsonPath("$.errors[0].field").value("name"))
//			.andExpect(jsonPath("$.errors[0].value").value(""))
//			.andExpect(jsonPath("$.errors[0].reason").isNotEmpty())
//			.andExpect(jsonPath("$.errors[1].field").value("name"))
//			.andExpect(jsonPath("$.errors[1].value").value(""))
//			.andExpect(jsonPath("$.errors[1].reason").isNotEmpty())
//			.andExpect(jsonPath("$.message").value("잘못된 값을 입력하셨습니다."))
//			.andDo(print())
//			.andDo(document("item/item-update-fail-invalid-name",
//				Preprocessors.preprocessRequest(prettyPrint()),
//				preprocessResponse(prettyPrint()),
//				pathParameters(parameterWithName("id").description("상품 ID")),
//				requestParameters(parameterWithName("memberId").description("고객 ID")),
//				requestFields(
//					fieldWithPath("name").description("상품 이름"),
//					fieldWithPath("stock").description("재고 수"),
//					fieldWithPath("discountPrice").description("할인가"),
//					fieldWithPath("originalPrice").description("원가"),
//					fieldWithPath("description").description("상세 설명"),
//					fieldWithPath("information").description("안내 사항"),
//					fieldWithPath("image").description("상품 이미지")
//				),
//				responseFields(
//					fieldWithPath("timestamp").type(STRING).description("예외 시간"),
//					fieldWithPath("code").type(STRING).description("예외 코드"),
//					fieldWithPath("errors[]").type(ARRAY).description("오류 목록"),
//					fieldWithPath("errors[].field").type(STRING).description("오류 필드"),
//					fieldWithPath("errors[].value").type(STRING).description("오류 값"),
//					fieldWithPath("errors[].reason").type(STRING).description("오류 사유"),
//					fieldWithPath("message").type(STRING).description("오류 메시지")
//				)
//			));
//	}
//
//	@DisplayName("상품 수정 실패 테스트 - 할인가가 원가보다 큰 경우")
//	@Test
//	public void itemUpdateFailureTest_invalidDiscountPrice() throws Exception {
//		//given
//		ItemReq itemReq = new ItemReq("치즈 떡볶이", 4, 10000, 5000, "치즈 떡볶이 입니다.", "통신사 할인 가능 합니다.", null);
//
//		Long memberId = 1L;
//		Long itemId = 1L;
//
//		when(itemService.update(any(), any(), any())).thenThrow(
//			new BusinessException(ErrorCode.INVALID_ITEM_DISCOUNT_PRICE));
//
//		//when
//		//then
//		mockMvc.perform(RestDocumentationRequestBuilders.patch("/api/items/{id}", itemId)
//				.contentType(MediaType.APPLICATION_JSON)
//				.content(objectMapper.writeValueAsString(itemReq))
//				.param("memberId", memberId.toString()))
//			.andExpect(status().isBadRequest())
//			.andExpect(jsonPath("$.timestamp").isNotEmpty())
//			.andExpect(jsonPath("$.code").value("I001"))
//			.andExpect(jsonPath("$.errors").isEmpty())
//			.andExpect(jsonPath("$.message").value("상품 할인가는 원가보다 클 수 없습니다."))
//			.andDo(print())
//			.andDo(document("item/item-update-fail-invalid-discount-price",
//				Preprocessors.preprocessRequest(prettyPrint()),
//				preprocessResponse(prettyPrint()),
//				pathParameters(parameterWithName("id").description("상품 ID")),
//				requestParameters(parameterWithName("memberId").description("고객 ID")),
//				requestFields(
//					fieldWithPath("name").description("상품 이름"),
//					fieldWithPath("stock").description("재고 수"),
//					fieldWithPath("discountPrice").description("할인가"),
//					fieldWithPath("originalPrice").description("원가"),
//					fieldWithPath("description").description("상세 설명"),
//					fieldWithPath("information").description("안내 사항"),
//					fieldWithPath("image").description("상품 이미지")
//				),
//				responseFields(
//					fieldWithPath("timestamp").type(STRING).description("예외 시간"),
//					fieldWithPath("code").type(STRING).description("예외 코드"),
//					fieldWithPath("errors[]").type(ARRAY).description("오류 목록"),
//					fieldWithPath("message").type(STRING).description("오류 메시지")
//				)
//			));
//	}
//
//	@DisplayName("상품 수정 실패 테스트 - 이미 등록된 상품인 경우(이름 중복)")
//	@Test
//	public void itemUpdateFailureTest_duplicatedItemName() throws Exception {
//		//given
//		ItemReq itemReq = new ItemReq("떡볶이", 2, 4500, 4000, "기본 떡볶이 입니다.", "통신사 할인 불가능 합니다.", null);
//
//		Long memberId = 1L;
//		Long itemId = 1L;
//
//		doThrow(new BusinessException(DUPLICATED_ITEM_NAME)).when(
//			itemService).update(any(), any(), any());
//
//		//when
//		//then
//		mockMvc.perform(RestDocumentationRequestBuilders.patch("/api/items/{id}", itemId)
//				.contentType(MediaType.APPLICATION_JSON)
//				.content(objectMapper.writeValueAsString(itemReq))
//				.param("memberId", memberId.toString()))
//			.andExpect(status().isBadRequest())
//			.andExpect(jsonPath("$.timestamp").isNotEmpty())
//			.andExpect(jsonPath("$.code").value("I003"))
//			.andExpect(jsonPath("$.errors").isEmpty())
//			.andExpect(jsonPath("$.message").value("동일한 이름을 가진 상품이 이미 등록되어 있습니다."))
//			.andDo(print())
//			.andDo(document("item/item-update-fail-duplicated-item-name",
//				Preprocessors.preprocessRequest(prettyPrint()),
//				preprocessResponse(prettyPrint()),
//				pathParameters(parameterWithName("id").description("상품 ID")),
//				requestParameters(parameterWithName("memberId").description("고객 ID")),
//				requestFields(
//					fieldWithPath("name").description("상품 이름"),
//					fieldWithPath("stock").description("재고 수"),
//					fieldWithPath("discountPrice").description("할인가"),
//					fieldWithPath("originalPrice").description("원가"),
//					fieldWithPath("description").description("상세 설명"),
//					fieldWithPath("information").description("안내 사항"),
//					fieldWithPath("image").description("상품 이미지")
//				),
//				responseFields(
//					fieldWithPath("timestamp").type(STRING).description("예외 시간"),
//					fieldWithPath("code").type(STRING).description("예외 코드"),
//					fieldWithPath("errors[]").type(ARRAY).description("오류 목록"),
//					fieldWithPath("message").type(STRING).description("오류 메시지")
//				)
//			));
//	}

	@DisplayName("상품 삭제 성공 테스트")
	@Test
	public void itemDeleteSuccessTest() throws Exception {
		//given
		Long itemId = 1L;
		Long memberId = 1L;

		doNothing().when(itemService).delete(any(), any());

		//when
		//then
		mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/items/{id}", itemId)
				.contentType(MediaType.APPLICATION_JSON)
				.param("memberId", memberId.toString()))
			.andExpect(status().isNoContent())
			.andDo(print())
			.andDo(document("item/item-delete",
				Preprocessors.preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				pathParameters(parameterWithName("id").description("상품 ID")),
				requestParameters(parameterWithName("memberId").description("고객 ID"))
			));
	}

	@DisplayName("상품 삭제 실패 테스트 - 요청된 상품이 해당 업체에 등록되지 않은 상품인 경우")
	@Test
	public void itemDeleteFailureTest_storeHasNoItem() throws Exception {
		//given
		Long itemId = 1L;
		Long memberId = 1L;

		doThrow(new BusinessException(STORE_HAS_NO_ITEM)).when(
			itemService).delete(any(), any());

		//when
		//then
		mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/items/{id}", itemId)
				.contentType(MediaType.APPLICATION_JSON)
				.param("memberId", memberId.toString()))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.timestamp").isNotEmpty())
			.andExpect(jsonPath("$.code").value("I005"))
			.andExpect(jsonPath("$.errors").isEmpty())
			.andExpect(jsonPath("$.message").value("요청하신 상품은 해당 업체에 등록되지 않은 상품입니다."))
			.andDo(print())
			.andDo(document("item/item-delete-store-has-no-item",
				Preprocessors.preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				pathParameters(parameterWithName("id").description("상품 ID")),
				requestParameters(parameterWithName("memberId").description("고객 ID")),
				responseFields(
					fieldWithPath("timestamp").type(STRING).description("예외 시간"),
					fieldWithPath("code").type(STRING).description("예외 코드"),
					fieldWithPath("errors[]").type(ARRAY).description("오류 목록"),
					fieldWithPath("message").type(STRING).description("오류 메시지")
				)
			));
	}
}
