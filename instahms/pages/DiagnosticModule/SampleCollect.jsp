<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="autoGenerate" value='<%= GenericPreferencesDAO.getdiagGenericPref().get("autogenerate_sampleid") %>' scope="request"/>
<jsp:useBean id="currentDate" class="java.util.Date"/>
<c:set var="currDate">
	<fmt:formatDate value="${currentDate}" pattern="dd-MM-yyyy"/>
</c:set>
<c:set var="currTime">
	<fmt:formatDate value="${currentDate}" pattern="HH:mm"/>
</c:set>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<insta:link type="script" file="date_go.js" />
<insta:link type="script" file="hmsvalidation.js" />
<insta:link type="script" file="instadate.js" />
<insta:link type="js" file="diagnostics/CollectSamples.js" />
<title>${ifn:cleanHtml(param.title)} - <insta:ltext key="laboratory.pendingsamples.list.instahms"/></title>

	<script type="text/javascript">
		var outHouseNameList=<%= request.getAttribute("outhouseNamesJSON") %>;
 		var cpath = '${cpath}';
		var samplePrintType = '${genPrefs.sampleCollectionPrintType}';
		var form = document.sampleForm;
		var isGenerateReq = "${genPrefs.independentGenerationOfSampleId}";
		var autogenerate = '${autoGenerate}';
		var cpath = "${cpath}";
		var testId = '${ifn:cleanJavaScript(param.testId)}';
		var sampleContainers = <%= request.getAttribute("sampleContainers") %>;
		var date = '${currDate}';
		var time = '${currTime}';
		var rejectionList = <%= request.getAttribute("rejectionList") %>;
		<%-- var outSourceChainJSON = <%= request.getAttribute("outSourceChainJSON") %>; --%>
		var collectSampleUrl = '${urlRightsMap.lab_pending_samples_search == 'A' ? 'PendingSamplesSearch' : 'PendingSamples'}';

	</script>
	<style>
		.dropdown {
			border-top:1px #999 solid;
			border-left:1px #999 solid;
			border-bottom:1px #ccc solid;
			border-right:1px #ccc solid;
			color:#666;
			height:20px;
			width:130px;
			padding:2px 0px 0px 2px;
			vertical-align: middle;
		}
	</style>
<insta:js-bundle prefix="diagnostics.collectsample"/>
<insta:js-bundle prefix="registration.patient"/>
</head>
<body onload="init();" class="yui-skin-sam">
<c:set var="select">
 <insta:ltext key="selectdb.dummy.value"/>
</c:set>
<c:set var="all">
 <insta:ltext key="laboratory.signedoffreportslist.search.all"/>
</c:set>
<c:set var="labreportslist">
 <insta:ltext key="laboratory.reconductiontests.test.labreportslist"/>
</c:set>
<c:set var="radreportslist">
 <insta:ltext key="laboratory.reconductiontests.test.radreportslist"/>
</c:set>
<c:set var="labtransferbar">
 <insta:ltext key="laboratory.transfersamples.list.laboratorytransfersamples"/>
</c:set>
<c:set var="labtransfermanual">
 <insta:ltext key="laboratory.transfersamples.list.laboratorytransfersamples.search"/>
</c:set>
<c:set var="okButton">
 <insta:ltext key="laboratory.transfersamples.list.laboratorytransfersamples.ok"/>
</c:set>
<c:set var="nextButton">
 <insta:ltext key="laboratory.transfersamples.list.laboratorytransfersamples.next"/>
</c:set>
<c:set var="prevButton">
 <insta:ltext key="laboratory.transfersamples.list.laboratorytransfersamples.prev"/>
</c:set>
<c:set var="cancel">
 <insta:ltext key="laboratory.transfersamples.list.laboratorytransfersamples.cancel"/>
</c:set>
<c:set var="saveBtn">
 <insta:ltext key="laboratory.transfersamples.list.laboratorytransfersamples.save"/>
</c:set>
<c:set var="saveprintBtn">
 <insta:ltext key="laboratory.transfersamples.list.laboratorytransfersamples.save.print"/>
</c:set>
<h1 style="float: left;">${ifn:cleanHtml(param.title)} </h1>

<c:choose>
	<c:when test="${urlRightsMap.lab_pending_samples_search == 'A' }">
		<c:set var="urltext" value="PendingSamplesSearch"/>
	</c:when>
	<c:otherwise>
		<c:set var="urltext" value="PendingSamples"/>
	</c:otherwise>
</c:choose>
<insta:patientsearch searchType="visit" searchUrl="${urltext }.do" buttonLabel="Find"
	searchMethod="getSampleCollectionScreen" fieldName="visitid" />
<insta:feedback-panel />
<insta:patientdetails visitid="${patientvisitdetails.map.patient_id}" showClinicalInfo="true"/>
<form  method="POST" name="sampleForm" action="${urltext }.do">
<input type="hidden" name="_method" value="saveSamples"/>
<input type="hidden" name="visitid" value="${ifn:cleanHtmlAttribute(param.visitid)}"/>
<input type="hidden" name="mrno" value="${patientvisitdetails.map.mr_no}"/>
<input type="hidden" name="category" value="${ifn:cleanHtmlAttribute(param.category)}"/>
<input type="hidden" name="pageNum" value="${ifn:cleanHtmlAttribute(param.pageNum)}">
<input type="hidden" name="sampleNo" value="${ifn:cleanHtmlAttribute(param.sampleNo)}">
<input type="hidden" name="sampleDates" value="${ifn:cleanHtmlAttribute(param.sampleDates)}"/>
<input type="hidden" name="sampleTypes" value="${ifn:cleanHtmlAttribute(param.sampleTypes)}>"/>
<input type="hidden" name="outSourceSampleNo" value="${ifn:cleanHtmlAttribute(param.outSourceSampleNos)}">
<input type="hidden" name="outSourceName" value="${ifn:cleanHtmlAttribute(param.outSourceName)}"/>
<input type="hidden" name="outsourceDestType" value="${ifn:cleanHtmlAttribute(param.outsourceDestType)}"/>
<input type="hidden" name="prescribedIds" value="${ifn:cleanHtmlAttribute(param.prescribedIds)}"/>
<input type="hidden" name="outSourceSampleStatuses" value="${ifn:cleanHtmlAttribute(param.outSourceSampleStatuses)}" />
<input type="hidden" name="from" value="internal" />
<input type="hidden" name="reqAutoGenId" value="" />
<input type="hidden" name="needPrint" value="${ifn:cleanHtmlAttribute(param.needPrint)}"/>
<input type="hidden" name="contextPath"	value="${cpath}"/>
<input type="hidden" name="printType" value="${genPrefs.sampleCollectionPrintType}"/>
<input type="hidden" name="title" value="${ifn:cleanHtmlAttribute(param.title)}" id="title"/>
<c:set var="centerId" value="${patientvisitdetails.map.center_id}" />
<fieldset class="fieldSetBorder"><legend
	class="fieldSetLabel"><insta:ltext key="laboratory.pendingsamples.list.collectiondetails"/></legend>
<table class="formtable">
	<tr>
		<td class="formlabel"><insta:ltext key="laboratory.pendingsamples.list.collectioncenter"/> </td>
		<td class="forminfo">${collectionCenter.map.collection_center}</td>
		<td class="formlabel"><insta:ltext key="laboratory.pendingsamples.list.collectedby"/></td>
		<td><b>${ifn:cleanHtml(userid)}</b></td>
		<td class="formlabel"></td>
		<td></td>
	</tr>
</table>
</fieldset>
<table class="formtable" cellpadding="0" cellspacing="0" width="100%"
	border="0">
	<tr>
		<td class="formlabel"><insta:ltext key="laboratory.pendingsamples.list.order"/>&nbsp;<insta:ltext key="laboratory.pendingsamples.list.date"/> </td>
		<td>
			<select name="filterDate" id="filterDate" class="dropdown" onchange="fillOrdernosCorresDate(),onChangeSampleDepartment();">
				<option value="">${select}</option>
				<c:forEach items="${orderDatesList}" var="orderDate">
				<fmt:formatDate value="${orderDate.map.pres_date}" pattern="dd-MM-yyyy" var="orderDate"/>
				<option value="${orderDate}" ${orderDate == currDate ? 'selected' : ''}>${orderDate}</option>
				</c:forEach>
			</select>
		</td>
		<td class="formlabel"><insta:ltext key="laboratory.pendingsamples.list.order"/>&nbsp;<insta:ltext key="laboratory.pendingsamples.list.number"/>: </td>
		<td>
			<select name="fiterOrderno" id="filterOrderno" class="dropdown" onchange="onChangeSampleDepartment();">
				<option value=""><insta:ltext key="laboratory.signedoffreportslist.search.all"/></option>
			</select>
		</td>
	</tr>
	<tr>
		<td class="formlabel"><insta:ltext key="laboratory.pendingsamples.list.filter"/>&nbsp;<insta:ltext key="laboratory.pendingsamples.list.sample"/>&nbsp;<insta:ltext key="laboratory.pendingsamples.list.department"/>: </td>
		<td>
			<insta:selectdb name="filterSamples" table="diagnostics_departments" valuecol="ddept_id"
			displaycol="ddept_name" orderby="ddept_name" dummyvalue="${all}" dummyvalueId=""
			onchange="onChangeSampleDepartment();" value="${userDept}"/>
		</td>
		<td class="formlabel"><insta:ltext key="laboratory.pendingsamples.list.hide"/>&nbsp;<insta:ltext key="laboratory.pendingsamples.list.collected"/>: </td>
		<td><input type="checkbox" name="hideCollected" onclick="onChangeSampleDepartment();"/></td>
	</tr>
</table>

<div class="resultList">
<fieldset class="fieldSetBorder"><legend
	class="fieldSetLabel"><insta:ltext key="laboratory.pendingsamples.list.tests"/></legend>
<table class="resultList dialog_displayColumns" width="100%" cellspacing="0" cellpadding="0"
	id="sampleList" border="0">
	<tr>
		<th></th>
		<th class="noremove"><insta:ltext key="laboratory.pendingsamples.list.samplefor"/></th>
		<th><insta:ltext key="laboratory.pendingsamples.list.orderdatetime"/></th>
		<th><insta:ltext key="laboratory.pendingsamples.list.orderno"/></th>
		<th><insta:ltext key="laboratory.pendingsamples.list.department"/></th>
		<th class="noremove"><insta:ltext key="laboratory.pendingsamples.list.sampletype"/></th>
		<th class="noremove"><insta:ltext key="laboratory.pendingsamples.list.testname"/></th>
		<th><insta:ltext key="laboratory.pendingsamples.list.labno"/></th>
		<th class="noremove"><insta:ltext key="laboratory.pendingsamples.list.sampleno"/></th>
		<th class="noremove"><insta:ltext key="laboratory.pendingsamples.list.transferto"/></th>
		<th><insta:ltext key="laboratory.pendingsamples.list.sampleinstructions"/></th>
		<th></th>
	</tr>
	<c:forEach items="${saplesList}" var="sample">
		<c:set var="flagColor">
			<c:choose>
				<c:when test="${sample.house_status eq 'O'}">violet</c:when>
				<c:otherwise>empty</c:otherwise>
			</c:choose>
		</c:set>
		<c:set var="sampleStatusText" value="Pending" />
		<c:set var="sampleStatusValue" value="" />
		<c:set var="rowStyle" value="" />
		<c:set var="collect" value="" />
		<c:set var="generate" value="" />

		<c:if test="${sample.sample_status eq 'P'}">
			<c:set var="sampleStatusText" value="Partial" />
			<c:set var="sampleStatusValue" value="P" />
			<c:set var="generate" value="checked disabled" />
			<c:set var="rowStyle" value="" />
		</c:if>
		<c:if test="${sample.sample_status eq 'C'}">
			<c:set var="sampleStatusText" value="Completed" />
			<c:set var="sampleStatusValue" value="C" />
			<c:set var="collect" value="checked disabled" />
			<c:set var="generate" value="checked disabled" />
			<c:set var="rowStyle" value="" />
		</c:if>
		<c:if test="${sample.sample_status eq 'A'}">
			<c:set var="sampleStatusText" value="Asserted" />
			<c:set var="sampleStatusValue" value="A" />
			<c:set var="collect" value="checked disabled" />
			<c:set var="generate" value="checked disabled" />
			<c:set var="rowStyle" value="" />
		</c:if>
		<tr class="${rowStyle }">
			<td>
				<c:if test="${ !sample.collectible }">
					<img src="${cpath}/images/alert.png" class="flag" title="<insta:ltext key="laboratory.pendingsamples.list.dependenttest"/>: ${sample.dependent_test_name }"/>
				</c:if>
			</td>
			<td>
				<input type="checkbox" name="sampleFor" id="sampleFor" ${generate} onclick="setEmptyToStatus(this);"/>
			</td>
			<td>
				<label><fmt:formatDate value="${sample.pres_date}" pattern="dd-MM-yyyy HH:mm" /></label>
			</td>
			<td>
				<label>${sample.common_order_id}</label>
			</td>
			<td><label>${sample.ddept_name }</label>
				<input type="hidden" name="ddept_id" value="${sample.ddept_id }" />
				<input type="hidden" name="sample_date" id="sample_date"
					value="<fmt:formatDate value="${sample.sample_date}" pattern="dd-MM-yyyy HH:mm"/>" />
				<input type="hidden" name="sampleDate"
					value="<fmt:formatDate value="${sample.sample_date}" pattern="dd-MM-yyyy"/>" />
				<input type="hidden" name="sampleTime"
					value="<fmt:formatDate value="${sample.sample_date}" pattern="HH:mm"/>" />
					<input type="hidden" name="collectable" value="${ sample.collectible }"/>
			</td>
			<td>
				<c:choose>
					<c:when test="${sample.sample_status == 'C' || sample.sample_status == 'P' || sample.sample_status == 'A'}">
						<label>${sample.sample_type}</label>
						<input type="hidden" name="sampleTypeForTests" value="${sample.sample_type}"/>
					</c:when>
					<c:otherwise>
						<insta:selectdb name="sampleTypeForTests"
								id="sampleTypeForTests" table="sample_type" valuecol="sample_type_id"
								displaycol="sample_type" dummyvalue="${select}" dummyvalueId="" value="${sample.sample_type_id}" onchange="addToSamplesGrid(this);" />
					</c:otherwise>
				</c:choose>
				<input type="hidden" name="sampleType" id="sampleType" value="${sample.sample_type}" />
			</td>
			<td><img src="${cpath}/images/${flagColor}_flag.gif"
				class="flag" />
				<c:choose>
					<c:when test="${sample.priority=='S'}">
						<b><font color="#444444"><insta:truncLabel value="${sample.test_name}" length="30"></insta:truncLabel></font></b>
					</c:when>
					<c:otherwise>
						<insta:truncLabel value="${sample.test_name}" length="30"></insta:truncLabel>
					</c:otherwise>
				</c:choose>
				<input type="hidden" name="testName" id="testName" value="${sample.test_name}" />
				<input type="hidden" name="test_id" id="test_id" value="${sample.test_id}" />
				<input type="hidden" name="prescribed_id" id="prescribed_id" value="${sample.prescribed_id}" />
				<input type="hidden" name="sample_type_id" id="sample_type_id"
					value="${(sample.sample_status == 'C' || sample.sample_status == 'P' || sample.sample_status == 'A') ? sample.sc_sample_type_id : sample.sample_type_id}" />
				<input type="hidden" name="house_status" id="house_status" value="${sample.house_status}" />
				<input type="hidden" name="sample_collection_id" id="sample_collection_id" value="${sample.sample_collection_id}"/>
				<input type="hidden" name="conducted" id="conducted" value="${sample.conducted }" />
				<input type="hidden" name="cancelled" value="N" />
				<input type="hidden" name="edited" value="N">
				<input type="hidden" name="sample_source_id" id="sample_source_id" value="${sample.sample_source_id }"/>
				<input type="hidden" name="flag" value="${flagColor }" />
				<input type="hidden" name="prescribedDate"
					value="<fmt:formatDate value="${sample.pres_date}" pattern="dd-MM-yyyy"/>" />
				<input type="hidden" name="prescribedTime"
					value="<fmt:formatDate value="${sample.pres_date}" pattern="HH:mm"/>" />
				<input type="hidden" name="outsource_dest_id" id="outsource_dest_id" value="${sample.outsource_dest_id }"/>
				<input type="hidden" name="qty" id="qty" value="${sample.sample_qty}"/>

				<input type="hidden" name="existingsample_date" id="existingsample_date"
					value="<fmt:formatDate value="${sample.sample_date}" pattern="dd-MM-yyyy HH:mm"/>" />
				<input type="hidden" name="existingsampleDate"
					value="<fmt:formatDate value="${sample.sample_date}" pattern="dd-MM-yyyy"/>" />
				<input type="hidden" name="existingsampleTime"
					value="<fmt:formatDate value="${sample.sample_date}" pattern="HH:mm"/>" />
				<input type="hidden" name="existingoutsource_dest_id" id="existingoutsource_dest_id" value="${sample.outsource_dest_id }"/>
				<input type="hidden" name="existingsample_source_id" id="existingsample_source_id" value="${sample.sample_source_id }"/>
				<input type="hidden" name="existingqty" id="existingqty" value="${sample.sample_qty}"/>
				<input type="hidden" name="existingStatus" id="existingStatus" value="${sample.sample_status}" />
				<input type="hidden" name="existingSampleNo" value="${sample.sample_sno}" />
				<input type="hidden" name="commonOrderId" value="${sample.common_order_id}"/>

			</td>
			<td><label>${sample.labno}</label></td>

			<td><c:choose>
				<c:when
					test="${sample.sample_status == 'C' || sample.sample_status == 'P' || sample.sample_status == 'A'}">
					<a
						onclick="return appendTemplateName(
							this,'${sample.sample_sno}','${sample.sample_date}','${sample.type_of_specimen}')"
						target="blank" href="#" title="Click to RePrint Sample Number">${sample.sample_sno}</a>
					<input type="hidden" name="sampleId" id="sampleId"
						value="${sample.sample_sno}">
					<input type="hidden" name="existingSampleStatus"
						id="existingSampleStatus" value="${sample.sample_status}" />
					<input type="hidden" name="new" value="N" />
				</c:when>
				<c:when test="${genPrefs.autoSampleIdRequired eq 'N'}">
					<label id="sampleNumber"></label>
					<input type="hidden" name="sampleId" id="sampleId" value="">
					<input type="hidden" name="existingSampleStatus"
						id="existingSampleStatus" value="" />
					<input type="hidden" name="new" value="Y" />
				</c:when>
				<c:when test="${sample.sample_status == 'A'}">
					<input type="hidden" name="existingSampleStatus"
						id="existingSampleStatus" value="${sample.sample_status}" />
				</c:when>
				<c:otherwise>
					<label id="sampleNumber"></label>
					<input type="hidden" name="sampleId" id="sampleId" value="">
					<input type="hidden" name="existingSampleStatus"
						id="existingSampleStatus" value="" />
					<input type="hidden" name="new" value="Y" />
				</c:otherwise>
			</c:choose>
			</td>
			<td>
				<c:choose>
					<c:when test="${sample.sample_status == 'C' || sample.sample_status == 'P' || sample.sample_status == 'A'}">
						<label>${sample.out_source_name}</label>
						<input type="hidden" name="outSourceNameList" value=""/>
					</c:when>
					<c:when test="${sample.house_status eq 'O'}">
					<!-- setOutSourceChain(this);  -->
						<select name="outSourceNameList" class="dropdown" onchange="addToSamplesGrid(this);">
						    <c:if test="${fn:length(outsourcesAgainstTests[sample.test_id]) gt 1 }">
							   <option value="">${select}</option>
						    </c:if>
						    <c:set var="selectedOutsource" value=""/>
							<c:if test="${not empty outsourcesAgainstTests}">
							<c:forEach items="${outsourcesAgainstTests[sample.test_id]}" var="ohMap">
									<option value="${ohMap.OUTSOURCE_DEST_ID}" ${ohMap.DEFAULT_OUTSOURCE == 'Y' ? 'selected' : '' }>${ohMap.OUTSOURCE_NAME}</option>
									<c:if test="${ohMap.DEFAULT_OUTSOURCE =='Y' || fn:length(outsourcesAgainstTests[sample.test_id]) eq 1 }">
										<c:set var="selectedOutsource" value="${ohMap.OUTSOURCE_DEST_ID}"/>
									</c:if>
							</c:forEach>
							</c:if>
						</select>
					</c:when>
					<c:otherwise>
						<input type="hidden" name="outSourceNameList" value=""/>
					</c:otherwise>
				</c:choose>
				<input type="hidden" name="outSourcename" id="outSourcename" value="" />
				<input type="hidden" name="outSourceId" id="outSourceId" value="${sample.outsource_dest_id}" />
			</td>
			<td><insta:truncLabel
				value="${sample.sample_collection_instructions }" length="30" />
				<input type="hidden" name="sample_status" id="sample_status" value="${sample.sample_status }">
				<input type="hidden" name="defaultSampleStatus" id="defaultSampleStatus" value="${sampleStatusValue}">
			</td>
			<td>
				<c:set var="show_rej_icon" value="false"/>
				<c:forEach items="${rejection_det}" var="rejection">
					<c:if test="${rejection.test_prescribed_id eq sample.prescribed_id}"><c:set var="show_rej_icon" value="true"/></c:if>
				</c:forEach>
				<c:set var="collect_status" value="true"/>
				<c:if test="${sample.sample_status eq 'C' || sample.sample_status eq 'A'}"><c:set var="collect_status" value="false"/></c:if>
				<c:if test="${show_rej_icon && collect_status}">
					<a onclick="getRejectionDetails(this,'${sample.prescribed_id}');" href="javascript:void(0)"
							id="rejection_dialog" title='<insta:ltext key="laboratory.pendingsamples.list.rejectiondetails"/>'> <img
							src="${cpath}/images/alert.png" class="button" /> </a>
				</c:if>
			</td>
		</tr>
	</c:forEach>
</table >
</fieldset>
</div>
<table>
	<tr>
		<td>&nbsp;</td>
	</tr>
</table>
<div class="resultList">
<fieldset class="fieldsetBorder" ><legend class="fieldSetLabel"><insta:ltext key="laboratory.signedoffreportslist.search.samples"/></legend>
<table class="resultList" width="100%" cellspacing="0" cellpadding="0" id="sampleNosList" >
	<tr>
		<c:if test="${genPrefs.independentGenerationOfSampleId == 'Y'}">
		<th title='<insta:ltext key="laboratory.pendingsamples.list.generatesampleno"/>' class="noremove">
				<input type="checkbox" title='<insta:ltext key="laboratory.pendingsamples.list.generatesampleno"/>' onclick="selectAllSamples(this,'generate')" id="generateAll"/><insta:ltext key="laboratory.pendingsamples.list.generate"/>
		</th>
		</c:if>
		<th  title='<insta:ltext key="laboratory.pendingsamples.list.collectsample"/>'  class="noremove">
				<input type="checkbox" title='<insta:ltext key="laboratory.pendingsamples.list.collectsample"/>' onclick="selectAllSamples(this,'collect')" id="collectAll"/><insta:ltext key="laboratory.pendingsamples.list.collect"/>
		</th>
		<th><insta:ltext key="laboratory.pendingsamples.list.sampledate"/></th>
		<th><insta:ltext key="laboratory.pendingsamples.list.sampletype"/></th>
		<th><insta:ltext key="laboratory.pendingsamples.list.container"/></th>
		<th><insta:ltext key="laboratory.pendingsamples.list.sampleno"/></th>
		<th><insta:ltext key="laboratory.pendingsamples.list.source"/></th>
		<th><insta:ltext key="laboratory.pendingsamples.list.qty"/></th>
		<th><insta:ltext key="laboratory.pendingsamples.list.transferto"/></th>
		<th>&nbsp;</th>
	</tr>
   <c:set var="distinctSamplesLen" value="${fn:length(distinctSamples)}" />
	<c:forEach begin="1" end="${distinctSamplesLen+1}" var="index">
		<c:set var="distinctBean" value="${distinctSamples[index-1]}" />
		<c:if test="${empty distinctBean}"><c:set var="style" value="style='display: none'"/></c:if>

		<tr ${style}>
			<c:if test="${genPrefs.independentGenerationOfSampleId == 'Y'}">
			<td>
				<input type="checkbox" name="generate" id="generate" ${distinctBean.map.sample_status eq 'P' || distinctBean.map.sample_status eq 'C' ? 'checked disabled' : '' }
					onclick="setValuesToTestGrid('generate', this);"/>
			</td>
			</c:if>
			<td>
				<input type="checkbox" name="collect" id="collect" ${distinctBean.map.sample_status eq 'C' ? 'checked disabled' : '' } onclick="disableGenerate(this); setValuesToTestGrid('collect', this);" />
			</td>
			<td>
				<label> <fmt:formatDate	value="${distinctBean.map.sample_date}" pattern="dd-MM-yyyy HH:mm" /> </label>
				<input type="hidden" name="sg_sample_date" id="sg_sample_date"
					value="<fmt:formatDate value="${distinctBean.map.sample_date}" pattern="dd-MM-yyyy HH:mm"/>" />
				<input type="hidden" name="sg_sampleDate"
					value="<fmt:formatDate value="${distinctBean.map.sample_date}" pattern="dd-MM-yyyy"/>" />
				<input type="hidden" name="sg_sampleTime"
					value="<fmt:formatDate value="${distinctBean.map.sample_date}" pattern="HH:mm"/>" />
			</td>
			<td>
				<c:choose>
					<c:when test="${distinctBean.map.sample_status eq 'C' || distinctBean.map.sample_status eq 'P' || distinctBean.map.sample_status eq 'A'}">
						<label>${distinctBean.map.sample_type}</label>
						<input type="hidden" name="sg_sample_type_id" id="sg_sample_type_id" value="${distinctBean.map.sc_sample_type_id}" />
						<input type="hidden" name="sg_sampleType" id="sg_sampleType" value="${distinctBean.map.sample_type}" />
					</c:when>
					<c:otherwise>
						<label>${distinctBean.map.master_sample_type}</label>
						<input type="hidden" name="sg_sample_type_id" id="sg_sample_type_id" value="${distinctBean.map.sample_type_id}" />
						<input type="hidden" name="sg_sampleType" id="sg_sampleType" value="${distinctBean.map.master_sample_type}" />
					</c:otherwise>
				</c:choose>
			</td>
			<td>
				<label>${distinctBean.map.sample_container}</label>
			</td>
			<td>
				<label>${distinctBean.map.sample_sno}</label>
				<input type="hidden" name="sg_sample_sno" id="sg_sample_sno" value="${distinctBean.map.sample_sno}" />
			</td>
			<td><label>${distinctBean.map.source_name }</label>
				<input type="hidden" name="sg_sample_source_id" id="sg_sample_source_id" value="${distinctBean.map.sample_source_id }" />
			</td>
			<td>
				<label>${empty distinctBean.map.sample_qty ? 1 : distinctBean.map.sample_qty}</label>
				<input type="hidden" name="sg_qty" id="sg_qty" value="${distinctBean.map.sample_qty}" />
			</td>
			<td><label>${distinctBean.map.out_source_name}</label>
				<input type="hidden" name="sg_outsource_dest_id" id="sg_outsource_dest_id" value="${distinctBean.map.outsource_dest_id }" />
				<input type="hidden" name="sg_sample_status" id="sg_sample_status" value="${distinctBean.map.sample_status}" />
				<input type="hidden" name="sg_selected_status" value="${distinctBean.map.sample_status}" />
			</td>
			<td>
				<c:choose>
					<c:when test="${distinctBean.map.sample_status ne 'C'}">
						<a onclick="getEditDialog(this,false);" href="javascript:void(0)"
							id="loadDialog" title='<insta:ltext key="laboratory.pendingsamples.list.editsamples"/>'> <img
							src="${cpath}/icons/Edit.png" class="button" /> </a>
					</c:when>
					<c:otherwise>
						<img class="imgDelete" src="${cpath}/icons/Edit1.png" />
					</c:otherwise>
				</c:choose>
			</td>
		</tr>
	</c:forEach>
</table>
</fieldset>
</div>

<div class="screenActions">
	<input type="submit" id="save" name="save" value="${saveBtn}" onclick="return getValue(this.value);"/>
	<input type="submit" id="print"  name="save&print" value="${saveprintBtn}" onclick="return getValue(this.value);"/>
	<c:choose>
		<c:when test="${category == 'DEP_LAB'}">
			<c:set var="url" value="${('Laboratory')}"/>
			<c:set var="reportListLink" value="${labreportslist}"/>
		</c:when>
		<c:otherwise>
			<c:set var="url" value="${('Radiology')}"/>
			<c:set var="reportListLink" value="${radreportslist}"/>
		</c:otherwise>
	</c:choose>
	| <a href='<c:out value="${cpath}/${url}/schedules.do?_method=getScheduleList&category=${ifn:cleanURL(category)}&mr_no=${patientvisitdetails.map.mr_no}&patient_id=${not empty patientvisitdetails ? '' : patientvisitdetails.map.patient_id}"/>'>${reportListLink}</a>
	<c:if test="${(category == 'DEP_LAB') && (mod_central_lab == 'Y')}">
		<insta:screenlink screenId="lab_transfer_sample_barcode" label="${labtransferbar}" addPipe="true"
				extraParam="?_method=searchBySample" />
		<insta:screenlink screenId="lab_transfer_sample_manual" label="${labtransfermanual}" addPipe="true"
				extraParam="?_method=list&mr_no=${patientvisitdetails.map.mr_no}&sample_transfer_status=P" />
	</c:if>
</div>
<div align="right" style="display: ${genPrefs.sampleCollectionPrintType == 'SL'?'none':'block'}">
	<insta:selectdb name="sampleBardCodeTemplate" table="sample_bar_code_print_templates" valuecol="template_name"
			displaycol="template_name" filtered="false" value="${template_name}"/>
</div>

<div class="legend" style="margin-top: 10px;">
<div class="flag"><img src='${cpath}/images/violet_flag.gif' /></div>
<div class="flagText"><insta:ltext key="laboratory.pendingsamples.list.collect.outsource"/></div>
</div>
</form>

<form name="editform"><input type="hidden" id="editRowId"
	value="" />
<div id="editDialog" style="visibility: hidden">
<div class="bd">
<fieldset class="fieldSetBorder"><legend
	class="fieldSetLabel"><insta:ltext key="laboratory.pendingsamples.list.editsamples"/></legend>
<table cellpadding="0" cellspacing="0" width="100%" id="panel"
	class="formtable">
	<tr>
		<td class="formlabel"><insta:ltext key="laboratory.pendingsamples.list.sampletype"/>:</td>
		<td><label id="eSampleType"></label></td>
		<td class="formlabel"><insta:ltext key="laboratory.pendingsamples.list.sampledate"/>:</td>
		<td><insta:datewidget name="eSampleDate" btnPos="left" value="" />
		<input type="text" name="eSampleTime" class="timefield" size="4" /></td>
		<td class="formlabel"><insta:ltext key="laboratory.pendingsamples.list.samplesource"/></td>
		<td><insta:selectdb name="eSource" id="eSource"
			table="sample_sources" valuecol="source_id" displaycol="source_name"
			dummyvalue="${select}" dummyvalueId="" /></td>
	</tr>
	<tr>
		<td style="display: none;" class="formlabel"><insta:ltext key="laboratory.pendingsamples.list.outhouse"/>:</td>
		<td style="display: none"><select name="eOutHouse" id="eOutHouse" class="dropdown"
			tabindex="2"></select></td>

		<td id="sampleLblid" class="formlabel"><insta:ltext key="laboratory.pendingsamples.list.samplenumber"/>:</td>
		<td id="sampleField"><input type="text" name="eSampleId"
			id="eSampleId" size="8" maxlength="15" tabindex="4"
			onkeypress="if(event.keyCode != 13) return enterAlphaNumeric(event);else return false; "></td>
		<td class="formlabel"><insta:ltext key="laboratory.pendingsamples.list.qty"/>:</td>
		<td><input type="text" name="eQty" id="eQty" value="" class="number" onkeypress="return enterNumOnly(event);" maxlength="2"></td>
	</tr>
</table>
</fieldset>
<input type="button" name="add" id="add" value="${okButton}" onclick="onEdit();"
	tabindex="4" /> <input type="button" name="close" id="close"
	value="${closeButton}" onclick="closeDialog();" tabindex="5" /> <input
	type="button" onclick="showNextOrPrevSample(this)" name="prevBtn"
	value="${prevButton}" /> <input type="button"
	onclick="showNextOrPrevSample(this)" name="nextBtn" value="${nextButton}" />
</div>
</div>
</form>

<form name="showform"><input type="hidden" id="showRowId"
	value="" />
	<div id="showDialog" style="visibility: hidden">
		<div class="bd">
			<fieldset class="fieldSetBorder"><legend
				class="fieldSetLabel"><insta:ltext key="laboratory.transfersamples.list.laboratorytransfersamples.rejectiondetails"/></legend>
				<table cellpadding="0" cellspacing="0" width="100%" id="detailspanel"
					class="formtable">
				</table>
			</fieldset>
			<input type="button" name="add" id="add" value="Ok" onclick="closeDialog();"
						tabindex="4" />
		</div>
	</div>
</form>
</body>
</html>
