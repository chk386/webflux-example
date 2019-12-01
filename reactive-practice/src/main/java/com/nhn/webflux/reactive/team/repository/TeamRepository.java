package com.nhn.webflux.reactive.team.repository;

import com.nhn.webflux.reactive.team.entity.Team;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author haekyu cho
 */

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {}
