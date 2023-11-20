package com.palpal.dealightbe.domain.auth.application.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.palpal.dealightbe.domain.member.domain.Member;

public record MemberSignupAuthReq(
	@NotBlank(message = "Provider는 비어있을 수 없습니다.")
	String provider,

	@NotNull(message = "ProviderId는 비어있을 수 없습니다.")
	Long providerId,
	@NotBlank(message = "사용자 이름은 필수 입력값입니다.")
	String realName,

	@NotBlank(message = "닉네임은 필수 입력값입니다.")
	@Pattern(regexp = "^[a-zA-Z0-9가-힣]+",
		message = "닉네임은 한글, 알파벳, 숫자만 사용할 수 있습니다.")
	String nickName,

	@NotBlank(message = "사용자 전화번호는 필수 입력값입니다.")
	@Pattern(regexp = "\\d+", message = "숫자만 입력해주세요.")
	String phoneNumber
) {
	public static Member toMember(MemberSignupAuthReq request) {
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
			.toString();
	}
}
