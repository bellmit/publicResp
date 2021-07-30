package com.insta.hms.core.clinical.patientproblems;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import com.insta.hms.core.clinical.forms.FormParameter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Patient Problem List Repository.
 * 
 * @author VinayKumarJavalkar
 *
 */
@Repository
public class PatientProblemListRepository extends GenericRepository {

  public PatientProblemListRepository() {
    super("patient_problem_list");
  }

  private static final String GET_PATIENT_PROBLEMS = "SELECT"
      + " ppl.ppl_id, ppl.mr_no, ppl.patient_problem_id, ppl.ext_identified,"
      + " COALESCE(d.doctor_name,r.referal_name,ppl.ext_identified_by) AS identified_by_name,"
      + " ppl.onset, ppl.problem_note, mcm.code_type, mcm.code AS patient_problem_code,"
      + " ppl.identified_by, mcm.code_desc AS patient_problem_desc, ppl.recorded_date,"
      + " ppl.recorded_by, rd.doctor_name AS recorded_by_name"
      + " FROM patient_problem_list ppl"
      + " JOIN mrd_codes_master mcm ON mcm.mrd_code_id = ppl.patient_problem_id"
      + " LEFT JOIN doctors d ON ppl.identified_by = d.doctor_id"
      + " LEFT JOIN referral r ON ppl.identified_by = r.referal_no"
      + " LEFT JOIN doctors rd ON ppl.recorded_by = rd.doctor_id"
      + " WHERE ppl.mr_no = :mrNo AND ppl.status = 'A'"
      + " AND (ppl.created_in_sec_detail_id = :sectionDetailId"
      + " OR ppl.created_at <= :consVisitStartDate::TIMESTAMP)";

  /**
   * Get Patient Problems.
   * 
   * @param formParam the form parmateres
   * @return list of beans
   */
  public List<BasicDynaBean> getPatientProblems(FormParameter formParam, int sectionDetailId) {
    MapSqlParameterSource parameter = new MapSqlParameterSource();
    parameter.addValue("mrNo", formParam.getMrNo());
    parameter.addValue("sectionDetailId", sectionDetailId);
    if (sectionDetailId != 0) {
      parameter.addValue("consVisitStartDate", getStartTimeOfVisitOrConsultation(formParam));
    } else {
      parameter.addValue("consVisitStartDate", "now()");
    }
    return DatabaseHelper.queryToDynaList(GET_PATIENT_PROBLEMS, parameter);
  }
  
  private static final String GET_START_DATE_TIME_OF_VISIT_OR_CONS = "SELECT"
      + " CASE WHEN (pr.visit_type='o') THEN dc.start_datetime::TEXT"
      + " ELSE concat(to_char(pr.reg_date,'YYYY-MM-DD '),"
      + " to_char(pr.reg_time,'HH24:MI:SS')) END AS start_date_time"
      + " FROM patient_registration pr"
      + " LEFT JOIN doctor_consultation dc ON (dc.patient_id = pr.patient_id"
      + " AND dc.consultation_id::TEXT=?)"
      + " WHERE pr.patient_id=?";
  
  /**
   * Gets end date time of consultation, if end time is not available start date's end of the day
   * will be considered as end time for OP.
   * 
   * @param formParam the form parmateres
   * @return string
   */
  public String getStartTimeOfVisitOrConsultation(FormParameter formParam) {
    return DatabaseHelper.getString(GET_START_DATE_TIME_OF_VISIT_OR_CONS,
        formParam.getId().toString(), formParam.getPatientId());
  }
}
