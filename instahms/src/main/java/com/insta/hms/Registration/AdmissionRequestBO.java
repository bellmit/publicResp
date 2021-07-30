/**
 *
 */
package com.insta.hms.Registration;

import com.bob.hms.common.DateUtil;
import com.google.common.html.HtmlEscapers;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.core.clinical.forms.FormParameter;
import com.insta.hms.core.clinical.mar.MarService;
import com.insta.hms.core.clinical.notes.NotesRepository;
import com.insta.hms.mdm.notetypes.NoteTypesRepository;
import com.insta.hms.medicalrecorddepartment.MRDDiagnosisDAO;
import com.insta.hms.outpatient.PatientPrescriptionDAO;
import com.insta.hms.outpatient.PrescriptionBO;
import com.insta.hms.wardactivities.doctorsnotes.DoctorsNotesDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.List;

/**
 * @author mithun.saha
 *
 */
public class AdmissionRequestBO {
	static Logger log = LoggerFactory.getLogger(AdmissionRequestBO.class);
	MRDDiagnosisDAO mrdDAO = new MRDDiagnosisDAO();
//	IPPrescriptionDAO prescDAO = new IPPrescriptionDAO();
	PatientPrescriptionDAO prescDAO = new PatientPrescriptionDAO();
	PrescriptionBO pbo = new PrescriptionBO();
	DoctorsNotesDAO dnotedao = new DoctorsNotesDAO();
	GenericDAO mediPres = new GenericDAO("patient_medicine_prescriptions");
	GenericDAO invPres = new GenericDAO("patient_test_prescriptions");
	GenericDAO serPres = new GenericDAO("patient_service_prescriptions");
	GenericDAO docPres = new GenericDAO("patient_consultation_prescriptions");
	GenericDAO nonBillablePres = new GenericDAO("patient_other_prescriptions");
	GenericDAO otherMediPres = new GenericDAO("patient_other_medicine_prescriptions");
	MarService marService = ApplicationContextProvider.getBean(MarService.class);
	NotesRepository notesRepository = ApplicationContextProvider.getBean(NotesRepository.class);
	NoteTypesRepository notesTypeRepository = ApplicationContextProvider.getBean(NoteTypesRepository.class);

	public boolean insertAdmissionRequestDetails(Connection con, Integer admReqId, String patientId, String userName, String mrNo) throws Exception{
		boolean success = true;
		try {
			List<BasicDynaBean> diagnosisDetailsList = mrdDAO.listAll(null, "adm_request_id", admReqId);
			List<BasicDynaBean> ipPrescriptions = prescDAO.listAll(null, "adm_request_id", admReqId);
			BasicDynaBean admRequestBean = new AdmissionRequestDAO().findByKey("adm_request_id", admReqId);
			for(BasicDynaBean diagBean : diagnosisDetailsList) {
				diagBean.set("id", new BigDecimal(mrdDAO.getNextSequence()));
				diagBean.set("visit_id", patientId);
				diagBean.set("username", userName);
				diagBean.set("mod_time", DateUtil.getCurrentTimestamp());
				success = mrdDAO.insert(con, diagBean);
			}

			for(BasicDynaBean prescBean : ipPrescriptions) {
				boolean isTypeMedicine = false;
			  if ("Operation".equals(prescBean.get("presc_type"))) {
			    continue;
			  }
			    Object presSequence = prescDAO.getNextSequence();
			    Object oldSequence = prescBean.get("patient_presc_id");
			    Timestamp modTime = DateUtil.getCurrentTimestamp();
				prescBean.set("patient_presc_id", presSequence);
				prescBean.set("visit_id", patientId);
				prescBean.set("username", userName);
				prescBean.set("adm_request_id", null);
				prescDAO.insert(con, prescBean);
				BasicDynaBean bean = null;
				String ipPrescriptionType = "";
				if ("Medicine".equals(prescBean.get("presc_type"))) {
				  isTypeMedicine = true;
				  ipPrescriptionType = "M";
				  bean = mediPres.findByKey("op_medicine_pres_id", oldSequence);
				  if (bean == null) {
				    bean = otherMediPres.findByKey("prescription_id", oldSequence);
				    bean.set("prescription_id", presSequence);
				    bean.set("mod_time", modTime);
				    bean.set("visit_id",patientId);
				    bean.set("medicine_quantity",1);
				    success = success && otherMediPres.insert(con, bean);
				  } else {
				    bean.set("op_medicine_pres_id", presSequence);
				    bean.set("mod_time", modTime);
				    bean.set("visit_id",patientId);
				    bean.set("medicine_quantity",1);
				    success = success && mediPres.insert(con, bean);
				  }

				  FormParameter formParameter = new FormParameter("Form_IP", "", mrNo,
							patientId, patientId, "patient_id");
				  marService.marSetup(prescBean,formParameter);
				  
				} else if ("Inv.".equals(prescBean.get("presc_type"))) {
				  ipPrescriptionType = "I";
				  bean = invPres.findByKey("op_test_pres_id", oldSequence);
				  bean.set("op_test_pres_id", presSequence);
				  bean.set("mod_time", modTime);
				  success = success && invPres.insert(con, bean);
				} else if ("Service".equals(prescBean.get("presc_type"))) {
				  ipPrescriptionType = "S";
				  bean = serPres.findByKey("op_service_pres_id", oldSequence);
				  bean.set("op_service_pres_id", presSequence);
				  bean.set("mod_time", modTime);
				  success = success && serPres.insert(con, bean);
				} else if ("Doctor".equals(prescBean.get("presc_type"))) {
				  ipPrescriptionType = "C";
				  bean = docPres.findByKey("prescription_id", oldSequence);
				  bean.set("prescription_id", presSequence);
				  bean.set("mod_time", modTime);
				  success = success && docPres.insert(con, bean);
				} else if ("NonBillable".equals(prescBean.get("presc_type"))) {
				  ipPrescriptionType = "O";
				  bean = nonBillablePres.findByKey("prescription_id", oldSequence);
				  bean.set("prescription_id", presSequence);
				  bean.set("mod_time", modTime);
				  success = success && nonBillablePres.insert(con, bean);
				}
				prescBean.set("presc_type", ipPrescriptionType);
				if (!isTypeMedicine) {
					if (!success || !pbo.insertActivity(con, prescBean, patientId, userName)) {
						success = false;
						break;
					}
				}
			}
			if	(success) {
				BasicDynaBean doctorNotesBean = dnotedao.getBean();
				BasicDynaBean patientNoteBean = notesRepository.getBean();
				doctorNotesBean  = copyToDoctorNotesBean(admRequestBean,doctorNotesBean,patientId);
				Integer noteTypeId = notesTypeRepository.getNotesTypeIdByName("Admission Request Notes");
				if(noteTypeId!=null && ((String)admRequestBean.get("remarks")).trim().length() > 0) {
					patientNoteBean = copyToPatientNotesBean(admRequestBean, patientNoteBean, patientId,noteTypeId,userName);
					notesRepository.insert(patientNoteBean);
				} else {
					log.error("Note Type Id for Admission Request found null");
				}
				success = dnotedao.insert(con, doctorNotesBean);
			}
			if(success) {
				success = AdmissionRequestDAO.updateAdmissionRequestStatus(con, admReqId, "I", userName);
			}
		} finally {
			if(success)
				log.debug("Admission request details inserted suceesfully.........");
			else
				log.debug("Failed to insert the activity.........");
		}
		return success;
	}

	private BasicDynaBean copyToDoctorNotesBean(BasicDynaBean bean,BasicDynaBean doctorNotesBean,String patientId) throws Exception {

		doctorNotesBean.set("note_id", new DoctorsNotesDAO().getNextSequence());
		doctorNotesBean.set("patient_id", patientId);
		doctorNotesBean.set("note_num", 1);
		String remarks = "Admission Request Remarks as of " +new DateUtil().getDateFormatter().format(bean.get("admission_date"))+": "+(String)bean.get("remarks");
		doctorNotesBean.set("notes", remarks);
		doctorNotesBean.set("billable_consultation", "N");
		doctorNotesBean.set("creation_datetime", DateUtil.getCurrentTimestamp());
		doctorNotesBean.set("doctor_id", (String)bean.get("requesting_doc"));
		doctorNotesBean.set("mod_time", DateUtil.getCurrentTimestamp());
		doctorNotesBean.set("mod_user", "InstaAdmin");
		doctorNotesBean.set("finalized", "Y");
		return doctorNotesBean;
	}

	private BasicDynaBean copyToPatientNotesBean(BasicDynaBean bean,BasicDynaBean patientNotesBean,String patientId,Integer noteTypeId,String username) throws Exception {

		patientNotesBean.set("note_id", notesRepository.getNextSequence());
		patientNotesBean.set("patient_id", patientId);
		String remarks = (String)bean.get("remarks");
        remarks = HtmlEscapers.htmlEscaper().escape(remarks).replaceAll("(\r\n|\n)", "<br />");
		patientNotesBean.set("note_content", remarks);
		patientNotesBean.set("billable_consultation", "N");
		patientNotesBean.set("on_behalf_doctor_id", (String)bean.get("requesting_doc"));
		patientNotesBean.set("mod_time", DateUtil.getCurrentTimestamp());
		patientNotesBean.set("mod_user", username);
		patientNotesBean.set("save_status", "F");
		patientNotesBean.set("note_type_id", noteTypeId);
		patientNotesBean.set("created_by", username);
		patientNotesBean.set("documented_date", new java.sql.Date(new java.util.Date().getTime()));
		patientNotesBean.set("documented_time", new java.sql.Time((new java.util.Date()).getTime()));
		return patientNotesBean;
	}
}
