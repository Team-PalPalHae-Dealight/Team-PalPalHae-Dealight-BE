package com.palpal.dealightbe.domain.store.application.dto.response;

import com.palpal.dealightbe.domain.store.domain.Store;
import com.palpal.dealightbe.domain.store.domain.StoreStatus;

public record StoreStatusRes(
	Long storeId,
	StoreStatus storeStatus
) {

	public static StoreStatusRes from(Store store) {
		return new StoreStatusRes(store.getId(), store.getStoreStatus());
	}
}
