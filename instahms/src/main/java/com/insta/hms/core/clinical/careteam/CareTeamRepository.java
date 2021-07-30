package com.insta.hms.core.clinical.careteam;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Care team repo.
 * @author anup vishwas
 *
 */

@Repository
public class CareTeamRepository extends GenericRepository {

  public CareTeamRepository() {
    super("visit_care_team");
  }

  private static final String GET_ALL_CARE_TEAM_LIST = " SELECT doc.doctor_id, doc.doctor_name"
      + " From visit_care_team vct" + " JOIN doctors doc ON (vct.care_doctor_id = doc.doctor_id)"
      + " WHERE vct.care_doctor_id = ?";

  protected List<BasicDynaBean> getAllCareTeamList(String doctorId) {

    List<BasicDynaBean> careTeamList =
        DatabaseHelper.queryToDynaList(GET_ALL_CARE_TEAM_LIST, doctorId);
    return careTeamList;
  }

  private static final String CARE_TEAM_VISIT_LIST =
      "SELECT pr.patient_id" + " FROM patient_registration pr"
          + " JOIN visit_care_team vct ON (vct.patient_id = pr.patient_id)"
          + " WHERE pr.mr_no = ? AND vct.care_doctor_id = ?";

  public List<BasicDynaBean> careTeamVisitList(String mrNo, String doctorId) {
    return DatabaseHelper.queryToDynaList(CARE_TEAM_VISIT_LIST, mrNo, doctorId);
  }

  private static final String GET_CARE_TEAM_VISITWISE = " SELECT doc.doctor_id, doc.doctor_name, "
      + " dept.dept_name, dept.dept_id, " + " vct.mod_time, vct.username "
      + " From visit_care_team vct" + " JOIN doctors doc ON (vct.care_doctor_id = doc.doctor_id)"
      + " JOIN department dept ON (doc.dept_id = dept.dept_id)" + " WHERE vct.patient_id = ?";

  public List<BasicDynaBean> careTeamVisitList(String patientId) {
    return DatabaseHelper.queryToDynaList(GET_CARE_TEAM_VISITWISE, patientId);
  }
}

