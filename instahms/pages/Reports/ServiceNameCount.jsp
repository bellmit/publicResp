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
	<title>Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="/reports/servicenamecount.js"/>

</head>

<html>
<body onload="onInit()">
	<div class="pageHeader">Service Count Report</div>
	<form name="inputform"  target="_blank"	 method="get">
	<input type="hidden" name="method" value="getReport"/>
	<input type="hidden" name="patientType" value=""/>

		<div class="tipText">
				This report shows the number of services rendered,
				within the chosen date range, for the selected patient type and department.
		</div>


		<table align="center" class="search" width="100%">
			<tr>
				<td colspan="3" align="left">
					<jsp:include page="/pages/Common/DateRangeSelector.jsp">
						<jsp:param name="skipWeek" value="Y"/>
					</jsp:include>
				</td>
			</tr>
		</table>
		<div class="stwMain">
			<table class="search" width="100%" align="center">
				<tr>
					<th>Department Name</th>
					<th>Service Status</th>
				</tr>
				<tr>
					<td>
						<table>
							<tr>
								<td>
									<input type="checkbox" name="all" value="all" onclick="selectDepartments();">All
								</td>
								<td>
									<insta:selectdb  name="deptIdArray"  size = "5" onclick="deselectAll()"  multiple= "true" value=""
 										table="services_departments" valuecol="department" displaycol="department" filtered="false"/>
								</td>
							</tr>
						</table>
					</td>
					<td>
						<table>
							<tr>
								<td>
									<insta:checkgroup name="serviceStatusArray" selValues="${paramValues.serviceStatusArray}"
									opvalues="C,N,P,X" optexts="Conducted,Not Conducted,Partially Conducted,Cancelled" />
								</td>
							</tr>
						</table>
					</td>
				</tr>
			</table>
		</div>
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
