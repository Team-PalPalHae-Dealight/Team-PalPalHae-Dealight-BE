package com.palpal.dealightbe.domain.store.presentation;

import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.palpal.dealightbe.domain.address.application.dto.response.AddressRes;
import com.palpal.dealightbe.domain.store.application.StoreService;
import com.palpal.dealightbe.domain.store.application.dto.request.StoreCreateReq;
import com.palpal.dealightbe.domain.store.application.dto.response.StoreCreateRes;
import com.palpal.dealightbe.global.error.ErrorCode;
import com.palpal.dealightbe.global.error.exception.BusinessException;

@WebMvcTest(value = StoreController.class)
@AutoConfigureRestDocs
class StoreControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@MockBean
	StoreService storeService;

	@Test
	@DisplayName("업체 등록 성공")
	void registerStoreSuccessTest() throws Exception {

		//given
		Long memberId = 1L;
		LocalTime openTime = LocalTime.of(9, 0);
		LocalTime closeTime = LocalTime.of(23, 0);

		StoreCreateReq storeCreateReq = new StoreCreateReq("888-222-111", "맛짱조개", "01066772291", "서울시 강남구", 67.89, 293.2323, openTime, closeTime, "월요일");
		AddressRes addressRes = new AddressRes("서울시 강남구", 67.89, 293.2323);
		StoreCreateRes storeCreateRes = new StoreCreateRes("888-222-111", "맛짱조개", "01066772291", addressRes, openTime, closeTime, "월요일");

		given(storeService.register(memberId, storeCreateReq))
			.willReturn(storeCreateRes);

		//when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.post("/api/stores/{memberId}", memberId)
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(storeCreateReq)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.storeNumber").value(storeCreateRes.storeNumber()))
			.andExpect(jsonPath("$.name").value(storeCreateRes.name()))
			.andExpect(jsonPath("$.telephone").value(storeCreateRes.telephone()))
			.andExpect(jsonPath("$.addressRes.name").value(storeCreateRes.addressRes().name()))
			.andExpect(jsonPath("$.openTime").value(storeCreateRes.openTime().toString()))
			.andExpect(jsonPath("$.closeTime").value(storeCreateRes.closeTime().toString()))
			.andExpect(jsonPath("$.dayOff").value(storeCreateRes.dayOff()))
			.andDo(print())
			.andDo(document("store-register",
				Preprocessors.preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				pathParameters(parameterWithName("memberId").description("고객 ID")
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
					fieldWithPath("storeNumber").description("사업자 등록 번호"),
					fieldWithPath("name").description("상호명"),
					fieldWithPath("telephone").description("업체 전화번호"),
					subsectionWithPath("addressRes").description("주소 정보"),
					fieldWithPath("openTime").description("오픈 시간"),
					fieldWithPath("closeTime").description("마감 시간"),
					fieldWithPath("dayOff").description("휴무일")
				)
			));
	}

	@Test
	@DisplayName("업체 등록 실패 - 잘못된 영업 시간")
	void registerStoreFailTest_invalidBusinessTime() throws Exception {

		//given
		Long memberId = 1L;
		LocalTime openTime = LocalTime.of(23, 0);
		LocalTime closeTime = LocalTime.of(9, 0);

		StoreCreateReq storeCreateReq = new StoreCreateReq("888-222-111", "맛짱조개", "01066772291", "서울시 강남구", 67.89, 293.2323, openTime, closeTime, "월요일");
		AddressRes addressRes = new AddressRes("서울시 강남구", 67.89, 293.2323);
		StoreCreateRes storeCreateRes = new StoreCreateRes("888-222-111", "맛짱조개", "01066772291", addressRes, openTime, closeTime, "월요일");

		given(storeService.register(memberId, storeCreateReq))
			.willThrow(new BusinessException(ErrorCode.INVALID_BUSINESS_TIME));

		//when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.post("/api/stores/{memberId}", memberId)
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(storeCreateReq)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.timestamp").isNotEmpty())
			.andExpect(jsonPath("$.code").value("ST001"))
			.andExpect(jsonPath("$.errors").isEmpty())
			.andExpect(jsonPath("$.message").value("마감 시간은 오픈 시간보다 이전일 수 없습니다"))
			.andDo(print())
			.andDo(document("store-register-fail-invalid-business-time",
				Preprocessors.preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				pathParameters(parameterWithName("memberId").description("고객 ID")
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
}