package com.palpal.dealightbe.domain.auth.application.dto.response;

public record LoginRes(
	Long providerId,
	String accessToken,
	String refreshToken
) {
}
