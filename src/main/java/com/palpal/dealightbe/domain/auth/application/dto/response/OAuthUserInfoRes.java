package com.palpal.dealightbe.domain.auth.application.dto.response;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public record OAuthUserInfoRes(
	String provider,
	Long providerId,
	String nickName
) {

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
			.append("provider", provider)
			.append("providerId", providerId)
			.append("nickName", nickName)
			.toString();
	}
}
