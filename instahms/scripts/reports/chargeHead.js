/*function for select current year  and current month*/
var  sysdate = getServerTime();
var cdate=sysdate.getDate();
var fulldate =getFullDay(sysdate.getDate());
var cmonth=getFullMonth(sysdate.getMonth());
var cyear=sysdate.getFullYear();
var cfulldate=fulldate+"-"+cmonth+"-"+cyear;

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
	}else if(timeperiod=="day"){
		if(validateform()){
			return false;
		}
	}else if(timeperiod=="yearDate"){
		if(validateform()){
			return false;
		}
	}else if(timeperiod=="year"){
		if(validateform()){
			return false;
		}
	}else if(timeperiod=="month"){
		if(validateform()){
			return false;
		}
	}else if(timeperiod=="week"){
		if(validateform()){
			return false;
		}
	}else {

	}

	return true;
}

var chargeHead ;
function getChargeHeads(){
	var ele;
	var temp="";
	var len =document.forms[0].chargeHead.length;
	var options = document.forms[0].chargeHead;

	for (i=0;i<len;i++){
		if (options[i].selected ==true){
			if(temp==""){
				temp = options[i].value;
				chargeHead = "'"+temp+"'";
			}else{
				temp = options[i].value;
				chargeHead+=','+"'"+temp+"'";
			}
		}
	}
	return false;
}
function getChargeRevenueExportReport(){

	if(!validateTimePeriod()){
		var fromdate = getDateFormat(document.getElementById("fdate").value);
		var todate = getDateFormat(document.getElementById("todate").value);
		var chargeGroup = document.getElementById("chargeGroup").value;
		var report = document.getElementById("report").value;
		if(report == "total"){
		window.open("chargehead.do?method=getChargeRevenueCSV&fromdate="+fromdate+"&todate="+todate+"&chargeHead="+chargeHead+"&chargeGroup="+chargeGroup+"&visitType="+visitType);
		}else if (report == "details") {
		window.open("chargehead.do?method=getChargeRevenueDetailCSV&fromdate="+fromdate+"&todate="+todate+"&chargeHead="+chargeHead+"&chargeGroup="+chargeGroup+"&visitType="+visitType);

		}

	}

}

function validateform(){
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

	 if (!checkPatientType())  return false ; 

	return true;
}

function loadChargeHeads(chargeGroup){
	var group = chargeGroup.value;
	var chargeHeadSelect = document.getElementById("chargeHead");
	chargeHeadSelect.length = 0;
	var optIndex =0;
	for (var i=0;i<jChargeHeads.length;i++){
		var charge = jChargeHeads[i];
		if(charge.CHARGEGROUP_ID == chargeGroup){
			chargeHeadSelect.length +=1;
			chargeHeadSelect.options[optIndex].value=charge.CHARGEHEAD_ID;
			chargeHeadSelect.options[optIndex].text=charge.CHARGEHEAD_NAME;
			chargeHeadSelect.options[optIndex].selected=true;
			optIndex++;
		}
	}
	getChargeHeads();
}

function hideChargeHead(){
	var cgGroup = document.forms[0].chargegroup.options[document.forms[0].chargegroup.selectedIndex].value;
	var cg = document.forms[0].chargegroup.options[document.forms[0].chargegroup.selectedIndex].value;
	if (cg=="all"){
		document.getElementById("chargeheadDiv").style.display="none";
	}else{
		document.getElementById("chargeheadDiv").style.display="block";
	}
	loadChargeHeads(cgGroup);
}

var visitType;
function checkPatientType(){
	var chkIP = document.forms[0].ip.checked;
	var chkOP = document.forms[0].op.checked;
	var chkRetail = document.forms[0].retail.checked;

	if ((chkIP==true) || (chkOP==true) || (chkRetail==true)){
		visitType="";
		if(chkIP==true){
			if(visitType!="") visitType=visitType+",";
			visitType = visitType +"'i'";
		}
		if(chkOP==true){
			if(visitType!="") visitType=visitType+",";
			visitType = visitType +"'o'";
		}
		if(chkRetail==true){
			if(visitType!="") visitType=visitType+",";
			visitType = visitType +"'r'";
		}
	}else{
		alert("Select Patient Type")
		return false;
	}
	return true;
}

function getChargeRevenuePrint(){
	if(!validateTimePeriod()){
		var chargeGroup = document.getElementById("chargeGroup").value;
		var fromdate = document.getElementById("fdate").value;
		var todate = document.getElementById("todate").value;
		var report = document.getElementById("report").value;
		if(report=="total"){
		window.open("chargehead.do?method=getChargeRevenue&fromdate="+fromdate+"&todate="+todate+"&chargeHead="+chargeHead+"&chargeGroup="+chargeGroup+"&visitType="+visitType);
		}else if(report =="details") {
		window.open("chargehead.do?method=getChargeRevenueDetails&fromdate="+fromdate+"&todate="+todate+"&chargeHead="+chargeHead+"&chargeGroup="+chargeGroup+"&visitType="+visitType);

		}
	}
}

