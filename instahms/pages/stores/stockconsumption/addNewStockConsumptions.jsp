<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>
<%@ page import="com.insta.hms.stores.StoresDBTablesUtil" %>
<html>
<head>
	<title><insta:ltext key="storemgmt.addstockconsumption.addstock.title"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	<script>
	</script>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="dashboardsearch.js"/>
	<insta:link type="script" file="stores/stockconsumption/stockConsumption.js"/>
	<script type="text/javascript">
		var method = '${ifn:cleanJavaScript(param._method)}';
		var jItemNames = ${itemNames};
	</script>

<insta:js-bundle prefix="stores.mgmt"/>
</head>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="stockList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty stockList}"/>
<body onload="init('${consumptionMainBean.map.status}');showFilterActive(document.stockSearchForm)">
<c:set var="itemname">
<insta:ltext key="storemgmt.addstockconsumption.addstock.itemname"/>
</c:set>
<c:set var="storeqty">
<insta:ltext key="storemgmt.addstockconsumption.addstock.storeqty"/>
</c:set>
<c:set var="dummyvalue">
	<insta:ltext key="selectdb.dummy.value"/>
</c:set>
<c:set var="addText">
	<insta:ltext key="storemgmt.addstockconsumption.addstock.add"/>
</c:set>
<c:set var="editText">
	<insta:ltext key="storemgmt.addstockconsumption.addstock.edit"/>
</c:set>
<h1>${param._method != 'showStockConsumption' ? addText : editText} <insta:ltext key="storemgmt.addstockconsumption.addstock.stockconsumption"/></h1>

<insta:feedback-panel/>

<form name="stockSearchForm" method="GET">
	<input type="hidden" name="_method"  id = "_method" value="showStockConsumption" autocomplete="off">
	<input type="hidden" name="_searchMethod" value="showStockConsumption"/>
	<input type="hidden" name="_consumption_id" value="${consumptionMainBean.map.consumption_id}">
	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>
	<insta:search form="stockSearchForm" optionsId="optionalFilter" closed="${hasResults}" validateFunction="checkStore()">
	<div class="searchBasicOpts" >
		<div class="sboField">
			<div class="sboFieldLabel"><insta:ltext key="storemgmt.addstockconsumption.addstock.store"/> :</div>
				<c:choose>
					<c:when test="${not empty consumptionMainBean}">
						<b>${ifn:cleanHtml(storeName)}</b>
						<input type="hidden" name="dept_id" id="dept_id" value="${consumptionMainBean.map.store_id}" />
					</c:when>
					<c:otherwise>
						<c:choose>
						   	<c:when test="${(multiStoreAccess eq 'A' || roleId eq 1 || roleId eq 2 )}">
							<insta:userstores username="${userid}" elename="dept_id" id="dept_id" val="${param.dept_id}"/>
							</c:when>
							<c:otherwise>
								<input type="hidden" name="dept_id" id="dept_id" value="${pharmacyStoreId}" />
								<b><insta:getStoreName store_id="${pharmacyStoreId}"/></b>
							</c:otherwise>
						</c:choose>
					</c:otherwise>
				</c:choose>
				<input type="hidden" name="dept_id@type" value="integer">
		</div>
	</div>
	  <div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
	  	<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel"><insta:ltext key="storemgmt.addstockconsumption.addstock.category"/> :</div>
						<div class="sfField">
							<insta:selectdb name="med_category_id" value="${param.med_category_id}"
						 		table="store_category_master" displaycol="category" valuecol="category_id"
						 		dummyvalue="${dummyvalue}" class="dropdown" orderby="category"/>
			 				<input type="hidden" name="med_category_id@type" value="integer" />
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="storemgmt.addstockconsumption.addstock.manufacturer"/> :</div>
						<div class="sfField">
							<input type="text" name="manf_name" value="${ifn:cleanHtmlAttribute(param.manf_name)}">
							<input type="hidden" name="manf_name@op" value="ilike"/>
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="storemgmt.addstockconsumption.addstock.itemname"/> :</div>
						<div class="sfField">
							<input type="text" name="medicine_name" id="medicine_name" value="${ifn:cleanHtmlAttribute(param.medicine_name)}">
							<input type="hidden" name="medicine_name@op" value="ilike"/>
							<div id="item_dropdown" class="scrolForContainer"></div>
						</div>
					</td>
				</tr>
				<tr>
					<td>
						<div class="sfLabel"><insta:ltext key="storemgmt.addstockconsumption.addstock.bin.rack"/> :</div>
						<div class="sfField">
							<input type="text" name="bin" value="${ifn:cleanHtmlAttribute(param.bin)}">
							<input type="hidden" name="bin@op" value="ilike"/>
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="storemgmt.addstockconsumption.addstock.servicegroup"/> :</div>
						<div class="sfField">
							<insta:selectdb name="service_group_id" value="${param.service_group_id}"
						 		table="service_groups" displaycol="service_group_name" valuecol="service_group_id"
						 		dummyvalue="${dummyvalue}" class="dropdown" orderby="service_group_name"/>
						 	<input type="hidden" name="service_group_id@type" value="integer" />
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="storemgmt.addstockconsumption.addstock.servicesubgroup"/> :</div>
						<div class="sfField">
							<insta:selectdb name="service_sub_group_id" value="${param.service_sub_group_id}"
						 		table="service_sub_groups" displaycol="service_sub_group_name" valuecol="service_sub_group_id"
						 		dummyvalue="${dummyvalue}" class="dropdown" orderby="service_sub_group_name"/>
						 	<input type="hidden" name="service_sub_group_id@type" value="integer" />
						</div>
					</td>
				</tr>
		</table>
	  </div>
</insta:search>
	<c:if test="${empty consumptionMainBean}">
		<insta:noresults hasResults="${hasResults}"/>
	</c:if>
</form>
<form name="stockConsumptionDetailsForm" action="stockconsumtionnew.do" method="POST" autocomplete="off">
<div>&nbsp;</div>
<c:if test="${not empty consumptionMainBean || not empty stockList}">
<div style="width:100%">
 	<fieldset class="fieldsetborder">
	<legend class="fieldsetlabel"><insta:ltext key="storemgmt.addstockconsumption.addstock.consumptiontransactiondetails"/></legend>
	<table style="width:100%">
	 	<tr>
	 		<td valign="top">
			 	<input type="hidden" name="_stockPageNum" value="${ifn:cleanHtmlAttribute(param._stockPageNum)}">
			 	<input type="hidden" name="_method" value="">
			 	<input type="hidden" name="dept_id" value="${ifn:cleanHtmlAttribute(param.dept_id)}">
			 	<input type="hidden" name="dept_id@type" value="integer">
			 	<input type="hidden" name="med_category_id" value="${ifn:cleanHtmlAttribute(param.med_category_id)}">
			 	<input type="hidden" name="med_category_id@type" value="integer" />
			 	<input type="hidden" name="manf_name" value="${ifn:cleanHtmlAttribute(param.manf_name)}"/>
			 	<input type="hidden" name="manf_name@op" value="ilike"/>
			 	<input type="hidden" name="medicine_name" value="${ifn:cleanHtmlAttribute(param.medicine_name)}"/>
			 	<input type="hidden" name="medicine_name@op" value="ilike"/>
			 	<input type="hidden" name="bin" value="${ifn:cleanHtmlAttribute(param.bin)}"/>
			 	<input type="hidden" name="bin@op" value="ilike"/>
			 	<input type="hidden" name="service_group_id" value="${ifn:cleanHtmlAttribute(param.service_group_id)}" />
			 	<input type="hidden" name="service_group_id@type" value="integer" />
			 	<input type="hidden" name="service_sub_group_id" value="${ifn:cleanHtmlAttribute(param.service_sub_group_id)}" />
			 	<input type="hidden" name="service_sub_group_id@type" value="integer" />
			 	<input type="hidden" name="_consumption_id" value="${consumptionMainBean.map.consumption_id}">
				<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
				<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>
				<c:set var="showDiv" value="Y"/>
				<c:choose>
					<c:when test="${consumptionMainBean.map.status == 'X'}">
						<c:set var="showDiv" value="N"/>
					</c:when>
					<c:when test="${consumptionMainBean.map.status == 'F'}">
						<c:set var="showDiv" value="N"/>
					</c:when>
					<c:otherwise>
						<c:set var="showDiv" value="Y"/>
					</c:otherwise>
				</c:choose>
			 	<fieldset class="fieldsetborder">
			 	<legend class="fieldsetlabel"><insta:ltext key="storemgmt.addstockconsumption.addstock.stockdetails"/></legend>
			 	<c:if test="${empty pagedList.pageNumber}">
			 		<div style="height:20px">&nbsp;</div>
			 	</c:if>
			 	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}" pageNumParam="_stockPageNum"/>
			 	<table class="resultList" cellspacing="0" cellpadding="0" id="storeDetailsTable" style="width:450px">
		 			<tr>
		 				<insta:sortablecolumn name="medicine_name" title="${itemname}"/>
		 				<th><insta:ltext key="storemgmt.addstockconsumption.addstock.batch"/></th>
		 				<insta:sortablecolumn name="qty" title="${storeqty}"/>
		 				<th><insta:ltext key="storemgmt.addstockconsumption.addstock.consumed"/></th>
		 				<th><insta:ltext key="storemgmt.addstockconsumption.addstock.balance"/></th>
		 				<th>&nbsp;</th>
		 			</tr>
		 				<c:forEach var="stockDetailsList" items="${stockList}" varStatus="st">
		 					<tr>
			 					<td>
			 						<input type="checkbox" name="_s_select_item" id="_s_select_item${st.index}" onclick="setCheckBoxesValue(this,${st.index})">
			 						<input type="hidden" name="_s_item_batch_id" id="_s_item_batch_id${st.index}" value="${stockDetailsList.item_batch_id}">
			 						<input type="hidden" name="_s_is_insert" id="_s_is_insert${st.index}" value="N">
			 						<input type="hidden" name="_s_item_name" id="_s_item_name${st.index}" value="${stockDetailsList.medicine_name}">
			 						<input type="hidden" name="_s_batch_no" id="s_batch_no${st.index}" value="${stockDetailsList.batch_no}">
			 						<input type="hidden" name="_s_store_qty" id="_s_store_qty${st.index}" value="${stockDetailsList.qty}">
			 						<input type="hidden" name="_s_consumed_qty" id="_s_consumed_qty${st.index}" value="0">
			 						<input type="hidden" name="_s_balance_qty" id="_s_balance_qty${st.index}" value="${stockDetailsList.qty}">
			 						<insta:truncLabel value="${stockDetailsList.medicine_name}" length="15"/>
			 					</td>
			 					<td>${stockDetailsList.batch_no}</td>
			 					<td>${stockDetailsList.qty}</td>
			 					<td>0</td>
			 					<td>${stockDetailsList.qty}</td>
			 					<td>
									<a title='<insta:ltext key="storemgmt.addstockconsumption.addstock.add.editconsumablesqty"/>'>
										<img src="${cpath}/icons/Edit.png" class="button" id="_s_editIcon${st.index}" name="_s_editIcon" onclick="return openStockDialogBox(this)"/>
									</a>
			 					</td>
		 					</tr>
		 				</c:forEach>
		 		</table>
			 	</fieldset>
			 	<c:if test="${hasResults}">
			 		<div class="screenActions">
						<button type="button" accesskey="S" onclick="validateStockDetails()"><b><u><insta:ltext key="storemgmt.addstockconsumption.addstock.s"/></u></b><insta:ltext key="storemgmt.addstockconsumption.addstock.ave"/></button>
					</div>
				</c:if>
			</td>
			<td>&nbsp;</td>
			<td valign="top">
			 	<input type="hidden" name="_consumptionPageNum" value="${ifn:cleanHtmlAttribute(param._consumptionPageNum)}">
			 	<fieldset class="fieldsetborder">
			 	<legend class="fieldsetlabel"><insta:ltext key="storemgmt.addstockconsumption.addstock.stockconsumptions"/></legend>
		 		<c:set var="consumedList" value="${stockConsumptionDetailsList.dtoList}"/>
		 		<c:if test="${empty stockConsumptionDetailsList.pageNumber}">
			 		<div style="height:20px">&nbsp;</div>
			 	</c:if>
		 		<insta:paginate curPage="${stockConsumptionDetailsList.pageNumber}" numPages="${stockConsumptionDetailsList.numPages}"
		 			totalRecords="${stockConsumptionDetailsList.totalRecords}" pageNumParam="_consumptionPageNum"/>
		 		<table class="resultList" cellspacing="0" cellpadding="0" id="stcokConsumptionDetailsTable" style="width:450px">
		 			<tr>
		 				<insta:sortablecolumn name="item_name" title="${itemname}"/>
		 				<th><insta:ltext key="storemgmt.addstockconsumption.addstock.batch"/></th>
		 				<insta:sortablecolumn name="store_qty" title="${storeqty}"/>
		 				<th><insta:ltext key="storemgmt.addstockconsumption.addstock.consumed"/></th>
		 				<th><insta:ltext key="storemgmt.addstockconsumption.addstock.balance"/></th>
		 				<th>&nbsp;</th>
		 			</tr>
		 			<c:set var="className" value="${showDiv == 'Y' ? 'rowbgToolbar' : ''}"/>
		 			<c:forEach var="consumptionDetailsList" items="${consumedList}" varStatus="st">
		 				<tr class="${className}">
		 					<td>
		 						<input type="checkbox" name="_c_select_item" id="_c_select_item${st.index}" onclick="setConsumedItemValue(this,${st.index})">
		 						<input type="hidden" name="_reagent_detail_id" id="_reagent_detail_id${st.index}" value="${consumptionDetailsList.reagent_detail_id}">
		 						<input type="hidden" name="_c_item_batch_id" id="_c_item_batch_id${st.index}" value="${consumptionDetailsList.item_batch_id}">
		 						<input type="hidden" name="_c_is_update" id="_c_is_update${st.index}" value="Y">
		 						<input type="hidden" name="_c_item_name" id="_c_item_name${st.index}" value="${consumptionDetailsList.medicine_name}">
		 						<input type="hidden" name="_c_batch_no" id="_c_batch_no${st.index}" value="${consumptionDetailsList.batch_no}">
		 						<input type="hidden" name="_c_store_qty" id="_c_store_qty${st.index}" value="${consumptionDetailsList.stock_qty}">
		 						<input type="hidden" name="_c_consumed_qty" id="_c_consumed_qty${st.index}" value="${consumptionDetailsList.qty}">
		 						<input type="hidden" name="_c_balance_qty" id="_c_balance_qty${st.index}" value="${consumptionDetailsList.stock_qty-consumptionDetailsList.qty}">
		 						<insta:truncLabel value="${consumptionDetailsList.medicine_name}" length="20"/>
		 					</td>
		 					<td>${consumptionDetailsList.batch_no}</td>
		 					<td>${consumptionDetailsList.stock_qty}</td>
		 					<td>${consumptionDetailsList.qty}</td>
		 					<td>${consumptionDetailsList.stock_qty-consumptionDetailsList.qty}</td>
		 					<td>
		 						<c:if test="${showDiv == 'Y'}">
									<a title='<insta:ltext key="storemgmt.addstockconsumption.addstock.add.edit.consumablesqty"/>' >
										<img src="${cpath}/icons/Edit.png" class="button" id="_c_editIcon${st.index}" name="_c_editIcon" onclick="return openConsumptionDialogBox(this)"/>
									</a>
								</c:if>
		 					</td>
		 				</tr>
		 			</c:forEach>
		 		</table>
		 	</fieldset>
		 	<c:if test="${not empty consumedList && showDiv == 'Y'}">
		 		<div class="screenActions">
					<button type="button" accesskey="A" onclick="validateConsumptionDetails()"><insta:ltext key="storemgmt.addstockconsumption.addstock.s"/><b><u><insta:ltext key="storemgmt.addstockconsumption.addstock.a"/></u></b><insta:ltext key="storemgmt.addstockconsumption.addstock.ve"/></button>
				</div>
			</c:if>
		</td>
	</tr>
	</table>
	</fieldset>
</div>
</c:if>
<div class="screenActions">
	<c:if test="${not empty consumptionMainBean && showDiv == 'Y'}">
		<button type="button" accesskey="C" onclick="validateCancelltion()"><b><u><insta:ltext key="storemgmt.addstockconsumption.addstock.c"/></u></b><insta:ltext key="storemgmt.addstockconsumption.addstock.ancel"/></button>
	</c:if>
 	<c:if test="${not empty stockConsumptionDetailsList.dtoList  && showDiv == 'Y'}">
	 	<button type="button" accesskey="F" onclick="validateFinalization()"><b><u><insta:ltext key="storemgmt.addstockconsumption.addstock.f"/></u></b><insta:ltext key="storemgmt.addstockconsumption.addstock.inalize"/></button>
	</c:if>
	<c:if test="${not empty urlRightsMap.store_stock_consumption_list && urlRightsMap.store_stock_consumption_list == 'A'}">
		<a href="stockconsumtionlist.do?_method=getStoreStockConsumptionList&store_id@type=integer&store_id=${ifn:cleanURL(param.dept_id)}&grum.status=O&sortOrder=consumption_id&sortReverse=false"><insta:ltext key="storemgmt.addstockconsumption.addstock.consumptiondetailslist"/></a>
	</c:if>
</div>
</form>
<form name="s_dialogForm">
	<div id="stock_details_dialog" style="display:none;">
	<div class="hd"><insta:ltext key="storemgmt.addstockconsumption.addstock.edit"/></div>
		<div class="bd">
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel"><insta:ltext key="storemgmt.addstockconsumption.addstock.editstockconsumptions"/></legend>
				<input type="hidden" id="_s_editRowId" name="_s_editRowId" value=""/>
				<table class="formtable">
					<tr>
						<td class="formlabel"><insta:ltext key="storemgmt.addstockconsumption.addstock.itemname"/>:</td>
						<td class="forminfo"><label id="_d_item_name"></label></td>
					</tr>
					<tr>
						<td class="formlabel"><insta:ltext key="storemgmt.addstockconsumption.addstock.batchno"/>:</td>
						<td class="forminfo" ><label id="_d_batch_no"></label></td>
					<tr>
					<tr>
						<td class="formlabel"><insta:ltext key="storemgmt.addstockconsumption.addstock.storeqty"/>:</td>
						<td class="forminfo"><label id="_d_store_qty"></label></td>
					<tr>
					<tr>
						<td class="formlabel"><insta:ltext key="storemgmt.addstockconsumption.addstock.consumedqty"/> :</td>
						<td class="forminfo">
							<input type="text" style="width:60px" name="_d_consumed_qty" id="_d_consumed_qty"
								onchange="return formatAmountObj(this, true);" onkeypress="return enterNumAndDot(event);">
						</td>
					<tr>
					<tr>
						<td class="formlabel"><insta:ltext key="storemgmt.addstockconsumption.addstock.balanceqty"/> :</td>
						<td class="forminfo">
							<input type="text" style="width:60px" name="_d_balance_qty" id="_d_balance_qty"
								onchange="return formatAmountObj(this, true);" onkeypress="return enterNumAndDot(event);">
						</td>
					<tr>
				</table>
				<table style="margin-top: 10px">
					<tr>
						<td><input type="button" id="editDialogOk" name="editDialogOk" value="Ok"/></td>
						<td><input type="button" id="editDialogPrevious" name="editDialogPrevious" value="<<Previous"/></td>
						<td><input type="button" id="editDialogNext" name="editDialogNext" value="Next>>"/></td>
						<td><input type="button" id="editDialogCancel" name="editDialogCancel" value="Close" /></td>
					</tr>
				</table>
			</fieldset>
		</div>
	</div>
</form>

<form name="c_dialogForm">
	<div id="consumption_details_dialog" style="display:none;">
	<div class="hd"><insta:ltext key="storemgmt.addstockconsumption.addstock.edit"/></div>
		<div class="bd">
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel"><insta:ltext key="storemgmt.addstockconsumption.addstock.editstockconsumptions"/></legend>
				<input type="hidden" id="_c_editRowId" name="_c_editRowId" value=""/>
				<table class="formtable">
					<tr>
						<td class="formlabel"><insta:ltext key="storemgmt.addstockconsumption.addstock.itemname"/>:</td>
						<td class="forminfo"><label id="_d1_item_name"></label></td>
					</tr>
					<tr>
						<td class="formlabel"><insta:ltext key="storemgmt.addstockconsumption.addstock.batchno"/>:</td>
						<td class="forminfo" ><label id="_d1_batch_no"></label></td>
					<tr>
					<tr>
						<td class="formlabel"><insta:ltext key="storemgmt.addstockconsumption.addstock.storeqty"/>:</td>
						<td class="forminfo"><label id="_d1_store_qty"></label></td>
					<tr>
					<tr>
						<td class="formlabel"><insta:ltext key="storemgmt.addstockconsumption.addstock.consumedqty"/> :</td>
						<td class="forminfo">
							<input type="text" style="width:60px" name="_d1_consumed_qty" id="_d1_consumed_qty"
								onchange="return formatAmountObj(this, true);" onkeypress="return enterNumAndDot(event);">
						</td>
					<tr>
					<tr>
						<td class="formlabel"><insta:ltext key="storemgmt.addstockconsumption.addstock.balanceqty"/> :</td>
						<td class="forminfo">
							<input type="text" style="width:60px" name="_d1_balance_qty" id="_d1_balance_qty"
								onchange="return formatAmountObj(this, true);" onkeypress="return enterNumAndDot(event);">
						</td>
					<tr>
				</table>
				<table style="margin-top: 10px">
					<tr>
						<td><input type="button" id="editDialogOk1" name="editDialogOk1" value="Ok"/></td>
						<td><input type="button" id="editDialogPrevious1" name="editDialogPrevious1" value="<<Previous"/></td>
						<td><input type="button" id="editDialogNext1" name="editDialogNext1" value="Next>>"/></td>
						<td><input type="button" id="editDialogCancel1" name="editDialogCancel1" value="Close" /></td>
					</tr>
				</table>
			</fieldset>
		</div>
	</div>
 </form>
</body>
</html>