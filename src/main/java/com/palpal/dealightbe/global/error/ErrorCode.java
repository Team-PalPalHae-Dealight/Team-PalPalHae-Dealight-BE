package com.palpal.dealightbe.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	//서버
	INTERNAL_SERVER_ERROR("S001", "예기치 못한 오류가 발생했습니다."),
	UNABLE_TO_HANDLE_ERROR("S002", "처리할 수 없는 데이터입니다."),

	//공용
	INVALID_INPUT_VALUE("C001", "잘못된 값을 입력하셨습니다."),
	UNAUTHORIZED_REQUEST("C002", "해당 요청을 수행할 권한이 없습니다."),

	//멤버
	NOT_FOUND_MEMBER("M001", "고객을 찾을 수 없습니다."),

	//업체
	INVALID_BUSINESS_TIME("ST001", "마감 시간은 오픈 시간보다 이전일 수 없습니다"),
	NOT_FOUND_DAY_OFF("ST002", "존재하지 않는 휴무일 입니다."),
	NOT_FOUND_STORE("ST003", "업체가 존재하지 않습니다."),
	NOT_MATCH_OWNER_AND_REQUESTER("ST004", "업체 소유자와 요청자가 일치하지 않습니다."),
	NOT_FOUND_STATUS("ST005", "존재하지 않는 영업 상태 입니다."),

	//상품
	INVALID_ITEM_DISCOUNT_PRICE("I001", "상품 할인가는 원가보다 클 수 없습니다."),
	NOT_FOUND_ITEM("I002", "상품이 존재하지 않습니다."),
	DUPLICATED_ITEM_NAME("I003", "동일한 이름을 가진 상품이 이미 등록되어 있습니다."),
	INVALID_ITEM_QUANTITY("I004", "상품 재고가 부족합니다"),
	STORE_HAS_NO_ITEM("I005", "요청하신 상품은 해당 업체에 등록되지 않은 상품입니다."),

	//파일
	NOT_FOUND_IMAGE("F001", "존재하지 않는 이미지 입니다."),
	EMPTY_IMAGE("F002", "이미지를 업로드 바랍니다."),
	INVALID_IMAGE_FORMAT("F003", "지원하지 않는 이미지 파일 형식입니다."),

	//주문
	NOT_FOUND_ORDER("OR001", "존재하지 않는 주문입니다."),
	INVALID_ARRIVAL_TIME("OR002", "예상 도착 시간은 업체 마감 시간 이전이어야 합니다."),
	INVALID_DEMAND_LENGTH("OR003", "요청 사항은 최대 100자까지 입력할 수 있습니다."),
	INVALID_ORDER_STATUS("OR004", "유효하지 않은 주문 상태입니다."),
	INVALID_ORDER_STATUS_UPDATER("OR005", "주문을 받은 업체만 주문 상태를 변경할 수 있습니다."),
	EXCEEDED_ORDER_ITEMS("OR006", "한 번에 5개 종류의 상품까지만 주문할 수 있습니다."),
	UNCHANGEABLE_ORDER_STATUS("OR007", "주문 완료 또는 주문 취소 상태에서는 상태 변경이 불가능합니다."),
	INVALID_ORDER_FILTER("OR008", "유효하지 않은 주문 조회 필터링 조건입니다."),

	//인증, 인가
	ACCESS_DENIED("AUTH001", "사용자 인증에 실패했습니다."),
	ALREADY_EXIST_MEMBER("AUTH002", "이미 가입된 사용자입니다."),
	INVALID_ROLE_TYPE("AUTH003", "유효하지 않은 권한이 입력됐습니다."),
	NOT_FOUND_ROLE("AUTH004", "존재하지 않는 역할입니다."),
	INVALID_ROLE_UPDATE("AUTH005", "적어도 하나의 역할이 필요합니다."),
	INVALID_TOKEN("AUTH006", "인증토큰이 올바르지 않습니다."),

	//리뷰
	NOT_FOUND_REVIEW("R001", "존재하지 않는 리뷰입니다."),
	INVALID_REVIEW_CREATOR("R002", "리뷰는 상품을 주문한 고객 본인만 작성할 수 있습니다."),
	ILLEGAL_REVIEW_REQUEST("R003", "완료된 주문에 대해서만 리뷰를 작성할 수 있습니다."),
	INVALID_REVIEW_MESSAGE("R004", "유효하지 않은 리뷰 메시지가 존재합니다."),
	;

	private final String code;
	private final String message;
}
