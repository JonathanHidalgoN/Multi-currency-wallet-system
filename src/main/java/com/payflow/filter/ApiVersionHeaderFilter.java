package com.payflow.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ApiVersionHeaderFilter extends OncePerRequestFilter {

  private static final String API_VERSION_HEADER = "X-API-Version";
  private static final String CURRENT_VERSION = "v1";

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    response.setHeader(API_VERSION_HEADER, CURRENT_VERSION);

    filterChain.doFilter(request, response);
  }
}
