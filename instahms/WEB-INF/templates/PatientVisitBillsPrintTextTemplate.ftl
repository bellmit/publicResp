[#include "TextPrintMacros.ftl"]
[#setting time_format="HH:mm"]
[#setting date_format="dd-MM-yyyy"]
[#setting datetime_format="dd-MM-yyyy"]
[#assign paymentTypeDisplay = {"R":"Receipt","S":"Sponsor","F":"Refund"}]
[#assign receiptTypeDisplay = {"A":"Advance","S":"Settlement"}]
[#assign title]
  Patient Visit Bills Print Template
[/#assign]
[@cfill title 80/]

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
[#assign allinsuranceDeduction = 0]
[#assign allsponsorPayments = 0]
[#assign totalReceipt = 0]
[#assign receiptTypeDisplay = {"A":"Advance","S":"Settlement"}]
[#assign totalPatientRoundOff=0]
[#assign totalInsRoundOff=0]

Name:       ${lf(name,                     46)}
Age/Gender: ${lf(age,                      46)}
Address:    ${lf(patient.patient_address!, 46)} MR No:        ${lf(patient.mr_no,16)}
Location:   ${lf(location,                 46)} Visit ID:     ${lf(patient.patient_id,16)}
Doctor:     ${lf(patient.doctor_name!,     46)} ${admitted}   ${lf(regdate,16)}
Dept:       ${lf(patient.dept_name!,       46)} ${distitle}   ${lf(dischdate,16)}
Rate Plan:  ${lf(patient.org_name,         46)} ${bedtitle}   ${lf(wardnbed!,25)}
[#if (patient.tpa_name??) || (patient.refdoctorname??)]
Sponsor:    ${lf(patient.tpa_name!,        46)} Referred:     ${lf(patient.refdoctorname!,25)}
[/#if]
[#if patient.tpa_name??]
[#if patient.insurance_id?? && patient.insurance_id != 0]
Case No:  ${lf(patient.insurance_id?string('#'),48)}	Insurance No: ${lf(patient.insurance_no!,16)}
[/#if]
[#if patient.policy_no??]
Policy Holder: ${lf(patient.policy_holder_name!,43)}Policy No: ${lf(patient.policy_no!,16)}
Policy Name: ${lf(patient.plan_name!,46)}
[/#if]
[/#if]
[#if patient.insurance_co_name?? ]
Ins.Comp:   ${lf(patient.insurance_co_name!,46)}
[/#if]
[#list bill as patBill]
[#assign billRoundOff = patientRoundOff[patBill.billNo] + insRoundOff[patBill.billNo] ]
[#assign totalPatientRoundOff = totalPatientRoundOff + patientRoundOff[patBill.billNo]]
[#assign totalInsRoundOff = totalInsRoundOff + insRoundOff[patBill.billNo]]
------------------------------------------------------------------------------------
Charges      Ord#  Head          Description                Rate  Qty         Amount
------------------------------------------------------------------------------------
[#list chargeGroups[patBill.billNo] as cg][#if !cg?starts_with("Discounts") || (patientRoundOff[patBill.billNo] != 0 || insRoundOff[patBill.billNo] != 0)]
[#assign subTotal=0]
${cg}
  [#list chargeGroupMap[patBill.billNo][cg] as charge]
	[#if charge.charge_head != "BIDIS"]
    ${""} ${charge.posted_date} ${""} [#t]
    [#if charge.order_number??]
    	${lf(charge.order_number?string("#"),5)} [#t]
    [#else]${lf("",6)}[#t]
    [/#if]
    ${lf(charge.chargehead_name,13)} ${lf(charge.act_description!,21)}[#t]
    ${""}  ${rf(charge.act_rate,8)} ${rf(charge.act_quantity?string("#0.00"),4)}[#t]
    ${""} 		  ${rf(charge.amount + charge.discount,10)}[#lt]
    [#assign subTotal=subTotal+(charge.amount + charge.discount)]
	[/#if]
  [/#list]
                              ${rf(cg + " Sub Total",          37)}:      ${rf(subTotal,10)}
[/#if][/#list]
                                                                     -----------------
                                                         Total Bill:       ${rf(totalAmount[patBill.billNo]+billRoundOff+totalDiscount[patBill.billNo],10)}

[#if hasDiscounts[patBill.billNo]]
--------------------------------------------------------------------------------------
Discounts   Head            Description                                        Amount
--------------------------------------------------------------------------------------
[#list charges[patBill.billNo] as charge][#if (charge.status != 'X') && (charge.discount != 0)]
 ${charge.posted_date} ${lf(charge.chargehead_name,15)} ${lf(charge.act_description!,41)} ${rf(charge.discount,10)}
[/#if]
[/#list]
                                                                         -----------------
                                                        Total Discount:     ${rf(totalDiscount[patBill.billNo],10)}

[/#if]
[#if receipts?has_content]
--------------------------------------------------------------------------------------
Payments    Receipt No    Mode    Card Type        Bank      Ref. No            Amount
--------------------------------------------------------------------------------------
[#list ["R","S","F"] as type][#if receiptTypeMap[patBill.billNo][type]?has_content]
[#list receiptTypeMap[patBill.billNo][type] as receipt]
[#if type == "R"]${receiptTypeDisplay[receipt.recpt_type]}[/#if][#t]
[#if type == "F"]${paymentTypeDisplay[type]}[/#if][#t]
[#if type == "S"]${paymentTypeDisplay[type]} - ${receiptTypeDisplay[receipt.recpt_type]}[/#if]
   ${rf(receipt.display_date,11)} ${lf(receipt.receipt_no,10)} ${lf(receipt.payment_mode_name,15)}[#t]
   ${lf(receipt.card_type!,8)}${lf(receipt.bank_name!,16)} ${lf(receipt.reference_no!,12)} ${rf(receipt.amount,10)}[#lt]
  [/#list]
[/#if][/#list]
                                                                         ---------------
                                                         Net Payments:      ${rf(patBill.totalReceipts,10)}

[/#if]
[#if  (patBill.sponsorBillNo!) !='']
[#assign sponsorPayments = patBill.claimRecdAmount!0][#t]
[#else]
[#assign sponsorPayments = patBill.totalPrimarySponsorReceipts + patBill.totalSecondarySponsorReceipts][#t]
[/#if]
----------------------------------------------------------------------------------------
Bill Expenses
----------------------------------------------------------------------------------------
[#assign totalReceipt = totalReceipt+patBill.totalReceipts]
                                                        Total Bill Amount: ${rf(totalAmount[patBill.billNo] + billRoundOff + totalDiscount[patBill.billNo],10)}
                                                            Less Discount: ${rf(totalDiscount[patBill.billNo],10)}
[#if !(patient.tpa_name??) || patient.tpa_name == '']
                                                        Less Net Payments: ${rf(patBill.totalReceipts,10)}
[#if billDeposits[patBill.billNo]?? && billDeposits[patBill.billNo]!=0]
                                                    Less Deposit Set Offs: ${rf(billDeposits[patBill.billNo],10)}
[/#if]
                                                                        -------------
                                                              Balance Due: ${rf(totalAmount[patBill.billNo]-billDeposits[patBill.billNo]- patBill.totalReceipts + billRoundOff,10)}
[#else]
                                                                        -------------
                                                               Net Amount: ${rf(totalAmount[patBill.billNo] + billRoundOff,10)}
                                                                        -------------
                                                 Deductions (Patient Amt): ${rf(totalAmount[patBill.billNo] + billRoundOff - (patBill.totalClaim  - patBill.insuranceDeduction),10)}
                                                    Less Patient Payments: ${rf(patBill.totalReceipts,10)}
[#if billDeposits[patBill.billNo]?? && billDeposits[patBill.billNo]!=0]
                                                    Less Deposit Set Offs: ${rf(billDeposits[patBill.billNo],10)}
                                                                        -------------
[/#if]
                                                              Patient Due: ${rf(totalAmount[patBill.billNo] + patientRoundOff[patBill.billNo] - patBill.totalReceipts - billDeposits[patBill.billNo] - (totalClaimAmount[patBill.billNo] -patBill.insuranceDeduction),10)}
                                                         [#assign allinsuranceDeduction = allinsuranceDeduction + patBill.insuranceDeduction]
                                                                        -------------
                                                         [#assign allsponsorPayments = allsponsorPayments + sponsorPayments ]
                                                           Sponsor Amount: ${rf(totalClaimAmount[patBill.billNo] + insRoundOff[patBill.billNo] - patBill.insuranceDeduction,10)}
                                                    Less Sponsor Payments: ${rf(sponsorPayments,10)}
                                                              Sponsor Due: ${rf(totalClaimAmount[patBill.billNo] + insRoundOff[patBill.billNo] - sponsorPayments-patBill.insuranceDeduction,10)}
                                                                        -------------
[/#if]
[/#list]
-------------------------------------------------------------------------------------
Total Expenses
-------------------------------------------------------------------------------------
                                                        Total Bill Amount: ${rf(alltotalAmount + allroundOff + alltotalDiscount,10)}
                                                            Less Discount: ${rf(alltotalDiscount,10)}
[#if !(patient.tpa_name??) || patient.tpa_name == '']
                                                        Less Net Payments: ${rf(totalReceipt,10)}
[#if allbillDeposits?? && allbillDeposits!=0]
                                                    Less Deposit Set Offs: ${rf(allbillDeposits,10)}
[/#if]
                                                                         ------------
                                                              Balance Due: ${rf(alltotalAmount + allroundOff - allbillDeposits - totalReceipt,10)}
[#else]
                                                                         ------------
                                                               Net Amount: ${rf(alltotalAmount + allroundOff,10)}
                                                                         ------------
                                                 Deductions (Patient Amt): ${rf(alltotalAmount + allroundOff - (alltotalClaimAmount  - allinsuranceDeduction),10)}
                                                    Less Patient Payments: ${rf(totalReceipt,10)}
[#if allbillDeposits?? && allbillDeposits!=0]
                                                    Less Deposit Set Offs: ${rf(allbillDeposits,10)}
                                                                         ------------
[/#if]
                                                              Patient Due: ${rf(alltotalAmount + totalPatientRoundOff - totalReceipt - (alltotalClaimAmount - allinsuranceDeduction - allbillDeposits),10)}
                                                                         ------------
                                                           Sponsor Amount: ${rf(alltotalClaimAmount + totalInsRoundOff - allinsuranceDeduction,10)}
                                                    Less Sponsor Payments: ${rf(allsponsorPayments,10)}
                                                              Sponsor Due: ${rf(alltotalClaimAmount + totalInsRoundOff - allsponsorPayments-allinsuranceDeduction,10)}
                                                                         ------------
[/#if]
