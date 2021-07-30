/**
 * 
 */

package com.insta.hms.mdm.diagdepartments;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class DiagDepartmentRepository.
 *
 * @author krishnat
 */
@Repository
public class DiagDepartmentRepository extends MasterRepository<String> {

  /**
   * Instantiates a new diag department repository.
   */
  public DiagDepartmentRepository() {
    super("diagnostics_departments", "ddept_id", "ddept_name");
  }

  /**
   * Gets the active diag departments.
   *
   * @return the active diag departments
   */
  public List<BasicDynaBean> getActiveDiagDepartments() {
    return DatabaseHelper.queryToDynaList("select ddept_id as dept_id, ddept_name as dept_name from"
        + " diagnostics_departments where status='A' order by ddept_name");
  }

  /** The Constant GET_DIAG_DEPTS. */
  public static final String GET_DIAG_DEPTS = " SELECT DDEPT_ID, DDEPT_NAME || "
      + "'(' || d.dept_name || ')' as DDEPT_NAME "
      + " FROM DIAGNOSTICS_DEPARTMENTS dd join department d on(d.dept_id = dd.category) "
      + " WHERE dd.STATUS='A' ORDER BY dd.category ";

  /**
   * Gets the diag departments.
   *
   * @return the diag departments
   */
  public List<BasicDynaBean> getDiagDepartments() {
    return DatabaseHelper.queryToDynaList(GET_DIAG_DEPTS);
  }
}
