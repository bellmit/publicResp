[#include "TextPrintMacros.ftl"]
[#setting time_format="HH:mm"]
[#setting date_format="dd-MM-yyyy"]
[#setting datetime_format="dd-MM-yyyy HH:mm"]


[#assign title]
	[#if visitdetails.visit_type == "i"]Print Prescription[#rt]
	[/#if]
[/#assign]
[#assign name = "${visitdetails.full_name!}"]
[#assign age = "${visitdetails.age!} ${visitdetails.agein!}/${visitdetails.patient_gender!}"]
[#assign address = "${visitdetails.address!}"]
[#assign location = "${visitdetails.cityname!},${visitdetails.statename!}"]
[#assign doctor = "${visitdetails.doctor_name!}"]
[#assign department = "${visitdetails.dept_name!}"]
[#assign rateplan]
	[#if (visitdetails.org_name!'GENERAL')!='GENERAL']${visitdetails.org_name!}[#rt]
	[/#if]
[/#assign]
[#assign sponsor]
	[#if (visitdetails.tpa_name!'')!='']${visitdetails.tpa_name!}[#rt]
	[/#if]
[/#assign]
[#assign referredto]
	[#if (visitdetails.visit_type == "i")]
		[#if (visitdetails.referred_to_hosp!'')!='']
			${visitdetails.referred_to_hosp!}
		[/#if]
	[/#if]
[/#assign]
[#assign mrno = "${visitdetails.mr_no!}"]
[#assign visitid = "${visitdetails.patient_id!}"]
[#assign admitted]
  [#if visitdetails.visit_type == "i"]Admit Date:[#else]Regd. Date:[/#if][#t]
[/#assign]
[#assign regdate = "${visitdetails.reg_date!}"]
[#assign bedtitle]
  [#if visitdetails.visit_type == "i"]Bed Type:  [#else]           [/#if][#t]
[/#assign]
[#assign wardnbed]
  [#if visitdetails.visit_type == "i"]${visitdetails.bill_bed_type!}[#else][/#if][#t]
[/#assign]
[#assign distitle]
  [#if visitdetails.visit_type == "i"]
    [#if (visitdetails.discharge_type!)=="Expiry"]Death Date:[#else]Disch Date:[/#if][#t]
    [#else]Discharge Date:[#t]
  [/#if]
[/#assign]
[#assign dischdate]
  [#if visitdetails.visit_type == "i"]
    ${visitdetails.discharge_date!}[#t]
  [/#if]
[/#assign]
[#if ((visitdetails.refdoctorname!'')!='')]
	[#assign referredby = "${visitdetails.refdoctorname!}"]
[/#if]
Name:      ${lf(name,                     35)}  MR NO:       ${lf(mrno,                30)}

Age/Gender:${lf(age,                      35)}  Visit ID:    ${lf(visitid,             30)}

Address:   ${lf(address,                  29)}	${admitted}  ${lf(regdate,            29)}

Location:  ${lf(location,                 30)}	${distitle}  ${lf(dischdate,           30)}

Doctor:    ${lf(doctor,                   30)}	${bedtitle}  ${lf(wardnbed,             30)}

Department:${lf(department,               30)}	Reffered By: ${lf(referredby!,          30)}

Rate Plan: ${lf(rateplan,                 30)}

Sponsor:   ${lf(sponsor,                  30)}

Referred To:${lf(referredto,              30)}
Consulting Doctor:${lf(consultation_bean.doctor_name!, 18)}             User Name: ${lf(userName!,      20)}

Chief Complaint: ${visitdetails.complaint!}


[#if secondary_complaints?has_content]
	[#list secondary_complaints as s_complaint]
		Other Complaint: ${s_complaint.complaint!?html} [#lt]
	[/#list]
[/#if]

Consultation Details:

[#if diagnosis_details?has_content]
${lf("Diag. Type", 10)}  ${lf("Diag. Code", 5)}  ${lf("Description", 50)}
------------------------------------------------------------------------------
[#list diagnosis_details as diagnosis]
	[#if diagnosis.diag_type == 'P' ]
		[#assign diagnosis_type="Principal"]
	[#elseif diagnosis.diag_type == 'A']
		[#assign diagnosis_type="Admitting"]
	[#elseif diagnosis.diag_type == 'V']
		[#assign diagnosis_type="Reason For Visit"]
	[#else]
		[#assign diagnosis_type="Secondary"]
	[/#if]
	${lf(diagnosis_type!, 10)?html}  ${lf(diagnosis.icd_code!, 5)?html}  ${lf(diagnosis.description!, 50)?html}
[/#list]
[/#if]
[#if vital_values?has_content]

Vitals:
${lf("Date", 18)} | [#t]
[#list vital_params as param]
	${lf(param.param_label!, 4)?html}[#if param.param_uom?has_content](${rf(param.param_uom!, 5)})[/#if] | [#t]
[/#list]
${lf("User", 15)} 
------------------------------------------------------------------------------

[#list vital_values as vital]
${lf(vital['date_time']?string('dd-MM-yyyy HH:mm'),	20)}[#t]
[#list vital_params as param]
${""} ${lf(vital[param.param_label]!, 6)}${rf(vital.param_uom!, 7)}[#t]
[/#list]
${""} ${lf(vital['user_name']!, 15)}
[/#list]
[/#if]
[#if consultationFields?has_content]

Consultation Notes:

[#list consultationFields as field]
${field.field_value!}
[/#list]
[/#if]

Allergies:

[#if allergies?has_content]
${lf("Alrgy Type", 10)} ${lf("Allergy", 25)} ${lf("Onset Date", 15)} ${lf("Reaction", 20)} ${lf("Severity", 10)} ${lf("Status", 10)}
-------------------------------------------------------------------------------------------------------------
[#list allergies as allergy]
[#if allergy.allergy_type == 'N']
	[#assign allergy_type='No Known Allergies']
[#elseif allergy.allergy_type == 'M']
	[#assign allergy_type='Medicine']
[#elseif allergy.allergy_type == 'F']
	[#assign allergy_type='Food']
[#else]
	[#assign allergy_type='Others']
[/#if]
${lf(allergy_type!, 15)}[#t]
${""} ${lf(allergy.allergy, 25)?html}[#t]
${""} ${lf(allergy.onset_date, 15)?html}[#t]
${""} ${lf(allergy.reaction, 20)?html}[#t]
${""} ${lf(allergy.severity!, 10)?html}[#t]
${""} [#if allergy.status == 'A']${lf("Active", 10)}[#else]${lf("Inactive", 10)}[/#if]
[/#list]
[/#if]

Obstetric History:

[#if pregnancyhistories?has_content || pregnancyhistoriesBean?has_content] 
[#list pregnancyhistoriesBean as pregnancyhistory]
 [#assign pregnancyvariable = false] 
			[#if pregnancyhistory.field_g?has_content || pregnancyhistory.field_p?has_content || pregnancyhistory.field_l?has_content || pregnancyhistory.field_a?has_content]
				[#assign pregnancyvariable = true]
			[/#if]
[#if pregnancyvariable = true]
${""}${lf("G", 10)}[#if pregnancyhistory.field_g?has_content] ${lf(pregnancyhistory.field_g, 5)?html}[/#if]
${""}${lf("P", 10)}[#if pregnancyhistory.field_p?has_content] ${lf(pregnancyhistory.field_p, 5)?html} [/#if]
${""}${lf("L", 10)}[#if pregnancyhistory.field_l?has_content] ${lf(pregnancyhistory.field_l, 5)?html} [/#if]
${""}${lf("A", 10)}[#if pregnancyhistory.field_a?has_content] ${lf(pregnancyhistory.field_a, 5)?html} [/#if]
[/#if]
[/#list]

[#if pregnancyhistories?has_content]
[#list pregnancyhistories as pregnancyhistory]
${lf("Date of Delivery", 10)} ${""} ${lf(pregnancyhistory.date, 10)?html}
${lf("Week", 10)} ${""} [#if pregnancyhistory.weeks?has_content] ${lf(pregnancyhistory.weeks, 5)?html} [/#if]
${lf("Place", 10)} ${""} ${lf(pregnancyhistory.place!, 10)?html}
${lf("Method", 10)} ${""} ${lf(pregnancyhistory.method!, 10)?html}
${lf("Weight", 10)} ${""} [#if pregnancyhistory.weight?has_content] ${lf(pregnancyhistory.weight, 5)?html} [/#if]
[#if pregnancyhistory.sex == 'M']
	[#assign sex='Male']
[#elseif pregnancyhistory.sex == 'F']
	[#assign sex='Female']
[#elseif pregnancyhistory.sex == 'O']
	[#assign sex='Unknown']
[#else]
[/#if]
${lf("Sex", 10)} ${lf(sex!, 10)}
${lf("Complications", 13)} ${""} ${lf(pregnancyhistory.complications!, 10)?html}
${lf("Feeding", 10)} ${""} ${lf(pregnancyhistory.feeding!, 10)?html}
${lf("Outcome", 10)} ${""} ${lf(pregnancyhistory.outcome!, 10)?html}
---------------------------------------------------------------------------------
[/#list]
[/#if]
[/#if]

Antenatal Details:

[#if antenatalKeyCounts?has_content]
-----------------------------------------------------------------------------------
	[#assign pregnencyResult = '']
	[#assign pregnencyResultDate = '']
	[#assign numberOfBirth = '']
	[#assign remarks = '']
	[#list antenatalKeyCounts as pragnancyCount]
		[#assign count= pragnancyCount!?html]
		[#list antenatalinfoMap[pragnancyCount] as antenatal]
			[#assign pregnencyResult= antenatal.pregnancy_result!?html]
			[#assign pregnencyResultDate= antenatal.pregnancy_result_date!?html]
			[#assign numberOfBirth= antenatal.number_of_birth!?html]
			[#assign remarks= antenatal.remarks!?html]
[#if pragnancyCount == count]
${lf("Pregnancy-", 10)} ${lf(pragnancyCount!, 10)?html}
${lf("L.M.P", 10)} ${""} [#if antenatal.lmp?has_content]${lf(antenatal.lmp!, 10)?html}[/#if]
${lf("E.D.D", 10)} ${""} [#if antenatal.edd?has_content]${lf(antenatal.edd!, 10)?html}[/#if]
${lf("F.D.D", 10)} ${""} [#if antenatal.final_edd?has_content]${lf(antenatal.final_edd!, 10)?html}[/#if]
[#assign count = count + 1 ]
[/#if]
${lf("Visit Date", 10)} ${""} ${lf(antenatal.visit_date!, 10)?html}
${lf("Gestation Age", 10)} ${""}[#if antenatal.gestation_age?has_content] ${lf(antenatal.gestation_age, 10)?html}[/#if]
${lf("Height Of Fundus", 10)} ${""} [#if antenatal.height_fundus?has_content] ${lf(antenatal.height_fundus, 10)?html}[/#if]
${lf("Presentation", 10)} ${""} ${lf(antenatal.presentation!, 10)?html}
${lf("Relation Of PP To Brim", 20)} ${""} ${lf(antenatal.rel_pp_brim!, 20)?html}
${lf("Urine", 5)} ${""} ${lf(antenatal.urine!, 10)?html}
${lf("Foetal Heart", 15)} ${""} ${lf(antenatal.foetal_heart!, 15)?html}
${lf("BP", 5)}  ${""}[#if antenatal.systolic_bp?has_content] ${lf(antenatal.systolic_bp, 2)?html}[/#if]${""}[#if antenatal.diastolic_bp?has_content]/${lf(antenatal.diastolic_bp, 2)?html} [/#if]
${lf("Weight", 5)} ${""} [#if antenatal.weight?has_content] ${lf(antenatal.weight, 5)?html} [/#if]
${lf("Prescription Summary", 20)} ${""} ${""} ${lf(antenatal.prescription_summary!, 15)?html}
${lf("Consulting Doctor", 5)} ${""} ${lf(antenatal.doctor_id!, 5)?html}
${lf("Next Visit Date", 5)} ${""} ${lf(antenatal.next_visit_date!, 5)?html}
${lf("Position", 5)} ${""} ${lf(antenatal.position!, 5)?html}
${lf("Movement", 5)} ${""} ${lf(antenatal.movement!, 5)?html}
[/#list]
${lf(pregnencyResult!,10)?html}${"Date"} ${lf(pregnencyResultDate!, 10)?html}
${lf("Number of Birth", 20)} ${""} ${lf(numberOfBirth!, 20)?html}
${lf("Remarks", 20)} ${""} ${lf(remarks!, 20)?html}

[/#list]
------------------------------------------------------------------------------------
[/#if]


Pre Anesthesthetic Checkup Details:

[#if pac_details?has_content]
${lf("Doctor", 15)} ${lf("Conduction Date", 15)} ${lf("Validity Date", 15)} ${lf("Outcome", 7)} ${lf("Pre-Anaesthetic Evaluation Remarks", 20)}
-------------------------------------------------------------------------------------------------------------
[#list pac_details as pac]
${""} ${lf(pac.doctor_name, 15)?html}[#t]
${""} ${lf(pac.pac_date, 15)?html}[#t]
${""} ${lf(pac.pac_validity, 15)?html}[#t]
${""} [#if pac.status == 'F']${lf("Fit", 7)}[#else]${lf("Unfit", 7)}[/#if][#t]
${""} ${lf(pac.patient_pac_remarks, 20)?html}
[/#list]
[/#if]

Prescriptions:
[#if presMedicines?has_content]

	Medicines:
	[#assign prescribedate = "${presMedicines[0].mod_time!}"]
		Prescribed Date:  ${lf(prescribedate,       20)}

	[#assign medicine_name= presMedicines[0].item_name!?html]
	[#assign generic_name=presMedicines[0].generic_name!?html]
[#if medicine_name != '']
${lf("Medicine Name", 25)} ${lf("Dosage", 10)} ${lf("Freq.", 10)} ${lf("Duration", 10)} ${lf("Qty", 5)} ${lf("Instructions", 30)} ${lf("Refills", 10)}
-----------------------------------------------------------------------------
	[#list presMedicines as medicine]
	[#assign instructions =  "${medicine.item_remarks!} ${medicine.special_instr!}"]
		[#assign medicine_name= medicine.item_name!?html]
		[#if medicine.non_hosp_medicine]
			[#assign medicine_name=medicine_name+'[Non Hosp]']
		[/#if]
		[#assign dosage="${medicine.strength!} ${medicine.consumption_uom!}"]
		${lf(medicine_name!, 25)?html} ${lf(dosage, 10)} ${lf(medicine.medicine_dosage!,  10)?html} [#if medicine.duration?has_content]${rf(medicine.duration?string("0"),  10)}[/#if] ${rf(medicine.duration_units!, 10)} ${rf(medicine.medicine_quantity!,  5)} ${lf(instructions!,  30)?html} ${lf(medicine.refills!, 10)?html}[#lt]
	[/#list]
[#else]
${lf("Generic Name", 20)} ${lf("Form", 10)} ${lf("Strength", 10)} ${lf("Dosage", 10)} ${lf("Freq.", 9)} ${lf("Duration", 5)} ${lf("Qty", 5)} ${lf("Instructions", 20)} ${lf("Refills", 5)}
-----------------------------------------------------------------------------
	[#list presMedicines as medicine]
	[#assign instructions = "${medicine.item_remarks!} ${medicine.special_instr!}"]
		[#assign generic_name=medicine.generic_name!?html]
		[#if medicine.non_hosp_medicine]
			[#assign generic_name=medicine.item_name?html+'[Non Hosp]']
		[/#if]
		[#assign dosage="${medicine.strength!} ${medicine.consumption_uom!}"]
		[#assign strength="${medicine.item_strength!} ${medicine.unit_name!}"]]
		${lf(generic_name!, 20)?html} ${lf(medicine.item_form_name!, 10)?html} ${lf(strength, 10)} ${lf(dosage, 10)} ${lf(medicine.medicine_dosage!,  9)?html} [#if medicine.duration?has_content]${rf(medicine.duration?string("0"),  5)}[/#if] ${rf(medicine.duration_units!, 1)} ${rf(medicine.medicine_quantity!,  5)} ${lf(instructions!,  20)?html} ${lf(medicine.refills!,  5)?html}[#lt]
	[/#list]
[/#if]

[/#if]
[#if presTests?has_content]

	Investigations:
	[#assign prescribedate = "${presTests[0].mod_time!}"]
		Prescribed Date:  ${lf(prescribedate,       20)}

		Investigation  			Instructions
		--------------------------------------
	[#list presTests as test]
	[#assign instructions = "${test.item_remarks!} ${test.special_instr!}"]
		${lf(test.item_name!,    30)?html}	${lf(instructions!,  30)?html}
	[/#list]
[/#if]
[#if presServices?has_content]

	Services:
	[#assign prescribedate = "${presServices[0].mod_time!}"]
		Prescribed Date:  ${lf(prescribedate,       20)}

		Service Name			Qty  Tooth No. Instructions
		----------------------------------------------------
	[#list presServices as service]
	[#assign instructions = "${service.item_remarks!} ${service.special_instr!}"]
		[#if service.tooth_unv_number?has_content]
			[#assign tooth_number=service.tooth_unv_number]
		[#elseif service.tooth_fdi_number?has_content]
			[#assign tooth_number=service.tooth_fdi_number]
		[/#if]
		${lf(service.item_name!,    30)?html}	${lf(service.service_qty, 5)} ${lf(tooth_number!, 5)} ${lf(instructions!,  30)?html}
	[/#list]
[/#if]
[#if presConsultation?has_content]

	Cross Consultations:
	[#assign prescribedate = "${presConsultation[0].mod_time!}"]
		Prescribed Date:  ${lf(prescribedate,       20)}

		Doctor Name			Instructions
		-----------------------------------------
	[#list presConsultation as consultation]
	[#assign instructions = "${consultation.item_remarks!} ${consultation.special_instr!}"]
		${lf(consultation.item_name!,    30)?html}	${lf(instructions!,  30)?html}
	[/#list]
[/#if]
[#if presOperations?has_content]

	Operations
	[#assign prescribedate = "${presOperations[0].mod_time!}"]
		Prescribed Date:  ${lf(prescribedate,       20)}

		Operation Name			             Instructions
		--------------------------------------------------
	[#list presOperations as operation]
	[#assign instructions = "${operation.item_remarks!} ${operation.special_instr!}"]
		${lf(operation.item_name!,    30)?html}	${lf(instructions!,  30)?html}
	[/#list]
[/#if]

[#if NonHospitalItems?has_content]

	Non Hospital Items:
		[#assign prescribedate = "${NonHospitalItems[0].mod_time!}"]
		Prescribed Date:  ${lf(prescribedate,       20)}

		Item Name	  | Form  | Strength | Dosage | Freq.	   | Duration | Qty |	Instructions   | Refills
-----------------------------------------------------------------------------
	[#list NonHospitalItems as item]
		[#assign dosage="${item.strength!} ${item.consumption_uom!}"]
		[#assign instructions="${item.item_remarks!} ${item.special_instr!}"]
		${lf(item.item_name!,    15)?html}[#t]
		${""} [#if item.item_form_name?has_content]${lf(item.item_form_name, 10)?html}[/#if][#t]
		${""} [#if item.strength?has_content]${lf(item.item_strength, 5)?html}[/#if][#t]
		${""} ${lf(item.unit_name!, 3)?html} [#t]
		${""} ${lf(dosage!, 10)} [#t]
		${""} ${lf(item.medicine_dosage!,  12)?html} [#t]
		${""} [#if item.duration?has_content]${rf(item.duration?string("0"),  5)} ${rf(item.duration_units!, 1)}[/#if] ${rf(item.medicine_quantity!,  6)}	[#t]
		${""} ${lf(instructions!,  20)?html} ${lf(item.refills!,  10)?html}
	[/#list]
[/#if]

[#if followupDetails?has_content]

	Follow Up Details:

		Doctor Name  			               Follow Up Date       Remarks
		--------------------------------------------------------------------------------------------------
	[#list followupDetails as item]
		${lf(item.doctor_name!,    30)?html}	${lf(item.followup_date!,  20)?html}  ${lf(item.followup_remarks!,  30)?html}
	[/#list]
[/#if]

Prescriptions Notes :	${lf(consultation_bean.prescription_notes!,   20)?html}

Doctor's Signature
