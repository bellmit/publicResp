package com.insta.hms.core.billing;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.taxation.GenericBillingTaxCalculator;
import com.insta.hms.common.taxation.TaxContext;
import com.insta.hms.mdm.itemsubgroupstaxdetails.ItemSubgroupTaxDetailsService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class BillTaxCalculator extends GenericBillingTaxCalculator {
	
	private static final String[] SUPPORTED_GROUPS = new String[]{"GST","VAT","KSACTA"};

	public BillTaxCalculator() {
		super(SUPPORTED_GROUPS);
	}
	
	@LazyAutowired
	ItemSubgroupTaxDetailsService itemSubgroupTaxDetailsService;
	
	@LazyAutowired
	BillChargeTaxService billChargeTaxService;

	protected BasicDynaBean getTaxParameters(Map<String, Object> subGroupMap, TaxContext taxContext) {
		Integer itemSubGrpId = (Integer)subGroupMap.get("item_subgroup_id");
		String chargeId = taxContext.getTransactionId();
		BasicDynaBean taxParameter = null;
		if(null != chargeId){
			taxParameter = billChargeTaxService.getBillChargeTaxBean(chargeId, itemSubGrpId);
		}else{
			taxParameter = itemSubgroupTaxDetailsService.findByPk(subGroupMap);
		}
		return taxParameter;
	}
}
