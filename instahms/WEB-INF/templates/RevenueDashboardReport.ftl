
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>Consolidated Financial Summary - Insta HMS</title>
	<#include "ReportStyles.ftl">
	<style>
		table.report th {text-align: right}
		table.report td.sub {padding-left: 1em;}
	</style>
	<script>

	function popHelp(uri){
		  var helpWin=window.open(uri,'CFD-Help','height=700,width=800,resizable=yes,scrollbars=yes,status=no');
		  helpWin.focus();
	}

	</script>
</head>

<#setting date_format="dd-MM-yyyy">
<#escape x as x?html>

<#if dateField == 'posted_date'>
	<#assign dateType = 'Posted Date'>
<#else>
	<#assign dateType = 'Finalized Date'>
</#if>

<#if center_id?number != 0>
	<#assign centerFilterStr = " For " + center_name>
<#else>
	<#assign centerFilterStr = "">
</#if>

<body>
<div align="center">
	<span style="font-size: 12pt;font-weight: bold;margin-top:10px;"> Consolidated Financial Summary </span>
	<p style="margin-top:2px;">${fromDate} - ${toDate} <span style="font-size: 8pt">(as of ${curDateTime} based on ${dateType}${centerFilterStr})</span></p>
</div>

<table>
	<tr>
		<td valign="top">
			<table class="CFDreport" style="margin-top: 1em;" width="100%">
				<tr class="heading">
					<td>Patient Counts</td>
					<td></td>
				</tr>
				<tr>
					<td>OP Registrations</td>
					<td class="number"> ${(opRegCount!0)?string("#")}</td>
				</tr>
				<tr>
					<td>OSP Registrations</td>
					<td class="number"> ${(ospRegCount!0)?string("#")}</td>
				</tr>
				<tr>
					<td> IP Admissions</td>
					<td class="number"> ${(ipRegCount!0)?string("#")}</td>
				</tr>
				<tr>
					<td> IP Discharges</td>
					<td class="number"> ${(ipDischargeCount!0)?string("#")}</td>
				</tr>
				<tr>
					<td>Active In-Patients (Current)</td>
					<td class="number"> ${(inPatientCount!0)?string("#")}</td>
				</tr>
			</table>
		</td>
		<td>
			<table class="CFDreport" style="margin-left: 1em; margin-top: 1em;" width="96%">
				<tr class="heading">
					<td>Bill Counts</td>
					<td></td>
				</tr>
				<tr>
					<td>Bills Closed</td>
					<td class="number">${(closedBillCount!0)?string("#")}</td>
				</tr>
				<tr>
					<td>Bills Cancelled</td>
					<td class="number">${(cancelledBillCount!0)?string("#")}</td>
				</tr>
				<tr>
					<td> Bill Later Open (Current)</td>
					<td class="number">${(billLaterCount!0)?string("#")}</td>
				</tr>
				<tr>
					<td> Bill Now Open (Current)</td>
					<td class="number">${(billNowCount!0)?string("#")}</td>
				</tr>
			</table>
		</td>
	</tr>

	<tr>
		<td valign="top" style="padding-top: 1em">
			<table cellspacing="2" class="CFDreport">
				<tr class="heading">
					<td>Cash Flow By Type</td>
					<td>Count</td>
					<td>Amount</td>
				</tr>

				<#-- Bill Later: Advances, Settlements, Refunds, Sponsor -->
				<#assign creditGroupDisplay = {"RA": "Patient Advances", "RS":"Patient Settlements",
							"F":"Refunds", "S":"TPA/Sponsor Payments"}>
				<#assign ipSubtotalCount = 0>
				<#assign ipSubtotal = 0>

				<#list ["RA","RS", "F", "S"] as group>
					<#if (creditReceipts[group]??) >
						<#assign ipSubtotalCount = ipSubtotalCount + (creditReceipts[group].count!0)>
						<#assign ipSubtotal = ipSubtotal + (creditReceipts[group].sum!0)>
					</#if>
				</#list>
				<#assign empty = {}>

				<tr class="heading">
					<td>Bill Later - Total</td>
					<td class="number">${ipSubtotalCount?string("#")}</td>
					<td class="number">${ipSubtotal}</td>
				</tr>

				<#list ["RA","RS", "F", "S"] as group>
					<#if (creditReceipts[group]??) >
						<tr>
							<td>${creditGroupDisplay[group]}</td>
							<td class="number">${(creditReceipts[group].count!0)?string("#")}</td>
							<td class="number">${creditReceipts[group].sum!0}</td>
						</tr>
					</#if>
				</#list>

				<#-- Bill Now revenue/collection: expected to be same -->
				<tr class="heading">
					<td>Bill Now Revenue - Total</td>
					<td class="number">${(opTotalCount!0)?string("#")}</td>
					<td class="number">${opTotalAmount!0}</td>
				</tr>
				<#assign prepaidGroupDisplay = {"OPDOC": "OP Consultations ", "LTDIA":"Lab Tests",
							"RTDIA":"Radiology Tests", "PHMED":"Pharmacy", "O":"Others"}>
				<#list ["OPDOC","RTDIA", "LTDIA", "PHMED", "O"] as group>
					<#if ((prepaidCharges[group]!empty).activities!0) != 0 >
						<tr>
							<td>${prepaidGroupDisplay[group]}</td>
							<td class="number">${((prepaidCharges[group]!empty).activities!0)?string("#")}</td>
							<td class="number">${(prepaidCharges[group]!empty).amount!0}</td>
						</tr>
					</#if>
				</#list>

				<#if extraReceiptsTotalCount != 0>
					<tr class="heading">
						<td>Bill Now Extra Receipts - Total</td>
						<td class="number">${extraReceiptsTotalCount?string("#")}</td>
						<td class="number">${extraReceiptsTotalAmount}</td>
					</tr>
					<#if extraReceipts.count != 0 >
						<tr>
							<td>Receipts for bills excluded</td>
							<td class="number">${extraReceipts.count?string("#")}</td>
							<td class="number">${extraReceipts.amount}</td>
						</tr>
					</#if>
					<#if excessCollection.count != 0 >
						<tr>
							<td>Excess in bills included</td>
							<td class="number">${excessCollection.count?string("#")}</td>
							<td class="number">${excessCollection.amount}</td>
						</tr>
					</#if>
					<#if setOffs.count != 0 >
						<tr>
							<td>Less Deposit Set Offs</td>
							<td class="number">${setOffs.count?string("#")}</td>
							<td class="number">${0-setOffs.amount}</td>
						</tr>
					</#if>
				</#if>

				<#if deposits.count != 0>
					<tr class="heading">
						<td>Deposits - Total</td>
						<td class="number">${(deposits.count!0)?string("#")}</td>
						<td class="number">${deposits.total!0}</td>
					</tr>
					<#if ((depositReceipts.count!0)==0 && (depositReceipts.total!0)==0)>
					<#else>
					<tr>
						<td>Deposit Receipts</td>
						<td class="number">${(depositReceipts.count!0)?string("#")}</td>
						<td class="number">${depositReceipts.total!0}</td>
					</tr>
					</#if>
					<#if ((depositRefunds.count!0)==0 && (depositRefunds.total!0)==0)>
					<#else>
					<tr>
						<td>Deposit Refunds</td>
						<td class="number">${(depositRefunds.count!0)?string("#")}</td>
						<td class="number">${depositRefunds.total!0}</td>
					</tr>
					</#if>
				</#if>

				<#assign pTypeDisplay = {"D":"Doctor Payments", "P": "Presc Doctor Payments", "F":"Referral Payments",
						"O":"Outhouse Payments", "S":"Supplier Payments", "C":"Misc Payments"}>  <#-- " -->
				<tr class="heading">
					<td style="border-left:0px;border-right:0px;">Payments - Total</td>
					<td align="right" class="number total" style="border-left:0px;border-right:0px;">${(paymentCounts["_total"]!0)?string("#")}</td>
					<td class="number total" style="border-left:0px;border-right:0px;">${0-paymentAmounts["_total"]!0}</td>
				</tr>
				<#list ["D","P","F","O","S","C"] as ptype>
					<#if ((paymentAmounts[ptype]!0) == 0 && (paymentCounts[ptype]!0)==0)>
					<#else>
						<tr>
							<td align="left">${pTypeDisplay[ptype]}</td>
							<td align="right">${(paymentCounts[ptype]!0)?string("#")}</td>
							<td class="number">${0-paymentAmounts[ptype]!0}</td>
						</tr>
					</#if>
				</#list>

				<tr class="heading">
					<td>Grand Total</td>
					<td></td>
					<td class="number">${ipSubtotal + (opTotalAmount!0) +
								extraReceiptsTotalAmount + (deposits.total!0) - (paymentAmounts["_total"])!0}</td>
				</tr>
			</table>
		</td>

		<#-- section 2 : collection report payment mode vs. recpt type -->
		<td valign="top" style="padding-top: 1em">
			<table class="CFDreport" style="margin-left: 1em">
				<tr class="heading">
					<td>Cash Flow by Mode</td>
					<td>Receipts/<br /> Refunds</td>
					<td>Payments/<br /> Reversals</td>
					<td>Net</td>
				</tr>

				<#list displayModes as displayMode>
				<#assign mode = displayMode.payment_mode>
				<#assign modename = displayMode.mode_name>
			    <#if ((((collections[mode]!empty)["Receipts"]!0)==0)  && (((collections[mode]!empty)["Payments"]!0)==0) &&  (((collections[mode]!empty)["_total"]!0)==0))>
					<#else>
					<tr <#if mode == "_total">class="heading"</#if> >
						<td align="left" <#if mode != "_total">class="sub"</#if>>${modename}</td>
						<td class="number"> ${(collections[mode]!empty)["Receipts"]!0}</td>
						<td class="number">${(collections[mode]!empty)["Payments"]!0}</td>
						<td class="number">
							<#if mode=="_total">
								${(collections[mode]!empty)._total!0}
							<#else>
								${(collections[mode]!empty)._total!0}
							</#if>
						</td>
					</tr>
					</#if>
				</#list>
			</table>

			<table class="CFDreport" style="margin-left: 1em; margin-top:1em;" width="96%">
				<tr class="heading">
					<td>Expenses</td>
					<td></td>
				</tr>
				<tr class="heading">
					<td>Purchases - Total</td>
					<td class="number">${(storeExpenses._total)!0}</td>
				</tr>

				<#list storeNames as storeName>
					<#if (((storeExpenses[storeName])!0) != 0) >
						<tr>
							<td>${storeName!empty}</td>
							<td class="number">${(storeExpenses[storeName])!0}</td>
						</tr>
					</#if>
				</#list>

				<tr class="heading">
					<td>Doctor Payments - Total</td>
					<td class="number">${(pres_amt!0)+ (cond_amt!0) + (ref_amt!0)}</td>
				</tr>

				<#if (pres_amt!0) != 0>
					<tr>
						<td>Prescribing Doctor</td>
						<td class="number">${pres_amt!0}</td>
					</tr>
				</#if>

				<#if (cond_amt!0) != 0>
					<tr>
						<td>Conducting Doctor</td>
						<td class="number">${cond_amt!0}</td>
					</tr>
				</#if>
				<#if (ref_amt!0) != 0>
					<tr>
						<td>Referral</td>
						<td class="number">${ref_amt!0}</td>
					</tr>
				</#if>

				<#if outHouseExpenses?size &gt; 0>
					<tr class="heading">
						<td>Out House Payments - Total</td>
						<td class="number">${(outHouseExpenses._total)!0}</td>
					</tr>
					<#list outhouseNames as ohName>
						<tr class="heading">
							<td>${ohName!empty}</td>
							<td class="number">${(outHouseExpenses[ohName])!0}</td>
						</tr>
					</#list>
				</#if>

				<tr class="heading">
					<td>Miscellaneous Payments - Total</td>
					<td class="number">${misc_amt!0.0}</td>
				</tr>
				<tr class="heading">
					<td>Grand Total</td>
					<td class="number">${(pres_amt!0) + (cond_amt!0) + (ref_amt!0) + (misc_amt!0) + ((storeExpenses._total)!0)
							+ (outHouseExpenses._total)!0}</td>
				</tr>
			</table>
		</td>
	</tr>
</table>

<table class="CFDreport" style="font-size: <#if baseFontSize??>baseFontSize;<#else>10 px;</#if> margin-top: 1em; page-break-before: always;">
	<tr class="heading">
		<td>Revenue</td>
		<th align="center" colspan="2" style="padding-left: 2em">IP</th>
		<th align="center" colspan="2" style="padding-left: 2em">OP</th>
		<th align="center" colspan="2" style="padding-left: 2em">OSP</th>
		<th align="center" colspan="2" style="padding-left: 2em">Insurance</th>
		<td align="center">Total</td>
	</tr>

	<tr class="heading">
		<td></td>
		<td align="right">#</td>
		<td align="right">Amount</td>
		<td align="right">#</td>
		<td align="right">Amount</td>
		<td align="right">#</td>
		<td align="right">Amount</td>
		<td align="right">#</td>
		<td align="right">Amount</td>
		<td align="right">Amount</td>
	</tr>

	<#list groups as group>
		<#if (group != "_total") && (group != "Others")>
			<#list itemList[group] as item>
				<#if item == "_total"><#assign class="heading"><#else><#assign class=""></#if>
				<tr class="${class}">
					<#if item == "_total">
						<td align="left">${group} - Total</td>
					<#else>
						<td>
							<#if item != "_others">
								<#if item?length &gt; 50>${item?substring(0,50)}<#else>${item}</#if>
							<#else>
								Others
							</#if>
						</td>
					</#if>
					<td class="number">${(((counts[group]!empty)[item]!empty).i!0)?string("#")}</td>
					<td class="number">${((amounts[group]!empty)[item]!empty).i!0}</td>
					<td class="number">${(((counts[group]!empty)[item]!empty).o!0)?string("#")}</td>
					<td class="number">${((amounts[group]!empty)[item]!empty).o!0}</td>
					<td class="number">${(((counts[group]!empty)[item]!empty).r!0)?string("#")}</td>
					<td class="number">${((amounts[group]!empty)[item]!empty).r!0}</td>
					<td class="number">${(((counts[group]!empty)[item]!empty).s!0)?string("#")}</td>
					<td class="number">${((amounts[group]!empty)[item]!empty).s!0}</td>
					<td class="number">${((amounts[group]!empty)[item]!empty)._total!0}</td>
				</tr>
			</#list>
		</#if>
	</#list>

	<#-- show Others without item details at the end -->
	<tr class="heading">
		<td align="left" >Others - Total</td>
		<td class="number">${(((counts.Others!empty)._total!empty).i!0)?string("#")}</td>
		<td class="number">${((amounts.Others!empty)._total!empty).i!0}</td>
		<td class="number">${(((counts.Others!empty)._total!empty).o!0)?string("#")}</td>
		<td class="number">${((amounts.Others!empty)._total!empty).o!0}</td>
		<td class="number">${(((counts.Others!empty)._total!empty).r!0)?string("#")}</td>
		<td class="number">${((amounts.Others!empty)._total!empty).r!0}</td>
		<td class="number">${(((counts.Others!empty)._total!empty).s!0)?string("#")}</td>
		<td class="number">${((amounts.Others!empty)._total!empty).s!0}</td>
		<td class="number">${((amounts.Others!empty)._total!empty)._total!0}</td>
	</tr>

	<#-- show Grand Total at the end -->
	<tr class="heading">
		<td align="left">Grand Total</td>
		<td class="number" style="padding-left: 2em">${(((counts._total!empty)._total!empty).i!0)?string("#")}</td>
		<td class="number">${((amounts._total!empty)._total!empty).i!0}</td>
		<td class="number" style="padding-left: 2em">${(((counts._total!empty)._total!empty).o!0)?string("#")}</td>
		<td class="number">${((amounts._total!empty)._total!empty).o!0}</td>
		<td class="number" style="padding-left: 2em">${(((counts._total!empty)._total!empty).r!0)?string("#")}</td>
		<td class="number">${((amounts._total!empty)._total!empty).r!0}</td>
		<td class="number" style="padding-left: 2em">${(((counts._total!empty)._total!empty).s!0)?string("#")}</td>
		<td class="number">${((amounts._total!empty)._total!empty).s!0}</td>
		<td class="number" style="padding-left: 2em">${((amounts._total!empty)._total!empty)._total!0}</td>
	</tr>
</table>

<#assign help= (cpath!) + "/CollectionsDashboardHelp.jsp">

<#if format == "screen">
	<div align="center" style="margin-top: 2em">
		<form method="GET" action="" target="_blank">
			<input type="submit" value="Print"/>
			<#assign billUrl=(cpath!) + "/billing/RevenueReportPopup.do?method=getHelpPage">
			<input type="hidden" name="uri" value="${billUrl}"/>
			<input type="button" value="Help" onclick="popHelp(document.forms[0].uri.value);"/>
			<input type="hidden" name="method" value="dashboardReport"/>
			<input type="hidden" name="format" value="pdf"/>
			<input type="hidden" name="fromDate" value="${fromDate}"/>
			<input type="hidden" name="toDate" value="${toDate}"/>
			<input type="hidden" name="dateField" value="${dateField}"/>
			<input type="hidden" name="accountGroup" value="${accountGroup!}"/>
			<input type="hidden" name="centerFilter" value="${centerFilter!}"/>
		</form>
	</div>
</#if>

</body>
</#escape>

</html>

