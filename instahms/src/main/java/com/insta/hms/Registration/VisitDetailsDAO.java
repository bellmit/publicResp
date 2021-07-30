package com.insta.hms.Registration;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.billing.BillDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.discharge.DischargeDAO;
import com.insta.hms.emr.EMRDoc;
import com.insta.hms.emr.EMRInterface;
import com.insta.hms.emr.EMRInterface.Helper;
import com.insta.hms.ipservices.BedDTO;
import com.insta.hms.ipservices.IPBedDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDTO;
import com.insta.hms.wardactivities.defineipcareteam.IPCareDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class VisitDetailsDAO extends GenericDAO {

	static Logger logger = LoggerFactory.getLogger(VisitDetailsDAO.class);

	public VisitDetailsDAO() {
		super("patient_registration");
	}

	/*
	 * Patient id generation method: this is based on a set of preferences on how the
	 * type controls the prefix and sequence to use based on center.
	 */
	private static final String VISITID_SEQUENCE_PATTERN =
		" SELECT pattern_id "+
		" FROM (SELECT min(priority) as priority, pattern_id FROM hosp_op_ip_seq_prefs " +
		"  WHERE (visit_type = ?) AND " +
		"        (center_id = ? OR center_id = 0) " +
		" GROUP BY pattern_id ORDER BY priority limit 1) as foo ";

	public static final String getVisitIdPattern(String visitType, int centerId) throws SQLException {

		Connection con = null;
		PreparedStatement stmt = null;

		try {
			con = DataBaseUtil.getConnection();
			stmt = con.prepareStatement(VISITID_SEQUENCE_PATTERN);
			stmt.setString(1, visitType);
			stmt.setInt(2, centerId);

			List<BasicDynaBean> l = DataBaseUtil.queryToDynaList(stmt);
			BasicDynaBean b = l.get(0);
			return (String) b.get("pattern_id");
		} finally {
			DataBaseUtil.closeConnections(con, stmt);
		}
	}

	public static final String getNextVisitId(String visitType, int centerId) throws SQLException {

		String patternId = getVisitIdPattern(visitType, centerId);
		return DataBaseUtil.getNextPatternId(patternId);
	}

	public BasicDynaBean getLatestActiveVisit(Connection con , String mrNo) throws SQLException {
		//Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement("SELECT * FROM patient_registration WHERE mr_no=? AND (center_id=? or 0=?) " +
					"	AND status='A' ORDER BY reg_date DESC limit 1");
			ps.setString(1, mrNo);
			ps.setInt(2, RequestContext.getCenterId());
			ps.setInt(3, RequestContext.getCenterId());
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			//DataBaseUtil.closeConnections(con, ps);
		}
	}

	public BasicDynaBean getLatestActiveVisit(String mrNo) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		try {
			return getLatestActiveVisit(con , mrNo);
		} finally {
			DataBaseUtil.closeConnections(con,null);
		}
	}

	public BasicDynaBean getLatestActiveVisitForMainVisit(Connection con , String mrNo , String mainVisit) throws SQLException {

		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement("SELECT * FROM patient_registration WHERE mr_no=? AND (center_id=? or 0=?) " +
					"	AND status='A' AND main_visit_id=? ORDER BY reg_date DESC limit 1");
			ps.setString(1, mrNo);
			ps.setInt(2, RequestContext.getCenterId());
			ps.setInt(3, RequestContext.getCenterId());
			ps.setString(4, mainVisit);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	public BasicDynaBean getLatestActiveVisitForMainVisit(String mrNo , String mainVisit) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		try {
			return getLatestActiveVisitForMainVisit(con , mrNo , mainVisit);
		} finally {
			DataBaseUtil.closeConnections(con,null);
		}
	}

	public static final String ACTIVE_VISITID_ANOTHER_CENTER =
		"SELECT * FROM patient_registration WHERE mr_no=? and status = 'A' AND center_id!=?";
	public BasicDynaBean getVisitInAnotherCenter(String mrNo) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(ACTIVE_VISITID_ANOTHER_CENTER);
			ps.setString(1, mrNo);
			ps.setInt(2, RequestContext.getCenterId());
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	/**
	 * Get patient visit details (including patient details).
	 * @param patientId
	 * @return
	 * @throws SQLException
	 */

	private static final String GET_PATIENT_VISIT_DETAILS = " SELECT * FROM patient_details_ext_view WHERE patient_id=? ";

	// Warning: Don't use this method other than Patient Header and Report data 
	public static BasicDynaBean getPatientVisitDetailsBean(Connection con, String patientId) throws SQLException {

		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_PATIENT_VISIT_DETAILS);
			ps.setString(1, patientId);

			List list = DataBaseUtil.queryToDynaList(ps);
			if (list.size() > 0) {
				BasicDynaBean bean =  (BasicDynaBean) list.get(0);
				boolean precise = (bean.get("dateofbirth") != null);
	            if (bean.get("expected_dob") != null)
	            	bean.set("age_text", DateUtil.getAgeText((java.sql.Date) bean.get("expected_dob"), precise));
				return bean;
			} else {
				return null;
			}
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	// Warning: Don't use this method other than Patient Header and Report data 
	public static BasicDynaBean getPatientVisitDetailsBean(String patientId) throws SQLException {

		Connection con = null;
        try {
             con = DataBaseUtil.getReadOnlyConnection();
             return getPatientVisitDetailsBean(con, patientId);
        } finally {
            DataBaseUtil.closeConnections(con, null);
        }
	}

	// Warning: Don't use this method other than Patient Header and Report data
	public static Map getPatientVisitDetailsMap(Connection con, String patientId) throws SQLException {
	    PreparedStatement ps = null;
	    //System.out.println("5th Con:"+con.isClosed());
	    try {
	    	ps = con.prepareStatement(GET_PATIENT_VISIT_DETAILS);
	    	ps.setString(1, patientId);
	        List l = DataBaseUtil.queryToDynaList(ps);

	        if (l != null && l.size() > 0) {
	            BasicDynaBean b = (BasicDynaBean) l.get(0);
	            DateUtil.checkAndSetAgeComponents(b);
	            return b.getMap();
	        }
	    } finally {
	        DataBaseUtil.closeConnections(null, ps);
	    }
	    return null;
	}


	// Warning: Don't use this method other than Patient Header and Report data
	public static Map getPatientVisitDetailsMap(String patientId) throws SQLException {
	    Connection con = null;
	    try {
	        con = DataBaseUtil.getReadOnlyConnection();
	        return getPatientVisitDetailsMap(con, patientId);
	    } finally {
	        DataBaseUtil.closeConnections(con, null);
	    }
	}

	public static Map getPatientHeaderDetailsMap(String patientId) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
	    	ps = con.prepareStatement("SELECT * FROM patient_details_header_tag_view WHERE patient_id=?");
	    	ps.setString(1, patientId);
	        List l = DataBaseUtil.queryToDynaList(ps);

	        if (l != null && l.size() > 0) {
	            BasicDynaBean b = (BasicDynaBean) l.get(0);
				boolean precise = (b.get("dateofbirth") != null);
	            if (b.get("expected_dob") != null)
	            	b.set("age_text", DateUtil.getAgeText((java.sql.Date) b.get("expected_dob"), precise));
	            return b.getMap();
	        }
	    } finally {
	        DataBaseUtil.closeConnections(con, ps);
	    }
	    return null;
	}
	
	/**
	 * Get patient visit details (including patient details) center wise.
	 * @param patientId
	 *  @param centerId
	 * @return
	 * @throws SQLException
	 */

	private static final String GET_PATIENT_VISIT_DETAILS_BY_CENTER = " SELECT * FROM patient_details_ext_view WHERE patient_id=? AND center_id = ?";
	public static Map getPatientVisitDetailsMapByCenter(String patientId, Integer centerId) throws SQLException {
	    Connection con = null;
	    try {
	        con = DataBaseUtil.getReadOnlyConnection();
	        return getPatientVisitDetailsMapByCenter(con, patientId, centerId);
	    } finally {
	        DataBaseUtil.closeConnections(con, null);
	    }
	}
	public static Map getPatientVisitDetailsMapByCenter(Connection con, String patientId, Integer centerId) throws SQLException {
	    PreparedStatement ps = null;
	    try {
	    	ps = con.prepareStatement(GET_PATIENT_VISIT_DETAILS_BY_CENTER);
	    	ps.setString(1, patientId);
	    	ps.setInt(2, centerId.intValue());
	        List l = DataBaseUtil.queryToDynaList(ps);

	        if (l != null && l.size() > 0) {
	            BasicDynaBean b = (BasicDynaBean) l.get(0);
				boolean precise = (b.get("dateofbirth") != null);
	            if (b.get("expected_dob") != null)
	            	b.set("age_text", DateUtil.getAgeText((java.sql.Date) b.get("expected_dob"), precise));
	            return b.getMap();
	        }
	    } finally {
	        DataBaseUtil.closeConnections(null, ps);
	    }
	    return null;
	}

	public static String getSponsorTypeLabel(String tpaId) throws SQLException{
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
	    	ps = con.prepareStatement("select st.member_id_label from sponsor_type st join tpa_master tm on(st.sponsor_type_id=tm.sponsor_type_id) where tm.tpa_id=?");
	    	ps.setString(1, tpaId);
	        return DataBaseUtil.getStringValueFromDb(ps);
	    } finally {
	        DataBaseUtil.closeConnections(con, ps);
	    }
	}


    private static final String VISITS_FOR_MRNO = "SELECT pr.mr_no, pr.patient_id, " +
    		" pr.visit_type, dr.doctor_name, pr.reg_date,pr.prior_auth_id,pr.prior_auth_mode_id," +
    		" pr.org_id, od.org_name, pr.bed_type AS bill_bed_type, " +
    		" bn.bed_type AS alloc_bed_type, bn.bed_name AS alloc_bed_name," +
    		" pr.ward_id AS reg_ward_id, wnr.ward_name AS reg_ward_name, " +
    		" wn.ward_name AS alloc_ward_name, pr.center_id " +
    		" FROM patient_registration pr " +
    		" JOIN patient_details pd ON (pd.mr_no = pr.mr_no AND " +
    		" (patient_confidentiality_check(pd.patient_group,pd.mr_no))) " +
    		" LEFT JOIN ward_names wnr ON wnr.ward_no = pr.ward_id " +
    		" LEFT JOIN admission ad ON ad.patient_id = pr.patient_id " +
    		" LEFT JOIN bed_names bn ON bn.bed_id = ad.bed_id" +
    		" LEFT JOIN ward_names wn ON wn.ward_no = bn.ward_no" +
    		" LEFT JOIN organization_details od ON pr.org_id = od.org_id" +
    		" LEFT JOIN doctors dr ON (pr.doctor=dr.doctor_id)" +
    		" WHERE pr.mr_no=? AND pr.center_id = ? ";

	/**
	 * Gets the patient active Or inactive Or all visits for a given mr_no
	 * @param mr_no
	 * @param active -- Allowed values are true for active visits, false for inactive visits, null for all visits
	 * @throws SQLException
	 */
	public static List<BasicDynaBean> getPatientVisits(String mr_no, Boolean active) throws SQLException{
		Connection con = null;
		//System.out.println("4th Con:"+con.isClosed());
		PreparedStatement ps = null;
		int centerId = RequestContext.getCenterId();
		try {
			StringBuilder query = new StringBuilder(VISITS_FOR_MRNO);
			if (active == null) {
			} else {
				if (active) {
					query.append(" AND pr.status='A'");
				} else {
					query.append(" AND pr.status='I'");
				}
			}
		    con = DataBaseUtil.getReadOnlyConnection();
		    ps = con.prepareStatement(query.toString());
		    ps.setString(1, mr_no);
		    ps.setInt(2, centerId);
		    return DataBaseUtil.queryToDynaList(ps);
		} finally {
		    DataBaseUtil.closeConnections(con, ps);
		}
	}
	private static final String VISITS_AND_DOCTORS_AND_COMPLAINTS = " SELECT pr.complaint,pr.doctor,dm.doctor_name from patient_registration pr"
			+ "	LEFT JOIN doctors dm ON (pr.doctor=dm.doctor_id) "
			+ " WHERE pr.patient_id=?";

	public static BasicDynaBean getAllVisitsAndDoctorsByPatientId(String patientId) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(VISITS_AND_DOCTORS_AND_COMPLAINTS);
			ps.setString(1, patientId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String LATEST_VISIT_FOR_MRNO = " SELECT pr.patient_id FROM patient_registration pr"
	    + " JOIN patient_details pd ON (pd.mr_no = pr.mr_no)"
	    + " WHERE pr.mr_no=? AND (patient_confidentiality_check(pd.patient_group,pd.mr_no)) ";
	
	/**
	 * Gets the patient latest active or inactive Visit Id for a given mr_no
	 * @param mr_no
	 * @param active -- Allowed values are true for active visit, false for inactive visit, null for previous visit(active/inactive)
	 * @throws SQLException
	 */

	public static String getPatientLatestVisitId(String mr_no, Boolean active, String visitType) throws SQLException{
		return VisitDetailsDAO.getPatientLatestVisitId(mr_no, active, visitType, 0);
	}
	public static String getPatientLatestVisitId(String mrNo, Boolean active, String visitType, int centerId) throws SQLException{
		String patientId = null;
		
		StringBuilder query = new StringBuilder(LATEST_VISIT_FOR_MRNO);
		List<Object> args = new ArrayList<>();
		args.add(mrNo);
		if (active != null) {
			query.append(" AND status = ?");
			args.add(active ? "A" : "I");
		}
		if (centerId != 0) {
			query.append(" AND center_id=?");
			args.add(centerId);
		}
		if (visitType != null && !visitType.equals("")) {
			query.append(" AND visit_type = ?");
			args.add(visitType);
		}
		query.append(" ORDER BY (reg_date+reg_time) DESC LIMIT 1");
		try (Connection con = DataBaseUtil.getReadOnlyConnection();
				PreparedStatement ps = con.prepareStatement(query.toString())) {
			ListIterator<Object> argsIterator = args.listIterator();
			while (argsIterator.hasNext()) {
				ps.setObject(argsIterator.nextIndex() + 1, argsIterator.next());
			}
			try (ResultSet rs = ps.executeQuery()){
	            if (rs.next()) {
	            	patientId = rs.getString(1);
	            }				
			}
        }
        return patientId;
	}


	private static final String VISITS_AND_DOCTORS =
    	" SELECT pr.*, d.doctor_name FROM patient_registration pr " +
    	"   JOIN patient_details pd ON (pd.mr_no = pr.mr_no AND patient_confidentiality_check(COALESCE(pd.patient_group, 0),pd.mr_no))" +
    	"	LEFT JOIN doctors d ON (pr.doctor=d.doctor_id) " +
    	" WHERE pr.mr_no=?";

    public static List getAllVisitsAndDoctors(String mr_no) throws SQLException {
    	Connection con = DataBaseUtil.getConnection();
    	PreparedStatement ps = null;
    	try {
    		ps = con.prepareStatement(VISITS_AND_DOCTORS);
    		ps.setString(1, mr_no);
    		return DataBaseUtil.queryToDynaList(ps);
    	} finally {
    		DataBaseUtil.closeConnections(con, ps);
    	}
    }

    private static final String CENTER_VISITS_AND_DOCTORS =
        	" SELECT pr.*, d.doctor_name FROM patient_registration pr " +
        	"	LEFT JOIN doctors d ON (pr.doctor=d.doctor_id) " +
        	" WHERE pr.mr_no=?";

    public static List getAllCenterVisitsAndDoctors(String mr_no) throws SQLException {
       	Connection con = DataBaseUtil.getConnection();
       	PreparedStatement ps = null;
       	StringBuilder query = new StringBuilder();
       	query.append(CENTER_VISITS_AND_DOCTORS);
       	int centerId = RequestContext.getCenterId();
       	if(centerId != 0) {
       		query.append(" AND center_id in (?) ");
       	}
       	try {
       		ps = con.prepareStatement(query.toString());
       		ps.setString(1, mr_no);
       		if(centerId != 0) {
       		    ps.setInt(2, centerId);
       		}
       		return DataBaseUtil.queryToDynaList(ps);
       	} finally {
       		DataBaseUtil.closeConnections(con, ps);
       	}
    }

	private static final String GET_PATIENT_MLC_STATUS =
		" SELECT pr.mlc_status, b.status as bill_status " +
		" FROM patient_registration pr " +
		"  LEFT JOIN bill_activity_charge bac ON " +
		"    (bac.activity_code = 'MLREG' AND bac.activity_id = pr.patient_id::text) " +
		"  LEFT JOIN bill_charge bc ON (bac.charge_id = bc.charge_id) " +
		"  LEFT JOIN bill b ON (b.bill_no = bc.bill_no) " +
		" WHERE patient_id = ? ";

	public static BasicDynaBean getPatientMLCStatus(String visitId) throws SQLException {
		return DataBaseUtil.queryToDynaBean(GET_PATIENT_MLC_STATUS, visitId);
	}

	private static final String GET_REG_DATE_REG_CHARGE_ACCEPTED =
		" SELECT pr.reg_date AS visit_reg_date, pd.first_visit_reg_date as patient_reg_date " +
		" FROM patient_details pd " +
		"   LEFT JOIN patient_registration pr ON (pr.mr_no = pd.mr_no AND (pr.reg_charge_accepted = 'Y' OR pd.resource_captured_from = 'uhid')) " +
		" WHERE pd.mr_no=? " +
		" ORDER BY pr.reg_date DESC LIMIT 1";

	public static BasicDynaBean getPreviousRegDateChargeAccepted(String mrNo) throws SQLException {
		return DataBaseUtil.queryToDynaBean(GET_REG_DATE_REG_CHARGE_ACCEPTED, mrNo);
	}

	private static final String GET_PREV_PATIENT_POLICY_ID =
		" SELECT patient_policy_id, reg_date " +
		" FROM  patient_registration pr  " +
		" WHERE pr.mr_no=? " +
		" ORDER BY pr.reg_date DESC LIMIT 1";

	public static BasicDynaBean getPreviousPatientPolicyId(String mrNo) throws SQLException {
		return DataBaseUtil.queryToDynaBean(GET_PREV_PATIENT_POLICY_ID, mrNo);
	}

	 private static String REGFORMS_EMR = "select prc.doc_id,prc.doc_date,prc.username,prc.patient_id,dat.title,"
		 	+ " dat.template_name,dat.access_rights, pd.doc_format, pd.content_type,pr.reg_date"
			+ " FROM patient_registration_cards prc"
			+ " JOIN patient_registration pr USING (patient_id) "
			+ " JOIN patient_documents pd on pd.doc_id = prc.doc_id"
			+ " JOIN doc_all_templates_view dat USING (doc_format, template_id)";

	public static List<EMRDoc> getRegistrationFormsForEMR(String patientId, String mrNo, boolean allVisitsDocs)
			throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;

		List<EMRDoc> docs = new ArrayList<EMRDoc>();
		List<BasicDynaBean> l = null;
		if (allVisitsDocs) {
			l = DataBaseUtil.queryToDynaList(REGFORMS_EMR + " WHERE pr.mr_no=? ", mrNo);
		} else {
			l = DataBaseUtil.queryToDynaList(REGFORMS_EMR + " WHERE prc.patient_id=? " , patientId);
		}
		
		BasicDynaBean printpref = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT);
		int printerId = (Integer) printpref.get("printer_id");
		for (BasicDynaBean b : l) {
			EMRDoc doc = new EMRDoc();
			String format = (String)b.get("doc_format");
			String userName = (String) b.get("username");
			String accessRights = (String) b.get("access_rights");
			String docId = b.get("doc_id").toString();

			doc.setDocid(docId);
			doc.setVisitid(b.get("patient_id").toString());
			doc.setProvider(EMRInterface.Provider.RegistrationFormProvider);
			if (b.get("title") != null && !(b.get("title").equals("")))
				doc.setTitle((String) b.get("title"));
			else
				doc.setTitle((String) b.get("template_name"));
			doc.setDoctor("");
			doc.setDate((java.util.Date)b.get("doc_date"));
			doc.setVisitDate((java.util.Date)b.get("reg_date"));
			doc.setPrinterId(printerId);
			String displayUrl = "/pages/RegistrationDocuments/RegistrationDocumentsPrint.do?_method=print&doc_id="
					+ docId + "&&forcePdf=true&printerId="+printerId;
			if (format.equals("doc_hvf_templates"))
				displayUrl += "&allFields=Y";
			doc.setDisplayUrl(displayUrl);
			doc.setUpdatedDate(b.get("doc_date").toString());
			doc.setAuthorized(Helper.getAuthorized(userName, accessRights));
			doc.setUpdatedBy(userName);

			if (format.equals("doc_rtf_templates")) {
				doc.setPdfSupported(false);
			} else if (format.equals("doc_fileupload")) {
				String contentType = (String)b.get("content_type");
				if (contentType.equals("application/pdf") || contentType.split("/")[0].equals("image")) {
					doc.setPdfSupported(true);
				} else {
					doc.setPdfSupported(false);
				}
			} else {
				doc.setPdfSupported(true);
			}
			doc.setType("SYS_RG");
			doc.setAccessRights(accessRights);
			docs.add(doc);
		}
		return docs;
	}

	private static String GET_REG_DETAILS = "select pr.mr_no,pr.patient_id,prc.username from patient_registration pr"
			+ " JOIN patient_registration_cards prc on prc.patient_id = pr.patient_id "
			+ " WHERE prc.doc_id=?";

	public static BasicDynaBean getRegInfoFromDocidForEMR(int doc_id)
			throws SQLException {
		List list = DataBaseUtil.queryToDynaList(GET_REG_DETAILS, doc_id);
		if (list.size() > 0) {
			return (BasicDynaBean) list.get(0);
		}
		return null;
	}

	private static String GET_REVISIT_DETAILS = "SELECT patient_visit_count,consultation_id,visited_date," +
			" allowed_revisit_count,doctor_name " +
			" FROM " +
			"  (SELECT count(*) AS patient_visit_count " +
			"	FROM doctor_consultation dc  JOIN doctors d ON d.doctor_id = dc.doctor_name " +
			"	WHERE mr_no=?  " +
			"	AND date(current_date) <= date(visited_date)+op_consultation_validity::integer " +
			"	AND doctor_id=?) AS vc," +
			" (SELECT consultation_id,visited_date,allowed_revisit_count,d.doctor_name FROM doctor_consultation dc  " +
			"	JOIN doctors d ON d.doctor_id = dc.doctor_name WHERE mr_no=? ORDER BY " +
			"	visited_date DESC LIMIT 1) AS det";

	public static BasicDynaBean getRevisitDetails(String mrno, String doctorId)
			throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		List list = null;
		BasicDynaBean VisitBean = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_REVISIT_DETAILS);
			ps.setString(1, mrno);
			ps.setString(2, doctorId);
			ps.setString(3, mrno);
			list = DataBaseUtil.queryToDynaList(ps);
			if (null != list && list.size() > 0 )
				VisitBean = (BasicDynaBean)list.get(0);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return VisitBean;

	}
	public boolean updatePatientRegistration(Connection con,BasicDynaBean patientDetails)throws Exception{
		Map<String, String> keys = new HashMap<String, String>();
		keys.put("patient_id", (String)patientDetails.get("patient_id"));
		return update(con, patientDetails.getMap(), keys) > 0;
	}


	private final static String VISIT_DETAILS = "SELECT pr.*, tm.tpa_name, stm.tpa_name AS sec_tpa_name FROM patient_registration pr " +
			" LEFT OUTER JOIN tpa_master tm ON pr.primary_sponsor_id=tm.tpa_id " +
			" LEFT OUTER JOIN tpa_master stm ON pr.secondary_sponsor_id=stm.tpa_id " +
			" WHERE patient_id=?";
	public static BasicDynaBean getVisitDetails(String visitId) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			con.setAutoCommit(false);
			ps = con.prepareStatement(VISIT_DETAILS);
			ps.setString(1, visitId);
			List l = DataBaseUtil.queryToDynaList(ps);
			if (l != null && !l.isEmpty())
				return (BasicDynaBean) l.get(0);
		} finally {
			DataBaseUtil.closeConnections(null, ps);
			DataBaseUtil.commitClose(con, true);
		}
		return null;
	}

	public static BasicDynaBean getVisitDetails(Connection con, String visitId) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(VISIT_DETAILS);
			ps.setString(1, visitId);
			List l = DataBaseUtil.queryToDynaList(ps);
			if (l != null && !l.isEmpty())
				return (BasicDynaBean) l.get(0);
		} finally {
			DataBaseUtil.closeConnections(null,ps);
		}
		return null;
	}

	private static final String UPDATE_VISIT_STATUS = "UPDATE patient_registration SET status=?, user_name=? WHERE patient_id=?";

	public static void updateVisitStatus(Connection con, String patientId, String status, String remarks, String userName)
		throws SQLException {
		PreparedStatement ps = con.prepareStatement(UPDATE_VISIT_STATUS);
		int i=1;
		ps.setString(i++, status);
		ps.setString(i++, userName);
		ps.setString(i++, patientId);
		ps.executeUpdate();
		ps.close();
	}

	private static final String UPDATE_INSURANCE_STATUS = "UPDATE patient_registration SET insurance_id=? WHERE patient_id=?";

	public static void updateInsuranceStatus(Connection con, String patientId, Integer insuId)
		throws SQLException {
		PreparedStatement ps = con.prepareStatement(UPDATE_INSURANCE_STATUS);
		int i=1;
		ps.setInt(i++, insuId);
		ps.setString(i++, patientId);
		ps.executeUpdate();
		ps.close();
	}

	private static final String GET_CREDIT_NOTES = "SELECT b.* from bill b "
			+ "JOIN bill_credit_notes bcn ON(bcn.credit_note_bill_no = b.bill_no) "
			+ "JOIN bill nb ON(bcn.bill_no=nb.bill_no and nb.is_tpa='t') "
			+ "WHERE b.visit_id=? and not b.RESTRICTION_TYPE='P' and b.TOTAL_AMOUNT < 0";
	public static List<BasicDynaBean> getCreditNoteList(String visitId) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			con.setAutoCommit(false);
			ps = con.prepareStatement(GET_CREDIT_NOTES);
			ps.setString(1, visitId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(null, ps);
			DataBaseUtil.commitClose(con, true);
		}
	}

	private static final String VISIT_TYPE = " SELECT visit_type from patient_registration WHERE patient_id=? ";
    public static String getVisitType(String patientId) throws SQLException{
        if (patientId == null || patientId.equals("")) return null;
        Connection con = DataBaseUtil.getConnection();
        try {
            return getVisitType(con, patientId);
        } finally {
            DataBaseUtil.closeConnections(con, null);
        }
    }

    public static String getVisitType(Connection con, String patientId) throws SQLException{
        if (patientId == null || patientId.equals("")) return null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = con.prepareStatement(VISIT_TYPE);
            ps.setString(1, patientId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("visit_type");
            }
        } finally {
            DataBaseUtil.closeConnections(null, ps, rs);
        }
        return null;
    }


    private static final String CHECK_VISIT_STATUS = " SELECT status FROM patient_registration WHERE patient_id= ? ";
    public static boolean isVisitActive(String visitId) throws SQLException{
        Connection con = DataBaseUtil.getConnection();
        boolean isActive = false;
        try {
        	isActive = isVisitActive(con, visitId);
        } finally {
            DataBaseUtil.closeConnections(con, null);
        }
        return isActive;
    }

    public static boolean isVisitActive(Connection con, String visitId) throws SQLException{

	    PreparedStatement ps = null;
	    boolean isActive = false;
	    try {
            ps = con.prepareStatement(CHECK_VISIT_STATUS);
            ps.setString(1, visitId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                if (rs.getString("status").equals("A"))
                    isActive = true;
            }

        } finally {
            DataBaseUtil.closeConnections(null, ps);
            System.out.println("1st Con:"+con.isClosed());
        }
        return isActive;
	}


    public static final  String GET_MRNO = " SELECT mr_no FROM patient_registration WHERE patient_id = ? ";
    public static String getMrno(String visitId)throws SQLException{
        Connection con = null;
        PreparedStatement ps = null;
        try{
            con = DataBaseUtil.getReadOnlyConnection();
            ps = con.prepareStatement(GET_MRNO);
            ps.setString(1, visitId);
            return DataBaseUtil.getStringValueFromDb(ps);
        }finally{
            DataBaseUtil.closeConnections(con, ps);
        }
    }
    
    public static String getMrno(Connection con, String visitId)throws SQLException{
      PreparedStatement ps = null;
      try{
          ps = con.prepareStatement(GET_MRNO);
          ps.setString(1, visitId);
          return DataBaseUtil.getStringValueFromDb(ps);
      }finally{
        DataBaseUtil.closeConnections(null, ps);
      }
  }

    public static final String UPDATE_PATIENT_REGISTRATION =
        "update patient_registration set status='A',discharge_date = null,discharge_time = null, user_name=? "+
        " where patient_id=? ";
    public static final String UPDATE_PATIENT_DISCHARGE = "update patient_registration set discharge_flag='', patient_discharge_status = 'N', discharge_type_id = null "+
        " where patient_id=?" ;
    public static final String GET_DISCHARGE_STATUS = "SELECT count(patient_id) from patient_registration "+
        " where patient_id=? " ;
    public static final String UPDATE_DISCHARGE_STATUS = "update bill set discharge_status='N' " +
        " where visit_id=? AND bill_type='C' and status NOT IN ('X','C') ";

    public static int getPatientDischargeStatus(String patientId)throws SQLException{
            Connection con = null;
            PreparedStatement ps = null;

        try {
            con = DataBaseUtil.getReadOnlyConnection();
            ps = con.prepareStatement(GET_DISCHARGE_STATUS);
            ps.setString(1,patientId);
            ResultSet rs = ps.executeQuery();
            int statusCount = 0;
            while(rs.next()){
                statusCount = rs.getInt("count");
            }
            return statusCount;
        }
        finally {
            DataBaseUtil.closeConnections(con, ps);
        }
    }

    public static Boolean readmitPatient(String patientId,String visitType, String username) throws Exception{
	    boolean success=false;
	    int regcount=0;
	    int discount=0;
	    int disStatus=0;
	    Connection con = null;
	    PreparedStatement psReg = null;
	    PreparedStatement psDis = null;
	    PreparedStatement psDisStatus = null;
	    try {
	        con = DataBaseUtil.getReadOnlyConnection();
	        con.setAutoCommit(false);
	        int statusCount = getPatientDischargeStatus(patientId);
	        if ((statusCount!=0)){
	                psReg = con.prepareStatement(UPDATE_PATIENT_REGISTRATION);
	                psReg.setString(1, username);
	                psReg.setString(2, patientId);
	                regcount = psReg.executeUpdate();
	                psDis = con.prepareStatement(UPDATE_PATIENT_DISCHARGE);
	                psDis.setString(1,patientId);
	                discount = psDis.executeUpdate();
	                psDisStatus = con.prepareStatement(UPDATE_DISCHARGE_STATUS);
	                psDisStatus.setString(1, patientId);
	                disStatus = psDisStatus.executeUpdate();
	            if (regcount >= 0 && discount >= 0 && disStatus >= 0) { success = true; }else{ success= false;}
	        }else {
	                psReg = con.prepareStatement(UPDATE_PATIENT_REGISTRATION);
	                psReg.setString(1, username);
	                psReg.setString(2, patientId);
	                regcount = psReg.executeUpdate();
	                if (regcount >= 0) { success = true; }else{ success= false;}
	        }
	        if ((success)&&("i".equals(visitType))) {

	            IPBedDAO bedDAO = new IPBedDAO();
	            BedDTO admissionBed = bedDAO.getAdmissionBed(patientId);
	            if ((success) && (admissionBed.getPatientid() != null)) {
	                admissionBed.setBed_id(0);
	                admissionBed.setEstimateddays(0);
	                admissionBed.setDaycare("N");
	                success = bedDAO.updateAdmission(con, admissionBed);//update admission with bed_id 0 to bring the readmitting patient to bed notallocated state
	            }

	            //insert to care team if no care team
	            
	            IPCareDAO visitCareTeamDAO = new IPCareDAO() ;
	            BasicDynaBean visitDetailsBean = new VisitDetailsDAO().findByKey(con, "patient_id",patientId);
	            
	            if ( visitCareTeamDAO.findByKey(con, "patient_id",patientId) == null ){
	            	//no care team define,admitting doctor will be part of care team
					success &= visitCareTeamDAO.insertVisitCareDeatils(con, visitDetailsBean);
	            	
	            }
	        }
	        // reset financial State
	        if ((success)){
	            DischargeDAO disDao = new DischargeDAO();
	            if(disDao.checkIfPatientDischargeEntryExists(patientId)!=null) {
	            	success = disDao.updateFinancialDischargeDetails(con, patientId, false, username);
	            }
	        }
	        return success;
	    } finally {
	        psReg.close();
	        psDis.close();
	        psDisStatus.close();
	        DataBaseUtil.commitClose(con, success);
	    }

	}


	private static final String PATIENT_LAST_IP_VISIT = "SELECT patient_id,discharge_date,discharge_time,doctor AS admitting_doctor " +
			" FROM patient_registration WHERE mr_no = ? AND visit_type='i' AND status='I' " +
			" ORDER BY discharge_date DESC,discharge_time DESC LIMIT 1";

	public static BasicDynaBean getPatientLastIpVisitDischargeDoctor(String mrno) throws SQLException {
		return DataBaseUtil.queryToDynaBean(PATIENT_LAST_IP_VISIT, mrno);
	}

	private static final String PATIENT_POLICY_DETAILS = "select distinct(ppd.member_id),category_id,tpa_id,pr.plan_id,ppd.policy_holder_name, " +
		 " to_char(policy_validity_start,'dd-mm-yyyy') as policy_validity_start, " +
		 " to_char(policy_validity_end,'dd-mm-yyyy') as policy_validity_end,ppd.mr_no,pr.insurance_company, ppd.patient_policy_id " +
		 " from patient_policy_details ppd " +
		 " LEFT JOIN patient_registration pr ON (ppd.patient_policy_id = pr.patient_policy_id) " +
		 " where pr.mr_no = ? " ;

	public static List<BasicDynaBean> getPatientPolicyNos(String mrNo) throws SQLException {
		return DataBaseUtil.queryToDynaList(PATIENT_POLICY_DETAILS, mrNo);
	}

	/*
	 * Admitting doctor/department-doctor visits based on latest Main or Revisit,
	 * Used for checking revisit validity. i.e Followup or Revisit
	 */

	private static final String GET_PATIENT_PREVIOUS_VISITS_DOCTOR =
		" SELECT patient_id, op_type, main_visit_id, (reg_date + reg_time) AS visited_date, doctor AS doctor_name" +
		" FROM patient_registration " +
		" WHERE  (reg_date + reg_time) >= (SELECT (reg_date + reg_time) FROM patient_registration " +
		" 	WHERE mr_no=? AND doctor=? AND (op_type ='M' OR op_type = 'R') AND visit_type = 'o' ORDER BY (reg_date + reg_time) DESC LIMIT 1) " +
		" AND mr_no=? AND doctor=? AND visit_type = 'o' AND op_type != 'D' AND use_drg = 'N' " +
		" ORDER BY (reg_date + reg_time) " ;

	private static final String GET_PATIENT_PREVIOUS_VISITS_DEPARTMENT =
		"  SELECT patient_id, op_type, main_visit_id, (reg_date + reg_time) AS visited_date, doctor AS doctor_name, dept_name  " +
		" FROM patient_registration" +
		" WHERE  (reg_date + reg_time) >= (SELECT (reg_date + reg_time) FROM patient_registration" +
		"		 WHERE mr_no=? AND doctor is not null AND trim(doctor) != '' " +
		"		AND dept_name=(SELECT dept_id FROM doctors WHERE doctor_id =? ) " +
		"		AND (op_type ='M' OR op_type = 'R') AND visit_type = 'o' ORDER BY (reg_date + reg_time) DESC LIMIT 1) " +
		" AND mr_no=? AND doctor is not null AND trim(doctor) != '' " +
		" AND dept_name =(SELECT dept_id FROM doctors WHERE doctor_id = ?) AND visit_type = 'o' AND op_type != 'D' AND use_drg = 'N' " +
		" ORDER BY (reg_date + reg_time) " ;

	public static List<BasicDynaBean> getPatientPreviousVisits(String mrNo, String doctor) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		RegistrationPreferencesDTO regPrefs = RegistrationPreferencesDAO.getRegistrationPreferences();
		String visit_type_dependence = regPrefs.getVisit_type_dependence(); // D - Doctor, S - Department/Speciality
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			if (visit_type_dependence.equals("D")) {
				ps = con.prepareStatement(GET_PATIENT_PREVIOUS_VISITS_DOCTOR);
			}else {
				ps = con.prepareStatement(GET_PATIENT_PREVIOUS_VISITS_DEPARTMENT);
			}
			ps.setString(1, mrNo);
			ps.setString(2, doctor);
			ps.setString(3, mrNo);
			ps.setString(4, doctor);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_PATIENT_DOCTOR_PREVIOUS_MAIN_VISIT =
		" SELECT patient_id, op_type, main_visit_id, (reg_date + reg_time) AS visited_date, doctor AS doctor_name" +
		" FROM patient_registration " +
		" WHERE  (reg_date + reg_time) >= (SELECT (reg_date + reg_time) FROM patient_registration " +
		" 		WHERE mr_no=? AND doctor=? " +
		"		AND (op_type ='M' OR op_type = 'R') AND visit_type = 'o' ORDER BY (reg_date + reg_time) DESC OFFSET 1 LIMIT 1) " +
		" AND mr_no=? AND doctor=? " +
		" AND (op_type ='M' OR op_type = 'R') AND visit_type = 'o' " +
		" ORDER BY (reg_date + reg_time) DESC" ;

	private static final String GET_PATIENT_DEPARTMENT_PREVIOUS_MAIN_VISIT =
		"  SELECT patient_id, op_type, main_visit_id, (reg_date + reg_time) AS visited_date, doctor AS doctor_name, dept_name  " +
		" FROM patient_registration" +
		" WHERE  (reg_date + reg_time) >= (SELECT (reg_date + reg_time) FROM patient_registration" +
		"		 WHERE mr_no=? AND doctor is not null AND trim(doctor) != '' " +
		"		AND dept_name=(SELECT dept_id FROM doctors WHERE doctor_id =? ) " +
		"		AND (op_type ='M' OR op_type = 'R') AND visit_type = 'o' ORDER BY (reg_date + reg_time) DESC OFFSET 1 LIMIT 1) " +
		" AND mr_no=? AND doctor is not null AND trim(doctor) != '' " +
		" AND dept_name =(SELECT dept_id FROM doctors WHERE doctor_id = ?) " +
		" AND (op_type ='M' OR op_type = 'R') AND visit_type = 'o' " +
		" ORDER BY (reg_date + reg_time) DESC" ;

	public static List<BasicDynaBean> getPatientPreviousMainVisits(String mrNo, String doctor) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		RegistrationPreferencesDTO regPrefs = RegistrationPreferencesDAO.getRegistrationPreferences();
		String visit_type_dependence = regPrefs.getVisit_type_dependence(); // D - Doctor, S - Department/Speciality
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			if (visit_type_dependence.equals("D")) {
				ps = con.prepareStatement(GET_PATIENT_DOCTOR_PREVIOUS_MAIN_VISIT);
			}else {
				ps = con.prepareStatement(GET_PATIENT_DEPARTMENT_PREVIOUS_MAIN_VISIT);
			}
			ps.setString(1, mrNo);
			ps.setString(2, doctor);
			ps.setString(3, mrNo);
			ps.setString(4, doctor);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_PATIENT_DOCTOR_PREVIOUS_MAIN_VISIT_DETAILS =
		" SELECT patient_id, op_type, main_visit_id, (reg_date + reg_time) AS visited_date, doctor AS doctor_name" +
		" FROM patient_registration " +
		" WHERE mr_no=? AND doctor=? " +
		" AND (op_type ='M' OR op_type = 'R') AND visit_type = 'o' " +
		" ORDER BY (reg_date + reg_time) DESC LIMIT 1 " ;

	private static final String GET_PATIENT_DEPARTMENT_PREVIOUS_MAIN_VISIT_DETAILS =
		"  SELECT patient_id, op_type, main_visit_id, (reg_date + reg_time) AS visited_date, doctor AS doctor_name, dept_name  " +
		" FROM patient_registration" +
		" WHERE mr_no=? AND doctor is not null AND trim(doctor) != '' " +
		" AND dept_name =(SELECT dept_id FROM doctors WHERE doctor_id = ?) " +
		" AND (op_type ='M' OR op_type = 'R') AND visit_type = 'o' " +
		" ORDER BY (reg_date + reg_time) DESC" ;

	public static List<BasicDynaBean> getPatientPreviousMainVisits(String mrNo, String doctor, String opType) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		RegistrationPreferencesDTO regPrefs = RegistrationPreferencesDAO.getRegistrationPreferences();
		String visit_type_dependence = regPrefs.getVisit_type_dependence(); // D - Doctor, S - Department/Speciality
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			if (visit_type_dependence.equals("D")) {
				ps = con.prepareStatement(GET_PATIENT_DOCTOR_PREVIOUS_MAIN_VISIT_DETAILS);
			}else {
				ps = con.prepareStatement(GET_PATIENT_DEPARTMENT_PREVIOUS_MAIN_VISIT_DETAILS);
			}
			ps.setString(1, mrNo);
			ps.setString(2, doctor);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_EPISODE_PREVIOUS_VISITS =
		 " SELECT patient_id,op_type,main_visit_id FROM patient_registration " +
		 " WHERE main_visit_id = ? ORDER BY (reg_date + reg_time) DESC " ;

	public static List<BasicDynaBean> getPreviousEpisodeVisitDetails(String mainVisitId) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_EPISODE_PREVIOUS_VISITS);
			ps.setString(1, mainVisitId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static final String GET_DOCS = "select * from docs_upload";


	public static List<BasicDynaBean> getDocsUpload() throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_DOCS);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String UPDATE_COMPLAINT = "UPDATE patient_registration set complaint=? ";

	public boolean updateComplaint(Connection con, String complaint, String patientId) throws SQLException, IOException {
		return updateComplaint(con, complaint, patientId, null);
	}

	public boolean updateComplaint(Connection con, String complaint, String patientId , String userName) throws SQLException,
			IOException {
		PreparedStatement ps = null;
		int count = 0;
		boolean flag = true;
		try {
			if(null != userName) {
				String query = UPDATE_COMPLAINT + " ,user_name=? ,mod_time=? ";
				ps = con.prepareStatement(query + " WHERE patient_id=? ");
				ps.setString(1, complaint);
				ps.setString(2, userName);
				ps.setTimestamp(3, DateUtil.getCurrentTimestamp());
				ps.setString(4, patientId);
			} else {
				ps = con.prepareStatement(UPDATE_COMPLAINT + " WHERE patient_id=? ");
				ps.setString(1, complaint);
				ps.setString(2, patientId);
			}
			count = ps.executeUpdate();
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}

		return count == 1 && flag;

	}

	private final static String CHECK_FOR_DUPLICATE_MEMEBER_ID = "select member_id from patient_policy_details where  member_id=? " +
			" and mr_no not in (?, (SELECT mr_no FROM patient_details WHERE COALESCE(original_mr_no,'') = ?)) AND plan_id = ? ";

	public static boolean checkFOrDUplicateMemberId(String mrNO,String memberid, Integer planId) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		boolean status = false;
		try {
			ps = con.prepareStatement(CHECK_FOR_DUPLICATE_MEMEBER_ID);
			ps.setString(1, memberid);
			ps.setString(2, mrNO);
			ps.setString(3, mrNO);
			ps.setInt(4, planId.intValue());
			List l = DataBaseUtil.queryToDynaList(ps);
			if (l != null && !l.isEmpty())
				status = true;
			else
				status = false;
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return status;
	}


	private final static String CHECK_FOR_DUPLICATE_CORP_MEMBER_ID = "select employee_id AS member_id from patient_corporate_details where  employee_id=? " +
		" AND sponsor_id = ? ";

	public static boolean checkFOrDUplicateCorpMemberId(String mrNo,String memberid, String sponsorId) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		boolean status = false;
		try {
			int duplicates = DataBaseUtil.getIntValueFromDb("SELECT count(*) FROM patient_details"
			    + " WHERE COALESCE(original_mr_no,'') = ?", new Object[]{ mrNo });

			if (mrNo.isEmpty())
				ps = con.prepareStatement(CHECK_FOR_DUPLICATE_CORP_MEMBER_ID);
			else {
				if (duplicates > 1)
					ps = con.prepareStatement(CHECK_FOR_DUPLICATE_CORP_MEMBER_ID
						+ " and mr_no not in (?)"
						+ " and mr_no not in (SELECT mr_no FROM patient_details WHERE COALESCE(original_mr_no,'') = ?) ");
				else
					ps = con.prepareStatement(CHECK_FOR_DUPLICATE_CORP_MEMBER_ID
						+ " and mr_no not in (?, COALESCE((SELECT mr_no FROM patient_details WHERE COALESCE(original_mr_no,'') = ?),'')) ");
			}
			ps.setString(1, memberid);
			ps.setString(2, sponsorId);
			if (!mrNo.isEmpty()) {
				ps.setString(3, mrNo);
				ps.setString(4, mrNo);
			}
			List list = DataBaseUtil.queryToDynaList(ps);
			if (list != null && !list.isEmpty())
				status = true;
			else
				status = false;
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return status;
	}


	private final static String CHECK_FOR_DUPLICATE_NATIONAL_MEMEBER_ID = "select national_id AS member_id from patient_national_sponsor_details where  national_id=? " +
		" AND sponsor_id = ? ";

	public static boolean checkFOrDUplicateNationalMemberId(String mrNo,String nationalId, String sponsorId) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		boolean status = false;
		try {
			int duplicates = DataBaseUtil.getIntValueFromDb("SELECT count(*) FROM patient_details"
			    + " WHERE COALESCE(original_mr_no,'') = ?", new Object[]{ mrNo });

			if (mrNo.isEmpty())
				ps = con.prepareStatement(CHECK_FOR_DUPLICATE_NATIONAL_MEMEBER_ID);
			else {
				if (duplicates > 1)
					ps = con.prepareStatement(CHECK_FOR_DUPLICATE_NATIONAL_MEMEBER_ID
						+ " and mr_no not in (?)"
						+ " and mr_no not in (SELECT mr_no FROM patient_details WHERE COALESCE(original_mr_no,'') = ?) ");
				else
					ps = con.prepareStatement(CHECK_FOR_DUPLICATE_NATIONAL_MEMEBER_ID
						+ " and mr_no not in (?, COALESCE((SELECT mr_no FROM patient_details WHERE COALESCE(original_mr_no,'') = ?),'')) ");
			}
			ps.setString(1, nationalId);
			ps.setString(2, sponsorId);
			if (!mrNo.isEmpty()) {
				ps.setString(3, mrNo);
				ps.setString(4, mrNo);
			}
			List l = DataBaseUtil.queryToDynaList(ps);
			if (l != null && !l.isEmpty())
				status = true;
			else
				status = false;
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return status;
	}

	private static final String GET_ALL_EPISODE_VISITS_FOR_VISIT_ID = " SELECT patient_id FROM patient_registration " +
			" WHERE main_visit_id = (SELECT main_visit_id FROM patient_registration WHERE patient_id=? LIMIT 1) ";

	public static List getAllPrevEpisodeVisits(Connection con, String visitId, String currentVisitId) throws SQLException {
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(GET_ALL_EPISODE_VISITS_FOR_VISIT_ID + " AND patient_id!= ? ");
			ps.setString(1,visitId);
			ps.setString(2,currentVisitId);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(null, ps);
		}
	}


	public static String GET_IS_FIRST_OF_CATEGORY = " SELECT charge_id FROM bill_charge bc  "+
	" JOIN bill b ON b.bill_no= bc.bill_no  "+
	" JOIN patient_registration pr ON pr.patient_id = b.visit_id  "+
	" WHERE patient_id=? "+
	" AND bc.insurance_category_id =? AND  bc.status!='X' AND b.status!='X' AND bc.first_of_category= true ";

	public static Boolean getIsFirstOfCategory(Connection con, String visitId, int insuranceCategoryId) throws SQLException  {
		if(con == null) {
			return getIsFirstOfCategory(visitId, insuranceCategoryId);
		} else {
				PreparedStatement ps = con.prepareStatement(GET_IS_FIRST_OF_CATEGORY);
				ps.setString(1, visitId);
				ps.setInt(2, insuranceCategoryId);
				String charge =  DataBaseUtil.getStringValueFromDb(ps);
				ps.close();
				return charge == null;
		}
	}

	public static Boolean getIsFirstOfCategory(String visitId, int insuranceCategoryId) throws SQLException  {
		Connection con = DataBaseUtil.getConnection();
		Boolean isFirst = false;
		Boolean success = false;
		con.setAutoCommit(success);
		try {
			isFirst = getIsFirstOfCategory(con, visitId, insuranceCategoryId);
			success = true;
		}finally{
			DataBaseUtil.commitClose(con, success);
		}
		return isFirst;
	}

	private static final String UPDATE_DISCHARGE_DATE =
		" UPDATE patient_registration SET discharge_date=?, discharge_time=?, user_name=?" +
		" WHERE patient_id=? ";

	public static void updateDischargeDate(Connection con, String patientId, String disDate, String disTime,String username)
		throws SQLException, java.text.ParseException {
		PreparedStatement ps = con.prepareStatement(UPDATE_DISCHARGE_DATE);
		int i=1;
		ps.setDate(i++, DataBaseUtil.parseDate(disDate));
		ps.setTime(i++, DataBaseUtil.parseTime(disTime));
		ps.setString(i++, username );
		ps.setString(i++, patientId);
		ps.executeUpdate();
		ps.close();
	}

	private final static String FIND_VISIT = "SELECT * FROM patient_registration WHERE patient_id = ?";

	public BasicDynaBean findVisitById(Connection con, String visitId) throws SQLException {
		PreparedStatement ps = con.prepareStatement(FIND_VISIT);
		ps.setString(1, visitId);
		return DataBaseUtil.queryToDynaBean(ps);
	}

	private final static String COMPLAINT_DETAILS =
		" SELECT pr.complaint FROM patient_registration pr " +
		" WHERE pr.patient_id=?";

	public static BasicDynaBean getComplaint(String patientId) throws SQLException{
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(COMPLAINT_DETAILS);
			ps.setString(1, patientId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private String GET_ENCOUNTER = " SELECT encounter_type_id, encounter_type_desc, op_applicable, ip_applicable," +
						   " daycare_applicable FROM encounter_type_codes WHERE @ LIMIT 1 ";

	public BasicDynaBean getVisitDefaultEncounter(String visit_type, boolean is_daycare) throws SQLException {

		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = DataBaseUtil.getConnection();

			if (visit_type.equals("i")) {
				if (is_daycare)
					GET_ENCOUNTER = GET_ENCOUNTER.replace("@", " daycare_encounter_default = 'Y' ");
				else
					GET_ENCOUNTER = GET_ENCOUNTER.replace("@", " ip_encounter_default = 'Y' ");

			}else if (visit_type.equals("o")) {
				GET_ENCOUNTER = GET_ENCOUNTER.replace("@", " op_encounter_default = 'Y' ");
			}

			 ps = con.prepareStatement(GET_ENCOUNTER);
			 return DataBaseUtil.queryToDynaBean(ps);
		}
		finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static String GET_EPISODE_ALL_FOLLOWUP_VISITS_ONLY = "SELECT * " +
			" FROM patient_registration WHERE main_visit_id = " +
			" (SELECT main_visit_id FROM patient_registration WHERE patient_id = ?)" +
			" AND op_type IN ('F','D') ";

	public List<BasicDynaBean> getEpisodeAllFollowUpVisitsOnly(String visitId) throws SQLException {
			Connection con = null;
			PreparedStatement ps =null;
		try {
				con = DataBaseUtil.getReadOnlyConnection();
				ps = con.prepareStatement(GET_EPISODE_ALL_FOLLOWUP_VISITS_ONLY);
				ps.setString(1, visitId);
				return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private final static String PATIENT_PLAN_DETAILS =
		" SELECT * from patient_policy_details where patient_policy_id = ? " ;

	public static BasicDynaBean patientPlanDetails(int patientPolicyId) throws SQLException{
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(PATIENT_PLAN_DETAILS);
			ps.setInt(1, patientPolicyId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static String GET_PREV_VISITS_OF_SAME_DEPT = " SELECT mr_no,dept_name,patient_id, established_type, " +
			" (reg_date+reg_time)::timestamp as reg_date FROM patient_registration 	WHERE mr_no = ?  AND patient_id != ? ";

	public static boolean checkMarkAsEstablished (Connection con, BasicDynaBean visitBean) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_PREV_VISITS_OF_SAME_DEPT+ " AND center_id = ? AND dept_name = ?");
			ps.setString(1, (String)visitBean.get("mr_no"));
			ps.setString(2, (String) visitBean.get("patient_id"));
			ps.setInt(3, (Integer) visitBean.get("center_id"));
			ps.setString(4, (String) visitBean.get("dept_name"));
			List sameDeptsList = DataBaseUtil.queryToArrayList(ps);
			if(sameDeptsList == null || sameDeptsList.isEmpty() || sameDeptsList.size()== 0)
				return false;
			else
				return true;
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	public static List getAllEstablishedStatsOfPrevVisits(String mrno, String patientId) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_PREV_VISITS_OF_SAME_DEPT);
			ps.setString(1, mrno);
			ps.setString(2, patientId);
			return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(ps));
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static String GET_PREV_VISIT_BILL_WISE_DUES = " SELECT bill_no, "+
			" (total_amount + insurance_deduction + total_tax - total_receipts - total_claim - deposit_set_off - total_claim_tax ) as due_amount "+
			" FROM bill b "+
			" JOIN patient_registration pr ON (pr.patient_id = b.visit_id) "+
			" WHERE  payment_status = 'U' and b.status !='X' AND mr_no = ? ";

	public static List getAllPrevVisitBillWiseDues(String mrno) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_PREV_VISIT_BILL_WISE_DUES);
			ps.setString(1, mrno);
			return DataBaseUtil.queryToArrayList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static String GET_PATIENT_FAMILY_BILLS_TOTAL = " SELECT sum(total_amount) as total_amount "+
		" FROM bill b "+
		" JOIN patient_registration pr ON (pr.patient_id = b.visit_id) "+
		" JOIN patient_details pd ON (pd.mr_no = pr.mr_no) "+
		" WHERE b.status !='X' AND pd.family_id = ? AND extract('year' from pr.reg_date) = extract('year' from current_date) ";

	public static BasicDynaBean getPatientFamilyBillsTotal(String family_id) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_PATIENT_FAMILY_BILLS_TOTAL);
			ps.setString(1, family_id);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String CHECK_POLICY_STATUS = " SELECT status FROM patient_policy_details WHERE patient_policy_id = ? ";
    public static boolean isPolicyActive(int patient_policy_id) throws SQLException{
        Connection con = DataBaseUtil.getConnection();
        boolean isActive = false;
        try {
        	isActive = isPolicyActive(con, patient_policy_id);
        } finally {
            DataBaseUtil.closeConnections(con, null);
        }
        return isActive;
    }

    public static boolean isPolicyActive(Connection con, int patient_policy_id) throws SQLException{
	    PreparedStatement ps = null;
	    boolean isActive = false;
	    try {
            ps = con.prepareStatement(CHECK_POLICY_STATUS);
            ps.setInt(1, patient_policy_id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                if (rs.getString("status").equals("A"))
                    isActive = true;
            }

        } finally {
            DataBaseUtil.closeConnections(null, ps);
        }
        return isActive;
	}

    private static final String VISIT_USE_DRG = " SELECT use_drg FROM patient_registration WHERE patient_id = ? ";
    public static boolean visitUsesDRG(Connection con, String patientId) throws SQLException {
	    PreparedStatement ps = null;
	    boolean useDRG = false;
	    try {
            ps = con.prepareStatement(VISIT_USE_DRG);
            ps.setString(1, patientId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                if (rs.getString("use_drg").equals("Y"))
                	useDRG = true;
            }

        } finally {
            DataBaseUtil.closeConnections(null, ps);
        }
        return useDRG;
    }

    public static boolean visitUsesDRG(String patientId) throws SQLException {
    	Connection con = null;
    	try {
    		con = DataBaseUtil.getReadOnlyConnection();
    		return visitUsesDRG(con, patientId);
    	}finally {
    		DataBaseUtil.closeConnections(con, null);
    	}
    }


    private static final String GET_CENTER_ID =
    	" SELECT pr.center_id FROM patient_registration pr where pr.patient_id=? " +
    	" UNION ALL " +
    	" SELECT isr.center_id FROM incoming_sample_registration isr where isr.incoming_visit_id=? " +
    	" UNION ALL " +
    	" SELECT src.center_id FROM store_retail_customers src where src.customer_id=? ";

    public static Integer getCenterId(Connection con, String patientId) throws SQLException {
    	PreparedStatement ps = null;
    	ResultSet rs = null;
    	try {
    		ps = con.prepareStatement(GET_CENTER_ID);
    		ps.setString(1, patientId);
    		ps.setString(2, patientId);
    		ps.setString(3, patientId);
    		rs = ps.executeQuery();
    		if (rs.next()) {
    			return rs.getInt("center_id");
    		}
    	} finally {
    		DataBaseUtil.closeConnections(null, ps, rs);
    	}
    	return null;
    }

    public static Integer getCenterId(String patientId) throws SQLException {
    	Connection con = DataBaseUtil.getConnection();
    	try {
    		return getCenterId(con, patientId);
    	} finally {
    		DataBaseUtil.closeConnections(con, null);
    	}
    }

    private static String PATIENT_MLC_STATUS = "SELECT patient_id FROM " +
		" patient_registration WHERE COALESCE(mlc_status,'N') = 'Y' AND mr_no = ? ORDER BY reg_date DESC LIMIT 1 ";

	public String getMlcStatusVisitId(Connection con, String mr_no) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(PATIENT_MLC_STATUS);
			ps.setString(1, mr_no);
			return DataBaseUtil.getStringValueFromDb(ps);
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}


	private static String UPDATE_SPONSOR_DETAILS = " UPDATE patient_registration " +
	" SET primary_sponsor_id = ?, secondary_sponsor_id =?, primary_insurance_co=?, secondary_insurance_co=?, primary_insurance_approval=?, secondary_insurance_approval=?, " +
	" patient_policy_id=?, org_id=? WHERE patient_id =? ";

	public static String updateSponsorDetails(Connection con, BasicDynaBean visitDetailsBean) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(UPDATE_SPONSOR_DETAILS);
			ps.setString(1, (String)visitDetailsBean.get("primary_sponsor_id"));
			ps.setString(2, (String)visitDetailsBean.get("secondary_sponsor_id"));
			ps.setString(3, (String)visitDetailsBean.get("primary_insurance_co"));
			ps.setString(4, (String)visitDetailsBean.get("secondary_insurance_co"));
			ps.setBigDecimal(5, (BigDecimal)visitDetailsBean.get("primary_insurance_approval"));
			ps.setBigDecimal(6, (BigDecimal)visitDetailsBean.get("secondary_insurance_approval"));
			ps.setInt(7, (Integer)visitDetailsBean.get("patient_policy_id"));
			ps.setString(8, (String)visitDetailsBean.get("org_id"));
			ps.setString(9, (String)visitDetailsBean.get("patient_id"));

			String success = ps.executeUpdate()> 1? "": "error" ;
			return success;
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	private static final String GET_DETAILS_FOR_MRNO_OR_VISITID = "SELECT visit_id, mr_no FROM patient_details pd "
			+ " WHERE (visit_id = ? OR mr_no = ?) AND ( patient_confidentiality_check(pd.patient_group,pd.mr_no) )";

	public static BasicDynaBean getDetailsForMrnoOrVisitId(String patientId)throws SQLException {
		PreparedStatement ps = null;
		Connection con = null;

	    try {
	    	con = DataBaseUtil.getReadOnlyConnection();
	    	ps = con.prepareStatement(GET_DETAILS_FOR_MRNO_OR_VISITID);
	    	ps.setString(1, patientId);
	    	ps.setString(2, patientId);
	    	return DataBaseUtil.queryToDynaBean(ps);

	    } finally {
	        DataBaseUtil.closeConnections(con, ps);
	    }
	}

	private static final String VISIT_USE_PERDIEM = " SELECT use_perdiem FROM patient_registration WHERE patient_id = ? ";
    public static boolean visitUsesPerdiem(Connection con, String patientId) throws SQLException {
	    PreparedStatement ps = null;
	    boolean usePerdiem = false;
	    try {
            ps = con.prepareStatement(VISIT_USE_PERDIEM);
            ps.setString(1, patientId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                if (rs.getString("use_perdiem").equals("Y"))
                	usePerdiem = true;
            }

        } finally {
            DataBaseUtil.closeConnections(null, ps);
        }
        return usePerdiem;
    }

    public static boolean visitUsesPerdiem(String patientId) throws SQLException {
    	Connection con = null;
    	try {
    		con = DataBaseUtil.getReadOnlyConnection();
    		return visitUsesPerdiem(con, patientId);
    	}finally {
    		DataBaseUtil.closeConnections(con, null);
    	}
    }

	private static final String UPDATE_VISIT_PERDIEM_CODE =
			"UPDATE patient_registration SET per_diem_code=?, user_name = ? WHERE patient_id=?";

	public static boolean updateVisitPerdiemCode(Connection con, String perDiemCode,
			String patientId, String userid) throws SQLException {
		boolean update = false;
		PreparedStatement ps = con.prepareStatement(UPDATE_VISIT_PERDIEM_CODE);
		int i = 1;
		ps.setString(i++, perDiemCode);
		ps.setString(i++, userid);
		ps.setString(i++, patientId);
		update = (ps.executeUpdate() > 0);
		ps.close();
		return update;
	}

	private static final String GET_PATIENT_ICD_CODES = " SELECT textcat_commacat(icd_code_value) as icd_codes " +
			"	FROM (select icd_code||'('|| (case when diag_type = 'P' then 'Primary' else 'Secondary' end)||')' as icd_code_value"+
			"   FROM mrd_diagnosis where visit_id = ?) as foo";

	public static String getPatientIcdCodes(String patientId) throws SQLException{
		Connection con = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_PATIENT_ICD_CODES);
			ps.setString(1, patientId);
			return DataBaseUtil.getStringValueFromDb(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_PATIENT_CPT_CODES = " SELECT textcat_commacat(cpt_code_value) as cpt_codes " +
			"	FROM (select act_rate_plan_item_code as cpt_code_value"+
			"   FROM bill_charge bc" +
			"	JOIN bill b ON(bc.bill_no = b.bill_no)" +
			"  	WHERE b.visit_id = ? AND bc.code_type = 'CPT') as foo";
	
	public static String getPatientCptCodes(String patientId) throws SQLException{
		Connection con = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_PATIENT_CPT_CODES);
			ps.setString(1, patientId);
			return DataBaseUtil.getStringValueFromDb(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	private static final String GET_OP_PATIENT_PRESCRIBED_CPT_CODES =
			"SELECT textcat_commacat(item_code || '-' || description) AS "+
			"	cpt_item_code_with_description"+
			"	FROM (SELECT item_code,d.test_name as description "+
			"		FROM patient_prescription pp "+
			"		JOIN doctor_consultation dc on (pp.consultation_id = dc.consultation_id) "+
			"		JOIN patient_test_prescriptions ptp ON ( pp.patient_presc_id = ptp.op_test_pres_id) "+
			"		JOIN test_org_details tod ON(tod.test_id = ptp.test_id AND tod.org_id = ? ) "+
			"		JOIN diagnostics d ON d.test_id=ptp.test_id "+
			"		WHERE patient_id = ?" +

			"		UNION ALL "+
			" 		SELECT item_code,s.service_name as description " +
			"		FROM patient_prescription pp "+
			"		JOIN doctor_consultation dc on (pp.consultation_id = dc.consultation_id) "+
			"		JOIN patient_service_prescriptions psp ON ( pp.patient_presc_id = psp.op_service_pres_id)"+
			"		JOIN service_org_details sod ON(sod.service_id = psp.service_id AND sod.org_id = ? ) "+
			"		JOIN services s ON s.service_id=psp.service_id"+
			"		WHERE patient_id = ? "+

			"		UNION ALL "+
			"		SELECT item_code,om.operation_name as description "+
			"		FROM patient_prescription pp "+
			"		JOIN doctor_consultation dc on (pp.consultation_id = dc.consultation_id) "+
			"		JOIN patient_operation_prescriptions pop ON ( pp.patient_presc_id = pop.prescription_id) "+
			"		JOIN operation_org_details ood ON(ood.operation_id = pop.operation_id AND ood.org_id = ? )"+
			"		JOIN operation_master om ON om.op_id=pop.operation_id"+
			"		WHERE patient_id = ? ) AS foo WHERE item_code!=''";

	public static String getOpPatientPrescribedCptCodes(String patientId) throws SQLException{
		Connection con = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps = null;
		BasicDynaBean patientDetailBean = getPatientVisitDetailsBean(con, patientId);
		String orgId = (String)patientDetailBean.get("org_id");
		try {
			ps = con.prepareStatement(GET_OP_PATIENT_PRESCRIBED_CPT_CODES);
			ps.setString(1, orgId);
			ps.setString(2, patientId);
			ps.setString(3, orgId);
			ps.setString(4, patientId);
			ps.setString(5, orgId);
			ps.setString(6, patientId);
			
			return DataBaseUtil.getStringValueFromDb(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	private static final String GET_PATIENT_MEMBER_ID = "SELECT # FROM ";
	private static final String INSURANCE_TABLE = " patient_policy_details WHERE mr_no = ? ORDER BY patient_policy_id DESC ";
	private static final String CORPORATE_TABLE = " patient_corporate_details WHERE mr_no = ? ORDER BY patient_corporate_id DESC ";
	private static final String NATANIOAL_TABLE = " patient_national_sponsor_details WHERE mr_no = ? ORDER BY patient_national_sponsor_id DESC ";

	public static String getPatientMemberId(String mrNo,String sponsorType) throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			String query = GET_PATIENT_MEMBER_ID;
			if(sponsorType.equals("I")) {
				query = query.replace("#", "member_id");
				query += INSURANCE_TABLE;
			} else if (sponsorType.equals("C")) {
				query = query.replace("#", "employee_id AS member_id");
				query += CORPORATE_TABLE;
			} else if (sponsorType.equals("N")) {
				query = query.replace("#", "national_id AS member_id");
				query += NATANIOAL_TABLE;
			}

			ps = con.prepareStatement(query);
			ps.setString(1, mrNo);

			return DataBaseUtil.getStringValueFromDb(ps);

		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String VISIT_CENTER_FIELDS = "SELECT pr.mr_no, hcm.center_id, hcm.center_name "
			+ " FROM patient_registration pr "
			+ " JOIN hospital_center_master hcm ON(hcm.center_id = pr.center_id)"
			+ " WHERE pr.patient_id = ? "
			+ " UNION ALL "
			+ " SELECT isr.mr_no, hcm.center_id, hcm.center_name "
			+ " FROM incoming_sample_registration isr "
			+ " JOIN hospital_center_master hcm ON(hcm.center_id = isr.center_id) "
			+ " WHERE isr.incoming_visit_id = ? ";

	public static BasicDynaBean gettVisitCenterRelatedFields(Connection con, String visitID)throws SQLException {
		PreparedStatement pstmt = null;
		try {
			pstmt = con.prepareStatement(VISIT_CENTER_FIELDS);
			pstmt.setString(1, visitID);
			pstmt.setString(2, visitID);
			return DataBaseUtil.queryToDynaBean(pstmt);
		} finally {
			DataBaseUtil.closeConnections(null, pstmt);
		}


	}

	private  String PATIENT_VISITS_QUERY = "select v.mr_no, v.center_id, v.patient_id, v.doctor_name, v.dept_name, "
			+ "to_char(v.visit_reg_date AT TIME ZONE (SELECT  current_setting('TIMEZONE')) AT TIME ZONE 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') as reg_date, v.status, v.visit_type, "
			+ "to_char((v.discharge_date + v.discharge_time) AT TIME ZONE (SELECT  current_setting('TIMEZONE')) AT TIME ZONE 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') as discharge_date_time from all_visits_view v "
			+ "WHERE v.mr_no = ? AND v.visit_reg_date >= ? AND v.visit_reg_date <= ? ";

	public List getPatientVisits(Connection con, String mr_no, int centerId, java.sql.Timestamp fromDate, java.sql.Timestamp toDate)
			throws Exception {
		        if(con == null)
				    con = DataBaseUtil.getReadOnlyConnection();
				PreparedStatement ps = null;
				StringBuilder queryStr = new StringBuilder(PATIENT_VISITS_QUERY);
				try {
					if(centerId != -1) {
						queryStr.append(" AND v.center_id = ? ");
					}
				    ps = con.prepareStatement(queryStr.toString());
				    ps.setString(1, mr_no);
				    ps.setTimestamp(2, fromDate);
				    ps.setTimestamp(3, toDate);
				    if(centerId != -1)
				    	ps.setInt(4, centerId);

				    return DataBaseUtil.queryToArrayList(ps);
				} finally {
					DataBaseUtil.closeConnections(null, ps);
				}
			}

	private String PATIENT_VISITS_DETAILS_QUERY = "SELECT pd.mr_no, pd.email_id, pd.patient_phone, pr.center_id, hcm.center_name, pr.patient_id as visit_id, "
			+ " pr.org_id as rate_plan_id, to_char((pr.reg_date + pr.reg_time) AT TIME ZONE (SELECT  current_setting('TIMEZONE')) AT TIME ZONE 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') as reg_date_time, "
			+ " od.org_name, ppip.sponsor_id as primary_sponsor_id, ptm.tpa_name as primary_sponsor_name, "
			+ " ptm.mobile_no as primary_sponsor_mobile_no, ptm.email_id as primary_sponsor_email_id, "
			+ " ppip.plan_id as primary_plan_id, get_patient_name(pd.salutation, pd.patient_name, pd.middle_name, pd.last_name) as patient_name, "
			+ " pipm.plan_name as primary_plan_name, spip.sponsor_id as secondary_sponsor_id, stm.tpa_name as secondary_sponsor_name, "
			+ " stm.mobile_no as secondary_sponsor_mobile_no, stm.email_id as secondary_sponsor_email_id, spip.plan_id as secondary_plan_id, "
			+ " sipm.plan_name as secondary_plan_name , dept.dept_name, doc.doctor_name, dept.dept_id, doc.doctor_id, pr.visit_type, "
			+ " COALESCE(rdoc.doctor_name, ref.referal_name) as referred_by, pr.reference_docto_id as referrer_id, "
			+ " to_char((pr.discharge_date + pr.discharge_time) AT TIME ZONE (SELECT  current_setting('TIMEZONE')) AT TIME ZONE 'UTC', "
			+ " 'YYYY-MM-DD\\\"T\\\"HH24:MI:SS\\\"Z\\\"') as discharge_date_time, pd.patient_gender, pd.dateofbirth as date_of_birth, "
			+ " pd.expected_dob as date_of_birth_estimated, "
			+ " pppd.member_id as pri_member_id, pppd.policy_number as pri_policy_number, "
			+ " pppd.policy_holder_name as pri_policy_holder_name, pppd.patient_relationship as pri_patient_relationship, "
			+ " pppd.policy_validity_start as pri_policy_validity_start, pppd.policy_validity_end as pri_policy_validity_end, "
			+ " sppd.member_id as sec_member_id, sppd.policy_number as sec_policy_number, "
			+ " sppd.policy_holder_name as sec_policy_holder_name, sppd.patient_relationship as sec_patient_relationship, "
			+ " sppd.policy_validity_start as sec_policy_validity_start, sppd.policy_validity_end as sec_policy_validity_end, "
			+ " pr.visit_custom_field1 as visit_custom_field_1, pr.visit_custom_field2 as visit_custom_field_2 "
			+ " FROM patient_registration pr " 
			+ " JOIN patient_details pd using(mr_no) "
			+ " JOIN hospital_center_master hcm using(center_id) "
			+ " LEFT JOIN doctors doc ON(pr.doctor=doc.doctor_id) "
			+ " LEFT JOIN doctors rdoc ON(pr.reference_docto_id::text = rdoc.doctor_id::text) "
			+ " LEFT JOIN referral ref ON(pr.reference_docto_id::text = ref.referal_no::text) "
			+ " LEFT JOIN department dept ON (pr.dept_name=dept.dept_id) "
			+ " LEFT JOIN organization_details od on(pr.org_id = od.org_id) "
			+ " LEFT join patient_insurance_plans ppip on(ppip.patient_id=pr.patient_id AND ppip.priority = 1) "
			+ " LEFT JOIN patient_insurance_plans spip on(spip.patient_id=pr.patient_id AND spip.priority=2) "
			+ " LEFT JOIN insurance_plan_main pipm on(pipm.plan_id=ppip.plan_id) "
			+ " LEFT JOIN insurance_plan_main sipm on(sipm.plan_id=spip.plan_id) "
			+ " LEFT JOIN tpa_master ptm on(ptm.tpa_id=ppip.sponsor_id) "
			+ " LEFT JOIN tpa_master stm on(stm.tpa_id=spip.sponsor_id)  "
			+ " LEFT JOIN patient_policy_details pppd on (pppd.patient_policy_id=ppip.patient_policy_id) "
			+ " LEFT JOIN patient_policy_details sppd on (sppd.patient_policy_id=spip.patient_policy_id) WHERE ";

	public List getPatientVisitsDetails(Object fromTime, Object toTime, String mrno, String primSpId, String secSpId, Boolean useDischargeDateForIp)
			throws Exception {
				Connection con = DataBaseUtil.getReadOnlyConnection();
				PreparedStatement ps = null;
				StringBuilder queryStr = new StringBuilder(PATIENT_VISITS_DETAILS_QUERY);
				try {
					if (useDischargeDateForIp) {
						queryStr.append(" ((pr.visit_type = 'o' AND (pr.reg_date + pr.reg_time) BETWEEN ? AND ?) ");
						queryStr.append("  OR (pr.visit_type = 'i' AND (pr.discharge_date + pr.discharge_time) BETWEEN ? AND ?)) ");
					} else {
						queryStr.append(" ((pr.reg_date + pr.reg_time) BETWEEN ? AND ?) ");
					}
					if(mrno != null && !mrno.equals("")) {
						queryStr.append(" AND pd.mr_no = ? ");
					}
					if(primSpId != null && !primSpId.equals("")) {
						queryStr.append(" AND ppip.sponsor_id = ? ");
					}
					if(secSpId != null && !secSpId.equals("")) {
						queryStr.append(" AND spip.sponsor_id = ? ");
					}
				    ps = con.prepareStatement(queryStr.toString());
				    int index = 1;
				    ps.setObject(index++, fromTime);
				    ps.setObject(index++, toTime);
					if (useDischargeDateForIp) {
					    ps.setObject(index++, fromTime);
					    ps.setObject(index++, toTime);
					}
				    if(mrno != null && !mrno.equals("")) {
				        ps.setString(index++, mrno);
				    }
				    if(primSpId != null && !primSpId.equals("")) {
				        ps.setString(index++, primSpId);
				    }
				    if(secSpId != null && !secSpId.equals("")) {
				        ps.setString(index++, secSpId);
				    }

				    return DataBaseUtil.queryToArrayList(ps);
				} finally {
					DataBaseUtil.closeConnections(null, ps);
				}
			}

	private static final String MAIN_VISIT_OF_CURRENT_MONTH = " SELECT * FROM patient_registration " +
			" WHERE mr_no=? AND op_type='M' AND reg_date >= ? AND reg_date <= ? " +
			" ORDER BY reg_date DESC limit 1 ";

	public BasicDynaBean getMainVisitOfCurrentMonth(Connection con,String mrNo) throws ParseException , SQLException {

		PreparedStatement ps = null;
		try {
			java.sql.Date[] dateRange = DateUtil.getDateRange("tm");

			ps = con.prepareStatement(MAIN_VISIT_OF_CURRENT_MONTH);
			ps.setString(1, mrNo);
			//ps.setInt(2, RequestContext.getCenterId());
			//ps.setInt(3, RequestContext.getCenterId());
			ps.setDate(2, dateRange[0]);
			ps.setDate(3, dateRange[1]);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	public BasicDynaBean getMainVisitOfCurrentMonth(String mrNo) throws ParseException , SQLException {
		Connection con = DataBaseUtil.getConnection();
		try {
			return getMainVisitOfCurrentMonth(con , mrNo);
		} finally {
			DataBaseUtil.closeConnections(con,null);
		}
	}
	
	public List<BasicDynaBean> getAllMainVisitsOfPatient(String mrNo,java.sql.Date startDate, java.sql.Date endDate) throws ParseException , SQLException {
		Connection con = DataBaseUtil.getConnection();
		try {
			return getAllMainVisitsOfPatient(con, mrNo, startDate, endDate);
		} finally {
			DataBaseUtil.closeConnections(con,null);
		}
	}

	private static final String ALL_MAIN_VISITS_OF_PATIENT = " SELECT * FROM patient_registration " +
			" WHERE mr_no=? AND reg_date>=? AND reg_date<=? AND op_type='M' AND visit_type='o' " +
			" ORDER BY reg_date,reg_time,patient_id ";
	
	public List<BasicDynaBean> getAllMainVisitsOfPatient(Connection con, String mrNo, java.sql.Date startDate, java.sql.Date endDate) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(ALL_MAIN_VISITS_OF_PATIENT);
			ps.setString(1, mrNo);
			ps.setDate(2, startDate);
			ps.setDate(3, endDate);
			
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}
	
	private static final String GET_PATIENT_DOCTOR_CURRENT_VISITS_PREVIOUS_MAIN_VISIT_DIAG = 
			" SELECT patient_id, op_type, main_visit_id, (reg_date + reg_time) AS visited_date, doctor AS doctor_name  "+
		    " FROM patient_registration pr  WHERE mr_no= ? AND doctor=? AND (reg_date + reg_time) < ?::timestamp "+
		    " AND (op_type ='M' OR op_type = 'R') ORDER BY (reg_date + reg_time) DESC LIMIT 1 ";

		public static BasicDynaBean getPatientCurrentVisitsPreviousMainVisits(String mrNo, String curentvisitsMainvisitRegdatandtime, String doctorName) throws SQLException {
			Connection con = null;
			PreparedStatement ps = null;
			try {
				con = DataBaseUtil.getReadOnlyConnection();
				ps = con.prepareStatement(GET_PATIENT_DOCTOR_CURRENT_VISITS_PREVIOUS_MAIN_VISIT_DIAG);
				ps.setString(1, mrNo);
				ps.setString(2, doctorName);
				ps.setString(3, curentvisitsMainvisitRegdatandtime);
				return DataBaseUtil.queryToDynaBean(ps);
			} finally {
				DataBaseUtil.closeConnections(con, ps);
			}
		}
		
		private final static String VISIT_DETAILS_WITH_MAIN_VISIT_INFO = "SELECT pr.*, tm.tpa_name, stm.tpa_name AS sec_tpa_name, " + 
				" mainpr.reg_date as mainvisit_reg_date,mainpr.reg_time as mainvisit_reg_time " + 
				" FROM patient_registration pr " + 
				" JOIN patient_registration mainpr ON(pr.main_visit_id = mainpr.patient_id )" +
				" LEFT OUTER JOIN tpa_master tm ON pr.primary_sponsor_id=tm.tpa_id " +
				" LEFT OUTER JOIN tpa_master stm ON pr.secondary_sponsor_id=stm.tpa_id " +
				" WHERE pr.patient_id=?";
		
		public static BasicDynaBean getVisitDetailsWithMainvisitdetails(String visitId) throws SQLException {
			Connection con = DataBaseUtil.getConnection();
			PreparedStatement ps = null;
			try {
				con.setAutoCommit(false);
				ps = con.prepareStatement(VISIT_DETAILS_WITH_MAIN_VISIT_INFO);
				ps.setString(1, visitId);
				List l = DataBaseUtil.queryToDynaList(ps);
				if (l != null && !l.isEmpty())
					return (BasicDynaBean) l.get(0);
			} finally {
				DataBaseUtil.closeConnections(null, ps);
				DataBaseUtil.commitClose(con, true);
			}
			return null;
		}
		
		private static final String GET_PATIENT_VISIT_SPONSOR_INFO = " SELECT pr.visit_type, pd.mr_no,"
				+ " pr.org_id, pr.dept_name AS dept_id,"
				+ " pr.admitted_dept, pr.patient_id, pr.secondary_sponsor_id,"
				+ " pr.per_diem_code, pr.bed_type AS bill_bed_type,"
				+ " pr.primary_sponsor_id, pr.org_id, pr.plan_id, pr.center_id,"
				+ " pr.primary_sponsor_id, pr.secondary_sponsor_id, pr.use_drg,"
				+ " pr.reg_date, pr.reg_time, pr.op_type, pr.doctor,"
				+ " pr.reg_date, pr.reg_time, pr.use_perdiem, pr.patient_policy_id,"
				+ " pr.op_type, pr.insurance_id,"
				+ " tpa.sponsor_type AS sponsor_type, stpa.sponsor_type AS sec_sponsor_type,"
				+ " COALESCE(pd.dateofbirth, pd.expected_dob) AS expected_dob,"
				+ " pd.category_expiry_date, pd.patient_gender,"
				+ " bn.bed_type AS alloc_bed_type,"
				+ " sm.salutation || ' ' || patient_name"
				+ " || case when coalesce(middle_name, '') = '' then '' else (' ' || middle_name) end"
				+ " || case when coalesce(last_name, '') = '' then '' else (' ' || last_name) end"
				+ " as full_name, ''::text as age_text, dep.dept_name, dr.doctor_name"
				+ " FROM patient_registration pr"
				+ " JOIN patient_details pd ON pr.mr_no = pd.mr_no"
				+ " LEFT JOIN ward_names wnr ON wnr.ward_no = pr.ward_id"
				+ " LEFT JOIN salutation_master sm ON pd.salutation = sm.salutation_id"
				+ " LEFT JOIN department dep ON pr.dept_name = dep.dept_id"
				+ " LEFT JOIN doctors dr ON dr.doctor_id = pr.doctor"
				+ " LEFT JOIN admission ad ON ad.patient_id = pr.patient_id"
				+ " LEFT JOIN bed_names bn ON bn.bed_id = ad.bed_id"
				+ " LEFT JOIN tpa_master tpa ON tpa.tpa_id = pr.primary_sponsor_id"
				+ " LEFT JOIN tpa_master stpa ON stpa.tpa_id = pr.secondary_sponsor_id"
				+ " WHERE pr.patient_id = ?";
		
		public static BasicDynaBean getPatientVisitSponsorInfoBean(Connection con, String patientId) throws SQLException {

			PreparedStatement ps = null;
			try {
				ps = con.prepareStatement(GET_PATIENT_VISIT_SPONSOR_INFO);
				ps.setString(1, patientId);

				List list = DataBaseUtil.queryToDynaList(ps);
				if (list.size() > 0) {
					BasicDynaBean bean =  (BasicDynaBean) list.get(0);
					boolean precise = (bean.get("dateofbirth") != null);
		            if (bean.get("expected_dob") != null)
		            	bean.set("age_text", DateUtil.getAgeText((java.sql.Date) bean.get("expected_dob"), precise));
					return bean;
				} else {
					return null;
				}
			} finally {
				DataBaseUtil.closeConnections(null, ps);
			}
		}

		public static BasicDynaBean getPatientVisitSponsorInfoBean(String patientId) throws SQLException {

			Connection con = null;
	        try {
	             con = DataBaseUtil.getReadOnlyConnection();
	             return getPatientVisitSponsorInfoBean(con, patientId);
	        } finally {
	            DataBaseUtil.closeConnections(con, null);
	        }
		}

		private static final String GET_PATIENT_AND_VISIT_INFO = "SELECT pr.visit_type, pd.mr_no, pd.patient_gender,"
				+ " COALESCE(pd.dateofbirth, pd.expected_dob) AS expected_dob"
				+ " FROM patient_registration pr"
				+ " JOIN patient_details pd ON pr.mr_no = pd.mr_no"
				+ " WHERE pr.patient_id = ?";
		
		public static BasicDynaBean getPatientAndVisitInfoBean(Connection con, String patientId) throws SQLException {

			PreparedStatement ps = null;
			try {
				ps = con.prepareStatement(GET_PATIENT_AND_VISIT_INFO);
				ps.setString(1, patientId);

				return DataBaseUtil.queryToDynaBean(ps);
			} finally {
				DataBaseUtil.closeConnections(null, ps);
			}
		}

		public static BasicDynaBean getPatientAndVisitInfoBean(String patientId) throws SQLException {

			Connection con = null;
	        try {
	             con = DataBaseUtil.getReadOnlyConnection();
	             return getPatientAndVisitInfoBean(con, patientId);
	        } finally {
	            DataBaseUtil.closeConnections(con, null);
	        }
		}
		
		private static final String GET_PATIENT_VISIT_DETAILS_QUERY= "SELECT " +
			"pd.mr_no, pd.salutation as salutation_id, sm.salutation, pd.patient_name, pd.middle_name, pd.last_name,pd.name_local_language, " +
			"concat_ws(' ',sm.salutation,patient_name,middle_name,last_name) as full_name, " +
			"pd.patient_gender, pd.dateofbirth, COALESCE(pd.dateofbirth, pd.expected_dob) AS expected_dob, " +
			"CASE " +
			"	WHEN current_date - COALESCE(pd.dateofbirth, pd.expected_dob) < 31 " +
			"		THEN (floor(current_date - COALESCE(pd.dateofbirth, pd.expected_dob)))::integer " +
			"	WHEN current_date - COALESCE(pd.dateofbirth, pd.expected_dob) < 730 " +
			"		THEN (floor((current_date - COALESCE(pd.dateofbirth, pd.expected_dob))/30.43))::integer " +
			"	ELSE (floor((current_date - COALESCE(pd.dateofbirth, pd.expected_dob))/365.25))::integer " +
			"END AS age, " +
			"CASE " +
			"	WHEN current_date - COALESCE(pd.dateofbirth, pd.expected_dob) < 31 THEN 'D' " +
		 	"	WHEN (current_date - COALESCE(pd.dateofbirth, pd.expected_dob) < 730) THEN 'M' " +
			"	ELSE 'Y' " +
			"END AS agein, ''::text as age_text, " +
			"pd.patient_phone, pd.patient_phone2 AS addnl_phone, pd.patient_address, pd.patient_area, " +
			"pd.patient_city, ci.city_name AS cityname, pd.patient_state,st.state_name AS statename, " +
			"pd.country, cnm.country_name, ci.city_name, st.state_name, nc.country_name AS nationality_name, " +
			"pd.oldmrno, pd.casefile_no, pd.remarks, " +
			"pd.patient_care_oftext, pd.patient_careof_address, pd.relation, pd.next_of_kin_relation, " +
			"pd.death_date, pd.death_time,drm.reason as death_reason,pd.dead_on_arrival, " +
			"pd.custom_field1, pd.custom_field2, pd.custom_field3, pd.custom_field4, pd.custom_field5, pd.custom_field6, " +
			"pd.custom_field7, pd.custom_field8, pd.custom_field9, pd.custom_field10, pd.custom_field11, pd.custom_field12, " +
			"pd.custom_field13,custom_field14,pd.custom_field15,pd.custom_field16,pd.custom_field17, " +
			"pd.custom_field18,pd.custom_field19, pd.original_mr_no, " +
			"pd.custom_field5 as patient_source_category, pd.custom_field4 as patient_category_custom_field, " +
			"pd.custom_field6 as conditional_custom_field1, pd.custom_field7 as conditional_custom_field2, " +
			"pd.custom_field8 as conditional_custom_field3, pd.custom_field9 as conditional_custom_field4, " +
			"pd.custom_field10 as conditional_custom_field5,	pd.custom_field11 as clinical_field1, " +
			"pd.custom_field12 as clinical_field2, pd.custom_field13 as clinical_field3, " +
			"pd.custom_list1_value, pd.custom_list2_value, pd.custom_list3_value,pd.custom_list4_value, " +
			"custom_list5_value,custom_list6_value,custom_list7_value,custom_list8_value,custom_list9_value, " +
			"pra.visit_custom_list1, pra.visit_custom_list2, " +
			"pra.visit_custom_field1, pra.visit_custom_field2, pra.visit_custom_field3,pra.visit_custom_field4,pra.visit_custom_field5,pra.visit_custom_field6, " +
			"pra.visit_custom_field7,pra.visit_custom_field8,pra.visit_custom_field9, " +
			"pd.patient_category_id, pd.category_expiry_date, pcm.category_name, pd_pcm.category_name as patient_category_name, " +
			"pd.patient_consultation_info, " +
			"CASE WHEN pd.patient_photo IS NULL THEN 'N' ELSE 'Y' END AS patient_photo_available, " +
			"pd.previous_visit_id, pd.visit_id, pd.no_allergies, pd.med_allergies, pd.food_allergies, pd.other_allergies, pd.vip_status, " +
			"pd.government_identifier, pd.identifier_id, pd.portal_access, pd.email_id, " +
			"pd.passport_no, pd.passport_validity, pd.passport_issue_country, pd.visa_validity, pd.family_id, pd.nationality_id,  " +
			"mrd.file_status, mrd.indented, coalesce(depc.dept_name,mcu.file_user_name) as issued_to, pd.mod_time as patient_mod_time, " +
			"gim.remarks as govt_type_label,  " +
			"pra.patient_id, pra.status AS visit_status, pra.visit_type, pra.revisit, pra.reg_date, pra.reg_time, " +
			"pra.op_type, otn.op_type_name, pra.main_visit_id, pra.use_drg, pra.drg_code, pra.use_perdiem, pra.per_diem_code, " +
			"pra.mlc_status, pra.patient_category_id as patient_category, hcm.center_name,hcm.center_id, hcm.health_authority, hcm.center_code, " +
			"hcm.center_address , hcm.center_contact_phone , hcm.tin_number AS center_tin_number, " +
			"pra.patient_care_oftext AS patcontactperson, pra.relation AS patrelation, " +
			"pra.patient_careof_address AS pataddress, " +
			"pra.complaint, pra.analysis_of_complaint, " +
			"pra.doctor, dr.doctor_name, dr.specialization, dr.doctor_type, dr.doctor_address, " +
			"dr.doctor_mobile, dr.doctor_mail_id, dr.qualification, dr.registration_no, dr.res_phone, " +
			"dr.clinic_phone, dr.doctor_license_number, " +
			"pra.admitted_dept, admdep.dept_name AS admitted_dept_name,  " +
			"pra.dept_name AS dept_id, dep.dept_name,  pra.unit_id, dum.unit_name,	 " +
			"pra.org_id, od.org_name, od.store_rate_plan_id, " +
			"pra.bed_type AS bill_bed_type, bn.bed_type AS alloc_bed_type, bn.bed_name AS alloc_bed_name, " +
			"pra.ward_id AS reg_ward_id, wnr.ward_name AS reg_ward_name, wn.ward_name AS alloc_ward_name, " +
			"ad.admit_date as bed_start_date, date(ad.finalized_time) as bed_end_date, " +
			"pra.discharge_doc_id AS dis_doc_id, pra.discharge_format AS dis_format, " +
			"pra.discharge_flag, pra.discharge_doctor_id, pra.discharge_date, pra.discharge_time, " +
			"pra.discharge_finalized_date AS dis_finalized_date, pra.discharge_finalized_time AS dis_finalized_time, " +
			"pra.discharge_finalized_user AS dis_finalized_user, dtm.discharge_type, pra.discharged_by, pra.discharge_remarks, pra.user_name as admitted_by, " +
			"pra.codification_status,pra.established_type, pra.disch_date_for_disch_summary, pra.disch_time_for_disch_summary, " +
			"pra.reference_docto_id, COALESCE(drs.doctor_name, rd.referal_name) AS refdoctorname, " +
			"pra.reg_charge_accepted, " +
			"pra.mlc_no, pra.mlc_type, pra.accident_place, pra.police_stn, pra.mlc_remarks, pra.certificate_status, " +
			"pmd.icd_code AS primary_diagnosis_code, pmd.description AS primary_diagnosis_description, " +
			"amd.icd_code AS admitting_diagnosis_code, amd.description AS admitting_diagnosis_description, " +
			"(select textcat_commacat(description) from mrd_diagnosis md where (md.visit_id=pra.patient_id and diag_type='S')) " +
			"as secondary_diagnosis_description, prmain.primary_insurance_approval AS main_visit_primary_insurance_approval, " +
			"prmain.secondary_insurance_approval AS main_visit_secondary_insurance_approval, " +
			"pra.primary_insurance_approval,pra.secondary_insurance_approval, " +
			"pra.primary_sponsor_id, pra.secondary_sponsor_id, " +
			"tpa.tpa_name, stpa.tpa_name AS sec_tpa_name,tpa.tin_number AS tpa_tin_number, stpa.tin_number AS sec_tpa_tin_number, " +
			"tpa.sponsor_type AS sponsor_type, stpa.sponsor_type AS sec_sponsor_type, " +
			"icm.insurance_co_name, icm.insurance_co_address, " +
			"sicm.insurance_co_name as sec_insurance_co_name, sicm.insurance_co_address AS sec_insurance_co_address, " +
			"icm.tin_number AS insurance_co_tin_number, sicm.tin_number AS sec_insurance_co_tin_number, " +
			"tpa.state AS tpa_state, tpa.city AS tpa_city, tpa.country AS tpa_country, tpa.pincode AS tpa_pincode, " +
			"tpa.phone_no AS tpa_phone_no, tpa.mobile_no AS tpa_mobile_no, tpa.address AS tpa_address, " +
			"pra.insurance_id, pra.category_id AS insurance_category, " +
			"pra.plan_id, pra.prior_auth_id, pra.prior_auth_mode_id, pam.prior_auth_mode_name, " +
			"pra.doc_id, ins.prior_auth_id AS ins_prior_auth_id, " +
			"pra.primary_insurance_co, pra.secondary_insurance_co, " +
			"icam.category_name AS plan_type_name, ipm.plan_exclusions, ipm.plan_notes, pipm.plan_name,sipm.plan_name as sec_plan_name, " +
			"pra.patient_policy_id,pra.docs_download_passcode, " +
			"(case when modact.activation_status = 'Y' then ppd.member_id else ins.policy_no end) as member_id, " +
			"(case when modact.activation_status = 'Y' then ppd.policy_number else ins.insurance_no end) as policy_number, " +
			"(case when modact.activation_status = 'Y' then ppd.policy_validity_start else ins.policy_validity_start end) as policy_validity_start, " +
			"(case when modact.activation_status = 'Y' then ppd.policy_validity_end else ins.policy_validity_end end) as policy_validity_end, " +
			"(case when modact.activation_status = 'Y' then ppd.policy_holder_name else ins.policy_holder_name end) as policy_holder_name, " +
			"(case when modact.activation_status = 'Y' then ppd.patient_relationship else ins.patient_relationship end) as patient_relationship, " +
			"(case when modact.activation_status = 'Y' then sppd.member_id else ins.policy_no end) as sec_member_id, " +
			"(case when modact.activation_status = 'Y' then sppd.policy_number else ins.insurance_no end) as sec_policy_number, " +
			"(case when modact.activation_status = 'Y' then sppd.policy_validity_start else ins.policy_validity_start end) as sec_policy_validity_start, " +
			"(case when modact.activation_status = 'Y' then sppd.policy_validity_end else ins.policy_validity_end end) as sec_policy_validity_end, " +
			"(case when modact.activation_status = 'Y' then sppd.policy_holder_name else ins.policy_holder_name end) as sec_policy_holder_name, " +
			"(case when modact.activation_status = 'Y' then sppd.patient_relationship else ins.patient_relationship end) as sec_patient_relationship, " +
			"pcd.patient_relationship AS patient_corporate_relation, pcd.sponsor_id AS corporate_sponsor_id, " +
			"pcd.employee_id, pcd.employee_name, pnd.sponsor_id AS national_sponsor_id, " +
			"pnd.national_id, pnd.citizen_name, pnd.patient_relationship AS patient_national_relation, " +
			"spcd.patient_relationship AS sec_patient_corporate_relation, spcd.sponsor_id AS sec_corporate_sponsor_id, " +
			"spcd.employee_id AS sec_employee_id, spcd.employee_name AS sec_employee_name, spnd.sponsor_id AS sec_national_sponsor_id, " +
			"spnd.national_id AS sec_national_id, spnd.citizen_name AS sec_citizen_name ,spnd.patient_relationship AS sec_patient_national_relation " +
			",pra.signatory_username,pra.collection_center_id,scc.collection_center, coalesce(ipm.require_pbm_authorization, 'N') as require_pbm_authorization, " +
			"pst.member_id_label as primary_member_id_label,sst.member_id_label as secondary_member_id_label, " +
			"CASE WHEN pipm.limits_include_followup IS NOT NULL AND pipm.limits_include_followup = 'Y' THEN ppip.episode_limit " +
      " ELSE ppip.visit_limit END AS primary_approval_limit, " +
      "CASE WHEN sipm.limits_include_followup IS NOT NULL AND sipm.limits_include_followup = 'Y' THEN spip.episode_limit " +
      " ELSE spip.visit_limit END AS secondary_approval_limit, " +
			"	pst.plan_type_label as primary_plan_type_label, sst.plan_type_label as secondary_plan_type_label " +
		"FROM patient_registration pra " +
		   "JOIN patient_details pd ON pra.mr_no = pd.mr_no " +
		   "LEFT JOIN govt_identifier_master gim ON (pd.identifier_id=gim.identifier_id) " +
		   "LEFT JOIN op_type_names otn ON (otn.op_type = pra.op_type) " +
		   "LEFT JOIN ward_names wnr ON wnr.ward_no = pra.ward_id " +
		   "LEFT JOIN salutation_master sm ON pd.salutation = sm.salutation_id " +
		   "LEFT JOIN city ci ON pd.patient_city = ci.city_id " +
		   "LEFT JOIN state_master st ON pd.patient_state = st.state_id " +
		   "LEFT JOIN country_master cnm ON pd.country = cnm.country_id " +
		   "LEFT JOIN country_master nc ON pd.nationality_id = nc.country_id " +
		   "LEFT JOIN department dep ON pra.dept_name = dep.dept_id " +
		   "LEFT JOIN department admdep ON pra.admitted_dept = admdep.dept_id " +
		   "LEFT JOIN dept_unit_master dum ON dum.unit_id = pra.unit_id " +
		   "LEFT JOIN discharge_type_master dtm ON(dtm.discharge_type_id = pra.discharge_type_id) " +
		   "LEFT JOIN doctors dr ON dr.doctor_id = pra.doctor " +
		   "LEFT JOIN admission ad ON ad.patient_id = pra.patient_id " +
		   "LEFT JOIN bed_names bn ON bn.bed_id = ad.bed_id " +
		   "LEFT JOIN ward_names wn ON wn.ward_no = bn.ward_no " +
		   "LEFT JOIN organization_details od ON pra.org_id = od.org_id " +
		   "LEFT JOIN doctors drs ON pra.reference_docto_id = drs.doctor_id " +
		   "LEFT JOIN referral rd ON pra.reference_docto_id = rd.referal_no " +
		   "LEFT JOIN patient_category_master pcm on pcm.category_id = pra.patient_category_id " +
		   "LEFT JOIN patient_category_master pd_pcm ON pd_pcm.category_id=pd.patient_category_id " +
		   "LEFT JOIN tpa_master tpa ON tpa.tpa_id = pra.primary_sponsor_id " +
		   "LEFT JOIN tpa_master stpa ON stpa.tpa_id = pra.secondary_sponsor_id " +
		   "LEFT JOIN insurance_case ins ON pra.insurance_id = ins.insurance_id " +
		   "LEFT JOIN insurance_company_master icm ON icm.insurance_co_id = pra.primary_insurance_co " +
		   "LEFT JOIN insurance_company_master sicm ON sicm.insurance_co_id = pra.secondary_insurance_co " +
		   "LEFT JOIN insurance_plan_main ipm ON (pra.plan_id = ipm.plan_id) " +
		   "LEFT JOIN insurance_category_master icam  ON icam.category_id=ipm.category_id " +
		   "LEFT JOIN mrd_diagnosis pmd ON (pmd.visit_id = pra.patient_id AND pmd.diag_type = 'P') " +
		   "LEFT JOIN mrd_diagnosis amd ON (amd.visit_id = pra.patient_id AND amd.diag_type = 'A') " +
		   "LEFT JOIN patient_registration prmain ON (prmain.patient_id = pra.main_visit_id AND prmain.op_type = 'M') " +
		   "LEFT JOIN patient_insurance_plans ppip ON( ppip.patient_id = pra.patient_id AND ppip.priority = 1) " +
		   "LEFT JOIN patient_insurance_plans spip ON( spip.patient_id = pra.patient_id AND spip.priority = 2) " +
		   "LEFT JOIN insurance_plan_main pipm ON (ppip.plan_id = pipm.plan_id) " +
		   "LEFT JOIN insurance_plan_main sipm ON (spip.plan_id = sipm.plan_id) " +
		   "LEFT JOIN patient_policy_details ppd ON (ppd.mr_no = ppip.mr_no and ppd.status = 'A' AND ppip.patient_policy_id = ppd.patient_policy_id and ppip.plan_id = ppd.plan_id) " +
		   "LEFT JOIN patient_policy_details sppd ON (sppd.mr_no = ppip.mr_no and sppd.status = 'A' AND sppd.patient_policy_id = spip.patient_policy_id and spip.plan_id = sppd.plan_id) " +
		   "LEFT JOIN patient_corporate_details pcd ON (pcd.patient_corporate_id = pra.patient_corporate_id) " +
		   "LEFT JOIN patient_national_sponsor_details pnd ON (pnd.patient_national_sponsor_id = pra.patient_national_sponsor_id) " +
		   "LEFT JOIN patient_corporate_details spcd ON (spcd.patient_corporate_id = pra.secondary_patient_corporate_id) " +
		   "LEFT JOIN patient_national_sponsor_details spnd ON (spnd.patient_national_sponsor_id = pra.secondary_patient_national_sponsor_id) " +
		   "LEFT JOIN modules_activated modact ON (modact.module_id = 'mod_adv_ins') " +
		   "LEFT JOIN mrd_casefile_attributes mrd on (mrd.mr_no = pd.mr_no) " +
		   "LEFT JOIN mrd_casefile_users mcu on (mrd.issued_to_user = mcu.file_user_id) " +
		   "LEFT JOIN department depc on (depc.dept_id = mrd.issued_to_dept) " +
		   "LEFT JOIN prior_auth_modes pam ON ( pra.prior_auth_mode_id = pam.prior_auth_mode_id) " +
		   "LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pra.center_id) " +
		   "LEFT JOIN death_reason_master drm ON (drm.reason_id=pd.death_reason_id) " +
		   "LEFT JOIN sample_collection_centers scc ON (scc.collection_center_id = pra.collection_center_id) " +
		   "LEFT JOIN sponsor_type pst ON pst.sponsor_type_id = tpa.sponsor_type_id " +
		   "LEFT JOIN sponsor_type sst ON sst.sponsor_type_id = stpa.sponsor_type_id " +
		   "WHERE pra.patient_id = ?";
		
		public static BasicDynaBean getPatientVisitBean(String patientId) throws SQLException {

			PreparedStatement ps = null;
			Connection con = null;
			try {
				con = DataBaseUtil.getReadOnlyConnection();
				ps = con.prepareStatement(GET_PATIENT_VISIT_DETAILS_QUERY);
				ps.setString(1, patientId);

				List list = DataBaseUtil.queryToDynaList(ps);
				if (list.size() > 0) {
					BasicDynaBean bean =  (BasicDynaBean) list.get(0);
					boolean precise = (bean.get("dateofbirth") != null);
		            if (bean.get("expected_dob") != null)
		            	bean.set("age_text", DateUtil.getAgeText((java.sql.Date) bean.get("expected_dob"), precise));
					return bean;
				} else {
					return null;
				}
			} finally {
				DataBaseUtil.closeConnections(con, ps);
			}
		}
		
		public static final  String GET_IP_CREDITLIMIT = " SELECT ip_credit_limit_amount FROM patient_registration WHERE patient_id = ? ";
    public BigDecimal getIpCreditLimitAmount(String visitId)throws SQLException{
        if (visitId == null || (visitId.trim()).equals("")) {
          return null;
        }
        
        Connection con = null;
        PreparedStatement ps = null;
        try{
            con = DataBaseUtil.getReadOnlyConnection();
            ps = con.prepareStatement(GET_IP_CREDITLIMIT);
            ps.setString(1, visitId);
            return DataBaseUtil.getBigDecimalValueFromDb(ps);
        }finally{
            DataBaseUtil.closeConnections(con, ps);
        }
    }
   
    public BigDecimal getIpCreditLimitAmount(Connection con, String visitId)throws SQLException{
        if (visitId == null || (visitId.trim()).equals("")) {
          return null;
        }
        PreparedStatement ps = null;
        try{
            ps = con.prepareStatement(GET_IP_CREDITLIMIT);
            ps.setString(1, visitId);
            return DataBaseUtil.getBigDecimalValueFromDb(ps);
        }finally{
          DataBaseUtil.closeConnections(null, ps);
        }
    }
    
    public BigDecimal getAvailableCreditLimit(String visitId, boolean excludePatDue) throws SQLException {
      BigDecimal availableCreditLimit = BigDecimal.ZERO;
      BigDecimal sanctionedCreditLimit = getIpCreditLimitAmount(visitId);
      sanctionedCreditLimit = sanctionedCreditLimit == null ? BigDecimal.ZERO : sanctionedCreditLimit;
      BigDecimal availableDepositsBal = RegistrationBO.getAvailableGeneralAndIpDeposit(getMrno(visitId));
      BigDecimal visitPatientDue = BillDAO.getVisitPatientDue(visitId);
      visitPatientDue = visitPatientDue == null ? BigDecimal.ZERO : visitPatientDue;
      if(excludePatDue) {
        availableCreditLimit = sanctionedCreditLimit.add(availableDepositsBal);
      } else {
        availableCreditLimit = sanctionedCreditLimit.add(availableDepositsBal).subtract(visitPatientDue);
      }
       return availableCreditLimit;
    }
    
    public BigDecimal getAvailableCreditLimit(Connection con, String visitId, boolean excludePatDue) throws SQLException {
      BigDecimal availableCreditLimit = BigDecimal.ZERO;
      BigDecimal sanctionedCreditLimit = getIpCreditLimitAmount(con, visitId);
      sanctionedCreditLimit = sanctionedCreditLimit == null ? BigDecimal.ZERO : sanctionedCreditLimit;
      BigDecimal availableDepositsBal = RegistrationBO.getAvailableGeneralAndIpDeposit(con,getMrno(con,visitId));
      BigDecimal visitPatientDue = BillDAO.getVisitPatientDue(con,visitId);
      visitPatientDue = visitPatientDue == null ? BigDecimal.ZERO : visitPatientDue;
      if(excludePatDue) {
        availableCreditLimit = sanctionedCreditLimit.add(availableDepositsBal);
      } else {
        availableCreditLimit = sanctionedCreditLimit.add(availableDepositsBal).subtract(visitPatientDue);
      }
       return availableCreditLimit;
    }
    
    public BigDecimal calculateSanctionedCreditLimit(String visitId, BigDecimal availableCreditLimit) throws SQLException {
      BigDecimal sanctionedCreditLimit = BigDecimal.ZERO;
      BigDecimal availableDepositsBal = RegistrationBO.getAvailableGeneralAndIpDeposit(getMrno(visitId));
      BigDecimal visitPatientDue = BillDAO.getVisitPatientDue(visitId);
      visitPatientDue = visitPatientDue == null ? BigDecimal.ZERO : visitPatientDue;
      sanctionedCreditLimit = availableCreditLimit.subtract(availableDepositsBal).add(visitPatientDue);
      return sanctionedCreditLimit;
    }
    
    public Map<String, Object> getCreditLimitDetails(String visitId) throws SQLException {
      Map<String, Object> creditLimitDetailsMap= new HashMap<String, Object>();
      BigDecimal visitPatientDue = BillDAO.getVisitPatientDue(visitId);
      creditLimitDetailsMap.put("visitPatientDue", visitPatientDue);
      BigDecimal availableDepositsBal = RegistrationBO.getAvailableGeneralAndIpDeposit(getMrno(visitId));
      creditLimitDetailsMap.put("availableDeposit", availableDepositsBal);
      BigDecimal sanctionedCreditLimit = getIpCreditLimitAmount(visitId);
      sanctionedCreditLimit = sanctionedCreditLimit == null ? BigDecimal.ZERO : sanctionedCreditLimit;
      creditLimitDetailsMap.put("sanctionedCreditLimit", sanctionedCreditLimit); 
      BigDecimal availableCreditLimitWithoutDue = getAvailableCreditLimit(visitId, true);
      creditLimitDetailsMap.put("availableCreditLimitWithoutDue", availableCreditLimitWithoutDue);
      BigDecimal availableCreditLimit = getAvailableCreditLimit(visitId, false);
      creditLimitDetailsMap.put("availableCreditLimit", availableCreditLimit);
      return creditLimitDetailsMap;    
    }
    
    public Map<String, Object> getCreditLimitDetails(Connection con, String visitId) throws SQLException {
      Map<String, Object> creditLimitDetailsMap= new HashMap<String, Object>();
      BigDecimal visitPatientDue = BillDAO.getVisitPatientDue(con,visitId);
      creditLimitDetailsMap.put("visitPatientDue", visitPatientDue);
      BigDecimal availableDepositsBal = RegistrationBO.getAvailableGeneralAndIpDeposit(con,getMrno(con, visitId));
      creditLimitDetailsMap.put("availableDeposit", availableDepositsBal);
      BigDecimal sanctionedCreditLimit = getIpCreditLimitAmount(con,visitId);
      sanctionedCreditLimit = sanctionedCreditLimit == null ? BigDecimal.ZERO : sanctionedCreditLimit;
      creditLimitDetailsMap.put("sanctionedCreditLimit", sanctionedCreditLimit); 
      BigDecimal availableCreditLimitWithoutDue = getAvailableCreditLimit(con,visitId, true);
      creditLimitDetailsMap.put("availableCreditLimitWithoutDue", availableCreditLimitWithoutDue);
      BigDecimal availableCreditLimit = getAvailableCreditLimit(con, visitId, false);
      creditLimitDetailsMap.put("availableCreditLimit", availableCreditLimit);
      return creditLimitDetailsMap;    
    }

    public void updateCaseRateDetails(BasicDynaBean visitBean) throws SQLException, IOException{
      Connection con = null;
      Boolean success = false;
      try{
        con = DataBaseUtil.getConnection();
        con.setAutoCommit(false);
        success = update(con, visitBean.getMap(), "patient_id", (String)visitBean.get("patient_id")) >= 0;
      }finally{
        DataBaseUtil.commitClose(con, success);
      }
    }
    
    private static final String GET_VISIT_CASE_RATE_DETAILS = " SELECT pr.primary_case_rate_id, "
        + " pcrm.code||' / '||pcrm.code_description as primary_case_rate, "
        + " pr.secondary_case_rate_id, scrm.code||' / '||scrm.code_description as secondary_case_rate "
        + " FROM patient_registration pr "
        + " LEFT JOIN case_rate_main pcrm ON(pr.primary_case_rate_id = pcrm.case_rate_id) "
        + " LEFT JOIN case_rate_main scrm ON(pr.secondary_case_rate_id = scrm.case_rate_id) "
        + " WHERE pr.patient_id = ? AND "
        + " (pr.primary_case_rate_id IS NOT NULL OR pr.secondary_case_rate_id IS NOT NULL)";

    public BasicDynaBean getVisitCaseRateDetials(String visitId) throws SQLException{
      return DataBaseUtil.queryToDynaBean(GET_VISIT_CASE_RATE_DETAILS, new Object[]{visitId});
    }
    
    public BasicDynaBean getVisitCaseRateDetials(Connection con, String visitId) throws SQLException{
      return DataBaseUtil.queryToDynaBean(con, GET_VISIT_CASE_RATE_DETAILS, new Object[]{visitId});
    }
    
    private static final String GET_VIIST_DETAILS_WITH_EMR_ACCESS = "SELECT pd.mr_no, pr.patient_id, cgm.emr_access AS mandate_emr_comments"
    		+ " FROM patient_registration pr"
    		+ " JOIN patient_details pd ON (pd.mr_no = pr.mr_no "
    		+ "    AND ( patient_confidentiality_check(pd.patient_group,pd.mr_no) ))"
    		+ " JOIN confidentiality_grp_master cgm ON (cgm.confidentiality_grp_id = pd.patient_group) "
    		+ " WHERE pr.patient_id = ?";
    
    public BasicDynaBean getVisitDetailsWithEmrAccess(String visitId) throws SQLException {
    	return DataBaseUtil.queryToDynaBean(GET_VIIST_DETAILS_WITH_EMR_ACCESS, new Object[] {visitId});
    }
    
    private static final String GET_VIIST_WITH_EMR_ACCESS = "SELECT cgm.emr_access AS mandate_emr_comments"
    		+ " FROM patient_registration pr"
    		+ " JOIN patient_details pd ON (pd.mr_no = pr.mr_no "
    		+ "    AND ( patient_confidentiality_check(pd.patient_group,pd.mr_no) ))"
    		+ " JOIN confidentiality_grp_master cgm ON (cgm.confidentiality_grp_id = pd.patient_group) "
    		+ " WHERE pr.patient_id = ?";
    
    public BasicDynaBean getVisitWithEmrAccess(String visitId) throws SQLException {
    	return DataBaseUtil.queryToDynaBean(GET_VIIST_WITH_EMR_ACCESS, new Object[] {visitId});
    }
    
    private static final String GET_PATIENT_WITH_EMR_ACCESS = "SELECT cgm.emr_access AS mandate_emr_comments"
    		+ " FROM patient_details pd"
    		+ " JOIN confidentiality_grp_master cgm ON (cgm.confidentiality_grp_id = pd.patient_group"
    		+ "   AND ( patient_confidentiality_check(pd.patient_group,pd.mr_no) )) "
    		+ " WHERE pd.mr_no = ?";
    
    public BasicDynaBean getPatientWithEmrAccess(String mr_no) throws SQLException {
    	return DataBaseUtil.queryToDynaBean(GET_PATIENT_WITH_EMR_ACCESS, new Object[] {mr_no});
    }
    
    private static final String GET_VISIT_DETAILS_WITH_CONF_CHECK = "SELECT pr.patient_id, pr.status "
    		+ " FROM patient_registration pr"
    		+ " JOIN patient_details pd ON (pd.mr_no = pr.mr_no"
    		+ "    AND patient_confidentiality_check(pd.patient_group,pd.mr_no) )"
    		+ " WHERE pr.patient_id = ?";
    
    public BasicDynaBean getVisitDetailsWithConfCheck(String visit_id)throws SQLException {
    	return DataBaseUtil.queryToDynaBean(GET_VISIT_DETAILS_WITH_CONF_CHECK, new Object[] {visit_id});
    }

	private static final String GET_VISIT_BY_CENTER_ID = "SELECT pr.mr_no, pr.center_id"
			+ " FROM patient_registration pr"
			+ " JOIN patient_details pd ON (pd.mr_no = pr.mr_no"
			+ "    AND patient_confidentiality_check(pd.patient_group,pd.mr_no))"
			+ " WHERE pr.patient_id = ? AND pr.center_id = ?";

	public BasicDynaBean getVisitByCenterIdWithConfidentialityCheck(String visitId,
			Integer centerId) throws SQLException {
		return DataBaseUtil.queryToDynaBean(GET_VISIT_BY_CENTER_ID, new Object[] {visitId, centerId});
	}
	
  private static final String GET_SECONDARY_DIAGNOSIS_CODES = "SELECT "
      + " textcat_commacat(icd_code_value) as icd_codes "
      + " FROM (select icd_code as icd_code_value " + "   FROM mrd_diagnosis md "
      + " WHERE md.visit_id = ? AND md.diag_type='S') as foo ";

  public static String getSecondaryDiagnosisCodes(String patientId) throws SQLException {
    BasicDynaBean codeBean = DataBaseUtil.queryToDynaBean(GET_SECONDARY_DIAGNOSIS_CODES,
        new Object[] { patientId });
    String icdCode = null;
    if (null != codeBean) {
      icdCode = null != codeBean.get("icd_codes") ? (String) codeBean.get("icd_codes") : null;
    }
    return icdCode;
  }

  private static final String GET_OP_PATIENT_PRESCRIBED_MEDICNES =
        " SELECT textcat_commacat(sid.medicine_name) AS prescribed_medicines,"
      + " textcat_commacat(sic.item_code) AS drug_codes,"
      + " textcat_commacat(sic.item_code || '-' || sid.medicine_name) "
      + " AS prescribed_medicines_with_drug_code"
      + " FROM patient_prescription pp"
      + " JOIN doctor_consultation dc ON (pp.consultation_id = dc.consultation_id)"
      + " JOIN patient_medicine_prescriptions pmp"
      + " ON ( pp.patient_presc_id = pmp.op_medicine_pres_id)"
      + " JOIN store_item_details sid ON sid.medicine_id=pmp.medicine_id"
      + " JOIN patient_registration pr ON pr.patient_id=dc.patient_id"
      + " JOIN hospital_center_master hcm ON hcm.center_id=pr.center_id"
      + " LEFT JOIN ha_item_code_type hict ON hict.health_authority=hcm.health_authority "
      + " AND hict.medicine_id=sid.medicine_id"
      + " LEFT JOIN store_item_codes sic ON sic.medicine_id = hict.medicine_id "
      + " AND hict.code_type=sic.code_type"
      + " WHERE dc.patient_id =? ";

  /**
   * Gets the op patient prescribed medicines,drug codes.
   *
   * @param patientId
   *          the patient id
   * @return the op patient prescribed medicines.
   */
  public static BasicDynaBean getOpPatientPrescribedMedicines(String patientId) throws SQLException {
	  return DataBaseUtil.queryToDynaBean(GET_OP_PATIENT_PRESCRIBED_MEDICNES, new Object[] {patientId});
  }

  private final static String LATEST_LOG_ID = "SELECT log_id FROM patient_registration_audit_log"
      + " WHERE patient_id=? order by mod_time desc limit 1";
  
  /**
   * Gets latest log id.
   * 
   * @param visitId the visit id
   * @return the log id
   */
  public long getLatestLogId(String visitId) {
  	Long logId = DatabaseHelper.getLong(LATEST_LOG_ID, visitId);
  	return logId != null ? logId : 0L;
  }
  
  public BasicDynaBean getLastVisitOrgId(String mrNo) throws SQLException {
    try (Connection con = DataBaseUtil.getConnection()) {
      return getLastVisitOrgId(con, mrNo);
    }
  }
  
  private static final String GET_LAST_VISIT_ORG_ID = ""
      + " SELECT org_id "
      + " FROM patient_registration pr "
      + " WHERE mr_no = ? "
      + " ORDER BY reg_date DESC "
      + " LIMIT 1 ";

  public BasicDynaBean getLastVisitOrgId(Connection con, String mrNo) throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(GET_LAST_VISIT_ORG_ID)) {
      ps.setString(1, mrNo);
      return DataBaseUtil.queryToDynaBean(ps);
    }
  }
}
