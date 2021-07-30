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
</div>
<table style="margin-top: 10px">
	<tbody>
		<tr>
			<th width="5%">Type</th>
			<th width="30%">Name</th>
			<th width="5%">Dosage</th>
			<th width="5%">Frequency</th>
			<th width="5%">Route</th>
			<th width="10%">Start</th>
			<th width="10%">End</th>
			<th width="30%">Remarks</th>
		</tr>
			[#assign doctor='']
			[#list orders?sort_by("doctor_name") as order]
			[#if doctor='' || doctor != order.doctor_name]
			<tr>
				<td colspan="8">
					<font style="font-weight: bold">${order.doctor_name}</font>
				</td>
			</tr>
			[/#if]
			[#assign doctor=order.doctor_name]
			<tr>
	<!--			[#if order.item_type = 'Medicine']
					[#assign item_name=order.item_name]
					[#if order.item_form_name?has_content]
						[#assign item_name=item_name+'/'+order.item_form_name]
					[/#if]
					[#if order.item_strength?has_content]
						[#assign item_name=item_name+'/'+order.item_strength+' '+order.unit_name!]
					[/#if]
					[#else]
						[#assign item_name=order.item_name]
					[/#if]
					
					[#assign infusion_interval_units='']							
					[#if order.infusion_period_units! = 'M']
						[#assign infusion_interval_units='minutes']
					[#elseif order.infusion_period_units! = 'H']
						[#assign infusion_interval_units='hours']
					[/#if]
							
					[#assign interval_units='']							
					[#if order.repeat_interval_units! = 'M']
						[#assign interval_units='Minutes']
					[#elseif order.repeat_interval_units! = 'H']
						[#assign interval_units='Hours']
					[#elseif order.repeat_interval_units! = 'D']
						[#assign interval_units='Days']
					[/#if] -->
							
					<td>${order.item_type}
					[#if order.medication_type?has_content && order.medication_type = 'IV']
						(IV Fluid)
					[/#if]
					</td>
					<td>${item_name!?html}</td>
					<td>${order.strength!}${order.consumption_uom!}</td>
					<td>
						[#if order.freq_type = 'F']
							${order.recurrence_name!}
						[#else]
							[#assign interval=order.repeat_interval+' '+interval_units]
							${interval}
						[/#if]
						[#if order.max_doses?has_content]
						(Max Dose -${order.max_doses}/day)
						[/#if]
					</td>
					<td>${order.route_name!}</td>
					<td>
						[#if order.start_datetime?has_content]
							${order.start_datetime?string('dd-MM-yyyy HH:mm')}
						[/#if]
					</td>
					<td>
						[#if order.end_datetime?has_content]
							${order.end_datetime?string('dd-MM-yyyy HH:mm')}
						[#elseif order.no_of_occurrences?has_content]
							${order.no_of_occurrences} time(s)
						[#elseif order.end_on_discontinue?has_content]
							Till Discontinued
						[/#if]
					</td>
					<td>${order.item_remarks!?html}
					[#if order.flow_rate?has_content]<br/>
						[#assign flow_rate = order.flow_rate+' '+order.flow_rate_units]
						<font style="font-weight: bold">Flow Rate: </font>
						: ${flow_rate}
					[/#if]
					[#if order.infusion_period?has_content]<br/>
						[#assign infusion_period = order.infusion_period+' '+infusion_interval_units]
					<font style="font-weight: bold">Infusion period </font>
						: ${infusion_period}
					[/#if]
					[#if order.iv_administer_instructions?has_content]<br/>
						<font style="font-weight: bold">IV Instruction </font>
						: ${order.iv_administer_instructions}
					[/#if]
					</td>
				</tr>
			[/#list]
		</tbody>
	</table>
