package com.insta.hms.core.inventory.procurement;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.taxation.GenericTaxCalculator;
import com.insta.hms.common.taxation.ItemTaxDetails;
import com.insta.hms.common.taxation.TaxContext;
import com.insta.hms.mdm.itemsubgroupstaxdetails.ItemSubgroupTaxDetailsService;

import org.apache.commons.beanutils.BasicDynaBean;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 
 * @author irshadmohammed
 *
 */

public class PurchaseTaxCalculator extends GenericTaxCalculator {

    @LazyAutowired
    ItemSubgroupTaxDetailsService itemSubgroupTaxDetailsService;

	List<String> supportedGroups = new ArrayList<String>();
	
	protected PurchaseTaxCalculator(String[] supportedGroups) {
		this.supportedGroups.addAll(Arrays.asList(supportedGroups));
	}

	public String[] getSupportedGroups() {
		if (null == supportedGroups || supportedGroups.isEmpty()) {
			return null;
		}
		return supportedGroups.toArray(new String[0]);
	}

	protected boolean isInterStatePurchase(BasicDynaBean taxParameter, TaxContext taxContext) {
		if (null == taxContext) return false; // in case of no information, treat as same state
		
		BasicDynaBean supplierDetails = taxContext.getSupplierBean();
		BasicDynaBean centerDetails = taxContext.getCenterBean();
		if (null != supplierDetails && null != centerDetails) {
			String supplierState = (String)supplierDetails.get("state_id");
			if (null != supplierState && !supplierState.trim().isEmpty()) {
				return !supplierState.equalsIgnoreCase((String)centerDetails.get("state_id"));
			}
		}
		return false; // default
	}

	@Override
	public BigDecimal applyTaxRate(ItemTaxDetails itemTaxDetails,
			BigDecimal taxRate) {
		
		boolean useDiscountedPrice = true;
		String taxBasis = (null != itemTaxDetails) ? itemTaxDetails.getTaxBasis() : null;
		if (null != taxBasis && !taxBasis.startsWith("C")) {
			useDiscountedPrice = false;
		}
		return super.applyTaxRate(itemTaxDetails, taxRate, useDiscountedPrice);
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
			
			BigDecimal basePrice = (null != itemTaxDetails && null != itemTaxDetails.getCostPrice()) ? 
					itemTaxDetails.getCostPrice() : getBasePrice(itemTaxDetails);
			// apply the discount percentage on adjMRP * qty if the discount is exclusive of tax
			discAmount = ConversionUtils.setScale(
					basePrice.multiply(getNetQty(itemTaxDetails)).
					multiply(discPer).divide(BigDecimal.valueOf(100)));
		}
		return discAmount;
	
	}
}