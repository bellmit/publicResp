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
<c:set var="prefbarcode" value="<%= GenericPreferencesDAO.getGenericPreferences().getBarcodeForItem() %>"/>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<head>
	<title><insta:ltext key="storemgmt.storeitembatchdetails.list.title"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	<script>
		var addEditRights = '${urlRightsMap.pharma_view_store_item_batch_details}';
	</script>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="dashboardsearch.js"/>
<script type="text/javascript">
	    var itemsList = <%=request.getAttribute("itemsList")%> ;
		var prefbarcode = '${prefbarcode}';
		var toolbar = {}
		 toolbar.Edit={
			    title: "Edit Item Batch Details",
				imageSrc: "icons/Edit.png",
				href: 'stores/StoreItemBatchDetails.do?_method=show',
				onclick: null,
				description: "Edit Item Batch Details",
				show : (!empty(addEditRights) && addEditRights == 'A') 
				
				
			};
		
		 toolbar.BarCode= {
			
			title: "Print Barcode",
			imageSrc: "icons/Report.png",
		    href: '#',
			target: '',
			onclick: null, 
			description: "Print Barcode",
			show: ${prefbarcode == 'Y'}
		};
		

		function init() {
			showFilterActive(document.stockItemBatchSearchForm);
			if((!empty(addEditRights) && addEditRights == 'A') || prefbarcode == 'Y')
				createToolbar(toolbar);
		}
		var pxl = 400;
		function initBarCodeDialog(){
			BarCodeDialog = new YAHOO.widget.Dialog("BarCodeDialog",
					{
					width:pxl+"px",
						
					context :["BarCodeDialog", "tr", "br"],
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
		
		function getBarcodePrint (obj,itemid,rowNum,item_batch_id) {
				if(prefbarcode == 'Y'){
					var button = obj.id;
					document.getElementById("BarCodeDialog_h").innerText ="Print Barcode";
					document.getElementById("_itemId").value= itemid;
					document.getElementById("_item_batch_id").value = item_batch_id;
					document.getElementById("Label1").title = document.getElementById("medicine_name"+rowNum).innerText;
					document.getElementById("Label2").title = document.getElementById("category"+rowNum).innerText;
					document.getElementById("Label3").title = document.getElementById("manf_name"+rowNum).innerText; 
					document.getElementById("Label4").title = document.getElementById("package_type"+rowNum).innerText;
					document.getElementById("Label1").innerText = document.getElementById(button).cells.item(3).innerText;
					document.getElementById("Label2").innerText = document.getElementById(button).cells.item(1).innerText;
					document.getElementById("Label3").innerText = document.getElementById(button).cells.item(2).innerText;
					document.getElementById("Label4").innerText = truncateText(document.getElementById("package_type"+rowNum).innerText,30);
					document.getElementById("printNum").value= "1";
					
					document.getElementById("toolbarAction_defaultBarCode").href="#";
					document.getElementById("toolbarAction_defaultBarCode").onclick = function onclick(event) {
						
							BarCodeDialog.cfg.setProperty("context", [button, "tr", "br"], false);	
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
				    var item_batch_id = document.getElementById("_item_batch_id").value;
				    if(noOfPrints <= 1000){
				    	   document.getElementById('BarCodeDialog').style.display='none';
						   BarCodeDialog.cancel();
						   window.open("${cpath}/stores/StoreItemBatchDetailsList.do?_method=insertBarCode&barcodeType=ItemMaster&itemId="+med+"&noOfPrints="+noOfPrints+"&item_batch_id="+item_batch_id);
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

</script>


</head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="storsItemBatchDetailsList" value="${pagedList.dtoList}" />
<c:set var="prefStockEntryStatus" value="${stock_entry_agnst_do}" />
<c:set var="hasResults" value="${not empty storsItemBatchDetailsList}"/>
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
<body onload="init(),initBarCodeDialog(); ">
<c:set var="manufacturer">
 <insta:ltext key="storemgmt.storeitembatchdetails.list.manufacturer"/>
</c:set>
<c:set var="itemname">
 <insta:ltext key="storemgmt.storeitembatchdetails.list.itemname"/>
</c:set>
<c:set var="batchno">
 <insta:ltext key="storemgmt.storeitembatchdetails.list.batch.slno"/>
</c:set>
<h1><insta:ltext key="storemgmt.storeitembatchdetails.list.storeitembatchdetails"/></h1>
<insta:feedback-panel/>
<form name="stockItemBatchSearchForm" method="GET">
	<input type="hidden" name="_method"  id = "_method"value="searchList">
	<input type="hidden" name="_searchMethod" value="searchList"/>

	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

	<insta:search form="stockItemBatchSearchForm" optionsId="optionalFilter" closed="${hasResults}" >
	  <div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
	  	<table class="searchFormTable">
	  			<tr>
					<td>
						<div class="sfLabel"><insta:ltext key="storemgmt.storeitembatchdetails.list.purchase"/></div>


						<div class="sfField">
							<div class="sfFieldSub" style="width: 65px"><insta:ltext key="storemgmt.storeitembatchdetails.list.supplier"/></div>
								<div id="supplier_wrapper" style="width: 15em;">
									<input type="text" name="supplier_name" id="supplier_name" style="width: 11.5em" value="${ifn:cleanHtmlAttribute(param.supplier_name)}"/>
									<div id="suppliername_dropdown" class="scrolForContainer"></div>
								</div>
								<input type="hidden" name="supplier_name@op" value="~">
						</div>
						<br/><br/>
						<div class="sfField">
							<div class="sfFieldSub" style="width: 65px"><insta:ltext key="storemgmt.storeitembatchdetails.list.store"/></div>
							    <c:choose>
							    <c:when test="${(multiStoreAccess eq 'A' || roleId eq 1 || roleId eq 2 )}">
								<insta:userstores username="${userid}" elename="dept_id" id="dept_id" val="${param.dept_id}"/>
								</c:when>
								<c:otherwise>
								<input type="hidden" name="dept_id" id="dept_id" value="${pharmacyStoreId}" />
								<b><insta:getStoreName store_id="${pharmacyStoreId}"/></b>
								</c:otherwise>
								</c:choose>
								<input type="hidden" name="dept_id@type" value="integer">
						   </div>

						<div class="sfField">
							<div class="sfFieldSub" style="width: 65px"><insta:ltext key="storemgmt.storeitembatchdetails.list.grn"/></div>
								<input type="text" name="grn_no" value="${ifn:cleanHtmlAttribute(param.grn_no)}">
								<input type="hidden" name="grn_no@op" value="~">
						</div>
						<div class="sfField">
							<div class="sfFieldSub" style="width: 65px">
							<c:choose>
								<c:when test="${doSchemaAllowStatus}"><insta:ltext key="storemgmt.storeitembatchdetails.list.do"/></c:when>
								<c:otherwise><insta:ltext key="storemgmt.storeitembatchdetails.list.invoice"/></c:otherwise>
							</c:choose>
							</div>
								<input type="text" name="invoice_no" value="${ifn:cleanHtmlAttribute(param.invoice_no)}">
								<input type="hidden" name="invoice_no@op" value="ilike"/>
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="storemgmt.storeitembatchdetails.list.master"/></div>
						<div class="sfField">
							<div class="sfFieldSub" style="width: 75px"><insta:ltext key="storemgmt.storeitembatchdetails.list.manufacturer"/></div>
								<input type="text" name="manf_name" value="${ifn:cleanHtmlAttribute(param.manf_mnemonic)}">
								<input type="hidden" name="manf_name@op" value="ilike"/>
						</div>
						<div class="sfField">
						<div class="sfFieldSub" style="width: 75px"><insta:ltext key="storemgmt.storeitembatchdetails.list.category"/></div>
							<insta:selectdb name="med_category_id" values="${paramValues.category_id}"
						 		table="store_category_master" displaycol="category" valuecol="category_id"
						 		multiple="multiple" class="listbox"/>
						 		<input type="hidden" name="med_category_id@type" value="integer" />
						</div>
						<div class="sfField">
							<div class="sfFieldSub" style="width: 75px"><insta:ltext key="storemgmt.storeitembatchdetails.list.itemname"/></div>
								<input type="text" name="medicine_name" value="${ifn:cleanHtmlAttribute(param.medicine_name)}">
								<input type="hidden" name="medicine_name@op" value="ilike"/>
						</div>
						<div class="sfField">
							<div class="sfFieldSub" style="width: 75px"><insta:ltext key="storemgmt.storeitembatchdetails.list.generic"/></div>
								<input type="text" name="generic_name" value="${ifn:cleanHtmlAttribute(param.generic_name)}">
								<input type="hidden" name="generic_name@op" value="ilike"/>
						</div>
						<div class="sfField">
							<div class="sfFieldSub" style="width: 75px"><insta:ltext key="storemgmt.storeitembatchdetails.list.itembarcode"/></div>
								<input type="text" name="item_barcode_id" value="${ifn:cleanHtmlAttribute(param.item_barcode_id)}">
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="storemgmt.storeitembatchdetails.list.stock"/></div>
						 <div class="sfField">
							<div class="sfFieldSub" style="width: 60px"><insta:ltext key="storemgmt.storeitembatchdetails.list.batchno"/></div>
								<input type="text" name="batch_no" value="${ifn:cleanHtmlAttribute(param.batch_no)}">
								<input type="hidden" name="batch_no@op" value="ilike"/>
						 </div>
						 <div class="sfLabel"><insta:ltext key="storemgmt.storeitembatchdetails.list.qty.avbl.in.brackets"/></div>
						 <div class="sfField">
							<insta:checkgroup name="qtycond" selValues="${paramValues.qtycond}"
								opvalues="e,g,l" optexts="Qty = 0,Qty > 0,Qty < 0"/>

						 </div>
						 <div class="sfLabel"><insta:ltext key="storemgmt.storeitembatchdetails.list.expiry"/></div>
						<div class="sfField">
							<div class="sfFieldSub" style="width: 60px"><insta:ltext key="storemgmt.storeitembatchdetails.list.from"/></div>
							<insta:datewidget name="exp_dt" id="exp_dt0" value="${paramValues.exp_dt[0]}"/>
						</div>
						<div class="sfField">
							<div class="sfFieldSub" style="width: 60px"><insta:ltext key="storemgmt.storeitembatchdetails.list.to"/></div>
							<insta:datewidget name="exp_dt" id="exp_dt1" value="${paramValues.exp_dt[1]}"/>
							<input type="hidden" name="exp_dt@type" value="date"/>
							<input type="hidden" name="exp_dt@op" value="ge,le"/>
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
				<th>#</th>
				<th><insta:ltext key="storemgmt.storeitembatchdetails.list.category"/></th>
				<insta:sortablecolumn name="manf_name" title="${manufacturer}"/>
				<insta:sortablecolumn name="medicine_name" title="${itemname}"/>
				<th><insta:ltext key="storemgmt.storeitembatchdetails.list.bin.rack"/></th>
				<th><insta:ltext key="storemgmt.storeitembatchdetails.list.genericname"/></th>
				<th><insta:ltext key="storemgmt.storeitembatchdetails.list.unituom"/></th>
				<th><insta:ltext key="storemgmt.storeitembatchdetails.list.grnno"/></th>
				<c:choose>
					<c:when test="${doSchemaAllowStatus}"><th><insta:ltext key="storemgmt.storeitembatchdetails.list.dono"/></th></c:when>
					<c:otherwise><th><insta:ltext key="storemgmt.storeitembatchdetails.list.invoiceno"/></th></c:otherwise>
				</c:choose>
				<th><insta:ltext key="storemgmt.storeitembatchdetails.list.expiry"/></th>
				<insta:sortablecolumn name="batch_no" title="${batchno}"/>
				<c:if test="${!doAllowStatus}">
					<th><insta:ltext key="storemgmt.storeitembatchdetails.list.mrp.pkg.in.brackets"/></th>
				</c:if>
				<th><insta:ltext key="storemgmt.storeitembatchdetails.list.qty.units.in.brackets"/></th>
				<th><insta:ltext key="storemgmt.storeitembatchdetails.list.transitqty"/></th>
			</tr>
			<c:forEach var="record" items="${storsItemBatchDetailsList}" varStatus="st">
				<c:set var="i" value="${st.index + 1}"/>
				<c:choose>
						<c:when test="${(not empty urlRightsMap.store_stock_consumption_new && urlRightsMap.pharma_view_store_item_batch_details == 'A') || prefbarcode == 'Y'}">
						<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
							onclick="showToolbar(${st.index}, event, 'resultTable',
								{item_batch_id:'${record.item_batch_id }',dept_id:'${record.dept_id }',itemId:'${record.medicine_id}'},''),getBarcodePrint(this,'${record.medicine_id}',${st.index},'${record.item_batch_id}');"
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
					<td>${(pagedList.pageNumber-1) * pagedList.pageSize + i }</td>
					<td><insta:truncLabel value="${record.category}" length="30"/></td>
					<td><insta:truncLabel value="${record.manf_name}" length="10"/></td>
					<td><insta:truncLabel value="${record.medicine_name}" length="20"/></td>
					<td>${record.bin }</td>
					<td>${record.generic_name }</td>
					<td>${record.issue_units }</td>
					<td>${record.grn_no }</td>
					<td>${record.invoice_no }</td>
					<td width="8%"><fmt:formatDate value="${record.exp_dt}" pattern="MM"/>-<fmt:formatDate value="${record.exp_dt}" pattern="yy"/></td>
					<td><insta:truncLabel value="${record.batch_no}" length="10"/></td>
					<c:if test="${!doAllowStatus}">
						<td width="8%">${record.mrp}</td>
					</c:if>
					<td>${record.qty }</td>
					<td>${record.transit_qty }</td>
				</tr>
			</c:forEach>
		</table>

		<c:if test="${param._method == 'searchList'}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>
    </div>
	<div id="BarCodeDialog" style=" display: none;">
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
		    <input type="hidden" id="_item_batch_id"/>
			  No. of copies: <input type="text" id="printNum" value="1" style="width:10%" onblur="setValue();" onkeypress="return validation(event);"/><br>
		    <button  type="submit" accesskey="P" name="rint" class="button" onclick="return handlePrint();"><b><u>P</u></b>rint</button>
		</div>
    </div>

	</form>
 </div>
</body>
</html>