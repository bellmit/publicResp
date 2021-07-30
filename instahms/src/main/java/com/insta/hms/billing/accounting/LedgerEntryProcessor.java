package com.insta.hms.billing.accounting;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * The Class LedgerEntryProcessor.
 */
public class LedgerEntryProcessor implements TemplateDirectiveModel {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(LedgerEntryProcessor.class);

  /** The ledger entry map. */
  private Map<String, LedgerEntry> ledgerEntryMap = new LinkedHashMap<String, LedgerEntry>();

  /** The cost center map. */
  private Map<String, Map<String, BigDecimal>> costCenterMap = new LinkedHashMap<String, 
      Map<String, BigDecimal>>();

  /**
   * @see freemarker.template.TemplateDirectiveModel#execute(freemarker.core.Environment,
   *      java.util.Map, freemarker.template.TemplateModel[],
   *      freemarker.template.TemplateDirectiveBody)
   */
  public final void execute(Environment env, Map args, TemplateModel[] loopVars,
      TemplateDirectiveBody body) throws TemplateException, IOException {
    if (null != args && args.size() <= 2) {
      // invalid arguments
      return;
    }
    String accountName = null;
    if (args != null) {
      accountName = args.get("ledgerAccount").toString();
    }
    if (null != accountName) {
      String strAmount = args.get("amount").toString();
      BigDecimal amt = (null != strAmount) ? new BigDecimal(strAmount) : BigDecimal.ZERO;

      logger.debug(accountName + " : " + amt);
      LedgerEntry ledgerEntry = ledgerEntryMap.get(accountName);
      if (null == ledgerEntry) {
        ledgerEntry = new LedgerEntry(args.get("debitOrCredit").toString(), args.get("ledgerType")
            .toString(), accountName);
      }
      if (args.get("referenceName") != null) {
        ledgerEntry.setReferenceName(args.get("referenceName").toString());
        ledgerEntry.setIsNewRef(args.get("isNewRef") == null ? true : new Boolean(args.get(
            "isNewRef").toString()));
      }
      String centerCode = args.get("centerCode") == null ? "" : args.get("centerCode").toString();
      logger.debug("center : " + centerCode);
      if (!centerCode.equals("")) {
        Map<String, BigDecimal> centerAmtMap = costCenterMap.get(accountName);
        if (centerAmtMap == null) {
          centerAmtMap = new HashMap<String, BigDecimal>();
        }
        addAmtToMap(centerAmtMap, centerCode, amt);
        costCenterMap.put(accountName, centerAmtMap);
      }
      logger.debug("Reference: " + ledgerEntry.getReferenceName());

      ledgerEntry.addAmount(amt);
      ledgerEntryMap.put(accountName, ledgerEntry);
    } else {
      logger.error("Missing ledger account name, ignoring");
    }
  }

  /**
   * Adds the amt to map.
   *
   * @param map
   *          the map
   * @param key
   *          the key
   * @param amt
   *          the amt
   */
  private void addAmtToMap(Map<String, BigDecimal> map, String key, BigDecimal amt) {
    if (amt.compareTo(BigDecimal.ZERO) == 0) {
      return;
    }

    BigDecimal val = map.get(key);
    if (val != null) {
      amt = amt.add(val);
    }
    map.put(key, amt);
  }

  /**
   * Gets the cost centers map.
   *
   * @param voucherNo
   *          the voucher no
   * @return the cost centers map
   */
  public Map<String, Map<String, BigDecimal>> getCostCentersMap(String voucherNo) {
    return costCenterMap;
  }

  /**
   * Gets the ledger entries.
   *
   * @param voucherNo
   *          the voucher no
   * @return the ledger entries
   */
  public Set<Map.Entry<String, LedgerEntry>> getLedgerEntries(String voucherNo) {
    return ledgerEntryMap.entrySet();
  }

}