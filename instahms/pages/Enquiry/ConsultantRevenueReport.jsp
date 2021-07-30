
<%@taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@page isELIgnored="false"%>
<%
response.setHeader("Pragma", "no-cache");
response.setHeader("Cache-Control", "no-store");
response.setHeader("Expires", "0");
%>
<html>
<head>
<title>Insta HMS</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">


<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="script" file="date_go.js"/>
<script language="javascript" type="text/javascript">


/*function for select current year  and current month*/
var  sysdate = getServerTime();
var cdate=sysdate.getDate();
var fulldate =getFullDay(sysdate.getDate());
var cmonth=getFullMonth(sysdate.getMonth());
var cyear=sysdate.getFullYear();
var cfulldate=fulldate+"-"+cmonth+"-"+cyear;


/*  for getting seven days back date from current date

    var fulldate;
    function dateCalculation(j){
    var now = new Date();
    now = new Date(now.getFullYear(),now.getMonth(),now.getDate(),0,0,0); // midnight
    for (i=0; i<j; i++) {
    var theDay = new Date(now.getTime()-(i*24*60*60*1000));
    fulldate = theDay.getDate()+"-"+(theDay.getMonth()+1)+"-"+theDay.getFullYear() ;
    }
    }

*/

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
		//openReport(); FOR AJAX REQUEST
		getlabRevenue(timeperiod);
	}
	return true;
}


function getlabRevenue(){
	if(!validateTimePeriod()){
		var name = '${requestScope.names}';
		var fromdate = document.getElementById("fdate").value;
		var todate = document.getElementById("todate").value;
		if(name=="Hospital Lab Revenue"){
			window.open("labrevenue.do?method=getLabRevenueReport&fromdate="+fromdate+"&todate="+todate+"&status=print");
		}else if(name=="Doctor Consultant Revenue  Report"){
			if(docId==null){
				alert("There are no Consultant doctors are available");
				return false;
			}else{
			window.open("consultantdocrevenue.do?method=getConsultantDoctorRevenue&fromdate="+fromdate+"&todate="+todate+"&doctorId="+docId+"&status=print");
			}}else{
			window.open("discountreport.do?method=getAllDiscounts&fromdate="+fromdate+"&todate="+todate+"&status=print");
		}
	}

}


function getLabrevenueExportReport(){
	if(!validateTimePeriod()){
		var name = '${requestScope.names}';
		var fromdate = getDateFormat(document.getElementById("fdate").value);
		var todate = getDateFormat(document.getElementById("todate").value);

		if(name=="Hospital Lab Revenue"){
			window.open("labrevenue.do?method=getExportToCSV&fromdate="+fromdate+"&todate="+todate+"&status=excel");
		}else if(name=="Doctor Consultant Revenue  Report"){
			if(docId==null){
				alert("There are no Consultant doctors are available");
				return false;
			}else{
			window.open("consultantdocrevenue.do?method=exportPayments&status=excel&fromdate="+fromdate+"&todate="+todate+"&doctorId="+docId);
			}
		}	else{
			window.open("discountreport.do?method=getAllDiscounts&fromdate="+fromdate+"&todate="+todate+"&status=excel");
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

	return true;
}

function getDoctorList(){
	if(	document.forms[0].docList!=null){
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
	var len = 	document.forms[0].docList.length;
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
</script>

</head>
<body onload="selectDay();getDoctorList()">
	<div class="pageHeader">
			${requestScope.names}
	</div>
	<c:set var="consultantdoctors" value="${doctorList}"/>
<form method="POST" ><!-- Main tab-->

        <div class="tipText" style="width:50em">
					This report gives detailed list of all revenue posted under the doctor consultation charges group between the given dates. The report is filtered on doctors with grouping for OP and IP patients along with totals for each group and selection. The doctor amount is calculated based on doctor fees defined in doctor masters . The report displays MR no, Patient Visit Id, Patient Name, Bill No, Bill Date, Department, Billed Amount, Discount, Paid Amount, Consultation percentage , Consultaion Fees, tds percentage and Total Doctor Amount.The TDS is defualt to 11.33% of the doctor amount.	</div>

<!-- main table  start -->
						<table align="center">
							<tr>
								<td colspan="2">Select a date range for the report</td>
							</tr>
							<tr>
								<td valign="top">
								<input type="radio" name="condition" id="day" onclick=" selectDays()" value="day" checked="checked">
									<label>Today</label><br/>
									<input type="radio" name="condition" id="week" onclick="selectDays()" value="week">
									<label>This week</label><br/>
									<input type="radio" name="condition" onclick="selectDays()" id="month" 	value="month">
									<label>This month</label><br/>
									<input type="radio" id="year" name="condition" onclick=" selectDays()" value="year">
									<label>This year</label><br/>
								</td>
								<td>
									<table>
										<tr>
											<td align="right">From :</td>
											<td><insta:datewidget name="fdate" value="today"/></td>
										</tr>
										<tr>
											<td align="right">To :</td>
											<td><insta:datewidget name="todate" value="today"/></td>
										</tr>
									</table>

								</td>
							</tr>
						</table>
						<c:choose>
						<c:when test="${not empty consultantdoctors}">
						<c:set var="docList" value="docList"/>
						<div>
						<table align="center" cellpadding="4">
							<tr>
								<td>Select Doctor</td>
								<td><select name="${docList}" multiple="multiple" onblur="getCheckValue()" selected>
									<c:forEach var="docList" items="${consultantdoctors}">
												<option value="${docList.DOCTOR_ID}">${docList.DOCTOR_NAME}</option>
									</c:forEach>
											</select>
											</td>
								</tr>
							</table>
						</div>
						</c:when>
						<c:otherwise>
							<c:set var="docList" value=""/>
						</c:otherwise>
					</c:choose>
						<table align="center" style="margin-top: 1em">
							<tr>
								<td>
 									<input type="button" name="print" id="print" value="Print" class="button" onclick="getlabRevenue();">
       								<input type="button" name="exporttocsv" id="exporttocsv" value="Export to CSV" class="button" onclick="getLabrevenueExportReport();">
								</td>
						</tr>
						</table>

</form>
</body>
</html>
