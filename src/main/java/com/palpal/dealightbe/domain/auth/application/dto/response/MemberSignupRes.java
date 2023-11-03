package com.palpal.dealightbe.domain.auth.application.dto.response;

public record MemberSignupRes(
	String nickName,
	String accessToken,
	String refreshToken
) {
}
