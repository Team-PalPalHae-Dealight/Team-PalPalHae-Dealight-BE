package com.palpal.dealightbe.domain.store.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.palpal.dealightbe.global.error.ErrorCode;
import com.palpal.dealightbe.global.error.exception.BusinessException;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@AllArgsConstructor
@Slf4j
public enum DayOff {
	MON("월요일"),
	TUE("화요일"),
	WED("수요일"),
	THU("목요일"),
	FRI("금요일"),
	SAT("토요일"),
	SUN("일요일"),
	NONE("연중 무휴");

	private String name;

	@JsonValue
	public String getName() {
		return name;
	}

	@JsonCreator
	public static DayOff fromString(String text) {
		for (DayOff dayOff : DayOff.values()) {
			if (dayOff.name.equalsIgnoreCase(text)) {
				return dayOff;
			}
		}
		throw new BusinessException(ErrorCode.NOT_FOUND_DAY_OFF);
	}
}
