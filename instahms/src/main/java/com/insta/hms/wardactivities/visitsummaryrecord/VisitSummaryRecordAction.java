package com.insta.hms.wardactivities.visitsummaryrecord;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.instaforms.AbstractInstaForms;
import com.insta.hms.instaforms.IPForms;
import com.insta.hms.instaforms.PatientSectionDetailsDAO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.FormComponents.FormComponentsDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.HealthAuthorityPreferences.HealthAuthorityPreferencesDAO;
import com.insta.hms.master.ImageMarkers.ImageMarkerDAO;
import com.insta.hms.master.PhraseSuggestionsMaster.PhraseSuggestionsMasterDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.RegularExpression.RegularExpressionDAO;
import com.insta.hms.master.Sections.SectionsDAO;
import com.insta.hms.master.outpatient.SystemGeneratedSectionsDAO;
import com.insta.hms.master.sectionrolerights.SectionRoleRightsDAO;
import com.insta.hms.medicalrecorddepartment.MRDDiagnosisDAO;
import com.insta.hms.outpatient.SecondaryComplaintDAO;

import com.lowagie.text.DocumentException;

import flexjson.JSONSerializer;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

/**
 * The Class VisitSummaryRecordAction.
 */
public class VisitSummaryRecordAction extends DispatchAction {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(VisitSummaryRecordAction.class);

  /** The patient org DAO. */
  GenericDAO patientOrgDAO = new GenericDAO("patient_registration");
  
  /** The scomplaint dao. */
  SecondaryComplaintDAO scomplaintDao = new SecondaryComplaintDAO();
  
  /** The vsr DAO. */
  VisitSummaryRecordDAO vsrDAO = new VisitSummaryRecordDAO();
  
  /** The visit DAO. */
  VisitDetailsDAO visitDAO = new VisitDetailsDAO();
  
  /** The form comp dao. */
  FormComponentsDAO formCompDao = new FormComponentsDAO();
  
  /** The phy form desc. */
  SectionsDAO phyFormDesc = new SectionsDAO();
  
  /** The ip record DAO. */
  GenericDAO ipRecordDAO = new GenericDAO("ip_record");

  /**
   * List.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws Exception the exception
   */
  public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, IOException, Exception {

    String patientId = request.getParameter("patient_id");
    BasicDynaBean consBean = (BasicDynaBean) visitDAO.getAllVisitsAndDoctorsByPatientId(patientId);
    request.setAttribute("antenatal_bean_doctor_id", consBean.get("doctor"));
    request.setAttribute("antenatal_bean_doctor_name", consBean.get("doctor_name"));

    String userName = (String) request.getSession(false).getAttribute("userid");
    Integer patientCenterId = null;
    String phraseDept = null;
    if (patientId != null && !patientId.equals("")) {
      VisitDetailsDAO regDao = new VisitDetailsDAO();
      BasicDynaBean bean = regDao.findByKey("patient_id", patientId);
      if (bean == null) {
        // user entered visit id is not a valid patient id.
        FlashScope flash = FlashScope.getScope(request);
        flash.put("error", "No Patient with Id:" + patientId);
        ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
        return redirect;
      } else {
        patientCenterId = (Integer) bean.get("center_id");

        GenericDAO userDAO = new GenericDAO("u_user");
        BasicDynaBean userbean = userDAO.findByKey("emp_username", userName);
        String doctorDept = null;
        if (userbean != null) {
          request.setAttribute("isSharedLogIn", userbean.get("is_shared_login"));
          request.setAttribute("roleId", userbean.get("role_id"));
          String doctorId = (String) userbean.get("doctor_id");
          if (doctorId != null) {
            GenericDAO doctorDAO = new GenericDAO("doctors");
            BasicDynaBean doctorbean = doctorDAO.findByKey("doctor_id", doctorId);
            if (doctorbean != null) {
              doctorDept = (String) doctorbean.get("dept_id");
            }
          }
        }

        if (doctorDept == null || doctorDept.equals("")) {
          phraseDept = (String) bean.get("dept_name");
        } else {
          phraseDept = doctorDept;
        }
      }
    }
    JSONSerializer js = new JSONSerializer().exclude("class");

    AbstractInstaForms formDAO = new IPForms();
    BasicDynaBean ipform = formDAO.getComponents(request.getParameterMap());
    request.setAttribute("form", ipform);
    request.setAttribute("insta_form_json", js.serialize(ipform.getMap()));
    request.setAttribute("group_patient_sections",
        new FormComponentsDAO().findByKey("id", ipform.get("form_id"))
            .get("group_patient_sections"));
    request.setAttribute("section_rights", new SectionRoleRightsDAO()
        .getAllSectionsRights((Integer) request.getSession().getAttribute("roleId")));

    List<BasicDynaBean> sectionsDefList = new SectionsDAO().getSections((String) ipform
        .get("sections"));
    request.setAttribute("sectionsDefMap",
        ConversionUtils.listBeanToMapBean(sectionsDefList, "section_id"));

    // markers of fields from all sections.
    List<BasicDynaBean> imageMarkers = new ImageMarkerDAO().getMarkers((String) ipform
        .get("sections"));
    request.setAttribute("sectionsImageMarkers",
        ConversionUtils.listBeanToMapListBean(imageMarkers, "section_id"));

    List<BasicDynaBean> sectionsList = new SectionsDAO().listAll();
    request.setAttribute("insta_sections", sectionsList);
    request.setAttribute("insta_sections_json",
        js.serialize(ConversionUtils.copyListDynaBeansToMap(sectionsList)));

    BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();

    BasicDynaBean consultBean = vsrDAO.findComplaint(patientId);
    if (consultBean != null) {
      request.setAttribute("consultation_bean", consultBean.getMap());
    }
    request.setAttribute("secondary_complaints", scomplaintDao.getSecondaryComplaints(patientId));
    request.setAttribute("diagnosis_details", MRDDiagnosisDAO.getAllDiagnosisDetails(patientId));

    PatientSectionDetailsDAO psd = new PatientSectionDetailsDAO();
    request.setAttribute(
        "section_finalize_status",
        ConversionUtils.listBeanToMapMap(
            psd.getSections((String) consultBean.get("mr_no"), patientId, 0, 0,
                (Integer) ipform.get("form_id")), "section_id"));
    request.setAttribute(
        "allergies",
        formDAO.getAllergies((String) consultBean.get("mr_no"),
            (String) consultBean.get("patient_id"), 0, 0, (Integer) ipform.get("form_id")));
    request.setAttribute("preAnaesthestheticList", formDAO.getPreAnaestestheticRecords(
        (String) consultBean.get("mr_no"), patientId, 0, 0, (Integer) ipform.get("form_id")));
    request.setAttribute(
        "pregnancyhistories",
        formDAO.getPregnancyHistories((String) consultBean.get("mr_no"),
            (String) consultBean.get("patient_id"), 0, 0, (Integer) ipform.get("form_id")));
    request.setAttribute(
        "pregnancyhistoriesBean",
        ConversionUtils.listBeanToListMap(formDAO.getObstetricrecords(
            (String) consultBean.get("mr_no"), patientId, 0, 0, (Integer) ipform.get("form_id"))));
    request.setAttribute(
        "antenatalinfo",
        formDAO.getAntenatalRecords((String) consultBean.get("mr_no"),
            (String) consultBean.get("patient_id"), 0, 0, (Integer) ipform.get("form_id")));

    request.setAttribute("actionId", mapping.getProperty("action_id"));
    java.util.List<BasicDynaBean> phraseSuggestions = PhraseSuggestionsMasterDAO
        .getPhraseSuggestionsDynaList();
    request.setAttribute("phrase_suggestions_json", js.deepSerialize(ConversionUtils
        .listBeanToMapListMap(phraseSuggestions, "phrase_suggestions_category_id")));

    java.util.List<BasicDynaBean> phraseSuggestionsByDept = PhraseSuggestionsMasterDAO
        .getPhraseSuggestionsByDeptDynaList(phraseDept);
    request.setAttribute("phrase_suggestions_by_dept_json", js.deepSerialize(ConversionUtils
        .listBeanToMapListMap(phraseSuggestionsByDept, "phrase_suggestions_category_id")));

    request.setAttribute("sys_generated_forms", js.deepSerialize(ConversionUtils
        .listBeanToListMap(new SystemGeneratedSectionsDAO().listAll())));
    request.setAttribute("sys_generated_section",
        ConversionUtils.listBeanToListMap(new SystemGeneratedSectionsDAO().listAll()));
    String healthAuthority = CenterMasterDAO.getHealthAuthorityForCenter(patientCenterId);
    request.setAttribute("defaultDiagnosisCodeType", HealthAuthorityPreferencesDAO
        .getHealthAuthorityPreferences(healthAuthority).getDiagnosis_code_type());
    request.setAttribute("prefColorCodes", GenericPreferencesDAO.getAllPrefs());

    HashMap<Integer, String> regExpPatternMap = RegularExpressionDAO
        .getRegPatternWithExpression("E");
    request.setAttribute("regExpMapDesc", RegularExpressionDAO.getRegPatternWithExpression("D"));
    request.setAttribute("regExpPatternMap", js.serialize(regExpPatternMap));

    return mapping.findForward("visitsummaryrecord");
  }

  /**
   * Update.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws Exception the exception
   */
  public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, IOException, Exception {

    HttpSession session = request.getSession(false);
    String userName = (String) session.getAttribute("userId");
    String patientId = request.getParameter("patient_id");
    String authorUser = request.getParameter("authUser");
    Map params = request.getParameterMap();
    ArrayList errors = new ArrayList();

    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);
    boolean allSuccess = false;

    String error = null;
    try {
      if (authorUser != null && !authorUser.isEmpty()) {
        userName = authorUser;
      }
      // saving the optional components allergies, vitals, consultation notes, and physician forms
      error = new IPForms().save(con, params);
      if (error == null) {
        allSuccess = true;
      }
    } finally {
      DataBaseUtil.commitClose(con, allSuccess);
    }
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
    FlashScope flash = FlashScope.getScope(request);
    if (!errors.isEmpty()) {
      flash.put("error", "Some values had invalid format");
    } else if (!allSuccess) {
      flash.put("error", error == null ? "Transaction failed" : error);
    } else {
      Boolean isPrint = new Boolean(request.getParameter("printVisitSummary"));
      if (isPrint) {
        ActionRedirect printRedirect = new ActionRedirect(mapping.findForward("printRedirect"));
        printRedirect.addParameter("patient_id", patientId);

        List<String> printURLs = new ArrayList<String>();
        printURLs.add(request.getContextPath() + printRedirect.getPath());
        request.getSession(false).setAttribute("printURLs", printURLs);
      }
      flash.put("success", "visit summary record saved successfully..");
    }

    redirect.addParameter("patient_id", request.getParameter("patient_id"));
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;
  }

  /**
   * Generate report.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws DocumentException the document exception
   * @throws TemplateException the template exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws XPathExpressionException the x path expression exception
   * @throws TransformerException the transformer exception
   */
  public ActionForward generateReport(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException,
      DocumentException, TemplateException, IOException, XPathExpressionException,
      TransformerException {

    String visitId = request.getParameter("patient_id");
    BasicDynaBean pref = PrintConfigurationsDAO.getPageOptions(
        PrintConfigurationsDAO.PRINT_TYPE_PATIENT, 0);
    String printMode = "P";
    if (pref.get("print_mode") != null) {
      printMode = (String) pref.get("print_mode");
    }

    GenericVisitSummaryRecordsFtlHelper ftlHelper = new GenericVisitSummaryRecordsFtlHelper(
        AppInit.getFmConfig());

    if (printMode.equals("P")) {
      response.setContentType("application/pdf");
      OutputStream os = response.getOutputStream();
      ftlHelper.getVisitSummaryRecordsReport(visitId,
          GenericVisitSummaryRecordsFtlHelper.ReturnType.PDF, pref, os);
      os.close();
    } else {
      String textReport = new String(ftlHelper.getVisitSummaryRecordsReport(visitId,
          GenericVisitSummaryRecordsFtlHelper.ReturnType.TEXT_BYTES, pref, null));
      request.setAttribute("textReport", textReport);
      request.setAttribute("textColumns", pref.get("text_mode_column"));
      request.setAttribute("printerType", "DMP");
      return mapping.findForward("textPrintApplet");
    }
    return null;
  }

}