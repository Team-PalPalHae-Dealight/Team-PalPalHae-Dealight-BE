package com.palpal.dealightbe.domain.store.domain;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StoreRepository extends JpaRepository<Store, Long>, StoreRepositoryCustom {

	@Query("SELECT s FROM Store s JOIN FETCH s.member m WHERE m.providerId = :providerId")
	Optional<Store> findByMemberProviderId(@Param("providerId") Long providerId);
}
