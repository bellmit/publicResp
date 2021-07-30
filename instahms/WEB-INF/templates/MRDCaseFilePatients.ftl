[#setting date_format="dd-MM-yyyy"]
[#setting datetime_format="dd-MM-yyyy hh:mm"]
[#setting number_format="####"]
[#assign owner= {"A":"MRD","L":"Lost","I":"Inactive"}]
<html>
	<head>
		<style type="text/css">
			body {font-family: Arial; font-size: 12pt;}
		</style>
	</head>
	<body>
		<table align="center">
			<tr>
				<td align="center"><b>Case file List</b></td>
			</tr>
		</table>
		<table style="margin-top: 10px" width="100%">
			<tbody>
				<tr>
					<th>#</th>
					<th>Hosp No.</th>
					<th>Patient Name</th>
					<th>Requesting Dept.</th>
				</tr>
				[#list yearMapkey as k]
					[#assign index=1]
					<tr>
						<td colspan="6"><u>Year: ${k}</u></td>
					</tr>
					[#list patientsList as patient]
						[#if k == patient.created_date]

						<tr>
							<td width="30px">${index}</td>
							<td width="80px">${patient.mr_no}  </td>
							<td width="300px">${patient.patient_full_name}</td>
							<td width="250px">${(patient.requesting_dept!)?html}</td>
						</tr>
						[#assign index = index+1]
						[/#if]
					[/#list]
				[/#list]
			</tbody>
		</table>

	</body>
</html>
