<!--
Available variables:
  bill : Bean representing bill
	patient: Map representing patient
	charges: List of charge beans
	chargeGroups: List of charge groups
	chargeGroupMap: Map of charges organized on charge groups
	billChargeTax: List of Bill charge tax beans
	billChargeTaxGroups: List of Bill charge tax groups
	billChargeTaxGroupMap: Map of Bill charge tax organized on Bill charge tax groups
	chargeHeads: List of charge heads
	chargeHeadMap: Map of charges organized on charge heads
	serviceGroups: List of service groups
	serviceGroupMap: Map of charges organized on service groups
	serviceSubGroup: List of Sub Groups
	serviceSubGroupMap: Map of charges organized on service sub groups
	receipts: list of all receipts/refunds
	receiptTypeMap: Map of receipts organized on receipt Type (A,S,F)
	totalAmount: total bill amount
	totalDiscount: total discount amount
	hasDiscounts: whether there are any discounts (can be true even if totalDiscount = 0)
	totalClaimAmount: total insurance claim amount
	netPayments: sum of all receipts and refunds, net payments
	netPaymentsWords: Net payments in words: Rupees xxx and Paise xxx
	packageDetailsMap: Package component details if bill has package
	saleItemsMap: Map of Medicine sales items organized on sale ids.
	saledIDs: List of Sale Ids.
	visitTestsResults: List of Test Results related to this bill.
	primary_tax_amount: primary tax amount
	secondary_tax_amount: secondary tax amount
	primary_sponsor_amount: primary sponsor amount
	secondary_sponsor_amount: secondary sponsor amount
	Use conducted_in_reportformat token to find test type,i.e, Values/Report.
	If Report type, Resultlabel is test_name and resultvalue is patient_report_file
-->

<!--Please Note : For bug 18663 if package detailed print is needed in the bill print, customize the print using the following:
	 [if charge.charge_head == 'PKGPKG' ]
		 [assign pkg_id = charge.act_description_id]
			[assign pkgDetails = packageDetailsMap[pkg_id]]
			 <tr>
				 [list pkgDetails as package]
					 <td>package.package_name</td>
					 <td>package.activity_type</td> activity_description
					 <td>package.activity_remarks</td>
					 <td>package.activity_description</td> ...
				[/list]
			</tr>
	 [else]
			.....
	 [/if]
-->

<!--
	[#setting time_format="HH:mm"]
	[#setting date_format="dd-MM-yyyy"]
	[#setting datetime_format="dd-MM-yyyy"]

	[#assign empty={}]
	[#assign paymentTypeDisplay = {"R":"Receipt","S":"Sponsor","F":"Refund"}]
	[#assign receiptTypeDisplay = {"A":"Advance","S":"Settlement"}]
	[#assign claimRoundOff = 0]
	[#assign discountTotal = 0]
-->

[#escape x as x?html]
<div align="center">
	<b>
		[#if bill.bill_type=="P" && bill.total_amount >0]Bill Cum Receipt
		[#else]
			[#if bill.total_amount <0]Credit Note
			[#elseif bill.status == "C" ]Final Bill - Copy
			[#elseif bill.status == "A" ]Provisional Bill
			[#else]Final Bill
			[/#if]
		[/#if]
	</b>
</div>

<div class="patientHeader" style="margin-bottom: 1em">
	<table width="100%" cellspacing="0" cellpadding="0">
		<tr>
			<td>Name:</td>
			<td>${patient.full_name!}</td>
			<td>Bill No:</td>
			<td>${bill.bill_no}</td>
		</tr>
		<tr>
			<td>Age/Gender:</td>
			<td>${patient.age?string('#')}[#if patient.visit_type != "t"]${patient.agein!} ${patient.patient_gender!}[#else]${patient.age_unit!} ${patient.gender!}[/#if]</td>
			<td>Bill Date:</td>
			<td>${bill.finalized_date!}</td>
		</tr>
		<tr>
			<td>Address:</td>
			<td>${patient.patient_address!}</td>
			<td>MR No:</td>
			<td>${patient.mr_no!}</td>
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
			<td>${patient.reg_date!} ${patient.reg_time!}</td>
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
			<td>${patient.org_name!}</td>
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
			<!-- [#if bill.procedure_no != 0 ] -->
			<tr>
				<td>Procedure:</td>
				<td>${bill.procedure_name!}</td>
				<td>Code:</td>
				<td>${bill.procedure_code!}</td>
			</tr>
			<tr>
				<td>Limit(Rs):</td>
				<td>${bill.procedure_limit}</td>
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

<!--
[#assign packageAmount = 0]
[#assign packageName = ""]
[#list charges as charge]
	[#if (charge.act_rate_plan_item_code!) != 'x' && (charge.status != 'X')]
		[#assign packageAmount = packageAmount + charge.amount_included]
	[/#if]
	[#if charge.charge_head == "MARPKG"]
		[#assign packageName = charge.act_description]
	[/#if]
[/#list]
[#if bill.dyna_pkg_rate_plan_code??]
[#assign packageName = bill.dyna_pkg_rate_plan_code +" "+ packageName]
[/#if]
-->

[#assign totalAmount = totalAmount + roundOff]

<table width="100%" cellspacing="0" cellpadding="0">
	<tr class="border-above-below">
		<th width="10%" align="left">Charges</th>
		<th width="6%"  align="left">Ord#</th>
		<th width="11%" align="left">Head</th>
		<th width="19%" align="left">Description</th>
		<th width="7%"  align="right">Rate</th>
		<th width="6%"  align="right">Qty</th>
		<th width="9%" align="right">Amount</th>
		<th width="7%" align="left">Tax Rate</th>
		<th width="6%" align="left">Tax Amount</th>
  		<th width="8%" align="left">Tax Groups</th>
  		<th width="8%" align="left">Tax Sub Groups</th>
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

	<!--
		[#if (multivisitPackage.package_name)?has_content] -->
			<tr><td colspan="3"><b>Package : ${multivisitPackage.package_name}</b></td></tr>
			<tr><td colspan="3"></td></tr>
	<!--
		[/#if] -->

	<!-- [#list chargeGroups as cg] -->
		<!-- [#if !cg?starts_with("Discounts") || roundOff != 0 ] -->
			<!--
				[#assign i=0]
				[#list chargeGroupMap[cg] as charge]
					[#if ((packageName == '' && (charge.charge_excluded!) == 'N') ||
						 (packageName != '' && (charge.charge_excluded!) != 'N')) && (charge.status != 'X') ]
						[#assign i= i+1]
					[/#if]
				[/#list]
			-->
			<!-- [#if i > 0 ] -->
				<tr>
					<td colspan="3"><b>${cg}</b></td>
				</tr>
				<!-- [#assign subTotal=0]  -->
				<!-- [#list chargeGroupMap[cg] as charge] -->
					<!-- [#if charge.charge_head != "BIDIS" &&
						 ((packageName == '' && (charge.charge_excluded!) == 'N') ||
						 (packageName != '' && (charge.charge_excluded!) != 'N')) && (charge.status != 'X')] -->
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
								<!-- [#if ( (bill.visit_type == 't') && (sampleDetails?? ) ) ] -->
									<br/>
									<b>Sample No:</b> ${sampleDetails[charge.charge_id].sample_no!}
									<br/>
									<b>Original Sample No:</b> ${sampleDetails[charge.charge_id].orig_sample_no!}
								<!-- [/#if] -->
							</td>
							<td width="8%" align="right">${charge.act_rate!}</td>
							<td width="6%" align="right">${charge.act_quantity?string("#0.00")!}</td>
							<td width="12%" align="right">${charge.amount + charge.discount}</td>
							<!-- [#if billChargeTaxGroupMap[charge.charge_id]?has_content ] -->
						<!-- [#list billChargeTaxGroupMap[charge.charge_id] as taxCharge]-->
							<td width="12%" align="right">${taxCharge.tax_rate}</td>
							<td width="12%" align="right">${taxCharge.tax_amount}</td>
							<td width="12%" align="right">${taxCharge.item_group_name}</td>
							<td width="12%" align="right">${taxCharge.item_subgroup_name}</td>
						<!-- [/#list] -->
						<!-- [/#if] -->
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
		<td class="border-above-below" align="right"><b>${totalAmount + totalDiscount}</b></td>
	</tr>
</table>

[#if hasDiscounts]
	<table width="100%" cellpadding="0" cellspacing="0" style="margin-top: 1em; border-collapse: collapse">
		<tr class="border-above-below">
			<th style="width: 15%;" align="left">Discounts</th>
			<th style="width: 25%;" align="left">Head</th>
			<th style="width: 25%;" align="left">Description</th>
			<th style="width: 9%;" align="right">Amount</th>
		</tr>
		<!-- [#list charges as charge] -->
			 <!--[#if (charge.discount != 0) &&
						((packageName == '' && (charge.charge_excluded!) == 'N') ||
						 (packageName != '' && (charge.charge_excluded!) != 'N')) ] -->
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
			<!-- [#if receiptTypeMap[type]?has_content ] -->

				<!--[#list receiptTypeMap[type] as receipt]-->
					<!--[#if type == "R"] -->
					<tr>
						<td colspan="3"><b>${receiptTypeDisplay[receipt.recpt_type]}</b></td>
					</tr>
					<!--[/#if]-->
					<!--[#if type == "F"] -->
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
			<td class="border-above-below" align="right"><b>${netPayments}</b></td>
		</tr>
	</tbody>
</table>
[/#if]

[#if billDeposits >0]
	<p style="margin-top: 1em"><b>Amount from Deposits: ${billDeposits} </b></p>
[/#if]
[#if bill.points_redeemed >0]
	<p style="margin-top: 1em"><b>Amount from Points Redeemed: ${bill.points_redeemed_amt} </b></p>
[/#if]
[#if bill.bill_type !="C"]
	<p style="margin-top: 1em"><b>Received with thanks: </b>${netPaymentsWords} only</p>
[/#if]

[#if bill.bill_type == "C"]
	<table style="margin-top: 1em" width="100%" cellspacing="0" cellpadding="0">
		<tr class="border-above-below">
			<th colspan="3">Bill Summary</th>
		</tr>
		<tr>
			<td width="60%"></td>
			<td align="right">Total Bill Amount</td>
			<td align="right">${totalAmount + discountTotal}</td>
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
				<td align="right">${bill.total_receipts}</td>
			</tr>
			[#if billDeposits != 0]
				<tr>
					<td width="60%"></td>
					<td align="right">Less Deposits Set Off</td>
					<td align="right">${billDeposits}</td>
				</tr>
			[/#if]
			<tr style="font-weight: bold">
				<td width="60%"></td>
				<td class="border-above-below"  align="right">Balance Due</td>
				<td class="border-above-below" align="right">${totalAmount-billDeposits- bill.total_receipts}</td>
			</tr>
		[/#if]
		<tr style="font-weight: bold">
			<td width="60%"></td>
			<td class="border-above-below"  align="right">Net Amt.</td>
			<td class="border-above-below" align="right">${totalAmount}</td>
		</tr>
		<!--[#if patient.primary_sponsor_id?? && (patient.primary_sponsor_id != '') && patient.tpa_name?? && (patient.tpa_name != '')]-->
		<tr>
			<td width="60%"></td>
			<td align="right">Pri. Sponsor Amount </td>
			<td align="right">${primary_sponsor_amount+primary_tax_amount}</td>
		</tr>
		<tr>
			<td width="60%"></td>
			<td align="right">Pri. Sponsor Pay </td>
			<td align="right">
				[#if (bill.sponsor_bill_no!) !='']
					${bill.claim_recd_amount}
				[#else]
					${bill.primary_total_sponsor_receipts}
				[/#if]
			</td>
		</tr>
		<tr style="font-weight: bold">
			<td width="60%"></td>
			<td class="border-above-below" align="right">Pri. Sponsor Due </td>
			<td class="border-above-below" align="right">
				[#if (bill.sponsor_bill_no!) != '']
					${primary_sponsor_amount+primary_tax_amount  - bill.claim_recd_amount}
				[#else]
					${primary_sponsor_amount+primary_tax_amount - bill.primary_total_sponsor_receipts}
				[/#if]
			</td>
		</tr>
		<!--[/#if]-->
		<!--[#if patient.secondary_sponsor_id?? && (patient.secondary_sponsor_id != '') && patient.sec_tpa_name?? && (patient.sec_tpa_name != '')]-->
		<tr>
			<td width="60%"></td>
			<td align="right">Sec. Sponsor Amount </td>
			<td align="right">${secondary_sponsor_amount+secondary_tax_amount}</td>
		</tr>
		<tr>
			<td width="60%"></td>
			<td align="right">Sec. Sponsor Pay </td>
			<td align="right">
				[#if (bill.sponsor_bill_no!) !='']
					${bill.claim_recd_amount}
				[#else]
					${bill.secondary_total_sponsor_receipts}
				[/#if]
			</td>
		</tr>
		<tr style="font-weight: bold">
			<td width="60%"></td>
			<td class="border-above-below" align="right">Sec. Sponsor Due </td>
			<td class="border-above-below" align="right">
				[#if (bill.sponsor_bill_no!) != '']
					${secondary_sponsor_amount+secondary_tax_amount - bill.claim_recd_amount}
				[#else]
					${secondary_sponsor_amount+secondary_tax_amount - bill.secondary_total_sponsor_receipts}
				[/#if]
			</td>
		</tr>
		<!--[/#if]-->
		<!--[#if patient.tpa_name?? && (patient.tpa_name != '')]-->

			<tr>
				<td width="60%"></td>
				<td align="right">Deductions (Patient Amount)</td>
				<td align="right">${ bill.insurance_deduction }</td>
			</tr>
			[#if billDeposits != 0]
				<tr>
					<td width="60%"></td>
					<td align="right">Less Deposits Set Off </td>
					<td align="right">${billDeposits}</td>
				</tr>
			[/#if]
			[#if bill.points_redeemed > 0]
				<tr>
					<td width="60%"></td>
					<td align="right">Less Reward Points Amt. </td>
					<td align="right">${bill.points_redeemed_amt!0}</td>
				</tr>
			[/#if]
			<tr>
					<td width="60%"></td>
					<td align="right">Less Patient Payments </td>
					<td align="right">${bill.total_receipts}</td>
			</tr>
			<tr style="font-weight: bold">
				<td width="60%"></td>
				<td class="border-above-below" align="right">Patient Due </td>
				<td class="border-above-below" align="right">
					${totalAmount - bill.total_receipts - billDeposits - bill.points_redeemed_amt - primary_sponsor_amount-primary_tax_amount - secondary_sponsor_amount-secondary_tax_amount}
				</td>
			</tr>
		<!--[/#if]-->
	</table>
[/#if]

<table width="100%" style="margin-top: 1em">
	<tr align="right" valign="bottom">
		<td>Signature</td>
	</tr>
	<!--[#if genPrefs.user_name_in_bill_print??
       && (genPrefs.user_name_in_bill_print == 'Y')]-->
	   <!--[#if bill.temp_username?? && (bill.temp_username != '')]-->
	      <tr align="right" valign="bottom">
			<td style="padding-top: 2em">(${bill.temp_username})</td>
		</tr>
	    <!--[#else]-->
	      <tr align="right" valign="bottom">
			<td style="padding-top: 2em">(${bill.username})</td>
		</tr>
	   <!--[/#if]-->
	<!--[/#if]-->
</table>

[/#escape]
