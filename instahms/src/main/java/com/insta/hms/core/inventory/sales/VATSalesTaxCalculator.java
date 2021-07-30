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

@Component
public class VATSalesTaxCalculator extends SalesTaxCalculator {

	@LazyAutowired
	TaxSubGroupService taxSubGroupService;

	private static final String[] SUPPORTED_GROUPS = new String[]{"VAT","KSACTA"};
	
	public VATSalesTaxCalculator() {
		super(SUPPORTED_GROUPS);
	}

	@Override
	protected BigDecimal getBasePrice(ItemTaxDetails itemTaxDetails) {
		if (null != itemTaxDetails) {
			return itemTaxDetails.getMrp();
		}
		return BigDecimal.ZERO;
	}

	@Override
	protected void setNetAmount(ItemTaxDetails itemTaxDetails, BigDecimal taxAmount) {
		// Let the base net amount be set by the super class
		super.setNetAmount(itemTaxDetails, taxAmount); 
		// Add tax amount to it
		itemTaxDetails.setNetAmount(itemTaxDetails.getNetAmount().add(taxAmount));
	}
	
	@Override
	protected BigDecimal getDiscountBasePrice(ItemTaxDetails itemTaxDetails) {
		return (null != itemTaxDetails) ? itemTaxDetails.getMrp() : BigDecimal.ZERO;
	}

	@Override
	public Map<Integer, Object> calculateTaxes(ItemTaxDetails taxBean,
			TaxContext taxContext) {
		BasicDynaBean bean = getTaxRateBean(taxBean.getSugbroupId(), taxContext);
		if (null != bean) {
			BigDecimal aggRate = (null != (BigDecimal)bean.get("agg_tax_rate")) ? (BigDecimal)bean.get("agg_tax_rate") : BigDecimal.ZERO;
			BigDecimal mrp = (null != taxBean.getMrp()) ? taxBean.getMrp() : BigDecimal.ZERO;
			BigDecimal adjMrp = mrp.multiply(BigDecimal.ONE.add((aggRate.divide(BigDecimal.valueOf(100)))));
			taxBean.setAdjMrp(adjMrp);
		}
		// Let the rest of the calculation kick in
		return super.calculateTaxes(taxBean, taxContext);
	}

	public BasicDynaBean getTaxRateBean(Integer subGroupId, TaxContext taxContext) {
		
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

}
