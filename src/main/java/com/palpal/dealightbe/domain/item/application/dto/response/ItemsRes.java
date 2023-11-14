package com.palpal.dealightbe.domain.item.application.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;

import com.palpal.dealightbe.domain.item.domain.Item;

public record ItemsRes(
	List<ItemRes> items,
	boolean hasNext
) {

	public static ItemsRes from(Slice<Item> items) {

		List<ItemRes> itemResList = items.stream()
			.map(ItemRes::from)
			.toList();

		return new ItemsRes(itemResList, items.hasNext());
	}
}
