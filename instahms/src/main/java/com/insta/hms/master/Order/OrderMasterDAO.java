package com.insta.hms.master.Order;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDTO;
import com.insta.hms.master.HospitalRolesMaster.HospitalRolesMasterDAO;
import com.insta.hms.master.ServiceSubGroup.ServiceSubGroupDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderMasterDAO {
  static final HospitalRolesMasterDAO hospitalRoleMasterDAO = new HospitalRolesMasterDAO();
  static final ServiceSubGroupDAO serviceSubGrpDAO = new ServiceSubGroupDAO();
  
  private static final GenericDAO hospitalDirectBillPrefs =
      new GenericDAO("hosp_direct_bill_prefs");
	/*
	 * This takes the following parameters:
	 *  filter: Restrict the list to the given type. If filter is blank or null, then the list
	 *    is not restricted. eg. Laboratory/Radiology etc.
	 *  orderable: Y/N/blank: Y: lists only orderable items, N: non-orderable items (eg, reg charges)
	 *    blank: no restriction, ie, all items.
	 *  modStatus: A/I/blank: If A, includes only items whose modules are active,
	 *    if I, includes only items whose modules that are inactive,
	 *    if nothing specified, no filter is applied.
	 *
	 * Usage from various screens:
	 *  Order - normal items:   filter=         orderable=Y  modStatus=   operationApplicable=
	 *  Order - oper items:     filter=         orderable=Y  modStatus=   operationApplicable=Y
	 *  Service Order (etc.):   filter=Service  orderable=Y  modStatus=   operationApplicable=
	 *  Billing (normal):       filter=         orderable=N  modStatus=I  operationApplicable=
	 *  Billing (normal,oper):  filter=         orderable=N  modStatus=I  operationApplicable=Y
	 *  Billing (add-to-bill):  filter=         orderable=   modStatus=   operationApplicable=
	 *
	 * Following are rules to follow when adding an item from Order vs. Billing screen:
	 *
	 * 1. All orderable items are available from Order regardless of module being enabled. Bed
	 *    is not an orderable item, pure charges (Reg charge etc.) are not orderable either.
	 *
	 * 2. All items ordered from the Order screen are saved in activity tables, and
	 *    are visible in the Order screen. These can be cancelled/modified provided the activity
	 *    has not been conducted. If the module is not enabled, the conduction
	 *    never happens, so it can always be cancelled/modified.
	 *
	 * 3. Items added from the Bill screen, Bed Charges and other items not entered via the order
	 *    screen (eg pharmacy/inventory items) are not visible in the Order screen.
	 *
	 * 4. From Bill (normal) screen, only items which cannot be ordered from Order screen are
	 *    available. Thus, Billing and Order screen are mutually exclusive for normal users.
	 *
	 * 5. ADT is a special case: if mod_adt is enabled, Billing (normal) cannot add bed
	 *    charges. This is because bed allocation can be done only if ADT is there (as opposed
	 *    to orderable items can be ordered regardless of module).
	 *
	 * 6. All billable items are visible in the Bill screen, but those ordered from Order screen
	 *    can only be cancelled from the Order screen (ie, cannot cancel from Bill).
	 *
	 * 7. The billing superuser (with add-to-bill rights) can add any item to the bill, but this
	 *    will not create an activity, and thus not visible in Order, and can be cancelled from
	 *    the Bill screen itself.
	 *
	 * Note: Only Bed/ICU charges are associated with a module. Thus, modStatus only affects
	 * listing of Bed/ICU charges.
	 */


	public static List getAllOrderableItems(String orgId, String visitType,String filter,String orderable,
			String directBilling, String operationApplicable,String packageApplicable, String tpaId, Integer centerId, String deptId, String genderApplicability, List userId, Integer planId,
			Integer age, String ageIn)
	throws SQLException {

		return getAllOrderableItems(orgId,visitType,filter,orderable,directBilling,operationApplicable,packageApplicable,tpaId,centerId, deptId, genderApplicability,false, userId, false, planId,
				age, ageIn);

	}

       
  public static List getAllOrderableItems(String orgId, String visitType, String filter,
      String orderable, String directBilling, String operationApplicable, String packageApplicable,
      String tpaId, Integer centerId, String deptId, String genderApplicability,
      Boolean isMultiVisitPackage, List userId, Boolean ignoreCenter, Integer planId,
      Integer age, String ageIn) throws SQLException {
    return getAllOrderableItems(orgId, visitType, filter.split(","), orderable, directBilling,
        operationApplicable, packageApplicable, tpaId, centerId, deptId, genderApplicability,
        isMultiVisitPackage, userId, ignoreCenter, planId, age, ageIn);
  }

	//can be used if filter parameter is more than one type like "Service,Laboratory,Radiology..etc.,"
	public static List getAllOrderableItems(String orgId, String visitType, String filter,
			String orderable, String directBilling, String operationApplicable,String packageApplicable,
			String tpaId, Integer centerId, String deptId, String genderApplicability, 
			Boolean isMultiVisitPackage, List userId, Integer planId, Integer age, String ageIn)
		throws SQLException {
		return getAllOrderableItems(orgId, visitType, filter.split(","), orderable, directBilling,
				operationApplicable, packageApplicable, tpaId, centerId, deptId, genderApplicability,
				isMultiVisitPackage, userId, false, planId, age, ageIn);
	}
	public static List getAllOrderableItems(String orgId, String visitType,String[] filter,String orderable,
			String directBilling, String operationApplicable,String packageApplicable, String tpaId, Integer centerId, String deptId,
			String genderApplicability, Boolean isMultiVisitPackage, List userId, Boolean ignoreCenter, Integer planId,
			Integer age, String ageIn)

		throws SQLException {

		Map directBillingStatuses = ConversionUtils.listBeanToMapBean(
		    hospitalDirectBillPrefs.listAll(),"item_type");

		GenericPreferencesDTO genPrefs = GenericPreferencesDAO.getGenericPreferences();
		String operationOrders = "i";
		operationOrders = genPrefs.getOperationApplicableFor().equals("o")?"o"
						: genPrefs.getOperationApplicableFor().equals("b")?"":operationOrders;

		/*
		 * Dynamically construct the query based on various filters.
		 * We could have instead created a big view with Type, orderable etc. as columns and then
		 * filtered it, but some analysis showed that dynamic construction executes much faster.
		 */
		StringBuffer query = new StringBuffer();

		/*
		 * Track which queries have been added. This is because the same query can be part of
		 * multiple BillItem entries to enable better filtering.
		 */
		HashMap<String, Boolean> queryAddedMap = new HashMap<String, Boolean>();

		// rateplan independent items
		for (BillItem item : rpIndepItems) {
			// item can have empty visit type, indicating it is applicable for all.
			if (!visitType.isEmpty() && !item.visitType.isEmpty() && !item.visitType.equals(visitType))
				continue;
			if (!filter[0].isEmpty() &&  !Arrays.asList(filter).contains(item.type) )
				continue;
			if (!orderable.isEmpty() && !item.orderable.equals(orderable))
				continue;
			if (!operationApplicable.isEmpty() && !item.operationApplicable.equals(operationApplicable))
				continue;
			if (!packageApplicable.isEmpty()) {
				// pack master
				if (isMultiVisitPackage && item.multiVisitPackageApplicable.equals("N")) {
					continue;
				} else if (!isMultiVisitPackage && !item.packageApplicable.equals(packageApplicable)) {
					continue;
				}
			}

			if (item.type.equals("doctor")) {
				if (packageApplicable.isEmpty() && item.packageApplicable.equals("Y"))
					continue;
			}

			String itemDirectBilling = getItemOrderable(item);
			if (!directBilling.isEmpty() && itemDirectBilling.equals(directBilling))
				continue;

			if (null == queryAddedMap.get(item.query)) {
				if (query.length() > 0)
					query.append(" \nUNION ALL ");
				query.append(item.query);
				queryAddedMap.put(item.query, true);
			}
		}

		// rateplan dependent items, requiring org_id=
		StringBuffer rpDepQuery = new StringBuffer();
		int added_count = 0;

		for (BillItem item: rpDepItems) {
			String itemOrderable = getItemOrderable(item);
				
			if (item.type.equals("Operation"))
				item.visitType = operationOrders;

			if (!packageApplicable.isEmpty()) {
				// pack master
				if (isMultiVisitPackage && item.multiVisitPackageApplicable.equals("N"))
					continue;
				if (!isMultiVisitPackage && !item.packageApplicable.equals(packageApplicable))
					continue;
			} else {
				if (!isMultiVisitPackage && item.type.equals("MultiVisitPackage"))
					continue;
			}

			if (((item.visitType.equals("") || visitType.equals("") || item.visitType.equals(visitType)) &&
					(filter[0].isEmpty() ||  Arrays.asList(filter).contains(item.type) ) &&
					(orderable.isEmpty() || item.orderable.equals(orderable)) &&
					(directBilling.isEmpty() || itemOrderable.equals(directBilling) &&
					(operationApplicable.isEmpty() || item.operationApplicable.equals(operationApplicable))
				))) {

				if (null == queryAddedMap.get(item.query)) {
					String itemQuery = item.query;
					if (rpDepQuery.length() > 0)
						rpDepQuery.append(" \nUNION ALL ");
					if (!item.type.equals("Order Sets"))
						added_count++;
					else {
						if (genderApplicability != null)
							itemQuery += GENDER_APPLICABILITY_CONDITION;
						if (visitType != null && !visitType.isEmpty())
							itemQuery += VISIT_APPLICABILITY_CONDITION;
					}
					
					StringBuilder orderControlQuery = new StringBuilder();
					if((item.type.equals("Laboratory") && item.entity.equals("Laboratory")) ||
					    item.type.equals("Service") || (item.type.equals("Radiology") && item.entity.equals("Radiology")) ||
					    (item.type.equals("Laboratory") && item.entity.equals("DiagPackage")) || (item.type.equals("Radiology") && item.entity.equals("DiagPackage"))
					    || item.type.equals("Order Sets")){

					  List<BasicDynaBean> ordersRoleControlsList = new ArrayList();
				    List<String> itemList = new ArrayList();
				    List<Integer> subGrpIdList = new ArrayList();
				    
					  if(userId != null && userId.size() > 0) {
		          ordersRoleControlsList = hospitalRoleMasterDAO.getOrderControlRules(userId);
		        }  
		        List<Integer> grpIdList = new ArrayList();    
		        for(BasicDynaBean bean : ordersRoleControlsList){
		          if(bean.get("item_id").equals("*")){
		              if(!bean.get("service_sub_group_id").equals(-9)){
		                subGrpIdList.add((Integer) bean.get("service_sub_group_id"));
		              }else{
		                grpIdList.add((Integer) bean.get("service_group_id"));
		              }       
		          }else{
		            itemList.add((String)bean.get("item_id"));  
		          }      
		        }    
		        List<Integer> subGrps = new ArrayList();
		        if(!grpIdList.isEmpty()) {
		          subGrps = serviceSubGrpDAO.getAllServiceSubGrps(grpIdList);  
		          subGrpIdList.addAll(subGrps);
		        }       
		        
		        if(!subGrpIdList.isEmpty()){
		          if(!itemList.isEmpty()) {
		            orderControlQuery.append(" AND ( ");
		          }else{
		            orderControlQuery.append(" AND ");
		          }
		          orderControlQuery.append(" s.service_sub_group_id IN (#sub_grp_ids_list#)");
		          String value = "";
		          for(int subGrp : subGrpIdList){
		            if(!value.equals("")) {
		              value += ",";
		            }
		            value += subGrp;
		          }	  
		          orderControlQuery.replace(orderControlQuery.toString().indexOf("#"), 
		              orderControlQuery.toString().lastIndexOf("#")+1, value);
		        }	        
		        if(!itemList.isEmpty()){
		          if(!subGrpIdList.isEmpty()){
  		          if(item.type.equals("Laboratory") && item.entity.equals("Laboratory") ||
  		              (item.type.equals("Radiology") && item.entity.equals("Radiology"))) {
  		            orderControlQuery.append(" OR d.test_id IN (#item_list#))");
  		          } else if (item.type.equals("Service")){
  		            orderControlQuery.append(" OR s.service_id IN (#item_list#))");
  		          } else if (item.type.equals("Laboratory") && item.entity.equals("DiagPackage") 
  		              || item.type.equals("Radiology") && item.entity.equals("DiagPackage")){
  		            orderControlQuery.append(" OR pm.package_id::text IN (#item_list#))");
  		          } else if (item.type.equals("Order Sets")){
                  orderControlQuery.append(" OR p.package_id::text IN (#item_list#))");
                }
		          } else{
		            if(item.type.equals("Laboratory") && item.entity.equals("Laboratory") ||
		                (item.type.equals("Radiology") && item.entity.equals("Radiology"))) {
                  orderControlQuery.append(" AND d.test_id IN (#item_list#)");
                } else if (item.type.equals("Service")){
                  orderControlQuery.append(" AND s.service_id IN (#item_list#)");
                } else if (item.type.equals("Laboratory") && item.entity.equals("DiagPackage") 
                    || item.type.equals("Radiology") && item.entity.equals("DiagPackage")){
                  orderControlQuery.append(" AND pm.package_id::text IN (#item_list#)");
                } else if (item.type.equals("Order Sets")){
                  orderControlQuery.append(" AND p.package_id::text IN (#item_list#)");
                }
		          }
		          String value = "";
		          for(String itemid : itemList){
		            if(!value.equals("")) {
                  value += ",";
                }
                value += "'"+itemid +"'";
		          }		  
		          orderControlQuery.replace(orderControlQuery.toString().indexOf("#"), 
                  orderControlQuery.toString().lastIndexOf("#")+1, value);
		        }
		        itemQuery += orderControlQuery;
					}
					rpDepQuery.append(itemQuery);
					queryAddedMap.put(itemQuery, true);
					
				}
			}
		}

		if (rpDepQuery.length() > 0) {
			if (query.length() > 0)
				query.append(" \nUNION ALL ");
			// select * from all the select unions so that we can use a single org_id=? where clause
			query.append("SELECT type, id, name, code, department, subGrpId, groupid, prior_auth_required,")
				.append("insurance_category_id, conduction_applicable, conducting_doc_mandatory, ")
				.append("results_entry_applicable, tooth_num_required, multi_visit_package, center_id, tpa_id, dept_id, " +
						"mandate_additional_info, additional_info_reqts ")
				.append("FROM (").append(rpDepQuery).append(") AS depItems ");
		}

		// sort the whole thing on the name
		if (query.length() > 0)
			query.append(" ORDER BY name");

		Connection con = null;
		PreparedStatement ps = null;
		try {
			if (query.length() > 0) {
				con = DataBaseUtil.getConnection(true);
				String finalQuery = query.toString();
        if (ignoreCenter) {
          finalQuery = finalQuery.replace("#pkg_center_id#", "-1");
          finalQuery = finalQuery.replace("#pkg_center_join#", " ");
        } else {
          finalQuery = finalQuery.replace("#pkg_center_id#", "pcm.center_id");
          finalQuery = finalQuery.replace("#pkg_center_join#",
              "JOIN center_package_applicability pcm ON (pm.package_id=pcm.package_id and pcm.status='A' and (pcm.center_id=#center_id# or pcm.center_id=-1)) ");
        }
        finalQuery = finalQuery.replace("#center_id#", centerId+"");
 				finalQuery = finalQuery.replace("#tpa_id#", tpaId);
				finalQuery = finalQuery.replace("#dept_id#", deptId);
				finalQuery = finalQuery.replace("#plan_id#", planId.toString());
				if(genderApplicability != null)
					finalQuery = finalQuery.replace("#gender_applicability#", genderApplicability);
				if(visitType != null && !visitType.isEmpty())
					finalQuery = finalQuery.replace("#visit_applicability#", visitType);
				finalQuery = finalQuery.replace("#age#", age.toString());
				finalQuery = finalQuery.replace("#ageIn#", ageIn);
				ps = con.prepareStatement(finalQuery);
				if (rpDepQuery.length() > 0 && added_count > 0){
					for(int i=1; i<=added_count;i++){
						ps.setString(i, orgId);
					}
				}	
					//ps.setString(1, orgId);
				return DataBaseUtil.queryToDynaList(ps);
			} else {
				return new ArrayList();
			}
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static String getItemOrderable(BillItem item) throws SQLException {
		String orderable = "N";
		Map directBillingStatuses = ConversionUtils.listBeanToMapBean(
		    hospitalDirectBillPrefs.listAll(),"item_type");

		if (null != directBillingStatuses.get(item.type)) {
			orderable = (String)((BasicDynaBean)directBillingStatuses.get(item.type)).get("orderable");
		}
		return orderable;
	}

	public static class BillItem {
		public String type;
		public String visitType;
		public String module;
		public String orderable = "";
		public String query;
		public String operationApplicable = "";
		public String packageApplicable = "";
		public String multiVisitPackageApplicable = "";
		public String entity = "";

		public BillItem (String type, String visitType, String module, String orderable,
				String operationApplicable,String packageApplicable, String multiVisitPackageApplicable, String query, String entity) {
			this.type = type;
			this.visitType = visitType;
			this.module = module;
			this.orderable = orderable;
			this.query = query;
			this.operationApplicable = operationApplicable;
			this.packageApplicable = packageApplicable;
			this.multiVisitPackageApplicable = multiVisitPackageApplicable;
			this.entity = entity;
		}
	}

	/*
	 * Various item select queries, which can be joined in a UNION. Each individual query returns
	 * the following fields:
	 *  Rate Plan Dependent items:   type   id    name   code   department   org_id   ssg   sg
	 *  Rate Plan Independent items: type   id    name   code   department   ssg   sg
	 */
	// orderable rateplan independent items
	private static final String DOCTORS_QUERY =
		" SELECT distinct 'Doctor' as type,d.doctor_id AS id, d.doctor_name AS name, " +
		"  null as code, dept.dept_name AS department, " +
		"  s.service_sub_group_id as subGrpId, s.service_group_id as groupid,'N' as prior_auth_required, " +
		"  0 AS insurance_category_id ,false as conduction_applicable,false as conducting_doc_mandatory, " +
		"  false as results_entry_applicable,'N' as tooth_num_required, false as multi_visit_package, dcm.center_id, '-1' as tpa_id, '*' as dept_id, " +
		"  'N' as mandate_additional_info,'' as additional_info_reqts "+
		" FROM doctors d JOIN department dept USING (dept_id) " +
		"  JOIN service_sub_groups s using(service_sub_group_id) " +
		" JOIN doctor_center_master dcm ON (d.doctor_id = dcm.doctor_id)"+
		" WHERE d.status='A' AND ot_doctor_flag = 'N' AND dcm.status='A' AND (dcm.center_id = 0 OR dcm.center_id=#center_id#) ";


	private static final String OT_DOCTORS_QUERY =
		" SELECT 'Doctor' as type, d.doctor_id AS id, d.doctor_name AS name, " +
		"  null as code, dept.dept_name AS department, " +
		"  s.service_sub_group_id as subGrpId, s.service_group_id as groupid,'N' as prior_auth_required, " +
		"  0 AS insurance_category_id ,false as conduction_applicable,false as conducting_doc_mandatory, " +
		"  false as results_entry_applicable,'N' as tooth_num_required, false as multi_visit_package, dcm.center_id, '-1' as tpa_id, '*' as dept_id,  " +
		"  'N' as mandate_additional_info,'' as additional_info_reqts "+
		" FROM doctors d JOIN department dept USING (dept_id) " +
		"  JOIN service_sub_groups s using(service_sub_group_id) " +
		" JOIN doctor_center_master dcm ON (d.doctor_id = dcm.doctor_id)"+
		" WHERE d.status='A' AND ot_doctor_flag = 'Y' AND dcm.status='A' AND (dcm.center_id = 0 OR dcm.center_id=#center_id#)";


	private static final String DOCTOR_FOR_PACKAGE_QUERY =
		"SELECT 'Doctor' AS type,'Doctor' AS id,'Doctor' AS name, " +
		" null AS code, 'Package' AS department, " +
		"-1  AS subGrpId,-1 AS groupid,'N' as prior_auth_required, " +
		" 0 AS insurance_category_id, false as conduction_applicable, "+
		" false as conducting_doc_mandatory,false as results_entry_applicable,'N' as tooth_num_required, false as multi_visit_package, " +
		" -1 as center_id, '-1' as tpa_id, '*' as dept_id, 'N' as mandate_additional_info,'' as additional_info_reqts ";

	private static final String OTHER_CHARGES_QUERY =
		" SELECT 'Other Charge' AS type, charge_name as id, charge_name AS name, " +
		"  othercharge_code as code, '' AS department, " +
		"  s.service_sub_group_id as subGrpId, s.service_group_id as groupid,'N' as prior_auth_required, " +
		"  insurance_category_id,false as conduction_applicable,false as conducting_doc_mandatory,  " +
		"  false as results_entry_applicable,'N' as tooth_num_required, false as multi_visit_package, -1 as center_id, '-1' as tpa_id, '*' as dept_id, " +
		"  'N' as mandate_additional_info, '' as additional_info_reqts "+
		" FROM common_charges_master cm " +
		"  JOIN service_sub_groups s using(service_sub_group_id) " +
		" WHERE cm.status='A'  ";


	private static final String DIET_QUERY =
		" SELECT 'Meal' AS type, diet_id::text as id, meal_name AS name, " +
		"  null as code, '' AS department, " +
		"  s.service_sub_group_id as subGrpId, s.service_group_id as groupid,'N' as prior_auth_required," +
		"  insurance_category_id, false as conduction_applicable,false as conducting_doc_mandatory,   " +
		"  false as results_entry_applicable,'N' as tooth_num_required, false as multi_visit_package, -1 as center_id, '-1' as tpa_id, '*' as dept_id,  " +
		"  'N' as mandate_additional_info ,'' as additional_info_reqts"+
		" FROM diet_master dm " +
		"  JOIN service_sub_groups s using(service_sub_group_id) " +
		" WHERE dm.status='A'";

	/*
	 * Bed/ICU and Equipment charges are rateplan dependent, but their availability is not dependent on,
	 * the rateplan. Since we are only concerned about availability, we include ICU/Bed in
	 * rate plan independent items.
	 */
	private static final String EQUIPMENT_QUERY  =
		" SELECT 'Equipment' AS type, e.eq_id AS id, e.equipment_name AS name, e.equipment_code AS code, " +
		"  dept.dept_name AS department, " +
		"  s.service_sub_group_id as subGrpId, s.service_group_id as groupid,'N' as prior_auth_required, " +
		"  insurance_category_id,false as conduction_applicable,false as conducting_doc_mandatory,  " +
		"  false as results_entry_applicable,'N' as tooth_num_required, false as multi_visit_package,-1 as center_id, '-1' as tpa_id, '*' as dept_id, " +
		"  'N' as mandate_additional_info,'' as additional_info_reqts "+
		" FROM equipment_master e " +
		"  JOIN department dept USING (dept_id) " +
		"  JOIN service_sub_groups s using(service_sub_group_id)" +
		" WHERE e.status = 'A' ";

	// non-orderable rateplan independent items
	private static final String BED_TYPES_QUERY  =
		" SELECT 'Bed' AS type, bed_type_name AS id, bed_type_name AS name, null AS code, '' AS department, "+
		"  s.service_sub_group_id as subGrpId, s.service_group_id as groupid,'N' as prior_auth_required," +
		"  bt.insurance_category_id,false as conduction_applicable,false as conducting_doc_mandatory, " +
		"  false as results_entry_applicable,'N' as tooth_num_required, false as multi_visit_package, -1 as center_id, '-1' as tpa_id, '*' as dept_id,  " +
		"  'N' as mandate_additional_info, '' as additional_info_reqts "+
		" FROM bed_types bt " +
		"  JOIN chargehead_constants on ( chargehead_id = 'BBED' ) " +
		"  JOIN service_sub_groups s using(service_sub_group_id)" +
		" WHERE bt.status='A' AND is_icu = 'N'";

	private static final String ICU_BED_TYPES_QUERY =
		" SELECT 'ICU' AS type, bed_type_name AS id, bed_type_name AS name, null AS code, '' AS department, "+
		"  s.service_sub_group_id as subGrpId, s.service_group_id as groupid,'N' as prior_auth_required," +
		"  bt.insurance_category_id,false as conduction_applicable,false as conducting_doc_mandatory, " +
		"  false as results_entry_applicable,'N' as tooth_num_required,false as multi_visit_package, -1 as center_id, '-1' as tpa_id, '*' as dept_id,  " +
		"  'N' as mandate_additional_info, '' as additional_info_reqts "+
		" FROM bed_types bt " +
		"  JOIN chargehead_constants on ( chargehead_id = 'BICU' ) " +
		"  JOIN service_sub_groups s using(service_sub_group_id)" +
		" WHERE bt.status='A' AND is_icu = 'Y'";

	private static final String DIRECT_CHARGES_QUERY =
		" SELECT 'Direct Charge' AS type, chargehead_id as id, chargehead_name as name, " +
		"  null AS code, '' AS department, " +
		"  s.service_sub_group_id as subGrpId, s.service_group_id as groupid,'N' as prior_auth_required, " +
		"  insurance_category_id,false as conduction_applicable,false as conducting_doc_mandatory, " +
		"  false as results_entry_applicable,'N' as tooth_num_required,false as multi_visit_package, -1 as center_id, '-1' as tpa_id, '*' as dept_id, " +
		"  'N' as mandate_additional_info, '' as additional_info_reqts "+
		" FROM chargehead_constants " +
		"  JOIN service_sub_groups s using(service_sub_group_id)" +
		" WHERE associated_module = 'mod_billing' " ;

	private static final String DIRECT_IP_CHARGES_QUERY = DIRECT_CHARGES_QUERY + " AND ip_applicable = 'Y'";
	private static final String DIRECT_OP_CHARGES_QUERY = DIRECT_CHARGES_QUERY + " AND op_applicable = 'Y'";

	public static BillItem[] rpIndepItems = {
		/*
		 * For doctor, IP/OP disctinction exists, but only on the charge type, not on the doctor.
		 */
		new BillItem("Doctor",        "",  "mod_basic", "Y", "N","N", "Y", DOCTORS_QUERY, "Doctor"),
		new BillItem("Doctor",        "",  "mod_basic", "Y", "Y","N", "Y", OT_DOCTORS_QUERY, "Doctor"),
		new BillItem("Doctor",        "",  "mod_basic", "N", "Y","Y", "Y", DOCTOR_FOR_PACKAGE_QUERY ,"Doctor"),
		new BillItem("Other Charge",  "",  "mod_basic", "Y", "Y","Y", "Y", OTHER_CHARGES_QUERY, "Other charge"),
		new BillItem("Meal",          "i", "mod_basic", "Y", "Y","Y", "N", DIET_QUERY, "Diet"),
		new BillItem("Equipment",     "",  "mod_basic", "Y", "Y","Y", "N", EQUIPMENT_QUERY, "Equipment"),

		// we use orderable=I meaning Indirect for bed charges. From Order screen, we will not
		// get this since order screen requests orderable=Y. From billing, we will not get it
		// normally when asking for orderable="N", but superuser will get it since orderable="".
		new BillItem("Bed",           "i", "mod_adt",   "I", "N","Y", "N", BED_TYPES_QUERY, "Bed"),
		new BillItem("ICU",           "i", "mod_adt",   "I", "N","Y", "N", ICU_BED_TYPES_QUERY, "ICU"),

		new BillItem("Direct Charge", "i", "mod_basic", "N", "N","Y", "N", DIRECT_IP_CHARGES_QUERY, "Direct"),
		new BillItem("Direct Charge", "o", "mod_basic", "N", "N","Y", "N", DIRECT_OP_CHARGES_QUERY, "Direct")
	};

	// orderable rateplan dependent items
	private static final String LAB_TESTS_QUERY =
		" SELECT 'Laboratory' AS type, d.test_id AS id, d.test_name AS name, d.diag_code AS code, " +
		"  ddept.ddept_name AS department, tod.org_id AS org_id, s.service_sub_group_id as subGrpId, " +
		"  s.service_group_id as groupid,d.prior_auth_required,d.insurance_category_id,conduction_applicable," +
		" (CASE WHEN d.conducting_doc_mandatory = 'O' THEN true ELSE false END) as conducting_doc_mandatory, " +
		" results_entry_applicable,'N' as tooth_num_required,false as multi_visit_package, -1 as center_id, '-1' as tpa_id, '*' as dept_id, " +
		" d.mandate_additional_info, d.additional_info_reqts "+
		" FROM diagnostics d " +
		"  JOIN diagnostics_departments ddept USING (ddept_id) " +
		"  JOIN test_org_details tod ON (tod.org_id= ?  and tod.test_id = d.test_id AND tod.applicable) " +
		"  JOIN service_sub_groups s using(service_sub_group_id)" +
		" WHERE category='DEP_LAB' AND d.status = 'A' AND d.is_prescribable ";

	private static final String RADIOLOGY_TESTS_QUERY =
		" SELECT 'Radiology' AS type, d.test_id AS id, d.test_name AS name, d.diag_code AS code, " +
		"  ddept.ddept_name AS department, tod.org_id AS org_id, s.service_sub_group_id as subGrpId, " +
		"  s.service_group_id as groupid,d.prior_auth_required,d.insurance_category_id,conduction_applicable," +
		"  (CASE WHEN d.conducting_doc_mandatory = 'O' THEN true ELSE false END ) as  conducting_doc_mandatory, " +
		"  results_entry_applicable,'N' as tooth_num_required,false as multi_visit_package, -1 as center_id, '-1' as tpa_id, '*' as dept_id, " +
		"  d.mandate_additional_info,d.additional_info_reqts "+
		" FROM diagnostics d " +
		"  JOIN diagnostics_departments ddept USING (ddept_id) " +
		"  JOIN test_org_details tod ON (tod.org_id= ?  and  tod.test_id = d.test_id AND tod.applicable) " +
		"  JOIN service_sub_groups s using(service_sub_group_id)" +
		" WHERE category='DEP_RAD' AND d.status = 'A' AND d.is_prescribable ";

	private static final String DIAGNOSTICS_QUERY =
		" SELECT (CASE WHEN category='DEP_LAB' then 'Laboratory' else 'Radiology' END) AS type, " +
		"  d.test_id AS id, d.test_name AS name, d.diag_code AS code, "+
		"  ddept.ddept_name AS department, tod.org_id AS org_id,s.service_sub_group_id as subGrpId, " +
		"  s.service_group_id as groupid,d.prior_auth_required, d.insurance_category_id,conduction_applicable," +
		"  (CASE WHEN d.conducting_doc_mandatory = 'O' THEN true ELSE false END) as  conducting_doc_mandatory, " +
		"  results_entry_applicable,'N' as tooth_num_required,false as multi_visit_package, -1 as center_id, '-1' as tpa_id, '*' as dept_id, " +
		"  d.mandate_additional_info, d.additional_info_reqts "+
		" FROM diagnostics d " +
		"  JOIN diagnostics_departments ddept USING (ddept_id)  " +
		"  JOIN test_org_details tod ON (tod.org_id= ?  and tod.test_id = d.test_id AND tod.applicable) " +
		"  JOIN service_sub_groups s using(service_sub_group_id)" +
		" WHERE d.status = 'A' AND d.is_prescribable ";

	private static final String SERVICES_QUERY =
		" SELECT 'Service' AS type, s.service_id AS id, s.service_name AS name, s.service_code AS code, " +
		"  sd.department AS department, sod.org_id AS org_id,sg.service_sub_group_id as subGrpId, " +
		"  sg.service_group_id as groupid,s.prior_auth_required, s.insurance_category_id,conduction_applicable," +
		"  (CASE WHEN s.conducting_doc_mandatory = 'O' THEN true ELSE false END) as  conducting_doc_mandatory, " +
		"  false as results_entry_applicable,s.tooth_num_required,false as multi_visit_package, -1 as center_id, '-1' as tpa_id, '*' as dept_id, " +
		"  'N' as mandate_additional_info,'' as additional_info_reqts "+
		" FROM services s " +
		"  JOIN services_departments sd ON (s.serv_dept_id = sd.serv_dept_id) " +
		"  JOIN service_org_details sod ON (sod.org_id=  ? and s.service_id = sod.service_id AND sod.applicable) " +
		"  JOIN service_sub_groups sg using(service_sub_group_id) " +
		" WHERE s.status = 'A'";

	private static final String OPERATIONS_QUERY =
		" SELECT 'Operation' AS type, om.op_id AS id, om.operation_name AS name, om.operation_code AS code, "+
		"  d.dept_name AS department, ood.org_id AS org_id, " +
		"  s.service_sub_group_id as subGrpId, s.service_group_id as groupid,om.prior_auth_required, " +
		"  om.insurance_category_id,false as conduction_applicable,false as conducting_doc_mandatory, " +
		"  false as results_entry_applicable ,'N' as tooth_num_required,false as multi_visit_package, -1 as center_id, '-1' as tpa_id, '*' as dept_id, " +
		"  'N' as mandate_additional_info, '' as additional_info_reqts "+
		" FROM operation_master om " +
		"  JOIN department d USING (dept_id) " +
		"  JOIN operation_org_details ood ON (ood.org_id=  ? and ood.operation_id = om.op_id AND ood.applicable) " +
		"  JOIN service_sub_groups s using(service_sub_group_id) " +
		" WHERE om.status = 'A'";

	private static final String PACKAGES_QUERY =
		" SELECT 'Package' AS type, " +
		"  pm.package_id::text AS id, package_name AS name, package_code AS code, " +
		"  CASE WHEN visit_applicability = 'i' then 'IP' when visit_applicability = 'o' then 'OP' " +
		"    else 'BOTH' end AS  department, org_id, " +
		"  s.service_sub_group_id as subGrpId, s.service_group_id as groupid,pm.prior_auth_required, " +
		"  pm.insurance_category_id,false as conduction_applicable,false as conducting_doc_mandatory, " +
		"  false as results_entry_applicable,'N' as tooth_num_required, pm.multi_visit_package, #pkg_center_id#, psm.tpa_id, '*' as dept_id," +
		"  'N' as mandate_additional_info, '' as additional_info_reqts "+
		" FROM packages pm " +
		"  JOIN pack_org_details pod ON (pod.org_id=  ?  and pod.package_id = pm.package_id AND pod.applicable) " +
		"  JOIN service_sub_groups s using(service_sub_group_id) " +
		"  #pkg_center_join# " +
		"  JOIN package_sponsor_master psm ON (pm.package_id=psm.pack_id and psm.status='A' and (psm.tpa_id='#tpa_id#' OR psm.tpa_id = '-1')) " +
		"  JOIN package_plan_master ppm ON (pm.package_id=ppm.pack_id and ppm.status='A' and (ppm.plan_id=#plan_id# OR ppm.plan_id = -1)) " +
		" WHERE pm.status = 'A' AND pm.approval_status='A'";
	
	private static final String ORDER_SETS_QUERY = " SELECT (CASE WHEN p.type = 'P' THEN 'Package' ELSE 'Order Sets' END) AS type, "
			+ "  p.package_id::text AS id, package_name AS name, package_code AS code, "
			+ "  CASE WHEN visit_applicability = 'i' then 'IP' when visit_applicability = 'o' THEN 'OP' "
			+ "    ELSE 'BOTH' END AS  department, '' as org_id,  "
			+ "  s.service_sub_group_id as subGrpId, s.service_group_id as groupid,'N' as prior_auth_required, "
			+ "  -1 as insurance_category_id, false as conduction_applicable,false as conducting_doc_mandatory, "
			+ "  false AS results_entry_applicable,'N' AS tooth_num_required,false as multi_visit_package, cpa.center_id, '-1' as tpa_id, dpa.dept_id, "
			+ "  'N' AS mandate_additional_info, '' as additional_info_reqts "
			+ "  FROM packages p " + "  JOIN service_sub_groups s using(service_sub_group_id) "
			+ "  JOIN center_package_applicability cpa ON (p.package_id = cpa.package_id and (cpa.center_id=#center_id# or cpa.center_id=-1)) "
			+ "  JOIN dept_package_applicability dpa ON (p.package_id = dpa.package_id and (dpa.dept_id='#dept_id#' OR dpa.dept_id = '*')) "
			+ " WHERE p.type='O' AND p.status = 'A' AND ( valid_from <= CURRENT_DATE OR valid_from is NULL ) AND  ( valid_till >= CURRENT_DATE OR valid_till is NULL ) ";

	private static final String GENDER_APPLICABILITY_CONDITION = " AND (gender_applicability = '#gender_applicability#' or gender_applicability = '*' )";
	private static final String AGE_APPLICABILITY_CONDITION  = "AND (min_age <= #age# OR min_age IS NULL) AND (max_age >= #age# OR max_age IS NULL) AND (age_unit = '#ageIn#' OR age_unit IS NULL)";
	private static final String IP_PACKAGES_QUERY = PACKAGES_QUERY + GENDER_APPLICABILITY_CONDITION + AGE_APPLICABILITY_CONDITION + " AND (visit_applicability = '#visit_applicability#' or visit_applicability = '*') AND package_category_id not in(-2,-3) AND multi_visit_package = false";
	private static final String VISIT_APPLICABILITY_CONDITION = " AND ( p.visit_applicability = '#visit_applicability#' or p.visit_applicability = '*')";
	private static final String OP_PACKAGES_QUERY = PACKAGES_QUERY + GENDER_APPLICABILITY_CONDITION + AGE_APPLICABILITY_CONDITION + " AND (visit_applicability = '#visit_applicability#' or visit_applicability = '*') AND package_category_id not in(-2,-3)  AND multi_visit_package = false";
	private static final String DIAG_PACKAGES_QUERY = PACKAGES_QUERY + GENDER_APPLICABILITY_CONDITION + AGE_APPLICABILITY_CONDITION +  " AND (visit_applicability = '#visit_applicability#' or visit_applicability = '*') AND package_category_id in(-2,-3) AND multi_visit_package = false";
	private static final String MULTI_VISIT_PACKAGES_QUERY = PACKAGES_QUERY + GENDER_APPLICABILITY_CONDITION + AGE_APPLICABILITY_CONDITION + " AND (visit_applicability = '#visit_applicability#' or visit_applicability = '*') AND multi_visit_package = true ";

	public static BillItem[] rpDepItems = {
		new BillItem("Laboratory", "",  "mod_basic", "Y", "N","Y", "Y", LAB_TESTS_QUERY, "Laboratory"),
		new BillItem("Radiology",  "",  "mod_basic", "Y", "N","Y", "Y", RADIOLOGY_TESTS_QUERY, "Radiology"),
		new BillItem("Service",    "",  "mod_basic", "Y", "Y","Y", "Y", SERVICES_QUERY, "Service"),
		new BillItem("Operation",  "i", "mod_basic", "Y", "N","N", "N", OPERATIONS_QUERY, "Operation"),
		new BillItem("Package",    "i", "mod_basic", "Y", "N","Y", "N", IP_PACKAGES_QUERY, "Package"),
		new BillItem("Package",    "o", "mod_basic",  "Y", "N","Y","N", OP_PACKAGES_QUERY, "Package"),
		new BillItem("Order Sets", "", "mod_basic", "Y", "N","Y", "N", ORDER_SETS_QUERY, "Order sets"),
		new BillItem("MultiVisitPackage",    "o", "mod_adv_packages",  "Y", "N","N", "N", MULTI_VISIT_PACKAGES_QUERY, "multiVisit"),
		new BillItem("Laboratory", "",  "mod_basic", "Y", "N","Y", "N", DIAG_PACKAGES_QUERY, "DiagPackage"),
		new BillItem("Radiology",  "",  "mod_basic", "Y", "N","Y", "N", DIAG_PACKAGES_QUERY, "DiagPackage"),
		new BillItem("DiagPackage",  "d",  "mod_basic", "Y", "N","Y", "N", DIAGNOSTICS_QUERY, "Diagnostics")
	};

	/*
	 * Note: diag packages can be ordered from either laboratory or radiology order, despite
	 * having items from the other.
	 */

	public static final String GET_MASTERS_COUNTS =
		"SELECT * FROM masters_deprtmentwise_counts where type=? and dept_id = ? ";

	public BasicDynaBean getMastersCounts(String type,String depId) throws SQLException {
		return DataBaseUtil.queryToDynaBean(GET_MASTERS_COUNTS, new Object[]{type, depId});
	}
}
