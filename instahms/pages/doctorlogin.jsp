<%@ taglib prefix="c" uri="/WEB-INF/c.tld"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld"  prefix="bean" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<jsp:useBean id="date" class="java.util.Date" />

<html >
<head>
  <title>Doctor Login - Insta HMS</title>
  <insta:link type="script" file="login.js"/>
  <insta:link type="script" file="hmsvalidation.js" />

  <style type="text/css">
    #modules {margin-top: 3em; margin-bottom: 5em;}
    .corner_tl { background-image: url(images/corner_tl.gif); background-repeat: no-repeat;
        background-position: top left;
    }
    .corner_tr { background-image: url(images/corner_tr.gif); background-repeat: no-repeat;
        background-position: top right;
    }
		td.message { color: red }
  </style>
</head>

<body onload="clear()">

<!-- top bar -->
<table border="0" cellpadding="0" cellspacing="0" width="100%" style="border-collapse: collapse"
          class="headerbg">
  <tr>
    <td class="corner_tl" width="2">&nbsp;</td>
    <td align="right" width="100%" class="headerbg">&nbsp;
    </td>
    <td class="corner_tr" width="2">&nbsp;</td>
  </tr>
</table>

<insta:link type="image" file="InstaLogoGradient.jpg"/>

<form action="login.do" onsubmit="return validate()" method="post">

<div class="pageHeader" align="center" style="margin-top: 150px">Doctor Portal Login</div>

<table class="formTable" align="center" border="0" cellpadding="0" cellspacing="0">
	<tr>
		<td class="message" colspan="2" align="center">${ifn:cleanHtml(login_status)}</td>
	</tr>
	<tr>
		<td class="label">Hospital:</td>
		<td class="inputfield">
			<input type="text" name="hospitalId" size="20" maxlength="32" value="${ifn:cleanHtmlAttribute(param.hospitalId)}"/>
			<font color="red">*</font>
		</td>
	</tr>
	<tr>
		<td class="label">User Name:</td>
		<td class="inputfield">
			<input type="text" name="userid" size="20" maxlength="20" value="${ifn:cleanHtmlAttribute(param.userId)}" onkeypress="return enterAlphanNumerical(event);"/>
			<font color="red">*</font>
		</td>
	</tr>
	<tr >
		<td class="label">Password:</td>
		<td class="inputfield">
			<input type="password" name="password" size="20" maxlength="20"/>
			<font color="red">*</font>
		</td>
	</tr>
</table>

<div align="center" style="margin-top: 2em; margin-bottom: 100px;">
	<input type="submit" name="submit" value="Submit"/>
</div>
<div style="text-align:center; margin-top: 30px">
        <span class="footer">
          <a href="http://www.instahealthsolutions.com/" class="footer-supportlink"
             title="Visit the web site (opens in a new window)" target="_blank">
             Insta by Practo.
          </a> Version <fmt:message key="insta.software.version" />.
           Copyright &copy; <fmt:formatDate value="${date}" pattern="yyyy" /> Practo Technologies Pvt. Ltd. All Rights Reserved.
        </span>
</div>

</body>
</body>
</html>

