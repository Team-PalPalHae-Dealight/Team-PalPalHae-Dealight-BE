package com.palpal.dealightbe.domain.member.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.palpal.dealightbe.domain.address.domain.Address;
import com.palpal.dealightbe.domain.member.application.dto.response.MemberProfileRes;
import com.palpal.dealightbe.domain.member.domain.Member;
import com.palpal.dealightbe.domain.member.domain.MemberRepository;
import com.palpal.dealightbe.global.error.exception.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

	@Mock
	private MemberRepository memberRepository;

	@InjectMocks
	private MemberService memberService;

	@Test
	@DisplayName("멤버 프로필 조회 성공 테스트")
	void getMemberProfileSuccessTest() {
		// given
		Long memberId = 1L;
		Address mockAddress = Address.builder()
			.name("서울")
			.xCoordinate(37.5665)
			.yCoordinate(126.9780)
			.build();

		Member mockMember = Member.builder()
			.realName("유재석")
			.nickName("유산슬")
			.phoneNumber("01012345678")
			.build();

		mockMember.updateAddress(mockAddress);

		given(memberRepository.findById(memberId)).willReturn(Optional.of(mockMember));

		// when
		MemberProfileRes memberProfileRes = memberService.getMemberProfile(memberId);

		// then
		assertEquals("유재석", memberProfileRes.realName());
		assertEquals("유산슬", memberProfileRes.nickName());
		assertEquals("01012345678", memberProfileRes.phoneNumber());
		assertEquals("서울", memberProfileRes.address().name());
		assertEquals(37.5665, memberProfileRes.address().xCoordinate());
		assertEquals(126.9780, memberProfileRes.address().yCoordinate());
	}

	@Test
	@DisplayName("멤버 프로필 조회 실패 테스트: 멤버 ID가 존재하지 않는 경우")
	void getMemberProfileNotFoundTest() {
		// given
		Long nonexistentMemberId = 999L;
		given(memberRepository.findById(nonexistentMemberId)).willReturn(Optional.empty());

		// when & then
		assertThrows(EntityNotFoundException.class, () -> memberService.getMemberProfile(nonexistentMemberId));
	}
}
