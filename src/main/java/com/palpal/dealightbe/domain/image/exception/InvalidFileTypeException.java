package com.palpal.dealightbe.domain.image.exception;

import com.palpal.dealightbe.global.error.ErrorCode;
import com.palpal.dealightbe.global.error.exception.BusinessException;

public class InvalidFileTypeException extends BusinessException {

	public InvalidFileTypeException(ErrorCode errorCode) {
		super(errorCode);
	}
}
