package com.insta.hms.emr;

import com.insta.hms.common.GenericDAO;
import com.insta.hms.documentpersitence.OpDocumentAbstractImpl;
import com.insta.hms.genericdocuments.GenericDocumentsDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.outpatient.OutPatientDAO;
import org.apache.commons.beanutils.BasicDynaBean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This provider is for all consultation form documents.
 *
 * A patient can many consultations in a visit, and each consultation results
 * in one or more consultation forms (documents). Typically each consultation is
 * with a different doctor.
 *
 */
public class CaseFormsProviderImpl implements EMRInterface {

	public List<EMRDoc> listDocumentsByVisit(String visitId)
			throws Exception {

		List<BasicDynaBean> beans =	new OpDocumentAbstractImpl().getDocumentsList("patient_id", visitId, true, "SYS_OP");
		return listDocuments(beans);

	}

	public List<EMRDoc> listVisitDocumentsForMrNo(String mrNo)
		throws Exception {

		List<BasicDynaBean> beans =	new OpDocumentAbstractImpl().getVisitDocumentsForMrNo(mrNo);
		return listDocuments(beans);
	}

	public List<EMRDoc> listDocumentsByMrno(String mrNo) throws Exception {
		return Collections.emptyList();
	}

	public byte[] getPDFBytes(String docid, int printerId) throws Exception {
		OutPatientDAO outpatientdocdao = new OutPatientDAO();
		BasicDynaBean generaldocbean = outpatientdocdao.findByKey("doc_id", Integer.parseInt(docid));
		Integer consultationId = (Integer) generaldocbean.get("consultation_id");
		GenericDAO consultDAO = new GenericDAO("doctor_consultation");
		String patientId = (String) consultDAO.findByKey("consultation_id", consultationId).get("patient_id");
		return GenericDocumentsDAO.getDocumentBytes(docid, false, null, patientId, printerId);
	}

	public List<EMRDoc> listDocuments (List<BasicDynaBean> beans ) throws Exception {
		if (beans == null || beans.isEmpty())
			return null;

		List<EMRDoc> emrDocs = new ArrayList<EMRDoc>();
		BasicDynaBean printpref = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT);
		int printerId = (Integer) printpref.get("printer_id");
		
		for(BasicDynaBean b : beans) {
			EMRDoc doc = new EMRDoc();

			String format = (String) b.get("doc_format");
			String docId = b.get("doc_id").toString();
			doc.setDocid(docId);
			doc.setTitle(b.get("doc_name").toString());
			doc.setDate((java.util.Date)b.get("doc_date"));
			doc.setVisitDate((java.util.Date)b.get("reg_date"));
			doc.setPrinterId(printerId);
			String displayUrl = "/Outpatient/OutpatientDocumentsPrint.do?_method=print&doc_id="
				+ docId +"&forcePdf=true&printerId="+printerId ;
			if (format.equals("doc_hvf_templates"))
				displayUrl = displayUrl + "&allFields=Y";

			doc.setDisplayUrl(displayUrl);
			doc.setVisitid(b.get("patient_id").toString());
			doc.setType("SYS_OP");
			doc.setUpdatedBy((String)b.get("username"));
			doc.setAuthorized(Helper.getAuthorized((String) b.get("username"),
					(String) b.get("access_rights")));
			doc.setDoctor(b.get("doctor").toString());


			if (format.equals("doc_rtf_templates")) {
				doc.setPdfSupported(false);
			} else if (format.equals("doc_fileupload")) {
				String contentType = (String) b.get("content_type");
				if (contentType.equals("application/pdf") || contentType.split("/")[0].equals("image"))
					doc.setPdfSupported(true);
				else doc.setPdfSupported(false);
			} else {
				doc.setPdfSupported(true);
			}
			doc.setProvider(EMRInterface.Provider.CaseFormsProvider);
			emrDocs.add(doc);
		}

		return emrDocs;

	}

}
