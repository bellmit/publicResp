[#setting number_format="#"]
[#setting time_format="HH:mm"]
[#setting date_format="dd-MM-yyyy"]
[#setting datetime_format="dd-MM-yyyy HH:mm"]
[#assign emptyList=[]]
[#assign emptyMap={}]
[#assign i = 0]
[#list operations as operationDetails]
	[#assign consultation_components=operationDetails.consultation_components!emptyList]
	[#assign opCompSectionValues=operationDetails.opCompSectionValues!emptyMap]
	[#assign system_components=operationDetails.system_components!emptyMap]
	[#assign surgery_details=operationDetails.surgery_details!emptyList]
	[#assign operation_team_details=operationDetails.operation_team_details!emptyList]
	[#assign opeartionsList=operationDetails.opeartionsList!emptyList]
	[#assign operationNames=operationDetails.operationNames!emptyList]

	<div>
		<!-- [#if opeartionsList?has_content] -->
		<h3 style="margin-top: 10px"><u>Operation Details</u></h3>
			<table cellspacing='0' cellpadding='1' style='margin-top: 5px' width="100%">
				<tbody>
					<tr>
						<th width="15%">Procedure Name</th>
						<th width="15%">Surgery Type</th>
						<th width="70%">Modifier</th>
					</tr>
			<!-- 	[#list opeartionsList as operation] -->
					<tr>
						<td valign="top">${operation.operation_name!?html}</td>
						<td valign="top">
							[#if operation.oper_priority == 'P' ]
								Primary Procedure
							[#else]Secondary Procedure
							[/#if]
						</td>
						<td valign="top">${operation.modifier!?html}</td>
					</tr>
			<!--	[/#list] -->
				</tbody>
			</table>
		<!-- [/#if] -->

		<!-- [#if operation_team_details?has_content] -->
		<h3 style="margin-top: 10px"><u>Operation Team</u></h3>
			<table cellspacing='0' cellpadding='1' style='margin-top: 5px' width="100%">
				<tbody>
					<tr>
						<th width="15%">Doctor Name</th>
						<th width="15%">Role</th>
					</tr>
				<!-- [#list operation_team_details as operationTeam] -->
					<tr>
						<td valign="top">${operationTeam.doctor_name!?html}</td>
						<td valign="top">
						[#switch operationTeam.operation_speciality]
							  [#case "SU"]
							     Surgeon
							     [#break]
							  [#case "ASU"]
							     Asst Surgeon
							     [#break]
							  [#case "COSOPE"]
							     Co-op. Surgeon
							     [#break]
						      [#case "AN"]
						         Anaesthetist
						         [#break]
						      [#case "ASAN"]
						      	 Asst Anaesthetist
						         [#break]
						      [#case "PAED"]
						      	Paediatrician
						      	[#break]
						[/#switch]
						</td>
					</tr>
				<!-- [/#list] -->
				</tbody>
			</table>
		<!-- [/#if] -->
		<!-- [#if operation_anaethesia_details?has_content] -->
		<h3 style="margin-top: 10px"><u>Operation Anaethesia Details</u></h3>
			<table cellspacing='0' cellpadding='1' style='margin-top: 5px' width="100%">
				<tbody>
					<tr>
						<th width="15%">Anaesthesia Type</th>
						<th width="15%">Anaesthesia Start</th>
						<th width="15%">Anaesthesia End</th>
					</tr>
				<!-- [#list operation_anaethesia_details as oae] -->
					<tr>
						<td valign="top">${oae.anesthesia_type_name!?html}</td>
						<td valign="top">${oae.anaesthesia_start}</td>
						<td valign="top">${oae.anaesthesia_end}</td>
					</tr>
				<!-- [/#list] -->
				</tbody>
			</table>
		<!-- [/#if] -->
		<!-- [#if surgery_details.theatre_name?has_content] -->
		<h3 style="margin-top: 10px"><u>Operation Theatre</u></h3>
			<table cellspacing='0' cellpadding='1' style='margin-top: 5px' width="100%">
				<tbody>
					<tr>
						<th width="15%">Theatre Name</th>
					</tr>
					<tr>
						<td valign="top">${surgery_details.theatre_name!?html}</td>
					</tr>
				</tbody>
			</table>
		<!-- [/#if] -->

		<!-- [#if surgery_details?has_content] -->
		<h3 style="margin-top: 10px"><u>Other Details</u></h3>
			<table cellspacing='0' cellpadding='1' style='margin-top: 5px' width="100%">
				<tbody>
					<!-- [#if surgery_details.prescribed_by?has_content] -->
					<tr>
						<td width="120px">Prescribed By: </td>
						<td valign="top">${surgery_details.prescribed_by!?html}</td>
					</tr>
					<!-- [/#if] -->
					<!-- [#if surgery_details.wheel_in_time?has_content || surgery_details.wheel_out_time?has_content] -->
					<tr>
						<td width="120px">Wheel In Time: </td>
						<td valign="top">${surgery_details.wheel_in_time!?html}</td>
						<td width="120px">Wheel Out Time: </td>
						<td valign="top">${surgery_details.wheel_out_time!?html}</td>
					</tr>
					<!-- [/#if] -->
					<!-- [#if surgery_details.surgery_start?has_content || surgery_details.surgery_end?has_content] -->
					<tr>
						<td width="120px">Surgery Start: </td>
						<td valign="top">${surgery_details.surgery_start!?html}</td>
						<td width="120px">Surgery End: </td>
						<td valign="top">${surgery_details.surgery_end!?html}</td>
					</tr>
					<!-- [/#if] -->
					<!-- [#if surgery_details.specimen?has_content] -->
					<tr>
						<td width="120px">Specimen: </td>
						<td valign="top">${surgery_details.specimen!?html}</td>
					</tr>
					<!-- [/#if] -->
					<!-- [#if surgery_details.conduction_remarks?has_content] -->
					<tr>
						<td width="120px">Conduction Remarks: </td>
						<td valign="top">${surgery_details.conduction_remarks!?html}</td>
					</tr>
					<!-- [/#if] -->
				</tbody>
			</table>
		<!-- [/#if] -->

		<!-- [#list system_components?keys as formid]
			 [#if formid?number == -1 ] -->
				<table style="margin-top: 10px">
				<!--[#if visitdetails.complaint?has_content] -->
					<tr>
						<th width="150px">Chief Complaint: </th>
						<td>${(visitdetails.complaint)!?html}</td>
					</tr>
				<!--[/#if] -->
				<!--[#if secondary_complaints?has_content]
						[#list secondary_complaints as s_complaint] -->
						<tr>
							<th>Other Complaint: </th>
							<td>${s_complaint.complaint!?html}</td>
						</tr>
				<!--	[/#list]
					[/#if] -->
				</table>
			<!--[#elseif formid?number == -6 && diagnosis_details?has_content] -->
				<h3 style="margin-top: 10px"><u>Diagnosis Details</u></h3>
				<table cellspacing='0' cellpadding='1' style='margin-top: 5px' width="100%">
					<tbody>
						<tr>
							<th width="15%">Diag. Type</th>
							<th width="15%">Diag. Code</th>
							<th width="70%">Description</th>
						</tr>
					<!-- [#list diagnosis_details as diagnosis] -->
							<tr>
								<td valign="top">
									 [#if diagnosis.diag_type == 'P' ]
										Principal
									 [#elseif diagnosis.diag_type == 'A']Admitting
									 [#else]Secondary
									 [/#if]
								</td>
								<td valign="top">${diagnosis.icd_code!?html}</td>
								<td valign="top">${diagnosis.description!?html}</td>
							</tr>
					<!-- [/#list] -->
					</tbody>
				</table>
		<!-- [/#if]
		     [/#list] -->

		<!-- [#list opCompSectionValues?keys as opeProcId]
			 [#if opeProcId?has_content] -->

			 <h2 align="center" style="margin-top: 10px"><u>${operationNames[opeProcId]?html}</u></h2>

			<!-- [#assign operationMap = opCompSectionValues[opeProcId]]  -->
				<!-- [#list operationMap?keys as fieldTitle]
						[#if fieldTitle?has_content] -->
						<h3>${fieldTitle?html}</h3>
						<table width="100%" cellspacing="0" cellpadding="0" style="margin-top: 10px">
						<tbody>
						<tr>
							<td><div style="width: 100%">
						<!-- [#list operationMap[fieldTitle] as field] -->
							<!-- [#if field[0].field_type == 'image'] -->
										</div></td></tr><tr><td><div style="width: 100%">
							<!--[/#if] -->
						 		${field[0].field_name?html}:&nbsp;
						 		<b> <!-- display all values in bold -->
							<!-- [#if field[0].field_type == 'image'] -->
							 		<div style="width: 800px; height: 400px; page-break-inside: avoid;
										background-image: url('PhysicianFieldsImage.do?_method=viewImage&amp;field_id=${field[0].field_id}&amp;image_id=${field[0].image_id!0}'); background-repeat:no-repeat;">
							<!-- [/#if] -->
							<!-- [#assign marker_number=1] -->
							<!-- [#list field as value]
									[#if value.field_type == 'checkbox']
											[#if value.allow_others == 'Y' && value.option_id ==-1] -->
												Others [#if value.option_remarks?has_content]-[/#if]${value.option_remarks!?html},
										<!--[#elseif value.allow_normal == 'Y' && value.option_id == 0] -->
												Normal [#if value.option_remarks?has_content]-[/#if]${value.option_remarks!?html},
										<!--[#else] -->
												${value.option_value!?html}[#if value.option_remarks?has_content]-[/#if]${value.option_remarks!?html},
										<!-- [/#if]
									[#elseif value.field_type == 'dropdown']
											 [#if value.allow_others == 'Y' && value.option_id ==-1] -->
												Others [#if value.option_remarks?has_content]-[/#if]${(value.option_remarks!)?html},
								   		<!-- [#elseif value.allow_normal == 'Y' && value.option_id == 0] -->
												Normal,
								   		<!-- [#else] -->
												${value.option_value!?html},
							  		 	<!-- [/#if]
									[#elseif value.field_type == 'text' || value.field_type == 'wide text'] -->
											${(value.option_remarks!)?html},
							   <!-- [#elseif value.field_type == 'image' && value.marker_id?has_content] -->
											<div style="height: 0px"> <!--  this is required to mention with 0px height to align the marker at the correct place.-->
												<img src="PhysicianFieldsImageMarkers.do?_method=view&amp;image_id=${value.marker_id}"
													style="top: ${value.coordinate_y!0}px;
													left: ${value.coordinate_x!0}px;position: relative;display:block;"/>
										   <!-- [#if (value.notes!?html)?has_content] -->
													<div style="top: ${value.coordinate_y!0}px;
														left: ${value.coordinate_x!0}px;position: relative;display:block;">${marker_number}</div>
											<!-- [#assign marker_number=marker_number+1]
												[/#if] -->
											</div>
								<!-- [#elseif value.field_type == 'date'] -->
											${(value.date?string('dd-MM-yyyy'))!}
								<!-- [#elseif value.field_type == 'datetime'] -->
											${(value.date_time?string('dd-MM-yyyy HH:mm'))!}
								<!-- [/#if]
								[/#list]  -->
							<!-- [#if field[0].field_type == 'image'] -->
										</div>
										<div style="clear:both"></div>
									<!-- [#assign marker_number=1] -->
									<!-- [#list field as value] -->
										<!-- [#if value.field_type == 'image' && (value.notes!?html)?has_content] -->
												${marker_number} ) ${value.notes?html}<br/>
											<!-- [#assign marker_number=marker_number+1]
											[/#if] -->
									<!-- [/#list] -->
							<!-- [/#if] -->
									</b>
				<!-- [/#list]  -->
							</div>
							</td>
			 			</tr>
				 		</tbody>
				 	</table>
			<!--[/#if]
				[/#list] -->

		<!-- [/#if]
		     [/#list] -->
	</div>
[#assign i = i+1]
[#if operations?size != i]
=============================================================================================================
[/#if]
[/#list]