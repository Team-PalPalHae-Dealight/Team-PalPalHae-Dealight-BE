package com.palpal.dealightbe.domain.store.application.dto.response;

import com.palpal.dealightbe.domain.store.domain.Store;

public record StoreByMemberRes(
	Long storeId
) {

	public static StoreByMemberRes from(Store store) {
		return new StoreByMemberRes(store.getId());
	}
}
