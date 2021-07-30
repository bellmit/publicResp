package com.insta.hms.dischargemedication;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.emr.EMRDoc;
import com.insta.hms.emr.EMRInterface;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class DischargeMedicationDAO.
 *
 * @author krishna
 */
public class DischargeMedicationDAO extends GenericDAO {

  /** Instantiates a new discharge medication DAO. */

  public DischargeMedicationDAO() {
    super("discharge_medication");
  }

  // Below methods used for prints
  private static final String GET_STORE_MEDICINE_PRESCRIPTIONS_PRINTS = "SELECT "
      + " pp.prescribed_date, sid.medicine_name as item_name, sid.medicine_id::text as item_id, "
      + " pmp.medicine_quantity, op_medicine_pres_id as item_prescribed_id, "
      + " frequency as medicine_dosage, pmp.frequency, pmp.strength, duration as medicine_days, "
      + " medicine_remarks as item_remarks, pp.status as issued, pmp.cons_uom_id,"
      + " cum.consumption_uom, "
      + " g.generic_name, g.generic_code, 'item_master' as master, 'Medicine' as item_type, "
      + " mod_time,  mr.route_id, mr.route_name, "
      + " '' AS category_name, '' AS manf_name, '' AS manf_mnemonic, "
      + " coalesce(pmp.item_form_id, 0) as item_form_id, "
      + " pmp.item_strength, ifm.item_form_name, 0 as charge, 0 as discount,  pp.doctor_id, "
      + " pmp.duration, pmp.duration_units, " + " pmp.item_strength_units, su.unit_name,"
      + " to_char(pmp.mod_time, 'YYYY-MM-DD HH24:MI:SS') as presc_time, "
      + " pmp.admin_strength, ifm.granular_units, pp.special_instr, "
      + " sic.item_code as drug_code" + " FROM patient_prescription pp " + " #JOIN_TABLE# "
      + "JOIN patient_medicine_prescriptions pmp ON (pp.patient_presc_id=pmp.op_medicine_pres_id"
      + " AND is_discharge_medication = true) "
      + " LEFT JOIN consumption_uom_master cum ON (cum.cons_uom_id=pmp.cons_uom_id)"
      + "LEFT JOIN store_item_details sid ON (pmp.medicine_id=sid.medicine_id) "
      + "JOIN organization_details od ON (od.org_id=?) "
      + "LEFT JOIN store_item_rates sir ON (sir.medicine_id=sid.medicine_id  "
      + "         AND sir.store_rate_plan_id=od.store_rate_plan_id) "
      + "LEFT JOIN store_category_master mc ON (mc.category_id = sid.med_category_id) "
      + "LEFT JOIN generic_name g ON (pmp.generic_code = g.generic_code) "
      + "LEFT JOIN strength_units su ON (pmp.item_strength_units=su.unit_id) "
      + "LEFT JOIN item_form_master ifm ON (pmp.item_form_id=ifm.item_form_id) "
      + "LEFT JOIN medicine_route mr ON (mr.route_id=pmp.route_of_admin) "
      + "LEFT JOIN ha_item_code_type hict ON "
      + "(hict.medicine_id = sid.medicine_id AND hict.health_authority= ? ) "
      + "LEFT JOIN store_item_codes sic ON "
      + "(sic.medicine_id = hict.medicine_id AND sic.code_type = hict.code_type) "
      + "WHERE #filter#=? ";

  private static final String GET_OTHER_MEDICINE_PRESCRIPTIONS_PRINT = "SELECT "
      + " pp.prescribed_date, pomp.medicine_name as item_name, '' as item_id, "
      + " pomp.medicine_quantity, prescription_id as item_prescribed_id, "
      + " pomp.frequency as medicine_dosage, pomp.frequency, pomp.strength, "
      + " pomp.duration as medicine_days, pomp.medicine_remarks as item_remarks, "
      + " 'P' as issued, pomp.cons_uom_id, cum.consumption_uom, g.generic_name, "
      + " g.generic_code, 'op' as master, 'Medicine' as item_type,"
      + " pomp.mod_time, mr.route_id, mr.route_name, '' AS category_name, "
      + " '' as manf_name, '' as manf_mnemonic, "
      + " coalesce(pomp.item_form_id, 0) as item_form_id, "
      + " pomp.item_strength, ifm.item_form_name, 0 as charge, 0 as discount, '' as doctor_id,"
      + " pomp.duration, pomp.duration_units, pomp.item_strength_units, su.unit_name, "
      + " to_char(pmp.mod_time, 'YYYY-MM-DD HH24:MI:SS') as presc_time, pomp.admin_strength,"
      + " ifm.granular_units, pp.special_instr, " + " '' as drug_code, pp.doctor_id"
      + " FROM patient_prescription pp"
      + " #JOIN_TABLE# "
      + " JOIN patient_other_medicine_prescriptions pomp"
      + " ON (pp.patient_presc_id=pomp.prescription_id"
      + " AND is_discharge_medication = true)"
      + " LEFT JOIN item_form_master ifm ON (pomp.item_form_id = ifm.item_form_id)"
      + "   JOIN prescribed_medicines_master pms ON (pomp.medicine_name=pms.medicine_name)"
      + "   LEFT JOIN strength_units su ON (su.unit_id=pms.item_strength_units) "
      + "   LEFT JOIN generic_name g ON (pms.generic_name=g.generic_code)"
      + " LEFT JOIN medicine_route mr ON (mr.route_id=pomp.route_of_admin) "
      + "   LEFT JOIN patient_medicine_prescriptions pmp"
      + " ON (pp.patient_presc_id=pmp.op_medicine_pres_id) "
      + "   LEFT JOIN store_item_details sid ON (sid.medicine_id=pmp.medicine_id) "
      + "   LEFT JOIN store_item_controltype sic ON (sic.control_type_id = sid.control_type_id)"
      + " LEFT JOIN consumption_uom_master cum ON (cum.cons_uom_id = pomp.cons_uom_id)"
      + " WHERE #filter#=?";

  private static final String GET_PRESCRIPTIONS_PRINTS = "UNION ALL "
      + "SELECT " // Non-Hospital
      + " pp.prescribed_date, item_name, '' as item_id, medicine_quantity,"
      + " prescription_id as item_prescribed_id, "
      + " frequency as medicine_dosage, frequency, strength, duration as medicine_days, "
      + " item_remarks, 'P' as issued, pop.cons_uom_id, cum.consumption_uom, '', '',"
      + " 'op' as master, "
      + " case when non_hosp_medicine then 'Medicine' else 'NonHospital' end as item_type, "
      + " mod_time, -1 as route_id, '' as route_name, "
      + " '' AS category_name,'' as manf_name, '' as manf_mnemonic , pop.item_form_id, "
      + " pop.item_strength, ifm.item_form_name, 0 as charge, 0 as discount, "
      + " pp.doctor_id, duration, duration_units, item_strength_units, su.unit_name, "
      + " to_char(pop.mod_time, 'YYYY-MM-DD HH24:MI:SS') as presc_time, pop.admin_strength, "
      + " ifm.granular_units, pp.special_instr, " + " '' as drug_code "
      + "FROM patient_prescription pp " + " #JOIN_TABLE# "
      + "JOIN patient_other_prescriptions pop ON (pp.patient_presc_id=pop.prescription_id"
      + " AND is_discharge_medication = true) "
      + "LEFT JOIN strength_units su ON (su.unit_id=pop.item_strength_units) "
      + "LEFT JOIN item_form_master ifm ON (pop.item_form_id=ifm.item_form_id) "
      + "LEFT JOIN consumption_uom_master cum ON (pop.cons_uom_id = cum.cons_uom_id) "
      + "WHERE #filter#=? ";

  /** The Constant ALL_DISCHARGE_MEDICATION_PHARMA_MEDICINES. */
  private static final String ALL_DISCHARGE_MEDICATION_PHARMA_MEDICINES =
      " SELECT dm.medication_id, sid.medicine_name as item_name, "
          + " sid.medicine_id::text as item_id, medicine_quantity, "
          + " medicine_presc_id as item_prescribed_id, frequency as medicine_dosage, "
          + " frequency, strength, duration as medicine_days, dmd.cons_uom_id,"
          + " medicine_remarks as item_remarks, dmd.cons_uom_id, cum.consumption_uom, "
          + " g.generic_name, g.generic_code, 'item_master' as master, "
          + " dmd.mod_time, mr.route_id, mr.route_name,dmd.issued, "
          + " icm.category AS category_name,mm.manf_name, mm.manf_mnemonic, "
          + " coalesce(dmd.item_form_id, 0) as item_form_id, dmd.item_strength, "
          + " if.item_form_name, 0 as charge, 0 as discount,doc.doctor_name,"
          + " dm.doctor_id, duration, duration_units, dmd.item_strength_units, "
          + " su.unit_name, to_char(dmd.mod_time, 'YYYY-MM-DD HH24:MI:SS') as presc_time, "
          + " dmd.admin_strength, if.granular_units, dmd.special_instr, "
          + " sic.item_code as drug_code " + " FROM discharge_medication dm "
          + " JOIN discharge_medication_details dmd ON (dm.medication_id=dmd.medication_id) "
          + " LEFT JOIN doctors doc ON (doc.doctor_id=dm.doctor_id) "
          + " LEFT JOIN store_item_details sid ON (dmd.medicine_id=sid.medicine_id) "
          + " LEFT JOIN item_form_master if ON (dmd.item_form_id=if.item_form_id) "
          + " LEFT OUTER JOIN generic_name g ON (dmd.generic_code = g.generic_code) "
          + " LEFT OUTER JOIN medicine_route mr ON (mr.route_id=dmd.route_of_admin)"
          + " LEFT OUTER JOIN manf_master mm on mm.manf_code=sid.manf_name "
          + " LEFT OUTER JOIN store_category_master icm on sid.med_category_id=icm.category_id "
          + " LEFT OUTER JOIN strength_units su ON (dmd.item_strength_units=su.unit_id) "
          + " LEFT JOIN ha_item_code_type hict ON (hict.medicine_id = sid.medicine_id "
          + " AND hict.health_authority= ? ) "
          + " LEFT JOIN store_item_codes sic ON (sic.medicine_id = hict.medicine_id "
          + " AND sic.code_type = hict.code_type) "
          + " LEFT JOIN consumption_uom_master cum ON (dmd.cons_uom_id = cum.cons_uom_id)"
          + " WHERE dm.visit_id=? ";

  /**
   * Gets the discharge medication details.
   *
   * @param visitId the visit id
   * @param centerId the center id
   * @return the discharge medication details
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getDischargeMedicationDetails(String visitId, Integer centerId)
      throws SQLException {
    // old one not used for print
    Connection con = null;
    PreparedStatement ps = null;
    try {
      String helathAuthority = CenterMasterDAO.getHealthAuthorityForCenter(centerId);
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(ALL_DISCHARGE_MEDICATION_PHARMA_MEDICINES);
      ps.setString(1, helathAuthority);
      ps.setString(2, visitId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the discharge medication details.
   * 
   * @param visitId the visit id
   * @param healthAuthority the health authority
   * @param presFromStores indicator for store prescription
   * @param visitType indicator for visit type
   * @param orgId the org id
   * @return the discharge medication details
   */
  public static List<BasicDynaBean> getDischargeMedicationDetails(String visitId,
      String healthAuthority, Boolean presFromStores, String visitType, String orgId) {
    // Used For Print IP or OP discharge Medication
    if (presFromStores) {
      String query = (GET_STORE_MEDICINE_PRESCRIPTIONS_PRINTS + GET_PRESCRIPTIONS_PRINTS)
          .replace("#JOIN_TABLE#", visitType.equals("i")
              ? ""
                  : "JOIN doctor_consultation dc on (pp.consultation_id = dc.consultation_id)")
          .replace("#filter#", visitType.equals("i") ? "pp.visit_id" : "dc.patient_id");
      return DatabaseHelper.queryToDynaList(query,
          new Object[] {orgId, healthAuthority, visitId, visitId});
    } else {
      String query = (GET_OTHER_MEDICINE_PRESCRIPTIONS_PRINT + GET_PRESCRIPTIONS_PRINTS)
          .replace("#JOIN_TABLE#", visitType.equals("i")
              ? ""
                  : "JOIN doctor_consultation dc on (pp.consultation_id = dc.consultation_id)")
          .replace("#filter#", visitType.equals("i") ? "pp.visit_id" : "dc.patient_id");
      return DatabaseHelper.queryToDynaList(query, new Object[] {visitId, visitId});
    }

  }

  /**
   * This method is used to get all discharge medication and doctor name for visit id.
   *
   * @param visitId the visit id
   * @return the discharge medication details
   * @throws SQLException the SQL exception
   */
  public static List getDischargeMedicationDetails(String visitId) throws SQLException {
    // used in medicine sales action
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(DISCHARGE_MEDICATION_QUERY);
      ps.setString(1, visitId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_DISCHARGE_MEDICATION_BEAN. */
  private static final String GET_DISCHARGE_MEDICATION_BEAN = // TODO:remove
      " SELECT dm.*,doc.doctor_name,doc.doctor_license_number,doc.doctor_id "
          + " FROM discharge_medication dm "
          + " JOIN doctors doc ON(dm.doctor_id=doc.doctor_id) "
          + " LEFT JOIN discharge_medication_details dmd ON (dm.medication_id=dmd.medication_id) "
          + " LEFT JOIN patient_registration pr ON (pr.patient_id = dm.visit_id)"
          + " WHERE visit_id = ?";

  private static final String GET_ADMITTING_DOCTORS_DETAILS =
      " SELECT doc.doctor_name,doc.doctor_license_number,doc.doctor_id "
          + " FROM patient_registration pr "
          + " LEFT JOIN doctors doc ON(pr.doctor=doc.doctor_id) "
          + " WHERE patient_id = ?";

  /**
   * Gets the discharge medication bean.
   *
   * @param visitId the visit id
   * @return the discharge medication bean
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getDischargeMedicationBean(String visitId) throws SQLException {
    // used in old
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_DISCHARGE_MEDICATION_BEAN);
      ps.setString(1, visitId);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets discharge medication bean.
   * 
   * @param visitId the visit id
   * @return the discharge medication bean
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getDischargeMedicationByUser(String visitId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_ADMITTING_DOCTORS_DETAILS);
      ps.setString(1, visitId);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  // Filter on admitting doctor
  private static final String MEDICATION_SECTION_STORE_MEDICINE_DOCS_FOR_OP =
      " (SELECT pr.patient_id as visit_id, d.doctor_name, pr.reg_date,"
          + " pr.patient_id, pp.username as user_name "
          + " FROM patient_prescription pp "
          + " JOIN doctor_consultation dc on (dc.consultation_id = pp.consultation_id)"
          + " JOIN patient_registration pr ON (pr.patient_id = dc.patient_id)"
          + " JOIN patient_medicine_prescriptions pmp on"
          + " (pmp.op_medicine_pres_id = pp.patient_presc_id"
          + " AND is_discharge_medication = true) "
          + " LEFT JOIN doctors d ON (pr.doctor = d.doctor_id)  where #Filter#) ";

  // Filter on admitting doctor
  private static final String MEDICATION_SECTION_OTHER_MEDICINE_DOCS_FOR_OP =
      " (SELECT pr.patient_id as visit_id, d.doctor_name, pr.reg_date,"
          + " pr.patient_id, pp.username as user_name "
          + " FROM patient_prescription pp "
          + " JOIN doctor_consultation dc on (dc.consultation_id = pp.consultation_id)"
          + " JOIN patient_registration pr ON (pr.patient_id = dc.patient_id)"
          + " JOIN patient_other_medicine_prescriptions pomp on"
          + " (pomp.prescription_id = pp.patient_presc_id AND is_discharge_medication = true) "
          + " LEFT JOIN doctors d ON (pr.doctor = d.doctor_id) where #Filter#) ";

  // Filter on admitting doctor
  private static final String MEDICATION_SECTION_NON_HOSP_MEDICINE_DOCS_FOR_OP =
      " UNION "
      + " SELECT pr.patient_id as visit_id, d.doctor_name,"
      + " pr.reg_date, pr.patient_id, pp.username as user_name " + " FROM patient_prescription pp "
      + " JOIN doctor_consultation dc on (dc.consultation_id = pp.consultation_id)"
      + " JOIN patient_registration pr ON (pr.patient_id = dc.patient_id)"
      + " JOIN patient_other_prescriptions pop on (pop.prescription_id = pp.patient_presc_id"
      + " AND is_discharge_medication = true) "
      + " LEFT JOIN doctors d ON (pr.doctor = d.doctor_id) where #Filter# ";

  /**
   * Gets the discharge medication EMR for OP.
   *
   * @param patientId the patient id
   * @param mrNo the mr no
   * @return the discharge medication EMR for OP
   * @throws Exception the exception
   */
  public List getDischargeMedicationEMRForOP(String patientId, String mrNo) throws Exception {
    BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
    Boolean useStoreItems = genericPrefs.get("prescription_uses_stores").equals("Y");
    List<EMRDoc> emrDischargeMedicationRecords = new ArrayList<EMRDoc>();
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    int index = 1;
    try {
      String query = "";
      if (useStoreItems) {
        query = (MEDICATION_SECTION_STORE_MEDICINE_DOCS_FOR_OP
            + MEDICATION_SECTION_NON_HOSP_MEDICINE_DOCS_FOR_OP).replace(
                "#Filter#",
                patientId != null ? " pr.patient_id = ? LIMIT 1 "
                    : " pr.mr_no = ? AND pr.visit_type='o' "
                        + " GROUP BY pr.patient_id, d.doctor_name, pr.reg_date, pp.username ");
      } else {
        query = (MEDICATION_SECTION_OTHER_MEDICINE_DOCS_FOR_OP
            + MEDICATION_SECTION_NON_HOSP_MEDICINE_DOCS_FOR_OP).replace(
                "#Filter#",
                patientId != null ? "pr.patient_id = ? LIMIT 1 "
                    : "pr.mr_no = ? AND pr.visit_type='o' "
                        + " GROUP BY pr.patient_id, d.doctor_name, pr.reg_date, pp.username ");
      }
      ps = con.prepareStatement(query);
      ps.setString(index++, patientId != null ? patientId : mrNo);
      ps.setString(index++, patientId != null ? patientId : mrNo);
      try (ResultSet rs = ps.executeQuery()) {
        Map recordsMap = new HashMap();
        BasicDynaBean printpref =
            PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT);
        while (rs.next()) {
          if (!recordsMap.containsValue(rs.getString("visit_id"))) {
            EMRDoc dtoObj = new EMRDoc();
            populateEMRDocForOP(dtoObj, rs, printpref);
            emrDischargeMedicationRecords.add(dtoObj);
            recordsMap.put("patient_id", rs.getString("visit_id"));
          }
        }
      }

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return emrDischargeMedicationRecords;
  }

  /**
   * Populate EMR doc for OP.
   *
   * @param emrDoc the emr doc
   * @param rs the rs
   * @param printpref the printpref
   * @throws Exception the exception
   */
  private void populateEMRDocForOP(EMRDoc emrDoc, ResultSet rs, BasicDynaBean printpref)
      throws Exception {
    int printerId = (Integer) printpref.get("printer_id");
    String doctor = rs.getString("doctor_name");
    emrDoc.setPrinterId(printerId);
    String patientId = rs.getString("visit_id");
    emrDoc.setDate(rs.getDate("reg_date"));
    emrDoc.setType("SYS_CONSULT");
    emrDoc.setUpdatedBy(rs.getString("user_name"));
    emrDoc.setPdfSupported(true);
    emrDoc.setAuthorized(true);
    emrDoc.setDoctor(doctor);
    emrDoc.setTitle("Discharge Medication Record");
    emrDoc.setDisplayUrl("/pages/dischargeMedicationPrint.do?_method=dischargeMedicationPrint"
        + "&patient_id=" + patientId + "&printerId=" + printerId);
    emrDoc.setProvider(EMRInterface.Provider.CaseSheetsProvider);
    emrDoc.setVisitid(patientId);
  }

  // Filter on op admitting doctor
  private static final String MEDICATION_SECTION_STORE_MEDICINE_DOCS_FOR_IP =
      "  (SELECT pp.visit_id, d.doctor_name, pr.reg_date, pr.patient_id, pp.username as user_name "
          + " FROM patient_prescription pp "
          + " JOIN patient_medicine_prescriptions pmp"
          + " on (pmp.op_medicine_pres_id = pp.patient_presc_id"
          + " AND is_discharge_medication = true) "
          + " JOIN patient_registration pr ON (pr.patient_id = pp.visit_id) "
          + " JOIN doctors d ON (pr.doctor = d.doctor_id)  where #Filter#) ";

  // Filter on ip admitting doctor
  private static final String MEDICATION_SECTION_OTHER_MEDICINE_DOCS_FOR_IP =
      " (SELECT pp.visit_id, d.doctor_name, pr.reg_date,"
          + " pr.patient_id, pp.username as user_name " + " FROM patient_prescription pp "
          + " JOIN patient_other_medicine_prescriptions pomp"
          + " on (pomp.prescription_id = pp.patient_presc_id"
          + " AND is_discharge_medication = true) "
          + " JOIN patient_registration pr ON (pr.patient_id = pp.visit_id) "
          + " JOIN doctors d ON (pr.doctor = d.doctor_id) where #Filter#) ";

  // Filter on admitting doctor
  private static final String MEDICATION_SECTION_NON_HOSP_MEDICINE_DOCS_FOR_IP = " UNION "
      + " SELECT pp.visit_id, d.doctor_name, pr.reg_date, pr.patient_id, pp.username as user_name"
      + " FROM patient_prescription pp "
      + " JOIN patient_other_prescriptions pop on (pop.prescription_id = pp.patient_presc_id"
      + " AND is_discharge_medication = true) "
      + " JOIN patient_registration pr ON (pr.patient_id = pp.visit_id) "
      + " JOIN doctors d ON (pr.doctor = d.doctor_id) where #Filter# ";

  /**
   * Gets the discharge medication EMR for IP.
   *
   * @param patientId the patient id
   * @param mrNo the mr no
   * @return the discharge medication EMR for IP
   * @throws Exception the exception
   */
  public List getDischargeMedicationEMRForIP(String patientId, String mrNo) throws Exception {
    BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
    Boolean useStoreItems = genericPrefs.get("prescription_uses_stores").equals("Y");
    List<EMRDoc> emrDischargeMedicationRecords = new ArrayList<EMRDoc>();
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    int index = 1;
    try {
      String query = "";
      if (useStoreItems) {
        query = (MEDICATION_SECTION_STORE_MEDICINE_DOCS_FOR_IP
            + MEDICATION_SECTION_NON_HOSP_MEDICINE_DOCS_FOR_IP).replace(
                "#Filter#",
                patientId != null ? "pr.patient_id = ? LIMIT 1 "
                    : "pr.mr_no = ? AND pr.visit_type='i' "
                        + " GROUP BY pp.visit_id, d.doctor_name, pr.reg_date,"
                        + " pr.patient_id, pp.username ");
      } else {
        query = (MEDICATION_SECTION_OTHER_MEDICINE_DOCS_FOR_IP
            + MEDICATION_SECTION_NON_HOSP_MEDICINE_DOCS_FOR_IP).replace(
                "#Filter#",
                patientId != null ? " pr.patient_id = ? LIMIT 1 "
                    : " pr.mr_no = ? AND pr.visit_type='i' "
                        + " GROUP BY pp.visit_id, d.doctor_name, pr.reg_date,"
                        + " pr.patient_id, pp.username ");
      }
      ps = con.prepareStatement(query);
      ps.setString(index++, patientId != null ? patientId : mrNo);
      ps.setString(index++, patientId != null ? patientId : mrNo);
      try (ResultSet rs = ps.executeQuery()) {
        Map recordsMap = new HashMap();
        BasicDynaBean printpref =
            PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT);
        while (rs.next()) {
          if (!recordsMap.containsValue(rs.getString("patient_id"))) {
            EMRDoc dtoObj = new EMRDoc();
            populateEMRDocForIP(dtoObj, rs, printpref);
            emrDischargeMedicationRecords.add(dtoObj);
            recordsMap.put("patient_id", rs.getString("patient_id"));
          }
        }
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return emrDischargeMedicationRecords;
  }

  /**
   * Populate EMR doc for IP.
   *
   * @param emrDoc the emr doc
   * @param rs the rs
   * @param printpref the printpref
   * @throws Exception the exception
   */
  private void populateEMRDocForIP(EMRDoc emrDoc, ResultSet rs, BasicDynaBean printpref)
      throws Exception {
    String doctor = rs.getString("doctor_name");
    int printerId = (Integer) printpref.get("printer_id");
    emrDoc.setPrinterId(printerId);
    String patientId = rs.getString("visit_id");
    emrDoc.setDate(rs.getDate("reg_date"));
    emrDoc.setType("SYS_IP");
    emrDoc.setUpdatedBy(rs.getString("user_name"));
    emrDoc.setPdfSupported(true);
    emrDoc.setAuthorized(true);
    emrDoc.setDoctor(doctor);
    emrDoc.setTitle("Discharge Medication Record");
    emrDoc.setDisplayUrl("/pages/dischargeMedicationPrint.do?_method=dischargeMedicationPrint"
        + "&patient_id=" + patientId + "&printerId=" + printerId);
    emrDoc.setProvider(EMRInterface.Provider.VisitSummaryRecordsProvider);
    emrDoc.setVisitid(patientId);
  }

  /** The Constant GET_ALLERGY_DETAILS. */
  public static final String GET_ALLERGY_DETAILS =
      " SELECT * FROM patient_allergies pr "
          + " LEFT JOIN patient_section_details psd ON "
          + " (psd.section_detail_id = pr.section_detail_id) "
          + " WHERE  psd.patient_id =? and pr.status ='A'; ";

  /**
   * Gets the allergy details.
   *
   * @param patientId the patient id
   * @return the allergy details
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getallergyDetails(String patientId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_ALLERGY_DETAILS);
      ps.setString(1, patientId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_DIAGNOSIS_DETAILS. */
  public static final String GET_DIAGNOSIS_DETAILS =
      " SELECT * FROM mrd_diagnosis WHERE visit_id=?";

  /**
   * Gets the diagnosis details.
   *
   * @param patientId the patient id
   * @return the diagnosis details
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getdiagnosisDetails(String patientId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_DIAGNOSIS_DETAILS);
      ps.setString(1, patientId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_WEIGHT_VITALS. */
  public static final String GET_WEIGHT_VITALS =
      " SELECT vr.param_value as weight FROM vital_reading vr "
          + " LEFT JOIN vital_parameter_master vpm ON vpm.param_id = vr.param_id "
          + " LEFT JOIN visit_vitals vv ON vv.vital_reading_id = vr.vital_reading_id "
          + " WHERE vv.patient_id=? and vpm.param_label='Weight' GROUP BY vr.param_value "
          + " ORDER BY max(vv.date_time) DESC LIMIT 1; ";

  /**
   * Gets the vitals weight.
   *
   * @param patientId the patient id
   * @return the vitals weight
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getVitalsWeight(String patientId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_WEIGHT_VITALS);
      ps.setString(1, patientId);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_ALL_VISIT_PRESCRIBED_MEDICINES. */
  public static final String GET_ALL_VISIT_PRESCRIBED_MEDICINES =
      "SELECT * FROM ip_prescription where presc_type =  'M'  AND patient_id =  ?";

  /**
   * Gets the all visit prescribed medicines.
   *
   * @param patientId the patient id
   * @return the all visit prescribed medicines
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getAllVisitPrescribedMedicines(
      String patientId) // NOt Used
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_ALL_VISIT_PRESCRIBED_MEDICINES);
      ps.setString(1, patientId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The all medicine count. */
  private static final String ALL_MEDICINE_COUNT = " SELECT count(*) ";

  /** The pharma medicine presc for op fields. */
  private static final String PHARMA_MEDICINE_PRESC_FOR_OP_FIELDS =
      " SELECT coalesce(sid.medicine_name,g.generic_name) as item_name,"
          + " sid.medicine_id as item_id, "
          + " medicine_quantity,frequency as medicine_dosage,"
          + " frequency,pmp.strength,duration,pmp.medicine_remarks "
          + " as remarks,pmp.cons_uom_id,cum.consumption_uom,g.generic_name,g.generic_code, "
          + " 'item_master' as master, mr.route_id, mr.route_name, "
          + " pmp.item_form_id, pmp.item_strength, "
          + " if.item_form_name, 0 as charge, 0 as discount, "
          + " duration_units, pmp.item_strength_units, su.unit_name, "
          + " pmp.admin_strength, if.granular_units, "
          + " sic.item_code as drug_code, pp.special_instr ";

  /** The pharma medicine presc for op tables. */
  private static String PHARMA_MEDICINE_PRESC_FOR_OP_TABLES = "   FROM doctor_consultation dc "
      + " JOIN  patient_prescription pp ON (dc.consultation_id=pp.consultation_id "
      + " AND pp.presc_type = 'Medicine') "
      + " JOIN patient_medicine_prescriptions pmp ON(pp.patient_presc_id=pmp.op_medicine_pres_id)"
      + " LEFT JOIN store_item_details sid ON(pmp.medicine_id = sid.medicine_id)"
      + " LEFT JOIN item_form_master if ON (pmp.item_form_id=if.item_form_id) "
      + " LEFT JOIN generic_name g ON (pmp.generic_code = g.generic_code) "
      + " LEFT JOIN medicine_route mr ON (mr.route_id=pmp.route_of_admin) "
      + " LEFT JOIN strength_units su ON (su.unit_id=pmp.item_strength_units) "
      + " LEFT JOIN ha_item_code_type hict ON (hict.medicine_id = sid.medicine_id AND # ) "
      + " LEFT JOIN store_item_codes sic ON (sic.medicine_id = hict.medicine_id "
      + " AND sic.code_type = hict.code_type) "
      + " LEFT JOIN consumption_uom_master cum ON (cum.cons_uom_id = pmp.cons_uom_id)";

  /** The pharma medicine presc for ip fields. */
  private static final String PHARMA_MEDICINE_PRESC_FOR_IP_FIELDS =
      " SELECT sid.medicine_name as item_name, pmp.medicine_id::text as item_id, "
          + " '' as medicine_quantity, '' as medicine_dosage, '' as frequency, "
          + " '' as strength, '' as duration, pmp.medicine_remarks as remarks, "
          + " sid.cons_uom_id, cum.consumption_uom, g.generic_name, g.generic_code, "
          + " 'item_master' as master, mr.route_id, mr.route_name, "
          + " coalesce(pmp.item_form_id, 0) as item_form_id, "
          + " pmp.item_strength as item_strength, if.item_form_name, "
          + " 0 as charge, 0 as discount, '' as duration_units, "
          + " pmp.item_strength_units as item_strength_units, su.unit_name, "
          + " pmp.admin_strength, if.granular_units, '' as special_instr, "
          + " sic.item_code as drug_code";

  /** The pharma medicine presc for ip tables. */
  private static String PHARMA_MEDICINE_PRESC_FOR_IP_TABLES = " FROM patient_prescription pp "
      + " JOIN patient_medicine_prescriptions pmp on (pp.patient_presc_id=pmp.op_medicine_pres_id) "
      + " JOIN store_item_details sid ON(pmp.medicine_id=sid.medicine_id)"
      + " LEFT OUTER JOIN medicine_route mr ON (mr.route_id=pmp.route_of_admin) "
      + " LEFT JOIN item_form_master if ON (pmp.item_form_id=if.item_form_id) "
      + " LEFT JOIN generic_name g ON (pmp.generic_code = g.generic_code) "
      + " LEFT JOIN strength_units su ON (su.unit_id=pmp.item_strength_units) "
      + " LEFT JOIN ha_item_code_type hict ON (hict.medicine_id = sid.medicine_id AND #) "
      + " LEFT JOIN store_item_codes sic ON (sic.medicine_id = hict.medicine_id "
      + " AND sic.code_type = hict.code_type) "
      + " LEFT JOIN consumption_uom_master cum ON (cum.cons_uom_id = sid.cons_uom_id)";

  /**
   * Gets the all visit prescribed medicines.
   *
   * @param patientId the patient id
   * @param pageNum the page num
   * @param healthAuthority the health authority
   * @param visitType the visit type
   * @return the all visit prescribed medicines
   * @throws SQLException the SQL exception
   */
  public PagedList getAllVisitPrescribedMedicines(String patientId, Integer pageNum,
      String healthAuthority, String visitType) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    SearchQueryBuilder qb = null;
    try {
      if (visitType.equals("o")) {
        PHARMA_MEDICINE_PRESC_FOR_OP_TABLES = PHARMA_MEDICINE_PRESC_FOR_OP_TABLES.replace("#",
            "hict.health_authority= '" + healthAuthority + "'");
        qb = new SearchQueryBuilder(con, PHARMA_MEDICINE_PRESC_FOR_OP_FIELDS, ALL_MEDICINE_COUNT,
            PHARMA_MEDICINE_PRESC_FOR_OP_TABLES, null, "prescribed_date", false, 10, pageNum);
        qb.appendToQuery("  patient_id ='" + patientId + "'");
      } else {
        PHARMA_MEDICINE_PRESC_FOR_IP_TABLES = PHARMA_MEDICINE_PRESC_FOR_IP_TABLES.replace("#",
            "hict.health_authority= '" + healthAuthority + "'");
        qb = new SearchQueryBuilder(con, PHARMA_MEDICINE_PRESC_FOR_IP_FIELDS, ALL_MEDICINE_COUNT,
            PHARMA_MEDICINE_PRESC_FOR_IP_TABLES, null, "prescribed_date", false, 10, pageNum);
        qb.appendToQuery(" pp.presc_type='Medicine' AND  pp.visit_id ='" + patientId + "'");
      }

      qb.build();
      return qb.getMappedPagedList();
    } finally {
      if (qb != null) {
        qb.close();
      }
      con.close();
    }
  }

  /** The Constant PHARMA_DISCHARGE_MEDICINE_PRESCRIPTION. */
  private static final String PHARMA_DISCHARGE_MEDICINE_PRESCRIPTION =
      " SELECT sum(dmd.issued_qty) as issued_qty, "
          + " sum(dmd.medicine_quantity)::numeric as medicine_quantity,"
          + " dmd.medicine_id, null AS consultation_id, 0 as pbm_presc_id,"
          + " min(dmd.frequency) as frequency, min(dmd.medicine_remarks) "
          + " as medicine_remarks, min(dmd.strength) as strength, "
          + " min(dmd.duration) as duration,min(dmd.route_of_admin) as route_of_admin, "
          + " min(dmd.duration_units) as duration_units, "
          + " sid.medicine_name as pres_medicine_name, min(dmd.special_instr) AS special_instr "
          + " FROM discharge_medication dm "
          + " JOIN discharge_medication_details dmd ON (dm.medication_id=dmd.medication_id) "
          + " JOIN store_item_details sid ON (sid.medicine_id=dmd.medicine_id) "
          + " WHERE dm.visit_id=? AND dmd.status IN ('P', 'PA') AND dmd.issued NOT IN('Y','C') "
          + " GROUP BY dmd.medicine_id,sid.medicine_name";

  /**
   * Gets the discharge medicine prescription.
   *
   * @param patientId the patient id
   * @return the discharge medicine prescription
   * @throws SQLException the SQL exception
   */
  public static List getDischargeMedicinePrescription(String patientId) throws SQLException {
    // not used
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(PHARMA_DISCHARGE_MEDICINE_PRESCRIPTION);
      ps.setString(1, patientId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant UPDATE_DISCHARGE_MEDICINE_PRESCRIPTION. */
  private static final String UPDATE_DISCHARGE_MEDICINE_PRESCRIPTION =
      "UPDATE discharge_medication_details SET issued=?,"
          + " mod_time=LOCALTIMESTAMP(0), issued_qty=issued_qty+? ";

  /** The Constant UPDATE_DISCHARGE_MEDICINE_STATUS_ALL. */
  private static final String UPDATE_DISCHARGE_MEDICINE_STATUS_ALL =
      " UPDATE discharge_medication_details dmd SET issued='Y' FROM discharge_medication dm"
          + " WHERE dmd.medication_id=dm.medication_id AND dm.visit_id=?";

  /**
   * Update medication status.
   *
   * @param con the con
   * @param medAndQuantityMap the med and quantity map
   * @param visitid the visitid
   * @param medDispOpt the med disp opt
   * @param saleId the sale id
   * @param prescriptionsByGenerics the prescriptions by generics
   * @param pbmPrescId the pbm presc id
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public static boolean updateMedicationStatus(// Not in used
      Connection con, Map<String, BigDecimal> medAndQuantityMap, String visitid,
      String[] medDispOpt, String saleId, boolean prescriptionsByGenerics, int pbmPrescId)
      throws SQLException {
    boolean update = true;
    String keyCol = prescriptionsByGenerics ? "generic_code" : "medicine_id";

    for (Map.Entry<String, BigDecimal> entry : medAndQuantityMap.entrySet()) {
      BigDecimal qty = entry.getValue();
      String key = entry.getKey();
      String id = "";
      try (PreparedStatement ps = con.prepareStatement(
          " SELECT medicine_presc_id, medicine_id, issued_qty, "
              + " coalesce(medicine_quantity, 1) as medicine_quantity, "
              + " visit_id, initial_sale_id,medication_id "
              + " FROM discharge_medication dm "
              + " JOIN discharge_medication_details dmd USING(medication_id) "
              + " WHERE visit_id = ? and " + keyCol
              + " = ? and issued not in ('Y', 'C') order by medication_id")) {
        ps.setString(1, visitid);
        if (prescriptionsByGenerics) {
          ps.setString(2, key);
        } else {
          ps.setInt(2, Integer.parseInt(key));
        }

        try (ResultSet rs = ps.executeQuery()) {
          while (rs.next()) {
            BigDecimal issuedQty = rs.getBigDecimal("issued_qty");
            BigDecimal prescQty = rs.getBigDecimal("medicine_quantity");
            BigDecimal pendingQty = prescQty.subtract(issuedQty);
            String status = "";
            BigDecimal dispensedQty = BigDecimal.ZERO;
            if (qty.compareTo(pendingQty) >= 0) {
              // if ordered qty greater than or equal to the pending quantity and is the
              // last record then dump all the qty to the last consultation row
              dispensedQty = (rs.isLast()) ? qty : pendingQty;
              qty = qty.subtract(pendingQty);
              status = "O";
            } else {
              dispensedQty = qty;
              String dispenseStatus = "";
              dispenseStatus = medDispOpt[0];
              if (dispenseStatus.equals("all")) {
                status = "O";
              } else if (dispenseStatus.equals("partiall")) {
                // 'O' is ordered, 'P' is in progress
                status = qty.compareTo(BigDecimal.ZERO) > 0 ? "O" : "P";
              } else if (dispenseStatus.equals("full")) {
                // 'PA' is Partially Ordered.
                status = qty.compareTo(BigDecimal.ZERO) == 0 ? "N" : "PA";
              }
            }
            Integer prescribedId;
            try (PreparedStatement ps1 = con.prepareStatement(
                "UPDATE discharge_medication_details SET status=? WHERE "
                    + " medicine_presc_id=?")) {
              ps1.setString(1, status);
              prescribedId = rs.getInt("medicine_presc_id");
              ps1.setInt(2, prescribedId);
              if (ps1.executeUpdate() == 0) {
                update = false;
                break;
              }
            }

            String query = UPDATE_DISCHARGE_MEDICINE_PRESCRIPTION;
            String initialSaleId = rs.getString("initial_sale_id");
            if (initialSaleId == null || initialSaleId.equals("")) {
              query = query + " ,initial_sale_id=?,final_sale_id=? ";
            } else {
              query = query + " ,final_sale_id=? ";
            }

            int index = 1;
            try (PreparedStatement ps1 =
                con.prepareStatement(query + " WHERE medicine_presc_id = ?")) {
              ps1.setString(index++, status);
              ps1.setBigDecimal(index++, dispensedQty);
              if (initialSaleId == null || initialSaleId.equals("")) {
                ps1.setString(index++, saleId);
                ps1.setString(index++, saleId);
              } else {
                ps1.setString(index++, saleId);
              }
              ps1.setInt(index++, prescribedId);

              if (ps1.executeUpdate() == 0) {
                update = false;
                break;
              }
            }
          }
        }
      }
    }

    String allPrescQuery = UPDATE_DISCHARGE_MEDICINE_STATUS_ALL;

    if (medDispOpt[0].equalsIgnoreCase("all")) {
      // close all prescriptions whether they were issued or not. This is required
      // the medicine may not appear in the list of sold items or unavailable items
      // if the user has explicitly deleted the item.
      try (PreparedStatement ps2 = con.prepareStatement(allPrescQuery)) {
        ps2.setString(1, visitid);
        ps2.executeUpdate();
      }
    }
    return update;
  }

  /** The Constant GET_PATIENT_DISCHARGE_MEDICATION_DOCTOR_INFO. */
  private static final String GET_PATIENT_DISCHARGE_MEDICATION_DOCTOR_INFO =
      " SELECT DISTINCT d.doctor_name,d.doctor_id,dm.visit_id AS patient_id "
          + " FROM discharge_medication dm "
          + " JOIN doctors d ON dm.doctor_id=d.doctor_id "
          + " JOIN discharge_medication_details dmd on (dmd.medication_id = dm.medication_id) "
          + " WHERE dm.visit_id=? and dmd.status IN ('P', 'PA')";

  /**
   * Gets the discharge medication doctor details.
   *
   * @param patientId the patient id
   * @return the discharge medication doctor details
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getDischargeMedicationDoctorDetails(String patientId)
      // not used
      throws SQLException {

    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_PATIENT_DISCHARGE_MEDICATION_DOCTOR_INFO);
      ps.setString(1, patientId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  private static final String DISCHARGE_PHARMA_MEDICINE_QUERY =
      " SELECT COALESCE(sum(dmsd.issued_qty),0) as issued_qty, "
          + " sum(dmd.medicine_quantity::integer) as medicine_quantity,"
          + " dmd.medicine_id, dm.medication_id, "
          + " min(dmd.frequency) as frequency, min(dmd.medicine_remarks) as medicine_remarks, "
          + " min(dmd.strength) as strength, "
          + " min(dmd.duration) as duration,min(dmd.route_of_admin) as route_of_admin ,"
          + " min(dmd.duration_units) as duration_units,"
          + " sid.medicine_name as pres_medicine_name, dmd.special_instr "
          + " FROM discharge_medication dm "
          + " JOIN discharge_medication_details dmd ON (dm.medication_id=dmd.medication_id) "
          + " LEFT JOIN discharge_medication_sale_details dmsd ON "
          + " (dmd.medicine_presc_id = dmsd.discharge_presc_id) "
          + " JOIN store_item_details sid ON (sid.medicine_id=dmd.medicine_id) "
          + " WHERE dm.medication_id=? and dmd.issued IN ('N', 'PA') "
          + " GROUP BY dmd.medicine_id, dm.medication_id, sid.medicine_name, dmd.special_instr";

  /**
   * To get all discharge medication medicine details based on medicationId.
   *
   * @param medicationId the medication id
   * @return the discharge pharma medicines
   * @throws SQLException the SQL exception
   */
  public static List getDischargePharmaMedicines(int medicationId) throws SQLException {
    // used in sales screen
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(DISCHARGE_PHARMA_MEDICINE_QUERY);
      ps.setInt(1, medicationId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  private static final String PHARMA_GENERIC_DISCHARGE =
      " SELECT dmd.*, g.generic_name, dm.medication_id "
          + " FROM discharge_medication dm "
          + " JOIN discharge_medication_details dmd ON (dm.medication_id=dmd.medication_id) "
          + " JOIN generic_name g ON (dmd.generic_code=g.generic_code) "
          + " WHERE dm.medication_id=? and dmd.issued IN ('N', 'PA')";

  /**
   * This method is used to get pharma generic discharge medication prescription.
   *
   * @param medicationId the medication id
   * @return the pharma generic prescriptions
   * @throws SQLException the SQL exception
   */
  public static List getPharmaGenericPrescriptions(int medicationId) throws SQLException {
    // used in medicine sales action
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(PHARMA_GENERIC_DISCHARGE);
      ps.setInt(1, medicationId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  private static final String DISCHARGE_MEDICATION_QUERY =
      "SELECT dm.medication_id, d.doctor_name,dm.doctor_id "
          + " FROM discharge_medication dm "
          + " JOIN discharge_medication_details dmd ON (dm.medication_id = dmd.medication_id) "
          + " JOIN doctors d ON dm.doctor_id=d.doctor_id "
          + " where visit_id = ? group by dm.medication_id,d.doctor_name,dm.doctor_id";

  /**
   * Update medicines status.
   *
   * @param con the con
   * @param medAndQuantityMap the med and quantity map
   * @param medicationIds the medication ids
   * @param medDispOpt the med disp opt
   * @param saleId the sale id
   * @param prescriptionsByGenerics the prescriptions by generics
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public static boolean updateMedicinesStatus(Connection con,
      Map<String, BigDecimal> medAndQuantityMap, String[] medicationIds, String[] medDispOpt,
      String saleId, boolean prescriptionsByGenerics, List<Integer> dischargeMedPrescIdList)
      throws SQLException {
    boolean success = true;
    String keyCol = prescriptionsByGenerics ? "generic_code" : "medicine_id";
    String medquery = " SELECT dm.visit_id, dmd.medicine_presc_id, dmd.medicine_id, "
        + " coalesce(dmd.medicine_quantity, 1) as medicine_quantity, "
        + " dm.medication_id, coalesce(sum(dmsd.issued_qty), 0) as issued_qty "
        + " FROM discharge_medication_details dmd "
        + " JOIN discharge_medication dm "
        + " ON (dm.medication_id = dmd.medication_id) "
        + " LEFT JOIN discharge_medication_sale_details dmsd "
        + " ON (dmd.medicine_presc_id = dmsd.discharge_presc_id) ";

    for (Map.Entry<String, BigDecimal> entry : medAndQuantityMap.entrySet()) {
      BigDecimal qty = entry.getValue();
      List<Integer> medicationIdList = new ArrayList<Integer>();
      for (int j = 0; j < medicationIds.length; j++) {
        if (!medicationIds[j].equals("")) {
          medicationIdList.add(Integer.parseInt(medicationIds[j]));
        }
      }

      StringBuilder whereCond = new StringBuilder();
      if (medicationIdList.size() > 0) {
        DataBaseUtil.addWhereFieldInList(whereCond, "dm.medication_id", medicationIdList, false);
        whereCond.append(" AND ");
      } else {
        whereCond.append(" WHERE ");
      }

      whereCond.append(DataBaseUtil.quoteIdent(keyCol)
          + " = ? AND issued in ('N', 'PA') group by medicine_presc_id,"
          + " dmd.medicine_quantity, medicine_id, dm.medication_id "
          + " order by dm.medication_id");

      try (PreparedStatement ps = con.prepareStatement(medquery + whereCond.toString())) {

        int psIndex = 1;
        for (Integer medicationId : medicationIdList) {
          ps.setInt(psIndex++, medicationId);
        }
        String key = entry.getKey();
        if (prescriptionsByGenerics) {
          ps.setString(psIndex++, key);
        } else {
          ps.setInt(psIndex++, Integer.parseInt(key));
        }
        try (ResultSet rs = ps.executeQuery()) {
          while (rs.next()) {
            BigDecimal prescQty = rs.getBigDecimal("medicine_quantity");
            Integer prescribedId = rs.getInt("medicine_presc_id");
            int medicationId = rs.getInt("medication_id");
            BigDecimal issuedQty = rs.getBigDecimal("issued_qty");
            BigDecimal pendingQty = prescQty.subtract(issuedQty);
            BigDecimal dispensedQty = BigDecimal.ZERO;
            String status = "";
            if (qty.compareTo(pendingQty) >= 0) {
              // if ordered qty greater than or equal to the pending quantity and is the lastrecord
              // then dump all the qty to the last consultation row
              dispensedQty = (rs.isLast()) ? qty : pendingQty;
              /* qty = qty.subtract(pendingQty); */
              status = "O";
            } else {
              dispensedQty = qty;
              String dispenseStatus = "";
              for (int k = 0; k < medicationIds.length; k++) {

                if (medicationIds[k].equals(medicationId + "")) {
                  dispenseStatus = medDispOpt[k];
                }
              }
              if (dispenseStatus.equals("all")) {
                status = "O";
              } else if (dispenseStatus.equals("partiall")) {
                // 'O' is ordered, 'P' is in progress
                status = qty.compareTo(BigDecimal.ZERO) > 0 ? "O" : "N";
              } else if (dispenseStatus.equals("full")) {
                // 'PA' is Partially Ordered.
                status = qty.compareTo(BigDecimal.ZERO) == 0 ? "N" : "PA";
              }
            }
            if (!status.isEmpty()) {
              try (PreparedStatement ps1 = con.prepareStatement(
                  "UPDATE discharge_medication_details SET issued=? WHERE medicine_presc_id=?")) {
                ps1.setString(1, status);
                ps1.setInt(2, prescribedId);
                if (ps1.executeUpdate() == 0) {
                  success = false;
                  break;
                }
                dischargeMedPrescIdList.add(prescribedId);
              }
              if (success) {
                try (PreparedStatement ps2 = con.prepareStatement(
                    "INSERT INTO discharge_medication_sale_details VALUES(?,?,?)")) {
                  ps2.setInt(1, prescribedId);
                  ps2.setString(2, saleId);
                  ps2.setBigDecimal(3, dispensedQty);
                  if (ps2.executeUpdate() == 0) {
                    success = false;
                    break;
                  }
                }
              }
            }
          }
        }
      }
    }
    return success;
  }

  /**
   * Close all.
   *
   * @param medicationIds the medication ids
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public static boolean closeAll(String[] medicationIds) throws SQLException {
    // used in pharmacy prescription sales action
    if (medicationIds == null || medicationIds.length == 0) {
      return true;
    }

    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);
    PreparedStatement ps = null;
    boolean success = true;
    try {
      ps = con.prepareStatement(
          " UPDATE discharge_medication_details SET issued='O' WHERE medication_id = ? ");
      int idCount = 0;
      int medicationIdCount = 0;
      for (String id : medicationIds) {
        String[] idSplit = id.split("\\@");
        String prescriptionType = idSplit[1];
        if (prescriptionType != null) {
          if (prescriptionType.equalsIgnoreCase("DM")) {
            medicationIdCount++;
            ps.setInt(1, Integer.parseInt(idSplit[0]));
            if (ps.executeUpdate() > 0) {
              idCount++;
            } else {
              break;
            }
          }
        }
      }
      success = (idCount == medicationIdCount);
    } finally {
      DataBaseUtil.commitClose(con, success);
      if (ps != null) {
        ps.close();
      }
    }
    return success;
  }
}
