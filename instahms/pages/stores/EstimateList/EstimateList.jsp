<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<html>
<head>
	<title><insta:ltext key="salesissues.pharmacyestimateslist.list.title"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="dashboardsearch.js"/>
	<insta:js-bundle prefix="sales.issues"/>
	<script>
		var toolbarOptions = getToolbarBundle("js.sales.issues.toolbar");
	</script>
	<script>
		var toolbar = {}
			toolbar.Print= {
				title: toolbarOptions["print"]["name"],
				imageSrc: "icons/Print.png",
				href: 'pages/stores/MedicineSalesPrint.do?method=getEstimatePrint',
				onclick: null,
				target: '_blank',
				description: toolbarOptions["print"]["description"]
			};
		function init() {
			initMrNoAutoComplete(cpath);
			document.searchForm.mr_no.focus();
			createToolbar(toolbar);
		}
	</script>
</head>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="estimateList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty estimateList}"/>
<body onload="init(); showFilterActive(document.searchForm)" class="yui-skin-sam">
<c:set var="mrno">
<insta:ltext key="ui.label.mrno"/>
</c:set>
<c:set var="patientcustomername">
<insta:ltext key="salesissues.pharmacyestimateslist.list.patient.or.customername"/>
</c:set>
<c:set var="doctorname">
<insta:ltext key="salesissues.pharmacyestimateslist.list.doctorname"/>
</c:set>
<c:set var="estimateno">
<insta:ltext key="salesissues.pharmacyestimateslist.list.estimatenoheader"/>
</c:set>
<c:set var="date">
<insta:ltext key="salesissues.pharmacyestimateslist.list.date"/>
</c:set>

<h1><insta:ltext key="salesissues.pharmacyestimateslist.list.pharmacyestimateslist"/></h1>

<insta:feedback-panel/>

<form name="searchForm" method="GET">
	<input type="hidden" name="_method" value="search">
	<input type="hidden" name="_searchMethod" value="search"/>
	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

	<insta:search form="searchForm" optionsId="optionalFilter" closed="${hasResults}" >
	  <div class="searchBasicOpts">
			<div class="sboField">
				<div class="sboFieldLabel"><insta:ltext key="salesissues.pharmacyestimateslist.list.mrno.or.patientname"/>:</div>
				<div class="sboFieldInput">
					<div id="mrnoAutoComplete">
						<input type="text" name="mr_no" id="mrno" value="${ifn:cleanHtmlAttribute(param.mr_no)}" />
						<input type="hidden" name="mr_no@op" value="ilike" />
						<div id="mrnoContainer" style="width: 300px"></div>
					</div>
				</div>
			</div>
		  	<div class="sboField">
				<div class="sboFieldLabel"><insta:ltext key="salesissues.pharmacyestimateslist.list.estimateno"/>:</div>
					<div class="sboFieldInput">
						<input type="text" name="estimate_id" value="${ifn:cleanHtmlAttribute(param.estimate_id)}" >
					</div>
		    </div>
	  </div>

	  <div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
	  	<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel"><insta:ltext key="salesissues.pharmacyestimateslist.list.estimatedate"/></div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="salesissues.pharmacyestimateslist.list.from"/>:</div>
							<insta:datewidget name="estimate_date" id="est_date0" value="${paramValues.estimate_date[0]}"/>
							</div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="salesissues.pharmacyestimateslist.list.to"/>:</div>
							<insta:datewidget name="estimate_date" id="est_date1" value="${paramValues.estimate_date[1]}"/>
							<input type="hidden" name="estimate_date@op" value="ge,le"/>
							<input type="hidden" name="estimate_date@cast" value="y"/>
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="salesissues.pharmacyestimateslist.list.patient.or.customername"/></div>
						<div class="sfField">
							<input type="text" name="full_name"  value="${ifn:cleanHtmlAttribute(param.full_name)}"/>
							<input type="hidden" name="full_name@op" value="ilike" />
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="salesissues.pharmacyestimateslist.list.doctorname"/></div>
						<div class="sfField">
							<input type="text" name="doctor_name"  value="${ifn:cleanHtmlAttribute(param.doctor_name)}"/>
							<input type="hidden" name="doctor_name@op" value="ilike" />
						</div>
					</td>
					<td class="last"></td>
					<td class="last"></td>
				</tr>
			</table>
	  </div>
	</insta:search>

	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	<div class="resultList">
		<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
			<tr onmouseover="hideToolBar();">
		    <insta:sortablecolumn name="mr_no" title="${mrno}"/>
				<insta:sortablecolumn name="full_name" title="${patientcustomername}"/>
				<insta:sortablecolumn name="doctor_name" title="${doctorname}"/>
				<insta:sortablecolumn name="estimate_id" title="${estimateno}"/>
				<insta:sortablecolumn name="estimate_date" title="${date}"/>
				<th><insta:ltext key="salesissues.pharmacyestimateslist.list.totalamount"/></th>
			</tr>

			<c:forEach var="estimate" items="${estimateList}" varStatus="st">
				<c:set var="visitType" value="${empty estimate.mr_no ? 'r' : 'h'}"/>

				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{estimateId:'${estimate.estimate_id}', visitType:'${ifn:cleanJavaScript(visitType)}'},
						[true]);"
					onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>

					<td>${estimate.mr_no}</td>
					<td>${estimate.full_name}</td>
					<td>${estimate.doctor_name}</td>
					<td>${estimate.estimate_id}</td>
					<td><fmt:formatDate value="${estimate.estimate_date}" pattern="dd-MM-yyyy HH:mm:ss"/></td>
					<td>${estimate.total_item_amount - estimate.discount + estimate.round_off}</td>
				</tr>
			</c:forEach>
		</table>

		<c:if test="${param._method == 'search'}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>

	</div>

</form>
</body>
</html>

