

<!--
		[#setting time_format="HH:mm"]
		[#setting date_format="dd-MM-yyyy"]
		[#setting datetime_format="dd-MM-yyyy HH:mm"]
	-->
[#escape x as x?html]

		[#if patientType == "r"]
			<table width="100%" cellspacing="0" cellpadding="0" border="0">
				<tr>
					<td valign="top">
						<table>
							<tr>
								<td class="label">Patient: </td>
								<td class="info">${customer.customer_name}</td>
							</tr>
							<tr>
								<td class="label">Estimate No:</td>
								<td class="info">${estimate.estimate_id!""}</td>
							</tr>
							<tr>
								<td></td>
								<td></td>
							</tr>
						</table>
					</td>

					<td>
						<table align="right">
							<tr>
								<td class="label">Doctor: </td>
								<td class="info">${doctorName!""}</td>
							</tr>
							<tr>
								<td class="label">Date:</td>
								<td class="info">${estimate.estimate_date!""}</td>
							</tr>

						</table>
					</td>
				</tr>
			</table>
		[#else]
			<table width="100%" cellspacing="0" cellpadding="0" border="0">
				<tr>
					<td valign="top">
						<table>
							<tr>
								<td class="label">Patient: </td>
								<td class="info">${patient.full_name!""} </td>
							</tr>
							<tr>
								<td class="label">Estimate No:</td>
								<td class="info">${estimate.estimate_id!""}</td>
							</tr>
							<tr>
								<td class="label">MR No: </td>
								<td class="info">${patient.mr_no!""}
									[#if patient.org_name != "GENERAL"](${patient.org_name!""})[/#if]
								</td>
							</tr>
						</table>
					</td>

					<td>
						<table align="right">
							<tr>
								<td class="label">Doctor:</td>
								<td class="info">${doctorName!""}</td>
							</tr>
							<tr>
								<td class="label">Date:</td>
								<td class="info">${estimate.estimate_date!""}</td>
							</tr>


						</table>
					</td>
				</tr>
			</table>
		[/#if]
	[/#escape]

	<br/>

	<table style="border-collapse: collapse;border-top: 1px solid silver;border-bottom: 1px solid silver;" width="100%" border="0">
		<tr>
			<th align="left" style="width: 3%;border-bottom: 1px solid silver;padding: 1px 1px 1px 0px;">#</th>
			<!--	[#if hasDiscounts] -->
				<th align="left" style="width: 40%; border-bottom: 1px solid silver;padding: 1px 1px 1px 0px;">Particulars</th>
			<!--	[#else] -->
				<th align="left" style="width: 48%; border-bottom: 1px solid silver;padding: 1px 1px 1px 0px;">Particulars</th>
			<!--	[/#if] -->
			<th align="left" style="width: 7%;border-bottom: 1px solid silver;padding: 1px 1px 1px 0px;">Qty</th>
			<th align="left" style="width: 8%;border-bottom: 1px solid silver;padding: 1px 1px 1px 0px;">Mfr</th>
			<th align="left" style="width: 8%;border-bottom: 1px solid silver;padding: 1px 1px 1px 0px;">Batch</th>
			<th align="left" style="width: 8%;border-bottom: 1px solid silver;padding: 1px 1px 1px 0px;">Expiry</th>
			<th align="right" style="width: 8%;border-bottom: 1px solid silver;padding: 1px 1px 1px 0px;">Rate</th>
			<!--	[#if hasDiscounts] -->
				<th align="left" style="width: 8%;border-bottom: 1px solid silver;padding: 1px 1px 1px 0px;">Disc</th>
			<!--	[/#if] -->
			<th align="right" style="width: 10%;border-bottom: 1px solid silver;padding: 1px 1px 1px 0px;">Amount</th>
		</tr>

		<!--	[#assign total=0]
		[#assign bill_discount = estimate.discount!0]
		[#assign roundoff=estimate.round_off!0]
		[#assign sno=1]

		[#list items as s]
			[#escape x as x?html] -->
				<tr>
					<td style="padding: 1px 1px 1px 0px;">${sno?string("#")}</td>
					<td style="padding: 1px 1px 1px 0px;">${s.medicine_short_name}</td>
					<td style="padding: 1px 1px 1px 0px;">${s.quantity}</td>
					<td style="padding: 1px 1px 1px 0px;">${s.manf_mnemonic}</td>
					<td style="padding: 1px 1px 1px 0px;">${s.batch_no}</td>
					<td style="padding: 1px 1px 1px 0px;">[#if s.expiry_date?has_content]${s.expiry_date?string("MMM-yyyy")}[/#if]</td>
					<td align="right" style="padding: 1px 1px 1px 0px;">${s.rate}</td>
					<!--	[#if hasDiscounts] -->
						<td style="padding: 1px 1px 1px 0px;"><!--	[#if (s.discount_per!0) != 0] -->${s.discount_per}% <!--[/#if ] --></td>
					<!--	[/#if] -->
					<td align="right" style="padding: 1px 1px 1px 0px;">${(s.amount)}</td>
				</tr>
			<!--	[/#escape]
			[#assign total=(total+s.amount)]
			[#assign sno=sno+1]
		[/#list]

		[#if hasDiscounts]
			[#assign span = 8]
		[#else]
			[#assign span = 7]
		[/#if]

		[#if (bill_discount != 0)] -->
			<tr>
				<!--	[#if (estimate.discount_per != 0)] -->
					<td colspan="${span}" align="right">Bill Discount @${estimate.discount_per?string("##.00")}%</td>
				<!--	[#else] -->
					<td colspan="${span}" align="right">Bill Discount</td>
				<!--	[/#if] -->
				<td align="right">${bill_discount}</td>
			</tr>
			<!--	[#assign total=(total - bill_discount)]
		[/#if]
		[#if roundoff !=0.00] -->
			<tr>
				<td colspan="${span}" align="right">Round Off</td>
				<td align="right">${roundoff}</td>
			</tr>
			<!--	[#assign total=(total + roundoff)]
		[/#if] -->
	</table>

	<br/>

	<table cellpadding="0" celspacing="0" width="100%" border="0">
		<tr>
			<td>

			</td>
			<td align="right">Total: <b>${total}</b></td>
		</tr>

		<!--	[#list vatDetails?keys as rate] -->
			<tr>
				<td>VAT @${rate?string}%: ${vatDetails[rate]}</td>
				<td></td>
			</tr>
		<!--	[/#list] -->
	</table>

	<br/>

	[#escape x as x?html]
	  <p>${estimate.user_display_name}</p>
	[/#escape]
