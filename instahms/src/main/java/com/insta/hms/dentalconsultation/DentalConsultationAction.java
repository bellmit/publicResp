package com.insta.hms.dentalconsultation;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.MimeTypeDetector;
import com.bob.hms.common.RequestContext;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.billing.Bill;
import com.insta.hms.billing.BillBO;
import com.insta.hms.billing.BillDAO;
import com.insta.hms.billing.ChargeDAO;
import com.insta.hms.billing.ChargeDTO;
import com.insta.hms.billing.Receipt;
import com.insta.hms.billing.paymentdetails.AbstractPaymentDetails;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.core.billing.AllocationService;
import com.insta.hms.insurance.SponsorBO;
import com.insta.hms.master.BillPrintTemplate.BillPrintTemplateDAO;
import com.insta.hms.master.CardType.CardTypeMasterDAO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.CommonPrintTemplates.PrintTemplate;
import com.insta.hms.master.CommonPrintTemplates.PrintTemplatesDAO;
import com.insta.hms.master.DiscountAuthorizerMaster.DiscountAuthorizerMasterAction;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.HealthAuthorityPreferences.HealthAuthorityPreferencesDAO;
import com.insta.hms.master.PaymentModes.PaymentModeMasterDAO;
import com.insta.hms.master.PrescriptionsMaster.PrescriptionsMasterDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.ServiceMaster.ServiceMasterDAO;
import com.insta.hms.master.dentalsupplieritemratemaster.DentalSupplierItemRateMasterDAO;
import com.insta.hms.orders.OrderBO;
import com.insta.hms.orders.TestDocumentDTO;
import com.insta.hms.outpatient.CrownStatusesDAO;
import com.insta.hms.outpatient.DentalChartHelperDAO;
import com.insta.hms.outpatient.DoctorConsultationDAO;
import com.insta.hms.outpatient.PatientPrescriptionDAO;
import com.insta.hms.outpatient.RootStatusesDAO;
import com.insta.hms.outpatient.SurfaceMaterialDAO;
import com.insta.hms.outpatient.ToothImageDetails;
import com.insta.hms.outpatient.ToothImageDetails.Tooth;
import com.insta.hms.outpatient.ToothImageDetails.ToothPart;
import com.insta.hms.usermanager.UserDAO;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DentalConsultationAction extends DispatchAction {

  static GenericDAO patientServicePrescriptionsDao = 
      new GenericDAO("patient_service_prescriptions");
  static GenericDAO patientTestPrescriptionsDao = new GenericDAO("patient_test_prescriptions");
  static CrownStatusesDAO crownStatusesDAO = new CrownStatusesDAO();
  static RootStatusesDAO rootStatusesDAO = new RootStatusesDAO();
  static SurfaceMaterialDAO surMatrlDAO = new SurfaceMaterialDAO();
  static DoctorMasterDAO doctorDAO = new DoctorMasterDAO();
  static ToothTreatmentDetailsDao toothTreatmentDetailsDao = new ToothTreatmentDetailsDao();
  static GenericDAO medDosageDao = new GenericDAO("medicine_dosage_master");
  static DoctorConsultationDAO consultDao = new DoctorConsultationDAO();
  static PrescriptionsMasterDAO mmDao = new PrescriptionsMasterDAO();
  static VisitDetailsDAO visitDAO = new VisitDetailsDAO();
  static DentalSuppliesOrderDao orderDAO = new DentalSuppliesOrderDao();
  static DentalSuppliesItemDao itemDAO = new DentalSuppliesItemDao();
  static GenericDAO uUserDao = new GenericDAO("u_user");

  /** The Constant PERMISSIBLE_DISC_CAP. */
  private static final String PERMISSIBLE_DISC_CAP = "permissible_discount_cap";
  
  /** The allocation service. */
  private final AllocationService allocationService = (AllocationService)ApplicationContextProvider
      .getApplicationContext().getBean("allocationService");
  private final PaymentModeMasterDAO paymentModeMasterDao = new PaymentModeMasterDAO();
  
  /**
   * Show Dental Consultation Screen.
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
  
  @IgnoreConfidentialFilters
  public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, IOException, ParseException {
    JSONSerializer js = new JSONSerializer().exclude(".class");

    BasicDynaBean genPrefs = GenericPreferencesDAO.getAllPrefs();
    Boolean maddDisable = false;
    List items = null;
    String toothNumberingSystem = (String) genPrefs.get("tooth_numbering_system");
    ToothImageDetails adultToothImageDetails = DentalChartHelperDAO.getToothImageDetails(true);
    ToothImageDetails pediacToothImageDetails = DentalChartHelperDAO.getToothImageDetails(false);

    request.setAttribute("adult_tooth_numbers", DentalChartHelperDAO
        .getToothNumbersForAdult(toothNumberingSystem, adultToothImageDetails));
    request.setAttribute("pediac_tooth_numbers",
        DentalChartHelperDAO.getToothNumbers(toothNumberingSystem, pediacToothImageDetails));
    // these are used in dental chart include. for displaying the overlay images for tooths.
    request.setAttribute("adult_tooth_image_details", adultToothImageDetails);
    request.setAttribute("adult_tooth_image_details_json",
        js.deepSerialize(adultToothImageDetails));

    request.setAttribute("pediac_tooth_details_json", js.deepSerialize(pediacToothImageDetails));

    String mrNo = request.getParameter("mr_no");
    if (request.getParameter("emptyScreen") == null) {
      Map patient = PatientDetailsDAO.getPatientGeneralDetailsMap(mrNo);
      if (patient == null) {
        FlashScope flash = FlashScope.getScope(request);
        flash.error("Invalid MR. No: " + mrNo);
        ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
        redirect.addParameter("emptyScreen", true);
        redirect.addParameter("ps_status", "active");
        redirect.addParameter("madd_disable", false);
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
        return redirect;
      }
      BasicDynaBean latestConsultation = null;
      String prescByGenerics = "N";
      Integer latestVisitCenter = -1;
      String error = null;
      BasicDynaBean latestvisit = visitDAO.getLatestActiveVisit(mrNo);
      if (latestvisit == null) {
        maddDisable = true;
        error = "No Active Visit found for the patient";
        if (RequestContext.getCenterId() != 0) {
          error += " in this center.." + RequestContext.getCenterName();
        }
      } else {
        latestVisitCenter = (Integer) latestvisit.get("center_id");
        prescByGenerics = (String) HealthAuthorityPreferencesDAO
            .getHealthAuthorityPreferences(
                CenterMasterDAO.getHealthAuthorityForCenter(latestVisitCenter))
            .getPrescriptions_by_generics();
        latestConsultation = DoctorConsultationDAO
            .getLatestConsultation((String) latestvisit.get("patient_id"));
      }
      items = ConversionUtils.copyListDynaBeansToMap(DentalSupplierItemRateMasterDAO.getItems());
      String orgId = "ORG0001";
      String bedType = "GENERAL";

      BasicDynaBean orgIdForIPOP = toothTreatmentDetailsDao.getOpIoOrgId(mrNo);
      if (null != orgIdForIPOP && latestvisit != null) {
        if (latestvisit.get("visit_type").equals("o")) {
          orgId = (String) orgIdForIPOP.get("op_rate_plan_id");
        } else {
          orgId = (String) orgIdForIPOP.get("ip_rate_plan_id");
        }
        if (orgId == null || orgId.equals("")) {
          orgId = "ORG0001";
        }
      }

      request.setAttribute("printTemplate",
          PrintTemplatesDAO.getTemplateNames(PrintTemplate.DentalConsultation.getType()));
      BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();

      List<BasicDynaBean> treatments = toothTreatmentDetailsDao.getTreatmentDetails(
          mrNo, orgId, bedType);
      request.setAttribute("treatments", treatments);

      HashMap serviceSubTasks = ConversionUtils.listBeanToMapListBean(
          toothTreatmentDetailsDao.getServiceSubTasksForPatient(mrNo), "treatment_id");
      request.setAttribute("service_sub_tasks", serviceSubTasks);

      request.setAttribute("users", new UserDAO().getAllActiveUsersDynaList());

      String templateName = request.getParameter("printTemplate");
      String printerId = request.getParameter("printerId");

      if (templateName == null || templateName.equals("")) {
        templateName = (String) genericPrefs.get("default_dental_cons_print_template");
      }
      request.setAttribute("templateName", templateName);

      if (printerId == null || printerId.equals("")) {
        request.setAttribute("printerDef",
            PrintConfigurationsDAO.getPatientDefaultPrintPrefs().get("printer_id"));        
      } else {
        request.setAttribute("printerDef", printerId);
      }

      String prescriptionUsesStores = (String) genericPrefs.get("prescription_uses_stores");
      request.setAttribute("prescriptions",
          PrescriptionsMasterDAO.getPrescriptionsForPatient(mrNo, prescriptionUsesStores));
      request.setAttribute("prescriptions_by_generics",
          prescriptionUsesStores.equals("Y") && prescByGenerics.equals("Y"));
      request.setAttribute("prescriptions_by_generics_js", prescByGenerics);
      request.setAttribute("latestVisit", latestvisit);
      request.setAttribute("latestConsultation", latestConsultation);
      request.setAttribute("latest_visit_centerid_js", latestVisitCenter);
      request.setAttribute("openAndFinalizedUnPaidBillsList",
          BillDAO.getAllOpenAndFinalizedUnPaidBills(mrNo));
      request.setAttribute("openAndFinalizedUnPaidBillsJSON", js.deepSerialize(
          ConversionUtils.listBeanToListMap(BillDAO.getAllOpenAndFinalizedUnPaidBills(mrNo))));
      request.setAttribute("madd_disable", maddDisable);
      request.setAttribute("orgId", orgId);
      request.setAttribute("error", error);
    }
    request.setAttribute("user", js.deepSerialize(
        UserDAO.getRecord((String) request.getSession(false).getAttribute("userid")).getMap()));
    List medDosages = medDosageDao.listAll();
    request.setAttribute("medDosages",
        js.serialize(ConversionUtils.copyListDynaBeansToMap(medDosages)));
    List itemFormList = new GenericDAO("item_form_master").listAll();
    request.setAttribute("itemFormList",
        js.serialize(ConversionUtils.copyListDynaBeansToMap(itemFormList)));

    request.setAttribute("doctors",
        js.deepSerialize(ConversionUtils.listBeanToListMap(DoctorMasterDAO.getDentalDoctors())));
    request.setAttribute("shadesList", js.deepSerialize(
        ConversionUtils.listBeanToListMap(new GenericDAO("dental_shades_master").listAll())));
    request.setAttribute("availableTemplates",
        BillPrintTemplateDAO.getAvailableTemplatesList("P", "N"));
    request.setAttribute("getAllCreditTypes", js.serialize(ConversionUtils
        .copyListDynaBeansToMap(new CardTypeMasterDAO().listAll(null, "status", "A", null))));
    int centerId = (Integer) request.getSession(false).getAttribute("centerId");
    request.setAttribute("screenId", mapping.getProperty("screen_id"));
    List<BasicDynaBean> discAuths = DiscountAuthorizerMasterAction.getDiscountAuthorizers(centerId);
    request.setAttribute("discountAuthorizersJSON",
        js.serialize(ConversionUtils.listBeanToListMap(discAuths)));
    request.setAttribute("discountAuthorizers", discAuths);
    request.setAttribute("items", js.deepSerialize(items));
    List<String> listColumns = new ArrayList<>();
    listColumns.add(PERMISSIBLE_DISC_CAP);
    Map<String, Object> filtermap = new HashMap<>();
    String userId = (String) request.getSession().getAttribute("userid");
    filtermap.put("emp_username", userId);
    BigDecimal permissibleDiscountPercenatge = BigDecimal.ZERO;
    BasicDynaBean userBean = uUserDao.findByKey(listColumns, filtermap);
    if (userBean != null && userBean.get(PERMISSIBLE_DISC_CAP) != null) {
      permissibleDiscountPercenatge = (BigDecimal) userBean.get(PERMISSIBLE_DISC_CAP);
    }
    request.setAttribute("permissibleDiscountPercenatge", permissibleDiscountPercenatge);
    request.setAttribute("cashTransactionLimit",paymentModeMasterDao.getCashLimit());
    return mapping.findForward("show");
  }

  /**
   * Gets the dental chart.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the dental chart
   * @throws ServletException the servlet exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getDentalChart(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException, SQLException {
    BasicDynaBean prefs = GenericPreferencesDAO.getAllPrefs();

    String numberingSystem = (String) prefs.get("tooth_numbering_system");
    String dentalChart = numberingSystem.equals("U") ? "DentalChart_Adult_UNV.png"
        : "DentalChart_Adult_FDI.png";
    FileInputStream stream = new FileInputStream(
        AppInit.getRootRealPath() + "/images/Dental/" + dentalChart);
    response.setContentType(MimeTypeDetector.getMimeTypes(stream).toString());
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    OutputStream os = response.getOutputStream();
    os.write(DataBaseUtil.readInputStream(
        new FileInputStream(AppInit.getRootRealPath() + "/images/Dental/" + dentalChart)));
    os.flush();
    os.close();

    return null;

  }

  /**
   * Gets the dental chart marker image.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the dental chart marker image
   * @throws ServletException the servlet exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   */
  public ActionForward getDentalChartMarkerImage(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException, SQLException, ParseException {
    String imageName = "";
    String toothNumber = request.getParameter("dc_unv_number");
    String statusId = request.getParameter("dc_status_id");
    String materialId = request.getParameter("dc_material_id");
    String toothPart = request.getParameter("dc_tooth_part");
    String mrNo = request.getParameter("mr_no");

    ToothImageDetails desc = DentalChartHelperDAO.getToothImageDetails(true);
    for (Map.Entry<String, Tooth> entry : desc.getTeeth().entrySet()) {
      if (toothNumber.equals(entry.getKey())) {

        for (Map.Entry<String, ToothPart> tpEntry : entry.getValue().getToothPart().entrySet()) {
          ToothPart part = tpEntry.getValue();
          if (tpEntry.getKey().equals(toothPart)) {
            imageName = part.getImage_name();
            break;
          }
        }
      }
    }

    BasicDynaBean bean = null;
    if (toothPart.equals("crown")) {
      bean = crownStatusesDAO.findByKey("crown_status_id", Integer.parseInt(statusId));

    } else if (toothPart.equals("root")) {
      bean = rootStatusesDAO.findByKey("root_status_id", Integer.parseInt(statusId));
    } else if (!materialId.equals("")) {
      bean = surMatrlDAO.findByKey("material_id", Integer.parseInt(materialId));
    }

    String fileName = "adult/" + toothNumber + "/" + imageName;
    BufferedImage image = ImageIO
        .read(new File(AppInit.getRootRealPath() + "/images/Dental/" + fileName));
    if (bean != null) {
      String colorCode = (String) bean.get("color_code");
      if (toothPart.equals("root")) {
        image = DentalChartHelperDAO.changeColor(image,
            DentalChartHelperDAO.stringToColor("#F7E7BB"),
            DentalChartHelperDAO.stringToColor(colorCode.toUpperCase()));
      } else {
        image = DentalChartHelperDAO.changeColor(image,
            DentalChartHelperDAO.stringToColor("#FFFFFF"),
            DentalChartHelperDAO.stringToColor(colorCode.toUpperCase()));
      }
    }

    response.setContentType("image/png");
    OutputStream os = response.getOutputStream();
    ImageIO.write(image, "png", os);
    os.flush();
    return null;
  }

  /**
   * Gets the services.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the services
   * @throws SQLException the SQL exception
   * @throws ServletException the servlet exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @IgnoreConfidentialFilters
  public ActionForward getServices(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws SQLException, ServletException, IOException {
    response.setContentType("text/plain");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

    JSONSerializer js = new JSONSerializer().exclude("class");
    String query = request.getParameter("query");
    String orgId = request.getParameter("org_id");
    String bedType = request.getParameter("bed_type");
    bedType = bedType.equals("") ? "GENERAL" : bedType;

    List servicesList = ServiceMasterDAO.getServices(bedType, orgId, query);
    HashMap retVal = new HashMap();
    retVal.put("result", ConversionUtils.copyListDynaBeansToMap(servicesList));

    js.deepSerialize(retVal, response.getWriter());
    response.flushBuffer();
    return null;
  }

  /**
   * Gets the dental supplies.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the dental supplies
   * @throws SQLException the SQL exception
   * @throws ServletException the servlet exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @IgnoreConfidentialFilters
  public ActionForward getDentalSupplies(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws SQLException, ServletException, IOException {
    response.setContentType("text/plain");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

    JSONSerializer js = new JSONSerializer().exclude("class");
    String serviceId = request.getParameter("service_id");
    String treatmentId = request.getParameter("treatment_id");

    response.getWriter().write(js.serialize(ConversionUtils
        .listBeanToListMap(DentalSuppliesItemDao.getDentalSupplies(serviceId, treatmentId))));
    response.flushBuffer();
    return null;
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
   * @throws ServletException the servlet exception
   * @throws ParseException the parse exception
   */
  public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response)
      throws SQLException, IOException, ServletException, ParseException {

    Map params = request.getParameterMap();
    List errorFields = new ArrayList();
    String[] serviceIds = request.getParameterValues("h_service_id");
    String mrNo = request.getParameter("mr_no");
    String orgId = request.getParameter("org_id_rate");

    BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
    request.setAttribute("genPrefs", genericPrefs);
    String toothNumberingSystem = (String) genericPrefs.get("tooth_numbering_system");
    String dentalChartPreference = (String) genericPrefs.get("dental_chart");
    toothNumberingSystem = toothNumberingSystem == null || toothNumberingSystem.equals("") ? "U"
        : toothNumberingSystem;

    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);
    boolean success = true;
    String error = null;
    try {
      txn: {
        success = false;
        for (int serviceIndex = 0; serviceIndex < serviceIds.length - 1; serviceIndex++) {
          BasicDynaBean bean = toothTreatmentDetailsDao.getBean();
          ConversionUtils.copyIndexToDynaBeanPrefixed(params,
              serviceIndex, bean, errorFields, "h_");
          bean.set("mr_no", mrNo);
          bean.set("mod_user", (String) request.getSession(false).getAttribute("userid"));
          bean.set("mod_time", new Timestamp(new java.util.Date().getTime()));
          String treatmentStatus = request.getParameterValues("h_treatment_status")[serviceIndex];
          if (treatmentStatus.equals("X")) {
            bean.set("cancelled_by", (String) request.getSession(false).getAttribute("userid"));
            bean.set("cancelled_date", new Timestamp(new java.util.Date().getTime()));
          } else {
            bean.set("cancelled_by", null);
            bean.set("cancelled_date", null);
          }
          Boolean delete = new Boolean(request.getParameterValues("ht_delete")[serviceIndex]);
          Boolean edited = bean.get("treatment_id") != null;
          String orderIndex = request.getParameterValues("h_order_index")[serviceIndex];
          String[] supplierIds = request.getParameterValues("h_" + orderIndex + "_supplier_id");
          String[] itemIds = request.getParameterValues("h_" + orderIndex + "_item_id");
          String serviceIdx = request.getParameterValues("h_service_idx")[serviceIndex];

          String action = "";
          if (errorFields.isEmpty()) {
            if (delete) {
              action = "delete";
              if (!toothTreatmentDetailsDao.delete(con, "treatment_id", bean.get("treatment_id"))) {
                error = "Failed to delete treatment details...";
                break txn;
              }
            } else if (edited) {
              action = "edit";
              if (toothTreatmentDetailsDao.update(con, bean.getMap(), "treatment_id",
                  bean.get("treatment_id")) == 0) {
                error = "Failed to update the treatment details...";
                break txn;
              }
            } else {
              action = "add";
              int treatmentId = toothTreatmentDetailsDao.getNextSequence();
              bean.set("org_id", orgId);
              bean.set("treatment_id", treatmentId);
              if (!toothTreatmentDetailsDao.insert(con, bean)) {
                error = "Failed to insert the treatment details";
                break txn;
              }

            }
            if (!updateServiceSubTasks(con, request, Integer.parseInt(serviceIdx),
                (Integer) bean.get("treatment_id"), action)) {
              error = "Failed to update the Service Sub Tasks Details";
              break txn;
            }
            if (!updateDentalSuppliesDetails(con, params, mrNo, orderIndex,
                (Integer) bean.get("treatment_id"), delete, bean.get("planned_by").toString(),
                supplierIds, itemIds)) {
              error = "Failed to insert/update/delete Dental Supplies Details";
              break txn;
            }
          } else {
            error = "Incorrectly formatted values supplied";
            break txn;
          }
        }

        String[] prescribedIds = request.getParameterValues("item_prescribed_id");
        String[] itemNames = request.getParameterValues("item_name");
        String[] itemIds = request.getParameterValues("item_id");
        String[] adminStrengths = request.getParameterValues("admin_strength");
        String[] medFrequencies = request.getParameterValues("frequency");
        String[] durations = request.getParameterValues("duration");
        String[] durationUnits = request.getParameterValues("duration_units");
        String[] medQty = request.getParameterValues("medicine_quantity");
        String[] itemRemarks = request.getParameterValues("item_remarks");
        String[] ispackage = request.getParameterValues("ispackage");
        String[] delItems = request.getParameterValues("delItem");
        String[] itemType = request.getParameterValues("itemType");
        String[] itemMaster = request.getParameterValues("item_master");
        String[] routeOfAdmin = request.getParameterValues("route_id");
        //strength is the dosage to the patient how much to take (1 tab or 1/2 tab or 5ml or 10ml)
        String[] medicineStrengths = request.getParameterValues("strength");
        String[] genericCode = request.getParameterValues("generic_code");
        String[] itemFormIds = request.getParameterValues("item_form_id");
        //item_strength is the medicine strength (100mg, 200mg, 500ml etc.,)
        String[] itemStrengths = request.getParameterValues("item_strength");
        String[] itemStrengthUnits = request.getParameterValues("item_strength_units");
        String[] consUomId = request.getParameterValues("cons_uom_id");
        String[] prescByGenerics = request.getParameterValues("presc_by_generics");

        String useStoreItems = (String) genericPrefs.get("prescription_uses_stores");
        int visitCenterId = 0;
        BasicDynaBean visitDetailsBean = new GenericDAO("patient_registration").findByKey("mr_no",
            mrNo);
        if (visitDetailsBean != null) {
          visitCenterId = (Integer) visitDetailsBean.get("center_id");
        }

        GenericDAO medicineDao = new GenericDAO(
            useStoreItems.equals("Y") ? "patient_medicine_prescriptions"
                : "patient_other_medicine_prescriptions");
        String userName = (String) request.getSession(false).getAttribute("userid");
        if (prescribedIds != null) {
          for (int prescIdIndex = 0; prescIdIndex < prescribedIds.length - 1; prescIdIndex++) {
            boolean deleteItem = new Boolean(delItems[prescIdIndex]);
            String prescribedId = prescribedIds[prescIdIndex];
            int itemPrescriptionId = 0;
            BasicDynaBean itemBean = null;
            PatientPrescriptionDAO patPrescDAO = new PatientPrescriptionDAO();
            if (prescribedIds[prescIdIndex].equals("_")) {
              BasicDynaBean bean = patPrescDAO.getBean();
              itemPrescriptionId = patPrescDAO.getNextSequence();

              bean.set("patient_presc_id", itemPrescriptionId);
              bean.set("status", "P");
              bean.set("presc_type", itemType[prescIdIndex]);
              bean.set("mr_no", mrNo);
              bean.set("store_item", genericPrefs.get("prescription_uses_stores").equals("Y"));

              if (!patPrescDAO.insert(con, bean)) {
                break txn;
              }                
            } else {
              itemPrescriptionId = Integer.parseInt(prescribedIds[prescIdIndex]);
              if (deleteItem && !patPrescDAO.delete(con, "patient_presc_id", itemPrescriptionId)) {
                break txn;
              }
            }

            if (itemType[prescIdIndex].equals("Medicine")) {
              itemBean = medicineDao.getBean();
              String prescribedIdColName = "";
              String duration = durations[prescIdIndex];
              String medicineQuantity = medQty[prescIdIndex];
              if (!duration.equals("")) {
                itemBean.set("duration", Integer.parseInt(duration));
                itemBean.set("duration_units", durationUnits[prescIdIndex]);
              }
              if (!medicineQuantity.equals("")) {
                itemBean.set("medicine_quantity", Integer.parseInt(medicineQuantity));
              }
              if (useStoreItems.equals("Y")) {
                if (prescByGenerics[prescIdIndex].equals("N")) {
                  // instead of checking preference only check for itemId.
                  itemBean.set("medicine_id", Integer.parseInt(itemIds[prescIdIndex]));
                }
                // update the generic_code always when pharmacy module is enabled
                itemBean.set("generic_code", genericCode[prescIdIndex]);
                prescribedIdColName = "op_medicine_pres_id";
              } else {
                itemBean.set("medicine_name", itemNames[prescIdIndex]);
                prescribedIdColName = "prescription_id";
              }
              itemBean.set("admin_strength", adminStrengths[prescIdIndex]);
              itemBean.set("frequency", medFrequencies[prescIdIndex]);
              itemBean.set("medicine_remarks", itemRemarks[prescIdIndex]);
              itemBean.set("strength", medicineStrengths[prescIdIndex]);
              itemBean.set("item_strength", itemStrengths[prescIdIndex]);
              if (!"".equals(consUomId[prescIdIndex])) {
                itemBean.set("consumption_uom", Integer.parseInt(consUomId[prescIdIndex]));
              }
              if (!itemStrengthUnits[prescIdIndex].equals("")) {
                itemBean.set("item_strength_units",
                    Integer.parseInt(itemStrengthUnits[prescIdIndex]));
              }                
              if (!itemFormIds[prescIdIndex].equals("")) {
                itemBean.set("item_form_id", Integer.parseInt(itemFormIds[prescIdIndex]));
              }                
              if (!routeOfAdmin[prescIdIndex].equals("")) {
                itemBean.set("route_of_admin", Integer.parseInt(routeOfAdmin[prescIdIndex]));
              }
              if (prescribedId.equals("_")) {
                if (!useStoreItems.equals("Y")
                    && !PrescriptionsMasterDAO.medicineExisits(itemNames[prescIdIndex])) {
                  BasicDynaBean presMedMasterBean = mmDao.getBean();
                  presMedMasterBean.set("medicine_name", itemNames[prescIdIndex]);
                  presMedMasterBean.set("status", "A");

                  if (!mmDao.insert(con, presMedMasterBean)) {
                    break txn;
                  }                    
                }

                itemBean.set(prescribedIdColName, itemPrescriptionId);
                if (!medicineDao.insert(con, itemBean)) {
                  break txn;
                }
              } else {
                if (deleteItem) {
                  if (!medicineDao.delete(con, prescribedIdColName, itemPrescriptionId)) {
                    break txn;
                  }                    
                } else {
                  Map keys = new HashMap();
                  keys.put(prescribedIdColName, itemPrescriptionId);
                  itemBean.set("mod_time", new Timestamp(new java.util.Date().getTime()));
                  if (medicineDao.update(con, itemBean.getMap(), keys) <= 0) {
                    break txn;
                  }                    
                }
              }
            } else if (itemType[prescIdIndex].equals("Inv.")) {
              itemBean = patientTestPrescriptionsDao.getBean();
              itemBean.set("test_id", itemIds[prescIdIndex]);
              itemBean.set("test_remarks", itemRemarks[prescIdIndex]);
              itemBean.set("ispackage", new Boolean(ispackage[prescIdIndex]));
              itemBean.set("username", userName);

              if (prescribedId.equals("_")) {
                itemBean.set("op_test_pres_id", itemPrescriptionId);
                if (!patientTestPrescriptionsDao.insert(con, itemBean)) {
                  break txn;
                }
              } else {
                if (deleteItem) {
                  if (!patientTestPrescriptionsDao.delete(con,
                      "op_test_pres_id", itemPrescriptionId)) {
                    break txn;
                  }                    
                } else {
                  Map keys = new HashMap();
                  keys.put("op_test_pres_id", itemPrescriptionId);
                  itemBean.set("mod_time", new Timestamp(new java.util.Date().getTime()));
                  if (patientTestPrescriptionsDao.update(con, itemBean.getMap(), keys) <= 0) {
                    break txn;
                  }
                }
              }
            } else if (itemType[prescIdIndex].equals("Service")) {
              itemBean = patientServicePrescriptionsDao.getBean();
              itemBean.set("service_id", itemIds[prescIdIndex]);
              itemBean.set("service_remarks", itemRemarks[prescIdIndex]);
              itemBean.set("username", userName);

              if (prescribedId.equals("_")) {
                itemBean.set("op_service_pres_id", itemPrescriptionId);
                if (!patientServicePrescriptionsDao.insert(con, itemBean)) {
                  break txn;
                }

              } else {
                if (deleteItem) {
                  if (!patientServicePrescriptionsDao.delete(con,
                      "op_service_pres_id", itemPrescriptionId)) {
                    break txn;
                  }
                } else {
                  Map keys = new HashMap();
                  keys.put("op_service_pres_id", itemPrescriptionId);
                  itemBean.set("mod_time", new Timestamp(new java.util.Date().getTime()));
                  if (patientServicePrescriptionsDao.update(con, itemBean.getMap(), keys) <= 0) {
                    break txn;
                  }
                }
              }
            }
          }
        }
        if (dentalChartPreference.equals("Y")) {
          error = new PatientDentalConditionDao().recordPatDentalCondition(con,
              request.getParameterMap());
          if (error != null) {
            break txn;
          }            
        }

        success = true;
      }
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    FlashScope flash = FlashScope.getScope(request);
    if (error != null) {
      flash.error(error);
    } else {
      Boolean isPrint = new Boolean(request.getParameter("isPrint"));
      if (isPrint) {
        ActionRedirect printRedirect = new ActionRedirect(mapping.findForward("printConsultation"));
        printRedirect.addParameter("mr_no", mrNo);
        printRedirect.addParameter("printerId", request.getParameter("printerId"));
        printRedirect.addParameter("printTemplate", request.getParameter("printTemplate"));

        List<String> printURLs = new ArrayList<String>();
        printURLs.add(request.getContextPath() + printRedirect.getPath());
        request.getSession(false).setAttribute("printURLs", printURLs);
      }
    }
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
    redirect.addParameter("mr_no", mrNo);
    redirect.addParameter("printerId", request.getParameter("printerId"));
    redirect.addParameter("printTemplate", request.getParameter("printTemplate"));
    redirect.addParameter("ps_status", "active");
    redirect.addParameter("madd_disable", false);
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;
  }

  private boolean updateServiceSubTasks(Connection con, HttpServletRequest request, int index,
      int treatmentId, String action)
      throws ServletException, SQLException, IOException, ParseException {

    String[] taskIds = request.getParameterValues("sub_task_id_" + index);
    String[] taskTreatmentIds = request.getParameterValues("st_treatment_id_" + index);
    String[] taskCompletedBys = request.getParameterValues("task_completed_by_" + index);
    String[] taskComplDates = request.getParameterValues("st_completion_date_" + index);
    String[] taskComplTimes = request.getParameterValues("st_completion_time_" + index);
    String[] taskStatuses = request.getParameterValues("st_status_" + index);
    String[] taskPrescIds = request.getParameterValues("task_presc_id_" + index);

    GenericDAO taskDAO = new GenericDAO("services_presc_tasks");
    for (int j = 0; j < taskPrescIds.length; j++) {
      if (taskIds[j].equals("_")) {
        continue;
      }        

      BasicDynaBean bean = taskDAO.getBean();
      bean.set("treatment_id", treatmentId);
      bean.set("sub_task_id", Integer.parseInt(taskIds[j]));
      String taskCompletedBy = "";

      Timestamp completionTime = null;
      if (taskStatuses[j].equals("C")) {
        completionTime = DateUtil.parseTimestamp(taskComplDates[j], taskComplTimes[j]);
        taskCompletedBy = taskCompletedBys[j];
      }
      bean.set("completed_by", taskCompletedBy);
      bean.set("completion_time", completionTime);
      bean.set("status", taskStatuses[j]);

      if (action.equals("add")) {
        bean.set("task_presc_id", taskDAO.getNextSequence());
        if (!taskDAO.insert(con, bean)) {
          return false;
        }
      } else if (action.equals("edit")) {
        if (taskPrescIds[j].equals("_")) {
          // sub tasks added in this transaction.
          bean.set("task_presc_id", taskDAO.getNextSequence());

          if (!taskDAO.insert(con, bean)) {
            return false;
          }
        } else {
          if (taskDAO.update(con, bean.getMap(), "task_presc_id",
              Integer.parseInt(taskPrescIds[j])) == 0) {
            return false;
          }
        }
      } else if (action.equals("delete")) {
        if (taskDAO.findByKey(con, "treatment_id", treatmentId) != null) {
          if (!taskDAO.delete(con, "treatment_id", treatmentId)) {
            return false;
          }
        }
      }
    }
    return true;

  }

  private boolean updateDentalSuppliesDetails(Connection con, Map params, String mrNo,
      String orderIndex, Integer treatmentId, boolean delete, String plannedBy,
      String[] supplierIds, String[] itemIds) throws SQLException, IOException {
    boolean flag = false;

    txn: {
      if (supplierIds != null && supplierIds.length > 0) {
        // if user do not edit treatment then no need to do anything.
        if (!deleteDentalSupplies(con, mrNo, treatmentId)) {
          break txn;
        }

          if (!insertDentalSupplierAndSupplies(con, params, orderIndex, delete, supplierIds,
              itemIds, treatmentId, plannedBy, mrNo)) {
            break txn;            
          }
        } else { 
          // without doing edit if user delete treatment then 
          // related dental supplies will also deleted.
          if (delete) {
            if (!deleteDentalSupplies(con, mrNo, treatmentId)) {
              break txn;            
            }
          }
        }
        flag = true;
      }
    return flag;
  }

  private boolean insertDentalSupplierAndSupplies(Connection con, Map params, String orderIndex,
      boolean delete, String[] supplierIds, String[] itemIds, Integer treatmentId, String plannedBy,
      String mrNo) throws SQLException, IOException {
    Set<String> temp = new HashSet<String>();
    BasicDynaBean supplierBean = null;
    for (int i = 0; i < supplierIds.length; i++) {
      if (delete) {
        continue;
      }        
      if (supplierIds[i] != null && !supplierIds[i].equals("")) {
        if (!temp.contains(supplierIds[i])) {
          temp.add(supplierIds[i]);
          supplierBean = orderDAO.getBean();
          if (!insertDentalSuppliers(con, params, orderIndex, supplierBean, treatmentId, plannedBy,
              mrNo, i)) {
            return false;
          }
        }
        if (itemIds != null) {
          if (!insertDentalSupplies(con, params, orderIndex, supplierBean, i)) {
            return false;
          }
        }
      }
    }
    return true;
  }

  private boolean insertDentalSuppliers(Connection con, Map params, String orderIndex,
      BasicDynaBean supplierBean, Integer treatmentId, String plannedBy, String mrNo, int index)
      throws SQLException, IOException {
    List errorFields = new ArrayList();
    ConversionUtils.copyIndexToDynaBeanPrefixed(params, index, supplierBean, errorFields,
        "h_" + orderIndex + "_");
    if (errorFields.isEmpty()) {
      int suppliesOrderId = orderDAO.getNextSequence();
      supplierBean.set("supplies_order_id", suppliesOrderId);
      supplierBean.set("treatment_id", treatmentId);
      supplierBean.set("supplies_order_status", "O");// it is always ordered status.
      supplierBean.set("ordered_by", plannedBy);
      supplierBean.set("ordered_date", new Date(new java.util.Date().getTime()));
      supplierBean.set("mr_no", mrNo);
      if (!orderDAO.insert(con, supplierBean)) {
        return false;
      }
    } else {
      return false;
    }
    return true;
  }

  private boolean insertDentalSupplies(Connection con, Map params, String orderIndex,
      BasicDynaBean supplierBean, int index) throws SQLException, IOException {
    List errorFields = new ArrayList();
    BasicDynaBean itemBean = itemDAO.getBean();
    ConversionUtils.copyIndexToDynaBeanPrefixed(params, index, itemBean, errorFields,
        "h_" + orderIndex + "_");
    if (errorFields.isEmpty()) {
      itemBean.set("supplies_order_id",
          supplierBean != null ? (Integer) supplierBean.get("supplies_order_id") : -1);
      itemBean.set("supplies_order_item_id", itemDAO.getNextSequence());
      if (!itemDAO.insert(con, itemBean)) {
        return false;
      }
    } else {
      return false;
    }
    return true;
  }

  private boolean deleteDentalSupplies(Connection con, String mrNo, Integer treatmentId)
      throws SQLException {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("mr_no", mrNo);
    map.put("treatment_id", treatmentId);
    if (orderDAO.findByKey(con, map) != null) {
      if (itemDAO.itemsExists(con, mrNo, treatmentId)
          && !itemDAO.deleteItems(con, mrNo, treatmentId)) {
        return false;
      }
      if (!orderDAO.delete(con, "treatment_id", treatmentId)) {
        return false;
      }
    }
    return true;
  }

  static boolean duplicatesCheck(String[] supplierIds) {
    boolean duplicates = false;
    for (int j = 0; j < supplierIds.length; j++) {
      for (int k = j + 1; k < supplierIds.length; k++) {
        if (k != j && supplierIds[k].equals(supplierIds[j])) {
          duplicates = true;          
        }        
      }      
    }

    return duplicates;
  }

  /**
   * Order service and finalized bill.
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
  public ActionForward orderServiceAndFinalizedBill(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws SQLException, IOException, Exception {
    String error = null;
    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);
    String billNo = null;
    String mrNo = request.getParameter("mr_no");
    String userId = (String) request.getSession(false).getAttribute("userid");
    String[] serviceIds = request.getParameterValues("order_this_service");
    String[] treatmentIds = request.getParameterValues("order_treatment_id");
    String[] orderQtys = request.getParameterValues("order_qty");
    String[] toothNumbers = request.getParameterValues("tooth_number");
    String[] conductingDoctor = request.getParameterValues("conducting_doctor");
    String[] plannedByDoctor = request.getParameterValues("planned_by_doctor");
    String[] rates = request.getParameterValues("rate");
    String[] qtys = request.getParameterValues("qty");
    String[] discs = request.getParameterValues("disc");
    String[] amts = request.getParameterValues("amt");
    String[] overallDiscountAuthName = request.getParameterValues("overall_discount_auth_name");
    String[] overallDiscountAuth = request.getParameterValues("overall_discount_auth");
    String[] overallDiscountAmt = request.getParameterValues("overall_discount_amt");
    String discAuths = request.getParameter("discAuth");
    String latestVisitId = null;

    boolean success = true;
    try {
      txn: {
        if (serviceIds != null && !serviceIds.equals("")) {
          success = false;
          GenericDAO servDao = new GenericDAO("services_prescribed");
          ServiceMasterDAO serviceMasterDao = new ServiceMasterDAO();
          BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
          BasicDynaBean latestvisit = visitDAO.getLatestActiveVisit(mrNo);
          if (latestvisit != null) {
            latestVisitId = (String) latestvisit.get("patient_id");
          }

          if (latestvisit == null) {
            error = "No Active Visit found for the patient";
            if (RequestContext.getCenterId() != 0) {
              error += " in this center.." + RequestContext.getCenterName();
            }
            break txn;
          }
          Bill bill = BillDAO.getVisitCreditBill((String) latestvisit.get("patient_id"), true);
          Timestamp dtTime = DataBaseUtil.getDateandTime();
          OrderBO orderBo = new OrderBO();

          if (bill != null && bill.getPaymentStatus().equals("U")) {
            billNo = bill.getBillNo();
          }
          error = orderBo.setBillInfo(con, (String) latestvisit.get("patient_id"), billNo, false,
              (String) request.getSession(true).getAttribute("userid"), "C");
          if (error != null) {
            break txn;            
          }

          billNo = (orderBo.getBill() != null)
              ? (String) ((BasicDynaBean) orderBo.getBill()).get("bill_no")
              : null;

          BillDAO billDAO = new BillDAO(con);
          Bill billObj = billDAO.getBill(billNo);

          for (int i = 0; i < serviceIds.length - 1; i++) {
            if (serviceIds[i] != null && !serviceIds[i].isEmpty()) {
              BasicDynaBean bean = null;
              bean = servDao.getBean();
              bean.set("service_id", serviceIds[i]);
              bean.set("presc_date", dtTime);
              Boolean conduction = 
                  (Boolean) serviceMasterDao.findByKey("service_id", serviceIds[i])
                    .get("conduction_applicable");
              if (!conduction) {
                bean.set("conducted", "C");
              } else {
                bean.set("conducted", "N");
              }
              String toothNumberingSystem = (String) genericPrefs.get("tooth_numbering_system");
              if (toothNumberingSystem.equals("U")) {
                bean.set("tooth_unv_number", toothNumbers[i]);
              } else {
                bean.set("tooth_fdi_number", toothNumbers[i]);
              }
              bean.set("doctor_id", plannedByDoctor[i]);
              bean.set("remarks", "Dental Service");
              BigDecimal orderQty = BigDecimal.ONE;
              if (orderQtys[i] != null && !orderQtys[i].equals("")) {
                orderQty = new BigDecimal(orderQtys[i]);
              }

              bean.set("quantity", orderQty);

              List<BasicDynaBean> orders = new ArrayList<BasicDynaBean>();
              orders.add(bean);
              List<String> firstOfCategoryList = new ArrayList<String>();
              List<String> condDoctrsList = new ArrayList<String>();
              List<String> newPreAuths = new ArrayList<String>();
              List<Integer> newPreAuthModes = new ArrayList<Integer>();
              List<String> newSecPreAuths = new ArrayList<String>();
              List<Integer> newSecPreAuthModes = new ArrayList<Integer>();
              List<List<TestDocumentDTO>> testDocList = new ArrayList<List<TestDocumentDTO>>();
              String conductingDoctorStr = conductingDoctor[i];
              if (conductingDoctorStr != null && !conductingDoctorStr.equals("")) {
                condDoctrsList.add(conductingDoctorStr);                
              }
              // Order each service as separate order item.
              boolean flag = (orderBo.orderItems(con, orders, newPreAuths, newPreAuthModes,
                  firstOfCategoryList, condDoctrsList, null, null, null, null, 0, true,
                  newSecPreAuths, newSecPreAuthModes, null, testDocList) == null);

              if (flag) {
                BasicDynaBean toothTrtBean = toothTreatmentDetailsDao.getBean();
                toothTrtBean.set("service_prescribed_id", bean.get("prescription_id"));
                if (toothTreatmentDetailsDao.update(con, toothTrtBean.getMap(), "treatment_id",
                    Integer.parseInt(treatmentIds[i])) != 1) {
                  error = "Failed to update the service prescription id";
                  break txn;
                }
                // Applying Item discount.
                if (billObj != null && billObj.getBillDiscountCategory() == 0) {
                  // discount rule applied already
                  ChargeDAO chargeDao = new ChargeDAO(con);
                  String chargeId = toothTreatmentDetailsDao.getChargeId(
                      con, (Integer) bean.get("prescription_id"),
                      (String) bean.get("service_id"));
                  if (chargeId != null && !chargeId.isEmpty()) {
                    ChargeDTO curCharge = chargeDao.getCharge(chargeId);
                    curCharge.setActRate(new BigDecimal(rates[i]));
                    curCharge.setAmount(new BigDecimal(amts[i]));
                    curCharge.setDiscount(new BigDecimal(discs[i]));
                    curCharge.setOverall_discount_auth_name(overallDiscountAuthName[i]);
                    curCharge.setOverall_discount_auth(overallDiscountAuth[i]);
                    curCharge.setOverall_discount_amt(new BigDecimal(overallDiscountAmt[i]));

                    chargeDao.updateChargeAmounts(curCharge);
                  }
                }
              } else {
                error = "Failed to order the Service..";
                break txn;
              }
            }
          }
          // Update the bill.
          if (discAuths != null && !discAuths.isEmpty()) {
            billObj.setBillDiscountAuth(Integer.parseInt(discAuths));
            if (!billDAO.updateBill(billObj)) {
              error = "Failed to update bill...";
              break txn;
            }
          }
          /*
           * Update the bill status as finalized.
           */
          error = new BillBO().updateBillStatus(con, billObj, Bill.BILL_STATUS_FINALIZED,
              billObj.getPaymentStatus(), billObj.getOkToDischarge(),
              DateUtil.getCurrentTimestamp(), userId, false, false, false);
          if (error != null && !error.equals("")) {
            break txn;
          }
          success = true;
        } else {
          error = "Please Select Atleast one service for order and finalized Bill.";
        }
      }
    } catch (Exception ex) {
      success = false;
      throw ex;
    } finally {
      DataBaseUtil.commitClose(con, success);
      /*
       * this will taken care of all recalculation for bill.
       */
      if (success && billNo != null && !billNo.equals("")) {
        BillDAO.resetTotalsOrReProcess(billNo, false);
      }        

      new SponsorBO().recalculateSponsorAmount(latestVisitId);
    }
    FlashScope flash = FlashScope.getScope(request);
    if (error != null) {
      flash.error(error);
    }
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
    redirect.addParameter("mr_no", mrNo);
    redirect.addParameter("printerId", request.getParameter("printerId"));
    redirect.addParameter("printTemplate", request.getParameter("printTemplate"));
    redirect.addParameter("ps_status", "active");
    redirect.addParameter("madd_disable", false);
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;

  }

  /**
   * Pay and print bill.
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
  public ActionForward payAndPrintBill(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws SQLException, IOException, Exception {
    String error = null;
    Map requestParams = request.getParameterMap();
    String userId = (String) request.getSession(false).getAttribute("userid");
    String mrNo = request.getParameter("mr_no");
    String billNo = request.getParameter("billNo");
    BillBO billBOObj = new BillBO();
    List<Receipt> receiptList = null;
    String newStatus = null;
    String paymentStatus = null;
    String origStatus = null;

    ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
    try {
      txn: {
        Bill bill = billBOObj.getBill(billNo);
        origStatus = bill.getStatus();
        AbstractPaymentDetails bpImpl = AbstractPaymentDetails
            .getReceiptImpl(AbstractPaymentDetails.BILL_PAYMENT);
        /*
         * Build receipts if any payments done.
         */
        receiptList = bpImpl.processReceiptParams(requestParams);

        List insertBillChargeList = new ArrayList();
        List updateBillChargeList = new ArrayList();

        if (!billBOObj.updateBillDetails(bill, bill.getStatus(), updateBillChargeList,
            insertBillChargeList, receiptList, false, null, false, null, null)) {
          error = "Failed to update Bill Details...";
          break txn;
        }

        Bill billStUpObj = billBOObj.getBill(billNo);
        BigDecimal result = billStUpObj.getTotalAmount().setScale(2)
            .subtract(billStUpObj.getTotalReceipts().setScale(2));
        if (result.compareTo(BigDecimal.ZERO) == 0) {
          newStatus = billStUpObj.BILL_STATUS_CLOSED;
          paymentStatus = billStUpObj.BILL_PAYMENT_PAID;
        } else {
          newStatus = billStUpObj.getStatus();
          paymentStatus = billStUpObj.getPaymentStatus();
        }
        /*
         * Status update
         */
        if (!origStatus.equals(newStatus)) {
          java.sql.Timestamp finalizedDate = null;
          if (finalizedDate == null) {
            finalizedDate = billStUpObj.getFinalizedDate() == null ? DateUtil.getCurrentTimestamp()
                : new java.sql.Timestamp(billStUpObj.getFinalizedDate().getTime());
          }

          error = billBOObj.updateBillStatus(billStUpObj, newStatus, paymentStatus,
              billStUpObj.getOkToDischarge(), finalizedDate, userId, false, false, false);
        }

        if (error != null &&  !error.isEmpty() && !error.startsWith("Bill status")) {
          error = error
              + "<br/> Please modify the discount amount and finalize /close the bill again.";
        }

        Map printParamMap = null;
        if ((receiptList != null && receiptList.size() > 0)) {
          String printerTypeStr = request.getParameter("printType");
          String customTemplate = request.getParameter("printBill");

          printParamMap = new HashMap();
          printParamMap.put("printerTypeStr", printerTypeStr);
          printParamMap.put("customTemplate", customTemplate);
          printParamMap.put("billNo", billNo);

          List<String> printURLs = bpImpl.generatePrintReceiptUrls(receiptList, printParamMap);
          request.getSession(false).setAttribute("printURLs", printURLs);
        }
      }
      // Update the bill total amount.
      allocationService.updateBillTotal(billNo);
      Integer centerId = RequestContext.getCenterId();
      // Call the Allocation method.
      allocationService.allocate(billNo, centerId);      
    
    
    } finally {
      FlashScope flash = FlashScope.getScope(request);
      if (error != null) {
        flash.error(error);
      }
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    }
    redirect.addParameter("mr_no", mrNo);
    redirect.addParameter("printerId", request.getParameter("printerId"));
    redirect.addParameter("printTemplate", request.getParameter("printTemplate"));
    redirect.addParameter("ps_status", "active");
    redirect.addParameter("madd_disable", false);
    return redirect;
  }

  /**
   * Gets the service sub tasks.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the service sub tasks
   * @throws SQLException the SQL exception
   * @throws ServletException the servlet exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @IgnoreConfidentialFilters
  public ActionForward getServiceSubTasks(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws SQLException, ServletException, IOException {
    response.setContentType("text/plain");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

    JSONSerializer js = new JSONSerializer().exclude("class");
    String serviceId = request.getParameter("service_id");

    response.getWriter().write(
        js.serialize(ConversionUtils.listBeanToListMap(
            toothTreatmentDetailsDao.getServiceSubTasks(serviceId))));
    response.flushBuffer();
    return null;
  }

}
