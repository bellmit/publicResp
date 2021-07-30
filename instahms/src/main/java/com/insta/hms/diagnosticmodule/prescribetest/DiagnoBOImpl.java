package com.insta.hms.diagnosticmodule.prescribetest;

import com.bob.hms.common.DataBaseUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class DiagnoBOImpl implements DiagnoBOIF{
	DiagnoDAOIF dao = DiagnoFactory.getDiagnoDAO();
	static Logger logger = LoggerFactory.getLogger(DiagnoBOImpl.class);

	public String getpatients() {

		return dao.getpatients();
	}

	public String getPatientdetails(String mrno) throws SQLException, ParseException{

		return dao.getPatientdetails(mrno);
	}

	public ArrayList getDeapartmentlist() {
		return dao.getDeapartmentlist();
	}

	public ArrayList getdiagnosticdepartmentlist() {
			return dao.getdiagnosticdepartmentlist();
	}

	public ArrayList getdoctorslist() {
		return dao.getdoctorslist();

	}


	public String gettestcharge(String testid, String selectedorgname, String priority) {

		return dao.gettestcharge(testid,selectedorgname,priority);
	}

	public String getprevtestdetails(String mrno) {

		return dao.getprevtestdetails(mrno);
	}



	public String getPatientId(String mrno) {
		try {
      return DataBaseUtil.getStringValueFromDb(
          "select patient_id from patient_registration where mr_no=? and status='A'", mrno);
    } catch (SQLException e) {
      return null;
    }
	}



	public ArrayList getPatientReports(String mrno, String patId) throws SQLException{

		return dao.getPatientReports(mrno,patId);
	}



	public ArrayList getComments(String pescId) throws SQLException{

		return dao.getComments(pescId);
	}
	public String getPatientIds(String mrno) throws SQLException{
		return dao.getPatientIds(mrno);
	}
	public List getprevtestdetailsList(String mrno,String module){
		return dao.getprevtestdetailsList(mrno,module);
	}

}
