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
	NOT_FOUND_DAY_OFF("ST002", "존재하지 않는 휴무일 입니다.");

	private final String code;
	private final String message;
}
