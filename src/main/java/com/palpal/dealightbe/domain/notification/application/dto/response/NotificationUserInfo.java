package com.palpal.dealightbe.domain.notification.application.dto.response;

import com.palpal.dealightbe.domain.member.domain.RoleType;

public record NotificationUserInfo(Long id, RoleType role) {
}
