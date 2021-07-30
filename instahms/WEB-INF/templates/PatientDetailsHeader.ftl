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
