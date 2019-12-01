package com.nhn.webflux.reactive.user.repository;

import com.nhn.webflux.reactive.user.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {}
