package com.palpal.dealightbe.domain.store.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.palpal.dealightbe.domain.address.domain.Address;
import com.palpal.dealightbe.domain.address.domain.AddressRepository;
import com.palpal.dealightbe.domain.item.domain.Item;
import com.palpal.dealightbe.domain.item.domain.ItemRepository;
import com.palpal.dealightbe.domain.member.domain.Member;
import com.palpal.dealightbe.domain.member.domain.MemberRepository;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class StoreRepositoryTest {

	@Autowired
	private StoreRepository storeRepository;

	@Autowired
	private ItemRepository itemRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private AddressRepository addressRepository;

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
		memberRepository.save(member);
		memberRepository.save(member2);
		memberRepository.save(member3);

		address = Address.builder()
			.name("강남역 2번 출구 약국")
			.xCoordinate(37.4974)
			.yCoordinate(127.0283)
			.build();

		address2 = Address.builder()
			.name("아리따움")
			.xCoordinate(37.49794)
			.yCoordinate(127.0286)
			.build();

		address3 = Address.builder()
			.name("강남역 순대")
			.xCoordinate(37.49801)
			.yCoordinate(127.0279)
			.build();

		addressRepository.save(address);
		addressRepository.save(address2);
		addressRepository.save(address3);

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

		storeRepository.save(store);
		storeRepository.save(store2);
		storeRepository.save(store3);

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

		itemRepository.save(item);
		itemRepository.save(item2);
		itemRepository.save(item3);
	}

	@Test
	@DisplayName("거리순 정렬 성공")
	void findByDistanceWithin3Km() throws Exception {

		//given
		double within3KmX = 37.4980136;
		double within3KmY = 127.0279372;
		String keyword = "떡볶이";

		double longDistanceX = 35.44142;
		double longDistanceY = 128.2417;
		String keyword2 = "순대";

		//when
		Slice<Store> stores = storeRepository.findByDistanceWithin3Km(within3KmX, within3KmY, keyword);
		Slice<Store> stores2 = storeRepository.findByDistanceWithin3Km(longDistanceX, longDistanceY, keyword2);

		//then
		assertThat(stores).isNotNull();
		assertThat(stores).hasSize(2);

		assertThat(stores2).isNotNull();
		assertThat(stores2).isEmpty();
	}

	@Test
	@DisplayName("마감임박순 정렬")
	void findByDeadline() throws Exception {

		//given
		double within3KmX = 37.4980136;
		double within3KmY = 127.0279372;
		String keyword = "떡볶이";

		//when
		Slice<Store> stores = storeRepository.findByDeadLine(within3KmX, within3KmY, keyword);

		//then
		assertThat(stores).isNotNull();
		assertThat(stores).hasSize(2);
		assertThat(stores.getContent().get(0).getName()).isEqualTo(store.getName());
	}

	@Test
	@DisplayName("할인율순 정렬")
	void findByDiscountRate() throws Exception {

		//given
		double within3KmX = 37.4980136;
		double within3KmY = 127.0279372;
		String keyword = "떡볶이";

		//when
		Slice<Store> stores = storeRepository.findByDiscountRate(within3KmX, within3KmY, keyword);

		//then
		assertThat(stores).isNotNull();
		assertThat(stores).hasSize(2);
		assertThat(stores.getContent().get(0).getName()).isEqualTo(store.getName());
	}
}
