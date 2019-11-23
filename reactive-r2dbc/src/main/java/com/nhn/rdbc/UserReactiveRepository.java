package com.nhn.rdbc;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

/**
 * @author haekyu cho
 */
public interface UserReactiveRepository extends ReactiveCrudRepository<User, Long> {}
