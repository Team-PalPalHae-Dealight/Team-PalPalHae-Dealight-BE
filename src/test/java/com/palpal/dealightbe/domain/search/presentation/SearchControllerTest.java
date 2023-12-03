package com.palpal.dealightbe.domain.search.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalTime;
import java.util.List;

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
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.palpal.dealightbe.config.SecurityConfig;
import com.palpal.dealightbe.domain.search.application.SearchService;
import com.palpal.dealightbe.domain.store.application.dto.response.StoreInfoSliceRes;
import com.palpal.dealightbe.domain.store.application.dto.response.StoresInfoSliceRes;

@WebMvcTest(value = SearchController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class,
	OAuth2ClientAutoConfiguration.class}, excludeFilters = {
	@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)})
@AutoConfigureRestDocs
class SearchControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@MockBean
	SearchService searchService;

	@Test
	@DisplayName("업체 검색")
	void SearchByDefault() throws Exception {

		//given
		double xCoordinate = 127.0279372;
		double yCoordinate = 37.4980136;
		String keyword = "떡볶이";
		Long lastId = 5L;

		int size = 5;
		int page = 0;

		StoreInfoSliceRes store1 = new StoreInfoSliceRes(1L, "천하장사", LocalTime.of(19, 00), "image");
		StoreInfoSliceRes store2 = new StoreInfoSliceRes(10L, "떡볶이 파는집", LocalTime.of(21, 30), "image");
		StoresInfoSliceRes storesInfoSliceRes = new StoresInfoSliceRes(
			List.of(store1, store2), false);

		when(searchService.searchToES(anyDouble(), anyDouble(), any(), any(), any()))
			.thenReturn(storesInfoSliceRes);

		//when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.get("/api/search")
				.contentType(APPLICATION_JSON)
				.param("x-coordinate", String.valueOf(xCoordinate))
				.param("y-coordinate", String.valueOf(yCoordinate))
				.param("keyword", keyword)
				.param("size", String.valueOf(size))
				.param("page", String.valueOf(page)))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("search/search-by-option",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestParameters(
					List.of(parameterWithName("x-coordinate").description("경도"),
						parameterWithName("y-coordinate").description("위도"),
						parameterWithName("keyword").description("검색어"),
						parameterWithName("size").description("한 페이지 당 업체 목록 개수"),
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
}
