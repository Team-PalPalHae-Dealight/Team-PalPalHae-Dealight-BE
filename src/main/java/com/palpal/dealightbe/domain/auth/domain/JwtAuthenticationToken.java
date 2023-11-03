package com.palpal.dealightbe.domain.auth.domain;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

	private final Object principal;
	private String credentials;

	public JwtAuthenticationToken(String principal, String credentials) {
		super(null);
		super.setAuthenticated(false);
		log.info("JwtAuthenticationToken을 생성합니다...");
		log.info("Authrities : null, Principal : {}, Credentials : {}, setAuthenticated : false",
			principal, credentials);

		this.principal = principal;
		this.credentials = credentials;
	}

	public JwtAuthenticationToken(Object principal, String credentials,
		Collection<? extends GrantedAuthority> authorities) {
		super(authorities);
		super.setAuthenticated(true);
		log.info("JwtAuthenticationToken을 생성합니다...");
		log.info("Authrities : {}, Principal : {}, Credentials : {}, setAuthenticated : true",
			authorities, principal, credentials);

		this.principal = principal;
		this.credentials = credentials;
	}

	@Override
	public Object getPrincipal() {
		return principal;
	}

	@Override
	public Object getCredentials() {
		return credentials;
	}

	@Override
	public void setAuthenticated(boolean authenticated) {
		if (authenticated) {
			throw new IllegalArgumentException("Authenticated 값을 true로 변경할 수 없습니다.");
		}
		super.setAuthenticated(false);
	}

	@Override
	public void eraseCredentials() {
		log.info("JwtAuthenticationToken의 Credential을 제거합니다...");
		super.eraseCredentials();
		credentials = null;
	}
}
