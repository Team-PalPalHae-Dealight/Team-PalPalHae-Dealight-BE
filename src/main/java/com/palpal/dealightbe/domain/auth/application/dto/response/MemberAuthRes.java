package com.palpal.dealightbe.domain.auth.application.dto.response;

public record MemberAuthRes(

	Long userId,
	String role,
	String accessToken,
	String refreshToken
) {
}
