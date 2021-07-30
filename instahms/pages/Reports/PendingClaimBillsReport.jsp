<%@taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@page isELIgnored="false"%>

<%
response.setHeader("Pragma", "no-cache");
response.setHeader("Cache-Control", "no-store");
response.setHeader("Expires", "0");
%>

<c:set var = "contextPath" value="${pageContext.request.contextPath}"/>

<head>
	<title>Pending Claim Bills Report-Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="css" file="widgets.css"/>
	<script>

	var fromDate, toDate;

		function onInit() {

			fromDate = document.inputform.fromDate;
			toDate = document.inputform.toDate;
			setDateRangeYesterday(fromDate, toDate);

		}

	function validateCategory(){
			if (validateFromToDate(document.inputform.fromDate,document.inputform.toDate)){
				return true;
			}else{
				return false;
			}
		}
	</script>
</head>

<html>
<body onload="onInit()">
	<div class="pageHeader">Pending Claim Bills Report</div>
	<form name="inputform"  target="_blank"	 method="get">
	<input type="hidden" name="method" value="getReport"/>
	<input type="hidden" name="patientType" value=""/>

		<div class="tipText">
				This report shows the bills which are pending for raise claim processing,
				(i.e. the bills of insurance cases, which have not yet been submitted to the insurance company)
				for cases opened within the specified date range.
		</div>


		<table align="center" class="search" width="100%">
			<tr>
				<td align="center">
				<b>
				Cases opened within:
				</b>
				</td>
			</tr>
			<tr>
				<td colspan="3" align="left">
					<jsp:include page="/pages/Common/DateRangeSelector.jsp">
						<jsp:param name="skipWeek" value="Y"/>
					</jsp:include>
				</td>
			</tr>
		</table>
		<table align="center" style="margin-top: 1em;">
			<tr>
				<td>
					<button type="submit" accesskey="G"
						onclick="return validateCategory()"><b><u>G</u></b>enerate Report</button>
				</td>
			</tr>

		</table>
	</form>
</body>
</html>


















