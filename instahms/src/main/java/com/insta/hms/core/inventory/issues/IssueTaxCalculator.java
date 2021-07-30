package com.insta.hms.core.inventory.issues;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.taxation.GenericTaxCalculator;
import com.insta.hms.common.taxation.ItemTaxDetails;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The Class IssueTaxCalculator.
 *
 * @author irshadmohammed
 */
public abstract class IssueTaxCalculator extends GenericTaxCalculator {

  /** The supported groups. */
  List<String> supportedGroups = new ArrayList<String>();

  /**
   * Instantiates a new issue tax calculator.
   *
   * @param supportedGroups the supported groups
   */
  protected IssueTaxCalculator(String[] supportedGroups) {
    this.supportedGroups.addAll(Arrays.asList(supportedGroups));
  }

  /**
   * Gets the supported groups.
   *
   * @return the supported groups
   */
  public String[] getSupportedGroups() {
    if (null == supportedGroups || supportedGroups.isEmpty()) {
      return null;
    }
    return supportedGroups.toArray(new String[0]);
  }

  /* (non-Javadoc)
   * @see com.insta.hms.common.taxation.BaseTaxCalculator#getDiscount
   * (com.insta.hms.common.taxation.ItemTaxDetails)
   */
  @Override
  protected BigDecimal getDiscount(ItemTaxDetails itemTaxDetails) {

    BigDecimal discAmount = (null != itemTaxDetails.getDiscount()) ? itemTaxDetails.getDiscount()
        : BigDecimal.ZERO;
    if (null == itemTaxDetails.getDiscount()
        || BigDecimal.ZERO.compareTo(itemTaxDetails.getDiscount()) == 0) {
      // what comes from the item details is discount percentage and not discount amount.
      BigDecimal discPer = itemTaxDetails.getDiscountPercent();
      if (null == discPer || BigDecimal.ZERO.compareTo(discPer) == 0) {
        return BigDecimal.ZERO;
      }

      BigDecimal basePrice = getDiscountBasePrice(itemTaxDetails);
      discAmount = ConversionUtils.setScale(basePrice.multiply(getNetQty(itemTaxDetails))
          .multiply(discPer).divide(BigDecimal.valueOf(100)));
    }
    return discAmount;

  }

  /**
   * Gets the discount base price.
   *
   * @param itemTaxDetails the item tax details
   * @return the discount base price
   */
  protected abstract BigDecimal getDiscountBasePrice(ItemTaxDetails itemTaxDetails);
}
