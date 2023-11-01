package com.palpal.dealightbe.domain.auth.infra;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.palpal.dealightbe.domain.member.domain.Member;

public interface AuthRepository extends JpaRepository<Member, Long> {
	@Query("""
		SELECT m FROM Member m
		WHERE m.provider = :provider AND m.providerId = :providerId
		""")
	Optional<Member> findByProviderAndProviderId(String provider, Long providerId);
}
