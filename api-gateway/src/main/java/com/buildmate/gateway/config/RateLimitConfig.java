package com.buildmate.gateway.config;

import com.buildmate.gateway.ratelimit.InMemoryRateLimiter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimitConfig {

    @Bean
    @Primary
    public RateLimiter<InMemoryRateLimiter.Config> inMemoryRateLimiter(
            @Value("${gateway.rate-limit.replenish-rate:10}") int replenishRate,
            @Value("${gateway.rate-limit.burst-capacity:20}") int burstCapacity) {
        return new InMemoryRateLimiter(replenishRate, burstCapacity);
    }

    /**
     * Prefer authenticated principal (OAuth2 client/user); fall back to client IP.
     */
    @Bean
    public KeyResolver clientKeyResolver() {
        return exchange -> exchange.getPrincipal()
                .map(principal -> principal.getName())
                .switchIfEmpty(Mono.justOrEmpty(exchange.getRequest().getRemoteAddress())
                        .map(address -> address.getAddress().getHostAddress())
                        .defaultIfEmpty("anonymous"));
    }
}
