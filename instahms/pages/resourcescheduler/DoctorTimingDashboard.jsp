<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta"%>

<html>
<head>
<title><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.title"/></title>
<insta:link type="css" file="widgets.css"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="script" file="hmsvalidation.js" />
<script>
	function validateDelete(){
		var deleteDoctorEls = document.getElementsByName("deleteDoctor");
		for (var i=0; i<deleteDoctorEls.length; i++) {
			var el = deleteDoctorEls[i];
			if (el.checked) {
				return true;
			}
		}
		showMessage("js.scheduler.doctorscheduler.selectdoctor.todelete");
		return false;
	}
</script>

<insta:js-bundle prefix="scheduler.doctornonavailability"/>
<insta:js-bundle prefix="widgets.commonvalidations"/>
<insta:js-bundle prefix="scheduler.resourceavailability"/>
<insta:js-bundle prefix="scheduler.doctorscheduler"/>
<insta:js-bundle prefix="scheduler.todaysappointment"/>
<insta:js-bundle prefix="scheduler.schedulerdashboard"/>
<insta:js-bundle prefix="scheduler.portalappointment"/>
<insta:js-bundle prefix="clinicaldata.commonvalidations"/>
</head>

<c:set var="cpath" value="${pageContext.request.contextPath}" />
<body>
<form onsubmit="return validateDelete();">
<input type="hidden" name="method" value="deleteDoctorAvailableTimings" />

<div class="label" align="center"><b>${message}</b></div>
<div align="center" class="pageHeader"> <insta:ltext key="patient.resourcescheduler.doctortimingdashboard.pageHeader"/></div>
<div align="center" class="resultMessage">${msg}</div>

<table class="dashboard" width="50%" align="center">
	<tr>
		<th><input type="checkbox" name="deleteDoctor" onclick="return checkOrUncheckAll('deleteDoctor', this)"/></th>
		<th><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.doctor"/></th>
		<th><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.department"/></th>
		<th><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.edit"/></th>
	</tr>
	<c:forEach items="${Doctorlist}" var="doctor">
		<tr>
				<c:choose>
					<c:when test="${doctor.map.doctor_id == '*'}">
						<td width="10%"></td>
						<td width="30%"><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.defaultall"/></td>
						<td width="30%"><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.defaultalldept"/></td>
						<td width="10%">
						<c:url var="Eurl" value="doctortiming.do">
							<c:param name="method" value="getEditDoctorTimingScreen"/>
							<c:param name="doctorId" value="${doctor.map.doctor_id}"/>
							<c:param name="doctorName" value="All Doctors"/>
							<c:param name="deptName" value="All Departments"/>
						</c:url>
						<a href="${Eurl}"><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.edit"/></a>
						</td>
					</c:when>
					<c:otherwise>
						<td width="10%"><input type="checkbox" name="deleteDoctor" value="${doctor.map.doctor_id}"/></td>
						<td width="30%"><c:out value="${doctor.map.doctor_name}"/></td>
						<td width="30%"><c:out value="${doctor.map.dept_name}"/></td>
						<td width="10%">
						<c:url var="Eurl" value="doctortiming.do">
							<c:param name="method" value="getEditDoctorTimingScreen"/>
							<c:param name="doctorId" value="${doctor.map.doctor_id}"/>
							<c:param name="doctorName" value="${doctor.map.doctor_name}"/>
							<c:param name="deptName" value="${doctor.map.dept_name}"/>
						</c:url>
						<a href="${Eurl}"><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.edit"/></a>
						</td>
					</c:otherwise>
				</c:choose>

		</tr>
	</c:forEach>
</table>
<table width="50%" align="center">
	<tr>
		<td align="left">

		<insta:accessbutton buttonkey="patient.resourcescheduler.doctortimingdashboard.delete" name="save" type="button" />
		</td>
		<td align="left"><a href="doctortiming.do?method=getEditDoctorTimingScreen&doctorId=&doctorName=&deptName="><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.addscheduler"/></a></td>
	</tr>
</table>
</form>
</body>
</html>