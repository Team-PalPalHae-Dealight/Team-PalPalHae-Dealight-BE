package com.palpal.dealightbe.domain.item.application.dto.request;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

import org.hibernate.validator.constraints.Length;

import com.palpal.dealightbe.domain.item.domain.Item;
import com.palpal.dealightbe.domain.store.domain.Store;

public record ItemReq(
	@NotBlank(message = "상품 이름을 입력해주세요.")
	@Length(min = 1, max = 50, message = "상품 이름은 1자 이상 50자 이하로 등록 가능합니다.")
	String name,

	@Min(value = 1, message = "상품 재고 수량은 1개 이상부터 등록 가능합니다.")
	int stock,

	@Min(value = 1, message = "상품 할인가는 1원 이상부터 등록 가능합니다.")
	int discountPrice,

	@Min(value = 1, message = "상품 원가는 1원 이상부터 등록 가능합니다.")
	int originalPrice,

	@Length(max = 300, message = "상품 설명은 300자 이하로 등록 가능합니다.")
	String description,

	@Length(max = 300, message = "상품 안내 사항은 300자 이하로 등록 가능합니다.")
	String information
) {

	public static Item toItem(ItemReq itemReq, Store store, String image) {

		return Item.builder()
			.name(itemReq.name)
			.stock(itemReq.stock)
			.discountPrice(itemReq.discountPrice)
			.originalPrice(itemReq.originalPrice)
			.description(itemReq.description)
			.information(itemReq.information)
			.image(image)
			.store(store)
			.build();
	}
}
