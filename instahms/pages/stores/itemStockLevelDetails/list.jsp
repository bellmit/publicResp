<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>
<html>
<c:set var="prefbarcode" value="<%= GenericPreferencesDAO.getGenericPreferences().getBarcodeForItem() %>"/>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<head>
	<title><insta:ltext key="storemgmt.itemstockleveldetails.list.title"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="dashboardsearch.js"/>
	<script type="text/javascript">
	    var prefbarcode = '${prefbarcode}';
		var toolbar = {
				BarCode: {
					title: "Print Barcode",
					imageSrc: "icons/Report.png",
				    href: '#',
					target: '',
					onclick: null, 
					description: "Print Barcode",
					show:  ${prefbarcode == 'Y'}
					}
			};
	
		var pxl = 400;
		function initBarCodeDialog(){
			BarCodeDialog = new YAHOO.widget.Dialog("BarCodeDialog",
					{
					width:pxl+"px",
						
					context :["BarCodeDialog", "tr", "br",],
					visible:false,
					modal:true,
					constraintoviewport:true,
					});

					var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
			                                              { fn:handleClose,
			                                                scope:BarCodeDialog,
			                                                correctScope:true } );
					BarCodeDialog.cfg.queueProperty("keylisteners", escKeyListener);

					BarCodeDialog.render();
		}
		
		function setValue(){
			if(isNaN(document.getElementById("printNum").value)){
				alert("Please enter numbers only.");
				return false;
			}else{
				var num = Math.round(document.getElementById("printNum").value);
				if(num < 1){
					alert("Please enter more than zero.");
					return false;
				}
				document.getElementById("printNum").value = num;
				return true;
			}
		}
			
		function handlePrint(){
				if(document.getElementById('BarCodeDialog_c').style.visibility == "hidden"){
					return false;
				}
			
				if(setValue() && document.getElementById('BarCodeDialog').style.display == 'block'){
				   var noOfPrints = document.getElementById("printNum").value;
				   var med = document.getElementById("_itemId").value;
				   
				   if(noOfPrints <= 1000){
					   document.getElementById('BarCodeDialog').style.display='none';
					   BarCodeDialog.cancel();
					   window.open("${cpath}/stores/StoreItemBatchDetailsList.do?_method=insertBarCode&barcodeType=ItemMaster&itemId="+med+"&noOfPrints="+noOfPrints);
					   return false; 
				   }else{
					   alert("Please enter less than 1000");
					   return false;
				   }					
				}else{ 
					return false;
				}
		}
		
		function handleClose(){
			BarCodeDialog.cancel();
		}
		
		function validation(event){
			if(event.which == 13){
				return false;
			}else{
				return enterNumOnlyzeroToNine(event);
			}	 	
		}
		
		function getBarcodePrint (obj,itemid,rowNum) {
			if(prefbarcode == 'Y'){
				var button = obj.id;
				document.getElementById("BarCodeDialog_h").innerText ="Print Barcode";
				document.getElementById("_itemId").value= itemid;
				document.getElementById("Label1").title = document.getElementById("medicine_name"+rowNum).innerText;
				document.getElementById("Label2").title = document.getElementById("category"+rowNum).innerText;
				document.getElementById("Label3").title = document.getElementById("manf_name"+rowNum).innerText; 
				document.getElementById("Label4").title = document.getElementById("package_type"+rowNum).innerText;
				document.getElementById("Label1").innerText = document.getElementById(button).cells.item(1).innerText;
				document.getElementById("Label2").innerText = document.getElementById(button).cells.item(2).innerText;
				document.getElementById("Label3").innerText = document.getElementById(button).cells.item(3).innerText;
				document.getElementById("Label4").innerText = truncateText(document.getElementById("package_type"+rowNum).innerText,30);
				document.getElementById("printNum").value= "1"; 
				
				BarCodeDialog.cfg.setProperty("context", [button, "tr", "br"], false);	
				document.getElementById("toolbarAction_defaultBarCode").href="#";
				document.getElementById("toolbarAction_defaultBarCode").onclick = function onclick(event) {
					
						var str = document.getElementById("divToolBar_default").style.left;
						var len = str.substring(0,str.length-2);
						var ll =895-pxl+130;
						
						 if(len > ll){
							 document.getElementById("BarCodeDialog_c").style.left=ll+"px";
						 }else{
							 document.getElementById("BarCodeDialog_c").style.left=len+"px";
						 }  
						 
						document.getElementById("BarCodeDialog_c").style.top ="406px";
						document.getElementById('BarCodeDialog').style.display='block';
						BarCodeDialog.show(); 
						document.getElementById("printNum").focus();
				}
			}
		}
		
		function init() {
			showFilterActive(document.itemStockLevelSearchForm);
			if(prefbarcode == 'Y'){
				createToolbar(toolbar);
			}
		}

	</script>
</head>
<c:set var="itemStockLevelDetailsList" value="${pagedList.dtoList}"/>
<c:set var="manufacturer">
 <insta:ltext key="storemgmt.itemstockleveldetails.list.manufacturer"/>
</c:set>
<c:set var="itemname">
 <insta:ltext key="storemgmt.itemstockleveldetails.list.itemname"/>
</c:set>
<c:set var="onhand">
 <insta:ltext key="storemgmt.itemstockleveldetails.list.onhand"/>
</c:set>
<c:set var="danger">
 <insta:ltext key="storemgmt.itemstockleveldetails.list.danger"/>
</c:set>
<c:set var="min">
 <insta:ltext key="storemgmt.itemstockleveldetails.list.min"/>
</c:set>
<c:set var="reorder">
 <insta:ltext key="storemgmt.itemstockleveldetails.list.reorder"/>
</c:set>
<c:set var="max">
 <insta:ltext key="storemgmt.itemstockleveldetails.list.max"/>
</c:set>
<c:set var="hasResults" value="${not empty itemStockLevelDetailsList}"/>
<body onload="init(), initBarCodeDialog();">
<h1><insta:ltext key="storemgmt.itemstockleveldetails.list.itemstockleveldetails"/></h1>
<insta:feedback-panel/>
<form name="itemStockLevelSearchForm" method="GET">
	<input type="hidden" name="_method"  id = "_method"value="searchList">
	<input type="hidden" name="_searchMethod" value="searchList"/>

	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

	<insta:search form="itemStockLevelSearchForm" optionsId="optionalFilter" closed="${hasResults}" >
	  <div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
	  	<table class="searchFormTable">
	  			<tr>
					<td>
						<div class="sfLabel"><insta:ltext key="storemgmt.itemstockleveldetails.list.purchase"/></div>
						<div class="sfField">
							<div class="sfFieldSub" style="width: 75px"><insta:ltext key="storemgmt.itemstockleveldetails.list.store"/></div>
							    <c:choose>
								    <c:when test="${(multiStoreAccess eq 'A' || roleId eq 1 || roleId eq 2 )}">
										<insta:userstores username="${userid}" elename="store_id" id="store_id" val="${param.store_id}"/>
									</c:when>
									<c:otherwise>
										<input type="hidden" name="store_id" id="store_id" value="${pharmacyStoreId}" />
										<b><insta:getStoreName store_id="${pharmacyStoreId}"/></b>
									</c:otherwise>
								</c:choose>
								<input type="hidden" name="store_id@type" value="integer">
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="storemgmt.itemstockleveldetails.list.master"/></div>
						<div class="sfField">
							<div class="sfFieldSub" style="width: 75px"><insta:ltext key="storemgmt.itemstockleveldetails.list.manufacturer"/></div>
								<input type="text" name="manf_name" value="${ifn:cleanHtmlAttribute(param.manf_name)}">
								<input type="hidden" name="manf_name@op" value="ilike"/>
						</div>
						<div class="sfField">
						<div class="sfFieldSub" style="width: 75px"><insta:ltext key="storemgmt.itemstockleveldetails.list.category"/></div>
							<insta:selectdb name="med_category_id" values="${paramValues.med_category_id}"
						 		table="store_category_master" displaycol="category" valuecol="category_id"
						 		multiple="multiple" class="listbox"/>
						 		<input type="hidden" name="med_category_id@type" value="integer" />
						</div>
						<div class="sfField">
							<div class="sfFieldSub" style="width: 75px"><insta:ltext key="storemgmt.itemstockleveldetails.list.itemname"/></div>
								<input type="text" name="medicine_name" value="${ifn:cleanHtmlAttribute(param.medicine_name)}">
								<input type="hidden" name="medicine_name@op" value="ilike"/>
						</div>
						<div class="sfField">
							<div class="sfFieldSub" style="width: 75px"><insta:ltext key="storemgmt.itemstockleveldetails.list.generic"/></div>
								<input type="text" name="generic_name" value="${ifn:cleanHtmlAttribute(param.generic_name)}">
								<input type="hidden" name="generic_name@op" value="ilike"/>
						</div>
						<div class="sfField">
							<div class="sfFieldSub" style="width: 75px"><insta:ltext key="storemgmt.itemstockleveldetails.list.itembarcode"/></div>
								<input type="text" name="item_barcode_id" value="${ifn:cleanHtmlAttribute(param.item_barcode_id)}">
						</div>
					</td>
					<td>
						 <div class="sfLabel"><insta:ltext key="storemgmt.itemstockleveldetails.list.qty.avbl.in.brackets"/></div>
						 <div class="sfField">
							<insta:checkgroup name="qty" selValues="${paramValues.qty}"
								opvalues="e,g,l" optexts="Qty = 0,Qty > 0,Qty < 0"/>

						 </div>
						 <div class="sfLabel"><insta:ltext key="storemgmt.itemstockleveldetails.list.searchreorder"/></div>
						 <div class="sfField">
						 	<insta:checkgroup name="qtycond" selValues="${paramValues.qtycond}"
								opvalues="brl,bdl,bml,aml" optexts="Below Reorder Level,Below Danger Level,Below Minimum Level,Above Max Level"/>
						 </div>
					 </td>
				</tr>
		</table>
	  </div>
	</insta:search>

	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
	<div class="resultList">
		<table class="resultList dialog_displayColumns" cellspacing="0" cellpadding="0" id="resultTable"">
			<tr onmouseover="hideToolBar();">
				<th style="text-align:center;">#</th>
				<insta:sortablecolumn name="medicine_name" title="${itemname}"/>
				<th><insta:ltext key="storemgmt.itemstockleveldetails.list.category"/></th>
				<insta:sortablecolumn name="manf_name" title="${manufacturer}"/>
				<th><insta:ltext key="storemgmt.itemstockleveldetails.list.bin.rack"/></th>
				<th><insta:ltext key="storemgmt.itemstockleveldetails.list.genericname"/></th>
				<th><insta:ltext key="storemgmt.itemstockleveldetails.list.unituom"/></th>
				<insta:sortablecolumn name="danger_level" title="${danger}"/>
				<insta:sortablecolumn name="min_level" title="${min}"/>
				<insta:sortablecolumn name="reorder_level" title="${reorder}"/>
				<insta:sortablecolumn name="max_level" title="${max}"/>
				<insta:sortablecolumn name="availableqty" title="${onhand}"/>
				<th><insta:ltext key="storemgmt.storeitembatchdetails.list.transitqty"/></th>
			<c:forEach var="record" items="${itemStockLevelDetailsList}" varStatus="st">
				<c:set var="i" value="${st.index + 1}"/>
				<c:choose>
					<c:when test="${prefbarcode == 'Y'}">
						<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
							onclick="showToolbar(${st.index}, event, 'resultTable',
								{},''),getBarcodePrint(this,'${record.medicine_id}',${st.index});" 
								onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}">
								
								<p id="medicine_name${st.index}" hidden>${record.medicine_name}</p>
							    <p id="category${st.index}" hidden>${record.category}</p>
							    <p id="manf_name${st.index}" hidden>${record.manf_name}</p>
							    <p id="package_type${st.index}" hidden>${record.package_type}</p>
					</c:when>
					<c:otherwise>
						<tr>
					</c:otherwise>
				</c:choose> 
					<td style="text-align:center;">${(pagedList.pageNumber-1) * pagedList.pageSize + i }</td>
					<td style="text-align:left;"><insta:truncLabel value="${record.medicine_name}" length="30"/></td>
					<td style="text-align:left;"><insta:truncLabel value="${record.category}" length="10"/></td>
					<td style="text-align:left;"><insta:truncLabel value="${record.manf_name}" length="10"/></td>
					<td style="text-align:left;">${record.bin}</td>
					<td style="text-align:left;"><insta:truncLabel value="${record.generic_name }" length="10"/></td>
					<td style="text-align:left;">${record.package_uom }</td>
					<td style="text-align:left;">${record.danger_level}</td>
					<td style="text-align:left;">${record.min_level }</td>
					<td style="text-align:left;">${record.reorder_level }</td>
					<td style="text-align:left;">${record.max_level }</td>
					<c:set var="formattedAvailableQty"><fmt:formatNumber type="number" minFractionDigits="${after_decimal_digits}" maxFractionDigits="${after_decimal_digits}" value="${record.availableqty}" /></c:set>
					<td style="text-align:left;">${formattedAvailableQty}</td>
					<td style="text-align:left;">${record.transit}</td>
				</tr>
			</c:forEach>
		</table>

		<c:if test="${param._method == 'searchList'}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>
    </div>
    <div id="BarCodeDialog" style=" display: none;" >
		<div class="bd" >
		    <fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Item Details</legend>
		     <table class="formtable" style="width:100%">
			   <tr> <td class="formlabel" style="width:50px;"> Item Name:   </td><td id="Label1" class="forminfo" ></td></tr>
			   <tr> <td class="formlabel" style="width:50px;"> Category:    </td><td id="Label2" class="forminfo" ></td></tr>
			   <tr> <td class="formlabel" style="width:50px;"> Manufacturer:</td><td id="Label3" class="forminfo" ></td></tr>
			   <tr> <td class="formlabel" style="width:50px;"> Package Type:</td><td id="Label4" class="forminfo" ></td></tr>
		     </table>
		      <br/>
		    </fieldset>
		    <input type="hidden" id="_itemId"/>
			  No. of copies: <input type="text" id="printNum" value="1" style="width:10%" onblur="setValue();" onkeypress="return validation(event);"/><br>
		    <button type="submit" accesskey="P" name="rint" class="button" onclick="return handlePrint();"><b><u>P</u></b>rint</button>
		</div>
    </div>

 </form>
 </div>
</body>
</html>