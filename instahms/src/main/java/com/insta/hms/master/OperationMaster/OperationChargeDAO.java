package com.insta.hms.master.OperationMaster;

import com.bob.hms.adminmasters.organization.OrgMasterDao;
import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.auditlog.AuditLogDao;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.jobs.CronJobService;
import com.insta.hms.master.rateplan.ItemChargeDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OperationChargeDAO extends ItemChargeDAO {

	static Logger logger = LoggerFactory.getLogger(OperationChargeDAO.class);
    private static final GenericDAO ratePlanParameterDao = new GenericDAO("rate_plan_parameters");

	public OperationChargeDAO() {
		super("operation_charges");
	}

	/*
	 * Returns charges of all charge types for the given orgId, and list of operation IDs,
	 * for all bed types
	 *
	 * This is used for displaying the charges for each bed in the main list master screen
	 */

	private static final String GET_ALL_CHARGES_FOR_ORG =
		" SELECT op_id, bed_type, surg_asstance_charge, surgeon_charge, anesthetist_charge,surg_asst_discount,surg_discount,anest_discount " +
		" FROM operation_charges " +
		" WHERE org_id=?";

	public List<BasicDynaBean> getAllChargesForOrg(String orgId, List<String> ids) throws SQLException {

		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			StringBuilder query = new StringBuilder(GET_ALL_CHARGES_FOR_ORG);
			SearchQueryBuilder.addWhereFieldOpValue(true, query, "op_id", "IN", ids);

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

	/*
	 * Returns charges of all charge types for the given orgId, and a single operation ID,
	 * for all bed types
	 */
	public List<BasicDynaBean> getAllChargesForOrgOperation(String orgId, String id) throws SQLException {
		List<String> ids = new ArrayList();
		ids.add(id);
		return getAllChargesForOrg(orgId, ids);
	}


	public void getAllChargesForBedTypesXLS(String orgId, List<String> bedTypes, HSSFSheet opSheet)
		throws SQLException, IOException {

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<String> columnNames = new ArrayList<String>();

		/*
		 * Need to construct a query like this:
		 *   SELECT ood.operation_id as op_id, operation_name,
		 *     oc1.surg_asstance_charge AS "GENERAL/surg_asstance_charge",
		 *       oc1.surgeon_charge AS "GENERAL/surgeon_charge",
		 *       oc1.anesthetist_charge AS "GENERAL/anesthetist_charge",
		 *     oc2.surg_asstance_charge AS "PRIVATE/surg_asstance_charge",
		 *       oc2.surgeon_charge AS "PRIVATE/surgeon_charge",
		 *       oc2.anesthetist_charge AS "PRIVATE/anesthetist_charge",
		 *       ...
		 *   FROM operation_org_details ood
		 *     JOIN operation_charges oc1 ON
		 *     	  (oc1.op_id = ood.operation_id AND oc1.org_id = ood.org_id AND oc1.bed_type=?)
		 *     JOIN operation_charges oc2 ON
		 *     	  (oc2.op_id = ood.operation_id AND oc2.org_id = ood.org_id AND oc2.bed_type=?)
		 *     ...
		 *     JOIN operation_master om ON (om.op_id = ood.operation_id)
		 *   WHERE ood.org_id = ?
		 */

		StringBuilder query = new StringBuilder();
		String[] chargeTypes = {"surg_asstance_charge", "surg_asst_discount", "surgeon_charge", "surg_discount", "anesthetist_charge", "anest_discount"};

		query.append("SELECT ood.operation_id as op_id, operation_name, ood.applicable as rateplan_applicable, ood. item_code ");
		int i = 1;
		for (String bedType: bedTypes) {
			for (String chargeType: chargeTypes) {
				query.append(", oc"+i+".").append(chargeType);
				query.append(" AS ").append(DataBaseUtil.quoteIdent(bedType+"/"+chargeType, true));
			}
			i++;
		}

		query.append(" FROM operation_org_details ood ");

		i = 1;
		for (String bedType: bedTypes) {
			query.append(" JOIN operation_charges oc"+i).append(" ON ");
			query.append("  oc"+i+".op_id=ood.operation_id AND ");
			query.append("  oc"+i+".org_id=ood.org_id AND ");
			query.append("  oc"+i+".bed_type=?");
			i++;
		}

		query.append(" JOIN operation_master om ON (om.op_id = ood.operation_id) ");
		query.append(" WHERE ood.org_id=? AND om.status='A' ");

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(query.toString());

			i=1;
			for (String bedType: bedTypes) {
				ps.setString(i++, bedType);
			}
			ps.setString(i++, orgId);


			rs = ps.executeQuery();
			ResultSetMetaData metaData = rs.getMetaData();
			int colCount = metaData.getColumnCount();

			for (int c=1; c<=colCount; c++) {
				columnNames.add(metaData.getColumnName(c));
			}

		//	HSSFWorkbookUtils.createPhysicalCellsWithValues(DataBaseUtil.queryToDynaList(ps), columnNames, opSheet);

		} finally {
			DataBaseUtil.closeConnections(con, ps, rs);
		}
	}

	/*
	 * Copy the GENERAL rate plan charges to all other rate plans. Assumes that the other
	 * rate-plan charges are non-existent, ie, no updates only inserts new charges.
	 */
	private static final String COPY_GENERAL_CHARGES_TO_ALL_ORGS =
		" INSERT INTO operation_charges " +
		"   (org_id, bed_type, op_id, surg_asstance_charge, surgeon_charge, anesthetist_charge, " +
		"	surg_asst_discount,surg_discount,anest_discount) " +
		" SELECT abo.org_id, abo.bed_type, oc.op_id, oc.surg_asstance_charge, oc.surgeon_charge, " +
		"    oc.anesthetist_charge, oc.surg_asst_discount, oc.surg_discount, oc.anest_discount " +
		" FROM all_beds_orgs_view abo " +
		"  JOIN operation_charges oc ON (oc.op_id=? AND oc.bed_type=abo.bed_type AND oc.org_id='ORG0001') " +
		" WHERE abo.org_id != 'ORG0001'";

	public void copyGeneralChargesToAllOrgs(Connection con, String id) throws SQLException {
		PreparedStatement ps = con.prepareStatement(COPY_GENERAL_CHARGES_TO_ALL_ORGS);
		ps.setString(1, id);
		ps.executeUpdate();
		ps.close();
	}

	/*
	 * Copy the GENERAL bed type charges to all inactive bed types for the given Operation ID.
	 * These charges will not be inserted normally when the services is created, since
	 * only active bed charges are input from the user.
	 */
	private static final String COPY_GENERAL_CHARGES_TO_INACTIVE_BEDS =
		" INSERT INTO operation_charges " +
		"   (org_id, bed_type, op_id, surg_asstance_charge, surgeon_charge, anesthetist_charge," +
		"	surg_asst_discount,surg_discount,anest_discount) " +
		" SELECT abo.org_id, abo.bed_type, oc.op_id, oc.surg_asstance_charge, oc.surgeon_charge, " +
		"    oc.anesthetist_charge, oc.surg_asst_discount, oc.surg_discount, oc.anest_discount " +
		" FROM all_beds_orgs_view abo " +
		"   JOIN operation_charges oc ON (oc.org_id = abo.org_id AND oc.bed_type = 'GENERAL') " +
		" WHERE abo.bed_type IN ( " +
		"     SELECT DISTINCT intensive_bed_type FROM icu_bed_charges WHERE bed_status = 'I' " +
		"     UNION ALL SELECT DISTINCT bed_type FROM bed_details WHERE bed_status = 'I') " +
		"   AND oc.op_id=? " ;

	public void copyGeneralChargesToInactiveBeds(Connection con, String id) throws SQLException {
		PreparedStatement ps = con.prepareStatement(COPY_GENERAL_CHARGES_TO_INACTIVE_BEDS);
		ps.setString(1, id);
		ps.executeUpdate();
		ps.close();
	}

	public void groupIncreaseCharges(Connection con, String orgId, String chargeType,
			List<String> bedTypes, List<String> ids, BigDecimal amount, boolean isPercentage,
			BigDecimal roundTo, String updateTable, String userName)
		throws SQLException {

		if (roundTo.compareTo(BigDecimal.ZERO) == 0) {
			groupIncreaseChargesNoRoundOff(con, orgId, chargeType, bedTypes, ids,
					amount, isPercentage, updateTable, userName);
		}else {
			groupIncreaseChargesWithRoundOff(con, orgId, chargeType, bedTypes, ids,
					amount, isPercentage, roundTo, updateTable, userName);
		}
	}

	/*
	 * Group increase charges: takes in:
	 *  - orgId to update (reqd)
	 *  - list of bed types to update (optional, if not given, all bed types)
	 *  - list of IDs to update (optional, if not given, all operations)
	 *  - amount to increase by (can be negative for a decrease)
	 *  - whether the amount is a percentage instead of an abs. amount
	 *  - an amount to be rounded to (nearest). Rounding to 0 is invalid.
	 *
	 * The new amount will not be allowed to go less than zero.
	 */
	private static final String GROUP_INCR_CHARGES =
		" UPDATE operation_charges SET #=GREATEST( round((#+?)/?,0)*?, 0), username = ? " +
		" WHERE org_id=? ";

	private static final String GROUP_INCR_CHARGES_PERCENTAGE =
		" UPDATE operation_charges SET #=GREATEST( round(#*(100+?)/100/?,0)*?, 0), username = ? " +
		" WHERE org_id=? ";

	private static final String GROUP_INCR_DISCOUNTS =
		" UPDATE operation_charges SET #=LEAST(GREATEST( round((#+?)/?,0)*?, 0), @), username = ? " +
		" WHERE org_id=? ";

	private static final String GROUP_INCR_DISCOUNTS_PERCENTAGE =
		" UPDATE operation_charges SET #=LEAST(GREATEST( round(#*(100+?)/100/?,0)*?, 0), @), username = ? " +
		" WHERE org_id=? ";

	private static final String GROUP_APPLY_DISCOUNTS =
		" UPDATE operation_charges SET @ = LEAST(GREATEST( round(( # + ?)/?,0)*?, 0), #), username = ? " +
		" WHERE org_id=? ";

	private static final String GROUP_APPLY_DISCOUNTS_PERCENTAGE =
		" UPDATE operation_charges SET @ = LEAST(GREATEST( round(#+(# * ?/100/?),0)*?, 0), #), username = ? " +
		" WHERE org_id=? ";

	private static final String AUDIT_LOG_HINT = ":GUP"; // : is important, use all upper case to be consistent

	public void groupIncreaseChargesWithRoundOff(Connection con, String orgId, String chargeType,
			List<String> bedTypes, List<String> ids, BigDecimal amount, boolean isPercentage,
			BigDecimal roundTo, String updateTable, String userName)
		throws SQLException {

		String userNameWithHint = ((null == userName) ? "" : userName) + AUDIT_LOG_HINT;
		StringBuilder query = null;
		if(updateTable !=null && updateTable.equals("UPDATECHARGE")) {
			query = new StringBuilder(
				isPercentage ? GROUP_INCR_CHARGES_PERCENTAGE.replaceAll("#",DataBaseUtil.quoteIdent(chargeType)) :
					GROUP_INCR_CHARGES.replaceAll("#",DataBaseUtil.quoteIdent(chargeType)) );

		}else if(updateTable.equals("UPDATEDISCOUNT")) {

			String updateDisAmount = GROUP_INCR_DISCOUNTS.replaceAll("@",DataBaseUtil.quoteIdent(chargeType));
			String updateDisPercent = GROUP_INCR_DISCOUNTS_PERCENTAGE.replaceAll("@",DataBaseUtil.quoteIdent(chargeType));

			if(chargeType.equals("surg_asstance_charge")) {
				chargeType = "surg_asst_discount";
			}else if(chargeType.equals("surgeon_charge")) {
				chargeType = "surg_discount";
			}else if(chargeType.equals("anesthetist_charge")) {
				chargeType = "anest_discount";
			}

			query = new StringBuilder(
				isPercentage ? updateDisPercent.replaceAll("#",DataBaseUtil.quoteIdent(chargeType)) :
					updateDisAmount.replaceAll("#",DataBaseUtil.quoteIdent(chargeType)) );

		}else {
			String chargeTypeDiscount = null;
			String updateDiscOnCharge = null;
			if(chargeType.equals("surg_asstance_charge")) {
				chargeTypeDiscount = "surg_asst_discount";
			}else if(chargeType.equals("surgeon_charge")) {
				chargeTypeDiscount = "surg_discount";
			}else if(chargeType.equals("anesthetist_charge")) {
				chargeTypeDiscount = "anest_discount";
			}

			if(isPercentage) {
				updateDiscOnCharge = GROUP_APPLY_DISCOUNTS_PERCENTAGE.replaceAll("@",DataBaseUtil.quoteIdent(chargeTypeDiscount));
				updateDiscOnCharge = updateDiscOnCharge.replaceAll("#",DataBaseUtil.quoteIdent(chargeType));
			}else {
				updateDiscOnCharge = GROUP_APPLY_DISCOUNTS.replaceAll("@",DataBaseUtil.quoteIdent(chargeTypeDiscount));
				updateDiscOnCharge = updateDiscOnCharge.replaceAll("#",DataBaseUtil.quoteIdent(chargeType));
			}
			query = new StringBuilder(updateDiscOnCharge);
		}

		SearchQueryBuilder.addWhereFieldOpValue(true, query, "bed_type", "IN", bedTypes);
		SearchQueryBuilder.addWhereFieldOpValue(true, query, "op_id", "IN", ids);

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
		" UPDATE operation_charges SET # = GREATEST( # + ?, 0), username = ? " +
		" WHERE org_id=? ";

	private static final String GROUP_INCR_CHARGES_PERCENTAGE_NO_ROUNDOFF =
		" UPDATE operation_charges SET # = GREATEST(# +( # * ? / 100 ) , 0), username = ? " +
		" WHERE org_id=? ";

	private static final String GROUP_INCR_DISCOUNTS_NO_ROUNDOFF =
		" UPDATE operation_charges SET # = LEAST(GREATEST( # + ?, 0), @), username = ? " +
		" WHERE org_id=? ";

	private static final String GROUP_INCR_DISCOUNTS_PERCENTAGE_NO_ROUNDOFF =
		" UPDATE operation_charges SET # = LEAST(GREATEST(# +( # * ? / 100 ) , 0), @), username = ? " +
		" WHERE org_id=? ";

	private static final String GROUP_APPLY_DISCOUNTS_NO_ROUNDOFF =
		" UPDATE operation_charges SET @ = LEAST(GREATEST( # + ?, 0), #) , username = ? " +
		" WHERE org_id=? ";

	private static final String GROUP_APPLY_DISCOUNTS_PERCENTAGE_NO_ROUNDOFF =
		" UPDATE operation_charges SET @ = LEAST(GREATEST(# + ( # * ? / 100) , 0), #), username = ? " +
		" WHERE org_id=? ";

	public void groupIncreaseChargesNoRoundOff(Connection con, String orgId, String chargeType,
			List<String> bedTypes, List<String> ids, BigDecimal amount, boolean isPercentage,
			String updateTable, String userName) throws SQLException {

		String userNameWithHint = ((null == userName) ? "" : userName) + AUDIT_LOG_HINT;
		StringBuilder query = null;
		if(updateTable !=null && updateTable.equals("UPDATECHARGE")) {
			query = new StringBuilder(
				isPercentage ? GROUP_INCR_CHARGES_PERCENTAGE_NO_ROUNDOFF.replaceAll("#",DataBaseUtil.quoteIdent(chargeType)) :
					GROUP_INCR_CHARGES_NO_ROUNDOFF.replaceAll("#",DataBaseUtil.quoteIdent(chargeType)) );

		}else if(updateTable.equals("UPDATEDISCOUNT")) {

			String updateDisAmount = GROUP_INCR_DISCOUNTS_NO_ROUNDOFF.replaceAll("@",DataBaseUtil.quoteIdent(chargeType));
			String updateDisPercent = GROUP_INCR_DISCOUNTS_PERCENTAGE_NO_ROUNDOFF.replaceAll("@",DataBaseUtil.quoteIdent(chargeType));

			if(chargeType.equals("surg_asstance_charge")) {
				chargeType = "surg_asst_discount";
			}else if(chargeType.equals("surgeon_charge")) {
				chargeType = "surg_discount";
			}else if(chargeType.equals("anesthetist_charge")) {
				chargeType = "anest_discount";
			}

			query = new StringBuilder(
				isPercentage ? updateDisPercent.replaceAll("#",DataBaseUtil.quoteIdent(chargeType)) :
					updateDisAmount.replaceAll("#",DataBaseUtil.quoteIdent(chargeType)) );

		}else {
			String chargeTypeDiscount = null;
			String updateDiscOnCharge = null;
			if(chargeType.equals("surg_asstance_charge")) {
				chargeTypeDiscount = "surg_asst_discount";
			}else if(chargeType.equals("surgeon_charge")) {
				chargeTypeDiscount = "surg_discount";
			}else if(chargeType.equals("anesthetist_charge")) {
				chargeTypeDiscount = "anest_discount";
			}

			if(isPercentage) {
				updateDiscOnCharge = GROUP_APPLY_DISCOUNTS_PERCENTAGE_NO_ROUNDOFF.replaceAll("@",DataBaseUtil.quoteIdent(chargeTypeDiscount));
				updateDiscOnCharge = updateDiscOnCharge.replaceAll("#",DataBaseUtil.quoteIdent(chargeType));
			}else {
				updateDiscOnCharge = GROUP_APPLY_DISCOUNTS_NO_ROUNDOFF.replaceAll("@",DataBaseUtil.quoteIdent(chargeTypeDiscount));
				updateDiscOnCharge = updateDiscOnCharge.replaceAll("#",DataBaseUtil.quoteIdent(chargeType));
			}
			query = new StringBuilder(updateDiscOnCharge);
		}

		SearchQueryBuilder.addWhereFieldOpValue(true, query, "bed_type", "IN", bedTypes);
		SearchQueryBuilder.addWhereFieldOpValue(true, query, "op_id", "IN", ids);

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
		" INSERT INTO operation_charges_backup (user_name, bkp_time, org_id, bed_type, op_id, " +
		"   surg_asstance_charge, surgeon_charge, anesthetist_charge, surg_asst_discount, surg_discount, anest_discount) " +
		" SELECT ?, current_timestamp, org_id, bed_type, op_id, " +
		"   surg_asstance_charge, surgeon_charge, anesthetist_charge, surg_asst_discount, surg_discount, anest_discount " +
		" FROM operation_charges WHERE org_id=?" ;

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
		" UPDATE operation_charges " +
		" SET surg_asstance_charge=?, surgeon_charge=?, anesthetist_charge=?," +
		" surg_asst_discount=?, surg_discount=?, anest_discount=? " +
		" WHERE op_id=? AND org_id=? AND bed_type=?";

	public boolean updateChargeList(Connection con, List<BasicDynaBean> chargeList) throws SQLException {

		PreparedStatement ps = con.prepareStatement(UPDATE_CHARGE);

		for (BasicDynaBean c: chargeList) {
			int i=1;
			ps.setBigDecimal(i++, (BigDecimal) c.get("surg_asstance_charge"));
			ps.setBigDecimal(i++, (BigDecimal) c.get("surgeon_charge"));
			ps.setBigDecimal(i++, (BigDecimal) c.get("anesthetist_charge"));
			ps.setBigDecimal(i++, (BigDecimal) c.get("surg_asst_discount"));
			ps.setBigDecimal(i++, (BigDecimal) c.get("surg_discount"));
			ps.setBigDecimal(i++, (BigDecimal) c.get("anest_discount"));
			ps.setString(i++, (String) c.get("op_id"));
			ps.setString(i++, (String) c.get("org_id"));
			ps.setString(i++, (String) c.get("bed_type"));
			ps.addBatch();
		}

		int results[] = ps.executeBatch();
		boolean status = DataBaseUtil.checkBatchUpdates(results);
		ps.close();
		return status;
	}
	private static final String OP_NAMESAND_iDS="select op_id,operation_name from operation_master";

	   public static List getOpNamesAndIds() throws SQLException {
		   return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(OP_NAMESAND_iDS));
	   }

	   private static final String GET_OPERATION_DETAILS="SELECT om.op_id, om.operation_name, d.dept_name, " +
	   													 "om.status,sg.service_group_name, ssg.service_sub_group_name, " +
	   													 "om.conduction_applicable,om.operation_code,om.operation_duration " +
	   													 " FROM operation_master om " +
	   													 "JOIN department d USING(dept_id) " +
	   													 "JOIN service_sub_groups ssg using(service_sub_group_id) " +
	   													 "JOIN service_groups sg using(service_group_id)" +
	   													 "ORDER BY op_id";

	    public static List<BasicDynaBean> getOperationDetails() throws SQLException{

	    	List operationList=null;
			PreparedStatement ps=null;
			Connection con=null;
			con=DataBaseUtil.getReadOnlyConnection();
			ps=con.prepareStatement(GET_OPERATION_DETAILS);
			operationList = DataBaseUtil.queryToDynaList(ps);
			DataBaseUtil.closeConnections(con, ps);
			return operationList;
	    }

	private static final String INSERT_INTO_OPERATIONS_PLUS = "INSERT INTO operation_charges(op_id,org_id,bed_type," +
		"surg_asstance_charge,surgeon_charge,anesthetist_charge,username)" +
		"(SELECT op_id,?,bed_type,round(surg_asstance_charge+?),round(surgeon_charge+?),round(anesthetist_charge+?),? " +
		"FROM operation_charges WHERE org_id =? ) ";

	private static final String INSERT_INTO_OPERATIONS_MINUS = "INSERT INTO operation_charges(op_id,org_id,bed_type," +
		"surg_asstance_charge,surgeon_charge,anesthetist_charge,username)" +
		"(SELECT op_id,?,bed_type, GREATEST(round(surg_asstance_charge - ?), 0), GREATEST(round(surgeon_charge - ?), 0), GREATEST(round(anesthetist_charge -?), 0),? " +
		"FROM operation_charges WHERE org_id =? ) ";

	private static final String INSERT_INTO_OPERATIONS_BY = "INSERT INTO operation_charges(op_id,org_id,bed_type," +
		"surg_asstance_charge,surgeon_charge,anesthetist_charge,username)" +
		"(SELECT op_id,?,bed_type,doroundvarying(surg_asstance_charge,?,?)," +
		"doroundvarying(surgeon_charge,?,?),doroundvarying(anesthetist_charge,?,?),? FROM operation_charges" +
		" WHERE org_id=? )";

	private static final String INSERT_INTO_OPER_WITH_DISCOUNTS_BY = "INSERT INTO operation_charges(op_id,org_id,bed_type," +
	"surg_asstance_charge,surgeon_charge,anesthetist_charge," +
	"surg_asst_discount,surg_discount,anest_discount," +
	"username)" +
	"(SELECT op_id,?,bed_type," +
	" doroundvarying(surg_asstance_charge,?,?)," +
	" doroundvarying(surgeon_charge,?,?),doroundvarying(anesthetist_charge,?,?)," +
	" doroundvarying(surg_asst_discount,?,?)," +
	" doroundvarying(surg_discount,?,?),doroundvarying(anest_discount,?,?)," +
	" ? FROM operation_charges" +
	" WHERE org_id=? )";

	public static boolean addOrgForOperations(Connection con,String newOrgId,String varianceType,
			Double varianceValue,Double varianceBy,boolean useValue,String baseOrgId,
			Double nearstRoundOfValue,String userName,String orgName ) throws Exception{
		return addOrgForOperations(con,newOrgId,varianceType,varianceValue,varianceBy,
				useValue,baseOrgId,nearstRoundOfValue,userName,orgName,false );
	}

	public static boolean addOrgForOperations(Connection con,String newOrgId,String varianceType,
			Double varianceValue,Double varianceBy,boolean useValue,String baseOrgId,
			Double nearstRoundOfValue,String userName,String orgName, boolean updateDiscounts ) throws Exception{

			boolean status = false;
			GenericDAO.alterTrigger(con, "DISABLE", "operation_charges", "z_operation_charges_audit_trigger");

			PreparedStatement ps = null;
			if(useValue){
				if(varianceType.equals("Incr")){
					ps = con.prepareStatement(INSERT_INTO_OPERATIONS_PLUS);
				}else{
					ps = con.prepareStatement(INSERT_INTO_OPERATIONS_MINUS);
				}
					ps.setString(1, newOrgId);
					ps.setBigDecimal(2, new BigDecimal(varianceValue));
					ps.setBigDecimal(3, new BigDecimal(varianceValue));
					ps.setBigDecimal(4, new BigDecimal(varianceValue));
					ps.setString(5, userName);
					ps.setString(6, baseOrgId);

					int i = ps.executeUpdate();
					logger.info("added charges : " + i);
					if(i>=0)status = true;
			}else{

			    if(!varianceType.equals("Incr")){
			    	varianceBy = new Double(-varianceBy);
			    }
					/*ps = con.prepareStatement(INSERT_INTO_OPERATIONS_BY);
					ps.setString(1, newOrgId);
					if(!varianceType.equals("Incr")){
					 	varianceBy = new Double(-varianceBy);
					}
					ps.setBigDecimal(2, new BigDecimal(varianceBy));
					ps.setBigDecimal(3, new BigDecimal(nearstRoundOfValue));
					ps.setBigDecimal(4, new BigDecimal(varianceBy));
					ps.setBigDecimal(5, new BigDecimal(nearstRoundOfValue));
					ps.setBigDecimal(6, new BigDecimal(varianceBy));
					ps.setBigDecimal(7, new BigDecimal(nearstRoundOfValue));
					ps.setString(8, userName);
					ps.setString(9, baseOrgId);

					int i = ps.executeUpdate(); */
					int i = insertChargesByPercent(con, newOrgId, baseOrgId, userName, varianceBy, nearstRoundOfValue, updateDiscounts);
					// logger.info("added charges : " + i);
					if(i>=0)status = true;
			}
			status &= new AuditLogDao("Master","operation_charges_audit_log").
 			logMasterChange(con, userName, "INSERT","org_id", orgName);

			if (null != ps) ps.close();

		return status;
	}

	private static int insertChargesByPercent(Connection con, String newOrgId, String baseOrgId, String userName,
			Double varianceBy, Double nearstRoundOfValue, boolean updateDiscounts) throws Exception {

		int ndx = 1;
		int numCharges = 3;

		PreparedStatement pstmt = null;
		try {
			pstmt = con.prepareStatement(updateDiscounts ?
					INSERT_INTO_OPER_WITH_DISCOUNTS_BY : INSERT_INTO_OPERATIONS_BY);
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

	  private static final String INSERT_OPERATION_CODES_FOR_ORG = "INSERT INTO operation_org_details " +
	  		"   SELECT operation_id, ?, applicable, item_code, code_type, ?, 'N'" +
	   		"	FROM operation_org_details WHERE org_id = ?";

	   public static boolean addOrgCodesForOperations(Connection con,String newOrgId,String varianceType,
			Double varianceValue,Double varianceBy,boolean useValue,String baseOrgId,Double nearstRoundOfValue) throws Exception{
	  		boolean status = false;
	  		PreparedStatement ps = null;
            BasicDynaBean obean = new OrgMasterDao().findByKey(con, "org_id", newOrgId);
            String rateSheetId = ("N".equals((String)obean.get("is_rate_sheet")) ? baseOrgId : null);
			try{
		   		ps = con.prepareStatement(INSERT_OPERATION_CODES_FOR_ORG);
				ps.setString(1,newOrgId);
				ps.setString(2, rateSheetId);
				ps.setString(3, baseOrgId);
				int i = ps.executeUpdate();
				logger.info("added codes :" + i);
				if(i>=0)status = true;
			}finally{
				if (ps != null)
					ps.close();
			}
	  		return status;
	   }

    // Rate Plan Changes - Begin

	public static boolean addOrgCodesForItems(Connection con, String newOrgId, String baseOrgId) throws Exception {
		return addOrgCodesForOperations(con, newOrgId, null, null, null, false, baseOrgId, null);
	}

    private static String INIT_ORG_DETAILS = "INSERT INTO operation_org_details " +
	       "   SELECT op_id, ?, false, null, null, null, 'N'" +
	       "   FROM operation_master";

    private static String INIT_CHARGES = "INSERT INTO operation_charges(op_id,org_id,bed_type," +
        "surg_asstance_charge,surgeon_charge,anesthetist_charge,username)" +
        "(SELECT op_id, ?, abov.bed_type, 0.0, 0.0, 0.0, ? " +
	"FROM operation_master om CROSS JOIN all_beds_orgs_view abov WHERE abov.org_id =? ) ";

    private static final String INSERT_CHARGES = "INSERT INTO operation_charges(op_id,org_id,bed_type," +
    " surg_asstance_charge, surgeon_charge, anesthetist_charge, username, is_override) " +
    " SELECT oc.op_id, ?, oc.bed_type, " +
    " doroundvarying(oc.surg_asstance_charge, ?, ?), " +
    " doroundvarying(oc.surgeon_charge, ?, ?), " +
    " doroundvarying(oc.anesthetist_charge, ?, ?), " +
    " ?, 'N' " +
    " FROM operation_charges oc, operation_org_details ood, operation_org_details oodtarget " +
    " where oc.org_id = ood.org_id and oc.op_id = ood.operation_id  " +
    " and oodtarget.org_id = ? and oodtarget.operation_id = ood.operation_id and oodtarget.base_rate_sheet_id = ? " +
    " and ood.applicable = true "+
    " and oc.org_id = ? ";

    private static final String UPDATE_CHARGES = "UPDATE operation_charges AS target SET " +
    	" surg_asstance_charge = doroundvarying(oc.surg_asstance_charge, ?, ?), " +
    	" surgeon_charge = doroundvarying(oc.surgeon_charge, ?, ?), " +
    	" anesthetist_charge = doroundvarying(oc.anesthetist_charge, ?, ?), " +
    	" surg_asst_discount = doroundvarying(oc.surg_asst_discount, ?, ?), " +
    	" surg_discount = doroundvarying(oc.surg_discount, ?, ?), " +
    	" anest_discount = doroundvarying(oc.anest_discount, ?, ?), " +
    	" username = ?, is_override = 'N' " +
    	" FROM operation_charges oc, operation_org_details ood " +
    	" where ood.org_id = ? and oc.op_id = ood.operation_id and ood.base_rate_sheet_id = ? and " +
    	" target.op_id = oc.op_id and target.bed_type = oc.bed_type and " +
    	" ood.applicable = true and target.is_override != 'Y'"+
    	" and oc.org_id = ? and target.org_id = ?";

    private static final String UPDATE_EXCLUSIONS = "UPDATE operation_org_details AS target " +
	    " SET item_code = ood.item_code, code_type = ood.code_type," +
	    " applicable = true, base_rate_sheet_id = ood.org_id, is_override = 'N' " +
	    " FROM operation_org_details ood WHERE ood.operation_id = target.operation_id and " +
	    " ood.org_id = ? and ood.applicable = true and target.org_id = ? and target.applicable = false and target.is_override != 'Y'";

    private static String INIT_ITEM_ORG_DETAILS = "INSERT INTO operation_org_details " +
    "(operation_id, org_id, applicable, item_code, code_type, base_rate_sheet_id, is_override)" +
    " ( SELECT ?, od.org_id, false, null, null, prspv.base_rate_sheet_id, 'N' FROM organization_details od " +
    " LEFT OUTER JOIN priority_rate_sheet_parameters_view prspv ON od.org_id = prspv.org_id )";

    private static String INIT_ITEM_CHARGES = "INSERT INTO operation_charges(op_id,org_id,bed_type," +
    "surg_asstance_charge, surgeon_charge, anesthetist_charge, username)" +
    "(SELECT ?, od.org_id, 'GENERAL', 0.0, 0.0, 0.0, ? FROM organization_details od) ";

    public boolean updateRatePlan(Connection con,String newOrgId, String baseOrgId,
					 String varianceType, Double variance, Double rndOff,
					 String userName, String orgName ) throws Exception {

	    boolean status = false;

		if(!varianceType.equals("Incr")) {
			variance = new Double(-variance);
	    }

        BigDecimal varianceBy = new BigDecimal(variance);
        BigDecimal roundOff = new BigDecimal(rndOff);

	    Object updparams[] = {varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff,
	    		varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff,
	    		userName, newOrgId, baseOrgId, baseOrgId, newOrgId};
	    Object insparams[] = {newOrgId, varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff, userName, newOrgId, baseOrgId, baseOrgId};
		status = updateExclusions(con, UPDATE_EXCLUSIONS, newOrgId, baseOrgId, true);
		if (status) status = updateCharges(con, UPDATE_CHARGES, updparams);
		postAuditEntry(con, "operation_charges_audit_log", userName, orgName);

		return status;

    }

    public boolean initRatePlan(Connection con, String newOrgId,String varianceType,
			Double varianceBy,String baseOrgId, Double roundOff,String userName,String orgName) throws Exception {
		boolean status = addOrgCodesForItems(con, newOrgId, baseOrgId);
		if (status) status = addOrgForOperations(con,newOrgId, varianceType,
				0.0, varianceBy, false, baseOrgId,
				roundOff, userName, orgName, true);
		postAuditEntry(con, "operation_charges_audit_log", userName, orgName);
		return status;
    }
    private static final String REINIT_EXCLUSIONS = "UPDATE operation_org_details as target " +
    " SET applicable = ood.applicable, base_rate_sheet_id = ood.org_id, " +
    " item_code = ood.item_code, code_type = ood.code_type, is_override = 'N' " +
    " FROM operation_org_details ood WHERE ood.operation_id = target.operation_id and " +
    " ood.org_id = ? and target.org_id = ? and target.is_override != 'Y'";

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
		status = updateExclusions(con, REINIT_EXCLUSIONS, newOrgId, baseOrgId, true);
		if (status) status = updateCharges(con,UPDATE_RATEPLAN_OPERATION_CHARGES, updparams);
		postAuditEntry(con, "operation_charges_audit_log", userName, orgName);
		return status;
    }

    public boolean initItemCharges(Connection con, String opId, String userName) throws Exception {

        boolean status = false;
        //  disableAuditTriggers("service_master_charges", "z_services_charges_audit_trigger");
        status = initItemCharges(con, INIT_ITEM_ORG_DETAILS, INIT_ITEM_CHARGES, opId, userName);
        // postAuditEntry(con, "service_master_charges_audit_log", userName, orgName);

        return status;
    }

    // Rate Plan Changes - End

		private static final String UPDATE_OPERATION_PLUS = "UPDATE operation_charges totab SET " +
				" surg_asstance_charge = round(fromtab.surg_asstance_charge + ?), surgeon_charge = round(fromtab.surgeon_charge + ?)," +
				" anesthetist_charge = round(fromtab.anesthetist_charge + ?)" +
				" FROM operation_charges fromtab" +
				" WHERE totab.org_id = ? AND fromtab.org_id = ?" +
				" AND totab.op_id = fromtab.op_id AND totab.bed_type = fromtab.bed_type AND totab.is_override='N'";

		private static final String UPDATE_OPERATION_MINUS = "UPDATE operation_charges totab SET " +
				" surg_asstance_charge = GREATEST(round(fromtab.surg_asstance_charge - ?), 0), surgeon_charge = GREATEST(round(fromtab.surgeon_charge - ?), 0)," +
				" anesthetist_charge = GREATEST(round(fromtab.anesthetist_charge - ?), 0)" +
				" FROM operation_charges fromtab" +
				" WHERE totab.org_id = ? AND fromtab.org_id = ?" +
				" AND totab.op_id = fromtab.op_id AND totab.bed_type = fromtab.bed_type AND totab.is_override='N'";

		private static final String UPDATE_OPERATION_BY = "UPDATE operation_charges totab SET " +
				" surg_asstance_charge = doroundvarying(fromtab.surg_asstance_charge,?,?), surgeon_charge = doroundvarying(fromtab.surgeon_charge,?,?)," +
				" anesthetist_charge = doroundvarying(fromtab.anesthetist_charge,?,?)" +
				" FROM operation_charges fromtab" +
				" WHERE totab.org_id = ? AND fromtab.org_id = ?" +
				" AND totab.op_id = fromtab.op_id AND totab.bed_type = fromtab.bed_type AND totab.is_override='N'";

		public static boolean updateOrgForOperations(Connection con,String orgId,String varianceType,
				Double varianceValue,Double varianceBy,boolean useValue,String baseOrgId,
				Double nearstRoundOfValue,String userName,String orgName ) throws SQLException, IOException {

			boolean status = false;
			PreparedStatement pstmt = null;
			GenericDAO.alterTrigger("DISABLE", "operation_charges", "z_operation_charges_audit_trigger");

			if (useValue) {

			if (varianceType.equals("Incr"))
			pstmt = con.prepareStatement(UPDATE_OPERATION_PLUS);
			else
			pstmt = con.prepareStatement(UPDATE_OPERATION_MINUS);

			pstmt.setBigDecimal(1, new BigDecimal(varianceValue));
			pstmt.setBigDecimal(2, new BigDecimal(varianceValue));
			pstmt.setBigDecimal(3, new BigDecimal(varianceValue));
			pstmt.setString(4, orgId);
			pstmt.setString(5, baseOrgId);

			int i = pstmt.executeUpdate();
			if (i>=0) status = true;

			} else {

			pstmt = con.prepareStatement(UPDATE_OPERATION_BY);
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
			status &= new AuditLogDao("Master","operation_charges_audit_log").
			logMasterChange(con, userName, "UPDATE","org_id", orgName);
			pstmt.close();

			return status;
		}

		public boolean updateOrgForDerivedRatePlans(Connection con,String[] ratePlanIds, String[] applicable,
				String opId) throws Exception{
			return updateOrgForDerivedRatePlans(con,ratePlanIds, applicable, "operation_org_details", "operations",
					"operation_id", opId);
		}

		public  boolean updateChargesForDerivedRatePlans(Connection con,String baseRateSheetId, String[] ratePlanIds,
		    		String[] bedType, Double[] surgAsstCharge,Double[] surgCharge,
		    		Double[] anesthCharge,String opId, Double[] surgAsstDiscount,Double[] surgDiscount,
		    		Double[] anestDiscount, String[] applicable)throws Exception {
			boolean success = false;

			OperationChargeDAO opdao = new OperationChargeDAO();
			for(int i=0; i<ratePlanIds.length; i++) {
	    		Map<String,Object> keys = new HashMap<String, Object>();
	    		keys.put("base_rate_sheet_id", baseRateSheetId);
	    		keys.put("org_id", ratePlanIds[i]);
	    		BasicDynaBean bean = ratePlanParameterDao.findByKey(keys);
	    		int variation =(Integer)bean.get("rate_variation_percent");
	    		int roundoff = (Integer)bean.get("round_off_amount");

	    		List<BasicDynaBean> chargeList = new ArrayList();

	    		boolean overrided = isChargeOverrided(con,ratePlanIds[i],"operation_id",opId,"operations","operation_org_details");

	    		if(!overrided) {
		    		for(int k=0 ; k<bedType.length; k++) {
		    			BasicDynaBean charge = opdao.getBean();

		    			charge.set("op_id", opId);
		    			charge.set("org_id", ratePlanIds[i]);
		    			charge.set("bed_type", bedType[k]);

		    			Double surgAsstchg = calculateCharge(surgAsstCharge[k], new Double(variation), roundoff);
		    			Double surgchg = calculateCharge(surgCharge[k], new Double(variation), roundoff);
		    			Double anesCharge = calculateCharge(anesthCharge[k], new Double(variation), roundoff);

		    			Double surgAsstDisc = calculateCharge(surgAsstDiscount[k], new Double(variation), roundoff);
		    			Double surgDisc = calculateCharge(surgDiscount[k], new Double(variation), roundoff);
		    			Double anestDisc = calculateCharge(anestDiscount[k], new Double(variation), roundoff);

		    			charge.set("surg_asstance_charge", new BigDecimal(surgAsstchg));
		    			charge.set("surgeon_charge", new BigDecimal(surgchg));
		    			charge.set("anesthetist_charge", new BigDecimal(anesCharge));
		    			charge.set("surg_asst_discount", new BigDecimal(surgAsstDisc));
		    			charge.set("surg_discount", new BigDecimal(surgDisc));
		    			charge.set("anest_discount", new BigDecimal(anestDisc));
		    			chargeList.add(charge);
		    		}
	    		}
	    		for (BasicDynaBean c: chargeList) {
	    			opdao.updateWithNames(con, c.getMap(),  new String[] {"op_id", "org_id", "bed_type"});
	    		}
	    		success = true;

	    		boolean overrideItemCharges = checkItemStatus(con, ratePlanIds[i], "operation_org_details", "operations", "operation_id",
	    				opId, applicable[i]);
	    		if(overrideItemCharges)
	    			OverrideItemCharges(con, "operation_charges", "operations", ratePlanIds[i], "op_id", opId);
			}
			return success;
		}

		private static final String GET_DERIVED_RATE_PALN_DETAILS = "select rp.org_id,od.org_name, "+
			" case when rate_variation_percent<0 then 'Decrease By' else 'Increase By' end as discormarkup, "+
			" rate_variation_percent,round_off_amount,ood.applicable,ood.operation_id,rp.base_rate_sheet_id, ood.is_override "+
			" from rate_plan_parameters rp "+
			" join organization_details od on(od.org_id=rp.org_id) "+
			" join operation_org_details ood on (ood.org_id = rp.org_id) "+
			" where rp.base_rate_sheet_id =?  and operation_id=? and ood.base_rate_sheet_id = ? ";

		public List<BasicDynaBean> getDerivedRatePlanDetails(String baseRateSheetId,String opId)throws SQLException {
			return getDerivedRatePlanDetails(baseRateSheetId, "operations", opId,GET_DERIVED_RATE_PALN_DETAILS);
		}

		private static final String UPDATE_RATEPLAN_OPERATION_CHARGES = "UPDATE operation_charges totab SET " +
				" surg_asstance_charge = doroundvarying(fromtab.surg_asstance_charge,?,?), surgeon_charge = doroundvarying(fromtab.surgeon_charge,?,?)," +
				" anesthetist_charge = doroundvarying(fromtab.anesthetist_charge,?,?), " +
				" surg_asst_discount = doroundvarying(fromtab.surg_asst_discount,?,?), " +
				" surg_discount = doroundvarying(fromtab.surg_discount,?,?), " +
				" anest_discount = doroundvarying(fromtab.anest_discount,?,?) " +
				" FROM operation_charges fromtab" +
				" WHERE totab.org_id = ? AND fromtab.org_id = ?" +
				" AND totab.op_id = fromtab.op_id AND totab.bed_type = fromtab.bed_type AND totab.is_override='N'";

		public boolean updateOperationChargesForDerivedRatePlans(String orgId,String varianceType,
				Double varianceValue,Double varianceBy,String baseOrgId,
				Double nearstRoundOfValue,String userName,String orgName, boolean upload ) throws SQLException, Exception {

			boolean success = false;
			Connection con = null;
			GenericDAO.alterTrigger("DISABLE", "operation_charges", "z_operation_charges_audit_trigger");

			try {
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);

				if(upload) {
					List<BasicDynaBean> rateSheetList =  getRateSheetsByPriority(con, orgId, ratePlanParameterDao);
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
							variance, roundoff, variance, roundoff, userName, orgId, baseOrgId, baseOrgId, orgId};
					success = updateCharges(con, UPDATE_CHARGES, updparams);
				}

				success &= new AuditLogDao("Master","operation_charges_audit_log").
				logMasterChange(con, userName, "UPDATE","org_id", orgName);
			}finally{
				DataBaseUtil.commitClose(con, success);
			}

			return success;
		}

		public static boolean updateChargesBasedOnNewRateSheet(Connection con, String orgId,Double varianceBy,
				String baseOrgId,Double nearstRoundOfValue, String operationId)throws SQLException,Exception {
			boolean success = false;
			PreparedStatement ps = null;
			try {
				ps = con.prepareStatement(UPDATE_RATEPLAN_OPERATION_CHARGES+ " AND totab.op_id = ? ");
				setPsValues(ps,varianceBy,nearstRoundOfValue,orgId,baseOrgId);
				ps.setString(15, operationId);

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

		private static final String UPDATE_CHARGES_BY_PERCENT = "UPDATE operation_charges totab SET " +
		" surg_asstance_charge = round(surg_asstance_charge*(100+?)/100/?, 0)*?, " +
		" surgeon_charge = round(surgeon_charge*(100+?)/100/?,0)*?," +
		" anesthetist_charge = round(anesthetist_charge*(100+?)/?,0)*?, " +
		" surg_asst_discount = round(surg_asst_discount*(100+?)/?,0)*?, " +
		" surg_discount = round(surg_discount*(100+?)/?,0)*?, " +
		" anest_discount = round(anest_discount*(100+?)/?,0)*? " +
		" WHERE org_id = ? AND bed_type = ?";

		public boolean updateChargesByPercent(Connection con, Double varianceBy, Double roundOff, String ratePlanId, String bedType) throws SQLException {
			int i = updateChargesByPercent(con, UPDATE_CHARGES_BY_PERCENT, // update query
												3, 							// No of charge fields in the query
																			//assumes num charge fields= num discounts
												varianceBy, roundOff, 		// variance and roundoff passed in
												ratePlanId, 				// which rate plan - we do one by one
												bedType,					// new bed type being added
												true);						// whether there are discount fields in the query
			return (i >=0);
		}


		public boolean updateBedForRatePlan(Connection con, String ratePlanId,
				Double variance,String rateSheetId, Double rndOff, String bedType) throws Exception {

			boolean status = false;

			BigDecimal varianceBy = new BigDecimal(variance);
		    BigDecimal roundOff = new BigDecimal(rndOff);

			Object updparams[] = {varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff,
					varianceBy, roundOff, varianceBy, roundOff, ratePlanId, rateSheetId,bedType};
			status = updateCharges(con,UPDATE_RATEPLAN_OPERATION_CHARGES + " AND totab.bed_type=? ", updparams);
			return status;
		}

		public void operationChargeScheduleJob(String operationId, String userName) {
			LinkedHashMap<String, Object> queryParams =
		            new LinkedHashMap<String, Object>();
		        // Maintain the queryparams as per operation charges index
		        queryParams.put("org_id", "abov.org_id");
		        queryParams.put("bed_type", "abov.bed_type");
		        queryParams.put("op_id", operationId);
		        queryParams.put("surg_asstance_charge", BigDecimal.ZERO);
		        queryParams.put("surgeon_charge", BigDecimal.ZERO);
		        queryParams.put("anesthetist_charge", BigDecimal.ZERO);
		        queryParams.put("username", userName);
		        CronJobService cronJobService = ApplicationContextProvider.getBean(CronJobService.class);
		        cronJobService.masterChargeScheduleJob(queryParams, "operation_charges", "OPERATION");
		}
}
