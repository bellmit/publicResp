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
								<td align='left' valign='top'>${patientdetails.full_name!}</td>
							</tr>
							<tr>
								<td align='left' valign='top'>Age/Gender:</td>
								<td align='left' valign='top'>${patientdetails.age!} ${patientdetails.agein!}/${patientdetails.patient_gender!}</td>
							</tr>
							<tr>
								<td align='left' valign='top'>Address:</td>
								<td align='left' valign='top'>${patientdetails.patient_address!}</td>
							</tr>
							<tr>
								<td align='left' valign='top'>Location:</td>
								<td align='left' valign='top'>${patientdetails.cityname!}, ${patientdetails.statename!}</td>
							</tr>
							<tr>
								<td align='left' valign='top'>Doctor:</td>
								<td align='left' valign='top'>${patientdetails.doctor_name!}</td>
							</tr>
							<tr>
								<td align='left' valign='top'>Department:</td>
								<td align='left' valign='top'>${patientdetails.dept_name!}</td>
							</tr>
							<!-- [#if (patientdetails.org_name!'GENERAL')!='GENERAL'] -->
							<tr>
								<td align='left' valign='top'>Rate Plan:</td>
								<td align='left' valign='top'>
										${patientdetails.org_name}
								</td>
							</tr>
							<!-- [/#if] -->
							<!-- [#if ((patientdetails.tpa_name!'')!='')] -->
							<tr>
								<td align='left' valign='top'>Sponsor:</td>
								<td align='left' valign='top'>${patientdetails.tpa_name!}</td>
							</tr>
							<!-- [/#if] -->
						<!-- [#if patientdetails.visit_type == 'i']] -->
						<!-- [#if ((patientdetails.referred_to_hosp!'')!='')] -->
							<tr>
								<td align='left' valign='top'>Referred To:</td>
								<td align='left' valign='top'>${patientdetails.referred_to_hosp!}</td>
							</tr>
						<!-- [/#if] -->
						<!-- [/#if] -->
					</tbody>
					</table>
				</td>
				<td align='right' valign='top' width="50%">
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
								<td align='left' valign='top'>${patientdetails.mr_no}</td>
							</tr>
							<tr>
								<td align='left' valign='top'>Visit ID:</td>
								<td align='left' valign='top'>${patientdetails.patient_id}</td>
							</tr>
							<tr>
								<td align='left' valign='top'>
									[#if (patientdetails.visit_type)! = 'i']Admission Date:
									[#else]Visit Date:
									[/#if]
								</td>
								<td align='left' valign='top'>${patientdetails.reg_date!} ${patientdetails.reg_time!}</td>
							</tr>
							<tr>
								<!-- [#assign disType=(patientdetails.discharge_type)!''] -->
								<td align='left' valign='top'>
								[#if disType == 'Expiry']
									Death Date:
								[#else]Discharge Date:
								[/#if]</td>
								<td align='left' valign='top'>${patientdetails.discharge_date!} ${patientdetails.discharge_time!}
								</td>
							</tr>
							<!--	[#if patientdetails.visit_type = 'i'] -->
							<tr>
								<td align='left' valign='top'>Ward/Bed:</td>
								<td align='left' valign='top'>
									[#if (modules_activated.mod_ipservices!'') == 'Y']
										[#if (patientdetails.alloc_bed_name!'')=='' ](Not Allocated)
										[#else]${(patientdetails.alloc_ward_name)!}/${(patientdetails.alloc_bed_name)!}
										[/#if]
									[#else]
										${(patientdetails.reg_ward_name)!}/${(patientdetails.bill_bed_type)!}
									[/#if]
								</td>
							</tr>
							<!-- [/#if] -->
							<!-- [#if ((patientdetails.refdoctorname!'')!='')] -->
							<tr>
								<td align='left' valign='top' style='width: 40%'>Referred By:</td>
								<td align='left' valign='top' style='width: 60%'>${(patientdetails.refdoctorname)!}</td>
							</tr>
							<!-- [/#if] -->
							[#if pbm_request_id?has_content]
							<tr>
								<td align='left' valign='top' style='width: 40%'>PBM Request ID:</td>
								<td align='left' valign='top' style='width: 60%'>${(pbm_request_id)!}</td>
							</tr>
							[/#if]
							[#if approval_status?has_content]							
							<tr>
								<td align='left' valign='top' style='width: 40%'>PBM Approval Status:</td>
								[#if approval_status = 'F']
									<td align='left' valign='top' style='width: 60%'>Fully Approved</td>
								[#elseif approval_status = 'P']
									<td align='left' valign='top' style='width: 60%'>Partially Approved</td>
								[#elseif approval_status = 'R']
									<td align='left' valign='top' style='width: 60%'>Fully Rejected</td>
								[#elseif approval_status = 'S']
									<td align='left' valign='top' style='width: 60%'>Pending...</td>
								[#else]
									<td align='left' valign='top' style='width: 60%'></td>
								[/#if]								
							</tr>
							[/#if]
							[#if approval_comments?has_content]
							<tr>
								<td align='left' valign='top' style='width: 40%'>Rejection Details:</td>
								<td align='left' valign='top' style='width: 60%'>${(approval_comments)!}</td>
							</tr>
							[/#if]
						</tbody>
					</table>
				</td>
			</tr>
			<!-- [/#escape] -->
		</tbody>
	</table>

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
								<td>${(medicine.frequency!)?html}</td>
								<td>[#if medicine.duration?has_content]${(medicine.duration)} ${(medicine.duration_units)!}[/#if]</td>
								<td>${(medicine.medicine_quantity)!}</td>
								<td>${(medicine.medicine_remarks!)?html}</td>
							</tr>
							<!-- [/#list] -->
							</tbody>
						</table>
					</td>
				</tr>
			<!--[/#if] -->
		</tbody>
	</table>
</div>