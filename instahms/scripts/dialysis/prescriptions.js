var toolbar = {};
	toolbar.Edit= {
		title: toolbarOptions["editprescriptions"]["name"],
		imageSrc: "icons/Edit.png",
		href: 'dialysis/DialysisPrescriptions.do?_method=show',
		onclick: null,
		description: toolbarOptions["editprescriptions"]["description"]
	};

/*function doSearch(){
	var mrno = document.forms[0].mr_no.value ;
	if(mrno == ""){
		showMessage("Please enter MRNO to search");
		document.forms[0].mr_no.focus();
		return false;
	}
	return true;
}*/

function clearFields(form){
	var form;
	if(form == 'detailsForm')
		form = document.detailsForm;
	else
		form = document.forms[0];
	form.mr_no.value="";
	form.presc_doctor.value="";
	form.start_date.value="";
	form.end_date.value="";
}

function clearSearch(){
	clearFields('');
	document.forms[0].status[0].checked= true;
	enableCheckGroupAll(document.forms[0].status[0]);
}


function fetchMrnoDetails(){
	var mrno = document.searchForm.mr_no.value ;
	if(mrno !=""){
		document.searchForm.action="DialysisPrescriptions.do?_method=add";
		document.searchForm.submit();
	}else{
		return false;
	}
}
/*function makeDisabled(){
	var method = document.detailsForm._method.value;
	if(method== 'update' && loadStatus!='' && (loadStatus=='I' || loadStatus=='A')){
		document.detailsForm.presc_doctor.disabled = true;
		document.detailsForm.start_date.disabled = true;
		document.detailsForm.presc_date.disabled = true;
		document.detailsForm.shift1.disabled = true;
		document.detailsForm.shift2.disabled = true;
		document.detailsForm.shift3.disabled = true;
		document.detailsForm.day1.disabled = true;
		document.detailsForm.day2.disabled = true;
		document.detailsForm.day3.disabled = true;
		document.detailsForm.day4.disabled = true;
		document.detailsForm.day5.disabled = true;
		document.detailsForm.shift4.disabled = true;
		document.detailsForm.shift5.disabled = true;
		document.detailsForm.dialysate_type_id.disabled = true;
		document.detailsForm.dialyzer_type_id.disabled = true;
		//document.detailsForm.access_type_id.disabled = true;
		//document.detailsForm.access_site_id.disabled = true;
		document.detailsForm.heparin_profile.disabled = true;

		if(loadStatus=='I'){
			document.detailsForm.status.disabled = true;
			document.detailsForm.end_date.disabled = true;
		}
	}
}*/
function funValidateAndSubmit(){
	var method = document.detailsForm._method.value;
	var detailsForm = document.detailsForm.prescDuration.value;
	var tempAccessTblobj =	document.getElementById('TaccessTbl');
	var permanentAccessTblobj = document.getElementById('PaccessTbl');
	document.detailsForm.weekly_frequency.value = trim(document.detailsForm.weekly_frequency.value);

	if(method=='create' || method=='update'){
		var detailsForm = document.detailsForm;

		var mr_no = document.searchForm.mr_no.value;
		if(mr_no == ""){
			showMessage("js.clinicaldata.scorecard.entermrno");
			searchForm.mr_no.focus();
			return false;
		}

		var presc_doctor = detailsForm.presc_doctor.value;
		if(presc_doctor == ""){
			showMessage("js.clinicaldata.scorecard.prescribingdoctor");
			detailsForm.presc_doctor.focus();
			return false;
		}

		var presc_date = detailsForm.presc_date.value;
		if(presc_date == ""){
			showMessage("js.clinicaldata.scorecard.prescribingdate");
			detailsForm.presc_date.focus();
			return false;
		}

		var start_date = detailsForm.start_date.value;
		if(start_date == ""){
			showMessage("js.clinicaldata.scorecard.startdate");
			detailsForm.start_date.focus();
			return false;
		}

		var duration = detailsForm.prescDuration.value;
		if(duration == ""){
			showMessage("js.clinicaldata.scorecard.duration");
			detailsForm.prescDuration.focus();
			return false;
		}

		var weeklyFrequency = detailsForm.weekly_frequency.value;
		if (empty(weeklyFrequency)) {
			showMessage('js.clinicaldata.scorecard.weeklyfrequency');
			detailsForm.weekly_frequency.focus();
			return false;
		}

		var dialysate_type_id = detailsForm.dialysate_type_id.value;
		if(dialysate_type_id == ""){
			showMessage("js.clinicaldata.scorecard.dialysatetype");
			detailsForm.dialysate_type_id.focus();
			return false;
		}

		var dialyzer_type_id = detailsForm.dialyzer_type_id.value;
		if(dialyzer_type_id == ""){
			showMessage("js.clinicaldata.scorecard.dialyzer");
			detailsForm.dialyzer_type_id.focus();
			return false;
		}


		var day1 = detailsForm.day1.value;
		var day2 = detailsForm.day2.value;
		var day3 = detailsForm.day3.value;

		var shift1 = detailsForm.shift1.value;
		var shift2 = detailsForm.shift2.value;
		var shift3 = detailsForm.shift3.value;

		if(tempAccessTblobj.rows.length <= 3 && permanentAccessTblobj.rows.length <= 3) {
			showMessage("js.clinicaldata.scorecard.temporpermanentaccess");
			return false;
		}

	}

	if (isSelectedAllForDelete()) {
		showMessage("js.clinicaldata.scorecard.accesstype.required");
		return false;
	}

	if(duration!=''){
		if(!calcDuration())
			return false;
	}

	var status = document.detailsForm.status.value;

	if(method== 'create' && status == loadStatus){
		status = status=='A'?'Active':'Pending';
		var msg=status;
		msg+=getString("js.dialysismodule.commonvalidations.prescription.exists");
		alert(msg);
		return false;
	}
	document.detailsForm.mr_no.value= document.searchForm.mr_no.value;
	document.detailsForm.action="DialysisPrescriptions.do?_method="+method;
	document.detailsForm.submit();
}
function init(){
	temporaryAccessDialog();
	permanentAccessDialog();
	Insta.initMRNoAcSearch(contextPath, "mr_no", "mrnoAcDropdown", "all",
			function(type, args) {
				fetchMrnoDetails();
			}
	);
}
function initPresList(){
	Insta.initMRNoAcSearch(contextPath, "mrno", "mrnoContainer", "all",null,null);
	createToolbar(toolbar);
}
function preFillDialysateDetails(){
	var method = document.detailsForm._method.value;
	var dialysateID = document.detailsForm.dialysate_type_id.value;

	for(var i=0;i<DialysateDetails.length;i++){
		if(dialysateID == DialysateDetails[i]["dialysate_type_id"]){
			document.detailsForm.potassium.value = DialysateDetails[i]["potasium"];
			document.detailsForm.calcium.value = DialysateDetails[i]["calcium"];
			document.detailsForm.magnesium.value = DialysateDetails[i]["magnesium"];
			document.detailsForm.glucose.value = DialysateDetails[i]["glucose"];
			document.detailsForm.sodium.value = DialysateDetails[i]["sodium"];
		}else if(dialysateID == ''){
			document.detailsForm.potassium.value ='';
			document.detailsForm.calcium.value = '';
			document.detailsForm.magnesium.value = '';
			document.detailsForm.glucose.value = '';
			document.detailsForm.sodium.value = '';
		}
	}
}

function setPrescDuration(){

	if(prescDuration!='' && prescDuration!=null){
		var hh = parseInt(prescDuration/60);
		document.detailsForm.prescDuration.value= hh;
	}
}

function calcDuration(){

	var prescDuration = document.detailsForm.prescDuration.value;
	var duration ;

	if(prescDuration!='') {

	    duration=prescDuration*60;
		duration = parseInt(duration);
        document.detailsForm.duration.value = duration;

	} else {
		  document.detailsForm.duration.value = 0;

	  }

	 return true;
}

var permanentAccessDialog;
var newRowinserted4P = true;
var currentRow4P = null;

function permanentAccessDialog() {

	var PerAccessDIV = document.getElementById("PerAccessDIV");
	PerAccessDIV.style.display = 'block';
	permanentAccessDialog = new YAHOO.widget.Dialog('PerAccessDIV', {
				width:"800px",
		        visible: false,
		        modal: true,
		        constraintoviewport: true
		});

	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleCancelP,
	                                                scope:permanentAccessDialog,
	                                                correctScope:true } );
	permanentAccessDialog.cfg.queueProperty("keylisteners", [escKeyListener]);
	permanentAccessDialog.cancelEvent.subscribe(cancelP);
	permanentAccessDialog.render();

}

var tempAccessDialog;
var newRowinserted4T = true;
var currentRow4T = null;

function temporaryAccessDialog() {

var TempAccessDIV = document.getElementById("TempAccessDIV");
	TempAccessDIV.style.display = 'block';
	tempAccessDialog = new YAHOO.widget.Dialog('TempAccessDIV', {
				width:"800px",
				visible:false,
				modal:true,

		});

	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleCancelT,
	                                                scope:tempAccessDialog,
	                                                correctScope:true } );
	tempAccessDialog.cfg.queueProperty("keylisteners", [escKeyListener]);
	tempAccessDialog.cancelEvent.subscribe(cancelT);
	tempAccessDialog.render();

}

function handleCancelP() {

	permanentAccessDialog.cancel();
}

function handleCancelT() {

	tempAccessDialog.cancel();
}

function cancelT() {
	newRowinserted4T = true;
	currentRow4T = null;
}

function showDialogT(obj) {

	document.getElementById('TaccessTypes').value = '';
	document.getElementById('Tdate').value = '';
	document.getElementById('Tdoctor').value = '';
	document.getElementById('Tsite').value = '';
	document.getElementById('TdateRemoval').value = '';
	document.getElementById('Thname').value='';
	document.getElementById('Tarea').value = '';

	document.getElementById('Taccesschange').
				options[document.getElementById('Taccesschange').selectedIndex].value = '';

    document.getElementById('TaccesschangeTR').style.display="none";
	tempAccessDialog.cfg.setProperty("context", [obj, "tr", "bl"], false);
	tempAccessDialog.show();

}

function showDialogP(obj) {
	document.getElementById('PaccessTypes').value = '';
	document.getElementById('Pdate').value = '';
	document.getElementById('Pdoctor').value = '';
	document.getElementById('Psite').value = '';
	document.getElementById('PdateRemoval').value = '';
	document.getElementById('Phname').value='';
	document.getElementById('Parea').value = '';
	document.getElementById('Paccesschange').
				options[document.getElementById('Paccesschange').selectedIndex].value = '';

	document.getElementById('PaccesschangePer').style.display="none";
	permanentAccessDialog.cfg.setProperty("context", [obj, "tr", "bl"], false);
	permanentAccessDialog.show();

}

function Tempaccesschange(){

	var currentRowIndex = -1;
	if (newRowinserted4T == false) {
		var rowObj = getThisRow(currentRow4T, 'TR');
		var parts = rowObj.id.split('row');
		currentRowIndex = parseInt(parts[1]);

	var tableObj = document.getElementById('TaccessTbl');
	var rowsLength = tableObj.rows.length;
	var templateRow = tableObj.rows[rowsLength-1];
	var newRow = '';

	if (newRowinserted4T) {
		var id = rowsLength-2;
		newRow = templateRow.cloneNode(true);
		newRow.style.display = '';
		newRow.id = 'row'+(rowsLength-3);
		getElementByName(newRow, 'selectedrow4temp').id = 'selectedrow4temp'+id;
		getElementByName(newRow, 'added4temp').id = 'added4temp'+id;
		getElementByName(newRow, 'added4temp').value = 'Y';
		YAHOO.util.Dom.insertBefore(newRow, templateRow);
	} else {
			newRow = getThisRow(currentRow4T, 'TR');
		}

   	var accessTypeID = getElementByName(newRow, "temporary_access_type_id").value;
	var savedAccessType = getElementByName(newRow, "access_type_id_t").value;
	var selectedAccessType = document.getElementById('TaccessTypes').options[document.getElementById('TaccessTypes').selectedIndex].value;
	var changereasonvalue =document.getElementById('Taccesschange').options[document.getElementById('Taccesschange').selectedIndex];


	if (!empty(accessTypeID)) {
	   	if (selectedAccessType != '' && savedAccessType != selectedAccessType || empty(changereasonvalue)) {
	    		document.getElementById('TaccesschangeTR').style.display="table-row";
        } else {
				document.getElementById('TaccesschangeTR').style.display="none";
        	}

	  	 return false;
	}
  } else {
 		 document.getElementById('TaccesschangeTR').style.display="none";
  	  }
 	return true;
}

function Permanentaccesschange(){

 	var currentRowIndex = -1;
	if (newRowinserted4P == false) {
		var rowObj = getThisRow(currentRow4P, 'TR');
		var parts = rowObj.id.split('row');
		currentRowIndex = parseInt(parts[1]);

	var tableObj = document.getElementById('PaccessTbl');
	var rowsLength = tableObj.rows.length;
	var templateRow = tableObj.rows[rowsLength-1];
	var newRow = '';

	if (newRowinserted4P) {
		var id = rowsLength-2;
		newRow = templateRow.cloneNode(true);
		newRow.style.display = '';
		newRow.id = 'row'+(rowsLength-3);
		getElementByName(newRow, 'selectedrow4per').id = 'selectedrow4per'+id;
		getElementByName(newRow, 'added4per').id = 'added4per'+id;
		getElementByName(newRow, 'added4per').value = 'Y';
		YAHOO.util.Dom.insertBefore(newRow, templateRow);
	} else {
			newRow = getThisRow(currentRow4P, 'TR');
		}

	var PaccessTypeID = getElementByName(newRow, "permanent_access_type_id").value;
	var PsavedAccessType = getElementByName(newRow, "access_type_id_p").value;
	var PselectedAccessType = document.getElementById('PaccessTypes').options[document.getElementById('PaccessTypes').selectedIndex].value;
	var Pchangereasonvalue=document.getElementById("Paccesschange");

		if (!empty(PaccessTypeID)) {
			if (PselectedAccessType != '' && PsavedAccessType != PselectedAccessType || empty(Pchangereasonvalue)) {

				 document.getElementById('PaccesschangePer').style.display="table-row";
			 } else {
				  	document.getElementById('PaccesschangePer').style.display="none";
				 }
			return false;
		}
	} else {
		  document.getElementById('PaccesschangePer').style.display="none";
		}

	return true;
}

function addToTableT() {

	if(document.getElementById('TaccessTypes').value == '' || document.getElementById('Tsite').value == '') {
		showMessage("js.clinicaldata.scorecard.tempaccesstype.accessfield.notempty");
		return false;
	}

     var currentRowIndex = -1;
	if (newRowinserted4T == false) {
		var rowObj = getThisRow(currentRow4T, 'TR');
		var parts = rowObj.id.split('row');
		currentRowIndex = parseInt(parts[1]);
	}

	var tableObj = document.getElementById('TaccessTbl');
	var rowsLength = tableObj.rows.length;
	var templateRow = tableObj.rows[rowsLength-2];
	var newRow = '';

	if (newRowinserted4T) {
		var id = rowsLength-2;
		newRow = templateRow.cloneNode(true);
		newRow.style.display = '';
		newRow.id = 'row'+(rowsLength-3);
		getElementByName(newRow, 'selectedrow4temp').id = 'selectedrow4temp'+id;
		getElementByName(newRow, 'added4temp').id = 'added4temp'+id;
		getElementByName(newRow, 'added4temp').value = 'Y';
		YAHOO.util.Dom.insertBefore(newRow, templateRow);
	} else {
			newRow = getThisRow(currentRow4T, 'TR');
		}

	var accessTypeID = getElementByName(newRow, "temporary_access_type_id").value;
	var savedAccessType = getElementByName(newRow, "access_type_id_t").value;
	var selectedAccessType = document.getElementById('TaccessTypes').options[document.getElementById('TaccessTypes').selectedIndex].value;
	var changereasonvalue =document.getElementById('Taccesschange').options[document.getElementById('Taccesschange').selectedIndex].value;


	if (!empty(accessTypeID)) {
	   if (selectedAccessType != '' && savedAccessType != selectedAccessType && empty(changereasonvalue)) {
		  showMessage("js.dialysismodule.commonvalidations.editdialysisprescription.accesschangereason");

	   return false;
}
	}

	var tds = newRow.getElementsByTagName('td');

	var accessType = document.getElementById('TaccessTypes').options[document.getElementById('TaccessTypes').selectedIndex].text;
	var Tdate = document.getElementById('Tdate').value.trim();
	var Tdoctor = document.getElementById('Tdoctor').options[document.getElementById('Tdoctor').selectedIndex].text;
	var Tsite = document.getElementById('Tsite').options[document.getElementById('Tsite').selectedIndex].text;
	var Tvalue = document.getElementById('Tdoctor').options[document.getElementById('Tdoctor').selectedIndex].value;
	var TdateRemoval = document.getElementById('TdateRemoval').value;
	var Tarea = document.getElementById('Tarea').value.trim();
	var Thname= document.getElementById('Thname').value.trim();
	var Taccesschange = document.getElementById('Taccesschange').options[document.getElementById('Taccesschange').selectedIndex].text;
	var TaccesschangeValue = changereasonvalue;

    var THname=truncateText(Thname,12);
    var TArea=truncateText(Tarea,20);

	tds[0].textContent = accessType;
	tds[1].textContent = Tsite;
	tds[2].textContent = Tdate;
	tds[3].textContent = TdateRemoval;
	tds[4].textContent = Tvalue != '' ? Tdoctor : '';
	tds[5].textContent = THname;
	tds[6].textContent = TArea;
    tds[7].textContent= TaccesschangeValue !='' ? Taccesschange.substring(0,30) : '';
    if(Thname.length > 15){
       tds[5].setAttribute("title",Thname);
   }
	if (Tarea.length > 20) {
		tds[6].setAttribute("title", Tarea);
	}

	getElementByName(newRow, 'access_type_id_t').value = document.getElementById('TaccessTypes').
				options[document.getElementById('TaccessTypes').selectedIndex].value;
	getElementByName(newRow, 'date_of_intiation_t').value = Tdate;
	getElementByName(newRow, 'date_of_failure_t').value = TdateRemoval;
	getElementByName(newRow, 'access_site_t').value = document.getElementById('Tsite')
				.options[document.getElementById('Tsite').selectedIndex].value;
	getElementByName(newRow, 'doctor_name_t').value = document.getElementById('Tdoctor')
			.options[document.getElementById('Tdoctor').selectedIndex].value;


    getElementByName(newRow,'hospital_name_t').value= Thname;
    getElementByName(newRow, 'reason_t').value = Tarea;

    getElementByName(newRow, 'access_t_change_reason').value = TaccesschangeValue;
	newRowinserted4T = true;
	currentRow4T = null;
	removeClassName(newRow, 'editing');

	tempAccessDialog.cancel();



}

function onEditT(obj) {

	tempAccessDialog.cfg.setProperty('context', [obj, 'tr', 'br'], false);
	var trObj = getThisRow(obj, 'TR');
	var id = getRowChargeIndex(trObj);
	addClassName(trObj, 'editing');
	var tds = trObj.getElementsByTagName('td');
	var access_type_id = getIndexedValue('temporary_access_type_id', id);

	setSelectedIndexText(document.getElementById('TaccessTypes'), tds[0].textContent);
	setSelectedIndexText(document.getElementById('Tsite'), tds[1].textContent);
	document.getElementById('Tdate').value = tds[2].textContent;
	document.getElementById('TdateRemoval').value = tds[3].textContent;
	//setSelectedIndexText(document.getElementById('Tdoctor'), tds[4].textContent);
	document.getElementById('Tdoctor').value = getIndexedValue('doctor_name_t', id);
	document.getElementById('Thname').value = getElementByName(trObj,'hospital_name_t').value;
	newRowinserted4T = false;
	currentRow4T = obj;

	tempAccessDialog.show();
}

var trashPosition = 8;

function changeElsColorT(index, obj) {

		var row = document.getElementById("TaccessTbl").rows[index];
		var trObj = getThisRow(obj);
		var tab = getThisTable(obj);
		var parts = trObj.id.split('row');
		var index = parseInt(parts[1])+1;
		var trashObj = trObj.cells[trashPosition].getElementsByTagName("img")[0];
		var markRowForDelete = document.getElementById('selectedrow4temp'+index).value == 'false' ? 'true' : 'false';
		document.getElementById('selectedrow4temp'+index).value = document.getElementById('selectedrow4temp'+index).value == 'false' ? 'true' :'false';

		if (markRowForDelete == 'true') {
		trashObj.src = cpath+'/icons/undo_delete.gif';
		} else {
			trashObj.src = cpath+'/icons/delete.gif';
	   	}
}

function cancelP() {
	newRowinserted4P = true;
	currentRow4P = null;
}

function addToTableP() {

	if(document.getElementById('PaccessTypes').value == '' || document.getElementById('Psite').value == '') {
		showMessage("js.clinicaldata.scorecard.permaccesstype.accessfield.notempty");
		return false;
	}
	var currentRowIndex = -1;
	if (newRowinserted4P == false) {
		var rowObj = getThisRow(currentRow4P, 'TR');
		var parts = rowObj.id.split('row');
		currentRowIndex = parseInt(parts[1]);
	}

	var tableObj = document.getElementById('PaccessTbl');
	var rowsLength = tableObj.rows.length;
	var templateRow = tableObj.rows[rowsLength-2];
	var newRow = '';

	if (newRowinserted4P) {
		var id = rowsLength-2;
		newRow = templateRow.cloneNode(true);
		newRow.style.display = '';
		newRow.id = 'row'+(rowsLength-3);
		getElementByName(newRow, 'selectedrow4per').id = 'selectedrow4per'+id;
		getElementByName(newRow, 'added4per').id = 'added4per'+id;
		getElementByName(newRow, 'added4per').value = 'Y';
		YAHOO.util.Dom.insertBefore(newRow, templateRow);
	} else {
		newRow = getThisRow(currentRow4P, 'TR');
	}

	var PaccessTypeID = getElementByName(newRow, "permanent_access_type_id").value;
	var PsavedAccessType = getElementByName(newRow, "access_type_id_p").value;
	var PselectedAccessType = document.getElementById('PaccessTypes').options[document.getElementById('PaccessTypes').selectedIndex].value;
	var Pchangereasonvalue=document.getElementById("Paccesschange").value;

	if (!empty(PaccessTypeID)) {
		if (PselectedAccessType != '' && PsavedAccessType != PselectedAccessType && empty(Pchangereasonvalue)) {
			showMessage("js.dialysismodule.commonvalidations.editdialysisprescription.accesschangereason");
			return false;
		}
	}

     var tds = newRow.getElementsByTagName('td');

	var accessType = document.getElementById('PaccessTypes').options[document.getElementById('PaccessTypes').selectedIndex].text;
	var Pdate = document.getElementById('Pdate').value.trim();
	var Pdoctor = document.getElementById('Pdoctor').options[document.getElementById('Pdoctor').selectedIndex].text;
	var Pvalue = document.getElementById('Pdoctor').options[document.getElementById('Pdoctor').selectedIndex].value;
	var Psite = document.getElementById('Psite').options[document.getElementById('Psite').selectedIndex].text;
	var PdateRemoval = document.getElementById('PdateRemoval').value;
	var Phname= document.getElementById('Phname').value.trim();
	var Parea = document.getElementById('Parea').value.trim();
    var Paccesschange = document.getElementById('Paccesschange').options[document.getElementById('Paccesschange').selectedIndex].text;
	var PaccesschangeValue = document.getElementById('Paccesschange').
				options[document.getElementById('Paccesschange').selectedIndex].value;

    var PHname=truncateText(Phname,12);
    var PArea=truncateText(Parea,20);


	tds[0].textContent = accessType;
	tds[1].textContent = Psite;
	tds[2].textContent = Pdate;
	tds[3].textContent = PdateRemoval;
	tds[4].textContent = Pvalue != '' ? Pdoctor : '';
	tds[5].textContent = PHname;
	tds[6].textContent = PArea;
	tds[7].textContent= PaccesschangeValue !='' ? Paccesschange.substring(0,30) : '';
	if(Phname.length > 15){
	tds[5].setAttribute("title",Phname);
	}
	if (Parea.length > 20) {
		tds[6].setAttribute("title", Parea);
	}

	getElementByName(newRow, 'access_type_id_p').value = document.getElementById('PaccessTypes').
				options[document.getElementById('PaccessTypes').selectedIndex].value;
	getElementByName(newRow, 'date_of_intiation_p').value = Pdate;
	getElementByName(newRow, 'date_of_removal_p').value = PdateRemoval;
	getElementByName(newRow, 'access_site_p').value = document.getElementById('Psite')
				.options[document.getElementById('Psite').selectedIndex].value;
	getElementByName(newRow, 'doctor_name_p').value = document.getElementById('Pdoctor')
			.options[document.getElementById('Pdoctor').selectedIndex].value;
	getElementByName(newRow,'hospital_name_p').value= Phname;
	getElementByName(newRow, 'reason_p').value = Parea;

    getElementByName(newRow, 'access_p_change_reason').value = PaccesschangeValue;
	newRowinserted4P = true;
	currentRow4P = null;
	removeClassName(newRow, 'editing');

	permanentAccessDialog.cancel();

}

function onEditP(obj) {

	permanentAccessDialog.cfg.setProperty('context', [obj, 'tr', 'br'], false);
	var trObj = getThisRow(obj, 'TR');
	var id = getRowChargeIndex(trObj);
	addClassName(trObj, 'editing');
	var tds = trObj.getElementsByTagName('td');
    var access_type_id = getIndexedValue('permanent_access_type_id', id);
	setSelectedIndexText(document.getElementById('PaccessTypes'), tds[0].textContent);
	setSelectedIndexText(document.getElementById('Psite'), tds[1].textContent);
	document.getElementById('PdateRemoval').value = tds[3].textContent;
	document.getElementById('Pdate').value = tds[2].textContent;
	document.getElementById('Pdoctor').value = getIndexedValue('doctor_name_p', id);
	document.getElementById('Phname').value = getElementByName(trObj,'hospital_name_p').value;
	newRowinserted4P = false;
	currentRow4P = obj;
	permanentAccessDialog.show();
}
    var trashPosition = 8;
function changeElsColorP(index, obj) {

		var row = document.getElementById("PaccessTbl").rows[index];
		var trObj = getThisRow(obj);
		var tab = getThisTable(obj);
		var parts = trObj.id.split('row');
		var index = parseInt(parts[1])+1;
        var trashObj = trObj.cells[trashPosition].getElementsByTagName("img")[0];
		var markRowForDelete = document.getElementById('selectedrow4per'+index).value == 'false' ? 'true' : 'false';
		document.getElementById('selectedrow4per'+index).value = document.getElementById('selectedrow4per'+index).value == 'false' ? 'true' :'false';

		if (markRowForDelete == 'true') {
		   trashObj.src = cpath+'/icons/undo_delete.gif';
		} else {

	   	      trashObj.src = cpath+'/icons/delete.gif';

	   	  }
}

function getRowChargeIndex(row) {
	return row.rowIndex - getFirstItemRow();var Taccesschange = document.getElementById('Taccesschange').options[document.getElementById('Taccesschange').selectedIndex].text;
	var TaccesschangeValue = document.getElementById('Taccesschange').
				options[document.getElementById('Taccesschange').selectedIndex].value;
}

function getFirstItemRow() {
	// index of the first charge item: 0 is header, 1 is first charge item.
	return 1;
}

function getIndexedValue(name, index) {
	var obj = getIndexedFormElement(document.detailsForm, name, index);
	if (obj)
		return obj.value;
	else
	return null;
}

function isSelectedAllForDelete() {
	var deletedP = document.getElementsByName('selectedrow4per');
	var deletedT = document.getElementsByName('selectedrow4temp');
	var isDeletedAllP = true;
	var isDeletedAllT = true;

	for (var i=0; i<deletedP.length-1; i++) {
		if (deletedP[i].value == 'false') {
			isDeletedAllP = false;
			break;
		}
	}

	for (var i=0; i<deletedT.length-1; i++) {
		if (deletedT[i].value == 'false') {
			isDeletedAllT = false;
			break;
		}
	}

	return isDeletedAllP && isDeletedAllT;
}

function DisableHeparinType() {
    var heparintype = document.detailsForm.heparin_type.value;

    if(heparintype == 'h') {
	    document.getElementById("low_heparin_initial_dose").disabled=true;
		document.getElementById("low_heparin_intrim_dose").disabled=true;
		document.getElementById("heparin_bolus").readOnly=false;

	 } else {
	    document.getElementById("low_heparin_initial_dose").disabled=false;
		document.getElementById("low_heparin_intrim_dose").disabled=false;
		document.getElementById("heparin_bolus").readOnly=true;
     }
}

function totalheparindose() {
     var initialdose=document.getElementById("low_heparin_initial_dose").value || 0;
     var intrimdose=document.getElementById("low_heparin_intrim_dose").value || 0;
     var sum = parseFloat(initialdose) + parseFloat(intrimdose);
     document.getElementById('heparin_bolus').value = parseFloat(sum);
     return true;
}
