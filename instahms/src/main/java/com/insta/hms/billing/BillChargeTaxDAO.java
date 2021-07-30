package com.insta.hms.billing;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.Registration.PatientInsurancePlanDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.taxation.ItemTaxDetails;
import com.insta.hms.common.taxation.TaxContext;
import com.insta.hms.master.TpaMaster.TpaMasterDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BillChargeTaxDAO extends GenericDAO{

	public BillChargeTaxDAO() {
		super("bill_charge_tax");
	}
	BillingHelper billHelper = new BillingHelper();
	public boolean insertBillChargeTaxes(Connection con, 
			Map<String, List<BasicDynaBean>> insertBillChargeTaxMap) throws IOException, SQLException{
		// TODO Auto-generated method stub
		Boolean success = true; 
		List<BasicDynaBean> billChargeTaxList = new ArrayList<BasicDynaBean>();
		
		for(String key : insertBillChargeTaxMap.keySet()){
			List<BasicDynaBean>taxList = insertBillChargeTaxMap.get(key);
			for(BasicDynaBean bean : taxList){
				bean.set("charge_tax_id", DataBaseUtil.getNextSequence("bill_charge_tax_seq"));
				bean.set("original_tax_amt", bean.get("tax_amount"));
				billChargeTaxList.add(bean);
			}
		}
		if(!billChargeTaxList.isEmpty())
			success = insertAll(con, billChargeTaxList);
		
		return success;
	}
	
	private static final String UPDATE_BILL_CHARGE_TAX = "UPDATE bill_charge_tax set tax_sub_group_id=?, tax_rate=?, tax_amount=? , "
			+ " original_tax_amt =? WHERE charge_id=? AND charge_tax_id=? ";

	public boolean updateBillChargeTaxes(Connection con,
			Map<String, List<BasicDynaBean>> updateBillChargeTaxMap) throws SQLException, IOException{
		// TODO Auto-generated method stub
		Boolean success = true; 
		List<BasicDynaBean> insertChargeList = new ArrayList<BasicDynaBean>();
		
		PreparedStatement ps = null;
		ps = con.prepareStatement(UPDATE_BILL_CHARGE_TAX);
		
		for(String key : updateBillChargeTaxMap.keySet()){
			List<BasicDynaBean>taxList = updateBillChargeTaxMap.get(key);
			for(BasicDynaBean bean : taxList){
				String chargeId = (String) bean.get("charge_id");
				int chargeTaxId = (Integer)bean.get("charge_tax_id");
				
				int serviceSubGrpId = (Integer)bean.get("tax_sub_group_id");
				
				if(chargeTaxId != 0){
					ps.setInt(1, serviceSubGrpId);
					ps.setBigDecimal(2, (BigDecimal)bean.get("tax_rate"));
					ps.setBigDecimal(3, (BigDecimal)bean.get("tax_amount"));
					ps.setBigDecimal(4, (BigDecimal)bean.get("tax_amount"));
					ps.setString(5, chargeId);
					ps.setInt(6, chargeTaxId);
					ps.addBatch();
				}else{
					bean.set("charge_tax_id", DataBaseUtil.getNextSequence("bill_charge_tax_seq"));
					insertChargeList.add(bean);
				}
			}
		}
		
		if(!insertChargeList.isEmpty())
			success = insertAll(con, insertChargeList);
	
		if(null != ps){
			int results[] = ps.executeBatch();
			
			for (int p = 0; p < results.length; p++) {
				if (results[p] <= 0) {
					success = false;
					break;
				}
			}
		}

		if(null != ps) ps.close();
		
		return success;
			
	}
	
	private static final String IS_SERVICE_SUBGROUP_EXISTS = "SELECT * FROM bill_charge_tax WHERE charge_id = ? AND tax_sub_group_id = ? ";

	private boolean isServiceSubGrpExists(Connection con, String chargeId, int serviceSubGrpId) throws SQLException,IOException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement(IS_SERVICE_SUBGROUP_EXISTS);
			ps.setString(1, chargeId);
			ps.setInt(2, serviceSubGrpId);
			rs = ps.executeQuery();
			if (rs.next()) {
				return true;
			}
		} finally{
			if(null != rs) rs.close();
			if(null != ps) ps.close();
		}
		return false;
		
	}
	
	private static final String PACKAGES_TAX_FOR_BILL = "WITH RECURSIVE charge_closure AS ("
			+ " SELECT bc.charge_id package_charge_id,bc.charge_id,"
			+ " bct.tax_amount "
			+ " FROM bill_charge bc"
			+ " LEFT JOIN bill_charge_tax bct ON(bc.charge_id = bct.charge_id) "
			+ " WHERE bc.bill_no = ? and bc.charge_head = 'PKGPKG' "
			+ " UNION "
			+ " SELECT cp.package_charge_id,bc_internal.charge_id,bct.tax_amount"
			+ " FROM "
			+ " bill_charge bc_internal"
			+ " LEFT JOIN bill_charge_tax bct ON(bc_internal.charge_id = bct.charge_id) "
			+ " INNER JOIN charge_closure cp ON cp.charge_id = bc_internal.charge_ref "
			+ " ) select * from charge_closure";
	
	private static final String BILL_CHARGE_TAX = "SELECT  bct.charge_id,bct.tax_sub_group_id,"
			    + "bct.tax_rate,coalesce(cpc.tax_amount,bct.tax_amount) as tax_amount,"
			    + "bct.charge_tax_id,bct.original_tax_amt ,isg.*,ig.*,igt.*  "
				+ " FROM bill_charge bc "
				+ " LEFT JOIN bill_charge_tax bct ON(bc.charge_id = bct.charge_id) " 
				+ " LEFT JOIN item_sub_groups isg ON(bct.tax_sub_group_id = isg.item_subgroup_id) " 
				+ " LEFT JOIN item_groups ig ON(isg.item_group_id = ig.item_group_id) " 
				+ " LEFT JOIN item_group_type igt ON(ig.item_group_type_id = igt.item_group_type_id) "
				+ " LEFT JOIN ( select"
				+ "		package_charge_id, "
				+ " sum(tax_amount) AS tax_amount"
				+ " from ("
				+ 	PACKAGES_TAX_FOR_BILL
				+ ") pcc group by package_charge_id"
				+ ") as  cpc ON bc.charge_head = 'PKGPKG' AND cpc.package_charge_id = bc.charge_id" 
				+ " WHERE (bct.charge_id != '' OR bct.charge_id != null) AND bc.bill_no = ? "
				+ "AND ((bc.charge_group = 'PKG' AND bc.charge_head ='PKGPKG') OR (bc.charge_group != 'PKG'))";
	public static List getPrintTaxChargeDetailsBean(String billNo) throws SQLException {
		return DataBaseUtil.queryToDynaList(BILL_CHARGE_TAX, billNo, billNo);
	}

	@SuppressWarnings("unchecked")
	public void calculateAndUpdateBillChargeTaxes(Connection con , List<ChargeDTO> chargeList, BasicDynaBean bill) throws SQLException, IOException {
		BillingHelper billHelper = new BillingHelper();
		BasicDynaBean visitBean = new VisitDetailsDAO().findByKey("patient_id", (String)bill.get("visit_id"));
		BasicDynaBean patientBean = new PatientDetailsDAO().findByKey("mr_no",(String)visitBean.get("mr_no"));
		BasicDynaBean centerBean = new GenericDAO("hospital_center_master").findByKey("center_id", (Integer)visitBean.get("center_id"));
		
		for (int i = 0; i < chargeList.size(); i++) {
			ChargeDTO taxCharge = (ChargeDTO) chargeList.get(i);
			BasicDynaBean billBean = new GenericDAO("bill").findByKey(con,"bill_no", taxCharge.getBillNo());
			
      if (taxCharge.getChargeHead().equals("INVRET")) {
        continue;
      }
			
			if(taxCharge.getChargeHead().equals("PHCMED") || taxCharge.getChargeHead().equals("PHCRET") ||
			    taxCharge.getChargeHead().equals("PHMED") || taxCharge.getChargeHead().equals("PHRET")){
        continue;
      }
			
			ItemTaxDetails itemTaxDetails = new ItemTaxDetails();
			itemTaxDetails.setAmount(taxCharge.getAmount());
			
			TaxContext taxContext = new TaxContext();
			taxContext.setBillBean(billBean);
			taxContext.setCenterBean(centerBean);
			taxContext.setPatientBean(patientBean);
			taxContext.setVisitBean(visitBean);
			if(null != billBean && (boolean)billBean.get("is_tpa")){
				List<BasicDynaBean> patientInsuranceBeanList =
						new PatientInsurancePlanDAO().getSponsorDetails(con,(String)bill.get("visit_id"));
				if(null != patientInsuranceBeanList && patientInsuranceBeanList.size()>0){
					BasicDynaBean patientInsuranceBean = patientInsuranceBeanList.get(0);
					BasicDynaBean sponsorBean = new TpaMasterDAO().findByKey("tpa_id", patientInsuranceBean.get("sponsor_id"));
					taxContext.setItemBean(sponsorBean);
				}
			}
			
			List<BasicDynaBean> subGroupCodes = new ArrayList<BasicDynaBean>();

			subGroupCodes = billHelper.getItemSubgroupCodes(taxCharge.getChargeId());
			String actDescId = (String) taxCharge.getActDescriptionId();
            if ("PKG".equals(taxCharge.getChargeGroup()) && !"PKGPKG".equals(taxCharge.getChargeHead())) {
                actDescId = String.valueOf((int) taxCharge.getPackageId());
            }
			if(subGroupCodes.isEmpty()){
				subGroupCodes = billHelper.getItemSubgroupCodes(actDescId,
						taxCharge.getChargeGroup(), taxCharge.getChargeHead(), taxCharge.getConsultation_type_id(), taxCharge.getOp_id());
			}else{
				taxContext.setTransactionId(taxCharge.getChargeId());
			}
			
			/* 
			 * If Charge Head is MED we need a method that takes a Charge Id and Calculate tax amounts and update against each sale item id.
			 * Ex: If we know charge id we will get sale id and based on sale id we will get all sale item that are linked with sale id and tax subgroups 
			 * that are linked with sale item id.
			 */ 

			Map<Integer, Object> taxChargesMap  = billHelper.getTaxChargesMap(itemTaxDetails,
					taxContext, subGroupCodes);
						
			BillChargeTaxDAO billChargeTaxDAO = new BillChargeTaxDAO();
			List<BasicDynaBean> chargeBean = billChargeTaxDAO.findAllByKey("charge_id", taxCharge.getChargeId());
			
			Map<String,Object> chargeBeanMap = new HashMap<String, Object>();
			if(null != chargeBean && !chargeBean.isEmpty()){
				chargeBeanMap = ConversionUtils.listBeanToMapBean(chargeBean, "tax_sub_group_id");
			}
			
			for(Map.Entry<Integer, Object> taxMapEntry : taxChargesMap.entrySet()){
				Map<String,Object> taxMapEntryValue = (Map<String, Object>) taxMapEntry.getValue();
				BasicDynaBean taxBean = (BasicDynaBean) chargeBeanMap.get(Integer.parseInt((String) taxMapEntryValue.get("tax_sub_group_id")));
				if(null != taxBean){
					Map<String,Object> keys = new HashMap<String, Object>();
					keys.put("charge_id", taxBean.get("charge_id"));
					keys.put("tax_sub_group_id", taxBean.get("tax_sub_group_id"));
					taxBean.set("tax_rate",  new BigDecimal((String) taxMapEntryValue.get("rate")));
					taxBean.set("tax_amount",   new BigDecimal((String) taxMapEntryValue.get("amount")));
					taxBean.set("original_tax_amt",   new BigDecimal((String) taxMapEntryValue.get("amount")));
					update(con, taxBean.getMap(), keys);
				}
			}				
		}		
	}
	
	private static final String CANCEL_BILL_CHARGE_TAX = "UPDATE bill_charge_tax set tax_amount = 0 , original_tax_amt = 0 WHERE charge_id = ? ";

	public boolean cancelBillChargeTax(Connection con, String chargeId) throws SQLException{
		// TODO Auto-generated method stub
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(CANCEL_BILL_CHARGE_TAX);
			ps.setString(1, chargeId);
			return ps.executeUpdate() > 0 ;
		}finally{
			if(null != ps) ps.close();
		}
		
	}
	
	private static final String GET_ITEM_SUBGROUP_CODES = "SELECT bct.charge_tax_id, bct.tax_sub_group_id as item_subgroup_id, isg.subgroup_code, isg.item_subgroup_name, "+
			" bct.tax_amount, bct.tax_rate, isg.item_group_id,  ig.group_code, bct.original_tax_amt  "+
			" FROM bill_charge_tax bct "+
			" JOIN item_sub_groups isg ON(bct.tax_sub_group_id = isg.item_subgroup_id) "+
			" JOIN item_groups ig ON (ig.item_group_id = isg.item_group_id) " +
			" WHERE bct.charge_id = ? ";

	public List<BasicDynaBean> getItemSubgroupCodes(String chargeId) throws SQLException {
		PreparedStatement ps = null;
		Connection con = DataBaseUtil.getReadOnlyConnection();
		try{
			ps = con.prepareStatement(GET_ITEM_SUBGROUP_CODES);
			ps.setString(1, chargeId);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	public List<BasicDynaBean> getItemSubgroupCodes(Connection con, String chargeId) throws SQLException{
		return DataBaseUtil.queryToDynaList(GET_ITEM_SUBGROUP_CODES, new Object[]{chargeId});
	}
	
	private static final String GET_TAX_SUB_GROUP_DETAILS = "SELECT isg.*, isgt.tax_rate, isgt.validity_start, isgt.validity_end "+
			" FROM item_sub_groups isg "+
			" JOIN item_sub_groups_tax_details isgt ON(isg.item_subgroup_id = isgt.item_subgroup_id) "+
			" WHERE isg.status = 'A' ";
	
	public List<BasicDynaBean> getTaxSubGroupsDetails() throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_TAX_SUB_GROUP_DETAILS);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	
	public List<BasicDynaBean> getTaxSubGroupsDetails(Connection con) throws SQLException{
    PreparedStatement ps = null;
    try{
      ps = con.prepareStatement(GET_TAX_SUB_GROUP_DETAILS);
      return DataBaseUtil.queryToDynaList(ps);
    }finally{
      DataBaseUtil.closeConnections(null, ps);
    }
  }

	private static final String BILL_BEAN_QUERY = "SELECT COALESCE(sum(tax_amt),0.0) AS tax_amount  from bill_claim bc "
			+ "JOIN bill_charge_claim bcc on(bcc.bill_no = bc.bill_no AND bcc.claim_id = bc.claim_id) "
			+ "where bc.bill_no =? AND priority=? ";
	public static BasicDynaBean getPrimarySecondaryPrintTaxDetailsBean(String billNo, Integer priority) throws SQLException {

		return DataBaseUtil.queryToDynaBean(BILL_BEAN_QUERY, new Object[]{billNo, priority});
	}
	
	private static final String GET_ITEM_GROUP_CODES_NAME = "select DISTINCT item_group_name, item_group_id FROM item_groups ";
	public List<BasicDynaBean> getItemGroupCodesName() throws SQLException {
		PreparedStatement ps = null;
		Connection con = DataBaseUtil.getReadOnlyConnection();
		try{
			ps = con.prepareStatement(GET_ITEM_GROUP_CODES_NAME);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	
	private static final String GET_BILL_CHARGE_TAX_ENTRY = " SELECT count(*)::INTEGER AS rec_count FROM bill_charge_tax "
			+ " WHERE charge_id=? AND tax_sub_group_id=? ";
	
	public boolean isBillChargeTaxExist(Connection con, String chargeId, Integer taxSubGroupId) throws SQLException {
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(GET_BILL_CHARGE_TAX_ENTRY);
			ps.setString(1, chargeId);
			ps.setInt(2, taxSubGroupId);
			BasicDynaBean bean = DataBaseUtil.queryToDynaBean(ps);
			if(bean == null) {
				return false;
			} else {
				Integer count = (Integer)bean.get("rec_count");
				if(count > 0) {
					return true;
				} else {
					return false;
				}
			}
		} finally  {
			DataBaseUtil.closeConnections(null, ps);
		}
	}
	
	public void updateBillChargeTaxes(Connection con, BasicDynaBean charge, Boolean isDrgCodeChanged) throws SQLException, IOException {

		ItemTaxDetails itemTaxDetails = new ItemTaxDetails();
		itemTaxDetails.setAmount((BigDecimal) charge.get("amount"));
		TaxContext taxContext = new TaxContext();
		List<BasicDynaBean> subGroupCodes = new ArrayList<>();
		if(!isDrgCodeChanged)
		  subGroupCodes = billHelper.getItemSubgroupCodes((String) charge.get("charge_id"));

		if(subGroupCodes.isEmpty()){
			String actDesc = (String)charge.get("act_description_id");
			if ("PKG".equals(charge.get("charge_group")) && !"PKGPKG".equals(charge.get("charge_head"))) {
				actDesc = String.valueOf((int) charge.get("package_id"));
			}
			subGroupCodes = billHelper.getItemSubgroupCodes(actDesc,
					(String)charge.get("charge_group"), (String)charge.get("charge_head"), 
					(Integer)charge.get("consultation_type_id"), (String)charge.get("op_id"));
		}else{
			taxContext.setTransactionId((String)charge.get("charge_id"));
		}

		Map<Integer, Object> taxChargesMap  = billHelper.getTaxChargesMap(itemTaxDetails,
				taxContext, subGroupCodes);
		if(taxChargesMap.isEmpty() && isDrgCodeChanged){
		  deleteBillChargeTax(con, (String) charge.get("charge_id"));
		  deleteBillChargeClaimTax(con, (String) charge.get("charge_id"));
		}
		BillChargeTaxDAO billChargeTaxDAO = new BillChargeTaxDAO();
		List<BasicDynaBean> chargeBean = billChargeTaxDAO.findAllByKey("charge_id", (String)charge.get("charge_id"));

		if(null != chargeBean && !chargeBean.isEmpty()){
			for(BasicDynaBean taxBean :chargeBean){
			  for(Map.Entry<Integer, Object> taxMapEntry : taxChargesMap.entrySet()){
			    Map<String,Object> taxMapEntryValue = (Map<String, Object>) taxMapEntry.getValue();				
					Map<String,Object> keys = new HashMap<>();
					keys.put("charge_id", taxBean.get("charge_id"));
					keys.put("tax_sub_group_id", taxBean.get("tax_sub_group_id"));
					taxBean.set("tax_sub_group_id",   Integer.valueOf((String)taxMapEntryValue.get("tax_sub_group_id")));
					taxBean.set("original_tax_amt",   new BigDecimal((String) taxMapEntryValue.get("amount")));
					taxBean.set("tax_rate",  new BigDecimal((String) taxMapEntryValue.get("rate")));
					taxBean.set("tax_amount",   new BigDecimal((String) taxMapEntryValue.get("amount")));
					update(con, taxBean.getMap(), keys);
					}
				}
		}else{
			for(Map.Entry<Integer, Object> mapEntry : taxChargesMap.entrySet()){
				Map<String,Object> mapEntryValue = (Map<String, Object>) mapEntry.getValue();
				BasicDynaBean taxBean = billChargeTaxDAO.getBean();
				taxBean.set("charge_id", charge.get("charge_id"));
				taxBean.set("tax_sub_group_id", Integer.parseInt((String) mapEntryValue.get("tax_sub_group_id")));
				taxBean.set("tax_rate",  new BigDecimal((String) mapEntryValue.get("rate")));
				taxBean.set("tax_amount", new BigDecimal((String) mapEntryValue.get("amount")));
				insert(con, taxBean);
			}	
		}
	}
	private static final String UPDATE_BILL_CHARGE_TAX_FOR_EXEMPT = 
			" update bill_charge_tax set tax_amount = coalesce ((select coalesce(sum(sponsor_tax_amount),0) "+ 
			" from bill_charge_claim_tax bclt " +
			" JOIN bill_charge_claim bcl on (bcl.charge_id = bclt.charge_id " +
			" and bcl.claim_id = bclt.claim_id) " +
			" where bclt.charge_id = ? and adj_amt ='Y' group by bclt.charge_id,tax_sub_group_id),0.00) " +
			" where charge_id = ? ";
				 
	
	private static final String CHECK_FOR_TAX_ADJUSTMENTS = "select * from bill_charge_claim_tax where charge_id = ? and adj_amt ='Y' ";
	
	public void updateBillChargeTaxAmountsForExempts(
			List<String> chargeTaxToBeAdjusted) throws SQLException {
		for(String charge: chargeTaxToBeAdjusted){
			PreparedStatement ps = null;
			Connection con = null;
			boolean success = false;
			try{
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);
				//List<BasicDynaBean> taxBeans = DataBaseUtil.queryToDynaList(CHECK_FOR_TAX_ADJUSTMENTS, new Object[]{charge});
				//if(null !=taxBeans && taxBeans.size() >0){
					ps = con.prepareStatement(UPDATE_BILL_CHARGE_TAX_FOR_EXEMPT);
					ps.setString(1, charge);
					ps.setString(2, charge);
					success = ps.executeUpdate()>0;
				//}
			}finally{
				if(null != ps){
					ps.close();
				}
				if(null!= con){ 
					DataBaseUtil.commitClose(con, success);
				}
			}
		}
	}
	
	private static String GET_DETAILS = " select isg.*, ig.group_code from item_sub_groups isg "
			+ " join item_groups ig ON (isg.item_group_id = ig.item_group_id) where isg.item_subgroup_id = ? ";

	public BasicDynaBean getMasterSubGroupDetails(int subGrpID) throws SQLException {
		return DataBaseUtil.queryToDynaBean(GET_DETAILS, new Object[]{subGrpID});
	}

	private static final String DELETE_BILL_CHARGE_CLAIM_TAX_DETAILS = " delete from bill_charge_claim_tax where charge_id in (select charge_id from bill_charge "
			+ " where bill_no = ? )";
	
	public void deleteClaimTax(Connection con, String billNO) throws SQLException {
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(DELETE_BILL_CHARGE_CLAIM_TAX_DETAILS);
			ps.setString(1, billNO);
			ps.executeUpdate();
		}finally{
			if(null != ps) ps.close();
		}		
	}
	
	private static final String DELETE_BILL_CHARGE_TAX_DETAILS = " delete from bill_charge_tax where charge_id = ?";
  
  public void deleteBillChargeTax(Connection con, String chargeId) throws SQLException {
    PreparedStatement ps = null;
    try{
      ps = con.prepareStatement(DELETE_BILL_CHARGE_TAX_DETAILS);
      ps.setString(1, chargeId);
      ps.executeUpdate();
    }finally{
      if(null != ps) ps.close();
    }   
  }
  
  private static final String DELETE_BILL_CHARGE_CLAIM_TAX = " delete from bill_charge_claim_tax where charge_id = ?";
  
  public void deleteBillChargeClaimTax(Connection con, String chargeId) throws SQLException {
    PreparedStatement ps = null;
    try{
      ps = con.prepareStatement(DELETE_BILL_CHARGE_CLAIM_TAX);
      ps.setString(1, chargeId);
      ps.executeUpdate();
    }finally{
      if(null != ps) ps.close();
    }   
  }
  
  private static final String  GET_HOSPITAL_ITEMS_CONTAINING_TOTAL_TAX ="SELECT bct.charge_id from bill_charge_tax bct "
	  		+ " JOIN bill_charge bc ON bc.charge_id=bct.charge_id "
	  		+ " JOIN bill b ON b.bill_no = bc.bill_no "
	  		+ " WHERE b.visit_id = ? AND bc.status='A';";

	public List getAllHospitalItemsContainingTotalTax(String visitId) throws SQLException {
		return DataBaseUtil.queryToDynaList(GET_HOSPITAL_ITEMS_CONTAINING_TOTAL_TAX, new Object[]{visitId});
	}

  public void calculateAndInsertBillChargeTaxes(Connection con, List<ChargeDTO> insertedCharges,
      BasicDynaBean bill) throws SQLException, IOException {

    BillingHelper billHelper = new BillingHelper();
    BasicDynaBean visitBean = new VisitDetailsDAO().findByKey("patient_id", (String)bill.get("visit_id"));
    BasicDynaBean patientBean = new PatientDetailsDAO().findByKey("mr_no",(String)visitBean.get("mr_no"));
    BasicDynaBean centerBean = new GenericDAO("hospital_center_master").findByKey("center_id", (Integer)visitBean.get("center_id"));
    
    for (int i = 0; i < insertedCharges.size(); i++) {
      ChargeDTO taxCharge = (ChargeDTO) insertedCharges.get(i);
      BasicDynaBean billBean = new GenericDAO("bill").findByKey("bill_no", taxCharge.getBillNo());
      
      if(taxCharge.getChargeHead().equals("INVRET")){
        continue;
      }
      
      ItemTaxDetails itemTaxDetails = new ItemTaxDetails();
      itemTaxDetails.setAmount(taxCharge.getAmount());
      
      TaxContext taxContext = new TaxContext();
      taxContext.setBillBean(billBean);
      taxContext.setCenterBean(centerBean);
      taxContext.setPatientBean(patientBean);
      taxContext.setVisitBean(visitBean);
      if(null != billBean && (boolean)billBean.get("is_tpa")){
        List<BasicDynaBean> patientInsuranceBeanList =
            new PatientInsurancePlanDAO().getSponsorDetails(con,(String)bill.get("visit_id"));
        if(null != patientInsuranceBeanList && patientInsuranceBeanList.size()>0){
          BasicDynaBean patientInsuranceBean = patientInsuranceBeanList.get(0);
          BasicDynaBean sponsorBean = new TpaMasterDAO().findByKey("tpa_id", patientInsuranceBean.get("sponsor_id"));
          taxContext.setItemBean(sponsorBean);
        }
      }
      String actDesc = taxCharge.getActDescriptionId();
      if ("PKG".equals(taxCharge.getChargeGroup()) && !"PKGPKG".equals(taxCharge.getChargeHead())) {
         actDesc = String.valueOf((int) taxCharge.getPackageId());
	  }
      List<BasicDynaBean> subGroupCodes = new ArrayList<BasicDynaBean>();
      subGroupCodes = billHelper.getItemSubgroupCodes(actDesc,
            taxCharge.getChargeGroup(), taxCharge.getChargeHead(), taxCharge.getConsultation_type_id(), taxCharge.getOp_id());
      
      /* 
       * If Charge Head is MED we need a method that takes a Charge Id and Calculate tax amounts and update against each sale item id.
       * Ex: If we know charge id we will get sale id and based on sale id we will get all sale item that are linked with sale id and tax subgroups 
       * that are linked with sale item id.
       */ 

      Map<Integer, Object> taxChargesMap  = billHelper.getTaxChargesMap(itemTaxDetails,
          taxContext, subGroupCodes);
     
      for(Map.Entry<Integer, Object> taxMapEntry : taxChargesMap.entrySet()){
        @SuppressWarnings("unchecked")
        Map<String,Object> taxMapEntryValue = (Map<String, Object>) taxMapEntry.getValue();
        BasicDynaBean taxBean = getBean();
        taxBean.set("charge_id", (String)taxCharge.getChargeId());
        taxBean.set("tax_sub_group_id", Integer.parseInt((String)taxMapEntryValue.get("tax_sub_group_id")));
        taxBean.set("tax_rate",  new BigDecimal((String) taxMapEntryValue.get("rate")));
        taxBean.set("tax_amount",   new BigDecimal((String) taxMapEntryValue.get("amount")));
        taxBean.set("original_tax_amt",   new BigDecimal((String) taxMapEntryValue.get("amount")));
        insert(con, taxBean);
      }       
    }   
  }
  
  private static final String GET_TAX_SUB_GROUP_DETAILS_WITH_VALIDITY_FILTER = "SELECT isg.*, COALESCE(isgt.tax_rate,0) as tax_rate, isgt.validity_start, isgt.validity_end "+
      " FROM item_sub_groups isg "+
      " JOIN item_sub_groups_tax_details isgt ON(isg.item_subgroup_id = isgt.item_subgroup_id) "+
      " WHERE isg.status = 'A' " +
      " and isgt.validity_start <= now() "+
      " and (case when isgt.validity_end is null then true "+
      " else isgt.validity_end >= now() end );";
  
  public List<BasicDynaBean> getTaxSubGroupsDetailsWithValidityFilter() throws SQLException{
    Connection con = null;
    PreparedStatement ps = null;
    try{
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_TAX_SUB_GROUP_DETAILS_WITH_VALIDITY_FILTER);
      return DataBaseUtil.queryToDynaList(ps);
    }finally{
      DataBaseUtil.closeConnections(con, ps);
    }
  }
}
