package com.insta.hms.extension.billing;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

// TODO: Auto-generated Javadoc
/**
 * The Class ExportAccountNameRepository.
 */
@Repository
public class ExportAccountNameRepository extends GenericRepository {

  /**
   * Instantiates a new export account name repository.
   */
  public ExportAccountNameRepository() {
    super("export_account_names");
  }

}
