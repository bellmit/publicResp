<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="/WEB-INF/instafn.tld"  prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />

<html>
<head>
<title><insta:ltext key="laboratory.testauditlog.search.title"/></title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<meta name="i18nSupport" content="true"/>
<insta:link type="css" file="widgets.css" />
<insta:link type="script" file="widgets.js" />
<insta:link type="script" file="dashboardColors.js" />
<insta:link type="js" file="dashboardsearch.js"/>
<jsp:include page="/pages/Common/MrnoPrefix.jsp" />
<script src="<%=request.getContextPath()%>/gettests.do?${test_timestamp}&${ifn:cleanURL(sesHospitalId)}&module=${ifn:cleanURL(module)}"></script>

<c:set var="urlPrefix" value="${category eq 'DEP_RAD' ? 'radiology' : 'laboratory'}"/>
<script>

	var allTestNames = deptWiseTestsjson;
	var auditToolbar = {
		Edit: {
			title: 'View Log',
			imageSrc: 'icons/Edit.png',
			href: '/${urlPrefix}/auditlog/AuditLogSearch.do?_method=getAuditLogDetails&al_table=tests_prescribed_audit_log'
		}
	}
	function autoCompleteTest() {
		dataSource = new YAHOO.util.LocalDataSource(allTestNames)
		dataSource.responseSchema = {fields : ["TEST"]};
		oAutoComp1 = new YAHOO.widget.AutoComplete('diagname', 'test_container', dataSource);
		oAutoComp1.maxResultsDisplayed = 15;
		oAutoComp1.allowBrowserAutocomplete = false;
		oAutoComp1.prehighlightClassName = "yui-ac-prehighlight";
		oAutoComp1.typeAhead = false;
		oAutoComp1.useShadow = false;
		oAutoComp1.minQueryLength = 0;
		oAutoComp1.animVert = false;
	}

	function init() {
		autoCompleteTest();
		enablePatientType();
		enableTestStatus();
		initMrNoAutoComplete('${cpath}');
		createToolbar(auditToolbar);

		var theForm = document.diagcenterform;
		var ip = theForm.patientIp.checked ;
		var op = theForm.patientOp.checked ;

		if (!ip && !op)  theForm.patientAll.checked = true;
		if (null != theForm.testStatusAll) {
			var pres = theForm.testPrescribed.checked ;
			var conducted = theForm.testConducted.checked ;
			var partConducted = theForm.testPartialConducted.checked ;
			var cancelled = theForm.testCancelled.checked ;
			var presNoResults = theForm.testPrescribedNoResults.checked;
			var conductedNoResutls = theForm.testConductedNoResults.checked;

			if( !pres && !partConducted && !conducted && !cancelled && !presNoResults && !conductedNoResutls){
				theForm.testStatusAll.checked = true;
				enableTestStatus();

		}
	}

	}

	function enableTestStatus() {
		var theForm = document.diagcenterform;
		if (null != theForm.testStatusAll) {
			var disabled = theForm.testStatusAll.checked;

			theForm.testPrescribed.disabled = disabled;
			theForm.testPartialConducted.disabled = disabled;
			theForm.testConducted.disabled = disabled;
			theForm.testCancelled.disabled = disabled;
			theForm.testPrescribedNoResults.disabled = disabled;
			theForm.testConductedNoResults.disabled = disabled;
		}
	}

	function enablePatientType() {
		var theForm = document.diagcenterform;
		var disabled = theForm.patientAll.checked;

		theForm.patientIp.disabled = disabled;
		theForm.patientOp.disabled = disabled;
	}

	function validateAuditSearch() {
		var theForm = document.diagcenterform;
		if (!doValidateDateField(theForm.fdate))
			return false;
		if (!doValidateDateField(theForm.tdate))
			return false;
		theForm.pageNum.value="";
		theForm._method.value = "getTestList";
		return true;
	}

</script>

</head>

<body onload="init();" class="yui-skin-sam">
<c:set var="select">
 <insta:ltext key="selectdb.dummy.value"/>
</c:set>
<c:set var="mrno">
 <insta:ltext key="ui.label.mrno"/>
</c:set>
<div class="pageHeader">
	<c:if test="${category == 'DEP_LAB'}"><insta:ltext key="laboratory.testauditlog.search.lab.testauditlogs"/></c:if>
	<c:if test="${category == 'DEP_RAD'}"><insta:ltext key="laboratory.testauditlog.search.radiology.testauditlogs"/> </c:if>
</div>
<b><c:out value="${param.msg}"/></b>
<b><c:out value="${resultmsg}"/></b>
<html:form method="GET">
	<input type="hidden" name="_method" value="getTestList"/>
	<input type="hidden" name="_searchMethod" value="getTestList"/>
	<html:hidden property="startPage" />
	<html:hidden property="endPage" />

	<%--The following varibles has to present in
		BatchconductionScreen.jsp
		ViewAllTestPresribedMrnos.jsp
		EditDiagReport.jsp
	  --%>
	<input type="hidden" name="category" value="${ifn:cleanHtmlAttribute(category)}"/>
	<input type="hidden" name="visitid" />
	<input type="hidden" name="reportId" value="" />
	<input type="hidden" name="visitType">
	<html:hidden property="pageNum" />
	<c:forEach var="entry" items="${paramValues}">
		<c:if test="${entry.key eq 'checkBox'}">
			<c:forEach items="${entry.value}" var="p" >
			<input type="hidden" name="checkBox" value="${p}">
			</c:forEach>
		</c:if>
	</c:forEach>
	<c:set var="diagList" value="${pagedList.dtoList}" />
	<c:set var="hasResults" value="${not empty diagList}" />

	<insta:search form="diagcenterform" optionsId="optionalFilter" closed="${hasResults}" validateFunction="validateAuditSearch">
		<div class="searchBasicOpts" >
			<div class="sboField">
				<div class="sboFieldLabel"><insta:ltext key="ui.label.mrno"/>:</div>
				<div class="sboFieldInput">
					<div id="mrnoAutoComplete">
						<input type="text" name="mrno" id="mrno" value="${ifn:cleanHtmlAttribute(param.mrno)}"/>
						<input type="hidden" name="mr_no@op" value="ilike" />
						<div id="mrnoContainer" style="width: 280px"></div>
					</div>
				</div>
			</div>
		</div>

		<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
			<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel"><insta:ltext key="laboratory.testauditlog.search.testdate"/>:</div>
							<div class="sfField">
								<div class="sfFieldSub"><insta:ltext key="laboratory.testauditlog.search.from"/>:</div>
								<insta:datewidget name="fdate" valid="past" value="${param.fdate}" />
							</div>
							<div class="sfField">
								<div class="sfFieldSub"><insta:ltext key="laboratory.testauditlog.search.to"/>:</div>
								<insta:datewidget name="tdate" valid="past" value="${param.tdate}" />
							</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="laboratory.testauditlog.search.department"/>:</div>
						<div class="sfFieldSub">
								<html:select property="department" value="${userDept}" styleClass="dropdown">
									<html:option value="">${select}</html:option>
									<c:forEach var="dept" items="${DiagArraylist}">
										<html:option value="${dept.DDEPT_ID}">${dept.DDEPT_NAME}</html:option>
									</c:forEach>
								</html:select>
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="laboratory.testauditlog.search.testname"/>:</div>
						<div class="sfFieldSub">
							<div id="test_wrapper">
							 <html:text property="diagname" styleId="diagname" style="width: 150px;" value="${param.diagname}"/>
							 <div id="test_container" style="width: 200px;"></div>
							</div>
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="laboratory.testauditlog.search.testtype"/>:</div>
						<div class="sfField">
							<html:checkbox property="testStatusAll"
								onclick="enableTestStatus()"><insta:ltext key="laboratory.testauditlog.search.all"/></html:checkbox><br />
							<html:checkbox property="testPrescribed"><insta:ltext key="laboratory.testauditlog.search.new.results"/></html:checkbox><br />
							<html:checkbox property="testPrescribedNoResults"><insta:ltext key="laboratory.testauditlog.search.new.noresults"/></html:checkbox><br />
							<html:checkbox property="testPartialConducted"><insta:ltext key="laboratory.testauditlog.search.partialconducted"/></html:checkbox><br />
							<html:checkbox property="testConducted"><insta:ltext key="laboratory.testauditlog.search.conducted.results"/></html:checkbox><br />
							<html:checkbox property="testConductedNoResults"><insta:ltext key="laboratory.testauditlog.search.conducted.noresults"/></html:checkbox><br />
							<html:checkbox property="testCancelled"><insta:ltext key="laboratory.testauditlog.search.cancelled"/></html:checkbox><br />
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="laboratory.testauditlog.search.visittype"/>:</div>
						<div class="sfField">
							<html:checkbox property="patientAll"
								onclick="enablePatientType()"><insta:ltext key="laboratory.testauditlog.search.all"/></html:checkbox><br />
							<html:checkbox property="patientIp"><insta:ltext key="laboratory.testauditlog.search.ip"/></html:checkbox><br />
							<html:checkbox property="patientOp"><insta:ltext key="laboratory.testauditlog.search.op"/></html:checkbox><br />
						</div>
					</td>
				</tr>
			</table>
		</div>
	</insta:search>

	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	<c:set var="sampleFlow" value="${diagGenericPref.map.sampleflow_required}"/>
	<div class="resultList">
		<table class="resultList" width="100%" id="dashboardTable">

			<tr>
				<insta:sortablecolumn title="${mrno}" name="mrno"/>
				<th><insta:ltext key="laboratory.testauditlog.search.visitid"/></th>
				<th><insta:ltext key="ui.label.patient.name"/></th>
				<th><insta:ltext key="laboratory.testauditlog.search.testname"/></th>
			</tr>

			<%--Schedules Begins --%>
			<c:forEach var="prescription" items="${diagList}" varStatus="st">

				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'dashboardTable',
						{test_id: '${prescription.map.test_id}',
						 prescribed_id: '${prescription.map.prescribed_id}'});"
					onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>
					<c:set var="color">
						<c:choose>
							<c:when test="${prescription.map.conducted eq 'C' || prescription.map.conducted eq 'CRN' || prescription.map.conducted eq 'V'}">
								<c:set var="flagColor" value="yellow_flag"/>
							</c:when>
							<c:when test="${prescription.map.conducted eq 'X'}">
								<c:set var="flagColor" value="purple_flag"/>
							</c:when>
							<c:otherwise>
								<c:set var="flagColor" value="empty_flag"/>
							</c:otherwise>
						</c:choose>
					</c:set>
					<td ><b>${prescription.map.mr_no}</b></td>
					<td >${prescription.map.pat_id}</td>
					<td >
					<c:choose>
						<c:when test="${(prescription.map.name == null || prescription.map.name == '') && (prescription.map.hospital eq 'incoming')}">
							${prescription.map.incomingpatientname}
						</c:when>
						<c:otherwise>
							${prescription.map.salutation} ${prescription.map.name} ${prescription.map.lastname}
						</c:otherwise>
					</c:choose>
					</td>
					<td ><img src="${cpath}/images/${flagColor}.gif"/>${prescription.map.test_name}</td>

					<c:set var="iteration" value="1" />
					<c:set var="index" value="1" />

					<%-- End of Report List --%>
				</tr>
			</c:forEach>
			<%--Schedules Ends --%>
		</table>
		<insta:noresults hasResults="${hasResults}"/>
	</div>

</html:form>
<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
	<div class="flag"><img src="${cpath}/images/yellow_flag.gif"/></div>
	<div class="flagText"><insta:ltext key="laboratory.testauditlog.search.conducted"/></div>
	<div class="flag"><img src="${cpath}/images/purple_flag.gif"/></div>
	<div class="flagText"><insta:ltext key="laboratory.testauditlog.search.cancel"/></div>
</div>

</body>
</html>

