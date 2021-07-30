[#include "TextPrintMacros.ftl"]
[#setting time_format="HH:mm"]
[#setting date_format="dd-MM-yyyy"]
[#setting datetime_format="dd-MM-yyyy HH:mm"]
[#assign title]
	Operation Record:[#rt]
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
User Name: ${lf(userName!,      20)}


																					Operation Record :
																					------------------

[#if opeartionsList?has_content]
Operation Details:

${lf("Procedure Name",50)}  ${lf("Surgery Type",20)}  ${lf("Modifier",50)}
--------------------------------------------------------------------------------------------------------------
[#list opeartionsList as operation]
	[#if operation.oper_priority == 'P' ]
		[#assign procedure_type="Primary Procedure"]
	[#else]
		[#assign procedure_type="Secondary Procedure"]
	[/#if]
	${lf(operation.operation_name!, 50)?html}  ${lf(procedure_type!, 20)?html}  ${lf(operation.modifier!, 50)?html}
[/#list]
[/#if]

[#if operation_team_details?has_content]
Operation Team:

${lf("Doctor Name",50)}  ${lf("Role",20)}
--------------------------------------------------------------------------------------------------------------
[#list operation_team_details as operationTeam]
	[#switch operationTeam.operation_speciality]
		  [#case "SU"]
		     [#assign role_type="Surgeon"]
		     [#break]
		  [#case "ASU"]
		     [#assign role_type="Asst Surgeon"]
		     [#break]
		  [#case "COSOPE"]
		     [#assign role_type="Co-op. Surgeon"]
		     [#break]
	      [#case "AN"]
	         [#assign role_type="Anaesthetist"]
	         [#break]
	      [#case "ASAN"]
	         [#assign role_type="Asst. Anaesthetist"]
	         [#break]
		  [#case "PAED"]
			[#assign role_type="Paediatrician"]
			[#break]
	[/#switch]
	${lf(operationTeam.doctor_name!, 50)?html}  ${lf(role_type!, 50)?html}
[/#list]
[/#if]

[#if operation_anaethesia_details?has_content]
Operation Anaethesia Details:

${lf("Anaethesia Type",80)}  ${lf("Anaethesia Start",20)} ${lf("Anaethesia End",20)}
----------------------------------------------------------------------------------------------------------------------
[#list operation_anaethesia_details as oae]
	${lf(oae.anesthesia_type_name!, 80)?html}  ${lf(oae.anaesthesia_start!, 20)?html} ${lf(oae.anaesthesia_end!, 20)?html}
[/#list]
[/#if]

[#if surgery_details.theatre_name?has_content]
Operation Theatre:

Theatre Name 		: ${surgery_details.theatre_name!?html}
[/#if]

[#if surgery_details?has_content]
Other Details:

[#if surgery_details.prescribed_by?has_content]
Prescribed By 		: ${surgery_details.prescribed_by!?html}
[/#if]

[#if surgery_details.wheel_in_time?has_content || surgery_details.wheel_out_time?has_content]
Wheel In Time 		: ${surgery_details.wheel_in_time!?html}[#rt]     Wheel Out Time 	: ${surgery_details.wheel_out_time!?html}[#rt]
[/#if]

[#if surgery_details.surgery_start?has_content || surgery_details.surgery_end?has_content]
Surgery Start 		: ${surgery_details.surgery_start!?html}[#rt]     Surgery End 		: ${surgery_details.surgery_end!?html}[#rt]
[/#if]

[#if surgery_details.specimen?has_content]
Specimen 			: ${lf(surgery_details.specimen!, 100)?html}
[/#if]

[#if surgery_details.conduction_remarks?has_content]
Conduction Remarks : ${lf(surgery_details.conduction_remarks!, 100)?html}
[/#if]

[/#if]

[#if diagnosis_details?has_content]
Diagnosis Details:

${lf("Diag. Type", 10)}  ${lf("Diag. Code", 5)}  ${lf("Description", 50)}
------------------------------------------------------------------------------
[#list diagnosis_details as diagnosis]
	[#if diagnosis.diag_type == 'P' ]
		[#assign diagnosis_type="Principal"]
	[#elseif diagnosis.diag_type == 'A']
		[#assign diagnosis_type="Admitting"]
	[#else]
		[#assign diagnosis_type="Secondary"]
	[/#if]
	${lf(diagnosis_type!, 10)?html}  ${lf(diagnosis.icd_code!, 5)?html}  ${lf(diagnosis.description!, 50)?html}
[/#list]
[/#if]

[#if visitdetails.complaint?has_content]
Chief Complaint: ${visitdetails.complaint!}
[/#if]

[#if secondary_complaints?has_content]
	[#list secondary_complaints as s_complaint]
		Other Complaint: ${s_complaint.complaint!?html} [#lt]
	[/#list]
[/#if]

[#list opCompSectionValues?keys as opeProcId]
[#if opeProcId?has_content]

--------------------------------------------------------------------------------------------------------------
${lf(operationNames[opeProcId]!, 100)?html}
--------------------------------------------------------------------------------------------------------------
[#assign operationMap = opCompSectionValues[opeProcId]]
[#assign stn_title = '']
[#assign stn_count = 1]
[#list operationMap?keys as sectionDetailId]
[#assign fieldTitle = operationMap[sectionDetailId][0][0].section_title]
[#if fieldTitle?has_content]

${fieldTitle!?html}[#if stn_title == fieldTitle]${'- ' + stn_count?c}[#assign stn_count = stn_count + 1][/#if][#if stn_title != fieldTitle][#assign stn_count = 1][#assign stn_title = fieldTitle][/#if]

[#list operationMap[sectionDetailId] as field]
${field[0].field_name}:[#rt]
[#list field as value]
	[#if value.field_type == 'checkbox']
		[#if value.allow_normal == 'Y' && value.option_id == 0]
			Normal [#if value.option_remarks?has_content]-[/#if]${value.option_remarks!?html},
		[#else]
			${value.option_value!?html}[#if value.option_remarks?has_content]-[/#if]${value.option_remarks!?html},
		[/#if]
	[#elseif value.field_type == 'dropdown']
		 [#if value.allow_others == 'Y' && value.option_id ==-1]
			${value.option_remarks!?html}, [#rt]
		 [#elseif value.allow_normal == 'Y' && value.option_id == 0]
			Normal,
		 [#else]
			${value.option_value!?html},
		 [/#if]
	[#elseif value.field_type == 'text' || value.field_type == 'wide text']
    	${value.option_remarks!?html},
    [#elseif value.field_type == 'date']
    	${(value.date?string('dd-MM-yyyy'))!},
   	[#elseif value.field_type == 'datetime']
    	${(value.date_time?string('dd-MM-yyyy HH:mm'))!},
	[/#if]
[/#list]
[/#list]

[/#if]
[/#list]

[/#if]
[/#list]