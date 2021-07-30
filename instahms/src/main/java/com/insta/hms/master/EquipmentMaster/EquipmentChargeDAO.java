package com.insta.hms.master.EquipmentMaster;

import com.bob.hms.adminmasters.organization.OrgMasterDao;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.SearchQueryBuilder;
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
import java.util.List;
import java.util.Map;

public class EquipmentChargeDAO extends ItemChargeDAO {

	static Logger logger = LoggerFactory.getLogger(EquipmentChargeDAO.class);
    private static final GenericDAO ratePlanParametersDAO = new GenericDAO("rate_plan_parameters");

	public EquipmentChargeDAO() {
		super("equipement_charges");
	}

	/*
	 * Returns charges of all charge types for the given orgId, and list of Equipment IDs,
	 * for all bed types
	 *
	 * This is used for displaying the charges for each bed in the main list master screen
	 */

	private static final String GET_ALL_CHARGES_FOR_ORG =
		" SELECT equip_id, bed_type, daily_charge, min_charge, incr_charge ,slab_1_charge, tax ," +
		" daily_charge_discount, min_charge_discount, incr_charge_discount, slab_1_charge_discount  " +
		" FROM equipement_charges " +
		" WHERE org_id=?";

	public List<BasicDynaBean> getAllChargesForOrg(String orgId, List<String> ids) throws SQLException {

		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			StringBuilder query = new StringBuilder(GET_ALL_CHARGES_FOR_ORG);
			SearchQueryBuilder.addWhereFieldOpValue(true, query, "equip_id", "IN", ids);

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
	 * Returns charges of all charge types for the given orgId, and a single Equipment ID,
	 * for all bed types
	 */
	public List<BasicDynaBean> getAllChargesForOrgEquipment(String orgId, String id) throws SQLException {
		List<String> ids = new ArrayList();
		ids.add(id);
		return getAllChargesForOrg(orgId, ids);
	}

	/*
	 * Gets the charges for an equipment for the given orgid and bed type
	 */
	public static final String chargequery = "SELECT " +
		"  ec.equip_id, em.equipment_name, em.equipment_code, em.dept_id, " +
		"  em.duration_unit_minutes, em.min_duration, em.slab_1_threshold, em.incr_duration, " +
		"  em.service_sub_group_id, em.insurance_category_id, " +
		"  ec.daily_charge as charge, ec.daily_charge_discount, ec.min_charge, ec.min_charge_discount, " +
		"  ec.slab_1_charge, ec.slab_1_charge_discount, ec.incr_charge, ec.incr_charge_discount, ec.tax, " +
		" em.duration_unit_minutes,em.slab_1_threshold,allow_rate_increase,"+
		" allow_rate_decrease, billing_group_id "+
		" FROM equipement_charges ec " +
		"  JOIN equipment_master em on(ec.equip_id = em.eq_id) " +
		" WHERE ec.equip_id=? AND ec.bed_type =? AND ec.org_id =?";

	public BasicDynaBean getEquipmentCharge(String equipmentid,String bedtype,String orgid)
		throws SQLException{

		Connection con=null;
		PreparedStatement ps=null;
		List cl = null;
		BasicDynaBean bean = null;

		try {
			 con=DataBaseUtil.getConnection();

			 String generalorgid = "ORG0001";
			 String generalbedtype = "GENERAL";

			 ps = con.prepareStatement(chargequery);
			 ps.setString(1, equipmentid);
			 ps.setString(2, bedtype);
			 ps.setString(3, orgid);

			 bean = DataBaseUtil.queryToDynaBean(ps);

			 if (bean == null) {
				 ps.setString(1, equipmentid);
				 ps.setString(2, generalbedtype);
				 ps.setString(3, generalorgid);
				 bean = DataBaseUtil.queryToDynaBean(ps);
			 }
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return bean;
	}

	/*
	 * writes into a XLS file, for all the types of charges
	 */
	public void getAllChargesForBedTypesXLS(String orgId, List<String> bedTypes, HSSFSheet sheet)
		throws SQLException, IOException {

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<String> columnNames = new ArrayList<String>();

		StringBuilder query = new StringBuilder();
		String[] chargeTypes = {"daily_charge", "daily_charge_discount", "min_charge", "min_charge_discount",
								"incr_charge", "incr_charge_discount" , "slab_1_charge", "slab_1_charge_discount","tax"};

		query.append("SELECT em.eq_id , equipment_name ");
		int i = 1;
		for (String bedType: bedTypes) {
			for (String chargeType: chargeTypes) {
				query.append(", ec"+i+".").append(chargeType);
				query.append(" AS ").append(DataBaseUtil.quoteIdent(bedType+"/"+chargeType, true));
			}
			i++;
		}

		query.append(" FROM equipment_master em ");

		i = 1;
		for (String bedType: bedTypes) {
			query.append(" JOIN equipement_charges ec"+i).append(" ON ");
			query.append("  ec"+i+".equip_id=em.eq_id AND ");
			query.append("  ec"+i+".bed_type=? AND");
			query.append("  ec"+i+".org_id=? ");
			i++;
		}
        query.append(" WHERE em.status='A' " );
		query.append(" order by em.eq_id");

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(query.toString());

			i=1;
			for (String bedType: bedTypes) {
				ps.setString(i++, bedType);
				ps.setString(i++, orgId);
			}

			rs = ps.executeQuery();
			ResultSetMetaData metaData = rs.getMetaData();
			int colCount = metaData.getColumnCount();

			for (int c=1; c<=colCount; c++) {
				columnNames.add(metaData.getColumnName(c));
			}

			//HSSFWorkbookUtils.createPhysicalCellsWithValues(DataBaseUtil.queryToDynaList(ps), columnNames, sheet);

		} finally {
			DataBaseUtil.closeConnections(con, ps, rs);
		}
	}

	/*
	 * Copy the GENERAL rate plan charges to all other rate plans. Assumes that the other
	 * rate-plan charges are non-existent, ie, no updates only inserts new charges.
	 */
	private static final String COPY_GENERAL_CHARGES_TO_ALL_ORGS =
		" INSERT INTO equipement_charges " +
		"   (org_id, bed_type, equip_id, daily_charge, min_charge, incr_charge, slab_1_charge, tax," +
		"	daily_charge_discount, min_charge_discount, incr_charge_discount, slab_1_charge_discount) " +
		" SELECT abo.org_id, abo.bed_type,  ec.equip_id, ec.daily_charge, ec.min_charge,ec.incr_charge,ec.slab_1_charge,ec.tax," +
		"	ec.daily_charge_discount, ec.min_charge_discount, ec.incr_charge_discount, ec.slab_1_charge_discount " +
		" FROM all_beds_orgs_view abo " +
		"  JOIN equipement_charges ec ON (ec.equip_id=? AND ec.bed_type=abo.bed_type AND ec.org_id='ORG0001') " +
		" WHERE abo.org_id != 'ORG0001'";

	public void copyGeneralChargesToAllOrgs(Connection con, String id) throws SQLException {
		PreparedStatement ps = con.prepareStatement(COPY_GENERAL_CHARGES_TO_ALL_ORGS);
		ps.setString(1, id);
		ps.executeUpdate();
		ps.close();
	}

	/*
	 * Copy the GENERAL bed type charges to all inactive bed types for the given Equipment ID.
	 * These charges will not be inserted normally when the services is created, since
	 * only active bed charges are input from the user.
	 */
	private static final String COPY_GENERAL_CHARGES_TO_INACTIVE_BEDS =
		" INSERT INTO equipement_charges " +
		"   (org_id, bed_type, equip_id, daily_charge, min_charge, incr_charge, slab_1_charge, tax," +
		"	daily_charge_discount, min_charge_discount, incr_charge_discount, slab_1_charge_discount) " +
		" SELECT abo.org_id, abo.bed_type, ec.equip_id, ec.daily_charge, ec.min_charge, " +
		"    ec.incr_charge,ec.slab_1_charge,tax,ec.daily_charge_discount, ec.min_charge_discount, ec.incr_charge_discount, ec.slab_1_charge_discount " +
		" FROM all_beds_orgs_view abo " +
		"   JOIN equipement_charges ec ON (ec.org_id = abo.org_id AND ec.bed_type = 'GENERAL') " +
		" WHERE abo.bed_type IN ( " +
		"     SELECT DISTINCT intensive_bed_type FROM icu_bed_charges WHERE bed_status = 'I' " +
		"     UNION ALL SELECT DISTINCT bed_type FROM bed_details WHERE bed_status = 'I') " +
		"   AND ec.equip_id=? " ;

	public void copyGeneralChargesToInactiveBeds(Connection con, String id) throws SQLException {
		PreparedStatement ps = con.prepareStatement(COPY_GENERAL_CHARGES_TO_INACTIVE_BEDS);
		ps.setString(1, id);
		ps.executeUpdate();
		ps.close();
	}

	public void groupIncreaseCharges(Connection con, String orgId, String chargeType,
			List<String> bedTypes, List<String> ids, BigDecimal amount,
			boolean isPercentage,BigDecimal roundTo, String updateTable) throws SQLException {

		if (roundTo.compareTo(BigDecimal.ZERO) == 0) {
			groupIncreaseChargesNoRoundOff(con, orgId, chargeType, bedTypes, ids,
					amount, isPercentage, updateTable);
		}else {
			groupIncreaseChargesWithRoundOff(con, orgId, chargeType, bedTypes, ids,
					amount, isPercentage, roundTo, updateTable);
		}
	}

	/*
	 * Group increase charges: takes in:
	 *  - orgId to update (reqd)
	 *  - list of bed types to update (optional, if not given, all bed types)
	 *  - list of IDs to update (optional, if not given, all Equipments)
	 *  - amount to increase by (can be negative for a decrease)
	 *  - whether the amount is a percentage instead of an abs. amount
	 *  - an amount to be rounded to (nearest). Rounding to 0 is invalid.
	 *
	 * The new amount will not be allowed to go less than zero.
	 */
	private String GROUP_INCR_CHARGES =
		" UPDATE equipement_charges SET # = GREATEST( round((#+?)/?,0)*?, 0) " +
		" WHERE org_id=? ";

	private String GROUP_INCR_CHARGES_PERCENTAGE =
		" UPDATE equipement_charges SET # = GREATEST( round(#*(100+?)/100/?,0)*?, 0) " +
		" WHERE org_id=? ";

	private String GROUP_INCR_DISCOUNTS =
		" UPDATE equipement_charges SET # = LEAST(GREATEST( round((#+?)/?,0)*?, 0), @) " +
		" WHERE org_id=? ";

	private String GROUP_INCR_DISCOUNTS_PERCENTAGE =
		" UPDATE equipement_charges SET # = LEAST(GREATEST( round(#*(100+?)/100/?,0)*?, 0), @) " +
		" WHERE org_id=? ";

	private String GROUP_APPLY_DISCOUNTS =
		" UPDATE equipement_charges SET @ = LEAST(GREATEST( round(( # + ?)/?,0)*?, 0), #) " +
		" WHERE org_id=? ";

	private String GROUP_APPLY_DISCOUNTS_PERCENTAGE =
		" UPDATE equipement_charges SET @ = LEAST(GREATEST( round(#+(# * ?/100/?),0)*?, 0), #) " +
		" WHERE org_id=? ";

	public void groupIncreaseChargesWithRoundOff(Connection con, String orgId, String chargeType,
			List<String> bedTypes, List<String> ids, BigDecimal amount, boolean isPercentage,BigDecimal roundTo, String updateTable)
		throws SQLException {

		StringBuilder query = null;
		if(updateTable !=null && updateTable.equals("UPDATECHARGE")) {
			query = new StringBuilder(
				isPercentage ? GROUP_INCR_CHARGES_PERCENTAGE.replaceAll("#",DataBaseUtil.quoteIdent(chargeType)) :
					GROUP_INCR_CHARGES.replaceAll("#",DataBaseUtil.quoteIdent(chargeType)) );

		}else if(updateTable.equals("UPDATEDISCOUNT")) {

			GROUP_INCR_DISCOUNTS = GROUP_INCR_DISCOUNTS.replaceAll("@", DataBaseUtil.quoteIdent(chargeType));
			GROUP_INCR_DISCOUNTS_PERCENTAGE = GROUP_INCR_DISCOUNTS_PERCENTAGE.replaceAll("@", DataBaseUtil.quoteIdent(chargeType));

			chargeType = chargeType+"_discount";

			query = new StringBuilder(
				isPercentage ? GROUP_INCR_DISCOUNTS_PERCENTAGE.replaceAll("#",DataBaseUtil.quoteIdent(chargeType)) :
					GROUP_INCR_DISCOUNTS.replaceAll("#",DataBaseUtil.quoteIdent(chargeType)) );

		}else {
			String chargeTypeDiscount = chargeType+"_discount";
			if(isPercentage) {
				GROUP_APPLY_DISCOUNTS_PERCENTAGE = GROUP_APPLY_DISCOUNTS_PERCENTAGE.replaceAll("@",DataBaseUtil.quoteIdent(chargeTypeDiscount));
				GROUP_APPLY_DISCOUNTS_PERCENTAGE = GROUP_APPLY_DISCOUNTS_PERCENTAGE.replaceAll("#",DataBaseUtil.quoteIdent(chargeType));
			}else {
				GROUP_APPLY_DISCOUNTS = GROUP_APPLY_DISCOUNTS.replaceAll("@",DataBaseUtil.quoteIdent(chargeTypeDiscount));
				GROUP_APPLY_DISCOUNTS = GROUP_APPLY_DISCOUNTS.replaceAll("#",DataBaseUtil.quoteIdent(chargeType));
			}
			query = new StringBuilder(isPercentage ? GROUP_APPLY_DISCOUNTS_PERCENTAGE :	GROUP_APPLY_DISCOUNTS);
		}

		SearchQueryBuilder.addWhereFieldOpValue(true, query, "bed_type", "IN", bedTypes);
		SearchQueryBuilder.addWhereFieldOpValue(true, query, "equip_id", "IN", ids);

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

	private String GROUP_INCR_CHARGES_NO_ROUNDOFF =
		" UPDATE equipement_charges SET # = GREATEST( # + ?, 0) " +
		" WHERE org_id=? ";

	private String GROUP_INCR_CHARGES_PERCENTAGE_NO_ROUNDOFF =
		" UPDATE equipement_charges SET # = GREATEST(# +( # * ? / 100 ) , 0) " +
		" WHERE org_id=? ";

	private String GROUP_INCR_DISCOUNTS_NO_ROUNDOFF =
		" UPDATE equipement_charges SET # = LEAST(GREATEST( # + ?, 0), @) " +
		" WHERE org_id=? ";

	private String GROUP_INCR_DISCOUNTS_PERCENTAGE_NO_ROUNDOFF =
		" UPDATE equipement_charges SET # = LEAST(GREATEST(# +( # * ? / 100 ) , 0), @) " +
		" WHERE org_id=? ";

	private String GROUP_APPLY_DISCOUNTS_NO_ROUNDOFF =
		" UPDATE equipement_charges SET @ = LEAST(GREATEST( # + ?, 0), #) " +
		" WHERE org_id=? ";

	private String GROUP_APPLY_DISCOUNTS_PERCENTAGE_NO_ROUNDOFF =
		" UPDATE equipement_charges SET @ = LEAST(GREATEST(# + ( # * ? / 100) , 0), #) " +
		" WHERE org_id=? ";

	public void groupIncreaseChargesNoRoundOff(Connection con, String orgId, String chargeType,
			List<String> bedTypes, List<String> ids, BigDecimal amount, boolean isPercentage,String updateTable)
		throws SQLException {

		StringBuilder query = null;
		if(updateTable !=null && updateTable.equals("UPDATECHARGE")) {
			query = new StringBuilder(
				isPercentage ? GROUP_INCR_CHARGES_PERCENTAGE_NO_ROUNDOFF.replaceAll("#",DataBaseUtil.quoteIdent(chargeType)) :
					GROUP_INCR_CHARGES_NO_ROUNDOFF.replaceAll("#",DataBaseUtil.quoteIdent(chargeType)) );

		}else if(updateTable.equals("UPDATEDISCOUNT")) {

			GROUP_INCR_DISCOUNTS_NO_ROUNDOFF = GROUP_INCR_DISCOUNTS_NO_ROUNDOFF.replaceAll("@", DataBaseUtil.quoteIdent(chargeType));
			GROUP_INCR_DISCOUNTS_PERCENTAGE_NO_ROUNDOFF = GROUP_INCR_DISCOUNTS_PERCENTAGE_NO_ROUNDOFF.replaceAll("@", DataBaseUtil.quoteIdent(chargeType));

			chargeType = chargeType+"_discount";

			query = new StringBuilder(
				isPercentage ? GROUP_INCR_DISCOUNTS_PERCENTAGE_NO_ROUNDOFF.replaceAll("#",DataBaseUtil.quoteIdent(chargeType)) :
					GROUP_INCR_DISCOUNTS_NO_ROUNDOFF.replaceAll("#",DataBaseUtil.quoteIdent(chargeType)) );

		}else {
			String chargeTypeDiscount = chargeType+"_discount";
			if(isPercentage) {
				GROUP_APPLY_DISCOUNTS_PERCENTAGE_NO_ROUNDOFF = GROUP_APPLY_DISCOUNTS_PERCENTAGE_NO_ROUNDOFF.replaceAll("@",DataBaseUtil.quoteIdent(chargeTypeDiscount));
				GROUP_APPLY_DISCOUNTS_PERCENTAGE_NO_ROUNDOFF = GROUP_APPLY_DISCOUNTS_PERCENTAGE_NO_ROUNDOFF.replaceAll("#",DataBaseUtil.quoteIdent(chargeType));
			}else {
				GROUP_APPLY_DISCOUNTS_NO_ROUNDOFF = GROUP_APPLY_DISCOUNTS_NO_ROUNDOFF.replaceAll("@",DataBaseUtil.quoteIdent(chargeTypeDiscount));
				GROUP_APPLY_DISCOUNTS_NO_ROUNDOFF = GROUP_APPLY_DISCOUNTS_NO_ROUNDOFF.replaceAll("#",DataBaseUtil.quoteIdent(chargeType));
			}
			query = new StringBuilder(isPercentage ? GROUP_APPLY_DISCOUNTS_PERCENTAGE_NO_ROUNDOFF :	GROUP_APPLY_DISCOUNTS_NO_ROUNDOFF);
		}

		SearchQueryBuilder.addWhereFieldOpValue(true, query, "bed_type", "IN", bedTypes);
		SearchQueryBuilder.addWhereFieldOpValue(true, query, "equip_id", "IN", ids);

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

	/*
	 * Backs up the current set of charges in a given rate plan into a backup table.
	 * To store a record of the "previous" state of the rate plan before updating via
	 * a CSV file upload.
	 */
	private static final String BACKUP_CHARGES =
		" INSERT INTO equipment_charges_backup (user_name, bkp_time, org_id, bed_type, equip_id, " +
		"   daily_charge, min_charge, incr_charge,tax, daily_charge_discount, min_charge_discount, incr_charge_discount ) " +
		" SELECT ?, current_timestamp, org_id, bed_type, equip_id, " +
		"   daily_charge, min_charge, incr_charge,tax, daily_charge_discount, min_charge_discount, incr_charge_discount " +
		" FROM equipement_charges WHERE org_id=?" ;

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
		" UPDATE equipement_charges " +
		" SET daily_charge=?, min_charge=?, incr_charge=? ,slab_1_charge=?, tax=?, " +
		" daily_charge_discount=?, min_charge_discount=?, incr_charge_discount=?, slab_1_charge_discount=? " +
		" WHERE equip_id=? AND org_id=? AND bed_type=?";

	public boolean updateChargeList(Connection con, List<BasicDynaBean> chargeList) throws SQLException {

		PreparedStatement ps = con.prepareStatement(UPDATE_CHARGE);

		for (BasicDynaBean c: chargeList) {
			int i=1;
			ps.setBigDecimal(i++, (BigDecimal) c.get("daily_charge"));
			ps.setBigDecimal(i++, (BigDecimal) c.get("min_charge"));
			ps.setBigDecimal(i++, (BigDecimal) c.get("incr_charge"));
			ps.setBigDecimal(i++, (BigDecimal) c.get("slab_1_charge"));
			ps.setBigDecimal(i++, (BigDecimal) c.get("tax"));
			ps.setBigDecimal(i++, (BigDecimal) c.get("daily_charge_discount"));
			ps.setBigDecimal(i++, (BigDecimal) c.get("min_charge_discount"));
			ps.setBigDecimal(i++, (BigDecimal) c.get("incr_charge_discount"));
			ps.setBigDecimal(i++, (BigDecimal) c.get("slab_1_charge_discount"));
			ps.setString(i++, (String) c.get("equip_id"));
			ps.setString(i++, (String) c.get("org_id"));
			ps.setString(i++, (String) c.get("bed_type"));
			ps.addBatch();
		}

		int results[] = ps.executeBatch();
		boolean status = DataBaseUtil.checkBatchUpdates(results);
		ps.close();
		return status;
	}

	private static final String INSERT_INTO_EQUIPMENTS_PLUS = "INSERT INTO equipement_charges(equip_id,org_id,bed_type," +
		"daily_charge,min_charge,incr_charge,tax, slab_1_charge)(SELECT equip_id,?,bed_type,round(daily_charge + ?),round(min_charge + ?)," +
		"round(incr_charge+?),tax, round(slab_1_charge + ?) FROM equipement_charges where org_id=? )";

	private static final String INSERT_INTO_EQUIPMENTS_MINUS = "INSERT INTO equipement_charges(equip_id,org_id,bed_type," +
			"daily_charge,min_charge,incr_charge,tax, slab_1_charge)(SELECT equip_id,?,bed_type, GREATEST(round(daily_charge - ?), 0), GREATEST(round(min_charge - ?), 0)," +
			"GREATEST(round(incr_charge - ?), 0),tax, GREATEST(round(slab_1_charge - ?), 0) FROM equipement_charges where org_id=? )";

	private static final String INSERT_INTO_EQUIPMENTS_BY = "INSERT INTO equipement_charges(equip_id,org_id,bed_type," +
			"daily_charge,min_charge,incr_charge,tax, slab_1_charge)(SELECT equip_id,?,bed_type,doroundvarying(daily_charge,?,?)," +
			"doroundvarying(min_charge,?,?),doroundvarying(incr_charge,?,?),tax, doroundvarying(slab_1_charge,?,?) FROM equipement_charges WHERE org_id=?)";

	private static final String INSERT_INTO_EQUIP_WITH_DISCOUNTS_BY = "INSERT INTO equipement_charges(equip_id,org_id,bed_type," +
	"daily_charge,min_charge,incr_charge,tax, slab_1_charge, " +
	"daily_charge_discount,min_charge_discount,incr_charge_discount, slab_1_charge_discount" +
	")(SELECT equip_id,?,bed_type," +
	"doroundvarying(daily_charge,?,?)," +
	"doroundvarying(min_charge,?,?),doroundvarying(incr_charge,?,?),tax, doroundvarying(slab_1_charge,?,?), " +
	"doroundvarying(daily_charge_discount,?,?)," +
	"doroundvarying(min_charge_discount,?,?),doroundvarying(incr_charge_discount,?,?),doroundvarying(slab_1_charge_discount,?,?) " +
	"FROM equipement_charges WHERE org_id=?)";

    public static boolean addOrgForEquipments(Connection con,String newOrgId,String varianceType,
                    Double varianceValue,Double varianceBy,boolean useValue,String baseOrgId,
    		Double nearstRoundOfValue) throws Exception{
            return addOrgForEquipments(con,newOrgId,varianceType,varianceValue,varianceBy,
                            useValue,baseOrgId,nearstRoundOfValue,false );
    }

	public static boolean addOrgForEquipments(Connection con,String newOrgId,String varianceType,
		Double varianceValue,Double varianceBy,boolean useValue,String baseOrgId,
		Double nearstRoundOfValue, boolean updateDiscounts) throws Exception{

  		boolean status = false;
  		PreparedStatement ps = null;
  		if(useValue){
  			if(varianceType.equals("Incr")){
  				ps = con.prepareStatement(INSERT_INTO_EQUIPMENTS_PLUS);
  			}else{
  				ps = con.prepareStatement(INSERT_INTO_EQUIPMENTS_MINUS);
  			}

  			ps.setString(1, newOrgId);
  			ps.setBigDecimal(2, new BigDecimal(varianceValue));
  			ps.setBigDecimal(3, new BigDecimal(varianceValue));
  			ps.setBigDecimal(4, new BigDecimal(varianceValue));
  			ps.setBigDecimal(5, new BigDecimal(varianceValue));
  			ps.setString(6,baseOrgId );

			int i = ps.executeUpdate();
			logger.debug(Integer.toString(i));
			if(i>=0)status = true;
  		}else{
			if(!varianceType.equals("Incr")){
			 	varianceBy = new Double(-varianceBy);
			}
/*	  			ps = con.prepareStatement(INSERT_INTO_EQUIPMENTS_BY);
	  			ps.setString(1, newOrgId);
				ps.setBigDecimal(2, new BigDecimal(varianceBy));
				ps.setBigDecimal(3, new BigDecimal(nearstRoundOfValue));
				ps.setBigDecimal(4, new BigDecimal(varianceBy));
				ps.setBigDecimal(5, new BigDecimal(nearstRoundOfValue));
				ps.setBigDecimal(6, new BigDecimal(varianceBy));
				ps.setBigDecimal(7, new BigDecimal(nearstRoundOfValue));
				ps.setBigDecimal(8, new BigDecimal(varianceBy));
				ps.setBigDecimal(9, new BigDecimal(nearstRoundOfValue));
				ps.setString(10, baseOrgId);

				int i = ps.executeUpdate(); */
			int i = insertChargesByPercent(con, newOrgId, baseOrgId, varianceBy, nearstRoundOfValue, updateDiscounts);
			logger.debug(Integer.toString(i));
			if(i>=0)status = true;

  		}
  		if (null != ps) ps.close();

	  	return status;
	}

    private static int insertChargesByPercent(Connection con, String newOrgId, String baseOrgId,
            Double varianceBy, Double nearstRoundOfValue, boolean updateDiscounts) throws Exception {

	    int ndx = 1;
	    int numCharges = 4;

	    PreparedStatement pstmt = null;
	    try {
			pstmt = con.prepareStatement(updateDiscounts ?
					INSERT_INTO_EQUIP_WITH_DISCOUNTS_BY : INSERT_INTO_EQUIPMENTS_BY);
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


	public static boolean addOrgCodesForItems(Connection con, String newOrgId, String baseOrgId, String userName) throws Exception {
		return addOrgCodesForEquipment(con, newOrgId, null, null, null, false, baseOrgId, null, userName);
	}


    private static String INIT_ORG_DETAILS = "INSERT INTO equip_org_details" +
    " (equip_id, org_id, applicable, base_rate_sheet_id, is_override) " +
    "   SELECT eq_id, ?, false, null, 'N'" +
    "   FROM equipment_master";

    private static String INIT_CHARGES = "INSERT INTO equipement_charges(equip_id,org_id,bed_type," +
    "daily_charge,min_charge,incr_charge,slab_1_charge)" +
    "(SELECT eq_id, ?, abov.bed_type, 0.0, 0.0, 0.0, 0.0" +
    "FROM equipment_master em CROSS JOIN all_beds_orgs_view abov WHERE abov.org_id =? ) ";

    private static final String INSERT_CHARGES = "INSERT INTO equipement_charges(equip_id,org_id,bed_type," +
    " daily_charge,min_charge,incr_charge,slab_1_charge, tax, is_override)" +
    " SELECT ec.equip_id, ?, ec.bed_type, " +
    " doroundvarying(ec.daily_charge, ?, ?), " +
    " doroundvarying(ec.min_charge, ?, ?), " +
    " doroundvarying(ec.incr_charge, ?, ?), " +
    " doroundvarying(ec.slab_1_charge, ?, ?), " +
    " ec.tax, " +
    " 'N' " +
    " FROM equipement_charges ec, equip_org_details eod, equip_org_details eodtarget " +
    " where ec.org_id = eod.org_id and ec.equip_id = eod.equip_id " +
    " and eodtarget.org_id = ? and eodtarget.equip_id = eod.equip_id and eodtarget.base_rate_sheet_id = ? " +
    " and eod.applicable = true "+
    " and ec.org_id = ? ";

    private static final String UPDATE_CHARGES = "UPDATE equipement_charges AS target SET " +
    " daily_charge = doroundvarying(ec.daily_charge, ?, ?), " +
    " min_charge = doroundvarying(ec.min_charge, ?, ?), " +
    " incr_charge = doroundvarying(ec.incr_charge, ?, ?), " +
    " slab_1_charge = doroundvarying(ec.slab_1_charge, ?, ?), " +
    " daily_charge_discount = doroundvarying(ec.daily_charge_discount, ?, ?), " +
    " min_charge_discount = doroundvarying(ec.min_charge_discount, ?, ?), " +
    " incr_charge_discount = doroundvarying(ec.incr_charge_discount, ?, ?), " +
    " slab_1_charge_discount = doroundvarying(ec.slab_1_charge_discount, ?, ?), " +
    " tax = ec.tax, " +
    " is_override = 'N' " +
    " FROM equipement_charges ec, equip_org_details eod " +
    " where eod.org_id = ? and ec.equip_id = eod.equip_id and eod.base_rate_sheet_id = ? and" +
    " target.equip_id = ec.equip_id and target.bed_type = ec.bed_type and " +
    " eod.applicable = true and target.is_override != 'Y'"+
    " and ec.org_id = ? and target.org_id = ?";

    private static final String UPDATE_EXCLUSIONS = "UPDATE equip_org_details AS target " +
    " SET applicable = true, base_rate_sheet_id = eod.org_id, is_override = 'N' " +
    " FROM equip_org_details eod WHERE eod.equip_id = target.equip_id and " +
    " eod.org_id = ? and eod.applicable = true and target.org_id = ? and target.applicable = false and target.is_override != 'Y'";

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

		Object updparams[] = {varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff,
				varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff,
				newOrgId, baseOrgId, baseOrgId, newOrgId};
		Object insparams[] = {newOrgId, varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff, newOrgId,
				baseOrgId, baseOrgId};
		status = updateExclusions(con, UPDATE_EXCLUSIONS, newOrgId, baseOrgId);
		if (status) status = updateCharges(con, UPDATE_CHARGES, updparams);
		// postAuditEntry(con, "operation_charges_audit_log", userName, orgName);
		return status;
	}

    public boolean initRatePlan(Connection con, String newOrgId, String varianceType,
			Double varianceBy,String baseOrgId, Double roundOff, String userName, String orgName) throws Exception {
		boolean status = addOrgCodesForItems(con, newOrgId, baseOrgId, userName);
		if (status) status = addOrgForEquipments(con,newOrgId, varianceType,
				0.0, varianceBy, false, baseOrgId,
				roundOff, true);
		return status;
    }

    private static final String REINIT_EXCLUSIONS = "UPDATE equip_org_details as target " +
    " SET applicable = eod.applicable, base_rate_sheet_id = eod.org_id, " +
    " is_override = 'N' " +
    " FROM equip_org_details eod WHERE eod.equip_id = target.equip_id and " +
    " eod.org_id = ? and target.org_id = ? and target.is_override != 'Y'";

    public boolean reinitRatePlan(Connection con, String newOrgId, String varianceType,
			Double variance,String baseOrgId, Double rndOff,String userName, String orgName) throws Exception {
		boolean status = false;

		if(!varianceType.equals("Incr")) {
			variance = new Double(-variance);
		}

		BigDecimal varianceBy = new BigDecimal(variance);
        BigDecimal roundOff = new BigDecimal(rndOff);

		Object updparams[] = {varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff,
				varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff, newOrgId, baseOrgId};
		status = updateExclusions(con, REINIT_EXCLUSIONS, newOrgId, baseOrgId);
		if (status) status = updateCharges(con,UPDATE_RATEPLAN_EQUIP_CHARGES, updparams);
		return status;
    }

    private static String INIT_ITEM_ORG_DETAILS = "INSERT INTO equip_org_details " +
    "(equip_id, org_id, applicable, base_rate_sheet_id, is_override)" +
    " ( SELECT ?, od.org_id, true, prspv.base_rate_sheet_id, 'N' FROM organization_details od " +
    " LEFT OUTER JOIN priority_rate_sheet_parameters_view prspv ON od.org_id = prspv.org_id )";

    private static String INIT_ITEM_CHARGES = "INSERT INTO equipement_charges(equip_id,org_id,bed_type," +
    "daily_charge, min_charge, incr_charge)" +
    "(SELECT ?, abov.org_id, abov.bed_type, 0.0, 0.0, 0.0 FROM all_beds_orgs_view abov) ";

    public boolean initItemCharges(Connection con, String equipId, String userName) throws Exception {

        boolean status = false;
        //  disableAuditTriggers("service_master_charges", "z_services_charges_audit_trigger");
        status = initItemCharges(con, INIT_ITEM_ORG_DETAILS, INIT_ITEM_CHARGES, equipId, null); // no username field here
        // postAuditEntry(con, "service_master_charges_audit_log", userName, orgName);

        return status;
    }

	private static final String UPDATE_EQUIP_CHARGES_PLUS = "UPDATE equipement_charges totab SET " +
		" daily_charge = round(fromtab.daily_charge + ?), min_charge = round(fromtab.min_charge + ?)," +
		" incr_charge = round(fromtab.incr_charge + ?), slab_1_charge = round(fromtab.slab_1_charge + ?)," +
		" tax = round(fromtab.tax + ?)" +
		" FROM equipement_charges fromtab" +
		" WHERE totab.org_id = ? AND fromtab.org_id = ?" +
		" AND totab.equip_id = fromtab.equip_id AND totab.bed_type = fromtab.bed_type AND totab.is_override='N'";

	private static final String UPDATE_EQUIP_CHARGES_MINUS = "UPDATE equipement_charges totab SET " +
		" daily_charge = GREATEST(round(fromtab.daily_charge - ?), 0), min_charge = GREATEST(round(fromtab.min_charge - ?), 0)," +
		" incr_charge = GREATEST(round(fromtab.incr_charge - ?), 0), slab_1_charge = GREATEST(round(fromtab.slab_1_charge - ?), 0)," +
		" tax = GREATEST(round(fromtab.tax - ?), 0)" +
		" FROM equipement_charges fromtab" +
		" WHERE totab.org_id = ? AND fromtab.org_id = ?" +
		" AND totab.equip_id = fromtab.equip_id AND totab.bed_type = fromtab.bed_type AND totab.is_override='N'";

	private static final String UPDATE_EQUIP_CHARGES_BY = "UPDATE equipement_charges totab SET " +
		" daily_charge = doroundvarying(fromtab.daily_charge,?,?), min_charge = doroundvarying(fromtab.min_charge,?,?)," +
		" incr_charge = doroundvarying(fromtab.incr_charge,?,?), slab_1_charge = doroundvarying(fromtab.slab_1_charge,?,?)," +
		" tax = doroundvarying(fromtab.tax,?,?)" +
		" FROM equipement_charges fromtab" +
		" WHERE totab.org_id = ? AND fromtab.org_id = ?" +
		" AND totab.equip_id = fromtab.equip_id AND totab.bed_type = fromtab.bed_type AND totab.is_override='N'";

	public static boolean updateOrgForEquipments(Connection con,String orgId,String varianceType,
		Double varianceValue,Double varianceBy,boolean useValue,String baseOrgId,
		Double nearstRoundOfValue) throws SQLException, IOException {

			boolean status = false;
			PreparedStatement pstmt = null;

			if (useValue) {

			if (varianceType.equals("Incr"))
				pstmt = con.prepareStatement(UPDATE_EQUIP_CHARGES_PLUS);
			else
				pstmt = con.prepareStatement(UPDATE_EQUIP_CHARGES_MINUS);

			pstmt.setBigDecimal(1, new BigDecimal(varianceValue));
			pstmt.setBigDecimal(2, new BigDecimal(varianceValue));
			pstmt.setBigDecimal(3, new BigDecimal(varianceValue));
			pstmt.setBigDecimal(4, new BigDecimal(varianceValue));
			pstmt.setBigDecimal(5, new BigDecimal(varianceValue));
			pstmt.setString(6, orgId);
			pstmt.setString(7, baseOrgId);

			int i = pstmt.executeUpdate();
			if (i>=0) status = true;

			} else {

			pstmt = con.prepareStatement(UPDATE_EQUIP_CHARGES_BY);
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
			pstmt.setString(11, orgId);
			pstmt.setString(12, baseOrgId);

			int i = pstmt.executeUpdate();
			if (i>=0) status = true;

			}

			pstmt.close();

		return status;
	}

	private static final String COPY_EQUIP_DETAILS_TO_ALL_ORGS =
		" INSERT INTO equip_org_details (equip_id, org_id, applicable,username,mod_time) " +
		" SELECT eod.equip_id, o.org_id, eod.applicable,eod.username,eod.mod_time " +
		" FROM organization_details o " +
		" JOIN equip_org_details eod ON (eod.equip_id=? AND eod.org_id='ORG0001') " +
		" WHERE o.org_id != 'ORG0001'";


	public void copyEquipDetailsToAllOrgs(Connection con, String equipId) throws SQLException {
		PreparedStatement ps = con.prepareStatement(COPY_EQUIP_DETAILS_TO_ALL_ORGS);
		ps.setString(1, equipId);
		ps.executeUpdate();
		ps.close();
	}

	private static final String INSERT_ORG_DETAILS_FOR_EQUIPMENTS = "INSERT INTO equip_org_details " +
		"	SELECT equip_id, ?, applicable, ?, ?, ?, 'N'" +
		"	FROM equip_org_details WHERE org_id=?;";

	public static boolean addOrgCodesForEquipment(Connection con,String newOrgId,String varianceType,
		  Double varianceValue,Double varianceBy,boolean useValue,String baseOrgId,
		  Double nearstRoundOfValue, String userName) throws Exception{
			boolean status = false;
			PreparedStatement ps = null;
            BasicDynaBean obean = new OrgMasterDao().findByKey(con, "org_id", newOrgId);
            String rateSheetId = ("N".equals((String)obean.get("is_rate_sheet")) ? baseOrgId : null);
			try{
		   		ps = con.prepareStatement(INSERT_ORG_DETAILS_FOR_EQUIPMENTS);
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

	public boolean updateOrgForDerivedRatePlans(Connection con,String[] ratePlanIds, String[] applicable,
			String equipId) throws Exception{
		return updateOrgForDerivedRatePlans(con,ratePlanIds, applicable, "equip_org_details", "equipment",
				"equip_id", equipId);
	}

	public  boolean updateChargesForDerivedRatePlans(Connection con,String baseRateSheetId, String[] ratePlanIds,
	    		String[] bedType, Double[] dailyChg,Double[] minChg,
	    		Double[] incrChg,Double[] slabChg, String equipId,Double[] dailyDisc,Double[] minDisc,
	    		Double[] incrDisc,Double[] slabDisc,Double[] tax)throws Exception {
		boolean success = false;

		EquipmentChargeDAO opdao = new EquipmentChargeDAO();
		for(int i=0; i<ratePlanIds.length; i++) {
    		Map<String,Object> keys = new HashMap<String, Object>();
    		keys.put("base_rate_sheet_id", baseRateSheetId);
    		keys.put("org_id", ratePlanIds[i]);
    		BasicDynaBean bean = ratePlanParametersDAO.findByKey(keys);
    		int variation =(Integer)bean.get("rate_variation_percent");
    		int roundoff = (Integer)bean.get("round_off_amount");

    		List<BasicDynaBean> chargeList = new ArrayList();
    		boolean overrided = isChargeOverrided(con,ratePlanIds[i],"equip_id",equipId,"equipment","equip_org_details");
    		if(!overrided) {
	    		for(int k=0 ; k<bedType.length; k++) {
	    			BasicDynaBean charge = opdao.getBean();

	    			charge.set("equip_id", equipId);
	    			charge.set("org_id", ratePlanIds[i]);
	    			charge.set("bed_type", bedType[k]);

	    			Double dCharge = calculateCharge(dailyChg[k], new Double(variation), roundoff);
	    			Double mCharge = calculateCharge(minChg[k], new Double(variation), roundoff);
	    			Double iCharge = calculateCharge(incrChg[k], new Double(variation), roundoff);
	    			Double SCharge = calculateCharge(slabChg[k], new Double(variation), roundoff);

	    			Double dDisc = calculateCharge(dailyDisc[k], new Double(variation), roundoff);
	    			Double mDisc = calculateCharge(minDisc[k], new Double(variation), roundoff);
	    			Double iDisc = calculateCharge(incrDisc[k], new Double(variation), roundoff);
	    			Double SDisc = calculateCharge(slabDisc[k], new Double(variation), roundoff);

	    			charge.set("daily_charge", new BigDecimal(dCharge));
	    			charge.set("min_charge", new BigDecimal(mCharge));
	    			charge.set("incr_charge", new BigDecimal(iCharge));
	    			charge.set("slab_1_charge", new BigDecimal(SCharge));

	    			charge.set("daily_charge_discount", new BigDecimal(dDisc));
	    			charge.set("min_charge_discount", new BigDecimal(mDisc));
	    			charge.set("incr_charge_discount", new BigDecimal(iDisc));
	    			charge.set("slab_1_charge_discount", new BigDecimal(SDisc));
	    			charge.set("tax", new BigDecimal(tax[k]));

	    			chargeList.add(charge);
	    		}
    		}
    		for (BasicDynaBean c: chargeList) {
    			opdao.updateWithNames(con, c.getMap(),  new String[] {"equip_id", "org_id", "bed_type"});
    		}
    		success = true;
		}
		return success;
	}

	private static final String GET_DERIVED_RATE_PALN_DETAILS = "select rp.org_id,od.org_name, "+
		" case when rate_variation_percent<0 then 'Decrease By' else 'Increase By' end as discormarkup, "+
		" rate_variation_percent,round_off_amount,eod.applicable,eod.equip_id,rp.base_rate_sheet_id,eod.is_override "+
		" from rate_plan_parameters rp "+
		" join organization_details od on(od.org_id=rp.org_id) "+
		" join equip_org_details eod on (eod.org_id = rp.org_id) "+
		" where rp.base_rate_sheet_id =?  and equip_id=? and eod.base_rate_sheet_id=? ";

	public List<BasicDynaBean> getDerivedRatePlanDetails(String baseRateSheetId,String opId)throws SQLException {
		return getDerivedRatePlanDetails(baseRateSheetId, "equipment", opId,GET_DERIVED_RATE_PALN_DETAILS);
	}

	private static final String UPDATE_RATEPLAN_EQUIP_CHARGES = "UPDATE equipement_charges totab SET " +
	" daily_charge = doroundvarying(fromtab.daily_charge,?,?), min_charge = doroundvarying(fromtab.min_charge,?,?)," +
	" incr_charge = doroundvarying(fromtab.incr_charge,?,?), slab_1_charge = doroundvarying(fromtab.slab_1_charge,?,?)," +
	" daily_charge_discount = doroundvarying(fromtab.daily_charge_discount,?,?), "+
	" min_charge_discount = doroundvarying(fromtab.min_charge_discount,?,?), "+
	" slab_1_charge_discount = doroundvarying(fromtab.slab_1_charge_discount,?,?), "+
	" tax = fromtab.tax "+
	" FROM equipement_charges fromtab" +
	" WHERE totab.org_id = ? AND fromtab.org_id = ?" +
	" AND totab.equip_id = fromtab.equip_id AND totab.bed_type = fromtab.bed_type AND totab.is_override='N'";

	public boolean updateEquipChargesForDerivedRatePlans(String orgId,String varianceType,
		Double varianceValue,Double varianceBy,String baseOrgId,
		Double nearstRoundOfValue,String userName, String orgName,boolean upload) throws SQLException, Exception {

			boolean success = false;
			Connection con = null;
			try {
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);

				if(upload) {
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
				}else{
					BigDecimal variance = new BigDecimal(varianceBy);
					BigDecimal roundoff = new BigDecimal(nearstRoundOfValue);
					Object updparams[] = {variance, roundoff, variance, roundoff, variance, roundoff, variance, roundoff,
							variance, roundoff, variance, roundoff,variance, roundoff,variance, roundoff, orgId, baseOrgId, baseOrgId, orgId};
					success = updateCharges(con, UPDATE_CHARGES, updparams);
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

		Object updparams[] = {varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff,
				varianceBy, roundOff, varianceBy, roundOff, ratePlanId, rateSheetId,bedType};
		status = updateCharges(con,UPDATE_RATEPLAN_EQUIP_CHARGES + " AND totab.bed_type=? ", updparams);
		return status;
	}
}

