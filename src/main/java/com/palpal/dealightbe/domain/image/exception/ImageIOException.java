package com.palpal.dealightbe.domain.image.exception;

import com.palpal.dealightbe.global.error.ErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ImageIOException extends RuntimeException {

	private final ErrorCode errorCode;
}
