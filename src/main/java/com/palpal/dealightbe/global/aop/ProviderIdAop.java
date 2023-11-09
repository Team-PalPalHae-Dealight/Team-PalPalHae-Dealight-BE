package com.palpal.dealightbe.global.aop;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.palpal.dealightbe.domain.auth.domain.JwtAuthentication;
import com.palpal.dealightbe.domain.auth.domain.JwtAuthenticationToken;
import com.palpal.dealightbe.domain.auth.exception.RequiredAuthenticationException;
import com.palpal.dealightbe.global.error.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Aspect
@Component
public class ProviderIdAop {

	private static final String PROVIDER_ID = "providerId";

	@Around("@annotation(ProviderId)")
	public Object getProviderId(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		JwtAuthenticationToken authentication = (JwtAuthenticationToken)SecurityContextHolder.getContext()
			.getAuthentication();
		if (authentication == null) {
			throw new RequiredAuthenticationException(ErrorCode.REQUIRED_AUTHENTICATION);
		}
		JwtAuthentication principal = (JwtAuthentication)authentication.getPrincipal();
		Long providerId = Long.parseLong(principal.getUsername());

		Object[] modifiedArgs = modifyArgsWithProviderId(providerId, proceedingJoinPoint);

		return proceedingJoinPoint.proceed(modifiedArgs);
	}

	private Object[] modifyArgsWithProviderId(Long providerId, ProceedingJoinPoint proceedingJoinPoint) {
		Object[] parameters = proceedingJoinPoint.getArgs();
		MethodSignature signature = (MethodSignature)proceedingJoinPoint.getSignature();
		Method method = signature.getMethod();
		Parameter[] methodParameters = method.getParameters();

		for (int i = 0; i < methodParameters.length; i++) {
			String parameterName = methodParameters[i].getName();
			if (parameterName.equals(PROVIDER_ID)) {
				if (parameters[i] != null) {
					break;
				}
				parameters[i] = providerId;
			}
		}

		return parameters;
	}
}
