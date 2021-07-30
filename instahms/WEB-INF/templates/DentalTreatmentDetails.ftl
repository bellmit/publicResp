[#setting number_format="#"]
<div class="patientHeader">
  	<table cellspacing='0' cellpadding='0' width='100%'>
	  	<tbody>
			<!-- [#escape x as x?html] -->
				<tr>
					<td align='left' valign='top'>Name:</td>
					<td align='left' valign='top'>${patientdetails.full_name!}</td>
					<td align='left' valign='top'>MR No:</td>
					<td align='left' valign='top'>${patientdetails.mr_no}</td>
				</tr>
				<tr>
					<td align='left' valign='top'>Age/Gender:</td>
					<td align='left' valign='top'>
						[#if patientdetails.dateofbirth?has_content]
							[#assign date_of_birth="(${patientdetails.dateofbirth})"]
						[/#if]
						[#if patientdetails.patient_gender?has_content]
							[#if patientdetails.patient_gender == 'M']
								[#assign gender='Male']
							[#else]
								[#assign gender='Female']
							[/#if]
						[/#if]
						${patientdetails.age_text} ${date_of_birth!} / ${gender}
					</td>
					<td align='left' valign='top' width='15%'>Contact No:</td>
					<td align='left' valign='top' width='35%'>${patientdetails.patient_phone!}</td>
				</tr>
			<!-- [/#escape] -->
		</tbody>
	</table>
</div>
<!-- [#if dentalChartPref == 'Y'] -->
<h3 style="margin-top: 10px"><u>Dental Chart</u></h3>
<div id="dental_chart_image" onclick="updateDentalChartXY(event);"
	style="width: 800px; height: 400px;
	background-image: url('/DentalConsultation/Consultation.do?_method=getDentalChart&amp;age=${patientdetails.age}&amp;age_in=${patientdetails.agein}');
	background-repeat:no-repeat;">
<!--	[#list dental_chart_markers as marker] -->
		<div style="height: 0px" id="dental_chart_marker_div">
<!--		[#assign toothParts = (tooth_image_details.teeth)[marker.unv_number].toothPart]
			[#assign keys=toothParts?keys]
			[#list keys as part]
				[#if part == marker.tooth_part]
					[#assign dc_pos_x=toothParts[part].pos_x]
					[#assign dc_pos_y=toothParts[part].pos_y]
				[/#if]
			[/#list] -->
			<img src="/DentalConsultation/Consultation.do?getDentalChartMarkerImage&amp;mr_no=${patientdetails.mr_no}
				&amp;dc_unv_number=${marker.unv_number!0}&amp;dc_material_id=${marker.material_id!0}&amp;dc_status_id=${marker.status_id!0}
				&amp;dc_tooth_part=${marker.tooth_part}" name="dc_marker_image" id="dc_marker_image" style="
				top: ${dc_pos_y}px;
				left: ${dc_pos_x}px; position:relative;display:block;z-index:2;"/>
		</div>
<!--	[/#list] -->
</div>
<!-- [/#if]
[#if treatments?has_content] -->
<h3 style="margin-top: 10px"><u>Treatment Details</u></h3>
<table width="100%" cellspacing="0" cellpadding="0">
	<tr>
		<th>Tooth</th>
		<th>Service Name</th>
		<th>Status</th>
		<th>Planned By</th>
		<th>Planned Date</th>
		<th>Start Date</th>
		<th>Completed Date</th>
		<th>Completed By</th>
		<th>Ordered Status</th>
		<th>Service Subtask Status</th>
		<th>Comments</th>
	</tr>
<!-- [#list treatments as treatment] -->
	<tr>
		<td>
			[#if treatment.tooth_unv_number?has_content]
				${treatment.tooth_unv_number!}
			[#else]
				${treatment.tooth_fdi_number!}
			[/#if]
		</td>
		<td>${treatment.service_name!?html}</td>
		<td>
			[#if treatment.treatment_status == 'P']
				Planned
			[#elseif treatment.treatment_status == 'C']
				Completed
			[#elseif treatment.treatment_status == 'I']
				In Progress
			[#elseif treatment.treatment_status == 'N']
				Patient No Show
			[#else]
				Cancelled
			[/#if]
		</td>
		<td>${treatment.planned_by_name!}</td>
		<td>${treatment.planned_date?string('dd-MM-yyyy HH:mm')}</td>
		<td>[#if treatment.start_date?has_content]${treatment.start_date?string('dd-MM-yyyy HH:mm')}[/#if]</td>
		<td>[#if treatment.completed_date?has_content]${treatment.completed_date?string('dd-MM-yyyy HH:mm')}[/#if]</td>
		<td>${treatment.completed_by_name!}</td>
		<td>
			[#if treatment.service_prescribed_id?has_content]
				Ordered
			[#else]
				Order
			[/#if]
		</td>
		<td>
			[#assign complCount = 0]
			[#assign hasTasks = false]
			[#if service_sub_tasks['TRTMT'+treatment.treatment_id]??]
				[#list service_sub_tasks['TRTMT'+treatment.treatment_id] as task]
					[#assign hasTasks = true]
					[#if task.status == 'C' || task.status == 'NR']
						[#assign complCount = complCount+1]
					[/#if]
				[/#list]
			[/#if]
			[#assign taskStatus = 'None']
			[#if hasTasks]
				[#assign taskCount = service_sub_tasks['TRTMT'+treatment.treatment_id]?size]
				[#if complCount == 0]
					[#assign taskStatus = 'Not Completed']
				[#elseif complCount > 0 && complCount == taskCount]
					[#assign taskStatus = 'Completed']
				[#elseif complCount > 0 && complCount < taskCount]
					[#assign taskStatus = 'Partial']
				[/#if]
			[/#if]
			${taskStatus}
		</td>
		<td>${treatment.comments}</td>
	</tr>
<!-- [/#list] -->
</table>
<!-- [/#if] -->

<h3 style="margin-top: 10px"><u>Prescriptions</u></h3>
<!-- [#if presMedicines?has_content] -->
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
			</td>
			<!-- [#if medicine_name == ''] -->
			<td>${medicine.item_form_name!?html}</td>
			<td>${medicine.item_strength!?html} ${medicine.unit_name!?html}</td>
			<!-- [/#if] -->
			<td>${(medicine.frequency!)?html}</td>
			<td>[#if medicine.duration?has_content]${(medicine.duration)?string("0")} ${medicine.duration_units}[/#if]</td>
			<td>${(medicine.medicine_quantity)!}</td>
			<td>${(medicine.medicine_remarks!)?html}</td>
		</tr>
		<!-- [/#list] -->
		</tbody>
	</table>
<!-- [/#if] -->
<!-- [#if presTests?has_content] -->
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

<!-- [/#if] -->
<!-- [#if presServices?has_content] -->
		<table cellspacing='0' cellpadding='1' style='margin-top: 10px;' width="100%" >
			<tbody>
			<tr>
				<th width='40%'>Service Name</th>
				<th width='10%'>Qty</th>
				<th width='40%'>Remarks</th>
			</tr>
			<!-- [#list presServices as service] -->
			<tr>
				<td >${(service.item_name!)?html}</td>
				<td>${service.medicine_quantity!1}</td>
				<td >${(service.item_remarks!)?html}</td>
			</tr>
			<!-- [/#list] -->
			</tbody>
		</table>
<!-- [/#if] -->