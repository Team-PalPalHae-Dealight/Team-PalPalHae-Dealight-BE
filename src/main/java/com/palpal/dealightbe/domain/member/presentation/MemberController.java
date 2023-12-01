package com.palpal.dealightbe.domain.member.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.palpal.dealightbe.domain.address.application.dto.request.AddressReq;
import com.palpal.dealightbe.domain.address.application.dto.response.AddressRes;
import com.palpal.dealightbe.domain.image.application.dto.request.ImageUploadReq;
import com.palpal.dealightbe.domain.image.application.dto.response.ImageRes;
import com.palpal.dealightbe.domain.member.application.MemberService;
import com.palpal.dealightbe.domain.member.application.dto.request.MemberUpdateReq;
import com.palpal.dealightbe.domain.member.application.dto.response.MemberProfileRes;
import com.palpal.dealightbe.domain.member.application.dto.response.MemberUpdateRes;
import com.palpal.dealightbe.global.aop.ProviderId;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/members")
public class MemberController {

	private final MemberService memberService;

	@GetMapping("/profiles")
	@ProviderId
	public ResponseEntity<MemberProfileRes> getMemberProfile(Long providerId) {

		MemberProfileRes memberProfileRes = memberService.getMemberProfile(providerId);

		return ResponseEntity.ok(memberProfileRes);
	}

	@PatchMapping("/profiles")
	@ProviderId
	public ResponseEntity<MemberUpdateRes> updateMemberProfile(Long providerId,
		@RequestBody @Validated MemberUpdateReq request) {
		MemberUpdateRes updatedMember = memberService.updateMemberProfile(providerId, request);

		return ResponseEntity.ok(updatedMember);
	}

	@PatchMapping("/addresses")
	@ProviderId
	public ResponseEntity<AddressRes> updateMemberAddress(Long providerId,
		@RequestBody AddressReq request) {
		AddressRes updatedAddress = memberService.updateMemberAddress(providerId, request);

		return ResponseEntity.ok(updatedAddress);
	}

	@PatchMapping("/images")
	@ProviderId
	public ResponseEntity<ImageRes> updateMemberImage(Long providerId,
		@RequestParam MultipartFile file) {
		ImageUploadReq imageUploadReq = new ImageUploadReq(file);
		ImageRes imageRes = memberService.updateMemberImage(providerId, imageUploadReq);

		return ResponseEntity.ok(imageRes);
	}

	@DeleteMapping("/images")
	@ProviderId
	public ResponseEntity<Void> deleteMemberImage(Long providerId) {
		memberService.deleteMemberImage(providerId);

		return ResponseEntity.noContent()
			.build();
	}
}
