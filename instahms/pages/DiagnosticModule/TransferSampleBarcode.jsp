<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />

<html>
<c:set var="hasResults" value="${not empty samplesList}"/>

<head>
	<insta:js-bundle prefix="laboratory.transfersample"/>
	<insta:js-bundle prefix="laboratory.radiology.transfersample"/>
	<script>
		var toolbarOptions = getToolbarBundle("js.laboratory.radiology.transfersample.toolbar");
		var toolbar = {};
		toolbar.Edit ={
			title : toolbarOptions["sampletransferdetails"]["name"],
			imageSrc : "/icons/Edit.png",
			href : "/Laboratory/TransferSamples.do?_method=getTransferSamplesDetails",
			onclick : null
		};
		toolbar.PrintWorkSheet = { title: toolbarOptions["printsampleworksheet"]["name"],
			imageSrc: 'icons/Print.png',
			href: 'Laboratory/SampleWorkSheetPrint.do?_method=printSampleWorkSheet',
			target: '_blank'
		};
	</script>
	<title><insta:ltext key="laboratory.transfersamples.list.transfersamples"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="css" file="widgets.css" />
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="js" file="diagnostics/transfer_sample_barcode.js"/>
	<script>
		var cpath = '${cpath}';
		var gServerNow = new Date(<%= (new java.util.Date()).getTime() %>);
	</script>
</head>

<body onload="init(); ajaxForPrintUrls();">
    <c:set var="all">
 		<insta:ltext key="laboratory.transfersamples.list.all"/>
	</c:set>
	<c:set var="transferredsamplestatus">
		<insta:ltext key="laboratory.transfersamples.list.pending"/>,
	 	<insta:ltext key="laboratory.transfersamples.list.transferred"/>
	</c:set>
	<c:set var="mrno">
 		<insta:ltext key="ui.label.mrno"/>
	</c:set>
	<c:set var="sampleCollectionDate">
 		<insta:ltext key="laboratory.transfersamples.list.samplecollectiondate"/>
	</c:set>
	<c:set var="sampleCollectionCenter">
 		<insta:ltext key="laboratory.transfersamples.list.samplecollectioncenter"/>
	</c:set>		
	<c:set var="outSourceName">
 		<insta:ltext key="laboratory.transfersamples.list.outsourcename"/>
	</c:set>
	<c:set var="select">
		<insta:ltext key="selectdb.dummy.value"/>
	</c:set>			
	<jsp:useBean id="currentDate" class="java.util.Date"/>
	<h1><insta:ltext key="laboratory.transfersamples.list.laboratorytransfersamples"/></h1>
	<insta:feedback-panel/>
	<c:set var="actionURL" value="${cpath}/Laboratory/TransferSamplesBarcode.do"/>
	<c:set var="searchMethod" value="searchBySample"/>
	<c:set var="method" value="searchBySample"/>
	<c:set var="sampleAssertion" value="${diagGenericPref.map.sample_assertion == 'Y'}" />

	<form name="transferSamplesForm" action="${actionURL}">
		<input type="hidden" name="_method" value="${ifn:cleanHtmlAttribute(method)}"/>
		<input type="hidden" name="_searchMethod" value="${searchMethod }"/>
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>

		<insta:search-lessoptions form="transferSamplesForm">
			<table class="searchBasicOpts" >
				<tr>
					<td class="sboField">
						<div class="sboFieldLabel"><insta:ltext key="laboratory.transfersamples.list.sampleno"/></div>
						<div class="sboFieldInput">
							<c:set var="sampleNo" value="${param._sample_no}"/>
							<c:if test="${not empty sampleNo && !fn:startsWith(sampleNo, ',')}">
								<c:set var="sampleNo" value=",${param._sample_no}"/>
							</c:if>
							<input type="text" name="_sample_no" id="sample_no" value="${ifn:cleanHtmlAttribute(sampleNo)}"/>
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
					<th><input type="checkbox" ${not empty samplesList ? 'checked' : ''} onclick="return checkOrUncheckAll('transferCheck',this)" id="completeAll"/></th>
					<th>#</th>
					<insta:sortablecolumn name="mr_no" title="${mrno}" />
					<th><insta:ltext key="laboratory.transfersamples.list.visitid"/></th>
					<th><insta:ltext key="ui.label.patient.name"/></th>
					<th><insta:ltext key="laboratory.transfersamples.list.testname"/></th>
					<insta:sortablecolumn name="sample_date" title="${sampleCollectionDate}"/>
					<insta:sortablecolumn name="collection_center" title="${sampleCollectionCenter}"/>
					<insta:sortablecolumn name="outsource_name" title="${outSourceName}"/>
					<th><insta:ltext key="laboratory.transfersamples.list.samplesource"/></th>
					<th><insta:ltext key="laboratory.transfersamples.list.department"/></th>
					<th><insta:ltext key="laboratory.transfersamples.list.sampletype"/></th>
					<th><insta:ltext key="laboratory.transfersamples.list.sampleno"/></th>
					<th><insta:ltext key="laboratory.transfersamples.list.otherdetails"/></th>
				</tr>
				<c:set var="count" value="0"/>
				<c:forEach items="${samplesList}" var="transferSample" varStatus="st">
					<c:set var="outsourceDestPrescribedIdsWithComma" value="${transferSample.map.outsource_dest_prescribed_id}"/>
					<c:set var="splitOutsourceDestPrescribedId" value="${fn:split(outsourceDestPrescribedIdsWithComma, ',')}" />
					<c:forEach var="outsourceDestPrescribedIds" items="${splitOutsourceDestPrescribedId}">
					  <c:set var="outsourceDestPrescribedId" value="${outsourceDestPrescribedIds}"/>
					</c:forEach>
					<c:set var="isOutSourceSelected" value="false" />
					<c:if test="${not empty transferSample.map.outsource_dest_id}">
						<c:set var="isOutSourceSelected" value="true" />
					</c:if>						
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{sampleCollectionId: '${transferSample.map.sample_collection_id}', outsourceDestPrescribedId: '${fn:trim(outsourceDestPrescribedId)}',
							patient_id: '${transferSample.map.pat_id}', actionId: '${actionId}', bulkWorkSheetPrint: 'N'},
							[true,true]);"
						id="toolbarRow${st.index}">
						<input type="hidden" name="sampleDateTime" id="sampleDateTime${count}" value='<fmt:formatDate value="${transferSample.map.sample_date}" pattern="dd-MM-yyyy HH:mm"/>'/>
						<c:set var="count" value="${count+1}"/>
						<input type="hidden" name="sampleCollectionId" id="sampleCollectionId" value="${transferSample.map.sample_collection_id}"/>
						<input type="hidden" name="sampleNo" id="sampleNo" value="${transferSample.map.sample_no}"/>
						<input type="hidden" name="patient_id" id="patient_id" value="${transferSample.map.pat_id}"/>
						<input type="hidden" name="sg_test_id" id="sg_test_id" value="${transferSample.map.test_id}"/>
						<input type="hidden" name="sg_prescribed_id" id="sg_prescribed_id" value="${transferSample.map.prescribed_id}"/>
						<input type="hidden" name="sg_sample_no" id="sg_sample_no" value="${transferSample.map.sg_sample_no}"/>
						<input type="hidden" name="sg_sample_type_id" id="sg_sample_type_id" value="${transferSample.map.sg_sample_type_id}"/>
						<c:set var="blockTransfer" value="${(sampleAssertion eq true && transferSample.map.sample_status ne 'A') 
						|| ((not empty transferSample.map.sample_split_status) && transferSample.map.sample_split_status eq 'P')}" />
						<td style="width:15px;">
							<input type="checkbox" name="transferCheck" id="transferCheck" ${blockTransfer ? '' : 'checked'} ${blockTransfer ? 'disabled' : ''} value="${transferSample.map.sample_collection_id}" />
						</td>
						<td>${((pagedList.pageNumber-1) * pagedList.pageSize) + st.index + 1}</td>
						<td>${transferSample.map.mr_no }</td>
						<td>${transferSample.map.pat_id }</td>
						<td><insta:truncLabel value="${transferSample.map.patient_full_name}" length="25"/></td>
						<td><insta:truncLabel value="${transferSample.map.test_name}" length="30"/></td>
						<td><fmt:formatDate value="${transferSample.map.sample_date}" pattern="dd-MM-yyyy HH:mm"/></td>
						<td><insta:truncLabel value="${transferSample.map.collection_center }" length="20"/></td>
						<td>	
							<c:choose>
								<c:when test="${transferSample.map.sample_transfer_status == 'T' || isOutSourceSelected || empty outSourceList}">
									<insta:truncLabel value="${transferSample.map.outsource_name}" length="15"/>
									<input type="hidden" name="outsource_id" id="outsource_id" value="${transferSample.map.outsource_dest_id}" />						
								</c:when>
								<c:otherwise>
									<select name="outsource_id" id="outsource_id" class="dropdown noToolbar" style="width: 100px;">
							   			<option value="">${select}</option>
										<c:forEach items="${outSourceList}" var="outSource">
											<option value="${outSource.OUTSOURCE_DEST_ID}">
												${outSource.OUTSOURCE_NAME}
											</option>
										</c:forEach>
									</select>
								</c:otherwise>
							</c:choose>											
							<input type="hidden" name="outsource_dest_id" id="outsource_dest_id" value=""/>								
						</td>
						<td>${transferSample.map.source_center_name}</td>
						<td><insta:truncLabel value="${transferSample.map.ddept_name }" length="25"/></td>
						<td><insta:truncLabel value="${transferSample.map.sample_type}" length="30"/></td>
						<td>${transferSample.map.sample_no }</td>
						<td><insta:truncLabel value="${transferSample.map.transfer_other_details}" length="30"/></td>
					</tr>
				</c:forEach>
			</table>
		</div>
		<table class="screenActions" style="float:left" >
			<tr>
				<td>
					<button type="button" id="markTransferred" accessKey="S" onclick="return markTransferredAndPrint('');"><b><u><insta:ltext key="laboratory.transfersamples.list.m"/></u></b><insta:ltext key="laboratory.transfersamples.list.arktransferred"/></button>
				</td>
				<td>&nbsp;&nbsp;&nbsp;&nbsp;</td>
				<td>
					<button type="button" id="Print" accessKey="P" onclick="return markTransferredAndPrint('P');"><insta:ltext key="laboratory.transfersamples.list.MarkTransferred"/>
					<u><b><insta:ltext key="laboratory.transfersamples.list.p"/></b></u><insta:ltext key="laboratory.transfersamples.list.rint"/></button>
				</td>
				<td>&nbsp;&nbsp;&nbsp;&nbsp;</td>
				<td>
					<button type="button" id="PrintAll" accessKey="P" onclick="return printAll();"><u><b><insta:ltext key="laboratory.transfersamples.list.p"/></b></u><insta:ltext key="laboratory.transfersamples.list.rintall"/></button>
				</td>
				<td>&nbsp;&nbsp;&nbsp;&nbsp;</td>
				<td valign="middle">
					<label><insta:ltext key="laboratory.transfersamples.list.updateotherdetails"/>:</label>
				</td>
				<td>&nbsp;</td>
				<td>
					<textarea rows="2" cols="20" id="transferOtherDetails" name="transferOtherDetails"></textarea>
				</td>
			</tr>
		</table>
		<div class="screenActions" style="float:right">
			<label><insta:ltext key="laboratory.transfersamples.list.transfertime"/>:</label>
			<fmt:formatDate var="dateVal" value="${currentDate}" pattern="dd-MM-yyyy"/>
			<fmt:formatDate var="timeVal" value="${currentDate}" pattern="HH:mm"/>
			<c:choose>
				<c:when test="${actionRightsMap.allow_backdate == 'A' || roleId == 1 || roleId ==2}">
					<insta:datewidget name="transferDate" value="${dateVal}" valid="past" btnPos="left" />
					<input type="text" name="transferTime" class="timefield" value="${timeVal}" maxlength="5"/>
					<span class="star">*</span>
				</c:when>
				<c:otherwise>
					<b>${dateVal} ${timeVal}</b>
					<input type="hidden" name="transferDate" value="${dateVal}"/>
					<input type="hidden" name="transferTime" value="${timeVal}"/>
				</c:otherwise>
			</c:choose>
		</div>
	</form>
</body>
</html>