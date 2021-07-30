<%@ page contentType="text/html;charset=windows-1252"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.common.Encoder" %>
<html>
<head>
<title>Change Password - Insta HMS</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">

<insta:link type="script" file="jquery-2.2.4.min.js" />
<insta:link type="script" file="usermanager/checkPasswordStrength.js" />
<insta:link type="script" file="usermanager/showPassword.js" />
<insta:link type="script" file="loginAnimation.js" />

<insta:link type="css" file="buttonStyle.css" />
<insta:link type="css" file="login.css" />
<insta:link type="script" file="login.js" />
<insta:link type="css" file="../styles/font-awesome.min.css"/>

<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="hospital" value='<%=((String) session.getAttribute("sesHospitalId")) %>' />
<script>
    var passwordRules = <%= request.getAttribute("passwordRules") %>;
</script>
</head>

<body onload="initialize()">
    <form action="ChangePassword.do?method=updatePassword" onsubmit="return submitFun()" method="post">
        <input type="hidden" name="uid" id="uid"
            value="<%=Encoder.cleanHtmlAttribute((String)session.getAttribute("userid"))%>" />
        <div class="login-card">
            <div class="insta-header">
                <img class="insta-header-image" src="${cpath}/images/login/insta.png">
                <span class="insta-header-content">
                    Hospital Management System
                </span>
            </div>
            <div class="login-details-card login-card-margin">
                <table class="login-card-table" >
                    <tr>
                        <td class="login-card-image">
                            <div class="login-card-image-logo">
                                <img src="../../images/hospitalLogo/${ifn:cleanHtml(hospital)}Logo.png"
                                     onerror="this.src='../../images/hospitalLogo/Logo.png';">
                            </div>
                        </td>
                        <td class="login-card-details" style="width: 297px">
                            <table class="table-spec" id="button-redirect">
                                <tr>
                                    <td class="login-header">CHANGE PASSWORD</td>
                                </tr>
                                <tr>
                                    <td>
                                        <div class="login-header-inst">
                                            Your Password should not be same as your last 4.
                                        </div>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <div class="form-input" style="display:flex;justify-content:space-between;">
                                            <label for="oldpwd" id="oldpwd-Label">
                                                Old Password<sup class="required">*</sup>
                                            </label>
                                            <input type="password" name="oldpwd" class="forminput" id="oldpwd">
                                            <span class="spin"></span>
                                            <i class="fa fa-eye" onclick="toggleShowPassword('oldpwd','togglePassword1');" id="togglePassword1" style="padding-top:42px;padding-right:5px;color:#787887;cursor:pointer"></i>
                                        </div>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <div class="form-input" style="display:flex;justify-content:space-between;">
                                            <label for="pwd">
                                                New Password<sup class="required">*</sup>
                                            </label>
                                            <input name="pwd" type="password" class="forminput" id="pwd" onkeyup="checkPasswordStrengthOnKeyup('strength1','pwd')">
                                            <span class="spin"></span>
                                            <i class="fa fa-eye" onclick="toggleShowPassword('pwd','togglePassword2');" id="togglePassword2" style="padding-top:42px;padding-right:5px;color:#787887;cursor:pointer"></i>
                                        </div>
                                        <span id="strength1" style="font-family: 'CamphorWeb-Regular';display:block;padding-top:3px;"></span>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <div class="form-input" style="display:flex;justify-content:space-between;">
                                            <label for="cpwd">
                                                Confirm Password<sup class="required">*</sup>
                                            </label>
                                            <input name="cpwd" type="password" class="forminput" id="cpwd" onkeyup="checkPasswordStrengthOnKeyup('strength2','cpwd')">
                                            <span class="spin"></span>
                                            <i class="fa fa-eye" onclick="toggleShowPassword('cpwd','togglePassword3');" id="togglePassword3" style="padding-top:42px;padding-right:5px;color:#787887;cursor:pointer"></i>
                                        </div>
                                        <span id="strength2" style="font-family: 'CamphorWeb-Regular';display:block;padding-top:3px;"></span>
                                    </td>
                                </tr>

                                <c:choose>
                                    <c:when test="${msg eq 'Password successfully changed' }">
                                        <tr>
                                            <td class="login-success">
                                                <c:if test="${not empty requestScope.msg}">
                                                    ${msg}
                                                </c:if>
                                            </td>
                                        </tr>
                                        <script>
                                            $(document).ready(function() {
                                                document.getElementById("button-redirect").innerHTML =
                                                "<tr class=\"success-chanePassword-spec\">"+
                                                    "<td>"+
                                                        "<img src=\"../../images/Success_Tick.png\">"+
                                                        "<div class=\"login-success changePassword-sucess-inst\">"+
                                                            "Password Sucessfully Changed"+
                                                        "</div>"+
                                                    "</td>"+
                                                "</tr>"+
                                                "<tr>"+
                                                    "<td class=\"button-spec-td\" >"+
                                                        "<a href=\"${cpath}/home.do?userId=${ifn:cleanURL(userId)}&hospitalId=${ifn:cleanURL(sesHospitalId)}\">"+
                                                            "<button class=\"btn-spec save-new-pass-button\" type=\"button\">"+
                                                                "Go To Home Page"+
                                                            "</button>"+
                                                        "</a>"+
                                                    "</td>"+
                                                "</tr>";
                                            });
                                        </script>
                                    </c:when>
                                    <c:otherwise>
                                        <tr>
                                            <td class="error">
                                                <c:if test="${not empty requestScope.msg}">
                                                    ${msg}
                                                </c:if>
                                            </td>
                                        </tr>
                                    </c:otherwise>
                                </c:choose>

                                <tr>
                                    <td class="button-spec-td">
                                        <div class="save-new-pass-button-spec">
                                            <button class="btn-spec save-new-pass-button" type="submit"
                                                name="Submit" value="Submit" id="submit">Save New Password</button>
                                        </div>
                                        <div class="cancel-button-spec">
                                            <a href="${cpath}/home.do?userId=${ifn:cleanURL(userId)}&hospitalId=${ifn:cleanURL(sesHospitalId)}">
                                                <button type="button" class="btn-spec-secondary cancel-button">
                                                    Cancel
                                                </button>
                                            </a>
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
</body>
</html>
