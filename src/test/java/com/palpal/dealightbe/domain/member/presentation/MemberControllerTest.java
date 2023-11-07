package com.palpal.dealightbe.domain.member.presentation;

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.palpal.dealightbe.config.SecurityConfig;
import com.palpal.dealightbe.domain.address.application.dto.request.AddressReq;
import com.palpal.dealightbe.domain.address.application.dto.response.AddressRes;
import com.palpal.dealightbe.domain.image.application.dto.request.ImageUploadReq;
import com.palpal.dealightbe.domain.image.application.dto.response.ImageRes;
import com.palpal.dealightbe.domain.member.application.MemberService;
import com.palpal.dealightbe.domain.member.application.dto.request.MemberUpdateReq;
import com.palpal.dealightbe.domain.member.application.dto.response.MemberProfileRes;
import com.palpal.dealightbe.domain.member.application.dto.response.MemberUpdateRes;
import com.palpal.dealightbe.global.error.ErrorCode;
import com.palpal.dealightbe.global.error.exception.EntityNotFoundException;

@WebMvcTest(value = MemberController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class,
	OAuth2ClientAutoConfiguration.class}, excludeFilters = {
	@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)})
@AutoConfigureRestDocs
class MemberControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@MockBean
	MemberService memberService;

	@Test
	@DisplayName("멤버 프로필 조회 성공")
	void getProfileSuccessTest() throws Exception {

		//given
		AddressRes addressRes = new AddressRes("서울", 37.5665, 126.9780);
		MemberProfileRes memberProfileInfo = new MemberProfileRes("유재석", "유산슬", "01012345678", addressRes);

		given(memberService.getMemberProfile(any()))
			.willReturn(memberProfileInfo);

		//when -> then
		mockMvc.perform(
				RestDocumentationRequestBuilders.get("/api/members/profiles")
					.header("Authorization", "Bearer {ACCESS_TOKEN}")
					.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("member/member-get-profile",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				responseFields(
					fieldWithPath("realName").description("고객의 실명"),
					fieldWithPath("nickName").description("고객의 닉네임"),
					fieldWithPath("phoneNumber").description("고객의 전화번호"),
					fieldWithPath("address.name").description("고객의 주소명"),
					fieldWithPath("address.xCoordinate").description("고객의 주소의 X 좌표"),
					fieldWithPath("address.yCoordinate").description("고객의 주소의 Y 좌표")
				)
			));
	}

	@Test
	@DisplayName("멤버 프로필 조회 실패: 멤버 ID가 존재하지 않는 경우")
	void getProfileNotFoundTest() throws Exception {

		// given
		given(memberService.getMemberProfile(any()))
			.willThrow(new EntityNotFoundException(ErrorCode.NOT_FOUND_MEMBER));

		// when -> then
		mockMvc.perform(
				RestDocumentationRequestBuilders.get("/api/members/profiles")
					.header("Authorization", "Bearer {ACCESS_TOKEN}")
					.contentType(APPLICATION_JSON))
			.andExpect(status().isNotFound())
			.andDo(print())
			.andDo(document("member/member-get-profile-not-found",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				responseFields(
					fieldWithPath("message").description("에러 메시지"),
					fieldWithPath("timestamp").description("오류 발생 시각"),
					fieldWithPath("code").description("에러 코드"),
					fieldWithPath("errors").description("추가적인 에러 정보")
				)
			));
	}

	@Test
	@DisplayName("멤버 프로필 업데이트 성공")
	void updateProfileSuccessTest() throws Exception {

		// given
		AddressReq addressReq = new AddressReq("서울", 37.5665, 126.9780);
		MemberUpdateReq updateRequest = new MemberUpdateReq("박명수", "유산슬", addressReq);

		AddressRes addressRes = new AddressRes("서울", 37.5665, 126.9780);
		MemberUpdateRes updateResponse = new MemberUpdateRes("박명수", "유산슬", addressRes);

		given(memberService.updateMemberProfile(any(), eq(updateRequest)))
			.willReturn(updateResponse);

		// when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.patch("/api/members/profiles")
				.header("Authorization", "Bearer {ACCESS_TOKEN}")
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateRequest)))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("member/member-update-profile",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				requestFields(
					fieldWithPath("nickname").description("업데이트하려는 닉네임"),
					fieldWithPath("phoneNumber").description("업데이트하려는 전화번호"),
					fieldWithPath("address.name").description("업데이트하려는 주소명"),
					fieldWithPath("address.xCoordinate").description("업데이트하려는 주소의 X 좌표"),
					fieldWithPath("address.yCoordinate").description("업데이트하려는 주소의 Y 좌표")
				),
				responseFields(
					fieldWithPath("nickname").description("업데이트된 닉네임"),
					fieldWithPath("phoneNumber").description("업데이트된 전화번호"),
					fieldWithPath("address.name").description("업데이트된 주소명"),
					fieldWithPath("address.xCoordinate").description("업데이트된 주소의 X 좌표"),
					fieldWithPath("address.yCoordinate").description("업데이트된 주소의 Y 좌표")
				)
			));
	}

	@Test
	@DisplayName("멤버 프로필 업데이트 실패: 멤버 ID가 존재하지 않는 경우")
	void updateProfileNotFoundTest() throws Exception {

		// given
		AddressReq addressReq = new AddressReq("서울", 37.5665, 126.9780);
		MemberUpdateReq updateRequest = new MemberUpdateReq("박명수", "01087654321", addressReq);

		given(memberService.updateMemberProfile(any(), eq(updateRequest)))
			.willThrow(new EntityNotFoundException(ErrorCode.NOT_FOUND_MEMBER));

		// when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.patch("/api/members/profiles")
				.header("Authorization", "Bearer {ACCESS_TOKEN}")
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateRequest)))
			.andExpect(status().isNotFound())
			.andDo(print())
			.andDo(document("member/member-update-profile-not-found",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				requestFields(
					fieldWithPath("nickname").description("업데이트하려는 닉네임"),
					fieldWithPath("phoneNumber").description("업데이트하려는 전화번호"),
					fieldWithPath("address.name").description("업데이트하려는 주소명"),
					fieldWithPath("address.xCoordinate").description("업데이트하려는 주소의 X 좌표"),
					fieldWithPath("address.yCoordinate").description("업데이트하려는 주소의 Y 좌표")
				),
				responseFields(
					fieldWithPath("message").description("에러 메시지"),
					fieldWithPath("timestamp").description("오류 발생 시각"),
					fieldWithPath("code").description("에러 코드"),
					fieldWithPath("errors").description("추가적인 에러 정보")
				)
			));
	}

	@Test
	@DisplayName("멤버 프로필 주소 업데이트 성공")
	void updateAddressSuccessTest() throws Exception {

		// given
		AddressReq addressReq = new AddressReq("서울", 37.5665, 126.9780);
		AddressRes addressRes = new AddressRes("서울", 37.5665, 126.9780);

		given(memberService.updateMemberAddress(any(), eq(addressReq)))
			.willReturn(addressRes);

		// when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.patch("/api/members/addresses")
				.header("Authorization", "Bearer {ACCESS_TOKEN}")
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(addressReq)))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("member/member-update-address",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				requestFields(
					fieldWithPath("name").description("업데이트하려는 주소명"),
					fieldWithPath("xCoordinate").description("업데이트하려는 주소의 X 좌표"),
					fieldWithPath("yCoordinate").description("업데이트하려는 주소의 Y 좌표")
				),
				responseFields(
					fieldWithPath("name").description("업데이트된 주소명"),
					fieldWithPath("xCoordinate").description("업데이트된 주소의 X 좌표"),
					fieldWithPath("yCoordinate").description("업데이트된 주소의 Y 좌표")
				)
			));
	}

	@Test
	@DisplayName("멤버 프로필 주소 업데이트 실패: 멤버 ID가 존재하지 않는 경우")
	void updateAddressNotFoundTest() throws Exception {

		// given
		AddressReq addressReq = new AddressReq("서울", 37.5665, 126.9780);

		given(memberService.updateMemberAddress(any(), eq(addressReq)))
			.willThrow(new EntityNotFoundException(ErrorCode.NOT_FOUND_MEMBER));

		// when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.patch("/api/members/addresses")
				.header("Authorization", "Bearer {ACCESS_TOKEN}")
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(addressReq)))
			.andExpect(status().isNotFound())
			.andDo(print())
			.andDo(document("member/member-update-address-not-found",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				requestFields(
					fieldWithPath("name").description("업데이트하려는 주소명"),
					fieldWithPath("xCoordinate").description("업데이트하려는 주소의 X 좌표"),
					fieldWithPath("yCoordinate").description("업데이트하려는 주소의 Y 좌표")
				),
				responseFields(
					fieldWithPath("message").description("에러 메시지"),
					fieldWithPath("timestamp").description("오류 발생 시각"),
					fieldWithPath("code").description("에러 코드"),
					fieldWithPath("errors").description("추가적인 에러 정보")
				)
			));
	}

	@Test
	@DisplayName("멤버 이미지 업로드 성공")
	void uploadImageSuccessTest() throws Exception {

		// given
		MockMultipartFile file = new MockMultipartFile("file", "profile.png", "image/png",
			"sample-image-content".getBytes());

		ImageUploadReq request = new ImageUploadReq(file);
		ImageRes imageRes = new ImageRes("http://sample.com/profile.png");

		given(memberService.updateMemberImage(any(), any())).willReturn(imageRes);

		// when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.multipart("/api/members/images")
				.file(file)
				.header("Authorization", "Bearer {ACCESS_TOKEN}")
				.with(updateRequest -> {
					updateRequest.setMethod("PATCH");
					return updateRequest;
				}))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.imageUrl", is("http://sample.com/profile.png")))
			.andDo(print())
			.andDo(document("member/member-upload-image",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				requestParts(
					partWithName("file").description("업로드하려는 이미지 파일")
				),
				responseFields(
					fieldWithPath("imageUrl").description("업로드된 이미지의 URL")
				)
			));
	}

	@Test
	@DisplayName("멤버 이미지 업로드 실패 - 유효하지 않은 memberId")
	void uploadImageFailDueToInvalidMemberIdTest() throws Exception {

		// given
		MockMultipartFile file = new MockMultipartFile("file", "profile.png", "image/png",
			"sample-image-content".getBytes());

		given(memberService.updateMemberImage(any(), any()))
			.willThrow(new EntityNotFoundException(ErrorCode.NOT_FOUND_MEMBER));

		// when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.multipart("/api/members/images")
				.file(file)
				.header("Authorization", "Bearer {ACCESS_TOKEN}")
				.with(updateRequest -> {
					updateRequest.setMethod("PATCH");
					return updateRequest;
				}))
			.andExpect(status().isNotFound())
			.andDo(print())
			.andDo(document("member/member-upload-image-not-found",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				requestParts(
					partWithName("file").description("업로드하려는 이미지 파일")
				),
				responseFields(
					fieldWithPath("message").description("에러 메시지"),
					fieldWithPath("timestamp").description("오류 발생 시각"),
					fieldWithPath("code").description("에러 코드"),
					fieldWithPath("errors").description("추가적인 에러 정보")
				)
			));
	}

	@Test
	@DisplayName("멤버 이미지 삭제 성공")
	void deleteMemberImageSuccessTest() throws Exception {

		// given
		doNothing().when(memberService).deleteMemberImage(any());

		// when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/members/images")
				.header("Authorization", "Bearer {ACCESS_TOKEN}")
				.contentType(APPLICATION_JSON))
			.andExpect(status().isNoContent())
			.andDo(print())
			.andDo(document("member/member-delete-image-success",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				)
			));
	}

	@Test
	@DisplayName("멤버 이미지 삭제 실패: 멤버 ID가 존재하지 않는 경우")
	void deleteMemberImageFailNotFoundTest() throws Exception {

		// given
		doThrow(new EntityNotFoundException(ErrorCode.NOT_FOUND_MEMBER)).when(memberService)
			.deleteMemberImage(any());

		// when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/members/images")
				.header("Authorization", "Bearer {ACCESS_TOKEN}")
				.contentType(APPLICATION_JSON))
			.andExpect(status().isNotFound())
			.andDo(print())
			.andDo(document("member/member-delete-image-not-found",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Authorization").description("Access Token")
				),
				responseFields(
					fieldWithPath("message").description("에러 메시지"),
					fieldWithPath("timestamp").description("오류 발생 시각"),
					fieldWithPath("code").description("에러 코드"),
					fieldWithPath("errors").description("추가적인 에러 정보")
				)
			));
	}
}
