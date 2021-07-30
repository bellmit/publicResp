<!-- [#setting number_format="#"] -->

<!-- [#assign conductionFormat = ""] -->
<!-- [#list testDetailKeys as key] -->
		<h3>${testDetailsGroupMap[key][0].test_name!}</h3>
	<!-- [#assign conductionFormat = testDetailsGroupMap[key][0].conduction_format] -->
	<!-- [#if conductionFormat = 'V'] -->
			<table>
				<tbody>
					<tr>
						<th>Result</th>
						<th>Value</th>
						<th>Units</th>
						<th>Reference Range</th>
					</tr>	
					<!-- [#list testDetailsGroupMap[key] as testDetails] -->
						<!-- [#if testDetails?has_content] -->
							<tr>
								<td>${testDetails.resultlabel}</td>
								<td>${testDetails.report_value}
									<!--[#if testDetails.withinnormal != 'Y'] -->
										${testDetails.withinnormal}
									<!--[/#if]-->	
								</td>
								<td>${testDetails.units}</td>
								<td>${testDetails.reference_range}</td>																		
							</tr>
						<!-- [/#if] -->
					<!-- [/#list] -->		
				</tbody>
			</table>	
	<!-- [#elseif conductionFormat = 'T'] -->
			${testDetailsGroupMap[key][0].patient_report_file!}	
	<!-- [/#if] -->		
<!-- [/#list] -->
