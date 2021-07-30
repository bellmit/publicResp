package com.insta.hms.extension.billing;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

// TODO: Auto-generated Javadoc
/**
 * The Class AccountingExportJournalRepository.
 */
@Repository
public class AccountingExportJournalRepository extends GenericRepository {

  /**
   * Instantiates a new accounting export journal repository.
   */
  public AccountingExportJournalRepository() {
    super("accounting_export_journal");
  }

}
