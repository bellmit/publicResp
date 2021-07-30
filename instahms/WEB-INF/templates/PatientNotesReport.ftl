<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Patient Notes Record</title>

</head>
<body>
[#setting number_format="#"]
<div align="center" style="font-size: 12pt;"><b>Patient Notes</b></div>
<div class="patientHeader">
  	[#include "VisitDetailsHeader.ftl"]
</div>
<br/><br/>
<div>
	<!-- [#if patientNotes?has_content] -->
		<h3 style="margin-top: 10px"><u>Patient Notes</u></h3>
			<!-- [#list patientNotes as note] -->	
				<table width='100%' style='margin-top: 5px; border: 1px solid black;' >
					<tbody>								
						<tr>
							<td width="35%" align="left">Date : ${note.documented_date} ${note.documented_time}</td>
							<td width="35%" align="center">Note Type : ${note.note_type_name!?html}</td>
							<td width="30%" align="right">Created By : ${note.created_by!?html}</td>
						</tr>							
						<!--		[#if note.on_behalf_user?has_content] -->
						<tr>
							<td width="35%">&nbsp;</td>
							<td width="35%">&nbsp;</td>
							<td width="30%" align="right">On Behalf of : ${note.on_behalf_user!?html}</td>
						</tr>
								<!--[/#if] -->
						<tr><td colspan="3">&nbsp;</td></tr>
						<tr>
							[#if note.note_type_id > 0 && note.note_type_id < 5]
                                                          <td colspan="3" align="left" class="notes">${((note.note_content!)?html)?replace("\n","<br/>")}</td>
                                                        [#else]
                                                          <td colspan="3" align="left" class="notes">${note.note_content}</td>
                                                        [/#if]
						</tr>
						<tr><td colspan="3">&nbsp;</td></tr>
					</tbody>
				</table>
			<!--  [/#list] -->
	<!-- [/#if] -->
</div>
</body>
</html>
