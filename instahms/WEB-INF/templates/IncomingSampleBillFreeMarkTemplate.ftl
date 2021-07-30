<html>
<head>
<title> HMS </title>
<style type="text/css">
body {
	font-family:${printMaster.font_name};
	font-size: ${printMaster.font_size}pt;
	width: ${printMaster.page_width}pt;
	height: ${printMaster.page_height}pt;
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
<#assign print = printMaster>
<#assign sample = sampleDetails>
<#assign b = billDetails.bill>
<#assign refund = billDetails.refunds>
<#assign receipt = billDetails.receipts>
<#setting time_format="HH:mm">

<#assign patient = patientDetails>

<table width="100%" border="0">
<#if "${print.logo_header}"=="Y">
	<tr>
		<td>
			<img src="/report.do?method=getImage" height="100">
		</td>
	<#attempt>
	<#if "${printMaster}" != "">
		<td class="info">
			${print.header1}<br/>
			${print.header2}<br/>
			${print.header3}<br/>
		</td>
	</#if>
	<#recover>
	</#attempt>

	</tr>
</#if>
<#if "${print.logo_header}"=="L">
<tr>
<td>
	<img src="/report.do?method=getImage" height="100" />
</td>
</tr>
</#if>
<#if "${print.logo_header}"=="H">
	<tr>
<#attempt>
<#if "${printMaster}" != "">
	<td>
		${print.header1}<br/>
		${print.header2}<br/>
		${print.header3}<br/>
	</td>
</#if>
<#recover>
</#attempt>
	</tr>
</#if>
</table>
<#attempt>
	<table width="100%" border="0">
	<#list patient as p>
	<tr>
		<td>Patient Name: ${p.patient_name}</td>
	    <td align="center">Lab Name: ${p.hospital_name}</td>
	</tr>
	</#list>
	</table>
<#recover>
</#attempt>

<table class="items" width="100%" border="0">
	<tr>
		<th align="right" style="width: 5%">#</th>
		<th align="left" style="width: 10%">Test Name</th>
		<th align="left" style="width: 10%">Sample No</th>
		<th align="right" style="width: 10%">Amount</th>
	</tr>

	<#assign total=0>
	<#assign sno=1>
	<#list sample as s>
	<tr>
		<td align="right">${sno}</td>
		<td>${s.test_name}</td>
		<td>${s.orig_sample_no}</td>
		<td align="right">${s.amount}</td>
		</tr>
		<#assign total=(total+s.amount)>
		<#assign sno=sno+1>
	</#list>
</table>

<br/>

<table cellpadding="0" celspacing="0" width="100%" border="0">
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
	<tr>
		<td align="right">Total : <b>${total}</b></td>
	</tr>
</table>
<#attempt>
<#if "${printMaster}" !="" >
<div>${print.footer1}</div>
<div>${print.footer2}</div>
</#if>
<#recover>
</#attempt>
<br/>
</body>
</html>
