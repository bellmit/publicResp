[#include "TextPrintMacros.ftl"]
[#setting time_format="HH:mm"]
[#setting date_format="dd-MM-yyyy"]
[#setting datetime_format="dd-MM-yyyy"]
[#escape x as x?html]
[#assign title]
		[#if type == "F" ]REFUND[#rt]
		[#elseif type == "R"]RECEIPT[#rt]
		[#elseif type == "S"]SPONSOR RECEIPT[#rt]
		[/#if]
[/#assign]
[@cfill title 80/]

[#if visitType == "r"]
Name:         ${lf(patient.customer_name,    	46)} Receipt No:      ${lf(receiptOrrefund.receipt_no,16)}
Sponsor:      ${lf(patient.sponsor_name!,    	46)} Receipt Date:    ${lf(receiptOrrefund.display_date!,16)}
Visited Date: ${lf(patient.visit_date,          46)} Visit ID:        ${lf(patient.customer_id,16)}
[#elseif visitType == "t"]
	[#assign age = "${patient.patient_age?string('#')} ${patient.patient_gender}"][#t]
Name:         ${lf(patient.patient_name,     	46)} Receipt No:      ${lf(receiptOrrefund.receipt_no,16)}
Age/Gender:   ${lf(age, 						46)} Receipt Date:    ${lf(receiptOrrefund.display_date!,16)}
Address:      ${lf(patient.address!,         	46)} Visit ID:        ${lf(patient.incoming_visit_id,16)}
Referred By:  ${lf(patient.referral!,           46)} Regd. Date:      ${lf(patient.date!,16)}
[#else]
	[#assign name = "${patient.full_name!}"]
	[#assign age = "${patient.age?string('#')} ${patient.agein} ${patient.patient_gender}"]
	[#assign location = "${patient.cityname}, ${patient.statename}"]
	[#assign admitted]
	  [#if patient.visit_type == "i"]Admit Date:[#else]Regd. Date:[/#if][#t]
	[/#assign]
	[#assign regdate = "${patient.reg_date}"]
	[#assign bedtitle]
	  [#if patient.visit_type == "i"]Bed Type:  [#else]           [/#if][#t]
	[/#assign]
	[#assign wardnbed]
	  [#if patient.visit_type == "i"]${patient.bill_bed_type!}[#else][/#if][#t]
	[/#assign]
	[#assign distitle]
	  [#if patient.visit_type == "i"]
	    [#if (patient.discharge_type!)=="Expiry"]Death Date:[#else]Disch Date:[/#if][#t]
	  [/#if]
	[/#assign]
	[#assign dischdate]
	  [#if patient.visit_type == "i"]
	    ${patient.discharge_date!}[#t]
	  [/#if]
	[/#assign]
Name:      ${lf(name,                           46)} Receipt No:      ${lf(receiptOrrefund.receipt_no,16)}
Age/Gender:${lf(age,                            46)} Receipt Date:    ${lf(receiptOrrefund.display_date!,16)}
Address:   ${lf(patient.patient_address!,       46)} MR No:           ${lf(patient.mr_no,16)}
Location:  ${lf(location,                       46)} Visit ID:        ${lf(patient.patient_id,16)}
Doctor:    ${lf(patient.doctor_name!,           46)} ${admitted}      ${lf(regdate,16)}
Dept:      ${lf(patient.dept_name!,             46)} ${distitle}      ${lf(dischdate,16)}
Rate Plan: ${lf(patient.org_name, 		        46)} ${bedtitle}      ${lf(wardnbed!,16)}
[#if (patient.tpa_name??) || (patient.refdoctorname??)]
Sponsor:   ${lf(patient.tpa_name!,              46)} Referred:        ${lf(patient.refdoctorname!,16)}
[#if bill.procedure_no != 0 ]
Procedure: ${lf(bill.procedure_name!,           46)} Code:            ${lf(bill.procedure_code!,16)}
Limit(Rs): ${lf(bill.procedure_limit,           46)}
[/#if]
[/#if]

[/#if]
-----------------------------------------------------------------------------------------
[#if type == "F"]
Refunded Rs.  ${lf(receiptOrrefund.amount,      46)}
[#else]
[#assign paymentType][#if "${receiptOrrefund.recpt_type}" =="A"]Advance[#else]Settlement[/#if][#t][/#assign]
Received Rs.  ${lf(receiptOrrefund.amount,      46)} Towards:         ${lf(paymentType, 16)}
[/#if][#t]
Payment Mode: ${lf(receiptOrrefund.payment_mode_name,46)} Against Bill No: ${lf(receiptOrrefund.bill_no, 16)}
[#if receiptOrrefund.card_type?? && receiptOrrefund.card_type !=""]
Card Type:    ${lf(receiptOrrefund.card_type,   46)}
[/#if][#t]
[#if (receiptOrrefund.bank_name?? && receiptOrrefund.bank_name !="") || (receiptOrrefund.reference_no?? && receiptOrrefund.reference_no !="")]
Bank:	      ${lf(receiptOrrefund.bank_name,   46)} Reference:       ${lf(receiptOrrefund.reference_no!,16)}
[/#if][#t]
[#if type == "F"]
Net deposits against this bill:Rs. ${receiptOrrefund.total_receipts}
[#else]
Net amount received against this bill:Rs. ${receiptOrrefund.total_receipts}
[/#if][#t]
[#if receiptOrrefund.tds_amount > 0]
TDS Amount Received: ${receiptOrrefund.tds_amount}
[/#if][#t]
[#if type == "F"]
Received with thanks: ${netPayments} Only.
[#else]
Received with thanks: ${netPayments} Only.
[/#if][#t]

[#if "${receiptOrrefund.paid_by!}" !=""]
Paid By: ${receiptOrrefund.paid_by}
[/#if]

Signature
(${receiptOrrefund.username})
[/#escape]
