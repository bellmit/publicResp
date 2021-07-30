<%@page import="com.bob.hms.common.Constants" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<html>
<head>
<title><insta:ltext key="medicalrecords.patientemr.view.emrlist.instahms"/></title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">

<c:set var="emrurldate"
	value='<%= GenericPreferencesDAO.getAllPrefs().get("emr_url_date") %>'
	scope="request" />

<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="script" file="/emr/MainTreeView.js"/>
<insta:link type="js" file="/emr/duplicatePatients.js"/>
<insta:link type="script" file="ajax.js" />
<insta:link type="script" file="/emr/emrJustificatoinComments.js" />

<style type="text/css">
	.ygtvlabel, .ygtvlabel:link, .ygtvlabel:visited, .ygtvlabel:hover{
		margin-left:2px;text-decoration:none;cursor:pointer;
	}
	.myDiv{
		border-right:solid 1px #000000;
	}
	.scroll {
	       overflow:auto;
	       width: 20em;
	       height:30em;
	}

</style>
<script language="javascript" type="text/javascript">
var cpath = '<%= request.getContextPath()%>';
var roleId='${roleId}';
var docsFound = ${not empty allDocs};
var duplicateMrNoExists = ${duplicateMrNoExists};
var emrurldate = '${emrurldate}';
var add_comments = '${param.add_comments}';
var mr_no = '${param.mr_no}';
var visit_id = '${param.visit_id}';
var mandate_emr_comments = '${mandate_emr_comments}';
</script>
<insta:js-bundle prefix="registration.patient"/>
<insta:js-bundle prefix="medicalrecords.patientemr" />
</head>
<body  class="setMargin yui-skin-sam" onload="initDialog(); addCommentsOnPageLoad(); treeInit()">
<c:choose>
	<c:when test="${portalLink eq 'doc' && actionId eq 'emr_screen'}">
		<c:url var="url" value="/doctor/EMRMainDisplay.do"/>
	</c:when>
	<c:when test="${portalLink ne 'doc' && actionId eq 'emr_screen'}">
		<c:url var="url" value="/emr/EMRMainDisplay.do"/>
	</c:when>
	<c:when test="${portalLink eq 'doc' && actionId eq 'visit_emr_screen'}">
		<c:url var="url" value="/doctor/VisitEMRMainDisplay.do">
			<c:param name="filterType" value="docType"/>
		</c:url>
	</c:when>
	<c:when test="${portalLink ne 'doc' && actionId eq 'visit_emr_screen'}">
		<c:url var="url" value="/emr/VisitEMRMainDisplay.do">
		<c:param name="filterType" value="docType"/>
		</c:url>
	</c:when>
</c:choose>
<c:set var="patientText">
<insta:ltext key="medicalrecords.patientemr.view.patient"/>
</c:set>
<c:set var="visitText">
<insta:ltext key="medicalrecords.patientemr.view.visit"/>
</c:set>
<c:set var="docmandatoryText">
<insta:ltext key="medicalrecords.patientemr.view.documenttype.mandatory"/>
</c:set>
<c:set var="duplicatepatientslist">
<insta:ltext key="medicalrecords.patientemr.view.duplicatepatientslist"/>
</c:set>
<c:set var="searchText">
<insta:ltext key="medicalrecords.patientemr.view.search"/>
</c:set>
<c:set var="dummyvalue">
	<insta:ltext key="selectdb.dummy.value"/>
</c:set>
<c:choose>
	<c:when test="${actionId eq 'emr_screen_without_mrno_search'}">
		<h1><insta:ltext key="medicalrecords.patientemr.view.patientemr"/></h1>
	</c:when>
	<c:otherwise>
		<h1 style="float: left"> ${actionId eq 'emr_screen'? patientText: visitText}&nbsp;<insta:ltext key="medicalrecords.patientemr.view.emr.search"/></h1>
	</c:otherwise>
</c:choose>
<c:choose>
	<c:when test="${actionId eq 'emr_screen'}">
		<insta:patientsearch fieldName="mr_no" searchUrl="${url}" searchMethod="list" searchType="mrNo" screenID="emr_screen" />
	</c:when>
	<c:when test="${actionId eq 'visit_emr_screen'}">
		<insta:patientsearch fieldName="visit_id" searchUrl="${url}" searchMethod="list" searchType="visit" screenID="visit_emr_screen" />
	</c:when>
	<c:otherwise>
	</c:otherwise>
</c:choose>
<insta:feedback-panel/>

<c:choose>
	<c:when test="${actionId eq 'emr_screen' || actionId eq 'emr_screen_without_mrno_search'}">
		<insta:patientgeneraldetails tableID="patientdetails" mrno="${param.mr_no}" addExtraFields="true" showClinicalInfo="true"/>
	</c:when>
	<c:otherwise>
		<insta:patientdetails tableID="patientdetails" visitid="${empty param.visit_id? visit_id: visit_id}" showClinicalInfo="true" value="${param.visit_id}"/>
	</c:otherwise>
</c:choose>

<form name="emrTreeForm" id="emrForm" action='<c:out value="${url}"/>'>
<input type="hidden" name="_method" value="list">
<input type="hidden" name="mr_no" value="${ifn:cleanHtmlAttribute(param.mr_no)}">
<input type="hidden" name="mrNoLink" value="${ifn:cleanHtmlAttribute(portalLink)}"/>
<input type="hidden" name="mandate_emr_comments" id="mandate_emr_comments" value="" />


<table width="100%" height="100%" border="0">
   <c:choose>
		<c:when test ="${not empty allDocs}" >
			<tr>
				<td valign="top" width="30%">
					<fieldset class="fieldSetBorder"><legend class="fieldSetLabel"><insta:ltext key="medicalrecords.patientemr.view.searchfields"/></legend>
						<table class="formtable">
							<c:choose>
								<c:when test="${actionId ne 'visit_emr_screen'}">
								<tr>
									<td align="left">
										<insta:ltext key="medicalrecords.patientemr.view.basedon"/>:
										<input type="hidden" name="VisitId"  value="${empty param.visit_id? visit_id: visit_id}"/>
										<select  name="filterType" onchange="getEMRSearchResults();" class="dropdown">
											<option value="visits" ><insta:ltext key="medicalrecords.patientemr.view.visit"/></option>
											<option value="docType" ><insta:ltext key="medicalrecords.patientemr.view.documenttype"/></option>
										</select>
									</td>
								</tr>
								</c:when>
								<c:otherwise>
									<input type="hidden" name="filterType" value="docType">
									<input type="hidden" name="VisitId" value="${empty param.visit_id? visit_id: visit_id}"/>
								</c:otherwise>
						</c:choose>
							<tr id="documentTypeSearch" style="display:none;">
								<td>
									<table class="formtable">
										<tr>
											<td class="formlabel"><insta:ltext key="medicalrecords.patientemr.view.include"/>:</td>
											<td><insta:selectdb name="indocType" table="doc_type" valuecol="doc_type_id" displaycol="doc_type_name" value="${param.indocType}"
															dummyvalue="All" dummyvalueId="*" onchange="docTypeChange(true);" filtered="true" filtercol="status"  title="${docmandatoryText}"/></td>
										<tr>
										<tr>
											<td class="formlabel"><insta:ltext key="medicalrecords.patientemr.view.exclude"/>:</td>
											<td><insta:selectdb name="exdocType" table="doc_type" valuecol="doc_type_id" displaycol="doc_type_name" value="${param.exdocType}"
															dummyvalue="None" dummyvalueId="*" onchange="docTypeChange(false);" filtered="true" filtercol="status"  title="${docmandatoryText}"/></td>
										</tr>
									</table>
								</td>
							</tr>
							<tr id="datesearch" style="${actionId eq 'visit_emr_screen'? 'display:block': 'display:none'}">
								<td style="width: 100%">
									<table style="width: 100%">
										<tr>
											<td  class="formlabel"><insta:ltext key="medicalrecords.patientemr.view.from"/>:</td>
											<td><insta:datewidget name="fromDate" btnPos="left" value="${param.fromDate}"/></td>
										<tr>
										<tr>
											<td class="formlabel" ><insta:ltext key="medicalrecords.patientemr.view.to"/>:</td>
											<td><insta:datewidget name="toDate" btnPos="left" value="${param.toDate}"/></td>
										</tr>
									</table>
								</td>
							</tr>
							<tr>
								<td><input type="button" value="${searchText}" onclick="${actionId eq 'visit_emr_screen'? 'submitVisit();': 'submitValues();'}">
									<c:if test="${duplicateMrNoExists}">
										<a href="javascript:void(0)"
											onclick='return getDuplicatePatients(this, "${ifn:cleanJavaScript(param.mr_no)}");'
											title="${duplicatepatientslist}">
											<img src="${cpath}/images/information.png" class="button" />
										</a>
									</c:if>
								</td>
							</tr>
						</table>
					</fieldset>
					<div>
						<a name="expandAll" id="expandAll" onclick="expandTree()" style="cursor: pointer"><insta:ltext key="medicalrecords.patientemr.view.expandall"/></a>
					</div>
					<fieldset class="fieldSetBorder" style="margin-top: 10px">
						<legend class="fieldSetLabel"><insta:ltext key="medicalrecords.patientemr.view.documents"/></legend>
						<div ><div id="treeDiv1"></div></div>
					</fieldset>
					<table width="100%">
						<tr>
						<td>
						<c:if test="${preferences.modulesActivatedMap.mod_trend == 'Y'}">
							<c:url var="trendreportlink" value="pages/DiagnosticModule/TestTrendReport.do?method=getScreen"/>
							  <a href="${cpath}/${trendreportlink}&mrno=${empty param.mr_no? mr_no: param.mr_no}" target="_blank"><insta:ltext key="medicalrecords.patientemr.view.testtrendreport"/></a>
						| <a href="${cpath}/pages/Vitals/VitalTrendReport.do?method=getScreen&mrno=${empty param.mr_no? mr_no: param.mr_no}" target="_blank"><insta:ltext key="medicalrecords.patientemr.view.vitalstrendreport"/></a>
							<c:if test="${actionId eq 'visit_emr_screen'}">
								<c:url var="patientEMRlink" value="/emr/EMRMainDisplay.do?_method=list"/>
								| <a href="${patientEMRlink}&mr_no=${empty param.mr_no? mr_no: param.mr_no}" target="_blank"><insta:ltext key="medicalrecords.patientemr.view.patientemrsearch"/></a>
							</c:if>
							<c:if test="${not empty mrnoUrl}">
							| <a href="${mrnoUrl}${empty param.mr_no? mr_no: param.mr_no}" target="_blank"><insta:ltext key="medicalrecords.patientemr.view.patientpacsrepository"/></a>
							</c:if>
						</c:if>
						<c:if test="${preferences.modulesActivatedMap.mod_malaffi == 'Y' && roleId != 1 && hasMalaffiRole}">
							| <a href="${cpath}/malaffi/patient/${empty param.mr_no? mr_no: param.mr_no}.htm" target="_blank">Malaffi</a>
						</c:if>
						<insta:externalLinks screenId="${screenId}" centerId="${centerId}" mrNo="${empty param.mr_no? mr_no: param.mr_no}" visitId="${visit_id}"/>
						</td>
					</tr>
				 </table>
				</td>
				<td width="1%"></td>
				<td  valign="top" width="70%">
					<fieldset class="fieldSetBorder" style="position: relative;">
						<div id="emrLoader" style="position: absolute; top:14px; left:0; right:0; bottom:0; background-color: #FFFFFF; padding-top: 40%; color: #000000; opacity:0.8; text-align: center; box-sizing: border-box; z-index: 1; font-size: 20px; display:none;">Loading...</div>
						<legend class="fieldSetLabel"><insta:ltext key="medicalrecords.patientemr.view.documentdetails"/></legend>
						<table border="0" width="100%">
							<tr>
								<td>
									<table width="100%" >
										<tr>
											<td valign="top">
												<table id="documentSummary" class="formtable">
												</table>
											</td>
										</tr>
										<tr>
											<td valign="top">
												<table border="0" width="100%" height="600px">
													<tr>
														<td valign="top">
															<iframe  id="display1" name="display1" src="" height="99%" width="100%" frameborder="1" >
															</iframe>
														</td>
													</tr>
												</table>
											</td>
										</tr>
										<tr>
											<td>
												<table id="navigate" class="formtable" style="margin-top: 0px">
													<tr>
														<td><a href='javascript:void(0)' id="prevLink" target='display1' style='background-color:#fff'>&lt; <insta:ltext key="medicalrecords.patientemr.view.prev"/></a> |
															<a href='javascript:void(0)' id="nextLink" target='display1' style='background-color:#fff'><insta:ltext key="medicalrecords.patientemr.view.next"/> &gt;</a>
														</td>
														<td></td>
														<td></td>
														<td></td>
														<td style="width: 250px;text-align: right">
															<insta:selectdb name="printerId" id="printerId" table="printer_definition"
																valuecol="printer_id"  displaycol="printer_definition_name" dummyvalue="${dummyvalue}"
																value="${pref.map.printer_id}"/>

														</td>
														<td style="width: 20px"><a href="#" id="imgLink" target="_blank"><img class="newWindow" id="newWindowImg" src="${cpath}/images/cleardot.gif"/></a></td>

													</tr>
												</table>
											</td>
										</tr>
									</table>
								</td>
							</tr>
						</table>
					</fieldset>
				</td>
			</tr>
		</c:when>
		<c:when test="${empty allDocs && (not empty param.mr_no|| not empty mr_no)}">
			<tr><td  valign="top" align="center" style="font-size:15pt;"><insta:ltext key="medicalrecords.patientemr.view.norecordstodisplay"/></td></tr>
		</c:when>
	</c:choose>
</table>
</form>
	<div id="emrCommentsDiv" style="display: none">
	<div class="bd">
		<form name="emr_comments_form" id="emr_comments_form" action="" method="post">
		<input type="hidden" name="mr_no" id="emr_mr_no" value="${param.mr_no}" />
		<input type="hidden" name="visit_id" id="emr_visit_id" value="${param.visit_id}" >
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel"><insta:ltext key="medicalrecords.patientemr.view.justificationComments"/></legend>
			<table >
				<tr>
					<td>
						<textarea name="comments" id="emrCommentsFieldId" style="width: 470px; height: 110px; resize: none"></textarea>
					</td>
				</tr>
			</table>
		</fieldset>
			<table width="100%">
			<tr>
				<td width="50%">
					<input type="button" name="btnAmendOk" id="btnDisOk" value="Save" onclick="saveComments();"/>
				</td>
				<td width="50%">
					<label style="text-align: right; float: right">min. 30 chars</label>	
				</td>
			</tr>
			</table>	
		</form>
	</div>
</div>
	<jsp:include page="duplicatePatients.jsp"/>
	<script>
		var mrnoFromAction = '${ifn:cleanJavaScript(param.mr_no)}';
		var VisitId = '${ifn:cleanJavaScript(param.VisitId)}';
		var allDocsList = <%=request.getAttribute("filteredDocs")!=null?request.getAttribute("filteredDocs"):"''"%>;
		var filterTypeFromAction = '${ifn:cleanJavaScript(filterTypeFromAction)}';
		var docTypeDetails = ${docTypeValues};
		var orderUrl = '${orderUrl}';
		var printerId = '${pref.map.printer_id}';
		$(function() {
			$("#treeDiv1").on("click", "a.ygtvlabel", function(evt){
				var ctrlModifier = isMac ? evt.metaKey : evt.ctrlKey;
				if (!ctrlModifier) {
				    if ($(this).hasClass("disabled")) {
				    	evt.preventDefault();
				    } else {
				        $(this).addClass("disabled");
				        $("#emrLoader").show();
				    }
				}
			});
			$("#display1").load(function(){ 
				$("#emrLoader").hide();
				$("a.ygtvlabel").removeClass("disabled");
			});
		});
	</script>
</body>
</html>
