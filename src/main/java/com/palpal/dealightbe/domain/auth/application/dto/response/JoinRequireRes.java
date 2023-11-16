package com.palpal.dealightbe.domain.auth.application.dto.response;

public record JoinRequireRes(
	String provider,
	Long providerId,
	String nickName
) {
}
