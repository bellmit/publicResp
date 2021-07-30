<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Edit System Generated Section- Insta HMS</title>
	<insta:link type="script" file="hmsvalidation.js"/>

</head>
<body class="yui-skin-sam">
	<h1> ${bean.section_name} Section</h1>
	<insta:feedback-panel/>
	<form action="update.htm" method="POST">
	<c:set var="opFollowupFormRights" value="${preferences.modulesActivatedMap['mod_develop'] eq 'Y'}"/>	
		<input type="hidden" name="section_id" value="${bean.section_id}">
		<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel">Center Details</legend>
			<table class="formtable">
				<tr>
					<td class="formlabel">Section Name: </td>
					<td class="forminfo">${bean.section_name}</td>
					<td class="formlabel"></td>
					<td></td>
					<td class="formlabel"></td>
					<td></td>
				</tr>
				<tr>
					<td class="formlabel">Data Mandatory: </td>
					<td>
						<select name="section_mandatory" class="dropdown validate-not-first" title="Please select the value for Data Mandatory field.">
							<option value="">-- Select --</option>
							<option value="Y" ${bean.section_mandatory ? 'selected' : ''}>Yes</option>
							<option value="N" ${!bean.section_mandatory ? 'selected' : ''}>No</option>
						</select>
						<img class="imgHelpText" title="In IP Forms the Section Mandatory is applied only when the form is 'Save & Close'" src="${cpath}/images/help.png" style="float:right">
					</td>
				</tr>
				<tr>
					<td class="formlabel">OP Consultation: </td>
					<td class="forminfo">
						${bean.op == 'Y' ? 'Yes' : 'No'}
					</td>
				</tr>
				<tr>
					<td class="formlabel">IP Record: </td>
					<td class="forminfo">
						${bean.ip == 'Y' ? 'Yes' : 'No'}
					</td>
				</tr>
				<tr>
					<td class="formlabel">Surgery/Operation Theatre Management: </td>
					<td class="forminfo">
						${bean.surgery == 'Y' ? 'Yes' : 'No'}
					</td>
				</tr>
				<tr>
					<td class="formlabel">Triage: </td>
					<td class="forminfo">
						${bean.triage == 'Y' ? 'Yes' : 'No'}
					</td>
				</tr>
				<tr>
					<td class="formlabel">Initial Assessment: </td>
					<td class="forminfo">
						${bean.initial_assessment == 'Y' ? 'Yes' : 'No'}
					</td>
				</tr>
				<tr>
					<td class="formlabel">Generic Form: </td>
					<td class="forminfo">
						${bean.generic_form == 'Y' ? 'Yes' : 'No'}
					</td>
				</tr>
				<c:if test="${opFollowupFormRights}">
					<tr>
						<td class="formlabel">OP Follow Up Form: </td>
						<td class="forminfo">
							${bean.op_follow_up_consult_form == 'Y' ? 'Yes' : 'No'}
						</td>
					</tr>
				</c:if>	
				<tr>
					<c:choose>
					<c:when test="${param.section_id == -1}">
					<td class="formlabel">Phrase Category: </td>
					<td>
							<select name="field_phrase_category_id" id="field_phrase_category_id" class="dropdown">
							<option value="">--Select--</option>
									<c:forEach items="${phraseSuggestionsCategoryList}" var="category">
										<option value="${category.phrase_suggestions_category_id}" ${bean.field_phrase_category_id == category.phrase_suggestions_category_id ? 'selected':''}>${category.phrase_suggestions_category}</option>
									</c:forEach>
								</select>
					</td>
					</c:when>
					</c:choose>
				</tr>
				<c:choose>
					<c:when test="${param.section_id == -14}">
						<tr>
							<td class="formlabel">EDD Expression: </td>
							<td class="forminfo">
								<label>LMP + </label>
								<input type="text" id="edd_expression_value" name="edd_expression_value" value="${bean.edd_expression_value}" 
									onkeypress="return enterNumOnlyzeroToNine(event)" style="width: 35px"/>
								<label>Days</label>
							</td>
						</tr>
					</c:when>				
				</c:choose>
			</table>
		</fieldset>
		<table style="margin-top: 10px">
				<tr>
					<td>
						<input type="submit" name="Save" value="Save" />
						| <a href="list.htm">Sections List</a>
						<c:if test="${bean.section_id != -1 && bean.section_id != -6 &&
							bean.section_id != -7 && bean.section_id != -4 && bean.section_id != -3}">
						<insta:screenlink addPipe="true" screenId="mas_section_role_rights"
							label="Section Role Rights"
							extraParam="?_method=edit&section_id=${bean.section_id}" />
						</c:if>
					</td>
				</tr>
			</table>
	</form>
</body>
</html>