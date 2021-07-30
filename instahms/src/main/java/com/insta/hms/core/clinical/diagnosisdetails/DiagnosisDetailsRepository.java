package com.insta.hms.core.clinical.diagnosisdetails;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import com.insta.hms.core.clinical.forms.FormParameter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author krishnat.
 *
 */

@Repository
public class DiagnosisDetailsRepository extends GenericRepository {

  public DiagnosisDetailsRepository() {
    super("mrd_diagnosis");
  }

  private static final String GET_ALL_DIAGNOSIS_DETAILS = "SELECT "
      + " psd.patient_id, psd.section_id, id, description, icd_code, "
      + " md.code_type, diag_type, username, diagnosis_status_name, "
      + " remarks, md.diagnosis_status_id, "
      + " diagnosis_datetime, md.doctor_id, d.doctor_name, psd.section_detail_id, "
      + " md.year_of_onset, is_year_of_onset_mandatory " + " FROM patient_section_details psd "
      + " JOIN patient_section_forms psf ON (psd.patient_id=? and psd.item_type=? and "
      + "  #fitler#=? and section_id=-6 and psf.form_type=? "
      + "  and psd.section_detail_id=psf.section_detail_id) "
      + "   LEFT JOIN mrd_diagnosis md ON (md.visit_id = psd.patient_id) "
      + "   LEFT JOIN mrd_codes_master mcm "
      + "       ON (mcm.code = md.icd_code AND mcm.code_type = md.code_type) "
      + "   LEFT JOIN mrd_codes_details mcd ON (mcd.mrd_code_id = mcm.mrd_code_id) "
      + " LEFT JOIN diagnosis_statuses ds ON (ds.diagnosis_status_id = md.diagnosis_status_id) "
      + " LEFT JOIN doctors d ON (md.doctor_id=d.doctor_id) "
      + " ORDER BY diag_type asc, diagnosis_datetime asc";

  /**
   * converts To StructeredMap.
   * @param parameter the form param
   * @return list of BasicDynaBean
   */
  public List<BasicDynaBean> getAllDiagnosisDetails(FormParameter parameter) {

    return DatabaseHelper.queryToDynaList(
        GET_ALL_DIAGNOSIS_DETAILS.replace("#fitler#", "psd." + parameter.getFormFieldName()),
        parameter.getPatientId(), parameter.getItemType(), parameter.getId(),
        parameter.getFormType());
  }
  
  private static final String GET_ALL_VISIT_DIAGNOSIS_DETAILS = "SELECT "
      + " md.visit_id, md.id, md.description, md.icd_code, "
      + " md.code_type, md.diag_type, md.username, md.mod_time, "
      + " diagnosis_status_name, md.remarks, md.diagnosis_status_id, "
      + " md.diagnosis_datetime, md.doctor_id, d.doctor_name " + " FROM mrd_diagnosis md "
      + " LEFT JOIN diagnosis_statuses ds USING (diagnosis_status_id) "
      + " LEFT JOIN doctors d ON (md.doctor_id=d.doctor_id) " + " WHERE md.visit_id=? "
      + " ORDER BY diag_type asc, diagnosis_datetime asc";

  public List<BasicDynaBean> getAllDiagnosisDetails(String patientId) {
    return DatabaseHelper.queryToDynaList(GET_ALL_VISIT_DIAGNOSIS_DETAILS, patientId);
  }

  private static final String GET_ALL_ACTIVE_DIAGNOSIS_DETAILS = "SELECT "
      + " md.visit_id as patient_id, id, description, icd_code, "
      + " md.code_type, diag_type, username, diagnosis_status_name, "
      + " remarks, md.diagnosis_status_id, "
      + " diagnosis_datetime, md.doctor_id, d.doctor_name, "
      + " md.year_of_onset, is_year_of_onset_mandatory " + " FROM mrd_diagnosis md "
      + "  JOIN mrd_codes_master mcm ON (mcm.code = md.icd_code AND mcm.code_type = md.code_type) "
      + "  LEFT JOIN mrd_codes_details mcd ON (mcd.mrd_code_id = mcm.mrd_code_id) "
      + " LEFT JOIN diagnosis_statuses ds USING (diagnosis_status_id) "
      + " LEFT JOIN doctors d ON (md.doctor_id=d.doctor_id) " + " WHERE md.visit_id=? "
      + " ORDER BY diag_type asc, diagnosis_datetime asc";

  public List<BasicDynaBean> getAllActiveDiagnosisDetails(String patientId) {

    return DatabaseHelper.queryToDynaList(GET_ALL_ACTIVE_DIAGNOSIS_DETAILS, patientId);
  }

  private static final String CHECK_DUPLICATE_DIAG_CODE = " SELECT "
      + " distinct icd_code, visit_id, diag_type FROM mrd_diagnosis "
      + " WHERE visit_id=? group by visit_id, diag_type, icd_code ";

  public List<BasicDynaBean> isDuplicateDiagnosisCode(String patientId) {
    List<BasicDynaBean> list = DatabaseHelper.queryToDynaList(CHECK_DUPLICATE_DIAG_CODE, patientId);
    return list;
  }

  private static final String MULTIPLE_PRINCIPLE_DIAGNOSIS = "SELECT "
      + " count(diag_type), diag_type FROM mrd_diagnosis WHERE visit_id=? "
      + " group by diag_type order by diag_type";

  public List<BasicDynaBean> diagnosisCountList(String patientId) {
    return DatabaseHelper.queryToDynaList(MULTIPLE_PRINCIPLE_DIAGNOSIS, new Object[] { patientId });
  }

  public static String FIND_ALL_DIAGNOSIS = " SELECT (CASE WHEN diag_type = 'P' THEN 'Principal' "
      + " WHEN diag_type = 'A' THEN 'Admitting' " + " WHEN diag_type = 'V' THEN 'Reason For Visit' "
      + " ELSE 'Secondary' END) AS diag_type, md.diag_type as diagnosis_type, "
      + " md.code_type, icd_code, code_desc " + " FROM mrd_diagnosis md "
      + " JOIN mrd_codes_master mcm ON (mcm.code_type = md.code_type AND mcm.code = md.icd_code) "
      + " WHERE visit_id = ?";

  public List<BasicDynaBean> findAllDiagnosis(String mainVisitId) {
    return DatabaseHelper.queryToDynaList(FIND_ALL_DIAGNOSIS, new Object[] { mainVisitId });
  }
}
