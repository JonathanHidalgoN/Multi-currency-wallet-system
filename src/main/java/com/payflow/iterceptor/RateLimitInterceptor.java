package com.payflow.iterceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.security.core.Authentication;

import com.payflow.RateLimitService;
import com.payflow.exception.RateLimitExceededException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

  private static final Logger logger = LoggerFactory.getLogger(RateLimitInterceptor.class);

  private final RateLimitService rateLimitService;

  public RateLimitInterceptor(RateLimitService rateLimitService) {
    this.rateLimitService = rateLimitService;
  }

  @Override
  public boolean preHandle(HttpServletRequest request,
      HttpServletResponse response,
      Object handler) throws Exception {

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication != null && authentication.isAuthenticated()
        && authentication.getPrincipal() instanceof Long) {
      Long userId = (Long) authentication.getPrincipal();

      logger.debug("Checking rate limit for user: {}", userId);

      if (!rateLimitService.isAllowedForUser(userId)) {
        logger.warn("Rate limit exceeded for user: {}", userId);
        throw new RateLimitExceededException("User " + userId, "User");
      }
    }

    return true;
  }
}
