<%@page import="java.util.List"%>
<%@taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<html>
<head>
<title>Insta HMS</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<insta:link type="css" file="aw.css"/>
<insta:link type="script" file="date_go.js"/>
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="script" file="tableSearch.js"/>
<insta:link type="script" file="ajax.js"/>
<insta:link type="script" file="aw.js"/>

	<script language="javascript" type="text/javascript">

function getDiag()
{
 if(document.forms[0].deptId.value==""){
    document.forms[0].testId.disabled=true;
 	return;
 }
 if(window.XMLHttpRequest)
    {
    reqObject=new  XMLHttpRequest();
    }
    else if(window.ActiveXObject)
    {
      reqObject=new ActiveXObject("MSXML2.XMLHTTP");
    }


    if(reqObject)
    {
    reqObject.onreadystatechange=function()
	{

	if(reqObject.readyState == 4 && reqObject.status == 200)
	{
	 respXML=reqObject.responseXML;

	if(respXML!=null)
	{
	getXMLData();
	}
	}//if
	}  //function
    var url="/HMSNew/pages/Enquiry/diagreport.do?method=getDiagList&dept="+document.forms[0].deptId.value;

	//alert(url);
	reqObject.open("POST",url.toString(), true);
	reqObject.send(null);
//	reqObject.setRequestHeader("Content-Type", "text/xml");

	} //if

return true;
}
var doc;
   var x;
   function getXMLData()
   {
	       doc = respXML;
	    x=doc.documentElement;
      // alert(doc);
       populateTest();


  }

 function populateTest(){
  	var len=x.childNodes[0].childNodes.length;
  	//alert(len);
  	if(len == 0){
  		alert("No tests in this department");
  		return;
  	}
  	var testId = document.getElementById("testId");
  	 for(var i=0;i<len;i++){
  	 testId.options[i+1] = new Option();
     testId.options[i+1].text = x.childNodes[0].childNodes[i].attributes.getNamedItem('class2').nodeValue;
     testId.options[i+1].value = x.childNodes[0].childNodes[i].attributes.getNamedItem('class1').nodeValue;
//    alert(testId.options[i+1].text);
   }
  		testId.disabled=false;

  }
  function validateForm()
  {
      if(document.forms[0].mrno.value==""){
        alert("Select MR No");
        document.forms[0].mrno.focus();
        return false;
      }
      if(document.forms[0].deptId.value==""){
        alert("Select Department");
        document.forms[0].deptId.focus();
        return false;
      }
      if(document.forms[0].testId.value==""){
        alert("Select Test");
        document.forms[0].testId.focus();
        return false;
      }
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
  function report(){
     if(validateForm()){
     	var  mrno = document.forms[0].mrno.value;
        var  dept = document.forms[0].deptId.value;
        var  test = document.forms[0].testId.value;
        var  fromDate = document.forms[0].fromDate.value;
        var  toDate = document.forms[0].toDate.value;
          	url ="diagreport.do?method=getDiagRepeatReport&mrno="+mrno+"&dept="+dept+"&test="+test+"&fromDate="+fromDate+"&toDate="+toDate;
          //	alert(url);
          	window.open(url);
       document.forms[0].mrno.value="";
       document.forms[0].deptId.value="";
       document.forms[0].testId.value="";
       document.forms[0].fromDate.value="";
       document.forms[0].toDate.value="";
       document.forms[0].testId.disabled=true;

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

	</script>

</head>
<body  oncopy="return false" onbeforecut="return false" oncut="return false" >

<form method="POST">




<!-- Main tab--> <TABLE width="100%" height="100%" border="0" cellpadding="0" cellspacing="0">
                    <TR>
                      <TD width="10" height="20"><insta:link type="image" file="sub_img01.gif"/></TD>
                      <TD height="20" background="../../images/sub_img03.gif" class="subtitle" width="100%" align="center">Previous Diagnostics Report</TD>
                      <TD width="10" height="20"><insta:link type="image" file="sub_img02.gif"/></TD>
                    </TR>

                    <TR>
                      <TD width="10" background="../../images/sub_img07.gif" height="100%"><insta:link type="image" file="spacer.gif"/></TD>
                      <TD valign="top" bgcolor="#fbe6d3" width="100%" height="100%">
     <!-- Center tab-->    <TABLE width="100%" border="0" cellpadding="0" cellspacing="0" class="tabletext" height="100%">
                              <TR><TD height="9" width="100%"></TD></TR>
                              <TR><TD class="leftnavbg2" width="100%" valign="top" align="middle">
                                 <table width="820" height="1" border="0" cellpadding="0" cellspacing="0" bordercolor='white'>
           <!-- Constant Cell-->  <tr width="100%" valign="top"><td colspan='3'>&nbsp;</td></tr>
                                  <tr width="100%" valign="top"><td colspan='3'>&nbsp;</td></tr>
                                  <tr width="100%" valign="top"><td colspan='3'>&nbsp;</td></tr>
                                  <tr width="100%" valign="top">
                                          <td align='left' width='35%'></td>
                                         <td align='left' width='10%'>
                                            <span style="font-family:verdhana;color:#800000;font-size:12px">MR No</span></td>
                                         <td align='left' width='55%'>
                                            <select name="mrno" style="width:158" class="forminput">
                          					<option value="">..............Select.............</option>
                          					<logic:present name="mrnoList">
                          						<logic:iterate id="mrno" name="mrnoList">
                          							<option value='<bean:write name="mrno" property="MR_NO"/>'><bean:write name="mrno" property="MR_NO"/>
                          						</logic:iterate>
                          					</logic:present>
                          				</select></td></tr>
                                  <tr width="100%" valign="top"><td colspan='3'>&nbsp;</td></tr>
                                  <tr width="100%" valign="top">
                                          <td align='left' width='35%'></td>
                                          <td align='left' width='10%'>
                                            <span style="font-family:verdhana;color:#800000;font-size:12px">Department</span></td>
                                          <td align='left' width='55%'>
                                             <select name="deptId" style="width:158" class="forminput" onchange="getDiag()">
                          					<option value="">..............Select.............</option>
                          					<logic:present name="deptList">
                          						<logic:iterate id="dept" name="deptList">
                          							<option value='<bean:write name="dept" property="DDEPT_ID"/>'><bean:write name="dept" property="DDEPT_NAME"/>
                          						</logic:iterate>
                          					</logic:present>
                          				</select></td></tr>
                                  <tr width="100%" valign="top"><td colspan='3'>&nbsp;</td></tr>
                                  <tr width="100%" valign="top">
                                          <td align='left' width='35%'></td>
                                          <td align='left' width='10%'>
                                            <span style="font-family:verdhana;color:#800000;font-size:12px">Test</span></td>
                                          <td align='left' width='55%'>
                                            <select name="testId" id="testId" style="width:158" class="forminput" disabled>
                          					<option value="">..............Select.............
                          				</select></td></tr>
                                  <tr width="100%" valign="top"><td colspan='3'>&nbsp;</td></tr>
                                  <tr width="100%" valign="top"><td align='center' colspan='3'>
                                        <span style="font-family:Verdana; font-size:12px; color:#800000">From&nbsp;</span>
                                     <input type="text" name="fromDate" size="27" id="fdate" onclick="javascript:show_calendar('forms[0].fromDate');" class="forminput" readonly>
                                           &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                                     <span style="font-family:Verdana; font-size:12px; color:#800000">To&nbsp;</span>
                                     <input type="text" name="toDate" size="27" id="todate" onclick="javascript:show_calendar('forms[0].toDate');" class="forminput" readonly></td></tr>
                                  <tr width="100%" valign="top"><td colspan='3'>&nbsp;</td></tr>
                                  <tr width="100%" valign="top"><td colspan='3'>&nbsp;</td></tr>
                                  <tr width="100%" valign="top"><td align='center' colspan='3'>
                                            <input type="button" value="Report" onclick="report()" class="button"></td></tr>
                                  <tr width="100%" valign="top"><td colspan='3'>&nbsp;</td></tr>

           <!-- Constant Cell-->  </table>
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
