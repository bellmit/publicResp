package com.insta.hms.services;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.Preferences;
import com.insta.hms.billing.BillActivityChargeDAO;
import com.insta.hms.billing.payment.PaymentEngine;
import com.insta.hms.common.CommonUtils;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DynaBeanBuilder;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.instaforms.AbstractInstaForms;
import com.insta.hms.instaforms.PatientSectionDetailsDAO;
import com.insta.hms.instaforms.ServiceForms;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.FormComponents.FormComponentsDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.HealthAuthorityPreferences.HealthAuthorityPreferencesDAO;
import com.insta.hms.master.ImageMarkers.ImageMarkerDAO;
import com.insta.hms.master.PhraseSuggestionsMaster.PhraseSuggestionsMasterDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.PrintTemplates.PrintTemplate;
import com.insta.hms.master.PrintTemplates.PrintTemplatesDAO;
import com.insta.hms.master.RegularExpression.RegularExpressionDAO;
import com.insta.hms.master.Sections.SectionsDAO;
import com.insta.hms.master.ServiceDepartmentMaster.ServiceDepartmentMasterDAO;
import com.insta.hms.master.StoreMaster.StoreMasterDAO;
import com.insta.hms.master.outpatient.SystemGeneratedSectionsDAO;
import com.insta.hms.master.sectionrolerights.SectionRoleRightsDAO;
import com.insta.hms.medicalrecorddepartment.MRDDiagnosisDAO;
import com.insta.hms.outpatient.SecondaryComplaintDAO;
import com.insta.hms.resourcescheduler.ResourceDAO;
import com.insta.hms.stores.MedicineStock;
import com.insta.hms.stores.MedicineStockDAO;
import com.insta.hms.stores.StockFIFODAO;
import com.insta.hms.stores.StoreItemStock;
import com.insta.hms.stores.StoresDBTablesUtil;
import com.insta.hms.usermanager.UserServiceDeptDAO;
import com.insta.hms.vitalForm.VisitVitalsDAO;
import com.insta.hms.vitalparameter.VitalMasterDAO;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

// TODO: Auto-generated Javadoc
/**
 * The Class ServicesAction.
 *
 * @author krishna.t
 */
public class ServicesAction extends DispatchAction {

  /** The services DAO. */
  static ServicesDAO servicesDAO = new ServicesDAO();

  /** The s consumable DAO. */
  static ServiceConsumableUsageDAO sConsumableDAO = new ServiceConsumableUsageDAO();

  /** The service doc dao. */
  static ServiceDocumentsDAO serviceDocDao = new ServiceDocumentsDAO();

  /** The tech DAO. */
  static GenericDAO techDAO = new GenericDAO("hospital_technical");

  /** The con DAO. */
  static FormComponentsDAO conDAO = new FormComponentsDAO();

  /** The visitdao. */
  static GenericDAO visitdao = new GenericDAO("patient_registration");

  /** The scomplaint dao. */
  static SecondaryComplaintDAO scomplaintDao = new SecondaryComplaintDAO();

  /** The userservicedept dao. */
  static UserServiceDeptDAO userservicedeptDao = new UserServiceDeptDAO();

  /** The hosp direct bill pref dao. */
  static GenericDAO hospDirectBillPrefDao = new GenericDAO("hosp_direct_bill_prefs");

  /** The services dao. */
  static GenericDAO servicesDao = new GenericDAO("services");

  /** The stores dao. */
  static StoreMasterDAO storesDao = new StoreMasterDAO();

  JSONSerializer jsWithoutClass = new JSONSerializer().exclude("class");

  /**
   * Pending list which will lists the pending services for the patients.
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
   * @throws ServletException
   *           the servlet exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ParseException
   *           the parse exception
   * @throws SQLException
   *           the SQL exception
   */
  @IgnoreConfidentialFilters
  public ActionForward pendingList(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException, ParseException, SQLException {
    Map params = new HashMap(request.getParameterMap());
    
    HttpSession session = request.getSession(false);
    String userId = (String) session.getAttribute("userid");
    List<Hashtable<String, Object>> userServDept = null;

    userServDept = userservicedeptDao.getUserServiceDepartment(userId);
    request.setAttribute("userServDept", UserServiceDeptDAO.getUserServDept());
    request.setAttribute("userSelectServDept", UserServiceDeptDAO.getUserSelectServDept(userId));

    String servicename = request.getParameter("service_name");
    String[] servdeptid = request.getParameterValues("serv_dept_id");
    if (servdeptid != null) {
      params.put("serv_dept_id", servdeptid);
      params.put("serv_dept_id@cast", new String[] { "y" });
    }
    if (servicename != null && !"".equals(servicename)) {
      params.put("service_name", new String[] { servicename });
    }

    if (userServDept != null && userServDept.size() > 0) {
      if (null == request.getParameter("_mysearch")) {
        String[] serviceDeptArray = new String[userServDept.size()];
        for (int i = 0; i < userServDept.size(); i++) {
          serviceDeptArray[i] = userServDept.get(i).get("SERV_DEPT_ID").toString();
        }
        params.put("serv_dept_id", serviceDeptArray);
        params.put("serv_dept_id@cast", new String[] { "y" });
      }
    }
    String dateRange = request.getParameter("date_range");
    String weekStartDate = null;
    if (dateRange != null && dateRange.equals("week")) {
      SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.DATE, -7);
      Date openDt = cal.getTime();
      weekStartDate = dateFormat.format(openDt);

      params.put("presc_date", new String[] { weekStartDate, "" });
      params.put("presc_date@op", new String[] { "ge,le" });
      params.put("presc_date@cast", new String[] { "y" });
      params.remove("date_range");
    }

    request.setAttribute("pagedList", ServicesDAO.searchPendingServices(params,
        ConversionUtils.getListingParameter(params), true));
    request.setAttribute("directBillingPrefs",
        ConversionUtils.listBeanToMapBean(hospDirectBillPrefDao.listAll(), "item_type"));
    Map<String, String> filterMap = new HashMap<String, String>();
    filterMap.put("status", "A");

    ActionForward forward = new ActionForward(mapping.findForward("pendingserviceslist").getPath());
    // when ever user uses a pagination open_date should not append again.
    if (dateRange != null && dateRange.equals("week")
        && request.getParameter("open_date") == null) {
      addParameter("presc_date", weekStartDate, forward);
    }
    return forward;

  }
  
  /**
   * Service Autocomplete method.
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
   * @throws ServletException
   *           the servlet exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ParseException
   *           the parse exception
   * @throws SQLException
   *           the SQL exception
   */
  @IgnoreConfidentialFilters
  public ActionForward search(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException, ParseException, SQLException {
    
    String query = request.getParameter("query");

    List results = new ArrayList<>();
    if (query != null || !query.trim().isEmpty()) {
      Map<String,Object> params = new HashMap<>();
      params.put("service_name@op", new String[]{"isw"});
      params.put("service_name", new String[]{query.trim()});
      results = servicesDao.search(params).getDtoList();      
    }
    response.setContentType("application/json");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    Map<String, Object> responseMap = new HashMap<>();
    responseMap.put("services", results);
    response.getWriter().write(jsWithoutClass.deepSerialize(responseMap));
    response.flushBuffer();
    return null;
  }
  

  /**
   * User service dept DAO.
   *
   * @return the object
   */
  private Object userServiceDeptDAO() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Adds the parameter.
   *
   * @param key
   *          the key
   * @param value
   *          the value
   * @param forward
   *          the forward
   */
  public void addParameter(String key, String value, ActionForward forward) {
    StringBuffer sb = new StringBuffer(forward.getPath());
    if (key == null || key.length() < 1) {
      return;
    }  
    if (forward.getPath().indexOf('?') == -1) {
      sb.append('?');
    } else {
      sb.append('&');
    }  
    sb.append(key + "=" + value);
    forward.setPath(sb.toString());
  }

  /**
   * Conducted list returns partially conducted and coundction completed services details list.
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
   * @throws ServletException
   *           the servlet exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ParseException
   *           the parse exception
   * @throws SQLException
   *           the SQL exception
   */
  @IgnoreConfidentialFilters
  public ActionForward conductedList(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException, ParseException, SQLException {
    Map params = new HashMap(request.getParameterMap());

    HttpSession session = request.getSession(false);
    String userId = (String) session.getAttribute("userid");
    List<Hashtable<String, Object>> userServDept = null;

    userServDept = userservicedeptDao.getUserServiceDepartment(userId);
    request.setAttribute("userServDept", UserServiceDeptDAO.getUserServDept());
    request.setAttribute("userSelectServDept", UserServiceDeptDAO.getUserSelectServDept(userId));

    String servicename = request.getParameter("service_name");
    String[] servdeptid = request.getParameterValues("serv_dept_id");
    if (servdeptid != null) {
      params.put("serv_dept_id", servdeptid);
      params.put("serv_dept_id@cast", new String[] { "y" });
    }
    if (servicename != null && !"".equals(servicename)) {
      params.put("service_name", new String[] { servicename });
    }

    if (userServDept != null && userServDept.size() > 0) {
      if (null == request.getParameter("_mysearch")) {
        String[] serviceDeptArray = new String[userServDept.size()];
        for (int i = 0; i < userServDept.size(); i++) {
          serviceDeptArray[i] = userServDept.get(i).get("SERV_DEPT_ID").toString();
        }
        params.put("serv_dept_id", serviceDeptArray);
        params.put("serv_dept_id@cast", new String[] { "y" });
      }
    }
    String dateRange = request.getParameter("date_range");
    String weekStartDate = null;
    if (dateRange != null && dateRange.equals("week")) {
      SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.DATE, -7);
      Date openDt = cal.getTime();
      weekStartDate = dateFormat.format(openDt);

      params.put("presc_date", new String[] { weekStartDate, "" });
      params.put("presc_date@op", new String[] { "ge,le" });
      params.put("presc_date@cast", new String[] { "y" });
      params.remove("date_range");
    }

    request.setAttribute("pagedList", ServicesDAO.searchPendingServices(params,
        ConversionUtils.getListingParameter(params), false));
    Map<String, String> filterMap = new HashMap<String, String>();
    filterMap.put("status", "A");

    ActionForward forward = new ActionForward(mapping.findForward("conductedserviceslist")
        .getPath());
    // when ever user uses a pagination open_date should not append again.
    if (dateRange != null && dateRange.equals("week")
        && request.getParameter("presc_date") == null) {
      addParameter("presc_date", weekStartDate, forward);
    }
    return forward;
  }

  /**
   * Service details.
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
   * @throws ServletException
   *           the servlet exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   * @throws Exception
   *           the exception
   */
  public ActionForward serviceDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException, SQLException, Exception {
    HttpSession session = request.getSession(false);
    List storesList = null;
    String prescribedId = request.getParameter("prescription_id");
    List<BasicDynaBean> doctors = ServicesDAO.getDoctorsAndTechsList();
    BasicDynaBean serviceBean = ServicesDAO.getServiceDetails(Integer.parseInt(prescribedId));
    String[] conductingRoleIds = CommonUtils
        .getStringArrayFromCommaSeparatedString((String) serviceBean.get("conducting_role_id"));
    List<BasicDynaBean> roleUsers = null;
    String patientId = (String) serviceBean.get("patient_id");
    BasicDynaBean visitBean = visitdao.findByKey("patient_id", patientId);
    request.setAttribute("doctors_list",
        jsWithoutClass.deepSerialize(ConversionUtils.listBeanToListMap((doctors))));

    request.setAttribute("patient_id", patientId);
    String visitType = (String) visitBean.get("visit_type");
    request.setAttribute("serviceBean", serviceBean);
    
    AbstractInstaForms formDAO = new ServiceForms();
    BasicDynaBean serviceform = formDAO.getComponents(request.getParameterMap());
    request.setAttribute("form", serviceform);
    request.setAttribute("insta_form_json", jsWithoutClass.serialize(serviceform.getMap()));
    request.setAttribute("group_patient_sections",
        conDAO.findByKey("id", serviceform.get("form_id")).get("group_patient_sections"));
    request.setAttribute("section_rights", new SectionRoleRightsDAO()
        .getAllSectionsRights((Integer) request.getSession().getAttribute("roleId")));

    List<BasicDynaBean> sectionsDefList = new SectionsDAO()
        .getSections((String) serviceform.get("sections"));
    request.setAttribute("sectionsDefMap",
        ConversionUtils.listBeanToMapBean(sectionsDefList, "section_id"));

    PatientSectionDetailsDAO psd = new PatientSectionDetailsDAO();
    request.setAttribute("section_finalize_status",
        ConversionUtils.listBeanToMapMap(
            psd.getSections((String) visitBean.get("mr_no"), patientId,
                Integer.parseInt(prescribedId), 0, (Integer) serviceform.get("form_id")),
            "section_id"));

    // markers of fields from all sections.
    List<BasicDynaBean> imageMarkers = new ImageMarkerDAO()
        .getMarkers((String) serviceform.get("sections"));
    request.setAttribute("sectionsImageMarkers",
        ConversionUtils.listBeanToMapListBean(imageMarkers, "section_id"));

    List<BasicDynaBean> sectionsList = new SectionsDAO().listAll();
    request.setAttribute("insta_sections", sectionsList);
    request.setAttribute("insta_sections_json",
        jsWithoutClass.serialize(ConversionUtils.copyListDynaBeansToMap(sectionsList)));

    if (visitBean != null) {
      request.setAttribute("consultation_bean", visitBean.getMap());
    }
    request.setAttribute("secondary_complaints", scomplaintDao.getSecondaryComplaints(patientId));
    request.setAttribute("diagnosis_details", MRDDiagnosisDAO.getAllDiagnosisDetails(patientId));

    request.setAttribute("allergies",
        formDAO.getAllergies((String) visitBean.get("mr_no"), (String) visitBean.get("patient_id"),
            Integer.parseInt(prescribedId), 0, (Integer) serviceform.get("form_id")));
    request.setAttribute("preAnaesthestheticList",
        formDAO.getPreAnaestestheticRecords((String) visitBean.get("mr_no"), patientId,
            Integer.parseInt(prescribedId), 0, (Integer) serviceform.get("form_id")));

    VitalMasterDAO vmDAO = new VitalMasterDAO();
    VisitVitalsDAO vvDAO = new VisitVitalsDAO();
    request.setAttribute("all_fields", vmDAO.getActiveVitalParams("O"));
    List readingList = vvDAO.getVitals(patientId, null, null, "V");
    request.setAttribute("vital_readings", readingList);
    // vital reading exists is defined using above list, anyhow no date filters applied.
    request.setAttribute("vital_reading_exists", !readingList.isEmpty());
    request.setAttribute("latest_vital_reading_json",
        jsWithoutClass.deepSerialize(ConversionUtils.copyListDynaBeansToMap(vvDAO.getLatestVitals(
            (String) visitBean.get("mr_no"), (String) visitBean.get("patient_id")))));

    request.setAttribute("height_weight_params",
        jsWithoutClass.deepSerialize(ConversionUtils.copyListDynaBeansToMap(
            vvDAO.getHeightAndWeight((String) visitBean.get("mr_no"),
                (String) visitBean.get("patient_id")))));
    List<BasicDynaBean> usageconsumables = ServicesDAO
        .getServiceConsumablesUsed(Integer.parseInt(prescribedId));
    request.setAttribute("usageconsumables", usageconsumables);

    List<BasicDynaBean> consumables = usageconsumables;

    String serviceId = (String) serviceBean.get("service_id");
    List<BasicDynaBean> serviceconsumables = ServicesDAO.getServiceConsumables(serviceId);
    if (consumables.isEmpty()) {
      consumables = serviceconsumables;
    } 
    request.setAttribute("serviceconsumables", serviceconsumables);
    request.setAttribute("serviceconsumablesjson",
        jsWithoutClass.serialize(ConversionUtils.listBeanToListMap(serviceconsumables)));
    String servDeptId = serviceBean.get("serv_dept_id").toString();
    BasicDynaBean patientFormGroup = conDAO.getPatientSectionsGroupForServices(servDeptId,
        serviceId, "sc");

    request.setAttribute("consumables", consumables);
    request.setAttribute("genPrefs", GenericPreferencesDAO.getGenericPreferences());

    String serviceDeptStoreId = null;
    String userStoreId = null;
    String storeId = null;
    String storeName = "";
    // Get the Store from which the consumables/reagents are used.
    List<MedicineStock> medicineStkList = null;
    if (consumables != null && consumables.size() > 0) {
      medicineStkList = new ArrayList<MedicineStock>();
      boolean first = true;
      List<Integer> medicineIds = new ArrayList<Integer>();
      for (BasicDynaBean consumable : consumables) {
        if (consumable.get("store_id") != null) {
          storeId = (String) consumable.get("store_id");
        }
        medicineIds.add((int) consumable.get("consumable_id"));
        first = false;
      }
      if (medicineIds.size() > 0) {
        medicineStkList.addAll(MedicineStockDAO.getStockForMedicines(medicineIds));
      } 
    }

    // If the item does not exist in store, the reagent quantity is not deducted.
    // So get the store id from store_reagent_usage_main

    if (storeId == null || storeId.equals("")) {
      storeId = serviceBean.get("store_id") != null ? (String) serviceBean.get("store_id") : null;
    }

    // For the list of stores, get the service department related store (or) the user store(s).

    boolean serviceDeptHasStore = false;
    serviceDeptStoreId = ServiceDepartmentMasterDAO.getServiceDepartmentStoreStr(serviceId);

    if (serviceDeptStoreId != null && !serviceDeptStoreId.equals("")) {

      BasicDynaBean storebean = storesDao.findByKey("dept_id",
          Integer.parseInt(serviceDeptStoreId));

      if (storebean != null && (storebean.get("status")).equals("A")) {
        serviceDeptHasStore = true;
      } else {
        serviceDeptStoreId = null;
        serviceDeptHasStore = false;
      }
    }

    if (serviceDeptStoreId == null || serviceDeptStoreId.equals("")) {
      userStoreId = (String) session.getAttribute("pharmacyStoreId");

      if (userStoreId != null && !userStoreId.equals("")) {
        BasicDynaBean storebean = storesDao.findByKey("dept_id", Integer.parseInt(userStoreId));

        if (storebean == null || !(storebean.get("status")).equals("A")) {
          userStoreId = null;
        } 
      }
    }

    if (!serviceDeptHasStore) {
      String userId = (String) session.getAttribute("userId");
      int roleId = (Integer) session.getAttribute("roleId");
      String onlySuperStores = "N";
      String onlyWithCounters = "N";
      String allowedRaiseBill = "N";
      String onlySalesStores = "N";
      if (roleId == 1 || roleId == 2) {
        storesList = StoresDBTablesUtil.getStores(onlySuperStores, onlyWithCounters,
            allowedRaiseBill, onlySalesStores);
      } else {
        storesList = StoresDBTablesUtil.getLoggedUserStores(userId, onlySuperStores,
            onlyWithCounters, allowedRaiseBill, onlySalesStores);
      }
    }

    if (storeId == null || storeId.equals("")) {
      storeId = (serviceDeptStoreId != null && !serviceDeptStoreId.equals("")) ? serviceDeptStoreId
          : ((userStoreId != null && !userStoreId.equals("")) ? userStoreId : storeId);
    }

    if (storeId != null && !storeId.equals("")) {
      BasicDynaBean storebean = storesDao.findByKey("dept_id", Integer.parseInt(storeId));
      if (storebean != null && storebean.get("dept_name") != null) {
        storeName = (String) storebean.get("dept_name");
      } 
    }

    if ((storesList == null || storesList.size() == 0)
        && (storeId != null && !storeId.equals(""))) {
      storesList = new ArrayList();
      DynaBeanBuilder builder = new DynaBeanBuilder();
      builder.add("dept_id");
      builder.add("dept_name");
      BasicDynaBean bean = builder.build();
      bean.set("dept_id", storeId);
      bean.set("dept_name", storeName);
      storesList.add(bean);
    }

    if ((storesList == null || storesList.size() == 0) && (storeId == null || storeId.equals(""))
        && !consumables.isEmpty()) {
      request.setAttribute("error",
          "There is no assigned store. You do not have access to this screen.");
    }

    request.setAttribute("storesList", ConversionUtils.listBeanToListMap(storesList));

    boolean conducted = serviceBean.get("conducted") != null
        && ((String) serviceBean.get("conducted")).equals("C");

    List<BasicDynaBean> reagentsused = ServicesDAO
        .getServiceConsumableReagentsUsed(Integer.parseInt(prescribedId));
    if (conducted && consumables != null && consumables.size() > 0
        && (reagentsused == null || reagentsused.size() == 0)) {
      request.setAttribute("info", "No consumables were used for the service while conduction.");
    }
    if (conducted && consumables != null && consumables.size() > 0 && (reagentsused != null
        && reagentsused.size() != 0 && reagentsused.size() < consumables.size())) {
      request.setAttribute("info", "Some consumables are not used for service conduction.");
    }
    String printerId = request.getParameter("printerId");
    String templateName = request.getParameter("templateName");
    if (templateName == null || templateName.equals("")) {
      templateName = "BUILTIN_HTML";
    }  
    request.setAttribute("templateName", templateName);
    if (printerId == null || printerId.equals("")) {
      request.setAttribute("printerDef",
          PrintConfigurationsDAO.getServiceDefaultPrintPrefs().get("printer_id"));
    } else {
      request.setAttribute("printerDef", printerId);
    }  
    PrintTemplate template;
    template = PrintTemplate.Ser;
    String templateContent = new PrintTemplatesDAO().getCustomizedTemplate(template);
    request.setAttribute("printTemplate", templateContent);

    request.setAttribute("storeId", storeId);
    request.setAttribute("storeName", storeName);
    request.setAttribute("medicineStockDetailsJSON", jsWithoutClass.serialize(medicineStkList));
    request.setAttribute("serviceConsumablesJSON",
        jsWithoutClass.serialize(ConversionUtils.copyListDynaBeansToMap(consumables)));
    java.util.List<BasicDynaBean> phraseSuggestions = PhraseSuggestionsMasterDAO
        .getPhraseSuggestionsDynaList();
    request.setAttribute("phrase_suggestions_json", jsWithoutClass.deepSerialize(ConversionUtils
        .listBeanToMapListMap(phraseSuggestions, "phrase_suggestions_category_id")));
    String deptId = (String) visitBean.get("dept_name");
    java.util.List<BasicDynaBean> phraseSuggestionsByDept = PhraseSuggestionsMasterDAO
        .getPhraseSuggestionsByDeptDynaList(deptId);
    request.setAttribute("phrase_suggestions_by_dept_json", jsWithoutClass.deepSerialize(
        ConversionUtils.listBeanToMapListMap(
            phraseSuggestionsByDept, "phrase_suggestions_category_id")));
    request.setAttribute("sys_generated_forms", jsWithoutClass.deepSerialize(
        ConversionUtils.listBeanToListMap(new SystemGeneratedSectionsDAO().listAll())));
    String healthAuthority = CenterMasterDAO
        .getHealthAuthorityForCenter((Integer) visitBean.get("center_id"));
    request.setAttribute("defaultDiagnosisCodeType", HealthAuthorityPreferencesDAO
        .getHealthAuthorityPreferences(healthAuthority).getDiagnosis_code_type());

    HashMap<Integer, String> regExpPatternMap = RegularExpressionDAO
        .getRegPatternWithExpression("E");
    request.setAttribute("regExpMapDesc", RegularExpressionDAO.getRegPatternWithExpression("D"));
    request.setAttribute("regExpPatternMap", jsWithoutClass.serialize(regExpPatternMap));
    return mapping.findForward("servicedetails");
  }

  /**
   * Conduct.
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
   * @throws ServletException
   *           the servlet exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   * @throws Exception
   *           the exception
   */
  public ActionForward conduct(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response)
      throws ServletException, IOException, SQLException, ParseException, Exception {
    boolean saveRequired = new Boolean(request.getParameter("saveRequired"));
    ActionRedirect redirect = null;
    String prescriptionId = request.getParameter("prescription_id");
    String patientId = null;
    Integer storeId = (!StringUtils.isEmpty(request.getParameter("store_id")))
        ? Integer.parseInt(request.getParameter("store_id"))
        : null;
    FlashScope flash = FlashScope.getScope(request);
    boolean commit = false;
    String error = null;
    BasicDynaBean serviceBean =
        servicesDAO.findByKey("prescription_id", (Integer.parseInt(prescriptionId)));

    if (saveRequired) {
      HttpSession session = request.getSession(false);
      String userName = (String) session.getAttribute("userId");

      String serviceStatus = (String) serviceBean.get("conducted");
      boolean isReopened = serviceStatus.equals("R");

      String completed = request.getParameter("completed");
      completed = completed == null ? (isReopened ? "R" : "P") : completed;

      String updateConsumables = request.getParameter("updateConsumables");
      updateConsumables = updateConsumables == null ? "" : updateConsumables;
      String[] reagentUsageSeq = (String[]) request.getParameterValues("reagent_usage_seq");
      String[] qty = (String[]) request.getParameterValues("qty");
      String[] consumableId = (String[]) request.getParameterValues("consumable_id");
      String serviceId = request.getParameter("service_id");
      Map params = request.getParameterMap();
      BasicDynaBean serviceBeanComp =
          ServicesDAO.getServiceDetails(Integer.parseInt(prescriptionId));
      patientId = (String) serviceBeanComp.get("patient_id");
      String servDeptId = serviceBeanComp.get("serv_dept_id").toString();

      Preferences pref = (Preferences) request.getSession(false).getAttribute("preferences");
      String modConsumableActive = "Y";
      String invModAct = null;
      if ((pref != null) && (pref.getModulesActivatedMap() != null)) {
        modConsumableActive = (String) pref.getModulesActivatedMap().get("mod_consumables_flow");
        if (modConsumableActive == null || "".equals(modConsumableActive)) {
          modConsumableActive = "N";
        }
      }
      if ((pref != null) && (pref.getModulesActivatedMap() != null)) {
        invModAct = (String) pref.getModulesActivatedMap().get("mod_stores");
        if (invModAct == null || "".equals(invModAct)) {
          invModAct = "N";
        }
      }

      Connection con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      String endDate = request.getParameter("conducted_end_date");
      String endTime = request.getParameter("conducted_end_time");
      Timestamp conductedEndDate = null;
      if (endDate != null && endTime != null) {
        conductedEndDate = DateUtil.parseTimestamp(endDate, endTime);
      }
      serviceBean.set("conducted_end_date", conductedEndDate);
      String remarks = request.getParameter("remarks");
      String username = (String) session.getAttribute("userid");
      serviceBean.set("conducted", completed);
      serviceBean.set("remarks", remarks);
      serviceBean.set("user_name", username);
      String date = request.getParameter("conducted_date");
      String time = request.getParameter("conducted_time");
      java.sql.Timestamp conductedDate = DateUtil.parseTimestamp(date, time);
      serviceBean.set("conducteddate", conductedDate);
      // To save the form in reopened state.
      if (isReopened) {
        String errorOnFormSave = null;
        try {
          errorOnFormSave = new ServiceForms().save(con, params);
          if (errorOnFormSave == null) {
            if (servicesDAO.update(con, serviceBean.getMap(), "prescription_id",
                Integer.parseInt(prescriptionId)) != 0) {
              commit = true;
            }
          }
        } finally {
          DataBaseUtil.commitClose(con, commit);
          if (!commit) {
            flash.put("error", "Failed to save the service.");
          }
        }
      } else {
        String conductedby = request.getParameter("conducting_doctor_id");
        serviceBean.set("conductedby", conductedby);
        Map keys = new HashMap();
        keys.put("prescription_id", Integer.parseInt(prescriptionId));

        try {
          txnFailed: {

            DynaBeanBuilder builder = new DynaBeanBuilder();
            builder.add("item_id", Integer.class).add("qty", BigDecimal.class).add("redusing_qty",
                BigDecimal.class);
            List consumablesUsed = null;

            if (invModAct.equals("Y")) {
              if (updateConsumables.equals("Add")) {
                List consumableList = new ArrayList();
                consumablesUsed = new ArrayList();
                for (int i = 0; i < consumableId.length; i++) {
                  BasicDynaBean consBean = sConsumableDAO.getBean();
                  consBean.set("usage_no", sConsumableDAO.getNextSequence());
                  consBean.set("service_id", serviceId);
                  consBean.set("consumable_id", Integer.parseInt(consumableId[i]));
                  consBean.set("prescription_id", Integer.parseInt(prescriptionId));
                  if (storeId != null) {
                    consBean.set("store_id", storeId);
                  }
                  consBean.set("qty", new BigDecimal(qty[i]));
                  consumableList.add(consBean);

                  BasicDynaBean reagentBean = builder.build();
                  reagentBean.set("item_id", consBean.get("consumable_id"));
                  reagentBean.set("qty", consBean.get("qty"));
                  reagentBean.set("redusing_qty", consBean.get("qty"));
                  consumablesUsed.add(reagentBean);
                }
                if (!consumableList.isEmpty() && !sConsumableDAO.insertAll(con, consumableList)) {
                  break txnFailed;
                }

              } else if (updateConsumables.equals("Update")) {
                consumablesUsed = new ArrayList();
                for (int i = 0; i < consumableId.length; i++) {
                  BasicDynaBean consBean = sConsumableDAO.getBean();
                  consBean.set("usage_no", Integer.parseInt(reagentUsageSeq[i]));
                  consBean.set("service_id", serviceId);
                  consBean.set("consumable_id", Integer.parseInt(consumableId[i]));
                  consBean.set("prescription_id", Integer.parseInt(prescriptionId));
                  if (storeId != null) {
                    consBean.set("store_id", storeId);
                  }
                  consBean.set("qty", new BigDecimal(qty[i]));

                  if (sConsumableDAO.update(con, consBean.getMap(), "usage_no",
                      consBean.get("usage_no")) == 0) {
                    break txnFailed;
                  }
                  BasicDynaBean reagentBean = builder.build();
                  reagentBean.set("item_id", consBean.get("consumable_id"));
                  reagentBean.set("qty", consBean.get("qty"));
                  reagentBean.set("redusing_qty", consBean.get("qty"));
                  consumablesUsed.add(reagentBean);
                }
              }
            }

            if (completed.equals("C") && modConsumableActive.equals("Y")) {

              if (storeId != null) {
                if (!StoreItemStock.updateReagents(con, serviceId, Integer.parseInt(prescriptionId),
                    username, storeId, consumablesUsed, 0, "services")) {
                  break txnFailed;
                }
                serviceBean.set("stock_reduced", true);
              }
            }

            if (completed.equals("C")) {
              if (!ResourceDAO.updateAppointments(con, Integer.parseInt(prescriptionId), "SNP")) {
                break txnFailed;
              }
            }

            if (servicesDAO.update(con, serviceBean.getMap(), "prescription_id",
                Integer.parseInt(prescriptionId)) == 0) {
              break txnFailed;
            }

            if (conductedby != null && conductedDate != null) {
              if (!BillActivityChargeDAO.updateActivityDetails(con, "SER", prescriptionId,
                  conductedby, completed.equals("C") ? "Y" : "N", conductedDate, userName)) {
                break txnFailed;
              }

              String chargeId =
                  BillActivityChargeDAO.getChargeId("SER", Integer.parseInt(prescriptionId));
              if (chargeId != null) {
                if (!PaymentEngine.updateAllPayoutAmounts(con, chargeId)) {
                  break txnFailed;
                }
              }
            }
            error = new ServiceForms().save(con, params);
            if (error != null) {
              break txnFailed;
            }
            commit = true;
          }
        } finally {
          DataBaseUtil.commitClose(con, commit);
          // update stock timestamp
          StockFIFODAO stockFIFODAO = new StockFIFODAO();
          stockFIFODAO.updateStockTimeStamp();
          if (storeId != null) {
            stockFIFODAO.updateStoresStockTimeStamp(storeId);
          }
        }
        if (commit) {
          flash.put("success", "Service details saved successfully..");
        } else {
          flash.put("error",
              error != null ? error
                  : "Failed to save the service, Since there are "
                      + " no consumables available in stock or might have expired..");
        }
      }
    }
    Boolean isPrint = new Boolean(request.getParameter("isPrint"));
    Boolean addReport = new Boolean(request.getParameter("addReport"));
    if (saveRequired ? commit : true) {
      if (addReport) {
        String reportId = request.getParameter("reportId");
        String mrno = request.getParameter("mr_no");
        if (!reportId.equals("") && !reportId.equals("0")) {
          redirect = new ActionRedirect(mapping.findForward("updateServiceReport"));
          redirect.addParameter("doc_id", reportId);
          redirect.addParameter("format", request.getParameter("format"));
          redirect.addParameter("template_id", request.getParameter("template_id"));
        } else {
          redirect = new ActionRedirect(mapping.findForward("chooseTemplate"));
        }
        redirect.addParameter("mr_no", mrno);
      } else if (isPrint) {
        ActionRedirect printRedirect = new ActionRedirect(mapping.findForward("printRedirect"));
        printRedirect.addParameter("mr_no", request.getParameter("mr_no"));
        printRedirect.addParameter("printerId", request.getParameter("printerId"));
        printRedirect.addParameter("printTemplate", request.getParameter("printTemplate"));
        printRedirect.addParameter("patient_id", patientId);
        printRedirect.addParameter("prescription_id", prescriptionId);

        List<String> printURLs = new ArrayList<String>();
        printURLs.add(request.getContextPath() + printRedirect.getPath());
        request.getSession(false).setAttribute("printURLs", printURLs);
        redirect = new ActionRedirect(mapping.findForward("servicedetailsRedirect"));
      } else {
        redirect = new ActionRedirect(mapping.findForward("servicedetailsRedirect"));
      }
    } else {
      redirect = new ActionRedirect(mapping.findForward("servicedetailsRedirect"));
    }
    redirect.addParameter("patient_id", patientId);
    redirect.addParameter("prescription_id", prescriptionId);
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

    return redirect;
  }

  /**
   * Revert conduction of a service.
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
   * @throws ServletException
   *           the servlet exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   * @throws Exception
   *           the exception
   */
  public ActionForward revertConduction(ActionMapping mapping, ActionForm form,
      HttpServletRequest request,
      HttpServletResponse response)
          throws ServletException, IOException, SQLException, ParseException, Exception {

    ActionRedirect redirect = new ActionRedirect(
        request.getHeader("Referer").replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));
    FlashScope flash = FlashScope.getScope(request);
    String prescriptionId = request.getParameter("prescription_id");
    HttpSession session = request.getSession(false);
    String userName = (String) session.getAttribute("userId");
    BasicDynaBean serviceBean = servicesDAO.getBean();
    Connection con = null;
    boolean success = false;
    serviceBean.set("conducted", "R");
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      success = servicesDAO.update(con, serviceBean.getMap(), "prescription_id",
          Integer.parseInt(prescriptionId)) > 0;
    } finally {
      DataBaseUtil.commitClose(con, success);
      if (!success) {
        flash.put("error", "Failed to revert conduction of selected service.");
      }
    }
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;

  }

  /**
   * Sign off reports.
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
   * @throws ServletException
   *           the servlet exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   */
  public ActionForward signOffReports(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException, SQLException {

    ActionRedirect redirect = new ActionRedirect(
        request.getHeader("Referer").replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));

    FlashScope flash = FlashScope.getScope(request);
    String[] prescriptionIds = request.getParameterValues("prescription_id");
    Connection con = null;
    if (prescriptionIds != null) {
      int count = 0;
      try {
        con = DataBaseUtil.getConnection();
        con.setAutoCommit(false);
        for (String prescriptionId : prescriptionIds) {
          BasicDynaBean serviceDocBean = serviceDocDao.getBean();
          serviceDocBean.set("signed_off", true);
          if (serviceDocDao.update(con, serviceDocBean.getMap(), "prescription_id",
              Integer.parseInt(prescriptionId)) == 0) {
            break;
          }  
          count++;
        }
      } finally {
        boolean success = (count == prescriptionIds.length);
        DataBaseUtil.commitClose(con, success);
        if (!success) {
          flash.put("error", "Failed to sign off selected reports..");
        } 
      }
    }
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;
  }

  /**
   * Revert Sign off report.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws ServletException the servlet exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  public ActionForward revertSignOffReport(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException, SQLException {
    ActionRedirect redirect = new ActionRedirect(
        request.getHeader("Referer").replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));

    FlashScope flash = FlashScope.getScope(request);
    String prescriptionId = request.getParameter("prescription_id");
    Connection con = null;
    boolean success = false;
    if (prescriptionId != null) {
      try {
        con = DataBaseUtil.getConnection();
        con.setAutoCommit(false);
        BasicDynaBean serviceDocBean = serviceDocDao.getBean();
        serviceDocBean.set("signed_off", false);
        success = serviceDocDao.update(con, serviceDocBean.getMap(), "prescription_id",
            Integer.parseInt(prescriptionId)) > 0;
      } finally {
        DataBaseUtil.commitClose(con, success);
        if (!success) {
          flash.put("error", "Failed to revert sign off selected report.");
        }
      }
    }
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;
  }
}
