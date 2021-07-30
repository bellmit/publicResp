package com.insta.hms.billing.accounting;

import freemarker.core.Environment;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * The Class VoucherProcessor.
 */
public class VoucherProcessor implements TemplateDirectiveModel {

  /** The voucher map. */
  private Map<String, String> voucherMap = new LinkedHashMap<String, String>();

  /** The ledger processor. */
  private LedgerEntryProcessor ledgerProcessor = new LedgerEntryProcessor();

  /** The voucher no. */
  private String voucherNo = null;

  /** The aggregating. */
  private boolean aggregating = false;

  /**
   * Instantiates a new voucher processor.
   *
   * @param voucherNo
   *          the voucher no
   */
  public VoucherProcessor(String voucherNo) {
    this.voucherNo = voucherNo;
  }

  /**
   * Gets the ledger entries.
   *
   * @param voucherNo
   *          the voucher no
   * @return the ledger entries
   */
  public Set<Map.Entry<String, LedgerEntry>> getLedgerEntries(String voucherNo) {
    return ledgerProcessor.getLedgerEntries(voucherNo);
  }

  /**
   * Gets the cost centers map.
   *
   * @param voucherNo
   *          the voucher no
   * @return the cost centers map
   */
  public Map<String, Map<String, BigDecimal>> getCostCentersMap(String voucherNo) {
    return ledgerProcessor.getCostCentersMap(voucherNo);
  }

  /**
   * @see freemarker.template.TemplateDirectiveModel#execute(freemarker.core.Environment,
   *      java.util.Map, freemarker.template.TemplateModel[],
   *      freemarker.template.TemplateDirectiveBody)
   */
  public void execute(Environment env, Map args, TemplateModel[] loopVars,
      TemplateDirectiveBody body) throws TemplateException, IOException {
    String narration = args.get("narration").toString();
    if (null != narration) {
      voucherMap.put(voucherNo, narration);
    }
    String level = isAggregating() ? "I" : "V";
    if (loopVars.length > 0) {
      loopVars[0] = new SimpleScalar(level);
    }
    if (!isAggregating()) {
      aggregating = true;
    }
    if (null != body) {
      body.render(env.getOut());
    }
  }

  /**
   * Gets the ledger entry processor.
   *
   * @return the ledger entry processor
   */
  public LedgerEntryProcessor getLedgerEntryProcessor() {
    return ledgerProcessor;
  }

  /**
   * Checks if is aggregating.
   *
   * @return true, if is aggregating
   */
  protected boolean isAggregating() {
    return aggregating;
  }

  /**
   * Gets the narration.
   *
   * @param voucherNo
   *          the voucher no
   * @return the narration
   */
  public final String getNarration(String voucherNo) {
    return voucherMap.get(voucherNo);
  }

}