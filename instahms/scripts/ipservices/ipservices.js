/*
* JS file used in IPServcies
*/
var oAutoCompDisc;
var servicesNotExists = new Array();
var testsNotExists = new Array();
var mealNotExists = new Array();//for dietary
var splMealTime = ''; //for storing the time of Spl meal
function init(){
	if(ipservicesModule == 'Y'){
		initOperationsAutoComplete(1);
	}
	initMedicineAutoComplete(1);
	doctorAutoComplete("visitingDoctor","doctorcontainer1", doctor_list,1);
	doctorAutoComplete("prescribeddoctor","prescribeddoctorcontainer",doctor_list,1);
	initEquipmentAutoComplete(1);
	initServiceAutoComplete(1);
	if (dietaryModule == 'Y'){
		initMealAutoComplete(1);//for dietry
		getMealTimings(1);
	}
	initTestAutoComplete(1);
	setdatetime();
	dateandtime('visitdate1','visittime1');
	getConsultationChargeHeads(1);
	getOtherChargeheads(1);
	addPresTestsAndServices();
	if (servicesNotExists.length > 0) {
		var msg = "Following Prescribed Services are not available in Hostpital : \n";
		for (var i=0; i<servicesNotExists.length; i++) {
			msg = msg + (i+1) + ") " +servicesNotExists[i].service;
		}
		alert(msg);
	}
	if (testsNotExists.length > 0) {
		var msg = "Following Prescribed Tests are not available in Hospital : \n";
		for (var i=0; i<testsNotExists.length; i++) {
			msg = msg + (i+1) + ") " +testsNotExists[i].test + "\n";
		}
		alert(msg);
	}
	setInsuranceValues();
}
var tpa_id = "";
function setInsuranceValues(){
	if(billDetails.length == 1){
		for(var i=0;i<billDetails.length;i++){
			document.forms[0].billAmt.value = billDetails[i].TOTAL_AMOUNT;
			document.forms[0].aprovalAmt.value = billDetails[i].APPROVAL_AMOUNT;
			tpa_id = billDetails[i].TPA_ID;

			document.forms[0].bill_amount.value 	= billDetails[i].TOTAL_AMOUNT;
			document.forms[0].total_credit_amount.value = getPaise(billDetails[i].APPROVAL_AMOUNT) +
					getPaise(billDetails[i].DEPOSIT_SET_OFF) + getPaise(billDetails[i].TOTAL_RECEIPTS);

		}
	}else{
		nullifyAllBillAmountFields();
	}
	return true;
}

function onChangeSetInsuranceValues(){

	for(var i=0;i<billDetails.length;i++){

		if(billDetails[i].BILL_NO == document.forms[0].bill.value){
			document.forms[0].billAmt.value = billDetails[i].TOTAL_AMOUNT;
			document.forms[0].aprovalAmt.value = billDetails[i].APPROVAL_AMOUNT;
			tpa_id = billDetails[i].TPA_ID;

			document.forms[0].bill_amount.value 	= billDetails[i].TOTAL_AMOUNT;
			document.forms[0].total_credit_amount.value = getPaise(billDetails[i].APPROVAL_AMOUNT) +
					getPaise(billDetails[i].DEPOSIT_SET_OFF) + getPaise(billDetails[i].TOTAL_RECEIPTS);

		}
		if(document.forms[0].bill.value == "New" || document.forms[0].bill.value =="")
			nullifyAllBillAmountFields();
	}
}
function nullifyAllBillAmountFields(){
	document.forms[0].billAmt.value = "";
	document.forms[0].aprovalAmt.value = "";
	document.forms[0].bill_amount.value 	= "";
	document.forms[0].total_credit_amount.value = "";
	tpa_id = "";
}
function addPresTestsAndServices() {
	var index = 1;
	for (var i in allVisitTests) {
		var test = allVisitTests[i];
		if (test.test_name == null) {
			var len = testsNotExists.length;
			testsNotExists.length = len+1;
			testsNotExists[len] = test;
			continue;
		}
		/*
		* if index == 1 no need to add row.
		* b'cause first row is added( and AC's initianlized in init method ) in jsp only.
		*/
		if (index != 1)	addRowToPresTestTable(false);
		YAHOO.util.Dom.get('testName' + index).value = test.test_name;
		YAHOO.util.Dom.get('testremarks' + index).value = test.test_remarks;
		YAHOO.util.Dom.get('testConsultationId' + index).value = test.consultation_id;
		YAHOO.util.Dom.get('testPresId' + index).value = test.op_test_pres_id;
		if(billing == 'Y'){
			testAutoCompArray[index].containerCollapseEvent.fire();
		}
		index++;

	}
	if (index > 1)
		addRowToPresTestTable(false);

	index = 1;
	for (var i in allVisitServices) {
		var service = allVisitServices[i];
		if (service.service_name == null) {
			var len = servicesNotExists.length;
			servicesNotExists.length = len+1;
			servicesNotExists[len] = service;
			continue;
		}
		/*
		* if index == 1 no need to add row.
		* b'cause first row is added( and AC's initianlized in init method ) in jsp only.
		*/
		if (index != 1) addRowToPescServTable(false);
		document.getElementById('servicename' + index).value = service.service_name;
		document.getElementById('serviceremarks' + index).value = service.service_remarks;
		document.getElementById('noOfTimes' + index).value = 1;
		document.getElementById('serviceConsultId' + index).value = service.consultation_id;
		document.getElementById('servicePresId' + index).value = service.op_service_pres_id;
		if(billing == 'Y'){
			serviceAutoCompArray[index].containerCollapseEvent.fire();
		}
		index++;

	}
	if (index > 1)
		addRowToPescServTable(false);


	//for dietary
	index = 1;
	for (var i in allPrescribedMeals) {
		var meal = allPrescribedMeals[i];
		if (meal.meal_name == null) {
			var len = mealNotExists.length;
			mealNotExists.length = len+1;
			mealNotExists[len] = meal;
			continue;
		}
		/*
		* if index == 1 no need to add row.
		* b'cause first row is added( and AC's initianlized in init method ) in jsp only.
		*/
		if (index != 1) addRowToMealTable(false);

		var mealPrescDate = document.getElementById("mealdate"+index);
		var date = (new Date(meal.meal_date));
		var mealTiming = meal.meal_timing.split(" ")[0];
		//var dateToDisplay = date.getDate() +"-"+getFullMonth(date.getMonth())+"-"+date.getFullYear();
		document.getElementById('mealname' + index).value = meal.meal_name;
		document.getElementById('mealtiming' +index).value = mealTiming;

		if (mealTiming == 'Spl') {
			splMealTime = meal.meal_timing.split(" ")[1];
		}

		document.getElementById('mealQty' + index).value = 1;
		//mealPrescDate.value = dateToDisplay;
		mealPrescDate.value = formatDate(date, 'ddmmyyyy');
		document.getElementById('mealPresId' + index).value = meal.diet_pres_id;

		if(billing == 'Y'){
			mealAutoCompArray[index].containerCollapseEvent.fire();
		}
		getMealTimings(index);
		index++;
	}
	if (index > 1)
		addRowToMealTable(false);
}


function initPrescribedDoctorsAutoComplete(){
	var doctorsArray = [];

	var doctor = "prescribeddoctor";
	var doctorcontainer =  "prescribeddoctorcontainer";
	if(doctorlist != null){
		doctorsArray.length = doctorlist.length;
		for(var k =0;k<doctorlist.length;k++){
			doctorsArray[k] = doctorlist[k].DOCTOR_NAME;
		}
	}

	var dataSource = new YAHOO.widget.DS_JSArray(doctorsArray, { queryMatchContains : true } );
	oDocAutoComp = new YAHOO.widget.AutoComplete(doctor,doctorcontainer, dataSource);
	oDocAutoComp.formatResult = Insta.autoHighlight;

	oDocAutoComp.prehightlightClassName = "yui-ac-prehighlight";
	oDocAutoComp.useShadow = false;
	oDocAutoComp.allowBrowserAutocomplete = false;
	oDocAutoComp.minQueryLength = 0;
	oDocAutoComp.forceSelection = true;
	oDocAutoComp.itemSelectEvent.subscribe(nameInput);
}

var nameInput = function(sType, aArgs) {
	 var oData = aArgs[2];
	 document.getElementById('prescribeddoctor').value = oData;
}


var equipmentMapArray ;
function initEquipmentAutoComplete(len) {

	var EquipmentNamesArray = [];
	var equpname = "equipment" + len;
	var equp_container = "equipmentcontainer" + len;
	var serviceunits = "equipmentunits" +len;
	var equiprate = "equiprate" +len;
	var duration = "duration" +len;
	var equipamt = "equipamt" +len;
	var equipmentremarks = "equipmentremarks" + len;

	var equipHrduration = "equipHrduration" +len;

	YAHOO.util.Dom.get(equpname).value = "";
	if(equipments != null){
		equipmentMapArray = new Array(equipments.length);
		EquipmentNamesArray.length = equipments.length;
		for(var l = 0;l<equipments.length;l++){
			EquipmentNamesArray[l] = equipments[l].EQUIPMENT_NAME+'('+equipments[l].DEPT_NAME+')';
			equipmentMapArray[equipments[l].EQUIPMENT_NAME+'('+equipments[l].DEPT_NAME+')'] =  equipments[l].EQ_ID;
		}
	}
	this.oEqpSCDS = new YAHOO.widget.DS_JSArray(EquipmentNamesArray);
	oEqpAutoComp = new YAHOO.widget.AutoComplete(equpname,equp_container, this.oEqpSCDS);
	oEqpAutoComp.prehightlightClassName = "yui-ac-prehighlight";
	oEqpAutoComp.typeAhead = false;
	oEqpAutoComp.useShadow = false;
	oEqpAutoComp.allowBrowserAutocomplete = false;
	oEqpAutoComp.minQueryLength = 0;
	oEqpAutoComp.forceSelection = true;

	if(billing == 'Y'){
		oEqpAutoComp.containerCollapseEvent.subscribe(populateCharge);
	}
	function populateCharge(){
		var equipments = YAHOO.util.Dom.get(equpname).value;
	 	for(var j = 0;j<equipments.length;j++){
	 		if(equipments ==  EquipmentNamesArray[j]){
	 			resetAmounts(YAHOO.util.Dom.get(equiprate),YAHOO.util.Dom.get(duration),YAHOO.util.Dom.get(equipamt),parseFloat(0));
	 		}
	 	}
 	}
	oEqpAutoComp.textboxBlurEvent.subscribe(function(){
			if(YAHOO.util.Dom.get(equpname).value == ''){
				dataentered = false;
				YAHOO.util.Dom.get(serviceunits).checked = false;
				YAHOO.util.Dom.get(duration).value = 1;
				YAHOO.util.Dom.get(equipmentremarks).value = "";
				if(billing == 'Y'){
					YAHOO.util.Dom.get(equiprate).value = 0;
					YAHOO.util.Dom.get(equipamt).value = 0;
					resetAmounts(YAHOO.util.Dom.get(equiprate),YAHOO.util.Dom.get(duration),YAHOO.util.Dom.get(equipamt),parseFloat(0));
				}
			}
		});
}

function checkEquipmentRate(equpname,serviceunits,equiprate,duration,equipamt,equipHrduration){

	var equipmentname = YAHOO.util.Dom.get(equpname).value;
	for(var u = 0;u<equipments.length;u++){
		if(equipmentname.substring(0,equipmentname.indexOf("(")) == equipments[u].EQUIPMENT_NAME){
			if(!(YAHOO.util.Dom.get(serviceunits).checked)){
				YAHOO.util.Dom.get(serviceunits).value = 'D';
				YAHOO.util.Dom.get(equiprate).value = equipments[u].CHARGE;
				resetAmounts(YAHOO.util.Dom.get(equiprate),YAHOO.util.Dom.get(duration),YAHOO.util.Dom.get(equipamt),equipments[u].DAILY_CHARGE_DISCOUNT);
			}else{
				YAHOO.util.Dom.get(serviceunits).value = 'H';
				var mincharge = equipments[u].MIN_CHARGE;
				var incrcharge = equipments[u].INCR_CHARGE;
				var minduration = equipments[u].MIN_DURATION;
				var incrduration = equipments[u].INCR_DURATION;

				calculateEquipmentAmount(YAHOO.util.Dom.get(equiprate),YAHOO.util.Dom.get(duration),YAHOO.util.Dom.get(equipamt),mincharge,incrcharge,minduration,incrduration,equipments[u].MIN_CHARGE_DISCOUNT,equipments[u].INCR_CHARGE_DISCOUNT);
				//according to Murli sir for 9310 bug showing quantity as 1
				YAHOO.util.Dom.get(equipHrduration).value = YAHOO.util.Dom.get(duration).value;
				YAHOO.util.Dom.get(duration).value = '1';
			}
		}
	}
}

function calculateEquipmentAmount(equiprate,duration,equipamt,mincharge,incrcharge,minduration,incrduration,mindis,incrdis){
	var minNumofHrs = 0;
	var incrHrs = 0;
	var incrhrs = 0;
	var discount = 0;

	if(minduration == '') minduration = 0;
	if(mincharge == '') mincharge = 0;
	if(incrcharge == '') incrcharge = 0;
	if(incrduration == '') incrduration =0;


	if (document.getElementById(equiprate.id).value == "") {
		document.getElementById(equiprate.id).value = 0;
	}
	if (document.getElementById(duration.id).value == "") {
		document.getElementById(duration.id).value = 0;
	}

	var dur = document.getElementById(duration.id).value;

	if((minduration>0) && (parseFloat(dur,10) > parseFloat(minduration,10))) {
		minNumofHrs = parseFloat(minduration,10);
		incrHrs = eval (parseFloat(dur,10) - parseFloat(minNumofHrs,10));

		var incrhrs = 0;
		if(incrduration > 0){
			var incrhrs = incrHrs/incrduration;
			incr = incrHrs%incrduration;
			if(parseFloat(incr,10) > 0){
				incrhrs = parseFloat(incrhrs,10) +1;
			}
		}
		discount = parseFloat(mindis) + parseFloat(incrhrs)*parseFloat(incrdis);
	}else {
		minNumofHrs = parseFloat(minduration,10);
		discount = mindis;
	}

	if(numberCheck(document.getElementById(equiprate.id)) && numberCheck(document.getElementById(duration.id))){
		var rate = formatAmountObj(document.getElementById(equiprate.id));
		var qty = formatAmountObj(document.getElementById(duration.id));
		var amt = eval ((parseFloat(mincharge,10) + parseFloat((incrhrs * incrcharge),10)) - parseFloat(discount));
		//according to Murli sir for 9310 bug showing rate as calculated amount instead of master rate
		document.getElementById(equiprate.id).value = formatAmountValue(amt);//formatAmountValue(mincharge);
   		document.getElementById(equipamt.id).value = formatAmountValue(amt);
		resetTotals();
	}
}

var testAutoCompArray = new Array();
function initTestAutoComplete(len) {

		var testNamesArray = [];

		var test = "testName" + len;
		var test_dropdown = "testnamecontainer" + len;
		var testrate = "testrate" + len;
		var testqty = "testqty" + len;
		var testamt = "testamt" + len;
		var testremarks = "testremarks" + len;
		var discount = 0;
		YAHOO.util.Dom.get(test).value = "";
		if(deptWiseTestsjson != null){
			testNamesArray.length =  deptWiseTestsjson.length;
			for(var j = 0;j<deptWiseTestsjson.length;j++){
				testNamesArray[j] = deptWiseTestsjson[j].DIS_NAME;
			}
		}

		this.oTestNameSCDS = new YAHOO.widget.DS_JSArray(testNamesArray);
		this.oTestNameAutoComp = new YAHOO.widget.AutoComplete(test,test_dropdown, this.oTestNameSCDS);
		oTestNameAutoComp.maxResultsDisplayed = 20;
		oTestNameAutoComp.allowBrowserAutocomplete = false;
		oTestNameAutoComp.prehighlightClassName = "yui-ac-prehighlight";
		oTestNameAutoComp.typeAhead = false;
		oTestNameAutoComp.useShadow = false;
		oTestNameAutoComp.minQueryLength = 0;
		oTestNameAutoComp.animVert = false;
		if(billing == 'Y'){
			oTestNameAutoComp.containerCollapseEvent.subscribe(populateCharge);
		}
		function populateCharge(){
				var testName = YAHOO.util.Dom.get(test).value;
				for(var i = 0;i < deptWiseTestsjson.length;i++){
					if(testName == deptWiseTestsjson[i].DIS_NAME){
						var testcharge  = getTestCharge(deptWiseTestsjson[i].ID,deptWiseTestsjson[i].TYPE);
						eval("testcharge = " + testcharge);
						YAHOO.util.Dom.get(testrate).value = testcharge.charge;
						discount = testcharge.discount;
						resetAmounts(YAHOO.util.Dom.get(testrate),YAHOO.util.Dom.get(testqty),YAHOO.util.Dom.get(testamt),discount);
					}
				}
 		}
		oTestNameAutoComp.itemSelectEvent.subscribe(function(){
			dataentered = true;
		});
		testAutoCompArray[len] = oTestNameAutoComp;
}

function getTestCharge(id,type){
	var orgid = document.getElementById("orgid").value;
	var bedtype = document.getElementById("bedtype").value;
	var testid = testid;
	var priority = "";
	var url = cpath+'/visit/prescribe.do?method=getTestOrPackageCharge&orgid='+orgid+'&bedtype='+bedtype+'&testid='+id+'&priority='+priority+'&type='+type;
	var reqObject = newXMLHttpRequest();
	reqObject.open("POST",url.toString(), false);
	reqObject.send(null);
	if (reqObject.readyState == 4) {
		if ( (reqObject.status == 200) && (reqObject.responseText!=null) ) {
			return reqObject.responseText;
		}
	}
	return null;
}

function initOperationsAutoComplete(len) {
		var opeNamesArray = [];
		var operation = "opename" + len;
		var ope_dropdown = "openamecontainer" + len;
		var operate = "operate" + len;
		var opeqty = "opeqty" +len;
		var opeamt = "opeamt" +len;
		var operemarks = "operemarks" +len;
		var discount = 0;

		YAHOO.util.Dom.get(operation).value = "";
		if(operationslist != null){
			opeNamesArray.length = operationslist.length;
			for(var j = 0;j<operationslist.length;j++){
				opeNamesArray[j] =operationslist[j].OPERATION;
			}
		}
		this.oOpeNameSCDS = new YAHOO.widget.DS_JSArray(opeNamesArray);
		oOpeNameAutoComp = new YAHOO.widget.AutoComplete(operation,ope_dropdown,this.oOpeNameSCDS);

		oOpeNameAutoComp.maxResultsDisplayed = 20;
		oOpeNameAutoComp.allowBrowserAutocomplete = false;
		oOpeNameAutoComp.prehighlightClassName = "yui-ac-prehighlight";
		oOpeNameAutoComp.typeAhead = false;
		oOpeNameAutoComp.useShadow = false;
		oOpeNameAutoComp.minQueryLength = 0;
		oOpeNameAutoComp.forceSelection = true;
		oOpeNameAutoComp.animVert = false;
		if(billing == 'Y'){
			oOpeNameAutoComp.containerCollapseEvent.subscribe(populateCharge);
		}
		function populateCharge(){
 			var operations = YAHOO.util.Dom.get(operation).value;
	 			for(var j = 0;j<operationslist.length;j++){
	 				if(operations == operationslist[j].OPERATION){
	 					YAHOO.util.Dom.get(operate).value = parseFloat(operationslist[j].CHARGE)+parseFloat(operationslist[j].SURGEON_CHARGE)+parseFloat(operationslist[j].ANESTHETIST_CHARGE);
	 					discount = parseFloat(operationslist[j].SURG_ASST_DISCOUNT)+parseFloat(operationslist[j].SURG_DISCOUNT)+parseFloat(operationslist[j].ANEST_DISCOUNT);
	 					resetAmounts(YAHOO.util.Dom.get(operate),YAHOO.util.Dom.get(opeqty),YAHOO.util.Dom.get(opeamt),discount);
	 				}
	 			}
 		}
		oOpeNameAutoComp.itemSelectEvent.subscribe(function(){
			dataentered = true;

		});
}



function initMedicineAutoComplete(len) {
	/*var dataSource ;
	if(store != ''){
	dataSource = new YAHOO.widget.DS_JSArray(jMedicineNames[store]);
	}else if(store == ''){
	dataSource = new YAHOO.widget.DS_JSArray(jMedicineNames);
	}*/

	var medNamesArray = [];
	var medicine = "medicine" + len;
	var medicine_dropdown = "medicine_dropdown" + len;
	var medqty = "quantity" + len;
	var medrate = "medrate" + len;
	var medamt = "medamt" + len;
	var noofdays = "noofdays" + len;
	var medicinedosage = "medicinedosage" + len;
	var medremarks = "medremarks" + len;

	if(jMedicineNames != null){
		medNamesArray.length = jMedicineNames.length;
		for(var j = 0;j<jMedicineNames.length;j++){
			medNamesArray[j] =jMedicineNames[j].CHARGE_NAME;
		}
	}
	YAHOO.util.Dom.get(medicine).value = "";

	this.oMedNameSCDS = new YAHOO.widget.DS_JSArray(medNamesArray);
	medComp = new YAHOO.widget.AutoComplete(medicine, medicine_dropdown, this.oMedNameSCDS);
	medComp.maxResultsDisplayed = 20;
	medComp.allowBrowserAutocomplete = false;
	medComp.prehighlightClassName = "yui-ac-prehighlight";
	medComp.typeAhead = true;
	medComp.useShadow = false;
	medComp.autoHighlight = true;
	medComp.minQueryLength = 0;
	medComp.forceSelection = true;
	medComp.animVert = false;
	if(billing == 'Y'){
		medComp.containerCollapseEvent.subscribe(populateCharge);
	}
		function populateCharge(){
 			var medicines = YAHOO.util.Dom.get(medicine).value;
	 			for(var j = 0;j<jMedicineNames.length;j++){
	 				if(medicines == jMedicineNames[j].CHARGE_NAME){
	 					YAHOO.util.Dom.get(medrate).value = jMedicineNames[j].CHARGE;
	 					resetAmounts(YAHOO.util.Dom.get(medrate),YAHOO.util.Dom.get(medqty),YAHOO.util.Dom.get(medamt),parseFloat(0));
	 				}
	 			}
 		}
}
//Geting search details in Dash board
var dataentered;
var dayorhr = 'Days';
function changeDuration(checked){
	if(checked.checked){
		checked.value = 'H';
	}else{
	checked.value = 'D';
	}

}
function getSearchDetails(obj){
		document.forms[0].action = "../../pages/ipservices/Ipservices.do?method=getWardWisePatients&wardname="+obj+"&selected="+document.ipdashboardform.wards.selectedIndex;
		document.forms[0].submit();
}
function displayList(responseData){
		var tableObj = document.getElementById("dashboarddiv");
			for(var i=0; i<tableObj.childNodes.length; i++){
				tableObj.removeChild(tableObj.childNodes[i]);
			}
		var jsonExpression = "(" + responseData + ")";
		var list = eval(jsonExpression);
		var tdObj = "", trObj = "";
		for(var i =0;i<list.length;i++){
		}
}
function getSearchData(){
		if((document.getElementById("searchmrno").value != "") || (document.getElementById("firstName").value != "") || (document.getElementById("lastName").value != "") || (document.getElementById("searchdoctor").value != "") ){
			document.forms[0].action = "../../pages/ipservices/Ipservices.do?method=getSearchData&selectedConsultant="+document.ipdashboardform.searchdoctor.selectedIndex;
			document.forms[0].submit();
		}else{
			alert("Please Enter any criteria to search");
			return false;
		}
}
//Adds ward list to wards drop down from a Json string
function wardsList(){

			for (var i=0; i<wardNames.length; i++) {
			document.getElementById("wards").options[i+1] = new Option(wardNames[i].WARD_NAME,wardNames[i].WARD_NAME);
			document.forms[0].wards.options[i+1].text=wardNames[i].WARD_NAME;
			document.forms[0].wards.options[i+1].value=wardNames[i].WARD_NAME;
			}
			document.ipdashboardform.wards.selectedIndex=index;
}

function patientDetails(){
		var Mrno = document.forms[0].mrNo.value;
		if(window.XMLHttpRequest){
			req = new XMLHttpRequest();
		}
		else if(window.ActiveXObject){
			req = new ActiveXObject("MSXML2.XMLHTTP");
		}
		req.onreadystatechange = onResponse1;
		var url="Ipservices.do?method=getpatientDetails&Mrno="+Mrno;
		req.open("POST",url.toString(),true);
		req.setRequestHeader("Content-Type","text/xml");
		req.send(null);
}
function checkReadyState1(obj1){
	if(obj1.readyState == 4){
		if(obj1.status == 200){
			return true;
		}
	}
}

var tstr1=null;
var x1=null;
function onResponse1(){
	if(checkReadyState1(req)){
		tstr1=req.responseXML;
		if(tstr1!=null){
			getXML1();
		}
	}
}

    var doc1;
    var ar = null;
function getXML1(){
    doc1 = tstr1;
    x1=doc1.documentElement;
    var len1=x1.childNodes[0].childNodes.length;
	for(var n=0;n<len1;n++){
		document.getElementById("patName").value=x1.childNodes[0].childNodes[n].attributes.getNamedItem('class1').nodeValue;
		document.getElementById("mrNo").value=x1.childNodes[0].childNodes[n].attributes.getNamedItem('class5').nodeValue;
		document.getElementById("patientid").value=x1.childNodes[0].childNodes[n].attributes.getNamedItem('class4').nodeValue;
		document.getElementById("patientorg").value=x1.childNodes[0].childNodes[n].attributes.getNamedItem('class6').nodeValue;
		document.forms[0].doctor.options[1].value=x1.childNodes[0].childNodes[n].attributes.getNamedItem('class3').nodeValue;
		for(var i =0;i<doctorlist.length;i++){
		if(doctorlist[i].DOCTOR_ID==x1.childNodes[0].childNodes[n].attributes.getNamedItem('class3').nodeValue){
		document.forms[0].doctor.selectedIndex=i;
		}
		}
       }
}


function checkMrno(){
var flag = 0;
for(var i = 0; i<patientslist.length;i++){

        if(document.forms[0].mrNo.value == patientslist[i].MR_NO){
          flag = 1;
        }
        }
        if(flag==0){
          document.getElementById("mrNo").value="";
          document.getElementById("patName").value="";
          document.getElementById("patientid").value="";
          document.getElementById("ward").value="";
          document.getElementById("bednumber").value="";
          alert("Invalid MR no");
          return false;
        }else{
         patientDetails();
        }

}
function resetAll(){
}


function doctorAutoComplete(field,dropdown, list,len)
{
		YAHOO.example.ACJSAddArray = new function() {
			localDs = new YAHOO.util.LocalDataSource(list,{ queryMatchContains : true });
			localDs.responseType =  YAHOO.util.LocalDataSource.TYPE_JSON;
			localDs.responseSchema = {

				resultsList : "doctors",
				fields: [ {key : "DOCTOR_NAME"},
						  {key: "DOCTOR_ID"},
						  {key: "DEPT_NAME"}
						]
			};

			var autoComp = new YAHOO.widget.AutoComplete(field, dropdown, localDs);

			autoComp.prehightlightClassName = "yui-ac-prehighlight";
			autoComp.typeAhead = false;
			autoComp.useShadow = true;
			autoComp.allowBrowserAutocomplete = false;
			autoComp.minQueryLength = 0;
			autoComp.maxResultsDisplayed = 20;
			autoComp.autoHighlight = true;
			autoComp.forceSelection = true;
			autoComp.animVert = false;
			autoComp.useIFrame = true;
			autoComp.resultTypeList = false;
			var reArray = [];
			autoComp.formatResult = function(oResultData, sQuery, sResultMatch) {
				var escapedComp = Insta.escape(sQuery);
				reArray[0] = new RegExp(escapedComp, 'i');
		    	var det = highlight(oResultData.DOCTOR_NAME, reArray);
		    	det += "(" + oResultData.DEPT_NAME + ")";
		    	return det;
		    };

            var doctor_id;
			var itemSelectHandler = function(sType, aArgs) {
			        doctor_id=aArgs[2].DOCTOR_ID;
			        if (field == "prescribeddoctor") {
			        	document.getElementById("presdoctor").value =  aArgs[2].DOCTOR_ID;
			        } else {
			        	document.getElementById("doctorId"+len).value =  aArgs[2].DOCTOR_ID;
			        }
			};
			autoComp.itemSelectEvent.subscribe(itemSelectHandler);
			autoComp.containerCollapseEvent.subscribe(populateCharge);
			function populateCharge(){
				populateDoctorCharge(len);
			}
		 }
}



function populateDoctorCharge(len){
	var doctor = "doctor" + len;
	var docrate = "docrate" + len;
	var docqty = "docqty" + len;
	var docamt = "docamt" + len;
	var chargetype = "chargetype" + len;
	var visittime = "visittime" + len;

	var docdiscount = "docdiscount" + len;


	var doctors = YAHOO.util.Dom.get(doctor).value;

	if(doctors != ""){
		var doctor_id = document.getElementById("doctorId"+len).value;
		for(var j = 0;j<doctorlist.length;j++){
			if(doctor_id == doctorlist[j].DOCTOR_ID){
				var patType = document.getElementById("patientType").value;
				var visitingtime = YAHOO.util.Dom.get(visittime).value;
				var hr = visitingtime.substring(0,visitingtime.indexOf(":"));

				var chargeHead = document.getElementById(chargetype).value;
				var chargeRate = "";var docdiscountamt = "";

				if ( ("OPDOC" == chargeHead) ) { chargeRate =  doctorlist[j].DOCTOR_OP_CHARGE; docdiscountamt = doctorlist[j].OP_CHARGE_DISCOUNT; }
				else if ("IPDOC" == chargeHead)  { chargeRate =  doctorlist[j].DOCTOR_IP_CHARGE; docdiscountamt = doctorlist[j].DOCTOR_IP_CHARGE_DISCOUNT; }
				else if ("ROPDOC" == chargeHead) { chargeRate =  doctorlist[j].SUB_OP_CHARGE; docdiscountamt = doctorlist[j].OP_REVISIT_DISCOUNT; }
				else if ("NIPDOC" == chargeHead) { chargeRate =  doctorlist[j].NIGHT_IP_CHARGE; docdiscountamt = doctorlist[j].NIGHT_IP_CHARGE_DISCOUNT; }
				else if ("POPDOC" == chargeHead) { chargeRate =  doctorlist[j].PRIVATE_CONS_CHARGE; docdiscountamt = doctorlist[j].PRIVATE_CONS_DISCOUNT; }
				else if ("PRODOC" == chargeHead) { chargeRate =  doctorlist[j].PRIVATE_CONS_REVISIT_CHARGE; docdiscountamt = doctorlist[j].PRIVATE_REVISIT_DISCOUNT; }
				else if ("PAEDOC" == chargeHead) {
					if(patType == 'o') {chargeRate =  doctorlist[j].DOCTOR_OP_CHARGE; docdiscountamt = doctorlist[j].OP_CHARGE_DISCOUNT; }
					else if(patType == 'i') {chargeRate = doctorlist[j].DOCTOR_IP_CHARGE; docdiscountamt = doctorlist[j].DOCTOR_IP_CHARGE_DISCOUNT; }
				}
				else if ("DDODOC" == chargeHead) { chargeRate =  doctorlist[j].DOCTOR_OP_CHARGE; docdiscountamt = doctorlist[j].OP_CHARGE_DISCOUNT; }
				else if ("DDRDOC" == chargeHead) { chargeRate =  doctorlist[j].SUB_OP_CHARGE; docdiscountamt = doctorlist[j].OP_REVISIT_DISCOUNT; }
				else if ("SPODOC" == chargeHead) { chargeRate =  doctorlist[j].DOCTOR_OP_CHARGE; docdiscountamt = doctorlist[j].OP_CHARGE_DISCOUNT; }
				else if ("SPRDOC" == chargeHead) { chargeRate =  doctorlist[j].SUB_OP_CHARGE; docdiscountamt = doctorlist[j].OP_REVISIT_DISCOUNT; }

				YAHOO.util.Dom.get(docrate).value = chargeRate;
				YAHOO.util.Dom.get(docdiscount).value = docdiscountamt;
				resetAmounts(YAHOO.util.Dom.get(docrate),YAHOO.util.Dom.get(docqty),YAHOO.util.Dom.get(docamt),docdiscountamt);
			}
		}
		if(opRevisitSetting == 'R' && doctor_id != null) {
			var url = cpath+'/visit/prescribe.do?method=getRevisitCountOfDoctor&doctor='+doctor_id+'&mrno='+mrno;
			var ajaxobj = newXMLHttpRequest();
			ajaxobj.open("POST",url.toString(), false);
			ajaxobj.send(null);
		    if (ajaxobj) {
				if (ajaxobj.readyState == 4) {
					if ( (ajaxobj.status == 200) && (ajaxobj.responseText!=null) ) {
						eval("var revisitJSBean = " + ajaxobj.responseText);
						var visitedCount = 0;
						if(revisitJSBean != null) {
							if(revisitJSBean.allowed_revisit_count > 0) {
								if(revisitJSBean.patient_visit_count < (revisitJSBean.allowed_revisit_count+1)) {
									visitedCount = revisitJSBean.patient_visit_count;
								}else{
									visitedCount = revisitJSBean.patient_visit_count%(revisitJSBean.allowed_revisit_count+1);
								}
							}
							if(visitedCount > 0) {
								document.getElementById("previousVisitCount").innerHTML =
									"Previous visit on "+
									formatDate(new Date(revisitJSBean.visited_date), "ddmmyyyy", "-")+" <br/> with "+revisitJSBean.doctor_name+" for " +
									visitedCount + " consultation(s)";
							}else{
								document.getElementById("previousVisitCount").innerHTML = "";
							}
						}
					}else{
						document.getElementById("previousVisitCount").innerHTML = "";
					}
				} // end of ajaxobj.readyState
			} // end of ajaxobj
		} // end of opRevisitSetting == 'R'
	}else {
		YAHOO.util.Dom.get(doctor).value = "";
		YAHOO.util.Dom.get(docrate).value = 0;
		YAHOO.util.Dom.get(docdiscount).value = 0;
	}
}

function getresponsedoctorvisits(revisitJSBean) {
	if(revisitJSBean != null) {
		var visitedCount = 0;
		if(revisitJSBean.allowed_revisit_count > 0) {
			if(revisitJSBean.patient_visit_count < (revisitJSBean.allowed_revisit_count+1)) {
				visitedCount = revisitJSBean.patient_visit_count;
			}else{
				visitedCount = revisitJSBean.patient_visit_count%(revisitJSBean.allowed_revisit_count+1);
			}
		}
		if(visitedCount > 0) {
				"on "+
				formatDate(new Date(revisitJSBean.visited_date), "ddmmyyyy", "-")+" <br/> with "+revisitJSBean.doctor_name+" for " +
				visitedCount + " consultation(s)";
		}else{
			document.getElementById("previousVisitCount").innerHTML = "";
		}
	}else{
		document.getElementById("previousVisitCount").innerHTML = "";
	}
}


function setvalues(obj){
     var mrno =document.forms[0].mrNo.value;
     for(var i =0;i<patientsawaiting.length;i++){
     	if(mrno==patientsawaiting[i].MR_NO){
     	document.getElementById("patientid").value=patientsawaiting[i].PATIENT_ID;
     	document.getElementById("patientorg").value=patientsawaiting[i].PATIENT_ORG_ID;
     	document.getElementById("admitPatDeptId").value=patientsawaiting[i].DEPT_NAME;
     	YAHOO.util.Dom.get('admitbedtype').value = patientsawaiting[i].BED_TYPE;
     	YAHOO.util.Dom.get('admitward').value = patientsawaiting[i].WARD_NAME;
     	document.getElementById('admitward').focus();
     	populateBedNos();

     	}
     }
}

function validateonadmit(){
		if(document.getElementById("mrNo").value==""){
		alert("please select any mrno");
		return false;
		}
		if(document.getElementById("admitward").value==""){
		alert("please select any ward");
		return false;
		}
		if(document.getElementById("admitbednumber").value==""){
		alert("please select any bed");
		return false;
		}
		if(((document.forms[0].estimateddays.value).split('.')).length>2){
		alert("please enter proper estimated days");
		return false;
		}
		if(document.forms[0].estimateddays.value=="" || document.forms[0].estimateddays.value=='.'){
		alert("please enter estimated Days");
		return false;
		}
		if(!checkBillStatus("Admit",document.getElementById("mrNo").value,document.getElementById("patientid").value,"")){
		return false;
		}
		return true;
}
function admitPatient(){
        if(validateonadmit()){
		var mrno = document.getElementById("mrNo").value;
		var ward = document.getElementById("admitward").value;
		var bedno = document.getElementById("admitbednumber").value;
		var patid = document.getElementById("patientid").value;
		document.forms[0].action = "../../pages/ipservices/Ipservices.do?method=admitPatient&mrno="+mrno+"&ward="+ward+"&bed="+bedno+"&patid="+patid+"&org="+document.getElementById("patientorg").value;
		document.forms[0].submit();
		}

}
function doctorsList(obj){

     for(var i =0 ;i<doctorlist.length;i++){
    	document.getElementById("searchdoctor").options[i+1] = new Option(doctorlist[i].DOCTOR_NAME,doctorlist[i].DOCTOR_ID);

		document.forms[0].searchdoctor.options[i+1].text=doctorlist[i].DOCTOR_NAME;
		document.forms[0].searchdoctor.options[i+1].value=doctorlist[i].DOCTOR_ID;
     }
     document.ipdashboardform.searchdoctor.selectedIndex=selectedConsultantIndex;
}

function getWardsList(wardname){
		var ajaxreq = newXMLHttpRequest();
		var url = "<%=request.getContextPath()%>/pages/ipservices/Ipservices.do?method=getWardDetails&wardname="+wardname;
		getResponseHandlerText(ajaxreq, displaydetails, url);
}

function displaydetails(resposetext){
		var jsonExpression = "(" + resposetext + ")";
		var warddata = eval(jsonExpression);
		for(var i =0 ; i<warddata.length; i++){
		}

}
function prescribeScreen(){
	var mrno = document.getElementById("mrNo").value;
	document.forms[0].action = "../../pages/ipservices/Ipservices.do?method=getPatientInfo&mrno="+mrno;
	document.forms[0].submit();
}

	var noOfTestsDeleted = 0;
	var noofServicesDeleted = 0;
	var noofServicesDeleted = 0;
	var noofMealDeleted = 0;//for dietary
	var noOfEquipmentsDeleted = 0;
	var noOfMedicinesDeleted = 0;
	var noOfOtherServDeleted = 0;
	var noofVisitsDeleted = 0;
	var noofotherservsDeleted = 0;
	var noOfMediDeleted = 0;
	var noOfOperationsDeleted = 0;


function deleteRowFromDocotrVisitTable(){
	var doctorVisitcheckBoxes = document.forms[0].doctorCheckBox;
	var doctorTableObj = document.getElementById("doctorVisitTab");
	var flag = false;
	for(var checkedIndex=0;checkedIndex<doctorVisitcheckBoxes.length;checkedIndex++){
		if(doctorVisitcheckBoxes[checkedIndex].checked){
			doctorTableObj.deleteRow(checkedIndex+1);
			flag = true;
			checkedIndex = checkedIndex -1 ;
			noofVisitsDeleted++;
		}
	}
	if(flag == false){
		alert("please check the rows which you want to delete");
		return false;
	}
}
function addRowToOperationTable(){
	var operationtable = document.getElementById("prescribeopeTab");
	var len = operationtable.rows.length;
	if(YAHOO.util.Dom.get("opename"+(len-1)).value != ""){
	var tdObj="", trObj="";

	var opeRow = "opeRow" + len;

	trObj = operationtable.insertRow(len);
	trObj.id = opeRow;

	var opename = "opename" + len;
	var openameac  = "openameac" + len;
	var openamecontainer = "openamecontainer" + len;
	var operemarks = "operemarks" + len;
	var opeCheckBox = "opeCheckBox" + len;
	var operate = "operate" + len;
	var opeqty = "opeqty" + len;
	var opeamt = "opeamt" + len;
	var opeDeleteCharge = "opeDeleteCharge" +len;

	tdObj = trObj.insertCell(0);
	tdObj.innerHTML = '<img class="imgDelete" name="'+opeCheckBox+'" id="'+opeCheckBox+'" src="' + cpath + '/icons/Delete.png" onclick="cancel(this,'+opeDeleteCharge+','+opeRow+')" /> '+
				      '<input type="hidden" name="'+opeDeleteCharge+'" id="'+opeDeleteCharge+'" value="false"/>';

	tdObj = trObj.insertCell(1);
	tdObj.setAttribute("class", "yui-skin-sam");
	tdObj.setAttribute("valign", "top");
	tdObj.innerHTML = '<div id="'+ openameac +'" class="autocomplete" style="width:25em;" ><input id="'+ opename +'" name="'+ opename +'" type="text"  class="text-input"  style="width: 25em;"/><div id="'+ openamecontainer +'" class="scrolForContainer"></div></div>';

	tdObj = trObj.insertCell(2);
	tdObj.setAttribute("class", "yui-skin-sam");
	tdObj.setAttribute("valign", "top");
	tdObj.innerHTML = '<input type="text" name="'+operemarks+'" id="'+operemarks+'" onblur="document.forms[0].PrescribeMoreope.focus();"  class="text-input" maxlength="200" style="width: 15em"/>';

	if(billing == 'Y'){
		tdObj = trObj.insertCell(3);
		tdObj.setAttribute("valign", "top");
		tdObj.innerHTML = '<input type="text" name="'+ opeqty +'" id="'+ opeqty +'"  style="width: 5em;" value="1" readonly '+
						  '	onblur="return resetAmounts('+operate+','+opeqty+','+opeamt+');" onkeypress="return enterNumOnly(event);" />'+
						  '<input type="hidden" name="'+ operate +'" id="'+ operate +'"  style="width: 5em;" value="0" readonly />';


		tdObj = trObj.insertCell(4);
		tdObj.setAttribute("valign", "top");
		tdObj.innerHTML = '<input type="text" name="'+ opeamt +'" id="'+ opeamt +'"  style="width: 5em;" value="0" readonly />';
	}
	initOperationsAutoComplete(len);
	YAHOO.util.Dom.get(opename).focus();
	}
}

function addRowToPresTestTable(getFocus){

	if (getFocus == null || getFocus == undefined) {
		getFocus = true;
	}
	var testtable = document.getElementById("prescribeTestTable");
	var len = testtable.rows.length;
	if(YAHOO.util.Dom.get("testName"+(len-1)).value != ""){
		var tdObj="", trObj="";
		var testRow = "testRow" + len;
		trObj = testtable.insertRow(len);
		trObj.id = testRow;
		if(noOfTestsDeleted > 0){
			len = len + noOfTestsDeleted;
		}
		var testDept = "testDept" + len;
		var testName  = "testName" + len;
		var testDeptac = "testdeptac" + len;
		var testDeptContainer = "testdeptcontainer" + len;
		var testNameAc = "testnameac" + len;
		var testNamecontainer = "testnamecontainer" + len;
		var testremarks = "testremarks" + len;
		var testCheckBox = "testCheckBox" + len;
		var testrate = "testrate" + len;
		var testqty = "testqty" + len;
		var testamt = "testamt" + len;
		var testDeleteCharge = "testDeleteCharge" + len;
		var testConsultationId = "testConsultationId" + len;
		var testPresId = "testPresId" + len;

		tdObj = trObj.insertCell(0);
		tdObj.innerHTML = '<img class="imgDelete" name="'+testCheckBox+'" id="'+testCheckBox+'" src="' + cpath + '/icons/Delete.png" onclick="cancel(this,'+testDeleteCharge+','+testRow+')" /> '+
					      '<input type="hidden" name="'+testDeleteCharge+'" id="'+testDeleteCharge+'" value="false"/>' +
					      '<input type="hidden" name="'+ testConsultationId +'" id="'+ testConsultationId +'" value=""/>' +
					      '<input type="hidden" name="'+ testPresId +'" id="'+ testPresId +'" value=""/>' ;

		tdObj = trObj.insertCell(1);
		tdObj.setAttribute("class", "yui-skin-sam");
		tdObj.setAttribute("valign", "top");
		tdObj.innerHTML = '<div id="'+ testNameAc +'" class="autocomplete" style="width: 25em;" ><input id="'+ testName +'" name="'+ testName +'" type="text"  class="text-input"  style="width: 25em;"/><div id="'+ testNamecontainer +'" class="scrolForContainer"></div></div>';

		tdObj = trObj.insertCell(2);
		tdObj.setAttribute("class", "yui-skin-sam");
		tdObj.setAttribute("valign", "top");
		tdObj.innerHTML = '<input type="text" name="'+testremarks+'" id="'+testremarks+'" onblur="document.forms[0].PrescribeMoreTests.focus();"  class="text-input" maxlength="200" style="width: 15em;"/>';

		if(billing == 'Y'){
			tdObj = trObj.insertCell(3);
			tdObj.setAttribute("valign", "top");
			tdObj.innerHTML = '<input type="text" name="'+ testqty +'" id="'+ testqty +'"  style="width: 5em;" value="1" readonly '+
							  ' onblur="return resetAmounts('+testrate+','+testqty+','+testamt+');" onkeypress="return enterNumOnly(event);"/>'+
							  '<input type="hidden" name="'+ testrate +'" id="'+ testrate +'"  style="width: 5em;" value="0" readonly/>';

			tdObj = trObj.insertCell(4);
			tdObj.setAttribute("valign", "top");
			tdObj.innerHTML = '<input type="text" name="'+ testamt +'" id="'+ testamt +'"  style="width: 5em;" value="0" readonly />';
		}
		initTestAutoComplete(len);
		if (getFocus) {
			YAHOO.util.Dom.get(testName).focus();
		}

	}
}
function deleteRowFromPresTestTable(){
	var testCheckBoxes = document.forms[0].testCheckBox;
	var testsTableObj = document.getElementById("prescribeTestTable");
	var flag = false;
	for(var checkedIndex=0;checkedIndex<testCheckBoxes.length;checkedIndex++){
		if(testCheckBoxes[checkedIndex].checked){
			testsTableObj.deleteRow(checkedIndex+1);
			noOfTestsDeleted++;
			checkedIndex = checkedIndex -1 ;
			flag = true;
		}
	}
	if(flag == false){
		alert("please check the rows which you want to delete");
		return false;
	}
}

function addRowToPescServTable(getFocus){
	if (getFocus == null || getFocus == undefined) {
		getFocus = true;
	}
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
		var serviceConsultId = "serviceConsultId" + len;
		var servicePresId = "servicePresId" + len;
		var specializationId = "specialization" + len;

		tdObj = trObj.insertCell(0);
		tdObj.innerHTML = '<img class="imgDelete" name="'+serviceCheckBox+'" id="'+serviceCheckBox+'" src="' + cpath + '/icons/Delete.png" onclick="cancel(this,'+serviceDeleteCharge+','+serviceRow+')" /> '+
				      '<input type="hidden" name="'+serviceDeleteCharge+'" id="'+serviceDeleteCharge+'" value="false"/>' +
				      '<input type="hidden" name="'+ serviceConsultId +'" id="'+ serviceConsultId +'" value="">' +
				      '<input type="hidden" name="'+ servicePresId +'" id="'+ servicePresId +'">' +
				      '<input type="hidden" name="'+ specializationId +'" id="'+ specializationId +'">' ;

		tdObj = trObj.insertCell(1);
		tdObj.setAttribute("class", "yui-skin-sam");
		tdObj.setAttribute("valign", "top");
		tdObj.innerHTML = '<div id="'+ servicenameac +'" class="autocomplete" style="width: 25em;" ><input class="text-input" id="'+ txtservicename +'" name="'+ txtservicename +'" type="text"   style="width: 25em;" /><div id="'+ servicenamecontainer +'" class="scrolForContainer"></div></div>';

	       tdObj = trObj.insertCell(2);
	       tdObj.setAttribute("valign", "top");
		tdObj.innerHTML = '<input type="text" name="'+serviceremarks+'" id="'+serviceremarks+'" class="text-input" maxlength="200" style="width: 15em;"/>';

		if(billing == 'Y'){
			tdObj = trObj.insertCell(3);
			tdObj.setAttribute("valign", "top");
			tdObj.innerHTML = '<input type="text" class="text-input" value="1" readonly onchange="setValue(this,this.value);" name="'+ noOfTimes +'" ' +
					' id="'+ noOfTimes +'" onkeypress="return enterNumOnly(event);" style="width: 5em;"  onblur="document.forms[0].PrescribeMoreServices.focus();return resetAmounts('+servicerate+','+noOfTimes+','+serviceamt+');"  '+
					' onchange="return calcSerCharge(this)"/><input type="hidden" name="'+ serviceCharge +'" id="'+ serviceCharge +'"/>'+
					'<input type="hidden" name="'+ servicerate +'" id="'+ servicerate +'"  style="width: 5em;" value="0" readonly />';

			tdObj = trObj.insertCell(4);
			tdObj.setAttribute("valign", "top");
			tdObj.innerHTML = '<input type="text" name="'+ serviceamt +'" id="'+ serviceamt +'"  style="width: 5em;" value="0" readonly />';
		}else{
			tdObj = trObj.insertCell(3);
			tdObj.setAttribute("valign", "top");
			tdObj.innerHTML = '<input type="text" class="text-input" value="1" onchange="setValue(this,this.value);" name="'+ noOfTimes +'" ' +
					' id="'+ noOfTimes +'" onkeypress="return enterNumOnly(event);" style="width: 5em;" onblur="document.forms[0].PrescribeMoreServices.focus();" '+
					' onchange="return calcSerCharge(this)"/><input type="hidden" name="'+ serviceCharge +'" id="'+ serviceCharge +'"  />';
		}
		initServiceAutoComplete(len);
		if (getFocus) YAHOO.util.Dom.get(txtservicename).focus();
	}
}


// for dietry
function addRowToMealTable(getFocus){

	if (getFocus == null || getFocus == undefined) {
		getFocus = true;
	}
	var mealtab = document.getElementById("mealTab");
	var len = mealtab.rows.length;
	if(YAHOO.util.Dom.get("mealname"+(len-1)).value != "" && document.getElementById("mealtiming"+(len-1)).value != ""){
		var tdObj="", trObj="";
		var mealRow = "mealRow" +len;
		trObj = mealtab.insertRow(len);
		trObj.id = mealRow;
		if(noofMealDeleted > 0){
			len = len + noofMealDeleted;
		}
		var mealnameac = "mealnameac" + len;
		var txtmealname = "mealname" + len;
		var mealnamecontainer = "mealnamecontainer" + len;
		var mealdate = "mealdate"+len;
		var mealQty = "mealQty" + len;
		var mealCharge = "mealCharge" + len;
		var mealremarks = "mealremarks" + len;
		var mealCheckBox = "mealCheckBox" + len;
		var mealrate = "mealrate" + len;
		var mealamt = "mealamt" + len;
		var mealDeleteCharge = "mealDeleteCharge" + len;
		var mealConsultId = "mealConsultId" + len;
		var mealPresId = "mealPresId" + len;
		var mealTiming = "mealtiming" + len;
		var spltimediv = "SplTimeDiv" + len;
		var spltime = "spltime" + len;
		var curDate = (gServerNow != null) ? gServerNow : new Date();

		tdObj = trObj.insertCell(0);
		tdObj.innerHTML = '<img class="imgDelete" name="'+mealCheckBox+'" id="'+mealCheckBox+'"  src="' + cpath + '/icons/Delete.png" onclick="cancel(this,'+mealDeleteCharge+','+mealRow+')" /> '+
				      '<input type="hidden" name="'+mealDeleteCharge+'" id="'+mealDeleteCharge+'" value="false"/>' +
				      '<input type="hidden" name="'+ mealConsultId +'" id="'+ mealConsultId +'" value="">' +
				      '<input type="hidden" name="'+ mealPresId +'" id="'+ mealPresId +'">' ;

		tdObj = trObj.insertCell(1);
		tdObj.setAttribute("class", "yui-skin-sam");
		tdObj.setAttribute("valign", "top");
		tdObj.innerHTML = '<div id="'+ mealnameac +'" class="autocomplete" style="width: 25em;" ><input class="text-input" id="'+ txtmealname +'" name="'+ txtmealname +'" type="text"   style="width: 25em;" /><div id="'+ mealnamecontainer +'" class="scrolForContainer"></div></div>';

		tdObj = trObj.insertCell(2);
		tdObj.setAttribute("class", "yui-skin-sam");
		tdObj.setAttribute("valign", "top");
		tdObj.innerHTML =' <table><tr><td><select  class="text-input" style="width: 9em;" id="'+mealTiming+'" name="'+mealTiming+'" '+
						 ' onchange="getMealTimings('+len+');"> '+
						 ' <option value="">..Select..</option> '+
						 ' <option value="BF">BF</option> '+
						 ' <option value="Lunch">Lunch</option> '+
						 ' <option value="Dinner">Dinner</option> '+
						 ' <option value="Spl">Spl</option> '+
						 ' </select> ' +
						 '</td><td><div id="'+spltimediv+'" style="display: none"><input type="text" name="'+spltime+'" id="'+spltime+'" size="4" onblur="setTime(this)"/></div></td></tr></table>' ;

		startTimer(document.getElementById(spltime));

	    tdObj = trObj.insertCell(3);
	    tdObj.setAttribute("valign", "top");
		tdObj.innerHTML = '<input type="text" name="'+mealremarks+'" id="'+mealremarks+'" class="text-input" maxlength="200" style="width: 15em;"/>';

		tdObj = trObj.insertCell(4);
		tdObj.setAttribute("class", "yui-skin-sam");
		tdObj.setAttribute("valign","top");
		tdObj.innerHTML = getDateWidget (mealdate,mealdate,curDate);

		if(billing == 'Y'){
			tdObj = trObj.insertCell(5);
			tdObj.setAttribute("valign", "top");
			tdObj.innerHTML = '<input type="text" class="text-input" value="1" readonly onchange="setValue(this,this.value);" name="'+ mealQty +'" ' +
					' id="'+ mealQty +'" onkeypress="return enterNumOnly(event);" style="width: 5em;"  onblur="document.forms[0].PrescribeMoreServices.focus();return resetAmounts('+mealrate+','+mealQty+','+mealamt+');"  '+
					' onchange="return calcSerCharge(this)"/><input type="hidden" name="'+ mealCharge +'" id="'+ mealCharge +'"  />'+
					'<input type="hidden" name="'+ mealrate +'" id="'+ mealrate +'"  style="width: 5em;" value="0" readonly />';

			tdObj = trObj.insertCell(6);
			tdObj.setAttribute("valign", "top");
			tdObj.innerHTML = '<input type="text" name="'+ mealamt +'" id="'+ mealamt +'"  style="width: 5em;" value="0" readonly />';
		}else{
			tdObj = trObj.insertCell(5);
			tdObj.setAttribute("valign", "top");
			tdObj.innerHTML = '<input type="text" class="text-input" value="1" onchange="setValue(this,this.value);" name="'+ mealQty +'" ' +
					' id="'+ mealQty +'" onkeypress="return enterNumOnly(event);" style="width: 5em;" onblur="document.forms[0].PrescribeMoreServices.focus();" '+
					' onchange="return calcSerCharge(this)"/><input type="hidden" name="'+ mealCharge +'" id="'+ mealCharge +'"  />';
		}
		initMealAutoComplete(len);
		if (getFocus) YAHOO.util.Dom.get(txtmealname).focus();
		makePopupCalendar(mealdate, formatDate(curDate, 'ddmmyyyy'));

	}
	//getMealTimings(len);
}

function deleteRowFromPescServTable(){
		var serviceCheckBoxes = document.forms[0].serviceCheckBox;
		var servicesTableObj = document.getElementById("prescribeServiceTab");
		var flag = false;

		for(var checkedIndex=0;checkedIndex<serviceCheckBoxes.length;checkedIndex++){
			if(serviceCheckBoxes[checkedIndex].checked){
				servicesTableObj.deleteRow(checkedIndex+1);
				flag = true;
				checkedIndex = checkedIndex -1 ;
				noofServicesDeleted++;
			}
		}


//for dietary
		for(var checkedIndex=0;checkedIndex<serviceCheckBoxes.length;checkedIndex++){
			if(serviceCheckBoxes[checkedIndex].checked){
				servicesTableObj.deleteRow(checkedIndex+1);
				flag = true;
				checkedIndex = checkedIndex -1 ;
				noofServicesDeleted++;
			}
		}
		if(flag == false){
			alert("please check the rows which you want to delete");
			return false;
		}
	}

	function addRowToEquipmentTable(){
		var equipmenttable = document.getElementById("equipmentTab");
		var len = equipmenttable.rows.length;
		if(YAHOO.util.Dom.get("equipment"+(len-1)).value != ""){
		var tdObj="", trObj="";
		var equipRow = "equipRow" + len;
		trObj = equipmenttable.insertRow(len);
		trObj.id = equipRow;
		if(noOfEquipmentsDeleted > 0){
				len = len + noOfEquipmentsDeleted;
			}
		var eqdeptac = "eqdeptac" + len;
		var equipment = "equipment" + len;
		var equipmentac = "equipmentac" + len;
		var equipmentcontainer = "equipmentcontainer" + len;
		var duration = "duration" + len;
		var equipmentunits = "equipmentunits" + len;
		var equipmentremarks = "equipmentremarks" + len;
		var equipmentCheckBox = "equipmentCheckBox" + len;
		var equiprate = "equiprate" + len;
		var equipamt = "equipamt" + len;
		var equipDeleteCharge = "equipDeleteCharge" +len;

		var usedate = "usedate" + len;
		var usetime = "usetime" + len;
		var tilldate = "tilldate" + len;
		var tilltime = "tilltime" + len;

		var equipHrduration = "equipHrduration" +len;

		var curDate = (gServerNow != null) ? gServerNow : new Date();

		tdObj = trObj.insertCell(0);
		tdObj.innerHTML = '<img class="imgDelete" name="'+equipmentCheckBox+'" id="'+equipmentCheckBox+'" src="' + cpath + '/icons/Delete.png" onclick="cancel(this,'+equipDeleteCharge+','+equipRow+')" /> '+
				      '<input type="hidden" name="'+equipDeleteCharge+'" id="'+equipDeleteCharge+'" value="false"/>';

        tdObj = trObj.insertCell(1);
		tdObj.setAttribute("class", "yui-skin-sam");
		tdObj.setAttribute("valign", "top");
		tdObj.innerHTML = '<div id="'+ equipmentac +'" class="autocomplete" style="width: 20em;" ><input id="'+ equipment +'" name="'+ equipment +'" type="text" style="width: 20em;" class="text-input"  /><div id="'+ equipmentcontainer +'" class="scrolForContainer"></div></div>';

		tdObj = trObj.insertCell(2);
		tdObj.setAttribute("class", "yui-skin-sam");
		tdObj.setAttribute("valign", "top");
		tdObj.innerHTML = '<input  id="'+ equipmentunits +'" name="'+ equipmentunits +'" type="checkbox"  style="width:5em;" /> ';

		tdObj = trObj.insertCell(3);
		tdObj.setAttribute("class", "yui-skin-sam");
		tdObj.setAttribute("valign", "top");
		tdObj.innerHTML = '<input id="'+ equipmentremarks +'" name="'+ equipmentremarks +'" type="text" class="text-input" maxlength="200" style="width: 15em;"/>';

		tdObj = trObj.insertCell(4);
		tdObj.setAttribute("class", "yui-skin-sam");
		tdObj.setAttribute("valign", "top");
		tdObj.innerHTML = getDateWidget(usedate, usedate, curDate);

		tdObj = trObj.insertCell(5);
		tdObj.setAttribute("valign", "top");
		tdObj.innerHTML = '<input type="text" name="'+usetime+'" id="'+usetime+'" style="width: 6em;" maxlength="5" onblur="setTime(this)"/>';
		startTimer(document.getElementById(usetime));

		tdObj = trObj.insertCell(6);
		tdObj.setAttribute("class", "yui-skin-sam");
		tdObj.setAttribute("valign", "top");
		tdObj.innerHTML = getDateWidget(tilldate, tilldate, curDate);

		tdObj = trObj.insertCell(7);
		tdObj.setAttribute("valign", "top");
		tdObj.innerHTML = '<input type="text" name="'+tilltime+'" id="'+tilltime+'" style="width: 6em;" maxlength="5" onblur="setTime(this)"/>';
		startTimer(document.getElementById(tilltime));


		tdObj = trObj.insertCell(8);
		tdObj.setAttribute("class", "yui-skin-sam");
		tdObj.setAttribute("valign", "top");
		tdObj.innerHTML = '<input type="hidden" name="'+equipHrduration+'" id="'+equipHrduration+'"/>  <input id="'+ duration +'" name="'+ duration +'" class="text-input" style="width:5em;" type="text" readonly value="1" '+
			' onblur="document.forms[0].moreequipments.focus(); /> ';

		if(billing == 'Y'){
			tdObj = trObj.insertCell(9);
			tdObj.setAttribute("valign", "top");
			tdObj.innerHTML = '<input type="text" name="'+ equipamt +'" id="'+ equipamt +'"  style="width: 5em;" value="0" readonly />'+
			'<input type="hidden" name="'+ equiprate +'" id="'+ equiprate +'"  style="width: 5em;" value="0" readonly />';
		}

		initEquipmentAutoComplete(len);
		YAHOO.util.Dom.get(equipment).focus();
		}
		makePopupCalendar(usedate, formatDate(curDate, 'ddmmyyyy'));
		makePopupCalendar(tilldate, formatDate(curDate, 'ddmmyyyy'));
	}


	function addRowToPescMedTable(){
		var medicinetable = document.getElementById("medcinetab");
		var len = medicinetable.rows.length;
		if(YAHOO.util.Dom.get("medicine"+(len-1)).value != ""){
		var tdObj="", trObj="";
		var medRow = "medRow" + len;
		trObj = medicinetable.insertRow(len);
		trObj.id = medRow;
		if(noOfMedicinesDeleted > 0){
				len = len + noOfMedicinesDeleted;
			}
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
		var medrate = "medrate" + len;
		var medamt = "medamt" + len;
		var medDeleteCharge = "medDeleteCharge" + len;

		tdObj = trObj.insertCell(0);
		tdObj.innerHTML = '<img class="imgDelete" name="'+medCheckBox+'" id="'+medCheckBox+'" src="' + cpath + '/icons/Delete.png" onclick="cancel(this,'+medDeleteCharge+','+medRow+')" /> '+
				      '<input type="hidden" name="'+medDeleteCharge+'" id="'+medDeleteCharge+'" value="false"/>';

		tdObj = trObj.insertCell(1);
		tdObj.setAttribute("class", "yui-skin-sam");
		tdObj.setAttribute("valign", "top");
		tdObj.innerHTML = '<div id="'+mednameautocomplete+'" class="autocomplete" style="width: 15em;"><input class="text-input" name="'+medicine+'" style="width: 15em;" id="'+medicine+'" type="text"  > <div id="'+medicine_dropdown+'" class="scrolForContainer"></div></div>'

        tdObj = trObj.insertCell(2);
		tdObj.setAttribute("class", "yui-skin-sam");
		tdObj.setAttribute("valign", "top");
		tdObj.innerHTML = '<div id="'+autocompletedosage+'" class="autocomplete"  style="width: 5em;"><input class="text-input" name="'+medicinedosage+'" id="'+medicinedosage+'" style="width: 5em;" type="text"   /> <div id="'+meddosagecontainer+'" class="scrolForContainer"></div></div>'
        YAHOO.example.dosageArray1 = [];
        var h = 0;
        if(dosageList != null){
        for(var j = 0;j<dosageList.length;j++){
			YAHOO.example.dosageArray1.length = h+1;
			YAHOO.example.dosageArray1[h] = dosageList[j].DOSAGE;
			h++;
			}
		}
		YAHOO.example.ACJSArray = new function() {
		this.oACDS1 = new YAHOO.widget.DS_JSArray(YAHOO.example.dosageArray1);
		this.oAutoComp1 = new YAHOO.widget.AutoComplete(medicinedosage,meddosagecontainer, this.oACDS1);
		this.oAutoComp1.prehighlightClassName = "yui-ac-prehighlight";
		this.oAutoComp1.typeAhead = false;
		this.oAutoComp1.useShadow = false;
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

		tdObj = trObj.insertCell(3);
		tdObj.setAttribute("class", "yui-skin-sam");
		tdObj.setAttribute("valign", "top");
		tdObj.innerHTML = '<input id="'+ noofdays +'" class="text-input" name="'+ noofdays +'" '+
						  ' onblur="return calculateQuantity('+ noofdays +','+ quantity +','+medicinedosage+');" type="text" '+
						  ' style="width: 4.5em;" onkeypress="return enterNumOnly(event);" value="1" onchange="setValue(this,this.value);"/>';

		tdObj = trObj.insertCell(4);
		tdObj.setAttribute("class", "yui-skin-sam");
		tdObj.setAttribute("valign", "top");
		tdObj.innerHTML = '<input id="'+ medremarks +'" name="'+ medremarks +'" type="text" maxlength="200"  class="text-input" style="width:15em;"/> ';

		if(billing == 'Y'){
			tdObj = trObj.insertCell(5);
			tdObj.setAttribute("class", "yui-skin-sam");
			tdObj.setAttribute("valign", "top");
			tdObj.innerHTML = '<input class="text-input" id="'+ quantity +'" value="1" onchange="setValue(this,this.value);" '+
							  ' name="'+ quantity +'" type="text" style="width: 5em;" onkeypress="return enterNumOnly(event);" '+
							  ' onblur="document.forms[0].prescribeMedicine.focus();return resetAmounts('+medrate+','+quantity+','+medamt+');" />';

			tdObj = trObj.insertCell(6);
			tdObj.setAttribute("valign", "top");
			tdObj.innerHTML = '<input type="text" name="'+ medrate +'" id="'+ medrate +'"  style="width: 5em;" value="0" readonly />';

			tdObj = trObj.insertCell(7);
			tdObj.setAttribute("valign", "top");
			tdObj.innerHTML = '<input type="text" name="'+ medamt +'" id="'+ medamt +'"  style="width: 5em;" value="0" readonly />';
		}else{
			tdObj = trObj.insertCell(5);
			tdObj.setAttribute("class", "yui-skin-sam");
			tdObj.setAttribute("valign", "top");
			tdObj.innerHTML = '<input class="text-input" id="'+ quantity +'" value="1" onchange="setValue(this,this.value);"  onblur="document.forms[0].prescribeMedicine.focus();"'+
							  ' name="'+ quantity +'" type="text" style="width: 5em;" onkeypress="return enterNumOnly(event);" />';
		}
		initMedicineAutoComplete(len);
		YAHOO.util.Dom.get(medicine).focus();
		}
	}
	function deleteRowFromPescMedTable(){
		var medicineCheckBoxes = document.forms[0].medCheckBox;
		var medicineTable = document.getElementById("medcinetab");
		var flag = false;

		for(var checkedIndex=0;checkedIndex<medicineCheckBoxes.length;checkedIndex++){
			if(medicineCheckBoxes[checkedIndex].checked){
				medicineTable.deleteRow(checkedIndex+1);
				flag = true;
				checkedIndex = checkedIndex -1 ;
				noOfMedicinesDeleted++;
			}
		}
		if(flag == false){
			alert("please check the rows which you want to delete");
			return false;
		}
	}

	function addRowToOtherServiceTable(){
		var otherservtable = document.getElementById("otherservicesTab");
		var len = otherservtable.rows.length;
		if(YAHOO.util.Dom.get("otherservice"+(len-1)).value != ""){
		var tdObj="", trObj="";
		var otherserviceRow = "otherserviceRow" + len;
		trObj = otherservtable.insertRow(len);
		trObj.id = otherserviceRow;
		if(noOfOtherServDeleted > 0){
				len = len + noOfOtherServDeleted;
			}
		var otherserviceGroup = "otherserviceGroup" + len;
		var otherserviceac = "otherserviceac" + len;
		var otherservice = "otherservice" + len;
		var otherservicecontainer = "otherservicecontainer" + len;
		var otherserviceqty = "otherserviceqty" + len;
		var otherservicecharge = "otherservicecharge" + len;
		var otherserviceremarks = "otherserviceremarks" + len;
		var otherserviceCheckBox = "otherserviceCheckBox" + len;
		var otherserrate = "otherserrate" + len;
		var otherseramt = "otherseramt" + len;
		var otherserviceDeleteCharge = "otherserviceDeleteCharge" + len;

		tdObj = trObj.insertCell(0);
		tdObj.innerHTML = '<img class="imgDelete" name="'+otherserviceCheckBox+'" id="'+otherserviceCheckBox+'" src="' + cpath + '/icons/Delete.png" onclick="cancel(this,'+otherserviceDeleteCharge+','+otherserviceRow+')" /> '+
				      '<input type="hidden" name="'+otherserviceDeleteCharge+'" id="'+otherserviceDeleteCharge+'" value="false"/>';

		tdObj = trObj.insertCell(1);
		tdObj.setAttribute("class", "yui-skin-sam");
		tdObj.setAttribute("valign", "top");
		tdObj.innerHTML =' <select  class="text-input" style="width: 9em;" id="'+otherserviceGroup+'" name="'+otherserviceGroup+'" '+
						 ' onchange="getServices('+len+');"></select> ';

        tdObj = trObj.insertCell(2);
		tdObj.setAttribute("class", "yui-skin-sam");
		tdObj.setAttribute("valign", "top");
		tdObj.innerHTML = '<div id="'+otherserviceac+'"  style="width: 16em;"><input class="text-input" name="'+otherservice+'" id="'+otherservice+'" style="width: 16em;" type="text"   /> <div id="'+otherservicecontainer+'" class="scrolForContainer"></div></div>'

		tdObj = trObj.insertCell(3);
		tdObj.setAttribute("class", "yui-skin-sam");
		tdObj.setAttribute("valign", "top");
		tdObj.innerHTML = '<input id="'+ otherserviceremarks +'" name="'+ otherserviceremarks +'" type="text" style="width:15em;" class="text-input" maxlength="200" />';

		if(billing == 'Y'){
			tdObj = trObj.insertCell(4);
			tdObj.setAttribute("class", "yui-skin-sam");
			tdObj.setAttribute("valign", "top");
			tdObj.innerHTML = '<input class="text-input" id="'+ otherserviceqty +'" name="'+ otherserviceqty +'" type="text" '+
							  '	style="width:5em;" onkeypress="return enterNumOnly(event);" value="1" onchange="setValue(this,this.value);" '+
						  	  '	onblur="document.forms[0].moreotherservice.focus();return resetAmounts('+otherserrate+','+otherserviceqty+','+otherseramt+');" />'+
						  	  '<input type="hidden" name="'+ otherserrate +'" id="'+ otherserrate +'"  style="width: 5em;" value="0" readonly />';


			tdObj = trObj.insertCell(5);
			tdObj.setAttribute("valign", "top");
			tdObj.innerHTML = '<input type="text" name="'+ otherseramt +'" id="'+ otherseramt +'"  style="width: 5em;" value="0" readonly />';
		} else{
			tdObj = trObj.insertCell(4);
			tdObj.setAttribute("class", "yui-skin-sam");
			tdObj.setAttribute("valign", "top");
			tdObj.innerHTML = '<input class="text-input" id="'+ otherserviceqty +'" name="'+ otherserviceqty +'" type="text" onblur="document.forms[0].moreotherservice.focus();" '+
							  '	style="width:5em;" onkeypress="return enterNumOnly(event);" value="1" onchange="setValue(this,this.value);" /> ';
		}
		getOtherChargeheads(len);
		}
	}

	function deleteRowFromOtherServiceTable(){
		var otherserviceCheckBoxes = document.forms[0].otherserviceCheckBox;
		var otherservTable = document.getElementById("otherservicesTab");
		var flag = false;

		for(var checkedIndex=0;checkedIndex<otherserviceCheckBoxes.length;checkedIndex++){
			if(otherserviceCheckBoxes[checkedIndex].checked){
				otherservTable.deleteRow(checkedIndex+1);
				flag = true;
				checkedIndex = checkedIndex -1 ;
				noOfOtherServDeleted++;
			}
		}
		if(flag == false){
			alert("please check the rows which you want to delete");
			return false;
		}
	}


	function deleteRowFromEquipmentTable(){
		var equipmentCheckBoxes = document.forms[0].equipmentCheckBox;
		var equipmentTable = document.getElementById("equipmentTab");
		var flag = false;

		for(var checkedIndex=0;checkedIndex<equipmentCheckBoxes.length;checkedIndex++){
			if(equipmentCheckBoxes[checkedIndex].checked){
				equipmentTable.deleteRow(checkedIndex+1);
				flag = true;
				checkedIndex = checkedIndex -1 ;
				noofServicesDeleted++;
			}
		}
		if(flag == false){
			alert("please check the rows which you want to delete");
			return false;
		}
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
		}
		if ((parseInt(strHours) > 23) || (parseInt(strHours) < 0)) {
			alert("Incorrect hour : please enter 0-23 for hour");
			timeField.focus();
			return false;
		}
		if ((parseInt(strMinutes) > 59) || (parseInt(strMinutes) < 0)) {
			showMessage("js.common.message.time.range.minute");
			timeField.focus();
			return false;
		}
		if(strMinutes.length !=2){
			alert("incorrect minutes please enter 2 digit minuts");
			return false;
		}
	} else {
		alert("Incorrect time format : please enter HH:MI");
		timeField.focus();
		return false;
	}
	return true;
}
function IpValidateDate(visitdate,visittime){
       var date = document.getElementById(visitdate).value;
       var time = document.getElementById(visittime).value;
		if(date == ""){
				alert("Please enter visit date");
				document.getElementById(visitdate).focus();
				return false;
			}else{
		        if (!doValidateDateField(document.getElementById(visitdate))){
		                return false;
		        }
		        var msg = validateDateStr(date);
		        if (msg != null && msg!=""){
		               alert(msg);
		               return false;
		         }
			}

			if(time == ""){
				alert("Please enter visit time in HH:MI Format");
				document.getElementById(visittime).focus();
				return false;
			}else{
				if (!validateTime(document.getElementById(visittime))){
						return false;
					}
			}
			return true;
}

var status = 0;
function submitValues(obj){

    var print = 'N';
    if(document.getElementById(obj).value == 'Save'){
         print = 'N'; }
         else {
         print = 'Y'; }

	document.forms[0].save.disabled =true;
	if (showtests == 'Y') {
		if(document.forms[0].testmrno.value == ''){
			alert("Please enter Mrno");
			document.forms[0].save.disabled = false;
			return false;
		}
	}
	if (document.forms[0].bill.value == '') {
		alert("Please Select Bill No");
		document.forms[0].save.disabled = false;
		return false;
	}
	var testtablerows = document.getElementById("prescribeTestTable").rows.length;
	if (noOfTestsDeleted > 0) {
		testtablerows = testtablerows + noOfTestsDeleted;
	}
	var servicetablerows = document.getElementById("prescribeServiceTab").rows.length;
	if (noofServicesDeleted > 0) {
		servicetablerows = servicetablerows + noofServicesDeleted;
	}
	//for dietary
	if (dietaryModule == 'Y'){
		var mealtablerows = document.getElementById("mealTab").rows.length;
		if (noofServicesDeleted > 0) {
			mealtablerows = mealtablerows + noofServicesDeleted;
		}
	}
	var equipmenttablerows = document.getElementById("equipmentTab").rows.length;
	if (noOfEquipmentsDeleted > 0) {
		equipmenttablerows = equipmenttablerows + noofEquipmentsDeleted;
	}
	var visittablerows = document.getElementById("doctorVisitTab").rows.length;
	if (noofVisitsDeleted > 0) {
		equipmenttablerows = equipmenttablerows + noofVisitsDeleted;
	}
	var medicinetablerows = document.getElementById("medcinetab").rows.length;
	if (noOfMediDeleted > 0) {
		medicinetablerows = medicinetablerows + noOfMediDeleted;
	}
	var otherservtablerows = document.getElementById("otherservicesTab").rows.length;
	if (noOfOtherServDeleted > 0) {
		otherservtablerows = otherservtablerows + noOfOtherServDeleted;
	}
	if (ipservicesModule == 'Y') {
		var operationtablerows = document.getElementById("prescribeopeTab").rows.length;
		if (noOfOperationsDeleted > 0) {
			noOfOperationsDeleted = operationtablerows + noOfOperationsDeleted;
		}
	}

	if (!validateEquipmentQuantities(equipmenttablerows)) return false;
	if (!validateMealDateAndSpecialTimings (mealtablerows)) return false;

	var datestatus ;
	for (var c=1; c<visittablerows; c++) {
		var doctor = "doctor" + c;
		var visitdate = "visitdate" + c;
		var visittime = "visittime" + c;

		var doctorname = YAHOO.util.Dom.get(doctor).value;
		var doctorCheckBox = "doctorCheckBox" + c;
		var chargetype = "chargetype" + c;

		if (!IpValidateDate(visitdate,visittime)) {
			document.forms[0].save.disabled = false;
			return false;
		}
		if ((doctorname != "") && !(document.getElementById(doctorCheckBox).checked)) {
			if (document.getElementById(chargetype).value == "") {
				alert("select consultation");
				document.forms[0].save.disabled = false;
				return false;
			}
		}
	}

	if (!IpValidateDate('presdate','prestime')) {
		document.forms[0].save.disabled = false;
		return false;
	}

	for (var c=1; c<otherservtablerows; c++) {
		var otherserviceGroup = "otherserviceGroup" + c;
		var otherservice = "otherservice" + c;
		var otherserviceqty = "otherserviceqty" + c;

		if ( YAHOO.util.Dom.get(otherserviceGroup) != null) {
			if (YAHOO.util.Dom.get(otherserviceqty).value != ""){
				if (!isInteger(YAHOO.util.Dom.get(otherserviceqty).value))
					YAHOO.util.Dom.get(otherserviceqty).value=1;
			}
			var otherserviceGroupname = YAHOO.util.Dom.get(otherserviceGroup).value;
			var otherservicename = YAHOO.util.Dom.get(otherservice).value;

			if (YAHOO.util.Dom.get(otherservice).value != "") {
				if (YAHOO.util.Dom.get(otherserviceGroup).value == "") {
					alert("please select the Other Service");
					YAHOO.util.Dom.get(otherserviceGroup).focus();
					return false;
				}
			}
			if (otherserviceGroupname == "") {
				if (otherservicename == "") {
				}
				prescribedTests = true;
			}
		}
	}

	for (var c=0; c<medicinetablerows; c++){
		var medicine = "medicine" + c;
		var medicinedosage = "medicinedosage" + c;
		var quantity = "quantity" + c;
		var noofdays = "noofdays" + c;

		if ( YAHOO.util.Dom.get(medicine) != null ) {
			if (YAHOO.util.Dom.get(noofdays).value != "") {
				if (!isInteger(YAHOO.util.Dom.get(noofdays).value)) {
					YAHOO.util.Dom.get(noofdays).value = 1;
				}
			}
			if (YAHOO.util.Dom.get(quantity).value != "") {
				if (!isInteger(YAHOO.util.Dom.get(quantity).value)) {
					YAHOO.util.Dom.get(quantity).value = 1;
				}
			}
			var medicinename = YAHOO.util.Dom.get(medicine).value;
			var medicinedosagevalue = YAHOO.util.Dom.get(medicinedosage).value;
			if (medicinename != "") {
				if (medicinedosagevalue == "") {
					alert("please select the Medicine Dosage");
					YAHOO.util.Dom.get(medicine).focus();
					document.forms[0].save.disabled = false;
					return false;
				}
				prescribedTests = true;
			}
		}
	}

	for (var c=0; c<testtablerows; c++) {
		var testDept = "testDept" + c;
		var testName = "testName" + c;

		if ( YAHOO.util.Dom.get(testDept) != null) {
			var testDeptName = YAHOO.util.Dom.get(testDept).value;
			var testName = YAHOO.util.Dom.get(testName).value;

			if (testDeptName != "") {
				if (testName == "") {
					alert("please select the TestName");
					YAHOO.util.Dom.get(testName).focus();
					return false;
				}
				prescribedTests = true;
			}
		}
	}

	for (var c=0;c<servicetablerows;c++) {
		var servicename = "servicename" + c;
		var noOfTimes = "noOfTimes" + c;

		if ( YAHOO.util.Dom.get(servicename) != null) {
			if (YAHOO.util.Dom.get(noOfTimes).value != "") {
				if (!isInteger(YAHOO.util.Dom.get(noOfTimes).value)) {
					YAHOO.util.Dom.get(noOfTimes).value = 1;
				}
			}
		}
	}

// for dietary
	for (var c=0;c<mealtablerows;c++) {
		var mealname = "mealname" + c;
		var mealQty = "mealQty" + c;

		if ( YAHOO.util.Dom.get(mealname) != null) {
			if (YAHOO.util.Dom.get(mealQty).value != "") {
				if (!isInteger(YAHOO.util.Dom.get(mealQty).value)) {
					YAHOO.util.Dom.get(mealQty).value = 1;
				}
			}
		}
	}

	for (var c=0; c<equipmenttablerows; c++) {
		var equipment = "equipment" + c;
		var duration = "duration" + c;

		if ( YAHOO.util.Dom.get(equipment) != null) {
			if( YAHOO.util.Dom.get(duration).value != "") {
				if (!isInteger(YAHOO.util.Dom.get(duration).value)) {
					YAHOO.util.Dom.get(duration).value = 1;
				}
			}
			var equipmentname = YAHOO.util.Dom.get(equipment).value;
			if (equipment == "") {
				alert("please select the TestName");
				YAHOO.util.Dom.get(equipment).focus();
				return false;
			}
			prescribedTests = true;
		}
	}

	for (var c=0; c<visittablerows; c++) {
		var doctor = "doctor" + c;

		if ( YAHOO.util.Dom.get(doctorname) != null) {
			var doctorname = YAHOO.util.Dom.get(doctor).value;
			if (doctorname == "") {
				alert("please select the TestName");
				YAHOO.util.Dom.get(doctor).focus();
				return false;
			}
			prescribedTests = true;
		}
	}

	for (var j=0; j<otherservtablerows; j++) {
		var otherserviceGroup = "otherserviceGroup" + j;
		var otherservice = "otherservice" + j;
		var otherserviceqty = "otherserviceqty" + j;
		var otherservicecharge = "otherservicecharge" + j;
		var otherserviceremarks = "otherserviceremarks" + j;
		var otherserviceCheckBox = "otherserviceCheckBox" +j;
		var otherserviceDeleteCharge = "otherserviceDeleteCharge" + j;

		if(YAHOO.util.Dom.get(otherserviceGroup) != null){
			var otherserviceGroupName = YAHOO.util.Dom.get(otherserviceGroup).value;
			var otherserviceName = YAHOO.util.Dom.get(otherservice).value;
			var otherserviceqtyvalue =YAHOO.util.Dom.get(otherserviceqty).value;
			var qty = YAHOO.util.Dom.get(otherservice).value;
			var remarks = YAHOO.util.Dom.get(otherserviceremarks).value;

			if ((otherserviceGroupName != "") && (otherserviceName != "")
				&& (document.getElementById(otherserviceDeleteCharge).value == 'false' )) {

				innerOtherServiceHTML(otherserviceGroupName, otherserviceName, otherserviceqtyvalue, remarks);
			}
		}
	}

	for (o = 1; o<operationtablerows; o++) {
		var opename = "opename" + o;
		var operemarks = "operemarks" + o;
		var opeCheckBox = "opeCheckBox" + o;
		var opeDeleteCharge = "opeDeleteCharge" + o;
		var operationid = '',operatiodept ='';
		var operationsName = YAHOO.util.Dom.get(opename).value;

		if (YAHOO.util.Dom.get(opename) != null) {
			if (operationslist != null) {
				for (var op = 0;op<operationslist.length;op++) {
					if (operationsName == operationslist[op].OPERATION) {
						operatiodept = operationslist[op].DEPT_ID;
						operationid = operationslist[op].OP_ID;
						operationsName = operationslist[op].OPERATION_NAME;
					}
				}
				if (operationid != '' && operatiodept != ''
					&& (document.getElementById(opeDeleteCharge).value == 'false' )
					&& operationsName != '') {

					addOPerationsToInnerTable(operationid, operatiodept,
						YAHOO.util.Dom.get(operemarks).value,
						operationsName);
				}
			}
		}
	}

	var int =0;
	for (var j=1; j<testtablerows; j++) {
		var testName = "testName" + j;
		var testremarks = "testremarks" + j;
		var testCheckBox = "testCheckBox" + j;
		var testDeleteCharge = "testDeleteCharge" + j;
		var testConsultationId = "testConsultationId" + j;
		var testPresId = "testPresId" + j;
		var dDeptId = "";
		var testId = "";
		var conductionFlow="";
		var type = '';

		if (YAHOO.util.Dom.get(testName) != null) {
			if (deptWiseTestsjson != null) {
				var testName = YAHOO.util.Dom.get(testName).value;
				var catogery = '';
				for (var i = 0; i < deptWiseTestsjson.length; i++){
					if (testName == deptWiseTestsjson[i].DIS_NAME) {
						dDeptId = deptWiseTestsjson[i].DDEPT_ID;
						catogery = deptWiseTestsjson[i].CATEGORY;
						testId = deptWiseTestsjson[i].ID;
						conductionFlow=deptWiseTestsjson[i].CONDUCTION_APPLICABLE;
						type = deptWiseTestsjson[i].TYPE
					}
				}
				if ( (document.getElementById(testDeleteCharge).value == 'false') && (testName != '')
					&&  (testId == '')) {
					alert("Prescribed Test Name is not available in Hospital : "+ testName +".\n Please cancel it to proceed further.");
					clearAllInnerHtmlTables();
					return false;
				}
				var testremark = YAHOO.util.Dom.get(testremarks).value;
				var consultationId = YAHOO.util.Dom.get(testConsultationId).value;
				if ( (testId != "") && (testName != "") && (document.getElementById(testDeleteCharge).value == 'false' ) ) {
					var presId = document.getElementById(testPresId).value;
					innertTestHTML(dDeptId, testId, consultationId, testName, testremark, catogery, presId,type,conductionFlow);
				}
			}
		}
	}

	for (var j=0; j<medicinetablerows; j++) {
		var medicine = "medicine" + j;
		var medicinedosage = "medicinedosage" + j;
		var noofdays = "noofdays" + j;
		var quantity = "quantity" + j;
		var medremarks = "medremarks" + j;
		var medCheckBox = "medCheckBox" + j;
		var medDeleteCharge = "medDeleteCharge" + j;

		if (YAHOO.util.Dom.get(medicine) != null) {
			if ((YAHOO.util.Dom.get(medicine).value != "") && (YAHOO.util.Dom.get(medicinedosage).value != "")
				&& (YAHOO.util.Dom.get(noofdays).value != "") && (document.getElementById(medDeleteCharge).value == 'false' )) {

				innertMedHTML(YAHOO.util.Dom.get(medicine).value,
					YAHOO.util.Dom.get(medicinedosage).value,
					YAHOO.util.Dom.get(noofdays).value,
					YAHOO.util.Dom.get(quantity).value,
					YAHOO.util.Dom.get(medremarks).value);
			}
		}
	}

	for (var a=1; a<servicetablerows; a++) {
		var servicename = "servicename" + a;
		var  noOfTimes= "noOfTimes" + a;
		var serviceremarks = "serviceremarks" + a;
		var serviceCheckBox = "serviceCheckBox" + a;
		var serviceDeleteCharge = "serviceDeleteCharge" + a;
		var serviceConsultId = "serviceConsultId" + a;
		var servicePresId = "servicePresId" + a;
		var specializationId = "specialization"+a;
		var servicedeptName = "";

		if (YAHOO.util.Dom.get(servicename) != null) {
			if (serNameAndCharges != null) {
				var dDeptId = "";
				var servicetId = "";
				var servicename = YAHOO.util.Dom.get(servicename).value;
				servicetId = servicesMapArray[servicename];
				for (var k = 0; k<serNameAndCharges.length; k++) {
					if(servicetId == serNameAndCharges[k].SERVICE_ID){
						servicedeptName = serNameAndCharges[k].DEPT_NAME;
					}
				}
				if (!(document.getElementById(serviceDeleteCharge).value == 'false' )
					&& (servicename != "")
					&& (servicedeptName == "")
					&& (servicetId == undefined)) {

					alert("Prescribed Service is not available in the Hospital : " + servicename +".\n Please cancel it to proceed further.");
					clearAllInnerHtmlTables();
					return false;
				}
				var serviceremark = YAHOO.util.Dom.get(serviceremarks).value;
				noOfTimes = document.getElementById(noOfTimes).value;
				var consultationId = YAHOO.util.Dom.get(serviceConsultId).value;

				if ((servicedeptName != "")	&& (servicetId != "") && (servicename != "")
							&& (document.getElementById(serviceDeleteCharge).value == 'false')) {

					var presId = document.getElementById(servicePresId).value;
					var specilaze = document.getElementById(specializationId).value;
					innertServiceHTML(servicedeptName, servicetId, consultationId, servicename, noOfTimes,
						serviceremark, presId,specilaze);
				}
			}
		}
	}
	//for dietary
	for (var a=1; a<mealtablerows; a++) {
		var mealname = "mealname" + a;
		var mealdate = "mealdate" + a;
		var mealQty= "mealQty" + a;
		var mealremarks = "mealremarks" + a;
		var mealCheckBox = "mealCheckBox" + a;
		var mealPresId = "mealPresId" + a;
		var mealdelete = "mealDeleteCharge" + a;
		var mealTiming = "mealtiming" + a;
		var specialMealTime = "";
		if (document.getElementById(mealTiming).value == 'Spl') {
			specialMealTime = document.getElementById("spltime"+a).value;
		}

		var splMealTime = "";

		var mealId = "";

		if (YAHOO.util.Dom.get(mealname) != null) {
			if (mealNameAndCharges != null) {
				var dDeptId = "";
				var mealtId = "";
				var mealnames = YAHOO.util.Dom.get(mealname).value;
				mealtId = mealMapArray[mealnames];

				for (var k = 0; k<mealNameAndCharges.length; k++) {

					if(mealtId == mealNameAndCharges[k].MEAL_NAME){

						mealId = mealNameAndCharges[k].DIET_ID;
					}
				}

				if ( (document.getElementById(mealdelete).value == 'false' ) && (mealnames != "") && (mealtId == undefined)) {

					alert("Prescribed meal is not available in the Hospital : " + mealnames +".\n Please cancel it to proceed further.");
					clearAllInnerHtmlTables();
					return false;
				}
				var mealremark = YAHOO.util.Dom.get(mealremarks).value;
				var mealDate = YAHOO.util.Dom.get(mealdate).value;
				var mealDelete = YAHOO.util.Dom.get(mealdelete).value;
				var mTiming = YAHOO.util.Dom.get(mealTiming).value;
				mealQty = document.getElementById(mealQty).value;

				if ((mealId != "") && (mealname != "") && (document.getElementById(mealdelete).value == 'false' )) {
					var presId = document.getElementById(mealPresId).value;
					innertMealHTML(mealId, mealtId,  mealQty,mealremark, presId,mealDate,mealDelete,mTiming,specialMealTime);
				}
			}
		}
	}

	for (var c=0; c<equipmenttablerows; c++) {
		var equipment = "equipment" + c;
		var  duration = "duration" + c;
		var equipmentunits = "equipmentunits" + c;
		var equipmentremarks = "equipmentremarks" + c;
		var equipmentCheckBox = "equipmentCheckBox" + c;
		var equipDeleteCharge = "equipDeleteCharge" + c;
		var usedate = "usedate" + c;
		var usetime = "usetime" + c;
		var tilldate = "tilldate" + c;
		var tilltime = "tilltime" + c;
		var equipHrduration = "equipHrduration" + c;
		var eqCharge = "";
		var equipmentId = "";

		if (YAHOO.util.Dom.get(equipment) != null) {
			if (equipments != null) {
				var equipmentname = YAHOO.util.Dom.get(equipment).value;
				equipmentId = equipmentMapArray[equipmentname];
				var eqdept = "";
				var equipmentremark = YAHOO.util.Dom.get(equipmentremarks).value;
				for (var k = 0;k<equipments.length;k++) {
					if (equipmentId == equipments[k].EQ_ID) {
						eqdept = equipments[k].DEPT_ID;
					}
				}
				var eqduration = document.getElementById(duration).value;
				if (billing == 'Y') {
					if (document.getElementById(equipmentunits).value == 'D') {
					} else {
						eqduration = document.getElementById(equipHrduration).value;
					}
				}
				if ((equipmentname != "") && (equipmentId != "") && (document.getElementById(equipDeleteCharge).value == 'false' )) {
					innertEquipmentHTML(equipmentname, equipmentId, eqduration,
						document.getElementById(equipmentunits).value,
						eqdept, eqCharge, equipmentremark,
						document.getElementById(usedate).value,
						document.getElementById(usetime).value,
						document.getElementById(tilldate).value,
						document.getElementById(tilltime).value);
				}
			}
		}
	}

	if (doctorlist != null) {
		for (var k = 0;k<doctorlist.length;k++) {
			if (YAHOO.util.Dom.get(prescribeddoctor).value == doctorlist[k].DOCTOR_NAME) {
				document.forms[0].presdoctor.value = doctorlist[k].DOCTOR_ID;
			}
		}
	}

	for (var d=0; d<visittablerows; d++) {
		var doctor = "doctor" + d;
		var doctorId = "doctorId" + d;
		var visitdate = "visitdate" + d;
		var visittime = "visittime" + d;
		var doctorremarks = "doctorremarks" + d;
		var doctorCheckBox = "doctorCheckBox" + d;
		var docDeleteCharge = "docDeleteCharge" + d;
		var chargetype = "chargetype" + d;
		var docrate = "docrate" + d;
		var docdiscount = "docdiscount" +d;
		if (YAHOO.util.Dom.get(doctor) != null) {
			if (doctorlist != null) {
				var doctorname = YAHOO.util.Dom.get(doctor).value;
				var doctorId = YAHOO.util.Dom.get(doctorId).value;
				var visitingdate = YAHOO.util.Dom.get(visitdate).value;
				var visitingtime = YAHOO.util.Dom.get(visittime).value;
				var visitremarks = YAHOO.util.Dom.get(doctorremarks).value;

				if ((doctorname != "") && !(document.getElementById(docDeleteCharge).value == 'false' )) {
					innertVisitHTML(doctorname, visitingdate, visitingtime, visitremarks, doctorId,
						document.getElementById(chargetype).value,
						document.getElementById(docrate).value,
						document.getElementById(docdiscount).value);
				}
			}
		}
	}

	for (var i in servicesNotExists) {
		var service = servicesNotExists[i];
		innertServiceHTML('', '', service.consultation_id, service.service, '1', service.service_remarks,
			service.op_service_pres_id,service.specialization);
	}

	for (var i in testsNotExists) {
		var test = testsNotExists[i];
		innertTestHTML('', '', test.consultation_id, test.test, test.test_remarks, '', test.op_test_pres_id,'DIA','');
	}

	if(tpa_id != null && tpa_id != "" && status !=0){
		if(!validateInsAmount()) clearAllInnerHtmlTables();
	}
	if (status==1) {
		document.forms[0].action = cpath+"/visit/prescriptions.do?method=savePrescriptions&print="+print;
		document.forms[0].submit();
	}else if (status==2) {
		document.forms[0].save.disabled = false;
		return false;
	} else {
		alert("Please enter any thing to save");
		document.forms[0].save.disabled = false;
		return false;
	}
}

function validateInsAmount(){
	var total_credit_amount = document.getElementById("total_credit_amount").value;

	var total = getPaise(document.getElementById("totalAmt").value) + getPaise(document.getElementById("bill_amount").value);

	if(total > total_credit_amount){
		var ok = confirm("Bill Amount is greater than Existing Credits \n" +
						"Do you want to proceed?");
		if (!ok){
			status = 2;
			return false;
		}else{
			status  = 1;
			return true;
		}
	}else{
		status  = 1;
		return true;
	}
}
function innerOtherServiceHTML(otherserviceGroupName, otherserviceName, otherserviceqtyvalue, remarks) {
	status =1;
	var innerOtherTabObj = document.getElementById("innerOtherTab");
	var trObj = "", tdObj = "";
	trObj = innerOtherTabObj.insertRow(-1);

	tdObj = trObj.insertCell(-1);
	var el = makeHidden('otherserviceGroup', null, otherserviceGroupName);
	tdObj.appendChild(el);

	tdObj = trObj.insertCell(-1);
	el = makeHidden('otherservice', null, otherserviceName);
	tdObj.appendChild(el);

	tdObj = trObj.insertCell(-1);
	el = makeHidden('otherserviceqty', null, otherserviceqtyvalue);
	tdObj.appendChild(el);

	tdObj = trObj.insertCell(-1);
	el = makeHidden('otherserviceremarks', null, remarks);
	tdObj.appendChild(el);
}

function innertTestHTML(dDeptId, testId, consultationId, testName, testremark, catogery, testPresId,type,conductionFlow) {
	status =1;
	var innerOtherTabObj = document.getElementById("innerTestTab");
	var trObj = "", tdObj = "";
	trObj = innerOtherTabObj.insertRow(-1);

	tdObj = trObj.insertCell(-1);
	var el = makeHidden('htestDept', null, dDeptId);
	tdObj.appendChild(el);

	tdObj = trObj.insertCell(-1);
	el = makeHidden('htestId', null, testId);
	tdObj.appendChild(el);

	tdObj = trObj.insertCell(-1);
	el = makeHidden('htestconsultId', null, consultationId);
	tdObj.appendChild(el);

	tdObj = trObj.insertCell(-1);
	el = makeHidden('htestName', null, testName);
	tdObj.appendChild(el);

	tdObj = trObj.insertCell(-1);
	el = makeHidden('testremark', null, testremark);
	tdObj.appendChild(el);

	tdObj = trObj.insertCell(-1);
	el = makeHidden('testremark', null, testremark);
	tdObj.appendChild(el);

	tdObj = trObj.insertCell(-1);
	el = makeHidden('catogery', null, catogery);
	tdObj.appendChild(el);

	tdObj = trObj.insertCell(-1);
	el = makeHidden('htestpresId', null, testPresId);
	tdObj.appendChild(el);

	tdObj = trObj.insertCell(-1);
	el = makeHidden('test_pack', null, type);
	tdObj.appendChild(el);

	tdObj = trObj.insertCell(-1);
	el = makeHidden('hConductionFlowReq', null, conductionFlow);
	tdObj.appendChild(el);

}

function addOPerationsToInnerTable(operationid, operationdept, operemarks, opename) {
	status =1;
	var innerOtherTabObj = document.getElementById("innerOpeTab");
	var trObj = "", tdObj = "";
	trObj = innerOtherTabObj.insertRow(-1);

	tdObj = trObj.insertCell(-1);
	var el = makeHidden('operationid', null, operationid);
	tdObj.appendChild(el);

	tdObj = trObj.insertCell(-1);
	el = makeHidden('operationdeptid', null, operationdept);
	tdObj.appendChild(el);

	tdObj = trObj.insertCell(-1);
	el = makeHidden('operationremarks', null, operemarks);
	tdObj.appendChild(el);

	tdObj = trObj.insertCell(-1);
	el = makeHidden('operationname', null, opename);
	tdObj.appendChild(el);
}

function innertServiceHTML(servicedeptName, serviceId, consultationId, servicename, noOfTimes,
			serviceremark, servicePresId,specializationId) {
	status =1;
	var innerOtherTabObj = document.getElementById("innerServiceTab");
	var trObj = "", tdObj = "";
	trObj = innerOtherTabObj.insertRow(-1);

	tdObj = trObj.insertCell(-1);
	var el = makeHidden('servicedept', null, servicedeptName);
	tdObj.appendChild(el);

	tdObj = trObj.insertCell(-1);
	el = makeHidden('serviceid', null, serviceId);
	tdObj.appendChild(el);

	tdObj = trObj.insertCell(-1);
	el = makeHidden('hserviceConsultId', null, consultationId);
	tdObj.appendChild(el);

	tdObj = trObj.insertCell(-1);
	el = makeHidden('servicename', null, servicename);
	tdObj.appendChild(el);

	tdObj = trObj.insertCell(-1);
	el = makeHidden('noOfTimes', null, noOfTimes);
	tdObj.appendChild(el);

	tdObj = trObj.insertCell(-1);
	el = makeHidden('serviceremark', null, serviceremark);
	tdObj.appendChild(el);

	tdObj = trObj.insertCell(-1);
	el = makeHidden('hservicepresId', null, servicePresId);
	tdObj.appendChild(el);

	tdObj = trObj.insertCell(-1);
	el = makeHidden('specialization', null, specializationId);
	tdObj.appendChild(el);
}
//for dietry
function innertMealHTML(mealId, mealname, mealQty,
			mealremark, mealPresId,mealDate,mealDelete,mealTiming,specialMealTime) {
	status =1;
	var innerOtherTabObj = document.getElementById("innerMealTab");
	var trObj = "", tdObj = "";
	trObj = innerOtherTabObj.insertRow(-1);


	tdObj = trObj.insertCell(-1);
	el = makeHidden('mealid', null, mealId);
	tdObj.appendChild(el);

	tdObj = trObj.insertCell(-1);
	el = makeHidden('mealname', null, mealname);
	tdObj.appendChild(el);

	tdObj = trObj.insertCell(-1);
	el = makeHidden('mealQty', null, mealQty);
	tdObj.appendChild(el);

	tdObj = trObj.insertCell(-1);
	el = makeHidden('mealremark', null, mealremark);
	tdObj.appendChild(el);

	tdObj = trObj.insertCell(-1);
	el = makeHidden('mealdate', null, mealDate);
	tdObj.appendChild(el);

	tdObj = trObj.insertCell(-1);
	el = makeHidden('hmealpresId', null, mealPresId);
	tdObj.appendChild(el);

	tdObj = trObj.insertCell(-1);
	el = makeHidden('mealdelete', null, mealDelete);
	tdObj.appendChild(el);

	tdObj = trObj.insertCell(-1);
	el = makeHidden('mealtiming', null, mealTiming);
	tdObj.appendChild(el);

	tdObj = trObj.insertCell(-1);
	el = makeHidden('splMealTime', null, specialMealTime);
	tdObj.appendChild(el);

}


function innertEquipmentHTML(equipmentname, equipmentId, eqduration, equnit, eqdept, eqCharge,
			equipmentremark, usedate, usetime, tilldate, tilltime) {

	status =1;
	var innerOtherTabObj = document.getElementById("equipmentTab");
	var trObj = "", tdObj = "";
	trObj = innerOtherTabObj.insertRow(-1);

	tdObj = trObj.insertCell(-1);
	var el = makeHidden('equipmentname', null, equipmentname);
	tdObj.appendChild(el);

	tdObj = trObj.insertCell(-1);
	el = makeHidden('equipmentId', null, equipmentId);
	tdObj.appendChild(el);

	tdObj = trObj.insertCell(-1);
	el = makeHidden('equipmentduration', null, eqduration);
	tdObj.appendChild(el);

	tdObj = trObj.insertCell(-1);
	el = makeHidden('equipmentunit', null, equnit);
	tdObj.appendChild(el);

	tdObj = trObj.insertCell(-1);
	el = makeHidden('equipmentdepartment', null, eqdept);
	tdObj.appendChild(el);

	tdObj = trObj.insertCell(-1);
	el = makeHidden('unitcharge', null, eqCharge);
	tdObj.appendChild(el);

	tdObj = trObj.insertCell(-1);
	el = makeHidden('equipmentremark', null, equipmentremark);
	tdObj.appendChild(el);

	tdObj = trObj.insertCell(-1);
	el = makeHidden('usedate', null, usedate);
	tdObj.appendChild(el);

	tdObj = trObj.insertCell(-1);
	el = makeHidden('usetime', null, usetime);
	tdObj.appendChild(el);

	tdObj = trObj.insertCell(-1);
	el = makeHidden('tilldate', null, tilldate);
	tdObj.appendChild(el);

	tdObj = trObj.insertCell(-1);
	el = makeHidden('tilltime', null, tilltime);
	tdObj.appendChild(el);

}

function innertVisitHTML(doctorname, visitingdate, visitingtime, visitremarks, doctorId, chargetype, docrate, docdiscount) {
	var innerTestTabObj = document.getElementById("innervisitTab");
	var trObj = "", tdObj = "";
	trObj = innerTestTabObj.insertRow(-1);

	tdObj = trObj.insertCell(-1);
	var el = makeHidden('doctorname', 'doctorname', doctorname);
	tdObj.appendChild(el);

	tdObj = trObj.insertCell(-1);
	var el = makeHidden('doctorId', 'doctorId', doctorId);
	tdObj.appendChild(el);


	tdObj = trObj.insertCell(-1);
	el = makeHidden('visitingdate', 'visitingdate', visitingdate);
	tdObj.appendChild(el);

	tdObj = trObj.insertCell(-1);
	el = makeHidden('visitingtime', 'visitingtime', visitingtime);
	tdObj.appendChild(el);

	tdObj = trObj.insertCell(-1);
	el = makeHidden('visitremarks', 'visitremarks', visitremarks);
	tdObj.appendChild(el);


	tdObj = trObj.insertCell(-1);
	el = makeHidden('chargetype', 'chargetype', chargetype);
	tdObj.appendChild(el);

	tdObj = trObj.insertCell(-1);
	el = makeHidden('doctorConsultCharge', 'doctorConsultCharge', docrate);
	tdObj.appendChild(el);

	tdObj = trObj.insertCell(-1);
	el = makeHidden('doctorConsultDiscount', 'doctorConsultDiscount', docdiscount);
	tdObj.appendChild(el);

	status = 1;
}

function innertMedHTML(medicine, medicinedosage, noofdays, quantity, medremarks){
	status =1;
	var innerOtherTabObj = document.getElementById("innermedTab");
	var trObj = "", tdObj = "";
	trObj = innerOtherTabObj.insertRow(-1);

	tdObj = trObj.insertCell(-1);
	var el = makeHidden('medicine', null, medicine);
	tdObj.appendChild(el);

	tdObj = trObj.insertCell(-1);
	var el = makeHidden('medicinedosage', null, medicinedosage);
	tdObj.appendChild(el);

	tdObj = trObj.insertCell(-1);
	var el = makeHidden('mednoofdays', null, noofdays);
	tdObj.appendChild(el);

	tdObj = trObj.insertCell(-1);
	var el = makeHidden('medquantity', null, quantity);
	tdObj.appendChild(el);

	tdObj = trObj.insertCell(-1);
	var el = makeHidden('medremarks', null, medremarks);
	tdObj.appendChild(el);
}


/*
 * When the medicine name changes, go and fetch the other details like
 * manufacturer name, batch names available, and quantity.
 */

/*
 * Response handler for the ajax call to retrieve medicine details like mfr and batches
 */
var gAddManf;		// store the current manf in a global
var gStock;			// store the latest retrieved stock in a global

function handleMedicineStockResponse(responseText) {

	if (responseText==null) return;
	if (responseText=="") return;

    eval("gStock = " + responseText);
	if (gStock.length > 0) {
		var label = document.getElementById('manfName');
		if (label)
			label.innerHTML = gStock[0].manfName;
		gAddManf = gStock[0].manfName;

		var batchSel = document.salesform.batch;
		var index = 0;
		if (gStock.length > 1) {
			batchSel.disabled = false;
			batchSel.length = gStock.length + 1;
			batchSel.options[index].text = "..Select Batch..";
			batchSel.options[index].value = "";
			index++;
		} else {
			batchSel.length = 1;
			batchSel.disabled = true;
		}

		for (var i=0; i<gStock.length; i++){
			var item = gStock[i];
			batchSel.options[index].text = item.batchNo + " (" + item.qty + ") " + formatExpiry(item.expDt);
			batchSel.options[index].value = item.batchNo;
			index++;
		}

		// success: move to the next selectable item
		if (gStock.length > 1) {
			document.salesform.batch.focus();
		} else {
			document.salesform.addQty.focus();
		}

	} else {
		alert("Error retrieving stock details for medicine");
		resetMedicineDetails();
	}
}

var servicesMapArray = [];
var serviceAutoCompArray = new Array();
function initServiceAutoComplete(len){

		var serviceNamesArray = [];
		var service = "servicename" + len;
		var service_container = "servicenamecontainer" +len;
		var servicerate  = "servicerate" +len;
		var serviceqty = "noOfTimes" + len;
		var serviceamt = "serviceamt" + len;
		var serviceremarks = "serviceremarks" + len;
		var specialization = "specialization" + len;
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
		oServAutoComp.prehightlightClassName = "yui-ac-prehighlight";
		oServAutoComp.typeAhead = false;
		oServAutoComp.useShadow = false;
		oServAutoComp.allowBrowserAutocomplete = false;
		oServAutoComp.minQueryLength = 0;
		//oServAutoComp.forceSelection = true;

		oServAutoComp.containerCollapseEvent.subscribe(populateCharge);

		function populateCharge(){
			var services = YAHOO.util.Dom.get(service).value;
			for(var j = 0;j<serNameAndCharges.length;j++){
				if(services == serviceNamesArray[j]){
					if(billing == 'Y'){
						YAHOO.util.Dom.get(servicerate).value = serNameAndCharges[j].UNIT_CHARGE;
						resetAmounts(YAHOO.util.Dom.get(servicerate),YAHOO.util.Dom.get(serviceqty),YAHOO.util.Dom.get(serviceamt),serNameAndCharges[j].DISCOUNT);
					}
					YAHOO.util.Dom.get(specialization).value = serNameAndCharges[j].SPECIALIZATION;
				}
			}
		}
		serviceAutoCompArray[len] = oServAutoComp;
}
// for dietary
var mealMapArray = [];
var mealAutoCompArray = new Array();
function initMealAutoComplete(len){

		var mealNamesArray = [];
		var meal = "mealname" + len;
		var meal_container = "mealnamecontainer" +len;
		var mealrate  = "mealrate" +len;
		var mealqty = "mealQty" + len;
		var mealamt = "mealamt" + len;
		var mealremarks = "mealremarks" + len;
		YAHOO.util.Dom.get(meal).value = "";

		if(mealNameAndCharges != null){
		mealMapArray = new Array(mealNameAndCharges.length);
		mealNamesArray.length = mealNameAndCharges.length;
		for(var l = 0;l<mealNameAndCharges.length;l++){
			mealNamesArray[l] = mealNameAndCharges[l].MEAL_NAME;
			mealMapArray[mealNameAndCharges[l].MEAL_NAME] = mealNameAndCharges[l].MEAL_NAME;
			}
		}
		this.oServSCDS = new YAHOO.widget.DS_JSArray(mealNamesArray);
		MealAutoComp = new YAHOO.widget.AutoComplete(meal,meal_container, this.oServSCDS);
		MealAutoComp.prehightlightClassName = "yui-ac-prehighlight";
		MealAutoComp.typeAhead = false;
		MealAutoComp.useShadow = false;
		MealAutoComp.allowBrowserAutocomplete = false;
		MealAutoComp.minQueryLength = 0;
		MealAutoComp.forceSelection = true;
		MealAutoComp.itemSelectEvent.subscribe(function(sType,aArgs){
			var data = aArgs[2];
			for (var i=0;i<mealNameAndCharges.length;i++){
				if (data == mealNameAndCharges[i].MEAL_NAME){
					document.getElementById("mealid"+len).value = mealNameAndCharges[i].DIET_ID;
				}
			}
		});

		if(billing == 'Y'){
			MealAutoComp.containerCollapseEvent.subscribe(populateMealCharge);
		}
		function populateMealCharge(){
		var meals = YAHOO.util.Dom.get(meal).value;
			for(var j = 0;j<mealNameAndCharges.length;j++){
				if(meals == mealNamesArray[j]){
					YAHOO.util.Dom.get(mealrate).value = mealNameAndCharges[j].CHARGE;
					resetAmounts(YAHOO.util.Dom.get(mealrate),YAHOO.util.Dom.get(mealqty),YAHOO.util.Dom.get(mealamt),mealNameAndCharges[j].DISCOUNT);
				}
			}
		}
		mealAutoCompArray[len] = MealAutoComp;
}

function getServices(len){
	var group = "otherserviceGroup" +len;
	var service = "otherservice" +len;
	var container = "otherservicecontainer" +len;
	var qty = "otherserviceqty" +len;
	var ratefld = "otherserrate" +len;
	var amt = "otherseramt" + len;
	var remarks = "otherserviceremarks" + len;

	var serviceGroup = document.getElementById(group).value;
	if(serviceGroup=="OCOTC"){
	populateOtherCharges(service,container,othercharges,ratefld,qty,amt,remarks);
	}
	else if(serviceGroup=="CONOTC"){
	populateOtherCharges(service,container,consumables,ratefld,qty,amt,remarks);
	}else if(serviceGroup=="IMPOTC"){
	populateOtherCharges(service,container,implants,ratefld,qty,amt,remarks);
	}else if(serviceGroup==""){
		YAHOO.util.Dom.get(service).value = "";
		otherchargesarr.length = 0;
		//otherserviceAutoComp.forceSelection = false;
	}
	YAHOO.util.Dom.get(service).focus();
}


var otherchargesarr = [];
var otherserviceAutoComp;
var otherACDS;
function populateOtherCharges(service,container,list,ratefld,qty,amt,remarks){

		if(otherserviceAutoComp != undefined){
			YAHOO.util.Dom.get(qty).value = 1;
			if (billing == 'Y') {
				otherserviceAutoComp.destroy();
				YAHOO.util.Dom.get(ratefld).value = 0;
				YAHOO.util.Dom.get(amt).value = 0;
			}
		}
		dataentered = true;
		if(list != null){
			otherchargesarr.length = list.length;
			YAHOO.util.Dom.get(service).value = "";
			for(var j = 0;j<list.length;j++){
				otherchargesarr[j] = list[j].CHARGE_NAME;
			}
		}
		otherACDS = new YAHOO.widget.DS_JSArray(otherchargesarr);
		otherserviceAutoComp = new YAHOO.widget.AutoComplete(service,container, otherACDS);
		otherserviceAutoComp.prehighlightClassName = "yui-ac-prehighlight";
		otherserviceAutoComp.typeAhead = false;
		otherserviceAutoComp.useShadow = false;
		otherserviceAutoComp.minQueryLength = 0;
		otherserviceAutoComp.forceSelection = true;
		otherserviceAutoComp.allowBrowserAutocomplete = false;
		if(billing == 'Y'){
			otherserviceAutoComp.containerCollapseEvent.subscribe(populateCharge);
		}
		function populateCharge(){
			var services = YAHOO.util.Dom.get(service).value;
			for(var j = 0;j<list.length;j++){
				if(services == list[j].CHARGE_NAME){
					YAHOO.util.Dom.get(ratefld).value = list[j].CHARGE;
					resetAmounts(YAHOO.util.Dom.get(ratefld),YAHOO.util.Dom.get(qty),YAHOO.util.Dom.get(amt),parseFloat(0));
				}
			}
		}
}

function setdatetime(){
		document.forms[0].date.value = getCurrentDate();
		document.forms[0].time.value = getCurrentTime();
}
function setopedatetime(){
		document.forms[0].hdate.value = getCurrentDate();
		document.forms[0].htime.value = getCurrentTime();
}

function checkData(dataentered){
		var otherservtablerows = document.getElementById("otherservicesTab").rows.length;
		var medicinetablerows = document.getElementById("medcinetab").rows.length;
	    var visittablerows = document.getElementById("doctorVisitTab").rows.length;
	    var equipmenttablerows = document.getElementById("equipmentTab").rows.length;
	    var servicetablerows = document.getElementById("prescribeServiceTab").rows.length;
	   	var mealtablerows = document.getElementById("mealTab").rows.length; // for dietary
	    var testtablerows = document.getElementById("prescribeTestTable").rows.length;
	    var ot_dataentered,med_dataentered ;
		 for(var c=1;c<otherservtablerows;c++){
				var otherserviceGroup = "otherserviceGroup" + c;
				var otherservice = "otherservice" + c;
				var otherserviceCheckBox = "otherserviceCheckBox" + c;
				if( YAHOO.util.Dom.get(otherservice).value != ""){
						ot_dataentered = true;
				}else{
					ot_dataentered = false;
				}
			}
		for(var c=1;c<medicinetablerows;c++){
				var medicine = "medicine" + c;
				var medicinedosage = "medicinedosage" + c;

				if( YAHOO.util.Dom.get(medicine).value != ""){
					med_dataentered = true;
				}else{
					med_dataentered = false;
				}
			}
		for(var c=1;c<visittablerows;c++){
			var doctor = "doctor" + c;

			if( YAHOO.util.Dom.get(doctor).value != ""){
					doc_dataentered = true;
				}else{
					doc_dataentered = false;
				}
			}
		for(var c=1;c<equipmenttablerows;c++){
			var equipment = "equipment" + c;

			if( YAHOO.util.Dom.get(equipment) != ""){
						dataentered = true;
				}else{
					dataentered = false;
				}
			}
		for(var c=1;c<servicetablerows;c++){
			var servicedept = "servicedept" + c;
			var servicename = "servicename" + c;

			if( YAHOO.util.Dom.get(servicename).value != ""){
						dataentered = true;
				}else{
					dataentered = false;
				}
			}
//for dietary
		for(var c=1;c<mealtablerows;c++){
			var mealname = "mealname" + c;

			if( YAHOO.util.Dom.get(mealname).value != ""){
						dataentered = true;
				}else{
					dataentered = false;
				}
			}
		for(var c=1;c<servicetablerows;c++){
		    var testDept = "testDept" + c;
			var testName = "testName" + c;
				if( YAHOO.util.Dom.get(testName).value != ""){
						dataentered = true;
				}else{
					dataentered = false;
				}
			}
     return dataentered;
}
function setHref(obj,billno){

          dataentered = checkData(dataentered);
			if(dataentered){
				alert("Please save other wise you may loos the data");
				return false;
			}
		else {
		var href = obj.getAttribute("href");
		href = cpath+"/pages/ipservices/prescriptions.do?method=getOTServicesScreen&mrno="+mrno+"&patientid="+
		patientid+"&billno="+billno;
		obj.setAttribute("href",href);
		return true;
		}
}
function setDisHref(obj,d_mrno){
		var href = obj.getAttribute("href");
		href = cpath+"/pages/DischargeSummary/dischargesummary.do?operation=getDischarge&Mrno="+d_mrno+"&whichScreen=IPServices";
		obj.setAttribute("href",href);
		return true;
}
function setpatientslist(){
           for(var i =0;i<patientsawaiting.length;i++){
			document.getElementById("mrNo").options[i+1] = new Option(patientsawaiting[i].MR_NO,patientsawaiting[i].MR_NO);
			document.forms[0].mrNo.options[i+1].text= patientsawaiting[i].PATIENT_NAME+','+patientsawaiting[i].DOCTOR_NAME+','+patientsawaiting[i].MR_NO+','+patientsawaiting[i].PATIENT_ID;
			document.forms[0].mrNo.options[i+1].value= patientsawaiting[i].MR_NO;
			}

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

//var billstatus = 0;
function checkBillStatus(whichLink,mrno,visitid,link){
	/*var dischargeStatus = true;
	for(var i =0;i<opencreditbills.length;i++){
		if(visitid==opencreditbills[i].visit_id){
			billstatus = 1;
			if(opencreditbills[i].bill_no!=null){
			billno = opencreditbills[i].bill_no;
			}
		}
	}
	if(billstatus != 1){ */
	var ajaxobj = newXMLHttpRequest();
	var url = cpath+'/pages/ipservices/Ipservices.do?_method=ajaxCreditBillCheck&visitId='+visitid;
	var reqObject = newXMLHttpRequest();
	reqObject.open("POST",url.toString(), false);
	reqObject.send(null);
	if (reqObject.readyState == 4) {
		if ( (reqObject.status == 200) && (reqObject.responseText!=null) ) {
			if (reqObject.responseText != 'CreditBillExists') {
				if ((whichLink=='Bed Details' && link=='Allocate Bed') || whichLink=='OT' || whichLink=='Prescribe' || whichLink == 'Add Doctor Visit') {
					if (link == 'Discharge Summary' || link == 'OT Report' || link == 'Prescribe Diet') {
						return true;
					} else {
						alert("Patient does not have open credit bill");
						return false;
					}
				}
			}
		}
	}
	return true;
}

function setHrefForAdmission(obj,billno){
		var href = obj.getAttribute("href");
		href = "../../pages/ipservices/Ipservices.do?method=getIPDashBoard&amp;filterClosed=true&billno="+billno;
		obj.setAttribute("href",href);
		return true;
}

function setHrefForBedDetails(obj,billno){
		if(dataentered){
		alert("Please save other wise you may loos the data");
		return false;
		}else{
		var href = obj.getAttribute("href");
		var ajaxreqobjfortestnames = newXMLHttpRequest();
		var url = '../../pages/ipservices/Ipservices.do?method=getPatientDetailsForBill&patientid='+patientid+'&billno='+billno;
		href = url;
		obj.setAttribute("href",href);
		}
}
function setValue(obj,value){
   		if(value==""){
   			obj.value="1";
   		}
}

function getPage(anchor,offset){

	var offsetVal = offset*15;
	var href1 = anchor.getAttribute("href");
	var wardName = document.getElementById("wards").value;
 	var selectedDoctor = document.getElementById("searchdoctor").value;

	if(document.getElementById("wards").value != ""){
		href1 = href1+"method=getWardWisePatients&wardname="+wardName;
	}else if((document.getElementById("searchmrno").value != "") || (document.getElementById("firstName").value != "") || (document.getElementById("lastName").value != "") || (document.getElementById("searchdoctor").value != "") ){
		href1 = href1+"method=getSearchData&selectedDoctor="+selectedDoctor;
	}else{
		href1 = href1+"method=getIPDashBoard";
	}
	href1 = href1 + "&offsetval="+offsetVal+"&selectedPage="+offset;
	anchor.setAttribute("href", href1);
}
function getBabyRegistration(value,mrno,visitid){
	alert(value);
}
var selectedCheckBoxIndex ;
var selectedMrNo;
var selectedPatId;

function funDischarge(d_mrno,d_patId,d_doctorId,d_billstatusOk,d_paymentOk,index){
	var mesage = "Do you want to Discharge?";
	if (d_billstatusOk != 'true') {
  		alert("There are pending bills for this patient which need action. Cannot discharge.");
  		return false;
  	}else {
  		var ajaxobj = newXMLHttpRequest();
		var url = cpath+'/pages/ipservices/Ipservices.do?_method=ajaxPendingTestsCheck&visitId='+d_patId;
		var reqObject = newXMLHttpRequest();
		reqObject.open("POST",url.toString(), false);
		reqObject.send(null);
		if (reqObject.readyState == 4) {
			if ( (reqObject.status == 200) && (reqObject.responseText!=null) ) {
				if (reqObject.responseText == 'Pending') {
					mesage = "Some tests are pending for this visit,Still want to Discharge?";
				}
			}
		}
		if(!confirm(mesage))
	    	return false;
	}
	return true;
}

function changeDaysToHrs(checked){
	if(checked.checked){
		document.getElementById("dayhrth").innerHTML = 'Hrs';
		dayorhr = 'Hrs';
		}
	else{
	    document.getElementById("dayhrth").innerHTML = 'Days';
	    dayorhr = 'Days';
	    }
}
function setDateEntered(value){
		if(value == ''){
			dataentered = false;
		}
}
//function to change starting letter in to caps on enter key
function onKeyPressName(e) {
	if (isEventEnterOrTab(e)) {
		return capWords(document.forms[0].firstName);
	} else {
		return true;
	}
}
function getNextPage(startPage,endPage){
	var form  = document.ipdashboardform;
	form.startpage.value = parseInt(startPage) + 1 ;
	form.endpage.value = parseInt(endPage) + 10;
	form.pageNum.value = parseInt(endPage) + 1 ;
	document.ipdashboardform.submit();
}
function getPrevPage(startPage,endPage){
	var form  = document.ipdashboardform;
	form.startpage.value = parseInt(startPage) - 10;
	form.endpage.value = parseInt(form.startpage.value) + 9;
	form.pageNum.value = parseInt(startPage) - 10;
	document.ipdashboardform.submit();
}

function cancelEquipment(checkbox,deleteCharge,row){
	if(document.getElementById(deleteCharge.id).checked){
		document.getElementById(deleteCharge.id).value=true;
	}else{
		document.getElementById(deleteCharge.id).value=false;
	}
	if (document.getElementById(checkbox.id).checked) {
		document.getElementById(row.id).className = "deleted";
	} else {
		document.getElementById(row.id).className = "newRow";
	}
	resetTotals();
}

	function cancel(checkbox,deleteCharge,row){

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

	function resetAmounts(rate,qty,amt,discount){
		if (document.getElementById(rate.id).value == "") {
			document.getElementById(rate.id).value = 0;
		}
		if (document.getElementById(qty.id).value == "") {
			document.getElementById(qty.id).value = 0;
		}

		if(numberCheck(document.getElementById(rate.id)) && numberCheck(document.getElementById(qty.id))){
			var changedRate = formatAmountObj(document.getElementById(rate.id));
			var changedQty = formatAmountObj(document.getElementById(qty.id));
			var updatedAmt = eval(changedRate*changedQty)-parseFloat(discount) ;
		   	document.getElementById(amt.id).value = formatAmountValue(updatedAmt);
			resetTotals();
		   }
	}

	function resetTotals(){
	if(billing =='Y'){
		var tempTotAmt = 0;
		var docRows = document.getElementById("doctorVisitTab").rows.length-1;
		for (var i=1;i<=docRows;i++) {
			if ( (document.getElementById("docDeleteCharge"+i).value == 'false' )) {
				tempTotAmt = eval(parseFloat(tempTotAmt) + parseFloat(formatAmountObj(document.getElementById("docamt"+i))));
			}
		}
		var testRows = document.getElementById("prescribeTestTable").rows.length-1;
		for (var i=1;i<=testRows;i++) {
			if ( (document.getElementById("testDeleteCharge"+i).value == 'false' )) {
				tempTotAmt = eval(parseFloat(tempTotAmt) + parseFloat(formatAmountObj(document.getElementById("testamt"+i))));
			}
		}
		var serviceRows = document.getElementById("prescribeServiceTab").rows.length-1;
		for (var i=1;i<=serviceRows;i++) {
			if ( (document.getElementById("serviceDeleteCharge"+i).value == 'false' ) ) {
				tempTotAmt = eval(parseFloat(tempTotAmt) + parseFloat(formatAmountObj(document.getElementById("serviceamt"+i))));
			}
		}
//for dietry
		if (dietaryModule == 'Y'){
			var mealRows = document.getElementById("mealTab").rows.length-1;
			for (var i=1;i<=mealRows;i++) {
				if ( (document.getElementById("mealDeleteCharge"+i).value == 'false' ) ) {
					tempTotAmt = eval(parseFloat(tempTotAmt) + parseFloat(formatAmountObj(document.getElementById("mealamt"+i))));
				}
			}
		}
		var medRows = document.getElementById("medcinetab").rows.length-1;
		for (var i=1;i<=medRows;i++) {
			if ( (document.getElementById("medDeleteCharge"+i).value == 'false' )) {
				tempTotAmt = eval(parseFloat(tempTotAmt) + parseFloat(formatAmountObj(document.getElementById("medamt"+i))));
			}
		}
		var equipRows = document.getElementById("equipmentTab").rows.length-1;
		for (var i=1;i<=equipRows;i++) {
			if ( (document.getElementById("equipDeleteCharge"+i).value == 'false' )) {
				tempTotAmt = eval(parseFloat(tempTotAmt) + parseFloat(formatAmountObj(document.getElementById("equipamt"+i))));
			}
		}
		var otherSerRows = document.getElementById("otherservicesTab").rows.length-1;
		for (var i=1;i<=otherSerRows;i++) {
			if ( (document.getElementById("otherserviceDeleteCharge"+i).value == 'false' )) {
				tempTotAmt = eval(parseFloat(tempTotAmt) + parseFloat(formatAmountObj(document.getElementById("otherseramt"+i))));
			}
		}
		if(ipservicesModule == 'Y'){
			var opeRows = document.getElementById("prescribeopeTab").rows.length-1;
			for (var i=1;i<=opeRows;i++) {
				if ( (document.getElementById("opeDeleteCharge"+i).value == 'false' )) {
					tempTotAmt = eval(parseFloat(tempTotAmt) + parseFloat(formatAmountObj(document.getElementById("opeamt"+i))));
				}
			}
		}

		document.getElementById("totalAmt").value = formatAmountValue(tempTotAmt);
		tempTotAmt = 0;
	}
	}

function validMrno(){
}

function clearFields(){
}

function onKeyPressMrno(e) {
	if (isEventEnterOrTab(e)) {
		return onChangeMrno();
	} else {
		return true;
	}
}

function onChangeMrno() {
	var mrnoBox = document.forms[0].testmrno;
	var category = document.forms[0].category;
	// complete
	var valid = addPrefix(mrnoBox, gMrNoPrefix, gMrNoDigits);
	if (!valid) {
		alert("Invalid MR No. Format");
		mrnoBox.value = "";
		mrnoBox.focus();
		return false;
	}
	document.forms[0].action = "../../pages/ipservices/Ipservices.do?method=getPrescriptionScreen&mrno="+mrnoBox.value+"&category="+category.value;
	if (document.forms[0].category.value == "DEP_LAB") {
		document.forms[0].action = "../../pages/DiagnosticModule/prescribeLabTests.do?method=getPrescriptionScreen&mrno="+mrnoBox.value+"&category="+category.value;
	} else if (document.forms[0].category.value == "DEP_RAD") {
		document.forms[0].action = "../../pages/DiagnosticModule/prescribeRadiologyTests.do?method=getPrescriptionScreen&mrno="+mrnoBox.value+"&category="+category.value;
	}
	document.forms[0].patientid.value="";
	document.forms[0].submit();
}

function popup_mrsearch(){
	window.open(
    '../Common/PatientSearchPopup.do?title=Active%20Patients&mrnoForm=ipform&mrnoField=testmrno&searchType=active',
    'Search','width=655,height=430,scrollbars=yes,left=200,top=200');
    return false;
}//end of popup_mrsearch


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

//for dietary
function getMealTimings(len) {

	var mealtiming = "mealtiming" + len;
	var SplTimeDiv = "SplTimeDiv" + len;
	var splTime = "spltime" + len;
	var mealtime = document.getElementById(mealtiming).value;
	if (mealtime == 'Spl') {
		if (splMealTime == ''){
			startTimer(document.getElementById(splTime));
			document.getElementById(SplTimeDiv).style.display='block';
		}else {
			document.getElementById(splTime).value = splMealTime;
			document.getElementById(SplTimeDiv).style.display='block';
			splMealTime = '';
		}
	} else {

		document.getElementById(SplTimeDiv).style.display='none';
	}
}

//for dietary
function validateMealDateAndSpecialTimings (mealTableRows) {

	for (var m=1;m<mealTableRows;m++) {

		var mealdate = "mealdate" + m;
		var splTime = "spltime" + m;
		var mealname = "mealname" + m;
		var checkBox = "mealCheckBox" + m;
		var mealtiming = "mealtiming" + m;
		if ((YAHOO.util.Dom.get(mealname).value != "") && !(document.getElementById(checkBox).checked)) {

			if (document.getElementById(mealdate).value == ""){
				alert("Meal date is required");
				document.getElementById(mealdate).focus();
				document.forms[0].save.disabled = false;
				return false;
			}
			if (!doValidateDateField(document.getElementById(mealdate))) {
				document.forms[0].save.disabled = false;
	  			return false;
			}
			if (document.getElementById(mealtiming).value == "") {
				alert("Meal time is needed")
				document.getElementById(mealtiming).focus();
				document.forms[0].save.disabled = false;
				return false;
			}
			if (document.getElementById(mealtiming).value == 'Spl'){
				if (document.getElementById(splTime).value == "") {
					document.getElementById(splTime).focus();
					document.forms[0].save.disabled = false;
					return false;
				}
				if(!validateTime(document.getElementById(splTime))) {
					document.forms[0].save.disabled = false;
		  			return false;
				}
			}
		}

	}
	return true;
}

function calculateEquipmentQuantity(len){

 	var fromObj = document.getElementById("usedate" + len);
	var fromTimeObj = document.getElementById("usetime" + len);
	var toObj = document.getElementById("tilldate" + len);
	var tillTimeObj = document.getElementById("tilltime" + len);

	var duration = "duration" + len;
	var equipmentunits = "equipmentunits" + len;


	var fromDateTime = getDateTimeFromField(fromObj,fromTimeObj);
	var toDateTime = getDateTimeFromField(toObj,tillTimeObj);

	var millisecondsDiff = toDateTime.getTime() - fromDateTime.getTime();
	var noOfHrs = Math.round(millisecondsDiff / 60 / 60 / 1000);

	if(!document.getElementById(equipmentunits).checked){
		if(noOfHrs > 0 && noOfHrs <= 24){
			document.getElementById(duration).value = 1;
		}
		if(noOfHrs > 24){
			var diff = daysDiff(fromDateTime, toDateTime);
			if(noOfHrs%24 > 0){
				diff = diff +1;
			}
			document.getElementById(duration).value = Math.round(diff);
		}
	}else{
		document.getElementById(duration).value = noOfHrs;
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

function validateEquipmentDates(len){

	var equipmentunits = "equipmentunits" + len;

	var fromObj = document.getElementById("usedate" + len);
	var fromTimeObj = document.getElementById("usetime" + len);
	var toObj = document.getElementById("tilldate" + len);
	var tillTimeObj = document.getElementById("tilltime" + len);

	var regdate = document.ipform.patientRegDate;
	var regtime = document.ipform.patientRegTime;

	var regdt = getDateFromField(regdate);

	var regdttime = getDateTimeFromField(regdate,regtime);

	if (!doValidateDateField(fromObj))
		return false;
	if (!doValidateDateField(toObj))
		return false;

	if (fromObj.value == "")  {
		alert("From Date is required");
		return false;
	}
	if (toObj.value == "")  {
		alert("To Date is required");
		return false;
	}
	if (fromTimeObj.value == "")  {
		alert("From Time is required");
		return false;
	}
	if (tillTimeObj.value == "")  {
		alert("To Time is required");
		return false;
	}

	if(!validateTime(fromTimeObj)) return false;

	if(!validateTime(tillTimeObj)) return false;

	var fromDt = getDateFromField(fromObj);
	var toDt = getDateFromField(toObj);

	if (fromDt != null) {
		if (regdt.getTime()  > fromDt.getTime() ) {
			alert("From date cannot be less than Admit date");
			fromObj.focus();
			return false;
		}
	}

	if ( (toDt != null) && (fromDt != null) ) {
		if (fromDt.getTime() > toDt.getTime()) {
			alert("Till date cannot be less than From date");
			toObj.focus();
			return false;
		}
	}

	var fromDateTime = getDateTimeFromField(fromObj,fromTimeObj);
	var toDateTime = getDateTimeFromField(toObj,tillTimeObj);

	var lessregdatetime = daysDiff(regdttime, fromDateTime);

	if(lessregdatetime <  0){
		alert("From time cannot be less than Admit time");
		fromTimeObj.focus();
		return false;
	}

	var diff = daysDiff(fromDateTime, toDateTime);


	if(diff ==  0){
		alert("Till time cannot be less than From time");
		tillTimeObj.focus();
		return false;
	}

	if(diff <  0){
		alert("Till time cannot be less than From time");
		tillTimeObj.focus();
		return false;
	}

	return true;

}

// Get Date object from given date and time fields
function getDateTimeFromField(datefield,timefield){

	var darray=datefield.value.split("-");

	var d = parseInt(darray[0],10);
	var m = parseInt(darray[1],10);
	var y = parseInt(darray[2],10);

	var tarray=timefield.value.split(":");

	var h = parseInt(tarray[0],10);
	var i = parseInt(tarray[1],10);

	return new Date(y,m-1,d,h,i,0);

}
function calculateQuantities(){
  var equipmenttablerows = document.getElementById("equipmentTab").rows.length;
  if(noOfEquipmentsDeleted > 0){
     equipmenttablerows = equipmenttablerows + noofEquipmentsDeleted;
  }
  if(!validateEquipmentQuantities(equipmenttablerows))
  	return false;
  else
  	return true;
}
function validateEquipmentQuantities(equipmenttablerows){
	for(var e=1; e<equipmenttablerows; e++){

	var duration = "duration" + e;
	var equipmentunits = "equipmentunits" + e;

	var equiprate = "equiprate" + e;
	var equipamt = "equipamt" + e;

	var equipment = "equipment" + e;
	var equipmentCheckBox = "equipmentCheckBox" + e;

	var equipHrduration = "equipHrduration" + e;

	if((YAHOO.util.Dom.get(equipment).value != "") && !(document.getElementById(equipmentCheckBox).checked)){
	  	if(!validateEquipmentDates(e)) {
	  		document.forms[0].save.disabled = false;
	  		return false;
	  	}else{
	  		calculateEquipmentQuantity(e);
	  		if(billing == 'Y'){
	  			checkEquipmentRate(equipment,equipmentunits,equiprate,duration,equipamt,equipHrduration);
	  		}
	  	}
	}
  }
  return true;
}
 var reconductStatus=false;
function prevTestsValidation(name){

	 if(showtests == 'Y'){
		if(document.forms[0].testmrno.value == ''){
			alert("Please enter Mrno");
			document.forms[0].save.disabled = false;
			return false;
		}
	 }

	 if(document.forms[0].bill.value == ''){
		alert("Please Select Bill No");
		document.forms[0].save.disabled = false;
		return false;
	  }
	  if(doctorlist != null){
		for(var k = 0;k<doctorlist.length;k++){
			if(YAHOO.util.Dom.get(prescribeddoctor).value == doctorlist[k].DOCTOR_NAME){
				document.forms[0].presdoctor.value = doctorlist[k].DOCTOR_ID;
			}
		}
	}
	var testtablerows = document.getElementById("prescribeTestTable").rows.length;
	var int =0;
	for(var j=1; j<testtablerows; j++){
		var testName = "testName" + j;
		var testremarks = "testremarks" + j;
		var testCheckBox = "testCheckBox" + j;
		var testConsultationId = "testConsultationId" + j;
		var dDeptId = "";
		var testId = "";
		var conductionFlow="";
		var testPresId = "testPresId" + j;

		if(YAHOO.util.Dom.get(testName) == null){
		}else{
			if(deptWiseTestsjson != null){
				var testName = YAHOO.util.Dom.get(testName).value;
				var catogery = '';
				var type = '';
				for(var i = 0;i < deptWiseTestsjson.length;i++){
					if(testName == deptWiseTestsjson[i].DIS_NAME){
						dDeptId = deptWiseTestsjson[i].DDEPT_ID;
						catogery = deptWiseTestsjson[i].CATEGORY;
						testId = deptWiseTestsjson[i].ID;
						conductionFlow=deptWiseTestsjson[i].CONDUCTION_APPLICABLE;
						type = deptWiseTestsjson[i].TYPE;
					}
				}
				if (!(document.getElementById(testCheckBox).checked) && (testName != '') && (dDeptId == '') && (testId == '')) {
					alert("Prescribed Test Name is not available in Hospital : "+ testName +".\n Please cancel it to proceed further.");
					clearAllInnerHtmlTables();
					return false;
				}
				var testremark = YAHOO.util.Dom.get(testremarks).value;
				var consultationId = YAHOO.util.Dom.get(testConsultationId).value;
				if((testId != "") && (testName != "") && (document.getElementById(testCheckBox).value == 'false' ) ){
					var presId = document.getElementById(testPresId).value;
					innertTestHTML(dDeptId, testId, consultationId, testName, testremark, catogery, presId,type,conductionFlow);
					dataentered = true;
				}
			}
		}
	}

	for (var i in testsNotExists) {
		var test = testsNotExists[i];
		innertTestHTML('', '', test.consultation_id, test.test, test.test_remarks, '', test.op_test_pres_id,type,'');
	}

   if(!reconductStatus && !dataentered){
	   alert("Select tests to prescribe or reconduct");
	   return false;
   }
   var print = name == 'SavePrint'?'Y':'N';
   var category = document.forms[0].category;
   document.forms[0].action = cpath+"/pages/ipservices/Ipservices.do?_method=saveReconductionDetails&recoduction="+reconductStatus+"&category="+category.value+"&print="+print;
   document.forms[0].submit();
}

function changeReConduction(index,value){
	var varible = "isWithNewSample"+index;
	reconductStatus=true;
	document.getElementById(varible).value = value;
}

function toggleConduction(index,value){
	var checkBoxObj = document.getElementById("sample"+index);
	reconductStatus=true;
	if(checkBoxObj.checked){

		document.getElementById("isWithNewSample"+index).value = 'NO SAMPLE';
	}else{
		document.getElementById("isWithNewSample"+index).value = 'NO CHANGE';
	}
}

function clearAllInnerHtmlTables() {
	var innerServiceTab = document.getElementById("innerServiceTab");
	var innerMealTab = document.getElementById("innerMealTab");//for dietry
	var innerTestTab = document.getElementById("innerTestTab");
	var innervisitTab = document.getElementById("innervisitTab");
	var innermedTab = document.getElementById("innermedTab");
	var innerOtherTab = document.getElementById("innerOtherTab");
	var innerOpeTab = document.getElementById("innerOpeTab");
	var len = innerServiceTab.rows.length
	for (var i=0; i<len; i++) {
		innerServiceTab.deleteRow(-1);
	}

	len = innerTestTab.rows.length;
	for (var i=0; i<len; i++) {
		innerTestTab.deleteRow(-1);
	}
	len = innervisitTab.rows.length;
	for (var i=0; i<len; i++) {
		innervisitTab.deleteRow(-1);
	}

	len = innermedTab.rows.length;
	for (var i=0; i<len; i++) {
		innermedTab.deleteRow(-1);
	}

	len = innerOtherTab.rows.length;
	for (var i=0; i<len; i++) {
		innerOtherTab.deleteRow(-1);
	}

	len = innerOpeTab.rows.length;
	for (var i=0; i<len; i++) {
		innerOpeTab.deleteRow(-1);
	}

	//for dietry
	len = innerMealTab.rows.length;
	for (var i=0; i<len; i++) {
		innerMealTab.deleteRow(-1);
	}
	document.forms[0].save.disabled = false;
}
