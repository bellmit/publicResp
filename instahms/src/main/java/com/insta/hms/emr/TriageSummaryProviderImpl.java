/**
 *
 */
package com.insta.hms.emr;

import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.outpatient.DoctorConsultationDAO;
import com.insta.hms.outpatient.OPPrescriptionFtlHelper;
import com.insta.hms.outpatient.OPPrescriptionFtlHelper.ReturnType;

import org.apache.commons.beanutils.BasicDynaBean;

import java.util.Collections;
import java.util.List;

/**
 * @author krishna
 *
 */
public class TriageSummaryProviderImpl implements EMRInterface {

	public List<EMRDoc> listDocumentsByVisit(String visitId) throws Exception {
		return DoctorConsultationDAO.getTriageDocs(visitId, null, false);
	}

	public List<EMRDoc> listVisitDocumentsForMrNo(String mrNo) throws Exception {
		return DoctorConsultationDAO.getTriageDocs(null, mrNo, true);
	}

	public List<EMRDoc> listDocumentsByMrno(String mrNo) throws Exception {
		// not applicable
		return Collections.emptyList();
	}

	public byte[] getPDFBytes(String docid, int printerId) throws Exception {
		BasicDynaBean prefs = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT,
				printerId);
		return new OPPrescriptionFtlHelper().getTriageAndClinicalInfoFtlReport(Integer.parseInt(docid),
		    ReturnType.PDF_BYTES, prefs, null, null, true);
	}
}
