<html>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<c:set var="opList" value="${pagedList.dtoList}"/>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="hasResults" value="${not empty opList}"/>

<head>
	<insta:link type="script" file="dashboardsearch.js"/>
	<jsp:include page="/pages/Common/MrnoPrefix.jsp" />

	<title>OT Stock Consumption - Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<script>
	var psAc = null;

	var toolbar = {

		Return: {
			title: "Return",
			imageSrc: "icons/Collect.png",
			href: 'pages/cssd/OTStockConsumption.do?_method=show',
			onclick: null,
			description: "Stock Conumption or Return"
		}

	};

	function init(){
		psAc = Insta.initMRNoAcSearch(cpath, 'mrno', 'mrnoContainer', 'active', null, null);

		createToolbar(toolbar);;
	}

</script></head>
<body class="yui-skin-sam" onload="init();">

<h1>OT Stock Consumption</h1>
<form name="search">

	<input type="hidden" name="_method" value="list"/>
	<input type="hidden" name="_searchMethod" value="list"/>

	<insta:search form="search" optionsId="optionalFilter" closed="${hasResults}" >
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
		</div>
	  	<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
			<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel">Appointment Date</div>
						<div class="sfField">
							<div class="sfFieldSub">From:</div>
							<insta:datewidget name="appointment_date" id="appointment_date0" value="${paramValues.appointment_date[0]}"/>
						</div>
						<div class="sfField">
							<div class="sfFieldSub">To:</div>
							<insta:datewidget name="appointment_date" id="appointment_date1" value="${paramValues.appointment_date[1]}"/>
							<%-- NOTE: tell the query-builder to use >= and <= operators for the dates --%>
							<input type="hidden" name="appointment_date@op" value="ge,le"/>
							<input type="hidden" name="appointment_date@cast" value="y"/>
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
					<th>Transfer No.</th>
					<th>Mr No.</th>
					<th>Patient Name</th>
					<th>Surgery/Procedure Name</th>
					<th>OT</th>
					<th>Kit</th>
				</tr>
				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st" >


					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}" id="toolbarRow${st.index}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
						{transfer_no: '${record.transfer_no}',kit_id: '${record.kit_id }',appointment_id:'${record.appointment_id }'},[true]);">

						<td>${(pagedList.pageNumber - 1) * pagedList.pageSize + (st.index + 1)}</td>
						<td>${record.transfer_no} </td>
						<td>${record.mr_no} </td>
						<td>${record.patient_name} </td>
						<td><insta:truncLabel value="${record.operation_name}" length="30"/></td>
						<td>${record.theatre_name }</td>
						<td><insta:truncLabel value="${record.kit_name}" length="30"/></td>
					</tr>
				</c:forEach>

			</table>
</form>
</body>
</html>
