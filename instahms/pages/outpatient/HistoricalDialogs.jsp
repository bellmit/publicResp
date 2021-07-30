<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div id="diagnosisHistoryDiv" style="display: none">
	<div class="bd">
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel">Diagnosis History</legend>
			<table class="formtable" id="diagnosisHistoryTable">
				<tr style="display: none;">
					<td style="border-bottom : 1px solid; font-weight: bold; font-color: dark"></td>
					<td style="border-bottom : 1px solid; font-weight: bold; font-color: dark" ></td>
					<td style="border-bottom : 1px solid; font-weight: bold; font-color: dark"></td>
					<td style="border-bottom : 1px solid; font-weight: bold; font-color: dark" ></td>
				</tr>
				<tr style="display: none;">
					<td class="formlabel" style="white-space: nowrap"><label></label></td>
					<td class="forminfo"><label></label></td>
					<td class="formlabel">Code: </td>
					<td class="forminfo"><label></label></td>
				</tr>
			</table>
			<table style="margin-top: 10px">
				<tr>
					<td><input type="button" name="dhdCloseBtn" id="dhdCloseBtn" value="Close"/></td>
				</tr>
			</table>
		</fieldset>
	</div>
</div>

<div id="vitalsHistoryDiv" style="display: none">
	<div class="bd">
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel">Vitals History</legend>
			<table class="formtable" id="vitalsHistoryTable">
				<tr style="display: none;">
					<td style="border-bottom : 1px solid; font-weight: bold; font-color: dark"></td>
					<td style="border-bottom : 1px solid; font-weight: bold; font-color: dark" ></td>
					<td style="border-bottom : 1px solid; font-weight: bold; font-color: dark"></td>
					<td style="border-bottom : 1px solid; font-weight: bold; font-color: dark" ></td>
				</tr>
				<tr style="display: none">
					<td class="formlabel"><label></label></td>
					<td class="forminfo"><label></label></td>
					<td class="formlabel"><label></label></td>
					<td class="forminfo"><label></label></td>
				</tr>
			</table>
			<table style="margin-top: 10px">
				<tr>
					<td><input type="button" name="vhdCloseBtn" id="vhdCloseBtn" value="Close"/></td>
				</tr>
			</table>
		</fieldset>
	</div>
</div>
<div id="prescriptionsHistoryDiv" style="display: none">
	<div class="bd">
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel">Management History</legend>
			<table class="detailList" id="prescriptionsHistoryTable">
				<tr>
					<th>Type</th>
					<th>Name</th>
					<th>Details</th>
					<th>Qty</th>
					<th>Remarks</th>
				</tr>
				<tr style="display: none">
					<td style="border-bottom : 1px solid; font-weight: bold; font-color: dark"></td>
					<td style="border-bottom : 1px solid; font-weight: bold; font-color: dark" ></td>
					<td style="border-bottom : 1px solid; font-weight: bold; font-color: dark" ></td>
					<td style="border-bottom : 1px solid; font-weight: bold; font-color: dark"></td>
					<td style="border-bottom : 1px solid; font-weight: bold; font-color: dark" ></td>
				</tr>
				<tr style="display: none">
					<td><label></label></td>
					<td style="white-space: normal"><label></label></td>
					<td><label></label></td>
					<td style="white-space: normal"><label></label></td>
					<td><label></label></td>
				</tr>
			</table>
			<table style="margin-top: 10px">
				<tr>
					<td><input type="button" name="phdCloseBtn" id="phdCloseBtn" value="Close"/></td>
				</tr>
			</table>
	</div>
</div>
<div id="consultationNotesHistoryDiv" style="display: none;">
	<div class="bd">
		<fieldset class="fieldSetBorder" >
			<legend class="fieldSetLabel">Consultation Notes History</legend>
			<table class="formtable" id="consultNotesTable">
				<tr style="display: none">
					<td style="border-bottom : 1px solid; font-weight: bold; font-color: dark"></td>
					<td style="border-bottom : 1px solid; font-weight: bold; font-color: dark" ></td>
				</tr>
				<tr style="display: none">
					<td class="formlabel" style="width; 20%;"></td>
					<td class="forminfo" style="width: 80%;"></td>
				</tr>
			</table>
			<table style="margin-top: 10px">
				<tr>
					<td><input type="button" name="cnhdCloseBtn" id="cnhdCloseBtn" value="Close"/></td>
				</tr>
			</table>
	</div>
</div>
<div id="consultationImagesHistoryDiv" style="display: none">
	<div class="bd">
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel">Consultation Images</legend>
			<table class="detailList" id="consultationImagesHistoryTable">
				<tr>
					<th>Date</th>
					<th>Action</th>
				</tr>
				<tr style="display: none">
					<td style="border-bottom : 1px solid; font-weight: bold; font-color: dark"></td>
					<td style="border-bottom : 1px solid; font-weight: bold; font-color: dark" ></td>
				</tr>
				<tr style="display: none">
					<td ></td>
					<td name="History_dialog_rowName">
						<a href="#" name="History_dialog_viewImage">View</a>
						<label name="History_dialog_withoutView" id="withoutView" style="display: none;">View</label>
					</td>
				</tr>
			</table>
			<table style="margin-top: 10px">
				<tr>
					<td style="display: none;" id="History_dialog_imageTd" colspan="2"></td>
				</tr>
				<tr>
					<td><input type="button" name="cihdCloseBtn" id="cihdCloseBtn" value="Close"/></td>
				</tr>
			</table>
		</fieldset>
	</div>
</div>