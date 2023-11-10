package com.palpal.dealightbe.domain.auth.domain;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.palpal.dealightbe.config.JwtConfig;
import com.palpal.dealightbe.domain.member.domain.Member;
import com.palpal.dealightbe.domain.member.domain.MemberRole;
import com.palpal.dealightbe.domain.member.domain.Role;
import com.palpal.dealightbe.domain.member.domain.RoleType;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class Jwt {

	private final String issuer;
	private final String tokenSecret;
	private final Long accessTokenExpiry;
	private final Long refreshTokenExpiry;

	public Jwt(JwtConfig jwtConfig) {
		log.debug("Jwt 객체를 생성합니다...");
		validateJwtProperties(jwtConfig);
		this.issuer = jwtConfig.getIssuer();
		this.tokenSecret = jwtConfig.getTokenSecret();
		this.accessTokenExpiry = jwtConfig.getAccessTokenExpiry();
		this.refreshTokenExpiry = jwtConfig.getRefreshTokenExpiry();
		log.debug("Jwt 객체 생성에 성공했습니다.");
	}

	public String createAccessToken(Member member) {
		Long providerId = member.getProviderId();
		String subject = String.valueOf(providerId);
		log.info("AccessToken을 생성하는 유저({})", providerId);

		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + accessTokenExpiry);
		log.info("유저({})의 AccessToken 유효시간({})", providerId, expiryDate);

		List<MemberRole> memberRoles = member.getMemberRoles();
		String authorities = memberRoles.stream()
			.map(memberRole -> {
				Role role = memberRole.getRole();
				RoleType type = role.getType();

				return type.name();
			})
			.collect(Collectors.joining(","));
		log.info("유저({})의 권한({})", providerId, authorities);

		return Jwts.builder()
			.setSubject(subject)
			.setIssuer(issuer)
			.setIssuedAt(now)
			.setExpiration(expiryDate)
			.claim("Authorities", authorities)
			.signWith(SignatureAlgorithm.HS256, tokenSecret)
			.compact();
	}

	public String createRefreshToken(Member member) {
		Long providerId = member.getProviderId();
		String subject = String.valueOf(providerId);
		log.info("RefreshToken을 생성하는 유저({})", providerId);

		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + refreshTokenExpiry);
		log.info("유저({})의 RefreshToken 유효시간({})", providerId, expiryDate);

		List<MemberRole> memberRoles = member.getMemberRoles();
		String authorities = memberRoles.stream()
			.map(memberRole -> {
				Role role = memberRole.getRole();
				RoleType type = role.getType();

				return type.name();
			})
			.collect(Collectors.joining(","));
		log.info("유저({})의 권한({})", providerId, authorities);

		return Jwts.builder()
			.setSubject(subject)
			.setIssuer(issuer)
			.setIssuedAt(now)
			.setExpiration(expiryDate)
			.claim("Authorities", authorities)
			.signWith(SignatureAlgorithm.HS256, tokenSecret)
			.compact();
	}

	public String getSubject(String jwt) {
		log.info("Jwt(value: {})로부터 Subject를 가져옵니다...", jwt);
		Claims claims = Jwts.parserBuilder()
			.setSigningKey(tokenSecret)
			.build()
			.parseClaimsJws(jwt)
			.getBody();

		String subject = claims.getSubject();
		log.info("Subject({})를 가져오는데 성공했습니다.", subject);
		return subject;
	}

	public Collection<? extends GrantedAuthority> getAuthorities(String jwt) {
		log.info("Jwt(value: {})로부터 Authority를 가져옵니다...", jwt);
		Claims claims = Jwts.parserBuilder()
			.setSigningKey(tokenSecret)
			.build()
			.parseClaimsJws(jwt)
			.getBody();

		String authoritiesFromToken = claims.get("Authorities", String.class);
		List<SimpleGrantedAuthority> authorities = Arrays.stream(authoritiesFromToken.split(","))
			.map(SimpleGrantedAuthority::new)
			.toList();
		log.info("Authorities({})를 가져오는데 성공했습니다.", authorities);

		return authorities;
	}

	public void validateToken(String jwt) {
		log.info("Jwt(value: {})의 유효성 검증을 시작합니다...", jwt);
		Jwts.parserBuilder()
			.setSigningKey(tokenSecret)
			.build()
			.parseClaimsJws(jwt);
		log.info("Jwt(value: {})의 유효성이 검증되었습니다.", jwt);
	}

	private void validateJwtProperties(JwtConfig jwtConfig) {
		log.debug("Jwt 설정 값 검증을 시작합니다...");
		log.debug("Issuer : {}", jwtConfig.getIssuer());
		Assert.hasText(jwtConfig.getIssuer(), "Issuer 정보가 없습니다.");
		log.debug("AccessTokenSecret : {}", jwtConfig.getTokenSecret());
		Assert.hasText(jwtConfig.getTokenSecret(), "Token Secret 정보가 없습니다.");
		log.debug("AccessTokenExpiry : {}", jwtConfig.getAccessTokenExpiry());
		Assert.isInstanceOf(Long.class, jwtConfig.getAccessTokenExpiry(),
			"Access Token 만료시간이 올바르지 않습니다.");
		log.debug("RefreshTokenExpiry : {}", jwtConfig.getRefreshTokenExpiry());
		Assert.isInstanceOf(Long.class, jwtConfig.getRefreshTokenExpiry(),
			"Refresh Token 만료시간이 올바르지 않습니다.");
		log.debug("JwtConfig 설정 값 검증에 성공했습니다.");
	}

	public Date getExpiryDate(String jwt) {
		log.info("Jwt(value: {})로부터 토큰의 유효기간을 가져옵니다...", jwt);
		Claims claims = Jwts.parserBuilder()
			.setSigningKey(tokenSecret)
			.build()
			.parseClaimsJws(jwt)
			.getBody();
		Date expiration = claims.getExpiration();
		log.info("토큰의 유효기간({})을 가져오는데 성공했습니다.", expiration);

		return expiration;
	}
}
