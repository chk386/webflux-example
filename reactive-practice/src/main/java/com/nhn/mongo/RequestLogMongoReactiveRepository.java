package com.nhn.mongo;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

/**
 * @author haekyu cho
 */
public interface RequestLogMongoReactiveRepository extends ReactiveMongoRepository<RequestLog, Long> {}
