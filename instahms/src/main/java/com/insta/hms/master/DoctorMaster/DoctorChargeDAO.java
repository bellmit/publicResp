package com.insta.hms.master.DoctorMaster;

import com.bob.hms.adminmasters.organization.OrgMasterDao;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
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

public class DoctorChargeDAO extends ItemChargeDAO {

	static Logger logger = LoggerFactory.getLogger(DoctorChargeDAO.class);
	
	private static final GenericDAO ratePlanParameterDao = new GenericDAO("rate_plan_parameters");

	public DoctorChargeDAO() {
		super("doctor_consultation_charge");
	}

	/*
	 * Returns charges of all charge types for the given orgId, and list of Doctor IDs,
	 * for all bed types
	 *
	 * This is used for displaying the charges for each bed in the main list master screen
	 */

	private static final String GET_ALL_CHARGES_FOR_ORG =
		" SELECT doctor_name, bed_type, doctor_ip_charge,night_ip_charge,ward_ip_charge,"+
		"ot_charge,co_surgeon_charge," +
		" assnt_surgeon_charge,organization,doctor_ip_charge_discount,night_ip_charge_discount,ward_ip_charge_discount,ot_charge_discount" +
		" ,co_surgeon_charge_discount,assnt_surgeon_charge_discount " +
		" FROM doctor_consultation_charge " +
		" WHERE organization=?";

	public List<BasicDynaBean> getAllChargesForOrg(String orgId, List<String> ids) throws SQLException {

		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			StringBuilder query = new StringBuilder(GET_ALL_CHARGES_FOR_ORG);
			SearchQueryBuilder.addWhereFieldOpValue(true, query, "doctor_name", "IN", ids);

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
	 * Returns charges of all charge types for the given orgId, and a single Doctor ID,
	 * for all bed types
	 */
	public List<BasicDynaBean> getAllChargesForOrgDoctor(String orgId, String id) throws SQLException {
		List<String> ids = new ArrayList();
		ids.add(id);
		return getAllChargesForOrg(orgId, ids);
	}

	/*
	 * Copy the GENERAL rate plan charges to all other rate plans. Assumes that the other
	 * rate-plan charges are non-existent, ie, no updates only inserts new charges.
	 */
	private static final String COPY_GENERAL_CHARGES_TO_ALL_ORGS_IP =
		" update doctor_consultation_charge dc set doctor_ip_charge = a.doctor_ip_charge, " +
		" night_ip_charge = a.night_ip_charge, ward_ip_charge = a.ward_ip_charge, " +
		" ot_charge = a.ot_charge, co_surgeon_charge =a.co_surgeon_charge, assnt_surgeon_charge = a.assnt_surgeon_charge, " +
		" doctor_ip_charge_discount = a.doctor_ip_charge_discount, " +
		" night_ip_charge_discount = a.night_ip_charge_discount, " +
		" ward_ip_charge_discount = a.ward_ip_charge_discount, " +
		" ot_charge_discount = a.ot_charge_discount, co_surgeon_charge_discount = a.co_surgeon_charge_discount," +
		" assnt_surgeon_charge_discount = a.assnt_surgeon_charge_discount" +
		" FROM( SELECT doctor_name, d.bed_type, doctor_ip_charge, night_ip_charge, ward_ip_charge, dopc.organization,ot_charge, co_surgeon_charge, " +
		" assnt_surgeon_charge, doctor_ip_charge_discount, night_ip_charge_discount, ward_ip_charge_discount, ot_charge_discount, co_surgeon_charge_discount," +
		" assnt_surgeon_charge_discount from doctor_consultation_charge  dopc cross join all_beds_orgs_view d " +
		" where  dopc.bed_type = d.bed_type and d.org_id = dopc.organization and dopc.doctor_name =? and dopc.organization = 'ORG0001') as a " +
		" where dc.doctor_name = ? and dc.organization !='ORG0001'";
		public void copyGeneralChargesToAllOrgsIP(Connection con, String id) throws SQLException {
		PreparedStatement ps = con.prepareStatement(COPY_GENERAL_CHARGES_TO_ALL_ORGS_IP);
		ps.setString(1, id);
		ps.setString(2, id);
		ps.executeUpdate();
		ps.close();
	}

	/*
	 * Copy the GENERAL rate plan charges to all other rate plans. Assumes that the other
	 * rate-plan charges are non-existent, ie, no updates only inserts new charges.
	 */
	private static final String COPY_GENERAL_CHARGES_TO_ALL_ORGS_OP =
		"update doctor_op_consultation_charge dc set  op_charge = a.op_charge, op_revisit_charge = a.op_revisit_charge," +
		" private_cons_charge = a.private_cons_charge,private_cons_revisit_charge = a.private_cons_revisit_charge," +
		" op_charge_discount = a.op_charge_discount, op_revisit_discount = a.op_revisit_discount," +
		" private_cons_discount = a.private_cons_discount, private_revisit_discount = a.private_revisit_discount" +
		" from(SELECT doctor_id, od.org_id, op_charge, op_revisit_charge, private_cons_charge, private_cons_revisit_charge," +
		"  op_charge_discount, op_revisit_discount, private_cons_discount, private_revisit_discount " +
		" from doctor_op_consultation_charge  dc " +
		" cross join organization_details  od" +
		" where  od.org_id = dc.org_id and dc.doctor_id =? and dc.org_id = 'ORG0001') as a " +
		" where dc.doctor_id = ? and dc.org_id != 'ORG0001'";

	public void copyGeneralChargesToAllOrgsOP(Connection con, String id) throws SQLException {
		PreparedStatement ps = con.prepareStatement(COPY_GENERAL_CHARGES_TO_ALL_ORGS_OP);
		ps.setString(1, id);
		ps.setString(2, id);
		ps.executeUpdate();
		ps.close();
	}

	/*
	 * Copy the GENERAL bed type charges to all inactive bed types for the given Operation ID.
	 * These charges will not be inserted normally when the services is created, since
	 * only active bed charges are input from the user.
	 */
	private static final String COPY_GENERAL_CHARGES_TO_INACTIVE_BEDS =
		" update doctor_consultation_charge dc set doctor_ip_charge = a.doctor_ip_charge, night_ip_charge = a.night_ip_charge, ward_ip_charge = a.ward_ip_charge, " +
		" ot_charge = a.ot_charge, co_surgeon_charge =a.co_surgeon_charge, assnt_surgeon_charge = a.assnt_surgeon_charge, " +
		" doctor_ip_charge_discount = a.doctor_ip_charge_discount,night_ip_charge_discount = a.night_ip_charge_discount,ward_ip_charge_discount = a.ward_ip_charge_discount," +
		" ot_charge_discount = a.ot_charge_discount, co_surgeon_charge_discount = a.co_surgeon_charge_discount," +
		" assnt_surgeon_charge_discount = a.assnt_surgeon_charge_discount" +
		" FROM( SELECT doctor_name, d.bed_type, doctor_ip_charge, night_ip_charge, ward_ip_charge, dopc.organization,ot_charge, co_surgeon_charge, " +
		" assnt_surgeon_charge, doctor_ip_charge_discount, night_ip_charge_discount, ward_ip_charge_discount, ot_charge_discount, co_surgeon_charge_discount," +
		" assnt_surgeon_charge_discount from doctor_consultation_charge  dopc cross join all_beds_orgs_view d " +
		" where  dopc.bed_type = d.bed_type and d.org_id = dopc.organization and " +
		" d.bed_type in(SELECT DISTINCT bed_type FROM bed_details WHERE bed_status='I')" +
		" and dopc.doctor_name =? and dopc.organization = 'ORG0001') as a " +
		" where dc.doctor_name = ? and dc.organization !='ORG0001'";

	public void copyGeneralChargesToInactiveBeds(Connection con, String id) throws SQLException {
		PreparedStatement ps = con.prepareStatement(COPY_GENERAL_CHARGES_TO_INACTIVE_BEDS);
		ps.setString(1, id);
		ps.setString(2, id);
		ps.executeUpdate();
		ps.close();
	}

	/*
	 * Copy the GENERAL bed type charges to all inactive bed types for the given Operation ID.
	 * These charges will not be inserted normally when the services is created, since
	 * only active bed charges are input from the user.
	 */
	private static final String COPY_GENERAL_CHARGES_TO_INACTIVE_ICU_BEDS =
		" update doctor_consultation_charge dc set doctor_ip_charge = a.doctor_ip_charge, night_ip_charge = a.night_ip_charge,ward_ip_charge = a.ward_ip_charge," +
		" ot_charge = a.ot_charge, co_surgeon_charge =a.co_surgeon_charge, assnt_surgeon_charge = a.assnt_surgeon_charge, " +
		" doctor_ip_charge_discount = a.doctor_ip_charge_discount,night_ip_charge_discount = a.night_ip_charge_discount,ward_ip_charge_discount = a.ward_ip_charge_discount," +
		" ot_charge_discount = a.ot_charge_discount, co_surgeon_charge_discount = a.co_surgeon_charge_discount," +
		" assnt_surgeon_charge_discount = a.assnt_surgeon_charge_discount" +
		" FROM( SELECT doctor_name, d.bed_type, doctor_ip_charge, night_ip_charge, ward_ip_charge, dopc.organization,ot_charge, co_surgeon_charge, " +
		" assnt_surgeon_charge, doctor_ip_charge_discount, night_ip_charge_discount, ward_ip_charge_discount, ot_charge_discount, co_surgeon_charge_discount," +
		" assnt_surgeon_charge_discount from doctor_consultation_charge  dopc cross join all_beds_orgs_view d " +
		" where  dopc.bed_type = d.bed_type and d.org_id = dopc.organization and " +
		" d.bed_type in(select intensive_bed_type from  icu_bed_charges WHERE bed_status='I')" +
		" and dopc.doctor_name =? and dopc.organization = 'ORG0001') as a " +
		" where dc.doctor_name = ? and dc.organization !='ORG0001'";

	public void copyGeneralChargesToInactiveIcuBeds(Connection con, String id) throws SQLException {
		PreparedStatement ps = con.prepareStatement(COPY_GENERAL_CHARGES_TO_INACTIVE_ICU_BEDS);
		ps.setString(1, id);
		ps.setString(2, id);
		ps.executeUpdate();
		ps.close();
	}

	/*
	 * Return List of records, for particualr IP Charge Type
	 *
	 *  */
	public List<BasicDynaBean> getIPChargesForBedTypesXLS(String orgId, List<String> bedTypes, String chargeType)
		throws SQLException, IOException {

			Connection con = null;
			PreparedStatement ps = null;
			List<BasicDynaBean> list = null;
			StringBuilder query = new StringBuilder();

			query.append("SELECT d.doctor_name, dept.dept_name");
			int i = 1;
			for (String bedType: bedTypes) {
					query.append(", dc"+i+".").append(chargeType);
					query.append(" AS ").append(DataBaseUtil.quoteIdent(bedType, true));
				i++;
			}

			query.append(" FROM doctors d ");

			i = 1;
			String keysarray[] = new String[bedTypes.size()+2];
			keysarray[0] = "DOCTOR_ID";
			keysarray[1] = "DOCTOR_NAME";
			for (String bedType: bedTypes) {

				query.append(" JOIN doctor_consultation_charge dc"+i).append(" ON ");
				query.append("  dc"+i+".doctor_name=d.doctor_id AND ");
				query.append("  dc"+i+".bed_type=? AND");
				query.append("  dc"+i+".organization=? ");
				keysarray[i+1] = bedType+"/"+chargeType;
				i++;
			}
			query.append(" JOIN department dept ON(dept.dept_id = d.dept_id)");
			query.append(" WHERE d.status = 'A'");
			query.append(" order by d.doctor_id");

			try {
				con = DataBaseUtil.getReadOnlyConnection();
				ps = con.prepareStatement(query.toString());

				i=1;
				for (String bedType: bedTypes) {
					ps.setString(i++, bedType);
					ps.setString(i++, orgId);
				}

				list = DataBaseUtil.queryToDynaList(ps);

			} finally {
				DataBaseUtil.closeConnections(con, ps);
			}

		return list;
	}

	private static String GET_OP_CHARGES_XLS =
		"select d.doctor_name, dept.dept_name, op_charge,op_charge_discount,op_revisit_charge," +
		" op_revisit_discount,private_cons_charge,private_cons_discount,private_cons_revisit_charge,"+
		" private_revisit_discount,op_oddhr_charge,op_oddhr_charge_discount" +
		" FROM doctors d" +
		" JOIN doctor_op_consultation_charge  dopc on d.doctor_id = dopc.doctor_id" +
		" JOIN department dept ON(dept.dept_id = d.dept_id)" +
		" WHERE org_id=? AND d.status = 'A' order by d.doctor_id";
	/*
	 * Return List of records of all OP charge Types
	 *
	 *  */

	public List<BasicDynaBean> getOPChargesXLS(String orgId)
	throws SQLException, IOException {

		List list = DataBaseUtil.queryToDynaList(GET_OP_CHARGES_XLS, orgId);
		return list;

	}
	/*
	 * Backs up the current set of charges in a given rate plan into a backup table.
	 * To store a record of the "previous" state of the rate plan before updating via
	 * a CSV file upload.
	 */
	private static final String BACKUP_CHARGES =
		" INSERT INTO doctor_charges_backup (user_name, bkp_time, org_id, bed_type, doctor_id, " +
		"   doctor_ip_charge, night_ip_charge, ward_ip_charge, ot_charge,co_surgeon_charge,assnt_surgeon_charge, " +
		"	doctor_ip_charge_discount,night_ip_charge_discount,ward_ip_charge_discount,ot_charge_discount,co_surgeon_charge_discount," +
		"	assnt_surgeon_charge_discount) " +
		" SELECT ?, current_timestamp, organization, bed_type, doctor_name, " +
		"   doctor_ip_charge, night_ip_charge, ward_ip_charge, ot_charge,co_surgeon_charge,assnt_surgeon_charge, " +
		"	doctor_ip_charge_discount,night_ip_charge_discount,ward_ip_charge_discount,ot_charge_discount,co_surgeon_charge_discount," +
		"	assnt_surgeon_charge_discount" +
		" FROM doctor_consultation_charge WHERE organization=?" ;

	public void backupCharges(String orgId, String user) throws SQLException {
		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			PreparedStatement ps = con.prepareStatement(BACKUP_CHARGES);
			ps.setString(1, user);
			ps.setString(2, orgId);
			ps.execute();
		} finally {
			DataBaseUtil.closeConnections(con, pstmt);
		}
	}

	/*
	 * Backs up the current set of charges in a given rate plan into a backup table.
	 * To store a record of the "previous" state of the rate plan before updating via
	 * a CSV file upload.
	 */
	private static final String BACKUP_OP_CHARGES =
		" INSERT INTO doctor_charges_op_backup (user_name, bkp_time, org_id, doctor_id, " +
		"   op_charge,op_revisit_charge,private_cons_charge,private_cons_revisit_charge," +
		" 	op_charge_discount,op_revisit_discount,private_cons_discount,private_revisit_discount) " +
		" SELECT ?, current_timestamp,org_id,doctor_id,op_charge,op_revisit_charge,private_cons_charge,private_cons_revisit_charge, " +
		" 	op_charge_discount,op_revisit_discount,private_cons_discount,private_revisit_discount " +
		" FROM doctor_op_consultation_charge WHERE org_id=?" ;

	public void backupOPCharges(String orgId, String user) throws SQLException {
		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			pstmt = con.prepareStatement(BACKUP_OP_CHARGES);
			pstmt.setString(1, user);
			pstmt.setString(2, orgId);
			pstmt.execute();
		} finally {
			DataBaseUtil.closeConnections(con, pstmt);
		}
	}
	/*
	 * Update a list of charges in a batch. Note that large batches actually slow down the
	 * update, since addBatch causes a string to be reconstructed and this exponentially
	 * slows down when there is a large number of addBatch calls.
	 */

	public boolean updateChargeList(Connection con, List<BasicDynaBean> chargeList,String chargeType) throws SQLException {
		StringBuilder query = new StringBuilder();

		query.append("UPDATE doctor_consultation_charge  SET ");
		query.append(chargeType ).append("= ?");
		query.append(" WHERE doctor_name=? AND organization=? AND bed_type=?");

		PreparedStatement ps = con.prepareStatement(query.toString());

		for (BasicDynaBean c: chargeList) {
			int i=1;
			ps.setBigDecimal(i++, (BigDecimal)( c.get(chargeType)!=null?c.get(chargeType):new BigDecimal(0)));
			ps.setString(i++, (String) c.get("doctor_name"));
			ps.setString(i++, (String) c.get("organization"));
			ps.setString(i++, (String) c.get("bed_type"));
			ps.addBatch();
		}
		int results[] = ps.executeBatch();


		boolean status = DataBaseUtil.checkBatchUpdates(results);
		ps.close();
		return status;
	}


	private static final String UPDATE_OP_CHARGE =
		" UPDATE doctor_op_consultation_charge " +
		" SET op_charge = ? ,op_charge_discount = ?, op_revisit_charge = ?, op_revisit_discount = ?, " +
		" private_cons_charge = ?, private_cons_discount = ?, private_cons_revisit_charge = ?, " +
		" private_revisit_discount = ?, op_oddhr_charge=?, op_oddhr_charge_discount=? " +
		" WHERE doctor_id=? AND org_id=? ";

	public boolean updateOPChargeList(Connection con, List<BasicDynaBean> chargeList) throws SQLException {

		PreparedStatement ps = con.prepareStatement(UPDATE_OP_CHARGE);

		for (BasicDynaBean c: chargeList) {
			int i=1;
			ps.setBigDecimal(i++, (BigDecimal) c.get("op_charge"));
			ps.setBigDecimal(i++, (BigDecimal) c.get("op_charge_discount"));
			ps.setBigDecimal(i++, (BigDecimal) c.get("op_revisit_charge"));
			ps.setBigDecimal(i++, (BigDecimal) c.get("op_revisit_discount"));
			ps.setBigDecimal(i++, (BigDecimal) c.get("private_cons_charge"));
			ps.setBigDecimal(i++, (BigDecimal) c.get("private_cons_discount"));
			ps.setBigDecimal(i++, (BigDecimal) c.get("private_cons_revisit_charge"));
			ps.setBigDecimal(i++, (BigDecimal) c.get("private_revisit_discount"));
			ps.setBigDecimal(i++, (BigDecimal) c.get("op_oddhr_charge"));
			ps.setBigDecimal(i++, (BigDecimal) c.get("op_oddhr_charge_discount"));

			ps.setString(i++, (String) c.get("doctor_id"));
			ps.setString(i++, (String) c.get("org_id"));
			ps.addBatch();
		}

		int results[] = ps.executeBatch();
		boolean status = DataBaseUtil.checkBatchUpdates(results);
		ps.close();
		return status;
	}

	public void groupIncreaseCharges(Connection con, String orgId, String chargeType,
			List<String> bedTypes, List<String> ids, BigDecimal amount, boolean isPercentage,BigDecimal roundTo,String updateTable)
		throws SQLException {

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
	private String GROUP_INCR_CHARGES_OP =
		" UPDATE doctor_op_consultation_charge SET #=GREATEST( round((#+?)/?,0)*?, 0) " +
		" WHERE org_id=? ";

	private String GROUP_INCR_CHARGES_PERCENTAGE_OP =
		" UPDATE doctor_op_consultation_charge SET #=GREATEST( round(#*(100+?)/100/?,0)*?, 0) " +
		" WHERE org_id=? ";

	private String GROUP_INCR_CHARGES_IP =
		" UPDATE doctor_consultation_charge SET #=GREATEST( round((#+?)/?,0)*?, 0) " +
		" WHERE organization=? ";

	private String GROUP_INCR_CHARGES_PERCENTAGE_IP =
		" UPDATE doctor_consultation_charge SET #=GREATEST( round(#*(100+?)/100/?,0)*?, 0) " +
		" WHERE organization=? ";

	/*
	 * Discount
	 */
	private String GROUP_INCR_DISCOUNTS_OP =
		" UPDATE doctor_op_consultation_charge SET #=LEAST(GREATEST( round((#+?)/?,0)*?, 0), @) " +
		" WHERE org_id=? ";

	private String GROUP_INCR_DISCOUNTS_PERCENTAGE_OP =
		" UPDATE doctor_op_consultation_charge SET #=LEAST(GREATEST( round(#*(100+?)/100/?,0)*?, 0), @) " +
		" WHERE org_id=? ";

	private String GROUP_INCR_DISCOUNTS_IP =
		" UPDATE doctor_consultation_charge SET #=LEAST(GREATEST( round((#+?)/?,0)*?, 0), @) " +
		" WHERE organization=? ";

	private String GROUP_INCR_DISCOUNTS_PERCENTAGE_IP =
		" UPDATE doctor_consultation_charge SET #=LEAST(GREATEST( round(#*(100+?)/100/?,0)*?, 0), @) " +
		" WHERE organization=? ";


	/*
	 * Discount on charge
	 */
	private String GROUP_APPLY_DISCOUNTS_OP =
		" UPDATE doctor_op_consultation_charge SET @=LEAST(GREATEST( round((#+?)/?,0)*?, 0), #) " +
		" WHERE org_id=? ";

	private String GROUP_APPLY_DISCOUNTS_PERCENTAGE_OP =
		" UPDATE doctor_op_consultation_charge SET @= LEAST(GREATEST( round(#+(# * ?/100/?),0)*?, 0), #) " +
		" WHERE org_id=? ";

	private String GROUP_APPLY_DISCOUNTS_IP =
		" UPDATE doctor_consultation_charge SET @=LEAST(GREATEST( round((#+?)/?,0)*?, 0), #) " +
		" WHERE organization=? ";

	private String GROUP_APPLY_DISCOUNTS_PERCENTAGE_IP =
		" UPDATE doctor_consultation_charge SET @= LEAST(GREATEST( round(#+(# * ?/100/?),0)*?, 0), #) " +
		" WHERE organization=? ";


	public void groupIncreaseChargesWithRoundOff(Connection con, String orgId, String chargeType,
			List<String> bedTypes, List<String> ids, BigDecimal amount, boolean isPercentage,BigDecimal roundTo,String updateTable)
		throws SQLException {
		StringBuilder query = null;

		if(chargeType.equals("op_charge") || chargeType.equals("op_revisit_charge") || chargeType.equals("private_cons_charge")
				|| chargeType.equals("private_cons_revisit_charge")){

			if(updateTable !=null && updateTable.equals("UPDATECHARGE")) {
				 query = new StringBuilder(isPercentage ?
						 GROUP_INCR_CHARGES_PERCENTAGE_OP.replaceAll("#",DataBaseUtil.quoteIdent(chargeType)) :
							GROUP_INCR_CHARGES_OP.replaceAll("#",DataBaseUtil.quoteIdent(chargeType)) );
				 SearchQueryBuilder.addWhereFieldOpValue(true, query, "doctor_id", "IN", ids);

			}else if(updateTable.equals("UPDATEDISCOUNT")) {

				String chargeTypeDiscount = null;

				if(chargeType.equals("op_charge"))  chargeTypeDiscount = "op_charge_discount";
				if(chargeType.equals("op_revisit_charge"))  chargeTypeDiscount = "op_revisit_discount";
				if(chargeType.equals("private_cons_charge"))  chargeTypeDiscount = "private_cons_discount";
				if(chargeType.equals("private_cons_revisit_charge"))  chargeTypeDiscount = "private_revisit_discount";

				String updateDisAmount = GROUP_INCR_DISCOUNTS_OP.replaceAll("@",DataBaseUtil.quoteIdent(chargeType));
				String updateDisPercent = GROUP_INCR_DISCOUNTS_PERCENTAGE_OP.replaceAll("@",DataBaseUtil.quoteIdent(chargeType));

				query = new StringBuilder(isPercentage ?
						updateDisPercent.replaceAll("#",DataBaseUtil.quoteIdent(chargeTypeDiscount)) :
							updateDisAmount.replaceAll("#",DataBaseUtil.quoteIdent(chargeTypeDiscount)) );
				SearchQueryBuilder.addWhereFieldOpValue(true, query, "doctor_id", "IN", ids);

			}else {
				String chargeTypeDiscount = null;
				String updateDiscOnCharge = null;

				if(chargeType.equals("op_charge"))  chargeTypeDiscount = "op_charge_discount";
				if(chargeType.equals("op_revisit_charge"))  chargeTypeDiscount = "op_revisit_discount";
				if(chargeType.equals("private_cons_charge"))  chargeTypeDiscount = "private_cons_discount";
				if(chargeType.equals("private_cons_revisit_charge"))  chargeTypeDiscount = "private_revisit_discount";

				if(isPercentage) {
					updateDiscOnCharge = GROUP_APPLY_DISCOUNTS_PERCENTAGE_OP.replaceAll("@",DataBaseUtil.quoteIdent(chargeTypeDiscount));
					updateDiscOnCharge = updateDiscOnCharge.replaceAll("#",DataBaseUtil.quoteIdent(chargeType));
				}else {
					updateDiscOnCharge = GROUP_APPLY_DISCOUNTS_OP.replaceAll("@",DataBaseUtil.quoteIdent(chargeTypeDiscount));
					updateDiscOnCharge = updateDiscOnCharge.replaceAll("#",DataBaseUtil.quoteIdent(chargeType));
				}
				query = new StringBuilder(updateDiscOnCharge);
				SearchQueryBuilder.addWhereFieldOpValue(true, query, "doctor_id", "IN", ids);
			}

		}else{
			if(updateTable !=null && updateTable.equals("UPDATECHARGE")) {
				 query = new StringBuilder(isPercentage ?
						 GROUP_INCR_CHARGES_PERCENTAGE_IP.replaceAll("#",DataBaseUtil.quoteIdent(chargeType)) :
							GROUP_INCR_CHARGES_IP.replaceAll("#",DataBaseUtil.quoteIdent(chargeType)) );
				 SearchQueryBuilder.addWhereFieldOpValue(true, query, "bed_type", "IN", bedTypes);
				 SearchQueryBuilder.addWhereFieldOpValue(true, query, "doctor_name", "IN", ids);

			}else if(updateTable.equals("UPDATEDISCOUNT")) {

				String chargeTypeDiscount = chargeType+ "_discount";

				String updateDisPercent = GROUP_INCR_DISCOUNTS_PERCENTAGE_IP.replaceAll("@",DataBaseUtil.quoteIdent(chargeType));
				String updateDisAmount = GROUP_INCR_DISCOUNTS_IP.replaceAll("@",DataBaseUtil.quoteIdent(chargeType));

				query = new StringBuilder(isPercentage ?
						updateDisPercent.replaceAll("#",DataBaseUtil.quoteIdent(chargeTypeDiscount)) :
							updateDisAmount.replaceAll("#",DataBaseUtil.quoteIdent(chargeTypeDiscount)) );
				 SearchQueryBuilder.addWhereFieldOpValue(true, query, "bed_type", "IN", bedTypes);
				 SearchQueryBuilder.addWhereFieldOpValue(true, query, "doctor_name", "IN", ids);

			}else {
				String chargeTypeDiscount = chargeType+ "_discount";
				String updateDiscOnCharge= null;
				if(isPercentage) {
					updateDiscOnCharge = GROUP_APPLY_DISCOUNTS_PERCENTAGE_IP.replaceAll("@",DataBaseUtil.quoteIdent(chargeTypeDiscount));
					updateDiscOnCharge = updateDiscOnCharge.replaceAll("#",DataBaseUtil.quoteIdent(chargeType));
				}else {
					updateDiscOnCharge = GROUP_APPLY_DISCOUNTS_IP.replaceAll("@",DataBaseUtil.quoteIdent(chargeTypeDiscount));
					updateDiscOnCharge = updateDiscOnCharge.replaceAll("#",DataBaseUtil.quoteIdent(chargeType));
				}
				query = new StringBuilder(updateDiscOnCharge);
				SearchQueryBuilder.addWhereFieldOpValue(true, query, "bed_type", "IN", bedTypes);
				SearchQueryBuilder.addWhereFieldOpValue(true, query, "doctor_name", "IN", ids);
			}

		}

		PreparedStatement ps = con.prepareStatement(query.toString());

		int i=1;
		ps.setBigDecimal(i++, amount);
		ps.setBigDecimal(i++, roundTo);
		ps.setBigDecimal(i++, roundTo);		// roundTo appears twice in the query
		ps.setString(i++, orgId);

		if(!(chargeType.equals("op_charge") || chargeType.equals("op_revisit_charge") || chargeType.equals("private_cons_charge")
				|| chargeType.equals("private_cons_revisit_charge"))){
			if (bedTypes != null) for (String bedType : bedTypes) {
				ps.setString(i++, bedType);
			}
		}
		if (ids != null) for (String id : ids) {
			ps.setString(i++, id);
		}

		ps.executeUpdate();
		if (ps != null) ps.close();
	}


	private String GROUP_INCR_CHARGES_OP_NO_ROUNDOFF =
		" UPDATE doctor_op_consultation_charge SET # = GREATEST( # + ?, 0) " +
		" WHERE org_id=? ";

	private String GROUP_INCR_CHARGES_PERCENTAGE_OP_NO_ROUNDOFF =
		" UPDATE doctor_op_consultation_charge SET # = GREATEST(# +(( # * ? / 100 )) , 0) " +
		" WHERE org_id=? ";

	private String GROUP_INCR_CHARGES_IP_NO_ROUNDOFF =
		" UPDATE doctor_consultation_charge SET # = GREATEST( # + ?, 0) " +
		" WHERE organization=? ";

	private String GROUP_INCR_CHARGES_PERCENTAGE_IP_NO_ROUNDOFF =
		" UPDATE doctor_consultation_charge SET # = GREATEST(# +(( # * ? / 100 )) , 0) " +
		" WHERE organization=? ";

	/*
	 * Discount
	 */
	private String GROUP_INCR_DISCOUNTS_OP_NO_ROUNDOFF =
		" UPDATE doctor_op_consultation_charge SET # = LEAST(GREATEST( # + ?, 0), @) " +
		" WHERE org_id=? ";

	private String GROUP_INCR_DISCOUNTS_PERCENTAGE_OP_NO_ROUNDOFF =
		" UPDATE doctor_op_consultation_charge SET # = LEAST(GREATEST(# +( # * ? / 100 ) , 0), @) " +
		" WHERE org_id=? ";

	private String GROUP_INCR_DISCOUNTS_IP_NO_ROUNDOFF =
		" UPDATE doctor_consultation_charge SET # = LEAST(GREATEST( # + ?, 0), @) " +
		" WHERE organization=? ";

	private String GROUP_INCR_DISCOUNTS_PERCENTAGE_IP_NO_ROUNDOFF =
		" UPDATE doctor_consultation_charge SET # = LEAST(GREATEST(# +( # * ? / 100 ) , 0), @) " +
		" WHERE organization=? ";


	/*
	 * Discount on charge
	 */
	private String GROUP_APPLY_DISCOUNTS_OP_NO_ROUNDOFF =
		" UPDATE doctor_op_consultation_charge SET @ = LEAST(GREATEST( # + ?, 0), #) " +
		" WHERE org_id=? ";

	private String GROUP_APPLY_DISCOUNTS_PERCENTAGE_OP_NO_ROUNDOFF =
		" UPDATE doctor_op_consultation_charge SET @ = LEAST(GREATEST(# + ( # * ? / 100) , 0), #)" +
		" WHERE org_id=? ";

	private String GROUP_APPLY_DISCOUNTS_IP_NO_ROUNDOFF =
		" UPDATE doctor_consultation_charge SET @ = LEAST(GREATEST( # + ?, 0), #) " +
		" WHERE organization=? ";

	private String GROUP_APPLY_DISCOUNTS_PERCENTAGE_IP_NO_ROUNDOFF =
		" UPDATE doctor_consultation_charge SET @ = LEAST(GREATEST(# + ( # * ? / 100) , 0), #) " +
		" WHERE organization=? ";


	public void groupIncreaseChargesNoRoundOff(Connection con, String orgId, String chargeType,
			List<String> bedTypes, List<String> ids, BigDecimal amount,
			boolean isPercentage, String updateTable)
		throws SQLException {
		StringBuilder query = null;

		if(chargeType.equals("op_charge") || chargeType.equals("op_revisit_charge") || chargeType.equals("private_cons_charge")
				|| chargeType.equals("private_cons_revisit_charge")){

			if(updateTable !=null && updateTable.equals("UPDATECHARGE")) {
				 query = new StringBuilder(isPercentage ?
						 GROUP_INCR_CHARGES_PERCENTAGE_OP_NO_ROUNDOFF
						 .replaceAll("#",DataBaseUtil.quoteIdent(chargeType)):
						 GROUP_INCR_CHARGES_OP_NO_ROUNDOFF.replaceAll("#",DataBaseUtil.quoteIdent(chargeType)) );
				 SearchQueryBuilder.addWhereFieldOpValue(true, query, "doctor_id", "IN", ids);

			}else if(updateTable.equals("UPDATEDISCOUNT")) {

				String chargeTypeDiscount = null;

				if(chargeType.equals("op_charge"))  chargeTypeDiscount = "op_charge_discount";
				if(chargeType.equals("op_revisit_charge"))  chargeTypeDiscount = "op_revisit_discount";
				if(chargeType.equals("private_cons_charge"))  chargeTypeDiscount = "private_cons_discount";
				if(chargeType.equals("private_cons_revisit_charge"))  chargeTypeDiscount = "private_revisit_discount";

				String updateDisAmount = GROUP_INCR_DISCOUNTS_OP_NO_ROUNDOFF.replaceAll("@",DataBaseUtil.quoteIdent(chargeType));
				String updateDisPercent = GROUP_INCR_DISCOUNTS_PERCENTAGE_OP_NO_ROUNDOFF.replaceAll("@",DataBaseUtil.quoteIdent(chargeType));

				query = new StringBuilder(isPercentage ?
						updateDisPercent
						.replaceAll("#",DataBaseUtil.quoteIdent(chargeTypeDiscount)):
						updateDisAmount.replaceAll("#",DataBaseUtil.quoteIdent(chargeTypeDiscount)) );
				SearchQueryBuilder.addWhereFieldOpValue(true, query, "doctor_id", "IN", ids);

			}else {
				String chargeTypeDiscount = null;
				String updateDiscOnCharge = null;

				if(chargeType.equals("op_charge"))  chargeTypeDiscount = "op_charge_discount";
				if(chargeType.equals("op_revisit_charge"))  chargeTypeDiscount = "op_revisit_discount";
				if(chargeType.equals("private_cons_charge"))  chargeTypeDiscount = "private_cons_discount";
				if(chargeType.equals("private_cons_revisit_charge"))  chargeTypeDiscount = "private_revisit_discount";

				if(isPercentage) {
					updateDiscOnCharge = GROUP_APPLY_DISCOUNTS_PERCENTAGE_OP_NO_ROUNDOFF.replaceAll("@",DataBaseUtil.quoteIdent(chargeTypeDiscount));
					updateDiscOnCharge = updateDiscOnCharge.replaceAll("#",DataBaseUtil.quoteIdent(chargeType));
				}else {
					updateDiscOnCharge = GROUP_APPLY_DISCOUNTS_OP_NO_ROUNDOFF.replaceAll("@",DataBaseUtil.quoteIdent(chargeTypeDiscount));
					updateDiscOnCharge = updateDiscOnCharge.replaceAll("#",DataBaseUtil.quoteIdent(chargeType));
				}
				query = new StringBuilder(updateDiscOnCharge);
				SearchQueryBuilder.addWhereFieldOpValue(true, query, "doctor_id", "IN", ids);
			}

		}else{
			if(updateTable !=null && updateTable.equals("UPDATECHARGE")) {
				 query = new StringBuilder(isPercentage ?
						 GROUP_INCR_CHARGES_PERCENTAGE_IP_NO_ROUNDOFF
						 .replaceAll("#",DataBaseUtil.quoteIdent(chargeType)):
						GROUP_INCR_CHARGES_IP_NO_ROUNDOFF.replaceAll("#",DataBaseUtil.quoteIdent(chargeType)) );
				 SearchQueryBuilder.addWhereFieldOpValue(true, query, "bed_type", "IN", bedTypes);
				 SearchQueryBuilder.addWhereFieldOpValue(true, query, "doctor_name", "IN", ids);

			}else if(updateTable.equals("UPDATEDISCOUNT")) {

				String chargeTypeDiscount = chargeType+ "_discount";

				String updateDisPercent = GROUP_INCR_DISCOUNTS_PERCENTAGE_IP_NO_ROUNDOFF.replaceAll("@",DataBaseUtil.quoteIdent(chargeType));
				String updateDisAmount = GROUP_INCR_DISCOUNTS_IP_NO_ROUNDOFF.replaceAll("@",DataBaseUtil.quoteIdent(chargeType));

				query = new StringBuilder(isPercentage ?
						updateDisPercent
						.replaceAll("#",DataBaseUtil.quoteIdent(chargeTypeDiscount)):
						updateDisAmount.replaceAll("#",DataBaseUtil.quoteIdent(chargeTypeDiscount)) );
				 SearchQueryBuilder.addWhereFieldOpValue(true, query, "bed_type", "IN", bedTypes);
				 SearchQueryBuilder.addWhereFieldOpValue(true, query, "doctor_name", "IN", ids);

			}else {
				String chargeTypeDiscount = chargeType+ "_discount";
				String updateDiscOnCharge= null;
				if(isPercentage) {
					updateDiscOnCharge = GROUP_APPLY_DISCOUNTS_PERCENTAGE_IP_NO_ROUNDOFF.replaceAll("@",DataBaseUtil.quoteIdent(chargeTypeDiscount));
					updateDiscOnCharge = updateDiscOnCharge.replaceAll("#",DataBaseUtil.quoteIdent(chargeType));
				}else {
					updateDiscOnCharge = GROUP_APPLY_DISCOUNTS_IP_NO_ROUNDOFF.replaceAll("@",DataBaseUtil.quoteIdent(chargeTypeDiscount));
					updateDiscOnCharge = updateDiscOnCharge.replaceAll("#",DataBaseUtil.quoteIdent(chargeType));
				}
				query = new StringBuilder(updateDiscOnCharge);
				SearchQueryBuilder.addWhereFieldOpValue(true, query, "bed_type", "IN", bedTypes);
				SearchQueryBuilder.addWhereFieldOpValue(true, query, "doctor_name", "IN", ids);
			}

		}

		PreparedStatement ps = con.prepareStatement(query.toString());

		int i=1;
		ps.setBigDecimal(i++, amount);


		ps.setString(i++, orgId);

		if(!(chargeType.equals("op_charge") || chargeType.equals("op_revisit_charge") || chargeType.equals("private_cons_charge")
				|| chargeType.equals("private_cons_revisit_charge"))){
			if (bedTypes != null) for (String bedType : bedTypes) {
				ps.setString(i++, bedType);
			}
		}
		if (ids != null) for (String id : ids) {
			ps.setString(i++, id);
		}

		ps.executeUpdate();
		if (ps != null) ps.close();
	}

	private static String INSERT_ZERO_CHARGES_TO_ALLORGS_IP="INSERT INTO doctor_consultation_charge select ?,bed_type,0.00,0.00,org_id,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00 from all_beds_orgs_view";
	public void insertZeroChargesToAllOrgsIP(Connection con, String id) throws SQLException {
		PreparedStatement ps = con.prepareStatement(INSERT_ZERO_CHARGES_TO_ALLORGS_IP);
		ps.setString(1, id);
		ps.executeUpdate();
		ps.close();
	}

	private static String INSERT_ZERO_CHARGES_TO_ALLORGS_OP="INSERT INTO doctor_op_consultation_charge select ?,org_id,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00 from organization_details";
	public void insertZeroChargesToAllOrgsOP(Connection con, String id) throws SQLException {
		PreparedStatement ps = con.prepareStatement(INSERT_ZERO_CHARGES_TO_ALLORGS_OP);
		ps.setString(1, id);
		ps.executeUpdate();
		ps.close();
	}

	private static String INSERT_ZERO_CHARGES_INACTIVE_ICUBEDS="INSERT INTO doctor_consultation_charge" +
			" select ?,abo.bed_type,0.00,0.00,org_id,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00 " +
			" FROM all_beds_orgs_view abo " +
			" WHERE bed_type in(select intensive_bed_type from  icu_bed_charges WHERE bed_status='I')";
	public void insertZeroChargesToInactiveIcuBeds(Connection con, String id) throws SQLException {
		PreparedStatement ps = con.prepareStatement(INSERT_ZERO_CHARGES_INACTIVE_ICUBEDS);
		ps.setString(1, id);
		ps.executeUpdate();
		ps.close();
	}

	private static String INSERT_ZERO_CHARGES_INACTIVE_BEDS="INSERT INTO doctor_consultation_charge" +
	" select ?,abo.bed_type,0.00,0.00,org_id,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00 " +
	" FROM all_beds_orgs_view abo " +
	" WHERE bed_type in(SELECT DISTINCT bed_type FROM bed_details WHERE bed_status='I')";
	public void insertZeroChargesToInactiveBeds(Connection con, String id) throws SQLException {
		PreparedStatement ps = con.prepareStatement(INSERT_ZERO_CHARGES_INACTIVE_BEDS);
		ps.setString(1, id);
		ps.executeUpdate();
		ps.close();
	}

	private static final String INSERT_INTO_DOCTORS_PLUS = "INSERT INTO doctor_consultation_charge(doctor_name,bed_type,doctor_ip_charge,night_ip_charge,ward_ip_charge," +
		"organization,ot_charge,co_surgeon_charge,assnt_surgeon_charge)(SELECT doctor_name,bed_type,round(doctor_ip_charge+?),round(night_ip_charge + ?),round(ward_ip_charge + ?)," +
		"?,round(ot_charge + ?),round(co_surgeon_charge + ?),round(assnt_surgeon_charge + ?) FROM" +
		" doctor_consultation_charge WHERE  organization = ?)";

	private static final String INSERT_INTO_DOCTORS_MINUS = "INSERT INTO doctor_consultation_charge(doctor_name,bed_type,doctor_ip_charge,night_ip_charge,ward_ip_charge," +
		"organization,ot_charge,co_surgeon_charge,assnt_surgeon_charge)(SELECT doctor_name,bed_type,GREATEST(round(doctor_ip_charge - ?), 0), " +
		"GREATEST(round(night_ip_charge - ?), 0), GREATEST(round(ward_ip_charge - ?), 0)," +
		"?, GREATEST(round(ot_charge - ?), 0), GREATEST(round(co_surgeon_charge - ?), 0), GREATEST(round(assnt_surgeon_charge - ?), 0) FROM" +
		" doctor_consultation_charge WHERE  organization = ?)";

	private static final String INSER_INTO_DOCTORS_BY = "INSERT INTO doctor_consultation_charge(doctor_name,bed_type,organization," +
		"doctor_ip_charge,night_ip_charge,ward_ip_charge,ot_charge,co_surgeon_charge,assnt_surgeon_charge)" +
		"(SELECT doctor_name,bed_type,?," +
		"doroundvarying(doctor_ip_charge,?,?),doroundvarying(night_ip_charge,?,?),doroundvarying(ward_ip_charge,?,?)," +
		"doroundvarying(ot_charge,?,?),doroundvarying(co_surgeon_charge,?,?),doroundvarying(assnt_surgeon_charge,?,?) FROM " +
		"doctor_consultation_charge WHERE  organization = ?)";

	private static final String INSER_INTO_DOCTORS_WITH_DISCOUNTS_BY = "INSERT INTO doctor_consultation_charge(doctor_name,bed_type,organization," +
	"doctor_ip_charge,night_ip_charge,ward_ip_charge,ot_charge,co_surgeon_charge,assnt_surgeon_charge," +
	"doctor_ip_charge_discount,night_ip_charge_discount,ward_ip_charge_discount,ot_charge_discount,co_surgeon_charge_discount,assnt_surgeon_charge_discount)" +
	"(SELECT doctor_name,bed_type,?," +
	"doroundvarying(doctor_ip_charge,?,?),doroundvarying(night_ip_charge,?,?),doroundvarying(ward_ip_charge,?,?)," +
	"doroundvarying(ot_charge,?,?),doroundvarying(co_surgeon_charge,?,?),doroundvarying(assnt_surgeon_charge,?,?)," +
	"doroundvarying(doctor_ip_charge_discount,?,?),doroundvarying(night_ip_charge_discount,?,?),doroundvarying(ward_ip_charge_discount,?,?)," +
	"doroundvarying(ot_charge_discount,?,?),doroundvarying(co_surgeon_charge_discount,?,?),doroundvarying(assnt_surgeon_charge_discount,?,?)" +
	" FROM doctor_consultation_charge WHERE  organization = ?)";;

	public static boolean addOrgFordoctors(Connection con,String newOrgId,String varianceType,
			Double varianceValue,Double varianceBy,boolean useValue,String baseOrgId,Double nearstRoundOfValue) throws Exception{
		return addOrgFordoctors(con,newOrgId,varianceType,
				varianceValue,varianceBy,useValue,baseOrgId,nearstRoundOfValue, false);
	}

	public static boolean addOrgFordoctors(Connection con,String newOrgId,String varianceType,
			Double varianceValue,Double varianceBy,boolean useValue,String baseOrgId,Double nearstRoundOfValue,
			boolean updateDiscounts) throws Exception{
			boolean status = false;
			PreparedStatement ps = null;
			if(useValue){
				if(varianceType.equals("Incr")){
					ps = con.prepareStatement(INSERT_INTO_DOCTORS_PLUS);
					ps.setDouble(1,varianceValue);
					ps.setDouble(2,varianceValue);
					ps.setDouble(3,varianceValue);
					ps.setString(4,newOrgId);
					ps.setDouble(5,varianceValue );
					ps.setDouble(6,varianceValue);
					ps.setDouble(7,varianceValue);
					ps.setString(8,baseOrgId);

					int i = ps.executeUpdate();
					logger.debug(Integer.toString(i));
					if(i>=0)status = true;

				}else{
					ps = con.prepareStatement(INSERT_INTO_DOCTORS_MINUS);

					ps.setDouble(1,varianceValue );
					ps.setDouble(2,varianceValue);
					ps.setDouble(3,varianceValue);
					ps.setString(4,newOrgId);
					ps.setDouble(5,varianceValue);
					ps.setDouble(6,varianceValue);
					ps.setDouble(7,varianceValue);
					ps.setString(8,baseOrgId);

					int i = ps.executeUpdate();
					logger.debug(Integer.toString(i));
					if(i>=0)status = true;

				}

			}else{
				if(!varianceType.equals("Incr")){
				 	varianceBy = new Double(-varianceBy);
				}
				/*
				ps = con.prepareStatement(INSER_INTO_DOCTORS_BY);
				ps.setBigDecimal(1, new BigDecimal(varianceBy));
				ps.setBigDecimal(2, new BigDecimal(nearstRoundOfValue));
				ps.setBigDecimal(3, new BigDecimal(varianceBy));
				ps.setBigDecimal(4, new BigDecimal(nearstRoundOfValue));
				ps.setBigDecimal(5, new BigDecimal(varianceBy));
				ps.setBigDecimal(6, new BigDecimal(nearstRoundOfValue));
				ps.setString(7, newOrgId);
				ps.setBigDecimal(8, new BigDecimal(varianceBy));
				ps.setBigDecimal(9, new BigDecimal(nearstRoundOfValue));
				ps.setBigDecimal(10, new BigDecimal(varianceBy));
				ps.setBigDecimal(11, new BigDecimal(nearstRoundOfValue));
				ps.setBigDecimal(12, new BigDecimal(varianceBy));
				ps.setBigDecimal(13, new BigDecimal(nearstRoundOfValue));
				ps.setString(14,baseOrgId);

				int i = ps.executeUpdate();*/
				int i = insertChargesByPercent(con, INSER_INTO_DOCTORS_BY, INSER_INTO_DOCTORS_WITH_DISCOUNTS_BY, newOrgId,
						baseOrgId, varianceBy, nearstRoundOfValue, updateDiscounts, 6);
				logger.debug(Integer.toString(i));
				if(i>=0)status = true;


			}
			if (null != ps) ps.close();

		return status;
	}


	private static final String INSERT_OP_CHARGES_PLUS= "INSERT INTO doctor_op_consultation_charge(doctor_id,org_id,op_charge," +
		"op_revisit_charge, private_cons_charge, private_cons_revisit_charge, op_oddhr_charge)" +
		"(SELECT doctor_id,?,round(op_charge + ?),round(op_revisit_charge + ?), round(private_cons_charge + ?), " +
		"round(private_cons_revisit_charge + ?), round(op_oddhr_charge + ?) FROM doctor_op_consultation_charge WHERE" +
		" org_id=?)";

	private static final String INSERT_OP_CHARGES_MINUS= "INSERT INTO doctor_op_consultation_charge(doctor_id,org_id,op_charge," +
			"op_revisit_charge, private_cons_charge, private_cons_revisit_charge, op_oddhr_charge)" +
		"(SELECT doctor_id,?, GREATEST(round(op_charge - ?), 0), GREATEST(round(op_revisit_charge - ?), 0), GREATEST(round(private_cons_charge - ?), 0)," +
		"GREATEST(round(private_cons_revisit_charge - ?), 0), GREATEST(round(op_oddhr_charge - ?), 0) FROM doctor_op_consultation_charge WHERE" +
		" org_id=?)";

	private static final String INSERT_OP_CHARGES_BY = "INSERT INTO doctor_op_consultation_charge(doctor_id,org_id,op_charge,op_revisit_charge," +
			" private_cons_charge, private_cons_revisit_charge, op_oddhr_charge)" +
		"(SELECT doctor_id,?,doroundvarying(op_charge,?, ?),doroundvarying(op_revisit_charge,?,?)," +
		"doroundvarying(private_cons_charge,?, ?),doroundvarying(private_cons_revisit_charge,?,?)," +
		"doroundvarying(op_oddhr_charge,?,?) FROM doctor_op_consultation_charge WHERE" +
		" org_id=?)";

	private static final String INSERT_OP_WITH_DISCOUNTS_BY = "INSERT INTO doctor_op_consultation_charge(doctor_id,org_id," +
	" op_charge,op_revisit_charge, private_cons_charge, private_cons_revisit_charge, op_oddhr_charge," +
	" op_charge_discount,op_revisit_discount, private_cons_discount, private_revisit_discount, op_oddhr_charge_discount" +
	" )" +
	" (SELECT doctor_id,?," +
	" doroundvarying(op_charge,?, ?),doroundvarying(op_revisit_charge,?,?),doroundvarying(private_cons_charge,?, ?)," +
	" doroundvarying(private_cons_revisit_charge,?,?),doroundvarying(op_oddhr_charge,?,?), " +
	" doroundvarying(op_charge_discount,?, ?),doroundvarying(op_revisit_discount,?,?),doroundvarying(private_cons_discount,?, ?)," +
	" doroundvarying(private_revisit_discount,?,?),doroundvarying(op_oddhr_charge_discount,?,?) " +
	" FROM doctor_op_consultation_charge WHERE" +
	" org_id=?)";

	public static boolean addOrgForOpCharges(Connection con,String newOrgId,String varianceType,
			Double varianceValue,Double varianceBy,boolean useValue,String baseOrgId,
			Double nearstRoundOfValue)throws Exception{
		return addOrgForOpCharges(con,newOrgId,varianceType,
				varianceValue,varianceBy,useValue,baseOrgId, nearstRoundOfValue, false);
	}

	public static boolean addOrgForOpCharges(Connection con,String newOrgId,String varianceType,
			Double varianceValue,Double varianceBy,boolean useValue,String baseOrgId,
			Double nearstRoundOfValue, boolean updateDiscounts)throws Exception{

			boolean status = false;
			PreparedStatement ps = null;
			if(useValue){
				if(varianceType.equals("Incr")){
					ps = con.prepareStatement(INSERT_OP_CHARGES_PLUS);
				}else{
					ps = con.prepareStatement(INSERT_OP_CHARGES_MINUS);
				}

				ps.setString(1,newOrgId);
				ps.setBigDecimal(2,new BigDecimal(varianceValue));
				ps.setBigDecimal(3,new BigDecimal(varianceValue));
				ps.setBigDecimal(4,new BigDecimal(varianceValue));
				ps.setBigDecimal(5,new BigDecimal(varianceValue));
				ps.setBigDecimal(6,new BigDecimal(varianceValue));
				ps.setString(7, baseOrgId);

				int i = ps.executeUpdate();
				if(i>=0)status = true;

			}else{
				//round varying
				if(!varianceType.equals("Incr")){
				 	varianceBy = new Double(-varianceBy);
				}

				/*ps = con.prepareStatement(INSERT_OP_CHARGES_BY);
				ps.setString(1,newOrgId);
				ps.setBigDecimal(2, new BigDecimal(varianceBy));
				ps.setBigDecimal(3, new BigDecimal(varianceValue));
				ps.setBigDecimal(4, new BigDecimal(varianceBy));
				ps.setBigDecimal(5, new BigDecimal(varianceValue));
				ps.setBigDecimal(6, new BigDecimal(varianceBy));
				ps.setBigDecimal(7, new BigDecimal(varianceValue));
				ps.setBigDecimal(8, new BigDecimal(varianceBy));
				ps.setBigDecimal(9, new BigDecimal(varianceValue));
				ps.setBigDecimal(10, new BigDecimal(varianceBy));
				ps.setBigDecimal(11, new BigDecimal(varianceValue));

				ps.setString(12, baseOrgId);

				int i = ps.executeUpdate(); */
				int i = insertChargesByPercent(con, INSERT_OP_CHARGES_BY, INSERT_OP_WITH_DISCOUNTS_BY, newOrgId,
							baseOrgId, varianceBy, nearstRoundOfValue, updateDiscounts, 5);
				logger.debug(Integer.toString(i));
				if(i>=0)status = true;

			}


			if (null != ps) ps.close();

		return status;
	}

	private static int insertChargesByPercent(Connection con, String chargesOnlyQuery, String chargesNdiscountsQuery,
			String newOrgId, String baseOrgId, Double varianceBy,
			Double nearstRoundOfValue, boolean updateDiscounts, int numCharges) throws Exception {

		int ndx = 1;

		PreparedStatement pstmt = null;
		try {
			pstmt = con.prepareStatement(updateDiscounts ?
					chargesNdiscountsQuery : chargesOnlyQuery);
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

    private static String INIT_ITEM_ORG_DETAILS = "INSERT INTO doctor_org_details " +
    "(doctor_id, org_id, applicable, base_rate_sheet_id, is_override)" +
    " ( SELECT ?, od.org_id, true, prspv.base_rate_sheet_id, 'N' FROM organization_details od " +
    " LEFT OUTER JOIN priority_rate_sheet_parameters_view prspv ON od.org_id = prspv.org_id )";

	public boolean initItemCharges(Connection con, String doctorId, String userName) throws Exception {

        boolean status = true;
        //  disableAuditTriggers("service_master_charges", "z_services_charges_audit_trigger");
        insertZeroChargesToAllOrgsIP(con, doctorId);
        insertZeroChargesToAllOrgsOP(con, doctorId);
        initItemCharges(con, INIT_ITEM_ORG_DETAILS, null, doctorId, null);
        // postAuditEntry(con, "service_master_charges_audit_log", userName, orgName);

        return status;
    }

	private static final String UPDATE_DOCCONSCHG_PLUS = "UPDATE doctor_consultation_charge totab SET " +
		" doctor_ip_charge = round(fromtab.doctor_ip_charge + ?), night_ip_charge = round(fromtab.night_ip_charge + ?)," +
		" ot_charge = round(fromtab.ot_charge + ?), co_surgeon_charge = round(fromtab.co_surgeon_charge + ?)," +
		" assnt_surgeon_charge = round(fromtab.assnt_surgeon_charge + ?), ward_ip_charge = round(fromtab.ward_ip_charge + ?)" +
		" FROM doctor_consultation_charge fromtab" +
		" WHERE totab.organization = ? AND fromtab.organization = ?" +
		" AND totab.doctor_name = fromtab.doctor_name AND totab.bed_type = fromtab.bed_type AND totab.is_override='N'";

	private static final String UPDATE_DOCCONSCHG_MINUS = "UPDATE doctor_consultation_charge totab SET " +
		" doctor_ip_charge = GREATEST(round(fromtab.doctor_ip_charge - ?), 0), night_ip_charge = GREATEST(round(fromtab.night_ip_charge - ?), 0)," +
		" ot_charge = GREATEST(round(fromtab.ot_charge - ?), 0), co_surgeon_charge = GREATEST(round(fromtab.co_surgeon_charge - ?), 0)," +
		" assnt_surgeon_charge = GREATEST(round(fromtab.assnt_surgeon_charge - ?), 0), ward_ip_charge = GREATEST(round(fromtab.ward_ip_charge - ?), 0)" +
		" FROM doctor_consultation_charge fromtab" +
		" WHERE totab.organization = ? AND fromtab.organization = ?" +
		" AND totab.doctor_name = fromtab.doctor_name AND totab.bed_type = fromtab.bed_type AND totab.is_override='N'";

	private static final String UPDATE_DOCCONSCHG_BY = "UPDATE doctor_consultation_charge totab SET " +
		" doctor_ip_charge = doroundvarying(fromtab.doctor_ip_charge,?,?), night_ip_charge = doroundvarying(fromtab.night_ip_charge,?,?)," +
		" ot_charge = doroundvarying(fromtab.ot_charge,?,?), co_surgeon_charge = doroundvarying(fromtab.co_surgeon_charge,?,?)," +
		" assnt_surgeon_charge = doroundvarying(fromtab.assnt_surgeon_charge,?,?), ward_ip_charge = doroundvarying(fromtab.ward_ip_charge,?,?)" +
		" FROM doctor_consultation_charge fromtab" +
		" WHERE totab.organization = ? AND fromtab.organization = ?" +
		" AND totab.doctor_name = fromtab.doctor_name AND totab.bed_type = fromtab.bed_type AND totab.is_override='N'";

	public static boolean updateOrgForDrConstCharge(Connection con,String orgId,String varianceType,
			Double varianceValue,Double varianceBy,boolean useValue,String baseOrgId,
			Double nearstRoundOfValue) throws SQLException, IOException {

		boolean status = false;
		PreparedStatement pstmt = null;

		if (useValue) {

			if (varianceType.equals("Incr"))
				pstmt = con.prepareStatement(UPDATE_DOCCONSCHG_PLUS);
			else
				pstmt = con.prepareStatement(UPDATE_DOCCONSCHG_MINUS);

			pstmt.setBigDecimal(1, new BigDecimal(varianceValue));
			pstmt.setBigDecimal(2, new BigDecimal(varianceValue));
			pstmt.setBigDecimal(3, new BigDecimal(varianceValue));
			pstmt.setBigDecimal(4, new BigDecimal(varianceValue));
			pstmt.setBigDecimal(5, new BigDecimal(varianceValue));
			pstmt.setBigDecimal(6, new BigDecimal(varianceValue));
			pstmt.setString(7, orgId);
			pstmt.setString(8, baseOrgId);

			int i = pstmt.executeUpdate();
			if (i>=0) status = true;

		} else {

			pstmt = con.prepareStatement(UPDATE_DOCCONSCHG_BY);
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
			pstmt.setString(13, orgId);
			pstmt.setString(14, baseOrgId);

			int i = pstmt.executeUpdate();
			if (i>=0) status = true;

		}
		pstmt.close();

		return status;
	}


	private static final String UPDATE_DR_OP_CONSCHARGE_PLUS = "UPDATE doctor_op_consultation_charge totab SET " +
		" op_charge = round(fromtab.op_charge + ?), op_revisit_charge = round(fromtab.op_revisit_charge + ?)," +
		" private_cons_charge = round(fromtab.private_cons_charge + ?), private_cons_revisit_charge = round(fromtab.private_cons_revisit_charge + ?)," +
		" op_oddhr_charge = round(fromtab.op_oddhr_charge + ?)" +
		" FROM doctor_op_consultation_charge fromtab" +
		" WHERE totab.org_id = ? AND fromtab.org_id = ?" +
		" AND totab.doctor_id = fromtab.doctor_id AND totab.is_override='N'";

	private static final String UPDATE_DR_OP_CONSCHARGE_MINUS = "UPDATE doctor_op_consultation_charge totab SET " +
		" op_charge = GREATEST(round(fromtab.op_charge - ?), 0), op_revisit_charge = GREATEST(round(fromtab.op_revisit_charge - ?), 0)," +
		" private_cons_charge = GREATEST(round(fromtab.private_cons_charge - ?), 0), private_cons_revisit_charge = GREATEST(round(fromtab.private_cons_revisit_charge - ?), 0)," +
		" op_oddhr_charge = GREATEST(round(fromtab.op_oddhr_charge - ?), 0)" +
		" FROM doctor_op_consultation_charge fromtab" +
		" WHERE totab.org_id = ? AND fromtab.org_id = ?" +
		" AND totab.doctor_id = fromtab.doctor_id AND totab.is_override='N'";

	private static final String UPDATE_DR_OP_CONSCHARGE_BY = "UPDATE doctor_op_consultation_charge totab SET " +
		" op_charge = doroundvarying(fromtab.op_charge,?,?), op_revisit_charge = doroundvarying(fromtab.op_revisit_charge,?,?)," +
		" private_cons_charge = doroundvarying(fromtab.private_cons_charge,?,?), private_cons_revisit_charge = doroundvarying(fromtab.private_cons_revisit_charge,?,?)," +
		" op_oddhr_charge = doroundvarying(fromtab.op_oddhr_charge,?,?)" +
		" FROM doctor_op_consultation_charge fromtab" +
		" WHERE totab.org_id = ? AND fromtab.org_id = ?" +
		" AND totab.doctor_id = fromtab.doctor_id AND totab.is_override='N'";

	public static boolean updateOrgForDrOPConscharge(Connection con,String orgId,String varianceType,
			Double varianceValue,Double varianceBy,boolean useValue,String baseOrgId,
			Double nearstRoundOfValue) throws SQLException, IOException {

		boolean status = false;
		PreparedStatement pstmt = null;

		if (useValue) {

			if (varianceType.equals("Incr"))
				pstmt = con.prepareStatement(UPDATE_DR_OP_CONSCHARGE_PLUS);
			else
				pstmt = con.prepareStatement(UPDATE_DR_OP_CONSCHARGE_MINUS);

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

			pstmt = con.prepareStatement(UPDATE_DR_OP_CONSCHARGE_BY);
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


	private static final String COPY_DOCTOR_DETAILS_TO_ALL_ORGS =
		" INSERT INTO doctor_org_details (doctor_id, org_id, applicable,username,mod_time) " +
		" SELECT dod.doctor_id, o.org_id, dod.applicable,dod.username,dod.mod_time " +
		" FROM organization_details o " +
		" JOIN doctor_org_details dod ON (dod.doctor_id=? AND dod.org_id='ORG0001') " +
		" WHERE o.org_id != 'ORG0001'";


	public void copyDoctorDetailsToAllOrgs(Connection con, String doctorId) throws SQLException {
		PreparedStatement ps = con.prepareStatement(COPY_DOCTOR_DETAILS_TO_ALL_ORGS);
		ps.setString(1, doctorId);
		ps.executeUpdate();
		ps.close();
	}

	private static final String INSERT_ORG_DETAILS_FOR_DOCTORS = "INSERT INTO doctor_org_details " +
		"	SELECT doctor_id, ?, applicable, ?, ?, ?, 'N'" +
		"	FROM doctor_org_details WHERE org_id=?;";

	public static boolean addOrgCodesForDoctors(Connection con,String newOrgId,String varianceType,
		  Double varianceValue,Double varianceBy,boolean useValue,String baseOrgId,
		  Double nearstRoundOfValue, String userName) throws Exception{
			boolean status = false;
			PreparedStatement ps = null;
            BasicDynaBean obean = new OrgMasterDao().findByKey(con, "org_id", newOrgId);
            String rateSheetId = ("N".equals((String)obean.get("is_rate_sheet")) ? baseOrgId : null);
			try{
		   		ps = con.prepareStatement(INSERT_ORG_DETAILS_FOR_DOCTORS);
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
		return addOrgCodesForDoctors(con, newOrgId, null, null, null, false, baseOrgId, null, userName);
	}

    private static String INIT_ORG_DETAILS = "INSERT INTO doctor_org_details" +
    " (doctor_id, org_id, applicable, base_rate_sheet_id, is_override) " +
    "   SELECT doctor_id, ?, true, null, 'N'" +
    "   FROM doctors";

    private static String INIT_IP_CHARGES = "INSERT INTO doctor_consultation_charge(doctor_name,organization,bed_type," +
    "doctor_ip_charge,night_ip_charge,ot_charge,co_surgeon_charge,assnt_surgeon_charge,ward_ip_charge)" +
    "(SELECT doctor_id, ?, abov.bed_type, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0" +
    "FROM doctors d CROSS JOIN all_beds_orgs_view abov WHERE abov.org_id =? ) ";

    private static String INIT_OP_CHARGES = "INSERT INTO doctor_op_consultation_charge(doctor_id,org_id, " +
    "op_charge,op_revisit_charge,private_cons_charge,private_cons_revisit_charge,op_oddhr_charge) " +
    "(SELECT doctor_id, ?, 0.0, 0.0, 0.0, 0.0, 0.0 " +
    "FROM doctors d) ";

    private static final String UPDATE_IP_CHARGES = "UPDATE doctor_consultation_charge AS target SET " +
    " doctor_ip_charge = doroundvarying(dc.doctor_ip_charge, ?, ?), " +
    " night_ip_charge = doroundvarying(dc.night_ip_charge, ?, ?), " +
    " ot_charge = doroundvarying(dc.ot_charge, ?, ?), " +
    " co_surgeon_charge = doroundvarying(dc.co_surgeon_charge, ?, ?), " +
    " assnt_surgeon_charge = doroundvarying(dc.assnt_surgeon_charge, ?, ?), " +
    " ward_ip_charge = doroundvarying(dc.ward_ip_charge, ?, ?), " +
    " doctor_ip_charge_discount = doroundvarying(dc.doctor_ip_charge_discount, ?, ?), " +
    " night_ip_charge_discount = doroundvarying(dc.night_ip_charge_discount, ?, ?), " +
    " ot_charge_discount = doroundvarying(dc.ot_charge_discount, ?, ?), " +
    " co_surgeon_charge_discount = doroundvarying(dc.co_surgeon_charge_discount, ?, ?), " +
    " assnt_surgeon_charge_discount = doroundvarying(dc.assnt_surgeon_charge_discount, ?, ?), " +
    " ward_ip_charge_discount = doroundvarying(dc.ward_ip_charge_discount, ?, ?), " +
    " is_override = 'N' " +
    " FROM doctor_consultation_charge dc, doctor_org_details dod " +
    " where dod.org_id = ? and dc.doctor_name = dod.doctor_id  and dod.base_rate_sheet_id = ? and " +
    " target.doctor_name = dc.doctor_name and target.bed_type = dc.bed_type and " +
    " dod.applicable = true and target.is_override != 'Y'"+
    " and dc.organization = ? and target.organization = ?";

    private static final String UPDATE_OP_CHARGES = "UPDATE doctor_op_consultation_charge AS target SET " +
    " op_charge = doroundvarying(dc.op_charge, ?, ?), " +
    " op_revisit_charge = doroundvarying(dc.op_revisit_charge, ?, ?), " +
    " private_cons_charge = doroundvarying(dc.private_cons_charge, ?, ?), " +
    " private_cons_revisit_charge = doroundvarying(dc.private_cons_revisit_charge, ?, ?), " +
    " op_oddhr_charge = doroundvarying(dc.op_oddhr_charge, ?, ?), " +
    " op_charge_discount = doroundvarying(dc.op_charge_discount, ?, ?), " +
    " op_revisit_discount = doroundvarying(dc.op_revisit_discount, ?, ?), " +
    " private_cons_discount = doroundvarying(dc.private_cons_discount, ?, ?), " +
    " private_revisit_discount = doroundvarying(dc.private_revisit_discount, ?, ?), " +
    " op_oddhr_charge_discount = doroundvarying(dc.op_oddhr_charge_discount, ?, ?), " +
    " is_override = 'N' " +
    " FROM doctor_op_consultation_charge dc, doctor_org_details dod " +
    " where dod.org_id = ? and dc.doctor_id = dod.doctor_id  and dod.base_rate_sheet_id = ? and " +
    " target.doctor_id = dc.doctor_id and " +
    " dod.applicable = true and target.is_override != 'Y'"+
    " and dc.org_id = ? and target.org_id = ?";

    private static final String UPDATE_EXCLUSIONS = "UPDATE doctor_org_details AS target " +
    " SET applicable = true, base_rate_sheet_id = dod.org_id, is_override = 'N' " +
    " FROM doctor_org_details dod WHERE dod.doctor_id = target.doctor_id and " +
    " dod.org_id = ? and dod.applicable = true and target.org_id = ? and target.applicable = false";

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


		Object ipParams[] = {varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff,
				varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff,
				varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff,
				varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff,
				newOrgId, baseOrgId, baseOrgId, newOrgId};
		Object opParams[] = {varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff,
				varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff,
				varianceBy, roundOff, varianceBy, roundOff, newOrgId, baseOrgId, baseOrgId, newOrgId};

		status = updateExclusions(con, UPDATE_EXCLUSIONS, newOrgId, baseOrgId);

		if (status) status = updateCharges(con, UPDATE_IP_CHARGES, ipParams);
		if (status) status = updateCharges(con, UPDATE_OP_CHARGES, opParams);
		// postAuditEntry(con, "operation_charges_audit_log", userName, orgName);
		return status;
	}

    public boolean initRatePlan(Connection con, String newOrgId, String varianceType,
			Double varianceBy,String baseOrgId, Double roundOff, String userName, String orgName) throws Exception {
		boolean status = addOrgCodesForItems(con, newOrgId, baseOrgId, userName);
		if (status) status = addOrgFordoctors(con,newOrgId, varianceType,
				0.0, varianceBy, false, baseOrgId,
				roundOff, true);
		if (status) status = addOrgForOpCharges(con,newOrgId, varianceType,
				0.0, varianceBy, false, baseOrgId,
				roundOff, true);
		return status;
    }

    private static final String REINIT_EXCLUSIONS = "UPDATE doctor_org_details as target " +
    " SET applicable = dod.applicable, base_rate_sheet_id = dod.org_id, " +
    " is_override = 'N' " +
    " FROM doctor_org_details dod WHERE dod.doctor_id = target.doctor_id and " +
    " dod.org_id = ? and target.org_id = ? and target.is_override != 'Y'";

    public boolean reinitRatePlan(Connection con, String newOrgId, String varianceType,
			Double variance,String baseOrgId, Double rndOff,String userName, String orgName) throws Exception {
		boolean status = false;
		if(!varianceType.equals("Incr")) {
			variance = new Double(-variance);
		}
		BigDecimal varianceBy = new BigDecimal(variance);
        BigDecimal roundOff = new BigDecimal(rndOff);

		Object ipupdparams[] = {varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff,
				varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff,
				varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff, newOrgId, baseOrgId};
		Object opupdparams[] = {varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff,
				varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff,
				varianceBy, roundOff, newOrgId, baseOrgId};
		status = updateExclusions(con, REINIT_EXCLUSIONS, newOrgId, baseOrgId, true);
		if (status) status = updateCharges(con,UPDATE_RATEPLAN_DOCCONSCHG, ipupdparams);
		if (status) status = updateCharges(con,UPDATE_RATEPLAN_DR_OP_CONSCHARGE, opupdparams);
		return status;
    }

	private static final String GET_DERIVED_RATE_PALN_DETAILS = "select rp.org_id,od.org_name, "+
		" case when rate_variation_percent<0 then 'Decrease By' else 'Increase By' end as discormarkup, "+
		" rate_variation_percent,round_off_amount,dod.applicable,dod.doctor_id,rp.base_rate_sheet_id, dod.is_override "+
		" from rate_plan_parameters rp "+
 		" join organization_details od on(od.org_id=rp.org_id) "+
		" join doctor_org_details dod on (dod.org_id = rp.org_id) "+
		" where rp.base_rate_sheet_id =?  and doctor_id=? and dod.base_rate_sheet_id = ? ";

	public List<BasicDynaBean> getDerivedRatePlanDetails(String baseRateSheetId,String doctorId)throws SQLException {
		return getDerivedRatePlanDetails(baseRateSheetId, "doctor", doctorId,GET_DERIVED_RATE_PALN_DETAILS);
	}

	public boolean updateOrgForDerivedRatePlans(Connection con,String[] ratePlanIds, String[] applicable,
			String doctorId) throws Exception{
		return updateOrgForDerivedRatePlans(con,ratePlanIds, applicable, "doctor_org_details", "doctor",
				"doctor_id", doctorId);
	}

	public boolean updateOpChargesForDerivedRatePlans(Connection con,String baseRateSheetId, String[] ratePlanIds,
			Double op_charge,Double op_revisit_charge,Double private_cons_charge,
			Double private_cons_revisit_charge,Double op_oddhr_charge,String doctorId,
			GenericDAO opChargesdao,Double op_charge_discount,Double op_revisit_discount,
			Double private_cons_discount,Double private_revisit_discount,Double op_oddhr_charge_discount)
		throws SQLException,Exception {

		boolean success = false;
		for(int i=0; i<ratePlanIds.length; i++) {
			boolean overrided = isChargeOverrided(con,ratePlanIds[i],"doctor_id",doctorId,"doctor","doctor_org_details");
			if(!overrided) {
	    		Map<String,Object> keys = new HashMap<String, Object>();
	    		keys.put("base_rate_sheet_id", baseRateSheetId);
	    		keys.put("org_id", ratePlanIds[i]);
	    		BasicDynaBean bean = ratePlanParameterDao.findByKey(keys);
	    		int variation =(Integer)bean.get("rate_variation_percent");
	    		int roundoff = (Integer)bean.get("round_off_amount");

	    		Double opCharge = calculateCharge(op_charge, new Double(variation), roundoff);
	    		Double opRevisitCharge = calculateCharge(op_revisit_charge, new Double(variation), roundoff);
	    		Double privateConsCharge = calculateCharge(private_cons_charge, new Double(variation), roundoff);
	    		Double privateConsRevisitCharge = calculateCharge(private_cons_revisit_charge, new Double(variation), roundoff);
	    		Double opOddhrCharge = calculateCharge(op_oddhr_charge, new Double(variation), roundoff);

	    		Double opDisc = calculateCharge(op_charge_discount, new Double(variation), roundoff);
	    		Double opRevisitDisc = calculateCharge(op_revisit_discount, new Double(variation), roundoff);
	    		Double privateConsDisc = calculateCharge(private_cons_discount, new Double(variation), roundoff);
	    		Double privateConsRevisitDisc = calculateCharge(private_revisit_discount, new Double(variation), roundoff);
	    		Double opOddhrDisc = calculateCharge(op_oddhr_charge_discount, new Double(variation), roundoff);

	    		BasicDynaBean opBean = opChargesdao.getBean();
	    		opBean.set("op_charge", new BigDecimal(opCharge));
	    		opBean.set("op_revisit_charge", new BigDecimal(opRevisitCharge));
	    		opBean.set("private_cons_charge", new BigDecimal(privateConsCharge));
	    		opBean.set("private_cons_revisit_charge", new BigDecimal(privateConsRevisitCharge));
	    		opBean.set("op_oddhr_charge", new BigDecimal(opOddhrCharge));

	    		opBean.set("op_charge_discount", new BigDecimal(opDisc));
	    		opBean.set("op_revisit_discount", new BigDecimal(opRevisitDisc));
	    		opBean.set("private_cons_discount", new BigDecimal(privateConsDisc));
	    		opBean.set("private_revisit_discount", new BigDecimal(privateConsRevisitDisc));
	    		opBean.set("op_oddhr_charge_discount", new BigDecimal(opOddhrDisc));

	    		Map<String,Object> opKeys = new HashMap<String, Object>();
	    		opKeys.put("doctor_id", doctorId);
	    		opKeys.put("org_id", ratePlanIds[i]);
	    		success = opChargesdao.update(con, opBean.getMap(), opKeys)>0;
			}
		}
		return success;
	}

	public boolean updateIPChargesForDerivedRateplans(Connection con, String baseRateSheetId, String[] ratePlanIds,
			Double[] doctorIpCharge,Double[] nightIpCharge,Double[] otCharge,Double[] coSurgeonCharge,
			Double[] assntsurgeonCharge, Double[] wardIpCharge,String doctorId,String[] bedType,
			GenericDAO ipChargesDao,Double[] doctorIpDiscount,Double[] nightIpDiscount,Double[] otDiscount,
			Double[] coSurgeonDiscount,Double[] assntsurgeonDiscount,Double[] wardIpDiscount)throws SQLException,Exception {
		boolean success = false;

		for(int i=0; i<ratePlanIds.length; i++) {
    		Map<String,Object> keys = new HashMap<String, Object>();
    		keys.put("base_rate_sheet_id", baseRateSheetId);
    		keys.put("org_id", ratePlanIds[i]);
    		BasicDynaBean bean = ratePlanParameterDao.findByKey(keys);
    		int variation =(Integer)bean.get("rate_variation_percent");
    		int roundoff = (Integer)bean.get("round_off_amount");

    		List<BasicDynaBean> chargeList = new ArrayList();
    		boolean overrided = isChargeOverrided(con,ratePlanIds[i],"doctor_id",doctorId,"doctor","doctor_org_details");
    		if(!overrided) {
	    		for(int k=0 ; k<bedType.length; k++) {
	    			BasicDynaBean charge = ipChargesDao.getBean();
	    			charge.set("doctor_name", doctorId);
	    			charge.set("organization", ratePlanIds[i]);
	    			charge.set("bed_type", bedType[k]);

	    			Double d_IpCharge = calculateCharge(doctorIpCharge[k], new Double(variation), roundoff);
	    			Double n_IpCharge = calculateCharge(nightIpCharge[k], new Double(variation), roundoff);
	    			Double ot_charge = calculateCharge(otCharge[k], new Double(variation), roundoff);
	    			Double co_surgeonCharge = calculateCharge(coSurgeonCharge[k], new Double(variation), roundoff);
	    			Double ass_surgeonCharge = calculateCharge(assntsurgeonCharge[k], new Double(variation), roundoff);
	    			Double ward_IpCharge = calculateCharge(wardIpCharge[k], new Double(variation), roundoff);

	    			Double d_IpDiscount = calculateCharge(doctorIpDiscount[k], new Double(variation), roundoff);
	    			Double n_IpDiscount = calculateCharge(nightIpDiscount[k], new Double(variation), roundoff);
	    			Double ot_discount = calculateCharge(otDiscount[k], new Double(variation), roundoff);
	    			Double co_surgeonDiscount = calculateCharge(coSurgeonDiscount[k], new Double(variation), roundoff);
	    			Double ass_surgeonDiscount = calculateCharge(assntsurgeonDiscount[k], new Double(variation), roundoff);
	    			Double ward_IpDiscount = calculateCharge(wardIpDiscount[k], new Double(variation), roundoff);



	    			charge.set("doctor_ip_charge", new BigDecimal(d_IpCharge));
	    			charge.set("night_ip_charge", new BigDecimal(n_IpCharge));
	    			charge.set("ot_charge", new BigDecimal(ot_charge));
	    			charge.set("co_surgeon_charge", new BigDecimal(co_surgeonCharge));
	    			charge.set("assnt_surgeon_charge", new BigDecimal(ass_surgeonCharge));
	    			charge.set("ward_ip_charge", new BigDecimal(ward_IpCharge));

	    			charge.set("doctor_ip_charge_discount", new BigDecimal(d_IpDiscount));
	    			charge.set("night_ip_charge_discount", new BigDecimal(n_IpDiscount));
	    			charge.set("ot_charge_discount", new BigDecimal(ot_discount));
	    			charge.set("co_surgeon_charge_discount", new BigDecimal(co_surgeonDiscount));
	    			charge.set("assnt_surgeon_charge_discount", new BigDecimal(ass_surgeonDiscount));
	    			charge.set("ward_ip_charge_discount", new BigDecimal(ward_IpDiscount));
	    			chargeList.add(charge);
	    		}
    		}

    		for (BasicDynaBean c: chargeList) {
    			ipChargesDao.updateWithNames(con, c.getMap(),  new String[] {"doctor_name", "organization", "bed_type"});
    		}
    		success = true;
		}
		return success;
	}


	private static final String UPDATE_RATEPLAN_DOCCONSCHG = "UPDATE doctor_consultation_charge totab SET " +
		" doctor_ip_charge = doroundvarying(fromtab.doctor_ip_charge,?,?), night_ip_charge = doroundvarying(fromtab.night_ip_charge,?,?)," +
		" ot_charge = doroundvarying(fromtab.ot_charge,?,?), co_surgeon_charge = doroundvarying(fromtab.co_surgeon_charge,?,?)," +
		" assnt_surgeon_charge = doroundvarying(fromtab.assnt_surgeon_charge,?,?), ward_ip_charge = doroundvarying(fromtab.ward_ip_charge,?,?), " +
		" doctor_ip_charge_discount = doroundvarying(fromtab.doctor_ip_charge_discount,?,?), "+
		" night_ip_charge_discount = doroundvarying(fromtab.night_ip_charge_discount,?,?), "+
		" ot_charge_discount = doroundvarying(fromtab.ot_charge_discount,?,?), "+
		" co_surgeon_charge_discount = doroundvarying(fromtab.co_surgeon_charge_discount,?,?), "+
		" assnt_surgeon_charge_discount = doroundvarying(fromtab.assnt_surgeon_charge_discount,?,?), "+
		" ward_ip_charge_discount = doroundvarying(fromtab.ward_ip_charge_discount,?,?) "+
		" FROM doctor_consultation_charge fromtab" +
		" WHERE totab.organization = ? AND fromtab.organization = ?" +
		" AND totab.doctor_name = fromtab.doctor_name AND totab.bed_type = fromtab.bed_type AND totab.is_override='N'";

	public boolean updateConstChargesForDerivedRatePlans(String orgId,String varianceType,
			Double varianceValue,Double varianceBy,String baseOrgId,
			Double nearstRoundOfValue,String userName, String orgName,boolean upload) throws SQLException, Exception {

		boolean success = false;
		Connection con = null;
		try {
			con = DataBaseUtil.getConnection(60);
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
						variance, roundoff, variance, roundoff,variance, roundoff,variance, roundoff, variance, roundoff,
						variance, roundoff, variance, roundoff, variance, roundoff, orgId, baseOrgId, baseOrgId, orgId};
				success = updateCharges(con, UPDATE_IP_CHARGES, updparams);
			}
		}finally {
			DataBaseUtil.commitClose(con, success);
		}

		return success;
	}

	private static final String UPDATE_RATEPLAN_DR_OP_CONSCHARGE = "UPDATE doctor_op_consultation_charge totab SET " +
		" op_charge = doroundvarying(fromtab.op_charge,?,?), op_revisit_charge = doroundvarying(fromtab.op_revisit_charge,?,?)," +
		" private_cons_charge = doroundvarying(fromtab.private_cons_charge,?,?), private_cons_revisit_charge = doroundvarying(fromtab.private_cons_revisit_charge,?,?)," +
		" op_oddhr_charge = doroundvarying(fromtab.op_oddhr_charge,?,?), " +
		" op_charge_discount = doroundvarying(fromtab.op_charge_discount,?,?), "+
		" op_revisit_discount = doroundvarying(fromtab.op_revisit_discount,?,?), "+
		" private_cons_discount = doroundvarying(fromtab.private_cons_discount,?,?), "+
		" private_revisit_discount = doroundvarying(fromtab.private_revisit_discount,?,?), "+
		" op_oddhr_charge_discount = doroundvarying(fromtab.op_oddhr_charge_discount,?,?) "+
		" FROM doctor_op_consultation_charge fromtab" +
		" WHERE totab.org_id = ? AND fromtab.org_id = ?" +
		" AND totab.doctor_id = fromtab.doctor_id AND totab.is_override='N'";

	public boolean updateDrOPConschargesForDerivedRatePlans(String orgId,String varianceType,
			Double varianceValue,Double varianceBy,String baseOrgId,
			Double nearstRoundOfValue,String userName, String orgName,boolean upload) throws SQLException, Exception {

		boolean success = false;
		Connection con = null;
		try {
			con = DataBaseUtil.getConnection(60);
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
						variance, roundoff, variance, roundoff,variance, roundoff,variance, roundoff, variance, roundoff,
						variance, roundoff, orgId, baseOrgId, baseOrgId, orgId};
				success = updateCharges(con, UPDATE_OP_CHARGES, updparams);
			}
		} finally {
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
				varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff, varianceBy, roundOff,
				varianceBy, roundOff, varianceBy, roundOff, ratePlanId, rateSheetId, bedType};
		status = updateCharges(con,UPDATE_RATEPLAN_DOCCONSCHG + " AND totab.bed_type=? ", updparams);
		return status;
	}


}
