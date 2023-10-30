package com.palpal.dealightbe.domain.auth.domain;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.palpal.dealightbe.config.JwtConfig;
import com.palpal.dealightbe.domain.member.domain.Member;
import com.palpal.dealightbe.domain.member.domain.MemberRole;
import com.palpal.dealightbe.domain.member.domain.Role;
import com.palpal.dealightbe.domain.member.domain.RoleType;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
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

	public String createAccessToken(Long providerId, Member member) {
		String subject = String.valueOf(providerId);
		log.info("AccessToken을 생성하는 유저({})", providerId);

		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + accessTokenExpiry);
		log.info("유저({})의 AccessToken 유효시간({})", providerId, expiryDate);

		List<MemberRole> memberRoles = member.getMemberRoles();
		String roles = memberRoles.stream()
			.map(memberRole -> {
				Role role = memberRole.getRole();
				RoleType type = role.getType();
				return type.name();
			})
			.collect(Collectors.joining(","));
		log.info("유저({})의 권한({})", providerId, roles);

		return Jwts.builder()
			.setSubject(subject)
			.setIssuer(issuer)
			.setIssuedAt(now)
			.setExpiration(expiryDate)
			.claim("authorities", roles)
			.signWith(SignatureAlgorithm.HS256, tokenSecret)
			.compact();
	}

	public String createRefreshToken(Long providerId) {
		String subject = String.valueOf(providerId);
		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + refreshTokenExpiry);

		return Jwts.builder()
			.setSubject(subject)
			.setIssuer(issuer)
			.setIssuedAt(now)
			.setExpiration(expiryDate)
			.signWith(SignatureAlgorithm.HS256, tokenSecret)
			.compact();
	}

	public String parseTokenFromHttpRequest(HttpServletRequest request) {
		String jwtWithBearer = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (validateTokenFromRequest(jwtWithBearer)) {
			return null;
		}
		return jwtWithBearer.substring(7);
	}

	public boolean validateToken(String jwt) {
		log.info("Jwt(value: {})의 유효성 검증을 시작합니다...", jwt);
		try {
			Jwts.parserBuilder()
				.setSigningKey(tokenSecret)
				.build()
				.parseClaimsJws(jwt);

			log.info("Jwt(value: {})의 유효성이 검증되었습니다.", jwt);
			return true;
		} catch (SignatureException ex) {
			log.error("JWT signature가 올바르지 않습니다.");
		} catch (MalformedJwtException ex) {
			log.error("JWT가 올바른 값이 아닙니다.");
		} catch (ExpiredJwtException ex) {
			log.error("JWT가 만료되었습니다.");
		} catch (UnsupportedJwtException ex) {
			log.error("지원하지 않는 형식의 JWT입니다.");
		} catch (IllegalArgumentException ex) {
			log.error("JWT의 claim 값들이 비어있습니다.");
		}

		log.info("Jwt(value: {})가 유효성 검증에 실패했습니다.", jwt);
		return false;
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

	private boolean validateTokenFromRequest(String jwtWithBearer) {
		return StringUtils.hasText(jwtWithBearer) && jwtWithBearer.startsWith("Bearer ");
	}
}
