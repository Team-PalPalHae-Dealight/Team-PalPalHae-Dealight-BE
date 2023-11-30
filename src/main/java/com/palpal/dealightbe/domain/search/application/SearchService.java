package com.palpal.dealightbe.domain.search.application;

import static com.palpal.dealightbe.domain.item.domain.ItemDocument.convertToItemDocuments;
import static com.palpal.dealightbe.domain.store.domain.StoreDocument.convertToStoreDocuments;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.palpal.dealightbe.domain.item.domain.UpdatedItem;
import com.palpal.dealightbe.domain.item.domain.UpdatedItemRepository;
import com.palpal.dealightbe.domain.item.infrastructure.ItemSearchRepositoryImpl;
import com.palpal.dealightbe.domain.store.application.dto.response.StoresInfoSliceRes;
import com.palpal.dealightbe.domain.store.domain.DocumentStatus;
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
	public StoresInfoSliceRes searchToES(double xCoordinate, double yCoordinate, String keyword, Pageable pageable) {
		Slice<StoreDocument> storeDocuments = storeSearchRepositoryImpl.searchStores(xCoordinate, yCoordinate, keyword, pageable);

		return StoresInfoSliceRes.fromDocuments(storeDocuments);
	}

	public void uploadToES() {
		List<UpdatedStore> updatedStoreList = updatedStoreRepository.findAll();
		validateAndBulkStores(updatedStoreList, storeSearchRepositoryImpl);

		List<UpdatedItem> updatedItemList = updatedItemRepository.findAll();
		validateAndBulkItems(updatedItemList, itemSearchRepositoryImpl);

		markDocumentsAsDone(updatedItemList, updatedStoreList);
	}

	public void updateStatusToES() {
		List<UpdatedStore> updatedStoreList = updatedStoreRepository.findAll();
		updateStoreDocuments(updatedStoreList, storeSearchRepositoryImpl);
	}

	private void validateAndBulkStores(List<UpdatedStore> updatedStoreList, StoreSearchRepositoryImpl storeSearchRepository) {
		if (updatedStoreList.isEmpty()) {
			throw new BusinessException(ErrorCode.UPDATABLE_STORE_NOT_EXIST);
		}

		updatedStoreList.forEach(updatedStore -> {
			if (updatedStore.getDocumentStatus() != DocumentStatus.DONE) {
				storeSearchRepository.bulkInsertOrUpdate(convertToStoreDocuments(updatedStoreList));
			}
		});
	}

	private void validateAndBulkItems(List<UpdatedItem> updatedItemList, ItemSearchRepositoryImpl itemSearchRepository) {
		updatedItemList.forEach(updatedItem -> {
			if (updatedItem.getDocumentStatus() != DocumentStatus.DONE) {
				itemSearchRepository.bulkInsertOrUpdate(convertToItemDocuments(updatedItemList));
			}
		});
	}

	private void markDocumentsAsDone(List<UpdatedItem> updatedItemList, List<UpdatedStore> updatedStoreList) {
		updatedItemList.forEach(UpdatedItem::markAsDone);
		updatedStoreList.forEach(UpdatedStore::markAsDone);
	}

	private void updateStoreDocuments(List<UpdatedStore> updatedStoreList, StoreSearchRepositoryImpl storeSearchRepositoryImpl) {
		updatedStoreList.forEach(updatedStore -> {
			if (updatedStore.getDocumentStatus() == DocumentStatus.DONE) {
				StoreDocument existingStoreDocument = storeSearchRepositoryImpl.findById(String.valueOf(updatedStore.getId()));
				if (existingStoreDocument != null) {
					updateStoreStatusIfNeeded(updatedStore, existingStoreDocument, storeSearchRepositoryImpl);
					updateStoreItemsIfNeeded(updatedStore, storeSearchRepositoryImpl);
				}
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
