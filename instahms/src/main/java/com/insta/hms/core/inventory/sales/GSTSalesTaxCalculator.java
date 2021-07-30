package com.insta.hms.core.inventory.sales;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.taxation.ItemTaxDetails;
import com.insta.hms.common.taxation.TaxContext;
import com.insta.hms.mdm.taxsubgroups.TaxSubGroupService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 
 * @author irshadmohammed
 *
 */
@Component
public class GSTSalesTaxCalculator extends SalesTaxCalculator {
	@LazyAutowired
	TaxSubGroupService taxSubGroupService;
	
	private static final String[] SUPPORTED_GROUPS = new String[]{"GST"};

	public GSTSalesTaxCalculator() {
		super(SUPPORTED_GROUPS);
	}

	private BasicDynaBean getTaxRateBean(Integer subGroupId, TaxContext taxContext) {
		
		List<BasicDynaBean> subGroups = taxContext.getSubgroups();
		BasicDynaBean salesBean = taxContext.getItemBean();
		Map subGroupBeans = ConversionUtils.listBeanToMapBean(subGroups, "item_subgroup_id");
		Object[] subgroupIds = null;
		if (null != subGroupBeans && subGroupBeans.size() > 0) {
			subgroupIds = subGroupBeans.keySet().toArray(new Object[0]);
		}
		if (null != subgroupIds && subgroupIds.length > 0) {
			Map filter = new HashMap();
			filter.put("item_subgroup_id", subgroupIds);
			List<BasicDynaBean> subgroupParamList = taxSubGroupService.getSubGroups(filter);
			Map subGroupParamMap = ConversionUtils.listBeanToMapBean(subgroupParamList, "item_subgroup_id");
			if (null != subGroupParamMap) {
				return (BasicDynaBean) subGroupParamMap.get(subGroupId);
			}
		}
		return null;
	}

	@Override
	public Map<Integer, Object> calculateTaxes(ItemTaxDetails taxBean,
			TaxContext taxContext) {
		BasicDynaBean bean = getTaxRateBean(taxBean.getSugbroupId(), taxContext);
		if (null != bean) {
			BigDecimal aggRate = (null != (BigDecimal)bean.get("agg_tax_rate")) ? (BigDecimal)bean.get("agg_tax_rate") : BigDecimal.ZERO;
			BigDecimal mrp = (null != taxBean.getMrp()) ? taxBean.getMrp() : BigDecimal.ZERO;
			BigDecimal aggTaxPer = aggRate.divide(BigDecimal.valueOf(100));
			BigDecimal adjMrp = ConversionUtils.divideHighPrecision(mrp, BigDecimal.ONE.add(aggTaxPer));
			taxBean.setAggregateRate(aggRate);
			taxBean.setAdjMrp(adjMrp);
		}
		// Let the rest of the calculation kick in
		return super.calculateTaxes(taxBean, taxContext);
	}
	
	@Override
	protected BigDecimal getTaxBasePrice(ItemTaxDetails itemTaxDetails) {

		// For MB mapped items tax is always on adjusted MRP
		if (null != itemTaxDetails && null != itemTaxDetails.getTaxBasis() 
				&& itemTaxDetails.getTaxBasis().startsWith("M")) {
			return (null != itemTaxDetails) ? itemTaxDetails.getAdjMrp() : BigDecimal.ZERO;
		}
		
		// TODO : This really needs cleanup, will live with it for 11.12.
		
		if (null != itemTaxDetails && null != itemTaxDetails.getTaxBasis()
				&& itemTaxDetails.getTaxBasis().startsWith("C")) {
			// tax % = [ (MRP / Adj.MRP) - 1 ] * 100
			BigDecimal mrp = (null != itemTaxDetails.getMrp()) ? itemTaxDetails.getMrp() : BigDecimal.ZERO;
			BigDecimal adjMrp = (null != itemTaxDetails.getAdjMrp()) ? itemTaxDetails.getAdjMrp() : BigDecimal.ZERO;
			
			BigDecimal aggTaxPerc = (null != itemTaxDetails.getAggTaxRate()) ? itemTaxDetails.getAggTaxRate() : BigDecimal.ZERO; 
/*			BigDecimal aggTaxPerc = ((ConversionUtils.divideHighPrecision(mrp, adjMrp)).
					subtract(BigDecimal.ONE)).
					multiply(BigDecimal.valueOf(100)); */
			BigDecimal discPerc = (null != itemTaxDetails.getDiscountPercent()) ? itemTaxDetails.getDiscountPercent() : BigDecimal.ZERO;
			
			if ("I".equalsIgnoreCase(itemTaxDetails.getDiscountType())) {
				// For C/CB mapped items, inclusive of tax -
				// discount = mrp * d /100
				// discounted amt = mrp - discount
				// tax base amt = discounted amt adjusted for tax
				// 				= discounted amt / ( 1 + (r/100))
				// The above translates to 
				// tax base amt = discounted amount / (1 + (r/100))
				// 				= (mrp - discount) / (1 + r/100)
				//				= (mrp - (mrp * d / 100))) / ((100 + r)/100)
				// 				= ( ( 100 . mrp - mrp . d ) / 100 ) * (100 / (100 + r))
				// =====================================================================
				// 				= mrp [ 100 - d ] / [ 100 - r ]
				// =====================================================================

				return ConversionUtils.divideHighPrecision(mrp.multiply(
						BigDecimal.valueOf(100).subtract(discPerc)), 
						(BigDecimal.valueOf(100).add(aggTaxPerc)));
				
			} else {
				// For C/CB mapped items exclusive of tax -
				// discount = adjmrp * d / 100
				// discounted_amt = mrp - discount
				// tax_base_amt = discounted_amt adjusted for tax
				// tax_base_amt = discounted_amt * 100 / ( 100 + r)
				// 				= (mrp - discount) * 100 / (100 + r)
				//				= (mrp - (adjmrp*d/100)) * 100 / (100 + r)
				// Substituting adjmrp ==> mrp / (1 + r/100) we get
				// =======================================================
				// Final Base price is 100 * MRP * (100+r-d) / (100+r)^2
				// =======================================================
				
				return ConversionUtils.divideHighPrecision(mrp.multiply(
						BigDecimal.valueOf(100).subtract(discPerc)), 
						(BigDecimal.valueOf(100).add(aggTaxPerc)));
				/*return ConversionUtils.divideHighPrecision(
						(BigDecimal.valueOf(100).
						multiply(mrp).
						multiply(BigDecimal.valueOf(100).add(aggTaxPerc).subtract(discPerc))), 
						((BigDecimal.valueOf(100).add(aggTaxPerc)).multiply(BigDecimal.valueOf(100).add(aggTaxPerc))));*/
			}
		}
		return super.getTaxBasePrice(itemTaxDetails);
	}

	@Override
	protected BigDecimal getDiscountBasePrice(ItemTaxDetails itemTaxDetails) {
		if (null != itemTaxDetails && "I".equalsIgnoreCase(itemTaxDetails.getDiscountType())) {
			return itemTaxDetails.getMrp(); // Discount is calculated on MRP 
		} else {
			return itemTaxDetails.getMrp(); // Discount is calculated on MRP
			//return itemTaxDetails.getAdjMrp(); // Discount is calculated on Adj MRP
		}
	}
	
	@Override
	public BigDecimal applyTaxRate(ItemTaxDetails itemTaxDetails,
			BigDecimal taxRate) {
		return super.applyTaxRate(itemTaxDetails, taxRate, false);
	}
}
