package com.palpal.dealightbe.domain.auth.application;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import com.palpal.dealightbe.domain.auth.domain.Jwt;
import com.palpal.dealightbe.domain.auth.infra.AuthRepository;
import com.palpal.dealightbe.domain.member.domain.Member;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class AuthService {

	private final AuthRepository authRepository;
	private final Jwt jwt;

	public LoginResponse login(OAuth2AuthenticationToken oAuth2AuthenticationToken) {
		String provider = oAuth2AuthenticationToken.getAuthorizedClientRegistrationId();
		Long providerId = Long.parseLong(oAuth2AuthenticationToken.getPrincipal().getName());

		return authRepository.findByProviderAndProviderId(provider, providerId)
			.map(member -> {
				log.info("사용자(provider: {}, providerId: {})의 로그인을 진행합니다.", provider, providerId);
				String accessToken = jwt.createAccessToken(providerId, member);
				String refreshToken = jwt.createRefreshToken(providerId);

				return new LoginResponse(providerId, accessToken, refreshToken);
			})
			.orElse(null);
	}
}
