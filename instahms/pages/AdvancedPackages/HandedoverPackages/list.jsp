<html>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<c:set var="pkgList" value="${pagedList.dtoList}"/>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="hasResults" value="${not empty pkgList}"/>

<head>
	<title>Conducted Packages - Insta HMS</title>
</head>

<body class="yui-skin-sam" onload="init();">

<h1>Conducted Packages</h1>

<insta:feedback-panel/>

<form name="patpackageform">
	<input type="hidden" name="_method" value="list"/>
	<input type="hidden" name="_searchMethod" value="list"/>

	<insta:search form="patpackageform" optionsId="optionalFilter" closed="false" >
	  <div class="searchBasicOpts" >
	  		<div class="sboField">
				<div class="sboFieldLabel">MR No/Patient Name:</div>
				<div class="sboFieldInput">
					<div id="mrnoAutoComplete">
						<input type="text" name="mr_no" id="mrno" value="${ifn:cleanHtmlAttribute(param.mr_no)}" />
						<div id="mrnoContainer" style="width: 300px"></div>
					</div>
				</div>
			</div>

			<div class="sboField">
				<div class="sboFieldLabel">&nbsp;
					<div class="sboFieldInput">
						<input type="checkbox" name="_mr_no" id="_mr_no" onclick="changeStatus()"/>Active Only
					</div>
				</div>
			</div>
		</div>
		<div id="optionalFilter" style="clear: both; display:'block'" >
			<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel">Sponsors:</div>
						<div class="sfField">
							<insta:selectdb name="primary_sponsor_id" id="primary_sponsor_id" table="tpa_master" displaycol="tpa_name" filtered="true" valuecol="tpa_id" orderby="tpa_name" dummyvalue="-- Select --"
								value="${param.primary_sponsor_id}"/>
					</td>
					<td>
						<div class="sfLabel">Completed Date Range:</div>
						<div class="sfField">
							<div class="sfFieldSub">From:</div>
							<insta:datewidget name="completion_date" id="completion_date0"
								value="${paramValues.completion_date[0]}"/>
						</div>
						<div class="sfField">
							<div class="sfFieldSub">To:</div>
							<insta:datewidget name="completion_date" id="completion_date1"
								value="${paramValues.completion_date[1]}"/>
							<input type="hidden" name="completion_date@op" value="ge,le"/>
							<input type="hidden" name="completion_date@cast" value="y"/>
						</div>
					</td>
					<td>
						<div class="sfLabel">Handedover Date Range:</div>
						<div class="sfField">
							<div class="sfFieldSub">From:</div>
							<insta:datewidget name="handover_date" id="handover_date0"
								value="${paramValues.handover_date[0]}"/>
						</div>
						<div class="sfField">
							<div class="sfFieldSub">To:</div>
							<insta:datewidget name="handover_date" id="handover_date1"
								value="${paramValues.handover_date[1]}"/>
							<input type="hidden" name="handover_date@op" value="ge,le"/>
							<input type="hidden" name="handover_date@cast" value="y"/>
						</div>
					</td>
					<td>
					<div class="sfLabel">Completion Status:</div>
					<div class="sfField">
						<insta:checkgroup name="handover_to" selValues="${paramValues.handover_to}"
						opvalues="P,S" optexts="Handed over to Patient,Handed over to Sponsor"/>
					</div>
					</td>
				</tr>
			</table>
		</div>
 	</insta:search>

<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

<div class="detailList" >

	<table class="detailList" cellspacing="" cellpadding="" id="resultTable" onmouseover="hideToolBar();">
		<tr onmouseover="hideToolBar();">
			<th>#</th>
			<th>Mr No.</th>
			<th>Package Name</th>
			<th>Completed Date</th>
			<th>Handed over Date</th>
			<th>Handed over to</th>
		</tr>


		<c:forEach var="record" items="${pkgList}" varStatus="st" >

			<c:set var="flagColor">
				<c:choose>
					<c:when test="${record.handed_over == 'Y'}">yellow</c:when>
					<c:otherwise>empty</c:otherwise>
				</c:choose>
			</c:set>
			<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}" id="toolbarRow${st.index}"
				onclick="showToolbar(${st.index}, event, 'resultTable',
						{prescription_id: '${record.prescription_id}',patient_id: '${record.patient_id}',mr_no: '${record.mr_no}'},
						[${ record.handover_to == 'P' }]);">
				<td>${((pagedList.pageNumber-1) * pagedList.pageSize) + st.index + 1}</td>
				<td>${record.mr_no}</td>
				<td>
					<img class="flag" src="${cpath}/images/${flagColor}_flag.gif"/>
					<insta:truncLabel value="${record.package_name}" length="30"/>
				</td>
				<td><fmt:formatDate value="${record.completion_date}" pattern="dd-MM-yyyy HH:mm"/></td>
				<td><fmt:formatDate value="${record.handover_time }" pattern="dd-MM-yyyy HH:mm"/></td>
				<td>${record.package_handover_to }</td>
			</tr>
		</c:forEach>
	</table>
	<div class="legend" >
		<div class="flag"><img src='${cpath}/images/yellow_flag.gif'></div>
		<div class="flagText">Handed Over</div>
	</div>
</form>

	<script>var cpath = '${cpath}';</script>
	<insta:link type="script" file="dashboardsearch.js"/>
	<jsp:include page="/pages/Common/MrnoPrefix.jsp" />
	<insta:link type="script" file="AdvancedPackages/handedover_packages.js"/>

</body>
</html>

