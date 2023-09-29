package org.homeschoolpebt.app.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
public class DisabledApplicationsInterceptor implements HandlerInterceptor {
  public static final String PATH_FORMAT = "/flow/{flow}/{screen}";

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    try {
      var parsedUrl = new AntPathMatcher().extractUriTemplateVariables(PATH_FORMAT, request.getRequestURI());
      String urlFlow = parsedUrl.get("flow");
      if (urlFlow.equals("pebt") || urlFlow.equals("docUpload")) {
        log.info("Redirecting to homepage - flow {} disabled", urlFlow);
        response.sendRedirect("/");
        return false;
      } else {
        return true;
      }
    } catch (IllegalStateException e) {
      return true;
    }
  }
}
