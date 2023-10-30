package com.palpal.dealightbe.domain.store.domain;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, Long> {

	Optional<Store> findByMemberId(Long memberId);
}
