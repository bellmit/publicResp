<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
 <%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />

<html>
<c:set var="transferSamplesList" value="${pagedList.dtoList}" />
<c:set var="hasResults" value="${not empty transferSamplesList}"/>

<head>
	<insta:js-bundle prefix="laboratory.transfersample"/>
	<insta:js-bundle prefix="laboratory.radiology.transfersample"/>

	<title><insta:ltext key="laboratory.transfersamples.list.transfersamples.search"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="css" file="widgets.css" />
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="js" file="diagnostics/transfer_sample_manual.js"/>
	<insta:link type="js" file="diagnostics/transfer_sample_barcode.js"/>
	<script src="<%=request.getContextPath()%>/gettests.do?${test_timestamp}&${ifn:cleanJavaScript(sesHospitalId)}&module=${ifn:cleanJavaScript(module)}"></script>
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

		var cpath = '${cpath}';
		var outSources = ${outSources};
		var allTestNames = deptWiseTestsjson;
		var collectionCenters = ${collectionCenters};		
	</script>
</head>

<body onload="initAll(); ajaxForPrintUrls();">
    <c:set var="all">
 		<insta:ltext key="laboratory.transfersamples.list.all"/>
	</c:set>
	<c:set var="transferredsamplestatus">
		<insta:ltext key="laboratory.transfersamples.list.pending"/>,
	 	<insta:ltext key="laboratory.transfersamples.list.transferred"/>
	</c:set>
	<c:set var="visittype">
	 	<insta:ltext key="laboratory.transfersamples.list.ip"/>,
	 	<insta:ltext key="laboratory.transfersamples.list.op"/>,
		<insta:ltext key="laboratory.transfersamples.list.incomingtest"/>
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
	<c:set var="tBatchId">
		<insta:ltext key="laboratory.transfersamples.list.batchid"/>
	</c:set>
	<c:set var="select">
		<insta:ltext key="selectdb.dummy.value"/>
	</c:set>		
	<jsp:useBean id="currentDate" class="java.util.Date"/>
	<h1><insta:ltext key="laboratory.transfersamples.list.laboratorytransfersamples.search"/></h1>
	<insta:feedback-panel/>
	<c:set var="actionURL" value="${cpath}/Laboratory/TransferSamplesManual.do"/>
	<c:set var="searchMethod" value="list"/>
	<c:set var="method" value="list"/>
	<c:set var="sampleAssertion" value="${diagGenericPref.map.sample_assertion == 'Y'}" />

	<form name="transferSamplesForm" action="${actionURL}">
		<input type="hidden" name="_method" value="${ifn:cleanHtmlAttribute(method)}"/>
		<input type="hidden" name="_searchMethod" value="${searchMethod }"/>
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>

		<insta:search form="transferSamplesForm" optionsId="optionalFilter" closed="${hasResults}" validateFunction="doSearch()">
			<div class="searchBasicOpts" >
				<div class="sboField">
					<div class="sboFieldLabel"><insta:ltext key="laboratory.transfersamples.list.mrno.patientname"/></div>
					<div class="sboFieldInput">
						<div id="mrnoAutoComplete">
							<input type="text" name="mr_no" id="mrno" value="${ifn:cleanHtmlAttribute(param.mr_no)}" />
							<div id="mrnoContainer" style="width: 300px"></div>
						</div>
					</div>
				</div>
				<div class="sboField">
					<div class="sboFieldLabel"><insta:ltext key="laboratory.transfersamples.list.outsourcename"/>:</div>
					<div class="sboFieldInput">
						<div id="outhouse_wrapper">
							<input type="text" name="outsource_name" id="outsource_name" value="${ifn:cleanHtmlAttribute(param.outsource_name)}"/>
							<div id="outsource_container" style="width: 300px"></div>
						</div>
					</div>
				</div>
				<div class="sboField">
					<div class="sboFieldLabel"><insta:ltext key="laboratory.transfersamples.list.sampleno"/>:</div>
					<input type="text" name="_sample_no" id="sample_no" value="${ifn:cleanHtmlAttribute(param._sample_no)}"/>
				</div>
				<div class="sboField">
					<div class="sboFieldLabel"><insta:ltext key="laboratory.transfersamples.list.department"/>:</div>
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
								<div class="sfLabel"><insta:ltext key="laboratory.transfersamples.list.sampletype"/></div>
								<div class="sfField">
									<insta:selectdb name="sample_type_id" table="sample_type" valuecol="sample_type_id"
										displaycol="sample_type" size="8" style="width: 11em" multiple="true"
										values="${paramValues.sample_type_id}" class="noClass" orderby="sample_type"/>
										<input type="hidden" name="sample_type_id@cast" value="y"/>
								</div>
							</td>
							<td>
								<div class="sfLabel"><insta:ltext key="laboratory.transfersamples.list.samplecollectiondate"/></div>
								<div class="sfField">
									<div class="sfFieldSub"><insta:ltext key="laboratory.transfersamples.list.from"/>:</div>
									<insta:datewidget name="sample_date" id="sample_date0" valid="past"	value="${paramValues.sample_date[0]}" />
									<input type="hidden" name="sample_date@type" value="date"/>
									<input type="hidden" name="sample_date@op" value="ge,le"/>
									<input type="hidden" name="sample_date@cast" value="y"/>
								</div>
								<div class="sfField">
									<div class="sfFieldSub"><insta:ltext key="laboratory.transfersamples.list.to"/>:</div>
									<insta:datewidget name="sample_date" id="sample_date1" valid="past"	value="${paramValues.sample_date[1]}" />
								</div>
								<div class="sfLabel"><insta:ltext key="laboratory.transfersamples.list.sampletransferredtime"/></div>
								<div class="sfField">
									<div class="sfFieldSub"><insta:ltext key="laboratory.transfersamples.list.from"/>:</div>
									<insta:datewidget name="_transfer_date" id="transfer_date0" valid="past" value="${paramValues._transfer_date[0]}" />
									<input type="text" name="_transfer_time" id="transfer_time0" class="timefield" value="${paramValues._transfer_time[0]}" maxlength="5"/>
									<input type="hidden" name="transfer_time@type" value="timestamp"/>
									<input type="hidden" name="transfer_time@op" value="ge,le"/>
								</div>
								<div class="sfField">
									<div class="sfFieldSub"><insta:ltext key="laboratory.transfersamples.list.to"/>:</div>
									<insta:datewidget name="_transfer_date" id="transfer_date1" valid="past" value="${paramValues._transfer_date[1]}" />
									<input type="text" name="_transfer_time" id="transfer_time1" class="timefield" value="${paramValues._transfer_time[1]}" maxlength="5"/>
								</div>
							</td>
							<td>
								<div class="sfLabel"><insta:ltext key="laboratory.transfersamples.list.testname"/>:</div>
								<div class="sfField" style="height: 20px">
									<div id="test_wrapper">
										<input name="test_name" type="text" id="test_name" value="${ifn:cleanHtmlAttribute(param.test_name)}"/>
										<div id="test_container" style="width: 300px"></div>
									</div>
								</div>
								<div class="sfLabel"><insta:ltext key="laboratory.transfersamples.list.samplecollectioncenter"/>:</div>
								<div class="sfField" style="height: 20px">
									<div id="collectioncenter_wrapper">
										<input name="collection_center" type="text" id="collection_center" value="${ifn:cleanHtmlAttribute(param.collection_center)}"/>
										<div id="collection_center_container" style="width: 300px"></div>
									</div>
								</div>
								<div class="sfLabel">${tBatchId}:</div>
								<div class="sfField" style="height: 20px">
									<input name="transfer_batch_id" type="text" id="transfer_batch_id" value="${ifn:cleanHtmlAttribute(param.transfer_batch_id)}"/>
								</div>
							</td>
							<td>
								<div class="sfLabel"><insta:ltext key="laboratory.transfersamples.list.sampletransferstatus"/></div>
								<div class="sfField">
									<insta:checkgroup name="sample_transfer_status" selValues="${paramValues.sample_transfer_status}" opvalues="P,T" optexts="${transferredsamplestatus}"/>
								</div>
								<div class="sfLabel"><insta:ltext key="laboratory.transfersamples.list.patienttype"/>:</div>
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
					<insta:sortablecolumn name="transfer_batch_id" title="${tBatchId}"/>
					<th><insta:ltext key="laboratory.transfersamples.list.sampleno"/></th>
					<th><insta:ltext key="laboratory.transfersamples.list.otherdetails"/></th>
				</tr>
				<c:set var="count" value="0"/>
				<c:forEach items="${transferSamplesList}" var="transferSample" varStatus="st">
					<c:set var="outsourceDestPrescribedIdsWithComma" value="${transferSample.map.outsource_dest_prescribed_id}"/>
					<c:set var="splitOutsourceDestPrescribedId" value="${fn:split(outsourceDestPrescribedIdsWithComma, ',')}" />
					<c:forEach var="outsourceDestPrescribedIds" items="${splitOutsourceDestPrescribedId}">
					  <c:set var="outsourceDestPrescribedId" value="${outsourceDestPrescribedIds}"/>
					</c:forEach>
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{sampleCollectionId: '${transferSample.map.sample_collection_id}', outsourceDestPrescribedId: '${fn:trim(outsourceDestPrescribedId)}',
							patient_id: '${transferSample.map.pat_id}', actionId: '${actionId}', bulkWorkSheetPrint: 'N'},
							[true,true]);"
						id="toolbarRow${st.index}">
						<input type="hidden" name="sampleDateTime" id="sampleDateTime${count}" value='<fmt:formatDate value="${transferSample.map.sample_date}" pattern="dd-MM-yyyy HH:mm"/>'/>
						<input type="hidden" name="sampleCollectionId" id="sampleCollectionId" value="${transferSample.map.sample_collection_id}"/>
						<input type="hidden" name="sampleNo" id="sampleNo" value="${transferSample.map.sample_no}"/>
						<input type="hidden" name="patient_id" id="patient_id" value="${transferSample.map.pat_id}"/>
						<input type="hidden" name="sg_test_id" id="sg_test_id" value="${transferSample.map.test_id}"/>
						<input type="hidden" name="sg_prescribed_id" id="sg_prescribed_id" value="${transferSample.map.prescribed_id}"/>
						<input type="hidden" name="sg_sample_no" id="sg_sample_no" value="${transferSample.map.sg_sample_no}"/>
						<input type="hidden" name="sg_sample_type_id" id="sg_sample_type_id" value="${transferSample.map.sg_sample_type_id}"/>
						
						<c:set var="count" value="${count+1}"/>						
						<c:set var="isOutSourceSelected" value="false" />
						<c:if test="${not empty transferSample.map.outsource_dest_id}">
							<c:set var="isOutSourceSelected" value="true" />
						</c:if>
						<c:set var="checkBoxDisabledValue" value=""/>
						<c:if test="${transferSample.map.sample_transfer_status == 'T' || 
						(sampleAssertion eq true && transferSample.map.sample_status ne 'A') || 
						((not empty transferSample.map.sample_split_status) && transferSample.map.sample_split_status eq 'P')}">
							<c:set var="checkBoxDisabledValue" value="disabled"/>
						</c:if>
						<td style="width:15px;">
							<input type="checkbox" name="transferCheck" id="transferCheck" value="${transferSample.map.sample_collection_id}" 
							${checkBoxDisabledValue} />							
						</td>
						<td>${st.index + 1}</td>
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
						<td>${transferSample.map.transfer_batch_id}</td>
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