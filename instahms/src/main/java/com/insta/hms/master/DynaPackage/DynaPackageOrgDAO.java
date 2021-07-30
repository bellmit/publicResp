package com.insta.hms.master.DynaPackage;

import com.insta.hms.common.GenericDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DynaPackageOrgDAO extends GenericDAO {
	
	public DynaPackageOrgDAO() {
		super("dyna_package_org_details");
	}
	
	private static final String COPY_GENERAL_DETAILS_TO_ALL_ORGS = 
		" INSERT INTO dyna_package_org_details (dyna_package_id, org_id, applicable, item_code, code_type) " +
		" SELECT dp.dyna_package_id, o.org_id, dp.applicable, dp.item_code, dp.code_type  " +
		" FROM organization_details o " +
		"  JOIN dyna_package_org_details dp ON (dp.dyna_package_id=? AND dp.org_id='ORG0001') " +
		" WHERE o.org_id != 'ORG0001'";


	public void copyDynaPackageDetailsToAllOrgs(Connection con, int dynaPackageID) throws SQLException {
		PreparedStatement ps = con.prepareStatement(COPY_GENERAL_DETAILS_TO_ALL_ORGS);
		ps.setInt(1, dynaPackageID);
		ps.executeUpdate();
		ps.close();
	}
	
}