<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.common.Encoder" %>
<jsp:useBean id="date" class="java.util.Date" />
<c:set var="defaultHospital">
    <fmt:message key="defaultHospital" />
</c:set>
<c:set var="hosp" value="${not empty param.hospital ? param.hospital : defaultHospital}" />
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="ssoLoginOnly" value="${not empty initParam['ssoLoginOnly'] && initParam['ssoLoginOnly'] eq 'true' }" />

<html>
<head>
<title>Login - Insta HMS</title>

<insta:link type="css" file="login.css" />
<insta:link type="css" file="buttonStyle.css" />

<insta:link type="script" file="jquery-2.2.4.min.js" />
<insta:link type="script" file="login.js" />
<insta:link type="script" file="usermanager/checkPasswordStrength.js" />
<insta:link type="script" file="usermanager/showPassword.js" />
<insta:link type="script" file="loginAnimation.js" />
<insta:link type="css" file="../styles/font-awesome.min.css"/>

<script>
    var cpath = '${cpath}';
    var ssoLoginOnly = ${initParam['ssoLoginOnly']};
    var loginStatus = '${ifn:cleanJavaScript(login_status)}';
    var notifyPasswordChange = '${notifyPasswordChange}';
    var notifyDays = '${notifyDays}';
    var hosp = '${ifn:cleanJavaScript(hosp)}';
    var passwordRules = '${passwordRules}'===''?'':JSON.parse('${passwordRules}');
</script>
<style>
	.sso-only-spec {
		margin-top: 85px;
		margin-bottom: 117px;
	}
	.sso-only-spec.button-spec {
		float:none;
		text-align:center;
	}
</style>
</head>

<body onload="init()">
    <c:choose>
        <c:when test="${!notifyPasswordChange && login_status!='blockUser' && login_status!='errorInUpdatePassword'}">
            <form action="login.do" onsubmit="return validate()" method="post">
                <input id="hashFragment" type="hidden" name="hashFragment" value="" /> 
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
                                            <td class="login-header">LOGIN</td>
                                        </tr>
                                        <c:if test="${not empty license_status}">
                                            <tr>
                                                <td class="error"><insta:ltext key="ui.message.instacollection.rental.expired"/></td>
                                            </tr>                                        
                                        </c:if>
										<c:if test="${empty ssoLoginOnly or not ssoLoginOnly}">
	                                        <tr>
	                                            <td>
	                                                <div class="form-input">
	                                                    <label for="hospital" id="hospital-Label"> 
	                                                        Hospital
	                                                    </label> 
	                                                    <input name="hospital" type="text" class="field"
	                                                        id="hospital" value="${hosp}"> 
	                                                    <span class="spin"></span>
	                                                </div>
	                                            </td>
	                                        </tr>
	                                        <tr>
	                                            <td>
	                                                <div class="form-input">
	                                                    <label for="userId" id="userId-Label"> 
	                                                        User Name 
	                                                    </label> 
	                                                    <input name="userId" type="text" class="field" id="userId" /> 
	                                                    <span class="spin"></span>
	                                                </div>
	                                            </td>
	                                        </tr>
	                                        <tr>
	                                            <td>
	                                                <div class="form-input" style="display:flex;justify-content:space-between;">
                                                        <label for="password" id="password-Label"> Password </label>
                                                        <input name="password" type="password" class="field"
                                                            id="password" onchange="listner()" />
                                                        <i class="fa fa-eye" onclick="toggleShowPassword('password','togglePassword');" id="togglePassword" style="padding-top:42px;padding-right:5px;color:#787887;cursor:pointer"></i>
                                                    </div>
	                                            </td>
	                                        </tr>
    									</c:if>
                                        <c:choose>
                                            <c:when
                                                test="${login_status eq 'Password successfully changed. Please login with your new password.' }">
                                                <tr>
                                                    <td class="login-success">${ifn:cleanHtml(login_status)}</td>
                                                </tr>
                                            </c:when>
                                            <c:otherwise>
                                                <tr>
                                                    <td class="error">${ifn:cleanHtml(login_status)}</td>
                                                </tr>
                                            </c:otherwise>
                                        </c:choose>
                                        <tr>
                                            <td>
                                                <div class="button-spec ${empty ssoLoginOnly or not ssoLoginOnly ? '' : 'sso-only-spec'}">
                                                	<c:if test="${ssoEnabled}">
	                                                    <button type="${empty ssoLoginOnly or not ssoLoginOnly ? 'submit' : 'button'}" class="btn-spec-secondary" 
	                                                    	name="windows-sso-button">Login using Windows SSO</button>
                                                    </c:if>
                                                    <c:if test="${empty ssoLoginOnly or not ssoLoginOnly}">
                                                    <button type="submit" class="btn-spec login-button"
                                                        id="login" name="button">Login</button>
                                                    </c:if>
                                                </div>
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
                        </table>
                    </div>
                </div>
            </form>
        </c:when>

        <c:when test="${login_status == 'blockUser'}">
            <div class="login-card" id="blockUser">
                <div class="insta-header">
                    <img class="insta-header-image" src="./images/login/insta.png">
                        <span class="insta-header-content">
                            Hospital Management System
                        </span>
                </div>
                <div class="login-details-card">
                    <table class="login-card-table">
                        <tr>
                            <td class="login-card-image">
                                <div class="login-card-image-logo">
                                    <img src="./images/hospitalLogo/Logo.png" class="img-responsive">
                                </div>
                            </td>
                            <td class="login-card-details">
                                <table class="table-spec">
                                    <tr>
                                        <td class="warning-spec">
                                            <img src="./images/warningExclamation.png">
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="warning-inst">
                                            <div class="login-header-inst waring-inst-spec-expired">
                                                <table>
                                                     <tr>
                                                        <td class="login-header-inst sen">
                                                            If you are a new user or
                                                        </td>
                                                    </tr> 
                                                    <tr class="login-header-inst">
                                                        <td class="sen">
                                                            need to change your password as per 
                                                        </td>
                                                    </tr>
                                                     <tr class="login-header-inst">
                                                        <td class="sen">hospitals data security policy.
                                                        </td>
                                                    </tr>
                                                    <tr class="login-header-inst">
                                                        <td class="sen">
                                                            Please change your password to continue.
                                                        </td>
                                                    </tr>
                                                </table>
                                            </div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="button-spec-td">
                                            <div class="changePassword-spec">
                                                <button class="btn-spec changePassword-button" type="button"
                                                    id="changePassword" name="changePassword"
                                                    value="changePassword" onClick="changePassword()">
                                                    Change Password
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </div>
            </div>
        </c:when>

        <c:when test="${notifyPasswordChange}">
            <%
                String contextPath = request.getContextPath();
                        String redirectURL = contextPath
                                + "/pages/AdminModule/NotifyChangePassword.do?method=changePassword";
                        response.sendRedirect(redirectURL);
            %>
        </c:when>

    </c:choose>

    <div id="changePasswordDiv" style="display: none">
        <form name="changePasswordForm" method="post">
            <div class="login-card">
                <input type="hidden" name="method" value="" /> 
                <input type="hidden" name="user_id" id="user_id"
                        value="<%=Encoder.cleanHtmlAttribute((String)session.getAttribute("userid"))%>" />
                <div class="insta-header">
                    <img class="insta-header-image" src="./images/login/insta.png">
                        <span class="insta-header-content">
                            Hospital Management System
                        </span>
                </div>
                <div class="login-details-card">
                    <table class="login-card-table">
                        <tr>
                            <td class="login-card-image">
                                <div class="login-card-image-logo">
                                    <img src="./images/hospitalLogo/Logo.png" class="img-responsive">
                                </div>
                            </td>
                            <td class="login-card-details">
                                <table class="table-spec">
                                    <tr>
                                        <td class="login-header">PASSWORD HAS EXPIRED</td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <div class="login-header-inst">
                                                Please change your password to continue.
                                            </div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <div class="form-input">
                                                <label for="oldpwd" id="oldpwd-Label"> 
                                                    Old Password <sup class="required">*</sup>
                                                </label> 
                                                <input type="password" name="oldpwd" class="forminput" id="oldpwd"> 
                                                <span class="spin"></span>
                                            </div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <div class="form-input">
                                                <label for="pwd"> New Password<sup class="required">*</sup>
                                                </label> <input name="pwd" type="password" class="forminput" id="pwd">
                                                <span class="spin"></span>
                                            </div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <div class="form-input">
                                                <label for="cpwd"> Confirm Password<sup
                                                    class="required">*</sup>
                                                </label> <input name="cpwd" type="password" class="forminput"
                                                    id="cpwd"> <span class="spin"></span>
                                            </div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <c:choose>
                                            <c:when test="${msg eq 'Password successfully changed' }">
                                                <td class="login-success">${ifn:cleanHtml(msg)}</td>
                                            </c:when>
    
                                            <c:otherwise>
                                                <td class="error">${ifn:cleanHtml(msg)}</td>
                                            </c:otherwise>
    
                                        </c:choose>
                                    </tr>
                                    <tr>
                                        <td class="button-spec-td">
                                            <div class="save-new-pass-remind-button-spec">
                                                <button class="btn-spec save-new-pass-button" type="button"
                                                    name="Submit" value="Submit" onClick="submitFun()"
                                                    id="submit-expire">Save New Password</button>
                                            </div>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </div>
            </div>
        </form>
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

