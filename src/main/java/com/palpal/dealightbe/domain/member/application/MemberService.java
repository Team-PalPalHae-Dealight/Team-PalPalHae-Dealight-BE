package com.palpal.dealightbe.domain.member.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.palpal.dealightbe.domain.address.application.dto.request.AddressReq;
import com.palpal.dealightbe.domain.address.application.dto.response.AddressRes;
import com.palpal.dealightbe.domain.member.application.dto.request.MemberUpdateReq;
import com.palpal.dealightbe.domain.member.application.dto.response.MemberProfileRes;
import com.palpal.dealightbe.domain.member.application.dto.response.MemberUpdateRes;
import com.palpal.dealightbe.domain.member.domain.Member;
import com.palpal.dealightbe.domain.member.domain.MemberRepository;
import com.palpal.dealightbe.global.error.ErrorCode;
import com.palpal.dealightbe.global.error.exception.EntityNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
@Transactional
public class MemberService {
	private final MemberRepository memberRepository;

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

		member.getAddress().updateInfo(
			request.address().name(),
			request.address().xCoordinate(),
			request.address().yCoordinate()
		);

		member.updateInfo(request.nickname(), request.phoneNumber(), member.getAddress());

		return MemberUpdateRes.from(member);
	}

	public AddressRes updateMemberAddress(Long memberId, AddressReq request) {
		Member member = memberRepository.findById(memberId).orElseThrow(() -> {
			log.warn("PATCH:UPDATE:NOT_FOUND_MEMBER_BY_ID : {}", memberId);
			throw new EntityNotFoundException(ErrorCode.NOT_FOUND_MEMBER);
		});

		member.getAddress().updateInfo(
			request.name(),
			request.xCoordinate(),
			request.yCoordinate()
		);

		return AddressRes.from(member.getAddress());
	}
}
