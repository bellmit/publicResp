package com.insta.hms.billing;

import com.bob.hms.common.RequestContext;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.genericdocuments.GenericDocumentsDAO;
import com.insta.hms.master.Accounting.ChargeHeadsDAO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.HealthAuthorityPreferences.HealthAuthorityDTO;
import com.insta.hms.master.HealthAuthorityPreferences.HealthAuthorityPreferencesDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDTO;
import com.insta.hms.master.ServiceMaster.ServiceMasterDAO;
import org.apache.commons.beanutils.BasicDynaBean;


import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;

public class claimErrorProcessor{

	ClaimSubmissionDAO submitdao = new ClaimSubmissionDAO();
	ClaimReconciliationDAO reconcildao = new ClaimReconciliationDAO();
	GenericDocumentsDAO gendao = new GenericDocumentsDAO();
	ClaimGeneratorHelper helper = new ClaimGeneratorHelper();
	ClaimDAO claimdao = new ClaimDAO();

	public void process(String submission_batch_id, String isResubmission, String healthAuthority,
			Map<String,StringBuilder> errorsMap, String path, BasicDynaBean headerbean, OutputStream stream)
			throws Exception, SQLException{

		HttpSession session = RequestContext.getSession();
		boolean mod_eclaim = (Boolean)session.getAttribute("mod_eclaim");

		String eclaimXMLSchema = null;
		String defaultDiagnosisCodeType = null;

		RegistrationPreferencesDTO regPref = RegistrationPreferencesDAO.getRegistrationPreferences();

		String govenmtIdLabel = regPref.getGovernment_identifier_label() != null ? regPref.getGovernment_identifier_label() : "Emirates ID";
		String govenmtIdTypeLabel = regPref.getGovernment_identifier_label() != null ? regPref.getGovernment_identifier_type_label() : "Emirates ID Type";

		String encTypePref = regPref.getEncntr_type_reqd() != null ? regPref.getEncntr_type_reqd() : "RQ";
		String encStartEndTypePref = regPref.getEncntr_start_and_end_reqd() != null ? regPref.getEncntr_start_and_end_reqd() : "RQ";

		StringBuilder attachmentErr = new StringBuilder("<br/> ATTACHMENT ERROR: Claims which have attachment could not be attached in XML. <br/>" +
										"Please check the attachments for Claims : <br/> ");

		StringBuilder noCliniciansErr = new StringBuilder("<br/> NO CLINICIANS ERROR: Claims without clinician. " +
										"Please enter clinician for Patients : <br/>");

		StringBuilder clinicianIdErr = new StringBuilder("<br/> CLINICIAN ERROR: Claims without clinician Id. " +
										"Please enter clinician id for Doctors : <br/>");
		
		StringBuilder orderingClinicianErr = new StringBuilder("<br/> ORDERING CLINICIAN ERROR: Claims without ordering clinician. " +
											  "Please enter ordering clinician for Doctors : <br/>");

		StringBuilder encountersErr = new StringBuilder("<br/> ENCOUNTERS ERROR: Claims without encounter types. <br/>" +
										"Please enter encounter types for Patients : <br/>");

		StringBuilder encountersStartErr = new StringBuilder("<br/> ENCOUNTERS START ERROR: Claims without encounter start types. <br/>" +
										"Please enter encounter start types for Patients : <br/>");

		StringBuilder encountersEndErr = new StringBuilder("<br/> ENCOUNTERS END ERROR: Claims without encounter end types. <br/>" +
										"Please enter encounter end types for Patients : <br/>");

		StringBuilder encountersEndDateErr= new StringBuilder("<br/> ENCOUNTERS END DATE ERROR: Claims without encounter end date. <br/>" +
										"Please enter encounter end date for Patients : <br/>");

		StringBuilder diagnosisCodesErr = new StringBuilder("<br/> DIAGNOSIS ERROR: Claims without diagnosis codes. <br/>" +
										"Please enter diagnosis codes for Patients : <br/>");
		StringBuilder primaryDiagnosisCodesErr = new StringBuilder("<br/> PRIMARY DIAGNOSIS ERROR: Claims without Primary diagnosis codes. <br/>" +
				"Please enter Primary diagnosis codes for Patients : <br/>");

		StringBuilder accumedDiagnosisCodeTypeErr = new StringBuilder("<br/> DIAGNOSIS CODE TYPE ERROR: Claims with invalid diagnosis code type. <br/>" +
										"Please enter diagnosis codes of type (ICD9 / ICD10) for Patients : <br/>");

		StringBuilder haadDiagnosisCodeTypeErr = new StringBuilder("<br/> DIAGNOSIS CODE TYPE ERROR: Claims with invalid diagnosis code type. <br/>" +
										"Please enter diagnosis codes of type (ICD) for Patients : <br/>");

		StringBuilder noBillsErr = new StringBuilder("<br/> ZERO BILLS ERROR: Claims found without bills. <br/>" +
										"Please check the Claims without finalized bills: <br/>");

		StringBuilder billStatusErr = new StringBuilder("<br/> BILLS OPEN ERROR: Claims which have Open bills. <br/>" +
										"Please check the claims with Open Bills: <br/>");

		StringBuilder codesErr = new StringBuilder("<br/> CODES ERROR: Claims found without activity codes. <br/>" +
										"Please check the Bills : <br/>");

		StringBuilder consCodesErr = new StringBuilder("<br/> CONS CODES ERROR: Claims found without observation codes (or) values.<br/> " +
										"Please check the Consultation(s) for Patients : <br/>");

		StringBuilder consComplaintCodesErr = new StringBuilder("<br/> CONS COMPLAINT CODES ERROR: Claims found without presenting-complaint observation codes (or) values.<br/> " +
										"Please check the Consultation(s) for Patients : <br/>");


		StringBuilder toothCodesErr = new StringBuilder("<br/> TOOTH NUMBER ERROR: Claims found without tooth number observation(s).<br/> " +
										"Please check the Service(s) for for Patients : <br/>");

		StringBuilder toothNoCodesErr = new StringBuilder("<br/> TOOTH NUMBER CODES ERROR: Claims found without tooth number observation code(s).<br/> " +
										"Please check the Observation(s) for Service(s) : <br/>");

		StringBuilder toothNoUniversalCodeErr = new StringBuilder("<br/> TOOTH NUMBER NO UNIVERSAL CODE ERROR: Claims found without universal code.<br/> " +
										"Please check the Observation(s) for Service(s) : <br/>");

		StringBuilder diagCodesErr = new StringBuilder("<br/> DIAG CODES ERROR: Claims found without observation codes (or) values.<br/> " +
										"Please check the Test(s) for Patients : <br/>");

		StringBuilder noActivitiesErr = new StringBuilder("<br/> ZERO ACTIVITIES ERROR: Submission has claims with no activities.<br/> " +
										"Please check the claims in the submission: <br/>");

		StringBuilder govtIdNoErr = new StringBuilder("<br/> EMIRATES ID ERROR: Claims without "+govenmtIdLabel+" (or) "+govenmtIdTypeLabel+".<br/> " +
										"Please check the "+govenmtIdLabel+" (or) "+govenmtIdTypeLabel+" for Patients: <br/>");

		StringBuilder drugCodesErr = new StringBuilder("<br/> DRUG CODES ERROR: Claims found without activity codes(Drugs). <br/>" +
										"Please check the Drugs : <br/>");

		StringBuilder cardFileTypesErr = new StringBuilder("<br/> CARD FILE TYPE ERROR: Claims without card file type/invalid file types. <br/>" +
										"Please upload files of type (GIF/PNG/JPG/JPEG/PDF/RTF/TXT/TIFF) for Patients : <br/>");

		StringBuilder receiverErr = new StringBuilder("<br/> RECEIVER ERROR: Claim does not contain receiver. <br/>" +
										"Please check tpa code for sponsor : <br/> ");

		StringBuilder noCompanyErr = new StringBuilder("<br/> COMPANY ERROR: Claim does not contain insurance company. <br/>" +
										"Please check insurance company for the claim : <br/> ");

		StringBuilder payerErr = new StringBuilder("<br/> PAYER ERROR: Claim does not contain payer. <br/>" +
										"Please check company code for insurance company : <br/> ");

		//StringBuilder weightValueErr = new StringBuilder("<br/> WEIGHT VALUE ERROR: Baby birth weight not provided<br/> ");

		List<BasicDynaBean> claims = submitdao.findAllClaims(submission_batch_id, isResubmission,healthAuthority);

		List<BasicDynaBean> diagnosis  = null;
		List<BasicDynaBean> bills  = null;
		List<BasicDynaBean> charges  = null;
		Map<String, List> observationsMap = new HashMap<String, List>();
		List<String> visitIdList = new ArrayList<String>();
		List<String> cliniciansList = new ArrayList<String>();
		List<String> clinicianIdsList = new ArrayList<String>();
		List<String> orderingCliniciansList = new ArrayList<String>();

		List<Eclaim> claimsList = new ArrayList<Eclaim>();
		boolean claimsHaveCharges = false;

		try {

			for (BasicDynaBean claim : claims) {
				//Eclaim eclaim = new Eclaim();

				List<BasicDynaBean> supportedAttachments  = new ArrayList<BasicDynaBean>();
				String claim_id = (String)claim.get("claim_id");
				String sponsor_id = (String)claim.get("sponsor_id"); //(String)(new GenericDAO("bill_charge_claim").findByKey("claim_id", claim_id).get("sponsor_id"));
				String mr_no = (String)claim.get("patient_id");
				String visit_type = (String)claim.get("visit_type");
				String main_visit_id = (String)claim.get("main_visit_id");
				String claim_patient_id = (String)claim.get("claim_patient_id");
				BasicDynaBean babyDetails = PatientDetailsDAO.getBabyDateOfBirtsAndSalutationDetails(mr_no, claim_patient_id);
				String salutation = null;
				if(babyDetails != null)
					salutation = (String)babyDetails.get("salutation");
				String emirates_id_number = claim.get("emirates_id_number") != null ? (String)claim.get("emirates_id_number") : null;
				String encounter_type = claim.get("encounter_type") != null ? ((Integer)claim.get("encounter_type")).toString() :null;
				String encounter_start_type = claim.get("encounter_start_type") != null ? ((Integer)claim.get("encounter_start_type")).toString() :null;
				String encounter_end_type = claim.get("encounter_end_type") != null ? ((Integer)claim.get("encounter_end_type")).toString() :null;
				String start_date = claim.get("start_date") != null ? ((String)claim.get("start_date")).toString() :null;
				String end_date = claim.get("end_date") != null ? ((String)claim.get("end_date")).toString() :null;
				String resubmissionType = claim.get("resubmission_type") != null ? ((String)claim.get("resubmission_type")).toString() :null;
				int patient_policy_id = claim.get("patient_policy_id") != null ? (Integer)claim.get("patient_policy_id") : 0;
				int plan_id = claim.get("plan_id") != null ? (Integer)claim.get("plan_id") : 0;
				Integer centerId = (Integer)claim.get("batch_center_id");
				HealthAuthorityDTO healthAuthorityPreferance = HealthAuthorityPreferencesDAO.getHealthAuthorityPreferences(CenterMasterDAO.getHealthAuthorityForCenter(centerId));
				eclaimXMLSchema = healthAuthorityPreferance.getHealth_authority();
				eclaimXMLSchema = eclaimXMLSchema != null ? eclaimXMLSchema : "HAAD";
				defaultDiagnosisCodeType = healthAuthorityPreferance.getDiagnosis_code_type();
				defaultDiagnosisCodeType = defaultDiagnosisCodeType != null ? defaultDiagnosisCodeType : "ICD";

				if (isResubmission.equals("Y")) {
					Map attachmentMap = reconcildao.getAttachment(claim_id);
					InputStream file = (InputStream)attachmentMap.get("Content");
					if (file != null) {
						String attachment =  submitdao.convertToBase64Binary(file);
						if (attachment == null)
						{
							errorsMap.put("ATTACHMENT ERROR:", attachmentErr.append(submitdao.urlString(path, "attachment", claim_id, null)));
							attachmentErr.append(" , ");
						}
					}
				}

				if (emirates_id_number == null || emirates_id_number.equals("")) {
					errorsMap.put("EMIRATES ID ERROR:", govtIdNoErr.append(submitdao.urlString(path, "pre-registration", mr_no, null)));
					govtIdNoErr.append("  ,  ");
				}

				if (((visit_type.equals("i") && encTypePref.equals("IP"))
						|| (visit_type.equals("o") && encTypePref.equals("OP"))
						|| encTypePref.equals("RQ")) && (encounter_type == null || encounter_type.equals("0"))) {
					errorsMap.put("ENCOUNTERS ERROR:", encountersErr.append(submitdao.urlString(path, "diagnosis", claim_patient_id, null)));
					encountersErr.append("  ,  ");
				}

				if ((visit_type.equals("i") && encStartEndTypePref.equals("IP"))
						|| (visit_type.equals("o") && encStartEndTypePref.equals("OP"))
						|| encStartEndTypePref.equals("RQ")) {

					if (encounter_start_type == null || encounter_start_type.equals("0")) {
						errorsMap.put("ENCOUNTERS START ERROR:", encountersStartErr.append(submitdao.urlString(path, "diagnosis", claim_patient_id, null)));
						encountersStartErr.append("  ,  ");
					}

					if (encounter_end_type == null || encounter_end_type.equals("0")) {
						errorsMap.put("ENCOUNTERS END ERROR:", encountersEndErr.append(submitdao.urlString(path, "diagnosis", claim_patient_id, null)));
						encountersEndErr.append("  ,  ");
					}
				}

				if (visit_type != null && visit_type.equals("i")) {

					if (end_date == null || end_date.trim().equals("")) {
						errorsMap.put("ENCOUNTERS END DATE ERROR:", encountersEndDateErr.append(submitdao.urlString(path, "adt", mr_no, null)));
						encountersEndDateErr.append("  ,  ");
					}
				}
				
				/*
				 * Validation for Primary diagnosis While generating/uploading E-claim xml. 
				 */
				diagnosis = submitdao.findPrimaryDiagnosis(claim_patient_id);
				if (diagnosis == null || diagnosis.size() == 0) {
					errorsMap.put("PRIMARY DIAGNOSIS ERROR:", primaryDiagnosisCodesErr.append(submitdao.urlString(path, "diagnosis", claim_patient_id, null)));
					primaryDiagnosisCodesErr.append("  ,  ");
				}
				diagnosis = submitdao.findAllCoderDiagnosis(claim_patient_id);

				if (diagnosis == null || diagnosis.size() == 0) {
					errorsMap.put("DIAGNOSIS ERROR:", diagnosisCodesErr.append(submitdao.urlString(path, "diagnosis", claim_patient_id, null)));
					diagnosisCodesErr.append("  ,  ");

				}else {
					for (BasicDynaBean diag : diagnosis) {
						String code_type = (String)diag.get("code_type");
						String icd_code = (String)diag.get("icd_code");
						String diag_type = (String)diag.get("diag_type");

						if (icd_code == null || icd_code.equals("")) {
							errorsMap.put("DIAGNOSIS ERROR:", diagnosisCodesErr.append(submitdao.urlString(path, "diagnosis", claim_patient_id, null)));
							diagnosisCodesErr.append(" , ");
						}

						/*if (mod_eclaim) {
							//64625 proposed code chnages to be here
							// For HAAD Claim -- Diagnosis code types is ICD.
							BasicDynaBean visitBean = new GenericDAO("patient_registration").findByKey("patient_id", claim_patient_id);
							String codificationStatus = null;
							if (null != visitBean && null != visitBean.get("codification_status"))
								codificationStatus = (String)visitBean.get("codification_status");
							if (code_type.equalsIgnoreCase(defaultDiagnosisCodeType)) {
							}else {
								if(null != codificationStatus && codificationStatus.equals("P")){
									errorsMap.put("DIAGNOSIS CODE TYPE ERROR:", haadDiagnosisCodeTypeErr.append(submitdao.urlString(path, "diagnosis", claim_patient_id, null)));
									haadDiagnosisCodeTypeErr.append(" , ");
								}
							}
						}*/
					}
				}

				bills  = submitdao.findAllBills(claim_id);

				List<BasicDynaBean> allBillCharges = new ArrayList<BasicDynaBean>();

				if (bills == null || bills.size() == 0) {
					errorsMap.put("ZERO BILLS ERROR:", noBillsErr.append(submitdao.urlString(path, "claim", claim_id, null)));
					noBillsErr.append(" , ");

				}else {
					for (BasicDynaBean bill : bills) {
						String bill_no = (String)bill.get("bill_no");
						String status  = (String)bill.get("status");
						//
						if (isResubmission.equals("Y") && (resubmissionType.equalsIgnoreCase(
								"internal complaint") || resubmissionType.equalsIgnoreCase(
										"reconciliation"))) {
							charges  = claimdao.findAllChargesForResub(bill_no, claim_id);
						}else {
							charges  = claimdao.findAllCharges(bill_no, claim_id);
						}

						if (status.equals("A")) {
							errorsMap.put("BILLS OPEN ERROR:", billStatusErr.append(submitdao.urlString(path, "claim", claim_id, null)));
							billStatusErr.append(" , ");
						}

						if (charges != null && (status.equals("F") || status.equals("C"))) {

							allBillCharges.addAll(charges);

							/* Check for codes for each charge */
							for (BasicDynaBean charge : charges) {
								List<BasicDynaBean> observations  = null;
								String activity_charge_id = (String)charge.get("activity_charge_id");
								String charge_id          = (String)charge.get("charge_id");
								String item_code          = (String)charge.get("item_code");
								String charge_group       = (String)charge.get("charge_group");
								String charge_head        = (String)charge.get("charge_head");
								String act_desc           = (String)charge.get("act_description");
								String act_desc_id        = (String)charge.get("act_description_id");
								String posted_date        = (String)charge.get("posted_date");
								String item_id            = (String)charge.get("item_id");

								String doctor_license_number= (String)charge.get("doctor_license_number");
								String doctor_id			= (String)charge.get("doctor_id");
								String doctor_name			= (String)charge.get("doctor_name");
								String doctor_type			= (String)charge.get("doctor_type");
								
								String prescribing_doctor_license_number = (String)charge.get("prescribing_doctor_license_number");
								String prescribing_doctor_name = (String)charge.get("prescribing_doctor_name");
								String prescribing_doctor_id = (String)charge.get("prescribing_doctor_id");

								if (doctor_name == null || doctor_name.trim().equals("")){

									if (!cliniciansList.contains(doctor_name) && !visitIdList.contains(claim_patient_id)) {
										errorsMap.put("NO CLINICIANS ERROR:", noCliniciansErr.append(submitdao.urlString(path, "patient", claim_patient_id, null)));
										noCliniciansErr.append(" , ");

										cliniciansList.add(doctor_name);
										visitIdList.add(claim_patient_id);
									}

								}else if (doctor_license_number == null || doctor_license_number.trim().equals("")) {

									if (!clinicianIdsList.contains(doctor_license_number) && !visitIdList.contains(claim_patient_id)) {
										if (doctor_type.equals("Doctor")) {
											errorsMap.put("CLINICIAN ERROR:", clinicianIdErr.append(submitdao.urlString(path, "doctor", doctor_id, doctor_name)));
										}else {
											errorsMap.put("CLINICIAN ERROR:", clinicianIdErr.append(submitdao.urlString(path, "referral", doctor_id, doctor_name)));
										}
										clinicianIdErr.append(" , ");

										clinicianIdsList.add(doctor_license_number);
										visitIdList.add(claim_patient_id);
									}
								}else if (healthAuthorityPreferance.getPresc_doctor_as_ordering_clinician().equals("Y") && prescribing_doctor_name != null && (prescribing_doctor_license_number == null || prescribing_doctor_license_number.trim().equals(""))) {

									if (!orderingCliniciansList.contains(prescribing_doctor_license_number) && !visitIdList.contains(claim_patient_id)) {
										errorsMap.put("ORDERING CLINICIAN ERROR:", orderingClinicianErr.append(submitdao.urlString(path, "doctor", prescribing_doctor_id, prescribing_doctor_name)));
										
										orderingClinicianErr.append(" , ");

										orderingCliniciansList.add(prescribing_doctor_license_number);
										visitIdList.add(claim_patient_id);
									}
								}

								BasicDynaBean chrgBean = ChargeHeadsDAO.getChargeHeadBean(charge_head);
								if (chrgBean != null && !charge_head.equals("ADJDRG")) {
									String codificationSupported = chrgBean.get("codification_supported") != null &&
																	!chrgBean.get("codification_supported").equals("") ?
																		(String)chrgBean.get("codification_supported") : "N";

									if (item_code == null || item_code.trim().equals("")) {
										if (activity_charge_id.startsWith("A")) {
											if (codificationSupported != null && codificationSupported.equals("Y")) {
												errorsMap.put("CODES ERROR:", codesErr.append(submitdao.urlString(path, "diagnosis", claim_patient_id, null)));
												codesErr.append("( "+act_desc+ ", Posted Date: "+posted_date+" ), <br/> ");
											}else {
												errorsMap.put("CODES ERROR:", codesErr.append(submitdao.urlString(path, "bill", bill_no, null)));
												codesErr.append("( "+act_desc+ ", Posted Date: "+posted_date+" ), <br/> ");
											}
										}else if (activity_charge_id.startsWith("P")) {
											Map idMap = new HashMap();
											idMap.put("chargeId", charge.get("charge_id"));
											idMap.put("billNo", charge.get("bill_no"));
											idMap.put("saleItemId", ((String)charge.get("activity_charge_id")).split("-")[2]);
											errorsMap.put("DRUG CODES ERROR:", drugCodesErr.append(submitdao.urlString(path, "drug", ((String)charge.get("activity_charge_id")).split("-")[2] , act_desc)));
											drugCodesErr.append(" , ");
										}
									}
								}

								observations = submitdao.findAllObservations(charge_id, sponsor_id,eclaimXMLSchema);

								/* Check for tooth number if required if dental service i.e SNP */
								if(charge_group.equalsIgnoreCase("SNP")){
									BasicDynaBean service = new ServiceMasterDAO().findByKey("service_id", act_desc_id);
									if(service!= null && service.get("tooth_num_required").equals("Y")){
										if(observations == null || observations.size() == 0){
											errorsMap.put("TOOTH NUMBER ERROR:", toothCodesErr.append(submitdao.urlString(path, "diagnosis", claim_patient_id, null)));
											toothCodesErr.append("<br/>Service :" + bill_no + "( "+act_desc+ ", Posted Date: "+posted_date+" ), <br/> ");
										}
										int numOfDentalCodes = 0;
										if(observations.size() > 0 ){
											for (BasicDynaBean observation : observations) {
												String result_code  = (String)observation.get("code");
												String result_type = (String)observation.get("type");
												BasicDynaBean haadBean =  new GenericDAO("mrd_supported_code_types").findByKey("code_type", result_type);
												Integer haadCode = haadBean == null? null: (Integer)haadBean.get("haad_code");
												if(haadCode != null && haadCode.intValue() == 16) {
													++numOfDentalCodes;
												}
												if (result_code == null || result_code.equals("")) {
													errorsMap.put("TOOTH NUMBER CODES ERROR:", toothNoCodesErr.append(submitdao.urlString(path, "diagnosis", claim_patient_id, null)));
													toothNoCodesErr.append("<br/>Service :" + bill_no + "( "+act_desc+ ", Posted Date: "+posted_date+" ), <br/> ");
												}
											}
										}
										if(numOfDentalCodes == 0) {
											errorsMap.put("TOOTH NUMBER NO UNIVERSAL CODE ERROR:", toothNoUniversalCodeErr.append(submitdao.urlString(path, "diagnosis", claim_patient_id, null)));
											toothNoUniversalCodeErr.append("<br/>Service :" + bill_no + "( "+act_desc+ ", Posted Date: "+posted_date+" ), <br/> ");
										}
									}
								}

								/* Check for complaint observation for DHA claim if consultation i.e DOC */
								if (!visit_type.equals("i") && eclaimXMLSchema.equals("DHA") && charge_group.equals("DOC")) {
									if (observations == null || observations.size() == 0 ) {
										errorsMap.put("CONS COMPLAINT CODES ERROR:", consComplaintCodesErr.append(submitdao.urlString(path, "diagnosis", claim_patient_id, null)));
										consComplaintCodesErr.append("<br/>Consultation :" + bill_no + "( "+act_desc+ ", Posted Date: "+posted_date+" ), <br/> ");
									}
								}
								//check observation for drg if baby patient
								/*if(charge_group.equals("DRG") && eclaimXMLSchema.equals("HAAD") && salutation != null && salutation.equalsIgnoreCase("Baby")) {
									if (observations.size() == 0 ) {
										errorsMap.put("WEIGHT VALUE ERROR:", weightValueErr.append(submitdao.urlString(path, "drg", claim_patient_id, null)));
									} else {
										for (BasicDynaBean observation : observations) {
											String result_value  = (String)observation.get("value");
											if (result_value.equals("") || result_value == null) {
												errorsMap.put("WEIGHT VALUE ERROR:", weightValueErr.append(submitdao.urlString(path, "drg", claim_patient_id, null)));
											}
										}
									}
								}*/

								if (observations != null && observations.size() > 0 ) {

									/* Check for value for each observation if consultation i.e DOC  */
									if (charge_group.equals("DOC")) {
										boolean hasPresentingComplaint = false;
										for (BasicDynaBean observation : observations) {
											String result_code  = (String)observation.get("code");
											String result_value  = (String)observation.get("value");
											if (result_code != null && result_code.equals("Presenting-Complaint"))
												hasPresentingComplaint = true;
											if ((result_code != null && !result_code.equals("")) && (result_value == null || result_value.equals(""))) {
												errorsMap.put("CONS CODES ERROR:", consCodesErr.append(submitdao.urlString(path, "diagnosis", claim_patient_id, null)));
												consCodesErr.append("<br/>Consultation :" + bill_no + "( "+act_desc+ ", Posted Date: "+posted_date+" ), <br/> ");
											}
										}
										if (!visit_type.equals("i") && eclaimXMLSchema.equals("DHA") && !hasPresentingComplaint) {
											errorsMap.put("CONS COMPLAINT CODES ERROR:", consComplaintCodesErr.append(submitdao.urlString(path, "diagnosis", claim_patient_id, null)));
											consComplaintCodesErr.append("<br/>Consultation :" + bill_no + "( "+act_desc+ ", Posted Date: "+posted_date+" ), <br/> ");
										}
									}

									/* Check for codes for each observation if lab test i.e LTDIA */
									if (charge_head.equals("LTDIA")) {
										for (BasicDynaBean observation : observations) {
											String result_code  = (String)observation.get("code");
											String result_value  = (String)observation.get("value");

											if ((result_code != null && !result_code.equals("")) && (result_value == null || result_value.equals(""))) {

												errorsMap.put("DIAG CODES ERROR:", diagCodesErr.append(submitdao.urlString(path, "diagnosis", claim_patient_id, null)));
												diagCodesErr.append("<br/>Test :" + bill_no + "( "+act_desc+ ", Posted Date: "+posted_date+" ), <br/> ");
											}
										}
									}

									if(salutation != null && salutation.equalsIgnoreCase("Baby") && charge_group.equals("DRG")) {
										if(eclaimXMLSchema.equals("HAAD"))
											observationsMap.put(charge_id, observations);
									}else {
										observationsMap.put(charge_id, observations);
									}
								}// Observations
							}

						if (allBillCharges.size() > 0)
							claimsHaveCharges = true;
						}// Charges
					}
				}// Bills

			}// Claims

		} catch (Exception e) {
			throw e;
		}

		if (!claimsHaveCharges) {
			errorsMap.put("ZERO ACTIVITIES ERROR:", noActivitiesErr.append(submitdao.urlString(path, "submission", submission_batch_id, null)));
		}

		headerbean.set("claims_count", claimsList.size());

	}

}