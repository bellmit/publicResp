/**
 *
 */
package com.insta.hms.pbmauthorization;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author lakshmi
 *
 */
public class PBMApprovalsDAO {

	static Logger logger = LoggerFactory.getLogger(PBMApprovalsDAO.class);

	private static final String PBM_PRESC_ID = "pbm_presc_id";
	private static final String PBM_REQUEST_ID = "pbm_request_id";
	private static final String PBM_MEDICINE_PRES_ID = "pbm_medicine_pres_id";
	private static final String PBM_STATUS = "pbm_status";

	private static final String SELECT_PBM_APPROVAL_FIELDS = "SELECT * ";

	private static final String SELECT_PBM_APPROVAL_COUNT = " SELECT count(*) ";

	private static final String SELECT_PBM_APPROVAL_TABLES = " FROM (SELECT " +
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
	    " pr.plan_id, pr.category_id :: text, ppd.member_id, " +
	    " icm.insurance_co_name, tp.tpa_name, pm.plan_name, cat.category_name," +
		" prd.request_date, pbmp.pbm_request_id, prd.pbm_request_type, prd.pbm_auth_id_payer,  " +
		" pbmp.drug_count, pbmp.comments, pbmp.resubmit_type, prd.is_resubmit," +
		" prd.approval_recd_date, prd.approval_status, prd.file_id, prd.approval_comments " +
		" FROM (SELECT pbm_presc_id, consultation_id, visit_id, " +
		"			avg(CASE WHEN issued IN ('Y', 'C') THEN 3 WHEN issued = 'P' THEN 2 ELSE 1 END) as sts " +
	    " 		FROM pbm_medicine_prescriptions " +
	    " 		GROUP by pbm_presc_id, consultation_id, visit_id " +
	    "		) as grp" +
	    " 	JOIN patient_registration pr ON (grp.visit_id = pr.patient_id)" +
	    " 	JOIN patient_details pd ON (pr.mr_no = pd.mr_no) " +
	    " 	JOIN pbm_prescription pbmp ON (grp.pbm_presc_id = pbmp.pbm_presc_id ) " +
	    " 	LEFT JOIN doctor_consultation dc ON (dc.consultation_id = grp.consultation_id) " +
	    " 	LEFT JOIN doctors d ON (d.doctor_id = pr.doctor)" +
		" 	LEFT JOIN (" +
		"		SELECT referal_name, referal_no FROM referral" +
		"		UNION" +
		"		SELECT doctor_name, doctor_id FROM doctors" +
		" 	) AS ref ON (ref.referal_no = pr.reference_docto_id)" +
	    " 	LEFT JOIN salutation_master sm ON (pd.salutation = sm.salutation_id) " +
	    " 	LEFT JOIN patient_insurance_plans pip ON (pr.patient_id = pip.patient_id AND pip.priority=1 ) " +
		" 	LEFT JOIN patient_policy_details ppd ON (ppd.patient_policy_id = pip.patient_policy_id) "+
		"	LEFT JOIN tpa_master tp ON (tp.tpa_id = pr.primary_sponsor_id)" +
		"	LEFT JOIN insurance_company_master icm ON (icm.insurance_co_id = pr.primary_insurance_co)" +
		"	LEFT JOIN insurance_category_master cat ON (cat.category_id = pr.category_id)" +
		"	LEFT JOIN insurance_plan_main pm ON (pm.plan_id = pr.plan_id)" +
		"	LEFT JOIN pbm_request_approval_details prd on (prd.pbm_request_id = pbmp.pbm_request_id) " +
	    " ) as list ";


	public static PagedList searchPBMApprovalList(Map filter, Map listing)
	throws SQLException, ParseException {

		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();

			SearchQueryBuilder qb = new SearchQueryBuilder(con,
					SELECT_PBM_APPROVAL_FIELDS, SELECT_PBM_APPROVAL_COUNT, SELECT_PBM_APPROVAL_TABLES, listing);

			qb.addFilterFromParamMap(filter);
			int centerId = RequestContext.getCenterId();
			if (centerId != 0)
				qb.addFilter(SearchQueryBuilder.INTEGER, "center_id", "=", centerId);
			qb.addSecondarySort("approval_recd_date", true);
			qb.addSecondarySort(PBM_PRESC_ID, true);
			qb.build();

			PagedList l = qb.getMappedPagedList();

			qb.close();
			return l;
		}finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	PBMPrescriptionsDAO pbmdao = new PBMPrescriptionsDAO();
	GenericDAO pbmReqDao = new GenericDAO("pbm_request_approval_details");
	GenericDAO prescReqDao = new GenericDAO("pbm_prescription_request");
	GenericDAO pbmMedPrescDAO = new GenericDAO("pbm_medicine_prescriptions");
	GenericDAO reqAmtDao = new GenericDAO("pbm_approval_amount_details");

	public boolean updatePBMApprovalDetails(PriorAuthorization desc)
				throws SQLException, IOException, ParseException {
		boolean success = true;
		boolean allSuccess = false;

		Connection con = null;

		try {
		  approvalTxn:{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			SimpleDateFormat timeStampFormatterSecs = new SimpleDateFormat("dd/MM/yyyy HH:mm");
			PriorAuthorizationHeader header = desc.getHeader();
			String transactionDate = header.getTransactionDate();
			if(transactionDate.trim().length() <= 10)
				transactionDate = transactionDate + " 00:00";

			Timestamp txnDate = new java.sql.Timestamp(timeStampFormatterSecs.parse(transactionDate).getTime());

			PriorAuthAuthorization priorAuth = desc.getAuthorization();

			String authReqID = priorAuth.getAuthorizationID();
			String authIdPayer = priorAuth.getAuthorizationIDPayer();
			String authDenialCode = priorAuth.getDenialCode();
			String authResult = priorAuth.getAuthorizationResult();
			BigDecimal limit = priorAuth.getLimit();
			String comments = priorAuth.getComments();

			ArrayList<PriorAuthorizationActivity> activities = priorAuth.getActivities();

			BasicDynaBean pbmReqBean = pbmReqDao.findByKey(PBM_REQUEST_ID, authReqID);
			BasicDynaBean prescReqbean = prescReqDao.findByKey(PBM_REQUEST_ID, authReqID);

			int pbmPrescId = (Integer)prescReqbean.get(PBM_PRESC_ID);
			BasicDynaBean pbmbean = pbmdao.getPBMPresc(pbmPrescId);
			int drugCount = (Integer)pbmbean.get("drug_count");
			String pbmResubmitRequestId =
				pbmbean.get("pbm_resubmit_request_id") != null ? (String)pbmbean.get("pbm_resubmit_request_id") : null;

			// Get the resubmission request id or the request id for saving approval amount details.
    		String pbmPrescriptionRequestId =
    			(pbmResubmitRequestId != null &&  !pbmResubmitRequestId.equals("")) ? pbmResubmitRequestId : authReqID;

			int approvedActivityCount = 0;

			/* Insert activity details into pbm approval amount details table. */
			for (PriorAuthorizationActivity activity : activities) {
				String activityId = activity.getActivityID();
				BigDecimal net = activity.getNet();
				String activityDenialCode = activity.getActivityDenialCode();
				BigDecimal quantity = activity.getQuantity();
				BigDecimal list = activity.getList();
				BigDecimal patientShare = activity.getPatientShare();
				BigDecimal paymentAmount = activity.getPaymentAmount();

				String actId = activityId.split("-")[0];
				int medPresId = Integer.parseInt(actId);

				BasicDynaBean actAmtBean = reqAmtDao.getBean();
				actAmtBean.set(PBM_REQUEST_ID, pbmPrescriptionRequestId);
				actAmtBean.set(PBM_MEDICINE_PRES_ID, medPresId);
				actAmtBean.set("denial_code", activityDenialCode);
				actAmtBean.set("pbm_auth_id_payer", authIdPayer);
				actAmtBean.set("quantity", quantity);
				actAmtBean.set("net", net);
				actAmtBean.set("list", list);
				actAmtBean.set("patient_share", patientShare);
				actAmtBean.set("payment_amount", paymentAmount);

				activityDenialCode = (activityDenialCode == null) ? authDenialCode : activityDenialCode;

				success = reqAmtDao.insert(con, actAmtBean);
				if (!success)
					break approvalTxn;


				BasicDynaBean activityBean = pbmMedPrescDAO.findByKey(PBM_MEDICINE_PRES_ID, medPresId);
				/* The denied activities need to be resubmitted.
				 * The Approved activitied rate details needs to be saved.
				 * No check required if the sent claim amount mismatches with approved claim amount.*/
				if ( activityDenialCode == null || activityDenialCode.equals("") ) {

					approvedActivityCount++;
					activityBean.set("claim_net_approved_amount", paymentAmount);
					activityBean.set(PBM_STATUS, "C");

					BigDecimal actAmt = (patientShare != null) ? patientShare.add(paymentAmount) : paymentAmount;
					BigDecimal pkgsize =BigDecimal.ZERO;
					if(null !=activityBean.get("package_size"))
						 pkgsize =(BigDecimal) activityBean.get("package_size");

					BigDecimal actRate = BigDecimal.ZERO;
					if(quantity.compareTo(BigDecimal.ZERO) == 0) {
						// take requested quantity if approved quantity is zero.
						quantity = (BigDecimal)activityBean.get("medicine_quantity");
						actRate = ConversionUtils.setScale(net.divide(quantity.multiply(pkgsize), RoundingMode.HALF_UP));
					}
					else if(pkgsize.compareTo(BigDecimal.ZERO) != 0)
						actRate = ConversionUtils.setScale(net.divide(quantity.multiply(pkgsize), RoundingMode.HALF_UP));
					else
						actRate = ConversionUtils.setScale(net.divide(quantity, RoundingMode.HALF_UP));

					BigDecimal discount = BigDecimal.ZERO;
					if(net.compareTo(actAmt) > 0)
							discount=net.subtract(actAmt);

					activityBean.set("amount", actAmt);
					activityBean.set("rate", actRate);
					activityBean.set("claim_net_amount", paymentAmount);
					activityBean.set("discount", discount);

				}else {
					 activityBean.set("claim_net_approved_amount", paymentAmount);
					 activityBean.set("denial_code", activityDenialCode);
					 activityBean.set(PBM_STATUS, "D");
				}

				/* Update patient medicine prescriptions table with net and denials. */
				int i = pbmMedPrescDAO.updateWithName(con, activityBean.getMap(), PBM_MEDICINE_PRES_ID);
				success = (i > 0);
				if (!success)
					break approvalTxn;
			}

			/* Mark PBM Approval as F/P/R: Fully Approved/Partially Approved/Fully Rejected */
			String approvalStatus = "";
			if (approvedActivityCount == drugCount) {
				approvalStatus = "F";
			}else if (approvedActivityCount == 0) {
				approvalStatus = "R";
			}else {
				approvalStatus = "P";
			}

			/* Update pbm_request_approval_details table with approval details. */
			pbmReqBean.set(PBM_REQUEST_ID, authReqID);
			pbmReqBean.set("approval_result", authResult);
			pbmReqBean.set("pbm_auth_id_payer", authIdPayer);
			pbmReqBean.set("approval_recd_date", txnDate);
			pbmReqBean.set("approval_comments", comments);
			pbmReqBean.set("approval_limit", limit);
			pbmReqBean.set("approval_status", approvalStatus);

			int k = pbmReqDao.updateWithName(con, pbmReqBean.getMap(), PBM_REQUEST_ID);
			success = (k > 0);
			if (!success)
				break approvalTxn;

			List<BasicDynaBean> pbmPrescActivities =  pbmMedPrescDAO.findAllByKey(con, PBM_PRESC_ID, pbmPrescId);
			boolean denied = false;
			for (BasicDynaBean activity : pbmPrescActivities) {
				String status = (String)activity.get(PBM_STATUS);
				if (status.equals("D")) {
					denied = true;
					break;
				}
			}

			String pbmPrescStatus = "";
			if (!denied) {
				pbmPrescStatus = "C"; // Closed
			}else {
				pbmPrescStatus = "D"; // Denied
			}

			/* Update pbm_prescription table with approval details. */
			BasicDynaBean pbmBean = pbmdao.getBean();
			pbmBean.set(PBM_PRESC_ID, pbmPrescId);
			pbmBean.set("status", pbmPrescStatus);
			int n = pbmdao.updateWithName(con, pbmBean.getMap(), PBM_PRESC_ID);
			success = (n > 0);
			if (!success)
				break approvalTxn;

		  }// label approvalTxn
		  allSuccess = true;

		}finally {
			DataBaseUtil.commitClose(con, allSuccess);
		}

		return success;
	}

	private static final String GET_PBM_APPROVAL =
		" SELECT * FROM pbm_approval_amount_details " +
		" WHERE pbm_medicine_pres_id = ? ORDER BY pbm_request_id DESC LIMIT 1 ";

	public static BasicDynaBean getPBMApprovalBean(int pbmMedicinePresId)
			throws SQLException {
		return DataBaseUtil.queryToDynaBean(GET_PBM_APPROVAL, pbmMedicinePresId);
	}
}
