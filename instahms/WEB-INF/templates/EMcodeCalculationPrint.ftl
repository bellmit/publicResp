[#setting number_format="#"]
<html>
	<body>
	[#escape x as x?html]
		<div class="patientHeader">
		  	<table cellspacing='0' cellpadding='0' width='100%'>
			  	<tbody>
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
				</tbody>
			</table>
		  	<table id="consulDoctorTab" cellspacing='0' cellpadding='0' width='100%'>
		  		<tr>
		  			<td colspan="2">
		  				Consulting Doctor: ${(consultation_bean.doctor_name)!?html}
		  			</td>
		  		</tr>
		  	</table>
		</div>
		<table width="100%">
			<tr>
				<td width="35%">&nbsp;</td>
				<td width="5%">&nbsp;</td>
				<td width="5%">&nbsp;</td>
				<td width="55%">&nbsp;</td>
			</tr>
			<tr>
				<td align="center" colspan="3">E&amp;M code calculation</td>
			</tr>
				[#assign visitTypeText={"N":"New","E":"Established","M":"Emergency"}]
			<tr>
				<td style="text-align: right; padding-top: 23px">Visit Type :</td>
				<td colspan="3" style="padding-left: 40px; padding-top: 23px">${visitTypeText[eandmDetailsMap.visit_type]!''}</td>
			</tr>
			<tr>
				<td style="text-align: right" colspan="2">-------------------------</td>
			</tr>
			<tr>
				<td style="text-align: right">HPI :</td>
				<td style="text-align: right">${eandmDetailsMap.hpi_count!}</td>
			</tr>
			<tr>
				<td style="text-align: right">ROS :</td>
				<td style="text-align: right">${eandmDetailsMap.ros_count!}</td>
			</tr>
			<tr>
				<td style="text-align: right">PFSH :</td>
				<td style="text-align: right">${eandmDetailsMap.pfsh_count!}</td>
			</tr>
			<tr>
				[#assign historyCount=eandmDetailsMap.hpi_count + eandmDetailsMap.ros_count + eandmDetailsMap.pfsh_count]
				<td style="text-align: right; padding-top: 12px">History Count :</td>
				<td style="text-align: right; padding-top: 12px">${historyCount}</td>
			</tr>
			<tr>
				<td style="text-align: right" colspan="2">-------------------------</td>
			</tr>
			<tr>
				<td style="text-align: right">Physical Examination Count :</td>
				<td style="text-align: right">${eandmDetailsMap.pe_count!}</td>
			</tr>
			<tr>
				<td style="text-align: right" colspan="2">-------------------------</td>
			</tr>
			<tr>
				[#assign problemStatus={"SM":1,"EI":1,"EW":2,"NN":3,"NW":4}]
				[#assign problemStatusText={"SM":"Self-limited or minor (stable, improved or worsening)",
					"EI":"Established Problem (to examiner) stable, improved","EW":"Established problem (to examiner) worsening",
					"NN":"New problem (to examiner) no additional workup planned","NW":"New problem (to examiner), Additional workup planned"}]

				<td style="text-align: right">Problem Status :</td>
				<td colspan="3" style="padding-left: 40px">${(problemStatusText[eandmDetailsMap.problem_status])!''}</td>
			</tr>
			<tr>
				<td style="text-align: right">Treatment Options Count :</td>
				<td style="text-align: right">${eandmDetailsMap.treatment_options_count!}</td>
			</tr>
			<tr>
				<td style="text-align: right; padding-top: 12px">Calculated Treatment Options :</td>
				<td style="text-align: right">${eandmDetailsMap.calculated_treatment_options_count!}</td>
			</tr>
			<tr>
				<td style="text-align: right" colspan="2">------------------------------------------------</td>
			</tr>
			[#if eandmDetailsMap.complexity1?has_content]
				<tr>
					<td colspan="4" style="padding-left: 40px">Review and/or order of any Laboratory tests</td>
				</tr>
			[/#if]

			[#if eandmDetailsMap.complexity2?has_content]
				<tr>
					<td colspan="4" style="padding-left: 40px">Review and or Order of tests in Radiology</td>
				</tr>
			[/#if]

			[#if eandmDetailsMap.complexity3?has_content]
				<tr>
					<td colspan="4" style="padding-left: 40px">Review and or order of tests in the Medicine section of CPT</td>
				</tr>
			[/#if]

			[#if eandmDetailsMap.complexity4?has_content]
				<tr>
					<td colspan="4" style="padding-left: 40px">Discussion of Test results with the performing physician</td>
				</tr>
			[/#if]

			[#if eandmDetailsMap.complexity5?has_content]
				<tr>
					<td colspan="4" style="padding-left: 40px">Decision to obtain old records and or obtain history from someone other than patient</td>
				</tr>
			[/#if]

			[#if eandmDetailsMap.complexity6?has_content]
				<tr>
					<td colspan="4" style="padding-left: 40px">Review and summarization of old records and/ or obtaining history from
									some one other than the patient and/or discussion of case with another health care provider </td>
				</tr>
			[/#if]

			[#if eandmDetailsMap.complexity7?has_content]
				<tr>
					<td colspan="4" style="padding-left: 40px">Independent visualization of image, specimen or tracing</td>
				</tr>
			[/#if]

			<tr>
				<td style="text-align: right; padding-top: 13px">Data Complexity :</td>
				<td style="text-align: right; padding-top: 13px">${eandmDetailsMap.data_amount_complexity_count!}</td>
			</tr>
			<tr>
				<td style="text-align: right" colspan="2">-------------------------</td>
			</tr>
				[#assign riskEvalText={"1": "Minimum", "2":"Low", "3":"Moderate", "4":"High"}]
			<tr>
				<td style="text-align: right">Risk Evaluation :</td>
				<td colspan="2" style="padding-left: 40px">${(riskEvalText[eandmDetailsMap.risk_count?string])!''}</td>
			</tr>
			<tr>
				<td style="text-align: right" colspan="2">-------------------------</td>
			</tr>
			<tr>
				[#assign mdmMap={"1":"Straight Forward", "2":"Low", "3":"Moderate", "4":"High"}]
				<td style="text-align: right">MDM Value :</td>
				<td style="text-align: left; padding-left: 40px" colspan="2">${(mdmMap[eandmDetailsMap.mdm_value?string])!''}</td>
			</tr>
			<tr>
				<td style="text-align: right" colspan="2">-------------------------</td>
			</tr>
			<tr>
				<td style="text-align: right">E&amp;M Code :</td>
				<td style="text-align: left; padding-left: 40px" colspan="2">${eandmDetailsMap.em_code!}</td>
			</tr>
			<tr>
				<td style="text-align: right" colspan="2">======================</td>
			</tr>
		</table>
		[/#escape]
	</body>
</html>
