package com.palpal.dealightbe.domain.member.application.dto.request;

import com.palpal.dealightbe.domain.address.application.dto.request.AddressReq;

public record MemberUpdateReq(
	String nickname,
	String phoneNumber,
	AddressReq address
) {
}
