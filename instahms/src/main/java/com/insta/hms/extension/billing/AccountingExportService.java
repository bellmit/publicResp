package com.insta.hms.extension.billing;

import com.insta.hms.extension.DataExportEventHandler;
import com.insta.hms.extension.DataExportService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class AccountingExportService.
 *
 * @author anupama
 * @param <S>
 *          the generic type
 * @param <A>
 *          Target specific object representing the account name
 * @param <J>
 *          Target specific object representing the journal / voucher
 * 
 *          Class that abstracts out the voucher processing strategy so that each target system
 *          implementation need not repeat the same. This class should not have any target
 *          accounting system specific API / object references here.
 */
public abstract class AccountingExportService<S, A, J> implements DataExportService {

  /** The supported targets. */
  List<String> supportedTargets = new ArrayList<String>();

  /** The event handler. */
  private DataExportEventHandler eventHandler;

  /**
   * Instantiates a new accounting export service.
   *
   * @param supportedTargets
   *          the supported targets
   */
  public AccountingExportService(String[] supportedTargets) {
    if (null != supportedTargets) {
      this.supportedTargets.addAll(Arrays.asList(supportedTargets));
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.extension.DataExportService#setExportEventHandler(com.insta.hms.extension.
   * DataExportEventHandler)
   */
  @Override
  public void setExportEventHandler(DataExportEventHandler handler) {
    this.eventHandler = handler;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.extension.DataExportService#supportsTarget(java.lang.String)
   */
  @Override
  public boolean supportsTarget(String target) {
    return supportedTargets.contains(target);
  }

  /**
   * Initialize.
   *
   * @param target
   *          the target
   * @param centerId
   *          the center id
   * @param accountGroupId
   *          the account group id
   * @return the s
   * @throws Exception
   *           the exception
   */
  protected abstract S initialize(String target, Integer centerId, Integer accountGroupId)
      throws Exception;

  /**
   * Sync account names.
   *
   * @param service
   *          the service
   * @return the list
   * @throws Exception
   *           the exception
   */
  protected abstract List<A> syncAccountNames(S service) throws Exception;

  /**
   * Process voucher.
   *
   * @param current
   *          the current
   * @param credit
   *          the credit
   * @param debit
   *          the debit
   */
  protected void processVoucher(Map<String, Object> current, Map<String, BigDecimal> credit,
      Map<String, BigDecimal> debit) {

    if (null == current) {
      return;
    }
    
    Integer status = (Integer) current.get("update_status");
    if (status == -1 || status == 0) { // errored out earlier or not exported at all
      String creditAccount = (String) current.get("credit_account");
      String debitAccount = (String) current.get("debit_account");
      if (null != credit.get(creditAccount)) {
        credit.put(creditAccount, ((BigDecimal) 
            current.get("net_amount")).add(credit.get(creditAccount)));
      } else {
        credit.put(creditAccount, (BigDecimal) current.get("net_amount"));
      }
      if (null != debit.get(debitAccount)) {
        debit.put(debitAccount, ((BigDecimal) 
            current.get("net_amount")).add(debit.get(debitAccount)));
      } else {
        debit.put(debitAccount, (BigDecimal) current.get("net_amount"));
      }
    }
  }

  /**
   * Post voucher.
   *
   * @param service
   *          the service
   * @param accountList
   *          the account list
   * @param beanMap
   *          the bean map
   * @param credit
   *          the credit
   * @param debit
   *          the debit
   * @return the j
   * @throws Exception
   *           the exception
   */
  protected abstract J postVoucher(S service, List<A> accountList, Map<String, Object> beanMap,
      Map<String, BigDecimal> credit, Map<String, BigDecimal> debit) throws Exception;

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.extension.DataExportService#export(java.util.Map, java.lang.Integer,
   * java.lang.Integer)
   */
  @Override
  public Integer export(Map<String, Map<String, Object>> recordMap, Integer centerId,
      Integer accountGroupId) throws Exception {
    Map<String, Object> prev = null;
    Map<String, BigDecimal> credit = new HashMap<String, BigDecimal>();
    Map<String, BigDecimal> debit = new HashMap<String, BigDecimal>();
    S service = null;
    // Initialize all targets

    for (String target : supportedTargets) {
      service = initialize(target, centerId, accountGroupId);
    }

    // Sync account names
    List<A> accountList = syncAccountNames(service);

    // Typically this strategy of accumulating vouchers should be delegated to the respective
    // services. However, it is here to avoid each of the subclasses using a different strategy
    // right now. When there are more accounting services implemented, then we will refactor and
    // abstract out the strategy more appropriately
    Integer exportId = 0;
    Integer journalId = 0;
    String voucherId = null;

    exportId = eventHandler.startExport();
    if (null != recordMap) {

      Iterator<Map.Entry<String, Map<String, Object>>> iterator = recordMap.entrySet().iterator();
      int mapSize = recordMap.size(); //

      // We need to iterate "once more" than the size of the map, since we actually
      // post the voucher at the beginning of the next iteration. without the +1 in the
      // for loop boundary check, the last journal will not be posted to accounting system
      // although, all the vouchers for it would have been processed

      for (int currentIteration = 0; mapSize > 0
          && currentIteration < mapSize + 1; currentIteration++) {

        Map<String, Object> current = iterator.hasNext() ? iterator.next().getValue() : null;

        if (isNewJournal(current, prev)) {
          // Create a journal entry for the completed voucher
          if (null != prev) {
            eventHandler.endJournal(exportId, journalId, prev);
            try {
              J journal = postVoucher(service, accountList, prev, credit, debit);
              if (null != journal) {
                eventHandler.postSuccess(exportId, journalId);
              }
            } catch (Exception ex) {
              ex.printStackTrace();
              eventHandler.postError(exportId, journalId, ex);
            }
          }
          credit.clear();
          debit.clear();
          journalId = 0;
        }

        if (isVoucherOpen(current)) {
          if (null == journalId || journalId == 0) {
            journalId = eventHandler.startJournal(exportId, current);
          }
          voucherId = eventHandler.startVoucher(exportId, journalId, current);
          processVoucher(current, credit, debit);
          eventHandler.endVoucher(exportId, journalId, voucherId, current);
        }
        prev = current;
      }
    }
    eventHandler.endExport(exportId);
    return exportId;
  }

  /**
   * Checks if is voucher open.
   *
   * @param current
   *          the current
   * @return true, if is voucher open
   */
  private boolean isVoucherOpen(Map<String, Object> current) {
    if (null != current) {
      Integer status = (Integer) current.get("update_status");
      if (null != status && (status == 0 || status == -1)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks if is new journal.
   *
   * @param current
   *          the current
   * @param prev
   *          the prev
   * @return true, if is new journal
   */
  private boolean isNewJournal(Map<String, Object> current, Map<String, Object> prev) {

    if (null == prev && null != current) {
      return true; // first journal is always considered new
    } 
    if (null == current && null != prev) {
      return true; // last journal is always considered new
    } 
    // we consider the current different from the prev, if at least one of date, type and # are
    // different

    // Some voucher numbers can be null. Empty string instead of a null string
    String currVoucherNo = (String) current.get("voucher_no");
    String prevVoucherNo = (String) prev.get("voucher_no");

    if (null == currVoucherNo) {
      currVoucherNo = "";
    }
    if (null == prevVoucherNo) {
      prevVoucherNo = "";
    }
    if (null != prev && null != current
        && current.get("voucher_date").equals(prev.get("voucher_date"))
        && current.get("voucher_type").equals(prev.get("voucher_type"))
        && currVoucherNo.equals(prevVoucherNo)) {
      return false;
    } 
    return true;
  }

}
