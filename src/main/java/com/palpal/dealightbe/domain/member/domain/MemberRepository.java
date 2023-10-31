package com.palpal.dealightbe.domain.member.domain;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
	Optional<Member> findMemberByProviderId(Long providerId);
}
