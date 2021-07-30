<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>

<c:set var="cpath" value="${pageContext.request.contextPath}" />

<head>
	<title>Diag Progressive Report - Insta HMS</title>

<script type="text/javascript">
//on page load call
function onInit() {
	setSelDateRange();
}
</script>
	</head>
<html>
	<body onload="onInit();">
		<div class="pageHeader"> Department wise progressive Report </div>
		<form name="inputform" method="GET" action="${cpath}/pages/DiagnosticModule/diagProgressiveDetails.do" target="_blank">
			<input type="hidden" name="method" value="showReport">
			<input type="hidden" name="format" value="pdf">

			<div class="tipText">
				This report  will give you number of tests count and progressive count depends on
				prescribed test date and department.
			</div>
			<table align="center">
			   <jsp:include page="/pages/Common/DateRangeSelector.jsp">
					<jsp:param name="addTable" value="N"/>
					<jsp:param name="skipWeek" value="Y"/>
				</jsp:include>
				<tr></tr>
				<tr>
				    <td width="50%">
				    <button type="submit" name="submit" accesskey="G"><b><u>G</u></b>enerate Report</button>
				    </td>
				</tr>
		   </table>
		</body>
  </html>
