package com.palpal.dealightbe.domain.member.domain;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import io.lettuce.core.dynamic.annotation.Param;

public interface RoleRepository extends JpaRepository<Role, Long> {

	@Query("SELECT r FROM Role r WHERE r.type = :roleType")
	Optional<Role> findByRoleType(@Param("roleType") RoleType roleType);
}
