<%@ page contentType="text/html;charset=windows-1252"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
	<head>
		<title>Insta HMS Help</title>
		<insta:link type="css" file="help.css" />
		<insta:link type="script" file="jquery-2.2.4.min.js" />
		<insta:link type="script" file="helpMail.js" />
		<c:set var="cpath" value="${pageContext.request.contextPath}" />
	</head>
	<script>
		var cpath = '${cpath}';
		var hospitalid = '${ifn:cleanJavaScript(sesHospitalId)}';
	</script>
	<body>
		<div class="header">
			<img class="logo" alt="Practo Help" src="${cpath}/images/Practohelp.png">
		</div>
		<div class="content">
			<div class="content-manual">
				<h1>Insta HMS - User Manual</h1>
				<iframe id="iframepdf" src="${cpath}/help/InstaHMS_help.pdf"
					width="100%" class="homepage-pdf-iframe">
				</iframe>
			</div>
			<div class="content-desc">
				<h4>
					Contact Us
				</h4>
				<div>Click the Button below to contact us: </div>
				<div style="padding: 10px 0;"><img src="${cpath}/images/Contact-Us-Button.png" id="send-mail"></div>
				<div style="padding-top: 10px;">Or call us at:</div>
				<div style="padding-top: 10px;"><strong>Phone (India):</strong></div>
				<div>+91 80-49202432, +91 80-67095209</div>
				<div>Available from 8:00 AM IST - 8:00 PM IST, All Days</div>
				<div style="padding-top: 10px;"><strong>Email:</strong></div>
				<div>insta-support@practo.com</div>
			</div>
		</div>
		<div class="footer">
			<a href="http://www.instahealthsolutions.com/" title="Visit the web site (opens in a new window)" target="_blank">Insta by Practo</a>.
	         Copyright &copy; <fmt:formatDate value="${date}" pattern="yyyy" /> Practo Technologies Pvt. Ltd. All Rights Reserved.
		</div>
	</body>
</html>
