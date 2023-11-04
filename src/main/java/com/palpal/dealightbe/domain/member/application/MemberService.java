package com.palpal.dealightbe.domain.member.application;

import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.palpal.dealightbe.domain.address.application.dto.request.AddressReq;
import com.palpal.dealightbe.domain.address.application.dto.response.AddressRes;
import com.palpal.dealightbe.domain.auth.application.AuthService;
import com.palpal.dealightbe.domain.image.ImageService;
import com.palpal.dealightbe.domain.image.dto.request.ImageUploadReq;
import com.palpal.dealightbe.domain.image.dto.response.ImageRes;
import com.palpal.dealightbe.domain.member.application.dto.request.MemberUpdateReq;
import com.palpal.dealightbe.domain.member.application.dto.response.MemberProfileRes;
import com.palpal.dealightbe.domain.member.application.dto.response.MemberUpdateRes;
import com.palpal.dealightbe.domain.member.domain.Member;
import com.palpal.dealightbe.domain.member.domain.MemberRepository;
import com.palpal.dealightbe.domain.store.application.StoreService;
import com.palpal.dealightbe.global.error.ErrorCode;
import com.palpal.dealightbe.global.error.exception.BusinessException;
import com.palpal.dealightbe.global.error.exception.EntityNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberService {

	private final MemberRepository memberRepository;
	private final ImageService imageService;

	@Transactional(readOnly = true)
	public MemberProfileRes getMemberProfile(Long memberId) {

		Member member = memberRepository.findById(memberId).orElseThrow(() -> {
			log.warn("GET:READ:NOT_FOUND_MEMBER_BY_ID : {}", memberId);
			throw new EntityNotFoundException(ErrorCode.NOT_FOUND_MEMBER);
		});

		return MemberProfileRes.from(member);

	}

	public MemberUpdateRes updateMemberProfile(Long memberId, MemberUpdateReq request) {
		Member member = memberRepository.findById(memberId).orElseThrow(() -> {
			log.warn("PATCH:UPDATE:NOT_FOUND_MEMBER_BY_ID : {}", memberId);
			throw new EntityNotFoundException(ErrorCode.NOT_FOUND_MEMBER);
		});

		Member memberFromRequest = MemberUpdateReq.toMember(request);
		member.updateInfo(memberFromRequest);

		return MemberUpdateRes.from(member);
	}

	public AddressRes updateMemberAddress(Long memberId, AddressReq request) {
		Member member = memberRepository.findById(memberId).orElseThrow(() -> {
			log.warn("PATCH:UPDATE:NOT_FOUND_MEMBER_BY_ID : {}", memberId);
			throw new EntityNotFoundException(ErrorCode.NOT_FOUND_MEMBER);
		});

		member.getAddress().updateInfo(
			AddressReq.toAddress(request)
		);

		return AddressRes.from(member.getAddress());
	}

	public ImageRes updateMemberImage(Long memberId, ImageUploadReq request) {
		String imageUrl = imageService.store(request.file());

		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> {
				log.warn("PATCH:UPDATE:NOT_FOUND_MEMBER_BY_ID : {}", memberId);
				return new EntityNotFoundException(ErrorCode.NOT_FOUND_MEMBER);
			});

		member.updateImage(imageUrl);

		memberRepository.save(member);

		return ImageRes.from(member.getImage());
	}

	public void deleteMemberImage(Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> {
				log.warn("DELETE:DELETE:NOT_FOUND_MEMBER_BY_ID : {}", memberId);
				return new EntityNotFoundException(ErrorCode.NOT_FOUND_MEMBER);
			});

		String imageUrl = member.getImage();

		if (Objects.equals(imageUrl, AuthService.MEMBER_DEFAULT_IMAGE_PATH)) {
			log.warn("DELETE:DELETE:DEFAULT_IMAGE_ALREADY_SET : {}", memberId);
			throw new BusinessException(ErrorCode.DEFAULT_IMAGE_ALREADY_SET);
		}

		imageService.delete(imageUrl);
		member.updateImage(StoreService.DEFAULT_PATH);
		memberRepository.save(member);
	}
}
