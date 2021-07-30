<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>Intake Output Parameter</title>
</head>
<body>
	[#escape x as x?html]
	<div align="center" style="font-size: 12pt;"><b>[#if "${paramType}" == "V"]Vital Details[#else]Intake/Output Details[/#if]</b></div>
	<div class="patientHeader">
	  	[#include "VisitDetailsHeader.ftl"]
	</div>
	<br/>
	<div>
		<table width='100%' style="border-collapse: collapse;border: 1px solid; margin-top: 10px">
			<tr>
				<th style="border: 1px solid;" align="center" width="25%">Date</th>
				<th style="border: 1px solid;" align="center" width="25%">Intake/Output</th>
				<th style="border: 1px solid;" align="center" width="25%">Values</th>
				<th style="border: 1px solid;" align="center" width="25%">User</th>
			</tr>
			[#list vitalReadingGroupedMap?values as readings]
			[#list readings as reading]
			<tr>
				[#if reading?is_first]
				<td style="border: 1px solid;" align="center" rowspan="${readings?size}">${reading['date_time']?string("dd-MM-yyyy HH:mm")}</td>
				[/#if]
				<td style="border: 1px solid;">${reading['param_label']!""}</td>
				<td style="border: 1px solid;">${reading['param_value']!""}</td>
				[#if reading?is_first]
				<td style="border: 1px solid;" align="center" rowspan="${readings?size}">${reading['user_name']!""}</td>
				[/#if]
			</tr>
			[/#list]
			[/#list]
		</table>
	</div>
	[/#escape]
</body>
</html>