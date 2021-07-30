package com.insta.hms.diagnosticmodule.prescribetest;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public interface DiagnoBOIF {

	public String getpatients();
	public String getPatientdetails(String mrno) throws SQLException, ParseException;
	public ArrayList getdoctorslist();
	public ArrayList getDeapartmentlist();
	public ArrayList getdiagnosticdepartmentlist();
	public String gettestcharge(String testid,String selectedorgname,String priority);
	public String getprevtestdetails(String mrno);
	public String getPatientId(String mrno);
	public ArrayList getPatientReports(String mrno, String patId) throws SQLException;
	public ArrayList getComments(String pescId) throws SQLException;
	public String getPatientIds(String mrno) throws SQLException;
	public List getprevtestdetailsList(String mrno,String module);

}
