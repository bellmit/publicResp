<%@ taglib uri="/WEB-INF/struts-html.tld"  prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld"  prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld"  prefix="logic" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>

<html:html>
<head>
<meta http-equiv="Content-Language" content="en-us">
<title><insta:ltext key="billing.authorization.info.unauthorized"/></title>
</head>
<body >

<p>&nbsp;</p>
<p>&nbsp;</p>
<p>&nbsp;</p>

<table border="2" align="center" cellpadding="3" cellspacing="0" style="border-collapse: collapse" bordercolor="#111111" width="750"    height="100">
  <tr>
  <td height="20" bgcolor="#82B98E" align="center"><div align="center"><b><font color="white"><insta:ltext key="billing.authorization.info.unauthorizeduser"/></font></b></div></td>
  </tr>
  <tr>
    <td width="750" align="center" height="50"><insta:ltext key="billing.authorization.info.notauthorizetoedit.view"/></td>
  </tr>
</table>

<p align="center"><input type="button" name="b" value="Back" class="button" onclick="history.back();"/></p>


</body>
</html:html>
