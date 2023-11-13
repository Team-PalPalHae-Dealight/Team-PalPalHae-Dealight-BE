package com.palpal.dealightbe.domain.cart.domain;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface CartRepository extends CrudRepository<Cart, String> {

	Optional<Cart> findByItemIdAndMemberProviderId(Long itemId, Long providerId);
}
