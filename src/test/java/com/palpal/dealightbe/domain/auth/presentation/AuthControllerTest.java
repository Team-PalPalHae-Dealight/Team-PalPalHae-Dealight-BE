package com.palpal.dealightbe.domain.auth.presentation;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.palpal.dealightbe.domain.auth.application.AuthService;
import com.palpal.dealightbe.domain.auth.application.OAuth2AuthorizationService;
import com.palpal.dealightbe.domain.auth.application.dto.request.MemberNickNameCheckReq;
import com.palpal.dealightbe.domain.auth.application.dto.request.MemberSignupAuthReq;
import com.palpal.dealightbe.domain.auth.application.dto.response.MemberAuthRes;
import com.palpal.dealightbe.domain.auth.application.dto.response.OAuthLoginRes;
import com.palpal.dealightbe.domain.auth.application.dto.response.OAuthUserInfoRes;
import com.palpal.dealightbe.domain.auth.exception.OAuth2AuthorizationException;
import com.palpal.dealightbe.domain.auth.exception.RequiredAuthenticationException;
import com.palpal.dealightbe.domain.member.domain.Member;
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
	@DisplayName("<닉네임 중복검사>")
	class nickNameDuplicateTest {

		String duplicateApi = "/api/auth/duplicate";

		@DisplayName("중복 닉네임이 없다면 성공")
		@ValueSource(strings = {"예", "예성", "요송", "요송송", "yosongsong", "요songsong", "ye성성"})
		@ParameterizedTest
		void successIfNoDuplicateNickName(String nickName) throws Exception {
			// given
			MemberNickNameCheckReq request = new MemberNickNameCheckReq(nickName);

			doNothing().when(authService)
				.checkDuplicateNickName(request);

			// when -> then
			mockMvc.perform(post(duplicateApi)
					.with(user("user").roles("MEMBER"))
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON_VALUE)
					.content(objectMapper.writeValueAsString(request))
				)
				.andExpect(status().isNoContent())
				.andDo(print())
				.andDo(document(
					"auth/auth-duplicate-nickName-check-success",
					preprocessRequest(prettyPrint()),
					requestFields(
						fieldWithPath("nickName").type(JsonFieldType.STRING)
							.description("중복검사 대상 닉네임")
					)
				));
		}

		@DisplayName("중복 닉네임이 존재한다면 실패")
		@Test
		void failIfDuplicateNickNameIsExist() throws Exception {
			// given
			MemberNickNameCheckReq request = new MemberNickNameCheckReq("요송");
			BusinessException businessException = new BusinessException(ErrorCode.DUPLICATED_NICK_NAME);

			doThrow(businessException).when(authService)
				.checkDuplicateNickName(request);

			// when -> then
			mockMvc.perform(post(duplicateApi)
					.with(user("user").roles("MEMBER"))
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON_VALUE)
					.content(objectMapper.writeValueAsString(request))
				)
				.andExpect(status().isBadRequest())
				.andDo(print())
				.andDo(document(
					"auth/auth-duplicate-nickName-check-fail-already-exist",
					preprocessRequest(prettyPrint()),
					responseFields(
						fieldWithPath("timestamp").type(STRING).description("예외 시간"),
						fieldWithPath("code").type(STRING).description("오류 코드"),
						fieldWithPath("errors").type(ARRAY).description("오류 목록"),
						fieldWithPath("message").type(STRING).description("오류 메시지")
					)
				));
		}

		@DisplayName("닉네임이 유효한 형태가 아닌 경우")
		@NullAndEmptySource
		@ValueSource(strings = {"요#@$", "       송", "#$%^&", "yosong  !@#$%,,,", ",,,,,,", "~@#$T예,,,&^%#@#-987성"})
		@ParameterizedTest
		void failIfNickNameIsNotValid(String nickName) throws Exception {
			// given
			MemberNickNameCheckReq request = new MemberNickNameCheckReq(nickName);

			// when -> then
			mockMvc.perform(post(duplicateApi)
					.with(user("user").roles("MEMBER"))
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON_VALUE)
					.content(objectMapper.writeValueAsString(request))
				)
				.andExpect(status().isBadRequest())
				.andDo(print())
				.andDo(document(
					"auth/auth-duplicate-nickName-check-fail-not-valid-form",
					preprocessRequest(prettyPrint()),
					responseFields(
						fieldWithPath("timestamp").type(STRING).description("예외 시간"),
						fieldWithPath("code").type(STRING).description("오류 코드"),
						fieldWithPath("errors").type(ARRAY).description("오류 목록"),
						fieldWithPath("errors[0].field").type(STRING).description("오류가 발생한 필드"),
						fieldWithPath("errors[1].value").type(STRING).description("오류를 발생시킨 값"),
						fieldWithPath("errors[2].reason").type(STRING).description("오류의 원린"),
						fieldWithPath("message").type(STRING).description("오류 메시지")
					)
				));
		}
	}

	@Nested
	@DisplayName("<로그인>")
	class loginTest {

		String loginApiPath = "/api/auth/kakao";

		@DisplayName("등록된 회원이라면 로그인 성공")
		@Test
		void loginSuccessIfAlreadyExistMember() throws Exception {
			// given
			OAuthUserInfoRes requiredUserInfoRes
				= new OAuthUserInfoRes("MOCK_SERVER", 12345L, "TESTER");
			MemberAuthRes authRes = new MemberAuthRes(12345L, "member",
				"MOCK_ACCESS_TOKEN", "MOCK_REFRESH_TOKEN");
			OAuthLoginRes oAuthLoginRes = OAuthLoginRes.from(authRes);

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
				.andExpect(jsonPath("$.data.userId").value(12345L))
				.andExpect(jsonPath("$.data.role").value("member"))
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
						fieldWithPath("data.userId").type(JsonFieldType.NUMBER)
							.description("회원 ID 값"),
						fieldWithPath("data.role").type(JsonFieldType.STRING)
							.description("Role"),
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
			// given
			OAuthUserInfoRes oAuthUserInfoRes
				= new OAuthUserInfoRes("MOCK_SERVER", 12345L, "TESTER");
			OAuthLoginRes oAuthLoginRes
				= new OAuthLoginRes("딜라이트 서비스에 가입이 필요합니다.", oAuthUserInfoRes);

			when(oAuth2AuthorizationService.authorizeFromKakao(any(String.class)))
				.thenReturn(oAuthUserInfoRes);
			when(authService.authenticate(oAuthUserInfoRes))
				.thenReturn(oAuthLoginRes);

			// when -> then
			mockMvc.perform(get(loginApiPath)
					.with(user("user").roles("MEMBER"))
					.param("code", "MOCK_AUTHORIZATION_CODE")
				)
				.andExpect(status().isOk())
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

		@DisplayName("Authorization Code가 null이라면 400에러 발생")
		@Test
		void throwExceptionIfAuthorizationCodeIsNull() throws Exception {
			// when -> then
			mockMvc.perform(get(loginApiPath)
					.with(user("user").roles("MEMBER"))
					.param("code", (String)null)
				)
				.andExpect(status().isBadRequest())
				.andExpect(result -> assertThat(result.getResolvedException())
					.isInstanceOf(MissingServletRequestParameterException.class))
				.andDo(print())
				.andDo(document(
					"auth/auth-null-authorization-code-fail",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestParameters(
						parameterWithName("code").description("OAuth 서버로부터 받은 Authorization Code")
					),
					responseFields(
						fieldWithPath("timestamp").type(STRING).description("예외 시간"),
						fieldWithPath("code").type(STRING).description("오류 코드"),
						fieldWithPath("errors").type(ARRAY).description("오류 목록"),
						fieldWithPath("message").type(STRING).description("오류 메시지")
					)
				));
		}

		@DisplayName("Authorization Code가 잘못됐다면, Authroization Server에 토큰을 요청한 결과에서 예외 발생")
		@EmptySource
		@ValueSource(strings = {"@#@#$^;&%^", "@!@;", "?:{{}:", "~~<>?:{}_+"})
		@ParameterizedTest
		void throwExceptionIfInvalidAuthorizationCode(String authorizationCode) throws Exception {
			// given
			OAuth2AuthorizationException oAuth2AuthorizationException = new OAuth2AuthorizationException(
				ErrorCode.UNABLE_TO_GET_TOKEN_FROM_AUTH_SERVER);
			doThrow(oAuth2AuthorizationException).when(oAuth2AuthorizationService)
				.authorizeFromKakao(authorizationCode);

			// when -> then
			mockMvc.perform(get(loginApiPath)
					.with(user("user").roles("MEMBER"))
					.param("code", authorizationCode)
				)
				.andExpect(status().isInternalServerError())
				.andDo(print())
				.andDo(document(
					"auth/auth-invalid-authorization-code",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestParameters(
						parameterWithName("code").description("OAuth 서버로부터 받은 Authorization Code")
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
	@DisplayName("<회원가입>")
	class signupTest {
		String signupApiPath = "/api/auth/signup";

		MemberSignupAuthReq memberAuthReq = new MemberSignupAuthReq(
			"kakao",
			12345L,
			"이홍섭",
			"맹수호빵",
			"01012341234"
		);

		MemberAuthRes memberAuthRes = new MemberAuthRes(
			12345L,
			"member",
			"MOCK_ACCESS_TOKEN",
			"MOCK_REFRESH_TOKEN"
		);

		@DisplayName("요청 정보가 모두 유효하면 회원가입에 성공")
		@Test
		void signupSuccess() throws Exception {
			// given
			given(authService.signup(memberAuthReq))
				.willReturn(memberAuthRes);

			// when -> then
			mockMvc.perform(post(signupApiPath)
					.with(csrf())
					.with(user("user").roles("MEMBER"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(memberAuthReq))
				)
				.andDo(print())
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.userId").value(12345L))
				.andExpect(jsonPath("$.role").value("member"))
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
							.description("회원 전화번호")
					),
					responseFields(
						fieldWithPath("userId").type(JsonFieldType.NUMBER)
							.description("회원의 ID"),
						fieldWithPath("role").type(JsonFieldType.STRING)
							.description("가입한 회원의 Role"),
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
			given(authService.signup(memberAuthReq))
				.willThrow(new BusinessException(ErrorCode.ALREADY_EXIST_MEMBER));

			// when -> then
			mockMvc.perform(post(signupApiPath)
					.with(csrf())
					.with(user("user").roles("MEMBER"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(memberAuthReq))
				)
				.andDo(print())
				.andExpect(status().isBadRequest())
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
							.description("회원 전화번호")
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
			MemberSignupAuthReq invalidSignupReq = new MemberSignupAuthReq(
				"kakao",
				null,
				"섭홍이",
				"호떡맹수",
				"01012341234"
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
					assertThat(result.getResolvedException())
						.isInstanceOf(MethodArgumentNotValidException.class);
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
							.description("회원 전화번호")
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
			MemberSignupAuthReq invalidSignupReq = new MemberSignupAuthReq(
				null,
				123L,
				"섭홍이",
				"호떡맹수",
				"01012341234"
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
							.description("회원 전화번호")
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
				12345L,
				"member",
				"ACCESS_TOKEN",
				"REFRESH_TOKEN"
			);

			given(authService.reissueToken(any(), any()))
				.willReturn(memberAuthRes);

			// when -> then
			mockMvc.perform(get(reissueTokenApiPath)
					.with(csrf())
					.header("Authorization", "Bearer {ACCESS_TOKEN}")
					.with(user("user").roles("MEMBER"))
				)
				.andDo(print())
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.userId").value(12345L))
				.andExpect(jsonPath("$.role").value("member"))
				.andExpect(jsonPath("$.accessToken").value("ACCESS_TOKEN"))
				.andExpect(jsonPath("$.refreshToken").value("REFRESH_TOKEN"))
				.andDo(document("auth/auth-reissue-token-success-request",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestHeaders(
						headerWithName("Authorization").description("Refresh Token")
					),
					responseFields(
						fieldWithPath("userId").type(JsonFieldType.NUMBER)
							.description("회원 ID"),
						fieldWithPath("role").type(JsonFieldType.STRING)
							.description("회원의 Role"),
						fieldWithPath("accessToken").type(JsonFieldType.STRING)
							.description("Access Token"),
						fieldWithPath("refreshToken").type(JsonFieldType.STRING)
							.description("Refresh Token")
					)
				));
		}

		@DisplayName("ProviderId로 멤버 조회에 실패한 경우 Access Token 재발급에 실패")
		@Test
		void reissueAccessTokenFailIfUnableToGetProviderId() throws Throwable {
			// given
			EntityNotFoundException entityNotFoundException = new EntityNotFoundException(ErrorCode.NOT_FOUND_MEMBER);
			doThrow(entityNotFoundException)
				.when(authService)
				.reissueToken(any(), any());

			// when -> then
			mockMvc.perform(get(reissueTokenApiPath)
					.with(csrf())
					.header("Authorization", "Bearer {ACCESS_TOKEN}")
					.with(user("user").roles("MEMBER"))
				)
				.andDo(print())
				.andExpect(status().isNotFound())
				.andExpect(result -> {
					assertThat(result.getResolvedException())
						.isInstanceOf(EntityNotFoundException.class);
				})
				.andDo(document("auth/auth-reissue-token-fail-if-not-found-member",
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
	}

	@Nested
	@DisplayName("<권한변경>")
	class changeRoleMemberToStore {

		String changeRoleApiPath = "/api/auth/role";

		@DisplayName("ROLE_MEMBER를 ROLE_STORE로 변경")
		@Test
		void changeRoleSuccess() throws Exception {
			// given
			Long providerId = 12345L;
			String accessToken = "ACCESS_TOKEN";
			String refreshToken = "REFRESH_TOKEN";
			MemberAuthRes memberAuthRes = new MemberAuthRes(providerId, "store", accessToken, refreshToken);

			given(authService.updateMemberRoleToStore(any()))
				.willReturn(memberAuthRes);

			// when -> then
			mockMvc.perform(patch(changeRoleApiPath)
					.header("Authorization", "Bearer {ACCESS_TOKEN}")
					.with(csrf())
					.with(user("user").roles("MEMBER"))
				)
				.andDo(print())
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.userId").value(providerId))
				.andExpect(jsonPath("$.role").value("store"))
				.andExpect(jsonPath("$.accessToken").value(accessToken))
				.andExpect(jsonPath("$.refreshToken").value(refreshToken))
				.andDo(document(
					"auth/auth-change-role-success",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestHeaders(
						headerWithName("Authorization").description("Access Token")
					),
					responseFields(
						fieldWithPath("userId").type(JsonFieldType.NUMBER)
							.description("회원의 ID"),
						fieldWithPath("role").type(JsonFieldType.STRING)
							.description("변경된 회원의 Role"),
						fieldWithPath("accessToken").type(JsonFieldType.STRING)
							.description("Access Token"),
						fieldWithPath("refreshToken").type(JsonFieldType.STRING)
							.description("Refresh Token")
					)
				));
		}
	}
}
