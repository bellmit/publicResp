package com.insta.hms.billing;

import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.Registration.PatientInsurancePlanDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.taxation.ItemTaxDetails;
import com.insta.hms.common.taxation.TaxContext;
import com.insta.hms.master.TpaMaster.TpaMasterDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BillChargeTaxBO {
	
	public void setTaxAmounts(ChargeDTO chg, BasicDynaBean patientBean, BasicDynaBean billBean, BasicDynaBean centerBean, 
			BasicDynaBean visitBean, BasicDynaBean tpaBean) throws SQLException{
		// TODO Auto-generated method stub
		
		Map<Integer,Object> taxMap = getBillChargeTaxMap(chg, patientBean, billBean, centerBean,visitBean, tpaBean);
		BigDecimal totTaxAmt = BigDecimal.ZERO;
		for(Map.Entry<Integer, Object> entry : taxMap.entrySet()){
			
			Map<String,String> taxAmtMap =  (Map<String,String>)entry.getValue();
			BigDecimal amt = new BigDecimal(taxAmtMap.get("amount"));
			totTaxAmt = totTaxAmt.add(amt);
		}
		chg.setTaxAmt(totTaxAmt);
	}
	
	public Map<Integer, Object> getBillChargeTaxMap(ChargeDTO chg, BasicDynaBean patientBean, BasicDynaBean billBean,
			BasicDynaBean centerBean, BasicDynaBean visitBean, BasicDynaBean tpaBean) throws SQLException{
		
		Map<Integer, Object> taxMap = new HashMap<Integer, Object>();
		
		ItemTaxDetails itemTaxDetails = new ItemTaxDetails();
		itemTaxDetails.setAmount(chg.getAmount());
		BillingHelper billHelper = new BillingHelper();
		TaxContext taxContext = new TaxContext();
		taxContext.setBillBean(billBean);
		taxContext.setPatientBean(patientBean);
		taxContext.setCenterBean(centerBean);
		taxContext.setVisitBean(visitBean);
		if(null != billBean && (boolean)billBean.get("is_tpa") && null == tpaBean){
			Map identifiers = new HashMap();
			identifiers.put("patient_id", visitBean.get("patient_id"));
			identifiers.put("priority", 1);
			BasicDynaBean patientInsuranceBean = new PatientInsurancePlanDAO().findByKey(identifiers);
			BasicDynaBean sponsorBean = null;
			if(null != patientInsuranceBean && !patientInsuranceBean.getMap().isEmpty()){	
				sponsorBean = new TpaMasterDAO().findByKey("tpa_id", patientInsuranceBean.get("sponsor_id"));
			}	
			taxContext.setItemBean(sponsorBean);	
		}else{
			taxContext.setItemBean(tpaBean);
		}
		
		String itemId = chg.getActDescriptionId();
		String chargeGroup = chg.getChargeGroup(); 
		String chargeHead = chg.getChargeHead();
		int consultationId = chg.getConsultation_type_id();
		String opId = chg.getOp_id();
		if ("PKG".equals(chg.getChargeGroup()) && !"PKGPKG".equals(chg.getChargeHead())) {
			itemId = String.valueOf((int) chg.getPackageId());
		}
				
		if(null != itemId && null != chargeGroup) {
			
			List<BasicDynaBean> subGroupCodes = billHelper.getItemSubgroupCodes(itemId, chargeGroup, chargeHead, consultationId, opId);
			taxMap  = billHelper.getTaxChargesMap(itemTaxDetails,taxContext, subGroupCodes);
		}
		return taxMap;
	}
	
	public Map<Integer, Object> getBillChargeTaxMap(ChargeDTO chg, Connection con) throws SQLException{
		
		Map<Integer, Object> taxMap = new HashMap<Integer, Object>();
		
		ItemTaxDetails itemTaxDetails = new ItemTaxDetails();
		itemTaxDetails.setAmount(chg.getAmount());
		BillingHelper billHelper = new BillingHelper();
		TaxContext taxContext = new TaxContext();
		setOtherContextDetails(con,chg,taxContext);
		
		String itemId = chg.getActDescriptionId();
		String chargeGroup = chg.getChargeGroup(); 
		String chargeHead = chg.getChargeHead();
		int consultationId = chg.getConsultation_type_id();
		String opId = chg.getOp_id();
		if ("PKG".equals(chg.getChargeGroup()) && !"PKGPKG".equals(chg.getChargeHead())) {
			itemId = String.valueOf((int) chg.getPackageId());
		}
				
		if(null != itemId && null != chargeGroup) {
			
			List<BasicDynaBean> subGroupCodes = billHelper.getItemSubgroupCodes(itemId, chargeGroup, chargeHead, consultationId, opId);
			taxMap  = billHelper.getTaxChargesMap(itemTaxDetails,taxContext, subGroupCodes);
		}
		return taxMap;
	}
	
	private void setOtherContextDetails(Connection con, ChargeDTO chg, TaxContext taxContext) throws SQLException {
		BasicDynaBean billBean = BillDAO.getBillBean(con,chg.getBillNo());
		String visitId= "";
		if(null != billBean)
			visitId = (String)billBean.get("visit_id");
		else
			visitId = chg.getVisitId();
		BasicDynaBean visitBean = new VisitDetailsDAO().findByKey(con,"patient_id", visitId);
		BasicDynaBean patientBean = new PatientDetailsDAO().findPatientByMrno(con,(String)visitBean.get("mr_no")).get(0);
		BasicDynaBean centerBean = new GenericDAO("hospital_center_master").findByKey("center_id", visitBean.get("center_id"));
		
		taxContext.setCenterBean(centerBean);
		taxContext.setBillBean(billBean);
		taxContext.setPatientBean(patientBean);
		taxContext.setVisitBean(visitBean);
		if(null != billBean && (boolean)billBean.get("is_tpa")){
			Map identifiers = new HashMap();
			identifiers.put("patient_id", visitId);
			identifiers.put("priority", 1);
			BasicDynaBean patientInsuranceBean = new PatientInsurancePlanDAO().findByKey(con,identifiers);
			BasicDynaBean sponsorBean = null;
			if(null != patientInsuranceBean && !patientInsuranceBean.getMap().isEmpty())
				sponsorBean = new TpaMasterDAO().findByKey("tpa_id", patientInsuranceBean.get("sponsor_id"));
			taxContext.setItemBean(sponsorBean);	
		}
	}

	public List<BasicDynaBean> getBillChargeTaxBeans(Connection con, ChargeDTO charge)throws SQLException{
		Map<Integer, Object> taxMap = new HashMap<Integer, Object>();
		List<BasicDynaBean> chargeTaxList = new ArrayList<BasicDynaBean>();
		taxMap = getBillChargeTaxMap(charge,con);
		
		for(Map.Entry<Integer, Object> entry : taxMap.entrySet()){
			BasicDynaBean bean = new BillChargeTaxDAO().getBean();
			Map<String,String> taxAmtMap =  (Map<String,String>)entry.getValue();
			bean.set("charge_id", charge.getChargeId());
			bean.set("tax_sub_group_id", Integer.parseInt(taxAmtMap.get("tax_sub_group_id")));
			bean.set("tax_rate", new BigDecimal(taxAmtMap.get("rate")));
			bean.set("tax_amount", new BigDecimal(taxAmtMap.get("amount")));
			chargeTaxList.add(bean);
		}
		
		return chargeTaxList;
		
	}
	
	public void getTaxDetails(Map<String, Object> taxInfo, List<BasicDynaBean> subGroupCodes, Map<String, Object> taxInfoMap) {
		Iterator<BasicDynaBean> subGroupCodesIterator = subGroupCodes.iterator();
		while(subGroupCodesIterator.hasNext()) {
			BasicDynaBean subGroupCodesBean = subGroupCodesIterator.next();
			String subGroupCode = (String)subGroupCodesBean.get("subgroup_code");
			//String subGroupName = (String)subGroupCodesBean.get("item_subgroup_name");
			Integer subGroupId = (Integer)subGroupCodesBean.get("item_subgroup_id");
			if(taxInfo.get(subGroupCode) != null){
				taxInfoMap.put(subGroupId.toString(), taxInfo.get(subGroupCode));
			}
		}
	}

}
