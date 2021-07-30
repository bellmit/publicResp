package com.insta.hms.insurance;

import org.apache.commons.beanutils.BasicDynaBean;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class XLBillRemittanceProcessor.
 */
public class XLBillRemittanceProcessor extends XLRemittanceProcessor {

  /**
   * Instantiates a new XL bill remittance processor.
   *
   * @param columnMap the column map
   */
  public XLBillRemittanceProcessor(Map<String, String> columnMap) {
    super(columnMap);
  }

  /**
   * Instantiates a new XL bill remittance processor.
   */
  public XLBillRemittanceProcessor() {
    super();
  }

  /**
   * get xl remittance provider.
   *
   * @return the XL remittance provider
   */
  public XLRemittanceProvider getXLRemittanceProvider() {
    return new XLBillRemittanceProvider();
  }

  /**
   * Get default col map.
   *
   * @param remittanceBean the remittance bean
   * @return the default column map
   */
  @Override
  public Map<String, String> getDefaultColumnMap(BasicDynaBean remittanceBean) {
    Map<String, String> columnMap = new HashMap<String, String>();
    columnMap.putAll(super.getDefaultColumnMap(remittanceBean));
    columnMap.put("item_identification", "B");
    columnMap.put("bill_no_heading", "bill_no");
    return columnMap;
  }
}
