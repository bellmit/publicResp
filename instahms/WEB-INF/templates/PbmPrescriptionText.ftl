[#include "TextPrintMacros.ftl"]
[#setting time_format="HH:mm"]
[#setting date_format="dd-MM-yyyy"]
[#setting datetime_format="dd-MM-yyyy HH:mm"]


[#assign title]
	[#if patientdetails.visit_type == "i"]Print Prescription[#rt]
	[/#if]
[/#assign]
[#assign name = "${patientdetails.full_name!}"]
[#assign age = "${patientdetails.age!} ${patientdetails.agein!}/${patientdetails.patient_gender!}"]
[#assign address = "${patientdetails.address!}"]
[#assign location = "${patientdetails.cityname!},${patientdetails.statename!}"]
[#assign doctor = "${patientdetails.doctor_name!}"]
[#assign department = "${patientdetails.dept_name!}"]
[#assign rateplan]
	[#if (patientdetails.org_name!'GENERAL')!='GENERAL']${patientdetails.org_name!}[#rt]
	[/#if]
[/#assign]
[#assign sponsor]
	[#if (patientdetails.tpa_name!'')!='']${patientdetails.tpa_name!}[#rt]
	[/#if]
[/#assign]
[#assign referredto]
	[#if (patientdetails.visit_type == "i")]
		[#if (patientdetails.referred_to_hosp!'')!='']
			${patientdetails.referred_to_hosp!}
		[/#if]
	[/#if]
[/#assign]
[#assign mrno = "${patientdetails.mr_no!}"]
[#assign visitid = "${patientdetails.patient_id!}"]
[#assign admitted]
  [#if patientdetails.visit_type == "i"]Admit Date:[#else]Regd. Date:[/#if][#t]
[/#assign]
[#assign regdate = "${patientdetails.reg_date!}"]
[#assign bedtitle]
  [#if patientdetails.visit_type == "i"]Bed Type:  [#else]           [/#if][#t]
[/#assign]
[#assign wardnbed]
  [#if patientdetails.visit_type == "i"]${patientdetails.bill_bed_type!}[#else][/#if][#t]
[/#assign]
[#assign distitle]
  [#if patientdetails.visit_type == "i"]
    [#if (patientdetails.discharge_type!)=="Expiry"]Death Date:[#else]Disch Date:[/#if][#t]
    [#else]Discharge Date:[#t]
  [/#if]
[/#assign]
[#assign dischdate]
  [#if patientdetails.visit_type == "i"]
    ${patientdetails.discharge_date!}[#t]
  [/#if]
[/#assign]
[#if ((patientdetails.refdoctorname!'')!='')]
	[#assign referredby = "${patientdetails.refdoctorname!}"]
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


Prescriptions:
[#if presMedicines?has_content]

	Medicines:
	[#assign prescribedate = "${presMedicines[0].mod_time!}"]
		Prescribed Date:  ${lf(prescribedate,       20)}

	[#assign medicine_name= presMedicines[0].item_name!?html]
	[#assign generic_name=presMedicines[0].generic_name!?html]
[#if medicine_name != '']
${lf("Medicine Name", 28)} ${lf("Dosage", 10)} ${lf("Freq.", 10)} ${lf("Duration", 5)} ${lf("Qty", 5)} ${lf("Remarks", 28)}
-----------------------------------------------------------------------------
	[#list presMedicines as medicine]
		[#assign medicine_name= medicine.item_name!?html]
		[#assign dosage="${medicine.strength!} ${medicine.consumption_uom!}"]
		${lf(medicine_name!, 28)?html} ${lf(dosage, 10)} ${lf(medicine.frequency!,  10)?html} [#if medicine.duration?has_content]${rf(medicine.duration?string("0"),  5)}[/#if] ${rf(medicine.duration_units!, 1)} ${rf(medicine.medicine_quantity!,  5)} ${lf(medicine.medicine_remarks!,  28)?html}[#lt]
	[/#list]
[#else]
${lf("Generic Name", 20)} ${lf("Form", 8)} ${lf("Strength", 8)} ${lf("Dosage", 10)} ${lf("Freq.", 8)} ${lf("Duration", 5)} ${lf("Qty", 5)} ${lf("Remarks", 20)}
-----------------------------------------------------------------------------
	[#list presMedicines as medicine]
		[#assign generic_name=medicine.generic_name!?html]
		[#if medicine.non_hosp_medicine]
			[#assign generic_name=medicine.item_name?html+'[Non Hosp]']
		[/#if]
		[#assign dosage="${medicine.strength!} ${medicine.consumption_uom!}"]
		[#assign strength="${medicine.item_strength!} ${medicine.unit_name!}"]]
		${lf(generic_name!, 20)?html} ${lf(medicine.item_form_name!, 8)?html} ${lf(strength, 8)} ${lf(dosage, 10)} ${lf(medicine.frequency!,  8)?html} [#if medicine.duration?has_content]${rf(medicine.duration?string("0"),  5)}[/#if] ${rf(medicine.duration_units!, 1)} ${rf(medicine.medicine_quantity!,  5)} ${lf(medicine.medicine_remarks!,  20)?html} [#lt]
	[/#list]
[/#if]
[/#if]
