package com.palpal.dealightbe.domain.store.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.palpal.dealightbe.domain.address.application.AddressService;
import com.palpal.dealightbe.domain.address.application.dto.response.AddressRes;
import com.palpal.dealightbe.domain.member.domain.Member;
import com.palpal.dealightbe.domain.member.domain.MemberRepository;
import com.palpal.dealightbe.domain.store.application.dto.request.StoreCreateReq;
import com.palpal.dealightbe.domain.store.application.dto.response.StoreRes;
import com.palpal.dealightbe.domain.store.domain.StoreRepository;
import com.palpal.dealightbe.global.error.exception.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

	@Mock
	private StoreRepository storeRepository;

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private AddressService addressService;

	@InjectMocks
	private StoreService storeService;

	private Member member;

	@BeforeEach
	void setUp() {
		member = Member.builder()
			.nickName("홍섭")
			.realName("이홍섭")
			.phoneNumber("010010101")
			.build();
	}

	@DisplayName("업체 등록 성공 테스트")
	@Test
	void registerStoreSuccessTest() {
		// given
		LocalDateTime openTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 0));
		LocalDateTime closeTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 0));
		StoreCreateReq storeCreateReq = new StoreCreateReq("888-222-111", "맛짱조개", "01066772291", "서울시 강남구", 67.89, 293.2323, openTime, closeTime, "월요일");

		when(memberRepository.findById(member.getId()))
			.thenReturn(Optional.of(member));
		when(addressService.register(eq("서울시 강남구"), eq(67.89), eq(293.2323)))
			.thenReturn(new AddressRes("서울시 강남구", 67.89, 293.2323));

		//when
		StoreRes storeRes = storeService.register(member.getId(), storeCreateReq);

		//then
		assertThat(storeRes.name()).isEqualTo(storeCreateReq.name());
		assertThat(storeRes.addressRes().name()).isEqualTo(storeCreateReq.addressName());
	}

	@Test
	void registerStoreFailureTest() {
		// given
		LocalDateTime openTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 0));
		LocalDateTime closeTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 0));
		StoreCreateReq storeCreateReq = new StoreCreateReq("888-222-111", "맛짱조개", "01066772291", "서울시 강남구", 67.89, 293.2323, openTime, closeTime, "월요일");

		when(memberRepository.findById(member.getId())).thenReturn(Optional.empty());

		// when -> then
		assertThrows(EntityNotFoundException.class, () -> {
			storeService.register(member.getId(), storeCreateReq);
		});
	}
}
