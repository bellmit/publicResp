
package com.insta.hms.medicalrecorddepartment;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.ipservices.DashBoardDAO;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.master.InsuranceCategoryMaster.InsuranceCategoryMasterDAO;
import com.insta.hms.master.InsuranceCompanyTPAMaster.InsuranceCompanyTPAMasterDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO;
import com.insta.hms.master.TpaMaster.TpaMasterDAO;

import flexjson.JSONSerializer;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class ICDPatientSearchAction.
 */
public class ICDPatientSearchAction extends DispatchAction {

  /** The log. */
  static Logger log = LoggerFactory
      .getLogger(ICDPatientSearchAction.class);
  static JSONSerializer js = new JSONSerializer().exclude("class");
  static InsuranceCompanyTPAMasterDAO insuranceCoTpaMasterDao = new InsuranceCompanyTPAMasterDAO();
  static InsuranceCategoryMasterDAO insuranceCatMasterDao = new InsuranceCategoryMasterDAO();
  static TpaMasterDAO tpaMasterDao = new TpaMasterDAO();

  /**
   * Search ICD patients.
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
   * @throws Exception
   *           the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward searchICDPatients(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {

    Map<LISTING, Object> pagingParams = ConversionUtils
        .getListingParameter(request.getParameterMap());

    PagedList list = null;

    list = PatientDetailsDAO.searchICDPatients(request.getParameterMap(), pagingParams);
    request.setAttribute("pagedList", list);
    request.setAttribute("doctors", new DashBoardDAO().getDoctors());
    request.setAttribute("doclist", DoctorMasterDAO.getAllActiveDoctors());
    request.setAttribute("regPref", RegistrationPreferencesDAO.getRegistrationPreferences());
    List userList = PatientDetailsDAO.getUserNameByRights();
    request.setAttribute("asigneeNames", userList);
    request.setAttribute("insCompTpaList", js.serialize(
            ConversionUtils.listBeanToListMap(insuranceCoTpaMasterDao
            .getCompanyTpaList())));
    request.setAttribute("tpaList", js.serialize(ConversionUtils
            .listBeanToListMap(tpaMasterDao
            .listAll(null, "status", "A", "tpa_name"))));
    request.setAttribute("inscatname", InsuranceCategoryMasterDAO
            .getInsCatCenter(RequestContext.getCenterId()));
    return mapping.findForward("getICDSearchScreen");
  }

  /**
   * Gets the screen.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the screen
   * @throws Exception
   *           the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getScreen(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws Exception {

    request.setAttribute("regPref", RegistrationPreferencesDAO.getRegistrationPreferences());
    request.setAttribute("doctors", new DashBoardDAO().getDoctors());
    request.setAttribute("doclist", DoctorMasterDAO.getAllActiveDoctors());
    request.setAttribute("insCompTpaList", js.serialize(
            ConversionUtils.listBeanToListMap(insuranceCoTpaMasterDao
            .getCompanyTpaList())));
    request.setAttribute("tpaList", js.serialize(ConversionUtils
            .listBeanToListMap(tpaMasterDao
            .listAll(null, "status", "A", "tpa_name"))));
    request.setAttribute("inscatname", InsuranceCategoryMasterDAO
            .getInsCatCenter(RequestContext.getCenterId()));

    return mapping.findForward("getICDSearchScreen");
  }

  /**
   * Prints the old.
   *
   * @param mapping
   *          the mapping
   * @param af
   *          the af
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws Exception
   *           the exception
   */
  public ActionForward printOld(ActionMapping mapping, ActionForm af, HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    ICDPatientSearchForm form = (ICDPatientSearchForm) af;
    Map filter = request.getParameterMap();
    String condition = " WHERE 1=1 ";

    if ((form.getMrno() != null) && (!form.getMrno().equals(""))) {
      condition = condition + " AND pd.mr_no = '" + form.getMrno() + "' ";

    }
    if ((form.getComplaint() != null) && (!form.getComplaint().equals(""))) {
      condition = condition + " AND complaint_icd ilike '%" + form.getComplaint() + "%' ";

    }

    if ((form.getDiagnosis() != null) && (!form.getDiagnosis().equals(""))) {
      condition = condition + " AND diagnosis_icd ilike '%" + form.getDiagnosis() + "%' ";
    }

    if ((form.getTreatment() != null) && (!form.getTreatment().equals(""))) {
      condition = condition + " AND treatment_icd ilike '%" + form.getTreatment() + "%' ";
    }

    if ((form.getRegFromDate() != null && !form.getRegFromDate().equals(""))
        && (form.getRegToDate() != null) && !form.getRegToDate().equals("")) {
      condition = condition + " AND date(pra.reg_date)  between '"
          + DataBaseUtil.parseDate(form.getRegFromDate()) + "' AND '"
          + DataBaseUtil.parseDate(form.getRegToDate()) + "'";
    }

    if ((form.getDisFromDate() != null && !form.getDisFromDate().equals(""))
        && (form.getDisToDate() != null) && !form.getDisToDate().equals("")) {
      condition = condition + "  AND date(pra.discharge_date)  between '"
          + DataBaseUtil.parseDate(form.getDisFromDate()) + "' AND '"
          + DataBaseUtil.parseDate(form.getDisToDate()) + "'";
    }

    HashMap params = new HashMap();
    params.put("condition", condition);
    Map<LISTING, Object> pagingParams = ConversionUtils
        .getListingParameter(request.getParameterMap());
    PagedList list = null;
    list = PatientDetailsDAO.searchICDPatients(params, pagingParams);
    Template template = AppInit.getFmConfig().getTemplate("ICDPatientReport.ftl");
    Map ftlParams = new HashMap();
    ftlParams.put("patientsList", list.getDtoList());
    StringWriter writer = new StringWriter();
    try {
      template.process(ftlParams, writer);
    } catch (TemplateException te) {
      throw te;
    }
    HtmlConverter converter = new HtmlConverter();
    OutputStream os = response.getOutputStream();
    /*
     * res.setContentType("text/html"); os.write(writer.toString().getBytes());
     */
    response.setContentType("application/pdf");
    converter.writePdf(os, writer.toString());
    return null;
  }

  /**
   * Prints the.
   *
   * @param mapping
   *          the mapping
   * @param af
   *          the af
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws Exception
   *           the exception
   */
  public ActionForward print(ActionMapping mapping, ActionForm af, HttpServletRequest request,
      HttpServletResponse response) throws Exception {

    Map filter = request.getParameterMap();

    Map<LISTING, Object> pagingParams = ConversionUtils
        .getListingParameter(request.getParameterMap());
    PagedList list = null;
    list = PatientDetailsDAO.searchICDPatients(filter, pagingParams);

    Template template = AppInit.getFmConfig().getTemplate("ICDPatientSearchReportprint.ftl");

    Map ftlParams = new HashMap();
    ftlParams.put("patientsList", list.getDtoList());

    StringWriter writer = new StringWriter();

    try {
      template.process(ftlParams, writer);
    } catch (TemplateException te) {
      throw te;
    }

    HtmlConverter converter = new HtmlConverter();
    OutputStream os = response.getOutputStream();
    /*
     * response.setContentType("text/html"); os.write(writer.toString().getBytes());
     */

    response.setContentType("application/pdf");
    converter.writePdf(os, writer.toString());

    return null;

  }

  /**
   * Save codification.
   *
   * @param map
   *          the map
   * @param form
   *          the form
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the action forward
   * @throws ServletException
   *           the servlet exception
   * @throws Exception
   *           the exception
   */
  public ActionForward saveCodification(ActionMapping map, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws ServletException, Exception {

    String[] patientId = req.getParameterValues("_selectedPatient");
    List<String> patientList = new ArrayList<String>();
    String codifiedBy = req.getParameter("_assign_to");
    boolean success = false;

    for (int i = 0; i < patientId.length; i++) {
      patientList.add(patientId[i]);
    }
    success = ICDPatientSearchDAO.updateCodifiedBy(patientList, codifiedBy);

    if (success) {
      log.info("Updated Successfully");
    } else {
      log.error("Updation Failed...");
    }

    FlashScope flash = FlashScope.getScope(req);
    ActionRedirect redirect = new ActionRedirect(
        req.getHeader("Referer").replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;
  }

  /**
   * Codification complete.
   *
   * @param map
   *          the map
   * @param form
   *          the form
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the action forward
   * @throws ServletException
   *           the servlet exception
   * @throws Exception
   *           the exception
   */
  public ActionForward codificationComplete(ActionMapping map, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws ServletException, Exception {

    String[] patientId = req.getParameterValues("_selectedPatient");
    List<String> patientList = new ArrayList<String>();
    boolean success = false;

    for (int i = 0; i < patientId.length; i++) {
      patientList.add(patientId[i]);
    }
    success = ICDPatientSearchDAO.verifiedAndCompleted(patientList);

    if (success) {
      log.info("Updated Successfully");
    } else {
      log.error("Updation Failed...");
    }

    FlashScope flash = FlashScope.getScope(req);
    ActionRedirect redirect = new ActionRedirect(
        req.getHeader("Referer").replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;
  }

  /**
   * Reopen for codification.
   *
   * @param map
   *          the map
   * @param form
   *          the form
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the action forward
   * @throws ServletException
   *           the servlet exception
   * @throws Exception
   *           the exception
   */
  public ActionForward reopenForCodification(ActionMapping map, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws ServletException, Exception {

    String[] patientId = req.getParameterValues("_selectedPatient");
    List<String> patientList = new ArrayList<String>();
    boolean success = false;

    for (int i = 0; i < patientId.length; i++) {
      patientList.add(patientId[i]);
    }
    success = ICDPatientSearchDAO.reopenForCodification(patientList);

    if (success) {
      log.info("Reopened for codification successfully");
    } else {
      log.error("Updation Failed...");
    }

    ActionRedirect redirect = new ActionRedirect(
        req.getHeader("Referer").replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));
    FlashScope flash = FlashScope.getScope(req);
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;
  }

  /**
   * Ajax Call to get InsComp,TpaList and InsCatList.
   *
   * @param form
   *          the form
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the action forward
   * @throws ServletException
   *           the servlet exception
   * @throws Exception
   *           the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getdetailsAJAX(ActionMapping mapping, ActionForm form, 
      HttpServletRequest req,HttpServletResponse res) throws Exception {
    String insuranceCoId = req.getParameter("primary_insurance_co_id");
    Integer centerId = RequestContext.getCenterId();
    res.setContentType("application/json");
    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    List<BasicDynaBean> catList = insuranceCatMasterDao.getInsCategory(centerId, insuranceCoId);
    if (catList == null || catList.isEmpty()) {
      catList = insuranceCatMasterDao.getInsCatCenter(centerId);
    }
    Map responseMap = new HashMap();
    responseMap.put("inscatName", ConversionUtils
          .listBeanToListMap(catList));
    res.getWriter().write(js.deepSerialize(responseMap));
    res.flushBuffer();
    return null;
  }
}
