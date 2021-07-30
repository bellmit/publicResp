package com.insta.hms.emr;

import com.insta.hms.common.GenericDAO;
import com.insta.hms.genericdocuments.GenericDocumentsDAO;
import com.insta.hms.mlcdocuments.MLCDocumentsBO;
import org.apache.commons.beanutils.BasicDynaBean;
import java.util.Collections;
import java.util.List;

public class MLCFormProviderBOImpl implements EMRInterface {

	public byte[] getPDFBytes(String docid, int printerId) throws Exception {
		GenericDAO dao  = new GenericDAO("patient_registration");
		BasicDynaBean bean = dao.findByKey("doc_id", Integer.parseInt(docid));
		return GenericDocumentsDAO.getDocumentBytes(docid, false, (String) bean.get("mr_no"),
				(String) bean.get("patient_id"), printerId);
	}

	public List<EMRDoc> listVisitDocumentsForMrNo(String mrNo) throws Exception {
		return MLCDocumentsBO.getMLCListForEMR(null, mrNo, true);
	}

	public List<EMRDoc> listDocumentsByMrno(String mrNo) throws Exception {
		return Collections.emptyList();
	}

	public List<EMRDoc> listDocumentsByVisit(String visitId) throws Exception {
		return MLCDocumentsBO.getMLCListForEMR(visitId, null, false);
	}

}
