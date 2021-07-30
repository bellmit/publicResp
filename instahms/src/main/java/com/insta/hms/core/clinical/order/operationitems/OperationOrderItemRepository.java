package com.insta.hms.core.clinical.order.operationitems;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.core.clinical.order.master.OrderItemRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Repository
public class OperationOrderItemRepository extends OrderItemRepository {

  private static final String GENERATE_NEXT_SEQUENCE_QUERY = " SELECT nextval(?)";

  public OperationOrderItemRepository() {
    super("bed_operation_schedule");
  }

  @Override
  public Integer getNextSequence() {
    return DatabaseHelper.getInteger(GENERATE_NEXT_SEQUENCE_QUERY, "ip_operation_sequence");
  }

  private static String GET_COMPLETED_OPERATIONS = "SELECT doc.doctor_name AS primarysurgeon,"
      + " doctor.doctor_name AS primaryanaesthetist, tm.theatre_name as theatre, "
      + " bps.operation_name as opid, to_char(start_datetime,'hh24:mi') as starttime, "
      + " to_char(bps.start_datetime,'DD-MM-YYYY') as operation_date,"
      + " to_char(bps.end_datetime,'DD-MM-YYYY') as operation_end_date, om.operation_name as name,"
      + " bps.prescribed_id, bps.status, od.doc_id, bps.remarks, " 
      + " to_char(end_datetime,'hh24:mi') as endtime, "
      + " (CASE WHEN dat.doc_format IS NULL THEN 'doc_fileupload' ELSE dat.doc_format END) "
      + " AS doc_format, (case when bps.status = 'X' then 'checked' else '' end) as check,"
      + " (case when bps.status = 'X' then 'disabled' when bps.status ='C' "
      + " then 'disabled' end) as dis"
      + " FROM operation_master om, bed_operation_schedule bps"
      + " LEFT JOIN  theatre_master tm ON theatre_id= bps.theatre_name"
      + " LEFT OUTER JOIN doctors doc ON doc.doctor_id=bps.surgeon"
      + " LEFT OUTER JOIN doctors doctor ON doctor.doctor_id=bps.anaesthetist"
      + " LEFT OUTER JOIN operation_documents od ON (bps.prescribed_id=od.prescription_id)"
      + " LEFT OUTER JOIN patient_documents pd ON (od.doc_id=pd.doc_id)"
      + " LEFT OUTER JOIN doc_all_templates_view dat ON (pd.template_id=dat.template_id "
      + " AND pd.doc_format=dat.doc_format)"
      + " WHERE  om.op_id = bps.operation_name AND bps.status ='C' AND patient_id = ?"
      + " ORDER BY bps.operation_name";

  public List<BasicDynaBean> getCompletedOperation(String patientId) {

    return DatabaseHelper.queryToDynaList(GET_COMPLETED_OPERATIONS, patientId);
  }

  private static final String OPERATIONS_QUERY = " SELECT 'Operation' AS type, om.op_id AS id, "
      + "  om.operation_name AS name, om.operation_code AS code, "
      + "  d.dept_name AS department,"
      + "  s.service_sub_group_id as subGrpId, s.service_group_id as groupid, "
      + "  om.prior_auth_required, om.insurance_category_id,false as conduction_applicable, "
      + "  false as conducting_doc_mandatory, "
      + "  false as results_entry_applicable ,'N' as tooth_num_required, "
      + "  false as multi_visit_package, -1 as center_id, '-1' as tpa_id, "
      + "  'N' as mandate_additional_info, '' as additional_info_reqts "
      + " FROM operation_master om " + "  JOIN department d USING (dept_id) "
      + "  JOIN service_sub_groups s using(service_sub_group_id) ";

  public List<BasicDynaBean> getItemDetails(List<Object> entityIdList) {
    return super.getItemDetails(OPERATIONS_QUERY, entityIdList, "om.op_id", null);
  }

  private static final String GET_OPERATION_ANAESTHESIA_DETAILS =
      "SELECT bc.charge_id, bc.amount, "
      + " bc.tax_amt, bc.sponsor_tax_amt, b.is_tpa, "
      + " bc.insurance_claim_amount, bc.discount ,sud.*, atm.* FROM surgery_anesthesia_details sud "
      + " JOIN bill_charge bc USING (surgery_anesthesia_details_id)"
      + " JOIN bill b ON (bc.bill_no = b.bill_no) "
      + " JOIN anesthesia_type_master atm ON(atm.anesthesia_type_id=sud.anesthesia_type)"
      + " WHERE prescribed_id = ? order by sud.surgery_anesthesia_details_id";

  private static final String GET_ADDITIONAL_THEATER_DETAILS = "SELECT bc.charge_id, bc.amount, "
      + " bc.tax_amt, bc.sponsor_tax_amt, b.is_tpa, "
      + " bc.insurance_claim_amount, bc.discount FROM bill_charge bc "
      + " JOIN bill_activity_charge bac ON (bac.charge_id = bc.charge_ref AND "
      + " bac.activity_code='OPE') "
      + " JOIN bill b ON (bc.bill_no = b.bill_no) "
      + " WHERE bc.charge_head = 'TCAOPE' AND  bac.activity_id= ?";

  public List<BasicDynaBean> getOperationAnaesthesiaDetails(Integer prescribedId) {
    return DatabaseHelper.queryToDynaList(GET_OPERATION_ANAESTHESIA_DETAILS,
        new Object[] { prescribedId });
  }

  public List<BasicDynaBean> getOperationAdditionalTheaterDetails(Integer prescribedId) {
    return DatabaseHelper.queryToDynaList(GET_ADDITIONAL_THEATER_DETAILS,
        new Object[] { prescribedId.toString() });
  }

  private static final String GET_ADVANCED_OPER_ANAESTHESIA_DETAILS = 
      "SELECT * FROM surgery_anesthesia_details sud "
      + " JOIN operation_procedures op ON(op.prescribed_id=sud.prescribed_id AND "
      + " op.oper_priority = 'P') "
      + " JOIN operation_anaesthesia_details oad "
      + " ON(op.operation_details_id=oad.operation_details_id)"
      + " WHERE op.prescribed_id = ? order by sud.surgery_anesthesia_details_id";

  public List<BasicDynaBean> getAdvanceOperationAnaesthesiaDetails(Integer prescribedId) {
    return DatabaseHelper.queryToDynaList(GET_ADVANCED_OPER_ANAESTHESIA_DETAILS,
        new Object[] { prescribedId });
  }

  private static final String PRESCRIBED_OPERATIONS = " SELECT bps.prescribed_id AS order_id, "
      + "  bps.operation_name as item_id, om.operation_name as item_name,"
      + "  sdoc.doctor_id as surgeon_id, sdoc.doctor_name AS surgeon_name, "
      + "  adoc.doctor_id as anaesthetist_id, adoc.doctor_name AS anaesthetist_name, "
      + "  'Operation' as type, tm.theatre_id as theatre_id, tm.theatre_name AS theatre_name, "
      + "  start_datetime as from_timestamp, end_datetime as to_timestamp, "
      + "  bps.status, (CASE WHEN bps.finalization_status='Y' THEN 'F' ELSE "
      + "  bps.finalization_status END) as finalization_status, b.bill_no, b.is_tpa, "
      + "  b.status AS bill_status, b.is_primary_bill, bps.remarks, "
      + "   consultant_doctor as pres_doctor_id, bc.charge_id as theatre_charge_id, "
      + " bc_sac.charge_id as surgical_charge_id, "
      + "  bc_surgeon.charge_id as surgeon_charge_id, bc.insurance_category_id, "
      + "  doc.doctor_name as pres_doctor_name, bps.common_order_id, "
      + "  bps.prescribed_date as pres_timestamp, "
      + "  foo.secondary_operations, CASE WHEN bps.hrly = 'checked' THEN 'H' ELSE 'D' END as units,"
      + "  COALESCE(bc.posted_date, bps.prescribed_date) as posted_date, "
      + "  bc_sac.consultation_type_id as surgical_assistance_consultation_type_id, "
      + "  bc_surgeon.consultation_type_id as surgeon_consultation_type_id, "
      + "  COALESCE(bc.amount,0) as theatre_amount, COALESCE(bc.tax_amt,0) as theatre_tax_amount, "
      + "  COALESCE(bc.insurance_claim_amount, 0) as theatre_insurance_claim_amount, "
      + "  COALESCE(bc.sponsor_tax_amt, 0) as theatre_sponsor_tax_amount, "
      + "  COALESCE(bc.discount, 0) as theatre_discount, "
      + "  b.bill_rate_plan_id as org_id, b.bill_type, "
      + "  COALESCE(bc_surgeon.amount, 0) as surgeon_amount, "
      + "  COALESCE(bc_surgeon.tax_amt, 0) as surgeon_tax_amount, "
      + "  COALESCE(bc_surgeon.insurance_claim_amount, 0) as surgeon_insurance_claim_amount, "
      + "  COALESCE(bc_surgeon.sponsor_tax_amt, 0) as surgeon_sponsor_tax_amount, "
      + "  COALESCE(bc_surgeon.discount, 0) as surgeon_discount, "
      + "  COALESCE(bc_sac.amount, 0) as surgical_assistance_amount, COALESCE(bc_sac.tax_amt, 0) "
      + "  as surgical_assistance_tax_amount, "
      + "  COALESCE(bc_sac.insurance_claim_amount, 0) as "
      + "  surgical_assistance_insurance_claim_amount, COALESCE(bc_sac.sponsor_tax_amt, 0) as "
      + "  surgical_assistance_sponsor_tax_amount, "
      + "  COALESCE(bc_sac.discount, 0) as surgical_assistance_discount, "
      + "  (bc.amount + COALESCE(bc_surgeon.amount, 0) + COALESCE(bc_sac.amount, 0)) as amount, "
      + "  (bc.tax_amt + COALESCE(bc_surgeon.tax_amt, 0) + COALESCE(bc_sac.tax_amt, 0)) as tax_amt,"
      + "  (bc.insurance_claim_amount + COALESCE(bc_surgeon.insurance_claim_amount, 0) + "
      + "  COALESCE(bc_sac.insurance_claim_amount, 0)) as  insurance_claim_amount,"
      + "  (bc.sponsor_tax_amt + COALESCE(bc_surgeon.sponsor_tax_amt, 0) + "
      + "  COALESCE(bc_sac.sponsor_tax_amt, 0)) as sponsor_tax_amt, bc.item_excluded_from_doctor, "
      + "  bc.item_excluded_from_doctor_remarks, bc.charge_head,bc.charge_group, bc.preauth_act_id,"
      + "  ppc.content_id_ref, ppc.patient_package_content_id, "
      + "  ppa.preauth_required AS send_for_prior_auth, ppa.preauth_act_status, "
      + "  pm.multi_visit_package, true as canclebill "
      + "  FROM bed_operation_schedule bps "
      + "  JOIN operation_master om ON (om.op_id = bps.operation_name) "
      + "  LEFT JOIN  theatre_master tm ON (theatre_id = bps.theatre_name)  "
      + "  LEFT JOIN doctors sdoc ON (sdoc.doctor_id = bps.surgeon) "
      + "  LEFT JOIN doctors adoc ON (adoc.doctor_id = bps.anaesthetist) "
      + "  LEFT JOIN doctors doc ON (doc.doctor_id = bps.consultant_doctor) "
      + "  LEFT JOIN bill_activity_charge bac ON (bac.activity_id=bps.prescribed_id::text "
      + "  AND bac.activity_code='OPE') "
      + "  LEFT JOIN bill_charge bc USING (charge_id) " + "  LEFT JOIN bill b USING (bill_no) "
      + "  LEFT JOIN (select textcat_commacat(opm.operation_name) "
      + "  as secondary_operations, bos.prescribed_id "
      + "  FROM bed_operation_secondary bos "
      + "  JOIN operation_master opm ON(opm.op_id = bos.operation_id) "
      + "  group by bos.prescribed_id) as foo " 
      + " ON (foo.prescribed_id = bps.prescribed_id) "
      + "  LEFT JOIN bill_charge bc_sac ON (bc.bill_no = bc_sac.bill_no AND "
      + "  bc_sac.charge_head = 'SACOPE' and bac.charge_id = bc_sac.charge_ref )"
      + "  LEFT JOIN bill_charge bc_surgeon ON (bc.bill_no = bc_surgeon.bill_no AND "
      + "  bc_surgeon.charge_head = 'SUOPE' and bac.charge_id = bc_surgeon.charge_ref )"
      + " LEFT JOIN package_prescribed pp ON(pp.common_order_id = bps.common_order_id) "
      + " LEFT JOIN patient_package_content_consumed ppcc "
      + " ON (ppcc.prescription_id = bps.prescribed_id AND "
      + "     ppcc.item_type IN ('Operation')) "
      + " LEFT JOIN patient_package_contents ppc "
      + " ON (ppcc.patient_package_content_id = ppc.patient_package_content_id) "
      + " LEFT JOIN preauth_prescription_activities ppa "
      + "  ON bc.preauth_act_id = ppa.preauth_act_id"
      + " LEFT JOIN packages pm ON (pm.package_id = pp.package_id) ";

  private static final String GET_PRESCRIBED_OPERATIONS = PRESCRIBED_OPERATIONS
      + " WHERE bps.patient_id=? AND package_ref IS NULL ORDER BY bps.operation_name";

  public List<BasicDynaBean> getOrderedItems(String patientId) {
    return DatabaseHelper.queryToDynaList(GET_PRESCRIBED_OPERATIONS, new Object[] { patientId });
  }

  private static final String GET_PACKAGE_OPERATION_REF = "SELECT prescribed_id as order_id, "
      + " 'Operation' as type FROM bed_operation_schedule WHERE patient_id = ? ";

  @Override
  public String getPackageRefQuery() {
    return GET_PACKAGE_OPERATION_REF;
  }

  @Override
  public String getOperationRefQuery() {
    return null;
  }

  private static final String GET_FINALIZATION_STATUS = 
      "SELECT bps.prescribed_id, bps.finalization_status, bps.hrly, "
      + " bps.operation_name, bps.theatre_name, b.bill_no FROM bed_operation_schedule bps "
      + " JOIN bill_activity_charge bac ON (bac.activity_id=bps.prescribed_id::text AND "
      + " bac.activity_code='OPE') "
      + "  LEFT JOIN bill_charge bc USING (charge_id) LEFT JOIN bill b USING (bill_no) ";

  /**
   * get finalization status.
   * @param prescribedIdsList the prescribedIdsList
   * @return list of basic dyna bean
   */
  public List<BasicDynaBean> getFinalizationStatus(List<Integer> prescribedIdsList) {
    StringBuilder query = new StringBuilder(GET_FINALIZATION_STATUS);

    if (prescribedIdsList != null && !prescribedIdsList.isEmpty()) {
      String[] placeholdersArr = new String[prescribedIdsList.size()];
      Arrays.fill(placeholdersArr, "?");
      query.append(" WHERE prescribed_id IN ( ")
          .append(StringUtils.arrayToCommaDelimitedString(placeholdersArr)).append(")");
      return DatabaseHelper.queryToDynaList(query.toString(), prescribedIdsList.toArray());
    }

    return Collections.emptyList();
  }

  @Override
  public List<BasicDynaBean> getOperationRefOrders(String visitId, List<Integer> prescribedIdList) {
    return Collections.emptyList();
  }

}
