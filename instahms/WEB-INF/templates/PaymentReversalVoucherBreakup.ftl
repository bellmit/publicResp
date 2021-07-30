<!-- [#setting datetime_format ="dd-MM-yyyy"]-->
<html>
	<body>
[#escape x as x?html]
		<table align="center" width="100%">
			<tr>
				<td align="center"><b>Receipt Voucher Details</b></td>
			</tr>
		</table>
		[#assign payeeName=""]
		[#assign postedDate=""]
		[#list paymentReversalVoucher as v]
			[#assign payeeName	= v.payee_id]
			[#assign postedDate = v.posted_date]
			[#assign remarks = v.remarks]
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
				<th>Voucher Date</th>
				<th>Description</th>
				<th>Category</th>
				<th>Amount</th>
			</tr>
			[#assign GrandTotal=0]
			[#assign TotalAmount=0]
			[#assign serviceTax=0]
			[#assign tds=0]
			[#list paymentReversalVoucher as voucher]
			<tr>
				<td>${voucher.posted_date!}</td>
				<td>${voucher.description!}</td>
				<td>${voucher.category!}</td>
				<td>${voucher.amount}</td>
			</tr>
			[#assign GrandTotal = GrandTotal + voucher.amount]
			[#assign TotalAmount = voucher.voucher_amount]
			[#assign serviceTax = voucher.tax_amount]
			[#assign tds = voucher.tds_amount]
			[/#list]
			</table>
			<table width="100%" style="margin-top:1em" cellspacing="3" cellpadding="0">
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
				<td align="right">${TotalAmount + tds}</td>
			</tr>
		</table>
		<table width="100%" cellspacing="3" cellpadding="3">
			<tr>
				<td>Amount in words : ${voucherTotalAmount}</td>
			</tr>
		</table>
		<table width="100%" cellspacing="3" cellpadding="3">
			<tr>
				<td>Remarks: ${remarks!}</td>
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
