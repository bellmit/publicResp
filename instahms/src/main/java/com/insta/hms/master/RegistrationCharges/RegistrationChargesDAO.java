/**
 *
 */
package com.insta.hms.master.RegistrationCharges;

import com.bob.hms.common.Constants;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Logger;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.rateplan.ItemChargeDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author krishna.t
 *
 */
public class RegistrationChargesDAO extends ItemChargeDAO {

	public RegistrationChargesDAO() {
		super("registration_charges");
	}



	public static final String query = "select * from registration_charges where org_id = ?";
	
    private static final GenericDAO ratePlanParametersDAO = new GenericDAO("rate_plan_parameters");

	@SuppressWarnings("unchecked")
	public static List<BasicDynaBean> getRegistrationChargesBeans(String orgId)throws SQLException {
		Connection con = null;
		PreparedStatement pstmt = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, orgId);
			return DataBaseUtil.queryToDynaList(pstmt);

		} finally {
			DataBaseUtil.closeConnections(con, pstmt);

		}
	}

	public static final String chargequery = "SELECT " +
			" org_id, bed_type, ip_reg_charge, op_reg_charge, gen_reg_charge, reg_renewal_charge, " +
			" mrcharge, ip_mlccharge, op_mlccharge, gen_reg_charge_discount, op_reg_charge_discount, " +
			" reg_renewal_charge_discount, ip_reg_charge_discount, ip_mlccharge_discount," +
			" op_mlccharge_discount, mrcharge_discount FROM registration_charges rc" +
			" WHERE rc.bed_type =? AND rc.org_id =?";

	public BasicDynaBean getRegistrationCharges(String bedtype,String orgid) throws SQLException{

		Connection con=null;
		PreparedStatement ps=null;
		List cl = null;
		BasicDynaBean bean = null;

		try {
			 con=DataBaseUtil.getConnection();

			 String orgquery = "select org_id from organization_details where org_name=?";
			 String generalorgid = DataBaseUtil.getStringValueFromDb(orgquery,
			     Constants.getConstantValue("ORG"));
			 String generalbedtype = Constants.getConstantValue("BEDTYPE");

			 ps = con.prepareStatement(chargequery);
			 ps.setString(1, bedtype);
			 ps.setString(2, orgid);

			 cl = DataBaseUtil.queryToDynaList(ps);

			 Logger.log(cl);

			 if(cl.size()>0){
				 bean = (BasicDynaBean)cl.get(0);
			 }else{
				 ps.setString(1, generalbedtype);
				 ps.setString(2, generalorgid);
				 bean = (BasicDynaBean)DataBaseUtil.queryToDynaList(ps).get(0);
			 }

		}catch(Exception e){
			Logger.logException("Exception raised in getRegistrationCharges", e);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		return bean;
	}

	private static final String INSERT_INTO_REGISTRATION_PLUS = "INSERT INTO registration_charges(org_id, bed_type, ip_reg_charge," +
		" op_reg_charge, gen_reg_charge, reg_renewal_charge, mrcharge, ip_mlccharge, op_mlccharge)(SELECT ?, bed_type, round(ip_reg_charge + ?)," +
		" round(op_reg_charge + ?), round(gen_reg_charge + ?), round(reg_renewal_charge + ?), round(mrcharge + ?), round(ip_mlccharge + ?)," +
		" round(op_mlccharge + ?) FROM  registration_charges WHERE org_id = ?)";

	private static final String INSERT_INTO_REGISTRATION_MINUS = "INSERT INTO registration_charges(org_id, bed_type, ip_reg_charge," +
		" op_reg_charge, gen_reg_charge, reg_renewal_charge, mrcharge, ip_mlccharge, op_mlccharge)(SELECT ?, bed_type, GREATEST(round(ip_reg_charge - ?), 0)," +
		" GREATEST(round(op_reg_charge - ?), 0), GREATEST(round(gen_reg_charge - ?), 0), GREATEST(round(reg_renewal_charge - ?), 0), " +
		" GREATEST(round(mrcharge - ?), 0), GREATEST(round(ip_mlccharge - ?), 0)," +
		" GREATEST(round(op_mlccharge - ?), 0) FROM registration_charges WHERE org_id = ?)";

	private static final String INSERT_INTO_REGISTRATION_BY = "INSERT INTO registration_charges(org_id, bed_type, ip_reg_charge," +
		" op_reg_charge, gen_reg_charge, reg_renewal_charge, mrcharge, ip_mlccharge, op_mlccharge)(SELECT ?, bed_type, doroundvarying(ip_reg_charge,?,?)," +
		" doroundvarying(op_reg_charge,?,?), doroundvarying(gen_reg_charge,?,?), doroundvarying(reg_renewal_charge,?,?), doroundvarying(mrcharge,?,?)," +
		" doroundvarying(ip_mlccharge,?,?), doroundvarying(op_mlccharge,?,?)  FROM  registration_charges WHERE org_id = ?)";

	private static final String INSERT_INTO_REG_WITH_DISCOUNTS_BY = "INSERT INTO registration_charges (org_id, bed_type, " +
	" ip_reg_charge, op_reg_charge, gen_reg_charge, reg_renewal_charge, mrcharge, ip_mlccharge, op_mlccharge," +
	" ip_reg_charge_discount, op_reg_charge_discount, gen_reg_charge_discount, reg_renewal_charge_discount, mrcharge_discount, ip_mlccharge_discount, op_mlccharge_discount)" +
	"(SELECT ?, bed_type, " +
	" doroundvarying(ip_reg_charge,?,?), doroundvarying(op_reg_charge,?,?), doroundvarying(gen_reg_charge,?,?), " +
	" doroundvarying(reg_renewal_charge,?,?), doroundvarying(mrcharge,?,?), doroundvarying(ip_mlccharge,?,?), doroundvarying(op_mlccharge,?,?), " +
	" doroundvarying(ip_reg_charge_discount,?,?), doroundvarying(op_reg_charge_discount,?,?), doroundvarying(gen_reg_charge_discount,?,?), " +
	" doroundvarying(reg_renewal_charge_discount,?,?), doroundvarying(mrcharge_discount,?,?), doroundvarying(ip_mlccharge_discount,?,?), " +
	" doroundvarying(op_mlccharge_discount,?,?)  " +
	" FROM  registration_charges WHERE org_id = ?)";

	public static boolean addOrgForRegistration(Connection con,String newOrgId,String varianceType,
			Double varianceValue,Double varianceBy,boolean useValue,String baseOrgId,Double nearstRoundOfValue) throws Exception {
		return addOrgForRegistration(con, newOrgId, varianceType, varianceValue, varianceBy, useValue, baseOrgId, nearstRoundOfValue, false);
	}

	public static boolean addOrgForRegistration(Connection con,String newOrgId,String varianceType,
			Double varianceValue,Double varianceBy,boolean useValue,String baseOrgId,Double nearstRoundOfValue, boolean updateDiscounts) throws Exception {
			boolean status = false;
			PreparedStatement pstmt = null;
			if (useValue) {
				if (varianceType.equals("Incr")) {
					pstmt = con.prepareStatement(INSERT_INTO_REGISTRATION_PLUS);
					pstmt.setString(1, newOrgId);
					pstmt.setDouble(2, varianceValue);
					pstmt.setDouble(3, varianceValue);
					pstmt.setDouble(4, varianceValue);
					pstmt.setDouble(5, varianceValue);
					pstmt.setDouble(6, varianceValue);
					pstmt.setDouble(7, varianceValue);
					pstmt.setDouble(8, varianceValue);
					pstmt.setString(9, baseOrgId);

					status = pstmt.executeUpdate() >= 0;

				} else {
					pstmt = con.prepareStatement(INSERT_INTO_REGISTRATION_MINUS);
					pstmt.setString(1, newOrgId);
					pstmt.setDouble(2, varianceValue);
					pstmt.setDouble(3, varianceValue);
					pstmt.setDouble(4, varianceValue);
					pstmt.setDouble(5, varianceValue);
					pstmt.setDouble(6, varianceValue);
					pstmt.setDouble(7, varianceValue);
					pstmt.setDouble(8, varianceValue);
					pstmt.setString(9, baseOrgId);

					status = pstmt.executeUpdate() >= 0;
				}

			} else {
				if (!varianceType.equals("Incr"))
					varianceBy = new Double(-varianceBy);

				/*pstmt.setString(1, newOrgId);

				pstmt.setBigDecimal(2, new BigDecimal(varianceBy));
				pstmt.setBigDecimal(3, new BigDecimal(nearstRoundOfValue));

				pstmt.setBigDecimal(4, new BigDecimal(varianceBy));
				pstmt.setBigDecimal(5, new BigDecimal(nearstRoundOfValue));

				pstmt.setBigDecimal(6, new BigDecimal(varianceBy));
				pstmt.setBigDecimal(7, new BigDecimal(nearstRoundOfValue));

				pstmt.setBigDecimal(8, new BigDecimal(varianceBy));
				pstmt.setBigDecimal(9, new BigDecimal(nearstRoundOfValue));

				pstmt.setBigDecimal(10, new BigDecimal(varianceBy));
				pstmt.setBigDecimal(11, new BigDecimal(nearstRoundOfValue));

				pstmt.setBigDecimal(12, new BigDecimal(varianceBy));
				pstmt.setBigDecimal(13, new BigDecimal(nearstRoundOfValue));

				pstmt.setBigDecimal(14, new BigDecimal(varianceBy));
				pstmt.setBigDecimal(15, new BigDecimal(nearstRoundOfValue));

				pstmt.setString(16, baseOrgId);

				status = pstmt.executeUpdate() >= 0; */
				status = insertChargesByPercent(con, newOrgId, baseOrgId, varianceBy, nearstRoundOfValue, updateDiscounts) >= 0;

			}
			if (pstmt != null) pstmt.close();

		return status;
	}

	private static int insertChargesByPercent(Connection con, String newOrgId, String baseOrgId,
			Double varianceBy, Double nearstRoundOfValue, boolean updateDiscounts) throws Exception {

		int ndx = 1;
		int numCharges = 7;

		PreparedStatement pstmt = null;
		try {
			pstmt = con.prepareStatement(updateDiscounts ?
									INSERT_INTO_REG_WITH_DISCOUNTS_BY :
									INSERT_INTO_REGISTRATION_BY);
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
	/*Rate Plan Changes - Begin*/

	private static String INIT_CHARGES = "INSERT INTO registration_charges(org_id,bed_type," +
	   	"ip_reg_charge, op_reg_charge, gen_reg_charge, reg_renewal_charge, mrcharge, ip_mlccharge, op_mlccharge)" +
	    "(SELECT ?, abov.bed_type, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0" +
	    "FROM all_beds_orgs_view abov WHERE abov.org_id =? ) ";

	private static final String UPDATE_CHARGES = "UPDATE registration_charges AS target SET " +
	    " ip_reg_charge = doroundvarying(rc.ip_reg_charge,?,?), " +
	    " op_reg_charge = doroundvarying(rc.op_reg_charge,?,?), " +
	    " gen_reg_charge = doroundvarying(rc.gen_reg_charge,?,?), " +
	    " reg_renewal_charge = doroundvarying(rc.reg_renewal_charge,?,?), " +
	    " mrcharge = doroundvarying(rc.mrcharge,?,?), " +
	    " ip_mlccharge = doroundvarying(rc.ip_mlccharge,?,?), " +
	    " op_mlccharge = doroundvarying(rc.op_mlccharge,?,?), " +
	    " ip_reg_charge_discount = doroundvarying(rc.ip_reg_charge_discount,?,?), " +
	    " op_reg_charge_discount = doroundvarying(rc.op_reg_charge_discount,?,?), " +
	    " gen_reg_charge_discount = doroundvarying(rc.gen_reg_charge_discount,?,?), " +
	    " reg_renewal_charge_discount = doroundvarying(rc.reg_renewal_charge_discount,?,?), " +
	    " mrcharge_discount = doroundvarying(rc.mrcharge_discount,?,?), " +
	    " ip_mlccharge_discount = doroundvarying(rc.ip_mlccharge_discount,?,?), " +
	    " op_mlccharge_discount = doroundvarying(rc.op_mlccharge_discount,?,?), " +
	    " is_override = 'N' " +
	    " FROM registration_charges rc where " +
	    " target.bed_type = rc.bed_type and " +
	    " target.is_override != 'Y'"+
	    " and rc.org_id = ? and target.org_id = ?";

	public boolean updateRatePlan(Connection con,String newOrgId, String baseOrgId,
            String varianceType, Double variance, Double rndOff,
            String userName, String orgName ) throws Exception {

        boolean status = false;
        if(!varianceType.equals("Incr")) {
            variance = new Double(-variance);
        }

        BigDecimal varianceBy = new BigDecimal(variance);
        BigDecimal roundOff = new BigDecimal(rndOff);


        Object params[] = {varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff,
        		varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff,
        		varianceBy, roundOff, varianceBy, roundOff,
        		varianceBy, roundOff, varianceBy, roundOff, baseOrgId, newOrgId};
        status = updateCharges(con, UPDATE_CHARGES, params);

        return status;
	}

	public boolean initRatePlan(Connection con, String newOrgId,  String varianceType,
			Double varianceBy,String baseOrgId, Double roundOff, String userName, String orgName) throws Exception {

        // no org_details for registration
        boolean status = addOrgForRegistration(con, newOrgId, varianceType, 0.0, varianceBy, false, baseOrgId, roundOff, true);
        return status;
	}
	/* Rate Plan Changes - End*/
    public boolean reinitRatePlan(Connection con, String newOrgId, String varianceType,
			Double variance,String baseOrgId, Double rndOff,String userName, String orgName) throws Exception {
    	// in case of registration reinit is nothing but update.
    	return updateRatePlan(con, newOrgId, baseOrgId, varianceType, variance, rndOff, userName, orgName);
    }


	private static final String UPDATE_REGCHARGE_PLUS = "UPDATE registration_charges totab SET " +
		" ip_reg_charge = round(fromtab.ip_reg_charge + ?), op_reg_charge = round(fromtab.op_reg_charge + ?)," +
		" gen_reg_charge = round(fromtab.gen_reg_charge + ?), reg_renewal_charge = round(fromtab.reg_renewal_charge + ?)," +
		" mrcharge = round(fromtab.mrcharge + ?), ip_mlccharge = round(fromtab.ip_mlccharge + ?)," +
		" op_mlccharge = round(fromtab.op_mlccharge + ?)" +
		" FROM registration_charges fromtab" +
		" WHERE totab.org_id = ? AND fromtab.org_id = ?" +
		" AND totab.bed_type = fromtab.bed_type AND totab.is_override='N'";

	private static final String UPDATE_REGCHARGE_MINUS = "UPDATE registration_charges totab SET " +
		" ip_reg_charge = GREATEST(round(fromtab.ip_reg_charge - ?), 0), op_reg_charge = GREATEST(round(fromtab.op_reg_charge - ?), 0)," +
		" gen_reg_charge = GREATEST(round(fromtab.gen_reg_charge - ?), 0), reg_renewal_charge = GREATEST(round(fromtab.reg_renewal_charge - ?), 0)," +
		" mrcharge = GREATEST(round(fromtab.mrcharge - ?), 0), ip_mlccharge = GREATEST(round(fromtab.ip_mlccharge - ?), 0)," +
		" op_mlccharge = GREATEST(round(fromtab.op_mlccharge - ?), 0)" +
		" FROM registration_charges fromtab" +
		" WHERE totab.org_id = ? AND fromtab.org_id = ?" +
		" AND totab.bed_type = fromtab.bed_type AND totab.is_override='N'";

	private static final String UPDATE_REGCHARGE_BY = "UPDATE registration_charges totab SET " +
		" ip_reg_charge = doroundvarying(fromtab.ip_reg_charge,?,?), op_reg_charge = doroundvarying(fromtab.op_reg_charge,?,?)," +
		" gen_reg_charge = doroundvarying(fromtab.gen_reg_charge,?,?), reg_renewal_charge = doroundvarying(fromtab.reg_renewal_charge,?,?)," +
		" mrcharge = doroundvarying(fromtab.mrcharge,?,?), ip_mlccharge = doroundvarying(fromtab.ip_mlccharge,?,?)," +
		" op_mlccharge = doroundvarying(fromtab.op_mlccharge,?,?)" +
		" FROM registration_charges fromtab" +
		" WHERE totab.org_id = ? AND fromtab.org_id = ?" +
		" AND totab.bed_type = fromtab.bed_type AND totab.is_override='N'";


	public static boolean updateOrgForRegCharges(Connection con,String orgId,String varianceType,
			Double varianceValue,Double varianceBy,boolean useValue,String baseOrgId,
			Double nearstRoundOfValue) throws SQLException, IOException {

		boolean status = false;
		PreparedStatement pstmt = null;

		if (useValue) {

			if (varianceType.equals("Incr"))
				pstmt = con.prepareStatement(UPDATE_REGCHARGE_PLUS);
			else
				pstmt = con.prepareStatement(UPDATE_REGCHARGE_MINUS);

			pstmt.setBigDecimal(1, new BigDecimal(varianceValue));
			pstmt.setBigDecimal(2, new BigDecimal(varianceValue));
			pstmt.setBigDecimal(3, new BigDecimal(varianceValue));
			pstmt.setBigDecimal(4, new BigDecimal(varianceValue));
			pstmt.setBigDecimal(5, new BigDecimal(varianceValue));
			pstmt.setBigDecimal(6, new BigDecimal(varianceValue));
			pstmt.setBigDecimal(7, new BigDecimal(varianceValue));
			pstmt.setString(8, orgId);
			pstmt.setString(9, baseOrgId);

			int i = pstmt.executeUpdate();
			if (i>=0) status = true;

		} else {

			pstmt = con.prepareStatement(UPDATE_REGCHARGE_BY);
			if (!varianceType.equals("Incr"))
				varianceBy = new Double(-varianceBy);

			pstmt.setBigDecimal(1, new BigDecimal(varianceBy));
			pstmt.setBigDecimal(2, new BigDecimal(nearstRoundOfValue));
			pstmt.setBigDecimal(3, new BigDecimal(varianceBy));
			pstmt.setBigDecimal(4, new BigDecimal(nearstRoundOfValue));
			pstmt.setBigDecimal(5, new BigDecimal(varianceBy));
			pstmt.setBigDecimal(6, new BigDecimal(nearstRoundOfValue));
			pstmt.setBigDecimal(7, new BigDecimal(varianceBy));
			pstmt.setBigDecimal(8, new BigDecimal(nearstRoundOfValue));
			pstmt.setBigDecimal(9, new BigDecimal(varianceBy));
			pstmt.setBigDecimal(10, new BigDecimal(nearstRoundOfValue));
			pstmt.setBigDecimal(11, new BigDecimal(varianceBy));
			pstmt.setBigDecimal(12, new BigDecimal(nearstRoundOfValue));
			pstmt.setBigDecimal(13, new BigDecimal(varianceBy));
			pstmt.setBigDecimal(14, new BigDecimal(nearstRoundOfValue));
			pstmt.setString(15, orgId);
			pstmt.setString(16, baseOrgId);

			int i = pstmt.executeUpdate();
			if (i>=0) status = true;

		}
		pstmt.close();

		return status;
	}


	public static List<BasicDynaBean> getRegChargesForOrganization(String orgId, List<String> charges, List<String> bedTypes)throws SQLException {
		Connection con = null;
		PreparedStatement pstmt = null;
		StringBuilder chargesQuery = new StringBuilder();
		chargesQuery.append("SELECT ");
		int bedTypesLen = bedTypes.size();
		int chargesLen = charges.size();

		int bedCount = 0;

		for (String bed : bedTypes) {
			bedCount ++;
			int chargesCount = 0;

			for (String charge : charges) {
				chargesCount ++;
				chargesQuery.append("(SELECT "+ charge +" FROM registration_charges " +
						"WHERE bed_type = ? AND org_id = ?) AS "+DataBaseUtil.quoteIdent(bed+""+charge, true));
				if (bedTypesLen != bedCount ||(bedTypesLen == bedCount && chargesLen != chargesCount))
					chargesQuery.append(",");
			}
		}
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			pstmt = con.prepareStatement(chargesQuery.toString());
			int i = 1;
			for (String bed : bedTypes) {
				for (String charge : charges) {
					pstmt.setString(i++, bed);
					pstmt.setString(i++, orgId);
				}
			}

			return DataBaseUtil.queryToDynaList(pstmt);
		} finally {
			DataBaseUtil.closeConnections(con, pstmt);
		}
	}

	private static final String BACKUP_REG_CHARGES = "INSERT INTO registration_charges_backup  (user_name, bkp_time, org_id, ip_reg_charge, " +
			" op_reg_charge, gen_reg_charge,reg_renewal_charge, mrcharge, ip_mlccharge, op_mlccharge, gen_reg_charge_discount, reg_renewal_charge_discount, " +
			" ip_reg_charge_discount, ip_mlccharge_discount, op_mlccharge_discount, mrcharge_discount, bed_type )" +
			" SELECT ?, current_timestamp, org_id, ip_reg_charge, op_reg_charge, gen_reg_charge," +
			" reg_renewal_charge, mrcharge, ip_mlccharge, op_mlccharge, gen_reg_charge_discount, op_reg_charge_discount, reg_renewal_charge_discount," +
			" ip_mlccharge_discount, op_mlccharge_discount, mrcharge_discount, bed_type FROM registration_charges WHERE org_id = ?";

	public static void backUpCharges(String userName, String orgId)throws SQLException {
		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			pstmt = con.prepareStatement(BACKUP_REG_CHARGES);
			pstmt.setString(1, userName);
			pstmt.setString(2, orgId);
			pstmt.execute();
		} finally {
			DataBaseUtil.closeConnections(con, pstmt);
		}
	}

	public boolean updateChargesForDerivedRatePlans(Connection con,String baseRateSheetId,String[] ratePlanIds,
			Double[] ipRegCharge,Double[] opRegCharge,Double[] regRenewalCharge,Double[] genRegCharge,
			Double[] mrCharge,Double[] ipMlcCharge,Double[] opMlcCharge,
			String[] bedType,Double[] ipRegDisc,Double[] opRegDisc,Double[] genRegDisc,Double[] regRenewalDisc,
			Double[] mrDisc,Double[] ipMlcDisc,Double[] opMlcDisc)throws SQLException,Exception{

		RegistrationChargesDAO dao = new RegistrationChargesDAO();
		boolean success = false;

		for(int i=0; i<ratePlanIds.length; i++) {
    		Map<String,Object> keys = new HashMap<String, Object>();
    		keys.put("base_rate_sheet_id", baseRateSheetId);
    		keys.put("org_id", ratePlanIds[i]);
    		BasicDynaBean bean = ratePlanParametersDAO.findByKey(keys);
    		int variation =(Integer)bean.get("rate_variation_percent");
    		int roundoff = (Integer)bean.get("round_off_amount");

    		Map<String,Object> okeys = new HashMap<String, Object>();
    		okeys.put("org_id", ratePlanIds[i]);
    		okeys.put("bed_type", bedType[0]);
    		String isOverrided = (String)dao.findByKey(con, okeys).get("is_override");
    		List<BasicDynaBean> chargeList = new ArrayList();
    		if(!isOverrided.equals("Y")) {
	    		for(int k=0 ; k<bedType.length; k++) {
	    			BasicDynaBean charge = dao.getBean();
	    			charge.set("org_id", ratePlanIds[i]);
	    			charge.set("bed_type", bedType[k]);

	    			Double ip_regCharge = calculateCharge(ipRegCharge[k], new Double(variation), roundoff);
	    			Double op_regCharge = calculateCharge(opRegCharge[k], new Double(variation), roundoff);
	    			Double reg_renewalCharge = calculateCharge(regRenewalCharge[k], new Double(variation), roundoff);
	    			Double gen_regCharge = calculateCharge(genRegCharge[k], new Double(variation), roundoff);
	    			Double mr_charge = calculateCharge(mrCharge[k], new Double(variation), roundoff);
	    			Double ip_mlcCharge = calculateCharge(ipMlcCharge[k], new Double(variation), roundoff);
	    			Double op_mlcCharge = calculateCharge(opMlcCharge[k], new Double(variation), roundoff);

	    			Double ip_regDisc = calculateCharge(ipRegDisc[k], new Double(variation), roundoff);
	    			Double op_regDisc = calculateCharge(opRegDisc[k], new Double(variation), roundoff);
	    			Double reg_renewalDisc = calculateCharge(regRenewalDisc[k], new Double(variation), roundoff);
	    			Double gen_regDisc = calculateCharge(genRegDisc[k], new Double(variation), roundoff);
	    			Double mr_disc = calculateCharge(mrDisc[k], new Double(variation), roundoff);
	    			Double ip_mlcDisc = calculateCharge(ipMlcDisc[k], new Double(variation), roundoff);
	    			Double op_mlcDisc = calculateCharge(opMlcDisc[k], new Double(variation), roundoff);

	    			charge.set("ip_reg_charge", new BigDecimal(ip_regCharge));
	    			charge.set("op_reg_charge", new BigDecimal(op_regCharge));
	    			charge.set("gen_reg_charge", new BigDecimal(gen_regCharge));
	    			charge.set("reg_renewal_charge", new BigDecimal(reg_renewalCharge));
	    			charge.set("mrcharge", new BigDecimal(mr_charge));
	    			charge.set("ip_mlccharge", new BigDecimal(ip_mlcCharge));
	    			charge.set("op_mlccharge", new BigDecimal(op_mlcCharge));

	    			charge.set("ip_reg_charge_discount", new BigDecimal(ip_regDisc));
	    			charge.set("op_reg_charge_discount", new BigDecimal(op_regDisc));
	    			charge.set("gen_reg_charge_discount", new BigDecimal(gen_regDisc));
	    			charge.set("reg_renewal_charge_discount", new BigDecimal(reg_renewalDisc));
	    			charge.set("mrcharge_discount", new BigDecimal(mr_disc));
	    			charge.set("ip_mlccharge_discount", new BigDecimal(ip_mlcDisc));
	    			charge.set("op_mlccharge_discount", new BigDecimal(op_mlcDisc));
	    			chargeList.add(charge);
	    		}
    		}
    		for (BasicDynaBean c: chargeList) {
    			dao.updateWithNames(con, c.getMap(),  new String[] {"org_id", "bed_type"});
    		}
    		success = true;
		}
		return success;
	}

	private static final String GET_DERIVED_RATE_PALN_DETAILS = "select rp.org_id,od.org_name, "+
		" case when rate_variation_percent<0 then 'Decrease By' else 'Increase By' end as discormarkup, "+
		" rate_variation_percent,round_off_amount,rp.base_rate_sheet_id, is_override  "+
		" from priority_rate_sheet_parameters_view rp "+
		" join organization_details od on(od.org_id=rp.org_id) "+
		" join registration_charges rc on(rc.org_id = rp.org_id) "+
		" where rp.base_rate_sheet_id =? and bed_type='GENERAL' ";

	public List<BasicDynaBean> getDerivedRatePlanDetails(String baseRateSheetId)throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_DERIVED_RATE_PALN_DETAILS);
			ps.setString(1, baseRateSheetId);
			return DataBaseUtil.queryToDynaList(ps);
		}finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String UPDATE_RATEPLAN_REGCHARGE = "UPDATE registration_charges totab SET " +
		" ip_reg_charge = doroundvarying(fromtab.ip_reg_charge,?,?), op_reg_charge = doroundvarying(fromtab.op_reg_charge,?,?)," +
		" gen_reg_charge = doroundvarying(fromtab.gen_reg_charge,?,?), reg_renewal_charge = doroundvarying(fromtab.reg_renewal_charge,?,?)," +
		" mrcharge = doroundvarying(fromtab.mrcharge,?,?), ip_mlccharge = doroundvarying(fromtab.ip_mlccharge,?,?)," +
		" op_mlccharge = doroundvarying(fromtab.op_mlccharge,?,?), " +
		" op_mlccharge_discount = doroundvarying(fromtab.op_mlccharge_discount,?,?), " +
		" mrcharge_discount = doroundvarying(fromtab.mrcharge_discount,?,?), " +
		" ip_mlccharge_discount = doroundvarying(fromtab.ip_mlccharge_discount,?,?), " +
		" ip_reg_charge_discount = doroundvarying(fromtab.ip_reg_charge_discount,?,?), " +
		" reg_renewal_charge_discount = doroundvarying(fromtab.reg_renewal_charge_discount,?,?), " +
		" op_reg_charge_discount = doroundvarying(fromtab.op_reg_charge_discount,?,?), " +
		" gen_reg_charge_discount = doroundvarying(fromtab.gen_reg_charge_discount,?,?) " +
		" FROM registration_charges fromtab" +
		" WHERE totab.org_id = ? AND fromtab.org_id = ?" +
		" AND totab.bed_type = fromtab.bed_type AND totab.is_override='N'";


	public boolean updateRegChargesForDerivedRatePlans(String orgId,String varianceType,
			Double varianceValue,Double varianceBy,String baseOrgId,
			Double nearstRoundOfValue, String userName,String orgName,boolean upload) throws SQLException, Exception {

		boolean success = false;
		Connection con = null;

		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			List<BasicDynaBean> rateSheetList =  getRateSheetsByPriority(con, orgId, ratePlanParametersDAO);
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

		}finally {
			DataBaseUtil.commitClose(con, success);
		}

		return success;
	}

	public boolean updateBedForRatePlan(Connection con, String ratePlanId,
			Double variance,String rateSheetId, Double rndOff, String bedType) throws Exception {

		boolean status = false;

		BigDecimal varianceBy = new BigDecimal(variance);
	    BigDecimal roundOff = new BigDecimal(rndOff);

		Object updparams[] = {varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff,varianceBy, roundOff,varianceBy, roundOff,
				varianceBy, roundOff, varianceBy, roundOff,varianceBy, roundOff,varianceBy, roundOff,varianceBy, roundOff,varianceBy, roundOff,
				varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff, ratePlanId, rateSheetId,bedType};
		status = updateCharges(con,UPDATE_RATEPLAN_REGCHARGE + " AND totab.bed_type=? ", updparams);
		return status;
	}

}
