<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta name="i18nSupport" content="true"/>
<title>Edit Default Center Report Delivery Times</title>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="ajax.js"/>
	<insta:link type="script" file="reportsdelivery/reportsdeliverydashboard.js"/>
	<insta:link file="SpryAssets/SpryCollapsiblePanel.js" type="js"/>
	<insta:link path="scripts/SpryAssets/SpryCollapsiblePanel.css" type="css" />

<style type="text/css">
		.notAvailableRow{
			background-color:#EAEAEA; cursor:pointer;
			border-bottom:1px #666 solid;  border-right:1px #999 solid;
			padding:5px 10px 4px 10px;  color:#ffffff;
		}
</style>

<insta:js-bundle prefix="scheduler.doctornonavailability"/>
<insta:js-bundle prefix="clinicaldata.commonvalidations"/>
<insta:js-bundle prefix="widgets.commonvalidations"/>
<insta:js-bundle prefix="scheduler.resourceavailability"/>
<insta:js-bundle prefix="scheduler.doctorscheduler"/>
<insta:js-bundle prefix="scheduler.todaysappointment"/>
<insta:js-bundle prefix="scheduler.schedulerdashboard"/>
<insta:js-bundle prefix="scheduler.portalappointment"/>
</head>
<body onload="initDialog();">

<form name="reportsAvailableForm"  method="POST" action="reportscenterapplicability.do"  autocomplete="off">
<input type="hidden" name="_method" value="update">
<input type="hidden" name="center_id" value="${ifn:cleanHtmlAttribute(param.center_id)}">
	<jsp:useBean id="daysMap" class="java.util.HashMap"/>
		<c:set target="${daysMap}" property="0" value= "Sunday" />
		<c:set target="${daysMap}" property="1" value= "Monday" />
		<c:set target="${daysMap}" property="2" value= "Tuesday" />
		<c:set target="${daysMap}" property="3" value= "Wednesday" />
		<c:set target="${daysMap}" property="4" value= "Thursday" />
		<c:set target="${daysMap}" property="5" value= "Friday" />
		<c:set target="${daysMap}" property="6" value= "Saturday" />
	<h1>Edit Default Center Report Delivery Times</h1>

	<insta:feedback-panel/>
	<fieldset class="fieldSetBorder">
	<table class="formtable">
		<tr>
			<td class="formlabel" style="width:70px;">Center Name:</td>
			<td class="forminfo" style="width : 300px">${bean.map.center_name}</td>
		</tr>
	</table>
	</fieldset>
    <c:set var="style" value=""/>
    <c:if test="${empty reportsAvailabilitiesList}">
    	<c:set var="style" value='style="display:none"'/>
    </c:if>
    <fieldset class="fieldsetborder">
    <legend class="fieldsetlabel">Edit Default Center Reports Delivery Times</legend>
    <table class="formtable" id="calTable">
    	<tr>
    		<td>
	  		<c:forEach begin="0" end="6" varStatus="i">
	    	<c:set var="str" value=""/>
	    	<c:set var="sindex" value="${i.index}"/>
	    	<c:set var="str" value="${str}${sindex}"/>
	   			<br/>
		    	<div id="CollapsiblePanel${sindex}" class="CollapsiblePanel">
				<div class=" title CollapsiblePanelTab"  style=" border-left:none;" >
					<div class="fltL " style="width: 230px; margin:5px 0px 0px 10px;"><b><i><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.dayofthe"/>${daysMap[str]}</i></b></div>
					<div class="fltR txtRT" style="width: 25px; margin:-10px 10px 0px 680px;">
						<img src="${cpath}/images/down.png" />
					</div>
					<div class="clrboth"></div>
				</div>
				<c:choose>
			    	<c:when test="${not empty reportsAvailableMap &&  not empty reportsAvailableMap[str]}">
			    		<c:set var="style" value='style="display:none"'/>
			    	</c:when>
			    	<c:otherwise>
			    			<c:set var="style" value=""/>
			    	</c:otherwise>
			    </c:choose>
				<fieldset class="fieldSetBorder" id="fieldset${sindex}">
		    		<table class="detailList" cellpadding="0" cellspacing="0" id="resultTable${sindex}">
		    			<tr>
		    				
		    				<th>Report Delivery Time (HH:MM):</th>
		    				<th>&nbsp;</th>
		    				<th>&nbsp;</th>
		    			</tr>
		    			<c:choose>
			    			<c:when test="${empty reportsAvailabilitiesList}">
					    			 <tr style="display: none;" class="notAvailableRow">
					    				<td><label></label>
					    					<input type="hidden" name="default_value" id="default_value" value="true">
					    					<input type="hidden" name="day_of_week" id="day_of_week" value="${sindex}">
					    					<input type="hidden" name="delivery_time" id="delivery_time" value="">
					    					<input type="hidden" id="rep_deliv_default_id" name="rep_deliv_default_id" value=""/>
					    					<input type="hidden" name="r_delete" id="r_delete" value="false">
					    				</td>	
					    				<td>
					    					<a title='Edit Report Delivery Timings' >
											<img src="${cpath}/icons/Edit.png" class="button" id="editIcon${index}" name="editIcon" onclick="openEditReportsTimingsDialogBox(this,${sindex})"/>
					    				</td>
					    			</tr> 
					    			 <tr style="display:none">
						    				<td><label>&nbsp;</label>
						    					<input type="hidden" name="default_value" id="default_value" value="">
						    					<input type="hidden" name="day_of_week" id="day_of_week" value="">
						    					<input type="hidden" name="delivery_time" id="delivery_time" value="">
						    					<input type="hidden" name="rep_deliv_default_id" id="rep_deliv_default_id" value=""/>
						    					<input type="hidden" name="r_delete" id="r_delete" value="false">
						    				</td>
						 					<td>
						 					</td>
						    				<td>
						    					<a title='Edit Reports Delivery Timings' >
													<img src="${cpath}/icons/Edit.png" class="button" id="editIcon${index}" name="editIcon" onclick="openEditReportsTimingsDialogBox(this,${sindex})"/>
												</a>
						    				</td>
						    			</tr> 
				    		</c:when>
			    			<c:otherwise>
			    				<c:choose>
				    				<c:when test="${not empty reportsAvailableMap &&  not empty reportsAvailableMap[str]}">
					    				<c:forEach items="${reportsAvailabilitiesList}" var="records" varStatus="st">
					    					<c:choose>
					    						<c:when test="${records.map.day_of_week == sindex}">
									    			<tr style="">
									    				<td><label><fmt:formatDate pattern="HH:mm" value="${records.map.delivery_time}"/></label>
									    					<input type="hidden" name="default_value" id="default_value" value="false">
									    					<input type="hidden" name="day_of_week" id="day_of_week" value="${records.map.day_of_week}">
									    					<input type="hidden" name="delivery_time" id="delivery_time${sindex}" value='<fmt:formatDate pattern="HH:mm" value="${records.map.delivery_time}"/>'>
									    					<input type="hidden" name="rep_deliv_default_id" id="rep_deliv_default_id" value="${records.map.rep_deliv_default_id}"/>
									    					<input type="hidden" name="r_delete" id="r_delete" value="false">
									    				</td>
									    				<td>
											    			<a title="">
																<img src="${cpath}/icons/Delete.png" class="imgDelete button" onclick="deleteItem(this,${sindex})" title='Delete Reports Delivery Timings'/>
															</a>
									    				</td>
									    				<td>
									    					<a title='Delete Reports Delivery Timings' >
															<img src="${cpath}/icons/Edit.png" class="button" id="editIcon${index}" name="editIcon" onclick="openEditReportsTimingsDialogBox(this,${sindex})"/>
									    				</td>
									    			</tr>
									    		</c:when>
									    		<c:otherwise>
									    		</c:otherwise>
									    	</c:choose>
							    		</c:forEach>
							    			 <tr ${style} class="notAvailableRow">
								    				<td><label>&nbsp;</label>
								    					<input type="hidden" name="default_value" id="default_value" value="">
								    					<input type="hidden" name="day_of_week" id="day_of_week" value="">
								    					<input type="hidden" name="delivery_time" id="delivery_time" value="">
								    					<input type="hidden" name="rep_deliv_default_id" id="rep_deliv_default_id" value=""/>
								    					<input type="hidden" name="r_delete" id="r_delete" value="false">
								    				</td>
								    				<td>
								    				</td>
								    				<td>
								    					<a title='Edit Reports Delivery Timings' >
															<img src="${cpath}/icons/Edit.png" class="button" id="editIcon${index}" name="editIcon" onclick="openEditReportsTimingsDialogBox(this,${sindex})"/>
														</a>
								    				</td>
									    	</tr> 
						    		</c:when>
						    		<c:otherwise>
					    				<tr id="defaultRow${sindex}">
						    			<td><label>&nbsp;</label>
						    					<input type="hidden" name="default_value" id="default_value" value="true">
						    					<input type="hidden" name="day_of_week" id="day_of_week" value="${index}">
						    					<input type="hidden" name="delivery_time" id="delivery_time" value="">
						    					<input type="hidden" name="rep_deliv_default_id" id="rep_deliv_default_id" value=""/>
						    					<input type="hidden" name="r_delete" id="r_delete" value="false">
						    				</td>
						 					<td>
						 					</td>
						 					<td></td>
						    				
				    					</tr>
				    					<tr style="display:none">
						    				<td><label>&nbsp;</label>
						    					<input type="hidden" name="default_value" id="default_value" value="">
						    					<input type="hidden" name="day_of_week" id="day_of_week" value="">
						    					<input type="hidden" name="delivery_time" id="delivery_time" value="">
						    					<input type="hidden" name="rep_deliv_default_id" id="rep_deliv_default_id"value=""/>
						    					<input type="hidden" name="r_delete" id="r_delete" value="false">
						    				</td>
						 					<td>
						 					</td>
						    				<td>
						    					<a title='Edit Reports Delivery Timings' >
													<img src="${cpath}/icons/Edit.png" class="button" id="editIcon${index}" name="editIcon" onclick="openEditReportsTimingsDialogBox(this,${sindex})"/>
												</a>
						    				</td>
						    			</tr>
						    		</c:otherwise>						    		
						    	</c:choose>
			    			</c:otherwise>
			    		</c:choose>
		    		</table>
		   			<table class="addButton">
						<tr>
							<td align="right">
								<button type="button" name="btnAddItem" id="btnAddItem${sindex}" title='Add New Record'
										onclick="openReportsTimingsDialog(${sindex});"
										accesskey="+" class="imgButton"><img src="${cpath}/icons/Add.png"></button>
							</td>
						</tr>
					</table>
				</fieldset>
			</div>
			<br/>
			<input type="hidden" name="dialogId" id="dialogId${sindex}">
			<div id="reportsTimingsDialog${sindex}" style="visibility:hidden">
				<div class="hd" id="reportsdialogheader${sindex}">Edit Delivery Report</div>
				<div class="bd">
					<fieldset class="fieldsetborder">
					<legend class="fieldsetlabel">Reports Delivery Timings</legend>
					<table class="formtable" cellpadding="0" cellspacing="0">
						<tr>
							<td class="formlabel">Report Delivery Time (HH:MM):</td>
							<td><input type="text" name="report_time" id="report_time${sindex}" class="number"
							maxlength="5" value="${currenttime}"  />
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
	</fieldset>
	<table class="screenActions">
	<tr>
	<td>
	<input type="submit" name="Save" value="Save" onclick="saveForm();"></td>
		 <c:url var="url" value="reportscenteroverrides.do">
			<c:param name="_method" value="getScreen"/>
			<c:param name="center_id" value="${param.center_id}"/>
		</c:url> 
				<td><a href="<c:out value='${url}' />">&nbsp;Report Delivery Overrides</a></td>
				<td> &nbsp;| <insta:screenlink screenId="mas_centers" extraParam="/show.htm?&center_id=${param.center_id}" label="Back To Center"  addPipe="false"/></td>
		</tr>
	</table>
	<table id="InnerResourceTable"></table>
</form>
<script type="text/javascript">
var cpath = '${pageContext.request.contextPath}';
	var CollapsiblePanel0 = new Spry.Widget.CollapsiblePanel("CollapsiblePanel0", {contentIsOpen:false});
	var CollapsiblePanel1 = new Spry.Widget.CollapsiblePanel("CollapsiblePanel1", {contentIsOpen:false});
	var CollapsiblePanel2 = new Spry.Widget.CollapsiblePanel("CollapsiblePanel2", {contentIsOpen:false});
	var CollapsiblePanel3 = new Spry.Widget.CollapsiblePanel("CollapsiblePanel3", {contentIsOpen:false});
	var CollapsiblePanel4 = new Spry.Widget.CollapsiblePanel("CollapsiblePanel4", {contentIsOpen:false});
	var CollapsiblePanel5 = new Spry.Widget.CollapsiblePanel("CollapsiblePanel5", {contentIsOpen:false});
	var CollapsiblePanel6 = new Spry.Widget.CollapsiblePanel("CollapsiblePanel6", {contentIsOpen:false});

</script>

</body>
</html>
