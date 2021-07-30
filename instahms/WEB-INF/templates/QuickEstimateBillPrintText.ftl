[#include "TextPrintMacros.ftl"]
[#setting time_format="HH:mm"]
[#setting date_format="dd-MM-yyyy"]
[#setting datetime_format="dd-MM-yyyy HH:mm"]

[#assign name = "${estimateBill.person_name!}"]
[#assign estimateno = "${estimateBill.estimate_no?string.number!}"]
[#assign date = "${estimateBill.estimate_date!}"]
[#assign visittype]
	[#if (estimateBill.visit_type == "i")]
IN Patient
	[/#if]
	[#if (estimateBill.visit_type == "o")]
OUT Patient
	[/#if]
[/#assign]
[#assign rateplan = "${estimateBill.org_name!}"]
[#assign bedtype = "${estimateBill.bed_type!}"]
[#assign categoryname = "${estimateBill.category_name!}"]
[#assign insurancecompay = "${estimateBill.insurance_co_name!}"]
[#assign plantype = "${estimateBill.plan_type_name!}"]
[#assign planname = "${estimateBill.plan_name!}"]
[#assign remarks = "${estimateBill.remarks!}"]

Person Name: ${lf(name, 45)} Estimate No: ${lf(estimateno, 10)}

Estimate date: ${lf(date, 35)} Visit Type: ${lf(visittype, 10)}

Rate Plan: ${lf(rateplan, 49)} Bed Type: ${lf(bedtype, 10)}

Patient Category: ${lf(categoryname, 35)} Insurance Co.: ${lf(insurancecompay, 20)}

Network/Plan Type: ${lf(plantype, 25)} Plan Name: ${lf(planname, 20)}

[#if (remarks)?? && (remarks != "") ]
Remarks: ${lf(remarks,                     100)}
[/#if]

------------------------------------------------------------------------------------------------------------------------
${lf("Head", 8)} ${lf("Description", 12)} ${lf("Remarks", 9)} ${lf("Rate", 8)} ${lf("Qty", 8)} ${lf("Disc.", 8)} ${lf("Amt.", 9)} ${lf("Tax.", 5)} ${lf("Sponsor Amt.", 12)} ${lf("Sponsor Tax.", 12)} ${lf("Patient Amt.", 12)} ${lf("Patient Tax.", 12)}
------------------------------------------------------------------------------------------------------------------------
[#list chargeGroups as cg]
	[#assign i=0]
	[#list chargeGroupMap[cg] as charge]
		[#if charge.status != 'X' ]
			[#assign i= i+1]
		[/#if]
	[/#list]
	[#if i > 0 ]
		[#list chargeGroupMap[cg] as charge]
			[#if charge.status != 'X' ]
			[#assign head=charge.chargehead_name!?html]
			[#assign description=charge.act_description!?html]
			[#assign remarks=charge.remarks!?html]
			[#assign rate=charge.act_rate!?html]
			[#assign quantity=charge.act_quantity!?html]
			[#assign discount=charge.discount!?html]
			[#assign amount=charge.amount!?html]
			[#assign tax=charge.tax_amt!?html]
			[#assign sponsoramt=charge.sponsor_amt!?html]
			[#assign sponsortax=charge.sponsor_tax!?html]
			[#assign patientamt=charge.patient_amt!?html]
			[#assign patienttax=charge.patient_tax!?html]
${lf(head!, 8)?html} ${lf(description!, 12)?html} ${lf(remarks!, 10)?html} ${lf(rate!, 8)?html} ${lf(quantity!,8)?html} ${lf(discount!, 8)?html} ${lf(amount!, 8)?html} ${lf(tax!, 8)?html} ${lf(sponsoramt!, 18)?html} ${lf(sponsortax!, 20)?html} ${lf(patientamt!, 18)?html} ${lf(patienttax!, 10)?html}
			[/#if]
		[/#list]
	[/#if]
[/#list]

[#assign totalAmount = 0]
[#assign totalTax = 0]
[#assign totalsponsorAmount = 0]
[#assign totalsponsorTaxAmount = 0]
[#assign totalpatientAmount = 0]
[#assign totalpatientTaxAmount = 0]
[#assign totalDiscountAmount = 0]
[#assign totalNetAmount = 0]
[#list charges as charge]
	[#if charge.status != 'X' ]
		[#assign totalDiscountAmount = totalDiscountAmount + charge.discount!0]
		[#assign totalTax = totalTax + charge.tax_amt!0]
		[#assign totalNetAmount = totalNetAmount + charge.amount]
		[#assign totalAmount = totalAmount + charge.amount + charge.discount!0]
		[#assign totalsponsorAmount = totalsponsorAmount + charge.sponsor_amt!0]
		[#assign totalsponsorTaxAmount = totalsponsorTaxAmount + charge.sponsor_tax!0]
		[#assign totalpatientTaxAmount = totalpatientTaxAmount + charge.patient_tax!0]
	[/#if]
[/#list]
[#assign totalpatientAmount = totalNetAmount - totalsponsorAmount]


																																																	-----------------------------
																																																		Estimated Amount: ${totalAmount!}
																																																	-----------------------------
																																																		Net Amount: ${totalNetAmount!}
																																																	-----------------------------
																																																		Net Tax: ${totalTax!}
																																																	-----------------------------
																																																		Discounts: ${totalDiscountAmount!}
																																																	-----------------------------
																																																		Sponsor Amount: ${totalsponsorAmount!}
																																																	-----------------------------
																																																		Sponsor Tax: ${totalsponsorTaxAmount!}
																																																	-----------------------------
																																																		Patient Amount: ${totalpatientAmount!}
																																																	-----------------------------
																																																		Patient Tax: ${totalpatientTaxAmount!}
																																																	-----------------------------




																																																																							Signature


																																																																						(${estimateBill.username!})









