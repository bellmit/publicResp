<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-html.tld"  prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld"  prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<html>

<head>
	<title><insta:ltext key="storemgmt.stockapproval.list.title"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	<insta:js-bundle prefix="stores.mgmt"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="dashboardsearch.js"/>
	<insta:link type="js" file="ajax.js"/>
	<jsp:useBean id="issueTypeDisplay" class="java.util.HashMap"/>
	<c:set target="${issueTypeDisplay}" property="P" value="Permanent"/>
	<c:set target="${issueTypeDisplay}" property="C" value="Consumable"/>
	<c:set target="${issueTypeDisplay}" property="L" value="Reusable"/>
	<c:set target="${issueTypeDisplay}" property="R" value="Retailable"/>
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>
	<c:set var="stockList" value="${pagedList.dtoList}"/>
	<c:set var="hasResults" value="${not empty stockList}"/>
	<c:set var="taxLabel" value="${genPrefs.procurement_tax_label}" scope="request"/>
	<style>
		#table2 td {
			padding: 0px 10px 10px 10px;
			font-weight: bold;
		}
	</style>
	<script>
	var toolbarOptions = getToolbarBundle("js.stores.mgmt.toolbar");
	var deptId = '${ifn:cleanJavaScript(dept_id)}';
	var gRoleId = '${roleId}';

    function selectAllItems(){
		var form = document.invStockAprvlForm;
		var checkStatus =   form._AllItems.checked

	 	//disable or enable per page checkboxes
		form._All.disabled = checkStatus;
		form._All.checked = false;
		var length = form._approve.length;
		if(length == undefined){
		 		form._approve.checked  = false;
				form._approve.disabled = checkStatus;
				if (checkStatus) form._happrove.value = 'true';
				else form._happrove.value = 'false';
				if (form._originalAsset.value == 'Y') {
					form._approve.checked = true;
					form._approve.disabled = true;
				}
		}else{
			var count = 0;
			for(var i=0;i<length;i++){
				form._approve[i].checked = false;
				form._approve[i].disabled =checkStatus
				if (checkStatus) form._happrove[i].value = 'true';
				else form._happrove[i].value = 'false';
				if (form._originalAsset[i].value == 'Y') {
					form._approve[i].checked = true;
					form._approve[i].disabled = true;
			    }
		   }
       }
    }
    function selectAll(){
		var form = document.invStockAprvlForm;
		var Allobj = document.forms[0]._All.checked;
		var length = document.forms[0]._approve.length;
		if(length == undefined){
			document.forms[0]._approve.checked = Allobj;
			if (Allobj) document.forms[0]._happrove[i].value = 'true';
			else document.forms[0]._happrove[i].value = 'false';
			if (form._originalAsset.value == 'Y') {
				form._approve.checked = true;
				form._approve.disabled = true;
			}

		}else{
			var count = 0;
			for(var i=0;i<length;i++){
				document.forms[0]._approve[i].checked =Allobj
				if (Allobj) document.forms[0]._happrove[i].value = 'true';
				else document.forms[0]._happrove[i].value = 'false';
				if (document.forms[0]._originalAsset[i].value == 'Y') {
					document.forms[0]._approve[i].checked = true;
					document.forms[0]._approve[i].disabled = true;
			    } else count = count + 1;
			}
			if (count == 0){
				showMessage("js.stores.mgmt.noitemsapprove.thisscreen");
				document.forms[0]._All.checked = false;
				return false;
			}
		}
    }
    function ValidateGropUpdate(){
    	var form = document.forms[0];
   		form._method.value = "groupUpdate";
		form.submit();
    }
    function chkVali(index) {
	    if (document.getElementById('_approve'+index).checked) document.getElementById('_happrove'+index).value = 'true';
	    else document.getElementById('_happrove'+index).value = 'false';
    }
    var theForm = document.invStockAprvlForm;

	function init() {
		theForm = document.invStockAprvlForm;
		theForm.dept_id.focus();
		createToolbar(toolbar);
		initDialog();
	}

	function checkstoreallocation() {
	 	if(gRoleId != 1 && gRoleId != 2 ) {
	 		if(deptId == "") {
	 		showMessage("js.stores.mgmt.noassignedstore.notaccessthisscreen");
	 		document.getElementById("storecheck").style.display = 'none';
	 		}
	 	}
	}

	var cfDialog = null;
	function initDialog() {
		var dialog = document.getElementById('existingstock');
		dialog.style.display = 'block';
		cfDialog = new YAHOO.widget.Dialog("existingstock", {
					width:"870px",
					context : ["", "tr", "br"],
					visible: false,
					modal: true,
					constraintoviewport: true
		});
		subscribeKeyListeners(cfDialog);
		cfDialog.render();
	}

	function showCustomDialog(obj) {
		var row = getThisRow(obj);
		cfDialog.cfg.setProperty("context", [obj, "tr", "tl"], false);
		cfDialog.show();
		return false;
	}

	function subscribeKeyListeners(dialog) {
		var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
				{ fn:dialog.hide, scope:dialog, correctScope:true } );
		dialog.cfg.setProperty("keylisteners", [escKeyListener]);
	}


	function openDialog(anchor, params, id, toolbar) {
		var itemId ='';
		var storeId='';
		var identifier='';
		var stock='';
		var item_name='';
		for (var paramname in params) {
			var paramvalue = params[paramname]
			if (paramname == 'itemId')
				itemId = paramvalue;
			if (paramname == 'storeId')
				storeId = paramvalue;
			if (paramname == 'identifier')
				identifier = paramvalue;
			if (paramname == 'stock')
				stock = paramvalue;
		}
		Ajax.get('./StoresAssetApprovalPopup.do?_method=getItemDetails&itemId='+itemId+'&storeId='+deptId+'&identifier='+identifier+'&stock='+stock,
			function(data, status) {
				var text = eval(data);
				var pd_table = document.getElementById("table1");
				removeRows();
				for(var i=0;i<text.length;i++) {
				item_name=text[i].medicine_name;
				var numRows = pd_table.rows.length;
				var id = numRows ;
				var row = pd_table.insertRow(id);
				var cell;
				cell = row.insertCell(-1);
				cell.innerHTML=text[i].supplier_name;
				cell.setAttribute('style','white-space:normal');
				cell = row.insertCell(-1);
				cell.innerHTML=text[i].cost_price;
				cell = row.insertCell(-1);
				cell.innerHTML=text[i].mrp;
				cell = row.insertCell(-1);
				cell.innerHTML=text[i].tax_rate;
				cell = row.insertCell(-1);
				cell.innerHTML=text[i].invoice_no;
				cell = row.insertCell(-1);
				cell.innerHTML=text[i].invoice_date;
				cell = row.insertCell(-1);
				cell.innerHTML=text[i].grn_no;
				cell = row.insertCell(-1);
				cell.innerHTML=text[i].grn_date;
				cell = row.insertCell(-1);
				cell.innerHTML=text[i].po_no;
			 }
			 var item_details_table = document.getElementById("table2");
			 var numRows = item_details_table.rows.length;
			 if(numRows == 1)
			   item_details_table.deleteRow(-1);
			 numRows = item_details_table.rows.length;
			 var id = numRows ;
			 var row = item_details_table.insertRow(id);
			 var cell;
			 cell = row.insertCell(-1);
			 cell.innerHTML=getString("js.stores.mgmt.stocktransfer.addshow.itemname")+":";
			 cell = row.insertCell(-1);
			 cell.innerHTML=item_name;
			 cell = row.insertCell(-1);
			 cell.innerHTML=getString("js.stores.mgmt.stocktransfer.addshow.batch.serialno")+":";
			 cell = row.insertCell(-1);
			 cell.innerHTML=identifier;
			 cell = row.insertCell(-1);
			 cell.innerHTML=getString("js.stores.mgmt.stocktransfer.addshow.stocktype")+" :";
			 cell = row.insertCell(-1);
			 if(stock == 'true')
			 cell.innerHTML=getString("js.stores.mgmt.stocktransfer.addshow.consignment");
			 else
			 cell.innerHTML=getString("js.stores.mgmt.stocktransfer.addshow.normal");
			}
			);

		showCustomDialog(anchor);
	}

	function removeRows(){
			var innerTableObj = document.getElementById("table1");
			var length = innerTableObj.rows.length ;
			for (var i=1; i<length; i++) {
				innerTableObj.deleteRow(1);
			}
	}

		var toolbar = {}
		 	toolbar.Edit = {
		 		title : toolbarOptions["phdetails"]["name"],
		 		imageSrc : "icons/View.png",
				onclick: 'openDialog',
				description: toolbarOptions["phdetails"]["description"]
		};

	</script>
	<insta:js-bundle prefix="stores.mgmt.stocktransfer.addshow"/>
</head>
<body onload="init(); showFilterActive(document.invStockAprvlForm); checkstoreallocation();">
<c:set var="approvalstatus">
 <insta:ltext key="storemgmt.stockapproval.list.approved"/>,
<insta:ltext key="storemgmt.stockapproval.list.unapproved"/>
</c:set>
<c:set var="consignmentstatus">
 <insta:ltext key="storemgmt.stockapproval.list.normalstock"/>,
<insta:ltext key="storemgmt.stockapproval.list.consignmentstock"/>
</c:set>
<c:set var="qystatus">
 <insta:ltext key="storemgmt.stockapproval.list.qty1"/>,
<insta:ltext key="storemgmt.stockapproval.list.qty2"/>,
<insta:ltext key="storemgmt.stockapproval.list.qty3"/>
</c:set>
<c:set var="medicinename">
 <insta:ltext key="storemgmt.stockapproval.list.itemname"/>
</c:set>
<c:set var="batchno">
 <insta:ltext key="storemgmt.stockapproval.list.batch.serialnumber"/>
</c:set>
<div id="storecheck" style="display: block;" >
<h1><insta:ltext key="storemgmt.stockapproval.list.stockapproval"/></h1>
<insta:feedback-panel/>
<form name="invStockAprvlForm" action="StoresAssetApproval.do" method="GET">
	<input type="hidden" name="_method" value="list">
	<input type="hidden" name="_searchMethod" value="list"/>
	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

<insta:search form="invStockAprvlForm" optionsId="optionalFilter" closed="${hasResults}" >
	  <div class="searchBasicOpts" >
	  	<div class="sboField">
			<div class="sboFieldLabel"><insta:ltext key="storemgmt.stockapproval.list.store"/></div>
				<c:choose>
					<c:when test="${(multiStoreAccess eq 'A' || roleId eq 1 || roleId eq 2 )}">
						<div class="sboFieldInput">
							<insta:userstores username="${userid}" elename="dept_id" id="dept_id" val="${dept_id}"/>
						</div>
					</c:when>
					<c:otherwise>
						<b><insta:getStoreName store_id="${pharmacyStoreId}"/></b>
						<input type="hidden" name="dept_id" id="dept_id" value="${pharmacyStoreId}">
					</c:otherwise>
				</c:choose>
				<input type="hidden" name="dept_id@type"  value="integer">
				<input type="hidden" name="dept_id@cast" value="y">
	    </div>
	    <div class="sboField">
			<div class="sboFieldLabel"><insta:ltext key="storemgmt.stockapproval.list.category"/></div>
				<div class="sboFieldInput">
					<insta:selectdb  name="med_category_id" value="${param.med_category_id}" table="store_category_master" valuecol="category_id" displaycol="category" dummyvalue="(ALL)"
						orderby="category"></insta:selectdb>
				</div>
				<input type="hidden" name="med_category_id@type"  value="integer">
	    </div>
	  </div>
	  <div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
	  	<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel"><insta:ltext key="storemgmt.stockapproval.list.item"/></div>
						<div class="sfField">
								<input type="text" name="medicine_name" value="${ifn:cleanHtmlAttribute(param.medicine_name)}">
								<input type="hidden" name="medicine_name@op" value="ilike"/>
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="storemgmt.stockapproval.list.batch.serialnumber"/></div>
						<div class="sfField">
								<input type="text" name="batch_no" value="${ifn:cleanHtmlAttribute(param.batch_no)}">
								<input type="hidden" name="batch_no@op" value="ilike"/>
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="storemgmt.stockapproval.list.stocktype"/></div>
						<div class="sfField">
								<insta:checkgroup name="consignment_stock" selValues="${paramValues.consignment_stock}"
								opvalues="false,true" optexts="${consignmentstatus}"/>
								<input type="hidden" name="consignment_stock@op" value="boolean"/>
								<input type="hidden" name="consignment_stock@cast" value="y"/>
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="storemgmt.stockapproval.list.asset"/></div>
						<div class="sfField">
								<insta:checkgroup name="asset_approved" selValues="${paramValues.asset_approved}"
								opvalues="Y,N" optexts="${approvalstatus}"/>
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="storemgmt.stockapproval.list.qty.avbl.in.brackets"/></div>
						<div class="sfField">
								<insta:checkgroup name="qtycond" selValues="${paramValues.qtycond}"
								opvalues="e,g,l" optexts="Qty = 0,Qty > 0,Qty < 0"/>
						</div>
					</td>

				</tr>
		</table>

</insta:search>

<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" sectionSize="15" totalRecords="${pagedList.totalRecords}"/>

	<div class="resultList">
		<table class="resultList" cellspacing="0" cellpadding="0"  id="resultTable">
			<tr>
				<th width="5%"><insta:ltext key="storemgmt.stockapproval.list.approve"/></th>
				<insta:sortablecolumn name="medicine_name" title="${medicinename}"/>
				<insta:sortablecolumn name="batch_no" title="${batchno}"/>
				<th><insta:ltext key="storemgmt.stockapproval.list.category"/></th>
				<th><insta:ltext key="storemgmt.stockapproval.list.issuetype"/></th>
				<th width="8%"><insta:ltext key="storemgmt.stockapproval.list.expiry.mm.yy"/></th>
				<th width="8%"><insta:ltext key="storemgmt.stockapproval.list.sp.pkg.in.brackets"/></th>
				<th width="8%"><insta:ltext key="storemgmt.stockapproval.list.qty.avbl.in.brackets"/></th>
				<th width="8%"><insta:ltext key="storemgmt.stockapproval.list.qty.notavbl.in.brackets"/></th>
			</tr>
			<c:forEach var="stock" items="${stockList}" varStatus="st">
			<c:set var="i" value="${st.index + 1}"/>
			<c:set var="flagColor">
				<c:choose>
					<c:when test="${stock.asset_approved =='N' && stock.consignment_stock == true}">blue</c:when>
					<c:when test="${stock.asset_approved =='Y' && stock.consignment_stock == true}">grey</c:when>
					<c:when test="${stock.asset_approved =='N' && stock.consignment_stock == false}">yellow</c:when>
					<c:otherwise>empty</c:otherwise>
				</c:choose>
			</c:set>
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{itemId:'${stock.medicine_id }',storeId:'${stock.dept_id }',
						identifier:'${stock.batch_no }',stock:'${stock.consignment_stock}'});"
					onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>
					<c:choose>
						<c:when test="${stock.asset_approved =='Y'}">
							<td width="5%">
								<input type="checkbox" name="_approve" id="_approve${i }" value="" checked="checked" disabled="disabled" onclick="chkVali(${i });"/>
								<input type="hidden" name="_happrove" id="_happrove${i }" value="true">
								<input type="hidden" name="_itemId" id="_itemId${i }" value="${stock.medicine_id }">
								<input type="hidden" name="_identifier" id="_identifier${i }" value="${stock.batch_no}">
							</td>
						</c:when>
						<c:otherwise>
							<td width="5%">
								<input type="checkbox" name="_approve" id="_approve${i }" value="" onclick="chkVali(${i });"/>
								<input type="hidden" name="_happrove" id="_happrove${i }" value="false">
								<input type="hidden" name="_itemId" id="_itemId${i }" value="${stock.medicine_id }">
								<input type="hidden" name="_identifier" id="_identifier${i }" value="${stock.batch_no}">
								<input type="hidden" name="_dept_id" id="_dept_id${i }" value="${stock.dept_id}">
							</td>
						</c:otherwise>
					</c:choose>
						<td><img class="flag" src="${cpath}/images/${flagColor}_flag.gif"/>${stock.medicine_name}<input type="hidden" name="_originalAsset" id="_originalAsset${i }" value="${stock.asset_approved}"></td>
						<td>${stock.batch_no}</td>
						<td>${stock.category_name}</td>
						<td>${issueTypeDisplay[stock.issue_type]}</td>
						<td width="8%"><fmt:formatDate value="${stock.exp_dt}" pattern="MM"/>-<fmt:formatDate value="${stock.exp_dt}" pattern="yy"/></td>
						<c:choose>
					       <c:when test="${stock.pkg_size gt 1}">
					       <td width="8%">${stock.mrp}/${stock.pkg_size}</td>
					       </c:when>
					       <c:otherwise>
					       <td width="8%">${stock.mrp}</td>
					       </c:otherwise>
					    </c:choose>
						<td align="right">${stock.qty}</td>
						<td align="right">${stock.qty_not_avbl}</td>
					</tr>
			</c:forEach>
		</table>
	<c:if test="${param._method == 'list'}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>

    </div>
     <div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
		<div class="flag"><img src='${cpath}/images/empty_flag.gif'></div>
		<div class="flagText"><insta:ltext key="storemgmt.stockapproval.list.normalstockapproveditems"/></div>
		<div class="flag"><img src='${cpath}/images/yellow_flag.gif'></div>
		<div class="flagText"><insta:ltext key="storemgmt.stockapproval.list.normalstocknotapproveditems"/></div>
		<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
		<div class="flagText"><insta:ltext key="storemgmt.stockapproval.list.consignmentstockapproveditems"/></div>
		<div class="flag"><img src='${cpath}/images/blue_flag.gif'></div>
		<div class="flagText"><insta:ltext key="storemgmt.stockapproval.list.consignmentstocknotapproveditems"/></div>

	</div>
	<c:if test="${not empty stockList}">
		<table class="dashboard">
		<tbody><tr>
					<th colspan="2"><insta:ltext key="storemgmt.stockapproval.list.assetapprove.selectall"/> ${pagedList.pageNumber}
						 <input type="checkbox" onchange="selectAll()" name="_All"/>
						    

						 <insta:ltext key="storemgmt.stockapproval.list.selectall"/> ${pagedList.totalRecords } Items:<input type="checkbox" value="UpdateAllItems" onchange="selectAllItems()" name="_AllItems"/>
					</th>
				</tr>
			</tbody>
		</table>
		<div class="screenActions">
			<button type="button" name="save" accesskey="A"
				onclick="ValidateGropUpdate()"><b><u><insta:ltext key="storemgmt.stockapproval.list.a"/></u></b><insta:ltext key="storemgmt.stockapproval.list.pprove"/></button>
		</div>
	</c:if>

</form>
</div>
<div id="existingstock" style="visibility:hidden">
	<div class="bd">
		<table id="table2" cellpadding="5">
		</table>
		<fieldset class="fieldSetBorder" >
			<legend class="fieldSetLabel"><insta:ltext key="storemgmt.stockapproval.list.itempurchasedetails"/></legend>
			<table class="dashboard" width="100%" cellspacing="0" cellpadding="0" id="table1">
				<tr >
		        <th ><insta:ltext key="storemgmt.stockapproval.list.supplier"/></th>
		        <th ><insta:ltext key="storemgmt.stockapproval.list.costprice"/></th>
		        <th ><insta:ltext key="storemgmt.stockapproval.list.mrp"/></th>
		        <th><insta:ltext key="storemgmt.stockapproval.list.vatrate.${taxLabel}"/></th>
		        <th ><insta:ltext key="storemgmt.stockapproval.list.invoicenumber"/></th>
		        <th ><insta:ltext key="storemgmt.stockapproval.list.invoicedate"/></th>
		        <th ><insta:ltext key="storemgmt.stockapproval.list.grnnumber"/></th>
		        <th ><insta:ltext key="storemgmt.stockapproval.list.grndate"/></th>
		        <th ><insta:ltext key="storemgmt.stockapproval.list.ponumber"/></th>
		        </tr>
			</table>
			</div>
			</fieldset>
		</div>
</div>
</body>
</html>





