package com.insta.hms.core.billing;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.taxation.GenericBillingTaxCalculator;
import com.insta.hms.common.taxation.ItemTaxDetails;
import com.insta.hms.common.taxation.TaxContext;
import com.insta.hms.mdm.itemsubgroupstaxdetails.ItemSubgroupTaxDetailsService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class KSATaxCalculator extends GenericBillingTaxCalculator{
	
	private static final String[] SUPPORTED_GROUPS = new String[]{"KSACEX"};

	public KSATaxCalculator() {
		super(SUPPORTED_GROUPS);
	}
	
	@LazyAutowired
	ItemSubgroupTaxDetailsService itemSubgroupTaxDetailsService;
	
	@LazyAutowired
	BillChargeTaxService billChargeTaxService;

	@Override
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

	protected BigDecimal applyTaxRate(ItemTaxDetails itemTaxDetails, BigDecimal taxRate, TaxContext taxContext) {
		BasicDynaBean patientBean = taxContext.getPatientBean();
		BasicDynaBean billBean = taxContext.getBillBean();
		BasicDynaBean centerBean = taxContext.getCenterBean();
		BasicDynaBean sponsorBean = taxContext.getItemBean();
		BigDecimal taxrate = taxRate ;
		//KSA TPA - when insurance tax is not payable
		if(null != billBean && !billBean.getMap().isEmpty() && (boolean)billBean.get("is_tpa") && 
				null != sponsorBean && !sponsorBean.getMap().isEmpty() && (boolean)sponsorBean.get("claim_amount_includes_tax").equals("N")
				&& null != patientBean && !patientBean.getMap().isEmpty() && null != patientBean.get("nationality_id") 
				&& patientBean.get("nationality_id").equals(centerBean.get("country_id"))){
			taxrate = BigDecimal.ZERO;
		}//KSA cash patient
		else if(null != billBean && !billBean.getMap().isEmpty() && !(boolean)billBean.get("is_tpa")
			&& null != patientBean && !patientBean.getMap().isEmpty() && null != patientBean.get("nationality_id") 
				&& patientBean.get("nationality_id").equals(centerBean.get("country_id"))){
			taxrate = BigDecimal.ZERO;
		}
		return super.applyTaxRate(itemTaxDetails, taxrate, true);
	}
	
	
	public Map<Integer, Object> calculateTaxes(ItemTaxDetails taxBean, TaxContext taxContext) {
		//Call the BaseClassCalulator
		Map<Integer, Object> taxMap = super.calculateTaxes(taxBean, taxContext);
		
		//Additional details required to check Nationality
		BasicDynaBean patientBean = taxContext.getPatientBean();
		BasicDynaBean centerBean = taxContext.getCenterBean();
		BasicDynaBean billBean = null;
		if(null != taxContext.getBillBean()){
			billBean = taxContext.getBillBean();
		}else{
			billBean = taxContext.getItemBean();
		}
		
		//Adjusted Amount for KSA TPA - When Insurance is Payable
		if(null != taxContext.getItemBean() && !taxContext.getItemBean().getMap().isEmpty() &&
				taxContext.getItemBean().get("claim_amount_includes_tax").equals("Y") && null != billBean && 
						!billBean.getMap().isEmpty() && (boolean)billBean.get("is_tpa")
						&& null != patientBean && !patientBean.getMap().isEmpty() && null != patientBean.get("nationality_id") 
						&& patientBean.get("nationality_id").equals(centerBean.get("country_id")) ){			
			for(Map.Entry<Integer, Object> taxMapEntry : taxMap.entrySet()){
				Map<String,Object> taxMapEntryValue = (Map<String, Object>) taxMapEntry.getValue();
				taxMapEntryValue.put("adjTaxAmt", "Y");
				taxMap.put(taxMapEntry.getKey(), taxMapEntryValue);
			}
		}
		return taxMap;
	}
	
}
