/*
 * Javascript functions for use with list.jsp for Stock Ledger
 */

	var oAutoComp;
	var oAutoComp1;

	function init() {

		if ( !document.getElementById("checkpoints").checked) {
			onChecked('notcheckpoints');
		}
		getMedicinesForStore(initMedicineAutoComplete);
	}

	function onChecked(val){

	  var sel = document.forms[0].fromCheckPt;
	  if(val=='checkpoints'){
	   	loadSelectBox(sel, checkPoints, 'checkpoint_name',
					'checkpoint_id', null, null);
	  }else{
	     sel.length=0;
	     insertIntoSelectBox(sel, 1, "..(None)..", "");
	  }
	  sel.selectedIndex = 0;

	  sel = document.forms[0].toCheckPt;
	  if(val=='checkpoints'){
	   	loadSelectBox(sel, checkPoints, 'checkpoint_name',
					'checkpoint_id', null, null);
		insertIntoSelectBox(sel, 1, "..(Now)..","-1");
	  }else{
	     sel.length=0;
	     insertIntoSelectBox(sel, 1, "..(None)..", "");
	  }
	  sel.selectedIndex = 0;

	  if (!document.getElementById("dates").checked) {
	  	document.forms[0].fromDate.value = "";
	  	document.forms[0].toDate.value = "";
	  }
	}

	function doSearch () {
		document.getElementById("export_type").value = '';
		if (document.getElementById("dates").checked) {
			if (document.forms[0].fromDate.value == "" ) {
				showMessage("js.stores.mgmt.selectfromdate");
				document.forms[0].fromDate.focus();
				return false;
			}
	  		if (document.forms[0].toDate.value == "" ) {
	  			showMessage("js.stores.mgmt.selecttodate");
	  			document.forms[0].toDate.focus();
	  			return false;
	  		}
		}
	  if (document.getElementById("medicineName").value == "" ) {
	  		showMessage("js.stores.mgmt.selecttheitem");
	  		return false;
	  }
	}

	function onChangeStore() {
		var storeId = document.forms[0].store_id.value;
		document.stockLedger.storeName.value = document.forms[0].store_id.options[document.forms[0].store_id.selectedIndex].text;
		getMedicinesForStore(initMedicineAutoComplete);
		//initMedicineAutoComplete();
	}

	function initMedicineAutoComplete() {

		if (oAutoComp != undefined) {
			oAutoComp.destroy();
		}
		var itemNamesArray = jMedicineNames;
		dataSource = new YAHOO.widget.DS_JSArray(itemNamesArray);
		 	dataSource.responseSchema = {
			resultsList : "result",
			fields : [  {key : "cust_item_code_with_name"},{key : "medicine_name"} ]
	 	};
		//dataSource = new YAHOO.widget.DS_JSArray(jMedicineNames[document.forms[0].store_id.value]);
		oAutoComp = new YAHOO.widget.AutoComplete('medicineName', 'medicineName_dropdown', dataSource);
		oAutoComp.maxResultsDisplayed = 18;
		oAutoComp.allowBrowserAutocomplete = false;
		oAutoComp.prehighlightClassName = "yui-ac-prehighlight";
		oAutoComp.typeAhead = false;
		oAutoComp.useShadow = false;
		oAutoComp.minQueryLength = 0;
		oAutoComp.forceSelection = true;
		oAutoComp.animVert = false;
		oAutoComp.filterResults = Insta.queryMatchWordStartsWith;
		oAutoComp.formatResult = Insta.autoHighlightWordBeginnings;
		if (oAutoComp._elTextbox.value != '') {
			oAutoComp._bItemSelected = true;
			oAutoComp._sInitInputValue = oAutoComp._elTextbox.value;
		}
		oAutoComp.itemSelectEvent.subscribe(onSelectMedicine);
	}

	var onSelectMedicine = function (oSelf,args) {

		if ( args == undefined ){
			medicineName = document.forms[0].medicineName.value
		} else {
			medicineName = args[2][1];
		}
	 	
	 	document.forms[0].medicineName.value = medicineName;
		var medExist = false;
		purchaseDetails = '';
		var ajaxReqObject = newXMLHttpRequest();
		var storeId = document.forms[0].store_id.value;
		var url=cpath+"/stores/stockentry.do?_method=getBatchs&medicineName="+encodeURIComponent(medicineName)+"&storeId="+encodeURIComponent(storeId);
		getResponseHandlerText(ajaxReqObject, handleBatchNosResponse, url);
		document.forms[0].batchno.value = '';
		status = false;
	}

	var popBatchNo = '';
	function handleBatchNosResponse(responseText) {
		if (responseText==null) return;
		if (responseText=="") return;
	    eval("gBatch = " + responseText);
	    popBatchNo = gBatch;
	    batchPopulated(gBatch);

	}

	function batchPopulated(gBatch){

		if (oAutoComp1 != undefined) {
			oAutoComp1.destroy();
		}

		YAHOO.example.ACJSAddArray = new function() {
			var dataSource = new YAHOO.widget.DS_JSArray(gBatch);
			oAutoComp1 = new YAHOO.widget.AutoComplete('batchno', 'batch_dropdown', dataSource);
			oAutoComp1.maxResultsDisplayed = 10;
			oAutoComp1.allowBrowserAutocomplete = false;
			oAutoComp1.prehighlightClassName = "yui-ac-prehighlight";
			oAutoComp1.typeAhead = false;
			oAutoComp1.useShadow = false;
			oAutoComp1.minQueryLength = 0;
			oAutoComp1.forceSelection = true;
			oAutoComp1._elTextbox.value = selBatch;
			if (oAutoComp1._elTextbox.value != '') {
				oAutoComp1._bItemSelected = true;
				oAutoComp1._sInitInputValue = oAutoComp1._elTextbox.value;
				selBatch = '';
			}
			oAutoComp1.textboxFocusEvent.subscribe(function(){
				var sInputValue = YAHOO.util.Dom.get('batchno').value;
				if(sInputValue.length == 0) {
					var oSelf = this;
					setTimeout(function(){oSelf.sendQuery(sInputValue);},0);
				}
			});
			//oAutoComp1.textboxBlurEvent.subscribe(onSelectBatchNo);
		}
	}

	function checkstoreallocation() {
 		if(gRoleId != 1 && gRoleId != 2) {
 			if(deptId == "") {
 				showMessage("js.stores.mgmt.noassignedstore.notaccessthisscreen");
		 		document.getElementById("storecheck").style.display = 'none';
 			}
 		}
 		setSelItemTOac ();
	}

	function setSelItemTOac() {
		var med = document.forms[0].medicineName.value;
		if (med != '')onSelectMedicine ();
	}

	function clearSearch() {
		document.forms[0].medicineName.value='';
		document.forms[0].batchno.value='';
		document.forms[0].fromDate.value='';
		document.forms[0].toDate.value='';
		document.forms[0].history.checked=true;
		onChecked('history');
	}


function getMedicinesForStore(onCompletionFunction) {
	// get the medicine time stamp for this store: required for fetching the items.
	var storeId = document.forms[0].store_id.value;
	var url = cpath + "/stores/utils.do?_method=getStoreStockTimestamp&storeId=" + storeId;
	YAHOO.util.Connect.asyncRequest('GET', url, {
			success: onGetStockTimestamp,
			argument: onCompletionFunction,
		}
	);
}

function onGetStockTimestamp(response) {
	if (response.status != 200)
		return;

	var ts = parseInt(response.responseText);
	var storeId = document.forms[0].store_id.value;
	var url = cpath + "/pages/stores/getMedicinesInStock.do?ts=" + ts + "&hosp=" + sesHospitalId +
		"&includeZeroStock=Y&includeConsignmentStock=Y" +
		"&storeId=" + storeId;

	// Note that since this is a GET, the results could potentially come from the browser cache.
	// This is desirable. That's why the sequence of request parameters must match the original
	// <script> in the jsp.
	YAHOO.util.Connect.asyncRequest('GET', url, { success: onGetStoreStock, argument: response.argument });
}

function onGetStoreStock(response) {
	if (response.status != 200)
		return;

	eval(response.responseText);		// response is like var jMedicineNames = [...];
	// overwrite the global object.
	window.jMedicineNames = jMedicineNames;

	// the function to be called after the fetch.
	var completionFunction = response.argument;
	if (completionFunction)
		completionFunction();
}

function exportData(type)
{ 
	var form = document.forms["stockLedger"];
	document.getElementById("export_type").value = type;
	document.getElementById("medicineName").value = medicine_Name
	form.store_id.value = store_id;
	form.fromDateTime.value = fromDateTime;
	form.toDateTime.value = toDateTime;
	form.batchno.value = batchno;
	form.period.value = period;
	form.fromDate.value = fromDate;
	form.toDate.value = toDate;
	form.fromCheckPt.value = fromCheckPt;
	form.toCheckPt.value = toCheckPt;
	
	form.submit();
}


