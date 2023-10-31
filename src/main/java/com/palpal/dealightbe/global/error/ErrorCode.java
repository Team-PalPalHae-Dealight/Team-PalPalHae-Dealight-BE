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

	//멤버
	NOT_FOUND_MEMBER("M001", "고객을 찾을 수 없습니다."),

	//업체
	INVALID_BUSINESS_TIME("ST001", "마감 시간은 오픈 시간보다 이전일 수 없습니다"),
	NOT_FOUND_DAY_OFF("ST002", "존재하지 않는 휴무일 입니다."),
	NOT_FOUND_STORE("ST003", "업체가 존재하지 않습니다."),
	NOT_MATCH_OWNER_AND_REQUESTER("ST004", "업체 소유자와 요청자가 일치하지 않습니다."),
  
	//상품
	INVALID_ITEM_DISCOUNT_PRICE("I001", "상품 할인가는 원가보다 클 수 없습니다."),
	NOT_FOUND_ITEM("I002", "상품이 존재하지 않습니다."),
	ALREADY_REGISTERED_ITEM_NAME("I003", "동일한 이름을 가진 상품이 이미 등록되어 있습니다."),

	//파일
	NOT_FOUND_IMAGE("F001", "존재하지 않는 이미지 입니다."),
	EMPTY_IMAGE("F002", "이미지를 업로드 바랍니다."),
	INVALID_IMAGE_FORMAT("F003", "지원하지 않는 이미지 파일 형식입니다.");

	private final String code;
	private final String message;
}
