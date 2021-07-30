<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Add/Edit Phrase Suggestions Master - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="js" file="/masters/phraseSuggestions.js" />

<script>

</script>

</head>
<body >

<form action="update.htm" method="POST" name="phraseSuggestionsMaster">
	<c:set var="selectPrompt">
			<insta:ltext key="selectdb.dummy.value"/>
	</c:set>
	<input type="hidden" name="_method" value="update">
	<input type="hidden" name="phrase_suggestions_id" id="phrase_suggestions_id" value="${bean.phrase_suggestions_id}"/>

	<h1>Edit Phrase Suggestion </h1>
	<insta:feedback-panel/>
	<fieldset class="fieldsetborder">
		<legend class="fieldsetlabel">Phrase Suggestion Details</legend>

		<table class="formtable">
			<tr>
				<td class="formlabel">Department: </td>
				<td>
				<select class="dropdown" name="dept_id" id="dept_id" >
					<option value="">-- Select --</option>
					<c:forEach items="${phraseSuggestionsDeptList}" var="PhraseSugDeptList">
						<option value="${PhraseSugDeptList.dept_id}" ${bean.dept_id == PhraseSugDeptList.dept_id ?'selected' : ''}>
							${PhraseSugDeptList.dept_name}</option>
					</c:forEach>
				</select>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Phrase Suggestion Category:</td>
				<td>
					<select class="dropdown" name="phrase_suggestions_category_id" id="phrase_suggestions_category_id" >
						<option value="">-- Select --</option>
						<c:forEach items="${phraseSuggCategoryList}" var="PhSugCatList">
							<option value="${PhSugCatList.phrase_suggestions_category_id}" ${bean.phrase_suggestions_category_id == PhSugCatList.phrase_suggestions_category_id ? 'selected' : ''}>
								${PhSugCatList.phrase_suggestions_category}</option>
						</c:forEach>
					</select><span class="star">*</span>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Phrase Suggestion:</td>
				<td>
					<input type="text" name="phrase_suggestions_desc" id="phrase_suggestions_desc" value="<c:out value="${bean.phrase_suggestions_desc}"/>" maxlength="500" class="required" title="Phrase Suggestion Name is mandatory."><span class="star">*</span>
				</td>
				<td/>
				<td/>
				<td/>
			</tr>

			<tr>
				<td class="formlabel">Status:</td>
				<td><insta:selectoptions name="status" id="status" value="${bean.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
			</tr>
		</table>
	</fieldset>

	<div class="screenActions">
		<button type="submit" accesskey="S" onclick="return validate();"><b><u>S</u></b>ave</button>
		| <a href="add.htm" >Add</a>
		| <a href="list.htm?sortOrder=phrase_suggestions_id&sortReverse=false&phrasestatus=A">Phrase Suggestions List</a>
	</div>
</form>

</body>
</html>
