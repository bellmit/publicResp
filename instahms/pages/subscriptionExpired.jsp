<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.common.Encoder" %>
<c:set var="defaultHospital">
    <fmt:message key="defaultHospital" />
</c:set>
<c:set var="hosp" value="${not empty param.hospital ? param.hospital : defaultHospital}" />
<c:set var="cpath" value="${pageContext.request.contextPath}" />

<html>
<head>
<title>Subscription Expired - Insta HMS</title>

<insta:link type="css" file="login.css" />
<insta:link type="css" file="buttonStyle.css" />

<insta:link type="script" file="jquery-2.2.4.min.js" />
<insta:link type="script" file="login.js" />
<insta:link type="script" file="usermanager/checkPasswordStrength.js" />
<insta:link type="script" file="loginAnimation.js" />

<script>
    var cpath = '${cpath}';
</script>

</head>

<body onload="init()">
    <div class="login-card">
        <div class="insta-header">
            <img class="insta-header-image" src="./images/login/insta.png">
            <span class="insta-header-content">
               Hospital Management System
            </span>
        </div>
        <div class="login-details-card">
            <table class="login-card-table" >
                <tr>
                    <td class="login-card-image">
                        <div class="login-card-image-logo">
                            <img src="./images/hospitalLogo/Logo.png">
                        </div>
                    </td>
                    <td class="login-card-details">
                        <table class="table-spec">
                            <tr>
                                <td class="login-header"><insta:ltext key="ui.label.subscription.expired.allcaps"/></td>
                            </tr>
                            <tr>
                                <td class="expired-inst"><insta:ltext key="ui.message.instacollection.rental.expired"/></td>
                            </tr>                                        
                            <tr>
                                <td style="padding:20px 10px;">
                                   <a href="${cpath}" class="return-link">Return to Login Page</a>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>
        </div>
    </div>
    <!-- Footer starts Here -->
    <div class="footer">
        <div class="footer-image" align="center" >
            <a href="https://www.practo.com/providers" class="footer-supportlink"
               title="Visit the web site (opens in a new window)" target="_blank">
                <img class="footer-image-practo" src="./images/login/practo.png">
            </a>
        </div>
        <div align="center" class="footer-content">
            <a href="http://www.instahealthsolutions.com/" class="footer-supportlink"
               title="Visit the web site (opens in a new window)" target="_blank">
               Insta by Practo.
            </a> Version <fmt:message key="insta.software.version" />.
             Copyright &copy; <fmt:formatDate value="${date}" pattern="yyyy" /> Practo Technologies Pvt. Ltd. All Rights Reserved.
        </div>
    </div>
    <!-- Footer ends starts Here-->

</body>
</html>

