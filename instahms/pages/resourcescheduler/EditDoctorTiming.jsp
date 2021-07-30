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
<meta name="i18nSupport" content="true"/>
<title><insta:ltext key="patient.resourcescheduler.editdoctortiming.title"/></title>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="ajax.js"/>
	<insta:link type="script" file="/resourcescheduler/editavailabilitytiming.js"/>
<style type="text/css">

table.dashboard input[type="text"] {
	 max-width: 5em;
}

.Autocomplete{
	width:22em; /* set width here or else widget will expand to fit its container */
    padding-bottom:2em;
}

</style>

<script>
	var weekNumber = '${weekNumber}';
	var doctorsJson = ${doctorsJSON};
	function doClose() {
		window.location.href = "${cpath}/pages/resourcescheduler/doctortiming.do?method=getDoctorTimingDashboard";
	}
</script>
<insta:js-bundle prefix="scheduler.doctornonavailability"/>
<insta:js-bundle prefix="clinicaldata.commonvalidations"/>
<insta:js-bundle prefix="scheduler.resourceavailability"/>
<insta:js-bundle prefix="scheduler.doctorscheduler"/>
<insta:js-bundle prefix="scheduler.todaysappointment"/>
<insta:js-bundle prefix="scheduler.schedulerdashboard"/>
<insta:js-bundle prefix="scheduler.portalappointment"/>
</head>
<body onload="fillEmpty();">
<form>
<input type="hidden" name="method" value="saveDoctorAvailableTimings"/>

<c:set var="docID"><c:choose><c:when test="${empty param.doctorId}">${doctorId}</c:when><c:otherwise>${param.doctorId}</c:otherwise></c:choose></c:set>
<c:set var="docName"><c:choose><c:when test="${empty param.doctorName}">${doctorName}</c:when><c:otherwise>${param.doctorName}</c:otherwise></c:choose></c:set>
<c:set var="depName"><c:choose><c:when test="${empty param.deptName}">${deptName}</c:when><c:otherwise>${param.deptName}</c:otherwise></c:choose></c:set>

<input type="hidden" name="doctor" id="doctor" value="${docID}"/>
<input type="hidden" name="doctorName" id="doctorName" value="${docName}">
<input type="hidden" name="deptName" id="deptName" value="${depName}">

	<div class="pageHeader"><insta:ltext key="patient.resourcescheduler.editdoctortiming.pageheader"/> </div>
	<div align="center" class="resultMessage">${msg}</div>

<table align="center">
<tr bgcolor="lightgrey"><td style="height: 1em"><insta:ltext key="patient.resourcescheduler.editdoctortiming.message"/> ${rescheduleMsg}</td></tr>
<tr>
	<td><insta:ltext key="patient.resourcescheduler.editdoctortiming.doctor"/>
		<table>
			<tr>
				<c:choose>
					<c:when test="${empty param.doctorName}">
						<c:set var="docdept" value='---Select---'/>
						<c:set var="readonly" value=""/>
					</c:when>
					<c:when test="${empty param.doctorName}">
						<c:set var="docdept" value='---Select---'/>
						<c:set var="readonly" value=""/>
					</c:when>
					<c:otherwise>
						<c:set var="docdept" value='${param.doctorName}-${param.deptName}'/>
						<c:set var="readonly" value="readonly"/>
					</c:otherwise>
				</c:choose>
				 <td class="yui-skin-sam" valign="top">
					<div id="doctorAutocomplete" class="Autocomplete">
		          	  <input type="text" id="newDoctor" name="newDoctor" value="${docdept}" ${readonly} onfocus="document.getElementById('newDoctor').value=''"/>
		                	<div id="doctorContainer"></div>
	           		 </div>
		    	</td>
		    	<script>
		    		doctorAutoComplete('${docdept}');
		   		 </script>
			</tr>
		</table>
	</td>
</tr>
<tr>
	<td>&nbsp;</td>
</tr>
<tr>
	<td align="left"><insta:ltext key="patient.resourcescheduler.editdoctortiming.availabilitytime"/>
	<table class="dashboard">
		<tr>
			<th rowspan="2"><insta:ltext key="patient.resourcescheduler.editdoctortiming.day"/></th><th colspan="2"><insta:ltext key="patient.resourcescheduler.editdoctortiming.timeslot1"/></th><th colspan="2"><insta:ltext key="patient.resourcescheduler.editdoctortiming.timeslot2"/></th>
		</tr>
		<tr>
			<th><insta:ltext key="patient.resourcescheduler.editdoctortiming.fromhm"/></th><th><insta:ltext key="patient.resourcescheduler.editdoctortiming.tohm"/></th><th><insta:ltext key="patient.resourcescheduler.editdoctortiming.fromhm"/></th><th><insta:ltext key="patient.resourcescheduler.editdoctortiming.tohm"/></th>
		</tr>
		<c:forEach items="${timingList}" var="time">
		<tr>
			<td><insta:ltext key="patient.resourcescheduler.editdoctortiming.monday"/></td>
			<td><input type="text" name="mon1" id="mon1" maxlength="5" onchange="getCompleteTime(this)" value='<fmt:formatDate value="${time.map.mon_slot1_from}" type="time" pattern="HH:mm"/>' onfocus="makeblank(this);" onblur="setTime(this)"/></td>
			<td><input type="text" name="mon2" id="mon2" maxlength="5" onchange="getCompleteTime(this)" value='<fmt:formatDate value="${time.map.mon_slot1_to}" type="time" pattern="HH:mm"/>' onfocus="makeblank(this);" onblur="setTime(this)"/></td>
			<td><input type="text" name="mon3" id="mon3" maxlength="5" onchange="getCompleteTime(this)" value='<fmt:formatDate value="${time.map.mon_slot2_from}" type="time" pattern="HH:mm"/>' onfocus="makeblank(this);" onblur="setTime(this)"/></td>
			<td><input type="text" name="mon4" id="mon4" maxlength="5" onchange="getCompleteTime(this)" value='<fmt:formatDate value="${time.map.mon_slot2_to}" type="time" pattern="HH:mm"/>' onfocus="makeblank(this);" onblur="setTime(this)"/></td>
		</tr>
		<tr>
			<td><insta:ltext key="patient.resourcescheduler.editdoctortiming.tuesday"/></td>
			<td><input type="text" name="tue1" id="tue1" maxlength="5" onchange="getCompleteTime(this)" value='<fmt:formatDate value="${time.map.tue_slot1_from}" type="time" pattern="HH:mm"/>' onfocus="makeblank(this);" onblur="setTime(this)"/></td>
			<td><input type="text" name="tue2" id="tue2" maxlength="5" onchange="getCompleteTime(this)" value='<fmt:formatDate value="${time.map.tue_slot1_to}" type="time" pattern="HH:mm"/>' onfocus="makeblank(this);" onblur="setTime(this)"/></td>
			<td><input type="text" name="tue3" id="tue3" maxlength="5" onchange="getCompleteTime(this)" value='<fmt:formatDate value="${time.map.tue_slot2_from}" type="time" pattern="HH:mm"/>' onfocus="makeblank(this);" onblur="setTime(this)"/></td>
			<td><input type="text" name="tue4" id="tue4" maxlength="5" onchange="getCompleteTime(this)" value='<fmt:formatDate value="${time.map.tue_slot2_to}" type="time" pattern="HH:mm"/>' onfocus="makeblank(this);" onblur="setTime(this)"/></td>
		</tr>
		<tr>
			<td><insta:ltext key="patient.resourcescheduler.editdoctortiming.wednesday"/></td>
			<td><input type="text" name="wed1" id="wed1" maxlength="5" onchange="getCompleteTime(this)" value='<fmt:formatDate value="${time.map.wed_slot1_from}" type="time" pattern="HH:mm"/>' onfocus="makeblank(this);" onblur="setTime(this)"/></td>
			<td><input type="text" name="wed2" id="wed2" maxlength="5" onchange="getCompleteTime(this)" value='<fmt:formatDate value="${time.map.wed_slot1_to}" type="time" pattern="HH:mm"/>' onfocus="makeblank(this);" onblur="setTime(this)"/></td>
			<td><input type="text" name="wed3" id="wed3" maxlength="5" onchange="getCompleteTime(this)" value='<fmt:formatDate value="${time.map.wed_slot2_from}" type="time" pattern="HH:mm"/>' onfocus="makeblank(this);" onblur="setTime(this)"/></td>
			<td><input type="text" name="wed4" id="wed4" maxlength="5" onchange="getCompleteTime(this)" value='<fmt:formatDate value="${time.map.wed_slot2_to}" type="time" pattern="HH:mm"/>' onfocus="makeblank(this);" onblur="setTime(this)"/></td>
		</tr>
		<tr>
			<td><insta:ltext key="patient.resourcescheduler.editdoctortiming.thursday"/></td>
			<td><input type="text" name="thu1" id="thu1" maxlength="5" onchange="getCompleteTime(this)" value='<fmt:formatDate value="${time.map.thu_slot1_from}" type="time" pattern="HH:mm"/>' onfocus="makeblank(this);" onblur="setTime(this)"/></td>
			<td><input type="text" name="thu2" id="thu2" maxlength="5" onchange="getCompleteTime(this)" value='<fmt:formatDate value="${time.map.thu_slot1_to}" type="time" pattern="HH:mm"/>' onfocus="makeblank(this);" onblur="setTime(this)"/></td>
			<td><input type="text" name="thu3" id="thu3" maxlength="5" onchange="getCompleteTime(this)" value='<fmt:formatDate value="${time.map.thu_slot2_from}" type="time" pattern="HH:mm"/>' onfocus="makeblank(this);" onblur="setTime(this)"/></td>
			<td><input type="text" name="thu4" id="thu4" maxlength="5" onchange="getCompleteTime(this)" value='<fmt:formatDate value="${time.map.thu_slot2_to}" type="time" pattern="HH:mm"/>' onfocus="makeblank(this);" onblur="setTime(this)"/></td>
		</tr>
		<tr>
			<td><insta:ltext key="patient.resourcescheduler.editdoctortiming.friday"/></td>
			<td><input type="text" name="fri1" id="fri1" maxlength="5" onchange="getCompleteTime(this)" value='<fmt:formatDate value="${time.map.fri_slot1_from}" type="time" pattern="HH:mm"/>' onfocus="makeblank(this);" onblur="setTime(this)"/></td>
			<td><input type="text" name="fri2" id="fri2" maxlength="5" onchange="getCompleteTime(this)" value='<fmt:formatDate value="${time.map.fri_slot1_to}" type="time" pattern="HH:mm"/>' onfocus="makeblank(this);" onblur="setTime(this)"/></td>
			<td><input type="text" name="fri3" id="fri3" maxlength="5" onchange="getCompleteTime(this)" value='<fmt:formatDate value="${time.map.fri_slot2_from}" type="time" pattern="HH:mm"/>' onfocus="makeblank(this);" onblur="setTime(this)"/></td>
			<td><input type="text" name="fri4" id="fri4" maxlength="5" onchange="getCompleteTime(this)" value='<fmt:formatDate value="${time.map.fri_slot2_to}" type="time" pattern="HH:mm"/>' onfocus="makeblank(this);" onblur="setTime(this)"/></td>
		</tr>
		<tr>
			<td><insta:ltext key="patient.resourcescheduler.editdoctortiming.saturday"/></td>
			<td><input type="text" name="sat1" id="sat1" maxlength="5" onchange="getCompleteTime(this)" value='<fmt:formatDate value="${time.map.sat_slot1_from}" type="time" pattern="HH:mm"/>' onfocus="makeblank(this);" onblur="setTime(this)"/></td>
			<td><input type="text" name="sat2" id="sat2" maxlength="5" onchange="getCompleteTime(this)" value='<fmt:formatDate value="${time.map.sat_slot1_to}" type="time" pattern="HH:mm"/>' onfocus="makeblank(this);" onblur="setTime(this)"/></td>
			<td><input type="text" name="sat3" id="sat3" maxlength="5" onchange="getCompleteTime(this)" value='<fmt:formatDate value="${time.map.sat_slot2_from}" type="time" pattern="HH:mm"/>' onfocus="makeblank(this);" onblur="setTime(this)"/></td>
			<td><input type="text" name="sat4" id="sat4" maxlength="5" onchange="getCompleteTime(this)" value='<fmt:formatDate value="${time.map.sat_slot2_to}" type="time" pattern="HH:mm"/>' onfocus="makeblank(this);" onblur="setTime(this)"/></td>
		</tr>
		<tr>
			<td><insta:ltext key="patient.resourcescheduler.editdoctortiming.sunday"/></td>
			<td><input type="text" name="sun1" id="sun1" maxlength="5" onchange="getCompleteTime(this)" value='<fmt:formatDate value="${time.map.sun_slot1_from}" type="time" pattern="HH:mm"/>' onfocus="makeblank(this);" onblur="setTime(this)"/></td>
			<td><input type="text" name="sun2" id="sun2" maxlength="5" onchange="getCompleteTime(this)" value='<fmt:formatDate value="${time.map.sun_slot1_to}" type="time" pattern="HH:mm"/>' onfocus="makeblank(this);" onblur="setTime(this)"/></td>
			<td><input type="text" name="sun3" id="sun3" maxlength="5" onchange="getCompleteTime(this)" value='<fmt:formatDate value="${time.map.sun_slot2_from}" type="time" pattern="HH:mm"/>' onfocus="makeblank(this);" onblur="setTime(this)"/></td>
			<td><input type="text" name="sun4" id="sun4" maxlength="5" onchange="getCompleteTime(this)" value='<fmt:formatDate value="${time.map.sun_slot2_to}" type="time" pattern="HH:mm"/>' onfocus="makeblank(this);" onblur="setTime(this)"/></td>
		</tr>
		</c:forEach>
	</table>
	</td>
</tr>
<tr>
	<td align="center">
		<button type="submit" accesskey="S" onclick="return validate()"><b><u><insta:ltext key="patient.resourcescheduler.editdoctortiming.s"/></u></b><insta:ltext key="patient.resourcescheduler.editdoctortiming.ave"/></button>
		<button type="button" accesskey="C" onclick="doClose();"><b><u><insta:ltext key="patient.resourcescheduler.editdoctortiming.c"/></u></b><insta:ltext key="patient.resourcescheduler.editdoctortiming.lose"/></button>
	</td>
</tr>
<c:if test="${docID != '*' && docID != ''}">
<tr>
	<td>
		<table>
			<tr>
				<td  class="forminput"><b><insta:ltext key="patient.resourcescheduler.editdoctortiming.frominput1"/></b></td>
				<td class="forminput"><a href="#" onclick="return getDoctorWeekNonAvailability('Previous');"></a><b><insta:ltext key="patient.resourcescheduler.editdoctortiming.next"/></b><a href="#" onclick="return getDoctorWeekNonAvailability('Next');"></a></td>
			</tr>
		</table>
	</td>
</tr>
<tr>
	<td>
		<table class="dashboard" id="nonAvailableTable">
			<c:forEach items="${nonAvailabilityTiming}" var="notAvail">
				<tr>
					<td>${notAvail.map.week_day}</td>
					<td><c:set var="naDate"><fmt:formatDate value="${notAvail.map.non_available_date}" type="date" pattern="dd-MM-yyyy"/></c:set>${naDate}</td>
					<td><insta:ltext key="patient.resourcescheduler.editdoctortiming.from"/></td>
					<td><c:set var="firstFrom"><fmt:formatDate value="${notAvail.map.firsthalf_from}" type="time" pattern="HH:mm"/></c:set>${firstFrom}</td>
					<td><insta:ltext key="patient.resourcescheduler.editdoctortiming.to"/></td>
					<td><c:set var="firstTo"><fmt:formatDate value="${notAvail.map.firsthalf_to}" type="time" pattern="HH:mm"/></c:set>${firstTo}</td>
					<td><insta:ltext key="patient.resourcescheduler.editdoctortiming.from"/></td>
					<td><c:set var="secondFrom"><fmt:formatDate value="${notAvail.map.secondhalf_from}" type="time" pattern="HH:mm"/></c:set>${secondFrom}</td>
					<td><insta:ltext key="patient.resourcescheduler.editdoctortiming.to"/></td>
					<td><c:set var="secondTo"><fmt:formatDate value="${notAvail.map.secondhalf_to}" type="time" pattern="HH:mm"/></c:set>${secondTo}</td>
					<td><a href="doctortiming.do?method=getDoctorNonAvailabilityScreen&doctorId=${docID}&doctorName=${docName}&weekday=${notAvail.map.week_no}
					&dept=${notAvail.map.dept_id}&deptName=${notAvail.map.dept_name}&naDate=${naDate}&firstFrom=${firstFrom}&firstTo=${firstTo}
					&secondFrom=${secondFrom}&secondTo=${secondTo}&action=edit"><insta:ltext key="patient.resourcescheduler.common.edit"/></a></td>
				</tr>
			</c:forEach>
		</table>
	</td>
</tr>
</c:if>
<tr>
	<td>
		<table class="formtable">
			<tr>
				<c:if test="${docID != '*' && docID != ''}">
					<td align="right">
						<a href="doctortiming.do?method=getDoctorNonAvailabilityScreen&doctorId=${docID}&doctorName=${docName}&deptName=${depName}&action=add"><insta:ltext key="patient.resourcescheduler.editdoctortiming.adddoctor"/></a>
					</td>
				</c:if>
				<td></td>
			</tr>
		</table>
	</td>
</tr>
</table>
</form>
</body>
</html>
