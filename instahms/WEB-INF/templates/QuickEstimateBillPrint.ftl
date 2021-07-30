<!--
Available variables:
    estimateBill : Bean representing estimate bill
	charges: List of charge beans
	chargeGroups: List of charge groups
	chargeGroupMap: Map of charges organized on charge groups
	chargeHeads: List of charge heads
	chargeHeadMap: Map of charges organized on charge heads
	totalAmount: total bill amount
-->
<!--
	[#setting time_format="HH:mm"]
	[#setting date_format="dd-MM-yyyy"]
	[#setting datetime_format="dd-MM-yyyy"]
	[#assign empty={}]
-->
[#escape x as x?html]
<div align="center">
	<b><u> Bill Estimate </u></b>
</div>
<br/>
<div class="patientHeader" style="margin-bottom: 1em">
	<table width="100%" cellspacing="0" cellpadding="0">
		<tr>
			<td>Person Name:</td>
			<td>${estimateBill.person_name!}</td>
			<td>Estimate No:</td>
			<td>${estimateBill.estimate_no?string.number}</td>
		</tr>
		<tr>
			<td>Estimate Date:</td>
			<td>${estimateBill.estimate_date!}</td>
			<td>Visit Type:</td>
			<td>
			<!-- [#if estimateBill.visit_type = "i"] -->
				IN Patient
			<!-- [/#if] -->
			<!-- [#if estimateBill.visit_type = "o"] -->
				OUT Patient
			<!-- [/#if] -->
			</td>
		</tr>
		<tr>
			<td>Rate Plan:</td>
			<td>${estimateBill.org_name!}</td>
			<td>Bed Type:</td>
			<td>${estimateBill.bed_type!}</td>
		</tr>
		<tr>
			<td>Patient Category:</td>
			<td>${estimateBill.category_name!}</td>
			<td>Insurance Co.:</td>
			<td>${estimateBill.insurance_co_name!}</td>
		</tr>
		<tr>
			<td>Network/Plan Type:</td>
			<td>${estimateBill.plan_type_name!}</td>
			<td>Plan Name:</td>
			<td>${estimateBill.plan_name!}</td>
		</tr>
		<!-- [#if (estimateBill.remarks)?? && (estimateBill.remarks != "") ] -->
		<tr>
			<td>Remarks:</td>
			<td>${estimateBill.remarks!}</td>
		</tr>
		<!-- [/#if] -->
		<tr>
			<td>&nbsp;</td>
		</tr>
	</table>
</div>

<table width="100%" cellspacing="0" cellpadding="0">
	<tr class="border-above-below">
		<th style="width: 18%;" align="left">Head</th>
		<th style="width: 18%;" align="left">Description</th>
		<th style="width: 18%;" align="left">Remarks</th>
		<th style="width: 5%;" align="right">Rate</th>
		<th style="width: 5%;" align="right">Qty</th>
		<th style="width: 6%;" align="right">Discount</th>
		<th style="width: 10%;" align="right">Amt.</th>
		<th style="width: 8%;" align="right">Tax.</th>
		<th style="width: 8%;" align="right">Sponsor Amt.</th>
		<th style="width: 8%;" align="right">Sponsor Tax.</th>
		<th style="width: 8%;" align="right">Patient Amt.</th>
		<th style="width: 8%;" align="right">Patient Tax.</th>
	</tr>
	<!-- [#list chargeGroups as cg] -->
				<!-- [#assign i=0] -->
				<!-- [#list chargeGroupMap[cg] as charge] -->
					<!-- [#if charge.status != 'X' ] -->
						<!-- [#assign i= i+1]  -->
					<!-- [/#if] -->
				<!-- [/#list] -->
			<!-- [#if i > 0 ] -->

				<!-- [#list chargeGroupMap[cg] as charge] -->
					<!-- [#if charge.status != 'X' ] -->
						<tr>
							<td valign="top" style="padding-left: 3px">${charge.chargehead_name?html}</td>
							<td valign="top" style="padding-left: 3px">${(charge.act_description!)?html}</td>
							<td valign="top" style="padding-left: 3px">${(charge.remarks!)?html}</td>
							<td valign="top" style="padding-right: 3px" align="right">${charge.act_rate!}</td>
							<td valign="top" style="padding-right: 3px" align="right">${charge.act_quantity!}</td>
							<td valign="top" style="padding-right: 3px" align="right">${charge.discount!}</td>
							<td valign="top" style="padding-right: 3px" align="right">${charge.amount!}</td>
							<td valign="top" style="padding-right: 3px" align="right">${charge.tax_amt!}</td>
							<td valign="top" style="padding-right: 3px" align="right">${charge.sponsor_amt!}</td>
							<td valign="top" style="padding-right: 3px" align="right">${charge.sponsor_tax!}</td>
							<td valign="top" style="padding-right: 3px" align="right">${charge.patient_amt!}</td>
							<td valign="top" style="padding-right: 3px" align="right">${charge.patient_tax!}</td>
						</tr>
					<!-- [/#if] -->
				<!-- [/#list] -->
			<!-- [/#if] -->
	<!-- [/#list] -->
	<tr>
		<td>&nbsp;</td>
	</tr>
	<!-- [#assign totalAmount = 0] -->
	<!-- [#assign totalTax = 0] -->
	<!-- [#assign totalsponsorAmount = 0] -->
	<!-- [#assign totalsponsorTax = 0] -->
	<!-- [#assign totalpatientAmount = 0] -->
	<!-- [#assign totalpatientTax = 0] -->
	<!-- [#assign totalDiscountAmount = 0] -->
	<!-- [#assign totalNetAmount = 0] -->
	<!-- [#list charges as charge] -->
		<!-- [#if charge.status != 'X' ] -->
			<!-- [#assign totalAmount = totalAmount + charge.amount + charge.discount!0]  -->
			<!-- [#assign totalTax = totalTax + charge.tax_amt!0]  -->
			<!-- [#assign totalsponsorTax = totalsponsorTax + charge.sponsor_tax!0]  -->
			<!-- [#assign totalpatientTax = totalpatientTax + charge.patient_tax!0]  -->
			<!-- [#assign totalNetAmount = totalNetAmount + charge.amount]  -->
			<!-- [#assign totalDiscountAmount = totalDiscountAmount + charge.discount!0]  -->
			<!-- [#assign totalsponsorAmount = totalsponsorAmount + charge.sponsor_amt!0]  -->
		<!-- [/#if] -->
	<!-- [/#list] -->
	<!-- [#assign totalpatientAmount = totalNetAmount - totalsponsorAmount]  -->
	<tr>
		<td colspan="8"></td>
		<td colspan="3" class="border-above-below" align="left"><b>Estimated Amount:</b></td>
		<td class="border-above-below" align="left"><b>${totalAmount!}</b></td>
	</tr>
	<tr>
		<td colspan="8"></td>
		<td colspan="3" align="left"><b>Net Amount:</b></td>
		<td align="left"><b>${totalNetAmount!}</b></td>
	</tr>
	<tr>
		<td colspan="8"></td>
		<td colspan="3" class="border-above-below" align="left"><b>Net Tax:</b></td>
		<td class="border-above-below" align="left"><b>${totalTax!}</b></td>
	</tr>
	<tr>
		<td colspan="8"></td>
		<td colspan="3" align="left"><b>Discounts:</b></td>
		<td align="left"><b>${totalDiscountAmount!}</b></td>
	</tr>
	<tr>
		<td colspan="8"></td>
		<td colspan="3" class="border-above-below" align="left"><b>Sponsor Amount:</b></td>
		<td class="border-above-below" align="left"><b>${totalsponsorAmount!}</b></td>
	</tr>
	<tr>
		<td colspan="8"></td>
		<td colspan="3" align="left"><b>Sponsor Tax:</b></td>
		<td align="left"><b>${totalsponsorTax!}</b></td>
	</tr>
	<tr>
		<td colspan="8"></td>
		<td colspan="3" class="border-above-below" align="left"><b>Patient Amount:</b></td>
		<td class="border-above-below" align="left"><b>${totalpatientAmount!}</b></td>
	</tr>
	<tr>
		<td colspan="8"></td>
		<td colspan="3" class="border-above-below" align="left"><b>Patient Tax:</b></td>
		<td class="border-above-below" align="left"><b>${totalpatientTax!}</b></td>
	</tr>
</table>

<table width="100%" style="margin-top: 1em">
	<tr>
		<td>&nbsp;</td>
	</tr>
	<tr align="right" valign="bottom">
		<td>Signature</td>
	</tr>
	<tr align="right" valign="bottom">
		<td style="padding-top: 2em">(${estimateBill.username})</td>
	</tr>
</table>

[/#escape]
