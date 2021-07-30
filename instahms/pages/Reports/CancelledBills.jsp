<%@taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@page isELIgnored="false"%>
<%
response.setHeader("Pragma", "no-cache");
response.setHeader("Cache-Control", "no-store");
response.setHeader("Expires", "0");
%>
<html>
<head>
<title>Cancelled Bills - Insta HMS</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<script language="javascript" type="text/javascript"
	src="../../scripts/tableSearch.js"> </script>
<script language="JavaScript" type="text/javascript"
	src="../../scripts/ajax.js"></script>


<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="script" file="date_go.js"/>
<insta:link type="script" file="/dashboard/dashboard.js"/>
<script language="javascript" type="text/javascript">

</script>
</head>
<body onload="initDate();getDoctorList();getDeptList();getuserList();">
	<h1>Cancelled Bill Report</h1>
<form method="POST" >
<!-- main table  start -->
<c:set var="consultantdoctors" value="${docList}"/>
<c:set var="departs" value="${deptList}"/>
<fieldset class="fieldSetBorder">
	<table class="formtable">
		<tr>
			<td class="formlabel" >
				<table>
					<tr>
						<jsp:include page="/pages/Common/DateRangeSelector.jsp">
							<jsp:param name="addTable" value="N"/>
							<jsp:param name="skipWeek" value="Y"/>
						</jsp:include>
					</tr>
				</table>
			</td>
		</tr>

		<tr>
		<td>
			<table>
				<tr>
					<td>Select Doctor:</td>
					<td>Select Department:</td>
					<td>Select User:</td>
				</tr>
				<tr>
					<td><select name="docList" multiple="multiple" size="5" onblur="getCheckValue()">
							<c:forEach var="docList" items="${consultantdoctors}">
							<option value="${docList.DOCTOR_ID}">${docList.DOCTOR_NAME}</option>
							</c:forEach>
						</select>
					</td>

					<td><select name="deptList" multiple="multiple" size="5" onblur="CheckDeptValue()" >
							<c:forEach var="deptList" items="${departs}">
							<option value="${deptList.DEPT_ID}">${deptList.DEPT_NAME}</option>
							</c:forEach>
						</select>
					</td>

					<td><select name="userNameList" multiple="multiple" size="5" onblur="CheckUserNames()">
							<c:forEach var="userNameList" items="${userList}">
								<option value="${userNameList.EMP_USERNAME}">${userNameList.EMP_USERNAME}</option>
							</c:forEach>
						</select>
					</td>
					</tr>
				</table>
			</td>
		</tr>
	</table>
</fieldset>

	<table class="screenActions">
		<tr>
			<td>
			<insta:selectoptions name="printerType" value="pdf" opvalues="pdf,text" optexts="PDF,TEXT" style="width: 5em" />
			<button type="button" name="print" id="print" accesskey="G" class="button" onclick="getCancelledReport();">
			<b><u>G</u></b>enerate Report</button>
			</td>
		</tr>
	</table>

</form>
</body>
</html>
