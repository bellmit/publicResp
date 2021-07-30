<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<title><insta:ltext key="registration.exportpatientdata.report.title"/></title>

    <insta:link type="js" file="hmsvalidation.js"/>
	<script>


	function init(){
		setDateRangeYesterday(document.forms[0].fromDate, document.forms[0].toDate);
	}

	function validate() {
		var valid = validateFromToDate(document.forms[0].fromDate, document.forms[0].toDate);
			if (!valid)
				return false;
			document.forms[0].submit();

	}
	</script>
</head>


	<body onload="init();" >
		<div class="pageHeader"><insta:ltext key="registration.exportpatientdata.report.exportpatientdata"/></div>
		<form name="inputform" method="GET" target="_blank"	>
		<input type="hidden" name="_method" value="getReport" />


			<div class="tipText">
				<insta:ltext key="registration.exportpatientdata.report.reporttemplate"/>.

			</div>

			<jsp:include page="/pages/Common/DateRangeSelector.jsp">
				<jsp:param name="skipWeek" value="Y"/>
			</jsp:include></br>

			<table align="center" style="margin-top: 1em">
				<tr>
					<td>
						<button type="button" accesskey="G" onclick="return validate();"><b><u><insta:ltext key="registration.exportpatientdata.report.g"/></u></b><insta:ltext key="registration.exportpatientdata.report.eneratereport"/></button>
					</td>
			</table>

		</form>
	</body>
</html>

