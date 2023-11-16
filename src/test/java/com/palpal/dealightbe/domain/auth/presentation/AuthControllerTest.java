package com.palpal.dealightbe.domain.auth.presentation;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
import com.palpal.dealightbe.domain.auth.application.OAuth2AuthorizationService;
import com.palpal.dealightbe.domain.auth.application.dto.request.MemberAuthReq;
import com.palpal.dealightbe.domain.auth.application.dto.response.JoinRequireRes;
import com.palpal.dealightbe.domain.auth.application.dto.response.LoginRes;
import com.palpal.dealightbe.domain.auth.application.dto.response.MemberAuthRes;
import com.palpal.dealightbe.domain.auth.application.dto.response.OAuthLoginRes;
import com.palpal.dealightbe.domain.auth.application.dto.response.RequiredUserInfoRes;
import com.palpal.dealightbe.domain.auth.exception.RequiredAuthenticationException;
import com.palpal.dealightbe.global.error.ErrorCode;
import com.palpal.dealightbe.global.error.exception.BusinessException;
import com.palpal.dealightbe.global.error.exception.EntityNotFoundException;

@AutoConfigureRestDocs
@WebMvcTest(value = AuthController.class)
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private AuthService authService;

	@MockBean
	private OAuth2AuthorizationService oAuth2AuthorizationService;

	@Nested
	@DisplayName("<로그인>")
	class loginTest {

		String loginApiPath = "/api/auth/kakao";

		@DisplayName("등록된 회원이라면 로그인 성공")
		@Test
		void loginSuccessIfAlreadyExistMember() throws Exception {
			RequiredUserInfoRes requiredUserInfoRes
				= new RequiredUserInfoRes("MOCK_SERVER", 12345L, "TESTER");
			LoginRes loginRes = new LoginRes(12345L, "MOCK_ACCESS_TOKEN", "MOCK_REFRESH_TOKEN");
			OAuthLoginRes oAuthLoginRes = new OAuthLoginRes("로그인에 성공했습니다.", loginRes);

			// given
			when(oAuth2AuthorizationService.authorizeFromKakao(any(String.class)))
				.thenReturn(requiredUserInfoRes);
			when(authService.authenticate(requiredUserInfoRes))
				.thenReturn(oAuthLoginRes);

			// when -> then
			mockMvc.perform(get(loginApiPath)
					.with(user("user").roles("MEMBER"))
					.param("code", "MOCK_AUTHORIZATION_CODE")
				)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("로그인에 성공했습니다."))
				.andExpect(jsonPath("$.data.providerId").value(12345L))
				.andExpect(jsonPath("$.data.accessToken").value("MOCK_ACCESS_TOKEN"))
				.andExpect(jsonPath("$.data.refreshToken").value("MOCK_REFRESH_TOKEN"))
				.andDo(print())
				.andDo(document(
					"auth/auth-login-success",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestParameters(
						parameterWithName("code").description("OAuth 서버로부터 받은 Authorization Code")
					),
					responseFields(
						fieldWithPath("message").type(JsonFieldType.STRING)
							.description("로그인 결과 메시지"),
						fieldWithPath("data.providerId").type(JsonFieldType.NUMBER)
							.description("회원 ID 값"),
						fieldWithPath("data.accessToken").type(JsonFieldType.STRING)
							.description("Access Token"),
						fieldWithPath("data.refreshToken").type(JsonFieldType.STRING)
							.description("Refresh Token")
					)
				));
		}

		@DisplayName("등록된 회원이 아니라면 회원가입 요구")
		@Test
		void requestSignupMessageIfNotExistMember() throws Exception {
			RequiredUserInfoRes requiredUserInfoRes
				= new RequiredUserInfoRes("MOCK_SERVER", 12345L, "TESTER");
			JoinRequireRes joinRequireRes = new JoinRequireRes("MOCK_SERVER", 12345L, "TESTER");
			OAuthLoginRes oAuthLoginRes = new OAuthLoginRes("딜라이트 서비스에 가입이 필요합니다.", joinRequireRes);

			// given
			when(oAuth2AuthorizationService.authorizeFromKakao(any(String.class)))
				.thenReturn(requiredUserInfoRes);
			when(authService.authenticate(requiredUserInfoRes))
				.thenReturn(oAuthLoginRes);

			// when -> then
			mockMvc.perform(get(loginApiPath)
					.with(user("user").roles("MEMBER"))
					.param("code", "MOCK_AUTHORIZATION_CODE")
				)
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("딜라이트 서비스에 가입이 필요합니다."))
				.andExpect(jsonPath("$.data.provider").value("MOCK_SERVER"))
				.andExpect(jsonPath("$.data.providerId").value(12345L))
				.andExpect(jsonPath("$.data.nickName").value("TESTER"))
				.andDo(print())
				.andDo(document(
					"auth/auth-require-signup",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestParameters(
						parameterWithName("code").description("OAuth 서버로부터 받은 Authorization Code")
					),
					responseFields(
						fieldWithPath("message").type(JsonFieldType.STRING)
							.description("로그인 결과 메시지"),
						fieldWithPath("data.provider").type(JsonFieldType.STRING)
							.description("회원 ID 값"),
						fieldWithPath("data.providerId").type(JsonFieldType.NUMBER)
							.description("Access Token"),
						fieldWithPath("data.nickName").type(JsonFieldType.STRING)
							.description("Refresh Token")
					)
				));
		}
	}

	@Nested
	@DisplayName("<회원가입>")
	class signupTest {
		String signupApiPath = "/api/auth/signup";

		MemberAuthReq memberSignupReq = new MemberAuthReq(
			"kakao",
			12345L,
			"이홍섭",
			"맹수호빵",
			"01012341234",
			"store"
		);

		MemberAuthRes memberSignupRes = new MemberAuthRes(
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
			MemberAuthReq invalidSignupReq = new MemberAuthReq(
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
			MemberAuthReq invalidSignupReq = new MemberAuthReq(
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
			MemberAuthReq invalidSignupReq = new MemberAuthReq(
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

	@Nested
	@DisplayName("<회원탈퇴>")
	class unregisterTest {
		String unregisterApiPath = "/api/auth/unregister";

		@DisplayName("인증이 된 회원의 경우 회원탈퇴에 성공")
		@Test
		void unregisterSuccess() throws Exception {
			// when -> then
			mockMvc.perform(delete(unregisterApiPath)
					.with(csrf())
					.header("Authorization", "Bearer {ACCESS_TOKEN}")
					.with(user("user").roles("MEMBER"))
				)
				.andDo(print())
				.andExpect(status().isNoContent())
				.andDo(document("auth/auth-unregister-success-request",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestHeaders(
						headerWithName("Authorization").description("Access Token")
					)
				));
		}

		@DisplayName("인증정보가 유효하지 않다면 회원탈퇴에 실패")
		@Test
		void unregisterFailIfAuthenticationIsNoValid() throws Exception {
			// given
			doThrow(RequiredAuthenticationException.class)
				.when(authService)
				.unregister(null);

			// when -> then
			mockMvc.perform(delete(unregisterApiPath)
					.with(csrf())
					.header("Authorization", "Bearer {ACCESS_TOKEN}")
					.with(user("user"))
				)
				.andDo(print())
				.andExpect(status().isUnauthorized())
				.andExpect(result -> {
					assertThat(result.getResolvedException()).isInstanceOf(RequiredAuthenticationException.class);
				})
				.andDo(
					document("auth/auth-unregister-fail-if-authentication-not-valid",
						preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint()),
						requestHeaders(
							headerWithName("Authorization").description("Access Token")
						),
						responseFields(
							fieldWithPath("timestamp").type(STRING)
								.description("예외가 발생한 시간"),
							fieldWithPath("code").type(STRING)
								.description("오류 코드"),
							fieldWithPath("errors").type(ARRAY)
								.description("오류 목록"),
							fieldWithPath("message").type(STRING)
								.description("오류 메시지")
						)
					));
		}

		@DisplayName("회원정보 조회에 실패한다면, 회원탈퇴에 실패")
		@Test
		void unregisterFailIfMemberFoundFail() throws Exception {
			// given
			EntityNotFoundException entityNotFoundException = new EntityNotFoundException(
				ErrorCode.NOT_FOUND_MEMBER);
			doThrow(entityNotFoundException)
				.when(authService)
				.unregister(any());

			// when -> then
			mockMvc.perform(delete(unregisterApiPath)
					.with(csrf())
					.header("Authorization", "Bearer {ACCESS_TOKEN}")
					.with(user("user").roles("MEMBER"))
				)
				.andDo(print())
				.andExpect(status().isNotFound())
				.andExpect(result -> {
					assertThat(result.getResolvedException()).isInstanceOf(EntityNotFoundException.class);
				})
				.andDo(document("auth/auth-unregister-fail-if-member-not-found", preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestHeaders(
						headerWithName("Authorization").description("Access Token")
					),
					responseFields(
						fieldWithPath("timestamp").type(STRING)
							.description("예외가 발생한 시간"),
						fieldWithPath("code").type(STRING)
							.description("오류 코드"),
						fieldWithPath("errors").type(ARRAY)
							.description("오류 목록"),
						fieldWithPath("message").type(STRING)
							.description("오류 메시지")
					)
				));
		}
	}

	@Nested
	@DisplayName("<토큰 재발급>")
	class reissueTokenTest {
		String reissueTokenApiPath = "/api/auth/reissue";

		@DisplayName("회원인증이 된 경우 토큰 재발급 성공")
		@Test
		void reissueTokenSuccess() throws Exception {
			// given
			MemberAuthRes memberAuthRes = new MemberAuthRes(
				"말왕의 장충동 왕족발 보쌈",
				"ACCESS_TOKEN",
				"REFRESH_TOKEN"
			);

			given(authService.reIssueToken(any(), any()))
				.willReturn(memberAuthRes);

			// when -> then
			mockMvc.perform(get(reissueTokenApiPath)
					.with(csrf())
					.header("Authorization", "Bearer {ACCESS_TOKEN}")
					.with(user("user").roles("MEMBER"))
				)
				.andDo(print())
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.nickName").value("말왕의 장충동 왕족발 보쌈"))
				.andExpect(jsonPath("$.accessToken").value("ACCESS_TOKEN"))
				.andExpect(jsonPath("$.refreshToken").value("REFRESH_TOKEN"))
				.andDo(document("auth/auth-reissue-token-success-request",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestHeaders(
						headerWithName("Authorization").description("Access Token")
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
	}
}
