/**
 *
 */
package com.insta.hms.master.ConsultationFavourites;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author krishna
 *
 */
public class ConsultationFavouritesDAO extends GenericDAO {

	String table = null;
	public ConsultationFavouritesDAO(String table) {
		super(table);
		this.table = table;
	}

	private static final String ALL_PHARMA_MEDICINES_FAVOURITES =
		" SELECT doctor_id, 2 as category_display_order, dmf.display_order, sid.medicine_name as item_name, " +
		"	sid.medicine_id::text as item_id, medicine_quantity, " +
		" 	favourite_id, frequency as medicine_dosage, frequency, strength, " +
		"	medicine_remarks as item_remarks, dmf.cons_uom_id, cum.consumption_uom, g.generic_name, g.generic_code, 'item_master' as master, " +
		"	'Medicine' as item_type, false as ispackage, mr.route_id, mr.route_name, icm.category AS category_name, " +
		"	sid.prior_auth_required, coalesce(dmf.item_form_id, 0) as item_form_id, dmf.item_strength, if.item_form_name, " +
		"	'N' as tooth_num_required, false as non_hosp_medicine, duration, duration_units, " +
		"	dmf.item_strength_units, su.unit_name, dmf.admin_strength, if.granular_units, '' as drug_code, special_instr, " +
		"	iic.insurance_category_id, iic.insurance_category_name " +
		" FROM doctor_medicine_favourites dmf " +
		"	LEFT JOIN store_item_details sid ON (dmf.medicine_id=sid.medicine_id) " +
		" 	LEFT JOIN item_insurance_categories iic ON (iic.insurance_category_id=sid.insurance_category_id) " +
		" 	LEFT JOIN item_form_master if ON (dmf.item_form_id=if.item_form_id) " +
		"	LEFT OUTER JOIN generic_name g ON (dmf.generic_code = g.generic_code) " +
		"	LEFT OUTER JOIN medicine_route mr ON (mr.route_id=dmf.route_of_admin)" +
		"   LEFT OUTER JOIN store_category_master icm on sid.med_category_id=icm.category_id " +
		" 	LEFT OUTER JOIN strength_units su ON (su.unit_id=dmf.item_strength_units) " +
		"   LEFT JOIN consumption_uom_master cum ON (cum.cons_uom_id = dmf.cons_uom_id)" +
		" WHERE doctor_id=? #pharma_med_status# ";
	private static final String ALL_OP_MEDICINES_FAVOURITES =
		" SELECT doctor_id, 2 as category_display_order, domf.display_order, domf.medicine_name as item_name, '' as item_id, medicine_quantity, favourite_id," +
		"	frequency as medicine_dosage, frequency, strength, medicine_remarks as item_remarks, " +
		"	domf.cons_uom_id, cum.consumption_uom, g.generic_name, g.generic_code, 'op' as master, 'Medicine' as item_type, " +
		"	false as ispackage, mr.route_id, mr.route_name, '' AS category_name, '' as prior_auth_required, " +
		"	coalesce(domf.item_form_id, 0) as item_form_id, domf.item_strength, ifm.item_form_name, 'N' as tooth_num_required, " +
		"	false as non_hosp_medicine, duration, duration_units, domf.item_strength_units, su.unit_name, " +
		" 	domf.admin_strength, ifm.granular_units, '' as drug_code, special_instr, null as insurance_category_id, " +
		"	'' as insurance_category_name " +
		" FROM doctor_other_medicine_favourites domf " +
		"	LEFT JOIN item_form_master ifm ON (domf.item_form_id = ifm.item_form_id)" +
		" 	JOIN prescribed_medicines_master pms ON (domf.medicine_name=pms.medicine_name)"+
   		" 	LEFT JOIN generic_name g ON (pms.generic_name=g.generic_code)" +
   		"	LEFT JOIN medicine_route mr ON (mr.route_id=domf.route_of_admin) " +
   		"	LEFT JOIN strength_units su ON (su.unit_id=domf.item_strength_units)" +
   		"   LEFT JOIN consumption_uom_master cum ON (cum.cons_uom_id = domf.cons_uom_id)" +
		" WHERE doctor_id=? #pres_med_status# ";

	private static final String ALL_TEST_FAVOURITES =
		" SELECT doctor_id, 1 as category_display_order, dtf.display_order, atp.test_name, atp.test_id as item_id, 0, favourite_id, '', '', '', test_remarks as item_remarks, " +
		"	null as cons_uom_id, '' as consumption_uom, '', '', 'item_master' as master, 'Inv.' as item_type, ispkg as ispackage, " +
		"	-1 as route_id, '' as route_name,'' AS category_name, atp.prior_auth_required, " +
		"	0 as item_form_id, '' as item_strength, '' as item_form_name, 'N' as tooth_num_required, false as non_hosp_medicine, " +
		"	0 as duration, '' as duration_units, 0 as item_strength_units, '' as unit_name, '' as admin_strength, 'N' as granular_units, '' as drug_code, special_instr, " +
		"	iic.insurance_category_id, iic.insurance_category_name " +
		" FROM doctor_test_favourites dtf " +
		"	JOIN all_tests_pkgs_view atp ON (atp.test_id = dtf.test_id)" +
		" 	JOIN item_insurance_categories iic ON (iic.insurance_category_id=atp.insurance_category_id) " +
		" WHERE doctor_id=? #test_status# ";

	private static final String ALL_SERVICE_FAVOURITES=
		" SELECT doctor_id, 3 as category_display_order, dsf.display_order, s.service_name as item_name, s.service_id as item_id, 0, favourite_id, '', '',  '', " +
		"	service_remarks as item_remarks, null as cons_uom_id, '' as consumption_uom, '', '', 'item_master' as master, 'Service' as item_type, " +
		"	false as ispackage, -1 as  route_id, '' as route_name,'' AS category_name, s.prior_auth_required, " +
		"	0 as item_form_id, '' as item_strength, '' as item_form_name, s.tooth_num_required, false as non_hosp_medicine, " +
		"	0 as duration, '' as duration_units, 0 as item_strength_units, '' as unit_name, '' as admin_strength, 'N' as granular_units, '' as drug_code, special_instr,  " +
		"	iic.insurance_category_id, iic.insurance_category_name " +
		" FROM doctor_service_favourites dsf " +
		"	JOIN services s ON (s.service_id = dsf.service_id)" +
		" 	JOIN item_insurance_categories iic ON (iic.insurance_category_id=s.insurance_category_id) " +
		" WHERE doctor_id=? #service_status# ";

	private static final String ALL_OTHER_FAVOURITES =
		" SELECT doctor_id, 4 as category_display_order, dopf.display_order, om.operation_name as item_name, om.op_id as item_id, 0, favourite_id, '', '', '', " +
		"	dopf.remarks as item_remarks, null as cons_uom_id, '' as consumption_uom, '', '', 'item_master' as master, 'Operation' as item_type, " +
		"	false as ispackage, -1 as route_id, '' as route_name, '' AS category_name, om.prior_auth_required, " +
		"	0 as item_form_id, '' as item_strength, '' as item_form_name, 'N' as tooth_num_required, false as non_hosp_medicine, " +
		"	0 as duration, '' as duration_units, 0 as item_strength_units, '' as unit_name, '' as admin_strength, 'N' as granular_units, '' as drug_code, special_instr,  " +
		"	iic.insurance_category_id, iic.insurance_category_name " + 
		" FROM doctor_operation_favourites dopf " +
		"	JOIN operation_master om ON (dopf.operation_id=om.op_id)	" +
		" 	JOIN item_insurance_categories iic ON (iic.insurance_category_id=om.insurance_category_id) " +
		" WHERE doctor_id=? #operation_status# " +

		" UNION " +
		" SELECT dcf.doctor_id, 5 as category_display_order, dcf.display_order, d.doctor_name as item_name, d.doctor_id as item_id, " +
		"	0, favourite_id, '', '',  '', consultation_remarks as item_remarks, null as cons_uom_id, '' as consumption_uom, '', '', " +
		"	'item_master' as master, 'Doctor' as item_type, false as ispackage, " +
		"	-1 as route_id, '' as route_name,'' AS category_name, '' as prior_auth_required, 0 as item_form_id, " +
		"	'' as item_strength, '' as item_form_name, 'N' as tooth_num_required, false as non_hosp_medicine, " +
		"	0 as duration, '' as duration_units, 0 as item_strength_units, '' as unit_name, '' as admin_strength, 'N' as granular_units, '' as drug_code, special_instr, " +
		"	iic.insurance_category_id, iic.insurance_category_name " +
		" FROM doctor_consultation_favourites dcf "+ 
		" 	JOIN doctors d ON (dcf.cons_doctor_id = d.doctor_id) " +
		"	JOIN consultation_types ct ON (ct.consultation_type_id=-1) " +
		"	JOIN item_insurance_categories iic ON (iic.insurance_category_id=ct.insurance_category_id) " +
		" WHERE dcf.doctor_id=? #doctor_status# " +

		" UNION " +
		" SELECT doctor_id, 6 as category_display_order, dof.display_order, item_name, '' as item_id, medicine_quantity, favourite_id, frequency as medicine_dosage, " +
		"	frequency, strength, item_remarks, dof.cons_uom_id, cum.consumption_uom, '', '', 'op' as master, " +
		"	case when non_hosp_medicine then 'Medicine' else 'NonHospital' END as item_type, false as ispackage, -1 as route_id, '' as route_name,'' AS category_name, " +
		"	'' as prior_auth_required, dof.item_form_id, dof.item_strength, if.item_form_name, 'N' as tooth_num_required, " +
		"	non_hosp_medicine, duration, duration_units, item_strength_units, su.unit_name, dof.admin_strength, if.granular_units, '' as drug_code, special_instr, " +
		"	null as insurance_category_id, '' as insurance_category_name " +
		" FROM doctor_other_favourites dof " +
		"	LEFT JOIN item_form_master if ON (dof.item_form_id=if.item_form_id) " +
		"	LEFT JOIN strength_units su ON (su.unit_id=dof.item_strength_units) " +
		"   LEFT JOIN consumption_uom_master cum ON (cum.cons_uom_id = dof.cons_uom_id) " +
		" WHERE doctor_id=? " ;

	public static List getAllFavourites(String doctorId, String use_store_items) throws SQLException {
		use_store_items = use_store_items == null ? "" : use_store_items;
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			String query = (use_store_items.equals("Y") ? ALL_PHARMA_MEDICINES_FAVOURITES : ALL_OP_MEDICINES_FAVOURITES);
			query += " UNION " + ALL_TEST_FAVOURITES + " UNION " + ALL_SERVICE_FAVOURITES + " UNION " + ALL_OTHER_FAVOURITES;
			query += " ORDER BY category_display_order, display_order, item_name" ;
			query = query.replace("#pharma_med_status#", " AND case when dmf.medicine_id is null then g.status='A' else sid.status='A' end ");
			query = query.replace("#pres_med_status#", " AND pms.status='A'");
			query = query.replace("#test_status#", " AND atp.status='A'");
			query = query.replace("#service_status#", " AND s.status='A'");
			query = query.replace("#operation_status#", " AND om.status='A'");
			query = query.replace("#doctor_status#", " AND d.status='A'");

			ps = con.prepareStatement(query);
			ps.setString(1, doctorId);
			ps.setString(2, doctorId);
			ps.setString(3, doctorId);
			ps.setString(4, doctorId);
			ps.setString(5, doctorId);
			ps.setString(6, doctorId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String ALL_TEST_FAVOURITES_FOR_ORG =
		" SELECT doctor_id, 1 as category_display_order, dtf.display_order, d.test_name, " +
		" d.test_id as item_id, 0, favourite_id, '', '', '', test_remarks as item_remarks,  " +
		" null as cons_uom_id, '' as consumption_uom, '', '', 'item_master' as master, 'Inv.' as item_type, false as ispackage, " +
		" -1 as route_id, '' as route_name,'' AS category_name, d.prior_auth_required, 0 as item_form_id, " +
		" '' as item_strength, '' as item_form_name, 'N' as tooth_num_required, false as non_hosp_medicine, " +
		" 0 as duration, '' as duration_units, 0 as item_strength_units, '' as unit_name, '' as admin_strength, " +
		" 'N' as granular_units, '' as drug_code, special_instr, d.insurance_category_id, iic.insurance_category_name, " +
		" dc.charge, dc.discount, '' as package_type, '' AS pack_type, dd.category, 0 as item_batch_id, '' as batch_no, " +
		" '' as doctor_charge_type, ipd.category_payable, 0 AS issue_base_unit " +
		" FROM doctor_test_favourites dtf " +
		" JOIN diagnostics d ON (d.test_id=dtf.test_id) " +
		" JOIN item_insurance_categories iic ON (iic.insurance_category_id=d.insurance_category_id) " +
		" JOIN test_org_details tod ON (tod.org_id=? " +
		"		AND tod.applicable AND d.test_id=tod.test_id )" +
		" LEFT JOIN diagnostics_departments dd ON (dd.ddept_id=d.ddept_id)  " +
		" LEFT JOIN diagnostic_charges dc ON (d.test_id=dc.test_id " +
		"		AND dc.org_name=? AND dc.bed_type=?) " +
		" LEFT JOIN insurance_plan_details ipd on (d.insurance_category_id = ipd.insurance_category_id  " +
		"		AND ipd.patient_type=? AND ipd.plan_id=?) " +
		" WHERE dtf.doctor_id=? #test_status# " +
		" UNION ALL " +
		" SELECT doctor_id, 1 as category_display_order, dtf.display_order, pm.package_name, " +
		" pm.package_id::text as item_id, 0, favourite_id, '', '', '', test_remarks as item_remarks, " +
		" '' as consumption_uom, '', '', 'item_master' as master, 'Inv.' as item_type, true as ispackage, " +
		" -1 as route_id, '' as route_name, '' AS category_name, pm.prior_auth_required, 0 as item_form_id, " +
		" '' as item_strength, '' as item_form_name, 'N' as tooth_num_required, false as non_hosp_medicine, " +
		" 0 as duration, '' as duration_units, 0 as item_strength_units, '' as unit_name, '' as admin_strength, " +
		" 'N' as granular_units, '' as drug_code, special_instr, pm.insurance_category_id, iic.insurance_category_name, " +
		" pc.charge, pc.discount, 'd' as package_type, 'P' AS pack_type, '' AS category, 0 as item_batch_id, " +
		" '' as batch_no, '' as doctor_charge_type, ipd.category_payable AS category_payable, 0 AS issue_base_unit " +
		" FROM doctor_test_favourites dtf " +
		" JOIN packages pm ON (pm.package_id::text = dtf.test_id) " +
		" JOIN item_insurance_categories iic ON (iic.insurance_category_id=pm.insurance_category_id) " +
		" JOIN pack_org_details pod ON (pm.package_id=pod.package_id " +
		"		AND pod.applicable and pod.org_id=? )" +
		" LEFT JOIN package_charges pc ON (pc.package_id::text=dtf.test_id " +
		"	AND pc.org_id=? AND pc.bed_type =?) " +
		" LEFT JOIN insurance_plan_details ipd on (pm.insurance_category_id = ipd.insurance_category_id " +
		"		AND ipd.patient_type=? AND ipd.plan_id=?) " +
		" JOIN center_package_applicability pcm ON (pcm.package_id::text=dtf.test_id AND pcm.status='A' " +
		"		AND (pcm.center_id=? or pcm.center_id=-1)) " +
		" JOIN package_sponsor_master psm ON (psm.pack_id::text=dtf.test_id AND psm.status='A' " +
		"		AND (psm.tpa_id=? or psm.tpa_id='-1')) " +
		" WHERE pm.approval_status='A' AND doctor_id=? #package_status# ";

	private static final String ALL_SERVICE_FAVOURITES_FOR_ORG =
		" SELECT doctor_id, 3 as category_display_order, dsf.display_order, s.service_name as item_name, " +
		"	s.service_id as item_id, 0, favourite_id, '', '',  '', " +
		"	service_remarks as item_remarks, null as cons_uom_id, '' as consumption_uom, '', '', 'item_master' as master, 'Service' as item_type, " +
		"	false as ispackage, -1 as  route_id, '' as route_name,'' AS category_name, s.prior_auth_required, " +
		"	0 as item_form_id, '' as item_strength, '' as item_form_name, s.tooth_num_required, false as non_hosp_medicine, " +
		"	0 as duration, '' as duration_units, 0 as item_strength_units, '' as unit_name, '' as admin_strength," +
		"   'N' as granular_units, '' as drug_code, special_instr, iic.insurance_category_id, iic.insurance_category_name, " +
		"	unit_charge as charge, discount, '' as package_type, '' AS pack_type, '' as category," + 
		"	0 as item_batch_id, '' as batch_no, '' as doctor_charge_type,  ipd.category_payable AS category_payable, 0 AS issue_base_unit " + 
		" FROM doctor_service_favourites dsf " +
		"	JOIN services s ON (s.service_id = dsf.service_id) " +
		"	JOIN service_master_charges smc ON (smc.service_id=s.service_id AND smc.org_id=? and smc.bed_type=?) " +
		" 	LEFT JOIN insurance_plan_details ipd on (s.insurance_category_id = ipd.insurance_category_id " +
		"    AND ipd.patient_type=? AND ipd.plan_id=?) " +
		"	JOIN item_insurance_categories iic ON (iic.insurance_category_id=s.insurance_category_id) " +
		" 	LEFT OUTER JOIN service_org_details sod ON sod.org_id=? AND sod.applicable AND sod.service_id=s.service_id " +
		" WHERE doctor_id=? #service_status# ";

//	 passing patient health authority code
	private static final String ALL_PHARMA_MEDICINES_FAVOURITES_HEALTH_AUTHORITY =
		" SELECT doctor_id, 2 as category_display_order, dmf.display_order, sid.medicine_name as item_name, " +
		"	sid.medicine_id::text as item_id, medicine_quantity, " +
		" 	favourite_id, frequency as medicine_dosage, frequency, strength, " +
		"	medicine_remarks as item_remarks, dmf.cons_uom_id, cum.consumption_uom, g.generic_name, g.generic_code, 'item_master' as master, " +
		"	'Medicine' as item_type, false as ispackage, mr.route_id, mr.route_name, icm.category AS category_name, " +
		"	sid.prior_auth_required, coalesce(dmf.item_form_id, 0) as item_form_id, dmf.item_strength, if.item_form_name, " +
		"	'N' as tooth_num_required, false as non_hosp_medicine, duration, duration_units, " +
		"	dmf.item_strength_units, su.unit_name, dmf.admin_strength, if.granular_units , sic.item_code as drug_code, special_instr, " +
		"	iic.insurance_category_id, iic.insurance_category_name, " +
		"	COALESCE(sid.item_selling_price, sibd.mrp) AS charge, COALESCE(mc.discount,0) AS discount, '' as package_type, " + 
		"	'' AS pack_type, '' as category, sibd.item_batch_id, sibd.batch_no, " +
		" 	'' AS doctor_charge_type,  '' AS category_payable, sid.issue_base_unit " +
		" FROM doctor_medicine_favourites dmf  " +
		"	LEFT JOIN store_item_details sid ON (dmf.medicine_id=sid.medicine_id) " +
		"	LEFT JOIN store_item_batch_details sibd ON (sibd.medicine_id=sid.medicine_id " +
		"		AND sibd.item_batch_id=(select max(item_batch_id) from store_item_batch_details where medicine_id=sid.medicine_id)) " +
		"	LEFT JOIN item_insurance_categories iic ON (iic.insurance_category_id=sid.insurance_category_id) " +
		"   LEFT JOIN store_category_master mc ON mc.category_id = sid.med_category_id " +
		" 	LEFT JOIN item_form_master if ON (dmf.item_form_id=if.item_form_id) " +
		"	LEFT OUTER JOIN generic_name g ON (dmf.generic_code = g.generic_code) " +
		"	LEFT OUTER JOIN medicine_route mr ON (mr.route_id=dmf.route_of_admin)" +
		"   LEFT OUTER JOIN store_category_master icm on sid.med_category_id=icm.category_id " +
		" 	LEFT OUTER JOIN strength_units su ON (su.unit_id=dmf.item_strength_units) " +
		"   LEFT JOIN ha_item_code_type hict ON (hict.medicine_id = sid.medicine_id AND hict.health_authority= ? ) " +
		"	LEFT JOIN store_item_codes sic ON (sic.medicine_id = hict.medicine_id AND sic.code_type = hict.code_type) " +
		" WHERE doctor_id=? #pharma_med_status# ";

	private static final String ALL_OP_MEDICINES_FAVOURITES_NEW =
			" SELECT doctor_id, 2 as category_display_order, domf.display_order, domf.medicine_name as item_name, '' as item_id, medicine_quantity, favourite_id," +
			"	frequency as medicine_dosage, frequency, strength, medicine_remarks as item_remarks, " +
			"	domf.cons_uom_id, cum.consumption_uom, g.generic_name, g.generic_code, 'op' as master, 'Medicine' as item_type, " +
			"	false as ispackage, mr.route_id, mr.route_name, '' AS category_name, '' as prior_auth_required, " +
			"	coalesce(domf.item_form_id, 0) as item_form_id, domf.item_strength, ifm.item_form_name, 'N' as tooth_num_required, " +
			"	false as non_hosp_medicine, duration, duration_units, domf.item_strength_units, su.unit_name, " +
			" 	domf.admin_strength, ifm.granular_units, '' as drug_code, special_instr, null as insurance_category_id, " +
			"	'' as insurance_category_name " +
			"	0 AS charge, 0 AS discount, '' as package_type, " + 
			"	'' AS pack_type, '' as category, 0 AS item_batch_id, '' AS batch_no, " +
			" 	'' AS doctor_charge_type,  '' AS category_payable, 0 AS issue_base_unit " +
			" FROM doctor_other_medicine_favourites domf " +
			"	LEFT JOIN item_form_master ifm ON (domf.item_form_id = ifm.item_form_id)" +
			" 	JOIN prescribed_medicines_master pms ON (domf.medicine_name=pms.medicine_name)"+
	   		" 	LEFT JOIN generic_name g ON (pms.generic_name=g.generic_code)" +
	   		"	LEFT JOIN medicine_route mr ON (mr.route_id=domf.route_of_admin) " +
	   		"	LEFT JOIN strength_units su ON (su.unit_id=domf.item_strength_units)" +
	   		"   LEFT JOIN consumption_uom_master cum ON (cum.cons_uom_id = domf.cons_uom_id) " +
			" WHERE doctor_id=? #pres_med_status# ";

	private static final String ALL_OTHER_FAVOURITES_FOR_ORG =
			" SELECT doctor_id, 4 as category_display_order, dopf.display_order, om.operation_name as item_name, om.op_id as item_id, 0, favourite_id, '', '', '', " +
			"	dopf.remarks as item_remarks, '' as consumption_uom, '', '', 'item_master' as master, 'Operation' as item_type, " +
			"	false as ispackage, -1 as route_id, '' as route_name, '' AS category_name, om.prior_auth_required, " +
			"	0 as item_form_id, '' as item_strength, '' as item_form_name, 'N' as tooth_num_required, false as non_hosp_medicine, " +
			"	0 as duration, '' as duration_units, 0 as item_strength_units, '' as unit_name, '' as admin_strength, 'N' as granular_units, '' as drug_code, special_instr,  " +
			"	iic.insurance_category_id, iic.insurance_category_name, " + 
			"	surg_asstance_charge as charge, surg_asst_discount as discount, '' as package_type, '' AS pack_type, '' as category, " + 
			" 	0 as item_batch_id, '' as batch_no, '' as doctor_charge_type,  ipd.category_payable AS category_payable, 0 AS issue_base_unit " +
			" FROM doctor_operation_favourites dopf " +
			"	JOIN operation_master om ON (dopf.operation_id=om.op_id)	" +
			"	JOIN operation_charges oc ON (oc.op_id=om.op_id AND oc.org_id=? AND oc.bed_type=?)" +
			"  	LEFT JOIN insurance_plan_details ipd on (om.insurance_category_id = ipd.insurance_category_id " +
			"    AND ipd.patient_type=? AND ipd.plan_id=?) " +
			" 	JOIN item_insurance_categories iic ON (iic.insurance_category_id=om.insurance_category_id) " +
			" WHERE doctor_id=? #operation_status# " +

			" UNION ALL " +
			" SELECT dcf.doctor_id, 5 as category_display_order, dcf.display_order, d.doctor_name as item_name, d.doctor_id as item_id, " +
			"	0, favourite_id, '', '',  '', consultation_remarks as item_remarks, '' as consumption_uom, '', '', " +
			"	'item_master' as master, 'Doctor' as item_type, false as ispackage, " +
			"	-1 as route_id, '' as route_name,'' AS category_name, '' as prior_auth_required, 0 as item_form_id, " +
			"	'' as item_strength, '' as item_form_name, 'N' as tooth_num_required, false as non_hosp_medicine, " +
			"	0 as duration, '' as duration_units, 0 as item_strength_units, '' as unit_name, '' as admin_strength, 'N' as granular_units, '' as drug_code, special_instr, " +
			"	iic.insurance_category_id, iic.insurance_category_name, " +
			"	cc.charge, cc.discount, '' as package_type, '' AS pack_type, '' as category, 0 as item_batch_id, '' as batch_no, " +
			" 	doctor_charge_type,  ipd.category_payable AS category_payable, 0 AS issue_base_unit " +
			" FROM doctor_consultation_favourites dcf "+ 
			" 	JOIN doctors d ON (dcf.cons_doctor_id = d.doctor_id) " +
			"	JOIN consultation_types ct ON (ct.consultation_type_id=-1) " +
			"	JOIN consultation_charges cc ON (cc.consultation_type_id = -1 AND cc.org_id = ? AND cc.bed_type = ?) " +
			"  	LEFT JOIN insurance_plan_details ipd on (ct.insurance_category_id = ipd.insurance_category_id " +
			"    AND ipd.patient_type=? AND ipd.plan_id=?) " +
			"	JOIN item_insurance_categories iic ON (iic.insurance_category_id=ct.insurance_category_id) " +
			" WHERE dcf.doctor_id=? #doctor_status# " +

			" UNION ALL " +
			" SELECT doctor_id, 6 as category_display_order, dof.display_order, item_name, '' as item_id, medicine_quantity, favourite_id, frequency as medicine_dosage, " +
			"	frequency, strength, item_remarks, dof.cons_uom_id, cum.consumption_uom, '', '', 'op' as master, " +
			"	case when non_hosp_medicine then 'Medicine' else 'NonHospital' END as item_type, false as ispackage, -1 as route_id, '' as route_name,'' AS category_name, " +
			"	'' as prior_auth_required, dof.item_form_id, dof.item_strength, if.item_form_name, 'N' as tooth_num_required, " +
			"	non_hosp_medicine, duration, duration_units, item_strength_units, su.unit_name, dof.admin_strength, if.granular_units, '' as drug_code, special_instr, " +
			"	null as insurance_category_id, '' as insurance_category_name, " +
			"	0 AS charge, 0 AS discount, '' as package_type, '' AS pack_type, '' as category, 0 as item_batch_id, '' as batch_no, " +
			" 	'' AS doctor_charge_type,  '' AS category_payable, 0 AS issue_base_unit " +
			" FROM doctor_other_favourites dof " +
			"	LEFT JOIN item_form_master if ON (dof.item_form_id=if.item_form_id) " +
			"	LEFT JOIN strength_units su ON (su.unit_id=dof.item_strength_units) " +
			"   LEFT JOIN consumption_uom_master cum ON (cum.cons_uom_id = dof.cons_uom_id) " +
			" WHERE doctor_id=? " ;

	public static List getAllFavourites(String doctorId, String use_store_items, String orgId, String tpaId,
			int centerId, String patientFavHealthAutority, String bedType, Integer planId, Integer pageNo) throws SQLException {
		use_store_items = use_store_items == null ? "" : use_store_items;
		if(patientFavHealthAutority.isEmpty())
			patientFavHealthAutority = (String)CenterMasterDAO.getHealthAuthorityForCenter(centerId);

		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			String query = (use_store_items.equals("Y") ? ALL_PHARMA_MEDICINES_FAVOURITES_HEALTH_AUTHORITY : ALL_OP_MEDICINES_FAVOURITES_NEW);
			query += " UNION ALL " + ALL_TEST_FAVOURITES_FOR_ORG + " UNION ALL " + ALL_SERVICE_FAVOURITES_FOR_ORG + " UNION ALL" + ALL_OTHER_FAVOURITES_FOR_ORG;
			query += " ORDER BY category_display_order, display_order, item_name LIMIT 20 OFFSET 20*? " ;

			// get only active items favourites.
			query = query.replace("#pharma_med_status#", " AND case when dmf.medicine_id is null then g.status='A' else sid.status='A' end ");
			query = query.replace("#pres_med_status#", " AND pms.status='A'");
			query = query.replace("#test_status#", " AND d.status='A'");
			query = query.replace("#package_status#", " AND pm.status='A'");
			query = query.replace("#service_status#", " AND s.status='A'");
			query = query.replace("#operation_status#", " AND om.status='A'");
			query = query.replace("#doctor_status#", " AND d.status='A'");

			ps = con.prepareStatement(query);
			int i=1;
			// for medicines
			if (use_store_items.equals("Y"))
					ps.setString(i++, patientFavHealthAutority);
			ps.setString(i++, doctorId);
			// for tests
			ps.setString(i++, orgId);
			ps.setString(i++, orgId);
			ps.setString(i++, bedType);
			ps.setString(i++, "o");
			ps.setInt(i++, planId);
			ps.setString(i++, doctorId);
			ps.setString(i++, orgId);
			ps.setString(i++, orgId);
			ps.setString(i++, bedType);
			ps.setString(i++, "o");
			ps.setInt(i++, planId);
			ps.setInt(i++, centerId);
			ps.setString(i++, tpaId);
			ps.setString(i++, doctorId);
			// for services
			ps.setString(i++, orgId);
			ps.setString(i++, bedType);
			ps.setString(i++, "o");
			ps.setInt(i++, planId);
			ps.setString(i++, orgId);
			ps.setString(i++, doctorId);
			// for operations
			ps.setString(i++, orgId);
			ps.setString(i++, bedType);
			ps.setString(i++, "o");
			ps.setInt(i++, planId);
			ps.setString(i++, doctorId);
			// for cross consultations
			ps.setString(i++, orgId);
			ps.setString(i++, bedType);
			ps.setString(i++, "o");
			ps.setInt(i++, planId);
			ps.setString(i++, doctorId);
			// for non hospital items
			ps.setString(i++, doctorId);

			ps.setInt(i++, pageNo);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public int getMaxDisplayOrder(Connection con, String doctorId) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = con.prepareStatement("SELECT COALESCE(MAX(display_order), 0)+1 as display_order FROM "+table+" WHERE doctor_id=?");
			ps.setString(1, doctorId);
			rs = ps.executeQuery();
			int i = 1;
			while(rs.next()) {
				i = rs.getInt("display_order");
			}
			return i;
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}

}
