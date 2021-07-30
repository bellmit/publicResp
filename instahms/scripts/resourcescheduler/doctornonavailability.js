
	var sunflag = false; var monflag = false; var tueflag = false;
	var wednflag = false; var thurflag = false; var friflag = false;
	var satflag = false;

	function enableFields(id){
		var form = document.forms[0];
		if(id == "2"){
			if(monflag == true){
				monflag = false;
				form.mon1.disabled=true;
				form.mon2.disabled=true;
				form.mon3.disabled=true;
				form.mon4.disabled=true;
			}
			else{
				monflag = true;
				form.mon1.disabled=false;
				form.mon2.disabled=false;
				form.mon3.disabled=false;
				form.mon4.disabled=false;
			}
		}
		if(id == "3"){
			if(tueflag == true){
				tueflag = false;
				form.tue1.disabled=true;
				form.tue2.disabled=true;
				form.tue3.disabled=true;
				form.tue4.disabled=true;
			}
			else{
				tueflag = true;
				form.tue1.disabled=false;
				form.tue2.disabled=false;
				form.tue3.disabled=false;
				form.tue4.disabled=false;
			}
		}
		if(id == "4"){
			if(wednflag == true){
				wednflag = false;
				form.wed1.disabled=true;
				form.wed2.disabled=true;
				form.wed3.disabled=true;
				form.wed4.disabled=true;
			}
			else{
				wednflag = true;
				form.wed1.disabled=false;
				form.wed2.disabled=false;
				form.wed3.disabled=false;
				form.wed4.disabled=false;
			}
		}
		if(id == "5"){
			if(thurflag == true){
				thurflag = false;
				form.thu1.disabled=true;
				form.thu2.disabled=true;
				form.thu3.disabled=true;
				form.thu4.disabled=true;
			}
			else{
				thurflag = true;
				form.thu1.disabled=false;
				form.thu2.disabled=false;
				form.thu3.disabled=false;
				form.thu4.disabled=false;
			}
		}
		if(id == "6"){
			if(friflag == true){
				friflag = false;
				form.fri1.disabled=true;
				form.fri2.disabled=true;
				form.fri3.disabled=true;
				form.fri4.disabled=true;
			}
			else{
				friflag = true;
				form.fri1.disabled=false;
				form.fri2.disabled=false;
				form.fri3.disabled=false;
				form.fri4.disabled=false;
			}
		}
		if(id == "7"){
			if(satflag == true){
				satflag = false;
				form.sat1.disabled=true;
				form.sat2.disabled=true;
				form.sat3.disabled=true;
				form.sat4.disabled=true;
			}
			else{
				satflag = true;
				form.sat1.disabled=false;
				form.sat2.disabled=false;
				form.sat3.disabled=false;
				form.sat4.disabled=false;
			}
		}
		if(id == "1"){
			if(sunflag == true){
				sunflag = false;
				form.sun1.disabled=true;
				form.sun2.disabled=true;
				form.sun3.disabled=true;
				form.sun4.disabled=true;
			}
			else{
				sunflag = true;
				form.sun1.disabled=false;
				form.sun2.disabled=false;
				form.sun3.disabled=false;
				form.sun4.disabled=false;
			}
		}
		for(var i=0;i<form.deleteTiming.length;i++){
			if(id == form.deleteTiming[i].value){
				form.deleteTiming[i].disabled = false;
				form.deleteDay[i].disabled = false;
			}
		}
	}


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
		showMessage("js.scheduler.schedulerdashboard.incorrecttimeformat");
		timeField.focus();
		return false;
	}
	return true;
}

function validate(){
	var weekLen = document.forms[0].weekday.length;
	var remarks = document.forms[0].remarks.value;
	var fromdate = document.forms[0].fromdate.value;
	var todate = document.forms[0].todate.value;

	if(fromdate == ''){
		showMessage("js.scheduler.schedulerdashboard.fromtime");
		return false;
	}
	if(todate == ''){
		showMessage("js.scheduler.schedulerdashboard.totime");
		return false;
	}

	if(document.forms[0].finalfromdate.value != fromdate){
		showMessage("js.scheduler.schedulerdashboard.fromdatechange");
		return false;
	}

	if(document.forms[0].finaltodate.value != todate){
		showMessage("js.scheduler.schedulerdashboard.todatechange");
		return false;
	}

	var flag=false;
	for(var w=0;w<weekLen;w++){
	 if(document.forms[0].weekday[w].checked==true)
			flag=true;
	}

	if(flag==true){}
	else{
		 showMessage("js.scheduler.schedulerdashboard.select.day");
	 	return false;
	}

	if(!validateStartEndTime()){
		return false;
	}
	/*if(!checkAppointments()){
		return false;
	}*/

	return true;
}

function checkAppointments(){
	var fromdate = document.forms[0].fromdate.value;
	var todate = document.forms[0].todate.value;
	var docid = document.forms[0].doctor.value;
	var reqObject = newXMLHttpRequest();
	var url ="../../pages/resourcescheduler/addeditappointments.do?method=checkAppointments&fromdate="+fromdate+"&todate="+todate+"&docid="+docid;
	reqObject.open("POST",url.toString(), false);
	reqObject.send(null);
	if (reqObject.readyState == 4) {
		if ( (reqObject.status == 200) && (reqObject.responseText!=null) ) {
			var obj = (eval(reqObject.responseText));
			if(obj != null && obj != ''){
				showMessage("js.scheduler.schedulerdashboard.appointment.exists");
				return false;
			} else {
			return true;
			}
		}
	}
}


function checkValidations(){

	var fromdate = document.forms[0].fromdate.value;
	var todate = document.forms[0].todate.value;

	if(fromdate == ''){
		showMessage("js.scheduler.schedulerdashboard.fromdate");
		return false;
	}
	if(todate == ''){
		showMessage("js.scheduler.schedulerdashboard.todate");
		return false;
	}
	if(!doValidateDateField(document.forms[0].fromdate, "future"))
		return false;

	if(!doValidateDateField(document.forms[0].todate, "future"))
		return false;

	if(!validateFromToDate(document.forms[0].fromdate, document.forms[0].todate)){
		document.forms[0].todate.focus();
		return false;
	}

	document.forms[0].finalfromdate.value = fromdate;
	document.forms[0].finaltodate.value = todate;

	if(!checkNoofdays()) return false;

	getAjaxTimingBWDates();

	return true;
}

function checkNoofdays(){

	var weekLen = document.forms[0].weekday.length;
	var date1 = parseDateStr(document.forms[0].fromdate.value);
	var date2 = parseDateStr(document.forms[0].todate.value);
	var days = days_between(date1, date2);
	days = eval(days) +1;
	if(days >=7 ){
		for(var w=0;w<weekLen;w++){
			document.forms[0].weekday[w].disabled = false;
		}
		return true;
	}else{
		for(var w=0;w<weekLen;w++){
			if(document.forms[0].weekday[w].checked){
					if(w==0) monflag=true;
					if(w==1) tueflag=true;
					if(w==2) wednflag=true;
					if(w==3) thurflag=true;
					if(w==4) friflag=true;
					if(w==5) satflag=true;
					if(w==6) sunflag=true;
					document.forms[0].weekday[w].checked = false;
					enableFields(document.forms[0].weekday[w].value);
				}
				else{
					if(i==0) monflag=false;
					if(i==1) tueflag=false;
					if(i==2) wednflag=false;
					if(i==3) thurflag=false;
					if(i==4) friflag=false;
					if(i==5) satflag=false;
					if(i==6) sunflag=false;
				}
			document.forms[0].weekday[w].disabled = true;
		}
		var dayArr = new Array();
		var date = date1;
		for(var i=0;i<days;i++){
			var day = "";
				if(i == 0){
				}else{
					date = getNextDate(date);
				}
				day = date.getDay()+1;
			dayArr.push(day);
		}
		for(var d=0;d<dayArr.length;d++){
			for(var w=0;w<weekLen;w++){
	 			if(dayArr[d] == document.forms[0].weekday[w].value){
	 				document.forms[0].weekday[w].disabled = false;
	 				document.forms[0].weekday[w].checked = true;
	 				enableFields(document.forms[0].weekday[w].value);
	 			}
			}
		}
		return true;
	}
}

function getNextDate(date1){
		var d = date1.getDate();
		var m = date1.getMonth();
		var y = date1.getFullYear();
		var nextdate = new Date(y, m, d+1);
		return nextdate;
}

	function days_between(date1, date2) {
	    // The number of milliseconds in one day
	    var ONE_DAY = 1000 * 60 * 60 * 24;

	    // Convert both dates to milliseconds
	    var date1_ms = date1.getTime();
	    var date2_ms = date2.getTime();

	    // Calculate the difference in milliseconds
	    var difference_ms = Math.abs(date1_ms - date2_ms);

	    // Convert back to days and return
	    return Math.round(difference_ms/ONE_DAY);
	}

function validateStartEndTime(){

	for(var i=0;i<7;i++){
		if(document.forms[0].weekday[i].checked == true){
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

			if((document.getElementById(name+1).value == '' ) && (document.getElementById(name+2).value == '')
					&& (document.getElementById(name+3).value == '') && (document.getElementById(name+4).value == '')){
				showMessage("js.scheduler.schedulerdashboard.enter.time");
				return false;
			}

			var amsttime  = document.getElementById(name+1).value.split(":");
			var amendtime  = document.getElementById(name+2).value.split(":");

			var pmsttime  = document.getElementById(name+3).value.split(":");
			var pmendtime  = document.getElementById(name+4).value.split(":");

			if(amsttime != ''){
				if(!validateTime(YAHOO.util.Dom.get(name+1)))
				  	return false;
				 if(!validateTime(YAHOO.util.Dom.get(name+2)))
				  	return false;
				 if(!compareTimes(name+2,amsttime,amendtime))
					return false;
			}
			if(pmsttime != ''){
				if(!validateTime(YAHOO.util.Dom.get(name+3)))
				  	return false;
				if(!validateTime(YAHOO.util.Dom.get(name+4)))
				  	return false;
			    if(amendtime != '' && pmsttime != ''){
					if(!compareTimes(name+3,amendtime,pmsttime))
					return false;
				}
				if(!compareTimes(name+4,pmsttime,pmendtime))
					return false;
			}
		}//if
	}//for
	return true;
}

function compareTimes(name,time1,time2){
	if(eval(time1[0]) < eval(time2[0])){
	}else{
		if(eval(time1[1]) < eval(time2[1])){
		}else{
			showMessage("js.scheduler.schedulerdashboard.toslottime.morethan.fromslottime");
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
	}
}

function cancel(checkbox,deleteDay){
	if(document.getElementById(checkbox.id).checked){
		document.getElementById(deleteDay).value=true;
	}else{
		document.getElementById(deleteDay).value=false;
	}
	if (document.getElementById(checkbox.id).checked) {
		checkbox.parentNode.parentNode.style.background = '#F2DCDC';
	} else {
		checkbox.parentNode.parentNode.style.background = '';
	}
}

function getAjaxTimingBWDates(){
	var fromdate = document.forms[0].finalfromdate.value;
	var todate = document.forms[0].finaltodate.value;
	var docid = document.forms[0].doctor.value;
	var reqObject = newXMLHttpRequest();
	var url ="../../pages/resourcescheduler/addeditappointments.do?method=ajaxTimingBetweenDates&fromdate="+fromdate+"&todate="+todate+"&docid="+docid;
	reqObject.open("POST",url.toString(), false);
	reqObject.send(null);
	if (reqObject.readyState == 4) {
		if ( (reqObject.status == 200) && (reqObject.responseText!=null) ) {
			var obj = (eval(reqObject.responseText));
			if(obj != null && obj != ''){
				var form = document.forms[0];
				for(var i=0;i<obj.length;i++){
					var item = obj[i];
					if(item["week_no"] == 1){
						form.sun1.value = convertTime(item["firsthalf_from"]);
						form.sun2.value = convertTime(item["firsthalf_to"]);
						form.sun3.value = convertTime(item["secondhalf_from"]);
						form.sun4.value = convertTime(item["secondhalf_to"]);
					}else if(item["week_no"] == 2){
						form.mon1.value = convertTime(item["firsthalf_from"]);
						form.mon2.value = convertTime(item["firsthalf_to"]);
						form.mon3.value = convertTime(item["secondhalf_from"]);
						form.mon4.value = convertTime(item["secondhalf_to"]);
					}else if(item["week_no"] == 3){
						form.tue1.value = convertTime(item["firsthalf_from"]);
						form.tue2.value = convertTime(item["firsthalf_to"]);
						form.tue3.value = convertTime(item["secondhalf_from"]);
						form.tue4.value = convertTime(item["secondhalf_to"]);
					}else if(item["week_no"] == 4){
						form.wed1.value = convertTime(item["firsthalf_from"]);
						form.wed2.value = convertTime(item["firsthalf_to"]);
						form.wed3.value = convertTime(item["secondhalf_from"]);
						form.wed4.value = convertTime(item["secondhalf_to"]);
					}else if(item["week_no"] == 5){
						form.thu1.value = convertTime(item["firsthalf_from"]);
						form.thu2.value = convertTime(item["firsthalf_to"]);
						form.thu3.value = convertTime(item["secondhalf_from"]);
						form.thu4.value = convertTime(item["secondhalf_to"]);
					}else if(item["week_no"] == 6){
						form.fri1.value = convertTime(item["firsthalf_from"]);
						form.fri2.value = convertTime(item["firsthalf_to"]);
						form.fri3.value = convertTime(item["secondhalf_from"]);
						form.fri4.value = convertTime(item["secondhalf_to"]);
					}else if(item["week_no"] == 7){
						form.sat1.value = convertTime(item["firsthalf_from"]);
						form.sat2.value = convertTime(item["firsthalf_to"]);
						form.sat3.value = convertTime(item["secondhalf_from"]);
						form.sat4.value = convertTime(item["secondhalf_to"]);
					}
				}
			}
		}
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
