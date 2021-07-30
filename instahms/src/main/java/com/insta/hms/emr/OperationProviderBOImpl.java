package com.insta.hms.emr;

import com.insta.hms.OTServices.OTReportsDAO;
import com.insta.hms.OTServices.OTServicesDAO;
import com.insta.hms.genericdocuments.GenericDocumentsDAO;
import com.insta.hms.master.PatientHeaderTemplate.PatientHeaderTemplateDAO;
import org.apache.commons.beanutils.BasicDynaBean;
import java.util.Collections;
import java.util.List;

/**
 * This provider is for all Operation report documents related to a patient
 * visit.
 *
 */
public class OperationProviderBOImpl implements EMRInterface {

	PatientHeaderTemplateDAO phTemplateDAO = new PatientHeaderTemplateDAO();

	public List<EMRDoc> listDocumentsByVisit(String visitId) throws Exception {
		return OTReportsDAO.getConductedoperationsListForEMR(visitId, null, false);
	}

	public List<EMRDoc> listVisitDocumentsForMrNo(String mrNo) throws Exception {
		return OTReportsDAO.getConductedoperationsListForEMR(null, mrNo, true);
	}

	public List<EMRDoc> listDocumentsByMrno(String mrNo) throws Exception {
		return Collections.emptyList();
	}

	public byte[] getPDFBytes(String docid, int printerId) throws Exception {
		OTReportsDAO opdocdao = new OTReportsDAO();
		BasicDynaBean opdocbean = opdocdao.findByKey("doc_id", Integer.parseInt(docid));
		Integer consultationId = (Integer) opdocbean.get("prescription_id");
		String patientId = (String) OTServicesDAO.getOPDetails(consultationId).get("patient_id");
		return GenericDocumentsDAO.getDocumentBytes(docid, false, null, patientId, printerId);

	}
}



