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
<#assign sample = sampleDetails>
<#assign pack = packageDetails>
<#assign testsInPack = testInPackDetails>
<#assign b = billDetails.bill>
<#assign receipt = billDetails.receipts>
<#assign refunds = billDetails.refunds>
<#setting time_format="HH:mm">
<#setting date_format="dd-MM-yyyy">
<#assign payModeDisplay = {"C":"Cash","R":"Credit Card","Q":"Cheque","B":"Debit Card","D":"Demand Draft"}>
<#assign receiptTypeDisplay = {"A":"Advance","S":"Settlement"}>

<#assign patient = patientDetails>

<#escape x as x?html>
<div align="center" class="pageHeader">
	<b>Bill Cum Receipt</b>
</div>
<br/>

<#attempt>
	<table width="100%" border="0">
	<tr>
		<td class="label">Visit Id:</td><td> ${patient.incoming_visit_id}</td>
		<td class="label">Bill No:</td><td>${billNo}</td>
	</tr>
	<tr>
		<td class="label">Patient Name:</td><td>${patient.patient_name}</td>
		<td class="label">Hospital Name:</td><td>${patient.hospital_name}</td>
	</tr>
	<tr>
		<td class="label">Age/Gender:</td><td>${patient.patient_age?string('#')}${patient.age_unit}/${patient.patient_gender}</td>
		<#if receipt?has_content>
		    <td class="label">Bill Date:</td><td>${receipt[0].receiptDate?string("dd-MM-yyyy")}</td>
		</#if>
	</tr>
	<tr>
		<td class="label"></td><td></td>
		<#attempt>
			<#if "${patient.doctor_name!}" != "">
	    	<td class="label">Referral Doctor:</td><td>${patient.doctor_name!}</td>
	    	</#if>
	    	<#recover>
	    </#attempt>
	</tr>
	</table>
<#recover>
</#attempt>
</#escape>

<#escape x as x?html>
<table width="100%" border="0" cellpadding="0" celspacing="0">
    <#if "${printMode}" == 'P'>
	<tr><td colspan="6"><hr style="border:0.1px solid black;"/></td></tr>
	</#if>
	<tr>
		<th align="right" style="width: 5%">#</th>
		<th align="left" style="width: 15%">Package Name</th>
		<th align="center" style="width: 15%">Test Name</th>
		<th align="center" syle="width: 15%">Sample No</th>
		<th align="center" style="width: 15%">Original Sample No</th>
		<th align="right" style="width: 15%">Amount</th>
	</tr>
	<#if "${printMode}" != 'P'>
	     <p>&nbsp;&nbsp;&nbsp;</p>
	</#if>
	<#if "${printMode}" == 'P'>
	<tr><td colspan="6"><hr style="border:0.1px solid black;"/></td></tr>
	</#if>
	<#assign total=0>
	<#assign sno=1>
	<#list sample as s>
	   <#if "${s.status}" != 'X'>
		<tr>
			<td align="right">${sno?string('#')}.</td>
			<td></td>
			<td>${s.test_name}</td>
			<td align="center">${s.sample_no!}</td>
			<td align="center">${s.orig_sample_no!}</td>
			<td align="right">${s.amount}</td>

		</tr>
		<#assign total=(total+s.amount)>
		<#assign sno=sno+1>
		</#if>
	</#list>
	<#list pack as p>
		<tr>
			<td align="right">${sno?string('#')}.</td>
			<td>${p.pack_name}</td>
			<td></td>
			<td></td>
			<td></td>
			<td align="right">${p.amount}</td>
		</tr>
		<#list testsInPack as t>
			<#if "${t.pack_id}" == "${p.pack_id}">
				<tr>
					<td></td>
					<td></td>
					<td>${t.test_name}</td>
					<td align="center">${t.sample_no!}</td>
					<td>${t.orig_sample_no!}</td>
					<td></td>
				</tr>
			</#if>
		</#list>
		<#assign total=(total+p.amount)>
		<#assign sno=sno+1>
	</#list>
	<tr>
		<td colspan="4"  align="right">Total Amount:</td><td  align="right"><b>${total}</b></td>
	</tr>
</table>
<br/>
<#attempt>
<#if receipt?has_content>
  <#assign paymentTotal=0>
	<table cellpadding="0" celspacing="0" width="100%" border="0">
	<#if "${printMode}" == 'P'>
		<tr>
		    <td colspan="4"><hr style="border:0.1px solid black;"/></td>
		</tr>
		</#if>
		<tr>
			<td><b>Payments</b></td><td>Receipt No</td><td>Details</td><td>Amount</td>
		</tr>
		<#list receipt as rcp>
          <#assign recpdate = rcp.receiptDate?string("dd-MM-yyyy")>
		<#if "${printMode}" == 'P'>
		<tr>
		     <td colspan="4"><hr style="border:0.1px solid black;"/></td></tr>
		</#if>
		<tr>
			<td colspan="4">${receiptTypeDisplay[rcp.receiptType]}</td>
		</tr>
		<tr>
			<td>${recpdate}</td><td>${rcp.receiptNo}</td><td>${rcp.paymentMode}</td><td>${rcp.amount}</td>
		</tr>
		<#assign paymentTotal=(paymentTotal+rcp.amount)>
		</#list>
		<tr>
		    <td><b>Payment Total:</b>${paymentTotal}</td>
		</tr>
	 </table>
 </#if>
 <#recover>
</#attempt>
<#attempt>
<#if refunds?has_content>
  <#assign refundPaymentTotal=0>
	<table cellpadding="0" celspacing="0" width="100%" border="0">
	<#if "${printMode}" == 'P'>
		<tr>
		    <td colspan="4"><hr style="border:0.1px solid black;"/></td>
		</tr>
		</#if>
		<tr>
			<td><b> Refund Payments</b></td><td>Receipt No</td><td>Details</td><td>Amount</td>
		</tr>

		<#list refunds as ref>
          <#assign refdate = ref.receiptDate?string("dd-MM-yyyy")>

		<#if "${printMode}" == 'P'>
		<tr>
		     <td colspan="4"><hr style="border:0.1px solid black;"/></td></tr>
		</#if>

		<tr>
			<td colspan="4">${receiptTypeDisplay[ref.receiptType]}</td>
		</tr>
		<tr>
			<td>${refdate}</td><td>${ref.receiptNo}</td><td>${ref.paymentMode}</td><td>${ref.amount}</td>
		</tr>
		<#assign refundPaymentTotal=(refundPaymentTotal+ref.amount)>
		</#list>
		<tr>
		    <td><b> Refund Payment Total:</b>${refundPaymentTotal}</td>
		</tr>
	 </table>
 </#if>
 <#recover>
</#attempt>
	<table align="right">
	  <tr>
	    <td>Signature</td>
	   </tr>
    </table>
</#escape>
</body>
</html>
