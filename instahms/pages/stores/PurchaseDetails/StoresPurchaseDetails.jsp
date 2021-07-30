<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.stores.StoresDBTablesUtil"%>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>
<html>
<head>
	<title><insta:ltext key="storeprocurement.itempurchasedetails.list.title"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	<insta:js-bundle prefix="stores.procurement"/>
	 <script>
		 var toolbarOptions = getToolbarBundle("js.stores.procurement.toolbar.purchasedetails");
		var jmedNames = <%= StoresDBTablesUtil.getTableDataInJSON(StoresDBTablesUtil.GET_MEDICINE_NAMES_IN_MASTER) %>;
		var jgenNames = <%= StoresDBTablesUtil.getNamesInJSON(StoresDBTablesUtil.GET_GENERICS_IN_MASTER) %>;
		var deptId = '${ifn:cleanJavaScript(dept_id)}';
		var gRoleId = '${roleId}';
		var addEditRights = '${urlRightsMap.pharma_view_store_item_batch_details}';
	 </script>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="stores/view_ph_det.js"/>
	<insta:link type="script" file="dashboardsearch.js"/>
	<style type="text/css">
		 	.scrolForContainer .yui-ac-content{
			 max-height:18em;overflow:auto;overflow-x:auto; /* scrolling */
		    _height:18em; max-width:35em; width:35em;/* ie6 */
			}
	</style>
	<insta:js-bundle prefix="stores.procurement"/>
</head>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="medList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty medList}"/>
<c:set var="prefVat" value="<%= GenericPreferencesDAO.getGenericPreferences().getShowVAT() %>" scope="request"/>
<c:set var="taxLabel" value="${genPrefs.procurement_tax_label}" scope="request"/>
<c:set var="prefStockEntryStatus" value="${genPrefs.stock_entry_agnst_do}"/>
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
<body onload="init();  checkstoreallocation(); showFilterActive(document.PhPurchaseDetailsSearchForm);">
<!-- <script>
	var test1 = '${doAllowStatus}';
	var test2 = '${doSchemaAllowStatus}';
	console.log(" doSchemaAllowStatus: "+test2+" doAllowStatus: "+test1);
</script> -->
<div id="storecheck" style="display: block;" >
<h1><insta:ltext key="storeprocurement.itempurchasedetails.list.itempurchasedetails"/></h1>
<c:set var="purchasestatus">
 <insta:ltext key="storeprocurement.itempurchasedetails.list.purchases"/>,
<insta:ltext key="storeprocurement.itempurchasedetails.list.debits"/>
</c:set>
<insta:feedback-panel/>

<form name="PhPurchaseDetailsSearchForm" method="GET">
	<input type="hidden" name="_method" value="getPhPurchaseDetails">
	<input type="hidden" name="_searchMethod" value="getPhPurchaseDetails"/>

	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

	<insta:search form="PhPurchaseDetailsSearchForm" optionsId="optionalFilter" closed="${hasResults}" validateFunction="funValidate()">
	  <div class="searchBasicOpts" >
	  	<div class="sboField">
			<div class="sboFieldLabel"><insta:ltext key="storeprocurement.itempurchasedetails.list.store"/>:</div>
			   <c:choose>
			   <c:when test="${(multiStoreAccess eq 'A' || roleId eq 1 || roleId eq 2 )}">
				<div class="sboFieldInput">
					<insta:userstores username="${userid}" elename="store" id="store"  val="${param.store}"/>
				</div>
			  </c:when>
			  <c:otherwise>
			  <div class="sboFieldInput">
			   <b><insta:getStoreName store_id="${pharmacyStoreId}"/></b>
			   <input type="hidden" name="store" id="store" value="${pharmacyStoreId}" />
			  </div>
			  </c:otherwise>
			  </c:choose>
			   <input type="hidden" name="store@type" value="integer"/>
			   <input type="hidden" name="store@cast" value="y"/>
	    </div>
	    <div class="sboField">
			<div class="sboFieldLabel"><insta:ltext key="storeprocurement.itempurchasedetails.list.itemname"/> :</div>
				<div class="sboFieldInput">
					<div id="med_wrapper" style="width: 18em; padding-bottom:0.2em; ">
							<input type="text" name="medicine_name" id="medicine_name" style="width: 18em"  maxlength="100" value="${ifn:cleanHtmlAttribute(param.medicine_name)}" />
						<div id="med_dropdown" class="scrolForContainer"></div></div>
				</div>
	    </div>
	  </div>
	  <div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
	  	<table class="searchFormTable">
				<tr>
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
							<input type="text" name="batch_no" value="${ifn:cleanHtmlAttribute(param.batch_no)}"/>
							<input type="hidden" name="batch_no@op" value="like"/>
							 <input type="hidden" name="batch_no@type" value="text"/>
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="storeprocurement.itempurchasedetails.list.purchase.or.debitdate"/></div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="storeprocurement.itempurchasedetails.list.from"/>:</div>
							<insta:datewidget name="grn_date" id="grn_date0" value="${paramValues.grn_date[0]}"/>
							</div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="storeprocurement.itempurchasedetails.list.to"/>:</div>
							<insta:datewidget name="grn_date" id="grn_date1" value="${paramValues.grn_date[1]}"/>
							<input type="hidden" name="grn_date@op" value="ge,le"/>
							<input type="hidden" name="grn_date@cast" value="y"/>
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="storeprocurement.itempurchasedetails.list.txntype"/></div>
						<div class="sfField">
							<insta:checkgroup name="type" selValues="${paramValues.type}"
							opvalues="P,D" optexts="${purchasestatus}"/>
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
		        <c:if test="${!doAllowStatus}">
			        <th><insta:ltext key="storeprocurement.itempurchasedetails.list.mrp"/></th>
			        <th><insta:ltext key="storeprocurement.itempurchasedetails.list.rate"/></th>
			        <th><insta:ltext key="storeprocurement.itempurchasedetails.list.disc"/></th>
			        <c:if test="${prefVat eq 'Y'}"><th><insta:ltext key="storeprocurement.itempurchasedetails.list.${taxLabel}"/></th></c:if>
			        <th><insta:ltext key="storeprocurement.itempurchasedetails.list.amt"/></th>
		        </c:if>
			</tr>
			<c:forEach var="medicine" items="${medList}" varStatus="st">
			<c:choose>
			   <c:when test="${medicine.type eq'P'}">
			       <c:set var="enableGrnLink" value="true"/>
			       <c:set var="enableRestOfLink" value="false"/>
				  </c:when>
			    <c:otherwise>
			       <c:set var="enableRestOfLink" value="true"/>
			        <c:set var="enableGrnLink" value="false"/>
		        </c:otherwise>
	        </c:choose>

			<c:set var="flagColor">
					<c:choose>
						<c:when test="${medicine.type == 'P'}"><insta:ltext key="storeprocurement.itempurchasedetails.list.green"/></c:when>
						<c:otherwise><insta:ltext key="storeprocurement.itempurchasedetails.list.blue"/></c:otherwise>
					</c:choose>
			</c:set>
			<c:set var="title">${medicine.contact_person_name},${medicine.supplier_address},${medicine.contact_person_mobile_number}</c:set>

				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{grNo: '${medicine.txn_no}',debitNo: '${medicine.txn_no}',dept_id: '${medicine.store}',medicine_id:'${medicine.medicine_id }',
						 batch_no:'${medicine.batch_no }',hospitaltin:'${hosp_tin}',hospitalpan:'${hosp_pan}',identification:'${medicine.identification}'
						 ,hospitalSerRegno:'${hosp_ser_reg_no}',item_batch_id:'${medicine.item_batch_id}'},
						[true,${enableRestOfLink},${enableGrnLink}]);"
					onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>
					<td title="Invoice No : ${medicine.invoice_no }"> ${medicine.batch_no}</td>
					<td><fmt:formatDate value="${medicine.exp}" pattern="MM-yy"/></td>
					<td  title="${title}"> ${medicine.supplier_name}</td>
					<td> <img class="flag" src="${cpath}/images/${flagColor}_flag.gif"/> ${medicine.txn_no} </td>
					<td><fmt:formatDate value="${medicine.grn_date}" pattern="dd-MM-yyyy"/></td>
					<td> ${medicine.qty/medicine.grn_pkg_size}</td>
					<td>${medicine.bonus_qty }</td>
					<c:if test="${!doAllowStatus}">
						<td>${medicine.mrp }</td>
						<td> ${medicine.rate}</td>
						<td>${medicine.disc}</td>
						<c:if test="${prefVat eq 'Y'}"><td> ${medicine.vat}</td></c:if>
						<td>
						${ifn:afmt(medicine.rate * medicine.qty/medicine.grn_pkg_size - medicine.disc + medicine.vat) }</td>
					</c:if>	
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
	</div>

</form>
<script>
function checkstoreallocation() {
 	if(gRoleId != 1 && gRoleId != 2) {
 		if(deptId == "") {
 			showMessage("js.stores.procurement.noassignedstore.notaccessthisscreen");
		 	document.getElementById("storecheck").style.display = 'none';
 		}
 	}
}
</script>

</div>
</body>
</html>