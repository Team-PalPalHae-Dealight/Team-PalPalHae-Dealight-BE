package com.palpal.dealightbe.domain.member.domain;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import io.lettuce.core.dynamic.annotation.Param;

public interface MemberRepository extends JpaRepository<Member, Long> {

	Optional<Member> findMemberByProviderId(Long providerId);

	@Query("""
		SELECT m FROM Member m
		WHERE m.provider = :provider AND m.providerId = :providerId
		""")
	Optional<Member> findByProviderAndProviderId(@Param("provider") String provider,
		@Param("providerId") Long providerId);
	
	boolean existsByNickName(String nickName);
}
