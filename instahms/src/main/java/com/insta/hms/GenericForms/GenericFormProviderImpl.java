/**
 *
 */
package com.insta.hms.GenericForms;

import com.insta.hms.emr.EMRDoc;
import com.insta.hms.emr.EMRInterface;
import com.insta.hms.instaforms.GenericForms;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author insta
 *
 */
public class GenericFormProviderImpl implements EMRInterface {

	public byte[] getPDFBytes(String docid, int printId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public List<EMRDoc> listDocumentsByMrno(String mrNo) throws Exception {
		return Collections.EMPTY_LIST;
	}

	public List<EMRDoc> listDocumentsByVisit(String visitId) throws Exception {
		return populateList(GenericForms.listDocumentsByVisit(visitId));
	}

	public List<EMRDoc> listVisitDocumentsForMrNo(String mrNo) throws Exception {

		return populateList(GenericForms.listVisitDocumentsForMrNo(mrNo));
	}

	/**
	 * creates list of EMRDoc objects from a list of DyanaBeans representing each document
	 */
	private List<EMRDoc> populateList(List<BasicDynaBean> list) throws Exception {
		if (list == null || list.isEmpty())
			return null;

		List<EMRDoc> emrdoclist = new ArrayList<EMRDoc>();
		BasicDynaBean printpref = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT);
		int printerId = (Integer) printpref.get("printer_id");
		
		for (BasicDynaBean bean: list) {
			EMRDoc doc = new EMRDoc();
			doc.setTitle((String) bean.get("form_name"));
			doc.setDate(new Date(((Timestamp)bean.get("mod_time")).getTime()));

			doc.setDoctor("");

			String docId = bean.get("generic_form_id").toString();
			doc.setDocid(docId);
			doc.setVisitid((String) bean.get("patient_id"));
			doc.setPdfSupported(true);
			doc.setContentType("application/pdf");
			doc.setAuthorized(true);

			String userName = (String) bean.get("user_name");
			doc.setUpdatedBy(userName);

			doc.setType((String) bean.get("doc_type"));

			doc.setPrinterId(printerId);
			doc.setProvider(EMRInterface.Provider.GenericInstFormProvider);


			String displayUrl = "/GenericForms/GenericFormPrintAction.do?_method=print&printerId="+printerId;
			displayUrl += "&generic_form_id=" + docId + "&insta_form_id="+bean.get("form_id") +
					"&patient_id=" + bean.get("patient_id");
			doc.setDisplayUrl(displayUrl);
			doc.setVisitDate((java.util.Date)bean.get("reg_date"));


			emrdoclist.add(doc);
		}
		return emrdoclist;

	}



}
