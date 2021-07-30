<!--
Available variables:
  bill: Bean representing bill
	patient: Map representing patient
	charges: List of charge beans
	chargeGroups: List of charge groups
	chargeGroupMap: Map of charges organized on charge groups
	chargeHeads: List of charge heads
	chargeHeadMap: Map of charges organized on charge heads
	totalAmount:  total bill amount
	totalClaimAmount:  total insurance claim amount
	totalTaxOnClaim:  total tax on taxable insurance claim amount
	totalDiscount: total discount amount
	hasDiscounts: whether there are any discounts (can be true even if totalDiscount = 0)
-->
<!--
	[#setting time_format="HH:mm"]
	[#setting date_format="dd-MM-yyyy"]
	[#setting datetime_format="dd-MM-yyyy"]

-->
<div align="center">
	<b>Patient Bill Details</b>
</div>
<!-- [#if patient??] -->
<div class="patientHeader" style="margin-bottom: 1em">
	<table width="100%" cellspacing="0" cellpadding="0">
		<tr>
			<td>Name:</td>
			<td>${patient.salutation!?html} ${patient.patient_name!?html} ${patient.last_name!?html}</td>
			<td>Bill No:</td>
			<td><!-- [#if bill??] -->${bill.bill_no!?html}<!-- [/#if] --></td>
		</tr>
		<tr>
			<td>Age/Gender:</td>
			<td>${patient.age!?string("#")} ${patient.agein!} ${patient.patient_gender!}</td>
			<td>Bill Date:</td>
			<td><!-- [#if bill??] -->${bill.finalized_date!}<!-- [/#if] --></td>
		</tr>
		<tr>
			<td>Address:</td>
			<td>${patient.patient_address!}</td>
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
		<tr>
			<td>Insurance No:</td>
			<td>${case.insurance_no!}</td>
			<td>Policy No:</td>
			<td>${case.policy_no!}</td>
		</tr>
		<!-- [#if bill??] -->
		<!-- [#if bill.procedure_no?? && bill.procedure_no != 0 ] -->
		<tr>
			<td>Procedure:</td>
			<td>${bill.procedure_name!}</td>
			<td>Code:</td>
			<td>${bill.procedure_code!}</td>
		</tr>
		<tr>
			<td>Limit(Rs):</td>
			<td>${bill.procedure_limit!0}</td>
		</tr>
		<!-- [/#if] -->
		<tr>
			<td></td>
		</tr>
		<!-- [/#if] -->
	</table>
</div>
<!-- [/#if] -->

<!-- [#if bill??] -->
<table width="100%" cellspacing="0" cellpadding="0">
	<tr class="border-above-below">
		<th style="width: 20%;" align="left">Charges</th>
		<th style="width: 30%;" align="left">Head</th>
		<th style="width: 40%;" align="left">Description</th>
		<th style="width: 10%;" align="right">Amount</th>
	</tr>
	<!-- [#list chargeGroups as cg] -->
		<!-- [#if cg != "Discounts" ] -->
			<tr>
				<td colspan="3"><b>${cg?html}</b></td>
			</tr>
			<!-- [#assign subTotal=0] -->
			<!-- [#list chargeGroupMap[cg] as charge] -->
				<!-- [#if (charge.status != 'X') && (charge.insurance_claim_amount > 0) ] -->
					<tr>
						<td valign="top" style="padding-left: 6px">${charge.posted_date!}</td>
						<td valign="top" style="padding-left: 3px">${charge.chargehead_name?html}</td>
						<td valign="top" style="padding-left: 3px">${(charge.act_description!)?html}
							<!-- [#if (charge.act_remarks)?? && (charge.act_remarks != "") ] -->
								<br/>(${charge.act_remarks})
							<!-- [/#if] -->
						</td>
						<td valign="top" align="right">${charge.insurance_claim_amount!0}</td>
						<!-- [#assign subTotal=subTotal!0+charge.insurance_claim_amount!0] -->
					</tr>
				<!-- [/#if] -->
			<!-- [/#list] -->
			<tr>
				<td colspan="3" align="right"><b>Sub Total</b></td>
				<td colspan="1" align="right"><b>${subTotal!0}</b></td>
			</tr>
		<!-- [/#if] -->
	<!-- [/#list] -->
	<!-- [#if bill.insurance_deduction?? && bill.insurance_deduction > 0] -->
	<tr>
		<td colspan="1"></td>
		<td colspan="2" class="border-above-below" align="right"><b>Bill Deduction</b></td>
		<td class="border-above-below" align="right"><b>${bill.insurance_deduction}</b></td>
	</tr>
	<!-- [/#if] -->
	<tr>
		<td colspan="1"></td>
		<td colspan="2" class="border-above-below" align="right"><b>Total Bill Amount</b></td>
		<td class="border-above-below" align="right"><b>${totalClaimAmount!0 - bill.insurance_deduction!0}</b></td>
	</tr>
	<tr>
		<td colspan="1"></td>
		<td colspan="2" class="border-above-below" align="right"><b>Service Tax on Taxable Claim Amount</b></td>
		<td class="border-above-below" align="right"><b>${totalTaxOnClaim!0}</b></td>
	</tr>

</table>

<table width="100%" style="margin-top: 1em">
	<tr align="right" valign="bottom">
		<td>Signature</td>
	</tr>
	<tr align="right" valign="bottom">
		<td style="padding-top: 2em">(${bill.temp_username!})</td>
	</tr>
</table>
<!-- [/#if] -->


