/**
 *
 */

package com.insta.hms.initialassessment;

import com.bob.hms.common.APIUtility;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Preferences;
import com.bob.hms.common.RequestContext;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.preferences.clinicalpreferences.ClinicalPreferencesService;
import com.insta.hms.instaforms.AbstractInstaForms;
import com.insta.hms.instaforms.IAForms;
import com.insta.hms.instaforms.PatientSectionDetailsDAO;
import com.insta.hms.master.FormComponents.FormComponentsDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.IAComponents.IAComponentsDAO;
import com.insta.hms.master.ImageMarkers.ImageMarkerDAO;
import com.insta.hms.master.PhraseSuggestionsMaster.PhraseSuggestionsMasterDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.RegularExpression.RegularExpressionDAO;
import com.insta.hms.master.Sections.SectionsDAO;
import com.insta.hms.master.outpatient.SystemGeneratedSectionsDAO;
import com.insta.hms.master.sectionrolerights.SectionRoleRightsDAO;
import com.insta.hms.outpatient.DoctorConsultationDAO;
import com.insta.hms.usermanager.User;
import com.insta.hms.usermanager.UserDAO;
import com.insta.hms.vitalForm.VisitVitalsDAO;
import com.insta.hms.vitalForm.genericVitalFormDAO;
import com.insta.hms.vitalparameter.VitalMasterDAO;
import com.insta.instaapi.common.JsonProcessor;
import com.insta.instaapi.common.ServletContextUtil;
import com.lowagie.text.DocumentException;
import flexjson.JSONSerializer;
import freemarker.template.TemplateException;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

// TODO: Auto-generated Javadoc
/**
 * The Class InitialAssessmentAction.
 *
 * @author krishna
 */
public class InitialAssessmentAction extends DispatchAction {

  /** The vm DAO. */
  VitalMasterDAO vmDAO = new VitalMasterDAO();

  /** The vv DAO. */
  VisitVitalsDAO vvDAO = new VisitVitalsDAO();

  /** The consult dao. */
  DoctorConsultationDAO consultDao = new DoctorConsultationDAO();

  /** The pat det dao. */
  PatientDetailsDAO patDetDao = new PatientDetailsDAO();

  /** The assessment comp dao. */
  IAComponentsDAO assessmentCompDao = new IAComponentsDAO();

  /** The user dao. */
  UserDAO userDao = new UserDAO();
  
  private static final JSONSerializer js = JsonProcessor.getJSONParser();

  /**
   * Gets the initial assessment screen.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the initial assessment screen
   * @throws ServletException the servlet exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   */
  public ActionForward getInitialAssessmentScreen(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws ServletException,
      IOException, SQLException, ParseException {
    String consId = request.getParameter("consultation_id");
    BasicDynaBean consultBean = consultDao.findConsultationExt(Integer.parseInt(consId));

    // Redirect the request if module, mod_newcons is enabled.
    Map modulesActivatedMap =
        ((Preferences) RequestContext.getSession().getAttribute("preferences"))
            .getModulesActivatedMap();
    String mrNo = (String) consultBean.get("mr_no");
    String modNewconsStatus = (String) modulesActivatedMap.get("mod_newcons");
    if (null != modNewconsStatus && modNewconsStatus.equals("Y")) {
      ActionRedirect redirect =
          new ActionRedirect("/initialassessment/index.htm#/filter/default/patient/"
              + URLEncoder.encode(mrNo, "UTF-8") + "/initialassessment/" + consId
              + "?retain_route_params=true");
      return redirect;
    }

    String patientId = (String) consultBean.get("patient_id");
    BasicDynaBean consBean =
        (BasicDynaBean) new VisitDetailsDAO().getAllVisitsAndDoctorsByPatientId(patientId);
    request.setAttribute("antenatal_bean_doctor_id", consBean.get("doctor"));
    request.setAttribute("antenatal_bean_doctor_name", consBean.get("doctor_name"));
    request.setAttribute("consultation_bean", consultBean.getMap());
    request.setAttribute("all_fields", vmDAO.getActiveVitalParams("O"));
    List readingList = vvDAO.getVitals(request.getParameter("patient_id"), null, null, "V");
    request.setAttribute("vital_readings", readingList);
    // vital reading exists is defined using above list, anyhow no date filters applied.
    request.setAttribute("vital_reading_exists", !readingList.isEmpty());
    JSONSerializer js = new JSONSerializer().exclude("class");
    request.setAttribute("latest_vital_reading_json", js.deepSerialize(ConversionUtils
        .copyListDynaBeansToMap(vvDAO.getLatestVitals((String) consultBean.get("mr_no"),
            (String) consultBean.get("patient_id")))));

    request.setAttribute("height_weight_params", js.deepSerialize(ConversionUtils
        .copyListDynaBeansToMap(vvDAO.getHeightAndWeight((String) consultBean.get("mr_no"),
            (String) consultBean.get("patient_id")))));
    AbstractInstaForms formDAO = new IAForms();
    BasicDynaBean iaform = formDAO.getComponents(request.getParameterMap());
    request.setAttribute("form", iaform);
    request.setAttribute("insta_form_json", js.serialize(iaform.getMap()));

    List<BasicDynaBean> sectionsDefList =
        new SectionsDAO().getSections((String) iaform.get("sections"));
    request.setAttribute("sectionsDefMap",
        ConversionUtils.listBeanToMapBean(sectionsDefList, "section_id"));

    PatientSectionDetailsDAO psd = new PatientSectionDetailsDAO();
    request.setAttribute("section_finalize_status", ConversionUtils.listBeanToMapMap(psd
        .getSections((String) consultBean.get("mr_no"), patientId, Integer.parseInt(consId), 0,
            (Integer) iaform.get("form_id")), "section_id"));

    // markers of fields from all sections.
    List<BasicDynaBean> imageMarkers =
        new ImageMarkerDAO().getMarkers((String) iaform.get("sections"));
    request.setAttribute("sectionsImageMarkers",
        ConversionUtils.listBeanToMapListBean(imageMarkers, "section_id"));

    request.setAttribute("sys_generated_forms", js.deepSerialize(ConversionUtils
        .listBeanToListMap(new SystemGeneratedSectionsDAO().listAll())));

    request.setAttribute("sys_generated_section",
        ConversionUtils.listBeanToListMap(new SystemGeneratedSectionsDAO().listAll()));

    List<BasicDynaBean> sectionsList = new SectionsDAO().listAll();
    request.setAttribute("insta_sections", sectionsList);
    request.setAttribute("insta_sections_json",
        js.serialize(ConversionUtils.copyListDynaBeansToMap(sectionsList)));
    request.setAttribute("group_patient_sections",
        new FormComponentsDAO().findByKey("id", iaform.get("form_id"))
            .get("group_patient_sections"));
    request.setAttribute("section_rights", new SectionRoleRightsDAO()
        .getAllSectionsRights((Integer) request.getSession().getAttribute("roleId")));

    User user = userDao.getUser((String) request.getSession(false).getAttribute("userid"));
    request.setAttribute("doctor_logged_in", user == null ? "" : user.getDoctorId());
    Map patientDetails =
        VisitDetailsDAO.getPatientVisitDetailsMap(request.getParameter("patient_id"));
    request.setAttribute("referenceList", genericVitalFormDAO.getReferenceRange(patientDetails));
    request.setAttribute("prefColorCodes", GenericPreferencesDAO.getAllPrefs());
    request.setAttribute("paramType", "V");

    request.setAttribute(
        "pregnancyhistories",
        formDAO.getPregnancyHistories((String) consultBean.get("mr_no"),
            (String) consultBean.get("patient_id"), Integer.parseInt(consId), 0,
            (Integer) iaform.get("form_id")));
    request.setAttribute("pregnancyhistoriesBean", ConversionUtils.listBeanToListMap(formDAO
        .getObstetricrecords((String) consultBean.get("mr_no"), patientId,
            Integer.parseInt(consId), 0, (Integer) iaform.get("form_id"))));
    request.setAttribute(
        "antenatalinfo",
        formDAO.getAntenatalRecords((String) consultBean.get("mr_no"),
            (String) consultBean.get("patient_id"), Integer.parseInt(consId), 0,
            (Integer) iaform.get("form_id")));

    java.util.List<BasicDynaBean> phraseSuggestions =
        PhraseSuggestionsMasterDAO.getPhraseSuggestionsDynaList();
    request.setAttribute("phrase_suggestions_json", js.deepSerialize(ConversionUtils
        .listBeanToMapListMap(phraseSuggestions, "phrase_suggestions_category_id")));

    java.util.List<BasicDynaBean> phraseSuggestionsByDept =
        PhraseSuggestionsMasterDAO.getPhraseSuggestionsByDeptDynaList((String) consultBean
            .get("dept_id"));
    request.setAttribute("phrase_suggestions_by_dept_json", js.deepSerialize(ConversionUtils
        .listBeanToMapListMap(phraseSuggestionsByDept, "phrase_suggestions_category_id")));

    HashMap<Integer, String> regExpPatternMap =
        RegularExpressionDAO.getRegPatternWithExpression("E");
    request.setAttribute("regExpMapDesc", RegularExpressionDAO.getRegPatternWithExpression("D"));
    request.setAttribute("regExpPatternMap", js.serialize(regExpPatternMap));
    String consultationEditAcrossDoctorsOp =
        (String) ApplicationContextProvider.getBean(ClinicalPreferencesService.class)
            .getClinicalPreferences().get("op_consultation_edit_across_doctors");
    request.setAttribute("opConsultationEditAcrossDoctors", consultationEditAcrossDoctorsOp);
    return mapping.findForward("initialassessment");
  }

  /**
   * Update.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws ServletException the servlet exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   * @throws Exception the exception
   */
  public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException, SQLException, Exception {
    Map params = request.getParameterMap();
    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);
    boolean allSuccess = false;
    String consultationIdStr = request.getParameter("consultation_id");
    String error = null;

    try {
      txn: {
        error = new IAForms().save(con, params);
        if (error != null) {
          break txn;
        }
        BasicDynaBean bean = consultDao.getBean();
        bean.set("initial_assessment_status", "P");

        if (consultDao.update(con, bean.getMap(), "consultation_id",
            Integer.parseInt(consultationIdStr)) == 0) {
          break txn;
        }
      }
      allSuccess = true;
    } finally {
      DataBaseUtil.commitClose(con, allSuccess);
    }
    FlashScope flash = FlashScope.getScope(request);
    if (allSuccess) {
      if (new Boolean(request.getParameter("printAssessment"))) {
        ActionRedirect printRedirect = new ActionRedirect(mapping.findForward("printRedirect"));
        printRedirect.addParameter("consultation_id", consultationIdStr);

        List<String> printURLs = new ArrayList<String>();
        printURLs.add(request.getContextPath() + printRedirect.getPath());
        request.getSession(false).setAttribute("printURLs", printURLs);
      }
    } else {
      flash.put("error", error != null ? error : "Transaction Failed");
    }
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
    String patientId = (String) request.getParameter("patient_id");
    redirect.addParameter("consultation_id", consultationIdStr);
    redirect.addParameter("patient_id", patientId);

    return redirect;
  }

  /**
   * Prints the initial assessment.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws ServletException the servlet exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   * @throws XPathExpressionException the x path expression exception
   * @throws TransformerException the transformer exception
   * @throws DocumentException the document exception
   * @throws TemplateException the template exception
   */
  public ActionForward printInitialAssessment(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws ServletException,
      IOException, SQLException, XPathExpressionException, TransformerException, DocumentException,
      TemplateException {
    
    String reqHandlerKey = request.getParameter("request_handler_key");
    String error =
        APIUtility.setConnectionDetails(servlet.getServletContext(),
            reqHandlerKey);
    if (error != null) {
      APIUtility.setInvalidLoginError(response, error);
      return null;
    }
    ServletContext ctx = servlet.getServletContext();
    Map<String, Object> sessionMap = ServletContextUtil.getContextParametersMap(ctx);
    Map<String,Object> sessionParameters = null;
    if (sessionMap != null && !sessionMap.isEmpty()) {
      sessionParameters = (Map<String,Object>) sessionMap.get(reqHandlerKey);
    }
 
    String consIdStr = request.getParameter("consultation_id");
    int consId = Integer.parseInt(consIdStr);
    String printerIdStr = request.getParameter("printerId");
    BasicDynaBean prefs = null;
    int printerId = 0;
    if ((printerIdStr != null) && !printerIdStr.equals("")) {
      printerId = Integer.parseInt(printerIdStr);
    }
    
    boolean isPatientLogin = sessionParameters != null 
        && ((boolean) sessionParameters.get("patient_login"));
    if (isPatientLogin) {
      BasicDynaBean consultBean = consultDao.findConsultationExt(consId);
      String mrNo = (String) sessionParameters.get("customer_user_id");
      if (!((String)consultBean.get("mr_no")).equals(mrNo)) {
        String successMsg = "Invalid input parameters supplied for prescription_id";
        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("return_code", "1021");
        errorMap.put("return_message", successMsg);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write(js.deepSerialize(errorMap));
        response.flushBuffer();
        return null;
      }
    }
    prefs =
        PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT, printerId);

    String printMode = "P";
    if (prefs.get("print_mode") != null) {
      printMode = (String) prefs.get("print_mode");
    }

    String userName = (String) request.getSession(false).getAttribute("userid");
    InitialAssessmentFtlHelper ftlHelper = new InitialAssessmentFtlHelper();
    if (printMode.equals("P")) {
      response.setContentType("application/pdf");
      OutputStream os = response.getOutputStream();
      ftlHelper.printAssessment(consId, prefs, os, userName,
          InitialAssessmentFtlHelper.ReturnType.PDF);
      os.close();

    } else {
      String textReport =
          new String(ftlHelper.printAssessment(consId, prefs, null, userName,
              InitialAssessmentFtlHelper.ReturnType.TEXT_BYTES));
      request.setAttribute("textReport", textReport);
      request.setAttribute("textColumns", prefs.get("text_mode_column"));
      request.setAttribute("printerType", "DMP");
      return mapping.findForward("textPrintApplet");

    }

    return null;
  }
}
