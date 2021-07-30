

function validateTime(timeField) {
	var strTime = timeField.value;
	var timePattern = /[0-9]:[0-9]/;
	var regExp = new RegExp(timePattern);

	if (strTime == '') {
		showMessage("js.scheduler.schedulerdashboard.enter.time");
		timeField.focus();
		return false;
	}
	if (regExp.test(strTime)) {
		var strHours = strTime.split(':')[0];
		var strMinutes = strTime.split(':')[1];
		if (!isInteger(strHours)) {
			showMessage("js.scheduler.schedulerdashboard.incorrecttimeformat.hour");
			timeField.focus();
			return false;
		}
		if (!isInteger(strMinutes)) {
			showMessage("js.scheduler.schedulerdashboard.incorrecttimeformat.minute");
			timeField.focus();
			return false;
		}
		if ((parseInt(strHours) > 23) || (parseInt(strHours) < 0)) {
			showMessage("js.scheduler.schedulerdashboard.incorrecthour");
			timeField.focus();
			return false;
		}
		if ((parseInt(strMinutes) > 59) || (parseInt(strMinutes) < 0)) {
			showMessage("js.scheduler.schedulerdashboard.incorrectminute");
			timeField.focus();
			return false;
		}
		if(strMinutes.length !=2){
			showMessage("js.scheduler.schedulerdashboard.incorrectminute.digits");
			return false;
		}
	} else {
		showMessage("js.scheduler.doctornonavailability.incorrecttimeformat");
		timeField.focus();
		return false;
	}
	return true;
}

function validate(){
	var doctor = document.forms[0].doctor.value;

	if(doctor == ''){
		showMessage("js.scheduler.schedulerdashboard.doctor");
		return false;
	}

	if(!validateEmptyTimes()){
		return false;
	}

	if (!validateFromTime()) {
		return false;
	}

	if(!validateStartEndTime()){
		return false;
	}
	return true;
}

function validateEmptyTimes(){
	var flag = false;
	for(var i=0;i<7;i++){
			var name = "";
			if(i==0){
				name = "mon";
			}
			if(i==1){
				name = "tue";
			}
			if(i==2){
				name = "wed";
			}
			if(i==3){
				name = "thu";
			}
			if(i==4){
				name = "fri";
			}
			if(i==5){
				name = "sat";
			}
			if(i==6){
				name = "sun";
			}

			if((document.getElementById(name+1).value != 'HH:MM' ) || (document.getElementById(name+2).value != 'HH:MM')
					|| (document.getElementById(name+3).value != 'HH:MM') || (document.getElementById(name+4).value != 'HH:MM')){
				flag = true;
				break;
			}
		}
	if(!flag){
		showMessage("js.scheduler.schedulerdashboard.enter.time");
	}
	return flag;
}

function validateFromTime() {
	var flag = false;
	for(var i=0;i<7;i++){
			var name = "";
			if(i==0){
				name = "mon";
			}
			if(i==1){
				name = "tue";
			}
			if(i==2){
				name = "wed";
			}
			if(i==3){
				name = "thu";
			}
			if(i==4){
				name = "fri";
			}
			if(i==5){
				name = "sat";
			}
			if(i==6){
				name = "sun";
			}

			if((document.getElementById(name+1).value == 'HH:MM' ) ||  (document.getElementById(name+3).value == 'HH:MM')){
				if (document.getElementById(name+2).value != 'HH:MM') {
					if (!validateTime(YAHOO.util.Dom.get(name+2))) {
						return false;
					}
				}
				if (document.getElementById(name+4).value != 'HH:MM') {
					if (!validateTime(YAHOO.util.Dom.get(name+4))) {
						return false;
					}
				}
			}
		}
		return true;
}

function validateStartEndTime(){

	for(var i=0;i<7;i++){
			var name = "";
			if(i==0){
				name = "mon";
			}
			if(i==1){
				name = "tue";
			}
			if(i==2){
				name = "wed";
			}
			if(i==3){
				name = "thu";
			}
			if(i==4){
				name = "fri";
			}
			if(i==5){
				name = "sat";
			}
			if(i==6){
				name = "sun";
			}

			if((document.getElementById(name+1).value != 'HH:MM' ) || (document.getElementById(name+2).value != 'HH:MM')
					|| (document.getElementById(name+3).value != 'HH:MM') || (document.getElementById(name+4).value != 'HH:MM')){
			var amsttime  = document.getElementById(name+1).value.split(":");
			var amendtime  = document.getElementById(name+2).value.split(":");

			var pmsttime  = document.getElementById(name+3).value.split(":");
			var pmendtime  = document.getElementById(name+4).value.split(":");

			if(amsttime[0] != 'HH'){
				if(!validateTime(YAHOO.util.Dom.get(name+1)))
				  	return false;
				 if(!validateTime(YAHOO.util.Dom.get(name+2)))
				  	return false;
				 if(!compareTimes(name+2,amsttime,amendtime))
					return false;
			}
			if(pmsttime[0] != 'HH'){
				if(!validateTime(YAHOO.util.Dom.get(name+3)))
				  	return false;
				if(!validateTime(YAHOO.util.Dom.get(name+4)))
				  	return false;
			    if(amendtime[0] != 'HH' && pmsttime[0] != 'HH'){
					if(!compareTimes(name+3,amendtime,pmsttime))
					return false;
				}
				if(!compareTimes(name+4,pmsttime,pmendtime))
					return false;
			}
		}
	}//for
	return true;
}

function compareTimes(name,time1,time2){
	if(eval(time1[0]) < eval(time2[0])){
	}else{
		if(eval(time1[1]) < eval(time2[1])){
		}else{
			showMessage("js.scheduler.doctornonavailability.toslottime.morethan.fromslottime");
			document.getElementById(name).focus();
			return false;
		}
	}
	return true;
}

function getCompleteTime(obj){
	var time = obj.value;
	if(time != ''){
		if(time.length <= 2){
			obj.value = time + ":00";
		}
	}else{
		obj.value = "HH:MM";
	}
}

function makeblank(timefield){
	if(timefield.value == 'HH:MM') timefield.value="";
}

function setTime(time){
	if(time.value == '') time.value = 'HH:MM';
}

function fillEmpty(){
	var els = document.forms[0].elements.length;
	for (var i=0; i<els; i++) {
		if(document.forms[0].elements[i].name != 'doctor' && document.forms[0].elements[i].name != 'doctorName' && document.forms[0].elements[i].name != 'deptName'){
			var el = document.forms[0].elements[i].value;
			if (el == '' || el == null) {
				document.forms[0].elements[i].value = 'HH:MM';
			}
		}
	}
}

function getDoctorWeekNonAvailability(week){
	if(week == 'Previous'){
		weekNumber = eval(weekNumber) - 1;
	}else{
		weekNumber = eval(weekNumber) + 1;
	}
	var docID = document.forms[0].doctor.value;
	var url = "../../pages/resourcescheduler/addeditappointments.do?method=getDoctorNonAvailabilityTiming&docID="+docID+"&weekNo="+weekNumber;
	var reqObject = newXMLHttpRequest();
	reqObject.open("POST",url.toString(), false);
	reqObject.send(null);
	if (reqObject.readyState == 4) {
		if ( (reqObject.status == 200) && (reqObject.responseText!=null) ) {
			var obj = eval(reqObject.responseText);
			if (obj != null) {
				deleteRows();
				var tab = document.getElementById("nonAvailableTable");
				for(var i = 0; i< obj.length; i++){
					var item = obj[i];
					var tdObj="", trObj="";

					trObj = tab.insertRow(i);

					tdObj = trObj.insertCell(0);
					var weekDay = document.createTextNode(item["week_day"]);
					tdObj.appendChild(weekDay);

					tdObj = trObj.insertCell(1);
					var curdate = new Date();
					curdate.setTime(item["non_available_date"]);
					var dd = curdate.getDate();
					if(dd<10){
						dd = '0'+dd;
					}
					var mm = curdate.getMonth()+1;
					if((mm)<10){
						mm = '0'+mm;
					}
					var yyyy = curdate.getFullYear();
					var dt = dd+'-'+mm+'-'+yyyy;
					var date = document.createTextNode(dt);
					tdObj.appendChild(date);

					tdObj = trObj.insertCell(2);
					var fromtext = document.createTextNode("From");
					tdObj.appendChild(fromtext);

					tdObj = trObj.insertCell(3);
					var time1 = convertTime(item["firsthalf_from"]);
					var ffrom = document.createTextNode(time1);
					tdObj.appendChild(ffrom);

					tdObj = trObj.insertCell(4);
					var totext = document.createTextNode("To");
					tdObj.appendChild(totext);

					tdObj = trObj.insertCell(5);
					var time2 = convertTime(item["firsthalf_to"]);
					var fto = document.createTextNode(time2);
					tdObj.appendChild(fto);

					tdObj = trObj.insertCell(6);
					var fromtext = document.createTextNode("From");
					tdObj.appendChild(fromtext);

					tdObj = trObj.insertCell(7);
					var time3 = convertTime(item["secondhalf_from"]);
					var sfrom = document.createTextNode(time3);
					tdObj.appendChild(sfrom);

					tdObj = trObj.insertCell(8);
					var totext = document.createTextNode("To");
					tdObj.appendChild(totext);

					tdObj = trObj.insertCell(9);
					var time4 = convertTime(item["secondhalf_to"]);
					var sto = document.createTextNode(time4);
					tdObj.appendChild(sto);

					tdObj = trObj.insertCell(10);
					tdObj.innerHTML = '<a href="doctortiming.do?method=getDoctorNonAvailabilityScreen&doctorId='+docID+
					'&doctorName='+item["doctor_name"]+'&weekday='+item["week_no"]+
					'&dept='+item["dept_id"]+'&deptName='+item["dept_name"]+'&naDate='+dt+'&firstFrom='+time1+'&firstTo='+time2+
					'&secondFrom='+time3+'&secondTo='+time4+'&action=edit">Edit</a>';
				}
			}
		}
	}
	return null;
}


function deleteRows(){
	var table = document.getElementById("nonAvailableTable");
	var rowCount = table.rows.length;
	for(var i=0; i<rowCount;i++){
		table.deleteRow(-1);
	}
}

function convertTime(time){
	if(time != null){
		var curdate = new Date();
		curdate.setTime(time);
		var hours = curdate.getHours();
		if(hours<10){
			hours = '0'+hours;
		}
		var minutes = curdate.getMinutes();
		if(minutes<10){
			minutes = '0'+minutes;
		}
		return hours +':' + minutes;
	}else return '';
}



function matches(doctorInputVal, autoComp, doctorArr, doctorIndex) {
	for (var i in doctorArr) {
		var doctor = doctorArr[i];
		if (doctor == doctorInputVal) {
			var index = parseInt(doctorIndex[doctor]);
			var elNewItem = autoComp._aListItems[index];
			elNewItem._sResultKey = doctor;
			return elNewItem;
		}
	}
	return null;
}

function doctorAutoComplete(doctorInputVal) {
	var doctorinput = "newDoctor";
	var doctorContainer = "doctorContainer";

	var doctorIndex=[];
	var doctorArr=[];

	doctorArr.length = doctorsJson.length;
	for (var i=0; i<doctorsJson.length; i++) {
		var item = doctorsJson[i];
		doctorArr[i] = item["doctor_name"]+"-"+item["dept_name"];
		doctorIndex[doctorArr[i]] = i;
	}

	var ds = new YAHOO.widget.DS_JSArray(doctorArr);
	ds.queryMatchContains = true;

	YAHOO.widget.AutoComplete.prototype.maxResultsDisplayed = doctorsJson.length;
	var autoComp = new YAHOO.widget.AutoComplete(doctorinput, doctorContainer, ds);
	autoComp.prehighlightClassName = "yui-ac-prehighlight";
	autoComp.typeAhead = true;
	autoComp.useShadow = true;
	autoComp.allowBrowserAutocomplete = false;
	autoComp.minQueryLength = 0;

	autoComp.autoHighlight = false;
	autoComp.forceSelection = true;

	autoComp.itemSelectEvent.subscribe(function(){
		var doctorInput = document.getElementById("newDoctor").value;
		if(doctorInput != null && doctorInput != ''){
			for (var j=0;j<doctorArr.length;j++) {
				if(doctorArr[j] == doctorInput){
					document.getElementById("doctor").value = doctorsJson[j]["doctor_id"];
					document.getElementById("doctorName").value = doctorsJson[j]["doctor_name"];
					document.getElementById("deptName").value = doctorsJson[j]["dept_name"];
				}
			}
		}
	});

	autoComp.containerCollapseEvent.subscribe(function() {
		var doctorInput = document.getElementById("newDoctor").value;
		if(doctorInput != null && doctorInput == ''){
			document.getElementById("doctor").value = "";
			document.getElementById("doctorName").value = "";
			document.getElementById("deptName").value = "";
		}
	});

	if (doctorInputVal != null && doctorInputVal != '') {
		var elNewItem = matches(doctorInputVal, autoComp, doctorArr, doctorIndex);
		if(elNewItem != null){
			autoComp._selectItem(elNewItem);
		}
	}
}
