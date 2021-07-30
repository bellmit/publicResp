package com.insta.hms.master.ReferalDoctor;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReferalDoctorDAO extends GenericDAO {

	public ReferalDoctorDAO() {
		super("referral");
	}

	private String NEXT_REFERAL_ID = "select nextval('referal_id_sequence')";

	public String getNextReferalId(Connection con) throws SQLException {
		Statement stmt = null;
		ResultSet rs = null;
		String refId = null;
		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery(NEXT_REFERAL_ID);
			if (rs.next())
				refId = rs.getString(1);
		} finally {
			rs.close();
			stmt.close();
		}
		return refId;
	}

	private static String REFERAL_FIELDS = "SELECT *";

	private static String REFERAL_COUNT = "SELECT count(*)";

	private static String REFERAL_TABLES = " FROM (SELECT r.*, c.cat_name" +
			" FROM referral r " +
			" LEFT OUTER JOIN category_type_master c ON(r.payment_category=c.cat_id)) AS foo";

	private static String CENTER_WISE_REFERAL_DOCTOR = " FROM (SELECT distinct  r.*, c.cat_name,rcm.center_id" +
			" FROM referral r " +
			" left join referral_center_master rcm on  (r.referal_no = rcm.referal_no) "+
			" LEFT OUTER JOIN category_type_master c ON(r.payment_category=c.cat_id)) AS foo";

	public PagedList getReferalDoctorDetails(Map requestParams, Map<LISTING, Object> pagingParams)
			throws ParseException, SQLException {

		Connection con = null;
		SearchQueryBuilder qb = null;
		List list = new ArrayList();
		int centerID = RequestContext.getCenterId();
		try {
			con = DataBaseUtil.getReadOnlyConnection();

			if(centerID != 0 && GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1) {
				qb = new SearchQueryBuilder(con, REFERAL_FIELDS, REFERAL_COUNT, CENTER_WISE_REFERAL_DOCTOR, pagingParams);
				qb.addFilterFromParamMap(requestParams);
				list.add(RequestContext.getCenterId());
				list.add(0);
				qb.addFilter(qb.INTEGER, "center_id", "IN", list);
			} else {
				qb = new SearchQueryBuilder(con, REFERAL_FIELDS, REFERAL_COUNT, REFERAL_TABLES, pagingParams);
				qb.addFilterFromParamMap(requestParams);
			}

			qb.addSecondarySort("referal_no");
			qb.build();

			return qb.getMappedPagedList();
		} finally {
				qb.close();
				DataBaseUtil.closeConnections(con, null);
			}
	}

	private static final String REFERRAL_BY_ID = "SELECT * FROM referral where referal_no = ?";

	public static BasicDynaBean getReferralById(Connection con, String id) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(REFERRAL_BY_ID);
			ps.setString(1, id);
			List list = DataBaseUtil.queryToDynaList(ps);
			if (list.size() > 0) {
				return (BasicDynaBean) list.get(0);
			}
			else {
				return null;
			}
		}
		finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}
	

	private static final String GET_REF_DOCTORS = "SELECT d.doctor_id AS ref_id, d.doctor_name AS ref_name, " +
		" d.doctor_mobile AS ref_mobile, 'D' as ref_type, d.doctor_license_number  AS clinician_id  " +
		"  FROM doctors d " +
		" JOIN doctor_center_master dcm ON(d.doctor_id=dcm.doctor_id)"+
		" WHERE d.status = 'A' AND (dcm.center_id = 0 or dcm.center_id = ? ) and dcm.status = 'A'  " +
		" UNION SELECT r.referal_no AS ref_id, r.referal_name AS ref_name, r.referal_mobileno AS ref_mobile, 'O' AS ref_type, r.clinician_id " +
		"  FROM referral r " +
		"  JOIN referral_center_master rcm on (r.referal_no = rcm.referal_no) WHERE r.status ='A' "
		+ " AND rcm.status = 'A' AND (rcm.center_id = 0 or rcm.center_id = ? ) " +
		" ORDER BY ref_name";

	private static final String GET_PRESCRIBED_DOCTORS  = "SELECT d.doctor_id AS pres_doctor, d.doctor_name AS doctor_name " +
			" FROM doctors d " +
			" JOIN doctor_center_master dcm ON(d.doctor_id=dcm.doctor_id) " +
			" WHERE d.status = 'A' AND (dcm.center_id = 0 or dcm.center_id = ? ) and dcm.status = 'A'";
	
	private static final String REF_DOCTORS_SEARCH_QUERY = "SELECT d.doctor_id AS ref_id, d.doctor_name AS ref_name, " +
        " d.doctor_mobile AS ref_mobile, 'D' as ref_type, d.doctor_license_number  AS clinician_id  " +
        "  FROM doctors d " +
        " JOIN doctor_center_master dcm ON(d.doctor_id=dcm.doctor_id)"+
        " WHERE d.status = 'A' AND (dcm.center_id = 0 or dcm.center_id = ? ) and dcm.status = 'A' AND LOWER(d.doctor_name) LIKE ?" +
        " UNION SELECT r.referal_no AS ref_id, r.referal_name AS ref_name, r.referal_mobileno AS ref_mobile, 'O' AS ref_type, r.clinician_id " +
        "  FROM referral r " +
        "  JOIN referral_center_master rcm on (r.referal_no = rcm.referal_no) WHERE r.status ='A' "
        + " AND rcm.status = 'A' AND (rcm.center_id = 0 or rcm.center_id = ? ) AND LOWER(r.referal_name) LIKE ?" +   
        " ORDER BY ref_name";

	public static ArrayList getReferencedoctors() throws SQLException {
	    Connection con = null;
	    PreparedStatement ps = null;
	    ArrayList refDoc = null;
	    int centerID = RequestContext.getCenterId();
	    try{
	        con = DataBaseUtil.getConnection();
	        ps = con.prepareStatement(GET_REF_DOCTORS);
	        ps.setInt(1, centerID);
	        ps.setInt(2, centerID);
	        refDoc = DataBaseUtil.queryToArrayList(ps);
	    }finally{
	    	DataBaseUtil.closeConnections(con, ps);
	    }
	    return refDoc;
	}
	
  public static List getReferenceDoctors(String searchQuery) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    List refDocs = null;
    int centerId = RequestContext.getCenterId();
    String lowerSearchQuery="";
    if(searchQuery != null) {
      lowerSearchQuery = searchQuery.toLowerCase();
    }
    con = DataBaseUtil.getConnection();
    try {
      ps = con.prepareStatement(REF_DOCTORS_SEARCH_QUERY);
      ps.setInt(1, centerId);
      ps.setString(2, "%" + lowerSearchQuery + "%");
      ps.setInt(3, centerId);
      ps.setString(4, "%" + lowerSearchQuery + "%");
      refDocs = DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return refDocs;
  }

	public static ArrayList getPrescribedDoctors() throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		ArrayList refDoc = null;
		int centerID = RequestContext.getCenterId();
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_PRESCRIBED_DOCTORS);
			ps.setInt(1, centerID);
			refDoc = DataBaseUtil.queryToArrayList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		return refDoc ;
	}

	public static ArrayList getEditPatientVisitReferencedoctors(int centerID ) throws SQLException {
	    Connection con = null;
	    PreparedStatement ps = null;
	    ArrayList refDoc = null;
	    try{
	        con = DataBaseUtil.getConnection();
	        ps = con.prepareStatement(GET_REF_DOCTORS);
	        ps.setInt(1, centerID);
	        ps.setInt(2, centerID);
	        refDoc = DataBaseUtil.queryToArrayList(ps);
	    }finally{
	    	DataBaseUtil.closeConnections(con, ps);
	    }
	    return refDoc ;
	}

	private String DUPLICATE_REFERAL = "SELECT referal_no,referal_name FROM referral WHERE referal_name=?";

	public boolean checkDuplicateReferal(Connection con, String referalName) throws SQLException{
		boolean duplicate = false;
		PreparedStatement ps = null;
		if (referalName.equals("")) return true;
		ps = con.prepareStatement(DUPLICATE_REFERAL);
		ps.setString(1, referalName);
		ResultSet rs = ps.executeQuery();
		if(rs.next()){
			duplicate=true;
		}
		return duplicate;
	}

	private  String INSERT_REFERAL = "insert into referral(referal_no, referal_name,referal_mobileno, " +
			" payment_category,payment_eligible)  values(?,?,?,?,?)";

	public  boolean saveNewReferal(Connection con, String referalId, String referalName, String mobileNo,
			int referalCategory,String paymentEligible) throws SQLException {
		boolean status = false;
			PreparedStatement ps = null;
			ps = con.prepareStatement(INSERT_REFERAL);
			ps.setString(1, referalId);
			ps.setString(2, referalName);
			ps.setString(3, mobileNo);
			ps.setInt(4, referalCategory);
			ps.setString(5, paymentEligible);
			int i= ps.executeUpdate();
			if(i>0){
				 status = true;
			 }
			 ps.close();
		return status;
	}

	private static final String GET_REF_DOCTOR_LICENSE_NO = "SELECT * FROM referral WHERE clinician_id=?";

	public static boolean getDoctorLicenseNo(String refLicenseNo) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean flag = false;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_REF_DOCTOR_LICENSE_NO);
			ps.setString(1, refLicenseNo);
			rs = ps.executeQuery();
			if(rs.next()) {
				flag = true;
			}
		} finally {
			DataBaseUtil.closeConnections(con, ps, rs);
		}
		return flag;
	}

	private static final String GET_REFERRAL_DOCTOR = "SELECT * FROM referral where referal_no=?";

	public static BasicDynaBean getReferalDetails(String referalNo) throws SQLException {
		PreparedStatement ps = null;
		Connection con = DataBaseUtil.getConnection();
		try {
			ps = con.prepareStatement(GET_REFERRAL_DOCTOR);
			ps.setString(1, referalNo);
			return (BasicDynaBean)DataBaseUtil.queryToDynaList(ps).get(0);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	private static final String GET_REFERRAL_DOCTOR_DETAILS = " SELECT ref.*, "
	    + " cm.country_name, sm.state_name, ctm.city_name, am.area_name, dm.district_name, "
	    + " cm.country_id, sm.state_id, ctm.city_id, am.area_id, dm.district_id "
	    + " FROM referral ref "
	    + " LEFT JOIN area_master am ON (am.area_id = ref.referal_doctor_area_id) "
	    + " LEFT JOIN city ctm ON (ctm.city_id = ref.referal_doctor_city_id AND ctm.city_id = am.city_id) "
	    + " LEFT JOIN state_master sm ON (sm.state_id = ctm.state_id) "
	    + " LEFT JOIN district_master dm ON (dm.district_id = ctm.district_id AND dm.state_id = sm.state_id ) "
	    + " LEFT JOIN country_master cm ON (cm.country_id = sm.country_id) "
	    + " where referal_no=?";
	
	public BasicDynaBean getReferalDoctorDetails(String referalNo) throws SQLException {
	  Connection con = null;
	  PreparedStatement ps = null;
	  try {
	    con = DataBaseUtil.getReadOnlyConnection();
	    ps = con.prepareStatement(GET_REFERRAL_DOCTOR_DETAILS);
	    ps.setString(1, referalNo);
	    return (BasicDynaBean)DataBaseUtil.queryToDynaBean(ps);
	  } finally {
	    DataBaseUtil.closeConnections(con, ps);
	  }
	}

	private static final String REFERRAL_OR_CENTER_DOCTOR_DETAILS =
      "SELECT * "
          + " FROM (SELECT d.doctor_id AS referal_no, d.doctor_name AS referal_name, "
          + " d.doctor_mobile AS referal_mobileno, 'D' as ref_type,"
          + " d.doctor_license_number  AS clinician_id, dcm.center_id, "
          + " null as area_id, ctm.city_id, dm.district_id, sm.state_id, "
          + " null as area_name, ctm.city_name, dm.district_name, sm.state_name, " 
          + " cm.country_name, cm.country_id " 
          + " FROM doctors d "
          + " JOIN doctor_center_master dcm ON(d.doctor_id=dcm.doctor_id) "
          + " LEFT JOIN hospital_center_master hcm ON(hcm.center_id = dcm.center_id) "
          + " LEFT JOIN city ctm ON (ctm.city_id = hcm.city_id) "
          + " LEFT JOIN state_master sm ON (sm.state_id = ctm.state_id "
          + "                                AND sm.state_id = hcm.state_id) "
          + " LEFT JOIN district_master dm ON (dm.district_id = ctm.district_id "
          + "                                AND dm.state_id = sm.state_id ) "
 	      + " LEFT JOIN country_master cm ON (cm.country_id = sm.country_id) "
          + " WHERE d.status = 'A' and dcm.status = 'A' and d.doctor_id=? "
          + " UNION "
          + " SELECT r.referal_no, r.referal_name, r.referal_mobileno, 'O' AS ref_type,"
          + " r.clinician_id, rcm.center_id, am.area_id, ctm.city_id, dm.district_id, sm.state_id,"
          + " am.area_name, ctm.city_name, dm.district_name, sm.state_name, "
          + " cm.country_name, cm.country_id " 
          + " FROM referral r "
          + " JOIN referral_center_master rcm on (r.referal_no = rcm.referal_no) "
          + " LEFT JOIN area_master am ON (am.area_id = r.referal_doctor_area_id) "
          + " LEFT JOIN city ctm ON (ctm.city_id = r.referal_doctor_city_id "
          + "                         AND ctm.city_id = am.city_id) "
          + " LEFT JOIN state_master sm ON (sm.state_id = ctm.state_id) "
	      + " LEFT JOIN country_master cm ON (cm.country_id = sm.country_id) "
          + " LEFT JOIN district_master dm ON (dm.district_id = ctm.district_id "
          + "                         AND dm.state_id = sm.state_id ) "
          + " WHERE r.status ='A' and rcm.status = 'A' and r.referal_no=?"
          + " ORDER BY referal_name) as foo limit 1";

	public BasicDynaBean getReferalOrCenterDoctorDetails(String referalNo) throws SQLException {
	  Connection con = null;
	  PreparedStatement ps = null;
	  try {
	    con = DataBaseUtil.getReadOnlyConnection();
	    ps = con.prepareStatement(REFERRAL_OR_CENTER_DOCTOR_DETAILS);
	    ps.setString(1, referalNo);
	    ps.setString(2, referalNo);
	    return (BasicDynaBean)DataBaseUtil.queryToDynaBean(ps);
	  } finally {
	    DataBaseUtil.closeConnections(con, ps);
	  }
	}

}
