package com.insta.hms.mdm.formcomponents;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class FormDepartmentDetailsRepository.
 */
@Repository
public class FormDepartmentDetailsRepository extends GenericRepository {

  /**
   * Instantiates a new form department details repository.
   */
  public FormDepartmentDetailsRepository() {
    super("form_department_details");
  }

}
