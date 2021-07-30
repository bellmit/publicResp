/**
	TODO: Need to Clean up old code.
**/
var packageComponents = 0;
mealTimingsRequired = false;
equipTimingsRequired = false;
var addOrderDialog;
var gIsInsurance = false;
	function init() {
		loadServiceSubGroup();
		var method = document.packagemasterform._method.value;
		if (clonePackage || method == 'update')
			setSelectedIndex(document.getElementById("service_sub_group_id"), packageDetailsJSON.service_sub_group_id);

		//order dialog
		if (showOperations) {
			document.getElementById("operation_label_td").style.display = 'table-cell';
			document.getElementById("operation_dropdown_td").style.display = 'table-cell';
		} else {
			document.getElementById("operation_label_td").style.display = 'none';
			document.getElementById("operation_dropdown_td").style.display = 'none';
		}
		onTypeChange();

		var orderableItems;
		if(typeof enabledOrderableItemApi !== 'undefined' && enabledOrderableItemApi === true) {
			orderableItems = null;
		} else {
			orderableItems = rateplanwiseitems;
		}
		
		addOrderDialog = new Insta.AddOrderDialog('btnAddItem',
			orderableItems, null,
			addPackageElements,
			doctors, anaesthetists, packType, 'GENERAL',
			document.packagemasterform.org_id.value,
			'', '', null, null, 'A', 'Y', null, forceSubGroupSelection, null, anaeTypes, null,null,isMultiVisitPackage);
		initPkgValueCapDialog();

	}

	function retryJobSchedule(packageId) {
		var ajaxobj = newXMLHttpRequest();
		var url = cpath + "/master/addeditdiagnostics/retrychargeschedule.json?entity=PACKAGE&entity_id="+packageId;

		ajaxobj.open("POST", url.toString(), false);
		ajaxobj.send(null);
		if (ajaxobj.readyState == 4) {
			if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
				document.getElementById('entity_status_'+packageId).innerHTML = 'Processing';
				document.getElementById('error_status_'+packageId).innerHTML = '';
				document.getElementById('retry_job_'+packageId).innerHTML = '';
				alert("Retry processing");
				return JSON.parse(ajaxobj.responseText);
			}
		}
	}
	function calculateRoundOff() {
		if(isMultiVisitPackage == 'true') {
			var totalCharge = 0;
			var index = "";
			for(var i=0;i<bedTypesLength;i++) {
				var charge = empty(document.getElementById('charge'+i).value) ? 0 : document.getElementById('charge'+i).value
				var discount = empty(document.getElementById('discount'+i).value) ? 0 : document.getElementById('discount'+i).value;
				var packCharge = charge-discount;
				totalCharge = 0;
				for(var j=0;j<packageItemsLength;j++) {
					var index = j+""+"c"+i;
					var bedTypeItemCharge = empty(document.getElementById('pack_item_charge'+index).value) ? 0 : document.getElementById('pack_item_charge'+index).value;
					totalCharge = totalCharge + parseFloat(bedTypeItemCharge);
				}
				document.getElementById('pack_round_off'+i).value = parseFloat(packCharge) - totalCharge;
			}
		}
	}

	function getType() {
		var type = null;
		var els = document.packagemasterform.type;
		if (!empty(els.length)) {
			for (var i=0; i<els.length; i++) {
				if (els[i].checked) {
					type = els[i].value;
					break;
				}
			}
		} else {
			type = els.value;
		}
		return type;
	}
	var roundOffEdited = false;
	function isRoundOffEdited() {
		roundOffEdited = true;
	}

	function getTotalItemCharges(index) {
		var totalCharge = 0;
		var bedTypeItemBaseCharge =0;
		var bedTypeItemQty = 0;
		for(var i=0;i<packageItemsLength;i++) {
			iIndex = index;
			iIndex = i+""+"c"+iIndex;
			bedTypeItemBaseCharge = document.getElementById('multi_visit_package_item_base_charge'+iIndex).value;
			bedTypeItemQty = document.getElementById('multi_visit_package_item_qty'+i).value;
				totalCharge = totalCharge +(parseFloat(bedTypeItemBaseCharge)* parseFloat(bedTypeItemQty));
		}
		return totalCharge;
	}

	function fillValuesForItemCharge(object) {
		var tableObject = document.getElementById('itemChargesTable');
		if (object.checked) {
			for(var i=0;i<bedTypesLength;i++) {
				for(var j=0;j<packageItemsLength;j++) {
					var index = "";
					index = j+""+"c"+i;
					document.getElementById("pack_item_charge" + index).value = document.getElementById("pack_item_charge" +j+""+'c'+'0').value;
				}
			}

			if(roundOffEdited) {
				for(var i=0;i<bedTypesLength;i++) {
					document.getElementById("pack_round_off"+i).value = document.getElementById("pack_round_off"+0).value;
				}
				roundOffEdited = false;
			}
			object.checked = false;
			calculateRoundOff();
		}

	}

	function calculateItemCharges(obj,index,charge) {
		if(isMultiVisitPackage == 'true') {
			var packCharge = 0;
			if(charge == 'discount')
				packCharge = document.getElementById('charge'+index).value-obj.value;
			else
				packCharge = obj.value-document.getElementById('discount'+index).value;
			var totalCharge = 0;
			var bedTypeItemBaseCharge = 0;
			var totalChargeForRoundOff = 0;
			var bedTypeItemQty = 0;
			var packroundoffindex = index -1;
				totalCharge = getTotalItemCharges(index);
			for(var i=0;i<packageItemsLength;i++) {
				var iIndex = index;
					iIndex = i+""+"c"+iIndex
				bedTypeItemBaseCharge = document.getElementById('multi_visit_package_item_base_charge'+iIndex).value;
				bedTypeItemQty = document.getElementById('multi_visit_package_item_qty'+i).value;
				document.getElementById('pack_item_charge'+iIndex).value = (empty(totalCharge) ||totalCharge == 0) ?
									0 : parseInt((parseFloat(packCharge/totalCharge))*parseFloat(bedTypeItemBaseCharge)*parseInt(bedTypeItemQty));
			}
			totalChargeForRoundOff = getBedTypesToatlCharge(index);
			document.getElementById('pack_round_off'+index).value = packCharge-totalChargeForRoundOff;
		}
	}

	function getBedTypesToatlCharge(index) {
		var totalCharge = 0;
		var bedTypeItemCharge = 0;
		for(var i=0;i<packageItemsLength;i++) {
			iIndex = index;
			iIndex = i+""+"c"+iIndex;
			bedTypeItemCharge = document.getElementById('pack_item_charge'+iIndex).value;
				totalCharge = totalCharge +parseFloat(bedTypeItemCharge);
		}
		return totalCharge;

	}

	function fillPackageItemCharges(object) {
		if(isMultiVisitPackage == 'true') {
			var tableObject = document.getElementById("itemChargesTable");
			var packCharge = document.getElementById('charge'+0).value-document.getElementById('discount'+0).value;
			var totalItemCharge = 0;
			for(var i=0;i<bedTypesLength;i++) {
				totalItemCharge = getTotalItemCharges(i);
				for(var j=0;j<packageItemsLength;j++) {
					var index = "";
						index = j+""+"c"+i;
					var bedTypeItemBaseCharge = document.getElementById('multi_visit_package_item_base_charge'+index).value;
					var bedTypeItemQty = document.getElementById('multi_visit_package_item_qty'+j).value;

					document.getElementById('pack_item_charge'+index).value = (empty(totalItemCharge) ||totalItemCharge == 0) ?
											0 : parseInt((parseFloat(packCharge/totalItemCharge))*parseFloat(bedTypeItemBaseCharge)*parseInt(bedTypeItemQty));
				}
				totalChargeForRoundOff = getBedTypesToatlCharge(i);;
				document.getElementById('pack_round_off'+i).value = packCharge-totalChargeForRoundOff;
			}
		}
	}

	function validateForm() {
		if (masterJobCount != undefined && masterJobCount > 0) {
			alert("Package charge scheduler in progress");
			return false;
		}
		if(isMultiVisitPackage == 'true') {
			var packageCharge = 0;
			for(var i=0;i<bedTypesLength;i++) {
				packageCharge = document.getElementById('charge'+i).value - document.getElementById('discount'+i).value;
				var bedTypeRoundOff = document.getElementById('pack_round_off'+i).value;
				var totalItemChargeForBedType = 0;
				for(var j=0;j<packageItemsLength;j++) {
					var index = "";
					var index = j+""+"c"+i;
					totalItemChargeForBedType = totalItemChargeForBedType + parseFloat(document.getElementById('pack_item_charge'+index).value);
				}
				if(!empty(bedTypeRoundOff) && bedTypeRoundOff != 0) {
					alert("round off should be zero");
					return false;
				}
				totalItemChargeForBedType = totalItemChargeForBedType + parseInt(bedTypeRoundOff);
				if(packageCharge != totalItemChargeForBedType) {
					alert("package charge shoud be sum of all items charge including round off.");
					return false;
				}

			}
		}
		return true;
	}

function filterList(list, varName, varValue) {
	var filteredList = new Array();
	for (var i=0; i<list.length; i++) {
		if (list[i][varName] == varValue) {
			filteredList.push(list[i]);
		}
	}
	return filteredList;
}

function findInList(list, varName, varValue) {
	for (var i=0; i<list.length; i++) {
		if (list[i][varName] == varValue) {
			return list[i];
		}
	}
	return null;
}

function setHiddenValue(index, name, value) {
	var el = getIndexedFormElement(packagemasterform, name, index);
	if (el) {
		el.value = value;
	}
}


var tempTotDisc = 0;
var tempTotAmt = 0;
function getThisRow(node) {
	return findAncestor(node, "TR");
}

function getIndexedValue(name, index) {
	var obj = getIndexedFormElement(packagemasterform, name, index);
	if (obj)
		return obj.value;
	else
		return null;
}

function setIndexedValue(name, index, value) {
	var obj = getIndexedFormElement(packagemasterform, name, index);
	if (obj)
		obj.value = value;
	return obj;
}

function getChargeRow(i) {
	i = parseInt(i);
	var table = document.getElementById("chargeTable");
	return table.rows[i + 1];
}

function setFlagStyle(i) {
	var row = getChargeRow(i);
	var flagImgs = row.cells[0].getElementsByTagName("img");
	var chargeId = getIndexedValue("packageObId", i);
	var isNew = (chargeId.substring(0,1) == '_');
	var deleted = getIndexedValue("delCharge", i);
//	var edited = getIndexedValue("edited", i);

	var src;
	var cls;
	if (deleted == 'Y') {
		src = cpath+"/images/red_flag.gif";
		cls = 'delete';
	} else {
		if (isNew) {
			src = cpath+"/images/green_flag.gif";
			cls = 'newRow';
		} else {
			src = cpath+"/images/empty_flag.gif";
			cls = '';
		}
	}

	if (flagImgs && flagImgs[0])
		flagImgs[0].src = src;
	row.className = cls;
}

/*
 * Re-calculate all the totals: discount, amount
 */
function removeBottomBorder(index)
{
	addClassName('chargeHeadName'+index, 'previousEl');
	addClassName('description'+index, 'previousEl');
	addClassName('remarks'+index,'previousEl');
	addClassName('activity_qty'+index,'previousEl');
}
function changeColor(obj) {

	var row = getThisRow(obj);
	var id = row.rowIndex - 1;

	var oldDeleted =  getIndexedValue("delCharge", id);
//	var chargeId = getIndexedValue("chargeId", id);
//	var isNew = (chargeId.substring(0.1) == '_');

	var newDeleted;
	if (oldDeleted == 'Y'){
		newDeleted = 'N';
	} else {
		newDeleted = 'Y';
	}

	setIndexedValue("delCharge", id, newDeleted);
	setIndexedValue("edited", id, 'Y');
	setFlagStyle(id);

	/**var markRow;
	markRow=document.getElementById("delCharge"+index).value=='N'?'Y':'N';
	document.getElementById("delCharge"+index).value=document.getElementById("delCharge"+index).value=='N'?'Y':'N';

	if(markRow=='Y')
	{
		addClassName('chargeHeadName'+index, 'delete');
		addClassName('description'+index, 'delete');
		addClassName('remarks'+index,'delete');
		addClassName('activity_qty'+index,'delete');
	}
	else
	{
		removeClassName('chargeHeadName'+index, 'delete');
		removeClassName('description'+index, 'delete');
		removeClassName('remarks'+index,'delete');
		removeClassName('activity_qty'+index,'delete');
	}*/
}

function deductFromAmount(id){
	var tempTotAmt = document.getElementById("totAmt").value;
	if(parseInt(tempTotAmt) != 0){
		tempTotAmt =
		eval(parseFloat(tempTotAmt) -
		(parseFloat(getIndexedFormElement(packagemasterform,'activity_charge',id).value)
		*parseFloat(getIndexedFormElement(packagemasterform,'activity_qty',id).value)));
	}
	if(tempTotAmt < 0 ) tempTotAmt = 0;
	document.getElementById("totAmt").value = tempTotAmt;
}

var no_cancelled_rows = 0;
function validate() {
	if (document.forms[0].package_name.value == '') {
		alert("Package name can not be empty");
		document.forms[0].package_name.focus();
		return false;
	}
	if (document.forms[0].package_type.value == '') {
		alert("Select Package type ");
		document.forms[0].package_type.focus();
		return false;
	}
	if (document.getElementById('service_group_id').selectedIndex == 0) {
		alert("Service Group is required");
		document.getElementById('service_group_id').focus();
		return false;
	}
	if (document.getElementById('service_sub_group_id').selectedIndex == 0) {
		alert("Service Sub Group is required");
		document.getElementById('service_sub_group_id').focus();
		return false;
	}
	document.packagemasterform.description.value = trim(document.packagemasterform.description.value);
	document.getElementById("package_name").value = trim(document.getElementById("package_name").value);
	if (document.getElementById("package_name").value == "") {
		alert("Please enter package name");
		document.getElementById("package_name").focus();
		return false;
	}

	if (getType() == "P") {
		if(!checkAmt())return false;

		if(document.packagemasterform.totAmt.value < 0){
			alert("Packag charge can not be negative");
			document.packagemasterform.totAmt.value = 0;
			document.packagemasterform.totAmt.focus();
			return false;
		}
	}
	if (document.packagemasterform._method.value == 'create') {
		if (!checkDuplicatePackage()) return false;
		if (packageComponents <= 0 ) {
			 alert("package should have atleast one charge element");
			 return false;
		 }
	} else {
		var table = document.getElementById('packageComponentTable');
		var tabLength = table.rows.length-3;
		var index = 0;
		var cancelled = document.getElementsByName('cancelled');

		for(var i=0;i<cancelled.length-1;i++) {
			if(!empty(cancelled[i].value) && cancelled[i].value != 'N') {
				index++;
			}
		}
		if(tabLength == index) {
			alert("package must contain atleast one item.");
			return false;
		}
	}
	return true;
}
function checkDuplicatePackage(){
	for(var i =0;i<packages.length;i++){
		if(document.getElementById("package_name").value == packages[i].package_name){
			alert("Package Name already exists");
			document.getElementById("package_name").focus();
			return false;
		}
	}
	return true;
}
 function onSave(){
 	if (!validate()) {
	 	return false;
 	}
 	var fromObj = document.getElementById('valid_from_date');
 	var toObj = document.getElementById('valid_to_date');

 	if (!doValidateDateField(fromObj))
		return false;
	if (!doValidateDateField(toObj))
		return false;

	var fromDt = getDateFromField(fromObj);
	var toDt = getDateFromField(toObj);

	if ( !empty(toDt) && !empty(fromDt) ) {
		if (fromDt > toDt) {
			showMessage("js.common.message.date.to.before.from");
			return false;
		}
	}

	var isInsuranceCatIdSelected = false;
	var insuranceCatId = document.getElementById('insurance_category_id');
	for (var i=0; i<insuranceCatId.options.length; i++) {
	  if (insuranceCatId.options[i].selected) {
		  isInsuranceCatIdSelected = true;
	  }
	}
	if (!isInsuranceCatIdSelected) {
		alert("Please select at least one insurance category");
		return false;
	}

	var packObIds = document.getElementsByName("pack_ob_id");
	var method = document.packagemasterform._method.value;
	if(method == 'update') {
		for(var i=0; i<packObIds.length; i++) {
			var packObId = packObIds[i].value;
			if(packObId.substring(0,1) == '_' ) {
				alert("Charge will be 0 for newly added items. Please use edit charge screen to update.");
			}
		}
	}

	document.packagemasterform.submit();
	return true;
 }

function setHandoverValue(obj) {
	document.packagemasterform.handover_to.value = obj.value;
}

function onTypeChange() {
	var type = getType();
	var method = document.packagemasterform._method.value;
	var disabled = !(type == 'P' && method == 'create');
	document.getElementById('totAmt').disabled = disabled;
	var approvalStatusDiv = document.getElementById('CollapsiblePanel1');
	if (type == 'P' && mod_adv_packages == 'Y') {
		approvalStatusDiv.style.display = 'block';
	} else {
		approvalStatusDiv.style.display = 'none';
	}


	if (isMultiVisitPackage != 'true' && type == 'P') {
		document.getElementById('handover_dropdown_div').style.display = 'block';
		document.getElementById('handover_label_div').style.display = 'none';
		document.packagemasterform.handover_to.value = document.packagemasterform.dd_handover_to.value;
	} else {
		// for template and multivisit packages display only label.
		document.getElementById('handover_dropdown_div').style.display = 'none';
		document.getElementById('handover_label_div').style.display = 'block';
		document.packagemasterform.handover_to.value = 'P';
	}

/*	if (mod_adv_packages == 'Y' && (packType == 'o' || packType == 'd') && type == 'P') {
		document.getElementById('mpLabelTd').style.visibility = 'show';
		document.getElementById('mpValueTd').style.visibility = 'show';
	} else {
		// here style.display creating issues with alignment hence used visibility: hidden
		document.getElementById('mpLabelTd').style.visibility = 'hidden';
		document.getElementById('mpValueTd').style.visibility = 'hidden';
		document.getElementsByName('multi_visit_package')[1].checked = true; // defaulting to 'N'
	}*/
	if(document.getElementById('dtLabelTd')) {
		if (mod_adv_packages == 'Y' && type == 'P') {
			document.getElementById('dtLabelTd').style.display = 'table-cell';
			document.getElementById('dtValueTd').style.display = 'table-cell';
		} else {
			document.getElementById('dtLabelTd').style.display = 'none';
			document.getElementById('dtValueTd').style.display = 'none';
			var elements = document.getElementById("doc_type_id").options;

		    for (var i = 0; i < elements.length; i++) {
		       if (elements[i].selected)
		        	elements[i].selected = false;
		    }
		}
	}
}


function chk(e){
  var key=0;
  if(window.event || !e.which)
  {
	 key = e.keyCode;
  	  }
  else
  {
	 key = e.which;
  }
     if(document.packagemasterform.descrip.value.length<200 || key==8)
     {
       key=key;
       return true;
     }
     else
     {
      key=0;
      return false;
    }
}
function chklen(){
  document.packagemasterform.description.value = trim(document.packagemasterform.description.value);

  	 if(document.packagemasterform.description.value.length>4000)
  	 {
  	    var s = document.packagemasterform.description.value;
  	    s = s.substring(0,4000);
    	document.packagemasterform.description.value = s;
  	    alert("description should be 4000 characters only");
  	    document.packagemasterform.description.focus();
  	 }
  }
function changeRate(){
	document.forms[0].submit();
}
function onRatePlanChange(){
	document.forms[0]._method.value="getEditPackageCharges";
	document.forms[0].submit();
}
function checkAmt(){
	if (!validateAmount(document.packagemasterform.totAmt, "Amount must be a valid number")){
			document.packagemasterform.totAmt.value = 0;
			return false;
		}
	if(document.packagemasterform.totAmt.value == '' || document.packagemasterform.totAmt.value == ''){
		alert("Please give Package Charge");
		document.packagemasterform.totAmt.value = 0;
		document.packagemasterform.totAmt.focus();
			return false;
	}
	return true;
}
function limitText(limitField,limitNum) {
	if (limitField.value.length > limitNum) {
		alert("A maximum description of only "+ limitNum +" characters can be entered...");
		limitField.value = limitField.value.substring(0, limitNum);
	}
}


function subscribeEscKeyEvent(dialog) {
	var kl = new YAHOO.util.KeyListener(document, { keys:27 },
		{ fn:dialog.cancel, scope:dialog, correctScope:true } );
	dialog.cfg.setProperty("keylisteners", kl);
}


function initEditDialog() {
	editDialog = new YAHOO.widget.Dialog("editDialog", {
		width:"450px",
		context: ["packageComponentTable", "tr", "br"],
		visible:false,
		modal:true,
		constraintoviewport:true
	});
	editDialog.render();
	subscribeEscKeyEvent(editDialog);
}

function handleEditDialogCancel() {

	var id = document.packagemasterform.editRowId.value;
	var row = getChargeRow(id);
	editDialog.cancel();
	YAHOO.util.Dom.removeClass(row, 'rowUnderEdit');
}

function showEditChargeDialog(obj) {

	var row = getThisRow(obj);
	var id = row.rowIndex - 1;

	YAHOO.util.Dom.addClass(row, 'editing');
	document.packagemasterform.editRowId.value = id;
	var group = getIndexedValue("chargegroup_id", id);
	var newEl = getElementByName(row, 'pack_ob_id');
	if ( (newEl.value).substring(0,1) == '_' ){
		alert("Edit is not allowed for New items");
		return false;
	}

	if(group == 'OTC'){
		document.editForm.eQty.disabled = false;
	}else{
		if(isMultiVisitPackage == 'true') {
			document.editForm.eQty.disabled = false;
		} else {
			document.editForm.eQty.disabled = true;
		}
	}

	document.editForm.eRemarks.value = getIndexedValue("activity_remarks", id);
	document.editForm.eQty.value = getIndexedValue("activity_qty", id)
	document.editForm.eDisplayOrder.value = getIndexedValue("display_order", id);


	editDialog.cfg.setProperty("context", [row.cells[4], "tr", "bl"], false);
	editDialog.cfg.setProperty("context", [row.cells[5], "tr", "bl"], false);
	editDialog.show();
	document.editForm.eRemarks.focus();
	return false;
}


function onEdit() {

	var id = document.packagemasterform.editRowId.value;
	var row = getChargeRow(id);
	var stFlag='stFlag'+(parseInt(id)+1);
	document.getElementById(stFlag).src=cpath+'/images/blue_flag.gif';

	setNodeText(row.cells[3], document.getElementById("eRemarks").value, 20,
					document.getElementById("eRemarks").value);

	setIndexedValue("remarks", id, document.getElementById("eRemarks").value);

	setNodeText(row.cells[4], document.getElementById("eQty").value,20,
					document.getElementById("eQty").value);

	setIndexedValue("activity_qty", id, document.getElementById("eQty").value);

	setNodeText(row.cells[0], document.getElementById("eDisplayOrder").value,20,
					document.getElementById("eDisplayOrder").value);

	setIndexedValue("display_order", id, document.getElementById("eDisplayOrder").value);

	YAHOO.util.Dom.removeClass(row, 'rowUnderEdit');
}

function initAddDialog() {
	addDialog = new YAHOO.widget.Dialog("addDialog",
			{
			width:"880px",
			context :["", "tr", "br"],
			visible:false,
			modal:true,
			constraintoviewport:true
			});
	addDialog.render();
	subscribeEscKeyEvent(addDialog);
}

//new order changes
function saveEdit() {
	var id = document.packagemasterform.editRowId.value;
	var table = document.getElementById("packageComponentTable");
	var row =  table.rows[parseInt(id)+1];
	var newEl = getElementByName(row, 'pack_ob_id');
	var mvPackItemQty = document.editForm.eQty.value;
	var eDisplayOrder=document.getElementById('eDisplayOrder').value;
	if(eDisplayOrder=="" || eDisplayOrder==null){
		alert("Display Order should not be empty");
		return false;
	}
	if ((newEl.value).substring(0,1) == '_') // edit of a newly added row is not allowed, so we should not be here.
		return false;

	if(isMultiVisitPackage == 'true') {
		if(empty(mvPackItemQty)) {
			alert("for multi visit package item quantity is required.");
			document.editForm.eQty.focus();
			return false;
		}
		if(mvPackItemQty == 0) {
			alert("for multi visit package item quantity can not be zero.");
			document.editForm.eQty.focus();
			return false;
		}
	}


	var editedEl = getElementByName(row, 'edited');
	if (editedEl)
		editedEl.value = 'Y';
	getIndexedFormElement(packagemasterform,"activity_remarks", id).value = document.editForm.eRemarks.value;
	getIndexedFormElement(packagemasterform,"activity_qty", id).value = document.editForm.eQty.value;
	getIndexedFormElement(packagemasterform,"display_order", id).value = document.editForm.eDisplayOrder.value;

	setNodeText(row.cells[0], document.editForm.eDisplayOrder.value, 16);
	setNodeText(row.cells[4], document.editForm.eRemarks.value, 16);
	setNodeText(row.cells[5], document.editForm.eQty.value, 16);

	editDialog.cancel();
	YAHOO.util.Dom.addClass(row, 'edited');
}
function cancelEdit() {
	var id = document.getElementById("editRowId").value;
	var row = getRow(id);
	YAHOO.util.Dom.removeClass(row, 'editing');
	editDialog.cancel();
}
function addPackageElements(packageelement) {
	var type = packageelement.itemType;
	var activityId = packageelement.itemId;
	var activity_rate = packageelement.rate;
	var chargeType = packageelement.chargeType;
	var mvPackItemQty = packageelement.quantity;

	if (packageelement.rateDetails == null)
		return true;

	if(isMultiVisitPackage == 'true') {
		if(mvPackItemQty == 0) {
			alert("for multi visit package item quantity can not be zero.");
			return false;
		}
	}

		var flag = true;
		if(isMultiVisitPackage == 'true') {
			var itemIds = document.getElementsByName("activity_id");
			var consultationTypeId = document.getElementsByName("consultation_type_id");
			for(var i=0;i<itemIds.length;i++) {
				if(type == 'Doctor') {
					if(consultationTypeId[i].value == chargeType) {
						flag = false;
						break;
					}
				} else {
					if(itemIds[i].value == activityId) {
						flag = false;
						break;
					}
				}
			}

			if(!flag) {
				alert("duplicate item is not allowed for multivisit package.");
				return false;
			}
		}

	for (var i=0; i<packageelement.rateDetails.length; i++) {
		var addedelement = packageelement.rateDetails[i];
		if((addedelement.chargeGroup == 'BED' && addedelement.chargeHead != 'BBED')
		  ||(addedelement.chargeGroup == 'ICU' && addedelement.chargeHead != 'BICU')
		  ||(addedelement.chargeGroup == 'TAX')){
			continue;
		}
		addAsPackageElement(activityId,packageelement.quantity,
					addedelement.actUnit,packageelement.remarks,
					addedelement.actDescription,packageelement.itemType,
					addedelement.chargeHead,addedelement.actRate,addedelement.chargeGroup);
	}
	return true;
}
function addAsPackageElement(itemid,qty,unit,remarks,itemname,type,head,rate,group){
	var table = document.getElementById("packageComponentTable");
	var numRows = table.rows.length;
	var templateRow = table.rows[numRows-2];
	var newRow = templateRow.cloneNode(true);
	newRow.style.display = '';
	table.tBodies[0].insertBefore(newRow, templateRow);
	newRow.id="chRow"+(numRows-2);
	newRow.className = "newRow";
	newRow.cells[7].innerHTML = '';
	var id = numRows-2;
	var hiddenIndex = getHiddenIndex(id);

	var itemDisplayName = itemname;
	var displayType = findInList(jChargeHeads, "CHARGEHEAD_ID", head).CHARGEHEAD_NAME;
	//appent rate with name for Miscellaneous charges
	if(head == 'MISOTC'){
 		itemDisplayName =  itemname+"(Rate: "+rate+")";
	 }
	if(qty == 0){
		qty= 1;
	}
	var consultationTypeName =  '';
	if (group == 'DOC')
		consultationTypeName = getSelText(document.orderDialogForm.doctorCharge);

	var nextDisplayOrder = getNextDisplayOrder(document.packagemasterform.display_order);
	setNodeText(newRow.cells[0], nextDisplayOrder);
   	setNodeText(newRow.cells[1], displayType);
	setNodeText(newRow.cells[2], itemDisplayName, 40);
	setNodeText(newRow.cells[3], consultationTypeName, 40);
	setNodeText(newRow.cells[4], remarks, 20);
	setNodeText(newRow.cells[5], qty);
	setNodeText(newRow.cells[6], unit);

	setHiddenValue(hiddenIndex, "charge_head", head);
	setHiddenValue(hiddenIndex, "chargegroup_id", group);
	setHiddenValue(hiddenIndex, "pack_ob_id", "_"+id);
	setHiddenValue(hiddenIndex, "activity_id", itemid);
	setHiddenValue(hiddenIndex, "activity_remarks", remarks);
	setHiddenValue(hiddenIndex, "delCharge", "N");
	setHiddenValue(hiddenIndex, "activity_description", itemname);
	setHiddenValue(hiddenIndex, "activity_qty", qty);
	setHiddenValue(hiddenIndex, "activity_type", type);
	setHiddenValue(hiddenIndex, "cancelled", "N");
	setHiddenValue(hiddenIndex, "edited", "N");
	setHiddenValue(hiddenIndex, "activity_charge", rate);
	setHiddenValue(hiddenIndex, "display_order", nextDisplayOrder);
	if(head == 'EQUOTC' || type == 'Bed' || type == 'ICU'){
		setHiddenValue(hiddenIndex, "activity_units", unit == "Days"?"D":"H");
	}else{
		setHiddenValue(hiddenIndex, "activity_units", '');
	}
	if(group == 'DOC')
		setHiddenValue(hiddenIndex, "consultation_type_id", document.orderDialogForm.doctorCharge.value);
	else
		setHiddenValue(hiddenIndex, "consultation_type_id", '');

	packageComponents++;
	if (getType() == 'P') {
		var pamount = formatAmountValue(rate*qty);
		var prevamount = document.getElementById("totAmt").value
		document.getElementById("totAmt").value = eval (parseInt(prevamount) + parseInt(pamount));
	}
	return id;
}
function cancelPackageComponent(imgObj) {

	var row = getThisRow(imgObj);
	var id = row.rowIndex - 1;
	var newEl = getIndexedValue('pack_ob_id',id);

	var isNew = (newEl.substring(0,1) == '_');

	if (isNew) {
		// this is a new item, we just need to delete the entire row from the table
		if (getType() == 'P') {
			deductFromAmount(id);
		}
		row.parentNode.removeChild(row);
		packageComponents--;

	} else {
		// existing row. Operations can be uncancel or to ask for cancel options.
		var cancelEl = getElementByName(row, 'cancelled');
		var editEl = getElementByName(row, 'edited');

		if (cancelEl.value == 'N') {
			rowUnderEdit = row;
			cancelEl.value = "Y";
			editEl.value = "Y";

			// set "deleted" flag and also the edited class to the row
			YAHOO.util.Dom.addClass(row, 'edited');

			var flagImg = row.cells[1].getElementsByTagName("img")[0];
			flagImg.src = cpath+"/images/red_flag.gif";
			var trashImg = row.cells[7].getElementsByTagName("img")[0];
			trashImg.src = cpath+"/icons/undo_delete.gif";
		} else {
			cancelEl.value = 'N';
			editEl.value = "Y";
			var editedEl = getElementByName(row, 'edited');
			if (editedEl.value =='Y') {
				row.className = 'edited';
			} else {
				YAHOO.util.Dom.removeClass(row, 'edited');
			}
			var flagImg = row.cells[1].getElementsByTagName("img")[0];
			flagImg.src = cpath+"/images/empty_flag.gif";
			var trashImg = row.cells[7].getElementsByTagName("img")[0];
			trashImg.src = cpath+"/icons/delete.gif";
		}
	}
}
function disableFields() {
	document.orderDialogForm.prescribing_doctor.disabled = true;
	document.orderDialogForm.presdate.disabled = true;
	document.orderDialogForm.prestime.disabled = true;
}
function getRow(i) {
	i = parseInt(i);
	var table = document.getElementById("packageComponentTable");
	return table.rows[i + 1];
}
function getCharge(obj) {
	var prevamount = document.getElementById("totAmt").value
		var opeAmount = formatAmountValue(document.getElementById("opeCharge").value);
		if(prevamount != 0){
			document.getElementById("totAmt").value = eval (parseInt(prevamount) - parseInt(opeAmount));
		}

	if(document.packagemasterform.operation_id.value != ""){
		var url = cpath + '/pages/masters/insta/admin/PackagesMasterAction.do?_method=getPackageOperationCharge'+
			'&type=Operation&id='+document.packagemasterform.operation_id.value +
			'&bedType=GENERAL&orgId='+document.packagemasterform.org_id.value+
			'&fromDate=&toDate=&quantity=1&units=D&chargeType=&ot=&surgeon=&anaesthetist= '+
			' &visitType=i&operationRef=';
			;
		YAHOO.util.Connect.asyncRequest('GET', url, {success: getOperationCharge, failure: null});
	}else{
		document.getElementById("opeCharge").value = 0;
	}
}
function getOperationCharge(response) {
	if (response.responseText != undefined) {
		var charge  = eval('(' + response.responseText + ')');
		var opeAmount = formatAmountValue(charge);
		var prevamount = document.getElementById("totAmt").value
		document.getElementById("totAmt").value = eval (parseInt(prevamount) + parseInt(opeAmount));
		document.getElementById("opeCharge").value = opeAmount;
		packageComponents++;
	}
}
function getHiddenIndex(id){
	return id-1;
}
function loadServiceSubGroup(){
	var filteredSubGroupList = filterList(servicesSubGroupsJSON,'service_group_id',document.getElementById("service_group_id").value);
	loadSelectBox(document.getElementById("service_sub_group_id"), filteredSubGroupList, 'service_sub_group_name', 'service_sub_group_id', '--Select--', '0');
	if(document.packagemasterform.package_code.value == '')
		getOrderCode();
}
function getOrderCode(){
	var group = document.getElementById("service_group_id").value;
	var subGroup = document.getElementById("service_sub_group_id").value;
	if(subGroup != '0')
		ajaxForOrderCode('Package','Package',group,subGroup,document.packagemasterform.package_code);
}
function getNextDisplayOrder(nodeList){
	var displayOrderNodeList = (document.packagemasterform.display_order);
	if(displayOrderNodeList.length-2 == 0)
		return 1;
	var displayOrderList = new Array(displayOrderNodeList.length-2);
	for(var i=0;i<displayOrderNodeList.length-2;i++){
		displayOrderList[i]=parseFloat(displayOrderNodeList[i].value);
	}
	displayOrderList.sort(sortNumbers);
	return displayOrderList[0]+1;
}
function sortNumbers(a, b){
	return b-a;
}

function validateAllDiscounts() {
	var len = document.forms[0].ids.value;
	var valid = true;
	for(var i=0;i<len;i++) {
		valid = valid && validateDiscount('charge','discount',i);
	}
	if(!valid) return false;
	else return true;
}

function ChanellingValues(obj) {
	document.getElementById('chanelling').value = obj.checked ? 'Y' : 'N';
}