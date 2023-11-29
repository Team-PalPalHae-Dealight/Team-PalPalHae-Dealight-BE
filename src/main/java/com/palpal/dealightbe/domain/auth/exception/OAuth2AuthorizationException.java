package com.palpal.dealightbe.domain.auth.exception;

import com.palpal.dealightbe.global.error.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class OAuth2AuthorizationException extends RuntimeException {

	private final ErrorCode errorCode;
}
