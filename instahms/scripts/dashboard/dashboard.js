
/*function for select current year  and current month*/
var  sysdate = getServerTime();
var cdate=getFullDay(sysdate.getDate());
var cmonth=getFullMonth(sysdate.getMonth());
var cyear=sysdate.getFullYear();
var cfulldate=cdate+"-"+cmonth+"-"+cyear;


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
	var weekNo=	weekNum;
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




/* function for selecting  datas radio button */

function getDates(){
	var startyeardate = "01"+"-"+"01"+"-"+(sysdate.getFullYear());
	document.getElementById("fdate").value="";
	document.getElementById("todate").value=cfulldate;

	//document.getElementById("summaryDiv").style.display="none";
}//end function
/* end of date selection radio button */


/* function for selecting  datas radio button */



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
		if(validateForm()){
			return false;
		}
	}else if(timeperiod=="yearDate"){
		if(validateForm()){
			return false;
		}
	}else if(timeperiod=="year"){
		if(validateForm()){
			return false;
		}
	}else if(timeperiod=="month"){
		if(validateForm()){
			return false;
		}
	}else if(timeperiod=="week"){
		if(validateForm()){
			return false;
		}
	}else {
		//openReport(); FOR AJAX REQUEST
		getDashborad(timeperiod);
	}
	return true;

}

function getDoctorList(){

	if(document.forms[0].docList!=null){
	for (var i=0;i<document.forms[0].docList.length;i++){
		document.forms[0].docList[i].selected=true;
	}
	getCheckValue();
	}
}

var docId;
function getCheckValue(){
	var ele ;
	var temp="";
	var len =   document.forms[0].docList.length;
	var options = document.forms[0].docList;
	for(var i=0;i<len;i++){
		if(options[i].selected == true){
			if(temp==""){
				temp=options[i].value;
				docId = "'"+temp+"'";
			}else{
				temp=options[i].value;
				docId+= ','+"'"+temp+"'";
			}
		}
	}

	return false;

}


function getDashboard(){
	var printerType = document.forms[0].printerType.value;
	if(validateTimePeriodNew())
	{

		var name =names;
		var fromdate = document.forms[0].fromDate.value;
		var todate =  document.forms[0].toDate.value;
		if(name=="Dashboard - PatientType"){
			window.open("dashboard.do?method=getDashboard&fromdate="+fromdate+"&todate="+todate );
		}else if(name=="Dashboard - Charge Revenue"){
			window.open("dashboard.do?method=getChargeTypeDashBoard&fromdate="+fromdate+"&todate="+todate);
		}else if(name=="Dashboard - Doctors"){
			window.open("dashboard.do?method=getDashboardforConsultantDoctor&fromdate="+fromdate+"&todate="+todate+"&doctorId="+docId);
		}else if(name=="Dashboard - Patient Demography"){
			window.open("dashboard.do?method=getDashboardForPatientDemography&fromdate="+fromdate+"&todate="+todate);
		}else if(name=="Implants Report"){
			var methodName = document.forms[0].printerType.value == 'text' ? 'getText' : 'getDashboardForImplantedPatients';
			window.open("dashboard.do?method="+methodName+"&fromdate="+fromdate+"&todate="+todate+"&printerType="+printerType);
		}else if(name=="Dashboard - Charge Collections"){
			window.open("dashboard.do?method=getDashboardChargeCollection&fromdate="+fromdate+"&todate="+todate);
		}
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
		alert("From Date should not greater than To Date");
		return false;
	}

	return true;
}

function getDeptList(){
	if(document.forms[0].deptList!=null){
	for (var i=0;i<document.forms[0].deptList.length;i++){
		document.forms[0].deptList[i].selected=true;
	}
	CheckDeptValue();
	}
}

var deptId;
function CheckDeptValue(){
	var ele ;
	var temp="";
	var len =   document.forms[0].deptList.length;
	var options = document.forms[0].deptList;
	for(var i=0;i<len;i++){
		if(options[i].selected == true){
			if(temp==""){
				temp=options[i].value;
				deptId = "'"+temp+"'";
			}else{
				temp=options[i].value;
				deptId+= ','+"'"+temp+"'";
			}
		}
	}

	return false;

}

function getuserList(){
	if(document.forms[0].userNameList!=null){
	for (var i=0;i<document.forms[0].userNameList.length;i++){
		document.forms[0].userNameList[i].selected=true;
	}
	CheckUserNames();
	}
}

var userName;
function CheckUserNames(){
	var ele ;
	var temp="";
	var len =   document.forms[0].userNameList.length;
	var options = document.forms[0].userNameList;
	for(var i=0;i<len;i++){
		if(options[i].selected == true){
			if(temp==""){
				temp=options[i].value;
				userName = "'"+temp+"'";
			}else{
				temp=options[i].value;
				userName+= ','+"'"+temp+"'";
			}
		}
	}

	return false;
}

function getCancelledReport() {

	var methodName = 'showReport';
	if (document.forms[0].printerType.value == 'text' ) {
		methodName = 'getText';
	}else {
		methodName = 'showReport';
	}

	var fromdate = document.forms[0].fromDate.value;
	var todate =  document.forms[0].toDate.value;
	var printerType = document.forms[0].printerType.value;
	if (fromdate=="")
		alert("Please enter the from-date value.");
	else if (todate=="")
		alert("Please enter the to-date value.");
	else if(getDateDiff(document.forms[0].fromDate.value,document.forms[0].toDate.value)<0)
		alert("From Date should not greater than To Date");
	else
		window.open("dashboardPrint.do?method="+methodName+"&fromdate="+fromdate+"&todate="+todate+"&doctorId="+docId+"&departmentId="+deptId+"&userName="+userName+"&printerType="+printerType);

}


function initDate(){
	document.getElementById('td').checked=true;
	document.forms[0].fromDate.value=cfulldate;
	document.forms[0].toDate.value=cfulldate;
}

function validateTimePeriodNew(){
	var fromdate = document.forms[0].fromDate.value;
	var todate =  document.forms[0].toDate.value;
	if (fromdate==""){
		alert("Please enter the from-date value.");
		return false;
	} else if (todate==""){
		alert("Please enter the to-date value.");
		return false;
	}
	else if(getDateDiff(document.forms[0].fromDate.value,document.forms[0].toDate.value)<0){
		alert("From Date should not greater than To Date");
		return false;
	}
		return true;
}

