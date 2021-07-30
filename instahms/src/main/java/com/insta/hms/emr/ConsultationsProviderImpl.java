/**
 *
 */
package com.insta.hms.emr;

import com.insta.hms.dischargemedication.DischargeMedicationDAO;
import com.insta.hms.outpatient.DoctorConsultationDAO;
import com.insta.hms.outpatient.OPPrescriptionFtlHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author krishna.t
 *
 */
public class ConsultationsProviderImpl implements EMRInterface {

	public List<EMRDoc> listDocumentsByVisit(String visitId) throws Exception {
		List<EMRDoc> listOfDischargeMedication = new DischargeMedicationDAO().getDischargeMedicationEMRForOP(visitId, null);
		List<EMRDoc> conslist = DoctorConsultationDAO.getCaseSheetEMRDocs(visitId, null, false);
		List<EMRDoc> list = new ArrayList<EMRDoc>(conslist);
		list.addAll(listOfDischargeMedication);
		return list;
	}


	public List<EMRDoc> listVisitDocumentsForMrNo(String mrNo) throws Exception {
		// TODO Auto-generated method stub
		
		List<EMRDoc> conslist = DoctorConsultationDAO.getCaseSheetEMRDocs(null, mrNo, true);
		List<EMRDoc> listOfDischargeMedication = new DischargeMedicationDAO().getDischargeMedicationEMRForOP(null, mrNo);
		List<EMRDoc> list = new ArrayList<EMRDoc>(conslist);
		list.addAll(listOfDischargeMedication);
		return list;
	}


	public List<EMRDoc> listDocumentsByMrno(String mrNo) throws Exception {
		// not applicable
		return Collections.emptyList();
	}

	public byte[] getPDFBytes(String docid, int printerId) throws Exception {
		return new OPPrescriptionFtlHelper().getConsultationPdfBytes(docid, false, printerId);
	}

}
