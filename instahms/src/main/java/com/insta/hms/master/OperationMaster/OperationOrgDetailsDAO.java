package com.insta.hms.master.OperationMaster;

import com.insta.hms.common.GenericDAO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class OperationOrgDetailsDAO extends GenericDAO {

	static Logger logger = LoggerFactory.getLogger(OperationOrgDetailsDAO.class);

	public OperationOrgDetailsDAO() {
		super("operation_org_details");
	}

	private static final String COPY_GENERAL_DETAILS_TO_ALL_ORGS = 
		" INSERT INTO operation_org_details (operation_id, org_id, applicable, item_code, code_type) " +
		" SELECT ood.operation_id, o.org_id, ood.applicable, ood.item_code, ood.code_type " +
		" FROM organization_details o " +
		"  JOIN operation_org_details ood ON (ood.operation_id=? AND ood.org_id='ORG0001') " +
		" WHERE o.org_id != 'ORG0001'";


	public void copyGeneralDetailsToAllOrgs(Connection con, String id) throws SQLException {
		PreparedStatement ps = con.prepareStatement(COPY_GENERAL_DETAILS_TO_ALL_ORGS);
		ps.setString(1, id);
		ps.executeUpdate();
		ps.close();
	}
}


