<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<jsp:useBean id="date" class="java.util.Date" />

<c:set var="defaultHospital">
	<fmt:message key="defaultHospital" />
</c:set>
<c:set var="hosp"
	value="${not empty param.hospital ? param.hospital : defaultHospital}" />
<c:set var="cpath" value="${pageContext.request.contextPath}" />

<html>
<head>
<title>Login - Insta HMS</title>
<insta:link type="css" file="style.css" />
<jsp:include page="/pages/yuiScripts.jsp" />
<insta:link type="script" file="login.js" />


</head>

<body onload="clear()">

	<div
		style="position: absolute; padding: 0px; z-index: 2; bottom: 85%; top: 4%; left: 36%; width: 300px;">
		<img src="${cpath}/images/login_logo.png" width="192" height="74" />
	</div>
	<div>
		<img id="background" src="${cpath}/images/bg_login.png" />
	</div>
	<div
		style="position: absolute; padding: 20px; z-index: 5; bottom: 25%; right: 25%; top: 35%; left: 25%; width: 600px;">
		<div>

			<div class="message" align="center">${ifn:cleanHtml(login_status)}</div>
			<form action="login.do" onsubmit="return validate()" method="post">
				<table width="100%" cellspacing="0" cellpadding="0"
					class="formtable">
					<tr>
						<td style="width: 150px; float: left" class="txtRT formpglabel">&nbsp;</td>
						<td style="width: 138px; float: left" class="formpg"><h1>User
								Login</h1></td>
					</tr>
					<tr>
						<td style="width: 150px; float: left" class="formlabel">Hospital:</td>
						<td style="width: 138px; float: left" class="forminfo"><input
							name="hospital" type="text" class="field" id="hospital"
							value="${hosp}" /></td>
					</tr>
					<tr>
						<td style="width: 150px; float: left" class="formlabel">User
							Name:</td>
						<td style="width: 138px; float: left" class="forminfo"><input
							name="userId" type="text" class="field" id="userId" value="" /></td>
					</tr>
					<tr>
						<td style="width: 150px; float: left" class="formlabel">Password:</td>
						<td style="width: 138px; float: left" class="forminfo"><input
							name="password" type="password" class="field" id="password"
							value="" /></td>
					</tr>
					<tr>
						<td style="width: 150px; float: left" class="formlabel">&nbsp;</td>
						<td style="width: 150px; float: left" class="forminfo"><input
							type="submit" class="button" name="button" id="button"
							value="Submit" /></td>
					</tr>
				</table>
			</form>
		</div>
		<div class="foottertxt"
			style="margin-top: 15%; text-align: center; text-align: left;">
          <a href="http://www.instahealthsolutions.com/" class="footer-supportlink"
             title="Visit the web site (opens in a new window)" target="_blank">
             Insta by Practo.
          </a> Version <fmt:message key="insta.software.version" />.
           Copyright &copy; <fmt:formatDate value="${date}" pattern="yyyy" /> Practo Technologies Pvt. Ltd. All Rights Reserved.

		</div>
	</div>
</body>
</body>
</html>

