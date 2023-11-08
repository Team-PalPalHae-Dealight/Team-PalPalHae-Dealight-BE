package com.palpal.dealightbe.domain.store.domain;

import java.util.Optional;

import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StoreRepository extends JpaRepository<Store, Long> {

	Optional<Store> findByMemberId(Long memberId);

	@Query("SELECT s FROM Store s JOIN FETCH s.member m WHERE m.providerId = :providerId")
	Optional<Store> findByMemberProviderId(@Param("providerId") Long providerId);

	@Query(value = """
		SELECT s
		FROM Store s 
		JOIN FETCH s.items i 
		WHERE 
		   (6371 * acos(cos(radians(:xCoordinate)) * cos(radians(s.x_coordinate)) * cos(radians(s.y_coordinate) - radians(:yCoordinate)) + 
		   sin(radians(:xCoordinate)) * sin(radians(s.x_coordinate)))) <= 3 
		   AND (s.storeStatus = 'OPEN') 
		   AND (s.name LIKE %:keyword% OR i.name LIKE %:keyword%) 
		GROUP BY s.id 
		ORDER BY distance ASC
		""", nativeQuery = true)
	Slice<Store> findByDistanceWithin3Km(
		@Param("xCoordinate") double xCoordinate,
		@Param("yCoordinate") double yCoordinate,
		@Param("keyword") String keyword
	);

	@Query(value = """
		SELECT s
		FROM Store s 
		JOIN FETCH s.items i 
		WHERE 
		   (6371 * acos(cos(radians(:xCoordinate)) * cos(radians(s.x_coordinate)) * cos(radians(s.y_coordinate) - radians(:yCoordinate)) + 
		   sin(radians(:xCoordinate)) * sin(radians(s.x_coordinate)))) <= 3 
		   AND (s.storeStatus = 'OPEN') 
		   AND (s.name LIKE %:keyword% OR i.name LIKE %:keyword%) 
		GROUP BY s.id 
		ORDER BY 
		   CASE 
		       WHEN s.closeTime < CURRENT_TIME() THEN (24 * 60 * 60 + (HOUR(s.closeTime) * 3600 + MINUTE(s.closeTime) * 60 + SECOND(s.closeTime))) 
		       ELSE (HOUR(s.closeTime) * 3600 + MINUTE(s.closeTime) * 60 + SECOND(s.closeTime)) 
		   END ASC, 
		   i.updatedAt DESC
		""", nativeQuery = true)
	Slice<Store> findByDeadLine(
		@Param("xCoordinate") double xCoordinate,
		@Param("yCoordinate") double yCoordinate,
		@Param("keyword") String keyword
	);

	@Query(value = """
		SELECT s
		FROM Store s 
		JOIN FETCH s.items i 
		WHERE 
		   (6371 * acos(cos(radians(:xCoordinate)) * cos(radians(s.x_coordinate)) * cos(radians(s.y_coordinate) - radians(:yCoordinate)) + 
		   sin(radians(:xCoordinate)) * sin(radians(s.x_coordinate)))) <= 3 
		   AND (s.storeStatus = 'OPEN') 
		   AND (s.name LIKE %:keyword% OR i.name LIKE %:keyword%) 
		GROUP BY s.id 
		ORDER BY 
		   (i.originalPrice - i.discountPrice) * 1.0 / i.originalPrice DESC, 
		   i.updatedAt DESC
		""", nativeQuery = true)
	Slice<Store> findByDiscountRate(
		@Param("xCoordinate") double xCoordinate,
		@Param("yCoordinate") double yCoordinate,
		@Param("keyword") String keyword
	);
}
