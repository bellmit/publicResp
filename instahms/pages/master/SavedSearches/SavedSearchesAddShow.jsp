<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Edit Search - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>

</head>
<body >

<form action="${cpath}/master/SavedSearches/update.htm" method="POST" name="searchMaster">
	<input type="hidden" name="_method" value="update">
	<input type="hidden" name="search_id" value="${bean.search_id}"/>
	<input type="hidden" name="action_id" value="${bean.action_id}"/>

	<h1>Edit Search</h1>
	<insta:feedback-panel/>
	<fieldset class="fieldsetborder">
		<table class="formtable">
			<tr>
				<td class="formlabel">Search Name:</td>
				<td><input type="text" name="search_name" value="${bean.search_name }"
						title="Name is required and max length of name can be 100" class="required validate-length" length="100"/></td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td class="formlabel">User Name:</td>
				<td>
					<input type="text" name="user_name" value="${bean.user_name}" length="100" readonly/>
				</td>
			</tr>

			<tr>
				<td class="formlabel">Screen Name:</td>
                 <td><input type="text" name="screen_name" value="${bean.screen_name}" readonly></td>
			</tr>
		</table>

	</fieldset>

	<div class="screenActions">
		<c:url value="/master/SavedSearches/list.htm" var="savedSearchesList">
			<c:param name="sortOrder" value="search_name"/>
			<c:param name="sortReverse" value="false"/>
		</c:url>
		<c:set var="enableUpdate" value="${roleId == 1 || roleId == 2 || userid == bean.user_name ? '' : 'disabled'}"/>
		<button type="submit" accesskey="U"  value="" ${enableUpdate}><b><u>U</u></b>pdate</button>
		| <a href="${savedSearchesList}" title="Saved Searches List">Saved Searches List</a>
	</div>
</form>

</body>
</html>
