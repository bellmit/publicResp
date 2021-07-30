<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
	<style type="text/css">
		@page {
		size: A4 landscape;
		margin: 36pt 36pt 36pt 36pt;
	}
		body {
		font-family : Arial, Helvetica;
		font-size : 10pt;
	}
	</style>
	</head>
[#escape x as x?html]
<body>
	<h1>Stock Transfer</h1>
	[#setting number_format="#"]
	[#setting datetime_format = "dd-MM-yyyy HH:MM"]
	<table width='100%'>
		<tr>
			<td>From Store :</td>
			<td>${stocktransferdetails[0].from_store!}</td>
			<td>To Store:</td>
			<td>${stocktransferdetails[0].to_store!}</td>
		</tr>
		<tr>
			<td>Transfer No :</td>
			<td >${stocktransferdetails[0].transfer_no!}</td>
			<td>Transfer Date :</td>
			<td>${stocktransferdetails[0].date!}</td>
		</tr>
		<tr>
			<td>User:</td>
			<td>${stocktransferdetails[0].username!}</td>
			<td>Transfer Reason :</td>
			<td>${stocktransferdetails[0].reason!}</td>
		</tr>
	</table>
	<table>
		<tr>
			<td>Item Name</td>
			<td>Batch No</td>
			<td>Qty</td>
			<td>Value</td>
			<td>Expiry Date</td>
			<td>Mrp</td>
			<td>Description</td>
		</tr>
		[#assign sno = 0]
		[#if stocktransferdetails??]
		<!--
			[#list stocktransferdetails as st]
		-->
		<tr>
			[#assign sno = sno+1]
			<td>${st.medicine_name!}</td>
			<td>${st.batch_no!}</td>
			<td>${(st.qty-st.qty_rejected)?string('##0.00')}</td>
			<td>${st.amt!}</td>
			<td>${st.exp_dt!}</td>
			<td>${st.mrp!}</td>
			<td>${st.description!}</td>
		</tr>
		<!--
			[/#list]
		-->

		[/#if]
	</table>
</body>
[/#escape]
</html>
