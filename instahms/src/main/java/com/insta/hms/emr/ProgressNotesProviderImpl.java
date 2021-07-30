package com.insta.hms.emr;

import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.progress.PatientProgressDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProgressNotesProviderImpl implements EMRInterface{

	public List<EMRDoc> listVisitDocumentsForMrNo(String mrNo) throws Exception {

		List<BasicDynaBean> dynaList = new PatientProgressDAO().getProgressNtsListForEmr("forVisits", mrNo);
		return getEmrdocs(dynaList, mrNo);
	}

	public List<EMRDoc> listDocumentsByVisit(String visitId) throws Exception {
		List<BasicDynaBean> dynaList = new PatientProgressDAO().getProgressNtsListForVisitEmr(visitId); // HMS-19987 : Able to see progress notes in Visit EMR Screen.
		return getEmrdocs(dynaList, visitId);
		//return Collections.emptyList();
	}

	public List<EMRDoc> listDocumentsByMrno(String mrNo) throws Exception{
		List<BasicDynaBean> dynaList = new PatientProgressDAO().getProgressNtsListForEmr(null, mrNo);
		return getEmrdocs(dynaList, mrNo);
	}

	public byte[] getPDFBytes(String docid, int printId) throws Exception{
		return null;
	}

	private List<EMRDoc> getEmrdocs(List<BasicDynaBean> beans, String mrNO)throws SQLException {

		List<EMRDoc> emrDocs = new ArrayList<EMRDoc>();
		String doctorName = PatientProgressDAO.getDoctorName(mrNO);
		EMRDoc emrDoc = null;
		BasicDynaBean printPrefs = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT);
		
		for (BasicDynaBean bean : beans) {
			emrDoc = new EMRDoc();
			String visitId = (String) bean.get("visit_id");
			emrDoc.setDate((java.util.Date)bean.get("date_time"));
			emrDoc.setDisplayUrl("/patient/progress/print.do?method=getPrint&mr_no="+bean.get("mr_no")+"&patientId="+visitId);
			String docID = "mrNO=" + (String)bean.get("mr_no") + ":visitID=" + (visitId.equals("") ? "noVisit" : visitId);
			emrDoc.setDocid(docID);
			if (doctorName != null)
			emrDoc.setDoctor(doctorName);
			emrDoc.setProvider(EMRInterface.Provider.ProgressNotesProvider);
			emrDoc.setVisitid((String)bean.get("visit_id"));
			emrDoc.setUpdatedBy((String)bean.get("username"));
			emrDoc.setPdfSupported(true);
			emrDoc.setPrinterId((Integer)printPrefs.get("printer_id"));
			emrDoc.setAuthorized(true);
			emrDoc.setTitle("Progress Notes");
			emrDoc.setType("SYS_PROGRESS_NOTES");
			emrDoc.setVisitDate((java.util.Date)bean.get("reg_date"));

			emrDocs.add(emrDoc);
		}

		return emrDocs;
	}



}