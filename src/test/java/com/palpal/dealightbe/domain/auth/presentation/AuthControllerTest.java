package com.palpal.dealightbe.domain.auth.presentation;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.palpal.dealightbe.domain.auth.application.AuthService;
import com.palpal.dealightbe.domain.auth.application.dto.request.MemberSignupReq;
import com.palpal.dealightbe.domain.auth.application.dto.response.MemberSignupRes;
import com.palpal.dealightbe.global.error.ErrorCode;
import com.palpal.dealightbe.global.error.exception.BusinessException;

@AutoConfigureRestDocs
@WebMvcTest(value = AuthController.class)
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private AuthService authService;

	@Nested
	@DisplayName("<회원가입>")
	class signupTest {
		String signupApiPath = "/api/auth/signup";

		MemberSignupReq memberSignupReq = new MemberSignupReq(
			"kakao",
			12345L,
			"이홍섭",
			"맹수호빵",
			"01012341234",
			"store"
		);

		MemberSignupRes memberSignupRes = new MemberSignupRes(
			"맹수호빵",
			"MOCK_ACCESS_TOKEN",
			"MOCK_REFRESH_TOKEN"
		);

		@DisplayName("요청 정보가 모두 유효하면 회원가입에 성공")
		@Test
		void signupSuccess() throws Exception {
			// given
			given(authService.signup(memberSignupReq))
				.willReturn(memberSignupRes);

			// when -> then
			mockMvc.perform(post(signupApiPath)
					.with(csrf())
					.with(user("user").roles("MEMBER"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(memberSignupReq))
				)
				.andDo(print())
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.nickName").value("맹수호빵"))
				.andExpect(jsonPath("$.accessToken").value("MOCK_ACCESS_TOKEN"))
				.andExpect(jsonPath("$.refreshToken").value("MOCK_REFRESH_TOKEN"))
				.andDo(document(
					"auth/auth-signup-success",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestFields(
						fieldWithPath("provider").type(JsonFieldType.STRING)
							.description("OAuth 서버 이름"),
						fieldWithPath("providerId").type(JsonFieldType.NUMBER)
							.description("OAuth 서버로부터 받은 회원 아이디"),
						fieldWithPath("realName").type(JsonFieldType.STRING)
							.description("회원의 본명"),
						fieldWithPath("nickName").type(JsonFieldType.STRING)
							.description("회원의 닉네임"),
						fieldWithPath("phoneNumber").type(JsonFieldType.STRING)
							.description("회원 전화번호"),
						fieldWithPath("role").type(JsonFieldType.STRING)
							.description("가입하는 회원의 권한")
					),
					responseFields(
						fieldWithPath("nickName").type(JsonFieldType.STRING)
							.description("회원의 닉네임"),
						fieldWithPath("accessToken").type(JsonFieldType.STRING)
							.description("Access Token"),
						fieldWithPath("refreshToken").type(JsonFieldType.STRING)
							.description("Refresh Token")
					)
				));
		}

		@DisplayName("이미 가입된 회원이라면 가입 실패")
		@Test
		void signupFailIfAlreadyExistMember() throws Exception {
			// given
			given(authService.signup(memberSignupReq))
				.willThrow(new BusinessException(ErrorCode.ALREADY_EXIST_MEMBER));

			// when -> then
			mockMvc.perform(post(signupApiPath)
					.with(csrf())
					.with(user("user").roles("MEMBER"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(memberSignupReq))
				)
				.andDo(print())
				.andExpect(status().is4xxClientError())
				.andExpect(result -> assertThat(result.getResolvedException())
					.isInstanceOf(BusinessException.class))
				.andDo(document(
					"auth/auth-signup-fail-already-exist",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestFields(
						fieldWithPath("provider").type(JsonFieldType.STRING)
							.description("OAuth 서버 이름"),
						fieldWithPath("providerId").type(JsonFieldType.NUMBER)
							.description("OAuth 서버로부터 받은 회원 아이디"),
						fieldWithPath("realName").type(JsonFieldType.STRING)
							.description("회원의 본명"),
						fieldWithPath("nickName").type(JsonFieldType.STRING)
							.description("회원의 닉네임"),
						fieldWithPath("phoneNumber").type(JsonFieldType.STRING)
							.description("회원 전화번호"),
						fieldWithPath("role").type(JsonFieldType.STRING)
							.description("가입하는 회원의 권한")
					),
					responseFields(
						fieldWithPath("timestamp").type(STRING).description("예외 시간"),
						fieldWithPath("code").type(STRING).description("오류 코드"),
						fieldWithPath("errors").type(ARRAY).description("오류 목록"),
						fieldWithPath("message").type(STRING).description("오류 메시지")
					)
				));
		}

		@DisplayName("ProviderId가 없다면 회원가입 실패")
		@Test
		void signupFailIfProviderIdIsNotExist() throws Exception {
			// given
			MemberSignupReq invalidSignupReq = new MemberSignupReq(
				"kakao",
				null,
				"섭홍이",
				"호떡맹수",
				"01012341234",
				"store"
			);

			// when -> then
			mockMvc.perform(post(signupApiPath)
					.with(csrf())
					.with(user("user").roles("MEMBER"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(invalidSignupReq))
				)
				.andDo(print())
				.andExpect(status().is4xxClientError())
				.andExpect(result -> {
					assertThat(result.getResolvedException()).isInstanceOf(MethodArgumentNotValidException.class);
				})
				.andDo(document(
					"auth/auth-signup-fail-no-providerId",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestFields(
						fieldWithPath("provider").type(JsonFieldType.STRING)
							.description("OAuth 서버 이름"),
						fieldWithPath("providerId").type(JsonFieldType.NULL)
							.description("OAuth 서버로부터 받은 회원 아이디"),
						fieldWithPath("realName").type(JsonFieldType.STRING)
							.description("회원의 본명"),
						fieldWithPath("nickName").type(JsonFieldType.STRING)
							.description("회원의 닉네임"),
						fieldWithPath("phoneNumber").type(JsonFieldType.STRING)
							.description("회원 전화번호"),
						fieldWithPath("role").type(JsonFieldType.STRING)
							.description("가입하는 회원의 권한")
					),
					responseFields(
						fieldWithPath("timestamp").type(STRING).description("예외 시간"),
						fieldWithPath("code").type(STRING).description("오류 코드"),
						fieldWithPath("errors[].field").type(STRING).description("DTO 검증 오류 필드"),
						fieldWithPath("errors[].value").type(STRING).description("DTO 검증 오류 값"),
						fieldWithPath("errors[].reason").type(STRING).description("DTO 오류 원인"),
						fieldWithPath("message").type(STRING).description("오류 메시지")
					)
				));
		}

		@DisplayName("Provider 정보가 없다면 회원가입 실패")
		@Test
		void signupFailIfProviderIsNotExist() throws Exception {
			// given
			MemberSignupReq invalidSignupReq = new MemberSignupReq(
				null,
				123L,
				"섭홍이",
				"호떡맹수",
				"01012341234",
				"store"
			);

			// when -> then
			mockMvc.perform(post(signupApiPath)
					.with(csrf())
					.with(user("user").roles("MEMBER"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(invalidSignupReq))
				)
				.andDo(print())
				.andExpect(status().is4xxClientError())
				.andExpect(result -> {
					assertThat(result.getResolvedException()).isInstanceOf(MethodArgumentNotValidException.class);
				})
				.andDo(document(
					"auth/auth-signup-fail-no-provider",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestFields(
						fieldWithPath("provider").type(JsonFieldType.NULL)
							.description("OAuth 서버 이름"),
						fieldWithPath("providerId").type(JsonFieldType.NUMBER)
							.description("OAuth 서버로부터 받은 회원 아이디"),
						fieldWithPath("realName").type(JsonFieldType.STRING)
							.description("회원의 본명"),
						fieldWithPath("nickName").type(JsonFieldType.STRING)
							.description("회원의 닉네임"),
						fieldWithPath("phoneNumber").type(JsonFieldType.STRING)
							.description("회원 전화번호"),
						fieldWithPath("role").type(JsonFieldType.STRING)
							.description("가입하는 회원의 권한")
					),
					responseFields(
						fieldWithPath("timestamp").type(STRING).description("예외 시간"),
						fieldWithPath("code").type(STRING).description("오류 코드"),
						fieldWithPath("errors[].field").type(STRING).description("DTO 검증 오류 필드"),
						fieldWithPath("errors[].value").type(STRING).description("DTO 검증 오류 값"),
						fieldWithPath("errors[].reason").type(STRING).description("DTO 오류 원인"),
						fieldWithPath("message").type(STRING).description("오류 메시지")
					)
				));
		}

		@DisplayName("잘못된 Role이 들어온다면 회원가입 실패")
		@Test
		void signupFailIfInvalidRoleRequest() throws Exception {
			// given
			MemberSignupReq invalidSignupReq = new MemberSignupReq(
				"kakao",
				123L,
				"석봉이",
				"불나방 쏘시지 클럽",
				"01012341234",
				"포켓몬 마스터"
			);

			given(authService.signup(invalidSignupReq))
				.willThrow(new BusinessException(ErrorCode.INVALID_ROLE_TYPE));

			// when -> then
			mockMvc.perform(post(signupApiPath)
					.with(csrf())
					.with(user("user").roles("MEMBER"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(invalidSignupReq))
				)
				.andDo(print())
				.andExpect(status().is4xxClientError())
				.andExpect(result -> {
					assertTrue(result.getResolvedException() instanceof BusinessException);
				})
				.andDo(document(
					"auth/auth-signup-fail-invalid-role-request",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestFields(
						fieldWithPath("provider").type(JsonFieldType.STRING)
							.description("OAuth 서버 이름"),
						fieldWithPath("providerId").type(JsonFieldType.NUMBER)
							.description("OAuth 서버로부터 받은 회원 아이디"),
						fieldWithPath("realName").type(JsonFieldType.STRING)
							.description("회원의 본명"),
						fieldWithPath("nickName").type(JsonFieldType.STRING)
							.description("회원의 닉네임"),
						fieldWithPath("phoneNumber").type(JsonFieldType.STRING)
							.description("회원 전화번호"),
						fieldWithPath("role").type(JsonFieldType.STRING)
							.description("가입하는 회원의 권한")
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
}
