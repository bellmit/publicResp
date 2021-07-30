<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%@ taglib uri="/WEB-INF/struts-html.tld"  prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld"  prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld"  prefix="logic" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title><insta:ltext key="registration.patient.uhid.search"/></title>
<insta:js-bundle prefix="registration.uhid"/>
<script>
		var toolbarOptions = getToolbarBundle("js.registration.uhid.toolbar");
		var error = '${errorMessage}';
		var systemType ='${systemType}';
		if(systemType == null || systemType == '')
			systemType = '${param.systemType}';
</script>

<insta:link type="js" file="registration/uhidSearch.js" />
<c:set var="cpath" value="${pageContext.request.contextPath}" />
</head>
<body onload="init();">
<h1>UHID Patient Search</h1>
	<form action="${cpath}/pages/registration/UHIDSearch.do" method="POST"
		name="UHIDSearchForm">
		<input type="hidden" name="_method" value="getDetails"  />
		<div class="searchTitle">
			<div class="searchTitleContents"
				style="font-weight: bold; width: 75px;">Search</div>
		</div>
		<table cellspacing="0">
			<tbody>
				<tr>
					<td class="searchBody" width="820">
						<div class="searchBasicOpts">
							<div class="sboField">
								<div class="sboFieldLabel">UHID/MR No. :</div>
								<div class="sboFieldInput">
									<input name="uhid" id="uhid" type="text" maxlength="100"
										value="${param.uhid}">
								</div>
							</div>
							<div class="sboField">
								<div class="sboFieldLabel">Mobile No. :</div>
								<div class="sboFieldInput">
									<input name="phone" id="phone" type="text" maxlength="100"
										value="${param.phone}">
								</div>
							</div>
							<div class="sboField" style="width: 130px">
								<div class="sboFieldLabel">
									&nbsp;
									<div class="sboFieldInput">
										<input name="all_systems" id="all_systems" type="checkbox"
											onclick="systemSelect()">All Systems
									</div>
								</div>
							</div>
							<div class="sboField" style="width: 130px">
								<div class="sboFieldLabel">
									&nbsp;
									<div class="sboFieldInput">
										<input name="local_system" id="local_system" type="checkbox"
											onclick="systemSelect()">Local System
									</div>
								</div>
							</div>
						</div>

					</td>
					<td
						style="background-color: #eaf2f8; border: 1px #e6e6e6 solid; padding: 10px"
						valign="top" height="100%" width="132">
						<div align="middle">
							<input class="button" id="Search" value="Search" type="submit"
								onclick="validate()">
						</div>
					</td>
				</tr>
			</tbody>
		</table>
		
		<%-- <insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/> --%>

		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
				<tr onmouseover="hideToolBar();">
					<th>#</th>
					<insta:sortablecolumn name="mr_no" title="MR No. / UHID"/>
					<insta:sortablecolumn name="patient_name" title="Patient Name"/>
					<insta:sortablecolumn name="patient_phone" title="Patient Phone"/>
					<insta:sortablecolumn name="patient_gender" title="Patient Gender"/>
					<insta:sortablecolumn name="hospitl_location" title="Hospital Location"/>
				</tr>
				<c:forEach var="record" items="${patientDetailsList}" varStatus="st">
				
				<c:choose>
					<c:when test ="${record.custom_field1 eq 'remote'}">
						<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
							onclick="showToolbar(${st.index}, event, 'resultTable',
							{uhidPatient: 'yes', uhidPatientFirstName: '${record.patient_name}', 
							uhidPatientMiddleName: '${record.middle_name}', uhidPatientLastName: 
							'${record.last_name}', uhidPatientPhone: '${record.patient_phone}', 
							uhidPatientGender: '${record.patient_gender}' , uhidPatientUHID: '${record.mr_no}', 
							uhidPatientAge: '${record.custom_field3}' },null,'',null,uhidSetHrefs);" 
							id="toolbarRow${st.index}">
					</c:when>
					<c:otherwise>
						<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
							onclick="showToolbar(${st.index}, event, 'resultTable',
							{mr_no: '${record.mr_no}', patient_phone: '${record.patient_phone}'},null,'',null, uhidSetHrefs);" 
							id="toolbarRow${st.index}">
					</c:otherwise>
				</c:choose>
						
						<td>${st.index + 1 }</td>
						<td>
							<c:choose>
								<c:when test ="${record.custom_field1 eq 'remote'}">
									<img class="flag" src="${cpath}/images/yellow_flag.gif">
								</c:when>
								<c:otherwise>
									<img class="flag" src="${cpath}/images/violet_flag.gif">
								</c:otherwise>
							</c:choose> 
							${record.mr_no}
						</td>
						<td>${record.patient_name} ${record.middle_name} ${record.last_name}</td>
						<td>${record.patient_phone}</td>
						<td>${record.patient_gender}</td>
						<td>${record.custom_field2}</td>
					</tr>
				</c:forEach>
			</table>
			<c:if test="${param._method == 'list'}">
				<insta:noresults hasResults="${hasResults}"/>
			</c:if>
		</div>
		
		<c:choose>
			<c:when test="${patientDetailsList.size() eq 0}">
				<div style="width: 951px; height: 35px; border: 1px solid #E0E0E0; border-top: none; background-color:#FFC">
					<div style="float: left; width: 25px; margin-top: 10px; margin-left: 3px;">
						<img src="${cpath}/images/alert.png">
					</div>
					<div style="float: left; margin-top: 10px">
						No Results found for the given search criteria
					</div>
				</div>
			</c:when>
		</c:choose>
		<div class="legend" style="display: block">
			<div class="flag"><img src="${cpath}/images/violet_flag.gif"></div>
			<div class="flagText">Local Registrations</div>
			<div class="flag"><img src="${cpath}/images/yellow_flag.gif"></div>
			<div class="flagText">Remote Registrations</div>
		</div>
		
		
		
	</form>
	
	
</body>
</html>