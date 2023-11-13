package com.palpal.dealightbe.domain.item.application;

import static com.palpal.dealightbe.global.SearchSortType.findSortType;
import static com.palpal.dealightbe.global.error.ErrorCode.DUPLICATED_ITEM_NAME;
import static com.palpal.dealightbe.global.error.ErrorCode.NOT_FOUND_ITEM;
import static com.palpal.dealightbe.global.error.ErrorCode.NOT_FOUND_STORE;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.palpal.dealightbe.domain.image.ImageService;
import com.palpal.dealightbe.domain.image.application.dto.request.ImageUploadReq;
import com.palpal.dealightbe.domain.item.application.dto.request.ItemReq;
import com.palpal.dealightbe.domain.item.application.dto.response.ItemRes;
import com.palpal.dealightbe.domain.item.application.dto.response.ItemsRes;
import com.palpal.dealightbe.domain.item.domain.Item;
import com.palpal.dealightbe.domain.item.domain.ItemRepository;
import com.palpal.dealightbe.global.SearchSortType;
import com.palpal.dealightbe.domain.store.domain.Store;
import com.palpal.dealightbe.domain.store.domain.StoreRepository;
import com.palpal.dealightbe.global.error.exception.BusinessException;
import com.palpal.dealightbe.global.error.exception.EntityNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ItemService {

	public static final String DEFAULT_ITEM_IMAGE_PATH = "https://team-08-bucket.s3.ap-northeast-2.amazonaws.com/image/default-item-image.png";

	private final ItemRepository itemRepository;
	private final StoreRepository storeRepository;
	private final ImageService imageService;

	public ItemRes create(ItemReq itemReq, Long memberId, ImageUploadReq imageUploadReq) {
		Store store = getStore(memberId);

		checkDuplicatedItemName(itemReq.name(), store.getId());

		String imageUrl = saveImage(imageUploadReq);

		Item item = ItemReq.toItem(itemReq, store, imageUrl);
		Item savedItem = itemRepository.save(item);

		return ItemRes.from(savedItem);
	}

	@Transactional(readOnly = true)
	public ItemRes findById(Long itemId) {
		Item item = getItem(itemId);

		return ItemRes.from(item);
	}

	@Transactional(readOnly = true)
	public ItemsRes findAllForStore(Long memberId, Pageable pageable) {
		Store store = getStore(memberId);

		Page<Item> items = itemRepository.findAllByStoreIdOrderByUpdatedAtDesc(store.getId(), pageable);

		return ItemsRes.from(items);
	}

	@Transactional(readOnly = true)
	public ItemsRes findAllForMember(double xCoordinate, double yCoordinate, String sortBy, Pageable pageable) {
		Page<Item> items = Page.empty();

		SearchSortType sortType = findSortType(sortBy);

		switch (sortType) {
			case DEADLINE -> items = itemRepository.findAllByDeadline(xCoordinate, yCoordinate, pageable);
			case DISCOUNT_RATE -> items = itemRepository.findAllByDiscountRate(xCoordinate, yCoordinate, pageable);
			case DISTANCE -> items = itemRepository.findAllByDistance(xCoordinate, yCoordinate, pageable);
		}

		return ItemsRes.from(items);
	}

	public ItemRes update(Long itemId, ItemReq itemReq, Long memberId, ImageUploadReq imageUploadReq) {
		Store store = getStore(memberId);
		Item item = getItem(itemId);

		String image = item.getImage();
		imageService.delete(image);

		String imageUrl = saveImage(imageUploadReq);

		Item updatedItem = ItemReq.toItem(itemReq, store, imageUrl);
		item.update(updatedItem);

		return ItemRes.from(item);
	}

	public void delete(Long itemId, Long memberId) {
		Store store = getStore(memberId);
		Item item = getItem(itemId);

		item.checkItemInStore(store);

		itemRepository.delete(item);
	}

	public String saveImage(ImageUploadReq imageUploadReq) {
		if (!imageUploadReq.file().isEmpty()) {
			return imageService.store(imageUploadReq.file());
		}

		return DEFAULT_ITEM_IMAGE_PATH;
	}

	private void checkDuplicatedItemName(String itemName, Long storeId) {
		if (itemRepository.existsByNameAndStoreId(itemName, storeId)) {
			log.warn("DUPLICATED_ITEM_NAME : {}", itemName);
			throw new BusinessException(DUPLICATED_ITEM_NAME);
		}
	}

	private Store getStore(Long memberId) {
		return storeRepository.findByMemberId(memberId)
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_STORE_BY_MEMBER_ID : {}", memberId);
				return new EntityNotFoundException(NOT_FOUND_STORE);
			});
	}

	private Item getItem(Long itemId) {
		return itemRepository.findById(itemId)
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_ITEM_BY_ID : {}", itemId);
				return new EntityNotFoundException(NOT_FOUND_ITEM);
			});
	}
}
