<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="consultation_status" value="${consultation_bean.status}"/>
<c:set var="notes_status" value="${doctor_progress_notes.map.status}"/>
<fieldset class="fieldSetBorder" >
<legend class="fieldSetLabel">Progress Notes</legend>
<table class="formtable">
	<tr>
		<td class="formlabel">Notes:</td>
		<td><textarea  name="notes" cols="60" rows="2" ${consultation_status == 'C' ? 'readOnly' : ''}>${not empty notes_status && notes_status == 'C'? '' : doctor_progress_notes.map.notes}</textarea></td>
		<td align="left">
			<a href="javascript:void(0)" onclick="getPreviousProgressNotes(this)"
				title="Previous Progress Notes" class="progrees_notes_position" id="progress_notes_img_div">
				<img src="${cpath}/icons/Send.png" class="button" />
			</a>
			<input type="hidden" name="progress_notes_id" id="progress_notes_id" value="${not empty notes_status && notes_status == 'C' ? '' : doctor_progress_notes.map.progress_notes_id}"/>
			</td>
			<td>&nbsp;</td>
		</tr>
	</table>
</fieldset>

<div id="previousProgressNotesDiv" style="display:none">
	<div class="bd">
		<div style="max-height:22em;overflow-y:auto;overflow-x:hidden;height:250px;width:800px;">
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel">Previous Progress Notes</legend>
			<div id="progressbar" style="margin-top: 10px; font-weight: bold">
				Loading.. please wait..
			</div>
			<div style="float:right; margin-top: 10px; margin-right: 10px" id="paginationDiv">
			</div>
			<div style="clear:both"></div>
			<div class="resultList" style="width: 780px" >
				<table class="resultList" id="previousNotesTable" cellspacing="0" cellpadding="0" style="margin: top: 10px;width: 780px">
					<tr>
						<th style="text-align: left">Date/Time</th>
						<th style="text-align: left">Doctor</th>
						<th style="text-align: left">Progress Notes</th>
					</tr>
					<tr style="display:none">
						<td><label></label></td>
						<td><label></label></td>
						<td style="white-space:normal;"><label></label></td>
					</tr>
					<tr style="display: none; background-color:#FFC">
						<td colspan="6"><img src="${cpath}/images/alert.png"/> No previous history available for the patient.</td>
					</tr>
				</table>
			</div>
		</fieldset>
		</div>
		<div style="width:5px;height:5px;">&nbsp;</div>
		<table>
			<tr>
				<td><input type="button" name="previousNotes_btn" id="previousNotes_btn" value="Close"/></td>
			</tr>
		</table>
	</div>
</div>