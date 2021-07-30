package com.insta.hms.mdm.packages;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PackagesRepository extends MasterRepository<Integer> {

  public PackagesRepository() {
    super("packages", "package_id", "package_name");
  }

  private static final String PACKAGE_DETAILS = " SELECT pm.*, pc.org_id, pc.bed_type, pc.charge, "
      + "  pc.discount, pod.item_code as ct_code, pm.service_sub_group_id, pod.applicable,"
      + "  pod.code_type, pm.billing_group_id, pm.submission_batch_type"
      + "  FROM packages pm JOIN package_charges pc ON (pc.package_id = pm.package_id) "
      + "  JOIN pack_org_details pod ON (pod.package_id = pm.package_id "
      + "  AND pod.org_id = pc.org_id) "
      + "  WHERE pm.package_id=? AND pc.org_id=? AND pc.bed_type=?";

  public BasicDynaBean getPackageDetails(int packageId, String orgId, String bedType) {
    return DatabaseHelper.queryToDynaBean(PACKAGE_DETAILS,
        new Object[] { packageId, orgId, bedType });
  }

  private static final String ALL_BED_TYPE_PACKAGE_CHARGES =
      " SELECT org_id, bed_type, charge,"
          + " discount , package_charges_id FROM package_charges WHERE package_id=? AND org_id=?";

  public List<BasicDynaBean> getAllBedTypePackageDetails(int packageId, String orgId) {
    return DatabaseHelper.queryToDynaList(ALL_BED_TYPE_PACKAGE_CHARGES,
        new Object[] {packageId, orgId});
  }

  private static final String GET_PACKAGE_COMPONENTS =
      " SELECT pm.type, activity_id, "
      + " activity_type, activity_type AS item_type, '' as dept_id, '' as doctor_id, "
      + " CASE WHEN activity_type IN ('Operation', 'Bed', 'ICU') OR pc.panel_id is not null "
      + " THEN coalesce(pc.content_id_ref, pc.package_content_id) ELSE NULL END as content_id_ref, "
      + " CASE WHEN LOWER(activity_type) = 'doctor' "
      + " THEN pc.consultation_type_id::character varying "
      + "    ELSE activity_id END AS pack_item_id, "
      + "  pm.package_id, (CASE WHEN pc.activity_type = 'Operation' then pc.activity_id"
      + "  else '' end) as operation_id, operation_name, "
      + "  CASE WHEN pc.panel_id is not null THEN "
      + "  concat(test.test_name, ': ', pap.package_name) "
      + "  WHEN pc.activity_type='Bed' OR pc.activity_type='ICU' THEN"
      + "  concat(oi.item_name, ': ', cc.chargehead_name)"
      + "  ELSE coalesce(test.test_name, s.service_name, "
      + "  om.operation_name, sid.medicine_name, oi.item_name) END as activity_description, "
      + "  pc.charge_head, chargehead_name, pc.activity_qty, activity_qty_uom as activity_units, "
      + "  pm.description,pm.service_sub_group_id, pm.submission_batch_type, "
      + "  pc.consultation_type_id,ct.consultation_type,ct.duration "
      + "  as consultation_type_duration,pcc.charge as activity_charge, pm.insurance_category_id,"
      + "  pm.package_category_id,pc.package_content_id as pack_ob_id ,"
      + "  pac.discount as package_discount,pac.charge as package_charge, "
      + "  pm.multi_visit_package,pm.package_name, "
      + "  (case when coalesce(test.conducting_doc_mandatory, s.conducting_doc_mandatory, 'N')"
      + "  = 'O' then true else false end) as conducting_doc_mandatory, pc.display_order, "
      + "  coalesce(test.test_name, s.service_name, operation_name, "
      + "  sid.medicine_name, oi.item_name, '') as item_name,s.service_duration, "
      + "  s.serv_dept_id,'' as parent_pack_ob_id,pc.activity_remarks as remarks, "
      + "  coalesce(test.mandate_additional_info, 'N') as mandate_additional_info,"
      + "  coalesce(test.additional_info_reqts, '') as additional_info_reqts, pm.billing_group_id, "
      + "  pc.bed_id, pc.panel_id, coalesce(tod.item_code, sod.item_code, ood.item_code, "
      + "  cod.item_code, '') "
      + "  as ct_code, coalesce(tod.code_type, sod.code_type, ood.code_type, cod.code_type, '') "
      + "  as code_type,coalesce(test.allow_zero_claim_amount, s.allow_zero_claim_amount, "
      + "  om.allow_zero_claim_amount, ct.allow_zero_claim_amount, '') "
      + "  as allow_zero_claim_amount,"
      + "  coalesce(CASE WHEN pc.charge_head='SACOPE' THEN om.dept_id ELSE NULL END ,"
      + "  test.ddept_id,s.serv_dept_id::text,e.dept_id,doc.dept_id) AS act_department_id " 
      + "  FROM packages pm "
      + "  JOIN package_charges pac on pac.package_id = pm.package_id"
      + "  AND pac.org_id=(:orgId) AND pac.bed_type=(:bedType) "
      + "  LEFT JOIN package_contents pc ON pm.package_id = pc.package_id "
      + "  LEFT JOIN packages pap ON (pap.package_id=pc.panel_id) "
      + "  JOIN package_content_charges pcc ON pcc.package_content_id = pc.package_content_id "
      + "  AND pcc.org_id=(:orgId) AND pcc.bed_type=(:bedType)"
      + "  LEFT JOIN chargehead_constants cc ON cc.chargehead_id = pc.charge_head "
      + "  LEFT JOIN operation_master om ON (om.op_id = pc.activity_id AND pc.activity_type ="
      + " 'Operation')"
      + " LEFT JOIN operation_org_details ood ON(ood.operation_id=om.op_id "
      + " AND pcc.org_id=ood.org_id) LEFT JOIN store_item_details sid ON ("
      + " sid.medicine_id::character varying = pc.activity_id "
      + "  AND pc.activity_type ='Inventory') LEFT JOIN orderable_item oi ON (oi.entity_id"
      + "  =pc.activity_id AND oi.entity=pc.activity_type) LEFT JOIN diagnostics"
      + "  test ON (test.test_id=pc.activity_id) "
      + " LEFT JOIN test_org_details tod ON (test.test_id=tod.test_id and pcc.org_id = tod.org_id)"
      + "  LEFT JOIN services s ON (pc.activity_id=s.service_id) "
      + " LEFT JOIN service_org_details sod ON (sod.service_id = s.service_id AND "
      + "  pcc.org_id=sod.org_id) LEFT JOIN consultation_types ct "
      + "  ON(ct.consultation_type_id=pc.consultation_type_id)"
      + "  LEFT JOIN consultation_org_details cod"
      + "  ON (cod.consultation_type_id=ct.consultation_type_id AND pcc.org_id=cod.org_id)"
      + "  LEFT JOIN equipment_master e ON (e.eq_id = pc.activity_id) "
      + "  LEFT JOIN doctors doc ON (doc.doctor_id = pc.activity_id)  AND pc.activity_type='Doctor'"
      + "  WHERE pm.package_id IN "
      + " (:packageIds) AND pm.status = 'A' "
      + " UNION ALL "
      + " SELECT type, activity_id, activity_type, activity_type AS item_type, pc.dept_id, "
      + " pc.doctor_id, CASE WHEN activity_type IN ('Operation', 'Bed', 'ICU') "
      + " OR pc.panel_id is not null "
      + " THEN coalesce(pc.content_id_ref, pc.package_content_id) ELSE NULL END as content_id_ref, "
      + "  '' as pack_item_id, p.package_id, '' as operation_id, '' as operation_name, "
      + " coalesce(test.test_name, s.service_name, doc.doctor_name, d.dept_name, "
      + " em.equipment_name, 'Doctor') as activity_description, pc.charge_head,"
      + " cc.chargehead_name, pc.activity_qty, activity_qty_uom as activity_units, p.description,"
      + " p.service_sub_group_id, p.submission_batch_type, "
      + " pc.consultation_type_id,ct.consultation_type,ct.duration "
      + " as consultation_type_duration,0 as activity_charge, "
      + " -1 as insurance_category_id, -1 as package_category_id, "
      + " pc.package_content_id as pack_ob_id, 0 as package_discount, "
      + " 0 as package_charge, false as multi_visit_package, p.package_name, "
      + " (case when coalesce(test.conducting_doc_mandatory, s.conducting_doc_mandatory,'N') = 'O'"
      + "    then true else false end) as conducting_doc_mandatory, pc.display_order,"
      + " coalesce(test.test_name, s.service_name, doc.doctor_name, d.dept_name, "
      + " em.equipment_name, '') as item_name,s.service_duration,s.serv_dept_id,"
      + " pc.parent_pack_ob_id::character varying,pc.activity_remarks as remarks, "
      + " coalesce(test.mandate_additional_info, 'N')"
      + " as mandate_additional_info, coalesce(test.additional_info_reqts, '') "
      + " as additional_info_reqts, NULL as billing_group_id, "
      + " pc.bed_id, pc.panel_id, '' as ct_code , '' as code_type,"
      + " '' AS allow_zero_claim_amount,'' AS act_department_id FROM packages p "
      + " LEFT JOIN package_contents pc ON p.package_id = pc.package_id "
      + " LEFT JOIN doctors doc ON (pc.activity_id = doc.doctor_id AND pc.activity_type ="
      + " 'Doctor') LEFT JOIN services s ON (pc.activity_id=s.service_id AND pc.activity_type ="
      + " 'Service') LEFT JOIN diagnostics test ON (test.test_id=pc.activity_id AND"
      + "     (pc.activity_type = 'Laboratory' OR pc.activity_type = 'Radiology')) "
      + " LEFT JOIN equipment_master em ON (em.eq_id = pc.activity_id AND "
      + "     pc.activity_type = 'Equipment')"
      + " LEFT JOIN department d ON (pc.dept_id = d.dept_id AND pc.activity_type = 'Department')"
      + " LEFT JOIN chargehead_constants cc ON cc.chargehead_id = pc.charge_head "
      + " LEFT JOIN consultation_types ct ON(ct.consultation_type_id=pc.consultation_type_id) "
      + " WHERE p.package_id IN (:packageIds) AND p.status = 'A' and p.type='O' "
      + " ORDER BY display_order, pack_ob_id";

  private static final String GET_PATIENT_PACKAGE_COMPONENTS =
      " SELECT pc.patient_package_id, pc.patient_package_content_id, pm.type, pc.activity_id, "
      + " pc.activity_type, pc.activity_type AS item_type, '' as dept_id, '' as doctor_id, "
      + " CASE WHEN pc.activity_type IN ('Operation', 'Bed', 'ICU') OR pc.panel_id is not null "
      + " THEN coalesce(pc.content_id_ref, pc.patient_package_content_id) ELSE NULL"
      + " END as content_id_ref, "
      + " CASE WHEN LOWER(pc.activity_type) = 'doctor' "
      + " THEN pc.consultation_type_id::character varying "
      + "    ELSE pc.activity_id END AS pack_item_id, "
      + "  pm.package_id, (CASE WHEN pc.activity_type = 'Operation' then pc.activity_id"
      + "  else '' end) as operation_id, operation_name, "
      + "  CASE WHEN pc.panel_id is not null THEN "
      + "  concat(test.test_name, ': ', pap.package_name) "
      + "  WHEN pc.activity_type='Bed' OR pc.activity_type='ICU' THEN"
      + "  concat(oi.item_name, ': ', cc.chargehead_name)"
      + "  ELSE coalesce(test.test_name, s.service_name, "
      + "  om.operation_name, sid.medicine_name, oi.item_name) END as activity_description, "
      + "  pc.charge_head, chargehead_name, pc.activity_qty, pc.activity_units, "
      + "  pm.description,pm.service_sub_group_id, pm.submission_batch_type, "
      + "  pc.consultation_type_id,ct.consultation_type,ct.duration "
      + "  as consultation_type_duration,pcc.charge as activity_charge, pm.insurance_category_id,"
      + "  pm.package_category_id,pc.package_content_id as pack_ob_id ,"
      + "  pac.discount as package_discount,pac.amount as package_charge, "
      + "  pm.multi_visit_package,pac.package_name, "
      + "  (case when coalesce(test.conducting_doc_mandatory, s.conducting_doc_mandatory, 'N')"
      + "  = 'O' then true else false end) as conducting_doc_mandatory, pc.display_order, "
      + "  coalesce(test.test_name, s.service_name, operation_name, "
      + "  sid.medicine_name, oi.item_name, '') as item_name,s.service_duration, "
      + "  s.serv_dept_id,'' as parent_pack_ob_id,pc.activity_remarks as remarks, "
      + "  coalesce(test.mandate_additional_info, 'N') as mandate_additional_info,"
      + "  coalesce(test.additional_info_reqts, '') as additional_info_reqts, pm.billing_group_id, "
      + "  pc.bed_id, pc.panel_id,false AS is_old_mvp FROM packages pm "
      + " JOIN patient_packages pp On pm.package_id = pp.package_id"
      + "  JOIN patient_customised_package_details pac "
      + " ON pac.patient_package_id = pp.pat_package_id"
      + "  JOIN patient_package_contents pc ON pp.pat_package_id = pc.patient_package_id "
      + "  LEFT JOIN package_contents pmc ON pmc.package_content_id = pc.package_content_id "
      + "  LEFT JOIN packages pap ON (pap.package_id=pc.panel_id) "
      + "  JOIN patient_package_content_charges pcc"
      + " ON pcc.patient_package_content_id = pc.patient_package_content_id "
      + "  LEFT JOIN chargehead_constants cc ON cc.chargehead_id = pc.charge_head "
      + "  LEFT JOIN operation_master om ON (om.op_id = pc.activity_id AND pc.activity_type ="
      + " 'Operation') LEFT JOIN store_item_details sid ON ("
      + " sid.medicine_id::character varying = pc.activity_id "
      + "  AND pc.activity_type ='Inventory') LEFT JOIN orderable_item oi ON (oi.entity_id"
      + "  =pc.activity_id AND oi.entity=pc.activity_type) LEFT JOIN diagnostics"
      + "  test ON (test.test_id=pc.activity_id) "
      + "  LEFT JOIN services s ON (pc.activity_id=s.service_id) "
      + "  LEFT JOIN consultation_types ct ON(ct.consultation_type_id=pc.consultation_type_id) "
      + "  WHERE pm.package_id IN (:packageIds) AND pm.status = 'A' AND pp.mr_no=(:mrNo) "
      + "  AND pp.status NOT IN ('X','C') ORDER BY "
      + "  patient_package_id, CASE WHEN pac.is_customized_package THEN pc.display_order "
      + "  ELSE pmc.display_order END, patient_package_content_id";
  
  
  private static final String GET_PAT_PACKAGE_COMPONENT =
         " SELECT pp.pat_package_id AS patient_package_id, "
         + " pmc.package_content_id AS patient_package_content_id,pm.type, pmc.activity_id, "
         + " pmc.activity_type, pmc.activity_type AS item_type, '' as dept_id, '' as doctor_id, "
         + " CASE WHEN pmc.activity_type IN ('Operation', 'Bed', 'ICU') "
         + " OR pmc.panel_id is not null "
         + " THEN pmc.content_id_ref ELSE NULL"
         + " END as content_id_ref, "
         + " CASE WHEN LOWER(pmc.activity_type) = 'doctor' "
         + " THEN pmc.consultation_type_id::character varying "
         + "    ELSE pmc.activity_id END AS pack_item_id, "
         + "  pm.package_id, (CASE WHEN pmc.activity_type = 'Operation' then pmc.activity_id"
         + "  else '' end) as operation_id, operation_name, "
         + "  CASE WHEN pmc.panel_id is not null THEN "
         + "  concat(test.test_name, ': ', pap.package_name) "
         + "  WHEN pmc.activity_type='Bed' OR pmc.activity_type='ICU' THEN"
         + "  concat(oi.item_name, ': ', cc.chargehead_name)"
         + "  ELSE coalesce(test.test_name, s.service_name, "
         + "  om.operation_name, sid.medicine_name, oi.item_name) END as activity_description, "
         + "  pmc.charge_head, chargehead_name, pmc.activity_qty, "
         + " pmc.activity_qty_uom AS activity_units, "
         + "  pm.description,pm.service_sub_group_id, pm.submission_batch_type, "
         + "  pmc.consultation_type_id,ct.consultation_type,ct.duration "
         + "  as consultation_type_duration, 0 AS activity_charge, pm.insurance_category_id,"
         + "  pm.package_category_id,pmc.package_content_id as pack_ob_id ,"
         + " 0 AS package_discount,0 AS package_charge, "
         + "  pm.multi_visit_package,pm.package_name, "
         + "  (case when coalesce(test.conducting_doc_mandatory, s.conducting_doc_mandatory, 'N')"
         + "  = 'O' then true else false end) as conducting_doc_mandatory, pmc.display_order, "
         + "  coalesce(test.test_name, s.service_name, operation_name, "
         + "  sid.medicine_name, oi.item_name, '') as item_name,s.service_duration, "
         + "  s.serv_dept_id,'' as parent_pack_ob_id,pmc.activity_remarks as remarks, "
         + "  coalesce(test.mandate_additional_info, 'N') as mandate_additional_info,"
         + "  coalesce(test.additional_info_reqts, '') as additional_info_reqts, "
         + "  pm.billing_group_id, "
         + "  pmc.bed_id, pmc.panel_id,true AS is_old_mvp FROM packages pm "
         + " JOIN patient_packages pp On pm.package_id = pp.package_id"
         + "  JOIN package_contents pmc ON pmc.package_id = pm.package_id "
         + "  LEFT JOIN packages pap ON (pap.package_id=pmc.panel_id) "
         + "  LEFT JOIN chargehead_constants cc ON cc.chargehead_id = pmc.charge_head "
         + "  LEFT JOIN operation_master om ON (om.op_id = pmc.activity_id AND pmc.activity_type ="
         + " 'Operation') LEFT JOIN store_item_details sid ON ("
         + " sid.medicine_id::character varying = pmc.activity_id "
         + "  AND pmc.activity_type ='Inventory') LEFT JOIN orderable_item oi ON (oi.entity_id"
         + "  =pmc.activity_id AND oi.entity=pmc.activity_type) LEFT JOIN diagnostics"
         + "  test ON (test.test_id=pmc.activity_id) "
         + "  LEFT JOIN services s ON (pmc.activity_id=s.service_id) "
         + "  LEFT JOIN consultation_types ct ON(ct.consultation_type_id=pmc.consultation_type_id) "
         + "  WHERE pm.package_id IN (:packageIds) AND pm.status = 'A' AND pp.mr_no=(:mrNo) "
         + "  AND pp.status NOT IN ('X','C') ORDER BY "
         + "  patient_package_id, pmc.display_order";

  /**
   * Get Package component details.
   * @param packageIdList package ids
   * @param orgId orgId
   * @param bedType bedType
   * @return List of BasicDynaBean
   */
  public List<BasicDynaBean> getPackageComponents(List<Integer> packageIdList, String orgId,
                                                  String bedType) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("orgId", orgId);
    parameters.addValue("bedType", bedType);
    parameters.addValue("packageIds", packageIdList);
    return DatabaseHelper.queryToDynaList(GET_PACKAGE_COMPONENTS, parameters);
  }

  /**
   * Get Patient Package component details.
   * * @param mrNo     MR no
   * @param packageIdList package ids
   * @return List of BasicDynaBean
   */
  public List<BasicDynaBean> getPatientPackagesComponents(String mrNo,
      List<Integer> packageIdList) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("mrNo", mrNo);
    parameters.addValue("packageIds", packageIdList);
    List<BasicDynaBean> packagePackagesComponents = DatabaseHelper
        .queryToDynaList(GET_PATIENT_PACKAGE_COMPONENTS, parameters);
    /* Get the MVP Details of patients which have multi visit package 
     * partially consumed in 12.3 and upgraded to 12.4
     * Fall back to old method ,since migration has not done into 
     * new package transaction tables for mvp.
    */
    if (packagePackagesComponents.isEmpty() || packagePackagesComponents == null) {
      packagePackagesComponents =  DatabaseHelper
          .queryToDynaList(GET_PAT_PACKAGE_COMPONENT, parameters);
    }
    return packagePackagesComponents;
  }
  
  //To Be removed after 12.4- packages4.0 
  private static final String GET_PACKAGE_COMPONENTS_NEW = " SELECT type, activity_id, "
      + " activity_type AS item_type, '' as dept_id, '' as doctor_id,"
      + " CASE WHEN LOWER(activity_type) = 'doctor' "
      + " THEN pc.consultation_type_id::character varying "
      + "    ELSE activity_id END AS pack_item_id, "
      + "  pm.package_id, operation_id, activity_description, operation_name, "
      + "  pc.charge_head, chargehead_name, activity_qty, activity_units, "
      + "  pm.description,pm.service_sub_group_id, '' as submission_batch_type,"
      + "  pc.consultation_type_id,ct.consultation_type,ct.duration "
      + "  as consultation_type_duration,activity_charge, pm.insurance_category_id,"
      + "  pm.package_category_id,"
      + "  pack_ob_id,pm.multi_visit_package,pm.package_name, "
      + "  (case when coalesce(test.conducting_doc_mandatory, s.conducting_doc_mandatory, 'N')"
      + "  = 'O' then true else false end) as conducting_doc_mandatory, pc.display_order, "
      + "  coalesce(test.test_name, s.service_name, operation_name, '') "
      + "  as item_name,s.service_duration,s.serv_dept_id,'' "
      + "  as parent_pack_ob_id,pc.activity_remarks as remarks, "
      + "  coalesce(test.mandate_additional_info, 'N') as mandate_additional_info,"
      + "  coalesce(test.additional_info_reqts, '') as additional_info_reqts, pm.billing_group_id "
      + "  FROM pack_master pm "
      + "  LEFT JOIN package_componentdetail pc ON pm.package_id = pc.package_id "
      + "  LEFT JOIN chargehead_constants cc ON cc.chargehead_id = pc.charge_head "
      + "  LEFT JOIN operation_master ON op_id = operation_id "
      + "  LEFT JOIN diagnostics test ON (test.test_id=pc.activity_id) "
      + "  LEFT JOIN services s ON (pc.activity_id=s.service_id) "
      + "  LEFT JOIN consultation_types ct ON(ct.consultation_type_id=pc.consultation_type_id) "
      + "  WHERE pm.package_id = ? "
      + "  AND pm.package_active = 'A'  and pc.charge_head !='PKGPKG'"
      + " UNION ALL "
      + " SELECT pm.type, pcc.activity_id, "
      + " pcc.activity_type AS item_type, '' as dept_id, '' as doctor_id,"
      + " CASE WHEN LOWER(pcc.activity_type) = 'doctor' "
      + " THEN pc.consultation_type_id::character varying "
      + "    ELSE pcc.activity_id END AS pack_item_id, "
      + "  pm.package_id, pm.operation_id, pcc.activity_description, operation_name, "
      + "  pcc.charge_head, chargehead_name, pcc.activity_qty, pcc.activity_units, "
      + "  pm.description,pm.service_sub_group_id, '' as submission_batch_type,"
      + "  pcc.consultation_type_id,ct.consultation_type,ct.duration "
      + "  as consultation_type_duration,pcc.activity_charge, pm.insurance_category_id,"
      + "  pm.package_category_id,"
      + "  pcc.pack_ob_id,pm.multi_visit_package,pm.package_name, "
      + "  (case when coalesce(test.conducting_doc_mandatory, s.conducting_doc_mandatory, 'N')"
      + "  = 'O' then true else false end) as conducting_doc_mandatory, pcc.display_order, "
      + "  coalesce(test.test_name, s.service_name, operation_name, '') "
      + "  as item_name,s.service_duration,s.serv_dept_id,'' "
      + "  as parent_pack_ob_id,pcc.activity_remarks as remarks, "
      + "  coalesce(test.mandate_additional_info, 'N') as mandate_additional_info,"
      + "  coalesce(test.additional_info_reqts, '') as additional_info_reqts, pm.billing_group_id "
      + "  FROM pack_master pm "
      + "  LEFT JOIN package_componentdetail pc ON pm.package_id = pc.package_id "
      + "  LEFT JOIN package_componentdetail pcc ON pc.activity_id = pcc.package_id::text "
      + "  LEFT JOIN pack_master pm1 ON pc.activity_id = pm1.package_id::text "
      + "  LEFT JOIN chargehead_constants cc ON cc.chargehead_id = pc.charge_head "
      + "  LEFT JOIN operation_master ON op_id = pm.operation_id "
      + "  LEFT JOIN diagnostics test ON (test.test_id=pc.activity_id) "
      + "  LEFT JOIN services s ON (pc.activity_id=s.service_id) "
      + "  LEFT JOIN consultation_types ct ON(ct.consultation_type_id=pc.consultation_type_id) "
      + "  WHERE pm.package_id = ? "
      +    "AND pm.package_active = 'A' and pc.charge_head ='PKGPKG' and pm1.package_type ='d'"
      + " UNION ALL "
      + " SELECT type, activity_id, activity_type AS item_type, pc.dept_id, pc.doctor_id,"
      + "  '' as pack_item_id, p.package_id, '' as operation_id, coalesce(test.test_name, "
      + " s.service_name, doc.doctor_name, d.dept_name, em.equipment_name, 'Doctor') "
      + " as activity_description, '' as operation_name, pc.charge_head, cc.chargehead_name,"
      + " activity_qty, activity_qty_uom as activity_units, p.description, p.service_sub_group_id,"
      + " p.submission_batch_type, pc.consultation_type_id,ct.consultation_type,ct.duration "
      + " as consultation_type_duration,0 as activity_charge, "
      + " -1 as insurance_category_id, -1 as package_category_id, "
      + " package_content_id::character varying as pack_ob_id, false "
      + " as multi_visit_package, p.package_name, "
      + " (case when coalesce(test.conducting_doc_mandatory, s.conducting_doc_mandatory,'N') = 'O'"
      + "    then true else false end) as conducting_doc_mandatory, pc.display_order,"
      + " coalesce(test.test_name, s.service_name, doc.doctor_name, d.dept_name, "
      + " em.equipment_name, '') as item_name,s.service_duration,s.serv_dept_id,"
      + " pc.parent_pack_ob_id::character varying,pc.activity_remarks as remarks, "
      + " coalesce(test.mandate_additional_info, 'N')"
      + " as mandate_additional_info, coalesce(test.additional_info_reqts, '') "
      + " as additional_info_reqts, NULL as billing_group_id FROM packages p "
      + " LEFT JOIN package_contents pc ON p.package_id = pc.package_id "
      + " LEFT JOIN doctors doc ON (pc.doctor_id = doc.doctor_id AND pc.activity_type = 'Doctor') "
      + " LEFT JOIN services s ON (pc.activity_id=s.service_id AND pc.activity_type = 'Service') "
      + " LEFT JOIN diagnostics test ON (test.test_id=pc.activity_id AND"
      + "     (pc.activity_type = 'Laboratory' OR pc.activity_type = 'Radiology')) "
      + " LEFT JOIN equipment_master em ON (em.eq_id = pc.activity_id AND "
      + "     pc.activity_type = 'Equipment')"
      + " LEFT JOIN department d ON (pc.dept_id = d.dept_id AND pc.activity_type = 'Department')"
      + " LEFT JOIN chargehead_constants cc ON cc.chargehead_id = pc.charge_head "
      + " LEFT JOIN consultation_types ct ON(ct.consultation_type_id=pc.consultation_type_id) "
      + " WHERE p.package_id = ? AND p.status = 'A' ORDER BY display_order";
  
  //To Be removed after 12.4- packages4.0 
  public List<BasicDynaBean> getPackageComponentsNew(int packageId) {
    return DatabaseHelper.queryToDynaList(GET_PACKAGE_COMPONENTS_NEW,
        new Object[] { packageId, packageId, packageId });
  }

  private static final String GET_PACKAGE_ITEM_SUB_GROUP_TAX_DETAILS = "SELECT "
      + " isg.item_subgroup_id, isg.subgroup_code, isg.item_subgroup_name, ig.group_code "
      + " FROM package_item_sub_groups pisg "
      + " JOIN item_sub_groups isg ON(pisg.item_subgroup_id = isg.item_subgroup_id) "
      + " JOIN item_groups ig ON (ig.item_group_id = isg.item_group_id) "
      + " WHERE pisg.package_id = ? ";

  public List<BasicDynaBean> getPackageItemSubGroupTaxDetails(String actDescriptionId) {
    return DatabaseHelper.queryToDynaList(GET_PACKAGE_ITEM_SUB_GROUP_TAX_DETAILS,
        new Object[] { Integer.parseInt(actDescriptionId) });
  }

  private static final String PACKAGE_DETAILS_QUERY = "SELECT * FROM "
      + "(SELECT CASE WHEN pc.panel_id is not null THEN "
      + "  concat(test.test_name, ': ', pap.package_name) "
      + "  ELSE coalesce(test.test_name, s.service_name, "
      + "  om.operation_name, sid.medicine_name, oi.item_name) "
      + " END as activity_description, ct.consultation_type, "
      + " pc.activity_id, pc.activity_qty, pc.activity_type, pm.package_id, pc.display_order,"
      + " pc.package_content_id,pc.activity_type AS item_type,pc.charge_head, "
      + " pm.submission_batch_type"
      + " FROM packages pm "
      + " left join package_contents pc on  (pm.package_id = pc.package_id) "
      + "  LEFT JOIN packages pap ON (pap.package_id=pc.panel_id) "
      + "  LEFT JOIN operation_master om ON (om.op_id = pc.activity_id AND pc.activity_type ="
      + " 'Operation') LEFT JOIN store_item_details sid ON ("
      + " sid.medicine_id::character varying = pc.activity_id "
      + "  AND pc.activity_type ='Inventory') LEFT JOIN orderable_item oi ON (oi.entity_id"
      + "  =pc.activity_id AND oi.entity=pc.activity_type) LEFT JOIN diagnostics"
      + "  test ON (test.test_id=pc.activity_id) "
      + "  LEFT JOIN services s ON (pc.activity_id=s.service_id) "
      + " left join consultation_types ct on  (ct.consultation_type_id = pc.consultation_type_id)"
      + " UNION ALL"
      + " SELECT om.operation_name AS activity_description, '' as consultation_type,"
      + " om.op_id::character varying AS activity_id, "
      + " '1' AS activity_qty, 'Operation' AS activity_type, "
      + " pm.package_id, 0 as display_order, "
      + " pc.package_content_id,pc.activity_type AS item_type,pc.charge_head, "
      + " pm.submission_batch_type "
      + " FROM packages pm"
      + " left join package_contents pc on  (pm.package_id = pc.package_id) "
      + " left join operation_master om ON (om.op_id = pc.operation_id)"
      + " WHERE (pc.operation_id is not null AND pc.operation_id != '') ) AS foo";

  /**
   * Get Package component details.
   * @param packageId package id
   * @return List of BasicDynaBean
   */
  public List<BasicDynaBean> getPackgeComponentDetails(int packageId) {
    return DatabaseHelper.queryToDynaList(
        PACKAGE_DETAILS_QUERY + " WHERE package_id = ? ORDER BY package_content_id ",
        new Object[] { packageId });
  }
}
