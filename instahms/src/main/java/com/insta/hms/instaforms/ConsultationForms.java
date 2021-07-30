/**
 *
 */

package com.insta.hms.instaforms;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.Registration.PatientInsurancePlanDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DynaBeanBuilder;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.eauthorization.EAuthPrescriptionActivitiesDAO;
import com.insta.hms.eauthorization.EAuthPrescriptionDAO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.HealthAuthorityPreferences.HealthAuthorityPreferencesDAO;
import com.insta.hms.master.PlanMaster.PlanMasterDAO;
import com.insta.hms.master.PrescriptionsMaster.PrescriptionsMasterDAO;
import com.insta.hms.outpatient.ConsultationFieldValuesDAO;
import com.insta.hms.outpatient.DoctorConsultationDAO;
import com.insta.hms.outpatient.OpPrescribeAction;
import com.insta.hms.outpatient.PatientPrescriptionDAO;
import com.insta.hms.pbmauthorization.PBMPrescriptionsDAO;
import com.insta.hms.usermanager.UserDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class ConsultationForms.
 *
 * @author insta
 */
public class ConsultationForms extends AbstractInstaForms {

  /** The log. */
  private static Logger log = LoggerFactory.getLogger(OpPrescribeAction.class);

  /** The e auth act dao. */
  EAuthPrescriptionActivitiesDAO eauthActDao = new EAuthPrescriptionActivitiesDAO();

  /** The e auth presc DAO. */
  EAuthPrescriptionDAO euthPrescDAO = new EAuthPrescriptionDAO();

  /** The pat med presc DAO. */
  GenericDAO patMedPrescDAO = new GenericDAO("patient_medicine_prescriptions");

  /** The s dao. */
  GenericDAO serDao = new GenericDAO("patient_service_prescriptions");

  /** The t dao. */
  GenericDAO testDao = new GenericDAO("patient_test_prescriptions");

  /** The nh dao. */
  // non hospital items prescriptions.
  GenericDAO nhDao = new GenericDAO("patient_other_prescriptions");

  /** The ot dao. */
  GenericDAO otDao = new GenericDAO("patient_operation_prescriptions");

  /** The doctor DAO. */
  // cross consultations
  GenericDAO doctorDAO = new GenericDAO("patient_consultation_prescriptions");

  /** The mm dao. */
  PrescriptionsMasterDAO mmDao = new PrescriptionsMasterDAO();

  /** The pbm presc DAO. */
  PBMPrescriptionsDAO pbmPrescDAO = new PBMPrescriptionsDAO();

  /** The consult dao. */
  DoctorConsultationDAO consultDao = new DoctorConsultationDAO();

  /** The consult field values dao. */
  ConsultationFieldValuesDAO consultFieldValuesDao = new ConsultationFieldValuesDAO();

  /** The user dao. */
  UserDAO userDao = new UserDAO();

  /** The reg dao. */
  GenericDAO regDao = new GenericDAO("patient_registration");

  /**
   * (non-Javadoc)
   * 
   * @see com.insta.hms.instaforms.AbstractInstaForms#getKeys()
   */
  public Map getKeys() {
    Map map = new HashMap();
    map.put("form_type", "Form_CONS");
    map.put("item_type", "CONS");
    map.put("section_item_id", "consultation_id");
    return map;
  }

  /**
   * (non-Javadoc)
   * 
   * @see com.insta.hms.instaforms.AbstractInstaForms#getComponents(java.util.Map)
   */
  @Override
  public BasicDynaBean getComponents(Map params) throws SQLException {
    int consultationId =
        Integer.parseInt(ConversionUtils.getParamValue(params, "consultation_id", "0"));
    Connection con = DataBaseUtil.getConnection();
    // Old consultation screen doesn't support Follow up form,
    // Form Type required while closing the consultation form op patient list.
    String formType =
        (String) params.get("form_type") == null ? "Form_CONS" : (String) params.get("form_type");
    PreparedStatement ps = null;
    try {
      String getFormsFromTx =
          " SELECT psd.section_id, min(psf.display_order) as display_order, psf.form_id,"
              + " fc.form_name FROM patient_section_details psd "
              + " JOIN patient_section_forms psf USING (section_detail_id) "
              + " JOIN form_components fc ON (fc.id=psf.form_id) "
              + " WHERE psd.section_item_id=? AND psf.form_type=? "
              + " GROUP BY psd.section_id, psf.form_id, fc.form_name ORDER BY min(display_order) ";

      ps = con.prepareStatement(getFormsFromTx);
      ps.setInt(1, consultationId);
      ps.setString(2, formType);
      List<BasicDynaBean> formList = DataBaseUtil.queryToDynaList(ps);

      if (formList == null || formList.isEmpty()) {
        String getActiveForms =
            " SELECT foo.section_id::int as section_id, foo.id as form_id, form_name "
                + " FROM (SELECT fc.id, fc.form_name, regexp_split_to_table(fc.sections, ',')"
                + " as section_id, "
                + " generate_series(1, array_upper(regexp_split_to_array(fc.sections, E','), 1))"
                + " as display_order " + " FROM (Select fc.id, fc.form_name, fc.sections from "
                + " form_components fc, form_department_details fdd where fdd.dept_id=? "
                + " and doctor_id=? and form_type='Form_CONS' and fdd.id=fc.id LIMIT 1) as fc )"
                + " as foo "
                + " LEFT JOIN section_master sm ON (sm.section_id::text=foo.section_id) "
                + " WHERE coalesce(sm.status, 'A')='A' order by display_order ";
        ps = con.prepareStatement(getActiveForms);
        BasicDynaBean consbean = consultDao.findConsultationExt(consultationId);
        if (consbean != null) {
          ps.setString(1, (String) consbean.get("dept_id"));
          ps.setString(2, (String) consbean.get("doctor_name"));
          formList = DataBaseUtil.queryToDynaList(ps);
          if (formList == null || formList.isEmpty()) {
            ps.setString(1, (String) consbean.get("dept_id"));
            ps.setString(2, "-1");
            formList = DataBaseUtil.queryToDynaList(ps);
          }
        }

        if (formList == null || formList.isEmpty()) {
          ps.setString(1, "-1");
          ps.setString(2, "-1");
          formList = DataBaseUtil.queryToDynaList(ps);
        }
      }
      DynaBeanBuilder builder = new DynaBeanBuilder();
      builder.add("sections");
      builder.add("form_id", Integer.class);
      builder.add("form_name");
      BasicDynaBean bean = builder.build();

      String sections = "";
      int formId = 0;
      boolean first = true;
      String formName = "";

      for (BasicDynaBean b : formList) {
        if (!first) {
          sections += ",";
        }
        sections += (Integer) b.get("section_id");
        formId = (Integer) b.get("form_id");
        formName = (String) b.get("form_name");

        first = false;
      }
      bean.set("sections", sections);
      bean.set("form_id", formId);
      bean.set("form_name", formName);

      return bean;

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.instaforms.AbstractInstaForms#savePrescription(java.sql.Connection,
   * java.util.Map, java.lang.String, int, boolean)
   */
  @Override
  public String savePrescription(Connection con, Map params, String userName, int sectionDetailId,
      boolean insert) throws SQLException, IOException, Exception {

    String patientId = ConversionUtils.getParamValue(params, "patient_id", "");
    boolean modEclaimPbm = (Boolean) RequestContext.getSession().getAttribute("mod_eclaim_pbm");
    boolean modEclaimErx = (Boolean) RequestContext.getSession().getAttribute("mod_eclaim_erx");
    boolean priorAuthRequired = false;

    BasicDynaBean visitInsDet = VisitDetailsDAO.getVisitDetails(patientId);
    BasicDynaBean patientPlan = getPrimaryPlan(patientId);
    int planId = ((Integer) visitInsDet.get("plan_id") != 0) ? (Integer) visitInsDet.get("plan_id")
        : (null != patientPlan) ? (Integer) patientPlan.get("plan_id") : 0;
    int patientCenterId = (Integer) visitInsDet.get("center_id");

    /*
     * PBM Prior Auth is required only when mod_eclaim_pbm module is enabled (mod_eclaim_erx is
     * disabled) and visit type is 'o' (or) mod_eclaim_erx module is enabled and visit type is 'o'
     */
    if (!modEclaimErx && modEclaimPbm) {
      BasicDynaBean planBean = new PlanMasterDAO().findByKey("plan_id", planId);
      if (planBean != null && ((String) planBean.get("require_pbm_authorization")).equals("Y")) {
        priorAuthRequired = true;
      }
    } else if (modEclaimErx) {
      priorAuthRequired = true;
    }

    int pbmPrescId = 0;

    BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
    String[] prescribedIds = (String[]) params.get("item_prescribed_id");
    String[] itemType = (String[]) params.get("itemType");
    int consId = Integer.parseInt(ConversionUtils.getParameter(params, "consultation_id", 0));
    String[] specialInstructions = (String[]) params.get("special_instr");
    String[] nonHospMedicine = (String[]) params.get("non_hosp_medicine");
    String[] addToFavouritesAr = (String[]) params.get("addToFavourite");
    String[] delItems = (String[]) params.get("delItem");
    int drugCount = 0;
    Map map = null;
    boolean flag = true;
    String error = null;

    BasicDynaBean consRecord = consultDao.findConsultationExt(consId);
    txn: {
      if (prescribedIds != null) {
        flag = false;

        for (int i = 0; i < prescribedIds.length - 1; i++) {
          int itemPrescriptionId = 0;
          PatientPrescriptionDAO patPrescDAO = new PatientPrescriptionDAO();
          if (prescribedIds[i].equals("_")) {
            BasicDynaBean bean = patPrescDAO.getBean();
            itemPrescriptionId = patPrescDAO.getNextSequence();

            bean.set("patient_presc_id", itemPrescriptionId);
            bean.set("status", "P");
            bean.set("presc_type", itemType[i]);
            bean.set("consultation_id", consId);
            bean.set("store_item", genericPrefs.get("prescription_uses_stores").equals("Y")
                && itemType[i].equals("Medicine") && !(new Boolean(nonHospMedicine[i])));
            bean.set("special_instr", specialInstructions[i]);
            bean.set("username", userName);

            if (!patPrescDAO.insert(con, bean)) {
              break txn;
            }
          } else {
            itemPrescriptionId = Integer.parseInt(prescribedIds[i]);
            BasicDynaBean bean = patPrescDAO.getBean();
            bean.set("special_instr", specialInstructions[i]);
            bean.set("username", userName);
            boolean deleteItem = new Boolean(delItems[i]);
            if (!deleteItem && patPrescDAO.update(con, bean.getMap(), "patient_presc_id",
                itemPrescriptionId) != 1) {
              break txn;
            }
            if (deleteItem && !patPrescDAO.delete(con, "patient_presc_id", itemPrescriptionId)) {
              break txn;
            }
          }
          BasicDynaBean itemBean = null;

          if (itemType[i].equals("Medicine") && !new Boolean(nonHospMedicine[i])) {
            map = crudMedicine(con, itemPrescriptionId, params, i, consId, patientCenterId,
                priorAuthRequired, modEclaimErx, pbmPrescId, drugCount, genericPrefs, userName);
            pbmPrescId = (Integer) map.get("pbmPrescId");
            drugCount = (Integer) map.get("drugCount");

          } else if (itemType[i].equals("Inv.")) {
            map = crudTest(con, itemPrescriptionId, params, i, consId, patientId, userName,
                visitInsDet);
          } else if (itemType[i].equals("Service")) {
            map = crudService(con, itemPrescriptionId, params, i, consId, patientId, userName,
                visitInsDet);
          } else if (itemType[i].equals("Doctor")) {
            map = crudDoctor(con, itemPrescriptionId, params, i, consId, patientId, userName,
                visitInsDet);
          } else if (itemType[i].equals("NonHospital")
              || (itemType[i].equals("Medicine") && new Boolean(nonHospMedicine[i]))) {
            map = crudNonHospital(con, itemPrescriptionId, params, i, consId, patientId, userName);
          } else if (itemType[i].equals("Operation")) {
            map = crudOperation(con, itemPrescriptionId, params, i, consId, patientId, userName,
                visitInsDet);
          }

          if (!(Boolean) map.get("success")) {
            break txn;
          }
          itemBean = (BasicDynaBean) map.get("itemBean");
          Boolean addToFavourite = new Boolean(addToFavouritesAr[i]);

          if (addToFavourite) {
            if (!DoctorConsultationDAO.insertFavourites(con, itemPrescriptionId, itemBean,
                itemType[i], (String) consRecord.get("doctor_name"),
                (String) genericPrefs.get("prescription_uses_stores"),
                new Boolean(nonHospMedicine[i]), specialInstructions[i])) {
              error = "Failed to insert doctor favourites.";
              break txn;
            }

          }
        }
      }

      // Update drug count in PBM Prescription.
      if (pbmPrescId != 0) {
        List<BasicDynaBean> pbmMedPrescList =
            patMedPrescDAO.findAllByKey(con, "pbm_presc_id", pbmPrescId);
        drugCount = pbmMedPrescList.size();

        BasicDynaBean pbmBean = pbmPrescDAO.getBean();
        pbmBean.set("pbm_presc_id", pbmPrescId);
        pbmBean.set("drug_count", drugCount);
        pbmPrescDAO.updateWithName(con, pbmBean.getMap(), "pbm_presc_id");
      }
      flag = true;
    }

    return flag ? null : error;
  }

  /**
   * Gets the primary plan.
   *
   * @param patientId
   *          the patient id
   * @return the primary plan
   * @throws SQLException
   *           the SQL exception
   */
  private BasicDynaBean getPrimaryPlan(String patientId) throws SQLException {

    // Patient plan / tpa information should come from patient insurance plan rather than patient
    // visit table
    PatientInsurancePlanDAO pipdao = new PatientInsurancePlanDAO();
    List<BasicDynaBean> patientPlans = pipdao.getPlanDetails(patientId);
    BasicDynaBean patientPlan = null;
    if (null != patientPlans && patientPlans.size() > 0) {
      patientPlan = patientPlans.get(0); // primary plan
    }
    return patientPlan;
  }

  /**
   * Crud medicine.
   *
   * @param con
   *          the con
   * @param itemPrescriptionId
   *          the item prescription id
   * @param params
   *          the params
   * @param i
   *          the i
   * @param consultationId
   *          the consultation id
   * @param patientCenterId
   *          the patient center id
   * @param priorAuthRequired
   *          the prior auth required
   * @param mod_eclaim_erx
   *          the mod eclaim erx
   * @param pbmPrescId
   *          the pbm presc id
   * @param drugCount
   *          the drug count
   * @param genericPrefs
   *          the generic prefs
   * @param userName
   *          the user name
   * @return the map
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  /*
   * create or update or delete medicine.
   */
  private Map crudMedicine(Connection con, int itemPrescriptionId, Map params, int index,
      int consultationId, int patientCenterId, boolean priorAuthRequired, boolean modEclaimErx,
      int pbmPrescId, int drugCount, BasicDynaBean genericPrefs, String userName)
      throws SQLException, IOException {

    String useStoreItems = (String) genericPrefs.get("prescription_uses_stores");
    GenericDAO medDao = new GenericDAO(useStoreItems.equals("Y") ? "patient_medicine_prescriptions"
        : "patient_other_medicine_prescriptions");

    String prescribedId = ConversionUtils.getParameter(params, "item_prescribed_id", index);
    String itemName = ConversionUtils.getParameter(params, "item_name", index);
    String itemId = ConversionUtils.getParameter(params, "item_id", index);
    String adminStrenth = ConversionUtils.getParameter(params, "admin_strength", index);
    String medFrequencie = ConversionUtils.getParameter(params, "frequency", index);
    String duration = ConversionUtils.getParameter(params, "duration", index);
    String durationUnit = ConversionUtils.getParameter(params, "duration_units", index);
    String medQty = ConversionUtils.getParameter(params, "medicine_quantity", index);
    String itemRemark = ConversionUtils.getParameter(params, "item_remarks", index);
    String delItem = ConversionUtils.getParameter(params, "delItem", index);
    String routeOfAdmin = ConversionUtils.getParameter(params, "route_id", index);
    // strength is the dosage to the patient how much to take (1 tab or 1/2 tab or 5ml or 10ml)
    String medicineStrength = ConversionUtils.getParameter(params, "strength", index);
    String genericCode = ConversionUtils.getParameter(params, "generic_code", index);
    String itemFormId = ConversionUtils.getParameter(params, "item_form_id", index);
    // item_strength is the medicine strength (100mg, 200mg, 500ml etc.,)
    String itemStrength = ConversionUtils.getParameter(params, "item_strength", index);
    String itemStrengthUnit = ConversionUtils.getParameter(params, "item_strength_units", index);
    String consumptionUom = ConversionUtils.getParameter(params, "consumption_uom", index);
    String sendForErx = ConversionUtils.getParameter(params, "send_item_for_erx", index);
    String refills = ConversionUtils.getParameter(params, "refills", index);

    String prescByGenerics =
        (String) HealthAuthorityPreferencesDAO
            .getHealthAuthorityPreferences(
                CenterMasterDAO.getHealthAuthorityForCenter(patientCenterId))
            .getPrescriptions_by_generics();
    Boolean prescriptionsByGenerics = useStoreItems.equals("Y") && prescByGenerics.equals("Y");

    boolean deleteItem = new Boolean(delItem);
    BasicDynaBean itemBean = medDao.getBean();
    String prescribedIdColName = "";
    boolean flag = false;

    txn: {
      if (!duration.equals("")) {
        itemBean.set("duration", Integer.parseInt(duration));
        itemBean.set("duration_units", durationUnit);
      } else {
        itemBean.set("duration", null);
        itemBean.set("duration_units", null);
      }
      if (!medQty.equals("")) {
        itemBean.set("medicine_quantity", Integer.parseInt(medQty));
      } else {
        itemBean.set("medicine_quantity", null);
      }
      if (useStoreItems.equals("Y")) {
        if (!prescriptionsByGenerics) {
          itemBean.set("medicine_id", Integer.parseInt(itemId));
        }
        // update the generic_code always when pharmacy module is enabled
        itemBean.set("generic_code", genericCode);
        prescribedIdColName = "op_medicine_pres_id";
      } else {
        itemBean.set("medicine_name", itemName);
        prescribedIdColName = "prescription_id";
      }
      itemBean.set("admin_strength", adminStrenth);
      itemBean.set("frequency", medFrequencie);
      itemBean.set("medicine_remarks", itemRemark);
      itemBean.set("strength", medicineStrength);
      itemBean.set("item_strength", itemStrength);
      itemBean.set("consumption_uom", consumptionUom);
      itemBean.set("username", userName);
      itemBean.set("refills", refills);
      if (useStoreItems.equals("Y")) { // erx available only for store items.
        itemBean.set("send_for_erx", sendForErx);
      }
      if (!itemStrengthUnit.equals("")) {
        itemBean.set("item_strength_units", Integer.parseInt(itemStrengthUnit));
      } else {
        itemBean.set("item_strength_units", null);
      }
      if (!itemFormId.equals("")) {
        itemBean.set("item_form_id", Integer.parseInt(itemFormId));
      } else {
        itemBean.set("item_form_id", null);
      }
      if (!routeOfAdmin.equals("")) {
        itemBean.set("route_of_admin", Integer.parseInt(routeOfAdmin));
      } else {
        itemBean.set("route_of_admin", null);
      }
      if (prescribedId.equals("_")) {
        if (!useStoreItems.equals("Y") && !PrescriptionsMasterDAO.medicineExisits(itemName)) {
          BasicDynaBean presMedMasterBean = mmDao.getBean();
          presMedMasterBean.set("medicine_name", itemName);
          presMedMasterBean.set("status", "A");

          if (!mmDao.insert(con, presMedMasterBean)) {
            break txn;
          }
        }
        itemBean.set(prescribedIdColName, itemPrescriptionId);

        // Insert item into patient_other_medicine_prescriptions (or) patient_medicine_prescriptions
        if (!medDao.insert(con, itemBean)) {
          break txn;
        }
      } else {
        if (deleteItem) {
          if (!medDao.delete(con, prescribedIdColName, itemPrescriptionId)) {
            break txn;
          }
        } else {
          Map keys = new HashMap();
          keys.put(prescribedIdColName, itemPrescriptionId);
          itemBean.set("mod_time", new Timestamp(new java.util.Date().getTime()));
          if (medDao.update(con, itemBean.getMap(), keys) <= 0) {
            break txn;
          }
        }
      }

      // Update pbm_presc_id and medicine id in patient_medicine_prescriptions
      if (!deleteItem && priorAuthRequired && prescribedIdColName.equals("op_medicine_pres_id")) {
        drugCount++;
        // When mod_eclaim_erx is enabled, all medicines prescribed in the consultation
        // will be with in a single prescription.
        // So, get the pbm_presc_id if an id exists to save medicines prescriptions.
        if (modEclaimErx) {

          if (pbmPrescId == 0) {
            BasicDynaBean opMedPrescBean =
                patMedPrescDAO.findByKey(con, "op_medicine_pres_id", itemPrescriptionId);
            if (opMedPrescBean != null) {
              pbmPrescId = opMedPrescBean.get("pbm_presc_id") != null
                  ? (Integer) opMedPrescBean.get("pbm_presc_id") : 0;
            }
          }

          // Get the saved pbm_presc_id.
          pbmPrescId = pbmPrescDAO.updatePBMAndMedicineId(con, consultationId, itemPrescriptionId,
              pbmPrescId);
          if (pbmPrescId == -1) {
            break txn;
          }

        } else {

          // When mod_eclaim_erx is not enabled and mod_eclaim_pbm is enabled,
          // medicines prescribed in the consultation can fall into multiple prescriptions.
          // So, get the latest Open pbm_presc_id if exists to save medicines prescriptions.
          // There may be a pbm prescription in Sent mode. In such case, a new Id is generated for
          // later entries.
          // Also, new prescription(s) can be created by Pharmacist in Pharmacy for the same
          // consultation.
          if (pbmPrescId == 0) {
            pbmPrescId = new PBMPrescriptionsDAO().getLatestPBMPrescId(consultationId);
          }
          // Get the saved pbm_presc_id.
          pbmPrescId = pbmPrescDAO.updatePBMAndMedicineId(con, consultationId, itemPrescriptionId,
              pbmPrescId);
          if (pbmPrescId == -1) {
            break txn;
          }
        }
      }
      flag = true;
    }
    Map resultMap = new HashMap();
    resultMap.put("success", flag);
    resultMap.put("pbmPrescId", pbmPrescId);
    resultMap.put("drugCount", drugCount);
    resultMap.put("itemBean", itemBean);

    return resultMap;
  }

  /**
   * Crud test.
   *
   * @param con
   *          the con
   * @param itemPrescriptionId
   *          the item prescription id
   * @param params
   *          the params
   * @param index
   *          the index
   * @param consultationId
   *          the consultation id
   * @param patientId
   *          the patient id
   * @param userName
   *          the user name
   * @param regBean
   *          the reg bean
   * @return the map
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public Map crudTest(Connection con, int itemPrescriptionId, Map params, int index,
      int consultationId, String patientId, String userName, BasicDynaBean regBean)
      throws SQLException, IOException {

    String prescribedId = ConversionUtils.getParameter(params, "item_prescribed_id", index);
    String itemId = ConversionUtils.getParameter(params, "item_id", index);
    String itemRemark = ConversionUtils.getParameter(params, "item_remarks", index);
    String delItem = ConversionUtils.getParameter(params, "delItem", index);
    String itemType = ConversionUtils.getParameter(params, "itemType", index);
    String ispackage = ConversionUtils.getParameter(params, "ispackage", index);
    String requirePriorAuth = ConversionUtils.getParameter(params, "requirePriorAuth", index);

    boolean deleteItem = new Boolean(delItem);
    boolean flag = false;
    BasicDynaBean itemBean = testDao.getBean();
    txn: {

      itemBean.set("test_id", itemId);
      itemBean.set("test_remarks", itemRemark);
      itemBean.set("ispackage", new Boolean(ispackage));
      itemBean.set("preauth_required", requirePriorAuth != null ? requirePriorAuth : "N");
      itemBean.set("username", userName);

      if (prescribedId.equals("_")) {
        itemBean.set("op_test_pres_id", itemPrescriptionId);
        if (!testDao.insert(con, itemBean)) {
          break txn;
        }
        if (requirePriorAuth != null && (!new Boolean(ispackage))) {

          if (regBean != null) {
            String primaryInsuranceCo = (String) regBean.get("primary_insurance_co");
            String secondaryInsuranceCo = (String) regBean.get("secondary_insurance_co");
            if (null != primaryInsuranceCo && !primaryInsuranceCo.equals("")) {
              int preauthPrescId = 0;
              preauthPrescId = euthPrescDAO.getEAuthPrescSequenceId(con, userName, preauthPrescId,
                  consultationId, patientId, primaryInsuranceCo);
              if (!eauthActDao.insertEAuth(con, itemPrescriptionId, patientId, userName, itemType,
                  itemBean, preauthPrescId, consultationId)) {
                break txn;
              }
            }
            if (null != secondaryInsuranceCo && !secondaryInsuranceCo.equals("")) {
              int preauthPrescId = 0;
              preauthPrescId = euthPrescDAO.getEAuthPrescSequenceId(con, userName, preauthPrescId,
                  consultationId, patientId, secondaryInsuranceCo);
              if (!eauthActDao.insertEAuth(con, itemPrescriptionId, patientId, userName, itemType,
                  itemBean, preauthPrescId, consultationId)) {
                break txn;
              }
            }
          }
        }
      } else {
        List<BasicDynaBean> preAuthBeanList =
            eauthActDao.findAllByKey("patient_pres_id", itemPrescriptionId);

        if (deleteItem) {
          if (!testDao.delete(con, "op_test_pres_id", itemPrescriptionId)) {
            break txn;
          }

          if (!preAuthBeanList.isEmpty() && !new Boolean(ispackage)) {
            if (eauthActDao.deleteEAuth(con, itemPrescriptionId, userName) <= 0) {
              break txn;
            }
          }
        } else {
          Map keys = new HashMap();
          keys.put("op_test_pres_id", itemPrescriptionId);
          itemBean.set("mod_time", new Timestamp(new java.util.Date().getTime()));
          if (testDao.update(con, itemBean.getMap(), keys) <= 0) {
            break txn;
          }

          if (preAuthBeanList.isEmpty()) {
            if (requirePriorAuth != null && (!new Boolean(ispackage))) {
              if (regBean != null) {
                String primaryInsuranceCo = (String) regBean.get("primary_insurance_co");
                String secondaryInsuranceCo = (String) regBean.get("secondary_insurance_co");
                if (null != primaryInsuranceCo && !primaryInsuranceCo.equals("")) {
                  int preauthPrescId = 0;
                  preauthPrescId = euthPrescDAO.getEAuthPrescSequenceId(con, userName,
                      preauthPrescId, consultationId, patientId, primaryInsuranceCo);
                  if (!eauthActDao.insertEAuth(con, itemPrescriptionId, patientId, userName,
                      itemType, itemBean, preauthPrescId, consultationId)) {
                    break txn;
                  }
                }
                if (null != secondaryInsuranceCo && !secondaryInsuranceCo.equals("")) {
                  int preauthPrescId = 0;
                  preauthPrescId = euthPrescDAO.getEAuthPrescSequenceId(con, userName,
                      preauthPrescId, consultationId, patientId, secondaryInsuranceCo);
                  if (!eauthActDao.insertEAuth(con, itemPrescriptionId, patientId, userName,
                      itemType, itemBean, preauthPrescId, consultationId)) {
                    break txn;
                  }
                }
              }
            }
          } else {
            if (!new Boolean(ispackage)) {
              for (BasicDynaBean preAuthBean : preAuthBeanList) {
                if (eauthActDao.updateEAuth(con, itemPrescriptionId, patientId, userName, itemType,
                    itemBean, (Integer) preAuthBean.get("preauth_presc_id")) <= 0) {
                  break txn;
                }
              }
            }
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
   * Crud service.
   *
   * @param con
   *          the con
   * @param itemPrescriptionId
   *          the item prescription id
   * @param params
   *          the params
   * @param i
   *          the i
   * @param consId
   *          the cons id
   * @param patientId
   *          the patient id
   * @param userName
   *          the user name
   * @param regBean
   *          the reg bean
   * @return the map
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  private Map crudService(Connection con, int itemPrescriptionId, Map params, int index, int consId,
      String patientId, String userName, BasicDynaBean regBean) throws SQLException, IOException {
    String prescribedId = ConversionUtils.getParameter(params, "item_prescribed_id", index);
    String itemId = ConversionUtils.getParameter(params, "item_id", index);
    String itemRemark = ConversionUtils.getParameter(params, "item_remarks", index);
    String delItem = ConversionUtils.getParameter(params, "delItem", index);
    String itemType = ConversionUtils.getParameter(params, "itemType", index);
    String requirePriorAuth = ConversionUtils.getParameter(params, "requirePriorAuth", index);
    String toothNumberUNV = ConversionUtils.getParameter(params, "tooth_unv_number", index);
    String toothNumberFDI = ConversionUtils.getParameter(params, "tooth_fdi_number", index);
    String serviceQty = ConversionUtils.getParameter(params, "service_qty", index);

    boolean deleteItem = new Boolean(delItem);
    boolean flag = false;
    BasicDynaBean itemBean = serDao.getBean();

    txn: {

      itemBean.set("service_id", itemId);
      itemBean.set("service_remarks", itemRemark);
      itemBean.set("username", userName);
      itemBean.set("tooth_unv_number", toothNumberUNV);
      itemBean.set("tooth_fdi_number", toothNumberFDI);
      itemBean.set("qty", Integer.parseInt(serviceQty));
      itemBean.set("preauth_required", requirePriorAuth != null ? requirePriorAuth : "N");

      if (prescribedId.equals("_")) {
        itemBean.set("op_service_pres_id", itemPrescriptionId);
        if (!serDao.insert(con, itemBean)) {
          break txn;
        }
        if (requirePriorAuth != null) {
          if (regBean != null) {
            String primaryInsuranceCo = (String) regBean.get("primary_insurance_co");
            String secondaryInsuranceCo = (String) regBean.get("secondary_insurance_co");
            if (null != primaryInsuranceCo && !primaryInsuranceCo.equals("")) {
              int preauthPrescId = 0;
              preauthPrescId = euthPrescDAO.getEAuthPrescSequenceId(con, userName, preauthPrescId,
                  consId, patientId, primaryInsuranceCo);
              if (!eauthActDao.insertEAuth(con, itemPrescriptionId, patientId, userName, itemType,
                  itemBean, preauthPrescId, consId)) {
                break txn;
              }
            }
            if (null != secondaryInsuranceCo && !secondaryInsuranceCo.equals("")) {
              int preauthPrescId = 0;
              preauthPrescId = euthPrescDAO.getEAuthPrescSequenceId(con, userName, preauthPrescId,
                  consId, patientId, secondaryInsuranceCo);
              if (!eauthActDao.insertEAuth(con, itemPrescriptionId, patientId, userName, itemType,
                  itemBean, preauthPrescId, consId)) {
                break txn;
              }
            }
          }
        }

      } else {
        List<BasicDynaBean> preAuthBeanList =
            eauthActDao.findAllByKey("patient_pres_id", itemPrescriptionId);
        if (deleteItem) {
          if (!serDao.delete(con, "op_service_pres_id", itemPrescriptionId)) {
            break txn;
          }
          if (!preAuthBeanList.isEmpty()
              && eauthActDao.deleteEAuth(con, itemPrescriptionId, userName) <= 0) {
            break txn;
          }
        } else {
          Map keys = new HashMap();
          keys.put("op_service_pres_id", itemPrescriptionId);
          itemBean.set("mod_time", new Timestamp(new java.util.Date().getTime()));
          if (serDao.update(con, itemBean.getMap(), keys) <= 0) {
            break txn;
          }

          if (preAuthBeanList.isEmpty()) {
            if (requirePriorAuth != null) {
              if (regBean != null) {
                String primaryInsuranceCo = (String) regBean.get("primary_insurance_co");
                String secondaryInsuranceCo = (String) regBean.get("secondary_insurance_co");
                if (null != primaryInsuranceCo && !primaryInsuranceCo.equals("")) {
                  int preauthPrescId = 0;
                  preauthPrescId = euthPrescDAO.getEAuthPrescSequenceId(con, userName,
                      preauthPrescId, consId, patientId, primaryInsuranceCo);
                  if (!eauthActDao.insertEAuth(con, itemPrescriptionId, patientId, userName,
                      itemType, itemBean, preauthPrescId, consId)) {
                    break txn;
                  }
                }
                if (null != secondaryInsuranceCo && !secondaryInsuranceCo.equals("")) {
                  int preauthPrescId = 0;
                  preauthPrescId = euthPrescDAO.getEAuthPrescSequenceId(con, userName,
                      preauthPrescId, consId, patientId, secondaryInsuranceCo);
                  if (!eauthActDao.insertEAuth(con, itemPrescriptionId, patientId, userName,
                      itemType, itemBean, preauthPrescId, consId)) {
                    break txn;
                  }
                }
              }
            }
          } else {
            for (BasicDynaBean preAuthBean : preAuthBeanList) {
              if (eauthActDao.updateEAuth(con, itemPrescriptionId, patientId, userName, itemType,
                  itemBean, (Integer) preAuthBean.get("preauth_presc_id")) <= 0) {
                break txn;
              }
            }
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
   * @param con
   *          the con
   * @param itemPrescriptionId
   *          the item prescription id
   * @param params
   *          the params
   * @param i
   *          the i
   * @param consId
   *          the cons id
   * @param patientId
   *          the patient id
   * @param userName
   *          the user name
   * @param regBean
   *          the reg bean
   * @return the map
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  private Map crudDoctor(Connection con, int itemPrescriptionId, Map params, int index, int consId,
      String patientId, String userName, BasicDynaBean regBean) throws SQLException, IOException {
    String prescribedId = ConversionUtils.getParameter(params, "item_prescribed_id", index);
    String itemId = ConversionUtils.getParameter(params, "item_id", index);
    String itemRemark = ConversionUtils.getParameter(params, "item_remarks", index);
    String delItem = ConversionUtils.getParameter(params, "delItem", index);
    String itemType = ConversionUtils.getParameter(params, "itemType", index);
    String requirePriorAuth = ConversionUtils.getParameter(params, "requirePriorAuth", index);

    boolean flag = false;
    boolean deleteItem = new Boolean(delItem);
    BasicDynaBean itemBean = doctorDAO.getBean();

    txn: {

      itemBean.set("doctor_id", itemId);
      itemBean.set("cons_remarks", itemRemark);
      itemBean.set("username", userName);
      itemBean.set("preauth_required", requirePriorAuth != null ? requirePriorAuth : "N");

      if (prescribedId.equals("_")) {
        itemBean.set("prescription_id", itemPrescriptionId);
        if (!doctorDAO.insert(con, itemBean)) {
          break txn;
        }
        if (requirePriorAuth != null) {
          if (regBean != null) {
            String primaryInsuranceCo = (String) regBean.get("primary_insurance_co");
            String secondaryInsuranceCo = (String) regBean.get("secondary_insurance_co");
            if (null != primaryInsuranceCo && !primaryInsuranceCo.equals("")) {
              int preauthPrescId = 0;
              preauthPrescId = euthPrescDAO.getEAuthPrescSequenceId(con, userName, preauthPrescId,
                  consId, patientId, primaryInsuranceCo);
              if (!eauthActDao.insertEAuth(con, itemPrescriptionId, patientId, userName, itemType,
                  itemBean, preauthPrescId, consId)) {
                break txn;
              }
            }
            if (null != secondaryInsuranceCo && !secondaryInsuranceCo.equals("")) {
              int preauthPrescId = 0;
              preauthPrescId = euthPrescDAO.getEAuthPrescSequenceId(con, userName, preauthPrescId,
                  consId, patientId, secondaryInsuranceCo);
              if (!eauthActDao.insertEAuth(con, itemPrescriptionId, patientId, userName, itemType,
                  itemBean, preauthPrescId, consId)) {
                break txn;
              }
            }
          }
        }
      } else {
        List<BasicDynaBean> preAuthBeanList =
            eauthActDao.findAllByKey("patient_pres_id", itemPrescriptionId);
        if (deleteItem) {
          if (!doctorDAO.delete(con, "prescription_id", itemPrescriptionId)) {
            break txn;
          }
          if (!preAuthBeanList.isEmpty()
              && eauthActDao.deleteEAuth(con, itemPrescriptionId, userName) <= 0) {
            break txn;
          }
        } else {
          Map keys = new HashMap();
          keys.put("prescription_id", itemPrescriptionId);
          itemBean.set("mod_time", new Timestamp(new java.util.Date().getTime()));
          if (doctorDAO.update(con, itemBean.getMap(), keys) <= 0) {
            break txn;
          }
          if (preAuthBeanList.isEmpty()) {
            if (requirePriorAuth != null) {
              if (regBean != null) {
                String primaryInsuranceCo = (String) regBean.get("primary_insurance_co");
                String secondaryInsuranceCo = (String) regBean.get("secondary_insurance_co");
                if (null != primaryInsuranceCo && !primaryInsuranceCo.equals("")) {
                  int preauthPrescId = 0;
                  preauthPrescId = euthPrescDAO.getEAuthPrescSequenceId(con, userName,
                      preauthPrescId, consId, patientId, primaryInsuranceCo);
                  if (!eauthActDao.insertEAuth(con, itemPrescriptionId, patientId, userName,
                      itemType, itemBean, preauthPrescId, consId)) {
                    break txn;
                  }
                }
                if (null != secondaryInsuranceCo && !secondaryInsuranceCo.equals("")) {
                  int preauthPrescId = 0;
                  preauthPrescId = euthPrescDAO.getEAuthPrescSequenceId(con, userName,
                      preauthPrescId, consId, patientId, secondaryInsuranceCo);
                  if (!eauthActDao.insertEAuth(con, itemPrescriptionId, patientId, userName,
                      itemType, itemBean, preauthPrescId, consId)) {
                    break txn;
                  }
                }
              }
            }
          } else {
            for (BasicDynaBean preAuthBean : preAuthBeanList) {
              if (eauthActDao.updateEAuth(con, itemPrescriptionId, patientId, userName, itemType,
                  itemBean, (Integer) preAuthBean.get("preauth_presc_id")) <= 0) {
                break txn;
              }
            }
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
   * Crud operation.
   *
   * @param con
   *          the con
   * @param itemPrescriptionId
   *          the item prescription id
   * @param params
   *          the params
   * @param i
   *          the i
   * @param consId
   *          the cons id
   * @param patientId
   *          the patient id
   * @param userName
   *          the user name
   * @param regBean
   *          the reg bean
   * @return the map
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  private Map crudOperation(Connection con, int itemPrescriptionId, Map params, int index,
      int consId, String patientId, String userName, BasicDynaBean regBean)
      throws SQLException, IOException {
    String prescribedId = ConversionUtils.getParameter(params, "item_prescribed_id", index);
    String itemId = ConversionUtils.getParameter(params, "item_id", index);
    String itemRemark = ConversionUtils.getParameter(params, "item_remarks", index);
    String delItem = ConversionUtils.getParameter(params, "delItem", index);
    String itemType = ConversionUtils.getParameter(params, "itemType", index);
    String requirePriorAuth = ConversionUtils.getParameter(params, "requirePriorAuth", index);

    boolean flag = false;
    boolean deleteItem = new Boolean(delItem);
    BasicDynaBean itemBean = otDao.getBean();

    txn: {

      itemBean.set("operation_id", itemId);
      itemBean.set("remarks", itemRemark);
      itemBean.set("username", userName);
      itemBean.set("preauth_required", requirePriorAuth != null ? requirePriorAuth : "N");

      if (prescribedId.equals("_")) {
        itemBean.set("prescription_id", itemPrescriptionId);
        if (!otDao.insert(con, itemBean)) {
          break txn;
        }
        if (requirePriorAuth != null) {
          if (regBean != null) {
            String primaryInsuranceCo = (String) regBean.get("primary_insurance_co");
            String secondaryInsuranceCo = (String) regBean.get("secondary_insurance_co");
            if (null != primaryInsuranceCo && !primaryInsuranceCo.equals("")) {
              int preauthPrescId = 0;
              preauthPrescId = euthPrescDAO.getEAuthPrescSequenceId(con, userName, preauthPrescId,
                  consId, patientId, primaryInsuranceCo);
              if (!eauthActDao.insertEAuth(con, itemPrescriptionId, patientId, userName, itemType,
                  itemBean, preauthPrescId, consId)) {
                break txn;
              }
            }
            if (null != secondaryInsuranceCo && !secondaryInsuranceCo.equals("")) {
              int preauthPrescId = 0;
              preauthPrescId = euthPrescDAO.getEAuthPrescSequenceId(con, userName, preauthPrescId,
                  consId, patientId, secondaryInsuranceCo);
              if (!eauthActDao.insertEAuth(con, itemPrescriptionId, patientId, userName, itemType,
                  itemBean, preauthPrescId, consId)) {
                break txn;
              }
            }
          }
        }

      } else {
        itemPrescriptionId = Integer.parseInt(prescribedId);
        List<BasicDynaBean> preAuthBeanList =
            eauthActDao.findAllByKey("patient_pres_id", itemPrescriptionId);
        if (deleteItem) {
          if (!otDao.delete(con, "prescription_id", itemPrescriptionId)) {
            break txn;
          }
          if (!preAuthBeanList.isEmpty()
              && eauthActDao.deleteEAuth(con, itemPrescriptionId, userName) <= 0) {
            break txn;
          }
        } else {
          Map keys = new HashMap();
          keys.put("prescription_id", itemPrescriptionId);
          itemBean.set("mod_time", new java.sql.Timestamp(new java.util.Date().getTime()));
          if (otDao.update(con, itemBean.getMap(), keys) <= 0) {
            break txn;
          }
          if (preAuthBeanList.isEmpty()) {
            if (requirePriorAuth != null) {
              if (regBean != null) {
                String primaryInsuranceCo = (String) regBean.get("primary_insurance_co");
                String secondaryInsuranceCo = (String) regBean.get("secondary_insurance_co");
                if (null != primaryInsuranceCo && !primaryInsuranceCo.equals("")) {
                  int preauthPrescId = 0;
                  preauthPrescId = euthPrescDAO.getEAuthPrescSequenceId(con, userName,
                      preauthPrescId, consId, patientId, primaryInsuranceCo);
                  if (!eauthActDao.insertEAuth(con, itemPrescriptionId, patientId, userName,
                      itemType, itemBean, preauthPrescId, consId)) {
                    break txn;
                  }
                }
                if (null != secondaryInsuranceCo && !secondaryInsuranceCo.equals("")) {
                  int preauthPrescId = 0;
                  preauthPrescId = euthPrescDAO.getEAuthPrescSequenceId(con, userName,
                      preauthPrescId, consId, patientId, secondaryInsuranceCo);
                  if (!eauthActDao.insertEAuth(con, itemPrescriptionId, patientId, userName,
                      itemType, itemBean, preauthPrescId, consId)) {
                    break txn;
                  }
                }
              }
            }
          } else {
            for (BasicDynaBean preAuthBean : preAuthBeanList) {
              if (eauthActDao.updateEAuth(con, itemPrescriptionId, patientId, userName, itemType,
                  itemBean, (Integer) preAuthBean.get("preauth_presc_id")) <= 0) {
                break txn;
              }
            }
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
   * Crud non hospital.
   *
   * @param con
   *          the con
   * @param itemPrescriptionId
   *          the item prescription id
   * @param params
   *          the params
   * @param index
   *          the index
   * @param consId
   *          the cons id
   * @param patientId
   *          the patient id
   * @param userName
   *          the user name
   * @return the map
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public Map crudNonHospital(Connection con, int itemPrescriptionId, Map params, int index,
      int consId, String patientId, String userName) throws SQLException, IOException {
    String prescribedId = ConversionUtils.getParameter(params, "item_prescribed_id", index);
    String itemName = ConversionUtils.getParameter(params, "item_name", index);
    String adminStrenth = ConversionUtils.getParameter(params, "admin_strength", index);
    String medFrequencie = ConversionUtils.getParameter(params, "frequency", index);
    String duration = ConversionUtils.getParameter(params, "duration", index);
    String durationUnit = ConversionUtils.getParameter(params, "duration_units", index);
    String medQty = ConversionUtils.getParameter(params, "medicine_quantity", index);
    String itemRemark = ConversionUtils.getParameter(params, "item_remarks", index);
    String delItem = ConversionUtils.getParameter(params, "delItem", index);
    // strength is the dosage to the patient how much to take (1 tab or 1/2 tab or 5ml or 10ml)
    String medicineStrength = ConversionUtils.getParameter(params, "strength", index);
    String itemFormId = ConversionUtils.getParameter(params, "item_form_id", index);
    // item_strength is the medicine strength (100mg, 200mg, 500ml etc.,)
    String itemStrength = ConversionUtils.getParameter(params, "item_strength", index);
    String itemStrengthUnit = ConversionUtils.getParameter(params, "item_strength_units", index);
    String consumptionUom = ConversionUtils.getParameter(params, "consumption_uom", index);
    String nonHospMedicine = ConversionUtils.getParameter(params, "non_hosp_medicine", index);
    String refills = ConversionUtils.getParameter(params, "refills", index);

    boolean deleteItem = new Boolean(delItem);
    boolean flag = false;
    BasicDynaBean itemBean = nhDao.getBean();

    txn: {

      itemBean.set("item_name", itemName);
      itemBean.set("item_remarks", itemRemark);
      itemBean.set("non_hosp_medicine", new Boolean(nonHospMedicine));
      itemBean.set("consumption_uom", consumptionUom);
      itemBean.set("username", userName);
      if (!duration.equals("")) {
        itemBean.set("duration", Integer.parseInt(duration));
        itemBean.set("duration_units", durationUnit);
      } else {
        itemBean.set("duration", null);
        itemBean.set("duration_units", null);
      }
      if (!medQty.equals("")) {
        itemBean.set("medicine_quantity", Integer.parseInt(medQty));
      } else {
        itemBean.set("medicine_quantity", null);
      }
      itemBean.set("admin_strength", adminStrenth);
      itemBean.set("frequency", medFrequencie);
      itemBean.set("strength", medicineStrength);
      itemBean.set("item_strength", itemStrength);
      itemBean.set("refills", refills);
      if (!itemStrengthUnit.equals("")) {
        itemBean.set("item_strength_units", Integer.parseInt(itemStrengthUnit));
      } else {
        itemBean.set("item_strength_units", null);
      }
      if (!itemFormId.equals("")) {
        itemBean.set("item_form_id", Integer.parseInt(itemFormId));
      } else {
        itemBean.set("item_form_id", null);
      }
      if (prescribedId.equals("_")) {
        itemBean.set("prescription_id", itemPrescriptionId);
        if (!nhDao.insert(con, itemBean)) {
          break txn;
        }
      } else {
        if (deleteItem) {
          if (!nhDao.delete(con, "prescription_id", itemPrescriptionId)) {
            break txn;
          }
        } else {
          Map keys = new HashMap();
          keys.put("prescription_id", itemPrescriptionId);
          itemBean.set("mod_time", new java.sql.Timestamp(new java.util.Date().getTime()));
          if (nhDao.update(con, itemBean.getMap(), keys) <= 0) {
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
   * Run migration sql.
   *
   * @param con
   *          the con
   * @param docId
   *          the doc id
   * @param isOld
   *          the is old
   * @throws SQLException
   *           the SQL exception
   */
  private void runMigrationSql(Connection con, int docId, boolean isOld) throws SQLException {
    String migrationSql =
        "INSERT INTO patient_consultation_field_values (value_id,doc_id,field_id,field_value)" + "("
            + " SELECT DISTINCT nextval('patient_consultation_field_values_seq'), foo.doc_id, -1,"
            + " string_agg(foo.field_name_values, E'\n')" + " FROM" + " ("
            + " SELECT dhtf.field_name || ':-' || pc.field_value as field_name_values, pc.doc_id"
            + " FROM patient_consultation_field_values pc"
            + " JOIN doc_hvf_template_fields dhtf on (dhtf.field_id = pc.field_id)"
            + ") as foo where doc_id = ? GROUP BY doc_id"
            + " HAVING NOT EXISTS (SELECT 1 FROM patient_consultation_field_values"
            + " WHERE doc_id = foo.doc_id AND field_id = -1)" + ")";
    PreparedStatement migPstmt = null;
    PreparedStatement dpstmt = null;
    try {
      migPstmt = con.prepareStatement(migrationSql);
      migPstmt.setInt(1, docId);
      int rows = 0;
      if (isOld) {
        String deleteOldMigration =
            "DELETE FROM patient_consultation_field_values where doc_id = ? and field_id = -1";
        dpstmt = con.prepareStatement(deleteOldMigration);
        dpstmt.setInt(1, docId);
        dpstmt.executeUpdate();
        rows = migPstmt.executeUpdate();
        log.debug("Migrated rows :" + rows);
      } else {
        rows = migPstmt.executeUpdate();
        log.debug("Migrated rows :" + rows);
      }
    } finally {
      DataBaseUtil.closeConnections(null, migPstmt);
      DataBaseUtil.closeConnections(null, dpstmt);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.instaforms.AbstractInstaForms#saveConsultationNotes(java.sql.Connection,
   * java.util.Map, java.lang.String, int, boolean)
   */
  @Override
  public String saveConsultationNotes(Connection con, Map params, String userName,
      int sectionDetailId, boolean insert) throws SQLException, IOException, Exception {

    UserDAO userdao = new UserDAO(con);
    // do not process when prescription note taker is enabled.
    if (userdao.getPrescriptionNoteTaker(userName).equalsIgnoreCase("y")) {
      return null;
    }
    List errors = new ArrayList();
    int docId = Integer.parseInt(ConversionUtils.getParamValue(params, "doc_id", "0"));

    // saving the optional component : Consultation Notes.
    boolean flag = false;
    txn: {
      if (docId == 0) {
        List insertFieldsList = new ArrayList();
        String[] fieldIds = (String[]) params.get("field_id");
        if (fieldIds != null) {
          docId =
              DataBaseUtil.getIntValueFromDb("select nextval('patient_consultation_template_seq')");
          for (int i = 0; i < fieldIds.length; i++) {
            BasicDynaBean fieldsBean = consultFieldValuesDao.getBean();
            int valueId = consultFieldValuesDao.getNextSequence();
            ConversionUtils.copyIndexToDynaBean(params, i, fieldsBean, errors);
            fieldsBean.set("doc_id", docId);
            fieldsBean.set("value_id", valueId);
            insertFieldsList.add(fieldsBean);
          }
          if (!consultFieldValuesDao.insertAll(con, insertFieldsList)) {
            break txn;
          }
          runMigrationSql(con, docId, false);
        }
      } else {
        String[] fieldIds = (String[]) params.get("field_id");
        if (fieldIds != null) {
          for (int i = 0; i < fieldIds.length; i++) {
            BasicDynaBean fieldsBean = consultFieldValuesDao.getBean();
            fieldsBean.set("doc_id", docId);
            ConversionUtils.copyIndexToDynaBean(params, i, fieldsBean, errors);
            if (consultFieldValuesDao.updateWithNames(con, fieldsBean.getMap(),
                new String[] { "doc_id", "field_id" }) == 0) {
              break txn;
            }
          }
          if (!(fieldIds.length == 1 && Integer.parseInt(fieldIds[0]) == -1)) {
            runMigrationSql(con, docId, true);
          }
        }
      }

      int consId = Integer.parseInt(ConversionUtils.getParamValue(params, "consultation_id", "0"));
      int templateId = Integer.parseInt(ConversionUtils.getParamValue(params, "template_id", "0"));
      BasicDynaBean consBean = consultDao.getBean();
      consBean.set("doc_id", docId);
      consBean.set("template_id", templateId);

      if (consultDao.update(con, consBean.getMap(), "consultation_id", consId) == 0) {
        break txn;
      }
      flag = true;
    }

    return flag ? null : "Failed to update/insert the Consultation Notes section..";
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.instaforms.AbstractInstaForms#getSectionItemId(java.util.Map)
   */
  @Override
  public int getSectionItemId(Map params) {
    return Integer.parseInt(ConversionUtils.getParamValue(params, "consultation_id", "0"));
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.instaforms.AbstractInstaForms#getSectionDetails(int, java.lang.String,
   * java.lang.String, int, java.lang.String, java.lang.String, java.lang.String, int, int)
   */
  @Override
  public List getSectionDetails(int formId, String formType, String itemType, int sectionId,
      String linkedTo, String mrNo, String patientId, int itemId, int genericFormId)
      throws SQLException, IOException {
    List list = null;
    PatientSectionDetailsDAO sdDAO = new PatientSectionDetailsDAO();
    Connection con = DataBaseUtil.getConnection();
    try {
      if (linkedTo.equals("patient")) {
        BasicDynaBean record = PatientSectionDetailsDAO.getRecord(con, mrNo, patientId, itemId,
            genericFormId, sectionId, formId, itemType);
        if (record == null) {
          list = sdDAO.getPatientLevelSectionValues(mrNo, sectionId);
        } else {
          list = sdDAO.getSectionDetails(mrNo, patientId, itemId, genericFormId, formId, sectionId,
              itemType);
        }
      } else if (linkedTo.equals("visit")) {
        BasicDynaBean record = PatientSectionDetailsDAO.getRecord(con, mrNo, patientId, itemId,
            genericFormId, sectionId, formId, itemType);
        if (record == null) {
          list = sdDAO.getVisitLevelSectionValues(patientId, sectionId);
        } else {
          list = sdDAO.getSectionDetails(mrNo, patientId, itemId, genericFormId, formId, sectionId,
              itemType);
        }
      } else if (linkedTo.equals("order item")) {
        BasicDynaBean record = PatientSectionDetailsDAO.getRecord(con, mrNo, patientId, itemId,
            genericFormId, sectionId, formId, itemType);
        if (record == null) {
          list = sdDAO.getItemLevelSectionValues(itemId, itemType, sectionId);
        } else {
          list = sdDAO.getSectionDetails(mrNo, patientId, itemId, genericFormId, formId, sectionId,
              itemType);
        }
      } else if (linkedTo.equals("form")) {
        list = sdDAO.getSectionDetails(mrNo, patientId, itemId, genericFormId, formId, sectionId,
            itemType);
      }

    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
    return list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.instaforms.AbstractInstaForms#getSectionDetails(int, java.lang.String,
   * java.lang.String, int, java.lang.String, java.lang.String, java.lang.String, int, int,
   * org.apache.commons.beanutils.BasicDynaBean)
   */
  @Override
  public List getSectionDetails(int formId, String formType, String itemType, int sectionId,
      String linkedTo, String mrNo, String patientId, int itemId, int genericFormId,
      BasicDynaBean record) throws SQLException, IOException {
    List list = null;
    PatientSectionDetailsDAO sdDAO = new PatientSectionDetailsDAO();
    Connection con = DataBaseUtil.getConnection();
    try {
      if (linkedTo.equals("patient")) {
        if (record == null) {
          list = sdDAO.getPatientLevelSectionValues(mrNo, sectionId);
        } else {
          list = sdDAO.getSectionDetails(mrNo, patientId, itemId, genericFormId, formId, sectionId,
              itemType);
        }
      } else if (linkedTo.equals("visit")) {
        if (record == null) {
          list = sdDAO.getVisitLevelSectionValues(patientId, sectionId);
        } else {
          list = sdDAO.getSectionDetails(mrNo, patientId, itemId, genericFormId, formId, sectionId,
              itemType);
        }
      } else if (linkedTo.equals("order item")) {
        if (record == null) {
          list = sdDAO.getItemLevelSectionValues(itemId, itemType, sectionId);
        } else {
          list = sdDAO.getSectionDetails(mrNo, patientId, itemId, genericFormId, formId, sectionId,
              itemType);
        }
      } else if (linkedTo.equals("form")) {
        list = sdDAO.getSectionDetails(mrNo, patientId, itemId, genericFormId, formId, sectionId,
            itemType);
      }

    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
    return list;
  }

}
