package com.payflow.iterceptor;

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

  private final RateLimitService rateLimitService;

  public RateLimitInterceptor(RateLimitService rateLimitService) {
    this.rateLimitService = rateLimitService;
  }

  @Override
  public boolean preHandle(HttpServletRequest request,
      HttpServletResponse response,
      Object handler) throws Exception {
    String requestUri = request.getRequestURI();
    if (isUnauthenticatedEndpoint(requestUri)) {
      String ipAddress = getClientIpAddress(request);
      if (!rateLimitService.isAllowedForIp(ipAddress)) {
        throw new RateLimitExceededException(ipAddress, "IP");
      }
    } else {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      if (authentication != null && authentication.isAuthenticated()) {
        Long userId = (Long) authentication.getPrincipal();
        if (!rateLimitService.isAllowedForUser(userId)) {
          throw new RateLimitExceededException("User " + userId, "User");
        }
      }
    }
    return true;
  }

  private boolean isUnauthenticatedEndpoint(String uri) {
    return uri.equals("/api/auth/login") ||
        uri.equals("/api/auth/register");
  }

  private String getClientIpAddress(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
      // We care about the first, sometimes this header has multiple
      return xForwardedFor.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }

}
