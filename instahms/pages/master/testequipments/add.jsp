<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.master.URLRoute"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="pagePath" value="<%=URLRoute.TEST_EQUIPMENT_PATH %>"/>
<c:set var="max_centers_inc_default" value="${genPrefs[0].max_centers_inc_default}" scope="request" />

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Add Test Equipment - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="js" file="masters/equipmentResults.js" />

<script>
	var chkEquipmentName = ${ifn:convertListToJson(equipmentNames)}
	var backupName = '';
	var allResults =${ifn:convertListToJson(result)};
	var neworedit = 'new';
</script>

</head>
<body onload="keepBackUp();initAddDialog();initResultsAutoComplete();">

<form action="${cpath}${pagePath}/create.htm" method="POST" name="testEquipmentMaster">
	<input type="hidden" name="eq_id" id="eq_id" value="${bean.map.eq_id}"/>

	<h1>Add Equipment</h1>
	<insta:feedback-panel/>
	<fieldset class="fieldsetborder">
		<legend>Equipment Details</legend>

		<table class="formtable" align="center">
			<tr>
				<td class="formlabel">Equipment Name:</td>
				<td>
					<input type="text" name="equipment_name" id="equipment_name" >
				</td>
				<td class="formlabel">Status:</td>
				<td>
					<select name="status" class="dropdown">
						<option value="A">Active</option>
						<option value="I">InActive</option>
					</select>
				</td>
				<td class="formlabel">Schedulable:</td>
				<td><input type="checkbox" name="schedule" onclick="changeCheckboxValues()" /></td>
			</tr>
			<tr>
				<td class="formlabel">Diagnostic Department:</td>
				<td>
					<select id="ddept_id" name="ddept_id" class="dropdown" >
						<option value="">-- Select --</option>
						<c:forEach items="${diagdepts}" var="diagdept">
							<option value="${diagdept.get('dept_id')}" ${ifn:arrayFind(paramValues.ddept_id, diagdept.get('dept_id')) != -1 ? 'selected' : ''}>${diagdept.get('dept_name')}</option>
						</c:forEach>
					</select>
				</td>
				<td class="formlabel">hl7 Export Code:</td>
				<td>
					<input type="text" name="hl7_export_code" >
				</td>
				<c:set var="overbookDisable" value="${bean.map.schedule ? '' : 'disabled'}"/>
				 <td class="formlabel">Overbook Limit:</td>
				
				<td><input type="text" name="overbook_limit" value="${overbookDisable == 'disabled'? 0 : bean.map.overbook_limit}" ${overbookDisable} 
				onkeypress="return enterNumOnlyzeroToNine(event)" />
				<img class="imgHelpText" title=" Zero - overbook not allowed.
 Empty - infinite.
 Specific number - that many overbooking allowed." src="${cpath}/images/help.png">
				</td>
			</tr>
				<c:choose>
					<c:when test="${max_centers_inc_default == 1}">
						<input type="hidden" name="center_id" id="center_id" value="0"/>
					</c:when>
					<c:otherwise>
					<tr>
						<td class="formlabel">Center: </td>
						<td class="forminfo">
							<select class="dropdown" name="center_id" id="center_id">
								<option value="">-- Select --</option>
									<c:forEach items="${centers}" var="center">
										<option value="${center.get('center_id')}">${center.get('center_name')}</option>
									</c:forEach>
							</select>
						</td>
					</tr>
					</c:otherwise>
				</c:choose>
		</table>

	</fieldset>

	<fieldset class="fieldsetborder">
		<legend>Test Results</legend>
		<table width="100%" class="detailList" id="resultsTable">
			<tr>
				<th>Result Name</th>
				<th>Units</th>
				<th></th>
			</tr>
			<tr style="display: none">
				<td></td>
				<td></td>
				<td style="text-align:right">
					<a href="javascript:void(0)" onclick="deleteResult(this,deleted);"
						title="Delete Resulty " >
						<img src="${cpath}/icons/delete.gif" class="button" />
					</a>
				</td>
			</tr>
		</table>
		<table align="right">
			<tr>
				<td width="16px" style="text-align: center">
					<button type="button" name="btnAddItem" id="btnAddItem" title="Add a Result to be tested"
							onclick="showAddDialog(this);" accesskey="+" class="imgButton">
						<img src="${cpath}/icons/Add.png">
					</button>
				</td>
			</tr>
		</table>
	</fieldset>

	<div class="screenActions">
		<button type="submit" accesskey="S" onclick="return validate();"><b><u>S</u></b>ave</button>
		| <a href="${cpath}/master/testequipments.htm?sortOrder=equipment_name&sortReverse=false&tm.status=A">Equipment List</a>
	</div>

	<div id="addDialog" style="visibility: hidden; display:none">
		<div class="bd">
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel">Add Results</legend>
				<table class="formtable">
					<tr>
						<td class="formlabel">Result:</td>
						<td colspan="3" class="yui-skin-sam" valign="top">
							<div style="display: block; float: left; width: 250px" id="result">
								<input type="text" id="addResults" name="addResults" />
								<div id="addResultsAcDropdown" class="scrollingDropDown" style="width: 250px;"></div>
							</div>
							<div style="float: left; margin-left: 260px">
								<img class="imgHelpText" src="${cpath}/images/help.png"
									title="Type a result name to search among existing results"/>
							</div>
						</td>
					</tr>
					<tr>
						<td class="formlabel">Units:</td>
						<td  class="forminfo" id="rUnits"></td>
					</tr>
				</table>
			</fieldset>
			<table>
				<tr>
					<td>
						<button type="button" name="add" id="add" accesskey="A" onclick="onAdd();">
							<b><u>A</u></b>dd
						</button>
					</td>
					<td><input type="button" name="cancel" value="Close" onclick="onAddDialogCancel()"/></td>
				</tr>
			</table>
		</div>
	</div>


</form>

</body>
</html>