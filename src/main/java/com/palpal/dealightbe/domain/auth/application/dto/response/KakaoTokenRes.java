package com.palpal.dealightbe.domain.auth.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoTokenRes(
	@JsonProperty("token_type")
	String tokenType,
	@JsonProperty("access_token")
	String accessToken,
	@JsonProperty("id_token")
	String idToken,
	@JsonProperty("expires_in")
	Integer expiresIn,
	@JsonProperty("refresh_token")
	String refreshToken,
	@JsonProperty("refresh_token_expires_in")
	Integer refreshTokenExpiresIn,
	String scope
) {
}
