package com.palpal.dealightbe.global.aop;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

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
public class RefreshTokenAop {

	private static final String TOKEN = "refreshToken";

	@Around("@annotation(com.palpal.dealightbe.global.aop.RefreshToken)")
	public Object getProviderId(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		JwtAuthenticationToken authentication = (JwtAuthenticationToken)SecurityContextHolder.getContext()
			.getAuthentication();
		if (authentication == null) {
			throw new RequiredAuthenticationException(ErrorCode.REQUIRED_AUTHENTICATION);
		}
		JwtAuthentication principal = (JwtAuthentication)authentication.getPrincipal();
		String token = principal.getToken();

		Object[] modifiedArgs = modifyArgsWithToken(token, proceedingJoinPoint);

		return proceedingJoinPoint.proceed(modifiedArgs);
	}

	private Object[] modifyArgsWithToken(String token, ProceedingJoinPoint proceedingJoinPoint) {
		Object[] parameters = proceedingJoinPoint.getArgs();
		MethodSignature signature = (MethodSignature)proceedingJoinPoint.getSignature();
		Method method = signature.getMethod();
		Parameter[] methodParameters = method.getParameters();

		for (int i = 0; i < methodParameters.length; i++) {
			String parameterName = methodParameters[i].getName();
			if (parameterName.equals(TOKEN)) {
				if (parameters[i] != null) {
					break;
				}
				parameters[i] = token;
			}
		}

		return parameters;
	}
}
