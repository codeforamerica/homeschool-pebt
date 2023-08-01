package org.homeschoolpebt.app.filters;


import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.joda.time.DateTime;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class LoggingFilter implements Filter {

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    Filter.super.init(filterConfig);
  }

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                       FilterChain filterChain) throws ServletException, IOException {
    HttpServletRequest request = (HttpServletRequest) servletRequest;
    HttpSession session = request.getSession();
    UUID id = (UUID) session.getAttribute("id");

    MDC.put("sessionId", session.getId());
    MDC.put("submissionId", id == null ? "null" : id.toString());
    MDC.put("sessionCreatedAt", new DateTime(session.getCreationTime()).toString("HH:mm:ss.SSS"));
    MDC.put("method", request.getMethod());
    MDC.put("request", request.getRequestURI());
    filterChain.doFilter(servletRequest, servletResponse);
    MDC.clear();
  }

  @Override
  public void destroy() {
    Filter.super.destroy();
  }
}
