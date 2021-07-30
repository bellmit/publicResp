package com.insta.hms.insurance;

import org.apache.commons.beanutils.BasicDynaBean;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class XLItemRemittanceProcessor.
 */
public class XLItemRemittanceProcessor extends XLRemittanceProcessor {

  /**
   * Instantiates a new XL item remittance processor.
   *
   * @param columnMap the column map
   */
  public XLItemRemittanceProcessor(Map<String, String> columnMap) {
    super(columnMap);
  }

  /**
   * Instantiates a new XL item remittance processor.
   */
  public XLItemRemittanceProcessor() {
    super();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.insurance.XLRemittanceProcessor#getXLRemittanceProvider()
   */
  public XLRemittanceProvider getXLRemittanceProvider() {
    return new XLItemRemittanceProvider();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.insurance.XLRemittanceProcessor
   * #getDefaultColumnMap(org.apache.commons.beanutils.BasicDynaBean)
   */
  @Override
  public Map<String, String> getDefaultColumnMap(BasicDynaBean remittanceBean) {
    Map<String, String> columnMap = new HashMap<String, String>();
    columnMap.putAll(super.getDefaultColumnMap(remittanceBean));
    columnMap.put("item_identification", "ActivityId");
    columnMap.put("item_id_heading", "Activity Charge Id");

    // These are applicable only if the item_identification is not ActivityId

    /*
     * columnMap.put("bill_no_heading", "bill_no"); columnMap.put("service_name_heading",
     * "service_name"); columnMap.put("service_posted_date_heading", "posted_date");
     * columnMap.put("charge_insurance_claim_amount_heading", "claim_amount");
     */
    return columnMap;
  }
}
