package com.palpal.dealightbe.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.palpal.dealightbe.domain.auth.application.AuthService;
import com.palpal.dealightbe.domain.auth.application.OAuth2AuthorizationService;
import com.palpal.dealightbe.domain.auth.presentation.AuthController;
import com.palpal.dealightbe.domain.cart.application.CartService;
import com.palpal.dealightbe.domain.cart.presentation.CartController;
import com.palpal.dealightbe.domain.item.application.ItemService;
import com.palpal.dealightbe.domain.item.presentation.ItemController;
import com.palpal.dealightbe.domain.member.application.MemberService;
import com.palpal.dealightbe.domain.member.presentation.MemberController;
import com.palpal.dealightbe.domain.notification.application.NotificationService;
import com.palpal.dealightbe.domain.notification.presentation.NotificationController;
import com.palpal.dealightbe.domain.order.application.OrderService;
import com.palpal.dealightbe.domain.order.presentation.OrderController;
import com.palpal.dealightbe.domain.review.application.ReviewService;
import com.palpal.dealightbe.domain.review.presentation.ReviewController;
import com.palpal.dealightbe.domain.store.application.StoreService;
import com.palpal.dealightbe.domain.store.presentation.StoreController;

@WebMvcTest(
	value = {
		OrderController.class, MemberController.class, ItemController.class, StoreController.class,
		AuthController.class, ReviewController.class, CartController.class, NotificationController.class
	},
	excludeAutoConfiguration = {SecurityAutoConfiguration.class, OAuth2ClientAutoConfiguration.class}
)
@AutoConfigureRestDocs
@WithMockUser
public abstract class ControllerTest {
	@Autowired
	protected MockMvc mockMvc;

	@Autowired
	protected ObjectMapper objectMapper;

	@MockBean
	protected MemberService memberService;

	@MockBean
	protected CartService cartService;

	@MockBean
	protected ReviewService reviewService;

	@MockBean
	protected ItemService itemService;

	@MockBean
	protected StoreService storeService;

	@MockBean
	protected OrderService orderService;

	@MockBean
	protected AuthService authService;

	@MockBean
	protected OAuth2AuthorizationService oAuth2AuthorizationService;

	@MockBean
	protected NotificationService notificationService;
}
