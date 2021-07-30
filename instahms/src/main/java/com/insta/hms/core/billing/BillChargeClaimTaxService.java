package com.insta.hms.core.billing;

import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class BillChargeClaimTaxService {
	@LazyAutowired
	private BillChargeClaimTaxRepository billChargeClaimTaxRepo;

	public int insert(BasicDynaBean bean){
		return billChargeClaimTaxRepo.insert(bean);
	}
	
	public int update(BasicDynaBean bean, Map keys){
		return billChargeClaimTaxRepo.update(bean, keys);
	}
	
	public BasicDynaBean findByKey(Map filterMap){
		return billChargeClaimTaxRepo.findByKey(filterMap);
	}

	public BasicDynaBean getBean() {
		return billChargeClaimTaxRepo.getBean();
	}

  public int cancelBillChargeClaimTax(String chargeId) {
    return billChargeClaimTaxRepo.cancelBillChargeClaimTax(chargeId);
  }

  public int[] cancelBillChargeClaimTax(List<String> chargeIdsList) {
    return billChargeClaimTaxRepo.cancelBillChargeClaimTax(chargeIdsList);
  }

	public List<BasicDynaBean> checkForTaxAdjustments(String chargeId) {
		return billChargeClaimTaxRepo.checkForTaxAdjustments(chargeId);
	}
	
	/**
   * Delete bill charge tax.
   *
   * @param chargeId String
   */
  public void deleteBillChargeClaimTax(String chargeId) {
    billChargeClaimTaxRepo.delete(Collections.singletonMap("charge_id",(Object) chargeId));
  }

	public Boolean isBillChargeClaimTaxExist(String chargeId, String claimId,
			Integer taxSubGroupId) {
		// TODO Auto-generated method stub
		return billChargeClaimTaxRepo.isBillChargeClaimTaxExist(chargeId, claimId, taxSubGroupId);
	}
	
	public boolean batchInsert(List<BasicDynaBean> billChargeClaimTaxesToInsert) {
		// TODO Auto-generated method stub
		return billChargeClaimTaxRepo.batchInsert(billChargeClaimTaxesToInsert)[0] >= 0;
	}
	
	public boolean batchUpdate(
			List<BasicDynaBean> billChargeclaimTaxesToUpdate,
			Map<String, Object> updateChgClaimTaxKeysMap) {
		// TODO Auto-generated method stub
		return billChargeClaimTaxRepo.batchUpdate(billChargeclaimTaxesToUpdate, updateChgClaimTaxKeysMap)[0] >= 0;
	
	}
}
