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
	<title><insta:ltext key="storeprocurement.supplierreturns.replacementlist.list.title"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	<insta:js-bundle prefix="stores.procurement"/>
	<script>
		var toolbarOptions = getToolbarBundle("js.stores.procurement.toolbar");
		var suppId = '${ifn:cleanJavaScript(param.supplier_id)}';
		var suppliers = ${suppliers};
	</script>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="stores/view_supp_ret.js"/>
	<insta:link type="script" file="dashboardsearch.js"/>

 	<style type="text/css">
	 	.scrolForContainer .yui-ac-content{
		 max-height:18em;overflow:auto;overflow-x:auto; /* scrolling */
	    _height:18em; max-width:35em; width:35em;/* ie6 */
		}
 	</style>
</head>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="retList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty retList}"/>
<jsp:useBean id="statusDisplay" class="java.util.HashMap"/>
<c:set target="${statusDisplay}" property="C" value="Closed"/>
<c:set target="${statusDisplay}" property="O" value="Open"/>
<c:set target="${statusDisplay}" property="P" value="Partially Received"/>
<c:set target="${statusDisplay}" property="R" value="Received"/>
<jsp:useBean id="typeDisplay" class="java.util.HashMap"/>
<c:set target="${typeDisplay}" property="D" value="Damage"/>
<c:set target="${typeDisplay}" property="E" value="Expiry"/>
<c:set target="${typeDisplay}" property="N" value="Non-moving"/>
<c:set target="${typeDisplay}" property="O" value="Others"/>
<body onload="init(); showFilterActive(document.PhSuppRetSearchForm)">
<c:set var="txnno">
<insta:ltext key="storeprocurement.supplierreturns.replacementlist.list.txn.no"/>
</c:set>
<c:set var="returndate">
<insta:ltext key="storeprocurement.supplierreturns.replacementlist.list.returndate"/>
</c:set>
<c:set var="supplier">
<insta:ltext key="storeprocurement.supplierreturns.replacementlist.list.supplier"/>
</c:set>
<c:set var="txntype">
<insta:ltext key="storeprocurement.supplierreturns.replacementlist.list.returns"/>,
<insta:ltext key="storeprocurement.supplierreturns.replacementlist.list.replacements"/>
</c:set>
<c:set var="returntype">
<insta:ltext key="storeprocurement.supplierreturns.replacementlist.list.damage"/>,
<insta:ltext key="storeprocurement.supplierreturns.replacementlist.list.expiry"/>,
<insta:ltext key="storeprocurement.supplierreturns.replacementlist.list.non_moving"/>,
<insta:ltext key="storeprocurement.supplierreturns.replacementlist.list.others"/>
</c:set>
<c:set var="status">
<insta:ltext key="storeprocurement.supplierreturns.replacementlist.list.open"/>,
<insta:ltext key="storeprocurement.supplierreturns.replacementlist.list.partiallyreceived"/>,
<insta:ltext key="storeprocurement.supplierreturns.replacementlist.list.received"/>,
<insta:ltext key="storeprocurement.supplierreturns.replacementlist.list.closed"/>
</c:set>
<h1><insta:ltext key="storeprocurement.supplierreturns.replacementlist.list.supplierreturns.replacementslist"/></h1>

 <insta:feedback-panel/>
 <div>
 	<c:choose>
    	<c:when test="${param._message gt 0 && param._flag=='true'}" >
			<span class="resultMessage">${ifn:cleanHtml(param._type)}<a href="${pageContext.request.contextPath}/stores/StoresSupplierReturnslist.do?_method=generatePrintForItemReturnNote&return_no=${ifn:cleanURL(param._message)}" target="_blank"> ${ifn:cleanHtml(param._message)} </a> <insta:ltext key="storeprocurement.supplierreturns.replacementlist.list.generatedfortransaction"/></span>
			<c:if test="${param._gtpassId != 0}">
			<br></br>
			<span class="resultMessage"><insta:ltext key="storeprocurement.supplierreturns.replacementlist.list.gatepass"/> <a href="${pageContext.request.contextPath}/stores/StoresSupplierReturnslist.do?_method=generateGatePassprint&return_no=${ifn:cleanURL(param._message)}" target="_blank"/> ${ifn:cleanHtml(param._gtpassId)}</a>  <insta:ltext key="storeprocurement.supplierreturns.replacementlist.list.generatedfortransaction"/></span>
			</c:if>
		</c:when>
		<c:otherwise>${ifn:cleanHtml(param._message)}</c:otherwise>
	</c:choose>
 </div></br>

<form name="PhSuppRetSearchForm" method="GET">
	<input type="hidden" name="_method" value="getSupplierReturns">
	<input type="hidden" name="_searchMethod" value="getSupplierReturns"/>

	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

	<insta:search form="PhSuppRetSearchForm" optionsId="optionalFilter" closed="${hasResults}" >
	  <div class="searchBasicOpts" >
	  	<div class="sboField">
			<div class="sboFieldLabel"><insta:ltext key="storeprocurement.supplierreturns.replacementlist.list.txn.no"/></div>
				<div class="sboFieldInput">
					<input type="text" name="return_no" value="${ifn:cleanHtmlAttribute(param.return_no)}" onkeypress="return enterNumOnlyzeroToNine(event);">
					<input type="hidden" name="return_no@type" value="integer" />
				</div>
	    </div>
	  </div>
	  <div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
	  	<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel"><insta:ltext key="storeprocurement.supplierreturns.replacementlist.list.supplier"/></div>
						<div class="sfField">
						 <div id="supplier_name_wrapper" style="width: 15em;">
							<input type="text" name="supplier_name" id="supplier_name" class="field"/>
							<div id="supplier_name_dropdown" class="scrolForContainer"></div>
						</div>
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="storeprocurement.supplierreturns.replacementlist.list.store"/></div>
						<div class="sfField">
							<insta:userstores username="${userid}" elename="store_id" id="store_id"/>
							<input type="hidden" name="store_id@type" value="integer"/>
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="storeprocurement.supplierreturns.replacementlist.list.returndate"/></div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="storeprocurement.supplierreturns.replacementlist.list.from"/>:</div>
							<insta:datewidget name="return_date" id="return_date0" value="${paramValues.return_date[0]}"/>
							</div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="storeprocurement.supplierreturns.replacementlist.list.to"/>:</div>
							<insta:datewidget name="return_date" id="return_date1" value="${paramValues.return_date[1]}"/>
							<input type="hidden" name="return_date@op" value="ge,le"/>
							<input type="hidden" name="return_date@cast" value="y"/>
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="storeprocurement.supplierreturns.replacementlist.list.txn.type"/></div>
						<div class="sfField">
							<insta:checkgroup name="txn_type" selValues="${paramValues.txn_type}"
							opvalues="O,E" optexts="${txntype}"/>
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="storeprocurement.supplierreturns.replacementlist.list.returntype"/></div>
						<div class="sfField">
							<insta:checkgroup name="return_type" selValues="${paramValues.return_type}"
							opvalues="D,E,N,O" optexts="${returntype}"/>
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="storeprocurement.supplierreturns.replacementlist.list.type"/></div>
						<div class="sfField">
							<insta:checkgroup name="status" selValues="${paramValues.status}"
							opvalues="O,P,R,C" optexts="${status}"/>
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
				<insta:sortablecolumn name="return_no" title="${txnno}"/>
				<insta:sortablecolumn name="return_date" title="${returndate}"/>
				<insta:sortablecolumn name="supplier_name" title="${supplier}"/>
				<th><insta:ltext key="storeprocurement.supplierreturns.replacementlist.list.returntype"/></th>
			    <th><insta:ltext key="storeprocurement.supplierreturns.replacementlist.list.status"/></th>
			</tr>
			<c:forEach var="supp" items="${retList}" varStatus="st">
			<c:set var="replace" value="${supp.txn_type == 'O' && (supp.status == 'O'|| supp.status == 'P')}"/>
			<c:set var="close" value="${supp.txn_type == 'O' && supp.status == 'O'}"/>
			<c:set var="reopen" value="${supp.txn_type == 'O' && supp.status == 'C'}"/>
			<c:set var="replacement">
				<c:choose>
					<c:when test="${supp.txn_type == 'O' }"> false </c:when>
					<c:otherwise>true</c:otherwise>
				</c:choose>
			</c:set>
			<c:set var="flagColor">
					<c:choose>
						<c:when test="${supp.txn_type == 'E'}">green</c:when>
						<c:otherwise>yellow</c:otherwise>
					</c:choose>
			</c:set>

				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{retNo: '${supp.return_no}',status:'${supp.status }',store:'{supp.store_id}', replacement:'${replacement}', return_no: '${supp.return_no}'},
						[true,${replace},${close},${reopen}]);"
					onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>
					<td>${supp.return_no }</td>
					<td><fmt:formatDate value="${supp.return_date }" pattern="dd-MM-yyyy"/></td>
					<td>${supp.supplier_name }</td>
				    <td>${typeDisplay[supp.return_type]  }</td>
				    <td>
						<img class="flag" src="${cpath}/images/${flagColor}_flag.gif"/>${statusDisplay[supp.status]}
					</td>
				</tr>
			</c:forEach>
		</table>

		<c:if test="${param._method == 'getSupplierReturns'}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>

    </div>

    <div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
		<div class="flag"><img src='${cpath}/images/yellow_flag.gif'></div>
		<div class="flagText"><insta:ltext key="storeprocurement.supplierreturns.replacementlist.list.supplierreturn"/></div>
		<div class="flag"><img src='${cpath}/images/green_flag.gif'></div>
		<div class="flagText"><insta:ltext key="storeprocurement.supplierreturns.replacementlist.list.supplierreplacement"/></div>
	</div>

	<div class="screenActions">
		<a href="${cpath }/stores/StoresSupplierReturnslist.do?_method=list&store=0"><insta:ltext key="storeprocurement.supplierreturns.replacementlist.list.raisenewsupplierreturn"/> </a>
	</div>


</form>
</body>
</html>
