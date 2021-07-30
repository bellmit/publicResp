<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<jsp:useBean id="date" class="java.util.Date" />

<c:set var="defaultHospital">
	<fmt:message key="defaultHospital" />
</c:set>
<c:set var="hosp" value="${not empty param.hospitalId ? param.hospitalId : defaultHospital}"/>

<html>
<head>
  <title>Insta HMS</title>
  <insta:link type="script" file="login.js"/>

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
<blockquote style="text-align:center">
<H1>Not Found</H1>
The requested URL was not found.

<p>
</blockquote>

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

