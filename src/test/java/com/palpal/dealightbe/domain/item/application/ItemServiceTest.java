package com.palpal.dealightbe.domain.item.application;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.palpal.dealightbe.domain.address.domain.Address;
import com.palpal.dealightbe.domain.item.application.dto.request.ItemReq;
import com.palpal.dealightbe.domain.item.application.dto.response.ItemRes;
import com.palpal.dealightbe.domain.item.application.dto.response.ItemsRes;
import com.palpal.dealightbe.domain.item.domain.Item;
import com.palpal.dealightbe.domain.item.domain.ItemRepository;
import com.palpal.dealightbe.domain.store.domain.DayOff;
import com.palpal.dealightbe.domain.store.domain.Store;
import com.palpal.dealightbe.domain.store.domain.StoreRepository;
import com.palpal.dealightbe.global.error.exception.BusinessException;
import com.palpal.dealightbe.global.error.exception.EntityNotFoundException;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

	@InjectMocks
	private ItemService itemService;

	@Mock
	private ItemRepository itemRepository;

	@Mock
	private StoreRepository storeRepository;

	private Store store;
	private Store store2;
	private Item item;
	private Item item2;

	@BeforeEach
	void setUp() {
		LocalTime openTime = LocalTime.of(13, 0);
		LocalTime closeTime = LocalTime.of(20, 0);

		Address address = Address.builder()
			.xCoordinate(127.0324773)
			.yCoordinate(37.5893876)
			.build();

		store = Store.builder()
			.name("동네분식")
			.storeNumber("0000000")
			.telephone("00000000")
			.openTime(openTime)
			.closeTime(closeTime)
			.dayOff(Collections.singleton(DayOff.MON))
			.address(address)
			.build();

		item = Item.builder()
			.name("떡볶이")
			.stock(2)
			.discountPrice(4000)
			.originalPrice(4500)
			.description("기본 떡볶이 입니다.")
			.information("통신사 할인 불가능 합니다.")
			.store(store)
			.build();

		Address address2 = Address.builder()
			.xCoordinate(127.0028245)
			.yCoordinate(37.5805009)
			.build();

		store2 = Store.builder()
			.name("먼분식")
			.storeNumber("0000000")
			.telephone("00000000")
			.openTime(LocalTime.of(17, 0))
			.closeTime(LocalTime.of(23, 30))
			.dayOff(Collections.singleton(DayOff.MON))
			.address(address2)
			.build();

		item2 = Item.builder()
			.name("김밥")
			.stock(3)
			.discountPrice(3000)
			.originalPrice(4500)
			.description("김밥 입니다.")
			.information("통신사 할인 불가능 합니다.")
			.store(store2)
			.build();
	}

	@DisplayName("상품 등록 성공 테스트")
	@Test
	void itemCreateSuccessTest() {
		//given
		ItemReq itemReq = new ItemReq(item.getName(), item.getStock(), item.getDiscountPrice(), item.getOriginalPrice(), item.getDescription(), item.getInformation(), item.getImage());
		Long memberId = 1L;

		when(storeRepository.findByMemberId(any())).thenReturn(Optional.of(store));
		when(itemRepository.existsByNameAndStoreId(any(), any())).thenReturn(false);
		when(itemRepository.save(any(Item.class))).thenReturn(item);

		//when
		ItemRes itemRes = itemService.create(itemReq, memberId);

		//then
		assertThat(itemRes.name()).isEqualTo(item.getName());
		assertThat(itemRes.stock()).isEqualTo(item.getStock());
		assertThat(itemRes.discountPrice()).isEqualTo(item.getDiscountPrice());
		assertThat(itemRes.originalPrice()).isEqualTo(item.getOriginalPrice());
		assertThat(itemRes.description()).isEqualTo(item.getDescription());
		assertThat(itemRes.information()).isEqualTo(item.getInformation());
	}

	@DisplayName("상품 등록 실패 테스트 - 존재하지 않는 업체")
	@Test
	void itemCreateFailureTest_storeNotFound() {
		//given
		ItemReq itemReq = new ItemReq(item.getName(), item.getStock(), item.getDiscountPrice(), item.getOriginalPrice(), item.getDescription(), item.getInformation(), item.getImage());
		Long memberId = 1L;

		when(storeRepository.findByMemberId(any())).thenReturn(Optional.empty());

		//when
		//then
		assertThrows(EntityNotFoundException.class, () -> {
			itemService.create(itemReq, memberId);
		});
	}

	@DisplayName("상품 등록 실패 테스트 - 할인가가 원가보다 큰 경우")
	@Test
	void itemCreateFailureTest_invalidDiscountPrice() {
		//given
		ItemReq itemReq = new ItemReq(item.getName(), item.getStock(), 4500, 4000, item.getDescription(), item.getInformation(), item.getImage());
		Long memberId = 1L;

		when(storeRepository.findByMemberId(any())).thenReturn(Optional.of(store));
		when(itemRepository.existsByNameAndStoreId(any(), any())).thenReturn(false);

		//when
		//then
		assertThrows(BusinessException.class, () -> {
			itemService.create(itemReq, memberId);
		});
	}

	@DisplayName("상품 상세 정보 조회(단건) 성공 테스트")
	@Test
	void itemFindByIdSuccessTest() {
		//given
		Long itemId = 1L;
		when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

		//when
		ItemRes itemRes = itemService.findById(itemId);

		//then
		assertThat(itemRes.name()).isEqualTo(item.getName());
		assertThat(itemRes.stock()).isEqualTo(item.getStock());
		assertThat(itemRes.discountPrice()).isEqualTo(item.getDiscountPrice());
		assertThat(itemRes.originalPrice()).isEqualTo(item.getOriginalPrice());
		assertThat(itemRes.description()).isEqualTo(item.getDescription());
		assertThat(itemRes.information()).isEqualTo(item.getInformation());
	}

	@DisplayName("상품 상세 정보 조회(단건) 실패 테스트 - 상품이 존재하지 않는 경우")
	@Test
	void itemFindByIdFailureTest_notFoundItem() {
		//given
		Long itemId = 1L;
		when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

		//when
		//then
		assertThrows(EntityNotFoundException.class,
			() -> itemService.findById(itemId)
		);
	}

	@DisplayName("상품 목록 조회(업체 시점) 성공 테스트")
	@Test
	void itemFindAllForStoreSuccessTest() {
		//given
		Long memberId = 1L;

		int page = 0;
		int size = 5;
		PageRequest pageRequest = PageRequest.of(page, size);

		List<Item> items = new ArrayList<>();
		items.add(item);
		items.add(item2);

		Page<Item> itemPage = new PageImpl<>(items, pageRequest, items.size());

		when(storeRepository.findByMemberId(any())).thenReturn(Optional.of(store));
		when(itemRepository.findAllByStoreIdOrderByUpdatedAtDesc(any(), eq(PageRequest.of(page, size)))).thenReturn(itemPage);

		//when
		ItemsRes itemsRes = itemService.findAllForStore(memberId, pageRequest);

		//then
		assertThat(itemsRes.itemResponses()).hasSize(items.size());
	}

	@DisplayName("상품 목록 조회(고객 시점) - 마감순 성공 테스트")
	@Test
	void itemFindAllForStoreSuccessSortByDeadlineTest() {
		//given
		int page = 0;
		int size = 5;
		PageRequest pageRequest = PageRequest.of(page, size);

		List<Item> items = new ArrayList<>();
		items.add(item);
		items.add(item2);

		Page<Item> itemPage = new PageImpl<>(items, pageRequest, items.size());

		double xCoordinate = 127.0221068;
		double yCoordinate = 37.5912999;

		when(itemRepository.findAllByDeadline(anyDouble(), anyDouble(), eq(PageRequest.of(page, size)))).thenReturn(itemPage);

		//when
		ItemsRes itemsRes = itemService.findAllForMember(xCoordinate, yCoordinate, "deadline", pageRequest);

		//then
		assertThat(itemsRes.itemResponses()).hasSize(items.size());
	}

	@DisplayName("상품 목록 조회(고객 시점) - 할인율순 성공 테스트")
	@Test
	void itemFindAllForStoreSuccessSortByDiscountRateTest() {
		//given
		int page = 0;
		int size = 5;
		PageRequest pageRequest = PageRequest.of(page, size);

		List<Item> items = new ArrayList<>();
		items.add(item);
		items.add(item2);

		Page<Item> itemPage = new PageImpl<>(items, pageRequest, items.size());

		double xCoordinate = 127.0221068;
		double yCoordinate = 37.5912999;

		when(itemRepository.findAllByDiscountRate(anyDouble(), anyDouble(), eq(PageRequest.of(page, size)))).thenReturn(itemPage);

		//when
		ItemsRes itemsRes = itemService.findAllForMember(xCoordinate, yCoordinate, "discountRate", pageRequest);

		//then
		assertThat(itemsRes.itemResponses()).hasSize(items.size());
	}

	@DisplayName("상품 목록 조회(고객 시점) - 거리순 성공 테스트")
	@Test
	void itemFindAllForStoreSuccessSortByDistanceTest() {
		//given
		int page = 0;
		int size = 5;
		PageRequest pageRequest = PageRequest.of(page, size);

		List<Item> items = new ArrayList<>();
		items.add(item);
		items.add(item2);

		Page<Item> itemPage = new PageImpl<>(items, pageRequest, items.size());

		double xCoordinate = 127.0221068;
		double yCoordinate = 37.5912999;

		when(itemRepository.findAllByDistance(anyDouble(), anyDouble(), eq(PageRequest.of(page, size)))).thenReturn(itemPage);

		//when
		ItemsRes itemsRes = itemService.findAllForMember(xCoordinate, yCoordinate, "distance", pageRequest);

		//then
		assertThat(itemsRes.itemResponses()).hasSize(items.size());
	}

	@DisplayName("상품 수정 성공 테스트")
	@Test
	void itemUpdateSuccessTest() {
		//given
		ItemReq itemReq = new ItemReq("수정이름", 1, 3000, 3500, "상세 내용 수정", "안내 사항 수정", null);
		Long memberId = 1L;
		Long itemId = 1L;

		when(storeRepository.findByMemberId(any())).thenReturn(Optional.of(store));
		when(itemRepository.existsByNameAndStoreId(any(), any())).thenReturn(false);
		when(itemRepository.findById(any())).thenReturn(Optional.of(item));

		//when
		ItemRes itemRes = itemService.update(itemId, itemReq, memberId);

		//then
		assertThat(itemRes.name()).isEqualTo(itemReq.name());
		assertThat(itemRes.stock()).isEqualTo(itemReq.stock());
		assertThat(itemRes.discountPrice()).isEqualTo(itemReq.discountPrice());
		assertThat(itemRes.originalPrice()).isEqualTo(itemReq.originalPrice());
		assertThat(itemRes.description()).isEqualTo(itemReq.description());
		assertThat(itemRes.information()).isEqualTo(itemReq.information());
	}

	@DisplayName("상품 수정 실패 테스트 - 할인가가 원가보다 큰 경우")
	@Test
	void itemUpdateFailureTest_invalidDiscountPrice() {
		//given
		ItemReq itemReq = new ItemReq("수정이름", 1, 4000, 3500, "상세 내용 수정", "안내 사항 수정", null);
		Long memberId = 1L;
		Long itemId = 1L;

		when(storeRepository.findByMemberId(any())).thenReturn(Optional.of(store));
		when(itemRepository.existsByNameAndStoreId(any(), any())).thenReturn(false);
		when(itemRepository.findById(any())).thenReturn(Optional.of(item));

		//when
		//then
		assertThrows(BusinessException.class, () -> {
			itemService.update(itemId, itemReq, memberId);
		});
	}

	@DisplayName("상품 삭제 성공 테스트")
	@Test
	void itemDeleteSuccessTest() {
		//given
		//when
		assertDoesNotThrow(() -> itemRepository.delete(item));

		//then
		verify(itemRepository, times(1)).delete(item);
	}
}
