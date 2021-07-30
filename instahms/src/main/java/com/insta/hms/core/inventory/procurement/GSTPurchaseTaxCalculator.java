package com.insta.hms.core.inventory.procurement;

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
public class GSTPurchaseTaxCalculator extends PurchaseTaxCalculator {
	
	@LazyAutowired
	TaxSubGroupService taxSubGroupService;

	private static final String[] SUPPORTED_GROUPS = new String[]{"GST"};

	public GSTPurchaseTaxCalculator() {
		super(SUPPORTED_GROUPS);
	}

	@Override
	protected boolean isTaxApplicable(BasicDynaBean taxParameter, TaxContext taxContext) {
		if (!super.isTaxApplicable(taxParameter, taxContext)) return false;
		return !isInterStatePurchase(taxParameter, taxContext); 
    }
	
	@Override
	public Map<Integer, Object> calculateTaxes(ItemTaxDetails taxBean,
			TaxContext taxContext) {
		BigDecimal aggRate = getAggregateTaxRate(taxBean.getSugbroupId(), taxContext);
		BigDecimal mrp = (null != taxBean.getMrp()) ? taxBean.getMrp() : BigDecimal.ZERO;
		BigDecimal aggTaxPer = aggRate.divide(BigDecimal.valueOf(100));
		BigDecimal adjMrp = ConversionUtils.divideHighPrecision(mrp, BigDecimal.ONE.add(aggTaxPer));
		taxBean.setAdjMrp(adjMrp);
		// Let the rest of the calculation kick in
		return super.calculateTaxes(taxBean, taxContext);
	}
	
	
	private BigDecimal getAggregateTaxRate(Integer subGroupId, TaxContext taxContext) {

		List<BasicDynaBean> subGroups = taxContext.getSubgroups();
		BasicDynaBean bean = null;
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
				bean = (BasicDynaBean) subGroupParamMap.get(subGroupId);
				if (null != bean) {
					 return (null != bean.get("agg_tax_rate") ? (BigDecimal)bean.get("agg_tax_rate") : BigDecimal.ZERO);
				}
			}
		}
		return BigDecimal.ZERO;
	}
}
