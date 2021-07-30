<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<c:set var="cpath" value="${param.cpath}" />
<c:set var="hasResults" value="${param.hasResults}" />
<c:set var="actionURL" value="${cpath}/Laboratory/ReceiveAndSplitSamples.do" />
<c:set var="searchMethod" value="searchlist" />
<c:set var="method" value="searchlist" />
<c:set var="all">
	<insta:ltext key="laboratory.receivesamples.list.all" />
</c:set>
<c:set var="receivesamplestatus">
	<insta:ltext key="laboratory.receivesamples.list.pending" />,
 	<insta:ltext key="laboratory.receivesamples.list.received" />
</c:set>
<c:set var="splitsamplestatus">
	<insta:ltext key="laboratory.receivesamples.list.notneeded" />,
	<insta:ltext key="laboratory.receivesamples.list.splitrequired" />,
 	<insta:ltext key="laboratory.receivesamples.list.splitdone" />
</c:set>
<c:set var="mrno">
	<insta:ltext key="ui.label.mrno" />
</c:set>
<c:set var="sampleCollectionDate">
	<insta:ltext key="laboratory.receivesamples.list.samplecollectiondate" />
</c:set>
<c:set var="sampleCollectionCenter">
	<insta:ltext key="laboratory.receivesamples.list.samplecollectioncenter" />
</c:set>
<c:set var="tBatchId">
	<insta:ltext key="laboratory.receivesamples.list.batchid" />
</c:set>
<c:set var="sponsorType">
	<insta:ltext key="laboratory.receivesamples.list.sponsor" />,
 	<insta:ltext key="laboratory.receivesamples.list.retail" />
</c:set>
<c:set var="visittype">
	<insta:ltext key="laboratory.receivesamples.list.ip" />,
 	<insta:ltext key="laboratory.receivesamples.list.op" />,
	<insta:ltext key="laboratory.receivesamples.list.incomingtest" />
</c:set>

<div class="receive-and-search-panel">
	<form name="receiveSampleManualBarcode" id="receive-sample" method="POST">
		<div class="searchTitle">
			<div class="fltL">
				<div class="searchTitleContents" style="font-weight: bold;">
					<insta:ltext key="laboratory.receivesamples.list.receivesamplemanualorbarcode" />
				</div>
			</div>
		</div>
		<div class="sboField" style="margin-top: 3px;">
			<div class="sboFieldLabel">
				<insta:ltext key="laboratory.receivesamples.list.receive" /> <insta:ltext key="laboratory.receivesamples.list.sampleno" />:
			</div>
			<input type="text" name="_receive_sample_no"
				id="receive_form_sample_no"
				value="${ifn:cleanHtmlAttribute(param._receive_sample_no)}"
				autofocus="autofocus" />
		</div>
		<input type="button" id="receive" value='Receive' disabled/>
	</form>
	<form name="receiveSamplesForm" action="${actionURL}">
		<input type="hidden" name="_method" value="${ifn:cleanHtmlAttribute(method)}" /> 
		<input type="hidden" name="_searchMethod" value="${searchMethod }" /> 
		<div class="search-less-option">
			<div class="searchTitle">
				<div class="fltL">
					<div class="searchTitleContents" style="font-weight: bold; width: 75px;">
						<insta:ltext key="search.search" />
					</div>
					<div class="searchTitleContents searchTitleSeparator" style="width: 120px;">
						<a id="aMore" onclick="showMoreCustom('optionalFilter');">
							<c:choose>
								<c:when test="${hasResults}">
									<insta:ltext key="search.more.options" />
								</c:when>
								<c:otherwise>
									<insta:ltext key="search.less.options" />
								</c:otherwise>
							</c:choose>
						</a>
					</div>
				</div>

				<div class="searchList">
					<select size="1" id="_mysearch" name="_mysearch"
						onchange="onSearchChange(this.value,
						document.forms.receiveSamplesForm)">
						<option value="nosearch">
							<insta:ltext key="search.my.searches" />
						</option>
						<c:forEach var="search" items="${mysearches}">
							<option value="${search.map.search_id}">
								${ifn:cleanHtml(search.map.search_name)}
							</option>
						</c:forEach>
					</select>
				</div>
			</div>
			<div class="searchBasicOpts">
				<div class="sboField">
					<div class="sboFieldLabel">
						<insta:ltext key="search.search" /> <insta:ltext key="laboratory.receivesamples.list.sampleno" />:
					</div>
					<input type="text" name="_sample_no" id="sample_no" value="${ifn:cleanHtmlAttribute(param._sample_no)}" />
				</div>
				<div class="sboField">
					<div class="sboFieldLabel">
						<insta:ltext key="laboratory.receivesamples.list.mrno.patientname" />
					</div>
					<div class="sboFieldInput">
						<div id="mrnoAutoComplete">
							<input type="text" name="mr_no" id="mrno" value="${ifn:cleanHtmlAttribute(param.mr_no)}" />
							<div id="mrnoContainer" style="width: 300px"></div>
						</div>
					</div>
				</div>
			</div>
			<div
				style="background-color: #eaf2f8; border: 1px #e6e6e6 solid; padding: 10px; width: 115px; float: right">
				<table style="height: 100%">
					<tr>
						<td valign="top" height="100%">
							<a id="_save_search" style="cursor:pointer;" onclick="showSaveInputs();">
								<insta:ltext key="search.save.search"/>
							</a>
							<div id="_save_inputs" style="display: none">
								<input name="_search_name" id="_search_name" type="text" value=""
									style="width:100px; margin-bottom:5px;"/>
								<br/>
								<input type="hidden" name="_actionId" value="${actionId}"/>
								<input type="button" value='<insta:ltext key="search.save"/>'
									onclick="return validateSearchTagForm(event) && saveSearch(document.forms.receiveSamplesForm);"/>
								
								<a href="#" onclick="document.getElementById('_search_name').value=''; return false;">
									<insta:ltext key="search.clear"/>
								</a>
							</div>
						</td>
					</tr>
					<tr>
						<td valign="middle">
                            <input type="submit" class="button" id="Search" value='<insta:ltext key="search.search"/>' /> 
                            <a href="#" onclick="${empty clearFunction ? 'clearForm' : clearFunction}(document.forms.receiveSamplesForm);">
                                <insta:ltext key="search.clear" />
                            </a>
                        </td>
					</tr>
				</table>
			</div>
		</div>

		<insta:searchCustomized form="receiveSamplesForm" optionsId="optionalFilter" closed="${hasResults}"
			validateFunction="doSearch()">
			<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}">
				<table class="searchFormTable">
					<tr>
						<td>
							<div class="sfLabel">
								<insta:ltext key="laboratory.receivesamples.list.sampletype" />
							</div>
							<div class="sfField">
								<insta:selectdb name="sample_type_id" table="sample_type"
									valuecol="sample_type_id" displaycol="sample_type" size="8"
									style="width: 11em" multiple="true"
									values="${paramValues.sample_type_id}" class="noClass"
									orderby="sample_type" />
								<input type="hidden" name="sample_type_id@cast" value="y" />
							</div>
						</td>
						<td>
							<div class="sfLabel">
								<insta:ltext key="laboratory.receivesamples.list.samplecollectiondate" />
							</div>
							<div class="sfField">
								<div class="sfFieldSub">
									<insta:ltext key="laboratory.receivesamples.list.from" />:
								</div>
								<insta:datewidget name="sample_date" id="sample_date0"
									valid="past" value="${paramValues.sample_date[0]}" />
								<input type="hidden" name="sample_date@type" value="date" />
                                <input type="hidden" name="sample_date@op" value="ge,le" />
                                <input type="hidden" name="sample_date@cast" value="y" />
							</div>
							<div class="sfField">
								<div class="sfFieldSub">
									<insta:ltext key="laboratory.receivesamples.list.to" />:
								</div>
								<insta:datewidget name="sample_date" id="sample_date1"
									valid="past" value="${paramValues.sample_date[1]}" />
							</div>
							<div class="sfLabel">
								<insta:ltext key="laboratory.receivesamples.list.samplereceivedtime" />
							</div>
							<div class="sfField">
								<div class="sfFieldSub">
									<insta:ltext key="laboratory.receivesamples.list.from" />:
								</div>
								<insta:datewidget name="_receipt_date" id="receipt_date0"
									valid="past" value="${paramValues._receipt_date[0]}" />
								<input type="text" name="_receipt_time" id="receipt_time0"
									class="timefield" value="${paramValues._receipt_time[0]}"
									maxlength="5" />
                                <input type="hidden" name="receipt_time@type" value="timestamp" />
                                <input type="hidden" name="receipt_time@op" value="ge,le" />
							</div>
							<div class="sfField">
								<div class="sfFieldSub">
									<insta:ltext key="laboratory.receivesamples.list.to" />:
								</div>
								<insta:datewidget name="_receipt_date" id="receipt_date1"
									valid="past" value="${paramValues._receipt_date[1]}" />
								<input type="text" name="_receipt_time" id="receipt_time1"
									class="timefield" value="${paramValues._receipt_time[1]}"
									maxlength="5" />
							</div>
							<div class="sfLabel">
								<insta:ltext key="laboratory.receivesamples.list.sampletransferredtime" />
							</div>
							<div class="sfField">
								<div class="sfFieldSub">
									<insta:ltext key="laboratory.receivesamples.list.from" />:
								</div>
								<insta:datewidget name="_transfer_date" id="transfer_date0"
									valid="past" value="${paramValues._transfer_date[0]}" />
								<input type="text" name="_transfer_time" id="transfer_time0"
									class="timefield" value="${paramValues._transfer_time[0]}"
									maxlength="5" />
                                <input type="hidden" name="transfer_time@type" value="timestamp" />
                                <input type="hidden" name="transfer_time@op" value="ge,le" />
							</div>
							<div class="sfField">
								<div class="sfFieldSub">
									<insta:ltext key="laboratory.receivesamples.list.to" />:
								</div>
								<insta:datewidget name="_transfer_date" id="transfer_date1"
									valid="past" value="${paramValues._transfer_date[1]}" />
								<input type="text" name="_transfer_time" id="transfer_time1"
									class="timefield" value="${paramValues._transfer_time[1]}"
									maxlength="5" />
							</div>
						</td>
						<td>
							<div class="sfLabel">
								<insta:ltext key="laboratory.receivesamples.list.testname" />
							</div>
							<div class="sfField" style="height: 20px">
								<div id="test_wrapper">
									<input name="test_name" type="text" id="test_name"
										value="${ifn:cleanHtmlAttribute(param.test_name)}" />
									<div id="test_container" style="width: 300px"></div>
								</div>
							</div>
							<div class="sfLabel">${tBatchId}</div>
							<div class="sfField">
								<input type="text" name="transfer_batch_id" id="transfer_batch_id"
									value="${ifn:cleanHtmlAttribute(param.transfer_batch_id)}" />
							</div>
							<div class="sfLabel">
								<insta:ltext key="laboratory.receivesamples.list.samplecollectioncenter" />:
							</div>
							<div class="sfField" style="height: 20px;">
								<div id="outhouse_wrapper">
									<input type="text" name="collection_center" id="collection_center"
										value="${ifn:cleanHtmlAttribute(param.collection_center)}" />
									<div id="collection_center_container" style="width: 300px"></div>
								</div>
							</div>
							<div class="sfLabel">
								<insta:ltext key="laboratory.receivesamples.list.department" />:
							</div>
							<div class="sfField">
								<div class="sboFieldInput">
									<insta:selectdb name="ddept_id"
										table="diagnostics_departments" valuecol="ddept_id"
										displaycol="ddept_name"
										value="${empty param.ddept_id ? userDept : param.ddept_id}"
										dummyvalue="${all}" dummyvalueId="" filtered="true"
										filtercol="category,status" filtervalue="DEP_LAB,A" />
								</div>
							</div>
						</td>
						<td>
							<div class="sfLabel">
								<insta:ltext key="laboratory.receivesamples.list.samplereceivestatus" />
							</div>
							<div class="sfField">
								<insta:checkgroup name="sample_receive_status"
									selValues="${paramValues.sample_receive_status}"
									opvalues="P,R" optexts="${receivesamplestatus}" />
							</div>
							<div class="sfLabel">
								<insta:ltext key="laboratory.receivesamples.list.samplesplitstatus" />:
							</div>
							<div class="sfField">
								<insta:checkgroup name="sample_split_status"
									selValues="${paramValues.sample_split_status}" opvalues="N,P,D"
									optexts="${splitsamplestatus}" />
							</div>
							<div class="sfLabel">
								<insta:ltext key="laboratory.receivesamples.list.sponsortype" />
							</div>
							<div class="sfField">
								<insta:checkgroup name="patient_sponsor_type"
									selValues="${paramValues.patient_sponsor_type}" opvalues="S,R"
									optexts="${sponsorType}" />
							</div>
							<div class="sfLabel">
								<insta:ltext key="laboratory.receivesamples.list.patienttype" />:
							</div>
							<div class="sfField">
								<insta:checkgroup name="visit_type"
									selValues="${paramValues.visit_type}" opvalues="i,o,t"
									optexts="${visittype}" />
							</div>
						</td>
					</tr>
				</table>
			</div>
		</insta:searchCustomized>
		<input type="hidden" name="pageSize" value="15"/>
	</form>
</div>