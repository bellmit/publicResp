<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>Patient Reward Points Ledger - Insta HMS</title>
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

<#assign patientDetailsQuery = "SELECT * FROM patient_details_display_view WHERE mr_no= '${mr_no}' ">
<#assign rpl = queryToDynaBean(patientDetailsQuery).getMap()>

<#assign rewardPointsListQuery =
	"SELECT date as txn_date, bill_no,
	  CASE WHEN entry_type = 'E' THEN 'Earned' WHEN entry_type = 'M' THEN 'Manual' ELSE 'Reversed' END as type,
	  eligible_value, points as earned, 0 as redeemed, 0 as redeemed_value
	 FROM reward_points_earnings e
	 WHERE mr_no = '${rpl.mr_no}'
	UNION ALL
	SELECT b.finalized_date as txn_date, b.bill_no, 'Redeemed' AS type,
	  0 as eligible_value, 0 as earned, points_redeemed as redeemed, points_redeemed_amt as redeemed_value
	 FROM bill b
	  JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
	 WHERE b.status in ('F','C') and points_redeemed != 0
	  AND pr.mr_no = '${rpl.mr_no}'
	ORDER BY txn_date ">
<#assign rewardPointsList = queryToDynaList(rewardPointsListQuery)>

<#setting datetime_format="dd-MM-yyyy">
<#escape x as x?html>
<body>
	<div align="center">
		<span style="font-size: 12pt;font-weight: bold;margin-top:10px;"> Reward Points Ledger </span>
	</div>
	<div class="patientHeader" style="margin-bottom: 1em">
		<table width="100%"  >
			<tr>
				<td><b>MR No:</b> ${rpl.mr_no}</td>
				<td align="right"><b>Name:</b> ${rpl.full_name}</td>
			</tr>
			<tr>
				<td><b>Age/Gender</b>:
					<#if rpl.agein == 'Y'>
						${(rpl.age!0)?string("#")} Years / ${rpl.patient_gender}
					<#elseif rpl.agein == 'M'>
						${(rpl.age!0)?string("#")} Months / ${rpl.patient_gender}
					<#else>
						${(rpl.age!0)?string("#")} Days / ${rpl.patient_gender}
					</#if>
				</td>
				<td align="right"><b>Contact No:</b> ${rpl.patient_phone!}</td>
			</tr>
		</table>
	</div>
	<table width="100%" class="report" >
		<tr>
			<th>
				Bill No.
			</th>
			<th>
				Txn Date
			</th>
			<th>
				Type
			</th>
			<th style="text-align:right">
				Eligible Value
			</th>
			<th style="text-align:right">
				Earned
			</th>
			<th style="text-align:right">
			    Redeemed
			</th>
			<th style="text-align:right">
				Redeemed Value
			</th>
			<th style="text-align:right">
				Balance
			</th>
		</tr>
		<#assign eligibleValueSum=0>
		<#assign earnedSum=0>
		<#assign redeemedSum=0>
		<#assign redeemedValueSum=0>
		<#assign balanceSum=0>

		<#list rewardPointsList as reward>
			<tr>
				<td style="text-align: left;">
					${reward.bill_no}
				</td>
				<td style="text-align: left;">
					${reward.txn_date}
				</td>
				<td style="text-align: left;">
					${reward.type}
				</td>
				<td  style="align:right;">
					${reward.eligible_value}
					<#assign eligibleValueSum= eligibleValueSum+reward.eligible_value>
				</td>
				<td  style="align:right;">
					${reward.earned}
					<#assign earnedSum= earnedSum+reward.earned>
				</td>
				<td  style="align:right;">
					${reward.redeemed}
					<#assign redeemedSum= redeemedSum+reward.redeemed>
				</td>
				<td  style="align:right;">
					${reward.redeemed_value}
					<#assign redeemedValueSum= redeemedValueSum+reward.redeemed_value>
				</td>
				<td  style="align:right;">
					<#assign balance=reward.earned-reward.redeemed>
					<#assign balanceSum=balanceSum+balance>
					${balanceSum}
				</td>
			</tr>
		</#list>
		<tr>
			<td colspan="3">
				<b>Total</b>
			</td>
			<td  style="align:right;">
				<b>${eligibleValueSum!0}</b>
			</td>
			<td  style="align:right;">
				<b>${earnedSum!0}</b>
			</td>
			<td  style="align:right;">
				<b>${redeemedSum!0}</b>
			</td>
			<td  style="align:right;">
				<b>${redeemedValueSum!0}</b>
			</td>
			<td  style="align:right;">
				<b>${balanceSum!0}</b>
			</td>
		</tr>
	</table>
</body>
</#escape>
</html>
