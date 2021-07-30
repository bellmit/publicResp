<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<jsp:useBean id="currentDate" class="java.util.Date"/>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta name="i18nSupport" content="true"/>
<title>Edit Center Delivery Times Overrides</title>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="ajax.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="script" file="reportsdelivery/reportsdeliverytiming.js"/>

	<script>
		var method = '${ifn:cleanJavaScript(param._method)}';
	</script>
	<style type="text/css">
		.notAvailableRow{
			background-color:#EAEAEA; cursor:pointer;
			border-bottom:1px #666 solid;  border-right:1px #999 solid;
			padding:5px 10px 4px 10px;  color:#707070;
		}
	</style>


</head>
<body onload="init();">

	<h1> Edit Center Delivery Times Overrides</h1>
	<insta:feedback-panel/>
	<c:set var="valueDate">
		<fmt:formatDate pattern="dd-MM-yyyy" value="${currentDate}"/>
	</c:set>
	<c:set var="reportDate">
		<fmt:formatDate pattern="dd-MM-yyyy" value="${reportDate}"/>
	</c:set>

	<form action="reportscenteroverrides.do" name="ReportsOverrideForm" autocomplete="off">
		<input type="hidden" name="_method" value="update"/>
		<input type="hidden" name="center_id" id="center_id" value="${ifn:cleanHtmlAttribute(param.center_id)}">
		<fmt:formatDate var="report_date" pattern="dd-MM-yyyy " value="${rBean.map.day}"/>
		<input type="hidden" name="report_date" id="report_date" value="${report_date}">
		<fieldset class="fieldsetborder">
	    <legend class="fieldsetlabel">Edit Center Delivery Times Overrides</legend>
		<table class="formtable">
				<tr>
					<td class="formlabel" style="width:70px;">Center Name:</td>
					<td class="forminfo" style="width : 300px">${bean.map.center_name}</td>
					<td class="formlabel">Report Overridden Date:</td>
					<c:if test="${fn:length(reportsAvailtimingList) == 0}">
						<td>&nbsp;</td>
					</c:if>
					<c:if test="${fn:length(reportsAvailtimingList) gt 0}">
						<td><label style="text-align:bottom"><b><fmt:formatDate pattern="dd-MM-yyyy" value="${rBean.map.day}"/></b></label></td>
					</c:if>
				</tr>
			</table>
		</fieldset>
		<div class="resultList">
			<fieldset class="fieldsetborder">
	    	<legend class="fieldsetlabel">Edit Center Reports Overrides</legend>
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
				<tr>
					<th>Report Delivery Time (HH:MM):</th>
					<th>&nbsp;</th>
	    			<th>&nbsp;</th>
				</tr>
				<c:if test="${empty reportsAvailtimingList}">
					<tr id="defaultRow" class="notAvailableRow">
	    				<td><label></label>
	    					<input type="hidden" name="delivery_time" id="delivery_time" value="">
	    					<input type="hidden" name="rep_deliv_override_id" id="rep_deliv_override_id" value="">
	    					<input type="hidden" name="rep_delivery_override_time_id" id="rep_delivery_override_time_id" value="">
	    					<input type="hidden" name="default_value" id="default_value" value="true">
	    				</td>
	    				<td><input type="hidden" name="r_delete" id="r_delete" value="false" onclick="return deleteItem(this);"></td>			
	    				
	    				<td></td>
					</tr>
				</c:if>
				<c:set var="style" value='style=""'/>
					<c:set var="length" value="${fn:length(reportsAvailtimingList)}"/>
					<c:forEach var="i" begin="1" end="${length+1}" varStatus="loop">
						<c:set var="records" value="${reportsAvailtimingList[i-1]}"/>
						<c:if test="${empty records}">
							<c:set var="style" value='style="display:none"'/>
						</c:if>
		    			<tr ${style} ">
		    				<td><label><fmt:formatDate pattern="HH:mm" value="${records.map.delivery_time}"/></label>
		    					<input type="hidden" name="delivery_time" id="delivery_time" value='<fmt:formatDate pattern="HH:mm" value="${records.map.delivery_time}"/>'>
		    					<input type="hidden" name="rep_deliv_override_id" id="rep_deliv_override_id" value="${records.map.rep_deliv_override_id}">
		    					<input type="hidden" name="rep_delivery_override_time_id" id="rep_delivery_override_time_id" value="${records.map.rep_delivery_override_time_id}">
		    					<input type="hidden" name="default_value" id="default_value" value="false">
		    				</td>
		    				<td>
		    					<input type="hidden" name="r_delete" id="r_delete" value="false">
			    					<a title="">
										<img src="${cpath}/icons/Delete.png" class="imgDelete button" onclick="return deleteItem(this);" title='Delete Report Availability Timings'/>
									</a>
								</td>
		    				<td>
		    					<a title='Edit Reports Availability Timings' >
								<img src="${cpath}/icons/Edit.png" class="button" id="editIcon${index}" name="editIcon" onclick="openEditReportsTimingsDialogBox(this)"/>
		    				</td>
		    			</tr>
		    		</c:forEach>
			</table>
			<table class="addButton">
						<tr>
							<td align="right">
								<button type="button" name="btnAddItem" id="btnAddItem" title='Add Reports Availability Timings'
										onclick="openReportsTimingsDialog();"
										accesskey="+" class="imgButton"><img src="${cpath}/icons/Add.png"></button>
							</td>
						</tr>
					</table>
			</fieldset>
		</div>
		<input type="hidden" name="dialogId" id="dialogId">
		<c:url var="url" value="reportscenteroverrides.do">
			<c:param name="_method" value="list"/>
			<c:param name="center_id" value="${param.center_id}"/>
		</c:url>
		<c:if test="${fn:length(reportsAvailtimingList) == 0 }">
			<table>
				<tr>
					<td>&nbsp;</td>
					<td><b>Apply Report Delivery Overrides Date Ranges:</b></td>
					<td><insta:datewidget name="b_report_start_date" id="b_report_start_date" btnPos="left" value="${valueDate}"/></td>
					<td><insta:datewidget name="b_report_end_date" id="b_report_end_date" btnPos="left" value="${valueDate}"/></td>
					<td colspan="2">&nbsp;</td>
				</tr>
			</table>
		</c:if>
		<table class="screenActions">
			<tr>
				<td><button type="submit" accesskey="S" onclick="return validate();"/><b><u>S</u></b>ave</button></td>
				<td>&nbsp;<a href="<c:out value='${url}' />">Reports Override List</a></td>
				 <td> &nbsp;| <insta:screenlink screenId="mas_centers" extraParam="/show.htm?&center_id=${param.center_id}" label="Back To Center"  addPipe="false"/></td>
			</tr>
		</table>
		<div id="reportsTimingsDialog" style="display:none">
			<div class="hd" id="reportsdialogheader">Add Reports Delivery Timings</div>
			<div class="bd">
				<fieldset class="fieldsetborder">
				<legend class="fieldsetlabel">Reports Delivery Availability</legend>
				<table class="formtable" cellpadding="0" cellspacing="0">
					<tr>
						<td class="formlabel">Report Delivery Time (HH:MM):</td>
						<td><input type="text" name="report_time" id="report_time" class="number"
							maxlength="5" value="${currenttime}"/></td>
					</tr>
					
				</table>
				</fieldset>
				<table>
					<tr>
						<td>
						<insta:accessbutton buttonkey="patient.resourcescheduler.doctortimingdashboard.ok" name="Ok" type="button" onclick="addRecord();" />
							<insta:accessbutton buttonkey="patient.resourcescheduler.doctortimingdashboard.cancel" name="Cancel" type="button" onclick="cancelDialog();" />
						</td>
					</tr>
				</table>
			</div>
		</div>
		
		<table style="margin-top: 10px">
				<tr>
					 <c:if test="${param._method == 'show'}" >
						 <td> | <insta:screenlink screenId="mas_reports_cen_app" extraParam="?_method=getScreen&center_id=${param.center_id}" label="Edit Default Center Reports Delivery Time"  addPipe="false"/></td>
					</c:if>
					<c:if test="${param._method == 'show'}" >
						 <td> | <insta:screenlink screenId="mas_reports_overrides" extraParam="?_method=getScreen&center_id=${param.center_id}" label="Edit Center Delivery Times Overrides"  addPipe="false"/></td>
					</c:if>	 		 	
				</tr>
			</table>
</form>
</body>
</html>
