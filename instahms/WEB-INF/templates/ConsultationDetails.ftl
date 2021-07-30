[#setting number_format="#"]
[#assign emptyList=[]]
[#assign emptyMap={}]
[#list consultations as consultation]
	[#assign consultation_bean=consultation.consultation_bean]
	[#assign consultation_components=consultation.consultation_components]
	[#assign secondary_complaints=consultation.secondary_complaints!emptyList]
	[#assign allergies=consultation.allergies!emptyList]
	[#assign vital_values=consultation.vital_values!emptyList]
	[#assign consultationFields=consultation.consultationFields!emptyList]
	[#assign consultImages=consultation.consultImages!emptyList]
	[#assign consult_phy_forms=consultation.consult_phy_forms!emptyList]
	[#assign PhysicianForms=consultation.PhysicianForms!emptyMap]
	[#assign presMedicines=consultation.presMedicines!emptyList]
	[#assign presServices=consultation.presServices!emptyList]
	[#assign presTests=consultation.presTests!emptyList]
	[#assign presConsultation=consultation.presConsultation!emptyList]
	[#assign presInstructions=consultation.presInstructions!emptyList]
	[#assign NonHospitalItems=consultation.NonHospitalItems!emptyList]
	[#assign presOperations=consultation.presOperations!emptyList]
	<table id="consulDoctorTab" cellspacing='0' cellpadding='0' width='100%'>
		<tr>
			<td colspan="2">
				Consulting Doctor: ${(consultation_bean.doctor_name)!?html}
			</td>
		</tr>
	</table>

	<!-- [#list consultation_components.sections?split(",") as formid]
			[#if formid?number == -1 ] -->
				<table style="margin-top: 10px">
	<!--			[#if visitdetails.complaint?has_content] -->
					<tr>
						<td width="150px">Chief Complaint: </td>
						<td>${(visitdetails.complaint)!?html}</td>
					</tr>
	<!--			[/#if] -->
	<!--			[#if secondary_complaints?has_content]
						[#list secondary_complaints as s_complaint] -->
						<tr>
							<td>Other Complaint: </td>
							<td>${s_complaint.complaint!?html}</td>
						</tr>
	<!--				[/#list]
					[/#if] -->
				</table>
	<!-- 	[#elseif formid?number == -2 && allergies?has_content] -->
			<h3 style="margin-top: 10px"><u>Allergies Details</u></h3>
			<table cellspacing='0' cellpadding='1' style='margin-top: 5px' width="100%">
				<tbody>
					<tr>
						<th width="15%">Allergy Type</th>
						<th width="30%">Allergy</th>
						<th width="10%">Onset Date</th>
						<th width="35%">Reaction</th>
						<th width="10%">Severity</th>
						<th width="10%">Status</th>
					</tr>
					<!--[#list allergies as allergy] -->
						<tr>
							<td valign="top">
								[#if allergy.allergy_type == 'N']
										No Known Allergies
								[#elseif allergy.allergy_type == 'M']
									Medicine
								[#elseif allergy.allergy_type == 'F']
									Food
								[#else] Other
								[/#if]
							</td>
							<td valign="top">${allergy.allergy}</td>
							<td valign="top">${allergy.onset_date}</td>
							<td valign="top">${allergy.reaction}</td>
							<td valign="top">${allergy.severity!}</td>
							<td valign="top">
								[#if allergy.status == 'A']
									Active
								[#else]Inactive
								[/#if]
							</td>
						</tr>
					<!--[/#list] -->
				</tbody>
			</table>
	<!--	[#elseif formid?number == -4 && vital_values?has_content] -->
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
				<!--	[#list vital_values as vital] 	-->
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
	<!--	[#elseif formid?number == -5]
				[#if consultationFields?has_content] -->
					<h3 style="margin-top: 10px"><u>Consultation Notes</u></h3>
					<table cellspacing='0' cellpadding='1' style='margin-top: 5px' width="100%">
					<tbody>
						<!-- [#list consultationFields as field] -->
							<tr>
								<td><b>${field.field_name!}: </b><br/>
									<span style="margin-left: 10px">
										${(((field.field_value!)?html)?replace("\n","<br/>"))?replace("--break--", '<p class="pagebreak"/>')}
									</span>
								</td>
							</tr>
						<!-- [/#list] -->
					</tbody>
					</table>
	<!--		[/#if]
				[#if consultImages?has_content] -->
					<h3 style="margin-top: 10px"><u>Consultation Images</u></h3>
					<div id="attachImages">
						<table border="0" style="margin-top: 10px">
							<tbody>
								[#list consultImages as image]
									<tr>
										<td>Date: ${image.datetime?string('dd-MM-yyyy')}</td>
									</tr>
									<tr>
										<td><img src="OPPrescribeAction.do?method=viewImage&amp;image_id=${image.image_id}"/></td>
									</tr>
								[/#list]
							</tbody>
						</table>
					</div>
	<!--		[/#if]
			[#elseif formid?number == -6 && diagnosis_details?has_content] -->
				<h3 style="margin-top: 10px"><u>Consultation Details</u></h3>
				<table cellspacing='0' cellpadding='1' style='margin-top: 5px' width="100%">
					<tbody>
						<tr>
							<th width="15%">Diag. Type</th>
							<th width="15%">Diag. Code</th>
							<th width="70%">Description</th>
						</tr>
	<!--				[#list diagnosis_details as diagnosis] -->
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
	<!--				[/#list] -->
					</tbody>
				</table>
	<!--	[#elseif formid?number > 0] -->
				<!--[#list consult_phy_forms as tpf] -->
				<!--[#if PhysicianForms[tpf.section_title]?has_content && tpf.section_id == formid?number] -->
						<h3>${tpf.section_title?html}</h3>
						<table width="100%" cellspacing="0" cellpadding="0" style="margin-top: 10px">
							<tbody>
							<tr>
								<td>
				<!--				[#list PhysicianForms[tpf.section_title] as field] -->
				<!--					[#if field[0].field_type == 'image'] -->
											</td></tr><tr><td>
				<!--					[/#if] -->
								 		${field[0].field_name?html}:&nbsp;
								 		<b> <!-- display all values in bold -->
				<!--					[#if field[0].field_type == 'image'] -->
									 		<div style="width: 800px; height: 400px; page-break-inside: avoid;
												background-image: url('PhysicianFieldsImage.do?_method=viewImage&amp;field_id=${field[0].field_id}&amp;image_id=${field[0].image_id!0}'); background-repeat:no-repeat;">
				<!--					[/#if] -->
				<!--					[#assign marker_number=1] -->
				<!--					[#list field as value]
											[#if value.field_type == 'checkbox']
												[#if value.allow_normal == 'Y' && value.option_id == 0] -->
													Normal [#if value.option_remarks?has_content]-[/#if]${value.option_remarks!?html},
				<!--							[#else] -->
													${value.option_value!?html}[#if value.option_remarks?has_content]-[/#if]${value.option_remarks!?html},
				<!--							[/#if]
											[#elseif value.field_type == 'dropdown']
												[#if value.allow_others == 'Y' && value.option_id ==-1] -->
													${(value.option_remarks!)?html},
				<!--							[#elseif value.allow_normal == 'Y' && value.option_id == 0] -->
													Normal,
				<!--							[#else] -->
													${value.option_value!?html},
				<!--							[/#if]
											[#elseif value.field_type == 'text' || value.field_type == 'wide text'] -->
												${(value.option_remarks!)?html},
				<!--						[#elseif value.field_type == 'image' && value.marker_id?has_content] -->
												<div style="height: 0px"> <!--  this is required to mention with 0px height to align the marker at the correct place.-->
													<img src="PhysicianFieldsImageMarkers.do?_method=view&amp;image_id=${value.marker_id}"
														style="top: ${value.coordinate_y!0}px;
														left: ${value.coordinate_x!0}px;position: relative;display:block;"/>
				<!-- 								[#if (value.notes!?html)?has_content] -->
														<div style="top: ${value.coordinate_y!0}px;
															left: ${value.coordinate_x!0}px;position: relative;display:block;">${marker_number}</div>
				<!--								[#assign marker_number=marker_number+1]
													[/#if] -->
												</div>
				<!--						[#elseif value.field_type == 'date'] -->
												${(value.date?string('dd-MM-yyyy'))!}
				<!--						[#elseif value.field_type == 'datetime'] -->
												${(value.date_time?string('dd-MM-yyyy HH:mm'))!}
				<!--						[/#if]
										[/#list]  -->
				<!--					[#if field[0].field_type == 'image'] -->
											</div>
											<div style="clear:both"></div>
				<!--						[#assign marker_number=1] -->
				<!--						[#list field as value] -->
				<!--							[#if value.field_type == 'image' && (value.notes!?html)?has_content] -->
													${marker_number} ) ${value.notes?html}<br/>
				<!--								[#assign marker_number=marker_number+1]
												[/#if] -->
				<!--						[/#list] -->
				<!--					[/#if] -->
										</b>
				<!--			 	[/#list]  -->
								</td>
				 			</tr>
					 		</tbody>
					 	</table>
				<!--[/#if] -->
				<!--[/#list] -->
	<!--	[#elseif formid?number == -7] -->
				<!-- [#if presMedicines?has_content || presTests?has_content || presServices?has_content
						|| presConsultation?has_content || presOperations?has_content
						|| NonHospitalItems?has_content] -->
					<h3 style="margin-top: 10px"><u>Prescriptions</u></h3>
				<!-- [/#if] -->
				<table cellspacing='0' cellpadding='1' width='100%' align='center' style='margin-top: 5px'>
					<tbody>
					<!-- [#if presMedicines?has_content] -->
					<tr>
						<td>
							<table cellspacing='0' cellpadding='1' style='margin-top: 10px' width="100%">
								<tbody>
								<tr>
									<th width="25%">Medicine Name</th>
									<!-- [#assign medicine_name=presMedicines[0].item_name!?html] -->
									<!-- [#assign generic_name=presMedicines[0].generic_name!?html] -->
									<!-- [#if medicine_name == ''] -->
									<th width="10%">Form</th>
									<th width="10%">Strength</th>
									<!-- [/#if] -->
									<th width="10%">Dosage</th>
									<th width="10%">Duration</th>
									<th width="10%">Qty</th>
									<th width="35%">Remarks</th>
								</tr>
								<!-- [#list presMedicines as medicine] -->
								<tr>
									<td>
										<!-- [#assign medicine_name=medicine.item_name!?html] -->
										<!-- [#assign generic_name=medicine.generic_name!?html] -->
										[#if medicine_name == '']
											${generic_name}
										[#else]
											${medicine_name}
										[/#if]
										[#if medicine.non_hosp_medicine][Non Hosp][/#if]
									</td>
									<!-- [#if medicine_name == ''] -->
									<td>${medicine.item_form_name!?html}</td>
									<td>${medicine.item_strength!?html} ${medicine.unit_name!?html}</td>
									<!-- [/#if] -->
									<td>${(medicine.medicine_dosage!)?html}</td>
									<td>[#if medicine.duration?has_content]${(medicine.duration)} ${medicine.duration_units!}[/#if]</td>
									<td>${(medicine.medicine_quantity)!}</td>
									<td>${(medicine.item_remarks!)?html}</td>
								</tr>
								<!-- [/#list] -->
								</tbody>
							</table>
						</td>
					</tr>
					<!-- [/#if] -->
					<!-- [#if presTests?has_content] -->
					<tr>
						<td>
							<table cellspacing='0' cellpadding='1' style='margin-top: 10px' width="100%">
								<tbody>
								<tr>
									<th width='40%'>Investigation</th>
									<th width='60%'>Remarks</th>
								</tr>
								<!-- [#list presTests as test] -->
								<tr>
									<td>${(test.item_name!)?html}</td>
									<td>${(test.item_remarks!)?html}</td>
								</tr>
								<!-- [/#list] -->
								</tbody>
							</table>
						</td>
					</tr>
					<!-- [/#if] -->
					<!-- [#if presServices?has_content] -->
					<tr>
						<td>
							<table cellspacing='0' cellpadding='1' style='margin-top: 10px;' width="100%" >
								<tbody>
								<tr>
									<th width='40%'>Service Name</th>
									<th width='10%'>Qty</th>
									<th width='10%'>Tooth No(s).</th>
									<th width='40%'>Remarks</th>
								</tr>
								<!-- [#list presServices as service] -->
								<tr>
									<td >${(service.item_name!)?html}</td>
									<td>${service.service_qty}</td>
									<td>[#if service.tooth_unv_number?has_content]
											${service.tooth_unv_number}
										[#elseif service.tooth_fdi_number?has_content]
											${service.tooth_fdi_number}
										[/#if]
									</td>
									<td >${(service.item_remarks!)?html}</td>
								</tr>
								<!-- [/#list] -->
								</tbody>
							</table>
						</td>
					</tr>
					<!-- [/#if] -->
					<!-- [#if presConsultation?has_content] -->
					<tr>
						<td>
							<table cellspacing='0' cellpadding='1' style='margin-top: 10px' width="100%">
								<tbody>
								<tr>
									<th width='40%'>Consultation Name</th>
									<th width='60%'>Remarks</th>
								</tr>
								<!-- [#list presConsultation as consultation] -->
								<tr>
									<td>${(consultation.item_name!)?html}</td>
									<td>${(consultation.item_remarks!)?html}</td>
								</tr>
								<!-- [/#list] -->
								</tbody>
							</table>
						</td>
					</tr>
					<!-- [/#if] -->
					<!-- [#if presOperations?has_content] -->
					<tr>
						<td>
							<table cellspacing='0' cellpadding='1' style='margin-top: 10px;' width="100%" >
								<tbody>
								<tr>
									<th width='40%'>Operation Name</th>
									<th width='60%'>Remarks</th>
								</tr>
								<!-- [#list presOperations as operation] -->
								<tr>
									<td >${(operation.item_name!)?html}</td>
									<td >${(operation.item_remarks!)?html}</td>
								</tr>
								<!-- [/#list] -->
								</tbody>
							</table>
						</td>
					</tr>
					<!-- [/#if] -->
					<!-- [#if NonHospitalItems?has_content] -->
						<tr>
							<td>
								<table cellspacing="0" cellpadding="1" style="margin-top: 10px" width="100%">
									<tbody>
										<tr>
											<th width="25%">Item Name</th>
											<th width="10%">Form</th>
											<th width="10%">Strength</th>
											<th width="10%">Dosage</th>
											<th width="10%">Duration</th>
											<th width="10%">Qty</th>
											<th width='25%'>Item Remarks</th>
										</tr>
										<!-- [#list NonHospitalItems as item] -->
										<tr>
											<td >${(item.item_name!)?html}</td>
											<td>${item.item_form_name!?html}</td>
											<td>${item.item_strength!?html} ${item.unit_name!?html}</td>
											<td>${(item.medicine_dosage!)?html}</td>
											<td>[#if item.duration?has_content]${(item.duration)} ${item.duration_units!}[/#if]</td>
											<td>${(item.medicine_quantity)!}</td>
											<td >${(item.item_remarks!)?html}</td>
										</tr>
										<!-- [/#list] -->
									</tbody>
								</table>
							</td>
						</tr>
					<!-- [/#if] -->
					<!-- [#if consultation_bean.prescription_notes?has_content] -->
					<tr>
						<td>
							<table cellsapcing="0" cellpadding="1" style="margin-top: 10px" width="100%">
								<tbody>
									<tr>
										<td width="150px"><b>Prescription Notes: </b></td>
										<td>${(((consultation_bean.prescription_notes!)?html)?replace("\n","<br/>"))?replace("--break--", '<p class="pagebreak"/>')}</td>
									</tr>
								</tbody>
							</table>
						</td>
					</tr>
					<!-- [/#if] -->
					</tbody>
				</table>
	<!--	[/#if]
		[/#list] -->




	<table cellspacing='0' cellpadding='1' style='margin-top: 10px' width="100%">
		<tbody>
			<!-- [#if consultation.followup_date?has_content] -->
			<tr>
				<td align="left"><b>Follow Up Date: </b>${consultation.followup_date!}</td>
			</tr>
			<!-- [/#if] -->

			<tr height="10px"><td >&nbsp;</td></tr>
			<tr>
				<td align='left'><b>Doctor's Signature</b></td>
			</tr>
		</tbody>
	</table>
[/#list]
