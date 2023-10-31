package com.palpal.dealightbe.domain.member.application.dto.response;

import com.palpal.dealightbe.domain.address.application.dto.response.AddressRes;
import com.palpal.dealightbe.domain.member.domain.Member;

public record MemberProfileRes(
	String realName,
	String nickName,
	String phoneNumber,
	AddressRes address
) {

	public static MemberProfileRes from(Member member) {
		return new MemberProfileRes(member.getRealName(), member.getNickName(), member.getPhoneNumber(),
			AddressRes.from(member.getAddress()));
	}
}
