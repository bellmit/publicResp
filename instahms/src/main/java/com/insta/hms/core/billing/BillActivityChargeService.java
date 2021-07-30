package com.insta.hms.core.billing;

import com.insta.hms.common.BusinessService;
import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BillActivityChargeService extends BusinessService {
	
	@LazyAutowired
	private BillActivityChargeRepository billActivityChargeRepository;
	
	public List<BasicDynaBean> listByActivityCodeAndId(String activityCode, String activityId) {
		Map<String, Object> filterMap = new HashMap<String, Object>();
		filterMap.put("activity_code", activityCode);
		filterMap.put("activity_id", activityId);
		return billActivityChargeRepository.listAll(null, filterMap, "charge_id");
	}
	
	public Integer delete(String key,String identifier) {
		return billActivityChargeRepository.delete(key, identifier);
	}
	
	public BasicDynaBean getBean() {
		return billActivityChargeRepository.getBean();
	}

	public void insert(BasicDynaBean activityBean) {
		billActivityChargeRepository.insert(activityBean);
	}
	
	public int[] batchInsert(List<BasicDynaBean> activityCharges) {
		return billActivityChargeRepository.batchInsert(activityCharges);
	}

	/**
	 * Get the Activity Charge List
	 * 
	 * @param chargeList
	 * @param prescId
	 * @param activityCode
	 * @param activityConducted
	 * @return
	 */
	public List<BasicDynaBean> getActivityChargeList(List<BasicDynaBean> chargeList, Integer prescId,
			String activityCode, String activityConducted) {
		List<BasicDynaBean> activityChargeList = new ArrayList<BasicDynaBean>();
		for (BasicDynaBean charge : chargeList) {
			if (prescId != 0 && activityCode != null && charge.get("charge_ref") == null) {
				BasicDynaBean billActivityChargeBean = getBean();
				billActivityChargeBean.set("charge_id", charge.get("charge_id"));
				billActivityChargeBean.set("activity_code", activityCode);
				billActivityChargeBean.set("payment_charge_head", charge.get("charge_head"));
				billActivityChargeBean.set("activity_id", String.valueOf(prescId));
				billActivityChargeBean.set("act_description_id", charge.get("act_description_id"));
				billActivityChargeBean.set("doctor_id", charge.get("payee_doctor_id"));
				billActivityChargeBean.set("activity_conducted", activityConducted);
				billActivityChargeBean.set("conducted_datetime", charge.get("conducted_datetime"));
				activityChargeList.add(billActivityChargeBean);
			}
		}
		return activityChargeList;
	}

	/**
	 * Returns Activity Charge Bean
	 * @param chargeId
	 * @param activityCode
	 * @param paymentChargeHead
	 * @param activityId
	 * @param actDescriptionId
	 * @param doctorId
	 * @param activityConducted
	 * @param conductedDateTime
	 * @return
	 */
	public BasicDynaBean getBillActivityChargeBean(String chargeId, String activityCode, String paymentChargeHead,
			String activityId, String actDescriptionId, String doctorId, String activityConducted,
			Timestamp conductedDateTime) {
		BasicDynaBean billActivityChargeBean = getBean();
		billActivityChargeBean.set("charge_id", chargeId);
		billActivityChargeBean.set("activity_code", activityCode);
		billActivityChargeBean.set("payment_charge_head", paymentChargeHead);
		billActivityChargeBean.set("activity_id", activityId);
		billActivityChargeBean.set("act_description_id", actDescriptionId);
		billActivityChargeBean.set("doctor_id", doctorId);
		billActivityChargeBean.set("activity_conducted", activityConducted);
		billActivityChargeBean.set("conducted_datetime", conductedDateTime);
		return billActivityChargeBean;
	}
	
	public List<BasicDynaBean> getChargeActivities(String chargeId) {
		return billActivityChargeRepository.getChargeActivities(new Object[]{chargeId});
	}

	public int update(BasicDynaBean activitybean, Map keys) {
		return billActivityChargeRepository.update(activitybean, keys);		
	}

	public int deleteActivity(String activityCode, String activityId) {
		return	billActivityChargeRepository.deleteActivity(activityCode,activityId);	
	}

	public String getChargeId(String activityCode, String activityId) {
		return billActivityChargeRepository.getChargeId(activityCode,activityId);
	}

  public BasicDynaBean getChargeAndBillDetails(String activityCode, String activityId) {
    return billActivityChargeRepository.getChargeAndBillDetails(activityCode, activityId);
  }
  
  public BasicDynaBean getCharge(String activityCode, String activityId) {
    return billActivityChargeRepository.getCharge(activityCode, activityId);
  }

  public boolean updateActivityDetails(String activityCode, String activityId, String doctorId,
      String conductionStatus, Timestamp conductedDateTime) {
    return billActivityChargeRepository.updateActivityDetails(activityCode, activityId, doctorId,
        conductionStatus, conductedDateTime);
  }

  public BasicDynaBean findByKey(Map<String, Object> filterMap) {
    return billActivityChargeRepository.findByKey(filterMap);
  }

	public BasicDynaBean getActivity(String chargeId) {
		return billActivityChargeRepository.findByKey("charge_id", chargeId);
	}

	public String getBillStatus(String chargeId) {
		return billActivityChargeRepository.getBillChargeStatus(chargeId);
	}
	
	public int delete(Map<String,Object> params){
		return billActivityChargeRepository.delete(params);
	}

	public void updateChargeConsultationType(BasicDynaBean drCharge,
			String codeType) {
		billActivityChargeRepository.updateConsultationTypeChargeActivity(
				drCharge, codeType);
	}

}
