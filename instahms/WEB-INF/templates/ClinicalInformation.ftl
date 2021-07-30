[#-- other available tokens in this print are
1) vitals
2) diagnosis details
3) consultation fields(customizable fields)
4) allergies

to display

1) vitals, use the following code snippet

	[#if vitals?has_content]
		<h3 style="margin-top: 10px"><u>Vitals</u></h3>
		<table cellspacing='0' cellpadding='1' style='margin-top: 5px; empty-cells: show; border-collapse:separate;' width="100%" >
			<tbody>
				<tr>
					<th style="border: 1px solid;">Date</th>
		<!--		[#list vital_params as param] -->
						<th style="border: 1px solid;">${param.param_label!?html}
							<!--[#if (param.param_uom??) && "${param.param_uom}" != ""]-->
								(${param.param_uom})
							<!--[/#if]-->
						</th>
		<!--		[/#list] -->
					<th style="border: 1px solid">User</th>
				</tr>
		<!--	[#list vitals as vital] 	-->
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
	[/#if]

2)  diagnosis details, use the following code snippet

	[#if diagnosis_details?has_content]
	<h3 style="margin-top: 10px"><u>Consultation Details</u></h3>
	<table cellspacing='0' cellpadding='1' style='margin-top: 5px' width="100%">
		<tbody>
			[#assign diagnosisIndex = 0]
			[#list diagnosis_details as diagnosis]
				<tr>
					<td width="100px"><b>
						 [#if diagnosis.diag_type == 'P' ]
							Principal
						 [#elseif diagnosis.diag_type == 'A']Admitting
						 [#elseif diagnosis.diag_type == 'V']Reason For Visit
						 [#else]Secondary
						 [/#if] Diagnosis:
						 </b>
					</td>
					<td >${diagnosis.description!}</td>
					<td width="15px"><b>Code: </b></td>
					<td >${diagnosis.icd_code!}</td>
				</tr>
				[#assign diagnosisIndex = diagnosisIndex +1]
			[/#list]
		</tbody>
	</table>
	[/#if]

3) consultation Fields, use the following code snippet

	[#if consultationFields?has_content]
	<h3 style="margin-top: 10px"><u>Consultation Notes</u></h3>
	<table cellspacing='0' cellpadding='1' style='margin-top: 5px' width="100%">
	<tbody>
		[#list consultationFields as field]
			<tr>
				<td><b>${field.field_name!}</b><br/>
					<span style="margin-left: 10px">
						${(((field.field_value!)?html)?replace("\n","<br/>"))?replace("--break--", '<p class="pagebreak"/>')}
					</span>
				</td>
			</tr>
		[/#list]
	</tbody>
	</table>
	[/#if]

4) for allergies use the following code snippet.

	[#if allergies?has_content]
	<h3 style="margin-top: 10px"><u>Allergies Details</u></h3>
			<table cellspacing='0' cellpadding='1' style='margin-top: 5px' width="100%">
				<tbody>
					<tr>
						<th width="15%">Allergy Type</th>
						<th width="30%">Allergy</th>
						<th width="10%">Onset Date</th>
						<th width="35%">Reaction</th>
						<th width="15%">Severity</th>
						<th width="10%">Status</th>
					</tr>
					[#list allergies as allergy]
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
					[/#list]
				</tbody>
			</table>
		[/#if]
--]
[#setting number_format="#"]
<table width="100%">
	<tr>
		<td>Time In: </td>
		<td>${visitdetails.reg_date} ${visitdetails.reg_time}</td>
		<td rowspan="4" valign="top"></td>
	</tr>
	<tr>
		<td>Time Out: </td>
		<td>[#if consultation_bean.consultation_complete_time?has_content]
				${consultation_bean.consultation_complete_time?string('dd-MM-yyyy HH:mm')}
			[/#if]
		</td>
	</tr>
	<tr>
		<td>Established Patient: </td>
		<td>[#if visitdetails.established_type == 'E']Yes[#else]No[/#if]</td>
	</tr>
</table>
<!-- [#assign hpiIndex = 0] -->
<table width="100%" style="margin-top: 10px" cellspacing="0" cellpadding="0">
	<tr>
		<td width="40%" valign="top">Chief Complaint: </td>
		<td width="60%" valign="top">[#if visitdetails.complaint?exists]${visitdetails.complaint!?html}[/#if]</td>
	</tr>
	<tr>
		<td width="40%" valign="top">Analysis of Complaint: </td>
		<td width="60%" valign="top">${visitdetails.analysis_of_complaint!?html}</td>
	</tr>
	<tr>
		<td width="40%" valign="top">Immunization Status: </td>
		<td width="60%" valign="top">
			[#if (consultation_bean.immunization_status_upto_date!'') == 'Y']
				Yes
			[#elseif (consultation_bean.immunization_status_upto_date!'') == 'N']
				No
			[#else] Not Available
			[/#if]
		</td>
	</tr>
	<tr>
		<td width="40%" valign="top">Immunization Remarks: </td>
		<td width="60%" valign="top">${(consultation_bean.immunization_remarks)!?html} </td>
	</tr>
</table>

<!--[#assign stn_title = ''] -->
<!--[#assign stn_count = 1] -->
<!--[#list triage_insta_sections as tpf] -->
<!--[#if PhysicianForms['sd_' + tpf.section_detail_id]?has_content] -->
		<h3>${tpf.section_title?html}
<!--	[#if stn_title == tpf.section_title] -->
			${'- ' + stn_count?html}
<!--		[#assign stn_count = stn_count + 1] -->
<!--	[/#if] -->
<!--	[#if stn_title != tpf.section_title] -->
<!--		[#assign stn_count = 1] -->
<!--		[#assign stn_title = tpf.section_title] -->
<!--	[/#if] -->
		</h3>
			<table width="100%" cellspacing="0" cellpadding="0" style="margin-top: 10px">
				<tbody>
				<tr>
					<td>
<!--				[#list PhysicianForms['sd_' + tpf.section_detail_id] as field] -->
<!--					[#if field[0].field_type == 'image'] -->
							</td></tr><tr><td>
<!--					[/#if] -->
				 		${field[0].field_name?html}:&nbsp;
				 		<b> <!-- display all values in bold -->
<!--					[#if field[0].field_type == 'image'] -->
					 		<div style="width: 800px; height: 400px; page-break-inside: avoid;
								background-image: url('PhysicianFieldsImage.do?_method=viewImage&amp;field_id=${field[0].field_id}&amp;image_id=${field[0].image_id!0}'); background-repeat:no-repeat;">
<!--					[/#if] -->
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
								</div>
<!--						[#elseif value.field_type == 'date'] -->
								${(value.date?string('dd-MM-yyyy'))!}
<!--						[#elseif value.field_type == 'datetime'] -->
								${(value.date_time?string('dd-MM-yyyy HH:mm'))!}
<!--						[/#if]
						[/#list]  -->
<!--					[#if field[0].field_type == 'image'] -->
							</div>
<!--					[/#if] -->
						</b>
<!--			 	[/#list]  -->
					</td>
	 			</tr>
		 		</tbody>
		 	</table>

<!-- [/#if] -->
<!-- [/#list] -->

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
						<th width="20%">Medicine Name</th>
						<!-- [#assign medicine_name=presMedicines[0].item_name!?html] -->
						<!-- [#assign generic_name=presMedicines[0].generic_name!?html] -->
						<!-- [#if medicine_name == ''] -->
						<th width="10%">Form</th>
						<th width="10%">Strength</th>
						<!-- [/#if] -->
						<th width="10%">Dosage</th>
						<th width="10%">Freq.</th>
						<th width="10%">Duration</th>
						<th width="10%">Qty</th>
						<th width="20%">Remarks</th>
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
							</td>
							<!-- [#if medicine_name == ''] -->
							<td>${medicine.item_form_name!?html}</td>
							<td>${medicine.item_strength!?html} ${medicine.unit_name!?html}</td>
							<!-- [/#if] -->
							<td>${medicine.strength!} ${medicine.consumption_uom!}</td>
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
							<th width="20%">Item Name</th>
							<th width="10%">Form</th>
							<th width="10%">Strength</th>
							<th width="10%">Dosage</th>
							<th width="10%">Freq.</th>
							<th width="10%">Duration</th>
							<th width="10%">Qty</th>
							<th width='20%'>Item Remarks</th>
						</tr>
						<!-- [#list NonHospitalItems as item] -->
						<tr>
							<td >${(item.item_name!)?html}</td>
							<td>${item.item_form_name!?html}</td>
							<td>${item.item_strength!?html} ${item.unit_name!?html}</td>
							<td>${item.strength!} ${item.consumption_uom!}</td>
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
	<!--[#list diagnosis_details as diagnosis] -->
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
	<!--[/#list] -->
	</tbody>
</table>
<!-- [/#if] -->
<table width="100%" style="margin-top: 10px">
	<tr>
		<td width="40%">Physician Signature: </td>
		<td width="10%"></td>
		<td width="30%">Physician Stamp: </td>
		<td width="20%"></td>
	</tr>
	<tr>
		<td width="30%">E/M Code:(Coder) </td>
		<td width="20%"></td>
		<td width="30%">Date: </td>
		<td width="20%"></td>
	</tr>
</table>