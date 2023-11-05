package com.palpal.dealightbe.domain.review.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.palpal.dealightbe.domain.review.application.dto.response.ReviewStatistics;

public interface ReviewRepository extends JpaRepository<Review, Long> {

	@Query(nativeQuery = true,
		value = """
				select
			 	  	reviews.content as content,
			 		count(reviews.id) as count
			 	from
			 		reviews left outer join orders on reviews.order_id = orders.id
			 		left outer join members on orders.member_id = members.id
			 			left outer join stores on orders.store_id = stores.id
			 	where stores.id = :storeId
			 	group by reviews.content
			""")
	List<ReviewStatistics> selectStatisticsByStoreId(Long storeId);
}
