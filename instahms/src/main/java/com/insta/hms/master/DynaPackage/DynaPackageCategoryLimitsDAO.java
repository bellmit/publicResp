/**
 *
 */
package com.insta.hms.master.DynaPackage;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.HsSfWorkbookUtils;
import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;
import com.insta.hms.auditlog.AuditLogDao;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.master.rateplan.ItemChargeDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lakshmi
 *
 */
public class DynaPackageCategoryLimitsDAO extends ItemChargeDAO {
	static Logger  logger = LoggerFactory.getLogger(DynaPackageCategoryLimitsDAO.class);
	private static final GenericDAO ratePlanParameterDAO = new GenericDAO("rate_plan_parameters");

	public DynaPackageCategoryLimitsDAO() {
		super("dyna_package_category_limits");
	}

	/*
	 * Copy the GENERAL rate plan charges to all other rate plans. Assumes that the other
	 * rate-plan charges are non-existent, ie, no updates only inserts new charges.
	 */
	private static final String COPY_GENERAL_CHARGES_TO_ALL_ORGS =
		"INSERT INTO dyna_package_category_limits (org_id, bed_type, dyna_package_id, dyna_pkg_cat_id, username," +
		" pkg_included, amount_limit, qty_limit) " +
		" SELECT abo.org_id, abo.bed_type, dcl.dyna_package_id, dcl.dyna_pkg_cat_id, dcl.username, " +
		" dcl.pkg_included, dcl.amount_limit, dcl.qty_limit " +
		" FROM all_beds_orgs_view abo " +
		" JOIN dyna_package_category_limits dcl ON (dcl.dyna_package_id=? AND dcl.bed_type = abo.bed_type " +
		" AND dcl.org_id = 'ORG0001') " +
		" WHERE abo.org_id != 'ORG0001' ";

	public void copyGeneralChargesToAllOrgs(Connection con, int dynaPackageId) throws SQLException {
		PreparedStatement ps = con.prepareStatement(COPY_GENERAL_CHARGES_TO_ALL_ORGS);
		ps.setInt(1, dynaPackageId);
		ps.executeUpdate();
		ps.close();
	}

	/*
	 * Copy the GENERAL bed type charges to all inactive bed types for the given Package ID.
	 * These charges will not be inserted normally when the package is created, since
	 * only active bed charges are input from the user.
	 */
	private static final String COPY_GENERAL_CHARGES_TO_INACTIVE_BEDS =
		" INSERT INTO dyna_package_category_limits (org_id, bed_type, dyna_package_id, dyna_pkg_cat_id, pkg_included, amount_limit, qty_limit) " +
		" SELECT abo.org_id, abo.bed_type, dcl.dyna_package_id, dcl.dyna_pkg_cat_id, dcl.pkg_included, dcl.amount_limit, dcl.qty_limit " +
		" FROM all_beds_orgs_view abo " +
		"   JOIN dyna_package_category_limits dcl ON (dcl.org_id = abo.org_id AND dcl.bed_type = 'GENERAL') " +
		" WHERE abo.bed_type IN ( " +
		"     SELECT DISTINCT intensive_bed_type FROM icu_bed_charges WHERE bed_status = 'I' " +
		"     UNION ALL SELECT DISTINCT bed_type FROM bed_details WHERE bed_status = 'I') " +
		"   AND dcl.dyna_package_id=? " ;

	public void copyGeneralChargesToInactiveBeds(Connection con, int dynaPackageId) throws SQLException {
		PreparedStatement ps = con.prepareStatement(COPY_GENERAL_CHARGES_TO_INACTIVE_BEDS);
		ps.setInt(1, dynaPackageId);
		ps.executeUpdate();
		ps.close();
	}

	private static final String GET_ALL_CHARGES_FOR_ORG =
		" select dyna_package_id, bed_type, dyna_pkg_cat_id, pkg_included, amount_limit, qty_limit" +
		" from dyna_package_category_limits where org_id=? " ;

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

	public List<BasicDynaBean> getAllChargesForOrg(String orgId, String dynaPackageId) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_ALL_CHARGES_FOR_ORG + "and dyna_package_id=? ");
			ps.setString(1, orgId);
			ps.setInt(2, Integer.parseInt(dynaPackageId));
			return DataBaseUtil.queryToDynaList(ps);

		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GROUP_INCR_CHARGES =
		" UPDATE dyna_package_category_limits " +
		" SET amount_limit=GREATEST( round((amount_limit+?)/?,0)*?, 0), username = ? " +
		" WHERE org_id=? ";

	private static final String GROUP_INCR_CHARGES_PERCENTAGE =
		" UPDATE dyna_package_category_limits " +
		" SET amount_limit=GREATEST( round(amount_limit*(100+?)/100/?,0)*?, 0), username = ?" +
		" WHERE org_id=? ";

	private static final String AUDIT_LOG_HINT = ":GUP";

	public void groupIncreaseCharges(Connection con, String orgId, List<String> bedTypes,
			List<String> ids, BigDecimal amount, boolean isPercentage,
			BigDecimal roundTo, String updateTable, String userName)
		throws SQLException {

		StringBuilder query = null;
		String userNameWithHint = ((null == userName) ? "" : userName) + AUDIT_LOG_HINT;

		if(updateTable !=null && updateTable.equals("UPDATECHARGE")) {
			query = new StringBuilder(
				isPercentage ? GROUP_INCR_CHARGES_PERCENTAGE
						: GROUP_INCR_CHARGES);
		}
		
		//SearchQueryBuilder.addWhereFieldOpValue(true, query, "bed_type", "IN", bedTypes);
		String[] placeHolderArr = new String[bedTypes.size()];
    Arrays.fill(placeHolderArr, "?");
    String placeHolders = StringUtils.arrayToCommaDelimitedString(placeHolderArr);
    query.append("AND bed_type in ( " + placeHolders  + ")");
    
		//SearchQueryBuilder.addWhereFieldOpValue(true, query, "dyna_package_id::text", "IN", ids);
    String[] placeHolderArr2 = new String[ids.size()];
    Arrays.fill(placeHolderArr2, "?");
    String placeHolders2 = StringUtils.arrayToCommaDelimitedString(placeHolderArr2);
    query.append("AND dyna_package_id::text in ( " + placeHolders2  + ")");

    //	PreparedStatement ps = con.prepareStatement(query.toString());

		// sanity: round to zero is not allowed, can cause div/0
		if (roundTo.equals(BigDecimal.ZERO))
			roundTo = BigDecimal.ONE;

		/*int i=1;
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
		if (ps != null) ps.close();*/
		
		List<Object> values = new ArrayList<Object>();
		values.add(amount);
		values.add(roundTo);
		values.add(roundTo);   // roundTo appears twice in the query
		values.add(userNameWithHint);
		values.add(orgId);

    if (bedTypes != null){
      values.addAll(bedTypes);
    }
    if (ids != null){
      values.addAll(ids);
    }
    
    DataBaseUtil.executeQuery(con, query.toString(), values.toArray());
		
	}

	public PreparedStatement getAllChargesForBedTypesStmt(Connection con, String orgId,
			List<String> bedTypes, List<String> ids) throws SQLException {

		StringBuilder query = new StringBuilder();
			query.append("SELECT s.dyna_package_id, s.dyna_package_name");
		for (String bedType: bedTypes) {
			query.append(", (SELECT charge FROM dyna_package_category_limits WHERE " +
					" dyna_package_id=s.dyna_package_id AND bed_type=? AND org_id=?) AS " +
					DataBaseUtil.quoteIdent(bedType, true));
		}
			query.append(" FROM dyna_packages s" +
					 " JOIN dyna_package_org_details sod ON (s.dyna_package_id=sod.dyna_package_id AND sod.org_id=? )");
		if(ids == null)
			 query.append("WHERE  s.status='A' ");
			
		SearchQueryBuilder.addWhereFieldOpValue(false, query, "s.dyna_package_id::text", "IN", ids);

		PreparedStatement ps = null;
		ps = con.prepareStatement(query.toString());

		int i=1;
		for (String bedType: bedTypes) {
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

	public void fillWorkSheetValues(String orgId, XSSFSheet packageSheet)throws SQLException {

		List<String> columnsNames = new ArrayList<String>();
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			List<String> allBedTypes = new BedMasterDAO().getUnionOfBedTypes();
			ps = getAllChargesForBedTypesStmt(con, orgId, allBedTypes, null);
			ResultSet rs = ps.executeQuery();
			ResultSetMetaData rsMetaData = rs.getMetaData();
			int colCount = rsMetaData.getColumnCount();
			for (int i=1; i <= colCount; i++) {
				columnsNames.add(rsMetaData.getColumnName(i));
			}
			Map<String, List> columnNamesMap = new HashMap<String, List>();
			columnNamesMap.put("mainItems", columnsNames);
			HsSfWorkbookUtils.createPhysicalCellsWithValues(DataBaseUtil.queryToDynaList(ps), columnNamesMap, packageSheet, false);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String BACK_UP_CHARGES = "INSERT INTO dyna_package_category_limits_backup"+
		" (SELECT ?, current_timestamp, dyna_package_id, dyna_pkg_cat_id, bed_type, org_id, pkg_included, amount_limit, qty_limit " +
		" FROM dyna_package_category_limits WHERE org_id = ?)";

	public static void backupCharges(String uID, String orgID)throws SQLException {
		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			pstmt = con.prepareStatement(BACK_UP_CHARGES);
			pstmt.setString(1, uID);
			pstmt.setString(2, orgID);
			pstmt.execute();
		} finally {
			DataBaseUtil.closeConnections(con, pstmt);
		}
	}

	private static final String INSERT_INTO_DYNA_PLUS="INSERT INTO dyna_package_category_limits(dyna_package_id, " +
		" dyna_pkg_cat_id, bed_type, org_id, pkg_included, amount_limit, qty_limit,username)" +
		"(SELECT dyna_package_id,dyna_pkg_cat_id,bed_type,?,pkg_included,round(amount_limit + ?),qty_limit,? " +
		" FROM dyna_package_category_limits  WHERE org_id = ? ) ";

	private static final String INSERT_INTO_DYNA_MINUS="INSERT INTO dyna_package_category_limits(dyna_package_id," +
	" dyna_pkg_cat_id, bed_type, org_id, pkg_included, amount_limit, qty_limit,username)" +
		"(SELECT dyna_package_id,dyna_pkg_cat_id,bed_type,?,pkg_included,GREATEST(round(amount_limit - ?), 0), qty_limit,? " +
		" FROM dyna_package_category_limits  WHERE org_id = ? ) ";

	private static final String INSER_INTO_DYNA_BY = "INSERT INTO dyna_package_category_limits(dyna_package_id," +
		" dyna_pkg_cat_id, bed_type, org_id, pkg_included, amount_limit, qty_limit,username)" +
		"(SELECT dyna_package_id,dyna_pkg_cat_id,bed_type,?,pkg_included,doroundvarying(amount_limit,?,?), qty_limit,? " +
		" FROM dyna_package_category_limits  WHERE org_id = ? ) ";


	public static boolean addOrgForDynaPackages(Connection con,String newOrgId,String varianceType,
			Double varianceValue,Double varianceBy,boolean useValue,String baseOrgId,
			Double nearstRoundOfValue, String userName,String orgName) throws Exception{
	  		boolean status = false;
	  		PreparedStatement ps = null;
	  		GenericDAO.alterTrigger("DISABLE", "dyna_package_category_limits", "z_dyna_package_category_limits_audit_trigger");

			if(useValue){
				if(varianceType.equals("Incr")){
					ps = con.prepareStatement(INSERT_INTO_DYNA_PLUS);
					ps.setString(1,newOrgId);
					ps.setDouble(2,varianceValue);
					ps.setString(3, userName);
					ps.setString(4, baseOrgId);

					int i = ps.executeUpdate();
					logger.debug(Integer.toString(i));
					if(i>=0)status = true;

				}else{
					ps = con.prepareStatement(INSERT_INTO_DYNA_MINUS);
					ps.setString(1,newOrgId);
					ps.setDouble(2,varianceValue);
					ps.setString(3, userName);
					ps.setString(4,baseOrgId);

					int i = ps.executeUpdate();
					logger.debug(Integer.toString(i));
					if(i>=0)status = true;
				}
			}else{
					ps = con.prepareStatement(INSER_INTO_DYNA_BY);
					ps.setString(1,newOrgId);
					if(!varianceType.equals("Incr")){
					 	varianceBy = new Double(-varianceBy);
					}
					ps.setBigDecimal(2,new BigDecimal(varianceBy));
					ps.setBigDecimal(3, new BigDecimal(nearstRoundOfValue)) ;
					ps.setString(4, userName);
					ps.setString(5,baseOrgId);

					int i = ps.executeUpdate();
					logger.debug(Integer.toString(i));
					if(i>=0)status = true;

			}
			ps.close();
			status &= new AuditLogDao("Master","dyna_package_category_limits_audit_log").
			logMasterChange(con, userName, "INSERT","org_id", orgName);
	  		return status;
	}

    public boolean initRatePlan(Connection con, String newOrgId, String varianceType,
			Double varianceBy,String baseOrgId, Double roundOff, String userName, String orgName) throws Exception {
		boolean status = addOrgForDynaPackages(con,newOrgId, varianceType,
				0.0, varianceBy, false, baseOrgId,
				roundOff, userName, orgName);
		postAuditEntry(con, "dyna_package_category_limits_audit_log", userName, orgName);
		return status;
    }

	private static final String REINIT_CHARGES = "UPDATE dyna_package_category_limits AS target SET " +
    " amount_limit = doroundvarying(dpcl.amount_limit,?,?), " +
    " qty_limit = dpcl.qty_limit, pkg_included = dpcl.pkg_included, dyna_pkg_cat_id = dpcl.dyna_pkg_cat_id, " +
    " is_override = 'N' " +
    " FROM dyna_package_category_limits dpcl " +
    " where target.dyna_package_id = dpcl.dyna_package_id and target.bed_type = dpcl.bed_type and target.dyna_pkg_cat_id = dpcl.dyna_pkg_cat_id and " +
    " target.org_id = ? and dpcl.org_id = ? and target.is_override != 'Y'";

	public boolean reinitRatePlan(Connection con, String newOrgId, String varianceType,
			Double variance,String baseOrgId, Double rndOff,String userName, String orgName) throws Exception {
		boolean status = false;
		if(!varianceType.equals("Incr")) {
			variance = new Double(-variance);
		}
		BigDecimal varianceBy = new BigDecimal(variance);
        BigDecimal roundOff = new BigDecimal(rndOff);

		Object updparams[] = {varianceBy, roundOff, newOrgId, baseOrgId};
		status = updateCharges(con,REINIT_CHARGES, updparams);
		return status;
    }

	private static final String UPDATE_CHARGES = "UPDATE dyna_package_category_limits AS target SET " +
    " amount_limit = doroundvarying(dpcl.amount_limit,?,?), " +
    " qty_limit = dpcl.qty_limit, pkg_included = dpcl.pkg_included, dyna_pkg_cat_id = dpcl.dyna_pkg_cat_id, " +
    " is_override = 'N' " +
    " FROM dyna_package_category_limits dpcl, dyna_package_org_details dpod  " +
    " where dpod.org_id = ? and dpcl.dyna_package_id = dpod.dyna_package_id and dpod.base_rate_sheet_id = ? and " +
    " target.dyna_package_id = dpcl.dyna_package_id and target.bed_type = dpcl.bed_type and target.dyna_pkg_cat_id = dpcl.dyna_pkg_cat_id and " +
    " dpod.applicable = true and target.is_override != 'Y'"+
    " and dpcl.org_id = ? and target.org_id = ?";

	public boolean updateRatePlan(Connection con,String newOrgId, String baseOrgId,
	        String varianceType, Double variance, Double rndOff,
	        String userName, String orgName ) throws Exception {

	    boolean status = false;
	    if(!varianceType.equals("Incr")) {
	        variance = new Double(-variance);
	    }

	    BigDecimal varianceBy = new BigDecimal(variance);
	    BigDecimal roundOff = new BigDecimal(rndOff);

	    Object updparams[] = {varianceBy, roundOff, newOrgId, baseOrgId, baseOrgId, newOrgId};
	    // No update exclusion as that would already be done as part of dyna_package charges.
	    status = updateCharges(con, UPDATE_CHARGES, updparams);

	    return status;
	}

	private static final String UPDATE_DYNAPKG_CHARGES_PLUS = "UPDATE dyna_package_category_limits totab SET " +
		" amount_limit = round(fromtab.amount_limit + ?) " +
		" FROM dyna_package_category_limits fromtab" +
		" WHERE totab.org_id = ? AND fromtab.org_id = ?" +
		" AND totab.dyna_package_id = fromtab.dyna_package_id AND totab.bed_type = fromtab.bed_type";

	private static final String UPDATE_DYNAPKG_CHARGES_MINUS = "UPDATE dyna_package_category_limits totab SET " +
		" amount_limit = GREATEST(round(fromtab.amount_limit - ?), 0) " +
		" FROM dyna_package_category_limits fromtab" +
		" WHERE totab.org_id = ? AND fromtab.org_id = ?" +
		" AND totab.dyna_package_id = fromtab.dyna_package_id AND totab.bed_type = fromtab.bed_type";

	private static final String UPDATE_DYNAPKG_CHARGES_BY = "UPDATE dyna_package_category_limits totab SET " +
		" amount_limit = doroundvarying(fromtab.amount_limit,?,?) " +
		" FROM dyna_package_category_limits fromtab" +
		" WHERE totab.org_id = ? AND fromtab.org_id = ?" +
		" AND totab.dyna_package_id = fromtab.dyna_package_id AND totab.bed_type = fromtab.bed_type";

	public static boolean updateOrgForDynapkgs(Connection con,String orgId,String varianceType,
		Double varianceValue,Double varianceBy,boolean useValue,String baseOrgId,
		Double nearstRoundOfValue,String userName,String orgName ) throws SQLException, IOException {

			boolean status = false;
			PreparedStatement pstmt = null;
			GenericDAO.alterTrigger("DISABLE", "dyna_package_category_limits", "z_dyna_package_category_limits_audit_trigger");

			if (useValue) {

			if (varianceType.equals("Incr"))
				pstmt = con.prepareStatement(UPDATE_DYNAPKG_CHARGES_PLUS);
			else
				pstmt = con.prepareStatement(UPDATE_DYNAPKG_CHARGES_MINUS);

			pstmt.setBigDecimal(1, new BigDecimal(varianceValue));
			pstmt.setString(2, orgId);
			pstmt.setString(3, baseOrgId);

			int i = pstmt.executeUpdate();
			if (i>=0) status = true;

			} else {

			pstmt = con.prepareStatement(UPDATE_DYNAPKG_CHARGES_BY);
			if (!varianceType.equals("Incr"))
			varianceBy = new Double(-varianceBy);

			pstmt.setBigDecimal(1, new BigDecimal(varianceBy));
			pstmt.setBigDecimal(2, new BigDecimal(nearstRoundOfValue));
			pstmt.setString(3, orgId);
			pstmt.setString(4, baseOrgId);

			int i = pstmt.executeUpdate();
			if (i>=0) status = true;

			}
			status &= new AuditLogDao("Master","dyna_package_category_limits_audit_log").
			logMasterChange(con, userName, "UPDATE","org_id", orgName);
			pstmt.close();

		return status;
	}

	private static final String ADD_CATEGORY_TO_DYNA="INSERT INTO dyna_package_category_limits(dyna_package_id, " +
		" dyna_pkg_cat_id, bed_type, org_id, pkg_included, amount_limit, qty_limit)" +
		"(SELECT dyna_package_id,?,bed_type,org_id,'Y',0,0 " +
		" FROM dyna_package_category_limits  WHERE dyna_pkg_cat_id = ? ) ";

	public boolean addCategoryForDynaPackages(Connection con,
			int useCategoryId, int newCategoryId, String userName) throws Exception{
  		boolean status = false;
  		PreparedStatement ps = null;

  		try {
  			GenericDAO.alterTrigger(con, "DISABLE", "dyna_package_category_limits", "z_dyna_package_category_limits_audit_trigger");

	  		if (useCategoryId != 0 && newCategoryId != 0) {

	  			ps = con.prepareStatement(ADD_CATEGORY_TO_DYNA);
				ps.setInt(1, newCategoryId);
				ps.setInt(2, useCategoryId);

				int i = ps.executeUpdate();
				if (i > 0 ) status = true;

				ps.close();
				status &= new AuditLogDao("Master","dyna_package_category_limits_audit_log").
				logMasterChange(con, userName, "INSERT","dyna_pkg_cat_id", newCategoryId+"");
	  		}
  		}finally {
			GenericDAO.alterTrigger(con, "ENABLE", "dyna_package_category_limits",
					"z_dyna_package_category_limits_audit_trigger");
		}

  		return status;
	}

	private static final String INSERT_CATEGORY_INTO_DYNA="INSERT INTO dyna_package_category_limits(dyna_package_id, " +
		" dyna_pkg_cat_id, bed_type, org_id, pkg_included, amount_limit, qty_limit)" +
		"(SELECT dyna_package_id,?,bed_type,org_id,'Y',0,0 " +
		" FROM dyna_package_charges) ";

	public boolean insertCategoryForDynaPackages(Connection con,
				int newCategoryId, String userName) throws Exception{
		boolean status = false;
		PreparedStatement ps = null;

		try {
			GenericDAO.alterTrigger("DISABLE", "dyna_package_category_limits", "z_dyna_package_category_limits_audit_trigger");

			if (newCategoryId != 0) {

				ps = con.prepareStatement(INSERT_CATEGORY_INTO_DYNA);
				ps.setInt(1, newCategoryId);

				int i = ps.executeUpdate();
				if (i > 0 ) status = true;

				ps.close();
				status &= new AuditLogDao("Master","dyna_package_category_limits_audit_log").
				logMasterChange(con, userName, "INSERT","dyna_pkg_cat_id", newCategoryId+"");
			}
		}finally {
			GenericDAO.alterTrigger("ENABLE", "dyna_package_category_limits",
					"z_dyna_package_category_limits_audit_trigger");
		}

		return status;
	}
	private static final String GET_CATEGORY_LIMITS =
		" SELECT * FROM dyna_package_category_limits WHERE dyna_pkg_cat_id = ? ";

	@SuppressWarnings("unchecked")
	public boolean hasCategoryLimits(BasicDynaBean useCategoryBean) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			if (useCategoryBean != null && useCategoryBean.get("dyna_pkg_cat_id") != null) {
				int useCategoryId = (Integer)useCategoryBean.get("dyna_pkg_cat_id");

				con = DataBaseUtil.getReadOnlyConnection();
				ps = con.prepareStatement(GET_CATEGORY_LIMITS);
				ps.setInt(1, useCategoryId);
				List<BasicDynaBean> categoryLimits = DataBaseUtil.queryToDynaList(ps);
				if (categoryLimits != null && categoryLimits.size() > 0)
					return true;
			}
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return false;
	}

	private static final String FIX_MISSING_LIMITS =
		"INSERT INTO dyna_package_category_limits (dyna_package_id, org_id, bed_type, dyna_pkg_cat_id, "+
		"   pkg_included, amount_limit, qty_limit) "+
		" SELECT m.dyna_package_id, m.org_id, m.bed_type, m.dyna_pkg_cat_id, 'Y', 0, 0 "+
		" FROM missing_dyna_package_limits_view m "+
		" WHERE (SELECT count(*) FROM dyna_package_category_limits) != "+
		"   (SELECT count(*) FROM all_beds_orgs_view) * (SELECT count(*) FROM dyna_packages)  "+
		"   * (SELECT count(*) FROM dyna_package_category_limits) ";

	public static int fixMissingLimits(Connection con) throws SQLException {
		GenericDAO.alterTrigger("DISABLE", "dyna_package_category_limits", "z_dyna_package_category_limits_audit_trigger");
		int retVal = DataBaseUtil.executeQuery(con, FIX_MISSING_LIMITS, null);
		GenericDAO.alterTrigger("ENABLE", "dyna_package_category_limits", "z_dyna_package_category_limits_audit_trigger");
		return retVal;
	}


	private static final String UPDATE_RATEPLAN_DYNAPKG_CHARGES_BY = "UPDATE dyna_package_category_limits totab SET " +
	" amount_limit = doroundvarying(fromtab.amount_limit,?,?), qty_limit = fromtab.qty_limit, " +
	" pkg_included = fromtab.pkg_included "+
	" FROM dyna_package_category_limits fromtab" +
	" WHERE totab.org_id = ? AND fromtab.org_id = ?" +
	" AND totab.dyna_package_id = fromtab.dyna_package_id AND totab.bed_type = fromtab.bed_type "+
	" AND totab.dyna_pkg_cat_id = fromtab.dyna_pkg_cat_id AND totab.dyna_package_id=? AND totab.dyna_pkg_cat_id=? "+
	" AND totab.is_override='N' ";

	public boolean updateCategoryLimitsForDerivedRatePlans(String[] rateplanIds,String orgId,
			String dynaPackageId, String[] categoryIds)throws SQLException,Exception {
		boolean success = false;
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			ps = con.prepareStatement(UPDATE_RATEPLAN_DYNAPKG_CHARGES_BY);
			Map<String,Object> keys = new HashMap<String, Object>();

			for(int i=0; i<rateplanIds.length; i++) {
				keys.put("base_rate_sheet_id", orgId);
				keys.put("org_id", rateplanIds[i]);
				BasicDynaBean bean = ratePlanParameterDAO.findByKey(con, keys);
				Double varianceBy = new Double((Integer)bean.get("rate_variation_percent"));
				Double nearstRoundOfValue =  new Double((Integer)bean.get("round_off_amount"));

				for(int k = 0; k<categoryIds.length; k++) {
					ps.setBigDecimal(1, new BigDecimal(varianceBy));
					ps.setBigDecimal(2, new BigDecimal(nearstRoundOfValue));
					ps.setString(3, rateplanIds[i]);
					ps.setString(4, orgId);
					ps.setInt(5, Integer.parseInt(dynaPackageId));
					ps.setInt(6, Integer.parseInt(categoryIds[k]));

					int j = ps.executeUpdate();
					if (j>=0)
						success = true;
				}
			}
			ps.close();

		}finally{
			DataBaseUtil.commitClose(con, success);
		}
		return success;
	}


	private static final String UPDATE_RATEPALN_DYNAPKG_CHARGES_BY = "UPDATE dyna_package_category_limits totab SET " +
		" amount_limit = doroundvarying(fromtab.amount_limit,?,?), " +
		" qty_limit = fromtab.qty_limit, "+
		" pkg_included = fromtab.pkg_included "+
		" FROM dyna_package_category_limits fromtab" +
		" WHERE totab.org_id = ? AND fromtab.org_id = ?" +
		" AND totab.dyna_package_id = fromtab.dyna_package_id AND totab.dyna_pkg_cat_id = fromtab.dyna_pkg_cat_id " +
		" AND totab.bed_type = fromtab.bed_type AND totab.is_override='N' ";

	public boolean updateLimitsForDerivedRatePlans(String orgId,String varianceType,
			Double varianceValue,Double varianceBy,String baseOrgId,
			Double nearstRoundOfValue,String userName,String orgName, boolean upload) throws SQLException, Exception 	 {
		Connection con = null;
		PreparedStatement ps = null;
		boolean success = false;
		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			if(upload){
				List<BasicDynaBean> rateSheetList =  getRateSheetsByPriority(con, orgId, ratePlanParameterDAO);
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
				Object updparams[] = {variance, roundoff,  orgId, baseOrgId, baseOrgId, orgId};
				success = updateCharges(con, UPDATE_CHARGES, updparams);
			}

			success &= new AuditLogDao("Master","dyna_package_category_limits_audit_log").
			logMasterChange(con, userName, "UPDATE","org_id", orgName);

		}finally{
			if(ps != null) ps.close();
			DataBaseUtil.commitClose(con, success);
		}

		return success;
	}

	public static boolean updateChargesBasedOnNewRateSheet(Connection con, String orgId,Double varianceBy,
			String baseOrgId,Double nearstRoundOfValue, String dynaPackageId)throws SQLException,Exception {
		boolean success = false;
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(UPDATE_RATEPALN_DYNAPKG_CHARGES_BY+ " AND totab.dyna_package_id = ? ");
			ps.setBigDecimal(1, new BigDecimal(varianceBy));
			ps.setBigDecimal(2, new BigDecimal(nearstRoundOfValue));
			ps.setString(3, orgId);
			ps.setString(4, baseOrgId);
			ps.setInt(5, Integer.parseInt(dynaPackageId));

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

		Object updparams[] = {varianceBy, roundOff, ratePlanId, rateSheetId,bedType};
		status = updateCharges(con,UPDATE_DYNAPKG_CHARGES_BY + " AND totab.bed_type=? ", updparams);
		return status;
	}

}
