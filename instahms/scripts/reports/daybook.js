function getSelectedUser(){
	var selectedUser = document.forms[0].counterUserName.options[document.forms[0].counterUserName.selectedIndex].value;
	if(selectedUser=='all'){
		var temp="";
		var element = document.forms[0].counterUserName.length;
		var options = document.forms[0].counterUserName
			for(var j=1;j<element;j++){
				if(temp==""){
					temp=options[j].value;
					deptid="'"+temp+"'";
				}//end of if
				else if(temp==options[j].value){}
				else{
					temp=options[j].value;
					deptid+=','+"'"+temp+"'";
				}//end of else
			}//end of for loop
	}// end of if
	else{
		getCheckedValues();
	}//end of else
}//end of function

function onInit(){
	fromDate = document.forms[0].fromDate;
	toDate = document.forms[0].toDate;
	document.forms[0].fromTime.value = '00:00:00';
	document.forms[0].toTime.value = '23:59:59';
	setDateRangeToday(fromDate, toDate);
	clearForm();
}
function clearForm(){
	var bf = document.getElementById("billFrom");
	var bt = document.getElementById("billTo");
	var rf = document.getElementById("receiptFrom");
	var rt = document.getElementById("receiptTo");
	if (bf.value != "") bf.value = "";
	if (bt.value != "") bt.value = "";
	if (rf.value != "") rf.value = "";
	if (rt.value != "") rt.value = "";

}

var visitType;
function getPatientType(){

	var chkIP = document.getElementById("ip").checked;
	var chkOP = document.getElementById("op").checked;
	var chkOthers = document.getElementById("others").checked;
	if((chkIP==true) && (chkOP==true) && (chkOthers==true)){
		visitType = "all";
	}else if( (chkIP==true) || (chkOP==true) || (chkOthers==true) ) {
		visitType="";

		if( (chkIP==true)){
			if(visitType!="") visitType= visitType+",";
			visitType = visitType + "'i'";
		}
		if( (chkOP==true)){
			if(visitType!="") visitType= visitType+",";
			visitType = visitType + "'o'";
		}
		if( (chkOthers==true)){
			if(visitType!="") visitType= visitType+",";
			visitType = visitType + "'t','r',' '";
		}

	}else{
		alert("Select Visit Type");
		return false;
	}
	return true;
}



var deptid="";
function getCheckedValues(){
	var temp="";
	var element = document.forms[0].counterUserName.length;
	var options = document.forms[0].counterUserName
		for(var j=0;j<element;j++){
			if(options[j].selected == true){
				if(temp==""){
					temp=options[j].value;
					deptid="'"+temp+"'";
				}else if(temp==options[j].value){
				}else{
					temp=options[j].value;
					deptid+=','+"'"+temp+"'";
				}//end of else
			}//end of if
		}//end of for loop

}//end of function


function validateForm(){
	if (roleId == 1 || roleId == 2 || UsrCounterDayBookAccess == 'A') {
		if (document.getElementById("counterid").value == ""){
			alert("Select Counter Name");
			return false;
		}
	} else {
		if (counter_name == ""){
			alert("User has no counter, report will be empty");
			return false;
		}
	}
	var valid = validateFromToDate(fromDate, toDate);
	if (!valid) return false;

	var fromtime = document.forms[0].fromTime;
	var totime = document.forms[0].toTime;
	var validFromTime = validateTime(fromtime);
	if (!validFromTime) return false;
	var validToTime = validateTime(totime);
	if (!validToTime) return false;
	return true;
}

function onChangeDateRange(sel) {
	var type = sel.value;
	if (type == "pd") { setDateRangeYesterday(fromDate, toDate); }
	else if (type == "td") { setDateRangeToday(fromDate, toDate); }
	else if (type == "pm") { setDateRangePreviousMonth(fromDate, toDate); }
	else if (type == "tm") { setDateRangeMonth(fromDate, toDate); }
	else if (type == "py") { setDateRangePreviousFinancialYear(fromDate, toDate); }
	else if (type == "ty") { setDateRangeFinancialYear(fromDate, toDate); }
	sel.selectedIndex = 0;
}



function getReport(){

	var methodName = 'showReport';
	if (document.forms[0].printerType.value == 'text' ) {
		methodName = 'getText';
	}else {
		methodName = 'showReport';
	}

	if (validateForm()) {
		var counterid =document.getElementById("counterid").value;
		var userName = document.getElementById("counterUserName").value;
		var fDate = fromDate.value;
		var tDate = toDate.value;
		var fromTime = document.forms[0].fromTime.value;
		var toTime = document.forms[0].toTime.value;
		var billFrom = document.forms[0].billFrom.value;
		var billTo = document.forms[0].billTo.value;
		var receiptFrom = document.forms[0].receiptFrom.value;
		var receiptTo = document.forms[0].receiptTo.value;
		var payMode=document.forms[0].payMode.value;
		var printerType = document.forms[0].printerType.value;
		var centerFilter = document.forms[0].centerFilter.value;
		var actionId = document.forms[0]._actionId.value;

		if (getPatientType()) {
			window.open("daybookreport.do?_method="+methodName
				+"&counterid="+counterid
				+"&page=Day%20Book&status=print&user="+userName
				+"&visitType="+visitType+"&fromDate="+fDate+"&toDate="+tDate
				+"&fromTime="+fromTime+"&toTime="+toTime
				+"&billFrom="+billFrom+"&billTo="+billTo
				+"&receiptFrom="+receiptFrom+"&receiptTo="+receiptTo
				+"&paymentMode="+payMode+"&printerType="+printerType+"&centerFilter="+centerFilter+"&actionId="+actionId);
		}
	}
}

function validateTime(timeField) {
	var strTime = timeField.value;
	var timePattern = /[0-9]:[0-9]/;
	var regExp = new RegExp(timePattern);
	if (strTime == '') {
		alert("Select time period");
		return false;
	}else {
		if (regExp.test(strTime)) {
			var strHours = strTime.split(':')[0];
			var strMinutes = strTime.split(':')[1];
			var strSec = strTime.split(':')[2];
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
			if ((strSec == '' || strSec == null || strSec =='undefined' || !isInteger (strSec)  )  ) {
				alert("Incorrect time format : second is not a number");
				timeField.focus();
				return false;
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
			if ( strSec && (parseInt(strSec) > 59) || (parseInt(strSec) < 0)) {
				alert("Incorrect second : please enter 0-59 for second");
				timeField.focus();
				return false;
			}
			if(strMinutes.length !=2){
				alert("incorrect minutes: please enter 2 digit minutes");
				return false;
			}
			if(strSec && strSec.length !=2){
				alert("incorrect second: please enter 2 digit second");
				return false;
			}
		}else {
			alert("Incorrect time format : please enter HH:MI:SS");
			timeField.focus();
			return false;
		}
		return true;
	}
}
