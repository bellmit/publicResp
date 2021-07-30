package com.insta.hms.master.DietaryMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.master.rateplan.ItemChargeDAO;

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

public class DietChargesDAO extends ItemChargeDAO{

	static Logger logger = LoggerFactory.getLogger(DietChargesDAO.class);

	public DietChargesDAO() {
		super("diet_charges");
	}

	public void groupUpdateDiets(Connection con, String strOrgid, List<String> bedTypes, List<Integer> selectDiets,
			BigDecimal amount, boolean isPercentage, BigDecimal roundTo, String updateTable) throws SQLException {

		if (roundTo.compareTo(BigDecimal.ZERO) == 0) {
			groupUpdateDietsNoRoundOff(con, strOrgid, bedTypes, selectDiets,
					amount, isPercentage, updateTable);
		}else {
			groupUpdateDietsWithRoundOff(con, strOrgid, bedTypes, selectDiets,
					amount, isPercentage, roundTo, updateTable);
		}
	}

	private String GROUP_INCR_DIET_CHARGES = " UPDATE diet_charges SET charge = GREATEST( round((charge+?)/?,0)*?, 0) "
		+ " WHERE org_id=? ";

	private String GROUP_INCR_DIET_CHARGES_PERCENTAGE = " UPDATE diet_charges SET charge = GREATEST( round(charge*(100+?)/100/?,0)*?, 0) "
			+ " WHERE org_id=? ";

	private String GROUP_INCR_DIET_DISCOUNTS = " UPDATE diet_charges SET discount = LEAST(GREATEST( round((discount+?)/?,0)*?, 0), charge) "
		+ " WHERE org_id=? ";

	private String GROUP_INCR_DIET_DISCOUNT_PERCENTAGE = " UPDATE diet_charges SET discount = LEAST(GREATEST( round(discount*(100+?)/100/?,0)*?, 0), charge) "
			+ " WHERE org_id=? ";

	private String GROUP_APPLY_DISCOUNTS = " UPDATE diet_charges SET discount = LEAST(GREATEST( round((charge+?)/?,0)*?, 0), charge) "
		+ " WHERE org_id=? ";

	private String GROUP_APPLY_DISCOUNT_PERCENTAGE = " UPDATE diet_charges SET discount = LEAST(GREATEST( round(charge+(charge*?/100/?),0)*?, 0), charge) "
			+ " WHERE org_id=? ";


	public void groupUpdateDietsWithRoundOff(Connection con, String strOrgid, List<String> bedTypes, List<Integer> selectDiets,
			BigDecimal amount, boolean isPercentage, BigDecimal roundOff, String updateTable) throws SQLException {

		StringBuilder query = null;
		if(updateTable !=null && updateTable.equals("UPDATECHARGE")) {
			query = new StringBuilder(
				isPercentage ? GROUP_INCR_DIET_CHARGES_PERCENTAGE
						: GROUP_INCR_DIET_CHARGES);

		}else if(updateTable.equals("UPDATEDISCOUNT")) {
			query = new StringBuilder(
				isPercentage ? GROUP_INCR_DIET_DISCOUNT_PERCENTAGE
						: GROUP_INCR_DIET_DISCOUNTS);
		}else {
			query = new StringBuilder(
				isPercentage ? GROUP_APPLY_DISCOUNT_PERCENTAGE
						: GROUP_APPLY_DISCOUNTS);
		}

		SearchQueryBuilder.addWhereFieldOpValue(true, query, "bed_type", "IN",
				bedTypes);
		SearchQueryBuilder.addWhereFieldOpValue(true, query, "diet_id", "IN",
				selectDiets);

		PreparedStatement ps = con.prepareStatement(query.toString());

		int i = 1;
		ps.setBigDecimal(i++, amount);
		ps.setBigDecimal(i++, roundOff);
		ps.setBigDecimal(i++, roundOff);
		ps.setString(i++, strOrgid);

		if (bedTypes != null)
			for (String bedType : bedTypes) {
				ps.setString(i++, bedType);
			}
		if (selectDiets != null)
			for (Integer dietId : selectDiets) {
				ps.setInt(i++, dietId);
			}

		ps.executeUpdate();
		if (ps != null)
			ps.close();
	}

	private String GROUP_INCR_DIET_CHARGES_NO_ROUNDOFF =
		" UPDATE diet_charges SET charge = GREATEST( charge + ?, 0) "
		+ " WHERE org_id=? ";

	private String GROUP_INCR_DIET_CHARGES_PERCENTAGE_NO_ROUNDOFF =
		" UPDATE diet_charges SET charge = GREATEST(charge +(charge * ? / 100 ) , 0) "
			+ " WHERE org_id=? ";

	private String GROUP_INCR_DIET_DISCOUNTS_NO_ROUNDOFF =
		" UPDATE diet_charges SET discount = LEAST(GREATEST(discount + ?, 0), charge) "
		+ " WHERE org_id=? ";

	private String GROUP_INCR_DIET_DISCOUNT_PERCENTAGE_NO_ROUNDOFF =
		" UPDATE diet_charges SET discount = LEAST(GREATEST(discount +( discount * ? / 100 ) , 0), charge) "
			+ " WHERE org_id=? ";

	private String GROUP_APPLY_DISCOUNTS_NO_ROUNDOFF =
		" UPDATE diet_charges SET discount = LEAST(GREATEST(charge + ?, 0), charge) "
		+ " WHERE org_id=? ";

	private String GROUP_APPLY_DISCOUNT_PERCENTAGE_NO_ROUNDOFF =
		" UPDATE diet_charges SET discount = LEAST(GREATEST( charge + (charge * ? / 100) , 0), charge) "
			+ " WHERE org_id=? ";


	public void groupUpdateDietsNoRoundOff(Connection con, String strOrgid, List<String> bedTypes, List<Integer> selectDiets,
			BigDecimal amount, boolean isPercentage, String updateTable) throws SQLException {

		StringBuilder query = null;
		if(updateTable !=null && updateTable.equals("UPDATECHARGE")) {
			query = new StringBuilder(
				isPercentage ?
						GROUP_INCR_DIET_CHARGES_PERCENTAGE_NO_ROUNDOFF
						: GROUP_INCR_DIET_CHARGES_NO_ROUNDOFF);

		}else if(updateTable.equals("UPDATEDISCOUNT")) {
			query = new StringBuilder(
				isPercentage ?
						GROUP_INCR_DIET_DISCOUNT_PERCENTAGE_NO_ROUNDOFF
						: GROUP_INCR_DIET_DISCOUNTS_NO_ROUNDOFF);
		}else {
			query = new StringBuilder(
				isPercentage ?
						GROUP_APPLY_DISCOUNT_PERCENTAGE_NO_ROUNDOFF
						: GROUP_APPLY_DISCOUNTS_NO_ROUNDOFF);
		}

		SearchQueryBuilder.addWhereFieldOpValue(true, query, "bed_type", "IN",
				bedTypes);
		SearchQueryBuilder.addWhereFieldOpValue(true, query, "diet_id", "IN",
				selectDiets);

		PreparedStatement ps = con.prepareStatement(query.toString());

		int i = 1;
		ps.setBigDecimal(i++, amount);
		ps.setString(i++, strOrgid);

		if (bedTypes != null)
			for (String bedType : bedTypes) {
				ps.setString(i++, bedType);
			}
		if (selectDiets != null)
			for (Integer dietId : selectDiets) {
				ps.setInt(i++, dietId);
			}

		ps.executeUpdate();
		if (ps != null)
			ps.close();
	}

	private static final String INSERT_INTO_DIETARY_PLUS="INSERT INTO diet_charges(diet_id,bed_type,org_id,charge)" +
			"(SELECT diet_id,bed_type,?,round(charge + ?) FROM diet_charges  WHERE org_id = ? ) ";

	private static final String INSERT_INTO_DIETARY_MINUS="INSERT INTO diet_charges(diet_id,bed_type,org_id,charge)" +
		"(SELECT diet_id,bed_type,?, GREATEST(round(charge - ?), 0) FROM diet_charges  WHERE org_id = ? ) ";

	private static final String INSER_INTO_DIETARY_BY = "INSERT INTO diet_charges(diet_id,bed_type,org_id,charge)" +
		"(SELECT diet_id,bed_type,?,doroundvarying(charge,?,?) FROM diet_charges  WHERE org_id = ? ) ";

	private static final String INSER_INTO_DIETARY_WITH_DISCOUNTS_BY = "INSERT INTO diet_charges(diet_id,bed_type,org_id,charge, discount)" +
	"(SELECT diet_id,bed_type,?,doroundvarying(charge,?,?),doroundvarying(discount,?,?) FROM diet_charges  WHERE org_id = ? ) ";

	public static boolean addOrgForDietary(Connection con,String newOrgId,String varianceType,
			Double varianceValue,Double varianceBy,boolean useValue,String baseOrgId, Double nearstRoundOfValue) throws Exception {
		return 	addOrgForDietary(con, newOrgId,varianceType, varianceValue,varianceBy,useValue,baseOrgId, nearstRoundOfValue, false);

	}

	public static boolean addOrgForDietary(Connection con,String newOrgId,String varianceType,
		Double varianceValue,Double varianceBy,boolean useValue,String baseOrgId, Double nearstRoundOfValue, boolean updateDiscounts) throws Exception{
 		boolean status = false;
 		PreparedStatement ps = null;
		if(useValue){
			if(varianceType.equals("Incr")){
				ps = con.prepareStatement(INSERT_INTO_DIETARY_PLUS);
				ps.setString(1,newOrgId);
				ps.setDouble(2,varianceValue);
				ps.setString(3,baseOrgId);

				int i = ps.executeUpdate();
				logger.debug(Integer.toString(i));
				if(i>=0)status = true;

			}else{
				ps = con.prepareStatement(INSERT_INTO_DIETARY_MINUS);
				ps.setString(1,newOrgId);
				ps.setDouble(2,varianceValue);
				ps.setString(3,baseOrgId);

				int i = ps.executeUpdate();
				logger.debug(Integer.toString(i));
				if(i>=0)status = true;
			}
		}else{
				/*ps = con.prepareStatement(INSER_INTO_DIETARY_BY);
				ps.setString(1,newOrgId);
				if(!varianceType.equals("Incr")){
				 	varianceBy = new Double(-varianceBy);
				}
				ps.setBigDecimal(2,new BigDecimal(varianceBy));
				ps.setBigDecimal(3, new BigDecimal(nearstRoundOfValue)) ;
				ps.setString(4,baseOrgId);

				int i = ps.executeUpdate();
				logger.debug(i);*/
			int i = insertChargesByPercent(con, newOrgId, baseOrgId, varianceBy, nearstRoundOfValue, updateDiscounts);
			if(i>=0)status = true;

		}
		if (null != ps) ps.close();
 		return status;
	}

	private static int insertChargesByPercent(Connection con, String newOrgId, String baseOrgId,
			Double varianceBy, Double nearstRoundOfValue, boolean updateDiscounts) throws Exception {

		int ndx = 1;
		int numCharges = 1;

		PreparedStatement pstmt = null;
		try {
			pstmt = con.prepareStatement(updateDiscounts ?
					INSER_INTO_DIETARY_WITH_DISCOUNTS_BY : INSER_INTO_DIETARY_BY);
			pstmt.setString(ndx++, newOrgId);

			for (int i = 0; i < numCharges; i++) {
				pstmt.setBigDecimal(ndx++, new BigDecimal(varianceBy));
				pstmt.setBigDecimal(ndx++, new BigDecimal(nearstRoundOfValue));
			}

			if (updateDiscounts) { // go one more round setting the parameters
				for (int i = 0; i < numCharges; i++) {
					pstmt.setBigDecimal(ndx++, new BigDecimal(varianceBy));
					pstmt.setBigDecimal(ndx++, new BigDecimal(nearstRoundOfValue));
				}
			}

			pstmt.setString(ndx++, baseOrgId);

			return pstmt.executeUpdate();

		} finally {

			if (null != pstmt) pstmt.close();

		}
	}

	public boolean initRatePlan(Connection con, String newOrgId,String varianceType,
			Double varianceBy,String baseOrgId, Double roundOff,String userName,String orgName) throws Exception {
		boolean status = false;
		status = addOrgForDietary(con,newOrgId, varianceType, 0.0, varianceBy, false, baseOrgId, roundOff, true);
		return status;
	}

	private static final String UPDATE_RATEPLAN_DIETARY_CHARGES = "UPDATE diet_charges totab SET " +
	" charge = doroundvarying(fromtab.charge,?,?), " +
	" discount = doroundvarying(fromtab.discount,?,?) " +
	" FROM diet_charges fromtab" +
	" WHERE totab.org_id = ? AND fromtab.org_id = ?" +
	" AND totab.diet_id = fromtab.diet_id AND totab.bed_type = fromtab.bed_type";

	public boolean reinitRatePlan(Connection con, String newOrgId, String varianceType,
			Double variance,String baseOrgId, Double rndOff,String userName, String orgName) throws Exception {
		boolean status = false;
		if(!varianceType.equals("Incr")) {
			variance = new Double(-variance);
		}
		BigDecimal varianceBy = new BigDecimal(variance);
	    BigDecimal roundOff = new BigDecimal(rndOff);

		Object updparams[] = {varianceBy, roundOff, varianceBy, roundOff, newOrgId, baseOrgId};
		status = updateCharges(con,UPDATE_RATEPLAN_DIETARY_CHARGES, updparams);
		return status;
	}

	private static String INIT_ITEM_CHARGES = "INSERT INTO diet_charges(diet_id,org_id,bed_type,charge)" +
    "(SELECT ?, abov.org_id, abov.bed_type, 0.0 FROM all_beds_orgs_view abov) ";

    public boolean initItemCharges(Connection con, String dietId, String userName) throws Exception {
        return initItemCharges(con, null, INIT_ITEM_CHARGES, dietId, userName);
    }

	private static final String UPDATE_DIET_CHARGES_PLUS = "UPDATE diet_charges totab SET " +
		" charge = round(fromtab.charge + ?)" +
		" FROM diet_charges fromtab" +
		" WHERE totab.org_id = ? AND fromtab.org_id = ?" +
		" AND totab.diet_id = fromtab.diet_id AND totab.bed_type = fromtab.bed_type";

	private static final String UPDATE_DIET_CHARGES_MINUS = "UPDATE diet_charges totab SET " +
		" charge = GREATEST(round(fromtab.charge - ?), 0)" +
		" FROM diet_charges fromtab" +
		" WHERE totab.org_id = ? AND fromtab.org_id = ?" +
		" AND totab.diet_id = fromtab.diet_id AND totab.bed_type = fromtab.bed_type";

	private static final String UPDATE_DIET_CHARGES_BY = "UPDATE diet_charges totab SET " +
		" charge = doroundvarying(fromtab.charge,?,?)" +
		" FROM diet_charges fromtab" +
		" WHERE totab.org_id = ? AND fromtab.org_id = ?" +
		" AND totab.diet_id = fromtab.diet_id AND totab.bed_type = fromtab.bed_type";

	public static boolean updateOrgForDietCharges(Connection con,String orgId,String varianceType,
		Double varianceValue,Double varianceBy,boolean useValue,String baseOrgId,
		Double nearstRoundOfValue) throws SQLException, IOException {

			boolean status = false;
			PreparedStatement pstmt = null;

			if (useValue) {

			if (varianceType.equals("Incr"))
				pstmt = con.prepareStatement(UPDATE_DIET_CHARGES_PLUS);
			else
				pstmt = con.prepareStatement(UPDATE_DIET_CHARGES_MINUS);

			pstmt.setBigDecimal(1, new BigDecimal(varianceValue));
			pstmt.setString(2, orgId);
			pstmt.setString(3, baseOrgId);

			int i = pstmt.executeUpdate();
			if (i>=0) status = true;

			} else {

			pstmt = con.prepareStatement(UPDATE_DIET_CHARGES_BY);
			if (!varianceType.equals("Incr"))
			varianceBy = new Double(-varianceBy);

			pstmt.setBigDecimal(1, new BigDecimal(varianceBy));
			pstmt.setBigDecimal(2, new BigDecimal(nearstRoundOfValue));
			pstmt.setString(3, orgId);
			pstmt.setString(4, baseOrgId);

			int i = pstmt.executeUpdate();
			if (i>=0) status = true;

			}

			pstmt.close();

		return status;
	}

	public  boolean updateDietChargesForRatePlans(String baseRateSheetId, String[] ratePlanIds,
   		 ArrayList bedType, ArrayList regularcharges,ArrayList discounts,int dietId)throws Exception {
   	Connection con = null;
    	boolean success = false;
    	GenericDAO cdao = new GenericDAO("diet_charges");
    	GenericDAO ratePlanParametersDAO = new GenericDAO("rate_plan_parameters");
    	try {
    		con = DataBaseUtil.getConnection();
    		con.setAutoCommit(false);
	     	for(int i=0; i<ratePlanIds.length; i++) {
	     		Map<String,Object> keys = new HashMap<String, Object>();
	     		keys.put("base_rate_sheet_id", baseRateSheetId);
	     		keys.put("org_id", ratePlanIds[i]);
	     		BasicDynaBean bean = ratePlanParametersDAO.findByKey(keys);
	     		int variation =(Integer)bean.get("rate_variation_percent");
	     		int roundoff = (Integer)bean.get("round_off_amount");
	     		List<BasicDynaBean> chargeList = new ArrayList();

	 	    		for(int k=0 ; k<bedType.size(); k++) {
	 	    			BasicDynaBean charge = cdao.getBean();
	 	    			charge.set("diet_id", dietId);
	 	    			charge.set("org_id", ratePlanIds[i]);
	 	    			charge.set("bed_type", bedType.get(k).toString());
	 	    			String regchg = regularcharges.get(k).toString();
	 	    			String disc = discounts.get(k).toString();
	 	    			Double rpCharge = calculateCharge(new Double(regchg), new Double(variation), roundoff);
	 	    			Double rpDiscount = calculateCharge(new Double(disc), new Double(variation), roundoff);
	 	    			charge.set("charge", new BigDecimal(rpCharge));

	 	    			charge.set("discount", new BigDecimal(rpDiscount));
	 	    			chargeList.add(charge);
	 	    		}

	     		for (BasicDynaBean c: chargeList) {
	     			cdao.updateWithNames(con, c.getMap(),  new String[] {"diet_id", "org_id", "bed_type"});
	     		}
	     		success = true;
	     	}
    	}finally{
    		DataBaseUtil.commitClose(con, success);
    	}
	return success;
    }
}
