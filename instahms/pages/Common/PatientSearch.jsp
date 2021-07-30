<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
	<title>Patient Search - Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="js"  file="ajax.js"/>
	<insta:link type="js"  file="patient_search.js"/>
	<script>
		var openerMrnoForm = '${ifn:cleanJavaScript(param.mrnoForm)}';
		var openerMrnoField = '${ifn:cleanJavaScript(param.mrnoField)}';
		var searchType = '${ifn:cleanJavaScript(param.searchType)}';
	</script>
</head>

<body>
	<div class="pageHeader" style="margin-bottom: 1em">
		Patient Search<c:if test="${not empty param.title}">: ${ifn:cleanHtml(param.title)}</c:if>
	</div>

	<form name="searchForm" method="GET">
		<table class="formtable" width="100%" cellpadding="0%" cellspacing="0%">
			<tr>
				<td>First Name:</td>
				<td><input type="text" size="10" name="firstName"></input></td>
				<td>Last Name:</td>
				<td><input type="text" size="10" name="lastName"></input></td>
				<td>Moblie No.:</td>
				<td><input type="text" size="10" name="phone"></input></td>
			</tr>
		</table>

		<table class="formtable" align="center" style="margin-top: 1em">
			<tr>
				<td>
					<input type="submit" value="Search" onclick="doSearch(); return false"/>
				</td>
				<td><input type="reset" value="Reset" onclick="resetAll()"/></td>
			</tr>
		</table>
	</form>

	<div id='searchResults'></div>

	<div id="tooManyResults" style="visibility: hidden">
		<b>Note:</b> The search resulted is more than 100 items, only the first 100 matches are shown.
	</div>
</body>
</html>

