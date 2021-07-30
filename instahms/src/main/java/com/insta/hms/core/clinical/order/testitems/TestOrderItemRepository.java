package com.insta.hms.core.clinical.order.testitems;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.QueryAssembler;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.clinical.consultation.prescriptions.PendingPrescriptionsRepository;
import com.insta.hms.core.clinical.order.master.OrderItemRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class TestOrderItemRepository.
 */
@Repository
public class TestOrderItemRepository extends OrderItemRepository {

  /** The Constant GENERATE_NEXT_SEQUENCE_QUERY. */
  private static final String GENERATE_NEXT_SEQUENCE_QUERY = " SELECT nextval(?)";

  /** The Constant PREFIX_FIELD. */
  private static final String PREFIX_FIELD = "prefix";

  /** The Constant PATTERN_FIELD. */
  private static final String PATTERN_FIELD = "pattern";

  /** The Constant NEXTVAL_FIELD. */
  private static final String NEXTVAL_FIELD = "nextval";

  /** The pending prescriptions repository. */
  @LazyAutowired
  private PendingPrescriptionsRepository pendingPrescriptionsRepository;
  
  /**
   * Instantiates a new test order item repository.
   */
  public TestOrderItemRepository() {
    super("tests_prescribed");
  }

  /** The Constant GET_PRESC_TEST_LIST. */
  private static final String GET_PRESC_TEST_LIST =
      " SELECT d.test_name, d.test_id, tp.prescribed_id" + " FROM tests_prescribed tp "
          + " JOIN diagnostics d ON (d.test_id = tp.test_id)"
          + " WHERE tp.pat_id = ? AND tp.conducted = 'S'";

  /**
   * Gets the prescribed test list.
   *
   * @param patientId the patient id
   * @return the prescribed test list
   */
  public List<BasicDynaBean> getPrescribedTestList(String patientId) {

    return DatabaseHelper.queryToDynaList(GET_PRESC_TEST_LIST, patientId);
  }

  /** The Constant GET_TEST_DETAIL. */
  private static final String GET_TEST_DETAIL = "SELECT d.test_name, d.test_id,"
      + " d.conduction_format, tp.prescribed_id::text, td.patient_report_file, td.report_value,"
      + " td.comments, td.resultlabel, td.units, td.reference_range, td.withinnormal"
      + " FROM tests_prescribed tp" + " JOIN diagnostics d ON (d.test_id = tp.test_id)"
      + " JOIN test_details td ON (td.prescribed_id = tp.prescribed_id)";

  /**
   * Gets the selected test details.
   *
   * @param prescId the presc id
   * @return the selected test details
   */
  public List<BasicDynaBean> getSelectedTestDetails(String[] prescId) {

    StringBuilder where = new StringBuilder();
    QueryAssembler.addWhereFieldOpValue(false, where, "tp.prescribed_id::text", "IN",
        Arrays.asList(prescId));
    return DatabaseHelper.queryToDynaList(GET_TEST_DETAIL + " " + where.toString(),
        Arrays.asList(prescId).toArray());
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.common.GenericRepository#getNextSequence()
   */
  public Integer getNextSequence() {
    return DatabaseHelper.getInteger(GENERATE_NEXT_SEQUENCE_QUERY, "test_prescribed");
  }

  /**
   * AutoIncrement alternative Used by radiology and lab both Don't have repository for both as part
   * of test itself.
   *
   * @param sequenceName the sequence name
   * @param typeNumber the type number
   * @return the sequence id
   */
  public String getSequenceId(String sequenceName, String typeNumber) {
    BasicDynaBean details =
        DatabaseHelper.queryToDynaBean(GET_SEQUENCE_ID_QUERY, sequenceName, typeNumber);
    String prefix =
        details.get(PREFIX_FIELD) != null ? (String) details.get(PREFIX_FIELD) : typeNumber;
    String pattern =
        details.get(PATTERN_FIELD) != null ? (String) details.get(PATTERN_FIELD) : "000000";
    Long sequence = details.get(NEXTVAL_FIELD) != null ? (Long) details.get(NEXTVAL_FIELD) : 0;
    return prependPrefix(prefix, pattern, sequence);
  }

  /**
   * Gets the sequence id.
   *
   * @param typeNumber the type number
   * @return the sequence id
   */
  public String getSequenceId(String typeNumber) {
    BasicDynaBean sequenceBean = null;

    if (!StringUtils.isEmpty(typeNumber) && typeNumber.equals("LABNO")) {
      sequenceBean = DatabaseHelper.queryToDynaBean(LAB_SEQUENCE_PATTERN);
    } else if (!StringUtils.isEmpty(typeNumber) && typeNumber.equals("RADNO")) {
      sequenceBean = DatabaseHelper.queryToDynaBean(RADIOLOGY_SEQUENCE_PATTERN);
    }

    String patternId = (String) sequenceBean.get("pattern_id");
    return DatabaseHelper.getNextPatternId(patternId);
  }

  /** The Constant DIAGNOSTICS_TESTS_QUERY. */
  private static final String DIAGNOSTICS_TESTS_QUERY =
      " SELECT CASE WHEN category = 'DEP_LAB' THEN 'Laboratory' ELSE 'Radiology' END AS type, "
          + " d.test_id AS id, d.test_name AS name, d.diag_code AS code, "
          + " ddept.ddept_name AS department, ddept.ddept_id AS department_id,"
          + " s.service_sub_group_id as subGrpId, "
          + " s.service_group_id as groupid, d.prior_auth_required,"
          + " d.insurance_category_id, conduction_applicable,"
          + " (CASE WHEN d.conducting_doc_mandatory = 'O' THEN true ELSE false END)"
          + " as conducting_doc_mandatory, "
          + " results_entry_applicable,'N' as tooth_num_required,false as multi_visit_package,"
          + " -1 as center_id, '-1' as tpa_id, "
          + " d.mandate_additional_info, d.additional_info_reqts " + " FROM diagnostics d "
          + " JOIN diagnostics_departments ddept USING (ddept_id) "
          + " JOIN service_sub_groups s using(service_sub_group_id)";

  /**
   * Gets the item details.
   *
   * @param entityIdList the entity id list
   * @return the item details
   */
  public List<BasicDynaBean> getItemDetails(List<Object> entityIdList) {
    return super.getItemDetails(DIAGNOSTICS_TESTS_QUERY, entityIdList, "d.test_id", null);
  }

  /** The Constant ORDERED_ITEMS. */
  private static final String ORDERED_ITEMS =
      "SELECT tp.pat_id as patient_id, tp.prescribed_id as order_id, tp.common_order_id,"
          + " CASE WHEN ddept.category = 'DEP_LAB' THEN 'Laboratory' ELSE 'Radiology' END AS type,"
          + " ddept.ddept_id as sub_type, ddept.ddept_name as sub_type_name,"
          + " 'DIA' as activity_code,"
          + " tp.test_id as item_id, tp.reference_pres, prev_tp.common_order_id as"
          + " reference_common_order_id, CASE WHEN ppc.panel_id IS NOT NULL AND "
          + " pm.multi_visit_package THEN d.test_name || '"
          + " : ' || pap.package_name ELSE d.test_name  END as item_name, d.diag_code as item_code,"
          + " tp.pres_doctor as pres_doctor_id, pd.doctor_name as pres_doctor_name,"
          + " tp.pres_date as pres_timestamp,"
          + " COALESCE(bc.posted_date, tp.pres_date) as posted_date, "
          + " tp.remarks,TO_CHAR(tp.pres_date,'dd-mm-yyyy') as pres_date, tp.clinical_notes, "
          + " 1 as quantity, null::timestamp as from_timestamp, null::timestamp as to_timestamp,"
          + " '' AS details, null as operation_ref, tp.package_ref, b.bill_no, b.is_tpa,"
          + " b.bill_type, b.is_primary_bill, b.status as bill_status,bc.amount, bc.tax_amt, "
          + " bc.insurance_claim_amount, bc.sponsor_tax_amt, "
          + " tp.conducted AS status, (CASE WHEN tp.sflag = '1' THEN 'Y' ELSE 'N' END )"
          + " as sample_collected,'U' as finalization_status,"
          + " bc.prior_auth_id, bc.prior_auth_mode_id, bc.first_of_category,"
          + " dc.doctor_name as cond_doctor_name,"
          + " dc.doctor_id as cond_doctor_id,tp.labno,NOT tp.re_conduction as canclebill,"
          + " '' AS isdialysis, '' AS dialysis_status, '' AS completion_status,"
          + " tp.priority as urgent,null as tooth_number, bc.insurance_category_id, bc.charge_id,"
          + " tp.outsource_dest_prescribed_id, d.mandate_additional_info,"
          + " d.additional_info_reqts, COALESCE(pm.multi_visit_package,false)"
          + " AS multi_visit_package,"
          + " pm.package_id, pp.pat_package_id, b.bill_rate_plan_id as org_id,"
          + " bc.item_excluded_from_doctor, bc.item_excluded_from_doctor_remarks,"
          + " bc.charge_head,bc.charge_group, bc.preauth_act_id, ppc.content_id_ref, "
          + " ppc.patient_package_content_id, "
          + " ppa.preauth_required AS send_for_prior_auth, ppa.preauth_act_status"
          + " FROM tests_prescribed tp"
          + " JOIN diagnostics d on d.test_id = tp.test_id"
          + " LEFT JOIN tests_prescribed prev_tp ON prev_tp.prescribed_id = tp.reference_pres"
          + " AND prev_tp.pat_id = ?"
          + " JOIN diagnostics_departments ddept on ddept.ddept_id = d.ddept_id"
          + " LEFT JOIN doctors pd on tp.pres_doctor = pd.doctor_id"
          + " LEFT JOIN bill_activity_charge bac ON bac.activity_id=tp.prescribed_id::text"
          + " AND bac.activity_code='DIA'"
          + " LEFT JOIN bill_charge bc USING (charge_id)"
          + " LEFT JOIN doctors dc on dc.doctor_id = bc.payee_doctor_id"
          + " LEFT JOIN bill b USING (bill_no)"
          + " LEFT JOIN preauth_prescription_activities ppa "
          + "  ON bc.preauth_act_id = ppa.preauth_act_id"    
          + " LEFT JOIN package_prescribed pp ON(pp.prescription_id = tp.package_ref) "
          + " LEFT JOIN patient_package_content_consumed ppcc "
          + " ON (ppcc.prescription_id = tp.prescribed_id AND "
          + "     ppcc.item_type IN ('Laboratory', 'Radiology')) "
          + " LEFT JOIN patient_package_contents ppc "
          + " ON (ppcc.patient_package_content_id = ppc.patient_package_content_id) "
          + " LEFT JOIN packages pm ON CASE WHEN tp.package_ref IS NULL "
          + "   THEN (pm.package_id::text = tp.test_id)  "
          + "   ELSE (pm.package_id = pp.package_id) END "
          + " LEFT JOIN packages pap ON (ppc.panel_id = pap.package_id)"
          + "WHERE tp.pat_id = ? "
          + "   and tp.new_test_prescribed_id is null"
      ;

  /** The Constant GET_MVP_ITEM_CONDITION_TEST. */
  public static final String GET_MVP_ITEM_CONDITION_TEST =
      " AND tp.package_ref IS NOT NULL AND pm.multi_visit_package = true ";

  /** The Constant IGNORE_MVP_ITEM_CONDITION_TEST. */
  public static final String IGNORE_MVP_ITEM_CONDITION_TEST =
      " AND tp.package_ref IS NULL AND (pm.multi_visit_package = false"
      + " or pm.multi_visit_package IS NULL )";

  /**
   * Gets the ordered items.
   *
   * @param visitId the visit id
   * @param packageRef the package ref
   * @return the ordered items
   */
  public List<BasicDynaBean> getOrderedItems(String visitId, Boolean packageRef) {
    String packageRefCondition;
    if (packageRef != null && packageRef) {
      packageRefCondition = GET_MVP_ITEM_CONDITION_TEST;
    } else {
      packageRefCondition = IGNORE_MVP_ITEM_CONDITION_TEST;
    }
    return DatabaseHelper.queryToDynaList(ORDERED_ITEMS + packageRefCondition, visitId, visitId);
  }

  /** The Constant FOR_PATIENT_TESTS. */
  private static final String FOR_PATIENT_TESTS =
      "UPDATE patient_prescription SET status = 'P', username=? WHERE patient_presc_id = ?";

  /**
   * Update cancel status to patient.
   *
   * @param items the items
   * @param userName the user name
   * @return the int[]
   */
  public int[] updateCancelStatusToPatient(List<BasicDynaBean> items, String userName) {
    List<Object[]> paramsList = new ArrayList<>();
    for (BasicDynaBean item : items) {
      BasicDynaBean bean = findByKey("prescribed_id", item.get("prescribed_id"));
      Object docPrescId = bean == null ? null : bean.get("doc_presc_id");
      if (docPrescId != null && !"".equals(docPrescId.toString())) {
        paramsList.add(new Object[] {userName, docPrescId});
        pendingPrescriptionsRepository.updatePendingPrescriptionStatus((Integer)docPrescId, 1, "P");
      }
    }
    return DatabaseHelper.batchUpdate(FOR_PATIENT_TESTS, paramsList);
  }

  /** The Constant PRESCRIBED_DETAILS. */
  private static final String PRESCRIBED_DETAILS = " SELECT tp.*, d.test_name, ddept_id, "
      + " coalesce(pr.center_id, isr.center_id) as center_id ,"
      + " CASE WHEN d.sample_needed = 'n' THEN 'U' ELSE tp.sflag END AS sample_status ,"
      + " sc.sample_status as sample_collection_status, "
      + " CASE WHEN is_outhouse_test(d.test_id,coalesce(pr.center_id, isr.center_id)::integer) "
      + "      THEN 'O' " + "      ELSE 'I' "
      + " END AS house_status, dom.outsource_dest_type, isr.incoming_source_type,"
      + " CASE WHEN pr.patient_id IS NOT NULL THEN 'hospital' ELSE 'incoming' END AS hospital,"
      + " sc.sample_transfer_status " + " FROM tests_prescribed tp "
      + "   JOIN diagnostics d ON (d.test_id=tp.test_id) "
      + "   LEFT JOIN patient_registration pr ON (tp.pat_id=pr.patient_id) "
      + "   LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id=tp.pat_id) "
      + "   LEFT JOIN sample_collection sc ON (sc.sample_collection_id = tp.sample_collection_id)"
      + " LEFT JOIN outsource_sample_details oh ON (oh.prescribed_id = tp.prescribed_id)"
      + " LEFT JOIN diag_outsource_master dom ON (dom.outsource_dest_id = oh.outsource_dest_id)"
      + " WHERE tp.prescribed_id=? ";

  /**
   * Gets the prescribed details.
   *
   * @param prescribedId the prescribed id
   * @return the prescribed details
   */
  public BasicDynaBean getPrescribedDetails(int prescribedId) {
    return DatabaseHelper.queryToDynaBean(PRESCRIBED_DETAILS, new Object[] {prescribedId});
  }

  /** The Constant GET_PACKAGE_OPERATION_REF. */
  private static final String GET_PACKAGE_OPERATION_REF = "SELECT prescribed_id as order_id, "
      + " CASE WHEN ddept.category = 'DEP_LAB' THEN 'Laboratory' ELSE 'Radiology' END AS type "
      + " FROM tests_prescribed tp JOIN diagnostics d on d.test_id = tp.test_id "
      + " JOIN diagnostics_departments ddept ON ddept.ddept_id = d.ddept_id"
      + " WHERE tp.pat_id = ? ";

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.order.master.OrderItemRepository#getPackageRefQuery()
   */
  @Override
  public String getPackageRefQuery() {
    return GET_PACKAGE_OPERATION_REF;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.order.master.OrderItemRepository#getOperationRefQuery()
   */
  @Override
  public String getOperationRefQuery() {
    return null;
  }

  /** The Constant LAB_SEQUENCE_PATTERN. */
  private static final String LAB_SEQUENCE_PATTERN = " SELECT pattern_id "
      + " FROM (SELECT min(priority) as priority,  pattern_id FROM hosp_lab_number_seq_prefs "
      + " GROUP BY pattern_id ORDER BY priority limit 1) as foo";

  /** The Constant RADIOLOGY_SEQUENCE_PATTERN. */
  private static final String RADIOLOGY_SEQUENCE_PATTERN =
      " SELECT pattern_id "
          + " FROM (SELECT min(priority) as priority,  pattern_id"
          + " FROM hosp_radiology_number_seq_prefs "
          + " GROUP BY pattern_id ORDER BY priority limit 1) as foo";

  /** The Constant GET_MR_NO_FOR_REPORT_ID. */
  private static final String GET_MR_NO_FOR_REPORT_ID = "SELECT distinct(mr_no) "
      + " from tests_prescribed JOIN patient_details using(mr_no) WHERE report_id in (:reportid)";

  /** The Constant CHECK_ISR_PATIENT. */
  private static final String CHECK_ISR_PATIENT = "SELECT * FROM tests_prescribed tp"
      + " JOIN incoming_sample_registration isr ON(tp.pat_id = isr.incoming_visit_id)"
      + " WHERE report_id in (:reportid)";

  /**
   * Gets the mr no for report id.
   *
   * @param reportId the report id
   * @return the mr no for report id
   */
  public List<String> getMrNoForReportId(List<String> reportId) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("reportid", reportId, java.sql.Types.INTEGER);
    List<BasicDynaBean> listBeanMrNo =
        DatabaseHelper.queryToDynaList(GET_MR_NO_FOR_REPORT_ID, parameters);
    List<String> mrNos = null;
    if (listBeanMrNo.isEmpty()) {
      List<BasicDynaBean> listBeanISR =
          DatabaseHelper.queryToDynaList(CHECK_ISR_PATIENT, parameters);
      if (!listBeanISR.isEmpty()) {
        mrNos = Arrays.asList("ISR");
      }
    } else {
      mrNos = new ArrayList<>(listBeanMrNo.size());
      for (BasicDynaBean bean : listBeanMrNo) {
        String thisMrNo = (String) bean.get("mr_no");
        mrNos.add(thisMrNo);
      }
    }
    return mrNos;
  }

  /** The Constant GET_MR_NO_FOR_PRESCRIBED_ID. */
  private static final String GET_MR_NO_FOR_PRESCRIBED_ID = "SELECT mr_no from tests_prescribed "
      + " JOIN patient_details using(mr_no) WHERE prescribed_id in (:prescribedid)";

  /** The Constant CHECK_ISR_PATIENT_FOR_PRESCRIBED_ID. */
  private static final String CHECK_ISR_PATIENT_FOR_PRESCRIBED_ID = "SELECT * "
      + " FROM tests_prescribed tp"
      + " JOIN incoming_sample_registration isr ON(tp.pat_id = isr.incoming_visit_id)"
      + " WHERE prescribed_id in (:prescribedid)";

  /**
   * Gets the mr no for prescribed id.
   *
   * @param prescribedId the prescribed id
   * @return the mr no for prescribed id
   */
  public List<String> getMrNoForPrescribedId(List<String> prescribedId) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("prescribedid", prescribedId, java.sql.Types.INTEGER);
    List<BasicDynaBean> listBeanMrNo =
        DatabaseHelper.queryToDynaList(GET_MR_NO_FOR_PRESCRIBED_ID, parameters);
    List<String> mrNos = null;
    if (listBeanMrNo.isEmpty()) {
      List<BasicDynaBean> listBeanISR =
          DatabaseHelper.queryToDynaList(CHECK_ISR_PATIENT_FOR_PRESCRIBED_ID, parameters);
      if (!listBeanISR.isEmpty()) {
        mrNos = Arrays.asList("ISR");
      }
    } else {
      mrNos = new ArrayList<>(listBeanMrNo.size());
      for (BasicDynaBean bean : listBeanMrNo) {
        String thisMrNo = (String) bean.get("mr_no");
        mrNos.add(thisMrNo);
      }
    }
    return mrNos;
  }
  
  private static final String GET_MAIN_TEST_PRESC_ID_OF_RECONDUCTION_TEST = "WITH RECURSIVE"
      + " presc_id AS (SELECT prescribed_id,test_id,reference_pres"
      + " FROM tests_prescribed WHERE prescribed_id=:reConductedPrescId"
      + " UNION ALL"
      + " SELECT t.prescribed_id,t.test_id,t.reference_pres"
      + " FROM tests_prescribed t"
      + " INNER JOIN presc_id p ON p.reference_pres = t.prescribed_id)"
      + " SELECT prescribed_id FROM presc_id WHERE reference_pres is null";
  
  /**
   * Gets the prescription id of first test in recursive way.
   * 
   * @param reconductedPrescId the presc id
   * @return int
   */
  public int getMainTestPrescIdOfReconductedTest(int reconductedPrescId) {
    MapSqlParameterSource parameter = new MapSqlParameterSource();
    parameter.addValue("reConductedPrescId", reconductedPrescId);
    return DatabaseHelper.getInteger(GET_MAIN_TEST_PRESC_ID_OF_RECONDUCTION_TEST, parameter);
  }
  
  private static final String GET_INCOMING_TEST_DETAILS = "SELECT"
      + " tp_source.pat_id AS source_visit_id,"
      + " tp_source.prescribed_id AS source_prescribed_id, pr.center_id AS source_center_id"
      + " FROM tests_prescribed tp"
      + " LEFT JOIN tests_prescribed tp_source"
      + " ON (tp.source_test_prescribed_id = tp_source.prescribed_id)"
      + " LEFT JOIN patient_registration pr ON (tp_source.pat_id = pr.patient_id)"
      + " WHERE tp.prescribed_id = :incomingTestPrescId";
  
  /**
   * Get Incoming Test Details.
   * 
   * @param prescId the presc id
   * @return map
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> getIncomingTestDetails(int prescId) {
    MapSqlParameterSource parameter = new MapSqlParameterSource();
    parameter.addValue("incomingTestPrescId", prescId);
    BasicDynaBean bean =
        DatabaseHelper.queryToDynaBean(GET_INCOMING_TEST_DETAILS, parameter);
    if (bean != null) {
      return bean.getMap();
    }
    return null;
  }
}
