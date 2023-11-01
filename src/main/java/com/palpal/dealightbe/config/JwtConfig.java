package com.palpal.dealightbe.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ConfigurationProperties(prefix = "jwt.properties")
@Configuration
public class JwtConfig {

	private String issuer;
	private String tokenSecret;
	private Long accessTokenExpiry;
	private Long refreshTokenExpiry;
}
