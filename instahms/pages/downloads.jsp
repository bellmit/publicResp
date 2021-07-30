<%@ page contentType="text/html;charset=windows-1252"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
	<head>
		<title>Insta HMS - Downloads</title>
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
				<h1>Insta HMS - Downloads</h1>
				<ul>
					<li>
						<div>
							Insta Nexus ${nexus_version}
							<a href="${nexus_download_url}" style="float: right;">Download</a>
						</div>
						<div>
							<strong>Supported Operating System:</strong>
						</div>
						<div>
							Windows 8 or later
						</div>
						<div>
							<strong>Supported Architecture:</strong>
						</div>
						<div>
							x86, x64, ia64
						</div>
						<div>
							<strong>Hardware Requirements:</strong>
						</div>
						<div>
							Recommended Minimum: Pentium 1 GHz or higher with 512 MB RAM or more<br/>
							Disk space: 100MB
						</div>
					</li>
					<li>
						<div>
							Insta Nexus for Linux x64 ${nexus_linux_version}
							<a href="${nexus_linux64_download_url}" style="float: right;">Download</a>
						</div>
						<div>
							<strong>Supported Operating System:</strong>
						</div>
						<div>
							Ubuntu 12 or higher
						</div>
						<div>
							<strong>Supported Architecture:</strong>
						</div>
						<div>
							x64, ia64
						</div>
						<div>
							<strong>Hardware Requirements:</strong>
						</div>
						<div>
							Recommended Minimum: Pentium 1 GHz or higher with 512 MB RAM or more<br/>
							Disk space: 200MB
						</div>
					</li>
					<li>
						<div>
							Insta Nexus for Linux x86 ${nexus_linux_version}
							<a href="${nexus_linux32_download_url}" style="float: right;">Download</a>
						</div>
						<div>
							<strong>Supported Operating System:</strong>
						</div>
						<div>
							Ubuntu 12 or higher
						</div>
						<div>
							<strong>Supported Architecture:</strong>
						</div>
						<div>
							x86
						</div>
						<div>
							<strong>Hardware Requirements:</strong>
						</div>
						<div>
							Recommended Minimum: Pentium 1 GHz or higher with 512 MB RAM or more<br/>
							Disk space: 200MB
						</div>
					</li>
					<li>
						<div>
							WACOM STU Drivers
							<a href="${nexus_base}/wacom-stu-driver.exe" style="float: right;">Download</a>
						</div>
						<div>
							<strong>Supported Operating System:</strong>
						</div>
						<div>
							Windows 7 or later
						</div>
						<div>
							<strong>Supported Architecture:</strong>
						</div>
						<div>
							x86, x64, ia64
						</div>
						<div>
							<strong>Hardware Requirements:</strong>
						</div>
						<div>
							Recommended Minimum: Pentium 1 GHz or higher with 512 MB RAM or more<br/>
							Disk space: 100MB
						</div>
					</li>
					<li>
						<div>
							Digital Persona DPU-4500 Driver x86
							<a href="${nexus_base}/dpu-4500-driver-x86.exe" style="float: right;">Download</a>
						</div>
						<div>
							<strong>Supported Operating System:</strong>
						</div>
						<div>
							Windows 7 or later
						</div>
						<div>
							<strong>Supported Architecture:</strong>
						</div>
						<div>
							x86
						</div>
						<div>
							<strong>Hardware Requirements:</strong>
						</div>
						<div>
							Recommended Minimum: Pentium 1 GHz or higher with 512 MB RAM or more<br/>
							Disk space: 100MB
						</div>
					</li>
					<li>
						<div>
							Digital Persona DPU-4500 Driver x64
							<a href="${nexus_base}/dpu-4500-driver-x64.exe" style="float: right;">Download</a>
						</div>
						<div>
							<strong>Supported Operating System:</strong>
						</div>
						<div>
							Windows 7 or later
						</div>
						<div>
							<strong>Supported Architecture:</strong>
						</div>
						<div>
							x64, ia64
						</div>
						<div>
							<strong>Hardware Requirements:</strong>
						</div>
						<div>
							Recommended Minimum: Pentium 1 GHz or higher with 512 MB RAM or more<br/>
							Disk space: 100MB
						</div>
					</li>
					<li>
						<div>
							Microsoft .NET Runtime 4.5
							<a href="${nexus_base}/dotnet-runtime-4.5.exe" style="float: right;">Download</a>
						</div>
						<div>
							<strong>Supported Operating System:</strong>
						</div>
						<div>
							Windows 7 or later
						</div>
						<div>
							<strong>Supported Architecture:</strong>
						</div>
						<div>
							x86, x64, ia64
						</div>
						<div>
							<strong>Hardware Requirements:</strong>
						</div>
						<div>
							Recommended Minimum: Pentium 1 GHz or higher with 512 MB RAM or more<br/>
							Disk space : x86 - 850MB, x64 - 2GB
						</div>
					</li>
					<li>
						<div>
							Emirates Civil ID Toolkit V2.8.5
							<a href="${nexus_base}/EIDA_SDKSetup_V2.8.5.exe" style="float: right;">Download</a>
						</div>
						<div>
							<strong>Supported Operating System:</strong>
						</div>
						<div>
							Windows 7 or later
						</div>
						<div>
							<strong>Supported Architecture:</strong>
						</div>
						<div>
							x86, x64, ia64
						</div>
						<div>
							<strong>Hardware Requirements:</strong>
						</div>
						<div>
							Recommended Minimum: Pentium 1 GHz or higher with 512 MB RAM or more<br/>
							Disk space: 100MB
						</div>
					</li>
				</ul>
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
