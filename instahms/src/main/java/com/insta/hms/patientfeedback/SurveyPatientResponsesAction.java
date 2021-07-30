package com.insta.hms.patientfeedback;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.ftl.FtlReportGenerator;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;

import flexjson.JSONSerializer;
import freemarker.template.Configuration;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class SurveyPatientResponsesAction.
 *
 * @author mithun.saha
 */
public class SurveyPatientResponsesAction extends DispatchAction {
  
  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(SurveyPatientResponsesAction.class);
  
  /** The js. */
  JSONSerializer js = new JSONSerializer().exclude("class");
  
  /** The dao. */
  SurveyPatientResponsesDAO dao = new SurveyPatientResponsesDAO();

  /**
   * Gets the all patient responses.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the all patient responses
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getAllPatientResponses(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {

    Map map = request.getParameterMap();
    PagedList pagedList = dao.getAllSurveyPatientResponses(map,
        ConversionUtils.getListingParameter(request.getParameterMap()));
    request.setAttribute("pagedList", pagedList);

    return mapping.findForward("list");
  }

  /**
   * Prints the patient response.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws Exception the exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   */
  @IgnoreConfidentialFilters
  public ActionForward printPatientResponse(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception, IOException,
      ServletException {
    String responseId = request.getParameter("survey_response_id");
    HashMap templateParamsMap = new HashMap();
    BasicDynaBean generalBean = SurveyPatientResponsesDAO.getAllSurveyGeneralInfo(Integer
        .parseInt(responseId));
    List<BasicDynaBean> surveyPatientResponseDetails = SurveyPatientResponsesDAO
        .getSurveyResponseDetails(Integer.parseInt(responseId));
    List<BasicDynaBean> sectionIds = new ArrayList<BasicDynaBean>();
    sectionIds = new GenericDAO("survey_form_section").findAllByKey("form_id",
        (Integer) generalBean.get("form_id"));
    List<BasicDynaBean> surveyFormQuestionList = dao.getSurveyFormQuestionDetails(sectionIds,
        surveyPatientResponseDetails);
    templateParamsMap.put("generalBean", generalBean.getMap());
    templateParamsMap.put("surveyResponseDetails", surveyPatientResponseDetails);
    List<BasicDynaBean> surveySectionList = SurveyPatientResponsesDAO
        .getSurveyFormSectionDetails((Integer) generalBean.get("form_id"));
    templateParamsMap.put("surveySectionList", surveySectionList);
    templateParamsMap.put("surveyFormQuestionList", surveyFormQuestionList);
    int printerId = 0;
    Map patientDetails = VisitDetailsDAO.getPatientVisitDetailsMap((String) generalBean
        .get("visit_id"));
    templateParamsMap.put("patient", patientDetails);

    FtlReportGenerator ftlGen = null;
    String printerType = request.getParameter("printType");
    if ((printerType != null) && !printerType.equals("")) {
      printerId = Integer.parseInt(printerType);
    }

    BasicDynaBean printPref = PrintConfigurationsDAO.getPageOptions(
        PrintConfigurationsDAO.PRINT_TYPE_PATIENT_SURVEY_RESPONSE, printerId);

    if (printerId != 0) {
      templateParamsMap.put("printerId", printerId);
    } else {
      templateParamsMap.put("printerId", printPref.get("printer_id"));
    }
    Configuration cfg;
    cfg = AppInit.getFmConfig();
    cfg.setTagSyntax(Configuration.AUTO_DETECT_TAG_SYNTAX);

    String templateName = "PATIENT_RESPONSE_PRINT";
    String templateContent = null;
    String templateCodeQuery = " SELECT print_template_content FROM print_templates "
        + " WHERE template_type=?";

    BasicDynaBean templateBean = DataBaseUtil.queryToDynaBean(templateCodeQuery, templateName);
    if (templateBean != null) {
      templateContent = (String) templateBean.get("print_template_content");
    }

    if (templateContent != null && !templateContent.isEmpty()) {
      logger.debug("templateContent=" + templateContent);
      StringReader reader = new StringReader(templateContent);
      ftlGen = new FtlReportGenerator("CustomTemplate", reader);
    } else {
      ftlGen = new FtlReportGenerator("PatientResponsePrint");
    }
    StringWriter writer = new StringWriter();
    ftlGen.setReportParams(templateParamsMap);
    ftlGen.process(writer);
    OutputStream os = null;
    os = response.getOutputStream();
    response.setContentType("application/pdf");
    BasicDynaBean printPrefs = PrintConfigurationsDAO.getPageOptions(
        PrintConfigurationsDAO.PRINT_TYPE_PATIENT_SURVEY_RESPONSE, printerId);
    boolean repeatPatientHeader = ((String) printPrefs.get("repeat_patient_info"))
        .equalsIgnoreCase("Y");
    HtmlConverter hc = new HtmlConverter();
    hc.writePdf(os, writer.toString(), "Patient Survey Response ", printPrefs, false,
        repeatPatientHeader, true, true, true, false);
    os.close();
    return null;
  }
}
