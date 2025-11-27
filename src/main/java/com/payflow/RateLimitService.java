package com.payflow;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;

@Service
public class RateLimitService {

  private final Map<String, Bucket> ipBuckets = new ConcurrentHashMap<>();
  private final Map<String, Bucket> usersBucket = new ConcurrentHashMap<>();

  private Bucket createIpBucket() {
    return Bucket.builder()
        .addLimit(Bandwidth.builder()
            .capacity(5)
            .refillIntervally(5, Duration.ofMinutes(1))
            .build())
        .build();
  }

  private Bucket createUserBucket() {
    return Bucket.builder()
        .addLimit(Bandwidth.builder()
            .capacity(100)
            .refillIntervally(100, Duration.ofMinutes(1))
            .build())
        .build();
  }

  public boolean isAllowedForIp(String ipAddress) {
    Bucket bucket = ipBuckets
        .computeIfAbsent(ipAddress, key -> createIpBucket());
    return bucket.tryConsume(1);
  }

  public boolean isAllowedForUser(long userId) {
    Bucket bucket = usersBucket
        .computeIfAbsent("user:" + userId, key -> createUserBucket());
    return bucket.tryConsume(1);
  }

  public long getRemainingTokensForIp(String ipAddress) {
    Bucket bucket = ipBuckets.get(ipAddress);
    return bucket != null ? bucket.getAvailableTokens() : 5;
  }

  public long getRemainingTokensForUser(long userId) {
    Bucket bucket = usersBucket.get("user:" + userId);
    return bucket != null ? bucket.getAvailableTokens() : 100;
  }

}
