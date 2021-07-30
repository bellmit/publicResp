package com.insta.hms.focus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

/**
 * The Class FocusSyncTestServlet.
 *
 * @author krishna URL is /FocusSyncTestServlet
 */
public class FocusSyncTestServlet extends HttpServlet {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(FocusSyncTestServlet.class);

  /** The parse stream directly. */
  static boolean parseStreamDirectly = false;

  /**
   * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    try {
      // validate if the input is OK
      InputStream inputStream = request.getInputStream();
      // Thread.sleep(90*1000l);
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      // factory.setNamespaceAware(true);
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = null;

      if (parseStreamDirectly) {
        doc = builder.parse(inputStream);

      } else {
        // do like Focus does
        int inputData = 0;
        byte[] byData = null;
        ByteArrayOutputStream byArrayOutputStream = new ByteArrayOutputStream();
        while ((inputData = inputStream.read()) != -1) {
          byArrayOutputStream.write(inputData);
        }
        byArrayOutputStream.flush();
        byData = byArrayOutputStream.toByteArray();
        byArrayOutputStream.close();

        doc = builder.parse(new ByteArrayInputStream(byData));
      }

      XPathFactory xfact = XPathFactory.newInstance();
      XPath xpath = xfact.newXPath();

      XPathExpression expr = xpath.compile("//ALLLEDGERENTRIES.LIST");
      NodeList entries = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

      for (int i = 0; i < entries.getLength(); i++) {
        Node entry = entries.item(i);
        log.info(entry.getTextContent());
      }

    } catch (Exception exception) {
      log.error("exception", exception);
      response.setContentType("text/html");

      try (OutputStream os = response.getOutputStream()) {
        os.write("Status=0 VoucherNo=123 Msg=xml parsing.".getBytes());
        os.write(exception.toString().getBytes());
      } catch (IOException ioe) {
        log.error("", ioe);
      }

      return;
    }

    /*
     * Failed_To_Initialise= 0; Authentication_Failed= -1; DateTime_Restriction_Failed= -2;
     * Restricted_Multiple_Logins= -3; Bloacked_User= -4; Failed_To_Load_Rights= -5;
     * Not_Registered_Inf= -6; Users_Exceeded= -10; User_Already_Logged_In= -11;
     * Ldap_Authentication_Failed= -20;
     * 
     * to test all the above errors give the above error codes against the Status in the following
     * message.
     */

    response.setContentType("text/html");
    try (OutputStream os = response.getOutputStream()) {
      log.info("Request Received successfully.............");
      os.write("Status=1 VoucherNo=123 Msg=Ldap Authentication Failed.".getBytes());
    } catch (IOException ioe) {
      log.error("", ioe);
    }
  }

  /**
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   * javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    response.setContentType("text/plain");
    try (OutputStream os = response.getOutputStream()) {
      os.write("This URL does not support GET method".getBytes());
    } catch (IOException ioe) {
      log.error("", ioe);
    }
  }

}
