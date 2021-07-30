<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<%
request.setAttribute("mysearches",
	com.insta.hms.search.SearchDAO.getMySearches((String) request.getAttribute("actionId")));
%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<c:set var="pagedList" value="${pagedList}" scope="request" /> 
<c:set var="receiveSamplesList" value="${pagedList.dtoList}"/>
<c:set var="childSamplesList" value="${child_samples}" scope="request"/>
<c:set var="testCenterAssociation" value="${test_center_association}" scope="request"/>
<c:set var="hasResults" value="${not empty receiveSamplesList}" />
<c:set var="barcodeActionResponse" value="${param._barcode_action_response}" />
<c:set var="isSplitNeeded" value="${param._split_needed}" />
<c:set var="actionResponseClass" value="" />

<c:choose>
	<c:when test="${fn:length(barcodeActionResponse) == 0}">
		<c:set var="actionResponseClass" value="hidden" />
	</c:when>
	<c:when test="${isSplitNeeded == false}">
		<c:set var="actionResponseClass" value="normal-message" />
	</c:when>
	<c:otherwise>
		<c:set var="actionResponseClass" value="alert-message" />
	</c:otherwise>
</c:choose>
<head>
	<title>
		<insta:ltext key="laboratory.receivesamples.list.laboratoryreceiveandsplitsamples" />
	</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="css" file="widgets.css" />
	<insta:link type="css" file="Laboratory/labreceive.css"></insta:link>
	<insta:link type="js" file="dashboardsearch.js" />
	<insta:link type="js" file="diagnostics/receive_sample_manual.js" />
	<insta:link type="js" file="diagnostics/Laboratory/splitComponents.js" />
	<insta:link type="js" file="diagnostics/Laboratory/receiveAndSplitSample.js" />
	<insta:js-bundle prefix="laboratory.receivesample" />
	<insta:js-bundle prefix="laboratory.radiology.receivesample" />
	<script src="<%=request.getContextPath()%>/gettests.do?${test_timestamp}&${ifn:cleanURL(sesHospitalId)}&module=${ifn:cleanURL(module)}">
	</script>
	<script>
		var cpath = '${cpath}';
		var samplePrintType = '${genPrefs.sampleCollectionPrintType}';
		var collectionCenters = ${collectionCenters};
		var allTestNames = deptWiseTestsjson;
		var sampleCollectionIDs = [];
		var sampleDetailsDataMap = {};
		var testCenterMap = {};
		var childSamples = {};
		var prescribedIDTestNameMap = {};
	</script>
</head>

	<body onload="initAll(); ajaxForPrintUrls();">
        <!--initializing test center association and parent-child sample details-->
		<c:import url="receiveSamplesIncludes/TestCenterAssociationAndChildSamplesInit.jsp">
		</c:import>

		<jsp:useBean id="currentDate" class="java.util.Date" />
		<!-- Page head -->
        <h1 class="screen-name">
		    <insta:ltext key="laboratory.receivesamples.list.laboratoryreceiveandsplitsamples" />
        </h1>
		<h1 class="barcode-action-response ${actionResponseClass}">${barcodeActionResponse}</h1>
		<!-- Page head ends -->
		<insta:feedback-panel /> <!-- for Flash messages -->

        <!--search section and barcode receive form-->
		<jsp:include page="receiveSamplesIncludes/SampleSearchAndBarcodeReceiveSection.jsp">
            <jsp:param name="hasResults" value="${hasResults}"/>
            <jsp:param name="cpath" value="${cpath}" />
		</jsp:include>

		<div style="box-sizing: content-box" class="resultList">
            <!--listing all the samples retrieved w.r.t. search criteria specified.-->
            <c:import url="receiveSamplesIncludes/ReceiveSamplesList.jsp" />

            <!--displaying details specific to sample selected by user-->
            <jsp:include page="receiveSamplesIncludes/ReceiveSampleSpecificDetails.jsp"></jsp:include>
		</div>
		<div align="right" style="display: ${genPrefs.sampleCollectionPrintType == 'SL'?'none':'block'}">
			<insta:selectdb id = "sample-barcode-template-selection" name="sampleBarCodeTemplate" table="sample_bar_code_print_templates" valuecol="template_name"
					displaycol="template_name" filtered="false" value="${template_name}"/>
		</div>
	</body>
</html>