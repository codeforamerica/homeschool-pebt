package org.homeschoolpebt.app.cli;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!production")
public class MockSftpClientImpl implements SftpClient {

  @Override
  public void uploadFile(String zipFilename) {
    // Do nothing
    log.info("Mock uploading file " + zipFilename);
  }
}
