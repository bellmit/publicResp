package com.insta.hms.Registration;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PatientPolicyDAO extends GenericDAO{

	public PatientPolicyDAO(){
		super("patient_policy_details");
	}
/*
 * Should not update inactive policy except plain_id.
 * patient_policy_details.plan_id is one of the attribute which decides member id validity.
 * Hence plan id update irrespictive of status is needed.
 * This method is responisble for updating only plan id of policy.
 */
	public boolean updateInactivePolicyPlan(Connection con,int patientPolicyId,int newPlanId)
	throws SQLException, IOException{

		BasicDynaBean policyDetailsBean = findByKey(con,"patient_policy_id",patientPolicyId);
		policyDetailsBean.set("plan_id", newPlanId);
		return ( update(con, policyDetailsBean.getMap(), "patient_policy_id",patientPolicyId) > 0 );

	}

	private static final String UPDATE_OLD_POLICY = "UPDATE patient_policy_details set status='I' where mr_no =? and " +
	" (member_id is null or member_id =?); " ;

	public void updateOldPolicy(Connection con, String mrNo, String policyNo) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(UPDATE_OLD_POLICY);
			ps.setString(1, mrNo);
			ps.setString(2, policyNo);
			ps.executeUpdate();
		}finally{
			DataBaseUtil.closeConnections(null, ps);
		}

	}

}
