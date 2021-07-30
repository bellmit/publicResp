package com.insta.hms.emr;

import com.insta.hms.common.GenericDAO;
import com.insta.hms.dialysis.DialysisSessionReportAction;
import com.insta.hms.dialysis.DialysisSessionsDao;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.util.ArrayList;
import java.util.List;



public class DialysisSessionsProviderBOImpl implements EMRInterface{

	public List<EMRDoc> listDocumentsByVisit(String visitId)throws Exception {

		return null;
	}


	public List<EMRDoc> listVisitDocumentsForMrNo(String mrNo) throws Exception {
		List<EMRDoc> list = new ArrayList<EMRDoc>();
		int i = 1;
		EMRDoc emrDoc = null;


		List<BasicDynaBean> beanList = DialysisSessionsDao.fieldsForEmrdoc(mrNo);
		BasicDynaBean printpref = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_DISCHARGE);
		int printerId = (Integer) printpref.get("printer_id");
		for (BasicDynaBean bean : beanList) {

			emrDoc = new EMRDoc();
			emrDoc.setDate((java.util.Date)bean.get("start_date"));
			emrDoc.setDisplayUrl("/dialysis/DialysisSessionReport.do?method=getSessionReport&mr_no="
						+mrNo+"&order_id="+bean.get("order_id")+"&forcePdf=true&printerId=4&doc_id="+bean.get("order_id"));
			emrDoc.setDocid(bean.get("order_id").toString());
			
			emrDoc.setPrinterId(printerId);
			emrDoc.setPdfSupported(true);
			emrDoc.setProvider(EMRInterface.Provider.DialysisSessionsProvider);
			emrDoc.setTitle("Dialysis Session Report " + i++);
			emrDoc.setUserName((String)bean.get("user_name"));
			emrDoc.setUpdatedBy((String)bean.get("user_name"));
			emrDoc.setDoctor((String)bean.get("doctor_name"));
			emrDoc.setVisitid((String)bean.get("patient_id"));
			emrDoc.setAuthorized(true);
			emrDoc.setType("SYS_DIALYSIS");
			emrDoc.setVisitDate((java.util.Date)bean.get("reg_date"));


			list.add(emrDoc);
		}

		return list;
	}


	public List<EMRDoc> listDocumentsByMrno(String mrNo)throws Exception {
		return null;
	}


	public byte[] getPDFBytes(String docId, int printId) throws Exception {

		BasicDynaBean bean = new GenericDAO("services_prescribed").
													findByKey("prescription_id", Integer.parseInt(docId));
		byte[] bytes = DialysisSessionReportAction.dialysisSessionsBytes(bean.get("mr_no").toString(), docId);

		return bytes;
	}

}