package com.insta.hms.diagnosticmodule.prescribetest;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public interface DiagnoDAOIF {

	public String getPatientdetails(String mrno) throws SQLException, ParseException;
	public String getpatients();

	public ArrayList getdoctorslist();
	public ArrayList getDeapartmentlist();
	public ArrayList getdiagnosticdepartmentlist();

	public String gettestcharge(String testid,String selectedorgname,String priority);

	 public String getprevtestdetails(String mrno);
	 public List getprevtestdetailsList(String mrno,String module);
	 public ArrayList getPatientReports(String mrno, String patId) throws SQLException;
	 public ArrayList getComments(String pescId) throws SQLException;
	 public String getPatientIds(String mrno) throws SQLException;

	 public ArrayList getTestChargeForOrganizationAndBedType(String strTestId, String strOrgId,
			 String strBedType, String strPriority) throws SQLException;

}
