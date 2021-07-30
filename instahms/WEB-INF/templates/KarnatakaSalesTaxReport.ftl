<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>Sales-Tax Account Register- Insta HMS</title>
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

<div align="center">
	<b>
		Sales-Tax Account Register
	</b>
</div>
<div align="center">
	${from_date} to ${to_date}
</div>
<div align="center">
	Center:<b> ${center_name}</b>
</div>
<div align="center">
	Store:<b>${storeName}</b>
</div>

<table width='100%'>
	<tr>
		<td style="padding-top: 2em">
			<table border='1' cellspacing='0' cellpadding='0' width='100%'>
				<tr >
					<td></td>
					<td>Billed Vlaue</td>
					<td colspan="2" style="width:10%">CP@${goods_tax_rate}%</td>
					<td colspan="2" style="width:10%">MRP@${goods_tax_rate}%</td>
					<td colspan="2" style="width:10%">CP@${packaged_goods_tax_rate}%</td>
					<td colspan="2" style="width:10%">MRP@${packaged_goods_tax_rate}%</td>
					<td colspan="2" style="width:10%">CP@Others</td>
					<td colspan="2" style="width:10%">MRP@Others</td>
				</tr>
				<tr>
					<td></td>
					<td></td>
					<td style="width:8%">Pre-Amount</td>
					<td style="width:5%">Tax</td>
					<td style="width:8%">Pre-Amount</td>
					<td style="width:5%">Tax</td>
					<td style="width:8%">Pre-Amount</td>
					<td style="width:5%">Tax</td>
					<td style="width:8%">Pre-Amount</td>
					<td style="width:5%">Tax</td>
					<td style="width:8%">Pre-Amount</td>
					<td style="width:5%">Tax</td>
					<td style="width:8%">Pre-Amount</td>
					<td style="width:5%">Tax</td>
				</tr>

				<tr>
					<td>PHARMACY SALES BILLS (CASH)</td>
					<td style="width:5%">${(cash_sales_bean.billed_value)!}</td>
					<td style="width:5%">${(cash_sales_bean.camt)!}</td>
					<td style="width:5%">${(cash_sales_bean.ctax)!}</td>
					<td style="width:5%">${(cash_sales_bean.mamt)!}</td>
					<td style="width:5%">${(cash_sales_bean.mtax)!}</td>
					<td style="width:5%">${(cash_sales_bean.packagedcamt)!}</td>
					<td style="width:5%">${(cash_sales_bean.packagedctax)!}</td>
					<td style="width:5%">${(cash_sales_bean.packagedmamt)!}</td>
					<td style="width:5%">${(cash_sales_bean.packagedmtax)!}</td>
					<td style="width:5%">${(cash_sales_bean.otherscamt)!}</td>
					<td style="width:5%">${(cash_sales_bean.othersctax)!}</td>
					<td style="width:5%">${(cash_sales_bean.othersmamt)!}</td>
					<td style="width:5%">${(cash_sales_bean.othersmtax)!}</td>
				</tr>
				<tr>
					<td>RETURNED PHARMACY SALES BILLS (CASH)</td>
					<td style="width:5%">${(cash_returns_bean.billed_value)!}</td>
					<td style="width:5%">${(cash_returns_bean.camt)!}</td>
					<td style="width:5%">${(cash_returns_bean.ctax)!}</td>
					<td style="width:5%">${(cash_returns_bean.mamt)!}</td>
					<td style="width:5%">${(cash_returns_bean.mtax)!}</td>
					<td style="width:5%">${(cash_returns_bean.packagedcamt)!}</td>
					<td style="width:5%">${(cash_returns_bean.packagedctax)!}</td>
					<td style="width:5%">${(cash_returns_bean.packagedmamt)!}</td>
					<td style="width:5%">${(cash_returns_bean.packagedmtax)!}</td>
					<td style="width:5%">${(cash_returns_bean.otherscamt)!}</td>
					<td style="width:5%">${(cash_returns_bean.othersctax)!}</td>
					<td style="width:5%">${(cash_returns_bean.othersmamt)!}</td>
					<td style="width:5%">${(cash_returns_bean.othersmtax)!}</td>
				</tr>

				<tr>
					<td>NET CASH BILLS</td>
					<td style="width:5%">${cash_sales_bean.billed_value+cash_returns_bean.billed_value}</td>
					<td style="width:5%">${cash_sales_bean.camt+cash_returns_bean.camt}</td>
					<td style="width:5%">${cash_sales_bean.ctax+cash_returns_bean.ctax}</td>
					<td style="width:5%">${cash_sales_bean.mamt+cash_returns_bean.mamt}</td>
					<td style="width:5%">${cash_sales_bean.mtax+cash_returns_bean.mtax}</td>
					<td style="width:5%">${cash_sales_bean.packagedcamt+cash_returns_bean.packagedcamt}</td>
					<td style="width:5%">${cash_sales_bean.packagedctax+cash_returns_bean.packagedctax}</td>
					<td style="width:5%">${cash_sales_bean.packagedmamt+cash_returns_bean.packagedmamt}</td>
					<td style="width:5%">${cash_sales_bean.packagedmtax+cash_returns_bean.packagedmtax}</td>
					<td style="width:5%">${cash_sales_bean.otherscamt+cash_returns_bean.otherscamt}</td>
					<td style="width:5%">${cash_sales_bean.othersctax+cash_returns_bean.othersctax}</td>
					<td style="width:5%">${cash_sales_bean.othersmamt+cash_returns_bean.othersmamt}</td>
					<td style="width:5%">${cash_sales_bean.othersmtax+cash_returns_bean.othersmtax}</td>
				</tr>

				<tr>
					<td>PHARMACY SALES BILLS (CREDIT)</td>
					<td style="width:5%">${(credit_sales_bean.billed_value)!}</td>
					<td style="width:5%">${(credit_sales_bean.camt)!}</td>
					<td style="width:5%">${(credit_sales_bean.ctax)!}</td>
					<td style="width:5%">${(credit_sales_bean.mamt)!}</td>
					<td style="width:5%" >${(credit_sales_bean.mtax)!}</td>
					<td style="width:5%">${(credit_sales_bean.packagedcamt)!}</td>
					<td style="width:5%">${(credit_sales_bean.packagedctax)!}</td>
					<td style="width:5%">${(credit_sales_bean.packagedmamt)!}</td>
					<td style="width:5%">${(credit_sales_bean.packagedmtax)!}</td>
					<td style="width:5%">${(credit_sales_bean.otherscamt)!}</td>
					<td style="width:5%">${(credit_sales_bean.othersctax)!}</td>
					<td style="width:5%">${(credit_sales_bean.othersmamt)!}</td>
					<td style="width:5%">${(credit_sales_bean.othersmtax)!}</td>
				</tr>
				<tr>
					<td>RETURNED PHARMACY SALES BILLS (CREDIT)</td>
					<td style="width:5%">${(credit_returns_bean.billed_value)!}</td>
					<td style="width:5%">${(credit_returns_bean.camt)!}</td>
					<td style="width:5%">${(credit_returns_bean.ctax)!}</td>
					<td style="width:5%">${(credit_returns_bean.mamt)!}</td>
					<td style="width:5%">${(credit_returns_bean.mtax)!}</td>
					<td style="width:5%">${(credit_returns_bean.packagedcamt)!}</td>
					<td style="width:5%">${(credit_returns_bean.packagedctax)!}</td>
					<td style="width:5%">${(credit_returns_bean.packagedmamt)!}</td>
					<td style="width:5%">${(credit_returns_bean.packagedmtax)!}</td>
					<td style="width:5%">${(credit_returns_bean.otherscamt)!}</td>
					<td style="width:5%">${(credit_returns_bean.othersctax)!}</td>
					<td style="width:5%">${(credit_returns_bean.othersmamt)!}</td>
					<td style="width:5%">${(credit_returns_bean.othersmtax)!}</td>
				</tr>

				<tr>
					<td>NET CREDIT BILLS</td>
					<td style="width:5%">${credit_sales_bean.billed_value+credit_returns_bean.billed_value}</td>
					<td style="width:5%">${credit_sales_bean.camt+credit_returns_bean.camt}</td>
					<td style="width:5%">${credit_sales_bean.ctax+credit_returns_bean.ctax}</td>
					<td style="width:5%">${credit_sales_bean.mamt+credit_returns_bean.mamt}</td>
					<td style="width:5%">${credit_sales_bean.mtax+credit_returns_bean.mtax}</td>
					<td style="width:5%">${credit_sales_bean.packagedcamt+credit_returns_bean.packagedcamt}</td>
					<td style="width:5%">${credit_sales_bean.packagedctax+credit_returns_bean.packagedctax}</td>
					<td style="width:5%">${credit_sales_bean.packagedmamt+credit_returns_bean.packagedmamt}</td>
					<td style="width:5%">${credit_sales_bean.packagedmtax+credit_returns_bean.packagedmtax}</td>
					<td style="width:5%">${credit_sales_bean.otherscamt+credit_returns_bean.otherscamt}</td>
					<td style="width:5%">${credit_sales_bean.othersctax+credit_returns_bean.othersctax}</td>
					<td style="width:5%">${credit_sales_bean.othersmamt+credit_returns_bean.othersmamt}</td>
					<td style="width:5%">${credit_sales_bean.othersmtax+credit_returns_bean.othersmtax}</td>
				</tr>


				<tr>
					<td>TOTALS</td>
					<td style="width:5%">${(cash_sales_bean.billed_value+cash_returns_bean.billed_value)+(credit_sales_bean.billed_value+credit_returns_bean.billed_value)}</td>
					<td style="width:5%">${(cash_sales_bean.camt+cash_returns_bean.camt)+(credit_sales_bean.camt+credit_returns_bean.camt)}</td>
					<td style="width:5%">${(cash_sales_bean.ctax+cash_returns_bean.ctax)+(credit_sales_bean.ctax+credit_returns_bean.ctax)}</td>
					<td style="width:5%">${(cash_sales_bean.mamt+cash_returns_bean.mamt)+(credit_sales_bean.mamt+credit_returns_bean.mamt)}</td>
					<td style="width:5%">${(cash_sales_bean.mtax+cash_returns_bean.mtax)+(credit_sales_bean.mtax+credit_returns_bean.mtax)}</td>
					<td style="width:5%">${(cash_sales_bean.packagedcamt+cash_returns_bean.packagedcamt)+(credit_sales_bean.packagedcamt+credit_returns_bean.packagedcamt)}</td>
					<td style="width:5%">${(cash_sales_bean.packagedctax+cash_returns_bean.packagedctax)+(credit_sales_bean.packagedctax+credit_returns_bean.packagedctax)}</td>
					<td style="width:5%">${(cash_sales_bean.packagedmamt+cash_returns_bean.packagedmamt)+(credit_sales_bean.packagedmamt+credit_returns_bean.packagedmamt)}</td>
					<td style="width:5%">${(cash_sales_bean.packagedmtax+cash_returns_bean.packagedmtax)+(credit_sales_bean.packagedmtax+credit_returns_bean.packagedmtax)}</td>
					<td style="width:5%">${(cash_sales_bean.otherscamt+cash_returns_bean.otherscamt)+(credit_sales_bean.otherscamt+credit_returns_bean.otherscamt)}</td>
					<td style="width:5%">${(cash_sales_bean.othersctax+cash_returns_bean.othersctax)+(credit_sales_bean.othersctax+credit_returns_bean.othersctax)}</td>
					<td style="width:5%">${(cash_sales_bean.othersmamt+cash_returns_bean.othersmamt)+(credit_sales_bean.othersmamt+credit_returns_bean.othersmamt)}</td>
					<td style="width:5%">${(cash_sales_bean.othersmtax+cash_returns_bean.othersmtax)+(credit_sales_bean.othersmtax+credit_returns_bean.othersmtax)}</td>
				</tr>
			</table>
		</td>
	</tr>
	<tr>
		<td style="padding-top: 1em">
			<table border='1' cellspacing='0' cellpadding='0' width='100%'>
				<tr >
					<td></td>
					<td>Number of Bills</td>
					<td style="width:10%">Bill Amount</td>
					<td style="width:10%">Net Amount</td>
					<td style="width:10%">Discount</td>
					<td style="width:10%">Round Offs</td>
				</tr>
				<tr>
					<td >Pharmacy Sales Cash Bills</td>
					<td>${(cash_sales_bills.count)!}</td>
					<td>${(cash_sales_bills.amount)!}</td>
					<td>${(cash_sales_bills.net_amount)!}</td>
					<td>${(cash_sales_bills.discount)!}</td>
					<td>${(cash_sales_bills.round_off)!}</td>
				</tr>
				<tr>
					<td >Pharmacy Return Cash Bills</td>
					<td>${(cash_returns_bills.count)!}</td>
					<td>${(cash_returns_bills.amount)!}</td>
					<td>${(cash_returns_bills.net_amount)!}</td>
					<td>${(cash_returns_bills.discount)!}</td>
					<td>${(cash_returns_bills.round_off)!}</td>
				</tr>
				<tr>
					<td>Net Cash Sales</td>
					<td>${(cash_sales_bills.count+cash_returns_bills.count)!}</td>
					<td >${(cash_sales_bills.amount+cash_returns_bills.amount)!}</td>
					<td >${(cash_sales_bills.net_amount+cash_returns_bills.net_amount)!}</td>
					<td >${(cash_sales_bills.discount+cash_returns_bills.discount)!}</td>
					<td >${(cash_sales_bills.round_off+cash_returns_bills.round_off)!}</td>
				</tr>
				<tr>
					<td >Pharmacy Sales Credit Bills</td>
					<td >${(credit_sales_bills.count)!}</td>
					<td >${(credit_sales_bills.amount)!}</td>
					<td >${(credit_sales_bills.net_amount)!}</td>
					<td >${(credit_sales_bills.discount)!}</td>
					<td >${(credit_sales_bills.round_off)!}</td>
				</tr>
				<tr>
					<td >Pharmacy Return Credit Bills</td>
					<td >${(credit_returns_bills.count)!}</td>
					<td >${(credit_returns_bills.amount)!}</td>
					<td >${(credit_returns_bills.net_amount)!}</td>
					<td >${(credit_returns_bills.discount)!}</td>
					<td >${(credit_returns_bills.round_off)!}</td>
				</tr>
				<tr>
					<td>Net Credit Sales</td>
					<td >${credit_sales_bills.count+credit_returns_bills.count}</td>
					<td >${credit_sales_bills.amount+credit_returns_bills.amount}</td>
					<td >${credit_sales_bills.net_amount+credit_returns_bills.net_amount}</td>
					<td >${credit_sales_bills.discount+credit_returns_bills.discount}</td>
					<td >${credit_sales_bills.round_off+credit_returns_bills.round_off}</td>
				</tr>
				<tr>
					<td >Total of Pharmacy Sales Bills</td>
					<td >${cash_sales_bills.count+credit_sales_bills.count}</td>
					<td >${cash_sales_bills.amount+credit_sales_bills.amount}</td>
					<td >${cash_sales_bills.net_amount+credit_sales_bills.net_amount}</td>
					<td >${cash_sales_bills.discount+credit_sales_bills.discount}</td>
					<td >${cash_sales_bills.round_off+credit_sales_bills.round_off}</td>
				</tr>
				<tr>
					<td >Total of Pharmacy Return Bills</td>
					<td >${cash_returns_bills.count+credit_returns_bills.count}</td>
					<td >${cash_returns_bills.amount+credit_returns_bills.amount}</td>
					<td >${cash_returns_bills.net_amount+credit_returns_bills.net_amount}</td>
					<td >${cash_returns_bills.discount+credit_returns_bills.discount}</td>
					<td >${cash_returns_bills.round_off+credit_returns_bills.round_off}</td>
				</tr>
				<tr>
					<td >Net Pharmacy Bills</td>
					<td >${cash_sales_bills.count+credit_sales_bills.count+cash_returns_bills.count+credit_returns_bills.count}</td>
					<td >${cash_sales_bills.amount+credit_sales_bills.amount+cash_returns_bills.amount+credit_returns_bills.amount}</td>
					<td >${cash_sales_bills.net_amount+credit_sales_bills.net_amount+cash_returns_bills.net_amount+credit_returns_bills.net_amount}</td>
					<td >${cash_sales_bills.discount+credit_sales_bills.discount+cash_returns_bills.discount+credit_returns_bills.discount}</td>
					<td >${cash_sales_bills.round_off+credit_sales_bills.round_off+cash_returns_bills.round_off+credit_returns_bills.round_off}</td>
				</tr>
			</table>
		</td>
	</tr>
	[#if sponsor_bills?has_content]
	<tr>
		<td style="padding-top: 1em">
			<table border='1' cellspacing='0' cellpadding='0' width='100%'>
				<tr >
					<td></td>
					<td>Number of Bills</td>
					<td style="width:10%">Bill Amount</td>
					<td style="width:10%">Net Amount</td>
					<td style="width:10%">Discount</td>
					<td style="width:10%">Round Offs</td>
				</tr>
				[#list sponsor_bills?keys as key]
				[#assign sp_no_of_bills = 0]
				[#assign sp_amount = 0]
				[#assign sp_net_amount = 0]
				[#assign sp_discount = 0]
				[#assign sp_round_off = 0]
				[#assign tpaName = '']
					[#list sponsor_bills[key] as tpa]
						<tr>
							[#if tpa.type == "S"]
								<td >Total of ${tpa.tpa_name} Pharmacy Sales Bills</td>
							[#else]
								<td >Total of ${tpa.tpa_name} Pharmacy Return Sponsor Bills</td>
							[/#if]
							[#assign sp_no_of_bills = sp_no_of_bills+(tpa.count)!]
							[#assign sp_amount = sp_amount+(tpa.amount)!]
							[#assign sp_net_amount = sp_net_amount+(tpa.net_amount)!]
							[#assign sp_discount = sp_discount+(tpa.discount)!]
							[#assign sp_round_off = sp_round_off+(tpa.round_off)!]
							[#assign tpaName = tpa.tpa_name]
							<td>${(tpa.count)!}</td>
							<td>${(tpa.amount)!}</td>
							<td>${(tpa.net_amount)!}</td>
							<td>${(tpa.discount)!}</td>
							<td>${(tpa.round_off)!}</td>
				   		 </tr>
					[/#list]
					[#if tpaName != '' ]
					<tr>
							<td >Net ${tpaName} Bills</td>
							<td>${sp_no_of_bills}</td>
							<td>${sp_amount}</td>
							<td>${sp_net_amount}</td>
							<td>${sp_discount}</td>
							<td>${sp_round_off}</td>
					</tr>
					[/#if]
				[/#list]

			</table>
		</td>
	</tr>
	[/#if]
</table>
</body>
[/#escape]
</html>
