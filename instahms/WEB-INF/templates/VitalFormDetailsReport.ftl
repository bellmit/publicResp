<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Vital Parameter</title>

</head>
<body>
<!-- [#escape x as x?html] -->
<!--[#if "${paramType}" == "V"]-->
	<div align="center" style="font-size: 12pt;"><b>Vital Details </b></div>
<!--[#else]-->
	<div align="center" style="font-size: 12pt;"><b>Intake/Output Details </b></div>
<!--[/#if]-->
<div class="patientHeader">
  	[#include "VisitDetailsHeader.ftl"]
</div>
<br/><br/>
<div>

<!-- [#assign v = 0] -->
<!-- [#assign i = 0] -->
<!-- [#assign o = 0] -->

	<!-- [#list VitalMasterList as master] -->
		<!--[#if "${master.map.param_container}" == "V"]-->
			<!-- [#assign v = v+1] -->
		<!--[#elseif "${master.map.param_container}" == "I"]-->
			<!-- [#assign i = i+1] -->
		<!--[#elseif "${master.map.param_container}" == "O"]-->
			<!-- [#assign o = o+1] -->
		<!--[/#if]-->
	<!-- [/#list] -->

	<!-- [#assign vitalVar={"V":"Vitals", "I":"Intake", "O":"Output"}] -->
	<!-- [#assign keys=vitalVar?keys] -->
	<!-- [#list keys as key]-->
		<!-- [#if (key == 'V' && v>0) || (key == 'I' && i>0) || (key == 'O' && o>0)] -->
		<!--[#if key == 'V']
				[#assign colspan=v]
			[#elseif key == 'I']
				[#assign colspan=i]
			[#elseif key == 'O']
				[#assign colspan=o]
			[/#if] -->

			<table width='100%' style="border-collapse: collapse;border: 1px solid; margin-top: 10px">
				<tr>
					<th style="border: 1px solid;" align="center" rowspan="2" width="20%">Date</th>
					<th style="border: 1px solid;" colspan="${colspan}" align="center">${vitalVar[key]}</th>
					<th style="border: 1px solid;" align="center" rowspan="2">User</th>
				</tr>
				<tr>
					<!-- [#list VitalMasterList as master] -->
						<!-- [#if master.param_container == key] -->
							<th style="border: 1px solid;">${master.map.param_label}
								<!--[#if (master.map.param_uom??) && "${master.map.param_uom}" != ""]-->
								(${master.map.param_uom})
								<!--[/#if]-->
							</th>
						<!-- [/#if] -->
					<!-- [/#list] -->
				</tr>
				<!-- [#list VitalReadingList as reading] -->
				<tr>
					<td style="border: 1px solid;">${reading['date_time']?string("dd-MM-yyyy HH:mm")}</td>
					<!-- [#assign index=0] -->
					<!-- [#list VitalMasterList as master] -->
						<!-- [#if master.param_container == key] -->
								<!--	[#if index>0 && (index%colspan == colspan)] -->
										</tr><tr><td></td>
								<!-- 	[/#if] -->
								<!-- [#assign label = "${master.map.param_label}"] -->
								<td style="border: 1px solid;">${reading[label]!""}</td>
								<!-- [#assign label = ""]  -->
								<!-- [#assign index = index+1] -->
						<!-- [/#if] -->
					<!-- [/#list]-->
					<td style="border: 1px solid;">${reading['user']!""}</td>
				</tr>
				<!-- [/#list]-->
			</table>
<!--	[/#if] -->
<!--	[/#list] -->
<!-- [/#escape] -->
</div>
</body>
</html>