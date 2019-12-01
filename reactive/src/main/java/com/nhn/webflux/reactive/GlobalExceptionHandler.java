package com.nhn.webflux.reactive;

import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.EncoderHttpMessageWriter;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.List;
import java.util.Map;

@Component
@Order(-2) //DefaultErrorWebExceptionHandler order
public class GlobalExceptionHandler extends AbstractErrorWebExceptionHandler {

  public GlobalExceptionHandler(ErrorAttributes errorAttributes, ResourceProperties resourceProperties,
                                ApplicationContext applicationContext) {
    super(errorAttributes, resourceProperties, applicationContext);

    this.setMessageWriters(List.of(new EncoderHttpMessageWriter<>(new Jackson2JsonEncoder())));
  }

  @Override
  protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
    return RouterFunctions.route(RequestPredicates.all(), request -> {
      final Map<String, Object> error = getErrorAttributes(request, true);
      final int status = (Integer)error.get("status");

      return ServerResponse.status(HttpStatus.valueOf(status))
                           .contentType(MediaType.APPLICATION_JSON)
                           .body(BodyInserters.fromValue(error));
    });
  }

  @Component
  public static class GlobalErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, boolean includeStackTrace) {
      final Map<String, Object> errorAttributes = super.getErrorAttributes(request, includeStackTrace);
      errorAttributes.put("globalError", "webflux global Error");

      return errorAttributes;
    }
  }
}
