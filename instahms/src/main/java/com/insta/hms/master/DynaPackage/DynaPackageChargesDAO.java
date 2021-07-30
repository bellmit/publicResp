package com.insta.hms.master.DynaPackage;

import com.bob.hms.adminmasters.organization.OrgMasterDao;
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

public class DynaPackageChargesDAO extends ItemChargeDAO {
	static Logger  logger = LoggerFactory.getLogger(DynaPackageChargesDAO.class);
	private static final GenericDAO ratePlanParameterDAO = new GenericDAO("rate_plan_parameters");

	public DynaPackageChargesDAO() {
		super("dyna_package_charges");
	}

	/*
	 * Copy the GENERAL rate plan charges to all other rate plans. Assumes that the other
	 * rate-plan charges are non-existent, ie, no updates only inserts new charges.
	 */
	private static final String COPY_GENERAL_CHARGES_TO_ALL_ORGS =
		" INSERT INTO dyna_package_charges (org_id, bed_type, dyna_package_id, charge, username) " +
		" 	SELECT abo.org_id, abo.bed_type, dpc.dyna_package_id, dpc.charge, dpc.username " +
		" FROM all_beds_orgs_view abo " +
		" 	JOIN dyna_package_charges dpc ON (dpc.dyna_package_id=? AND dpc.bed_type = abo.bed_type " +
		" AND dpc.org_id = 'ORG0001') " +
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
		" INSERT INTO dyna_package_charges (org_id, bed_type, dyna_package_id, charge) " +
		" SELECT abo.org_id, abo.bed_type, dpc.dyna_package_id, dpc.charge " +
		" FROM all_beds_orgs_view abo " +
		"   JOIN dyna_package_charges dpc ON (dpc.org_id = abo.org_id AND dpc.bed_type = 'GENERAL') " +
		" WHERE abo.bed_type IN ( " +
		"     SELECT DISTINCT intensive_bed_type FROM icu_bed_charges WHERE bed_status = 'I' " +
		"     UNION ALL SELECT DISTINCT bed_type FROM bed_details WHERE bed_status = 'I') " +
		"   AND dpc.dyna_package_id=? " ;

	public void copyGeneralChargesToInactiveBeds(Connection con, int dynaPackageId) throws SQLException {
		PreparedStatement ps = con.prepareStatement(COPY_GENERAL_CHARGES_TO_INACTIVE_BEDS);
		ps.setInt(1, dynaPackageId);
		ps.executeUpdate();
		ps.close();
	}

	private static final String GET_ALL_PACKAGE_CHARGES_FOR_ORG =
		" SELECT dyna_package_id,bed_type,charge FROM dyna_package_charges WHERE org_id=? " ;

	public List<BasicDynaBean> getAllPackageChargesForOrganisation(String orgId) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_ALL_PACKAGE_CHARGES_FOR_ORG);
			ps.setString(1, orgId);
			return DataBaseUtil.queryToDynaList(ps);

		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_PACKAGE_CATEGORY_CHARGES_FOR_ORG =
		" SELECT dcl.dyna_package_id, dcl.dyna_pkg_cat_id::text as dyna_pkg_cat_id, dyna_pkg_cat_name," +
		" dpc.bed_type,charge,pkg_included,amount_limit,qty_limit, limit_type " +
		" 	FROM dyna_package_category_limits dcl " +
		" JOIN dyna_package_category dcat ON (dcat.dyna_pkg_cat_id = dcl.dyna_pkg_cat_id) " +
		" JOIN dyna_package_charges dpc ON (dpc.org_id = dcl.org_id AND dpc.bed_type = dcl.bed_type)  " +
		" WHERE dcl.org_id=? and dcl.dyna_package_id=? and dpc.dyna_package_id=? order by dyna_pkg_cat_name " ;

	private static final String GET_PACKAGE_CHARGES_FOR_ORG =
		" SELECT dpc.dyna_package_id, '0' as dyna_pkg_cat_id, '' as dyna_pkg_cat_name," +
		" dpc.bed_type,charge,'Y', 0, 0, '' " +
		" 	FROM dyna_package_charges dpc " +
		" WHERE dpc.org_id=? and dpc.dyna_package_id=? order by dyna_pkg_cat_name " ;

	@SuppressWarnings("unchecked")
	public List<BasicDynaBean> getAllChargesForOrg(String orgId,
				String dynaPackageId, List<BasicDynaBean> categories) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();

			if (categories != null && categories.size() > 0) {
				ps = con.prepareStatement(GET_PACKAGE_CATEGORY_CHARGES_FOR_ORG);
				ps.setString(1, orgId);
				ps.setInt(2, Integer.parseInt(dynaPackageId));
				ps.setInt(3, Integer.parseInt(dynaPackageId));
			}else {
				ps = con.prepareStatement(GET_PACKAGE_CHARGES_FOR_ORG);
				ps.setString(1, orgId);
				ps.setInt(2, Integer.parseInt(dynaPackageId));
			}

			return DataBaseUtil.queryToDynaList(ps);

		} finally {
			DataBaseUtil.closeConnections(con, ps);
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

	private static final String GROUP_INCR_CHARGES =
		" UPDATE dyna_package_charges " +
		" SET charge=GREATEST( round((charge+?)/?,0)*?, 0), username = ? " +
		" WHERE org_id=? ";

	private static final String GROUP_INCR_CHARGES_PERCENTAGE =
		" UPDATE dyna_package_charges " +
		" SET charge=GREATEST( round(charge*(100+?)/100/?,0)*?, 0), username = ?" +
		" WHERE org_id=? ";

	private static final String AUDIT_LOG_HINT = ":GUP";

	public void groupIncreaseChargesWithRoundOff(Connection con, String orgId, List<String> bedTypes,
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

		//PreparedStatement ps = con.prepareStatement(query.toString());

		// sanity: round to zero is not allowed, can cause div/0
		if (roundTo.equals(BigDecimal.ZERO))
			roundTo = BigDecimal.ONE;

		/*int i=1;
    ps.setBigDecimal(i++, amount);
    ps.setBigDecimal(i++, roundTo);
    ps.setBigDecimal(i++, roundTo);   // roundTo appears twice in the query
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

	private static final String GROUP_INCR_CHARGES_NO_ROUNDOFF =
		" UPDATE dyna_package_charges " +
		" SET charge = GREATEST( charge + ?, 0), username = ? " +
		" WHERE org_id=? ";

	private static final String GROUP_INCR_CHARGES_PERCENTAGE_NO_ROUNDOFF =
		" UPDATE dyna_package_charges " +
		" SET charge = GREATEST(charge +(charge * ? / 100 ) , 0), username = ?" +
		" WHERE org_id=? ";

	public void groupIncreaseChargesNoRoundOff(Connection con, String orgId, List<String> bedTypes,
			List<String> ids, BigDecimal amount, boolean isPercentage, String updateTable, String userName)
		throws SQLException {

		StringBuilder query = null;
		String userNameWithHint = ((null == userName) ? "" : userName) + AUDIT_LOG_HINT;

		if(updateTable !=null && updateTable.equals("UPDATECHARGE")) {
			query = new StringBuilder(
				isPercentage ? GROUP_INCR_CHARGES_PERCENTAGE_NO_ROUNDOFF
						: GROUP_INCR_CHARGES_NO_ROUNDOFF);
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

		/*PreparedStatement ps = con.prepareStatement(query.toString());

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
		if (ps != null) ps.close();*/
    
    List<Object> values = new ArrayList<Object>();
    values.add(amount);
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
			query.append(", (SELECT charge FROM dyna_package_charges WHERE " +
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

	private static final String BACK_UP_CHARGES = "INSERT INTO dyna_package_charges_backup"+
		" (SELECT ?, current_timestamp, dyna_package_id, bed_type, org_id, charge " +
		" FROM dyna_package_charges WHERE org_id = ?) ";

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

	private static final String INSERT_INTO_DYNA_PLUS="INSERT INTO dyna_package_charges(dyna_package_id,bed_type,org_id,charge,username)" +
		"(SELECT dyna_package_id,bed_type,?,round(charge + ?),? " +
		" FROM dyna_package_charges  WHERE org_id = ? ) ";

	private static final String INSERT_INTO_DYNA_MINUS="INSERT INTO dyna_package_charges(dyna_package_id,bed_type,org_id,charge,username)" +
			"(SELECT dyna_package_id,bed_type,?, GREATEST(round(charge - ?), 0),? " +
			"FROM dyna_package_charges  WHERE org_id = ? ) ";

	private static final String INSER_INTO_DYNA_BY = "INSERT INTO dyna_package_charges(dyna_package_id,bed_type,org_id,charge,username)" +
			"(SELECT dyna_package_id,bed_type,?,doroundvarying(charge,?,?),? FROM dyna_package_charges  WHERE org_id = ? ) ";


	public static boolean addOrgForDynaPackages(Connection con,String newOrgId,String varianceType,
			Double varianceValue,Double varianceBy,boolean useValue,String baseOrgId,
			Double nearstRoundOfValue, String userName,String orgName) throws Exception{
	  		boolean status = false;
	  		PreparedStatement ps = null;
	  		GenericDAO.alterTrigger(con, "DISABLE", "dyna_package_charges", "z_dyna_package_charges_audit_trigger");

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
					ps.setString(4, baseOrgId);

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
					ps.setString(5, baseOrgId);

					int i = ps.executeUpdate();
					logger.debug(Integer.toString(i));
					if(i>=0)status = true;

			}
			ps.close();
			status &= new AuditLogDao("Master","dyna_package_charges_audit_log").
			logMasterChange(con, userName, "INSERT","org_id", orgName);
	  		return status;
	}

	private static final String INSERT_DYNA_CODES_FOR_ORG = "INSERT INTO dyna_package_org_details " +
		" SELECT dyna_package_id, ?, applicable, item_code, code_type, ?, 'N'" +
 		" FROM dyna_package_org_details WHERE org_id = ?";

	public static boolean addOrgCodesForDynaPackages(Connection con,String newOrgId,String varianceType,
		Double varianceValue,Double varianceBy,boolean useValue,String baseOrgId,Double nearstRoundOfValue) throws Exception{
 		boolean status = false;
 		PreparedStatement ps = null;
        BasicDynaBean obean = new OrgMasterDao().findByKey(con, "org_id", newOrgId);
        String rateSheetId = ("N".equals((String)obean.get("is_rate_sheet")) ? baseOrgId : null);
		try{
	   		ps = con.prepareStatement(INSERT_DYNA_CODES_FOR_ORG);
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
		return addOrgCodesForDynaPackages(con, newOrgId, null, null, null, false, baseOrgId, null);
	}

    private static String INIT_ORG_DETAILS = "INSERT INTO dyna_package_org_details " +
    "(dyna_package_id, org_id, applicable, item_code, code_type, base_rate_sheet_id, is_override)" +
    " SELECT dyna_package_id, ?, false, null, null, null, 'N'" +
    " FROM dyna_packages";

	private static String INIT_CHARGES = "INSERT INTO dyna_package_charges(dyna_package_id,org_id,bed_type," +
	   	"charge, username)" +
	    "(SELECT dyna_package_id, ?, abov.bed_type, 0.0, ? " +
	    "FROM dyna_packages CROSS JOIN all_beds_orgs_view abov WHERE abov.org_id =? ) ";

	private static final String INSERT_CHARGES = "INSERT INTO dyna_package_charges(dyna_package_id,org_id,bed_type," +
	" charge, username, is_override)" +
    " SELECT dpc.dyna_package_id, ?, dpc.bed_type, " +
    " doroundvarying(dpc.charge,?,?), " +
    " ?, 'N' " +
    " FROM dyna_package_charges dpc, dyna_package_org_details dpod, dyna_package_org_details dpodtarget  " +
    " where dpc.org_id = dpod.org_id and dpc.dyna_package_id = dpod.dyna_package_id " +
    " and dpodtarget.org_id = ? and dpodtarget.dyna_package_id = dpod.dyna_package_id and dpodtarget.base_rate_sheet_id = ? " +
    " and dpod.applicable = true "+
    " and dpc.org_id = ? ";

	private static final String UPDATE_CHARGES = "UPDATE dyna_package_charges AS target SET " +
	    " charge = doroundvarying(dpc.charge,?,?), " +
	    " is_override = 'N' " +
	    " FROM dyna_package_charges dpc, dyna_package_org_details dpod  " +
	    " where dpod.org_id = ? and dpc.dyna_package_id = dpod.dyna_package_id and dpod.base_rate_sheet_id = ? and " +
	    " target.dyna_package_id = dpc.dyna_package_id and target.bed_type = dpc.bed_type and " +
	    " dpod.applicable = true and target.is_override != 'Y'"+
	    " and dpc.org_id = ? and target.org_id = ?";

	private static final String UPDATE_EXCLUSIONS = "UPDATE dyna_package_org_details AS target " +
	    " SET item_code = dpod.item_code, code_type = dpod.code_type," +
	    " applicable = true, base_rate_sheet_id = dpod.org_id, is_override = 'N' " +
	    " FROM dyna_package_org_details dpod WHERE dpod.dyna_package_id = target.dyna_package_id and " +
	    " dpod.org_id = ? and dpod.applicable = true and target.org_id = ? and target.applicable = false and target.is_override != 'Y'";

	public boolean updateRatePlan(Connection con,String newOrgId, String baseOrgId,
            String varianceType, Double variance, Double rndOff,
            String userName, String orgName ) throws Exception {

        boolean status = false;
        // No audit logs for package master as of now.

        if(!varianceType.equals("Incr")) {
            variance = new Double(-variance);
        }

        BigDecimal varianceBy = new BigDecimal(variance);
        BigDecimal roundOff = new BigDecimal(rndOff);

        Object updparams[] = {varianceBy, roundOff, newOrgId, baseOrgId, baseOrgId, newOrgId};
        Object insparams[] = {newOrgId, varianceBy, roundOff, userName, newOrgId, baseOrgId, baseOrgId};
        status = updateExclusions(con, UPDATE_EXCLUSIONS, newOrgId, baseOrgId, true);
        if (status) status = updateCharges(con, UPDATE_CHARGES, updparams);

        return status;
	}

    public boolean initRatePlan(Connection con, String newOrgId, String varianceType,
			Double varianceBy,String baseOrgId, Double roundOff, String userName, String orgName) throws Exception {
		boolean status = addOrgCodesForItems(con, newOrgId, baseOrgId, userName);
		if (status) status = addOrgForDynaPackages(con,newOrgId, varianceType,
				0.0, varianceBy, false, baseOrgId,
				roundOff, userName, orgName);
		postAuditEntry(con, "dyna_package_charges_audit_log", userName, orgName);
		return status;
    }
    private static final String REINIT_EXCLUSIONS = "UPDATE dyna_package_org_details as target " +
    " SET applicable = dpod.applicable, base_rate_sheet_id = dpod.org_id, " +
    " item_code = dpod.item_code, code_type = dpod.code_type, is_override = 'N' " +
    " FROM dyna_package_org_details dpod WHERE dpod.dyna_package_id = target.dyna_package_id and " +
    " dpod.org_id = ? and target.org_id = ? and target.is_override != 'Y'";

    public boolean reinitRatePlan(Connection con, String newOrgId, String varianceType,
			Double variance,String baseOrgId, Double rndOff,String userName, String orgName) throws Exception {
		boolean status = false;
		if(!varianceType.equals("Incr")) {
			variance = new Double(-variance);
		}
		BigDecimal varianceBy = new BigDecimal(variance);
        BigDecimal roundOff = new BigDecimal(rndOff);

		Object updparams[] = {varianceBy, roundOff, newOrgId, baseOrgId};
		status = updateExclusions(con, REINIT_EXCLUSIONS, newOrgId, baseOrgId, true);
		if (status) status = updateCharges(con,UPDATE_RATEPLAN_DYNAPKG_CHARGES_BY, updparams);
		return status;
    }

	private static final String UPDATE_DYNAPKG_CHARGES_PLUS = "UPDATE dyna_package_charges totab SET " +
		" charge = round(fromtab.charge + ?) " +
		" FROM dyna_package_charges fromtab" +
		" WHERE totab.org_id = ? AND fromtab.org_id = ?" +
		" AND totab.dyna_package_id = fromtab.dyna_package_id AND totab.bed_type = fromtab.bed_type AND totab.is_override='N'";

	private static final String UPDATE_DYNAPKG_CHARGES_MINUS = "UPDATE dyna_package_charges totab SET " +
		" charge = GREATEST(round(fromtab.charge - ?), 0) " +
		" FROM dyna_package_charges fromtab" +
		" WHERE totab.org_id = ? AND fromtab.org_id = ?" +
		" AND totab.dyna_package_id = fromtab.dyna_package_id AND totab.bed_type = fromtab.bed_type AND totab.is_override='N'";

	private static final String UPDATE_DYNAPKG_CHARGES_BY = "UPDATE dyna_package_charges totab SET " +
		" charge = doroundvarying(fromtab.charge,?,?) " +
		" FROM dyna_package_charges fromtab" +
		" WHERE totab.org_id = ? AND fromtab.org_id = ?" +
		" AND totab.dyna_package_id = fromtab.dyna_package_id AND totab.bed_type = fromtab.bed_type AND totab.is_override='N'";

	public static boolean updateOrgForDynapkgs(Connection con,String orgId,String varianceType,
		Double varianceValue,Double varianceBy,boolean useValue,String baseOrgId,
		Double nearstRoundOfValue, String userName,String orgName ) throws SQLException, IOException {

		boolean status = false;
		PreparedStatement pstmt = null;
		GenericDAO.alterTrigger("DISABLE", "dyna_package_charges", "z_dyna_package_charges_audit_trigger");

		if (useValue) {

			if (varianceType.equals("Incr"))
				pstmt = con.prepareStatement(UPDATE_DYNAPKG_CHARGES_PLUS);
			else
				pstmt = con.prepareStatement(UPDATE_DYNAPKG_CHARGES_MINUS);

			int c=1;
			pstmt.setBigDecimal(c++, new BigDecimal(varianceValue));
			pstmt.setString(c++, orgId);
			pstmt.setString(c++, baseOrgId);

			int i = pstmt.executeUpdate();
			if (i>=0) status = true;

		} else {

			pstmt = con.prepareStatement(UPDATE_DYNAPKG_CHARGES_BY);
			if (!varianceType.equals("Incr"))
			varianceBy = new Double(-varianceBy);

			int c = 1;
			pstmt.setBigDecimal(c++, new BigDecimal(varianceBy));
			pstmt.setBigDecimal(c++, new BigDecimal(nearstRoundOfValue));
			pstmt.setString(c++, orgId);
			pstmt.setString(c++, baseOrgId);

			int i = pstmt.executeUpdate();
			if (i>=0)
				status = true;

		}
		status &= new AuditLogDao("Master","dyna_package_charges_audit_log").
			logMasterChange(con, userName, "UPDATE","org_id", orgName);
		pstmt.close();

		return status;
	}

	private static final String GET_PACKAGE_CHARGES =
		" SELECT * FROM dyna_package_charges WHERE dyna_package_id = ? ";

	@SuppressWarnings("unchecked")
	public boolean hasPackageCharges(BasicDynaBean usePackageBean) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			if (usePackageBean != null && usePackageBean.get("dyna_package_id") != null) {
				int usePackageId = (Integer)usePackageBean.get("dyna_package_id");

				con = DataBaseUtil.getReadOnlyConnection();
				ps = con.prepareStatement(GET_PACKAGE_CHARGES);
				ps.setInt(1, usePackageId);
				List<BasicDynaBean> packageCharges = DataBaseUtil.queryToDynaList(ps);
				if (packageCharges != null && packageCharges.size() > 0)
					return true;
			}
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return false;
	}

	private static final String GET_DERIVED_RATE_PALN_DETAILS = "select rp.org_id,od.org_name, "+
		" case when rate_variation_percent<0 then 'Decrease By' else 'Increase By' end as discormarkup, "+
		" rate_variation_percent,round_off_amount,dod.applicable,dod.dyna_package_id,rp.base_rate_sheet_id, dod.is_override "+
		" from rate_plan_parameters rp "+
		" join organization_details od on(od.org_id=rp.org_id) "+
		" join dyna_package_org_details dod on (dod.org_id = rp.org_id) "+
		" where rp.base_rate_sheet_id =?  and dyna_package_id=? and dod.base_rate_sheet_id = ? ";

	public List<BasicDynaBean> getDerivedRatePlanDetails(String baseRateSheetId,String dynaPackId)throws SQLException {
		return getDerivedRatePlanDetails(baseRateSheetId, "dynapackages", dynaPackId,GET_DERIVED_RATE_PALN_DETAILS);
	}

	public boolean updateOrgForDerivedRatePlans(String[] ratePlanIds,String[] applicable,String dynaPackId)
		throws SQLException, Exception {
		boolean success= false;
		Connection con = null;
		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			success = updateOrgForDerivedRatePlans(con,ratePlanIds,applicable,"dyna_package_org_details","dynapackages",
					"dyna_package_id",dynaPackId);
		}finally{
			DataBaseUtil.commitClose(con, success);
		}

		return success;
	}

	public boolean updatedynaPackChargesForDerivedRatePlans(String[] rateplanIds,String orgId,
			String dynaPackageId, String[] applicable)throws SQLException,Exception {
		boolean success = false;
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			ps = con.prepareStatement(UPDATE_DYNAPKG_CHARGES_BY + " AND totab.dyna_package_id = ? ");
			for(int i=0; i<rateplanIds.length; i++) {

				Map<String,Object> keys = new HashMap<String, Object>();
				keys.put("base_rate_sheet_id", orgId);
				keys.put("org_id", rateplanIds[i]);
				BasicDynaBean bean = ratePlanParameterDAO.findByKey(con, keys);
				Double varianceBy = new Double((Integer)bean.get("rate_variation_percent"));
				Double nearstRoundOfValue =  new Double((Integer)bean.get("round_off_amount"));

				ps.setBigDecimal(1, new BigDecimal(varianceBy));
				ps.setBigDecimal(2, new BigDecimal(nearstRoundOfValue));
				ps.setString(3, rateplanIds[i]);
				ps.setString(4, orgId);
				ps.setInt(5, Integer.parseInt(dynaPackageId));

				int j = ps.executeUpdate();
				if (j>=0)
					success = true;

				boolean overrideItemCharges = checkItemStatus(con, rateplanIds[i], "dyna_package_org_details",
						"dynapackages", "dyna_package_id",dynaPackageId, applicable[i]);
	    		if(overrideItemCharges)
	    			OverrideItemCharges(con, "dyna_package_charges", "dynapackages", rateplanIds[i], "dyna_package_id", dynaPackageId);
			}
			ps.close();

		}finally{
			DataBaseUtil.commitClose(con, success);
		}
		return success;
	}


	private static final String UPDATE_RATEPLAN_DYNAPKG_CHARGES_BY = "UPDATE dyna_package_charges totab SET " +
		" charge = doroundvarying(fromtab.charge,?,?) " +
		" FROM dyna_package_charges fromtab" +
		" WHERE totab.org_id = ? AND fromtab.org_id = ?" +
		" AND totab.dyna_package_id = fromtab.dyna_package_id " +
		" AND totab.bed_type = fromtab.bed_type AND totab.is_override='N'";

	public boolean updateDynaPackChargesForDerivedRatePlans(String orgId,String varianceType,
			Double varianceValue,Double varianceBy,String baseOrgId,
			Double nearstRoundOfValue,String userName,String orgName, boolean upload) throws SQLException, Exception {

		boolean success = false;
		Connection con = null;
		PreparedStatement ps = null;
		GenericDAO.alterTrigger("DISABLE", "dyna_package_charges", "z_dyna_package_charges_audit_trigger");
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			if(upload) {
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
				Object updparams[] = {variance, roundoff, orgId, baseOrgId, baseOrgId, orgId};
				success = updateCharges(con, UPDATE_CHARGES, updparams);
			}

			success &= new AuditLogDao("Master","dyna_package_charges_audit_log").
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
			ps = con.prepareStatement(UPDATE_RATEPLAN_DYNAPKG_CHARGES_BY + " AND totab.dyna_package_id = ? ");
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
		status = updateCharges(con,UPDATE_RATEPLAN_DYNAPKG_CHARGES_BY + " AND totab.bed_type=? ", updparams);
		return status;
	}

}
