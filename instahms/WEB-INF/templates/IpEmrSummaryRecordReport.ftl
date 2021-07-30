<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>IP Record</title>

</head>
<body>
[#setting number_format="#"]
<div align="center" style="font-size: 12pt;"><b>IP EMR Record</b></div>
<div class="patientHeader">
  	[#include "VisitDetailsHeader.ftl"]
</div>
<br/><br/>
<div>
	<!-- [#list ip_record_components.sections?split(",") as formid]
			[#if formid !='' && formid?number == -1 ] -->
				<table style="margin-top: 10px">
				<tbody>
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
				</tbody>
				</table>
	<!--	[#elseif formid !='' && formid?number == -6 && diagnosis_details?has_content] -->
				<h3 style="margin-top: 10px"><u>Diagnosis Details</u></h3>
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
	<!--	[#elseif formid !='' && formid?number == -2 && allergies?has_content] -->
				<h3 style="margin-top: 10px"><u>Allergies Details</u></h3>
				<table cellspacing='0' cellpadding='1' style='margin-top: 5px' width="100%">
					<tbody>
						<tr>
							<th width="15%">Allergy Type</th>
							<th width="30%">Allergy</th>
							<th width="10%">Onset Date</th>
							<th width="35%">Reaction</th>
							<th width="35%">Severity</th>
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
								<td valign="top">${allergy.onset_date?html}</td>
								<td valign="top">${allergy.reaction?html}</td>
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
				
	<!-- 	[#elseif formid !='' && (formid?number == -13 && pregnancyhistories?has_content || formid?number == -13 && pregnancyhistoriesBean?has_content)] -->
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

			
<!-- 	[#elseif formid !='' && formid?number == -14 && antenatalinfo?has_content] -->
			<h3 style="margin-top: 10px"><u>Antenatal Details</u></h3>
			<table width="100%">
				<tbody>
					
					<!--[#list antenatalinfo as antenatal] -->
					
							<tr>
							<td width="20%"><b>Visit Date</b></td> 
							<td width="25%">${antenatal.visit_date}</td>
							
							<td width="35%"><b>Gestation Age</b></td>
							<td width="20%">[#if antenatal.gestation_age?has_content]${antenatal.gestation_age}[/#if]</td>
							</tr>
							
							<tr>
							<td width="25%"><b>Height of Fundus(cm)</b></td>
							<td width="25%">[#if antenatal.height_fundus?has_content]${antenatal.height_fundus}[/#if]</td>		
								
							<td width="20%"><b>Presentation</b></td>
							<td width="25">${antenatal.presentation?html!}</td>
							</tr>
							
							<tr>
							<td width="25%"><b>Relation of PP to Brim</b></td>
							<td width="25%">${antenatal.rel_pp_brim?html!}</td>
							
							<td width="15%"><b>Foetal Heart(bpm)</b></td>
							<td width="25%">${antenatal.foetal_heart?html!}</td>
							</tr> 
							
							<tr>
							<td width="10%"><b>Urine</b></td>
							<td width="25%">${antenatal.urine?html!}</td>
							
							<td width="10%"><b>BP(mmHg)</b></td>
							<td width="25%">[#if antenatal.systolic_bp?has_content]${antenatal.systolic_bp}[/#if][#if antenatal.diastolic_bp?has_content]/${antenatal.diastolic_bp}[/#if]</td>
							</tr> 
							
							<tr>
							<td width="10%"><b>Weight(Kg)</b></td>
							<td width="25%">[#if antenatal.weight?has_content]${antenatal.weight}[/#if]</td> 
							
							<td width="20%"><b>Prescription Summary</b></td>
							<td width="35%">${antenatal.prescription_summary?html!}</td>
							</tr>
							
							<tr>
							<td width="20%"><b>Consulting Doctor</b></td>
							<td width="25%">${antenatal.doctor_name!}</td>
							
							<td width="20%"><b>Next Visit Date</b></td>
							<td width="25%">${antenatal.next_visit_date!}</td>
							</tr>	
							<tr><td>&nbsp;</td></tr>
											
					<!--[/#list] -->
				
				</tbody>
			</table>
	<!--	[#elseif formid !='' && formid?number > 0] -->
			<!--[#assign stn_title = ''] -->
			<!--[#assign stn_count = 1] -->
			<!--[#list insta_sections as tpf] -->
			<!--[#if PhysicianForms['sd_' + tpf.section_detail_id]?has_content && tpf.section_id == formid?number] -->
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
										</div></td></tr><tr><td><div style="width: 100%">
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
							</div>
							</td>
			 			</tr>
				 		</tbody>
				 	</table>
			<!--[/#if] -->
			<!--[/#list] -->
	<!-- [/#if]
		[/#list] -->
</div>
</body>
</html>