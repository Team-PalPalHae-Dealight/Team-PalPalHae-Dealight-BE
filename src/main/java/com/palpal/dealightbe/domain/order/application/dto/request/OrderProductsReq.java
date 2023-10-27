package com.palpal.dealightbe.domain.order.application.dto.request;

import java.util.List;

public record OrderProductsReq (

	List<OrderProductReq> orderProducts,
	boolean hasNext
){}
