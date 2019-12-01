package com.nhn.webflux.reactive.user.repository;

import com.nhn.webflux.reactive.user.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author haekyu cho
 */
public interface UserRepository extends JpaRepository<User, Long> {}
