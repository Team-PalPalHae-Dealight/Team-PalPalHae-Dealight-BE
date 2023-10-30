package com.palpal.dealightbe.domain.member.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.palpal.dealightbe.domain.member.application.MemberService;
import com.palpal.dealightbe.domain.member.application.dto.response.MemberProfileRes;

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

}
