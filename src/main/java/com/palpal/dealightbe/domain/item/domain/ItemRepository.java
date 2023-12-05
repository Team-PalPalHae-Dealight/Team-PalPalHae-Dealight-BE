package com.palpal.dealightbe.domain.item.domain;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemRepository extends JpaRepository<Item, Long>, ItemRepositoryCustom {

	boolean existsByNameAndStoreId(String name, Long storeId);

	Optional<Item> findByNameAndStoreId(String name, Long storeId);

	void deleteAllByStoreId(Long storeId);

	@Query("SELECT i FROM Item i JOIN FETCH i.store s JOIN FETCH s.address WHERE i.id = :id")
	Optional<Item> findById(@Param("id") Long id);

	@Query(value = """
		SELECT i.*, s.*, a.*
		FROM items i
		INNER JOIN stores s ON i.store_id = s.id
		INNER JOIN addresses a ON s.address_id = a.id
			WHERE i.id = :id
		""", nativeQuery = true)
	Optional<Item> findByIdIgnoringStatus(@Param("id") Long id);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query(value = """
		UPDATE Item i SET i.stock = i.stock - :quantity
		WHERE i.id = :itemId AND i.stock > 0 AND i.stock >= :quantity
		""")
	int updateStock(Long itemId, int quantity);

	@Modifying
	@Query(value = """
		DELETE i
		FROM items i LEFT OUTER JOIN order_items oi ON oi.item_id = i.id
		WHERE oi.id IS NULL AND i.is_deleted = true;
		""", nativeQuery = true)
	void clearItemsDeleted();
}
