[#-- other available tokens in this print are
1) vitals
2) diagnosis details
3) consultation fields(customizable fields)
4) allergies

to display

1) vitals, use the following code snippet

	[#if vitals?has_content]
		<h3 style="margin-top: 10px"><u>Vitals</u></h3>
		<table cellspacing='0' cellpadding='1' style='margin-top: 5px; empty-cells: show; border-collapse:collapse;' width="100%" >
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
							<td valign="top">${allergy.allergy!?html}</td>
							<td valign="top">${allergy.onset_date!?html}</td>
							<td valign="top">${allergy.reaction!?html}</td>
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

<table style="margin-top: 10px;" cellspacing="0" cellpadding="0">
	<tr>
		<td>Established Patient: </td>
		<td width="20px"></td>
		<td>[#if visitdetails.established_type == 'E']Yes[#else]No[/#if]</td>
	</tr>
<!-- [#if (triage_components.immunization!'') ==	'Y'] -->
	<tr>
		<td valign="top">Immunization Status: </td>
		<td width="20px"></td>
		<td valign="top">
			[#if (consultation_bean.immunization_status_upto_date!'') == 'Y']
				Yes
			[#elseif (consultation_bean.immunization_status_upto_date!'') == 'N']
				No
			[#else] Not Available
			[/#if]
		</td>
	</tr>
	<tr>
		<td valign="top">Immunization Remarks: </td>
		<td width="20px"></td>
		<td valign="top">${(consultation_bean.immunization_remarks)!?html} </td>
	</tr>
<!-- [/#if] -->
</table>

<table style="margin-top: 10px">
	<tbody>
<!-- [#if visitdetails.complaint?has_content] -->
	 <tr>
		<td width="150px">Chief Complaint: </td>
		<td>${(visitdetails.complaint)!?html}</td>
	 </tr>
<!-- [/#if] -->
<!-- [#if secondary_complaints?has_content]
		[#list secondary_complaints as s_complaint] -->
			<tr>
				<td>Other Complaint: </td>
				<td>${s_complaint.complaint!?html}</td>
			</tr>
<!--	[/#list] -->
<!-- [/#if] -->
	</tbody>
</table>


<!-- [#if (triage_components.vitals!'N') == 'Y' && vitals?has_content] -->
	<h3 style="margin-top: 10px"><u>Vitals</u></h3>
	<table cellspacing='0' cellpadding='1' style='margin-top: 5px; empty-cells: show; border-collapse:collapse;' width="100%" >
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
<!-- [/#if] -->
<!-- [#if (triage_components.allergies!'N') == 'Y' && allergies?has_content] -->
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
				<td valign="top">${allergy.allergy!?html}</td>
				<td valign="top">${allergy.onset_date!?html}</td>
				<td valign="top">${allergy.reaction!?html}</td>
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
<!-- [/#if] -->

<!-- [#if pregnancyhistories?has_content ||  pregnancyhistoriesBean?has_content]  -->
			<h3 style="margin-top: 10px"><u>Obstetric History Details</u></h3>
			<table cellspacing='0' cellpadding='1' style='margin-top: 5px' width="100%">
				<tbody>	
		<!-- [#list pregnancyhistoriesBean as pregnancyhistory] -->
			<!-- [#assign pregnancyvariable = false] -->
			<!-- [#if pregnancyhistory.field_g?has_content || pregnancyhistory.field_p?has_content || pregnancyhistory.field_l?has_content || pregnancyhistory.field_a?has_content] -->
				<!-- [#assign pregnancyvariable = true] -->
			<!-- [/#if] -->
				
			<!-- [#if pregnancyvariable = true] -->
					<tr>
						<td width="10%"><b>G</b></td><td width="10%">[#if pregnancyhistory.field_g?has_content]${pregnancyhistory.field_g}[/#if]</td>
						<td width="10%"><b>P</b></td><td width="10%">[#if pregnancyhistory.field_p?has_content]${pregnancyhistory.field_p}[/#if]</td>
					</tr>
					<tr>
						<td width="10%"><b>L</b></td><td width="10%">[#if pregnancyhistory.field_l?has_content]${pregnancyhistory.field_l}[/#if]</td>
						<td width="10%"><b>A</b></td><td width="10%">[#if pregnancyhistory.field_a?has_content]${pregnancyhistory.field_a}[/#if]</td>
					</tr> 
					
			<!-- [/#if] -->
					<tr><td>&nbsp;</td></tr>
		<!--[/#list] -->
			
			
					
					<!--[#list pregnancyhistories as pregnancyhistory] -->
						
						<tr>
						<td width="10%"><b>Date of Delivery</b></td>
						<td width="20%">${pregnancyhistory.date}</td>
	
						

						<td width="25%"><b>Week(Weeks)</b></td>
						<td width="30">[#if pregnancyhistory.weeks?has_content]${pregnancyhistory.weeks}[/#if]</td>
						</tr> 
						
						<tr>
						<td width="10%"><b>Place</b></td>
						<td width="30%">${pregnancyhistory.place?html!}</td>

						

						<td width="10%"><b>Method</b></td>
						<td width="40%">${pregnancyhistory.method?html!}</td>
						</tr>
						
						<tr>
						<td width="10%"><b>Weight(Kg)</b></td>
						<td width="20%">[#if pregnancyhistory.weight?has_content]${pregnancyhistory.weight}[/#if]</td>						

						<td width="10%"><b>Sex</b></td>
						<td width="10%">
								[#if pregnancyhistory.sex == 'M']
										Male
								[#elseif pregnancyhistory.sex == 'F']
										Female
								[#elseif pregnancyhistory.sex == 'O']
										Unknown
								[#else]
								[/#if]
						</td>
						</tr>
						
						<tr>
						<td width="15%"><b>Complications</b></td>
						<td width="10%">${pregnancyhistory.complications?html!}</td>

						
						<td width="10%"><b>Feeding</b></td>
						<td width="30%">${pregnancyhistory.feeding?html!}</td>
						</tr>
						
						<tr>
						<td width="10%"><b>Outcome</b></td>
						<td width="40%">${pregnancyhistory.outcome?html!}</td>
						</tr>
						
						<tr><td>&nbsp;</td></tr>				
						<!--[/#list] -->
						
					</tbody>
			</table>
	<!-- [/#if]  -->
			
<!-- 	[#if antenatalKeyCounts?has_content] -->
			<h3 style="margin-top: 10px"><u>Antenatal Details</u></h3>
			<table width="100%">
				<tbody>
					<!--[#assign pregnencyResult = ''] -->
					<!--[#assign pregnencyResultDate = ''] -->
					<!--[#assign numberOfBirth = ''] -->
					<!--[#assign remarks = ''] -->
					<!-- [#list antenatalKeyCounts as pragnancyCount] -->
						<!-- [#assign count=(pragnancyCount)!] -->
						<!--[#list antenatalinfoMap[pragnancyCount] as antenatal] -->

							<!-- [#assign pregnencyResult=(antenatal.pregnancy_result!)!] -->
							<!-- [#assign pregnencyResultDate=(antenatal.pregnancy_result_date)!] -->
							<!-- [#assign numberOfBirth=(antenatal.number_of_birth)!] -->
							<!-- [#assign remarks=(antenatal.remarks)!] -->

							<!--    [#if pragnancyCount == count]  -->
								<tr>
									<span>
										<td width="20%"><u><b>Pregnancy - ${pragnancyCount!}</b></u></td>
									</span>
								</tr>
								<tr>
									<td width="25%"><b>L.M.P</b>&nbsp; [#if antenatal.lmp?has_content]${antenatal.lmp?string('dd-MM-yyyy')!}[/#if]</td>
									<td width="25%"><b>E.D.D</b>&nbsp; [#if antenatal.edd?has_content]${antenatal.edd?string('dd-MM-yyyy')!}[/#if]</td>
									<td width="50%"><b>F.D.D</b>&nbsp; [#if antenatal.final_edd?has_content]${antenatal.final_edd?string('dd-MM-yyyy')!}[/#if]</td>
								</tr>
								<!-- [#assign count = count + 1 ] -->
							<!--    [/#if]  -->
					
							<tr>
							<td width="20%"><b>Visit Date</b></td> 
							<td width="25%">${antenatal.visit_date}</td>
							
							<td width="20%"><b>Gestation</b></td>
							<td width="35%">[#if antenatal.gestation_age?has_content]${antenatal.gestation_age}[/#if]</td>
							</tr>
							
							<tr>
							<td width="25%"><b>Height of Fundus(cm)</b></td>
							<td width="25%">[#if antenatal.height_fundus?has_content]${antenatal.height_fundus}[/#if]</td>		
								
							<td width="25%"><b>Presentation</b></td>
							<td width="25%">${antenatal.presentation?html!}</td>
							</tr>
							
							<tr>
							<td width="25%"><b>Relation of PP to Brim</b></td>
							<td width="25%">${antenatal.rel_pp_brim?html!}</td>
							
							<td width="25%"><b>Foetal Heart(bpm)</b></td>
							<td width="25%">${antenatal.foetal_heart?html!}</td>
							</tr> 
							
							<tr>
							<td width="25%"><b>Urine</b></td>
							<td width="25%">${antenatal.urine?html!}</td>
							
							<td width="25%"><b>BP(mmHg)</b></td>
							<td width="25%">[#if antenatal.systolic_bp?has_content]${antenatal.systolic_bp}[/#if][#if antenatal.diastolic_bp?has_content]/${antenatal.diastolic_bp}[/#if]</td>
							</tr> 
							
							<tr>
							<td width="20%"><b>Weight(Kg)</b></td>
							<td width="25%">[#if antenatal.weight?has_content]${antenatal.weight}[/#if]</td> 
							
							<td width="20%"><b>Prescription Summary</b></td>
							<td width="35%">${antenatal.prescription_summary?html!}</td>
							</tr>
							
							<tr>
							<td width="25%"><b>Consulting Doctor</b></td>
							<td width="25%">${antenatal.doctor_name!}</td>
							
							<td width="25%"><b>Next Visit Date</b></td>
							<td width="25%">${antenatal.next_visit_date!}</td>
							</tr>
							<tr>
							<td width="25%"><b>Position</b></td>
							<td width="25%">${antenatal.position!}</td>

							<td width="25%"><b>Movement</b></td>
							<td width="25%">${antenatal.movement!}</td>
							</tr>	
						<tr><td>&nbsp;</td></tr>

					<!--[/#list] -->
						<tr>
							<td width="10%"><b>${pregnencyResult!} Date</b></td>
							<td width="20%">${pregnencyResultDate?html!}</td>

							<td width="10%"><b>Number of Birth</b></td>
							<td width="20%">${numberOfBirth}</td>

							<td width="10%"><b>Remarks</b></td>
							<td width="30%">${remarks!?html}</td>

					</tr>
				<!-- [/#list] -->
				</tbody>
			</table>
<!-- [/#if] -->

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
				<td><div style="width: 100%">
<!--				[#list PhysicianForms['sd_' + tpf.section_detail_id] as field] -->
<!--					[#if field[0].field_type == 'image'] -->
							</div></td></tr><tr><td><div style="width: 100%;">
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
								[#if value.allow_others == 'Y' && value.option_id ==-1] -->
									Others [#if value.option_remarks?has_content]-[/#if]${value.option_remarks!?html},
<!--							[#elseif value.allow_normal == 'Y' && value.option_id == 0] -->
									Normal [#if value.option_remarks?has_content]-[/#if]${value.option_remarks!?html},
<!--							[#else] -->
									${value.option_value!?html}[#if value.option_remarks?has_content]-[/#if]${value.option_remarks!?html},
<!--							[/#if]
							[#elseif value.field_type == 'dropdown']
								[#if value.allow_others == 'Y' && value.option_id ==-1] -->
									Others [#if value.option_remarks?has_content]-[/#if]${(value.option_remarks!)?html},
<!--							[#elseif value.allow_normal == 'Y' && value.option_id == 0] -->
									Normal,
<!--							[#else] -->
									${value.option_value!?html},
<!--							[/#if]
							[#elseif value.field_type == 'text' || value.field_type == 'wide text'] -->
								${((((value.option_remarks!)?html)?replace("\n","<br/>"))?replace("--break--", '<p class="pagebreak"/>'))},
<!--						[#elseif value.field_type == 'image' && value.marker_id?has_content] -->
								<div style="height: 0px"> <!--  this is required to mention with 0px height to align the marker at the correct place.-->
									<img src="PhysicianFieldsImageMarkers.do?_method=view&amp;image_id=${value.marker_id}"
										style="top: ${value.coordinate_y!0}px;
										left: ${value.coordinate_x!0}px;position: relative;display:block;"/>
<!-- 								[#if (value.notes!?html)?has_content] -->
										<div style="top: ${value.coordinate_y!0}px;
														left: ${value.coordinate_x!0}px;position: relative;display:block;">${marker_number}</div>
<!--									[#assign marker_number=marker_number+1]
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
				</div>
				</td>
			</tr>
	 		</tbody>
	 	</table>
<!-- [/#if] -->
<!-- [/#list] -->
