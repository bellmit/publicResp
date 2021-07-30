<!-- [#setting datetime_format ="dd-MM-yyyy"]-->
<html>
	<body>
[#escape x as x?html]
		<table align="center" width="100%">
			<tr>
				<td align="center"><b>Payment Voucher Details</b></td>
			</tr>
		</table>
		[#assign payeeName=""]
		[#assign postedDate=""]
		[#list voucherDetails as v]
			[#assign payeeName	= v.payee_name]
			[#assign postedDate = v.voucher_date]
			[#assign remarks = v.remarks]
			[#assign paymentType = v.payment_type]
		[/#list]
		<table width="100%">
			<tr>
				<td>Voucher No:  ${voucherNo}</td>
				<td align="right">Date: ${postedDate}</td>
			</tr>
			<tr>
				<td>Pay to: ${payeeName}</td>
			</tr>
		</table>
		<table width="100%">
			<tr class="border-above-below">
				<th>Bill Date</th>
				<th>Bill No</th>
				<th>MR No</th>
				<th>Patient Name</th>
				<th>Charge Head</th>
				[#if paymentType != 'D']
					<th>Description</th>
				[/#if]
				<th>Amount</th>
			</tr>
			[#assign GrandTotal=0]
			[#assign TotalAmount=0]
			[#assign serviceTax=0]
			[#assign tds=0]
			[#list voucherDetails as voucher]
			<tr>
				<td>${voucher.conducted_date!}</td>
				<td>${voucher.bill_no}</td>
				<td>${voucher.mr_no}</td>
				<td>${voucher.full_name}</td>
				<td>${voucher.chargehead_name}</td>
				[#if paymentType != 'D']
					<td>${voucher.description}</td>
				[/#if]
				<td>${voucher.amount}</td>
			</tr>
			[#assign GrandTotal = GrandTotal + voucher.amount]
			[#assign TotalAmount = voucher.voucher_amount]
			[#assign serviceTax = voucher.tax_amount]
			[#assign tds = voucher.tds_amount]
			[/#list]
			</table>
			<table width="100%" cellspacing="0" cellpadding="0">
				<tr>
					<td>Discount Summary </td>
				</tr>
				<tr class="border-above-below">
					<th>Bill No</th>
					<th>Discount Authorizer</th>
					<th align="right">Discount Amount</th>
				</tr>
			[#list voucherDetails as discount]
			[#assign remarks = discount.remarks!]
				[#if discount.overall_discount_auth_name??]
					<tr>
						<td>${discount.bill_no}</td>
						<td>${discount.overall_discount_auth_name!}</td>
						<td align="right">${discount.overall_discount_amt}</td>
					</tr>
				[/#if]
				[#if discount.discount_auth_dr_name?? ]
					<tr>
						<td>${discount.bill_no}</td>
						<td>${discount.discount_auth_dr_name!}</td>
						<td align="right">${discount.dr_discount_amt}</td>
					</tr>
				[/#if]
				[#if discount.discount_auth_pres_dr_name??]
					<tr>
						<td>${discount.bill_no}</td>
						<td>${discount.discount_auth_pres_dr_name!}</td>
						<td align="right">${discount.pres_dr_discount_amt}</td>
					</tr>
				[/#if]
				[#if discount.discount_auth_ref_name??]
					<tr>
						<td>${discount.bill_no}</td>
						<td>${discount.discount_auth_ref_name!}</td>
						<td align="right">${discount.ref_discount_amt}</td>
					</tr>
				[/#if]
				[#if discount.discount_auth_hosp_name??]
					<tr>
						<td>${discount.bill_no}</td>
						<td>${discount.discount_auth_hosp_name!}</td>
						<td align="right">${discount.hosp_discount_amt}</td>
					</tr>
				[/#if]
			[/#list]
			</table>
			<table width="100%" cellspacing="3" cellpadding="3">
			<tr>
				<td>Remarks: ${remarks!}</td>
			</tr>
			</table>
			<table width="100%" style="margin-top:1em" cellspacing="3" cellpadding="3">
			<tr class="border-above-below">
				<td colspan="3">Bill Summary</td>
			</tr>
			<tr>
				<td width="70%"></td>
				<td align="right">Grand total </td>
				<td align="right">${GrandTotal}</td>
			</tr>
			<tr>
				<td width="70%"></td>
				<td align="right">(+)Service Tax</td>
				<td align="right">${serviceTax}</td>
			</tr>
			<tr>
				<td width="70%"></td>
				<td align="right">(-) TDS </td>
				<td align="right">${tds}</td>
			</tr>
			<tr>
				<td width="70%"></td>
				<td align="right">Total Amount</td>
				<td align="right">${TotalAmount}</td>
			</tr>
		</table>
		<table width="100%" cellspacing="3" cellpadding="3">
			<tr>
				<td>Amount in words : ${voucherTotalAmount}</td>
			</tr>
		</table>
		<table width="100%" cellspacing="3" cellpadding="0">
			<tr>
				<td>Payment Mode: ${paymentMode}</td>
				<td>Card Type: ${cardType!}</td>
				<td>Bank Name: ${bankName!}</td>
				<td>Reference No: ${referenceNo!}</td>
			</tr>
			<tr>
				<td>Note: Total Amount shown includes all discounts.</td>
			</tr>
		</table>
[/#escape]
	</body>
</html>
