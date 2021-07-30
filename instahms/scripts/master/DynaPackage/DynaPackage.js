
var toolBar = {
	Edit : {
		title : 'View/Edit',
		imageSrc : 'icons/Edit.png',
		href  : 'master/DynaPackage.do?_method=show',
		onclick : null,
		description : 'View and/or Edit Dyna Package Details'
	}
};

function validate() {
	if (document.getElementById('dyna_package_name').value=="") {
		alert("Dyna Package Name is required");
		document.getElementById('dyna_package_name').focus();
		return false;
	}

    if(!validateCharge()) {
        return false;
    }

	document.dynaPackageForm.submit();
	return true;
}

function getChargesForNewRatePlan() {
	document.forms[0]._method.value = "show";
	document.forms[0].submit();
}

function fillAllValuesForAdd() {
	if (document.dynaPackageForm._method.value == 'create') {
		var checkObj = document.dynaPackageForm.checkbox;
		var tableName = "dynaPackageCharges";
		checkObj.checked = true;
		fillAllValues(tableName, checkObj);
	}
}

function fillAllValues(tableName, object) {
	if (object.checked) {
			var tableObject = document.getElementById(tableName);
			var index = 0;
			var auditLogRow = document.getElementById('audit_log_row');

			if (auditLogRow)
				index = 3;
			else
				index = 2;

			for (var i=1; i<=((tableObject.rows.length)-index); i++) {

				var rowObject = tableObject.rows[i];
				var el = rowObject.cells[1];
				var textEls = el.getElementsByTagName("input");
				if (textEls.length != 0 && !empty(textEls[0].name)) {
					var elName = textEls[0].name;
					var elValue = textEls[0].value;

					for (var j=1; j<(rowObject.cells.length-1); j++) {
						if (document.getElementById(elName + j) != null && !document.getElementById(elName + j).readOnly)
							document.getElementById(elName + j).value = elValue;
					}
				}else {
					var selectEls = el.getElementsByTagName("select");
					if (selectEls && selectEls[0]) {
						var elName = selectEls[0].name;
						var elValue = selectEls[0].value;

						for (var j=1; j<(rowObject.cells.length-1); j++) {
							
							if(document.getElementById(elName + j) != null) {
								setSelectedIndex(document.getElementById(elName + j), elValue);
							}	
							
							var limitTypeEls = el.getElementsByTagName("input");
							var limitTypeValue = limitTypeEls[0].value;
							var makeReadOnly = (elValue == 'N');

							var amtRowObject = tableObject.rows[i+1];
							if (amtRowObject != null) {
								var amtEl = amtRowObject.cells[1];
								var amtTextEls = amtEl.getElementsByTagName("input");
								if (amtTextEls && amtTextEls[0]) {
									var amtElName = amtTextEls[0].name;
									for (var a=0; a<(amtRowObject.cells.length-1); a++) {
										if (limitTypeValue == 'Q'){
											if(document.getElementById(amtElName + a) != null){
												document.getElementById(amtElName + a).readOnly = true;
											}
										}
										else if (limitTypeValue == 'A'){
											if(document.getElementById(amtElName + a) != null) {
												document.getElementById(amtElName + a).readOnly = false;
											}
										}
										else{
											if(document.getElementById(amtElName + a) != null){
												document.getElementById(amtElName + a).readOnly = true;
											}
												
										}
										if (makeReadOnly){
											if(document.getElementById(amtElName + a)!=null){
												document.getElementById(amtElName + a).readOnly = true;
											}
										}
										else{
											if(document.getElementById(amtElName + a)!=null){
												document.getElementById(amtElName + a).readOnly =
														document.getElementById(amtElName + a).readOnly;
											}
										}
									}
								}
							}

							var qtyRowObject = tableObject.rows[i+2];
							if (qtyRowObject != null) {
								var qtyEl = qtyRowObject.cells[1];
								var qtyTextEls = qtyEl.getElementsByTagName("input");
								if (qtyTextEls && qtyTextEls[0]) {
									var qtyElName = qtyTextEls[0].name;
									for (var q=0; q<(qtyRowObject.cells.length-1); q++) {

										if (limitTypeValue == 'A'){
											if(document.getElementById(qtyElName + q) !=null){
												document.getElementById(qtyElName + q).readOnly = true;
											}
										}
										else if (limitTypeValue == 'Q'){
											if(document.getElementById(qtyElName + q) != null){
												document.getElementById(qtyElName + q).readOnly = false;
											}
												
												
										}
										else{
											if(document.getElementById(qtyElName + q)){
												document.getElementById(qtyElName + q).readOnly = true;
											}
										}

										if (makeReadOnly){
											if(document.getElementById(qtyElName + q) != null){
												document.getElementById(qtyElName + q).readOnly = true;
											}
										}
										else{
											if(document.getElementById(qtyElName + q) !=null){
												document.getElementById(qtyElName + q).readOnly =
													document.getElementById(qtyElName + q).readOnly;
											}
										}
											
									}
								}
							}
						}
						
					}
				}
			}
			object.checked = false;
	}
}

function enableDisableFields(obj, cellIndex, catIndex) {
	var rowObject = findAncestor(obj, "TR");
	var el = rowObject.cells[cellIndex];

	var pkgIncludedObj = document.getElementById(catIndex + ".pkg_included" + cellIndex);
	var limitTypeObj = document.getElementById(catIndex + "limitType" + cellIndex);
	var amountObj = document.getElementById(catIndex + ".amount_limit" + cellIndex);
	var qtyObj = document.getElementById(catIndex + ".qty_limit" + cellIndex);

	var limitType = limitTypeObj.value;
	var pkgIncluded = pkgIncludedObj.value;

	var makeReadOnly = (pkgIncluded == 'N');

	if (limitType == 'Q')
		amountObj.readOnly = true;
	else if (limitType == 'A')
		amountObj.readOnly = false;
	else
		amountObj.readOnly = true;

	if (makeReadOnly)
		amountObj.readOnly = true;
	else
		amountObj.readOnly =	amountObj.readOnly;

	if (limitType == 'A')
		qtyObj.readOnly = true;
	else if (limitType == 'Q')
		qtyObj.readOnly = false;
	else
		qtyObj.readOnly = true;

	if (makeReadOnly)
		qtyObj.readOnly = true;
	else
		qtyObj.readOnly =	qtyObj.readOnly;
}

function selectAllBedTypes(){
	var selected = document.updateform.allBedTypes.checked;
	var bedTypesLen = document.updateform.selectBedType.length;

	for (i=bedTypesLen-1;i>=0;i--) {
		document.updateform.selectBedType[i].selected = selected;
	}
}

function deselectAllBedTypes(){
	document.updateform.allBedTypes.checked = false;
}

function changeRatePlan(){
	document.searchform.submit();
}

function selectAllPagePackages() {
	var checked = document.listform.allPagePackages.checked;
	var length = document.listform.selectPackage.length;

	if (length == undefined) {
		document.listform.selectPackage.checked = checked;
	} else {
		for (var i=0;i<length;i++) {
			document.listform.selectPackage[i].checked = checked;
		}
	}
}

function onChangeAllPackages() {
	var val = getRadioSelection(document.updateform.allPackages);
	// if allPackages = yes, then disable the page selections
	var disabled = (val == 'yes');

	var listform = document.listform;
	listform.allPagePackages.disabled = disabled;
	listform.allPagePackages.checked = false;

	var length = listform.selectPackage.length;

	if (length == undefined) {
		listform.selectPackage.disabled = disabled;
		listform.selectPackage.checked  = false;
	} else {
		for (var i=0;i<length;i++) {
			listform.selectPackage[i].disabled = disabled;
			listform.selectPackage[i].checked = false;
		}
	}
}

function doGroupUpdate() {

	var updateform = document.updateform;
	var listform = document.listform;
	updateform.org_id.value = document.searchform.org_id.value;

	var anyPackages = false;
	var allPackages = getRadioSelection(document.updateform.allPackages);
	if (allPackages == 'yes') {
		anyPackages = true;
	} else {
		var div = document.getElementById("PackageListInnerHtml");
		while (div.hasChildNodes()) {
			div.removeChild(div.firstChild);
		}

		var length = listform.selectPackage.length;
		if (length == undefined) {
			if (listform.selectPackage.checked ) {
				anyPackages = true;
				div.appendChild(makeHidden("selectPackage", "", listform.selectPackage.value));
			}
		} else {
			for (var i=0;i<length;i++) {
				if (listform.selectPackage[i].checked){
					anyPackages = true;
					div.appendChild(makeHidden("selectPackage", "", listform.selectPackage[i].value));
				}
			}
		}
	}

	if (!anyPackages) {
		alert('Select at least one Package for updation');
		return;
	}

	var anyBedTypes = false;
	if (updateform.allBedTypes.checked) {
		anyBedTypes = true;
	} else {
		var bedTypeLength = updateform.selectBedType.length;

		for (var i=0; i<bedTypeLength ; i++) {
			if(updateform.selectBedType.options[i].selected){
				anyBedTypes = true;
				break;
			}
		}
	}

	if (!anyBedTypes) {
		alert('Select at least one Bed Type for updation');
		return ;
	}

	if (!updateform.updateTable.checked) {
		alert("Select update option");
		updateform.updateTable.focus();
		return ;
	}


	if (updateform.amount.value=="") {
		alert("Value required for Amount");
		updateform.amount.focus();
		return ;
	}

	if(updateform.amtType.value == '%') {
		if(getAmount(updateform.amount.value) > 100){
			alert("Discount percent cannot be more than 100");
			updateform.amount.focus();
			return false;
		}
	}

	updateform.submit();
}

function doExport() {
	document.exportform.org_id.value = document.searchform.org_id.value;
	return true;
}

function doUpload(formType) {

   if(formType == "uploadform"){
	var form = document.uploadform;
	if (form.xlsChargesForm.value == "") {
		alert("Please browse and select a file to upload");
		return false;
	}
	form.orgId.value = document.searchform.org_id.value;
	}else{
	var form = document.uploadpackageform;
	if (form.xlsDetailsForm.value == "") {
		alert("Please browse and select a file to upload");
		return false;
	   }
	   form.orgId.value = document.searchform.org_id.value;
	}
	form.submit();
}

function setPackageInclusionExprParams() {
	var exprTab = document.getElementById("pkgExprTab");
	var exprRow = document.getElementById("pkgExprRow");
	var td = null;
	var label = null;
	if (exprList.length > 0) {
		for (var i=0; i<exprList.length;i++) {
			if (i%5 == 0) {
				exprRow = exprTab.insertRow(-1);
			}
			td = exprRow.insertCell(-1);
		    label = document.createElement('label');
		    label.textContent = exprList[i].token;
		    td.appendChild(label);
		    exprRow.appendChild(td);
		}
	}
}

function loadDynapackageNamesList() {

	var datasource = new YAHOO.widget.DS_JSArray(eval(dynapackNamesList));
	var autoComp = new YAHOO.widget.AutoComplete('dyna_package_name', 'dynanamesContainer', datasource);
	autoComp.prehighlightClassName = "yui-ac-prehighlight";
	autoComp.typeAhead = true;
	autoComp.allowBrowserAutocomplete = false;
	autoComp.minQueryLength = 0;
	autoComp.maxResultsDisplayed = 20;
	autoComp.autoHighlight = false;
	autoComp.forceSelection = false;
}

function validateImportLimitsFile() {
	if (importlimits.csvLimitsFile.value == '') {
		alert("Please browse and select a file to upload");
		return false;
	}
	importlimits.submit();
}

function doExport() {
	document.exportform.org_id.value = document.searchform.org_id.value;
	return true;
}

function doExportLimits() {
	document.exportlimits.org_id.value = document.searchform.org_id.value;
	return true;
}

function validateCharge() {
    var tableObject = document.getElementById("dynaPackageCharges");
    var rowObject = tableObject.rows[1];
	    for(var i = 0; i < rowObject.cells.length; i++) {
            var index = "charge"+i;
            var element = document.getElementById(index);
			if(element != null) {
			    var charge = element.value;
				if( charge < 0) {
				    showMessage("js.common.message.charge.negative");
			 		return false;
				}
			}
		}
    return true;
}


