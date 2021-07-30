package com.insta.hms.core.billing;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.patient.registration.PatientInsurancePlansService;
import com.insta.hms.mdm.discountplans.DiscountPlanService;
import com.insta.hms.mdm.insuranceplandetails.InsurancePlanDetailsService;
import com.insta.hms.mdm.insuranceplans.InsurancePlanService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DiscountService {
	
	@LazyAutowired
	private BillService billService; 
	
	@LazyAutowired
	private BillClaimService billClaimService;
	
	@LazyAutowired
	private PatientInsurancePlansService patInsPlanService;
	
	@LazyAutowired
	private InsurancePlanService insPlanService;
	
	@LazyAutowired
	private InsurancePlanDetailsService insPlanDetailsService;
	
	@LazyAutowired
	private DiscountPlanService discountPlanService;
	
	public void applyDiscountRule(BasicDynaBean charge, Integer billDiscountPlanId, String visitType){

		if (0 != billDiscountPlanId) {
			boolean isItemCategoryPayable = isItemCategoryPayable(charge, visitType);
			
      String billNo = (String) charge.get("bill_no");
      boolean isSystemDisc = false;
      if (null != billNo) {
        BasicDynaBean billBean = billService.findByKey(billNo);
        boolean isTpa = (Boolean) billBean.get("is_tpa");
        String patientId = (String) billBean.get("visit_id");
        BasicDynaBean visitPrimPlanDetails = patInsPlanService.getVisitPrimaryPlan(patientId);
        if (isTpa && visitPrimPlanDetails != null
            && visitPrimPlanDetails.get("discount_plan_id") != null) {
          isSystemDisc = true;
        }
      }
			// gets suitable discount rule for the charge
			BasicDynaBean discountRule = getDiscountRule(charge, billDiscountPlanId);
			Boolean isDoctorExcluded = null;

			if(null != charge.get("item_excluded_from_doctor")) {
				isDoctorExcluded = (Boolean) charge.get("item_excluded_from_doctor");
			}

			if (discountRule != null && (isDoctorExcluded != null ? !isDoctorExcluded :
					isItemCategoryPayable)) {

				BigDecimal disPerc = BigDecimal.ZERO;
				BigDecimal disaAmt = BigDecimal.ZERO;
				BigDecimal amt = ((BigDecimal) charge.get("act_rate")).multiply((BigDecimal) charge.get("act_quantity"));

				if (("P").equals((String) discountRule.get("discount_type"))) {
					disPerc = (BigDecimal) discountRule.get("discount_value");
				}

				if (("A").equals((String) discountRule.get("discount_type"))) {
					disaAmt = (BigDecimal) discountRule.get("discount_value");
				} else {
					disaAmt = (((BigDecimal) charge.get("act_rate")).multiply((BigDecimal) charge.get("act_quantity")))
							.multiply(disPerc).divide(new BigDecimal(100));
				}

				if ( disaAmt.compareTo(amt) > 0 ){
					disaAmt = amt;
				}
				amt = amt.subtract(disaAmt);
				charge.set("amount", amt);
				charge.set("discount", disaAmt);
				charge.set("overall_discount_amt", disaAmt);
				charge.set("overall_discount_auth", 0);
        if (isSystemDisc)
          charge.set("is_system_discount", "Y");
        else
          charge.set("is_system_discount", "N");
			}
		}
	}

	/** This method will return false when plan is mapped with default discount plan and
	 * insurance category payable is 'No' and bill is tpa bill. In all other cases it will 
	 * return true.  
	 * */
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public boolean isItemCategoryPayable(BasicDynaBean charge, String visitType){
		String billNo = (String) charge.get("bill_no");
		if(null != billNo) {
			BasicDynaBean billBean = billService.findByKey(billNo);
			boolean isTpa = (Boolean)billBean.get("is_tpa");
			if(isTpa) {
				Map keyMap = new HashMap();
				keyMap.put("bill_no", billNo);
				keyMap.put("priority", 1);
				
				BasicDynaBean bclBean = billClaimService.findByKey( keyMap);
				int planId = 0;
				String patientId = (String)billBean.get("visit_id");
				
				if(null != bclBean){
					planId = (Integer)bclBean.get("plan_id");
				}
				if(null == bclBean && null != patientId){
					Map pKeys = new HashMap();
					pKeys.put("patient_id", patientId);
					pKeys.put("priority", 1);
					BasicDynaBean patientPlanBean = patInsPlanService.findByKey(pKeys);
					planId = (Integer)patientPlanBean.get("plan_id");
				}
				
				if(planId != 0){
					Map insKeys = new HashMap();
					insKeys.put("plan_id", planId);
					BasicDynaBean planBean = insPlanService.findByPk(insKeys);
					int discPlanId = planBean.get("discount_plan_id") != null ? 
							(Integer)planBean.get("discount_plan_id") : 0;
					if(discPlanId > 0) {
						keyMap.clear();
						keyMap.put("plan_id", planId);
						keyMap.put("insurance_category_id", charge.get("insurance_category_id"));
						keyMap.put("patient_type", visitType);
						List<BasicDynaBean> planDetailsBeanList = insPlanDetailsService.listAll(keyMap);
						BasicDynaBean planDetBean = (planDetailsBeanList != null && !planDetailsBeanList.isEmpty())
								? planDetailsBeanList.get(0): null;
						if(planDetBean != null){
							String isCategoryPayable = (String)planDetBean.get("category_payable");
							if(isCategoryPayable.equals("N")) return false;
						}
					}
				}
			}
		}
		return true;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public BasicDynaBean getDiscountRule(BasicDynaBean chargeBean, Integer billDiscountPlanId){
		BasicDynaBean discountRuleBean = null;
		
		/*
		 * applicable_type tells on which to apply discount rule.It can have 3 values.
		 * N  : insurance category id of item in the charge
		 * C  : charge head of the charge
		 * I  : item id of the charge.
		 *    : if it is item id there is one more parameter which will decide which type of item to look at it.
		 */
		Map keys = new HashMap();
		keys.put("discount_plan_id", billDiscountPlanId);
		List<BasicDynaBean> discountPlanDetails = discountPlanService.listDiscountPlanDetails( keys, "priority");
		for (BasicDynaBean detailBean :  discountPlanDetails ){
			if ( ((detailBean.get("applicable_type").equals("N")
						&& (Integer) chargeBean.get("insurance_category_id") == Integer.parseInt(((String)detailBean.get("applicable_to_id")).trim())))
			  || ((detailBean.get("applicable_type").equals("C")
			        && chargeBean.get("package_id")  == null
					  	&& chargeBean.get("charge_head").equals(((String)detailBean.get("applicable_to_id")).trim()))
					  	|| (chargeBean.get("package_id")  != null && "PKG".equals(chargeBean.get("charge_group"))
					  	&& "PKGPKG".equals(((String)detailBean.get("applicable_to_id")).trim())))
			  || (detailBean.get("applicable_type").equals("I") 
						&& ((chargeBean.get("act_description_id")  != null && chargeBean.get("act_description_id").equals(((String)detailBean.get("applicable_to_id")).trim()))
							|| (chargeBean.get("package_id")  != null && "PKG".equals(chargeBean.get("charge_group"))
							&& (String.valueOf(chargeBean.get("package_id"))).equals(((String)detailBean.get("applicable_to_id")).trim()))))
					) {
				discountRuleBean = detailBean;
				break;
			}
		}
		return discountRuleBean;
		
	}

  public void applyDiscountPlan(int defaultDiscPlanId, List<BasicDynaBean> billCharges,
      String visitType, List<String> updateChgIdKeys, List<BasicDynaBean> billChargeToUpdateDisc) {
    // TODO Auto-generated method stub
    for(BasicDynaBean charge:billCharges){
      String chargeId = (String)charge.get("charge_id");
      applyDiscountRule(charge, defaultDiscPlanId, visitType);
      billChargeToUpdateDisc.add(charge);
      updateChgIdKeys.add(chargeId);
    }
  }

}
