package com.palpal.dealightbe.domain.auth.domain;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.core.GrantedAuthority;

import com.palpal.dealightbe.config.JwtConfig;
import com.palpal.dealightbe.domain.member.domain.Member;
import com.palpal.dealightbe.domain.member.domain.MemberRole;
import com.palpal.dealightbe.domain.member.domain.Role;
import com.palpal.dealightbe.domain.member.domain.RoleType;

import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.security.InvalidKeyException;
import io.jsonwebtoken.security.WeakKeyException;

class JwtTest {

	JwtConfig jwtConfig;
	Jwt jwt;
	List<MemberRole> memberRoles;
	Member testMember;

	@BeforeEach
	void setUp() {
		jwtConfig = new JwtConfig();
		jwtConfig.setIssuer("test");
		jwtConfig.setTokenSecret("db3GuBkkt0VD1C2dIcN3eGVa2f0LE7KkXXv8eySXkTVk4c=");
		jwtConfig.setAccessTokenExpiry(3_600_000L);
		jwtConfig.setRefreshTokenExpiry(1_296_000_000L);
		jwt = new Jwt(jwtConfig);

		testMember = Member
			.builder()
			.providerId(123L)
			.provider("test")
			.build();
		Role role = new Role(1L, RoleType.ROLE_MEMBER);
		memberRoles = new ArrayList<>();
		MemberRole memberRole = new MemberRole(testMember, role);
		memberRoles.add(memberRole);
		testMember.updateMemberRoles(memberRoles);
	}

	@DisplayName("토큰 생성 성공")
	@Test
	void createNotEmptyToken() {
		// when
		String accessToken = jwt.createAccessToken(testMember);
		String refreshToken = jwt.createRefreshToken(testMember);

		// then
		assertThat(accessToken).isNotEmpty();
		assertThat(refreshToken).isNotEmpty();
	}

	@DisplayName("토큰의 유효성 검증 성공")
	@Test
	void createValidToken() {
		// when
		String accessToken = jwt.createAccessToken(testMember);
		String refreshToken = jwt.createRefreshToken(testMember);

		boolean isValidAccessToken = jwt.validateToken(accessToken);
		boolean isValidRefreshToken = jwt.validateToken(refreshToken);

		// then
		assertThat(isValidAccessToken).isTrue();
		assertThat(isValidRefreshToken).isTrue();
	}

	@DisplayName("토큰으로부터 가져온 Subject 검증 성공")
	@Test
	void parseSubjectFromToken() {
		// when
		String accessToken = jwt.createAccessToken(testMember);
		String refreshToken = jwt.createRefreshToken(testMember);

		String accessTokenSubject = jwt.getSubject(accessToken);
		String refreshTokenSubject = jwt.getSubject(refreshToken);

		// then
		assertThat(accessTokenSubject).isEqualTo("123");
		assertThat(refreshTokenSubject).isEqualTo("123");
	}

	@DisplayName("토큰으로부터 가져온 Authorities 검증 성공")
	@Test
	void parseAuthoritiesFromToken() {
		// given
		String ROLE_MEMBER = RoleType.ROLE_MEMBER.name();

		// when
		String accessToken = jwt.createAccessToken(testMember);
		String refreshToken = jwt.createRefreshToken(testMember);

		Collection<? extends GrantedAuthority> accessTokenAuthorities = jwt.getAuthorities(accessToken);
		Collection<? extends GrantedAuthority> refreshTokenAuthorities = jwt.getAuthorities(refreshToken);
		String accessTokenAuthority = List.copyOf(accessTokenAuthorities)
			.get(0)
			.getAuthority();
		String refreshTokenAuthority = List.copyOf(refreshTokenAuthorities)
			.get(0)
			.getAuthority();

		// then
		assertThat(accessTokenAuthorities).isNotEmpty();
		assertThat(accessTokenAuthorities.size()).isEqualTo(1);
		assertThat(accessTokenAuthority).isEqualTo(ROLE_MEMBER);

		assertThat(refreshTokenAuthorities).isNotEmpty();
		assertThat(refreshTokenAuthorities.size()).isEqualTo(1);
		assertThat(refreshTokenAuthority).isEqualTo(ROLE_MEMBER);
	}

	@DisplayName("TokenSecret이 Null이거나 Empty일 경우 Jwt 객체 생성 실패")
	@NullAndEmptySource
	@ParameterizedTest
	void createFailIfTokenSecretIsNullOrEmpty(String invalidTokenSecret) {
		// given
		jwtConfig.setTokenSecret(invalidTokenSecret);

		// when -> then
		assertThatThrownBy(() -> new Jwt(jwtConfig))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@DisplayName("TokenSecret이 잘못된 경우 토큰 생성 실패")
	@ValueSource(strings = {"12", "123", "fboacn", "fhuireVOFEAWRPK4532DCM"})
	@ParameterizedTest
	void createFailIfTokenSecretIsInvalid(String invalidTokenSecret) {
		// given
		jwtConfig.setTokenSecret(invalidTokenSecret);
		Jwt testJwt = new Jwt(jwtConfig);

		// when -> then
		assertThatThrownBy(() -> testJwt.createAccessToken(testMember))
			.isInstanceOf(InvalidKeyException.class);
		assertThatThrownBy(() -> testJwt.createRefreshToken(testMember))
			.isInstanceOf(InvalidKeyException.class);
	}

	@DisplayName("TokenSecret의 길이가 1글자 이하일 때, 토큰 생성 실패")
	@ValueSource(strings = {"!", "1", "a", "A"})
	@ParameterizedTest
	void createFailIfSecretLengthIsNotValid(String invalidLengthTokenSecret) {
		// given
		jwtConfig.setTokenSecret(invalidLengthTokenSecret);
		Jwt testJwt = new Jwt(jwtConfig);

		// when -> then
		assertThatThrownBy(() -> testJwt.createAccessToken(testMember))
			.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> testJwt.createRefreshToken(testMember))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@DisplayName("8bit 이상의 안전한 TokenSecret(HS256)이 아니라면 토큰 생성에 실패")
	@ValueSource(strings = {"12", "ac", "AC", "123456789012345678901234", "AbCdEfGhIjKlMnOpQrStUvWx"})
	@ParameterizedTest
	void createFailIfSecretIsNotSafe(String notSafeTokenSecret) {
		// given
		jwtConfig.setTokenSecret(notSafeTokenSecret);
		Jwt testJwt = new Jwt(jwtConfig);

		// when -> then
		assertThatThrownBy(() -> testJwt.createAccessToken(testMember))
			.isInstanceOf(WeakKeyException.class);
		assertThatThrownBy(() -> testJwt.createRefreshToken(testMember))
			.isInstanceOf(WeakKeyException.class);
	}

	@DisplayName("TokenSecret이 특수문자로만 구성된 경우 토큰 생성 실패")
	@ValueSource(strings = {"!@#", "@!#$%!@#$%^", "!@#)(*&^%$#@", "#@$%^&*()_*&^%$#@!^&*()"})
	@ParameterizedTest
	void createFailIfTokenSecretHasNotValidCharacter(String invalidTokenSecret) {
		// given
		jwtConfig.setTokenSecret(invalidTokenSecret);
		Jwt testJwt = new Jwt(jwtConfig);

		// when -> then
		assertThatThrownBy(() -> testJwt.createAccessToken(testMember))
			.isInstanceOf(DecodingException.class);
		assertThatThrownBy(() -> testJwt.createRefreshToken(testMember))
			.isInstanceOf(DecodingException.class);
	}

	@DisplayName("Authority가 없는 경우 토큰 생성 실패")
	@Test
	void createFailIfAuthorityIsEmpty() {
		// given
		Member invalidMember = Member.builder()
			.providerId(123L)
			.build();

		// when -> then
		assertThatThrownBy(() -> jwt.createAccessToken(invalidMember))
			.isInstanceOf(NullPointerException.class);
		assertThatThrownBy(() -> jwt.createRefreshToken(invalidMember))
			.isInstanceOf(NullPointerException.class);
	}

	@DisplayName("토큰이 유효기간을 넘길 경우 토큰 검증 실패")
	@Test
	void validFailIfTokenExceedExpiryTime() {
		// given
		jwtConfig.setAccessTokenExpiry(0L);
		jwtConfig.setRefreshTokenExpiry(0L);
		Jwt testJwt = new Jwt(jwtConfig);

		// when
		String expiredAccessToken = testJwt.createAccessToken(testMember);
		String expiredRefreshToken = testJwt.createRefreshToken(testMember);

		boolean isValidAccessToken = testJwt.validateToken(expiredAccessToken);
		boolean isValidRefreshToken = testJwt.validateToken(expiredRefreshToken);

		// then
		assertThat(isValidAccessToken).isFalse();
		assertThat(isValidRefreshToken).isFalse();
	}
}
