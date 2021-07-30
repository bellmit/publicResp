<%@page import="java.util.List"%>
<%@taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@page import="com.insta.hms.common.Encoder" %>
<html>
<head>
<title>Insta HMS</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">

<insta:link type="script" file="date_go.js"/>
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="script" file="tableSearch.js"/>
<insta:link type="script" file="ajax.js"/>
	<script language="javascript" type="text/javascript">



function report(){
			if(validateForm()){
			   if(document.getElementById("mrno").value==""){
			   alert("Select the MR Number");
			   document.getElementById("mrno").focus();
			   document.getElementById("mrno").value="";
			   return false;
			}
			 if(document.getElementById("patient_id").value==""){
			   alert("Select the Patient Id");
			   document.getElementById("patient_id").focus();
			   document.getElementById("patient_id").value="";
			   return false;
			}
			   var status=null;
			   var patient_id = document.getElementById('patient_id').options[document.getElementById('patient_id').options.selectedIndex].text;
		       var total_amt =document.getElementById('patient_id').options[document.getElementById('patient_id').options.selectedIndex].value;
			 	window.open("report.do?method=getDischargeBillReport&patientId="+patient_id+"&totalAmount="+total_amt+"&status=Discharge");
				document.forms[0].fromDate.value="";
				document.forms[0].toDate.value="";
				document.getElementById("mrno").options[0].selected=true;
				document.getElementById("patient_id").options[0].selected=true;
			}
		}
			   function dateDiff(id1,id2)
  {

   var date1, date2;
   var month1, month2;
   var year1, year2;


  var x = document.getElementById(id2).value;//  to date
   var y = document.getElementById(id1).value;// from date



   date2 = y.substring (0, y.indexOf ("-"));
   month2 = findMonth(y.substring (y.indexOf ("-")+1, y.lastIndexOf ("-")));
   year2 = y.substring (y.lastIndexOf ("-")+1, y.length);

   date1 = x.substring (0, x.indexOf ("-"));
   month1 = findMonth(x.substring (x.indexOf ("-")+1, x.lastIndexOf ("-")));
   year1 = x.substring (x.lastIndexOf ("-")+1, x.length);

   if (year1 > year2) return 1;
   else if (year1 < year2) return -1;
   else if (month1 > month2) return 1;
   else if (month1 < month2) return -1;
   else if (date1 > date2) return 1;
   else if (date1 < date2) return -1;
   else return 0;


  }


  function validateForm()
  {
   if(document.forms[0].fromDate.value=="")
	  {
	  alert("Enter from date");
	  document.forms[0].fromDate.focus();
	  return false;
	  }

	  if(document.forms[0].fromDate.value!="")
	  {

	     if(isFutureDate("fdate")< 0)
	     {
	      alert("Selected from date should not be greater than current date");
	      return false;
	      }
	  }
	  if(document.forms[0].toDate.value=="")
	  {
	  alert("Enter to date");
	  document.forms[0].toDate.focus();
	  return false;
	  }


	  if(document.forms[0].toDate.value!="")
	  {
	     if(isFutureDate("todate")<0)
	     {
	      alert("Selected Todate should not be greater than current date");
	      return false;
	      }
	  }
	  if(dateDiff("fdate","todate")<0)
	  {
	  alert("From date should be less than to date")
	  return false;
	  }


  return true;
  }
  function isFutureDate(id)
	{

	var date1, date2;
   var month1, month2;
   var year1, year2;
   var today = new Date();

   var selectedDate = document.getElementById(id).value;
   date2 = selectedDate.substring (0, selectedDate.indexOf ("-"));
   month2 = findMonth(selectedDate.substring (selectedDate.indexOf ("-")+1, selectedDate.lastIndexOf ("-")));
   year2 = selectedDate.substring (selectedDate.lastIndexOf ("-")+1, selectedDate.length);
   month1 = today.getMonth()+1;
   date1 = today.getDate();
   year1 = today.getFullYear();

   if (year1 > year2) return 1;
   else if (year1 < year2) return -1;
   else if (month1 > month2) return 1;
   else if (month1 < month2) return -1;
   else if (date1 > date2) return 1;
   else if (date1 < date2) return -1;
   else return 0;
}

  function findMonth(month){
     var mon;

if(month=='JAN'){
mon=1;
}
else if(month=='FEB'){
mon=2;
}
else if(month=='MAR'){
mon=3;
}
else if(month=='APR'){
mon=4;
}
else if(month=='MAY'){
mon=5;
}
else if(month=='JUN'){
mon=6;
}
else if(month=='JUL'){
mon=7;
}
else if(month=='AUG'){
mon=8;
}
else if(month=='SEP'){
mon=9;
}
else if(month=='OCT'){
mon=10;
}
else if(month=='NOV'){
mon=11;
}
else if(month=='DEC'){
mon=12;
}
        return mon;


  }

   // ajax script for getting mrno number beetween the dates
  function getMRNO(){
  		 var status="";
  		 var from_dt = document.forms[0].fromDate.value;
 		 var to_dt = document.forms[0].toDate.value;

 		 if(window.XMLHttpRequest){
 		 	req =new XMLHttpRequest();
 		 }
 		 else if(window.ActiveXObject){
 		 	req = new ActiveXObject("MSXML2.XMLHTTP");
 		 }
 		req.onreadystatechange = onResponse;
	  	var url="/HMSNew/pages/Enquiry/report.do?method=getMrno&fromDate="+from_dt+"&toDate="+to_dt+"&status=Discharge";
  		req.open("POST",url.toString(),true);
  		req.setRequestHeader("Content-Type" , "text/xml");
  		req.send(null);
  }

  function checkReadyState(req){
  	if(req.readyState == 4){
  		if(req.status == 200){
  			return true;
  		}
  	}
  }

  var str=null;
  var x=null;
  function onResponse(){
  	if(checkReadyState(req)){
		  str=req.responseXML;
		  if(str!=null){
		  	getXML();
		  }
  	  }
  }

  var doc;
  function getXML(){
	  	doc = str;
 	x=doc.documentElement;
 	getMRNo();
 }

 function getMRNo(){

 	x=doc.documentElement;
 	var len = x.childNodes[0].childNodes.length;
 	var obj=document.getElementById("mrno");
 	var p=0;
 	document.forms[0].mrno.length=len+1;
	document.forms[0].mrno.options[0].value="";
//	document.forms[0].mrno.options[0].selectedIndex=0;
 	document.forms[0].mrno.options[0].text="...Select one...";

 	for(i=1;i<=len;i++){
 		document.forms[0].mrno.options[i].text = x.childNodes[0].childNodes[i-1].attributes.getNamedItem('class1').nodeValue;
		document.forms[0].mrno.options[i].value = x.childNodes[0].childNodes[i-1].attributes.getNamedItem('class1').nodeValue;
 		p++;
 	}
 }
//end of ajax script


// ajax script for getting patientid for the mrno

 function getPatientId(){
  		var mrno=document.forms[0].mrno.value;
 		 if(window.XMLHttpRequest){
 		 	req =new XMLHttpRequest();
 		 }
 		 else if(window.ActiveXObject){
 		 	req = new ActiveXObject("MSXML2.XMLHTTP");
 		 }
 		req.onreadystatechange = onResponse1;
	  	var url="/HMSNew/pages/Enquiry/report.do?method=getPatientId&mrNo="+mrno+"&status=Discharge";
  		req.open("POST",url.toString(),true);
  		req.setRequestHeader("Content-Type" , "text/xml");
  		req.send(null);
  }

  function checkReadyState1(req){
  	if(req.readyState == 4){
  		if(req.status == 200){
  			return true;
  		}
  	}
  }

  var str=null;
  var x=null;
  function onResponse1(){
  	if(checkReadyState1(req)){
		  str=req.responseXML;
		  if(str!=null){
		  	getXML1();
		  }
  	  }
  }

  var doc;
  function getXML1(){
	  	doc = str;
 	x=doc.documentElement;
 	getPatientid();
 }

 function getPatientid(){
 	x=doc.documentElement;
 	var len = x.childNodes[0].childNodes.length;
 	var p=0;
 	document.forms[0].patient_id.length=len+1;
	document.forms[0].patient_id.options[0].value="";
//	document.forms[0].patient_id.options[0].selectedIndex=0;
 	document.forms[0].patient_id.options[0].text="...Select one...";

 	for(i=1;i<=len;i++){
 		document.forms[0].patient_id.options[i].text = x.childNodes[0].childNodes[i-1].attributes.getNamedItem('class1').nodeValue;
 		document.forms[0].patient_id.options[i].value = x.childNodes[0].childNodes[i-1].attributes.getNamedItem('class2').nodeValue;
 		p++;
 	}
 }
//end of ajax script





	</script>

</head>
<body  oncopy="return false" onbeforecut="return false" oncut="return false" >

<form method="POST">
<input type="hidden" name="method" value="<%= Encoder.cleanHtmlAttribute((String)request.getParameter("method")) %>"/>




<!-- Main tab--> <TABLE width="100%" height="100%" border="0" cellpadding="0" cellspacing="0">
                    <TR>
                      <TD width="10" height="20"><insta:link type="image" file="sub_img01.gif"/></TD>
                      <TD height="20" background="../../images/sub_img03.gif" class="subtitle" width="100%" align="center">Discharge Bill report</TD>
                      <TD width="10" height="20"><insta:link type="image" file="sub_img02.gif"/></TD>
                    </TR>

                    <TR>
                      <TD width="10" background="../../images/sub_img07.gif" height="100%"><insta:link type="image" file="spacer.gif"/></TD>
                      <TD valign="top" bgcolor="#fbe6d3" width="100%" height="100%">
     <!-- Center tab-->    <TABLE width="100%" border="0" cellpadding="0" cellspacing="0" class="tabletext" height="100%">
                              <TR><TD height="9" width="100%"></TD></TR>
                              <TR><TD class="leftnavbg2" width="100%" valign="top" align="middle">
                                 <table width="820" height="100%" border="0" cellpadding="0" cellspacing="0" >
           <!-- Constant Cell-->  <tr width="100%" valign="top"><td>

                             <table width="100%" height="111">
                          		<tr width="100%" height="19"><td colspan="2">&nbsp;</td></tr>
                          		<tr width="100%" height="19"><td colspan="2">&nbsp;</td></tr>
                          		<tr width="100%" height="19"><td colspan="2">&nbsp;</td></tr>
                          		<tr width="100%" height="19"><td colspan="2">&nbsp;</td></tr>
                          		<tr width="100%">
                          			<td width="47%" align="right" height="19"><span style="font-family:Verdana; font-size:12; color:#800000">From</span>&nbsp;&nbsp;&nbsp;
						<label>From</label>
						Date :<insta:datewidget name="fromDate" id="fdate"  />
						<label>To</label>
						Date :<insta:datewidget name="toDate" id="todate" />
                             	</tr>
                          		<tr width="100%" height="19"><td colspan="2">&nbsp;</td></tr>
                          		<tr width="100%" height="19">
                          		<td align="center" colspan="2"><span style="font-family:Verdana; font-size:12px; color:#800000">MR Number</span>&nbsp;&nbsp;
                          		  			<select name="mrno" id="mrno" style="width:100" class="forminput"  onchange="getPatientId();">
                          					     <option value="">......Select......</option>
                          				   </select>
								</td></tr>
                          		<tr width="100%" height="19"><td colspan="2">&nbsp;</td></tr>
                          		<tr width="100%" height="19"><td colspan="2">&nbsp;</td></tr>
                          		<tr width="100%" height="19">
                          		<td align="center" colspan="2"><span style="font-family:Verdana; font-size:12px; color:#800000">Patient Id</span>&nbsp;&nbsp;
                          		  			<select name="patient_id" id="patient_id" style="width:100" class="forminput" >
                          					<option value="">......Select......</option>
                          				   </select>
								</td></tr>
                          		<tr width="100%" height="19"><td colspan="2">&nbsp;</td></tr>
                          		<tr width="100%" height="19"><td colspan="2">&nbsp;</td></tr>
                          		<tr width="100%" height="19"><td colspan="2">&nbsp;</td></tr>

                          		<tr width="100%">

                          			<td width="63%" height="15" align="center" colspan="2">
                                    <input type="button" value="Report" name="B1" class="button" onclick="report()"></td>
                          		</tr>
                          	</table>

           <!-- Constant Cell-->  </td></tr></table>
                               </TD></TR>
     <!-- Center tab-->    </TABLE>
                       </TD>
                      <TD width="10" background="../../images/sub_img08.gif" height="100%"><insta:link type="image" file="spacer.gif"/></TD>
                    </TR>

                    <TR>
                      <TD width="10" height="1"><insta:link type="image" file="sub_img04.gif"/></TD>
                      <TD height="1" background="../../images/sub_img06.gif" width="796"><insta:link type="image" file="spacer.gif"/></TD>
                      <TD width="10" height="1"><insta:link type="image" file="sub_img05.gif"/></TD>
                    </TR>

<!-- Main tab--> </TABLE>


        </FORM>
  </body>
</html>
