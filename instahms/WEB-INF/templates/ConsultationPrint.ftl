[#setting number_format="#"]
<div class="patientHeader">
  	<table cellspacing='0' cellpadding='0' width='100%'>
	  	<tbody>
			<!-- [#escape x as x?html] -->
			<tr>
				<td valign='top'>
					<table cellspacing='0' cellpadding='1' width='100%' >
						<tbody>
							<tr>
								<td align='left' valign='top'>Name:</td>
								<td align='left' valign='top'>${visitdetails.full_name!}</td>
							</tr>
							<tr>
								<td align='left' valign='top'>Age/Gender:</td>
								<td align='left' valign='top'>${visitdetails.age!} ${visitdetails.agein!}/${visitdetails.patient_gender!}</td>
							</tr>
							<tr>
								<td align='left' valign='top'>Address:</td>
								<td align='left' valign='top'>${visitdetails.patient_address!}</td>
							</tr>
							<tr>
								<td align='left' valign='top'>Location:</td>
								<td align='left' valign='top'>${visitdetails.cityname!}, ${visitdetails.statename!}</td>
							</tr>
							<tr>
								<td align='left' valign='top'>Doctor:</td>
								<td align='left' valign='top'>${visitdetails.doctor_name!}</td>
							</tr>
							<tr>
								<td align='left' valign='top'>Department:</td>
								<td align='left' valign='top'>${visitdetails.dept_name!}</td>
							</tr>
							<!-- [#if (visitdetails.org_name!'GENERAL')!='GENERAL'] -->
							<tr>
								<td align='left' valign='top'>Rate Plan:</td>
								<td align='left' valign='top'>
										${visitdetails.org_name}
								</td>
							</tr>
							<!-- [/#if] -->
							<!-- [#if ((visitdetails.tpa_name!'')!='')] -->
							<tr>
								<td align='left' valign='top'>Sponsor:</td>
								<td align='left' valign='top'>${visitdetails.tpa_name!}</td>
							</tr>
							<!-- [/#if] -->
						<!-- [#if visitdetails.visit_type == 'i']] -->
						<!-- [#if ((visitdetails.referred_to_hosp!'')!='')] -->
							<tr>
								<td align='left' valign='top'>Referred To:</td>
								<td align='left' valign='top'>${visitdetails.referred_to_hosp!}</td>
							</tr>
						<!-- [/#if] -->
						<!-- [/#if] -->
					</tbody>
					</table>
				</td>
				<td align='right' valign='top' width="45%">
					<table cellspacing='0' cellpadding='1' style='empty-cells:none;width:100%;'>
						<tbody>
							<!-- [#if ((label!'')!='')] -->
								<tr >
									<td align='left' valign='top'>${label1!}</td>
									<td align='left' valign='top'>${labelvalue1!}</td>
								</tr>
							<!-- [/#if] -->
							<!-- [#if ((label2!'')!='')] -->
								<tr >
									<td align='left' valign='top'>${label2!}</td>
									<td align='left' valign='top'>${labelvalue2!}</td>
								</tr>
							<!-- [/#if] -->

							<tr>
								<td align='left' valign='top'>MR No:</td>
								<td align='left' valign='top'>${visitdetails.mr_no}</td>
							</tr>
							<tr>
								<td align='left' valign='top'>Visit ID:</td>
								<td align='left' valign='top'>${visitdetails.patient_id}</td>
							</tr>
							<tr>
								<td align='left' valign='top'>
									[#if (visitdetails.visit_type)! = 'i']Admission Date:
									[#else]Visit Date:
									[/#if]
								</td>
								<td align='left' valign='top'>${visitdetails.reg_date!} ${visitdetails.reg_time!}</td>
							</tr>
							<tr>
								<!-- [#assign disType=(visitdetails.discharge_type)!''] -->
								<td align='left' valign='top'>
								[#if disType == 'Expiry']
									Death Date:
								[#else]Discharge Date:
								[/#if]</td>
								<td align='left' valign='top'>${visitdetails.discharge_date!} ${visitdetails.discharge_time!}
								</td>
							</tr>
							<!--	[#if visitdetails.visit_type = 'i'] -->
							<tr>
								<td align='left' valign='top'>Ward/Bed:</td>
								<td align='left' valign='top'>
									[#if (modules_activated.mod_ipservices!'') == 'Y']
										[#if (visitdetails.alloc_bed_name!'')=='' ](Not Allocated)
										[#else]${(visitdetails.alloc_ward_name)!}/${(visitdetails.alloc_bed_name)!}
										[/#if]
									[#else]
										${(visitdetails.reg_ward_name)!}/${(visitdetails.bill_bed_type)!}
									[/#if]
								</td>
							</tr>
							<!-- [/#if] -->
							<!-- [#if ((visitdetails.refdoctorname!'')!='')] -->
							<tr>
								<td align='left' valign='top' style='width: 40%'>Referred By:</td>
								<td align='left' valign='top' style='width: 60%'>${(visitdetails.refdoctorname)!}</td>
							</tr>
							<!-- [/#if] -->
						</tbody>
					</table>
				</td>
			</tr>
			<!-- [/#escape] -->
		</tbody>
	</table>
  	<table id="consulDoctorTab" cellspacing='0' cellpadding='0' width='100%'>
  		<tr>
  			<td colspan="2">
  				Consulting Doctor: ${(consultation_bean.doctor_name)!?html}
  			</td>
  		</tr>
  	</table>
  	<!-- [#if erx_cons_bean?? && ((erx_cons_bean.erx_reference_no!'')!='')] -->
  	<table cellspacing='0' cellpadding='0' width='100%'>
  		<tr>
  			<td colspan="2">
  				ERx Reference No.: ${(erx_cons_bean.erx_reference_no)!?html}
  			</td>
  		</tr>
  	</table>
  	<!-- [/#if] -->
</div>
<!-- [#if consultation_components.sections?has_content] -->
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
						<th width="15%">Severity</th>
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
<!-- 	[#elseif formid?number == -13 && pregnancyhistories?has_content || formid?number == -13 && pregnancyhistoriesBean?has_content] -->
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
			
<!-- 	[#elseif formid?number == -14 && antenatalKeyCounts?has_content] -->
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
							<td width="30%">${remarks!}</td>

					</tr>
				<!-- [/#list] -->
				</tbody>
			</table>

<!-- 	[#elseif formid?number == -16 && pac_details?has_content] -->
			<h3 style="margin-top: 10px"><u>Pre Anesthesthetic Checkup Details</u></h3>
			<table cellspacing='0' cellpadding='1' style='margin-top: 5px' width="100%">
				<tbody>
					<tr>
						<th width="15%">Doctor</th>
						<th width="20%">Conduction Date</th>
						<th width="20%">Validity Date</th>
						<th width="5%">Outcome</th>
						<th width="40%">Pre-Anaesthetic Evaluation Remarks</th>
					</tr>
					<!--[#list pac_details as pac] -->
						<tr>
							<td valign="top">${pac.doctor_name}</td>
							<td valign="top">${pac.pac_date}</td>
							<td valign="top">${pac.pac_validity}</td>
							<td valign="top">
								[#if pac.status == 'F']
										Fit
								[#elseif pac.status == 'U']
										Unfit
								[/#if]
							</td>
							<td valign="top">${pac.patient_pac_remarks!?html}</td>
						</tr>
					<!--[/#list] -->
				</tbody>
			</table>
<!--    [#elseif formid?number == -17 && (consultation_components.immunization!'') == 'Y'] -->
		    <h3 style="margin-top: 10px"><u>Immunization Details</u></h3>
		    <table cellspacing='0' cellpadding='1' style='margin-top: 5px' width="100%">
		      <tbody>
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
							<td>
								<span>
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
							<td ><div style="width: 100%">
			<!--				[#list PhysicianForms['sd_' + tpf.section_detail_id] as field] -->
			<!--					[#if field[0].field_type == 'image'] -->
										</div></td></tr><tr><td ><div style="width: 100%;">
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
<!--	[#elseif formid?number == -7] -->
			<!-- [#if presMedicines?has_content || presTests?has_content || presServices?has_content
					|| presConsultation?has_content || presOperations?has_content
					|| NonHospitalItems?has_content] -->
				<h3 style="margin-top: 10px"><u>Prescriptions</u></h3>
			<!-- [/#if] -->
			<table cellspacing='0' cellpadding='1' width='100%' align='center' style='margin-top: 5px'>
				<tbody>
				<!-- [#if presMedicinesMap?has_content] -->
				<tr>
					<td>
						<!-- [#list presMedicinesMap?keys as doctorId] -->
						<table cellspacing='0' cellpadding='1' style='margin-top: 10px' width="100%">
							<tbody>
								<!-- Added to add a layer of padding between two prescribed medicine -->
								<tr style='display: block;padding-top:30px'></tr>
								<tr>
									<td valign="top">Department Name:</td>
									<td valign="top" colspan="5">
										<!-- [#if (doctorMap[doctorId]??)] -->
											<!-- [#assign departmentName=doctorMap[doctorId].dept_name!?html] -->
												${departmentName}
										<!-- [#else] -->
												--
										<!-- [/#if] -->
									</td>
								</tr>
								<tr>
									<td valign="top">Doctor:</td>
									<td valign="top" colspan="5">
										<!-- [#if (doctorMap[doctorId]??)] -->
											<!-- [#assign doctorName=doctorMap[doctorId].doctor_name!?html] -->
											${doctorName}
										<!-- [#else] -->
											--
										<!-- [/#if] -->
									</td>
								</tr>
								<!-- Added to add a layer of padding between two prescribed medicine -->
								<tr style='display: block;padding-bottom:5px'></tr>
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
								<th width="5%">Duration</th>
								<th width="5%">Qty</th>
								<th width="10%">Instructions</th>
								<th width="10%">Refills</th>
								<th width="10%">Control Type</th>
								<th width="10%">Priority</th>
								<th width="5%">Start Date</th>
								<th width="5%">End Date</th>
							</tr>
								<!-- [#list presMedicinesMap[doctorId] as medicine] -->
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
								<td>${medicine.strength!} ${medicine.consumption_uom!}</td>
								<td>${(medicine.medicine_dosage!)?html}</td>
								<td>[#if medicine.duration?has_content]${(medicine.duration)} ${(medicine.duration_units)!}[/#if]</td>
								<td>${(medicine.medicine_quantity)!}</td>
								<td>${(medicine.item_remarks!)?html} ${(medicine.special_instr!)?html}</td>
								<td>${(medicine.refills!)?html}</td>
								<td>${(medicine.control_type_name!)?html}</td>
								<td>
									[#if (medicine.priority!'') == 'N' ] Regular
									[#elseif (medicine.priority!'') == 'P'] PRN/SOS
									[#elseif (medicine.priority!'') == 'S'] Stat
									[#elseif (medicine.priority!'') == 'U'] Urgent
									[/#if]
								</td>
							</tr>
							<!-- [/#list] -->
							</tbody>
						</table>
						<!-- [/#list] -->
					</td>
				</tr>
				<!-- [/#if] -->
				<!-- [#if presTests?has_content] -->
				<tr>
					<td>
						<table cellspacing='0' cellpadding='1' style='margin-top: 10px' width="100%">
							<tbody>
							<tr>
								<th width='30%'>Investigation</th>
								<th width='40%'>Instructions</th>
								<th width='30%'>Start Date</th>
							</tr>
							<!-- [#list presTests as test] -->
							<tr>
								<td>${(test.item_name!)?html}</td>
								<td>${(test.item_remarks!)?html} ${(test.special_instr!)?html}</td>
								<td>[#if test.start_datetime?has_content]${test.start_datetime?string('dd-MM-yyyy')!}[/#if]</td>
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
								<th width='30%'>Service Name</th>
								<th width='10%'>Qty</th>
								<th width='10%'>Tooth No(s).</th>
								<th width='30%'>Instructions</th>
								<th width='20%'>Start Date</th>
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
								<td >${(service.item_remarks!)?html} ${(service.special_instr!)?html}</td>
								<td>[#if service.start_datetime?has_content]${service.start_datetime?string('dd-MM-yyyy')!}[/#if]</td>
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
								<th width='30%'>Consultation Name</th>
								<th width='40%'>Instructions</th>
								<th width='20%'>Start Date</th>
							</tr>
							<!-- [#list presConsultation as consultation] -->
							<tr>
								<td>${(consultation.item_name!)?html}</td>
								<td>${(consultation.item_remarks!)?html} ${(consultation.special_instr!)?html}</td>
								<td>[#if consultation.start_datetime?has_content]${consultation.start_datetime?string('dd-MM-yyyy')!}[/#if]</td>
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
								<th width='30%'>Operation Name</th>
								<th width='40%'>Instructions</th>
								<th width='20%'>Start Date</th>
							</tr>
							<!-- [#list presOperations as operation] -->
							<tr>
								<td >${(operation.item_name!)?html}</td>
								<td >${(operation.item_remarks!)?html} ${(operation.special_instr!)?html}</td>
								<td>[#if operation.start_datetime?has_content]${operation.start_datetime?string('dd-MM-yyyy')!}[/#if]</td>
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
										<th width="5%">Duration</th>
										<th width="5%">Qty</th>
										<th width='15%'>Instructions</th>
										<th width="5%">Refills</th>
									</tr>
									<!-- [#list NonHospitalItems as item] -->
									<tr>
										<td >${(item.item_name!)?html}</td>
										<td>${item.item_form_name!?html}</td>
										<td>${item.item_strength!?html} ${item.unit_name!?html}</td>
										<td>${item.strength!} ${item.consumption_uom!}</td>
										<td>${(item.medicine_dosage!)?html}</td>
										<td>[#if item.duration?has_content]${(item.duration)} ${(item.duration_units)!}[/#if]</td>
										<td>${(item.medicine_quantity)!}</td>
										<td >${(item.item_remarks!)?html} ${(item.special_instr!)?html}</td>
										<td>${(item.refills!)?html}</td>
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
<!--	[/#if] -->
<!-- [#if followupDetails?has_content] -->
	<h3 style="margin-top: 10px"><u>Follouw Up Details: </u></h3>
	<table cellspacing='0' cellpadding='1' style='margin-top: 0px' width="100%">
		<tbody>
			<tr>
				<th width="30%">Doctor Name</th>
				<th width="20%">Follow Up Date</th>
				<th width="60%">Remarks</th>
			</tr>
			<!-- [#list followupDetails as item] -->
    			<tr>
					<td>${item.doctor_name}</td>
					<td>${item.followup_date}</td>
					<td>${(item.followup_remarks)!?html}</td>
				</tr>
			<!-- [/#list] -->
		</tbody>
	</table>
<!-- [/#if] -->


<table cellspacing='0' cellpadding='1' style='margin-top: 10px;height:400px;' width="100%">
	<tr>
		<td>
			<div style="height: 0px"> <!--  this is required to mention with 0px height to align the marker at the correct place.-->
				<img src="PatientInsurenceCard.do?_method=getPatientInsureneceCard&amp;visit_id=${visitdetails.patient_id}"/>
			</div>
		</td>
	</tr>
	<tr><td><div>&nbsp;</div></td></tr>
	<tr height="10px"><td >&nbsp;</td></tr>
	<tr>
		<td align='left'><b>Doctor's Signature</b></td>
	</tr>
</table>
