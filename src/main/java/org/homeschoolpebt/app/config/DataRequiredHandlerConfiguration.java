package org.homeschoolpebt.app.config;

import formflow.library.data.SubmissionRepositoryService;
import org.homeschoolpebt.app.submission.interceptors.DataRequiredInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class DataRequiredHandlerConfiguration implements WebMvcConfigurer {
  @Autowired
  SubmissionRepositoryService submissionRepositoryService;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new DataRequiredInterceptor(this.submissionRepositoryService)).addPathPatterns(DataRequiredInterceptor.PATH_FORMAT);
  }
}
