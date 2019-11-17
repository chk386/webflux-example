package com.nhn.r2dbc;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @author haekyu cho
 */
public interface UserReactiveRepository extends ReactiveCrudRepository<User, Long> {}
