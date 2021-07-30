[#setting number_format="#"]
[#setting datetime_format = "dd-mm-yyyy hh:mm"]
[#assign issuetype={"c":"consumable","r":"retail","l":"reusable","p":"permanent"}]
[#assign status={"o":"open","c":"cancled","f":"finalized"}]

<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
	<style type="text/css">
		@page {
		size: a4 landscape;
		margin: 36pt 36pt 36pt 36pt;
	}
		body {
		font-family : arial, helvetica;
		font-size : 10pt;
	}
	</style>
	</head>
[#escape x as x?html]

<body>
<center><h1>${title}</h1></center>
<div class="patientHeader">
  	[#include "VisitDetailsHeader.ftl"]
</div>
<br/>
<h3>Indent Details :</h3>
	<table>
		<tr>
			<td>Username :</td>
			<td>${patIndentMain.username!''}</td>
			<td>Status : </td>
			<td>${patIndentMain.status!''}</td>
		</tr>
		<tr>
			<td>Dispence status :</td>
			<td>${patIndentMain.dispense_status!''}</td>
			<td>Indent Type :</td>
			<td>${patIndentMain.indent_type!''}</td>
		</tr>
		<tr>
			<td>Expected Date :</td>
			<td>${patIndentMain.expected_date!''}</td>
			<td>Remarks :</td>
			<td>${patIndentMain.remarks!''}</td>
		</tr>
		<tr>
			<td>Prescribing Doctor Name :</td>
				<td>${patIndentMain.prescribing_doctor_name!''}</td>
			<td></td>
			<td></td>
		</tr>
	</table>
<h3>Item List :</h3>
	<table cellspacing='20'>
		<tr>
			<th>S.No.</th><th>Item Name</th><th>Qty Required</th><th>Unit</th><th>Drug Type</th><th>Drug Code</th>
		</tr>
		[#list patIndentDetList as item ]
		<tr>
			<td align='center'>${item_index + 1}</td><td align='center'>${item.medicine_name!''}</td><td align='center'>${item.qty_required!''}</td><td align='center'>${item.package_uom!''}</td><td align='center'>${item.code_type!''}</td><td align='center'>${item.item_code!''}</td>
		</tr>
		[/#list]
	</table>

</body>
[/#escape]
</html>
