package com.nhn.mongo;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface RequestLogMongoReactiveRepository extends ReactiveMongoRepository<RequestLog, Long> {}
