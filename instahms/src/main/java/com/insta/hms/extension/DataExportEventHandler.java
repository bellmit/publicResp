package com.insta.hms.extension;

import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Interface DataExportEventHandler.
 */
public interface DataExportEventHandler {

  /**
   * Start export.
   *
   * @return the integer
   */
  public Integer startExport();

  /**
   * End export.
   *
   * @param exportId
   *          the export id
   */
  public void endExport(Integer exportId);

  /**
   * Start journal.
   *
   * @param exportId
   *          the export id
   * @param current
   *          the current
   * @return the integer
   */
  public Integer startJournal(Integer exportId, Map<String, Object> current);

  /**
   * End journal.
   *
   * @param exportId
   *          the export id
   * @param journalId
   *          the journal id
   * @param prev
   *          the prev
   */
  public void endJournal(Integer exportId, Integer journalId, Map<String, Object> prev);

  /**
   * Start voucher.
   *
   * @param exportId
   *          the export id
   * @param journalId
   *          the journal id
   * @param prev
   *          the prev
   * @return the string
   */
  public String startVoucher(Integer exportId, Integer journalId, Map<String, Object> prev);

  /**
   * End voucher.
   *
   * @param exportId
   *          the export id
   * @param journalId
   *          the journal id
   * @param voucherId
   *          the voucher id
   * @param prev
   *          the prev
   */
  public void endVoucher(Integer exportId, Integer journalId, String voucherId,
      Map<String, Object> prev);

  /**
   * Post success.
   *
   * @param exportId
   *          the export id
   * @param journalId
   *          the journal id
   */
  public void postSuccess(Integer exportId, Integer journalId);

  /**
   * Post error.
   *
   * @param exportId
   *          the export id
   * @param journalId
   *          the journal id
   * @param error
   *          the error
   */
  public void postError(Integer exportId, Integer journalId, Exception error);

}
