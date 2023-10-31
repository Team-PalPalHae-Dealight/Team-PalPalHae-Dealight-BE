package com.palpal.dealightbe.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;

import com.palpal.dealightbe.domain.auth.presentation.CustomOAuth2AuthenticationSuccessHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableWebSecurity
@Configuration
public class SecurityConfig {

	private final AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository;
	private final CustomOAuth2AuthenticationSuccessHandler customOAuth2AuthenticationSuccessHandler;

	public SecurityConfig(AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository,
		CustomOAuth2AuthenticationSuccessHandler customOAuth2AuthenticationSuccessHandler) {
		this.authorizationRequestRepository = authorizationRequestRepository;
		this.customOAuth2AuthenticationSuccessHandler = customOAuth2AuthenticationSuccessHandler;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
			.headers().disable()
			.csrf().disable()
			.httpBasic().disable()
			.formLogin().disable()
			.logout().disable()
			.rememberMe().disable()
			.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			.and()
			.oauth2Login()
			.authorizationEndpoint()
			.authorizationRequestRepository(authorizationRequestRepository)
			.and()
			.successHandler(customOAuth2AuthenticationSuccessHandler)
			.and()
			.build();
	}
}
