package com.insta.hms.billing;

import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.security.SecurityService;
import com.insta.hms.genericdocuments.GenericDocumentsDAO;
import com.insta.hms.master.Accounting.ChargeHeadsDAO;
import com.insta.hms.master.HealthAuthorityPreferences.HealthAuthorityDTO;
import com.insta.hms.master.HealthAuthorityPreferences.HealthAuthorityPreferencesDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDTO;
import com.insta.hms.master.ServiceMaster.ServiceMasterDAO;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaBeanMapDecorator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ClaimProcessor{

	ClaimSubmissionDAO submitdao = new ClaimSubmissionDAO();
	ClaimReconciliationDAO reconcildao = new ClaimReconciliationDAO();
	GenericDocumentsDAO gendao = new GenericDocumentsDAO();
	ClaimGeneratorHelper helper = new ClaimGeneratorHelper();
	ClaimDAO claimdao = new ClaimDAO();
	ClaimActivityDAO claimActivityDAO = new ClaimActivityDAO();
	GenericDAO mrdCodeTypesDao = new GenericDAO("mrd_supported_code_types");
	GenericDAO accountGroupDao = new GenericDAO("account_group_master");
	GenericDAO healthAuthPrefDao = new GenericDAO("health_authority_preferences");
	ServiceMasterDAO serviceMasterDao = new ServiceMasterDAO();
	
  public static final String PRESENT_ON_ADMISSION_ERROR = "<br/> PRESENT ON ADMISSION ERROR: Diagnosis must contain Present on Admission value. <br/>";

  public static final String TOOTH_CODES_ERROR = "<br/> TOOTH NUMBER ERROR: Claims found without tooth number observation(s).<br/> "
      + "Please check the Service(s) for for Patients : <br/>";

  public static final String TOOTH_NO_CODES_ERR = "<br/> TOOTH NUMBER CODES ERROR: Claims found without tooth number observation code(s).<br/> "
      + "Please check the Observation(s) for Service(s) : <br/>";

  public static final String TOOTH_NO_UNIVERSAL_CODE_ERR = "<br/> TOOTH NUMBER NO UNIVERSAL CODE ERROR: Claims found without universal code.<br/> "
      + "Please check the Observation(s) for Service(s) : <br/>";

  public static final String DIAGNOSIS_CODE_ERR = "<br/> DIAGNOSIS ERROR: Claims without diagnosis codes. <br/>"
      + "Please enter diagnosis codes for Patients : <br/>";

  public static final String PRIMARY_DIAGNOSIS_CODE_ERR = "<br/> PRIMARY DIAGNOSIS ERROR: Claims without Primary diagnosis codes. <br/>"
      + "Please enter Primary diagnosis codes for Patients : <br/>";
  
	static Logger logger = LoggerFactory.getLogger(ClaimProcessor.class);
	
	public void process(String submission_batch_id, String isResubmission, String healthAuthority,
			Map<String,StringBuilder> errorsMap, String path, BasicDynaBean headerbean, OutputStream stream,HashMap  urlActionMap)
			throws SQLException{


		RegistrationPreferencesDTO regPref = RegistrationPreferencesDAO.getRegistrationPreferences();
		BasicDynaBean submissionBean = submitdao.findByKey("submission_batch_id", submission_batch_id);
		Integer accountGroupId = (Integer) submissionBean.get("account_group");

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

		StringBuilder diagCodesErr = new StringBuilder("<br/> DIAG CODES ERROR: Claims found without observation codes (or) values.<br/> " +
										"Please check the Test(s) for Patients : <br/>");

		StringBuilder noActivitiesErr = new StringBuilder("<br/> ZERO ACTIVITIES ERROR: Submission has claims with no activities.<br/> " +
										"Please check the claims : <br/>");

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

		List<BasicDynaBean> claims = submitdao.findAllClaims(submission_batch_id, isResubmission,healthAuthority);

		List<BasicDynaBean> diagnosis  = null;
		List<BasicDynaBean> bills  = null;
		List<BasicDynaBean> charges  = null;
		Map<String, List<Map>> observationsMapForFTL = new HashMap<>();
		List<String> visitIdList = new ArrayList<>();
		List<String> cliniciansList = new ArrayList<>();
		List<String> clinicianIdsList = new ArrayList<>();
		List<String> orderingCliniciansList = new ArrayList<>();

		List<Eclaim> claimsList = new ArrayList<>();
		boolean claimsHaveCharges = false;
		List<BasicDynaBean> accountGrpList;
		HealthAuthorityDTO healthAuthorityPreferance = HealthAuthorityPreferencesDAO.getHealthAuthorityPreferences(healthAuthority);
		String eclaimXMLSchema = healthAuthorityPreferance.getHealth_authority();
		eclaimXMLSchema = eclaimXMLSchema != null ? eclaimXMLSchema : "HAAD";
		long chargesTime=0;
		long claimsTime = 0;
		List<Map> allEclaimMap =new ArrayList<>();
		Map<String, List<Map>> obBeanMap = new HashMap<>();
		try {
			long claimsStart = System.currentTimeMillis();
			for (BasicDynaBean claim : claims) {
				Eclaim eclaim = new Eclaim();
				String claim_id = (String)claim.get("claim_id");
				String sponsor_id = (String)claim.get("sponsor_id");
				String mr_no = (String)claim.get("patient_id");
				String visitType = (String)claim.get("visit_type");
				String claim_patient_id = (String)claim.get("claim_patient_id");
				String emirates_id_number = claim.get("emirates_id_number") != null ? (String)claim.get("emirates_id_number") : null;
				String encounter_type = claim.get("encounter_type") != null ? ((Integer)claim.get("encounter_type")).toString() :null;
				String encounter_start_type = claim.get("encounter_start_type") != null ? ((Integer)claim.get("encounter_start_type")).toString() :null;
				String encounter_end_type = claim.get("encounter_end_type") != null ? ((Integer)claim.get("encounter_end_type")).toString() :null;
				
				String end_date = claim.get("end_date") != null ? ((String)claim.get("end_date")).toString() :null;
				eclaim.setClaim(claim);//******

				String encounterEndDate = claim.get("end_date") != null ? ((String)claim.get("end_date")).toString() :null;
				String resubmissionType = claim.get("resubmission_type") != null ? ((String)claim.get("resubmission_type")).toString() :null;
				Integer priority = (Integer)claim.get("priority");
				eclaim.setClaim(claim);
				if (isResubmission.equals("Y")) {
					Map attachmentMap = reconcildao.getAttachment(claim_id);
          eclaim.setIsResubmission(isResubmission.equals("Y") ? true : false);
					InputStream file = (InputStream)attachmentMap.get("Content");
					if (file != null) {
						String attachment =  submitdao.convertToBase64Binary(file);
						if (attachment != null){
							eclaim.setAttachment(attachment);
						}else{
							errorsMap.put("ATTACHMENT ERROR:", attachmentErr.append(submitdao.urlString(path, "attachment", claim_id, null,urlActionMap)));
							attachmentErr.append(" , ");
						}
					}
				}

				if (emirates_id_number == null || emirates_id_number.equals("")) {
					errorsMap.put("EMIRATES ID ERROR:", govtIdNoErr.append(submitdao.urlString(path, "pre-registration", mr_no, null,urlActionMap)));
					govtIdNoErr.append("  ,  ");
				}

				if (((visitType.equals("i") && encTypePref.equals("IP"))
						|| (visitType.equals("o") && encTypePref.equals("OP"))
						|| encTypePref.equals("RQ")) && (encounter_type == null || encounter_type.equals("0"))) {
					errorsMap.put("ENCOUNTERS ERROR:", encountersErr.append(submitdao.urlString(path, "diagnosis", claim_patient_id, null,urlActionMap)));
					encountersErr.append("  ,  ");
				}

				if ((visitType.equals("i") && encStartEndTypePref.equals("IP"))
						|| (visitType.equals("o") && encStartEndTypePref.equals("OP"))
						|| encStartEndTypePref.equals("RQ")) {

					if (encounter_start_type == null || encounter_start_type.equals("0")) {
						errorsMap.put("ENCOUNTERS START ERROR:", encountersStartErr.append(submitdao.urlString(path, "diagnosis", claim_patient_id, null,urlActionMap)));
						encountersStartErr.append("  ,  ");
					}

					if (encounter_end_type == null || encounter_end_type.equals("0")) {
						errorsMap.put("ENCOUNTERS END ERROR:", encountersEndErr.append(submitdao.urlString(path, "diagnosis", claim_patient_id, null,urlActionMap)));
						encountersEndErr.append("  ,  ");
					}
				}

				if (visitType != null && visitType.equals("i")) {
					if (end_date == null || end_date.trim().equals("")) {
						errorsMap.put("ENCOUNTERS END DATE ERROR:", encountersEndDateErr.append(submitdao.urlString(path, "adt", mr_no, null,urlActionMap)));
						encountersEndDateErr.append("  ,  ");
					}
				}

        diagnosis = findAllDiagnosis(claim_patient_id, (Integer) claim.get("claim_account_group"));
        eclaim.setDiagnosis(diagnosis);
        // validate the diagnosis codes and update errorsMap if required
        validateDiagnosis(claim, diagnosis, errorsMap, path, urlActionMap);
				bills  = submitdao.findAllBills(claim_id);
				eclaim.setBills(bills);
				List<BasicDynaBean> allBillCharges = new ArrayList<>();
				long chargesStart = System.currentTimeMillis();
				if (bills == null || bills.size() == 0) {
					errorsMap.put("ZERO BILLS ERROR:", noBillsErr.append(submitdao.urlString(path, "claim", claim_id, null,urlActionMap)));
					noBillsErr.append(" , ");

				}else {
					List<String> chHeadList= new ArrayList<>();
					for (BasicDynaBean bill : bills) {
						String bill_no = (String)bill.get("bill_no");
						String status  = (String)bill.get("status");

						if (isResubmission.equals("Y") && (resubmissionType.equalsIgnoreCase("internal complaint")
								|| ("DHA".equals(healthAuthority) && resubmissionType.equalsIgnoreCase("reconciliation"))) ) {
							charges  = claimdao.findAllChargesForResub(bill_no, claim_id);
						} else {
							charges  = claimdao.findAllCharges(bill_no, claim_id);
						}
						obBeanMap = findAllObservationsMap(claim_id,sponsor_id,eclaimXMLSchema);

						if (status.equals("A")) {
							errorsMap.put("BILLS OPEN ERROR:", billStatusErr.append(submitdao.urlString(path, "claim", claim_id, null,urlActionMap)));
							billStatusErr.append(" , ");
						}

						if (charges != null && (status.equals("F") || status.equals("C"))) {
							chHeadList= findAllChargeHead(charges);
							Map<String,BasicDynaBean> chHeadMap=getChargeHeadMap(chHeadList);
							allBillCharges.addAll(charges);
							
							/* Check for codes for each charge */
							for (BasicDynaBean charge : charges) {
								List<Map> observations  = null;
								String activity_charge_id = (String)charge.get("activity_charge_id");
								String claim_activity_id  = (String)charge.get("claim_activity_id");
								String charge_id          = (String)charge.get("charge_id");
								String item_code          = (String)charge.get("item_code");
								String charge_group       = (String)charge.get("charge_group");
								String charge_head        = (String)charge.get("charge_head");
								String act_desc           = (String)charge.get("act_description");
								String posted_date        = (String)charge.get("posted_date");
								String doctor_license_number= (String)charge.get("doctor_license_number");
								String doctor_id			= (String)charge.get("doctor_id");
								String doctor_name			= (String)charge.get("doctor_name");
								String doctor_type			= (String)charge.get("doctor_type");
								String prescribing_doctor_license_number = (String)charge.get("prescribing_doctor_license_number");
								String prescribing_doctor_name = (String)charge.get("prescribing_doctor_name");
								String prescribing_doctor_id = (String)charge.get("prescribing_doctor_id");

								if (doctor_name == null || doctor_name.trim().equals("")){
									if (!cliniciansList.contains(doctor_name) && !visitIdList.contains(claim_patient_id)) {
										errorsMap.put("NO CLINICIANS ERROR:", noCliniciansErr.append(submitdao.urlString(path, "patient", claim_patient_id, null,urlActionMap)));
										noCliniciansErr.append(" , ");

										cliniciansList.add(doctor_name);
										visitIdList.add(claim_patient_id);
									}

								}else if (doctor_license_number == null || doctor_license_number.trim().equals("")) {
									if (!clinicianIdsList.contains(doctor_license_number) && !visitIdList.contains(claim_patient_id)) {
										if (doctor_type.equals("Doctor")) {
											errorsMap.put("CLINICIAN ERROR:", clinicianIdErr.append(submitdao.urlString(path, "doctor", doctor_id, doctor_name,urlActionMap)));
										}else {
											errorsMap.put("CLINICIAN ERROR:", clinicianIdErr.append(submitdao.urlString(path, "referral", doctor_id, doctor_name,urlActionMap)));
										}
										clinicianIdErr.append(" , ");

										clinicianIdsList.add(doctor_license_number);
										visitIdList.add(claim_patient_id);
									}
								}else if (healthAuthorityPreferance.getPresc_doctor_as_ordering_clinician().equals("Y") && prescribing_doctor_name != null && (prescribing_doctor_license_number == null || prescribing_doctor_license_number.trim().equals(""))) {

									if (!orderingCliniciansList.contains(prescribing_doctor_license_number) && !visitIdList.contains(claim_patient_id)) {
										errorsMap.put("ORDERING CLINICIAN ERROR:", orderingClinicianErr.append(submitdao.urlString(path, "doctor", prescribing_doctor_id, prescribing_doctor_name,urlActionMap)));										
										orderingClinicianErr.append(" , ");

										orderingCliniciansList.add(prescribing_doctor_license_number);
										visitIdList.add(claim_patient_id);
									}
								}
								BasicDynaBean chrgBean = chHeadMap.get(charge_head);
								if (chrgBean != null && !charge_head.equals("ADJDRG")) {
									String codificationSupported = chrgBean.get("codification_supported") != null &&
																	!chrgBean.get("codification_supported").equals("") ?
																		(String)chrgBean.get("codification_supported") : "N";

									if (item_code == null || item_code.trim().equals("")) {
										if (activity_charge_id.startsWith("A")) {
											if (codificationSupported != null && codificationSupported.equals("Y")) {
												errorsMap.put("CODES ERROR:", codesErr.append(submitdao.urlString(path, "diagnosis", claim_patient_id, null,urlActionMap)));
												codesErr.append("( "+act_desc+ ", Posted Date: "+posted_date+" ), <br/> ");
											}else {
												errorsMap.put("CODES ERROR:", codesErr.append(submitdao.urlString(path, "bill", bill_no, null,urlActionMap)));
												codesErr.append("( "+act_desc+ ", Posted Date: "+posted_date+" ), <br/> ");
											}
										}else if (activity_charge_id.startsWith("P")) {
											Map idMap = new HashMap();
											idMap.put("chargeId", charge.get("charge_id"));
											idMap.put("billNo", charge.get("bill_no"));
											idMap.put("saleItemId", ((String)charge.get("activity_charge_id")).split("-")[2]);
											errorsMap.put("DRUG CODES ERROR:", drugCodesErr.append(submitdao.urlString(path, "drug", ((String)charge.get("activity_charge_id")).split("-")[2] , act_desc,urlActionMap)));
											drugCodesErr.append(" , ");
										}
									}
								}
								
								// Here we retrieve a map of charge_id to observations in order to do validation against each observation.
								observations = obBeanMap.get(charge_id);

								/* Check for tooth number if required if dental service i.e SNP */
								if(charge_group.equalsIgnoreCase("SNP")){
								  validateDentalService(bill_no, observations, charge, errorsMap, claim, urlActionMap, path);
								}

								/* Check for complaint observation for DHA claim if consultation i.e DOC */
								if (!visitType.equals("i") && eclaimXMLSchema.equals("DHA") && charge_group.equals("DOC")) {
									if (observations == null || observations.size() == 0 ) {
										errorsMap.put("CONS COMPLAINT CODES ERROR:", consComplaintCodesErr.append(submitdao.urlString(path, "diagnosis", claim_patient_id, null,urlActionMap)));
										consComplaintCodesErr.append("<br/>Consultation :" + bill_no + "( "+act_desc+ ", Posted Date: "+posted_date+" ), <br/> ");
									}
								}

								if (observations != null && !observations.isEmpty() ) {

								  
									/* Check for value for each observation if consultation i.e DOC  */
									if (charge_group.equals("DOC")) {
										boolean hasPresentingComplaint = false;
										for (Map observation : observations) {
											String result_code  = (String)observation.get("code");
											String result_value  = (String)observation.get("value");
											if (result_code != null && result_code.equals("Presenting-Complaint"))
												hasPresentingComplaint = true;
											if ((result_code != null && !result_code.equals("")) && (result_value == null || result_value.equals(""))) {
												errorsMap.put("CONS CODES ERROR:", consCodesErr.append(submitdao.urlString(path, "diagnosis", claim_patient_id, null,urlActionMap)));
												consCodesErr.append("<br/>Consultation :" + bill_no + "( "+act_desc+ ", Posted Date: "+posted_date+" ), <br/> ");
											}
										}
										if (!visitType.equals("i") && eclaimXMLSchema.equals("DHA") && !hasPresentingComplaint) {
											errorsMap.put("CONS COMPLAINT CODES ERROR:", consComplaintCodesErr.append(submitdao.urlString(path, "diagnosis", claim_patient_id, null,urlActionMap)));
											consComplaintCodesErr.append("<br/>Consultation :" + bill_no + "( "+act_desc+ ", Posted Date: "+posted_date+" ), <br/> ");
										}
									}

									/* Check for codes for each observation if lab test i.e LTDIA */
									if (charge_head.equals("LTDIA")) {
										for (Map observation : observations) {
											String result_code  = (String)observation.get("code");
											String result_value  = (String)observation.get("value");
											String obsType = (String)observation.get("type");
											//observations of type file can have null value as it will be base64 encoded file (from patient_documents) when xml is generated
											if ((result_code != null && !result_code.equals("")) && (
											    (result_value == null || result_value.equals("")) && (!"File".equalsIgnoreCase(obsType)))) {

												errorsMap.put("DIAG CODES ERROR:", diagCodesErr.append(submitdao.urlString(path, "diagnosis", claim_patient_id, null,urlActionMap)));
												diagCodesErr.append("<br/>Test :" + bill_no + "( "+act_desc+ ", Posted Date: "+posted_date+" ), <br/> ");
											}
										}
									}
								}
								
								/* All observation validations completed. Create the claim_activity_id -> observation map
								 * that will be sent to the FTL for final XML generation.
								 */
								
								setObservationMapForFTL(charge, observationsMapForFTL, observations, healthAuthority, claim);
							}

						if (!allBillCharges.isEmpty())
							claimsHaveCharges = true;
						}// Charges
					}
					if (!claimsHaveCharges) {
						errorsMap.put("ZERO ACTIVITIES ERROR:", noActivitiesErr.append(submitdao.urlString(path, "claim", claim_id, null,urlActionMap)));
					}
					
					//***************
					if (errorsMap == null || errorsMap.isEmpty()) {

            List<BasicDynaBean> allBillChargesNew = new ArrayList<>();

            if (isResubmission.equals("Y") && (resubmissionType.equalsIgnoreCase("internal complaint")
					|| ("DHA".equals(healthAuthority) && resubmissionType.equalsIgnoreCase("reconciliation")))) {
              charges  = claimActivityDAO.findAllChargesForResub(claim_id, (Boolean) submissionBean.get("is_external_pbm_batch"));
            } else {
              charges  = claimActivityDAO.findAllCharges(claim_id, (Boolean) submissionBean.get("is_external_pbm_batch"));
            }
            allBillChargesNew.addAll(charges);

            accountGrpList = accountGroupDao.listAll();
            if (!allBillChargesNew.isEmpty()) {
              eclaim.setCharges(allBillChargesNew);
              eclaim.setObservationsMap(observationsMapForFTL);

              if (isResubmission.equals("Y") && (resubmissionType.equalsIgnoreCase("internal complaint")
					  || ("DHA".equals(healthAuthority) && resubmissionType.equalsIgnoreCase("reconciliation")))) {
                eclaim.setDrgAdjustmentAmt(BigDecimal.ZERO);
              } else {
                eclaim.setDrgAdjustmentAmt(claimActivityDAO.getDRGAdjustmentAmt(claim_id, priority));
              }

              // TODO : It is better to separate the FTLs for HAAD and DHA
              Map bodyMap = new HashMap();
              bodyMap.put("eclaim", eclaim);
              bodyMap.put("eclaim_xml_schema", headerbean.get("eclaim_xml_schema"));
              bodyMap.put("accGrpList", accountGrpList);
              bodyMap.put("healthHAADPref", healthAuthPrefDao.findByKey("health_authority","HAAD").get("presc_doctor_as_ordering_clinician"));
              bodyMap.put("claim_id", claim_id);
              bodyMap.put("priority", priority);
              allEclaimMap.add(bodyMap);
            }
					}
					
				}// Bills
				chargesTime += System.currentTimeMillis() - chargesStart;

			}// Claims
			if (errorsMap == null || errorsMap.isEmpty()) {
				String claim_id = "";
				Integer priority = null;
				for(Map bodyMap: allEclaimMap){
					claim_id = (String)bodyMap.get("claim_id");
					priority = (Integer)bodyMap.get("priority");
					String claimBodyTemplate ="";
					if("HAAD".equalsIgnoreCase(healthAuthority) || "DHA".equalsIgnoreCase(healthAuthority)){
						claimBodyTemplate = "/Eclaim/"+healthAuthority.toLowerCase()+"/EclaimBody.ftl";
						if(claimActivityDAO.getDRGMarginExist(claim_id, priority)) {
							claimBodyTemplate = "/Eclaim/"+healthAuthority.toLowerCase()+"/DrgEclaimBody.ftl";
						}
					}else{
						claimBodyTemplate = "/Eclaim/EclaimBody.ftl";
						if(claimActivityDAO.getDRGMarginExist(claim_id, priority)) {
							claimBodyTemplate = "/Eclaim/DrgEclaimBody.ftl";
						}
					}
					
					helper.addClaimBody(stream, bodyMap, claimBodyTemplate);

					claimsList.add((Eclaim)bodyMap.get("eclaim"));
				}
				
			}
			claimsTime += System.currentTimeMillis() - claimsStart;
			logger.debug("Total Observation time :" + " : " + chargesTime + " : " + claimsTime);

		} catch (Exception e) {
			logger.error("ERROR in Claim Generation" + e);
		}

		headerbean.set("claims_count", claimsList.size());

	}

  /**
   * Validate all diagnosis for a given claim. If any errors are found
   * adds them to the errorsMap provided.
   *
   * @param claim the claim
   * @param diagnosis the diagnosis
   * @param errorsMap the errors map
   * @param path the path
   * @param urlActionMap the url action map
   * @throws ParseException the parse exception
   * @throws SQLException the SQL exception
   */
  private void validateDiagnosis(BasicDynaBean claim, List<BasicDynaBean> diagnosis,
      Map<String, StringBuilder> errorsMap, String path, HashMap urlActionMap)
      throws ParseException, SQLException {
    StringBuilder diagnosisCodesErr = new StringBuilder(DIAGNOSIS_CODE_ERR);
    StringBuilder primaryDiagnosisCodesErr = new StringBuilder(PRIMARY_DIAGNOSIS_CODE_ERR);
    String claimPatientId = (String) claim.get("claim_patient_id");
    if (diagnosis == null || diagnosis.isEmpty()) {
      if (errorsMap.containsKey("DIAGNOSIS ERROR:")) {
        diagnosisCodesErr = errorsMap.get("DIAGNOSIS ERROR:");
        diagnosisCodesErr.append("  ,  ");
      }
      errorsMap.put("DIAGNOSIS ERROR:", diagnosisCodesErr
          .append(submitdao.urlString(path, "diagnosis", claimPatientId, null, urlActionMap)));
    } else {
      boolean primaryDiagnosisExists = false;
      for (BasicDynaBean diag : diagnosis) {
        String icdCode = (String) diag.get("icd_code");
        String diagType = (String) diag.get("diag_type");
        if (diagType.equals("Principal")) {
          primaryDiagnosisExists = true;
        }
        if (icdCode == null || icdCode.equals("")) {
          if (errorsMap.containsKey("DIAGNOSIS ERROR:")) {
            diagnosisCodesErr = errorsMap.get("DIAGNOSIS ERROR:");
            diagnosisCodesErr.append("   ,  ");
          }
          errorsMap.put("DIAGNOSIS ERROR:", diagnosisCodesErr
              .append(submitdao.urlString(path, "diagnosis", claimPatientId, null, urlActionMap)));
        }
        validatePOADiagnosis(claim, errorsMap, diag, path, urlActionMap);
      }
      /*
       * Validation for Primary diagnosis While generating/uploading E-claim xml.
       */
      if (!primaryDiagnosisExists) {
        errorsMap.put("PRIMARY DIAGNOSIS ERROR:", primaryDiagnosisCodesErr
            .append(submitdao.urlString(path, "diagnosis", claimPatientId, null, urlActionMap)));
        diagnosisCodesErr.append("  ,  ");
      }
    }

  }

  /**
   * Validate Present on admission diagnosis if encounter type = 3 or 4.
   * Internally validates only for claims with encounter date > 7-31-2018.
   * As this is the date for the validation Go-Live.
   * Adds errors to errorsMap if found.
   *
   * @param claim the claim
   * @param errorsMap the errors map
   * @param diag the diag
   * @param path the path
   * @param urlActionMap the url action map
   * @throws ParseException the parse exception
   * @throws SQLException the SQL exception
   */
  private void validatePOADiagnosis(BasicDynaBean claim, Map<String, StringBuilder> errorsMap,
      BasicDynaBean diag, String path, HashMap urlActionMap) throws ParseException, SQLException {
    Date encounterStartDateObj = new Date();
    StringBuilder presentOnAdmissionError = new StringBuilder(PRESENT_ON_ADMISSION_ERROR);
    String presentOnAdmission = (String) diag.get("present_on_admission");
    String claimPatientId = (String) claim.get("claim_patient_id");
    String visitType = (String) claim.get("visit_type");
    String encounterType = claim.get("encounter_type") != null
        ? ((Integer) claim.get("encounter_type")).toString()
        : null;
    String encounterStartDateStr = (String) claim.get("start_date"); // This is the patient
    // Registration Date
    Calendar cal = Calendar.getInstance();
    if (StringUtils.isNotEmpty(encounterStartDateStr)) {
      SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
      encounterStartDateObj = formatter.parse(encounterStartDateStr);
      /*
       * This is date HAAD regulations for Present on Admission go Live. All claims with encounter
       * type 3/4 generated after this date will require the additional present on admission check.
       */
      cal.set(2018, 7, 31);
    }

    if (("3".equals(encounterType) || "4".equals(encounterType))
        && !StringUtils.isNotEmpty(presentOnAdmission)
        && (encounterStartDateObj.compareTo(cal.getTime()) > 0) && visitType.equals("i")) {
      if (errorsMap.containsKey("PRESENT ON ADMISSION ERROR:")) {
        presentOnAdmissionError = errorsMap.get("PRESENT ON ADMISSION ERROR:");
        presentOnAdmissionError.append("   ,  ");
      }
      errorsMap.put("PRESENT ON ADMISSION ERROR:", presentOnAdmissionError
          .append(submitdao.urlString(path, "diagnosis", claimPatientId, null, urlActionMap)));
    }
  }

  /**
   * Find all diagnosis for a given claim for a specific accountGroup.
   * For pharmacy claims, diagnoses will be brought from mrd_diagnosis.
   * For Hospital claims, diagnoses will be brought from hospital_claim_diagnosis.
   *
   * @param claimPatientId the claim patient id
   * @param accountGroupId the account group id (pharmacy =3, hospital = 1)
   * @return the list of diagnoses, if accountGroupId is null returns empty list
   * @throws SQLException the SQL exception
   */
  private List<BasicDynaBean> findAllDiagnosis(String claimPatientId, Integer accountGroupId) throws SQLException {
    // if pharmacy claim, get the diagnosis from mrd_diagnosis table
    if (accountGroupId != 1) {
      return submitdao.findAllDiagnosis(claimPatientId);
    } else {
      // bring the diagnoses from hospital_claim_diagnosis table (Coder edited diagnosis)
      return submitdao.findAllCoderDiagnosis(claimPatientId);
    }
  }


  /**
   * Create/Updates the observationMapForFTL that is sent to FTL for final XML Generation
   * Maintains a mapping between claim_activity_id -> observations.
   *
   * @param charge the charge
   * @param observationsMap the observations map
   * @param observations the observations
   * @param usePerdiem
   * @param useDRG
   */
  @SuppressWarnings({ "unchecked" })
  private void setObservationMapForFTL(BasicDynaBean charge, Map<String, List<Map>> observationsMapForFTL,
      List<Map> observations, String healthAuthority, BasicDynaBean claim) {

    List<String> activatedModules = ApplicationContextProvider.getBean(SecurityService.class)
        .getActivatedModules();
    Boolean modEclaimErx = activatedModules.contains("mod_eclaim_erx");
    String claimActivityId = (String) charge.get("claim_activity_id");
    List<Map> observationList = null;
    Map patientShareObservationMap = null;
        
    if (observationsMapForFTL.containsKey(claimActivityId)) {
      observationList = observationsMapForFTL.get((String) charge.get("claim_activity_id"));
      // Need this map to set patient share for combined activities
      patientShareObservationMap = getPatientShareObservations(observationList);
    } else {
      observationList = new ArrayList<>();
    }
		// if hospital charge the add to observationsMapForFTL as claim_activity_id ->
		// observations
		if (((String) charge.get("activity_group")).equalsIgnoreCase("Hospital") && observations != null
				&& !observations.isEmpty()) {
			// call dha specific observations code
			for (Map observation : observations) {
				// only make the following Observation changes after date mentioned in 
				// calendar object
				if ("DHA".equals(healthAuthority)) {
					String chargeGroup = (String) charge.get("charge_group");
					String chargeHead = (String) charge.get("charge_head");
					if (chargeGroup.equalsIgnoreCase("SNP")) {
						setDhaDentalObservations(observation, charge);
					} else if (chargeHead.equalsIgnoreCase("LTDIA")) {
						setDhaLabObservations(observation, charge);
					} else if (chargeGroup.equalsIgnoreCase("DOC")) {
						setDhaVitalObservations(observation, charge);
					}
				}
			}
			observationList.addAll(observations);
			observationsMapForFTL.put(claimActivityId, observationList);
		} else if (((String) charge.get("activity_group")).equalsIgnoreCase("Pharmacy") && modEclaimErx
				&& charge.get("erx_activity_id") != null && charge.get("erx_reference_no") != null
				&& !((String) charge.get("erx_activity_id")).equals("")
				&& !((String) charge.get("erx_reference_no")).equals("")) {
			/**
			 * Adds the erx activity related observations to existing observationMap. Incase
			 * of pharmacy claims with Erx we need to add erx observations Refer
			 * https://practo.atlassian.net/wiki/spaces/HIMS/pages/895483954/EmirateClinics+Pharmacy+Claim+Management
			 */
			HashMap chargeMap = new HashMap<>();
			chargeMap.put("type", "ERX");
			chargeMap.put("value", charge.get("erx_activity_id"));
			chargeMap.put("code", charge.get("erx_reference_no"));
			chargeMap.put("value_type", "Reference");
			observationList.add(chargeMap);
			observationsMapForFTL.put(claimActivityId, observationList);
		}
    
    /**
     * Sending patient share as an observations for 
     * items which are having haad code as 5(Drug/Drug HAAD).
     * 
     */
    
    setPatientShareObservations(healthAuthority, charge, claim, patientShareObservationMap, 
        observationList, observationsMapForFTL);

  }

  /**
   * Method to send patient share as observation in claim xml 
   * for activities which are having haad code as Drug/Drug HAAD.
   * @param healthAuthority
   * @param charge
   * @param claim
   * @param patientShareObservationMap
   * @param observationList
   * @param observationsMapForFTL
   */
  private void setPatientShareObservations(String healthAuthority, BasicDynaBean charge,
      BasicDynaBean claim, Map patientShareObservationMap, List<Map> observationList,
      Map<String, List<Map>> observationsMapForFTL) {

    BigDecimal amount = (BigDecimal) charge.get("amount");
    BigDecimal claimAmt = (BigDecimal) charge.get("insurance_claim_amt");
    String claimActivityId = (String) charge.get("claim_activity_id");

    String useDRG = (String) claim.get("use_drg");
    String usePerdiem = (String) claim.get("use_perdiem");

    int actType = null != charge.get("act_type") ? (Integer) charge.get("act_type") : 0;

    // Patient share observation should go as 0 for drg claims.
    if (healthAuthority.equals("HAAD") && actType == 5 
        && !usePerdiem.equals("Y")) {
      if (null == patientShareObservationMap) {
        HashMap chargeMap = new HashMap<>();
        chargeMap.put("type", "Text");
        BigDecimal actValue = useDRG.equals("Y") ? BigDecimal.ZERO : amount.subtract(claimAmt);
        
        chargeMap.put("value", actValue);
        chargeMap.put("code", "Drug patient share");
        chargeMap.put("value_type", "AED");
        observationList.add(chargeMap);

        observationsMapForFTL.put(claimActivityId, observationList);
      } else {
        /**
         * For combined activities modifying patient share observation value as sum of all
         * activities patient share
         */
        BigDecimal patientShare = (BigDecimal) patientShareObservationMap.get("value");
        BigDecimal actValue = useDRG.equals("Y") ? BigDecimal.ZERO : patientShare.add(amount.subtract(claimAmt));
        patientShareObservationMap.put("value", actValue);
      }
    }
  }

  /**
   * Method to get the patient observation map.
   * @param observationList
   * @return
   */
  private Map getPatientShareObservations(List<Map> observationList) {
    for (Map obs : observationList) {
      String code = (String) obs.get("code");
      if (code.equals("Drug patient share")) {
        return obs;
      }
    }
    return null;
  }

  /**
	 * Sets the value type in an observation as the item posted date
	 * from the corresponding charge. 
	 *
	 * @param observation the observation
	 * @param charge the charge
	 */
	private void setDhaVitalObservations(Map observation, BasicDynaBean charge) {
		if (observation.get("type").equals("Result") && charge.get("item_posted_date") != null) {
			// Refer https://practo.atlassian.net/browse/HMS-24304
			observation.put("value_type", new SimpleDateFormat("dd/MM/yyyy").format(charge.get("item_posted_date")));
		}
	}

	private void setDhaLabObservations(Map observation, BasicDynaBean charge) {
		if (observation.get("type").equals("Result") && charge.get("conducted_date") != null) {
			// Refer https://practo.atlassian.net/browse/HMS-24304
			observation.put("value_type", new SimpleDateFormat("dd/MM/yyyy").format(charge.get("conducted_date")));
		}

	}
  /**
   * Validate the observations for a dental charge.
   *
   * @param billNo the bill no
   * @param observations the observations
   * @param charge the charge
   * @param errorsMap the errors map
   * @param claim the claim
   * @param urlActionMap the url action map
   * @param path the path
   * @throws SQLException the SQL exception
   */
  private void validateDentalService(String billNo, List<Map> observations, BasicDynaBean charge,
      Map<String, StringBuilder> errorsMap, BasicDynaBean claim, HashMap urlActionMap, String path)
      throws SQLException {

    StringBuilder toothCodesErr = new StringBuilder(TOOTH_CODES_ERROR);
    StringBuilder toothNoCodesErr = new StringBuilder(TOOTH_NO_CODES_ERR);
    StringBuilder toothNoUniversalCodeErr = new StringBuilder(TOOTH_NO_UNIVERSAL_CODE_ERR);
    String actDescriptionId = (String) charge.get("act_description_id");
    String claimPatientId = (String) claim.get("claim_patient_id");
    String actDescription = (String) charge.get("act_description");
    String postedDate = (String) charge.get("posted_date");

    BasicDynaBean service = serviceMasterDao.findByKey("service_id", actDescriptionId);
    if (service != null && service.get("tooth_num_required").equals("Y")) {
      if (observations == null || observations.isEmpty()) {
        errorsMap.put("TOOTH NUMBER ERROR:", toothCodesErr
            .append(submitdao.urlString(path, "diagnosis", claimPatientId, null, urlActionMap)));
        toothCodesErr.append("<br/>Service :" + billNo + "( " + actDescription + ", Posted Date: "
            + postedDate + " ), <br/> ");
      }
      int numOfDentalCodes = 0;
      if (observations != null && !observations.isEmpty()) {
        for (Map observation : observations) {
          String resultCode = (String) observation.get("code");
          String resultType = (String) observation.get("type");
          BasicDynaBean haadBean = mrdCodeTypesDao.findByKey("code_type", resultType);
          Integer haadCode = haadBean == null ? null : (Integer) haadBean.get("haad_code");
          if (haadCode != null && haadCode.intValue() == 16) {
            ++numOfDentalCodes;
          }
          if (resultCode == null || resultCode.equals("")) {
            if (errorsMap.containsKey("TOOTH NUMBER CODES ERROR:")) {
              toothNoCodesErr = errorsMap.get("TOOTH NUMBER CODES ERROR:");
              toothNoCodesErr.append("   ,  ");
            }
            errorsMap.put("TOOTH NUMBER CODES ERROR:", toothNoCodesErr.append(
                submitdao.urlString(path, "diagnosis", claimPatientId, null, urlActionMap)));
            toothNoCodesErr.append("<br/>Service :" + billNo + "( " + actDescription + ", Posted Date: "
                + postedDate + " ), <br/> ");
          }
        }
      }
      if (numOfDentalCodes == 0) {
        if (errorsMap.containsKey("TOOTH NUMBER NO UNIVERSAL CODE ERROR:")) {
          toothNoUniversalCodeErr = errorsMap.get("TOOTH NUMBER NO UNIVERSAL CODE ERROR:");
          toothNoUniversalCodeErr.append("   ,  ");
        }
        errorsMap.put("TOOTH NUMBER NO UNIVERSAL CODE ERROR:", toothNoUniversalCodeErr
            .append(submitdao.urlString(path, "diagnosis", claimPatientId, null, urlActionMap)));
        toothNoUniversalCodeErr.append("<br/>Service :" + billNo + "( " + actDescription
            + ", Posted Date: " + postedDate + " ), <br/> ");
      }
    }
  }

	private void setDhaDentalObservations(Map observation, BasicDynaBean charge) {
		// Refer https://practo.atlassian.net/browse/HMS-24306
		if (observation.get("type").equals("Universal Dental")) {
			observation.put("value", observation.get("code"));
			observation.put("value_type", "ToothNumber");
    } else if (observation.get("type").equals("Result") && charge.get("conducted_date") != null) {
			// Refer https://practo.atlassian.net/browse/HMS-24304
			observation.put("value_type", charge.get("conducted_date"));
		}
	}
  
  private Map<String, List<Map>> findAllObservationsMap(String claim_id,
      String sponsor_id, String eclaimXMLSchema) throws SQLException {
		Map<String, List<Map>> obBeanMap = new HashMap<>();
		List<BasicDynaBean> observationsBean = submitdao.findAllClaimObservations(claim_id,sponsor_id,eclaimXMLSchema);
		List<Map> nw = null;
	
		for (BasicDynaBean chbean : observationsBean) {
		  // set value as base64 encoded attached document
		  if (chbean.get("document_id") != null && chbean.get("file_bytes") != null
		      && "File".equalsIgnoreCase((String)(chbean.get("type")))) {
        String fileBase64Str = Base64.encodeBase64String((byte[])chbean.get("file_bytes"));
        chbean.set("value", fileBase64Str);
      }
			if(obBeanMap.containsKey((String)chbean.get("charge_id"))){
				nw = obBeanMap.get((String)chbean.get("charge_id"));
				// creates a non read only map
				nw.add(new DynaBeanMapDecorator(chbean, false));
				obBeanMap.put((String)chbean.get("charge_id"), nw);
			} else {
				nw = new ArrayList<>();
				// creates a non read only map
				nw.add(new DynaBeanMapDecorator(chbean, false));
				obBeanMap.put((String)chbean.get("charge_id"), nw);
			}      
		}
		return obBeanMap;
	}


  private Map<String, BasicDynaBean> getChargeHeadMap(List<String> chHeadList) throws SQLException {
		Map<String, BasicDynaBean> chBeanMap= new HashMap<>();
		List<BasicDynaBean> chHeadBean = ChargeHeadsDAO.getChargeHeadBean(chHeadList);
		for (BasicDynaBean chbean : chHeadBean) {
			chBeanMap.put((String)chbean.get("chargehead_id"), chbean);
		}
		return chBeanMap;
	}

	private List<String> findAllChargeHead(List<BasicDynaBean> charges) {
		List<String> ls=new ArrayList<>();
		for (BasicDynaBean charge : charges) {
			ls.add((String)charge.get("charge_head"));
		}
		return ls;
	}

}