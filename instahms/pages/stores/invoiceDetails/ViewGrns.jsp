<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<html>
<head>
	<title><insta:ltext key="storeprocurement.grnlist.list.title"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:js-bundle prefix="stores.procurement"/>
	<script>
	var toolbarOptions = getToolbarBundle("js.stores.procurement.toolbar");
	  var suppId = '${ifn:cleanJavaScript(param.supplier_id)}';
	  var suppliers = ${suppliers};
	  var storeArray = '${paramValues.dept_id}' == '' ? '${default_store}' : '${fn:join(paramValues.dept_id, ",")}'.split(",");

	  var roleId = ${roleId};
	  var multiStoreAccess = '${multiStoreAccess}';

	  function checkstoreallocation() {
	 		if(roleId != 1 && roleId != 2) {
	 		if(storeArray == "") {
	 		showMessage("js.stores.procurement.noassignedstore.notaccessthisscreen");
	 		document.GrnSearchForm.style.display = 'none';
	 		}
	 	}
	}

	</script>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="stores/view_grn.js"/>
	<insta:link type="script" file="dashboardsearch.js"/>
	<insta:js-bundle prefix="stores.procurement"/>
</head>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="prefStockEntryStatus" value="${stock_entry_agnst_do}"/>
<c:set var="grnList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty grnList}"/>
<body onload="init(); checkstoreallocation(); showFilterActive(document.GrnSearchForm)">
<c:set var="consignmentstatus">
 <insta:ltext key="storeprocurement.grnlist.list.normal"/>,
<insta:ltext key="storeprocurement.grnlist.list.consignment"/>
</c:set>
<c:set var="finalizestatus">
<insta:ltext key="storeprocurement.grnlist.list.open"/>,
<insta:ltext key="storeprocurement.grnlist.list.finalized"/>,
<insta:ltext key="storeprocurement.grnlist.list.closed"/>
</c:set>
<c:set var="grnno">
<insta:ltext key="storeprocurement.grnlist.list.grnno"/>
</c:set>
<c:set var="grndate">
<insta:ltext key="storeprocurement.grnlist.list.grndate"/>
</c:set>
<c:set var="dono">
<insta:ltext key="storeprocurement.grnlist.list.dono"/>
</c:set>
<c:set var="invoiceno">
<insta:ltext key="storeprocurement.grnlist.list.invoiceno"/>
</c:set>
<c:set var="supplier">
<insta:ltext key="storeprocurement.grnlist.list.supplier"/>
</c:set>
<h1><insta:ltext key="storeprocurement.grnlist.list.grnlist"/></h1>
<c:set var="doAllowStatus">
	<c:choose>
		<c:when test="${fn:toLowerCase(prefStockEntryStatus) eq 'y' && userCenterId != 0}">true</c:when>
		<c:otherwise>false</c:otherwise>
	</c:choose>
</c:set>
<c:set var="doSchemaAllowStatus">
	<c:choose>
		<c:when test="${fn:toLowerCase(prefStockEntryStatus) eq 'y'}">true</c:when>
		<c:otherwise>false</c:otherwise>
	</c:choose>
</c:set>
<insta:feedback-panel/>

<form name="GrnSearchForm" method="GET">
	<input type="hidden" name="_method" value="getGrns">
	<input type="hidden" name="_searchMethod" value="getGrns"/>

	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

	<insta:search form="GrnSearchForm" optionsId="optionalFilter" closed="${hasResults}" >
	  <div class="searchBasicOpts" >
	  	<div class="sboField">
			<div class="sboFieldLabel"><insta:ltext key="storeprocurement.grnlist.list.grnno"/></div>
				<div class="sboFieldInput">
					<input type="text" name="grn_no" value="${ifn:cleanHtmlAttribute(param.grn_no)}" onkeypress="return onKeyPressGRNno(event);" onblur="onChangeGRNno();">
				</div>
	    </div>
	    <div class="sboField">
			<div class="sboFieldLabel"><insta:ltext key="storeprocurement.grnlist.list.supplier"/> : </div>
			    <div class="sboFieldInput">
					<div id="supplier_name_wrapper" style="width: 20em;">
							<input type="text" name="supplier_name" id="supplier_name" class="field" />
							<div id="supplier_name_dropdown"></div>
						</div>
				</div>
	    </div>
	  </div>
	  <div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
	  	<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel"><insta:ltext key="storeprocurement.grnlist.list.store"/></div>
						<div class="sfField">
						<c:choose>
							<c:when test="${roleId ==1 || roleId==2 || (multiStoreAccess == 'A')}">
								<insta:userstores username="${userid}" elename="dept_id"
									 id="dept_id" defaultVal="${pharmacyStoreId}" val="${param.dept_id}"/>
								<input type="hidden" name="dept_id@type" value="integer"/>
							</c:when>
							<c:otherwise>
									<insta:getStoreName store_id="${pharmacyStoreId}"/>
									<input type = "hidden" name="dept_id" id="dept_id" value="${pharmacyStoreId}" />
									<input type="hidden" name="dept_id@type" value="integer"/>
							</c:otherwise>
						</c:choose>
						</div>
					</td>
					<td>
						<c:choose>
							<c:when test="${doSchemaAllowStatus}"><div class="sfLabel"><insta:ltext key="storeprocurement.grnlist.list.dono"/></div></c:when>
							<c:otherwise><div class="sfLabel"><insta:ltext key="storeprocurement.grnlist.list.invoiceno"/></div></c:otherwise>
						</c:choose>
						<div class="sfField">
							<input type="text" name="invoice_no" value="${ifn:cleanHtmlAttribute(param.invoice_no)}"/>
							<input type="hidden" name="invoice_no@op" value="like"/>
					    </div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="storeprocurement.grnlist.list.grndate"/></div>
						<div class="sfField">
							<div class="sfFieldSub" ><insta:ltext key="storeprocurement.grnlist.list.from"/></div>
							<insta:datewidget name="grn_date" id="grn_date0" value="${paramValues.grn_date[0]}"/>
					    </div>
					    <div class="sfField">
							<div class="sfFieldSub" ><insta:ltext key="storeprocurement.grnlist.list.to"/></div>
							<insta:datewidget name="grn_date" id="grn_date1" value="${paramValues.grn_date[1]}"/>
							<input type="hidden" name="grn_date@op" value="ge,le"/>
							<input type="hidden" name="grn_date@cast" value="y"/>
					    </div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="storeprocurement.grnlist.list.stocktype"/></div>
						<div class="sfField">
							<insta:checkgroup name="consignment_stock" selValues="${paramValues.consignment_stock}"
							opvalues="false,true" optexts="${consignmentstatus}"/>
							<input type="hidden" name="consignment_stock@op" value="eq"/>
							<input type="hidden" name="consignment_stock@type" value="boolean"/>
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="storeprocurement.grnlist.list.status"/></div>
						<div class="sfField">
							<insta:checkgroup name="status" selValues="${paramValues.status}"
							opvalues="O,F,C" optexts="${finalizestatus}"/>
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
				<insta:sortablecolumn name="grn_no" title="${grnno}"/>
				<insta:sortablecolumn name="supplier_name" title="${supplier}"/>
				<c:choose>
					<c:when test="${doSchemaAllowStatus}">
						<insta:sortablecolumn name="invoice_no" title="${dono}"/>
					</c:when>
					<c:otherwise>
						<insta:sortablecolumn name="invoice_no" title="${invoiceno}"/>
					</c:otherwise>
				</c:choose>
				<insta:sortablecolumn name="grn_date" title="${grndate}"/>
				<th><insta:ltext key="storeprocurement.grnlist.list.store"/></th>
				<th><insta:ltext key="storeprocurement.grnlist.list.stocktype"/></th>
				<c:if test="${!doAllowStatus}">
					<th style="text-align: right"><insta:ltext key="storeprocurement.grnlist.list.invoiceamt"/></th>	
				</c:if>
			</tr>

			<c:forEach var="grn" items="${grnList}" varStatus="st">
			<c:set var="grnstore" value="${grn.dept_id}"/>
			<c:set var="stockEntryEnabled" >
				<c:choose>
				 <c:when test="${(roleId eq 1 || roleId eq 2 )}">${grn.status eq 'O'}</c:when>
				 <c:when test="${multiStoreAccess eq 'A'}">
				 	${grn.status eq 'O' && ifn:listFind(deptids,grnstore) ne -1}
				 </c:when>
				 <c:otherwise>${grn.status eq 'O' && fn:contains(pharmacyStoreId,grnstore)}</c:otherwise>
				</c:choose>
			</c:set>

				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{grNo: '${grn.grn_no}',doAllowStatus: '${doAllowStatus}',doSchemaAllowStatus: '${doSchemaAllowStatus}', grnPrintTemplate: document.getElementById('grn_print_template').value, 
						printerType : document.getElementById('print_type').value
					},
						[true,${stockEntryEnabled},${not stockEntryEnabled}]);"
					onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>
					<c:set var ="flagColor" >
						<c:choose>
							<c:when test="${grn.status == 'F'}">blue</c:when>
							<c:when test="${grn.status == 'C'}">grey</c:when>
							<c:otherwise>empty</c:otherwise>
						</c:choose>
					</c:set>
					<td><img class="flag" src="${cpath}/images/${flagColor}_flag.gif"/>${grn.grn_no}</td>
					<td>${grn.supplier_name}</td>
					<td>${grn.invoice_no}</td>
					<td><fmt:formatDate value="${grn.grn_date}" pattern="dd-MM-yyyy"/></td>
					<td>${grn.dept_name }</td>
					<c:choose>
						<c:when test="${grn.consignment_stock}">
							<td><insta:ltext key="storeprocurement.grnlist.list.consignment"/></td>
						</c:when>
						<c:otherwise>
							<td><insta:ltext key="storeprocurement.grnlist.list.normal"/></td>
						</c:otherwise>
					</c:choose>
					<c:if test="${!doAllowStatus}">
						<td style="text-align: right">${ifn:afmt(grn.invoice_amt)}</td>
					</c:if>
					
				</tr>
			</c:forEach>
		</table>

		<c:if test="${param._method == 'getGrns'}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>
		<div style="float: left; padding-top: 3px;">
			<insta:ltext key="ui.label.grn.print.template"/> : 
			<select name="" id="grn_print_template" class="dropdown">
				<option value="BUILTIN_HTML" ${defaultGrnPrintTemplate == "BUILTIN_HTML" ? "selected" : ""}>Built-in Default HTML template</option>
				<c:forEach var="grn_print_template" items="${grnPrintTemplates}" varStatus="st">
					<option ${defaultGrnPrintTemplate == grn_print_template.template_name ? "selected" : ""} value="${grn_print_template.template_name}">${grn_print_template.template_name}</option>
				</c:forEach>
			</select>
			<insta:selectdb name="" table="printer_definition" id="print_type"
					valuecol="printer_id"  displaycol="printer_definition_name"
					value="${printPref.map.printer_id}"
					/>

		</div>
		
		

		<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
			<div class="flag"><img src='${cpath}/images/empty_flag.gif'></div>
			<div class="flagText"><insta:ltext key="storeprocurement.grnlist.list.open"/></div>
			<div class="flag"><img src='${cpath}/images/blue_flag.gif'></div>
			<div class="flagText"><insta:ltext key="storeprocurement.grnlist.list.finalized"/></div>
			<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
			<div class="flagText"><insta:ltext key="storeprocurement.grnlist.list.closed"/></div>
		</div>

    </div>


</form>
</body>
</html>
