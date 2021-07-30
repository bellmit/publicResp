<%@page import="com.insta.hms.master.URLRoute"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.common.Encoder" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="pagePath" value="<%=URLRoute.RESOURCE_AVAILABILITY_OVERRIDE_PATH %>"/>
<c:set var="max_centers_inc_default"
	value='<%= GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default") %>'
	scope="request" />
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<jsp:useBean id="currentDate" class="java.util.Date"/>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta name="i18nSupport" content="true"/>
<title><insta:ltext key="patient.resourcescheduler.editresourceavailability.title"/></title>
<script>
		var resourceType = "${ifn:cleanJavaScript(param.res_sch_type)}";
		var gResourceType = "${ifn:cleanJavaScript(param.res_sch_type)}";
		var method = "${ifn:cleanJavaScript(param.method)}";
		var allResourcesList = ${ifn:convertListToJson(allResourcesList)};
		var oAutoComp = null;
		var resourceName = '${ifn:cleanJavaScript(param.res_sch_name)}';
		var centersJSON=${ifn:convertListToJson(centersJSON)};
		var max_centers_inc_default = '${max_centers_inc_default}';
		var doctorJSON = ${ifn:convertListToJson(DoctorsJSON)};
		var loggedInCenterId = '${centerId}';
</script>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="ajax.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="script" file="resourcescheduler/resourceavailability.js"/>
	<insta:link type="script" file="resourcescheduler/ResourceCommon.js"/>

	
	<style type="text/css">
		.notAvailableRow{
			background-color:#EAEAEA; cursor:pointer;
			border-bottom:1px #666 solid;  border-right:1px #999 solid;
			padding:5px 10px 4px 10px;  color:#707070;
		}
	</style>

<insta:js-bundle prefix="scheduler.doctornonavailability"/>
<insta:js-bundle prefix="clinicaldata.commonvalidations"/>
<insta:js-bundle prefix="scheduler.resourceavailability"/>
<insta:js-bundle prefix="scheduler.doctorscheduler"/>
<insta:js-bundle prefix="scheduler.todaysappointment"/>
<insta:js-bundle prefix="scheduler.schedulerdashboard"/>
<insta:js-bundle prefix="scheduler.portalappointment"/>

</head>
<body onload="init('edit')">
<c:set var="addText">
		<insta:ltext key="patient.resourcescheduler.common.add"/>
	</c:set>
	<h1>${addText} <insta:ltext key="patient.resourcescheduler.editresourceavailability.h1"/></h1>
	<insta:feedback-panel/>
	<c:set var="valueDate">
		<fmt:formatDate pattern="dd-MM-yyyy" value="${currentDate}"/>
	</c:set>
	<c:set var="rFormDate">
		<fmt:formatDate pattern="dd-MM-yyyy" value="${recordFromDate}"/>
	</c:set>
	<c:set var="rToDate">
		<fmt:formatDate pattern="dd-MM-yyyy" value="${recordToDate}"/>
	</c:set>

	<form name="ResourceAvailabilityForm" method="POST" action="create.htm" autocomplete="off">
		<input type="hidden" name="method" value="create">
		<input type="hidden" name="res_avail_id" id="res_avail_id" value="${bean.map.res_avail_id}">
		<input type="hidden" name="login_center_id" id="login_center_id" value="<%=(Integer) session.getAttribute("centerId") %>"/>
		<input type="hidden" name="login_center_name" id="login_center_name" value="<%=Encoder.cleanHtmlAttribute((String) session.getAttribute("centerName")) %>"/>
		<fieldset class="fieldsetborder">
	    <legend class="fieldsetlabel"><insta:ltext key="patient.resourcescheduler.editresourceavailability.resourcedetails"/></legend>
			<table class="formtable">
				<tr>
					<td class="formlabel"><insta:ltext key="patient.resourcescheduler.editresourceavailability.resourcetype"/></td>
						<td class="forminput">
							<select name="res_sch_type" id="res_sch_type" class="dropdown" onchange="loadResources(this)">
								<option value=""><insta:ltext key="patient.resourcescheduler.editresourceavailability.select"/></option>
								<option value="DOC" ${param.res_sch_type == 'DOC' ? 'selected' : ''}><insta:ltext key="patient.resourcescheduler.editresourceavailability.doctorconsultation"/></option>
								<c:forEach var="rec" items="${categoryDescripton}" varStatus="index">
									<option value="${rec.res_sch_type}" ${param.res_sch_type == rec.res_sch_type  ? 'selected' : ''}>${rec.resource_description}</option>
								</c:forEach>
							</select>
						</td>
					
					<td class="formlabel"><insta:ltext key="patient.resourcescheduler.editresourceavailability.resources"/></td>
						<td>
							<div class="sboFieldInput" id="resourceAutoComplete">
								<input type="text" name="_resource_name" id="_resource_name" class="autocomplete">
								<input type="hidden" name="res_sch_name" id="res_sch_name"/>
								<div id="resourceNameContainer"></div>
							</div>
						</td>
					
					<td class="formlabel"><insta:ltext key="patient.resourcescheduler.editresourceavailability.availabilitydate"/></td>
						<td>&nbsp;</td>
				</tr>
			</table>
		</fieldset>
		<div class="resultList">
			<fieldset class="fieldsetborder">
	    	<legend class="fieldsetlabel"><insta:ltext key="patient.resourcescheduler.editresourceavailability.overrides"/></legend>
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
				<tr>
					<th><insta:ltext key="patient.resourcescheduler.editresourceavailability.fromhm"/></th>
					<th><insta:ltext key="patient.resourcescheduler.editresourceavailability.tohm"/></th>
					<th><insta:ltext key="patient.resourcescheduler.editresourceavailability.status"/></th>
					<c:if test="${max_centers_inc_default > 1 && param.res_sch_type == 'DOC'}">
						<th><insta:ltext key="patient.resourcescheduler.editresourceavailability.center"/></th>
					</c:if>
					<c:if test="${param.res_sch_type == 'DOC'}">
						<th><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.visitMode"/></th>
					</c:if>	
					<th><insta:ltext key="patient.resourcescheduler.editresourceavailability.remarks"/></th>
					<th>&nbsp;</th>
	    			<th>&nbsp;</th>
				</tr>
				<c:if test="${empty resourceAvailtimingList}">
					<tr id="defaultRow" class="notAvailableRow">
	    				<td><label><insta:ltext key="patient.resourcescheduler.editresourceavailability.0"/></label>
	    					<input type="hidden" name="default_value" id="default_value" value="true">
	    					<input type="hidden" name="from_time" id="from_time" value="00:00">
	    					<input type="hidden" name="res_avail_details_id" id="res_avail_details_id" value="">
	    					<input type="hidden" name="res_avail_id" id="res_avail_id" value="">
	    				</td>
	    				<td><label><insta:ltext key="patient.resourcescheduler.editresourceavailability.23"/></label>
	    					<input type="hidden" name="to_time" id="to_time" value="23:59">
	    				</td>
	    				<td><label><insta:ltext key="patient.resourcescheduler.editresourceavailability.notavailable"/></label>
	    					<input type="hidden" name="availability_status" id="availability_status" value="N">
	    				</td>
	    				<c:if test="${max_centers_inc_default > 1 && param.res_sch_type == 'DOC'}">
					    	<td>
								<label>&nbsp;</label>
								<input type="hidden" name="center_name" id="center_name" value="">
								<input type="hidden" name="center_id" id="center_id" value="">
							</td>
						</c:if>
						<c:if test="${param.res_sch_type == 'DOC'}">
							<td>
								<label>&nbsp;</label>
								<input type="hidden" name="visit_mode" id="visit_mode" value="">
							 </td>
						</c:if>	
	    				<td>
							<label>&nbsp;</label>
							<input type="hidden" name="remarks" id="remarks" value="">
							<input type="hidden" name="r_delete" id="r_delete" value="false" onclick="deleteItem(this)">
						</td>
						<td>&nbsp;</td>
						<td>
	    					<a title='<insta:ltext key="patient.resourcescheduler.editresourceavailability.edit.resourcetimings"/>' >
								<img src="${cpath}/icons/Edit.png" class="button" id="editIcon" name="editIcon" onclick="openEditResTimingsDialogBox(this)"/>
							</a>
	    				</td>
					</tr>
				</c:if>
				<c:set var="style" value='style=""'/>
					<c:set var="length" value="${fn:length(resourceAvailtimingList)}"/>
					<c:forEach var="i" begin="1" end="${length+1}" varStatus="loop">
						<c:set var="records" value="${resourceAvailtimingList[i-1]}"/>
						<c:if test="${empty records}">
							<c:set var="style" value='style="display:none"'/>
						</c:if>
		    			<tr ${style} class="${records.map.availability_status == 'N' ? 'notAvailableRow': ''}">
		    				<td><label><fmt:formatDate pattern="HH:mm" value="${records.map.from_time}"/></label>
		    					<input type="hidden" name="default_value" id="default_value" value="false">
		    					<input type="hidden" name="from_time" id="from_time" value='<fmt:formatDate pattern="HH:mm" value="${records.map.from_time}"/>'>
		    					<input type="hidden" name="res_avail_details_id" id="res_avail_details_id" value="${records.map.res_avail_details_id}">
		    					<input type="hidden" name="res_avail_id" id="res_avail_id" value="${records.map.res_avail_id}">
		    				</td>
		    				<td><label><fmt:formatDate pattern="HH:mm" value="${records.map.to_time}"/></label>
		    					<input type="hidden" name="to_time" id="to_time" value='<fmt:formatDate pattern="HH:mm" value="${records.map.to_time}"/>'>
		    				</td>
		    				<td><label>${records.map.availability_status == 'A' ? 'Available' : 'Not Available'}</label>
		    					<input type="hidden" name="availability_status" id="availability_status" value="${records.map.availability_status}">
		    				</td>
		    				<c:if test="${max_centers_inc_default > 1 && param.res_sch_type == 'DOC'}">
		    					<td>
									<label>${records.center_name}</label>
									<input type="hidden" name="center_name" id="center_name" value="${records.center_name}">
									<input type="hidden" name="center_id" id="center_id" value="${records.center_id}">
								</td>
							</c:if>
							<c:if test="${param.res_sch_type == 'DOC'}">
								<td>
									<label> 
										<c:choose>
											<c:when test="${records.map.visit_mode == 'B' && records.map.availability_status == 'A' }">Both</c:when>   
											<c:when test="${records.map.visit_mode == 'O' && records.map.availability_status == 'A' }">Online</c:when>
											<c:when test="${records.map.visit_mode == 'I' && records.map.availability_status == 'A' }">In Person</c:when>
										<c:otherwise></c:otherwise>
										</c:choose>
									</label>
									<input type="hidden" name="visit_mode" id="visit_mode" value="${records.map.visit_mode}">	
								</td>
							</c:if>
		    				<td>
								<label><insta:truncLabel value="${records.map.remarks}" length="50"/></label>
								<input type="hidden" name="remarks" id="remarks" value="${records.map.remarks}">
								<input type="hidden" name="r_delete" id="r_delete" value="false" >
							</td>
		    				<td>
								<c:choose>
									<c:when test="${param.res_sch_type != 'DOC' && records.map.availability_status == 'A'}">
										<a title="">
											<img src="${cpath}/icons/Delete.png" class="imgDelete button" onclick="deleteItem(this)" title='<insta:ltext key="patient.resourcescheduler.editresourceavailability.delete.resource"/>'/>
										</a>
									</c:when>
									<c:when test="${records.availability_status == 'A' && (centerId == 0 || centerId == records.center_id)}">
										<c:if test="${param.res_sch_type == 'DOC'}">
											<a title="">
												<img src="${cpath}/icons/Delete.png" class="imgDelete button" onclick="deleteItem(this)" title='<insta:ltext key="patient.resourcescheduler.editresourceavailability.delete.resource"/>'/>
											</a>
										</c:if>
									</c:when>
									<c:otherwise>
										<a title="">
											<img src="${cpath}/icons/Delete1.png" class="imgDelete button" onclick=""/>
										</a>
									</c:otherwise>
								</c:choose>
							</td>
		    				<td>
		    					<a title='<insta:ltext key="patient.resourcescheduler.editresourceavailability.edit.resourcetimings"/>' >
								<img src="${cpath}/icons/Edit.png" class="button" id="editIcon${ifn:cleanHtmlAttribute(index)}" name="editIcon" onclick="openEditResTimingsDialogBox(this)"/>
		    				</td>
		    			</tr>
		    		</c:forEach>
			</table>
			<table class="addButton">
						<tr>
							<td align="right">
								<button type="button" name="btnAddItem" id="btnAddItem" title='<insta:ltext key="patient.resourcescheduler.editresourceavailability.add.newrecord"/>'
										onclick="openResourceTimingsDialog();"
										accesskey="+" class="imgButton"><img src="${cpath}/icons/Add.png"></button>
							</td>
						</tr>
					</table>
			</fieldset>
		</div>
		<input type="hidden" name="dialogId" id="dialogId">
		<c:url var="url" value="${pagePath}/list.htm">
			<c:param name="method" value="list"/>
			<c:param name="res_sch_type" value="${param.res_sch_type}"/>
			<c:param name="res_sch_name" value="${param.res_sch_name}"/>
		</c:url>
		<br>
			<table>
				<tr>
					<td class="formlabel"><b><insta:ltext key="patient.resourcescheduler.editresourceavailability.applytimings"/></b></td>
					<td>&nbsp;</td>
					<td><insta:ltext key="patient.resourcescheduler.editresourceavailability.from"/>:</td>
					<td><insta:datewidget name="from_date" id="from_date" value="${valueDate}"/></td>
					<td colspan="2">&nbsp;</td>
					<td colspan="2">&nbsp;</td>
					<td><insta:ltext key="patient.resourcescheduler.editresourceavailability.to"/></td>
					<td><insta:datewidget name="to_date" id="to_date" value="${valueDate}"/></td>
				</tr>
			</table>
		<table class="screenActions">
			<tr>
				<td>
				<!--<button type="submit" accesskey="S" onclick="return validate();"/><b><u><insta:ltext key="patient.resourcescheduler.editresourceavailability.s"/></u></b><insta:ltext key="patient.resourcescheduler.editresourceavailability.ave"/></button>
				-->
				<insta:accessbutton buttonkey="patient.resourcescheduler.editresourceavailability.save" type="submit" onclick="return validate();" />
				</td>
				<td>&nbsp;</td>
				<td><a href="<c:out value='${url}'/>"><insta:ltext key="patient.resourcescheduler.editresourceavailability.resourceavailabilitylist"/></a></td>
				<c:if test="${not empty screenName && not empty referer}">
					<td>&nbsp;|&nbsp;</td>
					<td>
						<a href="<c:out value='${referer}'/>"><insta:ltext key="patient.resourcescheduler.editresourceavailability.backtoscheduler"/></a>
					</td>
				</c:if>
			</tr>
		</table>
		<div id="resorceTimingsDialog" style="display:none">
			<div class="hd" id="resourcedialogheader"><insta:ltext key="patient.resourcescheduler.editresourceavailability.addeditresorce"/></div>
			<div class="bd">
				<fieldset class="fieldsetborder">
				<legend class="fieldsetlabel"><insta:ltext key="patient.resourcescheduler.editresourceavailability.resourceavailability"/></legend>
				<table class="formtable" cellpadding="0" cellspacing="0">
					<tr>
						<td class="formlabel"><insta:ltext key="patient.resourcescheduler.editresourceavailability.availabilityduration"/></td>
						<td colspan="2">
							<insta:ltext key="patient.resourcescheduler.editresourceavailability.from"/>: <select name="dialog_from_time" id="dialog_from_time" style="width:60px" class="dropdown" onchange="getCompleteTime('from_time',this)"/>
									<option value=""><insta:ltext key="patient.resourcescheduler.editresourceavailability.select"/> </option>
								</select> <insta:ltext key="patient.resourcescheduler.editresourceavailability.hm"/>
						</td>
						<td> <insta:ltext key="patient.resourcescheduler.editresourceavailability.to"/>: <select name="dialog_to_time" id="dialog_to_time" style="width:60px" class="dropdown" onchange="getCompleteTime('to_time',this)"/>
									<option value=""> <insta:ltext key="patient.resourcescheduler.editresourceavailability.select"/> </option>
								</select>  <insta:ltext key="patient.resourcescheduler.editresourceavailability.hm"/>
						</td>
					</tr>
					<tr>
						<c:if test="${max_centers_inc_default > 1 && param.res_sch_type == 'DOC'}">
							
							<td class="formlabel"><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.center"/>:</td>
							<td>
								<select name="dialog_center_name" id="dialog_center_name" class="dropdown" onchange="fillHidenCeterName(this)"/></select>
								<label id="dialog_center_label" ></label>
								<input type="hidden" name="dialog_center_name_hid" id="dialog_center_name_hid" value="">
							</td>
						</c:if>
						<!-------------show visit mode dropdown------------>
						<c:if test="${param.res_sch_type == 'DOC'}">
							<td class="formlabel"><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.visitMode"/>:</td>
							<td>
								<select name="dialog_visit_mode" id="dialog_visit_mode" class="dropdown" onchange="fillVisitMode(this)">
									<option value="I" selected>In Person</option>
									<option value="O">Online</option>
									<option value="B">Both</option>
								</select>
							</td>
						</c:if>
					</tr>
					<tr>
						<td class="formlabel"><insta:ltext key="patient.resourcescheduler.editresourceavailability.remarks"/>:</td>
						<td colspan="2">
							<textarea rows="2" cols="20" id="dialog_remarks" onblur="checkLength(this,100,'Remarks')"></textarea>
						</td>
					</tr>
				</table>
				</fieldset>
				<table>
					<tr>
						<td>
						<insta:accessbutton buttonkey="patient.resourcescheduler.editresourceavailability.ok" name="Ok" type="button" onclick="addRecord();" />
						<insta:accessbutton buttonkey="patient.resourcescheduler.editresourceavailability.cancel" name="Cancel" type="button" onclick="cancelDialog();" />
						</td>
					</tr>
				</table>
			</div>
		</div>
</form>
</body>
</html>
