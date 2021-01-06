package com.webflux.router.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

// 라우터 방식 Request Logging용
@Component
public class RouterRequestFilter implements WebFilter {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public Mono<Void> filter(ServerWebExchange serverWebExchange, WebFilterChain webFilterChain) {
        ServerHttpRequest request = serverWebExchange.getRequest();
        logger.info("Path [{}]", request.getPath());
        logger.info("Params [{}]", request.getQueryParams());
        // serverWebExchange.getResponse().getHeaders().add("web-filter",
        // "web-filter-test");
        return webFilterChain.filter(serverWebExchange);
    }
}
