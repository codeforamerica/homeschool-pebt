package org.homeschoolpebt.app;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.GitProperties;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.HashMap;

/**
 * A controller to render static pages that are not in any flow.
 */
@Controller
public class StaticPageController {

  @Autowired
  GitProperties gitProperties;

  /**
   * Renders the website index page.
   *
   * @param request The current HTTP request, not null
   * @return the static page template
   */
  @GetMapping("/")
  ModelAndView getIndex(HttpServletRequest request, @Value("${form-flow.applications-disabled}") String applicationsDisabled) {
    // Reset session if you visit home
    HttpSession httpSession = request.getSession(false);
    if (httpSession != null) {
      httpSession.invalidate();
    }
    httpSession = request.getSession(true);

    // provide a model so that we can pass the git commit hash to the footer, via the index page.
    HashMap<String, Object> model = new HashMap<>();
    model.put("codeCommitHashShort", gitProperties.getShortCommitId());
    model.put("codeCommitDateTime", gitProperties.getCommitTime());
    model.put("applicationsDisabled", applicationsDisabled.equals("true"));

    return new ModelAndView("index", model);
  }

  @GetMapping("/docs")
  RedirectView getDocs() {
    return new RedirectView("/flow/docUpload/addDocumentsSignpost");
  }

  /**
   * Renders the website privacy policy page.
   *
   * @return the static page template
   */
  @GetMapping("/privacy")
  String getPrivacy() {
    return "privacy";
  }
}
