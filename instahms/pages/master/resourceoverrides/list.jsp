<%@page import="com.insta.hms.master.URLRoute"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.common.Encoder" %>
<c:set var="pagePath" value="<%=URLRoute.RESOURCE_AVAILABILITY_OVERRIDE_PATH %>"/>
<c:set var="resAvailPath" value="<%=URLRoute.RESOURCE_AVAILABILITY_OVERRIDE_PATH %>"/>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="max_centers_inc_default"
	value='<%= GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default") %>'
	scope="request" />
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta name="i18nSupport" content="true"/>
<title><insta:ltext key="patient.resourcescheduler.resourceavailabilitylist.title"/></title>
	<script> 
		var resourceType = '${ifn:cleanJavaScript(param.res_sch_type)}'; 
		var gResourceType = '${ifn:cleanJavaScript(param.res_sch_type)}';
		var max_centers_inc_default = '${max_centers_inc_default}';
	</script>
	<insta:js-bundle prefix="scheduler.resourceavailability"/>

	<script>
		var toolbarOptions = getToolbarBundle("js.scheduler.resourceavailability.toolbar");
	</script>
	<script>
		var doctorJSON = ${ifn:convertListToJson(DoctorsJSON)};
		var allResourcesList = ${ifn:convertListToJson(allResourcesList)};
		var oAutoComp = null;
		var resourceName = '${ifn:cleanJavaScript(param.res_sch_name)}';
		var method = '${ifn:cleanJavaScript(param.method)}';
		var toolbar = {
				Edit: {
					title: toolbarOptions["editoverride"]["name"],
					imageSrc: "icons/Edit.png",
					href: '${pagePath}/show.htm?method=show',
					onclick: null,
					description: ""
					},
				Delete: {
					title: toolbarOptions["deleteoverride"]["name"],
					imageSrc: "icons/Edit.png",
					href: '${pagePath}/deleteResourceAvailability.htm?method=list',
					onclick: null,
					description: ""
					
				}
			};
		
	</script>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="ajax.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="script" file="resourcescheduler/resourceavailability.js"/>
<insta:js-bundle prefix="scheduler.doctornonavailability"/>
<insta:js-bundle prefix="scheduler.resourceavailability"/>
<insta:js-bundle prefix="scheduler.doctorscheduler"/>
<insta:js-bundle prefix="scheduler.todaysappointment"/>
<insta:js-bundle prefix="scheduler.schedulerdashboard"/>
<insta:js-bundle prefix="scheduler.portalappointment"/>

</head>
<body onload="init('list')">

	<c:set var="hasResults" value="${not empty pagedList.dtoList}"></c:set>
	<c:set var="loggedInCenter" value='<%=(Integer) session.getAttribute("centerId") %>'></c:set>
	<h1><insta:ltext key="patient.resourcescheduler.resourceavailabilitylist.h1"/></h1>
	<insta:feedback-panel/>

	<form name="CategorySearchForm" method="GET">

	<input type="hidden" name="method" value="list"/>
	<input type="hidden" name="_searchMethod" value="list"/>
	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>
	<input type="hidden" name="_referrer" value="${ifn:cleanHtmlAttribute(param._referrer)}"/>
	<input type="hidden" name="_screenName" value="${ifn:cleanHtmlAttribute(param._screenName)}"/>
	<input type="hidden" name="login_center_id" id="login_center_id" value="<%=(Integer) session.getAttribute("centerId") %>"/>
	<input type="hidden" name="login_center_name" id="login_center_name" value="<%=Encoder.cleanHtmlAttribute((String) session.getAttribute("centerName")) %>"/>
		<insta:search-lessoptions form="CategorySearchForm"
			validateFunction="return validateResourceForm() ">

			<div class="searchBasicOpts">
				<div class="sboField" style="height: 69px">
					<div class="sboFieldLabel">
						<insta:ltext
							key="patient.resourcescheduler.resourceavailabilitylist.resourcetype" />
					</div>
					<div class="sboFieldInput">
						<select name="res_sch_type" id="res_sch_type" class="dropdown"
							onchange="loadResources(this)">
							<option value=""><insta:ltext
									key="patient.resourcescheduler.resourceavailabilitylist.select" /></option>
							<option value="DOC"
								${param.res_sch_type == 'DOC' ? 'selected' : ''}><insta:ltext
									key="patient.resourcescheduler.resourceavailabilitylist.doctorconsultation" /></option>
							<c:forEach var="rec" items="${categoryDescripton}"
								varStatus="index">
								<option value="${rec.res_sch_type}"
									${param.res_sch_type == rec.res_sch_type  ? 'selected' : ''}>${rec.resource_description}</option>
							</c:forEach>
						</select> <input type="hidden" name="res_sch_type@op" value="ico" />
					</div>
				</div>
				<div class="sboField">
					<div class="sboFieldLabel">
						<insta:ltext
							key="patient.resourcescheduler.resourceavailabilitylist.resource" />
					</div>
					<div class="sboFieldInput" id="resourceAutoComplete">
						<input type="text" name="_resource_name" id="_resource_name"
							class="autocomplete"
							value="${ifn:cleanHtmlAttribute(param._resource_name)}">
						<input type="hidden" name="res_sch_name@op" value="in" /> <input
							type="hidden" name="res_sch_name" id="res_sch_name" />

						<div id="resourceNameContainer" style="width: 900px"></div>
					</div>
				</div>
				<div>
					<div style="float: left; padding: 12px 2px;">
					<insta:ltext
							key="patient.dialysis.prescriptions.from" />
					</div>
					<div class="sboFieldInput" style="float: left; padding: 10px 2px">
						<insta:datewidget style="width: 70px" name="availability_date"
							id="from_date" class="timefield"
							value="${paramValues.availability_date[0]}" />
					</div>
					<div style="float: left; padding: 12px 2px;">
					<insta:ltext
							key="patient.dialysis.prescriptions.to" />
					</div>
					<div class="sboFieldInput" style="float: left; padding: 10px 2px">
						<insta:datewidget style="width: 70px" name="availability_date"
							id="to_date" class="timefield"
							value="${paramValues.availability_date[1]}" />
						<input type="hidden" name="availability_date@op" value="between" />
					</div>
				</div>
			</div>
		</insta:search-lessoptions>

		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	<div class="resultList">
		<fieldset class="fieldsetborder">
    	<legend class="fieldsetlabel"><insta:ltext key="patient.resourcescheduler.resourceavailabilitylist.overrides"/></legend>
		<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
			<tr onmouseover="hideToolBar();">
					<tr>
						<th style="padding-top: 0px;padding-bottom: 0px">
						<input type="checkbox" name="_checkAllForDelete" onclick="return checkOrUncheckAll('_cancelResourceAvailability', this)"/>
					</th>
					<th><insta:ltext key="patient.resourcescheduler.resourceavailabilitylist.availabilitydate"/></th>
					<th><insta:ltext key="patient.resourcescheduler.schedulerresourcedialog.resourcetype"/></th>
					<th><insta:ltext key="patient.resourcescheduler.schedulerresourcedialog.resource"/></th>
					<th>&nbsp;</th>
					<th>&nbsp;</th>
				</tr>
			</tr>

			<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st" >

				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{res_sch_name: '${record.res_sch_name}',res_avail_id : '${record.res_avail_id}', res_sch_type :'${record.res_sch_type}'},'');" id="toolbarRow${st.index}">
						<td>
							<input type="checkbox" name="_cancelResourceAvailability" id="_cancelResourceAvailability" value="${record.res_avail_id}"/>
							<input type="hidden" name="login_center_id" id="login_center_id" value="<%=(Integer) session.getAttribute("centerId") %>"/>
							<input type="hidden" name="login_center_name" id="login_center_name" value="<%=Encoder.cleanHtmlAttribute((String) session.getAttribute("centerName")) %>"/>
							
						</td>
						<td>
							<fmt:formatDate pattern="dd-MM-yyyy" value="${record.availability_date}" />
						</td>
							<td><c:if test="${record.res_sch_type == 'DOC'}">
									<insta:ltext
										key="patient.resourcescheduler.resourceavailabilitylist.doctorconsultation" />
								</c:if> <c:forEach var="reca" items="${categoryDescripton}"
									varStatus="index">
									<c:if test="${record.res_sch_type == reca.res_sch_type}"> ${reca.resource_description }	 	
						</c:if>
								</c:forEach></td>
						<td>
						${record.resource_name}
						</td>	
							<td>&nbsp;</td>
						<td>&nbsp;</td>
			</c:forEach>
		</table>
		<c:if test="${param.method == 'list'}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>
		</fieldset>
	</div>
	<c:url var="url" value="${pagePath}/add.htm">
			<c:param name="method" value="add"/>
			<c:param name="res_sch_type" value="${param.res_sch_type}"/>
			<c:param name="res_sch_name" value="${param.res_sch_name}"/>
	</c:url>
	<c:url var="url1" value="${resAvailPath}/showDefaultTimings.htm">
		<c:param name="res_sch_type" value="${param.res_sch_type}"/>
		<c:param name="res_sch_name" value="${param.res_sch_name}"/>
	</c:url>
	<c:url var="url2" value="${pagePath}/addbulk.htm">
			<c:param name="method" value="addbulk"/>
			<c:param name="res_sch_type" value="${param.res_sch_type}"/>
			<c:param name="res_sch_name" value=""/>
			<c:param name="center_id" value="${loggedInCenter}"/>
	</c:url>
	<table class="screenActions">
		<tr>
			<c:if test="${not empty pagedList.dtoList}">
				<td colspan="2">
					<insta:accessbutton buttonkey="patient.resourcescheduler.resourceavailabilitylist.del" name="ConfirmedAppointment" type="button" onclick="deleteAllSelectedRows();" />
				</td>
			</c:if>
			<td>&nbsp;</td>
			<td>
				<a href="<c:out value='${url}'/>"><insta:ltext key="patient.resourcescheduler.resourceavailabilitylist.addnew"/></a>
			</td>
			<td>&nbsp;|&nbsp;</td>
			<td>
				<a href="<c:out value='${url2}'/>"><insta:ltext key="patient.resourcescheduler.resourceavailabilitylist.addnew.bulk"/></a>
			</td>
			<c:if test="${not empty pagedList.dtoList && not empty param.res_sch_name}">
				<td>&nbsp;|&nbsp;</td>
				<td>
					<a href="<c:out value='${url1}'/>"><insta:ltext key="patient.resourcescheduler.resourceavailabilitylist.defaulttimings"/></a>
				</td>
			</c:if>
			<c:if test="${not empty param._referrer && not empty param._screenName}">
				<td>&nbsp;|&nbsp;</td>
				<td>
					<a href="<c:out value='${param._referrer}'/>"><insta:ltext key="patient.resourcescheduler.resourceavailabilitylist.backtoscheduler"/></a>
				</td>
			</c:if>
		</tr>
	</table>
</form>
</body>
</html>
