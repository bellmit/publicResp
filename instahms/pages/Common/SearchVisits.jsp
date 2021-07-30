<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<html>

<head>
	<title>Insta HMS</title>
	<meta http-equiv="Content-Type"  content="text/html;charset=iso-8859-1">
	<insta:link type="js" file="ajax.js" />
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="script" file="dashboardColors.js"/>
	<insta:link type="script" file="/medicalrecorddepartment/mrdpatientsearch.js"/>
	<jsp:include page="/pages/Common/MrnoPrefix.jsp"/>
	<style type="text/css">
		.status_I  {background-color : #C5D9A3 }
		.status_ { background-color: #E0E8E0}
		table.legend {border-collapse : collapse ; margin-left : 6px }
		table.legend td {border : 1px solid grey ;padding 2px 5px }
		table.search td { white-space: nowrap }
	</style>

</head>

<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="patientList" value="${pagedList.dtoList}"/>

<body onload="init()">
	<div class="pageHeader">MRD Patient Search</div>

	<html:form method="GET" action="/pages/medicalrecorddepartment/MRDSearch.do">
	<input type="hidden" name="method" value="getMRDPatientSearchDetails" id="method">

	<table class="search" width="100%">
		<tr>
			<td>
				Enter MR No: <html:text property="mrno" size="10"  onkeypress="return onKeyPressMrno(event)" onchange="return onChangeMrno()"/>
			</td>
		</tr>
	</table>

	<c:set var="filterClosed" value="${not empty patientList}"/>

	<div class="stwMain">
		<div class="stwHeader ${filterClosed ? 'stwClosed' : ''}" id="filter" onclick="stwToggle(this);">
			<label>Filter</label>
		</div>

		<div id="filter_content" class="stwContent ${filterClosed ? 'stwHidden' : ''}">
			<table class="search" width="100%" align="center">
				<tr>
					<th>Patient Details:</th>
					<th>Status</th>
					<th>Type:</th>
					<th>Admission Date:</th>
					<th>Department:</th>
					<th>Doctor:</th>
				</tr>

				<tr>
					<td>
						<table>
							<tr>
								<td>First Name:</td>
								<td><html:text property="firstName" size ="8"/></td>
							</tr>
							<tr>
								<td>Last Name:</td>
								<td><html:text property="lastName" size="8"/></td>
							</tr>
							<tr>
								<td>Mobile No.:</td>
								<td><html:text property="phone" size="8"/></td>
							</tr>
						</table>
					</td>

					<td>
						<html:checkbox property="statusAll" onclick="enablePatientStatus()" >All</html:checkbox></br>
						<html:checkbox property="statusActive">Active</html:checkbox></br>
						<html:checkbox property="statusInactive">Inactive</html:checkbox>
					</td>

					<td style="padding-right: 1em">
						<html:checkbox property="typeAll" onclick="enablePatientType();" >All</html:checkbox></br>
						<html:checkbox property="typeIP">IP</html:checkbox></br>
						<html:checkbox property="typeOP">OP</html:checkbox></br>
						<html:checkbox property="typeDiag">Diag </html:checkbox></br>
					</td>

					<td>
						<table>
							<tr>
								<td>From:</td>
								<td><insta:datewidget name="fdate" valid="past" value="${param.fdate}"/></td>
							</tr>
							<tr>
								<td>To:</td>
								<td><insta:datewidget name="tdate" valid="past" value="${param.tdate}"/></td>
							</tr>
						</table>
					</td>

					<td>
						<c:set var="department" value="${deptlist}"/>
						<c:set var="doctor" value="${doctorlist}"/>
						<html:select property="department" size="8" style="width: 11em" multiple="true">
							<c:forEach var="dept" items="${department}">
							<html:option value="${dept.DEPT_ID}">${dept.DEPT_NAME}</html:option>
						</c:forEach>
						</html:select>
					</td>

					<td>
						<html:select property="doctor" size="8" style="width: 11em" multiple="true" >Doctor
							<c:forEach var="doc" items="${doctor}">
								<html:option value="${doc.DOCTOR_ID}">${doc.DOCTOR_NAME}</html:option>
							</c:forEach>
						</html:select>
					</td>
				</tr>

				<tr>
					<td colspan="6" align="right" style="padding-top: 1em">
						<input type="submit" value="Search" onclick="validateSearchForm();"/>
						<input type="reset" value="Reset"/>
						<input type="button" value="Clear" onclick="clearSearch()"/>
					</td>
				</tr>
			</table>
		</div>
	</div>

		<%
			String[] heads = {"mrno","patid","age","deptname","ward","docName","admit"};
			String[] headNames = {"MR No", "Visit No","Patient Name", "Age / Gender", "Mobile No.", "Department", "Doctor","Ward / Bed","AdmitDate / Last Visit"};
			pageContext.setAttribute("heads",heads);
			pageContext.setAttribute("headNames",headNames);
		%>
	<c:choose>
		<c:when test ="${not empty patientList}" >
			<table class="dashboard" cellpadding="0" cellspacing="0" align="center" width="100%">
				<tr>
					<th>MR No</th>
					<th>Visit No</th>
					<th>Patient Name</th>
					<th>Mobile No.</th>
					<th>Department</th>
					<th>Doctor</th>
				</tr>
				<c:forEach var="patient" items="${patientList}" varStatus="st">
					<tr class="status_${patient.status}">
						<td>${patient.mrNo}</td>
						<td><a href="${cpath}/pages/medicalrecorddepartment/MRDUpdate.do?method=getMRDUpdateScreen&patId=${patient.patientId}"
								title="View Patient MRD Details">${patient.patientId}</a></td>
						<td>${patient.title} ${patient.firstName} ${patient.lastName}</td>
						<td>${patient.phone}</td>
						<td>${patient.departmentName}</td>
						<td>${patient.doctorName}</td>
					</tr>
				</c:forEach>
			</table>

			<c:url var="searchURLWithoutPage" value="MRDSearch.do">
				<c:forEach var="p" items="${param}">
					<c:if	test="${p.key != 'pageNum'}">
						<c:param name="${p.key}" value="${p.value}" />
					</c:if>
				</c:forEach>
			</c:url>

			<c:if test="${not empty patientList}">
				<table class="pageList" align="center" >
					<tr>
						<td><b>Pages : </b></td>
						<c:forEach var="page" begin="1" end="${pagedList.numPages}">
							<td>
								<c:choose><c:when test="${pagedList.pageNumber == page}">
									${page}
								</c:when><c:otherwise>
									<b><a href="${searchURLWithoutPage}&pageNum=${page}">${page}</a></b>
								</c:otherwise></c:choose>
							</td>
						</c:forEach>
					</tr>
				</table>
			</c:if>
		</c:when>

		<c:otherwise>
					<p>No Results found for the given search criteria </p>
		</c:otherwise>
	</c:choose>

</html:form>

<b>Legend:</b>
<table class="legend">
	<tr><td class="status_A"> Active Patients</td></tr>
	<tr><td class="status_I">Inactive Patients</td></tr>
</table>

</body>
</html>

