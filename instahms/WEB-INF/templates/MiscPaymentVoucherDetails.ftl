<!-- [#setting datetime_format ="dd-MM-yyyy"]-->
<html>
	<body>
[#escape x as x?html]
		<table align="center" width="100%">
			<tr>
				<td align="center"><b>Miscellaneous Payment Voucher Details </b></td>
			</tr>
		</table>
		[#assign payeeName=""]
		[#assign postedDate=""]
		[#list miscVoucherDetails as v]
			[#assign payeeName	= v.payee_name]
			[#assign postedDate = v.date]
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
				<th>Posted Date</th>
				<th>Description</th>
				<th>Category</th>
				<th align="right">Amount</th>
			</tr>
			[#assign GrandTotal=0]
			[#assign TotalAmount=0]
			[#assign serviceTax=0]
			[#assign tds=0]
			[#list miscVoucherDetails as voucher]
			<tr>
				<td>${voucher.date}</td>
				<td>${voucher.description!}</td>
				<td>${voucher.category!}</td>
				<td align="right">${voucher.amount}</td>
			</tr>
			[#assign TotalAmount = voucher.totalamount]
			[#assign serviceTax = voucher.tax_amount]
			[#assign tds = voucher.tds_amount]
			[/#list]
			</table>
			<table width="100%" style="margin-top:1em" cellspacing="3" cellpadding="0">
			<tr>
				<td>Tax Amount : ${serviceTax}</td>
				<td>TDS Amount : ${tds}</td>
				<td align="right">Total Amount : ${TotalAmount}</td>
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
		<table width="100%" cellspacing="3" cellpadding="0" >
			<tr>
				<td>Payment Mode: ${paymentMode}</td>
				<td>Card Type: ${cardType!}</td>
				<td>Bank Name: ${bankName!}</td>
				<td>Reference No: ${referenceNo!}</td>
			</tr>
		</table>
		<table width="100%" cellspacing="10" cellpadding="0">
			<tr>
				<td align="center">Prepared by</td>
				<td align="center">Authorized by</td>
				<td align="center">Receiver's Signature</td>
			</tr>
		</table>
[/#escape]
	</body>
</html>
