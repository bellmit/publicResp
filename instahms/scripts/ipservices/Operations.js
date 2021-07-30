var noofServicesDeleted = 0;
//
function addRowToOtherServiceTable(){
		var otherservtable = document.getElementById("otherservicesTab");
		var len = otherservtable.rows.length;
		if(YAHOO.util.Dom.get("otherserviceGroup"+(len-1)).value != ""){
		var tdObj="", trObj="";

		trObj = otherservtable.insertRow(len);
		if(noOfOtherServDeleted > 0){
				len = len + noOfOtherServDeleted;
			}
		var osrow = "osrow"+len;
		trObj.id = osrow;
		var otherserviceGroup = "otherserviceGroup" + len;
		var otherserviceac = "otherserviceac" + len;
		var otherservice = "otherservice" + len;
		var otherservicecontainer = "otherservicecontainer" + len;
		var otherserviceqty = "otherserviceqty" + len;
		var otherservicecharge = "otherservicecharge" + len;
		var otherserviceremarks = "otherserviceremarks" + len;
		var otherserviceCheckBox = "otherserviceCheckBox" + len;
		var otherserviceDeleteCharge = "otherserviceDeleteCharge" +len;
		tdObj = trObj.insertCell(0);
		tdObj.setAttribute("class", "yui-skin-sam");
		tdObj.setAttribute("valign", "top");
		tdObj.innerHTML =' <select   style="width: 170" id="'+otherserviceGroup+'" '+
			+' name="'+otherserviceGroup+'" onchange="getServices(this.value,'+otherservice+','+otherservicecontainer+');"></select>';

        tdObj = trObj.insertCell(1);
		tdObj.setAttribute("class", "yui-skin-sam");
		tdObj.setAttribute("valign", "top");
		tdObj.innerHTML = '<div id="'+otherserviceac+'"  class="autocomplete" style="width: 300"><input  name="'+otherservice+'" height="10%" id="'+otherservice+'" style="width: 300" type="text"   /> <div id="'+otherservicecontainer+'" class="scrolForContainer"></div></div>'


		tdObj = trObj.insertCell(2);
		tdObj.setAttribute("class", "yui-skin-sam");
		tdObj.setAttribute("valign", "top");
		tdObj.innerHTML = '<input  id="'+ otherserviceqty +'" name="'+ otherserviceqty +'" type="text" style="width: 70;text-align: right;" onkeypress="return enterNumOnly(event);" value="1" onchange="setValue(this,this.value);"/>';

		tdObj = trObj.insertCell(3);
		tdObj.setAttribute("class", "yui-skin-sam");
		tdObj.setAttribute("valign", "top");
		tdObj.innerHTML = '<input id="'+ otherserviceremarks +'" name="'+ otherserviceremarks +'" type="text" style="width: 290"  maxlength="200" />';

		tdObj = trObj.insertCell(4);
		tdObj.innerHTML = '<img class="imgDelete" name="'+otherserviceCheckBox+'" id="'+otherserviceCheckBox+'"  src="' + cpath + '/icons/Delete.png" onclick="cancel(this,'+osrow+','+otherserviceDeleteCharge+')" />' +
							'<input type="hidden" name="'+otherserviceDeleteCharge+'" id="'+otherserviceDeleteCharge+'" value="false"/>';;
		getOtherChargeheads(len);
		}
	}
function addRowToEquipmentTable(){
		var equipmenttable = document.getElementById("equipmentTab");
		var len = equipmenttable.rows.length;
		if(YAHOO.util.Dom.get("equipment"+(len-1)).value != ""){
		var tdObj="", trObj="";
		trObj = equipmenttable.insertRow(len);
		if(noOfEquipmentsDeleted > 0){
				len = len + noOfEquipmentsDeleted;
			}
		var eqRow = "eqRow" + len;
		var eqdeptac = "eqdeptac" + len;
		var equipment = "equipment" + len;
		var equipmentac = "equipmentac" + len;
		var equipmentcontainer = "equipmentcontainer" + len;
		var duration = "duration" + len;
		var equipmentunits = "equipmentunits" + len;
		var equipmentremarks = "equipmentremarks" + len;
		var equipmentCheckBox = "equipmentCheckBox" + len;
		var equipmentDeleteCharge = "equipmentDeleteCharge" + len;
		trObj.setAttribute("id",eqRow);

        tdObj = trObj.insertCell(0);
		tdObj.setAttribute("class", "yui-skin-sam");
		tdObj.setAttribute("valign", "top");
		tdObj.innerHTML = '<div id="'+ equipmentac +'" class="autocomplete" style="width: 330" ><input id="'+ equipment +'" name="'+ equipment +'" type="text" style="width: 330"   /><div id="'+ equipmentcontainer +'" class="scrolForContainer"></div></div>';

		tdObj = trObj.insertCell(1);
		tdObj.setAttribute("class", "yui-skin-sam");
		tdObj.setAttribute("valign", "top");
		tdObj.innerHTML = '<input  id="'+ equipmentunits +'" name="'+ equipmentunits +'" type="checkbox"  style="width: 200;"   />';


		tdObj = trObj.insertCell(2);
		tdObj.setAttribute("class", "yui-skin-sam");
		tdObj.setAttribute("valign", "top");
		tdObj.innerHTML = '<input id="'+ duration +'" name="'+ duration +'"  style="width: 70;text-align: right;" type="text" onkeypress="return enterNumOnly(event);" value="1" onchange="setValue(this,this.value);" />';


		tdObj = trObj.insertCell(3);
		tdObj.setAttribute("class", "yui-skin-sam");
		tdObj.setAttribute("valign", "top");
		tdObj.innerHTML = '<input id="'+ equipmentremarks +'" name="'+ equipmentremarks +'" type="text"  maxlength="200" style="width: 380"/>';

		tdObj = trObj.insertCell(4);
		tdObj.innerHTML = '<img class="imgDelete" name="'+equipmentCheckBox+'" id="'+equipmentCheckBox+'"  src="' + cpath + '/icons/Delete.png" onclick="cancel(this,'+eqRow+','+equipmentDeleteCharge+')"/>' +
							 '<input type="hidden" name="'+equipmentDeleteCharge+'" id="'+equipmentDeleteCharge+'" value="false"/>';
		initEquipmentAutoComplete(equipment,equipmentcontainer,equipmentunits);
		}
	}


	function addRowToPescMedTable(){
		var medicinetable = document.getElementById("medcinetab");
		var len = medicinetable.rows.length;
		if(YAHOO.util.Dom.get("medicine"+(len-1)).value != ""){
		var tdObj="", trObj="";
		trObj = medicinetable.insertRow(len);
		if(noOfMedicinesDeleted > 0){
				len = len + noOfMedicinesDeleted;
			}
		var medRow = "medRow" + len;
		var medicine = "medicine" + len;
		var mednameautocomplete = "mednameautocomplete" + len;
		var medicine_dropdown = "medicine_dropdown" + len;
		var medicinedosage = "medicinedosage" + len;
		var autocompletedosage = "autocompletedosage" + len;
		var meddosagecontainer = "meddosagecontainer" + len;
		var noofdays = "noofdays" + len;
		var quantity = "quantity" + len;
		var medremarks = "medremarks" + len;
		var medCheckBox = "medCheckBox" + len;
		var medDeleteCharge = "medDeleteCharge" +len;
		tdObj = trObj.insertCell(0);
		tdObj.setAttribute("class", "yui-skin-sam");
		tdObj.setAttribute("valign", "top");
		tdObj.innerHTML = '<div id="'+mednameautocomplete+'" class="autocomplete" style="width: 330"><input  name="'+medicine+'" height="10%" style="width: 330" id="'+medicine+'" type="text"  > <div id="'+medicine_dropdown+'" class="scrolForContainer"></div></div>'

        tdObj = trObj.insertCell(1);
		tdObj.setAttribute("class", "yui-skin-sam");
		tdObj.setAttribute("valign", "top");
		tdObj.innerHTML = '<div id="'+autocompletedosage+'" class="autocomplete" ><input  name="'+medicinedosage+'" height="10%" id="'+medicinedosage+'" style="width: 100" type="text"   /> <div id="'+meddosagecontainer+'" class="scrolForContainer"></div></div>'
        YAHOO.example.dosageArray1 = [];
        var h = 0;
        for(var j = 0;j<dosage.length;j++){
			YAHOO.example.dosageArray1.length = h+1;
			YAHOO.example.dosageArray1[h] = dosage[j].DOSAGE;
			h++;
			}
		YAHOO.example.ACJSArray = new function() {
		this.oACDS1 = new YAHOO.widget.DS_JSArray(YAHOO.example.dosageArray1);
		this.oAutoComp1 = new YAHOO.widget.AutoComplete(medicinedosage,meddosagecontainer, this.oACDS1);
		this.oAutoComp1.prehighlightClassName = "yui-ac-prehighlight";
		this.oAutoComp1.typeAhead = true;
		this.oAutoComp1.useShadow = true;
		this.oAutoComp1.minQueryLength = 0;
		this.oAutoComp1.allowBrowserAutocomplete = false;
		this.oAutoComp1.forceSelection = true;
		this.oAutoComp1.textboxFocusEvent.subscribe(function(){
		var sInputValue = YAHOO.util.Dom.get(medicinedosage).value;
		if(sInputValue.length === 0) {
				var oSelf = this;
				setTimeout(function(){oSelf.sendQuery(sInputValue);},0);
				}
			});
		this.oAutoComp1.containerCollapseEvent.subscribe(function(){
				return calculateQuantity(YAHOO.util.Dom.get(noofdays),YAHOO.util.Dom.get(quantity),YAHOO.util.Dom.get(medicinedosage));
			});
		}

		tdObj = trObj.insertCell(2);
		tdObj.setAttribute("class", "yui-skin-sam");
		tdObj.setAttribute("valign", "top");
		tdObj.innerHTML = '<input id="'+ noofdays +'"  name="'+ noofdays +'" onblur="return calculateQuantity('+ noofdays +','+ quantity +','+medicinedosage+');" type="text" style="width: 60;text-align: right;" onkeypress="return enterNumOnly(event);" value="1" onchange="setValue(this,this.value);"/>';

		tdObj = trObj.insertCell(3);
		tdObj.setAttribute("class", "yui-skin-sam");
		tdObj.setAttribute("valign", "top");
		tdObj.innerHTML = '<input  id="'+ quantity +'" value="1" onchange="setValue(this,this.value);" name="'+ quantity +'" type="text" style="width: 60;text-align: right;" onkeypress="return enterNumOnly(event);" />';

		tdObj = trObj.insertCell(4);
		tdObj.setAttribute("class", "yui-skin-sam");
		tdObj.setAttribute("valign", "top");
		tdObj.innerHTML = '<input id="'+ medremarks +'" name="'+ medremarks +'" type="text" maxlength="200"   style="width : 280"/> ';

		tdObj = trObj.insertCell(5);
		tdObj.innerHTML = '<img class="imgDelete" name="'+medCheckBox+'" id="'+medCheckBox+'"  src="' + cpath + '/icons/Delete.png" onclick="cancel(this,"'+medRow+','+medDeleteCharge+'")"/>' +
							 '<input type="hidden" name="'+medDeleteCharge+'" id="'+medDeleteCharge+'" value="false"/>';
		initMedicineAutoComplete(medicine,medicine_dropdown);
		}
	}
function initMedicineAutoComplete(medicine,medicine_dropdown) {
	var dataSource ;
	YAHOO.example.medNamesArray = [];
		YAHOO.util.Dom.get(medicine).value = "";
		var n=0;
		for(var j = 0;j<jMedicineNames.length;j++){
			YAHOO.example.medNamesArray.length = n+1;
			YAHOO.example.medNamesArray[n] =jMedicineNames[j].CHARGE_NAME;
			n++;
		}
	this.oMedNameSCDS = new YAHOO.widget.DS_JSArray(YAHOO.example.medNamesArray);
	this.oAutoComp = new YAHOO.widget.AutoComplete(medicine, medicine_dropdown, this.oMedNameSCDS);

	this.oAutoComp.maxResultsDisplayed = 20;
	this.oAutoComp.allowBrowserAutocomplete = false;
	this.oAutoComp.prehighlightClassName = "yui-ac-prehighlight";
	this.oAutoComp.typeAhead = false;
	this.oAutoComp.useShadow = false;
	this.oAutoComp.minQueryLength = 0;
	this.oAutoComp.forceSelection = true;
	this.oAutoComp.animVert = false;
	this.oAutoComp.textboxFocusEvent.subscribe(function(){
							var sInputValue = YAHOO.util.Dom.get(medicine).value;
							if(sInputValue.length === 0) {
     						var oSelf = this;
     						setTimeout(function(){oSelf.sendQuery(sInputValue);},0);
   						}
					});
	this.oAutoComp.textboxBlurEvent.subscribe(function(){
			if(YAHOO.util.Dom.get(medicine).value == ''){
				dataentered = false;
			}
		});

}
var equipmentMapArray ;
function initEquipmentAutoComplete(equpname,equp_container,serviceunits) {
	equipmentMapArray = new Array(equipments.length);
	YAHOO.example.EquipmentNamesArray = [];

	YAHOO.example.EquipmentNamesArray.length = 0;
	var deptName = YAHOO.util.Dom.get(equpname).value;
	YAHOO.util.Dom.get(equpname).value = "";
	var k=0;
	for(var l = 0;l<equipments.length;l++){
		YAHOO.example.EquipmentNamesArray.length = k + 1;
		YAHOO.example.EquipmentNamesArray[k] = equipments[l].EQUIPMENT_NAME+'('+equipments[l].DEPT_NAME+')';
		equipmentMapArray[equipments[l].EQUIPMENT_NAME+'('+equipments[l].DEPT_NAME+')'] =  equipments[l].EQ_ID;
		k++;
	}
	this.oEqpSCDS = new YAHOO.widget.DS_JSArray(YAHOO.example.EquipmentNamesArray);
	this.oEqpAutoComp = new YAHOO.widget.AutoComplete(equpname,equp_container, this.oEqpSCDS);
	this.oEqpAutoComp.maxResultsDisplayed = 20;
	this.oEqpAutoComp.allowBrowserAutocomplete = false;
	this.oEqpAutoComp.prehighlightClassName = "yui-ac-prehighlight";
	this.oEqpAutoComp.typeAhead = false;
	this.oEqpAutoComp.useShadow = false;
	this.oEqpAutoComp.minQueryLength = 0;
	this.oEqpAutoComp.forceSelection = true;
	this.oEqpAutoComp.animVert = false;
	this.oEqpAutoComp.itemSelectEvent.subscribe(function (oSelf){
		dataentered = true;
		var unit = "";
		var equipmentname = YAHOO.util.Dom.get(equpname).value;
		var a = 0;
		for(var u = 0;u<equipments.length;u++){
			if(equipmentname.substring(0,equipmentname.indexOf("(")) == equipments[u].EQUIPMENT_NAME){
				if(equipments[u].EQIPMENT_TYPE=='D'){
					YAHOO.util.Dom.get(serviceunits).value = 'Daily' ;
				}else{
					document.getElementById(serviceunits).value = 'Hourly' ;
				}
			}
		}
	});
	this.oEqpAutoComp.textboxBlurEvent.subscribe(function(){
			if(YAHOO.util.Dom.get(equpname).value == ''){
				dataentered = false;
			}
		});
}

var otDoctorsArray = [];
otDoctorsArray.length = 0;
var sysdate = getServerDate().getTime();
var currTime = new Date().getTime();
var dateDiff = sysdate - currTime;
var tdVariable = "";
function init(){
	otanestetistautocomplete(otanesthetist1,otanesthetistcontainer1);
	initEquipmentAutoComplete(equipment1,equipmentcontainer1,equipmentunits1);
	initServiceAutoComplete(1);

	if(pharmacy == ''){
		medicineDoasageAutoComplete();
		initMedicineAutoComplete(medicine1,medicine_dropdown1);
	}
	opDeptAutoComplete(operations1,operationscontainer1);
	opeTheatersAutoComplete(theaters1,theaterscontainer1);
	getOtherChargeheads(1);
}

function initSchedule(){
	opeTheatersAutoComplete(theatre,theaterscontainer);
	scheduleotDoctorsAutoComplete(primarysurgeon,otsurgeoncontainer,"primarysurgeon");
	otanestetistautocomplete(primaryanae,otanesthetistcontainer);
	anesthesiatypeautocomplete(anesthesia_type,anesthesiatypecontainer);
	setopedatetime();
}
function setopedatetime(){
		document.forms[0].hdate.value = getCurrentDate();
		document.forms[0].htime.value = getCurrentTime();
}
function opDeptAutoComplete(operations,operationscontainer) {
		YAHOO.example.opeNamesArray = [];
		YAHOO.util.Dom.get(operations).value = "";
		var n=0;
		var n=0;
			for(var j = 0;j<operationslist.length;j++){
			YAHOO.example.opeNamesArray.length = n+1;
			YAHOO.example.opeNamesArray[n] =operationslist[j].OPERATION;
			n++;
			}
		this.oOpeNameSCDS = new YAHOO.widget.DS_JSArray(YAHOO.example.opeNamesArray);
		this.oOpeNameAutoComp = new YAHOO.widget.AutoComplete(operations,operationscontainer, this.oOpeNameSCDS);
		this.oOpeNameAutoComp.prehightlightClassName = "yui-ac-prehighlight";
		this.oOpeNameAutoComp.typeAhead = true;
		this.oOpeNameAutoComp.useShadow = true;
		this.oOpeNameAutoComp.allowBrowserAutocomplete = false;
		this.oOpeNameAutoComp.minQueryLength = 0;
		this.oOpeNameAutoComp.forceSelection = true;
		this.oOpeNameAutoComp.textboxFocusEvent.subscribe(function(){
							var sInputValue = YAHOO.util.Dom.get(operations).value;
							if(sInputValue.length === 0) {
     						var oSelf = this;
     						setTimeout(function(){oSelf.sendQuery(sInputValue);},0);
   						}
					});
}
function opetime(starttime,endtime){
		startTimer(document.getElementById(starttime));
		startTimer(document.getElementById(endtime));
}


	function startTimer(timerTimeObj){
		tdVariable = timerTimeObj;
		showTimer();
	}

	function showTimer() {
		var myDt = new Date().getTime();
		myDt += dateDiff;
		var myDate = new Date(myDt);
		var hour = myDate.getHours();
	    var min = myDate.getMinutes();
	    var sec = myDate.getSeconds();

		if (min <= 9) {
	      min = "0" + min;
	    }

	    tdVariable.value = hour + ":" +min;

	}


var status =0;
var opstatus = 0;
function prescribeOperation(){
var opetablerows = document.getElementById("opprescriptionTable").rows.length;

  for(var c=0;c<opetablerows;c++){
				var opdepts = "opdepts" + c;
				var operations = "operations" + c;

				if( YAHOO.util.Dom.get(opdepts) == null){

				}else{
					var opdeptsname = YAHOO.util.Dom.get(opdepts).value;
					var ooperationsname = YAHOO.util.Dom.get(operations).value;
					if(opdeptsname == ""){
					if(ooperationsname == ""){

							}
						prescribedTests = true;
					}
				}


			}

var into = 1;
for(var j=1; j<opetablerows; j++){
	var opeCheckBoxc = "opeCheckBox" + j;


	if(document.getElementById(opeCheckBoxc)!=null){

		var opdepts = "opdepts" + j;
		var operations = "operations" + j;
		var theaters = "theaters" + j;
		var starttime = "starttime" + j;
		var endtime = "endtime" + j;
		var startdate = "startdate" + j;
		var enddate = "enddate" + j;
		var opeCheckBox = "opeCheckBox" + j;
		var operationremarks = "operationremarks" + j;
		var otcharge = "otcharge" + j;
		var otsurgeon = "otsurgeon" + j;
		var otanesthetist = "otanesthetist" + j;

		if(YAHOO.util.Dom.get(operations)== null ){
		}else{
		if(!(document.getElementById(opeCheckBoxc).checked )){
			var opdeptsName = "";
			var operationsName = YAHOO.util.Dom.get(operations).value;
			for(var o = 0;o<operationslist.length;o++){
				if(operationsName == operationslist[o].OPERATION){
					opdeptsName = operationslist[o].DEPT_NAME;
					operationsName = operationslist[o].OPERATION_NAME;
				}
			}
			var theatersvalue =YAHOO.util.Dom.get(theaters).value;
			var hstarttime = YAHOO.util.Dom.get(starttime).value;
			var hendtime = YAHOO.util.Dom.get(endtime).value;
			var hoperationremarks = YAHOO.util.Dom.get(operationremarks).value;
			var operationstartdate = YAHOO.util.Dom.get(startdate).value;
			var operationenddate = YAHOO.util.Dom.get(enddate).value;

			var surgeonid = "";
			var anesthetistid = "";

			if(document.getElementById(otcharge).checked){
			otcharge = 'hrly';
			}else otcharge = '';
			var opdeptid = "",operationid="",theaterid = "";
			for(var t = 0;t<operationdept.length;t++){
			if(opdeptsName==operationdept[t].DEPT_NAME){
			   opdeptid = operationdept[t].DEPT_ID;
			}
			}
			for(var k =0;k<otlist.length;k++){
			if(YAHOO.util.Dom.get(otsurgeon).value == otlist[k].DOCTOR_NAME){

				surgeonid = otlist[k].DOCTOR_ID;
				}
			}
			for(var k =0;k<otlist.length;k++){
			if(YAHOO.util.Dom.get(otanesthetist).value == otlist[k].DOCTOR_NAME){

				anesthetistid = otlist[k].DOCTOR_ID;
				}
			}
			for(var y=0;y<operationslist.length;y++){
			 if(opdeptsName==operationslist[y].DEPT_NAME ){
			 if( operationsName==operationslist[y].OPERATION_NAME){

			   operationid = operationslist[y].OP_ID;
			 }
			 }
			}
			for(var u = 0;u<theaterslist.length;u++){
			if(theatersvalue==theaterslist[u].THEATRE_NAME){
			   theaterid = theaterslist[u].THEATRE_ID;
			}
			}
			if(operationid == ""){
				alert("Please select Any Operation");
				document.getElementById(operations).focus();
				return false;
			}
			if(theaterid == ""){
				alert("Please select Any Operation Theatre");
				document.getElementById(theaters).focus();
				return false;
			}
			if(YAHOO.util.Dom.get(otsurgeon).value == ""){
				alert("please select a surgeon");
				document.getElementById(otsurgeon).focus();
				return false;
			}
			if(operationstartdate == ""){
				alert("Please select Operation Start Date");
				document.getElementById(startdate).focus();
				return false;
			}
			if(operationenddate == ""){
				alert("Please select Operation End Date");
				document.getElementById(enddate).focus();
				return false;
			}
			if(operationstartdate == operationenddate){
				var startHours = hstarttime.split(':')[0];
				var startMinutes = hstarttime.split(':')[1];
				var endHours = hendtime.split(':')[0];
				var endMinutes = hendtime.split(':')[1];
				var sthrinsec = startHours*60*60 + startMinutes*60;
				var endhrinsec = endHours*60*60 + endMinutes*60;
				if(startHours == endHours){
				if(startMinutes > endMinutes){
				alert("End time must be greater that start time");
				return false;
				}
				}else if(sthrinsec > endhrinsec){
					alert("End time must be greater that start time");
					return false;
				}
			}
			if(operationenddate.split('-')[1] == operationstartdate.split('-')[1] && operationenddate.split('-')[2] == operationstartdate.split('-')[2]){
			if(operationenddate.split('-')[0] < operationstartdate.split('-')[0]  ){

				alert("operation End date must be greater than operation start date");
			return false;
			}
			}
			if(operationenddate.split('-')[2] == operationstartdate.split('-')[2]){
			if(operationenddate.split('-')[1] < operationstartdate.split('-')[1]){
				alert("operation End date must be greater than operation start date");
				return false;
			}
			}
			if(operationenddate.split('-')[0] == operationstartdate.split('-')[0] && operationenddate.split('-')[1] == operationstartdate.split('-')[1] && operationenddate.split('-')[2] == operationstartdate.split('-')[2]){

			var starthr = hstarttime.split(':')[0];
			var startmin = hstarttime.split(':')[1];
			var endhr = hendtime.split(':')[0];
			var endmin = hendtime.split(':')[1];
			var fromtimesecs = starthr*60*60 + startmin * 60 ;
		    var totimesecs = endhr*60*60 + endmin * 60 ;
				if(fromtimesecs >= totimesecs){
					alert("operation End time must be greater than operation start time");
					return false;
				}
			}

			if(hstarttime == ""){
				alert("Please Enter Start Time in HH:MI Format");
				document.getElementById(starttime).focus();
				return false;
			}else{
				if (!validateTime(YAHOO.util.Dom.get(starttime), j)){
						return false;
					}
			}

			if(hendtime == ""){
				alert("Please Enter End Time in HH:MI Format");
				document.getElementById(endtime).focus();
				return false;
			}else{
				if (!validateTime(YAHOO.util.Dom.get(endtime), j)){
						return false;
					}
			}

			if((opdeptsName != "") && (operationsName != "") && (!(document.getElementById(opeCheckBox).checked)) ){
				innerOperationsHTML(opdeptid,operationid,theaterid,hstarttime,hendtime,hoperationremarks,operationstartdate,operationenddate,operationsName,theatersvalue,otcharge,YAHOO.util.Dom.get(otsurgeon).value,YAHOO.util.Dom.get(otanesthetist).value,surgeonid,anesthetistid,into);
				}
			}
		}
	}
  }

  if(document.forms[0].bill.value == ''){
				alert("Please Select Bill No");
				return false;
			}

  document.forms[0].action="../../pages/ipservices/Otservices.do?method=prescribeOperation&bed="+bed;
 document.forms[0].submit();

}
function innerOtDoctorHTML(opdoctortypename,doctorid,otdoctorname,index){
		status =1;
		var innerOTTabObj = document.getElementById("innerOTDocTab");
		var trObj = "", tdObj = "";
		trObj = innerOTTabObj.insertRow(index-1);
		tdObj = trObj.insertCell(0);
		tdObj.innerHTML = '<input type="hidden" name="otdoctorname" id="otdoctorname" value="'+ doctorid +'">';

		tdObj = trObj.insertCell(1);
		tdObj.innerHTML = '<input type="hidden" name="otdoctortype" id="otdoctortype" value="'+ opdoctortypename +'">';

		tdObj = trObj.insertCell(2);
		tdObj.innerHTML = '<input type="hidden" name="otdoctor" id="otdoctor" value="'+ otdoctorname +'">';
}

function innerOperationsHTML(opdeptid,operationid,theaterid,hstarttime,hendtime,hoperationremarks,operationstartdate,operationenddate,operationsName,theatersvalue,otcharge,surgeon,anesthetist,surgeonid,anesthetistid,index){
				opstatus =1;
				var innerOpeTabObj = document.getElementById("innerOpeTab");
				var trObj = "", tdObj = "";
				trObj = innerOpeTabObj.insertRow(index-1);
				tdObj = trObj.insertCell(0);

				tdObj.innerHTML = '<input type="hidden" name="opdeptId" id="opdeptId" value="'+ opdeptid +'">';

				tdObj = trObj.insertCell(1);
				tdObj.innerHTML = '<input type="hidden" name="operationId" id="operationId" value="'+ operationid +'">';

				tdObj = trObj.insertCell(2);
				tdObj.innerHTML = '<input type="hidden" name="theaterId" id="theaterId" value="'+ theaterid+ '">';

				tdObj = trObj.insertCell(3);
				tdObj.innerHTML = '<input type="hidden" name="hstarttime" id="hstarttime" value="'+ hstarttime+ '">';

				tdObj = trObj.insertCell(4);
				tdObj.innerHTML = '<input type="hidden" name="hendtime" id="hendtime" value="'+ hendtime+ '">';
				tdObj = trObj.insertCell(5);
				tdObj.innerHTML = '<input type="hidden" name="hoperationremarks" id="hoperationremarks" value="'+ hoperationremarks+ '">';

				tdObj = trObj.insertCell(6);
				tdObj.innerHTML = '<input type="hidden" name="startdate" id="startdate" value="'+ operationstartdate+ '">';

				tdObj = trObj.insertCell(7);
				tdObj.innerHTML = '<input type="hidden" name="enddate" id="enddate" value="'+ operationenddate+ '">';

				tdObj = trObj.insertCell(8);
				tdObj.innerHTML = '<input type="hidden" name="operationname" id="operationname" value="'+ operationsName+ '">';

				tdObj = trObj.insertCell(8);
				tdObj.innerHTML = '<input type="hidden" name="theatername" id="theatername" value="'+ theatersvalue+ '">';

				tdObj = trObj.insertCell(9);
				tdObj.innerHTML = '<input type="hidden" name="otcharge" id="otcharge" value="'+ otcharge+ '">';

				tdObj = trObj.insertCell(10);
				tdObj.innerHTML = '<input type="hidden" name="surgeon" id="surgeon" value="'+ surgeon+ '">';

				tdObj = trObj.insertCell(11);
				tdObj.innerHTML = '<input type="hidden" name="anesthetist" id="anesthetist" value="'+ anesthetist + '">';

				tdObj = trObj.insertCell(11);
				tdObj.innerHTML = '<input type="hidden" name="surgeonid" id="surgeonid" value="'+ surgeonid+ '">';

				tdObj = trObj.insertCell(12);
				tdObj.innerHTML = '<input type="hidden" name="anesthetistid" id="anesthetistid" value="'+ anesthetistid + '">';

		}



var noOfOtDoctorsDeleted = 0;
function addRowOtDoctorTable(){
	var otdoctortable = document.getElementById("otdoctortable");
	var len = otdoctortable.rows.length;
	if(YAHOO.util.Dom.get("opdoctortype"+(len-1)).value != "" && YAHOO.util.Dom.get("otdoctor"+(len-1)).value != ""){
	var tdObj="", trObj="";
	trObj = otdoctortable.insertRow(len);
	trObj.setAttribute("height","30");
	if(noOfOtDoctorsDeleted > 0){
			len = len + noOfOtDoctorsDeleted;
		}
	var opdoctortype = "opdoctortype" + len;
	var opdoctortypeac = "opdoctortypeac" + len;
	var opdoctortypecontainer = "opdoctortypecontainer" + len;
	var otdoctorac = "otdoctorac" + len;
	var otdoctor = "otdoctor" + len;
	var otdoctorcontainer = "otdoctorcontainer" + len;
	var otdoctorcheck = "otdoctorcheck" +len;
	var docRow = "docRow" + len;
	var otdoctoDeleteCharge = "otdoctoDeleteCharge" +len;

	trObj.setAttribute("id",docRow);

	tdObj = trObj.insertCell(0);
	tdObj.setAttribute("class", "yui-skin-sam");
	tdObj.setAttribute("valign", "top");
	tdObj.innerHTML = '<select id="'+opdoctortype+'" name="'+opdoctortype+'" style="width: 330" onchange="otDoctorsAutoComplete('+len+')"> <option value="">...Select....</option> <option value="SUOPE">Surgeon</option> <option value="ASUOPE">Assistant Surgeon</option> <option value="COSOPE">Co Op Surgeon</option> <option value="ANAOPE">Anaesthetist</option> <option value="AANOPE">Assistant Anaesthetist</option></select>';

	tdObj = trObj.insertCell(1);
	tdObj.setAttribute("class", "yui-skin-sam");
	tdObj.setAttribute("valign", "top");
	tdObj.innerHTML = '<div id="'+ otdoctorac +'" class="autocomplete" style="width: 510" ><input id="'+ otdoctor +'" name="'+ otdoctor +'" type="text"  style="width: 510"  /><div id="'+ otdoctorcontainer +'" class="scrolForContainer"></div></div>';


	tdObj = trObj.insertCell(2);
	tdObj.innerHTML = '<img class="imgDelete" name="'+otdoctorcheck+'" id="'+otdoctorcheck+'"  src="' + cpath + '/icons/Delete.png" onclick="cancel(this,'+docRow+','+otdoctoDeleteCharge+')"/>' +
						 '<input type="hidden" name="'+otdoctoDeleteCharge+'" id="'+otdoctoDeleteCharge+'" value="false"/>';;
	}

}

function deleteRowOtDoctorTable(){
	var otdoctorcheckboxes = document.forms[0].otdoctorcheck;
	var otdoctortableObj = document.getElementById("otdoctortable");
	var flag = false;
	for(var checkedIndex=0;checkedIndex<otdoctorcheckboxes.length;checkedIndex++){
		if(otdoctorcheckboxes[checkedIndex].checked){
			otdoctortableObj.deleteRow(checkedIndex+1);
			flag = true;
			checkedIndex = checkedIndex -1 ;
			noOfOtDoctorsDeleted++;
		}
	}
	if(flag == false){
		alert("please check the rows which you want to delete");
		return false;
	}
}

function deleteRowFromOpeTable(){
	var opeCheckBoxs = document.forms[0].opeCheckBox;
	var opetableObj = document.getElementById("opprescriptionTable");
	var flag = false;
	for(var checkedIndex=0;checkedIndex<opeCheckBoxs.length;checkedIndex++){
		if(opeCheckBoxs[checkedIndex].checked){

			opetableObj.deleteRow(checkedIndex+1);
			flag = true;
			checkedIndex = checkedIndex -1 ;
			noOfOpesDeleted++;
		}
	}
	if(flag == false){
		alert("please check the rows which you want to delete");
		return false;
	}
}
var noOfEquipmentsDeleted = 0;
	var noOfMedicinesDeleted = 0;
	var noOfOtherServDeleted = 0;
	var noofVisitsDeleted = 0;
	var noofotherservsDeleted = 0;
	var noOfMediDeleted = 0;
	var noOfOperationsDeleted = 0;
function saveOpeDetails(){

 var equipmenttablerows = document.getElementById("equipmentTab").rows.length;
  if(noOfEquipmentsDeleted > 0){
     equipmenttablerows = equipmenttablerows + noofEquipmentsDeleted;
  }
  if(pharmacy == ''){
	  var medicinetablerows = document.getElementById("medcinetab").rows.length;
	  if(noOfMediDeleted > 0){
	     medicinetablerows = medicinetablerows + noOfMediDeleted;
	  }
  }
  var otherservtablerows = document.getElementById("otherservicesTab").rows.length;
  if(noOfOtherServDeleted > 0){
     otherservtablerows = otherservtablerows + noOfOtherServDeleted;
  }
 var servicetablerows = document.getElementById("prescribeServiceTab").rows.length;
  if(noofServicesDeleted > 0){
     servicetablerows = servicetablerows + noofServicesDeleted;
  }
var otdoctorstablerows = document.getElementById("otdoctortable").rows.length;
  if(noOfOtDoctorsDeleted > 0){
     otdoctorstablerows = otdoctorstablerows + noOfOtDoctorsDeleted;
  }
  for(var c=0;c<otherservtablerows;c++){
				var otherserviceGroup = "otherserviceGroup" + c;
				var otherservice = "otherservice" + c;
				var otherserviceqty = "otherserviceqty" + c;

				if( YAHOO.util.Dom.get(otherserviceGroup) == null){

				}else{
					if(YAHOO.util.Dom.get(otherserviceqty).value != ""){
						if (!isInteger(YAHOO.util.Dom.get(otherserviceqty).value)){
							YAHOO.util.Dom.get(otherserviceqty).value=1;
						}
					}
					var otherserviceGroupname = YAHOO.util.Dom.get(otherserviceGroup).value;
					var otherservicename = YAHOO.util.Dom.get(otherservice).value;
					if(otherserviceGroupname != ""){
						if(otherservicename == ""){	}
						prescribedTests = true;
					}else{
						if(otherservicename != ""){
							alert("please select any other service");
							return false;
						}
					}
				}
			}
for(var c=0;c<otdoctorstablerows;c++){
				var otdoctor = "otdoctor" + c;
				var opdoctortype = "opdoctortype" + c;
				var otdoctorcheck = "otdoctorcheck" + c;

				if( YAHOO.util.Dom.get(opdoctortype) == null){

				}else{
					var opdoctortypename = YAHOO.util.Dom.get(opdoctortype).value;
					var otdoctorname = YAHOO.util.Dom.get(otdoctor).value;

					if(opdoctortypename != ""){
					if(otdoctorname == ""){
					if(!document.getElementById(otdoctorcheck).checked){
							alert("please select OT Doctor");
							YAHOO.util.Dom.get(opdoctortypename).focus();
							return false;
							}
							}
						prescribedTests = true;
					}
				}


			}

for(var c=0;c<medicinetablerows;c++){
	if(pharmacy == ''){
				var medicine = "medicine" + c;
				var medicinedosage = "medicinedosage" + c;
				var quantity = "quantity" + c;
				var noofdays = "noofdays" + c;


				if( YAHOO.util.Dom.get(medicine) == null){

				}else{
					if(YAHOO.util.Dom.get(noofdays).value != ""){
						if(!isInteger(YAHOO.util.Dom.get(noofdays).value)){
							YAHOO.util.Dom.get(noofdays).value = 1;
						}
					}
					if(YAHOO.util.Dom.get(quantity).value != ""){
						if(!isInteger(YAHOO.util.Dom.get(quantity).value)){
							YAHOO.util.Dom.get(quantity).value = 1;
						}
					}
					var medicinename = YAHOO.util.Dom.get(medicine).value;
					var medicinedosagevalue = YAHOO.util.Dom.get(medicinedosage).value;
					if(medicinename != ""){
					if(medicinedosagevalue == ""){
							alert("please select the Medicine Dosage");
							YAHOO.util.Dom.get(medicine).focus();
							return false;
							}
						prescribedTests = true;
					}
				}

		}
	}
	for(var c=0;c<equipmenttablerows;c++){
			var equipment = "equipment" + c;
			var duration = "duration" + c;

			if( YAHOO.util.Dom.get(equipment) == null){
			}else{
				if( YAHOO.util.Dom.get(duration).value != ""){
					if(!isInteger(YAHOO.util.Dom.get(duration).value)){
							YAHOO.util.Dom.get(duration).value = 1;
						}
				}
				var equipmentname = YAHOO.util.Dom.get(equipment).value;
					if(equipment == ""){
						alert("please select the TestName");
						YAHOO.util.Dom.get(equipment).focus();
						return false;
					}
					prescribedTests = true;
			}


		}
for(var c=0;c<servicetablerows;c++){

			var servicename = "servicename" + c;
			var noOfTimes = "noOfTimes" + c;

			if( YAHOO.util.Dom.get(servicename) == null){
			}else{
				if(YAHOO.util.Dom.get(noOfTimes).value != ""){
					if(!isInteger(YAHOO.util.Dom.get(noOfTimes).value)){
							YAHOO.util.Dom.get(noOfTimes).value = 1;
						}
					}
			}


		}


var into = 0;
	for(var j=0; j<otherservtablerows; j++){
		var otherserviceGroup = "otherserviceGroup" + j;
		var otherservice = "otherservice" + j;
		var otherserviceqty = "otherserviceqty" + j;
		var otherserviceremarks = "otherserviceremarks" + j;
		var otherserviceCheckBox = "otherserviceCheckBox" +j;
		var otherserviceDeleteCharge = "otherserviceDeleteCharge" + j;

		if(YAHOO.util.Dom.get(otherserviceGroup) == null){
		}else{

		var otherserviceGroupName = YAHOO.util.Dom.get(otherserviceGroup).value;
		var otherserviceName = YAHOO.util.Dom.get(otherservice).value;
		var otherserviceqtyvalue =YAHOO.util.Dom.get(otherserviceqty).value;
		var qty = YAHOO.util.Dom.get(otherservice).value;
		var remarks = YAHOO.util.Dom.get(otherserviceremarks).value;
		if((otherserviceGroupName != "") && (otherserviceName != "") && (document.getElementById(otherserviceDeleteCharge).value == 'false')){
			innerOtherServiceHTML(otherserviceGroupName,otherserviceName,otherserviceqtyvalue,YAHOO.util.Dom.get(otherserviceremarks).value,into);
			into++;
			}
			}
	}
			var intot = 0;
	for(var j=0; j<otdoctorstablerows; j++){
		var otdoctor = "otdoctor" + j;
		var opdoctortype = "opdoctortype" + j;
		var otdoctorcheck = "otdoctorcheck" + j;
		var otdoctoDeleteCharge = "otdoctoDeleteCharge"+ j;
		if( YAHOO.util.Dom.get(opdoctortype) == null){

				}else{
					var opdoctortypename = document.getElementById(opdoctortype).value;
					var otdoctorname = YAHOO.util.Dom.get(otdoctor).value;
					var otdoctorid ="";
					for(var i =0;i<otlist.length;i++){
					  if(otdoctorname==otlist[i].DOCTOR_NAME){
					  otdoctorid = otlist[i].DOCTOR_ID;
					  }
					}
		if((opdoctortypename != "") && (otdoctorname != "") && (document.getElementById(otdoctoDeleteCharge).value == 'false')){
			innerOtDoctorHTML(opdoctortypename,otdoctorid,otdoctorname,intot);
			intot++;
			}
			}
			}
var intm = 0;
	for(var j=0; j<medicinetablerows; j++){
	if(pharmacy == ''){
		var medicine = "medicine" + j;
		var medicinedosage = "medicinedosage" + j;
		var noofdays = "noofdays" + j;
		var quantity = "quantity" + j;
		var medremarks = "medremarks" + j;
		var medCheckBox = "medCheckBox" + j;
		var medDeleteCharge = "medDeleteCharge" + j;
		if(YAHOO.util.Dom.get(medicine) == null){
		}else{
		if((YAHOO.util.Dom.get(medicine).value != "") && (YAHOO.util.Dom.get(medicinedosage).value != "") && (YAHOO.util.Dom.get(noofdays).value != "")&& (document.getElementById(medDeleteCharge).value == 'false')){
					innertMedHTML(YAHOO.util.Dom.get(medicine).value, YAHOO.util.Dom.get(medicinedosage).value, YAHOO.util.Dom.get(noofdays).value,YAHOO.util.Dom.get(quantity).value,YAHOO.util.Dom.get(medremarks).value,intm);
					intm++;
				}
			}
		}
	}
var ints = 0;
	for(var a=0; a<servicetablerows; a++){
		var servicename = "servicename" + a;
		var  noOfTimes= "noOfTimes" + a;
		var serviceremarks = "serviceremarks" + a;
		var serviceCheckBox = "serviceCheckBox" + a;
		var serviceDeleteCharge = "serviceDeleteCharge" + a;
		var servicedeptName = "";
		if(YAHOO.util.Dom.get(servicename) == null){
		}else{
			if(serNameAndCharges != null){
				var dDeptId = "";
				var servicetId = "";
				var servicename = YAHOO.util.Dom.get(servicename).value;
				servicetId = servicesMapArray[servicename];
				for(var k = 0;k<serNameAndCharges.length;k++){
					if(servicetId == serNameAndCharges[k].SERVICE_ID){
						servicedeptName = serNameAndCharges[k].DEPT_NAME;
					}
				}
				var serviceremark = YAHOO.util.Dom.get(serviceremarks).value;
				noOfTimes = document.getElementById(noOfTimes).value;
				if((servicedeptName != "") && (servicetId != "") && (servicename != "") && (document.getElementById(serviceDeleteCharge).value == 'false')){
					innertServiceHTML(servicedeptName, servicetId, servicename,noOfTimes,serviceremark,ints);
					ints++;
				}
		 	}
		}
	}
var inte = 0;
	for(var c=0; c<equipmenttablerows; c++){
		var equipment = "equipment" + c;
		var  duration = "duration" + c;
		var equipmentunits = "equipmentunits" + c;
		var equipmentremarks = "equipmentremarks" + c;
		var equipmentCheckBox = "equipmentCheckBox" + c;
		var equipmentDeleteCharge = "equipmentDeleteCharge"+ c;
		var eqCharge = "";

		if(YAHOO.util.Dom.get(equipment) == null){
		}else{

		var equipmentname = YAHOO.util.Dom.get(equipment).value;
		var eqdept = equipmentname.substring(equipmentname.indexOf("(")+1,equipmentname.indexOf(")"));
		equipmentname = equipmentname.substring(0,equipmentname.indexOf("("));
		var equipmentremark = YAHOO.util.Dom.get(equipmentremarks).value;
		var equipmentId = "";

			for(var k = 0;k<equipments.length;k++){
			  if(eqdept == equipments[k].DEPT_NAME){
				if(equipmentname == equipments[k].EQUIPMENT_NAME){
					equipmentId = equipments[k].EQ_ID;
					eqCharge = equipments[k].CHARGE;
					eqdept = equipments[k].DEPT_ID;
				}
				}
				}
				var eqduration = document.getElementById(duration).value;
		if((equipmentname != "") && (equipmentId != "") && (document.getElementById(equipmentDeleteCharge).value == 'false')){
			innertEquipmentHTML(equipmentname, equipmentId, eqduration,document.getElementById(equipmentunits).value,eqdept,eqCharge,equipmentremark,inte);
			inte++;
			}
			}
		}

			if(status==1){
			   document.forms[0].action = "../../pages/ipservices/Otservices.do?method=saveOperationDetails&dept="+department;
	           document.forms[0].submit();
          	 }else{
	           alert("please enter ot activities");
	           return false;
           }

}
 function innerOtherServiceHTML(otherserviceGroupName,otherserviceName,otherserviceqtyvalue,remarks,index){
                status =1;
                var innerOtherTabObj = document.getElementById("innerOtherTab");

				var trObj = "", tdObj = "";
				trObj = innerOtherTabObj.insertRow(index-1);
				tdObj = trObj.insertCell(0);

				var obj = document.createElement('input');
				obj.type = 'hidden';
				obj.name = 'otherserviceGroup';
				obj.value = otherserviceGroupName;
				tdObj.appendChild(obj);

				tdObj = trObj.insertCell(1);
				obj = document.createElement('input');
				obj.type = 'hidden';
				obj.name = 'otherservice';
				obj.value = otherserviceName;
				tdObj.appendChild(obj);

				tdObj = trObj.insertCell(2);
				obj = document.createElement('input');
				obj.type = 'hidden';
				obj.name = 'otherserviceqty';
				obj.value = otherserviceqtyvalue;
				tdObj.appendChild(obj);

				tdObj = trObj.insertCell(3);
				obj = document.createElement('input');
				obj.type = 'hidden';
				obj.name = 'otherserviceremarks';
				obj.value = remarks;
				tdObj.appendChild(obj);

		}
function innertMedHTML(medicine, medicinedosage, noofdays,quantity,medremarks,index){

				status =1;
                var innerOtherTabObj = document.getElementById("innermedTab");
				var trObj = "", tdObj = "";
				trObj = innerOtherTabObj.insertRow(index-1);
				tdObj = trObj.insertCell(0);

				var obj = document.createElement('input');
				obj.type = 'hidden';
				obj.name = 'medicine';
				obj.value = medicine;
				tdObj.appendChild(obj);

				tdObj = trObj.insertCell(1);
				obj = document.createElement('input');
				obj.type = 'hidden';
				obj.name = 'medicinedosage';
				obj.value = medicinedosage;
				tdObj.appendChild(obj);

				tdObj = trObj.insertCell(2);
				obj = document.createElement('input');
				obj.type = 'hidden';
				obj.name = 'mednoofdays';
				obj.value = noofdays;
				tdObj.appendChild(obj);

				tdObj = trObj.insertCell(3);
				obj = document.createElement('input');
				obj.type = 'hidden';
				obj.name = 'medquantity';
				obj.value = quantity;
				tdObj.appendChild(obj);

				tdObj = trObj.insertCell(4);
				obj = document.createElement('input');
				obj.type = 'hidden';
				obj.name = 'medremarks';
				obj.value = medremarks;
				tdObj.appendChild(obj);
		}
function innertEquipmentHTML(equipmentname, equipmentId, eqduration,equnit,eqdept,eqCharge,equipmentremark,index){
				status =1;
                var innerOtherTabObj = document.getElementById("equipmentTab");
				var trObj = "", tdObj = "";
				trObj = innerOtherTabObj.insertRow(index-1);
				tdObj = trObj.insertCell(0);

				var obj = document.createElement('input');
				obj.type = 'hidden';
				obj.name = 'equipmentname';
				obj.value = equipmentname;
				tdObj.appendChild(obj);

				tdObj = trObj.insertCell(1);
				obj = document.createElement('input');
				obj.type = 'hidden';
				obj.name = 'equipmentId';
				obj.value = equipmentId;
				tdObj.appendChild(obj);

				tdObj = trObj.insertCell(2);
				obj = document.createElement('input');
				obj.type = 'hidden';
				obj.name = 'equipmentduration';
				obj.value = eqduration;
				tdObj.appendChild(obj);

				tdObj = trObj.insertCell(3);
				obj = document.createElement('input');
				obj.type = 'hidden';
				obj.name = 'equipmentunit';
				obj.value = equnit;
				tdObj.appendChild(obj);

				tdObj = trObj.insertCell(4);
				obj = document.createElement('input');
				obj.type = 'hidden';
				obj.name = 'equipmentdepartment';
				obj.value = eqdept;
				tdObj.appendChild(obj);

				tdObj = trObj.insertCell(5);
				obj = document.createElement('input');
				obj.type = 'hidden';
				obj.name = 'unitcharge';
				obj.value = eqCharge;
				tdObj.appendChild(obj);

				tdObj = trObj.insertCell(6);
				obj = document.createElement('input');
				obj.type = 'hidden';
				obj.name = 'equipmentremark';
				obj.value = equipmentremark;
				tdObj.appendChild(obj);
		}

var flag = 0;
var operation = "";
function getOPEComponents(obj,id){
		var opeid = "status"+obj
		obj =document.getElementById(opeid).innerHTML;
		document.forms[0].hoperationid.value=id;

		if(flag==0){
		document.getElementById("opename").innerHTML = 'Operation -'+ obj;
		document.getElementById("otid").style.display="block";
		flag = 1;
		operation = obj;
		}else{
		if(operation==obj){
		document.getElementById("opename").innerHTML = 'Operation -'+ obj;
		document.getElementById("otid").style.display="none";
		flag = 0;
		}else{
		document.getElementById("opename").innerHTML = 'Operation -'+ obj;
		}
		}
}
   		var noOfOpesDeleted = 0;
function addRowToOpeTable(){
	var opetable = document.getElementById("opprescriptionTable");
	var len = opetable.rows.length;
	var tdObj="", trObj="";
	trObj = opetable.insertRow(len);
	if(noOfOpesDeleted > 0){
			len = len + noOfOpesDeleted;
		}
		len = len - prescribedoperationsjson.length;
	var opdepts = "opdepts" + len;
	var opdeptac = "opdeptac" + len;
	var opdeptscontainer = "opdeptscontainer" + len;
	var operationsac = "operationsac" + len;
	var operations = "operations" + len;
	var operationscontainer = "operationscontainer" + len;
	var operationremarks = "operationremarks" + len;
	var theatersac = "theatersac" + len;
	var theaters = "theaters" + len;
	var theaterscontainer = "theaterscontainer" + len;
	var startdate = "startdate" + len;
	var enddate = "enddate" + len;
	var starttime = "starttime" + len;
	var endtime = "endtime" + len;
	var opeCheckBox = "opeCheckBox" + len;
	var otcharge = "otcharge" + len;
	var otsurgeonac = "otsurgeonac" + len;
	var otsurgeon = "otsurgeon" + len;
	var otsurgeoncontainer = "otsurgeoncontainer" + len;
	var otanesthetistac = "otanesthetistac" + len;
	var otanesthetist = "otanesthetist" + len;
	var otanesthetistcontainer = "otanesthetistcontainer" +len;


	tdObj = trObj.insertCell(0);
	tdObj.setAttribute("class", "yui-skin-sam");
	tdObj.setAttribute("valign", "top");
	tdObj.innerHTML = '<div id="'+ operationsac +'" class="autocomplete" style="width: 250" ><input id="'+ operations +'" style="width: 250;" name="'+ operations +'" type="text"   /><div id="'+operationscontainer  +'" class="scrolForContainer"></div></div>';

	tdObj = trObj.insertCell(1);
	tdObj.setAttribute("class", "yui-skin-sam");
	tdObj.setAttribute("valign", "top");
	tdObj.innerHTML = '<div id="'+ theatersac +'" class="autocomplete" style="width: 120" ><input id="'+ theaters +'" style="width: 120;" name="'+ theaters +'" type="text"   /><div id="'+ theaterscontainer +'" class="scrolForContainer"></div></div>';

	tdObj = trObj.insertCell(2);
	tdObj.setAttribute("class", "yui-skin-sam");
	tdObj.setAttribute("valign", "top");
	tdObj.innerHTML = '<input type="checkbox" name="'+otcharge+'" id="'+otcharge+'"/>'

	tdObj = trObj.insertCell(3);
	tdObj.setAttribute("class", "yui-skin-sam");
	tdObj.setAttribute("valign", "top");
	tdObj.innerHTML = '<div id="'+ otsurgeonac +'" class="autocomplete" style="width: 150" ><input id="'+ otsurgeon +'" style="width: 150;" name="'+ otsurgeon +'" type="text"   /><div id="'+ otsurgeoncontainer +'" class="scrolForContainer"></div></div>';

	tdObj = trObj.insertCell(4);
	tdObj.setAttribute("class", "yui-skin-sam");
	tdObj.setAttribute("valign", "top");
	tdObj.innerHTML = '<div id="'+ otanesthetistac +'" class="autocomplete" style="width: 150" ><input id="'+ otanesthetist +'" style="width: 150;" name="'+ otanesthetist +'" type="text"   /><div id="'+ otanesthetistcontainer +'" class="scrolForContainer"></div></div>';

	tdObj = trObj.insertCell(5);
	tdObj.setAttribute("class", "yui-skin-sam");
	tdObj.setAttribute("valign", "top");

	tdObj.innerHTML = getDateWidget(startdate, startdate, gServerNow,
	null, null, false, true);
	makePopupCalendar(startdate);

	tdObj = trObj.insertCell(6);
	tdObj.setAttribute("class", "yui-skin-sam");
	tdObj.setAttribute("valign", "top");
	tdObj.innerHTML = '<input type="text" name="'+starttime+'" id="'+starttime+'" maxlength="5" style="width: 90" />';

	tdObj = trObj.insertCell(7);
	tdObj.innerHTML = getDateWidget(enddate, enddate, gServerNow,
	null, null, false, true);
	makePopupCalendar(enddate);

	tdObj = trObj.insertCell(8);
	tdObj.setAttribute("class", "yui-skin-sam");
	tdObj.setAttribute("valign", "top");
	tdObj.innerHTML = '<input type="text" name="'+ endtime +'" id="'+ endtime +'"  maxlength="5" style="width: 90" />';

	tdObj = trObj.insertCell(9);
	tdObj.setAttribute("class", "yui-skin-sam");
	tdObj.setAttribute("valign", "top");
	tdObj.innerHTML = '<input type="text" name="'+operationremarks+'" id="'+operationremarks+'"  maxlength="100" style="width: 140"/>';

	tdObj = trObj.insertCell(10);
	tdObj.innerHTML = '<input type="checkbox" name="opeCheckBox" id="'+opeCheckBox+'"/>';

	opetime(starttime,endtime);
	opDeptAutoComplete(operations,operationscontainer);
	opeTheatersAutoComplete(theaters,theaterscontainer);
	otDoctorsAutoComplete(YAHOO.util.Dom.get(otsurgeon),otsurgeoncontainer,"otsurgeon1",len);
	otanestetistautocomplete(otanesthetist,otanesthetistcontainer);
}

function opeTheatersAutoComplete(theaters,theaterscontainer){

		YAHOO.example.TheatersArray = [];
		var i=0;
		for(var j = 0;j<theaterslist.length;j++){
				YAHOO.example.TheatersArray[i] =theaterslist[j].THEATRE_NAME;
				i++;
				}
		YAHOO.example.ACJSArray = new function() {
		this.oTheaterACDS = new YAHOO.widget.DS_JSArray(YAHOO.example.TheatersArray);
		this.oTheaterAutoComp = new YAHOO.widget.AutoComplete(theaters,theaterscontainer, this.oTheaterACDS);
		this.oTheaterAutoComp.prehighlightClassName = "yui-ac-prehighlight";
		this.oTheaterAutoComp.typeAhead = true;
		this.oTheaterAutoComp.useShadow = false;
		this.oTheaterAutoComp.allowBrowserAutocomplete = false;
		this.oTheaterAutoComp.minQueryLength = 0;
		this.oTheaterAutoComp.forceSelection = true;
		this.oTheaterAutoComp.animVert = false;
	}
}


function scheduleotDoctorsAutoComplete(otdoctor,otdoctorcontainer,fieldName){
			otDoctorsArray.length = 0;
			var n =0;
			for(var k =0;k<otlist.length;k++){
				otDoctorsArray[n] = otlist[k].DOCTOR_NAME;
				n++;
			}

			var dataSource = new YAHOO.widget.DS_JSArray(otDoctorsArray, { queryMatchContains : true } );
			this.oOtDocAutoComp = new YAHOO.widget.AutoComplete(otdoctor,otdoctorcontainer, dataSource);
			this.oOtDocAutoComp.formatResult = Insta.autoHighlight;
			this.oOtDocAutoComp.prehightlightClassName = "yui-ac-prehighlight";
			this.oOtDocAutoComp.useShadow = false;
			this.oOtDocAutoComp.allowBrowserAutocomplete = false;
			this.oOtDocAutoComp.minQueryLength = 0;
			this.oOtDocAutoComp.forceSelection = true;
			this.oOtDocAutoComp.animVert = false;
			if (fieldName=="primarysurgeon") {
				this.oOtDocAutoComp.itemSelectEvent.subscribe(primarysurgeons);
			}
			if(fieldName=="otdoctor1") {
				this.oOtDocAutoComp.itemSelectEvent.subscribe(function(sType, aArgs){
					 var oData = aArgs[2];
					 otdoctor.value=oData;
				});
			}
			if(fieldName=="otsurgeon1") {
				this.oOtDocAutoComp.itemSelectEvent.subscribe(otsurgeons);
			}
}
var oDocAutoComp = [];
var otDocACDS;
var otDoctArr = [];

function populateDoctors(otdoctor,otdoctorcontainer,fieldName,list,index){
	YAHOO.util.Dom.get(otdoctor).value = '';
	var docArray = [];
	for(var i = 0;i<list.length;i++){
		var doctor = list[i];
		docArray[i] = doctor["DOCTOR_NAME"];
	}
	if(oDocAutoComp[index] != undefined){
		oDocAutoComp[index].destroy();
	}

	 var dataSource = new YAHOO.widget.DS_JSArray(docArray, { queryMatchContains : true } );
	 oDocAutoComp[index] = new YAHOO.widget.AutoComplete(otdoctor,otdoctorcontainer, dataSource);
	 oDocAutoComp[index].formatResult = Insta.autoHighlight;
	 oDocAutoComp[index].prehightlightClassName = "yui-ac-prehighlight";
	 oDocAutoComp[index].useShadow = false;
	 oDocAutoComp[index].allowBrowserAutocomplete = false;
	 oDocAutoComp[index].minQueryLength = 0;
	 oDocAutoComp[index].forceSelection = true;
	 oDocAutoComp[index].itemSelectEvent.subscribe(function(sType, aArgs){
		 var oData = aArgs[2];
		 YAHOO.util.Dom.get(otdoctor).value=oData;
		});
}

function otDoctorsAutoComplete(index){
			if((document.getElementById("opdoctortype"+index).value == 'ANAOPE') || (document.getElementById("opdoctortype"+index).value == 'AANOPE') ){
				populateDoctors("otdoctor"+index,"otdoctorcontainer"+index,"otdoctor1",otanaesthesistslist,index);
			}else{
				populateDoctors("otdoctor"+index,"otdoctorcontainer"+index,"otdoctor1",otsurgeonslist,index);
			}
}

var primarysurgeons = function(sType, aArgs) {
	 var oData = aArgs[2];
	 document.getElementById('primarysurgeon').value=oData;
}

var otsurgeons = function(sType, aArgs) {
	 var oData = aArgs[2];
	 document.getElementById('otsurgeon1').value=oData;
}

function otanestetistautocomplete(otanesthetist,otanesthetistcontainer){
			YAHOO.example.otAneArray = [];
			YAHOO.example.otAneArray.length = 0;
			var n =0;
			for(var k =0;k<otanaesthesistslist.length;k++){
				YAHOO.example.otAneArray[n] = otanaesthesistslist[k].DOCTOR_NAME;
				n++;
			}
			this.oOtAneSCDS = new YAHOO.widget.DS_JSArray(YAHOO.example.otAneArray);
			this.oOtAneAutoComp = new YAHOO.widget.AutoComplete(otanesthetist,otanesthetistcontainer, this.oOtAneSCDS);
			this.oOtAneAutoComp.prehightlightClassName = "yui-ac-prehighlight";
			this.oOtAneAutoComp.typeAhead = true;
			this.oOtAneAutoComp.useShadow = false;
			this.oOtAneAutoComp.allowBrowserAutocomplete = false;
			this.oOtAneAutoComp.minQueryLength = 0;
			this.oOtAneAutoComp.forceSelection = true;
			this.oOtAneAutoComp.animVert = false;

}


function anesthesiatypeautocomplete(anesthesia_type,anesthesiatypecontainer){
		YAHOO.example.aneArray = [];
		YAHOO.example.aneArray.length = 0;
		var n =0;
		for(var k =0;k<anaesthesiatypeslist.length;k++){
			YAHOO.example.aneArray[n] = anaesthesiatypeslist[k].ANESTHESIA_TYPE;
			n++;
		}
		this.aneSCDS = new YAHOO.widget.DS_JSArray(YAHOO.example.aneArray);
		this.aneAutoComp = new YAHOO.widget.AutoComplete(anesthesia_type,anesthesiatypecontainer, this.aneSCDS);
		this.aneAutoComp.prehightlightClassName = "yui-ac-prehighlight";
		this.aneAutoComp.typeAhead = true;
		this.aneAutoComp.useShadow = false;
		this.aneAutoComp.allowBrowserAutocomplete = false;
		this.aneAutoComp.minQueryLength = 0;
		this.aneAutoComp.forceSelection = false;
		this.aneAutoComp.animVert = false;
}

function equipmentsAutoComplete(){
			YAHOO.example.eqpDeptsArray = [];
			var i=0;
			for(var j = 0;j<equipments.length;j++){
				YAHOO.example.eqpDeptsArray.length = i+1;
				YAHOO.example.eqpDeptsArray[i] = equipments[j].DEPT_NAME;
				i++;
				}
			YAHOO.example.ACJSArray = new function() {
			this.oACDS = new YAHOO.widget.DS_JSArray(YAHOO.example.eqpDeptsArray);
			this.oAutoComp = new YAHOO.widget.AutoComplete('equipmentdept1','eqdeptcontainer1', this.oACDS);
			this.oAutoComp.prehighlightClassName = "yui-ac-prehighlight";
			this.oAutoComp.typeAhead = true;
			this.oAutoComp.useShadow = true;
			this.oAutoComp.allowBrowserAutocomplete = false;
			this.oAutoComp.minQueryLength = 0;
			this.oAutoComp.textboxFocusEvent.subscribe(function(){
							var sInputValue = YAHOO.util.Dom.get('equipmentdept1').value;
							if(sInputValue.length === 0) {
     						var oSelf = this;
     						setTimeout(function(){oSelf.sendQuery(sInputValue);},0);
     						}
							});
			this.oAutoComp.unmatchedItemSelectEvent.subscribe(unmatchedItemSelected);
			this.oAutoComp.containerCollapseEvent.subscribe(populateEquipments);
		    					}

			function unmatchedItemSelected(oSelf){
				alert("no matches found please select the existing Departments");
				return false;
			}

			YAHOO.example.EquipmentNamesArray = [];
			function populateEquipments(oSelf){
			YAHOO.example.EquipmentNamesArray.length = 0;
			var deptName = YAHOO.util.Dom.get('equipmentdept1').value;
			YAHOO.util.Dom.get("equipment1").value = "";
			var k=0;
			for(var l = 0;l<equipments.length;l++){
			if(deptName == equipments[l].DEPT_NAME){
				YAHOO.example.EquipmentNamesArray.length = k + 1;
				YAHOO.example.EquipmentNamesArray[k] = equipments[l].EQUIPMENT_NAME;
				k++;
				}
				}
			this.oEqpSCDS = new YAHOO.widget.DS_JSArray(YAHOO.example.EquipmentNamesArray);
			this.oEqpAutoComp = new YAHOO.widget.AutoComplete("equipment1","equipmentcontainer1", this.oEqpSCDS);
			this.oEqpAutoComp.prehightlightClassName = "yui-ac-prehighlight";
			this.oEqpAutoComp.typeAhead = true;
			this.oEqpAutoComp.useShadow = true;
			this.oEqpAutoComp.allowBrowserAutocomplete = false;
			this.oEqpAutoComp.minQueryLength = 0;
			this.oEqpAutoComp.textboxFocusEvent.subscribe(function(){
			var sInputValue = YAHOO.util.Dom.get('equipment1').value;
			if(sInputValue.length === 0) {
			var oSelf = this;
			setTimeout(function(){oSelf.sendQuery(sInputValue);},0);
 						}
			});
			this.oEqpAutoComp.containerCollapseEvent.subscribe(populateUnits);

					}
			function populateUnits(oSelf){
			var unit = "";
			var deptname = YAHOO.util.Dom.get('equipmentdept1').value;
			var equipmentname = YAHOO.util.Dom.get('equipment1').value;
			var a = 0;
				for(var u = 0;u<equipments.length;u++){
					if(deptname == equipments[u].DEPT_NAME ){
 					if(equipmentname == equipments[u].EQUIPMENT_NAME){
  					if(equipments[u].EQIPMENT_TYPE=='D'){
  					document.getElementById("serviceunits1").value = 'Daily' ;
  					}else{
  					document.getElementById("serviceunits1").value = 'Hourly' ;
  					}
 					}
					}
				}
			}
}
function medicineDoasageAutoComplete(){
		YAHOO.example.dosageArray = [];
		var i=0;
		for(var j = 0;j<dosage.length;j++){
		YAHOO.example.dosageArray.length = i+1;
		YAHOO.example.dosageArray[i] = dosage[j].DOSAGE;
			i++;
		}
		YAHOO.example.ACJSArray = new function() {
		this.oACDS = new YAHOO.widget.DS_JSArray(YAHOO.example.dosageArray);
		this.oAutoComp = new YAHOO.widget.AutoComplete('medicinedosage1','meddosagecontainer1', this.oACDS);
		this.oAutoComp.prehighlightClassName = "yui-ac-prehighlight";
		this.oAutoComp.typeAhead = true;
		this.oAutoComp.useShadow = true;
		this.oAutoComp.minQueryLength = 0;
		this.oAutoComp.allowBrowserAutocomplete = false;
		this.oAutoComp.forceSelection = true;
		this.oAutoComp.textboxFocusEvent.subscribe(function(){
					var sInputValue = YAHOO.util.Dom.get('medicinedosage1').value;
					if(sInputValue.length === 0) {
						var oSelf = this;
						setTimeout(function(){oSelf.sendQuery(sInputValue);},0);
					}
				});
		this.oAutoComp.containerCollapseEvent.subscribe(function(){
				  return calculateQuantity(noofdays1,quantity1,medicinedosage1);
			});
		}
}
function getDiff(starttime,endtime){
    var lastDot= starttime.indexOf(' ');
 	var nextdot=starttime.indexOf(':',lastDot+1);
    var hours=starttime.substring(1,2);
    var mindot=starttime.indexOf(':',nextdot+1);
 	var mins=starttime.substring(nextdot+1,mindot);
    var secdot=starttime.indexOf(':',mindot+1);
    var secs=starttime.substring(mindot+1,secdot);
}

function getOTReport(obj){

var mrNo = document.getElementById("mrno").value;
var PatId = document.getElementById("patientid").value;

var href = obj.getAttribute("href");
var href1 = href+"&Mrno="+mrNo+"&patient_id="+PatId+"&templateType=In+Patient";

obj.setAttribute("href",href1);
return true;

}
function validateTime(timeField, index) {
	var strTime = timeField.value;
	var timePattern = /[0-9]:[0-9]/;
	var regExp = new RegExp(timePattern);
	if (strTime == '') {
		return true;
	}
	if (regExp.test(strTime)) {
		var strHours = strTime.split(':')[0];
		var strMinutes = strTime.split(':')[1];
		if (!isInteger(strHours)) {
			alert("Incorrect time format : hour is not a number");
			timeField.focus();
			return false;
		}
		if (!isInteger(strMinutes)) {
			alert("Incorrect time format : minute is not a number");
			timeField.focus();
			return false;
		}else{
			if( strMinutes.length < 2 ){
				timeField.value = timeField.value+"0";
			}
		}
		if ((parseInt(strHours) > 23) || (parseInt(strHours) < 0)) {
			alert("Incorrect hour : please enter 0-23 for hour");
			timeField.focus();
			return false;
		}
		if ((parseInt(strMinutes) > 59) || (parseInt(strMinutes) < 0)) {
			alert("Incorrect minute : please enter 0-59 for minute");
			timeField.focus();
			return false;
		}

	} else {
		alert("Incorrect time format : please enter HH:MI");
		timeField.focus();
		return false;
	}
	return true;
}
function validate(){
	if(YAHOO.util.Dom.get('theatre').value == ''){
		alert("Please select Theatre ");
		return false;
	}else{
		for(var i =0 ;i<theaterslist.length;i++){
			if(YAHOO.util.Dom.get('theatre').value == theaterslist[i].THEATRE_NAME){
				document.forms[0].theatreid.value = theaterslist[i].THEATRE_ID;
			}
		}
	}
	if(YAHOO.util.Dom.get('primarysurgeon').value == ''){
		alert("Please select primary surgeon");
		return false;
	}else{
		for(var j = 0; j< otlist.length;j++){
			if(YAHOO.util.Dom.get('primarysurgeon').value == otlist[j].DOCTOR_NAME){
				document.forms[0].surgeonid.value = otlist[j].DOCTOR_ID;
			}
		}
	}
	if(!YAHOO.util.Dom.get('primaryanae').value == ''){
		for(var j = 0; j< otlist.length;j++){
			if(YAHOO.util.Dom.get('primaryanae').value == otlist[j].DOCTOR_NAME){
				document.forms[0].anaestesistid.value = otlist[j].DOCTOR_ID;
			}
		}

	}

	if(YAHOO.util.Dom.get('statrdate').value == ''){
		alert("Please enter start date");
		return false;
	}

	if(YAHOO.util.Dom.get('starttime').value == ''){
		alert("Please enter start time");
		return false;
	}

	if(!validateTime(YAHOO.util.Dom.get('starttime'),1))return false;

	if(YAHOO.util.Dom.get('enddate').value == ''){
		alert("Please enter end date");
		return false;
	}
	if(YAHOO.util.Dom.get('endtime').value == ''){
		alert("Please enter end time");
		return false;
	}
	if(!validateTime(YAHOO.util.Dom.get('endtime'),1))return false;
	if(!compareTimes(YAHOO.util.Dom.get('starttime').value,YAHOO.util.Dom.get('endtime').value,YAHOO.util.Dom.get('statrdate').value,YAHOO.util.Dom.get('enddate').value)) return false;
	document.forms[0].action="operationschedule.do?";
    document.forms[0].method="scheduleOperation";
    document.forms[0].submit();
	return true;

}
function setHrefToOt(obj,billno){
		var href = obj.getAttribute("href");
		href = "../../pages/ipservices/prescriptions.do?method=getOTServicesScreen&mrno="+document.forms[0].mrno.value+"&patientid="+
		document.forms[0].patientid.value+"&billno="+billno;
		obj.setAttribute("href",href);
		return true;
}
function getServices(serviceGroup,service,container,qty,ratefld,amt){
if(serviceGroup=="OCOTC"){
populateOtherCharges(service,container,othercharges,ratefld,qty,amt);
}
else if(serviceGroup=="CONOTC"){
populateOtherCharges(service,container,consumables,ratefld,qty,amt);
}else if(serviceGroup=="IMPOTC"){
populateOtherCharges(service,container,implants,ratefld,qty,amt);
}else if(serviceGroup==""){
service.value = "";
YAHOO.example.othercharges.length = 0;
otherserviceAutoComp.forceSelection = false;
}

}
var otherserviceAutoComp;
var otherACDS;
YAHOO.example.othercharges = [];
function populateOtherCharges(service,container,list,ratefld,qty,amt){

		dataentered = true;
					YAHOO.example.othercharges.length = 0;
					service.value = "";
					var i=0;
					for(var j = 0;j<list.length;j++){
					YAHOO.example.othercharges.length = i+1;
					YAHOO.example.othercharges[i] = list[j].CHARGE_NAME;
						i++;
					}


		YAHOO.example.ACJSArray = new function() {
						otherACDS = new YAHOO.widget.DS_JSArray(YAHOO.example.othercharges);
						otherserviceAutoComp = new YAHOO.widget.AutoComplete(service,container, otherACDS);
						otherserviceAutoComp.maxResultsDisplayed = 20;
						otherserviceAutoComp.allowBrowserAutocomplete = false;
						otherserviceAutoComp.prehighlightClassName = "yui-ac-prehighlight";
						otherserviceAutoComp.typeAhead = false;
						otherserviceAutoComp.useShadow = false;
						otherserviceAutoComp.minQueryLength = 0;
						otherserviceAutoComp.forceSelection = true;
						otherserviceAutoComp.animVert = false;
						if(billing == 'Y'){
							otherserviceAutoComp.containerCollapseEvent.subscribe(populateCharge);
						}
						otherserviceAutoComp.textboxBlurEvent.subscribe(function(){
							if(YAHOO.util.Dom.get(service).value == ''){
								dataentered = false;
							}
						});

 					}
 					function populateCharge(){
 						var services = YAHOO.util.Dom.get(service).value;
	 					for(var j = 0;j<list.length;j++){
	 						if(services == list[j].CHARGE_NAME){
	 							YAHOO.util.Dom.get(ratefld).value = list[j].CHARGE;
	 							resetAmounts(ratefld,qty,amt);
	 						}
	 					}
 					}

}
function makeblank(timefield){
	timefield.value="";
}
function calculateQuantity(noofdays,quantity,medicinedosage){
		var noOfMedicineRows = document.getElementById("medicinetable").rows.length;
		for(var i=1; i<=noOfMedicineRows; i++){
			var dosage = medicinedosage.value;
			noofdays = noofdays.value;
		}
		if((noofdays!="")&&(!isNaN(noofdays))){

			var quant='';
			if((dosage!="")&& noofdays!=""){

				if(dosage=='OD'){
					quant=noofdays;
				}
				if(dosage=='BD'){
					quant=eval(2*eval(noofdays));
				}
				if(dosage=='TID'){
					quant=eval(3*eval(noofdays));
				}
				if(dosage=='QID'){
					quant=eval(4*eval(noofdays));
				}
				quantity.value=quant;
			}
		}
	}



	function getOtherChargeheads(len){

	var otherservice = "otherserviceGroup" + len;
	if(chargeHeadsJSON != null){
		var otcChargeHeads  = filterList(chargeHeadsJSON,"CHARGEGROUP_ID","OTC");
		var otherChargeHeadsJSON = filterList(otcChargeHeads,"ASSOCIATED_MODULE","mod_prescribe");
		if(otherChargeHeadsJSON != null){
			YAHOO.util.Dom.get(otherservice).options.length = otherChargeHeadsJSON.length +1;

			var option = new Option("...Select...","");
			YAHOO.util.Dom.get(otherservice).options[0]= option;

			for(var i=0;i<otherChargeHeadsJSON.length;i++){
				 var item = otherChargeHeadsJSON[i];
				 var head_name = item["CHARGEHEAD_NAME"];
				 var head_id = item["CHARGEHEAD_ID"];
				 var option = new Option(head_name,head_id);
				 YAHOO.util.Dom.get(otherservice).options[i+1]= option;
			}
		}
	}
}

var servicesMapArray = [];
function initServiceAutoComplete(len){

		var serviceNamesArray = [];
		var service = "servicename" + len;
		var service_container = "servicenamecontainer" +len;
		var servicerate  = "servicerate" +len;
		var serviceqty = "noOfTimes" + len;
		var serviceamt = "serviceamt" + len;
		var serviceremarks = "serviceremarks" + len;
		YAHOO.util.Dom.get(service).value = "";
		if(serNameAndCharges != null){
		servicesMapArray = new Array(serNameAndCharges.length);
		serviceNamesArray.length = serNameAndCharges.length;
		for(var l = 0;l<serNameAndCharges.length;l++){
			serviceNamesArray[l] = serNameAndCharges[l].SERVICE_NAME+'('+serNameAndCharges[l].DEPT_NAME+')';
			servicesMapArray[serNameAndCharges[l].SERVICE_NAME+'('+serNameAndCharges[l].DEPT_NAME+')'] = serNameAndCharges[l].SERVICE_ID;
			}
		}
		this.oServSCDS = new YAHOO.widget.DS_JSArray(serviceNamesArray);
		oServAutoComp = new YAHOO.widget.AutoComplete(service,service_container, this.oServSCDS);
		oServAutoComp.maxResultsDisplayed = 20;
		oServAutoComp.allowBrowserAutocomplete = false;
		oServAutoComp.prehighlightClassName = "yui-ac-prehighlight";
		oServAutoComp.typeAhead = false;
		oServAutoComp.useShadow = false;
		oServAutoComp.minQueryLength = 0;
		oServAutoComp.forceSelection = true;
		oServAutoComp.animVert = false;
		oServAutoComp.itemSelectEvent.subscribe(function(){
			dataentered = true;
		});
		oServAutoComp.textboxBlurEvent.subscribe(function(){
			if(YAHOO.util.Dom.get(service).value == ''){
				dataentered = false;
				YAHOO.util.Dom.get(serviceremarks).value = "";
				YAHOO.util.Dom.get(serviceqty).value = 1;
				if(billing == 'Y'){
					YAHOO.util.Dom.get(servicerate).value = 0;
					YAHOO.util.Dom.get(serviceamt).value = 0;
					resetAmounts(YAHOO.util.Dom.get(servicerate),YAHOO.util.Dom.get(serviceqty),YAHOO.util.Dom.get(serviceamt));
				}

			}
		});
		if(billing == 'Y'){
			oServAutoComp.containerCollapseEvent.subscribe(populateCharge);
		}
		function populateCharge(){
			var services = YAHOO.util.Dom.get(service).value;
			for(var j = 0;j<serNameAndCharges.length;j++){
				if(services == serviceNamesArray[j]){
					YAHOO.util.Dom.get(servicerate).value = serNameAndCharges[j].UNIT_CHARGE;
					resetAmounts(YAHOO.util.Dom.get(servicerate),YAHOO.util.Dom.get(serviceqty),YAHOO.util.Dom.get(serviceamt));
				}
			}
		}
}
function addRowToPescServTable(){
		var presServtab = document.getElementById("prescribeServiceTab");
		var len = presServtab.rows.length;
		if(YAHOO.util.Dom.get("servicename"+(len-1)).value != ""){
		var tdObj="", trObj="";
		var serviceRow = "serviceRow" +len;
		trObj = presServtab.insertRow(len);
		trObj.id = serviceRow;
		if(noofServicesDeleted > 0){
			len = len + noofServicesDeleted;
		}

		var servicedeptac = "servicedeptac" + len;
		var txtservicedept = "servicedept" + len;
		var servicedeptcontainer = "servicedeptcontainer" + len;

		var servicenameac = "servicenameac" + len;
		var txtservicename = "servicename" + len;
		var servicenamecontainer = "servicenamecontainer" + len;
		var noOfTimes = "noOfTimes" + len;
		var serviceCharge = "serviceCharge" + len;
		var serviceremarks = "serviceremarks" + len;
		var serviceCheckBox = "serviceCheckBox" + len;
		var servicerate = "servicerate" + len;
		var serviceamt = "serviceamt" + len;
		var serviceDeleteCharge = "serviceDeleteCharge" + len;

		tdObj = trObj.insertCell(0);
		tdObj.setAttribute("class", "yui-skin-sam");
		tdObj.setAttribute("valign", "top");
		tdObj.innerHTML = '<div id="'+ servicenameac +'" class="autocomplete" style="width: 310;" ><input class="text-input" id="'+ txtservicename +'" name="'+ txtservicename +'" type="text"   style="width: 310;" /><div id="'+ servicenamecontainer +'" class="scrolForContainer"></div></div>';

        tdObj = trObj.insertCell(1);
        tdObj.setAttribute("valign", "top");
		tdObj.innerHTML = '<input type="text" name="'+serviceremarks+'" id="'+serviceremarks+'" class="text-input" maxlength="200" style="width: 39em;"/>';


		tdObj = trObj.insertCell(2);
		tdObj.setAttribute("valign", "top");
		tdObj.innerHTML = '<input type="text" class="text-input" value="1" onchange="setValue(this,this.value);" name="'+ noOfTimes +'" ' +
					' id="'+ noOfTimes +'" onkeypress="return enterNumOnly(event);" style="width: 5em;" onblur="document.forms[0].PrescribeMoreServices.focus();" '+
					' onchange="return calcSerCharge(this)"/><input type="hidden" name="'+ serviceCharge +'" id="'+ serviceCharge +'"  />';

		tdObj = trObj.insertCell(3);
		tdObj.setAttribute("valign", "top");
		tdObj.innerHTML = '<input type="checkbox" name="'+serviceCheckBox+'" id="'+serviceCheckBox+'" onclick="cancel(this,'+serviceRow+')" /> '+
				      '<input type="hidden" name="'+serviceDeleteCharge+'" id="'+serviceDeleteCharge+'" value="false"/>';

		initServiceAutoComplete(len);
		YAHOO.util.Dom.get(txtservicename).focus();
		}
	}
function setTime(time){
	if(time.value.length == 2){
			time.value = time.value+":00";
		}
		if(time.value.length == 1){
			time.value = "0"+time.value+":00";
		}
}
function innertServiceHTML(servicedeptName, servicetId, servicename,noOfTimes,serviceremark,index){
	status =1;
	var innerOtherTabObj = document.getElementById("innerServiceTab");
	var trObj = "", tdObj = "";
	trObj = innerOtherTabObj.insertRow(index-1);
	tdObj = trObj.insertCell(0);

	var obj = document.createElement('input');
	obj.type = 'hidden';
	obj.name = 'servicedept';
	obj.value = servicedeptName;
	tdObj.appendChild(obj);

	tdObj = trObj.insertCell(1);
	obj = document.createElement('input');
	obj.type = 'hidden';
	obj.name = 'serviceid';
	obj.value = servicetId;
	tdObj.appendChild(obj);

	tdObj = trObj.insertCell(2);
	obj = document.createElement('input');
	obj.type = 'hidden';
	obj.name = 'servicename';
	obj.value = servicename;
	tdObj.appendChild(obj);

	tdObj = trObj.insertCell(3);
	obj = document.createElement('input');
	obj.type = 'hidden';
	obj.name = 'noOfTimes';
	obj.value = noOfTimes;
	tdObj.appendChild(obj);

	tdObj = trObj.insertCell(4);
	obj = document.createElement('input');
	obj.type = 'hidden';
	obj.name = 'serviceremark';
	obj.value = serviceremark;
	tdObj.appendChild(obj);

}
function cancel(checkbox,row, deleteCharge){

	if (document.getElementById(deleteCharge.id).value == 'true' ){
			document.getElementById(deleteCharge.id).value = false;
			document.getElementById(row.id).className = "newRow";
			document.getElementById(checkbox.id).src = cpath+"/icons/Delete.png";
	}else{
		document.getElementById(deleteCharge.id).value = true;
		document.getElementById(row.id).className = "delete";
		document.getElementById(checkbox.id).src = cpath+"/icons/Deleted.png";
	}
	resetTotals();
}

function compareTimes(time1,time2,start_date,end_date){

   var diff = getDateDiff(start_date,end_date);

    if(diff == -1)
    {
     alert("End date cannot be less than start date");
     return false; }
    if(diff == 0)
    {
	if(eval(time1.split(":"))[0] > eval(time2.split(":"))[0]){
		alert(" End time cannot be less than start time");
		return false;
	}
	}
	return true;
}
function changeDuration(checked){
	if(checked.checked){
		checked.value = 'H';
	}else{
	checked.value = 'D';
	}

}
