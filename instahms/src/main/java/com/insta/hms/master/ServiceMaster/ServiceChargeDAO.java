package com.insta.hms.master.ServiceMaster;

import au.com.bytecode.opencsv.CSVWriter;
import com.bob.hms.adminmasters.organization.OrgMasterDao;
import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.auditlog.AuditLogDao;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.jobs.CronJobService;
import com.insta.hms.master.rateplan.ItemChargeDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;

public class ServiceChargeDAO extends ItemChargeDAO {

	static Logger logger = LoggerFactory.getLogger(ServiceMasterDAO.class);

	public ServiceChargeDAO() {
		super("service_master_charges");
	}

	/*
	 * Returns charges for multiple services, for given bed types for the given rate plan.
	 * Each bed type's charge will be a column in the returned list. The name of the column
	 * is the Bed Type name. Since the available bed types can only be determined at runtime,
	 * we have to construct the query dynamically.
	 *
	 * This is used for displaying the charges for each bed in the main list master screen
	 *
	 */
	public List<BasicDynaBean> getAllChargesForBedTypes(String orgId, List<String> bedTypes,
			List<String> ids)
		throws SQLException {

		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = getAllChargesForBedTypesStmt(con, orgId, bedTypes, ids);
			return DataBaseUtil.queryToDynaListWithCase(ps);

		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	/*
	 * Same as above, but writes into a CSV writer instead of returning a list.
	 */
	public void getAllChargesForBedTypesCSV(String orgId, List<String> bedTypes, CSVWriter w)
		throws SQLException, IOException {

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = getAllChargesForBedTypesStmt(con, orgId, bedTypes, null);
			rs = ps.executeQuery();
			w.writeAll(rs,true);
			w.flush();

		} finally {
			DataBaseUtil.closeConnections(con, ps, rs);
		}
	}

	/*
	 * Common function to support the above
	 */
	public PreparedStatement getAllChargesForBedTypesStmt(Connection con, String orgId,
			List<String> bedTypes, List<String> ids) throws SQLException {

		StringBuilder query = new StringBuilder();
			query.append("SELECT s.service_id, service_name, sod.applicable AS  " +
					  DataBaseUtil.quoteIdent("Rate Plan Applicable", true));
			query.append(", sod.item_code AS " +
					  DataBaseUtil.quoteIdent("Itemcode", true));
		for (String bedType: bedTypes) {
			query.append(", (SELECT unit_charge FROM service_master_charges WHERE " +
					" service_id=s.service_id AND bed_type=? AND org_id=?) AS " +
					DataBaseUtil.quoteIdent(bedType, true));
			query.append(", (SELECT discount FROM service_master_charges WHERE " +
					" service_id=s.service_id AND bed_type=? AND org_id=?) AS " +
					DataBaseUtil.quoteIdent(bedType+"(Discount)", true));
		}
			query.append(" FROM services s" +
					 " JOIN service_org_details sod ON (s.service_id=sod.service_id AND sod.org_id=? )");
		if(ids == null)
			query.append("WHERE  s.status='A' ");

		SearchQueryBuilder.addWhereFieldOpValue(false, query, "s.service_id", "IN", ids);

		PreparedStatement ps = null;
		ps = con.prepareStatement(query.toString());

		int i=1;
		for (String bedType: bedTypes) {
			ps.setString(i++, bedType);
			ps.setString(i++, orgId);
			ps.setString(i++, bedType);
			ps.setString(i++, orgId);
		}
			ps.setString(i++, orgId);
		if (ids != null) {
			for (String id: ids) {
				ps.setString(i++, id);
			}
		}
		return ps;
	}

	/*
	 * Copy the GENERAL rate plan charges to all other rate plans. Assumes that the other
	 * rate-plan charges are non-existent, ie, no updates only inserts new charges.
	 */
	private static final String COPY_GENERAL_CHARGES_TO_ALL_ORGS =
		" INSERT INTO service_master_charges (org_id, bed_type, service_id, unit_charge, discount) " +
		" SELECT abo.org_id, abo.bed_type, sc.service_id, sc.unit_charge, sc.discount " +
		" FROM all_beds_orgs_view abo " +
		"  JOIN service_master_charges sc ON (sc.service_id=? AND sc.bed_type = abo.bed_type " +
		"    AND sc.org_id = 'ORG0001') " +
		" WHERE abo.org_id != 'ORG0001'";

	public void copyGeneralChargesToAllOrgs(Connection con, String serviceId) throws SQLException {
		PreparedStatement ps = con.prepareStatement(COPY_GENERAL_CHARGES_TO_ALL_ORGS);
		ps.setString(1, serviceId);
		ps.executeUpdate();
		ps.close();
	}

	/*
	 * Copy the GENERAL bed type charges to all inactive bed types for the given Service ID.
	 * These charges will not be inserted normally when the services is created, since
	 * only active bed charges are input from the user.
	 */
	private static final String COPY_GENERAL_CHARGES_TO_INACTIVE_BEDS =
		" INSERT INTO service_master_charges (org_id, bed_type, service_id, unit_charge, discount) " +
		" SELECT abo.org_id, abo.bed_type, sc.service_id, sc.unit_charge, sc.discount " +
		" FROM all_beds_orgs_view abo " +
		"   JOIN service_master_charges sc ON (sc.org_id = abo.org_id AND sc.bed_type = 'GENERAL') " +
		" WHERE abo.bed_type IN ( " +
		"     SELECT DISTINCT intensive_bed_type FROM icu_bed_charges WHERE bed_status = 'I' " +
		"     UNION ALL SELECT DISTINCT bed_type FROM bed_details WHERE bed_status = 'I') " +
		"   AND sc.service_id=? " ;

	public void copyGeneralChargesToInactiveBeds(Connection con, String serviceId) throws SQLException {
		PreparedStatement ps = con.prepareStatement(COPY_GENERAL_CHARGES_TO_INACTIVE_BEDS);
		ps.setString(1, serviceId);
		ps.executeUpdate();
		ps.close();
	}

	public void groupIncreaseCharges(Connection con, String orgId, List<String> bedTypes,
			List<String> ids, BigDecimal amount, boolean isPercentage,
			BigDecimal roundTo, String updateTable, String userName) throws SQLException {

		if (roundTo.compareTo(BigDecimal.ZERO) == 0) {
			groupIncreaseChargesNoRoundOff(con, orgId, bedTypes, ids, amount,
					isPercentage, updateTable, userName);
		}else {
			groupIncreaseChargesWithRoundOff(con, orgId, bedTypes, ids, amount,
					isPercentage, roundTo, updateTable, userName);
		}
	}

	/*
	 * Group increase charges: takes in:
	 *  - orgId to update (reqd)
	 *  - list of bed types to update (optional, if not given, all bed types)
	 *  - list of service IDs to update (optional, if not given, all services)
	 *  - amount to increase by (can be negative for a decrease)
	 *  - whether the amount is a percentage instead of an abs. amount
	 *  - an amount to be rounded to (nearest). Rounding to 0 is invalid.
	 *
	 * The new amount will not be allowed to go less than zero.
	 */
	private static final String GROUP_INCR_CHARGES =
		" UPDATE service_master_charges " +
		" SET unit_charge=GREATEST( round((unit_charge+?)/?,0)*?, 0), username = ? " +
		" WHERE org_id=? ";

	private static final String GROUP_INCR_CHARGES_PERCENTAGE =
		" UPDATE service_master_charges " +
		" SET unit_charge=GREATEST( round(unit_charge*(100+?)/100/?,0)*?, 0), username = ? " +
		" WHERE org_id=? ";

	private static final String GROUP_INCR_DISCOUNTS =
		" UPDATE service_master_charges " +
		" SET discount=LEAST(GREATEST( round((discount+?)/?,0)*?, 0), unit_charge), username = ? " +
		" WHERE org_id=? ";

	private static final String GROUP_INCR_DISCOUNTS_PERCENTAGE =
		" UPDATE service_master_charges " +
		" SET discount=LEAST(GREATEST( round(discount*(100+?)/100/?,0)*?, 0), unit_charge), username = ? " +
		" WHERE org_id=? ";

	private static final String GROUP_APPLY_DISCOUNTS =
		" UPDATE service_master_charges " +
		" SET discount=LEAST(GREATEST( round((unit_charge+?)/?,0)*?, 0), unit_charge), username = ? " +
		" WHERE org_id=? ";

	private static final String GROUP_APPLY_DISCOUNTS_PERCENTAGE =
		" UPDATE service_master_charges " +
		" SET discount=LEAST(GREATEST( round(unit_charge+(unit_charge*?/100/?),0)*?, 0), unit_charge), username = ? " +
		" WHERE org_id=? ";

	private static final String AUDIT_LOG_HINT = ":GUP";

	public void groupIncreaseChargesWithRoundOff(Connection con, String orgId, List<String> bedTypes,
			List<String> ids, BigDecimal amount, boolean isPercentage,
			BigDecimal roundTo, String updateTable, String userName) throws SQLException {

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
		SearchQueryBuilder.addWhereFieldOpValue(true, query, "service_id", "IN", ids);

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
			ps.setString(i++, id);
		}

		ps.executeUpdate();
		if (ps != null) ps.close();
	}

	private static final String GROUP_INCR_CHARGES_NO_ROUNDOFF =
		" UPDATE service_master_charges " +
		" SET unit_charge = GREATEST( unit_charge + ?, 0), username = ? " +
		" WHERE org_id=? ";

	private static final String GROUP_INCR_CHARGES_PERCENTAGE_NO_ROUNDOFF =
		" UPDATE service_master_charges " +
		" SET unit_charge = GREATEST(unit_charge +(unit_charge * ? / 100 ) , 0), username = ? " +
		" WHERE org_id=? ";

	private static final String GROUP_INCR_DISCOUNTS_NO_ROUNDOFF =
		" UPDATE service_master_charges " +
		" SET discount = LEAST(GREATEST( discount + ?, 0), unit_charge), username = ? " +
		" WHERE org_id=? ";

	private static final String GROUP_INCR_DISCOUNTS_PERCENTAGE_NO_ROUNDOFF =
		" UPDATE service_master_charges " +
		" SET discount = LEAST(GREATEST(discount +(discount * ? / 100 ) , 0), unit_charge), username = ? " +
		" WHERE org_id=? ";

	private static final String GROUP_APPLY_DISCOUNTS_NO_ROUNDOFF =
		" UPDATE service_master_charges " +
		" SET discount = LEAST(GREATEST( unit_charge + ?, 0), unit_charge), username = ? " +
		" WHERE org_id=? ";

	private static final String GROUP_APPLY_DISCOUNTS_PERCENTAGE_NO_ROUNDOFF =
		" UPDATE service_master_charges " +
		" SET discount = LEAST(GREATEST( unit_charge + (unit_charge * ? / 100) , 0), unit_charge), username = ? " +
		" WHERE org_id=? ";

	public void groupIncreaseChargesNoRoundOff(Connection con, String orgId, List<String> bedTypes,
			List<String> ids, BigDecimal amount, boolean isPercentage,
			String updateTable, String userName) throws SQLException {

		String userNameWithHint = ((null == userName) ? "" : userName) + AUDIT_LOG_HINT;
		StringBuilder query = null;

		if(updateTable !=null && updateTable.equals("UPDATECHARGE")) {
			query = new StringBuilder(
				isPercentage ? GROUP_INCR_CHARGES_PERCENTAGE_NO_ROUNDOFF
						: GROUP_INCR_CHARGES_NO_ROUNDOFF);

		}else if(updateTable.equals("UPDATEDISCOUNT")) {
			query = new StringBuilder(
				isPercentage ? GROUP_INCR_DISCOUNTS_PERCENTAGE_NO_ROUNDOFF
						: GROUP_INCR_DISCOUNTS_NO_ROUNDOFF);
		}else {
			query = new StringBuilder(
				isPercentage ? GROUP_APPLY_DISCOUNTS_PERCENTAGE_NO_ROUNDOFF
						: GROUP_APPLY_DISCOUNTS_NO_ROUNDOFF);
		}

		SearchQueryBuilder.addWhereFieldOpValue(true, query, "bed_type", "IN", bedTypes);
		SearchQueryBuilder.addWhereFieldOpValue(true, query, "service_id", "IN", ids);

		PreparedStatement ps = con.prepareStatement(query.toString());

		int i=1;
		ps.setBigDecimal(i++, amount);
		ps.setString(i++, userNameWithHint);
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


	/*
	 * Backs up the current set of charges in a given rate plan into a backup table.
	 * To store a record of the "previous" state of the rate plan before updating via
	 * a CSV file upload.
	 */
	private static final String BACKUP_CHARGES =
		" INSERT INTO service_master_charges_backup (user_name, bkp_time, org_id, bed_type, service_id, " +
		"   unit_charge,discount) " +
		" SELECT ?, current_timestamp, org_id, bed_type, service_id, unit_charge, discount " +
		" FROM service_master_charges WHERE org_id=?" ;

	public void backupCharges(Connection con, String orgId, String user) throws SQLException {
		PreparedStatement ps = con.prepareStatement(BACKUP_CHARGES);
		ps.setString(1, user);
		ps.setString(2, orgId);
		ps.execute();
		ps.close();
	}

	/*
	 * Update a list of charges in a batch. Note that large batches actually slow down the
	 * update, since addBatch causes a string to be reconstructed and this exponentially
	 * slows down when there is a large number of addBatch calls.
	 */
	private static final String UPDATE_CHARGE =
		" UPDATE service_master_charges " +
		" SET unit_charge=?, discount=? " +
		" WHERE service_id=? AND org_id=? AND bed_type=?";

	public boolean updateChargeList(Connection con, List<BasicDynaBean> chargeList) throws SQLException {

		PreparedStatement ps = con.prepareStatement(UPDATE_CHARGE);

		for (BasicDynaBean c: chargeList) {
			ps.setBigDecimal(1, (BigDecimal) c.get("unit_charge"));
			ps.setBigDecimal(2, (BigDecimal) c.get("discount"));
			ps.setString(3, (String) c.get("service_id"));
			ps.setString(4, (String) c.get("org_id"));
			ps.setString(5, (String) c.get("bed_type"));
			ps.addBatch();
		}

		int results[] = ps.executeBatch();
		boolean status = DataBaseUtil.checkBatchUpdates(results);
		ps.close();
		return status;
	}

	public List<BasicDynaBean> getAllDiscountsForBedTypes(String orgId,
			List<String> bedTypes, List<String> idList) throws SQLException{

		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = getAllDiscountsForBedTypesStmt(con, orgId, bedTypes, idList);
			return DataBaseUtil.queryToDynaListWithCase(ps);

		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private PreparedStatement getAllDiscountsForBedTypesStmt(Connection con,
			String orgId, List<String> bedTypes, List<String> idList) throws SQLException{

		StringBuilder query = new StringBuilder();
		query.append("SELECT service_id, service_name ");
		for (String bedType: bedTypes) {
			query.append(", (SELECT discount FROM service_master_charges WHERE " +
					" service_id=s.service_id AND bed_type=? AND org_id=?) AS " +
					DataBaseUtil.quoteIdent(bedType, true));
		}
		query.append(" FROM services s");

		SearchQueryBuilder.addWhereFieldOpValue(false, query, "service_id", "IN", idList);

		PreparedStatement ps = null;
		ps = con.prepareStatement(query.toString());

		int i=1;
		for (String bedType: bedTypes) {
			ps.setString(i++, bedType);
			ps.setString(i++, orgId);
		}
		if (idList != null) {
			for (String id: idList) {
				ps.setString(i++, id);
			}
		}
		return ps;
	}

	private static final String INSERT_INTO_SERVICES_PLUS="INSERT INTO service_master_charges(service_id,bed_type,org_id,unit_charge,username)" +
  		"(SELECT service_id,bed_type,?,round(unit_charge + ?),? FROM service_master_charges  WHERE org_id = ? ) ";

	private static final String INSERT_INTO_SERVICES_MINUS="INSERT INTO service_master_charges(service_id,bed_type,org_id,unit_charge,username)" +
		"(SELECT service_id,bed_type,?, GREATEST(round(unit_charge - ?), 0),? FROM service_master_charges  WHERE org_id = ? ) ";

	private static final String INSER_INTO_SERVICES_BY = "INSERT INTO service_master_charges(service_id,bed_type,org_id,unit_charge,username)" +
		"(SELECT service_id,bed_type,?,doroundvarying(unit_charge,?,?),? FROM service_master_charges  WHERE org_id = ? ) ";

	private static final String INSER_INTO_SERVICES_WITH_DISCOUNTS_BY = "INSERT INTO service_master_charges(service_id,bed_type,org_id,unit_charge,discount,username)" +
	"(SELECT service_id,bed_type,?,doroundvarying(unit_charge,?,?),doroundvarying(discount,?,?),? FROM service_master_charges  WHERE org_id = ? ) ";

	public static boolean addOrgForServices(Connection con,String newOrgId,String varianceType,
			Double varianceValue,Double varianceBy,boolean useValue,String baseOrgId,
			Double nearstRoundOfValue, String userName,String orgName) throws Exception{
		return addOrgForServices(con,newOrgId,varianceType,
				varianceValue,varianceBy,useValue,baseOrgId,
				nearstRoundOfValue, userName, orgName, false);
	}
	public static boolean addOrgForServices(Connection con,String newOrgId,String varianceType,
			Double varianceValue,Double varianceBy,boolean useValue,String baseOrgId,
			Double nearstRoundOfValue, String userName,String orgName, boolean updateDiscounts) throws Exception{
	   		boolean status = false;
	   		PreparedStatement ps = null;
	   		GenericDAO.alterTrigger(con, "DISABLE", "service_master_charges", "z_services_charges_audit_trigger");
		if(useValue){
			if(varianceType.equals("Incr")){
				ps = con.prepareStatement(INSERT_INTO_SERVICES_PLUS);
				ps.setString(1,newOrgId);
				ps.setDouble(2,varianceValue);
				ps.setString(3, userName);
				ps.setString(4, baseOrgId);

				int i = ps.executeUpdate();
				logger.debug(Integer.toString(i));
				if(i>=0)status = true;

			}else{
				ps = con.prepareStatement(INSERT_INTO_SERVICES_MINUS);
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
/*				ps = con.prepareStatement(INSER_INTO_SERVICES_BY);
				ps.setString(1,newOrgId);
				ps.setBigDecimal(2,new BigDecimal(varianceBy));
				ps.setBigDecimal(3, new BigDecimal(nearstRoundOfValue)) ;
				ps.setString(4, userName);
				ps.setString(5,baseOrgId);

				int i = ps.executeUpdate(); */
			int i = insertChargesByPercent(con, newOrgId, baseOrgId, userName, varianceBy, nearstRoundOfValue, updateDiscounts);
			logger.debug(Integer.toString(i));
			if(i>=0)status = true;

		}
		if (null != ps) ps.close();
		status &= new AuditLogDao("Master","service_master_charges_audit_log").
		logMasterChange(con, userName, "INSERT","org_id", orgName);
	   		return status;
  	}

	private static final String INSERT_SERVICE_CODES_FOR_ORG = "INSERT INTO service_org_details " +
		" SELECT service_id, ?, applicable, item_code, code_type, ?, 'N'" +
  		" FROM service_org_details WHERE org_id = ?";

	public static boolean addOrgCodesForServices(Connection con,String newOrgId,String varianceType,
		Double varianceValue,Double varianceBy,boolean useValue,String baseOrgId,Double nearstRoundOfValue) throws Exception{
  		boolean status = false;
  		PreparedStatement ps = null;
        BasicDynaBean obean = new OrgMasterDao().findByKey(con, "org_id", newOrgId);
        String rateSheetId = ("N".equals((String)obean.get("is_rate_sheet")) ? baseOrgId : null);
		try{
	   		ps = con.prepareStatement(INSERT_SERVICE_CODES_FOR_ORG);
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
		return addOrgCodesForServices(con, newOrgId, null, null, null, false, baseOrgId, null);
	}

    private static int insertChargesByPercent(Connection con, String newOrgId, String baseOrgId, String userName,
            Double varianceBy, Double nearstRoundOfValue, boolean updateDiscounts) throws Exception {

	    int ndx = 1;
	    int numCharges = 1;

	    PreparedStatement pstmt = null;
	    try {
			pstmt = con.prepareStatement(updateDiscounts ?
					INSER_INTO_SERVICES_WITH_DISCOUNTS_BY : INSER_INTO_SERVICES_BY);
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

	// Rate Plan Changes - Begin

    private static String INIT_ORG_DETAILS = "INSERT INTO service_org_details " +
	    "(service_id, org_id, applicable, item_code, code_type, base_rate_sheet_id, is_override)" +
           " SELECT service_id, ?, false, null, null, null, 'N'" +
	   " FROM services";

    private static String INIT_ITEM_ORG_DETAILS = "INSERT INTO service_org_details " +
    "(service_id, org_id, applicable, item_code, code_type, base_rate_sheet_id, is_override)" +
    " ( SELECT ?, od.org_id, false, null, null, prspv.base_rate_sheet_id, 'N' FROM organization_details od " +
    " LEFT OUTER JOIN priority_rate_sheet_parameters_view prspv ON od.org_id = prspv.org_id )";

    private static String INIT_CHARGES = "INSERT INTO service_master_charges(service_id,org_id,bed_type," +
        "unit_charge, username)" +
        "(SELECT service_id, ?, abov.bed_type, 0.0, ? " +
        "FROM services CROSS JOIN all_beds_orgs_view abov WHERE abov.org_id =? ) ";

    private static String INIT_ITEM_CHARGES = "INSERT INTO service_master_charges(service_id,org_id,bed_type," +
    "unit_charge, username)" +
    "(SELECT ?, abov.org_id, abov.bed_type, 0.0, ? FROM all_beds_orgs_view abov) ";

    private static final String INSERT_CHARGES = "INSERT INTO service_master_charges(service_id,org_id,bed_type," +
    " unit_charge, username, is_override)" +
    " SELECT sc.service_id, ?, sc.bed_type, " +
    " doroundvarying(sc.unit_charge,?,?), " +
    " ?, 'N' " +
    " FROM service_master_charges sc, service_org_details sod, service_org_details sodtarget " +
    " where sc.org_id = sod.org_id and sc.service_id = sod.service_id "+
    " and sodtarget.org_id = ? and sodtarget.service_id = sod.service_id and sodtarget.base_rate_sheet_id = ? " +
    " and sod.applicable = true "+
    " and sc.org_id = ?";

    private static final String UPDATE_CHARGES = "UPDATE service_master_charges AS target SET " +
        " unit_charge = doroundvarying(sc.unit_charge,?,?), " +
        " discount = doroundvarying(sc.discount,?,?), " +
        " username = ?, is_override = 'N' " +
        " FROM service_master_charges sc, service_org_details sod " +
        " where sod.org_id = ? and sc.service_id = sod.service_id and sod.base_rate_sheet_id = ? and "+
        " target.service_id = sc.service_id and target.bed_type = sc.bed_type and " +
        " sod.applicable = true and target.is_override != 'Y'"+
        " and sc.org_id = ? and target.org_id = ?";

    private static final String UPDATE_EXCLUSIONS = "UPDATE service_org_details target " +
        " SET item_code = sod.item_code, code_type = sod.code_type," +
        " applicable = true, base_rate_sheet_id = sod.org_id, is_override = 'N' " +
        " FROM service_org_details sod WHERE sod.service_id = target.service_id and " +
        " sod.org_id = ? and sod.applicable = true and target.org_id = ? and target.applicable = false and target.is_override != 'Y'";

    public boolean updateRatePlan(Connection con,String newOrgId, String baseOrgId,
                                         String varianceType, Double variance, Double rndOff,
                                         String userName, String orgName ) throws Exception {
        boolean status = false;

        if(!varianceType.equals("Incr")) {
        	variance = new Double(-variance);
        }

        BigDecimal varianceBy = new BigDecimal(variance);
        BigDecimal roundOff = new BigDecimal(rndOff);

        Object updparams[] = {varianceBy, roundOff, varianceBy, roundOff, userName, newOrgId, baseOrgId, baseOrgId, newOrgId};
        Object insparams[] = {newOrgId, varianceBy, roundOff, userName, newOrgId, baseOrgId, baseOrgId};
        status = updateExclusions(con, UPDATE_EXCLUSIONS, newOrgId, baseOrgId, true);
        if (status) status = updateCharges(con, UPDATE_CHARGES, updparams);
        postAuditEntry(con, "service_master_charges_audit_log", userName, orgName);

        return status;

    }

    public boolean initRatePlan(Connection con, String newOrgId, String varianceType,
			Double varianceBy,String baseOrgId, Double roundOff, String userName, String orgName) throws Exception {
		boolean status = addOrgCodesForItems(con, newOrgId, baseOrgId, userName);
		if (status) status = addOrgForServices(con,newOrgId, varianceType,
				0.0, varianceBy, false, baseOrgId,
				roundOff, userName, orgName, true);
		postAuditEntry(con, "service_master_charges_audit_log", userName, orgName);
		return status;
    }

    private static final String REINIT_EXCLUSIONS = "UPDATE service_org_details as target " +
    " SET applicable = sod.applicable, base_rate_sheet_id = sod.org_id, " +
    " item_code = sod.item_code, code_type = sod.code_type, is_override = 'N' " +
    " FROM service_org_details sod WHERE sod.service_id = target.service_id and " +
    " sod.org_id = ? and target.org_id = ? and target.is_override != 'Y'";

    public boolean reinitRatePlan(Connection con, String newOrgId, String varianceType,
			Double variance,String baseOrgId, Double rndOff,String userName, String orgName) throws Exception {
		boolean status = false;

		if(!varianceType.equals("Incr")) {
			variance = new Double(-variance);
		}

		BigDecimal varianceBy = new BigDecimal(variance);
        BigDecimal roundOff = new BigDecimal(rndOff);

		Object updparams[] = {varianceBy, roundOff, varianceBy, roundOff, newOrgId, baseOrgId};
		status = updateExclusions(con, REINIT_EXCLUSIONS, newOrgId, baseOrgId, true);
		if (status) status = updateCharges(con,UPDATE_RATEPLAN_SERVICE_CHARGES, updparams);

		return status;
    }

    public boolean initItemCharges(Connection con, String serviceId, String userName) throws Exception {

        boolean status = false;
        //  disableAuditTriggers("service_master_charges", "z_services_charges_audit_trigger");
        status = initItemCharges(con, INIT_ITEM_ORG_DETAILS, INIT_ITEM_CHARGES, serviceId, userName);
        // postAuditEntry(con, "service_master_charges_audit_log", userName, orgName);

        return status;
    }

	public boolean initServiceItemCharges(Connection con, String serviceId, String userName) {

		boolean status = false;
		try {
			status = initItemCharges(con, INIT_ITEM_ORG_DETAILS, null, serviceId, userName);
			//scheduling a job for service charge insertion
			serviceChargeScheduleJob(serviceId, userName);
		} catch (Exception exp) {
			logger.error("error while inserting the service charges", exp);
		}

		return status;
	}

	private void serviceChargeScheduleJob(String serviceId, String userName) {
		LinkedHashMap<String, Object> queryParams =
				new LinkedHashMap<String, Object>();
		// Maintain the queryparams as per service_master_charges index
		queryParams.put("service_id", serviceId);
		queryParams.put("org_id", "abov.org_id");
		queryParams.put("bed_type", "abov.bed_type");
		queryParams.put("unit_charge", BigDecimal.ZERO);
		queryParams.put("username", userName);
		CronJobService cronJobService = ApplicationContextProvider.getBean(CronJobService.class);
		cronJobService.masterChargeScheduleJob(queryParams, "service_master_charges", "SERVICE");
	}
    

    // Rate Plan Changes - End

	private static final String UPDATE_SERVICE_CHARGES_PLUS = "UPDATE service_master_charges totab SET " +
		" unit_charge = round(fromtab.unit_charge + ?)" +
		" FROM service_master_charges fromtab" +
		" WHERE totab.org_id = ? AND fromtab.org_id = ?" +
		" AND totab.service_id = fromtab.service_id AND totab.bed_type = fromtab.bed_type AND totab.is_override='N'";

	private static final String UPDATE_SERVICE_CHARGES_MINUS = "UPDATE service_master_charges totab SET " +
		" unit_charge = GREATEST(round(fromtab.unit_charge - ?), 0)" +
		" FROM service_master_charges fromtab" +
		" WHERE totab.org_id = ? AND fromtab.org_id = ?" +
		" AND totab.service_id = fromtab.service_id AND totab.bed_type = fromtab.bed_type AND totab.is_override='N'";

	private static final String UPDATE_SERVICE_CHARGES_BY = "UPDATE service_master_charges totab SET " +
		" unit_charge = doroundvarying(fromtab.unit_charge,?,?)" +
		" FROM service_master_charges fromtab" +
		" WHERE totab.org_id = ? AND fromtab.org_id = ?" +
		" AND totab.service_id = fromtab.service_id AND totab.bed_type = fromtab.bed_type AND totab.is_override='N'";

	public static boolean updateOrgForServices(Connection con,String orgId,String varianceType,
		Double varianceValue,Double varianceBy,boolean useValue,String baseOrgId,
		Double nearstRoundOfValue,String userName,String orgName ) throws SQLException, IOException {

			boolean status = false;
			PreparedStatement pstmt = null;
			GenericDAO.alterTrigger("DISABLE", "service_master_charges", "z_services_charges_audit_trigger");

			if (useValue) {

			if (varianceType.equals("Incr"))
				pstmt = con.prepareStatement(UPDATE_SERVICE_CHARGES_PLUS);
			else
				pstmt = con.prepareStatement(UPDATE_SERVICE_CHARGES_MINUS);

			pstmt.setBigDecimal(1, new BigDecimal(varianceValue));
			pstmt.setString(2, orgId);
			pstmt.setString(3, baseOrgId);

			int i = pstmt.executeUpdate();
			if (i>=0) status = true;

			} else {

			pstmt = con.prepareStatement(UPDATE_SERVICE_CHARGES_BY);
			if (!varianceType.equals("Incr"))
			varianceBy = new Double(-varianceBy);

			pstmt.setBigDecimal(1, new BigDecimal(varianceBy));
			pstmt.setBigDecimal(2, new BigDecimal(nearstRoundOfValue));
			pstmt.setString(3, orgId);
			pstmt.setString(4, baseOrgId);

			int i = pstmt.executeUpdate();
			if (i>=0) status = true;

			}
			status &= new AuditLogDao("Master","service_master_charges_audit_log").
			logMasterChange(con, userName, "UPDATE","org_id", orgName);
			pstmt.close();

		return status;
	}

	private static final String GET_ALL_CHARGES_FOR_ORG =
		" select service_id,bed_type,unit_charge, discount from service_master_charges where org_id=? " ;

	public List<BasicDynaBean> getAllChargesForOrg(String orgId, String serviceId) throws SQLException {

		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_ALL_CHARGES_FOR_ORG + "and service_id=? ");
			ps.setString(1, orgId);
			ps.setString(2, serviceId);
			return DataBaseUtil.queryToDynaList(ps);

		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public boolean updateOrgForDerivedRatePlans(Connection con,String[] ratePlanIds, String[] applicable,
			String serviceId) throws Exception{
		return updateOrgForDerivedRatePlans(con,ratePlanIds, applicable, "service_org_details", "services",
				"service_id", serviceId);
	}

	public  boolean updateChargesForDerivedRatePlans(Connection con,String baseRateSheetId, String[] ratePlanIds,
	    		String[] bedType, Double[] regularcharges,String serviceId, Double[] discounts,String[] applicable)throws Exception {

		return updateChargesForDerivedRatePlans(con,baseRateSheetId,ratePlanIds,bedType,
				regularcharges, "service_master_charges","service_org_details","services",
				"service_id",serviceId, discounts,applicable);
	}

	private static final String GET_DERIVED_RATE_PALN_DETAILS = "select rp.org_id,od.org_name, "+
		" case when rate_variation_percent<0 then 'Decrease By' else 'Increase By' end as discormarkup, "+
		" rate_variation_percent,round_off_amount,sod.applicable,sod.service_id,rp.base_rate_sheet_id, sod.is_override "+
		" from rate_plan_parameters rp "+
		" join organization_details od on(od.org_id=rp.org_id) "+
		" join service_org_details sod on (sod.org_id = rp.org_id) "+
		" where rp.base_rate_sheet_id =?  and service_id=? and sod.base_rate_sheet_id=?";

	public List<BasicDynaBean> getDerivedRatePlanDetails(String baseRateSheetId,String serviceId)throws SQLException {
		return getDerivedRatePlanDetails(baseRateSheetId, "services", serviceId,GET_DERIVED_RATE_PALN_DETAILS);
	}

	private static final String UPDATE_RATEPLAN_SERVICE_CHARGES = "UPDATE service_master_charges totab SET " +
		" unit_charge = doroundvarying(fromtab.unit_charge,?,?),discount = doroundvarying(fromtab.discount,?,?) " +
		" FROM service_master_charges fromtab" +
		" WHERE totab.org_id = ? AND fromtab.org_id = ?" +
		" AND totab.service_id = fromtab.service_id AND totab.bed_type = fromtab.bed_type AND totab.is_override='N'";

	public boolean updateServiceChargesForDerivedRatePlans(String orgId,String varianceType,
		Double varianceValue,Double varianceBy,String baseOrgId,
		Double nearstRoundOfValue,String userName, String orgName, boolean upload) throws SQLException, Exception {

			boolean success = false;
			Connection con = null;
			GenericDAO.alterTrigger("DISABLE", "service_master_charges", "z_services_charges_audit_trigger");
			try {
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

				success &= new AuditLogDao("Master","service_master_charges_audit_log").
				logMasterChange(con, userName, "UPDATE","org_id", orgName);
			}finally{
				DataBaseUtil.commitClose(con, success);
			}

		return success;
	}

	public static boolean updateChargesBasedOnNewRateSheet(Connection con, String orgId,Double varianceBy,
			String baseOrgId,Double nearstRoundOfValue, String serviceId)throws SQLException,Exception {
		boolean success = false;
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(UPDATE_RATEPLAN_SERVICE_CHARGES+ " AND totab.service_id = ? ");
			ps.setBigDecimal(1, new BigDecimal(varianceBy));
			ps.setBigDecimal(2, new BigDecimal(nearstRoundOfValue));
			ps.setBigDecimal(3, new BigDecimal(varianceBy));
			ps.setBigDecimal(4, new BigDecimal(nearstRoundOfValue));
			ps.setString(5, orgId);
			ps.setString(6, baseOrgId);
			ps.setString(7, serviceId);

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
		status = updateCharges(con,UPDATE_RATEPLAN_SERVICE_CHARGES + " AND totab.bed_type=? ", updparams);
		return status;
	}

}
