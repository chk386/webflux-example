package com.nhn.webflux.configuration;

import com.nhn.mongo.RequestLog;
import com.nhn.mongo.RequestLogMongoReactiveRepository;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;

import java.util.List;
import java.util.stream.Collectors;

import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.SenderOptions;

import static java.time.Duration.ofSeconds;

@Configuration
public class KafkaConfiguration {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Bean
  ReactiveKafkaConsumerTemplate kafkaConsumerTemplate(KafkaProperties kafkaProperties) {
    var consumer = kafkaProperties.getConsumer();
    var properties = consumer.buildProperties();
    var topic = consumer.getProperties()
                        .get("topic");

    var receiverOptions = ReceiverOptions.<String, Object>create(properties).subscription(List.of(topic));

    return new ReactiveKafkaConsumerTemplate<>(receiverOptions);
  }

  @Bean
  ReactiveKafkaProducerTemplate<String, Object> kafkaProducerTemplate(KafkaProperties kafkaProperties) {
    var producer = kafkaProperties.getProducer();

    return new ReactiveKafkaProducerTemplate<>(SenderOptions.create(producer.buildProperties()));
  }

  @Bean
  ApplicationRunner run(ReactiveKafkaConsumerTemplate<String, Object> kafkaReceiver,
                        RequestLogMongoReactiveRepository requestLogMongoReactiveRepository) {
    return args -> {
      kafkaReceiver.receive()
                   .doOnNext(r -> logger.info("[kafka consumer] topic : [{}], key : {}, value : {}",
                                              r.topic(),
                                              r.key(),
                                              r.value()))
                   .bufferTimeout(5, ofSeconds(5))
                   .flatMap(receiverRecords -> {
                     var requestLogs = receiverRecords.stream()
                                                      .map(ConsumerRecord::value)
                                                      .map(v -> (RequestLog)v)
                                                      .collect(Collectors.toList());

                     return requestLogMongoReactiveRepository.saveAll(requestLogs)
                                                             .doOnComplete(() -> receiverRecords.forEach(v -> v.receiverOffset()
                                                                                                               .acknowledge()));
                   })
                   .doOnSubscribe(v -> logger.info("webflux 토픽 구독을 시작합니다."))
                   .subscribe();
    };
  }
}
