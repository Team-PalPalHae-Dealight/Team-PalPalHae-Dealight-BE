package com.palpal.dealightbe.domain.member.application.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import com.palpal.dealightbe.domain.address.application.dto.request.AddressReq;
import com.palpal.dealightbe.domain.member.domain.Member;

public record MemberUpdateReq(
	@NotBlank(message = "닉네임은 비어있을 수 없습니다.")
	@Pattern(regexp = "^[a-zA-Z0-9가-힣]+",
		message = "닉네임은 한글, 알파벳, 숫자만 사용할 수 있습니다.")
	String nickName,
	String phoneNumber,
	AddressReq address
) {
	public static Member toMember(MemberUpdateReq request) {
		return Member.builder()
			.nickName(request.nickName())
			.phoneNumber(request.phoneNumber())
			.address(AddressReq.toAddress(request.address()))
			.build();
	}
}
