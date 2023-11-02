package com.palpal.dealightbe.domain.item.application.dto.response;

import java.util.List;

import com.palpal.dealightbe.domain.item.domain.Item;

public record ItemsRes(
	List<ItemRes> itemResponses
) {

	public static ItemsRes from(List<Item> items) {

		return new ItemsRes(items.stream()
			.map(ItemRes::from)
			.toList());
	}
}
