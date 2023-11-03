package com.palpal.dealightbe.domain.member.domain;

import java.util.Arrays;

import com.palpal.dealightbe.global.error.ErrorCode;
import com.palpal.dealightbe.global.error.exception.BusinessException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RoleType {

	ROLE_MEMBER("member"),
	ROLE_STORE("store"),
	ROLE_ADMIN("admin");

	private final String role;

	public static RoleType fromString(String role) {
		return Arrays.stream(RoleType.values())
			.filter(roleType -> roleType.getRole().equals(role))
			.findAny()
			.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_ROLE_TYPE));
	}
}
