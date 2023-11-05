package com.palpal.dealightbe.domain.item.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemRepository extends JpaRepository<Item, Long>, ItemRepositoryCustom {

	boolean existsByNameAndStoreId(String name, Long storeId);

	@Query("SELECT i FROM Item i JOIN i.store s " +
		"WHERE s.storeStatus = 'OPENED'" +
		"AND (6371 * ACOS(COS(RADIANS(:yCoordinate)) " +
		"    	* COS(RADIANS(s.address.yCoordinate)) " +
		"		* COS(RADIANS(s.address.xCoordinate) - RADIANS(:xCoordinate)) " +
		"       + SIN(RADIANS(:yCoordinate)) * SIN(RADIANS(s.address.yCoordinate)))) < 3" +
		"ORDER BY " +
		"CASE " +
		"  WHEN s.closeTime < CURRENT_TIME THEN " +
		"    (24 * 60 * 60 + (HOUR(s.closeTime) * 3600 + MINUTE(s.closeTime) * 60 + SECOND(s.closeTime))) " +
		"  ELSE " +
		"    (HOUR(s.closeTime) * 3600 + MINUTE(s.closeTime) * 60 + SECOND(s.closeTime)) " +
		"END ASC, i.updatedAt DESC")
	Page<Item> findAllByDeadline(@Param("xCoordinate") double xCoordinate, @Param("yCoordinate") double yCoordinate, Pageable pageable);
}
