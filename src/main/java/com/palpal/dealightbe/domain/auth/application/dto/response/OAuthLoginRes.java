package com.palpal.dealightbe.domain.auth.application.dto.response;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public record OAuthLoginRes(
	String message,
	Object data
) {

	public static OAuthLoginRes from(Object data) {
		if (data instanceof JoinRequireRes) {
			return new OAuthLoginRes("딜라이트 서비스에 가입이 필요합니다.", data);
		}

		return new OAuthLoginRes("로그인에 성공했습니다.", data);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
			.append("message", message)
			.append("data", data)
			.toString();
	}
}
