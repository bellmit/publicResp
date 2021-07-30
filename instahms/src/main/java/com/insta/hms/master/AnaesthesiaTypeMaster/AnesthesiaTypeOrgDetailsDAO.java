package com.insta.hms.master.AnaesthesiaTypeMaster;

import com.insta.hms.common.GenericDAO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AnesthesiaTypeOrgDetailsDAO extends GenericDAO {

	static Logger logger = LoggerFactory.getLogger(AnesthesiaTypeOrgDetailsDAO.class);

	public AnesthesiaTypeOrgDetailsDAO() {
		super("anesthesia_type_org_details");
	}

	private static final String COPY_GENERAL_DETAILS_TO_ALL_ORGS =
		" INSERT INTO anesthesia_type_org_details (anesthesia_type_id, org_id, applicable, item_code, code_type) " +
		" SELECT ood.anesthesia_type_id, o.org_id, ood.applicable, ood.item_code, ood.code_type " +
		" FROM organization_details o " +
		"  JOIN anesthesia_type_org_details ood ON (ood.anesthesia_type_id=? AND ood.org_id='ORG0001') " +
		" WHERE o.org_id != 'ORG0001'";


	public void copyGeneralDetailsToAllOrgs(Connection con, String id) throws SQLException {
		PreparedStatement ps = con.prepareStatement(COPY_GENERAL_DETAILS_TO_ALL_ORGS);
		ps.setString(1, id);
		ps.executeUpdate();
		ps.close();
	}
}


