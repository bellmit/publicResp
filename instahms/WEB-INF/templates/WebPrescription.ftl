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

<!-- [#list consultation_components.sections?split(",") as formid]
		[#if formid?number == -7] -->
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
								<th width="5%">Duration</th>
								<th width="5%">Qty</th>
								<th width="20%">Instructions</th>
								<th width="10%">Special Instructions</th>
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
								<td>${medicine.strength!} ${medicine.consumption_uom!}</td>
								<td>${(medicine.medicine_dosage!)?html}</td>
								<td>[#if medicine.duration?has_content]${(medicine.duration)} ${(medicine.duration_units)!}[/#if]</td>
								<td>${(medicine.medicine_quantity)!}</td>
								<td>${(medicine.item_remarks!)?html}</td>
								<td>${(medicine.special_instr!)?html}</td>
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
								<th width='30%'>Investigation</th>
								<th width='40%'>Instructions</th>
								<th width="30%">Special Instructions</th>
							</tr>
							<!-- [#list presTests as test] -->
							<tr>
								<td>${(test.item_name!)?html}</td>
								<td>${(test.item_remarks!)?html}</td>
								<td>${(test.special_instr!)?html}</td>
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
								<th width="20%">Special Instructions</th>
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
								<td>${(service.special_instr!)?html}</td>
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
								<th width="30%">Special Instructions</th>
							</tr>
							<!-- [#list presConsultation as consultation] -->
							<tr>
								<td>${(consultation.item_name!)?html}</td>
								<td>${(consultation.item_remarks!)?html}</td>
								<td>${(consultation.special_instr!)?html}</td>
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
								<th width="30%">Special Instructions</th>
							</tr>
							<!-- [#list presOperations as operation] -->
							<tr>
								<td >${(operation.item_name!)?html}</td>
								<td >${(operation.item_remarks!)?html}</td>
								<td>${(operation.special_instr!)?html}</td>
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
										<th width='20%'>Instructions</th>
										<th width="10%">Special Instructions</th>
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
										<td >${(item.item_remarks!)?html}</td>
										<td>${(item.special_instr!)?html}</td>
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
								</tr>
								<tr>
									<td>${(((consultation_bean.prescription_notes!)?html)?replace("\n","<br/>"))?replace("--break--", '<p class="pagebreak"/>')}</td>
								</tr>
							</tbody>
						</table>
					</td>
				</tr>
				<!-- [/#if] -->
				</tbody>
			</table>
			
			<div style="position: relative;width:100%">
				<p style="position:fixed;bottom:20px;font-size:12px;">This is a computer generated document. Collect your hard copy from hospital.</p>
			</div>
<!--	[/#if]
	[/#list] -->
