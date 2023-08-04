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
    HttpSession session = request.getSession(false);
    UUID subId = session != null ? (UUID) session.getAttribute("id") : null;
    String sessionId = session != null ? session.getId() : "unset/expired";
    String sessionCreatedAt = session != null ?
        new DateTime(session.getCreationTime()).toString("HH:mm:ss.SSS") :
        "no session";

    MDC.put("sessionId", sessionId);
    MDC.put("sessionCreatedAt", sessionCreatedAt);
    MDC.put("submissionId", subId == null ? "null" : subId.toString());
    MDC.put("method", request.getMethod());
    MDC.put("request", request.getRequestURI());
    MDC.put("xForwardedFor", request.getHeader("X-Forwarded-For"));
    MDC.put("amznTraceId", request.getHeader("X-Amzn-Trace-Id"));

    filterChain.doFilter(servletRequest, servletResponse);
    MDC.clear();
  }

  @Override
  public void destroy() {
    Filter.super.destroy();
  }
}
