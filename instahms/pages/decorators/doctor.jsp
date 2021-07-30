<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator"%>
<%@page contentType="text/html" isELIgnored="false"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<jsp:useBean id="date" class="java.util.Date" />

<%--
  Main jsp page which shows a group of screen menus.
  Input parameter: group: ID of the screen group to show on the left frame.
--%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="screenGroupId" value="${param.group}" />
<c:set var="screenGroup" value="${screenConfig.screenGroupMap[screenGroupId]}" />
<%
	String userid = (String) session.getAttribute("userid");
	String hospital = (String) session.getAttribute("sesHospitalId");
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html"/>
<title>Insta HMS</title>
<jsp:include page="/pages/yuiScripts.jsp"/>
<insta:link type="script" file="jsvalidate.js"/>
<insta:link type="css" file="menu.css"/>

<decorator:head />
<style type="text/css">
.toplinks {
padding:5px;
}
.toplinks a {
color:#fff;
}
.headerLinks a, .headerLinks a:visited {
	color: darkgreen;
}
</style>

</head>
<body
	<decorator:getProperty property="body.onload" writeEntireProperty="true" />
	<decorator:getProperty property="body.class" writeEntireProperty="true" />
	<decorator:getProperty property="body.style" writeEntireProperty="true" />>

<script type="text/javascript">
	YAHOO.util.Event.onContentReady("topmenu", function () {
		var oMenu = new YAHOO.widget.MenuBar("topmenu", {
				autosubmenudisplay: false,
				showdelay:500, hidedelay:1000, lazyload: true});
		oMenu.render();
		oMenu.show();
	});
</script>
<table border="0" cellpadding="0" cellspacing="0" style="border-collapse: collapse;" width="100%" >
  <tr height="10px">
		<td width="120px" height="40px" style="background: url(${cpath}/showScreenLogo.do) no-repeat;"
			rowspan="2">
    </td>
    <td class="headerbg toplinks" align="right" valign="center" style="padding-right: 8px">
			User: <%=userid%> |
			Hospital: <%=hospital%> |
			<span class="headerLinks">
				<a href="${cpath}/help/InstaHMS_patient_portal_help.html" target="_blank">Help</a> |
			<a href="${cpath}/doctor/ChangePassword.do?method=changePassword">Change Password</a> |
		  	<insta:link type="href" path="doctor/logout.do" content="Logout" />
		</span>
		</td>
	</tr>
</table>

<table class="yui-skin-sam" border="0" cellpadding="0" cellspacing="0" width="100%">
  <tr height="20px;">
  	<td valign="bottom">
		<div id="topmenu" class="yuimenubar yuimenubarnav">
			<div class="bd">
				<ul class="first-of-type">

					<li class="yuimenubaritem"><a class="yuimenubaritemlabel" href="${cpath}/doctor/DoctorTreatmentDashboard.do?method=list">Dashboard</a></li>

					<li class="yuimenubaritem"><a class="yuimenubaritemlabel" href="${cpath}/doctor/PatientSearch.do?method=getPatientsearchPage">Medical Record History</a></li>

				</ul>
			</div>
		</div>
    	</td>
  </tr>
</table>

<div id="content">
<decorator:body />
</div>

<div style="text-align:center; margin-top: 30px">
	<span class="footer">
		<a href="http://www.instahealthsolutions.com/" class="footer-supportlink"
           title="Visit the web site (opens in a new window)" target="_blank">
           Insta by Practo.
        </a> Version <fmt:message key="insta.software.version" />.
         Copyright &copy; <fmt:formatDate value="${date}" pattern="yyyy" /> Practo Technologies Pvt. Ltd. All Rights Reserved.
 		<br>
		<a href="${cpath}/help/Insta_Acknowledgements.pdf" target="_blank">Acknowledgments</a> |
		<a href="mailto:insta-support@practo.com"
			title="Email Insta customer support team to report a problem or request for assistance"
			target="_blank">Email Insta Customer Support</a>
	</span>
</div>

</body>
</html>
