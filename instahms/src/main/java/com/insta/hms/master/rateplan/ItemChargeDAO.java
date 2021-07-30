package com.insta.hms.master.rateplan;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.auditlog.AuditLogDao;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemChargeDAO extends GenericDAO {

	static Logger logger = LoggerFactory.getLogger(ItemChargeDAO.class);
	
    private static final GenericDAO ratePlanParametersDAO = new GenericDAO("rate_plan_parameters");

	public ItemChargeDAO(String chargesTable) {
		super(chargesTable);
	}

    protected void postAuditEntry(Connection con, String auditTable, String userName, String orgName) throws SQLException, IOException {
        boolean status = false;
        status &= new AuditLogDao("Master", auditTable).
	    logMasterChange(con, userName, "INSERT","org_id", orgName);
	}

	protected void switchTrigger(Connection con, String targetTable, String triggerName, String switchAction) throws SQLException {
    	GenericDAO.alterTrigger(con, switchAction, targetTable, triggerName);
    	return;
	}

	protected void disableTrigger(Connection con, String targetTable, String triggerName) throws SQLException {
    	switchTrigger(con, targetTable, triggerName, "DISABLE");
    	return;
	}

	protected void enableTrigger(Connection con, String targetTable, String triggerName) throws SQLException {
    	switchTrigger(con, targetTable, triggerName, "ENABLE");
    	return;
	}

	public boolean updateCharges(Connection con, String updateChargesQuery, Object[] params) throws Exception {
        boolean status = false;
		if (null != updateChargesQuery) {
	        PreparedStatement ps = null;
	        ps = con.prepareStatement(updateChargesQuery);
	        for (int i = 0; i < params.length; i++) {
	        	ps.setObject(i+1, params[i]);
	        }
	        int i = ps.executeUpdate();
	        ps.close();
	        logger.debug(Integer.toString(i));
	        if(i>=0)status = true;
		}
        return status;
    }

	private static final String UPDATE_TIME_STAMP = "UPDATE master_timestamp set master_count = master_count + ? ";
	public boolean updateExclusions(Connection con, String updateExclusionsQuery, String orgId, String baseOrgId) throws Exception {
		return this.updateExclusions(con, updateExclusionsQuery, orgId, baseOrgId, false);
	}

	public boolean updateExclusions(Connection con, String updateExclusionsQuery, String orgId, String baseOrgId, boolean updateMasterTimestamp) throws Exception {
        PreparedStatement ps = null;
        boolean status = false;
        int i = 0;

        if (null != updateExclusionsQuery) {
        	try {
		        ps = con.prepareStatement(updateExclusionsQuery);
		        ps.setString(1, baseOrgId);
		        ps.setString(2, orgId);

		        i = ps.executeUpdate();
		        logger.info("update exclusions :" + i);
        	} finally {
        		if (ps != null) {
        			ps.close();
        		}
        	}
        }

        if (i >= 0) status = true;

        if (i > 0 && updateMasterTimestamp) {
        	try {
	        	ps = con.prepareStatement(UPDATE_TIME_STAMP);
		        ps.setInt(1, i);
		        ps.executeUpdate();
        	} finally {
        		if (ps != null) {
        			ps.close();
        		}
        	}
        }

        return status;
    }

	public boolean initRatePlan(Connection con, String initChargesQuery, String initExclusionsQuery,
    						String newOrgId, String userName) throws Exception {
		String params[];
		if (null != userName) {
			params = new String[] {newOrgId, userName, newOrgId};
		} else {
			params = new String[] {newOrgId, newOrgId};
		}
		return initRatePlan(con, initChargesQuery, initExclusionsQuery, params);
	}

	public boolean initRatePlan(Connection con, String initChargesQuery, String initExclusionsQuery,
			String[] params) throws Exception {
        // update the org_details table first
        boolean status = false;
        PreparedStatement ps = null;

        if (null != initExclusionsQuery) {
	        try {
	            ps = con.prepareStatement(initExclusionsQuery);
	            ps.setString(1,params[0]);
	            int i = ps.executeUpdate();
		        logger.info("init exclusions :" + i);
	            if(i>=0)status = true;
	        } finally {
	            if (ps != null)
	                ps.close();
	        }
        }

        if (null != initChargesQuery) {
	        try {
	            ps = con.prepareStatement(initChargesQuery);
	            for (int paramIndex = 0; paramIndex < params.length; paramIndex++) {
	            	ps.setObject(paramIndex+1,params[paramIndex]);
	            }
	            int i = ps.executeUpdate();
		        logger.info("init charges :" + i);
	            if(i>=0)status = true;
	        } finally {
	            if (ps != null)
	                ps.close();
	        }
        }
        return status;
    }

	protected boolean updateOrgForDerivedRatePlans(Connection con,String[] ratePlanIds, String[] applicable, String orgTbl,
			String category, String categoryId,String categoryIdValue)throws Exception {
    	boolean success = false;
    	GenericDAO odao = new GenericDAO(orgTbl);
    		for(int i=0; i<ratePlanIds.length; i++) {
    			BasicDynaBean bean = odao.getBean();
    			Map<String,Object> keys = new HashMap<String, Object>();
    			keys.put("org_id", ratePlanIds[i]);
    			if(category.equals("consultation") || category.equals("packages") || category.equals("dynapackages"))
    				keys.put(categoryId, Integer.parseInt(categoryIdValue));
    			else
    				keys.put(categoryId, categoryIdValue);
    			if(applicable[i].equals("true"))
    				bean.set("applicable", true);
    			else
    				bean.set("applicable", false);

    			boolean overrideItemCharges = checkItemStatus(con, ratePlanIds[i], orgTbl, category, categoryId,
        				categoryIdValue, applicable[i]);
    			if(overrideItemCharges) bean.set("is_override", "Y");

    			int j = odao.update(con, bean.getMap(), keys);
    			success = j>0;
    		}
    	return success;
    }

	protected  boolean updateChargesForDerivedRatePlans(Connection con,String baseRateSheetId, String[] ratePlanIds,
    		String[] bedType, Double[] regularcharges, String chargeTbl,String orgTbl,
    		String category,String categoryId,String categoryIdValue, Double[] discounts, String[] applicable)throws Exception {
    	boolean success = false;
    	GenericDAO cdao = new GenericDAO(chargeTbl);
    	for(int i=0; i<ratePlanIds.length; i++) {
    		Map<String,Object> keys = new HashMap<String, Object>();
    		keys.put("base_rate_sheet_id", baseRateSheetId);
    		keys.put("org_id", ratePlanIds[i]);
    		BasicDynaBean bean = ratePlanParametersDAO.findByKey(keys);
    		int variation =(Integer)bean.get("rate_variation_percent");
    		int roundoff = (Integer)bean.get("round_off_amount");
    		List<BasicDynaBean> chargeList = new ArrayList();
    		boolean overrided = isChargeOverrided(con,ratePlanIds[i],categoryId,categoryIdValue,category,orgTbl);

    		if(!overrided) {
	    		for(int k=0 ; k<bedType.length; k++) {
	    			BasicDynaBean charge = cdao.getBean();
	    			if(category.equals("consultation") || category.equals("packages"))
	    				charge.set(categoryId, Integer.parseInt(categoryIdValue));
	    			else
	    				charge.set(categoryId, categoryIdValue);
	    			if(category.equals("diagnostics"))
	    				charge.set("org_name", ratePlanIds[i]);
	    			else
	    				charge.set("org_id", ratePlanIds[i]);
	    			charge.set("bed_type", bedType[k]);
	    			Double rpCharge = calculateCharge(regularcharges[k], new Double(variation), roundoff);
	    			Double rpDiscount = calculateCharge(discounts[k], new Double(variation), roundoff);
	    			if(category.equals("services"))
	    				charge.set("unit_charge", new BigDecimal(rpCharge));
	    			else
	    				charge.set("charge", new BigDecimal(rpCharge));

	    			charge.set("discount", new BigDecimal(rpDiscount));
	    			chargeList.add(charge);
	    		}
    		}

    		for (BasicDynaBean c: chargeList) {
    			if(category.equals("diagnostics"))
    				cdao.updateWithNames(con, c.getMap(), new String[] {categoryId, "org_name", "bed_type"});
    			else
    				cdao.updateWithNames(con, c.getMap(),  new String[] {categoryId, "org_id", "bed_type"});
    		}
    		success = true;

    		boolean overrideItemCharges = checkItemStatus(con, ratePlanIds[i], orgTbl, category, categoryId,
    				categoryIdValue, applicable[i]);
    		if(overrideItemCharges)
    			OverrideItemCharges(con, chargeTbl, category, ratePlanIds[i], categoryId, categoryIdValue);
    	}
	return success;
    }

	protected boolean isChargeOverrided(Connection con, String ratePlanId, String categoryId,String categoryIdValue,
			String category, String orgTbl)throws SQLException {
		GenericDAO dao = new GenericDAO(orgTbl);
		Map<String,Object> keys = new HashMap<String, Object>();
		if(category.equals("consultation") || category.equals("packages"))
			keys.put(categoryId, Integer.parseInt(categoryIdValue));
		else
			keys.put(categoryId, categoryIdValue);
		keys.put("org_id", ratePlanId);

		BasicDynaBean bean = dao.findByKey(con, keys);
		String override = (String)bean.get("is_override");

		if(override.equals("Y"))
			return true;
		else
			return false;
	}

	protected  Double calculateCharge(Double rsCharge,Double variance,int roundOff)throws Exception {
	  	Double charge = 0.00;
	  	charge = rsCharge + (rsCharge*variance)/100;
	  	if (roundOff != 0) {
	  		Double x = new Double(roundOff)/2;
		  	x = charge + x;
		  	x = x/roundOff;
		  	int k = x.intValue();
		  	charge = roundOff * new Double(k);
	  	}
	  	return charge;
    }

    protected List<BasicDynaBean> getDerivedRatePlanDetails(String baseRateSheetId,String category,
		String categoryIdValue,String query)throws SQLException {
		Connection con  = null;
		PreparedStatement ps = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(query);
			ps.setString(1, baseRateSheetId);
			if(category.equals("consultation") || category.equals("packages") || category.equals("dynapackages"))
				ps.setInt(2, Integer.parseInt(categoryIdValue));
			else
				ps.setString(2, categoryIdValue);
			ps.setString(3, baseRateSheetId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	protected boolean initItemCharges(Connection con, String initExclusionsQuery,
		String initChargesQuery, Object itemId, String userName) throws SQLException {
        boolean status = false;
        PreparedStatement ps = null;
        //statement one
        if (null != initExclusionsQuery) {
	        try {
	            ps = con.prepareStatement(initExclusionsQuery);
	            ps.setObject(1,itemId);
	            int i = ps.executeUpdate();
	            logger.debug(Integer.toString(i));
	            if(i>=0)status = true;
	        } finally {
	            if (ps != null)
	                ps.close();
	        }
        }
        if (null != initChargesQuery) {
	        try {
	            ps = con.prepareStatement(initChargesQuery);
	            ps.setObject(1,itemId);
	            if (null != userName) {
	            	ps.setString(2,userName);
	            }
	            int i = ps.executeUpdate();
	            logger.debug(Integer.toString(i));

	            if (i>=0) status = true;
	        } finally {
	            if (ps != null)
	                ps.close();
	        }
        }

		return status;
	}

	public boolean updateChargesForDerivedRatePlans(String orgId,String userName,String category,boolean upload)
		throws SQLException,Exception {

		GenericDAO orgDetailsdao = new GenericDAO("organization_details");
		List<BasicDynaBean> derivedRatePlanList = ratePlanParametersDAO.findAllByKey("base_rate_sheet_id", orgId);
		boolean success = false;

		for(int i=0; i<derivedRatePlanList.size(); i++) {

			BasicDynaBean bean = derivedRatePlanList.get(i);
			String ratePlanId = (String)bean.get("org_id");
			Double variation = new Double((Integer)bean.get("rate_variation_percent"));
			Double roundoff = new Double((Integer)bean.get("round_off_amount"));
			String varianceType = variation > 0.00 ? "Incr" : "Decr";

			BasicDynaBean orgDetBean = orgDetailsdao.findByKey("org_id", ratePlanId);
			String orgName = (String)orgDetBean.get("org_name");

			success = UpdateChargesHelper.updateChargesForDerivedRatePlans(ratePlanId,varianceType,0.00,variation,
					orgId,roundoff,userName,orgName,category,upload);
		}
		return success;
	}

	public boolean checkItemStatus(Connection con, String ratePlanId, String orgDetailsTbl, String chargeCategory,
			String categoryId, String categoryValue,String applicable) throws SQLException,Exception {

		boolean success = false;
		GenericDAO dao = new GenericDAO(orgDetailsTbl);
		Map<String,Object> keys = new HashMap<String, Object>();
		keys.put("org_id", ratePlanId);
		if(chargeCategory.equals("consultation") || chargeCategory.equals("dynapackages") || chargeCategory.equals("packages"))
			keys.put(categoryId, Integer.parseInt(categoryValue));
		else
			keys.put(categoryId, categoryValue);
		BasicDynaBean bean = dao.findByKey(con, keys);
		String baseRateSheetId = (String)bean.get("base_rate_sheet_id");

		keys.put("org_id", baseRateSheetId);
		bean = dao.findByKey(con,keys);
		return bean.get("applicable").equals(true) != applicable.equals("true");
	}

	public boolean OverrideItemCharges(Connection con,String chargeTblName, String chargeCategory, String orgId,
			String categoryIdName,String categoryIdValue) throws SQLException,Exception {

		boolean success = false;
		GenericDAO cdao = new GenericDAO(chargeTblName);
		BasicDynaBean cBean = cdao.getBean();
		cBean.set("is_override", "Y");
		Map<String,Object> chKeys = new HashMap<String, Object>();
		if(chargeCategory.equals("diagnostics"))
			chKeys.put("org_name", orgId);
		else
			chKeys.put("org_id", orgId);
		if(chargeCategory.equals("operations")) {
			chKeys.put("op_id", categoryIdValue);
		} else {
			if(chargeCategory.equals("consultation") || chargeCategory.equals("dynapackages") || chargeCategory.equals("packages"))
				chKeys.put(categoryIdName, Integer.parseInt(categoryIdValue));
			else
				chKeys.put(categoryIdName, categoryIdValue);
		}

		success = cdao.update(con, cBean.getMap(), chKeys) > 0;
		return success;
	}

	public boolean updateApplicableFlag(Connection con,String ratePlanId,String orgDetailTblName,
			String categoryIdName, String categoryId,String chargeCategory,String rateSheetId) throws Exception {
		boolean success = false;

		GenericDAO dao = new GenericDAO(orgDetailTblName);
		BasicDynaBean bean = dao.getBean();

		bean.set("org_id", ratePlanId);
		String rateSheet = null;

		rateSheet = getOtherRatesheetId(con,ratePlanId,rateSheetId, orgDetailTblName, categoryIdName, categoryId, chargeCategory);
		if(rateSheet!=null) {
			bean.set("applicable",true);
			bean.set("base_rate_sheet_id", rateSheet);
		}else {
			List<BasicDynaBean> raetSheetList = getRateSheetsByPriority(con, ratePlanId, ratePlanParametersDAO);
			BasicDynaBean prBean = raetSheetList.get(0);
			rateSheet = (String)prBean.get("base_rate_sheet_id");
			bean.set("applicable", false);
			bean.set("base_rate_sheet_id", rateSheet);
		}

		Map<String,Object> keys = new HashMap<String, Object>();
		keys.put("org_id", ratePlanId);
		if(chargeCategory.equals("consultation") || chargeCategory.equals("dynapackages") || chargeCategory.equals("packages")) {
			keys.put(categoryIdName, Integer.parseInt(categoryId));
		} else {
			keys.put(categoryIdName, categoryId);
		}

		int j = dao.update(con, bean.getMap(), keys);
		if(j>0) success =true;
		recalculateCharges(con, ratePlanId, rateSheet, ratePlanParametersDAO, categoryId, chargeCategory);
		return success;

	}

	public boolean recalculateCharges(Connection con,String ratePlanId, String rateSheet,
			GenericDAO rdao,String categoryId,String chargeCategory)throws	Exception {

		boolean success = false;
		Map<String,Object> rKeys = new HashMap<String, Object>();
		rKeys.put("org_id", ratePlanId);
		rKeys.put("base_rate_sheet_id", rateSheet);
		BasicDynaBean rBean = rdao.findByKey(con,rKeys);
		Double varianceBy = new Double((Integer)rBean.get("rate_variation_percent"));
		Double nearstRoundOfValue = new Double((Integer)rBean.get("round_off_amount")) ;
		success = UpdateChargesHelper.updateChargesBasedOnNewRateSheet(con, ratePlanId,varianceBy,
				rateSheet,nearstRoundOfValue, categoryId,chargeCategory);
		return success;
	}

	public String getOtherRatesheetId(Connection con,String ratePlanId,String rateSheetId,
			String orgDetailTblName,String categoryIdName, String categoryId,String chargeCategory)throws SQLException,Exception {
		String newRateSheet = null;
		List<BasicDynaBean> rateSheetList = getRateSheetsByPriority(con, ratePlanId, ratePlanParametersDAO);
		for(int i=0; i<rateSheetList.size(); i++) {
			BasicDynaBean rBean = rateSheetList.get(i);
			String baseRateSheetId = (String)rBean.get("base_rate_sheet_id");
				if(checkItemExistence(con,baseRateSheetId,orgDetailTblName,categoryIdName,categoryId,chargeCategory)) {
					newRateSheet = baseRateSheetId;
					break;
				}
		}
		return newRateSheet;
	}

	public boolean checkItemExistence(Connection con, String rateSheetId,String orgDetailTblName,String categoryIdName,
			String categoryId,String chargeCategory)throws SQLException,Exception {
		boolean success = false;
		GenericDAO odao = new GenericDAO(orgDetailTblName);
		Map<String,Object>keys = new HashMap<String,Object>();
		keys.put("org_id", rateSheetId);
		if(chargeCategory.equals("consultation") || chargeCategory.equals("dynapackages") || chargeCategory.equals("packages")) {
			keys.put(categoryIdName, Integer.parseInt(categoryId));
		} else {
			keys.put(categoryIdName, categoryId);
		}
		BasicDynaBean bean = odao.findByKey(con,keys);

		if(bean.get("applicable").equals(true)) success = true;

		return success;
	}

	public List<BasicDynaBean> getRateSheetsByPriority(Connection con, String orgId,GenericDAO rateParameterDao) throws SQLException {
		Map filterMap = new HashMap();
		filterMap.put("org_id", orgId);
		List<BasicDynaBean> sheets = rateParameterDao.listAll(con, null, filterMap, "priority");
		logger.info("base rate sheet count :" + ((null != sheets) ? sheets.size() : "null"));
		return sheets;
	}

	public boolean updateApplicableflagForDerivedRatePlans(Connection con, List<BasicDynaBean> derivedRatePlanIds,
			String chargeCategory,String categoryIdName, String categoryIdValue,
			String orgDetailTblName,String orgId)throws Exception{
		GenericDAO dao = new GenericDAO(orgDetailTblName);
		boolean success = true;
		Map<String,Object> keys = new HashMap<>();
		if(chargeCategory.equals("consultation") || chargeCategory.equals("dynapackages") || chargeCategory.equals("packages"))
			keys.put(categoryIdName, Integer.parseInt(categoryIdValue));
		else
			keys.put(categoryIdName, categoryIdValue);

		for(int k=0; k<derivedRatePlanIds.size(); k++) {
			BasicDynaBean rBean = derivedRatePlanIds.get(k);
			String ratePlanId = (String)rBean.get("org_id");
			keys.put("org_id", ratePlanId);
			BasicDynaBean rpBean = dao.findByKey(con, keys);
			String isOverrided =  (String) (rpBean.get("is_override") != null
					? rpBean.get("is_override") : "N");
			if(!isOverrided.equals("Y")) {
				success = updateApplicableFlag(con,ratePlanId,orgDetailTblName,categoryIdName,
						categoryIdValue,chargeCategory,orgId);
				if (!success) break;
			}
		}
		return success;
	}

	// expects the query to be of the form
	// UPDATE master set
	// charge1=round(charge1*(100+?)/100/?, 0)*?,
	// charge2=round(charge2*(100+?)/100/?, 0)*?,
	// .. upto
	// chargeN=round(chargeN*(100+?)/100/?, 0)*?,
	// discount1= round(discount1*(100+?)/100/?, 0)*?,
	// discount2= round(discount2*(100+?)/100/?, 0)*?,
	// ...upto
	// discountN= round(discountN*(100+?)/100/?, 0)*?,
	// WHERE org_id = ? and bed_type = ?


	/**
	 * numCharges = N in the above query
	 * discount fields are optional. If a particular master does not have any discount fields, they can be
	 * omitted from the query and in all such cases, updateDiscounts should be passed in as false.
	 */
	public int updateChargesByPercent(Connection con, String sQuery, int numCharges,
											Double varianceBy, Double roundOff, String ratePlanId, String bedType,
											boolean updateDiscounts) throws SQLException {
		int ndx = 1;
		if (0 == new BigDecimal(roundOff).compareTo(BigDecimal.ZERO)) {
			roundOff = new Double(1).doubleValue();
		}

		PreparedStatement pstmt = null;
		try {
			pstmt = con.prepareStatement(sQuery);

			for (int i = 0; i < numCharges; i++) {
				pstmt.setBigDecimal(ndx++, new BigDecimal(varianceBy));
				pstmt.setBigDecimal(ndx++, new BigDecimal(roundOff));
				pstmt.setBigDecimal(ndx++, new BigDecimal(roundOff));
			}

			if (updateDiscounts) { // go one more round setting the parameters
				for (int i = 0; i < numCharges; i++) {
					pstmt.setBigDecimal(ndx++, new BigDecimal(varianceBy));
					pstmt.setBigDecimal(ndx++, new BigDecimal(roundOff));
					pstmt.setBigDecimal(ndx++, new BigDecimal(roundOff));
				}
			}

			pstmt.setString(ndx++, ratePlanId);
			pstmt.setString(ndx++, bedType);

			return pstmt.executeUpdate();

		} finally {

			if (null != pstmt) pstmt.close();

		}
	}

}
