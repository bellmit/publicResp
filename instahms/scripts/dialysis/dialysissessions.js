var toolbar = {};
	toolbar.Edit= {
		title: toolbarOptions["editprescriptions"]["name"],
		imageSrc: "icons/Edit.png",
		href: 'dialysis/DialysisCurrentSessions.do?_method=getSessionsScreen',
		onclick: null,
		description: toolbarOptions["editprescriptions"]["description"]
	};
function init(){
	createToolbar(toolbar);
}
function funValidateDetails(){
	var form  = document.preDialysis;
	var est_duration = form.estDuration.value;
	var completionStatus = document.getElementById('completion_status');

	var status = form.status.value;
	var singleUseDialyzer = form.singleUseDialyzer;
	var count = form.dialyzer_repr_count.value;
	
	
	if (singleUseDialyzer.checked) {
		document.getElementById('single_use_dialyzer').value = 'Y';
		if (count>1) {
			showMessage("js.dialysismodule.commonvalidations.dialyzer.singleuse");
			return false;
		}
	} else if(!singleUseDialyzer.checked) {
		document.getElementById('single_use_dialyzer').value = 'N';
	}

	if (status == 'C' && (completionStatus != undefined && completionStatus.value == '')) {
		showMessage("js.dialysismodule.commonvalidations.completionstatus");
		document.getElementById('completion_status').focus();
		return false;
	}

	if(status != "O" && status != 'C'){

		var location = form.location_id.value;
		if(location == ""){
			showMessage("js.dialysismodule.commonvalidations.select.location");
			form.location_id.focus();
			return false;
		}

		var machine_id = form.machine_id.value;
		if(machine_id == ""){
			showMessage("js.dialysismodule.commonvalidations.select.machine");
			form.machine_id.focus();
			return false;
		}

		var access_type_id = form.access_type_id.value;
		if(access_type_id == ""){
			showMessage("js.dialysismodule.commonvalidations.select.accesstype");
			form.access_type_id.focus();
			return false;
		}

		var needle_type = form.needle_type.value;
		if(needle_type == ""){
			showMessage("js.dialysismodule.commonvalidations.select.needletype");
			form.needle_type.focus();
			return false;
		}

		if (form.access_patency.value == 'P') {
			var bruit_thril = document.getElementById('patency_bruit_thrill').options[document.getElementById('patency_bruit_thrill').selectedIndex].value;
			if(bruit_thril == '') {
				showMessage("js.dialysismodule.commonvalidations.select.bruit.thrill");
				document.getElementById('patency_bruit_thrill').focus();
				return false;
			}
		}

		var access_site_id = form.access_site_id.value;
		if(access_site_id == ""){
			showMessage("js.dialysismodule.commonvalidations.select.accesssitetype");
			form.access_site_id.focus();
			return false;
		}

		var start_attendant = form.start_attendant.value;
		if(start_attendant == ""){
			showMessage("js.dialysismodule.commonvalidations.select.primarydialysistherapist");
			form.start_attendant.focus();
			return false;
		}

       var physician = form.physician.value;
       if(physician == ""){
            showMessage("js.dialysismodule.commonvalidations.select.physician");
            form.physician.focus();
            return false;
        }
		var dialysate_type_id = form.dialysate_type_id.value;
		if(dialysate_type_id == ""){
			showMessage("js.dialysismodule.commonvalidations.select.dialysatetype");
			form.dialysate_type_id.focus();
			return false;
		}

		var dialyzer_type_id = form.dialyzer_type_id.value;
		if(dialyzer_type_id == ""){
			showMessage("js.dialysismodule.commonvalidations.select.dialyzer");
			form.dialyzer_type_id.focus();
			return false;
		}

		var dialyzer_repr_count = form.dialyzer_repr_count.value;
		if(dialyzer_repr_count == ""){
			showMessage("js.dialysismodule.commonvalidations.select.reprocessnumber");
			form.dialyzer_repr_count.focus();
			return false;
		}

		if(dialyzer_repr_count > 50){
			showMessage("js.dialysismodule.commonvalidations.reprocessnumber.notgreaterthan50");
			form.dialyzer_repr_count.focus();
			return false;
		}

		if(dialyzer_repr_count != 1){
			var dialyzer_repr_date = form.dialyzer_repr_date.value;
			if(dialyzer_repr_date == ""){
				showMessage("js.dialysismodule.commonvalidations.reprocesseddate");
				form.dialyzer_repr_date.focus();
				return false;
			}
		}

		var in_dilayzer_rating_id = form.in_dilayzer_rating_id.value;
		if(in_dilayzer_rating_id == ""){
			showMessage("js.dialysismodule.commonvalidations.select.dialyzerrating");
			form.in_dilayzer_rating_id.focus();
			return false;
		}

		var cannulation = form.cannulation.value;
		if(cannulation=='R' && form.cannulation_reattempt.value == ""){
			showMessage("js.dialysismodule.commonvalidations.cannulation.reattemptvalue");
			form.cannulation_reattempt.focus();
			return false;
		}else if(cannulation=='N'){
			form.cannulation_reattempt.value="";
		}

		if(status == 'C'){
			if(isFinalized == 'false'){
				showMessage("js.dialysismodule.commonvalidations.finalize.sessionvalues");
				return false;
			}
		}

	}else{
		if(form.order_id.value != '' && form.order_id.value !=null && status == 'O'){
			var ok = confirm("Session is still in Ordered status. Do you want to continue?");
				if (!ok)
					return false;
		}
	}
	if(!checkPreEquipmentPreparation())
			return false;

	if(!setAccessPatencyValues(form))
			return false;
	
	if(!checkSecondaryPassword())
			return false;
	
	if(!checkStatus(form))
		return false;

	if(est_duration !=''){
		if(!calcDuration())
		return false;
	}else{
		form.est_duration.value = 0;
	}
	
	if (!checkDialysisFieldvalidation('pre')) {
		return false;
	}
	
	if (!validateDialysistimefields()) {
		return false;
	}
		

	return true;
}
function preInit(){
	disableFields();
	enableCannulationReAttemp();
	enableCoagulation();
	setEstDuration();
	setValues();
	setExcessWeight();
	setPatencyCheckBoxValues(document.preDialysis);
	editPreDialog();
	editPreAssessment();
	initprevSesDialog();
	showPrevSesNotes();
	isPatientHavingVaccinations();
	isPatientHavingLabResults();
	checkCompletionStatus();
	
}


function validateDialysistimefields() {
	var fromDatefull = document.getElementById("startDateTime").value;
	var enddate = document.getElementById("enddate").value;
	var endtime = document.getElementById("endtime").value;
	var toDatefull = enddate+" "+endtime;
	
	var myarray1=fromDatefull.split(/\D/);
	var d1 = parseInt(myarray1[0],10);
	var m1 = parseInt(myarray1[1],10);
	var y1 = parseInt(myarray1[2],10);
	var h1 = parseInt(myarray1[3],10);
	h1=h1+1;
	var mn1 = parseInt(myarray1[4],10);
	var dt1 = new Date(y1, m1-1, d1, h1, mn1);
	
	var myarray2=toDatefull.split(/\D/);
	var d2= parseInt(myarray2[0],10);
	var m2 = parseInt(myarray2[1],10);
	var y2 = parseInt(myarray2[2],10);
	var h2 = parseInt(myarray2[3],10);
	var mn2 = parseInt(myarray2[4],10);
	var dt2 = new Date(y2, m2-1, d2, h2, mn2);
	
	var millisecondsDiff = dt2.getTime() - dt1.getTime();
	if(millisecondsDiff <  0) {
		alert("Dialysis end time field should  be atleast 60 minutes more than dialysis start time. \nDialysis start time is "+fromDatefull);
		return false;
	}
		
	return true;
}

function checkDialysisFieldvalidation(action) {
	var prefix = null;
	if (action == 'pre'){
		prefix = 'in';
	} else if (action == 'post'){
		prefix = 'fin';
	}
	
	bpHighSit = document.getElementById(prefix+'_bp_high_sit').value;
	bpLowSit = document.getElementById(prefix+'_bp_low_sit').value;
	bpHighStand = document.getElementById(prefix+'_bp_high_stand').value;
	bpLowStand = document.getElementById(prefix+'_bp_low_stand').value;
	pulseSit = document.getElementById(prefix+'_pulse_sit').value;
	pulseStand = document.getElementById(prefix+'_pulse_stand').value;
	totalWt = document.getElementById(prefix+'_total_wt').value;
	
	 if (bpHighSit != '') {
		if (!(isInteger(bpHighSit))) {
			alert("Bp Sitting Systolic Field should be numeric field");
			return false;
		} else if((bpHighSit < 80 || bpHighSit > 240)) {
			alert("Bp Sitting Systolic should be between 80 and 240");
		return false;
		}
	} else {
			alert("Bp Sitting Systolic field sholud not be empty");
			document.getElementById(prefix+'_bp_high_sit').focus();
		return false;
	} if (bpLowSit != '') {
		if (!(isInteger(bpLowSit))) {
				alert("Bp Sitting Diastolic Field should be numeric field");
			return false;
		} else if((bpLowSit < 30 || bpLowSit > 140)) {
				alert("Bp Sitting Diastolic should be between 30 and 140");
			return false;
		}
	} else {
			alert("Bp Sitting Diastolic field sholud not be empty");
			document.getElementById(prefix+'_bp_low_sit').focus();
		return false;
	} if (bpHighStand != '') {
		if (!(isInteger(bpHighStand))) {
				alert("Bp Standing Systolic Field should be numeric field");
			return false;
		} else if((bpHighStand < 80 || bpHighStand > 240)) {
				alert("Bp Standing Systolic should be between 80 and 240");
			return false;
		}
	} else {
			alert("Bp Standing Systolic field sholud not be empty");
			document.getElementById(prefix+'_bp_high_stand').focus();
		return false;
	} if (bpLowStand != '') {
		if (!(isInteger(bpLowStand))) {
				alert("Bp Standing Diastolic Field should be numeric field");
			return false;
		} else if((bpLowStand < 30 || bpLowStand > 140)) {
				alert("Bp Standing Diastolic should be between 30 and 140");
			return false;
		}
	} else {
			alert("Bp Standing Diastolic field sholud not be empty");
			document.getElementById(prefix+'_bp_low_stand').focus();
		return false;
	} if (pulseSit != '') {
		if (!(isInteger(pulseSit))) {
				alert("Pulse Sitting Field should be numeric field");
			return false;
		} else if((pulseSit < 40 || pulseSit > 160)) {
				alert("Pulse Sitting should be between 40 and 160");
			return false;
		}
	} else {
			alert("Pulse Sitting field sholud not be empty");
			document.getElementById(prefix+'_pulse_sit').focus();
		return false;
	} if (pulseStand != '') {
		if (!(isInteger(pulseStand))) {
				alert("Pulse Standing Field should be numeric field");
			return false;
		} else if((pulseStand < 40 || pulseStand > 160)) {
				alert("Pulse Standing should be between 40 and 160");
			return false;
		}
	} else {
			alert("Pulse Standing field sholud not be empty");
			document.getElementById(prefix+'_pulse_stand').focus();
		return false;
	} if(totalWt != '') {
		if(!isDecimal(totalWt,2)) {
				alert("Invalid Weight Value.Enter proper weight value");
			return false;
		}
		if (totalWt != '') {
			 if((totalWt < 10 || totalWt > 160)) {
				 alert("Measured Weight should be between 10 and 160");
			return false;
			}
		}
	}  
	return true;
}


function isPatientHavingVaccinations() {
	var tableObj = document.getElementById('infoTable');
	var presentDate = getDatePart(new Date());
	var label = document.getElementById('labelId');
	var textContent = '';

	for (var i=0; i<jsVaccinations.length; i++) {
		var map = jsVaccinations[i];
		var vaccinationDueDate = map['NEXT_DUE_DATE'];
		var vaccinationType = map['VACCINATION_TYPE'];
		var dueDate = new Date(vaccinationDueDate);
		var daysLeft = daysDiff(presentDate, dueDate);
		if (daysLeft <= 7) {
			textContent = textContent +'  Vaccination '+vaccinationType+' is due on '+formatDate(dueDate,'ddmmyyyy','-');
			if (i+1 != jsVaccinations.length)
				textContent = textContent + ', ';
			if (((i+1) % 3) == 0)
				textContent = textContent + '</br></br>';
		}
	}
	if (jsVaccinations.length != 0)
		label.innerHTML = textContent;
}

function isPatientHavingLabResults() {
	var tableObj = document.getElementById('infoTable');
	var presentDate = getDatePart(new Date());
	var label = document.getElementById('labelId1');
	var textContent = '';
	for (var i=0; i<jsLabResults.length; i++) {
		var map = jsLabResults[i];
		var labResultsDueDate = map['NEXT_DUE_DATE'];
		var dueDate = new Date(labResultsDueDate);
		var daysLeft = daysDiff(presentDate, dueDate);
		if (daysLeft <= 7) {
			textContent = textContent +' Lab investigation next due on '+formatDate(dueDate,'ddmmyyyy','-');
			if (i+1 != jsLabResults.length)
				textContent = textContent + ', ';
			if (((i+1) % 3) == 0)
				textContent = textContent + '</br></br>';
		}
	}
	if (jsLabResults.length != 0)
		label.innerHTML = textContent;
}


function enableCannulationReAttemp(){
	var cannulation = document.preDialysis.cannulation.value;
	if(cannulation == 'R'){
		document.getElementById("cannulation_reattempt").style.display = 'block';
	}else{
		document.getElementById("cannulation_reattempt").style.display = 'none';
	}
}

function enableCoagulation(){
	var anticoagulation = document.preDialysis.anticoagulation.value;
	if(anticoagulation == 'H'){
		document.getElementById("withHeparin").style.display = 'table-row';
		document.getElementById("heparinFree").style.display = 'none';
		document.getElementById("heparintype").style.display = 'table-row';


	}else{
		document.getElementById("withHeparin").style.display = 'none';
		document.getElementById("heparinFree").style.display = 'table-row';
		document.getElementById("heparintype").style.display = 'none';

	}
}



function setEstDuration(){
	if(originalDuration!='' && originalDuration!=null){
		var hh = parseInt(originalDuration/60);
		if(hh < 10 )
			hh = '0'+hh
		var mm = parseInt(originalDuration%60);
		if(mm <10 )
			mm = '0'+mm;
		var d = hh + ":" +mm;
		document.preDialysis.estDuration.value= d;
	}
}

function checkStatus(form){
	var status = form.status.value;
	var statusCheck = true;

	if(originalStatus == 'P' && status =='O')
		statusCheck = false;
	else if((originalStatus == 'I' && status =='O') || (originalStatus == 'I' && status =='P'))
		statusCheck = false;
	else if((originalStatus == 'F' && status =='I') || (originalStatus == 'F' && status =='P') || (originalStatus == 'F' && status =='O'))
		statusCheck = false;
	else if((originalStatus == 'C' && status =='F') || (originalStatus == 'C' && status =='I') || (originalStatus == 'C' && status =='P') || (originalStatus == 'C' && status =='O'))
		statusCheck = false;
	else if(originalStatus == 'X' && status !='X')
		statusCheck = false;

	if(!statusCheck){
		showMessage("js.dialysismodule.commonvalidations.notdo.backstatus");
		return false;
	}
	return true;
}

function checkSecondaryPassword(){
	var form  = document.preDialysis;

	var password = form.emp_password.value;
	var status = form.status.value;
	var secondary_check_done = form.secondary_check_done.value;
	var dialyzer_check_user2 = form.dialyzer_check_user2.value;
	var start_attendant = form.start_attendant.value;

	if(start_attendant == dialyzer_check_user2){
	alert(dialyzer_check_user2+" "+start_attendant)
			showMessage("js.dialysismodule.commonvalidations.attendant.secondarycheck.notsame");
			return false;
	}

	if(status == 'I' && (secondary_check_done=='N' || secondary_check_done=='') ){

		if(dialyzer_check_user2 == ""){
			showMessage("js.dialysismodule.commonvalidations.secondarydialysistherapist");
			form.dialyzer_check_user2.focus();
			return false;
		}

		if(password == ""){
			showMessage("js.dialysismodule.commonvalidations.password");
			form.emp_password.focus();
			return false;
		}else{
			if(!validateSecondaryPassword(dialyzer_check_user2,password)){
				showMessage("js.dialysismodule.commonvalidations.invalid.secondarycheckpassword");
				form.dialyzer_check_user2.focus();
				form.emp_password.value = '';
				form.emp_password.focus();
				return false;
			}else{
				form.secondary_check_done.value = 'Y';
			}
		}
	}else if(!empty(dialyzer_check_user2)){
		if(empty(password)){
			showMessage("js.dialysismodule.commonvalidations.password");
			form.emp_password.focus();
			return false;
		} else {
				if(!validateSecondaryPassword(dialyzer_check_user2,password)){
					showMessage("js.dialysismodule.commonvalidations.invalid.secondarycheckpassword");
					form.dialyzer_check_user2.focus();
					form.emp_password.value = '';
					form.emp_password.focus();
					return false;
				}else{
					form.secondary_check_done.value = 'Y';
				}

		}
	} else if(empty(dialyzer_check_user2)) {
		showMessage("js.dialysismodule.commonvalidations.secondarydialysistherapist");
		form.dialyzer_check_user2.focus();
		return false;
	}
	if(form.secondary_check_done.value !='Y')
		form.secondary_check_done.value = 'N';
	return true;
}

function validateSecondaryPassword(user,password) {
	var ajaxobj = newXMLHttpRequest();
	var url = cpath + '/dialysis/PreDialysisSessions.do?_method=secondaryPasswordCheck&user='+user+'&password='+password;
	ajaxobj.open("POST", url.toString(), false);
	ajaxobj.send(null);
	if (ajaxobj) {
		if (ajaxobj.readyState == 4) {
			if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
					eval("var exists =" + ajaxobj.responseText);
					if (exists)
						return true;
					else
						return false;
			}
		}
	}
}

function setAccessPatencyValues(form){
	var accessPatency = form.access_patency.value;

	if(accessPatency == "T"){
		if(form.patencyNf.checked)
			form.patency_nf.value="Y";
		else
			form.patency_nf.value="N";

		if(form.patencyRf.checked)
			form.patency_rf.value="Y";
		else
			form.patency_rf.value="N";
	}else if(accessPatency == "P"){
		//nothing to do
	}else{
		form.patency_nf.value="N";
		form.patency_rf.value="N";
	}

	return true;
}
function getMachine(){
	var k = 0;
	var machineId ="";
	var locationId = document.forms[0].location_id.value;
	for(var i =0;i<machineMasterDetails.length;i++){
		if(locationId == machineMasterDetails[i]["location_id"]){
			machineId = machineMasterDetails[i]["machine_id"];
			k++;
		}
	}

	if(k==1)
		document.forms[0].machine_id.value = machineId;
}

function disableFields(){
	var method = document.preDialysis._method;
	if(method!=null && method.value=='update' && (originalStatus=='C')){
		document.preDialysis.location_id.disabled = true;
		document.preDialysis.machine_id.disabled = true;
		//document.preDialysis.status.disabled = true;
		document.preDialysis.access_type_id.disabled = true;
		document.preDialysis.access_site_id.disabled = true;
		document.preDialysis.dialyzer_type_id.disabled = true;
		document.preDialysis.dialyzer_repr_date.disabled = true;
		document.preDialysis.in_dilayzer_rating_id.disabled = true;
		document.preDialysis.access_patency.disabled = true;
		document.preDialysis.patencyNf.disabled = true;
		document.preDialysis.patencyRf.disabled = true;
		document.preDialysis.patency_bruit_thrill.disabled = true;
		document.preDialysis.equipPre.disabled = true;
		document.preDialysis.start_attendant.disabled = true;
		document.preDialysis.dialyzer_check_user2.disabled = true;
		document.getElementById("isoYes").disabled = true;
		document.getElementById("isoNo").disabled = true;
		document.getElementById("accessYes").disabled = true;
		document.getElementById("accessNo").disabled = true;
		document.preDialysis.iso_uf_time.disabled = true;
		document.preDialysis.anticoagulation.disabled = true;
		document.preDialysis.cannulation.disabled = true;
		//document.preDialysis.save.disabled = true;
	}
	if(method!=null && method.value=='update' && (originalStatus=='I' || originalStatus=='F' || originalStatus=='C')){
		document.preDialysis.machine_id.disabled = true;
	}
}
function setValues(){
	var measuredWt = document.preDialysis.in_total_wt.value ;
	var wheelChairWt = document.preDialysis.in_wheelchair_wt.value ;
	var prostheticWt =document.preDialysis.in_prosthetic_wt.value ;

	if(measuredWt != ''){
		var currentweight = measuredWt;
		if(wheelChairWt !='')
			currentweight = currentweight-wheelChairWt;
		if(prostheticWt!= '')
			currentweight = currentweight-prostheticWt;
		document.getElementById("currentWeight").textContent = Math.round(currentweight*100)/100;
		document.preDialysis.in_real_wt.value = currentweight;

		if(document.getElementById("previousWeight").textContent == '' || document.getElementById("previousWeight").textContent == 0){
			document.getElementById("weightChange").textContent = '';
			document.preDialysis.weight_change.value = 0;
		}else{
			var weightChange =  currentweight - document.getElementById("previousWeight").textContent;
			document.getElementById("weightChange").textContent = Math.round(weightChange*100)/100;
			document.preDialysis.weight_change.value = currentweight - document.getElementById("previousWeight").textContent ;
		}

		setExcessWeight();
	}else{
		document.getElementById("currentWeight").textContent ="";
		document.preDialysis.in_real_wt.value = null;
		document.getElementById("weightChange").textContent = "";
		document.preDialysis.weight_change.value = null;
	}
}

function funCancel(){
	document.preDialysis.status.disabled = true;
	document.preDialysis.machine_id.value = "";
	document.preDialysis.start_attendant.value = "";

	document.preDialysis.action = "DialysisCurrentSessions.do?_method=list&date_range=week";
	document.preDialysis.submit();
}

function funIntraCancel(){
	if(document.intraForm.status!=null)
		document.intraForm.status.disabled = true;
	document.intraForm.action = "DialysisCurrentSessions.do?_method=list&date_range=week";
	document.intraForm.submit();
}
function setExcessWeight(){

	var targetWeight = document.preDialysis.target_wt.value ;
	var currentWeight = document.getElementById("currentWeight").textContent;
	var duration = document.getElementById("estDuration").value ;

	if(targetWeight == '')
		targetWeight = 0;
	if(currentWeight == '')
		currentWeight = 0;

	if(duration == ''){
		duration = 0;
	} else{
		if(!calcDuration())
			return false;
		duration =  document.preDialysis.est_duration.value ;
	}

	if(targetWeight!='' && currentWeight!=''){
		var excessWeight = currentWeight-targetWeight;
		var excessWeightRound = Math.round(excessWeight*100)/100;
		document.getElementById("excessWt").textContent  = excessWeightRound;
		document.getElementById("targetUF").textContent  = excessWeightRound;

		if(duration != ''){
			var value = (parseFloat(excessWeight))/duration;
			value = Math.round(value*100)/100;
			document.getElementById("targetUFR").textContent  = value;
		}
	}
}

function calcDuration(){
	var estDuration = document.preDialysis.estDuration.value;
	var duration ;
	if(estDuration.match(":") == null){
		showMessage("js.dialysismodule.commonvalidations.duration.hh.mm");
		document.preDialysis.estDuration.focus();
		return false;
	}else{
		var values = estDuration.split(":");
		if (values[0] > 72) {
			showMessage("js.dialysismodule.commonvalidations.hours.notgreaterthan23");
			return false;
		}

		duration = values[0]*60 ;
		if(values[1]!=''){
			if (values[1] > 59) {
				showMessage("js.dialysismodule.commonvalidations.minutes.notgreaterthan60");
				return false;
			}
			duration = parseInt(duration)+ parseInt(values[1]);
		}
		document.preDialysis.est_duration.value = duration;
	}

	return true;
}

function setPatencyCheckBoxValues(form){
	var accessTypeId = form.access_type_id.value;

	if(accessTypeId == ''){
		document.getElementById("access_patency_group1").style.display = "none";
		document.getElementById("access_patency_group2").style.display = "none";
	}else{
		for(var i=0;i<AccessTypeDetailsJson.length;i++){

			if(accessTypeId ==AccessTypeDetailsJson[i]["access_type_id"] ){
				form.access_patency.value = empty(AccessTypeDetailsJson[i]["access_patency"]) ? '' : AccessTypeDetailsJson[i]["access_patency"];
				if(AccessTypeDetailsJson[i]["access_patency"] == 'T'){
					document.getElementById("access_patency_group1").style.display = "block";
					document.getElementById("access_patency_group2").style.display = "none";
				}else if(AccessTypeDetailsJson[i]["access_patency"] == 'P'){
					document.getElementById("access_patency_group1").style.display = "none";
					document.getElementById("access_patency_group2").style.display = "block";
				}else{
					document.getElementById("access_patency_group1").style.display = "none";
					document.getElementById("access_patency_group2").style.display = "none";
				}
			}
		}
	}
}

function editPreDialog() {
	dialog1 = new YAHOO.widget.Dialog("dialog1",
			{
				width:"320px",
				context : ["equipPre", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true,
				buttons : [ { text:"OK", handler:preHandleSubmit, isDefault:true },
							{ text:"Cancel", handler:handleCancel } ]
			} );
	dialog1.render();
}

function editPostDialog() {
	dialog2 = new YAHOO.widget.Dialog("dialog2",
			{
				width:"285px",
				context : ["equipPost", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true,
				buttons : [ { text:"OK", handler:postHandleSubmit, isDefault:true },
							{ text:"Cancel", handler:handleCancel } ]
			} );
	dialog2.render();
}

function editPreAssessment(){
	dialog3 = new YAHOO.widget.Dialog("dialog3",
			{
				width:"380px",
				context : ["preAssessment", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true,
				buttons : [ { text:"OK", handler:assessHandleSubmit, isDefault:true },
							{ text:"Cancel", handler:handleCancel } ]
			} );
	dialog3.render();

}

function initprevSesDialog(){
	dialog4 = new YAHOO.widget.Dialog("dialog4",
			{
				width:"380px",
				context : ["prevSesNotes", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true,
				buttons : [ { text:"OK", handler:prevSesNotesHandleSubmit, isDefault:true },
							{ text:"Cancel", handler:handleCancel } ]
			} );
	dialog4.render();

}

function preHandleSubmit(){
	checkPreEquipmentPreparation();
	dialog1.hide();
}


function postHandleSubmit(){
	checkPostEquipmentPreparation(document.intraForm);
	dialog2.hide();
}

function assessHandleSubmit(){
	dialog3.hide();
}

function previousSessionNotes(){
	dialog4.show();
}

function prevSesNotesHandleSubmit(){
	dialog4.hide();
}
function showPrevSesNotes(){
	if(document.preDialysis.method.value == 'create' && document.getElementById("prevSesDetails").textContent !='')
		dialog4.show();
}

function checkPreEquipmentPreparation(){
	var status = document.preDialysis.status.value;
	var preParamNames = document.getElementsByName('pre_prep_param_name');
	var flag = true;
	if(status == 'P' || status == 'I' || status == 'F'){
		for(var i=0;i<preParamNames.length;i++) {
			if (!document.getElementById('pre_prep_param_name'+i).checked) {
				flag = false;
				break;
			} else {
				document.getElementById('pre_prep_param_name'+i).value="Y";
				document.getElementById('prep_param_value'+i).value="Y";
			}
		}
		if (!flag) {
		 	showMessage("js.dialysismodule.commonvalidations.preequipmentprepationvales");
		 	return flag;
		}
	}
	return flag;
}

function checkPostEquipmentPreparation(form){
	var status = form.dialysis_status.value;
	var postParamNames = document.getElementsByName('post_prep_param_name');
	var flag = true;
	if(status == 'F' || status == 'C' || status == 'I') {
		for(var i=0;i<postParamNames.length;i++) {
			if (!document.getElementById('post_prep_param_name'+i).checked) {
				flag = false;
				break;
			} else {
				document.getElementById('post_prep_param_name'+i).value="Y";
				document.getElementById('prep_param_value'+i).value="Y";
			}
		}
		if (flag == false) {
		 	showMessage("js.dialysismodule.commonvalidations.postequipmentprepationvales");
		 	return flag;
		}
	}
	return flag;
}

function preEquipmentPreparation(){
	dialog1.cfg.setProperty("context",["equipPre", "tr", "br"], false);
	dialog1.show();
}

function postEquipmentPreparation(){
	dialog2.cfg.setProperty("context",["equipPost", "tr", "br"], false);
	dialog2.show();
}

function preAssessmentCheck(){
	dialog3.cfg.setProperty("context",["preAssessment", "tr", "br"], false);
	dialog3.show();
}

function handleCancel() {
	this.cancel();
}
//intra Dialysis Functions

function initIntra(){
	initaddDialog()
	initDialog();
	initSessionDialog();
	editPostDialog();
	addIncidentsToGrid();
	addSessionsToGrid();
	disableIntraFields();
	disableIntraFinalizeAll();
	// set autorefresh: refresh the intra dialysis screen for every 2 minutes
	setTimeout("refreshIntraDialysisPage()", 60000);
	checkCompletionStatus();
}
var autoRefresh = true;
var enabledAutoRefresh = true;
var disabledCompletely = false;
function refreshIntraDialysisPage() {
	if (autoRefresh && enabledAutoRefresh) {
		window.location.href = window.location.href;
	}
}

// this method is called on modifying the any content(edit/add/delete actions on the screen) in the intra dialysis screen
// and disables the auto refresh of the page and disables the auto refresh button.
function disableAutoRefresh() {
	if (!disabledCompletely) {
		showMessage("js.dialysismodule.commonvalidations.disableautorefresh");
		autoRefresh = false;
		enabledAutoRefresh = false;
		document.getElementById('autoRefreshButton').value = getString('js.dialysismodule.commonvalidations.disableautorefresh');
		document.getElementById('autoRefreshButton').disabled = true;
		disabledCompletely = true;
	}
}

function toggleAutoRefresh() {
	autoRefresh = !autoRefresh;
	enabledAutoRefresh = !enabledAutoRefresh;
	document.getElementById('autoRefreshButton').value = enabledAutoRefresh ? getString('js.dialysismodule.commonvalidations.disableautorefresh') : getString('js.dialysismodule.commonvalidations.enableautorefresh');
}

function saveCurrrentValues(actionToDO){
	document.intraForm._method.value = "create";
	document.intraForm.actionToDo.value = actionToDO;
	document.intraForm.submit();
}

function intraSubmitValues(){
	var status = document.intraForm.status;
	var len = document.getElementById('sessionTable').rows.length-2;
	var completionStatus = document.getElementById('completion_status');
	var finalized= 0;
	if(status!=null){
		if(status.value =='O' || status.value=='P' ){
			showMessage("js.dialysismodule.commonvalidations.notselect.previousstatus");
			return false;
		}

		if(status.value == 'C'){

			if (completionStatus.value == '') {
				showMessage("js.dialysismodule.commonvalidations.completionstatus");
				document.getElementById('completion_status').focus();
				return false;
			}

			for(var i=1;i<=len;i++){
				if(document.getElementById("finalized"+i).value == 'Y' || document.getElementById("finalized"+i).value == '' ){
					finalized ++;
				}
			}
			if(finalized!= len ){
				showMessage("js.dialysismodule.commonvalidations.finalize.sessionvalues");
				return false;
			}
		}
	}
	if(!checkPostEquipmentPreparation(document.intraForm))
			return false;

	var length = document.getElementsByName('obs_time').length;
	for (var i=1; i<=length; i++){
		var time = document.getElementById('obstimeLabel'+i).textContent;
		if (time == null || time == '')
			document.getElementById('obs_time'+i).value = currentDateandTime;
		else
			document.getElementById('obs_time'+i).value = currentDate + " " +time;
	}
	return document.intraForm.submit();
}

function disableIntraFinalizeAll(){
	var disableFinalizeAll =true;
	var finalizeCheckBoxs = document.getElementsByName("finalized_name");
	var finalized = document.getElementsByName("db_finalized");

	for (var i=0; i<finalizeCheckBoxs.length; i++) {
		if(finalized[i] == undefined){
			disableFinalizeAll = false;
			break;
		}
	}

	if(disableFinalizeAll)
		 document.getElementById("finalizeAllValues").disabled = true;
}
function finalizeAll(){
	disableAutoRefresh();
	var finalizeAllCheckbxs = document.getElementById("finalizeAllValues").checked;

	var finalizeCheckBoxs = document.getElementsByName("finalized_name");
	var finalized = document.getElementsByName("db_finalized");

	for (var i=0; i<finalizeCheckBoxs.length; i++) {
		if(finalized[i] == undefined){
			if (finalizeAllCheckbxs ) {
				finalizeCheckBoxs[i].checked = true;
			} else{
				finalizeCheckBoxs[i].checked = false;
			}
		}
		setFinalizedValue(i+1);
	}
}

function setDiscardValues(obj, obsId, index){
	disableAutoRefresh();
	var  val =
		document.getElementById("discardValue"+index).value = document.getElementById("discardValue"+index).value == 'false' ?  'true' : 'false';

			if (val == 'true'){
					document.getElementById("discard_obs_id"+index).value = obsId;
					document.getElementById(obj.id).src = cpath+"/icons/Deleted.png";
				} else {
					document.getElementById("discard_obs_id"+index).value="";
					document.getElementById(obj.id).src = cpath+"/icons/Delete.png";
			}

}

function setDiscardIncidentValues(index,incidentId){
	disableAutoRefresh();
	var discardValue =
		 document.getElementById('discardHidden'+index).value = document.getElementById('discardHidden'+index).value == 'false' ? 'true' : 'false';

	if(discardValue == 'true'){
		document.getElementById('discardIncidents'+index).src = cpath + '/icons/Deleted.png';
		if(incidentId != '')
			document.getElementById("discard_ids"+index).value=incidentId;
		else
			document.getElementById("discard_ids"+index).value="New";
	}else{
		document.getElementById('discardIncidents'+index).src = cpath + '/icons/Delete.png';
		document.getElementById("discard_ids"+index).value="";
	}

}

function setFinalizedValue(index){
	disableAutoRefresh();
	if(document.getElementById("finalized_name"+index).checked)
		document.getElementById("finalized"+index).value="Y";
	else
		document.getElementById("finalized"+index).value="N";

}

function openDialogBox(id){
	var button = document.getElementById("tableRow"+id);
	document.intraForm.descriptionField.value = document.getElementById("descLabel"+id).textContent ;
	document.intraForm.unusaleventField.value = document.getElementById("unusalLabel"+id).textContent ;
	if(document.getElementById("timeLabel"+id).textContent == '')
		document.intraForm.incidentTime.value = document.intraForm.presentTime.value;
	else
		document.intraForm.incidentTime.value = document.getElementById("timeLabel"+id).textContent ;

	if( document.getElementById("typeLabel"+id).textContent == 'Info')
		document.intraForm.incidentType.value = "I";
	else
		document.intraForm.incidentType.value = "A";

	dialog.cfg.setProperty("context",[button, "tr", "br"], false);
	document.getElementById("dialogId").value = id;

	dialog.show();
}
function initDialog(){
dialog = new YAHOO.widget.Dialog("dialog",
		{
			width:"340px",
			context : ["incidentsTable", "tr", "br"],
			visible:false,
			modal:true,
			constraintoviewport:true,

		} );
dialog.render();
}
function handelSubmit(){
	disableAutoRefresh();
	var time = document.intraForm.incidentTime.value;
	var  desc = document.intraForm.descriptionField.value;
	var type =  document.intraForm.incidentType.value;
	var unusal= document.intraForm.unusaleventField.value;
	addIncidentsToTable(time,type,desc,unusal,loggedInUser,'');

}
		// addIncidentsToTable(time,type,desc,unsual,username,incidentId);
function addIncidentsToTable(time,type,desc,unusal,username,incidentId){

	var incidentstable = document.getElementById("incidentsTable");
	var id = document.getElementById("dialogId").value;
	var tabLen = incidentstable.rows.length;
	var typename;

	document.getElementById("timeLabel"+id).textContent 	= time;
	document.getElementById("incident_time"+id).value 		=  time;

	typename =  type=='I'?'Info':'Alert';
	document.getElementById("typeLabel"+id).textContent 	=  typename;
	document.getElementById("incident_type"+id).value 		=  type;


	document.getElementById("authorLabel"+id).textContent 	= username;
	document.getElementById("username"+id).value 			=  username;

	document.getElementById("descLabel"+id).textContent 	= desc;
	document.getElementById("description"+id).value 		=  desc;

    document.getElementById("unusalLabel"+id).textContent 	= unusal;
    document.getElementById("treatment_for_unusual_event"+id).value =unusal;


	if(incidentId != '')
		document.getElementById("incident_id"+id).value 	=  incidentId;

	var imgDelete = makeImageButton('discardIncidents','discardIncidents'+id,'',cpath+'/icons/Delete.png');
	imgDelete.setAttribute("onclick","setDiscardIncidentValues('"+id+"','"+incidentId+"')");

	var discardHidden = makeHidden('discardHidden','discardHidden'+id,'false');

	if(document.getElementById("discardLabel"+id).firstChild == null) {
		document.getElementById("discardLabel"+id).appendChild(imgDelete);
		document.getElementById("discardLabel"+id).appendChild(discardHidden);
	}

	document.getElementById("add"+id).src= cpath+"/icons/Edit.png";

	var nextrow =  document.getElementById("tableRow"+(eval(id)+1));

	if(nextrow == null){
		AddRowsToGrid(tabLen);
	}

	dialog.hide();
}

function AddRowsToGrid(tabLen){
	var incidentstable = document.getElementById("incidentsTable");

	var tdObj="",trObj="";
	var row = "tableRow" + tabLen;

	var timeLabel		= makeLabel('timeLabel','timeLabel'+tabLen,'');
	var typeLabel		= makeLabel('typeLabel','typeLabel'+tabLen,'');
	var authorLabel		= makeLabel('authorLabel','authorLabel'+tabLen,'');
	var descLabel		= makeLabel('descLabel','descLabel'+tabLen,'');
	var unusalLabel		= makeLabel('unusalLabel','unusalLabel'+tabLen,'');
	var discardLabel	= makeLabel('discardLabel','discardLabel'+tabLen,'');

	var incident_timeHidden = makeHidden('incident_time','incident_time'+tabLen,'');
	var incident_typeHidden = makeHidden('incident_type','incident_type'+tabLen,'');
	var usernameHidden 		= makeHidden('username','username'+tabLen,'');
	var descriptionHidden= makeHidden('description','description'+tabLen,'');
	var unusaleventHidden= makeHidden('treatment_for_unusual_event','treatment_for_unusual_event'+tabLen,'');
	var incident_idHidden= makeHidden('incident_id','incident_id'+tabLen,'');
	var discard_idsHidden= makeHidden('discard_ids','discard_ids'+tabLen,'');

	var itemrowbtn = makeImageButton('add','add'+tabLen,'', cpath+'/icons/Add.png');
	itemrowbtn.setAttribute("onclick","openDialogBox('"+tabLen+"')");

	trObj = incidentstable.insertRow(tabLen);
	trObj.id = row;

	tdObj = trObj.insertCell(-1);
	tdObj.appendChild(timeLabel);
	tdObj.appendChild(incident_timeHidden);
	tdObj.appendChild(incident_idHidden);

	tdObj = trObj.insertCell(-1);
	tdObj.appendChild(typeLabel);
	tdObj.appendChild(incident_typeHidden);

	tdObj = trObj.insertCell(-1);
	tdObj.appendChild(authorLabel);
	tdObj.appendChild(usernameHidden);

	tdObj = trObj.insertCell(-1);
	tdObj.appendChild(descLabel);
	tdObj.appendChild(descriptionHidden);

	tdObj = trObj.insertCell(-1);
	tdObj.appendChild(unusalLabel);
	tdObj.appendChild(unusaleventHidden);

	tdObj = trObj.insertCell(-1);
	tdObj.appendChild(discardLabel);
	tdObj.appendChild(discard_idsHidden);

	tdObj = trObj.insertCell(-1);
	tdObj.appendChild(itemrowbtn);

}
function makeLabel(name, id, value) {
		var el = document.createElement('label');
		if (name!=null && name!="")
			el.name = name;
		if (id!=null && id!="")
			el.id = id;
		if (value!=null && value!="")
			el.value = value;
		return el;
	}
function addIncidentsToGrid(){
	if(incidents == null || incidents.length == 0)
		return;
	for(var i=0;i<incidents.length;i++){
		var time = incidents[i].incident_time;
		var type = incidents[i].incident_type;
		var desc = incidents[i].description;
		var unusal=incidents[i].treatment_for_unusual_event;
		var username = incidents[i].username;
		var incidentId =  incidents[i].incident_id;
		document.getElementById("dialogId").value = i+1;
		time = formatTime(new Date(time),false);
		addIncidentsToTable(time,type,desc,unusal,username,incidentId);
	}
}

function disableIntraFields(){
	if(sessionStatus == 'C'){
		document.intraForm.save.disabled = true;
		var incidentstable = document.getElementById("incidentsTable");
		var tabLen = incidentstable.rows.length-1;
		for(var i=1;i<=tabLen;i++){
			document.getElementById("add"+i).disabled = true;
			if(document.getElementById("discardIncidents"+i) != null)
				document.getElementById("discardIncidents"+i).disabled = true;
		}
	}
}
//Post Dialysis Functions
function setPostWtValues(){
	var measuredFinWt = document.postDialysis.fin_total_wt.value ;
	var wheelChairFinWt = document.postDialysis.fin_wheelchair_wt.value ;
	var prostheticFinWt =document.postDialysis.fin_prosthetic_wt.value ;

	if(measuredFinWt != ''){
		var currentFinweight = measuredFinWt;
		if(wheelChairFinWt !='')
			currentFinweight = currentFinweight-wheelChairFinWt;
		if(prostheticFinWt!= '')
			currentFinweight = currentFinweight-prostheticFinWt;
		document.postDialysis.fin_real_wt.value = currentFinweight;
		var weightLoss = preDialysisWt- currentFinweight;
		document.getElementById("weightLoss").textContent = Math.round(weightLoss*100)/100;
		document.postDialysis.total_wt_loss.value =  preDialysisWt- currentFinweight;
	}else{
		document.postDialysis.fin_real_wt.value = null;
		document.getElementById("weightLoss").textContent = null;
		document.postDialysis.total_wt_loss.value = null;
	}
}
function setHeparinInfused(){
	var heparinLeft = document.postDialysis.heparin_left.value;
	if(heparinLeft!=''){
		var infused = heparinLeft;
		var preheparin = 0;

		if(heparinBolus!='' && heparinStart!='')
			preheparin = parseFloat(heparinBolus) + parseFloat(heparinStart);

		if(preheparin > 0)
			infused = preheparin - infused;

		document.getElementById("heparinInfused").textContent = infused;
		document.postDialysis.total_heparin.value = infused;
	}

}
function setPostAccessPatencyValues(form){
	var accessPatency = form.access_patency.value;

	if(accessPatency == "T"){
		if(form.patencyNf.checked)
			form.fin_patency_nf.value="Y";
		else
			form.fin_patency_nf.value="N";

		if(form.patencyRf.checked)
			form.fin_patency_rf.value="Y";
		else
			form.fin_patency_rf.value="N";
	}else{
	//	document.forms[0].fin_patency_bruit.value="N";
	//	document.forms[0].fin_patency_thrill.value="N";
		form.fin_patency_nf.value="N";
		form.fin_patency_rf.value="N";
	}

	return true;
}
function funPostSubmitValues(){
	var form  = document.postDialysis;
	
	
	var status = form.status.value;
	if(status =='O' || status=='P' || status=='I'){
		showMessage("js.dialysismodule.commonvalidations.notselect.previousstatus");
		return false;
	}

	var end_attendant = form.end_attendant.value;
	if(end_attendant == ""){
		showMessage("js.dialysismodule.commonvalidations.select.closingattendant");
		form.end_attendant.focus();
		return false;
	}

	var endDateValue = document.getElementById('enddate').value.trim();
	var endDateTimeValue = document.getElementById('endtime').value.trim();
	if (endDateValue == '') {
		showMessage("js.dialysismodule.commonvalidations.date");
		document.getElementById('enddate').focus();
		return false;
	}
	if (endDateTimeValue != '') {
		if (!isTime(endDateTimeValue)) {
			showMessage('js.dialysismodule.commonvalidations.time.hh.mm');
			document.getElementById('endtime').focus();
			return false;
		}
	} else {
		showMessage('js.dialysismodule.commonvalidations.time');
		document.getElementById('endtime').focus();
		return false;
	}
	document.getElementById('end_time').value = endDateValue+' '+endDateTimeValue

	var completion_status = form.completion_status.value;
	if(completion_status == ""){
		showMessage("js.dialysismodule.commonvalidations.completionstatus");
		form.completion_status.focus();
		return false;
	}

	var fin_dialyzer_rating_id = form.fin_dialyzer_rating_id.value;
	if(fin_dialyzer_rating_id == ""){
		showMessage("js.dialysismodule.commonvalidations.select.dialyzerrating");
		form.fin_dialyzer_rating_id.focus();
		return false;
	}

	if(form.access_patency.value == "P"){
		var fin_patency = document.getElementById('fin_patency_bruit_thrill').options[document.getElementById('fin_patency_bruit_thrill').selectedIndex].value;
		if(fin_patency == '') {
			showMessage('js.dialysismodule.commonvalidations.select.bruit.thrill');
			document.getElementById('fin_patency_bruit_thrill').focus();
			return false;
		}
	}

	if(status == 'C'){
		if(isFinalized == 'false'){
			showMessage("js.dialysismodule.commonvalidations.finalize.sessionvalues");
			return false;
		}
	}

	if(!setPostAccessPatencyValues(form))
		return false;

	if (!onlyFourDigits(document.getElementById('target_wt_removal'))){
		showMessage("js.dialysismodule.commonvalidations.fluidremoved.shouldbe4digits");
		document.getElementById('target_wt_removal').focus();
		return false;
	}


	if (!onlyFourDigits(document.getElementById('fluid_in_wt'))){
		showMessage("js.dialysismodule.commonvalidations.ufrate.shouldbe4digits");
		document.getElementById('fluid_in_wt').focus();
		return false;
	}
	
	if (!checkDialysisFieldvalidation('post')) {
		return false;
	}
	
	if (!validateDialysistimefields()) {
		return false;
	}
	
		
}

function onlyFourDigits(object) {
		var val = document.getElementById(object.id).value;

		if (val != null && val != ""){
			var parts = val.split('.');
			if (parts[0].length >= 1 ) {

				if (parts[0].length > 4)
					return false;

			} else {
				if (val.length > 4)
				return false;
			}
		}
		return true;
}

function funPostCancel(){
	document.postDialysis.status.disabled = true;

	document.postDialysis.action = "DialysisCurrentSessions.do?_method=list&date_range=week";
	document.postDialysis.submit();
}
function checkConsumablesExist(consumablesExists){
	if(consumablesExists == "false"){
		showMessage("js.dialysismodule.commonvalidations.consumables.notdefined");
		return false;
	}
	return true;
}
//sessions Summary functions

function doSearch(){
	var mrno = document.summaryForm.mr_no.value ;
	var all = document.getElementById('alerts_');
	var noAlerts = document.getElementById('alerts_0');
	var alerts = document.getElementById('alerts_1');

	if(mrno == ""){
		showMessage("js.dialysismodule.commonvalidations.mrno");
		return false;
	}

	if (all.checked || (noAlerts.checked && alerts.checked) ) {
		document.getElementById('operator').value = '>=';
		document.getElementById('value').value = 0;
	} else if (noAlerts.checked) {
		document.getElementById('operator').value = '=';
		document.getElementById('value').value = 0;
	} else {
		document.getElementById('operator').value = '>=';
		document.getElementById('value').value = 1;
	}
	return true;
}
function setDates(){

	if(document.getElementById("30Range").checked){
		calcPrevNDays(document.summaryForm.start_date,document.summaryForm.end_date,30);
	} else if(document.getElementById("60Range").checked){
		calcPrevNDays(document.summaryForm.start_date, document.summaryForm.end_date,60);
	} else if(document.getElementById("90Range").checked){
		calcPrevNDays(document.summaryForm.start_date, document.summaryForm.end_date,90);
	}
}
function calcPrevNDays(fromEl, toEl,n){
	var today = getServerDate();
	var previous = new Date(today);
	toEl.value = formatDate(previous);
	previous.setDate(today.getDate() - n);
	var dateStr = formatDate(previous);
	fromEl.value = dateStr;
}

function initMrnoAutoComplete(){
	Insta.initMRNoAcSearch(contextPath, "mrno", "mrnoContainer", "all",null,null);
}

function showPostSesNotes() {
	if (prevSessionNotes != null && prevSessionNotes != "") {
		dialog4.show();
	}
}

//intradialysis

function checkTimeformat(obj) {

	doValidateTimeField(obj);
}

function subscribeEscKeyEvent(dialog) {
	var kl = new YAHOO.util.KeyListener(document, { keys:27 },
		{ fn:dialog.cancel, scope:dialog, correctScope:true } );
	dialog.cfg.setProperty("keylisteners", kl);
}



function getThisRow(node) {
	return findAncestor(node, "TR");
}

function initaddDialog() {
	intradialog = new YAHOO.widget.Dialog("addDilog",
		{
			width:"340px",
			context : ["sessionTable", "tr", "br"],
			visible:false,
			modal:true,
			constraintoviewport:true,

		} );

	var escKeyListener = new YAHOO.util.KeyListener("intraSessionFields", { keys:13 },
	                                              { fn: intraAddSubmit,
	                                                scope: intradialog,
	                                                correctScope: true } );

    var escKeyListener1 = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn: intradialog.cancel,
	                                                scope: intradialog,
	                                                correctScope: true } );

	intradialog.cfg.queueProperty("keylisteners", [escKeyListener1, escKeyListener]);
	intradialog.render();
}

function getAddDialog(id) {

	var read = false;

	if (document.getElementById('finalforRead'+id).value == 'Y' || document.getElementById('statusforRead'+id).value == 'C'){
		read = true;
	}

	var button = document.getElementById("itemrow"+id);
	var thisForm = document.intraForm;

	if(document.getElementById('obstimeLabel' + id).textContent == '')
		thisForm.obsTime.value = document.intraForm.presentTime.value;
	else
		thisForm.obsTime.value = document.getElementById('obstimeLabel' + id).textContent;

	thisForm.obsTime.readOnly = read;
	thisForm.bpHigh.value =
		document.getElementById('bphlabel' + id).textContent;
	thisForm.bpHigh.readOnly = read;
	thisForm.bpLow.value =
		document.getElementById('bplowLabel' + id).textContent;
	thisForm.bpLow.readOnly = read;
	thisForm.bpTime.value =
		document.getElementById('bptLabel' + id).textContent;
	thisForm.bpTime.readOnly = read;
	thisForm.pulseRate.value =
		document.getElementById('pulselabel' + id).textContent;
	thisForm.pulseRate.readOnly = read;
	thisForm.ufRemoved.value =
		document.getElementById('ufLabel' + id).textContent;
	thisForm.ufRemoved.readOnly = read;
	thisForm.ufRate.value =
		document.getElementById('ufrLabel' + id).textContent;
	thisForm.ufRate.readOnly = read;
	thisForm.bloodPumpRate.value =
		document.getElementById('bloodLabel' + id).textContent;
	thisForm.bloodPumpRate.readOnly = read;
	thisForm.heparinRate.value =
		document.getElementById('heparinLabel' + id).textContent;
	thisForm.heparinRate.readOnly = read;
	thisForm.dialysateTemp.value =
		document.getElementById('dialysateLabel' + id).textContent;
	thisForm.dialysateTemp.readOnly = read;
	thisForm.dialysateCond.value =
		document.getElementById('dialysatecLabel' + id).textContent;
	thisForm.dialysateCond.readOnly = read;
	thisForm.venousPressure.value =
		document.getElementById('venousLabel' + id).textContent;
	thisForm.venousPressure.readOnly = read;
	thisForm.dialysatePressure.value =
		document.getElementById('dialysatepLabel' + id).textContent;
	thisForm.dialysatePressure.readOnly = read;
	thisForm.tmP.value =
		document.getElementById('tmpLabel' + id).textContent;
	thisForm.tmP.readOnly = read;
	thisForm.dialysisTime.value =
		document.getElementById('dialysistLabel' + id).textContent;
	thisForm.dialysisTime.readOnly = read;
	thisForm.dialysateRate.value =
		document.getElementById('dialysaterLabel' + id).textContent;
	thisForm.dialysateRate.readOnly = read;

	intradialog.cfg.setProperty("context",[button, "tr", "br"], false);
	document.getElementById("dialogId").value = id;
	intradialog.show();
	thisForm.obsTime.focus();

}

function closeDialog() {
	addDialog.cancel();
}

function intraAddSubmit() {
	disableAutoRefresh();
	var id = document.getElementById("dialogId").value;

	var imgbutton = makeImageButton('discard','discard'+id,'imgDelete',cpath+'/icons/Delete.png');
	imgbutton.setAttribute('onclick','setDiscardValues(this, -'+id+', '+id+')');

	var checkbox = makeCheckbox('finalized_name', 'finalized_name'+id, '', false);
	checkbox.setAttribute('onclick', 'setFinalizedValue('+id+')');


	if(document.getElementById("deleteLabel"+id).firstChild == null) {

		document.getElementById("deleteLabel"+id).appendChild(imgbutton);
		var discardValue = makeHidden('discardValue', 'discardValue'+id, 'false');
		var discard_obs_id = makeHidden('discard_obs_id', 'discard_obs_id'+id, '');
		document.getElementById("deleteLabel"+id).appendChild(discardValue);
		document.getElementById("deleteLabel"+id).appendChild(discard_obs_id);
		document.getElementById('finalized_label'+id).appendChild(checkbox);
		document.getElementById('finalized'+id).value='N';
	}

	var obsTime = document.intraForm.obsTime.value;
	var bpHigh = document.intraForm.bpHigh.value;
	var bpLow = document.intraForm.bpLow.value;
	var bpTime = document.intraForm.bpTime.value;
	var pulseRate = document.intraForm.pulseRate.value;
	var ufRemoved = document.intraForm.ufRemoved.value;
	var ufRate = document.intraForm.ufRate.value;
	var bloodPumpRate = document.intraForm.bloodPumpRate.value;
	var heparinRate = document.intraForm.heparinRate.value;
	var dialysateTemp = document.intraForm.dialysateTemp.value;
	var dislysateCond = document.intraForm.dialysateCond.value;
	var venousPressure = document.intraForm.venousPressure.value;
	var dialysatePressure = document.intraForm.dialysatePressure.value;
	var tmp = document.intraForm.tmP.value;
	var dialysisTime = document.intraForm.dialysisTime.value;
	var dialysateRate = document.intraForm.dialysateRate.value;

	if (!(bpTime.trim() == '')) {
		if (!isTime(bpTime.trim())) {
			showMessage('js.dialysismodule.commonvalidations.time.hh.mm');
			document.intraForm.bpTime.focus();
			return false;
		}
	}

	document.getElementById('obstimeLabel' + id).textContent = obsTime;
	document.getElementById('bphlabel' + id).textContent =  bpHigh;
	document.getElementById('seperator' + id).textContent = "/";
	document.getElementById('bplowLabel' + id).textContent =  bpLow;
	document.getElementById('bptLabel' + id).textContent =  bpTime;
	document.getElementById('pulselabel' + id).textContent = pulseRate;
	document.getElementById('ufLabel' + id).textContent = ufRemoved;
	document.getElementById('ufrLabel' + id).textContent = ufRate;
	document.getElementById('bloodLabel' + id).textContent = bloodPumpRate;
	document.getElementById('heparinLabel' + id).textContent = heparinRate;
	document.getElementById('dialysateLabel' + id).textContent = dialysateTemp;
	document.getElementById('dialysatecLabel' + id).textContent = dislysateCond;
	document.getElementById('venousLabel' + id).textContent = venousPressure;
	document.getElementById('dialysatepLabel' + id).textContent = dialysatePressure;
	document.getElementById('tmpLabel' + id).textContent = tmp;
	document.getElementById('dialysistLabel' + id).textContent = dialysisTime;
	document.getElementById('dialysaterLabel' + id).textContent = dialysateRate;


	document.getElementById('bp_high' + id).value =  bpHigh;
	document.getElementById('bp_low' + id).value = bpLow;
	document.getElementById('bp_time' + id).value = bpTime;
	document.getElementById('pulse_rate' + id).value = pulseRate;
	document.getElementById('uf_removed' + id).value = ufRemoved;
	document.getElementById('uf_rate' + id).value = ufRate;
	document.getElementById('blood_pump_rate' + id).value = bloodPumpRate;
	document.getElementById('heparin_rate' + id).value = heparinRate;
	document.getElementById('dialysate_temp' + id).value = dialysateTemp;
	document.getElementById('dialysate_cond' + id).value = dislysateCond;
	document.getElementById('venous_pressure' + id).value = venousPressure;
	document.getElementById('dialysate_pressure' + id).value = dialysatePressure;
	document.getElementById('tmp' + id).value = tmp;
	document.getElementById('dialysis_time' + id).value = dialysisTime;
	document.getElementById('dialysate_rate' + id).value = dialysateRate;

	if (document.getElementById('observation_id'+ id).value == 'new'){
		document.getElementById('observation_id'+ id).value = '-'+id+'';
	}

	if (document.getElementById('png'+id).name == 'add'){
		document.getElementById('png'+id).src = cpath + '/icons/Edit.png';
		document.getElementById('png'+id).name = 'edit';
	}

	var tab = document.getElementById("sessionTable");
	var tabLen = tab.rows.length;
	var row = "row" + tabLen;
	var nextrow =  document.getElementById("row"+(eval(id)+1));

	if(nextrow == null) {

		var obstimeLabel = makeLabel('obstimeLabel', 'obstimeLabel'+tabLen, '');
		var bphlabel = makeLabel('bphlabel','bphlabel'+tabLen,'');
		var seperator = makeLabel('', 'seperator'+tabLen, '/');
		var bplowLabel = makeLabel('bplowLabel','bplowLabel'+tabLen,'');
		var bptLabel = makeLabel('bptLabel','bptLabel'+tabLen,'');
		var pulselabel = makeLabel('pulselabel','pulselabel'+tabLen,'');
		var ufLabel = makeLabel('ufLabel','ufLabel'+tabLen,'');
		var ufrLabel = makeLabel('ufrLabel', 'ufrLabel'+tabLen, '');
		var bloodLabel = makeLabel('bloodLabel','bloodLabel'+tabLen,'');
		var heparinLabel = makeLabel('heparinLabel','heparinLabel'+tabLen,'');
		var dialysateLabel = makeLabel('dialysateLabel','dialysateLabel'+tabLen,'');
		var dialysatecLabel = makeLabel('dialysatecLabel','dialysatecLabel'+tabLen,'');
		var venousLabel = makeLabel('venousLabel','venousLabel'+tabLen,'');
		var dialysatepLabel = makeLabel('dialysatepLabel','dialysatepLabel'+tabLen,'');
		var tmpLabel = makeLabel('tmpLabel','tmpLabel'+tabLen,'');
		var dialysistLabel = makeLabel('dialysistLabel','dialysistLabel'+tabLen,'');
		var dialysaterLabel = makeLabel('dialysaterLabel','dialysaterLabel'+tabLen,'');
		var deleteLabel = makeLabel('deleteLabel', 'deleteLabel'+tabLen, '');
		var checkbox = makeLabel('finalized_label', 'finalized_label'+tabLen, '');


		var obs_time = makeHidden('obs_time', 'obs_time'+tabLen, '');
		var bp_high = makeHidden('bp_high','bp_high'+tabLen,'');
		var bp_low = makeHidden('bp_low','bp_low'+tabLen,'');
		var bp_time = makeHidden('bp_time','bp_time'+tabLen,'');
		var pulse_rate = makeHidden('pulse_rate','pulse_rate'+tabLen,'');
		var uf_removed = makeHidden('uf_removed','uf_removed'+tabLen,'');
		var uf_rate = makeHidden('uf_rate','uf_rate'+tabLen,'');
		var blood_pump_rate = makeHidden('blood_pump_rate','blood_pump_rate'+tabLen,'');
		var heparin_rate = makeHidden('heparin_rate','heparin_rate'+tabLen,'');
		var dialysate_temp = makeHidden('dialysate_temp','dialysate_temp'+tabLen,'');
		var dialysate_cond = makeHidden('dialysate_cond','dialysate_cond'+tabLen,'');
		var venous_pressure = makeHidden('venous_pressure','venous_pressure'+tabLen,'');
		var dialysate_pressure = makeHidden('dialysate_pressure','dialysate_pressure'+tabLen,'');
		var tmp = makeHidden('tmp','tmp'+tabLen,'');
		var dialysis_time = makeHidden('dialysis_time','dialysis_time'+tabLen,'');
		var dialysate_rate = makeHidden('dialysate_rate','dialysate_rate'+tabLen,'');
		var observation_id = makeHidden('observation_id','observation_id'+tabLen, 'new');
		var finalized = makeHidden('finalized', 'finalized'+tabLen, '');
		var finalforRead = makeHidden('', 'finalforRead'+tabLen, '');
		var statusforRead = makeHidden('', 'statusforRead'+tabLen, '');

		var itemrowbtn = makeImageButton('add','png'+tabLen,'',cpath+'/icons/Add.png');

		itemrowbtn.setAttribute("onclick","getAddDialog('"+tabLen+"')");

		trObj = tab.insertRow(tabLen);
		trObj.id = row;

		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(obstimeLabel);

		tdObj.appendChild(obs_time);
		tdObj.appendChild(bp_high);
		tdObj.appendChild(bp_low);
		tdObj.appendChild(bp_time);
		tdObj.appendChild(pulse_rate);
		tdObj.appendChild(uf_removed);
		tdObj.appendChild(uf_rate);
		tdObj.appendChild(blood_pump_rate);
		tdObj.appendChild(heparin_rate);
		tdObj.appendChild(dialysate_temp);
		tdObj.appendChild(dialysate_cond);
		tdObj.appendChild(venous_pressure);
		tdObj.appendChild(dialysate_pressure);
		tdObj.appendChild(tmp);
		tdObj.appendChild(dialysis_time);
		tdObj.appendChild(dialysate_rate);
		tdObj.appendChild(observation_id);
		tdObj.appendChild(finalized);
		tdObj.appendChild(finalforRead);
		tdObj.appendChild(statusforRead);

		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(bphlabel);
		tdObj.appendChild(seperator);
		tdObj.appendChild(bplowLabel);

		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(bptLabel);

		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(pulselabel);

		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(ufLabel);

		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(ufrLabel);

		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(bloodLabel);

		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(heparinLabel);

		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(dialysateLabel);

		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(dialysatecLabel);

		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(venousLabel);

		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(dialysatepLabel);

		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(tmpLabel);

		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(dialysistLabel);

		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(dialysaterLabel);

		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(deleteLabel);

		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(checkbox);

		tdObj = trObj.insertCell(-1);

		tdObj = trObj.insertCell(-1);

		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(itemrowbtn);

	}

	intradialog.hide();

}
function cancelDialog(dialogName) {
	dialogName.cancel();
}

function initSessionDialog(){
    sesDialog = new YAHOO.widget.Dialog("sesNotesDialog",
			{
				width:"340px",
				context : ["sessionsTable", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true,

			} );
	sesDialog.render();
}

function addSessionsToTable(time, plan, intervention, evaluation, username, sessionId){

	var sessionstable = document.getElementById("sessionsTable");
	var id = document.getElementById("sesNotesDialog").value;
	var tabLen = sessionstable.rows.length;
	var typename;

	document.getElementById("timeSesLabel"+id).textContent 	= time;
	document.getElementById("session_time"+id).value 		=  time;

	document.getElementById("planLabel"+id).textContent 	=  plan;
	document.getElementById("plan"+id).value 		=  plan;

	document.getElementById("staffNameLabel"+id).textContent 	= username;
	document.getElementById("username"+id).value 			=  username;

	document.getElementById("interventionLabel"+id).textContent 	= intervention;
	document.getElementById("intervention"+id).value 		=  intervention;

	document.getElementById("evaluationLabel"+id).textContent 	= evaluation;
	document.getElementById("evaluation"+id).value 		=  evaluation;

	if(sessionId != '')
		document.getElementById("session_notes_id"+id).value 	=  sessionId;

	var imgDelete = makeImageButton('discardSession','discardSession'+id,'',cpath+'/icons/Delete.png');
	imgDelete.setAttribute("onclick","setDiscardSessionValues('"+id+"','"+sessionId+"')");

	var discardHidden = makeHidden('discardSessionHidden','discardSessionHidden'+id,'false');

	if(document.getElementById("discardSesLabel"+id).firstChild == null) {
		document.getElementById("discardSesLabel"+id).appendChild(imgDelete);
		document.getElementById("discardSesLabel"+id).appendChild(discardHidden);
	}

	document.getElementById("addSes"+id).src= cpath+"/icons/Edit.png";

	var nextrow =  document.getElementById("tableSesRow"+(eval(id)+1));

	if(nextrow == null){
		AddRowsToSessionGrid(tabLen);
	}

	sesDialog.hide();
}

function AddRowsToSessionGrid(tabLen){
	var sessiontable = document.getElementById("sessionsTable");

	var tdObj="",trObj="";
	var row = "tableSesRow" + tabLen;

	var timeSesLabel		= makeLabel('timeSesLabel','timeSesLabel'+tabLen,'');
	var staffLabel		= makeLabel('staffNameLabel','staffNameLabel'+tabLen,'');
	var planLabel		= makeLabel('planLabel','planLabel'+tabLen,'');
	var interventionLabel		= makeLabel('interventionLabel','interventionLabel'+tabLen,'');
	var evaluationLabel		= makeLabel('evaluationLabel','evaluationLabel'+tabLen,'');
	var discardSesLabel	= makeLabel('discardSesLabel','discardSesLabel'+tabLen,'');

	var session_timeHidden = makeHidden('session_time','session_time'+tabLen,'');
	var planHidden = makeHidden('plan','plan'+tabLen,'');
	var usernameHidden 		= makeHidden('username','username'+tabLen,'');
	var interventionHidden= makeHidden('intervention','intervention'+tabLen,'');
	var evaluationHidden =  makeHidden('evaluation','evaluation'+tabLen,'');
	var session_idHidden= makeHidden('session_notes_id','session_notes_id'+tabLen,'');
	var discardses_idsHidden= makeHidden('discard_sess_ids','discard_sess_ids'+tabLen,'');

	var itemrowbtn = makeImageButton('addSes','addSes'+tabLen,'', cpath+'/icons/Add.png');
	itemrowbtn.setAttribute("onclick","openSesDialogBox('"+tabLen+"')");

	trObj = sessiontable.insertRow(tabLen);
	trObj.id = row;

	tdObj = trObj.insertCell(-1);
	tdObj.appendChild(timeSesLabel);
	tdObj.appendChild(session_timeHidden);
	tdObj.appendChild(session_idHidden);

	tdObj = trObj.insertCell(-1);
	tdObj.appendChild(staffLabel);
	tdObj.appendChild(usernameHidden);

	tdObj = trObj.insertCell(-1);
	tdObj.appendChild(planLabel);
	tdObj.appendChild(planHidden);


	tdObj = trObj.insertCell(-1);
	tdObj.appendChild(interventionLabel);
	tdObj.appendChild(interventionHidden);

	tdObj = trObj.insertCell(-1);
	tdObj.appendChild(evaluationLabel);
	tdObj.appendChild(evaluationHidden);

	tdObj = trObj.insertCell(-1);
	tdObj.appendChild(discardSesLabel);
	tdObj.appendChild(discardses_idsHidden);

	tdObj = trObj.insertCell(-1);
	tdObj.appendChild(itemrowbtn);

}

function addSessionsToGrid(){
	if(sessionNts == null || sessionNts.length == 0)
		return;
	for(var i=0;i<sessionNts.length;i++){
		var time = sessionNts[i].session_time;
		var plan = sessionNts[i].plan;
		var intervention = sessionNts[i].intervention;
		var evaluation = sessionNts[i].evaluation;
		var username = sessionNts[i].username;
		var sessionNtsId =  sessionNts[i].session_notes_id;
		document.getElementById("sesNotesDialog").value = i+1;
		time = formatTime(new Date(time),false);
		addSessionsToTable(time, plan, intervention, evaluation, username, sessionNtsId);
	}
}

function openSesDialogBox(id){
	var button = document.getElementById("tableSesRow"+id);
	document.intraForm.interventionField.value = document.getElementById("interventionLabel"+id).textContent ;
	document.intraForm.sessionPlan.value = document.getElementById("planLabel"+id).textContent ;
	document.intraForm.evaluationField.value = document.getElementById("evaluationLabel"+id).textContent ;

	if(document.getElementById("timeSesLabel"+id).textContent == '')
		document.intraForm.sessionTime.value = document.intraForm.presentTime.value;
	else
		document.intraForm.sessionTime.value = document.getElementById("timeSesLabel"+id).textContent ;

	sesDialog.cfg.setProperty("context",[button, "tr", "br"], false);
	document.getElementById("sesNotesDialog").value = id;

	sesDialog.show();
}

function handelSesSubmit(){

	disableAutoRefresh();
	var sesTime = document.intraForm.sessionTime.value;
	var  plan = document.intraForm.sessionPlan.value;
	var intervention =  document.intraForm.interventionField.value;
	var evaluation = document.intraForm.evaluationField.value;

	addSessionsToTable(sesTime, plan, intervention, evaluation, loggedInUser, '');

}

function setDiscardSessionValues(index, sessionId){
	disableAutoRefresh();
	var discardValue =
		 document.getElementById('discardSessionHidden'+index).value = document.getElementById('discardSessionHidden'+index).value == 'false' ? 'true' : 'false';

	if(discardValue == 'true'){
		document.getElementById('discardSession'+index).src = cpath + '/icons/Deleted.png';
		if(sessionId != '')
			document.getElementById("discard_sess_ids"+index).value=sessionId;
		else
			document.getElementById("discard_sess_ids"+index).value="New";
	}else{
		document.getElementById('discardSession'+index).src = cpath + '/icons/Delete.png';
		document.getElementById("discard_sess_ids"+index).value="";
	}

}

var drugDialog;

function initDrugDialog() {
	drugDialog = new YAHOO.widget.Dialog("drugDialog",
				{
					width:"420px",
					context : ["equipPre", "tr", "br"],
					visible:false,
					modal:true,
					constraintoviewport:true,
					buttons : [ { text:"OK", handler:addToTable, isDefault:true },
								{ text:"Cancel", handler:drugDialogCancel } ]
				} );
		drugDialog.render();
}

function drugDialogCancel () {
	drugDialog.cancel();
}

function openDialog(obj) {
	drugDialog.cfg.setProperty('context', [obj, 'tr', 'br'], false);
	document.getElementById('medicine_name').value = '';
	document.getElementById('batch_no').value = '';
	remarks = document.getElementById('expiry_date').value = '';
	expression = document.getElementById('route_of_admin').value = '';
	resultCode = document.getElementById('staff').value = '';
	order = document.getElementById('doctors').value = '';
	drugDialog.show();
}

var newRowinserted = true;
var currentRow = null;

function addToTable() {
	var medicineName = document.getElementById('medicine_name').value;
	var administered = document.getElementById('administered').value;
	var batchNo = document.getElementById('batch_no').value;
	var expDate = document.getElementById('expiry_date').value;
	var route = document.getElementById('route_of_admin').value;

	var currentRowIndex = -1;
	if (newRowinserted == false) {
		var rowObj = getThisRow(currentRow, 'TR');
		currentRowIndex = parseInt(rowObj.id);
	}
	if (medicineName == '') {
		showMessage('js.dialysismodule.commonvalidations.medicinename.notempty');
		return false;
	}

	if (administered == 'Y') {
		if (batchNo == '' || expDate == '' || route == '') {
			showMessage("js.dialysismodule.commonvalidations.batchno.expirydate.routeno.mandatory");
			return false;
		}
	}

	var tableObj = document.getElementById('drugTable');
	var rowsLength = tableObj.rows.length;
	var templateRow = tableObj.rows[rowsLength-2];
	var newRow = '';
	var currentDate = new Date();
	var dateStr = formatDate(currentDate, 'yyyy-MM-dd HH:mm');
	if (expDate != '')
		expDate = changeDateFormat(expDate);

	if (newRowinserted) {
		var id = rowsLength-2;
		newRow = templateRow.cloneNode(true);
		newRow.style.display = '';
		newRow.id = rowsLength-2;
		getElementByName(newRow, 'newRow').value = true;
		getElementByName(newRow, 'newRow').id = 'newRow'+id;
		getElementByName(newRow, 'deleted').id = 'deleted'+id;
		getElementByName(newRow, 'deleted').value = 'N';
		YAHOO.util.Dom.insertBefore(newRow, templateRow);
	} else {
		newRow = getThisRow(currentRow, 'TR');
		getElementByName(newRow, 'edited').value = true;
	}

	var tds = newRow.getElementsByTagName('td');
	var staff = document.getElementById('staff').value;
	var doctor = document.getElementById('doctors').options[document.getElementById('doctors').selectedIndex].text;
	var administeredText = document.getElementById('administered').options[document.getElementById('administered').selectedIndex].text;

	tds[0].textContent = medicineName;

	tds[2].textContent = administeredText;
	tds[3].textContent = batchNo;
	tds[4].textContent = expDate;
	tds[5].textContent = route;
	tds[6].textContent = staff;
	tds[7].textContent = doctor;

	getElementByName(newRow, 'medicine_name').value = medicineName;
	getElementByName(newRow, 'date_time').value = dateStr;
	getElementByName(newRow, 'administered').value = administered;
	getElementByName(newRow, 'expiry_date').value = expDate;
	getElementByName(newRow, 'doctor').value = document.getElementById('doctors').value;
	getElementByName(newRow, 'route_of_administration').value = route;
	getElementByName(newRow, 'batch_no').value = batchNo;
	getElementByName(newRow, 'administered_by').value = staff;

	newRowinserted = true;
	currentRow = null;
	removeClassName(newRow, 'editing');
	drugDialog.cancel();

}

function onEdit(obj) {

	drugDialog.cfg.setProperty('context', [obj, 'tr', 'br'], false);
	var trObj = getThisRow(obj, 'TR');
	addClassName(trObj, 'editing');
	var tds = trObj.getElementsByTagName('td');

	document.getElementById('medicine_name').value = tds[0].textContent;
	document.getElementById('administered').value = tds[2].textContent;
	document.getElementById('batch_no').value = tds[3].textContent;
	document.getElementById('expiry_date').value = tds[4].textContent;
	document.getElementById('route_of_admin').value = tds[5].textContent;
	setSelectedIndexText(document.getElementById('staff'), tds[6].textContent);
	setSelectedIndexText(document.getElementById('doctors'), tds[7].textContent);

	newRowinserted = false;
	currentRow = obj;
	drugDialog.show();
}

function changeElsColor(index, obj) {

		var trObj = getThisRow(obj);
		var tab = getThisTable(obj);
		var index = trObj.id;

		var markRowForDelete = document.getElementById('deleted'+index).value == 'N' ? 'Y' : 'N';
		document.getElementById('deleted'+index).value = document.getElementById('deleted'+index).value == 'N' ? 'Y' :'N';

		if (markRowForDelete == 'Y') {
			addClassName(trObj, 'cancelled');
	   	}
	   	else {
			removeClassName(trObj, 'cancelled');
	   	}
}

function changeDateFormat(dateStr) {
	var dateParts = dateStr.split('-');
	return dateParts[2]+'-'+dateParts[1]+'-'+dateParts[0];

}

function checkCompletionStatus() {
	var status = document.getElementById('status');

	if (status != undefined && status.value == 'C') {
		document.getElementById('completion_status').disabled = false;

	} else {
		document.getElementById('completion_status').disabled = true;
		if (jscompletionStatus.trim() == '')
			document.getElementById('completion_status').value = '';
	}
}

function enabledisableHeparinType() {
	var heparintype = document.preDialysis.heparin_type.value;

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
}
