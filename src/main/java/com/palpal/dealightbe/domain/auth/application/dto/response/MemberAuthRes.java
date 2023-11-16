package com.palpal.dealightbe.domain.auth.application.dto.response;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public record MemberAuthRes(

	Long userId,
	String role,
	String accessToken,
	String refreshToken
) {

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
			.append("userId", userId)
			.append("role", role)
			.append("accessToken", accessToken)
			.append("refreshToken", refreshToken)
			.toString();
	}
}
