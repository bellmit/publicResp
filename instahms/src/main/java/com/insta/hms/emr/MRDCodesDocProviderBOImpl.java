package com.insta.hms.emr;

import com.insta.hms.common.HtmlConverter;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.medicalrecorddepartment.MRDUpdateScreenBO;
import com.insta.hms.medicalrecorddepartment.MRDUpdateScreenDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;



public class MRDCodesDocProviderBOImpl implements EMRInterface{

	public List<EMRDoc> listDocumentsByMrno(String visitId)throws Exception {

		return null;
	}

	public List<EMRDoc> listVisitDocumentsForMrNo(String mrNo) throws Exception {
		return populateEMRDocs(MRDUpdateScreenDAO.fieldsForEmrdoc(null, mrNo, true));
	}

	public List<EMRDoc> listDocumentsByVisit(String visitId)throws Exception {
		return populateEMRDocs(MRDUpdateScreenDAO.fieldsForEmrdoc(visitId, null, false));
	}

	public List<EMRDoc> populateEMRDocs(List<BasicDynaBean> beanList) throws SQLException {
		List<EMRDoc> list = new ArrayList<EMRDoc>();
		EMRDoc emrDoc = null;
		int i = 1;
		BasicDynaBean printpref = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_DISCHARGE);
		int printerId = (Integer) printpref.get("printer_id");
		for (BasicDynaBean bean : beanList) {

			emrDoc = new EMRDoc();
			String visitId = (String) bean.get("patient_id");
			emrDoc.setDate((java.util.Date)bean.get("reg_date"));
			emrDoc.setVisitDate((java.util.Date)bean.get("reg_date"));
			emrDoc.setDisplayUrl("/pages/medicalrecorddepartment/MRDUpdate.do?_method=print&patient_id="
						+visitId+"&forcePdf=true&printerId=4");
			emrDoc.setDocid(visitId);
			emrDoc.setPrinterId(printerId);
			emrDoc.setPdfSupported(true);
			emrDoc.setProvider(EMRInterface.Provider.MRDCodesProvider);
			emrDoc.setTitle("MRD Codification Report " + i++);
			emrDoc.setUserName((String) bean.get("user_name"));
			emrDoc.setUpdatedBy((String) bean.get("user_name"));
			emrDoc.setDoctor((String) bean.get("doctor_name"));
			emrDoc.setVisitid(visitId);
			emrDoc.setAuthorized(true);
			emrDoc.setType("SYS_MRDCODE");

			list.add(emrDoc);
		}

		return list;

	}

	public byte[] getPDFBytes(String visitId, int printId) throws Exception {
		StringWriter writer = MRDUpdateScreenBO.getReportContentStringWriter(visitId, "pdf", null);
	    String textContent = writer.toString();
	    BasicDynaBean printPref = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT, printId);
	    HtmlConverter converter = new HtmlConverter();
		return converter.getPdfBytes(textContent, "MRD Codes Report", printPref, false,
				   true, true, true, false);
	}

}
