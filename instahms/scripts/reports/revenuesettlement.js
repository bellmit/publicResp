/*function for select current year  and current month*/
var  sysdate = getServerTime();
var cdate=getFullDay(sysdate.getDate());
var cmonth=getFullMonth(sysdate.getMonth());
var cyear=sysdate.getFullYear();
var cfulldate=cdate+"-"+cmonth+"-"+cyear;

/* function for selecting day month ,year dates radio buttons*/

function  selectDays(){
	if(document.getElementById("day").checked==true){
		document.getElementById("fdate").value=cfulldate;
		document.getElementById("todate").value=cfulldate;
	}else if(document.getElementById("month").checked==true){
		var startmonthdate = "01"+"-"+getFullMonth(sysdate.getMonth())+"-"+(sysdate.getFullYear());
		document.getElementById("fdate").value=startmonthdate;
		document.getElementById("todate").value=cfulldate;
	}else if(document.getElementById("year").checked==true){
		var startyeardate = "01"+"-"+"01"+"-"+(sysdate.getFullYear());
		document.getElementById("fdate").value=startyeardate;
		document.getElementById("todate").value=cfulldate;
	}else if(document.getElementById("week").checked==true){
		var weeknum= getWeek();
		var rangeIsFrom= getDateRangeOfWeek(weeknum);
		document.getElementById("fdate").value=rangeIsFrom;
		document.getElementById("todate").value=cfulldate;
	}//end if else
}//end function

function selectDay(){
	document.getElementById("fdate").value=cfulldate;
	document.getElementById("todate").value=cfulldate;
}


/* end of radio button selection*/

/*start of current week selection*/
function getWeek(){
	var k=0;
	var cdate = getCurrentDate();
	var curdate = new Date();
	var weekNum = YAHOO.widget.DateMath.getWeekNumber(curdate,0,1);
	var sss = getDateRangeOfWeek(weekNum);
	return weekNum;
}//end function

/*end of current week selection*/
var rangeIsFrom="";
var rangeIsTo="";
function getDateRangeOfWeek(weekNum){
	var weeknum=weekNum;
	var weekNo= weekNum;
	var d1 = new Date();
	numOfdaysPastSinceLastMonday = eval(d1.getDay()- 1);
	d1.setDate(d1.getDate() - numOfdaysPastSinceLastMonday);
	var weekNoToday = YAHOO.widget.DateMath.getWeekNumber(d1,0,1);
	var weeksInTheFuture = eval( weekNo - weekNoToday );
	fullday = getFullDay(d1.getDate());
	fullmonth = getFullMonth(d1.getMonth());
	rangeIsFrom = fullday+"-" + fullmonth + "-" + d1.getFullYear();
	d1.setDate(d1.getDate() + 6);
	fullday = getFullDay(d1.getDate());
	rangeIsTo =fullday +"-" +fullmonth + "-" + d1.getFullYear() ;
	return rangeIsFrom;

}//end function



function getDates(){
	var startyeardate = "01"+"-"+"01"+"-"+(sysdate.getFullYear());
	document.getElementById("fdate").value="";
	document.getElementById("todate").value=cfulldate;

}//end function
/* end of date selection radio button */

function validateTimePeriod() {
	var timeperiod ="";
	for(var i =0;i<document.forms[0].condition.length;i++){
		if(document.forms[0].condition[i].checked){
			timeperiod=document.forms[0].condition[i].value;

		}
	}
	if(!timeperiod){
		alert("Select Time Period");
	}else if(timeperiod=="Day"){
		if(validateForm()){
			return true;
		} else {
			return false;
		}
	}else if(timeperiod=="yearDate"){
		if(validateForm()){
			return true;
		} else {
			return false;
		}
	}else if(timeperiod=="year"){
		if(validateForm()){
			return true;
		} else {
			return false;
		}
	}else if(timeperiod=="month"){
		if(validateForm()){
			return true;
		} else {
			return false;
		}
	}else if(timeperiod=="week"){
		if(validateForm()){
			return true;
		} else {
			return false;
		}
	}else {
	}

}


var bStatus="";

function getBillStatus(){
	var billStatus;
	var chkC = document.getElementById("closed").checked;
	var chkF = document.getElementById("finalized").checked;
	var chkS = document.getElementById("settled").checked;

	if ( (chkC==true) || (chkF==true) || (chkS==true) ){
		billStatus="";

		if (chkF==true){
			var final = document.getElementById("finalized").value;
			if(billStatus!="") billStatus = billStatus +",";
			billStatus=billStatus +final;
		}//end F

		if (chkS==true){
			var settle = document.getElementById("settled").value;
			if(billStatus!="") billStatus = billStatus +",";
			billStatus = billStatus +settle; 
		}//end S

		if ((chkC==true)){
			var closed = document.getElementById("closed").value;
			if(billStatus!="") billStatus= billStatus +","; 
			billStatus= billStatus +closed;  
		}//end C

	}else{
		alert("Select BillStatus");
		return false;
	}
	bStatus=billStatus;
}//end function

function getCheckList(){
	if(validateTimePeriod()){
		var fromdate =  document.getElementById("fdate").value;
		var todate = document.getElementById("todate").value;
		window.open("revenuesettlements.do?method=getRevenueSettlementReport&fromdate="+fromdate+"&todate="+todate+"&billStatus="+bStatus);
	}
}


function getRevenueExportReport(){
	if(validateTimePeriod()){
		var fromdate =  document.getElementById("fdate").value;
		var todate = document.getElementById("todate").value;
 window.open("revenuesettlements.do?method=exportRevenueSettlementReport&fromdate="+fromdate+"&todate="+todate+"&billStatus="+bStatus);
		return true;
	}else{
		return false;
	}
}

function validateForm(){
	if (document.getElementById("fdate").value == ""){
		alert("Enter fromdate" );
		return false;
	}
	if (document.getElementById("todate").value == ""){
		alert("Enter todate ");
		return false;
	}

	if (!doValidateDateField(document.getElementById("fdate"))){
		return false;
	}
	if (!doValidateDateField(document.getElementById("todate"))){
		return false;
	}

	var msg = validateDateStr(document.getElementById("fdate").value,"past");
	if (msg == null){
	}else{
		alert("From "+msg);
		return false;
	}

	var msg = validateDateStr(document.getElementById("todate").value,"past");
	if (msg == null){
	}else{
		alert("To "+msg);
		return false;
	}

	if(getDateDiff(document.getElementById("fdate").value,document.getElementById("todate").value)<0){
		alert("From date should not greater than Todate");
		return false;
	}

	return true;
}

