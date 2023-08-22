package org.homeschoolpebt.app.config;

import org.homeschoolpebt.app.interceptor.DisabledApplicationsInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class DisabledApplicationsConfiguration implements WebMvcConfigurer {
  final String applicationsDisabled;

  public DisabledApplicationsConfiguration(@Value("${form-flow.applications-disabled}") String applicationsDisabled) {
    this.applicationsDisabled = applicationsDisabled;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    if (this.applicationsDisabled.equals("true")) {
      registry.addInterceptor(new DisabledApplicationsInterceptor()).addPathPatterns(DisabledApplicationsInterceptor.PATH_FORMAT);
    }
  }
}
