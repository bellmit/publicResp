<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@page isELIgnored="false"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>

<c:set var="cPath" value="${pageContext.request.contextPath}"/>
<html:html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="i18nSupport" content="true"/>
<insta:link type="script" file="ajax.js" />
<insta:link type="script" file="hmsvalidation.js" />
<insta:link type="script" file="diagnostics/ManageReports.js"/>

<style type="text/css">
	.signedOff {background-color: #DDDA8A }
</style>

<script>
	function setprint(reportId){
		var form = document.diagcenterform;
		var printerId =form.printer.options[form.printer.selectedIndex].value;
		window.open('${cPath}/pages/DiagnosticModule/DiagReportPrint.do?_method=printReport&reportId='+reportId+
			'&rawprint=${prefs.map.print_mode}&col=${prefs.map.text_mode_column}&printerId='+printerId);
 	}
	function setSampleprint(obj,patientId,sampleno,sampleDate,sampleType){

		var href = cpath+"/pages/DiagnosticModule/DiagReportPrint.do?_method=generateSampleCollectionReport&visitid="+patientId+"&sampleNo='"+sampleno+"'&template_name="+document.diagcenterform.sampleBardCodeTemplate.value;
		if(samplePrintType != "SL"){
			href = cpath+"/Laboratory/GenerateSamplesBarCodePrint.do?method=execute&mrno="+document.diagcenterform.mr_no.value+"&sampleNo='"+sampleno+"'&barcodeType=sample&template_name="+document.diagcenterform.sampleBardCodeTemplate.value+"&sampleDates="+sampleDate+"&sampleTypes="+sampleType+"&visitId="+patientId;;
		}
		obj.setAttribute('href',href);
		return true;

 	}
	function setSampleoutHouseprint(patientId,outSourceDestId,pesId,sampleNo){
		window.open(cpath+"/pages/DiagnosticModule/DiagReportPrint.do?_method=generateSampleCollectionOutHouseReport&visitid="+patientId+"&outSourceName="+outSourceDestId+"&prescribedId="+pesId+"&sampleNo="+sampleNo);
	}
	function getBatchConductionScreen(reportId){
		var form = document.diagcenterform;
		form.reportIdForEditResults.value = reportId;

		form.action = 'editresults.do';
		form._method.value = 'getBatchConductionScreen';
		form.action = form.action+"?_method=getBatchConductionScreen&reportId="+reportId;
		form.submit();
 	}
 	function openwindow(reportId){
 		document.getElementById("save").disabled = true;
		var path = "${cPath}/Diagnostics/AddendumPopup.do?_method=getAddendumTemplateEditor";
		path = path + "&reportid=" + reportId ;

		window.open(path,'Popup_Window',"width=700, height=700,screenX=80,screenY=50,left=300,top=50,scrollbars=yes,menubar=0,resizable=yes");
	  	return false;
	}


</script>
<insta:js-bundle prefix="laboratory.radiology.managereports"/>
<insta:js-bundle prefix="registration.patient"/>
<title><insta:ltext key="laboratory.managereports.list.managereports"/> (${patientvisitdetails.map.mr_no}) - <insta:ltext key="laboratory.managereports.list.instahms"/> </title>
</head>
<body onload="initReportNameDialog()">
	<c:set var="visit_id"   scope="page"/>
	<c:set var="now" value="<%=new java.util.Date()%>" />

	<h1><insta:ltext key="laboratory.managereports.list.managereports"/></h1>
	<c:set var="info" value="${reportMsg}"/>
	<insta:feedback-panel/>
	<c:choose>
		<c:when test="${ not empty patientvisitdetails }">
			<insta:patientdetails  visitid="${patientvisitdetails.map.patient_id}"/>
			<c:set var="visit_id"  value="${patientvisitdetails.map.patient_id}" />
			<c:set var="mrNo" value="${patientvisitdetails.map.mr_no}"/>
		</c:when>
		<c:otherwise>
			<c:set var="visit_id"  value="${custmer.map.incoming_visit_id}" />
			<c:set var="mrNo" value=""/>
			<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel"><insta:ltext key="laboratory.managereports.list.patientdetails"/></legend>
			<table class="formtable" cellpadding="0" cellspacing="0" width="100%">
				<tr>
					<td><insta:ltext key="ui.label.patient.name"/>:</td>
					<td class="forminfo">${custmer.map.patient_name}</td>
					<td><insta:ltext key="laboratory.managereports.list.fromlab"/>:</td>
					<td class="forminfo">${custmer.map.hospital_name}</td>
				</tr>
				<tr>
					<td><insta:ltext key="laboratory.managereports.list.patientvisit"/>:</td>
					<td class="forminfo">${custmer.map.incoming_visit_id}
					  <c:set var="visit_id"  value="${custmer.map.incoming_visit_id}" />
					</td>
					<td><insta:ltext key="laboratory.managereports.list.age.gender"/>:</td>
					<td class="forminfo">${custmer.map.age_text}${fn:toLowerCase(custmer.map.age_unit)} / ${custmer.map.gender}</td>
				</tr>
			</table>
			</fieldset>
		</c:otherwise>
	</c:choose>

<html:form method="POST">
	<input type="hidden" value="saveReportPrescriptions" name="_method" />
	<input type="hidden" name="category" value="${ifn:cleanHtmlAttribute(category)}" />
	<input type="hidden" name='mrno' value="${patientvisitdetails.map.mr_no}">
	<input type="hidden" name="reportIdsForRegeneration" value="NO"/>
	<input type="hidden" name="fromScreen" value="manageReport">
	<input type="hidden" name="reportIdForEditResults">
	<input type="hidden" name='visitid' value="${ifn:cleanHtmlAttribute(visit_id)}">
	<input type="hidden" name="mr_no" value="${ifn:cleanHtmlAttribute(mrNo)}">
	<div class="resultList">
	<table width="100%">
		<tr>
			<td>
			<table class="dashboard" id="testsItemList" width="100%" cellspacing="0" cellpadding="0">
				<tr>
					<th><insta:ltext key="laboratory.managereports.list.reportname"/></th>
					<th></th>
					<th>
						<c:choose>
							<c:when test="${islabNoReq == 'Y'}"><insta:ltext key="laboratory.managereports.list.testname.labno.pdate.order"/></c:when>
							<c:otherwise ><insta:ltext key="laboratory.managereports.list.testname.pdate.order"/></c:otherwise>
						</c:choose>
					</th>
					<th><insta:ltext key="laboratory.managereports.list.testresult"/></th>
					<th><insta:ltext key="laboratory.managereports.list.print"/></th>
					<th><insta:ltext key="laboratory.managereports.list.delete"/></th>
					<th><insta:ltext key="laboratory.managereports.list.addaddendum"/></th>
				</tr>
				<c:set var="id" value="1" />
				<c:set var="sampleAssertion" value="${genPrefs.sampleassertion == 'Y'}" />
				<c:forEach var="report" items="${repotList}">
					<tr>
						<td>
							 <c:choose>
								 <c:when test="${report.signOff eq 'Y' && report.handedOver != 'Y'}">
								 	<img src="${cPath}/images/yellow_flag.gif"/><label>${ifn:cleanHtml(report.reportName)}</label>
								 </c:when>
								 <c:when test="${report.handedOver == 'Y'}">
								 	<img src="${cPath}/images/green_flag.gif"/><label>${ifn:cleanHtml(report.reportName)}</label>
								 </c:when>
								 <c:otherwise>
								 		<label>${ifn:cleanHtml(report.reportName)}</label>
								 </c:otherwise>
							 </c:choose>
						</td>
						<td>
							<html:hidden property="manageReportName" value="${report.reportName}"/>
							<html:hidden property="manageReportId" value="${report.reportId}"/>
							 <a href="javascript:void(0)"
								onclick='return showReportNameDialog(this);'
								title='<insta:ltext key="laboratory.managereports.list.editreportname"/>'>
								<img src="${cPath}/icons/Edit.png" class="button" />
						</td>
						<td>
							<c:set var="ReconductedTests" value="0"/>
							<c:set var="noOfTests" value="0"/>
							<c:forEach var="test" items="${report.testList}">
								<c:if test="${islabNoReq == 'Y'}">
									<c:set var="labnoText">(${ifn:cleanHtml(test.labNO)})-</c:set>
								</c:if>
								<c:if test="${test.conducted=='RBS'}">
									<c:set var="ReconductedTests" value="${ReconductedTests+1}" />
								</c:if>
								<fmt:formatDate value="${test.presDateTime}" pattern="dd-MM-yyyy" var="presDate"/>
								<c:out value="${test.testName}"/>-${labnoText}(${presDate})-[${test.commonOrderID}]<br />
								<c:set var="noOfTests" value="${noOfTests+1}"/>
							</c:forEach>
							<c:set var="isAllReconducted" value="${ReconductedTests==noOfTests}"/>
						</td>
						<c:choose>
							<c:when test = "${report.reportId ne ''}">
								<c:url var="generateURL" value="">
									<c:param name="_method" value="generateReport"/>
									<c:param name="reportId" value="${report.reportId}"/>
									<c:param name="visitid" value="${visit_id}"/>
									<c:param name="category" value="${category}"/>
								</c:url>
								<c:url var="editReportURL" value="editresults.do">
									<c:param name="_method" value="getEditReport"/>
			  						<c:param name="reportId" value="${report.reportId}"/>
									<c:param name="visitid" value="${visit_id}"/>
									<c:param name="category" value="${category}"/>
								</c:url>

								<c:url var="deleteURL" value="">
									<c:param name="_method" value="deleteReport"/>
									<c:param name="reportId" value="${report.reportId}"/>
									<c:param name="visitid" value="${visit_id}"/>
									<c:param name="category" value="${category}"/>
								</c:url>
								<c:url value="" var="regenerateURL">
									<c:param name="_method" value="generateReport"/>
									<c:param name="reportId" value="${report.reportId}"/>
									<c:param name="visitid" value="${visit_id}"/>
									<c:param name="category" value="${category}"/>
								</c:url>
                                <td>
	                        	     <c:if test="${report.signOff ne 'Y' && !isAllReconducted && (category == 'DEP_LAB' ? (report.outSourceDestType eq 'C' ? report.outSourceDest eq centerId : true) : true) &&
								    	  	(category == 'DEP_LAB' ? urlRightsMap.lab_edit_results : urlRightsMap.rad_edit_results) == 'A'}">
	                                   <a href="javascript:void(0)" onclick="getBatchConductionScreen('${report.reportId}')"><insta:ltext key="laboratory.managereports.list.edit"/> </a>
	                                 </c:if>
                                 </td>
								<c:choose>
								    <c:when test="${report.hasData != 'N'}">
										 <td>
											<a href="javascript:void(0)" onclick="setprint(${report.reportId})"><insta:ltext key="laboratory.managereports.list.print"/></a>
										  </td>
									      <td>
									      <c:if test="${report.signOff ne 'Y' && !isAllReconducted &&
									      	(category == 'DEP_LAB' ? urlRightsMap.lab_edit_results : urlRightsMap.rad_edit_results) == 'A'}">
										      <a href="${deleteURL}"><insta:ltext key="laboratory.managereports.list.delete"/> </a>
										   </c:if>
									     </td>
									</c:when>
								   <c:otherwise>
								     <%-- when report has no data --%>
									<td>&nbsp;</td>
									<td>&nbsp;</td>
									<td>&nbsp;</td>
								   </c:otherwise>
							  </c:choose>
							</c:when>
							<c:otherwise>
							    <%-- when no report exists. --%>
								<td>&nbsp;</td>
								<td>&nbsp;</td>
								<td>&nbsp;</td>
							</c:otherwise>
						</c:choose>
						<c:choose>
							<c:when  test="${report.reportId ne '' && report.addendumSignoff != 'Y' && report.signOff eq 'Y'}">
								<td>
									<a href="javascript:void(0)" onclick="return openwindow('${report.reportId}')">
										<insta:ltext key="laboratory.managereports.list.addaddendum"/>
									</a>
								</td>
							</c:when>
							<c:otherwise>
								<td></td>
							</c:otherwise>
						</c:choose>
					</tr>
					<c:set var="id" value="${id+1}" />
				</c:forEach>
			</table></td>
			<td valign="bottom"><input type="button" value="+"  onclick="addReports()"></td>
		</tr>

	</table>

	<table>

		<tr>
		<td>
			<table class="dataTable" id="addReport" cellspacing="0" cellpadding="0">
				<tr>
					<th>
						<c:choose>
							<c:when test="${islabNoReq == 'Y'}"><insta:ltext key="laboratory.managereports.list.testname.order"/></c:when>
							<c:otherwise ><insta:ltext key="laboratory.managereports.list.testname.order"/></c:otherwise>
						</c:choose>
					</th>
					<th><insta:ltext key="laboratory.managereports.list.addtoreport"/></th>
				</tr>
				<c:set var="id" value="1" />
				<c:forEach items="${testLists}" var="tests">
				<c:if test="${tests.CONDUCTED ne 'S' }">
					<tr>
						<td>${tests.TEST_NAME}-[${tests.COMMON_ORDER_ID }]</td>
						<td>
						  <select name="reportId" id="report${id}" class="dropdown"
						  	onchange="return checkManageReport(this.value,'${id}','${tests.TEST_ID}')">
						  	<option value="N"><insta:ltext key="laboratory.managereports.list.noreport"/></option>
								<c:forEach var="item" items="${reposts}">
									<c:if test="${item.SIGNED_OFF ne 'Y'}">
										<option value="${item.REPORT_NAME}"
										   ${((tests.REPORT_ID eq item.REPORT_ID) || (item.REPORT_NAME eq 'NEW1'))  ? 'Selected':''}>
										<c:choose>
											<c:when test="${item.REPORT_NAME eq 'NEW1'}">
											<c:if test="${category eq 'DEP_LAB'}">
										    <insta:ltext key="laboratory.managereports.list.lr"/>-<fmt:formatDate value="${now}" pattern="ddMMyy"/>-1
										    </c:if>
										    <c:if test="${category eq 'DEP_RAD'}">
										   <insta:ltext key="laboratory.managereports.list.rr"/>-<fmt:formatDate value="${now}" pattern="ddMMyy"/>-1
										    </c:if>
											</c:when>
											<c:otherwise>
											   ${item.REPORT_NAME}
											</c:otherwise>
										</c:choose>
										</option>
									</c:if>
								</c:forEach>
						</select>
						</td>
						<input type="hidden" name="prescribedid" value="${tests.PRESCRIBED_ID}" />
						<input type="hidden" name="revisionNumber" value="${tests.REVISION_NUMBER}" />
						<input type="hidden" name="testid" value="${tests.TEST_ID }" />
					</tr>
					<c:set var="id" value="${id+1}" />
				</c:if>
				</c:forEach>
			</table>
			</td>
			<td style="width:1em"></td>
			 <c:if test="${category eq 'DEP_LAB'}">
      <c:if test="${not empty collectedSampleList && not empty testLists}">
			<td  valign="top">
			<table class="dashboard" id="samplecollectionDetails" align="left" width="100%" cellspacing="0" cellpadding="0">
				<tr align="left">
					<th><insta:ltext key="laboratory.managereports.list.sampleno"/></th>
					<th><insta:ltext key="laboratory.managereports.list.testnames"/></th>
					<th><insta:ltext key="laboratory.managereports.list.samplelabelprint"/></th>
					</tr>
					<c:forEach var="samplecollectedlist" items="${collectedSampleList}">
					<c:if test="${samplecollectedlist.SAMPLE_SNO ne '' and  samplecollectedlist.SAMPLE_SNO ne patientvisitdetails.map.patient_id}">

					<tr>
					<td>${samplecollectedlist.COLL_SAMPLE_NO}</td>
					<td><c:set var="spltedtests" value="${fn:split(samplecollectedlist.TEST_NAME, ',')}"/>
					<c:forEach var="tests" items="${spltedtests}">
					  <c:out value="${tests}" /><br/>
					</c:forEach></td>
					<td>
						<a href="javascript:void(0)" target="blank"
						  onclick="setSampleprint(this,'${samplecollectedlist.PATIENT_ID}',
						  	'${samplecollectedlist.SAMPLE_SNO}','${samplecollectedlist.SAMPLE_DATE}','${samplecollectedlist.SAMPLE_TYPE}')">
						  	<insta:ltext key="laboratory.managereports.list.samplelabelprint"/>
					  	</a>
				  	</td>
					</tr>
					</c:if>
					</c:forEach>

					</table>
					</td>
					</c:if>
					</c:if>
					<td style="width:1em"></td>
					<!-- Outhouse sample collection duplicate  print -->
			 <c:if test="${category eq 'DEP_LAB'}">
      <c:if test="${not empty outSourceSampleList && not empty testLists}">
			<td  valign="top">
			<table class="dataTable" id="outsourcesamplecollectionDetails" align="left" width="100%" cellspacing="0" cellpadding="0">
				<tr align="left">
					<th><insta:ltext key="laboratory.managereports.list.sampleno"/></th>
					<th><insta:ltext key="laboratory.managereports.list.testnames"/></th>
					<th><insta:ltext key="laboratory.managereports.list.outsourcesampleprint"/></th>
					</tr>
					<c:forEach var="outsource" items="${outSourceSampleList}">
					<tr>
					<td>${outsource.map.sample_no}</td>
					<td><c:set var="spltedtests" value="${fn:split(outsource.map.test_name, ',')}"/>
					<c:forEach var="tests" items="${spltedtests}">
					  <c:out value="${tests}" /><br/>
					</c:forEach></td>
					<td><a href="javascript:void(0)" onclick="setSampleoutHouseprint('${outsource.map.pat_id}','${outsource.map.outsource_dest_id}','${outsource.map.prescribed_id}','${outsource.map.current_center_sample_no}')">Outsource sample Print</a></td>
					</tr>
					</c:forEach>
					</table>
					</td>
					</c:if>
					</c:if>
        </tr>
</table>
</div>
<div class="screenActions" style="float: left">
	<button type="submit" name="save" id="save" accesskey="S"><b><u><insta:ltext key="laboratory.managereports.list.s"/></u></b><insta:ltext key="laboratory.managereports.list.ave"/></button>
	<c:choose>
		<c:when test="${category == 'DEP_LAB'}">
			<c:set var="url" value="Laboratory"/>
			<c:set var="reportlistlink" value="Laboratory Reports List"/>
		</c:when>
		<c:otherwise>
			<c:set var="url" value="Radiology"/>
			<c:set var="reportlistlink" value="Radiology Reports List"/>
		</c:otherwise>
	</c:choose>
	| <a href="<c:out value="${cPath}/${url}/schedules.do?_method=getScheduleList&category=${ifn:cleanURL(category)}&mr_no=${patientvisitdetails.map.mr_no}&patient_id=${not empty patientvisitdetails ? '' : visit_id}"/>">${reportlistlink}</a>
</div>
<div style="float: right">
	<insta:selectdb name="printer" table="printer_definition" valuecol="printer_id" displaycol="printer_definition_name"
									 value="${prefs.map.printer_id}"/>
	<insta:selectdb name="sampleBardCodeTemplate" table="sample_bar_code_print_templates" valuecol="template_name"
			displaycol="template_name" filtered="false" value="${template_name}"/>
<div style="clear: both"></div>
<div class="legend" style="float: right">
	<div class="flag"><img src='${cPath}/images/yellow_flag.gif'></div>
	<div class="flagText"><insta:ltext key="laboratory.managereports.list.signedoffreports"/></div>
	<div class="flag"><img src='${cPath}/images/green_flag.gif'></div>
	<div class="flagText"><insta:ltext key="laboratory.managereports.list.handedoverreports"/></div>
</div>

<script>
	var totalReports = ${totalReports};
	var prescriptionList = ${JSONReportPrescriptionList};
	var reportList = ${JSONReportList};
	var samplePrintType = '${genPrefs.sampleCollectionPrintType}';
</script>
<div style="display:none" id="reportNameDialog">
<div class="bd">
	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel"><insta:ltext key="laboratory.managereports.list.editreportname"/></legend>
		<table >
			<tr>
				<td>
					<input type="text" name="eReportName" id="eReportName"/>
				</td>
			</tr>
			<tr>
				<td>
					<input type="button" name="btnNameOk" id="btnNameOk" value="Ok" onclick="setReportName();"/>
					<input type="button" name="btnNameX" value="Cancel" onclick="closeReportNameDialog()"/>
					<input type="hidden" name="reportNameEditId" id="reportNameEditId"/>
				</td>
			</tr>
		</table>
	</fieldset>
</div>
</div>
</html:form>
</body>

</html:html>
