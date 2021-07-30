package com.insta.hms.core.clinical.forms;

import com.insta.hms.common.GenericRepository;
import org.springframework.stereotype.Repository;

/**
 * The Class FormDataRepository.
 */
@Repository
public class FormDataRepository extends GenericRepository {

  /**
   * Instantiates a new form data repository.
   */
  public FormDataRepository() {
    super("form_data");
  }

}
