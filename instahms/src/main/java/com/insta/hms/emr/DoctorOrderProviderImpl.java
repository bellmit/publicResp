/**
 *
 */
package com.insta.hms.emr;

import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.wardactivities.prescription.IPPrescriptionDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author krishna
 *
 */
public class DoctorOrderProviderImpl implements EMRInterface {

	public List<EMRDoc> listDocumentsByVisit(String visitId) throws Exception {
		List<BasicDynaBean> list = new IPPrescriptionDAO().getDoctorOrderForEMR(visitId, null);
		return populateEMRDoc(list);
	}


	public List<EMRDoc> listVisitDocumentsForMrNo(String mrNo) throws Exception {
		// TODO Auto-generated method stub
		List<BasicDynaBean> list = new IPPrescriptionDAO().getDoctorOrderForEMR(null, mrNo);
		return populateEMRDoc(list);
	}


	public List<EMRDoc> listDocumentsByMrno(String mrNo) throws Exception {
		// not applicable
		return Collections.emptyList();
	}

	public byte[] getPDFBytes(String docid, int printerId) throws Exception {
		return null;
	}

	private List<EMRDoc> populateEMRDoc(List<BasicDynaBean> list) throws Exception {
		List<EMRDoc> docs = new ArrayList<EMRDoc>();
		BasicDynaBean printpref = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT);
		int printerId = (Integer) printpref.get("printer_id");
		for (BasicDynaBean b : list) {
			EMRDoc emrDoc = new EMRDoc();

			String doctor = (String) b.get("doctor_name");
			emrDoc.setPrinterId(printerId);
			String patientId = (String) b.get("patient_id");
			emrDoc.setDocid(patientId);
			emrDoc.setDate((Date)b.get("reg_date"));
			emrDoc.setVisitDate((Date)b.get("reg_date"));
			emrDoc.setType("SYS_IP");
			emrDoc.setPdfSupported(true);
			emrDoc.setAuthorized(true);
			emrDoc.setContentType("application/pdf");
			emrDoc.setDoctor(doctor);
			emrDoc.setTitle("Physician Order - " + doctor);
			emrDoc.setDisplayUrl("/print/printPhysicianOrders.json?" +
					"patientId=" + patientId + "&printerId=" + printerId);
			emrDoc.setProvider(EMRInterface.Provider.DoctorOrderProvider);
			emrDoc.setVisitid((String) b.get("patient_id"));

			docs.add(emrDoc);
		}
		return docs;
	}

}
