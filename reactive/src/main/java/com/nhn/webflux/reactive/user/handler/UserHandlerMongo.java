//package com.nhn.webflux.reactive.user.handler;
//
//import com.nhn.webflux.reactive.user.entity.User;
//import com.nhn.webflux.reactive.user.model.UserRequest;
//
//import org.springframework.data.redis.core.ReactiveRedisTemplate;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Component;
//import org.springframework.web.reactive.function.BodyInserters;
//import org.springframework.web.reactive.function.server.ServerRequest;
//import org.springframework.web.reactive.function.server.ServerResponse;
//
//import reactor.core.publisher.Mono;
//
///**
// * @author haekyu cho
// */
//
//@Component
//public class UserHandlerMongo {
//
//
//
//  public Mono<ServerResponse> getUser(ServerRequest request) {
//    return template.opsForValue()
//                   .get(request.pathVariable("id"))
//                   .flatMap(user -> ServerResponse.ok()
//                                                  .body(BodyInserters.fromValue(user)))
//                   .switchIfEmpty(ServerResponse.noContent()
//                                                .build());
//  }
//
//  public Mono<ServerResponse> createUser(ServerRequest request) {
//    return request.bodyToMono(UserRequest.class)
//                  .flatMap(req -> template.opsForValue()
//                                          .set(String.valueOf(req.getId()), toUser(req))
//                                          .flatMap(success -> success ? ServerResponse.ok()
//                                                                                      .bodyValue("성공")
//                                                                      : ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                                                                                      .bodyValue("실패")));
//  }
//
//  private User toUser(UserRequest userRequest) {
//    User user = new User();
//    user.setId(userRequest.getId());
//    user.setName(userRequest.getName());
//    user.setEmail(userRequest.getEmail());
//
//    return user;
//  }
//}
