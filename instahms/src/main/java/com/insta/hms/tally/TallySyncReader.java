package com.insta.hms.tally;

import com.insta.hms.focus.FocusSyncReader.ErrorCodes;

import org.w3c.dom.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * The Class TallySyncReader.
 *
 * @author krishna
 */
public class TallySyncReader {

  /** The s server url. */
  private String strFilePath;

  /** The str error path. */
  private String strErrorPath;

  /** The str success path. */
  private String strSuccessPath;

  /** The str server url. */
  private String strServerUrl;

  /** The cl error stream. */
  FileOutputStream clErrorStream = null;

  /** The is test case. */
  boolean isTestCase = false;

  /** The company not opened. */
  private boolean companyNotOpened = true;

  /*
   * when isTestCase is enabled should pass the sServername as like http://localhost:8080/instahms
   * including the context path.
   */

  /**
   * Instantiates a new tally sync reader.
   *
   * @param strFilePath
   *          the s file path
   * @param strSuccessPath
   *          the s success path
   * @param strErrorPath
   *          the s error path
   * @param strServerUrl
   *          the s server url
   * @param isTestCase
   *          the is test case
   */
  public TallySyncReader(String strFilePath, String strSuccessPath, String strErrorPath,
      String strServerUrl, boolean isTestCase) {
    this.strFilePath = strFilePath;
    this.strSuccessPath = strSuccessPath;
    this.strErrorPath = strErrorPath;
    this.strServerUrl = strServerUrl;
    this.isTestCase = isTestCase;
  }

  /**
   * The main method.
   *
   * @param stringArray
   *          the arguments
   */
  public static void main(String[] stringArray) {
    try {
      if (stringArray.length < 5) {
        System.out.println("Invalid number of arguments syntax is  "
            + " java com.insta.hms.focus.FocusSyncReader <Input path> <Done path> <Error path> "
            + " <Server url> " + " <isTestCase> [<PROXY/VPN IP> <PROXY/VPN PORT>]");
        System.out.println("<Input path> = complete path to the input folder");
        System.out.println("<Done path> = complete path to the done folder");
        System.out.println("<Error path> = complete path to the Error folder");
        System.out.println("<Server url> = url to the tally server ex: http://192.168.2.2:9000");
        System.out.println("<isTestCase> = true/false true will be "
            + " used to Test the FocusSync inside hms.");
        return;
      }

      if (stringArray.length > 5 && stringArray.length < 7) {
        System.getProperties().put("proxySet", "true");
        System.getProperties().put("proxyHost", stringArray[5]);
        System.getProperties().put("proxyPort", stringArray[6]);

        // if you use socks firewall then you need to use this (I haven't really test this case
        // myself yet :>) :
        System.getProperties().put("proxySet", "true");
        System.getProperties().put("socksProxyHost", stringArray[5]);
        System.getProperties().put("socksProxyPort", stringArray[6]);
      }

      boolean dirExists = true;
      File successDir = new File(stringArray[1]);
      if (!successDir.exists()) {
        dirExists = successDir.mkdirs();
      }
      if (!dirExists) {
        throw new Exception("Unable to create directory : '" + stringArray[1] + "'.");
      }

      File errorDir = new File(stringArray[2]);
      if (!errorDir.exists()) {
        dirExists = errorDir.mkdirs();
      }
      if (!dirExists) {
        throw new Exception("Unable to create directory : '" + stringArray[1] + "'.");
      }

      TallySyncReader tallySyncReader = new TallySyncReader(stringArray[0], stringArray[1],
          stringArray[2], stringArray[3], new Boolean(stringArray[4]));
      tallySyncReader.readFiles();
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  /**
   * Read files.
   */
  private void readFiles() {
    File clDir = new File(strFilePath);
    if (clDir.exists()) {
      File[] clFiles = clDir.listFiles();
      if (clFiles != null && clFiles.length > 0) {
        for (int i = 0; i < clFiles.length; i++) {
          if (clFiles[i].getName().endsWith(".xml")) {
            processXML(clFiles[i]);
          }
        }
      }
    }
  }

  /**
   * Parses the XML data and reads multiple TALLYMESSAGE tags one after another and sends to the
   * foucs server by calling the method postVoucher, in case of all the TALLYMESSAGE are sucessfully
   * posted, the moves the source xml file to done folder...otherwise to error folder
   *
   * @param clSrcFile
   *          the cl src file
   */
  private void processXML(File clSrcFile) {

    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      // factory.setNamespaceAware(true);
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.parse(clSrcFile);

    } catch (IOException io) {
      moveFile(ErrorCodes.FILE_NOT_FOUND, clSrcFile, io, "");
      return;
    } catch (Exception se) {
      moveFile(ErrorCodes.XML, clSrcFile, se, "");
      return;
    }

    String response = "";
    boolean[] isTimeout = new boolean[1];

    try {
      response = sendFile(clSrcFile, isTimeout);
    } catch (java.net.NoRouteToHostException nrth) {
      moveFile(ErrorCodes.NO_ROUTE_TO_HOST, clSrcFile, nrth, response);
      return;
    } catch (java.net.ConnectException cr) {
      moveFile(ErrorCodes.CONN_REFUSED, clSrcFile, cr, response);
      return;
    } catch (java.net.SocketException sock) {
      if (sock.getMessage().equals("Network is unreachable")) {
        moveFile(ErrorCodes.NET_UNREACHABLE, clSrcFile, sock, response);
      } else if (sock.getMessage().equals("Socket closed") && isTimeout[0]) {
        moveFile(ErrorCodes.READ_TIMEOUT, clSrcFile, sock, response);
      } else {
        moveFile(ErrorCodes.TRANSPORT, clSrcFile, sock, response);
      }
      return;
    } catch (IOException io) {
      moveFile(ErrorCodes.TRANSPORT, clSrcFile, io, response);
      return;
    } catch (Exception exception) {
      moveFile(ErrorCodes.UNKNOWN_ERROR, clSrcFile, exception, response);
      return;
    }
    if (isTestCase) {
      System.out.println(response);
    }

    int errors = 0;
    if (response.contains("Unknown Request")) {
      moveFile(ErrorCodes.INITIALIZE, clSrcFile, null, response);
    } else if (response.contains("<ERRORS>")) {
      errors = Integer.parseInt(response.substring(response.indexOf("<ERRORS>") + 8,
          response.indexOf("</ERRORS>")));
      if (errors == 0) {
        moveFile(ErrorCodes.SUCCESS, clSrcFile, null, response);
      } else {
        moveFile(ErrorCodes.VOUCHER, clSrcFile, null, response);
      }
    } else if (response.contains("Could not set &apos;SVCurrentCompany&apos;")) {
      moveFile(ErrorCodes.COMP_NOT_OPENED, clSrcFile, null, response);
      companyNotOpened = true;
    } else if (response.equals("") && companyNotOpened) {
      moveFile(ErrorCodes.COMP_NOT_OPENED, clSrcFile, null, response);
    }

  }

  /**
   * Send file.
   *
   * @param file
   *          the file
   * @param isTimeout
   *          the is timeout
   * @return the string
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws Exception
   *           the exception
   */
  public String sendFile(File file, boolean[] isTimeout) throws IOException, Exception {
    /*
     * make a connection to the tally server. use one connection to send one voucher data.
     * HttpUrlConnection can be used to send one request per connection. we cant use the same
     * connection for multiple requests.
     */
    InputStream inputStream = null;
    OutputStream outputStream = null;
    Thread thread = null;
    try {
      URL url = new URL(strServerUrl);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      // connection.setInstanceFollowRedirects(false);
      // connection.setRequestProperty("Connection", "Keep-Alive");
      connection.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
      connection.setRequestMethod("POST");
      connection.setUseCaches(false);
      connection.setDoInput(true);
      connection.setDoOutput(true);
      outputStream = connection.getOutputStream();

      FileInputStream fiStream = new FileInputStream(file);
      int size = fiStream.available();
      while (size > 0) {
        byte[] byteArray = new byte[1024];
        size = size - 1024;
        fiStream.read(byteArray);
        outputStream.write(byteArray);
      }

      // start the timout monitor thread
      thread = new Thread(new TallyInterruputThread(connection, isTimeout));
      thread.start();
      System.out.println("Got Response Code..." + connection.getResponseCode());

      inputStream = connection.getInputStream();
      byte[] byResData = new byte[inputStream.available()];
      inputStream.read(byResData);
      return new String(byResData);

    } finally {
      if (thread != null) {
        thread.interrupt(); // response got succussfully. hence interrupting the thread.
      }
      try {
        if (outputStream != null) {
          outputStream.close();
        }
        if (inputStream != null) {
          inputStream.close();
        }
      } catch (IOException io) {
        io.printStackTrace();
      }
    }
  }

  /**
   * The Class TallyInterruputThread.
   */
  class TallyInterruputThread implements Runnable {

    /** The con. */
    HttpURLConnection con = null;

    /** The is timeout. */
    boolean[] isTimeout = null;

    /**
     * Instantiates a new tally interruput thread.
     *
     * @param con
     *          the con
     * @param isTimeout
     *          the is timeout
     */
    public TallyInterruputThread(HttpURLConnection con, boolean[] isTimeout) {
      this.con = con;
      this.isTimeout = isTimeout;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    public void run() {
      try {
        if (isTestCase) {
          System.out.println("waiting for server response: for abount 90 sec...");
        }
        // wait for 90 seconds.
        Thread.sleep(90 * 1000L);
      } catch (InterruptedException ie) {
        System.out.println(ie.getMessage());
      }
      con.disconnect();
      isTimeout[0] = true;
    }
  }

  /**
   * Clean up error files.
   *
   * @param file
   *          the file
   */
  /*
   * which deletes the any error files generated for the inputted file in the last interval in all
   * the mentioned directories. input/done/error
   */
  private void cleanUpErrorFiles(File file) {
    String[] folder = { strFilePath, strErrorPath, strSuccessPath };
    String fileName = file.getName();

    for (int j = 0; j < folder.length; j++) {
      File dir = new File(folder[j]);
      File[] files = dir.listFiles();
      for (int i = 0; i < files.length; i++) {
        String fileNameInDir = files[i].getName();
        if (fileNameInDir.startsWith(fileName + "_")) {
          files[i].delete();
        }
      }
    }
  }

  /**
   * Move file.
   *
   * @param errorCode
   *          the error code
   * @param file
   *          the file
   * @param exception
   *          the e
   * @param extraInfo
   *          the extra info
   */
  private void moveFile(ErrorCodes errorCode, File file, Exception exception, String extraInfo) {
    FileOutputStream fos = null;
    String path = strFilePath;
    if (errorCode.getFolder().equals("done")) {
      path = strSuccessPath;
    } else if (errorCode.getFolder().equals("error")) {
      path = strErrorPath;
    } else if (errorCode.getFolder().equals("input")) {
      path = strFilePath;
    }
    try {
      cleanUpErrorFiles(file);

      StringBuilder message = new StringBuilder("\n");
      SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
      message
          .append(simpleDateFormat.format(new java.sql.Timestamp(new java.util.Date().getTime())));
      message.append("Status Code: " + errorCode.getErrorCode() + " Msg: " + errorCode.getMsg()
          + "\n");

      if (extraInfo != null && !extraInfo.equals("")) {
        message.append(extraInfo + "\n");
      }

      if (file.renameTo(new File(path + File.separator + file.getName()))) {
        // file moved successfully.
      } else {
        message.append("\n");
        message.append("Failed to move file : " + file.getName() + " to '" + path + "' directory");
      }

      fos = new FileOutputStream(new File(path + File.separator + file.getName() + "_"
          + errorCode.getErrorCode()), true);

      if (exception != null) {
        message.append("....Details ....\n");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        exception.printStackTrace(pw);
        pw.flush();
        sw.flush();

        message.append(sw.toString());
        message.append("--------------------------------------------------------------------\n");
      }

      fos.write(message.toString().getBytes());
    } catch (IOException io) {
      io.printStackTrace();
    } finally {
      try {
        if (fos != null) {
          fos.close();
        }
      } catch (IOException io) {
        io.printStackTrace();
      }
    }
  }

}
