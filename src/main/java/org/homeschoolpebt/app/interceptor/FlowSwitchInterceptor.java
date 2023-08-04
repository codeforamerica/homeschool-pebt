package org.homeschoolpebt.app.interceptor;

import formflow.library.data.SubmissionRepositoryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Objects;
import java.util.UUID;

@Component
@Slf4j
public class FlowSwitchInterceptor implements HandlerInterceptor {
  public static final String PATH_FORMAT = "/flow/{flow}/{screen}";
  private final SubmissionRepositoryService submissionRepositoryService;

  public FlowSwitchInterceptor(SubmissionRepositoryService submissionRepositoryService) {
    this.submissionRepositoryService = submissionRepositoryService;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    try {
      var parsedUrl = new AntPathMatcher().extractUriTemplateVariables(PATH_FORMAT, request.getRequestURI());
      String urlFlow = parsedUrl.get("flow");
      var session = request.getSession(false);
      if (session == null) {
        return true;
      }

      var submissionIdFromSession = (UUID) session.getAttribute("id");
      if (submissionIdFromSession != null) {
        var submissionMaybe = this.submissionRepositoryService.findById(submissionIdFromSession);
        if (submissionMaybe.isPresent()) {
          var submission = submissionMaybe.get();
          var submissionFlow = submission.getFlow();

          if (!Objects.equals(urlFlow, submissionFlow)) {
            log.info("sessionId: %s, submissionFlow: %s, urlFlow: %s, invalidating session".formatted(
              request.getSession(false).getId(),
              submissionFlow,
              urlFlow));
            invalidateSession(request);
          }
        }
      }
      return true;
    } catch (IllegalStateException e) {
      return true;
    }
  }

  private void invalidateSession(HttpServletRequest request) {
    request.getSession(false).invalidate();
    request.getSession(true);
  }
}
