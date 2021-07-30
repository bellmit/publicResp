<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title><insta:ltext key="suppliercontracts.itemrate.list.title"/> - Insta HMS</title>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<c:set var="DecimalDigits" value="<%= GenericPreferencesDAO.getGenericPreferences().getDecimalDigits() %>"/>
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>
	<script type="text/javascript">
	   var toolbar = {
			   Edit : {
					title : "Edit Item Rates",
					imageSrc : "icons/Edit.png",
					href : "pages/master/SupplierContractMaster/SupplierContractItemRates.do?_method=show",
					onclick: null,
					description: "View/Edit Supplier Contract Item Rates Details",
					show:true
				}
			} 
					
		var searchListData = {
				autoCompleteData : <%=request.getAttribute("lookupListMap")%>
		}	
		  
		function init()
		{ 
		   createToolbar(toolbar);
		   autoItems();
			
		} 
				 
  function autoItems() {
		var dataList = searchListData.autoCompleteData["medicine_name"];
		var ds = new YAHOO.util.LocalDataSource({result: dataList});
		ds.responseType = YAHOO.util.DataSourceBase.TYPE_JSON;
		ds.responseSchema = { resultsList : "result",
			fields: [ {key: "medicine_name"}, {key: "medicine_id"}],  };   
		
		var rAutoComp = new YAHOO.widget.AutoComplete('medicine_name','medicine_name_container', ds);
		rAutoComp.minQueryLength = 0;
	 	rAutoComp.maxResultsDisplayed = 20;
	    rAutoComp.forceSelection = false;
	 	rAutoComp.animVert = false;
	 	rAutoComp.resultTypeList = false;
	 	rAutoComp.typeAhead = false;
	 	rAutoComp.allowBrowserAutocomplete = false;
	 	rAutoComp.prehighlightClassname = "yui-ac-prehighlight";
		rAutoComp.autoHighlight = false;
		rAutoComp.useShadow = false; 
		rAutoComp.allowBroserAutocomplete = false;
		
		if (rAutoComp._elTextbox.value != '') {
			rAutoComp._bItemSelected = true;
			rAutoComp._sInitInputValue = rAutoComp._elTextbox.value;
		} 
		
}  

	</script>

</head>

<body onload="init();">
<form name="SupplierItemRatesListSearchForm" method="GET">
		<c:set var="hasResults" value="${not empty pagedList.dtoList}"></c:set>
        <input type="hidden" name="_method" value="list"/>
		<input type="hidden" name="_searchMethod" value="list"/>
		<input type="hidden" name="sortOrder" value="${param.sortOrder}"/>
		<input type="hidden" name="sortReverse" value="${param.sortReverse}"/>

<h1><insta:ltext key="suppliercontracts.itemrate.list.title"/></h1> 
 
 <insta:search-lessoptions form="SupplierItemRatesListSearchForm" >
	<table class="searchBasicOpts" >
		<tr>
			<td class="sboField">
				<div class="sboFieldLabel"><insta:ltext key="suppliercontracts.itemrate.addshow.itemname"/>:</div>
				<div class="sboFieldInput">
					<input type="text" name="medicine_name" id="medicine_name" value="${param.medicine_name}" style = "width:20em"/>
					<input type="hidden" name="medicine_name@op" value="ilike" />
					<div id="medicine_name_container" style = "width:20em"></div>
				</div>
				<div class="sboFieldLabel"></div>
				<div class="sboFieldInput">	</div>
			</td><td></td>
			<td class="sboField">
				<div class="sboFieldLabel"><insta:ltext key="suppliercontracts.itemrate.list.suppliercontractname"/>: </div>
				<div class="sboFieldInput">
					<input type="hidden" name="supplier_rate_contract_id@type" value="integer" />
					<select class="dropdown" name="supplier_rate_contract_id" id="supplier_rate_contract_id">
						<option value=""> ----All---- </option>
						<c:forEach items="${supplierContractList}" var="scList">
							<option value="${scList.map.supplier_rate_contract_id}" ${param.supplier_rate_contract_id == scList.map.supplier_rate_contract_id ? 'selected' : ''}>${scList.map.supplier_rate_contract_name}</option>
						</c:forEach>
			  		</select>
				</div>
			</td>
			<td class="sboField" >
				<div class="sboFieldLabel"><insta:ltext key="suppliercontracts.itemrate.list.status"/>:</div>
				<div class="sboFieldInput"  style="height:50px">
					<insta:checkgroup name="status" opvalues="A,I" optexts="Active,Inactive" selValues="${paramValues.status}"/>
					<input type="hidden" name="status@op" value="in" />
				</div>
			</td>
		</tr>
	</table>
</insta:search-lessoptions>
 
 
 <insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
 	 
	<div class="resultList">
		<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
			<tr onmouseover="hideToolBar();">
				<c:set var="itemname"><insta:ltext key="suppliercontracts.itemrate.addshow.itemname"/></c:set>
				<c:set var="suppliername"><insta:ltext key="suppliercontracts.itemrate.addshow.suppliername"/></c:set>
				<c:set var="suppliercontratname"><insta:ltext key="suppliercontracts.itemrate.list.suppliercontractname"/></c:set>
				<insta:sortablecolumn name="medicine_name" title="${itemname}"/>
				<insta:sortablecolumn name="supplier_name" title="${suppliername}"/>
				<insta:sortablecolumn name="supplier_rate_contract_name" title="${suppliercontratname}"/>
				<th><insta:ltext key="master.supplierratecontract.raisecontract.vstart"/></th> 
				<th><insta:ltext key="master.supplierratecontract.raisecontract.vend"/></th> 
				<th><insta:ltext key="suppliercontracts.itemrate.addshow.rate"/></th> 
				<th><insta:ltext key="suppliercontracts.itemrate.addshow.discount"/></th> 
				<th><insta:ltext key="suppliercontracts.itemrate.addshow.mrp"/></th>
			</tr>
			<c:forEach var="itemList" items="${pagedList.dtoList}" varStatus="st">
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
					{medicine_id: '${itemList.medicine_id}',supplier_code: '${itemList.supplier_code}',
					supplier_rate_contract_id:'${itemList.supplier_rate_contract_id}'},'');" 
					id="toolbarRow${st.index}">
		
					<td><insta:truncLabel value="${itemList.medicine_name}" length="30"/></td>
					<td><insta:truncLabel value="${itemList.supplier_name}" length="30"/></td>
					<td>
						<c:if test="${itemList.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
						<c:if test="${itemList.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
						${itemList.supplier_rate_contract_name}
					</td>
					
					<fmt:parseDate value="${itemList.validity_start_date}" pattern="yyyy-MM-dd" var="dtfrm"/>
					<fmt:formatDate value="${dtfrm}" pattern="dd-MM-yyyy" var="frm"/>
					<td>${frm}</td>
				
				
					<fmt:parseDate value="${itemList.validity_end}" pattern="yyyy-MM-dd" var="dt"/>
					<fmt:formatDate value="${dt}" pattern="dd-MM-yyyy" var="to"/>
					<td>${to}</td>
					
					<td>
						<fmt:formatNumber type="number" minFractionDigits="${DecimalDigits}" maxFractionDigits="${DecimalDigits}" value="${itemList.supplier_rate}" />
					</td>
					<td>
						<fmt:formatNumber type="number" minFractionDigits="${DecimalDigits}" maxFractionDigits="${DecimalDigits}" value="${itemList.discount}" />
					</td>
					<td>
						<fmt:formatNumber type="number" minFractionDigits="${DecimalDigits}" maxFractionDigits="${DecimalDigits}" value="${itemList.mrp}" />
					</td>
				</tr>
			</c:forEach>			
		</table>
		<insta:noresults hasResults="${hasResults}"/>
	</div>  
    <div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
  		<div class="flag"><img src='${cpath}/images/empty_flag.gif'></div>
  		<div class="flagText">Active</div>
		<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
		<div class="flagText">Inactive</div>
    </div>
</form>
</body>
</html>