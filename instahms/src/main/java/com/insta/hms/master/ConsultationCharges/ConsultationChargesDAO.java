package com.insta.hms.master.ConsultationCharges;

import com.bob.hms.adminmasters.organization.OrgMasterDao;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
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
import java.util.List;
import java.util.Map;

public class ConsultationChargesDAO extends ItemChargeDAO {

	static Logger logger = LoggerFactory.getLogger(ConsultationChargesDAO.class);

	public ConsultationChargesDAO() {
		super("consultation_charges");
	}

	public List<BasicDynaBean> getAllChargesForOrganisation(String orgId) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_ALL_CHARGES_FOR_ORG);
			ps.setString(1, orgId);
			return DataBaseUtil.queryToDynaList(ps);

		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String CONSULTATION_CHARGE_FIELDS = " select * ";

	private static final String CONSULTATION_CHARGE_QUERY_COUNT =
		" SELECT count(*) ";

	private static final String CONSULTATION_CHARGE_TABLES =
		" FROM (SELECT cod.consultation_type_id, cod.org_id, cod.applicable, " +
		" ct.consultation_type, ct.status, ct.consultation_code,ct.patient_type,od.org_name, " +
		" 'consultation'::text as chargeCategory, cod.is_override "+
		" FROM consultation_org_details cod " +
		" JOIN organization_details od on (od.org_id = cod.org_id) "+
		" JOIN consultation_types ct ON (ct.consultation_type_id = cod.consultation_type_id))   AS foo " ;

	public static PagedList searchList(Map filter, Map listing) throws Exception {

		Connection con = DataBaseUtil.getReadOnlyConnection();

		SearchQueryBuilder qb = new SearchQueryBuilder(con,CONSULTATION_CHARGE_FIELDS, CONSULTATION_CHARGE_QUERY_COUNT,
				CONSULTATION_CHARGE_TABLES, listing);

		qb.addFilterFromParamMap(filter);
		qb.addSecondarySort("consultation_type_id");
		qb.build();

		PagedList l = qb.getMappedPagedList();

		qb.close();
		con.close();

		return l;
	}

	private static final String GET_ALL_CHARGES_FOR_ORG =
		" select consultation_type_id,bed_type,charge, discount from consultation_charges where org_id=? " ;

	public List<BasicDynaBean> getAllChargesForOrg(String orgId, String consultId) throws SQLException {

		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_ALL_CHARGES_FOR_ORG + "and consultation_type_id=? ");
			ps.setString(1, orgId);
			ps.setInt(2, Integer.parseInt(consultId));
			return DataBaseUtil.queryToDynaList(ps);

		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static final String CONSULT_ORG_DETAILS = "select * from consultation_org_details  " +
			" where consultation_type_id =? and org_id=? ";

	public BasicDynaBean getConsultOrgDetailsBean(String consultId,String orgId)throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(CONSULT_ORG_DETAILS);
			ps.setInt(1, Integer.parseInt(consultId));
			ps.setString(2, orgId);
			return DataBaseUtil.queryToDynaBean(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String COPY_CONSULT_DETAILS_TO_ALL_ORGS =
		" INSERT INTO consultation_org_details (consultation_type_id, org_id, applicable) " +
		" SELECT cod.consultation_type_id, o.org_id, cod.applicable " +
		" FROM organization_details o " +
		"  JOIN consultation_org_details cod ON (cod.consultation_type_id=? AND cod.org_id='ORG0001') " +
		" WHERE o.org_id != 'ORG0001'";


	public void copyConsultDetailsToAllOrgs(Connection con, int consultId) throws SQLException {
		PreparedStatement ps = con.prepareStatement(COPY_CONSULT_DETAILS_TO_ALL_ORGS);
		ps.setInt(1, consultId);
		ps.executeUpdate();
		ps.close();
	}

	private static final String COPY_GENERAL_CHARGES_TO_ALL_ORGS =
		" INSERT INTO consultation_charges (org_id, bed_type, consultation_type_id, charge, discount) " +
		" SELECT abo.org_id, abo.bed_type, cc.consultation_type_id, cc.charge, cc.discount " +
		" FROM all_beds_orgs_view abo " +
		"  JOIN consultation_charges cc ON (cc.consultation_type_id=? AND cc.bed_type = abo.bed_type " +
		"    AND cc.org_id = 'ORG0001') " +
		" WHERE abo.org_id != 'ORG0001'";

	public void copyGeneralChargesToAllOrgs(Connection con, int consulId) throws SQLException {
		PreparedStatement ps = con.prepareStatement(COPY_GENERAL_CHARGES_TO_ALL_ORGS);
		ps.setInt(1, consulId);
		ps.executeUpdate();
		ps.close();
	}

	private static final String INSERT_CONSULTATION_CHARGE_PLUS = "INSERT INTO consultation_charges(consultation_type_id,org_id,charge,bed_type,discount,username)" +
		"(SELECT consultation_type_id,?,ROUND(charge + ?), bed_type, discount,? FROM consultation_charges WHERE org_id=?)";

	private static final String INSERT_CONSULTATION_CHARGE_MINUS = "INSERT INTO consultation_charges(consultation_type_id,org_id,charge,bed_type,discount,username)" +
		"(SELECT consultation_type_id,?, GREATEST(ROUND(charge - ?), 0), bed_type, discount,? FROM consultation_charges WHERE org_id=?)";

	private static final String INSERT_CONSULTATION_CHARGE_BY = "INSERT INTO consultation_charges(consultation_type_id,org_id,charge,bed_type,discount,username)" +
		"(SELECT consultation_type_id,?,doroundvarying(charge,?,?), bed_type, discount,? FROM consultation_charges WHERE org_id=?)";

	private static final String INSERT_CONSULTATION_WITH_DISCOUNTS_BY = "INSERT INTO consultation_charges(consultation_type_id,org_id,charge,bed_type,discount,username)" +
	"(SELECT consultation_type_id,?,doroundvarying(charge,?,?), bed_type, doroundvarying(discount,?,?), ? FROM consultation_charges WHERE org_id=?)";

	public static boolean addOrgForConsultations(Connection con,String newOrgId,String varianceType,
			Double varianceValue,Double varianceBy,boolean useValue,String baseOrgId,
			Double nearstRoundOfValue,String userName,String orgName) throws Exception{
		return addOrgForConsultations(con,newOrgId,varianceType,
				varianceValue,varianceBy,useValue,baseOrgId,
				nearstRoundOfValue,userName,orgName, false);
	}

	public static boolean addOrgForConsultations(Connection con,String newOrgId,String varianceType,
		Double varianceValue,Double varianceBy,boolean useValue,String baseOrgId,
		Double nearstRoundOfValue,String userName,String orgName, boolean updateDiscounts) throws Exception{
   		boolean status = false;
   		PreparedStatement ps = null;

			if(useValue){
				if(varianceType.equals("Incr")){
					ps = con.prepareStatement(INSERT_CONSULTATION_CHARGE_PLUS);
					ps.setString(1,newOrgId);
					ps.setDouble(2,varianceValue);
					ps.setString(3, userName);
					ps.setString(4, baseOrgId);

					int i = ps.executeUpdate();
					logger.debug(Integer.toString(i));
					if(i>=0)status = true;
				}else{
					ps = con.prepareStatement(INSERT_CONSULTATION_CHARGE_MINUS);
					ps.setString(1,newOrgId);
					ps.setDouble(2,varianceValue);
					ps.setString(3, userName);
					ps.setString(4, baseOrgId);

					int i = ps.executeUpdate();
					logger.debug(Integer.toString(i));
					if(i>=0)status = true;
			}
		 }else{
			 if(!varianceType.equals("Incr")){
				 	varianceBy = new Double(-varianceBy);
			 }
/*				 ps = con.prepareStatement(INSERT_CONSULTATION_CHARGE_BY);
				 ps.setString(1, newOrgId);
				 ps.setBigDecimal(2, new BigDecimal(varianceBy));
				 ps.setBigDecimal(3, new BigDecimal(nearstRoundOfValue));
				 ps.setString(4, userName);
				 ps.setString(5, baseOrgId);

				 int i = ps.executeUpdate(); */
			 int i = insertChargesByPercent(con, newOrgId, baseOrgId, userName, varianceBy, nearstRoundOfValue, updateDiscounts);
				 logger.debug(Integer.toString(i));
				if(i>=0)status = true;
		 }
		if (null != ps) ps.close();

   		return status;
	}

	private static final String INSERT_CONSULTATION_CODES_FOR_ORG = "INSERT INTO consultation_org_details " +
		"   SELECT consultation_type_id, ?, applicable, item_code, code_type, ?, ?, ?, 'N'" +
		"	FROM consultation_org_details WHERE org_id=?";

	public static boolean addOrgCodesForConsultations(Connection con,String newOrgId,String varianceType,
		Double varianceValue,Double varianceBy,boolean useValue,String baseOrgId,
		Double nearstRoundOfValue, String userName) throws Exception{
 		boolean status = false;
 		PreparedStatement ps = null;
        BasicDynaBean obean = new OrgMasterDao().findByKey(con, "org_id", newOrgId);
        String rateSheetId = ("N".equals((String)obean.get("is_rate_sheet")) ? baseOrgId : null);
		try{
	   		ps = con.prepareStatement(INSERT_CONSULTATION_CODES_FOR_ORG);
			ps.setString(1,newOrgId);
			ps.setString(2, userName);
			ps.setTimestamp(3, DateUtil.getCurrentTimestamp());
			ps.setString(4, rateSheetId);
			ps.setString(5, baseOrgId);

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
		return addOrgCodesForConsultations(con, newOrgId, null, null, null, false, baseOrgId, null, userName);
	}

    private static int insertChargesByPercent(Connection con, String newOrgId, String baseOrgId, String userName,
            Double varianceBy, Double nearstRoundOfValue, boolean updateDiscounts) throws Exception {

	    int ndx = 1;
	    int numCharges = 1;

	    PreparedStatement pstmt = null;
	    try {
			pstmt = con.prepareStatement(updateDiscounts ?
					INSERT_CONSULTATION_WITH_DISCOUNTS_BY : INSERT_CONSULTATION_CHARGE_BY);
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

			pstmt.setString(ndx++, userName);
			pstmt.setString(ndx++, baseOrgId);

			return pstmt.executeUpdate();

	    } finally {
            if (null != pstmt) pstmt.close();
	    }
    }

    private static String INIT_ORG_DETAILS = "INSERT INTO consultation_org_details " +
	" (consultation_type_id, org_id, applicable, item_code, code_type, base_rate_sheet_id, is_override) " +
	" SELECT consultation_type_id, ?, false, null, null, null, 'N'" +
	" FROM consultation_types";

	private static String INIT_CHARGES = "INSERT INTO consultation_charges(consultation_type_id,org_id,bed_type," +
	    "charge,username)" +
	    "(SELECT consultation_type_id, ?, abov.bed_type, 0.0, ? " +
	    "FROM consultation_types ct CROSS JOIN all_beds_orgs_view abov WHERE abov.org_id =? ) ";

	private static final String INSERT_CHARGES = "INSERT INTO consultation_charges(consultation_type_id,org_id,bed_type," +
	" charge,username, is_override)" +
	" SELECT cc.consultation_type_id, ?, cc.bed_type, " +
	" doroundvarying(cc.charge, ?, ?), " +
    " ?, 'N' " +
    " FROM consultation_charges cc, consultation_org_details cod, consultation_org_details codtarget " +
    " where cc.org_id = cod.org_id and cc.consultation_type_id = cod.consultation_type_id " +
    " and codtarget.org_id = ? and codtarget.consultation_type_id = cod.consultation_type_id and codtarget.base_rate_sheet_id = ? " +
    " and cod.applicable = true "+
    " and cc.org_id = ? ";

	private static final String UPDATE_CHARGES = "UPDATE consultation_charges AS target SET " +
	    " charge = doroundvarying(cc.charge, ?, ?), " +
	    " discount = doroundvarying(cc.discount, ?, ?), " +
	    " username = ?, is_override = 'N' " +
	    " FROM consultation_charges cc, consultation_org_details cod " +
	    " where cod.org_id = ? and cc.consultation_type_id = cod.consultation_type_id and cod.base_rate_sheet_id = ? and" +
	    " target.consultation_type_id = cc.consultation_type_id " +
	    " and target.bed_type = cc.bed_type and " +
	    " cod.applicable = true and target.is_override != 'Y'"+
	    " and cc.org_id = ? and target.org_id = ?";

	private static final String UPDATE_EXCLUSIONS = "UPDATE consultation_org_details AS target " +
	    " SET item_code = cod.item_code, code_type = cod.code_type," +
	    " applicable = true, base_rate_sheet_id = cod.org_id, is_override = 'N' " +
	    " FROM consultation_org_details cod WHERE cod.consultation_type_id = target.consultation_type_id and " +
	    " cod.org_id = ? and cod.applicable = true and target.org_id = ? and target.applicable = false and target.is_override != 'Y'";

	public boolean updateRatePlan(Connection con,String newOrgId, String baseOrgId,
					 String varianceType, Double variance, Double rndOff,
					 String userName, String orgName ) throws Exception {

	    boolean status = false;
		// disableAuditTriggers("consultation_charges", "z_diagnostictest_charges_audit_trigger");

		if(!varianceType.equals("Incr")) {
			variance = new Double(-variance);
	    }

		BigDecimal varianceBy = new BigDecimal(variance);
        BigDecimal roundOff = new BigDecimal(rndOff);


	    Object updparams[] = {varianceBy, roundOff, varianceBy, roundOff, userName, newOrgId, baseOrgId, baseOrgId, newOrgId};
	    Object insparams[] = {newOrgId, varianceBy, roundOff, userName, newOrgId, baseOrgId, baseOrgId};
		status = updateExclusions(con, UPDATE_EXCLUSIONS, newOrgId, baseOrgId);
		if(status) status = updateCharges(con, UPDATE_CHARGES, updparams);
		// postAuditEntry(con, "consultation_charges_audit_log", userName, orgName);
		return status;

	}

    public boolean initRatePlan(Connection con, String newOrgId, String varianceType,
			Double varianceBy,String baseOrgId, Double roundOff, String userName, String orgName) throws Exception {
		boolean status = addOrgCodesForItems(con, newOrgId, baseOrgId, userName);
		if (status) status = addOrgForConsultations(con,newOrgId, varianceType,
				0.0, varianceBy, false, baseOrgId,
				roundOff, userName, orgName, true);
		return status;
    }

    private static final String REINIT_EXCLUSIONS = "UPDATE consultation_org_details as target " +
    " SET applicable = cod.applicable, base_rate_sheet_id = cod.org_id, " +
    " item_code = cod.item_code, code_type = cod.code_type, is_override = 'N' " +
    " FROM consultation_org_details cod WHERE cod.consultation_type_id = target.consultation_type_id and " +
    " cod.org_id = ? and target.org_id = ? and target.is_override != 'Y'";

    public boolean reinitRatePlan(Connection con, String newOrgId, String varianceType,
			Double variance,String baseOrgId, Double rndOff,String userName, String orgName) throws Exception {
		boolean status = false;
		if(!varianceType.equals("Incr")) {
			variance = new Double(-variance);
		}
		BigDecimal varianceBy = new BigDecimal(variance);
        BigDecimal roundOff = new BigDecimal(rndOff);

		Object updparams[] = {varianceBy, roundOff, varianceBy, roundOff,
				newOrgId, baseOrgId};
		status = updateExclusions(con, REINIT_EXCLUSIONS, newOrgId, baseOrgId);
		if (status) status = updateCharges(con,UPDATE_RATEPLAN_CONS_CHARGES , updparams);
		return status;
    }

    private static String INIT_ITEM_ORG_DETAILS = "INSERT INTO consultation_org_details " +
    "(consultation_type_id, org_id, applicable, item_code, code_type, base_rate_sheet_id, is_override)" +
    " ( SELECT ?, od.org_id, false, null, null, prspv.base_rate_sheet_id, 'N' FROM organization_details od " +
    " LEFT OUTER JOIN priority_rate_sheet_parameters_view prspv ON od.org_id = prspv.org_id )";

    private static String INIT_ITEM_CHARGES = "INSERT INTO consultation_charges(consultation_type_id,org_id,bed_type," +
    "charge, username)" +
    "(SELECT ?, abov.org_id, abov.bed_type, 0.0, ? FROM all_beds_orgs_view abov) ";

	public boolean initItemCharges(Connection con, int consultationTypeId, String userName) throws Exception {

        boolean status = false;
        //  disableAuditTriggers("service_master_charges", "z_services_charges_audit_trigger");
        status = initItemCharges(con, INIT_ITEM_ORG_DETAILS, INIT_ITEM_CHARGES, consultationTypeId, userName);
        // postAuditEntry(con, "service_master_charges_audit_log", userName, orgName);

        return status;
    }

	private static final String UPDATE_CONS_CHARGES_PLUS = "UPDATE consultation_charges totab SET " +
		" charge = round(fromtab.charge + ?)" +
		" FROM consultation_charges fromtab" +
		" WHERE totab.org_id = ? AND fromtab.org_id = ?" +
		" AND totab.consultation_type_id = fromtab.consultation_type_id AND totab.bed_type = fromtab.bed_type AND totab.is_override='N'";

	private static final String UPDATE_CONS_CHARGES_MINUS = "UPDATE consultation_charges totab SET " +
		" charge = GREATEST(round(fromtab.charge - ?), 0)" +
		" FROM consultation_charges fromtab" +
		" WHERE totab.org_id = ? AND fromtab.org_id = ?" +
		" AND totab.consultation_type_id = fromtab.consultation_type_id AND totab.bed_type = fromtab.bed_type AND totab.is_override='N'";

	private static final String UPDATE_CONS_CHARGES_BY = "UPDATE consultation_charges totab SET " +
		" charge = doroundvarying(fromtab.charge,?,?)" +
		" FROM consultation_charges fromtab" +
		" WHERE totab.org_id = ? AND fromtab.org_id = ?" +
		" AND totab.consultation_type_id = fromtab.consultation_type_id AND totab.bed_type = fromtab.bed_type AND totab.is_override='N'";

	public static boolean updateOrgForConsultations(Connection con,String orgId,String varianceType,
		Double varianceValue,Double varianceBy,boolean useValue,String baseOrgId,
		Double nearstRoundOfValue) throws SQLException, IOException {

			boolean status = false;
			PreparedStatement pstmt = null;

			if (useValue) {

			if (varianceType.equals("Incr"))
				pstmt = con.prepareStatement(UPDATE_CONS_CHARGES_PLUS);
			else
				pstmt = con.prepareStatement(UPDATE_CONS_CHARGES_MINUS);

			pstmt.setBigDecimal(1, new BigDecimal(varianceValue));
			pstmt.setString(2, orgId);
			pstmt.setString(3, baseOrgId);

			int i = pstmt.executeUpdate();
			if (i>=0) status = true;

			} else {

			pstmt = con.prepareStatement(UPDATE_CONS_CHARGES_BY);
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

	private static String CONSULTATION_BACKUP_CHARGES = "INSERT INTO consultation_charges_backup(user_name, bkp_time," +
		" consultation_type_id, org_id, bed_type, charge, discount) " +
		" SELECT ?, current_timestamp, consultation_type_id, " +
		" org_id, bed_type, charge, discount " +
		" FROM consultation_charges WHERE org_id = ? ";

	public static void backupCharges(String orgId, String userId)throws SQLException {
		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			pstmt = con.prepareStatement(CONSULTATION_BACKUP_CHARGES);
			pstmt.setString(1, userId);
			pstmt.setString(2, orgId);
			pstmt.execute();

		} finally {
			DataBaseUtil.closeConnections(con, pstmt);
		}

	}

	public void groupIncreaseCharges(Connection con, String orgId, List<String> bedTypes,
			List<String> ids, BigDecimal amount, boolean isPercentage,
			BigDecimal roundTo, String updateTable, String userName) throws SQLException {

		if (roundTo.compareTo(BigDecimal.ZERO) == 0) {
			groupIncreaseChargesNoRoundOff(con, orgId, bedTypes, ids,
					amount, isPercentage, updateTable, userName);
		}else {
			groupIncreaseChargesWithRoundOff(con, orgId, bedTypes, ids, amount,
					isPercentage, roundTo, updateTable, userName);
		}
	}

	private String GROUP_INCR_CHARGES =
		" UPDATE consultation_charges " +
		" SET charge=GREATEST( round((charge+?)/?,0)*?, 0), username = ? " +
		" WHERE org_id=? ";

	private String GROUP_INCR_CHARGES_PERCENTAGE =
		" UPDATE consultation_charges " +
		" SET charge=GREATEST( round(charge*(100+?)/100/?,0)*?, 0), username = ? " +
		" WHERE org_id=? ";

	private String GROUP_INCR_DISCOUNTS =
		" UPDATE consultation_charges " +
		" SET discount=LEAST(GREATEST( round((discount+?)/?,0)*?, 0), charge), username = ? " +
		" WHERE org_id=? ";

	private String GROUP_INCR_DISCOUNTS_PERCENTAGE =
		" UPDATE consultation_charges " +
		" SET discount=LEAST(GREATEST( round(discount*(100+?)/100/?,0)*?, 0), charge), username = ? " +
		" WHERE org_id=? ";

	private String GROUP_APPLY_DISCOUNTS =
		" UPDATE consultation_charges " +
		" SET discount=LEAST(GREATEST( round((charge+?)/?,0)*?, 0), charge), username = ? " +
		" WHERE org_id=? ";

	private String GROUP_APPLY_DISCOUNTS_PERCENTAGE =
		" UPDATE consultation_charges " +
		" SET discount=LEAST(GREATEST( round(charge+(charge*?/100/?),0)*?, 0), charge), username = ? " +
		" WHERE org_id=? ";

	private static final String AUDIT_LOG_HINT = ":GUP";

	public void groupIncreaseChargesWithRoundOff(Connection con, String orgId, List<String> bedTypes,
			List<String> ids, BigDecimal amount, boolean isPercentage,
			BigDecimal roundTo, String updateTable, String userName)
		throws SQLException {

		String userNameWithHint = ((null == userName) ? "" : userName) + AUDIT_LOG_HINT;
		StringBuilder query = null;

		if(updateTable !=null && updateTable.equals("UPDATECHARGE")) {
			query = new StringBuilder(
				isPercentage ? GROUP_INCR_CHARGES_PERCENTAGE
						: GROUP_INCR_CHARGES);

		}else if(updateTable.equals("UPDATEDISCOUNT")) {
			query = new StringBuilder(
				isPercentage ? GROUP_INCR_DISCOUNTS_PERCENTAGE
						: GROUP_INCR_DISCOUNTS);
		}else {
			query = new StringBuilder(
				isPercentage ? GROUP_APPLY_DISCOUNTS_PERCENTAGE
						: GROUP_APPLY_DISCOUNTS);
		}

		SearchQueryBuilder.addWhereFieldOpValue(true, query, "bed_type", "IN", bedTypes);
		SearchQueryBuilder.addWhereFieldOpValue(true, query, "consultation_type_id", "IN", ids);

		PreparedStatement ps = con.prepareStatement(query.toString());

		// sanity: round to zero is not allowed, can cause div/0
		if (roundTo.equals(BigDecimal.ZERO))
			roundTo = BigDecimal.ONE;

		int i=1;
		ps.setBigDecimal(i++, amount);
		ps.setBigDecimal(i++, roundTo);
		ps.setBigDecimal(i++, roundTo);		// roundTo appears twice in the query
		ps.setString(i++, userNameWithHint);
		ps.setString(i++, orgId);

		if (bedTypes != null) for (String bedType : bedTypes) {
			ps.setString(i++, bedType);
		}
		if (ids != null) for (String id : ids) {
			ps.setInt(i++, Integer.parseInt(id));
		}

		ps.executeUpdate();
		if (ps != null) ps.close();
	}

	private String GROUP_INCR_CHARGES_NO_ROUNDOFF =
		" UPDATE consultation_charges " +
		" SET charge = GREATEST( charge + ?, 0), username = ? " +
		" WHERE org_id=? ";

	private String GROUP_INCR_CHARGES_PERCENTAGE_NO_ROUNDOFF =
		" UPDATE consultation_charges " +
		" SET charge = GREATEST(charge +(charge * ? / 100 ) , 0), username = ? " +
		" WHERE org_id=? ";

	private String GROUP_INCR_DISCOUNTS_NO_ROUNDOFF =
		" UPDATE consultation_charges " +
		" SET discount = LEAST(GREATEST( discount + ?, 0), charge), username = ? " +
		" WHERE org_id=? ";

	private String GROUP_INCR_DISCOUNTS_PERCENTAGE_NO_ROUNDOFF =
		" UPDATE consultation_charges " +
		" SET discount = LEAST(GREATEST(discount +(discount * ? / 100 ) , 0), charge), username = ? " +
		" WHERE org_id=? ";

	private String GROUP_APPLY_DISCOUNTS_NO_ROUNDOFF =
		" UPDATE consultation_charges " +
		" SET discount = LEAST(GREATEST( charge + ?, 0), charge), username = ? " +
		" WHERE org_id=? ";

	private String GROUP_APPLY_DISCOUNTS_PERCENTAGE_NO_ROUNDOFF =
		" UPDATE consultation_charges " +
		" SET discount = LEAST(GREATEST( charge + (charge * ? / 100) , 0), charge), username = ? " +
		" WHERE org_id=? ";

	public void groupIncreaseChargesNoRoundOff(Connection con, String orgId, List<String> bedTypes,
			List<String> ids, BigDecimal amount, boolean isPercentage,
			String updateTable, String userName) throws SQLException {

		String userNameWithHint = ((null == userName) ? "" : userName) + AUDIT_LOG_HINT;
		StringBuilder query = null;

		if(updateTable !=null && updateTable.equals("UPDATECHARGE")) {
			query = new StringBuilder(
				isPercentage ?
						GROUP_INCR_CHARGES_PERCENTAGE_NO_ROUNDOFF :
						GROUP_INCR_CHARGES_NO_ROUNDOFF);

		}else if(updateTable.equals("UPDATEDISCOUNT")) {
			query = new StringBuilder(
				isPercentage ?
						GROUP_INCR_DISCOUNTS_PERCENTAGE_NO_ROUNDOFF :
						GROUP_INCR_DISCOUNTS_NO_ROUNDOFF);
		}else {
			query = new StringBuilder(
				isPercentage ?
						GROUP_APPLY_DISCOUNTS_PERCENTAGE_NO_ROUNDOFF :
						GROUP_APPLY_DISCOUNTS_NO_ROUNDOFF);
		}

		SearchQueryBuilder.addWhereFieldOpValue(true, query, "bed_type", "IN", bedTypes);
		SearchQueryBuilder.addWhereFieldOpValue(true, query, "consultation_type_id", "IN", ids);

		PreparedStatement ps = con.prepareStatement(query.toString());

		int i=1;
		ps.setBigDecimal(i++, amount);
		ps.setString(i++, userNameWithHint);
		ps.setString(i++, orgId);

		if (bedTypes != null) for (String bedType : bedTypes) {
			ps.setString(i++, bedType);
		}
		if (ids != null) for (String id : ids) {
			ps.setInt(i++, Integer.parseInt(id));
		}

		ps.executeUpdate();
		if (ps != null) ps.close();
	}


	public boolean updateOrgForDerivedRatePlans(Connection con, String[] ratePlanIds, String[] applicable,
			String consultationTypeId) throws Exception{
		return updateOrgForDerivedRatePlans(con,ratePlanIds, applicable, "consultation_org_details", "consultation",
				"consultation_type_id", consultationTypeId);
	}

	public  boolean updateChargesForDerivedRatePlans(Connection con,String baseRateSheetId, String[] ratePlanIds,
	    		String[] bedType, Double[] regularcharges,String consultationTypeId, Double[] discounts, String[] applicable)throws Exception {

		return updateChargesForDerivedRatePlans(con,baseRateSheetId,ratePlanIds,bedType,
				regularcharges, "consultation_charges","consultation_org_details","consultation",
				"consultation_type_id",consultationTypeId,discounts,applicable);
	}

	private static final String GET_DERIVED_RATE_PALN_DETAILS = "select rp.org_id,od.org_name, "+
		" case when rate_variation_percent<0 then 'Decrease By' else 'Increase By' end as discormarkup, "+
		" rate_variation_percent,round_off_amount,cod.applicable,cod.consultation_type_id,rp.base_rate_sheet_id, cod.is_override  "+
		" from rate_plan_parameters rp "+
		" join organization_details od on(od.org_id=rp.org_id) "+
		" join consultation_org_details cod on (cod.org_id = rp.org_id) "+
		" where rp.base_rate_sheet_id =?  and consultation_type_id=?  and cod.base_rate_sheet_id=?";

	public List<BasicDynaBean> getDerivedRatePlanDetails(String baseRateSheetId,String consultationTypeId)throws SQLException {
		return getDerivedRatePlanDetails(baseRateSheetId, "consultation", consultationTypeId,GET_DERIVED_RATE_PALN_DETAILS);
	}

	private static final String UPDATE_RATEPLAN_CONS_CHARGES = "UPDATE consultation_charges totab SET " +
		" charge = doroundvarying(fromtab.charge,?,?), discount = doroundvarying(fromtab.discount,?,?) " +
		" FROM consultation_charges fromtab" +
		" WHERE totab.org_id = ? AND fromtab.org_id = ?" +
		" AND totab.consultation_type_id = fromtab.consultation_type_id AND totab.bed_type = fromtab.bed_type AND totab.is_override='N'";

	public boolean updateConsultationChargesForDerivedRatePlans(String orgId,String varianceType,
		Double varianceValue,Double varianceBy,String baseOrgId,
		Double nearstRoundOfValue,String userName, String orgName, boolean upload) throws SQLException, Exception {

			boolean success = false;
			Connection con = null;

			try{
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);
				if(upload) {
					GenericDAO rateParameterDao = new GenericDAO("rate_plan_parameters");
					List<BasicDynaBean> rateSheetList =  getRateSheetsByPriority(con, orgId, rateParameterDao);
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
					Object updparams[] = {variance, roundoff, variance, roundoff, userName, orgId, baseOrgId, baseOrgId, orgId};
					success = updateCharges(con, UPDATE_CHARGES, updparams);
				}

			}finally {
				DataBaseUtil.commitClose(con, success);
			}
		return success;
	}

	public static boolean updateChargesBasedOnNewRateSheet(Connection con, String orgId,Double varianceBy,
			String baseOrgId,Double nearstRoundOfValue, String consultationTypeId)throws SQLException,Exception {
		boolean success = false;
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(UPDATE_RATEPLAN_CONS_CHARGES+ " AND totab.consultation_type_id = ? ");
			ps.setBigDecimal(1, new BigDecimal(varianceBy));
			ps.setBigDecimal(2, new BigDecimal(nearstRoundOfValue));
			ps.setBigDecimal(3, new BigDecimal(varianceBy));
			ps.setBigDecimal(4, new BigDecimal(nearstRoundOfValue));
			ps.setString(5, orgId);
			ps.setString(6, baseOrgId);
			ps.setInt(7, Integer.parseInt(consultationTypeId));

			int i = ps.executeUpdate();
			if (i>=0) success = true;
		}finally {
			if (ps != null)
                ps.close();
		}
		return success;
	}

	public boolean updateBedForRatePlan(Connection con, String ratePlanId,
			Double variance,String rateSheetId, Double rndOff, String bedType) throws Exception {

		boolean status = false;

		BigDecimal varianceBy = new BigDecimal(variance);
	    BigDecimal roundOff = new BigDecimal(rndOff);

		Object updparams[] = {varianceBy, roundOff, varianceBy, roundOff,
				ratePlanId, rateSheetId,bedType};
		status = updateCharges(con,UPDATE_RATEPLAN_CONS_CHARGES + " AND totab.bed_type=? ", updparams);
		return status;
	}
}