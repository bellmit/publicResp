package com.insta.hms.core.clinical.forms;

import com.insta.hms.common.GenericRepository;
import org.springframework.stereotype.Repository;

/**
 * The Class FormVitalParametersRepository.
 *
 * @author teja
 */
@Repository
public class FormVitalParametersRepository extends GenericRepository {

  /**
   * Instantiates a new form vital parameters repository.
   */
  public FormVitalParametersRepository() {
    super("form_vital_parameters");
  }

}
