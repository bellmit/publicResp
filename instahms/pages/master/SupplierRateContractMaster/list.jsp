<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<html>
<c:set var="max_centers" value='<%= GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default") %>' scope="request"/>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title><insta:ltext key="master.supplierratecontract.raisecontract.supplierratecontract.list"/> - Insta HMS</title>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>
	<script type="text/javascript">
	   var toolbar = {
			   Edit : {
					title : "Edit Supplier Rate Contract",
					imageSrc : "icons/Edit.png",
					href : "pages/master/SupplierRateContractMaster.do?_method=addshow",
					onclick: null,
					description: "View/Edit Supplier Rate  Contract Details",
					show:true
					},
				CenterEdit : {
					title : "Center Applicability",
					imageSrc : "icons/Edit.png",
					href : "pages/master/SupplierRateContractMasterCenterAssociation.do?_method=getScreen",
					onclick: null,
					description: "View/Edit Supplier Rate Contract Center Applicability",
					show: ${max_centers > 1}
					},
				ItemEdit : {
					title : "View/Edit Supplier Item Rates",
					imageSrc : "icons/Edit.png",
					href : "pages/master/SupplierContractMaster/SupplierContractItemRates.do?_method=Redirectlist",
					onclick: null,
					description: "View/Edit Supplier Item Rates",
					show:true
					},
				ItemAdd : {
					title : "Add Supplier Item Rates",
					imageSrc : "icons/Edit.png",
					href : "pages/master/SupplierRateContractMaster.do?_method=addScreen",
					onclick: null,
					description: "Add Supplier Item Rates",
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
		var dataList = searchListData.autoCompleteData;
		var ds = new YAHOO.util.LocalDataSource({result: dataList});
		ds.responseType = YAHOO.util.DataSourceBase.TYPE_JSON;
		ds.responseSchema = { resultsList : "result",
			fields: [ {key: "supplier_rate_contract_name"}, {key: "supplier_rate_contract_id"}],  };   
		
		var rAutoComp = new YAHOO.widget.AutoComplete('supplier_rate_contract_name','supplier_rate_contract_name_container', ds);
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
<style type="text/css">
	.searchBasicOpts{
		-webkit-box-sizing:unset;
		box-sizing:unset;
	}
	</style>
<form name="SupplierRateContractsSearchForm" method="GET">
		<c:set var="hasResults" value="${not empty pagedList.dtoList}"></c:set>
        <input type="hidden" name="_method" value="list"/>
		<input type="hidden" name="_searchMethod" value="list"/>
		<input type="hidden" name="sortOrder" value="${param.sortOrder}"/>
		<input type="hidden" name="sortReverse" value="${param.sortReverse}"/>

<h1><insta:ltext key="master.supplierratecontract.raisecontract.supplierratecontract.list"/></h1> 
 
 <insta:search-lessoptions form="SupplierRateContractsSearchForm" >
	<table class="searchBasicOpts">
		<tr>
			<td class="sboField">
				<div class="sboFieldLabel"><insta:ltext key="master.supplierratecontract.raisecontract.contractname"/>:</div>
				<div class="sboFieldInput">
					<input type="text" name="supplier_rate_contract_name" id="supplier_rate_contract_name" value="${param.supplier_rate_contract_name}" style = "width:20em"/>
					<input type="hidden" name="supplier_rate_contract_name@op" value="ilike" />
					<div id="supplier_rate_contract_name_container" style = "width:20em"></div>
				</div>
			</td>
			<td class="sboField" style="padding-left: 147px;">
				<div class="sboFieldLabel"><insta:ltext key="suppliercontracts.itemrate.addshow.suppliername"/>: </div>
				<div class="sboFieldInput">
				<%-- 
			  		<insta:selectdb name="supplier_code" table="supplier_master" id="supplier_code"
							displaycol="supplier_name" valuecol="supplier_code" dummyvalue="---All---" dummyvalueid="" 
							filtered="true" filtercol="status" filtervalue="A" orderby="supplier_name" value="${param.supplier_code}"/>
				--%>
				<select name="supplier_code" id="supplier_code" class = "dropdown">
			 			<option value="">---All---</option>
			 			<option value="-1" ${param.supplier_code == '-1' ? 'selected':''}>DEFAULT SUPPLIER</option>
					<c:forEach var="supp" items="${supplierlist}">${supp.map.supplier_name}
						<option value="${supp.map.supplier_code}" ${param.supplier_code == supp.map.supplier_code ? 'selected':''}>${supp.map.supplier_name}</option>
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
			<%-- <td class="sboField" >
				<c:if test="${max_centers > 1}">
					<div class="sboFieldLabel"><insta:ltext key="master.supplierratecontract.raisecontract.center"/>:</div>
					<div class="sboFieldInput">
						<insta:selectdb name="center_id" table="hospital_center_master" id="center_id"
								displaycol="center_name" valuecol="center_id" dummyvalue="---All---" dummyvalueid="" 
								filtered="true" filtercol="status" filtervalue="A" orderby="center_name" value="${param.center_id}"/>
						<input type="hidden" name="center_id@type" value="integer" />
					</div>
				</c:if>
			</td> --%>
		</tr>
	</table>
</insta:search-lessoptions>
 
 
  <insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
 	 
	<div class="resultList">
		<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
			<tr onmouseover="hideToolBar();">
				<c:set var="contractname"><insta:ltext key="master.supplierratecontract.raisecontract.contractname"/></c:set>
				<c:set var="suppliername"><insta:ltext key="suppliercontracts.itemrate.addshow.suppliername"/></c:set>
				<c:set var="suppliercontratname"><insta:ltext key="suppliercontracts.itemrate.list.suppliercontractname"/></c:set>
				<insta:sortablecolumn name="supplier_rate_contract_name" title="${contractname}"/>
				<insta:sortablecolumn name="supplier_name" title="${suppliername}"/>
				<th>Validity Start</th> 
				<th>Validity End</th> 
			</tr>
			<c:forEach var="itemList" items="${pagedList.dtoList}" varStatus="st">
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
					{supplier_rate_contract_id: '${itemList.supplier_rate_contract_id}',supplier_code: '${itemList.supplier_code}',status:'${itemList.status}'},[true,true,true,${itemList.status == 'A'}]);" 
					id="toolbarRow${st.index}">
		
					<td>
						<c:if test="${itemList.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
						<c:if test="${itemList.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
						${itemList.supplier_rate_contract_name}
					</td>
					<td>${itemList.supplier_name}</td>
					<fmt:parseDate value="${itemList.validity_start_date}" pattern="yyyy-MM-dd" var="fdt"/>
					<fmt:formatDate value="${fdt}" pattern="dd-MM-yyyy" var="frm"/>
					<td>${frm}</td>
					<fmt:parseDate value="${itemList.validity_end}" pattern="yyyy-MM-dd" var="tdt"/>
					<fmt:formatDate value="${tdt}" pattern="dd-MM-yyyy" var="to"/>
					<td>${to}</td>
				</tr>
			</c:forEach>			
		</table>
		<insta:noresults hasResults="${hasResults}"/>
	</div> 
	<div style="float: none; margin-top: 10px;">
		<insta:screenlink screenId="mas_supplier_rate_contracts" addPipe="false" label="Add Supplier Rate Contract"
					extraParam="?_method=addshow" title="Add Supplier Rate Contract"/>
	    <div  style="display: ${hasResults? 'block' : 'none'};float: right;" >
	  		<div class="flag"><img src='${cpath}/images/empty_flag.gif'></div>
	  		<div class="flagText">Active</div>
			<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
			<div class="flagText">Inactive</div>
		</div>
    </div> 
</form>
</body>
</html>