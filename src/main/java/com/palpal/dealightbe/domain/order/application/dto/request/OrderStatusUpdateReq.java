package com.palpal.dealightbe.domain.order.application.dto.request;

import javax.validation.constraints.NotBlank;

public record OrderStatusUpdateReq(
	@NotBlank(message = "변경 후의 주문 상태를 입력해 주세요")
	String status
) {
}
