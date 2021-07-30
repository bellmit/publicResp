<%@page import="com.insta.hms.master.URLRoute"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<jsp:useBean id="seqResetFreqDisplay" class="java.util.HashMap"/>
<c:set target="${seqResetFreqDisplay}" property="D" value="Daily"/>
<c:set target="${seqResetFreqDisplay}" property="M" value="Monthly"/>
<c:set target="${seqResetFreqDisplay}" property="Y" value="Calender Year"/>
<c:set target="${seqResetFreqDisplay}" property="F" value="Financial Year"/>

<jsp:useBean id="transactionTypeDisplay" class="java.util.HashMap"/>
<c:set target="${transactionTypeDisplay}" property="MRN" value="MR No"/>
<c:set target="${transactionTypeDisplay}" property="BLN" value="Bill No"/>
<c:set target="${transactionTypeDisplay}" property="VID" value="Visit Id"/>
<c:set target="${transactionTypeDisplay}" property="PHB" value="Pharmacy Sales Bill No"/>
<c:set target="${transactionTypeDisplay}" property="REP" value="Receipt No"/>
<c:set target="${transactionTypeDisplay}" property="ACN" value="Audit Control No"/>
<c:set target="${transactionTypeDisplay}" property="CID" value="Claim Id"/>
<c:set target="${transactionTypeDisplay}" property="GRN" value="GRN No"/>
<c:set target="${transactionTypeDisplay}" property="PON" value="Purchase Order No"/>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Hospital Id Patterns List - Insta HMS</title>

	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardLookup.js"/>
	<c:set var="pagePath" value="<%=URLRoute.HOSPITAL_ID_PATTERNS_PATH %>"/>
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>

	<style type="text/css">
		.status_InActive{background-color: #E4C89C}
	</style>

	<script type="text/javascript">
		var toolbar = {
			Edit: {
				title: "View/Edit",
				imageSrc: "icons/Edit.png",
				href: '${pagePath}/show.htm?',
				onclick: null,
				description: "View and/or Edit Hospital Id Pattern details"
				}
		};
		function init()
		{
			createToolbar(toolbar);
		}
	</script>
</head>
<body onload="init();">

	<c:set var="hasResults" value="${not empty pagedList.dtoList}"></c:set>

	<h1><insta:ltext key="patient.sequences.hospitalidpattern.hospitalidpatternstitle"/></h1>

	<insta:feedback-panel/>

	<form name="HospitalIdPatternForm" method="GET">
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>
		
		<insta:search-lessoptions form="HospitalIdPatternForm" >
			<div class="searchBasicOpts">
				<div class="sboField">
					<div class="sboFieldLabel"><insta:ltext key="patient.sequences.hospitalidpattern.patternid"/>:</div>
					<div class="sboFieldInput">
						<input type="text" name="pattern_id" value="${ifn:cleanHtmlAttribute(param.pattern_id)}">
						<input type="hidden" name="pattern_id@op" value="ico" />
					</div>
				</div>
			</div>
		</insta:search-lessoptions>

		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
				<tr onmouseover="hideToolBar();">
					<th>#</th>
					<insta:sortablecolumn name="pattern_id" title="Pattern Id"/>
					<th><insta:ltext key="patient.sequences.hospitalidpattern.sequencename"/></th>
					<th><insta:ltext key="patient.sequences.hospitalidpattern.stdprefix"/></th>
					<th><insta:ltext key="patient.sequences.hospitalidpattern.dateprefixpattern"/></th>
					<th><insta:ltext key="patient.sequences.hospitalidpattern.numpattern"/></th>
					<th><insta:ltext key="patient.sequences.hospitalidpattern.seqresetfrequency"/></th>
					<th><insta:ltext key="patient.sequences.hospitalidpattern.dateprefix"/></th>
					<th><insta:ltext key="patient.sequences.hospitalidpattern.transactiontype"/></th>
				</tr>
				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
						 {pattern_id: '${record.pattern_id}'},'');" id="toolbarRow${st.index}">
						<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1 }</td>
						<td>
							${record.pattern_id}
						</td>
						<td>${record.sequence_name}</td>
						<td>${record.std_prefix}</td>
						<td>${record.date_prefix_pattern}</td>
						<td>${record.num_pattern}</td>
						<td>${seqResetFreqDisplay[record.sequence_reset_freq]}</td>
						<td>${record.date_prefix}</td>
						<td>${transactionTypeDisplay[record.transaction_type]}</td>
					</tr>
				</c:forEach>
			</table>
				<insta:noresults hasResults="${hasResults}"/>
		</div>

		<c:url var="url" value="${pagePath}/add.htm">
		</c:url>

		<div class="screenActions" style="float:left"><a href="<c:out value='${url}' />"><insta:ltext key="patient.sequences.hospitalidpattern.addhospitalidpattern"/></a></div>

	</form>
</body>
</html>
