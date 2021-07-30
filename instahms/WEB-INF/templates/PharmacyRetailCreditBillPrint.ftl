<div>
<!--
	[#setting time_format="HH:mm"]
	[#setting date_format="dd-MM-yyyy"]
	[#setting datetime_format="dd-MM-yyyy HH:mm"]
-->

[#escape x as x?html]
	<table width="100%" border="0">
		<tr>
			<td></td>
    	<td align="right"></td>
		</tr>
	</table>
[/#escape]

<div align="center"><b><u>Duplicate Retail Credit Bill</u></b></div>

<br/>

[#escape x as x?html]

		<table width="100%" cellspacing="0" cellpadding="0" border="0">
			<tr>
				<td valign="top">
					<table>
						<tr>
							<td class="label">Patient: </td>
							<td class="info">${customer.customer_name}</td>
						</tr>
						<tr>
							<td class="label"></td>
							<td class="info"></td>
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
							<td class="info">${bill.openDate}</td>
						</tr>
						<tr>
							<td class="label">Hosp. Bill No:</td>
							<td class="info">${bill.billNo}</td>

						</tr>
					</table>
				</td>
			</tr>
		</table>

[/#escape]

<br/>

<table style="border-collapse: collapse;border-top: 1px solid silver;border-bottom: 1px solid silver;" width="100%" border="0">
	<tr>
		<th align="left" style="width: 3%;border-bottom: 1px solid silver;padding: 1px 1px 1px 0px;">#</th>
		<th align="left" style="width: 10%;border-bottom: 1px solid silver;padding: 1px 1px 1px 0px;">Date</th>
		<!--	[#if hasDiscounts] -->
			<th align="left" style="width: 30%; border-bottom: 1px solid silver;padding: 1px 1px 1px 0px;">Particulars</th>
		<!--	[#else] -->
			<th align="left" style="width: 38%; border-bottom: 1px solid silver;padding: 1px 1px 1px 0px;">Particulars</th>
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
	[#assign bill_discount = 0]
	[#assign roundoff=0]
	[#assign sno=1]

	[#list items as s]
		[#escape x as x?html] -->
			<tr>
				<td style="padding: 1px 1px 1px 0px;">${sno?string("#")}</td>
				<td style="padding: 1px 1px 1px 0px;">${s.sale_date?string("dd-MM-yyyy")}</td>
				<td style="padding: 1px 1px 1px 0px;">${s.medicine_short_name}</td>
				<td style="padding: 1px 1px 1px 0px;">${s.quantity}</td>
				<td style="padding: 1px 1px 1px 0px;">${s.manf_mnemonic?html}</td>
				<td style="padding: 1px 1px 1px 0px;">${s.batch_no}</td>
				<td style="padding: 1px 1px 1px 0px;">[#if s.expiry_date?has_content]${s.expiry_date?string("MMM-yyyy")}[/#if]</td>
				<td align="right" style="padding: 1px 1px 1px 0px;">${s.rate}</td>
				<!--	[#if hasDiscounts] -->
					<td style="padding: 1px 1px 1px 0px;"><!--	[#if (s.discount_per!0) != 0] -->${s.discount_per}% <!--[/#if ] --></td>
				<!--	[/#if] -->
				<td align="right" style="padding: 1px 1px 1px 0px;">${(s.amount)}</td>
			</tr>

			[#assign bill_discount = bill_discount + (s.bill_discount/s.rcount)]
			[#assign roundoff = round_off]
		<!--	[/#escape]
		[#assign total=(total+s.amount)]
		[#assign sno=sno+1]
	[/#list]

	[#if hasDiscounts]
		[#assign span = 9]
	[#else]
		[#assign span = 8]
	[/#if]

	[#if (bill_discount != 0)] -->
		<tr>
			<td colspan="${span}" align="right">Bill Discount</td>

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
		<td><b>(Added to Bill)</b></td>
		<td align="right">Total: <b>${total}</b></td>
	</tr>

	<!--	[#list vatDetails?keys as rate] -->
		<tr>
			<td>VAT @${rate}%: ${vatDetails[rate]}</td>
			<td></td>
		</tr>
	<!--	[/#list] -->
</table>

<br/>

[#escape x as x?html]
  <p></p>
[/#escape]

</div>

