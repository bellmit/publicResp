package com.insta.hms.mdm.formcomponents;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class FormComponentsCenterApplicabilityRepository.
 */
@Repository
public class FormComponentsCenterApplicabilityRepository extends GenericRepository {

  /**
   * Instantiates a new form components center applicability repository.
   */
  public FormComponentsCenterApplicabilityRepository() {
    super("form_components_center_applicability");
  }

  /** The Constant GET_CENTERS. */
  public static final String GET_CENTERS = "SELECT fca.center_id, fca.status, "
      + "hcm.center_name, hcm.city_id, hcm.state_id, "
      + "c.city_name, s.state_name, fca.form_components_center_id  "
      + "FROM form_components_center_applicability  fca  "
      + "LEFT JOIN hospital_center_master hcm ON (hcm.center_id = fca.center_id) "
      + "LEFT JOIN city c ON (c.city_id=hcm.city_id)  "
      + "LEFT JOIN state_master s ON (s.state_id=c.state_id)   "
      + "WHERE fca.form_components_id= ? " + "ORDER BY s.state_name, c.city_name, hcm.center_name";

  /** The Constant GET_ALL_EXCEPT_DEFAULT. */
  public static final String GET_ALL_EXCEPT_DEFAULT = "SELECT * "
      + "FROM form_components_center_applicability "
      + " where form_components_id=? and center_id != 0 ";

  /** The Constant DELETE_ALL_EXCEPT_DEFAULT. */
  public static final String DELETE_ALL_EXCEPT_DEFAULT = "DELETE FROM "
      + "form_components_center_applicability " + "where form_components_id=? and center_id != 0 ";

  /**
   * Gets the all applicable centers.
   *
   * @param formId
   *          the form id
   * @return the all applicable centers
   */
  public List<BasicDynaBean> getAllApplicableCenters(int formId) {

    return DatabaseHelper.queryToDynaList(GET_CENTERS, new Object[] { formId });
  }

  /**
   * Gets the all non default centers.
   *
   * @param formId
   *          the form id
   * @return the all non default centers
   */
  public List<BasicDynaBean> getAllNonDefaultCenters(int formId) {
    return DatabaseHelper.queryToDynaList(GET_ALL_EXCEPT_DEFAULT, new Object[] { formId });
  }

  /**
   * Delete all non default centers.
   *
   * @param formComponentsId
   *          the form components id
   * @return the int
   */
  public int deleteAllNonDefaultCenters(int formComponentsId) {
    return DatabaseHelper.delete(DELETE_ALL_EXCEPT_DEFAULT, new Object[] { formComponentsId });
  }

  private static final String DUPLICATE_FORM_CENTER_APPLICABILITY = "Select Distinct "
      + "fcca.center_id FROM form_components fc "
      + "JOIN form_department_details fdd ON (fdd.id=fc.id AND fdd.dept_id=?) "
      + "JOIN form_components_center_applicability fcca on (fcca.form_components_id=fc.id) "
      + "WHERE fc.doctor_id=? and fc.form_type=? and fc.id !=?";

  /**
   * Gets the non applicable centers. This method returns the centers for which this form cannot be
   * made available.
   *
   * @param doctorId
   *          the doctor id
   * @param deptId
   *          the dept id
   * @param formId
   *          the form id
   * @return the non applicable centers
   */
  public List<BasicDynaBean> getNonApplicableCenters(String doctorId, String deptId,
      String formType, Integer formId) {
    return DatabaseHelper.queryToDynaList(DUPLICATE_FORM_CENTER_APPLICABILITY, deptId, doctorId,
        formType, formId);
  }

}
