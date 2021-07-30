package com.insta.hms.master.DoctorMaster;

import com.bob.hms.common.AutoIncrementId;
import com.bob.hms.common.Constants;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO;

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
import java.util.List;
import java.util.Map;

public class DoctorMasterDAO extends GenericDAO{

	public static String[] chargeValues = {
		"op_charge","doctor_ip_charge","night_ip_charge","ward_ip_charge", "ot_charge","co_surgeon_charge",
		"assnt_surgeon_charge","op_revisit_charge","private_cons_charge","private_cons_revisit_charge"
	};

	public static String[] chargeTexts = {
		"OP Consultation","IP Consultation","Night Consultation","IP Ward Visit","Surgeon/Anesthetist","Co-Surgeon/Anesthetist",
		"Asst-Surgeon/Anesthetist","OP Revisit Consultation","Private OP Consultation","Private OP Revisit Consultation"
	};

	public DoctorMasterDAO() {
		super("doctors");
	}

	public static final String DENTAL_DOCTORS =
			" SELECT distinct d.doctor_id, d.doctor_name, speciality_id FROM doctors d " +
			" JOIN doctor_center_master dcm ON (d.doctor_id = dcm.doctor_id)"+
			" JOIN department dept ON dept.dept_id=d.dept_id " +
			" where d.status='A' and dept_type_id='DENT' and dcm.status='A' ";

	public static final String DENTAL_DOCTORS_CENTERWISE =
			" SELECT distinct d.doctor_id, d.doctor_name, speciality_id FROM doctors d " +
			" JOIN doctor_center_master dcm ON (d.doctor_id = dcm.doctor_id)"+
			" JOIN department dept ON dept.dept_id=d.dept_id " +
			" where d.status='A' and dept_type_id='DENT' and dcm.status='A' and (dcm.center_id=? OR dcm.center_id=0) ";

	/*
	 * Generic DAO method has been overridden because of the need to maintain a separate token table
	 * for doctors (doctor_consultations_tokens). Whenever a doctor is inserted, a corresponding
	 * entry has to go in to the token table with the token value set to default(0); The insert
	 * method query has been appended with a separate token insertion query.
	 */
	public static final String INSERT_TOKEN_QUERY = "INSERT INTO doctor_consultation_tokens"
	        + " (doctor_id) VALUES (?)";

    @Override
    public boolean insert(Connection connection, BasicDynaBean bean) throws SQLException, IOException {
        String insertQuery = super.getInsertQuery(bean.getMap().keySet());
        insertQuery = insertQuery.concat("; ").concat(INSERT_TOKEN_QUERY);
        PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
        super.setParameterValues(insertStatement, bean, new Object[] {bean.get("doctor_id")});
        try {
            return insertStatement.executeUpdate() == 1;
        } finally {
            DataBaseUtil.closeConnections(null, insertStatement);
        }
    }

	public static List getDentalDoctors() throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		int centerID = RequestContext.getCenterId();
		try {
				if(centerID != 0 && GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1){
					ps = con.prepareStatement(DENTAL_DOCTORS_CENTERWISE);
					ps.setInt(1, centerID);
				}else {
					ps = con.prepareStatement(DENTAL_DOCTORS);
				}
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}


	public static BasicDynaBean getDoctorDept(String doctorId) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		BasicDynaBean basicDynaBean = null;
		try {
			ps = con.prepareStatement("SELECT d.dept_name, dr.doctor_name, d.dept_id FROM department d JOIN doctors dr ON dr.dept_id=d.dept_id WHERE dr.doctor_id=?");
			ps.setString(1, doctorId);
			basicDynaBean = (BasicDynaBean) DataBaseUtil.queryToDynaList(ps).get(0);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return basicDynaBean;
	}

	Connection con = null;

	static Logger logger = LoggerFactory.getLogger(DoctorMasterDAO.class);

	public String getNextDoctorId() throws Exception {
		String id = null;
		id = AutoIncrementId.getNewIncrId("DOCTOR_ID", "DOCTORS", "Doctor");
		return id;
	}

	public static final String QUERY8TO20 = "SELECT TO_CHAR('now'::time , 'HH24') AS HOURS";

	public String getIPConsultationCharge(String doctorid, String bedtype,
			String orgid) {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String doctorcharge = null;
		try {
			con = DataBaseUtil.getConnection();

			String hoursStr = DataBaseUtil.getStringValueFromDb(QUERY8TO20);
			int presenthour = Integer.parseInt(hoursStr);
			BasicDynaBean regPrefs = new RegistrationPreferencesDAO().getRecord();
			int am = ((BigDecimal) regPrefs.get("night_am")).intValue();
			int pm = ((BigDecimal) regPrefs.get("night_pm")).intValue();

			String orgquery = "select org_id from organization_details where org_name=?";
			String generalorgid = DataBaseUtil.getStringValueFromDb(orgquery,
			    Constants.getConstantValue("ORG"));

			if (presenthour >= am && presenthour < pm) {
				// day charge
				String chargequery = "select doctor_ip_charge from doctor_consultation_charge  where doctor_name=? and bed_type=? and organization=?";
				ps = con.prepareStatement(chargequery);

				ps.setString(1, doctorid);
				ps.setString(2, bedtype);
				ps.setString(3, orgid);

				rs = ps.executeQuery();
				if (rs.next()) {
					doctorcharge = rs.getString(1);
				} else {
					ps.setString(1, doctorid);
					ps.setString(2, Constants.getConstantValue("BEDTYPE"));
					ps.setString(3, generalorgid);
					rs = ps.executeQuery();
					if (rs.next()) {
						doctorcharge = rs.getString(1);
					}
				}
			} else {
				// night charge
				String chargequery = "select night_ip_charge from doctor_consultation_charge  where doctor_name=? and bed_type=? and organization=?";

				ps = con.prepareStatement(chargequery);

				ps.setString(1, doctorid);
				ps.setString(2, bedtype);
				ps.setString(3, orgid);

				rs = ps.executeQuery();
				if (rs.next()) {
					doctorcharge = rs.getString(1);
				} else {
					ps.setString(1, doctorid);
					ps.setString(2, Constants.getConstantValue("BEDTYPE"));
					ps.setString(3, generalorgid);
					rs = ps.executeQuery();
					doctorcharge = rs.getString(1);
				}

			}

		} catch (Exception e) {
			logger.error(
					"Exception occured in getIPConsultationCharge method", e);
		} finally {
			DataBaseUtil.closeConnections(con, ps, rs);
		}

		return doctorcharge;
	}

	public BasicDynaBean getDoctorAllCharges(String doctorId, String orgId,
			String bedType) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		BasicDynaBean doctorCharges = null;
		List dynalist = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_DOCTOR_DEPT_QUERY
							+ "  AND dc.bed_type=? AND dc.organization=? "
							+ "  AND dopc.org_id=? AND d.doctor_id=? ORDER BY doctor_name");
			ps.setString(1, bedType);
			ps.setString(2, orgId);
			ps.setString(3, orgId);
			ps.setString(4, doctorId);

			dynalist = DataBaseUtil.queryToDynaList(ps);
			if (dynalist.size() > 0) {
				doctorCharges = (BasicDynaBean) DataBaseUtil.queryToDynaList(ps).get(0);
			} else {
				ps = con.prepareStatement(GET_DOCTOR_DEPT_QUERY
								+ "  AND dc.bed_type='GENERAL' AND dc.organization='ORG0001' "
								+ "  AND dopc.org_id='ORG0001' AND d.doctor_id=? ORDER BY doctor_name");
				ps.setString(1, doctorId);
				dynalist = DataBaseUtil.queryToDynaList(ps);
				if (dynalist.size() > 0) {
					doctorCharges = (BasicDynaBean) DataBaseUtil.queryToDynaList(ps).get(0);
				}
			}
			return doctorCharges;
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_OP_CHARGES = "SELECT doctor_id,op_charge,op_revisit_charge FROM"
			+ " doctor_op_consultation_charge WHERE org_id=?";

	public static List<BasicDynaBean> getDoctorOpCharges(String orgId)
			throws SQLException {
		List<BasicDynaBean> beanList = null;
		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_OP_CHARGES);
			ps.setString(1, orgId);
			beanList = DataBaseUtil.queryToDynaList(ps);
		} finally {
			if (ps != null)
				ps.close();
			if (con != null)
				con.close();
		}
		return beanList;
	}

	private static final String GET_ALL_DOCTORS = " SELECT distinct d.doctor_id, d.doctor_name, d.status FROM doctors d " +
													" LEFT JOIN doctor_center_master dcm ON(d.doctor_id = dcm.doctor_id) "+
													" where d.status='A' and dcm.status='A' "+
													" order by doctor_name ";
	private static final String GET_ALL_DOCTORS_CENTERWISE = " SELECT distinct d.doctor_id, d.doctor_name, d.status FROM doctors d " +
										" LEFT JOIN doctor_center_master dcm ON(d.doctor_id = dcm.doctor_id) "+
										" where d.status='A' and dcm.status='A' and (dcm.center_id = 0 OR dcm.center_id = ?) "+
										" order by doctor_name ";

	public List getAllDoctor() throws SQLException {
		int centerID = RequestContext.getCenterId();
		List l = null;
		PreparedStatement ps = null;
		Connection con = null;
		con = DataBaseUtil.getConnection();
		if(centerID != 0 && GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1){
				ps = con.prepareStatement(GET_ALL_DOCTORS_CENTERWISE);
				ps.setInt(1, centerID);
		}else {
			ps = con.prepareStatement(GET_ALL_DOCTORS);
		}
		l = DataBaseUtil.queryToArrayList(ps);
		ps.close();
		con.close();
		return l;
	}
	/*private static final String GET_ALL_ACTIVE_DOCTORS =
		"SELECT doctor_id, doctor_name,center_id FROM doctors WHERE status='A' order by doctor_name ";
		*/
	private static final String GET_ALL_ACTIVE_DOCTORS =
		" SELECT distinct d.doctor_id, d.doctor_name, dcm.center_id, d.dept_id FROM doctors d" +
		" LEFT JOIN doctor_center_master dcm ON(d.doctor_id = dcm.doctor_id)"+
		" WHERE d.status='A' and dcm.status='A' order by doctor_name ";

	private static final String GET_ALL_ACTIVE_DOCTORS_CENTERWISE =
		" SELECT distinct d.doctor_id, d.doctor_name, dcm.center_id, d.dept_id FROM doctors d" +
		" LEFT JOIN doctor_center_master dcm ON (d.doctor_id = dcm.doctor_id) "+
		" WHERE d.status='A' and dcm.status='A' and (dcm.center_id=? OR dcm.center_id=0) order by doctor_name ";

	public static List getAllActiveDoctors() throws SQLException {
		List l = null;
		Connection con = null;
		PreparedStatement ps = null;
		int centerID = RequestContext.getCenterId();

		try {
			con = DataBaseUtil.getConnection();
			if(centerID != 0 && GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1) {
					ps = con.prepareStatement(GET_ALL_ACTIVE_DOCTORS_CENTERWISE);
					ps.setInt(1, centerID);
			}else {
				ps = con.prepareStatement(GET_ALL_ACTIVE_DOCTORS);
			}
			l = DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}

		return l;
	}

	/*private static final String GET_ALL_ACTIVE_DOCTORS_FOR_CENTERS =
		"SELECT doctor_id, doctor_name,center_id FROM doctors WHERE status='A' AND center_id IN(?,0) order by doctor_name ";
	*/

	private static final String GET_ALL_ACTIVE_DOCTORS_FOR_CENTERS =
		" SELECT distinct d.doctor_id, d.doctor_name,dcm.center_id "+
		" FROM doctors d "+
		" LEFT JOIN doctor_center_master dcm ON (d.doctor_id=dcm.doctor_id) "+
		" WHERE d.status='A' AND dcm.center_id IN(?,0) AND dcm.status='A' order by d.doctor_name ";

	public static List getAllCenterActiveDoctors(Integer centerId) throws SQLException {
		List l = null;
		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_ALL_ACTIVE_DOCTORS_FOR_CENTERS);
			ps.setInt(1, centerId);
			l = DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}

		return l;
	}


	public static List getAllDoctorNames() throws SQLException {
		return DataBaseUtil.queryToDynaList(GET_ALL_DOCTORS);
	}

	private static String GET_ALL_REFERERS = " SELECT doctor_name as referer_name FROM doctors "
			+ " UNION " + " SELECT referal_name as referer_name FROM referral ";

	public static List getAllRefererNames() throws SQLException {
		return DataBaseUtil.queryToDynaList(GET_ALL_REFERERS);
	}

	private String GET_ALL_DEPT_IDS = "SELECT dept_id FROM department";

	public ArrayList<String> getAllDepts() throws SQLException {
		ArrayList<String> l = null;
		Connection con = null;
		PreparedStatement ps = null;

		con = DataBaseUtil.getConnection();
		ps = con.prepareStatement(GET_ALL_DEPT_IDS);
		l = DataBaseUtil.queryToOnlyArrayList(ps);
		logger.debug("all deptId====>" + l);

		ps.close();
		con.close();

		return l;
	}

	private static final String GET_BED_TYPES = "SELECT distinct bed_type as bed FROM bed_details UNION "
			+ "SELECT distinct intensive_bed_type as bed FROM icu_bed_charges";

	public ArrayList getBedTypes() throws SQLException {
		ArrayList al = null;
		Connection con = null;
		PreparedStatement ps = null;
		con = DataBaseUtil.getConnection();
		ps = con.prepareStatement(GET_BED_TYPES);
		al = DataBaseUtil.queryToArrayList(ps);

		ps.close();
		con.close();

		return al;
	}


	public List getOTDoctorCharges(String otdoctorid, String bedtype,
			String orgid) throws SQLException {
		List cl = null;
		Connection con = DataBaseUtil.getConnection();
		String chargequery = "select ot_charge as charge,co_surgeon_charge as cosurgeoncharge ,assnt_surgeon_charge as asst_charge "
				+ " from doctor_consultation_charge where"
				+ " doctor_name=? and bed_type=? and organization=?";
		PreparedStatement ps = con.prepareStatement(chargequery);
		ps.setString(1, otdoctorid);
		ps.setString(2, bedtype);
		ps.setString(3, orgid);
		cl = DataBaseUtil.queryToArrayList(ps);
		logger.debug("{}", cl);
		if (ps != null)
			ps.close();
		if (con != null)
			con.close();
		return cl;
	}

	private static final String ot_charges = "select ot_charge as charge,co_surgeon_charge as cosurgeoncharge ,assnt_surgeon_charge as asst_charge "
			+ " from doctor_consultation_charge where"
			+ " doctor_name=? and bed_type=? and organization=?";

	public BasicDynaBean getDoctorOTCharges(String otdoctorid, String bedtype,
			String orgid) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(ot_charges);
			ps.setString(1, otdoctorid);
			ps.setString(2, bedtype);
			ps.setString(3, orgid);
			return (BasicDynaBean) DataBaseUtil.queryToDynaList(ps).get(0);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String CHECK_FOR_DUPLICATE = "SELECT count(*) FROM DOCTORS WHERE doctor_name=?";

	public static boolean checkDuplicate(String newDoctorName)
			throws SQLException {
		boolean status = true;
		Connection con = DataBaseUtil.getReadOnlyConnection();
		con.setAutoCommit(false);
		PreparedStatement ps = con.prepareStatement(CHECK_FOR_DUPLICATE);
		ps.setString(1, newDoctorName);

		String count = DataBaseUtil.getStringValueFromDb(ps);
		if (count.equals("0")) {
			status = false;
		}

		ps.close();
		con.close();

		return status;
	}

	private static final String DOCTOR_CHARGES = "select distinct(doctor_id),d.doctor_name,op_charge,doctor_ip_charge,night_ip_charge,ward_ip_charge,ot_charge,co_surgeon_charge"
			+ ",assnt_surgeon_charge from doctor_op_consultation_charge"
			+ " join doctor_consultation_charge on(doctor_id=doctor_name)"
			+ " join doctors d using(doctor_id) "
			+ " where doctor_id=? and organization=? and bed_type=?";

	public BasicDynaBean getDoctorDetails(String doctor, String org, String bed)
			throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(DOCTOR_CHARGES);
			ps.setString(1, doctor);
			ps.setString(2, org);
			ps.setString(3, bed);
			return (BasicDynaBean) DataBaseUtil.queryToDynaList(ps).get(0);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}

	}

	private static final String GET_COUNT_OF_ALL_DOCTORS = "SELECT count(*) FROM doctors";

	public static int getAllDocorsCoust() throws SQLException {
		int count = 0;
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_COUNT_OF_ALL_DOCTORS);

			String countStr = DataBaseUtil.getStringValueFromDb(ps);
			if (countStr != null && !countStr.equals(""))
				count = Integer.parseInt(countStr);

		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}

		return count;
	}

	private static final String DOCTOR_BY_ID = "SELECT * FROM doctors where doctor_id = ?";

	public static BasicDynaBean getDoctorById(String id) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(DOCTOR_BY_ID);
			ps.setString(1, id);
			List list = DataBaseUtil.queryToDynaList(ps);
			if (list.size() > 0) {
				return (BasicDynaBean) list.get(0);
			} else {
				return null;
			}
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_DOC_CATEGORIES = "SELECT cat_id,cat_name  FROM  category_type_master WHERE status='A'";

	public List getDocCategory() throws SQLException {
		List l = null;
		PreparedStatement ps = null;
		Connection con = DataBaseUtil.getReadOnlyConnection();
		ps = con.prepareStatement(GET_DOC_CATEGORIES);

		l = DataBaseUtil.queryToArrayList(ps);
		ps.close();
		con.close();

		return l;
	}

/*	private String GET_DOCTORNAMES_DEPARTMENTNAMES = " SELECT doctor_id, doctor_name, dept_id, ot_doctor_flag,dept_name FROM doctors d "
			+ " JOIN department using (dept_id) WHERE d.status = 'A' and d.schedule=true @ order by doctor_name";
*/
	public  List getSchedulableDoctorDepartmentNames(Integer centerid)
			throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		
		String GET_DOCTORNAMES_DEPARTMENTNAMES = " SELECT distinct d.doctor_id, d.doctor_name, d.dept_id, d.ot_doctor_flag,dep.dept_name FROM doctors d "
				+ " JOIN doctor_center_master  dcm ON(d.doctor_id=dcm.doctor_id)"
				+ " JOIN department dep using (dept_id) WHERE d.status = 'A' and dcm.status='A' and d.schedule=true " ;
		try {
			Integer resCenterId = null;
			if(centerid != null)
				resCenterId = centerid;
			else
				resCenterId = RequestContext.getCenterId();
			
			int max_center_inc_default = (Integer)GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default");
	
			if(max_center_inc_default > 1) {
				if(resCenterId != 0) {
					GET_DOCTORNAMES_DEPARTMENTNAMES = GET_DOCTORNAMES_DEPARTMENTNAMES + " AND center_id IN (0, ?) ORDER BY doctor_name";
					ps = con.prepareStatement(GET_DOCTORNAMES_DEPARTMENTNAMES);
					ps.setInt(1, resCenterId);
				} else {
					GET_DOCTORNAMES_DEPARTMENTNAMES = GET_DOCTORNAMES_DEPARTMENTNAMES + " ORDER BY doctor_name";
					ps = con.prepareStatement(GET_DOCTORNAMES_DEPARTMENTNAMES);
				}
			} else {
				GET_DOCTORNAMES_DEPARTMENTNAMES = GET_DOCTORNAMES_DEPARTMENTNAMES + " ORDER BY doctor_name";
				ps = con.prepareStatement(GET_DOCTORNAMES_DEPARTMENTNAMES);
			}
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		
	}

	public static final String docCharges = "SELECT doctor_id, doctor_name, dept_id as dept_name, ot_doctor_flag "
			+ " FROM doctors WHERE STATUS='A' AND doctor_type!='REFERRAL' ORDER BY doctor_name ";

	public static List getDoctorsandCharges() throws SQLException {

		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(docCharges);
			return DataBaseUtil.queryToArrayList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static final String docCharge = "SELECT doctor_id, doctor_name, dept_id as dept_name"
			+ " FROM doctors WHERE STATUS='A' AND doctor_type!='REFERRAL' ORDER BY doctor_name ";

	public static String getDoctorsandCharge() throws SQLException {

		Connection con = null;
		String arrDoctsXmlContent = null;
		con = DataBaseUtil.getReadOnlyConnection();
		arrDoctsXmlContent = DataBaseUtil.getXmlContentWithNoChild(docCharges,
				"DOCTSID");
		con.close();
		return arrDoctsXmlContent;
	}

	private static final String GET_DOCTOR_DEPT_QUERY =
		" SELECT d.doctor_id, d.doctor_name, d.dept_id, dept.dept_name, d.service_sub_group_id, " +
		"  dopc.op_charge, dopc.op_charge_discount, " +
		"  dopc.op_revisit_charge, dopc.op_revisit_discount as op_revisit_charge_discount, " +
		"  dopc.op_oddhr_charge, dopc.op_oddhr_charge_discount, " +
		"  dopc.private_cons_charge, dopc.private_cons_discount as private_cons_charge_discount, " +
		"  dopc.private_cons_revisit_charge, dopc.private_revisit_discount as private_cons_revisit_charge_discount, " +
		"  dc.doctor_ip_charge, dc.doctor_ip_charge_discount, " +
		"  dc.night_ip_charge, dc.night_ip_charge_discount, " +
		"  dc.ward_ip_charge, dc.ward_ip_charge_discount, " +
		"  dc.ot_charge, dc.ot_charge_discount, " +
		"  dc.co_surgeon_charge, dc.co_surgeon_charge_discount, " +
		"  dc.assnt_surgeon_charge, dc.assnt_surgeon_charge_discount, " +
		"  d.ot_doctor_flag " +
		" FROM doctors d" +
		" JOIN department dept ON (dept.dept_id = d.dept_id)" +
		" JOIN doctor_consultation_charge dc ON (dc.doctor_name = d.doctor_id)" +
		" JOIN doctor_op_consultation_charge dopc ON (dopc.doctor_id = d.doctor_id) ";

	public static BasicDynaBean getDoctorCharges(String doctorId, String orgId, String bedType)
		throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		BasicDynaBean doctorCharges = null;
		List dynalist = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_DOCTOR_DEPT_QUERY
							+ "  WHERE dc.bed_type=? AND dc.organization=? "
							+ "  AND dopc.org_id=? AND d.doctor_id=? ORDER BY doctor_name");
			ps.setString(1, bedType);
			ps.setString(2, orgId);
			ps.setString(3, orgId);
			ps.setString(4, doctorId);

			dynalist = DataBaseUtil.queryToDynaList(ps);
			if (dynalist.size() > 0) {
				doctorCharges = (BasicDynaBean) DataBaseUtil.queryToDynaList(ps).get(0);
			} else {
				ps = con.prepareStatement(GET_DOCTOR_DEPT_QUERY
								+ "  WHERE dc.bed_type='GENERAL' AND dc.organization='ORG0001' "
								+ "  AND dopc.org_id='ORG0001' AND d.doctor_id=? ORDER BY doctor_name");
				ps.setString(1, doctorId);
				dynalist = DataBaseUtil.queryToDynaList(ps);
				if (dynalist.size() > 0) {
					doctorCharges = (BasicDynaBean) DataBaseUtil.queryToDynaList(ps).get(0);
				}
			}
			return doctorCharges;
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_DOCTOR_DEPT_CHARGES = GET_DOCTOR_DEPT_QUERY
			+ " AND dc.bed_type=? AND dc.organization=? AND d.status='A' "
			+ " AND dopc.org_id=? "
			+ " UNION "
			+ GET_DOCTOR_DEPT_QUERY
			+ " AND dc.bed_type='GENERAL' AND dc.organization='ORG0001' AND d.status='A' "
			+ "       AND NOT EXISTS (SELECT doctor_name from doctor_consultation_charge WHERE "
			+ "                      doctor_name = d.doctor_id AND bed_type=? AND organization=?)"
			+ " AND dopc.org_id='ORG0001' "
			+ "       AND NOT EXISTS (SELECT doctor_id from  doctor_op_consultation_charge WHERE "
			+ "						doctor_id = d.doctor_id AND org_id = ?)"
			+ " ORDER BY doctor_name";

	public static List getAllDoctorDeptCharges(String bedtype, String orgid, boolean returnDynalist)
			throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		List list = null;
		try {
			con = DataBaseUtil.getConnection();
			logger.debug(GET_DOCTOR_DEPT_CHARGES);
			ps = con.prepareStatement(GET_DOCTOR_DEPT_CHARGES);
			ps.setString(1, bedtype);
			ps.setString(2, orgid);
			ps.setString(3, orgid);
			ps.setString(4, bedtype);
			ps.setString(5, orgid);
			ps.setString(6, orgid);

			if (returnDynalist)
				list = DataBaseUtil.queryToDynaList(ps);
			else
				list = DataBaseUtil.queryToArrayList(ps);
		} finally {
			if (ps != null)
				ps.close();
			if (con != null)
				con.close();
		}

		return list;
	}

	public static List getAllDoctorDeptCharges(String bedtype, String orgid) throws SQLException{
		return getAllDoctorDeptCharges(bedtype, orgid, false);
	}

	private static final String DOCTORS_AND_DEPTS  =
									" SELECT distinct doc.*, dept.* FROM doctors doc " +
									" JOIN doctor_center_master dcm ON (doc.doctor_id = dcm.doctor_id)"+
									" JOIN department dept ON (doc.dept_id=dept.dept_id)" +
									" WHERE doc.status='A' and dcm.status='A'";

	private static final String DOCTORS_AND_DEPTS_CENTERWISE  =
									" SELECT distinct doc.*, dept.* FROM doctors doc " +
									" JOIN doctor_center_master dcm ON (doc.doctor_id = dcm.doctor_id)"+
									" JOIN department dept ON (doc.dept_id=dept.dept_id)" +
									" WHERE doc.status='A' and dcm.status='A' and (dcm.center_id = ? OR dcm.center_id = 0)";
	public static List getDoctorsAndDepts() throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		int centerID = RequestContext.getCenterId();
		try {
			if(centerID != 0 && GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1) {
				ps = con.prepareStatement(DOCTORS_AND_DEPTS_CENTERWISE);
				ps.setInt(1, centerID);
			}else {
				ps = con.prepareStatement(DOCTORS_AND_DEPTS);
			}
				return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_OT_DOCTOR_CHARGES =
		"SELECT ot_charge as charge, ot_charge_discount as discount, " +
		"  co_surgeon_charge as cosurgeoncharge, co_surgeon_charge_discount, " +
		"  assnt_surgeon_charge as asst_charge, assnt_surgeon_charge_discount, " +
		"  d.doctor_id, d.doctor_name, d.dept_id " +
		" FROM doctor_consultation_charge dcc " +
		"  JOIN doctors d ON (d.doctor_id = dcc.doctor_name) " +
		" WHERE dcc.doctor_name=? and bed_type=? and organization=?";

	public BasicDynaBean getOTDoctorChargeBean(String doctorId, String bedType, String orgId)
		throws SQLException {
		return DataBaseUtil.queryToDynaBean(GET_OT_DOCTOR_CHARGES, new String[] {doctorId, bedType, orgId});
	}

	public static BasicDynaBean getOTDoctorChargesBean(String doctorId, String bedType, String orgId)
		throws SQLException{
		BasicDynaBean docchargebean = new DoctorMasterDAO().getOTDoctorChargeBean(doctorId, bedType, orgId);
		if (docchargebean == null)
			docchargebean = new DoctorMasterDAO().getOTDoctorChargeBean(doctorId, "GENERAL", "ORG0001");
		return docchargebean;
	}

	private String DUPLICATE_DOCTOR = "SELECT doctor_id,doctor_name FROM doctors WHERE doctor_name=?";

	public boolean checkDuplicateDoctor(Connection con, String referalName)
			throws SQLException {
		boolean duplicateDoctor = false;
		PreparedStatement ps = null;
		if (referalName.equals(""))
			return true;
		ps = con.prepareStatement(DUPLICATE_DOCTOR);
		ps.setString(1, referalName);
		ResultSet rs = ps.executeQuery();
		if (rs.next()) {
			duplicateDoctor = true;
		}
		return duplicateDoctor;
	}

	public String getDoctorNames() {
		String doctor = " SELECT  d.*,dcm.center_id FROM DOCTORS d " +
						"  JOIN doctor_center_master dcm ON (d.doctor_id = dcm.doctor_id) " +
						" WHERE d.STATUS='A' and dcm.status='A' and (dcm.center_id = ? OR dcm.center_id = 0) order by DOCTOR_NAME ";
		return doctor;
	}

	public static BasicDynaBean getDocChrg(String docId, String orgid) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			if (orgid.equals("")) {
				orgid = "ORG0001";
			}
			String docQuery = "select op_charge as doctor_op_charge,op_revisit_charge as sub_op_charge, cons_code, revisit_code "
					+ "from doctor_op_consultation_charge dop join organization_details od on od.org_id = dop.org_id "
					+ "where doctor_id=?"
					+ " and dop.org_id=?";
			
			ps = con.prepareStatement(docQuery);
			ps.setString(1, docId);
			ps.setString(2, orgid);
			
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static final String ALL_DOCTORS_AND_TECHNICIANS_LIST = "select d.doctor_name from doctors d";

	public static List getDoctors() throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(ALL_DOCTORS_AND_TECHNICIANS_LIST);
			return DataBaseUtil.queryToArrayList1(ps);
		} finally {
			if (ps != null)
				ps.close();
			if (con != null)
				con.close();
		}
	}

	private static final String ALL_DOCTORS="SELECT DOCTOR_ID,DOCTOR_NAME FROM DOCTORS";

	public static ArrayList getAllDoctorsList()throws SQLException{
		return DataBaseUtil.queryToArrayList(ALL_DOCTORS);
	}

	public List<String> getAllNames() throws SQLException {
		return new GenericDAO("doctors").getColumnList("doctor_name");
	}

	/*
	 * Search: returns a PagedList suitable for a dashboard type list
	 */
 	private static final String DOCTOR_FIELDS = "SELECT *";
 	private static final String DOCTOR_COUNT = "SELECT count(*)";
 /*	private static final String DOCTOR_FROM_TABLES = " FROM (SELECT d.doctor_name, d.doctor_id, d.status," +
 		" d.dept_id, d.payment_category, dept.dept_name, dod.org_id, dod.is_override, od.org_name,hcm.center_name,hcm.center_id " +
 		" FROM doctors d "+
 		" JOIN doctor_org_details dod on (dod.doctor_id = d.doctor_id) "+
 		" JOIN organization_details od on (od.org_id = dod.org_id) "+
 		" JOIN department dept ON (dept.dept_id = d.dept_id)" +
 		" JOIN hospital_center_master hcm ON(hcm.center_id=d.center_id))AS foo ";
 	*/
 	private static final String DOCTOR_FROM_TABLES = " FROM (SELECT distinct d.doctor_name, d.doctor_id, d.status ," +
		" d.dept_id, d.payment_category, dept.dept_name, dod.org_id, dod.is_override, od.org_name,dcm.status as doc_cen_status " +
		" FROM doctors d "+
		" JOIN doctor_org_details dod on (dod.doctor_id = d.doctor_id) "+
		" JOIN organization_details od on (od.org_id = dod.org_id) "+
		" JOIN department dept ON (dept.dept_id = d.dept_id)" +
		" JOIN doctor_center_master  dcm ON(d.doctor_id=dcm.doctor_id))AS foo ";

 	private static final String DOCTOR_FROM_CENTER_TABLES = " FROM (SELECT distinct d.doctor_name, d.doctor_id, d.status," +
		" d.dept_id, d.payment_category, dept.dept_name, dod.org_id, dod.is_override, od.org_name,dcm.center_id,dcm.status as doc_cen_status " +
		" FROM doctors d "+
		" JOIN doctor_org_details dod on (dod.doctor_id = d.doctor_id) "+
		" JOIN organization_details od on (od.org_id = dod.org_id) "+
		" JOIN department dept ON (dept.dept_id = d.dept_id)" +
		" JOIN doctor_center_master  dcm ON(d.doctor_id=dcm.doctor_id))AS foo ";

	public PagedList search(Map requestParams, Map pagingParams)
				throws ParseException, SQLException {

		Connection con = null;
		SearchQueryBuilder qb = null;
		List list = new ArrayList();
		int centerID = RequestContext.getCenterId();
		try {
			con = DataBaseUtil.getReadOnlyConnection();
				String[] centerId = (String[])requestParams.get("_center_id");
			if(centerId == null) {
				if(centerID != 0 && GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1) {
					qb = new SearchQueryBuilder(con, DOCTOR_FIELDS, DOCTOR_COUNT, DOCTOR_FROM_CENTER_TABLES, pagingParams);
					qb.addFilterFromParamMap(requestParams);
					list.add(RequestContext.getCenterId());
					list.add(0);
					qb.addFilter(qb.INTEGER, "center_id", "IN", list);
				} else{
					qb = new SearchQueryBuilder(con, DOCTOR_FIELDS, DOCTOR_COUNT, DOCTOR_FROM_TABLES, pagingParams);
					qb.addFilterFromParamMap(requestParams);
				}

			} else {
				qb = new SearchQueryBuilder(con, DOCTOR_FIELDS, DOCTOR_COUNT, DOCTOR_FROM_CENTER_TABLES, pagingParams);
			}
			//qb = new SearchQueryBuilder(con, DOCTOR_FIELDS, DOCTOR_COUNT, DOCTOR_FROM_TABLES, pagingParams);
			//String[] centerId = (String[])requestParams.get("_center_id");
			if(centerId != null) {
				if(centerId[0].equals("-1")) {
					qb = new SearchQueryBuilder(con, DOCTOR_FIELDS, DOCTOR_COUNT, DOCTOR_FROM_TABLES, pagingParams);
					//qb.addFilter(qb.INTEGER, "center_id", "!=", new Integer("-1"));
				} else {
					qb.addFilter(qb.INTEGER, "center_id", "=", Integer.parseInt(centerId[0]));
				}
			}
			qb.addFilter(qb.STRING, "doc_cen_status", "=", "A");
			qb.addFilterFromParamMap(requestParams);
			qb.addSecondarySort("doctor_id");
			qb.build();
			return qb.getMappedPagedList();
		} finally {
			qb.close();
			DataBaseUtil.closeConnections(con, null);
		}
	}

	/*
	 * Returns charges of all charge types for the given orgId, and list of Doctor IDs,
	 *
	 * This is used for displaying the charges for each Doctor in the main list master screen
	 */

	private static final String GET_ALL_CHARGES_FOR_OP =
		" SELECT doctor_id, org_id, op_charge, op_revisit_charge, private_cons_charge ,private_cons_revisit_charge" +
		" FROM doctor_op_consultation_charge " +
		" WHERE org_id=?";

	public List<BasicDynaBean> getAllOPChargesForOrg(String orgId, List<String> ids) throws SQLException {

		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			StringBuilder query = new StringBuilder(GET_ALL_CHARGES_FOR_OP);
			SearchQueryBuilder.addWhereFieldOpValue(true, query, "doctor_id", "IN", ids);

			ps = con.prepareStatement(query.toString());

			int i = 1;
			ps.setString(i++, orgId);
			if (ids != null) {
				for (String id : ids) {
					ps.setString(i++, id);
				}
			}
			return DataBaseUtil.queryToDynaList(ps);

		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	/*
	 * Returns charges of all charge types for the given orgId, and list of Doctor IDs,
	 * for all bed types
	 * This is used for displaying the charges for each bed in the main list master screen
	 */

	private static final String GET_ALL_CHARGES_FOR_IP =
		" SELECT doctor_name ,bed_type,  doctor_ip_charge, night_ip_charge, ward_ip_charge, ot_charge ,co_surgeon_charge,assnt_surgeon_charge" +
		" FROM doctor_consultation_charge " +
		" WHERE organization=?";

	public List<BasicDynaBean> getAllIPChargesForOrg(String orgId, List<String> ids) throws SQLException {

		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			StringBuilder query = new StringBuilder(GET_ALL_CHARGES_FOR_IP);
			SearchQueryBuilder.addWhereFieldOpValue(true, query, "doctor_name", "IN", ids);

			ps = con.prepareStatement(query.toString());

			int i = 1;
			ps.setString(i++, orgId);
			if (ids != null) {
				for (String id : ids) {
					ps.setString(i++, id);
				}
			}
			return DataBaseUtil.queryToDynaList(ps);

		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static String GET_ALL_CHARGE_ELEMENTS="SELECT d.doctor_id,d.doctor_name,d.dept_id,d.op_consultation_validity," +
			" de.dept_name ,doc.org_id,doc.op_charge,op_revisit_charge,private_cons_charge,private_cons_revisit_charge" +
			" ,op_charge_discount,op_revisit_discount,private_cons_discount,private_revisit_discount, " +
			" op_oddhr_charge, op_oddhr_charge_discount, od.org_name "+
			" FROM doctors d" +
			" JOIN department de on de.dept_id = d.dept_id" +
			" LEFT OUTER JOIN doctor_op_consultation_charge doc on doc.doctor_id = d.doctor_id" +
			" JOIN organization_details od on (od.org_id = doc.org_id) "+
			" WHERE d.doctor_id =? ";

	public BasicDynaBean getAllChargesForEdit(String orgId, String doctorId,String mode) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			StringBuilder query = new StringBuilder(GET_ALL_CHARGE_ELEMENTS);
			if(mode.equals("update"))
				SearchQueryBuilder.addWhereFieldOpValue(true, query, "doc.org_id", "=",orgId);

			ps = con.prepareStatement(query.toString());
			ps.setString(1, doctorId);
			if(mode.equals("update"))
				ps.setString(2, orgId);

			List list = DataBaseUtil.queryToDynaList(ps);
			if (list.size() > 0) {
				return (BasicDynaBean) list.get(0);
			}
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return null;
	}

	private static final String DOCTORS_NAMESAND_iDS = "SELECT doctor_name, doctor_id FROM doctors";

	public List getDoctorsNamesAndIds()throws SQLException {

		return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(DOCTORS_NAMESAND_iDS));

	}
	
	private static final String GET_DOCTORS_CENTER_LIST = "SELECT dcm.center_id,hcm.center_name, dcm.status" +
			" FROM  doctors d "+
			" JOIN doctor_center_master dcm ON(d.doctor_id=dcm.doctor_id)"+
			" JOIN hospital_center_master hcm ON (hcm.center_id=dcm.center_id)"+
			" where d.doctor_id=? ORDER BY center_name ";
	
	public static List<BasicDynaBean> getDoctorsCentersAndIds(String doctorId) throws SQLException {

		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		List<BasicDynaBean> docCenters = null;
		try {
			ps = con.prepareStatement(GET_DOCTORS_CENTER_LIST);
			ps.setString(1, doctorId);
			docCenters = DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return docCenters;
	}

/*	private static String GET_DOCTOR_DETAILS="SELECT doctor_id,doctor_name,specialization,doctor_address, " +
					"doctor_type,doctor_mobile,doctor_mail_id,op_consultation_validity, " +
					"d.status,dep.dept_name,ot_doctor_flag,schedule,qualification, "+
					"registration_no,res_phone,clinic_phone,payment_eligible,doctor_license_number," +
					"allowed_revisit_count,custom_field1_value,custom_field2_value," +
					"custom_field3_value,custom_field4_value,custom_field5_value # " +
				"FROM doctors d " +
				"JOIN hospital_center_master hcm ON(hcm.center_id = d.center_id)" +
				"JOIN department dep using(dept_id) WHERE d.status='A' ORDER BY doctor_id";
*/
	private static String GET_DOCTOR_DETAILS="SELECT d.doctor_id,d.doctor_name,d.specialization,d.doctor_address, " +
				"d.doctor_type,d.doctor_mobile,d.doctor_mail_id,d.op_consultation_validity, " +
				"d.status,dep.dept_name,d.prescribe_by_favourites,d.ot_doctor_flag,d.qualification, "+
				"d.registration_no,d.res_phone,d.clinic_phone,d.payment_eligible,d.doctor_license_number," +
				"d.allowed_revisit_count,d.custom_field1_value,d.custom_field2_value," +
				"d.custom_field3_value,d.custom_field4_value,d.custom_field5_value,pt.practitioner_name, " +
			" d.send_feedback_sms,d.scheduleable_by FROM doctors d " +
		//	"JOIN doctor_center_master dcm ON(d.doctor_id=dcm.doctor_id) "+
		//	"JOIN hospital_center_master hcm ON(hcm.center_id = dcm.center_id)" +
			"LEFT JOIN practitioner_types pt ON(d.practitioner_id = pt.practitioner_id) " +
			"JOIN department dep using(dept_id) ORDER BY doctor_id";


	public List<BasicDynaBean> getDoctorDetails() throws SQLException {

		List doctorList=null;
		PreparedStatement ps=null;
		Connection con=null;
		try {
			con=DataBaseUtil.getReadOnlyConnection();
			con.setAutoCommit(false);
			 // commented out because of Bug#46143
		/*	if ((Integer)(GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")) > 1) {
				GET_DOCTOR_DETAILS = GET_DOCTOR_DETAILS.replaceAll("#", ",center_name");
			} else {
				GET_DOCTOR_DETAILS = GET_DOCTOR_DETAILS.replaceAll("#", " ");
			}*/
			ps=con.prepareStatement(GET_DOCTOR_DETAILS);
			doctorList = DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}

		return doctorList;
	}

	private static String GET_OP_CHARGE_DETAILS ="SELECT * FROM doctor_op_consultation_charge WHERE doctor_id = ? AND org_id = ? " ;

	public static BasicDynaBean getDoctorOPChargeDetails(String doctorId, String orgId) throws SQLException{
		return DataBaseUtil.queryToDynaBean(GET_OP_CHARGE_DETAILS, new Object[]{doctorId, orgId});
	}

	private static String GET_DOC_ID = "SELECT doctor_id FROM doctors WHERE doctor_name = ? AND dept_id = ?";

	public static String getDoctorID(Object docName, Object deptId)throws SQLException {

		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			pstmt = con.prepareStatement(GET_DOC_ID);
			pstmt.setObject(1, docName);
			pstmt.setObject(2, deptId);
			rs = pstmt.executeQuery();
			if (rs.next())
				return rs.getString(1);
			else
				return null;

		} finally {
			DataBaseUtil.closeConnections(con, pstmt);
		}

	}

	/*
	 * Retrieve a list of doctors and their departments as an ArrayList of
	 * Strings
	 */
	/*private static final String GET_DOCTOR_DEPARTMENTS =
		" SELECT doctor_id, doctor_name, ot_doctor_flag, op_consultation_validity, allowed_revisit_count," +
		" d.dept_id, dep.dept_name, ip_discharge_consultation_validity, ip_discharge_consultation_count, practition_type  " +
		" FROM doctors d " +
		"  JOIN department dep ON (d.dept_id = dep.dept_id) " +
		" WHERE d.status = 'A' AND (d.center_id=? OR d.center_id = 0) order by doctor_name";
	 */
	private static final String GET_DOCTOR_DEPARTMENTS =
			" SELECT distinct d.doctor_id, d.doctor_name, d.ot_doctor_flag, d.op_consultation_validity, d.allowed_revisit_count," +
			" d.dept_id, dep.dept_name, d.ip_discharge_consultation_validity, d.ip_discharge_consultation_count, d.practition_type  " +
			" FROM doctors d " +
			"  JOIN doctor_center_master dcm ON(d.doctor_id=dcm.doctor_id) "+
			"  JOIN department dep ON (d.dept_id = dep.dept_id) " +
			" WHERE d.status = 'A' AND  dcm.status = 'A' order by doctor_name";
	private static final String GET_DOCTOR_DEPARTMENTS_CENTERWISE =
			" SELECT distinct d.doctor_id, d.doctor_name, d.ot_doctor_flag, d.op_consultation_validity, d.allowed_revisit_count," +
			" d.dept_id, dep.dept_name, d.ip_discharge_consultation_validity, d.ip_discharge_consultation_count, d.practition_type  " +
			" FROM doctors d " +
			"  JOIN doctor_center_master dcm ON(d.doctor_id=dcm.doctor_id) "+
			"  JOIN department dep ON (d.dept_id = dep.dept_id) " +
			" WHERE d.status = 'A' AND (dcm.center_id=? OR dcm.center_id = 0) and dcm.status = 'A' order by doctor_name";

	public static List getDoctorDepartmentsDynaList() throws SQLException {
		int centerId = RequestContext.getCenterId();
		return getDoctorDepartmentsDynaList(centerId);
	}

	public static List getDoctorDepartmentsDynaList(int centerId) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			if (centerId != 0 && GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1) {
				ps = con.prepareStatement(GET_DOCTOR_DEPARTMENTS_CENTERWISE);
				ps.setInt(1, centerId);
			} else {
				ps = con.prepareStatement(GET_DOCTOR_DEPARTMENTS);
			}
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	public static List getDoctorDepartmentsDynaList(Connection con, int centerId) throws SQLException {
    PreparedStatement ps = null;
    try {
      if (centerId != 0 && GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1) {
        ps = con.prepareStatement(GET_DOCTOR_DEPARTMENTS_CENTERWISE);
        ps.setInt(1, centerId);
      } else {
        ps = con.prepareStatement(GET_DOCTOR_DEPARTMENTS);
      }
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

	// TO DO  this is full confusion..
	private static final String GET_SCHEDULER_WEEKVIEW_DOCTOR = " SELECT doctor_id as resource_id, doctor_name as resource_name, dept_id as resource_dept, "
		+ " ot_doctor_flag,dept_name FROM doctors d "
		+ " JOIN department using (dept_id) WHERE d.status = 'A' and doctor_id = ?";
		//+ " and d.schedule=true order by doctor_name";

	public static BasicDynaBean getDefaultDoctorDeptBean(String docId) throws SQLException {
		return DataBaseUtil.queryToDynaBean(GET_SCHEDULER_WEEKVIEW_DOCTOR, docId);
	}

	private static final String GET_DOCTOR_LICENSE_NO = "SELECT * FROM doctors WHERE doctor_license_number=?";

	public static boolean getDoctorLicenseNo(String licenseNo) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean flag = false;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_DOCTOR_LICENSE_NO);
			ps.setString(1, licenseNo);
			rs = ps.executeQuery();
			if(rs.next()) {
				flag = true;
			}
		} finally {
			DataBaseUtil.closeConnections(con, ps, rs);
		}
		return flag;
	}


	public static final String GET_DOCTOR_PAYMENT_BEAN = " SELECT * FROM (SELECT doctor_id, doctor_name, specialization, doctor_type, dept_id, ot_doctor_flag, consulting_doctor_flag, "
			+ "  qualification, payment_category::varchar, custom_field1_value, "
			+ " custom_field2_value, custom_field3_value, custom_field4_value, custom_field5_value, payment_eligible "
			+ " FROM "
			+ " doctors "
			+ " UNION "
			+ " SELECT referal_no as doctor_id, referal_name as doctor_name, null as specialization, null as doctor_type,  "
			+ " null as dept_id,  null as ot_doctor_flag, null as consulting_doctor_flag, null as qualification,  "
			+ " payment_category::varchar, null AS custom_field1_value, null AS custom_field2_value, null AS custom_field3_value,  "
			+ " null AS custom_field4_value,null AS  custom_field5_value, payment_eligible "
			+ " FROM referral) AS foo where doctor_id = ? ";

	public static BasicDynaBean getDoctorPaymentBean(String doctorId) throws SQLException, ParseException {

		Connection con = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_DOCTOR_PAYMENT_BEAN);
			ps.setString(1, doctorId);

			List list = DataBaseUtil.queryToDynaList(ps);
			if (list.size() > 0) {
				return (BasicDynaBean) list.get(0);
			} else {
				return null;
			}
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}

	}

	/*public static final String GET_DOCTORS = "SELECT d.*,hcm.center_name FROM doctors d " +
			"	JOIN hospital_center_master hcm ON(d.center_id=hcm.center_id) " +
			"   WHERE doctor_id = ?";*/
	public static final String GET_DOCTORS = "SELECT d.*, pt.practitioner_name FROM doctors d" + 
				" LEFT JOIN practitioner_types pt ON(d.practitioner_id = pt.practitioner_id) where doctor_id=?";

	public static BasicDynaBean getDoctorDetails(String doctorId) throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_DOCTORS);
			ps.setString(1, doctorId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	public static final String GET_DOCTOR_CENTER = "SELECT d.*, dcm.status_on_practo FROM doctors d JOIN doctor_center_master dcm ON (dcm.doctor_id = d.doctor_id) "
			+ " WHERE d.doctor_id = ? AND dcm.center_id = ?";

	public static BasicDynaBean getDoctorDetails(String doctorId, int centerId) throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_DOCTOR_CENTER);
			int index = 1;
			ps.setString(index++, doctorId);
			ps.setInt(index++, centerId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_DOCTOR_FAVOURITES_CEN = "SELECT d.*,dcm.center_id FROM doctors d " +
			"	JOIN doctor_center_master dcm ON(d.doctor_id=dcm.doctor_id) " +
			"   WHERE d.doctor_id = ? and dcm.status='A' and d.status='A' ";


	public static BasicDynaBean getDoctorFavouriteCen(String doctorId) throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_DOCTOR_FAVOURITES_CEN);
			ps.setString(1, doctorId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	
	private static String GET_ALL_DOCTOR = " select doctor_name from doctors where status='A'  order by doctor_name " ;
	private static String GET_CENTER_WISE_DOCTORS = " select distinct doctor_name from doctors d"
				+ " LEFT JOIN doctor_center_master dcm ON(d.doctor_id=dcm.doctor_id) "
				+ " WHERE d.status = 'A' AND (dcm.center_id=? OR dcm.center_id = 0) and dcm.status = 'A' order by doctor_name";
	
	public static List getDoctorName() {
	PreparedStatement ps = null;
	ArrayList doctorNameList = null;
	Connection con = null;
	Integer centerID = RequestContext.getCenterId();

	try {
			con = DataBaseUtil.getConnection();
			if(centerID!=0 && GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1) {
				ps = con.prepareStatement(GET_CENTER_WISE_DOCTORS);
				ps.setInt(1, centerID);
			} else {
				ps = con.prepareStatement(GET_ALL_DOCTOR);
			}
			doctorNameList = DataBaseUtil.queryToArrayList1(ps);
		} catch (SQLException e) {
			logger.debug(e.toString());
		} finally {
			DataBaseUtil.closeConnections(con, ps);
	}

		return doctorNameList;
	}

	private static String GET_EXISTING_DOCTOR_NAME = "SELECT doctor_name FROM doctors where registration_no = ? AND doctor_id != ?";
	
	@SuppressWarnings("unchecked")
  public List<BasicDynaBean> getExistingDoctorName(String doctorId, String regNo) {
	  PreparedStatement ps = null;
	  List<BasicDynaBean> doctorName = new ArrayList<>();
	  Connection con = null;
	  try {
	    con = DataBaseUtil.getConnection();
	    ps = con.prepareStatement(GET_EXISTING_DOCTOR_NAME);
	    ps.setString(1, regNo);
	    ps.setString(2, doctorId);
	    doctorName = DataBaseUtil.queryToDynaList(ps);
	  } catch (SQLException e) {
	    logger.debug(e.toString());
	  }
	  return doctorName;
	}
}
