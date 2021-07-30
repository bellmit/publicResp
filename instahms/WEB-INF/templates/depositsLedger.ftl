<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>Deposit Statement - Insta HMS</title>
	<style>
		@page {
			size: 595pt 842pt;
			margin: 36pt 36pt 36pt 36pt;
		}
			body {
		font-family: Arial, sans-serif;
	}
	table.report {
		empty-cells: show;
		font-size: 9pt;
		border-collapse: collapse;
		border: 1px solid black;
	}

	table.report th {
		border: 1px solid black;
		padding: 2px 8px 2px 3px;
	}

	table.report td {
		padding: 2px 4px 2px 4px;
		border: 1px solid black;
	}

	table.report td.heading {
		font-weight: bold;
	}
	table.report th {text-align: left}
	table.report td {text-align: right}
	</style>
</head>
<#assign depositListQuery = "SELECT * FROM deposit_ledger_view  WHERE mr_no = '${dl.mr_no}' ORDER BY deposit_date, debit">
<#assign depositList = queryToDynaList(depositListQuery)>

<#setting datetime_format="dd-MM-yyyy">
<#escape x as x?html>
<body>
	<div align="center">
		<span style="font-size: 12pt;font-weight: bold;margin-top:10px;"> Deposit Statement </span>
	</div>
	<div class="patientHeader" style="margin-bottom: 1em">
		<table width="100%"  >
			<tr>
				<td><b>MR No:</b> ${dl.mr_no}</td>
				<td align="right"><b>Name:</b> ${dl.patient_name}</td>
			</tr>
			<tr>
				<td><b>Age/Gender</b>:
					<#if dl.agein == 'Y'>
						${(dl.age!0)?string("#")} Years / ${dl.patient_gender}
					<#elseif dl.agein == 'M'>
						${(dl.age!0)?string("#")} Months / ${dl.patient_gender}
					<#else>
						${(dl.age!0)?string("#")} Days / ${dl.patient_gender}
					</#if>
				</td>
				<td align="right"><b>Contact No:</b> ${dl.patient_phone!}</td>
			</tr>
		</table>
	</div>
	<table width="100%" class="report" >
		<tr>
			<th>
				Bill/Deposit No.
			</th>
			<th>
				Deposit Date
			</th>
			<th>
				Payment Mode
			</th>
			<th>
				Credit
			</th>
			<th>
				Debit
			</th>
			<th>
			    Balance
			</th>
			<th>
				Deposit Type
			</th>
		</tr>
		<#assign balanceSum=0>
		<#assign creditSum=0>
		<#assign debitSum=0>

		<#list depositList as deposit>
			<tr>
				<td style="text-align: left;">
					${deposit.deposit_no}
				</td>
				<td style="text-align: left;">
					${deposit.deposit_date}
				</td>
				<td style="text-align: left;">
					${deposit.payment_mode}
				</td>
				<td  style="align:right;">
					${deposit.credit}
					<#assign creditSum= creditSum+deposit.credit>
				</td>
				<td  style="align:right;">
					${deposit.debit}
					<#assign debitSum= debitSum+deposit.debit>
				</td>
				<td  style="align:right;">
					<#assign balanceSum=balanceSum+deposit.balance>
					${balanceSum}
				</td>
				<td style="text-align: left;">
					<#if deposit.deposit_available_for == 'I'>
						IP Deposit
					<#elseif deposit.deposit_available_for == 'B'>
						General
					<#else>
					</#if>
				</td>
			</tr>
		</#list>
		<tr>
			<td colspan="3">
				<b>Total</b>
			</td>
			<td  style="align:right;">
				<b>${creditSum!0}</b>
			</td>
			<td  style="align:right;">
				<b>${debitSum!0}</b>
			</td>
			<td  style="align:right;">
				<b>${balanceSum!0}</b>
			</td>
		</tr>
	</table>
</body>
</#escape>
</html>
