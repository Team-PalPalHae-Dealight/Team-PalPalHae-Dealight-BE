package com.palpal.dealightbe.domain.member.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.palpal.dealightbe.domain.address.application.dto.request.AddressReq;
import com.palpal.dealightbe.domain.address.application.dto.response.AddressRes;
import com.palpal.dealightbe.domain.member.application.MemberService;
import com.palpal.dealightbe.domain.member.application.dto.request.MemberUpdateReq;
import com.palpal.dealightbe.domain.member.application.dto.response.MemberProfileRes;
import com.palpal.dealightbe.domain.member.application.dto.response.MemberUpdateRes;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/members")
public class MemberController {

	private final MemberService memberService;

	@GetMapping("/profiles/{memberId}")
	public ResponseEntity<MemberProfileRes> getMemberProfile(@PathVariable Long memberId) {

		MemberProfileRes memberProfileRes = memberService.getMemberProfile(memberId);

		return ResponseEntity.ok(memberProfileRes);
	}

	@PatchMapping("/profiles/{memberId}")
	public ResponseEntity<MemberUpdateRes> updateMemberProfile(@PathVariable Long memberId,
		@RequestBody MemberUpdateReq request) {
		MemberUpdateRes updatedMember = memberService.updateMemberProfile(memberId, request);
		return ResponseEntity.ok(updatedMember);
	}

	@PatchMapping("/address/{memberId}")
	public ResponseEntity<AddressRes> updateMemberAddress(@PathVariable Long memberId,
		@RequestBody AddressReq request) {
		AddressRes updatedAddress = memberService.updateMemberAddress(memberId, request);
		return ResponseEntity.ok(updatedAddress);
	}
}
