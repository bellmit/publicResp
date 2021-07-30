package com.insta.hms.master.RateplanSpreadsheet;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class RateplanSpreadsheetDAO {


	public List<BasicDynaBean> getListForOperations(String orgId, boolean showdiscount,
			boolean onlyHospitalCharge, int servicegroupId)throws SQLException {

		List<String> bedTypes = BedMasterDAO.getUnionOfBedTypes();
		StringBuilder query = new StringBuilder();
		int index = 1;
		int bedCount = 0;

		query.append("SELECT operation_code, operation_name, serviceSubGroup.service_sub_group_name, " +
				"'Operation'::character varying AS type,");

		for (String bed : bedTypes) {
			bedCount++;

			query.append("(SELECT surg_asstance_charge FROM operation_charges WHERE op_id =" +
					" operation.op_id AND bed_type = ? AND org_id = ?) AS "+DataBaseUtil.quoteIdent(bed+"hospCharge", true));

			if (onlyHospitalCharge) {
				if (showdiscount)
					query.append(",");
				else {
					if (bedCount < bedTypes .size())
						query.append(",");
				}

			} else {
				query.append(",");
			}
			if (showdiscount) {
				query.append("(SELECT surg_asst_discount FROM operation_charges WHERE op_id =" +
						" operation.op_id AND bed_type = ? AND org_id = ?) AS "+DataBaseUtil.quoteIdent(bed+"hospchargeDiscount", true));
				if (onlyHospitalCharge) {
					if (bedCount < bedTypes .size())
						query.append(",");
				} else {
					query.append(",");
				}
			}
			if (!onlyHospitalCharge) {
				query.append("(SELECT surgeon_charge FROM operation_charges WHERE op_id =" +
						" operation.op_id AND bed_type = ? AND org_id = ?) AS "+DataBaseUtil.quoteIdent(bed+"doctorCharge", true)+", ");
				if (showdiscount) {
					query.append("(SELECT surg_discount FROM operation_charges WHERE op_id =" +
							" operation.op_id AND bed_type = ? AND org_id = ?) AS "+DataBaseUtil.quoteIdent(bed+"doctorChargeDiscount", true)+", ");
				}
				query.append("(SELECT anesthetist_charge FROM operation_charges WHERE op_id =" +
						" operation.op_id AND bed_type = ? AND org_id = ?) AS "+DataBaseUtil.quoteIdent(bed+"anesthesiaCharge", true)+", ");
				if (showdiscount) {
					query.append("(SELECT anest_discount FROM operation_charges WHERE op_id =" +
							" operation.op_id AND bed_type = ? AND org_id = ?) AS "+DataBaseUtil.quoteIdent(bed+"anesthesiachargeDiscount", true)+", ");
				}
				query.append("(SELECT "+0+"::numeric) AS total ");
				if (bedCount < bedTypes .size())
					query.append(",");
			}



		}
		query.append("FROM operation_master operation ");
		query.append("JOIN operation_org_details organization ON(operation.op_id = organization.operation_id) AND organization.org_id = ? ");
		query.append("JOIN service_sub_groups serviceSubGroup USING(service_sub_group_id) ");
		query.append("JOIN service_groups serviceGroup USING(service_group_id) ");
		query.append("WHERE service_group_id = ? AND operation.status = 'A'");
		query.append("ORDER BY operation.op_id");

		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			pstmt = con.prepareStatement(query.toString());

			for (String bed : bedTypes) {
				pstmt.setString(index++, bed);
				pstmt.setString(index++, orgId);
				if (showdiscount) {
					pstmt.setString(index++, bed);
					pstmt.setString(index++, orgId);
				}
				if (!onlyHospitalCharge) {
					pstmt.setString(index++, bed);
					pstmt.setString(index++, orgId);
					if (showdiscount) {
						pstmt.setString(index++, bed);
						pstmt.setString(index++, orgId);
					}
					pstmt.setString(index++, bed);
					pstmt.setString(index++, orgId);
					if (showdiscount) {
						pstmt.setString(index++, bed);
						pstmt.setString(index++, orgId);
					}
				}
			}
			pstmt.setString(index++, orgId);
			pstmt.setInt(index++, servicegroupId);

			return DataBaseUtil.queryToDynaList(pstmt);
		} finally {
			DataBaseUtil.closeConnections(con, pstmt);
		}
	}

	public List<BasicDynaBean> getListForEquipments(String orgId, boolean showdiscount,
			boolean onlyHospitalCharge, int servicegroupId)throws SQLException {

		List<String> bedTypes = BedMasterDAO.getUnionOfBedTypes();
		StringBuilder query = new StringBuilder();
		int index = 1;
		int bedCount = 0;

		query.append("SELECT equipment_code, equipment_name, serviceSubGroup.service_sub_group_name," +
				" 'Equipment'::character varying AS type,");

		for (String bed : bedTypes) {
			bedCount++;

			query.append("(SELECT CASE WHEN charge_basis ='D' THEN daily_charge WHEN charge_basis='H' THEN incr_charge ELSE daily_charge END " +
					"FROM equipement_charges WHERE equip_id = equipment.eq_id AND bed_type = ? AND org_id = ?)" +
					" AS "+DataBaseUtil.quoteIdent(bed+"hospCharge", true));
			if (onlyHospitalCharge) {
				if (showdiscount)
					query.append(",");
				else {
					if (bedCount < bedTypes .size())
						query.append(",");
				}

			} else {
				query.append(",");
			}

			if (showdiscount) {
				query.append("(SELECT CASE WHEN charge_basis ='D' THEN daily_charge_discount WHEN charge_basis='H' " +
						"THEN incr_charge_discount ELSE daily_charge_discount END " +
						"FROM equipement_charges WHERE equip_id = equipment.eq_id AND bed_type = ? AND org_id = ?)" +
						" AS "+DataBaseUtil.quoteIdent(bed+"hospchargeDiscount", true));
				if (onlyHospitalCharge) {
					if (bedCount < bedTypes .size())
						query.append(",");
				} else {
					query.append(",");
				}
			}
			if (!onlyHospitalCharge) {
				query.append("(SELECT "+0+"::numeric) AS "+DataBaseUtil.quoteIdent(bed+"doctorCharge", true)+", ");
				if (showdiscount) {
					query.append("(SELECT "+0+"::numeric) AS "+DataBaseUtil.quoteIdent(bed+"doctorChargeDiscount", true)+", ");
				}
				query.append("(SELECT "+0+"::numeric) AS "+DataBaseUtil.quoteIdent(bed+"anesthesiaCharge", true)+", ");
				if (showdiscount) {
					query.append("(SELECT "+0+"::numeric) AS "+DataBaseUtil.quoteIdent(bed+"anesthesiachargeDiscount", true)+", ");
				}
				query.append("(SELECT "+0+"::numeric) AS total ");
				if (bedCount < bedTypes .size())
					query.append(",");
			}

		}
		query.append("FROM equipment_master equipment ");
		query.append("JOIN service_sub_groups serviceSubGroup USING(service_sub_group_id) ");
		query.append("JOIN service_groups serviceGroup USING(service_group_id) ");
		query.append("WHERE service_group_id = ? AND equipment.status = 'A'");
		query.append("ORDER BY equipment.eq_id");

		Connection con = null;
		PreparedStatement pstmt = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			pstmt = con.prepareStatement(query.toString());

			for (String bed : bedTypes) {
				pstmt.setString(index++, bed);
				pstmt.setString(index++, orgId);
				if (showdiscount) {
					pstmt.setString(index++, bed);
					pstmt.setString(index++, orgId);
				}
			}
			pstmt.setInt(index++, servicegroupId);

			return DataBaseUtil.queryToDynaList(pstmt);
		} finally {
			DataBaseUtil.closeConnections(con, pstmt);
		}
	}


	private static String COMMON_CHARGES_WITH_ONLY_HOSP_CHARGE = "SELECT othercharge_code, " +
			"charge_name, serviceSubGroup.service_sub_group_name, 'Common'::character varying AS type," +
							" charge AS hosp_charge" +
							" FROM common_charges_master cm" +
							" JOIN service_sub_groups serviceSubGroup USING(service_sub_group_id)" +
							" JOIN service_groups serviceGroup USING(service_group_id)" +
							" WHERE service_group_id = ? AND cm.status = 'A'" +
							" ORDER BY charge_name";

	private static String COMMON_CHARGES_WITH_ONLY_HOSP_AND_DISCOUNT = "SELECT othercharge_code, " +
			"charge_name, serviceSubGroup.service_sub_group_name, 'Common'::character varying AS type," +
							" charge AS hosp_charge,0 AS hosp_discount" +
							" FROM common_charges_master cm" +
							" JOIN service_sub_groups serviceSubGroup USING(service_sub_group_id)" +
							" JOIN service_groups serviceGroup USING(service_group_id)" +
							" WHERE service_group_id = ? AND cm.status = 'A'" +
							" ORDER BY charge_name";

	private static String COMMON_CHARGES_WITHOUT_DISCOUNT = "SELECT othercharge_code, " +
			"charge_name, serviceSubGroup.service_sub_group_name, 'Common'::character varying AS type," +
							" charge AS hosp_charge,0 AS doctor_charge,0 AS anae_charge" +
							" FROM common_charges_master cm" +
							" JOIN service_sub_groups serviceSubGroup USING(service_sub_group_id)" +
							" JOIN service_groups serviceGroup USING(service_group_id)" +
							" WHERE service_group_id = ? AND cm.status = 'A'" +
							" ORDER BY charge_name";

	private static String COMMON_CHARGES_WITH_DISCOUNT = "SELECT othercharge_code, " +
			"charge_name, serviceSubGroup.service_sub_group_name, 'Common'::character varying AS type," +
							" charge AS hosp_charge,0 AS doctor_charge,0 AS anae_charge," +
							" 0 AS hosp_discount,0 AS doctor_discount,0 AS anae_discount" +
							" FROM common_charges_master cm" +
							" JOIN service_sub_groups serviceSubGroup USING(service_sub_group_id)" +
							" JOIN service_groups serviceGroup USING(service_group_id)" +
							" WHERE service_group_id = ? AND cm.status = 'A'" +
							" ORDER BY charge_name";


	public List<BasicDynaBean> getListForCommoncharges(String orgId, boolean showdiscount,
			boolean onlyHospCharge, int servicegroupId)throws SQLException {
		if (orgId.equals("ORG0001")) {
			Connection con = null;
			PreparedStatement pstmt = null;
			try {
				con = DataBaseUtil.getReadOnlyConnection();
				if (onlyHospCharge) {
					if (showdiscount)
						pstmt = con.prepareStatement(COMMON_CHARGES_WITH_ONLY_HOSP_AND_DISCOUNT);
					else
						pstmt = con.prepareStatement(COMMON_CHARGES_WITH_ONLY_HOSP_CHARGE);
				}
				else if (showdiscount)
					pstmt = con.prepareStatement(COMMON_CHARGES_WITH_DISCOUNT);
				else
					pstmt = con.prepareStatement(COMMON_CHARGES_WITHOUT_DISCOUNT);

				pstmt.setInt(1, servicegroupId);
				return DataBaseUtil.queryToDynaList(pstmt);

			} finally {
				DataBaseUtil.closeConnections(con, pstmt);
			}
		}
		return Collections.emptyList();
	}

}