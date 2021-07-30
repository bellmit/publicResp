<!-- [#escape x as x?html] -->
<div class="patientHeader">
	<table cellspacing='0' cellpadding='1' width='100%' >
		 <tr>
			 <td align='left' valign='top'>Patient&nbsp;Name:</td>
			 <td align='left' valign='top'>${visitdetails.full_name!}</td>
			 <td align='left' valign='top'>MR&nbsp;No:</td>
		     <td align='left' valign='top'>${visitdetails.mr_no!}</td>
		 </tr>
		 <tr>
		 	<td align='left' valign='top'>Visit No:</td>
			<td align='left' valign='top'>${visitdetails.patient_id!}</td>
			<td align='left' valign='top'>Age/Gender:</td>
			<td align='left' valign='top'>${visitdetails.age!} ${visitdetails.agein!}/${visitdetails.patient_gender!}</td>

		 </tr>
	</table>
</div>
<!-- [/#escape] -->

