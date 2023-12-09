package com.palpal.dealightbe.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import com.palpal.dealightbe.config.ElasticTestContainer;
import com.palpal.dealightbe.config.RedisConfig;
import com.palpal.dealightbe.domain.item.domain.ItemRepository;
import com.palpal.dealightbe.domain.member.domain.MemberRepository;
import com.palpal.dealightbe.domain.order.application.OrderService;
import com.palpal.dealightbe.domain.order.domain.OrderRepository;
import com.palpal.dealightbe.domain.review.application.ReviewService;
import com.palpal.dealightbe.domain.review.domain.ReviewRepository;
import com.palpal.dealightbe.domain.store.domain.StoreRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
@Import({ElasticTestContainer.class, RedisConfig.class})
public abstract class IntegrationTest {
	@Autowired
	protected OrderRepository orderRepository;

	@Autowired
	protected MemberRepository memberRepository;

	@Autowired
	protected StoreRepository storeRepository;

	@Autowired
	protected OrderService orderService;

	@Autowired
	protected ItemRepository itemRepository;

	@Autowired
	protected ReviewRepository reviewRepository;

	@Autowired
	protected ReviewService reviewService;
}
