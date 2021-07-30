<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Nurse's Notes</title>

</head>
<body>

<div align="center" style="font-size: 12pt;"><b>Nurse's Notes </b></div>
<div class="patientHeader">
  	[#include "VisitDetailsHeader.ftl"]
</div>
<br/><br/>
<div>
<!-- [#list NurseNotesList as notes] -->
		<table width='100%' style="border-collapse: collapse;border-bottom: 1px solid;border-bottom-color:#A4A4A4; margin-top: 10px">
		<th><b>${notes.map.creation_datetime?string('dd-MM-yyyy HH:mm')}       | ( [#if '${notes.map.note_type!}' == 'H'] Handed Over [#elseif '${notes.map.note_type!}' == 'T'] Taken Over [#else] Entered [/#if] By : ${notes.map.mod_user} )</b></th>
			<tr>
				<span style="margin-left: 10px" class="notes">
					${(((notes.map.notes!)?html)?replace("\n","<br/>"))}
				</span>
			</tr>
		</table>
<!--	[/#list] -->

</div>
</body>
</html>