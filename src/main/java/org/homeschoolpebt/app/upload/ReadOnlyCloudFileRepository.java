package org.homeschoolpebt.app.upload;

public interface ReadOnlyCloudFileRepository {

  CloudFile download(String filePath);
}
