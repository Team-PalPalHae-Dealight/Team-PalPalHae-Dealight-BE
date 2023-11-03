package com.palpal.dealightbe.domain.auth.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.palpal.dealightbe.domain.auth.domain.Jwt;
import com.palpal.dealightbe.domain.auth.infra.AuthRepository;
import com.palpal.dealightbe.domain.member.domain.Member;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private AuthRepository authRepository;

	@Mock
	private Jwt jwt;

	@InjectMocks
	private AuthService authService;

	private OAuth2AuthenticationToken mockOAuth2AuthenticationToken;

	private Member member;

	@BeforeEach
	void setUp() {
		SimpleGrantedAuthority roleMember = new SimpleGrantedAuthority("ROLE_MEMBER");
		Collection<SimpleGrantedAuthority> mockAuthorities = new ArrayList<>();
		mockAuthorities.add(roleMember);

		Map<String, Object> mockAttributes = new HashMap<>();
		mockAttributes.put("id", 12345);

		OAuth2User mockOAuth2User = new DefaultOAuth2User(mockAuthorities, mockAttributes, "id");
		mockOAuth2AuthenticationToken = new OAuth2AuthenticationToken(mockOAuth2User, mockAuthorities,
			"mockAuthServer");

		member = Member.builder()
			.provider("mockAuthServer")
			.providerId(12345L)
			.build();
	}

	@DisplayName("Dealight에 등록된 사용자일 경우 올바른 loginResponse를 반환")
	@Test
	void loginSuccessIfMemberAlreadyRegisteredMember() {
		// given
		Long mockProviderId = 12345L;
		String mockAccessToken = "MOCK_ACCESS_TOKEN";
		String mockRefreshToken = "MOCK_REFRESH_TOKEN";

		when(authRepository.findByProviderAndProviderId(member.getProvider(), member.getProviderId()))
			.thenReturn(Optional.of(member));
		when(jwt.createAccessToken(member))
			.thenReturn(mockAccessToken);
		when(jwt.createRefreshToken(member))
			.thenReturn(mockRefreshToken);

		// when
		LoginRes loginResponse = authService.login(mockOAuth2AuthenticationToken);

		// then
		assertThat(loginResponse.providerId()).isEqualTo(mockProviderId);
		assertThat(loginResponse.accessToken()).isEqualTo(mockAccessToken);
		assertThat(loginResponse.refreshToken()).isEqualTo(mockRefreshToken);
	}

	@DisplayName("Dealight에 등록된 사용자가 아닐 경우 loginResponse를 null로 반환")
	@Test
	void loginFailIfNotRegisteredMember() {
		// given
		when(authRepository.findByProviderAndProviderId(member.getProvider(), member.getProviderId()))
			.thenReturn(Optional.empty());

		// when
		LoginRes loginResponse = authService.login(mockOAuth2AuthenticationToken);

		// then
		assertThatThrownBy(() -> loginResponse.providerId())
			.isInstanceOf(NullPointerException.class);
		assertThatThrownBy(() -> loginResponse.accessToken())
			.isInstanceOf(NullPointerException.class);
		assertThatThrownBy(() -> loginResponse.refreshToken())
			.isInstanceOf(NullPointerException.class);
		assertThat(loginResponse).isNull();
	}

	@DisplayName("OAuth2AuthenticationToken이 null일 경우 로그인 실패")
	@Test
	void loginFailIfOAuth2AuthenticationTokenIsNull() {
		// given
		OAuth2AuthenticationToken nullOAuth2AuthenticationToken = null;

		// when -> then
		assertThatThrownBy(() -> authService.login(nullOAuth2AuthenticationToken))
			.isInstanceOf(NullPointerException.class);
	}

	@DisplayName("provider가 null일 경우 로그인 실패")
	@Test
	void loginFailIfProviderIsNull() {
		// given
		SimpleGrantedAuthority roleMember = new SimpleGrantedAuthority("ROLE_MEMBER");
		Collection<SimpleGrantedAuthority> mockAuthorities = new ArrayList<>();
		mockAuthorities.add(roleMember);

		Map<String, Object> mockAttributes = new HashMap<>();
		mockAttributes.put("id", 12345);

		OAuth2User mockOAuth2User = new DefaultOAuth2User(mockAuthorities, mockAttributes, "id");

		// when -> then
		assertThatThrownBy(() -> new OAuth2AuthenticationToken(mockOAuth2User, mockAuthorities, null))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@DisplayName("providerId가 null일 경우 로그인 실패")
	@Test
	void loginFailIfProviderIdIsNull() {
		// given
		SimpleGrantedAuthority roleMember = new SimpleGrantedAuthority("ROLE_MEMBER");
		Collection<SimpleGrantedAuthority> mockAuthorities = new ArrayList<>();
		mockAuthorities.add(roleMember);

		Map<String, Object> mockAttributes = new HashMap<>();

		// when -> then
		assertThatThrownBy(() -> new DefaultOAuth2User(mockAuthorities, mockAttributes, "id"))
			.isInstanceOf(IllegalArgumentException.class);
	}
}
