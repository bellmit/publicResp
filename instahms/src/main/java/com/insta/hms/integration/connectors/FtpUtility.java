package com.insta.hms.integration.connectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.SocketException;

/**
 * The Class FtpUtility.
 */
public class FtpUtility {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(FtpUtility.class);

  /**
   * Copy file to FTP server.
   *
   * @param fileToWrite the file to write
   * @param fileName the file name
   * @param ftpPath the ftp path
   * @param username the username
   * @param password the password
   * @return true, if successful
   * @throws Exception the exception
   */
  public static boolean copyFileToFTPServer(File fileToWrite, final String fileName,
      final String ftpPath, final String username, final String password) throws Exception {
    boolean isSuccessfulWrite = false;
    FTPClient client = new FTPClient();
    String[] ftpAddress = StringUtils.split(ftpPath, "/", 2);
    String ipAddress = null;
    String directoryPath = null;
    if (!ArrayUtils.isEmpty(ftpAddress)) {
      if (ftpAddress.length == 2) {
        directoryPath = StringUtils.trimToNull(ftpAddress[1]);
      }
      ipAddress = StringUtils.trimToNull(ftpAddress[0]);
    }
    try (FileInputStream fileInputStream = new FileInputStream(fileToWrite);) {
      if (StringUtils.isNotEmpty(ipAddress)) {
        connectAndLoginToFtpServer(username, password, client, ipAddress);
        client.enterLocalPassiveMode();
        isSuccessfulWrite = client.storeFile(StringUtils.trimToEmpty(directoryPath) + fileName,
            fileInputStream);
        logger.debug("Store File Reply code is " + client.getReplyCode());
        isSuccessfulWrite &= client.logout();
      } else {
        logger.error("Unable to send file to FTP Server. IP Address is Empty");
      }
    } catch (Exception exception) {
      isSuccessfulWrite = false;
      throw exception;
    } finally {
      try {
        client.disconnect();
      } catch (Exception exception) {
        logger.error("Unable to free-up resources after FTP File Upload", exception);
      }
    }
    return isSuccessfulWrite;
  }

  /**
   * Connect and login to ftp server.
   *
   * @param username the username
   * @param password the password
   * @param client the client
   * @param ipAddress the ip address
   * @throws SocketException the socket exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws Exception the exception
   */
  private static void connectAndLoginToFtpServer(final String username, final String password,
      FTPClient client, String ipAddress) throws SocketException, IOException, Exception {
    client.connect(ipAddress);
    client.login(username, password);
    int replyCode = client.getReplyCode();
    if (!FTPReply.isPositiveCompletion(replyCode)) {
      logger.error("FTP Server refused connection with reply code " + replyCode);
      throw new Exception(
          "Unable to connect to FTP Server, Reply code from server is " + replyCode);
    }
  }
}
