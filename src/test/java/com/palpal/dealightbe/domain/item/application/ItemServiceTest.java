package com.palpal.dealightbe.domain.item.application;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.palpal.dealightbe.domain.item.application.dto.request.ItemReq;
import com.palpal.dealightbe.domain.item.application.dto.response.ItemRes;
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
	private Item item;

	@BeforeEach
	void setUp() {
		store = Store.builder()
			.name("동네분식")
			.storeNumber("0000000")
			.telephone("00000000")
			.openTime(LocalTime.now())
			.closeTime(LocalTime.now().plusHours(6))
			.dayOff(Collections.singleton(DayOff.MON))
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
}
