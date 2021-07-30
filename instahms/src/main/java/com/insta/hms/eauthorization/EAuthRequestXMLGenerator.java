/**
 *
 */

package com.insta.hms.eauthorization;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.AppInit;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;


import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * The Class EAuthRequestXMLGenerator.
 *
 * @author lakshmi
 */
public class EAuthRequestXMLGenerator {

  /**
   * The logger.
   */
  static Logger logger = LoggerFactory
      .getLogger(EAuthRequestXMLGenerator.class);

  /**
   * The preauth prescdao.
   */
  EAuthPrescriptionDAO preauthPrescdao = new EAuthPrescriptionDAO();

  /**
   * The validator.
   */
  EAuthRequestValidator validator = new EAuthRequestValidator();

  /**
   * Generate request XML.
   *
   * @param preauthPrescId       the preauth presc id
   * @param insuranceCoId        the insurance co id
   * @param requestType          the request type
   * @param testing              the testing
   * @param shafafiyaEAuthActive the shafafiya E auth active
   * @return the string
   * @throws SQLException      the SQL exception
   * @throws IOException       Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   * @throws Exception         the exception
   */
  public String generateRequestXML(int preauthPrescId, String insuranceCoId,
                                   String requestType, boolean testing, String shafafiyaEAuthActive)
      throws SQLException, IOException, TemplateException, Exception {

    BasicDynaBean preauthPrescBean = preauthPrescdao
        .getEAuthPresc(preauthPrescId, insuranceCoId);

    StringBuilder errStr = new StringBuilder(
        "Error(s) while XML data check. Prior Auth Request XML could not be generated.<br/>"
            + "Please correct (or) update the following and generate Prior Auth request again"
            + ".<br/>");

    Integer centerId = RequestContext.getCenterId();
    Integer preauthCenterId = centerId;

    if (null != preauthPrescBean.get("center_id")) { // Visit Center
      preauthCenterId = (Integer) preauthPrescBean.get("center_id");
    }

    if (null != preauthPrescBean.get("preauth_center_id")) { // Prior Auth
      // Request
      // Center
      preauthCenterId = (Integer) preauthPrescBean
          .get("preauth_center_id");
    }

    String healthAuthority = CenterMasterDAO
        .getHealthAuthorityForCenter(preauthCenterId);

    String preauthRequestId = preauthPrescBean
        .get("preauth_request_id") != null
        ? (String) preauthPrescBean.get("preauth_request_id")
        : null;
    BasicDynaBean headerbean = preauthPrescdao
        .getEAuthHeaderFields(preauthRequestId);

    String dispositionFlag = "";
    if ("HAAD".equalsIgnoreCase(healthAuthority)) { // Active Mode
      dispositionFlag = (shafafiyaEAuthActive.equals("N")) ? "PTE_SUBMIT"
          : "PRODUCTION";
    } else {
      dispositionFlag = (shafafiyaEAuthActive.equals("N")) ? "TEST"
          : "PRODUCTION";
    }

    Map headerMap = new HashMap();
    // headerbean.set("testing", testing ? "Y" : "N");
    headerbean.set("disposition_flag", dispositionFlag);
    headerbean.set("health_authority", healthAuthority);

    EAuthRequest eauthRequest = new EAuthRequest();
    Map<String, StringBuilder> errorsMap = new HashMap<>();
    validator.validateEAuthRequest(errorsMap, shafafiyaEAuthActive,
        healthAuthority, preauthPrescBean, eauthRequest);

    if (errorsMap != null && !errorsMap.isEmpty()) {
      Iterator keys = errorsMap.keySet().iterator();

      while (keys.hasNext()) {
        String key = (String) keys.next();
        StringBuilder errorString = (StringBuilder) errorsMap.get(key);
        errStr.append("<br/>" + errorString);
      }
      return errStr.toString();

    } else {

      File requestFile = File.createTempFile("tempEAuthRequestFile", "");
      OutputStream fos = new FileOutputStream(requestFile);

      headerMap = headerbean.getMap();

      Map<String,
          EAuthRequest> bodyMap = new HashMap<String, EAuthRequest>();
      bodyMap.put("eAuthRequest", eauthRequest);

      addRequestHeader(fos, headerMap);
      if ("HAAD".equals(healthAuthority)) {
        addRequestBody(fos, bodyMap);
      } else {
        addRequestBodyDha(fos, bodyMap);
      }
      addRequestFooter(fos, new HashMap());

      fos.flush();
      fos.close();

      String xmlStr = FileUtils.readFileToString(requestFile);
      logger.debug(
          "Prior Auth Request XML Content for Prior Auth Presc Id: "
              + preauthPrescId + " is ... : " + xmlStr);

      return xmlStr;
    }
  }

  /**
   * Adds the request header.
   *
   * @param stream the stream
   * @param ftlMap the ftl map
   * @throws IOException       Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   */
  public void addRequestHeader(OutputStream stream, Map ftlMap)
      throws IOException, TemplateException {
    Template template = null;
    template = AppInit.getFmConfig()
        .getTemplate("/E-Authorization/EAuthRequestHeader.ftl");
    writeToStream(template, stream, ftlMap);
  }

  /**
   * Adds the request body.
   *
   * @param stream the stream
   * @param ftlMap the ftl map
   * @throws IOException       Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   */
  public void addRequestBody(OutputStream stream, Map ftlMap)
      throws IOException, TemplateException {
    Template template = null;
    template = AppInit.getFmConfig()
        .getTemplate("/E-Authorization/EAuthRequestBody.ftl");
    writeToStream(template, stream, ftlMap);
  }

  /**
   * Adds the request body dha.
   *
   * @param stream the stream
   * @param ftlMap the ftl map
   * @throws IOException       Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   */
  public void addRequestBodyDha(OutputStream stream, Map ftlMap)
      throws IOException, TemplateException {
    Template template = null;
    template = AppInit.getFmConfig()
        .getTemplate("/E-Authorization/DhaEAuthRequestBody.ftl");
    writeToStream(template, stream, ftlMap);
  }

  /**
   * Adds the request footer.
   *
   * @param stream the stream
   * @param ftlMap the ftl map
   * @throws IOException       Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   */
  public void addRequestFooter(OutputStream stream, Map ftlMap)
      throws IOException, TemplateException {
    Template template = AppInit.getFmConfig()
        .getTemplate("/E-Authorization/EAuthRequestFooter.ftl");
    writeToStream(template, stream, ftlMap);
  }

  /**
   * Write to stream.
   *
   * @param template      the t
   * @param stream the stream
   * @param ftlMap the ftl map
   * @throws IOException       Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   */
  public void writeToStream(Template template, OutputStream stream, Map ftlMap)
      throws IOException, TemplateException {
    if (template == null) {
      return;
    }
    StringWriter stringWriter = new StringWriter();
    template.process(ftlMap, stringWriter);
    stream.write(stringWriter.toString().getBytes());
    stream.flush();
  }
}
