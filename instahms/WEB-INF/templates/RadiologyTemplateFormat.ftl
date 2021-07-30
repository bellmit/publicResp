<!--
[#setting number_format = "##"]
[#assign hasAbnormalResults = false]
-->
${report_addendum!}
<h2 style='text-align: center;'>Radiology Report</h2>

<!--Section 2: Patient Details  -->
<div class="patientHeader">
	<table cellspacing='0' cellpadding='2' width='100%'>
		 <tbody>
		<!--[#if visitDetails??]-->
			<tr>
				<td width='15%'>Prescribed Doctor:</td>
				<td width='50%'>${prescribing_doctor}</td>
			</tr>
		   <!--[#if (singleTestDate && singleLabNo) && (reportTestDate !='' || reportLabNo !='')]-->
				<tr>
					<td width='15%'>Test Date: </td>
					<td width='50%'>${reportTestDate}</td>
					<td width='20%'>${idNoLabel}</td>
					<td width='15%'>${reportLabNo}</td>
				</tr>
		  <!--[/#if]-->
		  <!--[#if (singleSpecimenType && singleSampleDate && !isRadiology ) && (reportSampleDate !='' || reportSpecimenType !='')] -->
			   <tr>
					<td width='15%'>Sample Date:</td>
					<td width='15%'>${reportSampleDate}</td>
					<td width="20%">Specimen</td>
					<td width="15%">${reportSpecimenType?html}</td>
			   </tr>
		  <!--[/#if]-->
		   <!--[#if (incoming) ] -->
			  <tr>
			  	<td width='20%'>Incoming Hospital:</td>
			  	<td width='20%'>${incoming_hosp}</td>
			  	<td width='15%'>Referal Doctor:</td>
			  	<td width='15%'>${referal}</td>
			  </tr>
		  <!-- [/#if]
		  [/#if]-->
		  </tbody>
	</table>
</div>

<!--Section 3: Test Details  -->

<!-- [#list depts as d] -->
	 <h2 style='text-align: center'>${d.deptName?html}</h2>
	 <table valign='top' cellpadding='2' cellspacing='0' border='0' width='100%'>
	 <tbody>
	<!-- [#if (d.valueTests??) && (d.valueTests?size>0) ] -->
			<tr style='border-bottom: 1px solid black;'>
				<!--[#if (d.hasRemarks == true)]
						[#assign width = {"Test Description":"30%","Result":"10%","Methodology":"10%","Units":"10%","Reference Range":"30%","Remarks":"20%"}]
				 		[#assign keys = width?keys]
				  	[#else]
						[#assign width = {"Test Description":"40%","Result":"10%","Methodology":"10%","Units":"10%","Reference Range":"40%"}]
						[#assign keys = width?keys]
			 	 	[/#if]-->
			 	<!--[#list keys as head] -->
					<th align='left' width='${width[head]}'>${head}</th>
				<!--[/#list]-->
			</tr>
			<!--[#list d.valueTests as  test]
	            	[#assign singleValueTest = false]
					[#if test.results?size == 1]
						[#if test.results[0].resultlabel == test.testName]
							[#assign singleValueTest = true]
						[/#if]
					[/#if]-->
					<tr>
						<td colspan="5">&nbsp;</td>
					</tr>
					<tr>
						<td colspan='5'>
				 			<table width='100%' cellspacing='0' cellpadding='0'>
							   <tbody>
							        <!--[#if !singleValueTest]
									       <tr><td colspan='2'><b>${test.testName}</b></td></tr>
									    [/#if] -->
									<!--
										[#if !(singleSpecimenType && singleSampleDate)
					 						  &&  (test.specimenType?? && test.specimenType != '')
											  && (test.sampleDate??)] -->
											<tr>
											    <td align='left'> Specimen:${test.specimenType?html}</td>
												<td align='right'>Sample Date:${test.sampleDate?string.short}</td>
											</tr>
									<!--[/#if] -->
									<!--[#if !(singleLabNo && singleTestDate)
										     &&  ((test.labNumber?? && test.labNumber !='')
										        ||test.testDate??)]-->
											<tr>
												<td align='left'>
											<!-- [#if (test.labNumber??) && (test.labNumber !='') ] -->
														${idNoLabel} ${test.labNumber}
											<!-- [/#if] -->
												</td>
												<td align='right'>
											<!-- [#if test.testDate??] -->
														Test Date: ${test.testDate?string.short}
											<!--[/#if] -->
												</td>
											</tr>
									<!--[/#if]-->
								</tbody>
							</table>
			 			</td>
			 		</tr>
					<!--[#list test.results as result]
							[#if (result.report_value??) && (result.report_value != '')] -->
								<tr>
									<td valign='top'>
									   <!--[#if singleValueTest ]--><p style="font-weight: bold;"><!--[/#if] -->
									    ${result.resultlabel?html}
									   <!--[#if singleValueTest ]--></p><!--[/#if] -->
									 </td>
									<td valign='top'>
								 <!--[#if ((result.withinnormal != '*') && (result.withinnormal != '#')) ] -->
								          ${result.report_value?html}
								 <!--[/#if]-->
								 <!--[#if ((result.withinnormal != 'Y') && (result.withinnormal != '*') && (result.withinnormal != '#')) ]
									      [#assign hasAbnormalResults = true ] -->
									      ${result.withinnormal}
								 <!--[/#if]-->
								 <!--[#if ((result.withinnormal != 'Y') && (result.withinnormal == '*' || result.withinnormal == '#')) ]
									      [#assign hasAbnormalResults = true ] -->
									        <b>${result.report_value?html}
								     	    ${result.withinnormal}</b>
								 <!--[/#if]-->
									</td>
									 <td valign='top'>
									    ${result.method_name!}
									 </td>
									<td valign='top'>
										${result.units?html}
									</td>
									<td>
										${result.reference_range?html}
									</td>
									<td  valign='top'>
										${result.comments?html}
									</td>
								</tr>
					<!--
							[/#if]
					[/#list]
					[#if (test.testRemarks??) && (test.testRemarks != '')] -->
						<tr><td colspan='5'>Note:&nbsp;${test.testRemarks?html}</td></tr>
					<!--[/#if] -->
					<!-- [#if !singleTechnician && test.techSignatureName?has_content] -->
						<tr><td colspan="4"></td><td style="text-align: left"><img src="UserImage.do?_method=view&amp;user_name=${test.techSignatureName}"/></td></tr>
						<tr><td colspan="4"></td><td style="text-align: left">${test.techSignatureName}</td></tr>
					<!-- [/#if]-->
					<!-- [#if !singleCondDoctor && report.signed_off == 'Y' && test.condDoctorId?has_content] -->
						<tr><td colspan="4"></td><td style="text-align: left"><img src="UserImage.do?_method=view&amp;doctor_id=${test.condDoctorId}"/></td></tr>
						<tr><td colspan="4"></td><td style="text-align: left">${test.condDoctorName}</td></tr>
					<!-- [/#if]-->
		<!-- [/#list]-->
		<!--[/#if] -->
		<!--end of the value list -->
		<!--
		[#if d.reportTests??]
			[#list d.reportTests as format]
				[#if !isRadiology] -->
				<tr>
					<td colspan='5'>
						<table width='100%' cellspacing='0' cellpadding='0'>
						<tbody>
							<tr>
								<td colspan='2'><b>${format.testName?html}</b></td>
							</tr>
							<!--
							[#if !(singleSpecimenType && singleSampleDate)
								 &&  (format.specimenType?? && format.specimenType != '')
								 && (format.sampleDate??)] -->
								<tr>
								    <td align='left'> Specimen:${format.specimenType?html}</td>
									<td align='right'>Sample Date:${format.sampleDate?string.short}</td>
								</tr>
							<!--
							[/#if] -->
							<!--
							[#if !(singleLabNo && singleTestDate)
							     	&& (format.labNumber != '-' || format.testDate??)] -->
								<tr>
									<td align='left'>
								<!--	[#if format.labNumber != '-' ] -->
											${idNoLabel} ${format.labNumber}
								<!--	[/#if] -->
									</td>
									<td align='right'>
								<!--	[#if format.testDate??] -->
											Test Date: ${format.testDate?string.short}
								<!--	[/#if] -->
									</td>
							   </tr>
							<!--
						   	[/#if]-->
						</tbody>
						</table>
				 	</td>
				</tr>
				<!--
				[/#if]-->
				<tr>
					<td colspan='5'>
						<div style='margin-top: 10px;'>
						  <!--[#if (format.result.patient_report_file??)]-->
								${format.result.patient_report_file}
						   <!--[/#if]-->
						</div>
					</td>
				</tr>
				<!-- [#if (format.testRemarks??) && (format.testRemarks != '')] -->
					<tr><td colspan='5'>Note:&nbsp;${format.testRemarks?html}</td></tr>
				<!--[/#if] -->
				<!-- [#if !singleTechnician && format.techSignatureName?has_content] -->
					<tr><td colspan="4"></td><td style="text-align: left"><img src="UserImage.do?_method=view&amp;user_name=${format.techSignatureName}"/></td></tr>
					<tr><td colspan="4"></td><td style="text-align: left">${format.techSignatureName}</td></tr>
				<!-- [/#if]-->
				<!-- [#if !singleCondDoctor && report.signed_off == 'Y' && format.condDoctorId?has_content] -->
					<tr><td colspan="4"></td><td style="text-align: left"><img src="UserImage.do?_method=view&amp;doctor_id=${format.condDoctorId}"/></td></tr>
					<tr><td colspan="4"></td><td style="text-align: left">${format.condDoctorName}</td></tr>
				<!-- [/#if]-->
			<!--
			[/#list]
		[/#if]
         -->
	</tbody>
	</table>
 <!--
[/#list] -->
 <!--end of dept list -->

<!-- [#if (amendedDepts??) && (amendedDepts?size>0)] -->
<h3 style='text-align: center'>Amendments</h3>
<div>
	<table cellspacing='0' cellpadding='2' width='100%'>
		 <tbody>
		<!--[#if visitDetails??]-->
			<tr>
				<td width='15%'>Prescribed Doctor:</td>
				<td width='50%'>${prescribing_doctor}</td>
			</tr>
		   <!--[#if (singleTestDate && singleLabNo) && (reportTestDate !='' || reportLabNo !='')]-->
				<tr>
					<td width='15%'>Test Date: </td>
					<td width='50%'>${reportTestDate}</td>
					<td width='20%'>${idNoLabel}</td>
					<td width='15%'>${reportLabNo}</td>
				</tr>
		  <!--[/#if]-->
		  <!--[#if (singleSpecimenType && singleSampleDate && !isRadiology ) && (reportSampleDate !='' || reportSpecimenType !='')] -->
			   <tr>
					<td width='15%'>Sample Date:</td>
					<td width='15%'>${reportSampleDate}</td>
					<td width="20%">Specimen</td>
					<td width="15%">${reportSpecimenType?html}</td>
			   </tr>
		  <!--[/#if]-->
		   <!--[#if (incoming) ] -->
			  <tr>
			  	<td width='20%'>Incoming Hospital:</td>
			  	<td width='20%'>${incoming_hosp}</td>
			  	<td width='15%'>Referal Doctor:</td>
			  	<td width='15%'>${referal}</td>
			  </tr>
		  <!-- [/#if]
		  [/#if]-->
		  </tbody>
	</table>
</div>
<!-- [/#if] -->

<!-- [#list amendedDepts as d] -->
	 <h2 style='text-align: center'>${d.deptName?html}</h2>
	 <table valign='top' cellpadding='2' cellspacing='0' border='0' width='100%'>
	 <tbody>
	<!-- [#if (d.valueTests??) && (d.valueTests?size>0) ] -->
			<tr style='border-bottom: 1px solid black;'>
				<!--[#if (d.hasRemarks == true)]
						[#assign width = {"Test Description":"30%","Result":"10%","Methodology":"10%","Units":"10%","Reference Range":"30%","Remarks":"20%"}]
				 		[#assign keys = width?keys]
				  	[#else]
						[#assign width = {"Test Description":"40%","Result":"10%","Methodology":"10%","Units":"10%","Reference Range":"40%"}]
						[#assign keys = width?keys]
			 	 	[/#if]-->
			 	<!--[#list keys as head] -->
					<th align='left' width='${width[head]}'>${head}</th>
				<!--[/#list]-->
			</tr>
			<!--[#list d.valueTests as  test]
	            	[#assign singleValueTest = false]
					[#if test.results?size == 1]
						[#if test.results[0].resultlabel == test.testName]
							[#assign singleValueTest = true]
						[/#if]
					[/#if]-->
					<tr>
						<td colspan="5">&nbsp;</td>
					</tr>
					<tr>
						<td colspan='5'>
				 			<table width='100%' cellspacing='0' cellpadding='0'>
							   <tbody>
							        <!--[#if !singleValueTest]
									       <tr><td colspan='2'><b>${test.testName}</b></td></tr>
									    [/#if] -->
									<!--
										[#if !(singleSpecimenType && singleSampleDate)
					 						  &&  (test.specimenType?? && test.specimenType != '')
											  && (test.sampleDate??)] -->
											<tr>
											    <td align='left'> Specimen:${test.specimenType?html}</td>
												<td align='right'>Sample Date:${test.sampleDate?string.short}</td>
											</tr>
									<!--[/#if] -->
									<!--[#if !(singleLabNo && singleTestDate)
										     &&  ((test.labNumber?? && test.labNumber !='')
										        ||test.testDate??)]-->
											<tr>
												<td align='left'>
											<!-- [#if (test.labNumber??) && (test.labNumber !='') ] -->
														${idNoLabel} ${test.labNumber}
											<!-- [/#if] -->
												</td>
												<td align='right'>
											<!-- [#if test.testDate??] -->
														Test Date: ${test.testDate?string.short}
											<!--[/#if] -->
												</td>
											</tr>
									<!--[/#if]-->
								</tbody>
							</table>
			 			</td>
			 		</tr>
					<!--[#list test.results as result]
							[#if (result.report_value??) && (result.report_value != '')] -->
								<tr>
									<td valign='top'>
									    <strike>${result.resultlabel?html}</strike>
									 </td>
									<td valign='top'>
								 <!--[#if ((result.withinnormal != '*') && (result.withinnormal != '#')) ] -->
								         <strike> ${result.report_value?html}</strike>
								 <!--[/#if]-->
								 <!--[#if ((result.withinnormal != 'Y') && (result.withinnormal != '*') && (result.withinnormal != '#')) ]
									      [#assign hasAbnormalResults = true ] -->
									     <strike> ${result.withinnormal}</strike>
								 <!--[/#if]-->
								 <!--[#if ((result.withinnormal != 'Y') && (result.withinnormal == '*' || result.withinnormal == '#')) ]
									      [#assign hasAbnormalResults = true ] -->
									        <strike>${result.report_value?html}
								     	    ${result.withinnormal}</strike>
								 <!--[/#if]-->
									</td>
									<td valign='top'>
									    <strike>${result.method_name!}</strike>
									 </td>
									<td valign='top'>
										<strike>${result.units?html}</strike>
									</td>
									<td>
										<strike>${result.reference_range?html}</strike>
									</td>
									<td  valign='top'>
										<strike>${result.comments?html}</strike>
									</td>
								</tr>
					<!--
							[/#if]
					[/#list]
					[#if (test.testRemarks??) && (test.testRemarks != '')] -->
						<tr><td colspan='5'>Note:&nbsp;${test.testRemarks?html}</td></tr>
					<!--[/#if]
		 	[/#list]-->
		<!--[/#if] -->
		<!--end of the value list -->
		<!--
		[#if d.reportTests??]
			[#list d.reportTests as format]
				[#if !isRadiology] -->
				<tr>
					<td colspan='5'>
						<table width='100%' cellspacing='0' cellpadding='0'>
						<tbody>
							<tr>
								<td colspan='2'><b>${format.testName?html}</b></td>
							</tr>
							<!--
							[#if !(singleSpecimenType && singleSampleDate)
								 &&  (format.specimenType?? && format.specimenType != '')
								 && (format.sampleDate??)] -->
								<tr>
								    <td align='left'> Specimen:${format.specimenType?html}</td>
									<td align='right'>Sample Date:${format.sampleDate?string.short}</td>
								</tr>
							<!--
							[/#if] -->
							<!--
							[#if !(singleLabNo && singleTestDate)
							     	&& (format.labNumber != '-' || format.testDate??)] -->
								<tr>
									<td align='left'>
								<!--	[#if format.labNumber != '-' ] -->
											${idNoLabel} ${format.labNumber}
								<!--	[/#if] -->
									</td>
									<td align='right'>
								<!--	[#if format.testDate??] -->
											Test Date: ${format.testDate?string.short}
								<!--	[/#if] -->
									</td>
							   </tr>
							<!--
						   	[/#if]-->
						</tbody>
						</table>
				 	</td>
				</tr>
				<!--
				[/#if]-->
				<tr>
					<td>Amendment Reason:${format.amendmentReason?html}
					</td>
				</tr>
				<tr>
					<td colspan='5'>
						<div style="margin-top: 10px;background-image: url('${cpath}/images/strikeoff.png');background-repeat: no-repeat;">
						  <!--[#if (format.result.patient_report_file??)]-->
								<strike>${format.result.patient_report_file}</strike>
						   <!--[/#if]-->
						</div>
					</td>
				</tr>
				<!-- [#if (format.testRemarks??) && (format.testRemarks != '')] -->
					<tr><td colspan='5'>Note:&nbsp;${format.testRemarks?html}</td></tr>
				<!--[/#if] -->
			<!--
			[/#list]
		[/#if]
         -->
	</tbody>
	</table>
 <!--
[/#list] -->

<!--
	Section 4:Legend/Signature/designations etc.
-->
<div id="signature">
	<table>
		 <tbody>
			 <tr>
			<!--[#if (conducting_doctors??) && (conducting_doctors?size > 0)]

					[#list conducting_doctors as doctor] -->
						<td valign="top">
						 	${doctor.DOCTOR_NAME?html} ${doctor.QUALIFICATION}<br/>
						 	${doctor.SPECIALIZATION?html}
						</td>
			<!--	[/#list]
			  	[#else] -->
			<!--    [#list designations as designation ]-->
						<td>
							${designation.DESIGNATION}
						</td>
			<!--    [/#list]
			  	[/#if]-->
			</tr>
		</tbody>
	</table>
</div>

<!-- [#if singleTechnician] -->
<table width="100%">
	<tbody>
<!-- [#if allAreValidated] -->
	<tr>
		<td width="500px"></td>
		<td><img src="UserImage.do?_method=view&amp;user_name=${technician}"/></td>
	</tr>
<!--[/#if] -->
	<tr>
		<td width="500px"></td>
		<td >${technician}</td>
	</tr>
	</tbody>
</table>
<!-- [/#if] -->

<!-- [#if singleCondDoctor] -->
<table width="100%">
	<tbody>
<!-- [#if allAreSignedOff] -->
	<tr>
		<td width="500px"></td>
		<td><img src="UserImage.do?_method=view&amp;doctor_id=${conducting_doc_id}"/></td>
	</tr>
<!-- [/#if] -->
	<tr>
		<td width="500px"></td>
		<td >${conducting_doctor!}</td>
	</tr>
	</tbody>
</table>
<!-- [/#if] -->


