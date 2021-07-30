package com.insta.hms.core.billing;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.core.clinical.eauthorization.EAuthPrescriptionActivitiesRepository;
import com.insta.hms.core.clinical.preauth.PreAuthItemsService;
import com.insta.hms.core.clinical.eauthorization.EAuthPrescriptionActivitiesRepository;
import com.insta.hms.core.insurance.InsuranceClaimService;
import com.insta.hms.core.inventory.sales.SalesService;
import com.insta.hms.core.patient.registration.PatientInsurancePlansService;
import com.insta.hms.core.patient.registration.RegistrationService;
import com.insta.hms.integration.priorauth.PriorAuthorizationService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BillChargeClaimService {

  @LazyAutowired
  private BillChargeClaimRepository billChargeClaimRepo;

  @LazyAutowired
  private BillService billService;

  @LazyAutowired
  private BillClaimService billClaimService;

  @LazyAutowired
  private RegistrationService regService;

  @LazyAutowired
  private InsuranceClaimService insuranceClaimService;

  @LazyAutowired
  private PatientInsurancePlansService patInsPlansService;

  @LazyAutowired
  private SalesService salesService;
  
  @LazyAutowired
  private BillHelper billHelper;
  
  @LazyAutowired
  private EAuthPrescriptionActivitiesRepository eauthPrescActRepo;
  
  @LazyAutowired
  private GenericPreferencesService genericPreferencesService;

  public void insertInsuranceClaimId(String visitId, int planId, String claimId, String billNo) {

    BasicDynaBean visitDetailsBean = regService.findByKey(visitId);
    BasicDynaBean billBean = billService.getDetails(billNo);
    int accountGroup = (Integer) billBean.get("account_group");

    BasicDynaBean claimbean = null;
    String mainVisitId = visitDetailsBean.get("main_visit_id") != null ? (String) visitDetailsBean
        .get("main_visit_id") : null;
    String patientId = visitDetailsBean.get("patient_id") != null ? (String) visitDetailsBean
        .get("patient_id") : null;
    String opType = visitDetailsBean.get("op_type") != null ? (String) visitDetailsBean
        .get("op_type") : null;

    if (visitDetailsBean.get("op_type") != null) {

      opType = opType.equals("R") ? "M" : opType; // If Revisit then claim op type is Main.
      claimbean = insuranceClaimService.getBean();
      claimbean.set("claim_id", claimId);
      claimbean.set("main_visit_id", mainVisitId);
      claimbean.set("patient_id", patientId);
      claimbean.set("plan_id", planId);
      claimbean.set("op_type", opType);
      claimbean.set("status", "O");
      claimbean.set("account_group", accountGroup);
      insuranceClaimService.insert(claimbean);
    }
  }

  public void updateBillChargeClaims(List<BasicDynaBean> billCharges,
      Map<String, List<BasicDynaBean>> billChargeClaimsMap) {
    for (BasicDynaBean billCharge : billCharges) {
      String chargeID = (String) billCharge.get("charge_id");
      String chargeType = (String) billCharge.get("charge_type");
      List<BasicDynaBean> billChargeClaims = billChargeClaimsMap.get(chargeID);
      for (BasicDynaBean billChargeClaim : billChargeClaims) {
        String claimId = (String) billChargeClaim.get("claim_id");
        Map<String, Object> keys = new HashMap<String, Object>();
        if (chargeType.equals("hospital")) {
          keys.put("charge_id", chargeID);
          keys.put("claim_id", claimId);
          billChargeClaimRepo.update(billChargeClaim, keys);
        } else if (chargeType.equals("pharmacy")) { // TODO when migrating Inventory
          int saleItemId = Integer.parseInt(chargeID.split("-")[1]);
          keys.put("sale_item_id", saleItemId);
          keys.put("claim_id", claimId);
          BigDecimal insClaimAmt = (BigDecimal) billChargeClaim.get("insurance_claim_amt");
          BigDecimal insClaimTaxAmt = (BigDecimal)billChargeClaim.get("tax_amt");
          Boolean inclInClaimCalc = (Boolean) billChargeClaim.get("include_in_claim_calc");
          BasicDynaBean salesClaimBean = salesService.getSalesClaimDetails(keys);

          if (null != salesClaimBean) {
            salesClaimBean.set("insurance_claim_amt", insClaimAmt);
            salesClaimBean.set("ref_insurance_claim_amount", insClaimAmt);
            salesClaimBean.set("tax_amt", insClaimTaxAmt);
            salesClaimBean.set("include_in_claim_calc", inclInClaimCalc);
            salesService.updateSalesClaimBean(salesClaimBean, keys);
          }
        }
      }
    }
  }

  public Boolean insertAll(List<BasicDynaBean> billChargeClaimList) {
    // TODO Auto-generated method stub
    return billChargeClaimRepo.batchInsert(billChargeClaimList)[0] >= 0;
  }

  public Boolean delete(Map<String, Object> keys) {
    // TODO Auto-generated method stub
    return billChargeClaimRepo.delete(keys) >= 0;
  }

  public Boolean delete(String key, String value) {
    // TODO Auto-generated method stub
    return billChargeClaimRepo.delete(key, value) >= 0;
  }

  public Boolean updateBillChargeClaimsAmtToZero(String billNo) {
    // TODO Auto-generated method stub
    return billChargeClaimRepo.updateBillChargeClaimsAmtToZero(billNo);
  }

  public Boolean includeBillChargesInClaimCalc(String visitId, String billStatus) {
    // TODO Auto-generated method stub
    return billChargeClaimRepo.includeBillChargesInClaimCalc(visitId, billStatus);
  }

  public void updateSalesBillCharges(String visitId, String billStatus) {
    // TODO Auto-generated method stub
    List<BasicDynaBean> saleBillChargeClaims = salesService.getSaleBillCharges(visitId);

    for (BasicDynaBean saleBillChargeClaim : saleBillChargeClaims) {
      String chargeId = (String) saleBillChargeClaim.get("charge_id");
      String claimId = (String) saleBillChargeClaim.get("claim_id");
      Map<String, Object> keys = new HashMap<String, Object>();
      keys.put("charge_id", chargeId);
      keys.put("claim_id", claimId);
      BigDecimal insClaimAmt = salesService.getInsuranceClaimAmtFromSalesClaimDetails(chargeId,
          claimId);
      BigDecimal insClaimTaxAmt = salesService.getInsuranceClaimTaxAmtFromSalesClaimDetails(
          chargeId, claimId);
      saleBillChargeClaim.set("insurance_claim_amt", insClaimAmt);
      saleBillChargeClaim.set("tax_amt", insClaimTaxAmt);
      billChargeClaimRepo.update(saleBillChargeClaim, keys);
    }

  }
	
	/**
	 * BigDecimal[] claimAmounts : will have primary_claim_amt ==
	 * Bill.charge("amount"), secondary_claim_amt =0
	 * 
	 * PreAuthIds and preAuthModeIds will need to made both for primary and
	 * secondary.
	 */
  public Boolean insertBillChargeClaims(List<BasicDynaBean> charges, int[] planIds, String visitId,
      BasicDynaBean bill, String[] preAuthIds, Integer[] preAuthModeIds){
    return insertBillChargeClaims(charges, planIds, visitId, bill, preAuthIds, preAuthModeIds, new HashMap<String, String>());
  }
  
	public Boolean insertBillChargeClaims(List<BasicDynaBean> charges, int[] planIds, String visitId,
			BasicDynaBean bill, String[] preAuthIds, Integer[] preAuthModeIds, Map<String, String> preAuthStatusMap){
		boolean success = false;

		if (null != planIds) {
			for (int i = 0; i < planIds.length; i++) {
				if (planIds[i] != 0) {
					Integer accGroup = 0;
					accGroup = (null != bill) ? ((null != bill.get("account_group")) ? (Integer) bill
							.get("account_group") : 0) : 0;
					String visitClaimExists = billClaimService.isVisitClaimExists(planIds[i], visitId, accGroup);

					String claimId = visitClaimExists;
					if (null == visitClaimExists || visitClaimExists == "") {
						int centerId = regService.getCenterId(visitId);
						claimId = insuranceClaimService.getNextPrefixedId(centerId, accGroup);
						insertInsuranceClaimId(visitId, planIds[i], claimId, (String) bill.get("bill_no"));
					}

					boolean billClaimExist = billClaimService.isBillClaimExist(planIds[i], visitId,
							(String) bill.get("bill_no"));

					Map<String, Object> keys = new HashMap<String, Object>();
          keys.put("patient_id", visitId);
          keys.put("plan_id", planIds[i]);
          
					BasicDynaBean planBean = patInsPlansService.list(keys);
          int priority = (Integer) planBean.get("priority");
          String sponsorId = (String) planBean.get("sponsor_id");
          
					if (!billClaimExist) {
						BasicDynaBean billClaimBean = billClaimService.getBean();
						billClaimBean.set("visit_id", visitId);
						billClaimBean.set("bill_no", bill.get("bill_no"));
						billClaimBean.set("plan_id", planIds[i]);
						billClaimBean.set("sponsor_id", sponsorId);
						billClaimBean.set("claim_id", claimId);
						billClaimBean.set("priority", priority);
						success = billClaimService.insert(billClaimBean) > 0;
					} else {
						claimId = billClaimService.getClaimId(planIds[i], (String) bill.get("bill_no"), visitId);
					}

					if (null != charges) {
						for (BasicDynaBean charge : charges) {
							BasicDynaBean bean = billChargeClaimRepo.getBean();
							Object[] obj = new Object[] { (String) bill.get("bill_no"), charge.get("charge_id"),
									claimId };
							boolean billChgClaimExists = billChargeClaimRepo.isBillChargeClaimExists(obj);
							if (!billChgClaimExists) {
								obj = new Object[] { visitId, (String) bill.get("bill_no"), planIds[i] };
								String tpaId = billChargeClaimRepo.getSponsorIdFrombillClaim(obj);
								bean.set("claim_id", claimId);
								bean.set("charge_id", charge.get("charge_id"));
								bean.set("bill_no", charge.get("bill_no"));
								bean.set("sponsor_id", tpaId);
								bean.set("approval_id", charge.get("approval_id"));
								bean.set("include_in_claim_calc", true);
								bean.set("insurance_claim_amt", i==0 ? charge.get("amount"): BigDecimal.ZERO);
								if (null != preAuthIds && planIds.length < 3) {
									bean.set("prior_auth_id", preAuthIds[i]);
								}
								if (null != preAuthModeIds && planIds.length < 3) {
									bean.set("prior_auth_mode_id", preAuthModeIds[i]);
								}
								bean.set("code_type", charge.get("code_type"));
								bean.set("charge_excluded", charge.get("charge_excluded"));
								
                String chargeGroup = (String) charge.get("charge_group");

                if (!chargeGroup.equals("MED") && !chargeGroup.equals("RET")) {
                  billHelper.checkForInsCatInRedis(charge, planIds[i], visitId);
                }
								bean.set("insurance_category_id", charge.get("insurance_category_id"));
								bean.set("amount_included", charge.get("amount_included"));
		
								if(null != charge.get("preauth_act_id")){
								  Integer preAuthActId = (Integer)charge.get("preauth_act_id");
	                boolean shouldUpdateClaimAmountBasedOnPreAuth = "Y".equals(
	                    genericPreferencesService.getAllPreferences().get("set_preauth_approved_amt_as_claim_amt"));
								  if(priority == 1 && shouldUpdateClaimAmountBasedOnPreAuth) {
								    String status = preAuthStatusMap.get((String)charge.get("charge_id"));
								    setPreAuthApprovedAmount(preAuthActId, bean, charge, status);
								  }
								}
								success = billChargeClaimRepo.insert(bean) > 0;
							}
						}
					}
				}
			}
		}
		return success;
	}
	
	private void setPreAuthApprovedAmount(Integer preAuthActId, BasicDynaBean bean, BasicDynaBean charge, String preAuthStatus) {
	  
	  if ("D".equals(preAuthStatus)) {
	    bean.set("insurance_claim_amt", BigDecimal.ZERO);
	    bean.set("include_in_claim_calc", false);
	    return;
	  }
	  
    BasicDynaBean preAuthActBean = eauthPrescActRepo.findByKey("preauth_act_id", preAuthActId);
    if(null != preAuthActBean) {
      BigDecimal approvedAmt = (BigDecimal)preAuthActBean.get("claim_net_approved_amount");
      String preauthActStatus = (String)preAuthActBean.get("preauth_act_status");
      Integer approvedQuantity = (Integer) preAuthActBean.get("approved_qty");
      if (approvedAmt != null && approvedAmt.compareTo(BigDecimal.ZERO) > 0) {
        BigDecimal quantity = (BigDecimal) charge.get("act_quantity");
        if (quantity != null && approvedQuantity != null && approvedQuantity > 0
            && quantity.compareTo(BigDecimal.ZERO) > 0) {
          BigDecimal approvedPerItemAmt = approvedAmt.divide(new BigDecimal(approvedQuantity), RoundingMode.HALF_EVEN);
          approvedAmt = approvedPerItemAmt.multiply(
              new BigDecimal(Math.min(quantity.intValue(), approvedQuantity.intValue())));
        } else {
          approvedAmt = BigDecimal.ZERO;
        }
      }
      
      bean.set("insurance_claim_amt", approvedAmt);
      
      Integer initialQty = (Integer) preAuthActBean.get("act_qty");
      // If the claim is locked and the it is a service charge and the act_qty > approved_qty
      // assume that this should not be included in the claim.
      // If sent quantity and approved quantity are different and approvedAmt is 0 then do not
      // include in claim.
      boolean isMultiQtyServiceWithDeniedQty = "SNP".equals((String) charge.get("charge_group"))
          && (initialQty.compareTo(approvedQuantity) > 0);
      if (BigDecimal.ZERO.compareTo(approvedAmt) == 0
          && (preauthActStatus.equals("D") || isMultiQtyServiceWithDeniedQty)) {
        bean.set("include_in_claim_calc", false);
      }
    }
  }

  public boolean updateBillChargeClaims(List<BasicDynaBean> updateBillChargeList, String visitId,
			String billNo, int[] planIds, boolean preAuthUpdateReq, String[] preAuthIds, Integer preAuthModeIds[]) {
		boolean success = false;
		BasicDynaBean bean = getBean();
		if (planIds != null && planIds.length > 0) {
			for(int i=0; i<planIds.length; i++){
				Map<String,Object> keys = new HashMap<String, Object>();
				keys.put("bill_no", billNo);
				keys.put("visit_id", visitId);
				keys.put("plan_id", planIds[i]);
				BasicDynaBean billClaimBean = billChargeClaimRepo.findByKey(keys);
				if(null != billClaimBean) {
					for(BasicDynaBean charge : updateBillChargeList){
						String claimId = (String)billClaimBean.get("claim_id");
						Map<String,Object> chKeys = new HashMap<String, Object>();
						chKeys.put("bill_no", billNo);
						chKeys.put("claim_id", claimId);
						chKeys.put("charge_id", charge.get("charge_id"));
						bean.set("include_in_claim_calc", true);
						if(preAuthUpdateReq){
							if(null != preAuthIds)
								bean.set("prior_auth_id", preAuthIds[i]);
							if(null != preAuthModeIds)
								bean.set("prior_auth_mode_id", preAuthModeIds[i]);
						}
						success = update(bean, chKeys) >= 0;
					}
				}
			}
		}
		return success;
	}

	public BasicDynaBean getBean() {
		return billChargeClaimRepo.getBean();
	}

	public int cancelBillChargeClaim(String chargeId) {
		return billChargeClaimRepo.cancelBillClaimCharge(chargeId);
	}

	public int[] cancelBillChargeClaim(List<String> chargeIdsList) {
		return billChargeClaimRepo.cancelBillClaimCharge(chargeIdsList);
	}
	
	public int insert(BasicDynaBean bean){
		return billChargeClaimRepo.insert(bean);
	}
	
	public int update(BasicDynaBean bean, Map keys){
		return billChargeClaimRepo.update(bean, keys);
	}
	
	public BasicDynaBean findByKey(Map filterMap){
		return billChargeClaimRepo.findByKey(filterMap);
	}
	
	public void updateConsultationChargeClaims(List<BasicDynaBean> billCharges,
			Map<String, List<BasicDynaBean>> billChargeClaimsMap) {
		for (BasicDynaBean billCharge : billCharges) {
			String chargeID = (String) billCharge.get("charge_id");
			List<BasicDynaBean> billChargeClaims = billChargeClaimsMap
					.get(chargeID);
			for (BasicDynaBean billChargeClaim : billChargeClaims) {
				String claimId = (String) billChargeClaim.get("claim_id");
				Map<String, Object> keys = new HashMap<String, Object>();
				keys.put("charge_id", chargeID);
				keys.put("claim_id", claimId);
				billChargeClaimRepo.update(billChargeClaim, keys);
			}
		}
	}
	
  /**
   * Cancel DRG items claims.
   *
   * @param billNo String
   */
  public void cancelDRGItemsClaims(String billNo) {
    billChargeClaimRepo.cancelDRGItemsClaims(billNo);
  }

  /**
   * Include items in insurance calculation.
   *
   * @param billNo String
   */
  public void includeItemsInInsCalc(String billNo) {
    billChargeClaimRepo.includeItemsInInsCalc(billNo);
  }
  
  /**
   * Sets the items sponsor amount.
   *
   * @param billNo String
   * @return the boolean
   */
  public Boolean setItemsSponsorAmount(String billNo) {
    return billChargeClaimRepo.setItemsSponsorAmount(billNo);
  }
  
  /**
   * Cancel DRG outlier amount entry.
   *
   * @param billNo String
   * @return the boolean
   */
  public Boolean cancelDRGOutlierAmountEntry(String billNo) {
    return billChargeClaimRepo.cancelDRGOutlierAmountEntry(billNo);
  }
  
  /**
   * Cancel add on payment DRG items.
   *
   * @param billNo String
   * @return the boolean
   */
  public Boolean cancelAddOnPaymentDRGItems(String billNo) {
    return billChargeClaimRepo.cancelAddOnPaymentDRGItems(billNo);
  }
  
  public String getClaimId(int planId, String billNo, String visitId) {
    return billChargeClaimRepo.getClaimId(planId, billNo, visitId);
  }

  public void cancelBillChargeClaimAndReference(List<BasicDynaBean> deleteCharges) {
    for(BasicDynaBean bean : deleteCharges){
      Map<String,Object> key = new HashMap<String,Object>();
      key.put("charge_id", (String)bean.get("charge_id"));
      bean.set("insurance_claim_amount", BigDecimal.ZERO);
      billChargeClaimRepo.update(bean,key);
    }
  }

  public boolean updatepackageMarginInBillChgClaim(BasicDynaBean pkgMarginChargeBean){

    String billNo = (String) pkgMarginChargeBean.get("bill_no");
    String charge_id = (String) pkgMarginChargeBean.get("charge_id");
    Map<String,Object> keys = new HashMap<String, Object>();
    keys.put("bill_no", billNo);
    keys.put("priority", 1);
    BasicDynaBean billClaimBean  = billClaimService.findByKey(keys);

    Map<String,Object> billChgClaimKeys = new HashMap<String, Object>();
    billChgClaimKeys.put("charge_id", charge_id);
    billChgClaimKeys.put("claim_id", (String)billClaimBean.get("claim_id"));
    BasicDynaBean billChgClaimBean = findByKey(billChgClaimKeys);

    billChgClaimBean.set("insurance_claim_amt", (BigDecimal)pkgMarginChargeBean.get("insurance_claim_amount"));
    billChgClaimBean.set("amount_included", (BigDecimal)pkgMarginChargeBean.get("amount_included"));
    billChgClaimBean.set("include_in_claim_calc", true);
    update(billChgClaimBean, billChgClaimKeys);

    return true;
  }

  public Boolean updateInBillChargeClaim(BasicDynaBean billChargeBean, String claimId) {
    // TODO Auto-generated method stub
    String chargeId = (String)billChargeBean.get("charge_id");
    BigDecimal insuranceClaimAmt = (BigDecimal)billChargeBean.get("insurance_claim_amount");
    Map<String,Object> keys = new HashMap<String,Object>();
    keys.put("charge_id", chargeId);
    keys.put("claim_id", claimId);
    BasicDynaBean billChargeClaimBean = billChargeClaimRepo.findByKey(keys);
    billChargeClaimBean.set("insurance_claim_amt", insuranceClaimAmt);
    return billChargeClaimRepo.update(billChargeClaimBean, keys) >= 0;
  }

  public List<BasicDynaBean> getCombinedActivities(String claimId, String claimActivityId, Boolean isInternalComplaint) {
    return billChargeClaimRepo.getCombinedActivities(claimId, claimActivityId, isInternalComplaint);
  }
  
  public Integer setClaimAmountAndExclusionBasedOnPreAuth(String visitId) {
    return billChargeClaimRepo.setClaimAmountAndExclusionBasedOnPreAuth(visitId);
  }
  
  public Integer setPriorAuthApprovalAmountAsClaimAmount(int preAuthActId, Object approvedAmount, String preAuthId, Integer preAuthModeId) {
    return billChargeClaimRepo.setPriorAuthApprovalAmountAsClaimAmount(preAuthActId, approvedAmount, preAuthId, preAuthModeId);
  }

}
