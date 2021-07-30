<div class="patientHeader" >
	<table cellspacing='0' cellpadding='2' width='100%'>
		 <tbody>
		<!--[#if visitDetails??]-->
			<tr>
				 <td width='15%'>Patient&nbsp;Name:</td>
				 <td width='50%'>${visitDetails.fullName?html}</td>
				 <td width='20%'>Report&nbsp;Date:</td>
				 <td width='15%'>${visitDetails.reportDate}</td>
			</tr>
			<tr>
				 <td width='15%'>Age/Gender:</td>
				 <td width='50%'>${visitDetails.ageAndSex?html}</td>
				 <!--[#if (visitDetails.mrNo)??]-->
				   <td width='20%'>MR&nbsp;No:</td>
				   <td width='15%'>${visitDetails.mrNo?html}</td>
				<!--[/#if]-->
			</tr>
			<tr>
				<td width='20%'>Visit No:</td>
				<td width='15%'>${visitDetails.visitNo?html}</td>
			</tr>
		 <!-- [/#if]-->
		  </tbody>
	</table>
</div>
