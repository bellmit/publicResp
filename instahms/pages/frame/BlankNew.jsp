<%@ taglib uri="/WEB-INF/struts-html.tld"  prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld"  prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld"  prefix="logic" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@page import="com.bob.hms.common.AutoIncrementId,java.util.ArrayList,java.util.Iterator" %>
<html>
<head>
	<title>Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="script" file="aw.js"/>
	<insta:link type="css" file="aw.css"/>
</head>
<body class="setMargin">
<form method="post">

<table border="0" width="100%" height="100%" cellpadding="0%" cellspacing="0%">
   <tr>
	  <td></td>
	  <td width="98%" align="center">
	  <span class="pageHeader">
	  <!--You can specify page header here -->
	  </span>
	  </td>
	  <td ></td>
	</tr>
	 <tr>
		  <td height="96%" class="leftLine" width="0%"></td>
		  <td valign="top" height="100%" >
			<table border="0" width="100%" height="100%" cellpadding="0%" cellspacing="0%">
			<tr>
			<td width="100%" height="10px" class="topLineDownSpace"><span class="resultMessage">
			<logic:present name="message">
				<bean:write name="message"/>
			</logic:present>
			</span></td>
			</tr>
			<tr>
			<td class="totalBG" height="100%" valign="top" width="0%">
			<table  border="0" width="100%" height="100%" cellpadding="0%" cellspacing="0%">

					<tr>
						<!-- To maintain specific height from the outlet BEGIN -->
						<td colspan="2" height="20px" width="0%"></td>
						<!-- To maintain specific height from the outlet BEGIN -->
					</tr>
					<tr>
					<!-- To maintain specific width from the outlet BEGIN -->
					<td width="25%"></td>
					<!-- To maintain specific width from the outlet BEGIN -->
					<td  width="100%">
					<!--  Actual Design begins in below table -->
						<!--  DESIGN BEGIN  -->
						<table cellpadding="0" cellspacing="0"  border="0">
						<tr>
						<td>
							<!--  HERE U Can begin your design  -->
						</td>
						</tr>
						</table>
						<!--  DESIGN END  -->
					</td>
				</tr>
			</table>
			</td>
			</tr>
			</table>
		  </td>
		  <td height="100%" class="rightLine"></td>
	 </tr>
	  <tr>
	  <td class="bottomLeftCurve"></td>
	  <td width="96%" class="bottomLine"></td>
	  <td class="bottomRightCurve"></td>
	</tr>
  </table>
</form>
</body>
</html>
