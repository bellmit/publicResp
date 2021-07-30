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
								<td align='left' valign='top'>Consulting Doctor:</td>
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
  	
</div>

<h4 style="margin-top: 10px;text-align:center">DISCHARGE MEDICATION</h4>

<!-- [#list medicationDetailsMap?keys as doctorId] -->
<table cellspacing='0' cellpadding='1' style='margin-top: 5px' width="100%" >
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
			<th width="30%">Medicine Name</th>
			<th width="10%">Details</th>
			<th width="10%">Route</th>
			<th width="15%">Instructions</th>
			<th width="20%">Special Instructions</th>
			<th width="5%">Qty</th>
		</tr>
		<!--[#list medicationDetailsMap[doctorId] as medicationDetailsList] -->
			<tr>
				<td valign="top">
					<!-- [#assign medicine_name=medicationDetailsList.item_name!?html] -->
					<!-- [#assign generic_name=medicationDetailsList.generic_name!?html] -->
					[#if medicine_name == '']
						${generic_name}
					[#else]
						${medicine_name}
					[/#if]
					<p>Form: ${medicationDetailsList.item_form_name!}</p>
					<p>Strength: ${medicationDetailsList.item_strength!?html} ${medicationDetailsList.unit_name!?html}</p>
					<p>Admin Strength: ${medicationDetailsList.admin_strength!?html}</p>
				</td>
				<td valign="top">[#if medicationDetailsList.medicine_dosage?has_content]${medicationDetailsList.medicine_dosage?html}/[/#if][#if medicationDetailsList.duration?has_content]${medicationDetailsList.duration?html} [/#if] [#if medicationDetailsList.duration_units?has_content]${medicationDetailsList.duration_units?html} [/#if]</td>
				<td valign="top">${medicationDetailsList.route_name!?html}</td>
				<td valign="top">${medicationDetailsList.item_remarks!?html}</td>
				<td valign="top">${medicationDetailsList.special_instr!?html}</td>
				<td valign="top">${medicationDetailsList.medicine_quantity!?html}</td>
			</tr>
			<tr style='display: block;padding-bottom:3px'></tr>
		<!--[/#list] -->
		</tbody>
</table>
<!-- [/#list] -->
