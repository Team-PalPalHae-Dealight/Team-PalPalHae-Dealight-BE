package com.palpal.dealightbe.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

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
	CorsConfigurationSource corsConfigurationSource() {
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		CorsConfiguration config = new CorsConfiguration();
		config.addAllowedHeader("*");
		config.addAllowedMethod("*");
		config.setAllowedOrigins(
			Arrays.asList("http://localhost:8080", "http://localhost:8081", "http://localhost:3000",
				"https://dev-dealight.vercel.app", "http://127.0.0.1:5500"));
		source.registerCorsConfiguration("/**", config);

		return source;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
			.cors().configurationSource(corsConfigurationSource())
			.and()
			.authorizeRequests()
			// 운영자용 URL
			.antMatchers(HttpMethod.OPTIONS,
				"/h2-console/**", "/actuator/**"
			).hasRole("ADMIN")
			// 서비스 URL
			.antMatchers(HttpMethod.OPTIONS,
				"/api/auth/signup"
			).permitAll()
			.antMatchers(HttpMethod.OPTIONS,
				"/api/**"
			).hasAnyRole("MEMBER", "STORE", "ADMIN")
			.and()
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
