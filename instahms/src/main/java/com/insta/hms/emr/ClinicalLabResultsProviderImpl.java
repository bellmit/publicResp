package com.insta.hms.emr;

import com.bob.hms.common.DateUtil;
import com.insta.hms.clinicaldatalabresuts.ClinicalDataLabResutsAction;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClinicalLabResultsProviderImpl implements EMRInterface {

	public ClinicalLabResultsProviderImpl() {
		// TODO Auto-generated constructor stub
	}

	public byte[] getPDFBytes(String docid, int pritnerId) throws Exception {
		return ClinicalDataLabResutsAction.printFromID(Integer.parseInt(docid), pritnerId);

	}

	public List<EMRDoc> listVisitDocumentsForMrNo(String mrNo) throws Exception {
		return Collections.emptyList();
	}

	public List<EMRDoc> listDocumentsByMrno(String mrNo) throws Exception {
		return getReportForEMR(mrNo);
	}

	public List<EMRDoc> listDocumentsByVisit(String visitId) throws Exception {
		return Collections.emptyList();
	}


	public static List<EMRDoc> getReportForEMR(String mrNo)
			throws SQLException {


		List<BasicDynaBean> list = new GenericDAO("clinical_lab_recorded").findAllByKey("mrno", mrNo);
		if (list == null || list.isEmpty())
			return null;

		List<EMRDoc> emrdoclist = new ArrayList<EMRDoc>();
		BasicDynaBean printpref = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_DIAG);
		int printerId = (Integer) printpref.get("printer_id");
		for (BasicDynaBean bean: list) {
			EMRDoc doc = new EMRDoc();
			doc.setTitle(DateUtil.formatDate((java.sql.Date)bean.get("values_as_of_date")));
			doc.setDate((java.sql.Date)bean.get("values_as_of_date"));

			doc.setDoctor("");

			String docId = bean.get("clinical_lab_recorded_id").toString();
			doc.setDocid(docId);

			doc.setPdfSupported(true);
			doc.setType("SYS_CLINICAL_LAB");
			doc.setAuthorized(true);

			doc.setPrinterId(printerId);
			doc.setProvider(EMRInterface.Provider.ClinicalLabResultsProvider);

			 String printURL = "/dialysis/ClinicalDataLabResults.do?_method=generatePrint";
			 printURL = printURL+"&reportId="+bean.get("clinical_lab_recorded_id");
			 printURL = printURL+"&printerId="+printerId;
			doc.setDisplayUrl(printURL);
			emrdoclist.add(doc);
		}
		return emrdoclist;

	}


}
