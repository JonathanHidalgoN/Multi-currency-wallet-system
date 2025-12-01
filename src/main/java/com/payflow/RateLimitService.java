package com.payflow;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.payflow.config.RateLimitProperties;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;

@Service
public class RateLimitService {

  private final Map<String, Bucket> ipBuckets = new ConcurrentHashMap<>();
  private final Map<String, Bucket> usersBucket = new ConcurrentHashMap<>();
  private final RateLimitProperties rateLimitProperties;

  public RateLimitService(RateLimitProperties rateLimitProperties) {
    this.rateLimitProperties = rateLimitProperties;
  }

  private Bucket createIpBucket() {
    return Bucket.builder()
        .addLimit(Bandwidth.builder()
            .capacity(rateLimitProperties.getIpCapacity())
            .refillIntervally(rateLimitProperties.getIpRefillRate(),
                Duration.ofMinutes(rateLimitProperties.getIpRefillDurationMinutes()))
            .build())
        .build();
  }

  private Bucket createUserBucket() {
    return Bucket.builder()
        .addLimit(Bandwidth.builder()
            .capacity(rateLimitProperties.getUserCapacity())
            .refillIntervally(rateLimitProperties.getUserRefillRate(),
                Duration.ofMinutes(rateLimitProperties.getUserRefillDurationMinutes()))
            .build())
        .build();
  }

  public boolean isAllowedForIp(String ipAddress) {
    if (!rateLimitProperties.getEnabled()) {
      return true;
    }
    Bucket bucket = ipBuckets
        .computeIfAbsent(ipAddress, key -> createIpBucket());
    return bucket.tryConsume(1);
  }

  public boolean isAllowedForUser(long userId) {
    if (!rateLimitProperties.getEnabled()) {
      return true;
    }
    Bucket bucket = usersBucket
        .computeIfAbsent("user:" + userId, key -> createUserBucket());
    return bucket.tryConsume(1);
  }

  public long getRemainingTokensForIp(String ipAddress) {
    Bucket bucket = ipBuckets.get(ipAddress);
    return bucket != null ? bucket.getAvailableTokens() : rateLimitProperties.getIpCapacity();
  }

  public long getRemainingTokensForUser(long userId) {
    Bucket bucket = usersBucket.get("user:" + userId);
    return bucket != null ? bucket.getAvailableTokens() : rateLimitProperties.getUserCapacity();
  }

}
