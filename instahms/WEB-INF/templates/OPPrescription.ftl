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
								<td align='left' valign='top'>${visitdetails.salutation!} ${visitdetails.patient_name!} ${visitdetails.last_name!}</td>
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
</div>
<!-- this block of code inserts the attached images for that consultation -->
[#if consultImages?exists]
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
[/#if]
<!-- end of attaching images to the prescription -->
<!--escape sequence  escape x as x?html for the below division  is removed and ?html is applied individually -->
<div>
	<!-- [#assign empty=[] ] -->
	<table cellspacing='0' cellpadding='1' width='100%' align='center' style='margin-top: 20px'>
		<tbody>
		<!-- [#if consultation_bean.diagnosis?has_content] -->
		<tr>
			<td ><b>Diagnosis: </b>${(((consultation_bean.diagnosis!)?html)?replace("\n","<br/>"))?replace("--break--", '<p class="pagebreak"/>')}</td>

		</tr>
		<!-- [/#if] -->
		<tr>
			<td>&nbsp;</td>
		</tr>
		<!-- [#if consultation_bean.description?has_content] -->
		<tr>
			<td><b>Description: </b>${(((consultation_bean.description!)?html)?replace("\n","<br/>"))?replace("--break--", '<p class="pagebreak"/>')}</td>
		</tr>
		<!-- [/#if] -->
		<!-- [#if ((presMedicines!empty)[0]!'')!=''] -->
		<tr>
			<td>
				<table cellspacing='0' cellpadding='1' style='margin-top: 10px' width="100%">
					<tbody>
					<tr>
						<th >Medicine Name</th>
						<th width="20%">Dosage</th>
						<th width="15%">No. of Days</th>
						<th width="10%">Qty</th>
						<th width="10%">Remarks</th>
					</tr>
					<!-- [#list presMedicines as medicine] -->
					<tr>
						<td>${(medicine.medicine_name!)?html}</td>
						<td>${(medicine.medicine_dosage!)?html}</td>
						<td>${(medicine.medicine_days)!}</td>
						<td>${(medicine.medicine_quantity)!}</td>
						<td>${(medicine.medicine_remarks!)?html}</td>
					</tr>
					<!-- [/#list] -->
					</tbody>
				</table>
			</td>
		</tr>
		<!-- [/#if] -->
		<!-- [#if ((presTests!empty)[0]!'')!=''] -->
		<tr>
			<td>
				<table cellspacing='0' cellpadding='1' style='margin-top: 10px' width="100%">
					<tbody>
					<tr>
						<th width='40%'>Test Name</th>
						<th width='60%'>Remarks</th>
					</tr>
					<!-- [#list presTests as test] -->
					<tr>
						<td>${(test.test_name!)?html}</td>
						<td>${(test.test_remarks!)?html}</td>
					</tr>
					<!-- [/#list] -->
					</tbody>
				</table>
			</td>
		</tr>
		<!-- [/#if] -->
		<!-- [#if ((presServices!empty)[0]!'')!=''] -->
		<tr>
			<td>
				<table cellspacing='0' cellpadding='1' style='margin-top: 10px;' width="100%" >
					<tbody>
					<tr>
						<th width='40%'>Service Name</th>
						<th width='60%'>Remarks</th>
					</tr>
					<!-- [#list presServices as service] -->
					<tr>
						<td >${(service.service_name!)?html}</td>
						<td >${(service.service_remarks!)?html}</td>
					</tr>
					<!-- [/#list] -->
					</tbody>
				</table>
			</td>
		</tr>
		<!-- [/#if] -->
		<tr>
			<td>

				<table cellspacing='0' cellpadding='1' style='margin-top: 10px' width="100%">
					<tbody>
						<!-- [#if ((printDoctorNotes) && (consultation_bean.doctor_notes?has_content))] -->
						<tr>
							<td><b>Doctor Notes: </b><br/>
								<span style="margin-left: 10px">${(((consultation_bean.doctor_notes!)?html)?replace("\n","<br/>"))?replace("--break--", '<p class="pagebreak"/>')}</span></td>
						</tr>
						<!-- [/#if] -->

						<!-- [#if consultation_bean.remarks?has_content] -->
						<tr>
							<td><b>Other Instructions: </b><br/>
								<span style="margin-left: 10px">${(((consultation_bean.remarks!)?html)?replace("\n","<br/>"))?replace("--break--", '<p class="pagebreak"/>')}</span></td>
						</tr>
						<!-- [/#if] -->
						<!-- [#if followup_date?has_content] -->
						<tr height="10px"><td >&nbsp;</td></tr>
						<tr>
							<td align="left"><b>Follow Up Date: </b>${followup_date!}</td>
						</tr>
						<!-- [/#if] -->
						<tr height="10px"><td >&nbsp;</td></tr>
						<tr>
							<td align='left'><b>Doctor's Signature</b></td>
						</tr>
					</tbody>
				</table>
			</td>
		</tr>
		</tbody>
	</table>
</div>
