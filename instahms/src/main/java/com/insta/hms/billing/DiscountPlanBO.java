package com.insta.hms.billing;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.Registration.PatientInsurancePlanDAO;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiscountPlanBO {
	
	private int billDiscountPlanId = 0;
	private List<BasicDynaBean> discountPlanDetails = null;
	private static final GenericDAO billDAO = new GenericDAO("bill");
	private static final GenericDAO insurancePlanDetailsDAO = new GenericDAO("insurance_plan_details");
	private static final GenericDAO insurancePlanMainDAO = new GenericDAO("insurance_plan_main");
	
	public void setDiscountPlanDetails(int discountPlanId) throws SQLException{
		this.billDiscountPlanId = discountPlanId;
		this.discountPlanDetails = new GenericDAO("discount_plan_details").
				listAll(null,"discount_plan_id",discountPlanId,"priority");
	}
	
	/*
	 * Method is useful to apply discount rule given charge with amount.
	 * setDiscountPreReq() this should be called before u can use this method.
	 */
	public boolean applyDiscountRule(String billNo)throws SQLException{
		Connection con = null;
		boolean success = false;
		try{
		  con = DataBaseUtil.getConnection();
	    con.setAutoCommit(false);
			success = applyDiscountRule(con, billNo);
		} finally {
				DataBaseUtil.commitClose(con, success);
		}
		return success;
	}
	
	public boolean applyDiscountRule(Connection con, String billNo) throws SQLException{
	  boolean discountCategoryExists = false;
    BasicDynaBean bill = billDAO.findByKey(con,"bill_no",billNo);
			@SuppressWarnings("unchecked")
			List<ChargeDTO> chargeList = new BillBO().getBillDetails(con,billNo).getCharges();
			
      boolean isSystemDisc = false;
      BasicDynaBean visitPrimPlanDetails = new PatientInsurancePlanDAO()
          .getVisitPrimaryPlan((String) bill.get("visit_id"));
      if ((Boolean) bill.get("is_tpa") && null != visitPrimPlanDetails
          && null != visitPrimPlanDetails.get("discount_plan_id")) {
        isSystemDisc = true;
      }
			
      if ( bill.get("discount_category_id") != null ) { 
        setDiscountPlanDetails((Integer)bill.get("discount_category_id"));
      } 
			if(0 != billDiscountPlanId) {
				
				for ( ChargeDTO charge : chargeList ){
				  if(charge.getChargeHead().equals("INVRET")){
				    continue;
				  }
					discountCategoryExists = (Integer)billDiscountPlanId != 0;
					boolean isItemCategoryPayable = isItemCategoryPayable(con , charge);
					//gets suitable discount rule for the charge
					BasicDynaBean discountRule = getDiscountRule(con,charge);
					if(discountCategoryExists && discountRule != null
							&& isPayableAfterDoctorExcluded(charge, isItemCategoryPayable)){
						
						BigDecimal disPerc =  BigDecimal.ZERO;
						BigDecimal disaAmt =  BigDecimal.ZERO;
						
						if ( discountRule != null ) {
							
							if( ((String)discountRule.get("discount_type")).equals("P") ) {
								disPerc = (BigDecimal)discountRule.get("discount_value");
							}
							
							if ( ((String)discountRule.get("discount_type")).equals("A") ) {
								disaAmt = (BigDecimal)discountRule.get("discount_value");
								int result = disaAmt.compareTo( charge.getActRate().multiply(charge.getActQuantity()));
              if (result > -1) {
                disaAmt = charge.getActRate().multiply(charge.getActQuantity());
              } else {
                disaAmt = (BigDecimal) discountRule.get("discount_value");
              }
							} else {
								disaAmt = (charge.getActRate().multiply(charge.getActQuantity())).multiply(disPerc).divide(new BigDecimal(100));
							}
						} 
						
						BigDecimal amt = charge.getActRate().multiply(charge.getActQuantity());
						amt = amt.subtract(disaAmt);
						charge.setAmount(amt);
						charge.setDiscount(disaAmt);
						charge.setOverall_discount_amt(disaAmt);
            charge.setOverall_discount_auth(0);
            if (isSystemDisc)
              charge.setIsSystemDiscount("Y");
            else
              charge.setIsSystemDiscount("N");
          }
				}
				
				//update new amount,discounts
				return new ChargeDAO(con).updateChargeAmountsList(chargeList);
				
			}
    return false; 
	}
	
	
	public BasicDynaBean getDiscountRule(Connection con, ChargeDTO cdto) throws SQLException{
		BasicDynaBean discountRuleBean = null;
		
		/*
		 * applicable_type tells on which to apply discount rule.It can have 3 values.
		 * N  : insurance category id of item in the charge
		 * C  : charge head of the charge
		 * I  : item id of the charge.
		 *    : if it is item id there is one more parameter which will decide which type of item to look at it.
		 */
		for (BasicDynaBean detailBean :  discountPlanDetails ){
		  if ( ((detailBean.get("applicable_type").equals("N")
          && (Integer) cdto.getInsuranceCategoryId() == Integer.parseInt(((String)detailBean.get("applicable_to_id")).trim())))
      || ((detailBean.get("applicable_type").equals("C")
            && cdto.getPackageId()  == null
            && cdto.getChargeHead().equals(((String)detailBean.get("applicable_to_id")).trim()))
            || (cdto.getPackageId()  != null && "PKG".equals(cdto.getChargeGroup())
            && "PKGPKG".equals(((String)detailBean.get("applicable_to_id")).trim())))
      || (detailBean.get("applicable_type").equals("I") 
          && ((cdto.getActDescriptionId()  != null && (cdto.getActDescriptionId()).equals(((String)detailBean.get("applicable_to_id")).trim()))
            || (cdto.getPackageId()  != null && "PKG".equals(cdto.getChargeGroup())
            && (String.valueOf(cdto.getPackageId())).equals(((String)detailBean.get("applicable_to_id")).trim()))))
        ) {
				discountRuleBean = detailBean;
				break;
			}
		}
		return discountRuleBean;
		
	}
	
	public BasicDynaBean getDiscountRule( ChargeDTO cdto) throws SQLException{
		BasicDynaBean discountRuleBean = null;
		
		/*
		 * applicable_type tells on which to apply discount rule.It can have 3 values.
		 * N  : insurance category id of item in the charge
		 * C  : charge head of the charge
		 * I  : item id of the charge.
		 *    : if it is item id there is one more parameter which will decide which type of item to look at it.
		 */
		for (BasicDynaBean detailBean :  discountPlanDetails ){
			if ( (detailBean.get("applicable_type").equals("N") 
						&& cdto.getInsuranceCategoryId() == Integer.parseInt(((String)detailBean.get("applicable_to_id")).trim()) )
			  || (detailBean.get("applicable_type").equals("C") 
					  	&& cdto.getChargeHead().equals(((String)detailBean.get("applicable_to_id")).trim()) )
			  || (detailBean.get("applicable_type").equals("I") 
						&& ((cdto.getActDescriptionId()  != null && cdto.getActDescriptionId().equals(((String)detailBean.get("applicable_to_id")).trim()))
							||(cdto.getPackageId()  != null && "PKG".equals(cdto.getChargeGroup())
							&& (String.valueOf(cdto.getPackageId())).equals(((String)detailBean.get("applicable_to_id")).trim()))))
					) {
				discountRuleBean = detailBean;
				break;
			}
		}
		return discountRuleBean;
		
	}

	public void applyDiscountRule(Connection con,ChargeDTO charge)throws SQLException{
		boolean discountCategoryExists = false;
		if(0 != billDiscountPlanId) {
			discountCategoryExists = billDiscountPlanId != 0;
			boolean isItemCategoryPayable = isItemCategoryPayable(con , charge);
      boolean isSystemDisc = false;
      String billNo = charge.getBillNo();
      if (null != billNo) {
        BasicDynaBean bill = billDAO.findByKey(con, "bill_no", billNo);
        BasicDynaBean visitPrimPlanDetails = new PatientInsurancePlanDAO()
            .getVisitPrimaryPlan((String) bill.get("visit_id"));
        if ((Boolean) bill.get("is_tpa") && null != visitPrimPlanDetails
            && null != visitPrimPlanDetails.get("discount_plan_id")) {
          isSystemDisc = true;
        }
      }
			//gets suitable discount rule for the charge
			BasicDynaBean discountRule = getDiscountRule(con,charge);
			if(discountCategoryExists && discountRule != null
					&& isPayableAfterDoctorExcluded(charge , isItemCategoryPayable)){
				
				BigDecimal disPerc =  BigDecimal.ZERO;
				BigDecimal disaAmt =  BigDecimal.ZERO;
				
				if ( discountRule != null ) {
					
					if( ((String)discountRule.get("discount_type")).equals("P") ) {
						disPerc = (BigDecimal)discountRule.get("discount_value");
					}
					
					if ( ((String)discountRule.get("discount_type")).equals("A") ) {
						disaAmt = (BigDecimal)discountRule.get("discount_value");
						int result = disaAmt.compareTo( charge.getActRate().multiply(charge.getActQuantity()));
						if(result > -1) {
							disaAmt = charge.getActRate().multiply(charge.getActQuantity());
						} else { 
							disaAmt = (BigDecimal)discountRule.get("discount_value");
						}
					} else {
						disaAmt = (charge.getActRate().multiply(charge.getActQuantity())).multiply(disPerc).divide(new BigDecimal(100));
					}
				} 
			//amount might be after rate plan discount.Hence calculating amount again.Overriding rate plan discount.	
				BigDecimal amt = charge.getActRate().multiply(charge.getActQuantity());
				amt = amt.subtract(disaAmt);
				charge.setAmount(amt);
				charge.setDiscount(disaAmt);
				charge.setOverall_discount_amt(disaAmt);
        charge.setOverall_discount_auth(0);
        if (isSystemDisc)
          charge.setIsSystemDiscount("Y");
        else
          charge.setIsSystemDiscount("N");
			}
		}
	}
	
	public void applyDiscountRule(ChargeDTO charge)throws SQLException{
		boolean discountCategoryExists = false;
		if(0 != billDiscountPlanId) {
			discountCategoryExists = billDiscountPlanId != 0;
			boolean isItemCategoryPayable = isItemCategoryPayable(charge);
      boolean isSystemDisc = false;
      String billNo = charge.getBillNo();
      if (null != billNo) {
        BasicDynaBean bill = billDAO.findByKey("bill_no", billNo);
        BasicDynaBean visitPrimPlanDetails = new PatientInsurancePlanDAO()
            .getVisitPrimaryPlan((String) bill.get("visit_id"));
        if ((Boolean) bill.get("is_tpa") && null != visitPrimPlanDetails
            && null != visitPrimPlanDetails.get("discount_plan_id")) {
          isSystemDisc = true;
        }
      }
			//gets suitable discount rule for the charge
			BasicDynaBean discountRule = getDiscountRule(charge);
			if(discountCategoryExists && discountRule != null
					&& isPayableAfterDoctorExcluded(charge, isItemCategoryPayable)){
				
				BigDecimal disPerc =  BigDecimal.ZERO;
				BigDecimal disaAmt =  BigDecimal.ZERO;
				
				if ( discountRule != null ) {
					
					if( ((String)discountRule.get("discount_type")).equals("P") ) {
						disPerc = (BigDecimal)discountRule.get("discount_value");
					}
					
					if ( ((String)discountRule.get("discount_type")).equals("A") ) {
						disaAmt = (BigDecimal)discountRule.get("discount_value");
						int result = disaAmt.compareTo( charge.getActRate().multiply(charge.getActQuantity()));
						if(result > -1) {
							disaAmt = charge.getActRate().multiply(charge.getActQuantity());
						} else {
						  disaAmt = (BigDecimal)discountRule.get("discount_value");
						}	
					} else {
						disaAmt = (charge.getActRate().multiply(charge.getActQuantity())).multiply(disPerc).divide(new BigDecimal(100));
					}
				} 
			//amount might be after rate plan discount.Hence calculating amount again.Overriding rate plan discount.	
				BigDecimal amt = charge.getActRate().multiply(charge.getActQuantity());
				amt = amt.subtract(disaAmt);
				charge.setAmount(amt);
				charge.setDiscount(disaAmt);
				charge.setOverall_discount_amt(disaAmt);
        charge.setOverall_discount_auth(0);
        if (isSystemDisc)
          charge.setIsSystemDiscount("Y");
        else
          charge.setIsSystemDiscount("N");
			}
		}
	}
	
	/** This method will return false when plan is mapped with default discount plan and
	 * insurance category payable is 'No' and bill is tpa bill. In all other cases it will 
	 * return true.  
	 * */
	
	public boolean isItemCategoryPayable(Connection con , ChargeDTO charge) throws SQLException {
		String billNo = charge.getBillNo();
		if(null != billNo) {
			BasicDynaBean billBean = billDAO.findByKey(con, "bill_no", billNo);
			boolean isTpa = (Boolean)billBean.get("is_tpa");
			if(isTpa) {
				Map keyMap = new HashMap();
				keyMap.put("bill_no", billNo);
				keyMap.put("priority", 1);
				
				BasicDynaBean bclBean = new GenericDAO("bill_claim").findByKey(con, keyMap);
				int planId = 0;
				String patientId = (String)billBean.get("visit_id");
				
				if(null != bclBean){
					planId = (Integer)bclBean.get("plan_id");
				}
				if(null == bclBean && null != patientId){
					Map pKeys = new HashMap();
					pKeys.put("patient_id", patientId);
					pKeys.put("priority", 1);
					BasicDynaBean patientPlanBean = new GenericDAO("patient_insurance_plans").findByKey(con,pKeys);
					planId = (Integer)patientPlanBean.get("plan_id");
				}
				
				if(planId != 0){
					BasicDynaBean planBean = insurancePlanMainDAO.findByKey(con, "plan_id", planId);
					int discPlanId = planBean.get("discount_plan_id") != null ? 
							(Integer)planBean.get("discount_plan_id") : 0;
					if(discPlanId > 0) {
						keyMap.clear();
						keyMap.put("plan_id", planId);
						keyMap.put("insurance_category_id", charge.getInsuranceCategoryId());
						keyMap.put("patient_type", charge.getVisitType());
						BasicDynaBean planDetBean = insurancePlanDetailsDAO.findByKey(con, keyMap);
						String is_category_payable  = "N";
						if(planDetBean != null && !planDetBean.getMap().isEmpty()){
							is_category_payable= (String)planDetBean.get("category_payable");
						if(is_category_payable.equals("N")) return false;
						}
					}
				}
			}
		}
		return true;
	}
	
	public boolean isItemCategoryPayable(ChargeDTO charge) throws SQLException {
		Connection con = null;
		try {
			con = DataBaseUtil.getConnection();
			return isItemCategoryPayable(con , charge);
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}
	
	public boolean isItemCategoryPayable(Connection con , int planId , String visitType ,
				int insCatId , boolean isTpaBill) throws SQLException {
		if(planId > 0) {
			Map keyMap = new HashMap();					
			BasicDynaBean planBean = insurancePlanMainDAO.findByKey(con, "plan_id", planId);
			int discPlanId = planBean.get("discount_plan_id") != null ? 
					(Integer)planBean.get("discount_plan_id") : 0;
			if(discPlanId > 0) {
				keyMap.clear();
				keyMap.put("plan_id", planId);
				keyMap.put("insurance_category_id", insCatId);
				keyMap.put("patient_type", visitType);
				BasicDynaBean planDetBean = insurancePlanDetailsDAO.findByKey(con, keyMap);	
				String is_category_payable = "N";
				if(null !=planDetBean && !planDetBean.getMap().isEmpty()){
				  is_category_payable = (String)planDetBean.get("category_payable");
				} 
				if(is_category_payable.equals("N")) return false;
			}
			
		}
		return true;
	}
	
	public boolean isItemCategoryPayable(int planId , String visitType ,
			int insCatId , boolean isTpaBill) throws SQLException {
		Connection con = null;
		try {
			con = DataBaseUtil.getConnection();
			return isItemCategoryPayable(con , planId , visitType , insCatId , isTpaBill);
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	private Boolean isPayableAfterDoctorExcluded(ChargeDTO charge, Boolean isItemCategoryPayable) {
		return ((isItemCategoryPayable && charge.getItemExcludedFromDoctor() == null) ||
				(!isItemCategoryPayable && charge.getItemExcludedFromDoctor() != null
				&& !charge.getItemExcludedFromDoctor()));

	}
}

