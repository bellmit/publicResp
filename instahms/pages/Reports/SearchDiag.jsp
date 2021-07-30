<%@page import="java.util.List"%>
<%@taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<html>
<head>
<title>Insta HMS</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<insta:link type="css" file="hms.css"/>
<insta:link type="css" file="aw.css"/>
<insta:link type="script" file="DatePick.js"/>
<insta:link type="script" file="date_go.js"/>
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="script" file="tableSearch.js"/>
<insta:link type="script" file="ajax.js"/>
<insta:link type="script" file="aw.js"/>

	<script language="javascript" type="text/javascript">
	var showReport="none";
var scheck=false;
	function openReport(){
	if(validateForm()){
	    var condition1 ="";
	    for(var i =0;i<document.forms[0].condition.length;i++){
	    	if(document.forms[0].condition[i].checked){
	    		condition1=document.forms[0].condition[i].value;
	  //  		document.forms[0].condition[i].checked=false;
	    		break;
	        }

	    }
	   if(condition1==""){
	   	 window.open("diagreport.do?condition=1&conditionValue=1&method=searchDiagReport&fromDate="+document.forms[0].fromDate.value+"&toDate="+document.forms[0].toDate.value+"&showReport="+showReport);
	   }else{
	     window.open("diagreport.do?condition="+condition1+"&conditionValue="+document.forms[0].conditionValue.value.toUpperCase()+"&method=searchDiagReport&fromDate="+document.forms[0].fromDate.value+"&toDate="+document.forms[0].toDate.value+"&showReport="+showReport);
	   }
	    document.forms[0].conditionValue.value="";
	    document.forms[0].fromDate.value="";
	    document.forms[0].toDate.value="";
	    document.forms[0].docBox.value="";
	    document.forms[0].testBox.value="";
	    document.forms[0].deptBox.value="";
	    document.forms[0].samBox.value="";
	     showReport="none";
	    hideBoxes("");
	    document.forms[0].condition[5].checked=true;
	    scheck=false;
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
	  if(scheck){
		  if(document.forms[0].conditionValue.value==""){
			  alert("Enter search Criteria");
			  return false;
		  }
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


      function showTestBox(){
      	hideBoxes("test");
       document.forms[0].testBox.style.display="";
       showReport="test";
       enableBox();
      }
      function showDeptBox(){
      	hideBoxes("dept");
       document.forms[0].deptBox.style.display="";
       showReport="dept";
       enableBox();
      }
      function showpDocBox(){
      	hideBoxes("doc");
       document.forms[0].docBox.style.display="";
       showReport="pdoc";
       enableBox();
      }
       function showcDocBox(){
      	hideBoxes("doc");
       document.forms[0].docBox.style.display="";
       showReport="cdoc";
       enableBox();
      }
       function showSamBox(){
      	hideBoxes("sam");
       document.forms[0].samBox.style.display="";
       showReport="sam";
       enableBox();
      }


      function hideBoxes(show){
      	if(show !="test"){
      		document.forms[0].testBox.style.display="none";
      	}
      	if(show !="doc"){
      		document.forms[0].docBox.style.display="none";
      	}
      	if(show !="dept"){
      		document.forms[0].deptBox.style.display="none";
      	}
      	if(show !="sam"){

      		document.forms[0].samBox.style.display="none";
      	}


      }
      function outPutText(obj,rep){
      	document.forms[0].conditionValue.value=obj.options[obj.options.selectedIndex].text;
       // alert(document.forms[0].conditionValue.value);
      }
      function disableBox(){
        document.forms[0].conditionValue.disabled=true;
        hideBoxes("");
        scheck=false;
        showReport="none";
      }
      function enableBox(){
        document.forms[0].conditionValue.disabled=false;
        scheck=true;
      }


	</script>

</head>
<body  oncopy="return false" onbeforecut="return false" oncut="return false" >

<form method="POST">





<!-- Main tab--> <TABLE width="100%" height="100%" border="0" cellpadding="0" cellspacing="0">
                    <TR>
                      <TD width="10" height="20"><insta:link type="image" file="sub_img01.gif"/></TD>
                      <TD height="20" background="../../images/sub_img03.gif" class="subtitle" width="100%" align="center">Search For Diagnostics Report</TD>
                      <TD width="10" height="20"><insta:link type="image" file="sub_img02.gif"/></TD>
                    </TR>

                    <TR>
                      <TD width="10" background="../../images/sub_img07.gif" height="100%"><insta:link type="image" file="spacer.gif"/></TD>
                      <TD valign="top" bgcolor="#fbe6d3" width="100%" height="100%">
     <!-- Center tab-->    <TABLE width="100%" border="0" cellpadding="0" cellspacing="0" class="tabletext" height="100%">
                              <TR><TD height="9" width="100%"></TD></TR>
                              <TR><TD class="leftnavbg2" width="100%" valign="top" align="middle">
                                 <table width="820" height="1" border="0" cellpadding="0" cellspacing="0" bordercolor='white'>
           <!-- Constant Cell-->  <tr width="100%" valign="top"><td>&nbsp;</td></tr>
                                  <tr width="100%" valign="top"><td>&nbsp;</td></tr>
                                  <tr width="100%" valign="top"><td>&nbsp;</td></tr>

                                  <tr width="100%" valign="top"><td align='center'>
                                     <span style="font-family:Verdana; font-size:12px; color:#800000">From&nbsp;</span>
                                     <input type="text" name="fromDate" size="27" id="fdate" onclick="javascript:show_calendar('forms[0].fromDate');" class="forminput" readonly>
                                           &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                                     <span style="font-family:Verdana; font-size:12px; color:#800000">To&nbsp;</span>
                                     <input type="text" name="toDate" size="27" id="todate" onclick="javascript:show_calendar('forms[0].toDate');" class="forminput" readonly></td></tr>

                                  <tr width="100%" valign="top"><td>&nbsp;</td></tr>
                                  <tr width="100%" valign="top"><td align='center'>
                                      <span style="font-family:Verdana; font-size:12px; color:#800000">Search Criteria</span>&nbsp;&nbsp;&nbsp;
                                      <INPUT type="text" id="textBox" size="27" name="conditionValue" size="20" class="forminput" disabled> </td></tr>
                                  <tr width="100%" valign="top"><td>&nbsp;</td></tr>
                                  <tr width="100%" valign="top"><td align='center'>&nbsp;

                                       <select name="deptBox" style="display:none" style="width:160" class="forminput" onchange="outPutText(this)">
		                          	      <option value="">.....Select Department.....</option>
		                          	      <logic:present name="deptList">
		                          		  <logic:iterate name="deptList" id = "dept">
		                          			<option value="<bean:write name='dept' property='DDEPT_ID' />"><bean:write name="dept" property="DDEPT_NAME" /></option>
		                          		  </logic:iterate>
		                          	      </logic:present>
		                               </select>

		                               <select name="testBox" style="display:none" class="forminput" style="width:160" onchange="outPutText(this)">
		                          	      <option value="">..........Select Test..........</option>
		                          	      <logic:present name="testList" >
		                          		  <logic:iterate name="testList" id = "test">
		                          			<option value="<bean:write name='test' property='TEST_ID' />"><bean:write name="test" property="TEST_NAME" /></option>
		                          		  </logic:iterate>
		                          	      </logic:present>
		                               </select>

		                               <select name="docBox" style="display:none" class="forminput" style="width:160" onchange="outPutText(this)">
		                          	      <option value="">..........Select Doctor..........</option>
		                          	      <logic:present name="docList" >
		                          		  <logic:iterate name="docList" id = "doc">
		                          			<option value="<bean:write name='doc' property='DOCTOR_ID' />"><bean:write name="doc" property="DOCTOR_NAME" /></option>
		                          		  </logic:iterate>
		                          	      </logic:present>
		                               </select>

		                               <select name="samBox" style="display:none" class="forminput" style="width:160" onchange="outPutText(this)">
		                          	      <option value="">.....Select Sample No.....</option>
		                          	      <logic:present name="samList" >
		                          		  <logic:iterate name="samList" id = "sam">
		                          			<option value="<bean:write name='sam' property='SAMPLE_SNO' />"><bean:write name="sam" property="SAMPLE_SNO" /></option>
		                          		  </logic:iterate>
		                          	      </logic:present>
		                               </select>

                                     </td></tr>

                                  <tr width="100%" valign="top"><td>&nbsp;</td></tr>
                                  <tr width="100%" valign="top"><td align='center'>
                                      <span style="font-family:Verdana; font-size:12px; color:#800000">
                                      <INPUT type="radio" name="condition" value="d.test_name" onclick="showTestBox()">   Test Name&nbsp;&nbsp;
		                              <INPUT type="radio" name="condition" value="dd.ddept_name" onclick="showDeptBox()">   Dept Name&nbsp;&nbsp;
		                              <INPUT type="radio" name="condition" value="doc.doctor_name" onclick="showpDocBox()">   Prescribed By&nbsp;&nbsp;
		                              <INPUT type="radio" name="condition" value="doc1.doctor_name" onclick="showcDocBox()">   Conducted By&nbsp;&nbsp;
		                              <INPUT type="radio" name="condition" value="sc.sample_sno" onclick="showSamBox()"> Sample No&nbsp;
		                              <INPUT type="radio" name="condition" value="" onclick="disableBox()" checked> None</span></td></tr>
                                  <tr width="100%" valign="top"><td>&nbsp;</td></tr>
                                  <tr width="100%" valign="top"><td align='center'>
                                       <INPUT type="button" value="Search" name="B1" class="button" onclick="openReport();"></td></tr>


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