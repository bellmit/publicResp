<html>
<head>
<title> HMS </title>
<style type="text/css">
body {
}

body,pre {
	color: black;
	background-color:  #FFFFFF;
	font-family: Courier New, Fixed;
}

table {
	empty-cells: show;
}

table#patientDetails {
	margin-bottom: 20px;
}
table#patientDetails td {
padding: 1px 4px 1px 4px;
}
table#patientDetails td.info {
	padding-right: 50px;
	font-weight: bold;
}
table#licenseDetails td.info {
	padding-right: 50px;
	font-weight: bold;
}

table.items {
	border-collapse: collapse;
	border-top: 1px solid silver;
	border-bottom: 1px solid silver;
}
table.items th {
	border-top: 1px solid silver;
	border-bottom: 1px solid silver;
padding: 2px 8px 2px 3px;
}
table.items td {
padding: 2px 8px 2px 3px;
	}

table.items td.number {
	text-align: right;
}

.pageHeader {
	font-family: Verdana, Arial, sans-serif;
	font-size: 14px;
	font-weight: bold;
	   text-decoration: none;
	   text-align: center;
	   padding-bottom: 1em;
}


</style>
</head>
<body>
<#assign b = billDetails.bill>
<#assign refund = billDetails.refunds>
<#assign receipt = billDetails.receipts>
<#assign charge = billDetails.charges>
<#setting time_format="HH:mm">
<#setting date_format="dd-MM-yyyy">

<#assign patient = patientDetails>

<div align="center">
<#if "${payType}"=="receipt">
	<b>RECEIPT</b>
<#elseif "${payType}"=="refund">
	<b>REFUND VOUCHER</b>
</#if>
</div>
<br/>

<#assign ramount = 0>
<#assign rtype = "">

<#assign totalReceiptAmount = 0>
<#assign totalRefundAmount = 0>

	<#list receipt as rcp>
		<#assign totalReceiptAmount = totalReceiptAmount+rcp.amount>
	</#list>
	<#list refund as rfd>
		<#assign totalRefundAmount = totalRefundAmount+rfd.amount>
	</#list>

	<#if "${payType}"=="receipt">
		<#list receipt as rcp>
			<#if "${rcp.receiptNo}" == "${receiptNo}">
				<#if "${rcp.receiptType}" == "A">
					<#assign rtype = "Advance">
				<#elseif "${rcp.receiptType}" == "S">
					<#assign rtype = "Settlement">
				</#if>
					<#assign ramount = rcp.amount>
					<#assign rdate = rcp.receiptDate>
			</#if>
		</#list>
	<#elseif "${payType}"=="refund">
		<#list refund as rfd>
			<#if "${rfd.receiptNo}" == "${receiptNo}">
				<#if "${rfd.refundType}" == "A">
					<#assign rtype = "Advance">
				<#elseif "${rfd.refundType}" == "S">
					<#assign rtype = "Settlement">
				</#if>
					<#assign ramount = rfd.amount>
					<#assign rdate = rfd.receiptDate>
			</#if>
		</#list>
	</#if>

<#escape x as x?html>
<#attempt>
	<table width="100%" border="0">
	<#list patient as p>
	<tr>
		<td class="label">Patient Visit Id:</td><td> ${p.incoming_visit_id}</td>
		<td class="label">Receipt No:</td><td>${receiptNo}</td>
	</tr>
	<tr>
		<td class="label">Patient Name:</td><td>${p.patient_name}</td>
		<td class="label">Hospital Name:</td><td>${p.hospital_name}</td>
	</tr>
	<tr>
		<td class="label">Age/Gender:</td><td>${p.patient_age} Y/${p.patient_gender}</td>
		<td class="label">Receipt Date:</td><td>${rdate}</td>
	</tr>
	<tr>
		<td class="label"></td><td></td>
		<#attempt>
			<#if "${p.doctor_name}" != "">
	    	<td class="label">Referral Doctor:</td><td>${p.doctor_name}</td>
	    	</#if>
	    	<#recover>
	    </#attempt>
	</tr>
	</#list>
	</table>
<#recover>
</#attempt>
</#escape>

<br/>

<hr></hr>

<table cellpadding="0" celspacing="0" width="100%" border="0">
		<#if "${payType}"=="receipt">
		<tr>
			<td>Received Amount:<b>Rs.${ramount}</b> </td><td> Towards: ${rtype}</td>
		</tr>
		<#elseif "${payType}"=="refund">
		<tr>
			<td>Refunded Amount:<b>Rs.${ramount}</b> </td>
		</tr>
		</#if>

		<#if "${paymentMode}" =="C">
			<tr>
				<td align="left">Payment Mode: <b>Cash</b></td>
			</tr>
		</#if>
		<#if "${paymentMode}" =="R">
			<tr>
				<td align="left">Payment Mode: <b>Credit Card</b></td>
			</tr>
		</#if>
		<#if "${paymentMode}" =="B">
			<tr>
				<td align="left">Payment Mode: <b>Debit Card</b></td>
			</tr>
		</#if>
		<#if "${paymentMode}" =="Q">
			<tr>
				<td align="left">Payment Mode: <b>Cheque</b></td>
			</tr>
		</#if>
		<#if "${paymentMode}" =="D">
			<tr>
				<td align="left">Payment Mode: <b>Demand Draft</b></td>
			</tr>
		</#if>

		<#if "${payType}"=="receipt">
		<tr>
			<td>Against Bill No: <b>${billNo}</b></td><td>Net amount received against this bill:Rs. ${totalReceiptAmount - totalRefundAmount}</td>
		</tr>
		<#elseif "${payType}"=="refund">
		<tr>
			<td>Against Bill No: <b>${billNo}</b></td><td>Net deposits against this bill:Rs. ${totalReceiptAmount - totalRefundAmount} </td>
		</tr>
		<tr>
			<td>Narration:</td>
		</tr>
		</#if>

</table>
</body>
</html>
