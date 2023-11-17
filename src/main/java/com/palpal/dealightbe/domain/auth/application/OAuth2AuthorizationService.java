package com.palpal.dealightbe.domain.auth.application;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.palpal.dealightbe.config.OAuth2KakaoRegistrationProperty;
import com.palpal.dealightbe.domain.auth.application.dto.response.KakaoTokenRes;
import com.palpal.dealightbe.domain.auth.application.dto.response.KakaoUserInfoRes;
import com.palpal.dealightbe.domain.auth.application.dto.response.OAuthUserInfoRes;
import com.palpal.dealightbe.domain.auth.exception.OAuth2AuthorizationException;
import com.palpal.dealightbe.global.error.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class OAuth2AuthorizationService {

	private final OAuth2KakaoRegistrationProperty oAuth2KakaoRegistrationProperty;
	private final RestTemplate restTemplate;

	public OAuthUserInfoRes authorizeFromKakao(String code) {
		log.info("카카오 인증, 인가를 진행합니다...");

		KakaoTokenRes kakaoTokenRes = getTokenFromAuthorizationServer(code);
		KakaoUserInfoRes kakaoUserInfoRes = getUserInfoFromResourceServer(kakaoTokenRes);
		OAuthUserInfoRes oAuthUserInfoRes = getRequiredUserInfo("kakao", kakaoUserInfoRes);

		log.info("카카오 OAuth2 인증, 인가가 모두 완료됐습니다.");

		return oAuthUserInfoRes;
	}

	private KakaoTokenRes getTokenFromAuthorizationServer(String code) {
		log.info("Code(value: {})를 이용해 카카오 Authorization Server로부터 토큰을 발급합니다...", code);

		HttpHeaders headerForRequest = getHeaderForRequest(null);
		MultiValueMap<String, String> paramsForTokenRequest = getParamsForTokenRequest(code);

		String kakaoTokenUri = oAuth2KakaoRegistrationProperty.getTokenUri();
		try {
			log.info("카카오 Authorization Server({})에 토큰 발급을 요청합니다...", kakaoTokenUri);

			ResponseEntity<KakaoTokenRes> HttpKakaoRes = restTemplate.postForEntity(kakaoTokenUri,
				new HttpEntity<>(paramsForTokenRequest, headerForRequest), KakaoTokenRes.class);
			KakaoTokenRes kakaoTokenRes = HttpKakaoRes.getBody();

			log.info("카카오 Authorization Server로부터 토큰데이터({})를 받는데 성공했습니다.", kakaoTokenRes);

			return kakaoTokenRes;
		} catch (RuntimeException e) {
			log.warn("Authorization Code({})를 사용해 카카오 Authorization Server로부터 토큰을 발급받는데 실패했습니다.", code);
			log.warn("Error Message: {}", e.getMessage());
			throw new OAuth2AuthorizationException(ErrorCode.UNABLE_TO_GET_TOKEN_FROM_AUTH_SERVER);
		}
	}

	private KakaoUserInfoRes getUserInfoFromResourceServer(KakaoTokenRes kakaoTokenRes) {
		log.info("토큰 데이터({})를 이용해 카카오 Resource Server로부터 사용자 정보를 가져옵니다...", kakaoTokenRes);

		String accessToken = kakaoTokenRes.accessToken();
		HttpHeaders headerForRequest = getHeaderForRequest(accessToken);

		String userInfoUri = oAuth2KakaoRegistrationProperty.getUserInfoUri();
		try {
			log.info("카카오 Resource Server({})에 사용자 정보를 요청합니다...", userInfoUri);

			KakaoUserInfoRes kakaoUserInfoRes = restTemplate.postForObject(userInfoUri,
				new HttpEntity<>(headerForRequest), KakaoUserInfoRes.class);

			log.info("카카오 Resource Server로부터 사용자 정보({})를 받는데 성공했습니다.", kakaoUserInfoRes);

			return kakaoUserInfoRes;
		} catch (RuntimeException e) {
			log.warn("토큰 데이터({})를 사용해 카카오 Resource Server({})로부터 사용자 정보를 가져오는데 실패했습니다.",
				kakaoTokenRes, userInfoUri);
			throw new OAuth2AuthorizationException(ErrorCode.UNABLE_TO_GET_USER_INFO_FROM_RESOURCE_SERVER);
		}
	}

	private OAuthUserInfoRes getRequiredUserInfo(String provider, KakaoUserInfoRes kakaoUserInfoRes) {
		log.info("Resource Sever로부터 받은 사용자 정보({})에서 서비스에 필요한 데이터만 가져옵니다...", kakaoUserInfoRes);

		long providerId = kakaoUserInfoRes.id();
		KakaoUserInfoRes.Properties properties = kakaoUserInfoRes.properties();
		String nickName = properties.nickname();

		log.info("provider: {}, providerId: {}, nickName: {}", provider, providerId, nickName);

		return new OAuthUserInfoRes(provider, providerId, nickName);
	}

	private HttpHeaders getHeaderForRequest(String accessToken) {
		log.info("요청에 필요한 헤더를 세팅합니다...");

		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
		if (accessToken != null) {
			log.info("AccessToken({})값을 헤더에 추가합니다...", accessToken);

			headers.add("Authorization", "Bearer " + accessToken);
		}

		log.info("요청 헤더를 세팅하는데 성공했습니다.");

		return headers;
	}

	private MultiValueMap<String, String> getParamsForTokenRequest(String code) {
		log.info("토큰 발급에 필요한 파라미터를 세팅합니다...");

		String grantType = oAuth2KakaoRegistrationProperty.getGrantType();
		String clientId = oAuth2KakaoRegistrationProperty.getClientId();
		String clientSecret = oAuth2KakaoRegistrationProperty.getClientSecret();
		String redirectUri = oAuth2KakaoRegistrationProperty.getRedirectUri();

		log.info("grantType: {}, clientId: {},clientSecret: {}, redirectUri: {}, code: {}",
			grantType, clientId, clientSecret, redirectUri, code);

		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("grant_type", grantType);
		params.add("client_id", clientId);
		params.add("client_secret", clientSecret);
		params.add("redirect_uri", redirectUri);
		params.add("code", code);

		log.info("토큰 발급에 필요한 파라미터를 모두 세팅했습니다.");

		return params;
	}
}
