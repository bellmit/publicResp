/**
 * 
 */

package com.insta.hms.mdm.diagdepartments;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class DiagDepartmentService.
 *
 * @author krishnat
 */
@Service
public class DiagDepartmentService extends MasterService {

  /** The diag department repository. */
  @LazyAutowired
  private DiagDepartmentRepository diagDepartmentRepository;

  /**
   * Instantiates a new diag department service.
   *
   * @param repository
   *          DiagDepartmentRepository
   * @param validator
   *          DiagDepartmentValidator
   */
  public DiagDepartmentService(DiagDepartmentRepository repository,
      DiagDepartmentValidator validator) {
    super(repository, validator);
  }

  /**
   * Gets the active diag departments.
   *
   * @return the active diag departments
   */
  public List<BasicDynaBean> getActiveDiagDepartments() {
    return ((DiagDepartmentRepository) getRepository()).getActiveDiagDepartments();
  }

  /**
   * Gets the diag departments.
   *
   * @return the diag departments
   */
  public List<BasicDynaBean> getDiagDepartments() {
    return ((DiagDepartmentRepository) getRepository()).getDiagDepartments();
  }

  /**
   * Gets the diag departments map.
   *
   * @return the diag departments map
   */
  public Map<String, String> getDiagDepartmentsMap() {
    Map<String, String> deptMaps = new HashMap<>();
    List<BasicDynaBean> list = diagDepartmentRepository.getActiveDiagDepartments();
    for (BasicDynaBean bean : list) {
      deptMaps.put((String) bean.get("dept_name"), (String) bean.get("dept_id"));
    }
    return deptMaps;
  }
}
