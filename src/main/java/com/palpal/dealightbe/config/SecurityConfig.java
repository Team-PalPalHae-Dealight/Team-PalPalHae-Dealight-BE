package com.palpal.dealightbe.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.palpal.dealightbe.domain.auth.application.CustomAuthAccessDeniedHandler;
import com.palpal.dealightbe.domain.auth.application.CustomAuthenticationEntryPoint;
import com.palpal.dealightbe.domain.auth.domain.Jwt;
import com.palpal.dealightbe.domain.auth.filter.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
public class SecurityConfig {

	private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
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
			.antMatchers("/h2-console/**", "/actuator/**").hasAnyRole("ADMIN")
			// 서비스 URL
			.antMatchers("/api/auth/signup", "/api/auth/duplicate").permitAll()
			.antMatchers("/api/**").permitAll()
			.and()
			.headers().disable()
			.csrf().disable()
			.httpBasic().disable()
			.formLogin().disable()
			.logout().disable()
			.rememberMe().disable()
			.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			.and()
			.exceptionHandling()
			.authenticationEntryPoint(customAuthenticationEntryPoint)
			.accessDeniedHandler(customAuthAccessDeniedHandler)
			.and()
			.addFilterBefore(new JwtAuthenticationFilter(jwt), UsernamePasswordAuthenticationFilter.class)
			.build();
	}
}
