package com.insta.hms.core.inventory.sales;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.taxation.GenericTaxCalculator;
import com.insta.hms.common.taxation.ItemTaxDetails;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class SalesTaxCalculator extends GenericTaxCalculator {
	
	List<String> supportedGroups = new ArrayList<String>();
	
	protected SalesTaxCalculator(String[] supportedGroups) {
		this.supportedGroups.addAll(Arrays.asList(supportedGroups));
	}

	public String[] getSupportedGroups() {
		if (null == supportedGroups || supportedGroups.isEmpty()) {
			return null;
		}
		return supportedGroups.toArray(new String[0]);
	}

	@Override
	protected BigDecimal getDiscount(ItemTaxDetails itemTaxDetails) {

		BigDecimal discAmount = (null != itemTaxDetails.getDiscount()) ? itemTaxDetails.getDiscount() : BigDecimal.ZERO;
		if (null == itemTaxDetails.getDiscount() || 
				BigDecimal.ZERO.compareTo(itemTaxDetails.getDiscount()) == 0) {
			// what comes from the item details is discount percentage and not discount amount.
			BigDecimal discPer = itemTaxDetails.getDiscountPercent();
			if (null == discPer || BigDecimal.ZERO.compareTo(discPer) == 0) {
				return BigDecimal.ZERO;
			}
			
			BigDecimal basePrice = getDiscountBasePrice(itemTaxDetails);
			discAmount = ConversionUtils.setScale(
					basePrice.multiply(getNetQty(itemTaxDetails)).
					multiply(discPer).divide(BigDecimal.valueOf(100)));
		}
		return discAmount;
	
	}

	protected abstract BigDecimal getDiscountBasePrice(ItemTaxDetails itemTaxDetails);
}
