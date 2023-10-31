package com.palpal.dealightbe.domain.member.presentation;

import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.palpal.dealightbe.domain.address.application.dto.request.AddressReq;
import com.palpal.dealightbe.domain.address.application.dto.response.AddressRes;
import com.palpal.dealightbe.domain.member.application.MemberService;
import com.palpal.dealightbe.domain.member.application.dto.request.MemberUpdateReq;
import com.palpal.dealightbe.domain.member.application.dto.response.MemberProfileRes;
import com.palpal.dealightbe.domain.member.application.dto.response.MemberUpdateRes;
import com.palpal.dealightbe.global.error.ErrorCode;
import com.palpal.dealightbe.global.error.exception.EntityNotFoundException;

@WebMvcTest(value = MemberController.class)
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
		Long memberId = 1L;

		AddressRes addressRes = new AddressRes("서울", 37.5665, 126.9780);
		MemberProfileRes memberProfileInfo = new MemberProfileRes("유재석", "유산슬", "01012345678", addressRes);

		given(memberService.getMemberProfile(memberId))
			.willReturn(memberProfileInfo);

		//when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.get("/api/members/profiles/{memberId}", memberId)
				.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("member-get-profile",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				pathParameters(
					parameterWithName("memberId").description("고객 ID")
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
		Long nonexistentMemberId = 999L;

		given(memberService.getMemberProfile(nonexistentMemberId))
			.willThrow(new EntityNotFoundException(ErrorCode.NOT_FOUND_MEMBER));

		// when -> then
		mockMvc.perform(RestDocumentationRequestBuilders.get("/api/members/profiles/{memberId}", nonexistentMemberId)
				.contentType(APPLICATION_JSON))
			.andExpect(status().isNotFound())
			.andDo(print())
			.andDo(document("member-get-profile-not-found",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				pathParameters(
					parameterWithName("memberId").description("조회하려는 멤버의 ID")
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
