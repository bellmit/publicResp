/**
 *
 */
package com.insta.hms.billing;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.QueryBuilder;
import com.insta.hms.common.SearchQueryBuilder;
import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author lakshmi.p
 *
 */
public class ClaimReconciliationDAO {
    
	private static final String CLAIM_RECONCILIATION_FIELDS = " SELECT *  ";

	private static final String CLAIM_RECONCILIATION_COUNT = " SELECT count(claim_id) ";

	private static final String CLAIM_RECONCILIATION_TABLES = " FROM (SELECT pd.mr_no, pr.center_id, ic.main_visit_id," +
			" pd.patient_name || ' ' || coalesce(pd.middle_name, '') || ' ' || coalesce(pd.last_name, '') AS patient_full_name," +
			" ic.claim_id, payers_reference_no, isb2.submission_batch_id, isb2.created_date," +
			" isb2.submission_date, isb2.file_name, isb2.patient_type," +
			" isb2.is_resubmission,isb2.status AS submission_status, rc.resubmission_count," +
			" ic.status,isb2.username, ic.account_group, " +
			" CASE WHEN (ic.status = 'O') THEN 'Open' " +
			" 	   WHEN (ic.status = 'B' AND cs.status = 'O') THEN 'Batched' " +
			" 	   WHEN (ic.status = 'B' AND cs.status = 'S') THEN 'Sent' " +
			" 	   WHEN (ic.status = 'C' AND cs.status = 'D') THEN 'Closed' " +
			"      WHEN (ic.status = 'B' AND cs.status = 'D') THEN 'Denied' " +
			"      WHEN (ic.status = 'M') THEN 'ForResub.' " +
			"      WHEN (ic.status = 'C' AND cs.status = 'R') THEN 'Closed' ELSE NULL END AS claim_status, " +
			" ((SELECT sum(coalesce(insurance_claim_amt, 0.00) + coalesce(tax_amt, 0.00) + coalesce(return_insurance_claim_amt, 0.00) - coalesce(claim_recd_total, 0.00)) " +
			" 	FROM bill_charge_claim WHERE bill_charge_claim.claim_id = ic.claim_id AND bill_charge_claim.insurance_claim_amt >= 0) - " +
			" (coalesce((SELECT sum(coalesce(insurance_claim_amt, 0.00) + coalesce(bc.tax_amt, 0.00) + " +
			" coalesce(bc.return_insurance_claim_amt, 0.00) - coalesce(bc.claim_recd_total, 0.00))" +
			"	FROM bill_charge_claim bc" +
			"	JOIN bill_charge bcc ON bc.charge_id=bcc.charge_id " +
			"	WHERE bc.claim_id = ic.claim_id AND bc.insurance_claim_amt >= 0 " +
			" AND bcc.submission_batch_type='P' AND bcc.charge_head='PKGPKG'), 0.00))) " +
			" AS claim_due, " +
			" (SELECT sum(total_amount + total_tax - total_receipts - total_claim - total_claim_tax - deposit_set_off - points_redeemed_amt) " +
			" 	FROM bill JOIN bill_claim ON (bill_claim.bill_no = bill.bill_no) WHERE bill_claim.claim_id = ic.claim_id AND bill.total_amount >= 0) AS patient_due, " +
			" pip.insurance_co AS insurance_co_id, pip.sponsor_id As tpa_id , pip.plan_id, pip.plan_type_id :: text, " +
			" icm.insurance_co_name, tp.tpa_name, tp.max_resubmission_count, ipm.plan_name, cat.category_name, cat.category_id :: text, " +
			" ic.patient_id AS claim_for_visit " +
			" FROM bill b " +
			" JOIN bill_claim bcl ON (bcl.bill_no = b.bill_no) " +
			" JOIN insurance_claim ic ON (ic.claim_id = bcl.claim_id) " +
			"	LEFT JOIN claim_submissions cs ON (cs.claim_id = ic.claim_id AND ic.last_submission_batch_id = cs.submission_batch_id) " +
			"	LEFT JOIN insurance_submission_batch isb2 ON (cs.submission_batch_id = isb2.submission_batch_id) " +
			" 	LEFT JOIN LATERAL ( "+
			"		SELECT count(*) as resubmission_count, ic.claim_id " +
			"			FROM claim_submissions cs " +
			"     JOIN insurance_submission_batch isb ON(cs.submission_batch_id = isb.submission_batch_id) " +
			" WHERE isb.submission_batch_id = cs.submission_batch_id " +
			"			AND cs.claim_id = ic.claim_id AND isb.is_resubmission = 'Y' GROUP BY cs.claim_id) rc  ON (rc.claim_id = ic.claim_id" +
			"	) " +
			" 	LEFT JOIN patient_registration pr ON (pr.patient_id = ic.patient_id) " +
			" 	LEFT JOIN patient_insurance_plans pip ON (pip.patient_id = ic.patient_id AND pip.plan_id = bcl.plan_id) " +
			" 	LEFT JOIN patient_details pd ON (pd.mr_no = pr.mr_no)" +
			" 	LEFT JOIN tpa_master tp ON (tp.tpa_id = pip.sponsor_id)" +
			" 	LEFT JOIN insurance_company_master icm ON (icm.insurance_co_id = pip.insurance_co)" +
			" 	LEFT JOIN insurance_category_master cat ON (cat.category_id = pip.plan_type_id)" +
			" 	LEFT JOIN insurance_plan_main ipm ON (ipm.plan_id = pip.plan_id)" +
			" WHERE b.total_amount >= 0 #claim_status_filter#"+
			" AND ( patient_confidentiality_check(pd.patient_group,pd.mr_no) ) " +
			" GROUP BY pd.mr_no, pr.center_id, pd.patient_name, pd.middle_name, pd.last_name, ic.main_visit_id, " +
			"	 ic.claim_id, payers_reference_no,isb2.submission_batch_id, " +
			"	 isb2.created_date, isb2.submission_date, isb2.file_name, isb2.patient_type, isb2.is_resubmission,isb2.status, cs.status, rc.resubmission_count," +
			"	 ic.last_submission_batch_id, ic.status,isb2.username, ic.account_group," +
			"	 pip.insurance_co, pip.sponsor_id , pip.plan_id, pip.plan_type_id, " +
			"  icm.insurance_co_name, tp.tpa_name, tp.max_resubmission_count, ipm.plan_name, cat.category_name, ic.patient_id," +
			"  cat.category_id " +
			" ) AS foo ";

	public PagedList searchClaimReconciliations(Map filter, Map<LISTING, Object> listing) throws SQLException, ParseException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			boolean hasClaimIdFilter = false;
			boolean hasDenialCodeTypeFilter = false;
			con = DataBaseUtil.getConnection(60);
			String[] claimId = (String[])filter.get("claim_id");
			String[] denialCodeType = (String[])filter.get("denial_code_type");
			String[] claimsArr = null;

			if (claimId != null && claimId.length > 0 && !claimId[0].equals("")) {
				hasClaimIdFilter = true;
			}

			if (denialCodeType != null && denialCodeType.length > 0 && !denialCodeType[0].equals("")) {

				hasDenialCodeTypeFilter = true;

				Map<String,Object[]> claimfilter = new HashMap<>();
				claimfilter.put("denial_code_type", denialCodeType);

				StringBuilder query = new StringBuilder(CLAIM_ACTIVITIES);

				boolean append = false;
				if (hasClaimIdFilter)
					append = QueryBuilder.addWhereFieldOpValue(append, query, "ic.claim_id", "=", claimId[0]);

				List<String> denialCodeTypesList = Arrays.asList(denialCodeType);
				QueryBuilder.addWhereFieldOpValue(append, query, "denial_code_type", "IN", denialCodeTypesList);

				ps = con.prepareStatement(query.toString());
				int index = 1;
				if (hasClaimIdFilter) {
					ps.setString(index, claimId[0]);
					index++;
				}

				for (String code : denialCodeTypesList) {
					ps.setString(index, code);
					index++;
				}

				List<BasicDynaBean> claimActivities = DataBaseUtil.queryToDynaList(ps);
				Set<String> set = new HashSet<>();

				for (BasicDynaBean b : claimActivities) {
					set.add((String)b.get("claim_id"));
				}
				claimsArr = set.toArray(new String[0]);
			}

			if (!hasClaimIdFilter && claimsArr != null && claimsArr.length > 0) {
				filter.put("claim_id", claimsArr);
			}
			
			//Performance optimization for claim_status see HMS-16141
			String query = CLAIM_RECONCILIATION_TABLES.replaceAll("#claim_status_filter#", 
			    getClaimStatusFilters((String[]) filter.get("claim_status")));
			
			filter.remove("claim_status");

			SearchQueryBuilder qb = new SearchQueryBuilder(con, CLAIM_RECONCILIATION_FIELDS,
					CLAIM_RECONCILIATION_COUNT, query, listing);

			filter.remove("denial_code_type");
			qb.addFilterFromParamMap(filter);
			qb.build();
			PagedList l = qb.getMappedPagedList();
			if (hasDenialCodeTypeFilter && (claimsArr == null || claimsArr.length == 0)) {
				l.setDtoList(null);
			}
			filter.put("denial_code_type", denialCodeType);
			return l;

		} finally {
		  if(ps != null){
		    ps.close();
		  }
			DataBaseUtil.closeConnections(con, null);
		}
	}

	
	/**
	 * Return string to replace the placeholder in the list query. The return is the filters in the
	 * WHERE clause for claim status.
	 * 
	 * @param statuses
	 * @return
	 */
    private String getClaimStatusFilters(String[] statuses) {
		StringBuilder replacement = new StringBuilder();
		String[] claimStatuses = null != statuses ? statuses : new String[] {};
		boolean first = true;

		for (Integer index = 0; index < claimStatuses.length; index++) {
			String claimStatus = claimStatuses[index];
			String claimStatusFilter = getClaimStatusFilter(claimStatus);
       
			if (null != claimStatus && !claimStatus.isEmpty() && !claimStatusFilter.isEmpty()) {
				if (first) {
					replacement.append("AND (");
					first = false;
				} else {
					replacement.append("OR ");
				}

				replacement.append(claimStatusFilter);
			}
		}
		return replacement.append(replacement.length() > 0 ? ")" : "").toString();
    }
 
	/**
	 * Gets where clause for derived field claim_status from parent columns to speed up query to 
	 * avoid case-whens.
	 * 
	 * @param claimStatus
	 * @return WHERE Clause for the claim status.
	 */
	public String getClaimStatusFilter(String claimStatus) {
		String filter = "";
		if ("Open".equals(claimStatus)) {
			filter = "(ic.status = 'O' AND cs.status IS NULL) ";
		} else if ("Batched".equals(claimStatus)) {
			filter = "(ic.status = 'B' AND cs.status = 'O') ";
		} else if ("Sent".equals(claimStatus)) {
			filter = "(ic.status = 'B' AND cs.status = 'S') "; 
		} else if ("Closed".equals(claimStatus)) {
			filter = "((ic.status = 'C' AND cs.status = 'D') OR (ic.status = 'C' AND cs.status = 'R')) ";
		} else if ("Denied".equals(claimStatus)) {
			filter = "(ic.status = 'B' AND cs.status = 'D') ";
		} else if ("ForResub.".equals(claimStatus)) {
			filter = "(ic.status = 'M') ";
		}

		return filter;
	}

	private static final String CLAIM_ACTIVITIES =
			" SELECT * FROM (SELECT ic.claim_id, scl.denial_code, idc.type AS denial_code_type " +
			" 	FROM store_sales_details s" +
			"	JOIN sales_claim_details scl ON (scl.sale_item_id = s.sale_item_id) " +
			"	JOIN store_sales_main sm on (s.sale_id = sm.sale_id)" +
			"	JOIN bill_charge bc ON (sm.charge_id= bc.charge_id)" +
			"	JOIN bill b ON (b.bill_no = bc.bill_no) " +
			"	JOIN bill_claim bcl ON (b.bill_no = bcl.bill_no) " +
			"	JOIN insurance_claim ic ON (ic.claim_id = bcl.claim_id) " +
			"   LEFT JOIN insurance_denial_codes idc ON (idc.denial_code = scl.denial_code) " +
			"	WHERE (s.quantity + s.return_qty) > 0 " +
			" UNION " +
			" SELECT ic.claim_id, bccl.denial_code, idc.type AS denial_code_type " +
			"	FROM bill_charge bcc " +
			"	JOIN bill_charge_claim bccl ON (bcc.charge_id = bccl.charge_id) " +
			"	JOIN bill b ON (b.bill_no = bcc.bill_no)" +
			"	JOIN bill_claim bcl ON (b.bill_no = bcl.bill_no) " +
			"	JOIN insurance_claim ic ON (ic.claim_id = bcl.claim_id)" +
			"   LEFT JOIN insurance_denial_codes idc ON (idc.denial_code = bccl.denial_code)" +
			"	WHERE (bcc.act_quantity + bcc.return_qty) > 0 " +
			" ) AS foo " ;

	public static final String GET_ATTACHMENT_SIZE = "SELECT length(attachment) as attachment_size "+
				" FROM insurance_claim WHERE claim_id  = ?";

	public static int getFileSize(String claimId) throws SQLException {
		int size = 0;
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_ATTACHMENT_SIZE);
			ps.setString(1, claimId);
			rs = ps.executeQuery();
			while (rs.next()){
				size = rs.getInt(1);
			}
		}finally {
			DataBaseUtil.closeConnections(con, ps, rs);
		}

		return size;
	}

	private static final String GET_ATTACHMENT = "SELECT attachment,attachment_content_type FROM insurance_claim WHERE claim_id = ?";

	public Map getAttachment(String claimId) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_ATTACHMENT);
			ps.setString(1, claimId);
			rs = ps.executeQuery();
			if (rs.next()) {
				Map m = new HashMap();
				m.put("Content", rs.getBinaryStream(1));
				m.put("Type", rs.getString(2));
				return m;
			}
			else
				return null;
		} finally {
			DataBaseUtil.closeConnections(con, ps, rs);
		}
	}

	private static final String DELETE_ATTACHMENT = "UPDATE insurance_claim set attachment='' , attachment_content_type='' WHERE " +
													"claim_id=? ";

	public static boolean deleteAttachment(String claimId) throws SQLException {
		boolean success=false;
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(DELETE_ATTACHMENT);
			ps.setString(1, claimId);
			int result = ps.executeUpdate();
			if (result > 0 ) success = true;
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return success;
	}

	private static final String UPDATE_ATTACHMENT = "UPDATE insurance_claim set attachment=? , attachment_content_type=? WHERE " +
													"claim_id=? ";

	public static boolean updateAttachment(Map params, String claimId) throws SQLException,IOException {
		boolean success=false;
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(UPDATE_ATTACHMENT);
			InputStream stream = ((InputStream[])params.get("attachment"))[0];
			ps.setBinaryStream(1, stream, stream.available());
			ps.setString(2, ((String[])params.get("attachment_content_type"))[0]);
			ps.setString(3, claimId);
			int result = ps.executeUpdate();
			if (result > 0 ) success = true;
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return success;
	}

	public BasicDynaBean getLatestOpenSubOrResub(String resubmission, String claimId, int centerId) throws SQLException {

		String getSubmissionQuery = "SELECT submission_batch_id, created_date, " +
			" isb.insurance_co_id, isb.tpa_id, is_resubmission, isb.status, file_name, patient_type, " +
			" CASE WHEN isb.processing_type ='P' AND processing_status='C' "+
			" THEN true ELSE false END as prod_xml_generated "+
			" FROM insurance_submission_batch isb " +
			" JOIN (SELECT visit_type, primary_insurance_co, primary_sponsor_id   " +
			"		FROM patient_registration WHERE patient_id = " +
			"					(SELECT main_visit_id FROM insurance_claim WHERE claim_id = ?)) AS pr" +
			"	ON ((pr.visit_type = isb.patient_type OR isb.patient_type = '*') " +
			"		AND (pr.primary_insurance_co = isb.insurance_co_id OR pr.primary_sponsor_id = isb.tpa_id)) " +
			" WHERE isb.status = 'O' AND isb.center_id = ? AND  # " +
			" ORDER BY created_date DESC LIMIT 1 ";

		if (resubmission != null && resubmission.equals("Y"))
		  getSubmissionQuery = getSubmissionQuery.replace("#", " is_resubmission = 'Y' ");
		else
		  getSubmissionQuery = getSubmissionQuery.replace("#", " is_resubmission = 'N' ");

		return DataBaseUtil.queryToDynaBean(getSubmissionQuery, new Object[] { claimId, centerId });
	}

	private static final String HOSP_CLAIM_STATUS_COUNT = " SELECT count(bcl.claim_status) FROM bill_charge_claim bcl " +
	" JOIN bill_charge bc ON (bc.charge_id=bcl.charge_id AND bc.status != 'X') WHERE bcl.claim_status='D' " +
	" AND bcl.charge_head NOT IN ('PHCMED','PHMED','PHCRET','PHRET') AND bcl.claim_id=? ";

	private static final String PHARM_CLAIM_STATUS_COUNT = " SELECT count(scd.claim_status) FROM sales_claim_details scd " +
	    " JOIN store_sales_details ssd using (sale_item_id) "  +
		" JOIN store_sales_main ssm using (sale_id) WHERE scd.claim_status='D' AND ssm.type != 'R' AND scd.claim_id=?  ";

	private static final String HOSP_CLOSURE_TYPE_COUNT = " SELECT count(bcl.closure_type) FROM bill_charge_claim bcl " +
		" JOIN bill_charge bc ON (bc.charge_id=bcl.charge_id AND bc.status != 'X') WHERE bcl.closure_type IN ('M','D') " +
		" AND bcl.charge_head NOT IN ('PHCMED','PHMED','PHCRET','PHRET') AND claim_id=? ";

	private static final String PHARM_CLOSURE_TYPE_COUNT = " SELECT count(scd.closure_type) FROM sales_claim_details scd " +
	    " JOIN store_sales_details ssd using (sale_item_id) "  +
		" JOIN store_sales_main ssm using (sale_id) WHERE scd.closure_type IN ('M','D') AND ssm.type != 'R' AND scd.claim_id=? ";

	public boolean isInternalCompAllowed(String claimId) throws SQLException {
	Connection con=null;

	int hospClaimCount=0;
	int pharmClaimCount=0;
	int hospClosureCount=0;
	int pharmClosureCount=0;

	try{
		con = DataBaseUtil.getConnection();
		hospClaimCount = getHospitalClaimStatusCount(con, claimId);
		
		pharmClaimCount = getPharmacyClaimStatusCount(con, claimId);

		hospClosureCount = getHospitalClosureCount(con, claimId);
		
		pharmClosureCount = getPharmClosureCount(con, claimId);

		/* when total count of denied activities = total count of denial accepted then we should not allow
		 * resubmission with internal complaint */
		//
		return !((hospClaimCount+pharmClaimCount) != 0 && (hospClaimCount+pharmClaimCount)==(hospClosureCount+pharmClosureCount));

		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	private int getPharmClosureCount(Connection con, String claimId) throws SQLException{
	  int pharmClosureCount = 0;
	  ResultSet rs = null;
	  try(PreparedStatement ps = con.prepareStatement(PHARM_CLOSURE_TYPE_COUNT)){
	    ps.setString(1, claimId);
	    rs = ps.executeQuery();
	    while (rs.next()){
	      pharmClosureCount = rs.getInt(1);
	    }
	  }finally{
	    if(rs!= null){
	      rs.close();
	    }
	  }
	  return pharmClosureCount;
  }


  private int getHospitalClosureCount(Connection con, String claimId) throws SQLException{
    ResultSet rs = null;
    int hospClosureCount = 0;
    try(PreparedStatement ps = con.prepareStatement(HOSP_CLOSURE_TYPE_COUNT)){
      ps.setString(1, claimId);
      rs = ps.executeQuery();
      while (rs.next()){
        hospClosureCount = rs.getInt(1);
      }
    }finally{
      if(rs != null){
        rs.close();
      }
    }
    return hospClosureCount;
  }


  private int getPharmacyClaimStatusCount(Connection con, String claimId) throws SQLException{
	  ResultSet rs = null;
	  int pharmClaimCount = 0;
	  try(PreparedStatement ps = con.prepareStatement(PHARM_CLAIM_STATUS_COUNT)){
	    ps.setString(1, claimId);
	    rs = ps.executeQuery();
	    while (rs.next()){
	      pharmClaimCount = rs.getInt(1);
	    }
	  }finally{
	    if(null != rs){
        rs.close();
      }
	  }
	  return pharmClaimCount;
  }


  private int getHospitalClaimStatusCount(Connection con, String claimId) throws SQLException{
	  ResultSet rs = null;
	  int hospClaimCount = 0;
	  try(PreparedStatement ps = con.prepareStatement(HOSP_CLAIM_STATUS_COUNT)){
	    ps.setString(1, claimId);
	    rs = ps.executeQuery();
	    while (rs.next()){
	      hospClaimCount = rs.getInt(1);
	    }
	  }finally{
	    if(null != rs){
	      rs.close();
	    }
	  }
	  return hospClaimCount;
  }

  private static final String HOSP_EXCESS_AMT_ACTIVITIES_COUNT = " SELECT count(*) FROM bill_charge_claim bcl " +
		" JOIN bill_charge bc ON (bc.charge_id=bcl.charge_id AND bc.status != 'X') WHERE bcl.claim_status='D' " +
		" AND coalesce(bcl.insurance_claim_amt, 0.00) <= coalesce(bcl.claim_recd_total, 0.00) " +
		" AND bcl.closure_type NOT IN ('M','D') " +
		" AND bcl.charge_head NOT IN ('PHCMED','PHMED','PHCRET','PHRET') " +
		" AND bc.submission_batch_type != 'P' AND bc.charge_head != 'PKGPKG' "+
		" AND bcl.claim_id=? ";

	private static final String PHARM_EXCESS_AMT_ACTIVITIES_COUNT = " SELECT count(*) " +
		" FROM sales_claim_details scd " +
		" JOIN store_sales_details ssd using (sale_item_id) "  +
		" JOIN store_sales_main ssm using (sale_id) " +
		" WHERE scd.claim_status='D' " +
		" AND (coalesce(scd.insurance_claim_amt, 0.00) <= coalesce(scd.claim_recd, 0.00)) " +
		" AND scd.closure_type NOT IN ('M','D') " +
		" AND ssm.type != 'R' AND scd.claim_id=?  ";
	
	private static final String PKG_EXCESS_AMT_ACTIVITIES_COUNT = " SELECT count(*) "
	    + " FROM bill_charge_claim bcl "
	    + " JOIN bill_charge bc ON (bc.charge_id=bcl.charge_id AND bc.status != 'X') "
	    + " WHERE bcl.claim_status='D' AND bcl.closure_type NOT IN ('M','D') "
	    + " AND bcl.charge_head NOT IN ('PHCMED','PHMED','PHCRET','PHRET') "
	    + " AND coalesce(bcl.insurance_claim_amt, 0.00) < coalesce(bcl.claim_recd_total, 0.00) "
	    + " AND bc.submission_batch_type = 'P' AND bcl.claim_id = ? ";

	public boolean hasExcessAmtNotDenialAcceptedActivities(String claimId) throws SQLException {
		Connection con=null;
		int hospClaimCount=0;
		int pharmClaimCount=0;
		int pkgClaimCount=0;
		try{
			con = DataBaseUtil.getConnection();
			hospClaimCount = getHospExcessAmtActivitiesCount(con, claimId);
			pharmClaimCount = getPharExcessAmtActivitiesCount(con, claimId);
			pkgClaimCount = getPkgExcessAmtActivitiesCount(con, claimId);
			return (hospClaimCount+pharmClaimCount+pkgClaimCount > 0);
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}

	}

  private int getPharExcessAmtActivitiesCount(Connection con, String claimId) throws SQLException{
    ResultSet rs = null;
    int pharmClaimCount = 0;
    try(PreparedStatement ps = con.prepareStatement(PHARM_EXCESS_AMT_ACTIVITIES_COUNT)){
      ps.setString(1, claimId);
      rs = ps.executeQuery();
      while (rs.next()){
        pharmClaimCount = rs.getInt(1);
      }
    }finally{
      if(rs != null){
        rs.close();
      }
    }
    return pharmClaimCount;
  }


  private int getHospExcessAmtActivitiesCount(Connection con, String claimId) throws SQLException{
    ResultSet rs = null;
    int hospClaimCount = 0;
    try(PreparedStatement ps = con.prepareStatement(HOSP_EXCESS_AMT_ACTIVITIES_COUNT)){
      ps.setString(1, claimId);
      rs = ps.executeQuery();
      while (rs.next()){
        hospClaimCount = rs.getInt(1);
      }
    }finally {
      if(rs != null){
        rs.close();
      }
    }
    return hospClaimCount;
  }
  
  private int getPkgExcessAmtActivitiesCount(Connection con, String claimId) throws SQLException {
    ResultSet rs = null;
    int pkgClaimCount = 0;
    try(PreparedStatement ps = con.prepareStatement(PKG_EXCESS_AMT_ACTIVITIES_COUNT)){
      ps.setString(1, claimId);
      rs = ps.executeQuery();
      while (rs.next()){
        pkgClaimCount = rs.getInt(1);
      }
    } finally {
      if(rs != null){
        rs.close();
      }
    }
    return pkgClaimCount;
  }

}
