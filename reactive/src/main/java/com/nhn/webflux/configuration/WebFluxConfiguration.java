package com.nhn.webflux.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * @author haekyu cho
 */

@Configuration
@EnableWebFlux
public class WebFluxConfiguration implements WebFluxConfigurer {

    @Override
    public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
        configurer.defaultCodecs().enableLoggingRequestDetails(true);
    }

//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//
//        registry.addMapping("/**")
//                .allowedOrigins("http://localhost")
//                .allowedMethods("GET", "POST", "OPTIONS","PUT", "DELETE")
////                .allowedHeaders("header1", "header2", "header3")
////                .exposedHeaders("header1", "header2")
//                .allowCredentials(true).maxAge(3600);
//
//        // Add more mappings...
//    }
}
