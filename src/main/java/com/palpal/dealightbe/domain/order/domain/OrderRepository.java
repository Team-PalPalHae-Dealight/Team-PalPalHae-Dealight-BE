package com.palpal.dealightbe.domain.order.domain;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {
	@Query(
		value =
			"""
				select
					orders.id as id,
					orders.member_id as member_id,
					stores.id as store_id,
					orders.arrival_time as arrival_time,
					orders.demand as demand,
					orders.order_status as order_status,
					orders.total_price as total_price,
					stores.name as store_name,
					orders.created_at as order_created_at,
					orders.created_at as created_at,
					orders.updated_at as updated_at,
					orders.review_contains as review_contains

				from
					orders left outer join members on orders.member_id = members.id
					left outer join stores on orders.store_id = stores.id
				where
					store_id = :storeId and
					(:status is null or order_status = :status)
				""",
		countQuery =
			"""
				 select count(*)
				 from
					orders left outer join members on orders.member_id = members.id
					left outer join stores on orders.store_id = stores.id
				where
					store_id = :storeId and
					(:status is null or order_status = :status)
				""",
		nativeQuery = true
	)
	Slice<Order> findAllByStoreId(
		@Param("storeId") Long storeId,
		@Param("status") String status,
		Pageable pageable
	);

	@Query(
		value =
			"""
				select
					orders.id as id,
					orders.member_id as member_id,
					stores.id as store_id,
					orders.arrival_time as arrival_time,
					orders.demand as demand,
					orders.order_status as order_status,
					orders.total_price as total_price,
					stores.name as store_name,
					orders.created_at as order_created_at,
					orders.created_at as created_at,
					orders.updated_at as updated_at,
					members.provider_id as member_provider_id,
					orders.review_contains as review_contains
				from
					orders left outer join members on orders.member_id = members.id
					left outer join stores on orders.store_id = stores.id
				where
					members.provider_id = :member_provider_id and
					(:status is null or order_status = :status)
				""",
		countQuery =
			"""
					 select count(*)
					 from
						orders left outer join members on orders.member_id = members.id
						left outer join stores on orders.store_id = stores.id
					where
						members.provider_id = :member_provider_id and
						(:status is null or order_status = :status)
				""",
		nativeQuery = true
	)
	Slice<Order> findAllByMemberProviderId(
		@Param("member_provider_id") Long memberProviderId,
		@Param("status") String status,
		Pageable pageable
	);

	@Query(value = """
		select distinct o.id
		from orders o join order_items oi on o.id = oi.order_id
		where timestampdiff(MONTH, o.updated_at, now()) >= :period
		""", nativeQuery = true)
	List<Long> findAllByUpdatedMoreThan(@Param("period") int period);

	void deleteAllInBatchByIdIn(@Param("ids") List<Long> ids);
}
