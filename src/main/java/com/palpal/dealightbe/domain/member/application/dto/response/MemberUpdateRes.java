package com.palpal.dealightbe.domain.member.application.dto.response;

import com.palpal.dealightbe.domain.address.application.dto.response.AddressRes;
import com.palpal.dealightbe.domain.member.domain.Member;

public record MemberUpdateRes(
	String nickName,
	String phoneNumber,
	AddressRes address
) {
	public static MemberUpdateRes from(Member member) {
		return new MemberUpdateRes(
			member.getNickName(),
			member.getPhoneNumber(),
			AddressRes.from(member.getAddress())
		);
	}
}
