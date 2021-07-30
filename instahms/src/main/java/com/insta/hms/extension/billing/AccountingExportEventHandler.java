package com.insta.hms.extension.billing;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.extension.DataExportEventHandler;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class AccountingExportEventHandler.
 */
@Service
public class AccountingExportEventHandler implements DataExportEventHandler {

  /** The service. */
  @LazyAutowired
  private AccountingLoggerService service;

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.extension.DataExportEventHandler#startExport()
   */
  @Override
  public Integer startExport() {
    BasicDynaBean bean = service.createExportLog();
    return (Integer) bean.get("export_id");
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.extension.DataExportEventHandler#endExport(java.lang.Integer)
   */
  @Override
  public void endExport(Integer exportId) {
    // Nothing to do
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.extension.DataExportEventHandler#startVoucher(java.lang.Integer,
   * java.lang.Integer, java.util.Map)
   */
  @Override
  public String startVoucher(Integer exportId, Integer journalId, Map<String, Object> current) {
    BasicDynaBean bean = service.createVoucherLog(exportId, journalId, current);
    return (String) bean.get("guid");
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.extension.DataExportEventHandler#endVoucher(java.lang.Integer,
   * java.lang.Integer, java.lang.String, java.util.Map)
   */
  @Override
  public void endVoucher(Integer exportId, Integer journalId, String voucherId,
      Map<String, Object> prev) {
    // Nothing to do
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.extension.DataExportEventHandler#startJournal(java.lang.Integer,
   * java.util.Map)
   */
  @Override
  public Integer startJournal(Integer exportId, Map<String, Object> current) {
    BasicDynaBean bean = service.createJournalLog(exportId, current);
    return (Integer) bean.get("journal_id");
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.extension.DataExportEventHandler#endJournal(java.lang.Integer,
   * java.lang.Integer, java.util.Map)
   */
  @Override
  public void endJournal(Integer exportId, Integer journalId, Map<String, Object> prev) {
    // Nohing to do
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.extension.DataExportEventHandler#postSuccess(java.lang.Integer,
   * java.lang.Integer)
   */
  @Override
  public void postSuccess(Integer exportId, Integer journalId) {
    BasicDynaBean bean = service.updateJournalLog(exportId, journalId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.extension.DataExportEventHandler#postError(java.lang.Integer,
   * java.lang.Integer, java.lang.Exception)
   */
  @Override
  public void postError(Integer exportId, Integer journalId, Exception error) {
    BasicDynaBean bean = service.updateJournalLog(exportId, journalId, error);
  }
}