<%@page import="com.insta.hms.master.URLRoute"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Add/Edit Phrase Suggestions Category Master - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="js" file="/masters/phraseSuggestionsCategory.js" />
<c:set var="pagePath" value="<%=URLRoute.PHRASE_SUGGESTIONS_CATEGORY_PATH %>"/>
<script>
 	var chkPhraseSuggestionsCategory = ${ifn:convertListToJson(phraseSuggestionsCategoryList)};
	var phraseSuggestinList =  ${ifn:convertListToJson(phraseSuggestinList)};
	
</script>

</head>
<body >

<form action="create.htm" method="POST" name="phraseSuggestionsCategoryMaster">
	<input type="hidden" name="_method" value="create">
	<h1>Add Phrase Suggestions Category </h1>
	<insta:feedback-panel/>
	<fieldset class="fieldsetborder">
		<legend class="fieldsetlabel">Phrase Suggestions Category Details</legend>

		<table class="formtable">
			<tr>
				<td class="formlabel">Phrase Suggestions Category :</td>
				<td>
					<input type="text" name="phrase_suggestions_category" id="phrase_suggestions_category"  maxlength="100" class="required" title="Phrase Suggestions Category Name is mandatory."><span class="star">*</span>
				</td>
				<td/>
				<td/>
				<td/>
			</tr>
			<tr>
				<td class="formlabel">Status:</td>
				<td><insta:selectoptions name="status" id="status" value="A" opvalues="A,I" optexts="Active,Inactive" /></td>
				<td/>
				<td/>
				<td/>
			</tr>
		</table>
	</fieldset>

	<div class="screenActions">
		<button type="submit" accesskey="S" onclick="return validate();"><b><u>S</u></b>ave</button>
		| <a href="list.htm?sortOrder=phrase_suggestions_category_id&sortReverse=false&status=A">Phrase Suggestions Category List</a>
	</div>
</form>

</body>
</html>
