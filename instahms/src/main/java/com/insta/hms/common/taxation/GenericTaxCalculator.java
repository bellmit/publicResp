package com.insta.hms.common.taxation;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.itemsubgroupstaxdetails.ItemSubgroupTaxDetailsService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class GenericTaxCalculator.
 *
 * @author irshadmohammed
 */
@Service
public class GenericTaxCalculator extends BaseTaxCalculator {

  /** The item subgroup tax details service. */
  @LazyAutowired
  ItemSubgroupTaxDetailsService itemSubgroupTaxDetailsService;

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.common.taxation.BaseTaxCalculator#getTaxParameters(java.util.
   * Map, com.insta.hms.common.taxation.TaxContext)
   */
  protected BasicDynaBean getTaxParameters(Map<String, Object> subGroupMap, TaxContext taxContext) {
    BasicDynaBean taxParameter = itemSubgroupTaxDetailsService.findByPk(subGroupMap);
    return taxParameter;
  }

}
