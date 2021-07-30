package com.insta.hms.emr;

import com.insta.hms.master.DietaryMaster.DietaryMasterDAO;
import com.insta.hms.master.DietaryMaster.PrescribedMealFtlHealper;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.util.Collections;
import java.util.List;

public class MealsPrescriptionsProviderBOImpl implements EMRInterface{

	public byte[] getPDFBytes(String visitId, int printerId) throws Exception {

		PrescribedMealFtlHealper ftlHelper = new PrescribedMealFtlHealper();
		BasicDynaBean prefs =  PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT, printerId);

		byte[] report = ftlHelper.getPrescriptionFtlReport(visitId, PrescribedMealFtlHealper.return_type.PDF_BYTES, prefs, null);

		return report;
	}

	public List<EMRDoc> listDocumentsByMrno(String mrNo) throws Exception {

		return Collections.emptyList();
	}

	public List<EMRDoc> listVisitDocumentsForMrNo(String mrNo) throws Exception {
		return new DietaryMasterDAO().getVisits(null, mrNo, true);
	}

	public List<EMRDoc> listDocumentsByVisit(String visitId) throws Exception {
		return new DietaryMasterDAO().getVisits(visitId, null, false);
	}

}
