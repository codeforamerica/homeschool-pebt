package org.homeschoolpebt.app.inputs;

import formflow.library.data.FlowInputs;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.web.multipart.MultipartFile;

@Data
@EqualsAndHashCode(callSuper = true)
public class DocUpload extends FlowInputs {
  @NotBlank(message = "{personal-info.provide-first-name}")
  private String firstName;
  @NotBlank(message = "{personal-info.provide-last-name}")
  private String lastName;
  @NotBlank(message = "{validation.application-number-presence}")
  @Pattern(regexp = "\\d{7,9}|^$", message = "{validation.application-number-format}")
  private String applicationNumber;

  MultipartFile docUpload;
}
