package com.palpal.dealightbe.domain.member.application;

import org.springframework.stereotype.Service;

import com.palpal.dealightbe.domain.member.application.dto.response.MemberProfileRes;
import com.palpal.dealightbe.domain.member.domain.Member;
import com.palpal.dealightbe.domain.member.domain.MemberRepository;
import com.palpal.dealightbe.global.error.ErrorCode;
import com.palpal.dealightbe.global.error.exception.EntityNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberService {
	MemberRepository memberRepository;

	public MemberProfileRes getMemberProfile(Long memberId) {

		Member member = memberRepository.findById(memberId).orElseThrow(() -> {
			log.warn("GET:READ:NOT_FOUND_MEMBER_BY_ID : {}", memberId);
			throw new EntityNotFoundException(ErrorCode.NOT_FOUND_MEMBER);
		});

		return MemberProfileRes.from(member);

	}
}
