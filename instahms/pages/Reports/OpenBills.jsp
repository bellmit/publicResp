<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>

<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>

<head>
	<title>Open Bills Report - Insta HMS</title>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<script>
		function init() {
			document.getElementById('pd').checked = true;
			setDateRangeYesterday(document.inputform.fromDate, document.inputform.toDate);
		}
		function validateFields() {
			if(!validateFromToDate(document.inputform.fromDate, document.inputform.toDate)) return false;
			return true;
		}
	</script>
</head>
<body onload="init();">
		<div class="pageHeader">Open Bills Report</div>
		<form name="inputform" method="GET" target="_blank">
			<input type="hidden" name="method" value="getReport"/>

<%--
			<div class="tipText" align="center">
				This report lists Open Bills  in the given time period of bill opened date.
			</div>
			<table align="center">
				<jsp:include page="/pages/Common/DateRangeSelector.jsp">
					<jsp:param name="addTable" value="N"/>
					<jsp:param name="skipWeek" value="Y"/>
				</jsp:include>
			</table>
			--%>
			<center><br/>
			<label>Filter Options:</label>
			<div class="stwMain" style="width:40%;">
			<table width="100%" height="15%" class="search">
			<tr>
				<th>Patient Type</th>
				<th>Bill Type</th>
			</tr>
			<tr>
				<td>
					<insta:checkgroup name="patientTypeArray" optexts="OP,IP" opvalues="o,i"/>
				</td>
				<td>
					<insta:checkgroup name="billTypeArray" optexts="Bill Now,Bill Later" opvalues="P,C"/>
				</td>
			</tr>
			</table>
			</div>
			</center>

			<table align="center" style="margin-top: 1em">
			<tr>
				<td><insta:selectoptions name="printerType" value="pdf" opvalues="pdf,text" optexts="PDF,TEXT" style="width: 5em" />
					<button type="submit" accesskey="G"
						onclick="return validateFields();"><b><u>G</u></b>enerate Report</button>
				</td>
			  </tr>
			</table>

		</form>
	</body>
</html>

