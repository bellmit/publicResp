<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<div style="clear: both"></div>
<div id="testInfoDialog" style="display:${empty testAdditionalDocList ?'none':'block'}">
<legend class="fieldSetLabel" style="margin-top: 10px">Additional Order Details</legend>
<table class="resultList" style="margin-top: 10px" id="ad_test_info_table">
	<tr>
		<th>Package</th>
		<th>Test Name</th>
		<th>Additional Test Information</th>
		<th>User Notes</th>
		<th style="width: 250px"></th>
		<th style="width: 16px"></th>
		<th style="width: 16px"></th>
	</tr>
	<tr style="display: none" >
		<td>
			<label></label>
			<input type="hidden" name="ad_main_row_id" value=""/>
			<input type="hidden" name="ad_test_id" value=""/>
			<input type="hidden" name="ad_clinical_notes" value=""/>
			<input type="hidden" name="ad_package_activity_index" value=""/>
			<input type="hidden" name="ad_test_category" value=""/>
			<input type="hidden" name="ad_test_row_edited" value="false"/>
			<input type="hidden" name="ad_notes_entered" value="false"/>
			<input type="hidden" name="ad_test_doc_delete" value="false"/>
			<input type="hidden" name="ad_test_name" value=""/>
			<input type="hidden" name="ad_test_info_reqts" value="">
		</td>
		<td>
			<label></label>
		</td>
		<td>
			<label></label>
		</td>
		<td >
			<label style="float: left"></label>
			<a name="_editAnchor" href="javascript:Edit" onclick="return showTestAdInfoDialog(this);"
				title="Edit Test Additional Details" style="float: right">
				<img src="${cpath}/icons/Edit.png" class="button" />
			</a>
		</td>
		<td>
			<input type="file" id="ad_test_file_upload${i-1}" name="ad_test_file_upload[${i-1}]" 
				class="testFileUpload" 
				onchange="setTestDocRowEdited(this)" style="width: 200px;" accept="<insta:ltext key="upload.accept.medical_image"/>,<insta:ltext key="upload.accept.document"/>"/>
		</td>
		<td>
			<a href="javascript:void(0)" onclick="return cancelTestAdtnlDoc(this);" title="Cancel Test Additional Document">
				<img src="${cpath}/icons/delete.gif" class="imgDelete button" />
			</a>
		</td>
		<td> 
			<button type="button" name="btnAddItem" title="Add Test Additional Document"
				onclick="cloneTestDocRow(this); return false;"
				class="imgButton"><img src="${cpath}/icons/Add.png"></button>
		</td>
	</tr>
</table>

<div id="addTestAddiotionalInfoDialog" style="display: none">
	<div class="bd">
		<input type="hidden" id="adTestAdInfoRowId" value=""/>
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel">Add/Edit</legend>
			<table class="formtable">
			<tr>
					<td class="formlabel">Additional Test Info:</td>
					<td><label id="test_info"></label></td>
				</tr>
				<tr>
					<td class="formlabel">User Notes</td>
					<td><textarea name="d_test_additional_info" id="d_test_additional_info" rows="4" cols="60"></textarea></td>
				</tr>
			</table>
			<table style="margin-top: 10px">
				<tr>
					<td>
						<input type="button" id="adTestAdInfoOk" value="Ok">
						<input type="button" id="adTestAdInfoCancel" value="Cancel">
					</td>
				</tr>
			</table>
		</fieldset>
	</div>
	</div>
</div>
