package com.insta.hms.extension.billing;

import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class AccountingExportVoucherRepository.
 */
@Repository
public class AccountingExportVoucherRepository extends GenericRepository {

  /**
   * Instantiates a new accounting export voucher repository.
   */
  public AccountingExportVoucherRepository() {
    super("accounting_export_voucher");
  }

  /**
   * Find by exported journal.
   *
   * @param exportId
   *          the export id
   * @param journalId
   *          the journal id
   * @return the list
   */
  public List<BasicDynaBean> findByExportedJournal(Integer exportId, Integer journalId) {
    Map keys = new HashMap();
    keys.put("export_id", exportId);
    keys.put("journal_id", journalId);
    return findByCriteria(keys);
  }
}
