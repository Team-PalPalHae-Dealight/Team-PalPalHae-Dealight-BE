package com.palpal.dealightbe.domain.item.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.palpal.dealightbe.domain.address.domain.Address;
import com.palpal.dealightbe.domain.address.domain.AddressRepository;
import com.palpal.dealightbe.domain.member.domain.Member;
import com.palpal.dealightbe.domain.member.domain.MemberRepository;
import com.palpal.dealightbe.domain.store.domain.DayOff;
import com.palpal.dealightbe.domain.store.domain.Store;
import com.palpal.dealightbe.domain.store.domain.StoreRepository;
import com.palpal.dealightbe.domain.store.domain.StoreStatus;

@ExtendWith(SpringExtension.class)
@DataJpaTest
class ItemRepositoryTest {

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private AddressRepository addressRepository;

	@Autowired
	private StoreRepository storeRepository;

	@Autowired
	private ItemRepository itemRepository;

	private Item item1;
	private Item item2;
	private Item item3;
	private Item item4;
	private Item item5;
	private Item item6;

	@BeforeEach
	void setUp() {
		String storeDefaultImageUrl = "https://fake-image.com/store.png";

		Member member1 = Member.builder()
			.nickName("닉네임")
			.realName("이름")
			.phoneNumber("010010101")
			.provider("kakao")
			.providerId(123L)
			.build();

		memberRepository.save(member1);

		Member member2 = Member.builder()
			.nickName("닉네임2")
			.realName("이름")
			.phoneNumber("0100101021")
			.provider("kakao")
			.providerId(1234L)
			.build();

		memberRepository.save(member2);

		Member member3 = Member.builder()
			.nickName("닉네임3")
			.realName("이름")
			.phoneNumber("02100101021")
			.provider("kakao")
			.providerId(12345L)
			.build();

		memberRepository.save(member3);

		Member member4 = Member.builder()
			.nickName("닉네임4")
			.realName("이름")
			.phoneNumber("02100101021")
			.provider("kakao")
			.providerId(123456L)
			.build();

		memberRepository.save(member4);

		Member member5 = Member.builder()
			.nickName("닉네임5")
			.realName("이름")
			.phoneNumber("02100101021")
			.provider("kakao")
			.providerId(1234567L)
			.build();

		memberRepository.save(member5);

		Member member6 = Member.builder()
			.nickName("닉네임6")
			.realName("이름")
			.phoneNumber("02100101021")
			.provider("kakao")
			.providerId(123458L)
			.build();

		memberRepository.save(member6);

		Address address1 = Address.builder()
			.name("동네분식 주소")
			.xCoordinate(127.016539)
			.yCoordinate(37.592709)
			.build();

		addressRepository.save(address1);

		Store store1 = Store.builder()
			.name("동네분식")
			.storeNumber("0000000")
			.telephone("00000000")
			.openTime(LocalTime.of(13, 0))
			.closeTime(LocalTime.of(20, 0))
			.dayOff(Collections.singleton(DayOff.MON))
			.address(address1)
			.build();

		store1.updateImage(storeDefaultImageUrl);
		store1.updateStatus(StoreStatus.OPENED);
		store1.updateMember(member1);
		storeRepository.save(store1);

		item1 = Item.builder()
			.name("떡볶이")
			.stock(2)
			.discountPrice(3300)
			.originalPrice(4800)
			.description("기본 떡볶이 입니다.")
			.information("통신사 할인 불가능 합니다.")
			.image("https://fake-image.com/item1.png")
			.store(store1)
			.build();

		itemRepository.save(item1);

		Address address2 = Address.builder()
			.name("먼분식 주소")
			.xCoordinate(126.977041)
			.yCoordinate(37.579617)
			.build();

		addressRepository.save(address2);

		Store store2 = Store.builder()
			.name("먼분식")
			.storeNumber("0000000")
			.telephone("00000000")
			.openTime(LocalTime.of(13, 0))
			.closeTime(LocalTime.of(22, 0))
			.dayOff(Collections.singleton(DayOff.MON))
			.address(address2)
			.build();

		store2.updateImage(storeDefaultImageUrl);
		store2.updateStatus(StoreStatus.OPENED);
		store2.updateMember(member2);
		storeRepository.save(store2);

		item2 = Item.builder()
			.name("치즈 떡볶이")
			.stock(2)
			.discountPrice(3000)
			.originalPrice(3300)
			.description("치즈 떡볶이 입니다.")
			.information("통신사 할인 불가능 합니다.")
			.image("https://fake-image.com/item1.png")
			.store(store2)
			.build();

		itemRepository.save(item2);

		Address address3 = Address.builder()
			.name("가까운 분식 주소")
			.xCoordinate(127.0324773)
			.yCoordinate(37.5893876)
			.build();

		addressRepository.save(address3);

		Store store3 = Store.builder()
			.name("가까운 분식")
			.storeNumber("0000000")
			.telephone("00000000")
			.openTime(LocalTime.of(13, 0))
			.closeTime(LocalTime.of(2, 0))
			.dayOff(Collections.singleton(DayOff.MON))
			.address(address3)
			.build();

		store3.updateImage(storeDefaultImageUrl);
		store3.updateStatus(StoreStatus.OPENED);
		store3.updateMember(member3);
		storeRepository.save(store3);

		item3 = Item.builder()
			.name("순대")
			.stock(2)
			.discountPrice(1000)
			.originalPrice(2500)
			.description("순대 입니다.")
			.information("통신사 할인 불가능 합니다.")
			.image("https://fake-image.com/item1.png")
			.store(store3)
			.build();

		itemRepository.save(item3);

		Address address4 = Address.builder()
			.name("공원 분식 주소")
			.xCoordinate(127.0028245)
			.yCoordinate(37.5805009)
			.build();

		addressRepository.save(address4);

		Store store4 = Store.builder()
			.name("공원 분식")
			.storeNumber("0000000")
			.telephone("00000000")
			.openTime(LocalTime.of(13, 0))
			.closeTime(LocalTime.of(17, 30))
			.dayOff(Collections.singleton(DayOff.MON))
			.address(address4)
			.build();

		store4.updateImage(storeDefaultImageUrl);
		store4.updateStatus(StoreStatus.OPENED);
		store4.updateMember(member4);
		storeRepository.save(store4);

		item4 = Item.builder()
			.name("찹쌀 순대")
			.stock(2)
			.discountPrice(2010)
			.originalPrice(4800)
			.description("찹쌀 순대 입니다.")
			.information("통신사 할인 불가능 합니다.")
			.image("https://fake-image.com/item1.png")
			.store(store4)
			.build();

		itemRepository.save(item4);

		Address address5 = Address.builder()
			.name("공원 분식 주소")
			.xCoordinate(127.006008)
			.yCoordinate(37.588403)
			.build();

		addressRepository.save(address5);

		Store store5 = Store.builder()
			.name("공원 분식")
			.storeNumber("0000000")
			.telephone("00000000")
			.openTime(LocalTime.of(13, 0))
			.closeTime(LocalTime.of(18, 0))
			.dayOff(Collections.singleton(DayOff.MON))
			.address(address5)
			.build();

		store5.updateImage(storeDefaultImageUrl);
		store5.updateStatus(StoreStatus.OPENED);
		store5.updateMember(member5);
		storeRepository.save(store5);

		item5 = Item.builder()
			.name("김밥")
			.stock(2)
			.discountPrice(2400)
			.originalPrice(4550)
			.description("김밥 입니다.")
			.information("통신사 할인 불가능 합니다.")
			.image("https://fake-image.com/item1.png")
			.store(store5)
			.build();

		itemRepository.save(item5);

		Address address6 = Address.builder()
			.name("근처 분식 주소")
			.xCoordinate(127.0581605)
			.yCoordinate(37.5972565)
			.build();

		addressRepository.save(address6);

		Store store6 = Store.builder()
			.name("근처 분식")
			.storeNumber("0000000")
			.telephone("00000000")
			.openTime(LocalTime.of(13, 0))
			.closeTime(LocalTime.of(23, 0))
			.dayOff(Collections.singleton(DayOff.MON))
			.address(address6)
			.build();

		store6.updateImage(storeDefaultImageUrl);
		store6.updateStatus(StoreStatus.OPENED);
		store6.updateMember(member6);
		storeRepository.save(store6);

		item6 = Item.builder()
			.name("김밥")
			.stock(2)
			.discountPrice(3500)
			.originalPrice(4900)
			.description("김밥 입니다.")
			.information("통신사 할인 불가능 합니다.")
			.image("https://fake-image.com/item1.png")
			.store(store6)
			.build();

		itemRepository.save(item6);
	}

	@DisplayName("영업중인 업체 중 기준 위치 반경 3km 내 업체들의 상품 목록 마감순 정렬 테스트")
	@Test
	void findAllByDeadline() {
		//given
		double standardXCoordinate = 127.0221068;
		double standardYCoordinate = 37.5912999;

		Pageable pageable = PageRequest.of(0, 5);

		List<Item> items = new ArrayList<>(List.of(item1, item3, item4, item5));

		List<Item> expected = sortByStoreCloseTime(items);

		//when
		Slice<Item> sliceResult = itemRepository.findAllByDeadline(standardXCoordinate, standardYCoordinate, pageable);
		List<Item> result = sliceResult.stream().toList();

		//then
		assertThat(sliceResult.getSize()).isEqualTo(pageable.getPageSize());
		assertThat(result).containsExactlyElementsOf(expected);
	}

	@DisplayName("영업중인 업체 중 기준 위치 반경 3km 내 업체들의 상품 목록 할인순 정렬 테스트")
	@Test
	void findAllByDiscountRate() {
		//given
		double standardXCoordinate = 127.0221068;
		double standardYCoordinate = 37.5912999;

		Pageable pageable = PageRequest.of(0, 5);

		List<Item> expected = List.of(item3, item4, item5, item1);

		//when
		Slice<Item> result = itemRepository.findAllByDiscountRate(standardXCoordinate, standardYCoordinate, pageable);
		List<Item> items = result.stream().toList();

		//then
		assertThat(result.getSize()).isEqualTo(pageable.getPageSize());
		assertThat(items).containsExactlyElementsOf(expected);
	}

	@DisplayName("영업중인 업체 중 기준 위치 반경 3km 내 업체들의 상품 목록 거리순 정렬 테스트")
	@Test
	void findAllByDistance() {
		//given
		double standardXCoordinate = 127.0221068;
		double standardYCoordinate = 37.5912999;

		Pageable pageable = PageRequest.of(0, 5);

		List<Item> expected = List.of(item1, item3, item5, item4);

		//when
		Slice<Item> result = itemRepository.findAllByDistance(standardXCoordinate, standardYCoordinate, pageable);
		List<Item> items = result.stream().toList();

		//then
		assertThat(result.getSize()).isEqualTo(pageable.getPageSize());
		assertThat(items).containsExactlyElementsOf(expected);
	}

	private Long calculateExpirationSeconds(LocalTime storeCloseTime) {
		LocalDateTime currentDateTime2 = LocalDateTime.now();
		LocalDateTime closeDateTime = getCloseDateTime(currentDateTime2, storeCloseTime);

		return currentDateTime2.until(closeDateTime, ChronoUnit.SECONDS);
	}

	private LocalDateTime getCloseDateTime(LocalDateTime currentDateTime, LocalTime storeCloseTime) {
		if (storeCloseTime.isBefore(currentDateTime.toLocalTime())) {
			return currentDateTime.toLocalDate().atTime(storeCloseTime).plusDays(1);
		}

		return currentDateTime.toLocalDate().atTime(storeCloseTime);
	}

	private List<Item> sortByStoreCloseTime(List<Item> items) {
		items.sort((x, y) ->
			calculateExpirationSeconds(x.getStore().getCloseTime())
				.compareTo(calculateExpirationSeconds(y.getStore().getCloseTime())));

		return items;
	}
}
