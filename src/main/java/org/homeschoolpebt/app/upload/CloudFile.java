package org.homeschoolpebt.app.upload;

import java.io.File;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CloudFile {

  Long filesize;
  File file;
}
