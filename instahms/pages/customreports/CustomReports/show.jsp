<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<title>Custom Report Parameters - Insta HMS</title>
</head>

<body>
	<div class="pageHeader">${report.map.report_name}</div>

	<form name="inputform" method="GET" target="_blank" action="${cpath}/customreports/CustomReports.do">
		<input type="hidden" name="method" value="runReport">
		<input type="hidden" name="id" value="${report.map.report_id}">
		<div class="helpPanel">${report.map.report_desc}</div>

		<c:if test="${fn:length(reportvars) > 0}">
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel">Report Parameters</legend>

    		<table class="formtable">
					<c:forEach var="rptvar" items="${reportvars}">
						<tr>
							<td class="formlabel">${rptvar.map.report_var_label}:</td>
							<td>
								<c:choose>
									<c:when test="${rptvar.map.report_var_type == 'D'}">
										<insta:datewidget name="${rptvar.map.report_var}" value="today"/>
									</c:when>
									<c:otherwise>
										<input type="text" name="${rptvar.map.report_var}" />
									</c:otherwise>
								</c:choose>
							</td>

							<td class="formlabel"></td><td></td>
							<td class="formlabel"></td><td></td>
						</tr>
					</c:forEach>
				</table>
			</fieldset>
		</c:if>

		<div style="margin-top: 15px">
			<c:choose>
				<c:when test="${report.map.report_type == 'ftl'}">
					<button type="submit" name="_format" value="pdf">Run Report (PDF)</button>
					<c:if test="${report.map.ftl_csv_supported == 'Y'}">
						<button type="submit" name="_format" value="csv">Run Report (CSV)</button>
					</c:if>
				</c:when>
				<c:otherwise>
					<button type="submit">Run Report</button>
				</c:otherwise>
			</c:choose>
		</div>
	</form>

</body>
</html>

