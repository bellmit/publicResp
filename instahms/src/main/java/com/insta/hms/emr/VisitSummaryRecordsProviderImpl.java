package com.insta.hms.emr;

import com.insta.hms.dischargemedication.DischargeMedicationDAO;
import com.insta.hms.wardactivities.visitsummaryrecord.VisitSummaryRecordDAO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author nikunj.s
 *
 */
public class VisitSummaryRecordsProviderImpl implements EMRInterface {
	public List<EMRDoc> listDocumentsByMrno(String mrNo) throws Exception {
//		 we don't have any non-visit documents.
		return Collections.emptyList();
	}

	public List<EMRDoc> listDocumentsByVisit(String visitId) throws Exception {
		List<EMRDoc> listOfEMRdocs = new VisitSummaryRecordDAO().getAllEMRVisitSummaryRecord(visitId, null, false);
		List<EMRDoc> listOfDischargeMedication = new DischargeMedicationDAO().getDischargeMedicationEMRForIP(visitId, null);
		List<EMRDoc> mergeList = new ArrayList<EMRDoc>(listOfEMRdocs);
		mergeList.addAll(listOfDischargeMedication);
		
		return mergeList;
	}

	public List<EMRDoc> listVisitDocumentsForMrNo(String mrNo) throws Exception {
		List<EMRDoc> listOfEMRdocs = new VisitSummaryRecordDAO().getAllEMRVisitSummaryRecord(null, mrNo, true);
		List<EMRDoc> listOfDischargeMedication = new DischargeMedicationDAO().getDischargeMedicationEMRForIP(null, mrNo);
		List<EMRDoc> mergeList = new ArrayList<EMRDoc>(listOfEMRdocs);
		mergeList.addAll(listOfDischargeMedication);
		
		return mergeList;
	}

	public byte[] getPDFBytes(String docid, int printId) throws Exception {
		return null;
	}

}