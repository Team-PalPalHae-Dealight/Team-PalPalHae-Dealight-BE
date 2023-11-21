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
	DEFAULT_IMAGE_ALREADY_SET("C003", "기본 이미지로 설정된 이미지는 삭제할 수 없습니다."),
	INVALID_LIST_SORT_TYPE("C004", "유효하지 않은 정렬 조건입니다."),
	REQUIRE_QUERY_PARAM("C005", "URL에 추가적인 요청 조건이 필요합니다."),

	//멤버
	NOT_FOUND_MEMBER("M001", "고객을 찾을 수 없습니다."),
	DUPLICATED_NICK_NAME("M002", "이미 존재하는 닉네임입니다."),

	//업체
	INVALID_BUSINESS_TIME("ST001", "마감 시간은 오픈 시간보다 이전일 수 없습니다"),
	NOT_FOUND_DAY_OFF("ST002", "존재하지 않는 휴무일 입니다."),
	NOT_FOUND_STORE("ST003", "업체가 존재하지 않습니다."),
	NOT_MATCH_OWNER_AND_REQUESTER("ST004", "업체 소유자와 요청자가 일치하지 않습니다."),
	NOT_FOUND_STATUS("ST005", "존재하지 않는 영업 상태 입니다."),
	CLOSED_STORE("ST006", "영업이 종료된 업체입니다."),
	ALEADY_HAS_STORE("ST007", "이미 업체를 보유하고 있습니다"),

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
	INVALID_ORDER_TOTAL_PRICE("OR009", "입력된 총 금액이 실제와 일치하지 않습니다."),

	//장바구니
	NOT_FOUND_CART_ITEM("CT001", "장바구니에 상품이 존재하지 않습니다."),
	INVALID_CART_QUANTITY("CT002", "상품 당 최소 1개에서 최대 재고 수량까지만 장바구니에 담을 수 있습니다."),
	ANOTHER_STORE_ITEM_ALREADY_EXISTS_IN_THE_CART("CT003", "이미 다른 업체의 상품이 장바구니에 담겨 있습니다."),
	INVALID_ATTEMPT_TO_ADD_OWN_STORE_ITEM_TO_CART("CT004", "본인이 등록한 업체의 상품은 장바구니에 담을 수 없습니다."),
	EXCEEDED_CART_ITEM_SIZE("CT005", "최대 5가지 종류의 상품까지만 장바구니에 담을 수 있습니다."),
	INVALID_CART_ADDITION_TYPE("CT006", "유효하지 않은 장바구니 담기 타입 입니다."),

	//인증, 인가
	ACCESS_DENIED("AUTH001", "사용자 인증에 실패했습니다."),
	ALREADY_EXIST_MEMBER("AUTH002", "이미 가입된 사용자입니다."),
	INVALID_ROLE_TYPE("AUTH003", "유효하지 않은 권한이 입력됐습니다."),
	NOT_FOUND_ROLE("AUTH004", "존재하지 않는 역할입니다."),
	INVALID_ROLE_UPDATE("AUTH005", "적어도 하나의 역할이 필요합니다."),
	REQUIRED_AUTHENTICATION("AUTH006", "토큰이 필요한 접근입니다."),
	EXPIRED_TOKEN("AUTH007", "만료된 토큰입니다."),
	INVALID_TOKEN_FORMAT("AUTH008", "토큰의 유효성 혹은 형식이 올바르지 않습니다."),
	UNABLE_TO_CREATE_AUTHENTICATION("AUTH009", "토큰에 정보가 부족하여 인증생성에 실패했습니다."),
	UNABLE_TO_GET_TOKEN_FROM_AUTH_SERVER("AUTH010", "OAuth 서버로부터 토큰을 가져올 수 없습니다."),
	UNABLE_TO_GET_USER_INFO_FROM_RESOURCE_SERVER("AUTH011", "OAuth 서버로부터 유저 정보를 가져올 수 없습니다."),

	//리뷰
	NOT_FOUND_REVIEW("R001", "존재하지 않는 리뷰입니다."),
	INVALID_REVIEW_CREATOR("R002", "리뷰는 상품을 주문한 고객 본인만 작성할 수 있습니다."),
	ILLEGAL_REVIEW_REQUEST("R003", "완료된 주문에 대해서만 리뷰를 작성할 수 있습니다."),
	INVALID_REVIEW_MESSAGE("R004", "유효하지 않은 리뷰 메시지가 존재합니다."),

	//알림
	NOT_FOUND_NOTIFICATION("N001", "존재하지 않는 알림입니다."),

	// SSE
	SSE_STREAM_ERROR("SSE001", "SSE 스트림 연결 중 오류가 발생했습니다."),
	;

	private final String code;
	private final String message;
}
