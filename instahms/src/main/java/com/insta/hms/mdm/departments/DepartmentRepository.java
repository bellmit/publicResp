package com.insta.hms.mdm.departments;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;
import com.insta.hms.mdm.SearchQuery;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class DepartmentRepository.
 */
@Repository
public class DepartmentRepository extends MasterRepository<String> {

  /**
   * Instantiates a new department repository.
   */
  public DepartmentRepository() {
    super("department", "dept_id", "dept_name");
  }

  /** The Constant DEPARTMENT_SEARCH_TABLES. */
  private static final String DEPARTMENT_SEARCH_TABLES = " FROM (SELECT dep.dept_id, "
      + " dep.dept_name,dep.is_referral_doc_as_ordering_clinician , "
      + " dep.status,dep.allowed_gender, dep.cost_center_code, "
      + " dep.dept_type_id, dt.dept_type_desc FROM department dep "
      + " LEFT JOIN department_type_master dt ON (dep.dept_type_id=dt.dept_type_id)) AS FOO ";

  /* (non-Javadoc)
   * @see com.insta.hms.mdm.MasterRepository#getSearchQuery()
   */
  @Override
  public SearchQuery getSearchQuery() {
    return new SearchQuery(DEPARTMENT_SEARCH_TABLES);
  }

  /** The Constant GET_NONCLINICAL_DEPARTMENTS. */
  public static final String GET_NONCLINICAL_DEPARTMENTS = "SELECT * FROM department "
      + " WHERE status='A' AND (coalesce(dept_type_id,'') = '' OR dept_type_id != 'NOCL') "
      + " ORDER BY dept_name ";

  /**
   * Gets the non clinical departments.
   *
   * @return the non clinical departments
   */
  public List<BasicDynaBean> getNonClinicalDepartments() {
    return DatabaseHelper.queryToDynaList(GET_NONCLINICAL_DEPARTMENTS);
  }
  
  /** The Constant GET_ALL_DEPARTMENTS. */
  private static final String GET_ALL_DEPARTMENTS = "Select * from department @ order by dept_name";

  /**
   * Gets the all departments.
   *
   * @param sendOnlyActiveData the send only active data
   * @return the all departments
   */
  public List<BasicDynaBean> getAllDepartments(boolean sendOnlyActiveData) {
    String query = GET_ALL_DEPARTMENTS;
    if (sendOnlyActiveData) {
      query = query.replace("@", "where status = 'A' ");
    } else {
      query = query.replace("@", "");
    }
    return DatabaseHelper.queryToDynaList(query);
  }

  /** The Constant PRESC_DEPARTMENT. */
  private static final String PRESC_DEPARTMENT = " SELECT dept_name as item_name,"
      + " dept_id as item_id, 'Doctor' AS item_type, 'DEPT' as presc_activity_type,"
      + " 0 as charge, 'N' as category_payable, 0 as discount, 0 as insurance_category_id,"
      + " '' as insurance_category_name, false as applicable"
      + " FROM department WHERE status='A' AND (dept_name ilike ? OR dept_name ilike ?) "
      + " ORDER BY dept_name " + " LIMIT ?";

  /**
   * Gets the departments for prescription.
   *
   * @param searchQuery the search query
   * @param itemLimit the item limit
   * @return the departments for prescription
   */
  public List<BasicDynaBean> getDepartmentsForPrescription(String searchQuery, Integer itemLimit) {
    return DatabaseHelper.queryToDynaList(PRESC_DEPARTMENT, new Object[] {searchQuery + "%",
        "% " + searchQuery + "%", itemLimit});
  }

}
