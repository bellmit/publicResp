[#include "TextPrintMacros.ftl"]
[#setting time_format="HH:mm"]
[#setting date_format="dd-MM-yyyy"]
[#setting datetime_format="dd-MM-yyyy HH:mm"]

[@lfill "Tin: ${sale.tin_no}" 37/] [@rfill "DL No: ${sale.dl_no}" 36/]

[#assign title][#t]
  [#if duplicate]Duplicate [/#if][#t]
	[#if bill.billType=="C"]Credit [#else]Cash [/#if][#t]
  [#if sale.type=="R"]Refund[#else]Bill[/#if][#t]
[/#assign][#t]
[@cfill title 74/]

[#assign name]
  [#if bill.visitType == "r"]
    ${customer.customer_name}[#t]
  [#else]
    ${patient.full_name}[#t]
  [/#if]
[/#assign]

Patient:       [@lfill name 20 /] [@rfill "Doctor:" 17/] [@rfill doctorName!"" 20 /]
Bill No:       [@lfill sale.sale_id 20/] [@rfill "Date:" 17/] [@rfill sale.sale_date 20 /]
[#if bill.billType=="C"]
Hosp. Bill No: ${bill.billNo}
[/#if]
[#if bill.visitType != "r"]
MR No:         ${patient.mr_no} [#if patient.org_name != "GENERAL"](${patient.org_name})[/#if]
[/#if]

--------------------------------------------------------------------------
#  Particulars               Mfr  Batch   Expiry       Rate   Qty   Amount
--------------------------------------------------------------------------
[#assign total=0  bill_discount=sale.discount!0  roundoff=sale.round_off!0  sno=1 depositSetOff=bill.depositSetOff!0 ]
[#assign rewardPointsRedeemedAmount=bill.rewardPointsRedeemedAmount!0]
[#list items as s]
 [@rfill sno?string("#") 2/] [@lfill s.medicine_short_name 25/] [@lfill s.manf_mnemonic 4/][#t]
 [@lfill s.batch_no 6/]  [#if s.expiry_date?has_content]${s.expiry_date?string("MMM-yyyy")}[/#if] [@rfill s.rate 8/][#rt]
 [@rfill s.quantity?string("#.##") 5/] [@rfill s.amount 8/]
 [#assign total=(total+s.amount)  sno=sno+1]
[/#list]
--------------------------------------------------------------------------
[#if (bill_discount != 0)]
  Bill Discount[#if (sale.discount_per != 0)] @${sale.discount_per?string("##.00")}%[/#if]: ${bill_discount}[#t]
  [#assign total=(total - bill_discount)]
[/#if]
[#if roundoff !=0.00]
  Round Off: ${roundoff}[#t]
  [#assign total=(total + roundoff)]
[/#if]
[#if depositSetOff !=0.00 && bill.billType!="C"]
  Deposit Set Off: ${depositSetOff}[#t]
  [#assign total=(total - depositSetOff)]
[/#if]
[#if bill.rewardPointsRedeemed > 0 && bill.billType!="C"]
  Points Redeemed Amt: ${rewardPointsRedeemedAmount}[#t]
  [#assign total=(total - rewardPointsRedeemedAmount)]
[/#if]
[#if total !=0.00]
[#assign payMode]
  [#if bill.billType =="C"]
    (Added to Bill)[#t]
  [#elseif refund[0]??]
    ${refund[0].paymentMode} (${refund[0].cardType!})[#t]
  [#elseif receipt[0]??]
    ${receipt[0].paymentMode} (${receipt[0].cardType!})[#t]
  [/#if]
[/#assign]

[@lfill "Payment Mode:" 15/][@lfill payMode 25/][@rfill "Total: " 25/][@rfill total 9/]
[/#if]

[#if "${taxLabel.procurement_tax_label}" == "V"]
	[#list vatDetails?keys as rate]
	VAT @${rate}%: ${vatDetails[rate]}
	[/#list]
	[#else]
	[#list vatDetails?keys as rate]
	GST @${rate}%: ${vatDetails[rate]}
	[/#list]
[/#if]



${sale.user_display_name}

