package com.insta.hms.emr;

import com.insta.hms.common.GenericDAO;
import com.insta.hms.genericdocuments.GenericDocumentsDAO;
import com.insta.hms.master.DietaryMaster.DietaryMasterDAO;
import org.apache.commons.beanutils.BasicDynaBean;
import java.util.Collections;
import java.util.List;

public class DietChartProviderBOImpl implements EMRInterface{

	public byte[] getPDFBytes(String docid, int pritnerId) throws Exception {
		GenericDAO dao  = new GenericDAO("diet_chart_documents");

		BasicDynaBean bean = dao.findByKey("doc_id", Integer.parseInt(docid));
		return GenericDocumentsDAO.getDocumentBytes(docid, false, null,
				bean.get("patient_id").toString(), 4);

	}


	public List<EMRDoc> listVisitDocumentsForMrNo(String mrNo) throws Exception {
		return new DietaryMasterDAO().getDietaryChartForEMR(null, mrNo, true);
	}


	public List<EMRDoc> listDocumentsByMrno(String mrNo) throws Exception {

		return Collections.emptyList();
	}

	public List<EMRDoc> listDocumentsByVisit(String visitId) throws Exception {
		return new DietaryMasterDAO().getDietaryChartForEMR(visitId, null, false);
	}

}


