package com.insta.hms.eandmcalculator;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.billing.BillActivityChargeDAO;
import com.insta.hms.billing.BillDAO;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.genericdocuments.GenericDocumentsFields;
import com.insta.hms.insurance.SponsorBO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.HealthAuthorityPreferences.HealthAuthorityPreferencesDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.medicalrecorddepartment.MRDUpdateScreenBO;
import com.insta.hms.outpatient.DoctorConsultationDAO;
import com.lowagie.text.DocumentException;

import flexjson.JSONSerializer;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.xpath.XPathExpressionException;

// TODO: Auto-generated Javadoc
/**
 * The Class EandMcalculatorAction.
 */
public class EandMcalculatorAction extends DispatchAction {

  /** The dao. */
  EandMcalculatorDao dao = new EandMcalculatorDao();

  /** The forms with full names. */
  public static List<String> formsWithFullNames = new ArrayList<String>();

  /** The forms with short names. */
  public static List<String> formsWithShortNames = new ArrayList<String>();

  /** The consult DAO. */
  DoctorConsultationDAO consultDAO = new DoctorConsultationDAO();
  
  private static final GenericDAO consultationEmCalculatorDAO =
      new GenericDAO("consultation_em_calculation");

  static {

    formsWithFullNames.add("History of present Illness");
    formsWithFullNames.add("Review of Systems");
    formsWithFullNames.add("Personal, Family & Social History");
    formsWithFullNames.add("Physical Examination");

    formsWithShortNames.add("HPI");
    formsWithShortNames.add("ROS");
    formsWithShortNames.add("PFSH");
    formsWithShortNames.add("PE");

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
   * @throws SQLException
   *           the SQL exception
   */
  public ActionForward getScreen(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException {

    HttpSession session = (HttpSession) request.getSession(false);
    Integer centerId = (Integer) session.getAttribute("centerId");
    String consultationIdStr = request.getParameter("consultationId");
    Integer consultationId = -1;
    JSONSerializer js = new JSONSerializer().exclude(".class");
    List allowdConsultItemCodes = null;
    if (null != consultationIdStr && !consultationIdStr.equals("")
        && request.getParameter("formDoesnotExists") == null) {
      consultationId = Integer.parseInt(consultationIdStr);

      BasicDynaBean exists = null;

      for (int i = 0; i < formsWithFullNames.size(); i++) {
        exists = dao.isFormExist(formsWithFullNames.get(i));

        if (exists == null) {
          exists = dao.isFormExist(formsWithShortNames.get(i));
        }

        if (exists == null) {
          ActionRedirect redirect = new ActionRedirect(mapping.findForward("getScreenRedirect"));
          FlashScope scope = FlashScope.getScope(request);
          scope.error("The form " + formsWithFullNames.get(i) + " (Or) "
              + formsWithShortNames.get(i) + " Does not exist");
          redirect.addParameter("consultationId", consultationIdStr);
          redirect.addParameter("formDoesnotExists", true);
          redirect.addParameter(FlashScope.FLASH_KEY, scope.key());

          return redirect;
        }

      }

      BasicDynaBean consultBean = consultDAO.getConsultationDetails(consultationId);
      String patientId = (String) consultBean.get("patient_id");
      Integer patientCenterId = centerId;
      if (patientId != null && !patientId.equals("")) {
        patientCenterId = (Integer) new GenericDAO("patient_registration")
            .findByKey("patient_id", patientId).get("center_id");
      }
      Map<String, Integer> map = dao.getCountMap(consultationId, (String) consultBean.get("mr_no"));
      request.setAttribute("countedMap", map);
      request.setAttribute("consultationBean", consultBean);
      request.setAttribute("eandmBean",
          consultationEmCalculatorDAO.findByKey("consultation_id", consultationId));
      allowdConsultItemCodes = ConversionUtils.listBeanToListMap(
          new MRDUpdateScreenBO().getAllowedItemCodesList((String) consultBean.get("patient_id")));

      // Fetch the supported code type for Consultations category
      String[] consCodeTypes = HealthAuthorityPreferencesDAO
          .getHealthAuthorityPreferences(
              CenterMasterDAO.getHealthAuthorityForCenter(patientCenterId))
          .getConsultation_code_types();
      String consCodeType = "E&M";
      if (consCodeTypes != null && consCodeTypes.length > 0) {
        for (String codeType : consCodeTypes) {
          BasicDynaBean codeTypeBean = new GenericDAO("mrd_supported_code_types")
              .findByKey("code_type", codeType);
          if (codeTypeBean.get("status") != null && codeTypeBean.get("status").equals("A")) {
            consCodeType = codeType;
            break;
          }
        }
      }
      request.setAttribute("consultationSupportedCodeType", consCodeType);
    }
    request.setAttribute("allowdConsulItemCodes", js.deepSerialize(allowdConsultItemCodes));

    return mapping.findForward("eandmScreen");
  }

  /**
   * Save eand mvalues.
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
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws Exception
   *           the exception
   */
  public ActionForward saveEandMvalues(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
          throws SQLException, IOException, Exception {

    int consultationId = Integer.parseInt(request.getParameter("consultationId"));
    String visitType = request.getParameter("visitTypes");

    HttpSession session = request.getSession();
    String userid = (String) session.getAttribute("userid");

    ActionRedirect redirect = new ActionRedirect(mapping.findForward("getScreenRedirect"));
    redirect.addParameter("consultationId", consultationId);

    boolean operation = false;

    BasicDynaBean bean = consultationEmCalculatorDAO.getBean();
    
    int hpiCount = Integer.parseInt(request.getParameter("hpiCount"));
    int rosCount = Integer.parseInt(request.getParameter("rosCount"));
    int pfshCount = Integer.parseInt(request.getParameter("pfshCount"));
    int peCount = Integer.parseInt(request.getParameter("peCount"));
    String problemStatus = request.getParameter("problemStatus");
    
    
    bean.set("visit_type", visitType);
    bean.set("hpi_count", hpiCount);
    bean.set("ros_count", rosCount);
    bean.set("pfsh_count", pfshCount);
    bean.set("pe_count", peCount);
    bean.set("problem_status", problemStatus);
    
    String treatmentOptionsCount = request.getParameter("treatmentCount");
    String calcTreatmentOptionsCount = request.getParameter("calcTreatmentOptionsCount");
    String complexityCountHiddenVal = request.getParameter("complexityCountHiddenVal");
        
    if (!treatmentOptionsCount.equals("")) {
      bean.set("treatment_options_count", Integer.parseInt(treatmentOptionsCount));
    }
    if (!calcTreatmentOptionsCount.equals("")) {
      bean.set("calculated_treatment_options_count", Integer.parseInt(calcTreatmentOptionsCount));
    }
    if (!complexityCountHiddenVal.equals("")) {
      bean.set("data_amount_complexity_count", Integer.parseInt(complexityCountHiddenVal));
    }
    
    String riskCount = request.getParameter("risk");
    String mdmValue = request.getParameter("mdmHiddenVal");
    
    if (!riskCount.equals("")) {
      bean.set("risk_count", Integer.parseInt(riskCount));
    }
    if (!mdmValue.equals("")) {
      bean.set("mdm_value", Integer.parseInt(mdmValue));
    }
    
    Boolean finalized = new Boolean(request.getParameter("h_finalize_n_update"));
    String calculatedEnMValue = request.getParameter("eandmCodeHiddenVal");
    String editedEnMValue = request.getParameter("item_code");
    
    if (!calculatedEnMValue.equals("")) {
      bean.set("em_code", Integer.parseInt(calculatedEnMValue));
    } 
    if (finalized && editedEnMValue != null && !editedEnMValue.equals("")) {
      bean.set("em_code", Integer.parseInt(editedEnMValue));
      bean.set("finalized_em_code", Integer.parseInt(editedEnMValue));
    }
    if (finalized) {
      bean.set("remarks", request.getParameter("remarks"));
      bean.set("code_finalized_date", new Timestamp(new java.util.Date().getTime()));
      bean.set("finalized_user_name", userid);
    }
    bean.set("user_name", userid);
    bean.set("complexity1", request.getParameter("complexity1"));
    bean.set("complexity2", request.getParameter("complexity2"));
    bean.set("complexity3", request.getParameter("complexity3"));
    bean.set("complexity4", request.getParameter("complexity4"));
    bean.set("complexity5", request.getParameter("complexity5"));
    bean.set("complexity6", request.getParameter("complexity6"));
    bean.set("complexity7", request.getParameter("complexity7"));
    bean.set("mod_time", new Timestamp(new java.util.Date().getTime()));

    String consBillNo = null;
    BasicDynaBean ebean = consultationEmCalculatorDAO.findByKey("consultation_id", consultationId);
    Connection con = null;
    String error = null;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      String activityId = new Integer(consultationId).toString();
      String chargeId = BillActivityChargeDAO.getChargeId("DOC", activityId);
      BasicDynaBean existingChrge = new GenericDAO("bill_charge").findByKey("charge_id", chargeId);
      if (existingChrge != null && !existingChrge.get("status").equals("X")) {
        consBillNo = (String) existingChrge.get("bill_no");
      } 
      if (ebean == null) {
        bean.set("consultation_id", consultationId);
        operation = consultationEmCalculatorDAO.insert(con, bean);

      } else {
        Map<String, Integer> keys = new HashMap<String, Integer>();
        keys.put("consultation_id", consultationId);
        operation = consultationEmCalculatorDAO.update(con, bean.getMap(), keys) > 0;

      }
      if (operation) {
        BasicDynaBean modifiedBean = consultDAO.getBean();
        modifiedBean.set("head", request.getParameter("consultation_type_id"));
        operation = consultDAO.update(con, modifiedBean.getMap(), "consultation_id",
            consultationId) > 0;

        BasicDynaBean consultbean = consultDAO.getConsultationDetails(consultationId);
        if (finalized) {
          if (!consultbean.get("status").equals("C")) {
            operation = false;
            error = "Please close consultation before updating codification E&M code.";
          }
          if (!consultbean.get("bill_status").equals("A")) {
            operation = false;
            error = "Bill is not in Open Status. Cannot update E&M code";
          }
          consultbean.set("head", modifiedBean.get("head"));
          operation &= new MRDUpdateScreenBO().updateDoctorChargesForCode(con, consultbean,
              (String) consultbean.get("patient_id"), "E&M", bean.get("em_code") + "",
              userid) == null;
        }
      }
    } finally {
      DataBaseUtil.commitClose(con, operation);
      if (operation && consBillNo != null && !consBillNo.equals("")) {
        BillDAO.resetTotalsOrReProcess(consBillNo, true, false, true);
      } 
    }

    if (operation && consBillNo != null && !consBillNo.equals("")) {
      String visitId = BillDAO.getVisitId(consBillNo);
      new SponsorBO().recalculateSponsorAmount(visitId);
    }

    Boolean isPrint = new Boolean(request.getParameter("print"));
    if (isPrint && operation) {
      ActionRedirect printRedirect = new ActionRedirect(mapping.findForward("printRedirect"));
      printRedirect.addParameter("consultationId", consultationId);

      List<String> printURLs = new ArrayList<String>();
      printURLs.add(request.getContextPath() + printRedirect.getPath());
      request.getSession(false).setAttribute("printURLs", printURLs);
    }
    return redirect;
  }

  /**
   * Gets the e mcode.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the e mcode
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  @IgnoreConfidentialFilters
  public ActionForward getEMcode(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws IOException {

    String visitType = request.getParameter("visitType");
    HashMap<String, Object> parametersMap = new HashMap<String, Object>();
    String error = null;

    parametersMap.put("HPI", Integer.parseInt(request.getParameter("hpiCount")));
    parametersMap.put("ROS", Integer.parseInt(request.getParameter("rosCount")));
    parametersMap.put("PFSH", Integer.parseInt(request.getParameter("pfshCount")));
    parametersMap.put("EXAM", Integer.parseInt(request.getParameter("peCount")));
    parametersMap.put("MDM", request.getParameter("mdm"));

    Integer emCode = EandMcalculatorDao.getEMlevelCode(visitType, parametersMap);

    response.setContentType("text/plain");
    PrintWriter writer = response.getWriter();
    writer.write(emCode.toString());
    writer.flush();
    writer.close();

    return null;
  }

  /**
   * Gets the prints the.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the prints the
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws TemplateException
   *           the template exception
   * @throws SQLException
   *           the SQL exception
   * @throws DocumentException
   *           the document exception
   * @throws XPathExpressionException
   *           the x path expression exception
   */
  public ActionForward getPrint(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws IOException, TemplateException, SQLException,
          DocumentException, XPathExpressionException {

    int consultationId = Integer.parseInt(request.getParameter("consultationId"));
    BasicDynaBean consultBean = DoctorConsultationDAO.getConsultDetails(consultationId);
    Map patientDetails = new HashMap();
    GenericDocumentsFields.copyPatientDetails(patientDetails, null,
        (String) consultBean.get("patient_id"), false);
    Map eandmDetailsMap =
        consultationEmCalculatorDAO.findByKey("consultation_id", consultationId).getMap();
    Map ftlParamMap = new HashMap();
    ftlParamMap.put("visitdetails", patientDetails);
    ftlParamMap.put("consultation_bean", consultBean);
    ftlParamMap.put("eandmDetailsMap", eandmDetailsMap);
    BasicDynaBean printPref = PrintConfigurationsDAO
        .getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT, 4);

    Template template = AppInit.getFmConfig().getTemplate("EMcodeCalculationPrint.ftl");
    HtmlConverter htmlConverter = new HtmlConverter();

    StringWriter writer = new StringWriter();
    template.process(ftlParamMap, writer);
    String textContent = writer.toString();

    response.setContentType("application/pdf");
    OutputStream os = response.getOutputStream();

    try {
      htmlConverter.writePdf(os, textContent, "EM COde Calculation Print", printPref, false, false,
          false, false, false, false);
    } catch (Exception ex) {
      ex.printStackTrace();

    } finally {
      os.close();
    }
    return null;
  }
}