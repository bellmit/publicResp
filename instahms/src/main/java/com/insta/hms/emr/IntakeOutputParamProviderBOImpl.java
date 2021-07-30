package com.insta.hms.emr;

import com.insta.hms.common.AppInit;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.vitalForm.GenericVitalFormFtlHelper;
import com.insta.hms.vitalparameter.VitalMasterDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.util.Collections;
import java.util.List;

public class IntakeOutputParamProviderBOImpl implements EMRInterface {

	public List<EMRDoc> listDocumentsByVisit(String visitId) throws Exception {
		return new VitalMasterDAO().getAllEMRIntakeOutputs(visitId, null, false);
	}

	public List<EMRDoc> listVisitDocumentsForMrNo(String mrNo) throws Exception {
		return new VitalMasterDAO().getAllEMRIntakeOutputs(null, mrNo, true);
	}

	public List<EMRDoc> listDocumentsByMrno(String mrNo) throws Exception {
		// we don't have any non-visit documents.
		return Collections.emptyList();
	}

	public byte[] getPDFBytes(String docid, int printerId) throws Exception {

		BasicDynaBean prefs =  PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT, printerId);

		GenericVitalFormFtlHelper ftlHelper = new GenericVitalFormFtlHelper(
				AppInit.getFmConfig());

		byte[] vital = ftlHelper
					.getVitalParameterReport(docid,"I/O",
							GenericVitalFormFtlHelper.return_type.PDF_BYTES,
							prefs, null);
		return vital;

	}

}
