package com.palpal.dealightbe.domain.member.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.palpal.dealightbe.domain.address.application.dto.request.AddressReq;
import com.palpal.dealightbe.domain.address.application.dto.response.AddressRes;
import com.palpal.dealightbe.domain.image.dto.request.ImageUploadReq;
import com.palpal.dealightbe.domain.image.dto.response.ImageRes;
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

	@PatchMapping("/images/{memberId}")
	public ResponseEntity<ImageRes> updateMemberImage(@PathVariable Long memberId,
		@RequestParam MultipartFile file) {
		ImageUploadReq imageUploadReq = new ImageUploadReq(file);
		ImageRes imageRes = memberService.updateMemberImage(memberId, imageUploadReq);

		return ResponseEntity.ok(imageRes);
	}

	@DeleteMapping("/images/{memberId}")
	public ResponseEntity<Void> deleteMemberImage(@PathVariable Long memberId) {
		memberService.deleteMemberImage(memberId);

		return ResponseEntity.noContent()
			.build();
	}
}
