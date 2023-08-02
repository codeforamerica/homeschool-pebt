package org.homeschoolpebt.app.config;

import formflow.library.data.SubmissionRepositoryService;
import org.homeschoolpebt.app.interceptor.FlowSwitchInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FlowSwitchHandlerConfiguration implements WebMvcConfigurer {
  @Autowired
  SubmissionRepositoryService submissionRepositoryService;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new FlowSwitchInterceptor(this.submissionRepositoryService)).addPathPatterns(FlowSwitchInterceptor.PATH_FORMAT);
  }
}
