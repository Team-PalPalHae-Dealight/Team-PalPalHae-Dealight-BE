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
import com.palpal.dealightbe.domain.auth.application.dto.request.MemberSignupReq;
import com.palpal.dealightbe.domain.auth.application.dto.response.MemberSignupRes;
import com.palpal.dealightbe.domain.member.domain.Member;
import com.palpal.dealightbe.global.aop.ProviderId;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	@PostMapping("/signup")
	public ResponseEntity<MemberSignupRes> signup(@RequestBody @Validated MemberSignupReq request) {
		MemberSignupRes response = authService.signup(request);

		return ResponseEntity
			.status(HttpStatus.CREATED)
			.body(response);
	}

	@ProviderId
	@GetMapping("/test")
	public ResponseEntity<String> test(Long providerId) {
		Member memberByProviderId = authService.findMemberByProviderId(providerId);

		return ResponseEntity
			.ok(memberByProviderId.toString());
	}
}
