package com.insta.hms.extension.billing;

import com.insta.hms.common.BusinessService;
import com.insta.hms.common.DateHelper;
import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class AccountingLoggerService.
 */
@Service
public class AccountingLoggerService extends BusinessService {

  /** The Constant JOURNAL_STATUS_SUCCESS. */
  private static final Integer JOURNAL_STATUS_SUCCESS = 1;

  /** The Constant JOURNAL_STATUS_FAILED. */
  private static final Integer JOURNAL_STATUS_FAILED = -1;

  /** The log repo. */
  @LazyAutowired
  AccountingExportLogRepository logRepo;

  /** The journal repo. */
  @LazyAutowired
  AccountingExportJournalRepository journalRepo;

  /** The voucher repo. */
  @LazyAutowired
  AccountingExportVoucherRepository voucherRepo;

  /**
   * Creates the export log.
   *
   * @return the basic dyna bean
   */
  public BasicDynaBean createExportLog() {
    BasicDynaBean bean = logRepo.getBean();
    bean.set("export_id", logRepo.getNextSequence());
    bean.set("export_date", new Timestamp(DateHelper.getCurrentDate().getMillis()));
    logRepo.insert(bean);
    return bean;
  }

  /**
   * Creates the journal log.
   *
   * @param exportId
   *          the export id
   * @param current
   *          the current
   * @return the basic dyna bean
   */
  public BasicDynaBean createJournalLog(Integer exportId, Map<String, Object> current) {
    BasicDynaBean bean = null;
    if (current != null) {
      bean = journalRepo.getBean();
      bean.set("export_id", exportId);
      bean.set("journal_id", journalRepo.getNextSequence());
      bean.set("journal_date", new Timestamp(DateHelper.getCurrentDate().getMillis()));
      bean.set("status", new Integer(0));
      journalRepo.insert(bean);
    }
    return bean;
  }

  /**
   * Creates the voucher log.
   *
   * @param exportId
   *          the export id
   * @param journalId
   *          the journal id
   * @param current
   *          the current
   * @return the basic dyna bean
   */
  public BasicDynaBean createVoucherLog(Integer exportId, Integer journalId,
      Map<String, Object> current) {
    BasicDynaBean bean = null;
    if (null != current) {
      bean = voucherRepo.getBean();
      bean.set("export_id", exportId);
      bean.set("journal_id", journalId);
      bean.set("guid", current.get("guid"));
      bean.set("voucher_log_id", voucherRepo.getNextSequence());
      bean.set("status", new Integer(0));
      voucherRepo.insert(bean);
    }
    return bean;
  }

  /**
   * Update journal log.
   *
   * @param exportId
   *          the export id
   * @param journalId
   *          the journal id
   * @return the basic dyna bean
   */
  public BasicDynaBean updateJournalLog(Integer exportId, Integer journalId) {
    return updateJournalLog(exportId, journalId, JOURNAL_STATUS_SUCCESS, "Successfully Exported");
  }

  /**
   * Update journal log.
   *
   * @param exportId
   *          the export id
   * @param journalId
   *          the journal id
   * @param error
   *          the error
   * @return the basic dyna bean
   */
  public BasicDynaBean updateJournalLog(Integer exportId, Integer journalId, Exception error) {
    return updateJournalLog(exportId, journalId, JOURNAL_STATUS_FAILED, error.getMessage());
  }

  /**
   * Update journal log.
   *
   * @param exportId
   *          the export id
   * @param journalId
   *          the journal id
   * @param status
   *          the status
   * @param message
   *          the message
   * @return the basic dyna bean
   */
  private BasicDynaBean updateJournalLog(Integer exportId, Integer journalId, Integer status,
      String message) {
    Map keys = new HashMap();
    keys.put("export_id", exportId);
    keys.put("journal_id", journalId);

    BasicDynaBean bean = journalRepo.findByKey(keys);
    if (null != bean) {
      bean.set("status", status);
      bean.set("status_message", message);
      journalRepo.update(bean, keys);
    }

    List<BasicDynaBean> exportVouchers = voucherRepo.findByExportedJournal(exportId, journalId);
    if (null != exportVouchers && exportVouchers.size() > 0) {
      List<Integer> values = new ArrayList<Integer>(exportVouchers.size());
      for (int i = 0; i < exportVouchers.size(); i++) {
        exportVouchers.get(i).set("status", status);
        values.add((Integer) exportVouchers.get(i).get("voucher_log_id"));
      }
      Map<String, Object> valueMap = new HashMap<String, Object>();
      valueMap.put("voucher_log_id", values);
      voucherRepo.batchUpdate(exportVouchers, valueMap);
    }

    return bean;
  }

}
