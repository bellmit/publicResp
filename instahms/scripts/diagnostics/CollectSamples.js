function init(){
	getSampleCollectionReport();
	initAddDialog();
	initShowDialog();
	clearEditDialog();
	document.sampleForm.hideCollected.checked = true;
	fillOrdernosCorresDate();
	onChangeSampleDepartment();
	onChangeDefaultOutsource();
}
function getValue(value){
  document.sampleForm.needPrint.value=value;
  var valid = true;
  //valid = valid && setSampleStatus();

  return valid;
}

function isSelectedCorresTest() {
	var collect = document.getElementsByName("collect");
	var generate = document.getElementsByName("generate");
	var sgSampelTypes = document.getElementsByName("sg_sample_type_id");
	var sampleTypes = document.getElementsByName("sample_type_id");
	var sampleFor = document.getElementsByName("sampleFor");
	var sampleTypeName = document.getElementsByName("sampleType");

	for(var i=0; i<collect.length; i++) {
		if(!generate[i].disabled && generate[i].checked) {
			for(var j=0; j<sampleTypes.length; j++) {
				if (sgSampelTypes[i].value == sampleTypes[j].value) {
					if (sampleFor[j].disabled || sampleFor[j].checked) {
						break;
					} else {
						var msg=getString("js.diagnostics.collectsample.pleaseselectatleastonetestfor" );
						msg+=sampleTypeName[j].value;
						msg+=getString("js.diagnostics.collectsample.sampletype");
						alert(msg);
						return false;
					}
				}
			}
		}
	}
	return true;
}

function validateCollect(id){
	var houseStatus = document.getElementsByName("house_status")[id];
	var sourceId = document.getElementsByName("outsource_dest_id")[id];
	var sampleNo = document.getElementsByName("sampleId")[id];
	var sampleType = document.getElementsByName("sample_type_id")[id];

	if( houseStatus.value == 'O' && sourceId.value == ''){
		showMessage("js.diagnostics.collectsample.selectoutsource");
		getEditDialog(sourceId);
		return false;
	}
	if(autogenerate != 'Y') {
		if(sampleNo.value == ''){
			showMessage("js.diagnostics.collectsample.samplenumbercannotbeempty");
			getEditDialog(sampleNo);
			return false;
		}
	}

	if(sampleType.value == ''){
		showMessage("js.diagnostics.collectsample.selectsampletype");
		getEditDialog(sampleType);
		return false;
	}
	return true;
}

function validateSampleNo(id){
	var sampleNo = document.getElementsByName("sampleId")[id];
	var sampleType = document.getElementsByName("sample_type_id")[id];
	if(autogenerate != 'Y') {
		if(sampleNo.value == ''){
			showMessage("js.diagnostics.collectsample.samplenumbercannotbeempty");
			getEditDialog(sampleNo);
			return false;
		}
	}
}

function validateSampleType(id){
	var sampleType = document.getElementsByName("sampleType")[id];
		if(sampleType.value == ''){
			showMessage("js.diagnostics.collectsample.selectsampletype");
			getEditDialog(sampleType);
			return false;
		}

	return true;
}

function getSampleCollectionReport(){

   if(document.sampleForm.needPrint.value== 'Y'){
		var visitId=document.sampleForm.visitid.value;
		var outsourceSampleNo="";
		var sampleNo=document.sampleForm.sampleNo.value;
		var outSourceSampleNos=document.sampleForm.outSourceSampleNo.value;
		var outSourceNames=document.sampleForm.outSourceName.value;
		var outSourceDestTypes=document.sampleForm.outsourceDestType.value;
	    var prescribedIds=document.sampleForm.prescribedIds.value;
	    var printType=document.sampleForm.printType.value;
	    var cPath=document.sampleForm.contextPath.value;
	    var mrNo=document.sampleForm.mrno.value;
	    var sampleDates = document.sampleForm.sampleDates.value;
	    var sampleTypes = document.sampleForm.sampleTypes.value;
	    var sampleStatus = document.sampleForm.outSourceSampleStatuses.value;
		var outSourceName="";
		if(outSourceNames != ""){
			var outSourceArray=outSourceNames.split(",");
			var sampleArray=outSourceSampleNos.split(",");
			var sampleStatusArray = sampleStatus.split(",");
			var sampleOutSourceTypeArray = outSourceDestTypes.split(",");
			for( var i=0;i<outSourceArray.length;i++){
				outSourceName=outSourceArray[i]
				outsourceSampleNo=sampleArray[i];
				if((!empty(outSourceName)) && (sampleStatusArray[i] == 'C' || sampleStatusArray[i] == 'A') && sampleOutSourceTypeArray[i] == 'O')
					window.open(cPath+"/pages/DiagnosticModule/DiagReportPrint.do?_method=generateSampleCollectionOutHouseReport&visitid="+visitId+"&outSourceName="+outSourceName+"&sampleNo="+outsourceSampleNo+"&prescribedId="+prescribedIds+"&format="+"pdf"+"&template_name="+document.sampleForm.sampleBardCodeTemplate.value);
		    }
	   }
	   if(sampleNo != ''){
		   if(printType=="SL"){
				window.open(cPath+"/pages/DiagnosticModule/DiagReportPrint.do?_method=generateSampleCollectionReport&visitid="+visitId+"&sampleNo="+sampleNo+"&template_name="+document.sampleForm.sampleBardCodeTemplate.value+"&sampleDates="+sampleDates+"&sampleTypes="+sampleTypes);
		   }else{
				window.open(cPath+"/Laboratory/GenerateSamplesBarCodePrint.do?method=execute&sampleNo="+sampleNo+"&barcodeType=sample&template_name="+document.sampleForm.sampleBardCodeTemplate.value+"&visitId="+visitId);
		   }
	   }
	}
}

function subscribeEscKeyEvent(dialog) {
	var kl = new YAHOO.util.KeyListener(document, { keys:27 },
		{ fn:dialog.cancel, scope:dialog, correctScope:true } );
	dialog.cfg.setProperty("keylisteners", kl);
}

function initAddDialog() {
	addDialog = new YAHOO.widget.Dialog("editDialog", {
					width:"900px",
					context :["", "tr", "br"],
					visible:false,
					modal:true,
					constraintoviewport:true,
				});
	addDialog.cfg.setProperty("context", ["myContextEl", "tr", "tl"]);

	addDialog.render();
	subscribeEscKeyEvent(addDialog);
}
function initShowDialog() {
	showDialog = new YAHOO.widget.Dialog("showDialog", {
					width:"300px",
					context :["", "tr", "br"],
					visible:false,
					modal:true,
					constraintoviewport:true,
				});
	showDialog.cfg.setProperty("context", ["myContextEl", "tr", "tl"]);

	showDialog.render();
	subscribeEscKeyEvent(showDialog);
}

function getThisRow(node) {
	return findAncestor(node, "TR");
}


function isFirstRow(id){
	if(id == 0)
		return true;
	else
		return false;
}

function isLastRow(id){
	if(document.getElementById("sampleNosList").rows.length - 2 == id)
		return true;
	else
		return false;
}

function isOnlyRow(){
	return document.getElementById("sampleNosList").rows.length == 3;
}

function getEditDialog(obj,btnDisable) {

	var row = getThisRow(obj);
	var id = getRowIndex(row);
	document.editform.editRowId.value = id;
	var tds = row.getElementsByTagName('td');
	var sampleDate = getIndexedValue("sg_sampleDate", id);
	var sampleTime = getIndexedValue("sg_sampleTime", id);
	var source = getIndexedValue("sg_sample_source_id", id);

	if(autogenerate == 'Y') {
		document.getElementById('sampleLblid').style.display = 'none';
		document.getElementById('sampleField').style.display = 'none';
	}

	if(isOnlyRow()){
		document.editform.prevBtn.disabled = true;
		document.editform.nextBtn.disabled = true;
	}else if(isFirstRow(id)){
		document.editform.prevBtn.disabled = true;
		document.editform.nextBtn.disabled = false;
	}else if(isLastRow(id+1)){
		document.editform.prevBtn.disabled = false;
		document.editform.nextBtn.disabled = true;
	}else{
		document.editform.prevBtn.disabled = false;
		document.editform.nextBtn.disabled = false;
	}

	if(autogenerate != 'Y')
		document.editform.eSampleId.value = getIndexedValue("sg_sample_sno", id);
	document.editform.eSampleDate.value = sampleDate == '' ? date : sampleDate;
	document.editform.eSampleTime.value = empty(sampleTime) ? time : sampleTime;
	var sg_qty = getIndexedValue("sg_qty", id);
	document.editform.eQty.value = sg_qty == '' ? 1 : sg_qty;
	document.editform.eSource.value = source;
	document.getElementById('eSampleType').innerHTML = getIndexedValue("sg_sampleType", id);

	addDialog.cfg.setProperty("context", [row.cells[7], "tr", "br"], false);
	addDialog.show();
	return false;
}

function onEdit(){
	var id = document.editform.editRowId.value;
	var row = getEditableRow(id);
	var valid = true;
	var sgSampleStatus = getIndexedValue("sg_sample_status", id);
	var collect = document.getElementsByName('collect')[id];
	var generate = document.getElementsByName('generate')[id];
	var i=0;
	if (isGenerateReq == 'Y')
		i=1;

	if (!document.editform.eSampleTime.value.trim() == '') {
		if (!isTime(document.editform.eSampleTime.value.trim())) {
			showMessage('js.diagnostics.collectsample.entertimein.hh.mm');
			document.editform.eSampleTime.focus();
			return false;
		}
	}

	if(sgSampleStatus == ''){

		if (autogenerate != 'Y') {
			setNodeText(row.cells[4+i], document.editform.eSampleId.value);
			setIndexedValue("sg_sample_sno",id,document.editform.eSampleId.value);
		}

	}
		setNodeText(row.cells[1+i], document.editform.eSampleDate.value+" "+document.editform.eSampleTime.value);
		setIndexedValue("sg_sample_date",id,document.editform.eSampleDate.value+" "+document.editform.eSampleTime.value);
		setIndexedValue("sg_sampleDate",id,document.editform.eSampleDate.value);
		setIndexedValue("sg_sampleTime",id,document.editform.eSampleTime.value);
		setNodeText(row.cells[5+i], document.editform.eSource.value == '' ? '' : getSelTextFromId('eSource'));
		var eQty = document.editform.eQty.value;
		if (eQty == '' || eQty == 0) {
			showMessage('js.diagnostics.collectsample.selectquantity');
			return;
		}
		setIndexedValue("sg_qty",id,document.editform.eQty.value);
		setIndexedValue("sg_sample_source_id",id,document.editform.eSource.value);
		setNodeText(row.cells[6+i], document.editform.eQty.value);

		if ((sgSampleStatus == '' || sgSampleStatus == 'P') && ((!collect.disabled && collect.checked) || (isGenerateReq == 'Y' ? (!generate.disabled && generate.checked) : false))) {
			var chkBoxname = 'generate';
			var obj = generate;
			if (!collect.disabled && collect.checked) {
				chkBoxname = 'collect';
				obj = collect;
			}
			setValuesToTestGrid(chkBoxname, obj);
		}

	addDialog.hide();
	return valid;
}

function validateEdit(sgID, tID){
	if(getIndexedValue("sample_status", tID) == 'C')
		return true;
	if(getIndexedValue("house_status", tID) == 'O'
				&& getIndexedValue("sg_outsource_dest_id", sgID) == '' && document.getElementsByName("collect")[sgID].checked ){

				var msg=getString("js.diagnostics.collectsample.selectoutsourcefortest");
				msg+=getIndexedValue("testName", tID);
				alert(mag);
				return false;
	}

	var presDate = getDateTime(getIndexedValue("prescribedDate",tID) , getIndexedValue("prescribedTime",tID) );
	var sampleDate = getDateTimeFromField(document.getElementsByName("sg_sample_date")[sgID],
			document.getElementsByName("sg_sampleTime")[sgID]);
	if (sampleDate < presDate) {
		var msg=getString("js.diagnostics.collectsample.samplecollectiondate.or.timecannotbeearlier");
		msg+= presDate;
		msg+=getString("js.diagnostics.collectsample.forthetest");
		msg+=getIndexedValue("testName", tID);
		alert(msg);
		return false;
	}
	return true;
}

function isSampleEditable(sampleStatus){
	if(sampleStatus == '')
		return true;
	else
		return false;
}

function isSampleStatusEditable(sampleStatus){
	if( sampleStatus != 'C' )
		return true;
	else
		return false;
}

function isSampleDateEditable(sampleStatus){
	if(sampleStatus != 'C' )
		return true;
	else
		return false;
}

function getRowIndex(row) {
	return row.rowIndex - 1;
}

function getIndexedValue(name, index) {
	var obj = getIndexedFormElement(sampleForm, name, index);
	if (obj)
		return obj.value;
	else
		return null;
}
function setIndexedValue(name, index, value) {
	var obj = getIndexedFormElement(sampleForm, name, index);
	if (obj)
		obj.value = value;
	return obj;
}

function getEditableRow(i) {
	i = parseInt(i);
	var table = document.getElementById("sampleNosList");
	return table.rows[i + 1];
}

function closeDialog() {
	addDialog.cancel();
	showDialog.cancel();
}

function setOhId(obj,ohid){
	ohid.value = obj.value;
}

function appendTemplateName(obj,sampleNo,sampleDate,sampleType){
	var href = cpath+"/pages/DiagnosticModule/DiagReportPrint.do?_method=generateSampleCollectionReport&visitid="
			+document.sampleForm.visitid.value+"&sampleNo='"+sampleNo+"'&template_name="+document.sampleForm.sampleBardCodeTemplate.value
			+"&sampleDates="+sampleDate+"&sampleTypes="+sampleType;
	if(samplePrintType != "SL"){
		href = cpath+"/Laboratory/GenerateSamplesBarCodePrint.do?method=execute&mrno="+document.sampleForm.mrno.value+"&sampleNo='"+sampleNo+"'&barcodeType=sample&template_name="
			+document.sampleForm.sampleBardCodeTemplate.value+"&sampleDates="+sampleDate+"&sampleTypes="+sampleType+"&visitId="+document.sampleForm.visitid.value;
	}
	obj.setAttribute('href',href);
	return true;
}

function showNextOrPrevSample(obj){
	var id = document.editform.editRowId.value;
	var row = getEditableRow(id);
	var btnDisable = false;


	if(obj.name == "prevBtn")
		id--;
	else if(obj.name == "nextBtn")
		id++;

	if (id >= 0 && getIndexedValue("sg_sample_type_id", id) != null &&  getIndexedValue("sg_sample_type_id", id) != ""){
		YAHOO.util.Dom.removeClass(row, 'editing');
		row = getEditableRow(id);

		if (obj.name == "prevBtn")
			document.editform.prevBtn.disabled = false;
		else if(obj.name == 'nextBtn')
			document.editform.nextBtn.disabled = false;
		btnDisable = false
	}else {
		if (obj.name == "prevBtn")
			document.editform.nextBtn.disabled = true;
		else if(obj.name == "nextBtn")
			document.editform.prevBtn.disabled = true;
		btnDisable = true;
	}
	if(onEdit())
		getEditDialog(row);
}

function disableGenerate(collect){
	if (isGenerateReq == 'Y') {
		var row = getThisRow(collect);
		var id = getRowIndex(row);
		var generate = document.getElementsByName("generate");
		var sampleStatus = document.getElementsByName("sg_sample_status");
		if(collect.checked){
			generate[id].checked = true;
			row.setAttribute("class","added");
		}else if(sampleStatus[id].value == '' ){
			if(generate[id].checked){
				generate[id].checked = false;
				row.setAttribute("class","");
			}
		}
	}
}

function setRowClass(ckBx){
	var row = getThisRow(ckBx);
	if(ckBx.checked)
		row.setAttribute("class","added");
	else
		row.setAttribute("class","");
}

function setSampleStatus(){
	var valid = true;
	var sampleStatus = document.getElementsByName("sample_status");
	var genearte = document.getElementsByName("generate");
	var collect = document.getElementsByName("collect");
	var houseStatus = document.getElementsByName("house_status");
	var ohId = document.getElementsByName("outsource_dest_id");

	for(var i = 0; i< collect.length; i++){
		if( !collect[i].disabled && collect[i].checked ) {
			if(!validateCollect(i)){
				valid = false;
				break;
			}
			sampleStatus[i].value = 'C';
		}else if(genearte[i] && !genearte[i].disabled && genearte[i].checked ){
			if(!validateSampleNo(i)){
				valid = false;
				break;
			}
			sampleStatus[i].value = 'P';
		}
	}
	return valid;

}



function clearEditDialog(){
	if(autogenerate != 'Y')
		document.editform.eSampleId.value = '';
}

function onChangeSampleDepartment() {
	var num = getNumOfSamples();
   	var table = document.getElementById("sampleList");
   	var filterDepartment = document.sampleForm.filterSamples.value;
   	var filterOrderno = document.sampleForm.fiterOrderno.value;
   	var filterDate = document.sampleForm.filterDate.value;
   	var isHideCollected = document.sampleForm.hideCollected;

	for (var i=1; i<=num; i++) {
		var row = table.rows[i];
		var sampleDept = getElementByName(row, 'ddept_id').value;
		var orderNo = getElementByName(row, 'commonOrderId').value;
		var orderDate = getElementByName(row, 'prescribedDate').value;
		var existingSampleStatus = getElementByName(row, 'existingSampleStatus').value;
		var show = true;
		if ((filterDepartment != "") && (filterDepartment != sampleDept) ||
				(filterOrderno != "" && filterOrderno != orderNo) || (filterDate != "" && filterDate != orderDate) ||
						(isHideCollected.checked && (existingSampleStatus == "C" || existingSampleStatus == "A" )))
			show = false;
		if (show) {
			row.style.display = "";
		} else {
			row.style.display = "none";
		}
	}
}
  //This enhancement is for #44726.
function onChangeDefaultOutsource(){
    var tableObj = document.getElementById('sampleList');
   	var rowsLength = tableObj.rows.length;
		for (var i=1; i<rowsLength; i++) {
			i = parseInt(i);
			var testRow = tableObj.rows[i];
			var id = getRowIndex(testRow);
			var testName = getIndexedValue('testName', id);
			var sampleTypeForTest = getIndexedValue('sampleTypeForTests', id);
			var outSourceNameList = getIndexedValue('outSourceNameList', id);
			var existingStatus = getIndexedValue('existingStatus', id);
			if(sampleTypeForTest != '' && outSourceNameList.type != 'hidden' && existingStatus == ''){
			    addToSamplesGrid(testRow);
			    }
	   }
}

function getNumOfSamples() {
	return document.getElementById("sampleList").rows.length-1;
}

function selectAllSamples(allChkBx,chkBxName) {
	var sampleChkElmts = document.getElementsByName(chkBxName);
	if (allChkBx.checked)	{
		for(var i=0;i<sampleChkElmts.length;i++){
			var row = getThisRow(sampleChkElmts[i]);
			if( row.style.display ==  "" && !sampleChkElmts[i].disabled ){
				sampleChkElmts[i].checked=true;
				setValuesToTestGrid(chkBxName, sampleChkElmts[i]);
				if( chkBxName == 'collect' )
					disableGenerate(sampleChkElmts[i]);
				else
					setRowClass(sampleChkElmts[i]);
			}
			if( allChkBx.id == 'collectAll' && isGenerateReq == 'Y' ){
				if(document.getElementById("generateAll")){
					document.getElementById("generateAll").checked =true;
					document.getElementById("generateAll").disabled =true;
				}
			}

		}
	} else {
		for(var i=0;i<sampleChkElmts.length;i++){
			var row = getThisRow(sampleChkElmts[i]);
			if( row.style.display ==  "" && !sampleChkElmts[i].disabled ){
				sampleChkElmts[i].checked=false;
				setValuesToTestGrid(chkBxName, sampleChkElmts[i]);
				if( chkBxName == 'collect' )
					disableGenerate(sampleChkElmts[i]);
				else
					setRowClass(sampleChkElmts[i]);
			}
			if( allChkBx.id == 'collectAll' && isGenerateReq == 'Y'){
				document.getElementById("generateAll").disabled = false;
				document.getElementById("generateAll").checked = false;
			}

		}
	}
}


function unSelectAllSamples(allChkBx,chkBxName) {
	var sampleChkElmts = document.getElementsByName(chkBxName);
	if (!allChkBx.disabled)	{
		for(var i=0;i<sampleChkElmts.length;i++){
			var row = getThisRow(sampleChkElmts[i]);
			if( row.style.display ==  "" && !sampleChkElmts[i].disabled){
				sampleChkElmts[i].checked=false;
				if( chkBxName == 'collect' )
					disableGenerate(sampleChkElmts[i]);
			}
			if( allChkBx.id == 'collectAll' ){
				document.getElementById("generateAll").disabled = false;
			}

		}
	}
}


function fillOrdernosCorresDate() {
	var req = null;
	var visitId=document.sampleForm.visitid.value;
	if(window.XMLHttpRequest){
		req = new XMLHttpRequest();
	} else if(window.ActiveXObject){
		req = new ActiveXObject("MSXML2.XMLHTTP");
	}
	var date = document.getElementById("filterDate").value;
	var urlPath = cpath + "/Laboratory/"+collectSampleUrl+".do?_method=getOrdernosCorrespondingTotheDate&visitID="+visitId+"&date="+date+"&testId="+testId;
	req.open("GET",urlPath.toString(), true);
	req.setRequestHeader("Content-Type", "text/plain");
	req.send(null);
	req.onreadystatechange = function() {
		if (req.readyState == 4 && req.status == 200 ) {
			if (req.responseText != null && req.responseText != '') {
				var orderNos = eval(req.responseText);
				loadSelectBox(document.sampleForm.fiterOrderno, orderNos, "common_order_id", "common_order_id", "All", "");

			}
		}
	}
}

function uncheckCheckBox() {
	document.getElementById('collectAll').checked = false;
	document.getElementById('generateAll').checked = false;
	unSelectAllSamples(document.getElementById('collectAll'),'collect');
	unSelectAllSamples(document.getElementById('generateAll'),'generate');
}

function setValuesToTestGrid(chkBoxName, obj) {

	var sgRow = getThisRow(obj);
	var sgid = getRowIndex(sgRow);
	var sgOhid = getIndexedValue("sg_outsource_dest_id", sgid);
	var sgSampleDate = getIndexedValue("sg_sample_date", sgid);
	var sgDate = getIndexedValue("sg_sampleDate", sgid);
	var sgTime = getIndexedValue("sg_sampleTime", sgid);
	var sgSampleTypeID = getIndexedValue("sg_sample_type_id", sgid);
	var sgSourceID = getIndexedValue("sg_sample_source_id", sgid);
	var sgQty = getIndexedValue("sg_qty", sgid);
	var sgHouseStatus = getIndexedValue("sg_house_status", sgid);
	var sg_sample_sno = getIndexedValue("sg_sample_sno", sgid);
	var sg_outsource_dest_id = getIndexedValue("sg_outsource_dest_id", sgid);
	var collectable =  getIndexedValue("collectable");
	var sgSampleStatus = '';
	if (chkBoxName == 'generate')
		sgSampleStatus = obj.checked ? 'P' : '';
	if (chkBoxName == 'collect')
		sgSampleStatus = obj.checked ? 'C' : '';

	if (autogenerate != 'Y' && (chkBoxName == 'generate' || chkBoxName == 'collect') && sg_sample_sno == '') {
		showMessage("js.diagnostics.collectsample.samplenorequired");
		obj.checked = false;
		if (isGenerateReq == 'Y')
		document.getElementsByName("generate")[sgid].checked = false;
		return;
	}

	var tableObj = document.getElementById('sampleList');
		var rowsLength = tableObj.rows.length;
		for (var i=1; i<rowsLength; i++) {
			i = parseInt(i);
			var testRow = tableObj.rows[i];
			var id = getRowIndex(testRow);
			var SampleTypeID = getIndexedValue("sample_type_id", id);
			var exSampleStatus = getIndexedValue("existingStatus", id);
			var sampleSno = getIndexedValue("sampleId", id);
			var extSampleSno = getIndexedValue("existingSampleNo", id);
			var houseStatus = getIndexedValue("house_status", id);
			var collectable = getIndexedValue("collectable", id);
			var outSourceId = getIndexedValue("outSourceId", id);

			if (sgSampleTypeID == SampleTypeID && sg_outsource_dest_id == outSourceId && ('O' == houseStatus ? outSourceId != '' : true)
					&&	((autogenerate == 'N' && exSampleStatus == '') ? true : sg_sample_sno == sampleSno) && exSampleStatus != 'C' && testRow.style.display != 'none') {

				if (obj.checked == false) {
					setChkboxValue("sampleFor", id, obj.checked);
					setIndexedValue("sample_status", id, getIndexedValue("existingStatus", id));

					if (autogenerate != 'Y' && extSampleSno == '') {
						setIndexedValue("sampleId", id, '');
						setNodeText(testRow.cells[8], '');
					}
				} else {
					if(collectable == 'false')//dependent samples shd be avoided
						continue;
					if (!(validateEdit(sgid, id))) {
						setChkboxValue("sampleFor", id, false);
						obj.checked = false;
						return;
					}

					setChkboxValue("sampleFor", id, obj.checked);
					setIndexedValue("sample_status", id, sgSampleStatus);
					if (autogenerate != 'Y' && extSampleSno == '') {
						setIndexedValue("sampleId", id, sg_sample_sno);
						setNodeText(testRow.cells[8], sg_sample_sno);
					}
				}
			}
		}
		setIndexedValue('sg_selected_status', sgid, sgSampleStatus);
}

function addToSamplesGrid(obj) {

	var testGridRow = getThisRow(obj);
	var testGridID = getRowIndex(testGridRow);
	var sampleTypeObj = getIndexedFormElement(sampleForm, 'sampleTypeForTests', testGridID);
	var outSourceObj = getIndexedFormElement(sampleForm, 'outSourceNameList', testGridID);
	var selectedSampID = sampleTypeObj.value;
	var tgSampleNo = getIndexedFormElement(sampleForm, 'sampleId', testGridID).value;
	var seletedSamTxt = sampleTypeObj.options[sampleTypeObj.selectedIndex].text;
	var exSampleStatus = getIndexedFormElement(sampleForm, 'existingStatus', testGridID);
	var presentStatus = getIndexedFormElement(sampleForm, 'sample_status', testGridID);
	var sampleFor = document.getElementsByName('sampleFor')[testGridID];
	var outSourceID = "";
	var outSourceText = "";

	var index=0;
	if (isGenerateReq == 'Y')
		index=1;

	if (exSampleStatus.value == '' && presentStatus.value != '') {
		sampleFor.checked = false;
		setIndexedValue("sample_status", testGridID, '');
	}

	if (outSourceObj.type != 'hidden') {
		outSourceID = outSourceObj.options[outSourceObj.selectedIndex].value;
		outSourceText = outSourceObj.options[outSourceObj.selectedIndex].text;
		setIndexedValue('outSourcename', testGridID, outSourceText);
		setIndexedValue('outSourceId', testGridID, outSourceID);
	}
	setIndexedValue("sample_type_id", testGridID, selectedSampID);
	setIndexedValue("sampleType", testGridID, seletedSamTxt);
	clearRow();

	if (selectedSampID != '' && (outSourceObj.type != 'hidden' ? (outSourceID != '') : true)) {
		var tableObj = document.getElementById('sampleNosList');
		var rowsLength = tableObj.rows.length;
			for (var i=1; i<rowsLength-1; i++) {
				i = parseInt(i);
				var sampleRow = tableObj.rows[i];
				var sampleRowID = getRowIndex(sampleRow);
				var sgSampleTypeID = getIndexedValue("sg_sample_type_id", sampleRowID);
				var sgOutsourceID = getIndexedValue("sg_outsource_dest_id", sampleRowID);
				var sgSampleNo = getIndexedValue("sg_sample_sno", sampleRowID);
				var sgOutSourceChain = getIndexedValue("sg_outsource_chain", sampleRowID);
				if (selectedSampID == sgSampleTypeID && sgOutsourceID == outSourceID && sgSampleNo == tgSampleNo) {
					return;
				}
			}
			var hiddenRow = tableObj.rows[tableObj.rows.length - 1];
			var clonedRow = hiddenRow.cloneNode(true);
			clonedRow.style.display = '';
			tableObj.tBodies[0].insertBefore(clonedRow, hiddenRow);
			var sampleGridID = getRowIndex(clonedRow);
			setNodeText(clonedRow.cells[2+index], seletedSamTxt);
			setNodeText(clonedRow.cells[3+index], sampleContainers[selectedSampID]);
			setNodeText(clonedRow.cells[6+index], 1);
			if(outSourceObj.type != 'hidden') {
				setNodeText(clonedRow.cells[7+index], outSourceText);
				setIndexedValue('sg_outsource_dest_id', sampleGridID, outSourceID);
			}
			setIndexedValue("sg_sample_type_id", sampleGridID, selectedSampID);
			setIndexedValue("sg_sampleType", sampleGridID, seletedSamTxt);
	}
}

function setChkboxValue(name, index, value) {
	var obj = getIndexedFormElement(sampleForm, name, index);
	if (obj)
		obj.checked = value;
	return obj;
}

function setEmptyToStatus(obj) {
	var testRow = getThisRow(obj);
	var testRowID = getRowIndex(testRow);

	if (!obj.checked) {
		setIndexedValue("sample_status", testRowID, '');
	} else if(obj.checked) {
		setIndexedValue("sample_status", testRowID, 'setInAction');
	}
}

function clearRow() {
	var sgSampleTypeIDs = document.getElementsByName('sg_sample_type_id');
	var sgExistingStatus = document.getElementsByName('sg_sample_status');
	var tgExistingStatus = document.getElementsByName('existingStatus');
	var tgSampleTypeIDs = document.getElementsByName('sample_type_id');
	var tgSampleNo = document.getElementsByName("sampleId");
	var tgOsIDs = document.getElementsByName('outSourceId');
	var sgOsIDs =  document.getElementsByName('sg_outsource_dest_id');
	var sgSampleNo = document.getElementsByName("sg_sample_sno");
	var sampleGridTableObj = document.getElementById('sampleNosList');

	for (var i=0; i<sgSampleTypeIDs.length-1; i++) {
		if (sgExistingStatus[i].value == '') {
			for (var j=0; j<tgExistingStatus.length; j++) {
				if (tgExistingStatus[j].value == '' && sgSampleTypeIDs[i].value == tgSampleTypeIDs[j].value
						&& sgOsIDs[i].value == tgOsIDs[j].value && sgSampleNo[i].value == tgSampleNo[j].value) {
					break;
				} else if (j == tgExistingStatus.length-1) {
					sampleGridTableObj.deleteRow(i+1);
				}
			}
		}
	}
}

function getRejectionDetails(obj,prescribedId){

	var table = document.getElementById('detailspanel');
		for (var i=0; i<table.rows.length; ) {
			table.deleteRow(i);
		}
		for(var i=0; i< rejectionList.length; i++){
			var reject = rejectionList[i];
			if(prescribedId == reject.test_prescribed_id){
				var row = table.insertRow(-1);
				var cell = row.insertCell(-1);
				cell.innerHTML = 'Sample No \t:'+reject.sample_sno;
				var row = table.insertRow(-1);
				var cell = row.insertCell(-1);
				cell.innerHTML = 'Collected on \t:'+formatDateTime(new Date(reject.sample_date));
				var row = table.insertRow(-1);
				var cell = row.insertCell(-1);
				cell.innerHTML = 'Rejected By \t:'+reject.rejected_by;
				var row = table.insertRow(-1);
				var cell = row.insertCell(-1);
				cell.innerHTML = 'Rejected on \t:'+formatDateTime(new Date(reject.rejected_time));
				var row = table.insertRow(-1);
				var cell = row.insertCell(-1);
				cell.innerHTML = 'Rejection Remarks \t:'+reject.rejection_remarks;
				var row = table.insertRow(-1);
				var cell = row.insertCell(-1);
				if(i<rejectionList.length-1)
				cell.innerHTML = '---------------------------------------------------';
			}
		}
	showDialog.cfg.setProperty("context", [obj, "tr", "br"], false);
	showDialog.show();
}
