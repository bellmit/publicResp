<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.stores.StoresDBTablesUtil"%>

<html>
<head>
	<title><insta:ltext key="storeprocurement.itempurchasedetails.list.itempurchasedetails.instahms1"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	<insta:js-bundle prefix="stores.procurement"/>
	 <script>
		var toolbarOptions = getToolbarBundle("js.stores.procurement.toolbar");
		var jmedNames = <%= StoresDBTablesUtil.getNamesInJSON(StoresDBTablesUtil.GET_MEDICINE_NAMES_IN_MASTER) %>;
		var jgenNames = <%= StoresDBTablesUtil.getNamesInJSON(StoresDBTablesUtil.GET_GENERICS_IN_MASTER) %>;
	 </script>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="pharmacy/view_ph_det.js"/>
	<insta:link type="script" file="dashboardsearch.js"/>
</head>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="medList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty medList}"/>
<body onload="init(); showFilterActive(document.PhPurchaseDetailsSearchForm)">
<c:set var="type">
<insta:ltext key="storeprocurement.itempurchasedetails.list.purchases"/>,
<insta:ltext key="storeprocurement.itempurchasedetails.list.debits"/>,
<insta:ltext key="storeprocurement.itempurchasedetails.list.replacements"/>,
<insta:ltext key="storeprocurement.itempurchasedetails.list.returns"/>
</c:set>
<h1><insta:ltext key="storeprocurement.itempurchasedetails.list.itempurchasedetails"/></h1>

<insta:feedback-panel/>

<form name="PhPurchaseDetailsSearchForm" method="GET">
	<input type="hidden" name="_method" value="getPhPurchaseDetails">
	<input type="hidden" name="_searchMethod" value="getPhPurchaseDetails"/>

	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

	<insta:search form="PhPurchaseDetailsSearchForm" optionsId="optionalFilter" closed="${hasResults}" validateFunction="funValidate()">
	  <div class="searchBasicOpts" >
	  	<div class="sboField">
			<div class="sboFieldLabel"><insta:ltext key="storeprocurement.itempurchasedetails.list.store"/></div>
				<c:choose>
			     <c:when test="${(multiStoreAccess eq 'A' || roleId eq 1 || roleId eq 2 )}">
				<div class="sboFieldInput">
					<insta:selectdb name="store" table="stores" valuecol="dept_id" class="dropdown"
								displaycol="dept_name" value="${pharmacyStoreId}" />
				</div>
				</c:when>
				<c:otherwise>
				<div class="sboFieldInput">
				  <b><insta:getStoreName store_id="${pharmacyStoreId}"/></b>
				  <input type="hidden" name="store" id="store" value="${pharmacyStoreId}" />
				 </div>
				 </c:otherwise>
				 </c:choose>
	    </div>
	  </div>
	  <div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
	  	<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel"><insta:ltext key="storeprocurement.itempurchasedetails.list.itemname"/></div>
						<div class="sfField">
						<div id="med_wrapper" style="width: 18em; padding-bottom:0.2em; ">
							<input type="text" name="medicine_name" id="medicine_name" style="width: 18em"  maxlength="100" value="${ifn:cleanHtmlAttribute(param.medicine_name)}" />
						<div id="med_dropdown"></div></div>
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="storeprocurement.itempurchasedetails.list.genericname"/></div>
						<div class="sfField">
						<div id="gen_wrapper" style="width: 18em; padding-bottom:0.2em; ">
				    			<input type="text" name="generic_name" id="generic_name" style="width: 18em" maxlength="100" value="${ifn:cleanHtmlAttribute(param.generic_name)}" >
				    	<div id="gen_dropdown"></div></div>
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="storeprocurement.itempurchasedetails.list.batch.or.serialno"/></div>
						<div class="sfField">
							<input type="text" name="batch_no" value='${ifn:cleanHtmlAttribute(param.batch_no)}'/>
							<input type="hidden" name="batch_no@op" value="like"/>
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="storeprocurement.itempurchasedetails.list.txntype"/></div>
						<div class="sfField">
							<insta:checkgroup name="type" selValues="${paramValues.type}"
							opvalues="P,D,X,R" optexts="${type}"/>
						</div>
					</td>
				</tr>
		</table>
	  </div>
	</insta:search>

	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	<div class="resultList">
		<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
			<tr onmouseover="hideToolBar();">
				<th ><insta:ltext key="storeprocurement.itempurchasedetails.list.batchno"/></th>
				<th ><insta:ltext key="storeprocurement.itempurchasedetails.list.exp"/></th>
		        <th ><insta:ltext key="storeprocurement.itempurchasedetails.list.supplier"/></th>
		        <th ><insta:ltext key="storeprocurement.itempurchasedetails.list.txn"/>#</th>
		        <th ><insta:ltext key="storeprocurement.itempurchasedetails.list.date"/></th>
		        <th><insta:ltext key="storeprocurement.itempurchasedetails.list.qty.pkg"/></th>
		        <th><insta:ltext key="storeprocurement.itempurchasedetails.list.bonusqty"/></th>
		        <th><insta:ltext key="storeprocurement.itempurchasedetails.list.mrp"/></th>
		        <th><insta:ltext key="storeprocurement.itempurchasedetails.list.rate"/></th>
		        <th><insta:ltext key="storeprocurement.itempurchasedetails.list.disc"/></th>
		        <th><insta:ltext key="storeprocurement.itempurchasedetails.list.vat"/></th>
		        <th><insta:ltext key="storeprocurement.itempurchasedetails.list.amt"/></th>
			</tr>
			<c:set var="grnprint" value="grnreport&grNo="/>
			<c:set var="debitprintURL" value="StoresDebitNoteReport&debitNo="/>
			<c:set var="returnprintURL" value="PharmacySupplierReturnsReport&retNo="/>
			<c:set var="replaceprintURL" value="PharmacySupplierReplacementReport&retNo="/>
			<c:forEach var="medicine" items="${medList}" varStatus="st">
			<c:set var="printurl">
				<c:choose>
					<c:when test="${medicine.type =='R'}"> ${returnprintURL}</c:when>
					<c:when test="${medicine.type =='X'}">${replaceprintURL}</c:when>
					<c:when test="${medicine.type =='P'}">${grnprint}</c:when>
					<c:otherwise> ${debitprintURL}</c:otherwise>
				</c:choose>
			</c:set>
			<c:set var="flagColor">
					<c:choose>
						<c:when test="${medicine.type == 'R'}"><insta:ltext key="storeprocurement.itempurchasedetails.list.red"/></c:when>
						<c:when test="${medicine.type == 'X'}"><insta:ltext key="storeprocurement.itempurchasedetails.list.yellow"/></c:when>
						<c:when test="${medicine.type == 'P'}"><insta:ltext key="storeprocurement.itempurchasedetails.list.green"/></c:when>
						<c:otherwise><insta:ltext key="storeprocurement.itempurchasedetails.list.blue"/></c:otherwise>
					</c:choose>
			</c:set>
			<c:set var="title">${medicine.contact_person_name},${medicine.supplier_address},${medicine.contact_person_mobile_number}</c:set>

				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{report:'${printurl}${medicine.txn_no}',dept_id: '${medicine.store}',medicine_id:'${medicine.medicine_id }',
						 batch_no:'${medicine.batch_no }'},
						[true,true]);"
					onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>
					<td> ${medicine.batch_no}</td>
					<td><fmt:formatDate value="${medicine.exp}" pattern="MM-yy"/></td>
					<td  title="${title}"> ${medicine.supplier_name}</td>
					<td> <img class="flag" src="${cpath}/images/${flagColor}_flag.gif"/> ${medicine.txn_no} </td>
					<td><fmt:formatDate value="${medicine.grn_date}" pattern="dd-MM-yyyy"/></td>
					<td> ${medicine.qty} </td>
					<td>${medicine.bonus_qty }</td>
					<td>${medicine.mrp }</td>
					<td> ${medicine.rate}</td>
					<td> ${medicine.disc}</td>
					<td> ${medicine.vat}</td>
					<td>${ifn:afmt(medicine.rate * medicine.qty - medicine.disc + medicine.vat) }</td>

				</tr>
			</c:forEach>
		</table>

		<c:if test="${param._method == 'getPhPurchaseDetails'}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>

    </div>

    <div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
		<div class="flag"><img src='${cpath}/images/green_flag.gif'></div>
		<div class="flagText"><insta:ltext key="storeprocurement.itempurchasedetails.list.purchases"/></div>
		<div class="flag"><img src='${cpath}/images/blue_flag.gif'></div>
		<div class="flagText"><insta:ltext key="storeprocurement.itempurchasedetails.list.debitnotes"/></div>
		<div class="flag"><img src='${cpath}/images/yellow_flag.gif'></div>
		<div class="flagText"><insta:ltext key="storeprocurement.itempurchasedetails.list.replacements"/></div>
		<div class="flag"><img src='${cpath}/images/red_flag.gif'></div>
		<div class="flagText"><insta:ltext key="storeprocurement.itempurchasedetails.list.returns"/></div>
	</div>

</form>
</body>
</html>