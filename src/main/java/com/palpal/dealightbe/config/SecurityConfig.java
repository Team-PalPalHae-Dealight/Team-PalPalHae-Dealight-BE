package com.palpal.dealightbe.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.palpal.dealightbe.domain.auth.domain.Jwt;
import com.palpal.dealightbe.domain.auth.filter.JwtAuthenticationFilter;
import com.palpal.dealightbe.domain.auth.presentation.CustomAuthAccessDeniedHandler;
import com.palpal.dealightbe.domain.auth.presentation.CustomOAuth2AuthenticationSuccessHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
public class SecurityConfig {

	private final AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository;
	private final CustomOAuth2AuthenticationSuccessHandler customOAuth2AuthenticationSuccessHandler;
	private final CustomAuthAccessDeniedHandler customAuthAccessDeniedHandler;
	private final Jwt jwt;

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return web -> web.ignoring()
			// .antMatchers("/api/**")
			.antMatchers("/actuator/**")
			.antMatchers("/api/auth/signup")
			.antMatchers("/h2-console/**");
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
			// .authorizeRequests()
			// .antMatchers("/api/address/**").permitAll()
			// .antMatchers("/api/auth/**").permitAll()
			// .antMatchers("/api/image/**").permitAll()
			// .antMatchers("/api/item/**").permitAll()
			// .antMatchers("/api/member/**").permitAll()
			// .antMatchers("/api/order/**").permitAll()
			// .antMatchers("/api/review/**").permitAll()
			// .antMatchers("/api/store/**").permitAll()
			// .and()
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
			.exceptionHandling().accessDeniedHandler(customAuthAccessDeniedHandler)
			.and()
			.addFilterBefore(new JwtAuthenticationFilter(jwt), UsernamePasswordAuthenticationFilter.class)
			.build();
	}
}
