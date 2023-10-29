package com.palpal.dealightbe.domain.item.application;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.palpal.dealightbe.domain.item.application.dto.request.ItemReq;
import com.palpal.dealightbe.domain.item.application.dto.response.ItemRes;
import com.palpal.dealightbe.domain.item.domain.Item;
import com.palpal.dealightbe.domain.item.domain.ItemRepository;
import com.palpal.dealightbe.domain.store.domain.Store;
import com.palpal.dealightbe.domain.store.domain.StoreRepository;
import com.palpal.dealightbe.global.error.exception.BusinessException;
import com.palpal.dealightbe.global.error.exception.EntityNotFoundException;

import static com.palpal.dealightbe.global.error.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemService {

	private final ItemRepository itemRepository;
	private final StoreRepository storeRepository;

	public ItemRes create(ItemReq itemReq, Long memberId) {
		Store store = storeRepository.findByMemberId(memberId)
			.orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_STORE));

		checkAlreadyRegisteredItemName(itemReq.name(), store.getId());

		Item item = ItemReq.toItem(itemReq, store);

		itemRepository.save(item);

		return ItemRes.from(item);
	}

	private void checkAlreadyRegisteredItemName(String itemName, Long storeId) {
		if (itemRepository.existsByNameAndStoreId(itemName, storeId)) {
			throw new BusinessException(ALREADY_REGISTERED_ITEM_NAME);
		}
	}
}
