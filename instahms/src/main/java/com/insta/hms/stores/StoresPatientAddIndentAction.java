package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.Preferences;
import com.bob.hms.common.RequestContext;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.ftl.FtlReportGenerator;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.PrintTemplates.PrintTemplate;
import com.insta.hms.master.PrintTemplates.PrintTemplatesDAO;
import com.insta.hms.outpatient.PatientPrescriptionDAO;
import com.insta.hms.usermanager.UserDAO;
import com.lowagie.text.DocumentException;

import flexjson.JSONSerializer;
import freemarker.core.ParseException;
import freemarker.template.TemplateException;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;


/**
 * The Class StoresPatientAddIndentAction.
 */
public class StoresPatientAddIndentAction extends BaseAction {

  /** The presc DAO. */
  static PatientPrescriptionDAO prescDAO = new PatientPrescriptionDAO();

  /**
   * Addshow.
   *
   * @param am the am
   * @param af the af
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws Exception the exception
   */
  public ActionForward addshow(ActionMapping am, ActionForm af, HttpServletRequest req,
      HttpServletResponse res) throws IOException, ServletException, Exception {
    StoresPatientIndentDAO storePatientIndentDAO = new StoresPatientIndentDAO();
    req.setAttribute("titlePrefix", "Raise");
    req.setAttribute("returns", am.getProperty("category") != null);
    req.setAttribute("genPrefs", GenericPreferencesDAO.getGenericPreferences());
    String patientId = req.getParameter("patient_id");
    String healthAuthority = null;
    List<BasicDynaBean> returnIndentableItems = Collections.emptyList();
    List<BasicDynaBean> returnIndentableBatchItems = Collections.emptyList();
    if (patientId != null && !patientId.equals("")) {
      req.setAttribute("patient", VisitDetailsDAO.getPatientVisitDetailsMap(patientId));
      BasicDynaBean returnIndentDetails = storePatientIndentDAO
          .getPatientReturnIndentableStoreId(patientId);
      if (returnIndentDetails != null) {
        Integer storeId = (Integer) returnIndentDetails.get("store_id");
        BasicDynaBean storeDetails = StoreDAO.findByStore(storeId);
        Integer centerId = (Integer) storeDetails.get("center_id");
        healthAuthority = CenterMasterDAO.getHealthAuthorityForCenter(centerId);
      }
      returnIndentableItems = storePatientIndentDAO
          .getPatientReturnIndentableItems(req.getParameter("patient_id"), healthAuthority);
      returnIndentableBatchItems = new GenericDAO("patient_return_indentable_batch_items")
          .findAllByKey("visit_id", req.getParameter("patient_id"));
    }

    JSONSerializer js = new JSONSerializer().exclude("class");
    req.setAttribute("returnIndentableItems",
        js.deepSerialize(ConversionUtils.listBeanToMapListMap(returnIndentableItems, "store_id")));
    req.setAttribute("returnIndentableBatchItems", js.deepSerialize(
        ConversionUtils.listBeanToMapListMap(returnIndentableBatchItems, "store_id")));
    req.setAttribute("stock_ts", MedicineStockDAO.getMedicineTimestamp());
    req.setAttribute("master_timestamp", PharmacymasterDAO.getItemMasterTimestamp());
    req.setAttribute("doctorDetails", js.serialize(DoctorMasterDAO.getDoctorsandCharges()));
    req.setAttribute("genericNames", js.serialize(ConversionUtils
        .copyListDynaBeansToMap(new GenericDAO("generic_name").listAll(null, "status", "A"))));
    HttpSession session = req.getSession();
    String userName = (String) session.getAttribute("userid");
    req.setAttribute("defaultStore", UserDAO.getUserBean(userName).get("pharmacy_store_id"));
    return am.findForward("addIndent");
  }

  /**
   * Creates the.
   *
   * @param am the am
   * @param af the af
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws Exception the exception
   */
  public ActionForward create(ActionMapping am, ActionForm af, HttpServletRequest req,
      HttpServletResponse res) throws IOException, ServletException, Exception {

    Map reqMap = req.getParameterMap();
    String[] indentItemId = (String[]) reqMap.get("indent_item_id");
    String[] deleted = (String[]) reqMap.get("deleted");
    String[] dispenseStatus = (String[]) reqMap.get("dispense_status");
    String[] itemBatchId = (String[]) reqMap.get("item_batch_id");

    StoresPatientIndentDAO patIndentDAO = new StoresPatientIndentDAO();
    GenericDAO patIndentDetDAO = new GenericDAO("store_patient_indent_details");

    BasicDynaBean patIndentMainBean = patIndentDAO.getBean();
    BasicDynaBean patIndentDetBean = null;
    List<BasicDynaBean> newPatIndentDetList = new ArrayList<BasicDynaBean>();
    List<BasicDynaBean> updatePatIndentDetList = new ArrayList<BasicDynaBean>();
    List<BasicDynaBean> deletePatIndentDetList = new ArrayList<BasicDynaBean>();

    ConversionUtils.copyToDynaBean(reqMap, patIndentMainBean);// main details
    patIndentMainBean.set("visit_id", req.getParameter("visit_id"));
    Object patIndentNo = patIndentMainBean.get("patient_indent_no");
    boolean newIndent = (patIndentNo == null);
    patIndentNo = (patIndentNo == null ? patIndentDAO.getNextIndentNo() : patIndentNo);
    patIndentMainBean.set("patient_indent_no", patIndentNo);
    patIndentMainBean.set("dispense_status",
        patIndentMainBean.get("status").equals("C")
            ? "C"
            : patIndentMainBean.get("dispense_status"));

    if (patIndentMainBean.get("status").equals("F")
        && patIndentMainBean.get("finalized_date") == null) {
      patIndentMainBean.set("finalized_date", DateUtil.getCurrentTimestamp());
      patIndentMainBean.set("finalized_user", RequestContext.getUserName());

    }
    // cancelled indent can not have open dispense status
    String[] medicineId = (String[]) reqMap.get("medicine_id");

    for (int i = 0; i < medicineId.length - 1; i++) {

      patIndentDetBean = patIndentDetDAO.getBean();

      ConversionUtils.copyIndexToDynaBean(reqMap, i, patIndentDetBean);
      patIndentDetBean.set("dispense_status", dispenseStatus[i]);
      if (!itemBatchId[i].equals("")) {
        patIndentDetBean.set("item_batch_id", Integer.parseInt(itemBatchId[i]));
      }
      patIndentDetBean.set("patient_indent_no", patIndentNo);
      patIndentDetBean.set("dispense_status",
          patIndentMainBean.get("status").equals("C")
              ? "C"
              : patIndentDetBean.get("dispense_status"));

      if (deleted[i].equals("Y")) {
        deletePatIndentDetList.add(patIndentDetBean);
      } else {

        if (patIndentDetBean.get("patient_indent_no") == null || indentItemId[i].isEmpty()) {
          newPatIndentDetList.add(patIndentDetBean);
        } else {
          updatePatIndentDetList.add(patIndentDetBean);
        }
      }
    }

    Connection con = null;
    boolean status = true;
    Map keys = new HashMap<String, Object>();
    keys.put("patient_indent_no", patIndentMainBean.get("patient_indent_no"));
    try {

      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      // Insert/Update indent main details
      if (newIndent) {
        status &= patIndentDAO.insert(con, patIndentMainBean);
      } else {
        status &= patIndentDAO.update(con, patIndentMainBean.getMap(), keys) > 0;
      }
      // Insert indent details
      if (newPatIndentDetList.size() > 0) {
        status &= patIndentDetDAO.insertAll(con, newPatIndentDetList);
      }

      // Update indent details

      for (BasicDynaBean indentDet : updatePatIndentDetList) {

        keys = new HashMap<String, Object>();
        keys.put("indent_item_id", indentDet.get("indent_item_id"));

        status &= patIndentDetDAO.update(con, indentDet.getMap(), keys) > 0;
      }

      for (BasicDynaBean deletedBean : deletePatIndentDetList) {
        status &= patIndentDetDAO.delete(con, "indent_item_id", deletedBean.get("indent_item_id"));
      }

      status &= patIndentDAO.updateIndentDispenseStatus(con, req.getParameter("visit_id"));
    } finally {
      DataBaseUtil.commitClose(con, status);
    }

    req.setAttribute("titlePrefix", "Edit");
    ActionRedirect redirect = new ActionRedirect(am.findForward("addRedirect"));
    redirect.addParameter("patient_indent_no", patIndentNo);
    redirect.addParameter("stop_doctor_orders", true);
    return redirect;
  }

  /**
   * Gets the doctor orders.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the doctor orders
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public ActionForward getDoctorOrders(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
    String patientId = request.getParameter("patient_id");
    String storeId = request.getParameter("store_id");

    JSONSerializer js = new JSONSerializer();
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    response.setContentType("application/json");
    response.getWriter().write(js.serialize(
        ConversionUtils.listBeanToListMap(prescDAO.getPrescMedicines(patientId, storeId))));
    response.flushBuffer();

    return null;
  }

  /**
   * Gets the indent print.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the indent print
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws TransformerException the transformer exception
   * @throws TemplateException the template exception
   * @throws DocumentException the document exception
   * @throws XPathExpressionException the x path expression exception
   * @throws ParseException the parse exception
   */
  public ActionForward getIndentPrint(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws SQLException, IOException, TransformerException, TemplateException, DocumentException,
      XPathExpressionException, ParseException {

    String patientIndentNo = request.getParameter("patient_indent_no");
    StoresPatientIndentDAO indentMainDAO = new StoresPatientIndentDAO();
    BasicDynaBean patIndentMain = indentMainDAO.findByKey("patient_indent_no", patientIndentNo);
    Integer storeId = (Integer) patIndentMain.get("indent_store");
    BasicDynaBean storeDetails = StoreDAO.findByStore(storeId);
    Integer centerId = (Integer) storeDetails.get("center_id");
    String healthAuthority = CenterMasterDAO.getHealthAuthorityForCenter(centerId);
    List<BasicDynaBean> patIndentDetList = indentMainDAO.getPatientIndentDetails(patientIndentNo,
        healthAuthority);
    Map map = new HashMap();
    map.put("patIndentMain", patIndentMain);
    map.put("patIndentDetList", patIndentDetList);
    map.put("visitdetails",
        VisitDetailsDAO.getPatientVisitDetailsMap((String) patIndentMain.get("visit_id")));
    map.put("modules_activated",
        ((Preferences) RequestContext.getSession().getAttribute("preferences"))
            .getModulesActivatedMap());
    map.put("title", "Patient Indent Print");

    PrintTemplatesDAO printtemplatedao = new PrintTemplatesDAO();
    String templateContent = printtemplatedao.getCustomizedTemplate(PrintTemplate.PATIENT_INDENT);
    FtlReportGenerator ftlGen = null;
    if (templateContent == null || templateContent.equals("")) {
      ftlGen = new FtlReportGenerator(PrintTemplate.PATIENT_INDENT.getFtlName());
    } else {
      StringReader reader = new StringReader(templateContent);
      ftlGen = new FtlReportGenerator("PatientIndentPrint.ftl", reader);
    }

    StringWriter writer = new StringWriter();
    ftlGen.setReportParams(map);
    ftlGen.process(writer);
    String printContent = writer.toString();
    HtmlConverter hc = new HtmlConverter();
    BasicDynaBean printprefs = PrintConfigurationsDAO
        .getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_STORE);
    if (printprefs.get("print_mode").equals("P")) {
      OutputStream os = response.getOutputStream();
      response.setContentType("application/pdf");
      hc.writePdf(os, printContent, "patientindent", printprefs, false, false, true, true, true,
          false);
    } else {
      String textReport = null;
      textReport = new String(
          hc.getText(printContent, "patientIndentContentText", printprefs, true, true));
      request.setAttribute("textReport", textReport);
      request.setAttribute("textColumns", printprefs.get("text_mode_column"));
      request.setAttribute("printerType", "DMP");
      return mapping.findForward("textPrintApplet");
    }

    return null;

  }

  /**
   * Gets the medicine batch detail.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the medicine batch detail
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public ActionForward getMedicineBatchDetail(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
    String patientId = request.getParameter("patient_id");
    int itemId = Integer.parseInt(request.getParameter("item_id"));

    JSONSerializer js = new JSONSerializer();
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    response.setContentType("application/json");
    response.getWriter().write(js.serialize(ConversionUtils
        .listBeanToListMap(new StoresPatientIndentDAO().getBatchNo(patientId, itemId))));
    response.flushBuffer();

    return null;
  }

  /**
   * Gets the equivalent medicines list.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the equivalent medicines list
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws SQLException the SQL exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getEquivalentMedicinesList(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, SQLException {

    String medicineName = request.getParameter("medicineName");
    String genericName = request.getParameter("genericName");
    String storeId = request.getParameter("storeId");
    String saleType = request.getParameter("saleType");
    /*
     * if allstores is true: it ignores the storeid and search for equivalent medicines from all
     * stores. which is used in op/ip consultation screen.
     */
    Boolean allStores = new Boolean(request.getParameter("allStores"));

    response.setContentType("text/javascript");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    List<BasicDynaBean> medicineNames = MedicineStockDAO.getEquivalentMedicinesList(medicineName,
        genericName, storeId, allStores, saleType);
    JSONSerializer js = new JSONSerializer().exclude("class");
    response.getWriter().write(js.serialize(ConversionUtils.copyListDynaBeansToMap(medicineNames)));
    response.flushBuffer();
    return null;
  }

}
