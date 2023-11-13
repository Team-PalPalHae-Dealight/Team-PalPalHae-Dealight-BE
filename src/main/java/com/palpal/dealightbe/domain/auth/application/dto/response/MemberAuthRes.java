package com.palpal.dealightbe.domain.auth.application.dto.response;

public record MemberAuthRes(
	String nickName,
	String accessToken,
	String refreshToken
) {
}
