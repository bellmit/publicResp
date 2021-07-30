package com.insta.hms.mdm.departmenttypes;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class DepartmentTypeRepository.
 */
@Repository
public class DepartmentTypeRepository extends MasterRepository<String> {

  /**
   * Instantiates a new department type repository.
   */
  public DepartmentTypeRepository() {
    super("department_type_master", "dept_type_id", "dept_type_desc");
  }
}
