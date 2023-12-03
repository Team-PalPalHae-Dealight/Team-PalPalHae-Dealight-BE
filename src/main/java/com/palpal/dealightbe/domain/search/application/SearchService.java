package com.palpal.dealightbe.domain.search.application;

import static com.palpal.dealightbe.domain.item.domain.ItemDocument.convertToItemDocuments;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.palpal.dealightbe.domain.item.domain.ItemDocument;
import com.palpal.dealightbe.domain.item.domain.UpdatedItem;
import com.palpal.dealightbe.domain.item.domain.UpdatedItemRepository;
import com.palpal.dealightbe.domain.item.infrastructure.ItemSearchRepositoryImpl;
import com.palpal.dealightbe.domain.store.application.dto.response.StoresInfoSliceRes;
import com.palpal.dealightbe.domain.store.domain.StoreDocument;
import com.palpal.dealightbe.domain.store.domain.StoreStatus;
import com.palpal.dealightbe.domain.store.domain.UpdatedStore;
import com.palpal.dealightbe.domain.store.domain.UpdatedStoreRepository;
import com.palpal.dealightbe.domain.store.infrastructure.StoreSearchRepositoryImpl;
import com.palpal.dealightbe.global.error.ErrorCode;
import com.palpal.dealightbe.global.error.exception.BusinessException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SearchService {

	private final UpdatedStoreRepository updatedStoreRepository;
	private final StoreSearchRepositoryImpl storeSearchRepositoryImpl;
	private final UpdatedItemRepository updatedItemRepository;
	private final ItemSearchRepositoryImpl itemSearchRepositoryImpl;

	@Transactional(readOnly = true)
	public StoresInfoSliceRes searchToES(double xCoordinate, double yCoordinate, String keyword, String sortBy, Pageable pageable) {
		Slice<StoreDocument> storeDocuments = storeSearchRepositoryImpl.searchStores(xCoordinate, yCoordinate, keyword, sortBy, pageable);

		return StoresInfoSliceRes.fromDocuments(storeDocuments);
	}

	public void uploadToES() {
		List<UpdatedStore> updatedStoreList = updatedStoreRepository.findAllByDocumentStatusIsReady();
		validateAndBulkStores(updatedStoreList, storeSearchRepositoryImpl);

		List<UpdatedItem> updatedItemList = updatedItemRepository.findAllByDocumentStatusIsReady();
		validateAndBulkItems(updatedItemList, itemSearchRepositoryImpl);

		markDocumentsAsDone(updatedItemList, updatedStoreList);
	}

	public void updateStatusToES() {
		List<UpdatedStore> updatedStoreList = updatedStoreRepository.findAllByDocumentStatusIsDone();
		updateStoreDocuments(updatedStoreList, storeSearchRepositoryImpl);
	}

	private void validateAndBulkStores(List<UpdatedStore> updatedStoreList, StoreSearchRepositoryImpl storeSearchRepository) {
		if (updatedStoreList.isEmpty()) {
			throw new BusinessException(ErrorCode.UPDATABLE_STORE_NOT_EXIST);
		}

		List<StoreDocument> storeDocumentsToBulkInsertOrUpdate = updatedStoreList.stream()
			.map(StoreDocument::from)
			.collect(Collectors.toList());

		if (!storeDocumentsToBulkInsertOrUpdate.isEmpty()) {
			storeSearchRepository.bulkInsertOrUpdate(storeDocumentsToBulkInsertOrUpdate);
		}
	}

	private void validateAndBulkItems(List<UpdatedItem> updatedItemList, ItemSearchRepositoryImpl itemSearchRepository) {
		List<ItemDocument> itemDocumentsToBulkInsertOrUpdate = updatedItemList.stream()
			.map(ItemDocument::from)
			.collect(Collectors.toList());

		if (!itemDocumentsToBulkInsertOrUpdate.isEmpty()) {
			itemSearchRepository.bulkInsertOrUpdate(itemDocumentsToBulkInsertOrUpdate);
		}
	}

	private void markDocumentsAsDone(List<UpdatedItem> updatedItemList, List<UpdatedStore> updatedStoreList) {
		updatedItemList.forEach(UpdatedItem::markAsDone);

		updatedStoreList.forEach(UpdatedStore::markAsDone);
	}

	private void updateStoreDocuments(List<UpdatedStore> updatedStoreList, StoreSearchRepositoryImpl storeSearchRepositoryImpl) {
		updatedStoreList.forEach(updatedStore -> {
			StoreDocument existingStoreDocument = storeSearchRepositoryImpl.findById(String.valueOf(updatedStore.getId()));
			if (existingStoreDocument != null) {
				updateStoreStatusIfNeeded(updatedStore, existingStoreDocument, storeSearchRepositoryImpl);
				updateStoreItemsIfNeeded(updatedStore, storeSearchRepositoryImpl);
			}
		});
	}

	private void updateStoreStatusIfNeeded(UpdatedStore updatedStore, StoreDocument existingStoreDocument, StoreSearchRepositoryImpl storeSearchRepository) {
		StoreStatus currentStatus = updatedStore.getStoreStatus();
		StoreStatus existingStatus = existingStoreDocument.getStoreStatus();

		if (currentStatus != existingStatus) {
			storeSearchRepository.updateStoreStatus(String.valueOf(updatedStore.getId()), currentStatus);
		}
	}

	private void updateStoreItemsIfNeeded(UpdatedStore updatedStore, StoreSearchRepositoryImpl storeSearchRepository) {
		if (updatedStore.getItems() != null && !updatedStore.getItems().isEmpty()) {
			storeSearchRepository.updateStoreItems(String.valueOf(updatedStore.getId()), convertToItemDocuments(updatedStore.getItems()));
		}
	}

}
