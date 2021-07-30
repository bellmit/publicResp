/**
 *
 */
package com.insta.hms.master.PrescriptionsMaster;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.HealthAuthorityPreferences.HealthAuthorityPreferencesDAO;
import com.insta.hms.stores.MedicineStockDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author krishna.t
 *
 */
public class PrescriptionsMasterDAO extends GenericDAO{

	public PrescriptionsMasterDAO() {
		super("prescribed_medicines_master");
	}

	private static final String ALL_PHARMA_MEDICINES_PRES_WITHOUT_CHARGES =
		" SELECT pp.prescribed_date, sid.medicine_name as item_name, sid.medicine_id::text as item_id, medicine_quantity, " +
		" 	op_medicine_pres_id as item_prescribed_id, frequency as medicine_dosage, frequency, strength, duration as medicine_days, " +
		"	medicine_remarks as item_remarks, pp.status as issued, cum.consumption_uom, g.generic_name, g.generic_code, 'item_master' as master, " +
		"	'Medicine' as item_type, false as ispackage, activity_due_date, mod_time, mr.route_id, mr.route_name " +
		"  ,icm.category AS category_name,mm.manf_name, mm.manf_mnemonic,0 as lblcount,issue_base_unit, " +
		"	sid.prior_auth_required, coalesce(pmp.item_form_id, 0) as item_form_id, pmp.item_strength, if.item_form_name, " +
		"	'' as test_category, 'N' as tooth_num_required, '' as tooth_unv_number, " +
		"	'' as tooth_fdi_number, 0 as service_qty, '' as service_code, false as non_hosp_medicine, duration, duration_units, " +
		"	pmp.item_strength_units, su.unit_name, " +
		"	pmp.erx_status, pmp.erx_denial_code, pmp.erx_denial_remarks, " +
		"	idc.status as denial_code_status, idc.type as denial_code_type, pmp.admin_strength, if.granular_units, pp.special_instr, " +
		"	idc.code_description as denial_desc, idc.example, '' as preauth_required, '' as dept_name, " +
		"	pmp.send_for_erx, '' as item_code, " +
		// legacy support variabled in print
		"	sid.medicine_name, medicine_remarks, '' as test_name, '' as test_remarks, '' as service_name, '' as service_remarks, " +
		"	'' as cons_doctor_name,  '' as cons_remarks , sic.item_code as drug_code, pmp.refills, sict.control_type_name " +
		" FROM patient_prescription pp" +
		"	JOIN patient_medicine_prescriptions pmp ON (pp.patient_presc_id=pmp.op_medicine_pres_id) " +
			" LEFT JOIN consumption_uom_master cum ON (cum.cons_uom_id=pmp.cons_uom_id)" +
		"	LEFT JOIN store_item_details sid ON (pmp.medicine_id=sid.medicine_id) " +
		" 	LEFT JOIN item_form_master if ON (pmp.item_form_id=if.item_form_id) " +
		"	LEFT OUTER JOIN generic_name g ON (pmp.generic_code = g.generic_code) " +
		"	LEFT OUTER JOIN medicine_route mr ON (mr.route_id=pmp.route_of_admin)" +
		"   LEFT OUTER JOIN manf_master mm on mm.manf_code=sid.manf_name " +
		"   LEFT OUTER JOIN store_category_master icm on sid.med_category_id=icm.category_id " +
		"	LEFT OUTER JOIN strength_units su ON (pmp.item_strength_units=su.unit_id) " +
		"	LEFT JOIN insurance_denial_codes idc ON (idc.denial_code = pmp.erx_denial_code) " +
		//		Drug Code is added
		"   LEFT JOIN ha_item_code_type hict ON (hict.medicine_id = sid.medicine_id AND hict.health_authority= ? ) " +
		"	LEFT JOIN store_item_codes sic ON (sic.medicine_id = hict.medicine_id AND sic.code_type = hict.code_type) " +
   		" 	LEFT JOIN store_item_controltype sict ON (sict.control_type_id = sid.control_type_id)"+

		" WHERE pp.consultation_id=? ";
	private static final String ALL_OP_MEDICINES_PRES_WITHOUT_CHARGES =
		" SELECT pp.prescribed_date, pomp.medicine_name as item_name, '' as item_id, pomp.medicine_quantity, prescription_id as item_prescribed_id," +
		"	pomp.frequency as medicine_dosage, pomp.frequency, pomp.strength, pomp.duration as medicine_days, pomp.medicine_remarks as item_remarks, " +
		"	'P' as issued, pomp.consumption_uom, g.generic_name, " +
		"	g.generic_code, 'op' as master, 'Medicine' as item_type, false as ispackage, pomp.activity_due_date, " +
		"	pomp.mod_time, mr.route_id, mr.route_name ,'' AS category_name,'' as manf_name, '' as manf_mnemonic, " +
		"	0 as lblcount,0 as issue_base_unit, '' as prior_auth_required, coalesce(pomp.item_form_id, 0) as item_form_id, " +
		"	pomp.item_strength, ifm.item_form_name, '' as test_category, " +
		"	'N' as tooth_num_required, '' as tooth_unv_number, '' as tooth_fdi_number, 0 as service_qty, '' as service_code, " +
		"	false as non_hosp_medicine, pomp.duration, pomp.duration_units, pomp.item_strength_units, su.unit_name, " +
		"	'' as erx_status, '' as erx_denial_code, '' as erx_denial_remarks, " +
		"	'' as denial_code_status, '' as denial_code_type, pomp.admin_strength, ifm.granular_units, pp.special_instr, " +
		"	'' as denial_desc, '' as example, '' as preauth_required, '' as dept_name, 'N' as send_for_erx, " +
		"	'' as item_code, "+
		// legacy support variables in print
		"	pomp.medicine_name, pomp.medicine_remarks, '' as test_name, '' as test_remarks, '' as service_name, '' as service_remarks, " +
		"	'' as cons_doctor_name,  '' as cons_remarks , '' as drug_code, pomp.refills, null ascontrol_type_name " +
		" FROM patient_prescription pp" +
		"	JOIN patient_other_medicine_prescriptions pomp ON (pp.patient_presc_id=pomp.prescription_id)" +
		"	LEFT JOIN item_form_master ifm ON (pomp.item_form_id = ifm.item_form_id)" +
		" 	JOIN prescribed_medicines_master pms ON (pomp.medicine_name=pms.medicine_name)"+
		" 	LEFT JOIN strength_units su ON (su.unit_id=pms.item_strength_units) " +
   		" 	LEFT JOIN generic_name g ON (pms.generic_name=g.generic_code)" +
   		"	LEFT JOIN medicine_route mr ON (mr.route_id=pomp.route_of_admin) " +
   		
		" WHERE pp.consultation_id=?";

	private static final String ALL_PRESCRIPTIONS_WITHOUT_CHARGES =
		"	SELECT pp.prescribed_date, atp.test_name, atp.test_id as item_id, 0, op_test_pres_id, '', '', '', 0, test_remarks as item_remarks," +  
		"		pp.status as added_to_bill, '' as consumption_uom, '', '', 'item_master' as master, 'Inv.' as item_type, false as ispackage,  " +
		"		activity_due_date, mod_time, -1 as route_id, '' as route_name,'' AS category_name,'' as manf_name, '' as manf_mnemonic, " + 
		"		0 as lblcount,0 as issue_base_unit, atp.prior_auth_required, 0 as item_form_id, '' as item_strength, '' as item_form_name, " +  
		"		dd.category as test_category, " +  
		"		'N' as tooth_num_required, '' as tooth_unv_number, '' as tooth_fdi_number, 0 as service_qty, '' as service_code, "+  
		"		false as non_hosp_medicine, 0 as duration, '' as duration_units, 0 as item_strength_units, '' as unit_name, " + 
		"		'' as erx_status, '' as erx_denial_code, '' as erx_denial_remarks,  " +
		"		'' as denial_code_status, '' as denial_code_type,  ptp.admin_strength, 'N' as granular_units, pp.special_instr, " +  
		"		'' as denial_desc, '' as example, preauth_required, dd.ddept_name as dept_name, 'N' as send_for_erx, " + 
		"		tod.item_code as item_code, " + 
		
		"		'' as medicine_name, '' as medicine_remarks, atp.test_name, test_remarks, '' as service_name, '' as service_remarks, "+  
		"		'' as cons_doctor_name,  '' as cons_remarks , '' as drug_code, '' as refills, '' as control_type_name  " +
		" 	FROM patient_prescription pp " +
		"		JOIN patient_test_prescriptions ptp ON (pp.patient_presc_id=ptp.op_test_pres_id) " + 
		"		JOIN diagnostics atp ON (atp.test_id = ptp.test_id)  " +
		"		JOIN test_org_details tod ON tod.org_id=? AND atp.test_id=tod.test_id" +
		"		JOIN diagnostics_departments dd ON (dd.ddept_id=atp.ddept_id) " + 
		" 	WHERE pp.consultation_id=?  " +
		 
		" 	UNION ALL " +  

		"	SELECT pp.prescribed_date, atp.package_name as test_name, atp.package_id::text as item_id, 0, op_test_pres_id, '', '', '', 0, " + 
		"		test_remarks as item_remarks, " +  
		"		pp.status as added_to_bill, '' as consumption_uom, '', '', 'item_master' as master, 'Inv.' as item_type, true as ispackage, " +  
		"		activity_due_date, mod_time, -1 as route_id, '' as route_name,'' AS category_name,'' as manf_name, '' as manf_mnemonic, " +
		"		0 as lblcount,0 as issue_base_unit, atp.prior_auth_required, 0 as item_form_id, '' as item_strength, '' as item_form_name, " + 
		"		'' as test_category,  " +
		"		'N' as tooth_num_required, '' as tooth_unv_number, '' as tooth_fdi_number, 0 as service_qty, '' as service_code, " +  
		"		false as non_hosp_medicine, 0 as duration, '' as duration_units, 0 as item_strength_units, '' as unit_name, " +
		"		'' as erx_status, '' as erx_denial_code, '' as erx_denial_remarks, " +  
		"		'' as denial_code_status, '' as denial_code_type,  ptp.admin_strength, 'N' as granular_units, pp.special_instr, " +  
		"		'' as denial_desc, '' as example, preauth_required, null as dept_name, 'N' as send_for_erx, " +
		"		pod.item_code, " + 
		
		"		'' as medicine_name, '' as medicine_remarks, atp.package_name, test_remarks, '' as service_name, '' as service_remarks, " +  
		"		'' as cons_doctor_name,  '' as cons_remarks , '' as drug_code, '' as refills, '' as control_type_name " +
		" 	FROM patient_prescription pp " + 
		"		JOIN patient_test_prescriptions ptp ON (pp.patient_presc_id=ptp.op_test_pres_id) " + 
		"		JOIN packages atp ON (ptp.ispackage=true and atp.package_id = ptp.test_id::integer)  " +
		"		JOIN pack_org_details pod ON atp.package_id::integer=pod.package_id AND pod.org_id=? " +   
		"	WHERE pp.consultation_id=? " +  

		"	UNION ALL " +

		"	SELECT pp.prescribed_date, s.service_name as item_name, s.service_id as item_id, 0, op_service_pres_id, '', '',  '', 0, " +  
		"		service_remarks as item_remarks, pp.status as added_to_bill, '' as consumption_uom, '', '', 'item_master' as master, 'Service' as item_type, " + 
		"		false as ispackage, activity_due_date, mod_time, -1 as  route_id, '' as route_name,'' AS category_name,'' as manf_name, " +
		"		'' as manf_mnemonic, 0 as lblcount,0 as issue_base_unit, s.prior_auth_required, 0 as item_form_id, " +
		"		'' as item_strength, '' as item_form_name, '' as test_category, " +  
		"		s.tooth_num_required, psp.tooth_unv_number, psp.tooth_fdi_number, qty as service_qty, service_code, " +  
		"		false as non_hosp_medicine, 0 as duration, '' as duration_units, 0 as item_strength_units, '' as unit_name, " +  
		"		'' as erx_status, '' as erx_denial_code, '' as erx_denial_remarks, " + 
		"		'' as denial_code_status, '' as denial_code_type,  psp.admin_strength, 'N' as granular_units, pp.special_instr, " +  
		"		'' as denial_desc, '' as example, preauth_required, sd.department as dept_name, 'N' as send_for_erx, " + 
		"		sod.item_code, " +
		
		"		'' as medicine_name, '' as medicine_remarks, '' as test_name, '' as test_remarks, s.service_name, service_remarks, " +  
		"		'' as cons_doctor_name,  '' as cons_remarks , '' as drug_code, '' as refills, '' as control_type_name " +
		" 	FROM patient_prescription pp " + 
		"		JOIN patient_service_prescriptions psp ON (pp.patient_presc_id=psp.op_service_pres_id) " +  
		"		JOIN services s ON (s.service_id = psp.service_id)  " + 
		"		JOIN services_departments sd ON (sd.serv_dept_id=s.serv_dept_id) " +  
		"   	JOIN service_org_details sod ON sod.org_id=? AND sod.service_id=s.service_id " +  
		" 	WHERE pp.consultation_id=?  " +

		"	UNION ALL  " +

		"	SELECT pp.prescribed_date, d.doctor_name as item_name, d.doctor_id, 0, pcp.prescription_id, '', '',  '', 0, cons_remarks as item_remarks, " +  
		"		pp.status as added_to_bill, '' as consumption_uom, '', '', 'item_master' as master, 'Doctor' as item_type, false as ispackage,  " +
		"		activity_due_date, mod_time, -1 as route_id, '' as route_name,'' AS category_name,'' as manf_name, '' as manf_mnemonic,  " +
		"		0 as lblcount,0 as issue_base_unit, '' as prior_auth_required, 0 as item_form_id, '' as item_strength, '' as item_form_name, " +  
		"		'' as test_category, 'N' as tooth_num_required, '' as tooth_unv_number,  " +
		"		'' as tooth_fdi_number, 0 as service_qty, '' as service_code, false as non_hosp_medicine, 0 as duration, '' as duration_units, " +  
		"		0 as item_strength_units, '' as unit_name,  " +
		"		'' as erx_status, '' as erx_denial_code, '' as erx_denial_remarks, " +  
		"		'' as denial_code_status, '' as denial_code_type,  pcp.admin_strength, 'N' as granular_units, pp.special_instr, " +  
		"		'' as denial_desc, '' as example, preauth_required, dept.dept_name, 'N' as send_for_erx,  " +
		"		'' as item_code, " +
		
		"		'' as medicine_name, '' as medicine_remarks, '' as test_name, '' as test_remarks, '' as service_name, '' as service_remarks, " +  
		"		d.doctor_name as cons_doctor_name,  cons_remarks , '' as drug_code, '' as refills, '' as control_type_name  " +
		" 	FROM patient_prescription pp  " +
		"		JOIN patient_consultation_prescriptions pcp ON (pp.patient_presc_id=pcp.prescription_id) " +  
		"		JOIN doctors d ON (pcp.doctor_id = d.doctor_id)  " +
		"		JOIN department dept ON (dept.dept_id=d.dept_id) " +
		" 	WHERE pp.consultation_id=?  " +

		" 	UNION ALL  " +

		" 	SELECT pp.prescribed_date, item_name, '' as item_id, medicine_quantity, prescription_id, frequency as medicine_dosage, frequency, " +
		"		strength, duration as medicine_days, " +  
		"		item_remarks, 'P' as issued, cum.consumption_uom, '', '', 'op' as master, " +  
		"		case when non_hosp_medicine then 'Medicine' else 'NonHospital' end as item_type, false as ispackage, " +  
		"		activity_due_date, mod_time, -1 as route_id, '' as route_name,'' AS category_name,'' as manf_name, '' as manf_mnemonic, " +  
		"		0 as lblcount,0 as issue_base_unit, '' as prior_auth_required, pop.item_form_id, pop.item_strength, if.item_form_name, " + 
		"		'' as test_category, 'N' as tooth_num_required, '' as tooth_unv_number,  " +
		"		'' as tooth_fdi_number, 0 as service_qty, '' as service_code, non_hosp_medicine, duration, duration_units, " +  
		"		item_strength_units, su.unit_name,  " +
		"		'' as erx_status, '' as erx_denial_code, '' as erx_denial_remarks, " +  
		"		'' as denial_code_status, '' as denial_code_type,  pop.admin_strength, if.granular_units, pp.special_instr, " +  
		"		'' as denial_desc, '' as example, '' as preauth_required, '' as dept_name, 'N' as send_for_erx,  " +
		"		'' as item_code, " +
		
		"		'' as medicine_name, '' as medicine_remarks, '' as test_name, '' as test_remarks, '' as service_name, '' as service_remarks, " +  
		"		'' as cons_doctor_name,  '' as cons_remarks , '' as drug_code, pop.refills as refills, '' as control_type_name  " +
		" 	FROM patient_prescription pp  " +
		"		JOIN patient_other_prescriptions pop ON (pp.patient_presc_id=pop.prescription_id) " +  
		" 		LEFT JOIN strength_units su ON (su.unit_id=pop.item_strength_units)  " +
		"		LEFT JOIN item_form_master if ON (pop.item_form_id=if.item_form_id) " +
		"       LEFT JOIN consumption_uom_master cum ON (cum.cons_uom_id = pop.cons_uom_id) " +
		" 	WHERE pp.consultation_id=?  " +

		" 	UNION ALL  " +

		" 	SELECT pp.prescribed_date, om.operation_name as item_name, om.op_id as item_id, 0, prescription_id, '', '', '', 0, " + 
		"		pop.remarks as item_remarks, pp.status as added_to_bill, '' as consumption_uom, '', '', 'item_master' as master, 'Operation' as item_type, " +  
		"		false as ispackage, null::date as activity_due_date, mod_time, -1 as route_id, '' as route_name, " +
		"		'' AS category_name,'' as manf_name, '' as manf_mnemonic,0 as lblcount,0 as issue_base_unit, om.prior_auth_required, " +  
		"		0 as item_form_id, '' as item_strength, '' as item_form_name,  " +
		"		'' as test_category, 'N' as tooth_num_required, '' as tooth_unv_number, " +  
		"		'' as tooth_fdi_number, 0 as service_qty, '' as service_code, false as non_hosp_medicine, 0 as duration, 'D' as duration_units, " +  
		"		0 as item_strength_units, '' as master_item_strength_units,  " +
		"		'' as erx_status, '' as erx_denial_code, '' as erx_denial_remarks, " + 
		"		'' as denial_code_status, '' as denial_code_type,  pop.admin_strength, 'N' as granular_units, pp.special_instr, " +  
		"		'' as denial_desc, '' as example, preauth_required, dept.dept_name, 'N' as send_for_erx,  " +
		"		ood.item_code, " +
		
		"		'' as medicine_name, '' as medicine_remarks, '' as test_name, '' as test_remarks, '' as service_name, '' as service_remarks, " +  
		"		'' as cons_doctor_name,  '' as cons_remarks , '' as drug_code, '' as refills, '' as control_type_name " +  
		" 	FROM patient_prescription pp  " +
		"		JOIN patient_operation_prescriptions pop ON (pp.patient_presc_id=pop.prescription_id) " + 
		"		JOIN operation_master om ON (pop.operation_id=om.op_id) " +	 
		"		JOIN department dept ON (dept.dept_id=om.dept_id) " +  
		"		JOIN operation_org_details ood ON ood.org_id=? AND ood.operation_id=om.op_id " +
		" 	WHERE pp.consultation_id=?  " +

		" 	ORDER BY item_type, item_prescribed_id " ;

	public static List getAllPrescriptionsWithoutCharges(int consultationId, String patientId, String use_store_items, Map patDetMap) throws SQLException {
		use_store_items = use_store_items == null ? "" : use_store_items;
		Connection con = null;
		PreparedStatement ps = null;
		try {
			if (patDetMap == null) 
				patDetMap =  com.insta.hms.Registration.VisitDetailsDAO.getPatientVisitDetailsMap(patientId);
			int centerId = (Integer) patDetMap.get("center_id");
			String helathAuthority = CenterMasterDAO.getHealthAuthorityForCenter(centerId);

			con = DataBaseUtil.getConnection();
			String query = (use_store_items.equals("Y") ? ALL_PHARMA_MEDICINES_PRES_WITHOUT_CHARGES : ALL_OP_MEDICINES_PRES_WITHOUT_CHARGES);
			query += " UNION " +ALL_PRESCRIPTIONS_WITHOUT_CHARGES;
			ps = con.prepareStatement(query);

			int index = 1 ;
			if (use_store_items.equals("Y"))
				ps.setString(index++, helathAuthority);

			ps.setInt(index++, consultationId);

			String orgId = (String) patDetMap.get("org_id");
			orgId = orgId == null || orgId.equals("") ? "GENERAL" : orgId;
			String bedType = (String) patDetMap.get("alloc_bed_type");
			bedType = bedType == null || bedType.equals("") ? (String) patDetMap.get("bill_bed_type") : bedType;
			bedType = bedType == null || bedType.equals("") ? "GENERAL" : bedType;

			// test related place holders
			ps.setString(index++, orgId);
			ps.setInt(index++, consultationId);
			
			// package related place holders
			ps.setString(index++, orgId);
			ps.setInt(index++, consultationId);

			// service related place holders
			ps.setString(index++, orgId);
			ps.setInt(index++, consultationId);

			ps.setInt(index++, consultationId);
			ps.setInt(index++, consultationId);

			ps.setString(index++, orgId);
			ps.setInt(index++, consultationId);

			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	private static final String ALL_PHARMA_MEDICINES_PRES =
			" SELECT pp.prescribed_date, sid.medicine_name as item_name, sid.medicine_id::text as item_id, medicine_quantity, " +
			" 	op_medicine_pres_id as item_prescribed_id, frequency as medicine_dosage, frequency, strength, duration as medicine_days, " +
			"	medicine_remarks as item_remarks, pp.status as issued, pmp.cons_uom_id, cum.consumption_uom, g.generic_name, g.generic_code, 'item_master' as master, " +
			"	'Medicine' as item_type, false as ispackage, activity_due_date, mod_time, mr.route_id, mr.route_name " +
			"  ,icm.category AS category_name,mm.manf_name, mm.manf_mnemonic,0 as lblcount,issue_base_unit, " +
			"	sid.prior_auth_required, coalesce(pmp.item_form_id, 0) as item_form_id, pmp.item_strength, if.item_form_name, " +
			"	0 as charge, 0 as discount, '' as test_category, 'N' as tooth_num_required, '' as tooth_unv_number, " +
			"	'' as tooth_fdi_number, 0 as service_qty, '' as service_code, false as non_hosp_medicine, duration, duration_units, " +
			"	pmp.item_strength_units, su.unit_name, " +
			"	pmp.erx_status, pmp.erx_denial_code, pmp.erx_denial_remarks, " +
			"	idc.status as denial_code_status, idc.type as denial_code_type, pmp.admin_strength, if.granular_units, pp.special_instr, " +
			"	idc.code_description as denial_desc, idc.example, '' as preauth_required, '' as dept_name, " +
			"	pmp.send_for_erx, '' as item_code, " +
			// legacy support variabled in print
			"	sid.medicine_name, medicine_remarks, '' as test_name, '' as test_remarks, '' as service_name, '' as service_remarks, " +
			"	'' as cons_doctor_name,  '' as cons_remarks , sic.item_code as drug_code, pmp.refills, " +
			" pmp.time_of_intake, pp.start_datetime as start_date , pp.end_datetime as end_date, pmp.priority, sict.control_type_name " +
			" FROM patient_prescription pp" +
			"	JOIN patient_medicine_prescriptions pmp ON (pp.patient_presc_id=pmp.op_medicine_pres_id) " +
			"	LEFT JOIN store_item_details sid ON (pmp.medicine_id=sid.medicine_id) " +
			" 	LEFT JOIN item_form_master if ON (pmp.item_form_id=if.item_form_id) " +
			"	LEFT OUTER JOIN generic_name g ON (pmp.generic_code = g.generic_code) " +
			"	LEFT OUTER JOIN medicine_route mr ON (mr.route_id=pmp.route_of_admin)" +
			"   LEFT OUTER JOIN manf_master mm on mm.manf_code=sid.manf_name " +
			"   LEFT OUTER JOIN store_category_master icm on sid.med_category_id=icm.category_id " +
			"	LEFT OUTER JOIN strength_units su ON (pmp.item_strength_units=su.unit_id) " +
			"	LEFT JOIN insurance_denial_codes idc ON (idc.denial_code = pmp.erx_denial_code) " +
//			Drug Code is added
			"   LEFT JOIN ha_item_code_type hict ON (hict.medicine_id = sid.medicine_id AND hict.health_authority= ? ) " +
			"	LEFT JOIN store_item_codes sic ON (sic.medicine_id = hict.medicine_id AND sic.code_type = hict.code_type) " +
	   		" 	LEFT JOIN store_item_controltype sict ON (sict.control_type_id = sid.control_type_id)"+
	   		"   LEFT JOIN consumption_uom_master cum ON (cum.cons_uom_id = pmp.cons_uom_id) " +
			" WHERE pp.consultation_id=? ";
		private static final String ALL_OP_MEDICINES_PRES =
			" SELECT pp.prescribed_date, pomp.medicine_name as item_name, '' as item_id, pomp.medicine_quantity, prescription_id as item_prescribed_id," +
			"	pomp.frequency as medicine_dosage, pomp.frequency, pomp.strength, pomp.duration as medicine_days, pomp.medicine_remarks as item_remarks, " +
			"	'P' as issued, pomp.cons_uom_id, cum.consumption_uom, g.generic_name, " +
			"	g.generic_code, 'op' as master, 'Medicine' as item_type, false as ispackage, pomp.activity_due_date, " +
			"	pomp.mod_time, mr.route_id, mr.route_name ,'' AS category_name,'' as manf_name, '' as manf_mnemonic, " +
			"	0 as lblcount,0 as issue_base_unit, '' as prior_auth_required, coalesce(pomp.item_form_id, 0) as item_form_id, " +
			"	pomp.item_strength, ifm.item_form_name, 0 as charge, 0 as discount, '' as test_category, " +
			"	'N' as tooth_num_required, '' as tooth_unv_number, '' as tooth_fdi_number, 0 as service_qty, '' as service_code, " +
			"	false as non_hosp_medicine, pomp.duration, pomp.duration_units, pomp.item_strength_units, su.unit_name, " +
			"	'' as erx_status, '' as erx_denial_code, '' as erx_denial_remarks, " +
			"	'' as denial_code_status, '' as denial_code_type, pomp.admin_strength, ifm.granular_units, pp.special_instr, " +
			"	'' as denial_desc, '' as example, '' as preauth_required, '' as dept_name, 'N' as send_for_erx, " +
			"	'' as item_code, "+
			// legacy support variables in print
			"	pomp.medicine_name, pomp.medicine_remarks, '' as test_name, '' as test_remarks, '' as service_name, '' as service_remarks, " +
			"	'' as cons_doctor_name,  '' as cons_remarks , '' as drug_code, pomp.refills, " +
			" pomp.time_of_intake, pp.start_datetime as start_date , pp.end_datetime as end_date, pomp.priority, sic.control_type_name " +
			" FROM patient_prescription pp" +
			"	JOIN patient_other_medicine_prescriptions pomp ON (pp.patient_presc_id=pomp.prescription_id)" +
			"	LEFT JOIN item_form_master ifm ON (pomp.item_form_id = ifm.item_form_id)" +
			" 	JOIN prescribed_medicines_master pms ON (pomp.medicine_name=pms.medicine_name)"+
			" 	LEFT JOIN strength_units su ON (su.unit_id=pms.item_strength_units) " +
	   		" 	LEFT JOIN generic_name g ON (pms.generic_name=g.generic_code)" +
	   		"	LEFT JOIN medicine_route mr ON (mr.route_id=pomp.route_of_admin) " +
	   		"   LEFT JOIN patient_medicine_prescriptions pmp ON (pp.patient_presc_id=pmp.op_medicine_pres_id) "+
	   		"   LEFT JOIN store_item_details sid ON (sid.medicine_id=pmp.medicine_id) "+
	   		" 	LEFT JOIN store_item_controltype sic ON (sic.control_type_id = sid.control_type_id)"+
	   		"   LEFT JOIN consumption_uom_master cum ON (cum.cons_uom_id = pomp.cons_uom_id) " +
			" WHERE pp.consultation_id=?";

		private static final String ALL_PRESCRIPTIONS =
			" SELECT pp.prescribed_date, atp.test_name, atp.test_id as item_id, 0, op_test_pres_id, '', '', '', 0, test_remarks as item_remarks, " +
			"	pp.status as added_to_bill, null as cons_uom_id, '' as consumption_uom, '', '', 'item_master' as master, 'Inv.' as item_type, ispkg as ispackage, " +
			"	activity_due_date, mod_time, -1 as route_id, '' as route_name,'' AS category_name,'' as manf_name, '' as manf_mnemonic," +
			"	0 as lblcount,0 as issue_base_unit, atp.prior_auth_required, 0 as item_form_id, '' as item_strength, '' as item_form_name, " +
			"	coalesce(dc.charge, pc.charge, 0) as charge, coalesce(dc.discount, pc.discount, 0) as discount, dd.category as test_category, " +
			"	'N' as tooth_num_required, '' as tooth_unv_number, '' as tooth_fdi_number, 0 as service_qty, '' as service_code, " +
			"	false as non_hosp_medicine, 0 as duration, '' as duration_units, 0 as item_strength_units, '' as unit_name," +
			"	'' as erx_status, '' as erx_denial_code, '' as erx_denial_remarks, " +
			"	'' as denial_code_status, '' as denial_code_type,  ptp.admin_strength, 'N' as granular_units, pp.special_instr, " +
			"	'' as denial_desc, '' as example, preauth_required, dd.ddept_name as dept_name, 'N' as send_for_erx, " +
			"	coalesce(tod.item_code, pod.item_code) as item_code, "+
			//	legacy support variables in print
			"	'' as medicine_name, '' as medicine_remarks, atp.test_name, test_remarks, '' as service_name, '' as service_remarks, " +
			"	'' as cons_doctor_name,  '' as cons_remarks , '' as drug_code, '' as refills, "+
			" '' AS time_of_intake, pp.start_datetime as start_date , pp.end_datetime as end_date, ptp.priority, '' as control_type_name " +
			" FROM patient_prescription pp" +
			"	JOIN patient_test_prescriptions ptp ON (pp.patient_presc_id=ptp.op_test_pres_id)" +
			"	JOIN all_tests_pkgs_view atp ON (atp.test_id = ptp.test_id) " +
			"	LEFT OUTER JOIN test_org_details tod ON tod.org_id=? AND " +
			"		atp.test_id=tod.test_id " +
			"	LEFT OUTER JOIN pack_org_details pod ON atp.test_id=pod.package_id::text AND pod.org_id=?  " +
			"	LEFT JOIN diagnostics_departments dd ON (dd.ddept_id=atp.ddept_id)" +
			"	LEFT JOIN diagnostic_charges dc ON (dc.test_id=atp.test_id and dc.org_name=? and dc.bed_type=?) " +
			"   LEFT JOIN package_charges pc ON (pc.package_id::text=atp.test_id and pc.org_id=? and pc.bed_type=?) " +
			" WHERE pp.consultation_id=? " +
			" UNION ALL " +

			" SELECT pp.prescribed_date, s.service_name as item_name, s.service_id as item_id, 0, op_service_pres_id, '', '',  '', 0, " +
			"	service_remarks as item_remarks, pp.status as added_to_bill, null as cons_uom_id, '' as consumption_uom, '', '', 'item_master' as master, 'Service' as item_type, " +
			"	false as ispackage, activity_due_date, mod_time, -1 as  route_id, '' as route_name,'' AS category_name,'' as manf_name, " +
			"	'' as manf_mnemonic, 0 as lblcount,0 as issue_base_unit, s.prior_auth_required, 0 as item_form_id, " +
			"	'' as item_strength, '' as item_form_name, smc.unit_charge as charge, smc.discount as discount, '' as test_category, " +
			"	s.tooth_num_required, psp.tooth_unv_number, psp.tooth_fdi_number, qty as service_qty, service_code, " +
			"	false as non_hosp_medicine, 0 as duration, '' as duration_units, 0 as item_strength_units, '' as unit_name, " +
			"	'' as erx_status, '' as erx_denial_code, '' as erx_denial_remarks, " +
			"	'' as denial_code_status, '' as denial_code_type,  psp.admin_strength, 'N' as granular_units, pp.special_instr, " +
			"	'' as denial_desc, '' as example, preauth_required, sd.department as dept_name, 'N' as send_for_erx, " +
			"	sod.item_code, "+
			//	legacy support variables in print
			"	'' as medicine_name, '' as medicine_remarks, '' as test_name, '' as test_remarks, s.service_name, service_remarks, " +
			"	'' as cons_doctor_name,  '' as cons_remarks , '' as drug_code, '' as refills, " +
			" '' AS time_of_intake, pp.start_datetime as start_date , pp.end_datetime as end_date, psp.priority, '' as control_type_name " +
			" FROM patient_prescription pp " +
			"	JOIN patient_service_prescriptions psp ON (pp.patient_presc_id=psp.op_service_pres_id) " +
			"	JOIN services s ON (s.service_id = psp.service_id) " +
			" 	JOIN services_departments sd ON (sd.serv_dept_id=s.serv_dept_id) " +
			"   JOIN service_master_charges smc ON (smc.service_id=s.service_id and org_id=? and bed_type=?) " +
			" 	LEFT JOIN service_org_details sod ON sod.org_id=? AND sod.service_id=s.service_id " +
			" WHERE pp.consultation_id=? " +

			" UNION ALL " +
			" SELECT pp.prescribed_date, d.doctor_name as item_name, d.doctor_id, 0, pcp.prescription_id, '', '',  '', 0, cons_remarks as item_remarks, " +
			"	pp.status as added_to_bill, null as cons_uom_id, '' as consumption_uom, '', '', 'item_master' as master, 'Doctor' as item_type, false as ispackage, " +
			"	activity_due_date, mod_time, -1 as route_id, '' as route_name,'' AS category_name,'' as manf_name, '' as manf_mnemonic, " +
			"	0 as lblcount,0 as issue_base_unit, '' as prior_auth_required, 0 as item_form_id, '' as item_strength, '' as item_form_name, " +
			"	0 as charge, 0 as discount, '' as test_category, 'N' as tooth_num_required, '' as tooth_unv_number, " +
			"	'' as tooth_fdi_number, 0 as service_qty, '' as service_code, false as non_hosp_medicine, 0 as duration, '' as duration_units, " +
			"	0 as item_strength_units, '' as unit_name, " +
			"	'' as erx_status, '' as erx_denial_code, '' as erx_denial_remarks, " +
			"	'' as denial_code_status, '' as denial_code_type,  pcp.admin_strength, 'N' as granular_units, pp.special_instr, " +
			"	'' as denial_desc, '' as example, preauth_required, dept.dept_name, 'N' as send_for_erx, " +
			"	'' as item_code, "+
			//	legacy support variables in print
			"	'' as medicine_name, '' as medicine_remarks, '' as test_name, '' as test_remarks, '' as service_name, '' as service_remarks, " +
			"	d.doctor_name as cons_doctor_name,  cons_remarks , '' as drug_code, '' as refills, " +
			" '' AS time_of_intake, pp.start_datetime as start_date , pp.end_datetime as end_date, '' AS priority, '' as control_type_name " +
			" FROM patient_prescription pp " +
			"	JOIN patient_consultation_prescriptions pcp ON (pp.patient_presc_id=pcp.prescription_id) " +
			"	JOIN doctors d ON (pcp.doctor_id = d.doctor_id) " +
			"	JOIN department dept ON (dept.dept_id=d.dept_id)" +
			" WHERE pp.consultation_id=? " +

			" UNION ALL " +
			" SELECT pp.prescribed_date, item_name, '' as item_id, medicine_quantity, prescription_id, frequency as medicine_dosage, frequency, strength, duration as medicine_days, " +
			"	item_remarks, 'P' as issued, pop.cons_uom_id, cum.consumption_uom, '', '', 'op' as master, " +
			"	case when non_hosp_medicine then 'Medicine' else 'NonHospital' end as item_type, false as ispackage, " +
			"	activity_due_date, mod_time, -1 as route_id, '' as route_name,'' AS category_name,'' as manf_name, '' as manf_mnemonic , " +
			"	0 as lblcount,0 as issue_base_unit, '' as prior_auth_required, pop.item_form_id, pop.item_strength, if.item_form_name, " +
			"	0 as charge, 0 as discount,  '' as test_category, 'N' as tooth_num_required, '' as tooth_unv_number, " +
			"	'' as tooth_fdi_number, 0 as service_qty, '' as service_code, non_hosp_medicine, duration, duration_units, " +
			"	item_strength_units, su.unit_name, " +
			"	'' as erx_status, '' as erx_denial_code, '' as erx_denial_remarks, " +
			"	'' as denial_code_status, '' as denial_code_type,  pop.admin_strength, if.granular_units, pp.special_instr, " +
			"	'' as denial_desc, '' as example, '' as preauth_required, '' as dept_name, 'N' as send_for_erx, " +
			"	'' as item_code, "+
			// legacy support variables in print
			"	'' as medicine_name, '' as medicine_remarks, '' as test_name, '' as test_remarks, '' as service_name, '' as service_remarks, " +
			"	'' as cons_doctor_name,  '' as cons_remarks , '' as drug_code, pop.refills as refills, " +
			" pop.time_of_intake, pp.start_datetime as start_date , pp.end_datetime as end_date, pop.priority, '' as control_type_name " +
			" FROM patient_prescription pp " +
			"	JOIN patient_other_prescriptions pop ON (pp.patient_presc_id=pop.prescription_id) " +
			" 	LEFT JOIN strength_units su ON (su.unit_id=pop.item_strength_units) " +
			"	LEFT JOIN item_form_master if ON (pop.item_form_id=if.item_form_id)" +
			"   LEFT JOIN consumption_uom_master cum ON (pop.cons_uom_id = cum.cons_uom_id)" +
			" WHERE pp.consultation_id=? " +

			" UNION ALL " +
			" SELECT pp.prescribed_date, om.operation_name as item_name, om.op_id as item_id, 0, prescription_id, '', '', '', 0," +
			"	pop.remarks as item_remarks, pp.status as added_to_bill, null as cons_uom_id, '' as consumption_uom, '', '', 'item_master' as master, 'Operation' as item_type, " +
			"	false as ispackage, null::date as activity_due_date, mod_time, -1 as route_id, '' as route_name," +
			"	'' AS category_name,'' as manf_name, '' as manf_mnemonic,0 as lblcount,0 as issue_base_unit, om.prior_auth_required, " +
			"	0 as item_form_id, '' as item_strength, '' as item_form_name, surg_asstance_charge as charge, " +
			"	surg_asst_discount as discount, '' as test_category, 'N' as tooth_num_required, '' as tooth_unv_number, " +
			"	'' as tooth_fdi_number, 0 as service_qty, '' as service_code, false as non_hosp_medicine, 0 as duration, 'D' as duration_units, " +
			"	0 as item_strength_units, '' as master_item_strength_units, " +
			"	'' as erx_status, '' as erx_denial_code, '' as erx_denial_remarks, " +
			"	'' as denial_code_status, '' as denial_code_type,  pop.admin_strength, 'N' as granular_units, pp.special_instr, " +
			"	'' as denial_desc, '' as example, preauth_required, dept.dept_name, 'N' as send_for_erx, " +
			"	ood.item_code, "+
			// legacy support variables in print
			"	'' as medicine_name, '' as medicine_remarks, '' as test_name, '' as test_remarks, '' as service_name, '' as service_remarks, " +
			"	'' as cons_doctor_name,  '' as cons_remarks , '' as drug_code, '' as refills, " +
			" '' AS time_of_intake, pp.start_datetime as start_date , pp.end_datetime as end_date, '' AS priority, '' as control_type_name " +
			" FROM patient_prescription pp " +
			"	JOIN patient_operation_prescriptions pop ON (pp.patient_presc_id=pop.prescription_id)" +
			"	JOIN operation_master om ON (pop.operation_id=om.op_id)	" +
			"	JOIN department dept ON (dept.dept_id=om.dept_id) " +
			"	JOIN operation_charges oc ON (oc.op_id=pop.operation_id and org_id=? and bed_type=?) " +
			" 	LEFT JOIN operation_org_details ood ON ood.org_id=? AND ood.operation_id=om.op_id "+
			" WHERE pp.consultation_id=? " +

			" ORDER BY item_type, item_prescribed_id ";
		public static List getAllPrescriptions(int consultationId, String patientId, String use_store_items, Map patDetMap) throws SQLException {
			use_store_items = use_store_items == null ? "" : use_store_items;
			Connection con = null;
			PreparedStatement ps = null;
			try {
				java.util.Map  patient =  com.insta.hms.Registration.VisitDetailsDAO.getPatientVisitDetailsMap(patientId);
				int centerId = (Integer) patient.get("center_id");
				String helathAuthority = CenterMasterDAO.getHealthAuthorityForCenter(centerId);

				con = DataBaseUtil.getConnection();
				String query = (use_store_items.equals("Y") ? ALL_PHARMA_MEDICINES_PRES : ALL_OP_MEDICINES_PRES);
				query += " UNION " +ALL_PRESCRIPTIONS;
				ps = con.prepareStatement(query);

				int index = 1 ;
				if (use_store_items.equals("Y"))
					ps.setString(index++, helathAuthority);

				ps.setInt(index++, consultationId);

				if (patDetMap == null) {
					patDetMap = VisitDetailsDAO.getPatientVisitDetailsBean(patientId).getMap();
				}
				String orgId = (String) patDetMap.get("org_id");
				orgId = orgId == null || orgId.equals("") ? "GENERAL" : orgId;
				String bedType = (String) patDetMap.get("alloc_bed_type");
				bedType = bedType == null || bedType.equals("") ? (String) patDetMap.get("bill_bed_type") : bedType;
				bedType = bedType == null || bedType.equals("") ? "GENERAL" : bedType;

				// test related place holders
				ps.setString(index++, orgId);
				ps.setString(index++, orgId);
				ps.setString(index++, orgId);
				ps.setString(index++, bedType);
				ps.setString(index++, orgId);
				ps.setString(index++, bedType);
				ps.setInt(index++, consultationId);

				// service related place holders
				ps.setString(index++, orgId);
				ps.setString(index++, bedType);
				ps.setString(index++, orgId);
				ps.setInt(index++, consultationId);

				ps.setInt(index++, consultationId);
				ps.setInt(index++, consultationId);

				ps.setString(index++, orgId);
				ps.setString(index++, bedType);
				ps.setString(index++, orgId);
				ps.setInt(index++, consultationId);

				return DataBaseUtil.queryToDynaList(ps);
			} finally {
				DataBaseUtil.closeConnections(con, ps);
			}
		}

	public static List getPrescribedItems(List<BasicDynaBean> presList, String itemType) {
		List itemTypeList = new ArrayList();
		if (presList != null && !presList.isEmpty()) {
			for (BasicDynaBean b: presList) {
				if (b.get("item_type").equals(itemType)) {
					itemTypeList.add(b);
				}
			}
		}
		return itemTypeList;
	}

	public static List getPrescribedMeds(List<BasicDynaBean> presList, String itemType,List li) {
		List itemTypeList = new ArrayList();
		if (presList != null && !presList.isEmpty()) {
			for (BasicDynaBean b: presList) {
				if (b.get("item_type").equals(itemType)) {
					Iterator it = li.iterator();
					while (it.hasNext()) {
						Hashtable h = (Hashtable)it.next();
						if (((String) b.get("item_name")).equals((String) h.get("medicine_name"))) {
							b.set("lblcount", (Integer)h.get("lblcount"));
							itemTypeList.add(b);
						}
					}
				}
			}
		}
		return itemTypeList;
	}

	private static final String MEDICINES_PHARMA_FOR_TREATMENT_SHEET =
		" SELECT pp.consultation_id, sid.medicine_name, frequency as medicine_dosage, strength, medicine_remarks, " +
		" 	medicine_quantity, prescribed_date, mod_time, activity_due_date, route_id, mr.route_name, pp.status as issued, " +
		"	g.generic_name, g.generic_code, pmp.cons_uom_id, cum.consumption_uom, duration, duration_units " +
		" FROM patient_prescription pp " +
		"	JOIN patient_medicine_prescriptions pmp ON (pp.patient_presc_id=pmp.op_medicine_pres_id) " +
		" 	JOIN store_item_details sid ON (sid.medicine_id=pmp.medicine_id) " +
		"	LEFT JOIN medicine_route mr ON (mr.route_id=pmp.route_of_admin) " +
		" 	LEFT JOIN generic_name g ON (sid.generic_name=g.generic_code)" +
		"   LEFT JOIN consumption_uom_master cum ON (cum.cons_uom_id = pmp.cons_uom_id)" +
		" WHERE pp.consultation_id=? ORDER BY op_medicine_pres_id ";
	private static final String MEDICINES_OTHER_FOR_TREATMENT_SHEET =
		" SELECT pp.consultation_id, pomp.medicine_name, frequency as medicine_dosage, strength, medicine_remarks, " +
		"	medicine_quantity, prescribed_date, mod_time, activity_due_date, route_id, route_name, " +
		"	'P' as issued, g.generic_name, g.generic_code, pomp.cons_uom_id, cum.consumption_uom, duration, duration_units " +
		" FROM patient_prescription pp " +
		"	JOIN patient_other_medicine_prescriptions pomp ON (pp.patient_presc_id=pomp.prescription_id) " +
		" 	JOIN prescribed_medicines_master pms ON (pomp.medicine_name=pms.medicine_name)"+
   		" 	LEFT JOIN generic_name g ON (pms.generic_name=g.generic_code)" +
   		"	LEFT JOIN medicine_route mr ON (mr.route_id=pomp.route_of_admin) " +
   		"   LEFT JOIN consumption_uom_master cum ON (cum.cons_uom_id = pomp.cons_uom_id)" +
		" WHERE consultation_id=? ORDER BY prescription_id ";
	private static final String TESTS_FOR_TREATMENT_SHEET =
		" SELECT pp.consultation_id, atp.test_name, test_remarks, pp.status as added_to_bill, prescribed_date, mod_time, " +
		"	ispackage, activity_due_date " +
		" FROM patient_prescription pp " +
		"	JOIN patient_test_prescriptions ptp ON (pp.patient_presc_id=ptp.op_test_pres_id) " +
		" 	JOIN all_tests_pkgs_view atp ON (atp.test_id = ptp.test_id)" +
		" WHERE consultation_id=? ORDER BY op_test_pres_id ";
	private static final String SERVICES_FOR_TREATMENT_SHEET =
		" SELECT pp.consultation_id, s.service_name, service_remarks, pp.status as added_to_bill, prescribed_date, mod_time, " +
		"	activity_due_date " +
		" FROM patient_prescription pp " +
		"	JOIN patient_service_prescriptions psp ON (pp.patient_presc_id=psp.op_service_pres_id) " +
		"	JOIN services s ON (s.service_id = psp.service_id)" +
		" WHERE consultation_id=? ORDER BY op_service_pres_id ";
	private static final String CROSS_CONSULT_FOR_TREATMENT_SHEET =
		" SELECT pp.consultation_id, d.doctor_name as cons_doctor_name, cons_remarks, pp.status as added_to_bill, prescribed_date, mod_time, " +
		"	activity_due_date " +
		" FROM patient_prescription pp " +
		"	JOIN patient_consultation_prescriptions pcp ON (pp.patient_presc_id=pcp.prescription_id)" +
		"	JOIN doctors d ON (d.doctor_id=pcp.doctor_id) " +
		" WHERE consultation_id=? ORDER BY prescription_id ";
	public static List getPrescribedItems(String itemType, int consultationId, String mod_pharmacy)
		throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			String query = null;
			if (itemType.equals("Medicine")) {
				if (mod_pharmacy.equals("Y"))
					query = MEDICINES_PHARMA_FOR_TREATMENT_SHEET;
				else
					query = MEDICINES_OTHER_FOR_TREATMENT_SHEET;
			} else if (itemType.equals("Test")) {
				query = TESTS_FOR_TREATMENT_SHEET;
			} else if (itemType.equals("Service")) {
				query = SERVICES_FOR_TREATMENT_SHEET;
			} else if (itemType.equals("Doctor")) {
				query = CROSS_CONSULT_FOR_TREATMENT_SHEET;
			}
			ps = con.prepareStatement(query);
			ps.setInt(1, consultationId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String MEDICINE_EXISTS_IN_OP =
		" SELECT medicine_name FROM prescribed_medicines_master WHERE trim(medicine_name) like trim(?) ";
	public static boolean medicineExisits(String medicineName) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(MEDICINE_EXISTS_IN_OP);
			ps.setString(1, medicineName);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				return true;
			}
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return false;
	}

	private static final String SERVICE_EXISTS =
		" SELECT service_name FROM services WHERE trim(service_name) like trim(?) ";
	public static boolean serviceExisits(String serviceName) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(SERVICE_EXISTS);
			ps.setString(1, serviceName);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				return true;
			}
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return false;
	}

	private static final String TEST_EXISTS =
		" SELECT test_name FROM diagnostics WHERE trim(test_name) like trim(?) " +
		" UNION " +
		" SELECT package_name FROM packages WHERE trim(package_name) like trim(?)";
	public static boolean testExisits(String testName) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(TEST_EXISTS);
			ps.setString(1, testName);
			ps.setString(2, testName);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				return true;
			}
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return false;
	}

	private static final String PRESCRIPTION_HISTORY =
		" SELECT 'Medicine' as item_type, pp.consultation_id, coalesce(sid.medicine_name, g.generic_name) as item_name, " +
		"	frequency, strength, medicine_quantity, medicine_remarks as item_remarks, pp.status as issued, " +
		"	pp.prescribed_date, duration, duration_units, dc.visited_date " +
		" FROM patient_prescription pp" +
		"	JOIN patient_medicine_prescriptions pmp ON (pp.patient_presc_id=pmp.op_medicine_pres_id)" +
		"	LEFT JOIN store_item_details sid USING (medicine_id) " +
		"	LEFT JOIN generic_name g USING (generic_code) " +
		" 	JOIN generic_preferences gp ON (prescription_uses_stores = 'Y')" +
		" 	JOIN doctor_consultation dc using (consultation_id) " +
		" WHERE dc.consultation_id IN " +
		"	(SELECT consultation_id FROM doctor_consultation dc " +
		" 		JOIN patient_registration pr using (patient_id) " +
		"	WHERE dc.mr_no=? AND pr.visit_type=? and doctor_name = ? AND consultation_id < ? )" +
		" UNION ALL " +
		" SELECT 'Medicine' as item_type, pp.consultation_id, medicine_name as item_name, frequency, strength, " +
		"	medicine_quantity, medicine_remarks as item_remarks, 'P' as issued, pp.prescribed_date, duration, duration_units, " +
		"	dc.visited_date " +
		" FROM patient_prescription pp " +
		"	JOIN patient_other_medicine_prescriptions pomp ON (pp.patient_presc_id=pomp.prescription_id)" +
		" 	JOIN generic_preferences gp ON (prescription_uses_stores='N')" +
		" 	JOIN doctor_consultation dc using (consultation_id)" +
		" WHERE dc.consultation_id IN " +
		"	(SELECT consultation_id FROM doctor_consultation dc " +
		" 		JOIN patient_registration pr using (patient_id) " +
		"	WHERE dc.mr_no=? AND pr.visit_type=? and doctor_name = ? AND consultation_id < ? ) " +
		" UNION ALL " +
		" SELECT 'Inv.' as item_type, pp.consultation_id, atp.test_name as item_name, '' as frequency , '' as strength,  " +
		"	0 as medicine_quantity, test_remarks as item_remarks, pp.status as added_to_bill, pp.prescribed_date, 0 as duration, " +
		"	'' as duration_units, dc.visited_date " +
		" FROM patient_prescription pp " +
		"	JOIN patient_test_prescriptions ptp ON (pp.patient_presc_id=ptp.op_test_pres_id)" +
		"	JOIN all_tests_pkgs_view atp USING (test_id)" +
		" 	JOIN doctor_consultation dc using (consultation_id)" +
		" WHERE dc.consultation_id IN " +
		"	(SELECT consultation_id FROM doctor_consultation dc " +
		" 		JOIN patient_registration pr using (patient_id) " +
		"	WHERE dc.mr_no=? AND pr.visit_type=? and doctor_name = ? AND consultation_id < ? ) " +
		" UNION ALL " +
		" SELECT 'Service' as item_type, pp.consultation_id, s.service_name as item_name, '' as frequency, '' as strength,  " +
		"	0 as medicine_quantity, service_remarks as item_remarks, pp.status as added_to_bill, pp.prescribed_date, 0 as duration, " +
		"	'' as duration_units, dc.visited_date " +
		" FROM patient_prescription pp " +
		"	JOIN patient_service_prescriptions psp ON (pp.patient_presc_id=psp.op_service_pres_id)" +
		"	JOIN services s USING (service_id)" +
		" 	JOIN doctor_consultation dc using (consultation_id)" +
		" WHERE dc.consultation_id IN " +
		"	(SELECT consultation_id FROM doctor_consultation dc " +
		" 		JOIN patient_registration pr using (patient_id) " +
		"	WHERE dc.mr_no=? AND pr.visit_type=? and doctor_name = ? AND consultation_id < ?) " +
		" UNION ALL " +
		" SELECT 'Doctor' as item_type, pp.consultation_id, d.doctor_name as item_name, '' as frequency, '' as strength,  " +
		" 	0 as medicine_quantity, cons_remarks as item_remarks, pp.status as added_to_bill, pp.prescribed_date, 0 as duration, " +
		"	'' as duration_units, dc.visited_date " +
		" FROM patient_prescription pp" +
		"	JOIN patient_consultation_prescriptions pcp ON (pp.patient_presc_id=pcp.prescription_id) " +
		"	JOIN doctors d ON d.doctor_id = pcp.doctor_id" +
		" 	JOIN doctor_consultation dc using (consultation_id)" +
		" WHERE dc.consultation_id IN " +
		"	(SELECT consultation_id FROM doctor_consultation dc " +
		" 		JOIN patient_registration pr using (patient_id) " +
		"	WHERE dc.mr_no=? AND pr.visit_type=? and doctor_name = ? AND consultation_id < ?) " +
		" UNION ALL" +
		" SELECT 'NonHospital' as item_type, pp.consultation_id, item_name, '' as frequency, '' as strength, " +
		"	0 as medicine_quantity, item_remarks, 'P' as issued, prescribed_date, duration, duration_units, dc.visited_date " +
		" FROM patient_prescription pp " +
		"	JOIN patient_other_prescriptions pop ON (pp.patient_presc_id=pop.prescription_id)" +
		" 	JOIN doctor_consultation dc using (consultation_id) " +
		" WHERE dc.consultation_id IN " +
		"	(SELECT consultation_id FROM doctor_consultation dc " +
		" 		JOIN patient_registration pr using (patient_id) " +
		"	WHERE dc.mr_no=? AND pr.visit_type=? and doctor_name = ? AND consultation_id < ?) " +
		" UNION ALL " +
		" SELECT 'Operation' as item_type, pp.consultation_id, operation_name as item_name, '' as frequency, '' as strength, " +
		"	0 as medicine_quantity, pop.remarks, pp.status as added_to_bill, pp.prescribed_date, 0 as duration, '' as duration_units, " +
		"	dc.visited_date " +
		" FROM patient_prescription pp " +
		"	JOIN patient_operation_prescriptions pop ON (pp.patient_presc_id=pop.prescription_id) " +
		"	JOIN operation_master om ON (pop.operation_id=om.op_id) " +
		" 	JOIN doctor_consultation dc using (consultation_id)" +
		" WHERE dc.consultation_id IN " +
		"	(SELECT consultation_id FROM doctor_consultation dc " +
		" 		JOIN patient_registration pr using (patient_id) " +
		"	WHERE dc.mr_no=? AND pr.visit_type=? and doctor_name = ? AND consultation_id < ?) " +
		" order by consultation_id desc";
	public static List getAllPrescriptionsHistory(String mrNo, String visitType, String doctorName,
			int consultationId) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(PRESCRIPTION_HISTORY);
			int index = 1;
			ps.setString(index++, mrNo);
			ps.setString(index++, visitType);
			ps.setString(index++, doctorName);
			ps.setInt(index++, consultationId);
			
			ps.setString(index++, mrNo);
			ps.setString(index++, visitType);
			ps.setString(index++, doctorName);
			ps.setInt(index++, consultationId);
			
			ps.setString(index++, mrNo);
			ps.setString(index++, visitType);
			ps.setString(index++, doctorName);
			ps.setInt(index++, consultationId);
			
			ps.setString(index++, mrNo);
			ps.setString(index++, visitType);
			ps.setString(index++, doctorName);
			ps.setInt(index++, consultationId);
			
			ps.setString(index++, mrNo);
			ps.setString(index++, visitType);
			ps.setString(index++, doctorName);
			ps.setInt(index++, consultationId);
			
			ps.setString(index++, mrNo);
			ps.setString(index++, visitType);
			ps.setString(index++, doctorName);
			ps.setInt(index++, consultationId);
			
			ps.setString(index++, mrNo);
			ps.setString(index++, visitType);
			ps.setString(index++, doctorName);
			ps.setInt(index++, consultationId);
			
			
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}

	}

	private static final String GENERICS = "SELECT generic_name as item_name, generic_code as item_id, 0 as qty," +
			"	generic_code, generic_name, false as ispkg, 'item_master' as master, 'Medicine' as item_type, " +
			"	'' as consumption_uom," +
			"	'' as order_code, '' as prior_auth_required, 0 as item_form_id, '' as item_strength, 0 as item_strength_units, " +
			"	'N' as tooth_num_required, '' as package_uom, '' as issue_uom, '' as dept_id, 'N' as granular_units, " +
			"	'' as insurance_category_id, '' as insurance_category_name " +
			" FROM generic_name " +
			" WHERE (lower(generic_name) like lower(?) OR lower(generic_name) like lower(?)) and status='A' " +
			" ORDER BY generic_name limit 100";

	private static final String PHARMA_MEDICINES_DRUG_CODE =
		" SELECT sic.item_code as drug_code, sid.medicine_name as item_name, sid.medicine_id::text as item_id, " +
		"	COALESCE(sum(ssd.qty), 0) AS qty, g.generic_code, g.generic_name, false as ispkg, " +
		"   'item_master' as master, 'Medicine' as item_type, " +
		"	sid.cons_uom_id, cum.consumption_uom, '' as order_code, " +
		"	sid.prior_auth_required, coalesce(sid.item_form_id, 0) as item_form_id, sid.item_strength, sid.item_strength_units," +
		"	'N' as tooth_num_required, " +
		"	COALESCE(sid.package_uom, '') as package_uom, COALESCE(sid.issue_units,'') as issue_uom, '' as dept_id, " +
		"	COALESCE(ifm.granular_units, 'N') as granular_units, sid.insurance_category_id, iic.insurance_category_name " +
		" FROM store_stock_details ssd " +
		"   JOIN stores s ON (s.dept_id=ssd.dept_id and auto_fill_prescriptions) " +
		"   JOIN store_item_details sid USING (medicine_id) " +
		" 	JOIN item_insurance_categories iic ON (iic.insurance_category_id=sid.insurance_category_id) " +
		"   LEFT JOIN generic_name g ON (sid.generic_name=g.generic_code) " +
		"	LEFT JOIN item_form_master ifm ON (sid.item_form_id = ifm.item_form_id)" +
		"   LEFT JOIN consumption_uom_master cum ON (sid.cons_uom_id = cum.cons_uom_id)"+
		// drug code
		"   LEFT JOIN ha_item_code_type hict ON (hict.medicine_id = sid.medicine_id AND hict.health_authority= ? ) " +
		"	LEFT JOIN store_item_codes sic ON (sic.medicine_id = hict.medicine_id AND sic.code_type = hict.code_type) " +

		" WHERE sid.status='A'  " +
		"   AND (lower(medicine_name) like lower(?) OR lower(g.generic_name) like lower(?) OR lower(medicine_name) like lower(?)) " +
		" GROUP BY sic.item_code, medicine_name, sid.medicine_id, g.generic_name, g.generic_code, " +
		"   consumption_uom, sid.prior_auth_required," +
		"	sid.item_form_id, sid.item_strength, sid.item_strength_units, " +
		"	sid.package_uom, sid.issue_units, ifm.granular_units, sid.insurance_category_id, iic.insurance_category_name " +
		" ORDER BY medicine_name limit 100";

	private static final String PHARMA_MEDICINES =
		" SELECT sid.medicine_name as item_name, sid.medicine_id::text as item_id, " +
		"	COALESCE(sum(ssd.qty), 0) AS qty, g.generic_code, g.generic_name, false as ispkg, " +
		"   'item_master' as master, 'Medicine' as item_type, " +
		"	sid.cons_uom_id, cum.consumption_uom, '' as order_code, " +
		"	sid.prior_auth_required, coalesce(sid.item_form_id, 0) as item_form_id, sid.item_strength, sid.item_strength_units," +
		"	'N' as tooth_num_required, " +
		"	COALESCE(sid.package_uom, '') as package_uom, COALESCE(sid.issue_units,'') as issue_uom, '' as dept_id, " +
		"	COALESCE(ifm.granular_units, 'N') as granular_units, sid.insurance_category_id, iic.insurance_category_name " +
		" FROM store_stock_details ssd " +
		"   JOIN stores s ON (s.dept_id=ssd.dept_id and auto_fill_prescriptions) " +
		"   JOIN store_item_details sid USING (medicine_id) " +
		" 	JOIN item_insurance_categories iic ON (iic.insurance_category_id=sid.insurance_category_id) " +
		"   LEFT JOIN generic_name g ON (sid.generic_name=g.generic_code) " +
		"	LEFT JOIN item_form_master ifm ON (sid.item_form_id = ifm.item_form_id)" +
		"   LEFT JOIN consumption_uom_master cum ON (sid.cons_uom_id = cum.cons_uom_id)"+
		" WHERE sid.status='A'  " +
		"   AND (lower(medicine_name) like lower(?) OR lower(g.generic_name) like lower(?) OR lower(medicine_name) like lower(?)) " +
		" GROUP BY medicine_name, sid.medicine_id, g.generic_name, g.generic_code, " +
		"   consumption_uom, sid.prior_auth_required," +
		"	sid.item_form_id, sid.item_strength, sid.item_strength_units, " +
		"	sid.package_uom, sid.issue_units, ifm.granular_units, sid.insurance_category_id, iic.insurance_category_name " +
		" ORDER BY medicine_name limit 100";


   	private static final String NON_HOSPITAL_MEDICINES =
   		" SELECT medicine_name as item_name, '' as item_id, 0 as qty, g.generic_code, g.generic_name, false as ispkg, " +
   		"	'op' as master, 'Medicine' as item_type, " +
   		"	'' as consumption_uom, '' as order_code, '' as prior_auth_required, item_form_id, item_strength, pms.item_strength_units, " +
   		"	'N' as tooth_num_required, '' as package_uom, '' as issue_uom, '' as dept_id, 'N' as granular_units, " +
   		"	'' as insurance_category_id, '' as insurance_category_name " +
   		" FROM prescribed_medicines_master pms "+
   		" LEFT JOIN generic_name g ON (pms.generic_name=g.generic_code) " +
   		" WHERE pms.status='A' AND (lower(medicine_name) like lower(?) or lower(g.generic_name) like lower(?) OR lower(medicine_name) like lower(?)) limit 100";
	private static final String ALL_TESTS_FIELDS_FOR_ORG =
		" SELECT atpv.test_name as item_name, atpv.test_id as item_id, 0 as qty, '' as generic_code, " +
		"	'' as generic_name, atpv.ispkg, 'item_master' as master, 'Inv.' as item_type, " +
		"	'' as consumption_uom, order_code::text, " +
		"	atpv.prior_auth_required, 0 as item_form_id, '' as item_strength, 0 as item_strength_units," +
		"  'N' as tooth_num_required, '' as package_uom, '' as issue_uom, atpv.ddept_id as dept_id, 'N' as granular_units," +
		"  coalesce(tod.code_type,pod.code_type) as code_type," +
		"  coalesce(tod.item_code,pod.item_code) as item_code, iic.insurance_category_id, iic.insurance_category_name " +
		" FROM all_tests_pkgs_view atpv " +
		" LEFT JOIN diagnostics diagn on (diagn.test_id=atpv.test_id) " +
		" 	JOIN item_insurance_categories iic ON (atpv.insurance_category_id=iic.insurance_category_id) " +
		"	LEFT OUTER JOIN test_org_details tod ON tod.org_id=? and tod.applicable AND " +
		"		atpv.test_id=tod.test_id " +
		"	LEFT OUTER JOIN pack_org_details pod ON atpv.test_id=pod.package_id::text AND " +
		"	pod.applicable and pod.org_id=?  " +
		" WHERE (diagn.is_prescribable is null OR diagn.is_prescribable) AND atpv.approval_status='A' AND (case when atpv.ispkg then exists " +
		"		(select package_id as pack_id from center_package_applicability pcm "
		+ " where pcm.package_id::text=atpv.test_id AND pcm.status='A' AND (pcm.center_id=? or pcm.center_id=-1)) else true end) " +
		"	AND (case when atpv.ispkg then exists " +
		"		(select pack_id from package_sponsor_master psm where psm.pack_id::text=atpv.test_id AND psm.status='A' AND (psm.tpa_id=? OR psm.tpa_id = '-1')) else true end) " +
		"	AND " +
		"	atpv.status='A' AND (lower(atpv.test_name) like lower(?) OR lower(order_code) like lower(?) OR lower(atpv.test_name) like lower(?)) limit 100";
	private static final String ALL_SERVICES_FIELDS_FOR_ORG =
		" SELECT s.service_name as item_name, s.service_id as item_id, 0 as qty, '' as generic_code, " +
		"	'' as generic_name, false as ispkg, 'item_master' as master, 'Service' as item_type, " +
		"	'' as consumption_uom, service_code::text as order_code," +
		"	s.prior_auth_required, 0 as item_form_id, '' as item_strength, 0 as item_strength_units, " +
		"	tooth_num_required, '' as package_uom, '' as issue_uom, '' as dept_id, 'N' as granular_units," +
		"  sod.code_type as code_type,sod.item_code as item_code, iic.insurance_category_id, iic.insurance_category_name " +
		" FROM services s " +
		" 	JOIN item_insurance_categories iic ON (s.insurance_category_id=iic.insurance_category_id) " +
		" 	LEFT OUTER JOIN service_org_details sod ON sod.org_id=? AND sod.applicable AND sod.service_id=s.service_id " +
		" WHERE status='A' AND (lower(service_name) like lower(?) OR lower(service_code) like lower(?) OR lower(service_name) like lower(?)) limit 100";
	private static final String NON_DENTAL_SERVICES_FIELDS_FOR_ORG =
		" SELECT s.service_name as item_name, s.service_id as item_id, 0 as qty, '' as generic_code, " +
		"	'' as generic_name, false as ispkg, 'item_master' as master, 'Service' as item_type,  " +
		"	'' as consumption_uom, service_code::text as order_code," +
		"	s.prior_auth_required, 0 as item_form_id, '' as item_strength, 0 as item_strength_units, " +
		"	tooth_num_required, '' as package_uom, '' as issue_uom, '' as dept_id, 'N' as granular_units," +
		"  sod.code_type as code_type,sod.item_code as item_code, iic.insurance_category_id, iic.insurance_category_name" +
		" FROM services s " +
		" 	JOIN item_insurance_categories iic ON (s.insurance_category_id=iic.insurance_category_id) " +
		"	JOIN services_departments dep ON (dep.serv_dept_id=s.serv_dept_id) "+
		"	LEFT JOIN department_type_master dt ON (dep.dept_type_id=dt.dept_type_id)" +
		" 	LEFT OUTER JOIN service_org_details sod ON sod.org_id=? AND sod.applicable AND sod.service_id=s.service_id " +
		" WHERE s.status='A' AND coalesce(dt.dept_type_id, '')!='DENT' AND (lower(service_name) like lower(?) OR lower(service_code) like lower(?) OR lower(service_name) like lower(?)) limit 100";

	private static final String ALL_DOCTORS_FIELDS =
		" SELECT doctor_name as item_name, d.doctor_id as item_id, 0 as qty, '' as generic_code, '' as generic_name, " +
		"	false as ispkg, 'item_master' as master, 'Doctor' as item_type, " +
		"	'' as consumption_uom, '' as order_code," +
		"	'' as prior_auth_required, 0 as item_form_id, '' as item_strength, 0 as item_strength_units, 'N' as tooth_num_required, " +
		"	'' as package_uom, '' as issue_uom, '' as dept_id, 'N' as granular_units, '' as code_type,'' as item_code, " +
		"	iic.insurance_category_id, iic.insurance_category_name " +
		" FROM doctors d " +
		" 	JOIN consultation_types ct ON (ct.consultation_type_id=-1) " +
		" 	JOIN item_insurance_categories iic ON (ct.insurance_category_id=iic.insurance_category_id) " +
		" WHERE (lower(doctor_name) like lower(?) or lower(doctor_name) like lower(?) ) AND d.status='A' limit 100";
	private static final String ALL_OPERATIONS_FIELDS_FOR_ORG =
		" SELECT operation_name as item_name, op_id as item_id, 0 as qty, '' as generic_code, " +
		"	'' as generic_name, false as ispkg, 'item_master' as master, 'Operation' as item_type, " +
		"	'' as consumption_uom, operation_code::text as order_code," +
		"	prior_auth_required, 0 as item_form_id, '' as item_strength, 0 as item_strength_units, " +
		"	'N' as tooth_num_required, '' as package_uom, '' as issue_uom, 'N' as granular_units," +
		"   ood.code_type,ood.item_code, iic.insurance_category_id, iic.insurance_category_name " +
		" FROM operation_master om" +
		" 	JOIN item_insurance_categories iic ON (iic.insurance_category_id=om.insurance_category_id) " +
		" LEFT OUTER JOIN operation_org_details ood ON ood.org_id=? AND ood.applicable AND ood.operation_id=om.op_id "+
		" WHERE status='A' AND (lower(operation_name) like lower(?) OR lower(operation_code) like lower(?) OR lower(operation_name) like lower(?)) limit 100";
	
  private static final String ALL_INVENTORY_ITEMS = " SELECT m.medicine_name as item_name, "
      + " m.medicine_id as item_id, 0 as qty, '' as generic_code, '' as generic_name, "
      + " false as ispkg, 'item_master' as master, 'Inventory' as item_type, "
      + " '' as consumption_uom, '' as order_code, "
      + " m.prior_auth_required, 0 as item_form_id, '' as item_strength, "
      + " 0 as item_strength_units, 'N' as tooth_num_required, '' as package_uom, "
      + " '' as issue_uom, '' as dept_id, 'N' as granular_units,  sic.code_type, "
      + " sic.item_code, iic.insurance_category_id, iic.insurance_category_name "
      + " FROM store_item_details m "
      + " JOIN store_category_master icm ON (icm.category_id = m.med_category_id) "
      + " LEFT JOIN ha_item_code_type hict  ON (hict.medicine_id = m.medicine_id "
      + "   AND hict.health_authority = ?) "
      + " LEFT JOIN store_item_codes sic  ON (sic.medicine_id = m.medicine_id "
      + " AND sic.code_type = hict.code_type) " + " LEFT JOIN item_insurance_categories iic "
      + " ON(iic.insurance_category_id = m.insurance_category_id) "
      + " WHERE billable = true AND icm.issue_type IN ('C','R') AND m.status='A' "
      + " AND (lower(sic.item_code) like lower(?) OR lower(m.medicine_name) like lower(?)) ";
	
	public static List getAllItems(String orgId, String type, String findItem, String use_store_items,
			Boolean isStanding, Boolean non_dental_services, String tpaId, String centerIdStr,
			boolean forceUnUseOfGenerics,  String patientHealthAutority, String deptId) throws SQLException {
		use_store_items = use_store_items == null ? "" : use_store_items;
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
	    String prescByGenerics = (String) HealthAuthorityPreferencesDAO.getHealthAuthorityPreferences(
					CenterMasterDAO.getHealthAuthorityForCenter(Integer.parseInt(centerIdStr))).getPrescriptions_by_generics();
	    if (patientHealthAutority.isEmpty()) {
	    	// if patient healthautority is emplty , passing the user heathautority value
	    	patientHealthAutority = (String)CenterMasterDAO.getHealthAuthorityForCenter(Integer.parseInt(centerIdStr));
	    }
		boolean useGenerics = prescByGenerics.equals("Y");
		// For PBM, if prescriptions are done using generics,
		// the medicine ajax is made again for fetching medicine name as per user
		// looking at the generic name. Hence, forced not to use generics even though pref. has generics.
		useGenerics = forceUnUseOfGenerics ? false : useGenerics ;
		BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
		int centerId = -1;
		if ((Integer) genericPrefs.get("max_centers_inc_default") > 1 && centerIdStr != null && !centerIdStr.equals(""))
			centerId = Integer.parseInt(centerIdStr);
		try {
			String query = "";
			if (type.equals("Medicine")) {
				if (use_store_items.equals("Y")) {
					if (!isStanding && useGenerics)
						query = GENERICS;
					else
						query = PHARMA_MEDICINES_DRUG_CODE;
				} else {
					query = NON_HOSPITAL_MEDICINES;
				}
			} else if (type.equals("Inv.")) {
				query = ALL_TESTS_FIELDS_FOR_ORG;
			} else if (type.equals("Service")) {
				query = non_dental_services ? NON_DENTAL_SERVICES_FIELDS_FOR_ORG : ALL_SERVICES_FIELDS_FOR_ORG;
			} else if (type.equals("Doctor")) {
				query = ALL_DOCTORS_FIELDS;
			} else if (type.equals("Operation")) {
				query = ALL_OPERATIONS_FIELDS_FOR_ORG;
			} else if (type.equals("Inventory")) {
			  query = ALL_INVENTORY_ITEMS;
			}
			ps = con.prepareStatement(query);
			if (type.equals("Medicine")) {
				if (use_store_items.equals("Y")) {
					if (!isStanding && useGenerics) {
						ps.setString(1, findItem + "%");
						ps.setString(2, "% " +findItem+ "%");
					} else {
						ps.setString(1, patientHealthAutority);
						ps.setString(2, findItem + "%");		// name starts with "xx"
						ps.setString(3, findItem + "%");		// generic starts with "xx"
						ps.setString(4, "% " +findItem + "%");	// name contains " xx"
					}
				} else {
					ps.setString(1, findItem + "%");		// name starts with "xx"
					ps.setString(2, findItem + "%");		// generic starts with "xx"
					ps.setString(3, "% " +findItem+ "%");
				}

			} else if (type.equals("Service")) {
				ps.setString(1, orgId);
				ps.setString(2, findItem + "%");
				ps.setString(3, findItem + "%");
				ps.setString(4, "% " +findItem+ "%");
			} else if (type.equals("Inv.")) {
				ps.setString(1, orgId);
				ps.setString(2, orgId);
				ps.setInt(3, centerId);
				ps.setString(4, tpaId);
				ps.setString(5, findItem + "%");
				ps.setString(6, findItem + "%");
				ps.setString(7, "% " +findItem+ "%");
			} else if (type.equals("Doctor")) {
				ps.setString(1, findItem + "%");
				ps.setString(2, "% " +findItem+ "%");
			} else if (type.equals("Operation")) {
				ps.setString(1, orgId);
				ps.setString(2, findItem +"%");
				ps.setString(3, findItem + "%");
				ps.setString(4, "% " +findItem+ "%");
      } else if (type.equals("Inventory")) {
        ps.setString(1, patientHealthAutority);
        ps.setString(2, findItem + "%");
        ps.setString(3, "%" + findItem + "%");
      }
			return DataBaseUtil.queryToDynaList(ps);
		}  finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String ALL_TESTS_FIELDS =
		" SELECT atpv.test_name as item_name, atpv.test_id as item_id, 0 as qty, '' as generic_code, " +
		"	'' as generic_name, atpv.ispkg, 'item_master' as master, 'Inv.' as item_type, " +
		"	'' as consumption_uom, order_code::text, " +
		"	atpv.prior_auth_required, 0 as item_form_id, '' as item_strength, 0 as item_strength_units " +
		" FROM all_tests_pkgs_view atpv " +
		" WHERE atpv.status='A' AND atpv.approval_status='A' AND (test_name ilike ? OR order_code ilike ? OR test_name ilike ?) limit 100";
	private static final String ALL_SERVICES_FIELDS =
		" SELECT s.service_name as item_name, s.service_id as item_id, 0 as qty, '' as generic_code, " +
		"	'' as generic_name, false as ispkg, 'item_master' as master, 'Service' as item_type, " +
		"	'' as consumption_uom, service_code::text as order_code," +
		"	s.prior_auth_required, 0 as item_form_id, '' as item_strength, 0 as item_strength_units " +
		" FROM services s " +
		" WHERE status='A' AND (service_name ilike ? OR service_code ilike ? OR service_name ilike ?) limit 100";
	private static final String ALL_OPERATIONS_FIELDS =
			" SELECT operation_name as item_name, op_id as item_id, 0 as qty, '' as generic_code, " +
			"	'' as generic_name, false as ispkg, 'item_master' as master, 'Operation' as item_type, " +
			"	'' as consumption_uom, operation_code::text as order_code," +
			"	prior_auth_required, 0 as item_form_id, '' as item_strength, 0 as item_strength_units, " +
			"	'N' as tooth_num_required, '' as package_uom, '' as issue_uom, 'N' as granular_units " +
			" FROM operation_master om" +
			" WHERE status='A' AND (operation_name ilike ? OR operation_code ilike ? OR operation_name ilike ?) limit 100";

	public static List getAllItemsForFavourites(String searchType, String findItem, String use_store_items, int centerId) throws SQLException {
		use_store_items = use_store_items == null ? "" : use_store_items;
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		String prescByGenerics = (String) HealthAuthorityPreferencesDAO.getHealthAuthorityPreferences(
				CenterMasterDAO.getHealthAuthorityForCenter(centerId)).getPrescriptions_by_generics();
		try {
			String query = "";
			if (searchType.equals("Medicine")) {
				if (use_store_items.equals("Y")) {
					if (prescByGenerics.equals("Y"))
						query = GENERICS;
					else
						query = PHARMA_MEDICINES;
				} else {
					query = NON_HOSPITAL_MEDICINES;
				}
			} else if (searchType.equals("Inv.")) {
				query = ALL_TESTS_FIELDS;
			} else if (searchType.equals("Service")) {
				query = ALL_SERVICES_FIELDS;
			} else if (searchType.equals("Doctor")) {
				query = ALL_DOCTORS_FIELDS;
			} else if (searchType.equals("Operation")) {
				query = ALL_OPERATIONS_FIELDS;
			}
			ps = con.prepareStatement(query);
			if (searchType.equals("Medicine")) {
				if (use_store_items.equals("Y")) {
					if (prescByGenerics.equals("Y")) {
						ps.setString(1, findItem + "%");
						ps.setString(2, "% " +findItem+ "%");
					} else {
						ps.setString(1, findItem + "%");		// name starts with "xx"
						ps.setString(2, findItem + "%");		// generic starts with "xx"
						ps.setString(3, "% " +findItem + "%");	// name contains " xx"
					}
				} else {
					ps.setString(1, findItem + "%");		// name starts with "xx"
					ps.setString(2, findItem + "%");		// generic starts with "xx"
					ps.setString(3, "% " +findItem+ "%");
				}

			} else if (searchType.equals("Service")) {
				ps.setString(1, findItem + "%");
				ps.setString(2, findItem + "%");
				ps.setString(3, "% " +findItem+ "%");
			} else if (searchType.equals("Inv.")) {
				ps.setString(1, findItem + "%");
				ps.setString(2, findItem + "%");
				ps.setString(3, "% " +findItem+ "%");
			} else if (searchType.equals("Doctor")) {
				ps.setString(1, findItem + "%");
				ps.setString(2, "% " +findItem+ "%");
			} else if (searchType.equals("Operation")) {
				ps.setString(1, findItem +"%");
				ps.setString(2, findItem + "%");
				ps.setString(3, "% " +findItem+ "%");
			}
			return DataBaseUtil.queryToDynaList(ps);
		}  finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	// if package size or mrp is different for different batches then chosing the highest mrp item to estimate the cost of the selected medicine
	public static final String MEDICINE_MRP =
		" SELECT mrp, stock_pkg_size, '' as package_type, '' as type, '' as category, item_batch_id, batch_no, '' as doctor_charge_type " + 
		" FROM store_stock_details ssd join store_item_details sid USING (medicine_id) " +
		"	JOIN store_item_batch_details batch on (batch.item_batch_id=ssd.item_batch_id) " +
		" WHERE ssd.medicine_id=? and qty>0 " +
		" ORDER BY medicine_name, mrp desc limit 1";
	public static final String TEST_RATE =
		" SELECT charge, discount, '' as package_type, '' as type, category, 0 as item_batch_id, " + 
		"	'' as batch_no, '' as doctor_charge_type, ipd.category_payable AS category_payable " +
		" FROM diagnostic_charges JOIN diagnostics d USING (test_id) " +
		" 	JOIN diagnostics_departments dd on (dd.ddept_id=d.ddept_id) " +
		"  LEFT JOIN insurance_plan_details ipd on (d.insurance_category_id = ipd.insurance_category_id " +
		"    AND ipd.patient_type=? AND ipd.plan_id=?) " +
		" WHERE org_name=? and bed_type=? and test_id = ?";
	public static final String TEST_PKG_RATE =
		" SELECT charge, discount, (case when visit_applicability='*' and type='D' then 'd' else visit_applicability end) as package_type, 'P' as type, '' as category, 0 as item_batch_id, '' as batch_no, '' as doctor_charge_type, ipd.category_payable AS category_payable " + 
		"	FROM package_charges pc JOIN packages pm USING (package_id) " +
		"  LEFT JOIN insurance_plan_details ipd on (pm.insurance_category_id = ipd.insurance_category_id " +
		"    AND ipd.patient_type=? AND ipd.plan_id=?) " +
		" WHERE package_type='d' and org_id=? and bed_type = ? and package_id = ?";
	public static final String SERVICE_RATE =
		" SELECT unit_charge as charge, discount, '' as package_type, '' as category," + 
		"	0 as item_batch_id, '' as batch_no, '' as doctor_charge_type,  ipd.category_payable AS category_payable " + 
		" 	FROM service_master_charges smc JOIN services s USING (service_id) " +
		"  LEFT JOIN insurance_plan_details ipd on (s.insurance_category_id = ipd.insurance_category_id " +
		"    AND ipd.patient_type=? AND ipd.plan_id=?) " +
		" WHERE  org_id=? and bed_type=? and service_id = ?";
	public static final String OPERATION_CHARGE =
		" SELECT surg_asstance_charge as charge, surg_asst_discount as discount, '' as package_type, '' as category, " + 
		" 	0 as item_batch_id, '' as batch_no, '' as doctor_charge_type,  ipd.category_payable AS category_payable " +
		" FROM operation_charges oc JOIN operation_master om USING (op_id) " +
		"  LEFT JOIN insurance_plan_details ipd on (om.insurance_category_id = ipd.insurance_category_id " +
		"    AND ipd.patient_type=? AND ipd.plan_id=?) " +
		" WHERE op_id=? and org_id=? and bed_type=? ";
	public static final String DOCTOR_CONS_CHARGE = 
		" SELECT cc.charge, cc.discount, '' as package_type, '' as category, 0 as item_batch_id, '' as batch_no, " +
		" 	doctor_charge_type,  ipd.category_payable AS category_payable " +
		" FROM consultation_charges cc " +
		"  JOIN consultation_org_details cod ON (cod.consultation_type_id = cc.consultation_type_id " +
		"    AND cod.org_id = cc.org_id) " +
		"  JOIN consultation_types ct ON (ct.consultation_type_id=cc.consultation_type_id) " +
		"  LEFT JOIN insurance_plan_details ipd on (ct.insurance_category_id = ipd.insurance_category_id " +
		"    AND ipd.patient_type=? AND ipd.plan_id=?) " +
		" WHERE cc.consultation_type_id = -1 AND cc.org_id = ? AND cc.bed_type = ? ";
			
	public static BasicDynaBean getItemRateDetails(int planId, String orgId, String bedType, String itemType,
			String itemId, Boolean ispkg)	throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		List l = null;
		BasicDynaBean bean = null;
		int centerId = RequestContext.getCenterId();
		String healthAuthority = CenterMasterDAO.getHealthAuthorityForCenter(centerId);
		BasicDynaBean doctorChargeBean = null;
		try {
			if (itemType.equals("Medicine")) {
				List medicineIds = new ArrayList();
				medicineIds.add(Integer.parseInt(itemId));
				List beans = new MedicineStockDAO(con).getAllStoreMedicineStockWithPatAmtsInDept(medicineIds, planId, "o", true, 0, healthAuthority, true);

				return (beans == null || beans.isEmpty()) ? null : (BasicDynaBean) beans.get(0);

			} else if (itemType.equals("Inv.")) {
				if (ispkg) {
					ps = con.prepareStatement(TEST_PKG_RATE);
					ps.setString(1, "o");
					ps.setInt(2, planId);
					ps.setString(3, orgId);
					ps.setString(4, bedType);
					ps.setInt(5, Integer.parseInt(itemId));
				} else {
					ps = con.prepareStatement(TEST_RATE);
					ps.setString(1, "o");
					ps.setInt(2, planId);
					ps.setString(3, orgId);
					ps.setString(4, bedType);
					ps.setString(5, itemId);
				}

			} else if (itemType.equals("Service")) {
				ps = con.prepareStatement(SERVICE_RATE);
				ps.setString(1, "o");
				ps.setInt(2, planId);
				ps.setString(3, orgId);
				ps.setString(4, bedType);
				ps.setString(5, itemId);
			} else if (itemType.equals("Operation")) {
				ps = con.prepareStatement(OPERATION_CHARGE);
				ps.setString(1, "o");
				ps.setInt(2, planId);
				ps.setString(3, itemId);
				ps.setString(4, orgId);
				ps.setString(5, bedType);
			} else if (itemType.equals("Doctor")) {
				ps = con.prepareStatement(DOCTOR_CONS_CHARGE);
				ps.setString(1, "o");
				ps.setInt(2, planId);
				ps.setString(3, orgId);
				ps.setString(4, bedType);
				
				doctorChargeBean = DoctorMasterDAO.getDoctorCharges(itemId, orgId, bedType);
			}
			l = DataBaseUtil.queryToDynaList(ps);
			if (l != null && !l.isEmpty())
				bean = (BasicDynaBean) l.get(0);
			
			if (itemType.equals("Doctor")) {
				String chargeType = (String) bean.get("doctor_charge_type");
				BigDecimal conscharge = (BigDecimal) bean.get("charge");
				BigDecimal consdiscount = (BigDecimal) bean.get("discount");
				
				BigDecimal doccharge = (BigDecimal) doctorChargeBean.get(chargeType);
				BigDecimal docdiscount = (BigDecimal) doctorChargeBean.get(chargeType+"_discount");
				bean.set("charge", conscharge.add(doccharge));
				bean.set("discount", consdiscount.add(docdiscount));
			}
		} catch(SQLException se) {
			se.printStackTrace();
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return bean;
	}

	private static final String ALL_PHARMA_MEDICINES_PRES_FOR_PATIENT =
		" SELECT sid.medicine_name as item_name, sid.medicine_id::text as item_id , medicine_quantity, " +
		" 	op_medicine_pres_id as item_prescribed_id, frequency, strength, duration as medicine_days, " +
		"	medicine_remarks, medicine_remarks as item_remarks, pp.status as issued, pmp.cons_uom_id, cum.consumption_uom, g.generic_name, " +
		"	g.generic_code, 'item_master' as master, " +
		"	'Medicine' as item_type, false as ispackage, activity_due_date, mod_time, mr.route_id, mr.route_name, " +
		"	icm.category AS category_name, mm.manf_name, mm.manf_mnemonic, issue_base_unit, sid.prior_auth_required, " +
		"	coalesce(pmp.item_form_id, 0) as item_form_id, pmp.item_strength, if.item_form_name, " +
		"	0 as charge, 0 as discount, '' as test_category, duration, duration_units, pmp.item_strength_units, " +
		"	unit_name, pmp.admin_strength, granular_units " +
		" FROM patient_prescription pp" +
		"	JOIN patient_medicine_prescriptions pmp ON (pp.patient_presc_id=pmp.op_medicine_pres_id) " +
		"	LEFT JOIN store_item_details sid ON (pmp.medicine_id=sid.medicine_id) " +
		" 	LEFT JOIN item_form_master if ON (pmp.item_form_id=if.item_form_id) " +
		"	LEFT OUTER JOIN generic_name g ON (pmp.generic_code = g.generic_code) " +
		"	LEFT OUTER JOIN medicine_route mr ON (mr.route_id=pmp.route_of_admin)" +
		"   LEFT OUTER JOIN manf_master mm on mm.manf_code=sid.manf_name " +
		"   LEFT OUTER JOIN store_category_master icm on sid.med_category_id=icm.category_id " +
		" 	LEFT OUTER JOIN strength_units su ON (su.unit_id=pmp.item_strength_units) " +
		"   LEFT JOIN consumption_uom_master cum ON (cum.cons_uom_id=pmp.cons_uom_id) " +
		" WHERE mr_no=? AND coalesce(pp.consultation_id, 0)=0";
	private static final String ALL_OP_MEDICINES_PRES_FOR_PATIENT =
		" SELECT pomp.medicine_name as item_name, '' as item_id, medicine_quantity, prescription_id as item_prescribed_id," +
		"	frequency, strength, duration as medicine_days, medicine_remarks, medicine_remarks as item_remarks, " +
		"	'P' as issued, pomp.cons_uom_id, cum.consumption_uom, g.generic_name, " +
		"	g.generic_code, 'op' as master, 'Medicine' as item_type, false as ispackage, activity_due_date, mod_time, " +
		"	mr.route_id, mr.route_name , '' AS category_name,'' as manf_name, '' as manf_mnemonic, 0 as issue_base_unit, " +
		"	'' as prior_auth_required, coalesce(pomp.item_form_id, 0) as item_form_id, pomp.item_strength, " +
		"	ifm.item_form_name,  0 as charge, 0 as discount, '' as test_category, duration, duration_units, " +
		"	pomp.item_strength_units, unit_name, pomp.admin_strength, granular_units  " +
		" FROM patient_prescription pp " +
		"	JOIN patient_other_medicine_prescriptions pomp ON (pp.patient_presc_id=pomp.prescription_id) " +
		"	LEFT JOIN item_form_master ifm ON (pomp.item_form_id = ifm.item_form_id)" +
		" 	JOIN prescribed_medicines_master pms ON (pomp.medicine_name=pms.medicine_name)"+
   		" 	LEFT JOIN generic_name g ON (pms.generic_name=g.generic_code)" +
   		"	LEFT JOIN medicine_route mr ON (mr.route_id=pomp.route_of_admin) " +
   		"	LEFT JOIN strength_units su ON (su.unit_id=pomp.item_strength_units)" +
   		"   LEFT JOIN consumption_uom_master cum ON (cum.cons_uom_id=pomp.cons_uom_id)" +
		" WHERE mr_no=? AND coalesce(pp.consultation_id, 0)=0";

	private static final String ALL_PRESCRIPTIONS_FOR_PATIENT =
		" SELECT atp.test_name, atp.test_id as item_id, 0, op_test_pres_id, '', '', 0, test_remarks as item_remarks, " +
		"	test_remarks as item_remarks," +
		"	pp.status as added_to_bill, null as cons_uom_id, '' as consumption_uom, '', '', 'item_master' as master, 'Inv.' as item_type, ispkg as ispackage, " +
		"	activity_due_date, mod_time, -1 as route_id, '' as route_name,'' AS category_name,'' as manf_name, '' as manf_mnemonic," +
		"	0 as issue_base_unit, atp.prior_auth_required, 0 as item_form_id, '' as item_strength, '' as item_form_name, " +
		"	coalesce(dc.charge, pc.charge, 0) as charge, coalesce(dc.discount, pc.discount, 0) as discount, dd.category as test_category, " +
		"	0 as duration, '' as duration_units, 0 as item_strength_units, '' as unit_name, ptp.admin_strength, 'N' as granular_units  " +

		" FROM patient_prescription pp " +
		"	JOIN patient_test_prescriptions ptp ON (pp.patient_presc_id=ptp.op_test_pres_id) " +
		"	JOIN all_tests_pkgs_view atp ON (atp.test_id = ptp.test_id) " +
		"	LEFT JOIN diagnostics_departments dd ON (dd.ddept_id=atp.ddept_id)" +
		"	LEFT JOIN diagnostic_charges dc ON (dc.test_id=atp.test_id and dc.org_name='ORG0001' and dc.bed_type='GENERAL') " +
		"   LEFT JOIN package_charges pc ON (pc.package_id::text=atp.test_id and org_id='ORG0001' and pc.bed_type='GENERAL') " +
		" WHERE mr_no=? AND coalesce(pp.consultation_id, 0)=0 " +
		" UNION ALL " +

		" SELECT s.service_name as item_name, s.service_id as item_id, qty, op_service_pres_id, '', '',  0, " +
		"	service_remarks as item_remarks, service_remarks as item_remarks, pp.status as added_to_bill, null as cons_uom_id, '' as consumption_uom, " +
		"	'', '', 'item_master' as master, 'Service' as item_type, " +
		"	false as ispackage, activity_due_date, mod_time, -1 as  route_id, '' as route_name,'' AS category_name,'' as manf_name, " +
		"	'' as manf_mnemonic, 0 as issue_base_unit, s.prior_auth_required, 0 as item_form_id, " +
		"	'' as item_strength, '' as item_form_name, smc.unit_charge as charge, smc.discount as discount, '' as test_category, " +
		"	0 as duration, '' as duation_units, 0 as item_strength_units, '' as unit_name, psp.admin_strength, 'N' as granular_units " +

		" FROM patient_prescription pp " +
		"	JOIN patient_service_prescriptions psp ON (pp.patient_presc_id=psp.op_service_pres_id) " +
		"	JOIN services s ON (s.service_id = psp.service_id) " +
		"   JOIN service_master_charges smc ON (smc.service_id=s.service_id and org_id='ORG0001' and bed_type='GENERAL')" +
		" WHERE mr_no=? AND coalesce(pp.consultation_id, 0)=0 " ;

	public static List getPrescriptionsForPatient(String mrNo, String use_store_items) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement((use_store_items.equals("Y") ? ALL_PHARMA_MEDICINES_PRES_FOR_PATIENT
					: ALL_OP_MEDICINES_PRES_FOR_PATIENT) + " UNION ALL " + ALL_PRESCRIPTIONS_FOR_PATIENT);
			ps.setString(1, mrNo);
			ps.setString(2, mrNo);
			ps.setString(3, mrNo);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String ALL_PACKAGE_COMPONENT = " SELECT d.test_name, ptp.op_test_pres_id "
			+ " FROM patient_prescription pp "
			+ "	JOIN patient_test_prescriptions ptp ON (pp.patient_presc_id = ptp.op_test_pres_id) "
			+ " JOIN packages pm ON (pm.package_id::text = ptp.test_id AND ptp.ispackage = 'true') "
			+ " JOIN package_contents pcd ON (pcd.package_id = pm.package_id ) "
			+ " JOIN diagnostics d ON (d.test_id = pcd.activity_id) "
			+ " WHERE pm.type='O' AND pp.consultation_id = ? ORDER BY pcd.display_order ";

	public static List getAllPackageComponents(int consultationId) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(ALL_PACKAGE_COMPONENT);
			ps.setInt(1, consultationId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}


	private static final String DOC_FAV_NON_HOSPITAL_ITEMS =
		" SELECT dof.item_name, '' as item_id, '' as generic_code, '' as generic_name, false as ispkg, " +
		"	'op' as master, 'Medicine' as item_type, '' as order_code, '' as prior_auth_required," +
   		"	'N' as tooth_num_required, '' as package_uom, '' as issue_uom, '' as dept_id, 'N' as granular_units, " +
		"	COALESCE(dof.medicine_quantity, 0) as qty, dof.consumption_uom, COALESCE(dof.item_form_id, 0) AS item_form_id, dof.item_strength, " +
		"	dof.item_strength_units, dof.frequency, dof.item_remarks as remarks, '' as route_of_admin, dof.strength, " +
		"	dof.admin_strength, dof.special_instr, dof.duration, dof.duration_units, mdm.per_day_qty " +
   		" FROM doctor_other_favourites dof " +
		"	LEFT JOIN medicine_dosage_master mdm ON (mdm.dosage_name = dof.frequency)" +
   		" WHERE (dof.item_name ilike ? OR dof.item_name ilike ?) AND dof.doctor_id = ? AND non_hosp_medicine=? limit 100";

	private static final String DOC_FAV_GENERICS =
		" SELECT COALESCE(dmf.medicine_quantity, 0) as qty, dmf.consumption_uom, dmf.item_form_id, dmf.item_strength, " +
		"	dmf.item_strength_units, dmf.frequency, dmf.medicine_remarks as remarks, dmf.route_of_admin, dmf.strength, " +
		"	dmf.admin_strength, dmf.special_instr, dmf.duration, dmf.duration_units, " +
		"	gn.generic_name as item_name, gn.generic_code as item_id, gn.generic_code, gn.generic_name, " +
		"	false as ispkg, 'item_master' as master, 'Medicine' as item_type, '' as order_code, '' as prior_auth_required," +
		"	'N' as tooth_num_required, '' as package_uom, '' as issue_uom, '' as dept_id, 'N' as granular_units, mdm.per_day_qty" +
		" FROM doctor_medicine_favourites dmf" +
		"	JOIN generic_name gn ON (gn.generic_code = dmf.generic_code)" +
		"	LEFT JOIN medicine_dosage_master mdm ON (mdm.dosage_name = dmf.frequency)" +
		" WHERE (generic_name ilike ? OR generic_name ilike ?) AND dmf.doctor_id = ? " +
		" ORDER BY generic_name limit 100";

	private static final String DOC_FAV_PHARMA_MEDICINES_DRUG_CODE =
		" SELECT sic.item_code as drug_code, sid.medicine_name as item_name, sid.medicine_id::text as item_id, " +
		"	g.generic_code, g.generic_name, false as ispkg, 'item_master' as master, 'Medicine' as item_type, " +
		"	'' as order_code, sid.prior_auth_required, 'N' as tooth_num_required, " +
		"	COALESCE(sid.package_uom, '') as package_uom, COALESCE(sid.issue_units,'') as issue_uom, '' as dept_id, " +
		"	COALESCE(ifm.granular_units, 'N') as granular_units, " +
		"	COALESCE(dmf.medicine_quantity, 0) as qty, dmf.consumption_uom, COALESCE(dmf.item_form_id, 0) as item_form_id, dmf.item_strength, " +
		"	dmf.item_strength_units, dmf.frequency, dmf.medicine_remarks as remarks, dmf.route_of_admin, dmf.strength, " +
		"	dmf. admin_strength, dmf.special_instr, dmf.duration, dmf.duration_units, mdm.per_day_qty " +
		" FROM doctor_medicine_favourites dmf " +
		"	JOIN store_stock_details ssd ON (ssd.medicine_id = dmf.medicine_id)" +
		"   JOIN stores s ON (s.dept_id=ssd.dept_id and auto_fill_prescriptions) " +
		"   JOIN store_item_details sid ON (sid.medicine_id = ssd.medicine_id) " +
		"   LEFT JOIN generic_name g ON (sid.generic_name=g.generic_code) " +
		"	LEFT JOIN item_form_master ifm ON (sid.item_form_id = ifm.item_form_id)" +
		"	LEFT JOIN medicine_dosage_master mdm ON (mdm.dosage_name = dmf.frequency)" +
		// drug code
		"   LEFT JOIN ha_item_code_type hict ON (hict.medicine_id = sid.medicine_id AND hict.health_authority= ? ) " +
		"	LEFT JOIN store_item_codes sic ON (sic.medicine_id = hict.medicine_id AND sic.code_type = hict.code_type) " +

		" WHERE (medicine_name ilike ? OR g.generic_name ilike ? OR medicine_name ilike ?) AND dmf.doctor_id = ? " +
		" GROUP BY sic.item_code, medicine_name, sid.medicine_id, g.generic_name, g.generic_code, dmf.strength, " +
		"   dmf.consumption_uom, sid.prior_auth_required, dmf.item_form_id, dmf.frequency, dmf.route_of_admin," +
		"	sid.item_form_id, dmf.item_strength, dmf.item_strength_units, dmf.medicine_quantity, " +
		"	sid.package_uom, sid.issue_units, ifm.granular_units, dmf.special_instr, dmf.medicine_remarks," +
		"	dmf. admin_strength, dmf.duration, dmf.duration_units, mdm.per_day_qty, dmf.doctor_id " +
		" ORDER BY medicine_name limit 100";

   	private static final String DOC_FAV_NON_HOSPITAL_MEDICINES =
   		" SELECT pmm.medicine_name as item_name, '' as item_id, g.generic_code, g.generic_name, false as ispkg, " +
   		"	'op' as master, 'Medicine' as item_type, '' as consumption_uom, '' as order_code, '' as prior_auth_required," +
   		"	'N' as tooth_num_required, '' as package_uom, '' as issue_uom, '' as dept_id, 'N' as granular_units, " +
		"	COALESCE(domf.medicine_quantity, 0) as qty, domf.consumption_uom, COALESCE(domf.item_form_id, 0) AS item_form_id, " +
		"	domf.item_strength_units, domf.frequency, domf.route_of_admin, domf.strength, mdm.per_day_qty, domf.item_strength," +
		"	domf.admin_strength,  domf.medicine_remarks as remarks, domf.special_instr, domf.duration, domf.duration_units " +
   		" FROM doctor_other_medicine_favourites domf " +
		"	JOIN prescribed_medicines_master pmm ON (domf.medicine_name = pmm.medicine_name)" +
   		"	LEFT JOIN generic_name g ON (g.generic_code = pmm.generic_name) " +
   		"	LEFT JOIN medicine_dosage_master mdm ON (mdm.dosage_name = domf.frequency)" +
   		" WHERE (pmm.medicine_name ilike ? or g.generic_name ilike ? OR pmm.medicine_name ilike ?) AND domf.doctor_id = ? limit 100";

   	private static final String DOC_FAV_ALL_TESTS_FIELDS_FOR_ORG =
		" SELECT atpv.test_name as item_name, atpv.test_id as item_id, 0 as qty, '' as generic_code, " +
		"	'' as generic_name, atpv.ispkg, 'item_master' as master, 'Inv.' as item_type, " +
		"	'' as consumption_uom, order_code::text, '' as route_of_admin, '' as admin_strength, '' as frequency," +
		"	atpv.prior_auth_required, 0 as item_form_id, '' as item_strength, 0 as item_strength_units," +
		"	'N' as tooth_num_required, '' as package_uom, '' as issue_uom, atpv.ddept_id as dept_id, 'N' as granular_units," +
		"	coalesce(tod.code_type,pod.code_type) as code_type, '' as duration, '' as duration_units, '' as strength," +
		"	coalesce(tod.item_code,pod.item_code) as item_code, dtf.test_remarks AS remarks, dtf.special_instr " +
		" FROM doctor_test_favourites dtf " +
		" 	JOIN all_tests_pkgs_view atpv ON (atpv.test_id = dtf.test_id)" +
		"	LEFT OUTER JOIN test_org_details tod ON tod.org_id=? AND " +
		"		atpv.test_id=tod.test_id " +
		"	LEFT OUTER JOIN pack_org_details pod ON atpv.test_id=pod.package_id::text AND " +
		"		pod.org_id=?  " +
		" WHERE (case when atpv.ispkg then exists " +
		"		(select package_id as pack_id from center_package_applicability pcm where pcm.package_id::text=atpv.test_id AND (pcm.center_id=? or pcm.center_id=-1)) else true end) " +
		"	AND (case when atpv.ispkg then exists " +
		"		(select pack_id from package_sponsor_master psm where psm.pack_id::text=atpv.test_id AND (psm.tpa_id=? OR psm.tpa_id = '-1')) else true end) " +
		"	AND (test_name ilike ? OR order_code ilike ? OR test_name ilike ?) AND dtf.doctor_id = ? limit 100";


	private static final String DOC_FAV_SERVICES_FIELDS_FOR_ORG =
		" SELECT s.service_name as item_name, s.service_id as item_id, 0 as qty, '' as generic_code, " +
		"	'' as generic_name, false as ispkg, 'item_master' as master, 'Service' as item_type, '' as route_of_admin, " +
		"	'' as consumption_uom, service_code::text as order_code, '' as admin_strength, '' as frequency," +
		"	s.prior_auth_required, 0 as item_form_id, '' as item_strength, 0 as item_strength_units, " +
		"	tooth_num_required, '' as package_uom, '' as issue_uom, '' as dept_id, 'N' as granular_units," +
		"	sod.code_type as code_type,sod.item_code as item_code, dsf.service_remarks as remarks, dsf.special_instr, " +
		"	'' as duration, '' as duration_units, '' as strength" +
		" FROM doctor_service_favourites dsf " +
		" 	JOIN services s ON (s.service_id = dsf.service_id)" +
		"	JOIN services_departments dep ON (dep.serv_dept_id=s.serv_dept_id) "+
		"	LEFT JOIN department_type_master dt ON (dep.dept_type_id=dt.dept_type_id)" +
		" 	LEFT OUTER JOIN service_org_details sod ON sod.org_id=? AND sod.service_id=s.service_id " +
		" WHERE (service_name ilike ? OR service_code ilike ? OR service_name ilike ?) AND dsf.doctor_id = ? limit 100";

	private static final String DOC_FAV_ALL_DOCTORS_FIELDS =
		" SELECT doctor_name as item_name, d.doctor_id as item_id, 0 as qty, '' as generic_code, '' as generic_name, " +
		"	false as ispkg, 'item_master' as master, 'Doctor' as item_type, '' as duration, '' as duration_units, '' as strength, " +
		"	'' as consumption_uom, '' as order_code, '' as route_of_admin,  '' as admin_strength, '' as frequency," +
		"	'' as prior_auth_required, 0 as item_form_id, '' as item_strength, 0 as item_strength_units, 'N' as tooth_num_required, " +
		"	'' as package_uom, '' as issue_uom, '' as dept_id, 'N' as granular_units, '' as code_type,'' as item_code, " +
		"	dcf.consultation_remarks as remarks, dcf.special_instr" +
		" FROM doctor_consultation_favourites dcf " +
		"	JOIN doctors d ON (d.doctor_id = dcf.cons_doctor_id)" +
		" WHERE (doctor_name ilike ? or doctor_name ilike ? ) AND dcf.doctor_id = ? limit 100";

	private static final String DOC_FAV_ALL_OPERATIONS_FIELDS =
		" SELECT operation_name as item_name, op_id as item_id, 0 as qty, '' as generic_code, " +
		"	'' as generic_name, false as ispkg, 'item_master' as master, 'Operation' as item_type, " +
		"	'' as consumption_uom, operation_code::text as order_code, '' as route_of_admin, '' as admin_strength, '' as frequency," +
		"	prior_auth_required, 0 as item_form_id, '' as item_strength, 0 as item_strength_units, " +
		"	'N' as tooth_num_required, '' as package_uom, '' as issue_uom, 'N' as granular_units," +
		"   ood.code_type,ood.item_code, dof.remarks, dof.special_instr, '' as duration, '' as duration_units, '' as strength " +
		" FROM doctor_operation_favourites dof" +
		"	JOIN operation_master om ON (om.op_id = dof.operation_id)" +
		" LEFT OUTER JOIN operation_org_details ood ON ood.org_id=? AND ood.operation_id=om.op_id "+
		" WHERE (operation_name ilike ? OR operation_code ilike ? OR operation_name ilike ?) AND dof.doctor_id = ? limit 100";

	public static List getDoctorFavouriteItems(String orgId, String type, String findItem, String use_store_items,
			String consult_doctor_id, Boolean non_dental_services, String tpaId, String centerIdStr,
			boolean forceUnUseOfGenerics, String patientHealthAutority, Boolean non_hosp_medicine) throws SQLException {
		use_store_items = use_store_items == null ? "" : use_store_items;
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
	    String prescByGenerics = (String) HealthAuthorityPreferencesDAO.getHealthAuthorityPreferences(
					CenterMasterDAO.getHealthAuthorityForCenter(Integer.parseInt(centerIdStr))).getPrescriptions_by_generics();
	    if (patientHealthAutority.isEmpty()) {
	    	// if patient healthautority is emplty , passing the user heathautority value
	    	patientHealthAutority = (String)CenterMasterDAO.getHealthAuthorityForCenter(Integer.parseInt(centerIdStr));
	    }
		boolean useGenerics = prescByGenerics.equals("Y");
		BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
		int centerId = -1;
		if ((Integer) genericPrefs.get("max_centers_inc_default") > 1 && centerIdStr != null && !centerIdStr.equals(""))
			centerId = Integer.parseInt(centerIdStr);
		try {
			String query = "";
			if (type.equals("NonHospital") || (type.equals("Medicine") && non_hosp_medicine)) {
				query = DOC_FAV_NON_HOSPITAL_ITEMS;

			} else if (type.equals("Medicine")) {
				if (use_store_items.equals("Y")) {
					if (useGenerics)
						query = DOC_FAV_GENERICS;
					else
						query = DOC_FAV_PHARMA_MEDICINES_DRUG_CODE;
				} else {
					query = DOC_FAV_NON_HOSPITAL_MEDICINES;
				}
			} else if (type.equals("Inv.")) {
				query = DOC_FAV_ALL_TESTS_FIELDS_FOR_ORG;
			} else if (type.equals("Service")) {
				query = DOC_FAV_SERVICES_FIELDS_FOR_ORG;
			} else if (type.equals("Doctor")) {
				query = DOC_FAV_ALL_DOCTORS_FIELDS;
			} else if (type.equals("Operation")) {
				query = DOC_FAV_ALL_OPERATIONS_FIELDS;
			}
			ps = con.prepareStatement(query);
			if (type.equals("NonHospital") || (type.equals("Medicine") && non_hosp_medicine)) {
				ps.setString(1, findItem + "%");
				ps.setString(2, "% " +findItem+ "%");
				ps.setString(3, consult_doctor_id);
				ps.setBoolean(4, type.equals("NonHospital") ? false : non_hosp_medicine);
			} else if (type.equals("Medicine")) {
				if (use_store_items.equals("Y")) {
					if (useGenerics) {
						ps.setString(1, findItem + "%");
						ps.setString(2, "% " +findItem+ "%");
						ps.setString(3, consult_doctor_id);
					} else {
						ps.setString(1, patientHealthAutority);
						ps.setString(2, findItem + "%");		// name starts with "xx"
						ps.setString(3, findItem + "%");		// generic starts with "xx"
						ps.setString(4, "% " +findItem + "%");	// name contains " xx"
						ps.setString(5, consult_doctor_id);
					}
				} else {
					ps.setString(1, findItem + "%");		// name starts with "xx"
					ps.setString(2, findItem + "%");		// generic starts with "xx"
					ps.setString(3, "% " +findItem+ "%");
					ps.setString(4, consult_doctor_id);
				}

			} else if (type.equals("Service")) {
				ps.setString(1, orgId);
				ps.setString(2, findItem + "%");
				ps.setString(3, findItem + "%");
				ps.setString(4, "% " +findItem+ "%");
				ps.setString(5, consult_doctor_id);
			} else if (type.equals("Inv.")) {
				ps.setString(1, orgId);
				ps.setString(2, orgId);
				ps.setInt(3, centerId);
				ps.setString(4, tpaId);
				ps.setString(5, findItem + "%");
				ps.setString(6, findItem + "%");
				ps.setString(7, "% " +findItem+ "%");
				ps.setString(8, consult_doctor_id);
			} else if (type.equals("Doctor")) {
				ps.setString(1, findItem + "%");
				ps.setString(2, "% " +findItem+ "%");
				ps.setString(3, consult_doctor_id);
			} else if (type.equals("Operation")) {
				ps.setString(1, orgId);
				ps.setString(2, findItem +"%");
				ps.setString(3, findItem + "%");
				ps.setString(4, "% " +findItem+ "%");
				ps.setString(5, consult_doctor_id);
			}
			return DataBaseUtil.queryToDynaList(ps);
		}  finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}


}
