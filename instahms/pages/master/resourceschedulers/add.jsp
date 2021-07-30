<%@page import="com.insta.hms.master.URLRoute"%>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="pagePath" value="<%=URLRoute.RESOURCE_AVAILABILITY_PATH %>"/>
<c:set var="resOverridePath" value="<%=URLRoute.RESOURCE_AVAILABILITY_OVERRIDE_PATH %>"/>
<%@page import="com.insta.hms.common.Encoder" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="max_centers_inc_default"
	value='<%= GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default") %>'
	scope="request" />
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta name="i18nSupport" content="true"/>
<title><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.title"/></title>
	<script type="text/javascript">
		var pagePath  = '${pagePath}';
		var allResourcesList = ${ifn:convertListToJson(allResourcesList)};
		var resource_type = "${ifn:cleanJavaScript(param.res_sch_type)}";
		var max_centers_inc_default = ${max_centers_inc_default};
		var gResourceType = "${ifn:cleanJavaScript(param.res_sch_type)}";
		var centerId = ${centerId};
		var resourceName = '${ifn:cleanJavaScript(param.res_sch_name)}';
		var doctorJSON = ${ifn:convertListToJson(DoctorsJSON)};
	</script>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="ajax.js"/>
	<insta:link type="script" file="resourcescheduler/schedulerdashboard.js"/>
	<insta:link file="SpryAssets/SpryCollapsiblePanel.js" type="js"/>
	<insta:link path="scripts/SpryAssets/SpryCollapsiblePanel.css" type="css" />
	<insta:link type="script" file="resourcescheduler/ResourceCommon.js"/>
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
<body  onload="showResourceName('${ifn:cleanJavaScript(param.res_sch_type)}','load','${not empty param.schedulerName ? param.schedulerName : bean.map.res_sch_name}','${not empty dept_category ? param.dept_category : '' }'),initDialog();">
<c:set var="dummyvalue">
	<insta:ltext key="selectdb.dummy.value"/>
</c:set>
<c:set var="actionUrl" value="create.htm"/>
<form action="${actionUrl}" method="POST" name="resourceAvailableForm" autocomplete="off">
<input type="hidden" name="login_center_id" id="login_center_id" value="<%=(Integer) session.getAttribute("centerId") %>"/>
<input type="hidden" name="login_center_name" id="login_center_name" value="<%=Encoder.cleanHtmlAttribute((String) session.getAttribute("centerName")) %>"/>
<input type="hidden" name="res_sch_name" value="${ifn:cleanHtmlAttribute(param.res_sch_name)}">
<input type="hidden" name="method" value="create">
	<jsp:useBean id="daysMap" class="java.util.HashMap"/>
		<c:set target="${daysMap}" property="0" value= "Sunday" />
		<c:set target="${daysMap}" property="1" value= "Monday" />
		<c:set target="${daysMap}" property="2" value= "Tuesday" />
		<c:set target="${daysMap}" property="3" value= "Wednesday" />
		<c:set target="${daysMap}" property="4" value= "Thursday" />
		<c:set target="${daysMap}" property="5" value= "Friday" />
		<c:set target="${daysMap}" property="6" value= "Saturday" />
	
	<input type="hidden" name="res_sch_name" id="res_sch_name" value="${ifn:cleanHtmlAttribute(param.res_sch_name)}"/>
	<input type="hidden" name="category_default_duration" id="category_default_duration" value="${not empty param.category_default_duration ? category_default_duration : category_default_duration}">
<c:set var="addText">
	<insta:ltext key="patient.resourcescheduler.common.add"/>
</c:set>
	<h1>${addText} <insta:ltext key="patient.resourcescheduler.doctortimingdashboard.h1"/></h1>

	<insta:feedback-panel/>
	<fieldset class="fieldSetBorder">
	<table class="formtable">
		<tr>
			<td class="formlabel"><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.scheduler"/></td>
			<td>
				<select name="res_sch_type" onchange="submitForm()" class="dropdown" id="res_sch_type">
					<option value="">${dummyvalue}</option>
					<option value="DOC" ${param.res_sch_type == 'DOC' ? 'selected' : ''}><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.doctorconsult"/></option>
						<c:forEach var="rec" items="${categoryDescripton}" varStatus="index">
							<option value="${rec.map.res_sch_type}" ${param.res_sch_type == rec.map.res_sch_type  ? 'selected' : ''}>${rec.map.resource_description}</option>
						</c:forEach>
				</select>
			</td>
				
			<td class="formlabel"><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.description"/></td>
			<td><input type="text" name="description" value="${bean.map.description}"  maxlength="200" /></td>
			<c:choose>
				<c:when test="${(bean.map.res_sch_name == '*')}">
					<td class="formlabel"><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.status"/>:</td>
					<td class="forminfo">${bean.map.status == 'A' ? 'Active' : 'Inactive'}
					<input type="hidden"  name="status" value="${bean.map.status}"/>
					</td>
				</c:when>
				<c:otherwise>
					<td class="formlabel"><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.status"/>:</td>
					<td><insta:selectoptions name="status" value="${bean.map.status}" opvalues="A,I" optexts="Active, Inactive"/></td>
				</c:otherwise>
			</c:choose>
		</tr>
		<tr>
			<c:if test="${(resource_type == 'DOC' || param.res_sch_type == 'DOC')}">
				<td class="formlabel"><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.department"/></td><td><insta:selectdb  name="dept" value="${not empty param.dept ? param.dept : bean.map.dept}" table="department"
				 valuecol="dept_id" displaycol="dept_name" dummyvalue="${dummyvalue}"  orderby="dept_name" class="dropdown"
				 onchange="showResourceName('${param.resource_type}','edit')"/></td>
				 <input type="hidden"  name="dept1" id="dept1" value=""/>
			</c:if>
			<c:if test="${(resource_type == 'SUR' || param.res_sch_type == 'SUR')}">
				<td class="formlabel"><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.department"/></td><td><insta:selectdb  name="dept" value="${not empty param.dept ? param.dept : bean.map.dept}" table="department"
				 valuecol="dept_id" displaycol="dept_name" dummyvalue="${dummyvalue}" orderby="dept_name" class="dropdown"
				 onchange="showResourceName('${param.resource_type}','edit')"/></td>
				  <input type="hidden"  name="dept1" id="dept1" value=""/>
			</c:if>
			<c:if test="${(resource_type == 'SER' || param.res_sch_type == 'SER')}">
				<td class="formlabel"><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.department"/></td><td><insta:selectdb  name="dept" value="${not empty param.dept ? param.dept : bean.map.dept}" table="services_departments"
				 valuecol="serv_dept_id" displaycol="department" dummyvalue="${dummyvalue}" orderby="department" class="dropdown"
				 onchange="showResourceName('${param.resource_type}','edit')"/></td>
				  <input type="hidden"  name="dept1" id="dept1" value=""/>
			</c:if>
			<c:if test="${(resource_type == 'TST' || param.res_sch_type == 'TST')}">
				<td class="formlabel"><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.department"/></td>
				<td><insta:selectdb  name="dept" value="${not empty param.dept ? param.dept : bean.map.dept}" table="diagnostics_departments"
					 valuecol="ddept_id" displaycol="ddept_name" dummyvalue="${dummyvalue}" orderby="ddept_name" class="dropdown"
					 onchange="showResourceName('${param.resource_type}','edit')"/></td>
					  <input type="hidden"  name="dept1" id="dept1" value=""/>
			</c:if>

			<td class="formlabel">
				<c:choose>
					<c:when test="${param.res_sch_type=='DOC'}">
						<insta:ltext key="patient.resourcescheduler.doctortimingdashboard.doctorconsult"/>:
					</c:when>
					<c:otherwise>
						<c:forEach var="record" items="${categoryDescripton}">
							<c:if test="${param.res_sch_type == record.map.res_sch_type}">${record.map.resource_description}:</c:if>
						</c:forEach>
					</c:otherwise>
				</c:choose>
			</td>
			<c:if test="${(not empty  param.res_sch_type)}">
				<td>
					<div class="sboFieldInput" id="resourceAutoComplete" >
						<input type="text" name="_resource_name" id="_resource_name">
						<input type="hidden" name="schedulerName" id="schedulerName"/>
						<div id="resourceNameContainer"></div>
					</div>
				</td>
			</c:if>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.slotduration"/></td>
			<td>
			<c:choose>
				<c:when test="${(param.method == 'show') && (bean.map.res_sch_name =='*')}">
					<input type="text"  value="${category_default_duration}" readonly style="width:60px"/>&nbsp;<insta:ltext key="patient.resourcescheduler.doctortimingdashboard.minutes"/>
					<input type="hidden"  name="duration" value="${bean.map.default_duration}"/>
				</c:when>
				<c:when test="${empty param.category_default_duration}">
					<select class="dropdown" name="duration"><option value="">${dummyvalue}</option></select>
				</c:when>
				<c:otherwise>
					<c:set var="durText" value=""/>
					<c:set var="durValue" value=""/>
					<c:set var="j" value="1"/>
					<c:forEach var="i" begin="${category_default_duration}" end="${category_default_duration*10}" step="${category_default_duration*j}" >
							<c:set var="durText" value="${durText},${i}"/>
							<c:set var="durValue" value="${durValue},${i}"/>
							<c:set var="j" value="${j+1}"/>
					</c:forEach>
					<c:choose>
						<c:when test="${param.method == 'show'}">
							<insta:selectoptions name="default_duration" optexts="${durText}" style="width:5em" disabled="true"
							opvalues="${durValue}" value="${not empty bean.map.default_duration ? bean.map.default_duration : category_default_duration}"/> <insta:ltext key="patient.resourcescheduler.doctortimingdashboard.minutes"/>
							<input type="hidden"  name="duration" value="${not empty bean.map.default_duration ? bean.map.default_duration : category_default_duration}"/>
						</c:when>
						<c:otherwise>
							<insta:selectoptions name="duration" optexts="${durText}" style="width:5em"
							opvalues="${durValue}" value="${not empty param.duration ? param.duration : category_default_duration}" onchange="onchangeDuration()"/> <insta:ltext key="patient.resourcescheduler.doctortimingdashboard.minutes"/>
						</c:otherwise>
					</c:choose>
				</c:otherwise>
			</c:choose>
			</td>
			<c:if test="${bean.map.res_sch_name == '*'}">
				<td class="formlabel"><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.height"/></td>
				<td>
					<input type="text" name="height_in_px" value="${bean.map.height_in_px}"
						onkeypress="return enterNumOnly(event)"  class="number" maxlength="3"/><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.inpx"/>
				</td>
			</c:if>
		</tr>
	</table>
	</fieldset>
    <c:set var="style" value=""/>
    <c:if test="${empty resourceAvailabilitiesList}">
    	<c:set var="style" value='style="display:none"'/>
    </c:if>
    <fieldset class="fieldsetborder">
    <legend class="fieldsetlabel"><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.resourceavailability.details"/></legend>
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
			    	<c:when test="${not empty resourceAvailableMap &&  not empty resourceAvailableMap[str]}">
			    		<c:set var="style" value='style="display:none"'/>
			    	</c:when>
			    	<c:otherwise>
			    			<c:set var="style" value=""/>
			    	</c:otherwise>
			    </c:choose>
				<fieldset class="fieldSetBorder" id="fieldset${sindex}">
		    		<table class="detailList" cellpadding="0" cellspacing="0" id="resultTable${sindex}">
		    			<tr>
		    				<br/>
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
		    			<c:choose>
			    			<c:when test="${empty resourceAvailabilitiesList}">
		    					<c:if test="${rec.map.day_of_week != sindex}">
					    			<tr id="defaultRow${sindex}" class="notAvailableRow">
					    				<td><label><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.0"/></label>
					    					<input type="hidden" name="default_value" id="default_value" value="true">
					    					<input type="hidden" name="day_of_week" id="day_of_week" value="${sindex}">
					    					<input type="hidden" name="from_time" id="from_time" value="00:00">
					    				</td>
					    				<td><label><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.23"/></label>
					    					<input type="hidden" name="to_time" id="to_time" value="23:59">
					    				</td>
					    				<td><label><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.notavailable"/></label>
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
										</td>
					    				<td><label>&nbsp;</label>
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

						    				<td><label>&nbsp;</label>
												<input type="hidden" name="r_delete" id="r_delete" value="">
											</td>
						    				<td>
						    					<a title='<insta:ltext key="patient.resourcescheduler.doctortimingdashboard.edit.resourcetmngs"/>' >
													<img src="${cpath}/icons/Edit.png" class="button" id="editIcon${ifn:cleanHtmlAttribute(index)}" name="editIcon" onclick="openEditResTimingsDialogBox(this,${sindex})"/>
												</a>
						    				</td>
						    			</tr>
				    			</c:if>
				    		</c:when>
			    			<c:otherwise>
			    				<c:choose>
				    				<c:when test="${not empty resourceAvailableMap &&  not empty resourceAvailableMap[str]}">
					    				<c:forEach items="${resourceAvailabilitiesList}" var="records" varStatus="st">
					    					<c:choose>
					    						<c:when test="${records.map.day_of_week == sindex}">
									    			<tr style=""  class="${records.map.availability_status == 'N' ? 'notAvailableRow': ''}">
									    				<td><label><fmt:formatDate pattern="HH:mm" value="${records.map.from_time}"/></label>
									    					<input type="hidden" name="default_value" id="default_value" value="false">
									    					<input type="hidden" name="day_of_week" id="day_of_week" value="${sindex}">
									    					<input type="hidden" name="from_time" id="from_time" value='<fmt:formatDate pattern="HH:mm" value="${records.map.from_time}"/>'>
									    				</td>
									    				<td><label><fmt:formatDate pattern="HH:mm" value="${records.map.to_time}"/></label>
									    					<input type="hidden" name="to_time" id="to_time" value='<fmt:formatDate pattern="HH:mm" value="${records.map.to_time}"/>'>
									    				</td>
									    				<td><label>${records.map.availability_status == 'A' ? 'Available' : 'Not Available'}</label>
									    					<input type="hidden" name="availability_status" id="availability_status" value="${records.map.availability_status}">
									    				</td>
									    				<c:if test="${max_centers_inc_default > 1 && param.res_sch_type == 'DOC'}">
					    									<td>
										    					<c:if test="${records.map.availability_status == 'A'}">
										    						<label>${records.map.center_name}</label>
										    					</c:if>
									    						<input type="hidden" name="center_name" id="center_name" value="${records.map.center_name}">
									    						<input type="hidden" name="center_id" id="center_id" value="${records.map.center_id}">
									    					</td>
														</c:if>
														<c:if test="${param.res_sch_type == 'DOC'}">
															<td><label> 
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
									    				<td><label><insta:truncLabel value="${records.map.remarks}" length="50"/></label>
									    					<input type="hidden" name="remarks" id="remarks" value="${records.map.remarks}">
									    					<input type="hidden" name="r_delete" id="r_delete" value="false" onclick="deleteItem(this,${sindex})">
														</td>
									    				<td>
									    					<c:choose>
									    						<c:when test="${param.res_sch_type == 'DOC' && (centerId == 0 || centerId == records.map.center_id)}">
																		<c:if test="${records.map.availability_status == 'A'}">
											    							<a title="">
																				<img src="${cpath}/icons/Delete.png" class="imgDelete button" onclick="deleteItem(this,${sindex})" title='<insta:ltext key="patient.resourcescheduler.editresourceavailability.delete.resource"/>'/>
																			</a>
																		</c:if>
																</c:when>
									    						<c:when test="${param.res_sch_type != 'DOC' && records.map.availability_status == 'A'}">
											    						<a title="">
																			<img src="${cpath}/icons/Delete.png" class="imgDelete button" onclick="deleteItem(this,${sindex})" title='<insta:ltext key="patient.resourcescheduler.editresourceavailability.delete.resource"/>'/>
																		</a>
																</c:when>	
																<c:otherwise>
																	<a title="">
																		<img src="${cpath}/icons/Delete1.png" class="imgDelete button" onclick=""/>
																	</a>
																</c:otherwise>
															</c:choose>
									    				</td>
									    				<td>
									    					<a title='<insta:ltext key="patient.resourcescheduler.doctortimingdashboard.edit.resourcetmngs"/>' >
															<img src="${cpath}/icons/Edit.png" class="button" id="editIcon${ifn:cleanHtmlAttribute(index)}" name="editIcon" onclick="openEditResTimingsDialogBox(this,${sindex})"/>
									    				</td>
									    			</tr>
									    		</c:when>
									    		<c:otherwise>
									    		</c:otherwise>
									    	</c:choose>
							    		</c:forEach>
							    			<tr ${style}>
								    				<td><label>&nbsp;</label>
								    					<input type="hidden" name="default_value" id="default_value" value="">
								    					<input type="hidden" name="day_of_week" id="day_of_week" value="">
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
								    				<td><label>&nbsp;</label>
														<input type="hidden" name="r_delete" id="r_delete" value="" onclick="deleteItem(this,${sindex})">
													</td>
								    				<td>
								    					<a title='<insta:ltext key="patient.resourcescheduler.doctortimingdashboard.edit.resourcetmngs"/>' >
															<img src="${cpath}/icons/Edit.png" class="button" id="editIcon${ifn:cleanHtmlAttribute(index)}" name="editIcon" onclick="openEditResTimingsDialogBox(this,${sindex})"/>
														</a>
								    				</td>
									    	</tr>
						    		</c:when>
						    		<c:otherwise>
					    				<tr id="defaultRow${sindex}" class="notAvailableRow">
						    				<td><label><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.0"/></label>
						    					<input type="hidden" name="default_value" id="default_value" value="true">
						    					<input type="hidden" name="day_of_week" id="day_of_week" value="${sindex}">
						    					<input type="hidden" name="from_time" id="from_time" value="00:00">
						    				</td>
						    				<td><label><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.23"/></label>
						    					<input type="hidden" name="to_time" id="to_time" value="23:59">
						    				</td>
						    				<td><label><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.notavailable"/></label>
						    					<input type="hidden" name="availability_status" id="availability_status" value="N">
						    				</td>
						    				<c:if test="${max_centers_inc_default > 1 && param.res_sch_type == 'DOC'}">
					    						<td>
										    	
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
						    				<td><label>&nbsp;</label>
												<input type="hidden" name="r_delete" id="r_delete" value="false" onclick="deleteItem(this,${sindex})">
											</td>
						    				<td>
						    					<a title='<insta:ltext key="patient.resourcescheduler.doctortimingdashboard.edit.resourcetmngs"/>' >
													<img src="${cpath}/icons/Edit.png" class="button" id="editIcon${ifn:cleanHtmlAttribute(index)}" name="editIcon" onclick="openEditResTimingsDialogBox(this,${sindex})"/>
												</a>
						    				</td>
				    					</tr>
				    					<tr style="display:none">
						    				<td><label>&nbsp;</label>
						    					<input type="hidden" name="default_value" id="default_value" value="">
						    					<input type="hidden" name="day_of_week" id="day_of_week" value="">
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
						    				<td><label>&nbsp;</label>
												<input type="hidden" name="r_delete" id="r_delete" value="">
											</td>
						    				<td>
						    					<a title='<insta:ltext key="patient.resourcescheduler.doctortimingdashboard.edit.resourcetmngs"/>' >
													<img src="${cpath}/icons/Edit.png" class="button" id="editIcon${ifn:cleanHtmlAttribute(index)}" name="editIcon" onclick="openEditResTimingsDialogBox(this,${sindex})"/>
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
								<button type="button" name="btnAddItem" id="btnAddItem${sindex}" title='<insta:ltext key="patient.resourcescheduler.doctortimingdashboard.add.newrecord"/>'
										onclick="openResourceTimingsDialog(${sindex});"
										accesskey="+" class="imgButton"><img src="${cpath}/icons/Add.png"></button>
							</td>
						</tr>
					</table>
				</fieldset>
			</div>
			<br/>
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
						<!-- -----------show center drop down---------- -->
						<c:if test="${max_centers_inc_default > 1 && param.res_sch_type == 'DOC'}">
								<td class="formlabel"><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.center"/>:</td>
								<td>
									<select name="dialog_center_name" id="dialog_center_name${sindex}" class="dropdown" onchange="fillHidenCeterName(this, ${sindex})"/></select>
									<label id="dialog_center_label${sindex}"></label>
									<input type="hidden" name="dialog_center_name_hid" id="dialog_center_name_hid${sindex}" value="">
								</td>
						</c:if>
							<!-------------show visit mode dropdown------------>
							<c:if test="${param.res_sch_type == 'DOC'}">
								<td class="formlabel"><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.visitMode"/>:</td>
								<td>
									<select name="dialog_visit_mode" id="dialog_visit_mode${sindex}" class="dropdown" onchange="fillVisitMode(this,${sindex})">
										<option value="I" selected>In Person</option>
										<option value="B">Both</option>
										<option value="O">Online</option>
									</select>
								</td>
							</c:if>
						</tr>
						<tr>
							<td class="formlabel"><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.remarks"/>:</td>
							<td colspan="2">
								<textarea rows="2" cols="20" id="dialog_remarks${sindex}" onblur="checkLength(this,200,'Remarks')"></textarea>
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
	<c:if test="${(not empty param.res_sch_type && (param.res_sch_type == 'DOC' || param.res_sch_type == 'TST' || param.res_sch_type == 'SER' || param.res_sch_type == 'SUR')) && (bean.map.res_sch_name != '*') }">
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel"><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.selectresources"/></legend>
			<jsp:include page="SchedulerResourceDialog.jsp"/>
		</fieldset>
    </c:if>
	<table class="screenActions">
		<c:url var="url" value="${resOverridePath}/list.htm">
			<c:param name="method" value="list"/>
			<c:param name="res_sch_type" value="${param.res_sch_type}"/>
			<c:param name="res_sch_name" value="${param.res_sch_name}"/>
		</c:url>
		<tr>
			<td> 
			<insta:accessbutton buttonkey="patient.resourcescheduler.doctortimingdashboard.save" type="submit" onclick="return innerResourcesTable();" id="savebtn"/>
			</td>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="javascript:void(0)" onclick="doClose();return true;"><insta:ltext key="patient.resourcescheduler.doctortimingdashboard.defaultlist"/></a></td>
		</tr>
	</table>

	<table id="InnerResourceTable"></table>

</form>
<script type="text/javascript">
function onchangeDuration() {
	var category = document.forms[0].res_sch_type.value;
	var duration = document.forms[0].duration.value;
	var dept = "";
	if(document.forms[0].dept)
		dept = document.forms[0].dept.value;
	var schedulerName = document.forms[0].schedulerName.value
	window.location.href = "../../master/resourceschedulers/add.htm?method=add&res_sch_type="+category+"&duration="+duration+"&category_default_duration="+duration+"&dept="+dept+"&schedulerName="+schedulerName+"&resource_type=primary";
}

var cpath = '${pageContext.request.contextPath}';
<c:if test="${not empty param.res_sch_type}">
	var allResourcesList = ${ifn:convertListToJson(allResourcesList)};
	var doctorJSON = ${ifn:convertListToJson(DoctorsJSON)};
	var scheduleDoctorJSON = filterList(doctorJSON,"schedule","T");
	var surgeonsJSON = filterList(doctorJSON,"ot_doctor_flag","Y");
	var anesthestistsJSON = filterList(doctorJSON,"dept_id","DEP0002");
	var resourceTypeJSON = ${ifn:convertListToJson(resourceTypeListJSON)};
	var resourceTypeFilteredJSON =  filterList(resourceTypeJSON,"category",'${ifn:cleanJavaScript(category)}');
	var theatresJSON = ${ifn:convertListToJson(TheatresJSON)};
	var equipmentsJSON = ${ifn:convertListToJson(EquipmentsJSON)};
	var serviceResourcesListJson = ${ifn:convertListToJson(serviceResourcesListJson)};
	var genericResourceListJson = ${ifn:convertListToJson(genericResourceListJson)};
	
	<c:if test="${param.res_sch_type == 'SUR' || param.res_sch_type == 'THID'}">
		var operationsJSON = ${ifn:convertListToJson(OperationsJSON)};
	</c:if>
	<c:if test="${param.res_sch_type == 'SER' || param.res_sch_type == 'SRID'}">
		var servicesJSON = ${ifn:convertListToJson(ServicesJSON)};
	</c:if>
	<c:if test="${param.res_sch_type == 'TST' || param.res_sch_type == 'EQID'}">
		var testsJSON = ${ifn:convertListToJson(TestsJSON)};
		var labTechniciansJSON = ${ifn:convertListToJson(LabTechniciansJSON)};
	</c:if>
	<c:if test="${param.res_sch_type == 'BED'}">
		var bedsJSON = ${ifn:convertListToJson(BedsJSON)};
	</c:if>
	var resourceBeanJSON = ${ifn:convertListToJson(ResourceBeanJSON)};
	<c:if test="${(param.res_sch_type == 'SUR' || param.res_sch_type == 'DOC' || param.res_sch_type == 'SER' || param.res_sch_type == 'TST') && (empty param.res_sch_name || param.res_sch_name != '*')}">
		if(resourceBeanJSON != null){
			showAllResources();
		}else{
			addResourceRow();
		}
	</c:if>
</c:if>
	
	var centersJSON=${ifn:convertListToJson(centersJSON)};
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
