package com.insta.hms.ipservices;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.DietaryMaster.DietaryMasterDAO;
import com.insta.hms.master.DietaryMaster.PatientDietPrescriptionsDAO;
import com.insta.hms.master.DietaryMaster.PrescribedMealFtlHealper;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.master.GenericDocumentTemplate.GenericDocumentTemplateDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.lowagie.text.DocumentException;

import flexjson.JSONSerializer;
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

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

// TODO: Auto-generated Javadoc
/**
 * The Class DietPrescriptionsAction.
 */
public class DietPrescriptionsAction extends DispatchAction {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(DietPrescriptionsAction.class);
  private static final GenericDAO patientDietPrescriptionsDAO =
      new GenericDAO("patient_diet_prescriptions");

  /**
   * Gets the prescription screen.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the prescription screen
   * @throws SQLException the SQL exception
   */
  public ActionForward getPrescriptionScreen(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException {
    DietaryMasterDAO dao = new DietaryMasterDAO();
    String mealNameAndCharges = dao.getAllMeal();
    String docId = "";
    String templateId = "";
    String format = "";
    BasicDynaBean dietDocBean = new GenericDAO("diet_chart_documents").findByKey("patient_id",
        request.getParameter("patient_id"));
    if (dietDocBean != null) {
      docId = dietDocBean.get("doc_id").toString();
      templateId = dietDocBean.get("template_id").toString();
      GenericDAO patientDocDAO = new GenericDAO("patient_documents");
      BasicDynaBean patientDocBean = patientDocDAO.getBean();
      patientDocDAO.loadByteaRecords(patientDocBean, "doc_id", Integer.parseInt(docId));
      format = (String) patientDocBean.get("doc_format");

    }

    request.setAttribute("mealNameAndCharges", mealNameAndCharges);
    request.setAttribute("patientid", request.getParameter("patient_id"));
    PatientDietPrescriptionsDAO prescriptionDAO = new PatientDietPrescriptionsDAO();
    List prescriptionList = prescriptionDAO.getPrescribedMealsForPatient(request
        .getParameter("patient_id"));
    request.setAttribute("prescriptionList", prescriptionList);
    request.setAttribute("screentype", "prescriptionScreen");
    JSONSerializer js = new JSONSerializer();
    request.setAttribute("doctorList",
        js.exclude("class").serialize(new DoctorMasterDAO().getAllDoctor()));
    request.setAttribute("dietaryTempletes",
        GenericDocumentTemplateDAO.getTemplates(true, "SYS_DIE", "A"));
    request.setAttribute("doc_id", docId);
    request.setAttribute("template_id", templateId);
    request.setAttribute("format", format);
    request.setAttribute("opencreditbills", js.serialize(new DashBoardDAO().getOpenCreditBills()));
    return mapping.findForward("prescriptionScreen");
  }

  /**
   * Save meal prescriptions.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ParseException the parse exception
   */
  public ActionForward saveMealPrescriptions(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException,
      ParseException {

    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);
    boolean success = true;
    List errorFields = new ArrayList();
    String[] mealName = request.getParameterValues("meal_name");
    String[] newAdded = request.getParameterValues("newAdded");
    String[] delete = request.getParameterValues("delete");
    String[] dietpresID = request.getParameterValues("diet_pres_id");
    FlashScope flashScope = FlashScope.getScope(request);

    List<BasicDynaBean> newPrescribedLists = new ArrayList<BasicDynaBean>();
    List<BasicDynaBean> deletePrescribedLists = new ArrayList<BasicDynaBean>();

    if (mealName != null) {

      for (int i = 0; i < mealName.length; i++) {

        if ((newAdded[i].equals("Y")) && (delete[i].equals("N"))) {

          BasicDynaBean bean = patientDietPrescriptionsDAO.getBean();
          bean.set("diet_pres_id", patientDietPrescriptionsDAO.getNextSequence());
          bean.set("added_to_bill", Boolean.FALSE);
          bean.set("prescribed_time", DateUtil.getCurrentTimestamp());
          bean.set("visit_id", request.getParameter("visit_id"));
          bean.set("prescribed_by", request.getParameter("prescribed_by"));

          ConversionUtils.copyIndexToDynaBean(request.getParameterMap(), i, bean, errorFields);

          logger.debug("Date after setting: " + bean.get("prescribed_by"));
          logger.info("Date after setting: " + bean.get("meal_date"));
          newPrescribedLists.add(bean);
        }

        if ((newAdded[i].equals("N")) && (delete[i].equals("Y"))) {

          BasicDynaBean bean = patientDietPrescriptionsDAO.getBean();
          bean.set("diet_pres_id", Integer.parseInt(dietpresID[i]));

          deletePrescribedLists.add(bean);
        }
      }
      // delete
      for (BasicDynaBean deletePrescribedList : deletePrescribedLists) {

        success = success
            && patientDietPrescriptionsDAO.delete(con, "diet_pres_id",
                deletePrescribedList.get("diet_pres_id"));
      }

      // save
      for (BasicDynaBean newPrescribedList : newPrescribedLists) {

        success = success
            && patientDietPrescriptionsDAO.insert(con, newPrescribedList);
      }
      if (success) {
        DataBaseUtil.commitClose(con, success);
        flashScope.success("Prescription details are saved successfully....");
        flashScope.put("print", "MealPrescriptionPrint");
      } else {
        DataBaseUtil.commitClose(con, success);
        flashScope.error("Fail to save the prescription details....");
      }
    }
    DietaryMasterDAO dao = new DietaryMasterDAO();

    String mealNameAndCharges = dao.getAllMeal();
    request.setAttribute("mealNameAndCharges", mealNameAndCharges);
    request.setAttribute("patientid", request.getParameter("visit_id"));

    ActionRedirect rediActionRedirect = new ActionRedirect(mapping.findForward("prescRedirect"));
    rediActionRedirect.addParameter("patient_id", request.getParameter("visit_id"));
    rediActionRedirect.addParameter("showPrinter", request.getParameter("printerId"));
    rediActionRedirect.addParameter(FlashScope.FLASH_KEY, flashScope.key());
    return (rediActionRedirect);
  }

  /**
   * Prints the prescription.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws ServletException the servlet exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   * @throws DocumentException the document exception
   * @throws TemplateException the template exception
   * @throws XPathExpressionException the x path expression exception
   * @throws TransformerException the transformer exception
   */
  public ActionForward printPrescription(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws ServletException,
      IOException, SQLException, DocumentException, TemplateException, XPathExpressionException,
      TransformerException {
    String visitIdStr = request.getParameter("patient_id");
    String printerIdStr = request.getParameter("printerId");
    BasicDynaBean prefs = null;
    int printerId = 0;
    if ((printerIdStr != null) && !printerIdStr.equals("")) {
      printerId = Integer.parseInt(printerIdStr);
    }

    prefs = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT,
        printerId);

    String printMode = "P";
    if (prefs.get("print_mode") != null) {
      printMode = (String) prefs.get("print_mode");
    }
    PrescribedMealFtlHealper ftlHelper = new PrescribedMealFtlHealper();

    if (printMode.equals("P")) {
      response.setContentType("application/pdf");
      OutputStream os = response.getOutputStream();
      ftlHelper.getPrescriptionFtlReport(visitIdStr, PrescribedMealFtlHealper.return_type.PDF,
          prefs, os);
      os.close();

    } else {
      String textReport = new String(ftlHelper.getPrescriptionFtlReport(visitIdStr,
          PrescribedMealFtlHealper.return_type.TEXT_BYTES, prefs, null));
      request.setAttribute("textReport", textReport);
      request.setAttribute("textColumns", prefs.get("text_mode_column"));
      request.setAttribute("printerType", "DMP");
      return mapping.findForward("textPrintApplet");

    }
    return null;
  }

  /**
   * Gets the dates in range.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the dates in range
   */
  public ActionForward getDatesInRange(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) {
    String fromDate = request.getParameter("fromdate");
    String toDate = request.getParameter("todate");
    DateUtil dateUtil = new DateUtil();
    try {
      List<String> datesList = DateUtil.getDatesInRange(
          dateUtil.getDateFormatter().parse(fromDate), dateUtil.getDateFormatter().parse(toDate),
          "day");
      response.setContentType("text/plain");
      response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
      response.getWriter().write(new JSONSerializer().serialize(datesList));
    } catch (Exception exp) {
      log.error(exp.toString());
    }

    return null;
  }

}
