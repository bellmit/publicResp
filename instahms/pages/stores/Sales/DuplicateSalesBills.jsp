<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<html>
<head>
	<title><insta:ltext key="salesissues.duplicatesalesbills.list.title"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	<insta:js-bundle prefix="sales.issues"/>
	<script>
		var toolbarOptions = getToolbarBundle("js.sales.issues.toolbar");
		var editRights = '${urlRightsMap.pharma_sale_edit_bill}';
		var salePrintRights = '${urlRightsMap.pharma_sales_print}';
		var billPrintRights = '${urlRightsMap.bill_print}';
		var printerType = null;
		var labelPrinterType = null;
		var templatename = '';
	</script>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="stores/duplicate_sales_bills.js"/>
	<insta:link type="script" file="dashboardsearch.js"/>
	<jsp:include page="/pages/Common/MrnoPrefix.jsp" />
</head>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="billList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty billList}"/>
<body onload="init(); showFilterActive(document.dupSalesSearchForm)">
<c:set var="mrno">
<insta:ltext key="ui.label.mrno"/>
</c:set>
<c:set var="visitid">
<insta:ltext key="salesissues.duplicatesalesbills.list.visitid"/>
</c:set>
<c:set var="billno">
<insta:ltext key="salesissues.duplicatesalesbills.list.billno"/>
</c:set>
<c:set var="hospbillno">
<insta:ltext key="salesissues.duplicatesalesbills.list.hosp.billno"/>
</c:set>
<c:set var="ward">
<insta:ltext key="salesissues.duplicatesalesbills.list.ward"/>
</c:set>
<c:set var="saledate">
<insta:ltext key="salesissues.duplicatesalesbills.list.saledate"/>
</c:set>
<c:set var="patientname">
<insta:ltext key="ui.label.patient.name"/>
</c:set>
<c:set var="doctorname">
<insta:ltext key="salesissues.duplicatesalesbills.list.doctorname"/>
</c:set>
<c:set var="visittype">
<insta:ltext key="salesissues.duplicatesalesbills.list.ip"/>,
<insta:ltext key="salesissues.duplicatesalesbills.list.op"/>,
<insta:ltext key="salesissues.duplicatesalesbills.list.retail"/>,
<insta:ltext key="salesissues.duplicatesalesbills.list.retailcredit"/>
</c:set>
<c:set var="pharmbilltype">
<insta:ltext key="salesissues.duplicatesalesbills.list.sales"/>,
<insta:ltext key="salesissues.duplicatesalesbills.list.returns"/>
</c:set>
<c:set var="billbilltype">
<insta:ltext key="salesissues.duplicatesalesbills.list.billnow"/>,
<insta:ltext key="salesissues.duplicatesalesbills.list.billlater"/>
</c:set>
<h1><insta:ltext key="salesissues.duplicatesalesbills.list.duplicatesalesbills"/> </h1>

<insta:feedback-panel/>

<form name="dupSalesSearchForm" method="GET" >
	<input type="hidden" name="_method" value="getSaleBillsList">
	<input type="hidden" name="_searchMethod" value="getSaleBillsList"/>

	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>
		<insta:search form="dupSalesSearchForm" optionsId="optionalFilter" closed="${hasResults}" >
		  <div class="searchBasicOpts" >
				<div class="sboField">
					<div class="sboFieldLabel"><insta:ltext key="salesissues.duplicatesalesbills.list.mrno.or.patientname"/>:</div>
					<div class="sboFieldInput">
						<div id="mrnoAutoComplete">
							<input type="text" name="mr_no" id="mrno" value="${ifn:cleanHtmlAttribute(param.mr_no)}" />
							<input type="hidden" name="mr_no@op" value="ilike" />
							<div id="mrnoContainer"></div>
						</div>
					</div>
				</div>
			  	<div class="sboField">
					<div class="sboFieldLabel"><insta:ltext key="salesissues.duplicatesalesbills.list.pharmacybillno"/></div>
						<div class="sboFieldInput">
							<input type="text" name="sale_id" value="${ifn:cleanHtmlAttribute(param.sale_id)}" >
						</div>
			    </div>
			    <div class="sboField">
					<div class="sboFieldLabel"><insta:ltext key="salesissues.duplicatesalesbills.list.hospitalbillno"/></div>
					<div class="sboFieldInput">
						<input type="text" name="bill_no" size="10" value="${ifn:cleanHtmlAttribute(param.bill_no)}"/>
					</div>
				</div>
				<div class="sboField">
					<div class="sboFieldLabel"><insta:ltext key="salesissues.duplicatesalesbills.list.store"/></div>
					<div class="sboFieldInput">
						<insta:selectdb id="dept_id" name="dept_id" value="${empty param.dept_id ? pharmacyStoreId : param.dept_id}" table="stores"
									valuecol="dept_id"  displaycol="dept_name" usecache="true"
									class="dropdown"  filtercol="status,is_sales_store" filtervalue="A,Y" orderby="dept_name"/>
					</div>
				</div>
				<input type="hidden" name="dept_id@type" value="integer">
		  </div>
		  <div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
		  	<table class="searchFormTable">
					<tr>
						<td>
							<div class="sfLabel"><insta:ltext key="salesissues.duplicatesalesbills.list.saledate"/></div>
							<div class="sfField">
								<div class="sfFieldSub"><insta:ltext key="salesissues.duplicatesalesbills.list.from"/>:</div>
								<insta:datewidget name="sale_date" id="sale_date0" value="${paramValues.sale_date[0]}"/>
								</div>
							<div class="sfField">
								<div class="sfFieldSub"><insta:ltext key="salesissues.duplicatesalesbills.list.to"/>:</div>
								<insta:datewidget name="sale_date" id="sale_date1" value="${paramValues.sale_date[1]}"/>
								<input type="hidden" name="sale_date@op" value="ge,le"/>
								<input type="hidden" name="sale_date@cast" value="y"/>
							</div>
						</td>
						<td>
							<div class="sfLabel"><insta:ltext key="salesissues.duplicatesalesbills.list.person"/></div>
							<div class="sfField">
								<div class="sfLabel"><insta:ltext key="ui.label.patient.name"/></div>
									<input type="text" name="patient_full_name"  value="${ifn:cleanHtmlAttribute(param.patient_full_name)}"/>
									<input type="hidden" name="patient_full_name@op" value="ilike" />
								</div>
							<div class="sfField">
								<div class="sfLabel"><insta:ltext key="salesissues.duplicatesalesbills.list.doctorname"/></div>
									<input type="text" name="doctor_name"  value="${ifn:cleanHtmlAttribute(param.doctor_name)}"/>
									<input type="hidden" name="doctor_name@op" value="ilike" />
							</div>
						</td>
						<td>
							<div class="sfLabel"><insta:ltext key="salesissues.duplicatesalesbills.list.patienttype"/></div>
							<div class="sfField">
								<insta:checkgroup name="visit_type" selValues="${paramValues.visit_type}"
									opvalues="i,o,r,c" optexts="${visittype}"/>
						    </div>
						</td>
						<td>
							<div class="sfLabel"><insta:ltext key="salesissues.duplicatesalesbills.list.saletype"/></div>
							<div class="sfField">
								<insta:checkgroup name="pharm_bill_type" selValues="${paramValues.pharm_bill_type}"
									opvalues="S,R" optexts="${pharmbilltype}"/>
							</div>
						</td>
						<td class="last">
							<div class="sfLabel"><insta:ltext key="salesissues.duplicatesalesbills.list.billtype"/></div>
							<div class="sfField">
								<insta:checkgroup name="bill_bill_type" selValues="${paramValues.bill_bill_type}"
									opvalues="N,C" optexts="${billbilltype}"/>
							</div>
						</td>
					</tr>
			</table>
		  </div>
		</insta:search>

	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	<div class="resultList">
		<table class="resultList dialog_displayColumns" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
			<tr onmouseover="hideToolBar();">
			    <insta:sortablecolumn name="mr_no" title="${mrno}"/>
				<insta:sortablecolumn name="visit_id" title="${visitid}"/>
				<insta:sortablecolumn name="sale_id" title="${billno}"/>
				<insta:sortablecolumn name="bill_no" title="${hospbillno}"/>
				<insta:sortablecolumn name="ward_name" title="${ward}"/>
				<insta:sortablecolumn name="sale_date" title="${saledate}"/>
				<insta:sortablecolumn name="patient_full_name" title="${patientname}"/>
				<insta:sortablecolumn name="doctor_name" title="${doctorname}"/>
				<th style="text-align: right"><insta:ltext key="salesissues.duplicatesalesbills.list.billamount"/></th>
				<th><insta:ltext key="salesissues.duplicatesalesbills.list.remarks"/></th>
			</tr>

			<c:forEach var="sale" items="${billList}" varStatus="st">
			<c:set var="flagColor">
					<c:choose>
						<c:when test="${sale.pharm_bill_type == 'S'}"><insta:ltext key="salesissues.duplicatesalesbills.list.green"/></c:when>
						<c:otherwise><insta:ltext key="salesissues.duplicatesalesbills.list.red"/></c:otherwise>
					</c:choose>
			</c:set>
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{saleId:'${sale.sale_id}',printerId:printerType,labelPrinterId:labelPrinterType,billNo:'${sale.bill_no}',
						 visitId:'${sale.visit_id}',printerType:printerType,labelPrinterType:labelPrinterType,templatename:templatename,pharm_bill_type:'${sale.pharm_bill_type}'
						 ,visitType:'${sale.visit_type}',bill_visit_type:'${sale.bill_visit_type}'},
						[true,true,true,true,${(salePrintItems eq 'BILLPRESCLABEL'  || salePrintItems eq 'BILLLABEL') && (sale.pharm_bill_type eq 'S')}]);"
					onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}">

					<td>${sale.mr_no}</td>
					<td>${sale.visit_id}</td>
					<td><img class="flag" src="${cpath}/images/${flagColor}_flag.gif"/>${sale.sale_id}</td>
					<td>${sale.bill_no}</td>
					<td>${sale.ward_name}</td>
					<td><fmt:formatDate value="${sale.sale_date}" pattern="dd-MM-yyyy"/></td>
					<td>${sale.patient_full_name}</td>
					<td>${sale.doctor_name}</td>
					<td style="text-align: right">${sale.amount }</td>
					<td><insta:truncLabel value="${sale.remarks}" length="15"/></td>
			</tr>
			</c:forEach>
		</table>

		<c:if test="${param._method == 'getSaleBillsList'}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>

    </div>
    <div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
	<table><tr>
	<td align="right" class="formlabel"><insta:ltext key="salesissues.sales.details.billprint"/>:</td>
	<td>
	<insta:selectdb name="_printerType" table="printer_definition"
		valuecol="printer_id" displaycol="printer_definition_name" id="printerType"
		value="${billPrintBean.map.printer_id}" onchange="setPrinterId(this)"/>
	</td>
	<td align="right" class="formlabel"><insta:ltext key="salesissues.sales.details.sales.print.templates"/>:</td>
	<td>
	<insta:selectdb name="_templatename" table="store_print_template" filtered="false" dummyvalue="---select---" dummyvalueId=""
		valuecol="template_name" displaycol="template_name" id="templatename" onchange="setTemplatename(this)"
		/>
	</td>
	</tr>
	<c:if test="${salePrintItems eq 'BILLPRESCLABEL'  || salePrintItems eq 'BILLLABEL'}">
	<tr><td>&nbsp;</td></tr>
	<tr>
	<td class="formlabel"><insta:ltext key="salesissues.sales.details.billpresclabel"/>:</td>
	<td>
	<insta:selectdb name="_labelPrinterType" table="printer_definition"
		valuecol="printer_id" displaycol="printer_definition_name" id="labelPrinterType"
		value="${prescLabelPrintBean.map.printer_id}" onchange="setLabelPrinterId(this)"/>
	</td></tr>
	</c:if>
	</table>
	</div>
	<div style="clear:both"></div>
    <div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
		<div class="flag"><img src='${cpath}/images/green_flag.gif'></div>
		<div class="flagText"><insta:ltext key="salesissues.duplicatesalesbills.list.sales"/></div>
		<div class="flag"><img src='${cpath}/images/red_flag.gif'></div>
		<div class="flagText"><insta:ltext key="salesissues.duplicatesalesbills.list.returns"/></div>
	</div>
</form>
</body>
</html>
