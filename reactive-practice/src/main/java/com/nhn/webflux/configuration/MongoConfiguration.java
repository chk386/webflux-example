package com.nhn.webflux.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@Configuration
@EnableReactiveMongoRepositories(value = {"com.nhn.mongo"})
public class MongoConfiguration {}
