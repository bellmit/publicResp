<%@page import="com.bob.hms.common.RequestContext"%>
<%@page import="com.insta.hms.stores.PharmacymasterDAO"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ page import="com.insta.hms.stores.StoresDBTablesUtil" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
<title><insta:ltext key="storeprocurement.copypo.podetails.title"/></title>
<meta name="i18nSupport" content="true"/>
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="script" file="date_go.js"/>

<script>
var storeId = '${ifn:cleanJavaScript(store_id)}';
var gRoleId = '${ifn:cleanJavaScript(roleId)}';
var allowBackDate = '${actionRightsMap.allow_backdate}';
var jCenterSuppliers = ${listAllcentersforAPo};
var jAllSuppliers = <%= StoresDBTablesUtil.getTableDataInJSON(StoresDBTablesUtil.GETSUPPLIERS) %>;
var oSupplierAutoComp;
var centerId = '${centerId}';
function validate() {
	if (document.mainform.supplier_name.value == '') {
		showMessage("js.stores.procurement.selectsupplier");
		document.mainform.supplier_name.focus();
		return false;
	}

	if (document.getElementById("po_date").value == "") {
		showMessage("js.stores.procurement.podate.required");
		document.getElementById("po_date").focus();
		return false;
	}

	var valid = doValidateDateField(document.mainform.po_date, 'past');
	if (!valid) {
		document.getElementById("po_date").focus();
		return false;
	}
	return true;
}

function save(){
  if (validate()) {
		document.mainform.submit();
  }
}

function init() {
 	if (gRoleId != 1 && gRoleId != 2) {
 		if (storeId == "") {
			showMessage("js.stores.procurement.noassignedstore.notaccessthisscreen");
 			document.getElementById("storecheck").style.display = 'none';
 		}
 	}
 	
 	if ( ${noactiveitem} ){
 		alert("No active item to copy PO");
 	}

	if (allowBackDate == 'N') {
		document.mainform.po_date.readOnly = true;
	} else {
		document.mainform.po_date.readOnly = false;
	}
	initSupplierAutoComplete();
	
}

function initSupplierAutoComplete() {
	var supplierNames = [];
    var j = 0;
	if(centerId == 0) {
		var dataSource = new YAHOO.widget.DS_JSArray(jAllSuppliers);
	} else {    			
        var dataSource = new YAHOO.widget.DS_JSArray(jCenterSuppliers);
	}
	dataSource.responseSchema = {
		resultsList : "result",
		fields : [  {key : "SUPPLIER_NAME_WITH_CITY"}, {key : "SUPPLIER_CODE"}, {key : "SUPPLIER_NAME"} ]
	};

	oSupplierAutoComp = new YAHOO.widget.AutoComplete(mainform.supplier_name, 'supplier_dropdown', dataSource);
	oSupplierAutoComp.maxResultsDisplayed = 20;
	oSupplierAutoComp.allowBrowserAutocomplete = false;
	oSupplierAutoComp.prehighlightClassName = "yui-ac-prehighlight";
	oSupplierAutoComp.typeAhead = false;
	oSupplierAutoComp.useShadow = false;
	oSupplierAutoComp.minQueryLength = 0;
	oSupplierAutoComp.forceSelection = true;
	oSupplierAutoComp.filterResults = Insta.queryMatchWordStartsWith;
	oSupplierAutoComp.unmatchedItemSelectEvent.subscribe(clearSupplierAddress);

	oSupplierAutoComp.itemSelectEvent.subscribe(onSelectSupplier);
	//setTimeout(function () {oSupplierAutoComp.itemSelectEvent.subscribe(onSelectSupplier)}, 100);
	mainform.supplier_name.focus();
}

function clearSupplierAddress() {
    document.getElementById('suppAddId').textContent = '';
}

function onSelectSupplier(type, args) {
	var suppId = args[2][1];
	var selSupplierName = args[2][2];

    mainform.supplier_name.value = selSupplierName;
	mainform.supplier_id.value = suppId;
	
	loadSupplierDetails();
	deleteAllRows();
}

function loadSupplierDetails() {
	clearSupplierAddress();
    var suppId = mainform.supplier_id.value;
	var supplier = findInList(jAllSuppliers, 'SUPPLIER_CODE', suppId);
	
	if (centerId == 0) {
	    for (var i = 0; i < jAllSuppliers.length; i++) {
	        if (jAllSuppliers[i].STATUS == 'A' && jAllSuppliers[i] != null) {
	        	var supplierAddress = jAllSuppliers[i].SUPPLIER_ADDRESS;
	        	setNodeText(document.getElementById('suppAddId').parentNode, supplierAddress, 75, supplierAddress);
	        }
	    }
	} else {
		 for (var i = 0; i < jCenterSuppliers.length; i++) {
		    if (jCenterSuppliers[i].STATUS == 'A' && jCenterSuppliers[i] != null) {
		        var supplierAddress = jCenterSuppliers[i].SUPPLIER_ADDRESS;
		        setNodeText(document.getElementById('suppAddId').parentNode, supplierAddress, 75, supplierAddress);
		    }
		}
	}
	
	if(supplier != null) {
		var supplierAddress = supplier.SUPPLIER_ADDRESS;
    	setNodeText(document.getElementById('suppAddId').parentNode, supplierAddress, 75, supplierAddress);
    }
	
}

function setValues() {
	if(centerId == 0) {
		loadSelectBox(document.mainform.supplier_id, jAllSuppliers, 'SUPPLIER_NAME', 'SUPPLIER_CODE', '--Select--');
	} else {
		var centerSuppliers = filterList(jCenterSuppliers, "CENTER_ID", centerId);
		loadSelectBox(document.mainform.supplier_id, centerSuppliers, 'SUPPLIER_NAME', 'SUPPLIER_CODE', '--Select--');
	}
}

</script>
<insta:js-bundle prefix="stores.procurement"/>
</head>
<body onload="init();">
<c:set var="dummyvalue">
	<insta:ltext key="selectdb.dummy.value"/>
</c:set>
<div id="storecheck" style="display: block;" >

<h1><insta:ltext key="storeprocurement.copypo.podetails.copypo"/></h1>

<form name="mainform" method="POST" action="poscreen.do">
<input type="hidden" name="_method" value="copyPO">
<input type="hidden" name="po_no" value="${pobean.map.po_no}">

<fieldset class="fieldSetBorder" >
  <legend class="fieldSetLabel"><insta:ltext key="storeprocurement.copypo.podetails.podetails"/></legend>

	<table class="formtable" cellpadding="0" cellspacing="0" border="0" width="100%">
		<tr>
			<td class="formlabel"><span class="prestar">*</span><insta:ltext key="storeprocurement.copypo.podetails.store"/>:</td>
			<c:choose><c:when test="${(multiStoreAccess eq 'A' || roleId eq 1 || roleId eq 2)}">
				<td>
					<insta:userstores username="${userid}" elename="store_id" onlySuperStores="Y"
						val="${pobean.map.store_id}"/>
				</td>
			</c:when><c:otherwise>
				<td>
					<b>${ifn:cleanHtml(store_name)}</b>
					<input type="hidden" name="store_id" value="${ifn:cleanHtmlAttribute(store_id)}" />
				</td>
			</c:otherwise></c:choose>

			<td></td>
			<td></td>

			<td></td>
			<td></td>
		</tr>
		
		<tr>
			<td class="formlabel"><span class="prestar">*</span><insta:ltext key="storeprocurement.copypo.podetails.supplier"/>:</td>
			<td>
<!-- 				<select name="supplier_id" id="supplier_id" class="dropdown"> -->
<!-- 					<option value="">---Select---</option> -->
<%-- 						<c:forEach items="${listAllcentersforAPo}" var="supplier"> --%>
<%-- 							<option value="${supplier.map.supplier_code}">${supplier.map.supplier_name}</option> --%>
<%-- 						</c:forEach> --%>
<!-- 				</select> -->
					<div class="sboFieldInput">
							<div id="supplier_name_wrapper" style="width: 20em;">
								<input type="text" name="supplier_name" id="supplier_name" class="field" value="${supplier.map.supplier_name}"/>
								<div id="supplier_dropdown"></div>
								<input type="hidden" name="supplier_id" value="${supplier.map.supplier_code}"/>
							</div>
					</div>
				
			</td>
			
			<td class="formlabel"><insta:ltext key="storeprocurement.copypo.podetails.supplieraddress"/>:</td>
			<td class="forminfo" colspan="3"><insta:truncLabel id="suppAddId" name="suppAddId" value="${supplier.map.supplier_address}" length="75"/></td>
		</tr>

		<tr>
			<td class="formlabel"><span class="prestar">*</span><insta:ltext key="storeprocurement.copypo.podetails.podate"/>:</td>
			<td><insta:datewidget name="po_date" value="today"/></td>

			<td class="formlabel"><insta:ltext key="storeprocurement.copypo.podetails.deliverydate"/>:</td>
			<td><insta:datewidget name="delivery_date" value=""/></td>

			<td></td>
			<td></td>
		</tr>

		<tr>
			<td class="formlabel"><insta:ltext key="storeprocurement.copypo.podetails.pototalamount"/>:</td>
			<td class="forminfo" colspan="3">${pobean.map.po_total}</td>
			<td></td>
			<td></td>
			<td></td>
			<td></td>
		</tr>

	</table>
</fieldset>

<div class="screenActions">
	<button type="button"  class="button" accesskey="S" ${noactiveitem ? 'disabled' : '' } onclick="return save();"><b><u><insta:ltext key="storeprocurement.copypo.podetails.s"/></u></b><insta:ltext key="storeprocurement.copypo.podetails.ave"/></button>
	<div class="screenActions">
    	<b><insta:ltext key="storeprocurement.polist.list.note"/>:</b> <insta:ltext key="storeprocurement.copypo.podetails.checksupplierratevalue"/>
    </div>
</div>
</form>

</div>
</body>
</html>
