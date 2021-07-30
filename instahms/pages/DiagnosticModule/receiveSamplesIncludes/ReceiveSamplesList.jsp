<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>

<c:set var="pagedList" value="${requestScope.pagedList}" />
<c:set var="receiveSamplesList" value="${pagedList.dtoList}" />

<div class="sample-meta-details">
    <div class="sample-meta-details-header">
        <h2>
            <insta:ltext key="laboratory.receivesamples.list.samples" />
            <c:if test="${pagedList.totalRecords > 0}">
                <c:if test="${pagedList.totalRecords <= 9}">
                    <c:set var="appendZero" value="0"></c:set>
                </c:if>
                <span>(${appendZero}${pagedList.totalRecords} Records)</span>
            </c:if>
        </h2>
        <c:if test="${pagedList.totalRecords > 20}">
            <insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}"
                totalRecords="${pagedList.totalRecords}" showRecordCount="false" />
        </c:if>
    </div>
    <form name="resultsForm" method="POST">
    	<div class="sample-meta-details-list">
    		<table class="resultList" id="resultTable">
    			<tr>
    				<th>
    					<input id="master-check-box" type="checkbox"
    					   onclick="return toggleCheckForGroup('receiveCheck',this)" />
    				</th>
    				<th>#</th>
    				<th>
                        <insta:ltext key="laboratory.receivesamples.list.sampleno" />
                    </th>
    				<th>
                        <insta:ltext key="laboratory.receivesamples.list.receivestatus" />
                    </th>
    				<th>
                        <insta:ltext key="laboratory.receivesamples.list.splitstatus" />
                    </th>
    			</tr>
    			<c:set var="count" value="0" />
    			<c:forEach items="${receiveSamplesList}" var="receiveSample" varStatus="st">
    				<c:set var="sampleIndex" value="${((pagedList.pageNumber-1) * pagedList.pageSize) + st.index + 1}" />
    				<script>
    						var sampleDetails = {};
    						var outsourceDestinationIDs = [];
    						sampleCollectionIDs.push(parseInt("${receiveSample.map.sample_collection_id}", 10));
    						sampleDetails.patientName = "${receiveSample.map.patient_name}";
    						sampleDetails.sponsorType = "${receiveSample.map.patient_sponsor_type}";
    						sampleDetails.sourceCenter = "${receiveSample.map.source_center_name }";
    						if (sampleDetails.sourceCenter.length === 0) {
    							sampleDetails.sourceCenter = "-";
    						}
    						sampleDetails.visitID = "${receiveSample.map.pat_id }";
    						sampleDetails.ageWithUnit = "${receiveSample.map.patient_age}" + "${receiveSample.map.age_unit}";
    						sampleDetails.patientGender = "${receiveSample.map.patient_gender}";
    						if (sampleDetails.patientGender === 'M') {
    							sampleDetails.patientGender = "Male";
    						} else if (sampleDetails.patientGender === 'F') {
    							sampleDetails.patientGender = "Female";
    						} else if (sampleDetails.patientGender === 'C') {
    							sampleDetails.patientGender = "Couple";
    						} else {
    							sampleDetails.patientGender = "Other";
    						}
    						sampleDetails.collectionCenter = "${receiveSample.map.collection_center}";
    						if (sampleDetails.collectionCenter.length === 0) {
    							sampleDetails.collectionCenter = "-";
    						}
    						var transferDateTime = "${receiveSample.map.transfer_time}";
    						if (transferDateTime.length > 0) {
    							var transferDate = transferDateTime.substring(0,10).split("-").reverse().join("/");
    							var transferTime = transferDateTime.substring(11,16);
    							sampleDetails.transferDateTime = transferDate + " " + transferTime;
    						} else {
    							sampleDetails.transferDateTime = "-";
    						}
    						sampleDetails.mrNo = "${receiveSample.map.mr_no}";
    						sampleDetails.transferBatchId = "${receiveSample.map.transfer_batch_id}";
    						if (sampleDetails.transferBatchId.length === 0) {
    							sampleDetails.transferBatchId = "-";
    						}
    						var sampleDate = "${receiveSample.map.sample_date}";
    						if (sampleDate.length > 0) {
    							var collectionDate = sampleDate.substring(0,10).split("-").reverse().join("/");
    							var collectionTime = sampleDate.substring(11,16);
    							sampleDetails.collectionDateTime = collectionDate + " " + collectionTime;
    						} else {
    							sampleDetails.collectionDateTime = "-";
    						}
    						sampleDetails.sampleType = "${receiveSample.map.sample_type}";
    						sampleDetails.sampleNo = "${receiveSample.map.sample_no}";
    						sampleDetails.currSampleNo = "${receiveSample.map.current_sample_no}";
    					    var testsInSample = {};
    						var testName = "${receiveSample.map.test_name}".split("|");
    						var testID = "${receiveSample.map.test_id}".split(", ");
    						var deptName = "${receiveSample.map.ddept_name}".split(", ");
    						var prescribedID = "${receiveSample.map.prescribed_id}".split(", ");

    						sampleDetails.isSplittable = ("${receiveSample.map.sample_receive_status}" === "R");
    						sampleDetails.isSplittable = sampleDetails.isSplittable && (testID.length > 1);
    						sampleDetails.isSplittable = sampleDetails.isSplittable && ("${receiveSample.map.curr_sample_transfer_status}" !== "T");

    						for (var i = 0; i < testID.length; i++) {
    							var testDetail = {},
    								destination = "",
    								outsourceDestinationID = null;
    							
    							testDetail.testName = testName[i];
    							testDetail.testID = testID[i];
    							testDetail.deptName = deptName[i];
    							testDetail.prescribedID = prescribedID[i];
    							prescribedIDTestNameMap[testDetail.prescribedID] = testDetail.testName;

    							if (testID[i] in testCenterMap) { // Outhouse test
    								if (testCenterMap[testID[i]].testCategory === "splitable") {
    									destination = testCenterMap[testID[i]].destination;
    									testDetail.outsourceDestinationID = testCenterMap[testID[i]].outsourceDestinationID;
    								} else {
    									destination = "UNDECIDED";
    									outsourceDestinationIDs.push(-2);
    								}
    							} else {
    								destination = "INHOUSE";
    							}

    							if (!(destination in testsInSample)) {
    								testsInSample[destination] = [];
    								if (destination === "INHOUSE") {
    									outsourceDestinationIDs.push(-1);
    								} else if (destination !== "UNDECIDED"){
    									outsourceDestinationIDs.push(testDetail.outsourceDestinationID);
    								}
    							}
    							testsInSample[destination].push(testDetail);
    						}
    						sampleDetails.outsourceDestinationIDs = outsourceDestinationIDs;
    						sampleDetails.totalDestinations = Object.keys(testsInSample).length;
    						if ("UNDECIDED" in testsInSample) {
    							sampleDetails.totalDestinations += testsInSample["UNDECIDED"].length - 1;
    						}

    						sampleDetails.tests = testsInSample;
    						var currentSampleNo = "${receiveSample.map.current_sample_no}";
    						sampleDetailsDataMap["${st.index}"] = {};
    						sampleDetailsDataMap["${st.index}"].visitID = "${receiveSample.map.pat_id}";
    						sampleDetailsDataMap["${st.index}"].parentSample = sampleDetails;
    						sampleDetailsDataMap["${st.index}"].childSamples = childSamples[currentSampleNo];
    					</script>
    				<c:if test="${empty receiveSample.map.aliquot_parent_sample_no}">
    					<tr class="sample-meta-details-list-item" id="toolbarRow${st.index}">
    						<c:set var="count" value="${count+1}" />
    						<c:set var="checkboxVisibility" value="" />
    						<c:if test="${(receiveSample.map.sample_receive_status == 'R' || receiveSample.map.sample_transfer_status == 'P')}">
    							<c:set var="checkboxVisibility" value="hidden" />
    						</c:if>
    						<c:set var="isSampleSplittable" value="N" />
    						<c:if test="${(receiveSample.map.sample_receive_status == 'R' && receiveSample.map.sample_transfer_status != 'P')}">
    							<c:set var="isSampleSplittable" value="Y" />
    						</c:if>
    						<td class="checkbox-td">
    							<input class="${checkboxVisibility}" type="checkbox" name="receiveCheck" 
    								id="receiveCheck" value="${receiveSample.map.sample_collection_id}-${receiveSample.map.sample_no}"
    								${checkboxVisibility == 'hidden' ?'disabled' : ''}
    								onclick="toggleCheck(this,${st.index});" 
    							/>
    						</td>
    						<c:set var="appendZero" value="" />
    						<c:if test="${sampleIndex <= 9 && sampleIndex > 0}">
    							<c:set var="appendZero" value="0" />
    						</c:if>
    						<td>${appendZero}${sampleIndex}</td>
    						<td>${receiveSample.map.sample_no}</td>
    						<c:choose>
    							<c:when test="${receiveSample.map.sample_receive_status == 'P'}">
    								<td class="sample-status font-color-light-red">
                                        <insta:ltext key="laboratory.receivesamples.list.pending" />
                                    </td>
    							</c:when>
    							<c:otherwise>
    								<td class="sample-status font-color-light-green">
                                        <insta:ltext key="laboratory.receivesamples.list.received" />
                                    </td>
    							</c:otherwise>
    						</c:choose>
    						<c:choose>
    							<c:when test="${receiveSample.map.sample_split_status == 'N'}">
    								<td class="sample-status font-color-light-grey">
                                        <insta:ltext key="laboratory.receivesamples.list.notneeded"/>
                                    </td>
    				      		</c:when>
    							<c:when test="${receiveSample.map.sample_split_status == 'P'}">
    								<td class="sample-status font-color-light-red">
                                        <insta:ltext key="laboratory.receivesamples.list.required" />
                                    </td>
    							</c:when>
    							<c:otherwise>
    								<td class="sample-status font-color-light-green">
                                        <insta:ltext key="laboratory.receivesamples.list.done" />
                                    </td>
    							</c:otherwise>
    						</c:choose>
    					</tr>
    				</c:if>
    			</c:forEach>
    		</table>
    	</div>
    	<div class="form-submit-option hidden">
    		<button type="button" class="lab-receive-button" id="markReceived" accessKey="S" 
                onclick="return markReceivedAndPrint('');" disabled>
    			<insta:ltext key="laboratory.receivesamples.list.m" /><insta:ltext key="laboratory.receivesamples.list.arkreceived" />
    		</button>
    		<button type="button" class="lab-receive-button" id="print-all" accessKey="P">
    			<insta:ltext key="laboratory.receivesamples.list.printall" />
    		</button>
    	</div>
    </form>
</div>
