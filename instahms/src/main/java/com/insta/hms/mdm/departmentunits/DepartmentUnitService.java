package com.insta.hms.mdm.departmentunits;

import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * The Class DepartmentUnitService.
 */
@Component
public class DepartmentUnitService extends MasterService {

  /**
   * Instantiates a new department unit service.
   *
   * @param repo the r
   * @param validator the v
   */
  public DepartmentUnitService(DepartmentUnitRepository repo, DepartmentUnitValidator validator) {
    super(repo, validator);
  }

  /**
   * List all active.
   *
   * @return the list
   */
  public List<BasicDynaBean> listAllActive() {
    return ((DepartmentUnitRepository) this.getRepository()).listAll(null, "status", "A",
        "unit_name");
  }

  /**
   * Gets the department unit by rules.
   *
   * @param deptId the dept id
   * @return the department unit by rules
   */
  public BasicDynaBean getDepartmentUnitByRules(String deptId) {
    return ((DepartmentUnitRepository) this.getRepository()).getDepartmentUnitByRules(deptId);
  }
}
