package com.insta.hms.focus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
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
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class FocusSyncReader.
 *
 * @author krishna
 */
public class FocusSyncReader {

  static final Logger logger = LoggerFactory.getLogger(FocusSyncReader.class);

  /** The Constant CONTROLLER_NAME. */
  static final String CONTROLLER_NAME = "CLExternalHealthCtl";

  /** The s server url. */
  private String strFilePath;
  private String strErrorPath;
  private String strSuccessPath;
  private String strServerUrl;

  /** The cl error stream. */
  FileOutputStream clErrorStream = null;

  /** The is test case. */
  boolean isTestCase = false;

  /** The Constant NO_STATUS. */
  static final short NO_STATUS = -222;

  /**
   * The Enum ErrorCodes.
   */
  public enum ErrorCodes {

    /** The success. */
    // error codes for which file will move to done folder
    SUCCESS(1, "Voucher successfully posted.", "done"),

    /** The initialize. */
    // error codes for which file need not move to the error/done folder, file can be retried.
    INITIALIZE(0, "Failed to initialize.", "input"),

    /** The authentication. */
    AUTHENTICATION(-1, "Authentication Failed.", "input"), /** The datetime restriction. */
    DATETIME_RESTRICTION(-2, "DateTime Restriction Failed.", "input"),
    /** The multiple logins. */
    MULTIPLE_LOGINS(-3, "Restricted Multiple Logins.", "input"),
    /** The blocked user. */
    BLOCKED_USER(-4, "Blocked User.", "input"),
    /** The load rights. */
    LOAD_RIGHTS(-5, "Failed To Load Rights.", "input"),
    /** The registered inf. */
    REGISTERED_INF(-6, "Not Registered Inf.", "input"),
    /** The users exceeded. */
    USERS_EXCEEDED(-10, "Users Exceeded.", "input"),
    /** The already logged in. */
    ALREADY_LOGGED_IN(-11, "User Already Logged In.", "input"),
    /** The ldap authentication. */
    LDAP_AUTHENTICATION(-20, "Ldap Authentication Failed.", "input"),

    /** The transport. */
    TRANSPORT(100, "Unknown Transport Error, see details.", "input"), /** The no route to host. */
    NO_ROUTE_TO_HOST(101, "No route to host", "input"),
    /** The conn refused. */
    CONN_REFUSED(102, "Connection Refused", "input"),
    /** The net unreachable. */
    NET_UNREACHABLE(103, "Network unreachable", "input"),
    /** The read timeout. */
    READ_TIMEOUT(104, "Read timed out after 90 seconds", "input"),
    /** The url not found. */
    URL_NOT_FOUND(105, "URL Not found", "input"),
    /** The comp not opened. */
    COMP_NOT_OPENED(106, "Company not opened", "input"),

    // error codes for which file will move to error folder, some correction required at the focus
    // server
    /** The voucher. */
    // or at the insta
    VOUCHER(300, "Voucher Error ex: ledger not found etc.,", "error"),
    /** The xml. */
    XML(400, "Failed to parse xml. ex: file not found or xml not well formed etc.,", "error"),

    /** The file not found. */
    // file itself not found
    FILE_NOT_FOUND(600, "File not found.", ""),
    /** The unknown error. */
    UNKNOWN_ERROR(700, "Unknown Error.", "error"),
    /** The empty xml. */
    EMPTY_XML(800, "No Vouchers Found.", "done");

    /** The error code. */
    int errorCode;

    /** The msg. */
    String msg;

    /** The folder. */
    String folder;

    /**
     * Instantiates a new error codes.
     *
     * @param errorCode
     *          the error code
     * @param msg
     *          the msg
     * @param folder
     *          the folder
     */
    private ErrorCodes(int errorCode, String msg, String folder) {
      this.errorCode = errorCode;
      this.msg = msg;
      this.folder = folder;
    }

    /**
     * Gets the error code.
     *
     * @return the error code
     */
    public int getErrorCode() {
      return this.errorCode;
    }

    /**
     * Gets the msg.
     *
     * @return the msg
     */
    public String getMsg() {
      return this.msg;
    }

    /**
     * Gets the folder.
     *
     * @return the folder
     */
    public String getFolder() {
      return this.folder;
    }
  }

  /*
   * when isTestCase is enabled should pass the sServername as like http://localhost:8080/instahms
   * including the context path.
   */

  /**
   * Instantiates a new focus sync reader.
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

  public FocusSyncReader(String strFilePath, String strSuccessPath, String strErrorPath,
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
            + "java com.insta.hms.focus.FocusSyncReader <Input path> "
            + "<Done path> <Error path> <Server url> "
            + "<isTestCase> [<PROXY/VPN IP> <PROXY/VPN PORT>]");
        System.out.println("<Input path> = complete path to the input folder");
        System.out.println("<Done path> = complete path to the done folder");
        System.out.println("<Error path> = complete path to the Error folder");
        System.out.println("<Server url> = url to the focus server ex: http://192.168.2.2:8080");
        System.out.println("<isTestCase> = true/false true will "
            + "be used to Test the FocusSync inside hms.");
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
      FocusSyncReader focusSyncReader = new FocusSyncReader(stringArray[0], stringArray[1],
          stringArray[2], stringArray[3], new Boolean(stringArray[4]));
      focusSyncReader.readFiles();
    } catch (Exception exception) {
      logger.error("", exception);
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

  /*
   * Parses the XML data and reads multiple <TALLYMESSAGE> tags one after another and sends to the
   * foucs server by calling the method postVoucher, in case of all the <TALLYMESSAGE> are
   * sucessfully posted, the moves the source xml file to done folder...otherwise to error folder
   */

  /**
   * The process XML.
   * 
   * @param clSrcFile
   *          the cl src file
   */
  private void processXML(File clSrcFile) {

    NodeList voucherEls = null;
    Document doc = null;

    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      // factory.setNamespaceAware(true);
      DocumentBuilder builder = factory.newDocumentBuilder();
      doc = builder.parse(clSrcFile);

    } catch (IOException io) {
      moveFile(ErrorCodes.FILE_NOT_FOUND, clSrcFile, io, null, -1, 0);
      return;
    } catch (Exception se) {
      moveFile(ErrorCodes.XML, clSrcFile, se, null, -1, 0);
      return;
    }

    XPathFactory factory = XPathFactory.newInstance();
    XPath xpath = factory.newXPath();

    Object result = null;
    try {
      XPathExpression expr = xpath.compile("//VOUCHER");
      result = expr.evaluate(doc, XPathConstants.NODESET);
    } catch (XPathExpressionException xpe) {
      moveFile(ErrorCodes.XML, clSrcFile, xpe, null, -1, 0);
      return;
    }
    voucherEls = (NodeList) result;
    StringBuilder responseMessage = new StringBuilder();
    int totalVouchers = voucherEls.getLength();
    int successCount = 0;
    for (int i = 0; i < totalVouchers; i++) {
      Node voucher = voucherEls.item(i);

      NodeList childNodes = voucher.getChildNodes();
      // wrap all ledgers into the body tag for each voucher.
      Element bodyEl = doc.createElement("BODY");
      String voucherNo = "";
      for (int j = 0; j < childNodes.getLength(); j++) {
        if (childNodes.item(j).getNodeName().equals("ALLLEDGERENTRIES.LIST")) {
          bodyEl.appendChild(voucher.removeChild(childNodes.item(j)));
        }
        if (childNodes.item(j).getNodeName().equals("VOUCHERNUMBER")) {
          voucherNo = childNodes.item(j).getTextContent();
        }
      }
      voucher.appendChild(bodyEl);

      Node tallyMsgEl = voucher.getParentNode();

      DOMSource domSource = new DOMSource(tallyMsgEl);
      StringWriter writer = new StringWriter();
      StreamResult sresult = new StreamResult(writer);

      try {
        TransformerFactory tf = TransformerFactory.newInstance();
        tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "NO");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.transform(domSource, sresult);
      } catch (TransformerException te) {
        moveFile(ErrorCodes.XML, clSrcFile, te, null, totalVouchers, successCount);
        return; // no need to move the file
      }

      // send each voucher to focus server.
      responseMessage.append("Exporting Voucher No.: " + voucherNo + "....\n");
      String strResponse = "";
      boolean[] isTimeout = new boolean[1];

      try {

        strResponse += sendVoucher(writer.toString().replaceAll("\n|\r|\t", ""), isTimeout);

      } catch (java.net.NoRouteToHostException nrth) {
        moveFile(ErrorCodes.NO_ROUTE_TO_HOST, clSrcFile, nrth, responseMessage.toString(),
            totalVouchers, successCount);
        return;
      } catch (java.net.ConnectException cr) {
        moveFile(ErrorCodes.CONN_REFUSED, clSrcFile, cr, responseMessage.toString(), totalVouchers,
            successCount);
        return;
      } catch (java.net.SocketException sock) {
        if (sock.getMessage().equals("Network is unreachable")) {
          moveFile(ErrorCodes.NET_UNREACHABLE, clSrcFile, sock, responseMessage.toString(),
              totalVouchers, successCount);
        } else if (sock.getMessage().equals("Socket closed") && isTimeout[0]) {
          moveFile(ErrorCodes.READ_TIMEOUT, clSrcFile, sock, responseMessage.toString(),
              totalVouchers, successCount);
        } else {
          moveFile(ErrorCodes.TRANSPORT, clSrcFile, sock, responseMessage.toString(),
              totalVouchers, successCount);
        }
        return;
      } catch (IOException io) {
        moveFile(ErrorCodes.TRANSPORT, clSrcFile, io, responseMessage.toString(), totalVouchers,
            successCount);
        return;
      } catch (Exception exception) {
        moveFile(ErrorCodes.UNKNOWN_ERROR, clSrcFile, exception, responseMessage.toString(),
            totalVouchers, successCount);
        return;
      }

      int statusCode = FocusSyncReader.NO_STATUS;

      try {
        statusCode = Integer.parseInt(strResponse.substring(strResponse.indexOf("Status=") + 7,
            strResponse.indexOf("VoucherNo=")).trim());
        // Assumed to get the response as
        // "Status=<CODE> VoucherNo=<VOUCHER-NO> Msg=<SUCCESS or ERROR Msg>"
      } catch (Exception exception) {
        System.out.println(exception.getMessage());
      }

      responseMessage.append(strResponse + "\n\n");

      if (statusCode == 1) {
        successCount++;
        continue;
      }

      System.out.println("Voucher export failed: " + strResponse);

      for (ErrorCodes code : ErrorCodes.values()) {
        if (code.getErrorCode() == statusCode && code.getFolder().equals("input")) {
          // file needs to be retried
          moveFile(code, clSrcFile, null, strResponse, totalVouchers, successCount);
          return;
        }
      }
      // if status code is not 1 and not falling under any of the mentioned error codes,
      // we are assuming that as a voucher level errors, and continuing to send the next voucher.
    }
    if (isTestCase) {
      System.out.println("Total Vouchers in File: " + clSrcFile.getName() + "....."
          + voucherEls.getLength());
      System.out.println("Vouchers Successfully Posted..." + successCount);
    }
    if (voucherEls.getLength() == 0) {
      moveFile(ErrorCodes.EMPTY_XML, clSrcFile, null, responseMessage.toString(), totalVouchers,
          successCount);
    } else if (successCount == voucherEls.getLength()) {
      // all the vouchers exported to focus successfully.
      moveFile(ErrorCodes.SUCCESS, clSrcFile, null, responseMessage.toString(), totalVouchers,
          successCount);
    } else {
      moveFile(ErrorCodes.VOUCHER, clSrcFile, null, responseMessage.toString(), totalVouchers,
          successCount);
    }

  }

  /**
   * Send voucher.
   *
   * @param voucher
   *          the voucher
   * @param isTimeout
   *          the is timeout
   * @return the string
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws Exception
   *           the exception
   */
  public String sendVoucher(String voucher, boolean[] isTimeout) throws IOException, Exception {
    /*
     * make a connection to the focus server. use one connection to send one voucher data.
     * HttpUrlConnection can be used send one request per connection. we cant use the same
     * connection for multiple requests.
     */
    InputStream inputStream = null;
    OutputStream outputStream = null;
    Thread thread = null;
    try {
      String urlStr = strServerUrl + "/FocusOnWeb/health/" + CONTROLLER_NAME;
      if (isTestCase) {
        // sServerName should be in this form http://localhost:8080/instahms
        // and tomcat should be started before calling the FocusSyncReader.
        urlStr = strServerUrl + "/FocusSyncTestServlet";
      }
      URL url = new URL(urlStr);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      // connection.setInstanceFollowRedirects(false);
      // connection.setRequestProperty("Connection", "Keep-Alive");
      connection.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
      connection.setRequestMethod("POST");
      connection.setUseCaches(false);
      connection.setDoInput(true);
      connection.setDoOutput(true);
      outputStream = connection.getOutputStream();

      // send each voucher to focus server.
      outputStream.write(voucher.toString().getBytes());

      // start the timout monitor thread
      thread = new Thread(new InterruputThread(connection, isTimeout));
      thread.start();
      System.out.println("Got Response Code..." + connection.getResponseCode());

      inputStream = connection.getInputStream();
      byte[] byResData = new byte[inputStream.available()];
      int noBytesRead = inputStream.read(byResData);
      logger.debug("Bytes read: {}", noBytesRead);
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
        logger.error("", io);
      }
    }
  }

  /**
   * The Class InterruputThread.
   */
  class InterruputThread implements Runnable {

    /** The con. */
    HttpURLConnection con = null;

    /** The is timeout. */
    boolean[] isTimeout = null;

    /**
     * Instantiates a new interruput thread.
     *
     * @param con
     *          the con
     * @param isTimeout
     *          the is timeout
     */
    public InterruputThread(HttpURLConnection con, boolean[] isTimeout) {
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
        System.out.println("InterruptedException..." + ie.getMessage());
        Thread.currentThread().interrupt();
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
          boolean deleted = files[i].delete();
          if (deleted) {
            logger.debug(fileNameInDir + " deleted");
          }
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
   * @param totalVouchers
   *          the total vouchers
   * @param successVouchers
   *          the success vouchers
   */
  private void moveFile(ErrorCodes errorCode, File file, Exception exception, String extraInfo,
      int totalVouchers, int successVouchers) {
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
      message.append("\t");
      message.append("Total vouchers: " + totalVouchers + "; Success: " + successVouchers);
      message.append("\n");
      if (totalVouchers != 0) {
        message
            .append("Status Code: " + errorCode.errorCode + " Msg: " + errorCode.getMsg() + "\n");
      }

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
      logger.error("", io);
    } finally {
      try {
        if (fos != null) {
          fos.close();
        }
      } catch (IOException io) {
        logger.error("", io);
      }
    }
  }
}
