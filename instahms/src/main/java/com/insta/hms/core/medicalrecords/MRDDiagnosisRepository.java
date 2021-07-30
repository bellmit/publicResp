package com.insta.hms.core.medicalrecords;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

/**
 * The Class MRDDiagnosisRepository.
 *
 * @author anup vishwas
 */

@Repository
public class MRDDiagnosisRepository extends GenericRepository {

  /**
   * Instantiates a new MRD diagnosis repository.
   */
  public MRDDiagnosisRepository() {
    super("mrd_diagnosis");
  }

  /**
   * The Constant GET_ALL_DIAGNOSIS_DETAILS. returns the primary diagnosis
   * details as first record. Diag codes are independent for each visit.
   *
   */
  private static final String GET_ALL_DIAGNOSIS_DETAILS = "SELECT "
      + " visit_id, id, description, " + " icd_code, md.code_type, diag_type, "
      + " username, mod_time, diagnosis_status_name, "
      + " remarks, md.diagnosis_status_id, diagnosis_datetime, "
      + " md.doctor_id, d.doctor_name, "
      + " sent_for_approval, year_of_onset, is_year_of_onset_mandatory "
      + " FROM mrd_diagnosis md " + "   JOIN mrd_codes_master mcm "
      + "     ON (mcm.code = md.icd_code AND mcm.code_type = md.code_type) "
      + "   LEFT JOIN mrd_codes_details mcd "
      + "     ON (mcd.mrd_code_id = mcm.mrd_code_id) "
      + "   LEFT JOIN diagnosis_statuses ds USING (diagnosis_status_id) "
      + "   LEFT JOIN doctors d ON (md.doctor_id=d.doctor_id) "
      + " WHERE visit_id = ? "
      + " ORDER BY diagnosis_datetime asc, diag_type asc";

  /**
   * Gets the all diagnosis details.
   *
   * @param patientId
   *          the patient id
   * @return the all diagnosis details
   */
  public List<BasicDynaBean> getAllDiagnosisDetails(String patientId) {
    return DatabaseHelper.queryToDynaList(GET_ALL_DIAGNOSIS_DETAILS, patientId);
  }

  /** The Constant PRVS_DIAGNOSIS_DETAILS. */
  private static final String PRVS_DIAGNOSIS_DETAILS = "SELECT visit_id,"
      + " md.id, md.doctor_id, md.description, adm_request_id, "
      + " icd_code, md.code_type, diag_type, md.remarks, "
      + " ds.diagnosis_status_name, " + " diagnosis_datetime, doc.doctor_name, "
      + " pr.visit_type, pr.reg_date as visited_date, md.username, ds.diagnosis_status_id, "
      + " year_of_onset, is_year_of_onset_mandatory, d.dept_name "
      + " FROM mrd_diagnosis md "
      + " JOIN (select distinct visit_id as patient_id, prg.reg_date, prg.reg_time,"
      + " prg.visit_type, prg.dept_name From mrd_diagnosis mrd"
      + " JOIN patient_registration prg on (prg.patient_id = mrd.visit_id)"
      + " where prg.mr_no = ?"
      + " AND prg.reg_date+prg.reg_time < ? ORDER BY prg.reg_date DESC,"
      + " prg.reg_time DESC LIMIT ? OFFSET ? ) as pr ON (pr.patient_id = md.visit_id) "
      + " JOIN mrd_codes_master mcm "
      + "   ON (mcm.code = md.icd_code AND mcm.code_type = md.code_type) "
      + " LEFT JOIN mrd_codes_details mcd "
      + "   ON (mcd.mrd_code_id = mcm.mrd_code_id) "
      + " LEFT JOIN doctors doc ON (md.doctor_id = doc.doctor_id) "
      + " LEFT JOIN diagnosis_statuses ds "
      + "   ON (md.diagnosis_status_id = ds.diagnosis_status_id) "
      + " JOIN department d ON (d.dept_id = pr.dept_name) "
      + " ORDER BY diagnosis_datetime DESC, pr.reg_date DESC, pr.reg_time DESC, diag_type ";

  /**
   * Gets the previous diagnosis details.
   *
   * @param mrNo
   *          the mr no
   * @param regDateTime
   *          the reg date time
   * @return the previous diagnosis details
   */
  public List<BasicDynaBean> getPreviousDiagnosisDetails(String mrNo,
      Timestamp regDateTime, Integer limit, Integer pageNo) {

    return DatabaseHelper.queryToDynaList(PRVS_DIAGNOSIS_DETAILS, mrNo,
        regDateTime, limit, (pageNo - 1) * limit);
  }

  /** The Constant GET_YOONSET_DETAILS. */
  private static final String GET_YOONSET_DETAILS = "SELECT "
      + "md.year_of_onset FROM mrd_diagnosis md "
      + "JOIN patient_registration pr "
      + " ON (pr.patient_id = md.visit_id AND pr.mr_no=?) "
      + "WHERE icd_code = ? ORDER BY md.mod_time DESC LIMIT 1";

  /**
   * Gets the year of onset details.
   *
   * @param mrNo
   *          the mr no
   * @param icdCode
   *          the icd code
   * @return the year of onset details
   */
  public BasicDynaBean getYearOfOnsetDetails(String mrNo, String icdCode) {
    return DatabaseHelper.queryToDynaBean(GET_YOONSET_DETAILS, mrNo, icdCode);
  }
}
