package com.insta.hms.master.ServiceMaster;

import com.insta.hms.common.GenericDAO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ServiceOrgDetailsDAO extends GenericDAO {

	static Logger logger = LoggerFactory.getLogger(ServiceOrgDetailsDAO.class);

	public ServiceOrgDetailsDAO() {
		super("service_org_details");
	}

	private static final String COPY_GENERAL_DETAILS_TO_ALL_ORGS = 
		" INSERT INTO service_org_details (service_id, org_id, applicable, item_code, code_type) " +
		" SELECT s.service_id, o.org_id, s.applicable, s.item_code, s.code_type  " +
		" FROM organization_details o " +
		"  JOIN service_org_details s ON (s.service_id=? AND s.org_id='ORG0001') " +
		" WHERE o.org_id != 'ORG0001'";


	public void copyServiceDetailsToAllOrgs(Connection con, String serviceId) throws SQLException {
		PreparedStatement ps = con.prepareStatement(COPY_GENERAL_DETAILS_TO_ALL_ORGS);
		ps.setString(1, serviceId);
		ps.executeUpdate();
		ps.close();
	}
}


