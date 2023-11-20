package com.palpal.dealightbe.domain.store.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
import org.springframework.mock.web.MockMultipartFile;

import com.palpal.dealightbe.domain.address.application.AddressService;
import com.palpal.dealightbe.domain.address.domain.Address;
import com.palpal.dealightbe.domain.image.ImageService;
import com.palpal.dealightbe.domain.image.application.dto.request.ImageUploadReq;
import com.palpal.dealightbe.domain.image.application.dto.response.ImageRes;
import com.palpal.dealightbe.domain.item.domain.Item;
import com.palpal.dealightbe.domain.member.domain.Member;
import com.palpal.dealightbe.domain.member.domain.MemberRepository;
import com.palpal.dealightbe.domain.store.application.dto.request.StoreCreateReq;
import com.palpal.dealightbe.domain.store.application.dto.request.StoreStatusReq;
import com.palpal.dealightbe.domain.store.application.dto.request.StoreUpdateReq;
import com.palpal.dealightbe.domain.store.application.dto.response.StoreByMemberRes;
import com.palpal.dealightbe.domain.store.application.dto.response.StoreCreateRes;
import com.palpal.dealightbe.domain.store.application.dto.response.StoreInfoRes;
import com.palpal.dealightbe.domain.store.application.dto.response.StoreStatusRes;
import com.palpal.dealightbe.domain.store.application.dto.response.StoresInfoSliceRes;
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

	@Mock
	private ImageService imageService;

	@InjectMocks
	private StoreService storeService;

	public static final String DEFAULT_PATH = "https://team-08-bucket.s3.ap-northeast-2.amazonaws.com/image/free-store-icon.png";
	private Member member;
	private Member member2;
	private Member member3;
	private Store store;
	private Store store2;
	private Store store3;
	private Item item;
	private Item item2;
	private Item item3;
	private Address address;
	private Address address2;
	private Address address3;

	@BeforeEach
	void setUp() {
		member = Member.builder()
			.nickName("홍섭")
			.realName("이홍섭")
			.phoneNumber("010010101")
			.provider("kakao")
			.providerId(123L)
			.build();

		member2 = Member.builder()
			.nickName("홍섭2")
			.realName("이홍섭섭")
			.phoneNumber("0100101021")
			.provider("kakao")
			.providerId(1234L)
			.build();

		member3 = Member.builder()
			.nickName("홍섭23")
			.realName("이홍섭섭섭")
			.phoneNumber("02100101021")
			.provider("kakao")
			.providerId(12345L)
			.build();

		address = Address.builder()
			.name("서울특별시 성북구 안암로 145")
			.xCoordinate(127.0324773)
			.yCoordinate(37.5893876)
			.build();

		address2 = Address.builder()
			.name("서울특별시 종로구 이화동 대학로8길 1")
			.xCoordinate(127.0028245)
			.yCoordinate(37.5805009)
			.build();

		store = Store.builder()
			.storeNumber("8888")
			.name("맛짱고기")
			.telephone("123123123")
			.address(address)
			.openTime(LocalTime.of(12, 0))
			.closeTime(LocalTime.of(20, 0))
			.dayOff(Set.of(DayOff.FRI))
			.build();
		store.updateStatus(StoreStatus.OPENED);
		store.updateMember(member);

		store2 = Store.builder()
			.storeNumber("8888")
			.name("떡볶이를 파는집")
			.telephone("123123123")
			.address(address2)
			.openTime(LocalTime.of(12, 0))
			.closeTime(LocalTime.of(21, 0))
			.dayOff(Set.of(DayOff.FRI))
			.build();
		store2.updateStatus(StoreStatus.OPENED);
		store2.updateMember(member2);

		store3 = Store.builder()
			.storeNumber("8888")
			.name("순대가 맛있는집")
			.telephone("123123123")
			.address(address3)
			.openTime(LocalTime.of(10, 0))
			.closeTime(LocalTime.of(11, 0))
			.dayOff(Set.of(DayOff.FRI))
			.build();
		store3.updateMember(member3);
		store3.updateStatus(StoreStatus.OPENED);

		item = Item.builder()
			.name("떡볶이")
			.stock(2)
			.discountPrice(3000)
			.originalPrice(4500)
			.description("기본 떡볶이 입니다.")
			.information("통신사 할인 불가능 합니다.")
			.image("https://fake-image.com/item1.png")
			.store(store)
			.build();

		item2 = Item.builder()
			.name("떡볶이")
			.stock(2)
			.discountPrice(4000)
			.originalPrice(4500)
			.description("기본 떡볶이 입니다.")
			.information("통신사 할인 불가능 합니다.")
			.image("https://fake-image.com/item1.png")
			.store(store2)
			.build();

		item3 = Item.builder()
			.name("어묵")
			.stock(2)
			.discountPrice(100)
			.originalPrice(4500)
			.description("기본 순대 입니다.")
			.information("통신사 할인 불가능 합니다.")
			.image("https://fake-image.com/item1.png")
			.store(store3)
			.build();

	}

	@DisplayName("업체 등록 성공")
	@Test
	void registerStoreSuccessTest() {
		// given
		LocalTime openTime = LocalTime.of(9, 0);
		LocalTime closeTime = LocalTime.of(23, 0);
		StoreCreateReq storeCreateReq = new StoreCreateReq("888-222-111", "맛짱조개", "01066772291", "서울시 강남구", 67.89, 293.2323, openTime, closeTime, Set.of(DayOff.MON));

		when(memberRepository.findMemberByProviderId(member.getProviderId()))
			.thenReturn(Optional.of(member));
		when(addressService.register(eq("서울시 강남구"), eq(67.89), eq(293.2323)))
			.thenReturn(new Address("서울시 강남구", 67.89, 293.2323));

		//when
		StoreCreateRes storeCreateRes = storeService.register(member.getProviderId(), storeCreateReq);

		//then
		assertThat(storeCreateRes.name()).isEqualTo(storeCreateReq.name());
		assertThat(storeCreateRes.addressRes().name()).isEqualTo(storeCreateReq.addressName());
		assertThat(storeCreateRes.imageUrl()).isEqualTo(DEFAULT_PATH);
	}

	@DisplayName("업체 등록 성공 - 가게가 저녁에 문을 열고 새벽에 닫아도 성공")
	@Test
	void registerStoreSuccessTest_businessTime() {
		// given
		LocalTime openTime = LocalTime.of(15, 0);
		LocalTime closeTime = LocalTime.of(02, 0);
		StoreCreateReq storeCreateReq = new StoreCreateReq("888-222-111", "맛짱조개", "01066772291", "서울시 강남구", 67.89, 293.2323, openTime, closeTime, Set.of(DayOff.MON));

		when(memberRepository.findMemberByProviderId(member.getProviderId()))
			.thenReturn(Optional.of(member));
		when(addressService.register(eq("서울시 강남구"), eq(67.89), eq(293.2323)))
			.thenReturn(new Address("서울시 강남구", 67.89, 293.2323));

		//when
		StoreCreateRes storeCreateRes = storeService.register(member.getProviderId(), storeCreateReq);

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

		when(memberRepository.findMemberByProviderId(member.getProviderId()))
			.thenReturn(Optional.empty());

		// when -> then
		assertThrows(EntityNotFoundException.class, () -> {
			storeService.register(member.getProviderId(), storeCreateReq);
		});
	}

	@Test
	@DisplayName("업체 등록 실패 - 마감 시간이 오픈 시간 보다 빠른 경우")
	void registerStoreFailureTest_invalidBusinessHour() {
		// given
		LocalTime openTime = LocalTime.of(15, 0);
		LocalTime closeTime = LocalTime.of(13, 0);
		StoreCreateReq storeCreateReq = new StoreCreateReq("888-222-111", "맛짱조개", "01066772291", "서울시 강남구", 67.89, 293.2323, openTime, closeTime, Set.of(DayOff.MON));

		when(memberRepository.findMemberByProviderId(member.getProviderId()))
			.thenReturn(Optional.of(member));
		when(addressService.register(eq("서울시 강남구"), eq(67.89), eq(293.2323)))
			.thenReturn(new Address("서울시 강남구", 67.89, 293.2323));

		// when -> then
		assertThrows(BusinessException.class, () -> {
			storeService.register(member.getProviderId(), storeCreateReq);
		});
	}

	@Test
	@DisplayName("업체 마이페이지 조회 성공")
	void getStoreInfoSuccessTest() throws Exception {

		//given
		when(memberRepository.findMemberByProviderId(member.getProviderId()))
			.thenReturn(Optional.of(member));
		when(storeRepository.findById(store.getId()))
			.thenReturn(Optional.of(store));

		//when
		StoreInfoRes infoRes = storeService.getInfo(member.getProviderId(), store.getId());

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

		when(memberRepository.findMemberByProviderId(invalidMember.getProviderId()))
			.thenReturn(Optional.of(invalidMember));
		when(storeRepository.findById(store.getId()))
			.thenReturn(Optional.of(store));

		//when -> then
		assertThrows(BusinessException.class, () -> {
			storeService.getInfo(invalidMember.getProviderId(), store.getId());
		});
	}

	@Test
	@DisplayName("업체 마이페이지 정보 수정 성공")
	void updateInfoSuccessTest() throws Exception {

		//given
		LocalTime openTime = LocalTime.of(11, 0);
		LocalTime closeTime = LocalTime.of(12, 0);
		StoreUpdateReq updateReq = new StoreUpdateReq("77777", "부산시 수영구", 123.123, 222.333, openTime, closeTime, Set.of(DayOff.TUE));

		when(memberRepository.findMemberByProviderId(member.getProviderId()))
			.thenReturn(Optional.of(member));
		when(storeRepository.findById(store.getId()))
			.thenReturn(Optional.of(store));

		//when
		StoreInfoRes storeUpdatedInfoRes = storeService.updateInfo(member.getProviderId(), store.getId(), updateReq);

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

		when(memberRepository.findMemberByProviderId(member.getProviderId()))
			.thenReturn(Optional.of(member));
		when(storeRepository.findById(store.getId()))
			.thenReturn(Optional.of(store));

		//when
		StoreStatusRes storeStatusRes = storeService.updateStatus(member.getProviderId(), store.getId(), requestStoreStatus);

		//then
		assertThat(store.getStoreStatus()).isEqualTo(requestStoreStatus.storeStatus());
		assertThat(storeStatusRes.storeId()).isEqualTo(store.getId());
	}

	@Test
	@DisplayName("업체 이미지 등록 성공")
	void uploadImageSuccessTest() throws Exception {

		//given
		MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "Spring Framework".getBytes());
		ImageUploadReq request = new ImageUploadReq(file);
		String imageUrl = "http://fakeimageurl.com/image.jpg";

		when(memberRepository.findMemberByProviderId(member.getProviderId()))
			.thenReturn(Optional.of(member));
		when(storeRepository.findById(store.getId()))
			.thenReturn(Optional.of(store));
		when(imageService.store(file)).thenReturn(imageUrl);

		//when
		ImageRes imageRes = storeService.uploadImage(member.getProviderId(), store.getId(), request);

		//then
		assertThat(imageRes.imageUrl()).isEqualTo(imageUrl);
	}

	@Test
	@DisplayName("업체 이미지 수정 성공")
	void updateImageSuccessTest() throws Exception {
		//given
		MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "Updated Image Content".getBytes());
		ImageUploadReq request = new ImageUploadReq(file);
		String updatedImageUrl = "http://updatedfakeimageurl.com/updated_image.jpg";

		when(memberRepository.findMemberByProviderId(member.getProviderId()))
			.thenReturn(Optional.of(member));
		when(storeRepository.findById(store.getId()))
			.thenReturn(Optional.of(store));

		String initialImage = "http://initialfakeimageurl.com/initial_image.jpg";
		store.updateImage(initialImage);
		when(imageService.store(file)).thenReturn(updatedImageUrl);

		//when
		ImageRes imageRes = storeService.updateImage(member.getProviderId(), store.getId(), request);

		//then
		assertThat(imageRes.imageUrl()).isEqualTo(updatedImageUrl);
	}

	@Test
	@DisplayName("providerId로 업체 조회 성공")
	void findByProviderIdSuccessTest() throws Exception {

		//given
		when(memberRepository.findMemberByProviderId(member.getProviderId()))
			.thenReturn(Optional.of(member));

		when((storeRepository.findByMemberProviderId(member.getProviderId())))
			.thenReturn(Optional.of(store));

		//when
		Long storeId = store.getId();
		StoreByMemberRes storeByMemberRes = storeService.findByProviderId(member.getProviderId());

		//then
		assertThat(storeByMemberRes.storeId()).isEqualTo(storeId);
	}

	@Test
	@DisplayName("providerId로 업체 조회 실패 - 업체를 등록하지 않은 고객")
	void findByProviderIdFailTest_notRegisterStore() throws Exception {

		//given
		when(memberRepository.findMemberByProviderId(member.getProviderId()))
			.thenReturn(Optional.of(member));
		when((storeRepository.findByMemberProviderId(member.getProviderId())))
			.thenReturn(Optional.empty());

		//when -> then
		assertThrows(EntityNotFoundException.class, () -> {
			storeService.findByProviderId(member.getProviderId());
		});
	}

	@Test
	@DisplayName("업체 기본 검색 - 3km 근방 내 가까운순으로 나온다.")
	void searchByKeywordAndWithin3Km() throws Exception {

		//given
		double xCoordinate = 127.0221068;
		double yCoordinate = 37.5912999;
		String keyword = "떡볶이";
		Long cursor = 10L;
		String sortBy = "distance";
		Pageable pageable = PageRequest.of(0, 2);

		List<Store> stores = new ArrayList<>();
		stores.add(store);
		stores.add(store2);

		SliceImpl<Store> storeSlice = new SliceImpl<>(stores);

		when(storeRepository.findByKeywordAndDistanceWithin3KmAndSortCondition(xCoordinate, yCoordinate, keyword, sortBy, cursor, pageable))
			.thenReturn(storeSlice);

		//when
		StoresInfoSliceRes search = storeService.search(xCoordinate, yCoordinate, keyword, "distance", cursor, pageable);

		//then
		assertThat(search.storeInfoSliceRes().get(0).name()).isEqualTo(store.getName());
	}

	@Test
	@DisplayName("업체 필터 검색 - 마감 임박순")
	void searchByKeywordAndDeadline() throws Exception {

		//given
		double xCoordinate = 127.0221068;
		double yCoordinate = 37.5912999;
		String keyword = "떡볶이";
		String sortBy = "deadline";
		Long cursor = 10L;

		Pageable pageable = PageRequest.of(0, 2);

		List<Store> stores = new ArrayList<>();
		stores.add(store);
		stores.add(store2);

		SliceImpl<Store> storeSlice = new SliceImpl<>(stores);

		when(storeRepository.findByKeywordAndDistanceWithin3KmAndSortCondition(xCoordinate, yCoordinate, keyword, sortBy, cursor, pageable))
			.thenReturn(storeSlice);

		//when
		StoresInfoSliceRes search = storeService.search(xCoordinate, yCoordinate, keyword, sortBy, cursor, pageable);

		//then
		assertThat(search.storeInfoSliceRes().size()).isEqualTo(2);
	}

	@Test
	@DisplayName("업체 필터 검색 - 할인률순")
	void searchByKeywordAndDiscountRate() throws Exception {

		//given
		double xCoordinate = 127.0221068;
		double yCoordinate = 37.5912999;
		String keyword = "떡볶이";
		String sortBy = "discount-rate";
		Long cursor = 10L;
		Pageable pageable = PageRequest.of(0, 2);

		List<Store> stores = new ArrayList<>();
		stores.add(store);
		stores.add(store2);

		SliceImpl<Store> storeSlice = new SliceImpl<>(stores);

		when(storeRepository.findByKeywordAndDistanceWithin3KmAndSortCondition(xCoordinate, yCoordinate, keyword, sortBy, cursor, pageable))
			.thenReturn(storeSlice);

		//when
		StoresInfoSliceRes search = storeService.search(xCoordinate, yCoordinate, keyword, sortBy, cursor, pageable);

		//then
		assertThat(search.storeInfoSliceRes().size()).isEqualTo(2);
	}
}
