<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Add/Edit Form Section - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="script" file="/patientfeedback/feedbackformsection.js"/>
<insta:link type="js" file="dashboardsearch.js"/>

<script>
var chkSectionName = <%= request.getAttribute("feedbackFormSectionsList") %>;
var backupName = '';
var sectionOrderBackupName = '';
</script>

</head>
<body onload="keepBackUp();">

<form name="feedbackSectionSearchForm" action="SurveyFeedbackForms.do" method="POST">
	<input type="hidden" name="_method" value="${param._method == 'addSection' ? 'createSection' : 'updateSection'}">
	<input type="hidden" name="section_id" id="section_id" value="${bean.map.section_id}"/>
	<input type="hidden" name="form_id" id="form_id" value="${ifn:cleanHtmlAttribute(formId)}"/>

	<h1>${param._method == 'addSection' ? 'Add' : 'Edit'} Form Section</h1>
	<insta:feedback-panel/>
	<fieldset class="fieldsetborder">
		<legend class="fieldsetlabel">Section Details</legend>

		<table class="formtable">
			<tr>
				<td class="formlabel">Section Title:</td>
				<td colspan="3" title="${bean.map.section_title}">
					<input type="text" name="section_title" id="section_title" value="${bean.map.section_title}" maxlength="100"><span class="star">*</span>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Section Status:</td>
				<td colspan="3"><insta:selectoptions name="status" id="status" value="${bean.map.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
			</tr>
			<tr>
				<td class="formlabel">Section Order:</td>
				<td colspan="3">
					<input type="text" name="section_order" id="section_order" value="${bean.map.section_order}"
						onkeypress="return enterNumOnlyzeroToNine(event)" style="width:70px;"><span class="star">*</span>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Section Instructions:</td>
				<td colspan="3">
					<textarea cols="60" rows="4" name="section_instructions"><c:out value="${bean.map.section_instructions}"/></textarea>
				</td>
			</tr>
		</table>
	</fieldset>
	<fieldset class="fieldsetborder">
		<legend class="fieldsetlabel">Question Details</legend>
			<div style="text-align: right;">
				<div style="font-weight: bold; padding-top:5px; float: right">Hide Inactive Questions</div>
				<div style="padding-bottom: 2px; float: right">
					<input type="checkbox" name="hide_questions" id="hide_questions" onclick="hideUnHideInactiveQuestions(this)" checked="checked"/>
				</div>
				<div style="clear:both"></div>
			</div>
			<table id="resultTable" width="100%" height="100%" class="detailList">
				<tr>
					<th>Question Details</th>
					<th>Status</th>
					<th>Question Order</th>
					<th>Category</th>
					<th>Response Type</th>
					<th>&nbsp;</th>
					<th>&nbsp;</th>
				</tr>
				<c:set var="style" value='style=""'/>
				<c:set var="length" value="${fn:length(questionDetailsList)}"/>
	           	<c:forEach var="i" begin="1" end="${length+1}" varStatus="st">
	           		<c:set var="style" value='style=""'/>
	           		<c:set var="index" value="${st.index}"/>
	           		<c:set var="record" value="${questionDetailsList[i-1]}"/>
	           		<c:choose>
	           			<c:when test="${empty record}">
	           				<c:set var="style" value='style="display:none"'/>
	           			</c:when>
	           			<c:when test="${not empty record && record.map.status == 'I'}">
	           				<c:set var="style" value='style="display:none"'/>
	           			</c:when>
	           		</c:choose>
	           		<c:set var="responseType" value=""/>
	           		<c:choose>
	           			<c:when test="${record.map.response_type == 'T'}">
	           				<c:set var="responseType" value="Text"/>
	           			</c:when>
	           			<c:when test="${record.map.response_type == 'Y'}">
	           				<c:set var="responseType" value="Yes/No"/>
	           			</c:when>
	           			<c:when test="${record.map.response_type == 'R'}">
	           				<c:set var="responseType" value="${record.map.rating_type}"/>
	           			</c:when>
	           		</c:choose>
	           		<tr ${style} id="hide_question_row${index}">
	           			<td><label><insta:truncLabel value="${record.map.question_detail}" length="35"/></label>
	           				<input type="hidden" name="question_id" id="question_id" value="${record.map.question_id}">
	           				<input type="hidden" name="question_detail" id="question_detail" value="${record.map.question_detail}">
	           				<input type="hidden" name="question_order" id="question_order" value="${record.map.question_order}">
	           				<input type="hidden" name="category_id" id="category_id" value="${record.map.category_id}">
	           				<input type="hidden" name="rating_type_id" id="rating_type_id" value="${record.map.rating_type_id}">
	           				<input type="hidden" name="response_type" id="response_type" value="${record.map.response_type}">
	           				<input type="hidden" name="q_status" id="q_status" value="${record.map.status}">
	           				<input type="hidden" name="r_deleted" id="r_deleted" value="N"/>
	           				<input type="hidden" name="hide_question" id="hide_question${index}" value="Y"/>
							<input type="hidden" name="question_status" id="question_status${index}" value="${record.map.status}"/>
	           			</td>
	           			<td><label>${record.map.status == 'A' ? 'Active' : 'Inactive'}</label></td>
	           			<td><label>${record.map.question_order}</label></td>
	           			<td><label><insta:truncLabel value="${record.map.category}" length="35"/></label></td>
	           			<td><label><insta:truncLabel value="${responseType}" length="35"/></label></td>
	           			<td>
							<a>
								<img src="${cpath}/icons/Delete1.png" class="imgDelete button"/>
							</a>
						</td>
						<td>
							<a>
								<img src="${cpath}/icons/Edit.png" class="button" id="editIcon" name="editIcon"
									onclick="openEditQuestionDetailsDialogBox(this);" title="edit question details"/>
							</a>
	    				</td>
	           		</tr>
				</c:forEach>
			</table>
			<table align="right">
			<tr>
				<td width="16px" style="text-align: center">
					<button id="btnAddItem" class="imgButton" accesskey="+" onclick="showQuestionDialog(this);" title="Press (Alt+Shift+(+)) to add a question details row" name="btnAddItem" type="button">
						<img src="${cpath}/icons/Add.png">
					</button>
				</td>
			</tr>
		</table>
		<input type="hidden" name="dialogId" id="dialogId">
	</fieldset>

	<div style="display:none" id="questionDialog">
		<div class="hd" id="questiondialogheader"></div>
		<div class="bd">
			<fieldset class="fieldsetborder">
			<legend class="fieldsetlabel">Survey Question Details</legend>
				<table class="formtable">
					<tr>
						<td class="formlabel">Question Detail:</td>
						<td colspan="3">
							<textarea name="d_question_detail" cols="60" rows="4"></textarea><span class="star">*</span>
						</td>
					</tr>
					<tr>
						<td class="formlabel">Question Order:</td>
						<td colspan="3">
							<input type="text" name="d_question_order" id="d_question_order" style="width:50px;" onkeypress="return enterNumOnlyzeroToNine(event)">
							<span class="star">*</span>
						</td>
					</tr>
					<tr>
						<td class="formlabel">Question Status:</td>
						<td colspan="3"><insta:selectoptions name="d_question_status" id="d_question_status" value="A" opvalues="A,I" optexts="Active,Inactive" /></td>
					</tr>
					<tr>
						<td class="formlabel">Category:</td>
						<td colspan="3">
							<select id="d_question_category" name="d_question_category" class="dropdown" style="width: 255px">
									<option value="">-- Select --</option>
									<c:forEach items="${questionCategoryMaterList}" var="categoryList" >
										<option title="${categoryList.map.category}" value="${categoryList.map.category_id}"><insta:truncLabel value="${categoryList.map.category}" length="60"/></option>
									</c:forEach>
							</select>
							<span class="star">*</span>
						</td>
					</tr>
					<tr>
						<td class="formlabel">Reponse Type:</td>
						<td colspan="3">
							<select name="d_response_type" id="d_response_type" onchange="enableRating(this)" class="dropdown">
								<option value="">-- Select --</option>
								<option value="Y">Yes/No</option>
								<option value="T">Text</option>
								<option value="R">Rating</option>
							</select>
							<span class="star">*</span>
						</td>
					</tr>
					<tr>
						<td class="formlabel">Rating:</td>
						<td colspan="3">
							<select id="d_question_rating" name="d_question_rating" class="dropdown" style="width: 255px">
									<option value="">-- Select --</option>
									<c:forEach items="${ratingTypeMasterList}" var="ratingTypeList" >
										<option title="${ratingTypeList.map.rating_type}" value="${ratingTypeList.map.rating_type_id}"><insta:truncLabel value="${ratingTypeList.map.rating_type}" length="60"/></option>
									</c:forEach>
							</select>
							<span id="question_rating_span" class="star" style="display:none;">*</span>
						</td>
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
		<button type="submit" accesskey="S" onclick="return validate();"><b><u>S</u></b>ave</button>
		 <a href="SurveyFeedbackForms.do?_method=editForm&form_id=${not empty bean.map.form_id ? bean.map.form_id : param.form_id}">Feedback Form Design</a>
	</div>
</form>

</body>
</html>
