<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<c:set var="max_centers_inc_default" value='<%=GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")%>'/>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Manage Doctor's Practo Profile-Insta HMS</title>
<insta:link type="css" file="widgets.css" />
<insta:link type="js" file="ajax.js" />
<insta:link type="js" file="masters/Adddoctor.js" />
<insta:link type="js" file="dashboardsearch.js"/>
<insta:link type="js" file="integration/doctors.js"/>


</head>

<c:set var="doctorList" value="${pagedList.dtoList}" />
<c:set var="hasResults"
	value="${not empty pagedList.dtoList ? 'true' : 'false'}" />

<body onload="autoCompleteDoctors(); init();" class="yui-skin-sam">
	<h1>Manage Doctor's Practo Profile</h1>
	<insta:feedback-panel />

	<form action="DoctorProfileMapping.do" method="GET" name="searchform">
		<input type="hidden" name="_method" value="showDashboard" />
		<input type="hidden" name="_searchMethod" value="showDashboard"/>
		<input type="hidden" name="doctorId" />
		<input type="hidden" name="pageNum"/>
		<input type="hidden" name="_publish" value="false"/>
		
	<insta:search form="searchform" optionsId="optionalFilter" closed="${hasResults}">
	<input type="hidden" name="_publish" value="false"/>
				<div class="searchBasicOpts">
					<div class="sboField">
						<div class="sboFieldLabel">Doctor Name</div>
						<div class="sboFieldInput">
							<input type="text" id="doctor_name" name="doctor_name" value="${ifn:cleanHtmlAttribute(param.doctor_name)}"
								style="width:140px;"/>
								<input type="hidden" name="doctor_name@op" value="ilike"/>
								<div id="doctorContainer"></div>
						</div>
					</div>
				</div>

				<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
					<table class="searchFormTable">
						<tr>
							<td>
								<div class="sfLabel">Department</div>
								<div class="sfField">
									<insta:selectdb name="dept_id" multiple="true" table="department" valuecol="dept_id" displaycol="dept_name"
											orderby="dept_name" values="${paramValues.dept_id}" />
									<input type="hidden" name="dept_id@op" value="in"/>
								</div>
							</td>
							<c:if test="${max_centers_inc_default > 1 && centerId eq 0}">
								<td class="last">
								<div class="sfLabel">Center</div>
									<div class="sfField">
										<select class="dropdown" name="_center_id" id="center_id">
											<option value="-1" param._center_id eq -1 ? 'selected' : ''> -- Select -- </option>
											<c:forEach items="${centers}" var="center">
												<option value="${center.map.center_id}"
													${param._center_id eq center.map.center_id ? 'selected' : ''}>${center.map.center_name}</option>
											</c:forEach>
										</select>
										<input type="hidden" name="_center_id@cast" value="y"/>
									</div>
								</td>
							</c:if>
						</tr>
					</table>
				</div>
			</insta:search>
		<insta:paginate curPage="${pagedList.pageNumber}"
			numPages="${pagedList.numPages}"
			totalRecords="${pagedList.totalRecords}" />
	</form>
	<c:url var="publishUrl" value="DoctorProfileMapping.do">
	<c:param name="_method" value="publish"/>
	<%-- add all the request parameters except sort params as parameters to the search URL --%>
	<c:forEach var="p" items="${param}">
		<c:forEach items="${paramValues[p.key]}" var="value">	<%-- handle multival params --%>
			<c:param name="${p.key}" value="${value}"/>
		</c:forEach>
	</c:forEach>
	</c:url>

	<form name="publishForm" action="${publishUrl}" method="POST">
		
		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0"
				id="resultTable" onmouseover="hideToolBar('');">
				<tr onmouseover="hideToolBar();">
					<th style="padding-top: 0px; padding-bottom: 0px;"><input
						type="checkbox" name="_allPageOperations"
						onclick="return checkOrUncheckAll('_selectDoctor', this)" /></th>
					<insta:sortablecolumn name="doctor_name" title="Doctor Name" />
					<insta:sortablecolumn name="dept_name" title="Department"/>
					<insta:sortablecolumn name="center_name" title="Center Name" />
					<th style="width: 2em;">Specialization</th>
					<th style="width: 2em;">Published Status</th>
				</tr>
				<c:forEach var="doctor" items="${pagedList.dtoList}" varStatus="st">
					<tr
						class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						id="toolbarRow${st.index}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
								{doctor_id: '${doctor.map.doctor_id}', org_id: '${ifn:cleanJavaScript(orgId)}', mode: 'update'}, '');">
						<td><input type="checkbox" name="_selectDoctor" 
							<c:if test="${centerPublishedStatus[doctor.map.cen_id] == 'N' || doctor.map.status == 'Y'}">disabled</c:if>
							value="${doctor.map.doctor_id}_${doctor.map.center_name}"></td>
						<td>${doctor.map.doctor_name}</td>
						<td>${doctor.map.dept_name}</td>
						<td>${doctor.map.center_name}</td>
						<td><select name="_specialization_${doctor.map.doctor_id}_${doctor.map.center_name}" id="specialization" >
								  <optgroup label="">
								  	<option val="">--- Specialization ---</option>
								  </optgroup>
						          <c:forEach var="specialization" items="${specializations}">
						          <optgroup label="${specialization.map.speciality}">
						       		   <c:forEach var="subSpecialization" items="${specialization.map.sub_specialities}">
						          			<option value="${subSpecialization.map.id}">${subSpecialization.map.subspecialization}</option>
						          		</c:forEach>
						          </optgroup>
						          </c:forEach>
						     </select>
					      </td>
						<td><c:out value="${doctor.map.status}"/></td>
					</tr>
				</c:forEach>
				<br />
			</table>
		</div>

		<div class="fltL"
			style="width: 50%; margin-top: 5px; display: ${hasResults?'block':'none'}">
			<button type="submit" name="Add" accesskey="C" class="button"
				onclick="return checkIsDocSelected()">Publish</button>
		</div>

	</form>
	
	<script>
		var doctorNames = ${requestScope.doctorNames};
		var CollapsiblePanel1 = new Spry.Widget.CollapsiblePanel("CollapsiblePanel1", {contentIsOpen:false});
		var cpath = '${cpath}';
	</script>

</body>
</html>