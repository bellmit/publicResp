function initDoctorVisits(){
	editDialog();
	doctorAutoComplete("visitingDoctor","doctorcontainer1", doctorlist,1);
}

function editDialog() {
	addDoctorDialog = new YAHOO.widget.Dialog("doctorVisitDiag",
			{
				width:"550px",
				context : ["dialogId", "tl", "bl"],
				visible:false,
				modal:true,
				constraintoviewport:true,

			} );
	YAHOO.util.Event.addListener("doctorVisitOkBtn", "click", handleSubmit, addDoctorDialog, true);
	YAHOO.util.Event.addListener("doctorVisitCancelBtn", "click", handleCancel, addDoctorDialog, true);
	subscribeKeyListeners(addDoctorDialog);
	addDoctorDialog.render();
}
function subscribeKeyListeners(dialog) {
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
			{ fn:handleCancel, scope:dialog, correctScope:true } );

	// Alt+Shift+K
	var okButtonListener = new YAHOO.util.KeyListener(document, { alt:true, shift: true, keys:75 },
			{ fn:handleSubmit, scope:dialog, correctScope:true } );
	addDoctorDialog.cfg.setProperty("keylisteners", [escKeyListener, okButtonListener]);
}

function funAddNewDoctorVisit(){
	if(!(checkOpenUnpaidCreditBills(document.getElementById("patientId").value))) {
		document.getElementById('isChargeable').disabled = true;
		document.getElementById('isChargeable').checked = false;
	}


	addDoctorDialog.cfg.setProperty("context",["dialogId", "tl", "bl"], false);
	addDoctorDialog.show();
}

function checkOpenUnpaidCreditBills(visitid){
	var ajaxobj = newXMLHttpRequest();
	var url = cpath+'/pages/ipservices/Ipservices.do?_method=checkOpenUnpaidCreditBills&visitId='+visitid;
	var reqObject = newXMLHttpRequest();
	reqObject.open("POST",url.toString(), false);
	reqObject.send(null);
	if (reqObject.readyState == 4) {
		if ( (reqObject.status == 200) && (reqObject.responseText!=null) ) {
			if (reqObject.responseText != 'CreditBillExists') {
					alert("Patient does not have open unpaid credit bill");
					return false;
			}
		}
	}
	return true;
}

function handleSubmit(){

	if(YAHOO.util.Dom.get('visitingDoctor').value == ""){
		alert("Please select Doctor");
		YAHOO.util.Dom.get('visitingDoctor').focus();
		return false;
	}
	if(document.doctorVisits.chargetype1.value == ""){
		alert("Please select Consultation");
		document.doctorVisits.chargetype1.focus();
		return false;
	}
	if (document.doctorVisits.visit_date.value == "") {
		alert("Please select visit date");
		document.doctorVisits.visit_date.focus();
		return false;
	}
	var dateValidity =  (allowBackDateRights == 'A' || roleId <= 2) ? '' : 'future';
	if (!doValidateDateField(document.doctorVisits.visit_date)) {
		return false;
	}
	if (!doValidateTimeField(document.doctorVisits.visit_time)) {
		return false;
	}
	var reg_date = getDateFromField(document.getElementById('reg_date'));
	var reg_time = document.getElementById('reg_time').value.split(":");
	reg_date.setHours(reg_time[0]);
	reg_date.setMinutes(reg_time[1]);

	var d = getDateFromField(document.doctorVisits.visit_date);
	var time = document.doctorVisits.visit_time.value.split(":");
	d.setHours(time[0]);
	d.setMinutes(time[1]);

	var errorStr = validateDateTime(d, dateValidity);
	if (errorStr != null) {
		alert(errorStr);
		return false;
	}
	if (d < reg_date) {
		alert("visit date should be greater than registration date: "+
			formatDate(reg_date, 'ddmmyyyy', '-') + " " +formatTime(reg_date, false));
		return false;
	}

	addDoctorDialog.hide();
	document.doctorVisits.action="Ipservices.do?_method=AddNewDoctorVisit";
	document.doctorVisits.submit();
}

function handleCancel() {
	this.cancel();
}
