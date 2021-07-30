package com.insta.hms.emr;

import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.opthalmology.OphthalmologyReportAction;
import com.insta.hms.opthalmology.OptometristScreenDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class OphthalmologyProviderBOImpl implements EMRInterface{

	public List<EMRDoc> listDocumentsByVisit(String visitId) throws Exception {
		return populateEMRDocs(OptometristScreenDAO.getRecords(visitId, null, false));
	}

	public List<EMRDoc> listVisitDocumentsForMrNo(String mrNo) throws Exception {
		return populateEMRDocs(OptometristScreenDAO.getRecords(null, mrNo, true));
	}

	public List<EMRDoc> populateEMRDocs(List<BasicDynaBean> beanList) throws SQLException {
		List<EMRDoc> list = new ArrayList<EMRDoc>();
		BasicDynaBean printPrefs = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_DISCHARGE);
		for (BasicDynaBean bean : beanList) {
			EMRDoc emrDoc = new EMRDoc();

			String visitId = (String) bean.get("patient_id");
			emrDoc.setVisitDate((java.util.Date)bean.get("reg_date"));
			emrDoc.setDisplayUrl("/opthalmology/opthalmologyReport.do?_method=getReport&mr_no="+bean.get("mr_no")+"&patientId="+visitId);
			emrDoc.setDocid(bean.get("consultation_id").toString());
			emrDoc.setDoctor((String)bean.get("doctor_name"));
			emrDoc.setProvider(EMRInterface.Provider.OphthalmologyProvider);
			emrDoc.setVisitid((String)bean.get("patient_id"));
			emrDoc.setUpdatedBy((String)bean.get("user_name"));
			emrDoc.setPdfSupported(true);
			emrDoc.setPrinterId((Integer)printPrefs.get("printer_id"));
			emrDoc.setAuthorized(true);
			emrDoc.setTitle("Ophthalmology Report");
			emrDoc.setType("SYS_OPHTHALMOLOGY");

			list.add(emrDoc);
		}
		return list;
	}

	public List<EMRDoc> listDocumentsByMrno(String mrNo) {

		return null;
	}

	public byte[] getPDFBytes(String docId, int printId)throws SQLException, Exception {
		BasicDynaBean bean = new GenericDAO("doctor_consultation").findByKey("consultation_id", Integer.parseInt(docId));
		byte[] bytes = OphthalmologyReportAction.getBytes(bean.get("patient_id").toString(), bean.get("mr_no").toString());

		return bytes;
	}



}