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
						<td align='left' valign='top'>
							[#if disType == 'Expiry']
								${visitdetails.death_date!} ${visitdetails.death_time!}
							[#else]
								${visitdetails.discharge_date!} ${visitdetails.discharge_time!}
							[/#if]
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

<div>
	<!-- [#assign empty=[] ] -->
	<table cellspacing='0' cellpadding='0' width='100%' align='center' style='margin-top:20px'>
		<tbody>
		<tr><td align='center' colspan="2"><b>${form_title?html}</b></td></tr>
		<tr height='30px' >
			<td></td>
		</tr>
	<!--	[#list fieldvalues as values] -->
	<!--	[#if ((values.field_value)!'') != ''] -->
			<tr>
				<td colspan="2"><b>${(values.caption)?html}</b></td>
			</tr>
			<tr>
				<td colspan="2">&nbsp;</td>
			</tr>
			<tr>
				<td width="30px"></td>
				<td>
					${(((values.field_value)?html)?replace("\n","<br/>"))?replace("--break--", '<p class="pagebreak"/>')}
				</td>
			</tr>
			<tr>
				<td colspan="2">&nbsp;</td>
			</tr>
	<!--	[/#if] -->
	<!--	[/#list] -->
		</tbody>
	</table>
</div>

<div id="followupDetailsDiv">

	<!-- [#list dischargedetails as followupdetails] -->
		<!-- [#assign fdetails=(followupdetails.followup_doctorname)!'notexists'] -->
		<!-- [#break] -->
	<!-- [/#list]-->

	<table cellspacing='0' cellpadding='0' width='100%' align='center' style='margin-top:20px'>
		<tbody>
			<!-- [#if fdetails!='notexists'] -->
			<tr>
				<td align='left' colspan='3'><b>Follow Up Details</b></td>
			</tr>
			<tr style='height: 10px'>
				<td width='5px' colspan='3'></td>
			</tr>
			<!-- [#list dischargedetails as followupdetails] -->
				<tr>
					<td width='15%'>${(followupdetails.followup_date)!}</td>
					<td width='25%'>${((followupdetails.followup_doctorname)!)?html}</td>
					<td width='60%'>${((followupdetails.followup_remarks)!)?html}</td>
				</tr>
			<!-- [/#list] -->
			<tr style='height: 15px'>
				<td></td>
			</tr>
			<!-- [/#if] -->
			<!-- [#if visitdetails.signatory_username?has_content] -->
				<tr>
					<td align="left" colspan='3'><img src="UserImage.do?_method=view&amp;doctor_id=${visitdetails.signatory_username}"/></td>
				</tr>
			<!-- [/#if] -->
			<tr>
				<td align='left' colspan='3'><b>Doctor's Signature</b></td>
			</tr>
			<tr>
				<td colspan="3">
				<!--	[#list dischargedetails as doctors] -->
						${((doctors.discharge_doctor)!)?html} ${((doctors.discharge_doctor_specialization)!)?html}
				<!--		[#break] -->
				<!--	[/#list] -->
				</td>
			</tr>
		</tbody>
	</table>
</div>
