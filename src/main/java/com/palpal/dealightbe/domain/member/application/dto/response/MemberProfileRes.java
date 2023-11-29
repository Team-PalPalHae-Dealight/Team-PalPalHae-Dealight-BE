package com.palpal.dealightbe.domain.member.application.dto.response;

import com.palpal.dealightbe.domain.address.application.dto.response.AddressRes;
import com.palpal.dealightbe.domain.member.domain.Member;

public record MemberProfileRes(
	Long providerId,
	String role,
	String realName,
	String nickName,
	String phoneNumber,
	AddressRes address
) {

	public static MemberProfileRes from(Member member) {
		return new MemberProfileRes(
			member.getProviderId(),
			member.getMemberRoles().get(0).getRole().getType().getRole(),
			member.getRealName(),
			member.getNickName(),
			member.getPhoneNumber(),
			AddressRes.from(member.getAddress())
		);
	}
}
