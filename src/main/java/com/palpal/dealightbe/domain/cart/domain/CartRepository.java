package com.palpal.dealightbe.domain.cart.domain;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface CartRepository extends CrudRepository<Cart, String> {

	List<Cart> findAllByMemberProviderId(Long providerId);

	Optional<Cart> findByItemIdAndMemberProviderId(Long itemId, Long providerId);
}
