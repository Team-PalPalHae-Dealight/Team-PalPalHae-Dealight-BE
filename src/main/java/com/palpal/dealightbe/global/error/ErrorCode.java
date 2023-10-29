package com.palpal.dealightbe.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	//서버
	INTERNAL_SERVER_ERROR("S001", "예기치 못한 오류가 발생했습니다."),

	//공용
	INVALID_INPUT_VALUE("C001", "잘못된 값을 입력하셨습니다."),

	//상품
	INVALID_ITEM_DISCOUNT_PRICE("I001", "상품 할인가는 원가보다 클 수 없습니다."),
	NOT_FOUND_ITEM("I002", "상품이 존재하지 않습니다."),
	ALREADY_REGISTERED_ITEM_NAME("I003", "동일한 이름을 가진 상품이 이미 등록되어 있습니다."),

	//업체
	NOT_FOUND_STORE("ST002", "업체가 존재하지 않습니다.");

	private final String code;
	private final String message;
}
