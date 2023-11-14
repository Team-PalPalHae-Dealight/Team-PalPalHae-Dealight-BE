package com.palpal.dealightbe.domain.store.domain;

import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StoreRepository extends JpaRepository<Store, Long> {

	Optional<Store> findByMemberId(Long memberId);

	@Query("SELECT s FROM Store s JOIN FETCH s.member m WHERE m.providerId = :providerId")
	Optional<Store> findByMemberProviderId(@Param("providerId") Long providerId);


	@Query(value =
		"""
				SELECT s.*
				FROM stores s
					JOIN addresses a ON s.address_id = a.id
					LEFT JOIN items i ON s.id = i.store_id
					WHERE s.store_status = 'OPENED'
					AND (6371 * ACOS(COS(RADIANS(:yCoordinate))
						    	* COS(RADIANS(a.y_coordinate))
								* COS(RADIANS(a.x_coordinate) - RADIANS(:xCoordinate))
						       + SIN(RADIANS(:yCoordinate)) * SIN(RADIANS(a.y_coordinate)))) <= 3
					AND (s.name LIKE %:keyword% OR i.name LIKE %:keyword%)
				ORDER BY (6371 * ACOS(COS(RADIANS(:yCoordinate))
						    	* COS(RADIANS(a.y_coordinate))
								* COS(RADIANS(a.x_coordinate) - RADIANS(:xCoordinate))
						       + SIN(RADIANS(:yCoordinate)) * SIN(RADIANS(a.y_coordinate)))) ASC, i.updated_at DESC
			""", nativeQuery = true)
	Slice<Store> findByDistanceWithin3Km(
		@Param("xCoordinate") double xCoordinate,
		@Param("yCoordinate") double yCoordinate,
		@Param("keyword") String keyword,
		Pageable pageable
	);

	@Query(value =
		"""
			            SELECT s.*
			            FROM stores s
			            JOIN addresses a ON s.address_id = a.id
			            LEFT JOIN items i ON s.id = i.store_id
			            WHERE s.store_status = 'OPENED'
			            AND (6371 * ACOS(COS(RADIANS(:yCoordinate))
			                        * COS(RADIANS(a.y_coordinate))
			                        * COS(RADIANS(a.x_coordinate) - RADIANS(:xCoordinate))
			                        + SIN(RADIANS(:yCoordinate)) * SIN(RADIANS(a.y_coordinate)))) <= 3
			            AND (s.name LIKE %:keyword% OR i.name LIKE %:keyword%)    
			            ORDER BY ABS(EXTRACT(HOUR FROM s.close_time) * 60 + EXTRACT(MINUTE FROM s.close_time) - 
			                        (EXTRACT(HOUR FROM CURRENT_TIME) * 60 + EXTRACT(MINUTE FROM CURRENT_TIME)))
			""", nativeQuery = true)
	Slice<Store> findByDeadLine(
		@Param("xCoordinate") double xCoordinate,
		@Param("yCoordinate") double yCoordinate,
		@Param("keyword") String keyword,
		Pageable pageable
	);

	@Query(value =
		"""
					SELECT s.*
					FROM stores s
					JOIN addresses a ON s.address_id = a.id
					LEFT JOIN items i ON s.id = i.store_id
					WHERE s.store_status = 'OPENED'
					AND (6371 * ACOS(COS(RADIANS(:yCoordinate))
								* COS(RADIANS(a.y_coordinate))
								* COS(RADIANS(a.x_coordinate) - RADIANS(:xCoordinate))
								+ SIN(RADIANS(:yCoordinate)) * SIN(RADIANS(a.y_coordinate)))) <= 3
					AND (s.name LIKE %:keyword% OR i.name LIKE %:keyword%)	
					ORDER BY (i.original_price - i.discount_price) * 1.0 / i.original_price DESC, i.updated_at DESC
			""", nativeQuery = true)
	Slice<Store> findByDiscountRate(
		@Param("xCoordinate") double xCoordinate,
		@Param("yCoordinate") double yCoordinate,
		@Param("keyword") String keyword,
		Pageable pageable
	);
}
