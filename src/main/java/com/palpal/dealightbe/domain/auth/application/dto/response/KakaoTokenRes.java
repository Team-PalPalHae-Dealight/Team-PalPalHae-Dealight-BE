package com.palpal.dealightbe.domain.auth.application.dto.response;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

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

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
			.append("tokenType", tokenType)
			.append("accessToken", accessToken)
			.append("idToken", idToken)
			.append("expiresIn", expiresIn)
			.append("refreshToken", refreshToken)
			.append("refreshTokenExpiresIn", refreshTokenExpiresIn)
			.append("scope", scope)
			.toString();
	}
}
