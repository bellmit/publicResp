/**
 *
 */
package com.insta.hms.master.PerDiemCodes;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * @author lakshmi
 *
 */
public class PerDiemCodesChargesDAO extends GenericDAO {

	public PerDiemCodesChargesDAO() {
		super("per_diem_codes_charges");
	}

	private static final String GET_ALL_PERDIEM_CHARGES_FOR_ORG =
		" SELECT per_diem_code,bed_type,charge FROM per_diem_codes_charges WHERE org_id=? " ;

	@SuppressWarnings("unchecked")
	public List<BasicDynaBean> getAllPerDiemChargesForOrganisation(String orgId) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_ALL_PERDIEM_CHARGES_FOR_ORG);
			ps.setString(1, orgId);
			return DataBaseUtil.queryToDynaList(ps);

		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	/*
	 * Copy the GENERAL rate plan charges to all other rate plans. Assumes that the other
	 * rate-plan charges are non-existent, ie, no updates only inserts new charges.
	 */
	private static final String COPY_GENERAL_CHARGES_TO_ALL_ORGS =
		" INSERT INTO per_diem_codes_charges (org_id, bed_type, per_diem_code, charge) " +
		" SELECT abo.org_id, abo.bed_type, pc.per_diem_code, pc.charge " +
		" FROM all_beds_orgs_view abo " +
		"  JOIN per_diem_codes_charges pc ON (pc.per_diem_code=? AND pc.bed_type = abo.bed_type " +
		"    AND pc.org_id = 'ORG0001') " +
		" WHERE abo.org_id != 'ORG0001'";

	public void copyGeneralChargesToAllOrgs(Connection con, String perDiemCode) throws SQLException {
		PreparedStatement ps = con.prepareStatement(COPY_GENERAL_CHARGES_TO_ALL_ORGS);
		ps.setString(1, perDiemCode);
		ps.executeUpdate();
		ps.close();
	}

	/*
	 * Copy the GENERAL bed type charges to all inactive bed types for the given Per Diem Code.
	 * These charges will not be inserted normally when the code is created, since
	 * only active bed charges are input from the user.
	 */
	private static final String COPY_GENERAL_CHARGES_TO_INACTIVE_BEDS =
		" INSERT INTO per_diem_codes_charges (org_id, bed_type, per_diem_code, charge) " +
		" SELECT abo.org_id, abo.bed_type, pc.per_diem_code, pc.charge " +
		" FROM all_beds_orgs_view abo " +
		"   JOIN per_diem_codes_charges pc ON (pc.org_id = abo.org_id AND pc.bed_type = 'GENERAL') " +
		" WHERE abo.bed_type IN ( " +
		"     SELECT DISTINCT intensive_bed_type FROM icu_bed_charges WHERE bed_status = 'I' " +
		"     UNION ALL SELECT DISTINCT bed_type FROM bed_details WHERE bed_status = 'I') " +
		"   AND pc.per_diem_code=? " ;

	public void copyGeneralChargesToInactiveBeds(Connection con, String perDiemCode) throws SQLException {
		PreparedStatement ps = con.prepareStatement(COPY_GENERAL_CHARGES_TO_INACTIVE_BEDS);
		ps.setString(1, perDiemCode);
		ps.executeUpdate();
		ps.close();
	}

	private static final String GET_CODE_CHARGES_FOR_ORG =
		" SELECT pc.per_diem_code, pc.bed_type, charge " +
		" 	FROM per_diem_codes_charges pc " +
		" WHERE pc.org_id=? and pc.per_diem_code=? " ;

	@SuppressWarnings("unchecked")
	public List<BasicDynaBean> getAllChargesForBedTypesAndOrg(String orgId,	String perDiemCode) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_CODE_CHARGES_FOR_ORG);
			ps.setString(1, orgId);
			ps.setString(2, perDiemCode);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

}
