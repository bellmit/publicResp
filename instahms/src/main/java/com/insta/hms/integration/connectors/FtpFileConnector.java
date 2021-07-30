package com.insta.hms.integration.connectors;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.ftp.FtpFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;

public class FtpFileConnector extends FileConnector {

  static final Logger log = LoggerFactory.getLogger(FtpFileConnector.class);
  private String url = null;
  private StandardFileSystemManager manager = null;

  @Override
  public boolean open(String url) throws IOException {
    this.url = url;
    manager = new StandardFileSystemManager();
    try {
      manager.init();
    } catch (FileSystemException ex) {
      log.error(ex.getMessage());
      manager = null;
      return false;
    }
    return true;
  }

  @Override
  public void close() throws IOException {
    if (null != manager) {
      manager.close();
    }
  }

  @Override
  public OutputStream getOutputStream() {
    OutputStream os = null;
    try {
      FileObject remoteFileObj = manager.resolveFile(url, createDefaultOptions(url));
      // sftp does not support append mode.
      os = remoteFileObj.getContent().getOutputStream(null != url && url.startsWith("ftp"));
    } catch (FileSystemException ex) {
      log.error(ex.getMessage());
    }
    return os;
  }

  private FileSystemOptions createDefaultOptions(String url) throws FileSystemException {
    FileSystemOptions opts = new FileSystemOptions();
    getFileSystemOptions(opts, url);
    return opts;
  }

  private void getFileSystemOptions(FileSystemOptions opts, String uri) throws FileSystemException {
    if (null != uri && uri.trim().startsWith("sftp")) {
      // SSH Key checking
      SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts, "no");
      // user home is the ftp root.
      SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, true);
      SftpFileSystemConfigBuilder.getInstance().setTimeout(opts, 20000);
    } else if (null != uri && uri.trim().startsWith("ftp")) {
      // user home is the ftp root.
      FtpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, true);
      // no timeout on the control connection
      FtpFileSystemConfigBuilder.getInstance().setConnectTimeout(opts, null);            
      FtpFileSystemConfigBuilder.getInstance().setDataTimeout(opts, 20000);
      FtpFileSystemConfigBuilder.getInstance().setPassiveMode(opts, true);
    }
    return;
  }

}