package com.insta.hms.emr;

import com.insta.hms.OTServices.OtRecord.OtRecordDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;


public class OtRecordsProviderImpl implements EMRInterface {

	public List<EMRDoc> listDocumentsByMrno(String mrNo) throws Exception {
//		 we don't have any non-visit documents.
		return Collections.emptyList();
	}

	public List<EMRDoc> listDocumentsByVisit(String visitId) throws Exception {
		return populateEMROtRecords(OtRecordDAO.getAllCompletedOperationsOtRecord(visitId, null, false));
	}

	public List<EMRDoc> listVisitDocumentsForMrNo(String mrNo) throws Exception {
		return populateEMROtRecords(OtRecordDAO.getAllCompletedOperationsOtRecord(null, mrNo, true));
	}

	public byte[] getPDFBytes(String docid, int printId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public List<EMRDoc> populateEMROtRecords(List<BasicDynaBean> OperationOtRecordBeans) throws SQLException, IOException{
		List<EMRDoc> docs = new ArrayList<EMRDoc>();
		BasicDynaBean printpref = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT);
		int printerId = (Integer) printpref.get("printer_id");
		for (BasicDynaBean bean : OperationOtRecordBeans) {
			EMRDoc doc = new EMRDoc();

			Integer docId = (Integer) bean.get("operation_details_id");
			Integer operationDetailsId = (Integer)bean.get("operation_details_id");
			String mrNo = (String) bean.get("mr_no");
			doc.setType("SYS_OT");
			doc.setDocid(docId+"");
			doc.setTitle((String)bean.get("operation_name"));
			doc.setUpdatedBy("");
			Timestamp d = (Timestamp)bean.get("surgery_start");
			if (d == null)
				d = (Timestamp)bean.get("prescribed_date");

			doc.setDate(new Date(d.getTime()));
			doc.setDoctor((bean.get("doctor_name") == null) ? "" : (String)bean.get("doctor_name"));
			String patientId = (String)bean.get("patient_id");
			doc.setVisitid(patientId);
			doc.setPrinterId(printerId);
			doc.setPdfSupported(true);
			doc.setAuthorized(true);

			String displayUrl = "/OtManagement/OtRecord/OtRecordPrint.do?_method=print&patient_id="
				+ patientId + "&mr_no=" + mrNo + "&operation_details_id=" + operationDetailsId + "&printerId="+printerId;
			doc.setDisplayUrl(displayUrl);
			doc.setProvider(EMRInterface.Provider.OtRecordsProvider);
			doc.setVisitDate((java.util.Date)bean.get("reg_date"));

			docs.add(doc);
		}
		return docs;
	}

}
