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
		var resource_type = "${ifn:cleanJavaScript(param.res_sch_type)}";
		var oAutoComp = null;
		var resourceName = '${ifn:cleanJavaScript(param.res_sch_name)}';
		var selectedCenter = '${ifn:cleanJavaScript(param.center_id)}';
		var centersJSON=${ifn:convertListToJson(centersJSON)};
		var max_centers_inc_default = '${max_centers_inc_default}';
		var doctorJSON = ${ifn:convertListToJson(DoctorsJSON)};
		var loggedInCenterId = '${centerId}';
</script>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="css" file="chosen.css"/>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="ajax.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="script" file="chosen.jquery.min.js"/>
	<insta:link type="script" file="resourcescheduler/schedulerdashboard.js"/>
	<insta:link type="script" file="resourcescheduler/ResourceCommon.js"/>
	<insta:link type="script" file="resourcescheduler/ResourceBulkOverride.js"/>
	

	
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
<body onload="init('edit'), initDialog()">
<!-- testingg ${max_centers_inc_default >1 }  ${centerId}  -->
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

	<form name="ResourceAvailabilityForm" method="POST" action="createbulk.htm" autocomplete="off">
		<input type="hidden" name="method" value="createbulk">
		<input type="hidden" name="res_avail_id" id="res_avail_id" value="${bean.map.res_avail_id}">
		<input type="hidden" name="login_center_id" id="login_center_id" value="<%=(Integer) session.getAttribute("centerId") %>"/>
		<input type="hidden" name="login_center_name" id="login_center_name" value="<%=Encoder.cleanHtmlAttribute((String) session.getAttribute("centerName")) %>"/>
		
		<fieldset class="fieldsetborder">
	    <legend class="fieldsetlabel"><insta:ltext key="patient.resourcescheduler.editresourceavailability.resourcedetails"/></legend>
			<table class="formtable">
				<tr>

				<td class="formlabel" style ="display: ${max_centers_inc_default > 1 && centerId==0 ? '' : 'none'}">Center:</td>
				<td class="forminput" style ="display: ${max_centers_inc_default > 1 && centerId==0 ? '' : 'none'}">
					<insta:selectdb name="select_center_id" id="select_center_id"  filtercol="status" filtervalue="A" dummyvalue="-- Select --"
						 table="hospital_center_master" displaycol="center_name" valuecol="center_id" orderby="center_name" onchange="centerChanged(this)"
						 value = "${param.center_id != null && param.center_id !='' ? param.center_id : centerId}"/>
					</tr>
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
						<input type="hidden" name="schedulerName" id="schedulerName"/>
						<div class="chosen-mutli-select">
						<input type="hidden" name="_resource_name" id="_resource_name">
							<select name="res_sch_name" id="res_sch_name" multiple="multiple" size="1" class="chosenElement dropdown" style="width: 410px">
							
						</div>
						</td>
					<td><insta:ltext key="storemgmt.stockapproval.list.selectall"/>
						<input type = "checkbox" name ="select_all" id="select_all" onChange= "selectAllOptions(this)">
				
					
				</tr>
			</table>
		
		
		<table style="margin-top:20px;">
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
			
			<insta:ltext key="ui.label.update.existing"/>
				<input type = "checkbox" name ="override_existing" id="override_existing">
		
		</fieldset>
		
		
		<table class="formtable" id="calTable">
    	<tr>
    		<td>
	  		<c:forEach begin="0" end="6" varStatus="i">
	    	<c:set var="str" value=""/>
	    	<c:set var="sindex" value="${i.index}"/>
	    	<c:set var="str" value="${str}${sindex}"/>
		    	<div id="DayResult${sindex}" style="display: ${sindex==0 ? '' : 'none'}; margin-top:30px;">
			    	<c:set var="style" value=""/>
				<fieldset class="fieldSetBorder" id="fieldset${sindex}">
				<table>
				<tr>
    			<td> <input type="checkbox" name="dow${sindex}" class="${sindex}day0" value = '0' onchange="dowChecked(this)"/> Sunday 
				<td> <input type="checkbox" name="dow${sindex}" class="${sindex}day1" value = '1' onchange="dowChecked(this)"/> Monday 
				<td> <input type="checkbox" name="dow${sindex}" class="${sindex}day2" value = '2' onchange="dowChecked(this)"/> Tuesday 
				<td> <input type="checkbox" name="dow${sindex}" class="${sindex}day3" value = '3' onchange="dowChecked(this)"/> Wednesday 
				<td> <input type="checkbox" name="dow${sindex}" class="${sindex}day4" value = '4' onchange="dowChecked(this)"/> Thursday 
				<td> <input type="checkbox" name="dow${sindex}" class="${sindex}day5" value = '5' onchange="dowChecked(this)"/> Friday 
				<td> <input type="checkbox" name="dow${sindex}" class="${sindex}day6" value = '6' onchange="dowChecked(this)"/> Saturday 
				</tr>
				</table>
		    		<table class="detailList" cellpadding="0" cellspacing="0" id="resultTable${sindex}">
		    			
		    			<tr>
		    				<th><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.fromh"/></th>
		    				<th><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.toh"/></th>
		    				<th><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.status"/></th>
		    				<c:if test="${max_centers_inc_default > 1 && param.res_sch_type == 'DOC'}">
		    					<th><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.center"/></th>
							</c:if>
							<c:if test="${param.res_sch_type == 'DOC'}">
								<th><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.visitMode"/></th>
							</c:if>	
							<th><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.remarks"/></th>
		    				<th>&nbsp;</th>
		    				<th>&nbsp;</th>
		    			</tr>
		    					<c:if test="${rec.map.day_of_week != sindex}">
					    			<tr id="defaultRow${sindex}" class="notAvailableRow">
					    				<td><label><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.0"/></label>
					    					<input type="hidden" name="default_value" id="default_value" value="true">
					    					<input type="hidden" name="day_of_week" id="day_of_week" value="${sindex}">
					    					<input type="hidden" name="card_id" id="card_id" value="${sindex}">
					    					<input type="hidden" name="from_time" id="from_time" value="00:00">
					    				</td>
					    				<td><label><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.23"/></label>
					    					<input type="hidden" name="to_time" id="to_time" value="23:59">
					    				</td>
					    				<td><label><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.notavailable"/></label>
					    					<input type="hidden" name="availability_status" id="availability_status" value="N">
										</td>
										<c:if test="${param.res_sch_type == 'DOC'}">
					    					<td>
										    	<label>&nbsp;</label>
												<input type="hidden" name="visit_mode" id="visit_mode" value="">
											 </td>
										</c:if>	
					    				<td>
					    					<label>&nbsp;</label>
					    					<input type="hidden" name="remarks" id="remarks" value="">
										</td>
					    				<td>
											<label>&nbsp;</label>
											<input type="hidden" name="r_delete" id="r_delete" value="false" onclick="deleteItem(this,${sindex})">
										</td>

					    				<td>
					    					<a title='<insta:ltext key="patient.resourcescheduler.doctortimingdashboard.edit.resource"/>' >
											<img src="${cpath}/icons/Edit.png" class="button" id="editIcon${ifn:cleanHtmlAttribute(index)}" name="editIcon" onclick="openEditResTimingsDialogBox(this,${sindex})"/>
					    				</td>
					    			</tr>
					    			<tr style="display:none">
						    				<td><label>&nbsp;</label>
						    					<input type="hidden" name="default_value" id="default_value" value="">
						    					<input type="hidden" name="day_of_week" id="day_of_week" value="">
						    					<input type="hidden" name="card_id" id="card_id" value="">
						    					<input type="hidden" name="from_time" id="from_time" value="">
						    				</td>
						    				<td><label>&nbsp;</label>
						    					<input type="hidden" name="to_time" id="to_time" value="">
						    				</td>
						    				<td><label>&nbsp;</label>
						    					<input type="hidden" name="availability_status" id="availability_status" value="">
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
						    				<td><label>&nbsp;</label>
						    					<input type="hidden" name="remarks" id="remarks" value="">
											</td>
											
						    				<td>
												<label>&nbsp;</label>
												<input type="hidden" name="r_delete" id="r_delete" value="">
											</td>

						    				<td>
						    					<a title='<insta:ltext key="patient.resourcescheduler.doctortimingdashboard.edit.resourcetmngs"/>' >
													<img src="${cpath}/icons/Edit.png" class="button" id="editIcon${ifn:cleanHtmlAttribute(index)}" name="editIcon" onclick="openEditResTimingsDialogBox(this,${sindex})"/>
												</a>
						    				</td>
						    			</tr>
				    			</c:if>
		    		</table>
		   			<table class="addButton">
						<tr>
							<td align="right">
								<button type="button" name="btnAddItem" id="btnAddItem${sindex}" title='<insta:ltext key="patient.resourcescheduler.doctortimingdashboard.add.newrecord"/>'
										onclick="openResourceTimingsDialog(${sindex});"
										accesskey="+" class="imgButton"><img src="${cpath}/icons/Add.png"></button>
							</td>
						</tr>
					</table>
				</fieldset>
			</div>
			<input type="hidden" name="dialogId" id="dialogId${sindex}">
			<div id="resorceTimingsDialog${sindex}" style="visibility:hidden">
				<div class="hd" id="resourcedialogheader${sindex}"><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.add.edit"/></div>
				<div class="bd">
					<fieldset class="fieldsetborder">
					<legend class="fieldsetlabel"><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.resourceavailability"/></legend>
					<table class="formtable" cellpadding="0" cellspacing="0">
						<tr>
							<td class="formlabel"><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.availability.duration"/></td>
							<td colspan="2">
								<insta:ltext key="patient.resourcescheduler.doctortimingdashboard.from"/>: <select name="dialog_from_time" id="dialog_from_time${sindex}" style="width:60px" class="dropdown" onchange="getCompleteTime('from_time',this,${sindex})"/>
										<option value=""><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.select"/></option>
									</select> <insta:ltext key="patient.resourcescheduler.doctortimingdashboard.hm"/>
							</td>
							<td><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.to"/>: <select name="dialog_to_time" id="dialog_to_time${sindex}" style="width:60px" class="dropdown" onchange="getCompleteTime('to_time',this,${sindex})"/>
										<option value=""><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.select"/></option>
									</select> <insta:ltext key="patient.resourcescheduler.doctortimingdashboard.hm"/>
							</td>
						</tr>
						<tr>
							<!-------------show visit mode dropdown------------>
							<c:if test="${param.res_sch_type == 'DOC'}">
								<td class="formlabel"><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.visitMode"/>:</td>
								<td>
									<select name="dialog_visit_mode" id="dialog_visit_mode${sindex}" class="dropdown" onchange="fillVisitMode(this,${sindex})">
										<option value="I" selected>In Person</option>
										<option value="O">Online</option>
										<option value="B">Both</option>
									</select>
								</td>
							</c:if>
						</tr>
						<tr>
							<td class="formlabel"><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.remarks"/>:</td>
							<td colspan="2">
								<textarea rows="2" cols="20" id="dialog_remarks${sindex}" onblur="checkLength(this,100,'Remarks')"></textarea>
							</td>
						</tr>
					</table>
					</fieldset>
					<table>
						<tr>
							<td>
							<insta:accessbutton buttonkey="patient.resourcescheduler.doctortimingdashboard.ok" name="Ok" type="button" onclick="addRecord(${sindex});" />
							<insta:accessbutton buttonkey="patient.resourcescheduler.doctortimingdashboard.cancel" name="Cancel" type="button" onclick="cancelDialog(${sindex});" />
							</td>
						</tr>
					</table>
				</div>
			</div>
		</c:forEach>
		</td>
	</tr>
	</table>
	
<button type="button" name="btnAddReplica" id="btnAddReplica" title='<insta:ltext key="patient.resourcescheduler.editresourceavailability.add.newrecord"/>'
							onclick="addReplica();"
							class="imgButton"><img src="${cpath}/icons/Add.png"></button>

		<input type="hidden" name="dialogId" id="dialogId">
		<c:url var="url" value="${pagePath}/list.htm">
			<c:param name="method" value="list"/>
			<c:param name="res_sch_type" value="${param.res_sch_type}"/>
			<c:param name="res_sch_name" value="${param.res_sch_name}"/>
		</c:url>
			
		<table class="screenActions">
			<tr>
				<td>
				<!--<button type="submit" accesskey="S" onclick="return validate();"/><b><u><insta:ltext key="patient.resourcescheduler.editresourceavailability.s"/></u></b><insta:ltext key="patient.resourcescheduler.editresourceavailability.ave"/></button>
				-->
				<insta:accessbutton buttonkey="patient.resourcescheduler.editresourceavailability.save" id="savebtn" type="submit" onclick="return validatebulk();" />
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

		
		

</form>
</body>
</html>
