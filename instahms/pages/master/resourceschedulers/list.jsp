<%@page import="com.insta.hms.master.URLRoute"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="pagePath" value="<%=URLRoute.RESOURCE_AVAILABILITY_PATH %>"/>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta name="i18nSupport" content="true"/>
<title><insta:ltext key="patient.resourcescheduler.schedulerdashboard.title"/></title>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="ajax.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="js" file="dashboardLookup.js"/>

	<insta:js-bundle prefix="scheduler.schedulerdashboard"/>

	<script>
		var toolbarOptions = getToolbarBundle("js.scheduler.schedulerdashboard.toolbar");
	</script>
	<script>
		var toolbar = {
		Edit: {
			title: toolbarOptions["editvisit"]["name"],
			imageSrc: "icons/Edit.png",
			href: '${pagePath}/show.htm?method=show',
			onclick: null,
			description: toolbarOptions["editvisit"]["description"]
			}
	};

	function init()	{
		createToolbar(toolbar);
		showFilterActive(document.CategorySearchForm);
	}
	</script>
<insta:js-bundle prefix="scheduler.doctornonavailability"/>
<insta:js-bundle prefix="scheduler.resourceavailability"/>
<insta:js-bundle prefix="scheduler.doctorscheduler"/>
<insta:js-bundle prefix="scheduler.todaysappointment"/>
<insta:js-bundle prefix="scheduler.portalappointment"/>
</head>
<body onload="init()">
<c:set var="statusOptions">
	<insta:ltext key="patient.resourcescheduler.schedulerdashboard.active"/> ,
	<insta:ltext key="patient.resourcescheduler.schedulerdashboard.inactive"/>
</c:set>
<c:set var="resourcetype">
 <insta:ltext key="patient.resourcescheduler.schedulerdashboard.resourcetype"/>
</c:set>
<c:set var="appointmentitem">
 <insta:ltext key="patient.resourcescheduler.schedulerdashboard.appointmentitem"/>
</c:set>
<c:set var="dept">
 <insta:ltext key="patient.resourcescheduler.schedulerdashboard.department"/>
</c:set>
	<c:set var="hasResults" value="${empty pagedList.dtoList ? false : true}"></c:set>

	<h1><insta:ltext key="patient.resourcescheduler.schedulerdashboard.h1"/></h1>
	<insta:feedback-panel/>

	<form name="CategorySearchForm" method="GET">

	<input type="hidden" name="_method" value="list"/>
	<input type="hidden" name="method" value="list"/>
	<input type="hidden" name="_searchMethod" value="list"/>
	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>
	<c:set var="categoryMap" value="DOC"/>
	<c:forEach var="resource" items="${categoryDescripton}">
		<c:set  value="${categoryMap},${resource.res_sch_type}" var="categoryMap"/>
	</c:forEach>
	<c:set var="schedulerMap" value="Doctor Consultation"/>
	<c:forEach var="resource" items="${categoryDescripton}">
		<c:set  value="${schedulerMap},${resource.resource_description}" var="schedulerMap"/>
	</c:forEach>

	<insta:find form="CategorySearchForm" optionsId="optionalFilter" closed="${hasResults}">
		<div class="searchBasicOpts" >
			<div class="sboField">
				<div class="sboFieldLabel"><insta:ltext key="patient.resourcescheduler.schedulerdashboard.appointmentitem"/>:</div>
				<div class="sboFieldInput">
					<input type="text" name="resource_name" value="${ifn:cleanHtmlAttribute(param.resource_name)}">
					<input type="hidden" name="resource_name@op" value="ico" />
				</div>
			</div>
		</div>

		<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
			<table  class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel"><insta:ltext key="patient.resourcescheduler.schedulerdashboard.resourcetype"/>:</div>
						<div class="sfField">
							<insta:checkgroup name="res_sch_type" optexts="${schedulerMap}"
								opvalues="${categoryMap}" selValues="${paramValues.res_sch_type}"/>
							<input type="hidden" name="res_sch_type@op" value="in" />
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="patient.resourcescheduler.schedulerdashboard.department"/>:</div>
						<div class="sfField">
							<input type="text" name="dept_name" value="${ifn:cleanHtmlAttribute(param.dept_name)}">
							<input type="hidden" name="dept_name@op" value="ico" />
						</div>
					</td>
					<td class="last">
						<div class="sfLabel"><insta:ltext key="patient.resourcescheduler.schedulerdashboard.status"/></div>
						<div class="sfField">
							<insta:checkgroup name="status" opvalues="A,I" optexts="${statusOptions}" selValues="${paramValues.status}"/>
							<input type="hidden" name="status@op" value="in" />
						</div>
					</td>
					<td class="last">&nbsp;</td>
					<td class="last">&nbsp;</td>
				</tr>
			</table>
		</div>
	</insta:find>

	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	<div class="resultList">
		<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
			<tr onmouseover="hideToolBar();">
				<tr>
					<insta:sortablecolumn name="res_sch_type" title="${resourcetype}"/>
					<insta:sortablecolumn name="res_sch_name" title="${appointmentitem}"/>
					<insta:sortablecolumn name="dept" title="${dept}"/>
					<th><insta:ltext key="patient.resourcescheduler.schedulerdashboard.description"/></th>
				</tr>
			</tr>

			<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">

			<%-- <c:set var="flagColor">
					<c:choose>
						<c:when test="${record.res_sch_type == 'DOC'}">green</c:when>
						<c:when test="${record.res_sch_type == 'TST'}">red</c:when>
						<c:when test="${record.res_sch_type == 'SER'}">violet</c:when>
						<c:when test="${record.res_sch_type == 'SUR'}">blue</c:when>
						<c:when test="${record.res_sch_type == 'THID'}">yellow</c:when>
						<c:when test="${record.res_sch_type == 'EQID'}">dark_blue</c:when>
						<c:when test="${record.res_sch_type == 'SRID'}">purple</c:when>
						<c:otherwise>empty</c:otherwise>
					</c:choose>
				</c:set> --%>

				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{res_sch_id: '${record.res_sch_id}',category_default_duration: '${record.default_duration}',res_sch_name: '${record.res_sch_name}', res_sch_type: '${record.res_sch_type}'},'');" id="toolbarRow${st.index}">

						<td>
							<c:if test="${record.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
							<c:if test="${record.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
							<c:choose>
								<c:when test="${record.res_sch_type == 'DOC'}">
									<insta:ltext key="patient.resourcescheduler.schedulerdashboard.doctorconsultation"/>
								</c:when>
								<c:otherwise>
									<c:forEach var="resource" items="${categoryDescripton}">
										<c:if test="${record.res_sch_type == resource.res_sch_type}">
											${resource.resource_description}
										</c:if>
									</c:forEach>
								</c:otherwise>
							</c:choose>
						</td>
						<td>
							<c:choose>
								<c:when test="${empty record.resource_name}">
									<c:choose>
										<c:when test="${record.res_sch_type == 'DOC'}">
											<insta:ltext key="patient.resourcescheduler.schedulerdashboard.doctorconsultation"/>
										</c:when>
										<c:otherwise>
											<c:forEach var="resource" items="${categoryDescripton}">
												<c:if test="${record.res_sch_type == resource.res_sch_type}">
													${resource.resource_description}
												</c:if>
											</c:forEach>
										</c:otherwise>
									</c:choose>
								</c:when>
								<c:otherwise>
									${record.resource_name}
								</c:otherwise>
							</c:choose>
						</td>
						<td>
							<c:choose>
								<c:when test="${empty  record.dept_name}">
									<insta:ltext key="patient.resourcescheduler.schedulerdashboard.any"/>
								</c:when>
								<c:otherwise>
									${record.dept_name }
								</c:otherwise>
							</c:choose>
						</td>
						<td>${record.description}</td>
					</tr>
			</c:forEach>

		</table>
		<c:if test="${param.method == 'list'}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>
	</div>
	<c:url var="url" value="${pagePath}/add.htm">
			<c:param name="method" value="add"/>
			<c:param name="category_default_duration" value=""/>
			<c:param name="category" value=""/>
			<c:param name="resource_type" value="primary"/>
	</c:url>

	<table class="screenActions"><tr><td><a href="<c:out value='${url}'/>"><insta:ltext key="patient.resourcescheduler.schedulerdashboard.addnewdefault"/></a></td></tr></table>

	<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
		<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
		<div class="flagText"><insta:ltext key="patient.resourcescheduler.schedulerdashboard.inactive"/></div>
	</div>

</form>
</body>
</html>
