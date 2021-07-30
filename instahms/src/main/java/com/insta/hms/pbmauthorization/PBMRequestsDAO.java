/**
 *
 */
package com.insta.hms.pbmauthorization;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;


/**
 * @author lakshmi
 *
 */
public class PBMRequestsDAO {

	static Logger logger = LoggerFactory.getLogger(PBMRequestsDAO.class);

	private Connection con = null;

	public PBMRequestsDAO() {}

	public PBMRequestsDAO(Connection con) {
		this.con = con;
	}

	public String getGeneratedPBMRequestId() throws SQLException {
		return DataBaseUtil.getNextPatternId("pbm_request_approval_details");
	}

	public static String fillPBMRequestIdSearch(String pbmRequestId) {
		if((pbmRequestId == null) || (pbmRequestId.equals("")))
			return null;
		int pbmRequestIdDigits = DataBaseUtil.getStringValueFromDb("SELECT num_pattern FROM hosp_id_patterns " +
				" WHERE pattern_id ='pbm_request_approval_details'").length();

		if(pbmRequestId.length() < pbmRequestIdDigits){
			String requestID = DataBaseUtil.getStringValueFromDb("SELECT std_prefix||''||trim((TO_CHAR("
				+pbmRequestId+",num_pattern))) FROM hosp_id_patterns WHERE pattern_id ='pbm_request_approval_details'");
			if(requestID != null && !requestID.equals(""))
				return requestID;
		}
		return pbmRequestId;
	}

	private static final String SELECT_PBM_REQUEST_FIELDS = "SELECT * ";

	private static final String SELECT_PBM_REQUEST_COUNT = " SELECT count(*) ";

	private static final String SELECT_PBM_REQUEST_TABLES = " FROM (SELECT " +
	    " sm.salutation || ' ' || patient_name || case when coalesce(middle_name, '') = '' " +
	    " then '' else (' ' || middle_name) end || case when coalesce(last_name, '') = '' then '' " +
	    " else (' ' || last_name) end as patname, " +
	    " CASE WHEN sts = 1 THEN 'N' WHEN sts = '3' THEN 'Y' ELSE 'P' END as presc_status, " +
	    " pr.mr_no, pr.patient_id, pr.op_type, pr.status as patstatus, pr.center_id, " +
		" CASE WHEN (pr.op_type != 'O') THEN d.doctor_name ELSE ref.referal_name END AS doctor," +
		" CASE WHEN (pr.op_type != 'O') THEN dc.visited_date ELSE (pr.reg_date + pr.reg_time) END AS visited_date," +
	    " grp.consultation_id, pbmp.pbm_finalized," +
	    " pbmp.pbm_presc_id, pbmp.status AS pbm_presc_status, pbmp.resubmit_type," +
	    " pr.primary_insurance_co AS insurance_co_id, pr.primary_sponsor_id AS tpa_id , " +
	    " pr.plan_id, pr.category_id :: text, " +
		" icm.insurance_co_name, tp.tpa_name, pm.plan_name, cat.category_name," +
		" prd.request_date, pbmp.pbm_request_id, prd.pbm_request_type, prd.pbm_auth_id_payer,  " +
		" pbmp.drug_count, pbmp.comments, pbmp.resubmit_type, prd.is_resubmit," +
		" prd.approval_recd_date, prd.approval_status, pbmMedDetails.claim_status, " +
		" CASE WHEN total_qty = 0 THEN true ELSE false END AS all_items_returned "+
		" FROM (SELECT pbm_presc_id, consultation_id, visit_id, " +
		"			avg(CASE WHEN issued IN ('Y', 'C') THEN 3 WHEN issued = 'P' THEN 2 ELSE 1 END) as sts " +
	    " 		FROM pbm_medicine_prescriptions " +
	    " 		GROUP by pbm_presc_id, consultation_id, visit_id " +
	    "		) as grp" +
	    " 	JOIN patient_registration pr ON (grp.visit_id = pr.patient_id)" +
	    "   JOIN patient_insurance_plans pip ON (pip.patient_id = pr.patient_id AND pip.priority = 1) "+
	    " 	JOIN patient_details pd ON (pr.mr_no = pd.mr_no) " +
	    " 	JOIN pbm_prescription pbmp ON (grp.pbm_presc_id = pbmp.pbm_presc_id ) " +
	    " 	LEFT JOIN doctor_consultation dc ON (dc.consultation_id = grp.consultation_id) " +
	    " 	LEFT JOIN doctors d ON (d.doctor_id = pr.doctor)" +
		" 	LEFT JOIN (" +
		"		SELECT referal_name,referal_no  FROM referral" +
		"		UNION" +
		"		SELECT doctor_name, doctor_id FROM doctors" +
		" 	) AS ref ON (ref.referal_no = pr.reference_docto_id)" +
	    " 	LEFT JOIN salutation_master sm ON (pd.salutation = sm.salutation_id) " +
		"	LEFT JOIN tpa_master tp ON (tp.tpa_id = pip.sponsor_id)" +
		"	LEFT JOIN insurance_company_master icm ON (icm.insurance_co_id = pip.insurance_co)" +
		"	LEFT JOIN insurance_category_master cat ON (cat.category_id = pip.plan_type_id)" +
		"	LEFT JOIN insurance_plan_main pm ON (pm.plan_id = pip.plan_id)" +
		"	LEFT JOIN pbm_request_approval_details prd on (prd.pbm_request_id = pbmp.pbm_request_id) " +
		"	LEFT JOIN ( SELECT sum(ssd.quantity+ssd.return_qty ) as total_qty,pms.pbm_presc_id, bcl.claim_status "+
						" FROM  pbm_medicine_sales pms "+
						" JOIN store_sales_details ssd ON(ssd.sale_id = pms.sale_id) "+
						" JOIN store_sales_main ssm ON(ssm.sale_id = ssd.sale_id) "+
						" JOIN bill_claim bcl ON(bcl.bill_no = ssm.bill_no AND bcl.priority=1) "+
						" GROUP BY pms.pbm_presc_id, bcl.claim_status) AS pbmMedDetails ON (pbmMedDetails.pbm_presc_id = pbmp.pbm_presc_id) " +
			" WHERE pd.patient_group  in (SELECT confidentiality_grp_id from user_confidentiality_association " +
			" WHERE emp_username = current_setting('application.username') UNION SELECT 0)" +
	    " ) as list ";

	public static PagedList searchPBMRequestList(Map filter, Map listing)
	throws SQLException, ParseException {

		Connection con = DataBaseUtil.getReadOnlyConnection();

		SearchQueryBuilder qb = new SearchQueryBuilder(con,
				SELECT_PBM_REQUEST_FIELDS, SELECT_PBM_REQUEST_COUNT, SELECT_PBM_REQUEST_TABLES, listing);

		qb.addFilterFromParamMap(filter);
		int centerId = RequestContext.getCenterId();
		if (centerId != 0)
			qb.addFilter(SearchQueryBuilder.INTEGER, "center_id", "=", centerId);
		qb.addSecondarySort("request_date");
		qb.build();

		PagedList l = qb.getMappedPagedList();

		qb.close();
		con.close();

		return l;
    }

	private static final String GET_NOT_RETURNED_MEDICINES = "SELECT distinct medicine_name "+
		" FROM pbm_medicine_sales pms "+
		" JOIN pbm_medicine_prescriptions pmp ON(pms.pbm_medicine_pres_id = pmp.pbm_medicine_pres_id) "+
		" JOIN store_sales_details ssd ON(ssd.sale_id = pms.sale_id AND ssd.medicine_id = pmp.medicine_id) "+
		" JOIN store_item_details sitd ON(sitd.medicine_id = ssd.medicine_id) "+
		" WHERE (ssd.quantity+ssd.return_qty) > 0 AND  pms.pbm_presc_id = ? ";

	public static List<BasicDynaBean> getNotReturnedMedicineNames(String prescID) throws SQLException{

		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_NOT_RETURNED_MEDICINES);
			ps.setInt(1, Integer.parseInt(prescID));
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}
}
