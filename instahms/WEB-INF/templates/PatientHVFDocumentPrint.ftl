<!--
1) to display the patient photo use the following img tags
	<img src="/Registration/GeneralRegistrationPatientPhoto.do?_method=viewPatientPhoto&mr_no=${visitdetails.mr_no}" width="150px" height="150px" />

2) to display the vitals use the following code snippet.
	[#if vitals?has_content]
		<h3 style="margin-top: 10px"><u>Vitals</u></h3>
		<table cellspacing='0' cellpadding='1' style='margin-top: 5px' width="100%">
			<tbody>
				[#assign vitalsIndex = 0]
				[#list vitals as vital]
				[#if vitalsIndex%3 == 0]
					<tr>
				[/#if]
						<td width="15%"><b>${vital.param_label!}: </b></td>
						<td>${vital.param_value!} ${vital.param_uom!}</td>
				[#if (vitalsIndex%3 == 2) || (vitals?size-1 == vitalsIndex)]
					</tr>
				[/#if]
				[#assign vitalsIndex = vitalsIndex+1]
				[/#list]
			</tbody>
		</table>
	[/#if]
-->
<div class="patientHeader">
	<!-- [#-- if patient_id exists then displays the visit details header else displays the patientdetails header --] -->
	[#if (patient_id!'')!='']
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
	[#else]
		<table cellspacing='0' cellpadding='2' width='100%'>
			<tbody>
			<!-- [#escape x as x?html] -->
			<tr>
				<td align='left' valign='top' width='10%'>Name:</td>
				<td align='left' valign='top' width='60%'>${visitdetails.full_name!}</td>
				<td align='left' valign='top' width='15%'>MR No:</td>
				<td align='left' valign='top' width='35%'>${visitdetails.mr_no}</td>
			</tr>
			<tr>
				<td align='left' valign='top' width='10%'>Age/Gender:</td>
				<td align='left' valign='top' width='60%'>${visitdetails.age!} ${visitdetails.agein!}/${visitdetails.patient_gender!}</td>
				<td align='left' valign='top' width='15%'>Contact No:</td>
				<td align='left' valign='top' width='35%'>${visitdetails.patient_phone!}</td>
			</tr>
			<!-- [/#escape] -->
			</tbody>
		</table>

	[/#if]
</div>
<div>
	<table cellspacing='0' cellpadding='1' width='100%' align='center' style='margin-top:20px'>
		[#list fieldvalues as docdetails]
			[#assign formTitle=docdetails.title!]
			[#break]
		[/#list]

		<tr><td align='center'><b>${(formTitle)!?html}</b></td></tr>
		<tr height='30px' >
			<td></td>
		</tr>
		<tr>
			<td align='left'>
			<!--	[#if  patientDocDetails.doc_number??] -->
			Document Number:${patientDocDetails.doc_number!}
			<!--	[/#if] -->
			</td>
			<td align="right">
			<!--	[#if  visitdetails.doc_date??] -->
				Document Date: ${visitdetails.doc_date?string('dd-MM-yyyy')}
			<!--	[/#if] -->
			</td>
		</tr>
	<!--	[#list fieldvalues as values] -->
	<!--	[#if ((values.field_value)!'') != ''] -->
			<tr>
				<td><b>${(values.field_name)!?html}</b></td>
			</tr>
			<tr>
				<td ><font style='margin-left: 30px'>${((values.field_value)?html)?replace("\n","<br/>")}</font></td>
			</tr>
	<!--	[/#if] -->
	<!--	[/#list] -->
	</table>

	<!--	[#if imgFieldsList?? ] -->
	<table cellspacing='0' cellpadding='1' width='100%' align='center' style='margin-top:20px'>
	<!--	[#list imgFieldsList as imgFld] -->
		<!-- [#if imgFld.image_url?? && imgFld.image_url != ''] -->
			<tr>
				<td><img src="${(imgFld.image_url)!?html}" width="150px" height="100px" /></td>
			</tr>
			<tr>
				<td>${(imgFld.capture_time)?string('dd-MM-yyyy HH:mm:ss')} ${(imgFld.device_ip)!?html}
				 <!-- ${(imgFld.device_info)!?html} --> </td>
			</tr>
		<!-- [/#if] -->
	<!--	[/#list] -->
	</table>
	<!-- [/#if] -->
</div>
