<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Feedback Form  Design- Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>

<script>
	function hideUnHideInactiveSections(obj) {
		var hideSections = document.getElementsByName('hide_section');
		var sectionStatus = document.getElementsByName('section_status');

		if(obj.checked) {
			for(var i=0;i<hideSections.length;i++) {
				if(sectionStatus[i].value == 'I') {
					document.getElementById('section_div'+i).style.display = 'none';
				}
			}
		} else {
			for(var i=0;i<hideSections.length;i++) {
				if(sectionStatus[i].value == 'I') {
					document.getElementById('section_div'+i).style.display = '';
				}
			}
		}
	}
</script>

</head>
<body>
<form name="feedbackFormSearchForm" action="SurveyFeedbackForms.do" method="POST">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<input type="hidden" name="form_id" id="form_id" value="${bean.map.form_id}"/>

	<h1>Feedback Form Design</h1>
	<insta:feedback-panel/>

	<div style="text-align:center"><h2><label>${bean.map.form_name}</label></h2></div>
	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel">Form Title</legend>
		<table border="0" width="100%">
			<tr>
				<td colspan="2" style="border-top: none">
					<c:set var="formTitle" value="${ifn:breakContent(fn:escapeXml(bean.map.form_title))}"></c:set>
					<c:choose>
						<c:when test="${fn:length(formTitle) gt 150}">
							<label title="${bean.map.form_title}">
								<c:out value="${ifn:breakAfterNumChar(fn:substring(formTitle,0,120), 150)}..." escapeXml="false" />
							</label>
						</c:when>
						<c:otherwise>
							<label title="${bean.map.form_title}">
								<c:out value="${formTitle}" escapeXml="false" />
							</label>
						</c:otherwise>
					</c:choose>
				</td>
				<td width="20px" style="border-top: none"></td>
			</tr>
			<tr>
				<td colspan="2" style="border-top: none"></td>
				<td style="border-top: none; width: 20px">
					<a href="${cpath}/patientfeedback/SurveyFeedbackForms.do?_method=show&form_id=${bean.map.form_id}" title="Edit Feedback Form Details">Edit</a>
				</td>
			</tr>
		</table>
	</fieldset>
	<c:if test="${not empty formSectionsList}">
		<div style="text-align: right;">
			<div style="font-weight: bold; padding-top:5px; float: right">Hide Inactive Sections</div>
			<div style="padding-bottom: 2px; float: right">
				<input type="checkbox" name="hide_sections" id="hide_sections" onclick="hideUnHideInactiveSections(this)" checked="checked"/>
			</div>
			<div style="clear:both"></div>
		</div>
	</c:if>
	<div>
		<c:forEach items="${formSectionsList}" var="record" varStatus="st">
			<c:set var="style" value=""/>
			<c:if test="${record.map.status == 'I'}">
				<c:set var="style" value='style="display:none"'/>
			</c:if>
			<div id="section_div${st.index}" ${style}>
				<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel">Section:${record.map.section_title}</legend>
				<table border="0" width="100%">
					<tr>
						<td colspan="2" style="border-top: none">
							<input type="hidden" name="hide_section" id="hide_section${st.index}" value="Y"/>
							<input type="hidden" name="section_status" id="section_status${st.index}" value="${record.map.status}"/>
						</td>
					</tr>
					<tr>
						<td colspan="2" style="border-top: none">
							<c:set var="sectionInstructions" value="${ifn:breakContent(fn:escapeXml(record.map.section_instructions))}"></c:set>
							<label title="${record.map.section_instructions}">
								<c:out value="${sectionInstructions}" escapeXml="false" />
							</label>
						</td>
						<td width="20px" style="border-top: none"></td>
					</tr>
					<c:set var="questionDetailsList" value="${sectionQuestionDetailsMap[record.map.section_id]}"/>
					<c:if test="${not empty questionDetailsList}">
						<tr><td>&nbsp;</td></tr>
						<tr style="width: 100%;">
							<td width="100%">
								<table id="resultTable" width="100%" height="100%" class="detailList">
									<tr>
										<th>Question Details</th>
										<th>Question Order</th>
										<th>Category</th>
										<th>Response Type</th>
									</tr>
									<c:forEach var="item" items="${questionDetailsList}" varStatus="st">
										<c:choose>
						           			<c:when test="${item.map.response_type == 'T'}">
						           				<c:set var="responseType" value="Text"/>
						           			</c:when>
						           			<c:when test="${item.map.response_type == 'Y'}">
						           				<c:set var="responseType" value="Yes/No"/>
						           			</c:when>
						           			<c:when test="${item.map.response_type == 'R'}">
						           				<c:set var="responseType" value="${item.map.rating_type}"/>
						           			</c:when>
          								</c:choose>
										<tr>
          									<td><label><insta:truncLabel value="${item.map.question_detail}" length="35"/></label>
          									<td><label>${item.map.question_order}</td>
          									<td><label><insta:truncLabel value="${item.map.category}" length="35"/></td>
          									<td><label><insta:truncLabel value="${responseType}" length="35"/></td>
										</tr>
									</c:forEach>
								</table>
							</td>
						</tr>
						<tr><td>&nbsp;</td></tr>
					</c:if>
					<tr>
						<td align="right" width="100%">
							<a href="${cpath}/patientfeedback/SurveyFeedbackForms.do?_method=showSection&form_id=${record.map.form_id}&section_id=${record.map.section_id}" title="Edit Feedback Form Section Details">Edit</a>
						</td>
					</tr>
				</table>
			</fieldset>
			</div>
		</c:forEach>
	</div>
	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel">Form Footer</legend>
		<table border="0" width="100%">
			<tr>
				<td colspan="2" style="border-top: none">
					<c:set var="formFooter" value="${ifn:breakContent(fn:escapeXml(bean.map.form_footer))}"></c:set>
					<label  title="${bean.map.form_footer}">
						<c:out value="${formFooter}" escapeXml="false" />
					</label>
				</td>
				<td width="20px" style="border-top: none"></td>
			</tr>
			<tr>
				<td colspan="2" style="border-top: none"></td>
				<td style="border-top: none; width: 20px">
					<a href="${cpath}/patientfeedback/SurveyFeedbackForms.do?_method=show&form_id=${bean.map.form_id}" title="Edit Feedback Form Details">Edit</a>
				</td>
			</tr>
		</table>
	</fieldset>
	<div class="screenActions">
		  <a href="SurveyFeedbackForms.do?_method=addSection&form_id=${bean.map.form_id}">Add New Section</a>
		 | <a href="SurveyFeedbackForms.do?_method=getFeedbackFormsList&sortOrder=form_name&sortReverse=false&form_status=A">Feedback Forms List</a>
	</div>
</form>

</body>
</html>
