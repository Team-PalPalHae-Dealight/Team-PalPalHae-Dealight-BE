package com.palpal.dealightbe.domain.store.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
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
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalTime;
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
import com.palpal.dealightbe.domain.store.application.dto.response.StoreCreateRes;
import com.palpal.dealightbe.domain.store.application.dto.response.StoreInfoRes;
import com.palpal.dealightbe.domain.store.application.dto.response.StoreStatusRes;
import com.palpal.dealightbe.domain.store.domain.DayOff;
import com.palpal.dealightbe.domain.store.domain.StoreStatus;
import com.palpal.dealightbe.global.error.ErrorCode;
import com.palpal.dealightbe.global.error.exception.BusinessException;

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
	@DisplayName("업체 이미지 수정 성공")
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
}
