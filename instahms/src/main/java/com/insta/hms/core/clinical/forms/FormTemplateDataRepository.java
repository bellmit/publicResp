package com.insta.hms.core.clinical.forms;

import com.insta.hms.common.GenericRepository;
import org.springframework.stereotype.Repository;

/**
 * The Class FormTemplateDataRepository.
 */
@Repository
public class FormTemplateDataRepository extends GenericRepository {

  /**
   * Instantiates a new form template data repository.
   */
  public FormTemplateDataRepository() {
    super("form_template_data");
  }

}
