/**
 *
 */

package com.insta.hms.patientheader;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PhoneNumberUtil;
import com.insta.hms.common.TokenCommandBase;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.PatientHeaderPref.PatientHeaderPrefDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class PatientHeaderAction.
 *
 * @author krishna.t
 */
public class PatientHeaderAction extends DispatchAction {

  /** The dao. */
  PatientHeaderPrefDAO dao = new PatientHeaderPrefDAO();

  /** The pat dao. */
  PatientDetailsDAO patDao = new PatientDetailsDAO();

  /**
   * Gets the header details.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the header details
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ServletException
   *           the servlet exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getHeaderDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException,
      ServletException {
    response.setContentType("text/plain");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

    Boolean showClinical = new Boolean(request.getParameter("showClinicalInfo"));
    String visitType = request.getParameter("visit_type");
    String patientId = request.getParameter("patientId");
    Map map = new HashMap();
    map.put("patientDetails",
        ConversionUtils.copyListDynaBeansToMap(dao.getFields("P", showClinical, visitType)));
    map.put("visitDetails",
        ConversionUtils.copyListDynaBeansToMap(dao.getFields("V", showClinical, visitType)));
    map.put("token", request.getAttribute(TokenCommandBase.TRANSACTION_TOKEN_KEY));
    map.put("patientJSON", VisitDetailsDAO.getPatientHeaderDetailsMap(patientId));

    map.put("preferredLanguages", RegistrationPreferencesDAO.getPreferredLanguages(
        (String) request.getAttribute("language")));

    JSONSerializer js = new JSONSerializer().exclude("class");
    js.deepSerialize(map, response.getWriter());
    response.flushBuffer();
    return null;
  }

  /**
   * Update.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws SQLException
   *           the SQL exception
   * @throws ServletException
   *           the servlet exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, ServletException, IOException {

    String mrNo = request.getParameter("eph_mr_no");
    String phoneNo = request.getParameter("eph_patient_phone");
    String phoneNoCountryCode = request.getParameter("eph_patient_phone_country_code");
    List<String> splitCountryCodeAndText = PhoneNumberUtil.getCountryCodeAndNationalPart(
        phoneNo, null);
    if (splitCountryCodeAndText != null && !splitCountryCodeAndText.isEmpty()
        && !splitCountryCodeAndText.get(0).isEmpty() && phoneNo != null && !phoneNo.equals("")) {
      phoneNo = "+" + splitCountryCodeAndText.get(0) + splitCountryCodeAndText.get(1);
      phoneNoCountryCode = "+" + splitCountryCodeAndText.get(0);
    }
    String emailId = request.getParameter("eph_email_id");
    BasicDynaBean bean = patDao.getBean();
    bean.set("patient_phone", phoneNo);
    bean.set("patient_phone_country_code", phoneNoCountryCode);
    bean.set("email_id", emailId);

    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);
    boolean flag = true;
    try {
      flag = patDao.update(con, bean.getMap(), "mr_no", mrNo) > 0;
    } finally {
      DataBaseUtil.commitClose(con, flag);
    }
    String langCode = request.getParameter("eph_contact_pref_lang_code");
    PatientDetailsDAO.updateContactPreference(mrNo, langCode);

    JSONSerializer js = new JSONSerializer().exclude("class");
    response.setContentType("text/plain");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

    js.deepSerialize(flag, response.getWriter());
    response.flushBuffer();
    return null;
  }

}
