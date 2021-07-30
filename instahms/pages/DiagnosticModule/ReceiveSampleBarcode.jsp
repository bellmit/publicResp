<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
 <%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />

<html>
<c:set var="hasResults" value="${not empty samplesList}"/>

<head>
	<title><insta:ltext key="laboratory.receivesamples.list.receivesamples"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="css" file="widgets.css" />
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="js" file="diagnostics/receive_sample_barcode.js"/>
	<insta:js-bundle prefix="laboratory.receivesample"/>
	<insta:js-bundle prefix="laboratory.radiology.receivesample"/>
	<script>
		var toolbarOptions = getToolbarBundle("js.laboratory.radiology.receivesample.toolbar");
		var cpath = '${cpath}';
		var toolbar = {};
		toolbar.Edit ={
			title : toolbarOptions["samplereceivedetails"]["name"],
			imageSrc : "/icons/Edit.png",
			href : "/Laboratory/ReceiveSamples.do?_method=getReceiveSamplesDetails",
			onclick : null
		};
		toolbar.PrintWorkSheet = { title: toolbarOptions["printsampleworksheet"]["name"],
			imageSrc: 'icons/Print.png',
			href: 'Laboratory/SampleWorkSheetPrint.do?_method=printSampleWorkSheet',
			target: '_blank'
		};
		var gServerNow = new Date(<%= (new java.util.Date()).getTime() %>);
	</script>
</head>

<body onload="init(); ajaxForPrintUrls();">
    <c:set var="all">
 		<insta:ltext key="laboratory.receivesamples.list.all"/>
	</c:set>
	<c:set var="receivesamplestatus">
		<insta:ltext key="laboratory.receivesamples.list.pending"/>,
	 	<insta:ltext key="laboratory.receivesamples.list.received"/>
	</c:set>
	<c:set var="mrno">
 		<insta:ltext key="ui.label.mrno"/>
	</c:set>
	<c:set var="sampleCollectionDate">
 		<insta:ltext key="laboratory.receivesamples.list.samplecollectiondate"/>
	</c:set>
	<c:set var="sampleCollectionCenter">
 		<insta:ltext key="laboratory.receivesamples.list.samplecollectioncenter"/>
	</c:set>
	<c:set var="tBatchId">
		<insta:ltext key="laboratory.receivesamples.list.batchid"/>
	</c:set>	
	<h1><insta:ltext key="laboratory.receivesamples.list.laboratoryreceivesamples"/></h1>
	<insta:feedback-panel/>
	<c:set var="actionURL" value="${cpath}/Laboratory/ReceiveSamplesBarcode.do"/>
	<c:set var="searchMethod" value="searchBySample"/>
	<c:set var="method" value="searchBySample"/>

	<form name="receiveSamplesForm" action="${actionURL}">
		<input type="hidden" name="_method" value="${ifn:cleanHtmlAttribute(method)}"/>
		<input type="hidden" name="_searchMethod" value="${searchMethod}"/>
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>

		<insta:search-lessoptions form="receiveSamplesForm">
			<table class="searchBasicOpts" >
				<tr>
					<td class="sboField">
						<div class="sboFieldLabel"><insta:ltext key="laboratory.receivesamples.list.sampleno"/></div>
						<div class="sboFieldInput">
							<c:set var="sampleNo" value="${param._sample_no}"/>
							<c:if test="${not empty sampleNo && !fn:startsWith(sampleNo, ',')}">
								<c:set var="sampleNo" value=",${param._sample_no}"/>
							</c:if>
							<input type="text" name="_sample_no" id="sample_no" value="${ifn:cleanHtmlAttribute(sampleNo)}"/>
						</div>
					</td>
					<td class="sboField">
						<div class="sboFieldLabel">${tBatchId}</div>
						<div class="sboFieldInput">
							<input type="text" name="transfer_batch_id" id="transfer_batch_id" value="${ifn:cleanHtmlAttribute(param.transfer_batch_id)}"/>
						</div>
					</td>
				</tr>
			</table>
		</insta:search-lessoptions>
	</form>
	<form name="resultsForm" method="POST">
		<input type="hidden" name="isPrint" value"" />
		<div class="fltR" style="display: ${not empty samplesList ? 'block' : 'none'}">${fn:length(samplesList)} <insta:ltext key="paginate.records.found"/></div>
		<div style="clear:both"/>
		<div class="resultList" style="padding-top: 5px">
			<table class="resultList dialog_displayColumns" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
				<tr onmouseover="hideToolBar();">
					<th><input type="checkbox" ${not empty samplesList ? 'checked' : ''} onclick="return checkOrUncheckAll('receiveCheck',this)" id="completeAll"/></th>
					<th>#</th>
					<insta:sortablecolumn name="mr_no" title="${mrno}" />
					<th>
						<insta:ltext key="laboratory.receivesamples.list.visitid"/>
						(<insta:ltext key="laboratory.receivesamples.list.sponsor_type"/>)
					</th>
					<th><insta:ltext key="ui.label.patient.name"/></th>
					<th><insta:ltext key="laboratory.receivesamples.list.testname"/></th>
					<insta:sortablecolumn name="sample_date" title="${sampleCollectionDate}"/>
					<th><insta:ltext key="laboratory.receivesamples.list.sampletransferredtime"/></th>
					<insta:sortablecolumn name="collection_center" title="${sampleCollectionCenter}"/>
					<th><insta:ltext key="laboratory.receivesamples.list.samplesource"/></th>
					<th><insta:ltext key="laboratory.receivesamples.list.department"/></th>
					<th><insta:ltext key="laboratory.receivesamples.list.sampletype"/></th>
					<insta:sortablecolumn name="transfer_batch_id" title="${tBatchId}"/>
					<th><insta:ltext key="laboratory.receivesamples.list.sampleno"/></th>
					<th><insta:ltext key="laboratory.receivesamples.list.transferstatus"/></th>
					<th><insta:ltext key="laboratory.receivesamples.list.otherdetails"/></th>
					<th style="width: 150px"></th>
				</tr>
				<c:set var="count" value="0"/>
				<c:forEach items="${samplesList}" var="receiveSample" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{sampleCollectionId: '${receiveSample.map.sample_collection_id}',patient_id: '${receiveSample.map.pat_id}',actionId: '${actionId}',bulkWorkSheetPrint: 'N'},
							[${receiveSample.map.sample_transfer_status != 'P'},true]);"
						id="toolbarRow${st.index}">
						<input type="hidden" name="transferDateTime" id="transferDateTime${count}" value='<fmt:formatDate value="${receiveSample.map.transfer_time}" pattern="dd-MM-yyyy HH:mm"/>'/>
						<c:set var="count" value="${count+1}"/>
						<td style="width:15px;">
							<input type="checkbox" name="receiveCheck" id="receiveCheck" value="${receiveSample.map.sample_collection_id}" ${(receiveSample.map.sample_transfer_status == 'P') ? 'disabled' : 'checked'} />
						</td>
						<td>${((pagedList.pageNumber-1) * pagedList.pageSize) + st.index + 1}</td>
						<td>${receiveSample.map.mr_no }</td>
						<td>${receiveSample.map.pat_id } (${receiveSample.map.patient_sponsor_type})</td>
						<td><insta:truncLabel value="${receiveSample.map.patient_name}" length="25"/></td>
						<td><insta:truncLabel value="${receiveSample.map.test_name}" length="30"/></td>
						<td><fmt:formatDate value="${receiveSample.map.sample_date}" pattern="dd-MM-yyyy HH:mm"/></td>
						<td><fmt:formatDate value="${receiveSample.map.transfer_time}" pattern="dd-MM-yyyy HH:mm"/></td>
						<td>${receiveSample.map.collection_center }</td>
						<td>${receiveSample.map.source_center_name }</td>
						<td><insta:truncLabel value="${receiveSample.map.ddept_name}" length="25"/></td>
						<td><insta:truncLabel value="${receiveSample.map.sample_type}" length="30"/></td>
						<td>${receiveSample.map.transfer_batch_id}</td>
						<td>${receiveSample.map.sample_no }</td>
						<c:choose>
							<c:when test="${receiveSample.map.sample_transfer_status == 'P'}">
								<td><insta:ltext key="laboratory.receivesamples.list.pendingfortransfer"/></td>
							</c:when>
							<c:otherwise>
								<td><insta:ltext key="laboratory.receivesamples.list.transferred"/></td>
							</c:otherwise>
						</c:choose>
						<td><insta:truncLabel value="${receiveSample.map.receipt_other_details}" length="30"/></td>
						<td>
							<c:forTokens items="${receiveSample.map.test_name}" delims="," var="test_name" varStatus="st">
								<c:set var="mandate_additional_info" value="${fn:split(receiveSample.map.mandate_additional_info, ',')[st.index]}"/>
								<c:set var="prescId" value="${fn:split(receiveSample.map.prescribed_id, ',')[st.index]}"/>
								<c:if test="${fn:trim(mandate_additional_info) == 'O'}">
									<c:set var="test_info_viewer"><insta:ltext key="laboratory.receivesamples.list.test_info_viewer"/></c:set>
									<c:url value="/Diagnostics/TestInfoViewer.do" var="testDocUrl">
										<c:param name="_method" value="list"/>
										<c:param name="prescribed_id" value="${fn:trim(prescId)}"/>
										<c:param name="patient_id" value="${receiveSample.map.pat_id}"/>
									</c:url>
									<a href="${testDocUrl}" title="${test_name} ${test_info_viewer}" target="_blank">
										<insta:truncLabel value="${test_name}" length="15"/>
									</a>
									<br/>
								</c:if>
							</c:forTokens>
						</td>
					</tr>
				</c:forEach>
			</table>
		</div>
		<table class="screenActions" style="float:left">
			<tr>
				<td>
					<button type="button" id="markReceived" accessKey="S" onclick="return markReceivedAndPrint('');"><b><u><insta:ltext key="laboratory.receivesamples.list.m"/></u></b><insta:ltext key="laboratory.receivesamples.list.arkreceived"/></button>
				</td>
				<td>&nbsp;&nbsp;&nbsp;&nbsp;</td>
				<td>
					<button type="button" id="print" accessKey="P" onclick="return markReceivedAndPrint('P');"><insta:ltext key="laboratory.receivesamples.list.markreceived"/>
					<b><u><insta:ltext key="laboratory.receivesamples.list.p"/></u></b><insta:ltext key="laboratory.receivesamples.list.rint"/></button>
				</td>
				<td>&nbsp;&nbsp;&nbsp;&nbsp;</td>
				<td>
					<button type="button" id="PrintAll" accessKey="P" onclick="return printAll();"><u><b><insta:ltext key="laboratory.receivesamples.list.p"/></b></u><insta:ltext key="laboratory.receivesamples.list.rintall"/></button>
				</td>
				<td>&nbsp;&nbsp;&nbsp;&nbsp;</td>
				<td valign="middle">
					<label><insta:ltext key="laboratory.receivesamples.list.updateotherdetails"/>:</label>
				</td>
				<td>&nbsp;</td>
				<td>
					<textarea rows="2" cols="20" id="receiptOtherDetails" name="receiptOtherDetails"></textarea>
				</td>
			</tr>
		</table>		
		<div style="clear:both"/>
		<div class="legend" style="margin-top: 10px;display: ${hasResults? 'block' : 'none'}">
			<div class="flagText"><insta:ltext key="laboratory.receivesamples.list.legend.sponsor"/></div>
			<div class="flagText" style="padding-left: 10px"><insta:ltext key="laboratory.receivesamples.list.legend.retail"/></div>
		</div>
	</form>
</body>
</html>