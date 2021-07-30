package com.insta.hms.emr;

import com.insta.hms.eandmcalculator.EandMcalculatorDao;

import java.util.Collections;
import java.util.List;


public class EandMcalculatorResultsProviderImpl implements EMRInterface{


	public List<EMRDoc> listDocumentsByVisit(String visitId) throws Exception {

		return EandMcalculatorDao.getEandMcalcEMRDocs(visitId, null, false);
	}


	public List<EMRDoc> listVisitDocumentsForMrNo(String mrNo) throws Exception {
		// TODO Auto-generated method stub

		return EandMcalculatorDao.getEandMcalcEMRDocs(null, mrNo, true);
	}


	public List<EMRDoc> listDocumentsByMrno(String mrNo) throws Exception {
		// not applicable
		return Collections.emptyList();
	}

	public byte[] getPDFBytes(String docid, int printerId) throws Exception {
		return EandMcalculatorDao.returnBytes(docid, printerId);
	}

}