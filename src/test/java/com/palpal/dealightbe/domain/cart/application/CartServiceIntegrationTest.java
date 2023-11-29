package com.palpal.dealightbe.domain.cart.application;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import com.palpal.dealightbe.config.ElasticTestContainer;
import com.palpal.dealightbe.config.RedisConfig;
import com.palpal.dealightbe.config.RedisTestContainerConfig;
import com.palpal.dealightbe.domain.address.domain.Address;
import com.palpal.dealightbe.domain.address.domain.AddressRepository;
import com.palpal.dealightbe.domain.cart.application.dto.request.CartReq;
import com.palpal.dealightbe.domain.cart.application.dto.request.CartsReq;
import com.palpal.dealightbe.domain.cart.application.dto.response.CartsRes;
import com.palpal.dealightbe.domain.cart.domain.CartAdditionType;
import com.palpal.dealightbe.domain.cart.domain.CartRepository;
import com.palpal.dealightbe.domain.item.domain.Item;
import com.palpal.dealightbe.domain.item.domain.ItemRepository;
import com.palpal.dealightbe.domain.member.domain.Member;
import com.palpal.dealightbe.domain.member.domain.MemberRepository;
import com.palpal.dealightbe.domain.store.domain.DayOff;
import com.palpal.dealightbe.domain.store.domain.Store;
import com.palpal.dealightbe.domain.store.domain.StoreRepository;
import com.palpal.dealightbe.domain.store.domain.StoreStatus;
import com.palpal.dealightbe.global.error.exception.BusinessException;
import com.palpal.dealightbe.global.error.exception.EntityNotFoundException;

@Import({ElasticTestContainer.class, RedisConfig.class})
@ExtendWith(RedisTestContainerConfig.class)
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CartServiceIntegrationTest {

	@Autowired
	private AddressRepository addressRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private StoreRepository storeRepository;

	@Autowired
	private ItemRepository itemRepository;

	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private CartService cartService;

	@AfterEach
	void tearDown() {
		cartRepository.deleteAll();
		itemRepository.deleteAll();
		storeRepository.deleteAll();
		memberRepository.deleteAll();
		addressRepository.deleteAll();
	}

	@Nested
	@DisplayName("장바구니 담기")
	class addItemTest {

		@Nested
		@DisplayName("성공")
		class Success {

			@DisplayName("장바구니 담기(1개) 성공")
			@Test
			void addItemSuccessTest() {
				//given
				Address address1 = createAddress("동네분식 주소", 127.016539, 37.592709);
				Member member1 = createMember("닉네임", 123L);
				Store store1 = createStore("동네 분식", LocalTime.of(13, 0), LocalTime.of(20, 0), address1, member1);
				Item item1 = createItem("떡볶이", 3300, 4800, "기본 떡볶이 입니다.", store1);

				Member member2 = createMember("닉네임2", 12345L);

				Long providerId = member2.getProviderId();
				Long itemId = item1.getId();
				CartAdditionType cartAdditionType = CartAdditionType.BY_CHECK;
				int addQuantity = 1;

				//when
				cartService.addItem(providerId, itemId, cartAdditionType);

				//then
				CartsRes result = cartService.findAllByProviderId(providerId);

				assertThat(result.carts()).hasSize(1);
				assertThat(result.carts().get(0).itemId()).isEqualTo(item1.getId());
				assertThat(result.carts().get(0).storeId()).isEqualTo(store1.getId());
				assertThat(result.carts().get(0).memberProviderId()).isEqualTo(providerId);
				assertThat(result.carts().get(0).itemName()).isEqualTo(item1.getName());
				assertThat(result.carts().get(0).stock()).isEqualTo(item1.getStock());
				assertThat(result.carts().get(0).discountPrice()).isEqualTo(item1.getDiscountPrice());
				assertThat(result.carts().get(0).itemImage()).isEqualTo(item1.getImage());
				assertThat(result.carts().get(0).quantity()).isEqualTo(addQuantity);
				assertThat(result.carts().get(0).stock()).isEqualTo(item1.getStock());
				assertThat(result.carts().get(0).storeName()).isEqualTo(item1.getStore().getName());
			}
		}

		@Nested
		@DisplayName("실패")
		class Failure {

			@DisplayName("본인이 등록한 업체의 상품 담기 불가")
			@Test
			void addItemFailureTest_invalidAttemptOwnStoreItem() {
				//given
				Address address1 = createAddress("동네분식 주소", 127.016539, 37.592709);
				Member member1 = createMember("닉네임", 123L);
				Store store1 = createStore("동네 분식", LocalTime.of(13, 0), LocalTime.of(20, 0), address1, member1);
				Item item1 = createItem("떡볶이", 3300, 4800, "기본 떡볶이 입니다.", store1);

				Long providerId = member1.getProviderId();
				Long itemId = item1.getId();

				//when
				//then
				assertThrows(BusinessException.class,
					() -> cartService.addItem(providerId, itemId, CartAdditionType.BY_CHECK)
				);
			}

			@DisplayName("타 업체 상품이 존재하는 경우")
			@Test
			void addItemFailureTest_anotherStoreItemExists() {
				//given
				Address address1 = createAddress("동네분식 주소", 127.016539, 37.592709);
				Member member1 = createMember("닉네임", 123L);
				Store store1 = createStore("동네 분식", LocalTime.of(13, 0), LocalTime.of(20, 0), address1, member1);
				Item item1 = createItem("떡볶이", 3300, 4800, "기본 떡볶이 입니다.", store1);

				Address address2 = createAddress("먼분식 주소", 126.977041, 37.579617);
				Member member3 = createMember("닉네임2", 1234L);
				Store store2 = createStore("먼분식", LocalTime.of(13, 0), LocalTime.of(22, 0), address2, member3);
				Item item7 = createItem("키토 김밥", 3500, 4900, "키토 김밥 입니다.", store2);

				Member member2 = createMember("닉네임2", 12345L);

				Long providerId = member2.getProviderId();

				cartService.addItem(providerId, item1.getId(), CartAdditionType.BY_CHECK);

				//when
				//then
				assertThrows(BusinessException.class,
					() -> cartService.addItem(providerId, item7.getId(), CartAdditionType.BY_CHECK)
				);
			}

			@DisplayName("최대 5개 종류를 초과하여 장바구니 담기를 시도하는 경우")
			@Test
			void addItemFailureTest_exceededCartItemSize() {
				//given
				Address address1 = createAddress("동네분식 주소", 127.016539, 37.592709);
				Member member1 = createMember("닉네임", 123L);
				Store store1 = createStore("동네 분식", LocalTime.of(13, 0), LocalTime.of(20, 0), address1, member1);
				Item item1 = createItem("떡볶이", 3300, 4800, "기본 떡볶이 입니다.", store1);
				Item item2 = createItem("치즈 떡볶이", 3000, 3300, "치즈 떡볶이 입니다.", store1);
				Item item3 = createItem("순대", 1000, 2500, "순대 입니다.", store1);
				Item item4 = createItem("찹쌀 순대", 2010, 4800, "찹쌀 순대 입니다.", store1);
				Item item5 = createItem("김밥", 2400, 4550, "김밥 입니다.", store1);
				Item item6 = createItem("참치 김밥", 3500, 4900, "참치 김밥 입니다.", store1);

				Member member2 = createMember("닉네임2", 12345L);
				Long providerId = member2.getProviderId();

				cartService.addItem(providerId, item1.getId(), CartAdditionType.BY_CHECK);
				cartService.addItem(providerId, item2.getId(), CartAdditionType.BY_CHECK);
				cartService.addItem(providerId, item3.getId(), CartAdditionType.BY_CHECK);
				cartService.addItem(providerId, item4.getId(), CartAdditionType.BY_CHECK);
				cartService.addItem(providerId, item5.getId(), CartAdditionType.BY_CHECK);

				//when
				//then
				assertThrows(BusinessException.class,
					() -> cartService.addItem(providerId, item6.getId(), CartAdditionType.BY_CHECK)
				);
			}

			@DisplayName("상품이 존재하지 않는 경우")
			@Test
			void addItemFailureTest_itemNotFound() {
				//given
				Member member2 = createMember("닉네임2", 12345L);
				Long providerId = member2.getProviderId();

				//when
				//then
				assertThrows(EntityNotFoundException.class,
					() -> cartService.addItem(providerId, 111111L, CartAdditionType.BY_CHECK)
				);
			}

			@DisplayName("장바구니의 최소, 최대 수량 조건에 맞지 않는 경우")
			@Test
			void addItemFailureTest_invalidCartQuantity() {
				//given
				Address address1 = createAddress("동네분식 주소", 127.016539, 37.592709);
				Member member1 = createMember("닉네임", 123L);
				Store store1 = createStore("동네 분식", LocalTime.of(13, 0), LocalTime.of(20, 0), address1, member1);
				Item item1 = createItem("떡볶이", 3300, 4800, "기본 떡볶이 입니다.", store1);

				Member member2 = createMember("닉네임2", 12345L);
				Long providerId = member2.getProviderId();

				cartService.addItem(providerId, item1.getId(), CartAdditionType.BY_CHECK);
				cartService.addItem(providerId, item1.getId(), CartAdditionType.BY_CHECK);

				//when
				//then
				assertThrows(BusinessException.class,
					() -> {
						cartService.addItem(providerId, item1.getId(), CartAdditionType.BY_CHECK);
					}
				);
			}

			@DisplayName("업체가 임의로 상품을 삭제하여 더 이상 존재하지 않는 상품이 장바구니에 존재하는 경우")
			@Test
			void addItemFailureTest_itemRemovedNoLongerExistsItem() {
				//given
				Address address1 = createAddress("동네분식 주소", 127.016539, 37.592709);
				Member member1 = createMember("닉네임", 123L);
				Store store1 = createStore("동네 분식", LocalTime.of(13, 0), LocalTime.of(20, 0), address1, member1);
				Item item1 = createItem("떡볶이", 3300, 4800, "기본 떡볶이 입니다.", store1);

				Member member2 = createMember("닉네임2", 12345L);
				Long providerId = member2.getProviderId();

				cartService.addItem(providerId, item1.getId(), CartAdditionType.BY_CHECK);

				itemRepository.deleteById(item1.getId());

				//when
				//then
				assertThrows(EntityNotFoundException.class,
					() -> {
						cartService.addItem(providerId, item1.getId(), CartAdditionType.BY_CHECK);
					}
				);
			}

			@DisplayName("더 이상 존재하지 않는 업체의 상품이 장바구니에 존재하는 경우")
			@Test
			void addItemFailureTest_itemRemovedNoLongerExistsStore() {
				//given
				Address address1 = createAddress("동네분식 주소", 127.016539, 37.592709);
				Member member1 = createMember("닉네임", 123L);
				Store store1 = createStore("동네 분식", LocalTime.of(13, 0), LocalTime.of(20, 0), address1, member1);
				Item item1 = createItem("떡볶이", 3300, 4800, "기본 떡볶이 입니다.", store1);

				Member member2 = createMember("닉네임2", 12345L);
				Long providerId = member2.getProviderId();

				cartService.addItem(providerId, item1.getId(), CartAdditionType.BY_CHECK);

				itemRepository.deleteAllByStoreId(store1.getId());
				storeRepository.deleteById(store1.getId());

				//when
				//then
				assertThrows(EntityNotFoundException.class,
					() -> {
						cartService.addItem(providerId, item1.getId(), CartAdditionType.BY_CHECK);
					}
				);
			}
		}
	}

	@Nested
	@DisplayName("장바구니 조회")
	class findAllTest {

		@Nested
		@DisplayName("성공")
		class Success {

			@DisplayName("장바구니에 담겨있는 모든 상품 조회")
			@Test
			void findAllSuccessTest() {
				//given
				Address address1 = createAddress("동네분식 주소", 127.016539, 37.592709);
				Member member1 = createMember("닉네임", 123L);
				Store store1 = createStore("동네 분식", LocalTime.of(13, 0), LocalTime.of(20, 0), address1, member1);
				Item item1 = createItem("떡볶이", 3300, 4800, "기본 떡볶이 입니다.", store1);
				Item item2 = createItem("치즈 떡볶이", 3000, 3300, "치즈 떡볶이 입니다.", store1);

				Member member2 = createMember("닉네임2", 12345L);
				Long providerId = member2.getProviderId();

				cartService.addItem(providerId, item1.getId(), CartAdditionType.BY_CHECK);
				cartService.addItem(providerId, item2.getId(), CartAdditionType.BY_CHECK);

				//when
				CartsRes cartsRes = cartService.findAllByProviderId(providerId);

				//then
				assertThat(cartsRes.carts()).hasSize(2);
			}
		}

		@Nested
		@DisplayName("실패")
		class Failure {

			@DisplayName("업체가 임의로 상품을 삭제하여 더 이상 존재하지 않는 상품이 장바구니에 존재하는 경우")
			@Test
			void findAllFailureTest_itemRemovedNoLongerExistsItem() {
				//given
				Address address1 = createAddress("동네분식 주소", 127.016539, 37.592709);
				Member member1 = createMember("닉네임", 123L);
				Store store1 = createStore("동네 분식", LocalTime.of(13, 0), LocalTime.of(20, 0), address1, member1);
				Item item1 = createItem("떡볶이", 3300, 4800, "기본 떡볶이 입니다.", store1);

				Member member2 = createMember("닉네임2", 12345L);
				Long providerId = member2.getProviderId();

				cartService.addItem(providerId, item1.getId(), CartAdditionType.BY_CHECK);

				itemRepository.deleteById(item1.getId());

				//when
				//then
				assertThrows(EntityNotFoundException.class,
					() -> {
						cartService.findAllByProviderId(providerId);
					}
				);
			}

			@DisplayName("더 이상 존재하지 않는 업체의 상품이 장바구니에 존재하는 경우")
			@Test
			void findAllFailureTest_itemRemovedNoLongerExistsStore() {
				//given
				Address address1 = createAddress("동네분식 주소", 127.016539, 37.592709);
				Member member1 = createMember("닉네임", 123L);
				Store store1 = createStore("동네 분식", LocalTime.of(13, 0), LocalTime.of(20, 0), address1, member1);
				Item item1 = createItem("떡볶이", 3300, 4800, "기본 떡볶이 입니다.", store1);

				Member member2 = createMember("닉네임2", 12345L);
				Long providerId = member2.getProviderId();

				cartService.addItem(providerId, item1.getId(), CartAdditionType.BY_CHECK);

				itemRepository.deleteAllByStoreId(store1.getId());
				storeRepository.deleteById(store1.getId());

				//when
				//then
				assertThrows(EntityNotFoundException.class,
					() -> {
						cartService.findAllByProviderId(providerId);
					}
				);
			}
		}
	}

	@Nested
	@DisplayName("장바구니 수정")
	class updateTest {

		@Nested
		@DisplayName("성공")
		class Success {

			@DisplayName("장바구니 여러 상품 한 번에 수정")
			@Test
			void updateSuccessTest() {
				//given
				Address address1 = createAddress("동네분식 주소", 127.016539, 37.592709);
				Member member1 = createMember("닉네임", 123L);
				Store store1 = createStore("동네 분식", LocalTime.of(13, 0), LocalTime.of(20, 0), address1, member1);
				Item item1 = createItem("떡볶이", 3300, 4800, "기본 떡볶이 입니다.", store1);
				Item item2 = createItem("치즈 떡볶이", 3000, 3300, "치즈 떡볶이 입니다.", store1);

				Member member2 = createMember("닉네임2", 12345L);
				Long providerId = member2.getProviderId();

				cartService.addItem(providerId, item1.getId(), CartAdditionType.BY_CHECK);
				cartService.addItem(providerId, item2.getId(), CartAdditionType.BY_CHECK);
				cartService.addItem(providerId, item2.getId(), CartAdditionType.BY_CHECK);

				CartReq cartReq1 = new CartReq(item1.getId(), 2);
				CartReq cartReq2 = new CartReq(item2.getId(), 1);

				CartsReq cartsReq = new CartsReq(List.of(cartReq1, cartReq2));

				//when
				CartsRes cartsRes = cartService.update(providerId, cartsReq);

				//then
				assertThat(cartsRes.carts()).hasSize(2);
				assertThat(cartsRes.carts().get(0).quantity()).isEqualTo(2);
				assertThat(cartsRes.carts().get(1).quantity()).isEqualTo(1);
			}
		}

		@Nested
		@DisplayName("실패")
		class Failure {

			@DisplayName("장바구니의 최소, 최대 수량 조건에 맞지 않는 경우")
			@Test
			void updateFailureTest_invalidCartQuantity() {
				//given
				Address address1 = createAddress("동네분식 주소", 127.016539, 37.592709);
				Member member1 = createMember("닉네임", 123L);
				Store store1 = createStore("동네 분식", LocalTime.of(13, 0), LocalTime.of(20, 0), address1, member1);
				Item item1 = createItem("떡볶이", 3300, 4800, "기본 떡볶이 입니다.", store1);
				Item item2 = createItem("치즈 떡볶이", 3000, 3300, "치즈 떡볶이 입니다.", store1);

				Member member2 = createMember("닉네임2", 12345L);
				Long providerId = member2.getProviderId();

				cartService.addItem(providerId, item1.getId(), CartAdditionType.BY_CHECK);
				cartService.addItem(providerId, item2.getId(), CartAdditionType.BY_CHECK);

				CartReq cartReq1 = new CartReq(item1.getId(), 3);
				CartReq cartReq2 = new CartReq(item2.getId(), 0);

				CartsReq cartsReq = new CartsReq(List.of(cartReq1, cartReq2));

				//when
				//then
				assertThrows(BusinessException.class,
					() -> {
						cartService.update(providerId, cartsReq);
					}
				);
			}

			@DisplayName("장바구니에 상품이 존재하지 않는 경우")
			@Test
			void updateFailureTest_notFoundCartItem() {
				//given
				Address address1 = createAddress("동네분식 주소", 127.016539, 37.592709);
				Member member1 = createMember("닉네임", 123L);
				Store store1 = createStore("동네 분식", LocalTime.of(13, 0), LocalTime.of(20, 0), address1, member1);
				Item item1 = createItem("떡볶이", 3300, 4800, "기본 떡볶이 입니다.", store1);
				Item item2 = createItem("치즈 떡볶이", 3000, 3300, "치즈 떡볶이 입니다.", store1);

				Member member2 = createMember("닉네임2", 12345L);
				Long providerId = member2.getProviderId();

				CartReq cartReq1 = new CartReq(item1.getId(), 1);
				CartReq cartReq2 = new CartReq(item2.getId(), 2);

				CartsReq cartsReq = new CartsReq(List.of(cartReq1, cartReq2));

				//when
				//then
				assertThrows(EntityNotFoundException.class,
					() -> {
						cartService.update(providerId, cartsReq);
					}
				);
			}

			@DisplayName("업체가 임의로 상품을 삭제하여 더 이상 존재하지 않는 상품이 장바구니에 존재하는 경우")
			@Test
			void updateFailureTest_itemRemovedNoLongerExistsItem() {
				//given
				Address address1 = createAddress("동네분식 주소", 127.016539, 37.592709);
				Member member1 = createMember("닉네임", 123L);
				Store store1 = createStore("동네 분식", LocalTime.of(13, 0), LocalTime.of(20, 0), address1, member1);
				Item item1 = createItem("떡볶이", 3300, 4800, "기본 떡볶이 입니다.", store1);
				Item item2 = createItem("치즈 떡볶이", 3000, 3300, "치즈 떡볶이 입니다.", store1);

				Member member2 = createMember("닉네임2", 12345L);
				Long providerId = member2.getProviderId();

				cartService.addItem(providerId, item1.getId(), CartAdditionType.BY_CHECK);
				cartService.addItem(providerId, item2.getId(), CartAdditionType.BY_CHECK);

				CartReq cartReq1 = new CartReq(item1.getId(), 2);
				CartReq cartReq2 = new CartReq(item2.getId(), 2);

				CartsReq cartsReq = new CartsReq(List.of(cartReq1, cartReq2));

				itemRepository.deleteById(item1.getId());

				//when
				//then
				assertThrows(EntityNotFoundException.class,
					() -> {
						cartService.update(providerId, cartsReq);
					}
				);
			}

			@DisplayName("더 이상 존재하지 않는 업체의 상품이 장바구니에 존재하는 경우")
			@Test
			void updateFailureTest_itemRemovedNoLongerExistsStore() {
				//given
				Address address1 = createAddress("동네분식 주소", 127.016539, 37.592709);
				Member member1 = createMember("닉네임", 123L);
				Store store1 = createStore("동네 분식", LocalTime.of(13, 0), LocalTime.of(20, 0), address1, member1);
				Item item1 = createItem("떡볶이", 3300, 4800, "기본 떡볶이 입니다.", store1);
				Item item2 = createItem("치즈 떡볶이", 3000, 3300, "치즈 떡볶이 입니다.", store1);

				Member member2 = createMember("닉네임2", 12345L);
				Long providerId = member2.getProviderId();

				cartService.addItem(providerId, item1.getId(), CartAdditionType.BY_CHECK);
				cartService.addItem(providerId, item2.getId(), CartAdditionType.BY_CHECK);

				CartReq cartReq1 = new CartReq(item1.getId(), 2);
				CartReq cartReq2 = new CartReq(item2.getId(), 2);

				CartsReq cartsReq = new CartsReq(List.of(cartReq1, cartReq2));

				itemRepository.deleteAllByStoreId(store1.getId());
				storeRepository.deleteById(store1.getId());

				//when
				//then
				assertThrows(EntityNotFoundException.class,
					() -> {
						cartService.update(providerId, cartsReq);
					}
				);
			}
		}
	}

	@Nested
	@DisplayName("장바구니 삭제")
	class deleteTest {

		@Nested
		@DisplayName("성공")
		class Success {

			@DisplayName("요소 삭제")
			@Test
			void deleteOneSuccessTest() {
				//given
				Address address1 = createAddress("동네분식 주소", 127.016539, 37.592709);
				Member member1 = createMember("닉네임", 123L);
				Store store1 = createStore("동네 분식", LocalTime.of(13, 0), LocalTime.of(20, 0), address1, member1);
				Item item1 = createItem("떡볶이", 3300, 4800, "기본 떡볶이 입니다.", store1);

				Member member2 = createMember("닉네임2", 12345L);
				Long providerId = member2.getProviderId();

				cartService.addItem(providerId, item1.getId(), CartAdditionType.BY_CHECK);

				//when
				cartService.deleteOne(providerId, item1.getId());

				//then
				CartsRes result = cartService.findAllByProviderId(providerId);
				assertThat(result.carts()).hasSize(0);
			}

			@DisplayName("초기화")
			@Test
			void deleteAllSuccessTest() {
				//given
				Address address1 = createAddress("동네분식 주소", 127.016539, 37.592709);
				Member member1 = createMember("닉네임", 123L);
				Store store1 = createStore("동네 분식", LocalTime.of(13, 0), LocalTime.of(20, 0), address1, member1);
				Item item1 = createItem("떡볶이", 3300, 4800, "기본 떡볶이 입니다.", store1);
				Item item2 = createItem("치즈 떡볶이", 3000, 3300, "치즈 떡볶이 입니다.", store1);

				Member member2 = createMember("닉네임2", 12345L);
				Long providerId = member2.getProviderId();

				cartService.addItem(providerId, item1.getId(), CartAdditionType.BY_CHECK);
				cartService.addItem(providerId, item2.getId(), CartAdditionType.BY_CHECK);

				//when
				cartService.deleteAll(providerId);

				//then
				CartsRes result = cartService.findAllByProviderId(providerId);
				assertThat(result.carts()).hasSize(0);
			}
		}

		@Nested
		@DisplayName("실패")
		class Failure {

			@DisplayName("요소 삭제 실패 - 장바구니에 상품이 존재하지 않는 경우")
			@Test
			void deleteOneFailureTest_notFoundCartItem() {
				//given
				Address address1 = createAddress("동네분식 주소", 127.016539, 37.592709);
				Member member1 = createMember("닉네임", 123L);
				Store store1 = createStore("동네 분식", LocalTime.of(13, 0), LocalTime.of(20, 0), address1, member1);
				Item item1 = createItem("떡볶이", 3300, 4800, "기본 떡볶이 입니다.", store1);

				Member member2 = createMember("닉네임2", 12345L);
				Long providerId = member2.getProviderId();

				//when
				//then
				assertThrows(EntityNotFoundException.class,
					() -> {
						cartService.deleteOne(providerId, item1.getId());
					}
				);
			}
		}
	}

	private Member createMember(String nickname, Long providerId) {

		Member member = Member.builder()
			.nickName(nickname)
			.realName("이름")
			.phoneNumber("010010101")
			.provider("kakao")
			.providerId(providerId)
			.build();

		return memberRepository.save(member);
	}

	private Address createAddress(String AddressName, double xCoordinate, double yCoordinate) {

		Address address = Address.builder()
			.name(AddressName)
			.xCoordinate(xCoordinate)
			.yCoordinate(yCoordinate)
			.build();

		return addressRepository.save(address);
	}

	private Store createStore(String storeName, LocalTime openTime, LocalTime closeTime, Address address, Member member) {
		String storeDefaultImageUrl = "https://fake-image.com/store.png";

		Store store = Store.builder()
			.name(storeName)
			.storeNumber("0000000")
			.telephone("00000000")
			.openTime(openTime)
			.closeTime(closeTime)
			.dayOff(Collections.singleton(DayOff.MON))
			.address(address)
			.member(member)
			.build();

		store.updateImage(storeDefaultImageUrl);
		store.updateStatus(StoreStatus.OPENED);

		return storeRepository.save(store);
	}

	private Item createItem(String name, int discountPrice, int originalPrice, String description, Store store) {

		Item item = Item.builder()
			.name(name)
			.stock(2)
			.discountPrice(discountPrice)
			.originalPrice(originalPrice)
			.description(description)
			.image("https://fake-image.com/item.png")
			.store(store)
			.build();

		return itemRepository.save(item);
	}
}
