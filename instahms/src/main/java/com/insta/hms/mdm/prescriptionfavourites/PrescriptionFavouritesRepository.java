package com.insta.hms.mdm.prescriptionfavourites;

import com.insta.hms.common.DatabaseHelper;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PrescriptionFavouritesRepository {

  private String docFavPharmaMedPres =
      "SELECT"
          + " dmf.doctor_id, d.doctor_name, dmf.display_order, sid.medicine_name AS item_name, "
          + " sid.medicine_id::text AS item_id, '' AS presc_activity_type, "
          + " dmf.medicine_quantity AS prescribed_qty, "
          + " dmf.favourite_id, dmf.frequency, dmf.strength,"
          + " dmf.medicine_remarks AS item_remarks, cum.consumption_uom,"
          + " dmf.cons_uom_id, g.generic_name, "
          + " g.generic_code,'item_master' AS master, 'Medicine' AS item_type, "
          + " false AS is_package, dmf.route_of_admin AS route_id,"
          + " mr.route_name, sid.prior_auth_required, "
          + " coalesce(dmf.item_form_id, null) AS item_form_id, dmf.item_strength, "
          + " if.item_form_name, 'N' AS tooth_num_required, false AS non_hosp_medicine,"
          + " dmf.duration, dmf.duration_units, dmf.item_strength_units, su.unit_name, "
          + " dmf.admin_strength,"
          + " if.granular_units, sic.item_code, dmf.special_instr,"
          + " COALESCE(cat.insurance_category_id,0) AS insurance_category_id,"
          + " COALESCE(cat.insurance_category_name,'') AS insurance_category_name, "
          + " COALESCE((case when textregexeq(sir.selling_price_expr, '^[0-9]+\\.?[0-9]*$') "
          + " then sir.selling_price_expr::decimal else null end), "
          + " ssd.mrp, sid.item_selling_price, 0) AS charge,"
          + " (COALESCE((case when textregexeq(sir.selling_price_expr, '^[0-9]+\\.?[0-9]*$') "
          + " then sir.selling_price_expr::decimal else null end), "
          + " ssd.mrp, "
          + " sid.item_selling_price, 0) * COALESCE(icm.discount, 0)) / 100 AS discount, "
          + " COALESCE(cat.category_payable,'N') AS category_payable, ssd.qty,"
          + " sid.status, '' AS dept_id, '' AS dept_name, sid.cust_item_code AS order_code, "
          + " sid.issue_base_unit::integer AS issue_base_unit, '' AS category, "
          + " 1 AS category_display_order, sid.control_type_id, "
          + " sid.route_of_admin, dmf.prescription_format, true as applicable, "
          + " 'N' as mandate_clinical_info, '' as clinical_justification,true AS is_prescribable "
          + "FROM doctor_medicine_favourites dmf "
          + "LEFT JOIN doctors d ON (dmf.doctor_id = d.doctor_id) "
          + "JOIN store_item_details sid ON (dmf.medicine_id=sid.medicine_id) "
          + "JOIN LATERAL (select sum(qty) AS qty, max(sibd.mrp) AS mrp "
          + "from store_stock_details ssd, stores s, store_item_batch_details sibd "
          + " where s.dept_id=ssd.dept_id and "
          + " auto_fill_prescriptions and ssd.medicine_id = sid.medicine_id "
          + " AND ssd.item_batch_id=sibd.item_batch_id "
          + " AND s.center_id=? ) AS ssd ON ssd.qty IS NOT NULL "
          + "LEFT JOIN LATERAL (SELECT sic.medicine_id AS medicine_id, sic.insurance_category_id, "
          + " iic.insurance_category_name, ipd.category_payable "
          + " FROM store_items_insurance_category_mapping sic "
          + " JOIN item_insurance_categories iic "
          + "    ON(sic.insurance_category_id = iic.insurance_category_id) "
          + " JOIN insurance_plan_details ipd "
          + "    ON(ipd.insurance_category_id = iic.insurance_category_id "
          + " AND ipd.patient_type = ? AND ipd.plan_id=?) "
          + " WHERE sid.medicine_id = sic.medicine_id "
          + " ORDER BY iic.priority LIMIT 1) as cat ON(cat.medicine_id = sid.medicine_id) "
          + "LEFT JOIN item_form_master if ON (dmf.item_form_id=if.item_form_id) "
          + "LEFT JOIN generic_name g ON (dmf.generic_code = g.generic_code) "
          + "LEFT JOIN medicine_route mr ON (mr.route_id=dmf.route_of_admin) "
          + "JOIN store_category_master icm on sid.med_category_id=icm.category_id "
          + "LEFT JOIN strength_units su ON (su.unit_id=dmf.item_strength_units) "
          + "JOIN organization_details od ON (od.org_id=?) "
          + "LEFT JOIN ha_item_code_type hict ON "
          + "(hict.medicine_id = sid.medicine_id AND hict.health_authority=?) "
          + "LEFT JOIN store_item_codes sic ON "
          + "(sic.medicine_id = hict.medicine_id AND sic.code_type = hict.code_type) "
          + "LEFT JOIN store_item_rates sir ON (sir.medicine_id=sid.medicine_id "
          + " AND sir.store_rate_plan_id=od.store_rate_plan_id) "
          + "LEFT JOIN consumption_uom_master cum ON (dmf.cons_uom_id = cum.cons_uom_id)"
          + "WHERE ('Medicine'=? OR 'All'=?) AND dmf.doctor_id=? AND sid.status='A' "
          + " AND (sid.medicine_name ilike ? OR sid.medicine_name ilike ? "
          + "OR g.generic_name ilike ?) ";

  private String docFavGenericMedPres =
      "SELECT"
          + " dmf.doctor_id, d.doctor_name, dmf.display_order, g.generic_name AS item_name, "
          + " g.generic_code AS item_id, '' AS presc_activity_type,"
          + " dmf.medicine_quantity AS prescribed_qty, "
          + " dmf.favourite_id, dmf.frequency, dmf.strength, "
          + " dmf.medicine_remarks AS item_remarks, cum.consumption_uom, dmf.cons_uom_id,"
          + " g.generic_name, g.generic_code,"
          + " 'item_master' AS master, 'Medicine' AS item_type, false AS is_package, "
          + " dmf.route_of_admin AS route_id,"
          + " mr.route_name, '' AS prior_auth_required, coalesce(dmf.item_form_id, null)"
          + " AS item_form_id,dmf.item_strength, if.item_form_name, 'N' AS tooth_num_required, "
          + " false AS non_hosp_medicine,"
          + " dmf.duration, dmf.duration_units, dmf.item_strength_units, su.unit_name, "
          + " dmf.admin_strength,"
          + " if.granular_units, '' AS item_code, dmf.special_instr, 0 AS insurance_category_id,"
          + " '' AS insurance_category_name, 0 AS charge, 0 AS discount, '' AS category_payable,"
          + " 0 AS qty,g.status, '' AS dept_id, '' AS dept_name, '' AS order_code, "
          + " 0 AS issue_base_unit, '' AS category, 1 AS category_display_order, "
          + " 0 AS control_type_id, '' AS route_of_admin, dmf.prescription_format, "
          + " true as applicable, 'N' as mandate_clinical_info, '' as clinical_justification, "
          + " true AS is_prescribable "
          + "FROM doctor_medicine_favourites dmf "
          + "LEFT JOIN doctors d ON (dmf.doctor_id = d.doctor_id) "
          + "LEFT JOIN item_form_master if ON (dmf.item_form_id=if.item_form_id) "
          + "JOIN generic_name g ON (dmf.generic_code = g.generic_code) "
          + "LEFT JOIN medicine_route mr ON (mr.route_id=dmf.route_of_admin) "
          + "LEFT JOIN strength_units su ON (su.unit_id=dmf.item_strength_units) "
          + "LEFT JOIN consumption_uom_master cum ON (cum.cons_uom_id = dmf.cons_uom_id)"
          + "WHERE ('Medicine'=? OR 'All'=?) AND dmf.doctor_id=? AND g.status='A' "
          + " AND (g.generic_name ilike ? OR g.generic_name ilike ?) ";

  private String docFavOtherMedPres =
      "SELECT "
          + " domf.doctor_id, d.doctor_name, domf.display_order,"
          + " domf.medicine_name AS item_name, '' AS item_id,"
          + " '' AS presc_activity_type, domf.medicine_quantity AS prescribed_qty,"
          + " domf.favourite_id, domf.frequency,"
          + " domf.strength, domf.medicine_remarks AS item_remarks, cum.consumption_uom,"
          + " domf.cons_uom_id,"
          + " g.generic_name, pms.generic_name AS generic_code, 'op' AS master, "
          + " 'Medicine' AS item_type,"
          + " false AS is_package, domf.route_of_admin AS route_id, mr.route_name, "
          + " '' AS prior_auth_required,"
          + " coalesce(domf.item_form_id, null)  AS item_form_id, domf.item_strength, "
          + " ifm.item_form_name,"
          + " 'N' AS tooth_num_required, false AS non_hosp_medicine, domf.duration, "
          + " domf.duration_units,"
          + " domf.item_strength_units, su.unit_name, domf.admin_strength, ifm.granular_units,"
          + " '' AS item_code, domf.special_instr, null AS insurance_category_id, "
          + " '' AS insurance_category_name,"
          + " 0 AS charge, 0 AS discount, '' AS category_payable, null AS qty, pms.status, "
          + " '' AS dept_id,"
          + " '' AS dept_name, '' AS order_code, 0 AS issue_base_unit, '' AS category, "
          + " 1 AS category_display_order, "
          + " 0 AS control_type_id, pms.route_of_admin, domf.prescription_format, "
          + " true as applicable, 'N' as mandate_clinical_info, '' as clinical_justification, "
          + " true AS is_prescribable "
          + "FROM doctor_other_medicine_favourites domf "
          + "LEFT JOIN doctors d ON (domf.doctor_id = d.doctor_id) "
          + "LEFT JOIN item_form_master ifm ON (domf.item_form_id = ifm.item_form_id) "
          + "JOIN prescribed_medicines_master pms ON (domf.medicine_name=pms.medicine_name) "
          + "LEFT JOIN generic_name g ON (pms.generic_name=g.generic_code) "
          + "LEFT JOIN medicine_route mr ON (mr.route_id=domf.route_of_admin) "
          + "LEFT JOIN strength_units su ON (su.unit_id=domf.item_strength_units) "
          + "LEFT JOIN consumption_uom_master cum ON (cum.cons_uom_id = domf.cons_uom_id) "
          + "WHERE ('Medicine'=? OR 'All'=?) AND domf.doctor_id=? AND pms.status='A' "
          + " AND (domf.medicine_name ilike ? OR domf.medicine_name ilike ?) ";

  private String docFavPres =
      " UNION ALL "
          + "SELECT dopf.doctor_id, d.doctor_name, dopf.display_order, "
          + "om.operation_name AS item_name,"
          + " om.op_id AS item_id, '' as presc_activity_type, null AS prescribed_qty,"
          + " dopf.favourite_id, '' AS frequency,"
          + " '' AS strength, dopf.remarks AS item_remarks, '' AS consumption_uom,"
          + " null AS cons_uom_id,"
          + " '' AS generic_name, '' AS generic_code, 'item_master' AS master,"
          + " 'Operation' AS item_type, false AS is_package, null AS route_id, '' AS route_name,"
          + " om.prior_auth_required, null AS item_form_id, '' AS item_strength, "
          + "'' AS item_form_name,"
          + " 'N' AS tooth_num_required, false AS non_hosp_medicine, null AS duration, "
          + " '' AS duration_units,"
          + " null AS item_strength_units, '' AS unit_name, '' AS admin_strength, "
          + "'N' AS granular_units,"
          + " ood.item_code, dopf.special_instr, cat.insurance_category_id, "
          + "cat.insurance_category_name,"
          + " oc.surg_asstance_charge AS charge, oc.surg_asst_discount AS discount, "
          + "cat.category_payable,"
          + " null AS qty, om.status, om.dept_id, dept.dept_name, "
          + " om.operation_code AS order_code, "
          + " 0 AS issue_base_unit, '' AS category, 4 AS category_display_order, "
          + " 0 AS control_type_id, '' AS route_of_admin, '' AS prescription_format, "
          + " ood.applicable, 'N' as mandate_clinical_info, '' as clinical_justification, "
          + " true AS is_prescribable "
          + "FROM doctor_operation_favourites dopf "
          + "LEFT JOIN doctors d ON (dopf.doctor_id = d.doctor_id) "
          + " JOIN operation_master om "
          + "   ON (dopf.operation_id=om.op_id) "
          + " JOIN operation_charges oc "
          + "   ON (om.op_id=oc.op_id AND bed_type=? AND org_id=?) "
          + " JOIN operation_org_details ood "
          + "   ON (ood.operation_id=om.op_id AND ood.org_id=?)"
          + " LEFT JOIN LATERAL (SELECT oic.operation_id AS operation_id,"
          + "      oic.insurance_category_id, "
          + "      iic.insurance_category_name, ipd.category_payable "
          + "      FROM operation_insurance_category_mapping oic "
          + "      JOIN item_insurance_categories iic "
          + " ON (oic.insurance_category_id = iic.insurance_category_id) "
          + " JOIN insurance_plan_details"
          + " ipd ON(ipd.insurance_category_id = iic.insurance_category_id "
          + "        AND ipd.patient_type = ? AND ipd.plan_id=?) "
          + "      WHERE om.op_id = oic.operation_id "
          + "      ORDER BY iic.priority LIMIT 1) as "
          + " cat ON(cat.operation_id = om.op_id) "
          + "JOIN department dept ON (dept.dept_id=om.dept_id) "
          + " WHERE ('Operation'=? OR 'All'=?) AND dopf.doctor_id=? AND om.status='A' "
          + " AND (om.operation_name ilike ? OR om.operation_name ilike "
          + "? OR om.operation_code ilike ?) "
          + " UNION ALL "
          + "SELECT dsf.doctor_id, d.doctor_name, dsf.display_order, s.service_name AS item_name,"
          + " s.service_id AS item_id, '' AS presc_activity_type, "
          + " 1 AS prescribed_qty, dsf.favourite_id, '' AS frequency, '' AS strength,"
          + " dsf.service_remarks AS item_remarks, '' AS consumption_uom, null AS cons_uom_id,"
          + " '' AS generic_name,"
          + " '' AS generic_code,"
          + " 'item_master' AS master, 'Service' AS item_type, false AS is_package, "
          + "null AS route_id, '' AS route_name,"
          + " s.prior_auth_required, null AS item_form_id, '' AS item_strength, "
          + "'' AS item_form_name,"
          + " s.tooth_num_required, false As non_hosp_medicine, null AS duration, "
          + "'' AS duration_units,"
          + " null AS item_strength_units, '' AS unit_name, '' AS admin_strength, "
          + "'N' AS granular_units,"
          + " sod.item_code, dsf.special_instr, cat.insurance_category_id, "
          + "cat.insurance_category_name,"
          + " smc.unit_charge AS charge, smc.discount, cat.category_payable, null AS qty, "
          + "s.status,"
          + " s.serv_dept_id::text AS dept_id, sd.department AS dept_name, "
          + "s.service_code AS order_code, "
          + " 0 AS issue_base_unit, '' AS category, 3 AS category_display_order,"
          + " 0 AS control_type_id, "
          + " '' AS route_of_admin, '' AS prescription_format, sod.applicable,"
          + " 'N' as mandate_clinical_info, '' as clinical_justification, "
          + "true AS is_prescribable "
          + "FROM doctor_service_favourites dsf "
          + "LEFT JOIN doctors d ON (dsf.doctor_id = d.doctor_id) "
          + "JOIN services s ON (s.service_id = dsf.service_id) "
          + "JOIN service_master_charges smc ON (s.service_id=smc.service_id "
          + " AND bed_type=? AND org_id=?) "
          + "JOIN service_org_details sod ON (sod.service_id = smc.service_id AND sod.org_id = ?) "
          + "JOIN services_departments sd ON (s.serv_dept_id=sd.serv_dept_id) "
          + " LEFT JOIN LATERAL (SELECT sic.service_id AS service_id, sic.insurance_category_id, "
          + "      iic.insurance_category_name, ipd.category_payable "
          + "      FROM service_insurance_category_mapping sic "
          + "      JOIN item_insurance_categories iic ON "
          + " (sic.insurance_category_id = iic.insurance_category_id) "
          + "      JOIN insurance_plan_details ipd ON "
          + " (ipd.insurance_category_id = iic.insurance_category_id"
          + " AND ipd.patient_type = ?"
          + " AND ipd.plan_id=?) WHERE s.service_id = sic.service_id "
          + "      ORDER BY iic.priority LIMIT 1) as cat ON(cat.service_id = s.service_id) "
          + "WHERE ('Service'=? OR 'All'=?) AND dsf.doctor_id=? AND s.status='A' "
          + " AND (s.service_name ilike ? OR s.service_name ilike ? OR s.service_code ilike ?) "
          + " UNION ALL "
          + "SELECT dcf.doctor_id, d.doctor_name, dcf.display_order, d.doctor_name AS item_name,"
          + " d.doctor_id AS item_id, 'DOC' AS presc_activity_type,"
          + " null AS prescribed_qty, dcf.favourite_id, '' AS frequency, '' AS strength,"
          + " dcf.consultation_remarks AS item_remarks, '' AS consumption_uom,null AS cons_uom_id,"
          + " '' AS generic_name,"
          + " '' AS generic_code, 'item_master' AS master, 'Doctor' AS item_type, "
          + "false AS is_package,"
          + " null AS route_id, '' AS route_name, '' AS prior_auth_required,"
          + " null AS item_form_id,"
          + " '' AS item_strength, '' AS item_form_name, 'N' AS tooth_num_required, "
          + "false AS non_hosp_medicine,"
          + " null AS duration, '' AS duration_units, null AS item_strength_units,"
          + " '' AS unit_name,"
          + " '' AS admin_strength, 'N' AS granular_units, '' AS item_code, dcf.special_instr,"
          + " ct.insurance_category_id, iic.insurance_category_name, "
          + "cc.charge + docc.op_charge AS charge,"
          + " cc.discount + docc.op_charge_discount AS discount, ipd.category_payable,"
          + " null AS qty,"
          + " d.status, d.dept_id, dept.dept_name, '' AS order_code, 0 AS issue_base_unit, "
          + "'' AS category, "
          + " 5 AS category_display_order, 0 AS control_type_id, '' AS route_of_admin, "
          + " '' AS prescription_format, cod.applicable,"
          + " 'N' as mandate_clinical_info, '' as clinical_justification,true AS is_prescribable "
          + "FROM doctor_consultation_favourites dcf "
          + "JOIN doctors d ON (dcf.cons_doctor_id = d.doctor_id) "
          + "JOIN department dept ON (dept.dept_id=d.dept_id) "
          + "JOIN consultation_charges cc ON (cc.bed_type =? AND cc.org_id=?  "
          + " AND cc.consultation_type_id = -1) "
          + "JOIN consultation_org_details cod ON (cod.consultation_type_id =-1"
          + " AND cod.org_id=cc.org_id) "
          + "JOIN doctor_op_consultation_charge docc ON (docc.doctor_id=? AND docc.org_id=?) "
          + "JOIN consultation_types ct ON (ct.consultation_type_id=-1) "
          + "JOIN item_insurance_categories iic ON "
          + "(iic.insurance_category_id=ct.insurance_category_id) "
          + "LEFT JOIN insurance_plan_details ipd on "
          + "(ct.insurance_category_id = ipd.insurance_category_id "
          + " AND ipd.patient_type=? AND ipd.plan_id=?) "
          + "WHERE ('DOC'=? OR 'All'=?) AND dcf.doctor_id=? AND d.status='A' AND "
          + "(d.doctor_name ilike ? OR d.doctor_name ilike ?) "
          + " UNION ALL "
          + "SELECT dof.doctor_id, d.doctor_name, dof.display_order, dof.item_name, '' AS item_id, "
          + " '' AS presc_activity_type, dof.medicine_quantity AS prescribed_qty,"
          + " dof.favourite_id, dof.frequency, dof.strength, "
          + " dof.item_remarks, cum.consumption_uom, dof.cons_uom_id,"
          + "'' AS generic_name, '' AS generic_code, "
          + "'op' AS master,"
          + " CASE WHEN non_hosp_medicine THEN 'Medicine' ELSE 'NonHospital' END  AS item_type,"
          + " false AS is_package, null AS route_id, '' AS route_name, '' AS prior_auth_required,"
          + " dof.item_form_id, dof.item_strength, if.item_form_name, 'N' AS tooth_num_required,"
          + " dof.non_hosp_medicine, dof.duration, dof.duration_units, dof.item_strength_units,"
          + " su.unit_name, dof.admin_strength, if.granular_units, '' AS item_code, "
          + "dof.special_instr,"
          + " 0 AS insurance_category_id, '' AS insurance_category_name, 0 AS charge,"
          + " 0 AS discount,"
          + " '' AS category_payable, null AS qty, 'A' AS status, '' AS dept_id, "
          + " '' AS dept_name, '' AS order_code, 0 AS issue_base_unit, '' AS category,"
          + " 6 AS category_display_order, "
          + " 0 AS control_type_id, '' AS route_of_admin, '' AS prescription_format, "
          + " true as applicable, 'N' as mandate_clinical_info, '' as clinical_justification, "
          + " true AS is_prescribable "
          + "FROM doctor_other_favourites dof "
          + "LEFT JOIN doctors d ON (dof.doctor_id = d.doctor_id) "
          + "LEFT JOIN item_form_master if ON (dof.item_form_id=if.item_form_id) "
          + "LEFT JOIN strength_units su ON (su.unit_id=dof.item_strength_units) "
          + "LEFT JOIN consumption_uom_master cum ON (cum.cons_uom_id = dof.cons_uom_id) "
          + "WHERE ('NonHospital'=? OR 'All'=?) AND (dof.non_hosp_medicine=? OR ?) AND "
          + "dof.doctor_id=? AND (dof.item_name ilike ? OR dof.item_name ilike ?) "
          + " UNION ALL "
          + "SELECT dtf.doctor_id, doc.doctor_name, dtf.display_order, d.test_name AS item_name, "
          + "dtf.test_id AS item_id, '' AS presc_activity_type, "
          + " null AS prescribed_qty, dtf.favourite_id, '' AS frequency, '' AS strength,"
          + " dtf.test_remarks AS item_remarks, '' AS consumption_uom, null AS cons_uom_id,"
          + " '' AS generic_name, "
          + "'' AS generic_code,"
          + " 'item_master' AS master, 'Inv.' AS item_type, false AS is_package,"
          + " null AS route_id,"
          + " '' AS route_name,"
          + " d.prior_auth_required, null AS item_form_id, '' AS item_strength, "
          + "'' AS item_form_name,"
          + " 'N' AS tooth_num_required, false AS non_hosp_medicine, null AS duration, "
          + "'' AS duration_units,"
          + " null AS item_strength_units, '' AS unit_name, '' AS admin_strength, "
          + "'N' AS granular_units,"
          + " tod.item_code, dtf.special_instr, cat.insurance_category_id,"
          + " cat.insurance_category_name,"
          + " dc.charge, dc.discount, cat.category_payable, null AS qty, d.status, "
          + "d.ddept_id AS dept_id,"
          + " dd.ddept_name AS dept_name, d.diag_code AS order_code, 0 AS issue_base_unit, "
          + "dd.category, "
          + " 2 AS category_display_order, 0 AS control_type_id, '' AS route_of_admin, "
          + " '' AS prescription_format, tod.applicable, d.mandate_clinical_info,"
          + " d.clinical_justification, d.is_prescribable "
          + "FROM doctor_test_favourites dtf "
          + "LEFT JOIN doctors doc ON (doc.doctor_id=dtf.doctor_id) "
          + "JOIN diagnostics d ON (d.test_id=dtf.test_id) "
          + "JOIN diagnostic_charges dc ON (d.test_id=dc.test_id AND dc.bed_type=? "
          + "AND dc.org_name=?) "
          + "JOIN test_org_details tod ON (tod.test_id=d.test_id AND tod.org_id=?) "
          + "JOIN diagnostics_departments dd ON (dd.ddept_id=d.ddept_id) "
          + "LEFT JOIN LATERAL (SELECT dtic.diagnostic_test_id AS test_id, "
          + "      dtic.insurance_category_id, "
          + "      iic.insurance_category_name, ipd.category_payable "
          + "  FROM diagnostic_test_insurance_category_mapping dtic "
          + "  JOIN item_insurance_categories iic "
          + "       ON(dtic.insurance_category_id = iic.insurance_category_id) "
          + "  JOIN insurance_plan_details ipd "
          + "       ON(ipd.insurance_category_id = iic.insurance_category_id "
          + "        AND ipd.patient_type = ? AND ipd.plan_id=?) "
          + "      WHERE d.test_id = dtic.diagnostic_test_id "
          + "      ORDER BY iic.priority LIMIT 1) as cat ON(cat.test_id = d.test_id) "
          + "WHERE ('Inv.'=? OR 'All'=?) AND dtf.doctor_id=? AND d.status='A' "
          + " AND (d.test_name ilike ? OR d.test_name ilike ? OR d.diag_code ilike ?) "
          + " UNION ALL "
          + "SELECT dtf.doctor_id, d.doctor_name, dtf.display_order, pm.package_name AS item_name,"
          + " dtf.test_id AS item_id, '' AS presc_activity_type,"
          + " null AS prescribed_qty, dtf.favourite_id, '' AS frequency, '' AS strength,"
          + " dtf.test_remarks AS item_remarks, '' AS consumption_uom, null AS cons_uom_id,"
          + " '' AS generic_name, "
          + "'' AS generic_code,"
          + " 'item_master' AS master, 'Inv.' AS item_type, true AS is_package,"
          + " null AS route_id, "
          + " '' AS route_name,"
          + " pm.prior_auth_required, null AS item_form_id, '' AS item_strength,"
          + " '' AS item_form_name,"
          + " 'N' AS tooth_num_required, false AS non_hosp_medicine, null AS duration,"
          + " '' AS duration_units,"
          + " null AS item_strength_units, '' AS unit_name, '' AS admin_strength,"
          + " 'N' AS granular_units,"
          + " pod.item_code, dtf.special_instr, pm.insurance_category_id, "
          + " iic.insurance_category_name,"
          + " coalesce(pc.charge, 0) AS charge, coalesce(pc.discount, 0) AS discount,"
          + " ipd.category_payable,"
          + " null AS qty,"
          + " CASE WHEN EXISTS (SELECT * FROM center_package_applicability cpa JOIN"
          + " package_sponsor_master psm ON (psm.pack_id=pm.package_id AND psm.status='A'"
          + " AND (psm.tpa_id=? OR psm.tpa_id = '-1'))"
          + " WHERE cpa.package_id=pm.package_id AND cpa.status='A' "
          + " AND (cpa.center_id=? or cpa.center_id=-1)) AND pm.status='A' "
          + " AND pm.approval_status='A' THEN 'A' ELSE 'I' END "
          + " AS status, '' AS dept_id, '' AS dept_name, pm.package_code AS order_code,"
          + " 0 AS issue_base_unit, "
          + " '' AS category, 2 AS category_display_order, 0 AS control_type_id, "
          + " '' AS route_of_admin, '' AS prescription_format, pod.applicable,"
          + " 'N' as mandate_clinical_info, '' as clinical_justification, "
          + " true AS is_prescribable "
          + "FROM doctor_test_favourites dtf "
          + " LEFT JOIN doctors d ON (d.doctor_id=dtf.doctor_id) "
          + "JOIN packages pm ON (pm.package_id::text=dtf.test_id) "
          + "LEFT JOIN package_charges pc ON (pm.package_id=pc.package_id AND pc.bed_type=? "
          + " AND pc.org_id=?) "
          + "LEFT JOIN pack_org_details pod ON (pod.package_id=pm.package_id AND pod.org_id=?) "
          + "JOIN item_insurance_categories iic"
          + " ON (iic.insurance_category_id=pm.insurance_category_id) "
          + "LEFT JOIN insurance_plan_details ipd"
          + " on (pm.insurance_category_id = ipd.insurance_category_id "
          + " AND ipd.patient_type=? AND ipd.plan_id=?) "
          + "WHERE ('Inv.'=? OR 'All'=?) AND dtf.doctor_id=? AND pm.approval_status='A' "
          + "AND pm.status='A' "
          + " AND (pm.package_name ilike ? OR pm.package_name ilike ?"
          + " OR pm.package_code ilike ?) "
          + "ORDER BY category_display_order, display_order, item_name "
          + "LIMIT ? "
          + "OFFSET ? ";

  /**
   * get consultation prescriptions.
   * @param presType presc type
   * @param doctorId doctor ID
   * @param patientType patient type
   * @param bedType bed type
   * @param orgId org ID
   * @param planId plan ID
   * @param presFromStores pres from stores
   * @param generics generics
   * @param tpaId tpa ID
   * @param centerId center ID
   * @param healthAuthority health authority
   * @param searchQuery search query
   * @param limit limit
   * @param pageNo page number
   * @param nonHospMedicine non hospital medicine
   * @return list of beans
   */
  public List<BasicDynaBean> getPrescriptionsForConsultation(
      String presType,
      String doctorId,
      String patientType,
      String bedType,
      String orgId,
      Integer planId,
      Boolean presFromStores,
      Boolean generics,
      String tpaId,
      Integer centerId,
      String healthAuthority,
      String searchQuery,
      Integer limit,
      Integer pageNo,
      Boolean nonHospMedicine) {

    String query;
    if (presFromStores) {
      if (generics) {
        query = docFavGenericMedPres + docFavPres;
        return DatabaseHelper.queryToDynaList(
            query,
            new Object[] {
              presType,
              presType,
              doctorId,
              searchQuery + "%",
              "% " + searchQuery + "%",
              bedType,
              orgId,
              orgId,
              patientType,
              planId,
              presType,
              presType,
              doctorId,
              searchQuery + "%",
              "% " + searchQuery + "%",
              searchQuery + "%",
              bedType,
              orgId,
              orgId,
              patientType,
              planId,
              presType,
              presType,
              doctorId,
              searchQuery + "%",
              "% " + searchQuery + "%",
              searchQuery + "%",
              bedType,
              orgId,
              doctorId,
              orgId,
              patientType,
              planId,
              presType,
              presType,
              doctorId,
              searchQuery + "%",
              "% " + searchQuery + "%",
              presType,
              presType,
              nonHospMedicine == null ? false : nonHospMedicine,
              nonHospMedicine == null,
              doctorId,
              searchQuery + "%",
              "% " + searchQuery + "%",
              bedType,
              orgId,
              orgId,
              patientType,
              planId,
              presType,
              presType,
              doctorId,
              searchQuery + "%",
              "% " + searchQuery + "%",
              searchQuery + "%",
              tpaId,
              centerId,
              bedType,
              orgId,
              orgId,
              patientType,
              planId,
              presType,
              presType,
              doctorId,
              searchQuery + "%",
              "% " + searchQuery + "%",
              searchQuery + "%",
              limit,
              (pageNo - 1) * limit
            });
      }
      query = docFavPharmaMedPres + docFavPres;
      return DatabaseHelper.queryToDynaList(
          query,
          new Object[] {
            centerId,
            patientType,
            planId,
            orgId,
            healthAuthority,
            presType,
            presType,
            doctorId,
            searchQuery + "%",
            "% " + searchQuery + "%",
            searchQuery + "%",
            bedType,
            orgId,
            orgId,
            patientType,
            planId,
            presType,
            presType,
            doctorId,
            searchQuery + "%",
            "% " + searchQuery + "%",
            searchQuery + "%",
            bedType,
            orgId,
            orgId,
            patientType,
            planId,
            presType,
            presType,
            doctorId,
            searchQuery + "%",
            "% " + searchQuery + "%",
            searchQuery + "%",
            bedType,
            orgId,
            doctorId,
            orgId,
            patientType,
            planId,
            presType,
            presType,
            doctorId,
            searchQuery + "%",
            "% " + searchQuery + "%",
            presType,
            presType,
            nonHospMedicine == null ? false : nonHospMedicine,
            nonHospMedicine == null,
            doctorId,
            searchQuery + "%",
            "% " + searchQuery + "%",
            bedType,
            orgId,
            orgId,
            patientType,
            planId,
            presType,
            presType,
            doctorId,
            searchQuery + "%",
            "% " + searchQuery + "%",
            searchQuery + "%",
            tpaId,
            centerId,
            bedType,
            orgId,
            orgId,
            patientType,
            planId,
            presType,
            presType,
            doctorId,
            searchQuery + "%",
            "% " + searchQuery + "%",
            searchQuery + "%",
            limit,
            (pageNo - 1) * limit
          });
    } else {
      query = docFavOtherMedPres + docFavPres;
      return DatabaseHelper.queryToDynaList(
          query,
          new Object[] {
            presType,
            presType,
            doctorId,
            searchQuery + "%",
            "% " + searchQuery + "%",
            bedType,
            orgId,
            orgId,
            patientType,
            planId,
            presType,
            presType,
            doctorId,
            searchQuery + "%",
            "% " + searchQuery + "%",
            searchQuery + "%",
            bedType,
            orgId,
            orgId,
            patientType,
            planId,
            presType,
            presType,
            doctorId,
            searchQuery + "%",
            "% " + searchQuery + "%",
            searchQuery + "%",
            bedType,
            orgId,
            doctorId,
            orgId,
            patientType,
            planId,
            presType,
            presType,
            doctorId,
            searchQuery + "%",
            "% " + searchQuery + "%",
            presType,
            presType,
            nonHospMedicine == null ? false : nonHospMedicine,
            nonHospMedicine == null,
            doctorId,
            searchQuery + "%",
            "% " + searchQuery + "%",
            bedType,
            orgId,
            orgId,
            patientType,
            planId,
            presType,
            presType,
            doctorId,
            searchQuery + "%",
            "% " + searchQuery + "%",
            searchQuery + "%",
            tpaId,
            centerId,
            bedType,
            orgId,
            orgId,
            patientType,
            planId,
            presType,
            presType,
            doctorId,
            searchQuery + "%",
            "% " + searchQuery + "%",
            searchQuery + "%",
            limit,
            (pageNo - 1) * limit
          });
    }
  }
}
