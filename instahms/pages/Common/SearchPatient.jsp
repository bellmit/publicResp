<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>

<head>
	<title>Search Patient - Insta HMS</title>
	<meta http-equiv="Content-Type"  content="text/html;charset=iso-8859-1">
	<insta:link type="js" file="ajax.js" />
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="script" file="dashboardColors.js"/>
	<insta:link type="script" file="/patient/patientsearch.js"/>
	<insta:link type="script" file="hmsvalidation.js" />
	<insta:link type="script" file="dashboardsearch.js"/>
	<jsp:include page="/pages/Common/MrnoPrefix.jsp"/>

	<style type="text/css">
		.status_I  {background-color : #C5D9A3 }
		.status_ { background-color: #E0E8E0}
		table.legend {border-collapse : collapse ; margin-left : 6px }
		table.legend td {border : 1px solid grey ;padding 2px 5px }
		table.search td { white-space: nowrap }

		.scrolForContainer .yui-ac-content{
			 max-height:11em;overflow:auto;overflow-x:hidden; /* scrolling */
		    _height:11em; /* ie6 */
		}
		.autocomplete {
			width:12em; /* set width here or else widget will expand to fit its container */
			padding-bottom: 2em;
		 }
	</style>
	<script>
		var paramDept = '${ifn:cleanJavaScript(param.departmentName)}';
	</script>

</head>

<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="patientList" value="${pagedList.dtoList}"/>
<c:set var="emrlink" value="${cpath}/emr/EMRMainDisplay.do?method=list"/>
<c:set var="doclink" value="${cpath}/doctor/EMRMainDisplay.do?method=list"/>

<jsp:useBean id="typeDisplay" class="java.util.HashMap"/>
<c:set target="${typeDisplay}" property="i" value="IP"/>
<c:set target="${typeDisplay}" property="o" value="OP"/>
<c:set target="${typeDisplay}" property="r" value="Retail"/>
<c:set var="results" value="${not empty patientList}"/>

<body onload="init(${results}, document.patientSearchForm);showFilterActive(${results}, document.patientSearchForm);" class="yui-skin-sam" >
	<div class="pageHeader">Patient Search</div>

	<html:form method="GET" onsubmit="return validateSearchForm();">

	<input type="hidden" name="method" value="getPatientSearch" id="method">
	<input type="hidden" name="_method" value="getPatientSearch"/>
	<input type="hidden" name="mrNoLink" value="${mrNoLink}">
	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>
	<input type="hidden" name="countryid" value="" />
	<input type="hidden" name="stateid" value="" />
	<input type="hidden" name="cityid" value="" />
	<input type="hidden" name="_actionId" id="_actionId" value="${empty param._actionId ? _actionId : param._actionId}"/>
	<input type="hidden" name="_searchMethod" value="getPatientSearch"/>

	<insta:search form="patientSearchForm" optionsId="divMore" closed="${results}" >
		<div class="fltL" style="width: 306px; margin:0 0 10px 10px; ">
			<div style="width: 138px; float: left; margin:0px 12px 0 0;">MR No</div>
			<div class="clrboth"></div>
				<div style="width: 138px; float: left; margin:2px 10px 0px 0;" class="fielddiv">
					<div id="mrnoAutoComplete">
			    		<input type="text" name="mrno" id="mrno" class="field" />
			    		<div id="mrnoContainer"></div>
			    	</div>
				</div>
			<div class="clrboth"></div>
		</div>

		<div id="divMore" style="clear: both; display: ${empty results ? 'none' : 'block'}">
			<div class="mainfiltercontent" style="background-color:#eaf2f8;">

        <div style="width:820px; background-color:#f8fbfe" class=" brR">

	       	<div class="brT PaddBottom10" id="divMore" style="width:100%;" >
	       		<div class="w153_infltr dottedfltr" style="height:200px;">
     				<div class="bold brBfltr" style="padding:5px 0 5px 0; margin:0px 10px 0 0">Details</div>
     				<div class="dark" style="margin:5px 0 0 0;">First Name:</div>
     			 	<div style="margin:5px 0 0 0;">
		        		<html:text property="firstName" size ="8"/>
			        </div>
			        <div class="dark" style="margin:5px 0 0 0;">Last Name:</div>
			        <div style="margin:5px 0 0 0;">
			        	<html:text property="lastName" size="8"/>
			        </div>
			         <div class="dark" style="margin:5px 0 0 0;">Mobile No.:</div>
			        <div style="margin:5px 0 0 0;">
			        	<html:text property="phone" size="8"/>
			        </div>
			        <c:if test="${regPref.caseFileSetting != '' && regPref.caseFileSetting != null && regPref.caseFileSetting == 'Y'}">
			        	<div class="dark" style="margin:5px 0 0 0;">Case File No:</div>
				        <div style="margin:5px 0 0 0;">
				        	<html:text property="caseFileNo" size="8"/>
				        </div>
					</c:if>
					<c:if test="${regPref.oldRegNumField != '' && regPref.oldRegNumField != null}">
						<div class="dark" style="margin:5px 0 0 0;">${regPref.oldRegNumField}:</div>
				        <div style="margin:5px 0 0 0;">
				        	<html:text property="oldReg" size="8"  />
				        </div>
					</c:if>
				</div>

				<div class="w153_infltr dottedfltr" style="height:200px;">
     				<div class="bold brBfltr" style="padding:5px 0 5px 0; margin:0px 10px 0 0">Status</div>
     			 	<div style="margin:5px 0 0 0;">
		        		<html:checkbox property="statusAll" onclick="enablePatientStatus()" >All</html:checkbox></br>
						<html:checkbox property="statusActive">Active</html:checkbox></br>
						<html:checkbox property="statusInactive">Inactive</html:checkbox></br>
						<html:checkbox property="statusNoVisit">No Visits</html:checkbox>
			        </div>
			        <div class="dark" style="margin:5px 0 0 0;">Type</div>
			        <div style="margin:5px 0 0 0;">
			        	<html:checkbox property="typeAll" onclick="enablePatientType();" >All</html:checkbox></br>
						<html:checkbox property="typeIP">IP</html:checkbox></br>
						<html:checkbox property="typeOP">OP</html:checkbox></br>
			        </div>
				</div>

				<div class="w153_infltr dottedfltr" style="height:200px;">
     				<div class="bold brBfltr" style="padding:5px 0 5px 0; margin:0px 10px 0 0">Department</div>
     			 	<div style="margin:5px 0 0 0;">
		        		<c:set var="department" value="${deptlist}"/>
						<c:set var="doctor" value="${doctorlist}"/>
						<!-${doctorlist}  ${deptlist}->
						<html:select property="department" size="8" style="width: 11em" multiple="true">
							<c:forEach var="dept" items="${department}">
							<html:option value="${dept.DEPT_ID}">${dept.DEPT_NAME}</html:option>
						</c:forEach>
						</html:select>
			        </div>
				</div>

				<div class="w153_infltr dottedfltr" style="height:200px;">
     				<div class="bold brBfltr" style="padding:5px 0 5px 0; margin:0px 10px 0 0">Doctor</div>
     			 	<div style="margin:5px 0 0 0;">
		        		<html:select property="doctor" size="8" style="width: 11em" multiple="true" >
							<c:forEach var="doc" items="${doctor}">
							<html:option value="${doc.DOCTOR_ID}">${doc.DOCTOR_NAME}</html:option>
							</c:forEach>
						</html:select>
			        </div>
				</div>

				<div class="w153_infltr dottedfltr" style="height:200px;">
     				<div class="bold brBfltr" style="padding:5px 0 5px 0; margin:0px 10px 0 0">Referral Doctor</div>
     			 	<div style="margin:5px 0 0 0;">
						<html:select property="refdoctor" size="8" style="width: 11em" multiple="true" >
							<c:forEach var="doc" items="${referalDetails}">
							<html:option value="${doc.REF_ID}">${doc.REF_NAME}</html:option>
							</c:forEach>
						</html:select>
					</div>
				</div>

	    		<div class="w153_infltr dottedfltr" style="height: 200px; padding-top: 55px;">
     				<div class="bold brBfltr" style="padding:5px 0 5px 0; margin:0px 10px 0 0">Admission Date</div>
     				<div class="dark" style="margin:5px 0 0 0;">From</div>
     			 	<div style="margin:5px 0 0 0;">
		        		<insta:datewidget name="fdate" value="${param['fdate']}"/>
			        </div>
			        <div class="dark" style="margin:5px 0 0 0;">To</div>
			        <div style="margin:5px 0 0 0;">
			        	<insta:datewidget name="tdate" value="${param['tdate']}"/>
			        </div>
				</div>

				<div class="w153_infltr " style="height: 200px; padding-top: 55px;">
	       			<div class="bold brBfltr" style="padding:5px 0 5px 0; margin:0px 10px 0 0">Location</div>
	       			<div class="dark" style="margin:5px 0 0 0;">Country</div>
       				<div style="margin:5px 0 0 0;">
			           	<div id="autocountry" class="autocomplete">
							<input type="text" name="country" id="country" size="8" class="field" value="${ifn:cleanHtmlAttribute(param.country)}"/>
							<div id="countrycontainer" class="scrolForContainer"></div>
						</div>
					</div>
					<div class="dark" style="margin:5px 0 0 0;">State</div>
					<div style="margin:5px 0 0 0;">
			           	<div id="autostate" class="autocomplete">
							<input type="text"	name="patientstate" id="patientstate" size="8" class="field" onblur="autoCity();" value="${ifn:cleanHtmlAttribute(param.patientstate)}"/>
							<div id="statecontainer" class="scrolForContainer"></div>
						</div>
					</div>
					<div class="dark" style="margin:5px 0 0 0;">City</div>
					<div style="margin:5px 0 0 0;">
			           	<div id="autocity" class="autocomplete">
							<input type="text" name="patientcity" id="patientcity" size="8" class="field" onblur="autoArea();" value="${ifn:cleanHtmlAttribute(param.patientcity)}"/>
							<div id="citycontainer" class="scrolForContainer"></div>
						</div>
					</div>
					<div class="dark" style="margin:5px 0 0 0;">Area</div>
					<div style="margin:5px 0 0 0;">
			           <div id="autoarea" class="autocomplete">
							<input type="text"	name="patientarea" id="patientarea" class="field" size="8" value="${ifn:cleanHtmlAttribute(param.patientarea)}"/>
							<div id="areacontainer" class="scrolForContainer"></div>
						</div>
					</div>
				</div>
				<div class="clrboth"></div>
			</div>
	   	</div>
        <div class="clrboth"></div>
		</div>
		</div>
	</insta:search>


	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	<div style="overflow: auto; width: 953">
		<table class="dashboard" cellpadding="0" cellspacing="0" align="center" width="100%" >
			<tr>
				<th>S.No</th>
				<insta:sortablecolumn name="mrno" title="MR No"/>
				<th>Patient Name</th>
				<th>Age / Gender</th>
				<th>Mobile No.</th>
				<th>Type</th>
				<th>Department</th>
				<th>Doctor</th>
				<th>Referral Doctor</th>
				<th>Ward/Bed</th>
				<th>Admitted</th>
			</tr>

			<c:forEach var="patient" items="${patientList}" varStatus="st">
				<c:choose>
					<c:when test="${patient.status == 'A'}">
						<c:set var="flagColor" value="green_flag"/>
					</c:when>
					<c:when test="${patient.status == 'I'}">
						<c:set var="flagColor" value="red_flag"/>
					</c:when>
					<c:when test="${empty patient.status}">
						<c:set var="flagColor" value="grey_flag"/>
					</c:when>
				</c:choose>
				<tr >
					<td>${(pagedList.pageNumber-1) * 20 + st.index + 1 }</td>
					<td>
						<div style="width: 15px; float: left" ><img src="${cpath}/images/${flagColor}.gif"/></div>
						<c:choose><c:when test="${mrNoLink=='emr'}">
								<a href="${emrlink}&MrNo=${patient.mrNo}"
										title="Patient Medical Records">${patient.mrNo}</a>
						</c:when>
						<c:when test="${mrNoLink=='doc'}">
								<a href="${doclink}&MrNo=${patient.mrNo}&mrNoLink=${mrNoLink}"
										title="Patient Medical Records">${patient.mrNo}</a>
						</c:when>
						<c:otherwise>
							${patient.mrNo}
						</c:otherwise></c:choose>
					</td>
					<td>${patient.title} ${patient.firstName} ${patient.lastName}</td>
					<c:set var="agein" value=""/>
					<c:choose>
						<c:when test="${patient.ageIn eq 'D'}">
							<c:set var="agein" value="Days"/>
						</c:when>
						<c:when test="${patient.ageIn eq 'M'}">
							<c:set var="agein" value="Months"/>
						</c:when>
						<c:otherwise>
							<c:set var="agein" value="Years"/>
						</c:otherwise>

					</c:choose>
					<td>${patient.age} ${agein}/${patient.gender}</td>
					<td>${patient.phone}</td>
					<c:choose>
						<c:when test="${patient.status == 'I'}">
							<td>${typeDisplay[patient.previousVisitType]}</td>
							<td>${patient.previousVisitDepartmentName}</td>
							<td>${patient.previousVisitDoctorName}</td>
							<td>${patient.previousReferredBy}</td>
							<td></td>
							<td><fmt:formatDate value="${patient.previousVisitRegDate}" pattern="dd-MM-yyyy"/>
							<fmt:formatDate type="time" value="${patient.previousVisitRegTime}" pattern="hh:mm a"/>
							</td>
						</c:when>
						<c:when test="${patient.status == 'A'}">
							<td>${typeDisplay[patient.patientType]}</td>
							<td>${patient.departmentName}</td>
							<td>${patient.doctorName}</td>
							<td>${patient.referredBy}</td>
							<td><c:if test="${patient.patientType == 'i'}">
								<c:choose>
									<c:when test="${preferences.modulesActivatedMap['mod_ipservices'] eq 'Y'}">
										<%-- show the allocated bed, if not allocated, no need to show reg. bed type/ward --%>
										<c:choose>
											<c:when test="${empty patient.allocBedName}">(Not allocated)</c:when>
											<c:otherwise>${patient.allocWardName}/${patient.allocBedName}</c:otherwise>
										</c:choose>
									</c:when>
									<c:otherwise>${patient.regWardName}/${patient.billBedType}</c:otherwise>
								</c:choose>
							</c:if></td>
							<td><fmt:formatDate value="${patient.regDate}" pattern="dd-MM-yyyy"/>
							<fmt:formatDate type="time" value="${patient.regTime}" pattern="hh:mm a"/>
							</td>
						</c:when>
						<c:otherwise>
								<td></td>
								<td></td>
								<td></td>
								<td></td>
								<td></td>
						</c:otherwise>
					</c:choose>
				</tr>
			</c:forEach>
			<insta:noresults hasResults="${results}" />
		</table>
	</div>
	<div class="legend " style="display: ${results?'block':'none'}" >
		<div class="flag"><img src="${cpath}/images/green_flag.gif"/></div>
		<div class="flagText" >Active Patients</div>
		<div class="flag"><img src="${cpath}/images/red_flag.gif"/></div>
		<div class="flagText">Inactive Patients</div>
		<div class="flag"><img src="${cpath}/images/grey_flag.gif"/></div>
		<div class="flagText">Patients without visits</div>
	</div>
	<script>
		var areaListmain = ${areaList};
		var countryList = ${countryList};
		var stateList = ${stateList};
		var cityList = ${cityList};
		var referralDoctors = ${referralDoctorsJson};
		var doctors = ${doctorsJson};
		var depts = ${deptsJson};
	</script>
</html:form>
</body>
</html>
