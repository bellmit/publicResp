package com.insta.hms.core.billing;

import org.springframework.stereotype.Component;

@Component
public class BillChargeHelper {

  public final String UPDATE_TEST_CHARGES = " UPDATE bill_charge bc SET amount = (ic.charge * act_quantity)-ic.discount, "+
			" discount = ic.discount, act_rate = ic.charge, act_rate_plan_item_code = org.item_code, "+  
      " code_type = org.code_type "+
			" FROM test_org_details org "+                                                                                                                                                       
			" JOIN diagnostic_charges ic ON(org.test_id = ic.test_id AND org.org_id = ic.org_name) "+                                                                                              
			" WHERE org.test_id = bc.act_description_id AND org.applicable = true AND "+
			" bc.status != 'X'  AND bc.charge_group = 'DIA' AND org.org_id = ? AND ic.bed_type = ? AND "+
			" bc.bill_no in (#) ";
	
	public final String UPDATE_SERVICE_CHARGES = " UPDATE bill_charge bc SET amount = (ic.unit_charge * act_quantity)-ic.discount, "+
			" discount = ic.discount, act_rate = ic.unit_charge, act_rate_plan_item_code = org.item_code,  "+ 
	    " code_type = org.code_type "+
			" FROM service_org_details org "+                                                                                                                                                       
			" JOIN service_master_charges ic ON(org.service_id = ic.service_id AND org.org_id = ic.org_id) "+                                                                                              
			" WHERE org.service_id = bc.act_description_id AND org.applicable = true AND "+
			" bc.status != 'X'  AND bc.charge_group = 'SNP' AND org.org_id = ? AND ic.bed_type = ? AND "+
			" bc.bill_no in (#) ";
	
	public final String UPDATE_PACKAGE_CHARGE = " UPDATE bill_charge bc SET amount = (ic.charge * act_quantity)-ic.discount, "+
			" discount = ic.discount, act_rate = ic.charge, act_rate_plan_item_code = org.item_code, "+
	    " code_type = org.code_type "+
			" FROM pack_org_details org "+                                                                                                                                                       
			" JOIN package_charges ic ON(org.package_id = ic.package_id AND org.org_id = ic.org_id) "+                                                                                              
			" WHERE org.package_id::varchar = bc.act_description_id AND org.applicable = true AND "+
			" bc.status != 'X'  AND bc.charge_group = 'PKG' AND org.org_id = ? AND ic.bed_type = ? AND "+
			" bc.bill_no in (#) ";
	
	public final String GET_CONSULTATION_CHARGES = " SELECT  dc.*, bc.charge_id, bc.consultation_type_id, bc.act_quantity, b.bill_no, bc.username "+
			" FROM doctor_consultation dc  "+
			" JOIN patient_registration pr USING (patient_id) "+
			" JOIN bill b ON (b.visit_id = pr.patient_id)  "+
			" JOIN bill_charge bc ON (bc.bill_no = b.bill_no AND bc.consultation_type_id != 0) "+
			" JOIN bill_activity_charge bac ON bac.charge_id= bc.charge_id "+
			" AND bac.activity_id = dc.consultation_id::varchar "+
			" AND bc.charge_group != 'PKG' WHERE bc.bill_no in (#) ";
	
	public final String GET_REGISTRATION_CHARGES = "SELECT bc.charge_id, bc.charge_group, bc.charge_head, bc.act_description_id, bc.act_rate, "+ 
			" bc.act_quantity, bc.amount, bc.discount, bc.username "+
			" FROM bill_charge bc "+
			" WHERE bc.charge_group='REG' AND bc.bill_no in(#)";
	
	public final String UPDATE_OTHER_CHARGES = " UPDATE bill_charge bc SET amount = ic.charge * act_quantity, "+
			" act_rate = ic.charge "+                                                                           
			" FROM common_charges_master ic "+                                                                                                                                                       
			" WHERE ic.charge_name = bc.act_description_id AND "+
			" bc.status != 'X'  AND bc.charge_group = 'OTC' AND bc.charge_head NOT IN('EQ','MIS') AND "+
			" bc.bill_no in (#) ";
	
	public final String GET_EQUIPMENT_CHARGES = "SELECT bc.charge_id, bc.charge_group, bc.charge_head, bc.act_description_id, bc.act_rate, "+ 
      " bc.act_quantity, bc.amount, bc.discount, bc.username "+
      " FROM bill_charge bc "+
      " WHERE bc.charge_head='EQUOTC' AND bc.bill_no in(#)";
	
	public final String UPDATE_DYNA_PACKAGE_CHARGES = "UPDATE bill b SET dyna_package_charge = dpc.charge "+
			" FROM dyna_package_charges dpc "+
			" WHERE dpc.org_id = ? AND dpc.bed_type = ? AND b.dyna_package_id = dpc.dyna_package_id AND b.bill_no in (#) ";

	public final String GET_PACKAGE_CONTENT_CHARGES = "SELECT bc.charge_id, bc.charge_group, bc.charge_head, bc.act_description_id, bc.act_rate, "+
			" bc.act_quantity, bc.amount, bc.discount, bc.username, bc.package_id "+
			" FROM bill_charge bc "+
			" WHERE bc.status != 'X' AND (bc.charge_head='PKGPKG' OR bc.package_id IS NOT NULL) AND bc.bill_no in(#)";
}
