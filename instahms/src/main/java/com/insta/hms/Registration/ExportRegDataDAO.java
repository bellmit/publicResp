package com.insta.hms.Registration;

import com.bob.hms.common.DataBaseUtil;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.BasicDynaClass;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.beanutils.ResultSetDynaClass;

import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ExportRegDataDAO {

	private static String REG_DATA = 
		"SELECT * FROM export_patient_details_view WHERE date(max_mod_time) between ? and ? " ;

	private static String CLAIMS = 
		"SELECT claim_id, last_submission_batch_id, payers_reference_no, main_visit_id, status, " +
		" resubmission_count, closure_type, action_remarks, comments, account_group " +
		" FROM insurance_claim WHERE main_visit_id=?";

	public static void  getXmlFieldsArray(java.sql.Date from, java.sql.Date to, OutputStream stream)
		throws Exception {

		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection con = DataBaseUtil.getConnection();

		List<BasicDynaBean> arr = null;

		try {
			ps = con.prepareStatement(REG_DATA);
			ps.setDate(1, from);
			ps.setDate(2, to);

			rs = ps.executeQuery();
			ResultSetDynaClass rsdc = new ResultSetDynaClass(rs);
			DynaProperty[] properties = rsdc.getDynaProperties();
			BasicDynaClass bdc = new BasicDynaClass("pat", BasicDynaBean.class, rsdc.getDynaProperties());
			Iterator rows = rsdc.iterator();
			while (rows.hasNext()) {
				DynaBean row = (DynaBean) rows.next();
				DynaBean pat = bdc.newInstance();
				PropertyUtils.copyProperties(pat, row);

				// add a claims list if there are more than one claim
				List claimsList = null;
				long numClaims = (Long) pat.get("num_claim_ids");
				if (numClaims > 1)
					claimsList = DataBaseUtil.queryToDynaList(CLAIMS, (String) pat.get("patient_id"));

				Map bodyMap = new HashMap();
				bodyMap.put("pat", pat);
				bodyMap.put("claimsList", claimsList);

				PatDataExportUtility.addClaimBody(stream, bodyMap);
			}
		} finally {
			DataBaseUtil.closeConnections(con, ps, rs);
		}
	}

}

