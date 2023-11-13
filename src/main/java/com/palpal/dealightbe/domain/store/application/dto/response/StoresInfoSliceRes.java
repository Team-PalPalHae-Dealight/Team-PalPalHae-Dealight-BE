package com.palpal.dealightbe.domain.store.application.dto.response;

import java.util.List;

import org.springframework.data.domain.Slice;

import com.palpal.dealightbe.domain.store.domain.Store;

public record StoresInfoSliceRes(
	List<StoreInfoSliceRes> storeInfoSliceRes,
	boolean hasNext
) {

	public static StoresInfoSliceRes from(Slice<Store> storeSlice) {
		List<StoreInfoSliceRes> storesInfoSliceRes = storeSlice.stream()
			.map(StoreInfoSliceRes::from)
			.toList();

		return new StoresInfoSliceRes(storesInfoSliceRes, storeSlice.hasNext());
	}
}
