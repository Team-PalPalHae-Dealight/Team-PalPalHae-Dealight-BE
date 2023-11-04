package com.palpal.dealightbe.domain.member.application.dto.request;

import com.palpal.dealightbe.domain.address.application.dto.request.AddressReq;
import com.palpal.dealightbe.domain.member.domain.Member;

public record MemberUpdateReq(
	String nickname,
	String phoneNumber,
	AddressReq address
) {
	public static Member toMember(MemberUpdateReq request) {
		return Member.builder()
			.nickName(request.nickname())
			.phoneNumber(request.phoneNumber())
			.address(AddressReq.toAddress(request.address()))
			.build();
	}
}
