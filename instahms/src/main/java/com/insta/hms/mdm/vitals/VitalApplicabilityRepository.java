package com.insta.hms.mdm.vitals;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/** The Class VitalApplicabilityRepository. */
@Repository
public class VitalApplicabilityRepository extends MasterRepository<Integer> {

  /** Instantiates a new vital applicability repository. */
  public VitalApplicabilityRepository() {
    super("vitals_default_details", "vital_default_id");
  }

  /** The Constant GET_VITAL_APPLICABILITY_FOR_DEPARTMENT. */
  private static final String GET_VITAL_APPLICABILITY_FOR_DEPARTMENT = "SELECT "
      + "vdd.center_id, vdd.param_id, vdd.mandatory, vdd.vital_default_id "
      + "FROM vitals_default_details vdd "
      + "JOIN hospital_center_master hcm USING (center_id) "
      + "WHERE dept_id = ? AND hcm.status='A' AND vdd.visit_type= ? "
      + "AND #CENTERFILTER# ORDER BY center_id";


  /**
   * Gets the vital applicability for department.
   *
   * @param departmentId the department id
   * @param visitType the visit type
   * @param centerId the center id
   * @param maxCenterIncDefault the max center inc default
   * @return the vital applicability for department
   */
  public List<BasicDynaBean> getVitalApplicabilityForDepartment(String departmentId,
      String visitType, Integer centerId, int maxCenterIncDefault) {
    String query = GET_VITAL_APPLICABILITY_FOR_DEPARTMENT;
    if (maxCenterIncDefault > 1) {
      if (centerId > 0) {
        query = query.replace("#CENTERFILTER#", "hcm.center_id = ?");
      } else {
        query = query.replace("#CENTERFILTER#", "hcm.center_id != 0");
      }
    } else {
      query = query.replace("#CENTERFILTER#", "true");      
    }
    if (centerId > 0) {
      return DatabaseHelper.queryToDynaList(query,
          new Object[] { departmentId,visitType,centerId });
    } else {
      return DatabaseHelper.queryToDynaList(query,
          new Object[] { departmentId,visitType });
    }
  }

  /** The Constant GET_VITAL_APPLICABILITY_FOR_CENTER. */
  private static final String GET_VITAL_APPLICABILITY_FOR_CENTER = "SELECT "
      + "vdd.dept_id, vdd.param_id, vdd,mandatory, vdd.vital_default_id "
      + "FROM vitals_default_details vdd "
      + "JOIN department d USING (dept_id) "
      + "WHERE center_id = ? AND d.status='A' AND coalesce(d.dept_type_id,'') != 'NOCL' "
      + "AND vdd.visit_type=? ORDER BY dept_id";


  /**
   * Gets the vital applicability for center.
   *
   * @param centerId the center id
   * @param visitType the visit type
   * @return the vital applicability for center
   */
  public List<BasicDynaBean> getVitalApplicabilityForCenter(Integer centerId, String visitType) {
    return DatabaseHelper.queryToDynaList(GET_VITAL_APPLICABILITY_FOR_CENTER,
        new Object[] { centerId,visitType });
  }
}
