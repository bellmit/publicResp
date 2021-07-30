/**
 *
 */
package com.insta.hms.pbmauthorization;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DynaBeanBuilder;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.master.Accounting.AccountingGroupMasterDAO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.HealthAuthorityPreferences.HealthAuthorityDTO;
import com.insta.hms.master.HealthAuthorityPreferences.HealthAuthorityPreferencesDAO;
import com.insta.hms.master.PlanMaster.PlanDetailsDAO;
import com.insta.hms.master.PlanMaster.PlanMasterDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDTO;
import com.insta.hms.master.StoreItemRates.StoreItemRatesDAO;
import com.insta.hms.master.StoreMaster.StoreMasterDAO;
import com.insta.hms.medicalrecorddepartment.MRDDiagnosisDAO;
import com.insta.hms.stores.PharmacymasterDAO;
import com.insta.hms.stores.StoreItemCodesDAO;
import com.insta.hms.usermanager.User;
import com.insta.hms.usermanager.UserDAO;
import freemarker.template.TemplateException;
import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author lakshmi
 *
 */
public class PBMPrescriptionsDAO extends GenericDAO {

	static Logger logger = LoggerFactory.getLogger(PBMPrescriptionsDAO.class);

	GenericDAO pbmReqDAO = new GenericDAO("pbm_request_approval_details");

	GenericDAO pbmMedPrescDAO = new GenericDAO("pbm_medicine_prescriptions");
	PharmacymasterDAO storeItemDAO = new PharmacymasterDAO();
	GenericDAO genericNameDAO = new GenericDAO("generic_name");

	StoreMasterDAO storeDao = new StoreMasterDAO();
	GenericDAO stockDao = new GenericDAO("store_stock_details");
	GenericDAO itembatchDao = new GenericDAO("store_item_batch_details");
	GenericDAO storeItemCatDao = new GenericDAO("store_category_master");
	StoreItemRatesDAO storeRateDao = new StoreItemRatesDAO();
	PlanMasterDAO planDao = new PlanMasterDAO();
	PlanDetailsDAO planDetDao = new PlanDetailsDAO();
	GenericDAO itemInsCatDao = new GenericDAO("item_insurance_categories");
	MRDDiagnosisDAO mrdDao = new MRDDiagnosisDAO();

	public PBMPrescriptionsDAO() {
		super("pbm_prescription");
	}

	private static final String GET_LATEST_PBM_PRESC_ID =
		" SELECT pbmp.pbm_presc_id " +
		"	FROM patient_prescription pp " +
		"	JOIN patient_medicine_prescriptions pmp ON (pp.patient_presc_id=pmp.op_medicine_pres_id)" +
		" JOIN pbm_prescription pbmp USING(pbm_presc_id) " +
		" WHERE pbmp.status = 'O' AND (pbm_request_id IS NULL OR pbm_request_id = '') " +
		" AND pp.consultation_id = ? ORDER BY pbm_presc_id DESC LIMIT 1";

	public int getLatestPBMPrescId(int consultation_id) throws SQLException {
		Connection con = null;
	    try {
	        con = DataBaseUtil.getReadOnlyConnection();
	        return getLatestPBMPrescId(con, consultation_id);
	    } finally {
	        DataBaseUtil.closeConnections(con, null);
	    }
	}

	public int getLatestPBMPrescId(Connection con, int consultation_id) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_LATEST_PBM_PRESC_ID);
			ps.setInt(1, consultation_id);
			return DataBaseUtil.getIntValueFromDb(ps);
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	private static final String SELECT_PBM_PRESCRIPTION_FIELDS = "SELECT * ";

	private static final String SELECT_PBM_PRESCRIPTION_COUNT = " SELECT count(*) ";

	private static final String SELECT_PBM_PRESCRIPTION_TABLES = " FROM (SELECT " +
	    " sm.salutation || ' ' || patient_name || case when coalesce(middle_name, '') = '' " +
	    " then '' else (' ' || middle_name) end || case when coalesce(last_name, '') = '' then '' " +
	    " else (' ' || last_name) end as patname, " +
	    " get_patient_age(dateofbirth, expected_dob) as age," +
		" get_patient_age_in(dateofbirth, expected_dob) as age_in,pd.patient_phone,pd.patient_gender, d.doctor_id, " +
	    " CASE WHEN sts = 1 THEN 'N' WHEN sts = '3' THEN 'Y' ELSE 'P' END as presc_status, " +
	    " pr.mr_no, pr.patient_id, pr.op_type, pr.status as patstatus, pr.center_id, " +
		" CASE WHEN (pr.op_type != 'O') THEN d.doctor_name ELSE ref.referal_name END AS doctor," +
		" CASE WHEN (pr.op_type != 'O') THEN dc.visited_date ELSE (pr.reg_date + pr.reg_time) END AS visited_date," +
	    " grp.consultation_id, pbmp.pbm_finalized," +
	    " pbmp.pbm_presc_id, pbmp.status AS pbm_presc_status, pbmp.resubmit_type," +
	    " pbmp.pbm_store_id, s.dept_name AS pbm_store_name, " +
	    " pbmp.erx_presc_id, pbmp.erx_reference_no, pbmp.erx_approval_status, pbmp.erx_request_type, " +
	    " pr.primary_insurance_co AS insurance_co_id, pr.primary_sponsor_id AS tpa_id , " +
	    " pr.plan_id, pr.category_id :: text, " +
		" icm.insurance_co_name, tp.tpa_name, pm.plan_name, cat.category_name, pbmp.erx_request_date " +
		" FROM (SELECT pbm_presc_id, consultation_id, visit_id, " +
		"			avg(CASE WHEN issued IN ('Y', 'C') THEN 3 WHEN issued = 'P' THEN 2 ELSE 1 END) as sts " +
	    " 		FROM pbm_medicine_prescriptions " +
	    " 		GROUP by pbm_presc_id, consultation_id, visit_id " +
	    "		) as grp" +
	    " JOIN patient_registration pr ON (grp.visit_id = pr.patient_id)" +
	    " JOIN patient_details pd ON (pr.mr_no = pd.mr_no) " +
	    " JOIN pbm_prescription pbmp ON (grp.pbm_presc_id = pbmp.pbm_presc_id ) " +
	    " JOIN tpa_master tp ON (tp.tpa_id = pr.primary_sponsor_id)" +
		" JOIN insurance_company_master icm ON (icm.insurance_co_id = pr.primary_insurance_co)" +
		" JOIN insurance_category_master cat ON (cat.category_id = pr.category_id)" +
		" JOIN insurance_plan_main pm ON (pm.plan_id = pr.plan_id)" +
	    " LEFT JOIN doctor_consultation dc ON (dc.consultation_id = grp.consultation_id) " +
	    " LEFT JOIN doctors d ON (d.doctor_id = pr.doctor)" +
		" LEFT JOIN (" +
		"	SELECT referal_name,referal_no FROM referral" +
		"	UNION" +
		"	SELECT doctor_name,doctor_id FROM doctors" +
		" ) AS ref ON (ref.referal_no = pr.reference_docto_id)" +
	    " LEFT JOIN salutation_master sm ON (pd.salutation = sm.salutation_id) " +
	    " LEFT JOIN stores s ON (s.dept_id = pbmp.pbm_store_id)" +
	    " WHERE " +
		" patient_confidentiality_check(pd.patient_group,pd.mr_no) ) as list";


	private static final String PBM_MODULE_CONDITION = " AND pm.require_pbm_authorization = 'Y' ";

  public static List<BasicDynaBean> getPbmPrescriptions() throws SQLException {

    String query = null;
    List<Object> params = new ArrayList<>();
    try (Connection con = DataBaseUtil.getReadOnlyConnection()){
      User user = new UserDAO(con).getUser(RequestContext.getUserName());
      params.add(RequestContext.getCenterId());
      query = SELECT_PBM_PRESCRIPTION_FIELDS + SELECT_PBM_PRESCRIPTION_TABLES.replace("@", "")
      + " where center_id = ?" 
      + " AND erx_request_date >= NOW() - INTERVAL '15 DAY' and erx_request_date <= NOW()";
      if (user != null && user.getDoctorId() != null && !user.getDoctorId().isEmpty()) {
        query = query + " AND doctor_id = ?";
        params.add(user.getDoctorId());
      }
    }
    return DataBaseUtil.queryToDynaList(query, params.toArray());
  }
	
	public static PagedList searchPBMPrescriptionList(Map filter, Map listing)
	throws SQLException, ParseException {

		boolean mod_eclaim_pbm = (Boolean)RequestContext.getSession().getAttribute("mod_eclaim_pbm");
		boolean mod_eclaim_erx = (Boolean)RequestContext.getSession().getAttribute("mod_eclaim_erx");

		Connection con = DataBaseUtil.getReadOnlyConnection();

		String doctorId = null;
		User user = new UserDAO(con).
							getUser((String)RequestContext.getSession().getAttribute("userid"));
		if (user != null)
			doctorId = user.getDoctorId();

		// PBM prescriptions if plan requires, visit type is 'o'
		SearchQueryBuilder qb = null;

		if (mod_eclaim_erx) {
			qb = new SearchQueryBuilder(con,
				SELECT_PBM_PRESCRIPTION_FIELDS, SELECT_PBM_PRESCRIPTION_COUNT,
				SELECT_PBM_PRESCRIPTION_TABLES.replace("@", ""), listing);

			if (doctorId != null && !doctorId.equals(""))
				qb.addFilter(SearchQueryBuilder.STRING, "doctor_id", "=", doctorId);

		}else if (mod_eclaim_pbm) {
			qb = new SearchQueryBuilder(con,
					SELECT_PBM_PRESCRIPTION_FIELDS, SELECT_PBM_PRESCRIPTION_COUNT,
					SELECT_PBM_PRESCRIPTION_TABLES.replace("@", PBM_MODULE_CONDITION), listing);
		}

		PagedList l = new PagedList();
		if (qb != null) {
			qb.addFilterFromParamMap(filter);
			int centerId = RequestContext.getCenterId();
			if (centerId != 0)
				qb.addFilter(SearchQueryBuilder.INTEGER, "center_id", "=", centerId);
			qb.addSecondarySort("pbm_presc_id");
			qb.build();

			l = qb.getMappedPagedList();

			qb.close();
		}
		con.close();
		return l;
    }

	private static final String SEARCH_PBM_PRESCRIPTION =
		" SELECT * FROM (SELECT pbm.pbm_request_id, 1 AS record_count, file_name, " +
		" pbm_sender_id, pbm_receiver_id, file_id " +
		" FROM pbm_prescription pbm " +
		" JOIN pbm_request_approval_details prad on (prad.pbm_request_id = pbm.pbm_request_id)" +
		" ) AS foo " +
		" WHERE file_name = ? AND pbm_sender_id = ? " +
		" 	AND pbm_receiver_id = ? AND record_count = ? ";

	public String searchPBMPresc(String xmlfileName, String senderId, String receiverId,
			String recordCount) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		String pbmRequestId = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(SEARCH_PBM_PRESCRIPTION);
			ps.setString(1, xmlfileName);
			ps.setString(2, receiverId);
			ps.setString(3, senderId);
			ps.setInt(4, recordCount != null ? Integer.parseInt(recordCount) : 0);

			BasicDynaBean pbmbean = DataBaseUtil.queryToDynaBean(ps);
			if (pbmbean != null)
				pbmRequestId = (String)pbmbean.get("pbm_request_id");
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return pbmRequestId;
	}

	public BasicDynaBean searchPBMPrescBean(String xmlfileName, String senderId, String receiverId,
			String recordCount) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;

		String SEARCH_PBM_PRESCRIPTION_LIKE_FILENAME =
		"SELECT * FROM (SELECT pbm.pbm_request_id, 1 AS record_count, file_name, " +
			" pbm_sender_id, pbm_receiver_id, file_id FROM pbm_prescription pbm " +
			" JOIN pbm_request_approval_details prad on (prad.pbm_request_id = pbm.pbm_request_id)" +
			" ) AS foo  WHERE pbm_sender_id = ? AND pbm_receiver_id = ? AND record_count = ? "+
			"AND ? ilike '%' || pbm_request_id || '%'";

		logger.debug("Query SEARCH_PBM_PRESCRIPTION_LIKE_FILENAME = " + SEARCH_PBM_PRESCRIPTION_LIKE_FILENAME);
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			//ps = con.prepareStatement(SEARCH_PBM_PRESCRIPTION);
			ps = con.prepareStatement(SEARCH_PBM_PRESCRIPTION_LIKE_FILENAME);
			ps.setString(1, receiverId);
			ps.setString(2, senderId);
			ps.setInt(3, recordCount != null ? Integer.parseInt(recordCount) : 0);
			ps.setString(4, xmlfileName);

			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public boolean updatePBMRequestFileId(String pbmRequestId, String fileId) throws SQLException, IOException {
		boolean success = true;
		Connection con = null;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			BasicDynaBean pbmRequestBean = pbmReqDAO.getBean();
			pbmRequestBean.set("pbm_request_id", pbmRequestId);
			pbmRequestBean.set("file_id", fileId);

			int i = pbmReqDAO.updateWithName(con, pbmRequestBean.getMap(), "pbm_request_id");
			success = (i > 0);
		}finally {
			DataBaseUtil.commitClose(con, success);
		}

		if (!success) {
			logger.error("Error while updating PBM Request File Id "+ pbmRequestId);
		}
		return success;
	}

	public static final String GET_ATTACHMENT_SIZE = "SELECT length(attachment) as attachment_size "+
				" FROM pbm_prescription WHERE pbm_presc_id = ?";

	public int getFileSize(int pbm_presc_id) throws SQLException {
		int size = 0;
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_ATTACHMENT_SIZE);
			ps.setInt(1, pbm_presc_id);
			rs = ps.executeQuery();
			while (rs.next()){
				size = rs.getInt(1);
			}
		}finally {
			DataBaseUtil.closeConnections(con, ps, rs);
		}

		return size;
	}

	private static final String GET_ATTACHMENT =
		" SELECT attachment,attachment_content_type FROM pbm_prescription WHERE pbm_presc_id = ?";

	public Map getAttachment(int pbm_presc_id) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_ATTACHMENT);
			ps.setInt(1, pbm_presc_id);
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

	private static final String DELETE_ATTACHMENT =
		" UPDATE pbm_prescription set attachment='' , attachment_content_type='' WHERE pbm_presc_id=? ";

	public boolean deleteAttachment(int pbm_presc_id) throws SQLException {
		boolean success=false;
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(DELETE_ATTACHMENT);
			ps.setInt(1, pbm_presc_id);
			int result = ps.executeUpdate();
			if (result > 0 ) success = true;
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return success;
	}

	private static final String UPDATE_ATTACHMENT =
		" UPDATE pbm_prescription set attachment=? , attachment_content_type=? WHERE pbm_presc_id=? ";

	public boolean updateAttachment(Map params, int pbm_presc_id) throws SQLException,IOException {
		boolean success=false;
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(UPDATE_ATTACHMENT);
			InputStream stream = ((InputStream[])params.get("attachment"))[0];
			ps.setBinaryStream(1, stream, stream.available());
			ps.setString(2, ((String[])params.get("attachment_content_type"))[0]);
			ps.setInt(3, pbm_presc_id);
			int result = ps.executeUpdate();
			if (result > 0 ) success = true;
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return success;
	}

	private static final String FIND_ALL_DIAGNOSIS =
		" SELECT (CASE WHEN diag_type = 'P' THEN 'Principal' "+
		" WHEN diag_type = 'A' THEN 'Admitting' "+
		" WHEN diag_type = 'V' THEN 'Reason For Visit' "+
		" ELSE 'Secondary' END) AS diag_type, md.diag_type as diagnosis_type, " +
		" md.code_type, icd_code, code_desc " +
		" FROM mrd_diagnosis md " +
		" JOIN mrd_codes_master mcm ON (mcm.code_type = md.code_type AND mcm.code = md.icd_code) " +
		" WHERE visit_id = ?";

	public List<BasicDynaBean> findAllDiagnosis(String main_visit_id) throws Exception {
		return DataBaseUtil.queryToDynaList(FIND_ALL_DIAGNOSIS, main_visit_id);
	}

	// Query to get only those diagnosis codes that have been flagged as sent-for-approval.
	// Diagnosis codes that are present when sent-for-approval are flagged as true
	private static final String FIND_FLAGGED_DIAGNOSIS =
			" SELECT (CASE WHEN diag_type = 'P' THEN 'Principal' "+
			" WHEN diag_type = 'A' THEN 'Admitting' "+
			" WHEN diag_type = 'V' THEN 'Reason For Visit' "+
			" ELSE 'Secondary' END) AS diag_type, md.diag_type as diagnosis_type, " +
			" md.code_type, icd_code, code_desc " +
			" FROM mrd_diagnosis md " +
			" JOIN mrd_codes_master mcm ON (mcm.code_type = md.code_type AND mcm.code = md.icd_code) " +
			" WHERE visit_id = ? and sent_for_approval = true";

	public List<BasicDynaBean> findFlaggedDiagnosis(String main_visit_id) throws Exception {
		return DataBaseUtil.queryToDynaList(FIND_FLAGGED_DIAGNOSIS, main_visit_id);
	}

	private static String GET_PBM_ACCOUNT_HEADER_DETAILS =  "SELECT account_group_id, account_group_name, " +
		" pbm_sender_id AS provider_id, pbm_receiver_id AS receiver_id, " +
		" to_char(request_date::timestamp, 'dd/MM/yyyy hh24:mi') AS transaction_date, " +
		" 1 as pbm_auth_count, 'Y' AS testing, '' as health_authority " +
		" FROM pbm_request_approval_details prad" +
		" JOIN account_group_master agm ON (agm.account_group_id = prad.account_group)" +
		" WHERE pbm_request_id = ? ";

	private static String GET_PBM_HOSPITAL_CENTER_HEADER_DETAILS =  "SELECT hcm.center_id, center_name, " +
		" pbm_sender_id AS provider_id, pbm_receiver_id AS receiver_id, " +
		" to_char(request_date::timestamp, 'dd/MM/yyyy hh24:mi') AS transaction_date, " +
		" 1 as pbm_auth_count, 'Y' AS testing, '' as health_authority "+
		" FROM pbm_request_approval_details prad" +
		" JOIN hospital_center_master hcm ON (hcm.center_id = prad.center_id) " +
		" WHERE pbm_request_id = ? ";

	/**
	 * If PBM Request is from a center then center's hospital_center_service_reg_no is considered as provider id.
	 * If PBM Request is for a account group then account's account_group_service_reg_no is considered as provider id.
	 */

	public BasicDynaBean getPBMHeaderFields(String pbmRequestId) throws SQLException {
		BasicDynaBean pbmRequestBean = pbmReqDAO.findByKey("pbm_request_id", pbmRequestId);

		if (pbmRequestBean != null) {
			if (pbmRequestBean.get("account_group") != null && ((Integer)pbmRequestBean.get("account_group")).intValue() != 0)
				return DataBaseUtil.queryToDynaBean(GET_PBM_ACCOUNT_HEADER_DETAILS, pbmRequestId);
			else if (pbmRequestBean.get("center_id") != null)
				return DataBaseUtil.queryToDynaBean(GET_PBM_HOSPITAL_CENTER_HEADER_DETAILS, pbmRequestId);
		}
		return null;
	}

	public Map validatePBMPrescriptions(Map<String, StringBuilder> errorsMap, String path, String activeMode,
			List<String> pbmPrescList, List<PBMRequest> pbmRequestList) throws IOException, TemplateException, SQLException, Exception {

		PBMPrescriptionHelper pbmhelper = new PBMPrescriptionHelper();
		HttpSession session = RequestContext.getSession();
		boolean mod_eclaim = (Boolean)session.getAttribute("mod_eclaim");

		String testingMemberId = null; // "1116528"

		Integer userCenterId = RequestContext.getCenterId();
		userCenterId = userCenterId == null ? 0 : userCenterId;
		BasicDynaBean centerBean = new CenterMasterDAO().findByKey("center_id", userCenterId);
		if (centerBean != null) {
			String shafafiya_pbm_test_member_id = centerBean.get("shafafiya_pbm_test_member_id") != null
							? ((String)centerBean.get("shafafiya_pbm_test_member_id")).trim() : "";

			testingMemberId =  shafafiya_pbm_test_member_id;
		}

		String defaultDiagnosisCodeType = null;

		RegistrationPreferencesDTO regPref = RegistrationPreferencesDAO.getRegistrationPreferences();

		String govenmtIdLabel = regPref.getGovernment_identifier_label() != null ? regPref.getGovernment_identifier_label() : "Emirates ID";
		String govenmtIdTypeLabel = regPref.getGovernment_identifier_label() != null ? regPref.getGovernment_identifier_type_label() : "Emirates ID Type";

		String encTypePref = regPref.getEncntr_type_reqd() != null ? regPref.getEncntr_type_reqd() : "RQ";

		StringBuilder attachmentErr = new StringBuilder("<br/> ATTACHMENT ERROR: PBM Prescription(s) which have attachment could not be attached in XML. <br/>" +
										"Please check the attachments for PBM Prescription(s) : <br/> ");

		StringBuilder drugQtyErr = new StringBuilder("<br/> DRUG QUANTITY ERROR: PBM Prescription(s) found with zero quantity. <br/>" +
										"Please check the Drug(s) for PBM Prescription(s) : <br/>");

		StringBuilder pbmStoreIdErr = new StringBuilder("<br/> PBM STORE ERROR: PBM Prescription(s) without store. <br/>" +
										"Please select store for PBM Prescription(s) : <br/> ");

		StringBuilder noCliniciansErr = new StringBuilder("<br/> NO CLINICIANS ERROR: PBM Prescription(s) without clinician. " +
										"Please enter clinician for Patients : <br/>");

		StringBuilder clinicianIdErr = new StringBuilder("<br/> CLINICIAN ERROR: PBM Prescription(s) without clinician Id. " +
										"Please enter clinician id for Doctors : <br/>");

		StringBuilder encountersErr = new StringBuilder("<br/> ENCOUNTERS ERROR: PBM Prescription(s) without encounter types. <br/>" +
										"Please enter encounter types for PBM Prescription(s) : <br/>");

		StringBuilder diagnosisCodesErr = new StringBuilder("<br/> DIAGNOSIS ERROR: PBM Prescription(s) without diagnosis codes. <br/>" +
										"Please enter diagnosis codes for PBM Prescription(s) : <br/>");

		StringBuilder accumedDiagnosisCodeTypeErr = new StringBuilder("<br/> DIAGNOSIS CODE TYPE ERROR: PBM Prescription(s) with invalid diagnosis code type. <br/>" +
										"Please enter diagnosis codes of type (ICD9 / ICD10) for PBM Prescription(s) : <br/>");

		StringBuilder haadDiagnosisCodeTypeErr = new StringBuilder("<br/> DIAGNOSIS CODE TYPE ERROR: PBM Prescription(s) with invalid diagnosis code type. <br/>" +
										"Please enter diagnosis codes of type (ICD) for PBM Prescription(s) : <br/>");

		StringBuilder drugCodesErr = new StringBuilder("<br/> DRUG CODES ERROR: PBM Prescription(s) found without activity codes(Drugs) w.r.t Center Health Authority. <br/>" +
										"Please check the Drugs : <br/>");

		StringBuilder drugObsvCodesErr = new StringBuilder("<br/> DRUG OBSERVATION CODES ERROR: PBM Prescription(s) found without observation codes (or) values (or) value types<br/> " +
										"Please check the Drug(s) for PBM Prescription(s) : <br/>");

		StringBuilder noActivitiesErr = new StringBuilder("<br/> ZERO ACTIVITIES ERROR: PBM Prescription(s) have no activities.<br/> " +
										"Please check the PBM Prescription(s): <br/>");

		StringBuilder govtIdNoErr = new StringBuilder("<br/> EMIRATES ID ERROR: PBM Prescription(s) without "+govenmtIdLabel+" (or) "+govenmtIdTypeLabel+".<br/> " +
										"Please check the "+govenmtIdLabel+" (or) "+govenmtIdTypeLabel+" for Patients: <br/>");

		StringBuilder receiverErr = new StringBuilder("<br/> RECEIVER ERROR: PBM Prescription(s) does not contain receiver. <br/>" +
										"Please check tpa code for sponsor : <br/> ");

		StringBuilder noCompanyErr = new StringBuilder("<br/> COMPANY ERROR: PBM Prescription(s) does not contain insurance company. <br/>" +
										"Please check insurance company for the prescription : <br/> ");

		StringBuilder payerErr = new StringBuilder("<br/> PAYER ERROR: PBM Prescription(s) does not contain payer. <br/>" +
										"Please check company code for insurance company : <br/> ");

		StringBuilder testingMemberErr = new StringBuilder("<br/> TESTING MEMBER ERROR: Shafafiya PBM Web service is not set to active mode, cannot use live data. <br/>" +
										"For testing purpose, the Member Id for the patient needs to be: <b> "+testingMemberId+ " </b> <br/> ");

		if (activeMode.equals("N") && testingMemberId.equals("")) {
			errorsMap.put("TESTING EMPTY MEMBER ERROR:", new StringBuilder("<br/> TESTING EMPTY MEMBER ERROR: Shafafiya PBM Web service is not set to active mode, cannot use live data. <br/>" +
					"PBM Prescription Request cannot be sent. PBM Test Member Id cannot be null."));
		}

		List<BasicDynaBean> diagnosis  = null;
		Map<String, List> observationsMap = new HashMap<String, List>();
		List<String> pbmPrescIdsList = new ArrayList<String>();
		List<String> cliniciansList = new ArrayList<String>();
		List<String> clinicianIdsList = new ArrayList<String>();
		List<String> observationNamesList = new ArrayList<String>();

		try {

			for (String pbmPresc : pbmPrescList) {
				PBMRequest pbmrequest = new PBMRequest();

				int pbmPrescId = Integer.parseInt(pbmPresc);

				BasicDynaBean pbmbean = getPBMPresc(pbmPrescId);

				Integer pbm_presc_id = (Integer)pbmbean.get("pbm_presc_id");
				String mr_no = (String)pbmbean.get("mr_no");
				String patient_id = (String)pbmbean.get("patient_id");
				String member_id = (String)pbmbean.get("member_id");
				String visit_type = (String)pbmbean.get("visit_type");
				String doctor_id = (String)pbmbean.get("doctor_id");
				String doctor_name = (String)pbmbean.get("doctor");
				String doctor_type = (String)pbmbean.get("doctor_type");
				String doctor_license_number= (String)pbmbean.get("doctor_license_number");
				String emirates_id_number = pbmbean.get("emirates_id_number") != null ? (String)pbmbean.get("emirates_id_number") : null;
				String encounter_type = pbmbean.get("encounter_type") != null ? ((Integer)pbmbean.get("encounter_type")).toString() :null;
				String resubmissionType = pbmbean.get("resubmit_type") != null ? ((String)pbmbean.get("resubmit_type")).toString() :null;
				String is_resubmit = pbmbean.get("is_resubmit") != null ? ((String)pbmbean.get("is_resubmit")).toString() : "N";
				Integer pbm_store_id = (Integer)pbmbean.get("pbm_store_id");
				String pbmPrescStatus = (String)pbmbean.get("pbm_presc_status");
				String pbmRequestType = pbmbean.get("pbm_request_type") != null ? (String)pbmbean.get("pbm_request_type") : null;

				Integer centerId = RequestContext.getCenterId();
				Integer pbmCenterId = pbmbean.get("center_id") != null ? (Integer)pbmbean.get("center_id") : centerId;
				HealthAuthorityDTO healthAuthDTO = HealthAuthorityPreferencesDAO.getHealthAuthorityPreferences(
						CenterMasterDAO.getHealthAuthorityForCenter(pbmCenterId));
				String healthAuthority = healthAuthDTO.getHealth_authority();
				defaultDiagnosisCodeType = healthAuthDTO.getDiagnosis_code_type();
				defaultDiagnosisCodeType = defaultDiagnosisCodeType != null ? defaultDiagnosisCodeType : "ICD";

				if (activeMode.equals("N") && !member_id.trim().equals(testingMemberId.trim())) {
					errorsMap.put("TESTING MEMBER ERROR:", testingMemberErr.append(pbmhelper.urlString(path, "insurance", patient_id, null)));
					testingMemberErr.append(" , ");
				}

				if (pbm_store_id == null) {
					errorsMap.put("PBM STORE ERROR:", pbmStoreIdErr.append(pbmhelper.urlString(path, "pbmprescription", pbmPresc, null)));
					pbmStoreIdErr.append(" , ");
				}

				String receiver_id = pbmbean.get("receiver_id") != null ? (String)pbmbean.get("receiver_id") : null;
				String tpa_id = pbmbean.get("tpa_id") != null ? (String)pbmbean.get("tpa_id") : null;
				String tpa_name = pbmbean.get("tpa_name") != null ? (String)pbmbean.get("tpa_name") : null;

				if (receiver_id == null || receiver_id.trim().equals("")) {
					errorsMap.put("RECEIVER ERROR:", receiverErr.append(pbmhelper.urlString(path, "sponsor", tpa_id, tpa_name)));
					receiverErr.append(" , ");
				}

				String payer_id = pbmbean.get("payer_id") != null ? (String)pbmbean.get("payer_id") : null;
				String insurance_co_id = pbmbean.get("insurance_co_id") != null ? (String)pbmbean.get("insurance_co_id") : null;
				String insurance_co_name = pbmbean.get("insurance_co_name") != null ? (String)pbmbean.get("insurance_co_name") : null;

				if (insurance_co_id == null || insurance_co_id.trim().equals("")) {
					errorsMap.put("COMPANY ERROR:", noCompanyErr.append(pbmhelper.urlString(path, "pbmprescription", pbmPresc, null)));
					noCompanyErr.append(" , ");

				}else if (payer_id == null || payer_id.trim().equals("")) {
					errorsMap.put("PAYER ERROR:", payerErr.append(pbmhelper.urlString(path, "company", insurance_co_id, insurance_co_name)));
					payerErr.append(" , ");
				}

				pbmrequest.setPbmRequestBean(pbmbean);

				if (is_resubmit != null && is_resubmit.equals("Y")) {
					Map attachmentMap = getAttachment(pbm_presc_id);
					InputStream file = (InputStream)attachmentMap.get("Content");
					if (file != null) {
						String attachment =  pbmhelper.convertToBase64Binary(file);
						if (attachment != null)
							pbmrequest.setAttachment(attachment);
						else {
							errorsMap.put("ATTACHMENT ERROR:", attachmentErr.append(pbmhelper.urlString(path, "attachment", pbmPresc, null)));
							attachmentErr.append(" , ");
						}
					}
				}

				if (emirates_id_number == null || emirates_id_number.equals("")) {
					errorsMap.put("EMIRATES ID ERROR:", govtIdNoErr.append(pbmhelper.urlString(path, "pre-registration", mr_no, null)));
					govtIdNoErr.append("  ,  ");
				}

				if (((visit_type.equals("i") && encTypePref.equals("IP"))
						|| (visit_type.equals("o") && encTypePref.equals("OP"))
						|| encTypePref.equals("RQ")) && (encounter_type == null || encounter_type.equals("0"))) {
					errorsMap.put("ENCOUNTERS ERROR:", encountersErr.append(pbmhelper.urlString(path, "diagnosis", patient_id, null)));
					encountersErr.append("  ,  ");
				}

				if (pbmPrescStatus.equalsIgnoreCase("O") ||
						(pbmPrescStatus.equalsIgnoreCase("R") && resubmissionType.equalsIgnoreCase("correction")) ||
						(pbmRequestType != null && pbmRequestType.equalsIgnoreCase("cancellation"))){
					diagnosis = findAllDiagnosis(patient_id);
				} else {
					diagnosis = findFlaggedDiagnosis(patient_id);
				}
				pbmrequest.setDiagnosis(diagnosis);

				if (diagnosis == null || diagnosis.size() == 0) {
					errorsMap.put("DIAGNOSIS ERROR:", diagnosisCodesErr.append(pbmhelper.urlString(path, "diagnosis", patient_id, null)));
					diagnosisCodesErr.append("  ,  ");

				}else {
					for (BasicDynaBean diag : diagnosis) {
						String code_type = (String)diag.get("code_type");
						String icd_code = (String)diag.get("icd_code");
						String diag_type = (String)diag.get("diag_type");

						if (icd_code == null || icd_code.equals("")) {
							errorsMap.put("DIAGNOSIS ERROR:", diagnosisCodesErr.append(pbmhelper.urlString(path, "diagnosis", patient_id, null)));
							diagnosisCodesErr.append(" , ");
						}

						if (mod_eclaim) {
							// For HAAD -- Diagnosis code types is ICD.
							if (code_type.equalsIgnoreCase(defaultDiagnosisCodeType)) {
							}else {
								errorsMap.put("DIAGNOSIS CODE TYPE ERROR:", haadDiagnosisCodeTypeErr.append(pbmhelper.urlString(path, "diagnosis", patient_id, null)));
								haadDiagnosisCodeTypeErr.append(" , ");
							}
						}
					}
				}


				if (doctor_name == null || doctor_name.trim().equals("")){

					if (!cliniciansList.contains(doctor_name) && !pbmPrescIdsList.contains(pbmPresc)) {
						errorsMap.put("NO CLINICIANS ERROR:", noCliniciansErr.append(pbmhelper.urlString(path, "patient", patient_id, null)));
						noCliniciansErr.append(" , ");

						cliniciansList.add(doctor_name);
						pbmPrescIdsList.add(pbmPresc);
					}

				}else if (doctor_license_number == null || doctor_license_number.trim().equals("")) {

					if (!clinicianIdsList.contains(doctor_license_number) && !pbmPrescIdsList.contains(pbmPresc)) {

						if (doctor_type.equals("Doctor")) {
							errorsMap.put("CLINICIAN ERROR:", clinicianIdErr.append(pbmhelper.urlString(path, "doctor", doctor_id, doctor_name)));
						}else {
							errorsMap.put("CLINICIAN ERROR:", clinicianIdErr.append(pbmhelper.urlString(path, "referral", doctor_id, doctor_name)));
						}
						clinicianIdErr.append(" , ");

						clinicianIdsList.add(doctor_license_number);
						pbmPrescIdsList.add(pbmPresc);
					}
				}

				List<BasicDynaBean> pbmactivities = new ArrayList<BasicDynaBean>();
				if (is_resubmit.equals("Y") &&
						(resubmissionType.equalsIgnoreCase("internal complaint")
								|| ("DHA".equals(healthAuthority) && resubmissionType.equalsIgnoreCase("reconciliation")))) {
					pbmactivities  = findPBMActivities(pbm_presc_id, true, healthAuthority);
				}else {
					pbmactivities  = findPBMActivities(pbm_presc_id, false, healthAuthority);
				}

				/* Check for codes for each activity */
				for (BasicDynaBean item : pbmactivities) {
					List<BasicDynaBean> observations  = null;
					Integer pbm_medicine_pres_id  = (Integer)item.get("pbm_medicine_pres_id");
					String item_code         	 = (String)item.get("item_code");
					String medicine_name      	 = (String)item.get("medicine_name");
					String medicine_id      	 = (String)item.get("item_id");
					Timestamp prescribed_date    = (Timestamp)item.get("prescribed_date");
					BigDecimal med_qty = item.get("medicine_quantity") != null ? (BigDecimal)item.get("medicine_quantity") : BigDecimal.ZERO;

					if (med_qty.compareTo(BigDecimal.ZERO) == 0) {
						errorsMap.put("DRUG QUANTITY ERROR:",
						drugQtyErr.append(pbmhelper.urlString(path, "pbmprescription", pbmPresc, null)));
						drugQtyErr.append("<br/>PBM ID :" + pbmPresc + "Drug : ( "+medicine_name+ ", Prescribed Date: "+prescribed_date+" ), <br/> ");
					}

					if (item_code == null || item_code.trim().equals("")) {
						errorsMap.put("DRUG CODES ERROR:", drugCodesErr.append(pbmhelper.urlString(path, "drug", medicine_id, medicine_name)));
						drugCodesErr.append(" , ");
					}

					observations = findDrugObservations(pbm_medicine_pres_id);

					if (observations != null && observations.size() > 0 ) {

						/* Check for observation codes for each prescribed drug */
						for (BasicDynaBean observation : observations) {
							Integer obs_id  = (Integer)observation.get("obs_id");
							String obs_name  = (String)observation.get("observation_name");
							String code_type  = (String)observation.get("type");
							String result_code  = (String)observation.get("code");
							String result_value  = (String)observation.get("value");
							String value_type  = (String)observation.get("value_type");
							String required  = (String)observation.get("required");

							if (required.equals("Y")
									&& (result_value == null || result_value.trim().equals(""))) {
											//|| value_type == null || value_type.trim().equals(""))

								errorsMap.put("DRUG OBSERVATION CODES ERROR:",
								drugObsvCodesErr.append(pbmhelper.urlString(path, "pbmprescription", pbmPresc, null)));
								drugObsvCodesErr.append("<br/>PBM ID :" + pbmPresc + "Drug : ( "+medicine_name+ ", Prescribed Date: "+prescribed_date+" ), <br/> ");
							}

							if ((result_code == null || result_code.equals("")
									|| code_type == null || code_type.equals(""))
									&& (result_value != null && !result_value.trim().equals(""))
									&& !observationNamesList.contains(obs_name)) {

								errorsMap.put("DRUG OBSERVATION CODES ERROR:",
								drugObsvCodesErr.append(pbmhelper.urlString(path, "pbmobservation", obs_id.toString(), obs_name)));
								drugObsvCodesErr.append(" , ");
								observationNamesList.add(obs_name);
							}
						}

						observationsMap.put(pbm_medicine_pres_id.toString(), observations);
					}// Observations
				}// Activities

				if (pbmactivities.size() > 0) {
					pbmrequest.setActivities(pbmactivities);
					pbmrequest.setObservationsMap(observationsMap);

					pbmRequestList.add(pbmrequest);
				}else {
					errorsMap.put("ZERO ACTIVITIES ERROR:", noActivitiesErr.append(pbmhelper.urlString(path, "pbmprescription", pbmPresc, null)));
					noActivitiesErr.append(" , ");
				}
			}
		} catch (Exception e) {	throw e; }

		return errorsMap;
	}

	private static final String FIND_DRUG_OBSERVATIONS =
			" SELECT ppo.obs_id, ppo.observation_id, ppo.value, ppo.value_type, " +
			" pom.observation_name, pom.observation_type, " +
			" pom.observation_type AS type, pom.code, pom.required, " +
			" pom.patient_med_presc_value_column, pom.patient_med_presc_units_column " +
			" FROM pbm_presc_observations ppo " +
			" JOIN pbm_observations_master pom ON (pom.id = ppo.obs_id)" +
			" WHERE ppo.pbm_medicine_pres_id = ? ";

	public List<BasicDynaBean> findDrugObservations(int pbm_medicine_pres_id) throws SQLException {
		return DataBaseUtil.queryToDynaList(FIND_DRUG_OBSERVATIONS, pbm_medicine_pres_id);
	}


	private static final String GET_PBM_PRESCRIPTION_ACTIVITIES =
		" SELECT sid.medicine_name as item_name, sid.medicine_id::text as item_id," +
		"   sid.medicine_name, sid.medicine_id, pmp.medicine_quantity, " +
		"	CASE WHEN pmp.user_unit = 'I' THEN sid.issue_units " +
		"	WHEN pmp.user_unit = 'P' THEN sid.package_uom ELSE sid.issue_units END AS user_uom, " +
		"	pmp.user_unit, pmp.package_size, 0.00 as total_available_qty, " +
		"	COALESCE(sid.package_uom, '') as package_uom, COALESCE(sid.issue_units,'') as issue_uom, " +
		" 	pmp.pbm_medicine_pres_id::text as item_prescribed_id, pmp.pbm_medicine_pres_id, " +
		"	pmp.op_medicine_pres_id, pmp.medicine_remarks, " +
		"	pmp.frequency, pmp.strength, pmp.duration, pmp.duration_units," +
		"	pmp.medicine_remarks as item_remarks, pmp.issued, " +
		"	cum.consumption_uom, g.generic_name, g.generic_code, 'item_master' as master, " +
		"	'Medicine' as item_type, false as ispackage, pmp.activity_due_date, pmp.mod_time, mr.route_id, mr.route_name " +
		"  ,icm.category AS category_name,mm.manf_name, mm.manf_mnemonic,0 as lblcount,issue_base_unit, " +
		"	sid.prior_auth_required, coalesce(pmp.item_form_id, 0) as item_form_id, pmp.item_strength, if.item_form_name, " +
		"	pmp.item_strength_units, su.unit_name, " +
		"	to_char(pmp.prescribed_date::timestamp, 'dd/MM/yyyy hh24:mi') AS activity_prescribed_date," +
		"   pmp.prescribed_date, " +
		"	msct.haad_code, sic.item_code, sic.code_type, pmp.pbm_presc_id, pmp.pbm_status," +
		"   pmp.denial_code, pmp.denial_remarks, " +
		"	idc.status AS denial_code_status, idc.type AS denial_code_type, " +
		"	pmp.rate, pmp.discount, pmp.amount, pmp.claim_net_amount, pmp.claim_net_approved_amount," +

		"	COALESCE(patmp.op_medicine_pres_id::text, '') as pat_presc_item_prescribed_id," +
		"	patmp.medicine_remarks as pat_presc_medicine_remarks," +
		"	patmp.medicine_quantity as pat_presc_medicine_quantity," +
		"	coalesce(patmp.item_form_id, 0) as pat_presc_item_form_id, " +
		"	patmp.item_strength as pat_presc_item_strength, " +
		"	patmp.item_strength_units as pat_presc_item_strength_units," +
		"	patmp.route_of_admin as pat_presc_route_id, pmr.route_name as pat_presc_route_name," +
		"	pif.item_form_name as pat_presc_item_form_name, psu.unit_name as pat_presc_unit_name," +
		"	patmp.frequency as pat_presc_frequency, patmp.strength as pat_presc_strength, " +
		"	patmp.duration as pat_presc_duration, patmp.duration_units as pat_presc_duration_units," +
		"	if.granular_units " +

		" FROM pbm_medicine_prescriptions pmp " +
		"	JOIN pbm_prescription pbmp ON (pbmp.pbm_presc_id = pmp.pbm_presc_id) " +
		"	LEFT JOIN patient_medicine_prescriptions patmp ON(patmp.op_medicine_pres_id = pmp.op_medicine_pres_id)" +
		"	LEFT JOIN store_item_details sid ON (pmp.medicine_id=sid.medicine_id) " +
		"	LEFT JOIN ha_item_code_type hict ON (hict.medicine_id = sid.medicine_id AND hict.health_authority=?) " +
		"	LEFT JOIN store_item_codes sic ON (sic.medicine_id = sid.medicine_id AND sic.code_type = hict.code_type) "	+
		" 	LEFT JOIN item_form_master if ON (pmp.item_form_id=if.item_form_id) " +
		"	LEFT JOIN consumption_uom_master cum ON (sid.cons_uom_id = cum.cons_uom_id) "+
		"	LEFT OUTER JOIN generic_name g ON (pmp.generic_code = g.generic_code) " +
		"	LEFT OUTER JOIN medicine_route mr ON (mr.route_id=pmp.route_of_admin) " +
		"	LEFT OUTER JOIN strength_units su ON (pmp.item_strength_units=su.unit_id) " +

		" 	LEFT OUTER JOIN item_form_master pif ON (patmp.item_form_id=pif.item_form_id) " +
		"	LEFT OUTER JOIN strength_units psu ON (patmp.item_strength_units=psu.unit_id) " +
		"	LEFT OUTER JOIN medicine_route pmr ON (patmp.route_of_admin=pmr.route_id) " +

		"   LEFT OUTER JOIN manf_master mm on mm.manf_code=sid.manf_name " +
		"   LEFT OUTER JOIN store_category_master icm on sid.med_category_id=icm.category_id " +
		"	LEFT JOIN mrd_supported_code_types msct ON (msct.code_type = sic.code_type) " +
		"	LEFT JOIN insurance_denial_codes idc ON (idc.denial_code = pmp.denial_code) " +
		" WHERE pbmp.pbm_presc_id=?";

	private static final String TOTAL_AVAILABLE_QTY =
			" SELECT sum(qty) FROM store_stock_details ssd " +
			" JOIN stores s ON (s.dept_id=ssd.dept_id)" +
			" WHERE auto_fill_prescriptions AND COALESCE(store_rate_plan_id, 0) != 0 " +
			" AND medicine_id = ? ";

	public static List getPBMPrescriptionActivities(int pbm_presc_id, String healthAuthority) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_PBM_PRESCRIPTION_ACTIVITIES + " ORDER BY pmp.pbm_medicine_pres_id ");
			ps.setString(1, healthAuthority);
			ps.setInt(2, pbm_presc_id);
			List<BasicDynaBean> l = DataBaseUtil.queryToDynaList(ps);
			if (l != null && l.size() > 0) {
				for (BasicDynaBean actbean : l) {
					int medicine_id = actbean.get("medicine_id") != null ? (Integer)actbean.get("medicine_id") : 0;
					if (medicine_id != 0) {
						actbean.set("total_available_qty",
							DataBaseUtil.getBigDecimalValueFromDb(TOTAL_AVAILABLE_QTY, medicine_id));
					}
				}
			}
			return l;
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public List<BasicDynaBean> findPBMActivities(Integer pbm_presc_id,
			boolean isResubmission, String healthAuthority) throws SQLException {
		// While Resubmission of prescription, and Resubmission type is Internal complaint then,
		// only those activites which are denied should go in Prior Request XML.
		if (isResubmission) {
			return DataBaseUtil.queryToDynaList(GET_PBM_PRESCRIPTION_ACTIVITIES + " AND pmp.pbm_status = 'D' ", new Object[]{healthAuthority, pbm_presc_id});
		}else {
			return DataBaseUtil.queryToDynaList(GET_PBM_PRESCRIPTION_ACTIVITIES, new Object[]{healthAuthority, pbm_presc_id});
		}
	}

	private String GET_PBM_PRESCRIPTION = " SELECT " +
    " pr.mr_no, pr.patient_id, pr.visit_type, pr.op_type," +
    " CASE WHEN pd.government_identifier IS NULL OR pd.government_identifier = '' THEN " +
	" COALESCE(gim.identifier_type,'') ELSE  pd.government_identifier END AS emirates_id_number, " +
	" ppd.member_id, ppd.policy_number, " +
	" COALESCE (hta.tpa_code, '@'||tm.tpa_name) AS receiver_id, " +
	" COALESCE (hic.insurance_co_code,'@'||icm.insurance_co_name) AS payer_id," +
	" prad.account_group, prad.center_id, " +
	" CASE WHEN COALESCE(prad.account_group,0) != 0 THEN agm.account_group_service_reg_no " +
	"	ELSE hcm.hospital_center_service_reg_no END AS provider_id, " +
	" COALESCE(pprqresub.approval_status, pprq.approval_status, prad.approval_status) AS approval_status," +
	" pr.encounter_type, etc.encounter_type_desc," +
    " pr.status as patstatus, pr.center_id, pbmp.pbm_finalized, " +
    " CASE WHEN (pr.op_type != 'O') THEN d.doctor_license_number ELSE ref.doctor_license_number END AS doctor_license_number," +
	" CASE WHEN (pr.op_type != 'O') THEN d.doctor_name ELSE ref.referal_name END AS doctor," +
	" CASE WHEN (pr.op_type != 'O') THEN d.doctor_id ELSE ref.referal_no END AS doctor_id," +
	" CASE WHEN (pr.op_type != 'O') THEN 'Doctor' ELSE ref.doctor_type END AS doctor_type," +
	" CASE WHEN (pr.op_type != 'O') THEN dc.visited_date ELSE (pr.reg_date + pr.reg_time) END AS visited_date," +
	" CASE WHEN (pr.op_type != 'O') THEN to_char(dc.visited_date::date, 'dd/MM/yyyy') " +
	"	ELSE to_char(pr.reg_date, 'dd/MM/yyyy') END AS date_ordered," +
    " grp.consultation_id, " +
    " pbmp.resubmit_request_id_with_correction, pbmp.pbm_resubmit_request_id, " +
    " pbmp.pbm_presc_id, pbmp.status AS pbm_presc_status,  " +
    " pbmp.pbm_request_id, pbmp.drug_count, pbmp.comments," +
    " pbmp.resubmit_type, prad.is_resubmit, prad.pbm_request_type, prad.approval_comments,  " +
    " COALESCE(pprqresub.pbm_auth_id_payer, pprq.pbm_auth_id_payer, prad.pbm_auth_id_payer) AS pbm_auth_id_payer, " +
    " pbmp.pbm_store_id, s.dept_name AS pbm_store_name, " +
    " pbmp.erx_presc_id, pbmp.erx_request_type, pbmp.erx_request_date, " +
    " pbmp.erx_center_id, pbmp.erx_reference_no, pbmp.erx_approval_status, " +
    " pr.primary_insurance_co AS insurance_co_id, pr.primary_sponsor_id AS tpa_id , " +
    " pr.plan_id, pr.category_id :: text, " +
	" icm.insurance_co_name, tm.tpa_name, pm.plan_name, cat.category_name, " +
	" pr.org_id, org.pharmacy_discount_percentage, org.pharmacy_discount_type " +
    " FROM (SELECT pbm_presc_id, consultation_id, visit_id, " +
	"			avg(CASE WHEN issued IN ('Y', 'C') THEN 3 WHEN issued = 'P' THEN 2 ELSE 1 END) as sts " +
    " 		FROM pbm_medicine_prescriptions " +
    " 		GROUP by pbm_presc_id, consultation_id, visit_id " +
    "		) as grp" +
    " JOIN patient_registration pr ON (grp.visit_id = pr.patient_id)" +
    " JOIN patient_details pd ON (pr.mr_no = pd.mr_no) " +
    " JOIN organization_details org ON (org.org_id = pr.org_id)" +
    " JOIN pbm_prescription pbmp ON (grp.pbm_presc_id = pbmp.pbm_presc_id ) " +
    " LEFT JOIN doctor_consultation dc ON (dc.consultation_id = grp.consultation_id) " +
    " LEFT JOIN doctors d ON (d.doctor_id = pr.doctor)" +
	" LEFT JOIN (" +
	"	SELECT 'Referal' AS doctor_type,referal_no, referal_name, clinician_id AS doctor_license_number FROM referral" +
	"	UNION" +
	"	SELECT 'Doctor' AS doctor_type,doctor_id, doctor_name, doctor_license_number FROM doctors" +
	" ) AS ref ON (ref.referal_no = pr.reference_docto_id)" +
	" LEFT JOIN tpa_master tm ON (tm.tpa_id = pr.primary_sponsor_id) " +
	" LEFT JOIN insurance_company_master icm ON (icm.insurance_co_id = pr.primary_insurance_co)" +
	" LEFT JOIN insurance_category_master cat ON (cat.category_id = pr.category_id)" +
	" LEFT JOIN insurance_plan_main pm ON (pm.plan_id = pr.plan_id)" +
    " LEFT JOIN govt_identifier_master gim ON (gim.identifier_id = pd.identifier_id) "+
    " LEFT JOIN patient_insurance_plans pip ON (pr.patient_id = pip.patient_id AND pip.priority=1 ) " +
	" LEFT JOIN patient_policy_details ppd ON (ppd.patient_policy_id = pip.patient_policy_id) "+
	" LEFT JOIN encounter_type_codes etc ON (etc.encounter_type_id = encounter_type) " +
	" LEFT JOIN pbm_request_approval_details prad on (prad.pbm_request_id = pbmp.pbm_request_id) " +
	" LEFT JOIN pbm_prescription_request pprq on (pprq.pbm_request_id = pbmp.pbm_request_id) " +
	" LEFT JOIN pbm_prescription_request pprqresub on (pprqresub.pbm_request_id = pbmp.pbm_resubmit_request_id)" +
	" LEFT JOIN account_group_master agm ON (agm.account_group_id = prad.account_group) "+
	" LEFT JOIN hospital_center_master hcm ON (hcm.center_id = prad.center_id) " +
	" LEFT JOIN stores s ON (s.dept_id = pbmp.pbm_store_id)" +
	" LEFT JOIN ha_tpa_code hta ON(hta.tpa_id = tm.tpa_id AND hta.health_authority = hcm.health_authority)" +
	" LEFT JOIN ha_ins_company_code hic ON(hic.insurance_co_id=icm.insurance_co_id AND hic.health_authority=hcm.health_authority)"+
	" WHERE pbmp.pbm_presc_id = ? AND ( patient_confidentiality_check(pd.patient_group,pd.mr_no) )";

	public BasicDynaBean getPBMPresc(int pbmPrescId) throws SQLException {
		return DataBaseUtil.queryToDynaBean(GET_PBM_PRESCRIPTION, pbmPrescId);
	}

	private String GET_PATIENT_PRESCRIPTION = " SELECT " +
    " pr.mr_no, pr.patient_id, pr.visit_type, pr.op_type," +
    " CASE WHEN pd.government_identifier IS NULL OR pd.government_identifier = '' THEN " +
	" COALESCE(gim.identifier_type,'') ELSE  pd.government_identifier END AS emirates_id_number, " +
	" ppd.member_id, ppd.policy_number, " +
	" COALESCE (hta.tpa_code, '@'||tm.tpa_name) AS receiver_id, " +
	" COALESCE (hic.insurance_co_code,'@'||icm.insurance_co_name) AS payer_id," +
	" NULL AS account_group, NULL AS center_id, " +
	" '' AS provider_id, pr.encounter_type, etc.encounter_type_desc," +
    " pr.status as patstatus, pr.center_id, 'N' AS pbm_finalized, "+
    " 0 AS pbm_presc_id, ' ' AS pbm_presc_status,  " +
    " NULL AS pbm_request_id, 0 AS drug_count, '' AS comments, '' AS approval_comments, " +
    " '' AS resubmit_type, 'N' AS is_resubmit, " +
    " NULL AS pbm_store_id, pr.primary_insurance_co AS insurance_co_id, pr.primary_sponsor_id AS tpa_id , " +
    " pr.plan_id, pr.category_id :: text, " +
	" icm.insurance_co_name, tm.tpa_name, pm.plan_name, cat.category_name, " +
	" pr.org_id, org.pharmacy_discount_percentage, org.pharmacy_discount_type, " +
	" CASE WHEN (pr.op_type != 'O') THEN d.doctor_license_number ELSE ref.doctor_license_number END AS doctor_license_number," +
	" CASE WHEN (pr.op_type != 'O') THEN d.doctor_name ELSE ref.referal_name END AS doctor," +
	" CASE WHEN (pr.op_type != 'O') THEN d.doctor_id ELSE ref.referal_no END AS doctor_id," +
	" CASE WHEN (pr.op_type != 'O') THEN 'Doctor' ELSE ref.doctor_type END AS doctor_type," +
	" CASE WHEN (pr.op_type != 'O') THEN dc.visited_date ELSE (pr.reg_date + pr.reg_time) END AS visited_date," +
	" CASE WHEN (pr.op_type != 'O') THEN to_char(dc.visited_date::date, 'dd/MM/yyyy') " +
	"	ELSE to_char(pr.reg_date, 'dd/MM/yyyy') END AS date_ordered," +
    " consultation_id " +
    " FROM patient_registration pr " +
    " JOIN hospital_center_master hcm ON(hcm.center_id = pr.center_id)" +
    " JOIN organization_details org ON (org.org_id = pr.org_id)" +
    " LEFT JOIN doctor_consultation dc ON (dc.patient_id = pr.patient_id AND dc.doctor_name = pr.doctor) "+
    " LEFT JOIN ( SELECT 'Referal' AS doctor_type,referal_no, referal_name, clinician_id AS doctor_license_number FROM referral" +
	"		 UNION" +
	"		 SELECT 'Doctor' AS doctor_type,doctor_id, doctor_name, doctor_license_number FROM doctors" +
	"		) AS ref ON (ref.referal_no = pr.reference_docto_id) "+
    " LEFT JOIN doctors d on (dc.doctor_name = d.doctor_id) " +
	" LEFT JOIN tpa_master tm ON (tm.tpa_id = pr.primary_sponsor_id)" +
	" LEFT JOIN ha_tpa_code hta ON(hta.tpa_id = tm.tpa_id AND hta.health_authority = hcm.health_authority)" +
	" LEFT JOIN insurance_company_master icm ON (icm.insurance_co_id = pr.primary_insurance_co)" +
	" LEFT JOIN ha_ins_company_code hic ON(hic.insurance_co_id=icm.insurance_co_id AND hic.health_authority=hcm.health_authority)" +
	" LEFT JOIN insurance_category_master cat ON (cat.category_id = pr.category_id)" +
	" LEFT JOIN insurance_plan_main pm ON (pm.plan_id = pr.plan_id)" +
    " LEFT JOIN patient_details pd ON (pr.mr_no = pd.mr_no) " +
    " LEFT JOIN govt_identifier_master gim ON (gim.identifier_id = pd.identifier_id) "+
    " LEFT JOIN patient_insurance_plans pip ON (pr.patient_id = pip.patient_id AND pip.priority=1 ) " +
	" LEFT JOIN patient_policy_details ppd ON (ppd.patient_policy_id = pip.patient_policy_id) "+
	" LEFT JOIN encounter_type_codes etc ON (etc.encounter_type_id = encounter_type) ";

	public BasicDynaBean getPBMPatient(String patient_id) throws SQLException {
		return DataBaseUtil.queryToDynaBean(GET_PATIENT_PRESCRIPTION + " WHERE pr.patient_id = ? ", patient_id);
	}

	private static final String UPDATE_PBM_FINALIZED =
		" UPDATE pbm_prescription SET pbm_finalized = 'Y', pbm_finalized_by = ? WHERE pbm_presc_id = ? ";

	public boolean finalizePrescriptions(List<String> pbmPrescList, String userid) throws SQLException {
		boolean success = true;
		if (pbmPrescList.isEmpty()) {
			return success;
		} else {
			Connection con = null;
			PreparedStatement ps = null;
			try {
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);
				ps = con.prepareStatement(UPDATE_PBM_FINALIZED);

				Iterator<String> it = pbmPrescList.iterator();

				while (it.hasNext()) {
					String pbmPresc = it.next();
					ps.setString(1, userid);
					ps.setInt(2, Integer.parseInt(pbmPresc));
					ps.addBatch();
				}
				int[] result = ps.executeBatch();
				success = DataBaseUtil.checkBatchUpdates(result);
				return success;
			}finally {
				if(ps != null) {
					ps.close();
				}
				DataBaseUtil.commitClose(con, success);
			}
		}
	}


	public int updatePBMAndMedicineId(Connection con, int consId,
			int itemPrescriptionId, int pbmPrescId) throws SQLException, IOException {

		GenericDAO patMedPrescDAO = new GenericDAO("patient_medicine_prescriptions");

		boolean success = true;
		BasicDynaBean opMedPrescBean =
			patMedPrescDAO.findByKey(con, "op_medicine_pres_id", itemPrescriptionId);

		if (opMedPrescBean != null) {

			// Update medicine_id.
			if (opMedPrescBean.get("medicine_id") == null) {
				String genericCode = (String)opMedPrescBean.get("generic_code");
				BasicDynaBean genNameBean = genericNameDAO.findByKey("generic_code", genericCode);
				if (genNameBean != null) {
					String genericName = (String)genNameBean.get("generic_name");
					BasicDynaBean storeItemBean = storeItemDAO.findByKey("generic_name", genericName);
					if (storeItemBean != null) {
						opMedPrescBean.set("medicine_id", (Integer)storeItemBean.get("medicine_id"));
					}
				}
			}

			// Update pbm_presc_id.
			if (opMedPrescBean.get("pbm_presc_id") == null) {
				if (pbmPrescId == 0) {
					pbmPrescId = getNextSequence();
					BasicDynaBean pbmBean = getBean();
					pbmBean.set("pbm_presc_id", pbmPrescId);
					pbmBean.set("erx_consultation_id", consId);
					pbmBean.set("pbm_finalized", "N");
					pbmBean.set("status", "O");

					if (!insert(con, pbmBean)){
						success = false;
						return -1;
					}
				}
			}
			if (pbmPrescId != 0) {
				if (opMedPrescBean.get("pbm_presc_id") == null)	{
					opMedPrescBean.set("pbm_presc_id", pbmPrescId);
				}
				int i = patMedPrescDAO.updateWithName(con, opMedPrescBean.getMap(), "op_medicine_pres_id");
				success = (i > 0);
			}
		}
		return (success ? pbmPrescId : -1);
	}

	public boolean savePBMRequestDetails(int pbmPrescId, String userid, String requestType,
				int accountGroup, Integer centerId, boolean updateRequest) throws SQLException, IOException {

		boolean mod_eclaim_pbm = (Boolean)RequestContext.getSession().getAttribute("mod_eclaim_pbm");
		boolean mod_eclaim_erx = (Boolean)RequestContext.getSession().getAttribute("mod_eclaim_erx");

		boolean success = true;
		Connection con = null;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			GenericDAO pbmPrescReqDAO = new GenericDAO("pbm_prescription_request");
			String isResubmit = "N";

			List<String> columns = new ArrayList<String>();
			columns.add("pbm_presc_id");
			columns.add("pbm_request_id");
			columns.add("erx_presc_id");
			columns.add("pbm_finalized");
			columns.add("status");
			columns.add("resubmit_type");
			columns.add("pbm_resubmit_request_id");

			Map<String, Object> key = new HashMap<String, Object>();
			key.put("pbm_presc_id", pbmPrescId);
			BasicDynaBean pbmPresBean = findByKey(columns, key);

			String service_reg_no = null;

			if (accountGroup != 0) {
				BasicDynaBean accbean = new AccountingGroupMasterDAO().findByKey("account_group_id", accountGroup);
				service_reg_no = accbean.get("account_group_service_reg_no") != null ? (String)accbean.get("account_group_service_reg_no") : "";
			}else if (centerId != null) {
				BasicDynaBean centerbean = new CenterMasterDAO().findByKey("center_id", centerId);
				service_reg_no = centerbean.get("hospital_center_service_reg_no") != null ? (String)centerbean.get("hospital_center_service_reg_no") : "";
			}

			String healthAuthority = CenterMasterDAO.getHealthAuthorityForCenter(centerId);

			String receiverId = null;
			String patientId = DataBaseUtil.getStringValueFromDb("SELECT visit_id " +
					" FROM pbm_medicine_prescriptions WHERE pbm_presc_id = ? LIMIT 1 ", pbmPrescId);

			String primarySponsorId = DataBaseUtil.getStringValueFromDb("SELECT primary_sponsor_id " +
					" FROM patient_registration WHERE patient_id = ?", patientId);

			BasicDynaBean tpabean = DataBaseUtil.queryToDynaBean("SELECT hta.tpa_code, tm.tpa_name FROM tpa_master tm  " +
					"LEFT JOIN ha_tpa_code hta ON(hta.tpa_id=tm.tpa_id AND hta.health_authority = ?) WHERE tm.tpa_id = ? ", new Object[]{healthAuthority, primarySponsorId});

			receiverId = tpabean.get("tpa_code") != null ? (String)tpabean.get("tpa_code") : "@"+(String)tpabean.get("tpa_name");

			String pbmRequestId = null;
			String pbmPrescriptionRequestId = null;

			if (mod_eclaim_erx) {

				pbmRequestId = pbmPresBean.get("erx_presc_id") != null ? (String)pbmPresBean.get("erx_presc_id") : null;

				if (pbmRequestId == null) {
					return false;
				}

				String pbmResubmitRequestId =
					pbmPresBean.get("pbm_resubmit_request_id") != null ? (String)pbmPresBean.get("pbm_resubmit_request_id") : null;

	    		pbmPrescriptionRequestId =
	    			(pbmResubmitRequestId != null &&  !pbmResubmitRequestId.equals("")) ? pbmResubmitRequestId : pbmRequestId;

	    		isResubmit = (pbmPrescriptionRequestId.equals(pbmRequestId)) ? "N" : "Y";

			}else if (mod_eclaim_pbm) {

				// Generate Request Id if PBM prescription has No Request Id.
				if (pbmPresBean.get("pbm_request_id") != null
						&& !pbmPresBean.get("pbm_request_id").equals("")) {

					pbmRequestId = (String)pbmPresBean.get("pbm_request_id");
				}else {

					// REQUESTID format : SERVICEREGNO-PBMREQUESTID-YYYYMMDDHH24MISS
					String timeFormatStr = DataBaseUtil.getStringValueFromDb("SELECT to_char(now(), 'yyyymmddhh24miss')");
					pbmRequestId = new PBMRequestsDAO().getGeneratedPBMRequestId();
					pbmRequestId = service_reg_no + "-" +pbmRequestId + "-" +timeFormatStr;
				}

				String pbmResubmitRequestId =
					pbmPresBean.get("pbm_resubmit_request_id") != null ? (String)pbmPresBean.get("pbm_resubmit_request_id") : null;

	    		pbmPrescriptionRequestId =
	    			(pbmResubmitRequestId != null &&  !pbmResubmitRequestId.equals("")) ? pbmResubmitRequestId : pbmRequestId;

	    		isResubmit = (pbmPrescriptionRequestId.equals(pbmRequestId)) ? "N" : "Y";
			}

			// File Name Format Example : REQUESTID-PBMPRESCID.xml : PF1506-PR000115-20140227112844-66.xml
			String file_name =  pbmPrescriptionRequestId +"-"+ pbmPrescId +".xml";

			BasicDynaBean pbmRequestBean = pbmReqDAO.getBean();
			pbmRequestBean.set("pbm_request_id", pbmRequestId);
			pbmRequestBean.set("pbm_request_type", requestType);
			pbmRequestBean.set("request_date", DateUtil.getCurrentTimestamp());

			BasicDynaBean existingRequestBean = pbmReqDAO.findByKey("pbm_request_id", pbmRequestId);

			if (existingRequestBean == null || updateRequest) {
				pbmRequestBean.set("request_by", userid);
				pbmRequestBean.set("file_name", file_name);
				pbmRequestBean.set("file_id", "");
				pbmRequestBean.set("is_resubmit", isResubmit);
				pbmRequestBean.set("account_group", accountGroup);
				pbmRequestBean.set("center_id", centerId);
				pbmRequestBean.set("pbm_sender_id", service_reg_no);
				pbmRequestBean.set("pbm_receiver_id", receiverId);

			}else if (requestType.equals("Cancellation")) {

			}else {
				// If request XML is viewed, set back the original request type i.e Authorization/Cancellation
				// because for viewing XML request type is Authorization.
				pbmRequestBean.set("pbm_request_type", existingRequestBean.get("pbm_request_type"));
			}

			if (existingRequestBean == null) {
				if (!pbmReqDAO.insert(con, pbmRequestBean)){
					success = false;
					return success;
				}
			}else {
				int i = pbmReqDAO.updateWithName(con, pbmRequestBean.getMap(), "pbm_request_id");
				success = (i > 0);
				if (!success) return success;
			}

			Map<String, Object> keys = new HashMap<String, Object>();
			keys.put("pbm_presc_id", pbmPrescId);
			keys.put("pbm_request_id", pbmPrescriptionRequestId);
			BasicDynaBean pbmPrescReq = pbmPrescReqDAO.findByKey(keys);

			if (pbmPrescReq == null) {
				pbmPrescReq = pbmPrescReqDAO.getBean();
				pbmPrescReq.set("pbm_presc_id", pbmPrescId);
				pbmPrescReq.set("pbm_request_id", pbmPrescriptionRequestId);
				pbmPrescReq.set("mod_time", DateUtil.getCurrentTimestamp());
				pbmPrescReq.set("username", userid);

				if (!pbmPrescReqDAO.insert(con, pbmPrescReq)){
					success = false;
					return success;
				}
			}

			BasicDynaBean pbmPrescBean = getBean();
			pbmPrescBean.set("pbm_presc_id", pbmPrescId);
			pbmPrescBean.set("pbm_request_id", pbmRequestId);
			int i = updateWithName(con, pbmPrescBean.getMap(), "pbm_presc_id");
			success = success && (i > 0);

		}finally {
			DataBaseUtil.commitClose(con, success);
		}
		return success;
	}

	private BasicDynaBean getPBMActivityBean() {
		DynaBeanBuilder builder = new DynaBeanBuilder();
		builder.add("pbm_medicine_pres_id", Integer.class);
		builder.add("medicine_id", Integer.class);
		builder.add("medicine_name");
		builder.add("code_type");
		builder.add("item_code");
		builder.add("mrp", BigDecimal.class);
		builder.add("rate", BigDecimal.class);
		builder.add("discount", BigDecimal.class);
		builder.add("medicine_quantity", BigDecimal.class);
		builder.add("amount", BigDecimal.class);
		builder.add("claim_net_amount", BigDecimal.class);
		builder.add("patient_amount", BigDecimal.class);

		builder.add("total_available_qty", BigDecimal.class);
		builder.add("issue_qty", BigDecimal.class);
		builder.add("user_uom");
		builder.add("user_unit");
		builder.add("package_size", BigDecimal.class);
		builder.add("store_sale_unit");

		return builder.build();
	}

	public BasicDynaBean getPBMPrescItemRateBean(int medicineId, BigDecimal medQty, String itemUOM,
			String visitId, String visitType, String ratePlanId, Integer storeId, int planId,
			boolean calcRate) throws SQLException {

		String vatApplicable = GenericPreferencesDAO.getGenericPreferences().getShowVAT();
		vatApplicable = (vatApplicable != null && vatApplicable.equals("Y")) ? "Y" : "N";

		// Discount type always exclusive of tax.
		String discountType = (vatApplicable.equals("N") || vatApplicable.equals("Y")) ? "E" : "I";

		BasicDynaBean activityBean = getPBMActivityBean();
		activityBean.set("medicine_id", medicineId);
		activityBean.set("medicine_quantity", medQty);

		Integer store_rate_plan_id = null;

		// Doctor prescribed items have issue sale unit as default UOM.
		String sale_unit = itemUOM != null && !itemUOM.equals("") ? itemUOM : "I";

		if (storeId == null) {
			return null;
		}

		BasicDynaBean storeBean = storeDao.findByKey("dept_id", storeId);
		store_rate_plan_id = (Integer)storeBean.get("store_rate_plan_id");

		// When a new item is added store sale unit is defaulted as UOM.
		String store_sale_unit = null;

		if (storeBean.get("sale_unit") != null && !storeBean.get("sale_unit").equals(""))
			store_sale_unit = (String)storeBean.get("sale_unit");

		if (store_rate_plan_id == null) {
			return null;
		}

		Map<String, Object> rkeys = new HashMap<String, Object>();
		rkeys.put("store_rate_plan_id", store_rate_plan_id);
		rkeys.put("medicine_id", medicineId);
		BasicDynaBean storeItemRateBean = storeRateDao.findByKey(rkeys);

		if (storeItemRateBean == null) {
			return null;
		}

		activityBean.set("total_available_qty",
				DataBaseUtil.getBigDecimalValueFromDb(TOTAL_AVAILABLE_QTY, medicineId));
		Integer storeCenterId = -1;
		storeCenterId = (Integer)storeBean.get("center_id");
		BigDecimal sellingPrice = (BigDecimal)getSellingPrice(storeCenterId, ratePlanId, medicineId);
		String taxType = (String)storeItemRateBean.get("tax_type");
		BigDecimal taxRate = storeItemRateBean.get("tax_rate") != null ?
					(BigDecimal)storeItemRateBean.get("tax_rate") :BigDecimal.ZERO;
		BigDecimal mrp = sellingPrice;
		activityBean.set("mrp", sellingPrice);

		BasicDynaBean medBean = storeItemDAO.findByKey("medicine_id", medicineId);
		String healthAuthority = HealthAuthorityPreferencesDAO.getHealthAuthorityPreferences(CenterMasterDAO.getHealthAuthorityForCenter(storeCenterId)).getHealth_authority();
		String[] drugCodeTypes = HealthAuthorityPreferencesDAO.getHealthAuthorityPreferences(healthAuthority).getDrug_code_type();
		BasicDynaBean itemCodeBean = StoreItemCodesDAO.getDrugCodeType(medicineId, drugCodeTypes);
		activityBean.set("medicine_name", (String)medBean.get("medicine_name"));
		if(itemCodeBean != null) {
			activityBean.set("code_type", itemCodeBean.get("code_type"));
			activityBean.set("item_code", itemCodeBean.get("item_code"));
		}

		int insCatId = (Integer)medBean.get("insurance_category_id");
		int medCatId = (Integer)medBean.get("med_category_id");

		String issue_units = (String)medBean.get("issue_units");
		String package_uom = (String)medBean.get("package_uom");
		BigDecimal issue_qty = (BigDecimal)medBean.get("issue_qty");
		BigDecimal issue_base_unit = (BigDecimal)medBean.get("issue_base_unit");

		//BasicDynaBean stockBean = stockDao.findByKey("medicine_id", medicineId);
		//int batchId = (Integer)stockBean.get("item_batch_id");

		//BasicDynaBean itemBatchBean = itembatchDao.findByKey("item_batch_id", batchId);
		//BigDecimal mrp = (BigDecimal)itemBatchBean.get("mrp");
		//activityBean.set("mrp", mrp);

		BasicDynaBean itemCatBean = storeItemCatDao.findByKey("category_id", medCatId);
		String claimable = ((Boolean)itemCatBean.get("claimable")) ? "Y" : "N";
		BigDecimal discountPer = itemCatBean.get("discount") != null ?
				(BigDecimal)itemCatBean.get("discount") :BigDecimal.ZERO;

		BasicDynaBean ratePlanBean = new GenericDAO("organization_details").findByKey("org_id", ratePlanId);
		BigDecimal ratePlanDiscount = ratePlanBean.get("pharmacy_discount_percentage") != null ?
				(BigDecimal)ratePlanBean.get("pharmacy_discount_percentage") :BigDecimal.ZERO;
		//String ratePlanDiscType = (String)ratePlanBean.get("pharmacy_discount_type");

		discountPer = discountPer.add(ratePlanDiscount);

		BasicDynaBean planBean = planDao.findByKey("plan_id", planId);
		String is_copay_pc_on_post_discnt_amt = planBean.get("is_copay_pc_on_post_discnt_amt") != null ?
			(String)planBean.get("is_copay_pc_on_post_discnt_amt") : "N";

		Map<String, Object> keys = new HashMap<String, Object>();
		keys.put("insurance_category_id", insCatId);
		keys.put("patient_type", visitType);
		keys.put("plan_id", planId);
		BasicDynaBean planDetBean = planDetDao.findByKey(keys);

		BigDecimal patient_amount = planDetBean.get("patient_amount") != null
				? (BigDecimal)planDetBean.get("patient_amount") : BigDecimal.ZERO;
		BigDecimal patient_amount_per_category = planDetBean.get("patient_amount_per_category") != null
				? (BigDecimal)planDetBean.get("patient_amount_per_category") : BigDecimal.ZERO;
		BigDecimal patient_percent = planDetBean.get("patient_percent") != null
				? (BigDecimal)planDetBean.get("patient_percent") : BigDecimal.ZERO;
		BigDecimal patient_amount_cap = planDetBean.get("patient_amount_cap") != null
				? (BigDecimal)planDetBean.get("patient_amount_cap") : BigDecimal.ZERO;

		BasicDynaBean insItemCatBean = itemInsCatDao.findByKey("insurance_category_id", insCatId);
		String insurancePayable = (String)insItemCatBean.get("insurance_payable");

		BigDecimal pkgSize = issue_base_unit;
		BigDecimal discount = BigDecimal.ZERO;

		medQty = (sale_unit.equals("P")) ? pkgSize.multiply(medQty) : medQty;
    BigDecimal pkgQty = (pkgSize.compareTo(BigDecimal.ZERO) == 0)
        ? BigDecimal.ZERO : (medQty.divide(pkgSize, 4, RoundingMode.HALF_UP));
    BigDecimal iamount = (pkgQty.compareTo(BigDecimal.ZERO) == 0)
        ? BigDecimal.ZERO : ConversionUtils.setScale(mrp.multiply(pkgQty));

		/** MB -- MRP Based(with bonus)
			M  -- MRP Based(without bonus)
			CB -- CP Based(with bonus)
			C  -- CP Based(without bonus)*/
		if (taxType.equals("M") || taxType.equals("MB")) {

			if (discountType.equals("E")) {
				// discount excluding VAT: this is normal
				BigDecimal adjMrp = taxRate.compareTo(BigDecimal.ZERO) != 0 ?
						(mrp.multiply(new BigDecimal("100"))).divide(taxRate.add(new BigDecimal("100"))) : mrp;
				discount = (pkgSize.compareTo(BigDecimal.ZERO) == 0)
            ? BigDecimal.ZERO : pkgQty.multiply(discountPer).multiply(adjMrp)
            .divide(new BigDecimal("100"));
			} else {
				// discount includes VAT, on the total amount
				discount = (pkgSize.compareTo(BigDecimal.ZERO) == 0)
            ? BigDecimal.ZERO : pkgQty.multiply(discountPer).multiply(mrp)
            .divide(new BigDecimal("100"));
			}

		} else {
			// non-pharma item: deduct discount (on retail price) before calculating tax
			// discount Type is not considered here, assumed to be inclusive of VAT always.
			discount = (pkgSize.compareTo(BigDecimal.ZERO) == 0)
          ? BigDecimal.ZERO : pkgQty.multiply(discountPer).multiply(mrp)
          .divide(new BigDecimal("100"));
		}

		BigDecimal unitRate = (pkgSize.compareTo(BigDecimal.ZERO) == 0)
				? BigDecimal.ZERO : mrp.divide(pkgSize, 4, RoundingMode.HALF_UP);

		// final amount is initial amount - discount
		BigDecimal amount = iamount.subtract(discount);

		//For claim the unit quantity is required.
		String units = sale_unit;
		String user_uom = (units.equals("I")) ? issue_units : package_uom;

		// TODO: Need to check if ERX also requires rate as zero in Request XML.
		if (calcRate) {
			activityBean.set("amount", amount);
			activityBean.set("discount", discount);
			activityBean.set("rate", unitRate);
		}else {
			activityBean.set("amount", BigDecimal.ZERO);
			activityBean.set("discount", BigDecimal.ZERO);
			activityBean.set("rate", BigDecimal.ZERO);
		}
		activityBean.set("issue_qty", issue_qty);
		activityBean.set("user_uom", user_uom);
		activityBean.set("user_unit", units);
		activityBean.set("package_size", issue_base_unit);
		activityBean.set("store_sale_unit", store_sale_unit);

		BigDecimal patientAmt = amount;
		BigDecimal claimAmt = BigDecimal.ZERO;

		if (planId != 0) {
			if (insurancePayable.equals("Y")) {
				BigDecimal copayBasis = (is_copay_pc_on_post_discnt_amt.equals("Y")) ? amount : amount.add(discount);
				claimAmt = ConversionUtils.setScale((amount.subtract(patient_amount))
						.subtract(copayBasis.multiply(patient_percent).divide(new BigDecimal("100"))));
				patientAmt = amount.subtract(claimAmt);
				if (claimAmt.compareTo(BigDecimal.ZERO) < 0) {
					claimAmt = BigDecimal.ZERO;
					patientAmt = amount;
				} else if (patient_amount_cap.compareTo(BigDecimal.ZERO) > 0
								&& patientAmt.compareTo(patient_amount_cap) >= 0) {
					patientAmt = patient_amount_cap;
					claimAmt = amount.subtract(patientAmt);
				}
			} else {
				claimAmt =BigDecimal.ZERO;
				patientAmt = amount;
			}
		} else if (claimable.equals("Y")) {
			// if claimable, entire amount is claim
			claimAmt = amount;
			patientAmt = BigDecimal.ZERO;
		}

		if (calcRate) {
			activityBean.set("claim_net_amount", claimAmt);
			activityBean.set("patient_amount", patientAmt);
		}else {
			activityBean.set("claim_net_amount", BigDecimal.ZERO);
			activityBean.set("patient_amount", BigDecimal.ZERO);
		}
		return activityBean;
	}

	public List<BasicDynaBean> getPBMPrescRates(Connection con, int pbmPrescId, String visitId, String visitType,
			String ratePlanId, Integer storeId, int planId) throws SQLException {

		List<BasicDynaBean> rateDetails = new ArrayList<BasicDynaBean>();
		List<BasicDynaBean> pbmPrescActivities =  pbmMedPrescDAO.findAllByKey(con, "pbm_presc_id", pbmPrescId);

		for (BasicDynaBean activity : pbmPrescActivities) {

			int pbm_medicine_pres_id = (Integer)activity.get("pbm_medicine_pres_id");

			int medicineId = activity.get("medicine_id") != null ? (Integer)activity.get("medicine_id") : 0;
			if (medicineId != 0) {
				BigDecimal medQty = activity.get("medicine_quantity") != null ? (BigDecimal)activity.get("medicine_quantity") : BigDecimal.ZERO;
				String itemUOM = (String)activity.get("user_unit");

				String status = (String)activity.get("pbm_status");
				boolean calcRate = (!status.equals("C"));

				BasicDynaBean activityBean = getPBMPrescItemRateBean(medicineId, medQty, itemUOM,
						visitId, visitType, ratePlanId, storeId, planId, calcRate);

				if (activityBean != null) {
					activityBean.set("pbm_medicine_pres_id", pbm_medicine_pres_id);
					rateDetails.add(activityBean);
				}
			}
		}
		return rateDetails;
	}

	public boolean savePBMPrescStore(Connection con, int pbmPrescId, String visitId,
			String visitType, String ratePlanId, int planId, Integer storeId, String userid) throws SQLException, IOException {

	  if (storeId == null)
		  return true;

	  boolean success = true;
	  storeUpdate: {

		// Get prescriptions rate, discount, claim using planId and storeId.
		List<BasicDynaBean> pbmPrescRates = getPBMPrescRates(con, pbmPrescId,
				visitId, visitType, ratePlanId, storeId, planId);

		for (BasicDynaBean rateBean : pbmPrescRates) {

			int pbm_medicine_pres_id = (Integer)rateBean.get("pbm_medicine_pres_id");

			BasicDynaBean activity = pbmMedPrescDAO.findByKey(con, "pbm_medicine_pres_id", pbm_medicine_pres_id);
			activity.set("pbm_medicine_pres_id", pbm_medicine_pres_id);
			activity.set("rate", rateBean.get("rate"));
			activity.set("discount", rateBean.get("discount"));
			activity.set("amount", rateBean.get("amount"));
			activity.set("claim_net_amount", rateBean.get("claim_net_amount"));
			activity.set("package_size", rateBean.get("package_size"));
			activity.set("username", userid);

			// Update UOM for newly added prescriptions for existing prescription.
			if (activity.get("user_unit") == null || activity.get("user_unit").equals("")) {
				activity.set("user_unit", (String)rateBean.get("user_unit"));
			}

			int i = pbmMedPrescDAO.updateWithName(con, activity.getMap(), "pbm_medicine_pres_id");
			success = (i > 0);
			if (!success)
				break storeUpdate;
		}

		BasicDynaBean pbmPrescBean = getBean();
		pbmPrescBean.set("pbm_presc_id", pbmPrescId);
		pbmPrescBean.set("pbm_store_id", storeId);
		int i = updateWithName(con, pbmPrescBean.getMap(), "pbm_presc_id");
		success = (i > 0);
		if (!success)
			break storeUpdate;

		}// label storeUpdate

	   return success;
	}

	public boolean markPBMPrescSent(int pbm_presc_id, String patient_id) throws SQLException, IOException {
		boolean success = true;
		boolean setResetSuccess = false;
		Connection con = null;
		try {

			List<String> columns = new ArrayList<String>();
			columns.add("pbm_presc_id");
			columns.add("status");
			columns.add("resubmit_type");

			Map<String, Object> key = new HashMap<String, Object>();
			key.put("pbm_presc_id", pbm_presc_id);
			BasicDynaBean pbmPBean = findByKey(columns, key);

			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			BasicDynaBean pbmPrescBean = getBean();
			pbmPrescBean.set("pbm_presc_id", pbm_presc_id);
			pbmPrescBean.set("status", "S");
			int i = updateWithName(con, pbmPrescBean.getMap(), "pbm_presc_id");
			success = (i > 0);

			if (success) {

				if (pbmPBean != null) {
					// Set the flag to true if status changes from Open to Sent, i.e., O to S
					// or status changes from "Mark for Resubmission", type correction to Sent,
					// i.e., from R, type Correction to S
					String prescStatus = pbmPBean.get("status") != null ? (String)pbmPBean.get("status") :  null;
					String resubmitType = pbmPBean.get("resubmit_type") != null ? (String)pbmPBean.get("resubmit_type") : null;
					if (("O").equalsIgnoreCase(prescStatus) ||
							(("R").equalsIgnoreCase(prescStatus) && ("correction").equalsIgnoreCase(resubmitType))) {
						// Set the flags for the diagnosis codes
						setResetSuccess = mrdDao.setResetDiagFlags(con, patient_id, true);
						success = success && setResetSuccess;
					}
				}
			}
		}finally {
			DataBaseUtil.commitClose(con, success);
		}

		if (!success) {
			logger.error("Error while marking PBM Prescription "+ pbm_presc_id+" as Sent.");
		}
		return success;
	}

	public boolean markPBMPrescClosed(int pbm_presc_id, String patientId) throws SQLException, IOException  {
		boolean success = true;
		Connection con = null;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			BasicDynaBean pbmPrescBean = getBean();
			pbmPrescBean.set("pbm_presc_id", pbm_presc_id);
			// Leave the status as Sent, no need to change as Closed for cancelled request.
			// Bug # 40097
			//pbmPrescBean.set("status", "C");
			int i = updateWithName(con, pbmPrescBean.getMap(), "pbm_presc_id");
			success = (i > 0);

			// Reset sent_for_approval flag
			if (success) {
				// Check if there are any prescriptions for this visit that is in status 'C', or 'S' or 'R' type 'internal complaint'
				List<BasicDynaBean> sentClosedPBMPrescs = getSentClosedPBMPrescsForVisit(con, patientId, pbm_presc_id);
				if (sentClosedPBMPrescs == null || sentClosedPBMPrescs.size() == 0) { // no prescriptions exist
					boolean setResetSuccess = mrdDao.setResetDiagFlags(con, patientId, false);
					success = success && setResetSuccess;
				}
			}
		}finally {
			DataBaseUtil.commitClose(con, success);
		}

		if (!success) {
			logger.error("Error while marking PBM Prescription "+ pbm_presc_id+" as Closed.");
		}
		return success;
	}

	public String convertQtyToPackageUOM(String pbmPrescId) throws SQLException, IOException {

		boolean success = true;
		Connection con = null;
		try {

			int pbm_pres_id = Integer.parseInt(pbmPrescId);
			List<BasicDynaBean> pbmPrescActivities =  pbmMedPrescDAO.findAllByKey("pbm_presc_id", pbm_pres_id);

			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			for (BasicDynaBean activity : pbmPrescActivities) {

				int pbm_medicine_pres_id = (Integer)activity.get("pbm_medicine_pres_id");

				int medicineId = activity.get("medicine_id") != null ? (Integer)activity.get("medicine_id") : 0;
				if (medicineId != 0) {
					BigDecimal medQty = activity.get("medicine_quantity") != null ? (BigDecimal)activity.get("medicine_quantity") : BigDecimal.ZERO;
					String itemUOM = (String)activity.get("user_unit");

					BasicDynaBean medBean = storeItemDAO.findByKey("medicine_id", medicineId);
					BigDecimal issue_base_unit = (BigDecimal)medBean.get("issue_base_unit");
					BigDecimal pkgSize = issue_base_unit;
					if (null != itemUOM && itemUOM.equals("I"))
						medQty =  (pkgSize.compareTo(BigDecimal.ZERO) == 0 || medQty.compareTo(BigDecimal.ZERO) == 0)
							? BigDecimal.ZERO : medQty.divide(pkgSize, 4, RoundingMode.HALF_UP);

					activity.set("medicine_quantity", medQty);
					activity.set("user_unit", "P");

					int i = pbmMedPrescDAO.updateWithName(con, activity.getMap(), "pbm_medicine_pres_id");
					success = (i > 0);

					if (!success) {
						String errMsg = "Error while converting Qty from " +
							"Issue UOM to Package UOM for Medicine Id ("+medicineId+") of PBM Presc. Id : "+ pbmPrescId;
						logger.error(errMsg);
						return errMsg;
					}
				}
			}
		}finally {
			DataBaseUtil.commitClose(con, success);
		}
		return null;
	}

	public static final String GET_PBM_PRESCRIPTION_DETAILS =
			"   SELECT  pmp.*, sid.medicine_name as item_name, g.generic_name, mr.route_name, " +
			"		icm.category AS category_name, mm.manf_name, mm.manf_mnemonic, if.item_form_name, " +
			"		su.unit_name, idc.status as denial_code_status, idc.type as denial_code_type, " +
			"		idc.code_description as denial_desc, idc.example " +
			"	FROM pbm_medicine_prescriptions pmp " +
			"		LEFT JOIN store_item_details sid ON (pmp.medicine_id=sid.medicine_id) " +
			" 		LEFT JOIN item_form_master if ON (pmp.item_form_id=if.item_form_id) " +
			"		LEFT OUTER JOIN generic_name g ON (pmp.generic_code = g.generic_code) " +
			"		LEFT OUTER JOIN medicine_route mr ON (mr.route_id=pmp.route_of_admin)" +
			"   	LEFT OUTER JOIN manf_master mm on mm.manf_code=sid.manf_name " +
			"   	LEFT OUTER JOIN store_category_master icm on sid.med_category_id=icm.category_id " +
			"		LEFT OUTER JOIN strength_units su ON (pmp.item_strength_units=su.unit_id) " +
			"		LEFT JOIN insurance_denial_codes idc ON (idc.denial_code = pmp.erx_denial_code) " +
			"   WHERE pbm_presc_id = ?";

	public static List<BasicDynaBean> getPbmPrescriptionDetails(Integer pbmPrescId) throws Exception{
		PreparedStatement ps = null;
		Connection con = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_PBM_PRESCRIPTION_DETAILS);
			ps.setInt(1, pbmPrescId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static final String GET_SENT_CLOSED_PBM_PRESCRIPTIONS_FOR_VISIT =
		" select pp.pbm_presc_id from pbm_prescription pp join "
		+ "pbm_request_approval_details prad on pp.pbm_request_id = prad.pbm_request_id "
		+ "join pbm_medicine_prescriptions pmp on pp.pbm_presc_id = pmp.pbm_presc_id "
		+ "where (upper(status) = 'S' or upper(status) = 'C' or "
		+ "(upper(status) = 'R' and (upper(resubmit_type) = 'INTERNAL COMPLAINT' OR upper(resubmit_type) = 'RECONCILIATION')) "
		+ "or upper(approval_status) = 'P') "
		+ "and upper(pbm_request_type) != 'CANCELLATION' "
		+ "and pmp.visit_id = ? and pp.pbm_presc_id != ?";

	public List<BasicDynaBean> getSentClosedPBMPrescsForVisit(Connection conn, String visitId,
															  int pbmPrescId) throws SQLException {
		try (PreparedStatement ps =
					 conn.prepareStatement(GET_SENT_CLOSED_PBM_PRESCRIPTIONS_FOR_VISIT)) {
			ps.setString(1, visitId);
			ps.setInt(2, pbmPrescId);
			return DataBaseUtil.queryToDynaList(ps);
		}
	}

	public static final String GET_SELLING_PRICE = "SELECT "
			+ "	COALESCE((case when textregexeq(sir.selling_price_expr, '^[0-9]') "
			+ "	then sir.selling_price_expr::decimal else null end) ,"
			+ "	ssd.mrp, sid.item_selling_price, 0) AS charge "
			+ " FROM store_item_details sid "
			+ " JOIN LATERAL (select sum(qty) AS qty, max(sibd.mrp) AS mrp "
			+ " from store_stock_details ssd, stores s, store_item_batch_details sibd "
			+ " WHERE s.dept_id=ssd.dept_id and"
			+ " auto_fill_prescriptions and ssd.medicine_id = sid.medicine_id "
			+ " AND ssd.item_batch_id=sibd.item_batch_id "
			+ " AND s.center_id=? ) AS ssd ON ssd.qty IS NOT NULL"
			+ " JOIN organization_details od ON (od.org_id=?) "
			+ " LEFT JOIN store_item_rates sir ON (sir.medicine_id=sid.medicine_id "
			+ " AND sir.store_rate_plan_id=od.store_rate_plan_id) "
			+ " WHERE sid.status='A' AND sid.medicine_id=? ";

	public BigDecimal getSellingPrice(Integer centerId, String orgId, Integer medicineId) throws SQLException {
		  try (Connection con = DataBaseUtil.getConnection();
			   PreparedStatement ps = con.prepareStatement(GET_SELLING_PRICE)) {
			ps.setInt(1, centerId);
			ps.setString(2, orgId);
			ps.setInt(3, medicineId);
			BigDecimal sellingPrice = BigDecimal.ZERO;
			try (ResultSet rs = ps.executeQuery();) {
				while (rs.next()) {
					sellingPrice = rs.getBigDecimal("charge");
				}
			}
			return sellingPrice;
		}
	}

}
