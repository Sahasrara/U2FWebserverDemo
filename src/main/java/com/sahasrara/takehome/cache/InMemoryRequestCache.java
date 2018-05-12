package com.sahasrara.takehome.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * In-memory implementation of RequestCache.
 */
public class InMemoryRequestCache implements RequestCache {
    private final Cache<String, ChallengeData> requestCache;

    public InMemoryRequestCache() {
        requestCache = CacheBuilder
                .newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build();
    }

    @Override
    public void putRequest(String loginRequestId, ChallengeData registerRequestData) {
        requestCache.put(loginRequestId, registerRequestData);
    }

    @Override
    public <T> Optional<ChallengeData<T>> getRequest(String loginRequestId) {
        return Optional.ofNullable(requestCache.getIfPresent(loginRequestId));
    }

    @Override
    public <T> Optional<ChallengeData<T>> evictRequest(String loginRequestId) {
        ChallengeData requestData = requestCache.getIfPresent(loginRequestId);
        requestCache.invalidate(loginRequestId);
        return Optional.ofNullable(requestData);
    }
}
