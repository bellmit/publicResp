<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cPath" value="${pageContext.request.contextPath}"/>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html:html>

<jsp:useBean id="thisday" class="java.util.Date"/>
<fmt:formatDate var="thisDateVal" value="${thisday}" pattern="dd-MM-yyyy"/>
<fmt:formatDate var="thisTimeVal" value="${thisday}" pattern="HH:mm"/>


<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title><insta:ltext key="laboratory.testconduction.list.title"/></title>
<meta name="i18nSupport" content="true"/>
<insta:link type="js" file="date_go.js" />
<insta:link type="js" file="hmsvalidation.js" />
<insta:link type="js" file="ajax.js" />
<insta:link type="js" file="diagnostics/batchconductionScreen.js" />
<insta:link type="js" file="lightbox.js" />
<insta:link type="css" file="lightbox.css" />
<insta:link file="SpryAssets/SpryCollapsiblePanel.js" type="js"/>
<insta:link path="scripts/SpryAssets/SpryCollapsiblePanel.css" type="css" />
<insta:link type="js" file="diagnostics/testDetails.js"/>
<insta:link type="js" file="diagnostics/previousresults.js"/>
<insta:link type="js" file="diagnostics/amendments.js"/>

<style type="text/css">
		.yui-skin-sam .yui-dt-col-priority { padding: 2px 10px;}

		.yui-ac {
			width: 15em;
			padding-bottom: 2em;
		}

		.scrolForContainer .yui-ac-content{
			 max-height:11em;overflow:auto;overflow-x:hidden; /* scrolling */
		    _height:11em; /* ie6 */
		}

		table#operationtable tr.added td {
			background-color:#EAFADC;
		}

	</style>

<script>
	var printReportId = '${popupPrintReportId}';
	function popupPrint() {
		var printerid = document.getElementById('printer').value;
		if (printReportId != '') {
			window.open('${pageContext.request.contextPath}' +
			'/pages/DiagnosticModule/DiagReportPrint.do?_method=printReport' +
			'&reportid=' + printReportId + '${rawMode}&printerId='+printerid);
		}
		printReportId = '';
	}
	var impressionsArray = ${impressionsJSON};
	var noGrowthTemplatesArray = ${noGrowthTemplatesJSON};
	var microAbstPanelArray = ${microAbstPanelJSON};
	var microAbstAntiBioticArray = ${microAbstAntiBioticJSON};
	var microOrganismArray = ${microOrganismJSON};
	var growthTemplatesArray = ${growthTemplatesJSON};
	var cpath = '${cPath}';
	var cytoConduction = '${ifn:cleanJavaScript(cytoConduction)}';
	var normalResult = '${jsonHtmlColorCodes.map.normal_color_code}';
	var abnormalResult = '${jsonHtmlColorCodes.map.abnormal_color_code}';
	var criticalResult = '${jsonHtmlColorCodes.map.critical_color_code}';
	var improbableResult = '${jsonHtmlColorCodes.map.improbable_color_code}';
	var storeExist = ${storeExist};
	var reExist = ${ReagentExist};
	//this collectionCenterMrNo is useful in internal lab flow for conduction center, to fetch previous results.
	var collectionCenterMrNo = '${custmer.map.mr_no}';
	var reportID = '${ifn:cleanHtmlAttribute(param.reportId)}';
	var conductingDoctorsLenght = "${fn:length(requestScope.doctors)}"

	function doClose(index) {
		var shortImpression = document.getElementById('impression'+index).value;
		document.getElementById('impression'+index).value = '';
		// window.location.href = "${cPath}/master/HistoImpression.do?_method=addFromConductionScreen&impression_name="+shortImpression;
		var helpWin=window.open("${cPath}/master/HistoImpression.do?_method=addFromConductionScreen&impression_name="+shortImpression);
			helpWin.focus();
			document.getElementById('impression'+index).focus();
			return false;
	}
</script>
<insta:js-bundle prefix="laboratory.radiology"/>
<insta:js-bundle prefix="laboratory.radiology.batchconduction"/>
<insta:js-bundle prefix="diagnostics.batchconduction"/>
</head>
<body onload="init();ajaxForPrintUrls();initPreviousResultsDialog();" class="yui-skin-sam">
<c:set var="select">
 <insta:ltext key="selectdb.dummy.value"/>
</c:set>
<c:set var="manageReports">
 <insta:ltext key="laboratory.managereports.list.managereports"/>
</c:set>
<c:set var="labreportList">
 <insta:ltext key="laboratory.reconductiontests.test.labreportslist"/>
</c:set>
<c:set var="radreportList">
 <insta:ltext key="laboratory.reconductiontests.test.radreportslist"/>
</c:set>
<div class="pageHeader"><insta:ltext key="laboratory.testconduction.list.testconduction"/></div>
<insta:feedback-panel/>
<%-- when one or more tests not verified following information gets displayed on the page and save buttons disabled --%>
<div style="display:none; margin-bottom:10px; padding:10px 0 10px 10px; height: 15px; background-color:#FFC;" class="brB brT brL brR" id="infoMsgDiv">
	<div class="fltL" style="width: 25px; margin:-1px 0 0 3px;" id="infoImg"> <img src="${cPath}/images/information.png" /></div>
	<div class="fltL"  style="margin:0px 0 0 5px ; width:865px;" id="infoDiv"></div>
</div>

<div style="display:none; margin-bottom:10px; padding:10px 0 10px 10px; height: 15px; background-color:#FFC;" class="brB brT brL brR" id="warnMsgDiv">
	<div class="fltL" style="width: 25px; margin:-1px 0 0 3px;" id="warnImg"> <img src="${cPath}/images/error.png" /></div>
	<div class="fltL"  style="margin:0px 0 0 5px ; width:865px;" id="warnDiv"></div>
</div>
<c:choose>
	<c:when test="${not empty patientvisitdetails }">
		<insta:patientdetails  visitid="${patientvisitdetails.map.patient_id}" showClinicalInfo="true"/>
		<c:set var="visitId" value="${patientvisitdetails.map.patient_id}"/>
		<c:set var="mr_no" value="${patientvisitdetails.map.mr_no}" />
	</c:when>
	<c:otherwise>
		<c:set var="mr_no" value="${custmer.map.mr_no}" />
		<c:set var="visitId" value="${custmer.map.incoming_visit_id}"/>
		<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel"><insta:ltext key="laboratory.testconduction.list.patientdetails"/></legend>
		<table class="formtable" cellpadding="0" cellspacing="0" width="100%">
			<tr>
				<td><insta:ltext key="ui.label.mrno"/>:</td>
				<td class="forminfo">${custmer.map.mr_no}</td>
				<td><insta:ltext key="laboratory.testconduction.list.fromlab"/>:</td>
				<td class="forminfo">${custmer.map.hospital_name}</td>
			</tr>
			<tr>
				<td><insta:ltext key="ui.label.patient.name"/>:</td>
				<td class="forminfo">${custmer.map.patient_name}</td>
				<td><insta:ltext key="laboratory.testconduction.list.age.gender"/>:</td>			
				<td class="forminfo">${custmer.map.age_text}${fn:toLowerCase(custmer.map.age_unit)} / ${custmer.map.gender}</td>
			</tr>
			<tr>
			    <td><insta:ltext key="laboratory.testconduction.list.patientvisit"/>:</td>
			    <td class="forminfo">${custmer.map.incoming_visit_id}</td>
			</tr>
		</table>
		</fieldset>
	</c:otherwise>
</c:choose>

<form method="POST" name="diagcenterform" acceptCharset="UTF-8" action="${cPath}/${category == 'DEP_LAB' ? 'Laboratory' : 'Radiology'}/editresults.do?_method=saveTestDetails"  autocomplete="off">
	<input type="hidden" name='visitid' value="${ifn:cleanHtmlAttribute(visitId)}"/>
	<input type="hidden" name="contextpath" value="${pageContext.request.contextPath}" />
	<input type="hidden" name="saveandprint" />
	<input type="hidden" name="printerId" value=""/>
	<input type="hidden" name="category" id="category" value="${ifn:cleanHtmlAttribute(category)}"/>
	<input type="hidden" name="visitId" value="${ifn:cleanHtmlAttribute(param.visitId)}"/>
	<input type="hidden" name="testId" value="${tr.test.testId} "/>
	<input type="hidden" name="testName"  value="${tr.test.testName} "/>
	<input type="hidden" name="reagentsexist" value="${tr.reagentsexist}"/>
	<input type="hidden" name="_method" value=""/>
	<input type="hidden" name="updatedTemplateRowIndex" id="updatedTemplateRowIndex" value="0"/>
	<%--The following varibles has to present in
		BatchconductionScreen.jsp
		ViewAllTestPresribedMrnos.jsp
		EditDiagReport.jsp
	  --%>
	<input type="hidden" name="category" value="${ifn:cleanHtmlAttribute(param.category)}"/>
	<input type="hidden" name="pageNum" value="${ifn:cleanHtmlAttribute(param.pageNum)}"/>
	<input type="hidden" name="reportId" value="${ifn:cleanHtmlAttribute(param.reportId)}"/>
	<c:forEach var="p" items="${param}">
	   <c:if test="${p.key eq 'checkBox'}">
		  <input type="hidden" name="${p.key}" value="${p.value}">
		</c:if>
	</c:forEach>

	<input type="hidden" name="printreportId" value="${ifn:cleanHtmlAttribute(printreportId)}"/>
	<input type="hidden" name='mrno' id="mrno" value="${patientvisitdetails.map.mr_no}">



<%--Test with Results  --%>
<c:set var="incr" value="0" />
<c:set var="rincr" value="0" />
<c:set var="sampleFlow" value="${diagGenericPref.map.sampleflow_required}"/>
<c:set var="validateResultsRt" value="${actionRightsMap.validate_test_results eq 'A' || roleId == 1 || roleId == 2}"/>
<c:set var="revSignOffRt" value="${actionRightsMap.revert_signoff  eq 'A' || roleId == 1 || roleId == 2}"/>
<c:set var="hasSignOffRights" value="${(roleId le 2) || actionRightsMap['sign_off_lab_reports'] eq 'A'}"/>
<c:set var="amendTestResultsRt" value="${(roleId le 2) || actionRightsMap['amend_test_results'] eq 'A'}"/>
<c:set var="reportWithIncompleteTests" value="N"/>
<% String[] types = {"V", "T", "M", "H"}; pageContext.setAttribute("types", types);%>

<jsp:useBean id="conductionStatusBean" class="java.util.HashSet" scope="request">
    <%
    conductionStatusBean.add("N");
    conductionStatusBean.add("P");
    conductionStatusBean.add("MA");
    conductionStatusBean.add("CC");
    conductionStatusBean.add("TS");
    conductionStatusBean.add("C");
    conductionStatusBean.add("CR");
    conductionStatusBean.add("V");
    %>
</jsp:useBean>

<jsp:useBean id="amendableStatusBean" class="java.util.HashSet" scope="request">
    <%
    amendableStatusBean.add("RP");
    amendableStatusBean.add("RC");
    amendableStatusBean.add("RV");
    %>
</jsp:useBean>

<jsp:useBean id="reconductedStatusBean" class="java.util.HashSet" scope="request">
    <%
    reconductedStatusBean.add("RBS");
    reconductedStatusBean.add("RAS");
    %>
</jsp:useBean>


<input type="hidden" name="category" value="${ifn:cleanHtmlAttribute(category)}"/>
<input type="hidden" name="visitId" value="${patientvisitdetails.map.patient_id}"/>
<input type="hidden" name="testName" value="${tr.test.testName}" />
<input type="hidden" name="reagentsexist" value="${tr.test.testId}"/>
<input type="hidden" name="mod_activation_status" value="${mod_activation_status}"/>
<input type="hidden" name="amendresult" value="${empty param.amendresult  ?  amendresult: param.amendresult}"/>

<c:set var="canAmend" value="${empty param.amendresult ?  amendresult: param.amendresult}"/>
<c:set var="resultDisclaimerIndex" value="0"/>
<c:set var="commonIndex" value="0"/>
<c:set var="impessionIndex" value="0"/>
<c:set var="noGrowthIndex" value="0"/>
<c:set var="testsEditable" value="1"/>
<c:set var="allCompleted" value="checked disabled"/>
<c:set var="allVerified" value="checked disabled"/>
<c:set var="amendingPhase" value="N"/>
<c:set var="testsReconducted" value="Y"/>
<c:set var="reagenetsExists_V" value="N"/>
<c:set var="methodologyExists" value="N"/>

<c:forEach var="valuetest" items="${valueTests }">
	<c:set var="reagenetsExists_V" value="${ ( reagenetsExists_V == 'Y' || valuetest.reagentsexist ) ? 'Y' : 'N' }"/>
</c:forEach>
<c:forEach var="mainList" items="${valueTests}" >
	<c:forEach var="result" items="${mainList.result}">
		<c:set var="methodologyExists" value="${ (methodologyExists == 'Y' || not empty result.methodId ) ? 'Y' : 'N'}" />
	</c:forEach>
</c:forEach>

<c:set var="reagenetsExists_T" value="N"/>
<c:forEach var="templatetest" items="${templateTests }">
	<c:set var="reagenetsExists_T" value="${ ( reagenetsExists_T == 'Y' || templatetest.reagentsexist ) ? 'Y' : 'N' }"/>
</c:forEach>
			<div id="div${incr}">
				<c:if test="${not empty valueTests }">
				<div class="resultList">
					<table cellspacing="0" cellpadding="0" id="resultsTable" class="resultList" style="margin-top: 0px" width="100%">
						<%--hidden fields for each test --%>
						<tr>
							<th><insta:ltext key="laboratory.testconduction.list.test"/></th>
							<th><insta:ltext key="laboratory.testconduction.list.conductionstatus"/></th>
							<th id="reagenetsTh_V" style="display:none" ></th>
							<th></th>
							<th><insta:ltext key="laboratory.testconduction.list.resultname"/></th>
							<th id="methodologyTh" style="display:none"></th>
							<th></th>
							<th></th>
							<th><insta:ltext key="laboratory.testconduction.list.value"/></th>
							<th><insta:ltext key="laboratory.testconduction.list.severity"/></th>
							<th><insta:ltext key="laboratory.testconduction.list.units"/></th>
							<th><insta:ltext key="laboratory.testconduction.list.range"/></th>
							<th></th>
						</tr>
						<c:set var="isTransferedTest" value="false"/>
						<c:forEach var="tr" items="${valueTests }">
						<html:hidden property="testname" value="${tr.test.testName}" />
						<input type="hidden" name="sampleNo" value="${tr.test.sampleNo}" />
						<c:if test="${not empty tr.test.outSourceDestPresId && !isTransferedTest}">
							<c:set var="isTransferedTest" value="true" />
						</c:if>
						<c:set var="script" value="0" />
						<c:choose>
							<c:when test="${tr.condctionStatus != 'C' && tr.condctionStatus != 'V'
								&& tr.condctionStatus != 'S' && !ifn:contains(reconductedStatusBean,tr.condctionStatus)}">
								<c:set var="testsEditable" value="1"/>
							</c:when>
							<c:when test="${(tr.condctionStatus == 'C' || tr.condctionStatus == 'V')
									&& validateResultsRt && tr.condctionStatus != 'S'}">
								<c:set var="testsEditable" value="1"/>
							</c:when>
							<c:otherwise>
								<c:set var="testsEditable" value="0"/>
							</c:otherwise>
						</c:choose>
						
						<c:if test="${tr.condctionStatus == 'C' && validateResultsRt}">
						</c:if>
						<c:set var="resultDisclaimerIndex" value="${resultDisclaimerIndex+1 }"/>
					    <c:set var="script" value="${script + 1}" />
					    <c:set var="defaultCondutionStatus" value="${tr.condctionStatus ne 'N' ? tr.condctionStatus : 'P' }"/>
					     <c:if test="${param.category == 'DEP_RAD'}">
					     <c:set var="firstrowstatus" value="${conductionstates[0].value}"/>
					    <c:set var="defaultCondutionStatus" value="${tr.condctionStatus ne 'N' ? tr.condctionStatus : firstrowstatus}"/>
					    </c:if>
					    <c:set var="defaultCondutionStatus" value="${tr.condctionStatus eq 'S' && canAmend == 'Y' ? 'RP' : defaultCondutionStatus }"/>
					    <c:set var="testStatusEditable" value="${testsEditable == 1 || ifn:contains(amendableStatusBean,defaultCondutionStatus)}"/>
							    <tr>
							    	<td>
								     	<c:if test="${ script == 1}">
								     		${ script == 1 ? tr.test.testName : ''}
								     	</c:if>
							     	<br/>
							     		<c:if test="${testsEditable == 1 && script == 1 && !ifn:contains(amendableStatusBean,tr.condctionStatus)}">
								     		<a onclick="showTestDetails(this,${tr.prescribedId},${commonIndex},'${tr.test.sampleNo }');" href="javascript:void(0)"
								     			name="loadDialog" id="loadDialog" >
											<img src="${cPath}/icons/Edit.png" class="button" title='<insta:ltext key="laboratory.testconduction.list.addedittestdetails"/>'/>
											</a>
										</c:if>
										<c:if test="${script == 1 && testsEditable == 1  && !ifn:contains(amendableStatusBean,tr.condctionStatus)}">
											<c:url value="TestDocumentsList.do" var="testDocumentsUrl">
												<c:param name="_method" value="searchTestDocuments"/>
												<c:param name="prescribed_id" value="${tr.prescribedId}"/>
												<c:param name="reportId" value="${param.reportId}"/>
											</c:url>
											<c:if test="${category == 'DEP_LAB' ? urlRightsMap.lab_test_documents == 'A' : urlRightsMap.rad_test_documents == 'A'}">
												&nbsp;| &nbsp; <a href="<c:out value='${testDocumentsUrl}'/>" title='<insta:ltext key="laboratory.testconduction.list.addeditviewtestdocuments"/>'>
													${tr.hasDocuments ? 'Edit Doc List' : 'Add Document'}
												</a>
											</c:if>
											<c:if test="${tr.mandate_additional_info == 'O'}">
												<c:url value="/Diagnostics/TestInfoViewer.do" var="testInfoUrl">
													<c:param name="_method" value="list"/>
													<c:param name="prescribed_id" value="${tr.prescribedId}"/>
													<c:param name="patient_id" value="${patientvisitdetails.map.patient_id}"/>
												</c:url>
												|<a href="${testInfoUrl}" title="Test Information Viewer" target="_blank">
													Test Info Viewer
												</a>
												<br/>
											</c:if>
										</c:if>
										<input type="hidden" name="testid" value="${tr.test.testId}" />
										<input type="hidden" name="conducting_doc_mandatory" id="conducting_doc_mandatory${incr}"
												value="${tr.test.conducting_doc_mandatory}">
										<input type="hidden" name="dateOfInvestigation" id="dateOfInvestigation${commonIndex }" value="${empty tr.test.testDate ? thisDateVal : tr.test.testDate }"/>
										<input type="hidden" name="timeOfInvestigation" id="timeOfInvestigation${commonIndex }" value="${empty tr.test.testTime ? thisTimeVal : tr.test.testTime}"/>
										<input type="hidden" name="doctor" id="doctor${commonIndex }" value="${tr.test.conductedDoctor }"/>
										<input type="hidden" name="sampleno" id="sampleno${commonIndex }" value="${tr.test.sampleNo }"/>
										<input type="hidden" name="dateOfSample" id="dateOfSample${commonIndex }"  value="${tr.test.sampleDate }"/>
										<input type="hidden" name="specimen_condition" id="specimen_condition${commonIndex }" value="${tr.test.specimenCondition }"/>
										<input type="hidden" size="40" name="testRemarks" id="testRemarks${commonIndex }" value="${tr.test.remarks}"/>
										<input type="hidden" name="labNo" id="labNo${commonIndex }" value="${tr.test.labno}" />
										<input type="hidden" name="ddeptid" id="ddeptid${commonIndex}" value="${tr.test.ddeptId}" />
										<input type="hidden" name="h_technician" id="h_technician${commonIndex}" value="${tr.test.technician}"/>
									</td>
									<td>
										<c:if test="${ script == 1}">
											<select name="dum_completed" class="dropdown" style="width:8em"
												onchange="setCompletedStatus(this)" ${!testStatusEditable ? 'disabled' : ''}>
											<c:choose>
											  <c:when test="${param.category == 'DEP_RAD'}">
													 <c:forEach var="states" items="${stateMap[tr.prescribedId]}">
																	<option value="${states.map.value}" ${states.map.value == defaultCondutionStatus ? 'selected' : ''}>${states.map.display_name}
																	</option>
																</c:forEach>
														<c:set var="testsReconducted" value="N"/>
											  </c:when>
											  <c:otherwise>
												<c:choose>
													<c:when test="${ ifn:contains(conductionStatusBean,defaultCondutionStatus)}">
														<option value="P"
															<c:if test="${defaultCondutionStatus eq 'P'}"><insta:ltext key="laboratory.testconduction.list.selected"/></c:if>><insta:ltext key="laboratory.testconduction.list.inprogress"/></option>
														<option value="C"
															<c:if test="${defaultCondutionStatus eq 'C'}"><insta:ltext key="laboratory.testconduction.list.selected"/></c:if>><insta:ltext key="laboratory.testconduction.list.completed"/></option>
														<c:if test="${validateResultsRt }">
															<option value="V"
																<c:if test="${defaultCondutionStatus eq 'V'}"><insta:ltext key="laboratory.testconduction.list.selected"/></c:if>><insta:ltext key="laboratory.testconduction.list.validated"/></option>
														</c:if>
														<c:set var="testsReconducted" value="N"/>
													</c:when>
													<c:when test="${ ifn:contains(reconductedStatusBean,tr.condctionStatus)}">
														<option value="RBS" selected><insta:ltext key="laboratory.testconduction.list.reconducted"/></option>
													</c:when>
													<c:otherwise>
														<c:set var="amendingPhase" value="Y"/>
														<c:set var="testsReconducted" value="N"/>
														<option value="S"
															style="display:${defaultCondutionStatus ne 'S' && not empty canAmend  ? 'none' : 'block'}"
															<c:if test="${defaultCondutionStatus eq 'S'}"><insta:ltext key="laboratory.testconduction.list.selected"/></c:if>><insta:ltext key="laboratory.testconduction.list.signedoff"/></option>
														<option value="RP"
															style="display:${defaultCondutionStatus eq 'S' ? 'none' : 'block'}"
															<c:if test="${defaultCondutionStatus eq 'RP'}"><insta:ltext key="laboratory.testconduction.list.selected"/></c:if>><insta:ltext key="laboratory.testconduction.list.revisioninprogress"/></option>
														<option value="RC"
															style="display:${defaultCondutionStatus eq 'S' ? 'none' : 'block'}"
															<c:if test="${defaultCondutionStatus eq 'RC'}"><insta:ltext key="laboratory.testconduction.list.selected"/></c:if>><insta:ltext key="laboratory.testconduction.list.revisioncompleted"/></option>
														<option value="RV"
															style="display:${defaultCondutionStatus eq 'S' ? 'none' : 'block'}"
															<c:if test="${defaultCondutionStatus eq 'RV'}"><insta:ltext key="laboratory.testconduction.list.selected"/></c:if>><insta:ltext key="laboratory.testconduction.list.revisionvalidated"/></option>
													</c:otherwise>
													</c:choose>
												</c:otherwise>
											 </c:choose>
											</select>

											<c:set var="allCompleted"
												value="${allCompleted ne '' &&
													(tr.condctionStatus eq 'C' || tr.condctionStatus eq 'V'
														|| tr.condctionStatus eq 'RC' || tr.condctionStatus eq 'RV'
														|| (tr.condctionStatus eq 'S' && empty canAmend)
														|| tr.condctionStatus eq 'RBS' || tr.condctionStatus eq 'RAS')? 'checked disabled' : ''}"/>
											<c:set var="allVerified"
												value="${allVerified ne '' && tr.condctionStatus eq 'V'
													|| (tr.condctionStatus eq 'S' && empty canAmend) || tr.condctionStatus eq 'RV'
													|| tr.condctionStatus eq 'RBS' || tr.condctionStatus eq 'RAS' ? 'checked disabled' : ''}"/>
											<input type="hidden" name="completed" value="${defaultCondutionStatus}"/>
											<input type="hidden" name="prescribedid" value="${tr.prescribedId}" />
											<input type="hidden" name="revisionNumber" value="${tr.revisionNumber}" />
										</c:if>
									</td>

									<td>
										<c:if test="${script == 1 && tr.reagentsexist && !ifn:contains(amendableStatusBean,tr.condctionStatus) }">
											<c:url value="editresults.do" var="editReagentsUrl">
												<c:param name="_method" value="modifyReagents"/>
												<c:param name="visitId" value="${not empty patientvisitdetails ? patientvisitdetails.map.patient_id : custmer.map.incoming_visit_id }"/>
												<c:param name="testName" value="${tr.test.testName}"/>
												<c:param name="testId" value="${tr.test.testId}"/>
												<c:param name="category" value="${category}"/>
												<c:param name="prescribedId" value="${tr.prescribedId}"/>
												<c:param name="conducted" value="${tr.condctionStatus}"/>
											</c:url>
							             	<c:if test="${( tr.condctionStatus == 'N' || tr.condctionStatus == 'P')  && mod_activation_status eq 'Y'}">
												<a href="${editReagentsUrl}">
													Edit
												</a>
											</c:if>
										</c:if>
									</td>
									<td colspan="${tr.reagentsexist && reagenetsExists_V eq 'Y' && !ifn:contains(amendableStatusBean,tr.condctionStatus) ? 9 : 8}">&nbsp;
										<input type="hidden" name="remarks"
											id="remarks${ resultDisclaimerIndex}" value="${ifn:cleanHtmlAttribute(results.remarks)}"/>
										<html:hidden property="resultlabel" value="${results.resultLabel}" />
										<input type="hidden" name="resultlabel_id" value=""/>
										<input type="hidden" name="calc_res_expr" value=""/>
										<input type="hidden" name="method_id" id="method_id" value="" />
										<input type="hidden" name="units" value="${ifn:cleanHtmlAttribute(results.units)}"/>
										<input type="hidden" name="referenceRanges" value="${fn:escapeXml(results.referenceRanges)}" />
										<input type="hidden" name="rtestId" value="${tr.test.testId}" />
										<input type="hidden" name="rprescribedId" value="${tr.prescribedId}" />
										<input type="hidden" name="rrevisionNumber" value="${tr.revisionNumber}" />										
										<input type="hidden" name="conductedinreportformat" value="N" />
										<input type="hidden" name="formatid" value="" />
										<input type="hidden" name="reporttemplate" value="" />
										<input type="hidden" name="expression" id="expression" value="${results.expression }"/>
										<input type="hidden" name="resultDisclaimer"
											id="resultDisclaimer${ resultDisclaimerIndex}" value="${results.result_disclaimer}"/>
										<input type="hidden" name="test_details_id" value="${results.test_details_id }"/>
										<input type="hidden" name="deleted_new_test_details_id" value=""/>
										<input type="hidden" name="deleted" value="N"/>
										<input type="hidden" name="revised_test_details_id" value="${results.revised_test_details_id }"/>
										<input type="hidden" name="original_test_details_id" value="${results.original_test_details_id }"/>
										<input type="hidden" name="test_detail_status" value="${results.test_detail_status }"/>
										<input type="hidden" name="amendment_reason" id="amendment_reason${commonIndex }" value="${results.amendment_reason }"/>
										<input type="hidden"  name="dum_resultvalue"/>
										<input type="hidden" name="withinNormal"/>
										<input type="hidden" name="resultvalue" value=""/>
										<input type="hidden" name="seviarity" value=""/>
										<input type="hidden" name="commonIndex" value="${commonIndex}"/>
									</td>
									<c:set var="commonIndex" value="${commonIndex+1}"/>
									<c:set var="reportWithIncompleteTests" value="${tr.condctionStatus eq 'N' || tr.condctionStatus eq 'P' || tr.condctionStatus eq 'MA' || tr.condctionStatus eq 'CC' || tr.condctionStatus eq 'CR' || tr.condctionStatus eq 'TS' || tr.condctionStatus == 'RP' ? 'Y' : reportWithIncompleteTests}"/>
							    </tr>
							    <c:forEach var="results" items="${tr.result}" varStatus="st">
							    <c:set var="resultDisclaimerIndex" value="${resultDisclaimerIndex+1 }"/>
							    	<c:set var="allowOverridesForExpr" value="true" />
							    	<c:set var="disableCheckBox" value="${(fn:trim(results.expression) != '' && ((tr.condctionStatus == 'C' || tr.condctionStatus == 'V')
											&& !validateResultsRt) || (tr.condctionStatus == 'S' && canAmend != 'Y'))}" />
									<c:choose>
										<c:when test="${(fn:trim(results.expression) != '' && ((tr.condctionStatus == 'C' || tr.condctionStatus == 'V')
											&& !validateResultsRt) || (tr.condctionStatus == 'S' && canAmend != 'Y') || (results.calculated == 'Y' || empty (results.calculated))) }">
											<c:set var="allowOverridesForExpr" value="false"/>
										</c:when>							
									</c:choose>
							     <tr  style="text-decoration: ${results.test_detail_status eq 'A' ? 'line-through' :  '' }">

							     	<c:set var="resultReadonly" value="readonly" />
							     	<c:if test="${fn:trim(results.expression) == ''}">
							     		<c:set var="resultReadonly" value="" />
							     	</c:if>
							     	<c:set var="resultEditable" value="${testsEditable != 0}"/>
							     	<td colspan="${ ( reagenetsExists_V eq 'Y'
							     					&& !ifn:contains(amendableStatusBean,tr.condctionStatus) )
						     						? 4 : 3}" class="indent"></td>
									<td><insta:truncLabel value="${results.resultLabel}" length="32"/></td>
									<c:if test="${methodologyExists == 'Y'}">
										<td>${results.methodName}</td>
									</c:if>
									<td>
									<c:if test="${(canAmend == 'Y' ||
														ifn:contains(amendableStatusBean,tr.condctionStatus)
														&& amendTestResultsRt)}">
										<c:choose>
											<c:when test="${fn:trim(results.expression) eq '' && (results.original_test_details_id == 0 || results.test_detail_status eq 'S' || results.test_detail_status eq 'A')}">
											<c:set var="resultEditable" value="${!ifn:contains(amendableStatusBean,tr.condctionStatus)}"/>
												<input type="button"  value="Amend" name="amendBtn"
													onclick="askAmendmentReason(this,'V',${commonIndex });"
													${results.test_detail_status eq 'A' ? 'disabled' : ''}/>
													<a href="javascript:void(0)"
														onclick='getAmendemntReasonDialog(this,${commonIndex },false);'
														title='<insta:ltext key="laboratory.testconduction.list.amendmentreason"/>'>
														<img src="${cPath}/icons/AddBlue.png" class="button" />
													</a>
											</c:when>
											<c:otherwise>
												<c:set var="resultEditable" value="${ifn:contains(amendableStatusBean,tr.condctionStatus)}"/>
												<c:if test="${ (fn:trim(results.expression)) eq ''}">
													<input type="button"  value="Delete" name="amendBtn"
														${ (fn:trim(results.expression)) ne '' ? 'disabled' : '' }
														onclick="deleteRow(this);"/>
													<input type="hidden" name="newResultAfterAmendment" id="newResultAfterAmendment" value="Y"/>
												</c:if>
											</c:otherwise>
										</c:choose>
									</c:if>
									<input type="hidden" name="amendment_reason" id="amendment_reason${commonIndex }" value="${results.amendment_reason }"/>
									</td>
									<td>
										<c:set var="testDateAndTime"
											value="${not empty tr.test.testDate ? tr.test.testDate : thisDateVal} ${not empty tr.test.testDate ? tr.test.testTime : thisTimeVal}"/>
										<a href="javascript:void(0)"
											onclick='return getPreviousResults(this, "${testDateAndTime}", <insta:jsString value="${results.resultLabel}"/>, "${ifn:cleanJavaScript(category)}", "${results.methodId}", <insta:jsString value="${results.methodName}"/>, "${results.resultlabel_id}");'
											title='<insta:ltext key="laboratory.testconduction.list.previousresultshistory"/>'>
											<img src="${cPath}/icons/Send.png" class="button" />
										</a>
									</td>
									<c:choose>
										<c:when test="${ not empty results.withinNormal && ( results.withinNormal == '***' || results.withinNormal == '###' ) }">
											<c:set var="backColor" value="${jsonHtmlColorCodes.map.improbable_color_code}"/>
										</c:when>
										<c:when test="${not empty results.withinNormal && ( results.withinNormal == '**' || results.withinNormal == '##' ) }">
											<c:set var="backColor" value="${jsonHtmlColorCodes.map.critical_color_code}"/>
										</c:when>
										<c:when test="${not empty results.withinNormal && ( results.withinNormal == '*' || results.withinNormal == '#' ) }">
											<c:set var="backColor" value="${jsonHtmlColorCodes.map.abnormal_color_code}"/>
										</c:when>
										<c:when test="${ (fn:trim(results.expression)) ne '' && results.test_details_id == 0}">
											<c:set var="backColor" value="rgb(211,211,211)"/>
										</c:when>
										<c:when test="${ (fn:trim(results.expression)) ne '' }">
											<c:set var="backColor" value=""/>
										</c:when>
										<c:otherwise>
											<c:set var="backColor" value="${jsonHtmlColorCodes.map.normal_color_code}"/>
										</c:otherwise>
									</c:choose>
									<c:set var="expressionExists" value="${ (fn:trim(results.expression)) ne '' }"/>
									<td>
										<c:choose>
										<c:when test="${results.dataAllowed eq 'V'}">
											<c:set var="resultVal">
												<c:choose>
													<c:when test="${not empty results.defaultValue && results.test_details_id eq 0}">
														${ifn:cleanHtml(results.defaultValue)}
													</c:when>
													<c:otherwise>
														${results.resultvalue}
													</c:otherwise>
												</c:choose>
											</c:set>
											<c:set var="width" value="57" />
											<c:set var="height" value="21" />	
											<c:if test="${fn:length(resultVal) gt 7}">
												<c:set var="width" value="200" />
												<c:set var="txtLength" value="${fn:length(resultVal)}" />
												<c:choose>
													<c:when test="${txtLength > 60}">
														<c:set var="height" value="${txtLength/30*18}" />
													</c:when>
													<c:otherwise>
														<c:set var="height" value="37.8" />
													</c:otherwise>
												</c:choose>											
											</c:if>		
											<textarea style="width: ${width}px; height: ${height}px; background-color:${backColor};" name="dum_resultvalue" id="script${commonIndex}"
												tabindex="1" onkeypress="nextFieldOnEnter(this,${commonIndex},event);"
												onFocus="nextfield ='script${tr.prescribedId}${script}';" 
												${(fn:trim(results.expression) == '' ? (testsEditable == 0 || !resultEditable) : !allowOverridesForExpr) ? 'disabled' : ''}
												onblur="setSiviarity('${results.resultrange.map.min_normal_value}','${results.resultrange.map.max_normal_value}',
														'${results.resultrange.map.min_critical_value}','${results.resultrange.map.max_critical_value}',
														'${results.resultrange.map.min_improbable_value}','${results.resultrange.map.max_improbable_value}',
														this,withinNormal,seviarity,'${results.resultrange}','${results.countOfRanges}','${expressionExists}')"><c:out value="${resultVal}" /></textarea>
											<input type="hidden"  name="resultvalue" value="<c:out value="${resultVal}"/>"/>
											<input type="hidden" name="defaultValue" value="<c:out value="${results.defaultValue}"/>"/>
												<c:choose>
													<c:when test="${fn:trim(results.expression) != ''}">
														<input type="checkbox" ${disableCheckBox ? 'disabled' : ''} name="result_lbl_check" id="result_lbl_check${commonIndex}" onclick="enableResultEntry();" ${results.calculated == 'N' ? 'checked' : '' } title="Override auto-calculated value">
														<input type="hidden"  id="calc_res_expr${commonIndex}" name="calc_res_expr" value="${empty (results.calculated) ? 'Y' : results.calculated }"/>															
													</c:when>
													<c:otherwise>
														<input type="hidden"  id="calc_res_expr${commonIndex}" name="calc_res_expr" value=""/>
													</c:otherwise>
												</c:choose>	
										</c:when>
										<c:otherwise>
											<insta:selectoptions name="dum_resultvalue" style="width: 60px;background-color:${backColor};"
												id="script${commonIndex}"
												onkeypress="nextFieldOnEnter(this,${commonIndex},event)" onchange="setResultsValue(this)"
												disabled="${testsEditable == 0 || !resultEditable || resultReadonly ne '' }"
												onFocus="nextfield ='script${tr.prescribedId}${script}'" onblur="setSiviarity('${results.resultrange.map.min_normal_value}','${results.resultrange.map.max_normal_value}',
														'${results.resultrange.map.min_critical_value}','${results.resultrange.map.max_critical_value}',
														'${results.resultrange.map.min_improbable_value}','${results.resultrange.map.max_improbable_value}',
														this,withinNormal,seviarity,'${results.resultrange }','${results.countOfRanges}','${expressionExists}')"
												opvalues="${results.sourceIfList}" optexts="${results.sourceIfList}" value="${results.resultvalue}"
												dummyvalue="${select}" dummyvalueId=""></insta:selectoptions>
											<input type="hidden"  name="resultvalue" value="<c:out value="${results.resultvalue}"/>"/>
											<input type="hidden" name="defaultValue" value="<c:out value="${results.defaultValue}"/>"/>	
											<input type="hidden"  id="calc_res_expr${commonIndex}" name="calc_res_expr" value=""/>										
										</c:otherwise>
										</c:choose>
									</td>
									<td>
										<c:set var="withinnormaldisable" value=""/>
										<c:if test="${results.resultrange != null   || !resultEditable}">
											<c:set var="withinnormaldisable" value="disabled"/>
										</c:if>
										<select name="withinNormal" id="withinNormal"
											${ testsEditable == 0 || !resultEditable ? 'disabled' : ''}
											class="dropdown" style="width:8em" onchange="setSeviarity('${results.resultrange.map.min_normal_value}','${results.resultrange.map.max_normal_value}',
													'${results.resultrange.map.min_critical_value}','${results.resultrange.map.max_critical_value}',
													'${results.resultrange.map.min_improbable_value}','${results.resultrange.map.max_improbable_value}',
													this,withinNormal,seviarity,'${results.resultrange}','${results.countOfRanges}','${expressionExists}');">
											<option value="">${select}</option>
											<option value="Y" ${results.withinNormal == 'Y'?'selected':''}><insta:ltext key="laboratory.testconduction.list.normal"/></option>
											<option value="*" ${results.withinNormal == '*'?'selected':''} ><insta:ltext key="laboratory.testconduction.list.abnormallow"/></option>
											<option value="#" ${results.withinNormal == '#'?'selected':''}><insta:ltext key="laboratory.testconduction.list.abnormalhigh"/></option>
											<option value="**"${results.withinNormal == '**'?'selected':''} ><insta:ltext key="laboratory.testconduction.list.criticallow"/></option>
											<option value="##"${results.withinNormal == '##'?'selected':''} ><insta:ltext key="laboratory.testconduction.list.criticalhigh"/></option>
											<option value="***"${results.withinNormal == '***'?'selected':''} ><insta:ltext key="laboratory.testconduction.list.improbablelow"/></option>
											<option value="###"${results.withinNormal == '###'?'selected':''} ><insta:ltext key="laboratory.testconduction.list.improbablehigh"/></option>
										</select>
										<input type="hidden" name="seviarity" id="seviarity" value="${results.withinNormal}"/>
									</td>
									<td>${ifn:cleanHtml(results.units)}</td>
									<td>${ifn:breakContent(fn:escapeXml(results.referenceRanges))}</td>

									<td>
										<div id="disclaimerDiv" style="display:${testsEditable == 1  && resultEditable ? 'block' : 'none'}">
											<a onclick="showDiscliamerDialog(this,${ resultDisclaimerIndex})"
											 href="javascript:void(0)"
								     			name="otherDetails" id="otherDetails">
											<img src="${cPath}/icons/Report.png" class="button" title='<insta:ltext key="laboratory.testconduction.list.editremarks.disclaimer"/>'/>
											</a>
										</div>
										<input type="hidden" name="remarks"
											id="remarks${ resultDisclaimerIndex}" value="${ifn:cleanHtmlAttribute(results.remarks)}"/>
										<html:hidden property="resultlabel" value="${results.resultLabel}" />
										<input type="hidden" name="resultlabel_id" value="${ifn:cleanHtmlAttribute(results.resultlabel_id)}"/>
										<input type="hidden" name="method_id" id="method_id" value="${results.methodId}" />
										<input type="hidden" name="units" value="${ifn:cleanHtmlAttribute(results.units)}"/>
										<input type="hidden" name="referenceRanges" value="${fn:escapeXml(results.referenceRanges)}" />
										<input type="hidden" name="rtestId" value="${tr.test.testId}" />
										<input type="hidden" name="rprescribedId" value="${tr.prescribedId}" />
										<input type="hidden" name="rrevisionNumber" value="${tr.revisionNumber}" />										
										<input type="hidden" name="conductedinreportformat" value="N" />
										<input type="hidden" name="formatid" value="" />
										<input type="hidden" name="reporttemplate" value="" />
										<input type="hidden" name="expression" id="expression" value="${results.expression }"/>
										<input type="hidden" name="resultDisclaimer"
											id="resultDisclaimer${ resultDisclaimerIndex}" value="${results.result_disclaimer}"/>
										<input type="hidden" name="test_details_id" value="${results.test_details_id }"/>
										<input type="hidden" name="deleted_new_test_details_id" value=""/>
										<input type="hidden" name="deleted" value="N"/>
										<input type="hidden" name="revised_test_details_id" value="${results.revised_test_details_id }"/>
										<input type="hidden" name="original_test_details_id" value="${results.original_test_details_id }"/>
										<input type="hidden" name="test_detail_status" value="${results.test_detail_status }"/>
										<input type="hidden" name="doctor" id="doctor${commonIndex }" value="${tr.test.conductedDoctor }"/>
										<input type="hidden" size="40" name="testRemarks" value="${tr.test.remarks}"/>
										<input type="hidden" name="dateOfInvestigation" value="${empty tr.test.testDate ? thisDateVal : tr.test.testDate }"/>
										<input type="hidden" name="timeOfInvestigation" value="${empty tr.test.testTime ? thisTimeVal : tr.test.testTime}"/>
										<input type="hidden" name="specimen_condition" id="specimen_condition" value="${tr.test.specimenCondition }"/>
										<input type="hidden" name="conducting_doc_mandatory" id="conducting_doc_mandatory${incr}"
											value="NA">
										<input type="hidden" name="sampleno" value="${tr.test.sampleNo }"/>
										<input type="hidden" name="dateOfSample" value="${tr.test.sampleDate }"/>
										<input type="hidden" name="completed" value=""/>
										<input type="hidden" name="prescribedid" value="" />
										<input type="hidden" name="revisionNumber" value="${tr.revisionNumber}" />
										<input type="hidden" name="commonIndex" value="${commonIndex}"/>
										<input type="hidden" name="testid" value="${tr.test.testId}" />
										<input type="hidden" name="dum_completed" value="NA" />
										<input type="hidden" name="labNo" id="labNo${commonIndex }" value="${tr.test.labno}" />
										<input type="hidden" name="expression" value="${results.expression}"/>
										<input type="hidden" name="ddeptid" id="ddeptid${commonIndex}" value="${tr.test.ddeptId}" />
										<input type="hidden" name="h_technician" id="h_technician${commonIndex}" value="${tr.test.technician}"/>
										<input type="hidden" name="newResult" value="N"/>
									</td>


								</tr>
								<c:set var="rincr" value="${rincr+1}" />
								<c:set var="commonIndex" value="${commonIndex+1}"/>
							</c:forEach>
						</c:forEach>
					</table>
					</div>
				</c:if> <%--Test with Results END --%>
				<table  style="margin-bottom: 6px"></table>
				<c:if test="${not empty templateTests}">
					<div class="resultList">

					<table cellspacing="0" cellpadding="0" class="detailList" id="templatesTable" style="margin-top: 0px" width="100%">
						<tr>
							<th><insta:ltext key="laboratory.testconduction.list.test"/></th>
							<th><insta:ltext key="laboratory.testconduction.list.conductionstatus"/></th>
							<th id="reagenetsTh_T" style="display:none"></th>
							<th><insta:ltext key="laboratory.testconduction.list.result"/></th>
							<th></th>
							<th></th>
							<c:if test="${(not empty tr.newTemplates) and (tr.condctionStatus eq 'P' || tr.condctionStatus eq 'MA' || tr.condctionStatus eq 'CC'|| tr.condctionStatus eq 'TS')|| tr.isTemplatethere eq 'Y' }">
								  <th><insta:ltext key="laboratory.testconduction.list.changetotemplate"/></th>
							</c:if>
						</tr>

						<c:forEach var="tr" items="${templateTests }">
							<c:if test="${not empty tr.test.outSourceDestPresId && !isTransferedTest}">
								<c:set var="isTransferedTest" value="true" />
							</c:if>
						<c:set var="defaultCondutionStatus" value="${tr.condctionStatus ne 'N' ? tr.condctionStatus : 'P' }"/>
						<c:if test="${param.category == 'DEP_RAD'}">
					     <c:set var="firstrowstatus" value="${conductionstates[0].value}"/>
					    <c:set var="defaultCondutionStatus" value="${tr.condctionStatus ne 'N' ? tr.condctionStatus : firstrowstatus}"/>
					    </c:if>
						<c:choose>
							<c:when test="${tr.condctionStatus != 'C' && tr.condctionStatus != 'V'
								&& tr.condctionStatus != 'S' && !ifn:contains(reconductedStatusBean,tr.condctionStatus)}">
								<c:set var="testsEditable" value="1"/>
							</c:when>
							<c:when test="${(tr.condctionStatus == 'C' || tr.condctionStatus == 'V')
									&& validateResultsRt && tr.condctionStatus != 'S'}">
								<c:set var="testsEditable" value="1"/>
							</c:when>
							<c:otherwise>
								<c:set var="testsEditable" value="0"/>
							</c:otherwise>
						</c:choose>

						<c:set var="defaultCondutionStatus" value="${tr.condctionStatus eq 'S' && canAmend == 'Y' ? 'RP' : defaultCondutionStatus }"/>
					    <c:set var="testStatusEditable" value="${testsEditable == 1 || ifn:contains(amendableStatusBean,defaultCondutionStatus)}"/>

						<tr class="${testStatus.first ? 'firstRow' : ''}">
							<td>${ tr.test.testName }<br/>
								<c:if test="${testsEditable == 1 && !ifn:contains(amendableStatusBean,tr.condctionStatus) }">
						     		<a onclick="showTestDetails(this,${tr.prescribedId},${commonIndex},'${tr.test.sampleNo }');" href="javascript:void(0)"
						     			name="loadDialog" id="loadDialog">
									<img src="${cPath}/icons/Edit.png" class="button" title='<insta:ltext key="laboratory.testconduction.list.addedittestdetails"/>'/>
									</a>
								</c:if>
								<c:if test="${testsEditable == 1 && !ifn:contains(amendableStatusBean,tr.condctionStatus) }">
									<c:url value="TestDocumentsList.do" var="testDocumentsUrl">
										<c:param name="_method" value="searchTestDocuments"/>
										<c:param name="prescribed_id" value="${tr.prescribedId}"/>
										<c:param name="reportId" value="${param.reportId}"/>
									</c:url>
									<c:if test="${category == 'DEP_LAB' ? urlRightsMap.lab_test_documents == 'A' : urlRightsMap.rad_test_documents == 'A'}">
										|<a href="<c:out value='${testDocumentsUrl}'/>" title='<insta:ltext key="laboratory.testconduction.list.addeditviewtestdocuments"/>'>
											${tr.hasDocuments ? 'Edit Doc List' : 'Add Document'}
										</a>
									</c:if>
									<c:if test="${tr.mandate_additional_info == 'O'}">
										<c:url value="/Diagnostics/TestInfoViewer.do" var="testInfoUrl">
											<c:param name="_method" value="list"/>
											<c:param name="prescribed_id" value="${tr.prescribedId}"/>
											<c:param name="patient_id" value="${patientvisitdetails.map.patient_id}"/>
										</c:url>
										|<a href="${testInfoUrl}" title="Test Information Viewer" target="_blank">
											Test Info Viewer
										</a>
										<br/>
									</c:if>
								</c:if>
								<c:if test="${testsEditable == 1 && !ifn:contains(amendableStatusBean,tr.condctionStatus) }">
									<c:url value="AddTestImages.do" var="testImgesUrl">
										<c:param name="_method" value="getAttachImagesScreen"/>
										<c:param name="prescribedId" value="${tr.prescribedId}"/>
										<c:param name="reportId" value="${param.reportId}"/>
										<c:param name="patientId" value="${patientvisitdetails.map.patient_id}"/>
									</c:url>
									|<a href="<c:out value='${testImgesUrl}'/>" title='<insta:ltext key="laboratory.testconduction.list.addviewdeletetestimages"/>'>
										${tr.imageUploaded eq 'Y' ? 'Edit Image List' : 'Add Image' }
									</a>
								</c:if>
								<input type="hidden" name="conducting_doc_mandatory" id="conducting_doc_mandatory${incr}"
											value="${tr.test.conducting_doc_mandatory}">
								<input type="hidden" name="dateOfInvestigation" id="dateOfInvestigation${commonIndex }" value="${empty tr.test.testDate ? thisDateVal : tr.test.testDate }"/>
								<input type="hidden" name="timeOfInvestigation" id="timeOfInvestigation${commonIndex }" value="${empty tr.test.testTime ? thisTimeVal : tr.test.testTime}"/>
								<input type="hidden" name="doctor" id="doctor${commonIndex }" value="${tr.test.conductedDoctor }"/>
								<input type="hidden" name="sampleno" id="sampleno${commonIndex }" value="${tr.test.sampleNo }"/>
								<input type="hidden" name="dateOfSample" id="dateOfSample${commonIndex }"  value="${tr.test.sampleDate }"/>
								<input type="hidden" name="specimen_condition" id="specimen_condition${commonIndex }" value="${tr.test.specimenCondition }"/>
								<input type="hidden" size="40" name="testRemarks" id="testRemarks${commonIndex }" value="${tr.test.remarks}"/>
								 <input type="hidden" name="formatid" value="${tr.template.templateId}">
								 <input type="hidden" name="labNo" id="labNo${commonIndex }" value="${tr.test.labno}" />
							</td>
							<td>
								<select name="dum_completed" class="dropdown" style="width:8em"
									onchange="setCompletedStatus(this)" ${!testStatusEditable ? 'disabled' : ''}>
									<c:choose>
										<c:when test="${param.category == 'DEP_RAD'}">
											<c:forEach var="states" items="${stateMap[tr.prescribedId]}">
													<option value="${states.map.value}" ${states.map.value == defaultCondutionStatus ? 'selected' : ''}>${states.map.display_name}
													</option>
											</c:forEach>
													<c:set var="testsReconducted" value="N"/>
									  </c:when>
									  <c:otherwise>
											<c:choose>
												<c:when test="${ ifn:contains(conductionStatusBean,defaultCondutionStatus)}">
																<option value="P"
																	<c:if test="${defaultCondutionStatus eq 'P'}"><insta:ltext key="laboratory.testconduction.list.selected"/></c:if>><insta:ltext key="laboratory.testconduction.list.inprogress"/></option>
																<option value="C"
																	<c:if test="${defaultCondutionStatus eq 'C'}"><insta:ltext key="laboratory.testconduction.list.selected"/></c:if>><insta:ltext key="laboratory.testconduction.list.completed"/></option>
																<c:if test="${validateResultsRt }">
																	<option value="V"
																		<c:if test="${defaultCondutionStatus eq 'V'}"><insta:ltext key="laboratory.testconduction.list.selected"/></c:if>><insta:ltext key="laboratory.testconduction.list.validated"/></option>
																</c:if>
													<c:set var="testsReconducted" value="N"/>
												</c:when>
												<c:when test="${ ifn:contains(reconductedStatusBean,tr.condctionStatus)}">
													<option value="RBS" selected>Reconducted</option>
												</c:when>
												<c:otherwise>
														<c:set var="amendingPhase" value="Y"/>
														<c:set var="testsReconducted" value="N"/>
														<option value="S"
															style="display:${defaultCondutionStatus ne 'S' && not empty canAmend  ? 'none' : 'block'}"
															<c:if test="${defaultCondutionStatus eq 'S'}"><insta:ltext key="laboratory.testconduction.list.selected"/></c:if>><insta:ltext key="laboratory.testconduction.list.signedoff"/></option>
													<option  value="RP"
															style="display:${defaultCondutionStatus eq 'S' ? 'none' : 'block'}"
														<c:if test="${defaultCondutionStatus eq 'RP'}"><insta:ltext key="laboratory.testconduction.list.selected"/></c:if>><insta:ltext key="laboratory.testconduction.list.revisioninprogress"/></option>
													<option value="RC"
															style="display:${defaultCondutionStatus eq 'S' ? 'none' : 'block'}"
														<c:if test="${defaultCondutionStatus eq 'RC'}"><insta:ltext key="laboratory.testconduction.list.selected"/></c:if>><insta:ltext key="laboratory.testconduction.list.revisioncompleted"/></option>
													<option value="RV"
														style="display:${defaultCondutionStatus eq 'S' ? 'none' : 'block'}"
														<c:if test="${defaultCondutionStatus eq 'RV'}"><insta:ltext key="laboratory.testconduction.list.selected"/></c:if>><insta:ltext key="laboratory.testconduction.list.revisionvalidated"/></option>
												</c:otherwise>
											</c:choose>
									</c:otherwise>
									</c:choose>
								</select>

								<c:set var="allCompleted"
									value="${allCompleted ne '' &&
										(tr.condctionStatus eq 'C' || tr.condctionStatus eq 'V'
											|| tr.condctionStatus eq 'RC' || tr.condctionStatus eq 'RV'
											|| (tr.condctionStatus eq 'S' && empty canAmend)
											|| tr.condctionStatus eq 'RBS' || tr.condctionStatus eq 'RAS') ? 'checked disabled' : ''}"/>
								<c:set var="allVerified"
									value="${allVerified ne '' && tr.condctionStatus eq 'V'
										|| tr.condctionStatus eq 'RV'
										|| (tr.condctionStatus eq 'S' && empty canAmend)
										|| tr.condctionStatus eq 'RBS' || tr.condctionStatus eq 'RAS'  ? 'checked disabled' : ''}"/>
								<input type="hidden" name="completed" value="${defaultCondutionStatus }"/>
								<input type="hidden" name="prescribedid" value="${tr.prescribedId}" />
								<input type="hidden" name="revisionNumber" value="${tr.revisionNumber}" />
								<input type="hidden" name="testid" value="${tr.test.testId}" />
								<html:hidden property="testname" value="${tr.test.testName}" />
								<input type="hidden" name="rtestId" value="${tr.test.testId}" />
								<input type="hidden" name="rprescribedId" value="" />
								<input type="hidden" name="rrevisionNumber" value="${tr.revisionNumber}" />
								<input type="hidden" name="resultlabel" value="" />
								<input type="hidden" name="resultlabel_id" id="resultlabel_id"/>
								<input type="hidden" name="method_id" id="method_id" value="" />
								<input type="hidden" name="resultvalue" value="" />
								<input type="hidden" name="calc_res_expr" value=""/>
								<input type="hidden" name="units" value="" />
								<input type="hidden" name="referenceRanges" value="" />
								<input type="hidden" name="remarks" value="" />
								<input type="hidden" name="conductedinreportformat" value="Y" />
								<input type="hidden" name="ddeptid" id="ddeptid${commonIndex}" value="${tr.test.ddeptId}" />
								<input type="hidden" name="h_technician" id="h_technician${commonIndex}" value="${tr.test.technician}"/>
								<input type="hidden" name="withinNormal" id="withinNormal${rincr}" value="Y">
								<input type="hidden" name="seviarity" id="seviarity${rincr}" value="Y"/>
								<input type="hidden" name="resultDisclaimer" value=""/>
								<input type="hidden" name="test_details_id" value="${tr.reportFormatDetails.map.test_details_id }"/>
								<input type="hidden" name="deleted_new_test_details_id" value=""/>
								<input type="hidden" name="deleted" value="N"/>
								<input type="hidden" name="revised_test_details_id" value="${tr.reportFormatDetails.map.revised_test_details_id }"/>
								<input type="hidden" name="original_test_details_id" value="${tr.reportFormatDetails.map.original_test_details_id }"/>
								<input type="hidden" name="test_detail_status" value="${tr.reportFormatDetails.map.test_detail_status }"/>
								<input type="hidden" name="amendment_reason" id="amendment_reason${commonIndex }" value="${tr.reportFormatDetails.map.amendment_reason }"/>
								<input type="hidden"  name="dum_resultvalue"/>
								<input type="hidden" name="expression" id="expression" value=""/>
								<input type="hidden" name="sampleNo" value="${tr.test.sampleNo}" />
								<input type="hidden" name="commonIndex" value="${commonIndex}"/>
								<c:set var="rincr" value="${rincr+1}" />
							</td>
							<td>
								<c:if test="${ tr.reagentsexist && !ifn:contains(amendableStatusBean,tr.condctionStatus) }">
									<c:url value="editresults.do" var="editReagentsUrl">
										<c:param name="_method" value="modifyReagents"/>
										<c:param name="visitId" value="${not empty patientvisitdetails ? patientvisitdetails.map.patient_id : custmer.map.incoming_visit_id }"/>
										<c:param name="testName" value="${tr.test.testName}"/>
										<c:param name="testId" value="${tr.test.testId}"/>
										<c:param name="category" value="${category}"/>
										<c:param name="prescribedId" value="${tr.prescribedId}"/>
										<c:param name="conducted" value="${tr.condctionStatus}"/>
									</c:url>
					             	<c:if test="${( tr.condctionStatus == 'N' || tr.condctionStatus == 'P')  && mod_activation_status eq 'Y'}">
										<a href="${editReagentsUrl}">
											<insta:ltext key="laboratory.testconduction.list.edit"/>
										</a>
									</c:if>
								</c:if>
							</td>
							<td colspan="3">&nbsp;</td>
							<c:set var="commonIndex" value="${commonIndex+1}"/>
							<c:set var="reportWithIncompleteTests" value="${tr.condctionStatus eq 'N' || tr.condctionStatus eq 'P' || tr.condctionStatus eq 'MA' || tr.condctionStatus eq 'CC' || tr.condctionStatus eq 'CR' || tr.condctionStatus eq 'TS' || tr.condctionStatus == 'RP' ? 'Y' : reportWithIncompleteTests}"/>
							</tr>
							<tr>
								 <%--Result--%>
								<td colspan="${reagenetsExists_T eq 'Y' && !ifn:contains(amendableStatusBean,tr.condctionStatus) ? 3 : 2}">&nbsp;
									<input type="hidden" name="conducting_doc_mandatory" id="conducting_doc_mandatory${incr}"
												value="${tr.test.conducting_doc_mandatory}">
									<input type="hidden" name="dateOfInvestigation" id="dateOfInvestigation${commonIndex }" value="${empty tr.test.testDate ? thisDateVal : tr.test.testDate }"/>
									<input type="hidden" name="timeOfInvestigation" id="timeOfInvestigation${commonIndex }" value="${empty tr.test.testTime ? thisTimeVal : tr.test.testTime}"/>
									<input type="hidden" name="doctor" id="doctor${commonIndex }" value="${tr.test.conductedDoctor }"/>
									<input type="hidden" name="sampleno" id="sampleno${commonIndex }" value="${tr.test.sampleNo }"/>
									<input type="hidden" name="dateOfSample" id="dateOfSample${commonIndex }"  value="${tr.test.sampleDate }"/>
									<input type="hidden" name="specimen_condition" id="specimen_condition${commonIndex }" value="${tr.test.specimenCondition }"/>
									<input type="hidden" size="40" name="testRemarks" id="testRemarks${commonIndex }" value="${tr.test.remarks}"/>
									<input type="hidden" name="completed" value="${defaultCondutionStatus }"/>
									<input type="hidden" name="prescribedid" value="" />
									<input type="hidden" name="revisionNumber" value="${tr.revisionNumber}" />
									<input type="hidden" name="labNo" id="labNo${commonIndex }" value="${tr.test.labno}" />
								</td>
								<td>
								  <c:choose>
								    <c:when test="${tr.condctionStatus ne 'N'|| tr.isTemplatethere eq 'Y'}">
									   <input type="hidden" name="formatid" value="${tr.template.templateId}">
									   <div style="display:${testsEditable == 1 ?'block':'none'}">
										   		 <a href="javascript:void(0)" onclick="return openwindow(this,'${tr.template.templateId}','${tr.prescribedId}','${tr.test.testId}','${tr.reportFormatDetails.map.test_details_id }', '${tr.revisionNumber}')">
											 	${tr.template.templateName}
											 </a>
									   </div>
									   <div style="display:${testsEditable == 0 ?'block':'none'}">
									   		 <a href="javascript:void(0)" onclick="return false">
											 	${tr.template.templateName}
											 </a>
									   </div>
									 </c:when>
									 <c:otherwise>
										<c:choose>
										    <c:when test="${fn:length(tr.newTemplates) eq 0}">
												<insta:ltext key="laboratory.testconduction.list.template.unavailable"/>
												<input type="hidden" name="formatid" value="${tr.newTemplates[0].templateId}">
										    </c:when>
											<c:when test="${fn:length(tr.newTemplates) gt 1}">
							   			     <select name="formatid" style="width:20em" class="dropdown"
												  onchange="openwindow(this,this.value,${tr.prescribedId},'${tr.test.testId}', '0', '${tr.revisionNumber}')">
												<option value=""><insta:ltext key="laboratory.testconduction.list.selectnewtemplatetest"/></option>
												<c:forEach var="item" items="${tr.newTemplates}">
												   <option value="${item.templateId}">${item.templateName}</option>
												</c:forEach>
			  								 </select>
											</c:when>
											<c:otherwise>
											  <a href="javascript:void(0)" onclick="return openwindow(this,'${tr.newTemplates[0].templateId}','${tr.prescribedId}','${tr.test.testId}', '0', '${tr.revisionNumber}')">
											 		${tr.newTemplates[0].templateName}
											  </a>
											  <input type="hidden" name="formatid" value="${tr.newTemplates[0].templateId}">
											</c:otherwise>
										</c:choose>
									 </c:otherwise>
								  </c:choose>
								</td>
								<td>
									<c:if test="${(canAmend == 'Y' ||
														ifn:contains(amendableStatusBean,tr.condctionStatus)
														&& amendTestResultsRt)}">
										<c:choose>
											<c:when test="${tr.reportFormatDetails.map.original_test_details_id == null ||
												tr.reportFormatDetails.map.original_test_details_id == 0 ||
												tr.reportFormatDetails.map.test_detail_status eq 'S' ||
												tr.reportFormatDetails.map.test_detail_status eq 'A'}">
												<input type="button"  value="Amend" name="amendBtn"
													onclick="askAmendmentReason(this,'T',${commonIndex });"
													${(tr.reportFormatDetails.map.revised_test_details_id == null
														||tr.reportFormatDetails.map.revised_test_details_id == 0) ?'': 'disabled'}/>
												<a href="javascript:void(0)"
														onclick='getAmendemntReasonDialog(this,${commonIndex },false);'
														title='<insta:ltext key="laboratory.testconduction.list.amendmentreason"/>'>
														<img src="${cPath}/icons/AddBlue.png" class="button" />
													</a>
											</c:when>
											<c:otherwise>
												<input type="button"  value="Delete" name="amendBtn"
													onclick="deleteRow(this);"/>
												<input type="hidden" name="newResultAfterAmendment" id="newResultAfterAmendment" value="Y"/>
											</c:otherwise>
										</c:choose>
									</c:if>
									<input type="hidden" name="amendment_reason" id="amendment_reason${commonIndex }" value="${results.amendment_reason }"/>
								</td>
								<%--Choose Another Template--%>
								<td>
								<input type="hidden" name="showAnotherTemplate" value="${fn:length(tr.newTemplates) ge 1  ? 'listTemplates' : 'linkTemplate'}"/>
									<div style="display:${(not empty tr.newTemplates) and (tr.condctionStatus eq 'P' || tr.condctionStatus eq 'MA' || tr.condctionStatus eq 'CC'|| tr.condctionStatus eq 'TS' || tr.condctionStatus eq 'RP')|| tr.isTemplatethere eq 'Y' ? 'block' : 'none' }">
										<div style="display:${fn:length(tr.newTemplates) ge 1 ? 'block' : 'none'}" id="listTemplates">
											<select name="newformatid" style="width:20em" class="dropdown"
												  onchange="openwindow(this,this.value,${tr.prescribedId},'${tr.test.testId}','${tr.reportFormatDetails.map.test_details_id }', '${tr.revisionNumber}')">
												<option value=""><insta:ltext key="laboratory.testconduction.list.selectnewtemplatetest"/></option>
												<c:forEach var="item" items="${tr.newTemplates}">
												   <option value="${item.templateId}">${item.templateName}</option>
												</c:forEach>
			  								 </select>
										</div>
										<div style="display:${fn:length(tr.newTemplates) ge 1 ? 'none' : 'block'}" id="linkTemplate">
											<a href="javascript:void(0)" onclick="return openwindow(this,'${tr.newTemplates[0].templateId}','${tr.prescribedId}','${tr.test.testId}','${tr.reportFormatDetails.map.test_details_id }', '${tr.revisionNumber}')">
										 		${tr.newTemplates[0].templateName}
										  </a>
										</div>
									</div>
									<input type="hidden" name="testid" value="${tr.test.testId}" />
									<html:hidden property="testname" value="${tr.test.testName}" />
									<input type="hidden" name="rtestId" value="${tr.test.testId}" />
									<input type="hidden" name="rprescribedId" value="${tr.prescribedId}" />
									<input type="hidden" name="rrevisionNumber" value="${tr.revisionNumber}" />
									<input type="hidden" name="resultlabel" value="" />
									<input type="hidden" name="resultlabel_id" id="resultlabel_id"/>
									<input type="hidden" name="method_id" id="method_id" value="" />
									<input type="hidden" name="resultvalue" value="" />
									<input type="hidden" name="calc_res_expr" value=""/>
									<input type="hidden" name="units" value="" />
									<input type="hidden" name="referenceRanges" value="" />
									<input type="hidden" name="remarks" value="" />
									<input type="hidden" name="conductedinreportformat" value="Y" />
									<input type="hidden" name="ddeptid" id="ddeptid${commonIndex}" value="${tr.test.ddeptId}" />
									<input type="hidden" name="h_technician" id="h_technician${commonIndex}" value="${tr.test.technician}"/>
									<input type="hidden" name="withinNormal" id="withinNormal${rincr}" value="Y">
									<input type="hidden" name="seviarity" id="seviarity${rincr}" value="Y"/>
									<input type="hidden" name="resultDisclaimer" value=""/>
									<input type="hidden" name="test_details_id" value="${tr.reportFormatDetails.map.test_details_id }"/>
									<input type="hidden" name="deleted_new_test_details_id" value=""/>
									<input type="hidden" name="deleted" value="N"/>
									<input type="hidden" name="revised_test_details_id" value="${tr.reportFormatDetails.map.revised_test_details_id }"/>
									<input type="hidden" name="original_test_details_id" value="${tr.reportFormatDetails.map.original_test_details_id }"/>
									<input type="hidden" name="test_detail_status" value="${tr.reportFormatDetails.map.test_detail_status }"/>
									<input type="hidden" name="sampleNo" value="${tr.test.sampleNo}" />
									<input type="hidden" name="commonIndex" value="${commonIndex}"/>
									<input type="hidden" name="dum_completed" value="NA" />
									<input type="hidden"  name="dum_resultvalue"/>
									<input type="hidden" name="expression" id="expression" value=""/>
									<input type="hidden" name="newResult" value="N"/>
									<c:set var="rincr" value="${rincr+1}" />
								</td>
							</tr>
							<c:if test="${not empty tr.amendedTemplates }">
							<c:forEach items="${tr.amendedTemplates}" var="amendedTemplate">
								<tr style="text-decoration: line-through;">
									<td colspan="${tr.reagentsexist && reagenetsExists_T eq 'Y' && !ifn:contains(amendableStatusBean,tr.condctionStatus) ? 3 : 2}">&nbsp;
										<input type="hidden" name="conducting_doc_mandatory" id="conducting_doc_mandatory${incr}"
													value="${tr.test.conducting_doc_mandatory}">
										<input type="hidden" name="dateOfInvestigation" id="dateOfInvestigation${commonIndex }" value="${empty tr.test.testDate ? thisDateVal : tr.test.testDate }"/>
										<input type="hidden" name="timeOfInvestigation" id="timeOfInvestigation${commonIndex }" value="${empty tr.test.testTime ? thisTimeVal : tr.test.testTime}"/>
										<input type="hidden" name="doctor" id="doctor${commonIndex }" value="${tr.test.conductedDoctor }"/>
										<input type="hidden" name="sampleno" id="sampleno${commonIndex }" value="${tr.test.sampleNo }"/>
										<input type="hidden" name="dateOfSample" id="dateOfSample${commonIndex }"  value="${tr.test.sampleDate }"/>
										<input type="hidden" name="specimen_condition" id="specimen_condition${commonIndex }" value="${tr.test.specimenCondition }"/>
										<input type="hidden" size="40" name="testRemarks" id="testRemarks${commonIndex }" value="${tr.test.remarks}"/>
										<input type="hidden" name="completed" value="${defaultCondutionStatus }"/>
										<input type="hidden" name="prescribedid" value="" />
										<input type="hidden" name="revisionNumber" value="${tr.revisionNumber}" />
										<input type="hidden" name="labNo" id="labNo${commonIndex }" value="${tr.test.labno}" />
									</td>
									<td>
									    <input type="hidden" name="formatid" value="${amendedTemplate.templateId}">
								   		 <a href="javascript:void(0)" onclick="return false">
											 	${amendedTemplate.templateName}
										 </a>
									</td>
									<td>
										<input type="button"  value="Amend" name="amendBtn"
											onclick="askAmendmentReason(this,'T',${commonIndex });"
											disabled/>
										<a href="javascript:void(0)"
										onclick='getAmendemntReasonDialog(this,${commonIndex },false);'
										title='<insta:ltext key="laboratory.testconduction.list.amendmentreason"/>'>
										<img src="${cPath}/icons/AddBlue.png" class="button" />
									</a>
									<input type="hidden" name="amendment_reason" id="amendment_reason${commonIndex }" value="${amendedTemplate.amendmentReason }"/>
								</td>
								<td colspan="2">
									<input type="hidden" name="testid" value="${tr.test.testId}" />
									<html:hidden property="testname" value="${tr.test.testName}" />
									<input type="hidden" name="rtestId" value="${tr.test.testId}" />
									<input type="hidden" name="rprescribedId" value="${tr.prescribedId}" />
									<input type="hidden" name="rrevisionNumber" value="${tr.revisionNumber}" />
									<input type="hidden" name="resultlabel" value="" />
									<input type="hidden" name="resultlabel_id" id="resultlabel_id"/>
									<input type="hidden" name="method_id" id="method_id" value="" />
									<input type="hidden" name="resultvalue" value="" />
									<input type="hidden" name="calc_res_expr" value=""/>
									<input type="hidden" name="units" value="" />
									<input type="hidden" name="referenceRanges" value="" />
									<input type="hidden" name="remarks" value="" />
									<input type="hidden" name="conductedinreportformat" value="Y" />
									<input type="hidden" name="ddeptid" id="ddeptid${commonIndex}" value="${tr.test.ddeptId}" />
									<input type="hidden" name="h_technician" id="h_technician${commonIndex}" value="${tr.test.technician}"/>
									<input type="hidden" name="withinNormal" id="withinNormal${rincr}" value="Y">
									<input type="hidden" name="seviarity" id="seviarity${rincr}" value="Y"/>
									<input type="hidden" name="resultDisclaimer" value=""/>
									<input type="hidden" name="test_details_id" value="${tr.reportFormatDetails.map.test_details_id }"/>
									<input type="hidden" name="deleted_new_test_details_id" value=""/>
									<input type="hidden" name="deleted" value="N"/>
									<input type="hidden" name="revised_test_details_id" value="${tr.reportFormatDetails.map.revised_test_details_id }"/>
									<input type="hidden" name="original_test_details_id" value="${tr.reportFormatDetails.map.original_test_details_id }"/>
									<input type="hidden" name="test_detail_status" value="${tr.reportFormatDetails.map.test_detail_status }"/>
									<input type="hidden" name="sampleNo" value="${tr.test.sampleNo}" />
									<input type="hidden" name="commonIndex" value="${commonIndex}"/>
									<input type="hidden" name="dum_completed" value="NA" />
									<input type="hidden" name="newResult" value="N"/>
									<input type="hidden"  name="dum_resultvalue"/>
									<input type="hidden" name="expression" id="expression" value=""/>
									<c:set var="rincr" value="${rincr+1}" />
								</td>
							</tr>
							</c:forEach>
							</c:if>
							<c:set var="commonIndex" value="${commonIndex+1}"/>
						</c:forEach>
					</table>
					</div>
				</c:if> <%-- Test with Report format END --%>
				<table  style="margin-bottom: 6px"></table>
				<c:if test="${not empty histoTests}">
				<c:forEach var="tr" items="${histoTests}">
					<c:if test="${not empty tr.test.outSourceDestPresId && !isTransferedTest}">
							<c:set var="isTransferedTest" value="true" />
					</c:if>
				<c:set var="defaultCondutionStatus" value="${tr.condctionStatus ne 'N' ? tr.condctionStatus : 'P' }"/>
				<c:if test="${param.category == 'DEP_RAD'}">
					     <c:set var="firstrowstatus" value="${conductionstates[0].value}"/>
					    <c:set var="defaultCondutionStatus" value="${tr.condctionStatus ne 'N' ? tr.condctionStatus : firstrowstatus}"/>
					    </c:if>
					<div >
						<table class="detailList">
							<tr>
								<th><insta:ltext key="laboratory.testconduction.list.test"/></th>
								<th><insta:ltext key="laboratory.testconduction.list.conductionstatus"/></th>
							</tr>
							<tr>
								<td>${tr.test.testName}
						     		<a onclick="showTestDetails(this,${tr.prescribedId},${commonIndex},'${tr.test.sampleNo }');" href="javascript:void(0)"
						     			name="loadDialog" id="loadDialog">
									<img src="${cPath}/icons/Edit.png" class="button" title='<insta:ltext key="laboratory.testconduction.list.addedittestdetails"/>'/>
									</a>
									<c:url value="TestDocumentsList.do" var="testDocumentsUrl">
										<c:param name="_method" value="searchTestDocuments"/>
										<c:param name="prescribed_id" value="${tr.prescribedId}"/>
										<c:param name="reportId" value="${param.reportId}"/>
									</c:url>
									<c:if test="${category == 'DEP_LAB' ? urlRightsMap.lab_test_documents == 'A' : urlRightsMap.rad_test_documents == 'A'}">
										|<a href="<c:out value='${testDocumentsUrl}'/>" title='<insta:ltext key="laboratory.testconduction.list.addeditviewtestdocuments"/>'>
											${tr.hasDocuments ? 'Edit Doc' : 'Add Document'}
										</a>
									</c:if>
									<c:if test="${tr.mandate_additional_info == 'O'}">
										<c:url value="/Diagnostics/TestInfoViewer.do" var="testInfoUrl">
											<c:param name="_method" value="list"/>
											<c:param name="prescribed_id" value="${tr.prescribedId}"/>
											<c:param name="patient_id" value="${patientvisitdetails.map.patient_id}"/>
										</c:url>
										|<a href="${testInfoUrl}" title="Test Information Viewer" target="_blank">
											Test Info Viewer
										</a>
										<br/>
									</c:if>

									<input type="hidden" name="conducting_doc_mandatory" id="conducting_doc_mandatory${incr}"
											value="${tr.test.conducting_doc_mandatory}">
									<input type="hidden" name="dateOfInvestigation" id="dateOfInvestigation${commonIndex }" value="${empty tr.test.testDate ? thisDateVal : tr.test.testDate }"/>
									<input type="hidden" name="timeOfInvestigation" id="timeOfInvestigation${commonIndex }" value="${empty tr.test.testTime ? thisTimeVal : tr.test.testTime}"/>
									<input type="hidden" name="doctor" id="doctor${commonIndex }" value="${tr.test.conductedDoctor }"/>
									<input type="hidden" name="sampleno" id="sampleno${commonIndex }" value="${tr.test.sampleNo }"/>
									<input type="hidden" name="dateOfSample" id="dateOfSample${commonIndex }"  value="${tr.test.sampleDate }"/>
									<input type="hidden" name="specimen_condition" id="specimen_condition${commonIndex }" value="${tr.test.specimenCondition }"/>
									<input type="hidden" size="40" name="testRemarks" id="testRemarks${commonIndex }" value="${tr.test.remarks}"/>
									<input type="hidden" name="labNo" id="labNo${commonIndex }" value="${tr.test.labno}" />
								</td>
								<td>
									<select name="dum_completed" class="dropdown" style="width:8em" onchange="setCompletedStatus(this)">
									<c:choose>
									  <c:when test="${param.category == 'DEP_RAD'}">
											 <c:forEach var="states" items="${stateMap[tr.prescribedId]}">
												<option value="${states.map.value}" ${states.map.value == defaultCondutionStatus ? 'selected' : ''}>${states.map.display_name}
												</option>
											</c:forEach>
														<c:set var="testsReconducted" value="N"/>
									  </c:when>
									  <c:otherwise>
											<option value="P"
												${defaultCondutionStatus eq 'P'? 'selected' :''}><insta:ltext key="laboratory.testconduction.list.inprogress"/></option>
											<option value="C"
												${defaultCondutionStatus eq 'C'? 'selected' :''}><insta:ltext key="laboratory.testconduction.list.completed"/></option>
											<c:if test="${validateResultsRt }">
												<option value="V"
													${defaultCondutionStatus eq 'V'? 'selected' :''}><insta:ltext key="laboratory.testconduction.list.validated"/></option>
											</c:if>
											<option value="S"
													${defaultCondutionStatus eq 'S'? 'selected' :''}><insta:ltext key="laboratory.testconduction.list.signedoff"/></option>
										</c:otherwise>
									</c:choose>
									</select>
									<c:set var="allCompleted"
												value="${allCompleted ne '' &&
													(tr.condctionStatus eq 'C' || tr.condctionStatus eq 'V'
														|| tr.condctionStatus eq 'RC' || tr.condctionStatus eq 'RV'
														|| tr.condctionStatus eq 'S'
														|| tr.condctionStatus eq 'RBS' || tr.condctionStatus eq 'RAS') ? 'checked disabled' : ''}"/>
									<c:set var="allVerified"
												value="${allVerified ne '' && tr.condctionStatus eq 'V'
														|| tr.condctionStatus eq 'RV'
														|| tr.condctionStatus eq 'S'
														|| tr.condctionStatus eq 'RBS' || tr.condctionStatus eq 'RAS'  ? 'checked disabled' : ''}"/>
									<input type="hidden" name="completed" value="${defaultCondutionStatus }"/>
								</td>
							</tr>
						</table>
						<fieldset class="fieldSetBorder" style="margin-bottom: 3px">
							<table cellspacing="0" cellpadding="0" width="100%" class="formtable">
								<tr>
									<td>
										<table>
											<tr>
												<td class="formlabel"><insta:ltext key="laboratory.testconduction.list.noofblocks"/>:</td>
												<td>
													<input type="text"  class="number" name="no_of_blocks" value="${tr.impressionDetails.map.no_of_blocks}"/>
													<input type="hidden" name="hprescribed_id" value="${tr.prescribedId}"/>
													<input type="hidden" name='mr_no' value="${patientvisitdetails.map.mr_no}">
													<input type="hidden" name='patient_id' value="${patientvisitdetails.map.patient_id}">
													<input type="hidden" name='test_id' value="${tr.test.testId}">
													<input type="hidden" name="testid" value="${tr.test.testId}" />
													<html:hidden property="testname" value="${tr.test.testName}" />
													<input type="hidden" name="prescribedid" value="${tr.prescribedId}" />
													<input type="hidden" name="revisionNumber" value="${tr.revisionNumber}" />
													<input type="hidden" name="rtestId" value="${tr.test.testId}" />
													<input type="hidden" name="rprescribedId" value="${tr.prescribedId}" />
													<input type="hidden" name="rrevisionNumber" value="${tr.revisionNumber}" />
													<input type="hidden" name="resultlabel" value="" />
													<input type="hidden" name="resultlabel_id" id="resultlabel_id"/>
													<input type="hidden" name="method_id" id="method_id" value="" />
													<input type="hidden" name="resultvalue" value="" />
													<input type="hidden" name="calc_res_expr" value=""/>
													<input type="hidden" name="units" value="" />
													<input type="hidden" name="referenceRanges" value="" />
													<input type="hidden" name="remarks" value="" />
													<input type="hidden" name="conductedinreportformat" value="H" id="conductedinreportformat${impessionIndex}"/>
													<input type="hidden" name="ddeptid" id="ddeptid${commonIndex}" value="${tr.test.ddeptId}" />
													<input type="hidden" name="h_technician" id="h_technician${commonIndex}" value="${tr.test.technician}"/>
													<input type="hidden" name="withinNormal" id="withinNormal${rincr}" value="Y">
													<input type="hidden" name="seviarity" id="seviarity${rincr}" value="Y"/>
													<input type="hidden" name="resultDisclaimer" value="${ifn:cleanHtmlAttribute(results.resultDisclaimer)}"/>
													<input type="hidden" name="test_details_id" value="${results.test_details_id }"/>
													<input type="hidden" name="deleted_new_test_details_id" value=""/>
													<input type="hidden" name="deleted" value="N"/>
													<input type="hidden" name="revised_test_details_id" value="${results.revised_test_details_id }"/>
													<input type="hidden" name="original_test_details_id" value="${results.original_test_details_id }"/>
													<input type="hidden" name="test_detail_status" value="${results.test_detail_status }"/>
													<input type="hidden" name="amendment_reason" id="amendment_reason${commonIndex }" value="${results.amendment_reason }"/>
													<input type="hidden" name="sampleNo" value="${tr.test.sampleNo}" />
													<input type="hidden" name="formatid" value="" />
													<input type="hidden" name="reporttemplate" value="" />
													<input type="hidden" name="commonIndex" value="${commonIndex}"/>
												</td>
												<td class="formlabel"><insta:ltext key="laboratory.testconduction.list.blockdescription"/> :</td>
												<td>
													<input type="text" name="block_description" maxlength="100"
														value="${tr.impressionDetails.map.block_description}"/>
												</td>
											</tr>
											<tr>
												<td class="formlabel"><insta:ltext key="laboratory.testconduction.list.noofslides"/>:</td>
												<td>
													<input type="text" name="no_of_slides"
														class="number" value="${tr.impressionDetails.map.no_of_slides}"/>
												</td>
												<td class="formlabel"><insta:ltext key="laboratory.testconduction.list.slidedescription"/> :</td>
												<td>
													<input type="text" name="slide_description" maxlength="100"
														value="${tr.impressionDetails.map.slide_description}"/>
												</td>
											</tr>
									</table>
								</td>
								</tr>
								<tr>
									<td>
									<table>
										<tr>
											<td class="formlabel"><insta:ltext key="laboratory.testconduction.list.shortimpression"/>:</td>
											<td>
												<div id="impresAutocomplete${impessionIndex}" style="padding-bottom: 20px;float:left;width=160px;">
													<input type="text" name="impression${impessionIndex}" id="impression${impessionIndex}"
														style="width: 155px" value="${tr.impressionDetails.map.short_impression }"/>
													<input type="hidden" name="impression_id" id="impression_id${impessionIndex}" value="${tr.impressionDetails.map.impression_id }"/>
													<input type="hidden" name="histo_impression_id" id="histo_impression_id${impessionIndex}" value="${tr.impressionDetails.map.impression_id }"/>
													<div id="impresContainer${impessionIndex}" style="width: 500px;"></div>
												</div>
												<div>
													<div style="height:4px" id="showAlertDialog${impessionIndex}"></div>
													<a  href="javascript:void(0)" onclick="doClose(${impessionIndex});"><insta:ltext key="laboratory.testconduction.list.addtomasterlist"/></a>
												</div>
											</td>
										</tr>
										<tr>
											<td class="formlabel"><insta:ltext key="laboratory.testconduction.list.impression"/> :</td>
											<td>
												<textarea  cols="70" name="impression_details" id="impressionDetails${impessionIndex}">${tr.impressionDetails.map.histo_impression_details }</textarea>
											</td>
										</tr>
										<tr>
											<td class="formlabel"><insta:ltext key="laboratory.testconduction.list.clinicaldetails"/> :</td>
											<td>
												<textarea cols="90" rows="8" name="clinical_details" onblur="checkLength(this,5000,'Clinical Details')">${tr.impressionDetails.map.clinical_details}</textarea>
											</td>
										</tr>
										<tr>
											<td class="formlabel"><insta:ltext key="laboratory.testconduction.list.grossdetails"/> :</td>
											<td>
												<textarea cols="90" rows="8" name="micro_gross_details" onblur="checkLength(this,5000,'Gross Details')">${tr.impressionDetails.map.micro_gross_details }</textarea>
											</td>
										</tr>
										<tr>

											<td class="formlabel"><insta:ltext key="laboratory.testconduction.list.microscopic.grossdetails"/> :</td>
											<td>
												<textarea cols="90" rows="8" name="micro_gross_details" onblur="checkLength(this,5000,'Microscopic Gross Details')">${tr.impressionDetails.map.micro_gross_details }</textarea>
											</td>
										</tr>
										</table>
										</td>

							</table>
						</fieldset>
						<c:set var="impessionIndex" value="${ impessionIndex+1 }"/>
					</div>
					<c:set var="commonIndex" value="${commonIndex+1}"/>
					</c:forEach>
				</c:if>

				<c:if test="${ not empty microTests}">
				<c:forEach var="tr" items="${microTests}">
					<c:if test="${not empty tr.test.outSourceDestPresId && !isTransferedTest}">
							<c:set var="isTransferedTest" value="true" />
					</c:if>
				<c:set var="defaultCondutionStatus" value="${tr.condctionStatus ne 'N' ? tr.condctionStatus : 'P' }"/>
				<c:if test="${param.category == 'DEP_RAD'}">
					     <c:set var="firstrowstatus" value="${conductionstates[0].value}"/>
					    <c:set var="defaultCondutionStatus" value="${tr.condctionStatus ne 'N' ? tr.condctionStatus : firstrowstatus}"/>
					    </c:if>
					<div >
						<table class="detailList">
							<tr>
								<th><insta:ltext key="laboratory.testconduction.list.test"/></th>
								<th><insta:ltext key="laboratory.testconduction.list.conductionstatus"/></th>
							</tr>
							<tr>
								<td>
									${tr.test.testName}
						     		<a onclick="showTestDetails(this,${tr.prescribedId},${commonIndex},'${tr.test.sampleNo }');" href="javascript:void(0)"
						     			name="loadDialog" id="loadDialog">
									<img src="${cPath}/icons/Edit.png" class="button" title='<insta:ltext key="laboratory.testconduction.list.addedittestdetails"/>'/>
									</a>
									<c:url value="TestDocumentsList.do" var="testDocumentsUrl">
										<c:param name="_method" value="searchTestDocuments"/>
										<c:param name="prescribed_id" value="${tr.prescribedId}"/>
										<c:param name="reportId" value="${param.reportId}"/>
									</c:url>
									<c:if test="${category == 'DEP_LAB' ? urlRightsMap.lab_test_documents == 'A' : urlRightsMap.rad_test_documents == 'A'}">
										<a href="<c:out value='${testDocumentsUrl}'/>" title='<insta:ltext key="laboratory.testconduction.list.addeditviewtestdocuments"/>'>
											${tr.hasDocuments ? 'Edit Doc' : 'Add Document'}
										</a>
									</c:if>
									<c:if test="${tr.mandate_additional_info == 'O'}">
										<c:url value="/Diagnostics/TestInfoViewer.do" var="testInfoUrl">
											<c:param name="_method" value="list"/>
											<c:param name="prescribed_id" value="${tr.prescribedId}"/>
											<c:param name="patient_id" value="${patientvisitdetails.map.patient_id}"/>
										</c:url>
										|<a href="${testInfoUrl}" title="Test Information Viewer" target="_blank">
											Test Info Viewer
										</a>
										<br/>
									</c:if>

									<input type="hidden" name="conducting_doc_mandatory" id="conducting_doc_mandatory${incr}"
										value="${tr.test.conducting_doc_mandatory}">
									<input type="hidden" name="dateOfInvestigation" id="dateOfInvestigation${commonIndex }" value="${empty tr.test.testDate ? thisDateVal : tr.test.testDate }"/>
									<input type="hidden" name="timeOfInvestigation" id="timeOfInvestigation${commonIndex }" value="${empty tr.test.testTime ? thisTimeVal : tr.test.testTime}"/>
									<input type="hidden" name="doctor" id="doctor${commonIndex }" value="${tr.test.conductedDoctor }"/>
									<input type="hidden" name="sampleno" id="sampleno${commonIndex }" value="${tr.test.sampleNo }"/>
									<input type="hidden" name="dateOfSample" id="dateOfSample${commonIndex }"  value="${tr.test.sampleDate }"/>
									<input type="hidden" name="specimen_condition" id="specimen_condition${commonIndex }" value="${tr.test.specimenCondition }"/>
									<input type="hidden" name="labNo" id="labNo${commonIndex }" value="${tr.test.labno}" />
								</td>
								<td>
									<select name="dum_completed" class="dropdown" style="width:8em" onchange="setCompletedStatus(this)">
									<c:choose>
									  <c:when test="${param.category == 'DEP_RAD'}">
											<c:forEach var="states" items="${stateMap[tr.prescribedId]}">
												<option value="${states.map.value}" ${states.map.value == defaultCondutionStatus ? 'selected' : ''}>${states.map.display_name}
													</option>
													</c:forEach>
														<c:set var="testsReconducted" value="N"/>
										 </c:when>
									  <c:otherwise>
											<option value="P"
												${defaultCondutionStatus eq 'P'? 'selected' :''}><insta:ltext key="laboratory.testconduction.list.inprogress"/></option>
											<option value="C"
												${defaultCondutionStatus eq 'C'? 'selected' :''}><insta:ltext key="laboratory.testconduction.list.completed"/></option>
											<c:if test="${validateResultsRt }">
												<option value="V"
													${defaultCondutionStatus eq 'V'? 'selected' :''}><insta:ltext key="laboratory.testconduction.list.validated"/></option>
											</c:if>
											<option value="S"
													${defaultCondutionStatus eq 'V'? 'selected' :''}><insta:ltext key="laboratory.testconduction.list.signedoff"/></option>
										</c:otherwise>
									</c:choose>
									</select>
									<c:set var="allCompleted"
												value="${allCompleted ne '' &&
														(tr.condctionStatus eq 'C' || tr.condctionStatus eq 'V'
														|| tr.condctionStatus eq 'RC' || tr.condctionStatus eq 'RV'
														|| tr.condctionStatus eq 'S'
														|| tr.condctionStatus eq 'RBS' || tr.condctionStatus eq 'RAS'  ) ? 'checked disabled' : ''}"/>
									<c:set var="allVerified"
												value="${allVerified ne '' && tr.condctionStatus eq 'V'
														|| tr.condctionStatus eq 'RV'
														|| tr.condctionStatus eq 'S'
														|| tr.condctionStatus eq 'RBS' || tr.condctionStatus eq 'RAS'  ? 'checked disabled' : ''}"/>
									<input type="hidden" name="completed" value="${defaultCondutionStatus }"/>
								</td>
							</tr>
						</table>
						<div id="CollapsiblePanel1${noGrowthIndex}" class="CollapsiblePanel ">
						<div class=" title CollapsiblePanelTab"  style=" border-left:none;" >
							<div class="fltL " style="width: 230px; margin:5px 0 0 10px;">
								<insta:ltext key="laboratory.testconduction.list.nogrowth"/>
							</div>
							<div class="fltR txtRT" style="width: 25px; margin:10px 0 0 680px; padding-right:0px;">
								<img src="${cPath}/images/down.png" />
							</div>
							<div class="clrboth"></div>
						</div>
						<div class="CollapsiblePanelContentNoBkClr" >
							<fieldset class="fieldSetBorder">
							<table cellspacing="0" cellpadding="0" width="100%" class="formtable">
								<tr>
									<td class="formlabel"><insta:ltext key="laboratory.testconduction.list.confirm.nogrowth"/> :</td>
									<td align="left">
										<input type="checkbox" name="growth_exists" id="growth_exists_N${noGrowthIndex}" value="N"
											${!tr.microDetails.map.growth_exists ? 'checked' : '' }
											onchange="validateGrowth(this,${noGrowthIndex})"/>
										<input type="hidden" name="mprescribed_id" value="${tr.prescribedId}"/>
										<input type="hidden" name='mr_no' value="${patientvisitdetails.map.mr_no}">
										<input type="hidden" name='patient_id' value="${patientvisitdetails.map.patient_id}">
										<input type="hidden" name='test_id' value="${tr.test.testId}">
										<input type="hidden" name="testid" value="${tr.test.testId}" />
										<html:hidden property="testname" value="${tr.test.testName}" />
										<input type="hidden" name="prescribedid" value="${tr.prescribedId}" />
										<input type="hidden" name="revisionNumber" value="${tr.revisionNumber}" />
										<input type="hidden" name="rtestId" value="${tr.test.testId}" />
										<input type="hidden" name="rprescribedId" value="${tr.prescribedId}" />
										<input type="hidden" name="rrevisionNumber" value="${tr.revisionNumber}" />
										<input type="hidden" name="resultlabel" value="" />
										<input type="hidden" name="resultlabel_id" id="resultlabel_id"/>
										<input type="hidden" name="method_id" id="method_id" value="" />
										<input type="hidden" name="resultvalue" value="" />
										<input type="hidden" name="calc_res_expr" value=""/>
										<input type="hidden" name="units" value="" />
										<input type="hidden" name="referenceRanges" value="" />
										<input type="hidden" name="remarks" value="" />
										<input type="hidden" name="conductedinreportformat" value="M"/>
										<input type="hidden" name="ddeptid" id="ddeptid${commonIndex}" value="${tr.test.ddeptId}" />
										<input type="hidden" name="h_technician" id="h_technician${commonIndex}" value="${tr.test.technician}"/>
										<input type="hidden" name="withinNormal" id="withinNormal${rincr}" value="Y">
										<input type="hidden" name="seviarity" id="seviarity${rincr}" value="Y"/>
										<input type="hidden" name="testRemarks" />
										<input type="hidden" name="resultDisclaimer" value="${ifn:cleanHtmlAttribute(results.resultDisclaimer)}"/>
										<input type="hidden" name="test_details_id" value="${results.test_details_id }"/>
										<input type="hidden" name="deleted_new_test_details_id" value=""/>
										<input type="hidden" name="deleted" value="N"/>
										<input type="hidden" name="revised_test_details_id" value="${results.revised_test_details_id }"/>
										<input type="hidden" name="original_test_details_id" value="${results.original_test_details_id }"/>
										<input type="hidden" name="test_detail_status" value="${results.test_detail_status }"/>
										<input type="hidden" name="amendment_reason" id="amendment_reason${commonIndex }" value="${results.amendment_reason }"/>
										<input type="hidden" name="sampleNo" value="${tr.test.sampleNo}" />
										<input type="hidden" name="formatid" value="" />
										<input type="hidden" name="reporttemplate" value="" />
										<input type="hidden" name="commonIndex" value="${commonIndex}"/>
									</td>
									<td></td>
								</tr>
								<tr>
									<td class="formlabel"><insta:ltext key="laboratory.testconduction.list.nogrowth.template"/> :</td>
									<td align="left">
										<div id="nogrowthTemplateAC${noGrowthIndex}" style="padding-bottom: 20px">
											<input type="text" name="nogrowth_template${noGrowthIndex}"
												id="nogrowth_template${noGrowthIndex}"
												style="width: 100px" value="${tr.microDetails.map.nogrowth_template_name}"/>
											<div id="nogrowthTemplateContainer${noGrowthIndex}" style="width: 500px;"></div>
										</div>
										<input type="hidden" name="nogrowth_template_id"
											id="nogrowth_template_id${noGrowthIndex}"
											value="${tr.microDetails.map.nogrowth_template_id}"/>
									</td>
								</tr>
								<tr>
									<td class="formlabel"><insta:ltext key="laboratory.testconduction.list.reportcomment"/>:</td>
									<td  class="forminfo">
										<textarea name="nogrowth_report_comment" cols="90" rows="8" id="nogrowth_report_comment${noGrowthIndex}"><c:out  value="${tr.microDetails.map.nogrowth_report_comment}"/></textarea>
									</td>
								</tr>
							</table>
						</fieldset>
					</div>
					</div>
					<div id="CollapsiblePanel2${noGrowthIndex}" class="CollapsiblePanel ">
						<div class=" title CollapsiblePanelTab"  style=" border-left:none;" >
							<div class="fltL " style="width: 230px; margin:5px 0 0 10px;">
								<insta:ltext key="laboratory.testconduction.list.growthdetails"/>
							</div>
							<div class="fltR txtRT" style="width: 25px; margin:10px 0 0 680px; padding-right:0px;">
								<img src="${cPath}/images/down.png" />
							</div>
							<div class="clrboth"></div>
						</div>
						<div class="CollapsiblePanelContentNoBkClr" id="fieldSet${noGrowthIndex}">
							<fieldset class="fieldSetBorder" >
								<table cellspacing="0" cellpadding="0" width="100%" class="formtable">
									<tr>
										<td colspan="6">
											<table>
												<tr>
													<td class="formlabel" style="width: 150px;">
														&nbsp;&nbsp;&nbsp;<insta:ltext key="laboratory.testconduction.list.confirm.growth"/>  :</td>
													<td align="left">
														<input type="checkbox" name="growth_exists"
														 id="growth_exists_Y${noGrowthIndex}" value="Y"
														 ${tr.microDetails.map.growth_exists ? 'checked' : '' }
														 onchange="validateGrowth(this,${noGrowthIndex})"/>
													</td>
													<td class="formlabel"><insta:ltext key="laboratory.testconduction.list.colonycount"/> :</td>
													<td>
														<input type="text" name="colony_count" id="colony_count"
															class="number" value="${tr.microDetails.map.colony_count}"/>
														x 10^3/ml
														<input type="hidden" name="test_micro_id" value="${tr.microDetails.map.test_micro_id}"/>
													</td>
												</tr>
											</table>
										</td>
									</tr>
									<tr>
										<td>
											<fieldset class="fieldSetBorder">
												<legend class="fieldSetLabel" ><insta:ltext key="laboratory.testconduction.list.isolatedorganisms"/></legend>
												<table class="detailList" id="organismTable${noGrowthIndex}">
													<tr>
														<th><insta:ltext key="laboratory.testconduction.list.organismgroup"/></th>
														<th><insta:ltext key="laboratory.testconduction.list.organism"/></th>
														<th><insta:ltext key="laboratory.testconduction.list.abstpanel"/></th>
														<th><insta:ltext key="laboratory.testconduction.list.resistancemarker"/></th>
														<th><insta:ltext key="laboratory.testconduction.list.comment"/></th>
														<th><insta:ltext key="laboratory.testconduction.list.antibiotic"/></th>
														<th><insta:ltext key="laboratory.testconduction.list.results"/></th>
														<th><insta:ltext key="laboratory.testconduction.list.susceptibility"/></th>
													</tr>
													<c:set var="noofOrganisms" value="${fn:length(tr.microOrgDetails)}"/>
													<c:set var="microId" value="0"/>
													<c:forEach begin="0" end="${noofOrganisms }" var="i">
														<c:set var="organism" value="${tr.microOrgDetails[i]}"/>
															<tr style="${empty organism ? 'display:none' : ''}">
																<c:set var="microId" value="${organism.test_micro_id }"/>
																<td>${organism.org_group_name }
																	<input type="hidden" name="org_prescribed_id" value="${tr.prescribedId}"/>
																	<input type="hidden" name="antibioticRow" value="N">
																	<input type="hidden" name="org_group_id" value="${organism.org_group_id }"/>
																	<input type="hidden" name="organism_id" value="${organism.organism_id }"/>
																	<input type="hidden" name="abst_panel_id" value="${organism.abst_panel_id }"/>
																	<input type="hidden" name="comments" value="${organism.comments }"/>
																	<input type="hidden" name="resistance_marker" value="${organism.resistance_marker }"/>
																	<input type="hidden" name="micr_ant_results_id" value="${organism.micr_ant_results_id}"/>
																	<input type="hidden" name="test_org_group_id" value="${organism.test_org_group_id}"/>
																	<input type="hidden" name="anti_prescribed_id" value="${tr.prescribedId}"/>
																	<input type="hidden" name="antibiotic_name" value="${organism.antibiotic_name}"/>
																	<input type="hidden" name="antibiotic_id" value="${organism.antibiotic_id}"/>
																	<input type="hidden" name="anti_results" value="${organism.anti_results}"/>
																	<input type="hidden" name="susceptibility" value="${organism.anti_results}"/>
																	<input type="hidden" name="test_org_group_id" value="${organism.test_org_group_id }"/>
																	<input type="hidden" name="test_micro_id" value="${organism.test_micro_id }"/>

																</td>
																<td>${organism.organism_name }</td>
																<td>${organism.abst_panel_name }</td>
																<td>${organism.resistance_marker }</td>
																<td>
																	<insta:truncLabel value="${organism.comments }" length="32"/>
																</td>
																<%--<td>
																	 <c:forEach items="${organism.antibioticDetails}" var="antibiotic">
																			${antibiotic.map.antibiotic_name }
																			<input type="text" name="anti_results" value="${antibiotic.map.anti_results }"/>
																			<insta:selectoptions name="susceptibility" optexts="RESISTANT,INTERMEDIATE,SENSITIVE"
																				opvalues="R,I,S" dummyvalue="${select}" value="${antibiotic.map.susceptibility }" />
																			<br/>
																	</c:forEach>
																</td>--%>
																<td>&nbsp;</td>
																<td>&nbsp;</td>
																<td>&nbsp;</td>
															</tr>
													</c:forEach>
													</table>
													<table class="addButton">

													<tr>
														<td align="right">
															<button type="button" name="btnAddOrg" id="btnAddItem" title='<insta:ltext key="laboratory.testconduction.list.addneworganismdetails"/>'
																onclick="showOrganismDialog(this,${noGrowthIndex},'A')"
																accesskey="+" class="imgButton"><img src="${cPath}/icons/Add.png"></button>
														</td>
													</tr>
												</table>

										</fieldset>
										</td>
									</tr>
									<tr>
										<td >
											<fieldset class="fieldSetBorder">
												<table>
													<tr>
														<td class="formlabel" style="width:150px"><insta:ltext key="laboratory.testconduction.list.growthdetails"/> :</td>
														<td align="left" style="width:250px">
															<div id="growthTemplateAC${noGrowthIndex}" style="padding-bottom: 20px">
																<input type="text" name="nogrowth_template${noGrowthIndex}"
																	id="growth_template${noGrowthIndex}"
																	style="width: 100px" value="${tr.microDetails.map.growth_template_name}"/>
																<div id="growthTemplateContainer${noGrowthIndex}" style="width: 500px;"></div>
															</div>
															<input type="hidden" name="growth_template_id"
																id="growth_template_id${noGrowthIndex}"
																value="{tr.microDetails.map.growth_template_id}"/>
														</td>
													</tr>
													<tr>
														<td class="formlabel" style="width:150px"><insta:ltext key="laboratory.testconduction.list.reportcomment"/>:</td>
														<td align="left" style="width:205px">
															<textarea name="growth_report_comment" id="growth_report_comment${noGrowthIndex}">${tr.microDetails.map.growth_report_comment}</textarea>
														</td>
													</tr>
													<tr>
														<td class="formlabel"><insta:ltext key="laboratory.testconduction.list.microscopicdetails"/>:</td>
														<td >
															<textarea cols="90" rows="8" name="microscopic_details" onblur="checkLength(this,5000,'Microscopic Details')">${tr.microDetails.map.microscopic_details}</textarea>
														</td>
													<tr>
												</table>
											</fieldset>
										</td>
									</tr>

								</table>
							</fieldset>
						</div>

					</div>
						<c:set var="noGrowthIndex" value="${ noGrowthIndex+1 }"/>
					</div>
					<c:set var="commonIndex" value="${commonIndex+1}"/>
					</c:forEach>
				</c:if>

				<c:if test="${not empty cytoTests}">
				<c:forEach var="tr" items="${cytoTests}">
					<c:if test="${not empty tr.test.outSourceDestPresId && !isTransferedTest}">
							<c:set var="isTransferedTest" value="true" />
					</c:if>
				<c:set var="defaultCondutionStatus" value="${tr.condctionStatus ne 'N' ? tr.condctionStatus : 'P' }"/>
				<c:if test="${param.category == 'DEP_RAD'}">
					     <c:set var="firstrowstatus" value="${conductionstates[0].value}"/>
					    <c:set var="defaultCondutionStatus" value="${tr.condctionStatus ne 'N' ? tr.condctionStatus : firstrowstatus}"/>
					    </c:if>
					<div >
						<fieldset class="fieldSetBorder">
							<table class="detailList">
								<tr>
									<th><insta:ltext key="laboratory.testconduction.list.test"/></th>
									<th><insta:ltext key="laboratory.testconduction.list.conductionstatus"/></th>
								</tr>
								<tr>
									<td>
										${tr.test.testName}
							     		<a onclick="showTestDetails(this,${tr.prescribedId},${commonIndex},'${tr.test.sampleNo }');" href="javascript:void(0)"
							     			name="loadDialog" id="loadDialog">
										<img src="${cPath}/icons/Edit.png" class="button" title='<insta:ltext key="laboratory.testconduction.list.addedittestdetails"/>'/>
										</a>
										<c:url value="TestDocumentsList.do" var="testDocumentsUrl">
											<c:param name="_method" value="searchTestDocuments"/>
											<c:param name="prescribed_id" value="${tr.prescribedId}"/>
											<c:param name="reportId" value="${param.reportId}"/>
										</c:url>
										<c:if test="${category == 'DEP_LAB' ? urlRightsMap.lab_test_documents == 'A' : urlRightsMap.rad_test_documents == 'A'}">
											<a href="<c:out value='${testDocumentsUrl}'/>" title='<insta:ltext key="laboratory.testconduction.list.addeditviewtestdocuments"/>'>
												${tr.hasDocuments ? 'Edit Doc' : 'Add Document'}
											</a>
										</c:if>
										<c:if test="${tr.mandate_additional_info == 'O'}">
											<c:url value="/Diagnostics/TestInfoViewer.do" var="testInfoUrl">
												<c:param name="_method" value="list"/>
												<c:param name="prescribed_id" value="${tr.prescribedId}"/>
												<c:param name="patient_id" value="${patientvisitdetails.map.patient_id}"/>
											</c:url>
											|<a href="${testInfoUrl}" title="Test Information Viewer" target="_blank">
												Test Info Viewer
											</a>
											<br/>
										</c:if>
										<input type="hidden" name="conducting_doc_mandatory" id="conducting_doc_mandatory${incr}"
											value="${tr.test.conducting_doc_mandatory}">
										<input type="hidden" name="dateOfInvestigation" id="dateOfInvestigation${commonIndex }" value="${empty tr.test.testDate ? thisDateVal : tr.test.testDate }"/>
										<input type="hidden" name="timeOfInvestigation" id="timeOfInvestigation${commonIndex }" value="${empty tr.test.testTime ? thisTimeVal : tr.test.testTime}"/>
										<input type="hidden" name="doctor" id="doctor${commonIndex }" value="${tr.test.conductedDoctor }"/>
										<input type="hidden" name="sampleno" id="sampleno${commonIndex }" value="${tr.test.sampleNo }"/>
										<input type="hidden" name="dateOfSample" id="dateOfSample${commonIndex }"  value="${tr.test.sampleDate }"/>
										<input type="hidden" name="specimen_condition" id="specimen_condition${commonIndex }" value="${tr.test.specimenCondition }"/>
										<input type="hidden" size="40" name="testRemarks" id="testRemarks${commonIndex }" value="${tr.test.remarks}"/>
										<input type="hidden" name="labNo" id="labNo${commonIndex }" value="${tr.test.labno}" />

									</td>
									<td>
										<select name="dum_completed" class="dropdown" style="width:8em" onchange="setCompletedStatus(this)">
										<c:choose>
										  <c:when test="${param.category == 'DEP_RAD'}">
											 <c:forEach var="states" items="${stateMap[tr.prescribedId]}">
												<option value="${states.map.value}" ${states.map.value == defaultCondutionStatus ? 'selected' : ''}>${states.map.display_name}
												</option>
												</c:forEach>
												<c:set var="testsReconducted" value="N"/>
										  </c:when>
										  <c:otherwise>
												<option value="P"
													${defaultCondutionStatus eq 'P'? 'selected' :''}><insta:ltext key="laboratory.testconduction.list.inprogress"/></option>
												<option value="C"
													${defaultCondutionStatus eq 'C'? 'selected' :''}><insta:ltext key="laboratory.testconduction.list.completed"/></option>
												<c:if test="${validateResultsRt }">
													<option value="V"
														${defaultCondutionStatus eq 'V'? 'selected' :''}><insta:ltext key="laboratory.testconduction.list.validated"/></option>
												</c:if>
												<option value="S"
														${defaultCondutionStatus eq 'V'? 'selected' :''}><insta:ltext key="laboratory.testconduction.list.signedoff"/></option>
										</c:otherwise>
										</c:choose>
										</select>
										<c:set var="allCompleted"
												value="${allCompleted ne '' &&
													(tr.condctionStatus eq 'C' || tr.condctionStatus eq 'V'
														|| tr.condctionStatus eq 'RC' || tr.condctionStatus eq 'RV'
														|| tr.condctionStatus eq 'S'
														|| tr.condctionStatus eq 'RBS' || tr.condctionStatus eq 'RAS'  ) ? 'checked disabled' : ''}"/>
										<c:set var="allVerified"
												value="${allVerified ne '' && tr.condctionStatus eq 'V'
													|| tr.condctionStatus eq 'RV' || tr.condctionStatus eq 'S'
													|| tr.condctionStatus eq 'RBS' || tr.condctionStatus eq 'RAS'  ? 'checked disabled' : ''}"/>
										<input type="hidden" name="completed" value="${defaultCondutionStatus }"/>
									</td>
								</tr>
							</table>
						</fieldset>
						<fieldset class="fieldSetBorder" style="margin-bottom: 3px">
							<table cellspacing="0" cellpadding="0" width="100%" class="formtable">
								<tr>
									<td>
										<table>
											<tr>
												<td class="formlabel"><insta:ltext key="laboratory.testconduction.list.testtype"/>:</td>
												<td>
													<select class="dropdown" onchange="showSpecimanAdequecy(this,${impessionIndex})" name="test_type" id="test_type${impessionIndex};">
														<option value="">${select}</option>
														<option value="P" ${tr.cytoDetails.map.test_type == 'P'?'selected':''}><insta:ltext key="laboratory.testconduction.list.papsmear"/></option>
														<option value="F" ${tr.cytoDetails.map.test_type == 'F'?'selected':''}><insta:ltext key="laboratory.testconduction.list.fnac"/></option>
														<option value="T" ${tr.cytoDetails.map.test_type == 'T'?'selected':''}><insta:ltext key="laboratory.testconduction.list.thinprep"/></option>
													</select>
													<input type="hidden" name="cprescribed_id" value="${tr.prescribedId}"/>
													<input type="hidden" name='mr_no' value="${patientvisitdetails.map.mr_no}">
													<input type="hidden" name='patient_id' value="${patientvisitdetails.map.patient_id}">
													<input type="hidden" name='test_id' value="${tr.test.testId}">
													<input type="hidden" name="testid" value="${tr.test.testId}" />
													<html:hidden property="testname" value="${tr.test.testName}" />
													<input type="hidden" name="prescribedid" value="${tr.prescribedId}" />
													<input type="hidden" name="revisionNumber" value="${tr.revisionNumber}" />
													<input type="hidden" name="rtestId" value="${tr.test.testId}" />
													<input type="hidden" name="rprescribedId" value="${tr.prescribedId}" />
													<input type="hidden" name="rrevisionNumber" value="${tr.revisionNumber}" />
													<input type="hidden" name="resultlabel" value="" />
													<input type="hidden" name="resultlabel_id" id="resultlabel_id"/>
													<input type="hidden" name="method_id" id="method_id" value="" />
													<input type="hidden" name="resultvalue" value="" />
													<input type="hidden" name="calc_res_expr" value=""/>
													<input type="hidden" name="units" value="" />
													<input type="hidden" name="referenceRanges" value="" />
													<input type="hidden" name="remarks" value="" />
													<input type="hidden" name="conductedinreportformat" value="C" id="conductedinreportformat${impessionIndex}"/>
													<input type="hidden" name="ddeptid" id="ddeptid${commonIndex}" value="${tr.test.ddeptId}" />
													<input type="hidden" name="h_technician" id="h_technician${commonIndex}" value="${tr.test.technician}"/>
													<input type="hidden" name="withinNormal" id="withinNormal${rincr}" value="Y">
													<input type="hidden" name="seviarity" id="seviarity${rincr}" value="Y"/>
													<input type="hidden" name="resultDisclaimer" value="${ifn:cleanHtmlAttribute(results.resultDisclaimer)}"/>
													<input type="hidden" name="test_details_id" value="${results.test_details_id }"/>
													<input type="hidden" name="deleted_new_test_details_id" value=""/>
													<input type="hidden" name="deleted" value="N"/>
													<input type="hidden" name="revised_test_details_id" value="${results.revised_test_details_id }"/>
													<input type="hidden" name="original_test_details_id" value="${results.original_test_details_id }"/>
													<input type="hidden" name="test_detail_status" value="${results.test_detail_status }"/>
													<input type="hidden" name="amendment_reason" id="amendment_reason${commonIndex }" value="${results.amendment_reason }"/>
													<input type="hidden" name="sampleNo" value="${tr.test.sampleNo}" />
													<input type="hidden" name="formatid" value="" />
													<input type="hidden" name="reporttemplate" value="" />
													<input type="hidden" name="commonIndex" value="${commonIndex}"/>
												</td>
											</tr>
											<tr>
												<td class="formlabel"><insta:ltext key="laboratory.testconduction.list.specimenadequacy"/>:</td>
												<td class="forminput">
													<input type="text" name="specimen_adequacy" id="specimen_adequacy${impessionIndex}"
														 value="${tr.cytoDetails.map.specimen_adequacy}" style="width:300px;height:25px"/>
												</td>
												<td class="formlabel"><label id="adequecyId${impessionIndex}" style="${tr.cytoDetails.map.test_type == 'P' ? '' : 'display:none'}"><insta:ltext key="laboratory.testconduction.list.smearsreceived"/>:</td>
												<td id="textBoxId${impessionIndex}"  class="forminput">
													<input type="text" name="smear_received" id="smear_received${impessionIndex}" maxlength="100"
													style="${tr.cytoDetails.map.test_type == 'P' ? '' : 'display:none'};width:50px" value="${tr.cytoDetails.map.smear_received}"/>
												</td>
											</tr>
											<tr>
											<td class="formlabel"><insta:ltext key="laboratory.testconduction.list.shortimpression"/>:</td>
											<td>
												<div id="impresAutocomplete${impessionIndex}" style="padding-bottom: 20px;float:left; width:180px;">
													<input type="text" name="impression${impessionIndex}" id="impression${impessionIndex}"
														style="width: 150px" value="${(not empty cytoConduction && cytoConduction == 'Y') ? '' : tr.cytoDetails.map.short_impression }"/>
													<input type="hidden" name="impression_id" id="impression_id${impessionIndex}" value="${tr.cytoDetails.map.impression_id }"/>
													<input type="hidden" name="cyto_impression_id" id="cyto_impression_id${impessionIndex}" value="${tr.cytoDetails.map.impression_id }"/>
													<div id="impresContainer${impessionIndex}" style="width: 500px;"></div>
												</div>
												<div>
													<div style="height:4px" id="showAlertDialog${impessionIndex}"></div>
													<a  href="javascript:void(0)" onclick="doClose(${impessionIndex});"><insta:ltext key="laboratory.testconduction.list.addtomasterlist"/></a>
												</div>
											</td>

										</tr>
									</table>
								</td>
							</tr>
								<tr>
									<td>
										<table>
											<tr>
												<td class="formlabel"><insta:ltext key="laboratory.testconduction.list.impression"/> :</td>
												<td>
													<textarea  cols="70"  name="impression_details" id="impressionDetails${impessionIndex}" onblur="checkLength(this,500,'Impression Details')">${tr.cytoDetails.map.cyto_impression_details }</textarea>
												</td>
											</tr>
											<tr>
												<td class="formlabel"><insta:ltext key="laboratory.testconduction.list.clinicaldetails"/> :</td>
												<td>
													<textarea cols="90" rows="8" name="clinical_details" onblur="checkLength(this,5000,'Clinical Details')">${tr.cytoDetails.map.clinical_details}</textarea>
												</td>
											</tr>
											<tr>

												<td class="formlabel"><insta:ltext key="laboratory.testconduction.list.microscopic.grossdetails"/> :</td>
												<td>
													<textarea cols="90" rows="8" name="cyto_microscopic_details" onblur="checkLength(this,5000,'Microscopic Gross Details')">${tr.cytoDetails.map.microscopic_details}</textarea>
												</td>
											</tr>
										</table>
									</td>
								</tr>
							</table>
						</fieldset>
						<c:set var="impessionIndex" value="${impessionIndex+1 }"/>
					</div>
					<c:set var="commonIndex" value="${commonIndex+1}"/>
					</c:forEach>
				</c:if>

			</div>

			<c:set var="incr" value="${incr+1}" />
<div id="commonConductingDocDIV" style="float: left;">
	<insta:ltext key="laboratory.testconduction.list.setconductingdoctoras"/>:
	<select name="commonConductingDoctor" id="commonConductingDoctor" style="width:12em"
		 class="dropdown" onchange="setCommonConductingDoctor(this);"
		 title="Set conducting doctor for all test at once" ${allCompleted ne '' || allVerified ne '' ? 'disabled' : '' }>
		<option value="">${select}</option>
		<c:forEach var="doctor" items="${requestScope.doctors}">
				<option value="${doctor.DOCTOR_ID}">${doctor.DOCTOR_NAME}</option>
			</c:forEach>
	</select>
</div>
<div style="float: left;">
<c:set var="allCompleteChk" value="${!allCompleteChk ? 'disabled' : ''}" />
<c:set var="allvalidateChk" value="${!allvalidateChk ? 'disabled' : ''}" />
<c:if test="${category == 'DEP_LAB'}">
<c:set var="allCompleteChk" value="${allCompleted}" />
<c:set var="allvalidateChk" value="${allVerified}" />
</c:if>
	<c:choose>
	   <c:when test="${not empty patientvisitdetails}">
		  <c:set var="visitid" value="${patientvisitdetails.map.patient_id}" />
	   </c:when>
	       <c:otherwise>
		  <c:set var="visitid" value="${custmer.map.incoming_visit_id}" />
	   </c:otherwise>
	</c:choose>

   <insta:ltext key="laboratory.testconduction.list.completeall"/>:<input type="checkbox" name="markAllCompleted" id="markAllCompleted"
   		onclick="markAllComplete();" ${allCompleted == '' ? allCompleteChk : allCompleted }  />
   <c:if test="${validateResultsRt }">
   		<insta:ltext key="laboratory.testconduction.list.validateall"/>:<input type="checkbox" name="markAllValidated" id="markAllValidated"
  			onclick="markAllValidate(this);" ${allVerified == '' ? allvalidateChk : allVerified }/>
   </c:if>

</div>

<div style="float: left;clear:both" class="screenActions">
	<button type="button"  name="save" id="Save" style="button" accesskey="S" styleClass="button" tabindex="2" ${isTransferedTest ? 'disabled' : ''}
				onclick="return submitvalues(this)"><b><u><insta:ltext key="laboratory.testconduction.list.s"/></u></b><insta:ltext key="laboratory.testconduction.list.ave"/></button>
	<button type="button"  name="sanvprint" id="SaveAndPrint" style="button" accesskey="P" styleClass="button" tabindex="2" ${isTransferedTest ? 'disabled' : ''}
				onclick="return submitvalues(this)"><insta:ltext key="laboratory.testconduction.list.saveand"/><b><u><insta:ltext key="laboratory.testconduction.list.p"/></u></b><insta:ltext key="laboratory.testconduction.list.rint"/></button>

	<insta:screenlink screenId="${category == 'DEP_LAB' ?'lab_manage_reports' : 'rad_manage_reports'}"
			extraParam="?_method=getLabReport&category=${category}&visitid=${visitid}"
				label="${manageReports}" addPipe="true"/>
	<c:choose>
			<c:when test="${param.category == 'DEP_LAB'}">
				<c:set var="url" value="Laboratory"/>
				<c:set var="reportListLink" value="${labreportList}"/>
			</c:when>
			<c:otherwise>
				<c:set var="url" value="Radiology"/>
				<c:set var="reportListLink" value="${radreportList}"/>
			</c:otherwise>
		</c:choose>
		| <a href="<c:out value="${cPath}/${url}/schedules.do?_method=getScheduleList&category=${ifn:cleanURL(param.category)}&mr_no=${ifn:cleanURL(mr_no)}&patient_id=${not empty patientvisitdetails ? '' : visitid}"/>">${reportListLink}</a>
</div>
<div style="float: right; margin-top: 10px" >
	<insta:selectdb name="printer" id="printer" table="printer_definition" valuecol="printer_id"
			displaycol="printer_definition_name" value="${pref.map.printer_id}" />
</div>
<c:if test="${hasSignOffRights && not empty reportForSignoff && reportWithIncompleteTests eq 'N' }">
<c:set var="signgOffReports" value="Y"/>
<div >
	<div style="clear:both;"></div>
	<div id="CollapsiblePanelSignoff" class="CollapsiblePanel ">
	<div class=" title CollapsiblePanelTab"  style=" border-left:none;" >
		<div class="fltL " style="width: 230px; margin:5px 0 0 10px;">
			<insta:ltext key="laboratory.testconduction.list.signoffreports"/>
		</div>
		<div class="fltR txtRT" style="width: 25px; margin:10px 0 0 680px; padding-right:0px;">
			<img src="${cPath}/images/down.png" />
		</div>
		<div class="clrboth"></div>
	</div>
	<div class="CollapsiblePanelContentNoBkClr" >

	<fieldset class="fieldSetBorder">
		<table class="detailList">
			<tr>
				<th><input type="checkbox" name="signoffAll" onclick="selectAllReports(this)"/></th>
				<th><insta:ltext key="laboratory.testconduction.list.report"/></th>
			</tr>
			<tr>
			<td>
				<input type="checkbox" name="signoff" value="${reportForSignoff.map.report_id }"/>
			</td>
			<td>${reportForSignoff.map.report_name }</td>
			</tr>
			<tr>
				<td colspan="2">
					<input type="button" name="signoffreports" value="Sign Off" onclick="signOffReports('N')"/>
					<input type="button" name="signoffandprint" value="SignOff&Print" onclick="signOffReports('Y')"/>
				</td>
			</tr>
		</table>
	</fieldset>
	</div>
	</div>
</div>
</div>
</c:if>
<div style="display:none" id="discliamerDialog">
<div class="bd">
	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel"><insta:ltext key="laboratory.testconduction.list.editremarks.disclaimer"/></legend>
		<table >
			<tr>
				<td class="formlabel"><insta:ltext key="laboratory.testconduction.list.remarks"/>:</td>
				<td>
					<textarea name="eRemarks" id="eRemarks" onblur="checkLength(this,2000,'Remarks')"></textarea>
				</td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="laboratory.testconduction.list.disclaimer"/>:</td>
				<td>
					<textarea name="eResultsDiscliamer" id="eResultsDiscliamer" onblur="checkLength(this,200,'Disclaimer')"></textarea>
				</td>
			</tr>
			<tr>
				<td>
					<input type="button" name="btnDisOk" id="btnDisOk" value="Ok" onclick="setDisclaimer();"/>
					<input type="button" name="btnDisX" id="btnDisX" value="Cancel" onclick="onCancel();"/>
					<input type="hidden" name="editedResultIdx" id="editedResultIdx"/>
				</td>
			</tr>
		</table>
	</fieldset>
</div>
</div>
<div style="display:none" id="amendmentReasonDialog">
<div class="bd">
	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel"><insta:ltext key="laboratory.testconduction.list.amendmentreason"/></legend>
		<table >
			<tr>
				<td>
					<textarea name="eAmendmentReason" id="eAmendmentReason"></textarea>
				</td>
			</tr>
			<tr>
				<td>
					<input type="button" name="btnAmendOk" id="btnDisOk" value="Ok" onclick="setAmendedReason();"/>
					<input type="button" name="btnAmendX" value="Cancel" onclick="onCancelAmendDialog()"/>
					<input type="hidden" name="editedAmendIdx" id="editedAmendIdx"/>
					<input type="hidden" name="amendingTable" id="amendingTable"/>
				</td>
			</tr>
		</table>
	</fieldset>
</div>
</div>
<div style="display:none" id="organismDetailsDialog">
	<div class="bd">
	<input type="hidden" name="editedOrganismTableIndex" id="editedOrganismTableIndex" />
		<fieldset class="fieldSetBorder"><legend class="fieldSetLabel"><insta:ltext key="laboratory.testconduction.list.add.editorganismdetails"/></legend>
			<table class="formtable">
				<tr>
					<td class="formlabel"><insta:ltext key="laboratory.testconduction.list.organismgroup"/> :</td>
					<td>
						<insta:selectdb name="eOrg_group_id" id="eOrg_group_id"
							table="micro_org_group_master" valuecol="org_group_id"
							displaycol="org_group_name" value=""
							onchange="changeOrganism(this),changeABSTPanel(this)"
							dummyvalue="${select}"/>
					</td>
					<td class="formlabel"><insta:ltext key="laboratory.testconduction.list.organism"/> :</td>
					<td>
						<select name="eOrganism_id" id="eOrganism_id" class="dropdown" >
							<option value="">${select}</option>
					<td class="formlabel"><insta:ltext key="laboratory.testconduction.list.comment"/> :</td>
					<td>
						<textarea  cols="30" name="eComments" id="eComments" onblur="checkLength(this,500,'Comments')"></textarea>
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="laboratory.testconduction.list.abstpanel"/> :</td>
					<td>
						<insta:selectdb id="eAbst_panel_id" name="eAbst_panel_id"
							table="micro_abst_panel_master" valuecol="abst_panel_id"
							displaycol="abst_panel_name" value=""
							dummyvalue="${select}"
							onchange="fillAntibiotics(this)" />
					</td>
					<td class="formlabel"><insta:ltext key="laboratory.testconduction.list.resistancemarker"/> :</td>
					<td>
	                   	<input type="text" name="eResistance_marker" id="eResistance_marker"/>
					</td>
				</tr>
			</table>
			<table cellspacing="0" cellpadding="0" id="antiBoiticTable" class="dataTable" width="100%">
				<tr>
					<th><insta:ltext key="laboratory.testconduction.list.antibiotic"/></th>
					<th><insta:ltext key="laboratory.testconduction.list.results"/></th>
					<th><insta:ltext key="laboratory.testconduction.list.susceptibility"/></th>
				</tr>
			</table>
		</fieldset>
		<input type="button" name="add" id="add" value="Ok" onclick="onOkOrgDialog();" tabindex="4"/>
		<input type="button" name="close" id="close" value="Close" onclick="closeOrganismDialog();" tabindex="5"/>
	</div>
</div>
</div>
<jsp:include page="TestDetails.jsp"></jsp:include>
<div id="alertDialog" style="visibility:hidden">
<div class="bd">
	<table cellpadding="0" cellspacing="0">
		<tr>
			<th colspan="2" align="center"><insta:ltext key="laboratory.testconduction.list.test"/></th>
			<td><input type="hidden" name="alertDialogId" id="alertDialogId" value=""></td>
		</tr>
		<tr>
			<td colspan="2" align="center">&nbsp;</td>
		</tr>
	</table>
	<input type="button" name="yes" onclick="handleYes();" value="Yes" />
	<input type="button" name="no" onclick="handleNo();" value="No" />
</div>
</div>

<jsp:include page="PreviousResults.jsp"/>

<script>
	if('${amendingPhase}' == 'Y' || '${testsReconducted}' == 'Y'){
		document.getElementById("commonConductingDocDIV").style.display = 'none';
	}
	if('${reagenetsExists_V}' == 'Y'){
		document.getElementById("reagenetsTh_V").innerHTML = 'Reagents';
		document.getElementById("reagenetsTh_V").style.display = 'table-cell';
	}

	if('${reagenetsExists_T}' == 'Y'){
		document.getElementById("reagenetsTh_T").innerHTML = 'Reagents';
		document.getElementById("reagenetsTh_T").style.display = 'table-cell';
	}
	if ('${methodologyExists}' == 'Y') {
		document.getElementById('methodologyTh').innerHTML = 'Methodology';
		document.getElementById('methodologyTh').style.display = 'table-cell';
	}

	var methodologyExists =	'${methodologyExists}';

	if(!empty('${signgOffReports}')){
		var CollapsiblePanelSignoff = new Spry.Widget.CollapsiblePanel("CollapsiblePanelSignoff", false);
		CollapsiblePanelSignoff.close();
	}
	var testandresultsJSON = ${testandresultsJSON};
	var CollapsiblePanel1 = new Array();
	var CollapsiblePanel2 = new Array();
	var noGrowthTemplates = document.getElementsByName("nogrowth_template_id");
	for(var i = 0;i<noGrowthTemplates.length;i++){
		CollapsiblePanel1[i] = new Spry.Widget.CollapsiblePanel("CollapsiblePanel1"+i, false);
		CollapsiblePanel2[i] = new Spry.Widget.CollapsiblePanel("CollapsiblePanel2"+i, false);
	}
	var sampleFlow = '${sampleFlow}';
	var thisDate = '${thisDateVal}';
	var thisTime = '${thisTimeVal}';
	var category = '${ifn:cleanJavaScript(param.category)}';
	var validateResultsRt = ${validateResultsRt};
	var deptUsersMap = ${deptUsersMap};

</script>

</form>


</body>
</html:html>