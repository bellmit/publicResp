[#escape x as x?html]

<div align="center">
	<b>
		Patient Visit Bills Print Template
	</b>
</div>
<!--
	[#setting time_format="HH:mm"]
	[#setting date_format="dd-MM-yyyy"]
	[#setting datetime_format="dd-MM-yyyy"]

	[#assign empty={}]
	[#assign paymentTypeDisplay = {"R":"Receipt","S":"Sponsor","F":"Refund"}]
	[#assign receiptTypeDisplay = {"A":"Advance","S":"Settlement"}]
	[#assign claimRoundOff = 0]
	[#assign discountTotal = 0]
	[#assign totalReceipt = 0]
	[#assign allinsuranceDeduction = 0]
	[#assign alltotalSponsorReceipts = 0]
	[#assign allclaimRecdAmount = 0]
	[#assign totalPatientRoundOff=0]
	[#assign totalInsRoundOff=0]


-->
<div class="patientHeader" style="margin-bottom: 1em">
	<table width="100%" cellspacing="0" cellpadding="0">
		<tr>
			<td>Name:</td>
			<td>${patient.full_name!}</td>
		</tr>
		<tr>
			<td>Age/Gender:</td>
			<td>${patient.age!?string("#")} ${patient.agein!} ${patient.patient_gender!}</td>
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
		<!-- [#if (patient.tpa_name??) || (patient.refdoctorname??)] -->
			<tr>
				<td>Sponsor:</td>
				<td>${patient.tpa_name!}</td>
				<td>Referred By:</td>
				<td>${patient.refdoctorname!}</td>
			</tr>
			<!-- [/#if] -->
			<!-- [#if patient.tpa_id??] -->
			<tr>
				<!-- [#if patient.policy_holder_name??] -->
				<td>Policy Holder:</td>
				<td>${patient.policy_holder_name!}</td>
				<!-- [/#if] -->

				<!-- [#if patient.policy_no??] -->
				<td>Policy No:</td>
				<td>${patient.policy_no!}</td>
				<!-- [/#if] -->
			</tr>
			<tr>
				<!-- [#if patient.plan_name??] -->
					<td>Policy Name:</td>
					<td>${patient.plan_name!}</td>
				<!-- [/#if] -->
				<!-- [#if patient.insurance_id??] -->
					<td>Case No:</td>
					<td>${patient.insurance_id?string('#')}</td>
				<!-- [/#if] -->
			</tr>
			<tr>
			<!-- [#if patient.insurance_no??] -->
				<td>Insurance No:</td>
				<td>${patient.insurance_no!}</td>
			<!-- [/#if] -->
			</tr>
		<!-- [/#if] -->

		<!-- [#if patient.insurance_co_name?? ] -->
			<tr>
				<td>Insurance Comp:</td>
				<td>${patient.insurance_co_name!}</td>
			</tr>
		<!-- [/#if] -->
		<tr>
			<td></td>
		</tr>
	</table>
</div>

[#list bill as patBill]

<!--
[#assign billRoundOff = patientRoundOff[patBill.billNo] + insRoundOff[patBill.billNo] ]
[#assign totalPatientRoundOff = totalPatientRoundOff + patientRoundOff[patBill.billNo]]
[#assign totalInsRoundOff = totalInsRoundOff + insRoundOff[patBill.billNo]]

[#assign packageAmount = 0]
[#assign packageName = ""]
[#if charges[patBill.billNo]??]
[#list charges[patBill.billNo] as charge]
	[#if (charge.charge_excluded!) == 'N' && (charge.act_rate_plan_item_code!) != 'x' && (charge.status != 'X')]
		[#assign packageAmount = packageAmount + charge.amount]
	[/#if]
	[#if charge.charge_head == "MARPKG"]
		[#assign packageName = charge.act_description]
	[/#if]
[/#list]
[/#if]
[#if patBill.dyna_pkg_rate_plan_code??]
[#assign packageName = patBill.dyna_pkg_rate_plan_code +" "+ packageName]
[/#if]
[#assign billType=""]
[#if patBill.billType == 'P']
	[#assign billType="(Bill Now)"]
[#elseif patBill.billType == 'C']
	[#assign billType="(Bill Later)"]
[/#if]
-->

<table width="100%" cellspacing="0" cellpadding="0">
	<tr>
		<td class="formlabel"><b>Bill No:</b></td>
		<td class="forminfo" colspan="2"><b>${patBill.billNo}${billType}</b></td>
		<td class="formlabel" align="right"><b>Bill Date:</b></td>		<td class="forminfo"><b>${patBill.openDate}</b></td>
	</tr>
	<tr class="border-above-below">
		<th width="12%" align="left">Charges</th>
		<th width="7%"  align="left">Ord#</th>
		<th width="20%" align="left">Head</th>
		<th width="26%" align="left">Description</th>
		<th width="8%"  align="right">Rate</th>
		<th width="6%"  align="right">Qty</th>
		<th width="12%" align="right">Amount</th>
	</tr>
	<!-- [#if packageName != ""] -->
		<tr>
			<td colspan="3"><b>Packages</b></td>
		</tr>
		<tr>
			<td width="12%" align="left">${patient.reg_date}</td>
			<td width="7%"  align="left"></td>
			<td width="20%" align="left">Package Charges</td>
			<td width="26%" align="left">${packageName}</td>
			<td width="8%"  align="right">${packageAmount}</td>
			<td width="6%"  align="right">1</td>
			<td width="12%" align="right">${packageAmount}</td>

		</tr>
		<tr><td colspan="7">&nbsp;</td></tr>
		<tr>

			<td colspan="6" style="white-space:nowrap"  align="right"><b>Sub Total:</b></td>
			<td align="right"><b>${packageAmount}</b></td>
		</tr>
	<!-- [/#if] -->
<!-- [#list chargeGroups[patBill.billNo] as cg] -->
	<!-- [#if (!cg?starts_with("Discounts")) || (patientRoundOff[patBill.billNo] != 0 || insRoundOff[patBill.billNo] != 0) ] -->
		<!--
			[#assign i=0]
			[#list chargeGroupMap[patBill.billNo][cg] as charge]
				[#if ((packageName == '' && (charge.charge_excluded!) == 'N') ||
					 (packageName != '' && (charge.charge_excluded!) == 'Y')) && (charge.status != 'X') ]
					[#assign i= i+1]
				[/#if]
			[/#list]
		-->
		<!-- [#if i > 0 ] -->
			<tr>
				<td colspan="3"><b>${cg}</b></td>
			</tr>
			<!-- [#assign subTotal=0]  -->
			<!-- [#list chargeGroupMap[patBill.billNo][cg] as charge] -->
				<!-- [#if charge.charge_head != "BIDIS" &&
							((packageName == '' && (charge.charge_excluded!) == 'N') ||
							(packageName != '' && (charge.charge_excluded!) == 'Y')) && (charge.status != 'X')] -->
					<tr>
						<td width="12%" align="left">${charge.posted_date!}</td>
						<td width="7%" align="left">
							<!-- [#if (charge.order_number)??] -->
								${charge.order_number?string("#")}
							<!-- [/#if] -->
						</td>
						<td width="20%" align="left">${charge.chargehead_name}</td>
						<td width="26%" align="left">${charge.act_rate_plan_item_code!} ${charge.act_description!}
							<!-- [#if (charge.act_remarks)?? && (charge.act_remarks != "") ] -->
								<br/>(${charge.act_remarks})
							<!-- [/#if] -->
						</td>
						<td width="8%" align="right">${charge.act_rate!}</td>
						<td width="6%" align="right">${charge.act_quantity?string("#0.00")!}</td>
						<td width="12%" align="right">${charge.amount + charge.discount}</td>
						<!-- [#assign subTotal=subTotal+(charge.amount + charge.discount)] -->
					</tr>
				<!-- [/#if] -->
			<!-- [/#list] -->
			<tr><td colspan="7">&nbsp;</td></tr>
			<tr>
				<td colspan="6" style="white-space:nowrap"  align="right"><b>Sub Total:</b></td>
				<td align="right"><b>${subTotal}</b></td>
			</tr>
		<!-- [/#if] -->
	<!-- [/#if] -->
<!-- [/#list] -->
<tr><td colspan="7">&nbsp;</td></tr>
<tr>
	<td colspan="6" style="white-space:nowrap" class="border-above-below" align="right"><b>Bill Amount:</b></td>
	<td class="border-above-below" align="right"><b>${totalAmount[patBill.billNo] + billRoundOff + totalDiscount[patBill.billNo]}</b></td>
</tr>
</table>
[#if hasDiscounts[patBill.billNo]]
	<table width="100%" cellpadding="0" cellspacing="0" style="margin-top: 1em; border-collapse: collapse">
		<tr class="border-above-below">
			<th style="width: 15%;" align="left">Discounts</th>
			<th style="width: 25%;" align="left">Head</th>
			<th style="width: 25%;" align="left">Description</th>
			<th style="width: 9%;" align="right">Amount</th>
		</tr>
		<!--[#if charges[patBill.billNo]??] -->
			<!-- [#list charges[patBill.billNo] as charge] -->
				 <!--[#if (charge.discount != 0) &&
						((packageName == '' && (charge.charge_excluded!) == 'N') ||
						 (packageName != '' && (charge.charge_excluded!) == 'Y'))] -->
					 <tr>
						<td valign="top" style="padding-left: 6px">${charge.posted_date}</td>
						<td valign="top">${charge.chargehead_name}</td>
						<td valign="top">${charge.act_description!}
							<!-- [#if (charge.act_remarks)?? && (charge.act_remarks != "") ] -->
								<br/>(${charge.act_remarks})
							<!-- [/#if] -->
						</td>
						<td valign="top" align="right">${charge.discount!}</td>
						<!-- [#assign discountTotal=discountTotal+charge.discount] -->
					</tr>
				<!-- [/#if]-->
			<!-- [/#list] -->
		<!-- [/#if] -->
		<tr>
			<td colspan="2"></td>
			<td class="border-above-below" align="right"><b>Total Discount</b></td>
			<td class="border-above-below" align="right"><b>${discountTotal}</b></td>
		</tr>
	</table>
[/#if]

[#if receipts?has_content]
<table width="100%" cellspacing="0" cellpadding="0" style="margin-top: 1em; border-collapse: collapse;">
	<tbody>
		<tr class="border-above-below">
			<th width="15%">Payments</th>
			<th width="15%">Receipt No</th>
			<th width="15%">Mode</th>
			<th width="10%">Card Type</th>
			<th width="20%">Bank</th>
			<th width="15%">Reference No</th>
			<th width="15%" align="right">Amount</th>
		</tr>
		<!-- [#list ["R","S","F"] as type] -->
			<!-- [#if receiptTypeMap[patBill.billNo][type]?has_content ] -->

				<!--[#list receiptTypeMap[patBill.billNo][type] as receipt]-->
					<!--[#if type == "R"] -->
					[#assign totalReceipt=totalReceipt+receipt.amount]
					<tr>
						<td colspan="3"><b>${receiptTypeDisplay[receipt.recpt_type]}</b></td>
					</tr>
					<!--[/#if]-->
					<!--[#if type == "F"] -->
					[#assign totalReceipt=totalReceipt+receipt.amount]
					<tr>
						<td colspan="3"><b>${paymentTypeDisplay[type]}</b></td>
					</tr>
					<!--[/#if]-->
					<!--[#if type == "S"] -->
					<tr>
						<td colspan="3"><b>${paymentTypeDisplay[type]} - ${receiptTypeDisplay[receipt.recpt_type]}</b></td>
					</tr>
					<!--[/#if]-->

					<tr>
						<td width="15%">${receipt.display_date}</td>
						<td width="15%">${receipt.receipt_no}</td>
						<td width="15%">${receipt.payment_mode_name}</td>
						<td width="10%">${receipt.card_type!}</td>
						<td width="15%">${receipt.bank_name!}</td>
						<td width="15%">${receipt.reference_no!}</td>
						<td width="15%" align="right">${receipt.amount}</td>
					</tr>
				<!--[/#list]-->
			<!-- [/#if] -->
		<!--[/#list]-->
		<tr><td colspan="7">&nbsp;</td></tr>
		<tr>
			<td colspan="5"></td>
			<td style="white-space:nowrap" class="border-above-below" align="right"><b>Net Payments:</b></td>
			<td class="border-above-below" align="right"><b>${netPayments[patBill.billNo]}</b></td>
		</tr>
	</tbody>
</table>
[/#if]

<table style="margin-top: 1em" width="100%" cellspacing="0" cellpadding="0">
	<tr class="border-above-below">
		<th colspan="3">Bill Expenses</th>
	</tr>
	<tr>
		<td width="60%"></td>
		<td align="right">Total Bill Amount</td>
		<td align="right">${totalAmount[patBill.billNo] + billRoundOff + discountTotal}</td>
	</tr>
	<tr>
		<td width="60%"></td>
		<td align="right">Less Discount</td>
		<td align="right">${discountTotal}</td>
	</tr>
	[#if !(patient.tpa_name??) || patient.tpa_name == '']
		<tr>
			<td width="60%"></td>
			<td align="right">Less Net Payments</td>
			<td align="right">${patBill.totalReceipts}</td>
		</tr>
		[#if billDeposits[patBill.billNo] != 0]
			<tr>
				<td width="60%"></td>
				<td align="right">Less Deposits Set Off</td>
				<td align="right">${billDeposits[patBill.billNo]}</td>
			</tr>
		[/#if]
		<tr style="font-weight: bold">
			<td width="60%"></td>
			<td class="border-above-below"  align="right">Balance Due</td>
			<td class="border-above-below" align="right">${totalAmount[patBill.billNo]-billDeposits[patBill.billNo]- patBill.totalReceipts + billRoundOff}</td>
		</tr>
	[/#if]
	<!--[#if patient.tpa_name?? && (patient.tpa_name != '')]-->
		[#assign allinsuranceDeduction = allinsuranceDeduction+patBill.insuranceDeduction]
		[#assign alltotalSponsorReceipts = alltotalSponsorReceipts+patBill.totalPrimarySponsorReceipts + patBill.totalSecondarySponsorReceipts]
		[#if (patBill.sponsorBillNo!) !='']
			[#assign allclaimRecdAmount = allclaimRecdAmount+patBill.claimRecdAmount]
		[#else]
			[#assign allclaimRecdAmount = allclaimRecdAmount+patBill.totalPrimarySponsorReceipts + patBill.totalSecondarySponsorReceipts]
		[/#if]
		<tr style="font-weight: bold">
			<td width="60%"></td>
			<td class="border-above-below"  align="right">Net Amt.</td>
			<td class="border-above-below" align="right">${totalAmount[patBill.billNo] + billRoundOff}</td>
		</tr>
		[#if patBill.is_tpa ]
			<tr>
				<td width="60%"></td>
				<td align="right">Deductions (Patient Amount)</td>
				<td align="right">${totalAmount[patBill.billNo] + billRoundOff - (patBill.totalClaim  - patBill.insuranceDeduction) }</td>
			</tr>
			[#if billDeposits[patBill.billNo] != 0]
				<tr>
					<td width="60%"></td>
					<td align="right">Less Deposits Set Off </td>
					<td align="right">${billDeposits[patBill.billNo]}</td>
				</tr>
			[/#if]
		[/#if]
		<tr>
				<td width="60%"></td>
				<td align="right">Less Patient Payments </td>
				<td align="right">${patBill.totalReceipts}</td>
		</tr>
		<tr style="font-weight: bold">
			<td width="60%"></td>
			<td class="border-above-below" align="right">Patient Due </td>
			<td class="border-above-below" align="right">${totalAmount[patBill.billNo] + patientRoundOff[patBill.billNo] - patBill.totalReceipts - billDeposits[patBill.billNo] - (totalClaimAmount[patBill.billNo] -patBill.insuranceDeduction)}</td>
		</tr>
	<!--[/#if]-->

	<!--[#if patient.tpa_name?? && (patient.tpa_name != '')]-->
	<tr>
		<td width="60%"></td>
		<td align="right">Sponsor Amount </td>
		<td align="right">${totalClaimAmount[patBill.billNo] + insRoundOff[patBill.billNo] - patBill.insuranceDeduction}</td>
	</tr>
	<tr>
		<td width="60%"></td>
		<td align="right">Less Sponsor Pay </td>
		<td align="right">
			${patBill.totalPrimarySponsorReceipts + patBill.totalSecondarySponsorReceipts}
		</td>
	</tr>
	<tr style="font-weight: bold">
		<td width="60%"></td>
		<td class="border-above-below" align="right">Sponsor Due </td>
		<td class="border-above-below" align="right">
			${  totalClaimAmount[patBill.billNo] + insRoundOff[patBill.billNo] - patBill.insuranceDeduction  - patBill.totalPrimarySponsorReceipts - patBill.totalSecondarySponsorReceipts}
		</td>
	</tr>
	<!--[/#if]-->
</table>
[/#list]
<table style="margin-top: 1em" width="100%" cellspacing="0" cellpadding="0">
	<tr class="border-above-below">
		<th colspan="3">Total Expenses</th>
	</tr>
	<tr>
		<td width="60%"></td>
		<td align="right">Total Bill Amount</td>
		<td align="right">${alltotalAmount + allroundOff + discountTotal}</td>
	</tr>
	<tr>
		<td width="60%"></td>
		<td align="right">Less Discount</td>
		<td align="right">${discountTotal}</td>
	</tr>
	[#if !(patient.tpa_name??) || patient.tpa_name == '']
		<tr>
			<td width="60%"></td>
			<td align="right">Less Net Payments</td>
			<td align="right">${totalReceipt}</td>
		</tr>
		[#if allbillDeposits != 0]
			<tr>
				<td width="60%"></td>
				<td align="right">Less Deposits Set Off</td>
				<td align="right">${allbillDeposits}</td>
			</tr>
		[/#if]
		<tr style="font-weight: bold">
			<td width="60%"></td>
			<td class="border-above-below"  align="right">Balance Due</td>
			<td class="border-above-below" align="right">${alltotalAmount + allroundOff - allbillDeposits-totalReceipt-alltotalSponsorReceipts}</td>
		</tr>
	[/#if]
	<!--[#if patient.tpa_name?? && (patient.tpa_name != '')]-->
		<tr style="font-weight: bold">
			<td width="60%"></td>
			<td class="border-above-below"  align="right">Net Amt.</td>
			<td class="border-above-below" align="right">${alltotalAmount + allroundOff}</td>
		</tr>
		<tr>
			<td width="60%"></td>
			<td align="right">Deductions (Patient Amount)</td>
			<td align="right">${alltotalAmount + allroundOff - (alltotalClaimAmount  - allinsuranceDeduction) }</td>
		</tr>
		[#if allbillDeposits != 0]
			<tr>
				<td width="60%"></td>
				<td align="right">Less Deposits Set Off </td>
				<td align="right">${allbillDeposits}</td>
			</tr>
		[/#if]
		<tr>
				<td width="60%"></td>
				<td align="right">Less Patient Payments </td>
				<td align="right">${totalReceipt}</td>
		</tr>
		<tr style="font-weight: bold">
			<td width="60%"></td>
			<td class="border-above-below" align="right">Patient Due </td>
			<td class="border-above-below" align="right">${alltotalAmount + totalPatientRoundOff - totalReceipt - allbillDeposits - (alltotalClaimAmount -allinsuranceDeduction)}</td>
		</tr>
	<!--[/#if]-->

	<!--[#if patient.tpa_name?? && (patient.tpa_name != '')]-->
		<tr>
			<td width="60%"></td>
			<td align="right">Sponsor Amount </td>
			<td align="right">${alltotalClaimAmount + totalInsRoundOff - allinsuranceDeduction}</td>
		</tr>
		<tr>
			<td width="60%"></td>
			<td align="right">Less Sponsor Pay </td>
			<td align="right">
				${alltotalSponsorReceipts}
			</td>
		</tr>
		<tr style="font-weight: bold">
			<td width="60%"></td>
			<td class="border-above-below" align="right">Sponsor Due </td>
			<td class="border-above-below" align="right">
				${ alltotalClaimAmount + totalInsRoundOff - allinsuranceDeduction  - alltotalSponsorReceipts}
			</td>
		</tr>
	<!--[/#if]-->
</table>

[/#escape]
