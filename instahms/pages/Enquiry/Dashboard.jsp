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
<title>Implants Report-Insta HMS</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<script language="javascript" type="text/javascript"
	src="../../scripts/tableSearch.js"> </script>
<script language="JavaScript" type="text/javascript"
	src="../../scripts/ajax.js"></script>


<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="script" file="date_go.js"/>
<insta:link type="script" file="/dashboard/dashboard.js"/>
<script language="javascript" type="text/javascript">
var names = '${requestScope.names}'
</script>
</head>
<body onload="initDate();getDoctorList();">
	<div class="pageheader">${requestScope.names}</div>
<form method="POST" ><!-- Main tab-->
       <div class="tipText" style="width:50em">
			${requestScope.note}
	</div>
<!-- main table  start -->

<c:set var="consultantdoctors" value="${doctorList}"/>
<table  align="center">
		<!-- header start
<table  cellpadding="0" cellspacing="0" border="0" height="100%">
		<!-- header start -->
	<!-- header end -->

	<tr align="top" height="10" align="center">
		<td></td>
		<td width="100%" height="10">
			<table cellpadding="0" cellspacing="0" border="0"  align="center">
				<tr>
					<td height="2" width="100%"></td>
				</tr>
				<tr align="center" valign="top" height="10">
					<td align="center"></td>
				</tr>
<!-- time period tr start 	 -->

				<tr align="center" valign="top">
					<td class="totalbg" width="100%" valign="top" align="center">
						<table  border="0" cellpadding="0" cellspacing="0" bordercolor='white'>
							<tr>
 								<jsp:include page="/pages/Common/DateRangeSelector.jsp">
										<jsp:param name="addTable" value="N"/>
										<jsp:param name="skipWeek" value="Y"/>
								</jsp:include>
							</tr>
						</table>
			<c:choose>
			<c:when test="${names=='Dashboard - Doctors' && not empty consultantdoctors}" >
			<c:set var="docList" value="docList"/>
			<div>
				<table align="center" cellpadding="4">
					<tr>
						<td>Select Doctor</td>
						<td><select name="docList" multiple="multiple" onblur="getCheckValue()" >
								<c:forEach var="docList" items="${consultantdoctors}">
								<option value="${docList.DOCTOR_ID}">${docList.DOCTOR_NAME}</option>
								</c:forEach>
							</select>
						</td>
					</tr>
				</table>
			</div>

				</c:when>
				<c:otherwise>
				<c:set var="docList" value=""/>
				</c:otherwise>
				</c:choose>

<!-- center table -->
	<table align="center" style="margin-top: 1em;">
		<tr>
			<td align="center" colspan="4">
			<insta:selectoptions name="printerType" value="pdf" opvalues="pdf,text" optexts="PDF,TEXT" style="width: 5em" />
			<button type="button" name="print" id="print" accesskey="G" class="button" onclick="getDashboard();">
			<b><u>G</u></b>enerate Reoport</button>
			</td>
		</tr>


</table>
	<!-- footer start -->


<!-- main table end -->

</td>
</tr>
</table>
</td>
</tr>
</table>
</form>
</body>
</html>
