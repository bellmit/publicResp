<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@page import="com.insta.hms.master.FixedAssetMaster.FixedAssetMasterDAO"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Asset Complaints - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>

	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
<style>
.scrolForContainer .yui-ac-content{
	 max-height:11em;overflow:auto;overflow-x:hidden; /* scrolling */
    _height:11em; /* ie6 */
}

.yui-ac {
	padding-bottom: 2em;
}
</style>

	<script>
		var assets = <%=FixedAssetMasterDAO.getTableDataInJSON
		(FixedAssetMasterDAO.GET_ACTIVE_ASSETS, (String)request.getSession().getAttribute("userid"),
				(Integer)request.getSession().getAttribute("roleId"),(String)request.getSession().getAttribute("multiStoreAccess"),
				(String)request.getSession().getAttribute("pharmacyStoreId"))%>;
		/* itemNames AutoComplete */
	var oAutoComp = null;
	var centerId = '${centerId}';
	function initAssetAutoComplete() {

		var assetNames = []; var issueItemNames = []; var j = 0; var k = 0;
		if (null != assets){
			if(centerId == 0) {
				for (var i=0; i<assets.length; i++ ) {
				 	if (assets[i].ASSET_STATUS == 'A')
				 	assetNames[j++] = assets[i].MEDICINE_NAME+'-'+assets[i].BATCH_NO+'   ('+assets[i].DEPT_NAME+')';
				}
			}else {
				for (var i=0; i<assets.length; i++ ) {
				 	if (assets[i].ASSET_STATUS == 'A' && assets[i].CENTER_ID == centerId)
				 		assetNames[j++] = assets[i].MEDICINE_NAME+'-'+assets[i].BATCH_NO+'   ('+assets[i].DEPT_NAME+')';

				}
			}
		}

		//YAHOO.util.Event.addListener(itemname,"keypress",isEventEnterEscAuto);
		YAHOO.example.ACJSAddArray = new function() {
			var dataSource = null;

			dataSource = new YAHOO.widget.DS_JSArray(assetNames);

			if(oAutoComp != null) {
				oAutoComp.destroy();
			}

			oAutoComp = new YAHOO.widget.AutoComplete('asset_name', 'asset_dropdown', dataSource);
			oAutoComp.maxResultsDisplayed = 50;
			oAutoComp.allowBrowserAutocomplete = false;
			oAutoComp.prehighlightClassName = "yui-ac-prehighlight";
			oAutoComp.typeAhead = false;
			oAutoComp.useShadow = false;
			oAutoComp.minQueryLength = 0;
			oAutoComp.forceSelection = true;
			oAutoComp.filterResults = Insta.queryMatchWordStartsWith;
			oAutoComp.formatResult = Insta.autoHighlightWordBeginnings;
			oAutoComp.itemSelectEvent.subscribe(onSelectAsset);
		}

		if(document.forms[0].method.value == 'update') {
			document.getElementById('location').style.visibility = 'visible';
			document.getElementById('location_name_td').style.visibility = 'visible';
			document.getElementById('location_lbl').style.visibility = 'visible';
			document.getElementById('location').style.display = 'block';
			document.getElementById('location_lbl').innerHTML = '${complaintDetails[0].LOCATION_NAME}';
		}
			if(document.forms[0].asset_name.value == '') {
			 document.getElementById('location').style.display = 'none';
			}
	}
	function onSelectAsset(){
		var assetSel = document.forms[0].asset_name.value;
		var assetId = document.forms[0].asset_id;
		var batchNo = document.forms[0].batch_no;
		for (var i=0; i<assets.length; i++){
			var asset = assets[i]
			if ((null != asset) && (asset.MEDICINE_NAME != '')){
				var assetString = asset.MEDICINE_NAME+'-'+asset.BATCH_NO+'   ('+assets[i].DEPT_NAME+')';
				if (assetString.trim() == assetSel){
					assetId.value = asset.MEDICINE_ID;
					batchNo.value = asset.BATCH_NO;
					document.getElementById('location_lbl').textContent = asset.LOCATION_NAME;
				}

			}
		}
		document.getElementById('location').style.visibility = 'visible';
		document.getElementById('location_name_td').style.visibility = 'visible';
		document.getElementById('location_lbl').style.visibility = 'visible';
		document.getElementById('location').style.display = 'block';
	}

	function doValidate() {
		 var raiseddt = document.getElementById("raised_date");
		 var assigneddt = document.getElementById("assigned_date");
		 var resolveddt = document.getElementById("resolved_date");
		 var closeddt = document.getElementById("closed_date");
		 var raiseddthid = document.getElementById("raised_date_hid");
		 var assigneddthid = document.getElementById("assigned_date_hid");
		 var resolveddthid = document.getElementById("resolved_date_hid");
		 var closeddthid = document.getElementById("closed_date_hid");
		 if ((null != raiseddt) && (raiseddt.value != '') && (raiseddt.value != raiseddthid.value)){
			if (!(doValidateDateField(raiseddt,"future"))){
				return false;
			}
		}
		if ((null != assigneddt) && (assigneddt.value != '') && (assigneddt.value != assigneddthid.value)){
			if (!(doValidateDateField(assigneddt,"future"))){
				return false;
			}
		}
		if ((null != resolveddt) && (resolveddt.value != '') &&(resolveddt.value != resolveddthid.value)){
			if (!(doValidateDateField(resolveddt,"future"))){
				return false;
			}
		}
		if ((null != closeddt) && (closeddt.value != '') && (closeddt.value != closeddthid.value)){
			if (!(doValidateDateField(closeddt,"future"))){
				return false;
			}
		}
		if(document.forms[0].asset_name.value == '' || document.forms[0].asset_name.value == '-') {
			alert("Please Select the Asset from the list");
			return false;
		}
		if(raiseddt == null || raiseddt.value == '') {
			alert("Please Select the Raised Date.");
			return false;
		}

		return true;
	}
		function doClose() {

			window.location.href = "${cpath}/resourcemanagement/AssetComplaints.do?method=list";

		}

		function goodToSave(){
			if (doValidate()){
				window.location.href = "${cpath}/resourcemanagement/AssetComplaints.do?method=list";
				return true;
			} else
				return false;
			}

		function setLocation() {
			if(document.forms[0].asset_name.value == '' || document.forms[0].asset_name.value == '-'){
				document.getElementById('location').style.visibility = 'hidden';
				document.getElementById('location_name_td').style.visibility = 'hidden';
				document.getElementById('location_lbl').style.visibility = 'hidden';
				document.getElementById('location').style.display = 'none';
			}else{
				document.getElementById('location').style.visibility = 'visible';
				document.getElementById('location_name_td').style.visibility = 'visible';
				document.getElementById('location_lbl').style.visibility = 'visible';
				document.getElementById('location').style.display = 'block';
			}
		}
	</script>

</head>
<body onload="initAssetAutoComplete();">
<jsp:useBean id="currentDate" class="java.util.Date"/>
<form action="AssetComplaints.do" method="POST">
	<input type="hidden" name="method" value="${param.method == 'add' ? 'create' : 'update'}">
	<c:if test="${param.method == 'show'}">
		<input type="hidden" name="complaint_id" value="${bean.map.complaint_id}"/>
	</c:if>
	<c:choose>
		<c:when test="${param.method=='add' }">
			<c:set var="editableDesc" value="Y"/>
		</c:when>
		<c:otherwise>
			<c:set var="editableDesc" value="${bean.map.created_by == user ? 'Y' : 'N' }"/>
		</c:otherwise>
	</c:choose>
	<div class="pageHeader">${param.method == 'add' ? 'Add' : 'Edit'} Complaint</div>
	<span align="center" class="error">${ifn:cleanHtml(error)}</span>

	<fieldset class="fieldsetborder"><legend class="fieldSetLabel">Complaint Details</legend>
	<table class="formtable" width="100%">
		<tr>
			<td class="formlabel">Asset:</td>
			<td>
				<div id="item_wrapper" style="width: 23em; padding-bottom:2em;">
					<input type="text" name="asset_name" id="asset_name" style="width: 22em" maxlength="100"
					value="${complaintDetails[0].ASSET_NAME}${empty complaintDetails[0].ASSET_NAME ? '' : '-'}${complaintDetails[0].BATCH_NO}" onChange="setLocation();" tabindex="1"/>
					<div id="asset_dropdown"></div>
				</div>
				<input type="hidden" id="asset_id" name="asset_id" value="${complaintDetails[0].ASSET_ID}"/>
				<input type="hidden" id="batch_no" name="batch_no" value="${complaintDetails[0].BATCH_NO}"/>
			</td>
			<td class="formLabel">Raised On: </td>
			<td colspan="3">
				<fmt:formatDate var="dateVal" value="${currentDate}" pattern="dd-MM-yyyy"/>
				<insta:datewidget id = "raised_date" name="raised_date" value="${param.method == 'add' ? dateVal : complaintDetails[0].RAISED_DATE}" />
				<span class="star">*</span>
				<input type="hidden" name="raised_date_hid" id="raised_date_hid" value="${complaintDetails[0].RAISED_DATE}"/>
			</td>
		</tr>
		<tr >
			<span id="location" style="visibility:hidden;display:none;" >
				<td class="formlabel" id="location_name_td" style="visibility:hidden;">
					<input type="hidden" name="location_name" id="location_name" value="${complaintDetails[0].LOCATION_NAME}"/>
						Location:
				</td>
				<td>
					<b><label id="location_lbl"></label></b>
				</td>
				<td class="formLabel">&nbsp;</td>
				<td colspan="3">&nbsp;</td>
			</span>
		</tr>
		<tr>
			<td class="formlabel">Complaint Type:</td>
			<td><insta:selectoptions name="complaint_type" id="complaint_type"
					opvalues="Completely down, Intermittent failure, Part failure, Suggestion"
					 optexts="Completely down, Intermittent failure, Part failure, Suggestion"
					 value="${complaintDetails[0].COMPLAINT_TYPE}" /></td>
			<td class="formLabel">Assigned On: </td>
			<td colspan="3">
				<insta:datewidget id = "assigned_date" name="assigned_date" value="${complaintDetails[0].ASSIGNED_DATE}" />
				<input type="hidden" name="assigned_date_hid" id="assigned_date_hid" value="${complaintDetails[0].ASSIGNED_DATE}"/>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Complainant Name:</td>
			<td>
				<input type="text" name="emp_name" value="${complaintDetails[0].EMP_NAME}"  />
			</td>
			<td class="formLabel">Resolved On: </td>
			<td colspan="3">
				<insta:datewidget id = "resolved_date" name="resolved_date" value="${complaintDetails[0].RESOLVED_DATE}" />
				<input type="hidden" name="resolved_date_hid" id="resolved_date_hid" value="${complaintDetails[0].RESOLVED_DATE}"/>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Description:</td>
			<td>
			<c:choose>
			<c:when test="${editableDesc == 'Y'}">
				<textarea rows="5" cols="35" name="complaint_desc">${complaintDetails[0].COMPLAINT_DESC}</textarea>
			</c:when>
			<c:otherwise>
				<textarea rows="5" cols="35" name="complaint_desc" readonly>${complaintDetails[0].COMPLAINT_DESC}</textarea>
			</c:otherwise>
			</c:choose>
			</td>
			<td class="formLabel">Closed On: </td>
			<td colspan="3">
				<insta:datewidget id = "closed_date" name="closed_date" value="${complaintDetails[0].CLOSED_DATE}" />
				<input type="hidden" name="closed_date_hid" id="closed_date_hid" value="${complaintDetails[0].CLOSED_DATE}"/>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Status:</td>
			<td>
			<insta:selectoptions name="complaint_status" id="complaint_status"
				opvalues="0,1,2,3"
				 optexts="Recorded,Assigned,Resolved,Closed"
				value="${complaintDetails[0].COMPLAINT_STATUS}" />
				<input type="hidden" name="complaint_status@type" value="numeric" />
			</td>
			<td></td>
			<td></td>
			<td></td>
			<td></td>
		</tr>
		<tr>
			<td class="formlabel">Closure Note:</td>
			<td>
				<textarea rows="5" cols="35" name="complaint_closure_note">${complaintDetails[0].COMPLAINT_CLOSURE_NOTE}</textarea>
			</td>
			<td></td>
			<td></td>
			<td></td>
			<td></td>
		</tr>
		<tr>
			<td colspan="2">&nbsp;</td>
			<td></td>
			<td></td>
			<td></td>
			<td></td>
		</tr>
		</table>
		</fieldset>

		<div class="screenActions">
			<input type="submit" name="button" value="Save" onclick="return goodToSave();"/>
			<a href="#" onclick="return doClose();"> | Back To Dashboard</a>
		</div>

</form>

</body>
</html>
