package com.palpal.dealightbe.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "oauth2.kakao")
@Configuration
public class OAuth2KakaoRegistrationProperty {
	private String clientName;
	private String clientId;
	private String clientSecret;
	private String scope;
	private String grantType;
	private String redirectUri;
	private String tokenUri;
	private String userInfoUri;
}
