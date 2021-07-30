package com.insta.instaapi.patient.preferences;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.instaapi.common.JsonProcessor;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/** PreferencesAction class. */
public class PreferencesAction extends DispatchAction {

  static Logger logger = LoggerFactory.getLogger(PreferencesAction.class);

  /**
   * Get FAQS.
   *
   * @param mapping mapping parameter
   * @param form form paramter
   * @param request request object
   * @param response response object
   * @return returns action forward
   * @throws IOException Signals that an I/O exception has occurred
   * @throws ServletException may throw Servlet Exception
   * @throws SQLException may throw SQL Exception
   */
  public ActionForward getFaqs(
      ActionMapping mapping,
      ActionForm form,
      HttpServletRequest request,
      HttpServletResponse response)
      throws IOException, ServletException, SQLException {

    Map<String, Object> faqsDataMap = new HashMap<String, Object>();
    HttpSession session = request.getSession(false);
    String sesHospitalId = (String) session.getAttribute("sesHospitalId");
    String successMsg = "";

    if (sesHospitalId == null || "".equals(sesHospitalId)) {
      successMsg = "Session is expired, please login again";
      logger.info("Session is expired, please login again");
      faqsDataMap.put("return_code", "1010");
      faqsDataMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

    } else {

      logger.info(
          "getting connection object in session object ----"
              + (String) session.getAttribute("sesHospitalId"));
      List<BasicDynaBean> faqData =
          new GenericDAO("customer_preferences")
              .listAll(null, Arrays.asList(new String[] {"faq"}), null, null);
      faqsDataMap.put("faq", ConversionUtils.listBeanToListMap(faqData));
      logger.info("getting all faq  data....");
      response.setStatus(HttpServletResponse.SC_OK);
      faqsDataMap.put("return_code", "2001");
      faqsDataMap.put("return_message", "Success");
    }
    JSONSerializer js = JsonProcessor.getJSONParser();
    response.setContentType("application/json");
    response.setHeader("Cache-Control", "no-cache");
    // response.setHeader("Access-Control-Allow-Origin", "*");
    response.getWriter().write(js.deepSerialize(faqsDataMap));
    response.flushBuffer();
    return null;
  }

  /**
   * Get Terms and Conditions.
   *
   * @param mapping mapping parameter
   * @param form form parqamter
   * @param request request object
   * @param response response object
   * @return returns action forward
   * @throws IOException Signals that an I/O exception has occurred
   * @throws ServletException may throw Servlet Exception
   * @throws SQLException may throw SQL Exception
   */
  public ActionForward getTermsAndConditions(
      ActionMapping mapping,
      ActionForm form,
      HttpServletRequest request,
      HttpServletResponse response)
      throws IOException, ServletException, SQLException {

    Map<String, Object> termsAndConditionsDataMap = new HashMap<String, Object>();
    HttpSession session = request.getSession(false);
    String sesHospitalId = (String) session.getAttribute("sesHospitalId");
    String successMsg = "";

    if (sesHospitalId == null || "".equals(sesHospitalId)) {
      successMsg = "Session is expired, please login again";
      logger.info("Session is expired, please login again");
      termsAndConditionsDataMap.put("return_code", "1010");
      termsAndConditionsDataMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

    } else {

      logger.info(
          "getting connection object in session object ----"
              + (String) session.getAttribute("sesHospitalId"));
      List<BasicDynaBean> termsAndConditionsData =
          new GenericDAO("customer_preferences")
              .listAll(null, Arrays.asList(new String[] {"t_and_c"}), null, null);
      termsAndConditionsDataMap.put(
          "TermsAndConditions", ConversionUtils.listBeanToListMap(termsAndConditionsData));
      logger.info("getting all TermsAndConditions  data....");
      response.setStatus(HttpServletResponse.SC_OK);
      termsAndConditionsDataMap.put("return_code", "2001");
      termsAndConditionsDataMap.put("return_message", "Success");
    }
    JSONSerializer js = JsonProcessor.getJSONParser();
    response.setContentType("application/json");
    response.setHeader("Cache-Control", "no-cache");
    // response.setHeader("Access-Control-Allow-Origin", "*");
    response.getWriter().write(js.deepSerialize(termsAndConditionsDataMap));
    response.flushBuffer();
    return null;
  }

  /**
   * Get Privacy Statements.
   *
   * @param mapping mapping paramter
   * @param form form parameter
   * @param request request object
   * @param response response object
   * @return returns action forward
   * @throws IOException Signals that an I/O exception has occurred
   * @throws ServletException may throw Servlet Exception
   * @throws SQLException may throw SQL Exception
   */
  public ActionForward getPrivacyStatements(
      ActionMapping mapping,
      ActionForm form,
      HttpServletRequest request,
      HttpServletResponse response)
      throws IOException, ServletException, SQLException {

    Map<String, Object> privacyStatementsDataMap = new HashMap<String, Object>();
    HttpSession session = request.getSession(false);
    String sesHospitalId = (String) session.getAttribute("sesHospitalId");
    String successMsg = "";

    if (sesHospitalId == null || "".equals(sesHospitalId)) {
      successMsg = "Session is expired, please login again";
      logger.info("Session is expired, please login again");
      privacyStatementsDataMap.put("return_code", "1010");
      privacyStatementsDataMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

    } else {

      logger.info(
          "getting connection object in session object ----"
              + (String) session.getAttribute("sesHospitalId"));
      List<BasicDynaBean> privacyStatementsData =
          new GenericDAO("customer_preferences")
              .listAll(null, Arrays.asList(new String[] {"privacy_statements"}), null, null);
      privacyStatementsDataMap.put(
          "privacyStatements", ConversionUtils.listBeanToListMap(privacyStatementsData));
      logger.info("getting all privacyStatements  data....");
      response.setStatus(HttpServletResponse.SC_OK);
      privacyStatementsDataMap.put("return_code", "2001");
      privacyStatementsDataMap.put("return_message", "Success");
    }
    JSONSerializer js = JsonProcessor.getJSONParser();
    response.setContentType("application/json");
    response.setHeader("Cache-Control", "no-cache");
    // response.setHeader("Access-Control-Allow-Origin", "*");
    response.getWriter().write(js.deepSerialize(privacyStatementsDataMap));
    response.flushBuffer();
    return null;
  }

  /**
   * Get Logo.
   *
   * @param mapping mapping paramter
   * @param form form parameter
   * @param request request object
   * @param response response object
   * @return returns action forward
   * @throws IOException Signals that an I/O exception has occurred
   * @throws ServletException may throw Servlet Exception
   * @throws SQLException may throw SQL Exception
   */
  public ActionForward getLogo(
      ActionMapping mapping,
      ActionForm form,
      HttpServletRequest request,
      HttpServletResponse response)
      throws IOException, ServletException, SQLException {

    Map<String, Object> logoDataMap = new HashMap<String, Object>();
    HttpSession session = request.getSession(false);
    String sesHospitalId = (String) session.getAttribute("sesHospitalId");
    String successMsg = "";
    JSONSerializer js = JsonProcessor.getJSONParser();
    if (sesHospitalId == null || "".equals(sesHospitalId)) {
      successMsg = "Session is expired, please login again";
      logger.info("Session is expired, please login again");
      logoDataMap.put("return_code", "1001");
      logoDataMap.put("return_message", successMsg);
      response.setContentType("application/json");
      response.setHeader("Cache-Control", "no-cache");
      // response.setHeader("Access-Control-Allow-Origin", "*");
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(logoDataMap));
      response.flushBuffer();
      return null;
    } else {

      logger.info(
          "getting connection object in session object ----"
              + (String) session.getAttribute("sesHospitalId"));
      try {
        OutputStream os = response.getOutputStream();
        response.setContentType("image/gif");
        InputStream logo = PrintConfigurationsDAO.getLogo(0);
        byte[] bytes = new byte[4096];
        int len = 0;
        while ((len = logo.read(bytes)) > 0) {
          os.write(bytes, 0, len);
        }

        os.flush();
        logo.close();
        response.setStatus(HttpServletResponse.SC_OK);
        logoDataMap.put("return_code", "2001");
        logoDataMap.put("return_message", "Success");
        return null;
      } catch (Exception exception) {
        successMsg = "Not able to perform the operation";
        logger.info("Not able to perform the operation");
        logoDataMap.put("return_code", "1002");
        logoDataMap.put("return_message", successMsg);
        response.setContentType("application/json");
        response.setHeader("Cache-Control", "no-cache");
        // response.setHeader("Access-Control-Allow-Origin", "*");
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().write(js.deepSerialize(logoDataMap));
        response.flushBuffer();
        return null;
      }
    }
  }
}
