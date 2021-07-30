[#include "TextPrintMacros.ftl"]
[#setting time_format="HH:mm"]
[#setting date_format="dd-MM-yyyy"]
[#setting datetime_format="dd-MM-yyyy HH:mm"]
[#setting number_format="#"]
Dental Consultation[#rt]

[#assign name = "${patientdetails.full_name!}"]
[#if patientdetails.dateofbirth?has_content]
	[#assign date_of_birth="(${patientdetails.dateofbirth})"]
[/#if]
[#if patientdetails.patient_gender?has_content]
	[#if patientdetails.patient_gender == 'M']
		[#assign gender='Male']
	[#else]
		[#assign gender='Female']
	[/#if]
[/#if]
[#assign age = "${patientdetails.age_text} ${date_of_birth!} / ${gender}"]
[#assign mrno = "${patientdetails.mr_no!}"]
[#assign contactNo = patientdetails.patient_phone!]

Name:      ${lf(name,                     35)}  MR NO:       ${lf(mrno,                30)}

Age/Gender:${lf(age,                      35)}  Contact No.   ${lf(contactNo, 			30)}
[#if treatments?has_content]

Treatment Details:
------------------------------------------------------------------------------------------------------
[#list treatments as treatment]
[#if treatment.treatment_status == 'P']
	[#assign treatment_status='Planned']
[#elseif treatment.treatment_status == 'C']
	[#assign treatment_status='Completed']
[#elseif treatment.treatment_status == 'I']
	[#assign treatment_status='In Progress']
[#else]
	[#assign treatment_status='Cancelled']
[/#if]
[#if (treatment.service_prescribed_id!0) == 0]
	[#assign billing_status='Order']
[#else]
	[#assign billing_status='Ordered']
[/#if]
[#if treatment.tooth_unv_number?has_content]
	[#assign tooth_number="${treatment.tooth_unv_number!}"]
[#else]
	[#assign tooth_number="${treatment.tooth_fdi_number!}"]
[/#if]
[#assign complCount = 0]
[#assign hasTasks = false]
[#if service_sub_tasks['TRTMT'+treatment.treatment_id]??]
	[#list service_sub_tasks['TRTMT'+treatment.treatment_id] as task]
		[#assign hasTasks = true]
		[#if task.status == 'C' || task.status == 'NR']
			[#assign complCount = complCount+1]
		[/#if]
	[/#list]
[/#if]
[#assign taskStatus = 'None']
[#if hasTasks]
	[#assign taskCount = service_sub_tasks['TRTMT'+treatment.treatment_id]?size]
	[#if complCount == 0]
		[#assign taskStatus = 'Not Completed']
	[#elseif complCount > 0 && complCount == taskCount]
		[#assign taskStatus = 'Completed']
	[#elseif complCount > 0 && complCount < taskCount]
		[#assign taskStatus = 'Partial']
	[/#if]
[/#if]
 ${lf("Tooth",10)} ${lf(tooth_number, 10)}
 ${lf("Service Name", 15)?html} ${lf(treatment.service_name, 15)?html}
 ${lf("Status", 15)?html}  ${lf(treatment_status, 10)}
 ${lf('Pln. By', 15)?html} ${lf(treatment.planned_by_name!, 10)?html}
 ${lf('Pln. Date', 16)} ${lf(treatment.planned_date!?html, 16)}
 ${lf('Start Date', 16)}  ${lf(treatment.start_date!?html, 16)}
 ${lf('Compl. Date', 16)} ${lf(treatment.completed_date!?html, 16)}
 ${lf('Compl. By', 20)?html} ${lf(treatment.completed_by_name!, 20)?html}
 ${lf('Ordered Status', 8)} ${lf(billing_status, 8)}
 ${lf('Service Subtask Status', 15)}  ${lf(taskStatus, 15)}
 ${lf('Comments', 20)?html} ${lf(treatment.comments!, 20)?html}

[/#list]
[/#if]

Prescriptions:
[#if presMedicines?has_content]

	[#assign prescribedate = "${presMedicines[0].mod_time!}"]
		Prescribed Date:  ${lf(prescribedate,       20)}

	[#assign medicine_name= presMedicines[0].item_name!?html]
	[#assign generic_name=presMedicines[0].generic_name!?html]
[#if medicine_name == '']
${lf("Medicine Name", 28)} ${lf("Dosage", 10)} ${lf("Days", 5)} ${lf("Qty", 5)} ${lf("Remarks", 28)}
-----------------------------------------------------------------------------
	[#list presMedicines as medicine]
		[#assign medicine_name= medicine.item_name!?html]
		${lf(medicine_name!, 28)?html} ${lf(medicine.medicine_dosage!,  10)?html} ${rf(medicine.medicine_days!,  5)} ${rf(medicine.medicine_quantity!,  5)} ${lf(medicine.item_remarks!,  28)?html}[#lt]
	[/#list]
[#else]
${lf("Generic Name", 20)} ${lf("Form", 8)} ${lf("Strength", 8)} ${lf("Dosage", 8)} ${lf("Days", 5)} ${lf("Qty", 5)} ${lf("Remarks", 20)}
-----------------------------------------------------------------------------
	[#list presMedicines as medicine]
		[#assign generic_name=medicine.generic_name!?html]
		${lf(generic_name!, 20)?html} ${lf(medicine.item_form_name!, 8)?html} ${lf(medicine.item_strength!, 5)?html} ${lf(medicine.unit_name!, 3)?html} ${lf(medicine.frequency!,  8)?html} ${rf(medicine.medicine_days!,  5)} ${rf(medicine.medicine_quantity!,  5)} ${lf(medicine.medicine_remarks!,  20)?html} [#lt]
	[/#list]
[/#if]

[/#if]

[#if presTests?has_content]

	Investigations:
	[#assign prescribedate = "${presTests[0].mod_time!}"]
		Prescribed Date:  ${lf(prescribedate,       20)}

		Investigation  			Remarks
		---------------------------------------
	[#list presTests as test]
		${lf(test.item_name!,    30)?html}	${lf(test.item_remarks!,  30)?html}
	[/#list]
[/#if]
[#if presServices?has_content]

	Services:
	[#assign prescribedate = "${presServices[0].mod_time!}"]
		Prescribed Date:  ${lf(prescribedate,       20)}

		Service Name			Qty  Tooth No. Remarks
		----------------------------------------
	[#list presServices as service]
		[#if service.tooth_unv_number?has_content]
			[#assign tooth_number=service.tooth_unv_number]
		[#elseif service.tooth_fdi_number?has_content]
			[#assign tooth_number=service.tooth_fdi_number]
		[/#if]
		${lf(service.item_name!,    30)?html}	${lf(service.service_qty, 5)} ${lf(tooth_number!, 5)} ${lf(service.item_remarks!,  30)?html}
	[/#list]
[/#if]
