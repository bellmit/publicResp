package com.insta.hms.core.billing;

import com.insta.hms.common.BusinessService;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.taxation.ItemTaxDetails;
import com.insta.hms.common.taxation.TaxContext;
import com.insta.hms.core.patient.PatientDetailsService;
import com.insta.hms.core.patient.registration.PatientInsurancePlansService;
import com.insta.hms.core.patient.registration.RegistrationService;
import com.insta.hms.mdm.hospitalcenters.HospitalCenterService;
import com.insta.hms.mdm.ordersets.PackageService;
import com.insta.hms.mdm.services.ServicesService;
import com.insta.hms.mdm.tpas.TpaService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class BillChargeTaxService extends BusinessService{
	
	@LazyAutowired
	private BillChargeTaxRepository billChargeTaxRepo;
	
	@LazyAutowired
	private BillTaxCalculator billTaxCalculator;
	
	@LazyAutowired 
	private ServicesService servicesService;
	
	@LazyAutowired
	private BillHelper billHelper;

	@LazyAutowired
	private BillService billService;
	
	@LazyAutowired
	private RegistrationService regService;
	
	@LazyAutowired
	private PatientDetailsService patDetailsService;
	
	@LazyAutowired
	private HospitalCenterService centerService;
	
	@LazyAutowired
	private BillChargeClaimTaxService billChargeClaimTaxService;
	
	@LazyAutowired
	private PatientInsurancePlansService patInsPlanService;
	
	@LazyAutowired
	private TpaService tpaService;
	
	@LazyAutowired
	private HospitalCenterService hospitalCenterService;
	
	@LazyAutowired
	private PatientInsurancePlansService patientInsurancePlansService;
	
	@LazyAutowired
	private PackageService packageService;
	
	public int[] batchInsert(List<BasicDynaBean> chargesList) {
		// TODO Auto-generated method stub
		Map<String,List<BasicDynaBean>> insertBillChargeTaxMap = new HashMap<String, List<BasicDynaBean>>();
		for(BasicDynaBean charge : chargesList){
			String chargeId = (String)charge.get("charge_id");
			List<BasicDynaBean> billChargeTaxBeans = new ArrayList<BasicDynaBean>();
			billChargeTaxBeans = getBillChargeTaxBeans(charge);
			insertBillChargeTaxMap.put(chargeId, billChargeTaxBeans);
		}
		List<BasicDynaBean> billChargeTaxList = new ArrayList<BasicDynaBean>();
		
		for(String key : insertBillChargeTaxMap.keySet()){
			List<BasicDynaBean>taxList = insertBillChargeTaxMap.get(key);
			for(BasicDynaBean bean : taxList){
				billChargeTaxList.add(bean);
			}
		}

		return billChargeTaxRepo.batchInsert(billChargeTaxList);
		
	}
	
	public Map<Integer, Object> getBillChargeTaxMap(BasicDynaBean chg) {
		
		Map<Integer, Object> taxMap = new HashMap<Integer, Object>();
		
		ItemTaxDetails itemTaxDetails = new ItemTaxDetails();
		itemTaxDetails.setAmount((BigDecimal)chg.get("amount"));
		
		TaxContext taxContext = new TaxContext();
		setOtherContextDetails(chg,taxContext);
		
		String itemId = (String)chg.get("act_description_id");
		String chargeGroup = (String)chg.get("charge_group"); 
		String chargeHead = (String)chg.get("charge_head"); 
		int consId = 0;
		if(null != chg.get("consultation_type_id"))
			consId = (Integer)chg.get("consultation_type_id"); 
		String opId = (String)chg.get("op_id");
		Object packId = chg.get("package_id");
		
	    if (null != packId) {
	    	Map<String,Object> paramMap = new HashMap<String, Object>();
			paramMap.put("package_id", packId);
			BasicDynaBean packageBean = packageService.findByPk(paramMap);
			String pkgType =  null != packageBean ? (String)packageBean.get("type") : null;
			
			if(null != pkgType && pkgType.equals("P")) {
				itemId = String.valueOf(packId);
				chargeGroup = "PKG";
			}
	    }
	    
		if(null != itemId && null != chargeGroup) {
			List<BasicDynaBean> subGroupCodes = billHelper.getItemSubgroupCodes(itemId, chargeGroup, chargeHead, consId, opId);
			if(null != subGroupCodes)
				taxMap = billHelper.getTaxChargesMap(itemTaxDetails, taxContext, subGroupCodes);
		}
		return taxMap;
	}
	
    private void setOtherContextDetails(BasicDynaBean chg, TaxContext taxContext) {
    	BasicDynaBean billBean = billService.findByKey((String)chg.get("bill_no"));
		BasicDynaBean visitBean = regService.findByKey((String)billBean.get("visit_id"));
		BasicDynaBean patientBean = patDetailsService.findByKey((String)visitBean.get("mr_no"));
		BasicDynaBean centerBean = centerService.findByKey((Integer)visitBean.get("center_id"));
		Map<String,Object> keys = new HashMap<String,Object>();
		keys.put("patient_id", (String)billBean.get("visit_id"));
		keys.put("priority", 1);
		BasicDynaBean patientTpaBean = patInsPlanService.findByKeys(keys);
		BasicDynaBean tpaBean = null;
		if(null != patientTpaBean){
			Map<String,Object> tpaFilterKeys= new HashMap<String,Object>();
			tpaFilterKeys.put("tpa_id", patientTpaBean.get("sponsor_id"));
			tpaBean = tpaService.findByPk(tpaFilterKeys);
		}
		taxContext.setCenterBean(centerBean);
		taxContext.setBillBean(billBean);
		taxContext.setPatientBean(patientBean);
		taxContext.setVisitBean(visitBean);
		taxContext.setItemBean(tpaBean);
		
	}

//	private List<BasicDynaBean> getItemSubgroupCodes(String itemId,
//			String chargeGroup) {
//		List<BasicDynaBean> subGrpCodes = new ArrayList<BasicDynaBean>();
//		
//		if(chargeGroup.equals("SNP"))
//			subGrpCodes = servicesService.getServiceItemSubGroupTaxDetails(itemId);
//		
//		return subGrpCodes;
//	}

	public List<BasicDynaBean> getBillChargeTaxBeans(BasicDynaBean charge) {
		Map<Integer, Object> taxMap = new HashMap<Integer, Object>();
		List<BasicDynaBean> chargeTaxList = new ArrayList<BasicDynaBean>();
		taxMap = getBillChargeTaxMap(charge);
		
		for(Map.Entry<Integer, Object> entry : taxMap.entrySet()){
			BasicDynaBean bean = billChargeTaxRepo.getBean();
			Map<String,String> taxAmtMap =  (Map<String,String>)entry.getValue();
			bean.set("charge_id", (String)charge.get("charge_id"));
			bean.set("tax_sub_group_id", Integer.parseInt(taxAmtMap.get("tax_sub_group_id")));
			bean.set("tax_rate", new BigDecimal(taxAmtMap.get("rate")));
			bean.set("tax_amount", new BigDecimal(taxAmtMap.get("amount")));
			bean.set("original_tax_amt", new BigDecimal(taxAmtMap.get("amount")));
			chargeTaxList.add(bean);
		}
		
		return chargeTaxList;
		
	}

	public BasicDynaBean getBillChargeTaxBean(String chargeId,
			Integer itemSubGrpId) {
		return billChargeTaxRepo.getbillChargeTaxBean(chargeId, itemSubGrpId);
	}

	public List<BasicDynaBean> getItemSubgroupCodes(String chargeId) {
		return billChargeTaxRepo.getItemSubgroupCodes(chargeId);
	}

	public boolean exist(String key, String value) {
		return billChargeTaxRepo.exist(key, value, false);
	}

  public int[] cancelBillChargeTax(List<String> chargeIdsList) {
    return billChargeTaxRepo.cancelBillChargeTax(chargeIdsList);
  }

	public Boolean updateBillChargeClaimTaxAmtToZero(String billNo) {
		// TODO Auto-generated method stub
		return billChargeTaxRepo.updateBillChargeClaimTaxAmtToZero(billNo);
	}

	public BasicDynaBean getBean() {
		// TODO Auto-generated method stub
		return billChargeTaxRepo.getBean();
	}

	public Boolean isBillChargeTaxExist(String chargeId, Integer taxSubGroupId) {
		// TODO Auto-generated method stub
		return billChargeTaxRepo.isBillChargeTaxExist(chargeId, taxSubGroupId);
	}

	public Boolean insertBillChargeTaxes(
			List<BasicDynaBean> billChargeTaxesToInsert) {
		// TODO Auto-generated method stub
		return billChargeTaxRepo.batchInsert(billChargeTaxesToInsert)[0] >= 0;
	}

	public Boolean insert(
	    BasicDynaBean billChargeTaxesToInsert) {
	  return billChargeTaxRepo.insert(billChargeTaxesToInsert) >= 0;
	}

	public boolean batchUpdate(List<BasicDynaBean> billChargeTaxesToUpdate,
			Map<String, Object> updateKeysMap) {
		// TODO Auto-generated method stub
		return billChargeTaxRepo.batchUpdate(billChargeTaxesToUpdate, updateKeysMap)[0] >= 0;
	}

  @SuppressWarnings("unchecked")
  public void calculateAndUpdateBillChargeTaxes(List<BasicDynaBean> chargesList, String visitId) {

    List<BasicDynaBean> taxBeansList = new ArrayList<>();
    Map<String, Object> keysMap = new HashMap<>();
    List<Object> chargeIdsList = new ArrayList<>();
    List<Object> taxSubGroupIdsList = new ArrayList<>();
    BasicDynaBean patientBean = patDetailsService.getPatientDetailsForVisit(visitId);
    BasicDynaBean visitBean = regService.findByKey(visitId);
    BasicDynaBean centerBean = hospitalCenterService
        .findByKey((Integer) patientBean.get("center_id"));

    for (BasicDynaBean taxCharge : chargesList) {
      if(taxCharge.get("charge_head").equals(BillChargeService.CH_INVENTORY_RETURNS)){
        continue;
      }
      
      if(taxCharge.get("charge_head").equals("PHCMED") || taxCharge.get("charge_head").equals("PHCRET") ||
          taxCharge.get("charge_head").equals("PHMED") || taxCharge.get("charge_head").equals("PHRET")){
        continue;
      }
      
      BasicDynaBean billBean = billService.findByKey((String) taxCharge.get("bill_no"));

      ItemTaxDetails itemTaxDetails = new ItemTaxDetails();
      itemTaxDetails.setAmount((BigDecimal) taxCharge.get("amount"));
      TaxContext taxContext = new TaxContext();
      taxContext.setBillBean(billBean);
      taxContext.setCenterBean(centerBean);
      taxContext.setPatientBean(patientBean);
      taxContext.setVisitBean(visitBean);

      if (null != billBean && (boolean) billBean.get("is_tpa")) {
        List<BasicDynaBean> patientInsuranceBeanList = patientInsurancePlansService
            .getPlanDetails((String) billBean.get("visit_id"));
        if (null != patientInsuranceBeanList && !patientInsuranceBeanList.isEmpty()) {
          BasicDynaBean patientInsuranceBean = patientInsuranceBeanList.get(0);
          Map<String, Object> params = new HashMap<>();
          params.put("tpa_id", patientInsuranceBean.get("sponsor_id"));
          BasicDynaBean sponsorBean = tpaService.findByPk(params);
          taxContext.setItemBean(sponsorBean);
        }
      }

      String chargeId = (String) taxCharge.get("charge_id");

      List<BasicDynaBean> subGroupCodes = billHelper.getItemSubgroupCodes(chargeId);
      String actDescId = (String) taxCharge.get("act_description_id");
      if ("PKG".equals(taxCharge.get("charge_group")) && taxCharge.get("package_id") != null) {
        actDescId = String.valueOf((int) taxCharge.get("package_id"));
      }
      if (subGroupCodes.isEmpty()) {
        subGroupCodes = billHelper.getItemSubgroupCodes(
            actDescId, (String) taxCharge.get("charge_group"),
            (String) taxCharge.get("charge_head"), (Integer) taxCharge.get("consultation_type_id"),
            (String) taxCharge.get("op_id"));
      } else {
        taxContext.setTransactionId(chargeId);
      }

      Map<Integer, Object> taxChargesMap = billHelper.getTaxChargesMap(itemTaxDetails, taxContext,
          subGroupCodes);

      List<BasicDynaBean> chargeBean = billChargeTaxRepo.listAll(null, "charge_id", chargeId);

      Map<String, Object> chargeBeanMap = new HashMap<>();
      if (null != chargeBean && !chargeBean.isEmpty()) {
        chargeBeanMap = ConversionUtils.listBeanToMapBean(chargeBean, "tax_sub_group_id");
      }

      for (Map.Entry<Integer, Object> taxMapEntry : taxChargesMap.entrySet()) {
        Map<String, Object> taxMapEntryValue = (Map<String, Object>) taxMapEntry.getValue();
        BasicDynaBean taxBean = (BasicDynaBean) chargeBeanMap
            .get(Integer.parseInt(String.valueOf(taxMapEntryValue.get("tax_sub_group_id"))));
        if (null != taxBean) {
          chargeIdsList.add(taxBean.get("charge_id"));
          taxSubGroupIdsList.add(taxBean.get("tax_sub_group_id"));
          taxBean.set("tax_rate", new BigDecimal((String) taxMapEntryValue.get("rate")));
          taxBean.set("tax_amount", new BigDecimal((String) taxMapEntryValue.get("amount")));
          taxBean.set("original_tax_amt", new BigDecimal((String) taxMapEntryValue.get("amount")));
          taxBeansList.add(taxBean);
        }
      }
    }
    if (!taxBeansList.isEmpty()) {
      keysMap.put("charge_id", chargeIdsList);
      keysMap.put("tax_sub_group_id", taxSubGroupIdsList);

      billChargeTaxRepo.batchUpdate(taxBeansList, keysMap);
    }
  }

  public BasicDynaBean findByKey(Map<String, Object> chargeTaxMap) {
    return billChargeTaxRepo.findByKey(chargeTaxMap);
  }

  public void updateBillChargeTaxAmountsForExempts(
      List<String> chargeTaxToBeAdjusted) {
    for(String chargeId: chargeTaxToBeAdjusted){
      //List<BasicDynaBean> taxBeans = billChargeClaimTaxService.checkForTaxAdjustments((String)charge.get("charge_id"));
      //if(null !=taxBeans && taxBeans.size() >0){
        billChargeTaxRepo.updateAdjustedTaxAmts(chargeId);
      //}
    }
  } 

  /**
   * Sets the tax amounts.
   *
   * @param charge
   *          the charge
   * @param patientBean
   *          the patient bean
   * @param billBean
   *          the bill bean
   * @param centerBean
   *          the center bean
   * @param visitBean
   *          the visit bean
   * @param tpaBean
   *          the tpa bean
   */
  public void setTaxAmounts(BasicDynaBean charge, BasicDynaBean patientBean, BasicDynaBean billBean,
      BasicDynaBean centerBean, BasicDynaBean visitBean, BasicDynaBean tpaBean) {
    Map<Integer, Object> taxMap = getBillChargeTaxMap(charge, patientBean, billBean, centerBean,
        visitBean, tpaBean);
    BigDecimal totTaxAmt = BigDecimal.ZERO;

    for (Map.Entry<Integer, Object> entry : taxMap.entrySet()) {
      Map<String, String> taxAmtMap = (Map<String, String>) entry.getValue();
      BigDecimal amt = new BigDecimal(taxAmtMap.get("amount"));
      totTaxAmt = totTaxAmt.add(amt);
    }
    charge.set("tax_amt", totTaxAmt);
  }

  /**
   * Gets the bill charge tax map.
   *
   * @param charge
   *          the charge
   * @param patientBean
   *          the patient bean
   * @param billBean
   *          the bill bean
   * @param centerBean
   *          the center bean
   * @param visitBean
   *          the visit bean
   * @param tpaBean
   *          the tpa bean
   * @return the bill charge tax map
   */
  private Map<Integer, Object> getBillChargeTaxMap(BasicDynaBean charge, BasicDynaBean patientBean,
      BasicDynaBean billBean, BasicDynaBean centerBean, BasicDynaBean visitBean,
      BasicDynaBean tpaBean) {
    Map<Integer, Object> taxMap = new HashMap<>();

    ItemTaxDetails itemTaxDetails = new ItemTaxDetails();
    itemTaxDetails.setAmount((BigDecimal) charge.get("amount"));

    TaxContext taxContext = new TaxContext();
    setOtherContextDetails(taxContext, patientBean, billBean, centerBean, visitBean, tpaBean);

    String itemId = (String) charge.get("act_description_id");
    String chargeGroup = (String) charge.get("charge_group");
    String chargeHead = (String) charge.get("charge_head");
    int consId = 0;
    if (null != charge.get("consultation_type_id"))
      consId = (Integer) charge.get("consultation_type_id");
    String opId = (String) charge.get("op_id");

    Object packId = charge.get("package_id");
    
    if (null != packId) {
    	Map<String,Object> paramMap = new HashMap<String, Object>();
		paramMap.put("package_id", packId);
		BasicDynaBean packageBean = packageService.findByPk(paramMap);
		String pkgType =  null != packageBean ? (String)packageBean.get("type") : null;
		
		if(null != pkgType && pkgType.equals("P")) {
			itemId = String.valueOf(packId);
			chargeGroup = "PKG";
		}
    }
    
    if (null != itemId && null != chargeGroup) {
      List<BasicDynaBean> subGroupCodes = billHelper.getItemSubgroupCodes(itemId, chargeGroup,
          chargeHead, consId, opId);

      if (null != subGroupCodes)
        taxMap = billHelper.getTaxChargesMap(itemTaxDetails, taxContext, subGroupCodes);
    }
    return taxMap;
  }

  /**
   * Sets the other context details.
   *
   * @param taxContext
   *          the tax context
   * @param patientBean
   *          the patient bean
   * @param billBean
   *          the bill bean
   * @param centerBean
   *          the center bean
   * @param visitBean
   *          the visit bean
   * @param tpaBean
   *          the tpa bean
   */
  private void setOtherContextDetails(TaxContext taxContext, BasicDynaBean patientBean,
      BasicDynaBean billBean, BasicDynaBean centerBean, BasicDynaBean visitBean,
      BasicDynaBean tpaBean) {

    taxContext.setCenterBean(centerBean);
    taxContext.setBillBean(billBean);
    taxContext.setPatientBean(patientBean);
    taxContext.setVisitBean(visitBean);
    if (null != billBean && (boolean) billBean.get("is_tpa") && null == tpaBean) {
      Map<String, Object> keys = new HashMap<>();
      keys.put("patient_id", (String) billBean.get("visit_id"));
      keys.put("priority", 1);
      BasicDynaBean patientTpaBean = patInsPlanService.findByKeys(keys);
      Map<String, Object> tpaFilterKeys = new HashMap<String, Object>();
      tpaFilterKeys.put("tpa_id", patientTpaBean.get("sponsor_id"));
      tpaBean = tpaService.findByPk(tpaFilterKeys);
    }

    taxContext.setItemBean(tpaBean);
  }
  
  /**
   * Update bill charge taxes.
   *
   * @param charge BasicDynaBean
   * @param isDrgCodeChanged Boolean
   */
  public void updateBillChargeTaxes(BasicDynaBean charge, Boolean isDrgCodeChanged) {

    ItemTaxDetails itemTaxDetails = new ItemTaxDetails();
    itemTaxDetails.setAmount((BigDecimal) charge.get("amount"));
    TaxContext taxContext = new TaxContext();
    List<BasicDynaBean> subGroupCodes = new ArrayList<>();
    if (!isDrgCodeChanged)
      subGroupCodes = billHelper.getItemSubgroupCodes((String) charge.get("charge_id"));

    if (subGroupCodes.isEmpty()) {
      subGroupCodes = billHelper.getItemSubgroupCodes((String) charge.get("act_description_id"),
          (String) charge.get("charge_group"), (String) charge.get("charge_head"),
          (Integer) charge.get("consultation_type_id"), (String) charge.get("op_id"));
    } else {
      taxContext.setTransactionId((String) charge.get("charge_id"));
    }

    Map<Integer, Object> taxChargesMap = billHelper.getTaxChargesMap(itemTaxDetails, taxContext,
        subGroupCodes);
    if (taxChargesMap.isEmpty() && isDrgCodeChanged) {
      deleteBillChargeTax((String) charge.get("charge_id"));
      deleteBillChargeClaimTax((String) charge.get("charge_id"));
    }

    Map <String, Object>key = new HashMap<>();
    key.put("charge_id", charge.get("charge_id"));
    List<BasicDynaBean> chargeBean = billChargeTaxRepo.findAllByKey(key);

    if (null != chargeBean && !chargeBean.isEmpty()) {
      for (BasicDynaBean taxBean : chargeBean) {
        for (Map.Entry<Integer, Object> taxMapEntry : taxChargesMap.entrySet()) {
          Map<String, Object> taxMapEntryValue = (Map<String, Object>) taxMapEntry.getValue();
          Map<String, Object> keys = new HashMap<>();
          keys.put("charge_id", taxBean.get("charge_id"));
          keys.put("tax_sub_group_id", taxBean.get("tax_sub_group_id"));
          taxBean.set("tax_sub_group_id",
              Integer.valueOf((String) taxMapEntryValue.get("tax_sub_group_id")));
          taxBean.set("original_tax_amt", new BigDecimal((String) taxMapEntryValue.get("amount")));
          taxBean.set("tax_rate", new BigDecimal((String) taxMapEntryValue.get("rate")));
          taxBean.set("tax_amount", new BigDecimal((String) taxMapEntryValue.get("amount")));
          billChargeTaxRepo.update(taxBean, keys);
        }
      }
    } else {
      for (Map.Entry<Integer, Object> mapEntry : taxChargesMap.entrySet()) {
        Map<String, Object> mapEntryValue = (Map<String, Object>) mapEntry.getValue();
        BasicDynaBean taxBean = billChargeTaxRepo.getBean();
        taxBean.set("charge_id", charge.get("charge_id"));
        taxBean.set("tax_sub_group_id",
            Integer.parseInt((String) mapEntryValue.get("tax_sub_group_id")));
        taxBean.set("tax_rate", new BigDecimal((String) mapEntryValue.get("rate")));
        taxBean.set("tax_amount", new BigDecimal((String) mapEntryValue.get("amount")));
        billChargeTaxRepo.insert(taxBean);
      }
    }
  }

  /**
   * Delete bill charge tax.
   *
   * @param chargeId String, Object
   */
  public void deleteBillChargeTax(String chargeId) {
    billChargeTaxRepo.delete(Collections.singletonMap("charge_id",(Object) chargeId));
  }

  /**
   * Delete bill charge claim tax.
   *
   * @param chargeId String, Object
   */
  public void deleteBillChargeClaimTax(String chargeId) {
    billChargeClaimTaxService.deleteBillChargeClaimTax(chargeId);
  }

  public Boolean updateBillChargeTax(BasicDynaBean taxItemBean) {
    // TODO Auto-generated method stub
    Map<String,Object> keys = new HashMap<String,Object>();
    keys.put("charge_id", (String)taxItemBean.get("charge_id"));
    keys.put("tax_sub_group_id", (Integer)taxItemBean.get("tax_sub_group_id"));
    return billChargeTaxRepo.update(taxItemBean, keys) >= 0;
  }

   public List<BasicDynaBean> getAllHospitalItemsContainingTotalTax(String visitId) {
	return billChargeTaxRepo.getAllHospitalItemsContainingTotalTax(visitId);
  }

  public BasicDynaBean getMasterSubGroupDetails(int subGrpID) {
		return billChargeTaxRepo.getMasterSubGroupDetails(subGrpID);
	}

  public boolean exist(String keycolumn, String identifier, boolean caseInsensitive) {
    return billChargeTaxRepo.exist(keycolumn, identifier, caseInsensitive);
  }

  public Boolean updateBillChargeTaxAmtToZero(String pkgChargeIdRef) {
    return billChargeTaxRepo.updateBillChargeTaxAmtToZero(pkgChargeIdRef);
  }

}
