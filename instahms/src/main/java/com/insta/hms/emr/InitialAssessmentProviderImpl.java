/**
 *
 */
package com.insta.hms.emr;

import com.insta.hms.initialassessment.InitialAssessmentFtlHelper;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.outpatient.DoctorConsultationDAO;
import org.apache.commons.beanutils.BasicDynaBean;
import java.util.List;

/**
 * @author krishna
 *
 */
public class InitialAssessmentProviderImpl implements EMRInterface {

	public byte[] getPDFBytes(String docid, int printerId) throws Exception {
		BasicDynaBean prefs = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT,
				printerId);
		return new InitialAssessmentFtlHelper().printAssessment(Integer.parseInt(docid), prefs, null, null,
				InitialAssessmentFtlHelper.ReturnType.PDF_BYTES);
	}

	public List<EMRDoc> listDocumentsByMrno(String mrNo) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public List<EMRDoc> listDocumentsByVisit(String visitId) throws Exception {
		return DoctorConsultationDAO.getAssessmentDocs(visitId, null, false);
	}

	public List<EMRDoc> listVisitDocumentsForMrNo(String mrNo) throws Exception {
		return DoctorConsultationDAO.getAssessmentDocs(null, mrNo, true);
	}

}
