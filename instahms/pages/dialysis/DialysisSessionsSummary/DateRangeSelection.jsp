<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<html>
<head>
<script>
	function checkValidation() {
		if (document.getElementById('fromDate').value == '') {
			showMessage('js.dialysismodule.commonvalidations.from.date');
			return false;
		}
		if (document.getElementById('toDate').value == '') {
			showMessage('js.dialysismodule.commonvalidations.to.date');
			return false;
		}
		return true;
	}
</script>
<meta name="i18nSupport" content="true"/>
<insta:js-bundle prefix="dialysismodule.commonvalidations"/>
</head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<body>
	<h1><insta:ltext key="patient.dialysis.sessions.daterangeselection.heading"/></h1>
	<form name="flowsheetform" action="${cpath}/dialysis/DialysisSessionsSummary.do" method="GET" target="_blank">
		<input type="hidden" name="_method" value="getFlowSheetReport" />
		<input type="hidden" name="mr_no" value="${ifn:cleanHtmlAttribute(mrNo)}" />

			<fieldset class="fieldSetBorder">
			<table class="formtable">
				<tr>
					<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.daterangeselection.from"/></td>
					<td><insta:datewidget name="fromDate" id="fromDate"/></td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.daterangeselection.to"/></td>
					<td><insta:datewidget name="toDate" id="toDate"/></td>
				</tr>
			</table>
			</fieldset>
			<div class="screenActions">
				<input type="submit" target="_blank" value="Get Report" onclick="return checkValidation();"/> |
				<a href="javascript:void(0);" onclick="window.location.href='${cpath}/dialysis/DialysisSessionsSummary.do?_method=list&mr_no=${ifn:cleanURL(mrNo)}'"><insta:ltext key="patient.dialysis.sessions.daterangeselection.session.summery"/></a>
			</div>
	</form>
</body>
</html>