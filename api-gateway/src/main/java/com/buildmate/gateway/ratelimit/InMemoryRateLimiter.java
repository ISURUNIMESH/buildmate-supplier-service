package com.buildmate.gateway.ratelimit;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;

import reactor.core.publisher.Mono;

/**
 * In-memory token-bucket rate limiter for local demos (no Redis required).
 * Compatible with Spring Cloud Gateway {@code RequestRateLimiter}.
 */
public class InMemoryRateLimiter implements RateLimiter<InMemoryRateLimiter.Config> {

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final Config defaultConfig;

    public InMemoryRateLimiter(int replenishRate, int burstCapacity) {
        this.defaultConfig = new Config(replenishRate, burstCapacity, 1);
    }

    @Override
    public Mono<Response> isAllowed(String routeId, String id) {
        Bucket bucket = buckets.computeIfAbsent(routeId + ":" + id,
                key -> new Bucket(defaultConfig.burstCapacity()));
        long now = System.currentTimeMillis();
        boolean allowed = bucket.tryConsume(defaultConfig, now);

        Map<String, String> headers = new HashMap<>();
        headers.put("X-RateLimit-Remaining", String.valueOf(Math.max(0, (long) Math.floor(bucket.tokens))));
        headers.put("X-RateLimit-Burst-Capacity", String.valueOf(defaultConfig.burstCapacity()));
        headers.put("X-RateLimit-Replenish-Rate", String.valueOf(defaultConfig.replenishRate()));
        headers.put("X-RateLimit-Requested-Tokens", String.valueOf(defaultConfig.requestedTokens()));

        return Mono.just(new Response(allowed, headers));
    }

    @Override
    public Map<String, Config> getConfig() {
        return Map.of("default", defaultConfig);
    }

    @Override
    public Class<Config> getConfigClass() {
        return Config.class;
    }

    @Override
    public Config newConfig() {
        return new Config(defaultConfig.replenishRate(), defaultConfig.burstCapacity(),
                defaultConfig.requestedTokens());
    }

    public static class Config {
        private int replenishRate = 10;
        private int burstCapacity = 20;
        private int requestedTokens = 1;

        public Config() {
        }

        public Config(int replenishRate, int burstCapacity, int requestedTokens) {
            this.replenishRate = replenishRate;
            this.burstCapacity = burstCapacity;
            this.requestedTokens = requestedTokens;
        }

        public int replenishRate() {
            return replenishRate;
        }

        public int burstCapacity() {
            return burstCapacity;
        }

        public int requestedTokens() {
            return requestedTokens;
        }

        public int getReplenishRate() {
            return replenishRate;
        }

        public void setReplenishRate(int replenishRate) {
            this.replenishRate = replenishRate;
        }

        public int getBurstCapacity() {
            return burstCapacity;
        }

        public void setBurstCapacity(int burstCapacity) {
            this.burstCapacity = burstCapacity;
        }

        public int getRequestedTokens() {
            return requestedTokens;
        }

        public void setRequestedTokens(int requestedTokens) {
            this.requestedTokens = requestedTokens;
        }
    }

    private static final class Bucket {
        private double tokens;
        private long lastRefillMillis;

        private Bucket(int burstCapacity) {
            this.tokens = burstCapacity;
            this.lastRefillMillis = System.currentTimeMillis();
        }

        private synchronized boolean tryConsume(Config config, long now) {
            refill(config, now);
            if (tokens >= config.requestedTokens()) {
                tokens -= config.requestedTokens();
                return true;
            }
            return false;
        }

        private void refill(Config config, long now) {
            if (now <= lastRefillMillis) {
                return;
            }
            double elapsedSeconds = (now - lastRefillMillis) / 1000.0;
            tokens = Math.min(config.burstCapacity(), tokens + elapsedSeconds * config.replenishRate());
            lastRefillMillis = now;
        }
    }
}
