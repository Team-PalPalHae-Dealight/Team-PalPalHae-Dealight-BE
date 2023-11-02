package com.palpal.dealightbe.domain.store.application.dto.response;

import com.palpal.dealightbe.domain.store.domain.Store;
import com.palpal.dealightbe.domain.store.domain.StoreStatus;

public record StoreStatusUpdateRes(
	Long storeId,
	StoreStatus storeStatus
) {

	public static StoreStatusUpdateRes from(Store store) {
		return new StoreStatusUpdateRes(store.getId(), store.getStoreStatus());
	}
}
