
[#setting number_format="#"]
<table cellspacing='0' cellpadding='0' width='100%'>
	  	<tbody>
			<!-- [#escape x as x?html] -->
			<tr>
				<td valign='top'>
					<table cellspacing='0' cellpadding='1' width='100%' >
						<tbody>
							<tr>
								<td align='left' valign='top'>Service Name:</td>
								<td align='left' valign='top'>${serviceDetails.map.service_name!}</td>
							</tr>
							<tr>
								<td align='left' valign='top'>Service Department:</td>
								<td align='left' valign='top'>${visitdetails.dept_name!}</td>
							</tr>
							<tr>
								<td align='left' valign='top'>Conducted By:</td>
								<td align='left' valign='top'>${serviceDetails.map.conducting_doctor!}</td>
							</tr>
							<tr>
								<td align='left' valign='top'>Conducted Date:</td>
									<td align='left' valign='top'>
										${(serviceDetails.map.conducteddate?string("dd-MM-yyyy HH:mm"))!}
								</td>
							</tr>
						</tbody>
					</table>
				</td>
				<td align='right' valign='top' width="45%">
					<table cellspacing='0' cellpadding='1' style='empty-cells:none;width:100%;'>
						<tbody>
							<tr>
								<td align='left' valign='top'>Qty Ordered:</td>
								<td align='left' valign='top'>${serviceDetails.map.quantity!}</td>
							</tr>

							<tr>
								<td align='left' valign='top'>Completed:</td>
								<!-- [#if serviceDetails.map.conducted == 'C'] -->

								<td align='left' valign='top'>Yes</td>
								<!-- [#else] -->
									<td align='left' valign='top'>No</td>
							<!-- [/#if] -->
							</tr>

							<tr>
								<td align='left' valign='top'>Remarks:</td>
								<td align='left' valign='top'>${serviceDetails.map.remarks!}</td>
							</tr>
							<tr>
								<td align='left' valign='top'>Conducted Time:</td>
								<td align='left' valign='top'></td>
							</tr>
						</tbody>
					</table>
				</td>
			</tr>
			<!-- [/#escape] -->
		</tbody>
	</table>

<!-- [#list service_components.sections?split(",") as formid] -->
<!--	[#if formid?number > 0] -->
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


<!-- [/#if] -->
<!--[/#list] -->