function selectAllSamples(){

	var asserElmts = document.getElementsByName("assert");
	if (document.getElementById("assertAll").checked)	{
        document.sampleAssersionForm.rejectbtn.disabled = true;
      for(var i=0;i<asserElmts.length;i++){
      if (asserElmts[i].disabled == false){
			asserElmts[i].checked=true;
			setIndexedValue("asserted",i,'Y');
			}
		}
	} else {
	   document.sampleAssersionForm.rejectbtn.disabled = false;
		for(var i=0;i<asserElmts.length;i++){
			asserElmts[i].checked=false;
			setIndexedValue("asserted",i,'N');
		}
	}
}

function setAsserted(obj){

	var row = getThisRow(obj);
	var index = getRowIndex(row);
	if(obj.checked)
		setIndexedValue("asserted",index,'Y');
	else
		setIndexedValue("asserted",index,'N');

	var assertedChkbox = document.getElementsByName("assert");
	var rejectbtndisable = false;
	 for(var i=0;i<assertedChkbox.length;i++){
			if(assertedChkbox[i].checked) {
				rejectbtndisable = true;
				break;
			}
		}
		document.sampleAssersionForm.rejectbtn.disabled = rejectbtndisable;
	return true;
}

function setIndexedValue(name, index, value) {
	var obj = getIndexedFormElement(sampleAssersionForm, name, index);
	if (obj)
		obj.value = value;
	return obj;
}

function getRowIndex(row) {
	return row.rowIndex - 1;
}

function validate() {
	var assertChkBoxs = document.getElementsByName('assert');
	var qtyDelivered = document.getElementsByName('qty_delivered');
	var qtyRecd = document.getElementsByName('qty_recd');
	var sampleSnos = document.getElementsByName('sample_sno');
	var asserted = document.getElementsByName('asserted');
	var atLeastOneCkd = false;

	for (var i=0; i<assertChkBoxs.length; i++) {
		if (assertChkBoxs[i].checked) {
			atLeastOneCkd = true;
			break;
		}
	}

	if (!atLeastOneCkd && assertChkBoxs.length!=0) {
		showMessage("js.laboratory.radiology.pendingsampleassertion.select.assertion");
		return false;
	}

	for (var i=0; i<assertChkBoxs.length; i++) {
		if (assertChkBoxs[i].checked) {
			if (qtyDelivered[i].value != qtyRecd[i].value) {
				var msg=getString("js.laboratory.radiology.pendingsampleassertion.qtyreceived");
				msg+=" ";
				msg+=sampleSnos[i].value;
				alert(msg);
				assertChkBoxs[i].checked = false;
				asserted[i].value = 'N';
				qtyRecd[i].focus();
				if (document.getElementById('assertAll').checked)
					document.getElementById('assertAll').checked = false;

						var isAssertChkBoxchecked = false;
						for (var i=0; i<assertChkBoxs.length; i++) {
						if (assertChkBoxs[i].checked)
							isAssertChkBoxchecked = true;
							    	}
							   if(!isAssertChkBoxchecked){
							   document.sampleAssersionForm.rejectbtn.disabled = false;
							}

				return false;
			}
		}
	}
	document.sampleAssersionForm.submit();
}
var sampleRejectDialog;

function initsampleRejectDialog() {

	var dialogDiv = document.getElementById("sampleRejectDialog");
	dialogDiv.style.display = 'block';
	sampleRejectDialog = new YAHOO.widget.Dialog("sampleRejectDialog",{
			width:"250px",
			context :["", "tr", "br"],
			visible:false,
			modal:true,
			constraintoviewport:true
		});
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:closesampleRejectDialog,
	                                                scope:sampleRejectDialog,
	                                                correctScope:true } );
	sampleRejectDialog.cfg.queueProperty("keylisteners", escKeyListener);
	sampleRejectDialog.render();

}

function closesampleRejectDialog(){
	sampleRejectDialog.hide();
}

function showsampleRejectDialog( obj ){

	var row = getThisRow(obj);
	var index = getThisRow( obj ).rowIndex;
	document.getElementById("sampleRejectId").value = index ;
	document.getElementById("srejection_remarks").value = "";
	sampleRejectDialog.cfg.setProperty("context",[obj, "tl", "bl"], false);
	sampleRejectDialog.show();
}

function setRejected(obj){
	var index = document.getElementById("sampleRejectId").value;
	var rejectremarks = document.getElementById("srejection_remarks").value;
	var rejectedbutton = document.getElementsByName("rejecticon");
	if( (null != rejectremarks) && ('' == trimAll(rejectremarks))){
		showMessage("js.laboratory.radiology.pendingsampleassertion.entersample.rejectionremarks");
		document.sampleAssersionForm.srejection_remarks.focus();
		return false;
	}
	var rowId = index - 1;
    setIndexedValue("rejection_remarks",rowId,rejectremarks);
    setIndexedValue("rejected",rowId,'Y');
    setIndexedValue("rejecticon",rowId,'1');
    document.getElementById('btnundo'+rowId).style.display = "block";
	document.getElementById('btndelete'+rowId).style.display = "none";
	sampleRejectDialog.hide();

	disableAssetionButton();


}


function disableAssetionButton(){
	var rejectedbutton = document.getElementsByName("rejecticon");
	var assertbtndisable = false;
	 for(var i=0;i<rejectedbutton.length;i++){
	  	if(rejectedbutton[i].value == '1') {
				assertbtndisable = true;
				break;
			}
		}
		document.sampleAssersionForm.assertbtn.disabled = assertbtndisable;
}

function setDeleteUndo(obj){
	var rowId = getThisRow( obj ).rowIndex-1;

    setIndexedValue("rejection_remarks",rowId,"");
    setIndexedValue("rejected",rowId,'N');
    setIndexedValue("rejecticon",rowId,'0');
	document.getElementById('btnundo'+rowId).style.display = "none";
	document.getElementById('btndelete'+rowId).style.display = "block";

	disableAssetionButton();
}
function rvalidate(){
	var assertChkBoxs = document.getElementsByName('assert');
	var sampleSnos = document.getElementsByName('sample_sno');
	var asserted = document.getElementsByName('asserted');
	var srejected = document.getElementsByName('rejected');
	var ratLeastOneCkd = false;

	for (var i=0; i<srejected.length; i++) {
		if (srejected[i].value == 'Y') {
		ratLeastOneCkd = true;

			break;
		}
	}

	if (!ratLeastOneCkd && srejected.length!=0) {
		showMessage("js.laboratory.radiology.pendingsampleassertion.select.rejection");
		return false;
	}

	document.sampleAssersionForm._method.value = "reject";
	document.sampleAssersionForm.submit();
}

function autoCompleteInHouse() {
	dataSource = new YAHOO.util.LocalDataSource(inHouses,{ queryMatchContains : true })
	dataSource.responseSchema = {fields : ["hospital_name"]};
	oAutoComp1 = new YAHOO.widget.AutoComplete('ih_name', 'inhouse_container', dataSource);
	oAutoComp1.maxResultsDisplayed = 15;
	oAutoComp1.allowBrowserAutocomplete = false;
	oAutoComp1.prehighlightClassName = "yui-ac-prehighlight";
	oAutoComp1.typeAhead = false;
	oAutoComp1.useShadow = false;
	oAutoComp1.minQueryLength = 0;
	oAutoComp1.animVert = false;
	oAutoComp1.forceSelection = true;
}

var sampleAssertionDialog;

function initSampleAssertionDialog() {

	var dialogDiv = document.getElementById("sampleAssertionDialog");
	dialogDiv.style.display = 'block';
	sampleAssertionDialog = new YAHOO.widget.Dialog("sampleAssertionDialog",{
			width:"600px",
			context :["sample assertion", "tr", "br"],
			visible:false,
			modal:true,
			constraintoviewport:true
		});
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:closesampleAssertionDialog,
	                                                scope:sampleAssertionDialog,
	                                                correctScope:true } );
	sampleAssertionDialog.cfg.queueProperty("keylisteners", escKeyListener);
	sampleAssertionDialog.render();
}

function closesampleAssertionDialog(){
	sampleAssertionDialog.hide();
}

function showSampleAssertionDetailsDialog(sampleCollectionId, obj) {
	sampleAssertionDialog.cfg.setProperty("context", [obj, "tr", "br"], false);
	sampleAssertionDialog.show();
	var sampleDateTime= null;
	var transferTime= null;
	var receiptTime= null;
	var splittedTestsName = null;
	var testsName = '';

	for(var i =0;i<sampleAssertionListJSON.length;i++){
		if(sampleAssertionListJSON[i].sample_collection_id == sampleCollectionId){
			sampleDateTime = formatDateTime(new Date(sampleAssertionListJSON[i].sample_date));
			if(sampleAssertionListJSON[i].transfer_time != null)
				transferTime = formatDateTime(new Date(sampleAssertionListJSON[i].transfer_time));
			if(sampleAssertionListJSON[i].receipt_time != null)
				receiptTime = formatDateTime(new Date(sampleAssertionListJSON[i].receipt_time));

			document.getElementById("tdSampleno").innerHTML = sampleAssertionListJSON[i].coll_sample_no;
			document.getElementById("tdMrno").innerHTML = sampleAssertionListJSON[i].mr_no;
			document.getElementById("tdPatientName").innerHTML = sampleAssertionListJSON[i].patient_name;
			document.getElementById("tdSampleDate").innerHTML = sampleDateTime;
			if (sampleAssertionListJSON[i].incoming_source_type == 'H')
				document.getElementById("tdOrigSampleNo").innerHTML = sampleAssertionListJSON[i].orig_sample_no;
			document.getElementById("tdCollectionCenter").innerHTML = sampleAssertionListJSON[i].collection_center;
			document.getElementById("tdIncomingHospital").innerHTML = sampleAssertionListJSON[i].ih_name;
			splittedTestsName = sampleAssertionListJSON[i].test_name.split(',');
			for(var j=0; j<splittedTestsName.length; j++) {
				testsName = testsName + splittedTestsName[j] + '<br/>';
			}
			document.getElementById("tdtests").innerHTML = testsName;
			document.getElementById("tdTransferTime").innerHTML = transferTime;
			document.getElementById("tdReceiptTime").innerHTML = receiptTime;
			document.getElementById("tdTransferDetails").innerHTML = sampleAssertionListJSON[i].transfer_other_details;
			document.getElementById("tdReceiptDetails").innerHTML = sampleAssertionListJSON[i].receipt_other_details;
			break;
		}
	}
}







