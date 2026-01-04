package org.example.gateway;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class HomeRedirectController {

    @GetMapping("/")
    public Mono<Void> home(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.TEMPORARY_REDIRECT);
        response.getHeaders().set("Location", "/public/info");
        return response.setComplete();
    }
}