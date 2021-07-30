package com.insta.hms.extension.billing;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

// TODO: Auto-generated Javadoc
/**
 * The Class AccountingExportLogRepository.
 */
@Repository
public class AccountingExportLogRepository extends GenericRepository {

  /**
   * Instantiates a new accounting export log repository.
   */
  public AccountingExportLogRepository() {
    super("accounting_export_log");
  }
}
