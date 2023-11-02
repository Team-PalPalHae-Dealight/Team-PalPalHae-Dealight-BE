package com.palpal.dealightbe.domain.auth.application.dto.request;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.palpal.dealightbe.domain.member.domain.Member;

public record MemberSignupReq(
	String provider,
	Long providerId,
	String realName,
	String nickName,
	String phoneNumber,
	String role
) {
	public static Member toMember(MemberSignupReq request) {
		String provider = request.provider();
		Long providerId = request.providerId();
		String realName = request.realName();
		String nickName = request.nickName();
		String phoneNumber = request.phoneNumber();

		return Member.builder()
			.provider(provider)
			.providerId(providerId)
			.realName(realName)
			.nickName(nickName)
			.phoneNumber(phoneNumber)
			.build();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
			.append("provider", provider)
			.append("providerId", providerId)
			.append("realName", realName)
			.append("nickName", nickName)
			.append("phoneNumber", phoneNumber)
			.append("role", role)
			.toString();
	}
}
