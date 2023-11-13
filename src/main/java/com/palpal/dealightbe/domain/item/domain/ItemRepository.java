package com.palpal.dealightbe.domain.item.domain;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemRepository extends JpaRepository<Item, Long>, ItemRepositoryCustom {

	boolean existsByNameAndStoreId(String name, Long storeId);

	Optional<Item> findByNameAndStoreId(String name, Long storeId);

	@Query(value =
		"""
			SELECT * 
			FROM items i 
			LEFT JOIN stores s ON i.store_id = s.id 
			LEFT JOIN addresses a ON s.address_id = a.id 
			WHERE s.store_status = 'OPENED' 
			AND (6371 * ACOS(COS(RADIANS(:yCoordinate)) 
			    	* COS(RADIANS(a.y_coordinate)) 
					* COS(RADIANS(a.x_coordinate) - RADIANS(:xCoordinate)) 
			       + SIN(RADIANS(:yCoordinate)) * SIN(RADIANS(a.y_coordinate)))) < 3 
			ORDER BY 
			    CASE 
			        WHEN s.close_time < CURTIME() 
			        	THEN (24 * 60 * 60 + (HOUR(s.close_time) * 3600 + MINUTE(s.close_time) * 60 + SECOND(s.close_time))) 
			    	ELSE (HOUR(s.close_time) * 3600 + MINUTE(s.close_time) * 60 + SECOND(s.close_time)) 
			    END ASC, i.updated_at DESC
			""",
		nativeQuery = true)
	Page<Item> findAllByDeadline(@Param("xCoordinate") double xCoordinate, @Param("yCoordinate") double yCoordinate, Pageable pageable);

	@Query(
		value =
			"""
				SELECT * 
				FROM items i 
				LEFT JOIN stores s ON i.store_id = s.id 
				LEFT JOIN addresses a ON s.address_id = a.id 
				WHERE s.store_status = 'OPENED' 
				AND (6371 * ACOS(COS(RADIANS(:yCoordinate)) 
				    	* COS(RADIANS(a.y_coordinate)) 
						* COS(RADIANS(a.x_coordinate) - RADIANS(:xCoordinate)) 
				       + SIN(RADIANS(:yCoordinate)) * SIN(RADIANS(a.y_coordinate)))) < 3 
				ORDER BY (i.original_price - i.discount_price) * 1.0 / i.original_price DESC, i.updated_at DESC
				""",
		nativeQuery = true)
	Page<Item> findAllByDiscountRate(@Param("xCoordinate") double xCoordinate, @Param("yCoordinate") double yCoordinate, Pageable pageable);

	@Query(
		value =
			"""
				SELECT *
				FROM items i 
				LEFT JOIN stores s ON i.store_id = s.id 
				LEFT JOIN addresses a ON s.address_id = a.id 
				WHERE s.store_status = 'OPENED' 
				AND (6371 * ACOS(COS(RADIANS(:yCoordinate)) 
				    	* COS(RADIANS(a.y_coordinate)) 
						* COS(RADIANS(a.x_coordinate) - RADIANS(:xCoordinate)) 
				       + SIN(RADIANS(:yCoordinate)) * SIN(RADIANS(a.y_coordinate)))) < 3 
				ORDER BY (6371 * ACOS(COS(RADIANS(:yCoordinate)) 
				    	* COS(RADIANS(a.y_coordinate)) 
						* COS(RADIANS(a.x_coordinate) - RADIANS(:xCoordinate)) 
				       + SIN(RADIANS(:yCoordinate)) * SIN(RADIANS(a.y_coordinate)))) ASC, i.updated_at DESC
				""",
		nativeQuery = true)
	Page<Item> findAllByDistance(@Param("xCoordinate") double xCoordinate, @Param("yCoordinate") double yCoordinate, Pageable pageable);
}
