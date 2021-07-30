var  sysdate = getServerTime();
var cdate=sysdate.getDate();
var fulldate =getFullDay(sysdate.getDate());
var cmonth=getFullMonth(sysdate.getMonth());
var cyear=sysdate.getFullYear();
var cfulldate=fulldate+"-"+cmonth+"-"+cyear;


function init(){
	selectDay();


}

var fulldate;
function dateCalculation(j){
	var now = new Date();
	now = new Date(now.getFullYear(),now.getMonth(),now.getDate(),0,0,0); // midnight
	for (i=0; i<j; i++) {
		var theDay = new Date(now.getTime()-(i*24*60*60*1000));
		fulldate = theDay.getDate()+"-"+(theDay.getMonth()+1)+"-"+theDay.getFullYear() ;
	}
}



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
	document.forms[0].fdate.value=cfulldate;
	document.forms[0].todate.value=cfulldate;
		selectordeselectAll();
}

function getWeek(){
	var k=0;
	var cdate = getCurrentDate();
	var curdate = new Date();
	var weekNum = YAHOO.widget.DateMath.getWeekNumber(curdate,0,1);
	var sss = getDateRangeOfWeek(weekNum);
	return weekNum;
}//end function

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
		getserviceRevenue(timeperiod);
	}
	return true;
}


function getserviceRevenue(){

		var msg=validateDeptNames();
		if(msg!=null)
			alert( msg);
		else{
				var fromdate = document.getElementById("fdate").value;
				var todate = document.getElementById("todate").value;
				var flag=validateFromToDate(document.forms[0].fdate, document.forms[0].todate);
				if(flag){
					window.open("servicerevenue.do?method=getServiceRevenueReport&fromdate="+fromdate+"&todate="+todate+"&status=print"+"&sdept="+ddeptIds);
				}
		}
		if(names=="Patient Discount Report"){
			window.open("discountreport.do?method=getAllDiscounts&fromdate="+fromdate+"&todate="+todate+"&status=print");
		}else if(names=="Rate Variation Report"){
			window.open("discountreport.do?method=getRateVariationReport&fromdate="+fromdate+"&todate="+todate+"&status=print");
		}

}


function getServicerevenueExportReport(){
	{
		var msg=validateDeptNames();
		if(msg!=null)
			alert( msg);
		else{
				var fromdate = document.getElementById("fdate").value;
				var todate = document.getElementById("todate").value;
				var flag=validateFromToDate(document.forms[0].fdate, document.forms[0].todate);
				if(flag){
					window.open("servicerevenue.do?method=getExportToCSV&fromdate="+fromdate+"&todate="+todate+"&status=excel"+"&sdept="+ddeptIds);
				}
		}
		 if(names=="Patient Discount Report"){
			window.open("discountreport.do?method=getAllDiscounts&fromdate="+fromdate+"&todate="+todate+"&status=excel");
		}else if(names=="Rate Variation Report"){
			window.open("discountreport.do?method=getRateVariationReport&fromdate="+fromdate+"&todate="+todate+"&status=excel");
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

	if ( names == 'Services Revenue Report'){
		var msg  = validateDeptNames();
		if (msg == null){
		}else{
			alert(msg);
			return false;
		}


	}

	return true;
}


function selectordeselectAll(){
	var length = document.forms[0].servdept.length;
	var disabled = document.forms[0].allDept.checked;
	for (var i=0;i<length;i++){
		document.forms[0].servdept[i].selected = disabled;
	}
}


var ddeptIds = "";
function validateDeptNames(){
	var temp="";
	var deptSelected = false;
	var len = document.forms[0].servdept.length;
	var options = document.forms[0].servdept;
	var deptAll = document.forms[0].allDept;

	if (deptAll.checked){
		deptSelected=true;
		ddeptIds = "all";
	}else{
		for (var i=0;i<len;i++){
			if (options[i].selected == true){
				deptSelected=true;
				if(temp==""){
					temp = options[i].value;
					ddeptIds = "'"+temp+"'";
				}else{
					temp = options[i].value;
					ddeptIds=ddeptIds+ ','+"'"+temp+"'";
				}
			}
		}
	}
	if(!deptSelected){
			return "Select at least one department for the report";
	}

}

function deselectAll(){
	document.forms[0].allDept.checked = false;
}

