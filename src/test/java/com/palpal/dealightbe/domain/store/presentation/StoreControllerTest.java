package com.palpal.dealightbe.domain.store.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
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

import java.time.LocalTime;
import java.util.List;
import java.util.Set;

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
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.palpal.dealightbe.config.SecurityConfig;
import com.palpal.dealightbe.domain.address.application.dto.response.AddressRes;
import com.palpal.dealightbe.domain.image.application.dto.request.ImageUploadReq;
import com.palpal.dealightbe.domain.image.application.dto.response.ImageRes;
import com.palpal.dealightbe.domain.store.application.StoreService;
import com.palpal.dealightbe.domain.store.application.dto.request.StoreCreateReq;
import com.palpal.dealightbe.domain.store.application.dto.request.StoreStatusReq;
import com.palpal.dealightbe.domain.store.application.dto.request.StoreUpdateReq;
import com.palpal.dealightbe.domain.store.application.dto.response.StoreByMemberRes;
import com.palpal.dealightbe.domain.store.application.dto.response.StoreCreateRes;
import com.palpal.dealightbe.domain.store.application.dto.response.StoreInfoRes;
import com.palpal.dealightbe.domain.store.application.dto.response.StoreInfoSliceRes;
import com.palpal.dealightbe.domain.store.application.dto.response.StoreStatusRes;
import com.palpal.dealightbe.domain.store.application.dto.response.StoresInfoSliceRes;
import com.palpal.dealightbe.domain.store.domain.DayOff;
import com.palpal.dealightbe.domain.store.domain.StoreStatus;
import com.palpal.dealightbe.global.error.ErrorCode;
import com.palpal.dealightbe.global.error.exception.BusinessException;
import com.palpal.dealightbe.global.error.exception.EntityNotFoundException;

@WebMvcTest(value = StoreController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class,
	OAuth2ClientAutoConfiguration.class}, excludeFilters = {
	@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)})
@AutoConfigureRestDocs
class StoreControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@MockBean
	StoreService storeService;

	public static final String DEFAULT_PATH = "https://team-08-bucket.s3.ap-northeast-2.amazonaws.com/image/free-store-icon.png";

	@Test
	@DisplayName("업체 등록 성공")
	void registerStoreSuccessTest() throws Exception {
		//given
		LocalTime openTime = LocalTime.of(9, 0);
		LocalTime closeTime = LocalTime.of(23, 0);

		StoreCreateReq storeCreateReq = new StoreCreateReq("888222111", "맛짱조개", "01066772291", "서울시 강남구", 67.89,
			293.2323, openTime, closeTime, Set.of(DayOff.MON));
		AddressRes addressRes = new AddressRes("서울시 강남구", 67.89, 293.2323);
		StoreCreateRes storeCreateRes = new StoreCreateRes(1L, "888222111", "맛짱조개", "01066772291", addressRes, openTime,
			closeTime, Set.of(DayOff.MON), DEFAULT_PATH);

		given(storeService.register(any(), any()))
			.willReturn(storeCreateRes);

		//when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.post("/api/stores")
				.header("Authorization", "Bearer {ACCESS_TOKEN}")
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(storeCreateReq)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.storeNumber").value(storeCreateRes.storeNumber()))
			.andExpect(jsonPath("$.name").value(storeCreateRes.name()))
			.andExpect(jsonPath("$.telephone").value(storeCreateRes.telephone()))
			.andExpect(jsonPath("$.addressRes.name").value(storeCreateRes.addressRes().name()))
			.andExpect(jsonPath("$.openTime").value(storeCreateRes.openTime().toString()))
			.andExpect(jsonPath("$.closeTime").value(storeCreateRes.closeTime().toString()))
			.andExpect(jsonPath("$.dayOff[0]").value(DayOff.MON.getName()))
			.andExpect(jsonPath("$.imageUrl").value(DEFAULT_PATH))
			.andDo(print())
			.andDo(document("store/store-register",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				requestFields(
					fieldWithPath("storeNumber").description("사업자 등록 번호"),
					fieldWithPath("name").description("상호명"),
					fieldWithPath("telephone").description("업체 전화번호"),
					fieldWithPath("addressName").description("업체 주소"),
					fieldWithPath("xCoordinate").description("X 좌표"),
					fieldWithPath("yCoordinate").description("Y 좌표"),
					fieldWithPath("openTime").description("오픈 시간"),
					fieldWithPath("closeTime").description("마감 시간"),
					fieldWithPath("dayOff").description("휴무일")
				),
				responseFields(
					fieldWithPath("id").description("업체 ID"),
					fieldWithPath("storeNumber").description("사업자 등록 번호"),
					fieldWithPath("name").description("상호명"),
					fieldWithPath("telephone").description("업체 전화번호"),
					subsectionWithPath("addressRes").description("주소 정보"),
					fieldWithPath("openTime").description("오픈 시간"),
					fieldWithPath("closeTime").description("마감 시간"),
					fieldWithPath("dayOff").description("휴무일"),
					fieldWithPath("imageUrl").description("이미지 경로")
				)
			));
	}

	@Test
	@DisplayName("업체 등록 실패 - 잘못된 영업 시간")
	void registerStoreFailTest_invalidBusinessTime() throws Exception {

		//given
		LocalTime openTime = LocalTime.of(23, 0);
		LocalTime closeTime = LocalTime.of(9, 0);

		StoreCreateReq storeCreateReq = new StoreCreateReq("888222111", "맛짱조개", "01066772291", "서울시 강남구", 67.89,
			293.2323, openTime, closeTime, Set.of(DayOff.MON));

		given(storeService.register(any(), any()))
			.willThrow(new BusinessException(ErrorCode.INVALID_BUSINESS_TIME));

		//when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.post("/api/stores")
				.header("Authorization", "Bearer {ACCESS_TOKEN}")
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(storeCreateReq)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.timestamp").isNotEmpty())
			.andExpect(jsonPath("$.code").value("ST001"))
			.andExpect(jsonPath("$.errors").isEmpty())
			.andExpect(jsonPath("$.message").value("마감 시간은 오픈 시간보다 이전일 수 없습니다"))
			.andDo(print())
			.andDo(document("store/store-register-fail-invalid-business-time",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				requestFields(
					fieldWithPath("storeNumber").description("사업자 등록 번호"),
					fieldWithPath("name").description("상호명"),
					fieldWithPath("telephone").description("업체 전화번호"),
					fieldWithPath("addressName").description("업체 주소"),
					fieldWithPath("xCoordinate").description("X 좌표"),
					fieldWithPath("yCoordinate").description("Y 좌표"),
					fieldWithPath("openTime").description("오픈 시간"),
					fieldWithPath("closeTime").description("마감 시간"),
					fieldWithPath("dayOff").description("휴무일")
				),
				responseFields(
					fieldWithPath("timestamp").type(STRING).description("예외 시간"),
					fieldWithPath("code").type(STRING).description("오류 코드"),
					fieldWithPath("errors").type(ARRAY).description("오류 목록"),
					fieldWithPath("message").type(STRING).description("오류 메시지")
				)
			));
	}

	@Test
	@DisplayName("업체 마이페이지 조회 성공")
	void getInfoSuccessTest() throws Exception {

		//given
		Long storeId = 1L;
		LocalTime openTime = LocalTime.of(9, 0);
		LocalTime closeTime = LocalTime.of(23, 0);
		StoreInfoRes storeInfoRes = new StoreInfoRes("123123213", "피나치공", "02123456", "서울시 강남구", openTime, closeTime,
			Set.of(DayOff.MON, DayOff.TUE), StoreStatus.OPENED, null);

		given(storeService.getInfo(any(), eq(storeId)))
			.willReturn(storeInfoRes);

		//when -> then
		mockMvc.perform(
				RestDocumentationRequestBuilders.get("/api/stores/profiles/{storeId}", storeId)
					.header("Authorization", "Bearer {ACCESS_TOKEN}")
					.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("store/store-get-info",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				pathParameters(
					parameterWithName("storeId").description("업체 ID")
				),
				responseFields(
					fieldWithPath("storeNumber").description("사업자 등록 번호"),
					fieldWithPath("name").description("상호명"),
					fieldWithPath("telephone").description("업체 전화번호"),
					fieldWithPath("addressName").description("업체 주소"),
					fieldWithPath("openTime").description("오픈 시간"),
					fieldWithPath("closeTime").description("마감 시간"),
					fieldWithPath("dayOff").description("휴무일"),
					fieldWithPath("storeStatus").description("영업 유무"),
					fieldWithPath("image").description("이미지 주소")
				)
			));
	}

	@Test
	@DisplayName("업체 마이페이지 조회 실패 - 소유자와 요청자가 일치하지 않음")
	void getInfoFailTest_notMatchOwnerAndRequester() throws Exception {

		//given
		Long storeId = 1L;
		given(storeService.getInfo(any(), eq(storeId)))
			.willThrow(new BusinessException(ErrorCode.NOT_MATCH_OWNER_AND_REQUESTER));

		//when -> then
		mockMvc.perform(
				RestDocumentationRequestBuilders.get("/api/stores/profiles/{storeId}", storeId)
					.header("Authorization", "Bearer {ACCESS_TOKEN}")
					.contentType(APPLICATION_JSON))
			.andExpect(status().isBadRequest())
			.andDo(print())
			.andDo(document("store/store-get-info-fail-not-match-owner-and-requester",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				pathParameters(
					parameterWithName("storeId").description("업체 ID")
				),
				responseFields(
					fieldWithPath("timestamp").type(STRING).description("예외 시간"),
					fieldWithPath("code").type(STRING).description("오류 코드"),
					fieldWithPath("errors").type(ARRAY).description("오류 목록"),
					fieldWithPath("message").type(STRING).description("오류 메시지")
				)
			));
	}

	@Test
	@DisplayName("업체 마이페이지 정보 수정 성공")
	void updateInfoSuccessTest() throws Exception {

		//given
		Long storeId = 1L;
		LocalTime openTime = LocalTime.of(19, 0);
		LocalTime closeTime = LocalTime.of(22, 0);

		StoreUpdateReq updateReq = new StoreUpdateReq("888222111", "부산시", 777.777, 123.123234, openTime, closeTime,
			Set.of(DayOff.TUE));
		StoreInfoRes storeInfoRes = new StoreInfoRes("888222111", "맛짱조개", "01066772291", "부산시", openTime, closeTime,
			Set.of(DayOff.TUE), StoreStatus.OPENED, null);

		given(storeService.updateInfo(any(), eq(storeId), eq(updateReq)))
			.willReturn(storeInfoRes);

		//when -> then
		mockMvc.perform(
				RestDocumentationRequestBuilders.patch("/api/stores/profiles/{storeId}", storeId)
					.header("Authorization", "Bearer {ACCESS_TOKEN}")
					.contentType(APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(updateReq)))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("store/store-update-info",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				pathParameters(
					parameterWithName("storeId").description("업체 ID")
				),
				responseFields(
					fieldWithPath("storeNumber").description("사업자 등록 번호"),
					fieldWithPath("name").description("상호명"),
					fieldWithPath("telephone").description("업체 전화번호"),
					fieldWithPath("addressName").description("업체 주소"),
					fieldWithPath("openTime").description("오픈 시간"),
					fieldWithPath("closeTime").description("마감 시간"),
					fieldWithPath("dayOff").description("휴무일"),
					fieldWithPath("storeStatus").description("영업 유무"),
					fieldWithPath("image").description("이미지 주소")
				)
			));
	}

	@Test
	@DisplayName("업체 영업 상태 조회 성공")
	void getStatusSuccessTest() throws Exception {

		//given
		Long storeId = 1L;

		StoreStatusRes storeStatusRes = new StoreStatusRes(storeId, StoreStatus.OPENED);

		given(storeService.getStatus(any(), eq(storeId)))
			.willReturn(storeStatusRes);

		//when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.get("/api/stores/status/{storeId}", storeId)
				.header("Authorization", "Bearer {ACCESS_TOKEN}")
				.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("store/store-get-status",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				pathParameters(
					parameterWithName("storeId").description("업체 ID")
				),
				responseFields(
					fieldWithPath("storeId").description("업체 ID"),
					fieldWithPath("storeStatus").description("영업 상태")
				)
			));
	}

	@Test
	@DisplayName("업체 영업 상태 변경 성공")
	void updateStatusSuccessTest() throws Exception {

		//given
		Long storeId = 1L;

		StoreStatusReq storeStatusReq = new StoreStatusReq(StoreStatus.OPENED);
		StoreStatusRes storeStatusRes = new StoreStatusRes(storeId, storeStatusReq.storeStatus());

		given(storeService.updateStatus(any(), eq(storeId), eq(storeStatusReq)))
			.willReturn(storeStatusRes);

		//when -> then
		mockMvc.perform(
				RestDocumentationRequestBuilders.patch("/api/stores/status/{storeId}", storeId)
					.header("Authorization", "Bearer {ACCESS_TOKEN}")
					.contentType(APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(storeStatusReq)))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("store/store-status-update",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				pathParameters(parameterWithName("storeId").description("업체 ID")
				), requestFields(
					fieldWithPath("storeStatus").description("영업 상태")
				),
				responseFields(
					fieldWithPath("storeId").description("업체 ID"),
					fieldWithPath("storeStatus").description("영업 상태")
				)
			));
	}

	@Test
	@DisplayName("업체 이미지 등록 성공")
	void uploadImageSuccessTest() throws Exception {

		//given
		Long storeId = 1L;
		MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "Spring Framework".getBytes());
		ImageUploadReq request = new ImageUploadReq(file);
		String imageUrl = "http://fakeimageurl.com/image.jpg";
		ImageRes imageRes = new ImageRes(imageUrl);

		given(storeService.uploadImage(any(), eq(storeId), eq(request)))
			.willReturn(imageRes);

		//when -> then
		mockMvc.perform(
				RestDocumentationRequestBuilders.multipart("/api/stores/images/{storeId}", storeId)
					.file(file)
					.header("Authorization", "Bearer {ACCESS_TOKEN}")
					.contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("store/store-upload-image",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				pathParameters(
					parameterWithName("storeId").description("업체 ID")),
				requestParts(
					partWithName("file").description("등록할 이미지 URL")
				),
				responseFields(
					fieldWithPath("imageUrl").description("등록된 이미지 URL")
				)
			));
	}

	@Test
	@DisplayName("업체 이미지 수정 성공")
	void updateImageSuccessTest() throws Exception {

		//given
		Long storeId = 1L;
		MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "Spring Framework".getBytes());
		ImageUploadReq request = new ImageUploadReq(file);
		String imageUrl = "http://fakeimageurl.com/image.jpg";
		ImageRes imageRes = new ImageRes(imageUrl);

		given(storeService.updateImage(any(), eq(storeId), eq(request)))
			.willReturn(imageRes);

		//when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.fileUpload("/api/stores/images/{storeId}", storeId)
				.file(file)
				.with(updateRequest -> {
					updateRequest.setMethod("PATCH");
					return updateRequest;
				})
				.header("Authorization", "Bearer {ACCESS_TOKEN}"))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("store/store-update-image",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				pathParameters(
					parameterWithName("storeId").description("업체 ID")),
				requestParts(
					partWithName("file").description("수정할 이미지 URL")
				),
				responseFields(
					fieldWithPath("imageUrl").description("수정된 이미지 URL")
				)
			));
	}

	@Test
	@DisplayName("업체 이미지 삭제 성공")
	void deleteImageSuccessTest() throws Exception {

		//given
		Long storeId = 1L;

		doNothing().when(storeService).deleteImage(any(), eq(storeId));

		//when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/stores/images/{storeId}", storeId)
				.header("Authorization", "Bearer {ACCESS_TOKEN}"))
			.andExpect(status().isNoContent())
			.andDo(print())
			.andDo(document("store/store-delete-image",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				pathParameters(
					parameterWithName("storeId").description("이미지 삭제 요청 업체 ID"))
			));
	}

	@Test
	@DisplayName("고객이 업체를 보유하고 있다")
	void findByMemberProviderIdSuccessTest() throws Exception {

		//given
		StoreByMemberRes storeByMemberRes = new StoreByMemberRes(1L);

		given(storeService.findByProviderId(any()))
			.willReturn(storeByMemberRes);

		//when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.get("/api/stores/confirm")
				.header("Authorization", "Bearer {ACCESS_TOKEN}"))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("store/store-find-by-provider-id",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				responseFields(
					fieldWithPath("storeId").description("업체 ID")
				)
			));
	}

	@Test
	@DisplayName("회원가입은 했지만 업체를 등록하지 않음")
	void findByProviderIdFailTest_notRegisterStore() throws Exception {

		//given
		Long storeId = 1L;
		given(storeService.findByProviderId(any()))
			.willThrow(new EntityNotFoundException(ErrorCode.NOT_FOUND_STORE));

		//when -> then
		mockMvc.perform(
				RestDocumentationRequestBuilders.get("/api/stores/confirm")
					.header("Authorization", "Bearer {ACCESS_TOKEN}"))
			.andExpect(status().isNotFound())
			.andDo(print())
			.andDo(document("store/store-customer-not-register-store",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				responseFields(
					fieldWithPath("timestamp").type(STRING).description("예외 시간"),
					fieldWithPath("code").type(STRING).description("오류 코드"),
					fieldWithPath("errors").type(ARRAY).description("오류 목록"),
					fieldWithPath("message").type(STRING).description("오류 메시지")
				)
			));
	}

	@Test
	@DisplayName("업체 검색 - 기본 정렬(정렬조건 추가X -> 거리순 정렬)")
	void SearchByDefault() throws Exception {

		//given
		double xCoordinate = 127.0279372;
		double yCoordinate = 37.4980136;
		String keyword = "떡볶이";
		int size = 5;
		int page = 0;
		Pageable pageable = PageRequest.of(page, size);

		StoreInfoSliceRes store1 = new StoreInfoSliceRes(1L, "천하장사", LocalTime.of(19, 00), "image");
		StoreInfoSliceRes store2 = new StoreInfoSliceRes(10L, "떡볶이 파는집", LocalTime.of(21, 30), "image");
		StoresInfoSliceRes storesInfoSliceRes = new StoresInfoSliceRes(
			List.of(store1, store2), false);

		when(storeService.search(anyDouble(), anyDouble(), any(), any(), any()))
			.thenReturn(storesInfoSliceRes);

		//when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.get("/api/stores/search")
				.contentType(APPLICATION_JSON)
				.param("x-coordinate", String.valueOf(xCoordinate))
				.param("y-coordinate", String.valueOf(yCoordinate))
				.param("keyword", keyword)
				.param("size", String.valueOf(size))
				.param("page", String.valueOf(page)))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("store/search-by-default-option",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestParameters(
					List.of(parameterWithName("x-coordinate").description("경도"),
						parameterWithName("y-coordinate").description("위도"),
						parameterWithName("keyword").description("검색어"),
						parameterWithName("size").description("한 페이지 당 상품 목록 개수"),
						parameterWithName("page").description("페이지 번호")
					)),
				responseFields(
					fieldWithPath("storeInfoSliceRes[0].storeId").description("조회된 업체 ID"),
					fieldWithPath("storeInfoSliceRes[0].name").description("조회된 업체 이름"),
					fieldWithPath("storeInfoSliceRes[0].closeTime").description("조회된 업체 마감 시간"),
					fieldWithPath("storeInfoSliceRes[0].image").description("조회된 업체 이미지"),
					fieldWithPath("hasNext").description("추가 결과 여부")
				)
			));
	}

	@Test
	@DisplayName("업체 검색 - 거리순")
	void SearchByDefaultOption() throws Exception {

		//given
		double xCoordinate = 127.0279372;
		double yCoordinate = 37.4980136;
		String sortBy = "distance";
		String keyword = "떡볶이";
		int size = 5;
		int page = 0;
		Pageable pageable = PageRequest.of(page, size);

		StoreInfoSliceRes store1 = new StoreInfoSliceRes(1L, "천하장사", LocalTime.of(19, 00), "image");
		StoreInfoSliceRes store2 = new StoreInfoSliceRes(10L, "떡볶이 파는집", LocalTime.of(21, 30), "image");
		StoresInfoSliceRes storesInfoSliceRes = new StoresInfoSliceRes(
			List.of(store1, store2), false);

		when(storeService.search(anyDouble(), anyDouble(), eq(keyword), eq(sortBy), eq(pageable)))
			.thenReturn(storesInfoSliceRes);

		//when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.get("/api/stores/search")
				.contentType(APPLICATION_JSON)
				.param("x-coordinate", String.valueOf(xCoordinate))
				.param("y-coordinate", String.valueOf(yCoordinate))
				.param("keyword", keyword)
				.param("sort-by", sortBy)
				.param("size", String.valueOf(size))
				.param("page", String.valueOf(page)))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("store/search-by-distance",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestParameters(
					List.of(parameterWithName("x-coordinate").description("경도"),
						parameterWithName("y-coordinate").description("위도"),
						parameterWithName("keyword").description("검색어"),
						parameterWithName("sort-by").description("정렬 기준(기본 및 거리순) : distance"),
						parameterWithName("size").description("한 페이지 당 상품 목록 개수"),
						parameterWithName("page").description("페이지 번호")
					)),
				responseFields(
					fieldWithPath("storeInfoSliceRes[0].storeId").description("조회된 업체 ID"),
					fieldWithPath("storeInfoSliceRes[0].name").description("조회된 업체 이름"),
					fieldWithPath("storeInfoSliceRes[0].closeTime").description("조회된 업체 마감 시간"),
					fieldWithPath("storeInfoSliceRes[0].image").description("조회된 업체 이미지"),
					fieldWithPath("hasNext").description("추가 결과 여부")
				)
			));
	}

	@Test
	@DisplayName("업체 검색 - 마감 임박순")
	void SearchByDeadline() throws Exception {

		//given
		double xCoordinate = 127.0279372;
		double yCoordinate = 37.4980136;
		String sortBy = "deadline";
		String keyword = "떡볶이";
		int size = 5;
		int page = 0;
		Pageable pageable = PageRequest.of(page, size);

		StoreInfoSliceRes store1 = new StoreInfoSliceRes(1L, "천하장사", LocalTime.of(23, 00), "image");
		StoreInfoSliceRes store2 = new StoreInfoSliceRes(10L, "떡볶이 파는집", LocalTime.of(19, 30), "image2");
		StoresInfoSliceRes storesInfoSliceRes = new StoresInfoSliceRes(
			List.of(store2, store1), false);

		when(storeService.search(anyDouble(), anyDouble(), any(), any(), any()))
			.thenReturn(storesInfoSliceRes);

		//when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.get("/api/stores/search")
				.contentType(APPLICATION_JSON)
				.param("x-coordinate", String.valueOf(xCoordinate))
				.param("y-coordinate", String.valueOf(yCoordinate))
				.param("keyword", keyword)
				.param("sort-by", sortBy)
				.param("size", String.valueOf(size))
				.param("page", String.valueOf(page)))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("store/search-by-deadline",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestParameters(
					List.of(parameterWithName("x-coordinate").description("경도"),
						parameterWithName("y-coordinate").description("위도"),
						parameterWithName("keyword").description("검색어"),
						parameterWithName("sort-by").description("정렬 기준(마감 임박순) : deadline"),
						parameterWithName("size").description("한 페이지 당 상품 목록 개수"),
						parameterWithName("page").description("페이지 번호")
					)),
				responseFields(
					fieldWithPath("storeInfoSliceRes[0].storeId").description("조회된 업체 ID"),
					fieldWithPath("storeInfoSliceRes[0].name").description("조회된 업체 이름"),
					fieldWithPath("storeInfoSliceRes[0].closeTime").description("조회된 업체 마감 시간"),
					fieldWithPath("storeInfoSliceRes[0].image").description("조회된 업체 이미지"),
					fieldWithPath("hasNext").description("추가 결과 여부")
				)
			));
	}

	@Test
	@DisplayName("업체 검색 - 할인률 순")
	void SearchByDiscountRate() throws Exception {

		//given
		double xCoordinate = 127.0279372;
		double yCoordinate = 37.4980136;
		String sortBy = "discount-rate";
		String keyword = "떡볶이";
		int size = 5;
		int page = 0;
		Pageable pageable = PageRequest.of(page, size);

		StoreInfoSliceRes store1 = new StoreInfoSliceRes(1L, "천하장사", LocalTime.of(23, 00), "image");
		StoreInfoSliceRes store2 = new StoreInfoSliceRes(10L, "떡볶이 파는집", LocalTime.of(19, 30), "image2");
		StoresInfoSliceRes storesInfoSliceRes = new StoresInfoSliceRes(
			List.of(store2, store1), false);

		when(storeService.search(anyDouble(), anyDouble(), any(), any(), any()))
			.thenReturn(storesInfoSliceRes);

		//when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.get("/api/stores/search")
				.contentType(APPLICATION_JSON)
				.param("x-coordinate", String.valueOf(xCoordinate))
				.param("y-coordinate", String.valueOf(yCoordinate))
				.param("keyword", keyword)
				.param("sort-by", sortBy)
				.param("size", String.valueOf(size))
				.param("page", String.valueOf(page)))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("store/search-by-discount-rate",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestParameters(
					List.of(parameterWithName("x-coordinate").description("경도"),
						parameterWithName("y-coordinate").description("위도"),
						parameterWithName("keyword").description("검색어"),
						parameterWithName("sort-by").description("정렬 기준(할인률순) : discount-rate"),
						parameterWithName("size").description("한 페이지 당 상품 목록 개수"),
						parameterWithName("page").description("페이지 번호")
					)),
				responseFields(
					fieldWithPath("storeInfoSliceRes[0].storeId").description("조회된 업체 ID"),
					fieldWithPath("storeInfoSliceRes[0].name").description("조회된 업체 이름"),
					fieldWithPath("storeInfoSliceRes[0].closeTime").description("조회된 업체 마감 시간"),
					fieldWithPath("storeInfoSliceRes[0].image").description("조회된 업체 이미지"),
					fieldWithPath("hasNext").description("추가 결과 여부")
				)
			));
	}

	@Test
	@DisplayName("업체 검색 - 만족하는 조건이 없을시 빈 배열")
	void SearchByNoCondition() throws Exception {

		//given
		double xCoordinate = 127.0279372;
		double yCoordinate = 37.4980136;
		String sortBy = "discount-rate";
		String keyword = "감자";
		int size = 5;
		int page = 0;
		Pageable pageable = PageRequest.of(page, size);

		StoresInfoSliceRes storesInfoSliceRes = new StoresInfoSliceRes(List.of(), false);

		when(storeService.search(anyDouble(), anyDouble(), any(), any(), any()))
			.thenReturn(storesInfoSliceRes);

		//when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.get("/api/stores/search")
				.contentType(APPLICATION_JSON)
				.param("x-coordinate", String.valueOf(xCoordinate))
				.param("y-coordinate", String.valueOf(yCoordinate))
				.param("keyword", keyword)
				.param("sort-by", sortBy)
				.param("size", String.valueOf(size))
				.param("page", String.valueOf(page)))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("store/search-by-no-condition",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestParameters(
					List.of(parameterWithName("x-coordinate").description("경도"),
						parameterWithName("y-coordinate").description("위도"),
						parameterWithName("keyword").description("검색어"),
						parameterWithName("sort-by").description("정렬 기준(할인률순) : discount-rate"),
						parameterWithName("size").description("한 페이지 당 상품 목록 개수"),
						parameterWithName("page").description("페이지 번호")
					)),
				responseFields(
					fieldWithPath("storeInfoSliceRes").description("조회된 업체 정보 목록"),
					fieldWithPath("storeInfoSliceRes[]").description("업체 정보 목록 (빈 배열 가능)"),
					fieldWithPath("hasNext").description("추가 결과 여부")
				)
			));
	}
}
