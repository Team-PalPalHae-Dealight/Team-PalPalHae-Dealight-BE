package com.palpal.dealightbe.domain.auth.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

import com.palpal.dealightbe.domain.address.domain.Address;
import com.palpal.dealightbe.domain.auth.application.dto.request.MemberAuthReq;
import com.palpal.dealightbe.domain.auth.application.dto.response.LoginRes;
import com.palpal.dealightbe.domain.auth.application.dto.response.MemberAuthRes;
import com.palpal.dealightbe.domain.auth.domain.Jwt;
import com.palpal.dealightbe.domain.member.domain.Member;
import com.palpal.dealightbe.domain.member.domain.MemberRepository;
import com.palpal.dealightbe.domain.member.domain.MemberRole;
import com.palpal.dealightbe.domain.member.domain.MemberRoleRepository;
import com.palpal.dealightbe.domain.member.domain.Role;
import com.palpal.dealightbe.domain.member.domain.RoleRepository;
import com.palpal.dealightbe.domain.member.domain.RoleType;
import com.palpal.dealightbe.global.error.exception.BusinessException;
import com.palpal.dealightbe.global.error.exception.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private MemberRepository memberRepository;
	@Mock
	private MemberRoleRepository memberRoleRepository;
	@Mock
	private RoleRepository roleRepository;
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

		when(memberRepository.findByProviderAndProviderId(member.getProvider(), member.getProviderId()))
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
		when(memberRepository.findByProviderAndProviderId(member.getProvider(), member.getProviderId()))
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

	@DisplayName("회원가입에 성공하면 토큰을 반환한다")
	@Test
	void returnTokensIfSignupSuccess() {
		// given
		MemberAuthReq memberSignupReq = new MemberAuthReq(
			"test",
			123L,
			"테스터",
			"tester",
			"01012341234",
			"member");

		Address mockAddress = Address.builder()
			.xCoordinate(0)
			.yCoordinate(0)
			.build();
		Member mockMember = MemberAuthReq.toMember(memberSignupReq);
		mockMember.updateAddress(mockAddress);

		Role mockRole = Role.builder()
			.id(1L)
			.type(RoleType.ROLE_MEMBER)
			.build();
		MemberRole memberRole = new MemberRole(mockMember, mockRole);
		List<MemberRole> mockMemberRoles = Collections.singletonList(memberRole);

		given(memberRepository.findByProviderAndProviderId(any(String.class), any(Long.class)))
			.willReturn(Optional.empty());
		given(roleRepository.findByRoleType(RoleType.ROLE_MEMBER))
			.willReturn(Optional.of(mockRole));
		given(memberRepository.save(any(Member.class)))
			.willReturn(mockMember);
		given(memberRoleRepository.saveAll(any()))
			.willReturn(mockMemberRoles);
		mockMember.updateMemberRoles(mockMemberRoles);

		given(jwt.createAccessToken(mockMember))
			.willReturn("MOCK_ACCESS_TOKEN");
		given(jwt.createRefreshToken(mockMember))
			.willReturn("MOCK_REFRESH_TOKEN");

		// when
		MemberAuthRes response = authService.signup(memberSignupReq);

		// then
		assertThat(response.accessToken()).isEqualTo("MOCK_ACCESS_TOKEN");
		assertThat(response.refreshToken()).isEqualTo("MOCK_REFRESH_TOKEN");
		assertThat(response.nickName()).isEqualTo("tester");
	}

	@DisplayName("이미 존재하는 회원인 경우 회원가입 실패")
	@Test
	void throwExceptionIfAlreadyExistMember() {
		// given
		MemberAuthReq request = new MemberAuthReq(
			"tester",
			123L,
			"고예성",
			"요송송",
			"01012341234",
			"member");
		Member duplicatedMember = MemberAuthReq.toMember(request);
		given(memberRepository.findByProviderAndProviderId("tester", 123L))
			.willReturn(Optional.of(duplicatedMember));

		// when -> then
		assertThatThrownBy(() -> authService.signup(request))
			.isInstanceOf(BusinessException.class);
	}

	@DisplayName("존재하지 않는 Role로 회원가입을 요청하는 경우 실패")
	@Test
	void throwExceptionIFNotFoundRole() {
		// given
		MemberAuthReq request = new MemberAuthReq(
			"tester",
			123L,
			"고예성",
			"요송송",
			"01012341234",
			"genius");
		Member testMember = Member.builder()
			.provider("tester")
			.providerId(123L)
			.realName("고예성")
			.nickName("요송송")
			.phoneNumber("01012341234")
			.build();

		given(memberRepository.findByProviderAndProviderId(request.provider(), request.providerId()))
			.willReturn(Optional.empty());
		given(memberRepository.save(any()))
			.willReturn(testMember);

		// when -> then
		assertThatThrownBy(() -> authService.signup(request))
			.isInstanceOf(BusinessException.class);
	}
}
