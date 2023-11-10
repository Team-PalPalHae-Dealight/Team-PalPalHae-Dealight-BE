package com.palpal.dealightbe.domain.auth.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.palpal.dealightbe.domain.auth.application.AuthService;
import com.palpal.dealightbe.domain.auth.application.dto.request.MemberAuthReq;
import com.palpal.dealightbe.domain.auth.application.dto.response.MemberAuthRes;
import com.palpal.dealightbe.global.aop.ProviderId;
import com.palpal.dealightbe.global.aop.RefreshToken;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	@PostMapping("/signup")
	public ResponseEntity<MemberAuthRes> signup(@RequestBody @Validated MemberAuthReq request) {
		MemberAuthRes response = authService.signup(request);

		return ResponseEntity
			.status(HttpStatus.CREATED)
			.body(response);
	}

	@RefreshToken
	@ProviderId
	@GetMapping("/tokens")
	public ResponseEntity<MemberAuthRes> refreshAccessToken(Long providerId, String refreshToken) {
		MemberAuthRes response = authService.reIssueToken(providerId, refreshToken);

		return ResponseEntity
			.status(HttpStatus.CREATED)
			.body(response);
	}
}
