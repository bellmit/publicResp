package com.insta.hms.common.taxation;

import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Interface TaxCalculator.
 *
 * @author irshadmohammed
 */
public interface TaxCalculator {

  // TODO - We will remove this as apart of cleanup
  // public Map<String, Object> calculateTaxes(ItemTaxDetails taxBean,
  // List<BasicDynaBean> subgroupCodes, TaxContext txnContext) throws
  // SQLException;

  /**
   * Calculate taxes.
   *
   * @param taxBean
   *          the tax bean
   * @param txnContext
   *          the txn context
   * @return the map
   */
  public Map<Integer, Object> calculateTaxes(ItemTaxDetails taxBean, TaxContext txnContext);

  // public List<BasicDynaBean> getTaxRates(List<BasicDynaBean> subgroupCodes,
  // TaxContext taxContext) throws SQLException;
}
