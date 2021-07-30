<!-- medicines part starts -->
<!-- [#if (medicines??) && (medicines?size > 0)] -->
	<div id="medicinesDiv">
		<h3>Medicines: </h3>
		<table id="medicinesTab" width="100%">
			<tbody>
<!--		[#list medicines as medicine] -->
				<tr>
					<td width="30px"></td>
					<td>${(medicine.medicine_name?html)!}</td>
				</tr>
<!--		[/#list] -->
			</tbody>
		</table>
	</div>
<!-- [/#if] -->
<!-- medicines part ends -->
<!-- test part starts -->
<!--[#if tests?has_content] -->
	<div id="testsDiv">
		<h3>Investigations</h3>
		<!-- [#assign width = {"Test Description":"30%","Result":"10%","Units":"10%","Reference Range":"30%","Remarks":"20%"}]
		[#assign keys = width?keys] -->
		<table width='100%' cellspacing='0' cellpadding='0'>
			<tbody>
				<tr>
				<!--[#list keys as head] -->
					<th align='left' width='${width[head]}'>${head}</th>
				<!--[/#list]-->
				</tr>
			<!--[#list tests as test] -->
			<!--	[#if (test.format!'') == 'N' ] -->
						<tr>
							<td colspan='5'><b>${test.testName?html}</b></td>
						</tr>
			<!--		[#list test.testValues as values] -->
							<tr>
								<td>${values.resultlabel?html}</td>
								<td>
									<!--[#if ((values.withinnormal != '*') && (values.withinnormal != '#')) ] -->
								          ${values.report_value?html}
									<!--[/#if]-->
									<!--[#if ((values.withinnormal != 'Y') && (values.withinnormal != '*') && (values.withinnormal != '#')) ]
									      [#assign hasAbnormalResults = true ] -->
									      ${values.withinnormal}
									<!--[/#if]-->
									<!--[#if ((values.withinnormal != 'Y') && (values.withinnormal == '*' || values.withinnormal == '#')) ]
									    	[#assign hasAbnormalResults = true ] -->
									      	<b>${values.report_value?html}
									   	    	${values.withinnormal}</b>
									<!--[/#if]-->
								</td>
								<td>${values.units?html}</td>
								<td>${(values.reference_range?html)?replace("\n","<br/>")}</td>
								<td>${values.comments?html}</td>
							</tr>
			<!-- 		[/#list]
						[#if test.notes?has_content] -->
							<tr>
								<td colspan="5"><b>NOTES: ${test.notes?html}</b></td>
							</tr>
			<!--		[/#if]
					[/#if]
				[/#list] -->
			</tbody>
		</table>
		<table width="100%" cellspacing="0" cellpadding="0">
			<tbody>
	<!--	[#list tests as test]
				[#if (test.format!'') == 'Y' ]
					[#list test.testValues as values] -->
						<tr>
							<td>${values.patient_report_file}</td>
						</tr>
	<!--			[/#list]
					[#if test.notes?has_content] -->
						<tr>
							<td colspan="5"><b>NOTES: ${test.notes?html}</b></td>
						</tr>
	<!--			[/#if]
				[/#if]
			[/#list] -->
			</tbody>
		</table>
	</div>
<!-- [/#if] -->
<!-- test part ends-->

<!-- Services part starts here -->
<div id="servicesDiv">
	<h3>Services</h3>
	<table width="100%" cellspacing="0" cellpadding="0">
		<tbody>
<!--		[#list services as service] -->
				<tr>
					<td colspan="2">${service.serviceName?html}</td>
				</tr>
		<!--	[#if (service.format!'') == 'doc_hvf_templates']
					[#list service.hvfValues as values]
						[#if ((values.field_value)!'') != ''] -->
							<tr>
								<td><b>${(values.field_name)!?html}</b></td>
							</tr>
							<tr>
								<td ><font style='margin-left: 30px'>${((values.field_value)?html)?replace("\n","<br/>")}</font></td>
							</tr>
		<!--			[/#if]
					[/#list]
				[#elseif (service.format!'') == 'doc_rich_templates'] -->
						<tr>
							<td>${service.richTextContent!}</td>
						</tr>
		<!-- 	[/#if]
				[#if service.notes?has_content] -->
					<tr>
						<td><b>NOTES: ${service.notes?html}</b></td>
					</tr>
		<!-- 	[/#if]
			[/#list] -->
		</tbody>
	</table>
</div>
<!-- Services part ends here -->
<!-- Operation part starts here -->
<!-- [#if ((operations??) && (operations?size > 0))] -->
		<div id="operationsDiv" >
			<h3>Operations: </h3>
<!--		[#list operations as operation] -->
			  	<table width="100%">
			  		<tbody>
					<tr>
						<td align="left">Operation Theatre:</td>
						<td align="left"><b> ${operation.theatre!}</b></td>
						<td align="left">Operation:</td>
						<td align="left"><b>${operation.name!}</b></td>
						<td></td>
					</tr>
					<tr>
						<td align="left">Operation Start Date:</td>
						<td align="left"><b>${operation.operation_date!}</b></td>
						<td align="left">Start Time:</td>
						<td align="left"><b> ${operation.starttime!}</b></td>
						<td align="left">Operation End Date:</td>
						<td align="left"><b> ${operation.operation_end_date!}</b></td>
						<td align="center">End Time:</td>
						<td><b> ${operation.endtime!}</b></td>
					</tr>
<!--				[#assign teamSize = 0] -
					[#assign prescSize = 0]
						[#list operation_presc as presc]
							[#if (presc.operation_ref == operation.prescribed_id) && (presc.role != "1")]
								[#assign teamSize = teamSize+1]
							[/#if]
							[#if (presc.operation_ref == operation.prescribed_id) && (presc.role == "1")]
								[#assign prescSize = prescSize+1]
							[/#if]
						[/#list]
						[#if (teamSize > 0)] -->
							<tr>
								<th>Surgery&nbsp;Team</th>
							</tr>
<!--					[/#if] -->
							<tr>
								<td></td>
								<td align="left">${operation.primarysurgeon!}</td>
								<td align="left">Primary Surgeon</td>
								<td align="left"></td>
								<td align="left">1</td>
								<td align="center"></td>
							</tr>
<!--					[#list operation_presc as presc]
							[#if (presc.operation_ref == operation.prescribed_id) && (presc.role != "1")] -->
								<tr>
									<td align="left"></td>
									<td align="left">${presc.doctor!}</td>
									<td align="left">${presc.pgroup!}</td>
									<td align="left"></td>
									<td align="left">${presc.qty!}</td>
								</tr>
<!--						[/#if]
						[/#list] -->
					</tbody>
				</table>
				<table  width="100%">
					<tbody>
<!--				[#if (prescSize > 0)] -->
						<tr>
							<th>OT&nbsp;Prescriptions</th>
						</tr>
<!--				[/#if]
					[#list operation_presc as presc]
						[#if (presc.operation_ref == operation.prescribed_id) && (presc.role == "1") ] -->
							<tr>
								<td align="left"></td>
								<td align="left">${presc.doctor!}</td>
								<td align="left">${presc.pgroup!}</td>
								<td align="left">${presc.details!}</td>
								<td align="left">${presc.qty!}</td>
							</tr>
<!--					[/#if]
					[/#list]  -->
					</tbody>
				</table>
				<table width="100%">
					<tbody>
<!--					[#list operation_documents as op_doc]
							[#if op_doc.prescribedId == operation.prescribed_id]
								[#if (op_doc.format!'') == 'doc_hvf_templates']
									[#list op_doc.hvfValues as values]
										[#if ((values.field_value)!'') != ''] -->
											<tr>
												<td><b>${(values.field_name)!?html}</b></td>
											</tr>
											<tr>
												<td ><font style='margin-left: 30px'>${((values.field_value)?html)?replace("\n","<br/>")}</font></td>
											</tr>
<!--									[/#if]
									[/#list]
								[#elseif (op_doc.format!'') == 'doc_rich_templates'] -->
										<tr>
											<td>${op_doc.richTextContent}</td>
										</tr>
<!--						 	[/#if]
								[#if op_doc.notes?has_content] -->
									<tr>
										<td><b>NOTES: ${op_doc.notes?html}</b></td>
									</tr>
<!--						 	[/#if]
							[/#if]
						[/#list] -->
					</tbody>
				</table>
<!--		[/#list] -->
		</div>
<!-- [/#if] -->
<!-- End of Operations part-->
<!-- CaseSheet part starts here -->
	<div id="caseSheet">
		<h3>Case Sheet Details</h3>
		<table id="consulDoctorTab" cellspacing='0' cellpadding='0' width='100%'>
			<tbody>
<!-- 		[#if complaint?has_content] -->
		  		<tr>
		  			<td colspan="2">
		  				Complaint: ${(complaint)!?html}
		  			</td>
		  		</tr>
<!--		[/#if] -->
			</tbody>
	  	</table>
		<!-- [#if diagnosis_details?has_content] -->
		<h3 style="margin-top: 10px"><u>Consultation Details</u></h3>
		<table cellspacing='0' cellpadding='1' style='margin-top: 5px' width="100%">
			<tbody>
				<tr>
					<th width="15%">Diag. Type</th>
					<th width="15%">Diag. Code</th>
					<th width="70%">Description</th>
				</tr>
<!--		[#list diagnosis_details as diagnosis] -->
				<tr>
					<td valign="top">
						[#if diagnosis.diag_type == 'P' ]
							Principal
						[#elseif diagnosis.diag_type == 'A']Admitting
						[#elseif diagnosis.diag_type == 'V']Reason For Visit
						[#else]Secondary
						[/#if]
					</td>
					<td valign="top">${diagnosis.icd_code!?html}</td>
					<td valign="top">${diagnosis.description!?html}</td>
				</tr>
<!--		[/#list] -->
			</tbody>
		</table>
		<!-- [/#if] -->
<!--[#list patientConsultations as consultation]

		[#if consultation.cw_vital_values?has_content] -->
			<h3 style="margin-top: 10px"><u>Vitals</u></h3>
			<table cellspacing='0' cellpadding='1' style='margin-top: 5px; empty-cells: show; border-collapse:collapse;' width="100%" >
				<tbody>
					<tr>
						<th style="border: 1px solid;">Date</th>
			<!--		[#list vital_params as param] -->
							<th style="border: 1px solid;">${param.param_label!?html}
								<!--[#if  (param.param_uom??) && "${param.param_uom}" != ""]-->
									(${param.param_uom})
								<!--[/#if]-->
							</th>
			<!--		[/#list] -->
						<th style="border: 1px solid">User</th>
					</tr>
			<!--	[#list consultation.cw_vital_values as vital] 	-->
						<tr>
							<td style="border: 1px solid">${vital['date_time']?string('dd-MM-yyyy HH:mm')}</td>
							[#list vital_params as param]
								<td style="border: 1px solid">${vital[param.param_label]!?html} ${param.param_uom!}</td>
							[/#list]
							<td style="border: 1px solid">${vital['user_name']!}</td>
						</tr>
			<!--	[/#list] -->
				</tbody>
			</table>
<!-- 	[/#if] -->
<!--	[#if consultation.cw_hvf_fields?has_content] -->
			<h3 style="margin-top: 10px"><u>Consultation Notes</u></h3>
			<table cellspacing='0' cellpadding='1' style='margin-top: 5px' width="100%">
			<tbody>
<!-- 			[#list consultation.cw_hvf_fields as field] -->
					<tr>
						<td><b>${field.field_name!}</b><br/>
							<span style="margin-left: 10px">
								${(((field.field_value!)?html)?replace("\n","<br/>"))?replace("--break--", '<p class="pagebreak"/>')}
							</span>
						</td>
					</tr>
<!-- 			[/#list] -->
			</tbody>
			</table>
<!-- 	[/#if] -->
<!--	[#if consultation.cw_images?exists] -->
			<h3 style="margin-top: 10px"><u>Consultation Images</u></h3>
			<table border="0" style="margin-top: 10px">
				<tbody>
<!--				[#list consultation.cw_images as image] -->
						<tr>
							<td>Date: ${image.datetime?string('dd-MM-yyyy')}</td>
						</tr>
						<tr>
							<td><img src="OPPrescribeAction.do?method=viewImage&amp;image_id=${image.image_id}"/></td>
						</tr>
<!--				[/#list] -->
				</tbody>
			</table>
<!--	[/#if] -->
<!--	[#if consultation.cw_medicines?has_content]	-->
			<table cellspacing='0' cellpadding='1' style='margin-top: 10px' width="100%">
				<tbody>
				<tr>
					<th >Medicine Name</th>
					<th width="20%">Dosage</th>
					<th width="15%">Duration</th>
					<th width="10%">Qty</th>
					<th width="10%">Remarks</th>
				</tr>
<!-- 			[#list consultation.cw_medicines as medicine] -->
					<tr>
						<td>${(medicine.medicine_name!)?html}</td>
						<td>${(medicine.medicine_dosage!)?html}</td>
						<td>[#if medicine.duration?has_content]${(medicine.duration?string("0"))} ${medicine.duration_units!}[/#if]</td>
						<td>${(medicine.medicine_quantity)!}</td>
						<td>${(medicine.medicine_remarks!)?html}</td>
					</tr>
<!-- 			[/#list] -->
				</tbody>
			</table>
<!--	[/#if]  -->
<!--	[#if consultation.cw_tests?has_content] -->
			<table cellspacing='0' cellpadding='1' style='margin-top: 10px' width="100%">
				<tbody>
				<tr>
					<th width='40%'>Test Name</th>
					<th width='60%'>Remarks</th>
				</tr>
<!-- 			[#list consultation.cw_tests as test] -->
					<tr>
						<td>${(test.test_name!)?html}</td>
						<td>${(test.test_remarks!)?html}</td>
					</tr>
<!-- 			[/#list] -->
				</tbody>
			</table>
<!--	[/#if] -->
<!--	[#if consultation.cw_services?has_content] -->
			<table cellspacing='0' cellpadding='1' style='margin-top: 10px;' width="100%" >
				<tbody>
				<tr>
					<th width='40%'>Service Name</th>
					<th width='60%'>Remarks</th>
				</tr>
<!-- 			[#list consultation.cw_services as service] -->
					<tr>
						<td >${(service.service_name!)?html}</td>
						<td >${(service.service_remarks!)?html}</td>
					</tr>
<!-- 			[/#list] -->
				</tbody>
			</table>
<!--	[/#if] -->
<!--	[#if consultation.cw_crossConsultations?has_content] -->
			<table cellspacing='0' cellpadding='1' style='margin-top: 10px' width="100%">
				<tbody>
				<tr>
					<th width='40%'>Consultation Name</th>
					<th width='60%'>Remarks</th>
				</tr>
<!-- 			[#list consultation.cw_crossConsultations as consultation] -->
					<tr>
						<td>${(consultation.cons_doctor_name!)?html}</td>
						<td>${(consultation.cons_remarks!)?html}</td>
					</tr>
<!-- 			[/#list] -->
				</tbody>
			</table>
<!--	[/#if] -->
		<table cellsapcing="0" cellpadding="1" style="margin-top: 10px" width="100%">
			<tbody>
				<tr>
					<td width="150px"><b>Prescription Notes: </b></td>
					<td>${(((consultation.consultation_details.prescription_notes!)?html)?replace("\n","<br/>"))?replace("--break--", '<p class="pagebreak"/>')}</td>
				</tr>
			</tbody>
		</table>
<!-- [/#list] -->
<!-- [#if followupDetails?has_content] -->
	<h3 style="margin-top: 10px"><u>Follouw Up Details: </u></h3>
	<table cellspacing='0' cellpadding='1' style='margin-top: 0px' width="100%">
		<tbody>
			<tr>
				<th>Doctor Name</th>
				<th>Follow Up Date</th>
				<th>Remarks</th>
			</tr>
			<!-- [#list followupDetails as item] -->
    			<tr>
					<td>${item.doctor_name}</td>
					<td>${item.followup_date}</td>
					<td>${item.followup_remarks}</td>
				</tr>
			<!-- [/#list] -->
		</tbody>
	</table>
<!-- [/#if] -->
	</div>
<!-- CaseSheet part ends here -->