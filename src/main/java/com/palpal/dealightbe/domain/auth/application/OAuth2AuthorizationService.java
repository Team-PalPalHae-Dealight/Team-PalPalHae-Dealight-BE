package com.palpal.dealightbe.domain.auth.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class OAuth2AuthorizationService {

	private final
	private final RestTemplate restTemplate;

	public void authorizeKakao(String code) {

	}
}
