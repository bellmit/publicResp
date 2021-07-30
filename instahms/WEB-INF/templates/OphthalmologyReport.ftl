<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>Ophthalmology-Insta-HMS</title>

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
	[#assign sno = 0]
	<body>
			<div align="center"><h1>Ophthalmology Report</h1></div>
			<div class="patientHeader" style="margin-bottom: 1em">
				<table  width="65%" cellspacing="0" cellpadding="0">
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
					<tr>
						<td>Virology Status:</td>
						<td><b>${patient.custom_field1!}</b></td>
					</tr>
				</table>
			</div>

			<table width="100%" >
				<tr>
					<td><h2>Test Name</h2></td>
				</tr>

				[#list eyeList as eyeList]
					<tr><td><b>${eyeList.test_name!}:</b></td></tr>
					[#list records as record]
						[#if eyeList.test_id == record.test_id]
							[#if sno %2 == 0]
								<tr >
							[/#if]
						[#assign sno = sno +1]
								<td style="padding-bottom:13px; padding-left: 5px"></td>
								<td style="padding-bottom:13px">${record.attribute_name!}</td>
								<td style="padding-bottom:13px">${record.test_values!}</td>
							[#if sno % 2 == 0]
								</tr>
							[/#if]
						[/#if]
					[/#list]
				[/#list]
		</table>
	</body>
</html>
