package com.palpal.dealightbe.domain.auth.application;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.palpal.dealightbe.config.OAuth2KakaoRegistrationProperty;
import com.palpal.dealightbe.domain.auth.application.dto.response.KakaoTokenRes;
import com.palpal.dealightbe.domain.auth.application.dto.response.KakaoUserInfoRes;
import com.palpal.dealightbe.domain.auth.exception.OAuth2AuthorizationException;
import com.palpal.dealightbe.global.error.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class OAuth2AuthorizationService {

	private final OAuth2KakaoRegistrationProperty oAuth2KakaoRegistrationProperty;
	private final RestTemplate restTemplate;

	public KakaoUserInfoRes authorizeKakao(String code) {
		KakaoTokenRes kakaoTokenRes = getTokenFromAuthorizationServer(code);
		return getUserInfoFromResourceServer(kakaoTokenRes);
	}

	private KakaoUserInfoRes getUserInfoFromResourceServer(KakaoTokenRes kakaoTokenRes) {
		String accessToken = kakaoTokenRes.accessToken();
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + accessToken);
		headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

		String userInfoUri = oAuth2KakaoRegistrationProperty.getUserInfoUri();
		try {
			KakaoUserInfoRes kakaoUserInfoRes = restTemplate.postForObject(userInfoUri, new HttpEntity<>(headers),
				KakaoUserInfoRes.class);

			return kakaoUserInfoRes;
		} catch (RuntimeException e) {
			throw new OAuth2AuthorizationException(ErrorCode.UNABLE_TO_GET_USER_INFO_FROM_RESOURCE_SERVER);
		}
	}

	private KakaoTokenRes getTokenFromAuthorizationServer(String code) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

		String authorizationGrantType = oAuth2KakaoRegistrationProperty.getGrantType();
		String clientId = oAuth2KakaoRegistrationProperty.getClientId();
		String clientSecret = oAuth2KakaoRegistrationProperty.getClientSecret();
		String redirectUri = oAuth2KakaoRegistrationProperty.getRedirectUri();
		String kakaoTokenUri = oAuth2KakaoRegistrationProperty.getTokenUri();

		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("grant_type", authorizationGrantType);
		params.add("client_id", clientId);
		params.add("client_secret", clientSecret);
		params.add("redirect_uri", redirectUri);
		params.add("code", code);

		try {
			ResponseEntity<KakaoTokenRes> HttpKakaoRes = restTemplate.postForEntity(kakaoTokenUri,
				new HttpEntity<>(params, headers), KakaoTokenRes.class);
			KakaoTokenRes kakaoTokenRes = HttpKakaoRes.getBody();

			return kakaoTokenRes;
		} catch (RuntimeException e) {
			throw new OAuth2AuthorizationException(ErrorCode.UNABLE_TO_GET_TOKEN_FROM_AUTH_SERVER);
		}
	}
}
