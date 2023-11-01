package com.palpal.dealightbe.domain.store.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.palpal.dealightbe.domain.address.application.AddressService;
import com.palpal.dealightbe.domain.address.application.dto.response.AddressRes;
import com.palpal.dealightbe.domain.address.domain.Address;
import com.palpal.dealightbe.domain.member.domain.Member;
import com.palpal.dealightbe.domain.member.domain.MemberRepository;
import com.palpal.dealightbe.domain.store.application.dto.request.StoreCreateReq;
import com.palpal.dealightbe.domain.store.application.dto.request.StoreStatusReq;
import com.palpal.dealightbe.domain.store.application.dto.request.StoreUpdateReq;
import com.palpal.dealightbe.domain.store.application.dto.response.StoreCreateRes;
import com.palpal.dealightbe.domain.store.application.dto.response.StoreInfoRes;
import com.palpal.dealightbe.domain.store.application.dto.response.StoreStatusUpdateRes;
import com.palpal.dealightbe.domain.store.domain.DayOff;
import com.palpal.dealightbe.domain.store.domain.Store;
import com.palpal.dealightbe.domain.store.domain.StoreRepository;
import com.palpal.dealightbe.domain.store.domain.StoreStatus;
import com.palpal.dealightbe.global.error.exception.BusinessException;
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
	private Store store;
	private Address address;

	@BeforeEach
	void setUp() {
		member = Member.builder()
			.nickName("홍섭")
			.realName("이홍섭")
			.phoneNumber("010010101")
			.provider("kakao")
			.providerId(123L)
			.build();

		address = Address.builder()
			.name("서울시 강남구")
			.xCoordinate(111)
			.yCoordinate(222)
			.build();

		store = Store.builder()
			.storeNumber("8888")
			.name("맛짱고기")
			.telephone("123123123")
			.address(address)
			.openTime(LocalTime.of(9, 0))
			.closeTime(LocalTime.of(23, 0))
			.dayOff(Set.of(DayOff.FRI))
			.build();
		store.updateMember(member);
	}

	@DisplayName("업체 등록 성공")
	@Test
	void registerStoreSuccessTest() {
		// given
		LocalTime openTime = LocalTime.of(9, 0);
		LocalTime closeTime = LocalTime.of(23, 0);
		StoreCreateReq storeCreateReq = new StoreCreateReq("888-222-111", "맛짱조개", "01066772291", "서울시 강남구", 67.89, 293.2323, openTime, closeTime, Set.of(DayOff.MON));

		when(memberRepository.findById(member.getId()))
			.thenReturn(Optional.of(member));
		when(addressService.register(eq("서울시 강남구"), eq(67.89), eq(293.2323)))
			.thenReturn(new AddressRes("서울시 강남구", 67.89, 293.2323));

		//when
		StoreCreateRes storeCreateRes = storeService.register(member.getId(), storeCreateReq);

		//then
		assertThat(storeCreateRes.name()).isEqualTo(storeCreateReq.name());
		assertThat(storeCreateRes.addressRes().name()).isEqualTo(storeCreateReq.addressName());
	}

	@DisplayName("업체 등록 성공 - 가게가 저녁에 문을 열고 새벽에 닫아도 성공")
	@Test
	void registerStoreSuccessTest_businessTime() {
		// given
		LocalTime openTime = LocalTime.of(15, 0);
		LocalTime closeTime = LocalTime.of(02, 0);
		StoreCreateReq storeCreateReq = new StoreCreateReq("888-222-111", "맛짱조개", "01066772291", "서울시 강남구", 67.89, 293.2323, openTime, closeTime, Set.of(DayOff.MON));

		when(memberRepository.findById(member.getId()))
			.thenReturn(Optional.of(member));
		when(addressService.register(eq("서울시 강남구"), eq(67.89), eq(293.2323)))
			.thenReturn(new AddressRes("서울시 강남구", 67.89, 293.2323));

		//when
		StoreCreateRes storeCreateRes = storeService.register(member.getId(), storeCreateReq);

		//then
		assertThat(storeCreateRes.name()).isEqualTo(storeCreateReq.name());
		assertThat(storeCreateRes.addressRes().name()).isEqualTo(storeCreateReq.addressName());
	}

	@Test
	@DisplayName("업체 등록 실패 - 존재하지 않는 회원")
	void registerStoreFailureTest_notFoundMember() {
		// given
		LocalTime openTime = LocalTime.of(9, 0);
		LocalTime closeTime = LocalTime.of(23, 0);
		StoreCreateReq storeCreateReq = new StoreCreateReq("888-222-111", "맛짱조개", "01066772291", "서울시 강남구", 67.89, 293.2323, openTime, closeTime, Set.of(DayOff.MON));

		when(memberRepository.findById(member.getId()))
			.thenReturn(Optional.empty());

		// when -> then
		assertThrows(EntityNotFoundException.class, () -> {
			storeService.register(member.getId(), storeCreateReq);
		});
	}

	@Test
	@DisplayName("업체 등록 실패 - 마감 시간이 오픈 시간 보다 빠른 경우")
	void registerStoreFailureTest_invalidBusinessHour() {
		// given
		LocalTime openTime = LocalTime.of(15, 0);
		LocalTime closeTime = LocalTime.of(13, 0);
		StoreCreateReq storeCreateReq = new StoreCreateReq("888-222-111", "맛짱조개", "01066772291", "서울시 강남구", 67.89, 293.2323, openTime, closeTime, Set.of(DayOff.MON));

		when(memberRepository.findById(member.getId()))
			.thenReturn(Optional.of(member));
		when(addressService.register(eq("서울시 강남구"), eq(67.89), eq(293.2323)))
			.thenReturn(new AddressRes("서울시 강남구", 67.89, 293.2323));

		// when -> then
		assertThrows(BusinessException.class, () -> {
			storeService.register(member.getId(), storeCreateReq);
		});
	}

	@Test
	@DisplayName("업체 마이페이지 조회 성공")
	void getStoreInfoSuccessTest() throws Exception {

		//given
		when(memberRepository.findById(member.getId()))
			.thenReturn(Optional.of(member));
		when(storeRepository.findById(store.getId()))
			.thenReturn(Optional.of(store));

		//when
		StoreInfoRes infoRes = storeService.getInfo(member.getId(), store.getId());

		//then
		assertThat(infoRes.storeNumber()).isEqualTo(store.getStoreNumber());
		assertThat(infoRes.addressName()).isEqualTo(store.getAddress().getName());
		assertThat(infoRes.dayOff()).isEqualTo(store.getDayOffs());
		assertThat(infoRes.storeStatus()).isEqualTo(store.getStoreStatus());
	}

	@Test
	@DisplayName("업체 마이페이지 조회 실패 - 소유자와 요청자가 같지 않음")
	void getStoreInfoSuccessTest_notMatchOwnerAndRequester() throws Exception {

		//given
		Member invalidMember = Member.builder()
			.nickName("이홍섭")
			.realName("김홍섭")
			.phoneNumber("01330010101")
			.provider("kakao")
			.providerId(999L)
			.build();

		when(memberRepository.findById(invalidMember.getId()))
			.thenReturn(Optional.of(invalidMember));
		when(storeRepository.findById(store.getId()))
			.thenReturn(Optional.of(store));

		//when -> then
		assertThrows(BusinessException.class, () -> {
			storeService.getInfo(invalidMember.getId(), store.getId());
		});
	}

	@Test
	@DisplayName("업체 마이페이지 정보 수정 성공")
	void updateInfoSuccessTest() throws Exception {

		//given
		LocalTime openTime = LocalTime.of(11, 0);
		LocalTime closeTime = LocalTime.of(12, 0);
		StoreUpdateReq updateReq = new StoreUpdateReq("77777", "부산시 수영구", 123.123, 222.333, openTime, closeTime, Set.of(DayOff.TUE));

		when(memberRepository.findById(member.getId()))
			.thenReturn(Optional.of(member));
		when(storeRepository.findById(store.getId()))
			.thenReturn(Optional.of(store));

		//when
		StoreInfoRes storeUpdatedInfoRes = storeService.updateInfo(member.getId(), store.getId(), updateReq);

		//then
		assertThat(storeUpdatedInfoRes.telephone()).isEqualTo(updateReq.telephone());
		assertThat(storeUpdatedInfoRes.addressName()).isEqualTo(updateReq.addressName());
		assertThat(storeUpdatedInfoRes.dayOff()).isEqualTo(updateReq.dayOff());
		assertThat(store.getName()).isEqualTo("맛짱고기");
		assertThat(store.getStoreNumber()).isEqualTo("8888");
	}

	@Test
	@DisplayName("업체 상태 정보 변경 성공")
	void updateStoreStatusSuccessTest() throws Exception {

		//given
		StoreStatusReq requestStoreStatus = new StoreStatusReq(StoreStatus.OPENED);

		when(memberRepository.findById(member.getId()))
			.thenReturn(Optional.of(member));
		when(storeRepository.findById(store.getId()))
			.thenReturn(Optional.of(store));

		//when
		StoreStatusUpdateRes storeStatusUpdateRes = storeService.updateStatus(member.getId(), store.getId(), requestStoreStatus);

		//then
		assertThat(store.getStoreStatus()).isEqualTo(requestStoreStatus.storeStatus());
		assertThat(storeStatusUpdateRes.storeId()).isEqualTo(store.getId());
	}
}
