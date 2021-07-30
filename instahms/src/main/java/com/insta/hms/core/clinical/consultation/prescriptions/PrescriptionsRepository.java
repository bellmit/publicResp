package com.insta.hms.core.clinical.consultation.prescriptions;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PrescriptionsRepository extends GenericRepository {

  public PrescriptionsRepository() {
    super("patient_prescription");
  }

  private static final String GEN_MED_ITEMS =
      "SELECT" + " 'Medicine' AS item_type, generic_code as item_id, '' as presc_activity_type,"
          + " generic_name AS item_name, '' AS dept_name, '' AS order_code, '' AS item_code,"
          + " '' AS category, false AS is_package, '' AS prior_auth_required" + " FROM generic_name"
          + " WHERE ? AND status='A' AND (generic_name ilike ? OR generic_name ilike ?) ";

  private static final String PRES_MED_MASTER_ITEMS = "SELECT"
      + " 'Medicine' AS item_type, '' as item_id, '' as presc_activity_type,"
      + " medicine_name as item_name, '' AS dept_name, '' AS order_code, '' AS item_code,"
      + " '' AS category, false AS is_package, '' AS prior_auth_required"
      + " FROM prescribed_medicines_master pmm"
      + " WHERE ? AND pmm.status='A' AND (pmm.medicine_name ilike ? OR pmm.medicine_name ilike ?) ";

  private static final String MED_STORE_ITEMS = "SELECT"
      + " 'Medicine' AS item_type, sid.medicine_id::text as item_id, '' as presc_activity_type,"
      + " sid.medicine_name as item_name, '' AS dept_name, '' AS order_code,"
      + " sic.item_code AS item_code, '' AS category, false AS is_package, sid.prior_auth_required"
      + " FROM store_item_details sid" + " JOIN LATERAL ("
      + "   SELECT sum(qty) AS qty from store_stock_details ssd, stores s"
      + "   WHERE s.dept_id=ssd.dept_id and auto_fill_prescriptions"
      + "   and ssd.medicine_id = sid.medicine_id AND s.center_id=?) AS ssd ON ssd.qty IS NOT NULL"
      + " LEFT JOIN generic_name g ON (sid.generic_name=g.generic_code)"
      + " LEFT JOIN ha_item_code_type hict"
      + "   ON (hict.medicine_id = sid.medicine_id AND hict.health_authority=? )"
      + " LEFT JOIN store_item_codes sic"
      + "   ON (sic.medicine_id = hict.medicine_id AND sic.code_type = hict.code_type)"
      + " WHERE ? AND sid.status='A' AND (sid.medicine_name ilike ? OR sid.medicine_name ilike ?"
      + "   OR g.generic_name ilike ? ) ";

  private static final String PRES_ITEMS = " UNION ALL "
      // Diagnostics Tests
      + " SELECT 'Inv.' AS item_type, d.test_id AS item_id, '' as presc_activity_type,"
      + " d.test_name AS item_name, '' AS dept_name, d.diag_code AS order_code, tod.item_code,"
      + " dd.category, false AS is_package, d.prior_auth_required" + " FROM diagnostics d"
      + " JOIN test_org_details tod ON (tod.test_id=d.test_id AND tod.org_id=?)"
      + " JOIN diagnostics_departments dd ON (dd.ddept_id=d.ddept_id)"
      + " WHERE ? AND d.is_prescribable AND d.status='A' AND (d.test_name ilike ?"
      + "   OR d.test_name ilike ? OR d.diag_code ilike ?)"
      // Diagnostics Packages
      + " UNION ALL"
      + " SELECT 'Inv.' AS item_type, pm.package_id::text AS item_id, '' as presc_activity_type,"
      + " pm.package_name AS item_name, '' AS dept_name, pm.package_code AS order_code,"
      + " pod.item_code, '' AS category, true AS is_package, pm.prior_auth_required"
      + " FROM packages pm"
      + " LEFT JOIN pack_org_details pod ON (pod.package_id=pm.package_id AND pod.org_id=?)"
      + " JOIN center_package_applicability pcm ON (pcm.package_id=pm.package_id AND pcm.status='A'"
      + "   AND (pcm.center_id=? or pcm.center_id=-1))"
      + " JOIN package_sponsor_master psm ON (psm.pack_id=pm.package_id AND psm.status='A'"
      + "   AND (psm.tpa_id = ? OR psm.tpa_id = '-1' OR psm.tpa_id = '0'))"
      + " JOIN package_plan_master ppm ON (ppm.pack_id = pm.package_id and ppm.status='A' "
      + " AND (ppm.plan_id = ? OR ppm.plan_id = '-1' OR ppm.plan_id = 0)) "
      + " JOIN dept_package_applicability dpa ON (dpa.dept_id IN ('*', ?) "
      + " AND dpa.package_id::integer=pm.package_id::integer) "
      + " WHERE  pm.type='P' AND pm.package_category_id in(-2,-3)"
      + " AND pm.visit_applicability IN (?, '*') AND pm.gender_applicability IN (?, '*')"
      + " AND pm.status='A' AND pm.approval_status='A'"
      + " AND("
      + " ( pm.min_age is null OR pm.max_age is null OR pm.age_unit is null)"
      + " OR"
      + " (('P'||pm.min_age||age_unit)::interval <=  ?::interval AND ('P'||pm.max_age||age_unit)"
      + "::interval >=  ?::interval)"
      + " ) "
      + " AND "
      + " (pm.valid_from is NULL OR (pm.valid_from <=  ?)) "
      + "  AND (pm.valid_till is NULL OR (pm.valid_till >= ?)) "
      + "   AND (pm.package_name ilike ? OR pm.package_name ilike ? OR pm.package_code ilike ?)"
      // Dental Services
      + " UNION ALL "
      + " SELECT 'Service' AS item_type, s.service_id AS item_id, '' as presc_activity_type,"
      + " s.service_name AS item_name, '' AS dept_name, s.service_code AS order_code,"
      + " sod.item_code, '' AS category, false AS is_package, s.prior_auth_required"
      + " FROM services s"
      + " JOIN service_org_details sod ON (sod.service_id = s.service_id AND sod.org_id = ?)"
      + " JOIN services_departments sd ON (sd.serv_dept_id=s.serv_dept_id)"
      + " WHERE ? AND s.status='A' AND coalesce(sd.dept_type_id, '')='DENT'"
      + " AND (s.service_name ilike ? OR s.service_name ilike ? OR s.service_code ilike ?)"
      // Non Dental Services
      + " UNION ALL "
      + " SELECT 'Service' AS item_type, s.service_id AS item_id, '' as presc_activity_type,"
      + " s.service_name AS item_name, '' AS dept_name, s.service_code AS order_code,"
      + " sod.item_code, '' AS category, false AS is_package, s.prior_auth_required"
      + " FROM services s"
      + " JOIN service_org_details sod ON (sod.service_id = s.service_id AND sod.org_id = ?)"
      + " JOIN services_departments sd ON (sd.serv_dept_id=s.serv_dept_id)"
      + " WHERE ? AND s.status='A' AND coalesce(sd.dept_type_id, '')!='DENT'"
      + " AND (s.service_name ilike ? OR s.service_name ilike ? OR s.service_code ilike ?)"
      // Operations
      + " UNION ALL "
      + " SELECT 'Operation' AS item_type, om.op_id AS item_id, '' as presc_activity_type,"
      + " om.operation_name AS item_name, '' AS dept_name, om.operation_code AS order_code,"
      + " ood.item_code, '' AS category, false AS is_package, om.prior_auth_required"
      + " FROM operation_master om"
      + " JOIN operation_org_details ood ON (ood.operation_id=om.op_id AND ood.org_id=?)"
      + " WHERE ? AND om.status='A' AND (om.operation_name ilike ? OR om.operation_name ilike ?"
      + "   OR om.operation_code ilike ?)"
      // Doctors
      + " UNION ALL "
      + " SELECT 'Doctor' AS item_type, d.doctor_id AS item_id, 'DOC' as presc_activity_type,"
      + " doctor_name AS item_name, dept.dept_name, '' AS order_code, '' AS item_code,"
      + " '' AS category, false AS is_package, '' AS prior_auth_required"
      + " FROM doctors d JOIN department dept ON (dept.dept_id=d.dept_id)"
      + " WHERE ? AND d.status='A' AND (doctor_name ilike ? OR doctor_name ilike ?)"
      // Order Sets
      + " UNION ALL " + " SELECT"
      + " (CASE WHEN p.package_category_id = -2 THEN 'Inv.' ELSE 'Order Sets' END) AS item_type,"
      + " p.package_id::text AS item_id, '' as presc_activity_type, p.package_name AS item_name,"
      + " '' AS dept_name, p.package_code AS order_code, '' AS item_code, '' AS category,"
      + " false AS is_package, '' AS prior_auth_required" + " FROM packages p"
      + " JOIN center_package_applicability cpa ON (cpa.center_id IN (-1, ?)"
      + "   AND cpa.package_id=p.package_id)"
      + " JOIN dept_package_applicability dpa ON (dpa.dept_id IN ('*', ?)"
      + "   AND dpa.package_id=p.package_id)" + " WHERE ? AND  p.type = 'O' AND p.status='A' "
      + " AND (p.package_name ilike ? OR p.package_name ilike ?"
      + "   OR p.package_code ilike ?) AND p.gender_applicability IN (?, '*')"
      + "   AND p.visit_applicability IN (?, '*') AND (p.valid_from is NULL OR (p.valid_from <= ?))"
      + " AND (p.valid_till is NULL OR (p.valid_till >= ?))"
      // Department
      + " UNION ALL "
      + " SELECT 'Doctor' AS item_type, d.dept_id AS item_id, 'DEPT' as presc_activity_type,"
      + " dept_name AS item_name, d.dept_name, '' AS order_code, '' AS item_code, '' AS category,"
      + " false AS is_package, '' AS prior_auth_required" + " FROM department d"
      + " WHERE ? AND d.status='A' AND (dept_name ilike ? OR dept_name ilike ?)"
      + " ORDER BY item_name" + " LIMIT ?";


  private static final String GET_STORE_MEDICINE_PRESCRIPTIONS = "SELECT 'Medicine' AS item_type,"
      + " '' as presc_activity_type, false AS non_hosp_medicine,"
      + " pp.patient_presc_id AS item_prescribed_id,"
      + " COALESCE(sid.medicine_id::text, g.generic_code) As item_id,"
      + " COALESCE(sid.medicine_name, g.generic_name) AS item_name , sid.control_type_id,"
      + " g.generic_name, g.generic_code, 'item_master' AS master,"
      + " pp.prescribed_date AS prescribed_date, pmp.admin_strength, pmp.strength,"
      + " pmp.item_strength, pmp.item_strength_units, su.unit_name,"
      + " case when (pp.visit_id is not null and pmp.is_discharge_medication is false) "
      + " then rcm.display_name else pmp.frequency"
      + "   end as frequency, pmp.duration, pmp.duration_units,"
      + " pmp.medicine_quantity AS prescribed_qty, pmp.medicine_remarks AS item_remarks,"
      + " pp.special_instr, false AS is_package, ifm.granular_units, sic.item_code AS drug_code,"
      + " mr.route_id, mr.route_name, pmp.item_form_id, ifm.item_form_name, pmp.cons_uom_id,"
      + " cum.consumption_uom,"
      + " '' AS preauth_required, 'N' AS tooth_num_required, '' AS tooth_unv_number,"
      + " '' AS tooth_fdi_number, pp.status AS issued, sid.prior_auth_required, pmp.erx_status,"
      + " pmp.send_for_erx, pmp.erx_denial_code, idc.type AS denial_code_type,"
      + " idc.code_description AS denial_desc, pmp.erx_denial_remarks, idc.example,"
      + " pmp.refills AS refills, pmp.time_of_intake, pmp.priority, pmp.controlled_drug_number,"
      + " sid.insurance_category_id,"
      + " COALESCE((case when textregexeq(sir.selling_price_expr, '^[0-9]+\\.?[0-9]*$')"
      + "   THEN sir.selling_price_expr::decimal else null end), ssd.mrp,"
      + " sid.item_selling_price, 0) AS charge,"
      + " (COALESCE((case when textregexeq(sir.selling_price_expr, '^[0-9]+\\.?[0-9]*$')"
      + "   THEN sir.selling_price_expr::decimal else null end),"
      + " ssd.mrp, sid.item_selling_price, 0) * COALESCE(mc.discount, 0) /100) AS discount,"
      + " '' AS category, sid.issue_base_unit::integer AS issue_base_unit, sid.route_of_admin,"
      + " '' AS item_rate_plan_code, sic.code_type, pmp.expiry_date, pmp.prescription_format,"
      + " '' AS reorder, pp.item_excluded_from_doctor, pp.item_excluded_from_doctor_remarks,"
      + " pp.doctor_id, pp.prior_med, pp.freq_type, pp.recurrence_daily_id, pp.repeat_interval,"
      + " pp.start_datetime, pp.end_datetime, pp.no_of_occurrences, pp.end_on_discontinue,"
      + " pp.discontinued, pp.repeat_interval_units, pp.adm_request_id, d.doctor_name,"
      + " EXISTS (SELECT * FROM patient_activities WHERE (activity_status='D' "
      + "   OR order_no is not null) AND prescription_id=pp.patient_presc_id) as "
      + "   has_completed_activities,"
      + " (CASE WHEN pp.recurrence_daily_id = -2 THEN 'PRN/SOS' ELSE rcm.display_name END) as "
      + "   recurrence_name, pp.max_doses, pmp.flow_rate, pmp.flow_rate_units, pmp.infusion_period,"
      + " pmp.infusion_period_units, pmp.iv_administer_instructions, pmp.medication_type,"
      + " 'N' as mandate_clinical_info, '' as clinical_justification,"
      + " '' as clinical_note_for_conduction, '' as clinical_justification_for_item"
      + " FROM patient_prescription pp"
      + " JOIN patient_medicine_prescriptions pmp ON (pp.patient_presc_id=pmp.op_medicine_pres_id "
      + " AND is_discharge_medication = ?)"
      + " LEFT JOIN consumption_uom_master cum ON (cum.cons_uom_id=pmp.cons_uom_id)"
      + " LEFT JOIN store_item_details sid ON (pmp.medicine_id=sid.medicine_id)"
      + " JOIN organization_details od ON (od.org_id=?)"
      + " LEFT JOIN store_item_rates sir ON (sir.medicine_id=sid.medicine_id"
      + "   AND sir.store_rate_plan_id=od.store_rate_plan_id)"
      + " LEFT JOIN LATERAL (select max(sibd.mrp) AS mrp FROM store_stock_details ssd, stores s,"
      + "   store_item_batch_details sibd WHERE s.dept_id=ssd.dept_id and auto_fill_prescriptions"
      + "   and ssd.medicine_id = sid.medicine_id AND ssd.item_batch_id=sibd.item_batch_id"
      + "   AND s.center_id=? ) AS ssd ON true"
      + " LEFT JOIN store_category_master mc ON (mc.category_id = sid.med_category_id)"
      + " LEFT JOIN generic_name g ON (pmp.generic_code = g.generic_code)"
      + " LEFT JOIN strength_units su ON (pmp.item_strength_units=su.unit_id)"
      + " LEFT JOIN item_form_master ifm ON (pmp.item_form_id=ifm.item_form_id)"
      + " LEFT JOIN medicine_route mr ON (mr.route_id=pmp.route_of_admin)"
      + " LEFT JOIN insurance_denial_codes idc ON (idc.denial_code = pmp.erx_denial_code)"
      + " LEFT JOIN ha_item_code_type hict ON (hict.medicine_id = sid.medicine_id"
      + "   AND hict.health_authority= ? )"
      + " LEFT JOIN store_item_codes sic ON (sic.medicine_id = hict.medicine_id"
      + "   AND sic.code_type = hict.code_type)"
      + " LEFT JOIN doctors d ON (pp.doctor_id = d.doctor_id)"
      + " LEFT JOIN recurrence_daily_master rcm on (pp.recurrence_daily_id=rcm.recurrence_daily_id)"
      + " WHERE pp.#filter#=? ";

  private static final String GET_OTHER_MEDICINE_PRESCRIPTIONS = "SELECT 'Medicine' AS item_type,"
      + " '' as presc_activity_type, false AS non_hosp_medicine,"
      + " pp.patient_presc_id AS item_prescribed_id, '' AS item_id,"
      + " pomp.medicine_name AS item_name, 0 AS control_type_id, g.generic_name, g.generic_code,"
      + " 'op' AS master, pp.prescribed_date, pomp.admin_strength, pomp.strength,"
      + " pomp.item_strength, pomp.item_strength_units, su.unit_name,"
      + " case when (pp.visit_id is not null and pomp.is_discharge_medication is false)"
      + " then rcm.display_name else pomp.frequency end"
      + "   as frequency, pomp.duration, pomp.duration_units,"
      + " pomp.medicine_quantity AS prescribed_qty, pomp.medicine_remarks AS item_remarks,"
      + " pp.special_instr, false AS is_package, ifm.granular_units, '' AS drug_code, mr.route_id,"
      + " mr.route_name, coalesce(pomp.item_form_id, null) as item_form_id, ifm.item_form_name,"
      + " pomp.cons_uom_id, cum.consumption_uom, '' AS preauth_required, 'N' AS tooth_num_required,"
      + " '' AS tooth_unv_number, '' AS tooth_fdi_number, 'P' AS issued, '' AS prior_auth_required,"
      + " '' AS erx_status, 'N' AS send_for_erx, '' AS erx_denial_code, '' AS denial_code_type,"
      + " '' AS denial_desc, '' AS erx_denial_remarks, '' AS example, pomp.refills,"
      + " pomp.time_of_intake, pomp.priority, '' AS controlled_drug_number,"
      + " 0 AS insurance_category_id, 0 AS charge, 0 AS discount, '' AS category,"
      + " 0 AS issue_base_unit, pms.route_of_admin, '' AS item_rate_plan_code, '' AS code_type,"
      + " pomp.expiry_date, pomp.prescription_format, '' AS reorder, pp.item_excluded_from_doctor,"
      + " pp.item_excluded_from_doctor_remarks, pp.doctor_id, pp.prior_med, pp.freq_type,"
      + " pp.recurrence_daily_id, pp.repeat_interval, pp.start_datetime, pp.end_datetime,"
      + " pp.no_of_occurrences, pp.end_on_discontinue, pp.discontinued, pp.repeat_interval_units,"
      + " pp.adm_request_id, d.doctor_name, EXISTS (SELECT * FROM patient_activities"
      + "   WHERE (activity_status='D' OR order_no is not null)"
      + "   AND prescription_id=pp.patient_presc_id) as has_completed_activities,"
      + " rcm.display_name as recurrence_name, pp.max_doses, null as flow_rate,"
      + " null as flow_rate_units, null as infusion_period, null as infusion_period_units,"
      + " null as iv_administer_instructions, null as medication_type,"
      + " 'N' as mandate_clinical_info, '' as clinical_justification,"
      + " '' as clinical_note_for_conduction, '' as clinical_justification_for_item"
      + " FROM patient_prescription pp" + " JOIN patient_other_medicine_prescriptions pomp"
      + "   ON (pp.patient_presc_id = pomp.prescription_id AND is_discharge_medication = ?)"
      + " LEFT JOIN item_form_master ifm ON (pomp.item_form_id = ifm.item_form_id)"
      + " LEFT JOIN strength_units su ON (su.unit_id=pomp.item_strength_units)"
      + " JOIN prescribed_medicines_master pms ON (pomp.medicine_name=pms.medicine_name)"
      + " LEFT JOIN generic_name g ON (pms.generic_name=g.generic_code)"
      + " LEFT JOIN medicine_route mr ON (mr.route_id=pomp.route_of_admin)"
      + " LEFT JOIN doctors d ON (pp.doctor_id = d.doctor_id)"
      + " LEFT JOIN recurrence_daily_master rcm on (pp.recurrence_daily_id=rcm.recurrence_daily_id)"
      + " LEFT JOIN consumption_uom_master cum ON (cum.cons_uom_id = pomp.cons_uom_id)"
      + " WHERE pp.#filter#=? ";

  private static final String GET_PRESCRIPTIONS = "UNION ALL"
      // Service
      + " SELECT "
      + " 'Service' AS item_type, '' as presc_activity_type, false AS non_hosp_medicine,"
      + " pp.patient_presc_id AS item_prescribed_id, s.service_id AS item_id,"
      + " s.service_name AS item_name, 0 AS control_type_id, '' AS generic_name,"
      + " '' AS generic_code, 'item_master' AS master, pp.prescribed_date, psp.admin_strength,"
      + " '' AS strength, '' AS item_strength, null AS item_strength_units, '' AS unit_name,"
      + " '' AS frequency, null AS duration, '' AS duration_units, psp.qty AS prescribed_qty,"
      + " psp.service_remarks AS item_remarks, pp.special_instr AS special_instr,"
      + " false AS is_package, 'N' AS granular_units, '' AS drug_code, null AS route_id,"
      + " '' AS route_name, null AS item_form_id, '' AS item_form_name, null as cons_uom_id,"
      + " '' AS consumption_uom,"
      + " case when (psp.preauth_required='' or psp.preauth_required=' ') then 'N'"
      + "   else psp.preauth_required end as preauth_required, s.tooth_num_required,"
      + " psp.tooth_unv_number, psp.tooth_fdi_number, pp.status AS issued, s.prior_auth_required,"
      + " '' AS erx_status, 'N' AS send_for_erx, '' AS erx_denial_code, '' AS denial_code_type,"
      + " '' AS denial_desc, '' AS erx_denial_remarks, '' AS example, '' AS refills,"
      + " '' AS time_of_intake, psp.priority, '' AS controlled_drug_number,"
      + " s.insurance_category_id, smc.unit_charge as charge, smc.discount, '' AS category,"
      + " 0 AS issue_base_unit, '' AS route_of_admin, sod.item_code AS item_rate_plan_code,"
      + " '' AS code_type, null AS expiry_date, '' AS prescription_format, '' AS reorder,"
      + " pp.item_excluded_from_doctor, pp.item_excluded_from_doctor_remarks, pp.doctor_id,"
      + " pp.prior_med, pp.freq_type, pp.recurrence_daily_id, pp.repeat_interval,"
      + " pp.start_datetime, pp.end_datetime, pp.no_of_occurrences, pp.end_on_discontinue,"
      + " pp.discontinued, pp.repeat_interval_units, pp.adm_request_id, d.doctor_name,"
      + " EXISTS (SELECT * FROM patient_activities WHERE (activity_status='D'"
      + "   OR order_no is not null) AND prescription_id=pp.patient_presc_id)"
      + "   as has_completed_activities, rcm.display_name as recurrence_name, null as max_doses,"
      + " null as flow_rate, null as flow_rate_units, null as infusion_period,"
      + " null as infusion_period_units, null as iv_administer_instructions,"
      + " null as medication_type, 'N' as mandate_clinical_info, '' as clinical_justification,"
      + " '' as clinical_note_for_conduction, '' as clinical_justification_for_item"
      + " FROM patient_prescription pp"
      + " JOIN patient_service_prescriptions psp ON (pp.patient_presc_id=psp.op_service_pres_id)"
      + " JOIN services s ON (s.service_id = psp.service_id)"
      + " JOIN service_master_charges smc ON (s.service_id=smc.service_id AND bed_type=?"
      + "   AND smc.org_id=?)"
      + " JOIN service_org_details sod ON (sod.service_id = smc.service_id AND sod.org_id = ?)"
      + " LEFT JOIN doctors d ON (pp.doctor_id = d.doctor_id)"
      + " LEFT JOIN recurrence_daily_master rcm on (pp.recurrence_daily_id=rcm.recurrence_daily_id)"
      + " WHERE pp.#filter#=?"
      // Operation
      + " UNION ALL" + " SELECT "
      + " 'Operation' AS item_type, '' as presc_activity_type, false AS non_hosp_medicine,"
      + " pp.patient_presc_id AS item_prescribed_id, om.op_id AS item_id,"
      + " om.operation_name AS item_name, 0 AS control_type_id, '' AS generic_name,"
      + " '' AS generic_code, 'item_master' AS master, pp.prescribed_date, pop.admin_strength,"
      + " '' AS strength, '' AS item_strength, null AS item_strength_units, '' AS unit_name,"
      + " '' AS frequency, null AS duration, '' AS duration_units, null AS prescribed_qty,"
      + " pop.remarks AS item_remarks, pp.special_instr, false AS is_package,"
      + " 'N' AS granular_units, '' AS drug_code, null AS route_id, '' AS route_name,"
      + " null AS item_form_id, '' AS item_form_name, null as cons_uom_id, '' AS consumption_uom,"
      + " case when (pop.preauth_required='' or pop.preauth_required=' ') then 'N'"
      + "   else pop.preauth_required end as preauth_required, 'N' AS tooth_num_required,"
      + " '' AS tooth_unv_number, '' AS tooth_fdi_number, pp.status AS issued,"
      + " om.prior_auth_required, '' AS erx_status, 'N' AS send_for_erx, '' AS erx_denial_code,"
      + " '' AS denial_code_type, '' AS denial_desc, '' AS erx_denial_remarks, '' AS example,"
      + " '' AS refills, '' AS time_of_intake, '' AS priority, '' AS controlled_drug_number,"
      + " om.insurance_category_id, oc.surg_asstance_charge as charge,"
      + " oc.surg_asst_discount as discount, '' AS category, 0 AS issue_base_unit,"
      + " '' AS route_of_admin, ood.item_code AS item_rate_plan_code, '' AS code_type,"
      + " null AS expiry_date, '' AS prescription_format, '' AS reorder,"
      + " pp.item_excluded_from_doctor, pp.item_excluded_from_doctor_remarks, pp.doctor_id,"
      + " pp.prior_med, pp.freq_type, pp.recurrence_daily_id, pp.repeat_interval,"
      + " pp.start_datetime, pp.end_datetime, pp.no_of_occurrences, pp.end_on_discontinue,"
      + " pp.discontinued, pp.repeat_interval_units, pp.adm_request_id, d.doctor_name,"
      + " EXISTS (SELECT * FROM patient_activities WHERE (activity_status='D'"
      + "   OR order_no is not null) AND prescription_id=pp.patient_presc_id)"
      + "   as has_completed_activities, rcm.display_name as recurrence_name, null as max_doses,"
      + " null as flow_rate, null as flow_rate_units, null as infusion_period,"
      + " null as infusion_period_units, null as iv_administer_instructions,"
      + " null as medication_type, 'N' as mandate_clinical_info, '' as clinical_justification,"
      + " '' as clinical_note_for_conduction, '' as clinical_justification_for_item"
      + " FROM patient_prescription pp"
      + " JOIN patient_operation_prescriptions pop ON (pp.patient_presc_id=pop.prescription_id)"
      + " JOIN operation_master om ON (pop.operation_id=om.op_id)"
      + " JOIN operation_org_details ood ON (ood.operation_id=om.op_id AND ood.org_id=?)"
      + " JOIN operation_charges oc ON (om.op_id=oc.op_id AND bed_type=? AND oc.org_id=?)"
      + " LEFT JOIN doctors d ON (pp.doctor_id = d.doctor_id)"
      + " LEFT JOIN recurrence_daily_master rcm on (pp.recurrence_daily_id=rcm.recurrence_daily_id)"
      + " WHERE pp.#filter#=?"
      // Doctor
      + " UNION ALL" + " SELECT "
      + " 'Doctor' AS item_type, presc_activity_type, false AS non_hosp_medicine,"
      + " pp.patient_presc_id AS item_prescribed_id, d.doctor_id AS item_id,"
      + " d.doctor_name AS item_name, 0 AS control_type_id, '' AS generic_name,"
      + " '' AS generic_code, 'item_master' AS master, pp.prescribed_date, pcp.admin_strength,"
      + " '' AS strength, '' AS item_strength, null AS item_strength_units, '' AS unit_name,"
      + " '' AS frequency, null AS duration, '' AS duration_units, null AS prescribed_qty,"
      + " pcp.cons_remarks AS item_remarks, pp.special_instr, false AS is_package,"
      + " 'N' AS granular_units, '' AS drug_code, null AS route_id, '' AS route_name,"
      + " null AS item_form_id, '' AS item_form_name, null as cons_uom_id, '' AS consumption_uom,"
      + " case when (pcp.preauth_required='' or pcp.preauth_required=' ') then 'N'"
      + "   else pcp.preauth_required end as preauth_required, 'N' AS tooth_num_required,"
      + " '' AS tooth_unv_number, '' AS tooth_fdi_number, pp.status AS issued,"
      + " '' AS prior_auth_required, '' AS erx_status, 'N' AS send_for_erx, '' AS erx_denial_code,"
      + " '' AS denial_code_type, '' AS denial_desc, '' AS erx_denial_remarks, '' AS example,"
      + " '' AS refills, '' AS time_of_intake, '' AS priority, '' AS controlled_drug_number,"
      + " ct.insurance_category_id, cc.charge + docc.op_charge AS charge,"
      + " cc.discount + docc.op_charge_discount AS discount, '' AS category, 0 AS issue_base_unit,"
      + " '' AS route_of_admin, '' AS item_rate_plan_code, '' AS code_type, null AS expiry_date,"
      + " '' AS prescription_format, '' AS reorder, pp.item_excluded_from_doctor,"
      + " pp.item_excluded_from_doctor_remarks, pp.doctor_id, pp.prior_med, pp.freq_type,"
      + " pp.recurrence_daily_id, pp.repeat_interval, pp.start_datetime, pp.end_datetime,"
      + " pp.no_of_occurrences, pp.end_on_discontinue, pp.discontinued, pp.repeat_interval_units,"
      + " pp.adm_request_id, pres_d.doctor_name, EXISTS (SELECT * FROM patient_activities"
      + "   WHERE (activity_status='D' OR order_no is not null)"
      + "   AND prescription_id=pp.patient_presc_id) as has_completed_activities,"
      + " rcm.display_name as recurrence_name, null as max_doses, null as flow_rate,"
      + " null as flow_rate_units, null as infusion_period, null as infusion_period_units,"
      + " null as iv_administer_instructions, null as medication_type,"
      + " 'N' as mandate_clinical_info, '' as clinical_justification,"
      + " '' as clinical_note_for_conduction, '' as clinical_justification_for_item"
      + " FROM patient_prescription pp"
      + " JOIN patient_consultation_prescriptions pcp ON (pp.patient_presc_id=pcp.prescription_id)"
      + " JOIN doctors d ON (pcp.doctor_id = d.doctor_id)"
      + " JOIN consultation_charges cc ON (cc.bed_type = ? AND cc.org_id=?"
      + "   AND cc.consultation_type_id = -1)"
      + " JOIN doctor_op_consultation_charge docc ON (docc.doctor_id=d.doctor_id"
      + "   AND docc.org_id=cc.org_id)"
      + " JOIN consultation_types ct ON (ct.consultation_type_id=cc.consultation_type_id)"
      + " LEFT JOIN doctors pres_d ON (pp.doctor_id = pres_d.doctor_id)"
      + " LEFT JOIN recurrence_daily_master rcm on (pp.recurrence_daily_id=rcm.recurrence_daily_id)"
      + " WHERE pp.#filter#=?"
      // Tests
      + " UNION ALL" + " SELECT "
      + " 'Inv.' AS item_type, '' as presc_activity_type, false AS non_hosp_medicine,"
      + " pp.patient_presc_id AS item_prescribed_id,"
      + " coalesce(d.test_id, pm.package_id::text) AS item_id,"
      + " coalesce(d.test_name, pm.package_name) AS item_name, 0 AS control_type_id,"
      + " '' AS generic_name, '' AS generic_code, 'item_master' AS master, pp.prescribed_date,"
      + " ptp.admin_strength, '' AS strength, '' AS item_strength, null AS item_strength_units,"
      + " '' AS unit_name, '' AS frequency, null AS duration, '' AS duration_units,"
      + " null AS prescribed_qty, ptp.test_remarks AS item_remarks, pp.special_instr,"
      + " ptp.ispackage AS is_package, 'N' AS granular_units, '' AS drug_code, null AS route_id,"
      + " '' AS route_name, null AS item_form_id, '' AS item_form_name, null as cons_uom_id,"
      + " '' AS consumption_uom,"
      + " case when (ptp.preauth_required='' or ptp.preauth_required=' ') then 'N'"
      + "   else ptp.preauth_required end as preauth_required, 'N' AS tooth_num_required,"
      + " '' AS tooth_unv_number, '' AS tooth_fdi_number, pp.status AS issued,"
      + " coalesce(d.prior_auth_required, pm.prior_auth_required), '' AS erx_status,"
      + " 'N' AS send_for_erx, '' AS erx_denial_code, '' AS denial_code_type, '' AS denial_desc,"
      + " '' AS erx_denial_remarks, '' AS example, '' AS refills, '' AS time_of_intake,"
      + " ptp.priority, '' AS controlled_drug_number, coalesce(d.insurance_category_id,"
      + "   pm.insurance_category_id, 0) AS insurance_category_id,"
      + " coalesce(dc.charge, pc.charge, 0) AS charge,"
      + " coalesce(dc.discount, pc.discount, 0) AS discount, coalesce(dd.category, '') AS category,"
      + " 0 AS issue_base_unit, '' AS route_of_admin,"
      + " coalesce(tod.item_code, pod.item_code) AS item_rate_plan_code, '' AS code_type,"
      + " null AS expiry_date, '' AS prescription_format, ptp.reorder,"
      + " pp.item_excluded_from_doctor, pp.item_excluded_from_doctor_remarks, pp.doctor_id,"
      + " pp.prior_med, pp.freq_type, pp.recurrence_daily_id, pp.repeat_interval,"
      + " pp.start_datetime, pp.end_datetime, pp.no_of_occurrences, pp.end_on_discontinue,"
      + " pp.discontinued, pp.repeat_interval_units, pp.adm_request_id, pres_d.doctor_name,"
      + " EXISTS (SELECT * FROM patient_activities WHERE (activity_status='D'"
      + "   OR order_no is not null) AND prescription_id=pp.patient_presc_id)"
      + "   as has_completed_activities, rcm.display_name as recurrence_name, null as max_doses,"
      + " null as flow_rate, null as flow_rate_units, null as infusion_period,"
      + " null as infusion_period_units, null as iv_administer_instructions,"
      + " null as medication_type, d.mandate_clinical_info, d.clinical_justification,"
      + " ptp.clinical_note_for_conduction, ptp.clinical_justification_for_item"
      + " FROM patient_prescription pp"
      + " JOIN patient_test_prescriptions ptp ON (pp.patient_presc_id=ptp.op_test_pres_id)"
      + " LEFT JOIN diagnostics d ON (d.test_id=ptp.test_id)"
      + " LEFT JOIN test_org_details tod ON (tod.test_id=d.test_id AND tod.org_id=?)"
      + " LEFT JOIN diagnostics_departments dd ON (dd.ddept_id=d.ddept_id)"
      + " LEFT JOIN packages pm ON (pm.package_id::text=ptp.test_id)"
      + " LEFT JOIN pack_org_details pod ON (pod.package_id=pm.package_id AND pod.org_id=?)"
      + " LEFT JOIN diagnostic_charges dc ON (d.test_id=dc.test_id AND dc.bed_type=?"
      + "   AND dc.org_name=?)"
      + " LEFT JOIN package_charges pc ON (pm.package_id=pc.package_id AND pc.bed_type=?"
      + "   AND pc.org_id=?)" + " LEFT JOIN doctors pres_d ON (pp.doctor_id = pres_d.doctor_id)"
      + " LEFT JOIN recurrence_daily_master rcm on (pp.recurrence_daily_id=rcm.recurrence_daily_id)"
      + " WHERE pp.#filter#=?"
      // Non-Hospital
      + " UNION ALL" + " SELECT "
      + " pp.presc_type AS item_type, '' as presc_activity_type, non_hosp_medicine,"
      + " pp.patient_presc_id AS item_prescribed_id, '' AS item_id, pop.item_name,"
      + " 0 AS control_type_id, '' AS generic_name, '' AS generic_code, 'op' AS master,"
      + " pp.prescribed_date, pop.admin_strength, pop.strength, pop.item_strength,"
      + " pop.item_strength_units, su.unit_name, case when pp.visit_id is not null then"
      + "   rcm.display_name else pop.frequency end as frequency, pop.duration, pop.duration_units,"
      + " pop.medicine_quantity AS prescribed_qty, pop.item_remarks, pp.special_instr,"
      + " false AS is_package, ifm.granular_units, '' AS drug_code, null AS route_id,"
      + " '' AS route_name, pop.item_form_id, ifm.item_form_name, pop.cons_uom_id,"
      + " cum.consumption_uom,"
      + " '' AS preauth_required, 'N' AS tooth_num_required, '' AS tooth_unv_number,"
      + " '' AS tooth_fdi_number, 'P' AS issued, '' AS prior_auth_required, '' AS erx_status,"
      + " 'N' AS send_for_erx, '' AS erx_denial_code, '' AS denial_code_type, '' AS denial_desc,"
      + " '' AS erx_denial_remarks, '' AS example, '' AS refills, pop.time_of_intake, pop.priority,"
      + " '' AS controlled_drug_number, 0 AS insurance_category_id, 0 AS charge, 0 AS discount,"
      + " '' AS category, 0 AS issue_base_unit, '' AS route_of_admin, '' AS item_rate_plan_code,"
      + " '' AS code_type, null AS expiry_date, '' AS prescription_format, '' AS reorder,"
      + " pp.item_excluded_from_doctor, pp.item_excluded_from_doctor_remarks, pp.doctor_id,"
      + " pp.prior_med, pp.freq_type, pp.recurrence_daily_id, pp.repeat_interval,"
      + " pp.start_datetime, pp.end_datetime, pp.no_of_occurrences, pp.end_on_discontinue,"
      + " pp.discontinued, pp.repeat_interval_units, pp.adm_request_id, d.doctor_name,"
      + " EXISTS (SELECT * FROM patient_activities WHERE (activity_status='D'"
      + "   OR order_no is not null) AND prescription_id=pp.patient_presc_id)"
      + "   as has_completed_activities, rcm.display_name as recurrence_name, null as max_doses,"
      + " null as flow_rate, null as flow_rate_units, null as infusion_period,"
      + " null as infusion_period_units, null as iv_administer_instructions,"
      + " null as medication_type, 'N' as mandate_clinical_info, '' as clinical_justification,"
      + " '' as clinical_note_for_conduction, '' as clinical_justification_for_item"
      + " FROM patient_prescription pp JOIN patient_other_prescriptions pop"
      + "   ON (pp.patient_presc_id=pop.prescription_id)"
      + " LEFT JOIN strength_units su ON (su.unit_id=pop.item_strength_units)"
      + " LEFT JOIN item_form_master ifm ON (pop.item_form_id=ifm.item_form_id)"
      + " LEFT JOIN doctors d ON (pp.doctor_id = d.doctor_id)"
      + " LEFT JOIN recurrence_daily_master rcm on (pp.recurrence_daily_id=rcm.recurrence_daily_id)"
      + " LEFT JOIN consumption_uom_master cum ON (pop.cons_uom_id = cum.cons_uom_id)"
      + " WHERE pp.#filter#=?"
      // Department
      + " UNION ALL" + " SELECT "
      + "'Doctor' AS item_type, presc_activity_type, false AS non_hosp_medicine,"
      + " pp.patient_presc_id AS item_prescribed_id, d.dept_id AS item_id,"
      + " d.dept_name AS item_name, 0 AS control_type_id, '' AS generic_name, '' AS generic_code,"
      + " 'item_master' AS master, pp.prescribed_date, pcp.admin_strength,'' AS strength,"
      + " '' AS item_strength, null AS item_strength_units, '' AS unit_name, '' AS frequency,"
      + " null AS duration, '' AS duration_units, null AS prescribed_qty,"
      + " pcp.cons_remarks AS item_remarks, pp.special_instr, false AS is_package,"
      + " 'N' AS granular_units, '' AS drug_code, null AS route_id, '' AS route_name,"
      + " null AS item_form_id, '' AS item_form_name, null as cons_uom_id, '' AS consumption_uom,"
      + " case when (pcp.preauth_required='' or pcp.preauth_required=' ') then 'N'"
      + "   else pcp.preauth_required end as preauth_required, 'N' AS tooth_num_required,"
      + " '' AS tooth_unv_number, '' AS tooth_fdi_number, pp.status AS issued,"
      + " '' AS prior_auth_required, '' AS erx_status, 'N' AS send_for_erx, '' AS erx_denial_code,"
      + " '' AS denial_code_type, '' AS denial_desc, '' AS erx_denial_remarks, '' AS example,"
      + " '' AS refills, '' AS time_of_intake, '' AS priority, '' AS controlled_drug_number,"
      + " null as insurance_category_id, 0 AS charge, 0 AS discount, '' AS category,"
      + " 0 AS issue_base_unit, '' AS route_of_admin, '' AS item_rate_plan_code, '' AS code_type,"
      + " null AS expiry_date, '' AS prescription_format, '' AS reorder,"
      + " pp.item_excluded_from_doctor, pp.item_excluded_from_doctor_remarks, pp.doctor_id,"
      + " pp.prior_med, pp.freq_type, pp.recurrence_daily_id, pp.repeat_interval,"
      + " pp.start_datetime, pp.end_datetime, pp.no_of_occurrences, pp.end_on_discontinue,"
      + " pp.discontinued, pp.repeat_interval_units, pp.adm_request_id, pres_d.doctor_name,"
      + " false as has_completed_activities, rcm.display_name as recurrence_name,"
      + " null as max_doses, null as flow_rate, null as flow_rate_units, null as infusion_period,"
      + " null as infusion_period_units, null as iv_administer_instructions,"
      + " null as medication_type, 'N' as mandate_clinical_info, '' as clinical_justification,"
      + " '' as clinical_note_for_conduction, '' as clinical_justification_for_item"
      + " FROM patient_prescription pp"
      + " JOIN patient_consultation_prescriptions pcp ON (pp.patient_presc_id=pcp.prescription_id)"
      + " JOIN department d ON (pcp.dept_id = d.dept_id)"
      + " LEFT JOIN doctors pres_d ON (pp.doctor_id = pres_d.doctor_id)"
      + " LEFT JOIN recurrence_daily_master rcm on (pp.recurrence_daily_id=rcm.recurrence_daily_id)"
      + " WHERE pp.#filter#=? ORDER BY item_type, item_prescribed_id";

  private static final String GET_ALL_PRESCRIPTIONS_FOR_PRIOR_AUTH = "SELECT 'Inv.' AS item_type,"
      + " pp.patient_presc_id, d.test_name AS item_name, d.test_id AS item_id, tod.item_code,"
      + " d.ddept_id AS dept_id, dc.charge, dd.category, dc.discount, tod.code_type,"
      + " d.service_sub_group_id, d.insurance_category_id, d.allow_rate_increase,"
      + " d.allow_rate_decrease, 1 AS item_qty, '' AS tooth_unv_number, '' AS tooth_fdi_number,"
      + " ptp.test_remarks AS item_remarks, ptp.preauth_required, pp.status"
      + " FROM patient_prescription pp"
      + " JOIN patient_test_prescriptions ptp ON (ptp.ispackage=false"
      + "   AND pp.patient_presc_id=ptp.op_test_pres_id)"
      + " JOIN diagnostics d ON (d.test_id=ptp.test_id)"
      + " JOIN diagnostics_departments dd ON (dd.ddept_id = d.ddept_id)"
      + " JOIN diagnostic_charges dc ON (dc.bed_type=? AND dc.org_name=? AND d.test_id=dc.test_id)"
      + " JOIN test_org_details tod ON (tod.org_id =? AND tod.test_id = dc.test_id)"
      + " WHERE pp.consultation_id=?" + " UNION ALL" + " SELECT"
      + " 'Service' AS item_type, pp.patient_presc_id, s.service_name AS item_name,"
      + " s.service_id AS item_id, sod.item_code, s.serv_dept_id::text AS dept_id,"
      + " smc.unit_charge AS charge, '' AS category, smc.discount, sod.code_type,"
      + " s.service_sub_group_id, s.insurance_category_id, s.allow_rate_increase,"
      + " s.allow_rate_decrease, psp.qty AS item_qty, psp.tooth_unv_number, psp.tooth_fdi_number,"
      + " psp.service_remarks AS item_remarks, psp.preauth_required, pp.status"
      + " FROM patient_prescription pp"
      + " JOIN patient_service_prescriptions psp ON (pp.patient_presc_id=psp.op_service_pres_id)"
      + " JOIN services s ON (s.service_id = psp.service_id)"
      + " JOIN service_master_charges smc ON (smc.bed_type=? AND smc.org_id=?"
      + "   AND s.service_id=smc.service_id)"
      + " JOIN service_org_details sod ON (sod.org_id =? AND sod.service_id = smc.service_id)"
      + " WHERE pp.consultation_id=?" + " UNION ALL" + " SELECT"
      + " 'Operation' AS item_type, pp.patient_presc_id, om.operation_name AS item_name,"
      + " om.op_id AS item_id, ood.item_code, om.dept_id, oc.surg_asstance_charge AS charge,"
      + " '' AS category, oc.surg_asst_discount AS discount, ood.code_type,"
      + " om.service_sub_group_id, om.insurance_category_id, om.allow_rate_increase,"
      + " om.allow_rate_decrease, 1 AS item_qty, '' AS tooth_unv_number, '' AS tooth_fdi_number,"
      + " pop.remarks AS item_remarks, pop.preauth_required, pp.status"
      + " FROM patient_prescription pp"
      + " JOIN patient_operation_prescriptions pop ON (pp.patient_presc_id=pop.prescription_id)"
      + " JOIN operation_master om ON (pop.operation_id=om.op_id)"
      + " JOIN operation_charges oc ON (oc.bed_type=? AND oc.org_id=? AND om.op_id=oc.op_id)"
      + " JOIN operation_org_details ood ON (ood.org_id=? AND oc.op_id = ood.operation_id)"
      + " WHERE pp.consultation_id=?" + " UNION ALL" + " SELECT"
      + " 'Doctor' AS item_type, pp.patient_presc_id, d.doctor_name AS item_name,"
      + " d.doctor_id AS item_id, cod.item_code, d.dept_id, dopc.op_charge + cc.charge AS charge,"
      + " '' AS category, dopc.op_charge_discount + cc.discount AS discount, cod.code_type,"
      + " null AS service_sub_group_id, ct.insurance_category_id, ct.allow_rate_increase,"
      + " ct.allow_rate_decrease, 1 AS item_qty, '' AS tooth_unv_number, '' AS tooth_fdi_number,"
      + " pcp.cons_remarks AS item_remarks, pcp.preauth_required, pp.status"
      + " FROM patient_prescription pp"
      + " JOIN patient_consultation_prescriptions pcp ON (pp.patient_presc_id=pcp.prescription_id)"
      + " JOIN doctors d ON (pcp.doctor_id = d.doctor_id)"
      + " JOIN doctor_op_consultation_charge dopc ON (dopc.org_id=? AND dopc.doctor_id=d.doctor_id)"
      + " JOIN consultation_charges cc ON (cc.bed_type =? AND cc.org_id=?"
      + "   AND cc.consultation_type_id = -1)"
      + " JOIN consultation_org_details cod ON (cod.consultation_type_id = -1 AND cod.org_id =?)"
      + " JOIN doctor_op_consultation_charge docc ON (docc.org_id=? AND docc.doctor_id=d.doctor_id)"
      + " JOIN consultation_types ct ON (ct.consultation_type_id=-1)"
      + " WHERE pp.consultation_id=?" + " ORDER BY patient_presc_id";

  private static final String STORE_MEDICINE_PRESCRIPTIONS_WITH_IDS = "SELECT"
      + " 'Medicine' AS item_type," + " '' AS presc_activity_type, false AS non_hosp_medicine,"
      + " COALESCE(sid.medicine_id::text, g.generic_code) AS item_id,"
      + " COALESCE(sid.medicine_name, g.generic_name) AS item_name, g.generic_name, g.generic_code,"
      + " 'item_master' AS master, pmp.admin_strength, pmp.strength, pmp.item_strength,"
      + " pmp.item_strength_units, su.unit_name, pmp.frequency, pmp.duration, pmp.duration_units,"
      + " pmp.medicine_quantity AS prescribed_qty, pmp.medicine_remarks AS item_remarks,"
      + " pp.special_instr, false AS is_package, ifm.granular_units, sic.item_code AS drug_code,"
      + " mr.route_id, mr.route_name, pmp.item_form_id, ifm.item_form_name, pmp.cons_uom_id,"
      + " cum.consumption_uom,"
      + " '' AS preauth_required, 'N' AS tooth_num_required, '' AS tooth_unv_number,"
      + " '' AS tooth_fdi_number, sid.prior_auth_required, pmp.refills AS refills,"
      + " COALESCE((case when textregexeq(sir.selling_price_expr, '^[0-9]+\\.?[0-9]*$')"
      + "   then sir.selling_price_expr::decimal else null end), ssd.mrp, sid.item_selling_price,"
      + "   0) AS charge, "
      + " (COALESCE((case when textregexeq(sir.selling_price_expr, '^[0-9]+\\.?[0-9]*$')"
      + "   then sir.selling_price_expr::decimal else null end), ssd.mrp, sid.item_selling_price,"
      + "   0) * COALESCE(mc.discount, 0) / 100 )AS discount, sid.status, pmp.time_of_intake,"
      + " pmp.priority, pmp.controlled_drug_number, '' AS category,"
      + " COALESCE(sid.insurance_category_id, 0) AS insurance_category_id,"
      + " sid.issue_base_unit::integer AS issue_base_unit, sid.route_of_admin,"
      + " '' AS item_rate_plan_code, pmp.prescription_format, 'N' as mandate_clinical_info,"
      + " '' as clinical_justification, pp.item_excluded_from_doctor,"
      + " pp.item_excluded_from_doctor_remarks, true AS is_prescribable"
      + " FROM patient_prescription pp"
      + " JOIN patient_medicine_prescriptions pmp ON (pp.patient_presc_id=pmp.op_medicine_pres_id)"
      + " LEFT JOIN consumption_uom_master cum ON (cum.cons_uom_id=pmp.cons_uom_id)"
      + " LEFT JOIN store_item_details sid ON (pmp.medicine_id=sid.medicine_id)"
      + " LEFT JOIN generic_name g ON (pmp.generic_code = g.generic_code)"
      + " LEFT JOIN strength_units su ON (pmp.item_strength_units=su.unit_id)"
      + " LEFT JOIN item_form_master ifm ON (pmp.item_form_id=ifm.item_form_id)"
      + " LEFT JOIN medicine_route mr ON (mr.route_id=pmp.route_of_admin)"
      + " JOIN organization_details od ON (od.org_id=:org_id)"
      + " LEFT JOIN store_item_rates sir ON (sir.medicine_id=sid.medicine_id"
      + "   AND sir.store_rate_plan_id=od.store_rate_plan_id)"
      + " LEFT JOIN LATERAL (select max(sibd.mrp) AS mrp from store_stock_details ssd, stores s,"
      + "   store_item_batch_details sibd where s.dept_id=ssd.dept_id and auto_fill_prescriptions"
      + "   and ssd.medicine_id = sid.medicine_id AND ssd.item_batch_id=sibd.item_batch_id"
      + "   AND s.center_id=:center_id ) AS ssd ON true"
      + " LEFT JOIN store_category_master mc ON (mc.category_id = sid.med_category_id)"
      + " LEFT JOIN ha_item_code_type hict ON (hict.medicine_id = sid.medicine_id"
      + "   AND hict.health_authority= :health_authority)"
      + " LEFT JOIN store_item_codes sic ON (sic.medicine_id = hict.medicine_id"
      + "   AND sic.code_type = hict.code_type)" + " WHERE pp.patient_presc_id IN (:pres_ids) ";

  private static final String OTHER_MEDICINE_PRESCRIPTIONS_WITH_IDS = "SELECT"
      + " 'Medicine' AS item_type,"
      + " '' AS presc_activity_type, false AS non_hosp_medicine, '' AS item_id,"
      + " pomp.medicine_name AS item_name, g.generic_name, g.generic_code, 'op' AS master,"
      + " pomp.admin_strength, pomp.strength, pomp.item_strength, pomp.item_strength_units,"
      + " su.unit_name, pomp.frequency, pomp.duration, pomp.duration_units,"
      + " pomp.medicine_quantity AS prescribed_qty, pomp.medicine_remarks AS item_remarks,"
      + " pp.special_instr, false AS is_package, ifm.granular_units, '' AS drug_code, mr.route_id,"
      + " mr.route_name, coalesce(pomp.item_form_id, null) as item_form_id, ifm.item_form_name,"
      + " pomp.cons_uom_id, cum.consumption_uom, '' AS preauth_required, 'N' AS tooth_num_required,"
      + " '' AS tooth_unv_number, '' AS tooth_fdi_number, '' AS prior_auth_required, pomp.refills,"
      + " 0 AS charge, 0 AS discount, pms.status, pomp.time_of_intake, pomp.priority,"
      + " '' AS controlled_drug_number, '' AS category, 0 AS insurance_category_id,"
      + " 0 AS issue_base_unit, pms.route_of_admin, '' AS item_rate_plan_code,"
      + " pomp.prescription_format, 'N' as mandate_clinical_info, '' as clinical_justification,"
      + " pp.item_excluded_from_doctor, pp.item_excluded_from_doctor_remarks,"
      + " true AS is_prescribable" + " FROM patient_prescription pp"
      + " JOIN patient_other_medicine_prescriptions pomp"
      + "   ON (pp.patient_presc_id = pomp.prescription_id)"
      + " LEFT JOIN item_form_master ifm ON (pomp.item_form_id = ifm.item_form_id)"
      + " LEFT JOIN strength_units su ON (su.unit_id=pomp.item_strength_units)"
      + " JOIN prescribed_medicines_master pms ON (pomp.medicine_name=pms.medicine_name)"
      + " LEFT JOIN generic_name g ON (pms.generic_name=g.generic_code)"
      + " LEFT JOIN medicine_route mr ON (mr.route_id=pomp.route_of_admin)"
      + " LEFT JOIN consumption_uom_master cum ON (cum.cons_uom_id = pomp.cons_uom_id)"
      + " WHERE pp.patient_presc_id IN (:pres_ids) ";

  private static final String PRESCRIPTIONS_WITH_IDS = " UNION ALL SELECT 'Service' AS item_type,"
      + " '' AS presc_activity_type, false non_hosp_medicine, s.service_id AS item_id,"
      + " s.service_name AS item_name, '' AS generic_name, '' AS generic_code,"
      + " 'item_master' AS master, psp.admin_strength, '' AS strength, '' AS item_strength,"
      + " null AS item_strength_units, '' AS unit_name, '' AS frequency, null AS duration,"
      + " '' AS duration_units, psp.qty AS prescribed_qty, psp.service_remarks AS item_remarks,"
      + " pp.special_instr AS special_instr, false AS is_package, 'N' AS granular_units,"
      + " '' AS drug_code, null AS route_id, '' AS route_name, null AS item_form_id,"
      + " '' AS item_form_name, null as cons_uom_id, '' AS consumption_uom,"
      + " case when (psp.preauth_required='' or psp.preauth_required=' ') then 'N'"
      + "   else psp.preauth_required end as preauth_required, s.tooth_num_required,"
      + " psp.tooth_unv_number, psp.tooth_fdi_number, s.prior_auth_required, '' AS refills,"
      + " smc.unit_charge AS charge, smc.discount, s.status, '' AS time_of_intake, psp.priority,"
      + " '' AS controlled_drug_number, '' AS category, s.insurance_category_id,"
      + " 0 AS issue_base_unit, '' AS route_of_admin, sod.item_code AS item_rate_plan_code,"
      + " null AS prescription_format, 'N' as mandate_clinical_info, '' as clinical_justification,"
      + " pp.item_excluded_from_doctor, pp.item_excluded_from_doctor_remarks,"
      + " true AS is_prescribable" + " FROM patient_prescription pp"
      + " JOIN patient_service_prescriptions psp ON (pp.patient_presc_id=psp.op_service_pres_id)"
      + " JOIN services s ON (s.service_id = psp.service_id)"
      + " JOIN service_master_charges smc ON (s.service_id=smc.service_id AND bed_type=:bed_type"
      + "   AND smc.org_id=:org_id)"
      + " JOIN service_org_details sod ON (sod.service_id = smc.service_id"
      + "   AND sod.org_id = :org_id)" + " WHERE pp.patient_presc_id IN (:pres_ids)" + " UNION ALL"
      + " SELECT 'Operation' AS item_type, '' AS presc_activity_type, false AS non_hosp_medicine,"
      + " om.op_id AS item_id, om.operation_name AS item_name, '' AS generic_name,"
      + " '' AS generic_code, 'item_master' AS master, pop.admin_strength, '' AS strength,"
      + " '' AS item_strength, null AS item_strength_units, '' AS unit_name, '' AS frequency,"
      + " null AS duration, '' AS duration_units, null AS prescribed_qty,"
      + " pop.remarks AS item_remarks, pp.special_instr, false AS is_package,"
      + " 'N' AS granular_units, '' AS drug_code, null AS route_id, '' AS route_name,"
      + " null AS item_form_id, '' AS item_form_name, null as cons_uom_id, '' AS consumption_uom,"
      + " case when (pop.preauth_required='' or pop.preauth_required=' ') then 'N'"
      + "   else pop.preauth_required end as preauth_required, 'N' AS tooth_num_required,"
      + " '' AS tooth_unv_number, '' AS tooth_fdi_number, om.prior_auth_required, '' AS refills,"
      + " oc.surg_asstance_charge AS charge, oc.surg_asst_discount AS discount, om.status,"
      + " '' AS time_of_intake, '' AS priority, '' AS controlled_drug_number, '' AS category,"
      + " om.insurance_category_id, 0 AS issue_base_unit, '' AS route_of_admin,"
      + " ood.item_code AS item_rate_plan_code, null AS prescription_format,"
      + " 'N' as mandate_clinical_info, '' as clinical_justification, pp.item_excluded_from_doctor,"
      + " pp.item_excluded_from_doctor_remarks, true AS is_prescribable"
      + " FROM patient_prescription pp"
      + " JOIN patient_operation_prescriptions pop ON (pp.patient_presc_id=pop.prescription_id)"
      + " JOIN operation_master om ON (pop.operation_id=om.op_id)"
      + " JOIN operation_org_details ood ON (ood.operation_id=om.op_id AND ood.org_id=:org_id)"
      + " JOIN operation_charges oc ON (om.op_id=oc.op_id AND bed_type=:bed_type"
      + "   AND oc.org_id=:org_id)" + " WHERE pp.patient_presc_id IN (:pres_ids)" + " UNION ALL"
      + " SELECT 'Doctor' AS item_type, pcp.presc_activity_type, false AS non_hosp_medicine,"
      + " d.doctor_id AS item_id, d.doctor_name AS item_name, '' AS generic_name,"
      + " '' AS generic_code, 'item_master' AS master, pcp.admin_strength, '' AS strength,"
      + " '' AS item_strength, null AS item_strength_units, '' AS unit_name, '' AS frequency,"
      + " null AS duration, '' AS duration_units, null AS prescribed_qty,"
      + " pcp.cons_remarks AS item_remarks, pp.special_instr, false AS is_package,"
      + " 'N' AS granular_units, '' AS drug_code, null AS route_id, '' AS route_name,"
      + " null AS item_form_id, '' AS item_form_name, null as cons_uom_id, '' AS consumption_uom,"
      + " case when (pcp.preauth_required='' or pcp.preauth_required=' ') then 'N'"
      + "   else pcp.preauth_required end as preauth_required, 'N' AS tooth_num_required,"
      + " '' AS tooth_unv_number, '' AS tooth_fdi_number, '' AS prior_auth_required, '' AS refills,"
      + " cc.charge + docc.op_charge AS charge, cc.discount + docc.op_charge_discount AS discount,"
      + " d.status, '' AS time_of_intake, '' AS priority, '' AS controlled_drug_number,"
      + " '' AS category, ct.insurance_category_id, 0 AS issue_base_unit, '' AS route_of_admin,"
      + " '' AS item_rate_plan_code, null AS prescription_format, 'N' as mandate_clinical_info,"
      + " '' as clinical_justification, pp.item_excluded_from_doctor,"
      + " pp.item_excluded_from_doctor_remarks, true AS is_prescribable"
      + " FROM patient_prescription pp"
      + " JOIN patient_consultation_prescriptions pcp ON (pp.patient_presc_id=pcp.prescription_id)"
      + " JOIN doctors d ON (pcp.doctor_id = d.doctor_id)"
      + " JOIN consultation_charges cc ON (cc.bed_type =:bed_type AND cc.org_id=:org_id"
      + "   AND cc.consultation_type_id = -1)"
      + " JOIN doctor_op_consultation_charge docc ON (docc.doctor_id=d.doctor_id"
      + "   AND docc.org_id=cc.org_id)"
      + " JOIN consultation_types ct ON (ct.consultation_type_id=-1)"
      + " WHERE pp.patient_presc_id IN (:pres_ids)" + " UNION ALL"
      + " SELECT 'Inv.' AS item_type, '' AS presc_activity_type, false AS non_hosp_medicine,"
      + " coalesce(d.test_id, pm.package_id::text) AS item_id,"
      + " coalesce(d.test_name, pm.package_name) AS item_name, '' AS generic_name,"
      + " '' AS generic_code, 'item_master' AS master, ptp.admin_strength, '' AS strength,"
      + " '' AS item_strength, null AS item_strength_units, '' AS unit_name, '' AS frequency,"
      + " null AS duration, '' AS duration_units, null AS prescribed_qty,"
      + " ptp.test_remarks AS item_remarks, pp.special_instr, ptp.ispackage AS is_package,"
      + " 'N' AS granular_units, '' AS drug_code, null AS route_id, '' AS route_name,"
      + " null AS item_form_id, '' AS item_form_name, null as cons_uom_id, '' AS consumption_uom,"
      + " case when (ptp.preauth_required='' or ptp.preauth_required=' ') then 'N'"
      + "   else ptp.preauth_required end as preauth_required, 'N' AS tooth_num_required,"
      + " '' AS tooth_unv_number, '' AS tooth_fdi_number,"
      + " coalesce(d.prior_auth_required, pm.prior_auth_required), '' AS refills,"
      + " coalesce(dc.charge, pc.charge, 0) AS charge, coalesce(dc.discount, pc.discount,"
      + "   0) AS discount, coalesce(d.status, CASE WHEN EXISTS (SELECT *"
      + "   FROM center_package_applicability pcm JOIN package_sponsor_master psm"
      + "   ON (psm.pack_id=pm.package_id AND psm.status='A' AND (psm.tpa_id=:tpa_id"
      + "     OR psm.tpa_id = '-1')) WHERE pcm.package_id=pm.package_id AND pcm.status='A'"
      + "   AND (pcm.center_id=:center_id or pcm.center_id=-1)) AND pm.status='A' THEN 'A'"
      + "   ELSE 'I' END) AS status, '' AS time_of_intake, ptp.priority,"
      + " '' AS controlled_drug_number, coalesce(dd.category, '') AS category,"
      + " coalesce(d.insurance_category_id, pm.insurance_category_id) AS insurance_category_id,"
      + " 0 AS issue_base_unit, '' AS route_of_admin,"
      + " coalesce(tod.item_code, pod.item_code) AS item_rate_plan_code,"
      + " null AS prescription_format, d.mandate_clinical_info, d.clinical_justification,"
      + " pp.item_excluded_from_doctor, pp.item_excluded_from_doctor_remarks,"
      + " (CASE WHEN ptp.ispackage THEN true ELSE d.is_prescribable END) AS is_prescribable"
      + " FROM patient_prescription pp"
      + " JOIN patient_test_prescriptions ptp ON (pp.patient_presc_id=ptp.op_test_pres_id)"
      + " LEFT JOIN diagnostics d ON (d.test_id=ptp.test_id)"
      + " LEFT JOIN test_org_details tod ON (tod.test_id=d.test_id AND tod.org_id=:org_id)"
      + " LEFT JOIN diagnostics_departments dd ON (dd.ddept_id=d.ddept_id)"
      + " LEFT JOIN diagnostic_charges dc ON (d.test_id=dc.test_id AND dc.bed_type=:bed_type"
      + "   AND dc.org_name=:org_id)"
      + " LEFT JOIN packages pm ON (pm.package_id::text=ptp.test_id)"
      + " LEFT JOIN pack_org_details pod ON (pod.package_id=pm.package_id AND pod.org_id=:org_id)"
      + " LEFT JOIN package_charges pc ON (pm.package_id=pc.package_id AND pc.bed_type=:bed_type"
      + "   AND pc.org_id=:org_id) WHERE pp.patient_presc_id IN (:pres_ids)" + " UNION ALL"
      + " SELECT CASE WHEN non_hosp_medicine THEN 'Medicine' ELSE 'NonHospital' END AS item_type,"
      + " '' AS presc_activity_type, non_hosp_medicine, '' AS item_id, pop.item_name,"
      + " '' AS generic_name, '' AS generic_code, 'op' AS master, pop.admin_strength, pop.strength,"
      + " pop.item_strength, pop.item_strength_units, su.unit_name, pop.frequency, pop.duration,"
      + " pop.duration_units, pop.medicine_quantity AS prescribed_qty, pop.item_remarks,"
      + " pp.special_instr, false AS is_package, ifm.granular_units, '' AS drug_code,"
      + " null AS route_id, '' AS route_name, pop.item_form_id, ifm.item_form_name,"
      + " pop.cons_uom_id, cum.consumption_uom, '' AS preauth_required,"
      + " 'N' AS tooth_num_required,"
      + " '' AS tooth_unv_number, '' AS tooth_fdi_number, '' AS prior_auth_required,"
      + " '' AS refills, 0 AS charge, 0 AS discount, 'A' AS status, pop.time_of_intake,"
      + " pop.priority, '' AS controlled_drug_number, '' AS category, 0 AS insurance_category_id,"
      + " 0 AS issue_base_unit, '' AS route_of_admin, '' AS item_rate_plan_code,"
      + " null AS prescription_format, 'N' as mandate_clinical_info, '' as clinical_justification,"
      + " pp.item_excluded_from_doctor, pp.item_excluded_from_doctor_remarks,"
      + " true AS is_prescribable" + " FROM patient_prescription pp"
      + " JOIN patient_other_prescriptions pop ON (pp.patient_presc_id=pop.prescription_id)"
      + " LEFT JOIN strength_units su ON (su.unit_id=pop.item_strength_units)"
      + " LEFT JOIN item_form_master ifm ON (pop.item_form_id=ifm.item_form_id)"
      + " LEFT JOIN consumption_uom_master cum ON (cum.cons_uom_id = pop.cons_uom_id)"
      + " WHERE pp.patient_presc_id IN (:pres_ids)" + " UNION ALL"
      + " SELECT 'Doctor' AS item_type, pcp.presc_activity_type, false AS non_hosp_medicine,"
      + " d.dept_id AS item_id, d.dept_name AS item_name, '' AS generic_name, '' AS generic_code,"
      + " 'item_master' AS master, pcp.admin_strength, '' AS strength, '' AS item_strength,"
      + " null AS item_strength_units, '' AS unit_name, '' AS frequency, null AS duration,"
      + " '' AS duration_units, null AS prescribed_qty, pcp.cons_remarks AS item_remarks,"
      + " pp.special_instr, false AS is_package, 'N' AS granular_units, '' AS drug_code,"
      + " null AS route_id, '' AS route_name, null AS item_form_id, '' AS item_form_name,"
      + " null as cons_uom_id, '' AS consumption_uom,"
      + " case when (pcp.preauth_required='' or pcp.preauth_required=' ')"
      + "   then 'N' else pcp.preauth_required end as preauth_required, 'N' AS tooth_num_required,"
      + " '' AS tooth_unv_number, '' AS tooth_fdi_number, '' AS prior_auth_required, '' AS refills,"
      + " 0 AS charge, 0 AS discount, d.status, '' AS time_of_intake, '' AS priority,"
      + " '' AS controlled_drug_number, '' AS category, 0 AS insurance_category_id,"
      + " 0 AS issue_base_unit, '' AS route_of_admin, '' AS item_rate_plan_code,"
      + " null AS prescription_format, 'N' as mandate_clinical_info, '' as clinical_justification,"
      + " pp.item_excluded_from_doctor, pp.item_excluded_from_doctor_remarks,"
      + " true AS is_prescribable" + " FROM patient_prescription pp"
      + " JOIN patient_consultation_prescriptions pcp ON (pp.patient_presc_id=pcp.prescription_id)"
      + " JOIN department d ON (pcp.dept_id = d.dept_id)"
      + " WHERE pp.patient_presc_id IN (:pres_ids) ";

  /**
   * get all prescriptions.
   * 
   * @param medicine the boolean
   * @param test the boolean
   * @param dentalService the boolean
   * @param nonDentalService the boolean
   * @param operation the boolean
   * @param doctor the boolean
   * @param generics the boolean
   * @param presFromStores the boolean
   * @param department the boolean
   * @param visitType the string
   * @param gender the string
   * @param orgId the string
   * @param centerId the integer
   * @param deptId the string
   * @param searchQuery the string
   * @param healthAuthority the string
   * @return list of BasicDynaBean
   */
  public List<BasicDynaBean> getAllPrescriptionItems(Boolean medicine, Boolean test,
      Boolean dentalService, Boolean nonDentalService, Boolean operation, Boolean doctor,
      Boolean generics, Boolean presFromStores, boolean department, String visitType, String gender,
      String orgId, Integer centerId, String deptId, Integer age, String ageIn, Integer planId,
      String tpaId, String searchQuery, String healthAuthority) {

    String query;
    if (presFromStores) {
      if (generics) {
        query = GEN_MED_ITEMS + PRES_ITEMS;
      } else {
        query = MED_STORE_ITEMS + PRES_ITEMS;
        if (visitType.equals("i")) {
          query = query.replace("auto_fill_prescriptions", "auto_fill_indents");
        }
        return DatabaseHelper.queryToDynaList(query, new Object[] {centerId, healthAuthority,
            medicine, searchQuery + "%", "% " + searchQuery + "%", searchQuery + "%", orgId, test,
            searchQuery + "%", "% " + searchQuery + "%", searchQuery + "%", orgId, centerId, tpaId,
            planId, deptId, visitType, gender,
            "P" + age + ageIn, "P" + age + ageIn,
            DateUtil.getCurrentDate(),
            DateUtil.getCurrentDate(), searchQuery + "%", "% " + searchQuery + "%",
            searchQuery + "%", orgId, dentalService, searchQuery + "%", "% " + searchQuery + "%",
            searchQuery + "%", orgId, nonDentalService, searchQuery + "%", "% " + searchQuery + "%",
            searchQuery + "%", orgId, operation, searchQuery + "%", "% " + searchQuery + "%",
            searchQuery + "%", doctor, searchQuery + "%", "% " + searchQuery + "%", centerId,
            deptId, true, searchQuery + "%", "% " + searchQuery + "%", searchQuery + "%", gender,
            visitType, DateUtil.getCurrentDate(), DateUtil.getCurrentDate(), department,
            searchQuery + "%", "% " + searchQuery + "%", PrescriptionsService.ITEMS_LIMIT});
      }
    } else {
      query = PRES_MED_MASTER_ITEMS + PRES_ITEMS;
    }
    return DatabaseHelper.queryToDynaList(query,
        new Object[] {medicine, searchQuery + "%", "% " + searchQuery + "%", orgId, test,
            searchQuery + "%", "% " + searchQuery + "%", searchQuery + "%", orgId, centerId, tpaId,
            planId, deptId, visitType, gender, "P" + age + ageIn, "P" + age + ageIn,
            DateUtil.getCurrentDate(), DateUtil.getCurrentDate(), searchQuery + "%",
            "% " + searchQuery + "%", searchQuery + "%", orgId, dentalService, searchQuery + "%",
            "% " + searchQuery + "%", searchQuery + "%", orgId, nonDentalService, searchQuery + "%",
            "% " + searchQuery + "%", searchQuery + "%", orgId, operation, searchQuery + "%",
            "% " + searchQuery + "%", searchQuery + "%", doctor, searchQuery + "%",
            "% " + searchQuery + "%", centerId, deptId, true, searchQuery + "%",
            "% " + searchQuery + "%", searchQuery + "%", gender, visitType,
            DateUtil.getCurrentDate(), DateUtil.getCurrentDate(), department, searchQuery + "%",
            "% " + searchQuery + "%", PrescriptionsService.ITEMS_LIMIT});
  }

  /**
   * To fetch all medicine prescriptions.
   * 
   * @param medicine indicator for medicine item type
   * @param generics indicator for generic medicine
   * @param presFromStores indicator for store medicines
   * @param searchQuery the string to be looked up
   * @param healthAuthority the health authority
   * @param centerId the center id
   * @param visitType indicator for visit type
   * @return list of BasicDynaBean
   */
  public List<BasicDynaBean> getAllMedicinePrescriptionItems(boolean medicine, boolean generics,
      boolean presFromStores, String searchQuery, String healthAuthority, Integer centerId,
      String visitType) {

    String query;
    if (presFromStores) {
      if (generics) {
        query = GEN_MED_ITEMS + " ORDER BY item_name" + " LIMIT ?";
        return DatabaseHelper.queryToDynaList(query,
            new Object[] {medicine, searchQuery + "%", "% " + searchQuery + "%",
                PrescriptionsService.ITEMS_LIMIT});
      }
      query = MED_STORE_ITEMS + " ORDER BY item_name" + " LIMIT ?";
      if (visitType.equals("i")) {
        query = query.replace("auto_fill_prescriptions", "auto_fill_indents");
      }
      return DatabaseHelper.queryToDynaList(query,
          new Object[] {centerId, healthAuthority, medicine, searchQuery + "%",
              "% " + searchQuery + "%", searchQuery + "%", PrescriptionsService.ITEMS_LIMIT});
    } else {
      query = PRES_MED_MASTER_ITEMS + " ORDER BY item_name" + " LIMIT ?";

      return DatabaseHelper.queryToDynaList(query, new Object[] {medicine, searchQuery + "%",
          searchQuery + "%", PrescriptionsService.ITEMS_LIMIT});
    }
  }

  private static final String GET_DISCHARGE_MEDI_PRESCRIPTIONS = "UNION ALL "
      + "SELECT " // Non-Hospital
      + " pp.presc_type AS item_type, '' as presc_activity_type, non_hosp_medicine,"
      + " pp.patient_presc_id AS item_prescribed_id, '' AS item_id, pop.item_name,"
      + " 0 AS control_type_id, '' AS generic_name,"
      + " '' AS generic_code, 'op' AS master, pp.prescribed_date, pop.admin_strength,"
      + " pop.strength, pop.item_strength, pop.item_strength_units, su.unit_name,"
      + "  case when (pp.visit_id is not null and pop.is_discharge_medication is false) "
      + " then rcm.display_name else  pop.frequency end as frequency,"
      + " pop.duration, pop.duration_units,"
      + " pop.medicine_quantity AS prescribed_qty, pop.item_remarks, pp.special_instr,"
      + " false AS is_package, ifm.granular_units,"
      + " '' AS drug_code, null AS route_id, '' AS route_name,"
      + " pop.item_form_id, ifm.item_form_name," 
      + " pop.cons_uom_id, cum.consumption_uom, '' AS preauth_required,"
      + " 'N' AS tooth_num_required, '' AS tooth_unv_number,"
      + " '' AS tooth_fdi_number, 'P' AS issued, '' AS prior_auth_required,"
      + " '' AS erx_status, 'N' AS send_for_erx,"
      + " '' AS erx_denial_code, '' AS denial_code_type,"
      + " '' AS denial_desc, '' AS erx_denial_remarks,"
      + " '' AS example, '' AS refills, pop.time_of_intake, pop.priority,"
      + " '' AS controlled_drug_number,"
      + " 0 AS insurance_category_id, 0 AS charge, 0 AS discount, '' AS category, "
      + " 0 AS issue_base_unit,  '' AS route_of_admin,"
      + " '' AS item_rate_plan_code, '' AS code_type, "
      + " null AS expiry_date, '' AS prescription_format,"
      + " '' AS reorder, pp.item_excluded_from_doctor, "
      + " pp.item_excluded_from_doctor_remarks, pp.doctor_id, pp.prior_med,"
      + " pp.freq_type, pp.recurrence_daily_id, pp.repeat_interval,"
      + " pp.start_datetime, pp.end_datetime,"
      + " pp.no_of_occurrences, pp.end_on_discontinue,"
      + " pp.discontinued, pp.repeat_interval_units, pp.adm_request_id,"
      + " d.doctor_name, EXISTS (SELECT * FROM patient_activities WHERE"
      + " (activity_status='D' OR order_no is not null) "
      + " AND prescription_id=pp.patient_presc_id) as has_completed_activities,"
      + " rcm.display_name as recurrence_name, "
      + " null as max_doses, null as flow_rate, null as flow_rate_units, "
      + " null as infusion_period, null as infusion_period_units,"
      + " null as iv_administer_instructions, null as medication_type, "
      + " 'N' as mandate_clinical_info, '' as clinical_justification,"
      + " '' as clinical_note_for_conduction, " + " '' as clinical_justification_for_item "
      + "FROM patient_prescription pp "
      + "JOIN patient_other_prescriptions pop ON (pp.patient_presc_id=pop.prescription_id"
      + " AND is_discharge_medication = true) "
      + "LEFT JOIN strength_units su ON (su.unit_id=pop.item_strength_units) "
      + "LEFT JOIN item_form_master ifm ON (pop.item_form_id=ifm.item_form_id) "
      + "LEFT JOIN doctors d ON (pp.doctor_id = d.doctor_id) "
      + "LEFT JOIN recurrence_daily_master rcm on (pp.recurrence_daily_id=rcm.recurrence_daily_id) "
      + "LEFT JOIN consumption_uom_master cum ON (pop.cons_uom_id = cum.cons_uom_id) "
      + "WHERE pp.#filter#=? ";

  /**
   * get all prescriptions.
   * 
   * @param presIds list of prescription ids
   * @param presFromStores the boolean
   * @param bedType bed type
   * @param orgId Rate Plan identifier
   * @param tpaId Sponsor identifier
   * @param centerId Center identifier
   * @param healthAuthority the string
   * @return list of BasicDynaBean
   */
  public List<BasicDynaBean> getPrescriptions(List<Integer> presIds, Boolean presFromStores,
      String bedType, String orgId, String tpaId, Integer centerId, String healthAuthority) {

    String query;
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("bed_type", bedType);
    parameters.addValue("org_id", orgId);
    parameters.addValue("pres_ids", presIds);
    parameters.addValue("tpa_id", tpaId);
    parameters.addValue("center_id", centerId);
    if (presFromStores) {
      parameters.addValue("health_authority", healthAuthority);
      query = STORE_MEDICINE_PRESCRIPTIONS_WITH_IDS + PRESCRIPTIONS_WITH_IDS;
    } else {
      query = OTHER_MEDICINE_PRESCRIPTIONS_WITH_IDS + PRESCRIPTIONS_WITH_IDS;
    }
    return DatabaseHelper.queryToDynaList(query, parameters);
  }

  /**
   * Gets prescriptions.
   * 
   * @param consId the integer
   * @param bedType the string
   * @param orgId the string
   * @return List of prescriptions
   */
  public List<BasicDynaBean> getPrescriptions(Integer consId, String bedType, String orgId,
      String showPriorAuthPresc) {
    String query = showPriorAuthPresc.equals("A") ? GET_ALL_PRESCRIPTIONS_FOR_PRIOR_AUTH
        : GET_PRESCRIPTIONS_FOR_PRIOR_AUTH;

    return DatabaseHelper.queryToDynaList(query,
        new Object[] {bedType, orgId, orgId, consId, bedType, orgId, orgId, consId, bedType, orgId,
            orgId, consId, orgId, bedType, orgId, orgId, orgId, consId});
  }

  /**
   * Gets prescriptions.
   * 
   * @param consId the Object
   * @param bedType the String
   * @param orgId the string
   * @param centerId the integer
   * @param healthAuthority the string
   * @param presFromStores the boolean
   * @return list of BasicDynaBean
   */
  public List<BasicDynaBean> getPrescriptions(Object consId, String bedType, String orgId,
      Integer centerId, String healthAuthority, Boolean presFromStores) {
    if (presFromStores) {
      String query = (GET_STORE_MEDICINE_PRESCRIPTIONS + GET_PRESCRIPTIONS).replace("#filter#",
          (consId instanceof String) ? "visit_id" : "consultation_id");
      return DatabaseHelper.queryToDynaList(query, false, orgId, centerId, healthAuthority, consId,
          bedType, orgId, orgId, consId, orgId, bedType, orgId, consId, bedType, orgId, consId,
          orgId, orgId, bedType, orgId, bedType, orgId, consId, consId, consId);
    } else {
      String query = (GET_OTHER_MEDICINE_PRESCRIPTIONS + GET_PRESCRIPTIONS).replace("#filter#",
          (consId instanceof String) ? "visit_id" : "consultation_id");
      Object[] temp = new Object[] {false, consId, bedType, orgId, orgId, consId, orgId, bedType,
          orgId, consId, bedType, orgId, consId, orgId, orgId, bedType, orgId, bedType, orgId,
          consId, consId, consId};
      return DatabaseHelper.queryToDynaList(query, temp);
    }
  }

  /**
   * Gets list of discharge medication bean.
   * 
   * @param consId the visit id
   * @param bedType the bed type
   * @param orgId the org id
   * @param centerId the center id
   * @param healthAuthority the health authority
   * @param presFromStores indicator for store prescription
   * @return list of BasicDynaBean
   */
  public List<BasicDynaBean> getDischargeMedications(Object consId, String bedType, String orgId,
      Integer centerId, String healthAuthority, Boolean presFromStores) {
    if (presFromStores) {
      String query = (GET_STORE_MEDICINE_PRESCRIPTIONS + GET_DISCHARGE_MEDI_PRESCRIPTIONS)
          .replace("#filter#", (consId instanceof String) ? "visit_id" : "consultation_id");
      return DatabaseHelper.queryToDynaList(query, true, orgId, centerId, healthAuthority, consId,
          consId);
    } else {
      String query = (GET_OTHER_MEDICINE_PRESCRIPTIONS + GET_DISCHARGE_MEDI_PRESCRIPTIONS)
          .replace("#filter#", (consId instanceof String) ? "visit_id" : "consultation_id");
      Object[] temp = new Object[] {true, consId, consId};
      return DatabaseHelper.queryToDynaList(query, temp);
    }
  }

  private static final String STORE_MEDICINE_PRESCRIPTIONS_WITH_CONS_ID = "SELECT"
      + " 'Medicine' AS item_type," + " '' as presc_activity_type, false AS non_hosp_medicine,"
      + " COALESCE(sid.medicine_id::text, g.generic_code) AS item_id,"
      + " COALESCE(sid.medicine_name, g.generic_name) AS item_name, g.generic_name, g.generic_code,"
      + " 'item_master' AS master, pmp.admin_strength, pmp.strength, pmp.item_strength,"
      + " pmp.item_strength_units, su.unit_name, pmp.frequency, pmp.duration, pmp.duration_units,"
      + " pmp.medicine_quantity AS prescribed_qty, pmp.medicine_remarks AS item_remarks,"
      + " pp.special_instr, false AS is_package, ifm.granular_units, sic.item_code AS drug_code,"
      + " mr.route_id, mr.route_name, pmp.item_form_id, ifm.item_form_name, pmp.cons_uom_id,"
      + " cum.consumption_uom,"
      + " '' AS preauth_required, 'N' AS tooth_num_required, '' AS tooth_unv_number,"
      + " '' AS tooth_fdi_number, sid.prior_auth_required, pmp.refills AS refills,"
      + " COALESCE((case when textregexeq(sir.selling_price_expr, '^[0-9]+\\.?[0-9]*$') then"
      + "   sir.selling_price_expr::decimal else null end), ssd.mrp, sid.item_selling_price, 0)"
      + "   AS charge, (COALESCE((case when textregexeq(sir.selling_price_expr,"
      + "   '^[0-9]+\\.?[0-9]*$') then sir.selling_price_expr::decimal else null end), ssd.mrp,"
      + "   sid.item_selling_price, 0) * COALESCE(mc.discount, 0) /100) AS discount, sid.status,"
      + " pmp.time_of_intake, pmp.priority, pmp.controlled_drug_number, '' AS category,"
      + " COALESCE(sid.insurance_category_id, 0) AS insurance_category_id,"
      + " sid.issue_base_unit::integer AS issue_base_unit, pp.patient_presc_id AS id,"
      + " sid.route_of_admin, '' AS item_rate_plan_code, pmp.prescription_format,"
      + " pmp.expiry_date, pp.start_datetime, pp.end_datetime, 'N' as mandate_clinical_info,"
      + " '' as clinical_justification, pp.item_excluded_from_doctor,"
      + " pp.item_excluded_from_doctor_remarks, true AS is_prescribable"
      + " FROM patient_prescription pp"
      + " JOIN patient_medicine_prescriptions pmp ON (pp.patient_presc_id=pmp.op_medicine_pres_id)"
      + " LEFT JOIN consumption_uom_master cum ON (cum.cons_uom_id=pmp.cons_uom_id)"
      + " LEFT JOIN store_item_details sid ON (pmp.medicine_id=sid.medicine_id)"
      + " LEFT JOIN generic_name g ON (pmp.generic_code = g.generic_code)"
      + " LEFT JOIN strength_units su ON (pmp.item_strength_units=su.unit_id)"
      + " LEFT JOIN item_form_master ifm ON (pmp.item_form_id=ifm.item_form_id)"
      + " LEFT JOIN medicine_route mr ON (mr.route_id=pmp.route_of_admin)"
      + " JOIN organization_details od ON (od.org_id=?)"
      + " LEFT JOIN store_item_rates sir ON (sir.medicine_id=sid.medicine_id"
      + "   AND sir.store_rate_plan_id=od.store_rate_plan_id)"
      + " LEFT JOIN LATERAL (select max(sibd.mrp) AS mrp from store_stock_details ssd,"
      + "   stores s, store_item_batch_details sibd where s.dept_id=ssd.dept_id"
      + "   and auto_fill_prescriptions and ssd.medicine_id = sid.medicine_id"
      + "   AND ssd.item_batch_id=sibd.item_batch_id AND s.center_id=? ) AS ssd ON true"
      + " LEFT JOIN store_category_master mc ON (mc.category_id = sid.med_category_id)"
      + " LEFT JOIN ha_item_code_type hict ON (hict.medicine_id = sid.medicine_id"
      + "   AND hict.health_authority= ?)"
      + " LEFT JOIN store_item_codes sic ON (sic.medicine_id = hict.medicine_id"
      + "   AND sic.code_type = hict.code_type)" + " WHERE pp.#filter#=? ";

  private static final String OTHER_MEDICINE_PRESCRIPTIONS_WITH_CONS_ID = "SELECT"
      + " 'Medicine' AS item_type,"
      + " '' as presc_activity_type, false AS non_hosp_medicine, '' AS item_id,"
      + " pomp.medicine_name AS item_name, g.generic_name, g.generic_code, 'op' AS master,"
      + " pomp.admin_strength, pomp.strength, pomp.item_strength, pomp.item_strength_units,"
      + " su.unit_name, pomp.frequency, pomp.duration, pomp.duration_units,"
      + " pomp.medicine_quantity AS prescribed_qty, pomp.medicine_remarks AS item_remarks,"
      + " pp.special_instr, false AS is_package, ifm.granular_units, '' AS drug_code, mr.route_id,"
      + " mr.route_name, coalesce(pomp.item_form_id, null) as item_form_id, ifm.item_form_name,"
      + " pomp.cons_uom_id, cum.consumption_uom, '' AS preauth_required, 'N' AS tooth_num_required,"
      + " '' AS tooth_unv_number, '' AS tooth_fdi_number, '' AS prior_auth_required, pomp.refills,"
      + " 0 AS charge, 0 AS discount, pms.status, pomp.time_of_intake, pomp.priority,"
      + " '' AS controlled_drug_number, '' AS category, 0 AS insurance_category_id,"
      + " 0 AS issue_base_unit, pp.patient_presc_id AS id, pms.route_of_admin,"
      + " '' AS item_rate_plan_code, pomp.prescription_format, pomp.expiry_date, pp.start_datetime,"
      + " pp.end_datetime, 'N' as mandate_clinical_info, '' as clinical_justification,"
      + " pp.item_excluded_from_doctor, pp.item_excluded_from_doctor_remarks,"
      + " true AS is_prescribable" + " FROM patient_prescription pp"
      + " JOIN patient_other_medicine_prescriptions pomp"
      + "   ON (pp.patient_presc_id=pomp.prescription_id)"
      + " LEFT JOIN item_form_master ifm ON (pomp.item_form_id = ifm.item_form_id)"
      + " LEFT JOIN strength_units su ON (su.unit_id=pomp.item_strength_units)"
      + " JOIN prescribed_medicines_master pms ON (pomp.medicine_name=pms.medicine_name)"
      + " LEFT JOIN generic_name g ON (pms.generic_name=g.generic_code)"
      + " LEFT JOIN medicine_route mr ON (mr.route_id=pomp.route_of_admin)"
      + " LEFT JOIN consumption_uom_master cum ON (cum.cons_uom_id = pomp.cons_uom_id)"
      + " WHERE pp.#filter#=? ";

  private static final String DISCHARGE_PRESCRIPTIONS_WITH_CONS_ID = " UNION ALL "
      + "SELECT"
      + " CASE WHEN non_hosp_medicine THEN 'Medicine' ELSE 'NonHospital' END AS item_type,"
      + " '' as presc_activity_type, non_hosp_medicine, '' AS item_id, pop.item_name,"
      + " '' AS generic_name, '' AS generic_code,"
      + " 'op' AS master, pop.admin_strength, pop.strength, pop.item_strength,"
      + " pop.item_strength_units, su.unit_name, pop.frequency,  pop.duration, pop.duration_units,"
      + " pop.medicine_quantity AS prescribed_qty, pop.item_remarks,"
      + " pp.special_instr, false AS is_package,"
      + " ifm.granular_units, '' AS drug_code, null AS route_id,"
      + " '' AS route_name, pop.item_form_id, "
      + " ifm.item_form_name, pop.cons_uom_id, cum.consumption_uom, '' AS preauth_required,"
      + " 'N' AS tooth_num_required,"
      + " '' AS tooth_unv_number, '' AS tooth_fdi_number, '' AS prior_auth_required,"
      + " '' AS refills, 0 AS charge, 0 AS discount, 'A' AS status, pop.time_of_intake, "
      + " pop.priority, '' AS controlled_drug_number, '' AS category, "
      + " 0 AS insurance_category_id, 0 AS issue_base_unit,"
      + " pp.patient_presc_id AS id, '' AS route_of_admin, "
      + " '' AS item_rate_plan_code, null AS prescription_format, null AS expiry_date, "
      + " pp.start_datetime, pp.end_datetime, 'N' as mandate_clinical_info,"
      + " '' as clinical_justification,  "
      + " pp.item_excluded_from_doctor, pp.item_excluded_from_doctor_remarks,"
      + " true AS is_prescribable "
      + "FROM patient_prescription pp "
      + "JOIN patient_other_prescriptions pop ON (pp.patient_presc_id=pop.prescription_id) "
      + "LEFT JOIN strength_units su ON (su.unit_id=pop.item_strength_units) "
      + "LEFT JOIN item_form_master ifm ON (pop.item_form_id=ifm.item_form_id) "
      + "LEFT JOIN consumption_uom_master cum ON (pop.cons_uom_id = cum.cons_uom_id) "
      + "WHERE pp.#filter#=? AND non_hosp_medicine=true ";

  /**
   * Gets list of discharge medication bean.
   * 
   * @param consId the visit id.
   * @param presFromStores indicator for store medicine prescriptions
   * @param orgId the org id
   * @param centerId the center id
   * @param healthAuthority the health authority
   * @return list of discharge medication bean
   */
  public List<BasicDynaBean> getDischargeMedicationWithCharges(
      Object consId,
      Boolean presFromStores, String orgId, Integer centerId, String healthAuthority) {

    String query;
    if (presFromStores) {
      query = (STORE_MEDICINE_PRESCRIPTIONS_WITH_CONS_ID + DISCHARGE_PRESCRIPTIONS_WITH_CONS_ID)
          .replace(
              "#filter#",
          (consId instanceof String) ? "visit_id" : "consultation_id");;
      return DatabaseHelper.queryToDynaList(query,
          new Object[] {orgId, centerId, healthAuthority, consId, consId});
    } else {
      query = (OTHER_MEDICINE_PRESCRIPTIONS_WITH_CONS_ID + DISCHARGE_PRESCRIPTIONS_WITH_CONS_ID)
          .replace("#filter#", (consId instanceof String) ? "visit_id" : "consultation_id");;
      return DatabaseHelper.queryToDynaList(query, new Object[] {consId, consId});
    }
  }

  public static final String GET_ERX_PBM_ID = " SELECT pbm_presc_id "
      + " FROM patient_prescription pp "
      + " JOIN patient_medicine_prescriptions pmp"
      + " ON (pp.patient_presc_id=pmp.op_medicine_pres_id) "
      + " WHERE #filter# = ? LIMIT 1";

  /**
   * Gets pbm prescription id.
   * 
   * @param consId the visit id
   * @return the pbm id
   */
  public Integer getErxConsPBMId(Object consId) {
    String query = GET_ERX_PBM_ID.replace("#filter#",
        (consId instanceof String)
            ? "is_discharge_medication=true AND pp.visit_id"
            : "consultation_id");
    return DatabaseHelper.getInteger(query, new Object[] {consId});
  }

  private static final String PRESCRIPTIONS_WITH_CONS_ID = " UNION ALL"
      + " SELECT 'Service' AS item_type, '' as presc_activity_type, false non_hosp_medicine,"
      + " s.service_id AS item_id, s.service_name AS item_name, '' AS generic_name,"
      + " '' AS generic_code, 'item_master' AS master, psp.admin_strength, '' AS strength,"
      + " '' AS item_strength, null AS item_strength_units, '' AS unit_name, '' AS frequency,"
      + " null AS duration, '' AS duration_units, psp.qty AS prescribed_qty,"
      + " psp.service_remarks AS item_remarks, pp.special_instr AS special_instr,"
      + " false AS is_package, 'N' AS granular_units, '' AS drug_code, null AS route_id,"
      + " '' AS route_name, null AS item_form_id, '' AS item_form_name, null as cons_uom_id,"
      + " '' AS consumption_uom,"
      + " case when (psp.preauth_required='' or psp.preauth_required=' ') then 'N'"
      + "   else psp.preauth_required end as preauth_required, s.tooth_num_required,"
      + " psp.tooth_unv_number, psp.tooth_fdi_number, s.prior_auth_required, '' AS refills,"
      + " smc.unit_charge AS charge, smc.discount, s.status, '' AS time_of_intake, psp.priority,"
      + " '' AS controlled_drug_number, '' AS category, s.insurance_category_id,"
      + " 0 AS issue_base_unit, pp.patient_presc_id AS id, '' AS route_of_admin,"
      + " sod.item_code AS item_rate_plan_code, null AS prescription_format, null AS expiry_date,"
      + " pp.start_datetime, pp.end_datetime, 'N' as mandate_clinical_info,"
      + " '' as clinical_justification, pp.item_excluded_from_doctor,"
      + " pp.item_excluded_from_doctor_remarks, true AS is_prescribable"
      + " FROM patient_prescription pp"
      + " JOIN patient_service_prescriptions psp ON (pp.patient_presc_id=psp.op_service_pres_id)"
      + " JOIN services s ON (s.service_id = psp.service_id)"
      + " JOIN service_master_charges smc ON (s.service_id=smc.service_id AND bed_type=?"
      + "   AND smc.org_id=?)"
      + " JOIN service_org_details sod ON (sod.service_id = smc.service_id AND sod.org_id = ?)"
      + " WHERE pp.consultation_id=?" + " UNION ALL"
      + " SELECT 'Operation' AS item_type, '' as presc_activity_type, false AS non_hosp_medicine,"
      + " om.op_id AS item_id, om.operation_name AS item_name, '' AS generic_name,"
      + " '' AS generic_code, 'item_master' AS master, pop.admin_strength, '' AS strength,"
      + " '' AS item_strength, null AS item_strength_units, '' AS unit_name, '' AS frequency,"
      + " null AS duration, '' AS duration_units, null AS prescribed_qty,"
      + " pop.remarks AS item_remarks, pp.special_instr, false AS is_package,"
      + " 'N' AS granular_units, '' AS drug_code, null AS route_id, '' AS route_name,"
      + " null AS item_form_id, '' AS item_form_name, null as cons_uom_id, '' AS consumption_uom,"
      + " case when (pop.preauth_required='' or pop.preauth_required=' ') then 'N'"
      + "   else pop.preauth_required end as preauth_required, 'N' AS tooth_num_required,"
      + " '' AS tooth_unv_number, '' AS tooth_fdi_number, om.prior_auth_required, '' AS refills,"
      + " oc.surg_asstance_charge AS charge, oc.surg_asst_discount AS discount, om.status,"
      + " '' AS time_of_intake, '' AS priority, '' AS controlled_drug_number, '' AS category,"
      + " om.insurance_category_id, 0 AS issue_base_unit, pp.patient_presc_id AS id,"
      + " '' AS route_of_admin, ood.item_code AS item_rate_plan_code, null AS prescription_format,"
      + " null AS expiry_date, pp.start_datetime, pp.end_datetime, 'N' as mandate_clinical_info,"
      + " '' as clinical_justification, pp.item_excluded_from_doctor,"
      + " pp.item_excluded_from_doctor_remarks, true AS is_prescribable"
      + " FROM patient_prescription pp"
      + " JOIN patient_operation_prescriptions pop ON (pp.patient_presc_id=pop.prescription_id)"
      + " JOIN operation_master om ON (pop.operation_id=om.op_id)"
      + " JOIN operation_org_details ood ON (ood.operation_id=om.op_id AND ood.org_id=?)"
      + " JOIN operation_charges oc ON (om.op_id=oc.op_id AND bed_type=? AND oc.org_id=?)"
      + " WHERE pp.consultation_id=?" + " UNION ALL"
      + " SELECT 'Doctor' AS item_type, pcp.presc_activity_type, false AS non_hosp_medicine,"
      + " d.doctor_id AS item_id, d.doctor_name AS item_name, '' AS generic_name,"
      + " '' AS generic_code, 'item_master' AS master, pcp.admin_strength, '' AS strength,"
      + " '' AS item_strength, null AS item_strength_units, '' AS unit_name, '' AS frequency,"
      + " null AS duration, '' AS duration_units, null AS prescribed_qty,"
      + " pcp.cons_remarks AS item_remarks, pp.special_instr, false AS is_package,"
      + " 'N' AS granular_units, '' AS drug_code, null AS route_id, '' AS route_name,"
      + " null AS item_form_id, '' AS item_form_name, null as cons_uom_id, '' AS consumption_uom,"
      + " case when (pcp.preauth_required='' or pcp.preauth_required=' ') then 'N'"
      + "   else pcp.preauth_required end as preauth_required, 'N' AS tooth_num_required,"
      + " '' AS tooth_unv_number, '' AS tooth_fdi_number, '' AS prior_auth_required, '' AS refills,"
      + " cc.charge + docc.op_charge AS charge, cc.discount + docc.op_charge_discount AS discount,"
      + " d.status, '' AS time_of_intake, '' AS priority, '' AS controlled_drug_number,"
      + " '' AS category, ct.insurance_category_id, 0 AS issue_base_unit,"
      + " pp.patient_presc_id AS id, '' AS route_of_admin, '' AS item_rate_plan_code,"
      + " null AS prescription_format, null AS expiry_date, pp.start_datetime, pp.end_datetime,"
      + " 'N' as mandate_clinical_info, '' as clinical_justification, pp.item_excluded_from_doctor,"
      + " pp.item_excluded_from_doctor_remarks, true AS is_prescribable"
      + " FROM patient_prescription pp"
      + " JOIN patient_consultation_prescriptions pcp ON (pp.patient_presc_id=pcp.prescription_id)"
      + " JOIN doctors d ON (pcp.doctor_id = d.doctor_id)"
      + " JOIN consultation_charges cc ON (cc.bed_type =? AND cc.org_id=?"
      + "   AND cc.consultation_type_id = -1)"
      + " JOIN doctor_op_consultation_charge docc ON (docc.doctor_id=d.doctor_id"
      + "   AND docc.org_id=cc.org_id)"
      + " JOIN consultation_types ct ON (ct.consultation_type_id=-1)"
      + " WHERE pp.consultation_id=?" + " UNION ALL"
      + " SELECT 'Inv.' AS item_type, '' as presc_activity_type, false AS non_hosp_medicine,"
      + " coalesce(d.test_id, pm.package_id::text) AS item_id,"
      + " coalesce(d.test_name, pm.package_name) AS item_name, '' AS generic_name,"
      + " '' AS generic_code, 'item_master' AS master, ptp.admin_strength, '' AS strength,"
      + " '' AS item_strength, null AS item_strength_units, '' AS unit_name, '' AS frequency,"
      + " null AS duration, '' AS duration_units, null AS prescribed_qty,"
      + " ptp.test_remarks AS item_remarks, pp.special_instr, ptp.ispackage AS is_package,"
      + " 'N' AS granular_units, '' AS drug_code, null AS route_id, '' AS route_name,"
      + " null AS item_form_id, '' AS item_form_name, null as cons_uom_id, '' AS consumption_uom,"
      + " case when (ptp.preauth_required='' or ptp.preauth_required=' ') then 'N'"
      + "   else ptp.preauth_required end as preauth_required, 'N' AS tooth_num_required,"
      + " '' AS tooth_unv_number, '' AS tooth_fdi_number,"
      + " coalesce(d.prior_auth_required, pm.prior_auth_required), '' AS refills,"
      + " coalesce(dc.charge, pc.charge, 0) AS charge,"
      + " coalesce(dc.discount, pc.discount, 0) AS discount,"
      + " coalesce(d.status, CASE WHEN EXISTS (SELECT * FROM center_package_applicability pcm"
      + "   JOIN package_sponsor_master psm ON (psm.pack_id=pm.package_id AND psm.status='A'"
      + "   AND (psm.tpa_id=? OR psm.tpa_id = '-1'))"
      + "   WHERE pcm.package_id=pm.package_id AND pcm.status='A'"
      + "   AND (pcm.center_id=? or pcm.center_id=-1)) AND pm.status='A' THEN 'A'"
      + "   ELSE 'I' END) AS status, '' AS time_of_intake, ptp.priority,"
      + " '' AS controlled_drug_number, coalesce(dd.category, '') AS category,"
      + " coalesce(d.insurance_category_id, pm.insurance_category_id) AS insurance_category_id,"
      + " 0 AS issue_base_unit, pp.patient_presc_id AS id, '' AS route_of_admin,"
      + " coalesce(tod.item_code, pod.item_code) AS item_rate_plan_code,"
      + " null AS prescription_format, null AS expiry_date, pp.start_datetime, pp.end_datetime,"
      + " d.mandate_clinical_info, d.clinical_justification, pp.item_excluded_from_doctor,"
      + " pp.item_excluded_from_doctor_remarks, (CASE WHEN ptp.ispackage THEN true"
      + "   ELSE d.is_prescribable END) AS is_prescribable" + " FROM patient_prescription pp"
      + " JOIN patient_test_prescriptions ptp ON (pp.patient_presc_id=ptp.op_test_pres_id)"
      + " LEFT JOIN diagnostics d ON (d.test_id=ptp.test_id)"
      + " LEFT JOIN test_org_details tod ON (tod.test_id=d.test_id AND tod.org_id=?)"
      + " LEFT JOIN diagnostics_departments dd ON (dd.ddept_id=d.ddept_id)"
      + " LEFT JOIN diagnostic_charges dc ON (d.test_id=dc.test_id AND dc.bed_type=?"
      + "   AND dc.org_name=?)" + " LEFT JOIN packages pm ON (pm.package_id::text=ptp.test_id)"
      + " LEFT JOIN pack_org_details pod ON (pod.package_id=pm.package_id AND pod.org_id=?)"
      + " LEFT JOIN package_charges pc ON (pm.package_id=pc.package_id AND pc.bed_type=?"
      + "   AND pc.org_id=?)" + " WHERE pp.consultation_id=?" + " UNION ALL"
      + " SELECT CASE WHEN non_hosp_medicine THEN 'Medicine' ELSE 'NonHospital' END AS item_type,"
      + " '' as presc_activity_type, non_hosp_medicine, '' AS item_id, pop.item_name,"
      + " '' AS generic_name, '' AS generic_code, 'op' AS master, pop.admin_strength, pop.strength,"
      + " pop.item_strength, pop.item_strength_units, su.unit_name, pop.frequency, pop.duration,"
      + " pop.duration_units, pop.medicine_quantity AS prescribed_qty, pop.item_remarks,"
      + " pp.special_instr, false AS is_package, ifm.granular_units, '' AS drug_code,"
      + " null AS route_id, '' AS route_name, pop.item_form_id, ifm.item_form_name,"
      + " pop.cons_uom_id, cum.consumption_uom, '' AS preauth_required,"
      + " 'N' AS tooth_num_required,"
      + " '' AS tooth_unv_number, '' AS tooth_fdi_number, '' AS prior_auth_required, '' AS refills,"
      + " 0 AS charge, 0 AS discount, 'A' AS status, pop.time_of_intake, pop.priority,"
      + " '' AS controlled_drug_number, '' AS category, 0 AS insurance_category_id,"
      + " 0 AS issue_base_unit, pp.patient_presc_id AS id, '' AS route_of_admin,"
      + " '' AS item_rate_plan_code, null AS prescription_format, null AS expiry_date,"
      + " pp.start_datetime, pp.end_datetime, 'N' as mandate_clinical_info,"
      + " '' as clinical_justification, pp.item_excluded_from_doctor,"
      + " pp.item_excluded_from_doctor_remarks, true AS is_prescribable"
      + " FROM patient_prescription pp"
      + " JOIN patient_other_prescriptions pop ON (pp.patient_presc_id=pop.prescription_id)"
      + " LEFT JOIN strength_units su ON (su.unit_id=pop.item_strength_units)"
      + " LEFT JOIN item_form_master ifm ON (pop.item_form_id=ifm.item_form_id)"
      + " LEFT JOIN consumption_uom_master cum ON (pop.cons_uom_id = cum.cons_uom_id)"
      + " WHERE pp.consultation_id=?" + " UNION ALL"
      + " SELECT 'Doctor' AS item_type, pcp.presc_activity_type, false AS non_hosp_medicine,"
      + " d.dept_id AS item_id, d.dept_name AS item_name, '' AS generic_name, '' AS generic_code,"
      + " 'item_master' AS master, pcp.admin_strength, '' AS strength, '' AS item_strength,"
      + " null AS item_strength_units, '' AS unit_name, '' AS frequency, null AS duration,"
      + " '' AS duration_units, null AS prescribed_qty, pcp.cons_remarks AS item_remarks,"
      + " pp.special_instr, false AS is_package, 'N' AS granular_units, '' AS drug_code,"
      + " null AS route_id, '' AS route_name, null AS item_form_id, '' AS item_form_name,"
      + " null as cons_uom_id, '' AS consumption_uom,"
      + " case when (pcp.preauth_required='' or pcp.preauth_required=' ')"
      + "   then 'N' else pcp.preauth_required end as preauth_required, 'N' AS tooth_num_required,"
      + " '' AS tooth_unv_number, '' AS tooth_fdi_number, '' AS prior_auth_required, '' AS refills,"
      + " 0 AS charge, 0 AS discount, d.status, '' AS time_of_intake, '' AS priority,"
      + " '' AS controlled_drug_number, '' AS category, 0 insurance_category_id,"
      + " 0 AS issue_base_unit, pp.patient_presc_id AS id, '' AS route_of_admin,"
      + " '' AS item_rate_plan_code, null AS prescription_format, null AS expiry_date,"
      + " pp.start_datetime, pp.end_datetime, 'N' as mandate_clinical_info,"
      + " '' as clinical_justification, pp.item_excluded_from_doctor,"
      + " pp.item_excluded_from_doctor_remarks, true AS is_prescribable"
      + " FROM patient_prescription pp"
      + " JOIN patient_consultation_prescriptions pcp ON (pp.patient_presc_id=pcp.prescription_id)"
      + " JOIN department d ON (pcp.dept_id = d.dept_id)" + " WHERE pp.consultation_id=?"
      + " ORDER BY id ";

  /**
   * Gets list of prescription with charges.
   * 
   * @param consId the integer
   * @param presFromStores the boolean
   * @param bedType the string
   * @param orgId the string
   * @param tpaId the string
   * @param centerId the integer
   * @param healthAuthority the string
   * @return list of BasicDynaBean
   */
  public List<BasicDynaBean> getPrescriptionsWithCharges(Integer consId, Boolean presFromStores,
      String bedType, String orgId, String tpaId, Integer centerId, String healthAuthority) {

    String query;
    if (presFromStores) {
      query = (STORE_MEDICINE_PRESCRIPTIONS_WITH_CONS_ID + PRESCRIPTIONS_WITH_CONS_ID)
          .replace("#filter#", "consultation_id");
      return DatabaseHelper.queryToDynaList(query,
          new Object[] {orgId, centerId, healthAuthority, consId, bedType, orgId, orgId, consId,
              orgId, bedType, orgId, consId, bedType, orgId, consId, tpaId, centerId, orgId,
              bedType, orgId, orgId, bedType, orgId, consId, consId, consId});
    } else {
      query = (OTHER_MEDICINE_PRESCRIPTIONS_WITH_CONS_ID + PRESCRIPTIONS_WITH_CONS_ID)
          .replace("#filter#", "consultation_id");
      return DatabaseHelper.queryToDynaList(query,
          new Object[] {consId, bedType, orgId, orgId, consId, orgId, bedType, orgId, consId,
              bedType, orgId, consId, tpaId, centerId, orgId, bedType, orgId, orgId, bedType, orgId,
              consId, consId, consId});
    }
  }

  private static final String DISCHARGE_MEDI_PRESCRIPTIONS_WITH_IDS = "UNION ALL "
      + "SELECT"
      + " CASE WHEN non_hosp_medicine THEN 'Medicine' ELSE 'NonHospital' END AS item_type,"
      + " '' AS presc_activity_type, non_hosp_medicine, '' AS item_id, pop.item_name,"
      + " '' AS generic_name, '' AS generic_code,"
      + " 'op' AS master, pop.admin_strength, pop.strength, pop.item_strength,"
      + " pop.item_strength_units, su.unit_name, pop.frequency,  pop.duration, pop.duration_units,"
      + " pop.medicine_quantity AS prescribed_qty, pop.item_remarks,"
      + " pp.special_instr, false AS is_package,"
      + " ifm.granular_units, '' AS drug_code, null AS route_id,"
      + " '' AS route_name, pop.item_form_id, "
      + " ifm.item_form_name, pop.cons_uom_id, cum.consumption_uom, '' AS preauth_required,"
      + " 'N' AS tooth_num_required," + " '' AS tooth_unv_number, '' AS tooth_fdi_number,"
      + " '' AS prior_auth_required, '' AS refills,"
      + " 0 AS charge, 0 AS discount, 'A' AS status, pop.time_of_intake, "
      + " pop.priority, '' AS controlled_drug_number, '' AS category, "
      + " 0 AS insurance_category_id, 0 AS issue_base_unit,"
      + " '' AS route_of_admin, '' AS item_rate_plan_code, "
      + " null AS prescription_format, 'N' as mandate_clinical_info, '' as clinical_justification,"
      + " pp.item_excluded_from_doctor, pp.item_excluded_from_doctor_remarks,"
      + " true AS is_prescribable FROM patient_prescription pp "
      + "JOIN patient_other_prescriptions pop ON (pp.patient_presc_id=pop.prescription_id) "
      + "LEFT JOIN strength_units su ON (su.unit_id=pop.item_strength_units) "
      + "LEFT JOIN item_form_master ifm ON (pop.item_form_id=ifm.item_form_id) "
      + "LEFT JOIN consumption_uom_master cum ON (pop.cons_uom_id = cum.cons_uom_id) "
      + "WHERE pp.patient_presc_id IN (:pres_ids) ";

  private static final String GET_ERX_ACTIVITIES = " SELECT sid.medicine_name, pmp.medicine_id,"
      + " medicine_quantity, pmp.op_medicine_pres_id, pmp.op_medicine_pres_id::text as activity_id,"
      + " medicine_remarks, frequency, strength, duration, duration_units, pp.status as issued,"
      + " sid.cons_uom_id, cum.consumption_uom, g.generic_name, pmp.generic_code, mod_time,"
      + " mr.route_id,"
      + " mr.route_name, mr.route_code, icm.category AS category_name, mm.manf_name,"
      + " mm.manf_mnemonic, sid.prior_auth_required, coalesce(pmp.item_form_id, 0) as item_form_id,"
      + " pmp.item_strength, if.item_form_name, pmp.item_strength_units, su.unit_name,"
      + " to_char(pp.prescribed_date::timestamp, 'dd/MM/yyyy hh24:mi') AS activity_prescribed_date,"
      + " pp.prescribed_date , msct.haad_code, sic.item_code, sic.code_type, pmp.pbm_presc_id,"
      + " pmp.erx_status, pmp.erx_denial_code, pmp.erx_denial_remarks,"
      + " idc.status AS denial_code_status, idc.type AS denial_code_type, if.granular_units"
      + " FROM patient_prescription pp"
      + " JOIN patient_medicine_prescriptions pmp ON (pp.patient_presc_id=pmp.op_medicine_pres_id)"
      + " JOIN pbm_prescription pbmp ON (pbmp.pbm_presc_id = pmp.pbm_presc_id)"
      + " LEFT JOIN store_item_details sid ON (pmp.medicine_id=sid.medicine_id)"
      + " LEFT JOIN ha_item_code_type hict ON (hict.medicine_id = sid.medicine_id"
      + "   AND hict.health_authority=?)"
      + " LEFT JOIN store_item_codes sic ON (sic.medicine_id = sid.medicine_id"
      + "   AND sic.code_type = hict.code_type)"
      + " LEFT JOIN item_form_master if ON (pmp.item_form_id=if.item_form_id)"
      + " LEFT OUTER JOIN generic_name g ON (pmp.generic_code = g.generic_code)"
      + " LEFT OUTER JOIN medicine_route mr ON (mr.route_id=pmp.route_of_admin)"
      + " LEFT OUTER JOIN strength_units su ON (pmp.item_strength_units=su.unit_id)"
      + " LEFT OUTER JOIN manf_master mm on mm.manf_code=sid.manf_name"
      + " LEFT OUTER JOIN store_category_master icm on sid.med_category_id=icm.category_id"
      + " LEFT JOIN mrd_supported_code_types msct ON (msct.code_type = sic.code_type)"
      + " LEFT JOIN insurance_denial_codes idc ON (idc.denial_code = pmp.erx_denial_code)"
      + " LEFT JOIN consumption_uom_master cum ON (sid.cons_uom_id = cum.cons_uom_id)"
      + " WHERE pbmp.pbm_presc_id=? AND send_for_erx='Y'" + " ORDER BY pmp.op_medicine_pres_id ";

  /**
   * Gets discharge medicine prescriptions.
   * 
   * @param presIds list of prescription Ids
   * @param presFromStores indicator for store prescriptions
   * @param orgId the org id
   * @param centerId the center id
   * @param healthAuthority the health authority
   * @return list of discharge medication bean
   */
  public List<BasicDynaBean> getDischargeMediPrescriptions(List<Integer> presIds,
      Boolean presFromStores, String orgId, Integer centerId, String healthAuthority) {

    String query;
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("org_id", orgId);
    parameters.addValue("pres_ids", presIds);
    parameters.addValue("center_id", centerId);
    if (presFromStores) {
      parameters.addValue("health_authority", healthAuthority);
      query = STORE_MEDICINE_PRESCRIPTIONS_WITH_IDS + DISCHARGE_MEDI_PRESCRIPTIONS_WITH_IDS;
    } else {
      query = OTHER_MEDICINE_PRESCRIPTIONS_WITH_IDS + DISCHARGE_MEDI_PRESCRIPTIONS_WITH_IDS;
    }
    return DatabaseHelper.queryToDynaList(query, parameters);
  }

  public List<BasicDynaBean> getErxPrescribedActivities(int pbmPrescId, String healthAuthority) {
    return DatabaseHelper.queryToDynaList(GET_ERX_ACTIVITIES,
        new Object[] {healthAuthority, pbmPrescId});
  }

  private static final String RECENT_PRESCRIPTIONS = "SELECT patient_presc_id"
      + " FROM patient_prescription" + " WHERE consultation_id in (" + "   SELECT consultation_id"
      + "   FROM doctor_consultation" + "   WHERE doctor_name = ?" + "   ORDER BY visited_date DESC"
      + "   LIMIT 10)" + " ORDER BY patient_presc_id DESC" + " LIMIT 3";

  public List<BasicDynaBean> getRecentPrescriptionIds(String doctorId) {
    return DatabaseHelper.queryToDynaList(RECENT_PRESCRIPTIONS, new Object[] {doctorId});
  }

  private static final String GET_PRESCRIPTIONS_FOR_PRIOR_AUTH = "SELECT 'Inv.' AS item_type,"
      + " pp.patient_presc_id, d.test_name AS item_name, d.test_id AS item_id, tod.item_code,"
      + " d.ddept_id AS dept_id, dc.charge, dd.category, dc.discount, tod.code_type,"
      + " d.service_sub_group_id, d.insurance_category_id, d.allow_rate_increase,"
      + " d.allow_rate_decrease, 1 AS item_qty, '' AS tooth_unv_number, '' AS tooth_fdi_number,"
      + " ptp.test_remarks AS item_remarks, ptp.preauth_required, pp.status"
      + " FROM patient_prescription pp"
      + " JOIN patient_test_prescriptions ptp ON (ptp.ispackage=false"
      + "   AND pp.patient_presc_id=ptp.op_test_pres_id)"
      + " JOIN diagnostics d ON (d.test_id=ptp.test_id)"
      + " JOIN diagnostics_departments dd ON (dd.ddept_id = d.ddept_id)"
      + " JOIN diagnostic_charges dc ON (dc.bed_type=? AND dc.org_name=? AND d.test_id=dc.test_id)"
      + " JOIN test_org_details tod ON (tod.org_id =? AND tod.test_id = dc.test_id)"
      + " WHERE ptp.preauth_required = 'Y' AND pp.consultation_id=?" + " UNION ALL"
      + " SELECT 'Service' AS item_type, pp.patient_presc_id, s.service_name AS item_name,"
      + " s.service_id AS item_id, sod.item_code, s.serv_dept_id::text AS dept_id,"
      + " smc.unit_charge AS charge, '' AS category, smc.discount, sod.code_type,"
      + " s.service_sub_group_id, s.insurance_category_id, s.allow_rate_increase,"
      + " s.allow_rate_decrease, psp.qty AS item_qty, psp.tooth_unv_number, psp.tooth_fdi_number,"
      + " psp.service_remarks AS item_remarks, psp.preauth_required, pp.status"
      + " FROM patient_prescription pp"
      + " JOIN patient_service_prescriptions psp ON (pp.patient_presc_id=psp.op_service_pres_id)"
      + " JOIN services s ON (s.service_id = psp.service_id)"
      + " JOIN service_master_charges smc ON (smc.bed_type=? AND smc.org_id=?"
      + "   AND s.service_id=smc.service_id)"
      + " JOIN service_org_details sod ON (sod.org_id =? AND sod.service_id = smc.service_id)"
      + " WHERE psp.preauth_required = 'Y' AND pp.consultation_id=?" + " UNION ALL"
      + " SELECT 'Operation' AS item_type, pp.patient_presc_id, om.operation_name AS item_name,"
      + " om.op_id AS item_id, ood.item_code, om.dept_id, oc.surg_asstance_charge AS charge,"
      + " '' AS category, oc.surg_asst_discount AS discount, ood.code_type,"
      + " om.service_sub_group_id, om.insurance_category_id, om.allow_rate_increase,"
      + " om.allow_rate_decrease, 1 AS item_qty, '' AS tooth_unv_number, '' AS tooth_fdi_number,"
      + " pop.remarks AS item_remarks, pop.preauth_required, pp.status"
      + " FROM patient_prescription pp"
      + " JOIN patient_operation_prescriptions pop ON (pp.patient_presc_id=pop.prescription_id)"
      + " JOIN operation_master om ON (pop.operation_id=om.op_id)"
      + " JOIN operation_charges oc ON (oc.bed_type=? AND oc.org_id=? AND om.op_id=oc.op_id)"
      + " JOIN operation_org_details ood ON (ood.org_id=? AND oc.op_id = ood.operation_id)"
      + " WHERE pop.preauth_required = 'Y' AND pp.consultation_id=?" + " UNION ALL" + " SELECT"
      + " 'Doctor' AS item_type, pp.patient_presc_id, d.doctor_name AS item_name,"
      + " d.doctor_id AS item_id, cod.item_code, d.dept_id, dopc.op_charge + cc.charge AS charge,"
      + " '' AS category, dopc.op_charge_discount + cc.discount AS discount, cod.code_type,"
      + " null AS service_sub_group_id, ct.insurance_category_id, ct.allow_rate_increase,"
      + " ct.allow_rate_decrease, 1 AS item_qty, '' AS tooth_unv_number, '' AS tooth_fdi_number,"
      + " pcp.cons_remarks AS item_remarks, pcp.preauth_required, pp.status"
      + " FROM patient_prescription pp"
      + " JOIN patient_consultation_prescriptions pcp ON (pp.patient_presc_id=pcp.prescription_id)"
      + " JOIN doctors d ON (pcp.doctor_id = d.doctor_id)"
      + " JOIN doctor_op_consultation_charge dopc ON (dopc.org_id=?"
      + "   AND dopc.doctor_id = d.doctor_id)"
      + " JOIN consultation_charges cc ON (cc.bed_type =? AND cc.org_id=?"
      + "   AND cc.consultation_type_id = -1)"
      + " JOIN consultation_org_details cod ON (cod.consultation_type_id = -1 AND cod.org_id =?)"
      + " JOIN doctor_op_consultation_charge docc ON (docc.org_id=? AND docc.doctor_id=d.doctor_id)"
      + " JOIN consultation_types ct ON (ct.consultation_type_id=-1)"
      + " WHERE pcp.preauth_required = 'Y' AND pp.consultation_id=?" + " ORDER BY patient_presc_id";


  // Below methods used for prints
  private static final String GET_STORE_MEDICINE_PRESCRIPTIONS_PRINTS =
      "SELECT pp.prescribed_date, "
          + " sid.medicine_name as item_name, sid.medicine_id::text as item_id, "
          + " pmp.medicine_quantity, op_medicine_pres_id as item_prescribed_id, "
          + " frequency as medicine_dosage, pmp.frequency, pmp.strength,"
          + " duration as medicine_days, "
          + " medicine_remarks as item_remarks, pp.status as issued, pmp.cons_uom_id,"
          + " cum.consumption_uom, "
          + " g.generic_name, g.generic_code, 'item_master' as master, 'Medicine' as item_type, "
          + " false as ispackage, activity_due_date, mod_time,  mr.route_id, mr.route_name, "
          + " '' AS category_name, '' AS manf_name, '' AS manf_mnemonic, 0 as lblcount, "
          + " issue_base_unit, sid.prior_auth_required,"
          + " coalesce(pmp.item_form_id, 0) as item_form_id, "
          + " pmp.item_strength, ifm.item_form_name, 0 as charge, 0 as discount,"
          + " '' as test_category, "
          + " 'N' as tooth_num_required, '' as tooth_unv_number, '' as tooth_fdi_number, "
          + "0 as service_qty, '' as service_code, false as non_hosp_medicine, pmp.duration, "
          + "pmp.duration_units, pmp.item_strength_units, su.unit_name, pmp.pbm_presc_id, "
          + "pmp.erx_status, pmp.erx_denial_code, pmp.erx_denial_remarks, "
          + "idc.status as denial_code_status, idc.type as denial_code_type, pmp.admin_strength, "
          + "ifm.granular_units, pp.special_instr,"
          + " idc.code_description as denial_desc, idc.example, "
          + "'' as preauth_required, '' as dept_name, pmp.send_for_erx, '' as item_code, "
          + "sid.medicine_name, medicine_remarks, '' as test_name, '' as test_remarks, "
          + "'' as service_name, '' as service_remarks, '' as cons_doctor_name, "
          + "'' as cons_remarks, sic.item_code as drug_code, pmp.refills, pmp.time_of_intake,"
          + " pmp.priority, sict.control_type_name, pmp.expiry_date,"
          + " pp.start_datetime, pp.end_datetime, pp.doctor_id as prescribed_doctor_id "
          + " FROM patient_prescription pp "
          + "JOIN patient_medicine_prescriptions pmp ON"
          + " (pp.patient_presc_id=pmp.op_medicine_pres_id AND is_discharge_medication = false)"
          + " LEFT JOIN consumption_uom_master cum ON (cum.cons_uom_id=pmp.cons_uom_id)"
          + " LEFT JOIN store_item_details sid ON (pmp.medicine_id=sid.medicine_id) "
          + "JOIN organization_details od ON (od.org_id=?) "
          + "LEFT JOIN store_item_rates sir ON (sir.medicine_id=sid.medicine_id  "
          + "         AND sir.store_rate_plan_id=od.store_rate_plan_id) "
          + "LEFT JOIN LATERAL (select max(sibd.mrp) AS mrp "
          + "         from store_stock_details ssd, stores s, store_item_batch_details sibd "
          + " where s.dept_id=ssd.dept_id and "
          + "   auto_fill_prescriptions and ssd.medicine_id = sid.medicine_id "
          + "AND ssd.item_batch_id=sibd.item_batch_id "
          + "         AND s.center_id=? ) AS ssd ON true "
          + "LEFT JOIN store_category_master mc ON (mc.category_id = sid.med_category_id) "
          + "LEFT JOIN generic_name g ON (pmp.generic_code = g.generic_code) "
          + "LEFT JOIN strength_units su ON (pmp.item_strength_units=su.unit_id) "
          + "LEFT JOIN item_form_master ifm ON (pmp.item_form_id=ifm.item_form_id) "
          + "LEFT JOIN medicine_route mr ON (mr.route_id=pmp.route_of_admin) "
          + "LEFT JOIN insurance_denial_codes idc ON (idc.denial_code = pmp.erx_denial_code) "
          + "LEFT JOIN ha_item_code_type hict ON (hict.medicine_id = sid.medicine_id "
          + "AND hict.health_authority= ? ) "
          + "LEFT JOIN store_item_codes sic ON (sic.medicine_id = hict.medicine_id "
          + "AND sic.code_type = hict.code_type) "
          + "LEFT JOIN store_item_controltype sict ON (sict.control_type_id = sid.control_type_id)"
          + "WHERE pp.consultation_id=? ";

  private static final String GET_OTHER_MEDICINE_PRESCRIPTIONS_PRINT = "SELECT pp.prescribed_date,"
      + " pomp.medicine_name as item_name, '' as item_id, pomp.medicine_quantity,"
      + " prescription_id as item_prescribed_id, pomp.frequency as medicine_dosage, pomp.frequency,"
      + " pomp.strength, pomp.duration as medicine_days, pomp.medicine_remarks as item_remarks,"
      + " 'P' as issued, pomp.cons_uom_id, cum.consumption_uom, g.generic_name, g.generic_code,"
      + " 'op' as master,"
      + " 'Medicine' as item_type, false as ispackage, pomp.activity_due_date, pomp.mod_time,"
      + " mr.route_id, mr.route_name, '' AS category_name, '' as manf_name, '' as manf_mnemonic,"
      + " 0 as lblcount, 0 as issue_base_unit, '' as prior_auth_required,"
      + " coalesce(pomp.item_form_id, 0) as item_form_id, pomp.item_strength, ifm.item_form_name,"
      + " 0 as charge, 0 as discount, '' as test_category, 'N' as tooth_num_required,"
      + " '' as tooth_unv_number, '' as tooth_fdi_number, 0 as service_qty, '' as service_code,"
      + " false as non_hosp_medicine, pomp.duration, pomp.duration_units, pomp.item_strength_units,"
      + " su.unit_name, 0 as pbm_presc_id, '' as erx_status, '' as erx_denial_code,"
      + " '' as erx_denial_remarks, '' as denial_code_status, '' as denial_code_type,"
      + " pomp.admin_strength, ifm.granular_units, pp.special_instr, '' as denial_desc,"
      + " '' as example, '' as preauth_required, '' as dept_name, 'N' as send_for_erx,"
      + " '' as item_code, "
      // legacy support variables in print
      + " pomp.medicine_name, pomp.medicine_remarks, '' as test_name, '' as test_remarks,"
      + " '' as service_name, '' as service_remarks, '' as cons_doctor_name, '' as cons_remarks,"
      + " '' as drug_code, pomp.refills, pomp.time_of_intake, pomp.priority, sic.control_type_name,"
      + " pomp.expiry_date, pp.start_datetime, pp.end_datetime,"
      + " pp.doctor_id as prescribed_doctor_id"
      + " FROM patient_prescription pp"
      + " JOIN patient_other_medicine_prescriptions pomp "
      + " ON (pp.patient_presc_id=pomp.prescription_id AND is_discharge_medication = false)"
      + " LEFT JOIN item_form_master ifm ON (pomp.item_form_id = ifm.item_form_id)"
      + " JOIN prescribed_medicines_master pms ON (pomp.medicine_name=pms.medicine_name)"
      + " LEFT JOIN strength_units su ON (su.unit_id=pms.item_strength_units)"
      + " LEFT JOIN generic_name g ON (pms.generic_name=g.generic_code)"
      + " LEFT JOIN medicine_route mr ON (mr.route_id=pomp.route_of_admin)"
      + " LEFT JOIN patient_medicine_prescriptions pmp"
      + " ON (pp.patient_presc_id=pmp.op_medicine_pres_id)"
      + " LEFT JOIN store_item_details sid ON (sid.medicine_id=pmp.medicine_id)"
      + " LEFT JOIN store_item_controltype sic ON (sic.control_type_id = sid.control_type_id)"
      + " LEFT JOIN consumption_uom_master cum ON (cum.cons_uom_id = pomp.cons_uom_id)"
      + " WHERE pp.consultation_id=?";

  private static final String GET_PRESCRIPTIONS_PRINTS = "UNION ALL"
      // Service
      + " SELECT " + " pp.prescribed_date, s.service_name as item_name, s.service_id as item_id, 0,"
      + " op_service_pres_id, '', '', '', 0, service_remarks as item_remarks,"
      + " pp.status as added_to_bill, null as cons_uom_id, '' as consumption_uom, '', '',"
      + " 'item_master' as master,"
      + " 'Service' as item_type, false as ispackage, activity_due_date, mod_time, -1 as route_id,"
      + " '' as route_name,'' AS category_name,'' as manf_name, '' as manf_mnemonic, 0 as lblcount,"
      + " 0 as issue_base_unit, s.prior_auth_required, 0 as item_form_id, '' as item_strength,"
      + " '' as item_form_name, smc.unit_charge as charge, smc.discount as discount,"
      + " '' as test_category, s.tooth_num_required, psp.tooth_unv_number, psp.tooth_fdi_number,"
      + " qty as service_qty, service_code, false as non_hosp_medicine, 0 as duration,"
      + " '' as duration_units, 0 as item_strength_units, '' as unit_name, 0 as pbm_presc_id,"
      + " '' as erx_status, '' as erx_denial_code, '' as erx_denial_remarks,"
      + " '' as denial_code_status, '' as denial_code_type, psp.admin_strength,"
      + " 'N' as granular_units, pp.special_instr, '' as denial_desc, '' as example,"
      + " preauth_required, sd.department as dept_name, 'N' as send_for_erx, sod.item_code, "
      // legacy support variables in print
      + " '' as medicine_name, '' as medicine_remarks, '' as test_name, '' as test_remarks,"
      + " s.service_name, service_remarks, '' as cons_doctor_name, '' as cons_remarks ,"
      + " '' as drug_code, '' as refills, '' AS time_of_intake, psp.priority,"
      + " '' as control_type_name, null AS expiry_date, pp.start_datetime,"
      + " pp.end_datetime,  pp.doctor_id as prescribed_doctor_id"
      + " FROM patient_prescription pp"
      + " JOIN patient_service_prescriptions psp ON (pp.patient_presc_id=psp.op_service_pres_id)"
      + " JOIN services s ON (s.service_id = psp.service_id)"
      + " JOIN services_departments sd ON (sd.serv_dept_id=s.serv_dept_id)"
      + " JOIN service_master_charges smc ON (s.service_id=smc.service_id AND bed_type=?"
      + "   AND smc.org_id=?)"
      + " JOIN service_org_details sod ON (sod.service_id = smc.service_id AND sod.org_id = ?)"
      + " WHERE pp.consultation_id=?" + " UNION ALL"
      // Operation
      + " SELECT" + " pp.prescribed_date, om.operation_name as item_name, om.op_id as item_id, 0,"
      + " prescription_id, '', '', '', 0, pop.remarks as item_remarks, pp.status as added_to_bill,"
      + " null as cons_uom_id, '' as consumption_uom, '', '', 'item_master' as master,"
      + " 'Operation' as item_type,"
      + " false as ispackage, null::date as activity_due_date, mod_time, -1 as route_id,"
      + " '' as route_name, '' AS category_name,'' as manf_name, '' as manf_mnemonic,"
      + " 0 as lblcount, 0 as issue_base_unit, om.prior_auth_required, 0 as item_form_id,"
      + " '' as item_strength, '' as item_form_name, surg_asstance_charge as charge,"
      + " surg_asst_discount as discount, '' as test_category, 'N' as tooth_num_required,"
      + " '' as tooth_unv_number, '' as tooth_fdi_number, 0 as service_qty, '' as service_code,"
      + " false as non_hosp_medicine, 0 as duration, 'D' as duration_units,"
      + " 0 as item_strength_units, '' as master_item_strength_units, 0 as pbm_presc_id,"
      + " '' as erx_status, '' as erx_denial_code, '' as erx_denial_remarks,"
      + " '' as denial_code_status, '' as denial_code_type, pop.admin_strength,"
      + " 'N' as granular_units, pp.special_instr, '' as denial_desc, '' as example,"
      + " preauth_required, dept.dept_name, 'N' as send_for_erx, ood.item_code, "
      // legacy support variables in print
      + " '' as medicine_name, '' as medicine_remarks, '' as test_name, '' as test_remarks,"
      + " '' as service_name, '' as service_remarks, '' as cons_doctor_name, '' as cons_remarks,"
      + " '' as drug_code, '' as refills, '' AS time_of_intake, '' AS priority,"
      + " '' as control_type_name, null AS expiry_date, pp.start_datetime, pp.end_datetime,  "
      + " pp.doctor_id as prescribed_doctor_id"
      + " FROM patient_prescription pp"
      + " JOIN patient_operation_prescriptions pop ON (pp.patient_presc_id=pop.prescription_id)"
      + " JOIN operation_master om ON (pop.operation_id=om.op_id)"
      + " JOIN department dept ON (dept.dept_id=om.dept_id)"
      + " JOIN operation_org_details ood ON (ood.operation_id=om.op_id AND ood.org_id=?)"
      + " JOIN operation_charges oc ON (om.op_id=oc.op_id AND bed_type=? AND oc.org_id=?)"
      + " WHERE pp.consultation_id=?" + " UNION ALL" + " SELECT"
      // Doctor
      + " pp.prescribed_date, d.doctor_name as item_name, d.doctor_id, 0, pcp.prescription_id,"
      + " '', '', '', 0, cons_remarks as item_remarks, pp.status as added_to_bill,"
      + " null as cons_uom_id, '' as consumption_uom, '', '', 'item_master' as master,"
      + " 'Doctor' as item_type,"
      + " false as ispackage, activity_due_date, mod_time, -1 as route_id, '' as route_name,"
      + " '' AS category_name,'' as manf_name, '' as manf_mnemonic, 0 as lblcount,"
      + " 0 as issue_base_unit, '' as prior_auth_required, 0 as item_form_id, '' as item_strength,"
      + " '' as item_form_name, 0 as charge, 0 as discount, '' as test_category,"
      + " 'N' as tooth_num_required, '' as tooth_unv_number, '' as tooth_fdi_number,"
      + " 0 as service_qty, '' as service_code, false as non_hosp_medicine, 0 as duration,"
      + " '' as duration_units, 0 as item_strength_units, '' as unit_name, 0 as pbm_presc_id,"
      + " '' as erx_status, '' as erx_denial_code, '' as erx_denial_remarks,"
      + " '' as denial_code_status, '' as denial_code_type, pcp.admin_strength,"
      + " 'N' as granular_units, pp.special_instr, '' as denial_desc, '' as example,"
      + " preauth_required, dept.dept_name, 'N' as send_for_erx, '' as item_code, "
      // legacy support variables in print
      + " '' as medicine_name, '' as medicine_remarks, '' as test_name, '' as test_remarks,"
      + " '' as service_name, '' as service_remarks, d.doctor_name as cons_doctor_name,"
      + " cons_remarks, '' as drug_code, '' as refills, '' AS time_of_intake, '' AS priority,"
      + " '' as control_type_name, null AS expiry_date, pp.start_datetime, pp.end_datetime,"
      + " pp.doctor_id as prescribed_doctor_id"
      + " FROM patient_prescription pp"
      + " JOIN patient_consultation_prescriptions pcp ON (pp.patient_presc_id=pcp.prescription_id)"
      + " JOIN doctors d ON (pcp.doctor_id = d.doctor_id)"
      + " JOIN department dept ON (dept.dept_id=d.dept_id) WHERE pp.consultation_id=?"
      + " UNION ALL" + " SELECT"
      // Tests
      + " pp.prescribed_date, coalesce(d.test_name, pm.package_name) AS test_name,"
      + " coalesce(d.test_id, pm.package_id::text) AS item_id, 0, op_test_pres_id, '', '', '', 0,"
      + " ptp.test_remarks AS item_remarks, pp.status as added_to_bill, null as cons_uom_id,"
      + " '' as consumption_uom, '',"
      + " '', 'item_master' AS master, 'Inv.' as item_type, ptp.ispackage , activity_due_date,"
      + " mod_time, -1 as route_id, '' as route_name, '' AS category_name, '' as manf_name,"
      + " '' as manf_mnemonic, 0 as lblcount, 0 as issue_base_unit,"
      + " coalesce(d.prior_auth_required, pm.prior_auth_required), 0 as item_form_id,"
      + " '' as item_strength, '' as item_form_name, coalesce(dc.charge, pc.charge, 0) as charge,"
      + " coalesce(dc.discount, pc.discount, 0) as discount,"
      + " coalesce(dd.category, '') AS test_category, 'N' as tooth_num_required,"
      + " '' as tooth_unv_number, '' as tooth_fdi_number, 0 as service_qty, '' as service_code,"
      + " false as non_hosp_medicine, 0 as duration, '' as duration_units,"
      + " 0 as item_strength_units, '' as unit_name, 0 as pbm_presc_id, '' as erx_status,"
      + " '' as erx_denial_code, '' as erx_denial_remarks, '' as denial_code_status,"
      + " '' as denial_code_type, ptp.admin_strength, 'N' as granular_units, pp.special_instr,"
      + " '' as denial_desc, '' as example, ptp.preauth_required, dd.ddept_name as dept_name,"
      + " 'N' as send_for_erx, coalesce(tod.item_code, pod.item_code) AS item_code,"
      + " '' as medicine_name, '' as medicine_remarks,"
      + " coalesce(d.test_name, pm.package_name) AS test_name, test_remarks, '' as service_name,"
      + " '' as service_remarks, '' as cons_doctor_name, '' as cons_remarks, '' as drug_code,"
      + " '' as refills, '' AS time_of_intake, ptp.priority, '' as control_type_name,"
      + " null AS expiry_date, pp.start_datetime, pp.end_datetime,"
      + " pp.doctor_id as prescribed_doctor_id" 
      + " FROM patient_prescription pp"
      + " JOIN patient_test_prescriptions ptp ON (pp.patient_presc_id=ptp.op_test_pres_id)"
      + " LEFT JOIN diagnostics d ON (d.test_id=ptp.test_id)"
      + " LEFT JOIN test_org_details tod ON (tod.test_id=d.test_id AND tod.org_id=?)"
      + " LEFT JOIN diagnostics_departments dd ON (dd.ddept_id=d.ddept_id)"
      + " LEFT JOIN packages pm ON (pm.package_id::text=ptp.test_id)"
      + " LEFT JOIN pack_org_details pod ON (pod.package_id=pm.package_id AND pod.org_id=?)"
      + " LEFT JOIN diagnostic_charges dc ON (d.test_id=dc.test_id AND dc.bed_type=?"
      + "   AND dc.org_name=?)"
      + " LEFT JOIN package_charges pc ON (pm.package_id=pc.package_id AND pc.bed_type=?"
      + "   AND pc.org_id=?)" + " WHERE pp.consultation_id=?" + " UNION ALL" + " SELECT" + " "
      // Non-Hospital
      + " pp.prescribed_date, item_name, '' as item_id, medicine_quantity, prescription_id,"
      + " frequency as medicine_dosage, frequency, strength, duration as medicine_days,"
      + " item_remarks, 'P' as issued, pop.cons_uom_id, cum.consumption_uom, '', '',"
      + " 'op' as master,"
      + " case when non_hosp_medicine then 'Medicine' else 'NonHospital' end as item_type,"
      + " false as ispackage, activity_due_date, mod_time, -1 as route_id, '' as route_name,"
      + " '' AS category_name,'' as manf_name, '' as manf_mnemonic , 0 as lblcount,"
      + " 0 as issue_base_unit, '' as prior_auth_required, pop.item_form_id, pop.item_strength,"
      + " ifm.item_form_name, 0 as charge, 0 as discount, '' as test_category,"
      + " 'N' as tooth_num_required, '' as tooth_unv_number, '' as tooth_fdi_number,"
      + " 0 as service_qty, '' as service_code, non_hosp_medicine, duration, duration_units,"
      + " item_strength_units, su.unit_name, 0 as pbm_presc_id, '' as erx_status,"
      + " '' as erx_denial_code, '' as erx_denial_remarks, '' as denial_code_status,"
      + " '' as denial_code_type, pop.admin_strength, ifm.granular_units, pp.special_instr,"
      + " '' as denial_desc, '' as example, '' as preauth_required, '' as dept_name,"
      + " 'N' as send_for_erx, '' as item_code, "
      // legacy support variables in print
      + " '' as medicine_name, '' as medicine_remarks, '' as test_name, '' as test_remarks,"
      + " '' as service_name, '' as service_remarks, '' as cons_doctor_name, '' as cons_remarks,"
      + " '' as drug_code, pop.refills as refills, pop.time_of_intake, pop.priority,"
      + " '' as control_type_name, null AS expiry_date, pp.start_datetime, pp.end_datetime,"
      + " pp.doctor_id as prescribed_doctor_id"
      + " FROM patient_prescription pp"
      + " JOIN patient_other_prescriptions pop ON (pp.patient_presc_id=pop.prescription_id "
      + " AND is_discharge_medication = false)"
      + " LEFT JOIN strength_units su ON (su.unit_id=pop.item_strength_units)"
      + " LEFT JOIN item_form_master ifm ON (pop.item_form_id=ifm.item_form_id)"
      + " LEFT JOIN consumption_uom_master cum ON (pop.cons_uom_id = cum.cons_uom_id)"
      + " WHERE pp.consultation_id=?" + " UNION ALL" + " SELECT" + " "
      // Department Referral
      + " pp.prescribed_date, d.dept_name as item_name, d.dept_id as item_id, 0,"
      + " pcp.prescription_id, '', '', '', 0, cons_remarks as item_remarks,"
      + " pp.status as added_to_bill, null as cons_uom_id, '' as consumption_uom, '', '',"
      + " 'item_master' as master,"
      + " 'Doctor' as item_type, false as ispackage, activity_due_date, mod_time, -1 as route_id,"
      + " '' as route_name, '' AS category_name,'' as manf_name, '' as manf_mnemonic,"
      + " 0 as lblcount, 0 as issue_base_unit, '' as prior_auth_required, 0 as item_form_id,"
      + " '' as item_strength, '' as item_form_name, 0 as charge, 0 as discount,"
      + " '' as test_category, 'N' as tooth_num_required, '' as tooth_unv_number,"
      + " '' as tooth_fdi_number, 0 as service_qty, '' as service_code, false as non_hosp_medicine,"
      + " 0 as duration, '' as duration_units, 0 as item_strength_units, '' as unit_name,"
      + " 0 as pbm_presc_id, '' as erx_status, '' as erx_denial_code, '' as erx_denial_remarks,"
      + " '' as denial_code_status, '' as denial_code_type, pcp.admin_strength,"
      + " 'N' as granular_units, pp.special_instr, '' as denial_desc, '' as example,"
      + " preauth_required, d.dept_name, 'N' as send_for_erx, '' as item_code, "
      // legacy support variables in print
      + " '' as medicine_name, '' as medicine_remarks, '' as test_name, '' as test_remarks,"
      + " '' as service_name, '' as service_remarks, d.dept_name as cons_doctor_name, cons_remarks,"
      + " '' as drug_code, '' as refills, '' AS time_of_intake, '' AS priority,"
      + " '' as control_type_name, null AS expiry_date, pp.start_datetime, pp.end_datetime,"
      + " pp.doctor_id as prescribed_doctor_id"
      + " FROM patient_prescription pp"
      + " JOIN patient_consultation_prescriptions pcp ON (pp.patient_presc_id=pcp.prescription_id)"
      + " JOIN department d ON (pcp.dept_id = d.dept_id)" + " WHERE pp.consultation_id=?"
      + " ORDER BY item_type, item_prescribed_id";

  /**
   * Get prescriptions for prints.
   * 
   * @param consId the integer
   * @param bedType the string
   * @param orgId the string
   * @param centerId the integer
   * @param healthAuthority the string
   * @param presFromStores the boolean
   * @return list of BasicDynaBean
   */
  public List<BasicDynaBean> getPrescriptionsForPrints(Integer consId, String bedType, String orgId,
      Integer centerId, String healthAuthority, Boolean presFromStores) {
    if (presFromStores) {
      String query = GET_STORE_MEDICINE_PRESCRIPTIONS_PRINTS + GET_PRESCRIPTIONS_PRINTS;
      return DatabaseHelper.queryToDynaList(query,
          new Object[] {orgId, centerId, healthAuthority, consId, bedType, orgId, orgId, consId,
              orgId, bedType, orgId, consId, consId, orgId, orgId, bedType, orgId, bedType, orgId,
              consId, consId, consId});
    } else {
      String query = GET_OTHER_MEDICINE_PRESCRIPTIONS_PRINT + GET_PRESCRIPTIONS_PRINTS;
      return DatabaseHelper.queryToDynaList(query,
          new Object[] {consId, bedType, orgId, orgId, consId, orgId, bedType, orgId, consId,
              consId, orgId, orgId, bedType, orgId, bedType, orgId, consId, consId, consId});
    }
  }

  private static final String ALL_PACKAGE_COMPONENT =
      " SELECT d.test_name, ptp.op_test_pres_id" + " FROM patient_prescription pp"
          + " JOIN patient_test_prescriptions ptp ON (pp.patient_presc_id = ptp.op_test_pres_id)"
          + " JOIN packages pm ON (pm.package_id::text = ptp.test_id AND ptp.ispackage = 'true')"
          + " JOIN package_contents pcd ON (pcd.package_id = pm.package_id)"
          + " JOIN diagnostics d ON (d.test_id = pcd.activity_id)"
          + " WHERE pm.type='O' AND pp.consultation_id = ?" + " ORDER BY pcd.display_order ";

  public List getAllPackageComponents(int consultationId) {
    return DatabaseHelper.queryToDynaList(ALL_PACKAGE_COMPONENT, new Object[] {consultationId});
  }

  private static final String GET_GENERIC_PRES_FROM_TEMPLATE_IDS =
      "SELECT" + " generic_name AS item_name, '' AS item_rate_plan_code, generic_code as item_id,"
          + " generic_code, generic_name, '' AS drug_code, 'item_master' AS master,"
          + " 'Medicine' AS item_type, '' AS route_of_admin, '' AS consumption_uom,"
          + " '' AS prior_auth_required, 0 AS issue_base_unit, 0 AS item_form_id,"
          + " '' AS item_strength, 0 AS item_strength_units, 'N' AS granular_units,"
          + " 0 AS insurance_category_id, 0 AS charge, 0 AS discount, 0 AS control_type_id,"
          + " false AS is_package, '' AS category, 'N' AS tooth_num_required"
          + " FROM generic_name WHERE generic_code IN (:item_ids) ";

  private static final String GET_NON_HOSPITAL_PRES_FROM_TEMPLATE_NAMES = "SELECT"
      + " medicine_name as item_name, '' AS item_rate_plan_code, '' as item_id, g.generic_code,"
      + " g.generic_name, '' AS drug_code, 'op' as master, 'Medicine' as item_type,"
      + " pmm.route_of_admin, '' AS consumption_uom, '' AS prior_auth_required,"
      + " 0 AS issue_base_unit, item_form_id, item_strength, item_strength_units,"
      + " 'N' AS granular_units, 0 AS insurance_category_id, 0 AS charge, 0 AS discount,"
      + " 0 AS control_type_id, false AS is_package, '' AS category,"
      + " 'N' AS tooth_num_required FROM prescribed_medicines_master pmm"
      + " LEFT JOIN generic_name g ON (pmm.generic_name=g.generic_code)"
      + " WHERE pmm.status='A' AND medicine_name IN (:item_ids) ";

  private static final String GET_STORE_MEDICINE_PRES_FROM_TEMPLATE_IDS = "SELECT"
      + " sid.medicine_name as item_name, '' AS item_rate_plan_code,"
      + " sid.medicine_id::text as item_id, g.generic_code, g.generic_name, "
      + " sic.item_code AS drug_code, 'item_master' as master, 'Medicine' as item_type, "
      + " sid.route_of_admin, sid.cons_uom_id, cum.consumption_uom, sid.prior_auth_required,"
      + " sid.issue_base_unit,"
      + " coalesce(sid.item_form_id, 0) AS item_form_id, sid.item_strength,"
      + " sid.item_strength_units, COALESCE(ifm.granular_units, 'N') as granular_units,"
      + " sid.insurance_category_id, COALESCE((case when textregexeq(sir.selling_price_expr,"
      + "   '^[0-9]\\.?[0-9]*$') then sir.selling_price_expr::decimal else null end), ssd.mrp,"
      + "   sid.item_selling_price, 0) AS charge, (COALESCE((case when textregexeq("
      + "   sir.selling_price_expr, '^[0-9]\\.?[0-9]*$')"
      + "   then sir.selling_price_expr::decimal else null end), ssd.mrp,"
      + "   sid.item_selling_price, 0) * COALESCE(mc.discount, 0) / 100) AS discount,"
      + " sid.control_type_id, false AS is_package, '' AS category, 'N' AS tooth_num_required"
      + " FROM store_item_details sid"
      + " JOIN LATERAL (select sum(qty) AS qty, max(sibd.mrp) AS mrp from store_stock_details ssd,"
      + "   stores s, store_item_batch_details sibd where s.dept_id=ssd.dept_id"
      + "   and auto_fill_prescriptions and ssd.medicine_id = sid.medicine_id"
      + "   AND ssd.item_batch_id=sibd.item_batch_id AND s.center_id=:center_id)"
      + "   AS ssd ON ssd.qty IS NOT NULL"
      + " LEFT JOIN generic_name g ON (sid.generic_name=g.generic_code)"
      + " JOIN organization_details od ON (od.org_id=:org_id)"
      + " JOIN store_category_master mc ON (mc.category_id = sid.med_category_id)"
      + " LEFT JOIN item_form_master ifm ON (sid.item_form_id = ifm.item_form_id)"
      + " LEFT JOIN ha_item_code_type hict ON (hict.medicine_id = sid.medicine_id"
      + "   AND hict.health_authority=:health_authority )"
      + " LEFT JOIN store_item_codes sic ON (sic.medicine_id = hict.medicine_id"
      + "   AND sic.code_type = hict.code_type)"
      + " LEFT JOIN store_item_rates sir ON (sir.medicine_id=sid.medicine_id"
      + "   AND sir.store_rate_plan_id=od.store_rate_plan_id)"
      + " LEFT JOIN consumption_uom_master cum ON (cum.cons_uom_id = sid.cons_uom_id)"
      + " WHERE sid.status='A' AND sid.medicine_id::text IN (:item_ids) ";

  private static final String GET_PRES_FROM_TEMPLATE_IDS = " UNION ALL" + " SELECT"
      + " d.test_name AS item_name, tod.item_code AS item_rate_plan_code, d.test_id AS item_id,"
      + " '' AS generic_code, '' AS generic_name, '' AS drug_code, 'item_master' as master,"
      + " 'Inv.' AS item_type, '' AS route_of_admin, null AS cons_uom_id, '' AS consumption_uom,"
      + " d.prior_auth_required,"
      + " 0 AS issue_base_unit, null AS item_form_id, '' AS item_strength,"
      + " null AS item_strength_units, 'N' AS granular_units, d.insurance_category_id, dc.charge,"
      + " dc.discount, 0 AS control_type_id, false AS is_package, dd.category,"
      + " 'N' AS tooth_num_required" + " FROM diagnostics d"
      + " JOIN diagnostic_charges dc ON (d.test_id=dc.test_id AND dc.bed_type=:bed_type"
      + "   AND dc.org_name=:org_id)"
      + " JOIN diagnostics_departments dd ON (dd.ddept_id=d.ddept_id)"
      + " JOIN test_org_details tod ON (tod.test_id=d.test_id AND tod.org_id=:org_id)"
      + " WHERE d.status='A' AND d.test_id IN (:item_ids)" + " UNION ALL"
      + " SELECT pm.package_name AS item_name, pod.item_code AS item_rate_plan_code,"
      + " pm.package_id::text AS item_id, '' AS generic_code, '' AS generic_name, '' AS drug_code,"
      + " 'item_master' as master, 'Inv.' AS item_type, '' AS route_of_admin,"
      + " null AS cons_uom_id, '' AS consumption_uom, pm.prior_auth_required, 0 AS issue_base_unit,"
      + " null AS item_form_id, '' AS item_strength, null AS item_strength_units,"
      + " 'N' AS granular_units, pm.insurance_category_id, coalesce(pc.charge, 0) as charge,"
      + " coalesce(pc.discount, 0) as discount, 0 AS control_type_id, true AS is_package,"
      + " '' AS category, 'N' AS tooth_num_required" + " FROM packages pm"
      + " LEFT JOIN package_charges pc ON (pm.package_id=pc.package_id AND pc.bed_type=:bed_type"
      + "   AND pc.org_id=:org_id)"
      + " LEFT JOIN pack_org_details pod ON (pod.package_id=pm.package_id AND pod.org_id=:org_id)"
      + " JOIN center_package_applicability pcm ON (pcm.package_id=pm.package_id"
      + "   AND pcm.status='A' AND (pcm.center_id=:center_id or pcm.center_id=-1))"
      + " JOIN package_sponsor_master psm ON (psm.pack_id=pm.package_id AND psm.status='A'"
      + "   AND (psm.tpa_id=:tpa_id OR psm.tpa_id = '-1'))"
      + " WHERE pm.visit_applicability='d' AND pm.approval_status='A' AND pm.status='A'"
      + "   AND pm.package_id::text IN (:item_ids)" + " UNION ALL"
      + " SELECT om.operation_name AS item_name, ood.item_code AS item_rate_plan_code,"
      + " om.op_id AS item_id, '' AS generic_code, '' AS generic_name, '' AS drug_code,"
      + " 'item_master' as master, 'Operation' AS item_type, '' AS route_of_admin,"
      + " null AS cons_uom_id, '' AS consumption_uom, om.prior_auth_required, 0 AS issue_base_unit,"
      + " null AS item_form_id, '' AS item_strength, null AS item_strength_units,"
      + " 'N' AS granular_units, om.insurance_category_id, surg_asstance_charge as charge,"
      + " surg_asst_discount as discount, 0 AS control_type_id, false AS is_package,"
      + " '' AS category, 'N' AS tooth_num_required" + " FROM operation_master om"
      + " JOIN operation_charges oc ON (om.op_id=oc.op_id AND bed_type=:bed_type"
      + "   AND org_id=:org_id)"
      + " JOIN operation_org_details ood ON (ood.operation_id=om.op_id AND ood.org_id=:org_id)"
      + " WHERE om.status='A' AND om.op_id IN (:item_ids)" + " UNION ALL"
      + " SELECT s.service_name AS item_name, sod.item_code AS item_rate_plan_code,"
      + " s.service_id AS item_id, '' AS generic_code, '' AS generic_name, '' AS drug_code,"
      + " 'item_master' as master, 'Service' AS item_type, '' AS route_of_admin,"
      + " null AS cons_uom_id, '' AS consumption_uom, s.prior_auth_required, 0 AS issue_base_unit,"
      + " null AS item_form_id,"
      + " '' AS item_strength, null AS item_strength_units, 'N' AS granular_units,"
      + " s.insurance_category_id, unit_charge as charge, discount, 0 AS control_type_id,"
      + " false AS is_package, '' AS category, s.tooth_num_required" + " FROM services s"
      + " JOIN service_master_charges smc ON (s.service_id=smc.service_id AND bed_type=:bed_type"
      + "   AND org_id=:org_id)"
      + " JOIN service_org_details sod ON (sod.service_id = smc.service_id AND sod.org_id =:org_id)"
      + " WHERE s.status='A' AND s.service_id IN (:item_ids)" + " UNION ALL"
      + " SELECT doctor_name AS item_name, '' AS item_rate_plan_code, d.doctor_id AS item_id,"
      + " '' AS generic_code, '' AS generic_name, '' AS drug_code, 'item_master' as master,"
      + " 'Doctor' AS item_type, '' AS route_of_admin, null AS cons_uom_id, '' AS consumption_uom,"
      + " '' AS prior_auth_required, 0 AS issue_base_unit, null AS item_form_id,"
      + " '' AS item_strength, null AS item_strength_units, 'N' AS granular_units,"
      + " ct.insurance_category_id, cc.charge + docc.op_charge AS charge,"
      + " cc.discount + docc.op_charge_discount AS discount, 0 AS control_type_id,"
      + " false AS is_package, '' AS category, '' AS tooth_num_required" + " FROM doctors d"
      + " JOIN consultation_charges cc ON (cc.org_id=:org_id AND cc.bed_type =:bed_type"
      + "   AND cc.consultation_type_id = -1)"
      + " JOIN doctor_op_consultation_charge docc ON (docc.doctor_id=d.doctor_id"
      + "   AND docc.org_id=cc.org_id)"
      + " JOIN consultation_types ct ON (ct.consultation_type_id=cc.consultation_type_id)"
      + " WHERE d.status='A' AND d.doctor_id IN (:item_ids)";

  /**
   * Gets template prescriptions by id.
   * 
   * @param orgId the string
   * @param bedType the string
   * @param centerId the integer
   * @param tpaId the string
   * @param healthAuthority the string
   * @param itemIds the list of string
   * @param presFromStores the boolean
   * @param isGenerics the boolean
   * @return list of BasicDynaBean
   */
  public List<BasicDynaBean> getTemplatePrescriptionsByIds(String orgId, String bedType,
      Integer centerId, String tpaId, String healthAuthority, List<String> itemIds,
      Boolean presFromStores, Boolean isGenerics) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("org_id", orgId);
    parameters.addValue("bed_type", bedType);
    parameters.addValue("center_id", centerId);
    parameters.addValue("tpa_id", tpaId);
    parameters.addValue("item_ids", itemIds);
    // Null check handled in jdbc PreparedStatementCreator.
    if (itemIds.isEmpty()) {
      parameters.addValue("item_ids", null);
    }

    if (presFromStores) {
      if (isGenerics) {
        return DatabaseHelper.queryToDynaList(
            GET_GENERIC_PRES_FROM_TEMPLATE_IDS + GET_PRES_FROM_TEMPLATE_IDS, parameters);
      } else {
        parameters.addValue("health_authority", healthAuthority);
        return DatabaseHelper.queryToDynaList(
            GET_STORE_MEDICINE_PRES_FROM_TEMPLATE_IDS + GET_PRES_FROM_TEMPLATE_IDS, parameters);
      }
    } else {
      return DatabaseHelper.queryToDynaList(
          GET_NON_HOSPITAL_PRES_FROM_TEMPLATE_NAMES + GET_PRES_FROM_TEMPLATE_IDS, parameters);
    }
  }

  private static final String GET_MEDICATIONS = "SELECT pp.patient_presc_id AS item_prescribed_id,"
      + " COALESCE(sid.medicine_id::text, g.generic_code) As item_id,"
      + " COALESCE(sid.medicine_name, g.generic_name) AS item_name, g.generic_name, g.generic_code,"
      + " pp.prescribed_date AS prescribed_date, pmp.admin_strength, pmp.strength,"
      + " pmp.item_strength, pmp.item_strength_units, su.unit_name,"
      + " rcm.display_name as frequency, pmp.medicine_remarks AS item_remarks,"
      + " ifm.granular_units, mr.route_name, pmp.item_form_id, ifm.item_form_name,"
      + " pmp.cons_uom_id, cum.consumption_uom, pmp.priority, pmp.controlled_drug_number,"
      + " sid.route_of_admin,"
      + " pp.doctor_id, pp.prior_med, pp.freq_type, pp.recurrence_daily_id, pp.repeat_interval,"
      + " pp.start_datetime, pp.end_datetime, pp.no_of_occurrences, pp.end_on_discontinue,"
      + " pp.discontinued, pp.repeat_interval_units, d.doctor_name,"
      + " rcm.display_name as recurrence_name, pmp.medication_type, (CASE "
      + "   WHEN (pp.end_on_discontinue = 'Y') THEN false "
      + "   WHEN (pmp.priority = 'S' AND (pp.start_datetime + interval '1 h' * (serving_window "
      + "     + late_serving_period)) < now()) THEN true "
      + "   WHEN (pp.end_datetime is not null and (pp.end_datetime + interval '1 h' * "
      + "     (serving_window + late_serving_period)) < now()) THEN true "
      + "   WHEN (pmp.priority = 'P' and Exists (select 1 from patient_activities pa where "
      + "     pa.prescription_id = pp.patient_presc_id and pa.activity_num = pp.no_of_occurrences "
      + "     and pa.activity_status = 'D')) THEN true "
      + "   WHEN (pmp.priority != 'P' AND pp.no_of_occurrences is not null"
      + "   and (select pa.due_date + interval '1 h' * "
      + "     (serving_window + late_serving_period) from patient_activities pa where "
      + "     pa.prescription_id = pp.patient_presc_id and pa.activity_status in ('S', 'D') "
      + "     AND pa.activity_num = pp.no_of_occurrences) < now()) THEN true "
      + "     ELSE false END) as medication_completed, " + " (Case "
      + " WHEN Exists (select 1 from patient_activities pa1 where pa1.due_date > now() "
      + " and pa1.prescription_id = pp.patient_presc_id and pa1.activity_status != 'X') "
      + " then true else false end) as has_future_activities, pmp.flow_rate, pmp.flow_rate_units, "
      + " pmp.infusion_period, pmp.infusion_period_units,"
      + " pmp.iv_administer_instructions, pp.max_doses, pmp.stopped_user,"
      + " pmp.stopped_reason, pmp.stopped_date, sd.doctor_name as stopped_doctor_name "
      + "FROM patient_prescription pp "
      + "JOIN patient_medicine_prescriptions pmp ON (pp.patient_presc_id=pmp.op_medicine_pres_id "
      + "AND is_discharge_medication = false ) " + "JOIN clinical_preferences on true "
      + "LEFT JOIN consumption_uom_master cum ON (cum.cons_uom_id=pmp.cons_uom_id) "
      + "LEFT JOIN store_item_details sid ON (pmp.medicine_id=sid.medicine_id) "
      + "LEFT JOIN generic_name g ON (pmp.generic_code = g.generic_code) "
      + "LEFT JOIN strength_units su ON (pmp.item_strength_units=su.unit_id) "
      + "LEFT JOIN item_form_master ifm ON (pmp.item_form_id=ifm.item_form_id) "
      + "LEFT JOIN medicine_route mr ON (mr.route_id=pmp.route_of_admin) "
      + "LEFT JOIN doctors d ON (pp.doctor_id = d.doctor_id) "
      + " LEFT JOIN doctors sd ON (pmp.stopped_doctor_id = sd.doctor_id) "
      + "LEFT JOIN recurrence_daily_master rcm "
      + "  on (pp.recurrence_daily_id=rcm.recurrence_daily_id) " + "WHERE pp.visit_id=?";

  public List<BasicDynaBean> getMedications(String patientId) {
    return DatabaseHelper.queryToDynaList(GET_MEDICATIONS, patientId);
  }

  /**
   * Validates the presence of admission request id.
   * @param mrNo the String
   * @param visitId the String
   * @return boolean value
   */
  public boolean isAdmReqPresentInPresc(String mrNo, String visitId) {
    String query = "select * from patient_prescription where visit_id=? and mr_no=? "
            + "and adm_request_id is not null limit 1";
    Object[] temp = new Object[]{visitId,mrNo};
    List<BasicDynaBean> beanList = DatabaseHelper.queryToDynaList(query,temp);
    if (beanList != null && !beanList.isEmpty()) {
      return true;
    }
    return false;
  }
}
