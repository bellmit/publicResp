[#include "TextPrintMacros.ftl"]
[#setting time_format="HH:mm"]
[#setting date_format="dd-MM-yyyy"]
[#setting datetime_format="dd-MM-yyyy"]
[#assign paymentTypeDisplay = {"R":"Receipt","S":"Sponsor","F":"Refund"}]
[#assign receiptTypeDisplay = {"A":"Advance","S":"Settlement"}]
[#assign totalAmount = totalAmount + roundOff]
[#assign isTpa = patient.tpa_name?? && (patient.tpa_name != '') ]


[#assign title]
  [#if bill.bill_type=="P" && bill.total_amount >0]Bill Cum Receipt[#rt]
  [#else]
  	[#if bill.total_amount <0 ]Credit Note[#rt]
    [#elseif bill.status == "C" ]Final Bill - Copy[#rt]
    [#elseif bill.status == "A" ]Provisional Bill[#rt]
    [#else]Final Bill[#rt]
    [/#if]
  [/#if]
[/#assign]
[@cfill title 80/]

[#assign name = "${patient.full_name!}"]
[#assign age]
  ${patient.age?string('#')}[#if patient.visit_type != "t"]${patient.agein!} ${patient.patient_gender!}[#else]${patient.age_unit!} ${patient.gender!}[/#if][#t]
[/#assign]
[#assign location = "${patient.cityname!}, ${patient.statename!}"]
[#assign admitted]
  [#if patient.visit_type == "i"]Admit Date:[#else]Regd. Date:[/#if][#t]
[/#assign]
[#assign regdate = "${patient.reg_date!}"]
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

Name:      ${lf(name!,                     46)} Bill No:      ${lf(bill.bill_no,16)}
Age/Gender:${lf(age,                       46)} Bill Date:    ${lf(bill.finalized_date!,16)}
Address:   ${lf(patient.patient_address!,  46)} MR No:        ${lf(patient.mr_no!,16)}
Location:  ${lf(location!,                 46)} Visit ID:     ${lf(patient.patient_id!,16)}
Doctor:    ${lf(patient.doctor_name!!,     46)} ${admitted!}   ${lf(regdate!,16)}
Dept:      ${lf(patient.dept_name!,        46)} ${distitle!}   ${lf(dischdate!,16)}
Rate Plan: ${lf(patient.org_name!,         46)} ${bedtitle!}   ${lf(wardnbed!,25)}
[#if (patient.tpa_name??) || (patient.refdoctorname??)]
Sponsor:   ${lf(patient.tpa_name!,         46)} Referred:     ${lf(patient.refdoctorname!,25)}
[#if bill.procedure_no != 0 ]
Procedure: ${lf(bill.procedure_name!,     33)} Code:         ${lf(bill.procedure_code!,25)}
Limit(Rs): ${lf(bill.procedure_limit!,     33)}
[/#if]
[/#if]
[#if patient.tpa_name??]
[#if patient.insurance_id?? && (patient.insurance_id!0) != 0]
Case No: ${lf(patient.insurance_id?string('#'),48)}	Insurance No: ${lf(patient.insurance_no!,16)}
[/#if]
[#if patient.policy_no??]
Policy Holder: ${lf(patient.policy_holder_name!,43)}Policy No: ${lf(patient.policy_no!,16)}
Policy Name: ${lf(patient.plan_name!,46)}
[/#if]
[/#if]
[#if patient.insurance_co_name?? ]
Ins.Comp:  ${lf(patient.insurance_co_name!,46)}
[/#if]
------------------------------------------------------------------------------------------------------------------------------------------
Charges      Ord#  Head          Description                Rate  Qty     Amount    Tax Rate   Tax Amount   Tax Groups     Tax Sub Groups
-------------------------------------------------------------------------------------------------------------------------------------------
[#if (multivisitPackage.package_name)?has_content]
Package : ${lf(multivisitPackage.package_name!,46)}

[/#if]
[#list chargeGroups as cg][#if !cg?starts_with("Discounts") || roundOff != 0]
[#assign subTotal=0]
${cg}
  [#list chargeGroupMap[cg] as charge]
	[#if charge.charge_head != "BIDIS"]
    ${""} ${charge.posted_date} ${""} [#t]
    [#if charge.order_number??]
    	${lf(charge.order_number?string("#"),5)} [#t]
    [#else]${lf("",6)}[#t]
    [/#if]
    ${lf(charge.chargehead_name,13)} ${lf(charge.act_description!,21)}[#t]
    ${""}  ${rf(charge.act_rate,8)} ${rf(charge.act_quantity?string("#0.00"),4)}[#t]
    ${""} ${rf(charge.amount + charge.discount,17)}[#t]
    [#if billChargeTaxGroupMap[charge.charge_id]?has_content ]
  	[#list billChargeTaxGroupMap[charge.charge_id] as taxCharge]
  	${rf(taxCharge.tax_rate,8)}[#t]${rf(taxCharge.tax_amount,8)}[#t]${rf(taxCharge.item_group_name,10)}[#t]${rf(taxCharge.item_subgroup_name,12)}[#t]
  	[/#list]
	 [/#if]
    [#lt]
    [#assign subTotal=subTotal+(charge.amount + charge.discount)]
	[/#if]
  [/#list]
                               ${rf(cg + " Sub Total",          37)}: ${rf(subTotal,17)}
[/#if][/#list]
                                                                     -----------
                                                          Total Bill: ${rf(totalAmount+totalDiscount,17)}

[#if hasDiscounts]
--------------------------------------------------------------------------------
Discounts   Head            Description                                   Amount
--------------------------------------------------------------------------------
[#list charges as charge][#if (charge.status != 'X') && (charge.discount != 0)]
 ${charge.posted_date} ${lf(charge.chargehead_name,17)} ${lf(charge.act_description!,41)} ${rf(charge.discount,17)}
[/#if]
[/#list]
                                                                     -----------
                                                      Total Discount: ${rf(totalDiscount,17)}

[/#if]
[#if receipts?has_content]
--------------------------------------------------------------------------------
Payments    Receipt No    Mode    Card Type        Bank      Ref. No      Amount
--------------------------------------------------------------------------------
[#list ["R","S","F"] as type][#if receiptTypeMap[type]?has_content]
[#list receiptTypeMap[type] as receipt]
[#if type == "R"]${receiptTypeDisplay[receipt.recpt_type]}[/#if][#t]
[#if type == "F"]${paymentTypeDisplay[type]}[/#if][#t]
[#if type == "S"]${paymentTypeDisplay[type]} - ${receiptTypeDisplay[receipt.recpt_type]}[/#if]
${rf(receipt.display_date,11)} ${lf(receipt.receipt_no,17)} ${lf(receipt.payment_mode_name,17)}[#t]
${lf(receipt.card_type!,8)}${lf(receipt.bank_name!,16)} ${lf(receipt.reference_no!,12)} ${rf(receipt.amount,17)}[#lt]
[/#list]
[/#if][/#list]
                                                                     -----------
                                                        Net Payments: ${rf(bill.total_receipts,17)}

[/#if]
[#if billDeposits >0]
Amount from Deposits: ${billDeposits}
[/#if]
[#if bill.points_redeemed?? && bill.points_redeemed > 0]
Amount from Points Redeemed: ${bill.points_redeemed_amt}
[/#if]
[#if bill.bill_type !="C"]
Received with thanks: ${netPaymentsWords} only
[/#if]
[#if  (bill.sponsor_bill_no!) !='']
[#assign sponsorPayments = bill.claim_recd_amount!0][#t]
[#else]
[#assign sponsorPayments = bill.primary_total_sponsor_receipts + bill.secondary_total_sponsor_receipts][#t]
[/#if]
[#if bill.bill_type == "C"]
--------------------------------------------------------------------------------
Bill Summary
--------------------------------------------------------------------------------
                                                   Total Bill Amount: ${rf(totalAmount + totalDiscount,17)}
                                                       Less Discount: ${rf(totalDiscount,17)}
[#if !isTpa]
                                                   Less Net Payments: ${rf(bill.total_receipts,17)}
[#if billDeposits?? && billDeposits!=0]
                                               Less Deposit Set Offs: ${rf(billDeposits,17)}
                                                            		 -----------
[/#if]
[#if bill.points_redeemed?? && bill.points_redeemed > 0]
                                            Less Points Redeemed Amt: ${rf(bill.points_redeemed_amt!,17)}
                                                                     -----------
[/#if]
                                                                     -----------
                                                         Balance Due: ${rf(totalAmount-billDeposits - bill.points_redeemed_amt - bill.total_receipts,17)}
[#else]
                                                                     -----------
                                                          Net Amount: ${rf(totalAmount,17)}
                                                                     -----------
                                            Deductions (Patient Amt): ${rf(bill.insurance_deduction,17)}
                                               Less Patient Payments: ${rf(bill.total_receipts,17)}
[#if billDeposits?? && billDeposits!=0]
                                               Less Deposit Set Offs: ${rf(billDeposits,17)}
                                                                     -----------
[/#if]
                                                         Patient Due: ${rf(totalAmount - bill.total_receipts - billDeposits - bill.points_redeemed_amt - bill.primary_total_claim - bill.secondary_total_claim ,17)}
                                                                     -----------
                                                      Sponsor Amount: ${rf(bill.primary_total_claim + bill.secondary_total_claim,17)}
                                               Less Sponsor Payments: ${rf(sponsorPayments,17)}
                                                         Sponsor Due: ${rf(bill.primary_total_claim + bill.secondary_total_claim - sponsorPayments,17)}
                                                                     -----------
[/#if]
[/#if]

Signature

[#if user != ""](${user})[/#if]
