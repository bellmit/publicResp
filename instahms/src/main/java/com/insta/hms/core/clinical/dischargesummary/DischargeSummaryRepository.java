package com.insta.hms.core.clinical.dischargesummary;

import com.insta.hms.common.DatabaseHelper;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author anup vishwas.
 *
 */

@Repository
public class DischargeSummaryRepository {

  private static final String GET_ALL_VISIT_DETAILS_LIST =
      "SELECT pr.patient_id, pr.reg_date as visit_date,"
          + " pr.reg_time as visit_time, pr.status, pr.patient_discharge_status, pr.visit_type,"
          + " pr.disch_date_for_disch_summary as discharge_date,"
          + " pr.disch_time_for_disch_summary as discharge_time,"
          + " pr.discharge_format, pr.discharge_doc_id, null as last_updated_by,"
          + " pr.discharge_finalized_user,"
          + " null as report_status, bn.bed_name, wn.ward_name, dep.dept_name,"
          + " d.doctor_name, d.doctor_id,"
          + " dd.doctor_name as discharge_doctor_name, dd.doctor_id as discharge_doctor_id,"
          + " '' as loggedin_docornurse_visit" + " FROM patient_registration pr"
          + " LEFT JOIN admission adm ON (adm.patient_id = pr.patient_id)"
          + " LEFT JOIN bed_names bn ON (bn.bed_id = adm.bed_id)"
          + " LEFT JOIN ward_names wn ON (wn.ward_no = bn.ward_no)"
          + " LEFT JOIN doctors d ON (d.doctor_id = pr.doctor)"
          + " LEFT JOIN department dep ON (dep.dept_id = pr.dept_name)"
          + " LEFT JOIN doctors dd ON (dd.doctor_id = pr.discharge_doctor_id)"
          + " WHERE pr.mr_no IN ( ? , (SELECT COALESCE(CASE WHEN original_mr_no = ''"
          + " THEN NULL ELSE original_mr_no END, mr_no)"
          + " FROM patient_details WHERE mr_no=?)) AND pr.center_id = ?";

  protected List<BasicDynaBean> getAllVisitDetailsList(String mrNo, int centerId) {

    List<BasicDynaBean> visitDetailsList =
        DatabaseHelper.queryToDynaList(GET_ALL_VISIT_DETAILS_LIST, mrNo, mrNo, centerId);
    return visitDetailsList;
  }

  private static final String GET_DEFAULT_TEMPLATE_REPORT =
      " SELECT report_file, template_title, " + " template_caption as template_name "
          + " FROM discharge_format df " + " WHERE df.format_id = ?";

  public List<BasicDynaBean> getDefaultReport(String formatId) {

    return DatabaseHelper.queryToDynaList(GET_DEFAULT_TEMPLATE_REPORT, formatId);
  }

  private static final String GET_DEFAULT_FORM_FIELDS =
      " SELECT f.field_id, f.caption, f.no_of_lines,"
          + " f.default_text, fh.form_caption as template_name" + " FROM fields f"
          + " JOIN form_header fh ON (fh.form_id = f.form_id)" + " WHERE f.form_id = ?"
          + " ORDER BY displayorder";

  public List<BasicDynaBean> getDefaultFormFieldValues(String formId) {

    return DatabaseHelper.queryToDynaList(GET_DEFAULT_FORM_FIELDS, formId);
  }

  private static String MEDICINES_PRESCIBED_AND_SOLD =
      "SELECT" + " medicine_name FROM store_sales_main pmsm " + " JOIN bill b USING (bill_no) "
          + " JOIN store_sales_details pms USING (sale_id) "
          + " JOIN store_item_details USING (medicine_id) " + " WHERE visit_id=? " + " UNION "
          + " SELECT coalesce(sid.medicine_name, g.generic_name) as medicine_name "
          + " FROM patient_prescription pp " + " JOIN patient_medicine_prescriptions pmp"
          + " ON (pp.patient_presc_id=pmp.op_medicine_pres_id) "
          + " LEFT JOIN store_item_details sid ON (pmp.medicine_id=sid.medicine_id) "
          + " LEFT JOIN generic_name g ON (pmp.generic_code=g.generic_code) "
          + " JOIN doctor_consultation dc USING (consultation_id) " + " WHERE patient_id=? UNION"
          + " SELECT coalesce(sid.medicine_name, g.generic_name) as medicine_name"
          + " FROM pbm_medicine_prescriptions pbmp "
          + " LEFT JOIN store_item_details sid ON (pbmp.medicine_id=sid.medicine_id) "
          + " LEFT JOIN generic_name g ON (pbmp.generic_code=g.generic_code) "
          + " JOIN doctor_consultation dc USING (consultation_id) " + " WHERE patient_id=? ";

  /**
   * Get Medicines Prescribed And Sold.
   * 
   * @param patientId the visit id
   * @return list of beans
   */
  public List<BasicDynaBean> getMedicinesPrescribedAndSold(String patientId) {
    return DatabaseHelper.queryToDynaList(MEDICINES_PRESCIBED_AND_SOLD,
        new Object[] {patientId, patientId, patientId});
  }

  private static final String GET_REPORT_TEST_VALUES_FIELDS_FOR_PATIENT = "SELECT"
      + " ih.hospital_name, coalesce(rf.referal_name, refdoc.doctor_name) as referal_name,"
      + "  dd.category, "
      + "  td.prescribed_id, td.conducted_in_reportformat, td.resultlabel, td.report_value, "
      + "  td.units, td.reference_range, td.comments,d.conduction_instructions,"
      + "  d.sample_collection_instructions, "
      + "  coalesce(td.patient_report_file,'') as patient_report_file, td.withinnormal, "
      + "  d.test_name, d.type_of_specimen, dd.ddept_name, doc.doctor_name,tp.pres_date, "
      + "  tc.conducted_date,  tc.remarks, COALESCE(csc.sample_date,"
      + "  sc.sample_date) AS sample_date, "
      + "  tp.labno, COALESCE(csc.sample_sno, sc.sample_sno) AS sample_sno, ss.source_name, "
      + "  him.impression_details,him.short_impression,thr.no_of_blocks,thr.block_description,"
      + "  thr.no_of_slides,thr.slide_description,thr.clinical_details,thr.micro_gross_details,"
      + "  tmr.growth_exists,tmr.colony_count,mntm.nogrowth_template_name,tp.reference_pres,"
      + "  tmr.nogrowth_report_comment,tmr.growth_report_comment,tmr.microscopic_details,"
      + "  thr.histo_id,tmr.test_micro_id,"
      + "  tcr.test_type,tcr.specimen_adequacy,tcr.smear_received,"
      + "  tcr.microscopic_details AS cyto_microscopic_details,tcr.cyto_id, "
      + "  hic.short_impression AS cyto_short_impression,"
      + "  hic.impression_details AS cyto_impression_details,"
      + "  tcr.clinical_details AS cyto_clinical_details,"
      + "  td.test_detail_status,td.original_test_details_id,"
      + "  tp.re_conduction,td.amendment_reason,"
      + "  tp.reconducted_reason,tp.package_ref,pm.package_name,"
      + "  pm.description,st.sample_type,td.result_code,"
      + "  tc.validated_by, tc.validated_date, tc.technician, "
      + "  tc.conducted_by, cdoc.doctor_name as cond_doctor_name,"
      + "  sc.assertion_time, tp.pres_date, td.result_disclaimer, "
      + "  d.service_sub_group_id, ssg.service_sub_group_name,"
      + "  pc.display_order as display_order_in_package, dmm.method_name "
      + " FROM test_details td " + "  JOIN diagnostics d ON (d.test_id = td.test_id) "
      + "  JOIN tests_prescribed tp ON (COALESCE(tp.outsource_dest_prescribed_id,"
      + "  tp.prescribed_id) = td.prescribed_id) "
      + "  LEFT JOIN tests_conducted tc ON (tc.prescribed_id = td.prescribed_id) "
      + "  LEFT JOIN test_results_master tm ON (tm.test_id = td.test_id"
      + "  AND td.resultlabel_id = tm.resultlabel_id) "
      + "  LEFT JOIN diag_methodology_master dmm ON(tm.method_id = dmm.method_id)"
      + "  LEFT JOIN sample_collection sc ON (tp.sample_collection_id = sc.sample_collection_id) "
      + "  LEFT JOIN sample_collection csc ON (csc.sample_sno = sc.coll_sample_no) "
      + "  LEFT JOIN sample_type st ON (st.sample_type_id = sc.sample_type_id ) "
      + "  LEFT JOIN sample_sources ss on (ss.source_id = sc.sample_source_id ) "
      + "  LEFT JOIN doctors doc ON (doc.doctor_id = tp.pres_doctor) "
      + "  LEFT JOIN doctors cdoc ON (cdoc.doctor_id=tc.conducted_by) "
      + "  JOIN  diagnostics_departments dd ON (dd.ddept_id = d.ddept_id)"
      + "  LEFT JOIN service_sub_groups ssg ON (ssg.service_sub_group_id = d.service_sub_group_id) "
      + "  LEFT JOIN incoming_sample_registration isr on (tp.pat_id = isr.incoming_visit_id) "
      + "  LEFT JOIN referral rf  on (isr.referring_doctor = rf.referal_no) "
      + "  LEFT JOIN doctors refdoc on (isr.referring_doctor = refdoc.doctor_id) "
      + "  left join incoming_hospitals ih on(isr.orig_lab_name = ih.hospital_id) "
      + "  LEFT JOIN test_histopathology_results thr ON(thr.prescribed_id = td.prescribed_id) "
      + "  LEFT JOIN histo_impression_master him on(him.impression_id = thr.impression_id) "
      + "  LEFT JOIN test_microbiology_results tmr ON(tmr.prescribed_id = td.prescribed_id) "
      + "  LEFT JOIN micro_nogrowth_template_master mntm"
      + "  ON(mntm.nogrowth_template_id = tmr.nogrowth_template_id ) "
      + "  LEFT JOIN test_cytology_results tcr ON(tcr.prescribed_id = td.prescribed_id)"
      + "  LEFT JOIN histo_impression_master hic on(hic.impression_id = tcr.impression_id)"
      + "  LEFT JOIN package_prescribed pp ON( tp.package_ref = pp.prescription_id ) "
      + "  LEFT JOIN packages pm USING(package_id) "
      + "  LEFT JOIN package_contents pc ON(pc.package_id = pm.package_id "
      + "  AND activity_type IN ('Laboratory','Radiology') AND activity_id = d.test_id)"
      + "  WHERE td.test_detail_status != 'A' " + "  AND tp.conducted = 'S' AND tp.pat_id=? "
      + "  ORDER BY td.conducted_in_reportformat, tp.report_id, tc.conducted_date, td.test_id,"
      + "  tp.prescribed_id, tm.display_order";

  public List getTestValuesForPatient(String patientId) {
    return DatabaseHelper.queryToDynaList(GET_REPORT_TEST_VALUES_FIELDS_FOR_PATIENT, patientId);
  }

  private static final String GET_COMPLETED_PRESCRIBED_OPERATION =
      " SELECT * " + " FROM operation_prescriptions_view "
          + " WHERE patient_id = ? AND (status != 'C' OR status IS NULL) "
          + " ORDER BY mr_no,prescribed_time";

  // TO DO: need to move
  public List<BasicDynaBean> getOpetaionPrescriptionsCompleted(String patientId) {

    return DatabaseHelper.queryToDynaList(GET_COMPLETED_PRESCRIBED_OPERATION, patientId);
  }

  private static final String GET_DISCHARGE_SUMMARY_SAVE_FINALIZE_EVENT_DATA =
      "SELECT d_dis.doctor_name as discharge_doc_name,d_adm.doctor_name AS admitting_doc_name, "
          + "discharge_finalized_date, discharge_finalized_time, discharge_doc_id, mr_no, "
          + "d_dis.doctor_license_number,pr.patient_id, "
          + "d_dis.doc_first_name,d_dis.doc_last_name,d_dis.doc_middle_name, "
          + "discharge_summary_reopened FROM patient_registration pr "
          + "LEFT JOIN doctors d_dis ON (pr.discharge_doctor_id=d_dis.doctor_id) "
          + "LEFT JOIN doctors d_adm ON (pr.discharge_doctor_id=d_adm.doctor_id) "
          + "WHERE visit_type='i' AND patient_id=?";

  public BasicDynaBean getDischargeSummarySaveFinaliseEventData(String patientId) {
    return DatabaseHelper.queryToDynaBean(GET_DISCHARGE_SUMMARY_SAVE_FINALIZE_EVENT_DATA,
        patientId);
  }
}
