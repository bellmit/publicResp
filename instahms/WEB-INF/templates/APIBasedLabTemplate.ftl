<!--
[#setting number_format = "##"]
[#assign hasAbnormalResults = false]
-->
${report_addendum!}
<h2 style='text-align: center;'>Laboratory Report</h2>
<!--Section 2: Report Level header -->
<div>
	<table cellspacing='0' cellpadding='2' width='100%'>
		 <tbody>
		<!--[#if visitDetails??]-->

			<tr>
				<td width='15%'>Doctor:</td>
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
		  <!--[/#if] -->
		  <!--[#if (incoming) ] -->
			  <tr>
			  	<td width='20%'>Incoming Hospital:</td>
			  	<td width='20%'>${incoming_hosp}</td>
			  	<td width='15%'>Referal Doctor:</td>
			  	<td width='15%'>${referal}</td>
			  </tr>
		  <!-- [/#if] -->
		 <!-- [/#if]-->
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
				 		[#assign width = {"Test Description":"30%","Result":"10%","Units":"10%","Reference Range":"30%","Remarks":"20%"}]
				 		[#assign keys = width?keys]
				  	[#else]
						[#assign width = {"Test Description":"40%","Result":"10%","Units":"10%","Reference Range":"40%"}]
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
							 <!-- [#if !singleValueTest]-->
							       <tr><td colspan='2'><b>${test.testName?html}</b></td></tr>
						   <!--[/#if] -->
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
													${idNoLabel!} ${test.labNumber!}
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
									${result.units?html}
								</td>
								<td>
									${(result.reference_range?html)?replace("\n","<br/>")}
								</td>
								<td  valign='top'>
									${result.comments?html}
								</td>
							</tr>
				<!--	[/#if]
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
								<tr><td colspan='2'><b>${format.testName?html}</b></td></tr>
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
						  <!--[#if ((format.result!'').patient_report_file??)]-->
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

         <!--
		[#if d.histoTests??]
			[#list d.histoTests as format]
				[#if !isRadiology] -->
				<tr>
					<td colspan='5'>
						<table width='100%' cellspacing='0' cellpadding='0'>
						<tbody>
								<tr><td colspan='2'><b>${format.testName?html}</b></td></tr>
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
						  <!--[#if ((format.short_impression??))]-->
							  <table>
							  <tbody>
							  	<tr>
							  		<td  width='40%' align="right">No of Blocks Prepared:</td>
							  		<td>${format.no_of_blocks!}</td>
							  	</tr>
							  	<tr>
							  		<td  width='20%' align="right">Clinical Details:</td>
									 <td>${format.clinical_details}</td>
							  	</tr>
							  	<tr>
							  		<td  width='20%' align="right">Gross Findings:</td>
							  		<td>${format.gross_details!}</td>
							  	</tr>
							  	<tr>
									 <td  width='30%' align="right">Microscopic Findings :</td>
									 <td>${format.micro_gross_details!}</td>
								 </tr>
								  <tr>
									  <td align="right">Impression :</td>
									  <td>${format.impression_details}</td>
								  </tr>
							  </tbody>
							  </table>
						   <!--[/#if]-->
						</div>
					</td>
				</tr>
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

         <!--
		[#if d.microTests??]
			[#list d.microTests as format]
				[#if !isRadiology] -->
				<tr>
					<td colspan='5'>
						<table width='100%' cellspacing='0' cellpadding='0'>
						<tbody>
								<tr><td colspan='2'><b>${format.testName?html}</b></td></tr>
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
						  <!--[#if ((!format.growth_exists))]-->
							  <table>
							  <tbody>
							  	<tr>
							  		<td>Culture</td>
							  		<td>${format.nogrowth_report_comment!}</td>
							  	</tr>
							  </tbody>
							  </table>
						   <!--[/#if]-->
						   <!--[#if ((format.growth_exists))]-->
						   		 <!-- [#if ((format.orgGrpDetails?? && format.orgGrpDetails?size > 0))]-->
						   		 	[#list format.orgGrpDetails as orgGroup]
									<table width="100%">
									<tbody>
										<tr>
											<td >Organism:</td>
											<td>${orgGroup.organism_name!}</td>
										</tr>
										<tr>
											<td >ABST Panel:</td>
											<td>${orgGroup.abst_panel_name!}</td>
										</tr>
									</tbody>
									</table>
									<table style="border:1px solid;" width="100%">
									<tbody>
										<tr>
											<th style="border-bottom:1px solid;border-right:1px solid;">Antibiotic</th>
											<th style="border-bottom:1px solid ;border-right:1px solid;">Result</th>
											<th style="border-bottom:1px solid ;">Final SIR</th>
										</tr>
										<!-- [#if ((format.antibitics?? && format.antibitics?size > 0))]-->
											[#list orgGroup.antibitics as antibiotic]
												<tr>
													<td>${antibiotic.antibiotic_name!}</td>
													<td>${antibiotic.anti_results!}</td>
													<td>${antibiotic.susceptibility!}</td>
												</tr>
											[/#list]
										<!--[/#if]-->
									</tbody>
									</table>
									[/#list]
								<!-- [#else] -->
										<table>
										<tbody>
										<!--	[#if format.growth_report_comment??] -->
											<tr>
												<td align="right">Culture:</td>
												<td>${format.growth_report_comment}</td>
											</tr>
										<!--[/#if]-->
										<!-- [#if format.microscopic_details??] -->
										<tr>
											<td align="right">Culture:</td>
											<td>${format.microscopic_details}</td>
										</tr>
										<!--[/#if]-->
										</tbody>
										</table>
						   		 <!--[/#if]-->
						   <!--[/#if]-->
						</div>
					</td>
				</tr>
				<!-- [#if !singleTechnician && format.techSignatureName?has_content] -->
					<tr><td colspan="4"></td><td style="text-align: left"><img src="UserImage.do?_method=view&amp;user_name=${format.techSignatureName}"/></td></tr>
					<tr><td colspan="4"></td><td style="text-align: left">${format.techSignatureName}</td></tr>
				<!-- [/#if]-->
				<!-- [#if !singleCondDoctor && report.signed_off == 'Y' && format.condDoctorId?has_content] -->
					<tr><td colspan="4"></td><td style="text-align: left"><img src="UserImage.do?_method=view&amp;doctor_id=${format.condDoctorId}"/></td></tr>
					<tr><td colspan="4"></td><td style="text-align: left">${format.condDoctorName}</td></tr>
				<!-- [/#if]-->
			<!--
			[/#list]-->
		<!--
		[/#if]-->
		<!--
         [#if d.cytoTests??] -->

		<!--	[#list d.cytoTests as format]
				[#if !isRadiology] -->
				<tr>
					<td colspan='5'>
						<table width='100%' cellspacing='0' cellpadding='0'>
						<tbody>
								<tr><td colspan='2'><b>${format.testName?html}</b></td></tr>
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
						<table width="100%" style="border:1px solid;">
						<tbody>
							<tr>
								<td align="center">Cytology Results</td>
							</tr>
							<tr>
						  		<td style="border-bottom:1px solid;border-right:1px solid;"></td>
						  	</tr>
							<tr>
					  			<td  align="left">Test Type:
					  				<!-- [#if format.test_type == 'P'] -->
					  					Pap Smear
					  				<!-- [/#if]-->
					  				<!-- [#if format.test_type == 'F'] -->
					  					FNAC
					  				<!-- [/#if]-->
					  				<!-- [#if format.test_type == 'T'] -->
					  					THINPREP
					  				<!-- [/#if]-->
					  			</td>
						  	</tr>
						  	<tr>
						  		<td align="left">Specimen Adequacy:
						  		${format.speciman_adequecy!}</td>
						  	</tr>
						  	<tr>
						  		<td  align="left">Smear Received:
						 		${format.smear_received!}</td>
						  	</tr>
						</tbody>
						</table>
						<table width ="100%">
						<tbody>
							<!--[#if format.cyto_clinical_details ?? && format.cyto_clinical_details != '']] -->
	  							<tr>
						  			<td width="40%">Clinical Details :</td>
							  	</tr>
							  	<tr>
							  		<td>${(((format.cyto_clinical_details!)?html)?replace("\n","<br/>"))?replace("--break--", '<p class="pagebreak"/>')}</td>
							  	</tr>
						  	<!--[/#if]] -->
							<!--[#if format.cyto_microscopic_details ?? && format.cyto_microscopic_details != '']] -->
	  							<tr>
						  			<td width="40%">Microscopic Details :</td>
							  	</tr>
							  	<tr>
							  		<td>${(((format.cyto_microscopic_details!)?html)?replace("\n","<br/>"))?replace("--break--", '<p class="pagebreak"/>')}</td>
							  	</tr>
						  	<!--[/#if]] -->
						  	<!--[#if format.cyto_short_impression ?? && format.cyto_short_impression != '']] -->
							  	<tr>
							  		<td width="40%">Impression:</td>
							  	</tr>
							  	<tr>
							  		<td>${(((format.cyto_short_impression!)?html)?replace("\n","<br/>"))?replace("--break--", '<p class="pagebreak"/>')}</td>
							  	</tr>
						  	<!--[/#if]] -->
						  	<tr></tr>
						  	<!--[#if format.cyto_impression_details ?? && format.cyto_impression_details != '']] -->
								 <tr>
								  	<td  width='40%'>Impression details :</td>
								</tr>
								<tr>
									<td>${(((format.cyto_impression_details!)?html)?replace("\n","<br/>"))?replace("--break--", '<p class="pagebreak"/>')}</td>
								</tr>
							<!--[/#if]] -->
						</tbody>
						</table>
				 	</td>
				</tr>
				<!-- [#if !singleTechnician && format.techSignatureName?has_content] -->
					<tr><td colspan="4"></td><td style="text-align: left"><img src="UserImage.do?_method=view&amp;user_name=${format.techSignatureName}"/></td></tr>
					<tr><td colspan="4"></td><td style="text-align: left">${format.techSignatureName}</td></tr>
				<!-- [/#if]-->
				<!-- [#if !singleCondDoctor && report.signed_off == 'Y' && format.condDoctorId?has_content] -->
					<tr><td colspan="4"></td><td style="text-align: left"><img src="UserImage.do?_method=view&amp;doctor_id=${format.condDoctorId}"/></td></tr>
					<tr><td colspan="4"></td><td style="text-align: left">${format.condDoctorName}</td></tr>
				<!-- [/#if]-->
				<!--
				[/#if]-->
			<!--
			[/#list]-->
		<!--
		[/#if]-->
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
				<td width='15%'>Doctor:</td>
				<td width='50%'>${prescribing_doctor_amended}</td>
			</tr>
		   <!--[#if (singleTestDate && singleLabNo) && (reportTestDate !='' || reportLabNo !='')]-->
				<tr>
					<td width='15%'>Test Date: </td>
					<td width='50%'>${reportTestDate_amended}</td>
					<td width='20%'>${idNoLabel_amended}</td>
					<td width='15%'>${reportLabNo_amended}</td>
				</tr>
		  <!--[/#if]-->
		  <!--[#if (singleSpecimenType && singleSampleDate && !isRadiology ) && (reportSampleDate !='' || reportSpecimenType !='')] -->
			   <tr>
					<td width='15%'>Sample Date:</td>
					<td width='15%'>${reportSampleDate_amended}</td>
					<td width="20%">Specimen</td>
					<td width="15%">${reportSpecimenType_amended?html}</td>
			   </tr>
		  <!--[/#if] -->
		  <!--[#if (incoming) ] -->
			  <tr>
			  	<td width='20%'>Incoming Hospital:</td>
			  	<td width='20%'>${incoming_hosp_amended}</td>
			  	<td width='15%'>Referal Doctor:</td>
			  	<td width='15%'>${referal_amended}</td>
			  </tr>
		  <!-- [/#if] -->
		 <!-- [/#if]-->
		  </tbody>
	</table>
</div>
<!-- [/#if] -->
 <!-- [#list amendedDepts as d] -->
	<h4 style='text-align: center'>${d.deptName?html}</h4>
	<table valign='top' cellpadding='2' cellspacing='0' border='0' width='100%' >
	<tbody>
	<!-- [#if (d.valueTests??) && (d.valueTests?size>0) ] -->
			<tr style='border-bottom: 1px solid black;'>
				<!--[#if (d.hasRemarks == true)]
				 		[#assign width = {"Test Description":"30%","Result":"10%","Units":"10%","Reference Range":"30%","Remarks":"20%","Amendment Reason":"30%"}]
				 		[#assign keys = width?keys]
				  	[#else]
						[#assign width = {"Test Description":"40%","Result":"10%","Units":"10%","Reference Range":"40%","Amendment Reason":"30%"}]
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
					<td colspan="6">&nbsp;</td>
				</tr>
				<tr>
					<td colspan='6'>
						 <table width='100%' cellspacing='0' cellpadding='0'>
						   <tbody>
							 <!-- [#if !singleValueTest]-->
							       <tr><td colspan='2'><b>${test.testName?html}</b></td></tr>
						   <!--[/#if] -->
							</tbody>
						</table>
				 	</td>
				</tr>
				<!--[#list test.results as result]
						[#if (result.report_value??) ] -->
							<tr>
								<td valign='top'>
								   <!--[#if singleValueTest ]--><p><!--[/#if] -->
								    <strike>${result.resultlabel?html}</strike>
								   <!--[#if singleValueTest ]--></p><!--[/#if] -->
								</td>
								<td valign='top'>
								 <!--[#if ((result.withinnormal != '*') && (result.withinnormal != '#')) ] -->
								         <strike> ${result.report_value?html}</strike>
								 <!--[/#if]-->
								 <!--[#if ((result.withinnormal != 'Y') && (result.withinnormal != '*') && (result.withinnormal != '#')) ]
									      [#assign hasAbnormalResults = true ] -->
									      <strike>${result.withinnormal}</strike>
								  <!--[/#if]-->
								  <!--[#if ((result.withinnormal != 'Y') && (result.withinnormal == '*' || result.withinnormal == '#')) ]
									      [#assign hasAbnormalResults = true ] -->
									       <strike> <b>${result.report_value?html}
								     	    ${result.withinnormal}</b></strike>
								  <!--[/#if]-->
								</td>
								<td valign='top'>
									<strike>${result.units?html}</strike>
								</td>
								<td>
									<strike>${(result.reference_range?html)?replace("\n","<br/>")}</strike>
								</td>
								<!--[#if (d.hasRemarks == true)] -->
								<td  valign='top'>
									${result.comments?html}
								</td>
								<!-- [/#if] -->
								<td>
									${result.amendment_reason?html}
								</td>
							</tr>

				   <!--	[/#if] -->
				  <!-- [/#list] -->
				 <!-- [#if (test.testRemarks??) && (test.testRemarks != '')] -->
					<tr><td colspan='5'>Note:&nbsp;${test.testRemarks?html}</td></tr>
				<!--[/#if] -->
			<!--[/#list]-->
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
								<tr><td colspan='2'><b>${format.testName?html}</b></td></tr>
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
						<div style="margin-top: 10px;background-image: url('${cpath}/images/strikeoff.png');background-repeat: repeat-y;">
						  <!--[#if ((format.result!'').patient_report_file??)]-->
								${format.result.patient_report_file}
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

<br/><br/>
<!-- [#if hasAbnormalResults] -->
    <table>
    	<tbody>
	    	<tr><td>
				<p> Key: *=Abnormal Low, **=Critical Low,***=Improbable Low,<br/> #=Abnormal High, ##=Critical High, ###=Improbable High</p>
			</td></tr>
		</tbody>
	</table><br/><br/>
<!-- [/#if] -->
<!--
	Section 4:Legend/Signature/designations etc.
-->
<div id="signature" style="margin-top: 2em">
	<table width="100%">
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
		<td ><img src="UserImage.do?_method=view&amp;user_name=${technician}"/></td>
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
<!--[/#if] -->
	<tr>
		<td width="500px"></td>
		<td >${conducting_doctor!}</td>
	</tr>
</tbody>
</table>
<!-- [/#if] -->
