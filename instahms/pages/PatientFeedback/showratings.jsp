<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Add/Edit Survey Rating List - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="script" file="/patientfeedback/surveyratingdetails.js"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="css" file="widgets.css"/>
<insta:link type="script" file="ajax.js"/>
<insta:link type="js" file="dashboardsearch.js"/>

<script>
	var chkRatingName = <%= request.getAttribute("ratingListJson") %>;
	var backupName = '';

	function keepBackUp(){
		if(document.ratingMasterForm._method.value == 'update'){
			backupName = document.ratingMasterForm.rating_type.value;
		}
	}

	function init() {
		initSurveyRatingDialog();
	}
</script>
<style type="text/css">
	.deletedRow{
		background-color:#EAEAEA; cursor:pointer;
		border-bottom:1px #666 solid;  border-right:1px #999 solid;
		padding:5px 10px 4px 10px;  color:#707070;
	}
</style>

</head>
<body onload="keepBackUp();init();">

<form action="SurveyRatingMaster.do" method="POST" name="ratingMasterForm" autocomplete="off">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<input type="hidden" name="rating_type_id" id="rating_type_id" value="${bean.map.rating_type_id}"/>

	<h1>${param._method == 'add' ? 'Add' : 'Edit'} Survey Rating</h1>
	<insta:feedback-panel/>
	<fieldset class="fieldsetborder">
		<legend class="fieldsetlabel">Survey Rating Details</legend>

		<table class="formtable">
			<tr>
				<td class="formlabel">Rating Type:</td>
				<td colspan="3">
					<input type="text" name="rating_type" id="rating_type" value="${bean.map.rating_type}" maxlength="300"><span class="star">*</span>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Status:</td>
				<td colspan="3"><insta:selectoptions name="status" id="status" value="${bean.map.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
			</tr>
		</table>
	</fieldset>

	<fieldset class="fieldsetborder">
		<legend class="fieldsetlabel">Survey Rating</legend>

		<table width="100%" class="detailList" width="100%" id="resultTable">
			<tr>
				<th>Rating Text</th>
				<th>Rating Value</th>
				<th>&nbsp;</th>
				<th>&nbsp;</th>
			</tr>
			<c:set var="style" value='style=""'/>
			<c:set var="length" value="${fn:length(ratingDetailsList)}"/>
           	<c:forEach var="i" begin="1" end="${length+1}" varStatus="st">
           		<c:set var="index" value="${st.index}"/>
           		<c:set var="record" value="${ratingDetailsList[i-1]}"/>
				<c:if test="${empty record}">
					<c:set var="style" value='style="display:none"'/>
				</c:if>
           		<tr ${style}>
           			<td><label>${record.map.rating_text}</label>
           				<input type="hidden" name="rating_id" id="rating_id" value="${record.map.rating_id}">
           				<input type="hidden" name="rating_text" id="rating_text" value="${record.map.rating_text}">
           				<input type="hidden" name="rating_value" id="rating_value" value="${record.map.rating_value}">
           				<input type="hidden" name="r_deleted" id="r_deleted" value="N"/>
           			</td>
           			<td><label>${record.map.rating_value}</label></td>
           			<td>
						<a>
							<img src="${cpath}/icons/Delete1.png" class="imgDelete button"/>
						</a>
					</td>
					<td>
						<a>
							<img src="${cpath}/icons/Edit1.png" class="button" id="editIcon" name="editIcon"/>
						</a>
    				</td>
           		</tr>
			</c:forEach>
		</table>

		<table align="right">
			<tr>
				<td width="16px" style="text-align: center">
					<button id="btnAddItem" class="imgButton" accesskey="+" onclick="showSurveyRatingDialog(this);" title="Press (Alt+Shift+(+)) to add a rating details row" name="btnAddItem" type="button">
						<img src="${cpath}/icons/Add.png">
					</button>
				</td>
			</tr>
		</table>
		<input type="hidden" name="dialogId" id="dialogId">
	</fieldset>

	<div style="display:none" id="ratingDialog">
		<div class="hd" id="ratingdialogheader"></div>
		<div class="bd">
			<fieldset class="fieldsetborder">
			<legend class="fieldsetlabel">Survey Rating Details</legend>
				<table class="formtable">
					<tr>
						<td class="formlabel">Rating Text:</td>
						<td colspan="3"><input type="text" name="d_rating_text" id="d_rating_text" maxlength="50"></td>
					</tr>
					<tr>
						<td class="formlabel">Rating Value:</td>
						<td colspan="3"><input type="text" name="d_rating_value" id="d_rating_value" style="width:50px;" onkeypress="return enterNumOnlyzeroToNine(event)"></td>
					</tr>
				</table>
			</fieldset>
			<table>
				<tr>
					<td>
						<button type="button" name="Ok" accesskey="O" onclick="addRecord();"><b><u>O</u></b>k</button>
						<button type="button" name="Cancel" accesskey="C" onclick="cancelDialog();"><b><u>C</u></b>ancel</button>
					</td>
				</tr>
			</table>
		</div>
	</div>

	<div class="screenActions">
	<insta:accessbutton buttonkey="patientFeedback.showRating.save" type="submit" onclick="return validate();" />
		<c:if test="${param._method=='show'}">| <a href="SurveyRatingMaster.do?_method=add" >Add</a></c:if>
		| <a href="SurveyRatingMaster.do?_method=getRatingList&sortOrder=rating_type&sortReverse=false&status=A">Rating List</a>
	</div>
</form>

</body>
</html>
