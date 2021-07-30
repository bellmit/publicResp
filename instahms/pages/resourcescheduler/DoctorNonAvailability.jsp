<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title><insta:ltext key="patient.resourcescheduler.doctornonavailability.title"/></title>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="ajax.js"/>
	<insta:link type="script" file="/resourcescheduler/doctornonavailability.js"/>
<style type="text/css">

table.dashboard input[type="text"] {
	 max-width: 5em;
}

</style>

<script type="text/javascript">

	function enableWeekDayTime(){
		 if('${param.action}' != null && '${param.action}' == 'edit'){
			checkNoofdays();
			//document.forms[0].method.value = "updateDoctorNonAvailability";
			document.forms[0].submit.value = "Update";
			if('${param.weekday}' == '1'){
				document.forms[0].sun1.value = '${param.firstFrom}';
				document.forms[0].sun2.value = '${param.firstTo}';
				document.forms[0].sun3.value = '${param.secondFrom}';
				document.forms[0].sun4.value = '${param.secondTo}';
			}else if('${param.weekday}' == '2'){
				document.forms[0].mon1.value = '${param.firstFrom}';
				document.forms[0].mon2.value = '${param.firstTo}';
				document.forms[0].mon3.value = '${param.secondFrom}';
				document.forms[0].mon4.value = '${param.secondTo}';
			}else if('${param.weekday}' == '3'){
				document.forms[0].tue1.value = '${param.firstFrom}';
				document.forms[0].tue2.value = '${param.firstTo}';
				document.forms[0].tue3.value = '${param.secondFrom}';
				document.forms[0].tue4.value = '${param.secondTo}';
			}else if('${param.weekday}' == '4'){
				document.forms[0].wed1.value = '${param.firstFrom}';
				document.forms[0].wed2.value = '${param.firstTo}';
				document.forms[0].wed3.value = '${param.secondFrom}';
				document.forms[0].wed4.value = '${param.secondTo}';
			}else if('${param.weekday}' == '5'){
				document.forms[0].thu1.value = '${param.firstFrom}';
				document.forms[0].thu2.value = '${param.firstTo}';
				document.forms[0].thu3.value = '${param.secondFrom}';
				document.forms[0].thu4.value = '${param.secondTo}';
			}else if('${param.weekday}' == '6'){
				document.forms[0].fri1.value = '${param.firstFrom}';
				document.forms[0].fri2.value = '${param.firstTo}';
				document.forms[0].fri3.value = '${param.secondFrom}';
				document.forms[0].fri4.value = '${param.secondTo}';
			}else if('${param.weekday}' == '7'){
				document.forms[0].sat1.value = '${param.firstFrom}';
				document.forms[0].sat2.value = '${param.firstTo}';
				document.forms[0].sat3.value = '${param.secondFrom}';
				document.forms[0].sat4.value = '${param.secondTo}';
			}
		}
	}

	function doClose() {
		window.location.href = "${cpath}/pages/resourcescheduler/doctortiming.do?method=getEditDoctorTimingScreen&doctorId=${param.doctorId}&doctorName=${param.doctorName}&deptName=${param.deptName}";
	}

</script>

<insta:js-bundle prefix="scheduler.doctornonavailability"/>
<insta:js-bundle prefix="scheduler.resourceavailability"/>
<insta:js-bundle prefix="scheduler.doctorscheduler"/>
<insta:js-bundle prefix="scheduler.todaysappointment"/>
<insta:js-bundle prefix="scheduler.schedulerdashboard"/>
<insta:js-bundle prefix="scheduler.portalappointment"/>
</head>
<body onload="enableWeekDayTime();">
<form action="doctortiming.do">
<input type="hidden" name="method" value="saveDoctorNonAvailability"/>
<input type="hidden" name="finalfromdate" value="${param.naDate}"/>
<input type="hidden" name="finaltodate" value="${param.naDate}"/>
	<c:choose>
		<c:when test="${param.action == 'add'}">
			<div class="pageHeader"><insta:ltext key="patient.resourcescheduler.doctornonavailability.pageHeader1"/></div>
		</c:when>
		<c:otherwise>
			<div class="pageHeader"><insta:ltext key="patient.resourcescheduler.doctornonavailability.pageHeader2"/></div>
		</c:otherwise>
	</c:choose>

	<span align="center" class="resultMessage">${msg}</span>


<input type="hidden" name="doctor" value="${param.doctorId}"/>
<input type="hidden" name="doctorName" value="${param.doctorName}"/>
<input type="hidden" name="deptName" value="${param.deptName}"/>

<table align="center" class="formtable">
	<tr>
		<td>
			<table>
				<tr>
					<td><insta:ltext key="patient.resourcescheduler.doctornonavailability.department"/></td><td><b>${param.deptName}</b></td>
					<td><insta:ltext key="patient.resourcescheduler.doctornonavailability.doctor"/></td><td><b>${param.doctorName}</b></td>
				</tr>
			</table>
		</td>
	</tr>
	<c:choose>
	<c:when test="${param.action == 'add'}">
		<tr>
			<td>
				<table>
				<tr>
					<td><insta:ltext key="patient.resourcescheduler.doctornonavailability.nonperiod"/></td><td class="forminfo"><insta:ltext key="patient.resourcescheduler.doctornonavailability.from"/><insta:datewidget name="fromdate" calButton="true" valid="future" /></td>
					<td class="forminfo"><insta:ltext key="patient.resourcescheduler.doctornonavailability.to"/><insta:datewidget name="todate" id="todate" calButton="true" valid="future" /></td>
					<td><input type="button" name="checkdates" value="Apply" onclick="return checkValidations();"/></td>

				</tr>
			</table>
		</td>
	</tr>
	</c:when>
	<c:otherwise>
		<input type="hidden" name="nonAvailDate" value="${param.naDate}"/>
		<input type="hidden" name="fromdate"  id="fromdate" value="${param.naDate}"/>
		<input type="hidden" name="todate"  id="todate" value="${param.naDate}"/>
		<tr>
			<td>
				<table>
					<tr>
						<td><insta:ltext key="patient.resourcescheduler.doctornonavailability.nondate"/></td><td class="forminfo"> ${param.naDate}</td>
					</tr>
				</table>
			</td>
		</tr>
	</c:otherwise>
</c:choose>

<tr>
	<td><insta:ltext key="patient.resourcescheduler.doctornonavailability.remarks"/><input type="text" name="remarks" style="width: 30em;" maxlength="50"/></td>
</tr>

<tr>
<td>
	<insta:ltext key="patient.resourcescheduler.doctornonavailability.nontime"/>
	<table class="dashboard">
		<tr>
			<th rowspan="2"><insta:ltext key="patient.resourcescheduler.doctornonavailability.day"/></th><th colspan="2"><insta:ltext key="patient.resourcescheduler.doctornonavailability.slot1"/></th><th colspan="2"><insta:ltext key="patient.resourcescheduler.doctornonavailability.slot2"/></th><th rowspan="2"><img height="20" width="20"	name="deleteIcon" src="../../images/delete.jpg" /></th>
		</tr>
		<tr>
			<th><insta:ltext key="patient.resourcescheduler.doctornonavailability.fromhm"/></th><th><insta:ltext key="patient.resourcescheduler.doctornonavailability.tohm"/></th><th><insta:ltext key="patient.resourcescheduler.doctornonavailability.fromhm"/></th><th><insta:ltext key="patient.resourcescheduler.doctornonavailability.tohm"/></th>
		</tr>
		<tr>
			<td><input type="checkbox" name="weekday" value="2" onclick="enableFields(this.value)" disabled/> <insta:ltext key="patient.resourcescheduler.doctornonavailability.monday"/></td>
			<td><input type="text" name="mon1" id="mon1" disabled maxlength="5" onchange="getCompleteTime(this)"/></td>
			<td><input type="text" name="mon2" id="mon2" disabled maxlength="5" onchange="getCompleteTime(this)"/></td>
			<td><input type="text" name="mon3" id="mon3" disabled maxlength="5" onchange="getCompleteTime(this)"/></td>
			<td><input type="text" name="mon4" id="mon4" disabled maxlength="5" onchange="getCompleteTime(this)"/></td>
			<td>
				<input type="checkbox" name="deleteTiming" id="deleteTiming2" value="2" disabled onclick="cancel(this,'deleteDay2')"/>
				<input type="hidden" name="deleteDay" id="deleteDay2" disabled value="false"/>
			</td>
		</tr>
		<tr>
			<td><input type="checkbox" name="weekday" value="3" onclick="enableFields(this.value)" disabled/> <insta:ltext key="patient.resourcescheduler.doctornonavailability.tuesday"/></td>
			<td><input type="text" name="tue1" id="tue1" disabled maxlength="5" onchange="getCompleteTime(this)"/></td>
			<td><input type="text" name="tue2" id="tue2" disabled maxlength="5" onchange="getCompleteTime(this)"/></td>
			<td><input type="text" name="tue3" id="tue3" disabled maxlength="5" onchange="getCompleteTime(this)"/></td>
			<td><input type="text" name="tue4" id="tue4" disabled maxlength="5" onchange="getCompleteTime(this)"/></td>
			<td>
				<input type="checkbox" name="deleteTiming" id="deleteTiming3" value="3" disabled onclick="cancel(this,'deleteDay3')"/>
				<input type="hidden" name="deleteDay" id="deleteDay3" disabled value="false"/>
			</td>
		</tr>
		<tr>
			<td><input type="checkbox" name="weekday" value="4" onclick="enableFields(this.value)" disabled/> <insta:ltext key="patient.resourcescheduler.doctornonavailability.wednesday"/></td>
			<td><input type="text" name="wed1" id="wed1" disabled maxlength="5" onchange="getCompleteTime(this)"/></td>
			<td><input type="text" name="wed2" id="wed2" disabled maxlength="5" onchange="getCompleteTime(this)"/></td>
			<td><input type="text" name="wed3" id="wed3" disabled maxlength="5" onchange="getCompleteTime(this)"/></td>
			<td><input type="text" name="wed4" id="wed4" disabled maxlength="5" onchange="getCompleteTime(this)"/></td>
			<td>
				<input type="checkbox" name="deleteTiming" id="deleteTiming4" value="4" disabled onclick="cancel(this,'deleteDay4')"/>
				<input type="hidden" name="deleteDay" id="deleteDay4" disabled value="false"/>
			</td>
		</tr>
		<tr>
			<td><input type="checkbox" name="weekday" value="5" onclick="enableFields(this.value)" disabled/> <insta:ltext key="patient.resourcescheduler.doctornonavailability.thursday"/></td>
			<td><input type="text" name="thu1" id="thu1" disabled maxlength="5" onchange="getCompleteTime(this)"/></td>
			<td><input type="text" name="thu2" id="thu2" disabled maxlength="5" onchange="getCompleteTime(this)"/></td>
			<td><input type="text" name="thu3" id="thu3" disabled maxlength="5" onchange="getCompleteTime(this)"/></td>
			<td><input type="text" name="thu4" id="thu4" disabled maxlength="5" onchange="getCompleteTime(this)"/></td>
			<td>
				<input type="checkbox" name="deleteTiming" id="deleteTiming5" value="5" disabled onclick="cancel(this,'deleteDay5')"/>
				<input type="hidden" name="deleteDay" id="deleteDay5" disabled value="false"/>
			</td>
		</tr>
		<tr>
			<td><input type="checkbox" name="weekday" value="6" onclick="enableFields(this.value)" disabled/> <insta:ltext key="patient.resourcescheduler.doctornonavailability.friday"/></td>
			<td><input type="text" name="fri1" id="fri1" disabled maxlength="5" onchange="getCompleteTime(this)"/></td>
			<td><input type="text" name="fri2" id="fri2" disabled maxlength="5" onchange="getCompleteTime(this)"/></td>
			<td><input type="text" name="fri3" id="fri3" disabled maxlength="5" onchange="getCompleteTime(this)"/></td>
			<td><input type="text" name="fri4" id="fri4" disabled maxlength="5" onchange="getCompleteTime(this)"/></td>
			<td>
				<input type="checkbox" name="deleteTiming" id="deleteTiming6" value="6" disabled onclick="cancel(this,'deleteDay6')"/>
				<input type="hidden" name="deleteDay" id="deleteDay6" disabled value="false"/>
			</td>
		</tr>
		<tr>
			<td><input type="checkbox" name="weekday" value="7" onclick="enableFields(this.value)" disabled/> <insta:ltext key="patient.resourcescheduler.doctornonavailability.saturday"/></td>
			<td><input type="text" name="sat1" id="sat1" disabled maxlength="5" onchange="getCompleteTime(this)"/></td>
			<td><input type="text" name="sat2" id="sat2" disabled maxlength="5" onchange="getCompleteTime(this)"/></td>
			<td><input type="text" name="sat3" id="sat3" disabled maxlength="5" onchange="getCompleteTime(this)"/></td>
			<td><input type="text" name="sat4" id="sat4" disabled maxlength="5" onchange="getCompleteTime(this)"/></td>
			<td>
				<input type="checkbox" name="deleteTiming" id="deleteTiming7" value="7" disabled onclick="cancel(this,'deleteDay7')"/>
				<input type="hidden" name="deleteDay" id="deleteDay7" disabled value="false"/>
			</td>
		</tr>
		<tr>
			<td><input type="checkbox" name="weekday" value="1" onclick="enableFields(this.value)" disabled/> <insta:ltext key="patient.resourcescheduler.doctornonavailability.sunday"/></td>
			<td><input type="text" name="sun1" id="sun1" disabled maxlength="5" onchange="getCompleteTime(this)"/></td>
			<td><input type="text" name="sun2" id="sun2" disabled maxlength="5" onchange="getCompleteTime(this)"/></td>
			<td><input type="text" name="sun3" id="sun3" disabled maxlength="5" onchange="getCompleteTime(this)"/></td>
			<td><input type="text" name="sun4" id="sun4" disabled maxlength="5" onchange="getCompleteTime(this)"/></td>
			<td>
				<input type="checkbox" name="deleteTiming" id="deleteTiming1" value="1" disabled onclick="cancel(this,'deleteDay1')"/>
				<input type="hidden" name="deleteDay" id="deleteDay1" disabled value="false"/>
			</td>
		</tr>
		<tr>
			<td colspan="6" align="center">
				<input type="submit" value="Save" name="submit" onclick="return validate()"/>
				<input type="button" value="Close" onclick="doClose();"/>
			</td>
		</tr>
	</table>
	</td>
</tr>
</table>
</form>
</body>
</html>
