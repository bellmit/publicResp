<%@taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@page isELIgnored="false"%>
<%
response.setHeader("Pragma", "no-cache");
response.setHeader("Cache-Control", "no-store");
response.setHeader("Expires", "0");
%>
<html>
<head>
<title>Insta HMS</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">


<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="script" file="date_go.js"/>

</head>
<body >
<form method="POST" ><!-- Main tab-->
<!-- main table  start -->
<table  cellpadding="0" cellspacing="0" border="0" width="100%" height="100%">
		<!-- header start -->
	<tr>
		<td width="10" height="20" ></td>
		<td height="5" class="pageheader" width="100%" align="center"></td>
		<td width="10" height="20"></td>
	</tr>
	<!-- header end -->
<tr>
		<td width="10" height="20" ></td>
		<td height="5" class="pageheader" width="100%" align="center"></td>
		<td width="10" height="20"></td>
	</tr>

	<tr align="top" height="10" align="center">
		<td></td>
		<td width="100%" height="10">
			<table cellpadding="0" cellspacing="0" border="0" width="100%" height="100%" align="center">
				<tr>
					<td height="2" width="100%"></td>
				</tr>
				<tr align="center" valign="top" height="10">
					<td align="center"></td>
				</tr>
<!-- time period tr start 	 -->

<!-- time period tr ends -->
				<tr align="center" valign="top" >
					<td align="center"></td>
				</tr>
<!-- category table start -->

<!-- category table end -->
<!-- start of patient type -->

<!-- end of patient type -->


<!-- category table start -->

<!-- category table end -->

<!-- field table end -->

<!-- field table end -->


			</table>
<!-- center table -->
		</td>
		<td></td>
	</tr>
	<tr>
		<td>&nbsp;</td>
	</tr>

	<tr>
		<td>&nbsp;</td>
	</tr>


<!-- footer start -->
	<tr align="center" valign="bottom">
		<td></td>
		<td align="center"><font face="Arial" style="font-size: 11"><jsp:include page="/pages/frame/footer.jsp"/></font></td>
		<td></td>
	</tr>
	<!-- footer start -->

</table>
<!-- main table end -->

</FORM>
</body>
</html>
