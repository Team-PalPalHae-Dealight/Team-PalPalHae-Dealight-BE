package com.palpal.dealightbe.domain.member.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import com.palpal.dealightbe.domain.address.application.dto.request.AddressReq;
import com.palpal.dealightbe.domain.address.application.dto.response.AddressRes;
import com.palpal.dealightbe.domain.address.domain.Address;
import com.palpal.dealightbe.domain.image.ImageService;
import com.palpal.dealightbe.domain.image.application.dto.request.ImageUploadReq;
import com.palpal.dealightbe.domain.image.application.dto.response.ImageRes;
import com.palpal.dealightbe.domain.member.application.dto.request.MemberUpdateReq;
import com.palpal.dealightbe.domain.member.application.dto.response.MemberProfileRes;
import com.palpal.dealightbe.domain.member.application.dto.response.MemberUpdateRes;
import com.palpal.dealightbe.domain.member.domain.Member;
import com.palpal.dealightbe.domain.member.domain.MemberRepository;
import com.palpal.dealightbe.domain.store.application.StoreService;
import com.palpal.dealightbe.global.error.ErrorCode;
import com.palpal.dealightbe.global.error.exception.BusinessException;
import com.palpal.dealightbe.global.error.exception.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

	@Mock
	private MemberRepository memberRepository;

	@InjectMocks
	private MemberService memberService;

	@Mock
	private ImageService imageService;

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

		given(memberRepository.findMemberByProviderId(memberId)).willReturn(Optional.of(mockMember));

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
		given(memberRepository.findMemberByProviderId(nonexistentMemberId)).willReturn(Optional.empty());

		// when & then
		assertThrows(EntityNotFoundException.class, () -> memberService.getMemberProfile(nonexistentMemberId));
	}

	@Test
	@DisplayName("멤버 프로필 수정 성공 테스트")
	void updateMemberProfileSuccessTest() {
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

		AddressReq newAddress = new AddressReq("부산", 35.1796, 129.0756);
		MemberUpdateReq request = new MemberUpdateReq("유느님", "01087654321", newAddress);

		given(memberRepository.findMemberByProviderId(memberId)).willReturn(Optional.of(mockMember));

		// when
		MemberUpdateRes updatedMemberRes = memberService.updateMemberProfile(memberId, request);

		// then
		assertEquals("유느님", updatedMemberRes.nickname());
		assertEquals("01087654321", updatedMemberRes.phoneNumber());
		assertEquals("부산", updatedMemberRes.address().name());
		assertEquals(35.1796, updatedMemberRes.address().xCoordinate());
		assertEquals(129.0756, updatedMemberRes.address().yCoordinate());
	}

	@Test
	@DisplayName("멤버 프로필 수정 실패 테스트: 멤버 ID가 존재하지 않는 경우")
	void updateMemberProfileNotFoundTest() {
		// given
		Long nonexistentMemberId = 999L;
		AddressReq newAddress = new AddressReq("부산", 35.1796, 129.0756);
		MemberUpdateReq request = new MemberUpdateReq("유느님", "01087654321", newAddress);

		given(memberRepository.findMemberByProviderId(nonexistentMemberId)).willReturn(Optional.empty());

		// when & then
		assertThrows(EntityNotFoundException.class,
			() -> memberService.updateMemberProfile(nonexistentMemberId, request));
	}

	@Test
	@DisplayName("멤버 프로필 주소 수정 성공 테스트")
	void updateMemberProfileAddressSuccessTest() {
		//given
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

		AddressReq newAddress = new AddressReq("부산", 35.1796, 129.0756);

		given(memberRepository.findMemberByProviderId(memberId)).willReturn(Optional.of(mockMember));

		//when
		AddressRes updatedAddressRes = memberService.updateMemberAddress(memberId, newAddress);

		//then
		assertEquals("부산", updatedAddressRes.name());
		assertEquals(35.1796, updatedAddressRes.xCoordinate());
		assertEquals(129.0756, updatedAddressRes.yCoordinate());
	}

	@Test
	@DisplayName("멤버 프로필 주소 수정 실패 테스트: 멤버 ID가 존재하지 않는 경우")
	void updateMemberProfileAddressNotFoundTest() {
		//given
		Long nonexistentMemberId = 999L;
		AddressReq newAddress = new AddressReq("부산", 35.1796, 129.0756);

		given(memberRepository.findMemberByProviderId(nonexistentMemberId)).willReturn(Optional.empty());

		//when & then
		assertThrows(EntityNotFoundException.class,
			() -> memberService.updateMemberAddress(nonexistentMemberId, newAddress));
	}

	@Test
	@DisplayName("멤버 이미지 업로드 성공 테스트")
	void updateMemberImageSuccessTest() {
		// given
		Long memberId = 1L;
		String mockImageUrl = "https://example.com/image.jpg";

		MultipartFile mockFile = mock(MultipartFile.class);
		ImageUploadReq imageUploadReq = new ImageUploadReq(mockFile);
		Member mockMember = Member.builder()
			.realName("유재석")
			.nickName("유산슬")
			.phoneNumber("01012345678")
			.build();

		given(imageService.store(mockFile)).willReturn(mockImageUrl);
		given(memberRepository.findMemberByProviderId(memberId)).willReturn(Optional.of(mockMember));

		// when
		ImageRes imageRes = memberService.updateMemberImage(memberId, imageUploadReq);

		// then
		assertEquals(mockImageUrl, imageRes.imageUrl());
	}

	@Test
	@DisplayName("멤버 이미지 업로드 실패 테스트: 멤버 ID가 존재하지 않는 경우")
	void updateMemberImageNotFoundTest() {
		// given
		Long nonexistentMemberId = 999L;

		MultipartFile mockFile = mock(MultipartFile.class);
		ImageUploadReq imageUploadReq = new ImageUploadReq(mockFile);

		given(memberRepository.findMemberByProviderId(nonexistentMemberId)).willReturn(Optional.empty());

		// when & then
		assertThrows(EntityNotFoundException.class,
			() -> memberService.updateMemberImage(nonexistentMemberId, imageUploadReq));
	}

	@Test
	@DisplayName("멤버 이미지 삭제 성공 테스트")
	void deleteMemberImageSuccessTest() {
		// given
		Long memberId = 1L;
		String mockImageUrl = "https://foo.com/image.jpg";

		Member mockMember = Member.builder()
			.realName("유재석")
			.nickName("유산슬")
			.phoneNumber("01012345678")
			.build();

		mockMember.updateImage(mockImageUrl);

		given(memberRepository.findMemberByProviderId(memberId))
			.willReturn(Optional.of(mockMember));
		doNothing().when(imageService).delete(anyString());

		// when
		memberService.deleteMemberImage(memberId);

		// then
		verify(imageService).delete(mockImageUrl);
		assertEquals(StoreService.DEFAULT_PATH, mockMember.getImage());
	}

	@Test
	@DisplayName("멤버 이미지 삭제 실패 테스트: 멤버 ID가 존재하지 않는 경우")
	void deleteMemberImageNotFoundTest() {
		// given
		Long nonexistentMemberId = 999L;

		given(memberRepository.findMemberByProviderId(nonexistentMemberId)).willReturn(Optional.empty());

		// when & then
		assertThrows(EntityNotFoundException.class,
			() -> memberService.deleteMemberImage(nonexistentMemberId));
	}

	@Test
	@DisplayName("멤버 이미지 삭제 실패 테스트: 이미 기본 이미지가 설정된 경우")
	void deleteMemberImageDefaultImageAlreadySetTest() {
		// given
		Long memberId = 1L;
		String defaultImageUrl = "https://team-08-bucket.s3.ap-northeast-2.amazonaws.com/image/member-default-image.png";

		Member mockMember = Member.builder()
			.realName("유재석")
			.nickName("유산슬")
			.phoneNumber("01012345678")
			.build();

		mockMember.updateImage(defaultImageUrl);

		given(memberRepository.findMemberByProviderId(memberId)).willReturn(Optional.of(mockMember));

		// when & then
		BusinessException thrown = assertThrows(BusinessException.class,
			() -> memberService.deleteMemberImage(memberId));
		assertEquals(ErrorCode.DEFAULT_IMAGE_ALREADY_SET, thrown.getErrorCode());
	}
}
