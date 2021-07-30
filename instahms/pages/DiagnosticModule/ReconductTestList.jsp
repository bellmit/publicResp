<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@page isELIgnored="false"%>
<%@ taglib uri="/WEB-INF/taglibs-datagrid.tld" prefix="ui"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<html>
<head>
<title><insta:ltext key="laboratory.reconductiontests.test.title"/></title>
<insta:link type="script" file="hmsvalidation.js" />
<insta:link type="script" file="diagnostics/testReconductions.js"/>
<jsp:include page="/pages/Common/MrnoPrefix.jsp" />

<script type="text/javascript">
	var reconductStatus=false;
</script>

<style>
	td.forminfo { font-weight: bold; }
	form { padding: 0px; margin: 0px; }
	table.detailFormTable { font-family:Verdana,Arial,sans-serif; font-size:9pt; border-collapse: collapse; }
	table.detailFormTable td { white-space: nowrap; border: 1px solid silver;}
	.stwMain { margin: 5px 7px }
	tr.deleted {background-color: #F2DCDC; color: gray; }
	tr.deleted input {background-color: #F2DCDC; color: gray;}
	tr.newRow {background-color: #E9F2C2; color: gray; }

	.autocomplete {
		width:15em; /* set width here or else widget will expand to fit its container */
	    padding-bottom:2em;
    }
	.scrolForContainer .yui-ac-content{
		 max-height:11em;overflow:auto;overflow-x:hidden; /* scrolling */
	    _height:11em; /* ie6 */
	}

	.innerHtmlHeader{
		background-colr:#8FBC8F;
		color:black;
		font-size:13;
		font-weight: bold;
		text-align: center;
	}

	.status_reconduct { background-color: #C5D9A3 }

	.spl_timeHide { display: none;}
	.spl_timeShow {display: block;}


</style>
<insta:js-bundle prefix="laboratory.radiology.reconducttest"/>
<insta:js-bundle prefix="registration.patient"/>
</head>

<body>
<c:if test="${module == 'DEP_RAD'}">

	<c:choose>
		<c:when test="${reg_type eq 'incoming' }">
			<h1 style="float: left"><insta:ltext key="laboratory.reconductiontests.test.reconduct.radiologytests"/></h1>
			<c:url var="searchUrl" value="/DiagnosticLabModule/RadiologyIncomingReconductTestList.do" />
		</c:when>
		<c:otherwise>
			<h1 style="float: left"><insta:ltext key="laboratory.reconductiontests.test.reconduct.radiologytest"/></h1>
			<c:url var="searchUrl" value="/DiagnosticLabModule/RadiologyReconductTestList.do" />
		</c:otherwise>
	</c:choose>
</c:if>
<c:if test="${module == 'DEP_LAB'}">
	<c:choose>
		<c:when test="${reg_type eq 'incoming' }">
			<h1 style="float: left"><insta:ltext key="laboratory.reconductiontests.test.reconduct.labtests"/></h1>
			<c:url var="searchUrl" value="/DiagnosticLabModule/LabIncomingReconductTestList.do" />
		</c:when>
		<c:otherwise>
			<h1 style="float: left"><insta:ltext key="laboratory.reconductiontests.test.reconduct.labtest"/></h1>
			<c:url var="searchUrl" value="/DiagnosticLabModule/LabReconductTestList.do" />
		</c:otherwise>
	</c:choose>
</c:if>
<c:set var="labreportsList">
 <insta:ltext key="laboratory.reconductiontests.test.labreportslist"/>
</c:set>

<c:set var="radreportsList">
 <insta:ltext key="laboratory.reconductiontests.test.radreportslist"/>
</c:set>

<jsp:useBean id="recondutunableBean" class="java.util.HashSet" scope="request">
    <%
    recondutunableBean.add("N");
    recondutunableBean.add("X");
    recondutunableBean.add("RBS");
    recondutunableBean.add("RAS");
    %>
</jsp:useBean>

<c:choose>

	<c:when test="${reg_type eq 'incoming' }">
		<insta:incomingpatientsearch searchUrl="${searchUrl}"
			searchMethod="getReconductTestListScreen" fieldName="patientid" />
	</c:when>
	<c:otherwise>
		<insta:patientsearch searchType="visit" searchUrl="${searchUrl}" buttonLabel="Find"
			searchMethod="getReconductTestListScreen" fieldName="patientid" showStatusField="true" activeOnly="false"/>
	</c:otherwise>

</c:choose>
<insta:feedback-panel />
	<c:set var="pescTestDisplay" value="block"></c:set>
	<c:if test="${module eq 'DEP_RAD' or module eq 'DEP_LAB'}">
		<c:if test="${activeStatus eq 'I'}" >
			<c:set var="pescTestDisplay" value="none"></c:set>
		</c:if>
	</c:if>
	<c:choose>
		<c:when test="${reg_type eq 'incoming' }">
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel"><insta:ltext key="laboratory.reconductiontests.test.visitdetails"/></legend>
				<table class="patientdetails" cellpadding="0" cellspacing="0" width="100%">
					<tr>
					<c:if test="${patient.map.mr_no ne null && patient.map.mr_no ne ''}">
						<td><insta:ltext key="ui.label.mrno" />:</td>
						<td class="forminfo">${patient.map.mr_no}</td>
					</c:if>
						<td><insta:ltext key="ui.label.patient.name"/>:</td>
						<td class="forminfo">${patient.map.patient_name}</td>
						<td><insta:ltext key="laboratory.reconductiontests.test.fromlab"/>:</td>
						<td class="forminfo">${patient.map.hospital_name}</td>
					</tr>
					<tr>
						<td><insta:ltext key="laboratory.reconductiontests.test.patientvisit"/>:</td>
						<td class="forminfo">${patient.map.incoming_visit_id}</td>
						<td><insta:ltext key="laboratory.reconductiontests.test.age.gender"/>:</td>
						<td class="forminfo">${patient.map.age_text}${fn:toLowerCase(patient.map.age_unit)} / ${patient.map.gender}
							<c:set var="mrNo" value=""/>
							<c:set var="patientId" value="${patient.map.incoming_visit_id}"/>
						</td>
						<td class="formlabel"></td>
						<td></td>
					</tr>
				</table>
			</fieldset>
		</c:when>
		<c:otherwise>
			<insta:patientdetails visitid="${patientid}" />
			<c:set var="mrNo" value="${patient.mr_no}"/>
			<c:set var="patientId" value="${patient.patient_id}"/>
			<fieldset class="fieldSetBorder" style="display: ${visit_type == 'i' ? 'display' : 'none'}">
				<legend class="fieldSetLabel"><insta:ltext key="patient.header.fieldset.otherdetails"/></legend>
				<table class="patientdetails" cellpadding="0" cellspacing="0" width="100%">
					<c:set var="patientGender" value="${patient.patient_gender}"></c:set>
					<c:if test="${visit_type == 'i'}">
						<tr>
							<td class="formlabel"><insta:ltext key="laboratory.reconductiontests.test.billbedtype"/>: </td>
							<td class="forminfo">${patient.bill_bed_type}</td>
							<td class="formlabel"></td>
							<td></td>
							<td class="formlabel"></td>
							<td></td>
						</tr>
					</c:if>
				</table>
			</fieldset>
		</c:otherwise>
	</c:choose>
<form name="ipform" method="POST">
<input type="hidden" name="_method" id="_method" value="saveReconductionDetails" />
<input type="hidden" name="category" id="category" value="${ifn:cleanHtmlAttribute(module)}"/>
<input type="hidden" name="prescribeTests" id="prescribeTests" value="${prescribeTests}"/>
<input type="hidden" name="reconduction" id="reconduction" value=""/>
<input type="hidden" name="mrno" id="mrno" value="${ifn:cleanHtmlAttribute(mrNo)}"	/>
<input type="hidden" name="patientid" id="patientid" value="${ifn:cleanHtmlAttribute(patientId)}" />
<c:set var="cPath" value="${pageContext.request.contextPath}"/>

<input type="hidden" name="mrno" value="${ifn:cleanHtmlAttribute(param.mr_no)}"/>
<input type="hidden" name="diag_category" id="diag_category" value="${ifn:cleanHtmlAttribute(param.category)}"/>
<div>
		<fieldset class="fieldSetBorder"><legend class="fieldSetLabel"><insta:ltext key="laboratory.reconductiontests.test.testtobereconducted"/></legend>
			<table id="prvtesttable" class="detailList" width="100%">
				<tr>
					<th>
						<input type="checkbox" name="markAll" onclick="markAllForReconduction(this)" title='<insta:ltext key="laboratory.reconductiontests.test.markforreconduction"/>'/>
					</th>
					<th><insta:ltext key="laboratory.reconductiontests.test.doctor"/></th>
					<th><insta:ltext key="laboratory.reconductiontests.test.testname"/></th>
					<th><insta:ltext key="laboratory.reconductiontests.test.ordereddate"/></th>
					<th><insta:ltext key="laboratory.reconductiontests.test.orderedtime"/></th>
					<th><insta:ltext key="laboratory.reconductiontests.test.status"/></th>
					<th style="display:${category eq 'DEP_LAB' ? 'table-cell' : 'none' }"><insta:ltext key="laboratory.reconductiontests.test.reconduction"/>
						<input type="hidden" name="noOfTests" value="${fn:length(presTestList)}"/>
					</th>
					<th><insta:ltext key="laboratory.reconductiontests.test.remarks"/></th>
				</tr>

				<c:set var="sampleFlow" value="${diagGenericPref.map.sampleflow_required}"/>
				<c:forEach var="test" items="${presTestList}" varStatus="loop">
					<c:set var="flagColor" value="${test.RE_CONDUCTION == 't' ? 'green' : 'empty' }"/>
					<tr >
						<c:set var="reconductable" value="${!ifn:contains(recondutunableBean,test.CONDUCTED) and test.RECONDUCT_COUNT == 0 }"/>
						<td>
							<input type="checkbox" name="reconduct" onclick="markForReconduction(this)" value="${test.PRESCRIBED_ID}" ${!reconductable ? 'disabled' : '' }/>
							<input type="hidden" name="marked_for_reconduction" value="N"/>
							<input type="hidden" name="prescribedId"  value="${test.PRESCRIBED_ID}">
							<input type="hidden" name="conductionStatus" value="${test.CONDUCTED}"/>
						</td>
						<td><insta:truncLabel value="${test.DOCTOR_NAME}" length="32"/></td>
						<td><insta:truncLabel value="${test.TEST_NAME }" length="32"/></td>
						<td>${test.PRE_DATE}</td>
						<td>${test.PRES_TIME}</td>
						<td>
							<img class="flag" src="${cPath}/images/${flagColor}_flag.gif"/>${test.STATUS}
							<c:if test="${category eq 'DEP_RAD'}">
								<input type="hidden" name="sampleState" value="NA"/>
							</c:if>
						</td>
						<c:if test="${category eq 'DEP_LAB'}">
						  <td>
					    	<c:choose>
					    	<c:when test="${reconductable && (test.SAMPLE_NEEDED eq 'y' and sampleFlow eq 'Y') || (test.SAMPLE_NEEDED eq 'y' and test.PRESCRIPTION_TYPE eq 'o')}" >
					    		<select name="sampleState" class="dropdown">
					    			<c:if test="${test.OUTSOURCE_DEST_TYPE ne 'C' && test.OUTSOURCE_DEST_TYPE ne 'IO'}">
										<option value="E" ><insta:ltext key="laboratory.reconductiontests.test.existingsample"/></option>
									</c:if>
									<c:if test="${reg_type == null}">
										<option value="N"><insta:ltext key="laboratory.reconductiontests.test.newsample"/></option>
									</c:if>
								</select>
							</c:when>
							<c:otherwise>
								<input type="hidden" name="sampleState" value="NA"/>
							</c:otherwise>
							</c:choose>
						  </td>
						 </c:if>
						  <td>
							  <c:choose>
							  	<c:when test="${reconductable}">
									<input type="text" name="reconducted_reason" value="${test.RECONDUCTED_REASON }" />
								 </c:when>
								 <c:otherwise>
									 <insta:truncLabel value="${test.RECONDUCTED_REASON }" length="20"/>
								 	<input type="hidden" name="reconducted_reason" value="${test.RECONDUCTED_REASON }" />
								 </c:otherwise>
							  </c:choose>
						 </td>
					</tr>
				</c:forEach>
			</table>
		</fieldset>
</div>
	<c:set var="hasResults" value="${not empty presTestList}"/>
	<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
		<div class="flag"><img src='${cPath}/images/green_flag.gif'></div>
		<div class="flagText"><insta:ltext key="laboratory.reconductiontests.test.newtestfromreconduction"/></div>
	</div>
	<div class="screenActions" style="float: left">
		<c:choose>
			<c:when test="${category == 'DEP_LAB'}">
				<c:set var="url" value="Laboratory"/>
				<c:set var="reportListLink" value="${labreportsList}"/>
			</c:when>
			<c:otherwise>
				<c:set var="url" value="Radiology"/>
				<c:set var="reportListLink" value="${radreportsList}"/>
			</c:otherwise>
		</c:choose>
		<button type="button" name="Save" accesskey="S" onclick="return prevTestsValidation();"><b><u><insta:ltext key="laboratory.reconductiontests.test.s"/></u></b><insta:ltext key="laboratory.reconductiontests.test.ave"/></button>
		| <a href='<c:out value="${cPath}/${url}/schedules.do?_method=getScheduleList&category=${ifn:cleanURL(category)}&mr_no=${reg_type eq 'incoming' ? null : patient.mr_no}&patient_id=${reg_type eq 'incoming' ? patient.map.incoming_visit_id : patient.patient_id}"/>'>${reportListLink}</a>
	</div>

</form>
<script type="text/javascript">

	var patientid = '${reg_type eq 'incoming' ? patient.map.incoming_visit_id : patient.patient_id}';
	gServerNow = new Date(<%= (new java.util.Date()).getTime() %>);
	var cpath = '<%=request.getContextPath()%>';
	var cpath = '${cPath}';
	var regType = '${reg_type}';//to find incoming registrations

</script>

</body>
</html>
