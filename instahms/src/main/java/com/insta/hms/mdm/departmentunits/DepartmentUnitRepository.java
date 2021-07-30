package com.insta.hms.mdm.departmentunits;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

/**
 * The Class DepartmentUnitRepository.
 */
@Repository
public class DepartmentUnitRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new department unit repository.
   */
  public DepartmentUnitRepository() {
    super("dept_unit_master", "unit_id");
  }

  /**
   * Gets the department unit by rules.
   *
   * @param deptId the dept id
   * @return the department unit by rules
   */
  public BasicDynaBean getDepartmentUnitByRules(String deptId) {
    try {
      Integer unitId = DatabaseHelper.getInteger("SELECT getUnit(?)", deptId);
      return findByKey("unit_id", unitId);
    } catch (NullPointerException ex) {
      return null;
    }
  }
}
