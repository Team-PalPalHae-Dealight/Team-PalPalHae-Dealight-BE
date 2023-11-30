package com.palpal.dealightbe.domain.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.doNothing;
import static org.mockito.BDDMockito.doThrow;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.when;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.palpal.dealightbe.domain.address.domain.Address;
import com.palpal.dealightbe.domain.auth.application.dto.request.MemberNickNameCheckReq;
import com.palpal.dealightbe.domain.auth.application.dto.request.MemberSignupAuthReq;
import com.palpal.dealightbe.domain.auth.application.dto.response.MemberAuthRes;
import com.palpal.dealightbe.domain.auth.application.dto.response.OAuthLoginRes;
import com.palpal.dealightbe.domain.auth.application.dto.response.OAuthUserInfoRes;
import com.palpal.dealightbe.domain.auth.domain.Jwt;
import com.palpal.dealightbe.domain.image.ImageService;
import com.palpal.dealightbe.domain.member.domain.Member;
import com.palpal.dealightbe.domain.member.domain.MemberRepository;
import com.palpal.dealightbe.domain.member.domain.MemberRole;
import com.palpal.dealightbe.domain.member.domain.MemberRoleRepository;
import com.palpal.dealightbe.domain.member.domain.Role;
import com.palpal.dealightbe.domain.member.domain.RoleRepository;
import com.palpal.dealightbe.domain.member.domain.RoleType;
import com.palpal.dealightbe.domain.store.domain.StoreRepository;
import com.palpal.dealightbe.global.error.exception.BusinessException;
import com.palpal.dealightbe.global.error.exception.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private ImageService imageService;
	@Mock
	private MemberRepository memberRepository;
	@Mock
	private MemberRoleRepository memberRoleRepository;
	@Mock
	private RoleRepository roleRepository;
	@Mock
	private StoreRepository storeRepository;
	@Mock
	private Jwt jwt;

	@InjectMocks
	private AuthService authService;

	private Member member;

	@BeforeEach
	void setUp() {
		Role role = new Role(2L, RoleType.ROLE_MEMBER);
		member = Member.builder()
			.provider("mockAuthServer")
			.providerId(12345L)
			.build();
		MemberRole memberRole = new MemberRole(member, role);
		member.updateMemberRoles(Collections.singletonList(memberRole));
		member.updateImage("MOCK_IMAGE_URL");
	}

	@DisplayName("Dealight에 등록된 사용자일 경우 MemberAuthRes 반환")
	@Test
	void loginSuccessIfMemberAlreadyRegisteredMember() {
		// given
		Long mockProviderId = 12345L;
		OAuthUserInfoRes oAuthUserInfoRes
			= new OAuthUserInfoRes("mockAuthServer", mockProviderId, "요송송");

		when(memberRepository.findByProviderAndProviderId(member.getProvider(), member.getProviderId()))
			.thenReturn(Optional.of(member));
		when(jwt.createAccessToken(any()))
			.thenReturn("MOCK_ACCESS_TOKEN");
		when(jwt.createRefreshToken(any()))
			.thenReturn("MOCK_ACCESS_TOKEN");

		// when
		OAuthLoginRes oAuthLoginRes = authService.authenticate(oAuthUserInfoRes);
		MemberAuthRes memberAuthRes = (MemberAuthRes)oAuthLoginRes.data();

		// then
		assertThat(oAuthLoginRes.data())
			.isInstanceOf(MemberAuthRes.class);
		assertThat(memberAuthRes.accessToken())
			.isInstanceOf(String.class)
			.isNotEmpty();
		assertThat(memberAuthRes.refreshToken())
			.isInstanceOf(String.class)
			.isNotEmpty();
	}

	@DisplayName("Dealight에 등록된 사용자가 아닐 경우 OAuthUserInfoRes를 반환")
	@Test
	void loginFailIfNotRegisteredMemberReturnOAuthUserInfoRes() {
		// given
		Long mockProviderId = 12345L;
		OAuthUserInfoRes oAuthUserInfoRes
			= new OAuthUserInfoRes("mockAuthServer", mockProviderId, "요송송");

		when(memberRepository.findByProviderAndProviderId(member.getProvider(), member.getProviderId()))
			.thenReturn(Optional.empty());

		// when
		OAuthLoginRes oAuthLoginRes = authService.authenticate(oAuthUserInfoRes);
		OAuthUserInfoRes loginRes = (OAuthUserInfoRes)oAuthLoginRes.data();

		// then
		assertThat(oAuthLoginRes.data())
			.isInstanceOf(OAuthUserInfoRes.class);
		assertThat(loginRes.provider())
			.isEqualTo("mockAuthServer");
		assertThat(loginRes.providerId())
			.isEqualTo(mockProviderId);
		assertThat(loginRes.nickName())
			.isEqualTo("요송송");
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
		MemberSignupAuthReq memberSignupReq = new MemberSignupAuthReq(
			"test",
			123L,
			"테스터",
			"tester",
			"01012341234");

		Address mockAddress = Address.builder()
			.xCoordinate(0)
			.yCoordinate(0)
			.build();
		Member mockMember = MemberSignupAuthReq.toMember(memberSignupReq);
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
		assertThat(response.accessToken())
			.isEqualTo("MOCK_ACCESS_TOKEN");
		assertThat(response.refreshToken())
			.isEqualTo("MOCK_REFRESH_TOKEN");
	}

	@DisplayName("이미 존재하는 회원인 경우 회원가입 실패")
	@Test
	void throwExceptionIfAlreadyExistMember() {
		// given
		MemberSignupAuthReq request = new MemberSignupAuthReq(
			"tester",
			123L,
			"고예성",
			"요송송",
			"01012341234");
		Member duplicatedMember = MemberSignupAuthReq.toMember(request);
		given(memberRepository.findByProviderAndProviderId("tester", 123L))
			.willReturn(Optional.of(duplicatedMember));

		// when -> then
		assertThatThrownBy(() -> authService.signup(request))
			.isInstanceOf(BusinessException.class);
	}

	@DisplayName("회원탈퇴 성공")
	@Test
	void unregisterSuccess() {
		// given
		Long providerId = member.getProviderId();
		when(memberRepository.findMemberByProviderId(providerId))
			.thenReturn(Optional.of(member));
		doNothing().when(imageService)
			.delete(any());

		// when
		authService.unregister(providerId);

		// then
		verify(memberRepository, times(1))
			.delete(member);
	}

	@DisplayName("기본 이미지라면 삭제하지 않고, 회원탈퇴 성공")
	@Test
	void unregisterSuccessIfMemberHaveDefaultImage() {
		// given
		Long providerId = member.getProviderId();
		String memberImage = member.getImage();
		member.updateImage("https://team-08-bucket.s3.ap-northeast-2.amazonaws.com/image/member-default-image.png");
		when(memberRepository.findMemberByProviderId(providerId))
			.thenReturn(Optional.of(member));

		// when
		authService.unregister(providerId);

		// then
		verify(imageService, times(0))
			.delete(memberImage);
		verify(memberRepository, times(1))
			.delete(member);
	}

	@DisplayName("회원정보 조회에 실패할 경우, 회원탈퇴 실패")
	@Test
	void unregisterFailIfNotFoundMember() {
		// given
		Long providerId = member.getProviderId();
		when(memberRepository.findMemberByProviderId(providerId))
			.thenReturn(Optional.empty());

		// when ->then
		assertThatThrownBy(() -> authService.unregister(providerId))
			.isInstanceOf(EntityNotFoundException.class);
		verify(memberRepository, times(0))
			.delete(member);
	}

	@DisplayName("Token 재발급 성공: Access Token만 재발급")
	@ValueSource(longs = {11, 12, 13, 14, 15, 16, 17, 18, 19, 20})
	@ParameterizedTest
	void reissueOnlyAccessTokenSuccess(long days) {
		// given
		Long providerId = member.getProviderId();
		String mockRefreshToken = "MOCK_REFRESH_TOKEN";
		LocalDateTime expiryDateAfter20DaysFromCurrentTime
			= LocalDateTime.now().plusDays(days);
		Instant instant = expiryDateAfter20DaysFromCurrentTime
			.atZone(ZoneId.systemDefault()).toInstant();
		Date date = Date.from(instant);

		given(memberRepository.findMemberByProviderId(providerId))
			.willReturn(Optional.of(member));
		given(jwt.getExpiryDate(mockRefreshToken))
			.willReturn(date);

		// when
		authService.reissueToken(providerId, mockRefreshToken);

		// then
		verify(jwt, times(1))
			.createAccessToken(member);
	}

	@DisplayName("Access Token, Refresh Token 모두 재발급 성공")
	@ValueSource(longs = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10})
	@ParameterizedTest
	void reissueAllTokenSuccess(long days) {
		// given
		Long providerId = member.getProviderId();
		String mockRefreshToken = "MOCK_REFRESH_TOKEN";
		LocalDateTime expiryDateAfter20DaysFromCurrentTime
			= LocalDateTime.now().plusDays(days);
		Instant instant = expiryDateAfter20DaysFromCurrentTime
			.atZone(ZoneId.systemDefault()).toInstant();
		Date date = Date.from(instant);

		given(memberRepository.findMemberByProviderId(providerId))
			.willReturn(Optional.of(member));
		given(jwt.getExpiryDate(mockRefreshToken))
			.willReturn(date);

		// when
		authService.reissueToken(providerId, mockRefreshToken);

		// then
		verify(jwt, times(1))
			.createAccessToken(member);
		verify(jwt, times(1))
			.createRefreshToken(member);
	}

	@DisplayName("Member를 찾지 못하면 토큰 발급 실패")
	@Test
	void reissueFailIfMemberNotFound() {
		// given
		Long providerId = member.getProviderId();
		String mockRefreshToken = "MOCK_REFRESH_TOKEN";

		given(memberRepository.findMemberByProviderId(providerId))
			.willReturn(Optional.empty());

		// when -> then
		assertThatThrownBy(() -> authService.reissueToken(providerId, mockRefreshToken))
			.isInstanceOf(EntityNotFoundException.class);
	}

	@DisplayName("Jwt 객체에 문제가 발생하면 Access Token 발급에 실패")
	@Test
	void reissueFailIfJwtIsNotWorking() {
		// given
		Long providerId = member.getProviderId();
		String mockRefreshToken = "MOCK_REFRESH_TOKEN";

		given(memberRepository.findMemberByProviderId(providerId))
			.willReturn(Optional.of(member));
		doThrow(RuntimeException.class).when(jwt)
			.createAccessToken(member);

		// when -> then
		assertThatThrownBy(() -> authService.reissueToken(providerId, mockRefreshToken))
			.isInstanceOf(RuntimeException.class);
		verify(memberRepository, times(1))
			.findMemberByProviderId(providerId);
	}

	@DisplayName("닉네임 중복 검사 실패")
	@Test
	void successNickNameDuplicateCheck() {
		// given
		MemberNickNameCheckReq request = new MemberNickNameCheckReq("고요송");
		given(memberRepository.existsByNickName("고요송"))
			.willReturn(true);

		// when -> then
		assertThatThrownBy(() -> authService.checkDuplicateNickName(request))
			.isInstanceOf(BusinessException.class);
	}
}
