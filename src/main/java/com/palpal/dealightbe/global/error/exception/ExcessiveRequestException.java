package com.palpal.dealightbe.global.error.exception;

import com.palpal.dealightbe.global.error.ErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExcessiveRequestException extends RuntimeException {

	private final ErrorCode errorCode;
}
