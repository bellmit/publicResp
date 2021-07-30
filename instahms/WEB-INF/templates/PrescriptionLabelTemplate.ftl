[#setting number_format="#"]
 <!-- [#assign i = 1] -->
<!--[#list items as item] -->
<!-- <#assign manf_name = "${(item.manf_name!)?html}">
<#assign categoty = "${(item.category_name!)?html}"> -->
	<div class="patientHeader">
		<table>
			<tbody>
				<tr>
					<th align='left' valign='top'>Name : </th>
					<td align='left' valign='top'>${(item.patient_full_name!)?html}</td>
				</tr>
				<tr>
					<th align='left'>Doctor Name : </th>
					<td align='left'>${(item.doctor_name!)?html}</td>
				</tr>
			</tbody>
		</table>
	</div>

  	<table cellspacing='0' cellpadding='0' width='100%'>
	  	<tbody>

			<!-- [#if item?has_content] -->
					<tr>
						<th align='left'>Date : </th>
						<td align='left'>${(item.sale_date!)?html}</td>
					</tr>
					<tr><td>&nbsp;</td></tr>
					<tr>
						<th align='left'>Medicine Name : </th>
						<td align='left'>${(item.medicine_name!)?html}</td>
					</tr>
					<tr>
						<th align='left'>Dosage : </th>
						<td align='left'>${(item.dosage!)?html}</td>
					</tr>
					<tr>
						<th align='left'>Duration : </th>
						<td align='left'>${(item.duration!)?html}${item.duration_units!}</td>
					</tr>
					<tr>
						<th align='left'>Frequency : </th>
						<td align='left'>${(item.frequency!)?html}</td>
					</tr>
					<tr>
						<th align='left'>Route : </th>
						<td align='left'>${(item.route_name!)?html}</td>
					</tr>
					<tr>
						<th align='left'>Doctor Remarks : </th>
						<td align='left'>${(item.doctor_remarks!)?html}</td>
					</tr>
					<tr>
						<th align='left'>Other Remarks : </th>
						<td align='left'>${(item.sales_remarks!)?html}</td>
					</tr>
					<tr><td>&nbsp;</td></tr>
					<tr>
						<th>Exp Date : </th>
						<td>${(item.expiry_date!)?html}</td>
					</tr>
					<tr>
						<th>Bill No. : </th>
						<td>${(item.sale_id!)?html}</td>
					</tr>
			<!-- [/#if] -->
		</tbody>
	</table>

	<!-- [#if ((item.label_msg!'')!='')] -->
	<table width="100%" style="margin-top: 1em">
		<tr align="center" valign="bottom">
			<td><b>"</b>${(item.label_msg!)?html}<b>"</b></td>
		</tr>
	</table>
	<!-- [/#if] -->
<!-- [#if items?size > i] -->
<p class="pagebreak"/>
<!-- [/#if] -->
<!-- [#assign i = i+1] -->
<!--[/#list] -->

