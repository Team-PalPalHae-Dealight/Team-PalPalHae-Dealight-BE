package com.palpal.dealightbe.domain.item.domain;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemRepository extends JpaRepository<Item, Long>, ItemRepositoryCustom {

	boolean existsByNameAndStoreId(String name, Long storeId);

	Optional<Item> findByNameAndStoreId(String name, Long storeId);

	void deleteAllByStoreId(Long storeId);

	@Query("SELECT i FROM Item i JOIN FETCH i.store s JOIN FETCH s.address WHERE i.id = :id")
	Optional<Item> findById(@Param("id") Long id);
}
