package com.palpal.dealightbe.domain.item.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.palpal.dealightbe.domain.item.application.dto.request.ItemReq;
import com.palpal.dealightbe.domain.item.application.dto.response.ItemRes;
import com.palpal.dealightbe.domain.item.domain.Item;
import com.palpal.dealightbe.domain.item.domain.ItemRepository;
import com.palpal.dealightbe.domain.store.domain.Store;
import com.palpal.dealightbe.domain.store.domain.StoreRepository;
import com.palpal.dealightbe.global.error.ErrorCode;
import com.palpal.dealightbe.global.error.exception.BusinessException;
import com.palpal.dealightbe.global.error.exception.EntityNotFoundException;

import static com.palpal.dealightbe.global.error.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ItemService {

	private final ItemRepository itemRepository;
	private final StoreRepository storeRepository;

	public ItemRes create(ItemReq itemReq, Long memberId) {
		Store store = storeRepository.findByMemberId(memberId)
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_STORE_BY_MEMBER_ID : {}", memberId);
				return new EntityNotFoundException(NOT_FOUND_STORE);
			});

		checkDuplicatedItemName(itemReq.name(), store.getId());

		Item item = ItemReq.toItem(itemReq, store);
		Item savedItem = itemRepository.save(item);

		return ItemRes.from(savedItem);
	}

	@Transactional(readOnly = true)
	public ItemRes findById(Long itemId) {
		Item item = itemRepository.findById(itemId)
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_ITEM_BY_ID : {}", itemId);
				return new EntityNotFoundException(NOT_FOUND_ITEM);
			});

		return ItemRes.from(item);
	}

	public ItemRes update(Long itemId, ItemReq itemReq, Long memberId) {
		Store store = storeRepository.findByMemberId(memberId)
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_STORE_BY_MEMBER_ID : {}", memberId);
				return new EntityNotFoundException(NOT_FOUND_STORE);
			});

		checkDuplicatedItemName(itemReq.name(), store.getId());

		Item item = itemRepository.findById(itemId)
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_ITEM_BY_ID : {}", itemId);
				return new EntityNotFoundException(NOT_FOUND_ITEM);
			});

		Item updatedItem = ItemReq.toItem(itemReq, store);
		item.update(updatedItem);

		return ItemRes.from(item);
	}

	public void delete(Long itemId, Long memberId) {
		Store store = storeRepository.findByMemberId(memberId)
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_STORE_BY_MEMBER_ID : {}", memberId);
				return new EntityNotFoundException(NOT_FOUND_STORE);
			});

		Item item = itemRepository.findById(itemId)
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_ITEM_BY_ID : {}", itemId);
				return new EntityNotFoundException(NOT_FOUND_ITEM);
			});

		item.checkItemInStore(store);

		itemRepository.delete(item);
	}

	private void checkDuplicatedItemName(String itemName, Long storeId) {
		if (itemRepository.existsByNameAndStoreId(itemName, storeId)) {
			log.warn("DUPLICATED_ITEM_NAME : {}", itemName);
			throw new BusinessException(DUPLICATED_ITEM_NAME);
		}
	}
}
