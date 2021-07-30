<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>Patient Test Trend Report - Insta HMS</title>

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
[#setting number_format="number"]
[#setting date_format="dd-MM-yyyy"]
[#setting datetime_format="dd-MM-yyyy HH:mm"]

<h1>PATIENT TEST TREND REPORT</h1>
<div class="patientHeader" style="margin-bottom: 1em">
	<table  width="50%" cellspacing="0" cellpadding="0">
		<tr>
			<td>Name:</td>
			<td><b>${patient.salutation!} ${patient.patient_name!} ${patient.last_name!}</b></td>
		</tr>
		<tr>
			<td>MR No:</td>
			<td><b>${patient.mr_no}</b></td>
		</tr>
		<tr>
			<td>Age/Gender:</td>
			<td><b>${patient.age!?string("#")} ${patient.agein!} / ${patient.patient_gender!}</b></td>
		</tr>
	</table>
</div>

	[#if resultLabels?has_content]
		<table border='0' cellspacing='0' cellpadding='0' width='100%'>
			<tr> <th> </th>
				[#list resultDates as rdate]
					<th style="padding-right:2px;">${rdate}</th>
				[/#list]
			</tr>
			[#list resultLabels as rlabel]
				<tr>
					<th>${rlabel}</th>
					[#list resultDates as rdate]
							<td>[#if testValuesMap[rlabel][rdate]?exists]
								${testValuesMap[rlabel][rdate]["report_value"]} ${testValuesMap[rlabel][rdate]["units"]}
								[/#if]
							</td>
					[/#list]
				</tr>
			[/#list]
		</table>
	[#else]
		<p><b>No Data Found</b></p>
	[/#if]

</body>
[/#escape]
</html>
