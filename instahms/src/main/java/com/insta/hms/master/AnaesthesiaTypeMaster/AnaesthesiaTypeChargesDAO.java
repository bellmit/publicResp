package com.insta.hms.master.AnaesthesiaTypeMaster;

import com.bob.hms.adminmasters.organization.OrgMasterDao;
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

public class AnaesthesiaTypeChargesDAO extends ItemChargeDAO {

	static Logger logger = LoggerFactory.getLogger(AnaesthesiaTypeChargesDAO.class);
	private static final GenericDAO ratePlanParametersDao = new GenericDAO("rate_plan_parameters");

	public AnaesthesiaTypeChargesDAO() {
		super("anesthesia_type_charges");
	}

	public List<BasicDynaBean> getAllChargesForOrgAnaesthesiaType(String orgId, String id) throws SQLException {
		List<String> ids = new ArrayList();
		ids.add(id);
		return getAllChargesForOrg(orgId, ids);
	}

	private static final String GET_ALL_CHARGES_FOR_ORG =
		" SELECT anesthesia_type_id, bed_type,  min_charge, incr_charge ,slab_1_charge," +
		" min_charge_discount, incr_charge_discount, slab_1_charge_discount " +
		" FROM anesthesia_type_charges " +
		" WHERE org_id=?";

	public List<BasicDynaBean> getAllChargesForOrg(String orgId, List<String> ids) throws SQLException {

		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			StringBuilder query = new StringBuilder(GET_ALL_CHARGES_FOR_ORG);
			SearchQueryBuilder.addWhereFieldOpValue(true, query, "anesthesia_type_id", "IN", ids);

			ps = con.prepareStatement(query.toString());

			int i = 1;
			ps.setString(i++, orgId);
			if (ids != null) {
				for (String id : ids) {
					ps.setString(i++, id);
				}
			}
			return DataBaseUtil.queryToDynaList(ps);

		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	/**
	 * Returns Bean with anesthesiaType charge details of a rateplan and bedtype combination
	 * @param anesthesiaTypeId
	 * @param orgId
	 * @param bedType
	 * @return
	 * @throws SQLException
	 */
	private static final String GET_ANASTHESIA_TYPE =
		" SELECT * FROM anesthesia_type_charges atc " +
		"  JOIN anesthesia_type_org_details aod USING (org_id, anesthesia_type_id) " +
		"  JOIN anesthesia_type_master atm using(anesthesia_type_id) " +
		" WHERE org_id=? AND bed_type=? AND anesthesia_type_id=? ";

	public BasicDynaBean getAnaesthesiaTypeCharge(String anesthesiaTypeId, String orgId, String bedType)
		throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_ANASTHESIA_TYPE);
			ps.setString(1, orgId);
			ps.setString(2, bedType);
			ps.setString(3, anesthesiaTypeId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public BasicDynaBean getAnasthesiaTypeCharge(String anesthesiaTypeId, String bedType, String orgId)
		throws SQLException{
		BasicDynaBean anaechargebean = new AnaesthesiaTypeChargesDAO().getAnaesthesiaTypeCharge(anesthesiaTypeId, orgId, bedType);
		if (anaechargebean == null)
			anaechargebean = new AnaesthesiaTypeChargesDAO().getAnaesthesiaTypeCharge(anesthesiaTypeId, "GENERAL", "ORG0001");
		return anaechargebean;
	}

	private static final String COPY_GENERAL_CHARGES_TO_ALL_ORGS =
		" INSERT INTO anesthesia_type_charges " +
		"   (org_id, bed_type, anesthesia_type_id, min_charge, incr_charge, slab_1_charge, " +
		"	min_charge_discount, incr_charge_discount, slab_1_charge_discount) " +
		" SELECT abo.org_id, abo.bed_type,  ac.anesthesia_type_id, ac.min_charge,ac.incr_charge,ac.slab_1_charge, " +
		" ac.min_charge_discount, ac.incr_charge_discount, ac.slab_1_charge_discount " +
		" FROM all_beds_orgs_view abo " +
		"  JOIN anesthesia_type_charges ac ON (ac.anesthesia_type_id=? AND ac.bed_type=abo.bed_type AND ac.org_id='ORG0001') " +
		" WHERE abo.org_id != 'ORG0001'";

	public void copyGeneralChargesToAllOrgs(Connection con, String id) throws SQLException {
		PreparedStatement ps = con.prepareStatement(COPY_GENERAL_CHARGES_TO_ALL_ORGS);
		ps.setString(1, id);
		ps.executeUpdate();
		ps.close();
	}

	/*
	 * Copy the GENERAL bed type charges to all inactive bed types for the given Anesthesia Type ID.
	 * These charges will not be inserted normally when the services is created, since
	 * only active bed charges are input from the user.
	 */
	private static final String COPY_GENERAL_CHARGES_TO_INACTIVE_BEDS =
		" INSERT INTO anesthesia_type_charges " +
		"   (org_id, bed_type, anesthesia_type_id, min_charge, incr_charge, slab_1_charge, " +
		"	 min_charge_discount, incr_charge_discount, slab_1_charge_discount) " +
		" SELECT abo.org_id, abo.bed_type, ac.anesthesia_type_id, ac.min_charge, " +
		"    ac.incr_charge,ac.slab_1_charge,ac.min_charge_discount, ac.incr_charge_discount, ac.slab_1_charge_discount " +
		" FROM all_beds_orgs_view abo " +
		"   JOIN anesthesia_type_charges ac ON (ac.org_id = abo.org_id AND ac.bed_type = 'GENERAL') " +
		" WHERE abo.bed_type IN ( " +
		"     SELECT DISTINCT intensive_bed_type FROM icu_bed_charges WHERE bed_status = 'I' " +
		"     UNION ALL SELECT DISTINCT bed_type FROM bed_details WHERE bed_status = 'I') " +
		"   AND ac.anesthesia_type_id=? " ;

	public void copyGeneralChargesToInactiveBeds(Connection con, String id) throws SQLException {
		PreparedStatement ps = con.prepareStatement(COPY_GENERAL_CHARGES_TO_INACTIVE_BEDS);
		ps.setString(1, id);
		ps.executeUpdate();
		ps.close();
	}

	public void groupIncreaseCharges(Connection con, String orgId, String chargeType,
			List<String> bedTypes, List<String> ids, BigDecimal amount, boolean isPercentage,BigDecimal roundTo, String updateTable)
		throws SQLException {

		if (roundTo.compareTo(BigDecimal.ZERO) == 0) {
			groupIncreaseChargesNoRoundOff(con, orgId, chargeType, bedTypes, ids,
					amount, isPercentage, updateTable);
		}else {
			groupIncreaseChargesWithRoundOff(con, orgId, chargeType, bedTypes, ids,
					amount, isPercentage, roundTo, updateTable);
		}
	}

	private static final String GROUP_INCR_CHARGES =
		" UPDATE anesthesia_type_charges SET # = GREATEST( round((#+?)/?,0)*?, 0) " +
		" WHERE org_id=? ";

	private static final String GROUP_INCR_CHARGES_PERCENTAGE =
		" UPDATE anesthesia_type_charges SET # = GREATEST( round(#*(100+?)/100/?,0)*?, 0) " +
		" WHERE org_id=? ";

	private static String GROUP_INCR_DISCOUNTS =
		" UPDATE anesthesia_type_charges SET # = LEAST(GREATEST( round((#+?)/?,0)*?, 0), @) " +
		" WHERE org_id=? ";

	private static String GROUP_INCR_DISCOUNTS_PERCENTAGE =
		" UPDATE anesthesia_type_charges SET # = LEAST(GREATEST( round(#*(100+?)/100/?,0)*?, 0), @) " +
		" WHERE org_id=? ";

	private String GROUP_APPLY_DISCOUNTS =
		" UPDATE anesthesia_type_charges SET @ = LEAST(GREATEST( round(( # + ?)/?,0)*?, 0), #) " +
		" WHERE org_id=? ";

	private String GROUP_APPLY_DISCOUNTS_PERCENTAGE =
		" UPDATE anesthesia_type_charges SET @ = LEAST(GREATEST( round(#+(# * ?/100/?),0)*?, 0), #) " +
		" WHERE org_id=? ";

	public void groupIncreaseChargesWithRoundOff(Connection con, String orgId, String chargeType,
			List<String> bedTypes, List<String> ids, BigDecimal amount, boolean isPercentage,BigDecimal roundTo, String updateTable)
		throws SQLException {

		StringBuilder query = null;
		if(updateTable !=null && updateTable.equals("UPDATECHARGE")) {
			query = new StringBuilder(
				isPercentage ? GROUP_INCR_CHARGES_PERCENTAGE.replaceAll("#", DataBaseUtil.quoteIdent(chargeType)) :
					GROUP_INCR_CHARGES.replaceAll("#", DataBaseUtil.quoteIdent(chargeType)) );

		}else if(updateTable.equals("UPDATEDISCOUNT")) {

			GROUP_INCR_DISCOUNTS = GROUP_INCR_DISCOUNTS.replaceAll("@", DataBaseUtil.quoteIdent(chargeType));
			GROUP_INCR_DISCOUNTS_PERCENTAGE = GROUP_INCR_DISCOUNTS_PERCENTAGE.replaceAll("@", DataBaseUtil.quoteIdent(chargeType));

			chargeType = chargeType+"_discount";

			query = new StringBuilder(
				isPercentage ? GROUP_INCR_DISCOUNTS_PERCENTAGE.replaceAll("#", DataBaseUtil.quoteIdent(chargeType)) :
					GROUP_INCR_DISCOUNTS.replaceAll("#", DataBaseUtil.quoteIdent(chargeType)) );

		}else {
			String chargeTypeDiscount = chargeType+"_discount";
			if(isPercentage) {
				GROUP_APPLY_DISCOUNTS_PERCENTAGE = GROUP_APPLY_DISCOUNTS_PERCENTAGE.replaceAll("@", DataBaseUtil.quoteIdent(chargeTypeDiscount));
				GROUP_APPLY_DISCOUNTS_PERCENTAGE = GROUP_APPLY_DISCOUNTS_PERCENTAGE.replaceAll("#", DataBaseUtil.quoteIdent(chargeType));
			}else {
				GROUP_APPLY_DISCOUNTS = GROUP_APPLY_DISCOUNTS.replaceAll("@", DataBaseUtil.quoteIdent(chargeTypeDiscount));
				GROUP_APPLY_DISCOUNTS = GROUP_APPLY_DISCOUNTS.replaceAll("#", DataBaseUtil.quoteIdent(chargeType));
			}
			query = new StringBuilder(isPercentage ? GROUP_APPLY_DISCOUNTS_PERCENTAGE :	GROUP_APPLY_DISCOUNTS);
		}

		SearchQueryBuilder.addWhereFieldOpValue(true, query, "bed_type", "IN", bedTypes);
		SearchQueryBuilder.addWhereFieldOpValue(true, query, "anesthesia_type_id", "IN", ids);

		PreparedStatement ps = con.prepareStatement(query.toString());


		int i=1;
		ps.setBigDecimal(i++, amount);
		ps.setBigDecimal(i++, roundTo);
		ps.setBigDecimal(i++, roundTo);		// roundTo appears twice in the query
		ps.setString(i++, orgId);

		if (bedTypes != null) for (String bedType : bedTypes) {
			ps.setString(i++, bedType);
		}
		if (ids != null) for (String id : ids) {
			ps.setString(i++, id);
		}

		ps.executeUpdate();
		if (ps != null) ps.close();
	}

	private static final String GROUP_INCR_CHARGES_NO_ROUNDOFF =
		" UPDATE anesthesia_type_charges SET # = GREATEST( # + ?, 0) " +
		" WHERE org_id=? ";

	private static final String GROUP_INCR_CHARGES_PERCENTAGE_NO_ROUNDOFF =
		" UPDATE anesthesia_type_charges SET # = GREATEST(# +( # * ? / 100 ) , 0) " +
		" WHERE org_id=? ";

	private static String GROUP_INCR_DISCOUNTS_NO_ROUNDOFF =
		" UPDATE anesthesia_type_charges SET # = LEAST(GREATEST( # + ?, 0), @) " +
		" WHERE org_id=? ";

	private static String GROUP_INCR_DISCOUNTS_PERCENTAGE_NO_ROUNDOFF =
		" UPDATE anesthesia_type_charges SET # = LEAST(GREATEST(# +( # * ? / 100 ) , 0), @) " +
		" WHERE org_id=? ";

	private String GROUP_APPLY_DISCOUNTS_NO_ROUNDOFF =
		" UPDATE anesthesia_type_charges SET @ = LEAST(GREATEST( # + ?, 0), #) " +
		" WHERE org_id=? ";

	private String GROUP_APPLY_DISCOUNTS_PERCENTAGE_NO_ROUNDOFF =
		" UPDATE anesthesia_type_charges SET @ = LEAST(GREATEST(# + ( # * ? / 100) , 0), #)  " +
		" WHERE org_id=? ";

	public void groupIncreaseChargesNoRoundOff(Connection con, String orgId, String chargeType,
			List<String> bedTypes, List<String> ids, BigDecimal amount,  boolean isPercentage,
			String updateTable) throws SQLException {

		StringBuilder query = null;
		if(updateTable !=null && updateTable.equals("UPDATECHARGE")) {
			query = new StringBuilder(
				isPercentage ?
					GROUP_INCR_CHARGES_PERCENTAGE_NO_ROUNDOFF
					.replaceAll("#", DataBaseUtil.quoteIdent(chargeType)) :
					GROUP_INCR_CHARGES_NO_ROUNDOFF.replaceAll("#", DataBaseUtil.quoteIdent(chargeType)) );

		}else if(updateTable.equals("UPDATEDISCOUNT")) {

			GROUP_INCR_DISCOUNTS_NO_ROUNDOFF = GROUP_INCR_DISCOUNTS_NO_ROUNDOFF.replaceAll("@", DataBaseUtil.quoteIdent(chargeType));
			GROUP_INCR_DISCOUNTS_PERCENTAGE_NO_ROUNDOFF = GROUP_INCR_DISCOUNTS_PERCENTAGE_NO_ROUNDOFF.replaceAll("@", DataBaseUtil.quoteIdent(chargeType));

			chargeType = chargeType+"_discount";

			query = new StringBuilder(
				isPercentage ?
					GROUP_INCR_DISCOUNTS_PERCENTAGE_NO_ROUNDOFF
					.replaceAll("#", DataBaseUtil.quoteIdent(chargeType)) :
					GROUP_INCR_DISCOUNTS_NO_ROUNDOFF.replaceAll("#", DataBaseUtil.quoteIdent(chargeType)) );

		}else {
			String chargeTypeDiscount = chargeType+"_discount";
			query = new StringBuilder(
					isPercentage ?
						GROUP_APPLY_DISCOUNTS_PERCENTAGE_NO_ROUNDOFF
						.replaceAll("@", DataBaseUtil.quoteIdent(chargeTypeDiscount))
						.replaceAll("#", DataBaseUtil.quoteIdent(chargeType)) :
						GROUP_APPLY_DISCOUNTS_NO_ROUNDOFF.replaceAll("@", DataBaseUtil.quoteIdent(chargeTypeDiscount)).replaceAll("#",
								DataBaseUtil.quoteIdent(chargeType)) );
		}

		SearchQueryBuilder.addWhereFieldOpValue(true, query, "bed_type", "IN", bedTypes);
		SearchQueryBuilder.addWhereFieldOpValue(true, query, "anesthesia_type_id", "IN", ids);

		PreparedStatement ps = con.prepareStatement(query.toString());

		int i=1;
		ps.setBigDecimal(i++, amount);
		ps.setString(i++, orgId);

		if (bedTypes != null) for (String bedType : bedTypes) {
			ps.setString(i++, bedType);
		}
		if (ids != null) for (String id : ids) {
			ps.setString(i++, id);
		}

		ps.executeUpdate();
		if (ps != null) ps.close();
	}

	private static final String UPDATE_CHARGE =
		" UPDATE anesthesia_type_charges " +
		" SET min_charge=?, incr_charge=? ,slab_1_charge=?, " +
		" min_charge_discount=?, incr_charge_discount=?, slab_1_charge_discount=? " +
		" WHERE anesthesia_type_id=? AND org_id=? AND bed_type=?";

	public boolean updateChargeList(Connection con, List<BasicDynaBean> chargeList) throws SQLException {

		PreparedStatement ps = con.prepareStatement(UPDATE_CHARGE);

		for (BasicDynaBean c: chargeList) {
			int i=1;
			ps.setBigDecimal(i++, (BigDecimal) c.get("min_charge"));
			ps.setBigDecimal(i++, (BigDecimal) c.get("incr_charge"));
			ps.setBigDecimal(i++, (BigDecimal) c.get("slab_1_charge"));
			ps.setBigDecimal(i++, (BigDecimal) c.get("tax"));
			ps.setBigDecimal(i++, (BigDecimal) c.get("min_charge_discount"));
			ps.setBigDecimal(i++, (BigDecimal) c.get("incr_charge_discount"));
			ps.setBigDecimal(i++, (BigDecimal) c.get("slab_1_charge_discount"));
			ps.setString(i++, (String) c.get("anesthesia_type_id"));
			ps.setString(i++, (String) c.get("org_id"));
			ps.setString(i++, (String) c.get("bed_type"));
			ps.addBatch();
		}

		int results[] = ps.executeBatch();
		boolean status = DataBaseUtil.checkBatchUpdates(results);
		ps.close();
		return status;
	}

	private static final String INSERT_INTO_ANAESTHESIA_TYPE_PLUS = "INSERT INTO anesthesia_type_charges(anesthesia_type_id,org_id,bed_type," +
		"min_charge,slab_1_charge,incr_charge)(SELECT anesthesia_type_id,?,bed_type,round(min_charge + ?),round(slab_1_charge + ?)," +
		"round(incr_charge+?) FROM anesthesia_type_charges where org_id=? )";

	private static final String INSERT_INTO_ANAESTHESIA_TYPE_MINUS = "INSERT INTO anesthesia_type_charges(anesthesia_type_id,org_id,bed_type," +
			"min_charge,slab_1_charge,incr_charge)(SELECT anesthesia_type_id,?,bed_type, GREATEST(round(min_charge - ?), 0), GREATEST(round(slab_1_charge - ?), 0)," +
			"GREATEST(round(incr_charge - ?), 0) FROM anesthesia_type_charges where org_id=? )";

	private static final String INSERT_INTO_ANAESTHESIA_TYPE_BY = "INSERT INTO anesthesia_type_charges(anesthesia_type_id,org_id,bed_type," +
			"min_charge,slab_1_charge,incr_charge)(SELECT anesthesia_type_id,?,bed_type," +
			"doroundvarying(min_charge,?,?),doroundvarying(slab_1_charge,?,?),doroundvarying(incr_charge,?,?) FROM anesthesia_type_charges WHERE org_id=?)";

	private static final String INSERT_INTO_ANAES_WITH_DISCOUNT_BY = "INSERT INTO anesthesia_type_charges(anesthesia_type_id,org_id,bed_type," +
	"min_charge,slab_1_charge,incr_charge, " +
	"min_charge_discount,slab_1_charge_discount,incr_charge_discount" +
	")" +
	"(SELECT anesthesia_type_id,?,bed_type," +
	"doroundvarying(min_charge,?,?),doroundvarying(slab_1_charge,?,?),doroundvarying(incr_charge,?,?), " +
	"doroundvarying(min_charge_discount,?,?),doroundvarying(slab_1_charge_discount,?,?),doroundvarying(incr_charge_discount,?,?) " +
	"FROM anesthesia_type_charges WHERE org_id=?)";

	public static boolean addOrgForAnesthesiaType(Connection con,String newOrgId,String varianceType,
			Double varianceValue,Double varianceBy,boolean useValue,String baseOrgId,
			Double nearstRoundOfValue) throws Exception{

		return addOrgForAnesthesiaType(con,newOrgId,varianceType,varianceValue,varianceBy,useValue,baseOrgId,
				nearstRoundOfValue, false);
	}

	public static boolean addOrgForAnesthesiaType(Connection con,String newOrgId,String varianceType,
			Double varianceValue,Double varianceBy,boolean useValue,String baseOrgId,
			Double nearstRoundOfValue, boolean updateDiscounts) throws Exception{

	  		boolean status = false;
	  		PreparedStatement ps = null;
	  		if(useValue){
	  			if(varianceType.equals("Incr")){
	  				ps = con.prepareStatement(INSERT_INTO_ANAESTHESIA_TYPE_PLUS);
	  			}else{
	  				ps = con.prepareStatement(INSERT_INTO_ANAESTHESIA_TYPE_MINUS);
	  			}

	  			ps.setString(1, newOrgId);
	  			ps.setBigDecimal(2, new BigDecimal(varianceValue));
	  			ps.setBigDecimal(3, new BigDecimal(varianceValue));
	  			ps.setBigDecimal(4, new BigDecimal(varianceValue));
	  			ps.setString(5,baseOrgId );

				int i = ps.executeUpdate();
				logger.debug(Integer.toString(i));
				if(i>=0)status = true;
	  		}else{
				if(!varianceType.equals("Incr")){
				 	varianceBy = new Double(-varianceBy);
				}
/*	  			ps = con.prepareStatement(INSERT_INTO_ANAESTHESIA_TYPE_BY);
	  			ps.setString(1, newOrgId);
				ps.setBigDecimal(2, new BigDecimal(varianceBy));
				ps.setBigDecimal(3, new BigDecimal(nearstRoundOfValue));
				ps.setBigDecimal(4, new BigDecimal(varianceBy));
				ps.setBigDecimal(5, new BigDecimal(nearstRoundOfValue));
				ps.setBigDecimal(6, new BigDecimal(varianceBy));
				ps.setBigDecimal(7, new BigDecimal(nearstRoundOfValue));
				ps.setString(8, baseOrgId);

				int i = ps.executeUpdate(); */
				int i = insertChargesByPercent(con, newOrgId, baseOrgId, varianceBy, nearstRoundOfValue, updateDiscounts);
				logger.debug(Integer.toString(i));
				if(i>=0)status = true;

	  		}
	  		if (null != ps) ps.close();

	  	return status;
	}

	private static final String INSERT_ANESTHESIA_CODES_FOR_ORG = "INSERT INTO anesthesia_type_org_details " +
		"	SELECT anesthesia_type_id, ?, applicable, item_code, code_type, ?, 'N'" +
		"	FROM anesthesia_type_org_details WHERE org_id=?;";

	public static boolean addOrgCodesForAnesthesia(Connection con,String newOrgId,String varianceType,
		  Double varianceValue,Double varianceBy,boolean useValue,String baseOrgId,
		  Double nearstRoundOfValue, String userName) throws Exception{
			boolean status = false;
			PreparedStatement ps = null;
            BasicDynaBean obean = new OrgMasterDao().findByKey(con, "org_id", newOrgId);
            String rateSheetId = ("N".equals((String)obean.get("is_rate_sheet")) ? baseOrgId : null);
			try{
		   		ps = con.prepareStatement(INSERT_ANESTHESIA_CODES_FOR_ORG);
				ps.setString(1,newOrgId);
				ps.setString(2, rateSheetId);
				ps.setString(3, baseOrgId);

				int i = ps.executeUpdate();
				logger.debug(Integer.toString(i));
				if(i>=0)status = true;

			}finally{
				if (ps != null)
					ps.close();
			}
		return status;
	  }

	public static boolean addOrgCodesForItems(Connection con, String newOrgId, String baseOrgId, String userName) throws Exception {
		return addOrgCodesForAnesthesia(con, newOrgId, null, null, null, false, baseOrgId, null, userName);
	}

	private static int insertChargesByPercent(Connection con, String newOrgId, String baseOrgId,
            Double varianceBy, Double nearstRoundOfValue, boolean updateDiscounts) throws Exception {

	    int ndx = 1;
	    int numCharges = 3;

	    PreparedStatement pstmt = null;
	    try {
			pstmt = con.prepareStatement(updateDiscounts ?
					INSERT_INTO_ANAES_WITH_DISCOUNT_BY : INSERT_INTO_ANAESTHESIA_TYPE_BY);
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

    private static String INIT_ORG_DETAILS = "INSERT INTO anesthesia_type_org_details" +
    " (anesthesia_type_id, org_id, applicable, item_code, code_type, base_rate_sheet_id, is_override) " +
    "   SELECT anesthesia_type_id, ?, false, null, null, null, 'N'" +
    "   FROM anesthesia_type_master";

    private static String INIT_CHARGES = "INSERT INTO anesthesia_type_charges(anesthesia_type_id,org_id,bed_type," +
    "min_charge,incr_charge,slab_1_charge)" +
    "(SELECT anesthesia_type_id, ?, abov.bed_type, 0.0, 0.0, 0.0" +
    "FROM anesthesia_type_master atm CROSS JOIN all_beds_orgs_view abov WHERE abov.org_id =? ) ";

    private static final String INSERT_CHARGES = "INSERT INTO anesthesia_type_charges(anesthesia_type_id,org_id,bed_type, " +
    " min_charge,incr_charge,slab_1_charge, is_override)" +
    " SELECT atc.anesthesia_type_id, ?, atc.bed_type, " +
    " doroundvarying(atc.min_charge, ?, ?), " +
    " doroundvarying(atc.incr_charge, ?, ?), " +
    " doroundvarying(atc.slab_1_charge, ?, ?), " +
    " 'N' " +
    " FROM anesthesia_type_charges atc, anesthesia_type_org_details atod, anesthesia_type_org_details atodtarget "+
    " where atc.org_id = atod.org_id and atc.anesthesia_type_id = atod.anesthesia_type_id "+
    " and atodtarget.org_id = ? and atodtarget.anesthesia_type_id = atod.anesthesia_type_id and atodtarget.base_rate_sheet_id = ? " +
    " and atod.applicable = true "+
    " and atc.org_id = ?";

    private static final String UPDATE_CHARGES = "UPDATE anesthesia_type_charges AS target SET " +
    " min_charge = doroundvarying(atc.min_charge, ?, ?), " +
    " incr_charge = doroundvarying(atc.incr_charge, ?, ?), " +
    " slab_1_charge = doroundvarying(atc.slab_1_charge, ?, ?), " +
    " min_charge_discount = doroundvarying(atc.min_charge_discount, ?, ?), " +
    " incr_charge_discount = doroundvarying(atc.incr_charge_discount, ?, ?), " +
    " slab_1_charge_discount = doroundvarying(atc.slab_1_charge_discount, ?, ?), " +
    " is_override = 'N' " +
    " FROM anesthesia_type_charges atc, anesthesia_type_org_details atod "+
    " where atod.org_id = ? and atc.anesthesia_type_id = atod.anesthesia_type_id and atod.base_rate_sheet_id = ? and "+
    " target.anesthesia_type_id = atc.anesthesia_type_id and target.bed_type = atc.bed_type and " +
    " atod.applicable = true and target.is_override != 'Y'"+
    " and atc.org_id = ? and target.org_id = ?";

    private static final String UPDATE_EXCLUSIONS = "UPDATE anesthesia_type_org_details AS target " +
    " SET applicable = true, base_rate_sheet_id = atod.org_id " +
    " FROM anesthesia_type_org_details atod WHERE atod.anesthesia_type_id = target.anesthesia_type_id and " +
    " atod.org_id = ? and atod.applicable = true and target.org_id = ? and target.applicable = false and target.is_override != 'Y'";

    public boolean updateRatePlan(Connection con,String newOrgId, String baseOrgId,
				 String varianceType, Double variance, Double rndOff,
				 String userName, String orgName ) throws Exception {

		boolean status = false;
		// disableAuditTriggers("operation_charges", "z_operation_charges_audit_trigger");

		if(!varianceType.equals("Incr")) {
			variance = new Double(-variance);
		}

		BigDecimal varianceBy = new BigDecimal(variance);
        BigDecimal roundOff = new BigDecimal(rndOff);

		Object updparams[] = {varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff,
				varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff,
				newOrgId, baseOrgId, baseOrgId, newOrgId};
		Object insparams[] = {newOrgId, varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff, newOrgId,
				baseOrgId, baseOrgId};
		status = updateExclusions(con, UPDATE_EXCLUSIONS, newOrgId, baseOrgId);
		if (status) status = updateCharges(con, UPDATE_CHARGES, updparams);
		// postAuditEntry(con, "operation_charges_audit_log", userName, orgName);
		return status;
	}

    public boolean initRatePlan(Connection con, String newOrgId, String varianceType,
			Double varianceBy,String baseOrgId, Double roundOff, String userName, String orgName) throws Exception {
		boolean status = addOrgCodesForItems(con, newOrgId, baseOrgId, userName);
		if (status) status = addOrgForAnesthesiaType(con,newOrgId, varianceType,
				0.0, varianceBy, false, baseOrgId,
				roundOff, true);
		return status;
    }

    private static final String REINIT_EXCLUSIONS = "UPDATE anesthesia_type_org_details as target " +
    " SET applicable = atod.applicable, base_rate_sheet_id = atod.org_id, " +
    " is_override = 'N' " +
    " FROM anesthesia_type_org_details atod WHERE atod.anesthesia_type_id = target.anesthesia_type_id and " +
    " atod.org_id = ? and target.org_id = ? and target.is_override != 'Y'";

    public boolean reinitRatePlan(Connection con, String newOrgId, String varianceType,
			Double variance,String baseOrgId, Double rndOff,String userName, String orgName) throws Exception {
		boolean status = false;

		if(!varianceType.equals("Incr")) {
			variance = new Double(-variance);
		}

		BigDecimal varianceBy = new BigDecimal(variance);
        BigDecimal roundOff = new BigDecimal(rndOff);

		Object updparams[] = {varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff,
				varianceBy, roundOff, varianceBy, roundOff, newOrgId, baseOrgId};
		status = updateExclusions(con, REINIT_EXCLUSIONS, newOrgId, baseOrgId);
		if (status) status = updateCharges(con,UPDATE_RATEPLAN_ANAESTHESIA_CHARGES, updparams);
		return status;
    }

	private static String INIT_ITEM_ORG_DETAILS = "INSERT INTO anesthesia_type_org_details " +
    "(anesthesia_type_id, org_id, applicable, base_rate_sheet_id, is_override)" +
    " ( SELECT ?, od.org_id, false, prspv.base_rate_sheet_id, 'N' FROM organization_details od " +
    " LEFT OUTER JOIN priority_rate_sheet_parameters_view prspv ON od.org_id = prspv.org_id )";

    private static String INIT_ITEM_CHARGES = "INSERT INTO anesthesia_type_charges(anesthesia_type_id ,org_id,bed_type," +
    "min_charge, slab_1_charge, incr_charge)" +
    "(SELECT ?, abov.org_id, abov.bed_type, 0.0, 0.0, 0.0 FROM all_beds_orgs_view abov) ";

    public boolean initItemCharges(Connection con, String anesthesiaTypeId, String userName) throws Exception {

        boolean status = false;
        //  disableAuditTriggers("service_master_charges", "z_services_charges_audit_trigger");
        status = initItemCharges(con, INIT_ITEM_ORG_DETAILS, INIT_ITEM_CHARGES, anesthesiaTypeId, null); // no username field here
        // postAuditEntry(con, "service_master_charges_audit_log", userName, orgName);

        return status;
    }



	private static final String UPDATE_ANAESTHESIA_CHARGES_PLUS = "UPDATE anesthesia_type_charges totab SET " +
		" min_charge = round(fromtab.min_charge + ?), slab_1_charge = round(fromtab.slab_1_charge + ?)," +
		" incr_charge = round(fromtab.incr_charge + ?)" +
		" FROM anesthesia_type_charges fromtab" +
		" WHERE totab.org_id = ? AND fromtab.org_id = ?" +
		" AND totab.anesthesia_type_id = fromtab.anesthesia_type_id AND totab.bed_type = fromtab.bed_type AND totab.is_override='N'";

	private static final String UPDATE_ANAESTHESIA_CHARGES_MINUS = "UPDATE anesthesia_type_charges totab SET " +
		" min_charge = GREATEST(round(fromtab.min_charge - ?), 0), slab_1_charge = GREATEST(round(fromtab.slab_1_charge - ?), 0)," +
		" incr_charge = GREATEST(round(fromtab.incr_charge - ?), 0)" +
		" FROM anesthesia_type_charges fromtab" +
		" WHERE totab.org_id = ? AND fromtab.org_id = ?" +
		" AND totab.anesthesia_type_id = fromtab.anesthesia_type_id AND totab.bed_type = fromtab.bed_type AND totab.is_override='N'";

	private static final String UPDATE_ANAESTHESIA_CHARGES_BY = "UPDATE anesthesia_type_charges totab SET " +
		" min_charge = doroundvarying(fromtab.min_charge,?,?), slab_1_charge = doroundvarying(fromtab.slab_1_charge,?,?)," +
		" incr_charge = doroundvarying(fromtab.incr_charge,?,?)" +
		" FROM anesthesia_type_charges fromtab" +
		" WHERE totab.org_id = ? AND fromtab.org_id = ?" +
		" AND totab.anesthesia_type_id = fromtab.anesthesia_type_id AND totab.bed_type = fromtab.bed_type AND totab.is_override='N'";

	public static boolean updateOrgForAnaesthesia(Connection con,String orgId,String varianceType,
		Double varianceValue,Double varianceBy,boolean useValue,String baseOrgId,
		Double nearstRoundOfValue) throws SQLException, IOException {

			boolean status = false;
			PreparedStatement pstmt = null;

			if (useValue) {

			if (varianceType.equals("Incr"))
				pstmt = con.prepareStatement(UPDATE_ANAESTHESIA_CHARGES_PLUS);
			else
				pstmt = con.prepareStatement(UPDATE_ANAESTHESIA_CHARGES_MINUS);

			pstmt.setBigDecimal(1, new BigDecimal(varianceValue));
			pstmt.setBigDecimal(2, new BigDecimal(varianceValue));
			pstmt.setBigDecimal(3, new BigDecimal(varianceValue));
			pstmt.setString(4, orgId);
			pstmt.setString(5, baseOrgId);

			int i = pstmt.executeUpdate();
			if (i>=0) status = true;

			} else {

			pstmt = con.prepareStatement(UPDATE_ANAESTHESIA_CHARGES_BY);
			if (!varianceType.equals("Incr"))
			varianceBy = new Double(-varianceBy);

			pstmt.setBigDecimal(1, new BigDecimal(varianceBy));
			pstmt.setBigDecimal(2, new BigDecimal(nearstRoundOfValue));
			pstmt.setBigDecimal(3, new BigDecimal(varianceBy));
			pstmt.setBigDecimal(4, new BigDecimal(nearstRoundOfValue));
			pstmt.setBigDecimal(5, new BigDecimal(varianceBy));
			pstmt.setBigDecimal(6, new BigDecimal(nearstRoundOfValue));
			pstmt.setString(7, orgId);
			pstmt.setString(8, baseOrgId);

			int i = pstmt.executeUpdate();
			if (i>=0) status = true;

			}

			pstmt.close();

		return status;
	}

	private static String ANAESTHESIA_BACKUP_CHARGES = "INSERT INTO anesthesia_charges_backup(user_name, bkp_time," +
			" anesthesia_type_id, org_id, bed_type, min_charge, slab_1_charge, incr_charge, min_charge_discount, " +
			" incr_charge_discount, slab_1_charge_discount) SELECT ?, current_timestamp, anesthesia_type_id, " +
			" org_id, bed_type, min_charge, slab_1_charge, incr_charge, min_charge_discount, " +
			" incr_charge_discount, slab_1_charge_discount " +
			" FROM anesthesia_type_charges WHERE org_id = ? ";

	public static void backupCharges(String orgId, String userId)throws SQLException {
		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			pstmt = con.prepareStatement(ANAESTHESIA_BACKUP_CHARGES);
			pstmt.setString(1, userId);
			pstmt.setString(2, orgId);
			pstmt.execute();

		} finally {
			DataBaseUtil.closeConnections(con, pstmt);
		}

	}

	public boolean updateOrgForDerivedRatePlans(Connection con,String[] ratePlanIds, String[] applicable,
			String anesthTypeId) throws Exception{
		return updateOrgForDerivedRatePlans(con,ratePlanIds, applicable, "anesthesia_type_org_details", "anesthesia",
				"anesthesia_type_id", anesthTypeId);
	}

	public  boolean updateChargesForDerivedRatePlans(Connection con,String baseRateSheetId, String[] ratePlanIds,
	    		String[] bedType, Double[] minChg,Double[] incrChg,Double[] slabChg,
	    		String anesthTypeId, Double[] minDisc, Double[] incrDisc, Double[] slabDisc,String[] applicable)throws Exception {
		boolean success = false;

		AnaesthesiaTypeChargesDAO adao = new AnaesthesiaTypeChargesDAO();
		for(int i=0; i<ratePlanIds.length; i++) {
    		Map<String,Object> keys = new HashMap<String, Object>();
    		keys.put("base_rate_sheet_id", baseRateSheetId);
    		keys.put("org_id", ratePlanIds[i]);
    		BasicDynaBean bean = ratePlanParametersDao.findByKey(keys);
    		int variation =(Integer)bean.get("rate_variation_percent");
    		int roundoff = (Integer)bean.get("round_off_amount");

    		List<BasicDynaBean> chargeList = new ArrayList();

    		boolean overrided = isChargeOverrided(con,ratePlanIds[i],"anesthesia_type_id",anesthTypeId,
    				"anesthesia","anesthesia_type_org_details");

    		if(!overrided) {
	    		for(int k=0 ; k<bedType.length; k++) {
	    			BasicDynaBean charge = adao.getBean();

	    			charge.set("anesthesia_type_id", anesthTypeId);
	    			charge.set("org_id", ratePlanIds[i]);
	    			charge.set("bed_type", bedType[k]);

	    			Double mCharge = calculateCharge(minChg[k], new Double(variation), roundoff);
	    			Double iCharge = calculateCharge(incrChg[k], new Double(variation), roundoff);
	    			Double SCharge = calculateCharge(slabChg[k], new Double(variation), roundoff);

	    			Double mDisc = calculateCharge(minDisc[k], new Double(variation), roundoff);
	    			Double iDisc = calculateCharge(incrDisc[k], new Double(variation), roundoff);
	    			Double sDisc = calculateCharge(slabDisc[k], new Double(variation), roundoff);

	    			charge.set("min_charge", new BigDecimal(mCharge));
	    			charge.set("incr_charge", new BigDecimal(iCharge));
	    			charge.set("slab_1_charge", new BigDecimal(SCharge));
	    			charge.set("min_charge_discount", new BigDecimal(mDisc));
	    			charge.set("incr_charge_discount", new BigDecimal(iDisc));
	    			charge.set("slab_1_charge_discount", new BigDecimal(sDisc));
	    			chargeList.add(charge);
	    		}
    		}
    		for (BasicDynaBean c: chargeList) {
    			adao.updateWithNames(con, c.getMap(),  new String[] {"anesthesia_type_id", "org_id", "bed_type"});
    		}
    		success = true;

    		boolean overrideItemCharges = checkItemStatus(con, ratePlanIds[i], "anesthesia_type_org_details", "anesthesia", "anesthesia_type_id",
    				anesthTypeId, applicable[i]);
    		if(overrideItemCharges)
    			OverrideItemCharges(con, "anesthesia_type_charges", "anesthesia", ratePlanIds[i], "anesthesia_type_id", anesthTypeId);
		}
		return success;
	}

	private static final String GET_DERIVED_RATE_PALN_DETAILS = "select rp.org_id,od.org_name, "+
		" case when rate_variation_percent<0 then 'Decrease By' else 'Increase By' end as discormarkup, "+
		" rate_variation_percent,round_off_amount,aod.applicable,aod.anesthesia_type_id,rp.base_rate_sheet_id, aod.is_override "+
		" from rate_plan_parameters rp "+
		" join organization_details od on(od.org_id=rp.org_id) "+
		" join anesthesia_type_org_details aod on (aod.org_id = rp.org_id) "+
		" where rp.base_rate_sheet_id =?  and anesthesia_type_id=? and aod.base_rate_sheet_id=? ";

	public List<BasicDynaBean> getDerivedRatePlanDetails(String baseRateSheetId,String opId)throws SQLException {
		return getDerivedRatePlanDetails(baseRateSheetId, "equipment", opId,GET_DERIVED_RATE_PALN_DETAILS);
	}

	private static final String UPDATE_RATEPLAN_ANAESTHESIA_CHARGES = "UPDATE anesthesia_type_charges totab SET " +
		" min_charge = doroundvarying(fromtab.min_charge,?,?), slab_1_charge = doroundvarying(fromtab.slab_1_charge,?,?)," +
		" incr_charge = doroundvarying(fromtab.incr_charge,?,?)," +
		" min_charge_discount = doroundvarying(fromtab.min_charge_discount,?,?), "+
		" slab_1_charge_discount = doroundvarying(fromtab.slab_1_charge_discount,?,?), "+
		" incr_charge_discount = doroundvarying(fromtab.incr_charge_discount,?,?) "+
		" FROM anesthesia_type_charges fromtab" +
		" WHERE totab.org_id = ? AND fromtab.org_id = ?" +
		" AND totab.anesthesia_type_id = fromtab.anesthesia_type_id AND totab.bed_type = fromtab.bed_type AND totab.is_override='N'";

	public boolean updateAnaesthesiaChargesForDerivedRatePlans(String orgId,String varianceType,
		Double varianceValue,Double varianceBy,String baseOrgId,
		Double nearstRoundOfValue,String userName, String orgName, boolean upload) throws SQLException, Exception {

			boolean success = false;
			Connection con = null;
			try{
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);
				if(upload) {
					List<BasicDynaBean> rateSheetList =  getRateSheetsByPriority(con, orgId, ratePlanParametersDao);
					for (int i = 0; i < rateSheetList.size(); i++) {
						success = false;
						BasicDynaBean currentSheet = rateSheetList.get(i);
						Integer variation = (Integer)currentSheet.get("rate_variation_percent");
						String varType = (variation >= 0) ? "Incr" :"Decr";
						Double varBy = new Double((variation >= 0 ? variation : -variation));
						Double roundOff = new Double((Integer)currentSheet.get("round_off_amount"));
						if (i == 0) {
							success = reinitRatePlan(con, orgId, varType, varBy,
									(String)currentSheet.get("base_rate_sheet_id"), roundOff, userName, orgName);
						} else {
							success = updateRatePlan(con, orgId, (String)currentSheet.get("base_rate_sheet_id"), varType,
									varBy, roundOff, userName, orgName);
						}
					}
				}else {
					BigDecimal variance = new BigDecimal(varianceBy);
					BigDecimal roundoff = new BigDecimal(nearstRoundOfValue);
					Object updparams[] = {variance, roundoff, variance, roundoff, variance, roundoff, variance, roundoff,
							variance, roundoff, variance, roundoff, orgId, baseOrgId, baseOrgId, orgId};
					success = updateCharges(con, UPDATE_CHARGES, updparams);
				}
			}finally {
				DataBaseUtil.commitClose(con, success);
			}
		return success;
	}

	public static boolean updateChargesBasedOnNewRateSheet(Connection con, String orgId,Double varianceBy,
			String baseOrgId,Double nearstRoundOfValue, String AnesthesiaTypeId)throws SQLException,Exception {
		boolean success = false;
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(UPDATE_RATEPLAN_ANAESTHESIA_CHARGES+ " AND totab.anesthesia_type_id = ? ");
			setPsValues(ps,varianceBy,nearstRoundOfValue,orgId,baseOrgId);
			ps.setString(15, AnesthesiaTypeId);

			int i = ps.executeUpdate();
			if (i>=0) success = true;
		}finally {
			if (ps != null)
                ps.close();
		}
		return success;
	}

	private static void setPsValues(PreparedStatement ps, Double varianceBy, Double nearstRoundOfValue,
			String orgId, String baseOrgId) throws SQLException,Exception {

		ps.setBigDecimal(1, new BigDecimal(varianceBy));
		ps.setBigDecimal(2, new BigDecimal(nearstRoundOfValue));
		ps.setBigDecimal(3, new BigDecimal(varianceBy));
		ps.setBigDecimal(4, new BigDecimal(nearstRoundOfValue));
		ps.setBigDecimal(5, new BigDecimal(varianceBy));
		ps.setBigDecimal(6, new BigDecimal(nearstRoundOfValue));
		ps.setBigDecimal(7, new BigDecimal(varianceBy));
		ps.setBigDecimal(8, new BigDecimal(nearstRoundOfValue));
		ps.setBigDecimal(9, new BigDecimal(varianceBy));
		ps.setBigDecimal(10, new BigDecimal(nearstRoundOfValue));
		ps.setBigDecimal(11, new BigDecimal(varianceBy));
		ps.setBigDecimal(12, new BigDecimal(nearstRoundOfValue));

		ps.setString(13, orgId);
		ps.setString(14, baseOrgId);
	}

	public boolean updateBedForRatePlan(Connection con, String ratePlanId,
			Double variance,String rateSheetId, Double rndOff, String bedType) throws Exception {

		boolean status = false;

		BigDecimal varianceBy = new BigDecimal(variance);
	    BigDecimal roundOff = new BigDecimal(rndOff);

		Object updparams[] = {varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff,
				varianceBy, roundOff, varianceBy, roundOff, ratePlanId, rateSheetId,bedType};
		status = updateCharges(con,UPDATE_RATEPLAN_ANAESTHESIA_CHARGES + " AND totab.bed_type=? ", updparams);
		return status;
	}

}
