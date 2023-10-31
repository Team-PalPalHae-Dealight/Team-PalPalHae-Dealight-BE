package com.palpal.dealightbe.domain.auth.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class JwtAuthentication {

	private final String username;
	private final String token;
}
