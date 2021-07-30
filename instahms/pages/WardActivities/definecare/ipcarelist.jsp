<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" scope="request"/>
<jsp:useBean id="currentDate" class="java.util.Date"/>
<html>

<head>
	<title>Add Care Team - Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="script" file="wardactivities/definecare/ipcare.js"/>
	<insta:link type="js" file="shareLoginDialogCommon.js" />
	<insta:link type="script" file="hmsvalidation.js"/>
	<script>
	</script>
	
	<insta:js-bundle prefix="registration.patient"/>
	<script type="text/javascript">
		var doctors = ${doctors};
		var loggedInUserId = '${ifn:cleanJavaScript(userId)}';
		var currentDateAndTime = new Date(<%= (new java.util.Date()).getTime() %>);
		var curDate = '<fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${currentDate}"/>';
	</script>
	<style>
		.scrollForContainer .yui-ac-content{
			 max-height:11em;overflow:auto;overflow-x:auto; /* scrolling */
		}
		.yui-ac {
			padding-bottom: 20px;
		}
	</style>	
</head>
<body onload="init();">
	<h1>Add Care Team</h1>
	<insta:feedback-panel/>
	<insta:patientdetails  visitid="${not empty param.patient_id? param.patient_id : patient_id}" showClinicalInfo="true"/>
	<form name="careTeamForm" method="POST" action="${cpath}/wardactivities/IPCareTeam.do?" onsubmit="return false;">
	<input type="hidden" name="_method" value="save">
	<c:set var= "visitId" value="${not empty param.patient_id? param.patient_id : patient_id}"/>
	<input type="hidden" name="patient_id" value="${visitId}">
		<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel">
			Doctors On Care Team
		</legend>
		<div style="height: 30px;">
			<table>
			<tr>
				<td class="formlabel" style="width:45px">Doctor :</td>
				<td style="width: 180px">
					<div id="doctorAutoComplete ">
						<input type="text" name="doctor" id="doctor" value=""/>
						<input type="hidden" name="doctor_id" id="doctor_id" value=""/>
					<div id="doctorContainer" class="scrollForContainer" style="width: 350px"/>
					</div>
				</td>
				<td>&nbsp;&nbsp;</td>
				<td style="width: 100px">
					<button type="button" name="btnAddItem" id="btnAddItem" title="Add Care Doctor (Alt_Shift_+)"
					onclick="return addDoctorToGrid(this);" 
					accesskey="+" class="imgButton"><img src="${cpath}/icons/Add.png"></button>
				</td>
			</tr>
			</table>
		</div>
		<div class="resultList">
			<table class="detailList dialog_displayColumns" cellspacing="0" cellpadding="0" style="margin-top: 5px" id="itemsTable" border="0" width="100%">
				<tr>
					<th>S.No</th>
					<th>Doctor Name</th>
					<th>Department</th>
					<th>Date Added</th>
					<th>Added By </th>
					<th>Action </th>
				</tr>
				<c:set var="numRecords" value="${fn:length(visitcarelist)}"/>
				<c:forEach begin="1" end="${numRecords+1}" var="i" varStatus="loop">
					<c:set var="record" value="${visitcarelist[i-1].map}"/>
					<c:set var="flagColor" value="empty"/>
					<c:if test="${not empty admitingDocId}">
						<c:set var="flagColor" value="grey"/>
					</c:if>
					<c:if test="${empty record}">
						<c:set var="tr_style" value='style="display:none"'/>
					</c:if>
					<fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${record.mod_time}" var="datetime"/>
					<tr ${tr_style}>
						<td>${i}
							<input type="hidden" name="h_doctor_id" id="h_doctor_id" value="${record.care_doctor_id}"/>
							<input type="hidden" name="h_dept_id"  id="h_dept_id" value="${record.dept_id}"/>
							<input type="hidden" name="h_dept_name" id="h_dept_name" value="${record.dept_name}"/>
							<input type="hidden" name="h_user_name" id="h_user_name" value="${record.username}"/>
							<input type="hidden" name="h_doctor_name" id="h_doctor_name" value=""/>
							<input type="hidden" name="h_mode_time" id="h_mode_time" value="${datetime}"/>
							<input type="hidden" name="h_isadded" id="h_isadded" value="false"/>
							<input type="hidden" name="h_delItem" id="h_delItem" value="false"/>
						</td>
						<td>${record.doctor_name}</td>
						<td>${record.dept_name}</td>
						<td>${datetime}</td>
						<td>${record.username}</td>
						<td>
							<c:choose>
								<c:when test="${admitingDocId == record.care_doctor_id}">
									<img src="${cpath}/icons/delete_disabled.gif" class="imgDelete button" />
								</c:when>
								<c:otherwise>
									<a href="javascript:Cancel Item" onclick="return cancelItem(this);" title="Cancel care doctor" >
										<img src="${cpath}/icons/delete.gif" class="imgDelete button" />
									</a>
								</c:otherwise>
							</c:choose>
						</td>
				</tr>
				</c:forEach>
				
			</table>
		</div>
	</fieldset>	
	<div>
		<button type="button" accesskey="S" name="save" id="save" onclick="return saveIPCare();"><b><u>S</u></b>ave</button>
		| <a href="${cpath}/pages/ipservices/IpservicesList.do?_method=getIPDashBoard&sortOrder=mr_no&sortReverse=true">In Patient List</a>
	</div>
	</form>
</body>
</html>
