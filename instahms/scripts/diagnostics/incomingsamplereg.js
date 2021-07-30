// Gender
var rateDetails = '';
function salChange() {
    var title = getSelText(document.sampleregform.salutation);

    for (var i = 0; i < salutationJSON.length; i++) {
        var item = salutationJSON[i];
        if (title == item["salutation"]) {
            for (var k = 0; k < document.sampleregform.gender.options.length; k++) {
                if (document.sampleregform.gender.options[k].value == item["gender"]) document.sampleregform.gender.selectedIndex = k;
            }
        }
    }
}

function loadSelectBox(selectBox, itemList, dispNameVar, valueVar, departmentId) {
    // clearset the size of the select box
    selectBox.disabled = false;
    selectBox.length = 1;
    selectBox.options[0].text = '...Select..';
    selectBox.options[0].value = '';
    var index = 1;

    var recordfound = false;
    for (var i = 0; i < itemList.length; i++) {
        var item = itemList[i];
        if (item["DDEPT_ID"] == departmentId) {
            selectBox.length += 1;
            recordfound = true;
            selectBox.options[index].text = item[dispNameVar];
            selectBox.options[index].value = item[valueVar];
            index++;
        }
    }

    if (!recordfound) {
        selectBox.length = 1;
    }
}

var index = 1;

function addRecords() {

    var category = document.sampleregform.category.value;
    var testname = document.sampleregform.testnameObj.value;
    var amountDue = document.sampleregform.amountdue;
    var totalDisc = document.sampleregform.totalDiscount1.value;
    var totAmtPay = document.sampleregform.amtPay;
    var billType = document.sampleregform.billType.value;
    var condDoctor = document.sampleregform.conducting_doctorId.value;
    var sampleTypeId = '';
    var sampleType = '';
    if(category == 'DEP_LAB'){
    	originalSampleNo = document.sampleregform.originalSampleNo.value;
    	sampleTypeId = document.getElementById('sampleType').options[document.getElementById('sampleType').selectedIndex].value;
        sampleType = document.getElementById('sampleType').options[document.getElementById('sampleType').selectedIndex].text;
        var originalSampleNo = document.sampleregform.originalSampleNo.value;
	    var outhouseIds = document.getElementsByName('outhouseId');
	    var outhouseTypes = document.getElementsByName('houseType');
	    var orig_sample_nos = document.getElementsByName('orig_sample_no');
      }

    var totAmtDue = 0;
    var totDisc = getPaise(totalDisc.value);
    if (amountDue.value > 0) totAmtDue = amountDue.value;
    var sampleId = '';
    mycounterfrows++;

    if (testname == "") {
        showMessage("js.laboratory.radiology.incomingsampleregistration.required.testname");
        document.sampleregform.testnameObj.focus();
        return false;
    }
    if (sampleTypeId == "" && category == 'DEP_LAB' && sampleNeeded == 'y') {
    	showMessage('js.laboratory.radiology.incomingsampleregistration.required.sampletype');
    	document.getElementById('sampleType').focus();
    	return false;
    }

    if (category == 'DEP_LAB' && isSampleIdAutoGenerateReq != 'Y' && sampleNeeded == 'y') {
        var sampleId = trim(document.sampleregform.insampleId.value);
        if (sampleId == '') {
            showMessage('js.laboratory.radiology.incomingsampleregistration.required.sampleid');
            document.sampleregform.insampleId.focus();
            return false;
        }
    }
	if (category == 'DEP_LAB') {
	    var selectedOuthouseVal = document.getElementById('outhouse').options[document.getElementById('outhouse').selectedIndex].value;
	    var selectedOuthouseTxt = document.getElementById('outhouse').options[document.getElementById('outhouse').selectedIndex].text;
	    var outsourcesCount = document.getElementById('outhouse').options.length;

	    if (houseType == 'O' && selectedOuthouseVal == '') {

	    	showMessage('js.laboratory.radiology.incomingsampleregistration.select.outhouse');
	    	document.getElementById('outhouse').focus();
	    	return false;

	   	    }
	}

    if (category == 'DEP_LAB' && sampleNoGeneration == 'P' && isSampleIdAutoGenerateReq == 'Y' && originalSampleNo != '') {
       	for (var i=0; i<outhouseTypes.length; i++) {
    		if (orig_sample_nos[i].value == originalSampleNo && outhouseTypes[i].value != houseType) {
				showMessage('js.laboratory.radiology.incomingsampleregistration.originalsampleno.notsame.inouthouses');
				document.sampleregform.originalSampleNo.value = '';
				document.sampleregform.originalSampleNo.focus();
				return false;
    		} else if (orig_sample_nos[i].value == originalSampleNo && outhouseTypes[i].value == 'O' &&
    				houseType == 'O' && outhouseIds[i].value != selectedOuthouseVal) {
    			showMessage('js.laboratory.radiology.incomingsampleregistration.originalsampleno.notsame.twodiffouthouses');
				document.sampleregform.originalSampleNo.value = '';
				document.sampleregform.originalSampleNo.focus();
    			return false;
    		}
    	}
    }

    if (conductingDocReqAt == 'O' && condDoctor == '') {
    	showMessage('js.laboratory.radiology.batchconduction.required.conductingdoctor');
    	document.sampleregform.conducting_doctor.focus();
    	return false;
    }

    var testId = '';
    var additionalTestInfo = '';
    var test = '';
    var charge = 0;
    var disc = 0;
    var amt = 0;
    var outsourceDestType = '';

    var mandate_additional_info = 'N';
    for (var i = 0; i < deptWiseTestsjson.length; i++) {
        var record = deptWiseTestsjson[i];
        if (testname == record["TEST_NAME"]) {
            testId = record["TEST_ID"];
			test = record["TEST"];
			mandate_additional_info = record['MANDATE_ADDITIONAL_INFO'];
			additionalTestInfo = record['ADDITIONAL_INFO_REQTS'];

            if ( rateDetails == '' ) {
	            charge = formatAmountValue(record["CHARGE"]);
	            disc = formatAmountValue(record["DISCOUNT"]);
	            amt = formatAmountValue(record["CHARGE"] - record["DISCOUNT"]);//general rateplan charges
            }else {
            	charge = formatAmountValue(rateDetails.actRate);
	            disc = formatAmountValue(rateDetails.discount);
	            amt = formatAmountValue(rateDetails.actRate - rateDetails.discount);
            }
        }
    }
    if (category == 'DEP_LAB'){
		var outhousesAgainstTests = outhousesAgainstTestsJson[testId];
		if (outhousesAgainstTests != undefined) {
			for (var i=0; i<outhousesAgainstTests.length; i++) {
				var outHouseMap = outhousesAgainstTests[i];
				var selectedSourceVal = document.getElementById('outhouse').options[document.getElementById('outhouse').selectedIndex].value;
				if (outHouseMap['OUTSOURCE_DEST_ID'] == selectedSourceVal) {
					outsourceDestType = outHouseMap['OUTSOURCE_DEST_TYPE'];
					break;
				}
			}
		}
    }

    if (category == 'DEP_LAB' && sampleNeeded == 'n' && outsourceDestType == 'C') {
    	showMessage('js.laboratory.radiology.incomingsampleregistration.samplerequiredno.internallabtest');
    	document.sampleregform.testnameObj.focus();
    	return false;
    }

    var tableObj = document.getElementById('testdetails');
    var tr = tableObj.insertRow(tableObj.rows.length - 1);
    tr.setAttribute("id", "row" + index);
    tr.setAttribute("class", "");
    td = tr.insertCell(-1);
    var checkBoxObj = document.createElement("input");
    checkBoxObj.setAttribute("type", "checkbox");
    checkBoxObj.setAttribute("name", "selected");
    checkBoxObj.setAttribute("id", "selected" + index);
    checkBoxObj.setAttribute("value", index);
    checkBoxObj.setAttribute("onChange", "disableRow(this.value)");
    td.appendChild(checkBoxObj);

    var ar = new Array();
	ar['test_id'] = testId;
	ar['test_name'] = testname;
	ar['package_name'] = '';
	ar['is_pkg'] = false;
	ar['pkg_activity_index'] = 0;
	ar['mandate_additional_info'] = mandate_additional_info;
	ar['category'] = category;
	ar['additional_info_reqts'] = additionalTestInfo;


	addRowToTestAdtnlDocs(ar, index);

    totDisc += getPaise(disc);

	if (category == 'DEP_LAB')
    	td = tr.insertCell(-1);

    td = tr.insertCell(-1);
    td.innerHTML = testname;

    td.appendChild(makeHidden('rowIndex', 'rowIndex'+index, 'row'+index));

    var inputObj = document.createElement("input");
    inputObj.setAttribute("type", "hidden");
    inputObj.setAttribute("name", "testId");
    inputObj.setAttribute("id", "testId" + index);
    inputObj.setAttribute("value", testId);
    td.appendChild(inputObj);

    var inputObj = document.createElement("input");
	inputObj.setAttribute("type", "hidden");
	inputObj.setAttribute("name", "no_testsinpack");
	inputObj.setAttribute("id", "no_testsinpack" + index);
	inputObj.setAttribute("value", "");
	td.appendChild(inputObj);

    var inputObj = document.createElement("input");
    inputObj.setAttribute("type", "hidden");
    inputObj.setAttribute("name", "testName");
    inputObj.setAttribute("id", "testName" + index);
    inputObj.setAttribute("value", test);
	td.appendChild(inputObj);

	var inputObj = document.createElement("input");
    inputObj.setAttribute("type", "hidden");
    inputObj.setAttribute("name", "testINpackage");
    inputObj.setAttribute("id", "testINpackage" + index);
    inputObj.setAttribute("value", "n");
	td.appendChild(inputObj);

	var inputObj = document.createElement("input");
    inputObj.setAttribute("type", "hidden");
    inputObj.setAttribute("name", "isPackage");
    inputObj.setAttribute("id", "isPackage" + index);
    inputObj.setAttribute("value", "n");
	td.appendChild(inputObj);

	if (category == 'DEP_LAB') {
		var inputObj = document.createElement("input");
	    inputObj.setAttribute("type", "hidden");
	    inputObj.setAttribute("name", "sampleNeeded");
	    inputObj.setAttribute("id", "sampleNeeded" + index);
	    inputObj.setAttribute("value", sampleNeeded);
	    td.appendChild(inputObj);

		var inputObj = document.createElement("input");
	    inputObj.setAttribute("type", "hidden");
	    inputObj.setAttribute("name", "sampleTypeId");
	    inputObj.setAttribute("id", "sampleTypeId" + index);
	    inputObj.setAttribute("value", sampleNeeded == 'y' ? sampleTypeId : '');
	    td.appendChild(inputObj);

	    var inputObj = document.createElement("input");
    	inputObj.setAttribute("type", "hidden");
    	inputObj.setAttribute("name", "sampleId");
   	 	inputObj.setAttribute("id", "sampleId" + index);
    	inputObj.setAttribute("value", ( sampleNeeded == 'y' ? sampleId : '' ));
    	td.appendChild(inputObj);

    	var inputObj = document.createElement('input');
		inputObj.setAttribute("type", "hidden");
		inputObj.setAttribute("name", "houseType");
		inputObj.setAttribute("id", "houseType" + index);
	    inputObj.setAttribute("value", houseType);
		td.appendChild(inputObj);

		var inputObj = document.createElement('input');
		inputObj.setAttribute("type", "hidden");
		inputObj.setAttribute("name", "outhouseId");
		inputObj.setAttribute("id", "outhouseId" + index);
	    inputObj.setAttribute("value", selectedOuthouseVal);
		td.appendChild(inputObj);

		var inputObj = document.createElement("input");
	    inputObj.setAttribute("type", "hidden");
	    inputObj.setAttribute("name", "outsourceDestType");
	    inputObj.setAttribute("id", "outsourceDestType" + index);
	    inputObj.setAttribute("value", outsourceDestType);
	    td.appendChild(inputObj);

	}

	var inputObj = document.createElement("input");
    inputObj.setAttribute("type", "hidden");
    inputObj.setAttribute("name", "charge");
    inputObj.setAttribute("id", "charge" + index);
    inputObj.setAttribute("value", charge);
	td.appendChild(inputObj);

    var inputObj = document.createElement("input");
    inputObj.setAttribute("type", "hidden");
    inputObj.setAttribute("name", "disc");
    inputObj.setAttribute("id", "disc" + index);
    inputObj.setAttribute("value", disc);
    td.appendChild(inputObj);

    var inputObj = document.createElement("input");
    inputObj.setAttribute("type", "hidden");
    inputObj.setAttribute("name", "ratedisc");
    inputObj.setAttribute("id", "ratedisc" + index);
    inputObj.setAttribute("value", disc);
    td.appendChild(inputObj);

    var inputObj = document.createElement("input");
    inputObj.setAttribute("type", "hidden");
    inputObj.setAttribute("name", "amt");
    inputObj.setAttribute("id", "amt" + index);
    inputObj.setAttribute("value", amt);
    td.appendChild(inputObj);

    var inputObj = document.createElement("input");
	inputObj.setAttribute("type", "hidden");
	inputObj.setAttribute("name", "package_ref");
	inputObj.setAttribute("id", "package_ref" + index);
	inputObj.setAttribute("value", '');
	td.appendChild(inputObj);

	var inputObj = document.createElement("input");
	inputObj.setAttribute("type", "hidden");
	inputObj.setAttribute("name", "conducting_doctor_id");
	inputObj.setAttribute("id", "conducting_doctor_id" + index);
	inputObj.setAttribute("value", condDoctor);
	td.appendChild(inputObj);

    if (category == 'DEP_LAB') {
	    var inputObj = document.createElement("input");
	    inputObj.setAttribute("type", "hidden");
	    inputObj.setAttribute("name", "orig_sample_no");
	    inputObj.setAttribute("id", "orig_sample_no" + index);
	    inputObj.setAttribute("value", document.forms[0].originalSampleNo.value);
	    td.appendChild(inputObj);
	 }

    if (category == 'DEP_LAB') {
        td = tr.insertCell(-1);
        td.innerHTML = ( sampleNeeded == 'y' ? sampleType : '' );
        td = tr.insertCell(-1);
        td.innerHTML = document.forms[0].originalSampleNo.value;
        td = tr.insertCell(-1);
        td.innerHTML = ( sampleNeeded == 'y' ? sampleId : '' );
        td = tr.insertCell(-1);
  		td.innerHTML = (houseType == 'O' ? selectedOuthouseTxt : '');
    }

    td = tr.insertCell(-1);
    td.setAttribute("class", 'number');
    td.innerHTML = charge;

	amountDue.value = formatAmountValue(eval(totAmtDue) + eval(amt));
	totAmtPay.value = amountDue.value;
	document.getElementById('totalAmount1').value = amountDue.value;
	document.getElementById('totalDiscount1').value = formatAmountPaise(totDisc);
	document.getElementById('totalAmount').innerHTML = amountDue.value;

    td = tr.insertCell(-1);
    td.setAttribute("class", 'number');
    td.innerHTML = disc;

    td = tr.insertCell(-1);
    td.setAttribute("class", 'number');
    td.innerHTML = amt;

	 if (category == 'DEP_LAB') {
	    td = tr.insertCell(-1);
		var editImg = document.createElement("img");
		editImg.setAttribute("src", cpath + "/icons/Edit.png");
		editImg.setAttribute("name", "editbut");
		editImg.setAttribute("id", "editbut");
		editImg.setAttribute("onclick","showEditDialog(this)");
		editImg.setAttribute("class", "button");
		td.appendChild(editImg);
	}

    index++;
    document.forms[0].testnameObj.value = '';
    if (category == 'DEP_LAB') {
        document.getElementById('sampleType').value = '';
        document.getElementById('originalSampleNo').value = '';
        fillOuthouses(null);
    }
    if (document.forms[0].insampleId != undefined) document.forms[0].insampleId.value = '';
    document.sampleregform.conducting_doctorId.value = '';
    document.getElementById('conducting_doctor').value = '';
    document.getElementById('conducting_doctor').disabled = true;

    addDialog.cfg.setProperty("context", [document.getElementById("loadDialog"), "tr", "br"], false);
    getAddDialog(document.getElementById("loadDialog"));
    resetTotals();
}

function addPackage() {
	var tableObj = document.getElementById('testdetails');
	var packageId = testID;
	var packname = '';
	var isPkg = false;
	var packageRowIndex = index;

	if (!isTemplatePackage) {
		isPkg = true;
		packname = document.sampleregform.testnameObj.value;
		var amountDue = document.sampleregform.amountdue;
	    var totalDisc = document.sampleregform.totalDiscount1.value;
	    var totAmtPay = document.sampleregform.amtPay;
	    var billType = document.sampleregform.billType.value;

		var totAmtDue = 0;
	    var totDisc = getPaise(totalDisc.value);
	    if (amountDue.value > 0) totAmtDue = amountDue.value;

	    var tr = tableObj.insertRow(tableObj.rows.length - 1);
	    tr.setAttribute("id", "row" + index);
	    tr.setAttribute("class", "");
	    td = tr.insertCell(-1);
	    var checkBoxObj = document.createElement("input");
	    checkBoxObj.setAttribute("type", "checkbox");
	    checkBoxObj.setAttribute("name", "selected");
	    checkBoxObj.setAttribute("id", "selected" + index);
	    checkBoxObj.setAttribute("value", index);
	    checkBoxObj.setAttribute("onChange", "disableRow(this.value)");
	    td.appendChild(checkBoxObj);

	    var packId = '';
	    var charge = 0;
	    var disc = 0;
	    var amt = 0;
	    for (var i = 0; i < deptWiseTestsjson.length; i++) {
	        var record = deptWiseTestsjson[i];
	        if (packname == record["TEST_NAME"]) {
	            packId = record["TEST_ID"];

	            if ( rateDetails == '' ) {
		            charge = formatAmountValue(record["CHARGE"]);
		            disc = formatAmountValue(record["DISCOUNT"]);
		            amt = formatAmountValue(record["CHARGE"] - record["DISCOUNT"]);//general rateplan charges
	            }else {
	            	charge = formatAmountValue(rateDetails.actRate);
		            disc = formatAmountValue(rateDetails.discount);
		            amt = formatAmountValue(rateDetails.actRate - rateDetails.discount);
	            }
	        }
	    }

	    totDisc += getPaise(disc);

	    td = tr.insertCell(-1);
	    td.innerHTML = packname;

	    td.appendChild(makeHidden('rowIndex', 'rowIndex'+index, 'row'+index));

	    var inputObj = document.createElement("input");
	    inputObj.setAttribute("type", "hidden");
	    inputObj.setAttribute("name", "testId");
	    inputObj.setAttribute("id", "testId" + index);
	    inputObj.setAttribute("value", packId);
	    td.appendChild(inputObj);

		var inputObj = document.createElement("input");
		inputObj.setAttribute("type", "hidden");
		inputObj.setAttribute("name", "no_testsinpack");
		inputObj.setAttribute("id", "no_testsinpack" + index);
		inputObj.setAttribute("value", testList.length);
		td.appendChild(inputObj);

	    var inputObj = document.createElement("input");
	    inputObj.setAttribute("type", "hidden");
	    inputObj.setAttribute("name", "sampleTypeId");
	    inputObj.setAttribute("id", "sampleTypeId" + index);
	    inputObj.setAttribute("value", "");
	    td.appendChild(inputObj);

	    var inputObj = document.createElement("input");
	    inputObj.setAttribute("type", "hidden");
	    inputObj.setAttribute("name", "sampleId");
	    inputObj.setAttribute("id", "sampleId" + index);
	    inputObj.setAttribute("value", "");
	    td.appendChild(inputObj);

	    var inputObj = document.createElement("input");
	   	inputObj.setAttribute("type", "hidden");
	   	inputObj.setAttribute("name", "orig_sample_no");
	  	 	inputObj.setAttribute("id", "orig_sample_no" + index);
	   	inputObj.setAttribute("value", "");
	   	td.appendChild(inputObj);

	   	var inputObj = document.createElement("input");
	   	inputObj.setAttribute("type", "hidden");
	   	inputObj.setAttribute("name", "sampleNeeded");
	   	inputObj.setAttribute("id", "sampleNeeded" + index);
	   	inputObj.setAttribute("value", "n");
	   	td.appendChild(inputObj);

	    var inputObj = document.createElement("input");
	    inputObj.setAttribute("type", "hidden");
	    inputObj.setAttribute("name", "isPackage");
	    inputObj.setAttribute("id", "isPackage" + index);
	    inputObj.setAttribute("value", "y");
		td.appendChild(inputObj);

		var inputObj = document.createElement("input");
	    inputObj.setAttribute("type", "hidden");
	    inputObj.setAttribute("name", "testINpackage");
	    inputObj.setAttribute("id", "testINpackage" + index);
	    inputObj.setAttribute("value", "n");
		td.appendChild(inputObj);

		var inputObj = document.createElement("input");
    	inputObj.setAttribute("type", "hidden");
    	inputObj.setAttribute("name", "houseType");
   	 	inputObj.setAttribute("id", "houseType" + index);
    	inputObj.setAttribute("value", "");
    	td.appendChild(inputObj);

    	var inputObj = document.createElement("input");
    	inputObj.setAttribute("type", "hidden");
    	inputObj.setAttribute("name", "outhouseId");
   	 	inputObj.setAttribute("id", "outhouseId" + index);
    	inputObj.setAttribute("value", "");
    	td.appendChild(inputObj);

       	var inputObj = document.createElement("input");
    	inputObj.setAttribute("type", "hidden");
    	inputObj.setAttribute("name", "outsourceDestType");
   	 	inputObj.setAttribute("id", "outsourceDestType" + index);
    	inputObj.setAttribute("value", "");
    	td.appendChild(inputObj);

    	var inputObj = document.createElement("input");
		inputObj.setAttribute("type", "hidden");
		inputObj.setAttribute("name", "package_ref");
		inputObj.setAttribute("id", "package_ref" + index);
		inputObj.setAttribute("value", isTemplatePackage ? '' : packageRef);
		td.appendChild(inputObj);

	    var inputObj = document.createElement("input");
	    inputObj.setAttribute("type", "hidden");
	    inputObj.setAttribute("name", "disc");
	    inputObj.setAttribute("id", "disc" + index);
	    inputObj.setAttribute("value", disc);
	    td.appendChild(inputObj);

	    var inputObj = document.createElement("input");
	    inputObj.setAttribute("type", "hidden");
	    inputObj.setAttribute("name", "ratedisc");
	    inputObj.setAttribute("id", "ratedisc" + index);
	    inputObj.setAttribute("value", disc);
	    td.appendChild(inputObj);

		// this is of no use, conducting doctors appended to the last cell.
	    var inputObj = document.createElement("input");
		inputObj.setAttribute("type", "hidden");
		inputObj.setAttribute("name", "conducting_doctor_id");
		inputObj.setAttribute("id", "conducting_doctor_id" + index);
		inputObj.setAttribute("value", "");
		td.appendChild(inputObj);

	    var inputObj = document.createElement("input");
	    inputObj.setAttribute("type", "hidden");
	    inputObj.setAttribute("name", "amt");
	    inputObj.setAttribute("id", "amt" + index);
	    inputObj.setAttribute("value", amt);
	    td.appendChild(inputObj);
		td = tr.insertCell(-1);
		td.setAttribute("colspan", '5');

	    td = tr.insertCell(-1);
	    td.setAttribute("class", 'number');
	    td.innerHTML = charge;
	    var inputObj = document.createElement("input");
	    inputObj.setAttribute("type", "hidden");
	    inputObj.setAttribute("name", "charge");
	    inputObj.setAttribute("id", "charge" + index);
	    inputObj.setAttribute("value", charge);

		amountDue.value = formatAmountValue(eval(totAmtDue) + eval(amt));
		totAmtPay.value = amountDue.value;
		document.getElementById('totalAmount1').value = amountDue.value;
		document.getElementById('totalDiscount1').value = formatAmountPaise(totDisc);
		document.getElementById('totalAmount').innerHTML = amountDue.value;

	    td.appendChild(inputObj);

	    td = tr.insertCell(-1);
	    td.setAttribute("class", 'number');
	    td.innerHTML = disc;

	    td = tr.insertCell(-1);
	    td.setAttribute("class", 'number');
	    td.innerHTML = amt;
	    index++;
    }
    if (testList != null && testList != '') {
		var len = testList.length;
		for (var i = 0; i < len; i++) {

			var chargeList = '';
			var record = testList[i];
			tr = tableObj.insertRow(tableObj.rows.length - 1);

			var ar = new Array();
			ar['test_id'] = record.test_id;
			ar['test_name'] = record.test_name;
			ar['package_name'] = packname;
			ar['is_pkg'] = isPkg;
			ar['pkg_activity_index'] = i;
			ar['mandate_additional_info'] = record.mandate_additional_info;
			ar['additional_info_reqts'] = record.additional_info_reqts;
			ar['category'] = category;
			addRowToTestAdtnlDocs(ar, isTemplatePackage ? index : packageRowIndex);

			tr.setAttribute("id", "row" + index);
			if(isTemplatePackage) {
				td = tr.insertCell(-1);
			    var checkBoxObj = document.createElement("input");
			    checkBoxObj.setAttribute("type", "checkbox");
			    checkBoxObj.setAttribute("name", "selected");
			    checkBoxObj.setAttribute("id", "selected" + index);
			    checkBoxObj.setAttribute("value", index);
			    checkBoxObj.setAttribute("onChange", "disableRow(this.value)");
			    td.appendChild(checkBoxObj);
			    td = tr.insertCell(-1);
			    td = tr.insertCell(-1);
			} else {
				td = tr.insertCell(-1);
				td.setAttribute("class", "indent");

				var inputObj = document.createElement("input");
			    inputObj.setAttribute("type", "hidden");
			    inputObj.setAttribute("name", "selected");
			    inputObj.setAttribute("id", "selected" + index);
			    inputObj.setAttribute("value", index);
			    td.appendChild(inputObj);

				td = tr.insertCell(-1);
				td.setAttribute("class", "indent");
				td = tr.insertCell(-1);
				td.setAttribute("class", "subResult");
				td.setAttribute("valign", "top");
			}


			td.innerHTML = record.test_name;

			// if it is package item add the package row index to the package items as well.
			var tempIndex = (isTemplatePackage ? index : packageRowIndex);
			// hidden input element 'rowIndex' : id should contain it's actual row Index.(is used for disabling row)
			// where as it's element value should contain package row index. (is used for saving the test documents.)
			td.appendChild(makeHidden('rowIndex', 'rowIndex'+index, 'row'+tempIndex));

			var inputObj = document.createElement("input");
    		inputObj.setAttribute("type", "hidden");
   			inputObj.setAttribute("name", "testId");
		    inputObj.setAttribute("id", "testId" + index);
		    inputObj.setAttribute("value", record.test_id);
		    td.appendChild(inputObj);

			var inputObj = document.createElement("input");
			inputObj.setAttribute("type", "hidden");
			inputObj.setAttribute("name", "no_testsinpack");
			inputObj.setAttribute("id", "no_testsinpack" + index);
			inputObj.setAttribute("value", "");
			td.appendChild(inputObj);

		    var inputObj = document.createElement("input");
    		inputObj.setAttribute("type", "hidden");
    		inputObj.setAttribute("name", "testName");
		    inputObj.setAttribute("id", "testName" + index);
		    inputObj.setAttribute("value", record.test);
			td.appendChild(inputObj);

			var inputObj = document.createElement("input");
		    inputObj.setAttribute("type", "hidden");
		    inputObj.setAttribute("name", "isPackage");
		    inputObj.setAttribute("id", "isPackage" + index);
		    inputObj.setAttribute("value", "n");
			td.appendChild(inputObj);

			var inputObj = document.createElement("input");
		    inputObj.setAttribute("type", "hidden");
		    inputObj.setAttribute("name", "testINpackage");
		    inputObj.setAttribute("id", "testINpackage" + index);
		    inputObj.setAttribute("value", isTemplatePackage?"n":"y");
			td.appendChild(inputObj);

			var charge = 0;
	    	var disc = 0;
	    	var amt = 0;
			if(isTemplatePackage) {
				var type = category == 'DEP_LAB' ? 'Laboratory' : 'Radiology';
				var url = cpath + '/master/orderItems.do?method=getItemCharges'+
				'&type='+type+'&id='+encodeURIComponent(record.test_id) +
				'&bedType=GENERAL&orgId='+document.getElementById("bill_rate_plan_id").value+'&visitType=in';
				var ajaxobj = newXMLHttpRequest();
				ajaxobj.open("POST", url.toString(), false);
				ajaxobj.send(null);
				if (ajaxobj) {
					if (ajaxobj.readyState == 4) {
						if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
							eval("chargeList =" + ajaxobj.responseText);
						}
					}
				}

				charge = formatAmountValue(chargeList[0].actRate);
				disc = formatAmountValue(chargeList[0].discount);
				amt = formatAmountValue(chargeList[0].actRate - chargeList[0].discount);
			}
			var inputObj = document.createElement("input");
		    inputObj.setAttribute("type", "hidden");
		    inputObj.setAttribute("name", "charge");
		    inputObj.setAttribute("id", "charge" + index);
		    inputObj.setAttribute("value", isTemplatePackage?charge:'');
			td.appendChild(inputObj);

			var inputObj = document.createElement("input");
		    inputObj.setAttribute("type", "hidden");
		    inputObj.setAttribute("name", "disc");
		    inputObj.setAttribute("id", "disc" + index);
		    inputObj.setAttribute("value", isTemplatePackage?disc:'');
		    td.appendChild(inputObj);

		    var inputObj = document.createElement("input");
		    inputObj.setAttribute("type", "hidden");
		    inputObj.setAttribute("name", "amt");
		    inputObj.setAttribute("id", "amt" + index);
		    inputObj.setAttribute("value", isTemplatePackage?amt:'');
		    td.appendChild(inputObj);

		    var inputObj = document.createElement("input");
		    inputObj.setAttribute("type", "hidden");
		    inputObj.setAttribute("name", "ratedisc");
		    inputObj.setAttribute("id", "ratedisc" + index);
		    inputObj.setAttribute("value", isTemplatePackage?disc:'');
		    td.appendChild(inputObj);

			var inputObj = document.createElement("input");
	    	inputObj.setAttribute("type", "hidden");
	    	inputObj.setAttribute("name", "sampleNeeded");
	    	inputObj.setAttribute("id", "sampleNeeded" + index);
	    	inputObj.setAttribute("value", record.sample_needed);
	    	td.appendChild(inputObj);

		    var inputObj = document.createElement("input");
       		inputObj.setAttribute("type", "hidden");
        	inputObj.setAttribute("name", "sampleTypeId");
        	inputObj.setAttribute("id", "sampleTypeId" + index);
        	inputObj.setAttribute("value", record.sample_type_id);
        	td.appendChild(inputObj);

        	var inputObj = document.createElement("input");
		    inputObj.setAttribute("type", "hidden");
		    inputObj.setAttribute("name", "sampleId");
		    inputObj.setAttribute("id", "sampleId" + index);
		    inputObj.setAttribute("value", "");
		    td.appendChild(inputObj);

		    var inputObj = document.createElement("input");
	    	inputObj.setAttribute("type", "hidden");
	    	inputObj.setAttribute("name", "orig_sample_no");
	   	 	inputObj.setAttribute("id", "orig_sample_no" + index);
	    	inputObj.setAttribute("value", "");
	    	td.appendChild(inputObj);

	    	var inputObj = document.createElement("input");
	    	inputObj.setAttribute("type", "hidden");
	    	inputObj.setAttribute("name", "houseType");
	   	 	inputObj.setAttribute("id", "houseType" + index);
	    	inputObj.setAttribute("value", record.house_status);
	    	td.appendChild(inputObj);

	    	var inputObj = document.createElement("input");
	    	inputObj.setAttribute("type", "hidden");
	    	inputObj.setAttribute("name", "outhouseId");
	   	 	inputObj.setAttribute("id", "outhouseId" + index);
	    	inputObj.setAttribute("value", "");
	    	td.appendChild(inputObj);

	    	var inputObj = document.createElement("input");
	    	inputObj.setAttribute("type", "hidden");
	    	inputObj.setAttribute("name", "outsourceDestType");
	   	 	inputObj.setAttribute("id", "outsourceDestType" + index);
	    	inputObj.setAttribute("value", "");
	    	td.appendChild(inputObj);

			var conductingDoctor = '';
			var els = document.getElementsByName('packageActivityIndex');
			if (isPackage) {
				for (var k=0; k<els.length; k++) {
					if (parseInt(els[k].value) == i) {
						conductingDoctor = document.getElementsByName('packCondDoctorId')[k].value;
					}
				}
			}
	    	var inputObj = document.createElement("input");
			inputObj.setAttribute("type", "hidden");
			inputObj.setAttribute("name", "conducting_doctor_id");
			inputObj.setAttribute("id", "conducting_doctor_id" + index);
			inputObj.setAttribute("value", conductingDoctor);
			td.appendChild(inputObj);

	    	var inputObj = document.createElement("input");
			inputObj.setAttribute("type", "hidden");
			inputObj.setAttribute("name", "package_ref");
			inputObj.setAttribute("id", "package_ref" + index);
			inputObj.setAttribute("value", isTemplatePackage ? '' : packageRef);
			td.appendChild(inputObj);

			td = tr.insertCell(-1);
			td.innerHTML = record.sample_type;
			td = tr.insertCell(-1);
			td = tr.insertCell(-1);
			td = tr.insertCell(-1);

			td = tr.insertCell(-1);
			td.setAttribute("class", 'number');
			td.innerHTML = isTemplatePackage?charge:'';
			td = tr.insertCell(-1);
			td.setAttribute("class", 'number');
			td.innerHTML = isTemplatePackage?disc:'';
			td = tr.insertCell(-1);
			td.setAttribute("class", 'number');
			td.innerHTML = isTemplatePackage?amt:'';

			td = tr.insertCell(-1);
			var editImg = document.createElement("img");
			editImg.setAttribute("src", cpath + "/icons/Edit.png");
			editImg.setAttribute("name", "editbut");
			editImg.setAttribute("id", "editbut");
			editImg.setAttribute("onclick","showEditDialog(this)");
			editImg.setAttribute("class", "button");
			td.appendChild(editImg);

			index++;
		}
	}
    document.forms[0].testnameObj.value = '';
    if (category == 'DEP_LAB') {
        document.getElementById('sampleType').value = '';
        document.getElementById('originalSampleNo').value = '';
    }
    if (document.forms[0].insampleId != undefined) document.forms[0].insampleId.value = '';

    addDialog.cfg.setProperty("context", [document.getElementById("loadDialog"), "tr", "br"], false);
    getAddDialog(document.getElementById("loadDialog"));
    resetTotals();
}

function setTestDocRowEdited(obj) {
	var row = getThisRow(obj);
	getElementByName(row, 'ad_test_row_edited').value = 'true';
}

var tadColIndex=0;
var TAD_PKG_NAME = tadColIndex++,
TAD_TEST_NAME = tadColIndex++,
TAD_ADDITIONAL_INFO = tadColIndex++,
TAD_NOTES = tadColIndex++,
TAD_BROWSE = tadColIndex++,
TAD_TRASH_COL = tadColIndex++,
TAD_PLUS_COL = tadColIndex++;

function addRowToTestAdtnlDocs(order, prescId) {
	var orderTable = order.addTo;
	var ad_table = document.getElementById('ad_test_info_table');
	var item = findInList2(deptWiseTestsjson, "TYPE", "DIA", "TEST_ID", order.test_id);
	// if it is not applicable for rateplan then skip
	if (item == null) return;

	if (order.mandate_additional_info == 'O') {
		document.getElementById('testInfoDialog').style.display = 'block';
		var id = ad_table.rows.length-2;
		var templateRow = ad_table.rows[ad_table.rows.length-1];
		var row = templateRow.cloneNode(true);

		row.style.display = '';
		addClassName(row, 'added');
		addClassName(row, 'mainRow');
		ad_table.tBodies[0].insertBefore(row, templateRow);

		if (order.is_pkg == true)
			setNodeText(row.cells[TAD_PKG_NAME], order.package_name, 20);

		setNodeText(row.cells[TAD_TEST_NAME], order.test_name, 20);
		setNodeText(row.cells[TAD_ADDITIONAL_INFO], order.additional_info_reqts, 20);

		getElementByName(row, 'ad_main_row_id').value = 'row'+prescId;
		getElementByName(row, 'ad_test_id').value = order.test_id;
		getElementByName(row, 'ad_package_activity_index').value = empty(order.pkg_activity_index) ? 0 : order.pkg_activity_index;
		getElementByName(row, 'ad_test_category').value = order.category;
		getElementByName(row, 'ad_test_name').value = order.test_name;
		getElementByName(row, 'ad_test_info_reqts').value = order.additional_info_reqts;

		var plusImg = getElementByName(row, 'btnAddItem');
		plusImg.style.display = 'none';
		cloneTestDocRow(plusImg, templateRow, true);
		renameFileUploadEl();
	}
}

//called when a new row is added or deleted..
function renameFileUploadEl() {
	var table = document.getElementById('ad_test_info_table');
	var uploadEls = YAHOO.util.Dom.getElementsByClassName("testFileUpload", 'input', table);
	for (var i=0; i<uploadEls.length-1; i++) {
		var el = uploadEls[i];
		var row = getThisRow(el);

		var id = row.rowIndex - 1;
		el.setAttribute("name", "ad_test_file_upload[" + id + "]");
	}
}

function cloneTestDocRow(obj, addBeforeThisRow, isDummyRow) {
	var templateRow = getThisRow(obj);

	if (getElementByName(templateRow, 'ad_main_row_id').disabled) {
		alert('Test is marked for delete. Add/Edit/Delete of document is not allowed');
		return false;
	}

	var ad_table = document.getElementById('ad_test_info_table');
	var row = templateRow.cloneNode(true);
	row.style.display = '';
	ad_table.tBodies[0].insertBefore(row, empty(addBeforeThisRow) ? templateRow : addBeforeThisRow);

	var uploadEl = YAHOO.util.Dom.getElementsByClassName("testFileUpload", 'input', row)[0];
	var trashImg = row.cells[TAD_TRASH_COL].getElementsByTagName("A")[0];
	var plusImg = getElementByName(row, 'btnAddItem');

	removeClassName(row, "mainRow");
	if (isDummyRow) {
		addClassName(row, "dummyRow");
		uploadEl.style.display = 'none';
		trashImg.style.display = 'none';
		plusImg.style.display = 'block';
	} else {
		removeClassName(row, "dummyRow");
		trashImg.style.display = 'block';
		uploadEl.style.display = 'block';
		plusImg.style.display = 'none';
	}

	addClassName(row.cells[TAD_PKG_NAME], 'indent');
	addClassName(row.cells[TAD_TEST_NAME], 'indent');
	addClassName(row.cells[TAD_ADDITIONAL_INFO], 'indent');

	setNodeText(row.cells[TAD_PKG_NAME], '');
	setNodeText(row.cells[TAD_TEST_NAME], '');
	setNodeText(row.cells[TAD_ADDITIONAL_INFO], '');

	// hide the clinical notes icon.
	var notesImg = row.cells[TAD_NOTES].getElementsByTagName("A")[0];
	if (notesImg) {
		notesImg.style.display = 'none';
	}

	// cloned using + button
	if (empty(isDummyRow)) {
		renameFileUploadEl();
	}
}

function cancelTestAdtnlDoc(obj) {
	var row = getThisRow(obj);

	var table = document.getElementById('ad_test_info_table');
	var cancelled = getElementByName(row, "ad_test_doc_delete").value;

	if (getElementByName(row, 'ad_main_row_id').disabled) {
		alert('Test is marked for delete. Add/Edit/Delete of document is not allowed');
		return false;
	}

	if (cancelled == 'false') {
		if (!allowTestAdtnlDocRowDelete(row)) {
			alert("You are not allowed to delete all the documents. Atleast one document is required.");
			return false;
		}
		cancelled = 'true';
	} else {
		cancelled = 'false'
	}

	if (!YAHOO.util.Dom.hasClass(row, 'mainRow')) {
		// delete the row directly if it is not a main record.
		row.parentNode.removeChild(row);
		renameFileUploadEl(); // rename the file elements again since one row is physically deleted from the grid.
		return false;
	}

	getElementByName(row, 'ad_test_doc_delete').value = cancelled;
	getElementByName(row, 'ad_test_row_edited').value = 'true';
	addClassName(row, 'edited');

	var trashImg = row.cells[TAD_TRASH_COL].getElementsByTagName("img");
	if (trashImg && trashImg[0]) {
		if (cancelled == 'true')
			trashImg[0].src =  cpath+"/icons/undo_delete.gif";
		else
			trashImg[0].src = cpath+"/icons/delete.gif";
	}

	return false;
}

// allow deleting of selected test document, if atleast one document or clinical notes found in remaining rows.
function allowTestAdtnlDocRowDelete(row) {
	var noOfFileUploads = 1;
	if (YAHOO.util.Dom.hasClass(row, 'mainRow')) {
		var clinical_notes = getElementByName(row, 'ad_clinical_notes').value;
		if (!empty(clinical_notes)) return true;

		// search for any row, which is containing a exising or newly added document.
		// if found, currently selected document allowed for delete.
		var nextRow = YAHOO.util.Dom.getNextSibling(row);
		while (!YAHOO.util.Dom.hasClass(nextRow, 'dummyRow')) {
			var deleted = getElementByName(nextRow, "ad_test_doc_delete").value;
			var fileName = YAHOO.util.Dom.getElementsByClassName("testFileUpload", 'input', nextRow)[0].value;

			if (deleted == 'false' && !empty(fileName) ) return true;

			if (deleted == 'false') {
				noOfFileUploads++;
			}

			nextRow = YAHOO.util.Dom.getNextSibling(nextRow);
		}
	} else {
		// this is some where in the middle row selected,
		// search for document upwards and downwards
		var nextRow = YAHOO.util.Dom.getNextSibling(row);
		while (!YAHOO.util.Dom.hasClass(nextRow, 'dummyRow')) {
			var deleted = getElementByName(nextRow, "ad_test_doc_delete").value;
			var fileName = YAHOO.util.Dom.getElementsByClassName("testFileUpload", 'input', nextRow)[0].value;

			if (deleted == 'false' && !empty(fileName) ) return true;

			if (deleted == 'false') {
				noOfFileUploads++;
			}

			nextRow = YAHOO.util.Dom.getNextSibling(nextRow);
		}
		// no document found downwards. so search continues upwards
		var prevRow = YAHOO.util.Dom.getPreviousSibling(row);
		while (prevRow.rowIndex != 0 && !YAHOO.util.Dom.hasClass(prevRow, 'dummyRow')) {
			var deleted = getElementByName(prevRow, "ad_test_doc_delete").value;
			var fileName = YAHOO.util.Dom.getElementsByClassName("testFileUpload", 'input', prevRow)[0].value;

			if (deleted == 'false' && !empty(fileName) ) return true;

			if (deleted == 'false') {
				noOfFileUploads++;
			}

			// reached main row, even if clinical notes found allow deleting the document.
			if (YAHOO.util.Dom.hasClass(prevRow, "mainRow")) {
				if (!empty(getElementByName(prevRow,"ad_clinical_notes").value))
					return true;
			}
			prevRow = YAHOO.util.Dom.getPreviousSibling(prevRow);
		}
	}
	// more than one empty document rows exists. so no harm deleting the selected row.
	if (noOfFileUploads > 1)
		return true;
	return false;
}


var testAdInfoDialog = null;
function initTestAdditionalInfoDialog() {
	var dialogDiv = document.getElementById("addTestAddiotionalInfoDialog");
	if (dialogDiv == undefined) return;

	dialogDiv.style.display = 'block';
	testAdInfoDialog = new YAHOO.widget.Dialog("addTestAddiotionalInfoDialog",
			{	width:"600px",
				context : ["addTestAddiotionalInfoDialog", "tr", "br"],
				visible:false,
				modal:true,
				fixedcenter: false,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('adTestAdInfoOk', 'click', updateTestAdInfo, testAdInfoDialog, true);
	YAHOO.util.Event.addListener('adTestAdInfoCancel', 'click', cancelTestAdInfo, testAdInfoDialog, true);

	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:cancelTestAdInfo,
	                                                scope:testAdInfoDialog,
	                                                correctScope:true } );
	testAdInfoDialog.cancelEvent.subscribe(cancelTestAdInfo);
	testAdInfoDialog.cfg.setProperty("keylisteners", [escKeyListener]);
	testAdInfoDialog.render();
}


function showTestAdInfoDialog(obj) {
	var row = getThisRow(obj);

	if (getElementByName(row, 'ad_main_row_id').disabled) {
		alert('Test is marked for delete. Add/Edit/Delete of document is not allowed');
		return false;
	}

	addClassName(row, 'editing');
	testAdInfoDialog.cfg.setProperty('context', [obj, 'tr', 'tl'], false);
	testAdInfoDialog.show();

	document.getElementById('adTestAdInfoRowId').value = row.rowIndex - 1;
	document.getElementById('d_test_additional_info').value = getElementByName(row, 'ad_clinical_notes').value;
	document.getElementById('test_info').innerHTML = getElementByName(row, 'ad_test_info_reqts').value;
	document.getElementById('d_test_additional_info').focus();
	return false;
}

function updateTestAdInfo() {
	var id = document.getElementById('adTestAdInfoRowId').value;
	var table = document.getElementById('ad_test_info_table');
	var row = table.rows[parseInt(id) + 1]

	addClassName(row, 'edited');
	var notes = document.getElementById('d_test_additional_info').value;
	var testInfo = getElementByName(row, 'ad_test_info_reqts').value;
	notes = notes.replace(/^\s*[\r\n]*/gm, "");

	document.getElementsByName('ad_test_info_reqts')[id].value = testInfo;
	document.getElementsByName('ad_clinical_notes')[id].value = notes;
	document.getElementsByName('ad_test_row_edited')[id].value = 'true';
	document.getElementsByName('ad_notes_entered')[id].value = 'true';

	setNodeText(row.cells[TAD_ADDITIONAL_INFO], testInfo, 20);
	setNodeText(row.cells[TAD_NOTES], notes, 20);

	testAdInfoDialog.cancel();
	return false;
}

function cancelTestAdInfo() {
	var id = document.getElementById('adTestAdInfoRowId').value;
	var table = document.getElementById('ad_test_info_table');
	var row = table.rows[parseInt(id) + 1]

	removeClassName(row, 'editing');
	testAdInfoDialog.hide();
	return false;
}

function onApplyItemDiscPer() {

    if (!validateDecimal(document.forms[0].itemDiscPer, getString("js.laboratory.radiology.incomingsampleregistration.discountpercent.validamt"), 2)) return false;
    for (var i = 1; i <= getNumTests(); i++) {
    	var test_in_package = document.getElementById('testINpackage' +i).value;
    	var is_package = document.getElementById('isPackage' +i).value;
    	if(test_in_package == 'n')  {
	        var disCheck = document.getElementById('selected' + i); // getIndexedFormElement(document.forms[0], "selected",i);
	        var chrgPaise, charge;
	        var chargeObj = document.getElementById('charge' + i);

	        chrgPaise = getPaise(chargeObj.value);
	        var discPer = getAmount(document.forms[0].itemDiscPer.value);
	        if (discPer > 100) {
	            showMessage("js.laboratory.radiology.incomingsampleregistration.discount.notgreater100");
	            return;
	        }
	        var discPaise = (chrgPaise * discPer) / 100;
			if(trimAll(document.forms[0].itemDiscPer.value) == ''){
	        	discPaise = getPaise(document.getElementById('ratedisc' + i).value);
	        }
	        var discObj = document.getElementById('disc' + i);
	        var amtObj = document.getElementById('amt' + i);
	        var discCol = category == 'DEP_LAB' ? (is_package == 'y' ? 4 : 8) : 3;
	        var amtCol = category == 'DEP_LAB' ? (is_package == 'y' ? 5 : 9) : 4;
	        if (!disCheck.checked) {
	            setNodeText(document.getElementById('row' + i).cells[discCol], formatAmountPaise(discPaise));
	            discObj.value = formatAmountPaise(discPaise);
	            amtObj.value = formatAmountPaise((chrgPaise - discPaise));
	            setNodeText(document.getElementById('row' + i).cells[amtCol], formatAmountPaise((chrgPaise - discPaise)));
	        }
        }
    }
    resetTotals();
}

function resetTotals() {

    var amountDue = document.forms[0].amountdue;
    var totAmtPay = document.forms[0].amtPay;
    var billType = document.forms[0].billType.value;
    var totAmtPaise = 0;
    var totDiscPaise = 0;
    var selected = document.getElementsByName("selected");
    var amt = document.getElementsByName("amt");
    var disc = document.getElementsByName("disc");

    for (var i = 0; i < selected.length; i++) {
        if ( selected[i] && !selected[i].checked) {
            totAmtPaise += getPaise(amt[i].value);
            totDiscPaise += getPaise(disc[i].value);
        }
    }

    document.getElementById('totalAmount1').value = formatAmountPaise(totAmtPaise);
    document.getElementById('totalDiscount1').value = formatAmountPaise(totDiscPaise);
    document.getElementById('totalAmount').innerHTML = formatAmountPaise(totAmtPaise);

    if (billType == "BN") {
        amountDue.value = formatAmountPaise(totAmtPaise);
        totAmtPay.value = formatAmountPaise(totAmtPaise);
    }
    if (category == 'DEP_LAB') {
		var selectElement = document.getElementById('outhouse');
		for (var j=selectElement.options.length; j>0; j--) {
			selectElement.remove(j);
		}
	}
    resetPayments();
}

/**
 * For billPaymentDetails tag, the following functions have to be defined.
 * resetTotalsForPayments() -- This function calls getTotalAmount() & getTotalAmountDue()
 * to set the total_AmtPaise and total_AmtDuePaise values for validations in tag.
 * And set the total payment amount.
 */

function resetPayments() {

    resetTotalsForPayments();

    // for bill now bill, auto-set the amount to be paid by patient.
    if (document.sampleregform.billType.value == 'BN') {
        // if a single payment mode exists, update that with the due amount automatically
        setTotalPayAmount();
    }
}

function getTotalAmount() {
    return getPaise(document.getElementById("amtPay").value);
}

function getTotalAmountDue() {
    return getPaise(document.getElementById("amountdue").value);
}

function getNumTests() {
    return document.getElementById("testdetails").rows.length - 2;
}

function disableRow(valueIndex) {
    var id = "testId" + valueIndex;
    var checked = document.getElementById(id).checked;
    var category = document.forms[0].category.value;
    var amountDue = document.forms[0].amountdue;
    var totAmtPay = document.forms[0].amtPay;
    var billType = document.forms[0].billType.value;

    var testId = "testId" + valueIndex;
    var sampleTypeId = "sampleTypeId" + valueIndex;
    var sampleId = "sampleId" + valueIndex;
    var sampleNeeded = "sampleNeeded" + valueIndex;
    var orig_sample_no = "orig_sample_no" + valueIndex;
    var isPackage = "isPackage" + valueIndex;
    var testINpackage = "testINpackage" + valueIndex;
    var charge = "charge" + valueIndex;
    var discObj = "disc" + valueIndex;
    var amtObj = "amt" + valueIndex;
    var houseType = "houseType" + valueIndex;
    var row = "row" + valueIndex;
    var rowIndexElId = 'rowIndex'+valueIndex;
    var chargeVal = document.getElementById(charge).value
    var status = document.getElementById("selected" + valueIndex).checked;
    var rowObj = document.getElementById(row);

	if(document.getElementById(testINpackage).value=='n') {
	    if (status) {
	        rowObj.setAttribute("style", "background-color:#F2DCDC");
	        if (billType == "BN") {
	            amountDue.value = formatAmountValue(eval(amountDue.value) - eval(chargeVal));
	            totAmtPay.value = amountDue.value;
	            document.getElementById('totalAmount').innerHTML = amountDue.value;
	            document.getElementById('totalAmount1').value = amountDue.value;
	        }
	    } else {
	        rowObj.removeAttribute("style");
	        if (billType == "BN") {
	            amountDue.value = formatAmountValue(eval(amountDue.value) + eval(chargeVal));
	            totAmtPay.value = amountDue.value;
	            document.getElementById('totalAmount').innerHTML = amountDue.value;
	            document.getElementById('totalAmount1').value = amountDue.value;
	        }
	    }
	    if (billType == "BL") {
	        if (status) {
	            rowObj.setAttribute("style", "background-color:#F2DCDC");
	            var totalAmount = formatAmountValue(eval(document.getElementById('totalAmount1').value) - eval(chargeVal));
	            document.getElementById('totalAmount').innerHTML = totalAmount;
	            document.getElementById('totalAmount1').value = totalAmount;
	        } else {
	            rowObj.removeAttribute("style");
	            var totalAmount = formatAmountValue(eval(document.getElementById('totalAmount1').value) - eval(chargeVal));
	            document.getElementById('totalAmount').innerHTML = totalAmount;
	            document.getElementById('totalAmount1').value = totalAmount;
	        }
	    }
	}

	if(document.getElementById(isPackage).value=='y') {
		var no_of_tests_in_pack = document.getElementById("no_testsinpack" + valueIndex).value;
		var len = 0;
		for(var i=0; i<no_of_tests_in_pack; i++) {
			var len = eval(valueIndex)+eval(i)+eval(1);
			document.getElementById("testId"+len).disabled = status;
	    if (category == 'DEP_LAB') {
	        document.getElementById("sampleTypeId"+len).disabled = status;
	        document.getElementById("sampleId"+len).disabled = status;
	        document.getElementById("sampleNeeded"+len).disabled = status;
	        document.getElementById("orig_sample_no"+len).disabled = status;
	        document.getElementById("houseType"+len).disabled = status;
	    }
		    document.getElementById("isPackage"+len).disabled = status;
		    document.getElementById("testINpackage"+len).disabled = status;
		    document.getElementById("charge"+len).disabled = status;
		    document.getElementById("disc"+len).disabled = status;
		    document.getElementById("amt"+len).disabled = status;
		    document.getElementById('rowIndex'+len).disabled = status;
			if(status)
		   	 	document.getElementById("row"+len).setAttribute("style", "background-color:#F2DCDC");
		   	else
		   		document.getElementById("row"+len).removeAttribute("style");
		}
	}


    document.getElementById(testId).disabled = status;
    if (category == 'DEP_LAB') {
        document.getElementById(sampleTypeId).disabled = status;
        document.getElementById(sampleId).disabled = status;
        document.getElementById(sampleNeeded).disabled = status;
        document.getElementById(houseType).disabled = status;
    }
    document.getElementById(rowIndexElId).disabled = status;
    document.getElementById(isPackage).disabled = status;
    document.getElementById(testINpackage).disabled = status;
    document.getElementById(charge).disabled = status;
    document.getElementById(discObj).disabled = status;
    document.getElementById(amtObj).disabled = status;

	disableDocumentFields(valueIndex, status);
    resetTotals();
}


function disableDocumentFields(rowIndex, status) {
	var mainRowEls = document.getElementsByName('ad_main_row_id');
	var testIdEls = document.getElementsByName('ad_test_id');
	var notesEls = document.getElementsByName('ad_clinical_notes');
	var itemIndexEls = document.getElementsByName('ad_package_activity_index');
	var categoryEls = document.getElementsByName('ad_test_category');
	var editedEls = document.getElementsByName('ad_test_row_edited');
	var notesEditedEls = document.getElementsByName('ad_notes_entered');
	var testNameEls = document.getElementsByName('ad_test_name');
	var testDocDeleteEl = document.getElementsByName('ad_test_doc_delete');

	var uploadEls = YAHOO.util.Dom.getElementsByClassName("testFileUpload", 'input', 'ad_test_info_table')
	for (var i=0; i<mainRowEls.length-1; i++) {
		if (mainRowEls[i].value == ('row'+rowIndex)) {
			mainRowEls[i].disabled = status;
			testIdEls[i].disabled = status;
			notesEls[i].disabled = status;
			itemIndexEls[i].disabled = status;
			categoryEls[i].disabled = status;
			editedEls[i].disabled = status;
			notesEditedEls[i].disabled = status;
			testNameEls[i].disabled = status;
			testDocDeleteEl[i].disabled = status;

			uploadEls[i].disabled = status;
		}
	}

}

function getHospitalId(hospital) {
    var obj = document.forms[0].hospname;
    for (var i = 0; i < obj.length; i++) {
        if (hospital == obj.options[i].text) {
            return obj.options[i].value;
        }
    }
}

function getDepartmentId(deptname) {
    var obj = document.forms[0].department;
    for (var i = 0; i < obj.length; i++) {
        if (deptname == obj.options[i].text) {
            return obj.options[i].value;
        }
    }
}

function getTestId(testname) {
    var obj = document.forms[0].testname;
    for (var i = 0; i < obj.length; i++) {
        if (testname == obj.options[i].text) {
            return obj.options[i].value;
        }
    }
}

function validateGovtIdMandatory() {

	if (govtIdentifierMandatory == 'Y') {
		if (null == document.sampleregform.government_identifier || trim(document.sampleregform.government_identifier.value) == '') {
			document.sampleregform.government_identifier.focus();
     		alert(govtId_label+" "+getString("js.registration.patient.is.required.string"));
     		return false;
     	}
    }
	return true;
}

function saveDetails() {

	if (max_centers_inc_default>1 && centerId == 0) {
		showMessage("js.laboratory.radiology.incomingsampleregistration.registrationallowed.centerusers");
		return false;
	}

    if (!validatePatientFields()) return false;

    if (!validateTestsAdditionalDetails()) return false;
 
    if (!validateGovtIdPattern()) return false;
    
    if (!validateGovtIdMandatory()) return false;
    
    var sTable = document.getElementById('testdetails');
    if (sTable.rows.length == 1) {
        showMessage("js.laboratory.radiology.incomingsampleregistration.addsample");
        return false;
    }

    var form = document.forms[0];
    var canSubmit = false;
    if (form.selected == null) {
        showMessage('js.laboratory.radiology.incomingsampleregistration.addrecordstosave');
        return false;
    }
    if (form.selected.length == undefined) {
        if (!form.selected.checked) {
            canSubmit = true;
        }
    } else {
        for (var i = 0; i < form.selected.length; i++) {
            if (!form.selected[i].checked) {
                canSubmit = true;
            }
        }
    }
     if (!canSubmit) {
        showMessage('js.laboratory.radiology.incomingsampleregistration.norecordstosave');
        return false;
    }
    var testId = document.getElementsByName("testId");
    var alldisabled = 0;
    for(var i=0;i<testId.length;i++) {
    	if(testId[i].disabled==true)
    		alldisabled++;
    }
    if(alldisabled == testId.length) {
    	showMessage('js.laboratory.radiology.incomingsampleregistration.addrecordstosave');
        return false;
    }

	var sampleNeeded = document.getElementsByName("sampleNeeded");
	var sampleTypeId = document.getElementsByName("sampleTypeId");
	var sampleId = document.getElementsByName("sampleId");
	var origSampleNo = document.getElementsByName("orig_sample_no");
	var outhouseTypes = document.getElementsByName("houseType");
	var outhouseIds = document.getElementsByName('outhouseId');

/*	if (category == 'DEP_LAB') {
		for (var i=0; i<outhouseTypes.length; i++) {
			if (outhouseTypes[i].value == 'O' && outhouseIds[i].value == '' && outhouseTypes[i].disabled == false) {
				var outhousesAgainstTests = outhousesAgainstTestsJson[testId[i].value];
				if (outhousesAgainstTests == undefined || outhousesAgainstTests.length == 0) {
					showMessage("js.laboratory.radiology.incomingsampleregistration.internallabtests.notorder");
					return false;
				}
			}
		}
	}
*/
	for(var i=0; i<sampleNeeded.length; i++){
		if(sampleNeeded[i].value=='y' && sampleNeeded[i].disabled==false) {
			if(sampleTypeId[i].value=='') {
				showMessage("js.laboratory.radiology.incomingsampleregistration.selectsampletype.fortestsinthegrid");
				return false;
			}
			if(origSampleNo[i].value=='' && sampleNoGeneration != 'B') {
				showMessage("js.laboratory.radiology.incomingsampleregistration.selectoriginalsampleno.fortestsinthegrid");
				return false;
			}
		}
	}

	if (category == 'DEP_LAB') {
		for (var i=0; i<outhouseTypes.length; i++) {
			if (outhouseTypes[i].value == 'O' && outhouseIds[i].value == '' && outhouseTypes[i].disabled == false) {
				showMessage("js.laboratory.radiology.incomingsampleregistration.selectouthouse.fortestsinthegrid");
				return false;
			}
		}
	}

    var valid = true;

    var discountAuthorizer = document.getElementById('discountAuthName').options[document.getElementById('discountAuthName').selectedIndex].value;
    if ((discountAuthorizer == -1) && !validateIsRateplanDiscountAuthoriser()) {
    	showMessage('js.laboratory.radiology.incomingsampleregistration.discountauthorizer.notberateplandiscount');
    	document.getElementById('discountAuthName').focus();
    	return false;
    }

    if (document.sampleregform.billType.value == "BN"
    		&& document.getElementById('bl').style.display == 'none') {
        valid = valid && validatePayDates();
        valid = valid && validateCounter();
        valid = valid && validatePaymentRefund();
        valid = valid && validatePaymentTagFields();
        if(income_tax_cash_limit_applicability == 'Y') {
            valid = valid && checkIncomingCashLimitValidation();
         }
    }
    valid = valid && validateAllNumerics();
    valid = valid && validatePaymentAmount();
    valid = valid && doPaytmTransactions();

    if (!valid) return false;

    enableFormValues();
    form.submit();
    return true;
}

function validatePatientFields() {
    var salution = document.sampleregform.salutation.options[document.sampleregform.salutation.selectedIndex].text;
    var gender = document.sampleregform.gender.options[document.sampleregform.gender.selectedIndex].value;
    var pname = trim(document.sampleregform.patientname.value);

    var hospname = trimAll(document.sampleregform.orginalLabName.value);
    var billType = trimAll(document.sampleregform.billType.value);

    if (pname != '') {
        if (document.sampleregform.salutation.selectedIndex == 0) {
            showMessage('js.laboratory.radiology.incomingsampleregistration.required.salutation');
            document.sampleregform.salutation.focus();
            return;
        }
    }

    if (pname == "") {
        showMessage("js.laboratory.radiology.incomingsampleregistration.required.patientname");
        document.sampleregform.patientname.focus();
        return false;
    }

     if(isIncomingSampleDOBEntered()){
         var dob = validateIncomingSampleDOB();
         if (!dob) {
             return false;
         }
                         // set the hidden dateOfBirth input value that the backend needs
         document.sampleregform.dateOfBirth.value = dob.getFullYear() + "-" + getFullMonth(dob.getMonth()) + "-" + getFullDay(dob.getDate());
     }
     else {

         if(document.sampleregform.age.value=="..Age.." || document.sampleregform.age.value=="Age" || document.sampleregform.age.value=='') {
             if(agedisable == 'Y' ) {
     		      showMessage("js.registration.patient.dob.age.required");
     			  document.sampleregform.age.focus();
     			  return false;
     		} else if (agedisable == 'N') {
     				showMessage("js.registration.patient.dob.required");
     				document.sampleregform.dobDay.focus();
     				return false;
     		   }
     		}
     		else {
                     if(!validateIncomingSamplePatientAge()) {
                         return false;
                     }
             }
     }

    if (document.sampleregform.gender.selectedIndex == 0) {
        showMessage('js.laboratory.radiology.incomingsampleregistration.required.gender');
        document.sampleregform.gender.focus();
        return false;
    }

    if (null != document.sampleregform.phone_no && trim(document.sampleregform.phone_no.value) != '' ) {
      	if($("#phone_no_valid").val() == 'N') {
				alert(getString("js.registration.patient.mobileNumber") + " " +
				getString("js.registration.patient.enter.govt.invalid.string")+". ");
				$("#phone_no_national").focus();
	            return false;
	    }
      }

    if (hospname == '') {
        showMessage("js.laboratory.radiology.incomingsampleregistration.enterincominghospital");
        document.sampleregform.orginalLabName.focus();
        return false;
    } else {
        if (getLabId() != undefined) document.sampleregform.labId.value = getLabId();
        else document.sampleregform.labId.value = "";
    }

    var referralName = trimAll(document.sampleregform.referralDoctorName.value);
    if (referralName == '') {} else {
        document.sampleregform.referralDocId.value = getReferralDoctorId();
    }

    var disc = trimAll(document.sampleregform.itemDiscPer.value);
    var discAmt = trimAll(document.sampleregform.totalDiscount1.value);

    var isRatePlanDisc = validateIsRateplanDiscountAuthoriser();
	if (isRatePlanDisc && getPaise(discAmt) > 0) {
		setSelectedIndex(document.getElementById('discountAuthName'), '-1');
	}else {
		if (getPaise(disc) == 0)
    		setSelectedIndex(document.getElementById('discountAuthName'), '');
	}

    var discountAuthName = trimAll(document.sampleregform.discountAuthName.value);
    if ((getPaise(disc) > 0 || getPaise(discAmt) > 0) && discountAuthName == '') {
        showMessage("js.laboratory.radiology.incomingsampleregistration.selectdiscountauthorizer");
        document.sampleregform.discountAuthName.focus();
        return false;
    }
    return true;
}

function clearFields() {
	document.getElementById("salutation").value="";
	document.getElementById("patientname").value="";
	document.getElementById("gender").value="";
	//reset phone_no
	clearPhoneField($("#phone_no_country_code"),$("#phone_no_national"),defaultCountryCode);
	document.getElementById("patient_other_info").value="";
}

function clearAddDialogFields() {
	document.getElementById('testnameObj').value = '';
	if (category == 'DEP_LAB') {
		document.getElementById('originalSampleNo').value = '';
		document.getElementById('sampleType').value = '';
		document.getElementById('insampleId').value = '';
		document.getElementById('outhouse').value = '';
	}
	document.getElementById('conducting_doctor').value = '';
	document.getElementById('mainFieldSet').style.display = 'block';
	document.getElementById('conductingDoctorFS').style.display = 'none';
	clearCondDoctorsOfPack();
}

function clearCondDoctorsOfPack() {
	var condDocTable = document.getElementById("conductingDoctorsTable");
	for (var i=1; i<condDocTable.rows.length; ) {
		condDocTable.deleteRow(i);
	}
}

function init() {
	clearFields();
    populateTesNames('testnameObj', 'testContainer', deptWiseTestsjson);
    populateIncomingHospital('orginalLabName', 'orginalLabContainer');
    populateReferalDoctor('referralDoctorName', 'referralDoctorContainer');
    document.forms[0].orginalLabName.focus();
    document.forms[0].bill_rate_plan_id.value='ORG0001';
    initAddDialog();
    initEditDialog();
    setDefaultAuthoriser();
    initOrderDoctorAutoComplete();
    if (document.getElementById('counterId').value == "") {
        document.getElementById('paymentDetails').style.display = "none";
        document.getElementById('bn').style.display = "none";
        document.getElementById('bl').style.display = "block";
    }
    initTestAdditionalInfoDialog();
    setGovtPattern();
}

function validateGovtIdPattern() {
	  
  if (null != document.sampleregform.government_identifier && trim(document.sampleregform.government_identifier.value) != '' ) {
   	if(!(FIC_checkField(" validate-govt-id ", document.sampleregform.government_identifier))) {
				alert(govtId_label+" "+
				getString("js.registration.patient.enter.govt.invalid.string")+". "+
				" "+getString("js.registration.patient.enter.govt.format.string")+" : "+
				govtId_pattern);
	            document.sampleregform.government_identifier.focus();
	            return false;
	    }
   }
  return true;
}

//==================================================================================================================
//FIC_checkField
//c = className
//e = the element
//------------------------------------------------------------------------------------------------------------------
function FIC_checkField(c,e) {
	var valid = true;
	var t = e.value.trim();
	
	//search for required
	if (c.indexOf(' required ') != -1 && t.length == 0) {
		//required found, and not filled in
		valid = false;
	}
	
	// if not required and length is 0, don't validate, just return
	if (c.indexOf(' required ') == -1 && t.length == 0) {
		valid = true;
		return valid;
	}
	
	//check length
	if (c.indexOf(' required ') != -1){
		//check for minlength.
		var m = e.getAttribute('minlength');
		if (m && Math.abs(m) > 0){
			if (e.value.length < Math.abs(m)){
				valid = false;
			}
		}
	}
	
	//check for maxlength.
	if (c.indexOf(' validate-length ') != -1) {
		var m = e.getAttribute('length');
		if (m && Math.abs(m) > 0){
			if (e.value.length > Math.abs(m)){
				valid = false;
	       		}
		}
	}
	//search for validate-
	if (c.indexOf(' validate-number ') != -1 && isNaN(t) && t.match(/[^\d]/)) {
		//number bad
		valid = false;
	} else if (c.indexOf(' validate-decimal ') != -1 && isNaN(t) && !t.match(/^\d*\.?\d+$/)) {
		//decimal bad
		valid = false;
	} else if (c.indexOf(' validate-digits ') != -1 && t.replace(/ /,'').match(/[^\d]/)) {
		//digit bad
		valid = false;
	} else if (c.indexOf(' validate-alpha ') != -1 && !t.match(/^[a-zA-Z]+$/)) {
		//alpha bad
		valid = false;
	} else if (c.indexOf(' validate-alphanum ') != -1 && t.match(/\W/)) {
		//alpha bad
		valid = false;
	} else if (c.indexOf(' validate-date ') != -1) {
		var d = new date(t);
		if (isNaN(d)) {
			//date bad
			valid = false;
		}
	} else if (c.indexOf(' validate-email ') != -1 && !t.match(/\w{1,}[@][\w\-]{1,}([.]([\w\-]{1,})){1,3}$/)) {
		//email bad
		valid = false;
		if (c.indexOf(' required ') == -1 && t.length == 0) {
			valid = true;
		}
	} else if (c.indexOf(' validate-govt-id ') != -1 && govtId_pattern !='') {
		//govt id bad
		var regexp = getRegularExp(govtId_pattern);
		govtId_regexp = new RegExp(regexp);
		valid = govtId_regexp.test(t);
		if (c.indexOf(' required ') == -1 && t.length == 0) {
			valid = true;
		}
	}else if (c.indexOf(' validate-url ') != -1 && !t.match(/^(http|https|ftp):\/\/(([A-Z0-9][A-Z0-9_-]*)(\.[A-Z0-9][A-Z0-9_-]*)+)(:(\d+))?\/?/i)) {
		//url bad
		valid = false;
	} else if (c.indexOf(' validate-date-au ') != -1 && !t.match(/^(\d{2})\/(\d{2})\/(\d{4})$/)) {
		valid = false;
	} else if (c.indexOf(' validate-date-in ') != -1) {
		if (!t.match(/^(\d{2})-(\d{2})-(\d{4})$/))
			valid = false;
		if (validateDateStr && validateDateStr(t) != null)
			valid = false;
	} else if (c.indexOf(' validate-currency-dollar ') != -1 && !t.match(/^\$?\-?([1-9]{1}[0-9]{0,2}(\,[0-9]{3})*(\.[0-9]{0,2})?|[1-9]{1}\d*(\.[0-9]{0,2})?|0(\.[0-9]{0,2})?|(\.[0-9]{1,2})?)$/)) {
		valid = false;
	} else if (c.indexOf(' validate-regex ') != -1) {
	    var r = RegExp(e.getAttribute('regex'));
	    if (r && ! t.match(r)) {
	        valid = false;
	    }
	}
	
	return valid;
}


var testNamesArray = [];
var selectedTestId = '';
var testautoComp = null;

function populateTesNames(testNameObj, testContainer, testsJson) {

	if (testautoComp != undefined && testautoComp != null) {
		testautoComp.destroy();
		testautoComp = null;
	}

	var dataSource = new YAHOO.util.LocalDataSource(testsJson);
	dataSource.responseSchema = {resultsList : "result",
								 fields : [ {key :["TEST_NAME"]},{key :["DIAG_CODE"]},{key :["TEST_ID"]}],
								 numMatchFields: 2
								 };
    testautoComp = new YAHOO.widget.AutoComplete(testNameObj, testContainer, dataSource);
    testautoComp.prehighlightClassName = "yui-ac-prehighlight";
    testautoComp.typeAhead = false;
    testautoComp.useShadow = true;
    testautoComp.allowBrowserAutocomplete = false;
    testautoComp.minQueryLength = 1;
    testautoComp.maxResultsDisplayed = 20;
    testautoComp.autoHighlight = true;
    testautoComp.forceSelection = true;
    testautoComp.animVert = false;
    testautoComp.filterResults = Insta.queryMatchWordStartsWith;
    testautoComp.itemSelectEvent.subscribe(onSelectTest);
}
var mycounterfrows = 0;
var myarray1 = new Array(0); // sampletypeid

var sampleNeeded  = 'n';
var isPackage = false;
var isTemplatePackage = false;
var testID = '';
var houseType = '';
var packageRef = '';
var conductingDocReqAt = '';
var testList = null;
var conductingDoctorsList = null;
var activityIndexArray = null;
function onSelectTest(sType,args) {
    var testName = document.getElementById("testnameObj").value;
    var count = 0;
    var sampleType = null;
    var sampleTypeId = "";
    if (category == 'DEP_LAB')
    	fillOuthouses(null);
	selectedTestId = args[2][2];
    for (var i = 0; i < deptWiseTestsjson.length; i++) {
        var item = deptWiseTestsjson[i];
        if (testName == item["TEST_NAME"]) {

           // document.getElementById('sampleType').innerHTML = item["SAMPLE_TYPE"];
           testID = item["TEST_ID"];
           houseType = item["HOUSE_STATUS"];
           conductingDocReqAt = item["CONDUCTING_DOC_MANDATORY"];
           isPackage = item["TYPE"]=='PKG';
           packageRef = isPackage ? item["TEST_ID"] : '';
           if (isPackage) {
	          	isTemplatePackage = item["PACKAGE_TYPE"]=='O';

				var ajaxobj = newXMLHttpRequest();
				var url = cpath + '/pages/DiagnosticModule/IncomingRegistraionLaboratory.do?_method=getTestListinPackage&packageId=' + testID +'&bedType=GENERAL&orgId='+document.getElementById("bill_rate_plan_id").value;
				ajaxobj.open("POST", url.toString(), false);
				ajaxobj.send(null);
				if (ajaxobj) {
					if (ajaxobj.readyState == 4) {
						if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
							eval("testList =" + ajaxobj.responseText);
						}
					}
				}
				conductingDoctorsList = new Array();
				activityIndexArray = new Array
				for (var k=0; k<testList.length; k++) {
					if (testList[k].conducting_doc_mandatory) {
						conductingDoctorsList.push(testList[k]);
						activityIndexArray.push(k);
					}
				}
           } else {
           		isTemplatePackage = false;
           		testList = null;
           		activityIndexArray = null;
           		conductingDoctorsList = null;
           }

           if(!isPackage && category == 'DEP_LAB') {
	            selectSampleType(item["SAMPLE_TYPE_ID"]);
	            sampleTypeId = document.getElementById('sampleType').options[document.getElementById('sampleType').selectedIndex].value;
	            sampleType = document.getElementById('sampleType').options[document.getElementById('sampleType').selectedIndex].text;
	            sampleNeeded = ( item["SAMPLE_NEEDED"] );

	            if(isSampleIdAutoGenerateReq != 'Y')
	            	document.forms[0].insampleId.focus();

				if(sampleNeeded == 'y')
			   		disablefields(false);
			   	else
			   		disablefields(true);
	       } else {
	       		sampleNeeded  = 'n';
	       }
        }
    }
    if (category == 'DEP_LAB')
    	document.forms[0].sampleAutoGeneratedIdReq.value = isSampleIdAutoGenerateReq;

    if(!isPackage) {
	    if (myarray1.length) {
	        for (var i = 0; i < myarray1.length; i++) {
	            if ((isSampleIdAutoGenerateReq == 'Y') && (sampleTypeId == myarray1[i])) {
	                count = count + 1;
	            }
	        }
	    }

	    for (var i = 0; i < specimenType.length; i++) {
	        if ((isSampleIdAutoGenerateReq == 'Y') && (sampleType == specimenType[i].SAMPLE_TYPE)) {
	            myarray1[mycounterfrows] = new Array(1)
	            myarray1[mycounterfrows][0] = specimenType[i].SAMPLE_TYPE_ID;
	            break;
	        }
	    }
		if (category == 'DEP_LAB') {
			if (houseType == 'O') {
		    	fillOuthouses(testID);
		    	document.getElementById('outhouse').disabled = false;
		    } else {
		    	document.getElementById('outhouse').disabled = true;
		    }
		}
	} else {
		disablefields(true);
		document.getElementById('outhouse').disabled = true;
	}
	if (!isPackage && conductingDocReqAt == 'O') {
		document.getElementById('conducting_doctor').disabled = false;
	} else {
		document.getElementById('conducting_doctor').disabled = true;
	}

	if (isPackage && conductingDoctorsList.length > 0) {
		enableButtons('next', false);
	} else {
		enableButtons('add', false);
	}
}

function showAddDialog() {
	currentFieldSet = document.getElementById('mainFieldSet');
	currentFieldSet.style.display = 'block';
	if (isPackage && conductingDoctorsList.length > 0) {
		this.enableButtons('next', false);
	} else {
		this.enableButtons('add', false);
	}
}

function validateConductingDoctorInPack() {
	var els = document.getElementsByName('packConductingDoctor');
	for (var i=0; i<els.length; i++) {
		if (els[i].value == '') {
			showMessage("js.common.order.conducting.doctor.required");
			els[i].focus();
			return false;
		}
	}
	return true;
}

function enableButtons(addOrNext, previousEnabled) {
	var addEnabled = (addOrNext == 'add');
	var nextEnabled = !addEnabled;
	document.sampleregform.addBtn.disabled = !addEnabled;
	document.sampleregform.addNext.disabled = !nextEnabled;
	document.sampleregform.addPrevious.disabled = !previousEnabled;
}


var currentFieldSet = null;
function showConductingDoctors() {
	currentFieldSet = document.getElementById('conductingDoctorFS');
	currentFieldSet.style.display = 'block';
	initConductingDoctors();
}

function onNext() {
	currentFieldSet.style.display = 'none';
	this.enableButtons('add', true);
	showConductingDoctors();
}

function onPrevious() {
	currentFieldSet.style.display = 'none';
	this.enableButtons('next', false);
	showAddDialog();
}

function initConductingDoctors() {
	var condDoctorTable = document.getElementById("conductingDoctorsTable");
	for (var i=1; i<condDoctorTable.rows.length;) {
		condDoctorTable.deleteRow(i);
	}
	for (var i=0; i<conductingDoctorsList.length; i++ ) {
		var docRow = condDoctorTable.insertRow(-1);

		var cell = docRow.insertCell(-1);
		cell.appendChild(makeLabel(null, conductingDoctorsList[i].category == 'DEP_LAB' ? 'Laboratory' : 'Radiology'));

		var cell = docRow.insertCell(-1);
		cell.appendChild(makeLabel(null, conductingDoctorsList[i].test));
		cell.appendChild(makeHidden("packageActId", "packageActId"+i, conductingDoctorsList[i].activity_id));
		// this is required to maintain the association to support the duplicate items.
		cell.appendChild(makeHidden("packageActivityIndex", "packageActivityIndex"+i, activityIndexArray[i]+''));

		var condDoctor = "packConductingDoctor"+i;
		var condCoctorContainer = condDoctor + "AcDropdown";

		var cell = docRow.insertCell(-1);
		cell.setAttribute("class", "yui-skin-sam");
		cell.innerHTML = '<div class="myAutoComplete"><input id="'+ condDoctor +'" name="packConductingDoctor" type="text" /><div id="'+ condCoctorContainer +'" ></div></div>';
		cell.appendChild(makeHidden("packCondDoctorId", "packCondDoctorId"+i, null ));

		initConductingDoctorAutoComplete(condDoctor, {"doctorList": doctors},  function(sType, aArgs) {
			var index = (aArgs[0].getInputEl().getAttribute("id")).replace("packConductingDoctor", "");
				document.getElementById("packCondDoctorId"+index).value = aArgs[2][1];
				}, "dept_id", conductingDoctorsList[i].category);


	}
}

function fillOuthouses(testId) {

	var outhousesAgainstTests = outhousesAgainstTestsJson[testId];
	var selectElement = document.getElementById('outhouse');

	for (var j=selectElement.options.length; j>0; j--) {
		selectElement.remove(j);
		document.getElementById('outhouse').options[document.getElementById('outhouse').selectedIndex].value = '';
		document.getElementById('outhouse').options[document.getElementById('outhouse').selectedIndex].text = '--Select--';
	}

	if (outhousesAgainstTests != undefined) {
		for (var i=0; i<outhousesAgainstTests.length; i++) {
			var outHouseMap = outhousesAgainstTests[i];
			var option = document.createElement('option');
			option.value = outHouseMap['OUTSOURCE_DEST_ID'];
			option.text = outHouseMap['OUTSOURCE_NAME'];
			var flag = false;
			if(outhousesAgainstTests.length == 1){
		          document.getElementById('outhouse').options[document.getElementById('outhouse').selectedIndex].value = option.value;
		          document.getElementById('outhouse').options[document.getElementById('outhouse').selectedIndex].text = option.text;
		          flag = true;
		    }
		    if(outHouseMap['DEFAULT_OUTSOURCE'] == 'Y'){
		         document.getElementById('outhouse').options[document.getElementById('outhouse').selectedIndex].value = option.value;
		         document.getElementById('outhouse').options[document.getElementById('outhouse').selectedIndex].text = option.text;
		         flag = true;
		    }
			if(!flag){
			selectElement.appendChild(option);
			}
		}
	}

}

function disablefields(status){
	document.getElementById("sampleType").disabled = status;
	document.getElementById("originalSampleNo").disabled = status;
	document.getElementById("insampleId").disabled = status;
}

function selectSampleType(sampleTypeId) {
	var sampleTypes = document.getElementById('sampleType');
	for (var i=0; sampleTypes.options.length; i++) {
		if (sampleTypes.options[i].value == sampleTypeId) {
			sampleTypes.options[i].selected = true;
			break;
		}
	}
}


var labNamesArray = [];

function populateIncomingHospital(orginalLabName, orginalLabContainer) {

	var dataSource = new YAHOO.util.LocalDataSource(incomingHospitalJSON);
	dataSource.responseSchema = {resultsList : "result",
								 fields : [ {key :["hospital_name"]},{key :["hospital_id"]}] };

    var autoComp = new YAHOO.widget.AutoComplete(orginalLabName, orginalLabContainer, dataSource);
    autoComp.prehighlightClassName = "yui-ac-prehighlight";
    autoComp.typeAhead = false;
    autoComp.useShadow = true;
    autoComp.allowBrowserAutocomplete = false;
    autoComp.minQueryLength = 0;
    autoComp.maxResultsDisplayed = 20;
    autoComp.autoHighlight = true;
    autoComp.forceSelection = true;
    autoComp.animVert = false;
    autoComp.itemSelectEvent.subscribe(setDefaultRatePlan);
}

function setDefaultRatePlan(sType,args) {
	document.getElementById("bill_rate_plan_id").options.length = 0;
	getDefaultRatePlan();
	for ( var i =0;i<incomingHospitalJSON.length;i++ ) {
		if ( args[2][1] == incomingHospitalJSON[i]["hospital_id"] ) {
			document.getElementById("bill_rate_plan_id").value = incomingHospitalJSON[i]["default_rate_plan_id"];
			break;
		}
	}
	if ( document.getElementById("bill_rate_plan_id").value == "" )
		document.getElementById("bill_rate_plan_id").value = 'ORG0001';

}

function getLabId() {
    var labName = document.forms[0].orginalLabName.value;
    var labId = "";
    for (var i = 0; i < incomingHospitalJSON.length; i++) {
        if (labName == incomingHospitalJSON[i]["hospital_name"]) {
            labId = incomingHospitalJSON[i]["hospital_id"];
            return labId;
        }
    }
}

var referalNamesArray = [];

function populateReferalDoctor(referralDoctorName, referralDoctorContainer) {

    referalNamesArray.length = referalDoctorsJSON.length;
    for (i = 0; i < referalDoctorsJSON.length; i++) {
        var item = referalDoctorsJSON[i]
        referalNamesArray[i] = item["REF_NAME"];
    }

    var datasource = new YAHOO.widget.DS_JSArray(referalNamesArray, {
        queryMatchContains: true
    });
    var autoComp = new YAHOO.widget.AutoComplete(referralDoctorName, referralDoctorContainer, datasource);

    autoComp.formatResult = Insta.autoHighlight;
    autoComp.prehighlightClassName = "yui-ac-prehighlight";
    autoComp.useShadow = true;
    autoComp.allowBrowserAutocomplete = false;
    autoComp.minQueryLength = 0;
    autoComp.maxResultsDisplayed = 20;
    autoComp.autoHighlight = true;
    autoComp.forceSelection = false;
    autoComp.animVert = false;
    autoComp.itemSelectEvent.subscribe(nameInput);

}

var nameInput = function (sType, aArgs) {
        var oData = aArgs[2];
        document.forms[0].referralDoctorName.value = oData;
    }


function getReferralDoctorId() {
    var referalDocName = document.forms[0].referralDoctorName.value;
    var referralDocId = "";
    for (var i = 0; i < referalDoctorsJSON.length; i++) {
        if (referalDocName == referalDoctorsJSON[i]["REF_NAME"]) {
            referralDocId = referalDoctorsJSON[i]["REF_ID"];
            return referralDocId;
        }
    }
}

function subscribeEscKeyEvent(dialog) {
    var kl = new YAHOO.util.KeyListener(document, {
        keys: 27
    }, {
        fn: dialog.cancel,
        scope: dialog,
        correctScope: true
    });
    dialog.cfg.setProperty("keylisteners", kl);
}

function initAddDialog() {
    addDialog = new YAHOO.widget.Dialog("addDialog", {
        width: "880px",
        context: ["", "tr", "br"],
        visible: false,
        modal: true,
        constraintoviewport: true
    });
    addDialog.render();
    subscribeEscKeyEvent(addDialog);
}

function initEditDialog() {
	editDialog = new YAHOO.widget.Dialog("editDialog", {
        width: "750px",
        context: ["", "tr", "br"],
        visible: false,
        modal: true,
        constraintoviewport: true
    });
    editDialog.render();
    subscribeEscKeyEvent(editDialog);
}

function getThisRow(node) {
    return findAncestor(node, "TR");
}

function getAddDialog(obj) {
	clearAddDialogFields();
	enableButtons('add', false);
	if ( isRatePlanSelected() ) {
	    var row = getThisRow(obj);
	    addDialog.cfg.setProperty("context", [document.getElementById("loadDialog"), "tr", "br"], false);
	    addDialog.show();
	    showAddDialog();
	    document.getElementById("testnameObj").focus();
	    if (category == 'DEP_LAB' && isSampleIdAutoGenerateReq == 'Y') {
		    document.getElementById('sampleIdlbl').style.display = 'none';
		    document.getElementById('sampleIdField').style.display = 'none';
		}
	    return false;
    } else {
    	showMessage("js.laboratory.radiology.incomingsampleregistration.select.rateplan");
    	document.getElementById("bill_rate_plan_id").focus();
    	return false;
    }
}

function getRowItemIndex(row) {
	return row.rowIndex - getFirstItemRow();
}

function showEditDialog(obj) {
	var rowObj = getThisRow(obj);
	editDialog.cfg.setProperty("context", [obj, "tr", "br"], false);
	document.getElementById("e_testlbl").textContent = getElementByName(rowObj,"testName").value;
	document.getElementById("e_sampleType").value = getElementByName(rowObj,"sampleTypeId").value;
	setSelectedIndex(document.getElementById('e_sampleType'), getElementByName(rowObj,"sampleTypeId").value);
	document.getElementById("e_originalSampleNo").value = getElementByName(rowObj,"orig_sample_no").value;
	document.getElementById("e_insampleId").value =  getElementByName(rowObj,"sampleId").value;
	document.getElementById("dialogId").value = getRowItemIndex(rowObj);
	var editOuthouse = getElementByName(rowObj, 'houseType').value;
	setOuthouse(getElementByName(rowObj, 'testId').value, getElementByName(rowObj, 'outhouseId').value, editOuthouse);
	editDialog.show();
	if (category == 'DEP_LAB' && isSampleIdAutoGenerateReq == 'Y') {
		    document.getElementById('e_sampleIdlbl').style.display = 'none';
		    document.getElementById('e_sampleIdField').style.display = 'none';
	}
	if(getElementByName(rowObj,"sampleNeeded").value=='n'){
		document.getElementById("e_sampleType").disabled = true;
		document.getElementById("e_originalSampleNo").disabled = true;
		document.getElementById("e_insampleId").disabled = true;
	} else {
		document.getElementById("e_sampleType").disabled = false;
		document.getElementById("e_originalSampleNo").disabled = false;
		document.getElementById("e_insampleId").disabled = false;
	}
	var table = document.getElementById("testdetails");
	var len = table.rows.length - 3;
	var id = document.getElementById("dialogId").value;
	if(id == 0)
		document.getElementById("prevBtn").disabled = true;
	else  {
		if(id==1){
			if(document.getElementById("isPackage1").value=='y')
				document.getElementById("prevBtn").disabled = true;
			else
				document.getElementById("prevBtn").disabled = false;
		} else {
			document.getElementById("prevBtn").disabled = false;
		}
	}
	if(id == len)
		document.getElementById("nextBtn").disabled = true;
	else
		document.getElementById("nextBtn").disabled = false;
}

function showNextOrPrevTest(navigate) {
	var status = saveTest(navigate);
	var id = document.getElementById("dialogId").value;
	if(status == true) {
		if(navigate == 'next') {
			id++;
			var j = id+1;
			if(document.getElementById("isPackage"+j).value=='y')
				id++;
		}
		if(navigate == 'prev') {
			id--;
			var j = id+1;
			if(document.getElementById("isPackage"+j).value=='y')
				id--;
		}
	}
		var  row = getItemRow(id);
		showEditDialog(row);
}

function getFirstItemRow() {
		return 1;
}

function getItemRow(i) {
	i = parseInt(i);
	var table = document.getElementById("testdetails");
	return table.rows[i + getFirstItemRow()];
}

function setOuthouse(testId, outHouseId, editOuthouse) {

	var outhousesAgainstTests = outhousesAgainstTestsJson[testId];
	var selectElement = document.getElementById('editOuthouseEle');

	for (var j=selectElement.options.length; j>0; j--) {
		selectElement.remove(j);
	}

	if (editOuthouse == 'I') {
		document.getElementById('editOuthouseEle').disabled = true;
		return;
	} else {
		document.getElementById('editOuthouseEle').disabled = false;
	}

	if (outhousesAgainstTests != undefined) {
		for (var i=0; i<outhousesAgainstTests.length; i++) {
			var outHouseMap = outhousesAgainstTests[i];
			var option = document.createElement('option');
			option.value = outHouseMap['OUTSOURCE_DEST_ID'];
			option.text = outHouseMap['OUTSOURCE_NAME'];
			if (outHouseMap['OUTSOURCE_DEST_ID'] == outHouseId)
				option.selected = true;
			selectElement.appendChild(option);
		}
	}
}

function saveTest(navigate) {
	var rowObj = getItemRow(document.getElementById("dialogId").value);
	var e_sampletypeID = document.getElementById('e_sampleType').options[document.getElementById('e_sampleType').selectedIndex].value;
	var e_sampleNeeded = getElementByName(rowObj,"sampleNeeded").value;
	var outhouseVal = document.getElementById('editOuthouseEle').options[document.getElementById('editOuthouseEle').selectedIndex].value;
	var outhouseTxt = document.getElementById('editOuthouseEle').options[document.getElementById('editOuthouseEle').selectedIndex].text;
	var houseType = getElementByName(rowObj, 'houseType').value;
	var originalSampleNo = document.getElementById("e_originalSampleNo").value;
	var outhouseIds = document.getElementsByName('outhouseId');
    var outhouseTypes = document.getElementsByName('houseType');
    var orig_sample_nos = document.getElementsByName('orig_sample_no');
    var outsourcesCount = document.getElementById('editOuthouseEle').options.length;
    var testId = getElementByName(rowObj, 'testId').value;

/*	if (category == 'DEP_LAB' && houseType == 'O' && outhouseVal == '') {
		if (max_centers_inc_default>1 && outsourcesCount < 2) {
			showMessage('js.laboratory.radiology.incomingsampleregistration.internallabtests.notorder');
			return false;
		}
	}
*/
	if (e_sampletypeID == "" && category == 'DEP_LAB' && e_sampleNeeded == 'y') {
    	showMessage('js.laboratory.radiology.incomingsampleregistration.required.sampletype');
    	document.getElementById('e_sampleType').focus();
    	return false;
    }

    if (category == 'DEP_LAB' && isSampleIdAutoGenerateReq != 'Y' && e_sampleNeeded == 'y') {
        var sampleId = trim(document.sampleregform.e_insampleId.value);
        if (sampleId == '') {
            showMessage('js.laboratory.radiology.incomingsampleregistration.required.sampleid');
            document.sampleregform.e_insampleId.focus();
            return false;
        }

    }

    if(category == 'DEP_LAB' && sampleNoGeneration != 'B' && document.getElementById("e_originalSampleNo").value=='' && e_sampleNeeded=='y') {
		showMessage("js.laboratory.radiology.incomingsampleregistration.enteroriginalsampleno");
		return false;
	}

	if (houseType == 'O' && outhouseVal == '') {

		showMessage("js.laboratory.radiology.incomingsampleregistration.select.outhouse");
		return false;
	}

	if (category == 'DEP_LAB' && sampleNoGeneration == 'P' && isSampleIdAutoGenerateReq == 'Y' && originalSampleNo != '') {
       	for (var i=0; i<outhouseTypes.length; i++) {
       		var rowIndex = getThisRow(outhouseTypes[i]).rowIndex;
    		if (orig_sample_nos[i].value == originalSampleNo && outhouseTypes[i].value != houseType && rowObj.rowIndex != rowIndex) {
				showMessage('js.laboratory.radiology.incomingsampleregistration.originalsampleno.notsame.inouthouses');
				document.sampleregform.e_originalSampleNo.value = '';
				document.sampleregform.e_originalSampleNo.focus();
				return false;
    		} else if (orig_sample_nos[i].value == originalSampleNo && outhouseTypes[i].value == 'O' &&
    				houseType == 'O' && rowObj.rowIndex != rowIndex && outhouseIds[i].value != outhouseVal) {
    			showMessage('js.laboratory.radiology.incomingsampleregistration.originalsampleno.notsame.twodiffouthouses');
				document.sampleregform.e_originalSampleNo.value = '';
				document.sampleregform.e_originalSampleNo.focus();
    			return false;
    		}
    	}
    }

	if(document.getElementById('e_sampleType').options[document.getElementById('e_sampleType').selectedIndex].value!="") {
		setNodeText(rowObj.cells[3], document.getElementById('e_sampleType').options[document.getElementById('e_sampleType').selectedIndex].text);
		getElementByName(rowObj,"sampleTypeId").value = document.getElementById('e_sampleType').options[document.getElementById('e_sampleType').selectedIndex].value;
	} else {
		setNodeText(rowObj.cells[3], "");
		getElementByName(rowObj,"sampleTypeId").value = "";
	}
	getElementByName(rowObj,"orig_sample_no").value = document.getElementById("e_originalSampleNo").value;
	setNodeText(rowObj.cells[4], document.getElementById("e_originalSampleNo").value);

	if(isSampleIdAutoGenerateReq != 'Y' && e_sampleNeeded == 'y') {
		getElementByName(rowObj,"sampleId").value = document.getElementById("e_insampleId").value;
		setNodeText(rowObj.cells[5],document.getElementById("e_insampleId").value);
	}

	var outsourceDestType = '';
	var outhousesAgainstTests = outhousesAgainstTestsJson[testId];
	if (outhousesAgainstTests != undefined) {
		for (var i=0; i<outhousesAgainstTests.length; i++) {
			var outHouseMap = outhousesAgainstTests[i];
			if (outHouseMap['OUTSOURCE_DEST_ID'] == outhouseVal) {
				outsourceDestType = outHouseMap['OUTSOURCE_DEST_TYPE'];
				break;
			}
		}
	}

    if (category == 'DEP_LAB' && e_sampleNeeded == 'n' && outsourceDestType == 'C') {
    	showMessage('js.laboratory.radiology.incomingsampleregistration.samplerequiredno.internallabtest');
    	return false;
    }

	if (houseType == 'O') {
		getElementByName(rowObj, 'outhouseId').value = outhouseVal;
		getElementByName(rowObj, 'outsourceDestType').value = outsourceDestType;
		setNodeText(rowObj.cells[6], outhouseTxt);
	}

	if(navigate == 'add')
		editDialog.cancel();
	return true;
}

function isRatePlanSelected() {
	return !document.getElementById("bill_rate_plan_id").value == '';
}
function closeDialog() {
	clearAddDialogFields();
    addDialog.cancel();
}
function closeEditDialog() {
	editDialog.cancel();
}

function onChangeBillType() {

    var billType = document.forms[0].billType.value;
    if (billType == "BL") {

        document.getElementById('paymentDetails').style.display = "none";
        document.getElementById('bn').style.display = "none";
        document.getElementById('bl').style.display = "block";
    } else {
        if (document.getElementById('counterId').value == "") {
            document.getElementById('paymentDetails').style.display = "none";
            document.getElementById('bl').style.display = "block";
            document.getElementById('bn').style.display = "none";
        } else {
            document.getElementById('paymentDetails').style.display = "block";
            document.getElementById('bl').style.display = "none";
            document.getElementById('bn').style.display = "block";
        }
    }
    resetTotals();
}

function setDefaultAuthoriser() {
	var discountAuthoLen = document.getElementById('discountAuthName').options.length;
	var discountAuthoObj = document.getElementById('discountAuthName');
	for (var i=0; i<discountAuthoLen; i++) {
		if (discountAuthoObj.options[i].value == '') {
			discountAuthoObj.options[i].selected = true;
			return;
		}
	}
}

function validateIsRateplanDiscountAuthoriser() {
	var disc = document.getElementsByName('disc');
	var ratedisc = document.getElementsByName('ratedisc');

	for (var i=0; i<disc.length; i++) {
		if (getPaise(disc[i].value) != getPaise(ratedisc[i].value)) {
			return false;
		}
	}
	return true;
}

function addTest() {
	if (!orderDialogValidate()) {
		return false;
	}

	if(isTemplatePackage)
		addPackage();
	else
		getCharge();

}

function validateTestsAdditionalDetails() {
	var table = document.getElementById('ad_test_info_table');
	var mainRows = YAHOO.util.Dom.getElementsByClassName("mainRow", 'tr', table);
	var testNames = new Array();
	for (var i=0; i<mainRows.length; i++) {
		var row = mainRows[i];
		var notesEl = getElementByName(row, 'ad_clinical_notes');
		if (notesEl.disabled) continue;// ordered item marked for delete, so no need to validate this

		if (!empty(notesEl.value)) continue;

		var docFound = false;
		while (!YAHOO.util.Dom.hasClass(row, 'dummyRow')) {
			var deleted = getElementByName(row, "ad_test_doc_delete").value;
			var fileName = YAHOO.util.Dom.getElementsByClassName("testFileUpload", 'input', row)[0].value;

			if (deleted == 'false' && !empty(fileName) ) {
				docFound = true;
				break;
			}

			row = YAHOO.util.Dom.getNextSibling(row);
		}
		if (!docFound) {
			testNames.push(getElementByName(row, 'ad_test_name').value);
		}
	}

	if (testNames.length > 0) {
		alert("Please enter clinical notes or upload a document for the following tests. \n\n * " + testNames.join("\n * "));
		return false;
	}

	return true;
}

function validateMainDetails() {
	if (document.getElementById('testnameObj').value == '') {
		showMessage('js.laboratory.radiology.incomingsampleregistration.required.testname');
		document.getElementById('testnameObj').focus();
		return false;
	}

/*	if (category == 'DEP_LAB') {
		var selectedOuthouseVal = document.getElementById('outhouse').options[document.getElementById('outhouse').selectedIndex].value;
		var outsourcesCount = document.getElementById('outhouse').options.length;
	    if (houseType == 'O' && selectedOuthouseVal == '') {
	    	if (max_centers_inc_default>1 && outsourcesCount < 2) {
				showMessage('js.laboratory.radiology.incomingsampleregistration.internallabtests.notorder');
				return false;
			}
		}
	}
*/
	if (category == 'DEP_LAB' && sampleNoGeneration != 'B' && document.getElementById("originalSampleNo").value=='' && sampleNeeded=='y') {
		showMessage("js.laboratory.radiology.incomingsampleregistration.enteroriginalsampleno");
		return false;
	}
	return true;
}

function orderDialogValidate() {
	if (currentFieldSet == document.getElementById('mainFieldSet')) {
		return validateMainDetails();
	} else if (currentFieldSet == document.getElementById('conductingDoctorFS')) {
		return validateConductingDoctorInPack();
	}
}

 function getCharge () {
		rateDetails = '';//clear old values
		var type = category == 'DEP_LAB' ? 'Laboratory' : 'Radiology';
		type = isPackage ?  'Package' : type;
		var url = cpath + '/master/orderItems.do?method=getItemCharges'+
			'&type='+type+'&id='+encodeURIComponent(selectedTestId) +
			'&bedType=GENERAL&orgId='+document.getElementById("bill_rate_plan_id").value+'&visitType=in';

		var getChargeRequest = YAHOO.util.Connect.asyncRequest('GET', url,
			{	success: onGetCharge,
				failure: onGetChargeFailure,
				argument: null}
		);
	}

 function onGetCharge (response) {
		if (response.responseText != undefined) {
			rateDetails = eval('(' + response.responseText + ')');
			rateDetails = rateDetails[0];
			if(!isPackage)
				addRecords();
			else
			    addPackage();
		}
	}
 function onGetChargeFailure (response) {
 		if(!isPackage)
			addRecords();
		else
			addPackage();
	}

/**
	Clears all amount fields and payment fields
**/
function wipeOutAddedTests() {
	var testdetails = document.getElementById("testdetails");
	var rows = testdetails.rows;
	while(rows.length > 2) {
		for ( var i = 1; i < rows.length-1; i++) {
			testdetails.deleteRow(i);
		}
	}
	wipeOutCharges();
	var orgId = document.getElementById("bill_rate_plan_id").value;
	let age = document.getElementById("age").value;
	let ageIn = document.getElementById("ageIn").value;
	let ageText = '';
	if(age){
		ageText = age + ageIn;
	}

	if(orgId != null && orgId != '') {
		var testURL = url+"&orgidforitem="+orgId+"&ageText="+ageText;
			YAHOO.util.Connect.asyncRequest('GET', testURL, {
				success: onNewTestList,
				failure: null
			});
	}

	var testDocTable = document.getElementById('ad_test_info_table');
	for (var i=1; i<testDocTable.rows.length-1; ) {
		testDocTable.deleteRow(i);
	}
}

function onNewTestList(response) {
	eval(response.responseText);
	setTestName(deptWiseTestsjson);
	populateTesNames('testnameObj', 'testContainer', deptWiseTestsjson);
}
// replacing deptWiseTestsjson from the test list which is filtered using selected org id.
function setTestName (testList) {
	deptWiseTestsjson = testList;
}

function wipeOutCharges () {
	document.getElementById('amountdue').value = '';
	document.getElementById('amtPay').value = '';
	document.forms[0].totPayingAmt.value ='';
	document.getElementById('totalAmount1').value = '';
	document.getElementById('totalDiscount1').value = '';
	document.getElementById('totalAmount').innerHTML = '';
	resetTotals();
}


var autocompAr = new Array();
function initConductingDoctorAutoComplete(field, list, selectHandler, filterCol, filterValue) {
	if (!empty(autocompAr[field])){
		autocompAr[field].destroy();
	}

	var filteredList = list;
	if(list !=  null && filterCol != undefined){
		filteredList = { "doctors": filterList(list.doctorList, filterCol, filterValue) };
	}

	var ds = new YAHOO.util.LocalDataSource(filteredList,{ queryMatchContains : true });
	ds.responseType =  YAHOO.util.LocalDataSource.TYPE_JSON;
	ds.responseSchema = { resultsList : "doctors",
		  fields: [ {key : "doctor_name"}, {key: "doctor_id"} ]
	};

	var autoComp = new YAHOO.widget.AutoComplete(field, field+'AcDropdown', ds);

	autoComp.typeAhead = false;
	autoComp.useShadow = true;
	autoComp.allowBrowserAutocomplete = false;
	autoComp.minQueryLength = 0;
	autoComp.maxResultsDisplayed = 20;
	autoComp.autoHighlight = true;
	autoComp.forceSelection = true;
	autoComp.animVert = false;
	autoComp.useIFrame = true;
	autoComp.formatResult = Insta.autoHighlight;

	autoComp.itemSelectEvent.subscribe(selectHandler);
	autocompAr[field] = autoComp;

	return autoComp;
}

function initOrderDoctorAutoComplete() {

	var doctorsJson = {result : doctors};
	var ds = new YAHOO.util.LocalDataSource(doctorsJson,{ queryMatchContains : true });
	ds.responseType =  YAHOO.util.LocalDataSource.TYPE_JSON;
	ds.responseSchema = { resultsList : "result",
		  fields: [ {key : "doctor_name"}, {key: "doctor_id"} ]
	};

	var autoComp = new YAHOO.widget.AutoComplete('conducting_doctor', 'conducting_doctorAcDropdown', ds);

	autoComp.typeAhead = false;
	autoComp.useShadow = true;
	autoComp.allowBrowserAutocomplete = false;
	autoComp.minQueryLength = 0;
	autoComp.maxResultsDisplayed = 20;
	autoComp.autoHighlight = true;
	autoComp.forceSelection = true;
	autoComp.animVert = false;
	autoComp.useIFrame = true;
	autoComp.formatResult = Insta.autoHighlight;

	autoComp.itemSelectEvent.subscribe(function (sType, aArgs){
		var doctor = aArgs[2];
		document.sampleregform.conducting_doctorId.value = doctor[1];
	});
	return autoComp;

}

function getDefaultRatePlan() {
	var incHosp = document.sampleregform.orginalLabName;
	var selectedHosp = incHosp.value;
	var rateplanlist = '';
	if (selectedHosp != '') {
		var ajaxobj = newXMLHttpRequest();
		var url = cpath + '/pages/DiagnosticModule/IncomingRegistraionLaboratory.do?_method=getDefaultBillRatePlan&selectedHosp=' + selectedHosp;
		ajaxobj.open("POST", url.toString(), false);
		ajaxobj.send(null);
		if (ajaxobj) {
			if (ajaxobj.readyState == 4) {
				if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
					eval("rateplanlist=" + ajaxobj.responseText);
					var selectbox = document.getElementById('bill_rate_plan_id');

					  if(rateplanlist != null) {
						  var selectField = "";
						  if(rateplanlist.length > 1) {
							  selectField += "<option value=''>-- Select --</option>";
						  }
						  for(var i =0; i<rateplanlist.length; i++) {

							  selectField += " <option value="+rateplanlist[i].org_id+">"+rateplanlist[i].org_name+"</option>";
						  }
						  selectbox.innerHTML = selectField;
					  }
				}
			}
		}

	}
	wipeOutAddedTests();
}

function onChangeOfGovtIdType(){
	if (!empty(document.getElementById('identifier_id')))
		document.getElementById('identifier_id').removeAttribute('disabled');
}

function validateIncomingSamplePatientAge() {
	var msg = getString("js.registration.patient.age.validation.more.than.120.years");
	var ageIn = document.getElementById("ageIn").value;
	var age = document.getElementById("age").value;
	if(ageIn == 'Y' && age > 120) {
		alert(msg);
		document.getElementById("age").focus();
		return false;
	} else if(ageIn == 'M' && (age/12) > 120) {
		alert(msg);
		document.getElementById("age").focus();
		return false;
	} else if(ageIn == 'D' && (age/365) > 120) {
		alert(msg);
		document.getElementById("age").focus();
		return false;
	}
	return true;
}

function validateIncomingSampleDOB() {
    var strDay = document.getElementById("dobDay").value;
    var strMonth = document.getElementById("dobMonth").value;
    var strYear = document.getElementById("dobYear").value;

	if (strDay == "") {
		showMessage("js.registration.patient.date.required");
		setTimeout(document.getElementById("dobDay").focus(),0);
		return null;
	}

	if (strMonth == "") {
		showMessage("js.registration.patient.month.required");
		setTimeout(document.getElementById("dobMonth").focus(),0);
		return null;
	}

	if (strYear == "") {
		showMessage("js.registration.patient.year.required");
		return null;
	}

    if (!isInteger(strYear)) {
        showMessage("js.registration.patient.invalid.year.not.an.integer.string");
        return null;
    }
    if (!isInteger(strMonth)) {
        showMessage("js.registration.patient.invalid.month.not.an.integer.string");
	    setTimeout(document.getElementById("dobMonth").focus(),0);
        return null;
    }
    if (!isInteger(strDay)) {
        showMessage("js.registration.patient.invalid.month.not.an.integer.string");
	    setTimeout(document.getElementById("dobDay").focus,0);
        return null;
    }

    if (parseInt(strDay) > 31) {
        showMessage("js.registration.patient.enter.correct.date.string");
	    setTimeout(document.getElementById("dobDay").focus(),0);
        return null;
    }

    if (parseInt(strMonth) > 12) {
        showMessage("js.registration.patient.enter.correct.month.string");
	    setTimeout(document.getElementById("dobMonth").focus(),0);
        return null;
    }

    if (strYear.length < 4) {
        var year = convertTwoDigitYear(parseInt(strYear,10));
        if (year < 1900) {
            alert(getString("Invalid year:")+" " + year +
                ". "+getString("js.registration.patient.must.be.two.digit.or.four.digit.year.string"));
	        setTimeout(document.getElementById("dobYear").focus(),0);
            return null;
        }
        // silently set the 4-digit year back to the textbox, and get the new value
        document.getElementById("dobYear").value = year;
        strYear = year.toString();
    }

    // check if a conversion gives us back the same numbers, or else, correct it
    // For example, 31 Sep will be converted to 01 Oct. We need to warn the user.
    var dob = getDateFromDDMMYY(strDay, strMonth, strYear);
    if (!dob) {
        showMessage("js.registration.patient.invalid.date.specification.string");
	    setTimeout(document.getElementById("dobDay").focus(),0);
        return null;
    }

    if (dob > (new Date()) ) {
	    showMessage("js.registration.patient.date.of.birth.and.current.date.check.string");
	    setTimeout(document.getElementById("dobDay").focus(),0);
		return null;
    }

    var newDate = dob.getDate();
    var newMonth = dob.getMonth();
    var newYear = dob.getFullYear();

    if ( (parseInt(strDay,10) != newDate) || (parseInt(strMonth,10) != newMonth + 1) ||
         (parseInt(strYear,10) != newYear) )  {

        // clear the new value in the text boxes and warn the user
        document.getElementById("dobDay").value = "";
        //document.mainform.dobMonth.value = "";
        //document.mainform.dobYear.value = "";
        showMessage("js.registration.patient.valid.date.check.for.current.month.year.combination.string");
	    setTimeout(document.getElementById("dobDay").focus(),0);
        return null;
    }

    return dob;
}

function isIncomingSampleDOBEntered() {
    var strDay = document.getElementById("dobDay").value;
    var strMonth = document.getElementById("dobMonth").value;
    var strYear = document.getElementById("dobYear").value;

    if ( (strDay == 'DD') && (strMonth == 'MM') && (strYear == 'YY') ) {
        return false;
    }
    if ( (strDay == '') && (strMonth == '') && (strYear == '') ) {
        return false;
    }
    return true;
}
function getIncomingSampleAge(validate, validatedDob) {
	var dob = null;
	if (validate) {
	    if (!isIncomingSampleDOBEntered()) {
	        return;
	    }
    	dob = validateIncomingSampleDOB();
    } else {
    	dob = validateIncomingSampleDOB;
    }
    if (dob) {
        var now = new Date();
        var oneDay = 1000 * 60 * 60 * 24 ;

        var age = (now - dob) / (oneDay);
        var ageIn = null;
        if (age < 31) {
        	age = Math.floor(age);
        	ageIn = 'D';
        } else if (age < 730) {
        	age = age / 30.43;
        	age = Math.floor(age);
        	ageIn = 'M';
        } else {
        	age = age / 365.25;
        	age = Math.floor(age);
        	ageIn = 'Y';
        }
        document.sampleregform.age.value = age;
        document.sampleregform.ageIn.value = ageIn;
        if (age >= 1000) {
        	showMessage("js.registration.patient.valid.age.check.string");
        	document.sampleregform.age.value = "";
        	setTimeout("document.sampleregform.dobDay.focus()", 0);
        	return false;
        }
    }
    return true;
}

function incomingSampleCalculateAgeAndHijri() {
	if(getIncomingSampleAge(true, null)) {
		if(hijriPref == 'Y') {
			gregorianToHijriISR();
		}
		wipeOutAddedTests();
		return true;
	} else
		return false;
}

//Check the Cash Limit for Retail Patients
function checkIncomingCashLimitValidation(){
	var numPayments = getNumOfPayments();
	if (numPayments <= 0) return true;
	var amount =0;
	for (i=0; i<numPayments; i++){
		var totPayingAmt = "totPayingAmt"+i;
		var paymentModeId = "paymentModeId"+i;
		var paymentModelValue = $("#"+paymentModeId+" option:selected").val();
		if (paymentModelValue == -1) {
			var cashAmount = $("#"+totPayingAmt+"").val();
			amount += getAmount(cashAmount);
		}
		if (amount != 0 && amount > cashTransactionLimitAmt){
			alert("Total cash in aggregate from this patient in a day reaches the allowed Cash Transaction Limit of Rs." +cashTransactionLimitAmt+ ".");
			return false;
		}
	}
	return true;
}
