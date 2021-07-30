package com.insta.hms.emr;

import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.genericdocuments.GenericDocumentsDAO;
import org.apache.commons.beanutils.BasicDynaBean;
import java.util.List;

public class RegistrationFormsProviderBOImpl implements EMRInterface {

	public byte[] getPDFBytes(String docid, int printerId) throws Exception {

		BasicDynaBean bean = VisitDetailsDAO.getRegInfoFromDocidForEMR(Integer.parseInt(docid));

		return GenericDocumentsDAO.getDocumentBytes(docid, false, bean.get("mr_no").toString(),
				bean.get("patient_id").toString(), printerId);
	}

	public List<EMRDoc> listVisitDocumentsForMrNo(String mrNo) throws Exception {
		return VisitDetailsDAO.getRegistrationFormsForEMR(null, mrNo, true);
	}

	public List<EMRDoc> listDocumentsByMrno(String mrNo) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public List<EMRDoc> listDocumentsByVisit(String visitId) throws Exception {
		return VisitDetailsDAO.getRegistrationFormsForEMR(visitId, null, false);
	}
}
