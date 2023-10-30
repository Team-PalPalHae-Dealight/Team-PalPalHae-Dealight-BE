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

	//파일
	NOT_FOUND_IMAGE("F001", "존재하지 않는 이미지 입니다."),
	EMPTY_IMAGE("F002", "이미지를 업로드 바랍니다."),
	INVALID_IMAGE_FORMAT("F003", "지원하지 않는 이미지 파일 형식입니다.");

	private final String code;
	private final String message;
}
