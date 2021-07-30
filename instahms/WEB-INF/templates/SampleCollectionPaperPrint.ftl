<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
	</head>
[#escape x as x?html]
<body>
	[#setting number_format="#"]
	[#setting datetime_format = "dd-MM-yyyy HH:mm"]
	<table width='100%'>
		[#if sampleDetails??]

			[#list sampleDetails as sd]

				<tr>
					[#if sd.mr_no??]
						<td>${sd.mr_no!''}</td>
					[#else]
						<td>${sd.patient_id!''}</td>
					[/#if]
				</tr>
				<tr>
					<td>${sd.sample_sno!''}</td>
				</tr>
				<tr>
					<td>${sd.sample_type!''}</td>
				</tr>
				<tr>
					<td>${sd.test_name!''}</td>
				</tr>
				<tr>
					<td>${sd.sample_date!''}</td>
				</tr>

			[/#list]

		[/#if]
	</table>
</body>
[/#escape]
</html>