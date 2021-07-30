<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
	<title><insta:ltext key="storeprocurement.supplierreturn.list.title"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	<insta:js-bundle prefix="stores.procurement"/>
	<script>
		var toolbarOptions = getToolbarBundle("js.stores.procurement.toolbar");
		var suppArray = '${fn:join(paramValues.supplier_id, ",")}'.split(",");
	</script>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="stores/view_supp_debit.js"/>
	<insta:link type="script" file="dashboardsearch.js"/>
</head>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="retList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty retList}"/>
<jsp:useBean id="statusDisplay" class="java.util.HashMap"/>
<c:set target="${statusDisplay}" property="C" value="Closed"/>
<c:set target="${statusDisplay}" property="O" value="Open"/>
<jsp:useBean id="typeDisplay" class="java.util.HashMap"/>
<c:set target="${typeDisplay}" property="D" value="Damage"/>
<c:set target="${typeDisplay}" property="E" value="Expiry"/>
<c:set target="${typeDisplay}" property="O" value="Others"/>
<body onload="init(); showFilterActive(document.PhSuppRetDebitSearchForm)">
<c:set var="returntype">
<insta:ltext key="storeprocurement.supplierreturn.list.damage"/>,
<insta:ltext key="storeprocurement.supplierreturn.list.expiry"/>,
<insta:ltext key="storeprocurement.supplierreturn.list.others"/>
</c:set>
<c:set var="status">
<insta:ltext key="storeprocurement.supplierreturn.list.open"/>,
<insta:ltext key="storeprocurement.supplierreturn.list.closed"/>
</c:set>
<c:set var="debitnote">
<insta:ltext key="storeprocurement.supplierreturn.list.debitnote"/>
</c:set>
<c:set var="debitdate">
<insta:ltext key="storeprocurement.supplierreturn.list.debitdate"/>
</c:set>
<c:set var="supplier">
<insta:ltext key="storeprocurement.supplierreturn.list.supplier"/>
</c:set>

<h1><insta:ltext key="storeprocurement.supplierreturn.list.supplierreturn.withdebitnote"/></h1>

<insta:feedback-panel/>
<div>
 	<c:choose>
    	<c:when test="${param._flag=='true'}" >
			<span class="resultMessage"><insta:ltext key="storeprocurement.supplierreturn.list.debitno"/><a href="${pageContext.request.contextPath}/stores/StoresSupplierReturnslist.do?_method=generatePrintForItemReturnNote&debitNo=${ifn:cleanURL(param._message)}" target="_blank"> ${ifn:cleanHtml(param._message)}</a> <insta:ltext key="storeprocurement.supplierreturn.list.generatedforthistransaction"/></span>
		<c:if test="${param._gtpassId != 0}">
		<br></br>
			<span class="resultMessage"><insta:ltext key="storeprocurement.supplierreturn.list.gatepass"/> <a href="${pageContext.request.contextPath}/stores/StoresSupplierReturnslist.do?_method=generateGatePassprintForDebit&debitNo=${ifn:cleanURL(param._message)}" target="_blank"/> ${ifn:cleanHtml(param._gtpassId)}</a>  <insta:ltext key="storeprocurement.supplierreturn.list.generatedforthistransaction"/></span>
		</c:if>
		</c:when>
		<c:otherwise></c:otherwise>
	</c:choose>
 </div></br>

<form name="PhSuppRetDebitSearchForm" method="GET">
	<input type="hidden" name="_method" value="getSupplierReturnDebits">
	<input type="hidden" name="_searchMethod" value="getSupplierReturnDebits"/>

	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

	<insta:search form="PhSuppRetDebitSearchForm" optionsId="optionalFilter" closed="${hasResults}" >
	  <div class="searchBasicOpts" >
	  	<div class="sboField">
			<div class="sboFieldLabel"><insta:ltext key="storeprocurement.supplierreturn.list.debitnoteno"/></div>
				<div class="sboFieldInput">
					<input type="text" name="debit_note_no" value="${ifn:cleanHtmlAttribute(param.debit_note_no)}" onkeypress="return onKeyPressDebitno(event);" onchange="return onChangeDebitno();">
				</div>
	    </div>
	  </div>
	  <div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
	  	<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel"><insta:ltext key="storeprocurement.supplierreturn.list.supplier"/></div>
						<div class="sfField">
							<select name="supplier_id" id="supplier_id" multiple="multiple" class="listbox">
								<c:forEach items="${suppliers}" var="supp">
						            <option value=${supp.SUPPLIER_CODE }>${supp.SUPPLIER_NAME}</option>
						        </c:forEach>
						    </select>
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="storeprocurement.supplierreturn.list.store"/></div>
						<div class="sfField">
							<insta:userstores username="${userid}" elename="store_id" id="store_id" />
							<input type="hidden" name="store_id@type" value="integer"/>
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="storeprocurement.supplierreturn.list.debitnotedate"/></div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="storeprocurement.supplierreturn.list.from"/>:</div>
							<insta:datewidget name="debit_date" id="debit_date0" value="${paramValues.debit_date[0]}"/>
							</div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="storeprocurement.supplierreturn.list.to"/>:</div>
							<insta:datewidget name="debit_date" id="debit_date1" value="${paramValues.debit_date[1]}"/>
							<input type="hidden" name="debit_date@op" value="ge,le"/>
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="storeprocurement.supplierreturn.list.type"/></div>
						<div class="sfField">
							<insta:checkgroup name="return_type" selValues="${paramValues.return_type}"
							opvalues="D,E,O" optexts="${returntype}"/>
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="storeprocurement.supplierreturn.list.status"/></div>
						<div class="sfField">
							<insta:checkgroup name="status" selValues="${paramValues.status}"
							opvalues="O,C" optexts="${status}"/>
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
				<insta:sortablecolumn name="debit_note_no" title="${debitnote}"/>
				<insta:sortablecolumn name="debit_date" title="${debitdate}"/>
				<insta:sortablecolumn name="supplier_name" title="${supplier}"/>
				<th><insta:ltext key="storeprocurement.supplierreturn.list.type"/></th>
			    <th><insta:ltext key="storeprocurement.supplierreturn.list.status"/></th>
			</tr>
			<c:forEach var="supp" items="${retList}" varStatus="st">
			<c:set var="flagColor">
					<c:choose>
						<c:when test="${supp.status == 'O'}">green</c:when>
						<c:otherwise>yellow</c:otherwise>
					</c:choose>
			</c:set>

				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{debitNo: '${supp.debit_note_no}',store:'${supp.store_id }'},
						[true,true]);"
					onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>
					<td>${supp.debit_note_no }</td>
					<td><fmt:formatDate value="${supp.debit_date }" pattern="dd-MM-yyyy"/></td>
					<td>${supp.supplier_name }</td>
				    <td>${typeDisplay[supp.return_type]  }</td>
				    <td>
						<img class="flag" src="${cpath}/images/${flagColor}_flag.gif"/>${statusDisplay[supp.status]}
					</td>
				</tr>
			</c:forEach>
		</table>

		<c:if test="${param._method == 'getSupplierReturnDebits'}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>

    </div>

    <div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
		<div class="flag"><img src='${cpath}/images/green_flag.gif'></div>
		<div class="flagText"><insta:ltext key="storeprocurement.supplierreturn.list.opendebitnotes"/></div>
		<div class="flag"><img src='${cpath}/images/yellow_flag.gif'></div>
		<div class="flagText"><insta:ltext key="storeprocurement.supplierreturn.list.closeddebitnotes"/></div>
	</div>

<div class="screenActions">
	<a  href="StoresSupplierReturnslist.do?_method=getSupplierDebitScreen"><insta:ltext key="storeprocurement.supplierreturn.list.raisenewdebitnote"/> </a>
</div>
</form>
</body>
</html>
