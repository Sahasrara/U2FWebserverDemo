package com.sahasrara.takehome.cache;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Optional;

/**
 * Super simple cache to hold U2F request data.
 */
public interface RequestCache {
    /**
     * Put a new request in the cache.
     * @param loginGroupId group ID that identifies a group of users' login attempt
     * @param usernameToRequestData map of user name to request data
     * @param <T> the type of the challenge request, registration or authentication
     */
    <T> void putRequest(String loginGroupId, ChallengeData<T> usernameToRequestData);

    /**
     * Fetch request information from the cache.
     * @param loginGroupId group ID that identifies a group of users' login attempt
     * @param <T> the type of the challenge request, registration or authentication
     * @return cached request data
     */
    <T> Optional<ChallengeData<T>> getRequest(String loginGroupId);

    /**
     * Remove request information from the cache.
     * @param loginGroupId group ID that identifies a group of users' login attempt
     * @param <T> the type of the challenge request, registration or authentication
     * @return cached request data
     */
    <T> Optional<ChallengeData<T>> evictRequest(String loginGroupId);

    /**
     * ChallengeData holds challenge information for challenges yet to be signed.
     * @param <T> the type of the challenge request, registration or authentication
     */
    @Getter
    @Setter
    @AllArgsConstructor
    class ChallengeData<T> {
        Map<String, T> usernameToRequestData;
    }
}
