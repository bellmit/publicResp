package com.insta.hms.wardactivities.prescription;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * The Class IPPrescriptionDAO.
 *
 * @author krishna
 */
public class IPPrescriptionDAO extends GenericDAO {

  /**
   * Instantiates a new IP prescription DAO.
   */
  public IPPrescriptionDAO() {
    super("ip_prescription");
  }

  /** The Constant GET_PRESCRIPTIONS. */
  public static final String GET_PRESCRIPTIONS = " SELECT pp.visit_id,"
      + " pp.patient_presc_id AS prescription_id, pp.doctor_id, doc.doctor_name, "
      + " CASE WHEN presc_type = 'NonBillable' THEN 'O' WHEN presc_type = 'Medicine' THEN 'M'"
      + "   WHEN presc_type = 'Inv.' THEN 'I' WHEN presc_type = 'Doctor' THEN 'C' "
      + "   WHEN presc_type = 'Service' THEN 'S' END  presc_type, pp.username as entered_by,"
      + " pp.prescribed_date AS entered_datetime, COALESCE(pmp.mod_time, pomp.mod_time,"
      + " ptp.mod_time, psp.mod_time, pcp.mod_time, potp.mod_time) AS mod_time, pp.username,"
      + " COALESCE(pmp.medicine_id::text, ptp.test_id, psp.service_id, pcp.doctor_id, '')"
      + " AS item_id, CASE WHEN presc_type = 'NonBillable' THEN item_name"
      + " WHEN presc_type = 'Medicine' THEN sid.medicine_name "
      + " WHEN presc_type = 'Inv.' THEN test_name WHEN presc_type = 'Doctor'"
      + " THEN cdoc.doctor_name WHEN presc_type = 'Service' THEN service_name END item_name,"
      + " pmp.strength as med_dosage,"
      + " pmp.route_of_admin as med_route, pmp.item_form_id as med_form_id,"
      + " pmp.item_strength as med_strength, pp.prior_med, COALESCE(pmp.medicine_remarks,"
      + " pomp.medicine_remarks, ptp.test_remarks, psp.service_remarks, pcp.cons_remarks,"
      + " potp.item_remarks) as remarks, pp.freq_type, pp.recurrence_daily_id,"
      + " rdm.display_name as recurrence_name, pp.repeat_interval, pp.repeat_interval_units,"
      + " pp.start_datetime, pp.end_datetime, pp.prescribed_date as prescription_date,"
      + " pp.no_of_occurrences, pp.end_on_discontinue, pp.discontinued,"
      + " item_form_name as med_form_name, gn.generic_name, pmp.generic_code,"
      + " mr.route_name as med_route_name, sid.consumption_uom, pp.prior_med,"
      + " EXISTS (SELECT * FROM patient_activities WHERE"
      + " (activity_status='D' OR order_no is not null) AND prescription_id=pp.patient_presc_id)"
      + " as has_completed_activities, pmp.item_strength_units as med_strength_units,"
      + " unit_name, pmp.admin_strength, granular_units"
      + " FROM patient_prescription pp"
      + " LEFT JOIN patient_medicine_prescriptions pmp"
      + " on (pp.presc_type='Medicine' and pmp.op_medicine_pres_id=pp.patient_presc_id)"
      + " LEFT JOIN patient_other_medicine_prescriptions pomp "
      + " on (pp.presc_type='Medicine' and pomp.prescription_id=pp.patient_presc_id)"
      + " LEFT JOIN patient_test_prescriptions ptp "
      + " on (pp.presc_type='Inv.' and ptp.op_test_pres_id=pp.patient_presc_id)"
      + " LEFT JOIN patient_consultation_prescriptions pcp "
      + " on (pp.presc_type='Doctor' and pcp.prescription_id=pp.patient_presc_id)"
      + " LEFT JOIN patient_service_prescriptions psp "
      + " on (pp.presc_type='Service' and pp.patient_presc_id=psp.op_service_pres_id)"
      + " LEFT JOIN patient_other_prescriptions potp "
      + " on ((pp.presc_type='NonBillable' and pp.patient_presc_id=potp.prescription_id))"
      + " JOIN doctors doc ON (pp.doctor_id=doc.doctor_id)"
      + " LEFT JOIN store_item_details sid"
      + " ON (pp.presc_type = 'Medicine' AND pmp.medicine_id=sid.medicine_id)"
      + " LEFT JOIN generic_name gn ON (pp.presc_type='Medicine'"
      + " AND pmp.generic_code=gn.generic_code) "
      + " LEFT JOIN all_tests_pkgs_view atp ON (pp.presc_type = 'Inv.'"
      + " AND ptp.test_id=atp.test_id)"
      + " LEFT JOIN services s ON (pp.presc_type = 'Service' AND psp.service_id=s.service_id)"
      + " LEFT JOIN doctors cdoc ON (pp.presc_type = 'Doctor'"
      + " AND pcp.doctor_id=cdoc.doctor_id) "
      + " LEFT JOIN recurrence_daily_master rdm "
      + " ON (pp.recurrence_daily_id=rdm.recurrence_daily_id) "
      + " LEFT JOIN item_form_master ifm ON (pmp.item_form_id=ifm.item_form_id) "
      + " LEFT JOIN medicine_route mr ON (pmp.route_of_admin=mr.route_id) "
      + " LEFT JOIN strength_units su ON (su.unit_id=pmp.item_strength_units) " 
      + " WHERE #";

  /**
   * Gets the prescriptions.
   *
   * @param patientId the patient id
   * @return the prescriptions
   * @throws SQLException the SQL exception
   */
  public List getPrescriptions(String patientId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    String query = GET_PRESCRIPTIONS;
    try {
      query = query.replace("#", " pp.visit_id=?");
      ps = con.prepareStatement(query);
      ps.setString(1, patientId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

  }


  /** The Constant GET_PRESCRIPTIONS_ADM_REQUEST. */
  public static final String GET_PRESCRIPTIONS_ADM_REQUEST = " SELECT pp.visit_id,"
      + " pp.patient_presc_id AS prescription_id, pp.doctor_id, doc.doctor_name,"
      + " CASE WHEN presc_type = 'NonBillable' THEN 'O' WHEN presc_type = 'Medicine' THEN 'M' "
      + " WHEN presc_type = 'Inv.' THEN 'I' WHEN presc_type = 'Doctor' THEN 'C' "
      + " WHEN presc_type = 'Service' THEN 'S'  WHEN presc_type = 'Operation' THEN 'OPE'"
      + " END  presc_type, pp.username as entered_by, pp.prescribed_date AS entered_datetime,"
      + " COALESCE(pmp.mod_time, pomp.mod_time, ptp.mod_time, psp.mod_time, pcp.mod_time, "
      + "   potp.mod_time) AS mod_time, pp.username, COALESCE(pmp.medicine_id::text, "
      + " ptp.test_id, psp.service_id, pcp.doctor_id,pop.operation_id, '') AS item_id, "
      + " CASE WHEN presc_type = 'NonBillable' THEN item_name"
      + " WHEN presc_type = 'Medicine' THEN sid.medicine_name "
      + "   WHEN presc_type = 'Inv.' THEN test_name "
      + "WHEN presc_type = 'Doctor' THEN cdoc.doctor_name "
      + "   WHEN presc_type = 'Service' THEN service_name"
      + " WHEN presc_type = 'Operation' THEN operation_name END item_name,"
      + " pmp.strength as med_dosage, pmp.route_of_admin as med_route,"
      + " pmp.item_form_id as med_form_id, pmp.item_strength as med_strength,"
      + " pp.prior_med, COALESCE(pmp.medicine_remarks, pomp.medicine_remarks, "
      + "   ptp.test_remarks, psp.service_remarks, pcp.cons_remarks, "
      + " potp.item_remarks,pop.remarks) as remarks, pp.freq_type, pp.recurrence_daily_id,"
      + " rdm.display_name as recurrence_name, pp.repeat_interval,"
      + " pp.repeat_interval_units,  pp.start_datetime, pp.end_datetime,"
      + " pp.prescribed_date as prescription_date, "
      + " pp.no_of_occurrences, pp.end_on_discontinue, pp.discontinued,"
      + " item_form_name as med_form_name, gn.generic_name, pmp.generic_code,"
      + " mr.route_name as med_route_name, sid.cons_uom_id, cum.consumption_uom, pp.prior_med, "
      + " EXISTS (SELECT * FROM patient_activities"
      + " WHERE (activity_status='D' OR order_no is not null)"
      + "   AND prescription_id=pp.patient_presc_id) as has_completed_activities, "
      + " pmp.item_strength_units as med_strength_units, unit_name,"
      + " pmp.admin_strength, granular_units"
      + " FROM patient_prescription pp"
      + "   LEFT JOIN patient_medicine_prescriptions pmp on"
      + " (pp.presc_type='Medicine' and pmp.op_medicine_pres_id=pp.patient_presc_id) "
      + "   LEFT JOIN patient_other_medicine_prescriptions pomp on"
      + " (pp.presc_type='Medicine' and pomp.prescription_id=pp.patient_presc_id)"
      + "   LEFT JOIN patient_test_prescriptions ptp"
      + " on (pp.presc_type='Inv.' and ptp.op_test_pres_id=pp.patient_presc_id) "
      + "   LEFT JOIN patient_consultation_prescriptions pcp on"
      + " (pp.presc_type='Doctor' and pcp.prescription_id=pp.patient_presc_id) "
      + "   LEFT JOIN patient_service_prescriptions psp on"
      + " (pp.presc_type='Service' and pp.patient_presc_id=psp.op_service_pres_id)"
      + "   LEFT JOIN patient_other_prescriptions potp on"
      + " ((pp.presc_type='NonBillable' and pp.patient_presc_id=potp.prescription_id))"
      + "   LEFT JOIN patient_operation_prescriptions pop ON"
      + " (pp.patient_presc_id=pop.prescription_id)"
      + " JOIN doctors doc ON (pp.doctor_id=doc.doctor_id) "
      + " LEFT JOIN store_item_details sid ON"
      + " (pp.presc_type = 'Medicine' AND pmp.medicine_id=sid.medicine_id) "
      + " LEFT JOIN generic_name gn ON"
      + " (pp.presc_type='Medicine' AND pmp.generic_code=gn.generic_code) "
      + " LEFT JOIN all_tests_pkgs_view atp ON"
      + " (pp.presc_type = 'Inv.' AND ptp.test_id=atp.test_id) "
      + " LEFT JOIN services s ON"
      + " (pp.presc_type = 'Service' AND psp.service_id=s.service_id) "
      + " LEFT JOIN doctors cdoc ON"
      + " (pp.presc_type = 'Doctor' AND pcp.doctor_id=cdoc.doctor_id) "
      + " LEFT JOIN operation_master om ON (pop.operation_id=om.op_id)"
      + " LEFT JOIN recurrence_daily_master rdm ON"
      + " (pp.recurrence_daily_id=rdm.recurrence_daily_id) "
      + " LEFT JOIN item_form_master ifm ON (pmp.item_form_id=ifm.item_form_id) "
      + " LEFT JOIN medicine_route mr ON (pmp.route_of_admin=mr.route_id) "
      + " LEFT JOIN strength_units su ON (su.unit_id=pmp.item_strength_units) "
      + " LEFT JOIN consumption_uom_master cum ON (cum.cons_uom_id = sid.cons_uom_id)"
      + " WHERE adm_request_id=? AND pp.visit_id is null";

  /**
   * Gets the prescriptions.
   *
   * @param admissionRequestId the admission request id
   * @return the prescriptions
   * @throws SQLException the SQL exception
   */
  public List getPrescriptions(Integer admissionRequestId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    String query = GET_PRESCRIPTIONS_ADM_REQUEST;
    try {
      ps = con.prepareStatement(query);
      ps.setInt(1, admissionRequestId == null ? 0 : admissionRequestId.intValue());
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

  }

  /** The Constant EMR_DOCS. */
  public static final String EMR_DOCS = " SELECT pr.patient_id, doc.doctor_name, pr.reg_date"
      + " FROM patient_prescription pp "
      + " JOIN patient_registration pr ON (pp.visit_id=pr.patient_id) "
      + " JOIN doctors doc ON (pr.doctor=doc.doctor_id) ";

  /**
   * Gets the doctor order for EMR.
   *
   * @param patientId the patient id
   * @param mrNo the mr no
   * @return the doctor order for EMR
   * @throws SQLException the SQL exception
   */
  public List getDoctorOrderForEMR(String patientId, String mrNo) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(EMR_DOCS + " WHERE "
          + (patientId != null ? " pr.patient_id=? " : " pr.mr_no=?")
          + " GROUP BY pr.patient_id, doc.doctor_name, pr.reg_date");
      ps.setString(1, patientId != null ? patientId : mrNo);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GENERICS. */
  private static final String GENERICS = "SELECT generic_name as item_name,"
      + " generic_code as item_id, 0 as qty, generic_code, generic_name, false as ispkg,"
      + " 'item_master' as master, 'M' as item_type, '' as consumption_uom,"
      + " '' as order_code, '' as prior_auth_required, 0 as item_form_id,"
      + " '' as item_strength, '' as pack_type, 0 as med_strength_units "
      + " FROM generic_name "
      + " WHERE (generic_name ilike ? OR generic_name ilike ?) and status='A' "
      + " ORDER BY generic_name limit 100";

  /** The Constant PHARMA_MEDICINES. */
  private static final String PHARMA_MEDICINES = " SELECT sid.medicine_name as item_name,"
      + " sid.medicine_id::text as item_id, "
      + " COALESCE(sum(ssd.qty), 0) AS qty, g.generic_code, g.generic_name, false as ispkg,"
      + "   'item_master' as master, 'M' as item_type, "
      + " sid.cons_uom_id, cum.consumption_uom, '' as order_code, "
      + " sid.prior_auth_required, coalesce(sid.item_form_id, 0) as item_form_id,"
      + " sid.item_strength, '' as pack_type, sid.item_strength_units as med_strength_units"
      + " FROM store_stock_details ssd "
      + "   JOIN stores s ON (s.dept_id=ssd.dept_id and auto_fill_indents) "
      + "   JOIN store_item_details sid USING (medicine_id) "
      + " JOIN store_category_master mc ON (mc.category_id = sid.med_category_id) "
      + "   LEFT JOIN generic_name g ON (sid.generic_name=g.generic_code) "
      + " LEFT JOIN consumption_uom_master cum ON (sid.cons_uom_id = cum.cons_uom_id)"
      + " WHERE sid.status='A' AND mc.prescribable "
      + "   AND (medicine_name ilike ? OR g.generic_name ilike ? OR medicine_name ilike ?) "
      + " GROUP BY medicine_name, sid.medicine_id, g.generic_name, g.generic_code, "
      + "   cum.consumption_uom, sid.prior_auth_required,"
      + " sid.item_form_id, sid.item_strength, sid.item_strength_units "
      + " ORDER BY medicine_name limit 100";

  /** The Constant ALL_TESTS_FIELDS_FOR_ORG. */
  private static final String ALL_TESTS_FIELDS_FOR_ORG = " SELECT atpv.test_name as item_name,"
      + " atpv.test_id as item_id, 0 as qty, '' as generic_code, "
      + " '' as generic_name, atpv.ispkg, 'item_master' as master, 'I' as item_type, "
      + " '' as consumption_uom, order_code::text, "
      + " atpv.prior_auth_required, 0 as item_form_id, '' as item_strength, pack_type,"
      + " 0 as med_strength_units "
      + " FROM all_tests_pkgs_view atpv "
      + " LEFT JOIN diagnostics dia on (atpv.test_id=dia.test_id) "
      + " LEFT OUTER JOIN test_org_details tod ON tod.org_id=? and tod.applicable AND"
      + " atpv.test_id=tod.test_id "
      + " LEFT OUTER JOIN pack_org_details pod ON atpv.test_id=pod.package_id::text AND"
      + " pod.applicable and pod.org_id=?  "
      + " WHERE (dia.is_prescribable is null or dia.is_prescribable) AND "
      + " atpv.approval_status='A' AND (case when atpv.ispkg then exists "
      + " (select package_id as pack_id from center_package_applicability "
      + " pcm where pcm.package_id::text=atpv.test_id"
      + " AND pcm.status='A' AND (pcm.center_id=? or pcm.center_id=-1)) else true end) "
      + " AND (case when atpv.ispkg then exists "
      + " (select pack_id from package_sponsor_master psm where psm.pack_id::text=atpv.test_id"
      + " AND psm.status='A' AND (psm.tpa_id=? OR psm.tpa_id='-1')) else true end) "
      + " AND atpv.status='A' AND (CASE WHEN atpv.ispkg THEN atpv.pack_type='T'"
      + " ELSE atpv.pack_type='' END) "
      + " AND (atpv.test_name ilike ? OR order_code ilike ? OR atpv.test_name ilike ?) limit 100";
  
  /** The Constant ALL_SERVICES_FIELDS_FOR_ORG. */
  private static final String ALL_SERVICES_FIELDS_FOR_ORG = " SELECT s.service_name as item_name,"
      + " s.service_id as item_id, 0 as qty, '' as generic_code, "
      + " '' as generic_name, false as ispkg, 'item_master' as master, 'S' as item_type, "
      + " '' as consumption_uom, service_code::text as order_code,"
      + " s.prior_auth_required, 0 as item_form_id, '' as item_strength, '' as pack_type,"
      + " 0 as med_strength_units "
      + " FROM services s "
      + " LEFT OUTER JOIN service_org_details sod ON sod.org_id=? AND sod.applicable"
      + " AND sod.service_id=s.service_id "
      + " WHERE status='A' AND (service_name ilike ? OR service_code ilike ?"
      + " OR service_name ilike ?) limit 100";
  
  /** The Constant ALL_DOCTORS_FIELDS. */
  private static final String ALL_DOCTORS_FIELDS = " SELECT doctor_name as item_name,"
      + " doctor_id as item_id, 0 as qty, '' as generic_code, '' as generic_name, "
      + " false as ispkg, 'item_master' as master, 'C' as item_type, "
      + " '' as consumption_uom, '' as order_code,"
      + " '' as prior_auth_required, 0 as item_form_id, '' as item_strength, '' as pack_type,"
      + " 0 as med_strength_units "
      + " FROM doctors d "
      + " WHERE (doctor_name ilike ? or doctor_name ilike ? ) AND status='A' limit 100";

  /** The Constant ALL_CARE_PLANS. */
  // get the care plan only if contains atleast one test or service
  private static final String ALL_CARE_PLANS = "SELECT package_name as item_name,"
      + " p.package_id as item_id, 0 as qty, '' as generic_code, '' as generic_name, true as ispkg,"
      + "  'item_master' as master, 'CP' as item_type, '' as consumption_uom, '' as order_code,"
      + " '' as prior_auth_required, "
      + "  0 as item_form_id, '' as item_strength, type as pack_type, 0 as med_strength_units "
      + " FROM packages p "
      + "   JOIN center_package_applicability cpa "
      + " ON (p.package_id=cpa.package_id AND (cpa.center_id=? or cpa.center_id=-1)) "
      + "   JOIN dept_package_applicability dpa"
      + " ON (p.package_id=dpa.package_id AND (dpa.dept_id=? or dpa.dept_id='*')) "
      + " WHERE p.status = 'A' AND ( p.gender_applicability = ? or p.gender_applicability = '*' )"
      + " AND  p.visit_applicability != 'o' AND p.type='O' and p.package_id IN "
      + "   ( SELECT package_id FROM package_contents pc JOIN diagnostics d"
      + " ON (d.test_id=pc.activity_id) WHERE pc.package_id=p.package_id "
      + "     UNION "
      + "     SELECT package_id FROM package_contents pc JOIN services s"
      + " ON (s.service_id=pc.activity_id) WHERE pc.package_id=p.package_id ) "
      + " AND package_name ilike ?";
  
  /** The Constant ALL_SURGERY_FIELDS. */
  private static final String ALL_SURGERY_FIELDS = "  SELECT 'Operation' AS item_type, "
      + " om.op_id AS item_id, om.operation_name AS item_name,"
      + "   '' AS dept_name, om.operation_code AS order_code, ood.item_code, "
      + "     '' AS category, false AS is_package, om.prior_auth_required "
      + " FROM operation_master om "
      + " JOIN operation_org_details ood ON (ood.operation_id=om.op_id AND ood.org_id=?) "
      + " WHERE om.status='A' AND (om.operation_name ilike ? OR"
      + " om.operation_code ilike ? OR om.operation_name ilike ?) ";

  /**
   * Find items.
   *
   * @param orgId the org id
   * @param type the type
   * @param findItem the find item
   * @param useStoreItems the use store items
   * @param centerId the center id
   * @param tpaId the tpa id
   * @param deptId the dept id
   * @param genderApplicability the gender applicability
   * @return the list
   * @throws SQLException the SQL exception
   */
  public static List findItems(String orgId, String type, String findItem, String useStoreItems,
      int centerId, String tpaId, String deptId, String genderApplicability) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      String query = "";
      if (type.equals("M")) {
        query = PHARMA_MEDICINES;
      } else if (type.equals("I")) {
        query = ALL_TESTS_FIELDS_FOR_ORG;
      } else if (type.equals("S")) {
        query = ALL_SERVICES_FIELDS_FOR_ORG;
      } else if (type.equals("C")) {
        query = ALL_DOCTORS_FIELDS;
      } else if (type.equals("OPE")) {
        query = ALL_SURGERY_FIELDS;
      } else if (type.equals("CP")) { // care plans
        query = ALL_CARE_PLANS;
      }
      ps = con.prepareStatement(query);
      if (type.equals("M")) {
        ps.setString(1, findItem + "%"); // name starts with "xx"
        ps.setString(2, findItem + "%"); // generic starts with "xx"
        ps.setString(3, "% " + findItem + "%"); // name contains " xx"
      } else if (type.equals("S")) {
        ps.setString(1, orgId);
        ps.setString(2, findItem + "%");
        ps.setString(3, findItem + "%");
        ps.setString(4, "% " + findItem + "%");
      } else if (type.equals("I")) {
        ps.setString(1, orgId);
        ps.setString(2, orgId);
        ps.setInt(3, centerId);
        ps.setString(4, tpaId);
        ps.setString(5, findItem + "%");
        ps.setString(6, findItem + "%");
        ps.setString(7, "% " + findItem + "%");
      } else if (type.equals("C")) {
        ps.setString(1, findItem + "%");
        ps.setString(2, "% " + findItem + "%");
      } else if (type.equals("OPE")) {
        ps.setString(1, orgId);
        ps.setString(2, findItem + "%");
        ps.setString(3, findItem + "%");
        ps.setString(4, "% " + findItem + "%");
      } else if (type.equals("CP")) {
        ps.setInt(1, centerId);
        ps.setString(2, deptId);
        ps.setString(3, genderApplicability);
        ps.setString(4, "%" + findItem + "%");
      }
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

}
