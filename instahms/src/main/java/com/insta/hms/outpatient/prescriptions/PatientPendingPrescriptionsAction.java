package com.insta.hms.outpatient.prescriptions;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.Registration.PatientInsurancePlanDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.eauthorization.EAuthPrescriptionActivitiesDAO;
import com.insta.hms.eauthorization.EAuthPrescriptionDAO;
import com.insta.hms.master.DiagnosticDepartmentMaster.DiagnosticDepartmentMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.TpaMaster.TpaMasterDAO;
import com.insta.hms.outpatient.DentalChartHelperDAO;
import com.insta.hms.outpatient.PatientPrescriptionDAO;
import com.insta.hms.outpatient.ToothImageDetails;
import com.insta.hms.usermanager.UserBO;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * The Class PatientPendingPrescriptionsAction.
 *
 * @author krishna
 */
public class PatientPendingPrescriptionsAction extends DispatchAction {

  /** The dao. */
  static final PatientPendingPrescriptionsDAO dao = new PatientPendingPrescriptionsDAO();
  
  /** The ser dao. */
  static final GenericDAO serDao = new GenericDAO("patient_service_prescriptions");
  
  /** The test dao. */
  static final GenericDAO testDao = new GenericDAO("patient_test_prescriptions");
  
  /** The opt dao. */
  static final GenericDAO optDao = new GenericDAO("patient_operation_prescriptions");
  
  /** The doctor DAO. */
  static final GenericDAO doctorDAO = new GenericDAO("patient_consultation_prescriptions");
  
  /** The e auth act dao. */
  static final EAuthPrescriptionActivitiesDAO eauthActDao = new EAuthPrescriptionActivitiesDAO();
  
  /** The e auth presc DAO. */
  static final EAuthPrescriptionDAO eauthPrescDAO = new EAuthPrescriptionDAO();
  
  /** The reg dao. */
  static final GenericDAO regDao = new GenericDAO("patient_registration");

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
   * @throws ParseException the parse exception
   * @throws ServletException the servlet exception
   */
  public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, IOException, ParseException,
      ServletException {
    Map params = new HashMap(request.getParameterMap());
    String prescType = mapping.getProperty("presc_type");
    // while searching from patient pending prescriptions, retreive the presc type from request
    // params.
    if (prescType == null || prescType.equals("")) {
      prescType = request.getParameter("presc_type");
    }
    HttpSession session = request.getSession(false);
    String userId = (String) session.getAttribute("userid");

    String category = mapping.getProperty("category");
    if (prescType != null && prescType.equals("Inv.")) {
      BasicDynaBean userDept = new DiagnosticDepartmentMasterDAO().getUserDeptAndCategory(userId);
      String department = request.getParameter("cond_dept_id");

      if (department == null) {
        if (userDept != null) {
          String deptCategory = (String) userDept.get("category");
          if (deptCategory.equals(category)) {
            department = (String) userDept.get("lab_dept_id");
          }
        }
      }
      request.setAttribute("userDept", department);
      params.put("cond_dept_id", new String[] { department });
    }

    params.put("presc_type", new String[] { prescType });
    params.put("category", new String[] { category });

    Map<LISTING, Object> listingParams = ConversionUtils.getListingParameter(params);
    PagedList list = dao.getPrescriptions(params, listingParams);
    request.setAttribute("pagedList", list);
    request.setAttribute("presc_type", mapping.getProperty("presc_type"));
    request.setAttribute("category", mapping.getProperty("category"));
    return mapping.findForward("list");
  }

  /**
   * Adds the.
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
  public ActionForward add(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, IOException, ServletException,
      ParseException {
    Boolean defaultScreen = new Boolean(request.getParameter("defaultScreen"));
    if (!defaultScreen) {
      String patientId = request.getParameter("patient_id");
      java.util.Map patient = com.insta.hms.Registration.VisitDetailsDAO
          .getPatientHeaderDetailsMap(patientId);
      if (patient == null) {
        // user entered visit id is not a valid patient id.
        FlashScope flash = FlashScope.getScope(request);
        flash.put("error", "No Patient with Id:" + patientId);
        ActionRedirect redirect = new ActionRedirect(mapping.findForward("addRedirect"));
        redirect.addParameter("defaultScreen", true);
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
        return redirect;
      }
      setAttributes(request, mapping, patient);
    }
    return mapping.findForward("addshow");
  }

  /**
   * Show.
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
  public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, IOException, ServletException,
      ParseException {
    int patientPrescId = Integer.parseInt(request.getParameter("patient_presc_id"));
    BasicDynaBean bean = dao.getPrescDetails(patientPrescId);
    request.setAttribute("bean", bean);

    java.util.Map patient = com.insta.hms.Registration.VisitDetailsDAO
        .getPatientHeaderDetailsMap((String) bean.get("patient_id"));
    setAttributes(request, mapping, patient);
    return mapping.findForward("addshow");
  }

  /**
   * Sets the attributes.
   *
   * @param request the request
   * @param mapping the mapping
   * @param patient the patient
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void setAttributes(HttpServletRequest request, ActionMapping mapping, Map patient)
      throws SQLException, ParseException, IOException {
    BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
    String toothNumberingSystem = (String) genericPrefs.get("tooth_numbering_system");
    ToothImageDetails adultToothImageDetails = DentalChartHelperDAO.getToothImageDetails(true);
    ToothImageDetails pediacToothImageDetails = DentalChartHelperDAO.getToothImageDetails(false);

    request.setAttribute("adult_tooth_numbers",
        DentalChartHelperDAO.getToothNumbers(toothNumberingSystem, adultToothImageDetails));
    request.setAttribute("pediac_tooth_numbers",
        DentalChartHelperDAO.getToothNumbers(toothNumberingSystem, pediacToothImageDetails));
    JSONSerializer json = new JSONSerializer().exclude("class");
    request.setAttribute("users_json", json.deepSerialize(new UserBO().getAllActiveUsers()));

    String tpaId = (String) patient.get("primary_sponsor_id");
    tpaId = tpaId == null || tpaId.equals("") ? "-1" : tpaId;

    boolean modEclaimPreauth = (Boolean) request.getSession(false).getAttribute(
        "mod_eclaim_preauth");
    String tpaRequiresPreAuth = "N";

    /*
     * E-PreAuth is required only when mod_eclaim_preauth module is enabled and TPA has Prior
     * Authorization mode Online, for OP/IP/OSP patients
     */
    if (modEclaimPreauth) {
      BasicDynaBean tpaBean = new TpaMasterDAO().findByKey("tpa_id", tpaId);
      if (tpaBean != null
          && (((String) tpaBean.get("pre_auth_mode")).equals("M") || ((String) tpaBean
              .get("pre_auth_mode")).equals("O"))) {
        tpaRequiresPreAuth = "Y";
      }
    }
    List<BasicDynaBean> planListBean = new PatientInsurancePlanDAO()
        .getPlanDetails((String) patient.get("patient_id"));
    boolean multiPlanExists = null != planListBean && planListBean.size() == 2;
    request.setAttribute("multiPlanExists", multiPlanExists);

    request.setAttribute("TPArequiresPreAuth", tpaRequiresPreAuth);
    request.setAttribute("patient", patient);
    request.setAttribute("presc_type", mapping.getProperty("presc_type"));
    request.setAttribute("category", mapping.getProperty("category"));
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
      HttpServletResponse response) throws SQLException, IOException, ServletException,
      ParseException {

    String consIdStr = request.getParameter("consultation_id");
    int consId = 0;
    if (consIdStr != null && !consIdStr.equals("")) {
      consId = Integer.parseInt(consIdStr);
    }
    String patientId = request.getParameter("patient_id");
    String patientPrescIdStr = request.getParameter("patient_presc_id");
    int patientPrescId = 0;
    if (patientPrescIdStr != null && !patientPrescIdStr.equals("")) {
      patientPrescId = Integer.parseInt(patientPrescIdStr);
    }

    BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
    HttpSession session = (HttpSession) request.getSession(false);
    String userName = (String) session.getAttribute("userid");

    Map params = request.getParameterMap();
    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);
    boolean flag = false;
    try {
      Map map = insertOrUpdatePrescription(con, params, patientPrescId, consId, patientId,
          userName, genericPrefs);
      flag = (Boolean) map.get("success");
      patientPrescId = (Integer) map.get("patient_presc_id");
    } finally {
      DataBaseUtil.commitClose(con, flag);
    }

    ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
    if (patientPrescId == 0 && !flag) {
      redirect = new ActionRedirect(mapping.findForward("addRedirect"));
    }
    FlashScope flash = FlashScope.getScope(request);
    if (!flag) {
      flash.error("Failed to update/insert the prescription.");
    }
    redirect.addParameter("patient_presc_id", patientPrescId);
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;
  }

  /**
   * Insert or update prescription.
   *
   * @param con the con
   * @param params the params
   * @param patientPrescId the patient presc id
   * @param consultationId the consultation id
   * @param patientId the patient id
   * @param userName the user name
   * @param genericPrefs the generic prefs
   * @return the map
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ParseException the parse exception
   */
  private Map insertOrUpdatePrescription(Connection con, Map params, int patientPrescId,
      int consultationId, String patientId, String userName, BasicDynaBean genericPrefs)
      throws SQLException, IOException, ParseException {
    boolean flag = false;
    String itemType = ConversionUtils.getParamValue(params, "presc_type", "");
    String noOrder = ConversionUtils.getParamValue(params, "do_not_order", "N");
    String noOrderReason = ConversionUtils.getParamValue(params, "no_order_reason", "");
    String conductingPersonnel = ConversionUtils.getParamValue(params, "conducting_personnel", "");
    String priPreAuthNo = ConversionUtils.getParamValue(params, "pri_pre_auth_no", "");
    String priPreAuthMode = ConversionUtils.getParamValue(params, "pri_pre_auth_mode_id", "");
    String secPreAuthNo = ConversionUtils.getParamValue(params, "sec_pre_auth_no", "");
    String secPreAuthMode = ConversionUtils.getParamValue(params, "sec_pre_auth_mode_id", "");

    Map map = null;
    boolean add = patientPrescId == 0;
    PatientPrescriptionDAO patPrescDAO = new PatientPrescriptionDAO();
    BasicDynaBean presBean = patPrescDAO.getBean();

    BasicDynaBean record = patPrescDAO.findByKey("patient_presc_id", patientPrescId);
    txn: {
      presBean.set("pri_pre_auth_no", priPreAuthNo);
      presBean.set("sec_pre_auth_no", secPreAuthNo);
      if (!priPreAuthMode.equals("")) {
        presBean.set("pri_pre_auth_mode_id", Integer.parseInt(priPreAuthMode));
      }
      if (!secPreAuthMode.equals("")) {
        presBean.set("sec_pre_auth_mode_id", Integer.parseInt(secPreAuthMode));
      }

      if (consultationId == 0) {
        presBean.set("visit_id", patientId);
      }
      if (consultationId != 0) {
        presBean.set("consultation_id", consultationId);
      }
      presBean.set("conducting_personnel", conductingPersonnel);
      if (noOrder.equals("Y")) {
        presBean.set("status", "X");
        presBean.set("no_order_reason", noOrderReason);
        // do not update the cancelled by and datetime if it is already cancelled.
        if ((record == null || !record.get("status").equals("X"))) {
          presBean.set("cancelled_datetime", DateUtil.getCurrentTimestamp());
          presBean.set("cancelled_by", userName);
        }
      } else if (noOrder.equals("N")) {
        presBean.set("status", "P");
        presBean.set("no_order_reason", "");
        presBean.set("cancelled_datetime", null);
        presBean.set("cancelled_by", null);
      }
      if (add) {
        patientPrescId = patPrescDAO.getNextSequence();

        presBean.set("patient_presc_id", patientPrescId);
        presBean.set("presc_type", itemType);

        if (!patPrescDAO.insert(con, presBean)) {
          break txn;
        }

      } else {

        if (patPrescDAO.update(con, presBean.getMap(), "patient_presc_id", patientPrescId) == 0) {
          break txn;
        }
      }
      if (itemType.equals("Inv.")) {
        map = crudTest(con, patientPrescId, params, consultationId, patientId, userName, add);
      } else if (itemType.equals("Service")) {
        map = crudService(con, patientPrescId, params, consultationId, patientId, userName, add,
            genericPrefs);
      } else if (itemType.equals("Doctor")) {
        map = crudDoctor(con, patientPrescId, params, consultationId, patientId, userName, add);
      } else if (itemType.equals("Operation")) {
        map = crudOperation(con, patientPrescId, params, consultationId, patientId, userName, add);
      }
      if (map == null || !(Boolean) map.get("success")) {
        break txn;
      }

      flag = true;
    }
    Map resultMap = new HashMap();
    resultMap.put("success", flag);
    resultMap.put("patient_presc_id", patientPrescId);
    return resultMap;
  }

  /**
   * Crud service.
   *
   * @param con the con
   * @param itemPrescriptionId the item prescription id
   * @param params the params
   * @param consId the cons id
   * @param patientId the patient id
   * @param userName the user name
   * @param add the add
   * @param genericPrefs the generic prefs
   * @return the map
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private Map crudService(Connection con, int itemPrescriptionId, Map params, int consId,
      String patientId, String userName, boolean add, BasicDynaBean genericPrefs)
      throws SQLException, IOException {
    String itemId = ConversionUtils.getParamValue(params, "item_id", "");
    String itemRemark = ConversionUtils.getParamValue(params, "item_remarks", "");
    String itemType = ConversionUtils.getParamValue(params, "presc_type", "");
    String requirePriorAuth = ConversionUtils.getParamValue(params, "requirePriorAuth", "N");
    String toothNumber = ConversionUtils.getParamValue(params, "tooth_number", "");
    String serviceQty = ConversionUtils.getParamValue(params, "qty", "");
    String tpaId = ConversionUtils.getParamValue(params, "tpa_id", "");

    int preauthPrescId = 0;
    boolean flag = false;
    BasicDynaBean itemBean = serDao.getBean();

    txn: {

      itemBean.set("service_id", itemId);
      itemBean.set("service_remarks", itemRemark);
      itemBean.set("username", userName);
      if (genericPrefs.get("tooth_numbering_system").equals("U")) {
        itemBean.set("tooth_unv_number", toothNumber);
        itemBean.set("tooth_fdi_number", "");
      } else {
        itemBean.set("tooth_unv_number", "");
        itemBean.set("tooth_fdi_number", toothNumber);
      }

      itemBean.set("qty", Integer.parseInt(serviceQty));

      if (add) {
        itemBean.set("op_service_pres_id", itemPrescriptionId);
        if (!serDao.insert(con, itemBean)) {
          break txn;
        }
        if (!tpaId.equals("") && requirePriorAuth.equals("Y")) {
          BasicDynaBean regBean = regDao.findByKey("patient_id", patientId);
          if (regBean != null) {
            String primaryInsuranceCo = (String) regBean.get("primary_insurance_co");
            String secondaryInsuranceCo = (String) regBean.get("secondary_insurance_co");
            if (null != primaryInsuranceCo && !primaryInsuranceCo.equals("")) {
              preauthPrescId = eauthPrescDAO.getEAuthPrescSequenceId(con, userName, preauthPrescId,
                  consId, patientId, primaryInsuranceCo);
              if (!eauthActDao.insertEAuth(con, itemPrescriptionId, patientId, userName, itemType,
                  itemBean, preauthPrescId, consId)) {
                break txn;
              }
            }
            if (null != secondaryInsuranceCo && !secondaryInsuranceCo.equals("")) {
              preauthPrescId = eauthPrescDAO.getEAuthPrescSequenceId(con, userName, preauthPrescId,
                  consId, patientId, secondaryInsuranceCo);
              if (!eauthActDao.insertEAuth(con, itemPrescriptionId, patientId, userName, itemType,
                  itemBean, preauthPrescId, consId)) {
                break txn;
              }
            }
          }
        }
      } else {

        Map keys = new HashMap();
        keys.put("op_service_pres_id", itemPrescriptionId);
        itemBean.set("mod_time", new Timestamp(new java.util.Date().getTime()));
        if (serDao.update(con, itemBean.getMap(), keys) <= 0) {
          break txn;
        }

        BasicDynaBean preAuthBean = eauthActDao.findByKey("patient_pres_id", itemPrescriptionId);
        if (preAuthBean == null) {
          if (!tpaId.equals("") && requirePriorAuth.equals("Y")) {
            BasicDynaBean regBean = regDao.findByKey("patient_id", patientId);
            if (regBean != null) {
              String primaryInsuranceCo = (String) regBean.get("primary_insurance_co");
              String secondaryInsuranceCo = (String) regBean.get("secondary_insurance_co");
              if (null != primaryInsuranceCo && !primaryInsuranceCo.equals("")) {
                preauthPrescId = eauthPrescDAO.getEAuthPrescSequenceId(con, userName,
                    preauthPrescId, consId, patientId, primaryInsuranceCo);
                if (!eauthActDao.insertEAuth(con, itemPrescriptionId, patientId, userName,
                    itemType, itemBean, preauthPrescId, consId)) {
                  break txn;
                }
              }
              if (null != secondaryInsuranceCo && !secondaryInsuranceCo.equals("")) {
                preauthPrescId = eauthPrescDAO.getEAuthPrescSequenceId(con, userName,
                    preauthPrescId, consId, patientId, secondaryInsuranceCo);
                if (!eauthActDao.insertEAuth(con, itemPrescriptionId, patientId, userName,
                    itemType, itemBean, preauthPrescId, consId)) {
                  break txn;
                }
              }
            }
          }
        } else {
          preauthPrescId = (Integer) preAuthBean.get("preauth_presc_id");
          if (eauthActDao.updateEAuth(con, itemPrescriptionId, patientId, userName, itemType,
              itemBean, preauthPrescId) <= 0) {
            break txn;
          }
        }
      }
      flag = true;
    }

    Map resultMap = new HashMap();
    resultMap.put("success", flag);
    resultMap.put("itemBean", itemBean);

    return resultMap;

  }

  /**
   * Crud test.
   *
   * @param con the con
   * @param itemPrescriptionId the item prescription id
   * @param params the params
   * @param consultationId the consultation id
   * @param patientId the patient id
   * @param userName the user name
   * @param add the add
   * @return the map
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public Map crudTest(Connection con, int itemPrescriptionId, Map params, int consultationId,
      String patientId, String userName, boolean add) throws SQLException, IOException {

    String itemId = ConversionUtils.getParamValue(params, "item_id", "");
    String itemRemark = ConversionUtils.getParamValue(params, "item_remarks", "");
    String itemType = ConversionUtils.getParamValue(params, "presc_type", "");
    String ispackage = ConversionUtils.getParamValue(params, "ispackage", "");
    String requirePriorAuth = ConversionUtils.getParamValue(params, "requirePriorAuth", "N");
    String tpaId = ConversionUtils.getParamValue(params, "tpa_id", "");

    int preauthPrescId = 0;
    boolean flag = false;
    BasicDynaBean itemBean = testDao.getBean();
    txn: {

      itemBean.set("test_id", itemId);
      itemBean.set("test_remarks", itemRemark);
      itemBean.set("ispackage", new Boolean(ispackage));
      itemBean.set("preauth_required", requirePriorAuth != null ? requirePriorAuth : "N");
      itemBean.set("username", userName);

      if (add) {
        itemBean.set("op_test_pres_id", itemPrescriptionId);
        if (!testDao.insert(con, itemBean)) {
          break txn;
        }

        if (!tpaId.equals("") && requirePriorAuth != null && requirePriorAuth.equals("Y")) {
          BasicDynaBean regBean = regDao.findByKey("patient_id", patientId);
          if (regBean != null) {
            String primaryInsuranceCo = (String) regBean.get("primary_insurance_co");
            String secondaryInsuranceCo = (String) regBean.get("secondary_insurance_co");
            if (null != primaryInsuranceCo && !primaryInsuranceCo.equals("")) {
              preauthPrescId = eauthPrescDAO.getEAuthPrescSequenceId(con, userName, preauthPrescId,
                  consultationId, patientId, primaryInsuranceCo);
              if (!eauthActDao.insertEAuth(con, itemPrescriptionId, patientId, userName, itemType,
                  itemBean, preauthPrescId, consultationId)) {
                break txn;
              }
            }
            if (null != secondaryInsuranceCo && !secondaryInsuranceCo.equals("")) {
              preauthPrescId = eauthPrescDAO.getEAuthPrescSequenceId(con, userName, preauthPrescId,
                  consultationId, patientId, secondaryInsuranceCo);
              if (!eauthActDao.insertEAuth(con, itemPrescriptionId, patientId, userName, itemType,
                  itemBean, preauthPrescId, consultationId)) {
                break txn;
              }
            }
          }
        }

      } else {

        Map keys = new HashMap();
        keys.put("op_test_pres_id", itemPrescriptionId);
        itemBean.set("mod_time", new Timestamp(new java.util.Date().getTime()));
        if (testDao.update(con, itemBean.getMap(), keys) <= 0) {
          break txn;
        }

        BasicDynaBean preAuthBean = eauthActDao.findByKey("patient_pres_id", itemPrescriptionId);
        if (preAuthBean == null) {
          if (!tpaId.equals("") && requirePriorAuth != null && requirePriorAuth.equals("Y")) {
            BasicDynaBean regBean = regDao.findByKey("patient_id", patientId);
            if (regBean != null) {
              String primaryInsuranceCo = (String) regBean.get("primary_insurance_co");
              String secondaryInsuranceCo = (String) regBean.get("secondary_insurance_co");
              if (null != primaryInsuranceCo && !primaryInsuranceCo.equals("")) {
                preauthPrescId = eauthPrescDAO.getEAuthPrescSequenceId(con, userName,
                    preauthPrescId, consultationId, patientId, primaryInsuranceCo);
                if (!eauthActDao.insertEAuth(con, itemPrescriptionId, patientId, userName,
                    itemType, itemBean, preauthPrescId, consultationId)) {
                  break txn;
                }
              }
              if (null != secondaryInsuranceCo && !secondaryInsuranceCo.equals("")) {
                preauthPrescId = eauthPrescDAO.getEAuthPrescSequenceId(con, userName,
                    preauthPrescId, consultationId, patientId, secondaryInsuranceCo);
                if (!eauthActDao.insertEAuth(con, itemPrescriptionId, patientId, userName,
                    itemType, itemBean, preauthPrescId, consultationId)) {
                  break txn;
                }
              }
            }
          }
        } else {
          preauthPrescId = (Integer) preAuthBean.get("preauth_presc_id");
          if (eauthActDao.updateEAuth(con, itemPrescriptionId, patientId, userName, itemType,
              itemBean, preauthPrescId) <= 0) {
            break txn;
          }
        }
      }
      flag = true;
    }
    Map resultMap = new HashMap();
    resultMap.put("success", flag);
    resultMap.put("itemBean", itemBean);

    return resultMap;
  }

  /**
   * Crud doctor.
   *
   * @param con the con
   * @param itemPrescriptionId the item prescription id
   * @param params the params
   * @param consId the cons id
   * @param patientId the patient id
   * @param userName the user name
   * @param add the add
   * @return the map
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private Map crudDoctor(Connection con, int itemPrescriptionId, Map params, int consId,
      String patientId, String userName, boolean add) throws SQLException, IOException {
    String itemId = ConversionUtils.getParamValue(params, "item_id", "");
    String itemRemark = ConversionUtils.getParamValue(params, "item_remarks", "");

    boolean flag = false;
    BasicDynaBean itemBean = doctorDAO.getBean();

    txn: {

      itemBean.set("doctor_id", itemId);
      itemBean.set("cons_remarks", itemRemark);
      itemBean.set("username", userName);

      if (add) {
        itemBean.set("prescription_id", itemPrescriptionId);
        if (!doctorDAO.insert(con, itemBean)) {
          break txn;
        }

      } else {
        Map keys = new HashMap();
        keys.put("prescription_id", itemPrescriptionId);
        itemBean.set("mod_time", new Timestamp(new java.util.Date().getTime()));
        if (doctorDAO.update(con, itemBean.getMap(), keys) <= 0) {
          break txn;
        }

      }

      flag = true;
    }

    Map resultMap = new HashMap();
    resultMap.put("success", flag);
    resultMap.put("itemBean", itemBean);

    return resultMap;
  }

  /**
   * Crud operation.
   *
   * @param con the con
   * @param itemPrescriptionId the item prescription id
   * @param params the params
   * @param consId the cons id
   * @param patientId the patient id
   * @param userName the user name
   * @param add the add
   * @return the map
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private Map crudOperation(Connection con, int itemPrescriptionId, Map params, int consId,
      String patientId, String userName, boolean add) throws SQLException, IOException {
    String itemId = ConversionUtils.getParamValue(params, "item_id", "");
    String itemRemark = ConversionUtils.getParamValue(params, "item_remarks", "");
    String itemType = ConversionUtils.getParamValue(params, "presc_type", "");
    String requirePriorAuth = ConversionUtils.getParamValue(params, "requirePriorAuth", "N");
    String tpaId = ConversionUtils.getParamValue(params, "tpa_id", "");
    boolean flag = false;
    int preauthPrescId = 0;
    BasicDynaBean itemBean = optDao.getBean();

    txn: {

      itemBean.set("operation_id", itemId);
      itemBean.set("remarks", itemRemark);
      itemBean.set("preauth_required", requirePriorAuth != null ? requirePriorAuth : "N");

      if (add) {
        itemBean.set("prescription_id", itemPrescriptionId);
        if (!optDao.insert(con, itemBean)) {
          break txn;
        }

        if (!tpaId.equals("") && requirePriorAuth != null && requirePriorAuth.equals("Y")) {
          BasicDynaBean regBean = regDao.findByKey("patient_id", patientId);
          if (regBean != null) {
            String primaryInsuranceCo = (String) regBean.get("primary_insurance_co");
            String secondaryInsuranceCo = (String) regBean.get("secondary_insurance_co");
            if (null != primaryInsuranceCo && !primaryInsuranceCo.equals("")) {
              preauthPrescId = eauthPrescDAO.getEAuthPrescSequenceId(con, userName, preauthPrescId,
                  consId, patientId, primaryInsuranceCo);
              if (!eauthActDao.insertEAuth(con, itemPrescriptionId, patientId, userName, itemType,
                  itemBean, preauthPrescId, consId)) {
                break txn;
              }
            }
            if (null != secondaryInsuranceCo && !secondaryInsuranceCo.equals("")) {
              preauthPrescId = eauthPrescDAO.getEAuthPrescSequenceId(con, userName, preauthPrescId,
                  consId, patientId, secondaryInsuranceCo);
              if (!eauthActDao.insertEAuth(con, itemPrescriptionId, patientId, userName, itemType,
                  itemBean, preauthPrescId, consId)) {
                break txn;
              }
            }
          }
        }

      } else {

        Map keys = new HashMap();
        keys.put("prescription_id", itemPrescriptionId);
        itemBean.set("mod_time", new java.sql.Timestamp(new java.util.Date().getTime()));
        if (optDao.update(con, itemBean.getMap(), keys) <= 0) {
          break txn;
        }

        BasicDynaBean preAuthBean = eauthActDao.findByKey("patient_pres_id", itemPrescriptionId);
        if (preAuthBean == null) {
          if (!tpaId.equals("") && requirePriorAuth != null && requirePriorAuth.equals("Y")) {
            BasicDynaBean regBean = regDao.findByKey("patient_id", patientId);
            if (regBean != null) {
              String primaryInsuranceCo = (String) regBean.get("primary_insurance_co");
              String secondaryInsuranceCo = (String) regBean.get("secondary_insurance_co");
              if (null != primaryInsuranceCo && !primaryInsuranceCo.equals("")) {
                preauthPrescId = eauthPrescDAO.getEAuthPrescSequenceId(con, userName,
                    preauthPrescId, consId, patientId, primaryInsuranceCo);
                if (!eauthActDao.insertEAuth(con, itemPrescriptionId, patientId, userName,
                    itemType, itemBean, preauthPrescId, consId)) {
                  break txn;
                }
              }
              if (null != secondaryInsuranceCo && !secondaryInsuranceCo.equals("")) {
                preauthPrescId = eauthPrescDAO.getEAuthPrescSequenceId(con, userName,
                    preauthPrescId, consId, patientId, secondaryInsuranceCo);
                if (!eauthActDao.insertEAuth(con, itemPrescriptionId, patientId, userName,
                    itemType, itemBean, preauthPrescId, consId)) {
                  break txn;
                }
              }
            }
          }
        } else {
          preauthPrescId = (Integer) preAuthBean.get("preauth_presc_id");
          if (eauthActDao.updateEAuth(con, itemPrescriptionId, patientId, userName, itemType,
              itemBean, preauthPrescId) <= 0) {
            break txn;
          }
        }
      }

      flag = true;
    }

    Map resultMap = new HashMap();
    resultMap.put("success", flag);
    resultMap.put("itemBean", itemBean);

    return resultMap;
  }

}
