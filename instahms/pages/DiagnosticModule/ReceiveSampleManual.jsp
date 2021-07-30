<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
 <%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />

<html>
<c:set var="receiveSamplesList" value="${pagedList.dtoList}" />
<c:set var="hasResults" value="${not empty receiveSamplesList}"/>

<head>
	<title><insta:ltext key="laboratory.receivesamples.list.receivesamples.search"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="css" file="widgets.css" />
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="js" file="diagnostics/receive_sample_manual.js"/>
	<insta:link type="js" file="diagnostics/receive_sample_barcode.js"/>
	<insta:js-bundle prefix="laboratory.receivesample"/>
	<insta:js-bundle prefix="laboratory.radiology.receivesample"/>
	<script src="<%=request.getContextPath()%>/gettests.do?${test_timestamp}&${ifn:cleanURL(sesHospitalId)}&module=${ifn:cleanURL(module)}"></script>
	<script>
		var toolbarOptions = getToolbarBundle("js.laboratory.radiology.receivesample.toolbar");
		var cpath = '${cpath}';
		var collectionCenters = ${collectionCenters};
		var allTestNames = deptWiseTestsjson;
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
	</script>
</head>

<body onload="initAll(); ajaxForPrintUrls();">
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
	<c:set var="sponsorType">
	 <insta:ltext key="laboratory.receivesamples.list.sponsor"/>,
	 <insta:ltext key="laboratory.receivesamples.list.retail"/>
	</c:set>
	<c:set var="visittype">
	 	<insta:ltext key="laboratory.receivesamples.list.ip"/>,
	 	<insta:ltext key="laboratory.receivesamples.list.op"/>,
		<insta:ltext key="laboratory.receivesamples.list.incomingtest"/>
	</c:set> 
	<h1><insta:ltext key="laboratory.receivesamples.list.laboratoryreceivesamples.search"/></h1>
	<insta:feedback-panel/>
	<c:set var="actionURL" value="${cpath}/Laboratory/ReceiveSamplesManual.do"/>
	<c:set var="searchMethod" value="searchlist"/>
	<c:set var="method" value="searchlist"/>

	<form name="receiveSamplesForm" action="${actionURL}">
	<input type="hidden" name="_method" value="${ifn:cleanHtmlAttribute(method)}"/>
	<input type="hidden" name="_searchMethod" value="${searchMethod }"/>
	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>

		<insta:search form="receiveSamplesForm" optionsId="optionalFilter" closed="${hasResults}" validateFunction="doSearch()">
			<div class="searchBasicOpts" >
				<div class="sboField">
					<div class="sboFieldLabel"><insta:ltext key="laboratory.receivesamples.list.mrno.patientname"/></div>
					<div class="sboFieldInput">
						<div id="mrnoAutoComplete">
							<input type="text" name="mr_no" id="mrno" value="${ifn:cleanHtmlAttribute(param.mr_no)}" />
							<div id="mrnoContainer" style="width: 300px"></div>
						</div>
					</div>
				</div>
				<div class="sboField" style="width: 150px">
					<div class="sboFieldLabel"><insta:ltext key="laboratory.receivesamples.list.samplecollectioncenter"/>:</div>
					<div class="sboFieldInput">
						<div id="outhouse_wrapper">
							<input type="text" name="collection_center" id="collection_center" value="${ifn:cleanHtmlAttribute(param.collection_center)}" />
							<div id="collection_center_container" style="width: 300px"></div>
						</div>
					</div>
				</div>
				<div class="sboField">
					<div class="sboFieldLabel"><insta:ltext key="laboratory.receivesamples.list.sampleno"/>:</div>
					<input type="text" name="_sample_no" id="sample_no" value="${ifn:cleanHtmlAttribute(param._sample_no)}" />
				</div>
				<div class="sboField">
					<div class="sboFieldLabel"><insta:ltext key="laboratory.receivesamples.list.department"/>:</div>
					<div class="sboFieldInput">
						<insta:selectdb name="ddept_id" table="diagnostics_departments" valuecol="ddept_id"
							displaycol="ddept_name" value="${empty param.ddept_id ? userDept : param.ddept_id}"
						    dummyvalue="${all}" dummyvalueId="" filtered="true" filtercol="category,status"
						    filtervalue="DEP_LAB,A"/>
					</div>
				</div>
				<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
					<table class="searchFormTable">
						<tr>
							<td>
								<div class="sfLabel"><insta:ltext key="laboratory.receivesamples.list.sampletype"/></div>
								<div class="sfField">
									<insta:selectdb name="sample_type_id" table="sample_type" valuecol="sample_type_id"
										displaycol="sample_type" size="8" style="width: 11em" multiple="true"
										values="${paramValues.sample_type_id}" class="noClass" orderby="sample_type"/>
										<input type="hidden" name="sample_type_id@cast" value="y"/>
								</div>
							</td>
							<td>
								<div class="sfLabel"><insta:ltext key="laboratory.receivesamples.list.samplecollectiondate"/></div>
								<div class="sfField">
									<div class="sfFieldSub"><insta:ltext key="laboratory.receivesamples.list.from"/>:</div>
									<insta:datewidget name="sample_date" id="sample_date0" valid="past"	value="${paramValues.sample_date[0]}" />
									<input type="hidden" name="sample_date@type" value="date"/>
									<input type="hidden" name="sample_date@op" value="ge,le"/>
									<input type="hidden" name="sample_date@cast" value="y"/>
								</div>
								<div class="sfField">
									<div class="sfFieldSub"><insta:ltext key="laboratory.receivesamples.list.to"/>:</div>
									<insta:datewidget name="sample_date" id="sample_date1" valid="past"	value="${paramValues.sample_date[1]}" />
								</div>
								<div class="sfLabel"><insta:ltext key="laboratory.receivesamples.list.samplereceivedtime"/></div>
								<div class="sfField">
									<div class="sfFieldSub"><insta:ltext key="laboratory.receivesamples.list.from"/>:</div>
									<insta:datewidget name="_receipt_date" id="receipt_date0" valid="past" value="${paramValues._receipt_date[0]}" />
									<input type="text" name="_receipt_time" id="receipt_time0" class="timefield" value="${paramValues._receipt_time[0]}" maxlength="5"/>
									<input type="hidden" name="receipt_time@type" value="timestamp"/>
									<input type="hidden" name="receipt_time@op" value="ge,le"/>
								</div>
								<div class="sfField">
									<div class="sfFieldSub"><insta:ltext key="laboratory.receivesamples.list.to"/>:</div>
									<insta:datewidget name="_receipt_date" id="receipt_date1" valid="past"	value="${paramValues._receipt_date[1]}" />
									<input type="text" name="_receipt_time" id="receipt_time1" class="timefield" value="${paramValues._receipt_time[1]}" maxlength="5"/>
								</div>
								<div class="sfLabel"><insta:ltext key="laboratory.receivesamples.list.sampletransferredtime"/></div>
								<div class="sfField">
									<div class="sfFieldSub"><insta:ltext key="laboratory.receivesamples.list.from"/>:</div>
									<insta:datewidget name="_transfer_date" id="transfer_date0" valid="past" value="${paramValues._transfer_date[0]}" />
									<input type="text" name="_transfer_time" id="transfer_time0" class="timefield" value="${paramValues._transfer_time[0]}" maxlength="5"/>
									<input type="hidden" name="transfer_time@type" value="timestamp"/>
									<input type="hidden" name="transfer_time@op" value="ge,le"/>
								</div>
								<div class="sfField">
									<div class="sfFieldSub"><insta:ltext key="laboratory.receivesamples.list.to"/>:</div>
									<insta:datewidget name="_transfer_date" id="transfer_date1" valid="past" value="${paramValues._transfer_date[1]}" />
									<input type="text" name="_transfer_time" id="transfer_time1" class="timefield" value="${paramValues._transfer_time[1]}" maxlength="5"/>
								</div>
							</td>
							<td>
								<div class="sfLabel"><insta:ltext key="laboratory.receivesamples.list.testname"/></div>
								<div class="sfField" style="height: 20px">
									<div id="test_wrapper">
										<input name="test_name" type="text" id="test_name" value="${ifn:cleanHtmlAttribute(param.test_name)}"/>
										<div id="test_container" style="width: 300px"></div>
									</div>
								</div>
								<div class="sfLabel">${tBatchId}</div>
								<div class="sfField" style="margin-bottom: 20px">
									<input type="text" name="transfer_batch_id" id="transfer_batch_id" value="${ifn:cleanHtmlAttribute(param.transfer_batch_id)}"/>
								</div>
							</td>
							<td>
								<div class="sfLabel"><insta:ltext key="laboratory.receivesamples.list.samplereceivestatus"/></div>
								<div class="sfField">
									<insta:checkgroup name="sample_receive_status" selValues="${paramValues.sample_receive_status}" opvalues="P,R" optexts="${receivesamplestatus}"/>
								</div>
								<div class="sfLabel"><insta:ltext key="laboratory.receivesamples.list.sponsortype"/></div>
								<div class="sfField">
									<insta:checkgroup name="patient_sponsor_type" selValues="${paramValues.patient_sponsor_type}"
									opvalues="S,R" optexts="${sponsorType}"/>
								</div>
								<div class="sfLabel"><insta:ltext key="laboratory.receivesamples.list.patienttype"/>:</div>
								<div class="sfField">
									<insta:checkgroup name="visit_type" selValues="${paramValues.visit_type}"
									opvalues="i,o,t" optexts="${visittype}"/>
								</div>																
							</td>
						</tr>
					</table>
				</div>
			</div>
		</insta:search>
	</form>
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
	<form name="resultsForm" method="POST">
	<input type="hidden" name="isPrint" value"" />
		<div class="resultList">
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
				<c:forEach items="${receiveSamplesList}" var="receiveSample" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{sampleCollectionId: '${receiveSample.map.sample_collection_id}',patient_id: '${receiveSample.map.pat_id}',actionId: '${actionId}',bulkWorkSheetPrint: 'N'},
							[${receiveSample.map.sample_transfer_status != 'P'},true]);"
						id="toolbarRow${st.index}">
						<input type="hidden" name="transferDateTime" id="transferDateTime${count}" value='<fmt:formatDate value="${receiveSample.map.transfer_time}" pattern="dd-MM-yyyy HH:mm"/>'/>
						<c:set var="count" value="${count+1}"/>
						<td style="width:15px;">
							<input type="checkbox" name="receiveCheck" id="receiveCheck" value="${receiveSample.map.sample_collection_id}" ${(receiveSample.map.sample_receive_status == 'R' || receiveSample.map.sample_transfer_status == 'P') ? 'disabled' : ''} />
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