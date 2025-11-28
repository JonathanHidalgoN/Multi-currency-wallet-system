package com.payflow.filter;

import com.payflow.RateLimitService;
import com.payflow.exception.RateLimitExceededException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Rate Limit Filter for unauthenticated endpoints
 * Runs BEFORE Spring Security (order = -200) to block brute force attacks early
 */
@Component
@Order(-200)
public class RateLimitFilter extends OncePerRequestFilter {

  private static final Logger logger = LoggerFactory.getLogger(RateLimitFilter.class);

  private final RateLimitService rateLimitService;

  public RateLimitFilter(RateLimitService rateLimitService) {
    this.rateLimitService = rateLimitService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    String requestUri = request.getRequestURI();

    if (isUnauthenticatedEndpoint(requestUri)) {
      String ipAddress = getClientIpAddress(request);

      logger.debug("Checking rate limit for IP: {} on endpoint: {}", ipAddress, requestUri);

      if (!rateLimitService.isAllowedForIp(ipAddress)) {
        logger.warn("Rate limit exceeded for IP: {} on endpoint: {}", ipAddress, requestUri);
        throw new RateLimitExceededException(ipAddress, "IP");
      }
    }

    filterChain.doFilter(request, response);
  }

  private boolean isUnauthenticatedEndpoint(String uri) {
    return uri.equals("/api/auth/login") ||
        uri.equals("/api/auth/register");
  }

  private String getClientIpAddress(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
      // Get the first IP (original client) from the chain
      return xForwardedFor.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
}
