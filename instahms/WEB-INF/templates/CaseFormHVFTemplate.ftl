<div class="patientHeader">
	[#-- if patient_id exists then displays the visit details header else displays the patientdetails header --]
	[#if (patient_id!'')!='']
		[#include "VisitDetailsHeader.ftl"]
	[#else][#include "PatientDetailsHeader.ftl"]
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
</div>