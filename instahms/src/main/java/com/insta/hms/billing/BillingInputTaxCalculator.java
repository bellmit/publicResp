package com.insta.hms.billing;

import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.taxation.GenericBillingTaxCalculator;
import com.insta.hms.common.taxation.TaxContext;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public abstract class BillingInputTaxCalculator extends GenericBillingTaxCalculator {
	
	private static final String[] SUPPORTED_GROUPS = new String[]{"GST","VAT","KSACTA"};

	public BillingInputTaxCalculator() {
		super(SUPPORTED_GROUPS);
	}
	
	protected BasicDynaBean getTaxParameters(Map<String, Object> subGroupMap, TaxContext context) {
		
		Integer itemSubGrpId = (Integer)subGroupMap.get("item_subgroup_id");
		String chargeId = context.getTransactionId();
		BasicDynaBean taxParameter = null;
		if(null != chargeId){
			Map<String,Object> keys = new HashMap<String, Object>();
			keys.put("charge_id", chargeId);
			keys.put("tax_sub_group_id", itemSubGrpId);
			try{
				taxParameter = new GenericDAO("bill_charge_tax").findByKey(keys);
			}catch (SQLException sqle) {
				// logger.error();
			} 
		}else{
			try {
				taxParameter = new GenericDAO("item_sub_groups_tax_details").findByKey(subGroupMap);
			} catch (SQLException sqle) {
				// logger.error();
			}  
		}
		
		return taxParameter;
	}
	
}
