<!--
	[#setting time_format="HH:mm"]
	[#setting date_format="dd-MM-yyyy"]
	[#setting datetime_format="dd-MM-yyyy HH:mm"]
	[#assign empty={}]
-->
[#escape x as x?html]
<div align="center">
		[#if type == "F" ]<b><u>REFUND</u></b>
		[#elseif type == "R"]<b><u>RECEIPT</u></b>
		[#elseif type == "S"]<b><u>SPONSOR RECEIPT</u></b>
		[/#if]
</div>
<br/>
<div class="patientHeader" style="margin-bottom: 1em">
[#if visitType == "r"]
	<table width="100%" cellspacing="0" cellpadding="0">
		<tr>
			<td>Name:</td>
			<td>${patient.customer_name!}</td>
			<td>Receipt No:</td>
			<td>${receiptOrrefund.receipt_no}</td>
		</tr>
		<tr>
			<td>Sponsor:</td>
			<td>${patient.sponsor_name!}</td>
			<td>Receipt Date:</td>
			<td>${receiptOrrefund.display_date}</td>
		</tr>
		<tr>
			<td>Visited Date:</td>
			<td>${patient.visit_date}</td>
			<td>Visit ID:</td>
			<td>${patient.customer_id}</td>
		</tr>
	</table>
[#elseif visitType == "t"]
	<table width="100%" cellspacing="0" cellpadding="0">
		<tr>
			<td>Name:</td>
			<td>${patient.patient_name!}</td>
			<td>Receipt No:</td>
			<td>${receiptOrrefund.receipt_no}</td>
		</tr>
		<tr>
			<td>Age/Gender:</td>
			<td>${patient.patient_age?string('#')}${patient.age_unit!}/${patient.patient_gender}</td>
			<td>Receipt Date:</td>
			<td>${receiptOrrefund.display_date}</td>
		</tr>
		<tr>
			<td>Address:</td>
			<td>${patient.address!}</td>
			<td>Visit ID:</td>
			<td>${patient.incoming_visit_id}</td>
		</tr>
		<tr>
			<td>Referred By:</td>
			<td>${patient.referral!}</td>
			<td>Registered Date:</td>
			<td>${patient.date}</td>
		</tr>
	</table>
[#else]
	<table width="100%" cellspacing="0" cellpadding="0">
		<tr>
			<td>Name:</td>
			<td>${patient.full_name!}</td>
			<td>Receipt No:</td>
			<td>${receiptOrrefund.receipt_id}</td>
		</tr>
		<tr>
			<td>Age/Gender:</td>
			<td>${patient.age!?string("#")} ${patient.agein!} ${patient.patient_gender!}</td>
			<td>Receipt Date:</td>
			<td>${receiptOrrefund.display_date}</td>
		</tr>
		<tr>
			<td>Address:</td>
			<td>${patient.patient_address}</td>
			<td>MR No:</td>
			<td>${patient.mr_no}</td>
		</tr>
		<tr>
			<td>Location:</td>
			<td>${patient.cityname!},${patient.statename!}</td>
			<td>Visit ID:</td>
			<td>${patient.patient_id}</td>
		</tr>
		<tr>
			<td>Doctor:</td>
			<td>${patient.doctor_name!}</td>
			<td>
				[#if patient.visit_type == "i"]
					Admission Date:
				[#else]
					Registered Date:
				[/#if]
			</td>
			<td>${patient.reg_date} ${patient.reg_time}</td>
		</tr>
		<tr>
			<td>Department:</td>
			<td>${patient.dept_name!}</td>
			<!-- [#if patient.visit_type =='i'] -->
				<td>Ward/Bed</td>
				<td>${patient.bill_bed_type!}</td>
			<!-- [/#if] -->
		</tr>
		<tr>
			<td>Rate Plan:</td>
			<td>${patient.org_name}</td>
			<!-- [#if patient.visit_type =="i"] -->
				<td>[#if (patient.discharge_type!)=="Expiry"] Death Date: [#else] Discharge Date:[/#if]</td>
				<td>${patient.discharge_date!} ${patient.discharge_time!}</td>
			<!-- [/#if] -->
		</tr>
		<tr>
			<td>Sponsor:</td>
			<td>${patient.tpa_name!}</td>
			<td>Referred By:</td>
			<td>${patient.refdoctorname!}</td>
		</tr>
		<!-- [#if bill?? ] -->
		<tr>
			<td>Procedure:</td>
      <!-- [#if bill.procedure_name?? ] -->
                <td>${bill.procedure_name!}</td>
      <!-- [/#if] -->
			<td>Code:</td>
      <!-- [#if bill.procedure_code?? ] -->
                <td>${bill.procedure_code!}</td>
      <!-- [/#if] -->
		</tr>
		<tr>
			<td>Limit(Rs):</td>
      <!-- [#if bill.procedure_limit?? ] -->
                <td>${bill.procedure_limit!}</td>
      <!-- [/#if] -->
		</tr>
		<!-- [/#if] -->
		<tr>
			<td></td>
		</tr>
	</table>
[/#if]
</div>
<br/>
<div>
<table cellpadding="0" celspacing="0" width="100%" border="0">
		<tr>
			<td>
			[#if type == "F"]
				Refunded Amount: <b> ${currencySymbol} ${receiptOrrefund.amount}</b>
			[#else]
				Received Amount: <b> ${currencySymbol} ${receiptOrrefund.amount}</b>
			[/#if]
			</td>
			<td>
				[#if type == "F"]
				[#else]
					Towards: [#if !receiptOrrefund.is_settlement] Advance
					[#else]	 Settlement	[/#if]
				[/#if]
			</td>
		</tr>
    [#if receiptOrrefund.bill_no??]
      <tr>
        <td colspan="2">Against Bill No:  
              <b>${receiptOrrefund.bill_no}</b>
        </td>
      </tr>
    [/#if]
		<tr>
			<td align="left"> Payment Mode:	<b> ${receiptOrrefund.payment_mode_name}	</b> </td>
			[#if (receiptOrrefund.card_type??) && (receiptOrrefund.card_type !="")]
			<td>Card Type: <b>${receiptOrrefund.card_type!""}</b></td>
			[#else]
			<td></td>
			[/#if]
		</tr>

		<tr>
			[#if receiptOrrefund.bank_name?? && receiptOrrefund.bank_name !=""]
			<td>Bank:${receiptOrrefund.bank_name!""}</td>
			[#else]
			<td></td>
			[/#if]
			[#if receiptOrrefund.reference_no?? && receiptOrrefund.reference_no !=""]
			<td>Reference:${receiptOrrefund.reference_no!""}</td>
			[#else]
			<td></td>
			[/#if]
		</tr>
		<tr>
			<td>
				[#if type == "F"  && receiptOrrefund.total_receipts?? ]
					Net deposits against this bill: ${currencySymbol} ${receiptOrrefund.total_receipts}
				[/#if]
				[#if type == "R" && receiptOrrefund.total_receipts?? ]
					Net amount received against this bill: ${currencySymbol} ${receiptOrrefund.total_receipts}
				[/#if]
				[#if type == "S" && receiptOrrefund.total_sponsor_receipts?? ]
					Net amount received against this bill: ${currencySymbol} ${receiptOrrefund.total_sponsor_receipts}
				[/#if]
			</td>
			[#if receiptOrrefund.tds_amount > 0]
			<td>TDS Amount Received: ${receiptOrrefund.tds_amount}</td>
			[/#if]
		</tr>

		<tr>
			<td>
				[#if type == "F"]
					Received with thanks: ${netPayments} Only.
				[#else]
					Received with thanks: ${netPayments} Only.
				[/#if]
			</td>
		</tr>

		<tr>
			<td>
				[#if "${receiptOrrefund.paid_by!}" !=""]
					Paid By: ${receiptOrrefund.paid_by}
				[/#if]
			</td>
			<td>Signature</td>
		</tr>
		<tr>
			<td></td>
			<td>(${receiptOrrefund.created_by})</td>
		</tr>
</table>
</div>
[/#escape]
