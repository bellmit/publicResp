package com.insta.hms.core.billing;

import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BillClaimService {
	
	static final Logger log = LoggerFactory.getLogger(BillClaimService.class);
	
	@LazyAutowired
	private BillClaimRepository billClaimRepo;

	public String isVisitClaimExists(int planId, String visitId, Integer accGroup) {
		Object[] obj =new Object[]{visitId,planId,accGroup};
		return billClaimRepo.getVisitClaimId(obj);
	}

	public boolean isBillClaimExist(int planId, String visitId, String billNo) {
		String billClaimId = null;		
		billClaimId = getClaimId(planId,billNo,visitId);
		return billClaimId != null && !billClaimId.isEmpty();
	}

	public String getClaimId(int planId, String billNo, String visitId) {
		Object[] obj = new Object[]{visitId,billNo,planId};
		return billClaimRepo.getClaimId(obj);
	}

	public BasicDynaBean getBean() {
		return billClaimRepo.getBean();
	}

	public int insert(BasicDynaBean billClaimBean) {
		return billClaimRepo.insert(billClaimBean);
	}

	public BasicDynaBean findByKey(Map keyMap) {
		List<BasicDynaBean> beans = billClaimRepo.listAll(null, keyMap, null);
		if(null != beans && !beans.isEmpty())
			return beans.get(0);
		else
			return null;
	}

	public boolean closeBillClaim(String billNo, String claimId) {
		Map<String, Object> keyMap = new HashMap<String, Object>();
		keyMap.put("bill_no", billNo);
		keyMap.put("claim_id", claimId);
		BasicDynaBean bcbean = getBillClaim(billNo, claimId);
		if (null != bcbean) {
			bcbean.set("claim_status", "C");
			billClaimRepo.update(bcbean, keyMap);
			return true;
		}
		return false;
		
	}

	private BasicDynaBean getBillClaim(String billNo, String claimId) {
		if (null == billNo || billNo.equals("")) {
			log.error("Invalid Bill No :" + billNo + " , cannot update claim status");
			return null;
		}

		if (null == claimId || claimId.equals("")) {
			log.error("Invalid Claim Id :" + claimId + " , cannot update claim status");
			return null;
		}
		Map<String, Object> keyMap = new HashMap<String, Object>();
		keyMap.put("bill_no", billNo);
		keyMap.put("claim_id", claimId);
		return findByKey(keyMap);
	}
	

	public boolean isClaimClosed(BasicDynaBean b, String claimId) {
		String billNo = (null != b) ? (String)b.get("bill_no") : "";
		BasicDynaBean pbc = getPrimaryBillClaim(billNo);
		if (null != claimId && claimId.equals(pbc.get("claim_id"))) {
			return "R".equals(b.get("primary_claim_status"));
		}
		BasicDynaBean sbc = getSecondaryBillClaim(billNo);
		if (null != claimId && claimId.equals(sbc.get("claim_id"))) {
			return "R".equals(b.get("secondary_claim_status"));
		}
		return false;
	}
	
	public BasicDynaBean getPrimaryBillClaim(String billNo){
		HashMap keyMap = new HashMap();
		keyMap.put("bill_no", billNo);
		keyMap.put("priority", new Integer(1));
		return findByKey(keyMap);
	}

	public BasicDynaBean getSecondaryBillClaim(String billNo){
		HashMap keyMap = new HashMap();
		keyMap.put("bill_no", billNo);
		keyMap.put("priority", new Integer(2));
		return findByKey(keyMap);
	}

	public List<BasicDynaBean> listAll(String billNo) {
		// TODO Auto-generated method stub
		return billClaimRepo.listAll(null, "bill_no", billNo);
	}

	public int update(BasicDynaBean bean, Map<String, Object> billClmkeys) {
		// TODO Auto-generated method stub
		return billClaimRepo.update(bean, billClmkeys);
	}

	public Boolean delete(Map<String, Object> keys) {
		// TODO Auto-generated method stub
		return billClaimRepo.delete(keys) >= 0;
	}

	public Boolean delete(String key, String value){
		// TODO Auto-generated method stub
		return billClaimRepo.delete(key, value) >= 0;
	}

	public List<String> getMrNosByClaimId(List<String> claimList) {
		List<BasicDynaBean> mrNoBeanList = billClaimRepo.getMrNosByClaimId(claimList);
		List<String> mrNoList = new ArrayList<>();
		for(BasicDynaBean mrNoBean : mrNoBeanList) {
			if(StringUtils.isNotBlank((String)mrNoBean.get("mr_no"))){
				mrNoList.add((String)mrNoBean.get("mr_no"));
			}
		}
		return mrNoList;
	}
}
