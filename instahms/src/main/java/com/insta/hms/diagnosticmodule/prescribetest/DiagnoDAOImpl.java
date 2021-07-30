package com.insta.hms.diagnosticmodule.prescribetest;

import com.bob.hms.common.Constants;
import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class DiagnoDAOImpl implements DiagnoDAOIF{
	static Logger log = LoggerFactory.getLogger(DiagnoDAOImpl.class);
	protected static final String EXCEPTION_OCCURRED= "Exception occurred : ";
	
	Connection con = null;
	public String getpatients() {
		PreparedStatement ps = null;
		String patients = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement("select * from patient_registration where status='A'");
			patients = DataBaseUtil.getXmlContentWithNoChild(ps, "XMLMRNO");
		}catch(Exception exe){
		  log.error(EXCEPTION_OCCURRED+ exe.getMessage());
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}return patients;
	}


	public String getPatientdetails(String mrno) throws SQLException, ParseException{

		PreparedStatement ps = null;
		String patientDetails = null;
		try{
			con = DataBaseUtil.getConnection();

			String mrnoquery = "SELECT pd.mr_no, pd.patient_name, get_patient_age(pd.dateofbirth, pd.expected_dob) as patient_age, " +
				" get_patient_age_in(pd.dateofbirth, pd.expected_dob) as patient_age_in, " +
				" pd.patient_gender,  pd.patient_care_oftext, " +
				" pd.patient_address, pd.patient_city, pd.patient_state, pd.patient_phone," +
				" pr.org_id, pd.salutation,  pd.last_name, " +
				" pd.dateofbirth, pd.country,  pd.cflag, pd.patient_careof_address, " +
				" pd.relation,  pd.religion, pd.occupation, " +
				" pd.user_name, pd.oldmrno, pd.bloodgroup, pd.emr_access,pd.remarks,pr.reg_date,doc.doctor_name, " +
				" pr.patient_id,pr.dept_name,od.org_name,pr.visit_type,doc.doctor_id,od.org_id,pr.bed_type,d.dept_name FROM " +
				" patient_details pd left join patient_registration pr on pr.mr_no=pd.mr_no and pr.status='A' " +
				" left join doctors doc on pr.doctor = doc.doctor_id " +
				" join department d on(pr.dept_name=d.dept_id) " +
				" left join organization_details od on od.org_id = pr.org_id " +
				" where pd.mr_no=? order by pr.reg_date desc,pr.reg_time desc limit 1 ;";
			ps = con.prepareStatement(mrnoquery);
			ps.setString(1, mrno);
			patientDetails = DataBaseUtil.getXmlContentWithNoChild(ps, "PATIENTDETAILS");
		} finally{
			DataBaseUtil.closeConnections(con, ps);
		}return patientDetails;
	}


	public String getprevtestdetails(String mrno){
		PreparedStatement ps = null;
		String testDetails = null;
		try{
			con = DataBaseUtil.getConnection();
			String mrnoquery = "select tp.test_name, to_char(tp.pres_date,'dd-mon-yyyy') as pre_date, to_char(tp.pres_time,'hh:mi:ss AM')as pres_time,"+
						" tp.conducted,doc.doctor_name,dd.ddept_name,"+
						" '' as reportvalue,tp.test_id,tp.priority  from tests_prescribed tp left join doctors doc"+
						" on tp.pres_doctor = doc.doctor_id" +
						" join diagnostics d on d.test_id = tp.test_id"+
						" join diagnostics_departments dd on dd.ddept_id = d.ddept_id"+
						" where mr_no=?";
			ps = con.prepareStatement(mrnoquery);
			ps.setString(1, mrno);

			testDetails = DataBaseUtil.getXmlContentWithNoChild(ps, "PREVIOUSTESTDETAILS");

		}catch(Exception exe){
		  log.error(EXCEPTION_OCCURRED+ exe.getMessage());
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}return testDetails;
	}

private static final String GET_PREV_DETAILS = "SELECT d.test_name, TO_CHAR(tp.pres_date,'dd-MM-yyyy') AS pre_date, " +
		"  TO_CHAR(tp.pres_date,'hh:mi:ss AM') AS pres_time," +
		"  (CASE WHEN tp.conducted = 'N' THEN 'Not Conducted'  " +
		"		 WHEN tp.conducted = 'C' THEN 'Conducted'" +
		"    	 WHEN tp.conducted = 'X' THEN 'Cancelled' " +
		"		 WHEN tp.conducted = 'P' THEN 'Partially Conducted' " +
		"        WHEN tp.conducted = 'V' THEN 'Validated' " +
		"        WHEN tp.conducted = 'MA' THEN 'Patient Arrived' " +
		"        WHEN tp.conducted = 'CC' THEN 'Conduction Completed' " +
		"        WHEN tp.conducted = 'TS' THEN 'Scheduled for Transcriptionist' " +
		"        WHEN tp.conducted = 'CR' THEN 'Change Required' " +
		"        WHEN tp.conducted = 'S' THEN 'Signed off'" +
		"        WHEN tp.conducted = 'RP' THEN 'Revision in progress' " +
		"        WHEN tp.conducted = 'RC' THEN 'Revision is completed'" +
		"        WHEN tp.conducted = 'RV' THEN 'Revision validated'" +
		"        WHEN tp.conducted = 'RAS' THEN 'Reconducted after signoff'" +
		"        WHEN tp.conducted = 'RBS' THEN 'Reconducted before signoff' end) AS status," +
		" tp.conducted,doc.doctor_name,dd.ddept_name, tp.test_id,tp.priority,tp.prescribed_id,tp.prescription_type," +
		" coalesce(signed_off,'N') as signed_off, d.sample_needed, " +
		"(SELECT COUNT(*) FROM tests_prescribed tp1 WHERE tp.prescribed_id = tp1.reference_pres AND tp1.conducted = 'N') " +
		"AS reconduct_count, tp.re_conduction,tp.sample_no,tp.reconducted_reason, dom.outsource_dest_type " +
		" FROM tests_prescribed tp " +
		" LEFT JOIN test_visit_reports tvr ON tvr.patient_id = tp.pat_id and tvr.report_id = tp.report_id " +
		" LEFT JOIN doctors doc ON tp.pres_doctor = doc.doctor_id " +
		" LEFT OUTER JOIN diagnostics d ON d.test_id = tp.test_id" +
		" LEFT JOIN diag_outsource_master dom ON(tp.outsource_dest_id = dom.outsource_dest_id)" +
		" JOIN diagnostics_departments dd ON dd.ddept_id = d.ddept_id  " ;



public List getprevtestdetailsList(String mr_no,String module){
	PreparedStatement ps = null;
	List testDetails = null;
	try{
		con = DataBaseUtil.getConnection();
		StringBuffer mrnoquery = new StringBuffer(GET_PREV_DETAILS);

		if(module.equalsIgnoreCase("DEP_RAD"))mrnoquery.append(" and dd.category='DEP_RAD'");
		else if(module.equalsIgnoreCase("DEP_LAB"))mrnoquery.append(" and dd.category='DEP_LAB'");
		mrnoquery.append(" where mr_no=? and d.conduction_applicable=true");
		mrnoquery.append(" order by tp.conducted");

		ps = con.prepareStatement(mrnoquery.toString());
		ps.setString(1, mr_no);
		testDetails = DataBaseUtil.queryToArrayList(ps);

	}catch(Exception exe){
	  log.error(EXCEPTION_OCCURRED+ exe.getMessage());
	}finally{
		DataBaseUtil.closeConnections(con, ps);
	}return testDetails;
}
/**
 * Brings visit wise test details
 * @param pat_id
 * @param module
 * @return
 * @throws SQLException
 */
public List getCurrentVisitPrevTestDetailsList(String pat_id,String module,String[] presId,String[] reportId) throws SQLException{
	PreparedStatement ps = null;
	List testDetails = null;
	try{
		con = DataBaseUtil.getConnection();
		StringBuffer mrnoquery = new StringBuffer(GET_PREV_DETAILS);

		if(module.equalsIgnoreCase("DEP_RAD"))mrnoquery.append(" and dd.category='DEP_RAD'");
		else if(module.equalsIgnoreCase("DEP_LAB"))mrnoquery.append(" and dd.category='DEP_LAB'");
		mrnoquery.append(" where pat_id=? and d.conduction_applicable=true AND tp.conducted NOT IN  ('U','X','NRN','CRN' ) ");

		if ( presId != null ) {
			for ( int i = 0;i < presId.length;i++ ) {

				if ( presId[i].isEmpty() )
					break;

				if( i == 0 )
					mrnoquery.append(presId != null && presId.length > 0 ?"AND prescribed_id IN (" : "");

				mrnoquery.append(presId[i]+",");

			}
			if ( mrnoquery.toString().contains("prescribed_id IN (") ) {
				mrnoquery = new StringBuffer(mrnoquery.substring(0, mrnoquery.length()-1));
				mrnoquery.append(")");
			}
		}


		if( reportId != null && reportId.length > 0  && !reportId[0].equals("0") && !reportId[0].isEmpty()){

			mrnoquery.append(" AND tp.report_id IN (");

			for(String inValue : reportId){
				mrnoquery.append(inValue+",");
			}

			mrnoquery = new StringBuffer(mrnoquery.substring(0, mrnoquery.length()-1));
			mrnoquery.append(")");
		}

		mrnoquery.append(" order by tp.conducted");

		log.debug("***********"+mrnoquery);

		ps = con.prepareStatement(mrnoquery.toString());
		ps.setString(1, pat_id);
		testDetails = DataBaseUtil.queryToArrayList(ps);

	}finally{
		DataBaseUtil.closeConnections(con, ps);
	}return testDetails;
}
	public ArrayList getPrevTestDetailsArrList(String mrno){
		PreparedStatement ps = null;
		ArrayList testDetails = null;
		try{
			con = DataBaseUtil.getConnection();
			String mrnoquery = "select tp.test_name, to_char(tp.pres_date,'dd-mon-yyyy') as pre_date, to_char(tp.pres_time,'hh:mi:ss AM')as pres_time,"+
			" tp.conducted,doc.doctor_name,dd.ddept_name,"+
			" '' as reportvalue,tp.test_id,tp.priority  from tests_prescribed tp left join doctors doc"+
			" on tp.pres_doctor = doc.doctor_id"+
			" join diagnostics_departments dd on dd.ddept_id = tp.department"+
			" where mr_no=?";
			ps = con.prepareStatement(mrnoquery);
			ps.setString(1, mrno);
			testDetails = DataBaseUtil.queryToArrayList(ps);

		}catch(Exception exe){
			log.error(EXCEPTION_OCCURRED+ exe.getMessage());
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}return testDetails;
	}


	public ArrayList getDeapartmentlist() {
			PreparedStatement ps = null;
			ArrayList departmentList = null;
			try{
				con = DataBaseUtil.getConnection();
				ps = con.prepareStatement("SELECT * FROM DEPARTMENT WHERE STATUS='A' ORDER BY DEPT_NAME", ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
				departmentList = DataBaseUtil.queryToArrayList(ps);
			}catch(Exception exe){
			  log.error(EXCEPTION_OCCURRED+ exe.getMessage());
			}finally{
				DataBaseUtil.closeConnections(con, ps);
			}return departmentList;
	}

	public ArrayList getdiagnosticdepartmentlist() {
		PreparedStatement ps = null;
		ArrayList diagDepartment = null;
		try{
			con = DataBaseUtil.getConnection();
			String getdiagdeptlist = "SELECT ddept_id, ddept_name FROM diagnostics_departments where status='A' order by ddept_name";
			ps = con.prepareStatement(getdiagdeptlist, ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			diagDepartment = DataBaseUtil.queryToArrayList(ps);
		}catch(Exception exe){
		  log.error(EXCEPTION_OCCURRED+ exe.getMessage());
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}return diagDepartment;
	}


	public ArrayList getdoctorslist() {
		PreparedStatement ps = null;
		ArrayList doctorsList = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement("select doctor_id,doctor_name from doctors where status='A' order by doctor_name", ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			doctorsList = DataBaseUtil.queryToArrayList(ps);
		}catch(Exception exe){
		  log.error(EXCEPTION_OCCURRED+ exe.getMessage());
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}return doctorsList;
	}


  public static final String chargequery = "select charge from diagnostic_charges where test_id = ?"
      + " and org_name = ? AND BED_TYPE = ? AND priority = ?";

	public String gettestcharge(String testid, String orgId, String priority) {
		try {			
			return DataBaseUtil.getStringValueFromDb(chargequery, 
			    new Object[]{ testid, orgId, Constants.getConstantValue("BEDTYPE"), priority });
		} catch(SQLException ex) {
		  log.error(EXCEPTION_OCCURRED + ex.getMessage());
		}
		return null;
	}

	public ArrayList getDiagTestNames() {
		ArrayList labtestTypeDetails = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement("SELECT test_id,Test_Name||'-'||ddept_name as test_name, Test_Name as test, d.DDept_ID,ddept_name FROM Diagnostics d, " +
					"diagnostics_departments dd where test_id in(select test_id from diagnostic_charges) " +
					"and dd.ddept_id=d.ddept_id order by test_name");
			labtestTypeDetails=DataBaseUtil.queryToArrayList(ps);
		}catch(Exception exe){
		  log.error(EXCEPTION_OCCURRED+ exe.getMessage());
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		return labtestTypeDetails;
	}
	/**
	 * This method fetches  depends upon the module ,if diag module
	 * fetches only laboratory related tests
	 * if radiology fectes only radiology tests
	 * if from billing fetches all tests tests
	 * @return ArrayList of Test Names
	 */
	public List getTestNames(String module, String orgId,String sampleNeeded,int centerId){
		ArrayList radilogytest = null;
		PreparedStatement ps = null;
		StringBuffer teststring = new StringBuffer("SELECT  " +
				" CASE WHEN d.diag_code IS null OR d.diag_code='' THEN d.test_name||'-'||dd.ddept_name "+
				" ELSE d.test_name||'['|| d.diag_code ||']'||'-'||dd.ddept_name END AS test_name," +
				" d.test_name as test, d.test_id, d.diag_code, 'DIA' as type," +
				" d.ddept_id, dd.ddept_name, dcr.charge,dcr.discount,dd.category,st.sample_type_id," +
				" st.sample_type,d.conduction_applicable,d.sample_needed, " +
				" CASE WHEN is_outhouse_test(d.test_id,0) THEN 'O' ELSE 'I' END AS house_status," +
				" d.conducting_doc_mandatory, d.mandate_additional_info, d.additional_info_reqts "+
				" FROM diagnostics d " +
				" JOIN diagnostics_departments dd ON (dd.ddept_id=d.ddept_id AND d.STATUS='A')  " +
				" LEFT OUTER JOIN diagnostic_charges dcr ON (d.test_id = dcr.test_id AND dcr.org_name=?  AND dcr.bed_type = 'GENERAL' AND dcr.priority = 'R') " +
				" JOIN test_org_details tod ON (tod.test_id = dcr.test_id AND tod.org_id = dcr.org_name AND tod.applicable) " +
				" LEFT JOIN sample_type st ON (d.sample_type_id=st.sample_type_id)" );

		StringBuffer testStringForCenter = new StringBuffer("SELECT  " +
				" CASE WHEN d.diag_code IS null OR d.diag_code='' THEN d.test_name||'-'||dd.ddept_name "+
				" ELSE d.test_name||'['|| d.diag_code ||']'||'-'||dd.ddept_name END AS test_name," +
				" d.test_name as test, d.test_id, d.diag_code, 'DIA' as type," +
				" d.ddept_id, dd.ddept_name, dcr.charge,dcr.discount,dd.category,st.sample_type_id," +
				" st.sample_type,d.conduction_applicable,d.sample_needed, " +
				" CASE WHEN is_outhouse_test(d.test_id,?) THEN 'O' ELSE 'I' END AS house_status," +
				" d.conducting_doc_mandatory, d.mandate_additional_info, d.additional_info_reqts "+
				" FROM diagnostics d " +
				" JOIN diagnostics_departments dd ON (dd.ddept_id=d.ddept_id AND d.STATUS='A')  " +
				" LEFT OUTER JOIN diagnostic_charges dcr ON (d.test_id = dcr.test_id AND dcr.org_name=?  AND dcr.bed_type = 'GENERAL' AND dcr.priority = 'R') " +
				" JOIN test_org_details tod ON (tod.test_id = dcr.test_id AND tod.org_id = dcr.org_name AND tod.applicable) " +
				" LEFT JOIN sample_type st ON (d.sample_type_id=st.sample_type_id)" );
		try{
			con = DataBaseUtil.getConnection();
			int max_centers_inc_default = (Integer) GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default");
			StringBuffer query = max_centers_inc_default > 1 ? testStringForCenter : teststring;
			if(module.equalsIgnoreCase("DEP_LAB")&& sampleNeeded==null)query.append(" where d.is_prescribable AND dd.category='DEP_LAB' order by test_name");
			else if(module.equalsIgnoreCase("DEP_RAD")&& sampleNeeded==null)query.append(" where d.is_prescribable AND dd.category='DEP_RAD' order by test_name");
			else if(module.equalsIgnoreCase("DEP_LAB")&& sampleNeeded.equals("y"))query.append(" where d.is_prescribable AND dd.category='DEP_LAB' and d.sample_needed='y' order by test_name");
			else query.append(" order by test_name");
			ps = con.prepareStatement(query.toString());

			if(max_centers_inc_default > 1){
				ps.setInt(1, centerId);
				ps.setString(2, orgId);
			}else{
				ps.setString(1, orgId);
			}

			radilogytest=DataBaseUtil.queryToArrayList(ps);
		}catch(Exception exe){
		  log.error(EXCEPTION_OCCURRED+ exe.getMessage());
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		return radilogytest;
	}

	/**
	 * This method fetches  depends upon the module ,if diag module
	 * fetches only laboratory related tests
	 * if radiology fectes only radiology tests
	 * if from billing fetches all tests tests
	 * @return ArrayList of Test Names
	 */
	public List getTestAndPackageNames(String module, String orgId,String sampleNeeded, int centerId, String ageText) {
		ArrayList radilogytest = null;
		ArrayList packagesList = null;
		PreparedStatement ps = null;
		StringBuffer teststring = new StringBuffer("SELECT   CASE WHEN d.diag_code IS null OR d.diag_code='' THEN d.test_name||'-'||dd.ddept_name "+
					" ELSE d.test_name||'['|| d.diag_code ||']'||'-'||dd.ddept_name END AS test_name,d.test_name as test, d.test_id, "+
					" d.diag_code,d.ddept_id, dd.ddept_name, dcr.charge,dcr.discount,dd.category,st.sample_type_id, "+
					" st.sample_type,'DIA' as type,null as package_type,d.conduction_applicable,d.sample_needed,  "+
					" CASE WHEN is_outhouse_test(d.test_id,0) THEN 'O' ELSE 'I' END AS house_status, d.conducting_doc_mandatory, " +
					" d.mandate_additional_info, d.additional_info_reqts "+
					" FROM diagnostics d "+
					" JOIN diagnostics_departments dd ON(dd.ddept_id=d.ddept_id AND d.STATUS='A') "+
					" left outer JOIN diagnostic_charges dcr ON (d.test_id = dcr.test_id AND dcr.org_name=?  AND dcr.bed_type = 'GENERAL' AND dcr.priority = 'R') "+
					" JOIN test_org_details tod ON tod.test_id = dcr.test_id AND tod.org_id = dcr.org_name AND tod.applicable "+
					" LEFT JOIN sample_type st ON d.sample_type_id=st.sample_type_id " );

		StringBuffer testStringForCenter = new StringBuffer("SELECT   CASE WHEN d.diag_code IS null OR d.diag_code='' THEN d.test_name||'-'||dd.ddept_name "+
					" ELSE d.test_name||'['|| d.diag_code ||']'||'-'||dd.ddept_name END AS test_name,d.test_name as test, d.test_id, "+
					" d.diag_code,d.ddept_id, dd.ddept_name, dcr.charge,dcr.discount,dd.category,st.sample_type_id, "+
					" st.sample_type,'DIA' as type,null as package_type,d.conduction_applicable,d.sample_needed,  "+
					" CASE WHEN is_outhouse_test(d.test_id,?) THEN 'O' ELSE 'I' END AS house_status, d.conducting_doc_mandatory, " +
					" d.mandate_additional_info, d.additional_info_reqts "+
					" FROM diagnostics d "+
					" JOIN diagnostics_departments dd ON(dd.ddept_id=d.ddept_id AND d.STATUS='A') "+
					" left outer JOIN diagnostic_charges dcr ON (d.test_id = dcr.test_id AND dcr.org_name=?  AND dcr.bed_type = 'GENERAL' AND dcr.priority = 'R') "+
					" JOIN test_org_details tod ON tod.test_id = dcr.test_id AND tod.org_id = dcr.org_name AND tod.applicable "+
					" LEFT JOIN sample_type st ON d.sample_type_id=st.sample_type_id " );

		String packages = " SELECT pm.package_name AS test_name,pm.package_name AS test,pm.package_id::text AS test_id,null as diag_code, "+
					" null AS ddept_id,null AS ddept_name,0 AS charge ,0 as discount,null AS category,null AS sample_type_id, "+
					" null  AS sample_type,'PKG' AS type, 'P' as package_type, null AS  conduction_applicable, null as sample_needed, null as house_status," +
					" null AS conducting_doc_mandatory, 'N' as mandate_additional_info, '' as additional_info_reqts "+
					" FROM packages pm "+
					" JOIN pack_org_details pod using(package_id) "+
					" JOIN center_package_applicability pcm ON (pm.package_id=pcm.package_id AND pcm.status='A' AND (pcm.center_id=? or pcm.center_id=-1)) " +
					" JOIN package_sponsor_master psm ON (pm.package_id=psm.pack_id AND psm.status='A' AND (psm.tpa_id='0' OR psm.tpa_id = '-1'))" +
					" WHERE  pm.status='A'  AND pm.approval_status='A' AND pod.applicable='t' AND  pm.handover_to != 'S' AND "+
					" pod.org_id=? AND pm.visit_applicability='d' AND pm.package_id not in (SELECT package_id FROM package_contents  " +
					" WHERE charge_head = 'RTDIA' GROUP BY package_id) "
					+ 	"AND("
					+ " ( pm.min_age is null OR pm.max_age is null OR pm.age_unit is null)"
					+ " OR"
					+ " (('P'||pm.min_age||pm.age_unit)::interval <=  ?::interval AND ('P'||pm.max_age||pm.age_unit)"
					+ "::interval >=  ?::interval)"
					+ " )" +
					" UNION "
					+ " SELECT p.package_name AS test_name, p.package_name AS test, p.package_id::text AS test_id, null AS diag_code, "
					+ " null AS ddept_id, null AS ddept_name,0 AS charge ,0 as discount,null AS category,null AS sample_type_id, "
					+ " null  AS sample_type,'PKG' AS type, 'P' as package_type, null AS conduction_applicable, null as sample_needed, null as house_status,"
					+ " null AS conducting_doc_mandatory, 'N' as mandate_additional_info, '' as additional_info_reqts "
					+ " FROM packages p "
					+ " JOIN center_package_applicability cpa ON (p.package_id=cpa.package_id AND (cpa.center_id=? or cpa.center_id=-1)) "
					+ " WHERE p.status='A' AND p.package_id in (select package_id from package_contents where charge_head='LTDIA' and package_id in"
					+ "     (select package_id  from package_contents group by package_id having count(distinct(charge_head)) = 1) "
					+ "  group by package_id) "
					+ 	"AND("
					+ " ( p.min_age is null OR p.max_age is null OR p.age_unit is null)"
					+ " OR"
					+ " (('P'||p.min_age||p.age_unit)::interval <=  ?::interval AND ('P'||p.max_age||p.age_unit)"
					+ "::interval >=  ?::interval)"
					+ " )";
		try{
			con = DataBaseUtil.getConnection();
			int max_centers_inc_default = (Integer) GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default");
			StringBuffer query = max_centers_inc_default > 1 ? testStringForCenter : teststring;

			if(module.equalsIgnoreCase("DEP_LAB")&& sampleNeeded==null)query.append(" where d.is_prescribable AND dd.category='DEP_LAB' order by test_name");
			else if(module.equalsIgnoreCase("DEP_RAD")&& sampleNeeded==null)query.append(" where d.is_prescribable AND dd.category='DEP_RAD' order by test_name");
			else if(module.equalsIgnoreCase("DEP_LAB")&& sampleNeeded.equals("y"))query.append(" where d.is_prescribable AND dd.category='DEP_LAB' and d.sample_needed='y' order by test_name");
			else query.append(" ORDER BY test_name");
			ps = con.prepareStatement(query.toString());
			if(max_centers_inc_default > 1){
				ps.setInt(1, centerId);
				ps.setString(2, orgId);
			}else{
				ps.setString(1, orgId);
			}
			radilogytest=DataBaseUtil.queryToArrayList(ps);
			ps = con.prepareStatement(packages);
			ps.setInt(1, centerId);
			ps.setString(2, orgId);
			ps.setString(3, ageText);
			ps.setString(4, ageText);
			ps.setInt(5, centerId);
			ps.setString(6, ageText);
			ps.setString(7, ageText);

			packagesList = DataBaseUtil.queryToArrayList(ps);
			for(int i = 0;i<packagesList.size();i++){
				radilogytest.add(packagesList.get(i));
			}
		}catch(Exception exe){
		  log.error(EXCEPTION_OCCURRED+ exe.getMessage());
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		return radilogytest;
	}
	public ArrayList getDiagTestNamesWithCategory() throws SQLException {
		ArrayList LabtestTypeDetails = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement("SELECT test_id,Test_Name||' - ['||(case when dd.category='DEP_RAD' then 'Radiology' else 'Laboratory' end)||' / '||ddept_name||']' as test_name, Test_Name as test, d.DDept_ID,ddept_name FROM Diagnostics d, " +
					"diagnostics_departments dd where test_id in(select test_id from diagnostic_charges) " +
					"and dd.ddept_id=d.ddept_id order by test_name");
			LabtestTypeDetails=DataBaseUtil.queryToArrayList(ps);
		} finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		return LabtestTypeDetails;
	}

	public ArrayList getPatientReports(String mrno, String patId) throws SQLException{
		PreparedStatement ps = null;
		ArrayList list = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement("select mr_no, patient_id, tc.test_id,test_name, conducted_date, tc.prescribed_id, ddept_name, " +
					"tc.conducted_status from tests_conducted tc ,diagnostics dc,diagnostics_departments dd " +
					"where tc.test_id=dc.test_id and dd.ddept_id=dc.ddept_id and mr_no=? and patient_id=? and confirm_status='Y' order by patient_id");
			ps.setString(1, mrno);
			ps.setString(2, patId);
			list = DataBaseUtil.queryToArrayList(ps);
		}finally{
			if(ps != null){
				ps.close();
			}
			if(con != null){
				con.close();
			}
		}
		return list;
	}




	public ArrayList getComments(String pescId) throws SQLException{
		ArrayList list = null;
    con = DataBaseUtil.getConnection();
		try(PreparedStatement ps = con.prepareStatement("select td.comments,"
		    + " processed_by, designation from test_details td," 
				+ " test_confirmation tc"
				+ " where td.prescribed_id=tc.prescribed_id and tc.prescribed_id = ?")) {
			ps.setInt(1, Integer.parseInt(pescId));
			list = DataBaseUtil.queryToArrayList(ps);
		}finally{
				con.close();
			}
		return list;
	}


	public String getPatientIds(String mrno) throws SQLException{
		String list = null;
    con = DataBaseUtil.getConnection();
		try(PreparedStatement ps = con.prepareStatement("select patient_id,"
		    + " case when status='A' then 'ACTIVE' else 'INACTIVE' end as status"
		    + "  from patient_registration where mr_no = ?")) {
			ps.setString(1, mrno);
			list = DataBaseUtil.getXmlContentWithNoChild(ps, "VISIT");
		}finally{
				con.close();
		}
		return list;
	}
	public int getCountFromDiagTimeStamp()throws SQLException{
	  con = DataBaseUtil.getReadOnlyConnection();
		try(PreparedStatement ps = con.prepareStatement("SELECT * FROM DIAG_TEST_TIMESTAMP")) {
			return DataBaseUtil.getIntValueFromDb(ps);
		}
		finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	private static final String getTestChargeForOrganizationAndBedType = "SELECT " +
		" charge, item_code, discount " +
		" FROM diagnostic_charges dcr " +
		" JOIN test_org_details tod ON (tod.test_id=dcr.test_id AND dcr.org_name=tod.org_id AND applicable) "+
		" WHERE dcr.test_id=? and org_name=? and bed_type=? and priority=?";

	public ArrayList getTestChargeForOrganizationAndBedType(String strTestId, String strOrgId,
		String strBedType, String strPriority) throws SQLException {
		PreparedStatement ps = null;
		ArrayList charge = null;
		if (strOrgId.equals("")) {
			strOrgId = "ORG0001";
		}
		if (strBedType.equals("")) {
			strBedType = Constants.getConstantValue("BEDTYPE");
		}
		if (strPriority.equals("")) {
			strPriority = "R";
		}
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(getTestChargeForOrganizationAndBedType);
			ps.setString(1, strTestId);
			ps.setString(2, strOrgId);
			ps.setString(3, strBedType);
			ps.setString(4, strPriority);
			charge = DataBaseUtil.queryToArrayList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return charge;
	}

	public static boolean isAmended(int prescribedId)throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement("SELECT prescribed_id FROM tests_prescribed WHERE prescribed_id = ? " +
					"AND conducted IN ('RP','RC','RV')");
			ps.setInt(1, prescribedId);
			return DataBaseUtil.queryToDynaBean(ps) != null;

		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}
}
